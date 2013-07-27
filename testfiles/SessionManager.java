/*
 * SessionManager.java
 * Copyright (c) 2001 Dirk Moebius, Sergey V. Udaltsov
 * Copyright (c) 2007, 2008 Steve Jakob
 *
 * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package sessions;


import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.jedit.msg.BufferUpdate;


/**
 * A singleton class that holds a current session and acts as a controller for handling 
 * session management tasks.
 *
 * <h1>Saving Sessions</code>
 * <p>There are two different types of situations that may result in a session being
 * saved, each of which is handled differently. These are:</p>
 * <ul>
 * <li><b>User-requested save</b>: occurs when the user manually requests that a session be 
 * saved, either from the session switcher component or the session manager dialog. In 
 * this cases there is no need to confirm the user's desire to save the session, but the 
 * user will be notified following a successful save. This type of save is handled by 
 * the #saveCurrentSession(View) method.</li>
 * <li><b>Autosave</b>: occurs when the user initiates an action that results in the 
 * current session being closed. Such actions include switching to a different session, 
 * renaming a session, or shutting down the plugin. In this cases, the user might be 
 * shown a dialog to confirm his/her desire to save (unless the users' preferences indicate
 * that no confirmation is desired), but no confirmation of save sucess is given. This type 
 * of save is handled by the #autosaveCurrentSession method.</li>
 * </ul>
 */
public class SessionManager implements EBComponent
{

	public final static String SESSION_PROPERTY = "sessions.currentSession";


	/** The current session instance. */
	private Session currentSession;


	/** The singleton SessionManager instance. */
	private static SessionManager instance;


	/** The string that will contain the new temp jEdit view.title property.*/	
	private String titleBarSessionName;


	/** The default jEdit view.title property. */
	private String defaultViewTitle;
	
	/** Returns the singleton SessionManager instance */
	public static SessionManager getInstance()
	{
		if(instance == null)
			instance = new SessionManager();
		return instance;
	}


	/**
	 * Initialization
	 */
	private SessionManager()
	{
		currentSession = new Session(jEdit.getProperty(SESSION_PROPERTY, "default"));

		// create directory <jedithome>/sessions if it not yet exists
		File dir = new File(getSessionsDir());
		if(!dir.exists())
			dir.mkdirs();

		// convert old format session files, if necessary
		new SessionFileConverter().run();

		// create default session file if it not yet exists
		File defaultSessionFile = new File(createSessionFileName("default"));
		if(!defaultSessionFile.exists())
			new Session("default").save(null);
		
		
		// initialize the variables for the jEdit title bar
		defaultViewTitle = jEdit.getProperty("view.title", 
								jEdit.getProperty("sessions.titlebar.default"));
		titleBarSessionName = new String();
	}


	/**
	 * Save the current session (subject to user preferences) and switch to a new session.
	 * This sends out a SessionChanged message on EditBus, if the
	 * session could be changed successfully.
	 *
	 * @param view  view for displaying error messages
	 * @param newSession  the new session name
	 * @return false, if the new session could not be set.
	 */
	public void setCurrentSession(final View view, final String newSessionName)
	{
		if(newSessionName.equals(currentSession.getName()))
			return;

		Log.log(Log.DEBUG, this, "setCurrentSession:"
			+ " currentSession=" + currentSession.getName()
			+ " newSessionName=" + newSessionName);

		File currentSessionFile = new File(currentSession.getFilename());
		if(currentSessionFile.exists())
		{
			// Auto-save the current session, subject to user preferences.
			if (!autosaveCurrentSession(view))
			{
				// User doesn't want to switch the session
				return;
			}
		}
		else
		{
			// The current session file has been deleted, probably by the SessionManagerDialog.
			// Do nothing, because save would recreate it.
		}

		// close all open buffers, if closeAll option is set:
		if (jEdit.getBooleanProperty("sessions.switcher.closeAll", true))
			if (!jEdit.closeAllBuffers(view))
				return;  // jEdit should have shown an error

		final String oldSessionName = currentSession.getName();
		EditBus.send(new SessionChanging(this, oldSessionName, newSessionName));

		// load new session
		// make sure this is not done from the AWT thread

		// {{{ This section changed by Steve Jakob
		//     Opening the new session in a separate thread would occasionally
		//     cause jEdit to freeze. I've left the code here in case we wish to try 
		//     this sort of thing in the future.
		/*
		new Thread()
		{
			public void run()
			{
				currentSession = new Session(newSessionName);
				saveCurrentSessionProperty();
				currentSession.open(view);
				EditBus.send(new SessionChanged(
					SessionManager.this, oldSessionName, newSessionName, currentSession));
			}
		}.start();
		*/
		
		currentSession = new Session(newSessionName);
		saveCurrentSessionProperty();
		currentSession.open(view);
		EditBus.send(new SessionChanged(
			SessionManager.this, oldSessionName, newSessionName, currentSession));
		// }}} End of section changed by Steve Jakob

		// Change FSB directory if appropriate
		changeFSBToBaseDirectory(view);
		
		// update the jEdit title bar with the session name
		setSessionNameInTitleBar();
	}


