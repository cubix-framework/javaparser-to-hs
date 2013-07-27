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
package projectviewer.action;

//{{{ Imports
import java.io.File;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import java.util.HashSet;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.EnhancedDialog;

import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSManager;

import common.gui.OkCancelButtons;

import projectviewer.ProjectViewer;
import projectviewer.ProjectManager;
import projectviewer.VFSHelper;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTDirectory;
//}}}

/**
 *	Action for renaming files, directories and projects.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: NodeRenamerAction.java 12353 2008-03-31 00:48:45Z vanza $
 */
public class NodeRenamerAction extends Action {

	//{{{ +NodeRenamerAction() : <init>
	public NodeRenamerAction() {
		super("projectviewer_wrapper_rename");
	} //}}}

	//{{{ +getText() : String
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return jEdit.getProperty("projectviewer.action.rename");
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Renames a node. */
	public void actionPerformed(ActionEvent e) {
		VPTNode node = viewer.getSelectedNode();
		boolean isValid = false;
		String newName = null;

		RenameDialog dlg = new RenameDialog(node);

		while (!isValid) {
			dlg.setVisible(true);
			newName = dlg.getInput();

			if (newName == null || newName.length() == 0) {
				return;
			}

			// checks the input
			if (node.isFile() || node.isDirectory()) {
				if (!dlg.getDontChangeDisk()) {
					if (!node.canWrite()
							|| (newName.indexOf('/') != -1 || newName.indexOf('\\') != -1)) {
						JOptionPane.showMessageDialog(viewer,
							jEdit.getProperty("projectviewer.action.rename.file_error"),
							jEdit.getProperty("projectviewer.action.rename.title"),
							JOptionPane.ERROR_MESSAGE);
					} else {
						isValid = true;
					}
				} else {
					isValid = true;
				}
			} else if (node.isProject()) {
				if (ProjectManager.getInstance().hasProject(newName)) {
					JOptionPane.showMessageDialog(viewer,
						jEdit.getProperty("projectviewer.project.options.name_exists"),
						jEdit.getProperty("projectviewer.action.rename.title"),
						JOptionPane.ERROR_MESSAGE);
				} else {
					isValid = true;
				}
			} else {
				isValid = dlg.getDontChangeDisk();
				if (!isValid) {
					GUIUtilities.error(viewer, "projectviewer.action.rename.cannot_rename", null);
				}
			}
		}

		VPTProject project = VPTNode.findProjectFor(node);

		if (dlg.getDontChangeDisk()) {
			node.setName(newName);
			reinsert(node, project);
			return;
		} else if (node.isFile()) {
			VPTFile f = (VPTFile) node;
			String oldURL = f.getURL();
			VFS vfs = VFSManager.getVFSForPath(oldURL);
			String url = vfs.constructPath(vfs.getParentOfPath(oldURL), newName);
			if (!renameFile(f, url, true)) {
				JOptionPane.showMessageDialog(viewer,
						jEdit.getProperty("projectviewer.action.rename.rename_error"),
						jEdit.getProperty("projectviewer.action.rename.title"),
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			project.unregisterNodePath(oldURL);
			reinsert(f, project);
		} else if (node.isDirectory() ) {
			VPTDirectory dir = (VPTDirectory) node;
			if (VFSHelper.pathExists(dir.getURL())) {
				VFS vfs = VFSManager.getVFSForPath(dir.getURL());
				Object session = null;
				String oldDir = dir.getURL();
				String newDir = vfs.constructPath(vfs.getParentOfPath(oldDir), newName);
				HashSet<VPTNode> openable;

				if (!vfsRename(vfs, oldDir, newDir)) {
					JOptionPane.showMessageDialog(viewer,
							jEdit.getProperty("projectviewer.action.rename.rename_error"),
							jEdit.getProperty("projectviewer.action.rename.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				dir.setURL(newDir);

				// updates all files from the old directory to point to the new one
				openable = new HashSet<VPTNode>(project.getOpenableNodes());
				for (VPTNode n : openable) {
					if (n.isFile() && n.getNodePath().startsWith(oldDir)) {
						String oldPath = n.getNodePath();
						String newPath = vfs.constructPath(newDir, n.getName());
						renameFile((VPTFile)n, newPath, false);
						project.unregisterNodePath(oldPath);
						project.registerNodePath(n);
					}
				}
			} else {
				dir.setName(newName);
			}
			reinsert(dir, project);
		} else if (node.isProject()) {
			String oldName = node.getName();
			node.setName(newName);
			ProjectManager.getInstance().renameProject(oldName, newName);
			ProjectViewer.nodeChanged(node);
			((VPTProject)node).firePropertiesChanged();
			viewer.repaint();
		}

		ProjectManager.getInstance().saveProject(project);
	} //}}}

	//{{{ +prepareForNode(VPTNode) : void
	/** Disable action only for the root node. */
	public void prepareForNode(VPTNode node) {
		boolean dirty = false;
		if (node != null && node.isFile()) {
			Buffer b = jEdit.getBuffer(node.getNodePath());
			if (b != null)
				dirty = b.isDirty();
		}
		cmItem.setVisible(!dirty && node != null && node.canRename());
	} //}}}

	//{{{ -renameFile(VPTFile, File) : boolean
	/**
	 *	Renames a file and tries not to mess up jEdit's current buffer.
	 *
	 *	@param f File node to rename.
	 *	@param url New URL for the file.
	 *	@param rename Whether to actually perform a VFS rename, or just
	 *				  change the node's URL.
	 *
	 *	@return Whether the operation was successful.
	 */
	private boolean renameFile(VPTFile f,
							   String url,
							   boolean rename) {
		boolean open = false;
		Buffer b = null;

		b = jEdit.getActiveView().getBuffer();
		if (b.getPath().equals(f.getURL())) {
			b = null;
		}
		open = f.isOpened();
		if (open) {
			f.close();
		}

		if (rename) {
			VFS vfs = VFSManager.getVFSForPath(f.getURL());
			if (!vfsRename(vfs, f.getURL(), url)) {
				return false;
			}
		}

		f.setURL(url);

		if (open) {
			// this is an ugly hack to avoid "file has been modified on
			// disk" warnings that shouldn't happen, but do.
			try { Thread.sleep(1); } catch (Exception e) { }
			f.open();
			if (b != null) {
				jEdit.getActiveView().setBuffer(b);
			}

		}
		return true;
	} //}}}

	//{{{ -reinsert(VPTNode, VPTProject) : void
	private void reinsert(VPTNode node, VPTProject proj) {
		VPTNode parent = (VPTNode) node.getParent();
		ProjectViewer.removeNodeFromParent(node);
		ProjectViewer.insertNodeInto(node, parent);
		if (node.canOpen()) {
			proj.registerNodePath(node);
		}
	} //}}}


	/**
	 *	Renames a file in the given VFS.
	 *
	 *	@param vfs VFS instance.
	 *	@param from Source URL.
	 *	@param to Target URL.
	 *
	 *	@return whether the operation was successful.
	 */
	private boolean vfsRename(VFS vfs,
							  String from,
							  String to)
	{
		Object session = null;
		try {
			session = VFSHelper.createVFSSession(vfs, from, viewer.getView());
			if (!vfs._rename(session, from, to, viewer.getView())) {
				return false;
			}
		} catch (java.io.IOException ioe) {
			return false;
		} finally {
			VFSHelper.endVFSSession(vfs, session, viewer.getView());
		}
		return true;
	}

	//{{{ -class RenameDialog
	/**
	 *	A dialog for renaming nodes. Provides an extra checkbox to allow
	 *	the user to rename the node but not the actual file/dir on disk,
	 *	in case the node is a file or a directory.
	 */
	private class RenameDialog extends EnhancedDialog {

		//{{{ Private Members
		private JTextField	fName;
		private JCheckBox	chFile;

		private boolean		okPressed;
		//}}}

		//{{{ +RenameDialog(VPTNode) : <init>
		public RenameDialog(VPTNode node) {
			super(JOptionPane.getFrameForComponent(viewer),
					jEdit.getProperty("projectviewer.action.rename.title"),
					true);

			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(BorderLayout.NORTH,
				new JLabel(jEdit.getProperty("projectviewer.action.rename.message")));

			// user input
			fName = new JTextField(node.getName(), 20);
			fName.setSelectionStart(0);
			fName.setSelectionEnd(node.getName().length());

			if (node.isProject() || !VFSHelper.pathExists(node.getNodePath())) {
				getContentPane().add(BorderLayout.CENTER, fName);
			} else {
				boolean canRename;
				JPanel p = new JPanel(new GridLayout(2, 1));
				p.add(fName);
				chFile = new JCheckBox(
					jEdit.getProperty("projectviewer.action.rename.dont_change_disk"),
					false);
				p.add(chFile);

				// default for not renaming node: if node is read only, if it's
				// not a file, or if it's a file and the node name doesn't match
				// the file's name.
				canRename = node.canRename();
				if (!canRename || !node.canWrite() || !node.isFile()
					|| !node.getName().equals(((VPTFile)node).getFile().getName()))
				{
					chFile.setSelected(true);
				}
				chFile.setEnabled(canRename);
				getContentPane().add(BorderLayout.CENTER, p);
			}

			// ok/cancel buttons
			getContentPane().add(BorderLayout.SOUTH, new OkCancelButtons(this));

			setLocationRelativeTo(viewer);
			pack();
		} //}}}

		//{{{ +setVisible(boolean) : void
		public void setVisible(boolean b) {
			if (b) okPressed = false;
			super.setVisible(b);
		} //}}}

		//{{{ +ok() : void
		/** Renames the file/dir. */
		public void ok() {
			okPressed = true;
			dispose();
		} //}}}

		//{{{ +cancel() : void
		/** Cancels renaming. */
		public void cancel() {
			dispose();
		} //}}}

		//{{{ +getInput() : String
		/**
		 *	Returns the user's input in case the "ok" button was pressed (or
		 *	the user hit enter), or null otherwise.
		 */
		public String getInput() {
			return (okPressed) ? fName.getText() : null;
		} //}}}

		//{{{ +getDontChangeDisk() : boolean
		/**
		 *	Returns whether the "do not change file name on disk" checkbox is
		 *	selected or not.
		 */
		public boolean getDontChangeDisk() {
			return (chFile != null) ? chFile.isSelected() : false;
		} //}}}

	} //}}}

}

