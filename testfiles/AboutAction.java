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
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.ResourceUtil;
import gruntspud.ui.JTextBox;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;

/**
 * Action that upon activation will display a dialog containing information about this release of Gruntspud
 * 
 * @author magicthize
 */
public class AboutAction extends DefaultGruntspudAction {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle"); //$NON-NLS-1$
	public final static String ABOUT_NAME = res.getString("aboutAction.name"); //$NON-NLS-1$
	public final static String ABOUT_AUTHORS = res.getString("aboutAction.authors"); //$NON-NLS-1$
	public final static String ABOUT_TEXT = res.getString("aboutAction.text"); //$NON-NLS-1$
	public final static String ABOUT_HOME = res.getString("aboutAction.gruntspudHome"); //$NON-NLS-1$
	private String application;
	private String version;
	private String description;
	private String author;
	private String copyright;
	private String href;

	/**
	 * Constructor for the AboutAction object
	 * 
	 * @param context context
	 */
	public AboutAction(GruntspudContext context) {
		super(context);

		putValue(Action.NAME, Constants.ACTION_ABOUT);
		putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_ABOUT));
		putValue(Action.SHORT_DESCRIPTION, ABOUT_NAME);
		putValue(Action.LONG_DESCRIPTION, res.getString("aboutAction.shortDescription")); //$NON-NLS-1$
		putValue(Action.MNEMONIC_KEY, new Integer(ResourceUtil.getResourceMnemonic(res, "aboutAction.mnemonic"))); //$NON-NLS-1$
		putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_ABOUT));
	}

	private void showAbout() {
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		GridBagConstraints gBC = new GridBagConstraints();
		gBC.anchor = GridBagConstraints.CENTER;
		gBC.fill = GridBagConstraints.HORIZONTAL;
		gBC.insets = new Insets(1, 1, 1, 1);

		JLabel a = new JLabel(Gruntspud.APPLICATION_NAME);
		a.setFont(a.getFont().deriveFont(24f));
		UIUtil.jGridBagAdd(p, a, gBC, GridBagConstraints.REMAINDER);

		JLabel v = new JLabel(Gruntspud.APPLICATION_VERSION);
		v.setFont(v.getFont().deriveFont(10f));
		UIUtil.jGridBagAdd(p, v, gBC, GridBagConstraints.REMAINDER);

		JTextBox d = new JTextBox(description, 6, 25);
		UIUtil.jGridBagAdd(p, d, gBC, GridBagConstraints.REMAINDER);

		MultilineLabel x = new MultilineLabel(ABOUT_AUTHORS);
		x.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
		x.setFont(x.getFont().deriveFont(12f));
		UIUtil.jGridBagAdd(p, x, gBC, GridBagConstraints.REMAINDER);

		MultilineLabel c = new MultilineLabel(ABOUT_TEXT);
		c.setFont(c.getFont().deriveFont(10f));
		UIUtil.jGridBagAdd(p, c, gBC, GridBagConstraints.REMAINDER);

		if (href != null) {
			JLabel h = new JLabel(ABOUT_HOME);
			h.setFont(h.getFont().deriveFont(10f));
			UIUtil.jGridBagAdd(p, h, gBC, GridBagConstraints.REMAINDER);
		}

		final JComponent parent = getContext().getHost().getMainComponent();
		OptionDialog.Option close = new OptionDialog.Option(res.getString("aboutAction.closeButton.text"), res.getString( //$NON-NLS-1$
				"aboutAction.closeButton.toolTipText"), res //$NON-NLS-1$
				.getString("aboutAction.closeButton.mnemonic").charAt(0)); //$NON-NLS-1$
		OptionDialog.Option opt = OptionDialog.showOptionDialog("about", getContext(), parent, new OptionDialog.Option[]{close}, p, //$NON-NLS-1$
				res.getString("aboutAction.dialog.title"), close, new OptionDialog.Callback() { //$NON-NLS-1$
					public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
						return true;
					}

                    public void close(OptionDialog dialog, Option option) {                        
                    }
				}

				, false, true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		showAbout();
	}
}