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
import java.awt.event.ActionEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;

import projectviewer.ProjectViewer;
import projectviewer.ProjectManager;

import projectviewer.event.StructureUpdate;

import projectviewer.vpt.VPTGroup;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTRoot;

import projectviewer.config.ProjectOptions;
import projectviewer.config.ProjectViewerConfig;
import projectviewer.importer.RootImporter;
//}}}

/**
 *	Action that when executed creates a new project or edits an existing one.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: EditProjectAction.java 16946 2010-01-18 00:24:55Z vanza $
 */
public class EditProjectAction extends Action {

	//{{{ Private members
	private boolean forceNew;
	private VPTGroup parent;
	//}}}

	//{{{ Constructors

	/** Default constructor. */
	public EditProjectAction() {
		this(false, null);
	}

	/**
	 *	If forceNew is true, creation of new project will be forced. The
	 *	default parent is only allowed to be set if forceNew is true.
	 */
	public EditProjectAction(boolean forceNew, VPTGroup parent) {
		super("projectviewer_wrapper_edit");
		this.forceNew = forceNew;
		if (forceNew)
			this.parent = parent;
	}

	//}}}

	//{{{ getText() method
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return jEdit.getProperty("projectviewer.action.a_e_project");
	} //}}}

	//{{{ actionPerformed(ActionEvent) method
	/** Creates a new project. */
	public void actionPerformed(ActionEvent e) {
		VPTNode selected = null;
		VPTProject proj = null;
		VPTProject lockedProj = null;
		String oldName = null;
		String oldRoot = null;

		if (viewer == null) {
			viewer = ProjectViewer.getViewer(jEdit.getActiveView());
		}
		if (viewer != null && parent == null) {
			VPTNode sel = viewer.getSelectedNode();
			while (sel != null && !sel.isGroup()) {
				sel = (VPTNode) sel.getParent();
			}
			if (sel == null) {
				parent = VPTRoot.getInstance();
			} else {
				parent = (VPTGroup) sel;
			}
		}

		if (!forceNew) {
			if (viewer != null) {
				selected = viewer.getSelectedNode();
				if (selected == null) {
					selected = viewer.getRoot();
				}
				if (selected.isGroup()) {
					parent = (VPTGroup) selected;
				}
				proj = VPTNode.findProjectFor(selected);
			} else {
				proj = ProjectViewer.getActiveProject(jEdit.getActiveView());
			}
			if (proj != null
				&& !ProjectManager.getInstance().isLoaded(proj.getName()))
			{
				proj = ProjectManager.getInstance().getProject(proj.getName());
			}
			if (proj != null) {
				oldName = proj.getName();
				oldRoot = proj.getRootPath();
			}
		}

		boolean add = forceNew || (proj == null);
		VPTGroup oldParent = (proj != null) ? ((VPTGroup) proj.getParent())
											: null;

		if (!add && !proj.tryLock()) {
			viewer.setStatus(jEdit.getProperty("projectviewer.error.project_locked"));
			return;
		}
		lockedProj = proj;

		try {
			proj = ProjectOptions.run(proj, proj == null, parent);

			if (proj != null) {
				if (add) {
					ProjectManager.getInstance().addProject(proj, parent);
					ProjectViewer.setActiveNode(jEdit.getActiveView(), proj);
					RootImporter ipi = new RootImporter(proj, null, viewer, jEdit.getActiveView());
					ipi.doImport();
				} else {
					if (!proj.getName().equals(oldName)) {
						ProjectManager.getInstance().renameProject(oldName, proj.getName());
					}
					if (!proj.getRootPath().equals(oldRoot)) {
						RootImporter ipi;
						if (JOptionPane.showConfirmDialog(jEdit.getActiveView(),
								jEdit.getProperty("projectviewer.action.clean_old_root"),
								jEdit.getProperty("projectviewer.action.clean_old_root.title"),
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							ipi = new RootImporter(proj, oldRoot, viewer, jEdit.getActiveView());
						} else {
							ipi = new RootImporter(proj, null, viewer, jEdit.getActiveView());
						}
						ipi.doImport();
					}
					ProjectManager.getInstance().saveProject(proj);
					proj.firePropertiesChanged();

					boolean notify = true;
					if (viewer == null) {
						viewer = ProjectViewer.getViewer(jEdit.getActiveView());
						notify = viewer.getRoot().isNodeDescendant(proj);
					}

					if (proj.getParent() != oldParent) {
						VPTGroup newParent = (VPTGroup) proj.getParent();
						proj.setParent(oldParent);
						ProjectViewer.removeNodeFromParent(proj);
						ProjectViewer.insertNodeInto(proj, newParent);
						ProjectManager.getInstance().saveProjectList();
						StructureUpdate.send(proj, oldParent);
					} else if (notify) {
						ProjectViewer.nodeChanged(proj);
					}
					ProjectManager.getInstance().fireDynamicMenuChange();
				}
			}
		} finally {
			lockedProj.unlock();
		}
	} //}}}

	//{{{ prepareForNode(VPTNode) method
	/** Enable action only for the root node. */
	public void prepareForNode(VPTNode node) {
		if (forceNew || (node != null && (node.isGroup() || node.isProject()))) {
			cmItem.setVisible(true);
			((JMenuItem)cmItem).setText( (forceNew || node.isGroup()) ?
				jEdit.getProperty("projectviewer.action.add_project") :
				jEdit.getProperty("projectviewer.action.edit_project"));
		} else {
			cmItem.setVisible(false);
		}
	} //}}}

}

