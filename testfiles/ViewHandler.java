/*
 * ViewHandler.java
 * :tabSize=3:indentSize=3:noTabs=true:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Calvin Yu
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

package docker;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.DockableWindowManager.DockingArea;

/**
 * Handles auto-hiding when the EditPane gets focus. One of these is created for each View.
 * 
 */
public class ViewHandler implements FocusListener
{

	private View view;

	private Map<String, DockHandler> docks;

	private Set<EditPane> editPanes;

	private DockerConfig config;

	/**
	 * Create a new <code>ViewHandler</code>
	 */
	public ViewHandler(View aView)
	{
		view = aView;
		docks = new HashMap<String, DockHandler>(4);
		editPanes = new HashSet<EditPane>(2);
		config = DockerPlugin.getPlugin().getConfig();
		init();
	}

	/**
	 * Initialize this handler.
	 */
	public void init()
	{
		docks.clear();
		attachDock(DockableWindowManager.TOP);
		attachDock(DockableWindowManager.LEFT);
		attachDock(DockableWindowManager.BOTTOM);
		attachDock(DockableWindowManager.RIGHT);
		EditPane[] editPanes = view.getEditPanes();
		for (int i = 0; i < editPanes.length; i++)
		{
			editPaneCreated(editPanes[i]);
		}
	}

	/**
	 * Detach this handler from the view.
	 */
	public void detach()
	{
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			((DockHandler) i.next()).detach();
		}
		EditPane[] editPanes = view.getEditPanes();
		for (int i = 0; i < editPanes.length; i++)
		{
			editPaneDestroyed(editPanes[i]);
		}
	}

	/**
	 * Handle an edit pane creation message.
	 */
	public void editPaneCreated(EditPane editPane)
	{
		editPane.getTextArea().removeFocusListener(this);
		editPane.getTextArea().addFocusListener(this);
		editPanes.add(editPane);
	}

	/**
	 * Returns <code>true</code> if any dock is visible.
	 */
	public boolean isAnyDockVisible()
	{
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			if (((DockHandler) i.next()).isDockVisible())
			{
				return true;
			}
		}
		return false;
	}

	public void collapseAllDocks()
	{
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			i.next().collapse();
		}
	}

	public void saveDockState()
	{
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			i.next().saveDockState();
		}
	}

	public void restoreDockState()
	{
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			i.next().restoreDockState();
		}
	}

	/**
	 * Handle an edit pane destruction message.
	 */
	public void editPaneDestroyed(EditPane editPane)
	{
		editPane.getTextArea().removeFocusListener(this);
		editPanes.remove(editPane);
	}

	// {{{ FocusListener Methods
	/**
	 * Handle a focus gained event.
	 */
	public final void focusGained(FocusEvent evt)
	{
		// Log.log(Log.DEBUG, this, "Focus gained");
		for (Iterator<DockHandler> i = docks.values().iterator(); i.hasNext();)
		{
			i.next().autoHide();
		}
	}

	/**
	 * Handle a focus lost event.
	 */
	public final void focusLost(FocusEvent evt)
	{
		// Log.log(Log.DEBUG, this, "Focus gained");
	}

	// }}}

	/**
	 * Attach a handler to a dock.
	 */
	private void attachDock(String dockName)
	{
		DockableWindowManager wm = view.getDockableWindowManager();
		DockingArea container = null;
		if (DockableWindowManager.TOP.equals(dockName))
			container = wm.getTopDockingArea();
		else if (DockableWindowManager.LEFT.equals(dockName))
			container = wm.getLeftDockingArea();
		else if (DockableWindowManager.BOTTOM.equals(dockName))
			container = wm.getBottomDockingArea();
		else if (DockableWindowManager.RIGHT.equals(dockName))
			container = wm.getRightDockingArea();
		docks.put(dockName, new DockHandler(dockName, wm, container, config));
	}

}
