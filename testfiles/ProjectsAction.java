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

package gruntspud.actions;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ResourceUtil;
import gruntspud.project.Project;
import gruntspud.project.ProjectsPane;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Action to show the projects manager dialog
 * 
 * @author magicthize
 */
public class ProjectsAction extends DefaultGruntspudAction {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle");

	/**
	 * Constructor for the ProjectsAction object
	 * 
	 * @param context context
	 */
	public ProjectsAction(GruntspudContext context) {
		super(res, "projectsAction", context);
		putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_PROJECTS));
		putValue(GruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_PROJECTS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gruntspud.actions.GruntspudAction#checkAvailable()
	 */
	public boolean checkAvailable() {
		return getContext().getProjectListModel() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent evt) {
		OptionDialog.Option close = new OptionDialog.Option(res.getString("projectsAction.optionDialog.option.close.text"), res
				.getString("projectsAction.optionDialog.option.close.toolTipText"),
				ResourceUtil.getResourceMnemonic(res, "projectsAction.optionDialog.option.close.mnemonic"));
		final JComponent parent = getContext().getHost().getMainComponent();
		final ProjectsPane projectsPane = new ProjectsPane(getContext());
		OptionDialog.Option[] options = new OptionDialog.Option[]{close};
		String title = res.getString("projectsAction.optionDialog.title");
		Icon icon = UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_PROJECTS);
		OptionDialog dialog = null;
		Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, parent);
		if (w instanceof JFrame) {
			dialog = new OptionDialog((JFrame) w, options, projectsPane, title, options[0], null, getContext(), true, icon, "projects");
		}
		else
			if (w instanceof JDialog) {
				dialog = new OptionDialog((JDialog) w, options, projectsPane, title, options[0], null, getContext(), true, icon, "projects");
			}
			else {
				dialog = new OptionDialog((JFrame) null, options, projectsPane, title, options[0], null, getContext(), true, icon, "projects");
			}
		if (!getContext().getHost().isGeometryStored(Constants.OPTION_DIALOG_GEOMETRY_PREFIX + "projects")) {
			dialog.pack();
			UIUtil.positionComponent(SwingConstants.CENTER, dialog);
		}
		else {
			getContext().getHost().loadGeometry(dialog, Constants.OPTION_DIALOG_GEOMETRY_PREFIX + "projects");
		}
		dialog.getRootPane().setDefaultButton(dialog.getDefaultButton());
		dialog.setResizable(true);
		final OptionDialog optDialog = dialog;
		projectsPane.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Project prj = projectsPane.getSelectedProject();
					if (prj.getHome() != null && prj.getHome().exists() && prj.getHome().isDirectory()) {
						getContext().getViewManager().changeHome(prj.getHome());
					}
					else {
						throw new Exception(res.getString("projectsAction.error.projectHomeDoesNotExist.text"));
					}
				}
				catch (Exception e) {
					GruntspudUtil.showErrorMessage(projectsPane, res.getString("projectsAction.error.title"), e);
				}
				optDialog.setVisible(false);
			}
		});
		dialog.setVisible(true);
		getContext().getHost().saveGeometry(dialog, Constants.OPTION_DIALOG_GEOMETRY_PREFIX + "projects");
	}
}