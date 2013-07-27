package projectbuilder.build;
// imports {{{
import javax.swing.JOptionPane;
import javax.swing.JComponent;
import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.Properties;
import java.util.ArrayList;
import java.io.File;

import projectviewer.vpt.VPTProject;

import console.Shell;
import console.Console;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.util.Log;

import groovy.swing.SwingBuilder;

import common.gui.ListPanel;
// }}}
/**
 * This class handles building projects and editing build commands
 * Ant commands are routed through AntFarm, others are run in the system shell
 */

public class BuildCommand {
	
	public static JDialog settings;
	
	public static void run(final View view, final VPTProject proj) {
		
		ArrayList<String> commands = getCommandList(proj);
		if (commands == null) {
			GUIUtilities.error(view, "projectBuilder.msg.no-build-command", null);
			return;
		}
		final String cmd; // The final command to run
		if (commands.size()>1) {
			// Prompt for which command to use
			// TODO: For ant commands, try and find a way to display a cleaner description, such as "ANT build"
			cmd = (String) JOptionPane.showInputDialog(view,
												"Build this project with:",
												"Build",
												JOptionPane.PLAIN_MESSAGE,
												GUIUtilities.loadIcon("22x22/actions/application-run.png"),
												commands.toArray(),
												proj.getProperty("projectBuilder.command.build.last"));
			if (cmd == null) return;
			proj.setProperty("projectBuilder.command.build.last", cmd);
		} else {
			cmd = commands.get(0);
		}
		
		final DockableWindowManager wm = view.getDockableWindowManager();
		
		new Thread(new Runnable() {
			public void run() {
				if (cmd.startsWith("ANT[")) {
					// Build in AntFarm
					if (jEdit.getPlugin("antfarm.AntFarmPlugin") == null) {
						GUIUtilities.error(view, "projectBuilder.msg.no-ant-farm", null);
						return;
					}
					Properties props = parseAntCommand(cmd);
					String buildfile = props.getProperty("buildfile");
					wm.addDockableWindow("console");
					Console console = (Console) wm.getDockable("console");
					Shell ant = Shell.getShell("Ant");
					if (buildfile == null)
						buildfile = proj.getRootPath()+File.separator+"build.xml";
					Log.log(Log.DEBUG, BuildCommand.class, "Adding ant buildfile "+buildfile);
					console.run(ant, "+"+buildfile);
					ant.waitFor(console);
					console.run(ant, "!"+props.getProperty("target", ""));
					view.getDockableWindowManager().showDockableWindow("console");
					//new BuildWatcher(console).start();
					// QUESTION: Is there a way to run AntFarm commands without having the AntFarm window pop up? All we need is the shell.
				} else {
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
				System.gc();
			}
		}).start();
	}
	
	public static Properties parseAntCommand(String cmd) {
		Properties props = new Properties();
		String[] list = cmd.substring(4, cmd.indexOf("]")).split(",");
		for (int i=0; i<list.length; i++) {
			int equals = list[i].indexOf("=");
			if (equals == -1) continue;
			props.setProperty(list[i].substring(0, equals),
				list[i].substring(equals+1, list[i].length()));
		}
		return props;
	}
	
	public static void editCommands(View view, VPTProject proj) {
		settings = new BuildSettingsPanel(view, "Project Build Settings", proj);
	}
	
	public static ArrayList<String> getCommandList(VPTProject proj) {
		if (proj.getProperty("projectBuilder.command.build.0") == null) return null;
		ArrayList<String> commands = new ArrayList<String>();
		for (int i = 0; true; i++) {
			String cmd = proj.getProperty("projectBuilder.command.build."+i);
			if (cmd == null) break;
			commands.add(cmd);
		}
		return commands;
	}
	
}
