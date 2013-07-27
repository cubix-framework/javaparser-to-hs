/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.project;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractEditAction;
import gruntspud.actions.AbstractNormalAddAction;
import gruntspud.actions.AbstractNormalRemoveAction;
import gruntspud.actions.AbstractSetDefaultAction;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventListener;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Description of the Class
 * 
 * @author magicthize @created 26 May 2002
 */
public class ProjectsPane extends JPanel {
	private GruntspudContext context;
	private JList projectsList;
	private Action addAction, removeAction, editAction, setAsCurrentAction;
	private ProjectListModel projects;

	/**
	 * Constructor for the DiffOptionsPane object
	 * 
	 * @param host Description of the Parameter
	 */
	public ProjectsPane(GruntspudContext context) {
		super(new BorderLayout());

		this.context = context;

		projects = context.getProjectListModel();

		//  Create the text area
		projectsList = new JList(projects);
		projectsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		projectsList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				setAvailableActions();
			}
		});
		projectsList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2)
					fireActionEvent();
			}
		});
		projectsList.setVisibleRowCount(10);

		//  Create the toolbar
		JToolBar toolBar = new JToolBar("Ignore file editor tools");
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		toolBar.setBorder(null);
		toolBar.setFloatable(false);
		boolean showSelectiveText = context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
		toolBar.add(UIUtil.createButton(addAction = new AddAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(removeAction = new RemoveAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(editAction = new EditAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(setAsCurrentAction = new SetAsCurrentAction(), showSelectiveText, false));

		//
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(toolBar, BorderLayout.NORTH);
		topPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

		//  Build this
		add(topPanel, BorderLayout.NORTH);

		JScrollPane projectsListScroller = new JScrollPane(projectsList);
		add(projectsListScroller, BorderLayout.CENTER);

		//  Set the intially available actions
		setAvailableActions();
	}

	public void addActionListener(ActionListener l) {
		listenerList.add(ActionListener.class, l);
	}

	public void removeActionListener(ActionListener l) {
		listenerList.remove(ActionListener.class, l);
	}

	private void fireActionEvent() {
		EventListener[] l = listenerList.getListeners(ActionListener.class);
		ActionEvent evt = null;
		Project prj = getSelectedProject();
		if (prj != null) {
			for (int j = l.length - 1; j >= 0; j--) {
				if (evt == null) {
					evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, prj.getName());
				}
				((ActionListener) l[j]).actionPerformed(evt);
			}
		}
	}

	public Project getSelectedProject() {
		int idx = projectsList.getSelectedIndex();
		return idx == -1 ? null : projects.getProjectAt(idx);
	}

	/**
	 * Set what actions are available depending on state
	 */
	private void setAvailableActions() {
		setAsCurrentAction.setEnabled(projectsList.getSelectedIndices().length == 1);
		editAction.setEnabled(projectsList.getSelectedIndices().length == 1);
		removeAction.setEnabled(projectsList.getSelectedIndices().length == 1);
	}

	class AddAction extends AbstractNormalAddAction {
		/**
		 * Constructor for the CutAction object
		 */
		AddAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			Project prj = new Project("project" + context.getProjectListModel().getSize());
			final ProjectEditorPane editor = new ProjectEditorPane(context, prj);
			OptionDialog.Option ok = new OptionDialog.Option("Add", "Add the project", 'a');
			final OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel", 'c');
			OptionDialog.Option opt = OptionDialog.showOptionDialog("addProject", context, ProjectsPane.this,
					new OptionDialog.Option[]{ok, cancel}, editor, "Add project", ok, new OptionDialog.Callback() {
						public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
							return option == cancel || editor.validateTab();
						}

                        public void close(OptionDialog dialog, Option option) {
                            // TODO Auto-generated method stub
                            
                        }
					}, true, false, new Dimension(400, 300));
			if ((opt != ok)) {
				return;
			}
			editor.applyTab();
			projects.add(prj);
		}
	}

	class RemoveAction extends AbstractNormalRemoveAction {
		/**
		 * Constructor for the DeleteAction object
		 */
		RemoveAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			if (JOptionPane.showConfirmDialog(ProjectsPane.this, "Are you sure you want to remove this project?", "Remove project",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIUtil.getCachedIcon(
							Constants.ICON_TOOL_LARGE_NORMAL_REMOVE)) == JOptionPane.YES_OPTION) {
				projects.removeProjectAt(projectsList.getSelectedIndex());
			}
		}
	}

	class EditAction extends AbstractEditAction {
		/**
		 * Constructor for the DeleteAction object
		 */
		EditAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			Project prj = projects.getProjectAt(projectsList.getSelectedIndex());
			final ProjectEditorPane editor = new ProjectEditorPane(context, prj);
			OptionDialog.Option ok = new OptionDialog.Option("Update", "Update the project", 'a');
			final OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel", 'c');
			OptionDialog.Option opt = OptionDialog.showOptionDialog("editProject", context, ProjectsPane.this,
					new OptionDialog.Option[]{ok, cancel}, editor, "Edit project", ok, new OptionDialog.Callback() {
						public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
							return option == cancel || editor.validateTab();
						}

                        public void close(OptionDialog dialog, Option option) {
                            // TODO Auto-generated method stub
                            
                        }
					}, true, false, new Dimension(400, 300));
			if ((opt != ok)) {
				return;
			}
			editor.applyTab();
			projects.setProjectAt(projectsList.getSelectedIndex(), prj);
		}
	}

	class SetAsCurrentAction extends AbstractSetDefaultAction {
		/**
		 * Constructor for the SetDefaultAction object
		 */
		SetAsCurrentAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			fireActionEvent();
		}
	}
}