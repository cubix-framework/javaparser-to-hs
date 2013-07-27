/*
 *  TemplatesPlugin.java - Plugin for importing code templates
 *  Copyright (C) 1999 Steve Jakob
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
package templates;

import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.msg.DynamicMenuChanged;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;
import velocity.BufferWriter;
import velocity.VelocityConstants;
import velocity.jEditContext;

/**
 * A jEdit plugin for adding a templating function.
 *
 * @author   Steve Jakob
 */
public class TemplatesPlugin extends EditPlugin
		 implements RuntimeConstants, VelocityConstants
{
	private static String defaultTemplateDir;
	private static String defaultVelocityDir;
	private static String sepChar;// System-dependant separator character
	private static TemplateDir templates = null;
	private static VelocityEngine engine;

	/**
	 * Returns the root TemplateDir object, which represents templates as a
	 * hierarchical tree of TemplateDir and TemplateFile objects.
	 *
	 * @return   The current TemplateDir object.
	 */
	public static TemplateDir getTemplates()
	{
		return templates;
	}

	/**
	 * Sets the root TemplateDir object to another value.
	 *
	 * @param newTemplates  The new TemplateDir object
	 */
	public static void setTemplates(TemplateDir newTemplates)
	{
		templates = newTemplates;
	}

	/**
	 * Returns the directory where templates are stored
	 *
	 * @return   A string containing the template directory path.
	 */
	public static String getTemplateDir()
	{
		String templateDir = jEdit.getProperty("plugin.TemplatesPlugin.templateDir.0", "");
		if (templateDir.equals(""))
		{
			templateDir = defaultTemplateDir;
		}
		return templateDir;
	}

	/**
	 * Change the directory where templates are stored
	 *
	 * @param templateDirVal  The new templates directory
	 */
	public static void setTemplateDir(String templateDirVal)
	{
		if (!templateDirVal.endsWith(sepChar))
		{
			templateDirVal = templateDirVal + sepChar;
		}
		if (defaultTemplateDir.equals(templateDirVal))
		{
			jEdit.unsetProperty("plugin.TemplatesPlugin.templateDir.0");
		} else
		{
			jEdit.setProperty("plugin.TemplatesPlugin.templateDir.0", templateDirVal);
		}
		templates = new TemplateDir(null, new File(templateDirVal));
		TemplatesPlugin.refreshTemplates();
	}

	public static boolean getAcceleratorPassThruFlag()
	{
		return jEdit.getBooleanProperty(
				"plugin.TemplatesPlugin.accelPassThruFlag", false);
	}

	public static void setAcceleratorPassThruFlag(boolean accelPassThru)
	{
		if (false == accelPassThru)
		{
			jEdit.unsetProperty("plugin.TemplatesPlugin.accelPassThruFlag");
		} else
		{
			jEdit.setBooleanProperty("plugin.TemplatesPlugin.accelPassThruFlag", true);
		}
	}

	/**
	 * Returns the velocity engine.
	 *
	 * @return               The Velocity engine instance
	 * @exception Exception  Indicates that an exception occurred while
	 *      initializting the Velocity engine. Sadly, the Apache devs chose not to
	 *      throw a more specific exception.
	 */
	public static VelocityEngine getEngine()
			 throws Exception
	{
		if (engine == null)
		{
			engine = new VelocityEngine();
			Properties props = loadVelocityProperties();
			props.setProperty(RUNTIME_LOG, getVelocityLog());
			props.setProperty(FILE_RESOURCE_LOADER_PATH, TemplatesPlugin.getTemplateDir());
			engine.init(props);
		}
		return engine;
	}

	/**
	 * Returns the path to the Velocity configuration directory.
	 *
	 * @return   The Velocity configuration directory
	 */
	public static String getVelocityDirectory()
	{
		String velocityDir = jEdit.getProperty("plugin.TemplatesPlugin.velocityDir.0", "");
		if (velocityDir.equals(""))
		{
			velocityDir = defaultVelocityDir;
		}
		return velocityDir;
	}

	/**
	 * Change the directory where velocity resources are stored
	 *
	 * @param velocityDirVal  The new Velocity configuration directory
	 */
	public static void setVelocityDir(String velocityDirVal)
	{
		if (!velocityDirVal.endsWith(sepChar))
		{
			velocityDirVal = velocityDirVal + sepChar;
		}
		if (defaultVelocityDir.equals(velocityDirVal))
		{
			jEdit.unsetProperty("plugin.TemplatesPlugin.velocityDir.0");
		} else
		{
			jEdit.setProperty("plugin.TemplatesPlugin.velocityDir.0", velocityDirVal);
		}
	}

	/**
	 * Returns the file for velocity logging messages.
	 *
	 * @return   The absolute path for the Velocity log file
	 */
	private static String getVelocityLog()
	{
		return MiscUtilities.constructPath(getVelocityDirectory(),
				"velocity.log");
	}

	/**
	 * Return the path to the velocity properties file.
	 *
	 * @return   The absolute path for the Velocity properties file
	 */
	private static String getVelocityPropertiesPath()
	{
		return MiscUtilities.constructPath(getVelocityDirectory(),
				"velocity.properties");
	}

	/**
	 * Load velocity properties.
	 *
	 * @return   A <code>Properties</code> object
	 */
	private static Properties loadVelocityProperties()
	{
		Properties props = new Properties();
		InputStream in = null;
		TemplatesPlugin thePlugin = (TemplatesPlugin) jEdit.getPlugin(
				"templates.TemplatesPlugin");
		// Load the normal Velocity properties
		try
		{
			in = thePlugin.getClass().getClassLoader().
					getResourceAsStream("velocity/velocity.properties");
			props.load(in);
		}
		catch (IOException e)
		{
			Log.log(Log.ERROR, thePlugin, "Error loading normal velocity properties");
		}
		finally
		{
			IO.close(in);
		}
		// Load user's custom Velocity properties, if present
		try
		{
			File f = new File(getVelocityPropertiesPath());
			if (f.exists())
			{
				in = new FileInputStream(f);
				props.load(in);
			}
		}
		catch (IOException e)
		{
			Log.log(Log.ERROR, thePlugin, "Error loading custom velocity properties");
		}
		finally
		{
			IO.close(in);
		}
		return props;
	}

	//{{{ EditPlugin Methods
	/**
	 * Start this plugin.
	 */
	public void start()
	{
		sepChar = System.getProperty("file.separator");
		defaultTemplateDir = jEdit.getSettingsDirectory() + sepChar +
				"templates" + sepChar;
		defaultVelocityDir = jEdit.getSettingsDirectory() + sepChar +
				"velocity" + sepChar;
		File velocityDir = new File(getVelocityDirectory());
		if (velocityDir.isFile())
		{
			Log.log(Log.DEBUG, this, "'" + getVelocityDirectory() + "' is a file");
		}
		if (!velocityDir.exists() && !velocityDir.mkdirs())
		{
			Log.log(Log.DEBUG, this, "Cannot make directory '" + getVelocityDirectory() + "'");
		}

		File templatesDir = new File(TemplatesPlugin.getTemplateDir());
		if (!templatesDir.exists() && !templatesDir.mkdirs())
		{
			Log.log(Log.DEBUG, this, "Cannot make directory '" + TemplatesPlugin.getTemplateDir() + "'");
		}
		templates = new TemplateDir(null, templatesDir);
		templates.refreshTemplates();
	}

	/**
	 * Not used.
	 */
	public void stop()
	{
		AcceleratorManager.getInstance().save();
	}

	/**
	 * Scans the templates directory and sends an EditBus message to all
	 * TemplatesMenu objects to update themselves. Backup files are ignored based
	 * on the values of the backup prefix and suffix in the "Global Options"
	 * settings.
	 */
	public static void refreshTemplates()
	{
		String templateDirStr = getTemplateDir();
		File templateDir = new File(templateDirStr);
		try
		{
			if (!templateDir.exists())
			{// If the template directory doesn't exist
				Log.log(Log.DEBUG,
						jEdit.getPlugin(jEdit.getProperty("plugin.TemplatesPlugin.name")),
						"Attempting to create templates directory: " + templateDirStr);
				templateDir.mkdir();// then create it
				if (!templateDir.exists())
				{// If insufficent privileges to create it
					throw new java.lang.SecurityException();
				}
			}
			setTemplates(new TemplateDir(null, templateDir));
			getTemplates().refreshTemplates();
			buildAllMenus();
		}
		catch (java.lang.SecurityException se)
		{
			Log.log(Log.ERROR,
					jEdit.getPlugin(jEdit.getProperty("plugin.TemplatesPlugin.name")),
					jEdit.getProperty("plugin.TemplatesPlugin.error.create-dir") + templateDir
					);
		}
	}

	private static void buildAllMenus()
	{
		EditBus.send(new DynamicMenuChanged("plugin.templates.TemplatesPlugin.menu"));
	}

	/**
	 * Prompt the user for a template file and load it into the view from which the
	 * request was initiated.
	 *
	 * @param view  The view from which the "Edit Template" request was made.
	 */
	public static void editTemplate(View view)
	{
		JFileChooser chooser = new JFileChooser(getTemplateDir());
		int retVal = chooser.showOpenDialog(view);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if (file != null)
			{
				try
				{
					// Load file into jEdit
					jEdit.openFile(view, file.getCanonicalPath());
				}
				catch (IOException e)
				{
					// shouldn't happen
				}
			}
		}
	}

	/**
	 * Save the current buffer as a template. The file chooser displayed uses the
	 * Templates directory as the default.
	 *
	 * @param view  The view from which the "Save Template" request was made.
	 */
	public static void saveTemplate(View view)
	{
		JFileChooser chooser = new JFileChooser(getTemplateDir());
		int retVal = chooser.showSaveDialog(view);
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if (file != null)
			{
				try
				{
					// Save file
					view.getBuffer().save(view, file.getCanonicalPath());
				}
				catch (IOException e)
				{
					// shouldn't happen
				}
			}
		}
	}

	/**
	 * Process a specified template.
	 *
	 * @param template  The path of the template file to be processed
	 * @param textArea  The jEdit text area to receive template output
	 */
	public static void processTemplate(String template, JEditTextArea textArea)
	{
		processTemplate(template, GUIUtilities.getView(textArea), textArea);
	}

	/**
	 * Process a specified template.
	 *
	 * @param template  The path of the template file to be processed
	 * @param view      The view containing the text area for output
	 * @param textArea  The jEdit text area to receive template output
	 */
	public static void processTemplate(String template, View view, JEditTextArea textArea)
	{
		processTemplate(template, new VelocityContext(), view, textArea);
	}

	/**
	 * Process a specified template.
	 *
	 * @param template  The path of the template file to be processed
	 * @param ctx       The context used by the Velocity engine when processing the
	 *      template file
	 * @param view      The view containing the text area for output
	 * @param textArea  The jEdit text area to receive template output
	 */
	public static void processTemplate(String template, Context ctx,
			View view, JEditTextArea textArea)
	{
		try
		{
			if (!getEngine().templateExists(template))
			{
				GUIUtilities.error(view, "plugin.velocity.error.no-template-found",
						new String[]{template});
			}
			ctx = new jEditContext(view, textArea, ctx);
			((jEditContext) ctx).captureSelection();
			Writer out = new BufferWriter(((Buffer) textArea.getBuffer()),
					textArea.getCaretPosition());
			getEngine().mergeTemplate(template, ctx, out);
			if (ctx.get(CARET) != null)
			{
				Integer pos = (Integer) ctx.get(CARET);
				textArea.setCaretPosition(pos.intValue());
			}
		}
		catch (Exception e)
		{
			TemplatesPlugin thePlugin = (TemplatesPlugin) jEdit.getPlugin(
					"templates.TemplatesPlugin");
			Log.log(Log.ERROR, thePlugin, "Error processing template '" + template + "'");
			Log.log(Log.ERROR, thePlugin, e);
			JOptionPane.showMessageDialog(textArea, 
					"An error occurred while attempting to process a template." +
					"\nRefer to the Activity Log for more details.",
					"Template error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}