	/** Return the current session name. */
	public String getCurrentSession()
	{
		return currentSession.getName();
	}


	/** Return the current session. */
	public Session getCurrentSessionInstance()
	{
		return currentSession;
	}


	/**
	 * Save current session and show a dialog that it has been saved. This is the method 
	 * that is called for a "user-requested" save operation.
	 *
	 * @param view  view for displaying error messages
	 */
	public void saveCurrentSession(View view)
	{
		saveCurrentSession(view, false);
	}


	/**
	 * Save current session without showing the save confirmation dialog, 
	 * unless the "display confirmation dialog" flag has been set in the 
	 * plugin properties pane. This is the method that is called for "autosave" 
	 * functionality (ie. switching sessions, renaming a session, or shutting 
	 * down the plugin).
	 *
	 * @param view  view for displaying error messages
	 * @return <code>false</code> if the autosave process has been cancelled
	 */
	public boolean autosaveCurrentSession(View view)
	{
		if (currentSession.hasFileListChanged())
		{
			// If autosave sessions is on, save current session silently.
			if (jEdit.getBooleanProperty("sessions.switcher.autoSave", true))
			{
				// If the "askSave" property is set to "true" ...
				if (jEdit.getBooleanProperty("sessions.switcher.askSave", false))
				{
					// ... confirm whether the session should be saved
					boolean ok = new SaveDialog(view).isOK();
					if (!ok)
					{
						// User doesn't want to save the session
						return false;
					}
				}
				else
				{
					// Save the session.
					Log.log(Log.DEBUG, this, "autosaving current session...");
					saveCurrentSession(view, true);
				}
			}
		}
		return true;
	}


	/**
	 * Save current session. NOTE: developers should not call this method directly. Instead, 
	 * either the #saveCurrentSession(View) or #autosaveCurrentSession method should be 
	 * used, depending on the nature of the save operation.
	 *
	 * @param view  view for displaying error messages
	 * @param silently  if false, show a dialog that the current session has been saved.
	 */
	public void saveCurrentSession(View view, boolean silently)
	{
		currentSession.save(view);
		saveCurrentSessionProperty();
		if (!silently)
		{
			GUIUtilities.message(view, "sessions.switcher.save.saved", 
						new Object[] { currentSession });
		}
		Log.log(Log.DEBUG, this, "session saved: " + currentSession.getName());
	}


