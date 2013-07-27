/*
 *  JarMakerDialog.java
 *  part of the JarMaker plugin for the jEdit text editor
 *  Copyright (C) 2002 Alex Levin
 *  alevin@users.sourceforge.net
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package projectviewer.persist;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.util.Log;

import common.gui.ModalJFileChooser;
import common.gui.OkCancelButtons;
//}}}

/**
 *  The JarMaker plugin dialog, with enhancements and modifications to work
 *	the way ProjectViewer needs it to.
 *
 *	@author     Alex Levin
 *	@author		Marcelo Vanzin
 *	@version	$Id: ProjectZipper.java 8907 2007-02-07 05:31:23Z vanza $
 */
public class ProjectZipper extends EnhancedDialog implements ActionListener {

	//{{{ Private members
	private JPanel jp;
	private HistoryTextField jarName, jarLoc;
	private JComboBox jarFilter;
	private JTextArea manifest;
	private JarOutputStream jos;
	private File dir;
	private String jarPath;
	private boolean addManifest = true;
	private JButton ok, cancel, browseName, browseLocation, loadManifest;
	private JCheckBox manifestCb;
	//}}}

	//{{{ +ProjectZipper(View) : <init>
	public ProjectZipper(View view) {
		this(view, null, true);
	} //}}}

	//{{{ +ProjectZipper(View, String, boolean) : <init>
	public ProjectZipper(View view, String dir, boolean autoshow) {
		super(view, jEdit.getProperty("projectviewer.action.jarmaker.title"), false);

		GridBagConstraints gbc = new GridBagConstraints();
		GridBagLayout gbl = new GridBagLayout();
		getContentPane().setLayout(gbl);

		// Jar name
		JLabel label = new JLabel(jEdit.getProperty("projectviewer.action.jarmaker.name"));
		gbc.weightx = 0.0;
		gbl.setConstraints(label, gbc);
		getContentPane().add(label);

		jarName = new HistoryTextField("projectviewer.jarmaker.jarname");
		gbc.weightx = 2.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(jarName, gbc);
		getContentPane().add(jarName);

		Icon openDirIcon = GUIUtilities.loadIcon("OpenFolder.png");
		browseName = new JButton();
		browseName.setIcon(openDirIcon);
		browseName.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(browseName, gbc);
		getContentPane().add(browseName);

		// Location to add
		label = new JLabel(jEdit.getProperty("projectviewer.action.jarmaker.location"));
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbl.setConstraints(label, gbc);
		getContentPane().add(label);

		jarLoc = new HistoryTextField("jarmaker.location");
		if (dir != null) {
			jarLoc.setText(dir);
		} else if (jarLoc.getText().length() != 0) {
			jarLoc.addCurrentToHistory();
		}
		gbc.weightx = 2.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(jarLoc, gbc);
		getContentPane().add(jarLoc);

		openDirIcon = GUIUtilities.loadIcon("OpenFolder.png");
		browseLocation = new JButton();
		browseLocation.setIcon(openDirIcon);
		browseLocation.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(browseLocation, gbc);
		getContentPane().add(browseLocation);

		// Filters
		label = new JLabel(jEdit.getProperty("projectviewer.action.jarmaker.filter"));
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbl.setConstraints(label, gbc);
		getContentPane().add(label);

		jarFilter = new JComboBox();
		jarFilter.addItem(new NoJarJavaFilter());
		jarFilter.addItem(new ClassFileFilter());
		jarFilter.addItem(new AcceptAllFilter());

		gbc.weightx = 2.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(jarFilter, gbc);
		getContentPane().add(jarFilter);

		// Add manifest checkbox / load
		JPanel addPanel = new JPanel(new BorderLayout());
		manifestCb = new JCheckBox(jEdit.getProperty("projectviewer.action.jarmaker.add_manifest"));
		manifestCb.setSelected(true);
		manifestCb.addActionListener(this);
		addPanel.add(BorderLayout.WEST, manifestCb);

		loadManifest = new JButton(jEdit.getProperty("projectviewer.action.jarmaker.load_manifest"));
		loadManifest.addActionListener(this);
		addPanel.add(BorderLayout.EAST, loadManifest);

		gbc.weightx = 2.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(addPanel, gbc);
		getContentPane().add(addPanel);

		// Add manifest panel
		manifest = new JTextArea("Manifest-Version: 1.0\nCreated-By: JEdit\n");
		JScrollPane mScroll = new JScrollPane(manifest);
		mScroll.setBorder(
				BorderFactory.createTitledBorder(jEdit.getProperty("projectviewer.action.jarmaker.manifest"))
		);
		gbc.weightx = 2.0;
		gbc.weighty = 2.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(mScroll, gbc);
		getContentPane().add(mScroll);

		// OK / Cancel
		JPanel btnPanel = new OkCancelButtons(this);

		gbc.weightx = 2.0;
		gbc.weighty = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(btnPanel, gbc);
		getContentPane().add(btnPanel);

		if (jEdit.getProperty("projectviewer.jarmaker_dialog.x") != null) {
			GUIUtilities.loadGeometry(this, "projectviewer.jarmaker_dialog");
		} else {
			pack();
		}
		setLocationRelativeTo(view);
		if (autoshow) setVisible(true);
	} //}}}

