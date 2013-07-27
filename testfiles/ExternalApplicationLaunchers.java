/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package launcher.extapp;

//{{{ Imports
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import launcher.Launcher;
import launcher.LauncherPlugin;
import launcher.LauncherUtils;
import launcher.exec.ExecutableFileLauncherType;

import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.PluginJAR;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;

import common.gui.ModalJFileChooser;

/**
 *	Holds information on what applications to use to open certain types of
 *	files, based on user-configured file name globs.
 *
 *	@author		Matthew Payne, Francois Rey
 */
public class ExternalApplicationLaunchers {
	
	public static final String ASSOCIATIONS_FILENAME = "fileassocs.properties";
	private static final String DIALOG_SUFFIX = ".dialog";
	private static final String TITLE_SUFFIX = ".title";
	private static final String MESSAGE_SUFFIX = ".message";
	private static final String APPROVE_BUTTON_TEXT_SUFFIX = ".approve-button-text";
	private static final String DIALOG_NO_APP =
		ExecutableFileLauncherType.INSTANCE.getPropertyPrefix() +
			DIALOG_SUFFIX + ".no-app";
	private static final String DIALOG_CHOOSE_APP =
		ExecutableFileLauncherType.INSTANCE.getPropertyPrefix() +
			DIALOG_SUFFIX + ".choose-app";
	private static final String DIALOG_INVALID_APP =
		ExecutableFileLauncherType.INSTANCE.getPropertyPrefix() +
			DIALOG_SUFFIX + ".invalid-app";

	private static ExternalApplicationLaunchers instance;

	//{{{ +_getInstance()_ : ExternalApplicationLaunchers
	public static synchronized ExternalApplicationLaunchers getInstance() {
		if (instance == null) {
			instance = new ExternalApplicationLaunchers();
			try {
				instance.loadExts();
			} catch (IOException ioe) {
				Log.log(Log.ERROR, instance, ioe);
			}
		}
		return instance;
	} //}}}

	private TreeMap<ComparablePattern,String> appCol;

	//{{{ +ExternalApplicationLaunchers() : <init>
	public ExternalApplicationLaunchers() {
		appCol = new TreeMap<ComparablePattern,String>();
	} //}}}


	//}}}

	//{{{ Public methods

	//{{{ +getAppList() : Set
	public Set getAppList() {
	   //return all the values
		return appCol.entrySet();
	} //}}}

	//{{{ +addAppExt(String, String) : void
	public void addAppExt(String fileExt, String execPath) {
		if (fileExt.trim().length() > 0) {
			try {
				ComparablePattern re = new ComparablePattern(fileExt);
				appCol.put(re, execPath);
			} catch (PatternSyntaxException re) {
				Log.log(Log.ERROR, this, re);
			}
		}
	} //}}}

	//{{{ +removeAppExt(String) : void
	public void removeAppExt(Object fileExt) {
		appCol.remove(fileExt);
	} //}}}

	//{{{ +getCount() : int
	public int getCount() {
	   return appCol.size();
	} //}}}

	//{{{ +loadExts() : void
	/** load extension properties from file **/
	public void loadExts() throws IOException {

 		Properties props = new Properties();
		InputStream inprops =
			EditPlugin.getResourceAsStream(
					LauncherPlugin.class, ASSOCIATIONS_FILENAME);

		appCol.clear();

		if (inprops != null) {
			props.load(inprops);

			for (Object _key : props.keySet()) {
				String glob = (String) _key;
				String execPath = props.getProperty(glob);
				this.addAppExt(glob, execPath);
			}

			inprops.close();
		}
		
		registerLaunchers();
	} //}}}

