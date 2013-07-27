/*
 * Copyright (C) 2002 Calvin Yu
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
package lookandfeel;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

/**
 * A component for configuring a path.
 */
class PathComponent extends Box
	implements ActionListener
{

	private static String startingDirectory;

	private String property;
	private JTextField path;
	private JButton selectPath;
	private JButton deletePath;

	/**
	 * Create a new <code>PathComponent</code>.
	 */
	public PathComponent(String aProperty, boolean isRequired)
	{
		super(BoxLayout.X_AXIS);
		property = aProperty;
		String pathProperty = jEdit.getProperty(property);
		if (pathProperty == null) {
			pathProperty = "";
		}
		path = new JTextField(pathProperty, 20);
		path.setEditable(false);
		add(path);

		selectPath = new JButton(GUIUtilities.loadIcon("OpenFolder.png"));
		selectPath.setMargin(new Insets(0,0,0,0));
		selectPath.setToolTipText(jEdit.getProperty(property + ".select-tooltip"));
		selectPath.addActionListener(this);
		add(selectPath);

		if (!isRequired) {
			deletePath = new JButton(GUIUtilities.loadIcon("closebox.gif"));
			deletePath.setMargin(new Insets(0,0,0,0));
			deletePath.setToolTipText(jEdit.getProperty(property + ".remove-tooltip"));
			deletePath.setPreferredSize(selectPath.getPreferredSize());
			deletePath.addActionListener(this);
			add(deletePath);
		}
	}

	/**
	 * Assert that the given path exists and is readable.
	 */
	public boolean assertPath(boolean canBeEmpty)
	{
		if (canBeEmpty && LookAndFeelPlugin.isEmpty(getPath())) {
			return true;
		}
		File file = new File(getPath());
		if (!file.exists() || !file.canRead()) {
			GUIUtilities.error(this, "lookandfeel.message.fnf",
				new Object[] { file });
			return false;
		}
		return true;
	}

	/**
	 * Returns <code>true</code> if the path is empty.
	 */
	public boolean isPathEmpty()
	{
		return LookAndFeelPlugin.isEmpty(getPath());
	}

	/**
	 * Returns the path.
	 */
	public String getPath()
	{
		return path.getText();
	}

	/**
	 * Handle an action event.
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getSource() == selectPath) {
			selectPath();
		} else if (evt.getSource() == deletePath) {
			deletePath();
		}
	}

	/**
	 * Select a path.
	 */
	private void selectPath()
	{
		File oldFile = null;
		JFileChooser fc = getFileChooser();
		if (!LookAndFeelPlugin.isEmpty(path.getText())) {
			oldFile = new File(path.getText());
			fc.setCurrentDirectory(oldFile.getParentFile());
			fc.setSelectedFile(oldFile);
		}

		int answer = fc.showOpenDialog(this);
		startingDirectory = fc.getCurrentDirectory().getAbsolutePath();

		if (answer == JFileChooser.APPROVE_OPTION) {
			File newfile = fc.getSelectedFile();
			if (newfile != null && (oldFile == null || !newfile.equals(oldFile))) {
				path.setText(newfile.getAbsolutePath());
			}
		}
	}

	/**
	 * Delete the path.
	 */
	private void deletePath()
	{
		path.setText("");
		//TODO: Move this
		GUIUtilities.message(this, "lookandfeel.message.restart", null);
	}

	/**
	 * Returns a file chooser component.
	 */
	private JFileChooser getFileChooser() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setDialogTitle(property + ".title");
		chooser.setFileFilter(new CustomFilter());
		chooser.setCurrentDirectory(
			new File(startingDirectory == null ? "" : startingDirectory));
		return chooser;
	}

	/**
	 * A filter built from properties.
	 */
	class CustomFilter extends FileFilter
	{
		private Set extensions;

		/**
		 * Create a new <code>CustomFilter</code>.
		 */
		public CustomFilter()
		{
			extensions = new HashSet(2);
			String extProp = jEdit.getProperty(property + ".filter.extensions");
			if (extProp != null) {
				StringTokenizer strtok = new StringTokenizer(extProp);
				while (strtok.hasMoreTokens()) {
					extensions.add(strtok.nextToken());
				}
			}
		}

		/**
		 * Accept a file if it is a file and a has a matching extension.
		 * If there are no matching extensions, all files are accepted.
		 */
		public boolean accept(File f) {
			if (f.isDirectory() || extensions.isEmpty()) {
				return true;
			}
			int idx = f.getName().lastIndexOf('.');
			if (idx < 0) {
				return false;
			}
			return extensions.contains(f.getName().substring(idx + 1));
		}

		/**
		 * Returns the description of this filter.
		 */
		public String getDescription() {
			return jEdit.getProperty(property + ".filter.description");
		}
	}

}
