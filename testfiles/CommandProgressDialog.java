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

package gruntspud.ui;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractGruntspudAction;
import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.GruntspudAction;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Dialog that can be used to show the current progress of a command. Two progress bars are available for use.
 * 
 * @author magicthize
 */
public class CommandProgressDialog extends JDialog {
	//  Private instance variables
	private JProgressBar progressBar, progressBar2;
	private GruntspudContext context;
	private JLabel value1;
	private JLabel value2;
	private JLabel label1;
	private JLabel label2;
	private JLabel progressBar2Name;
	private boolean cancelled;
	private String geometryPropertyName;
	private JPanel progressPanel, progress2Panel ;
	private boolean indeterminate1, indeterminate2;

	/**
	 * Creates a new CommandProgressDialog object.
	 * 
	 * @param parent parent dialog
	 * @param modal modal to parent
	 * @param context context
	 * @param action if not null, then the action will be added as a button to the dialog
	 * @param label1Text text for label 1
	 * @param label2Text text for label 2
	 * @param title dialog title
	 * @param geometryPropertyName property name to store the dialog geometry in
	 * @param createDefaultCancelAction create the default cancel action
	 */
	public CommandProgressDialog(JDialog parent, boolean modal, GruntspudContext context, Action action, String label1Text,
			String label2Text, String title, String geometryPropertyName, boolean createDefaultCancelAction) {
		super(parent, title, modal);
		init(context, action, label1Text, label2Text, geometryPropertyName, createDefaultCancelAction);
	}

	/**
	 * Creates a new CommandProgressDialog object.
	 * 
	 * @param parent parent frame
	 * @param modal modal to parent
	 * @param context context
	 * @param action if not null, then the action will be added as a button to the dialog
	 * @param label1Text text for label 1
	 * @param label2Text text for label 2
	 * @param title dialog title
	 * @param geometryPropertyName property name to store the dialog geometry in
	 * @param createDefaultCancelAction create the default cancel action
	 */
	public CommandProgressDialog(JFrame parent, boolean modal, GruntspudContext context, Action action, String label1Text,
			String label2Text, String title, String geometryPropertyName, boolean createDefaultCancelAction) {
		super(parent, title, modal);
		init(context, action, label1Text, label2Text, geometryPropertyName, createDefaultCancelAction);
	}

	/**
	 * Create a modal progress dialog
	 * 
	 * @param parent parent component
	 * @param context context
	 * @param geometryPropertyName property name to store the dialog geometry in
	 * @param label1Text text for label 1
	 * @param label2Text text for label 2
	 * @param title dialog title
	 * @param createDefaultCancelAction create the default cancel action
	 * @return dialog
	 */
	public static CommandProgressDialog createDialog(Component parent, GruntspudContext context, String geometryPropertyName,
			String label1Text, String label2Text, String title, boolean createDefaultCancelAction) {
		return createDialog(parent, context, geometryPropertyName, null, label1Text, label2Text, title, createDefaultCancelAction,
				true);
	}

	/**
	 * Create a modal progress dialog
	 * 
	 * @param parent parent component
	 * @param context context
	 * @param geometryPropertyName property name to store the dialog geometry in
	 * @param action if not null, then the action will be added as a button to the dialog
	 * @param label1Text text for label 1
	 * @param label2Text text for label 2
	 * @param title dialog title
	 * @param createDefaultCancelAction create the default cancel action
	 * @param modal make the dialog modal to the parent
	 * @return dialog
	 */
	public static CommandProgressDialog createDialog(Component parent, GruntspudContext context, String geometryPropertyName,
			Action action, String label1Text, String label2Text, String title, boolean createDefaultCancelAction, boolean modal) {
		Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, parent);
		CommandProgressDialog progressDialog = null;

