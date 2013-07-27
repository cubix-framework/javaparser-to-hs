/*
 * SessionsPlugin.java
 * Copyright (c) 2000,2001 Dirk Moebius
 * Copyright (C) 2007 Steve Jakob
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


import java.awt.BorderLayout;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;


/**
 * The Sessions plugin.
 *
 * @author Dirk Moebius
 */
public class SessionsPlugin extends EBPlugin
{
	private Hashtable viewSessionSwitchers = new Hashtable();
	
	private boolean switcherInBufferList = false;
	
	public void start()
	{
		boolean restore = jEdit.getBooleanProperty("restore");
		boolean restore_cli =  jEdit.getBooleanProperty("restore.cli");
		boolean autosave = jEdit.getBooleanProperty("sessions.switcher.autoSave", true);

		if ((!restore || !restore_cli) && autosave)
		{
			// Show a warning if either "Restore previously open files on startup"
			// or "Restore even if file names were specified on the command line"
			// is off and Session Autosave is on:
			showInfoMessage("sessions.manager.info.restore_autosave");
		}
		else if (!restore)
		{
			// "Restore previously open files on startup" is off.
			// The last open session won't be restored.
			// Show an information dialog.
			showInfoMessage("sessions.manager.info.restore");
		}
		
		SessionManager mgr = SessionManager.getInstance();
		
		// Though we don't need to load the current session's files, we
		//  still need to load the custom properties into memory.
		mgr.getCurrentSessionInstance().open(jEdit.getActiveView(), false);
			
		// Put the session name in the jEdit title bar
		mgr.setSessionNameInTitleBar();
		mgr.refreshTitleBar();
		
		if (jEdit.getBooleanProperty("sessions.switcher.showToolBar", false))
		{
			// If the switcher is to be shown in the BufferList dockable ...
			if(jEdit.getBooleanProperty(
					"sessions.switcher.showInsideBufferList", false))
			{
				// ... defer switcher creation until after JEdit is completely 
				// started, so we can be sure that the BufferList dockable has
				// been created.
				Log.log(Log.DEBUG, this, "Defer session switcher creation");
				switcherInBufferList = true;
			}
			else
			{
				// Add SessionSwitcher to existing Views
				Log.log(Log.DEBUG, this, "Add SessionSwitcher to existing Views");
				View[] views = jEdit.getViews();
				for (int i = 0; i < views.length; i++)
				{
					addSessionSwitcher(views[i]);
				}
			}
		}
	}


	public void stop()
	{
		
		
		// Remove SessionSwitcher from existing Views
		View[] views = jEdit.getViews();
		for (int i = 0; i < views.length; i++)
		{
			removeSessionSwitcher(views[i]);
		}
		
		// update the title bar
		SessionManager mgr = SessionManager.getInstance();
		mgr.restoreTitleBarText();
		mgr.refreshTitleBar();
	}


	public void handleMessage(EBMessage message)
	{
		if (message instanceof ViewUpdate)
		{
			ViewUpdate vu = (ViewUpdate) message;
			if (vu.getWhat() == ViewUpdate.CREATED)
			{
				if(jEdit.getBooleanProperty("sessions.switcher.showToolBar", false))
					addSessionSwitcher(vu.getView());
			}
			else if (vu.getWhat() == ViewUpdate.CLOSED)
			{
				viewSessionSwitchers.remove(vu.getView());
			}
		}
		else if (message instanceof DockableWindowUpdate)
		{
			handleDockableWindowUpdate((DockableWindowUpdate)message);
		}
		else if (message instanceof EditorExitRequested)
		{
			handleEditorExit((EditorExitRequested)message);
		}
		else if (message instanceof PropertiesChanged)
		{
			handlePropertiesChanged();
		}
		else if (message instanceof SessionPropertiesShowing)
		{
			SessionPropertiesShowing smsg = (SessionPropertiesShowing) message;
			smsg.addPropertyPane(new DefaultSessionPropertyPane(smsg.getSession()));
		}
	}


	public static boolean isBufferListAvailable(View view)
	{
		// check if BufferList class is there
		Object bufferlist = jEdit.getPlugin("bufferlist.BufferListPlugin");
		if(bufferlist == null)
			return false;

		// check version, 0.8 is required
		String version = jEdit.getProperty("plugin.bufferlist.BufferListPlugin.version");
		if(version == null || version.length() == 0 || StandardUtilities.compareStrings(version, "0.8", true) < 0)
			return false;

		// check if docked
		if(getBufferList(view) == null)
			return false;

		return true;
	}


	private static JComponent getBufferList(View view)
	{
		DockableWindowManager mgr = view.getDockableWindowManager();
		return mgr.getDockable("bufferlist");
	}


