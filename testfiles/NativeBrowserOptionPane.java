package nativebrowser;
/*
 * NativeBrowserOptionPane.java
 * part of the NativeBrowser plugin for the jEdit text editor
 * Copyright (C) 2010 Francois Rey
 * jedit at francois . rey . name
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
 *
 * $Id$
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

public class NativeBrowserOptionPane extends AbstractOptionPane implements
		ActionListener {
	private JTextField homePage;

	public NativeBrowserOptionPane() {
		super(NativeBrowserPlugin.NAME);
	}

	public void _init() {
		homePage = new JTextField(jEdit
				.getProperty(NativeBrowserPlugin.OPTION_PREFIX + "homepage"));
		JButton pickPath = new JButton(jEdit
				.getProperty(NativeBrowserPlugin.OPTION_PREFIX + "choose-file"));
		pickPath.addActionListener(this);

		JPanel pathPanel = new JPanel(new BorderLayout(0, 0));
		pathPanel.add(homePage, BorderLayout.CENTER);
		pathPanel.add(pickPath, BorderLayout.EAST);

		addComponent(jEdit.getProperty(NativeBrowserPlugin.OPTION_PREFIX
				+ "homepage.label"), pathPanel);

	}

	public void _save() {
		jEdit.setProperty(NativeBrowserPlugin.OPTION_PREFIX + "homepage",
				homePage.getText());
	}

	// end AbstractOptionPane implementation

	// begin ActionListener implementation
	public void actionPerformed(ActionEvent evt) {
		String[] paths = GUIUtilities.showVFSFileDialog(null, null,
				JFileChooser.OPEN_DIALOG, false);
		if (paths != null) {
			homePage.setText(paths[0]);
		}
	}

}
