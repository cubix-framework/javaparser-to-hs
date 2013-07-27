package projectbuilder.run;
// imports {{{
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import projectviewer.vpt.VPTProject;

import console.Shell;
import console.Console;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.gui.DockableWindowManager;

import groovy.swing.SwingBuilder;

import common.gui.ListPanel;
// }}}
/**
 * This class handles building projects and editing build commands
 * Ant commands are routed through AntFarm, others are run in the system shell
 */
public class RunCommand {
	
	public static JDialog settings;
	
	public static void run(final View view, final VPTProject proj) {
		
		ArrayList<String> commands = getCommandList(proj);
		if (commands == null) {
			GUIUtilities.error(view, "projectBuilder.msg.no-run-command", null);
			return;
		}
		final String cmd; // The final command to run
		if (commands.size()>1) {
			// Prompt for which command to use
			cmd = (String) JOptionPane.showInputDialog(view,
												"Run this project with:",
												"Run",
												JOptionPane.PLAIN_MESSAGE,
												GUIUtilities.loadIcon("22x22/actions/application-run.png"),
												commands.toArray(),
												proj.getProperty("projectBuilder.command.run.last"));
			if (cmd == null) return;
			proj.setProperty("projectBuilder.command.run.last", cmd);
		} else {
			cmd = commands.get(0);
		}
		
		final DockableWindowManager wm = view.getDockableWindowManager();
		
		new Thread(new Runnable() {
			public void run() {
				// Run in system shell
				wm.addDockableWindow("console");
				Console console = (Console) wm.getDockable("console");
				console.setShell("System");
				Shell system = Shell.getShell("System");
				String cd = "cd \""+proj.getRootPath()+"\"";
				system.execute(console, null, console.getShellState(system), null, cd);
				system.waitFor(console);
				system.execute(console, null, console.getShellState(system), null, cmd);
				view.getDockableWindowManager().showDockableWindow("console");
			}
		}).start();
	}
	
	public static void editCommands(View view, VPTProject proj) {
		settings = new RunSettingsPanel(view, "Project Run Settings", proj);
	}
	
	public static ArrayList<String> getCommandList(VPTProject proj) {
		if (proj.getProperty("projectBuilder.command.run.0") == null) return null;
		ArrayList<String> commands = new ArrayList<String>();
		for (int i = 0; true; i++) {
			String cmd = proj.getProperty("projectBuilder.command.run."+i);
			if (cmd == null) break;
			commands.add(cmd);
		}
		return commands;
	}
	
}