	private void handleDockableWindowUpdate(DockableWindowUpdate eemsg)
	{
		String msgSrc = eemsg.getDockable();
		String what = (String)eemsg.getWhat();
		// We respond only to activation of the BufferList dockable
		if ("bufferlist".equals(msgSrc) && "ACTIVATED".equals(what))
		{
			// If the switcher is to be displayed in the BufferList dockable ...
			if (switcherInBufferList)
			{
				Log.log(Log.DEBUG, this, "Add session switcher to BufferList dockable");
				// ... then add switcher to all views
				View[] views = jEdit.getViews();
				for (int i = 0; i < views.length; i++)
				{
					addSessionSwitcher(views[i]);
				}
			}
		}
	}


	private void handleEditorExit(EditorExitRequested eemsg)
	{
		// call SessionManager to save the current session
		SessionManager mgr = SessionManager.getInstance();
		if (!mgr.autosaveCurrentSession(eemsg.getView()))
		{
			// User doesn't want to close jEdit
			eemsg.cancelExit();
		}
	}


	private void handlePropertiesChanged()
	{
		boolean show = jEdit.getBooleanProperty("sessions.switcher.showToolBar", false);
		View view = jEdit.getFirstView();

		while (view != null)
		{
			if (show)
				addSessionSwitcher(view);
			else
				removeSessionSwitcher(view);
			view = view.getNext();
		}
		
		SessionManager mgr = SessionManager.getInstance();
		if (jEdit.getBooleanProperty("sessions.switcher.showSessionNameInTitleBar", true) )
		{
			mgr.setSessionNameInTitleBar();
		}
		else
		{
			mgr.restoreTitleBarText();
		}
		
		mgr.refreshTitleBar();
		
		
	}


	private synchronized void addSessionSwitcher(final View view)
	{
		// remove old
		removeSessionSwitcher(view);

		// create new
		final SessionSwitcher switcher = new SessionSwitcher(view, true);
		viewSessionSwitchers.put(view, switcher);

		if(jEdit.getBooleanProperty("sessions.switcher.showJEditToolBar", false))
		{
			if(view.getToolBar() == null)
			{
				Log.log(Log.WARNING, this, "View toolbar is null!!!");
			}
			else
			{
				Log.log(Log.DEBUG, this, "Adding session switcher to main toolbar");
				// Add to jEdit's default toolbar:
				// We need to add it later. Cannot add it right now, because if the View
				// receives the PropertiesChanged message, it removes and recreates
				// the toolbar. If the View receives the message after _we_ received it,
				// then our switcher would be gone.
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						view.getToolBar().add(switcher);
						view.getRootPane().revalidate();
					}
				});
			}
		}
		else if(jEdit.getBooleanProperty("sessions.switcher.showInsideBufferList", false))
		{
			// Add the session switcher to BufferList's dockable.
			final JComponent bufferlist = getBufferList(view);
			// It's possible that this method was called prior to creation of
			// the BufferList dockable component, so we need to check to be 
			// sure that it exists.
			if(bufferlist == null)
			{
				Log.log(Log.WARNING, this, "BufferList is null!!!");
			}
			else
			{
				Log.log(Log.DEBUG, this, "Adding session switcher to BufferList dockable");
				// Add to BufferList:
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						bufferlist.add(BorderLayout.NORTH, switcher);
						bufferlist.revalidate();
					}
				});
			}
		}
		else
		{
			// Add as a _new_ toolbar to the View, below the main toolbar:
			Log.log(Log.DEBUG, this, "Adding session switcher below main toolbar");
			view.addToolBar(switcher);
		}
	}


	private synchronized void removeSessionSwitcher(View view)
	{
		SessionSwitcher switcher = (SessionSwitcher) viewSessionSwitchers.get(view);
		if (switcher != null)
		{
			// Try to remove toolbar
			// (this does nothing if there is no switcher)
			view.removeToolBar(switcher);
			viewSessionSwitchers.remove(view);

			// Try to remove from jEdit's default toolbar
			// (this does nothing if there is no switcher)
			if(view.getToolBar() != null)
			{
				view.getToolBar().remove(switcher);
				view.getRootPane().revalidate();
			}

			// Try to remove from BufferList
			JComponent bufferlist = getBufferList(view);
			if(bufferlist != null)
				bufferlist.remove(switcher);
		}
	}


	public static void showInfoMessage(String key)
	{
		if (!jEdit.getBooleanProperty(key + ".notAgain"))
		{
			String title = jEdit.getProperty(key + ".title");
			String msg = jEdit.getProperty(key + ".message");
			String msg2 = jEdit.getProperty("sessions.manager.info.dontShowAgain");
			JCheckBox notAgain = new JCheckBox(msg2, false);
						GUIUtilities.hideSplashScreen();
			JOptionPane.showMessageDialog(null, new Object[] { msg, notAgain },
				title, JOptionPane.INFORMATION_MESSAGE);
			jEdit.setBooleanProperty(key + ".notAgain", notAgain.isSelected());
		}
	}

}
