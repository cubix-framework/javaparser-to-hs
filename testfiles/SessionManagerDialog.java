/*
 * SessionManagerDialog.java - a dialog for managing sessions
 * Copyright (c) 2001 Dirk Moebius
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
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;


/**
 * A modal dialog for managing jEdit sessions and session files.
 *
 * @author Dirk Moebius
 */
class SessionManagerDialog
		extends EnhancedDialog
		implements ActionListener, ListSelectionListener
{

	public SessionManagerDialog(View view, final String currentSession)
	{
		super(view, jEdit.getProperty("sessions.manager.title"), true);
		this.currentSession = currentSession;

		lSessions = new JList(SessionManager.getInstance().getSessionNames());
		lSessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lSessions.addListSelectionListener(this);
		lSessions.addMouseListener(new MouseHandler()); // for double-clicks
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				lSessions.setSelectedValue(currentSession, true);
			}
		});
		// try to show as many sessions as possible (maximum 25)
		int numSessions = lSessions.getModel().getSize();
		if (numSessions > 25)
			numSessions = 25;
		lSessions.setVisibleRowCount(numSessions);

		JScrollPane scrSessions = new JScrollPane(lSessions);

		bRename = new JButton(jEdit.getProperty("sessions.manager.rename"));
		bRename.addActionListener(this);

		bDelete = new JButton(jEdit.getProperty("sessions.manager.delete"));
		bDelete.addActionListener(this);

		bChangeTo = new JButton(jEdit.getProperty("sessions.manager.changeTo"));
		bChangeTo.setDefaultCapable(true);
		bChangeTo.addActionListener(this);

		bClose = new JButton(jEdit.getProperty("sessions.manager.close"));
		bClose.addActionListener(this);

		Insets inset10 = new Insets(10, 10, 10, 10);
		Insets insetButton = new Insets(10, 0, 0, 10);
		Insets insetLast = new Insets(10, 0, 10, 10);

		getContentPane().setLayout(new GridBagLayout());
		addComponent(scrSessions, 1, 3, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, inset10);
		addComponent(bRename, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insetButton);
		addComponent(bDelete, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insetButton);
		addComponent(bChangeTo, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insetButton);
		addComponent(new JSeparator(), GridBagConstraints.REMAINDER, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, inset10);
		addComponent(new JPanel(), 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, inset10);
		addComponent(bClose, GridBagConstraints.REMAINDER, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, insetLast);
		getRootPane().setDefaultButton(bChangeTo);

		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}


	/**
	 * Invoked if ENTER or the Ok button are pressed.
	 * Switches the session and closes the dialog.
	 * You shouldn't show it again afterwards, because it is disposed.
	 */
	public void ok()
	{
		if (lSessions.getSelectedValue() != null)
			selectedSession = lSessions.getSelectedValue().toString();
		setVisible(false);
		dispose();
	}


	/**
	 * Invoked if ESC or the Cancel button are pressed.
	 * Closes the dialog.
	 * You shouldn't show it again afterwards, because it is disposed.
	 */
	public void cancel()
	{
		setVisible(false);
		dispose();
	}


	/**
	 * Return the selected session, or null if the dialog has been cancelled.
	 */
	public String getSelectedSession()
	{
		return selectedSession;
	}


	/**
	 * Return true if the list of sessions has been modified.
	 * The list has been modified if at least one session has been renamed
	 * or deleted.
	 */
	public boolean isListModified()
	{
		return listModified;
	}


	/**
	 * Invoked if one of the buttons is pressed.
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() == bClose)
			cancel();
		else if (evt.getSource() == bChangeTo)
			ok();
		else if (evt.getSource() == bRename)
			rename();
		else if (evt.getSource() == bDelete)
			delete();
	}


	/**
	 * Invoked when the selected item in the sessions list changes.
	 */
	public void valueChanged(ListSelectionEvent evt)
	{
		Object[] values = lSessions.getSelectedValues();
		if (values == null || values.length == 0)
		{
			// no selection
			bRename.setEnabled(false);
			bDelete.setEnabled(false);
			bChangeTo.setEnabled(false);
		}
		else
		{
			boolean isCurrentSession = values[0].toString().equals(currentSession);
			boolean isDefaultSession = values[0].toString().equalsIgnoreCase("default");
			bChangeTo.setEnabled(!isCurrentSession);
			bRename.setEnabled(!isDefaultSession);
			bDelete.setEnabled(!isDefaultSession);
		}
	}


	private void rename()
	{
		// TODO: Move this functionality to SessionManager
		
		String oldName = lSessions.getSelectedValue().toString();
		String newName = SessionManager.inputSessionName(this, oldName);

		if (newName == null)
			return;

		// Load the session to be re-named
		Session renameSession;
		if (oldName.equals(currentSession))
		{
			renameSession = SessionManager.getInstance().getCurrentSessionInstance();
		} else {
			renameSession = new Session(oldName);
			try {
				renameSession.loadXML();
			} catch (Exception e) {
				GUIUtilities.error(this, "sessions.manager.error.rename", new Object[] { oldName, newName });
				return;
			}
		}
		
		if (renameSession.rename(newName))	// rename succeeded
		{
			setNewListModel();
			lSessions.setSelectedValue(newName, true);
			if (oldName.equals(currentSession))
				currentSession = newName;
		}
		else	// rename failed
			GUIUtilities.error(this, "sessions.manager.error.rename", new Object[] { oldName, newName });
	}


	private void delete()
	{
		// TODO: Move this functionality to SessionManager
		
		String name = lSessions.getSelectedValue().toString();
		File file = new File(SessionManager.createSessionFileName(name));

		if (file.delete())
		{
			if (name.equals(currentSession))
				currentSession = null; // mark the current session as deleted
			setNewListModel();
			lSessions.setSelectedValue("default", true);
		}
		else
			GUIUtilities.error(this, "sessions.manager.error.delete",	new Object[] { file });
	}


	private void setNewListModel()
	{
		listModified = true;
		final String[] listData = SessionManager.getInstance().getSessionNames();

		lSessions.setModel(
			new AbstractListModel()
			{
				public int getSize() { return listData.length; }
				public Object getElementAt(int i) { return listData[i]; }
			}
		);
	}


	/**
	 * Convenience method for adding components to the GridBagLayout of this dialog.
	 */
	private void addComponent(Component comp,
			int gridwidth, int gridheight,
			double weightx, double weighty,
			int anchor, int fill,
			Insets insets)
	{
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridwidth = gridwidth;
		constraints.gridheight = gridheight;
		constraints.weightx = weightx;
		constraints.weighty = weighty;
		constraints.anchor = anchor;
		constraints.fill = fill;
		constraints.insets = insets;

		GridBagLayout gridBag = (GridBagLayout) getContentPane().getLayout();
		gridBag.setConstraints(comp, constraints);
		getContentPane().add(comp);
	}


	/**
	 * A <tt>MouseListener</tt> for handling double-clicks on the sessions list.
	 */
	private class MouseHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent evt)
		{
			if (evt.getClickCount() == 2)
				ok();
		}
	}


	private JList lSessions;
	private JButton bRename;
	private JButton bDelete;
	private JButton bChangeTo;
	private JButton bClose;
	private String selectedSession;
	private String currentSession;
	private boolean listModified;

}

