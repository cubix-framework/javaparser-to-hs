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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.awt.event.ActionEvent;

import javax.swing.JTree;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;

import projectviewer.ProjectViewer;
import projectviewer.ProjectManager;

import projectviewer.event.StructureUpdate;

import projectviewer.vpt.VPTDirectory;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTGroup;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTRoot;
//}}}

/**
 *	Action that when executed removes nodes from the trees.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: NodeRemoverAction.java 13015 2008-07-07 01:40:27Z vanza $
 */
public class NodeRemoverAction extends Action {

	//{{{ Constants
    private final static int FILE  = 1;
    private final static int DIR   = 2;
    private final static int PROJ  = 3;
    private final static int MULTI = 4;
	//}}}

	//{{{ +NodeRemoverAction(boolean) : <init>
	/**
	 *	Cretes a new node remover actions. Delete defines what "remove" means:
	 *	"false" means it only removes the node from the tree, "true" means it
	 *	will try to delete it. Deletion is only available for file nodes at
	 *	this moment.
	 */
	public NodeRemoverAction(boolean delete) {
		this.delete = delete;
	} //}}}

	//{{{ Instance variables
	private Map<VPTProject, List<VPTFile>> removedFiles;
	private Set<VPTNode> changed;
	private boolean delete;
	//}}}

	//{{{ +getText() : String
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return (delete) ? jEdit.getProperty("projectviewer.action.delete")
						: null;
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Try to remove nodes from the project, asking when necessary. */
	public void actionPerformed(ActionEvent e) {
		int idx = 0;
		JTree tree = viewer.getCurrentTree();
		List<VPTProject> projects = new ArrayList<VPTProject>();
		List<VPTNode> nodes = new ArrayList<VPTNode>();
		changed = new HashSet<VPTNode>();

        switch (tree.getSelectionCount()) {
            case 0:
                // No Selecion! Shouldn't happen, but, just in case...
                return;

            case 1: {
                // Single selection!
				nodes.add((VPTNode)tree.getLastSelectedPathComponent());
                break;
            }

            default:
                // Any other thing == multiple selection
                if (confirmAction(MULTI)) {
					getSelectedArtifacts(tree.getSelectionPaths(), nodes);
                }
        }

		// Find all projets related to the nodes being removed.
		for (VPTNode n : nodes) {
			if (!n.isGroup()) {
				VPTProject p = VPTNode.findProjectFor(n);
				if (!projects.contains(p)) {
					projects.add(p);
				}
			}
		}

		try {
			// First, make sure we have the locks for all project
			// being modified.
			for (idx = 0; idx < projects.size(); idx++) {
				if (!projects.get(idx).tryLock()) {
					throw new IllegalStateException();
				}
			}

			// Remove the nodes.
			for (VPTNode n : nodes) {
				remove(n, nodes.size() == 1);
			}

			// Notify changes.
			List<VPTProject> notified = new ArrayList<VPTProject>();
			for (VPTNode n : changed) {
				if (n.isGroup()) {
					ProjectViewer.nodeStructureChangedFlat(n);
				} else {
					VPTProject p = VPTNode.findProjectFor(n);
					if (!notified.contains(p)) {
						ProjectViewer.nodeStructureChangedFlat(p);
						notified.add(p);
					}
				}
			}

			if (removedFiles != null)
			for (VPTProject p : removedFiles.keySet()) {
				List<VPTFile> lst = removedFiles.get(p);
				p.fireFilesChanged(null, lst);
			}
		} catch (IllegalStateException ise) {
			viewer.setStatus(jEdit.getProperty("projectviewer.error.project_locked"));
		} finally {
			// Unlock the projects that were locked.
			for (; idx > 0; idx--) {
				projects.get(idx - 1).unlock();
			}
		}

		// cleanup
		removedFiles = null;
		changed = null;
	} //}}}

	//{{{ +prepareForNode(VPTNode) : void
	/** Enable action only for non-root nodes. */
	public void prepareForNode(VPTNode node) {
		if (delete) {
			if (node == null) {
				// if only writable files are selected, enable the "delete"
				// menu item
				cmItem.setVisible(true);
				JTree tree = viewer.getCurrentTree();
				TreePath[] paths = tree.getSelectionPaths();
				for (int i = 0; i < paths.length; i++) {
					VPTNode n = (VPTNode) paths[i].getLastPathComponent();
					if (!n.isFile() || !((VPTFile)n).canWrite()) {
						cmItem.setVisible(false);
						break;
					}
				}
			} else {
				cmItem.setVisible(node.isFile() && node.canWrite());
			}
		} else {
			if (node == null) {
				JTree tree = viewer.getCurrentTree();
				TreePath[] paths = tree.getSelectionPaths();

				// do not enable if there's a group in the multiple selection
				boolean enable = true;
				for (int i = 0; i < paths.length; i++) {
					VPTNode n = (VPTNode) paths[i].getLastPathComponent();
					if (n.isGroup()) {
						enable = false;
						break;
					}
				}

				if (enable) {
					((JMenuItem)cmItem).setText(
						jEdit.getProperty("projectviewer.action.remove_multiple"));
				}
				cmItem.setVisible(enable);
			} else if (!node.isRoot()) {
				String text;
				if (node.isProject()) {
					text =  jEdit.getProperty("projectviewer.action.remove_project");
				} else if (node.isGroup()) {
					text = jEdit.getProperty("projectviewer.action.remove_group");
				} else {
					text = jEdit.getProperty("projectviewer.action.remove");
				}
				((JMenuItem)cmItem).setText(text);
				cmItem.setVisible(true);
			} else {
				cmItem.setVisible(false);
			}
		}
	} //}}}