	/**
	 * Save current session under a different name and switch to it.
	 * This sends out SessionListChanged and SessionChanged messages
	 * on EditBus, if the session could be changed successfully, in this
	 * order.
	 *
	 * @param view  view for displaying error messages
	 */
	public void saveCurrentSessionAs(View view)
	{
		String newName = inputSessionName(view, currentSession.getName());

		if (newName == null)
			return;

		File file = new File(createSessionFileName(newName));
		if (file.exists())
		{
			int answer = GUIUtilities.confirm(view,
				"sessions.switcher.saveAs.exists",
				new Object[] { newName },
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);
			if (answer != JOptionPane.YES_OPTION)
				return;
		}

		// create new session:
		Session newSession = currentSession.getClone();
		newSession.setName(newName);
		newSession.save(view);
		EditBus.send(new SessionChanging(this, currentSession.getName(), newName));

		// set new session:
		String oldSessionName = currentSession.getName();
		currentSession = newSession;
		saveCurrentSessionProperty();
		EditBus.send(new SessionListChanged(this));
		EditBus.send(new SessionChanged(this, oldSessionName, newName, currentSession));
		// update the jEdit title bar with the session name
		setSessionNameInTitleBar();
	}


	/**
	 * Reload current session.
	 *
	 * @param view  view for displaying error messages
	 */
	public void reloadCurrentSession(final View view)
	{
		Log.log(Log.DEBUG, this, "reloadCurrentSession: currentSession=" + currentSession);

		// close all open buffers
		if(!jEdit.closeAllBuffers(view))
			return; // user cancelled

		// FIXME: do we need to make sure this is not the AWT thread?!?
		currentSession.open(view); // ignore any errors and return value
	}


	/**
	 * Change FSB directory if relevant option selected, "basedir" property
	 * set, and FSB is visible.
	 *
	 * @param view look for FSB in this View
	 */
	public void changeFSBToBaseDirectory(View view)
	{
		if ((jEdit.getBooleanProperty("sessions.switcher.changeFSBDirectory", false)) &&
			!("".equals(currentSession.getProperty(Session.BASE_DIRECTORY))) &&
			(view.getDockableWindowManager().isDockableWindowVisible(VFSBrowser.NAME)))
		{
			VFSBrowser.browseDirectory(view,
				currentSession.getProperty(Session.BASE_DIRECTORY));
		}
	}


	/**
	 * Show the Session Manager dialog.
	 * If the user changes the current session or modifies the
	 * list of sessions, SessionChanged and SessionListChanged
	 * messages are sent on EditBus.
	 *
	 * @param  view  center dialog on this View.
	 */
	public void showSessionManagerDialog(View view)
	{
		SessionManagerDialog dlg = new SessionManagerDialog(view, currentSession.getName());
		String newSession = dlg.getSelectedSession();

		if(dlg.isListModified())
		{
			EditBus.send(new SessionListChanged(this));
			if(newSession == null)
			{
				// Session list has been modified, but dialog has been cancelled.
				// Send out session changed events, just in case...
				String name = currentSession.getName();
				EditBus.send(new SessionChanging(this, name, name));
				EditBus.send(new SessionChanged(this, name, name, currentSession));
			}
		}

		if(newSession != null)
			setCurrentSession(view, newSession);
	}


	public void showSessionPropertiesDialog(View view)
	{
		SessionPropertiesShowing message = new SessionPropertiesShowing(this, currentSession);
		EditBus.send(message);
		new SessionPropertiesDialog(view, currentSession.getName(), message.getRootGroup());
	}


