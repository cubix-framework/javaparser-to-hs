/*
 *  TargetRunner.java - Plugin for running Ant builds from jEdit.
 *  Copyright (C) 2001 Brian Knowles
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package antfarm;

import java.io.File;
import java.io.PrintStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.Log;

import console.Console;
import console.ConsolePlugin;
import console.Output;
import console.Shell;

public class TargetRunner extends Thread
{

    PrintStream _out = System.out;

    Project runner = new Project();

    Target runAnt = new Target();

    PrintStream _err = System.err;

    DefaultLogger _buildLogger = new DefaultLogger();

    Throwable _error;

    Target _target;

    Project _project;

    File _buildFile;

    View _view;

    Output _output;

    Properties _userProperties;

    PrintStream _consoleOut;

    PrintStream _consoleErr;

    public TargetRunner(Target target, File buildFile, View view, Output output,
        Properties userProperties)
    {
        init(target, buildFile, view, output, userProperties);
    }

    public TargetRunner(Project project, File buildFile, View view, Output output,
        Properties userProperties)
    {
        Target target = (Target) project.getTargets().get(project.getDefaultTarget());
        init(target, buildFile, view, output, userProperties);
    }

    @Override
    public void run()
    {
        AntFarmPlugin.getErrorSource().clear();

        if (jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX + "save-on-execute"))
        {
            jEdit.saveAllBuffers(_view, false);

            // Have our Ant run wait for any IO to finish up.
            VFSManager.waitForRequests();
            runAntTarget();
        }
        else
        {
            runAntTarget();
        }
    }

    void resetLogging()
    {
        _consoleErr.flush();
        _consoleOut.flush();
        System.setOut(_out);
        System.setErr(_err);
        _project.removeBuildListener(_buildLogger);
    }

    private void setOutputStreams()
    {
        System.setOut(_consoleOut);
        System.setErr(_consoleErr);
    }

    private void runAntTarget()
    {

        boolean useSameJvm = jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX
            + "use-same-jvm");
        String antCommand = jEdit.getProperty(AntFarmPlugin.OPTION_PREFIX + "command");

        if (!useSameJvm && antCommand == null)
            promptForAntCommand();

        useSameJvm = jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX + "use-same-jvm");

        // assume we have the console open at this point since it called
        // us.
        Console console = (Console) _view.getDockableWindowManager().getDockable("console");

        if (useSameJvm)
        {
        	// Check if AntFarm is installed
        	if (jEdit.getPlugin("ant.AntPlugin") == null) {
        		GUIUtilities.error(_view, "ant-plugin-not-installed", null);
        		return;
        	}
            setOutputStreams();
            loadProjectProperties();
            AntFarmPlugin.loadCustomClasspath();

            try
            {
                _project.addBuildListener(_buildLogger);

                fireBuildStarted();
                _project.executeTarget(_target.getName());
            }
            catch (RuntimeException exc)
            {
                _error = exc;
            }
            catch (Error e)
            {
                _error = e;
            }
            finally
            {
                fireBuildFinished();
                resetLogging();
                cleanup();
            }
        }
        else
        {
            String command = " -buildfile ";
            command += "\"";
            command += _buildFile.getAbsolutePath();
            command += "\"";
            command += ' ' + _target.getName();
            runAntCommand(command);
        }
        cleanup();
    }

    private void cleanup()
    {
        System.gc();
        _output.commandDone();
        _view.getTextArea().requestFocus();
    }

    private void loadProjectProperties()
    {
        // re-init the project so that system properties are re-loaded.
        _project.init();
        _project.setUserProperty("ant.version", Main.getAntVersion());

        // set user-define properties
	for (Object o : _userProperties.keySet())
	{
		String arg = (String) o;
		String value = _userProperties.getProperty(arg);
		value = ConsolePlugin.expandSystemShellVariables(_view, value);
		_project.setUserProperty(arg, value);
	}

        _project.setUserProperty("ant.file", _buildFile.getAbsolutePath());
    }

    private void init(Target target, File buildFile, View view, Output output,
        Properties userProperties)
    {
        _target = target;
        // _project = _target.getProject();
        _buildFile = buildFile;
        _view = view;
        _output = output;
        _userProperties = userProperties;

        //_view.getDockableWindowManager().addDockableWindow("antfarm");
        //AntFarm antFarm = (AntFarm) _view.getDockableWindowManager().getDockable("antfarm");
        try
        {
            _project = AntFarmPlugin.parseBuildFile(buildFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            Log.log(Log.WARNING, this, "Cannot parse build file: " + e);
        }

        _consoleErr = new AntPrintStream(System.out, _view, _output);
        _consoleOut = new AntPrintStream(System.out, _view, _output);

        configureBuildLogger();

        // set so jikes prints emacs style errors
        _userProperties.setProperty("build.compiler.emacs", "true");

        // add in the global properties
        addGlobalProperties();

        // fire it up
        this.start();
    }

    private void addGlobalProperties()
    {
        String name;
        int counter = 1;
        while ((name = jEdit.getProperty(PropertiesOptionPane.PROPERTY + counter
            + PropertiesOptionPane.NAME)) != null)
        {

            String value = jEdit.getProperty(PropertiesOptionPane.PROPERTY + counter
                + PropertiesOptionPane.VALUE);
            _userProperties.setProperty(name, value);
            counter++;
        }

    }

    private void runAntCommand(String args)
    {
        String command = jEdit.getProperty(AntFarmPlugin.OPTION_PREFIX + "command");
        if (command == null || command.isEmpty())
            Log.log(Log.WARNING, this,
                "Please set the path to the Ant script you wish to use.");
	else
        {
            command = '"' + command + '"';

            command += AntFarmShell.getAntCommandFragment(_userProperties);
            if (jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX + "output-emacs"))
            {
                command += " -emacs ";
            }
            command += args;
            Console console = AntFarmPlugin.getConsole(_view);
            Shell antShell = console.getShell();

            Shell systemShell = ConsolePlugin.getSystemShell();
            console.run(systemShell, _output, command);

            // Bring the Ant Console to the front.
            // AntFarmPlugin.getConsole( _view, true );
            console.setShell(antShell);

            // Wait for and stop system shell animation.
            // ConsolePlugin.getSystemShell().waitFor(console);
            // console.setShell(ConsolePlugin.getSystemShell());
            // console.getOutput().commandDone();
            systemShell.waitFor(console);

            console.getOutput().commandDone();

            // Bring the Ant Console to the front.
            // AntFarmPlugin.getConsole( _view, true );
            console.setShell(antShell);
        }
    }

    private void configureBuildLogger()
    {
        _buildLogger.setEmacsMode(jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX
            + "output-emacs"));
        _buildLogger.setOutputPrintStream(_consoleOut);
        _buildLogger.setErrorPrintStream(_consoleErr);
        _buildLogger.setMessageOutputLevel(jEdit
            .getIntegerProperty(AntFarmPlugin.OPTION_PREFIX + "logging-level",
                LogLevelEnum.INFO.getValue()));
    }

    private void fireBuildStarted()
    {
        BuildEvent event = new BuildEvent(_project);
        event.setMessage("Running target: " + _target, Project.MSG_INFO);
        _buildLogger.buildStarted(event);
        _buildLogger.messageLogged(event);
    }

    private void fireBuildFinished()
    {
        BuildEvent event = new BuildEvent(_project);
        event.setException(_error);
        _buildLogger.buildFinished(event);
    }

    private String promptForAntCommand()
    {
        Object[] options = {
            jEdit.getProperty(AntFarmPlugin.OPTION_PREFIX + "select-path-button"),
            jEdit.getProperty(AntFarmPlugin.OPTION_PREFIX + "use-jvm-button") };
        int yesOrNo = JOptionPane.showOptionDialog(_view, jEdit
            .getProperty(AntFarmPlugin.OPTION_PREFIX + "prompt"), jEdit
            .getProperty(AntFarmPlugin.OPTION_PREFIX + "prompt-dialog-title"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
            options[0]);

	    if (yesOrNo == JOptionPane.YES_OPTION)
	    {
		    String scriptFilePath = AntFarmOptionPane.promptForAntScript(_view);
		    jEdit.setProperty(AntFarmPlugin.OPTION_PREFIX + "command", scriptFilePath);
		    jEdit.setBooleanProperty(AntFarmPlugin.OPTION_PREFIX + "use-same-jvm", false);
		    jEdit.propertiesChanged();
		    return scriptFilePath;
	    }
	    if (yesOrNo == JOptionPane.NO_OPTION)
	    {
		jEdit.setBooleanProperty(
		    AntFarmPlugin.OPTION_PREFIX + "use-same-jvm", true);
		jEdit.propertiesChanged();
	    }
	    return null;
    }
}