    /**
     *  Receives a collection of TreePath objects and returns the underlying
     *  objects selected, removing a child when its parent has also been
     *  selected.
     */
    private void getSelectedArtifacts(TreePath[] paths,
									  List<VPTNode> nodes)
	{
        TreePath last = null;

        for (int i = 0; i < paths.length; i++) {
            if (last != null && !last.isDescendant(paths[i])) {
                last = null;
            }

            if (last == null) {
                last = paths[i];
                nodes.add((VPTNode) paths[i].getLastPathComponent());
            }
        }
    }


	//{{{ -confirmAction(int) : boolean
     /**
     *  Asks the user if he reeeeeally wants to delete the selection he's
     *  made.
     *
     *  @param  delete  If the selection will be deleted or only removed
     *                  from the project;
     *  @param  type    Type of object being removed. 1=File 2=Directory 3=Project
     *                  4=Multiple objects
     */
    private boolean confirmAction(int type) {
        String message = null;

        // Sanity check
        if (type > MULTI || type < FILE) return false;

        if (delete) {
            switch (type) {
                case FILE: // File
                    message = jEdit.getProperty("projectviewer.remove.confirm_file_del");
                    break;

                case DIR: // Directory
                    message = jEdit.getProperty("projectviewer.remove.confirm_dir_del");
                    break;

                case MULTI: // Multiple
                    message = jEdit.getProperty("projectviewer.remove.confim_multi_del");
                    break;

                default: // No other deletion is supported (yet?)
                    return false;
            }
        } else {
            switch(type) {
                case FILE: // File
                    message = null;
                    break;

                case DIR: // Directory
                    message = jEdit.getProperty("projectviewer.remove.confirm_dir_remove");
                    break;

                case PROJ: // Project
                    message = jEdit.getProperty("projectviewer.remove.confirm_project_remove");
                    break;

                case MULTI: // Multiple
                    message = jEdit.getProperty("projectviewer.remove.confirm_multi_remove");
                    break;

                default: // Won't happen, but...
            }
        }

        if (message == null) return true;

		return (JOptionPane.showConfirmDialog(viewer,
            		message,
					jEdit.getProperty("projectviewer.remove.confirm.title"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
    } //}}}

    //{{{ -remove(VPTNode, boolean) : void
    /**
     *  Removes an object from a project (or the project, if the object is one).
     *
     *  @param  o       The node.
     *  @param  delete  If deletion from disk should occur.
     *  @param  ask     Is we should ask for confirmation.
     */
    private void remove(VPTNode o, boolean ask) {
		boolean removed = false;

		if(o == null || o.isRoot()) {
			Log.log(Log.WARNING, this, "Removing: unexpected object o: " + o);
			return;
		}

		if (o.isProject()) {
            if (!ask || confirmAction(PROJ)) {
                ProjectManager.getInstance().removeProject((VPTProject)o);
				changed.add(VPTRoot.getInstance());
            }
        } else if (o.isGroup()) {
			if (o.getChildCount() > 0) {
				JOptionPane.showMessageDialog(
					viewer,
					jEdit.getProperty("projectviewer.action.remove_group.not_empty"),
					jEdit.getProperty("projectviewer.action.remove_group.not_empty.title"),
					JOptionPane.WARNING_MESSAGE
				);
			} else {
				ProjectViewer.removeNodeFromParent(o);
				StructureUpdate.send(o, StructureUpdate.Type.GROUP_REMOVED);
			}
		} else {
			VPTProject project = VPTNode.findProjectFor(o);

			if (o.isFile()) {
				if (!delete || (!ask || confirmAction(FILE))) {
					addRemovedFile((VPTFile)o);
					ProjectViewer.removeNodeFromParent(o);
					if (delete) o.delete();
					removed = true;
				}
			} else if (o.isDirectory()) {
				if (!ask || o.getChildCount() == 0 || confirmAction(DIR)) {
					unregisterFiles((VPTDirectory)o, project);
					ProjectViewer.removeNodeFromParent(o);
					removed = true;
				}
			} else {
				ProjectViewer.removeNodeFromParent(o);
			}

			ProjectManager.getInstance().saveProject(project);
			if (removed) changed.add(project);
		}

    } //}}}

	//{{{ -unregisterFiles(VPTDirectory, VPTProject) : void
	/**
	 *	Recursively unregisters the files below the given directory from the
	 *	project.
	 */
	private void unregisterFiles(VPTDirectory dir, VPTProject proj) {
		Enumeration e = dir.children();
		while (e.hasMoreElements()) {
			VPTNode n = (VPTNode) e.nextElement();
			if (n.isDirectory()) {
				unregisterFiles((VPTDirectory)n, proj);
			} else {
				proj.unregisterNodePath(n);
				if (n.isFile()) {
					addRemovedFile((VPTFile)n);
				}
			}
		}
	} //}}}

	//{{{ -addRemovedFile(VPTFile) : void
	private void addRemovedFile(VPTFile f) {
		VPTProject p = VPTProject.findProjectFor(f);
		if (removedFiles == null) {
			removedFiles = new HashMap<VPTProject, List<VPTFile>>();
		}
		List<VPTFile> lst = removedFiles.get(p);
		if (lst == null) {
			lst = new ArrayList<VPTFile>();
			removedFiles.put(p, lst);
		}
		lst.add(f);
	} //}}}

}

