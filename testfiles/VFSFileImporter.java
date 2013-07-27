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

import java.util.ArrayList;
import java.util.Collection;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;

import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.browser.VFSBrowser;

import projectviewer.ProjectViewer;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VFSFile;
import projectviewer.vpt.VPTDirectory;
//}}}

/**
 *	Imports files from a VFS into a project. If any selected file is actually
 *	from the "File" VFS, a normal file is added instead of using the VFS
 *	facilities.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: VFSFileImporter.java 6334 2005-02-10 06:33:28Z vanza $
 */
public class VFSFileImporter extends Importer {

	//{{{ Protected Members
	protected int fileCount;
	//}}}

	//{{{ +VFSFileImporter(VPTNode, ProjectViewer) : <init>

	public VFSFileImporter(VPTNode node, ProjectViewer viewer) {
		super(node, viewer, true);
	}

	//}}}

	//{{{ #internalDoImport() : Collection
	/**
	 *	Queries the user for files to be added by showing a jEdit file chooser
	 *	dialog.
	 *
	 *	@return	A collection of VPTNode instances.
	 */
	protected Collection internalDoImport() {
		fileCount = 0;

		String[] chosen = null;
		if (selected.isDirectory() && ((VPTDirectory)selected).getFile().exists()) {
			chosen = GUIUtilities.showVFSFileDialog(viewer.getView(), selected.getNodePath() + "/",
							VFSBrowser.OPEN_DIALOG, true);
		} else {
			chosen = GUIUtilities.showVFSFileDialog(viewer.getView(), project.getRootPath() + "/",
							VFSBrowser.OPEN_DIALOG, true);
		}

		if (chosen == null || chosen.length == 0) {
			return null;
		}

		ArrayList lst = new ArrayList();

		boolean asked = false, recurse = false;
		for (int i = 0; i < chosen.length; i++) {
			VPTNode node = null;

			if (VFSManager.getVFSForPath(chosen[i]) == VFSManager.getFileVFS()) {
				File f;
				if (MiscUtilities.isURL(chosen[i])) {
					String proto = MiscUtilities.getProtocolOfURL(chosen[i]);
					f = new File(chosen[i].substring(proto.length()));
				} else {
					f = new File(chosen[i]);
				}
				// it's a normal file
				node = findDirectory(f, selected, false);
				if (node == null) {
					VPTFile file = new VPTFile(f);
					registerFile(file);
					lst.add(file);
					fileCount++;
				}
			} else {
				node = new VFSFile(chosen[i]);
				lst.add(node);
				fileCount++;
			}

		}

		showFileCount();
		return lst;
	} //}}}

	//{{{ #showFileCount() : void
	/** Shows a message in the status bar indicating how many files were imported. */
	protected void showFileCount() {
		viewer.setStatus(
			jEdit.getProperty("projectviewer.import.msg_result",
				new Object[] { new Integer(fileCount) }));
	} //}}}

}