	public static String[] getSessionNames()
	{
		String[] files = new File(getSessionsDir()).list(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				return name.toLowerCase().endsWith(".xml");
			}
		});

		Arrays.sort(files, new StandardUtilities.StringCompare(false));

		Vector v = new Vector();
		boolean foundDefault = false;
		for (int i=0; i < files.length; i++)
		{
			String name = files[i].substring(0, files[i].length() - 4); // cut off ".xml"
			if (name.equalsIgnoreCase("default"))
			{
				// default session always first
				v.insertElementAt(name, 0);
				foundDefault = true;
			}
			else
				v.addElement(name);
		}

		if (!foundDefault)
			v.insertElementAt("default", 0);

		String[] result = new String[v.size()];
		v.copyInto(result);
		return result;
	}


	/** EBComponent implementation; does nothing */
	public void handleMessage(EBMessage msg) {}
	


	/**
	 * Converts a session name (eg, "default") to a full path name
	 * (eg, "/home/slava/.jedit/sessions/default.xml").
	 */
	public static String createSessionFileName(String session)
	{
		String filename = MiscUtilities.constructPath(getSessionsDir(), session);
		if (!filename.toLowerCase().endsWith(".xml"))
			filename = filename + ".xml";
		return filename;
	}


	/**
	 * Return the directory where the session files are stored,
	 * usually $HOME/.jedit/sessions.
	 */
	public static String getSessionsDir()
	{
		return MiscUtilities.constructPath(jEdit.getSettingsDirectory(), "sessions");
	}


	/**
	 * Shows an input dialog asking for a session name as long as a valid
	 * session name is entered or the dialog is cancelled.
	 * A session name is valid if it doesn't contains the following characters:
	 * File.separatorChar, File.pathSeparatorChar and ':'.
	 *
	 * @param relativeTo  the component where the dialog is centered on.
	 * @param defaultName  a default session name to display in the input dialog; may be null.
	 * @return the new session name, or null if the dialog was cancelled.
	 */
	public static String inputSessionName(Component relativeTo, String defaultName)
	{
		String name = defaultName;

		do
		{
			name = GUIUtilities.input(relativeTo, "sessions.switcher.saveAs.input", name);
			if (name != null)
			{
				name = name.trim();
				if (name.length() == 0)
					GUIUtilities.error(relativeTo, "sessions.switcher.saveAs.error.empty", null);
				if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0
					|| name.indexOf(';') >= 0 || name.indexOf(':') >= 0)
				{
					GUIUtilities.error(relativeTo, "sessions.switcher.saveAs.error.illegalChars", new Object[] { "/  \\  ;  :" });
					name = "";
				}
			}
		} while (name != null && name.length() == 0);

		return name;
	}


	/**
	 * Show an error message dialog (using GUIUtilities.error())
	 * after the GUI has been updated, in the AWT thread.
	 */
	public static final void showErrorLater(final View view, final String messageProperty, final Object[] args)
	{
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				GUIUtilities.error(view, messageProperty, args);
			}
		});
	}

	// {{{ jEdit title bar controls
	// added by Paul Russell 2004-09-26
	/**
	 * Record the name of the current session in a jEdit property (SESSION_PROPERTY). This 
	 * property is used to restore the last used session the next time the plugin is started.
	 */
	void saveCurrentSessionProperty()
	{
		Log.log(Log.DEBUG, this, "saveCurrentSessionProperty: currentSession=" + currentSession.getName());
		jEdit.setProperty(SESSION_PROPERTY, currentSession.getName());
		jEdit.saveSettings();
	}
	
	/**
	 * Put the session name in the jEdit title bar
	 */
	public void setSessionNameInTitleBar()
	{
		if ( currentSession != null )
		{
			if ( jEdit.getBooleanProperty("sessions.switcher.showSessionNameInTitleBar", true) )
			{
				if (jEdit.getBooleanProperty("sessions.switcher.showSessionPrefixInTitleBar", true))
				{
					titleBarSessionName = defaultViewTitle + 
						jEdit.getProperty("sessions.titlebar.startbracket") +
						jEdit.getProperty("sessions.titlebar.prefix") +
						currentSession.getName() +
						jEdit.getProperty("sessions.titlebar.endbracket");	
				}
				else
				{
					titleBarSessionName = defaultViewTitle + 
						jEdit.getProperty("sessions.titlebar.startbracket") +
						currentSession.getName() +
						jEdit.getProperty("sessions.titlebar.endbracket");
				}
						
				jEdit.setTemporaryProperty("view.title", titleBarSessionName);
				refreshTitleBar();
			}
		}
	}
	
	/**
	 * Restore the original jEdit title bar text
	 */
	public void restoreTitleBarText()
	{
		jEdit.setTemporaryProperty("view.title", defaultViewTitle);	
	}

	/**
	 * refreshes the jEdit Title Bar. This only needs to be called
	 * when the plugin stops or restarts.
	 */
	public void refreshTitleBar()
	{
		View[] views = jEdit.getViews();
		
		for( int i = 0; i < views.length; i++ )
		{
			views[i].updateTitle();
		}	
	}
	
	// }}}
}
