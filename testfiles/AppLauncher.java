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
package projectviewer.config;

//{{{ Imports
import java.awt.Component;

import java.io.File;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.util.Set;
import java.util.TreeMap;
import java.util.Properties;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.io.VFSManager;

import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;

import common.gui.ModalJFileChooser;

import projectviewer.ProjectPlugin;
//}}}

/**
 *	Holds information on what applications to use to open certain types of
 *	files, based on user-configured file name globs.
 *
 *	@author		Matthew Payne
 *	@version	$Id: AppLauncher.java 13457 2008-08-30 18:57:52Z vanza $
 */
public class AppLauncher {

	private static AppLauncher instance;
	static {
		// make sure ProjectViewerConfig is loaded before the instance is
		// initialized.
		ProjectViewerConfig.getInstance();
	}

	//{{{ +_getInstance()_ : AppLauncher
	public static synchronized AppLauncher getInstance() {
		if (instance == null) {
			instance = new AppLauncher();
			try {
				instance.loadExts();
			} catch (IOException ioe) {
				Log.log(Log.ERROR, instance, ioe);
			}
		}
		return instance;
	} //}}}

	private TreeMap<ComparablePattern,String> appCol;

	//{{{ +AppLauncher() : <init>
	public AppLauncher() {
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
			ProjectPlugin.getResourceAsStream("fileassocs.properties");

		appCol.clear();

		if (inprops != null) {
			props.load(inprops);

			for (Object _key : props.keySet()) {
				String key = (String) _key;
				String value = props.getProperty(key);
				this.addAppExt(key, value);
			}

			inprops.close();
		}
	} //}}}

	//{{{ +storeExts() : void
	public void storeExts() throws IOException {
		PrintWriter out = new PrintWriter(
			new OutputStreamWriter(
				ProjectPlugin.getResourceAsOutputStream("fileassocs.properties")
			) );

		for (ComparablePattern key : appCol.keySet()) {
			String value = appCol.get(key);
			out.println(key.glob + "=" + value);
		}

		out.println("");
		out.close();
	} //}}}

	//{{{ +launchApp(String, Component) : void
	/**
	 *	Launches an external app depending on the extension of the path
	 *	provided, passing the path as an argument to the executable.
	 *
	 *	@since	PV 2.1.0
	 */
	public void launchApp(String path, Component comp) {
		String executable = getAppName(path);
		String ext = null; // if not null, we've been asked to pick an app.
		if (executable == null) {
			ext = "*." + getFileExtension(path);
			if (JOptionPane.showConfirmDialog(comp,
				jEdit.getProperty("projectviewer.launcher.no_app", new Object[] { ext }),
					jEdit.getProperty("projectviewer.launcher.no_app_title"),
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
				String msg = "projectviewer.launcher.io_error";
				if (ext != null) {
					msg = "projectviewer.launcher.io_error_cust";
				}
				JOptionPane.showMessageDialog(comp,
					jEdit.getProperty(msg, new Object[] { ioe.getMessage() }),
					jEdit.getProperty("projectviewer.error"),
					JOptionPane.ERROR_MESSAGE);
			}
		}
	} //}}}

	//{{{ +copy(AppLauncher) : void
	/** Copies the data from another AppLauncher into this one. */
	public void copy(AppLauncher other) {
		appCol.clear();
		for (ComparablePattern key : other.appCol.keySet()) {
			appCol.put(key, other.appCol.get(key));
		}
	} //}}}

	//{{{ +getAppName(String) : String
	/**
	 *	Returns the application name associated to the given file extension.
	 *	The file path can be any URL recognized by jEdit's VFSManager.
	 *
	 *	@since PV 2.1.0
	 */
	public String getAppName(String path) {
		String name = VFSManager.getVFSForPath(path).getFileName(path);
		for (ComparablePattern re : appCol.keySet()) {
			if (re.matches(name)) {
				return (String) appCol.get(re);
			}
		}

		return null;
	} //}}}

	//}}}

	//{{{ Private methods

	//{{{ -getFileExtension(String) : String
	/**
	 *	Returns the file's extension, or the file name if no extension can be
	 *	recognized.
	 *
	 *@param  filename
	 *@return	   The fileExtension value
	 */
	private String getFileExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex == -1 || dotIndex == fileName.length() - 1)
			return fileName;
		return fileName.substring(dotIndex + 1);
	} //}}}

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
	 *	Prompts the user for an application to run, and returns the path to the
	 *	executable file.
	 */
	private String pickApp(String ext, Component comp) {
		// Used for selected and executable file
		JFileChooser chooser = new ModalJFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		String exec;
		while (true) {
			if (chooser.showDialog(null, jEdit.getProperty("projectviewer.launcher.choose_app")) != JFileChooser.APPROVE_OPTION) {
				return null;
			}
			File fExec = chooser.getSelectedFile();
			if (!fExec.exists() || !fExec.canRead()) {
				JOptionPane.showMessageDialog(comp,
					jEdit.getProperty("projectviewer.launcher.invalid_app"),
					jEdit.getProperty("projectviewer.launcher.no_app_title"),
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
	 *	saving the AppLauncher information to the config file.
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

}