		if ((w != null) && w instanceof JDialog) {
			progressDialog = new CommandProgressDialog((JDialog) w, modal, context, action, label1Text, label2Text, title,
					geometryPropertyName, createDefaultCancelAction);
		}
		else
			if ((w != null) && w instanceof JFrame) {
				progressDialog = new CommandProgressDialog((JFrame) w, modal, context, action, label1Text, label2Text, title,
						geometryPropertyName, createDefaultCancelAction);
			}
			else {
				progressDialog = new CommandProgressDialog((JFrame) null, modal, context, action, label1Text, label2Text, title,
						geometryPropertyName, createDefaultCancelAction);

			}
		return progressDialog;
	}

	private void cancel() {
		cancelled = true;
	}

	private void init(GruntspudContext context, Action action, String label1Text, String label2Text, String geometryPropertyName,
			boolean createDefaultCancelAction) {
		this.context = context;
		this.geometryPropertyName = geometryPropertyName;

		//
		progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		progressBar = new JProgressBar(0, 100);
		setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar2 = new JProgressBar(0, 100);
		progressBar2.setStringPainted(true);

		JPanel s = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(3, 3, 3, 3);
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		UIUtil.jGridBagAdd(s, label1 = new JLabel(label1Text), gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(s, value1 = new JLabel(), gbc, GridBagConstraints.REMAINDER);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(s, label2 = new JLabel(label2Text), gbc, GridBagConstraints.RELATIVE);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		UIUtil.jGridBagAdd(s, value2 = new JLabel(), gbc, GridBagConstraints.REMAINDER);
		progressPanel.add(s, BorderLayout.NORTH);
		progressBar2Name = new JLabel(" ", JLabel.CENTER);
		progress2Panel = new JPanel(new BorderLayout(0, 4));
		progress2Panel.add(progressBar2Name, BorderLayout.NORTH);
		progress2Panel.add(progressBar2, BorderLayout.CENTER);

		JPanel p = new JPanel(new BorderLayout(0, 4));
		p.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		p.add(progressBar, BorderLayout.NORTH);
		p.add(progress2Panel, BorderLayout.SOUTH);

		progressPanel.add(p, BorderLayout.SOUTH);

		//
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(progressPanel, BorderLayout.CENTER);

		//  If an action was specifed, create the default
		if ((action == null) && createDefaultCancelAction) {
			action = new CancelAction();

		}
		if (action != null) {
			JPanel b = new JPanel(new FlowLayout(FlowLayout.CENTER));
			b.add(UIUtil.createButton(action, true, false));
			getContentPane().add(b, BorderLayout.SOUTH);
		}

		//  Postion and size
		if (context.getHost().isGeometryStored(geometryPropertyName)) {
			context.getHost().loadGeometry(this, geometryPropertyName);
		}
		else {
			pack();
			UIUtil.positionComponent(SwingConstants.CENTER, this);
		}
		
		setProgress2Visible(false);

		//  Listen for window closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				saveGeometry();
			}
		});
	}

	public void saveGeometry() {
		Constants.PLUGIN_LOG.info("Saving progress dialog geometry " + CommandProgressDialog.this.geometryPropertyName + " as "
				+ getSize());
		CommandProgressDialog.this.context.getHost()
				.saveGeometry(CommandProgressDialog.this, CommandProgressDialog.this.geometryPropertyName);
	}

	/**
	 * Set the visibility of the dialog.
	 * 
	 * @param visible visible
	 */
	public void setVisible(boolean visible) {
		if (isVisible() && !visible) {
			saveGeometry();

		}
		super.setVisible(visible);
	}

	/**
	 * Return the main component used in the dialog
	 * 
	 * @return component
	 */
	public JComponent getMainComponent() {
		return progressPanel;
	}

	/**
	 * Get if the button created for the cancel action was created
	 * 
	 * @return cancelled
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Set the text for label 1
	 * 
	 * @param text label 1 text
	 */
	public void setLabel1Text(String text) {
		label1.setText(text);
	}

	/**
	 * Set the text for label 2
	 * 
	 * @param text label 2 text
	 */
	public void setLabel2Text(String text) {
		label2.setText(text);
	}

	/**
	 * Set whether progress bar 1 is indeterminate. This only works on J2SE 1.4+
	 * 
	 * @param indeterminate indeterminate
	 */
	public void setIndeterminate(boolean indeterminate) {
		try {
			progressBar.getClass().getMethod("setIndeterminate", new Class[]{boolean.class})
					.invoke(progressBar, new Object[]{new Boolean(indeterminate)});
		}
		catch (Exception e) {
			setProgressValue(0);
		}
		this.indeterminate1 = indeterminate;
	}

	/**
	 * Set whether progress bar 1 is indeterminate. This only works on J2SE 1.4+
	 * 
	 * @param indeterminate indeterminate
	 */
	public void setIndeterminate2(boolean indeterminate) {
		try {
			progressBar2.getClass().getMethod("setIndeterminate", new Class[]{boolean.class})
					.invoke(progressBar, new Object[]{new Boolean(indeterminate)});
		}
		catch (Exception e) {
			setProgress2Value(0);
		}
		this.indeterminate2 = indeterminate;
	}

	/**
	 * Set the text on the first progress bar
	 * 
	 * @param text text on first progress bar
	 */
	public void setString(String text) {
		progressBar.setString(text);
	}

	/**
	 * Set whether the text is painted on the first progress bar
	 * 
	 * @param stringPainted string painted
	 */
	public void setStringPainted(boolean stringPainted) {
		progressBar.setStringPainted(stringPainted);
	}

	/**
	 * Get if the text is painted on the first progress bar
	 * 
	 * @return string painted on progress bar 1
	 */
	public boolean isStringPainted() {
		return progressBar.isStringPainted();
	}

	/**
	 * Set the text on the second progress bar
	 * 
	 * @param text text on second progress bar
	 */
	public void setString2(String text) {
		progressBar2.setString(text);
	}

	/**
	 * Set whether the text is painted on the second progress bar
	 * 
	 * @param stringPainted string painted
	 */
	public void setString2Painted(boolean stringPainted) {
		progressBar2.setStringPainted(stringPainted);
	}

	/**
	 * Get if the text is painted on the first progress bar
	 * 
	 * @return string painted on progress bar 1
	 */
	public boolean isString2Painted() {
		return progressBar2.isStringPainted();
	}

	/**
	 * Set the text of the second <i>value</i> label.
	 * 
	 * @param text value 2 text
	 */
	public void setValue2Text(String text) {
		value2.setText(text);
	}

	/**
	 * Set the tooltip on the second <i>value</i> label.
	 * 
	 * @param text tool tip text
	 */
	public void setValue2ToolTipText(String text) {
		value2.setToolTipText(text);
	}

	/**
	 * Set the text of the first <i>value</i> label.
	 * 
	 * @param text value 1 text
	 */
	public void setValue1Text(String text) {
		value1.setText(text);
	}

	/**
	 * Set the maximum value of the first progress bar
	 * 
	 * @param max progress bar 1 maximum
	 */
	public void setProgressMaximum(int max) {
		progressBar.setMaximum(max);
	}

	/**
	 * Set the maximum value of the second progress bar
	 * 
	 * @param max progress bar 2 maximum
	 */
	public void setProgress2Maximum(int max) {
		progressBar2.setMaximum(max);
	}

	/**
	 * Set the minimum value of the first progress bar
	 * 
	 * @param min progress bar 1 minimum
	 */
	public void setProgressMinimum(int min) {
		progressBar.setMinimum(min);
	}

	/**
	 * Set the value of the first progress bar
	 * 
	 * @param val progress bar 1 value
	 */
	public void setProgressValue(int val) {
		progressBar.setValue(val);
	}

	/**
	 * Set the value of the second progress bar
	 * 
	 * @param val progress bar 2 value
	 */
	public void setProgress2Value(int val) {
		progressBar2.setValue(val);
	}

	/**
	 * Set the text displayed above the second progress bar 
	 * 
	 * @param name progress bar 2 name
	 */
	public void setProgress2Name(String name) {
		progressBar2Name.setText(name);
	}

	/**
	 * Set the tool tip text of the first <i>value</i> label
	 * 
	 * @param text tool tip text of value 1
	 */
	public void setValue1ToolTipText(String text) {
		value1.setToolTipText(text);
	}
	
	/**
	 * Set whether the second progress bar and its name is visible
	 * 
	 * @param visibile progress bar 2 visibile
	 */
	public void setProgress2Visible(boolean visible) {
		progress2Panel.setVisible(visible);
		packHeight();
	}

	/**
	 * Get the maximum value of the first progress bar
	 * 
	 * @return maximum value of first progress bar
	 */
	public int getProgressMaximum() {
		return progressBar.getMaximum();
	}

	/**
	 * Get the current value of the first progress bar
	 * 
	 * @return current value of first progress bar
	 */
	public int getProgressValue() {
		return progressBar.getValue();
	}

	/**
	 * Return if the first progress bar is indeterminate
	 * 
	 * @return indeterminate
	 */
	public boolean isProgressIndeterminate() {
		return indeterminate1;
	}
	
	/**
	 * Pack the dialog, but change the height only
	 */
	public void packHeight() {
		Container parent = getParent();
		if (parent != null && parent.getPeer() == null) {
			parent.addNotify();
		}
		if (getPeer() == null) {
			addNotify();
		}
		setSize(new Dimension(getSize().width, getPreferredSize().height));
		validate();		
	}

	/**
	 * Get if the second progress bar is visible
	 * 
	 * @return second progress bar
	 */
	public boolean isProgress2Visible() {
		return progress2Panel.isVisible();
	}

	public class CancelAction extends AbstractGruntspudAction {
		CancelAction() {
			putValue(Action.NAME, "Cancel");
			putValue(GruntspudAction.SHOW_NAME, Boolean.TRUE);
			putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_STOP_COMMAND));
			putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_STOP_COMMAND));
			putValue(Action.MNEMONIC_KEY, new Integer('c'));
		}

		public void actionPerformed(ActionEvent evt) {
			cancel();
		}
	}
}