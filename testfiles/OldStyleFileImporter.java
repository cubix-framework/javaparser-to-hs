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
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

import common.gui.ModalJFileChooser;

import projectviewer.ProjectViewer;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTDirectory;
//}}}

/**
 *	Imports files and/or directories into a project, using the import style used
 *	by the old version of the plugin. This means that only files and directories
 *	below the project root are accepted, and that files are added with all its
 *	parents up to the project root. It uses the setting filter when importing
 *	directories recursively by default, or the CVS/Entries filter in case it
 *	was selected in the file chooser.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: OldStyleFileImporter.java 8907 2007-02-07 05:31:23Z vanza $
 */
public class OldStyleFileImporter extends FileImporter {

	//{{{ +OldStyleFileImporter(VPTNode, ProjectViewer) : <init>

	public OldStyleFileImporter(VPTNode node, ProjectViewer viewer) {
		super(node, viewer);
	}

	//}}}

	//{{{ #internalDoImport() : Collection
	/** Asks if the user wants to import files from the chosen project root. */
	protected Collection internalDoImport() {
		fileCount = 0;

		if (selected.isFile())
			selected = (VPTNode) selected.getParent();

		CVSEntriesFilter cvsFilter = new CVSEntriesFilter();
		NonProjectFileFilter filter = new NonProjectFileFilter(project);
		GlobFilter settingsFilter = GlobFilter.getImportSettingsFilter();

		JFileChooser chooser = null;
		if (selected.isDirectory() && ((VPTDirectory)selected).getFile().exists()) {
			chooser = new ModalJFileChooser(selected.getNodePath());
		} else {
			chooser = new ModalJFileChooser(project.getRootPath());
		}

		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		chooser.addChoosableFileFilter(filter);
		chooser.addChoosableFileFilter(settingsFilter);
		chooser.addChoosableFileFilter(cvsFilter);
		chooser.setFileFilter(filter);

		if(chooser.showOpenDialog(this.viewer) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File[] chosen = chooser.getSelectedFiles();
		if (chosen == null || chosen.length == 0) return null;

		FileFilter fFilter = (FileFilter) chooser.getFileFilter();
		FilenameFilter fnf = settingsFilter;
		if (fFilter instanceof FilenameFilter)
			fnf = (FilenameFilter) fFilter;

		ArrayList lst = new ArrayList();
		boolean selNodeModified = false;

		VPTNode where = null;

		for (int i = 0; i < chosen.length; i++) {

			if (i == 0) {
				// for the first node, do the following:
				// - check if files are under the root, so they can be imported
				// - find the node where the files are to be added, adding all the
				//   parents up to the root if necessary.
				String parent = getParentPath(chosen[i].getAbsolutePath());
				if (parent == null) {
					JOptionPane.showMessageDialog(viewer,
						jEdit.getProperty("projectviewer.import.not_under_root"),
						jEdit.getProperty("projectviewer.import.not_under_root.title"),
						JOptionPane.ERROR_MESSAGE);
					return null;
				}
				where = makePathTo(parent, lst);
				selNodeModified = (lst.size() != 0);
				if (!selNodeModified) {
					selected = where;
				}
			}

			VPTNode node = null;
			if (chosen[i].isDirectory()) {
				node = findDirectory(chosen[i], where, true);
				if (node.getParent() == null) {
					if (!selNodeModified || where == project) {
						lst.add(node);
					} else {
						where.insert(node, where.findIndexForChild(node));
					}
				}
				addTree(chosen[i], node, fnf, false);
			} else if (chosen[i].exists()) {
				node = findDirectory(chosen[i], selected, false);
				if (node == null) {
					node = new VPTFile(chosen[i]);
					registerFile((VPTFile)node);
					fileCount++;
					if (!selNodeModified || where == project) {
						lst.add(node);
					} else {
						where.insert(node, where.findIndexForChild(node));
					}
				}
			}

			if (i == 0) {
				postAction = new ShowNode(node);
			}
		}

		showFileCount();
		return lst;
	} //}}}

	//{{{ -getParentPath(String) : String
	/**
	 *	Returns the parent path of the given path. It checks the project root,
	 *	and in case the path is not under it, tries the canonical path of the
	 *	project root. If the path is still not under the root, returns null, or
	 *	else, returns the parent path.
	 */
	private String getParentPath(String path) {
		String rootPath = project.getRootPath();
		if (!path.startsWith(rootPath)) {
			try {
				rootPath = new File(rootPath).getCanonicalPath();
			} catch (IOException ioe) {
				Log.log(Log.WARNING, this, ioe);
				return null;
			}
			if (!path.startsWith(rootPath) || path.length() == rootPath.length()) {
				return null;
			}

			path = project.getRootPath() + File.separator +
					path.substring(rootPath.length() + 1);
		} else if (path.length() == rootPath.length()) {
			return null;
		}

		return (new File(path).getParent());
	} //}}}

}