	//{{{ +addFileFilter(FileFilter) : void
	public void addFileFilter(FileFilter ff) {
		jarFilter.addItem(ff);
	} //}}}

	//{{{ +setSelectedFilter(FileFilter) : void
	public void setSelectedFilter(FileFilter ff) {
		jarFilter.setSelectedItem(ff);
	} //}}}

	//{{{ +ok() : void
	public void ok() {
		String name = jarName.getText().trim();
		if(name.length() == 0) {
			JOptionPane.showMessageDialog(this,
				jEdit.getProperty("projectviewer.action.jarmaker.no_name"),
				jEdit.getProperty("projectviewer.action.jarmaker.no_name.title"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (!name.toLowerCase().endsWith(".jar") && !name.toLowerCase().endsWith(".war") &&
				!name.toLowerCase().endsWith(".ear")) {
			name = name + ".jar";
		}
		String location = jarLoc.getText().trim();
		if (name.indexOf(File.separatorChar) == -1) {
			jarPath = location + File.separator + name;
		} else {
			jarPath = name;
		}
		jarPath = jarPath.replace(File.separatorChar, '/');

		if (location.endsWith(File.separator) || location.endsWith("/")) {
			location = location.substring(0, location.length() - 1);
		}
		dir = new File(location);
		if (!dir.exists()) {
			JOptionPane.showMessageDialog(this,
				jEdit.getProperty("projectviewer.action.jarmaker.dir_not_found"),
				jEdit.getProperty("projectviewer.action.jarmaker.dir_not_found.title"),
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// Add Manifest
			if (addManifest) {
				InputStream is = new ByteArrayInputStream(manifest.getText().getBytes("UTF-8"));
				Manifest mf = new Manifest(is);
				jos = new JarOutputStream(new FileOutputStream(jarPath), mf);
			} else {
				jos = new JarOutputStream(new FileOutputStream(jarPath));
			}
			// Start traversing the directory tree
			fileTraverse(new File(jarLoc.getText()));
			jos.close();

			saveToHistory(jarName);
			saveToHistory(jarLoc);
			cancel();
		} catch (IOException e) {
			Log.log(Log.ERROR, this, e);
		}

	} //}}}

	//{{{ +cancel() : void
	public void cancel() {
		GUIUtilities.saveGeometry(this, "projectviewer.jarmaker_dialog");
		dispose();
	} //}}}

	//{{{ -saveToHistory(HistoryTextField) : void
	private void saveToHistory(HistoryTextField tf) {
		if (tf.getText().length() != 0) {
			tf.addCurrentToHistory();
		}
	} //}}}

	//{{{ -convertToJarName(File) : String
	/** Converts file's pathname to a form acceptable to jar files */
	private String convertToJarName(File file) throws IOException {
		String rootdir = dir.getCanonicalPath();
		String filename = file.getCanonicalPath();
		// I don't check if filename and rootdir are equal for speed so catch the exception instead
		try {
			filename = filename.substring(rootdir.length() + 1);
		}
		catch (IndexOutOfBoundsException e) {
		}
		filename = filename.replace(File.separatorChar, '/');
		return filename;
	} //}}}

	//{{{ -fileTraverse(File) : void
	private void fileTraverse(File dir) {
		File[] file_list = dir.listFiles((FileFilter)jarFilter.getSelectedItem());
		for (int i = 0; i < file_list.length; i++) {
			// Write this file to the jar
			if (!file_list[i].isDirectory()) {
				writeJarEntry(file_list[i]);
			}
			else {
				fileTraverse(file_list[i]);
			}
		}
	} //}}}

	//{{{ -writeJarEntry(File) : void
	private void writeJarEntry(File f) {
		byte[] buf = new byte[1024];
		int len;
		String filePath = f.getAbsolutePath();
		filePath = filePath.replace(File.separatorChar, '/');
		try {
			// Avoid adding created jar to itself
			if ((!addManifest || !filePath.toLowerCase().endsWith(".mf"))
					&& !filePath.equals(jarPath)) {
				JarEntry je = new JarEntry(convertToJarName(f));
				FileInputStream is = new FileInputStream(f);
				je.setTime(f.lastModified());
				// Add the jar entry
				jos.putNextEntry(je);

				while ((len = is.read(buf)) >= 0) {
					jos.write(buf, 0, len);
				}

				jos.closeEntry();
				is.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();

		if (source == ok) {
			ok();
		} else if (source == cancel) {
			cancel();
		} else if (source == browseName) {
			JFileChooser chooser = new ModalJFileChooser();
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			chooser.setDialogTitle(jEdit.getProperty("projectviewer.action.jarmaker.choose_dir"));
			if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
				jarName.setText(chooser.getSelectedFile().getPath());
		} else if (source == browseLocation) {
			JFileChooser chooser = new ModalJFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle(jEdit.getProperty("projectviewer.action.jarmaker.choose_dir"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
				jarLoc.setText(chooser.getSelectedFile().getPath());
		} else if (source == loadManifest) {
			// reads a file and puts its contents in the Manifest text area
			try {
				JFileChooser chooser;
				if (jarLoc.getText().length() != 0) {
					chooser = new ModalJFileChooser(jarLoc.getText());
				} else {
					chooser = new ModalJFileChooser();
				}
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					String chosen = chooser.getSelectedFile().getAbsolutePath();
					StringWriter sw = new StringWriter();
					FileReader fr = new FileReader(chosen);
					int i;

					while ((i = fr.read()) != -1) {
						sw.write((char)i);
					}

					fr.close();
					sw.close();

					manifest.setText(sw.toString());
				}
			} catch (IOException ioe) {
				Log.log(Log.ERROR, this, ioe);
			}
		} else if (source == manifestCb) {
			manifest.setEnabled(manifestCb.isSelected());
			loadManifest.setEnabled(manifestCb.isSelected());
		}
	} //}}}

	//{{{ -class _NoJarJavaFilter_
	/** Filters out *.java and *.jar files. */
	private static class NoJarJavaFilter implements FileFilter {

		//{{{ +toString() : String
		public String toString() {
			return jEdit.getProperty("projectviewer.action.jarmaker.no_jar_java");
		} //}}}

		//{{{ +accept(File) : boolean
		public boolean accept(File file) {
			return file.isDirectory() ||
				(!file.getName().endsWith(".java") &&
				 !file.getName().endsWith(".jar"));
		} //}}}

	} //}}}

	//{{{ -class _ClassFileFilter_
	/** Accepts class files only. */
	private static class ClassFileFilter implements FileFilter {

		//{{{ +toString() : String
		public String toString() {
			return jEdit.getProperty("projectviewer.action.jarmaker.class_files");
		} //}}}

		//{{{ +accept(File) : boolean
		public boolean accept(File file) {
			return file.isDirectory() || file.getName().endsWith(".class");
		} //}}}

	} //}}}

	//{{{ -class _AcceptAllFilter_
	/** Accepts class files only. */
	private static class AcceptAllFilter implements FileFilter {

		//{{{ +toString() : String
		public String toString() {
			return jEdit.getProperty("projectviewer.action.jarmaker.all_files");
		} //}}}

		//{{{ +accept(File) : boolean
		public boolean accept(File file) {
			return true;
		} //}}}

	} //}}}

}

