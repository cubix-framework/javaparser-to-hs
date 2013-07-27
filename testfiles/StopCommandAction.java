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

import gruntspud.CVSCommandHandler;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.CommandProgressDialog;
import gruntspud.ui.UIUtil;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

/**
 * Action to stop the currently running command.
 * 
 * @author magicthize
 */
public class StopCommandAction extends AbstractGruntspudAction {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle");

	protected GruntspudContext context;

	/**
	 * Constructor for the StopCommandAction object
	 * 
	 * @param context context
	 */
	public StopCommandAction(GruntspudContext context) {
		super(res, "stopCommandAction");
		this.context = context;
		putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_STOP_COMMAND));
		putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_STOP_COMMAND));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gruntspud.actions.GruntspudAction#checkAvailable()
	 */
	public boolean checkAvailable() {
		return CVSCommandHandler.getInstance().isCommandRunning() ||
			context.getViewManager().isLoadingTree();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		if (context.getViewManager().isLoadingTree()) {
			context.getViewManager().setStopTreeLoad(true);
		}
		else {
			CommandProgressDialog d = context.getViewManager().getProgressDialog();
			if (d != null
					&& JOptionPane.showConfirmDialog(d.getMainComponent(), res.getString("stopCommandAction.confirm.text"), res
							.getString("stopCommandAction.confirm.title"),
							JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UIUtil.getCachedIcon(
									Constants.ICON_TOOL_LARGE_STOP_COMMAND)) == JOptionPane.YES_OPTION) {
				CVSCommandHandler.getInstance().stop();
			}
		}
	}
}