	//{{{ +storeExts() : void
	public void storeExts() throws IOException {
		PrintWriter out = new PrintWriter(
			new OutputStreamWriter(
				EditPlugin.getResourceAsOutputStream(
						LauncherPlugin.class, ASSOCIATIONS_FILENAME)
			) );

		for (ComparablePattern key : appCol.keySet()) {
			String value = appCol.get(key);
			out.println(key.glob + "=" + value);
		}

		out.println("");
		out.close();
		
		registerLaunchers();
		
	} //}}}

	//{{{ +launchApp(String, Component) : void
	/**
	 *	Launches an external app depending on the extension of the applicationPath
	 *	provided, passing the applicationPath as an argument to the executable.
	 */
	public void launchApp(String path, Component comp) {
		// TODO: method no longer needed, dispatch and delete.
		String executable = getAppName(path);
		String ext = null; // if not null, we've been asked to pick an app.
		if (executable == null) {
			ext = "*." + LauncherUtils.getFileExtension(path);
			if (JOptionPane.showConfirmDialog(comp,
				jEdit.getProperty(DIALOG_NO_APP + MESSAGE_SUFFIX, new Object[] { ext }),
					jEdit.getProperty(DIALOG_NO_APP + TITLE_SUFFIX),
					javax.swing.JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				 executable = pickApp(ext, comp);
			 } else {
				 return;
			 }
		}
		if (executable != null) {
			Runtime rt = Runtime.getRuntime();
			String[] callAndArgs = { executable, path };
			try {
				rt.exec(callAndArgs);

			   	if (ext != null) {
					// running the app worked, so save it as the default
					// app for the extension.
					this.addAppExt(ext, executable);

					try {
						this.storeExts();
					} catch (Exception e) {
						Log.log(Log.ERROR, this, e);
					}
				}
			} catch(java.io.IOException ioe) {
				String res = ext == null? path : ext + " " + path;
				Launcher.logFailedLaunch(this, res, ioe);
			}
		}
	} //}}}

	//{{{ +copy(ExternalApplicationLaunchers) : void
	/** Copies the data from another ExternalApplicationLaunchers into this one. */
	public void copy(ExternalApplicationLaunchers other) {
		appCol.clear();
		for (ComparablePattern key : other.appCol.keySet()) {
			appCol.put(key, other.appCol.get(key));
		}
	} //}}}

	//{{{ +getAppName(String) : String
	/**
	 *	Returns the applicationPath name associated to the given file extension.
	 *	The file applicationPath can be any URL recognized by jEdit's VFSManager.
	 */
	public String getAppName(String path) {
		// TODO: delete this method
		String name = VFSManager.getVFSForPath(path).getFileName(path);
		for (ComparablePattern re : appCol.keySet()) {
			if (re.matches(name)) {
				return (String) appCol.get(re);
			}
		}

		return null;
	} //}}}

	//{{{ +getExternalApplicationLauncher(String) : String
	/**
	 *	Returns the GlobPatternExternalApplicationLauncher corresponding to the applicationPath name
	 *  associated to the given file applicationPath.
	 *	The file applicationPath can be any applicationPath recognized by jEdit's VFSManager.
	 */
	public GlobPatternExternalApplicationLauncher getExternalApplicationLauncher(String path) {
		GlobPatternExternalApplicationLauncher launcher = null;
		String executable = getAppName(path);
		if (executable != null) {
			launcher = (GlobPatternExternalApplicationLauncher)ServiceManager.getService(
					ExecutableFileLauncherType.SERVICE_NAME,
					executable);
		}
		return launcher;
	} //}}}

	//{{{ +getExternalApplicationLauncher(String) : String
	/**
	 *	Returns the GlobPatternExternalApplicationLauncher corresponding to the applicationPath name
	 *  associated to the given file extension.
	 *	The file applicationPath can be any URL recognized by jEdit's VFSManager.
	 */
	public GlobPatternExternalApplicationLauncher getExternalApplicationLauncher(File file) {
		return getExternalApplicationLauncher(file.getPath());
	} //}}}

	//}}}

	//{{{ Private methods


	//{{{ -_replaceString(String, String, String)_ : String
	private static String replaceString(String aSearch, String aFind, String aReplace)
	{ /* MP could not get regex replace to work.
		so I am including this function for now */
		String result = aSearch;
		if (result != null && result.length() > 0) {
			int a = 0;
			int b = 0;
			while (true) {
				a = result.indexOf(aFind, b);
				if (a != -1) {
					result = result.substring(0, a) + aReplace + result.substring(a + aFind.length());
					b = a + aReplace.length();
				}
				else
				break;
			}
		}
		return result;
	} //}}}

	//{{{ -pickApp(String, Component) : String
	/**
	 *	Prompts the user for an applicationPath to run, and returns the applicationPath to the
	 *	executable file.
	 */
	String pickApp(String ext, Component comp) {
		// Used for selected and executable file
		JFileChooser chooser = new ModalJFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		String exec;
		while (true) {
			if (chooser.showDialog(null,
					jEdit.getProperty(DIALOG_CHOOSE_APP + 
							APPROVE_BUTTON_TEXT_SUFFIX)) != 
								JFileChooser.APPROVE_OPTION) {
				return null;
			}
			File fExec = chooser.getSelectedFile();
			if (!fExec.exists() || !fExec.canRead()) {
				JOptionPane.showMessageDialog(comp,
					jEdit.getProperty(DIALOG_INVALID_APP + MESSAGE_SUFFIX),
					jEdit.getProperty(DIALOG_INVALID_APP + TITLE_SUFFIX),
					JOptionPane.ERROR_MESSAGE);
				continue;
			}
			exec = replaceString(fExec.getAbsolutePath(), "\\", "/");
			break;
		}

		return exec;
	} //}}}

	//}}}

	//{{{ -class _ComparablePattern_
	/**
	 *	A pattern that is comparable by comparing the glob string used to
	 *	create it. Stores the glob internally for ordering and for
	 *	saving the ExternalApplicationLaunchers information to the config file.
	 */
	private static class ComparablePattern implements Comparable {

		private Pattern	pattern;
		private String 	glob;

		//{{{ +ComparableRE(String) : <init>
		public ComparablePattern(String glob) {
			this.pattern = Pattern.compile(StandardUtilities.globToRE(glob));
			this.glob = glob;
		} //}}}

		public boolean matches(String test) {
			return pattern.matcher(test).matches();
		}

		//{{{ +compareTo(Object) : int
		public int compareTo(Object o) {
			try {
				return this.glob.compareTo(((ComparablePattern)o).glob);
			} catch (ClassCastException cce) {
				return -1;
			}
		} //}}}

		//{{{ +toString() : String
		public String toString() {
			return glob;
		} //}}}

	} //}}}

	//{{{ registerServices() method
	/**
	 * Registers all external applicationPath launchers that are loaded.
	 */
	public void registerLaunchers()
	{
		ExternalApplicationLauncherType.INSTANCE.unregisterUserDefinedLaunchers();
		PluginJAR pluginJAR = jEdit.getPlugin(LauncherPlugin.class.getName()).getPluginJAR();
		for (Object _key : appCol.keySet()) {
			String execPath = appCol.get(_key);
			String glob = ((ComparablePattern)_key).glob;
			String launcherName = GlobPatternExternalApplicationLauncher.computeName(
					GlobPatternExternalApplicationLauncher.PROP_PREFIX, new Object[]{execPath, glob});
			String initCode =
				"new " + GlobPatternExternalApplicationLauncher.class.getName() + "(" +
					"new Object[]{\"" + execPath + "\", \"" + glob + "\"}, false, true);"; 
			Log.log(Log.DEBUG, this, "Registering " + launcherName + " with code " + initCode);
			ServiceManager.registerService(
					ExternalApplicationLauncherType.SERVICE_NAME,
					launcherName,
					initCode,
					pluginJAR);
		}
	} //}}}

}

