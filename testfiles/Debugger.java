/*
Copyright (C) 2007  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package perl.core;

import perl.breakpoints.Breakpoint;
import perl.breakpoints.BreakpointList;
import perl.breakpoints.BreakpointTableView;
import perl.breakpoints.BreakpointView;
import perl.context.StackTrace;
import perl.core.GdbState.State;
import perl.core.GdbState.StateListener;
import perl.core.Parser.GdbResult;
import perl.core.Parser.ResultHandler;
import perl.execution.ControlView;
import perl.launch.LaunchConfiguration;
import perl.launch.LaunchConfigurationListDialog;
import perl.launch.LaunchConfigurationManager;
import perl.options.GeneralOptionPane;
import perl.output.MIShell;
import perl.output.ProgramShell;
import perl.proc.PerlProcess;
import perl.variables.GdbVar;
import perl.variables.LocalVariables;
import perl.variables.Variables;
import perl.variables.Watches;
import perl.variables.GdbVar.UpdateListener;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;

import debugger.itf.DebuggerTool;
import debugger.itf.IData;
import debugger.itf.JEditFrontEnd;
import debugger.jedit.Plugin;

public class Debugger implements DebuggerTool {

	private static Debugger debugger = null;

	private JEditFrontEnd frontEnd = null;

	private ControlView controlView = null;
	private BreakpointView breakpointsPanel = null;
	private BreakpointTableView gdbBreakpointsPanel = null;
	private LocalVariables localsPanel = null;
	private StackTrace stackTracePanel = null;
	private Watches watchesPanel = null;
	private Variables variablesPanel = null;
	// Command manager
	private CommandManager commandManager = null;
	// Parser
	private Parser parser = null;
	// Gdb internal message
	private String gdbInternalMessage = null;

	public static final String KILL_ACTION = "debugger-kill";
	public static final String GO_ACTION = "debugger-go";
	public static final String NEXT_ACTION = "debugger-next";
	public static final String STEP_ACTION = "debugger-step";
	public static final String FINISH_ACTION = "debugger-finish";
	public static final String UNTIL_ACTION = "debugger-until";
	public static final String PAUSE_ACTION = "debugger-pause";
	public static final String QUIT_ACTION = "debugger-quit";
	public static final String TOGGLE_BREAKPOINT_ACTION = "debugger-toggle-breakpoint";
	public static final String EDIT_LAUNCH_CONFIGS_ACTION = "debugger-edit-launch-configs";
	public static final String SHOW_WATCHES = "debugger-watches";

	public static final String COULD_NOT_GET_VALUE =
		Plugin.MESSAGE_PREFIX + "could_not_get_value";

	private VariableTooltipTextAreaExtension varTooltipExtension = null;

	private PerlProcess perlProcess = null;

	public IData getData(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void editLaunchConfigs(View view) {
		LaunchConfigurationListDialog dlg = new LaunchConfigurationListDialog(view);
		dlg.setVisible(true);
	}
	public void selectLaunchConfig(View view) {
		LaunchConfigurationManager mgr = LaunchConfigurationManager.getInstance();
		Vector<LaunchConfiguration> configs = mgr.get();
		LaunchConfiguration sel = (LaunchConfiguration)
			JOptionPane.showInputDialog(view,
						"Program",
						"Select:",
						JOptionPane.QUESTION_MESSAGE,
						null,
						configs.toArray(),
						mgr.getDefault());
		if (sel == null)
			return;
		mgr.setDefaultIndex(configs.indexOf(sel));
		mgr.save();
		go();
	}
	
	public void go() {
		if (! isRunning())
			start();
		else 
			commandManager.add("-exec-continue");
	}

	public void pause() {
		if (isRunning() && (perlProcess != null))
			perlProcess.pause();
	}

	public void quit() {
		if (! isRunning())
			return;
		commandManager.add("-gdb-exit", new ResultHandler() {
			public void handle(String msg, GdbResult res) {
				if (msg.equals("exit")) {
					sessionEnded();
				}
			}
		});
	}

	public void next() {
		if (isRunning())
			commandManager.add("-exec-next");
	}

	public void step() {
		if (isRunning())
			commandManager.add("-exec-step");
	}

	public void finishCurrentFunction() {
		if (isRunning())
			commandManager.add("-exec-finish");
	}

	public void runToCursor() {
		View view = jEdit.getActiveView();
		Buffer buffer = view.getBuffer();
		JEditTextArea ta = view.getTextArea();
		int line = ta.getCaretLine() + 1;
		if (isRunning())
			commandManager.add("-exec-until " + buffer.getPath() + ":" + line);
	}
	public void kill() {
		sessionEnded();
		destroy();
	}
	public void destroy() {
		if (perlProcess != null) {
			perlProcess.destroy();
			perlProcess = null;
		}
	}
	
	private void sessionEnded() {
		parser = null;
		commandManager = null;
		GdbState.setState(State.IDLE);
		frontEnd.programExited();
	}
	public void start() {
		setGdbStateListener();
		LaunchConfiguration currentConfig =
			LaunchConfigurationManager.getInstance().getDefault();
		if (currentConfig == null) {
			JOptionPane.showMessageDialog(jEdit.getActiveView(),
					"No program is selected for debugging.\n" +
					"Please do one of the following:\n" +
					"- Use the GdbPlugin options to specify the default " +
					"program for debugging ('Make default' button)\n" +
					"- Use 'Debug ...' from the GdbPlugin menu and select " +
					"the program you wish to debug from the list");
			return;
		}
		// Clear the consoles
		getProgramShell().clear();
		getMIShell().clear();
		// Start the debugging process
		debugger.start(currentConfig);
	}
	private void setGdbStateListener() {
		GdbState.addStateListener(new StateListener() {
			public void stateChanged(State prev, State current) {
				final State cur = current;
				VFSManager.runInAWTThread(new Runnable() {
					public void run() {
						if (cur == GdbState.State.PAUSED) {
							if (! jEdit.getBooleanProperty(GeneralOptionPane.EXPRESSION_TOOLTIP_PROP))
								return;
							if (varTooltipExtension != null)	// Don't duplicate extensions
								return;
							JEditTextArea ta = jEdit.getActiveView().getTextArea();
							varTooltipExtension = new VariableTooltipTextAreaExtension(ta);
							ta.getPainter().addExtension(varTooltipExtension);
						} else {
							if (varTooltipExtension != null) {
								JEditTextArea ta = varTooltipExtension.getTextArea();
								ta.getPainter().removeExtension(varTooltipExtension);
								varTooltipExtension = null;
							}
						}
					}
				});
			}
		});
	}

	public void start(LaunchConfiguration config) {
		try {
			perlProcess = new PerlProcess(config);
		} catch (IOException e) {
			e.printStackTrace();
			perlProcess = null;
			return;
		}
		parser = new Parser(this, perlProcess);
		parser.addOutOfBandHandler(new OutOfBandHandler());
		parser.start();
		commandManager = new CommandManager(perlProcess, parser);
		commandManager.start();
		GdbState.setState(State.RUNNING);
		// First set up the arguments
		commandManager.add("-exec-arguments " + config.getArguments());
		// Now set up the breakpoints
		Vector<Breakpoint> bps = BreakpointList.getInstance().getBreakpoints();
		for (int i = 0; i < bps.size(); i++) {
			Breakpoint b = bps.get(i);
			b.initialize();
			if (! b.isEnabled())
				b.setEnabled(false);
		}
		commandManager.add("-exec-run");
	}

	public void toggleBreakpoint(View view)
	{
		Buffer buffer = view.getBuffer();
		JEditTextArea ta = view.getTextArea();
		int line = ta.getCaretLine() + 1;
		Vector<Breakpoint> breakpoints =
			BreakpointList.getInstance().get(buffer.getPath(), line);
		if (breakpoints.isEmpty())
			setBreakpoint(view, buffer, line);
		else
			removeBreakpoint(view);
	}
	public void setBreakpoint(View view) {
		Buffer buffer = view.getBuffer();
		JEditTextArea ta = view.getTextArea();
		int line = ta.getCaretLine() + 1;
		setBreakpoint(view, buffer, line);
	}
	// Add the selected text to the watches view
	public void watchSelection(View view) {
		String selected = view.getTextArea().getSelectedText();
		if (watchesPanel == null)
			jEdit.getAction(SHOW_WATCHES).invoke(view);
		if (watchesPanel != null)
			watchesPanel.addWatch(selected);
	}
	// Show a tooltip with the value of the selected text
	public void evaluateSelection(final View view) {
		String selected = view.getTextArea().getSelectedText();
		GdbVar v = new GdbVar(selected);
		v.setChangeListener(new PopupEvaluationUpdateListener(view));
		v.done();
	}
	private void setBreakpoint(View view, Buffer buffer, int line) {
		new Breakpoint(view, buffer, line);
	}
	public void removeBreakpoint(View view) {
		Buffer buffer = view.getBuffer();
		JEditTextArea ta = view.getTextArea();
		int line = ta.getCaretLine() + 1;
		Vector<Breakpoint> breakpoints =
			BreakpointList.getInstance().get(buffer.getPath(), line);
		if (breakpoints.isEmpty())
			return;
		for (int i = 0; i < breakpoints.size(); i++) {
			Breakpoint b = breakpoints.get(i);
			b.remove();
		}
	}

	private void stopped(String file, int line) {
		GdbState.setState(State.PAUSED);
		frontEnd.setCurrentLocation(file, line);
	}

	public void breakpointHit(int bkptno, String file, int line) {
		String msg = "Breakpoint " + bkptno + " hit";
		if (file != null)
			msg = msg + ", at " + file + ":" + line + ".";
		//System.err.println(msg);
		if (jEdit.getBooleanProperty(GeneralOptionPane.SHOW_BREAKPOINT_POPUP_PROP))
			JOptionPane.showMessageDialog(jEdit.getActiveView(), msg);
		jEdit.getActiveView().getStatus().setMessage(msg);
	}

	public void signalReceived(String signal) {
		String msg = "Received signal: " + signal;
		JOptionPane.showMessageDialog(jEdit.getActiveView(), msg);
	}
	
	public void setFrontEnd(JEditFrontEnd frontEnd) {
		this.frontEnd = frontEnd;
	}

	private static final class PopupEvaluationUpdateListener implements
			UpdateListener {
		private final View view;

		private PopupEvaluationUpdateListener(View view) {
			this.view = view;
		}

		public void updated(GdbVar v) {
			JOptionPane.showMessageDialog(view, v.toString());
		}
	}
	private static final class VariableTooltipTextAreaExtension extends TextAreaExtension {
		private static final long VAR_VALUE_TIMEOUT = 500;
		JEditTextArea textArea;
		private boolean gotValue;
		public VariableTooltipTextAreaExtension(JEditTextArea ta) {
			textArea = ta;
		}
		public JEditTextArea getTextArea() {
			return textArea;
		}
		@Override
		public String getToolTipText(int x, int y) {
			int offset = textArea.xyToOffset(x, y);
			int line = textArea.getLineOfOffset(offset);
			int index = offset - textArea.getLineStartOffset(line);
			String text = textArea.getLineText(line);
			Pattern expressionPattern = Pattern.compile(
					jEdit.getProperty(GeneralOptionPane.EXPRESSION_REGEXP_PROP));
			Matcher m = expressionPattern.matcher(text);
			int end = -1;
			int start = -1;
			String selected = "";
			while (end <= index) {
				if (! m.find())
					return null;
				end = m.end();
				start = m.start();
				selected = m.group();
			}
			if (start > index || selected.length() == 0)
				return null;
			gotValue = false;
			GdbVar v = new GdbVar(selected);
			v.setChangeListener(new UpdateListener() {
				public void updated(GdbVar v) {
					synchronized(v) {
						gotValue = true;
						v.notify();
					}
				}
			});
			try {
				synchronized(v) {
					if (! gotValue)
						v.wait(VAR_VALUE_TIMEOUT);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String s = gotValue ? v.toString() :
				jEdit.getProperty(COULD_NOT_GET_VALUE, new String []{selected});
			v.done();
			return s;
		}
	}
	private class BreakpointHitHandler implements ResultHandler {
		int bkptno;
		BreakpointHitHandler(int bkptno) {
			this.bkptno = bkptno;
		}
		public void handle(String msg, GdbResult res) {
			String file = res.getStringValue("fullname");
			int line = 0;
			if (file != null) {
				line = Integer.parseInt(res.getStringValue("line"));
			}
			breakpointHit(bkptno, file, line);
			stopped(file, line);
		}
	}
	private class StoppedHandler implements ResultHandler {
		public void handle(String msg, GdbResult res) {
			String file = res.getStringValue("fullname");
			int line = 0;
			if (file != null) {
				line = Integer.parseInt(res.getStringValue("line"));
			}
			stopped(file, line);
		}
	}
	private class OutOfBandHandler implements ResultHandler {
		public void handle(String msg, GdbResult res) {
			if (msg.equals("running"))
				return;
			final String getCurrentPosition = "-file-list-exec-source-file";
			String reason = res.getStringValue("reason");
			if (reason.equals("breakpoint-hit")) {
				int bkptno = Integer.parseInt(res.getStringValue("bkptno"));
				commandManager.add(getCurrentPosition, new BreakpointHitHandler(bkptno));
			} else if (reason.startsWith("exited")) {
				sessionEnded();
			} else if (reason.startsWith("signal-received")) {
				signalReceived(res.getStringValue("signal-meaning"));
				commandManager.add(getCurrentPosition, new StoppedHandler());
			} else {
				commandManager.add(getCurrentPosition, new StoppedHandler());
			}
		}
	}

	public JEditFrontEnd getFrontEnd() {
		return frontEnd;
	}

	public JPanel showControlPanel(View view) {
		if (controlView == null)
			controlView = new ControlView();
		return controlView;
	}
	public JPanel showBreakpoints(View view) {
		if (breakpointsPanel == null)
			breakpointsPanel = new BreakpointView();
		return breakpointsPanel;
	}
	public JPanel showGdbBreakpoints(View view) {
		if (gdbBreakpointsPanel == null)
			gdbBreakpointsPanel = new BreakpointTableView();
		return gdbBreakpointsPanel;
	}
	public JPanel showLocals(View view) {
		if (localsPanel == null)
			localsPanel = new LocalVariables();
		return localsPanel;
	}
	public JPanel showWatches(View view) {
		variablesPanel = null;
		if (watchesPanel == null)
			watchesPanel = new Watches();
		return watchesPanel;
	}
	public JPanel showVariables(View view) {
		if (variablesPanel == null) {
			if (watchesPanel == null) {
				variablesPanel = new Variables();
				watchesPanel = variablesPanel.getWatches();
			} else {
				variablesPanel = new Variables(watchesPanel);
			}
		}
		return variablesPanel;
	}
	public JPanel showStackTrace(View view) {
		if (stackTracePanel == null)
			stackTracePanel = new StackTrace();
		return stackTracePanel;
	}
	private MIShell getMIShell() {
		return (MIShell) ServiceManager.getService("console.Shell", MIShell.NAME);
	}
	public void gdbRecord(String line)
	{
		//System.err.println(line);
		getMIShell().append(line);
	}
	public void commandRecord(String line)
	{
		gdbInternalMessage = null;
		//System.err.println(line);
		getMIShell().append(">>> CommandManager: " + line);
	}
	private ProgramShell getProgramShell() {
		return (ProgramShell) ServiceManager.getService("console.Shell", ProgramShell.NAME);
	}
	public void programError(String line) {
		getProgramShell().appendError(line);
	}
	public void programRecord(String line)
	{
		getProgramShell().append(line);
	}
	public static Debugger getInstance() {
		if (debugger == null)
			debugger = new Debugger();
		return debugger;
	}

	public boolean isRunning() {
		return GdbState.isRunning();
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}
	public Parser getParser() {
		return parser;
	}

	public void gdbMessage(String msg) {
		gdbInternalMessage = msg;
	}
	
	public String getGdbMessage() {
		return gdbInternalMessage;
	}
}
