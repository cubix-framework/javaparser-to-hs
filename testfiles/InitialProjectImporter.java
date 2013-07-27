/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
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
package projectviewer.importer;

//{{{ Imports
import java.io.File;
import java.io.FilenameFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.jEdit;

import projectviewer.ProjectViewer;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTDirectory;
//}}}

/**
 *	Imports files and/or directories into a project.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: InitialProjectImporter.java 6099 2003-02-21 16:24:19Z vanza $
 */
public class InitialProjectImporter extends FileImporter {

	//{{{ Private members
	private Component parent;
	//}}}

	//{{{ Constructor

	public InitialProjectImporter(VPTNode node, ProjectViewer viewer, Component parent) {
		super(node, viewer);
		if (parent != null) {
			this.parent = parent;
		} else {
			this.parent = viewer;
		}
	}

	public InitialProjectImporter(VPTNode node, ProjectViewer viewer) {
		this(node, viewer, null);
	}

	//}}}

	//{{{ internalDoImport() method
	/** Asks if the user wants to import files from the chosen project root. */
	protected Collection internalDoImport() {
		fileCount = 0;

		Object[] options = {
			jEdit.getProperty("projectviewer.import.yes-settings"),
			jEdit.getProperty("projectviewer.import.yes-all"),
			jEdit.getProperty("projectviewer.import.yes-cvs"),
			jEdit.getProperty("projectviewer.import.no")
		};
		Object sel = JOptionPane.showInputDialog(parent,
						jEdit.getProperty("projectviewer.import.msg_proj_root"),
						jEdit.getProperty("projectviewer.import.msg_proj_root.title"),
						JOptionPane.QUESTION_MESSAGE,
						null, options, options[0]);

		FilenameFilter fnf = null;
		if (sel == null || sel == options[3]) {
			return null;
		} else if (sel == options[0]) {
			fnf = new ImportSettingsFilter();
		} else if (sel == options[2]) {
			fnf = new CVSEntriesFilter();
		}

		addTree(new File(project.getRootPath()), project, fnf);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					ProjectViewer.nodeStructureChanged(project);
				}
			});
		} catch (InterruptedException ie) {
			// not gonna happen
		} catch (java.lang.reflect.InvocationTargetException ite) {
			// not gonna happen
		}

		showFileCount();
		return null;
	} //}}}

}

