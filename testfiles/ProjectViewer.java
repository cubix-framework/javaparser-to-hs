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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package projectviewer;

//{{{ Imports
import java.io.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import java.awt.BorderLayout;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;

import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.PluginJAR;
import org.gjt.sp.jedit.EditPlugin;
import static org.gjt.sp.jedit.EditBus.EBHandler;

import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.DockableWindowUpdate;
import org.gjt.sp.jedit.msg.DynamicMenuChanged;
import org.gjt.sp.jedit.msg.EditorExitRequested;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PluginUpdate;
import org.gjt.sp.jedit.msg.ViewUpdate;

import common.threads.WorkerThreadPool;

import errorlist.ErrorSource;
import errorlist.ErrorSourceUpdate;

import projectviewer.event.ProjectUpdate;
import projectviewer.event.StructureUpdate;
import projectviewer.event.ViewerUpdate;

import projectviewer.gui.ProjectComboBox;

import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTGroup;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTRoot;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.ProjectTreeModel;
import projectviewer.vpt.ProjectTreePanel;

import projectviewer.action.Action;
import projectviewer.action.ExpandAllAction;
import projectviewer.action.CollapseAllAction;
import projectviewer.action.EditProjectAction;
import projectviewer.action.FileImportAction;
import projectviewer.action.NodeRemoverAction;
import projectviewer.action.NodeRenamerAction;
import projectviewer.config.ProjectViewerConfig;
import projectviewer.importer.AutoReimporter;
import projectviewer.importer.NewFileImporter;
//}}}

/**
 *	Main GUI for the project viewer plugin.
 *
 *	@author		Marcelo Vanzin (with much code from original version)
 *	@version	$Id: ProjectViewer.java 16690 2009-12-21 01:22:31Z vanza $
 */
public final class ProjectViewer extends JPanel
	implements DefaultFocusComponent
{

	//{{{ Static members
	private static final ProjectViewerConfig config;
	private static final Map<View,ViewerEntry> viewers;

	static {
		config = ProjectViewerConfig.getInstance();
		viewers = new HashMap<View,ViewerEntry>();
	}

	//{{{ +_getViewer(View)_ : ProjectViewer
	/**
	 *	Returns the viewer associated with the given view, or null if none
	 *	exists.
	 */
	public static ProjectViewer getViewer(View view) {
		ViewerEntry ve = (ViewerEntry) viewers.get(view);
		return (ve != null) ? ve.dockable : null;
	} //}}}

	//{{{ Tree Changes Broadcast Methods

	//{{{ +_nodeStructureChanged(VPTNode)_ : void
	/**
	 *	Notify all project viewer instances of a change in a node's structure.
	 */
	public static void nodeStructureChanged(VPTNode node) {
		for (ViewerEntry ve : viewers.values()) {
			if (ve.dockable != null
				&& ve.dockable.getRoot().isNodeDescendant(node))
			{
				ve.dockable.getTreePanel().nodeStructureChanged(node);
			}
		}
	} //}}}

	//{{{ +_nodeChanged(VPTNode)_ : void
	/** Notify all project viewer instances of a change in a node. */
	public static void nodeChanged(VPTNode node) {
		for (ViewerEntry ve : viewers.values()) {
			if (ve.dockable != null
				&& ve.dockable.getRoot().isNodeDescendant(node))
			{
				ve.dockable.getTreePanel().nodeChanged(node);
				if (node == ve.dockable.getRoot()
					&& ve.dockable.pList != null)
				{
					// force a refresh of the "selected node" of the "combo"
					ve.dockable.pList.setSelectedNode(node);
				}
			}
		}
	} //}}}

	//{{{ +_insertNodeInto(VPTNode, VPTNode)_ : void
	/**
	 *	Inserts a node in the given parent node (in a sorted position according
	 *	to {@link projectviewer.vpt.VPTNode#findIndexForChild(VPTNode) } and
	 *	notifies folder trees in all instances of ProjectViewer.
	 */
	public static void insertNodeInto(VPTNode child, VPTNode parent) {
		int idx = parent.findIndexForChild(child);
		int[] indexes = new int[] { idx };

		parent.insert(child, idx);

		for (ViewerEntry ve : viewers.values() ) {
			if (ve.dockable != null
				&& ve.dockable.getRoot().isNodeDescendant(parent))
			{
				ve.dockable.getTreePanel().nodesWereInserted(parent, indexes);
			}
		}
	} //}}}

	//{{{ +_nodeStructureChangedFlat(VPTNode)_ : void
	/**
	 *	Notify all "flat trees" in any project viewer instances of a change in
	 *	a node's structure.
	 */
	public static void nodeStructureChangedFlat(VPTNode node) {
		for (ViewerEntry ve : viewers.values() ) {
			if (ve.dockable != null
				&& ve.dockable.getRoot().isNodeDescendant(node))
			{
				ve.dockable.getTreePanel().flatStructureChanged(node);
			}
		}
	} //}}}

	//{{{ +_removeNodeFromParent(VPTNode)_ : void
	/**
	 *	Removes a node from its parent, and notifies all folder trees in all
	 *	instances of ProjectViewer.
	 */
	public static void removeNodeFromParent(VPTNode child) {
		VPTNode parent = (VPTNode) child.getParent();
		int index = parent.getIndex(child);
		int[] idx = new int[] { index };
		Object[] removed = new Object[] { child };

		parent.remove(index);

		for (ViewerEntry ve : viewers.values() ) {
			if (ve.dockable != null
				&& ve.dockable.getRoot().isNodeDescendant(parent))
			{
				ve.dockable.getTreePanel().nodesWereRemoved(parent, idx, removed);
			}
		}
	} //}}}

	//{{{ +_projectRemoved(Object, VPTProject)_ : void
	/**
	 *	Notify all "flat trees" in any project viewer instances of a change in
	 *	a node's structure. Then, rebuild the project combo boxes.
	 *
	 *	@since PV 3.0.0
	 */
	public static void projectRemoved(VPTProject p) {
		VPTNode parent = (VPTNode) p.getParent();
		int index = parent.getIndex(p);
		int[] idx = new int[] { index };
		Object[] removed = new Object[] { p };

		parent.remove(index);

		for (ViewerEntry ve : viewers.values() ) {
			if (ve.dockable == null) {
				continue;
			}

			if (p == ve.dockable.getRoot()) {
				ve.dockable.setRootNode(VPTRoot.getInstance());
				continue;
			}

			if (ve.dockable.getRoot().isNodeDescendant(parent)) {
				ve.dockable.getTreePanel().nodesWereRemoved(parent, idx, removed);
			}
		}
	} //}}}

	//}}}

	//{{{ +_setActiveNode(View, VPTNode)_ : void
	/**
	 *	Sets the current active node for the view. If a viewer is
	 *	available for the given view, the root node of the viewer
	 *	is also changed.
	 *
	 *	@throws IllegalArgumentException If node is not a project or group.
	 *	@since PV 2.1.0
	 */
	public static void setActiveNode(View aView, VPTNode n) {
		if (!n.isGroup() && !n.isProject()) {
			throw new IllegalArgumentException("PV can only use Projects and Groups as root.");
		}

		ViewerEntry ve = (ViewerEntry) viewers.get(aView);
		if (ve == null) {
			ve = new ViewerEntry();
			ve.node = n;
			viewers.put(aView, ve);
		} else {
			if (n == ve.node)
				return;
			if (ve.dockable != null) {
				ve.dockable.setRootNode(n);
			} else {
				ve.node = n;
				modifyViewTitle(aView, n);
			}
		}

		if (ve.dockable == null) {
			// Loads projects if not yet loaded
			if (n.isProject()
					&& !ProjectManager.getInstance().isLoaded(n.getName())) {
				ProjectManager.getInstance().getProject(n.getName());
			}

			// Fires events if the dockable is not available
			// (setRootNode() fires events when the dockable is available)
			ViewerUpdate upd = new ViewerUpdate(aView, n,
				ViewerUpdate.Type.PROJECT_LOADED);
			EditBus.send(upd);
		} else if (!ve.dockable.isLoadingProject) {
			ve.dockable.setRootNode(n);
		}

	} //}}}

	//{{{ +_getActiveNode(View)_ : VPTNode
	/**
	 *	Return the current "active" node (opened project/group)
	 *	for the view. Returns null if no active node is known for the view.
	 *
	 *	@since	PV 2.1.0
	 */
	public static VPTNode getActiveNode(View aView) {
		if (aView == null) {
			aView = jEdit.getActiveView();
		}

		if (aView == null) {
			// cowardly bail out when jEdit is starting up.
			return null;
		}

		ViewerEntry ve = (ViewerEntry) viewers.get(aView);
		if (ve == null) {
			setActiveNode(aView, config.getLastNode());
			ve = (ViewerEntry) viewers.get(aView);
		}
		if (ve.dockable != null) {
			ve.dockable.waitForLoadLock();
		}

		return ve.node;
	} //}}}

	//{{{ -_modifyViewTitle(View, VPTNode)_ : void
	/**
	 *	Mofifies the title of a jEdit view, adding information about
	 *	the given node at the end of the current string.
	 */
	private static void modifyViewTitle(final View view, final VPTNode info) {
		// (info == null) might happen during jEdit startup.
		if (info != null
			&& config.getShowProjectInTitle())
		{
			view.updateTitle();
			StringBuffer title = new StringBuffer(view.getTitle());
			title.append(" [");
			if (info.isGroup()) {
				title.append("Group: ");
			} else {
				title.append("Project: ");
			}
			title.append(info.getName()).append(']');
			view.setTitle(title.toString());
		}
	} //}}}

	//{{{ +_getActiveProject(View)_ : VPTProject
	/**
	 *	Return the current active project for the view. If no project is
	 *	active, return null.
	 *
	 *	@since	PV 2.1.0
	 */
	public static VPTProject getActiveProject(View aView) {
		VPTNode n = getActiveNode(aView);
		return (n != null && n.isProject()) ? (VPTProject) n : null;
	} //}}}

	//{{{ #_cleanViewEntry(View)_ : void
	/**
	 *	Removes the "viewer entry" related to the given view. Called
	 *	by the ProjectPlugin class when a view closed message is
	 *	received.
	 */
	protected static void cleanViewEntry(View aView) {
		viewers.remove(aView);
	} //}}}

	//}}}

	//{{{ Constants

	private final static String TREE_STATE_PROP = "projectviewer.folder_tree_state";

	//}}}

	//{{{ Attributes
	private View					view;
	private HashSet<String>			dontAsk;

	private JPanel					topPane;
	private ProjectTreePanel		treePanel;
	private ProjectComboBox			pList;

	private VPTNode					treeRoot;
	private ConfigChangeListener	ccl;
	private Timer					reimporter;
	private Helper					errorListHelper;

	private boolean					isChangingBuffers;
	private boolean					isClosingProject;
	private volatile boolean		isLoadingProject;
	private volatile boolean		noTitleUpdate;
	//}}}

	//{{{ +ProjectViewer(View) : <init>
	/**
	 *	Create a new <code>ProjectViewer</code>. Only one instance is allowed
	 *	per view.
	 *
	 *	@param	aView  The jEdit view where the viewer is to be created.
	 *	@throws UnsupportedOperationException	If a viewer is already instantiated
	 *											for the given view.
`	 */
	public ProjectViewer(View aView) {
		ProjectViewer existant = getViewer(aView);
		if (existant != null) {
			throw new UnsupportedOperationException(
				jEdit.getProperty("projectviewer.error.multiple_views"));
		}

		setLayout(new BorderLayout());
		view = aView;
		treeRoot = VPTRoot.getInstance();
		isLoadingProject = false;

		buildGUI();

		ccl = new ConfigChangeListener();
		config.addPropertyChangeListener(ccl);

		// Register the dockable window in the viewer list
		ViewerEntry ve = (ViewerEntry) viewers.get(aView);
		if (ve == null) {
			ve = new ViewerEntry();
			ve.node = config.getLastNode();
			viewers.put(aView, ve);
		}
		ve.dockable = this;
		EditBus.addToBus(this);
		if (config.isErrorListAvailable()) {
			errorListHelper = new Helper();
			EditBus.addToBus(errorListHelper);
		}
		setRootNode(ve.node);
		isClosingProject = false;
		noTitleUpdate = false;
		setChangingBuffers(false);
	} //}}}

	//{{{ Private methods

	//{{{ -buildGUI() : void
	/** Builds the viewer GUI. */
	private void buildGUI() {
		pList = new ProjectComboBox(view);
		treePanel = new ProjectTreePanel(this);
		treePanel.loadGUI();
		add(BorderLayout.CENTER, treePanel);
		add(BorderLayout.NORTH, pList);
	} //}}}

	//{{{ -closeGroup(VPTGroup, VPTNode, boolean) : void
	private void closeGroup(VPTGroup group, VPTNode ignore, boolean ignoreClose) {
		for (int i = 0; i < group.getChildCount(); i++) {
			VPTNode child = (VPTNode) group.getChildAt(i);
			if (child != ignore) {
				if (child.isGroup()) {
					closeGroup((VPTGroup)child, ignore, ignoreClose);
				} else if (ProjectManager.getInstance().isLoaded(child.getName())){
					closeProject((VPTProject)child, ignoreClose);
				}
			}
		}
	} //}}}

	//{{{ -closeProject(VPTProject, boolean) : void
	/**
	 *	Closes a project: searches the open buffers for files related to the
	 *	given project and closes them (if desired) and/or saves them to the
	 *	open list (if desired).
	 */
	private void closeProject(VPTProject p, boolean ignoreClose) {
		setChangingBuffers(true);
		noTitleUpdate = true;
		isClosingProject = true;

		// check to see if project is active in some other viewer, so we
		// don't mess up that guy.
		if (config.getCloseFiles() && !ignoreClose) {
			for (Iterator it = viewers.values().iterator(); it.hasNext(); ) {
				ViewerEntry ve = (ViewerEntry) it.next();
				if (ve.dockable != this && ve.node.isNodeDescendant(p)) {
					noTitleUpdate = false;
					setChangingBuffers(false);
					isClosingProject = false;
					return;
				}
			}
		}

		// close files & populate "remember" list
		if ((config.getCloseFiles() && !ignoreClose) || config.getRememberOpen()) {
			Buffer[] bufs = jEdit.getBuffers();

			String currFile = null;
			if (p.getChildNode(view.getBuffer().getPath()) != null) {
				currFile = view.getBuffer().getPath();
			}

			for (int i = 0; i < bufs.length; i++) {
				if (p.getChildNode(bufs[i].getPath()) != null) {
					if (config.getRememberOpen() && !bufs[i].getPath().equals(currFile)) {
						p.addOpenFile(bufs[i].getPath());
					}
					if (config.getCloseFiles() && !ignoreClose) {
						jEdit.closeBuffer(view, bufs[i]);
					}
				}
			}

			if (config.getRememberOpen() && currFile != null) {
				p.addOpenFile(currFile);
			}
		}

		// saves the folder tree state
		String state = treePanel.getFolderTreeState(p);
		if (state != null) {
			p.setProperty(TREE_STATE_PROP, state);
		} else {
			p.removeProperty(TREE_STATE_PROP);
		}
		isClosingProject = false;
		noTitleUpdate = false;
		setChangingBuffers(false);

		// Stop the auto-reimporter.
		if (reimporter != null) {
			reimporter.stop();
			reimporter = null;
		}
	} //}}}

	//{{{ -openProject(VPTProject) : void
	/** Opens all the files that were previously opened in the project. */
	private void openProject(final VPTProject p) {
		setChangingBuffers(true);
		if (config.getRememberOpen()) {
			for (Iterator i = p.getOpenFiles().iterator(); i.hasNext(); ) {
				String next = (String) i.next();
				VPTNode f = p.getChildNode(next);
				if (f != null) {
					f.open();
				}
			}
		}
		p.clearOpenFiles();

		// loads tree state from the project, if saved
		final String state = p.getProperty(TREE_STATE_PROP);
		if (state != null && treePanel.getFolderTree() != null) {
			SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						treePanel.setFolderTreeState(p, state);
					}
				}
			);
		}
		setChangingBuffers(false);

		// setup the reimport timer.
		reimporter = AutoReimporter.create(p);
	} //}}}

	//{{{ -unloadInactiveProjects(VPTNode) : void
	/** Checks if some of the projects that are loaded can be unloaded. */
	private void unloadInactiveProjects(VPTNode newRoot) {
		List<String> active = null;
		for (Iterator i = viewers.values().iterator(); i.hasNext(); ) {
			ViewerEntry ve = (ViewerEntry) i.next();
			if (ve.node != null && ve.dockable != this) {
				if (active == null) {
					active = new ArrayList<String>();
				}
				if (ve.node.isProject()) {
					active.add(ve.node.getName());
				} else if (!ve.node.isRoot()) {
					addProjectsToList(ve.node, active);
				} else {
					return;
				}
			}
		}

		ProjectManager pm = ProjectManager.getInstance();
		for (VPTProject p : pm.getProjects()) {
			if (pm.isLoaded(p.getName()) && (p != newRoot)
				&& (active == null || !active.contains(p.getName())))
			{
				pm.unloadProject(p);
				getTreePanel().projectClosed(p);
			}
		}
	} //}}}

	//{{{ -addProjectsToList(VPTNode, List) : void
	private void addProjectsToList(VPTNode src, List<String> l) {
		for (int i = 0; i < src.getChildCount(); i++) {
			VPTNode n = (VPTNode) src.getChildAt(i);
			if (n.isProject()) {
				l.add(n.getName());
			} else {
				addProjectsToList(n, l);
			}
		}
	} //}}}

	//{{{ -waitForLoadLock() : void
	/**
	 *	If the isLoadingProject flag is true, wait until notified
	 *	by the thread that is loading the project that loading is
	 *	done. This is more effective than the old way of using a
	 *	few synchronized blocks here and there.
	 */
	private void waitForLoadLock() {
		if (isLoadingProject) {
			synchronized (this) {
				while (isLoadingProject) {
					try {
						this.wait();
					} catch (InterruptedException ie) {
						// ignore
					}
				}
			}
		}
	} //}}}

	//{{{ -unload() : void
	/**
	 *	Cleans up the current open project, and also clean up the
	 *	loaded project list when unloading PV from the current view.
	 */
	protected void unload() {
		EditBus.removeFromBus(this);
		if (errorListHelper != null) {
			EditBus.removeFromBus(errorListHelper);
		}
		if (treeRoot != null && treeRoot.isProject()) {
			closeProject((VPTProject)treeRoot, false);
		}
		unloadInactiveProjects(null);
		ViewerEntry ve = (ViewerEntry) viewers.get(view);
		if (ve != null) {
			ve.dockable = null;
		}
	} //}}}

	//}}}

	//{{{ Public Methods

	//{{{ +setStatus(String) : void
	/** Changes jEdit's status bar message for the current view. */
	public void setStatus(String message) {
		view.getStatus().setMessageAndClear(message);
	} //}}}

	//{{{ +getSelectedNode() : VPTNode
	/** Returns the currently selected node in the tree. */
	public VPTNode getSelectedNode() {
		JTree tree = getCurrentTree();
		if (tree != null && tree.getSelectionPath() != null) {
			return (VPTNode) tree.getSelectionPath().getLastPathComponent();
		} else {
			return null;
		}
	} //}}}

	//{{{ +getSelectedFilePaths() : ArrayList
	/**
	 *	Returns a list of Strings containing the file paths of the selected
	 *	file and folder nodes. This is mostly a utility method so other
	 *	plugins/macros can peform actions on a selection of files.
	 */
	public List getSelectedFilePaths() {
		List<String> obfp = new ArrayList<String>();
		JTree tree = getCurrentTree();
		if (tree == null)
			return null;

		if (tree.getSelectionPaths() != null) {
			TreePath[] paths= tree.getSelectionPaths();

			for (int i =0; i < paths.length; i++) {
				VPTNode nd = (VPTNode)paths[i].getLastPathComponent();
				if (nd.isFile()) {
					obfp.add(nd.getNodePath());
				}
			}
			return obfp;
		} else {
			return null;
		}
	} //}}}

	//{{{ +getCurrentTree() : JTree
	/** Returns the currently active tree. */
	public JTree getCurrentTree() {
		return treePanel.getCurrentTree();
	} //}}}

	//{{{ +getView() : View
	/** Returns the View associated with this instance. */
	public View getView() {
		return view;
	} //}}}

	//{{{ +setRootNode(VPTNode) : void
	/**
	 *	Sets the root node of the trees showm by this viewer. The current root
	 *	is cleaned up before setting the new root (e.g., project files are closed,
	 *	etc.)
	 *
	 *	@throws IllegalArgumentException If node is not a project or group.
	 *	@since PV 2.1.0
	 */
	public void setRootNode(VPTNode n) {
		if (n == null)
			n = VPTRoot.getInstance();
		if (n == treeRoot)
			return;

		waitForLoadLock();

		/*
		 * XXX: the locking when loading a project in an I/O thread is not
		 * enough. Since the AWT thread might be waiting for the lock to be
		 * released, the task has to release the lock before updating the
		 * tree (see ProjectLoader::run() below). If the AWT thread is, for
		 * any reason, running this method, it will cause a ClassCastException
		 * because the original tree model for the tree hasn't been restored
		 * yet. So add this ugly check here.
		 */
		if (!(getCurrentTree().getModel() instanceof ProjectTreeModel)) {
			return;
		}

		if (!n.isGroup() && !n.isProject()) {
			throw new IllegalArgumentException("PV can only use Projects and Groups as root.");
		}

		// close old project
		if (treeRoot != null && !n.isNodeDescendant(treeRoot)) {
			if (treeRoot.isProject()) {
				VPTProject p = (VPTProject) treeRoot;
				if (p.tryLock()) {
					try {
						closeProject(p, false);
					} finally {
						p.unlock();
					}
				} else {
					setStatus(jEdit.getProperty("projectviewer.error.project_locked"));
				}
			} else {
				closeGroup((VPTGroup)treeRoot, n, false);
			}
		}

		// try to release some memory.
		if (!n.isNodeDescendant(treeRoot)) {
			unloadInactiveProjects(n);
		}

		// set the new root
		ViewerEntry ve = (ViewerEntry) viewers.get(view);
		if (n.isProject()) {
			VPTProject p = (VPTProject) n;
			if (!ProjectManager.getInstance().isLoaded(p.getName())) {
				setEnabled(false);
				new ProjectLoader(p.getName()).loadProject();
				return;
			}
			openProject(p);
			if (ve.node != p) {
				sendUpdate(p, ViewerUpdate.Type.PROJECT_LOADED);
				ve.node = p;
			}
		} else if (n.isGroup()){
			if (ve.node != n) {
				sendUpdate(n, ViewerUpdate.Type.GROUP_ACTIVATED);
				ve.node = n;
			}
		}

		treeRoot = n;
		treePanel.setRoot(treeRoot);

		dontAsk = null;
		ProjectManager.getInstance().fireDynamicMenuChange();
		pList.setSelectedNode(treeRoot);
		modifyViewTitle(view, treeRoot);
	} //}}}

	//{{{ +getRoot() : VPTNode
	/** Returns the root node of the current tree. */
	public VPTNode getRoot() {
		waitForLoadLock();
		return treeRoot;
	} //}}}

	//{{{ +setEnabled(boolean) : void
	/** Enables or disables the viewer GUI. */
	public void setEnabled(boolean flag) {
		treePanel.setEnabled(flag);
		pList.setEnabled(flag);
		super.setEnabled(flag);
	} //}}}

	//{{{ +setChangingBuffers(boolean) : void
	/**
	 *	Method intended for use by classes that manage clicks on the
	 *	project trees to open buffers in jEdit; by setting this flag
	 *	to true, the auto-selecting of the new active buffer in jEdit
	 *	is temporarily disabled, preventing the tree from shifting
	 *	around when the user is interacting with it.
	 *
	 *	@since	PV 2.1.1
	 */
	public void setChangingBuffers(boolean flag) {
		isChangingBuffers = flag;
	} //}}}

	/**
	 *	Returns the component that manages the trees shown in the UI.
	 *
	 *	@since PV 3.0.0
	 */
	public ProjectTreePanel getTreePanel()
	{
		return treePanel;
	}

	/**
	 *	Sends a project viewer update message with the given data.
	 *
	 *	@since PV 3.0.0
	 */
	public void sendUpdate(VPTNode n, ViewerUpdate.Type type)
	{
		ViewerUpdate update = new ViewerUpdate(this, n, type);
		EditBus.send(update);
	}

	//}}}

	//{{{ Message handling

	@EBHandler
	public void handleDockableWindowUpdate(DockableWindowUpdate msg)
	{
		if (msg.getWhat() == DockableWindowUpdate.DEACTIVATED ||
			msg.getWhat() == DockableWindowUpdate.PROPERTIES_CHANGED) {
			if (!view.getDockableWindowManager()
					.isDockableWindowDocked("projectviewer")) {
				config.setLastNode(treeRoot);
				unload();
				viewers.remove(view);
			}
		}
	}


	@EBHandler
	public void handleDynamicMenuChanged(DynamicMenuChanged dmg)
	{
		if (dmg.getMenuName().equals("plugin.projectviewer.ProjectPlugin.menu")) {
			pList.updateMenu();
		}
	}


	@EBHandler
	public void handleEditorExitRequested(EditorExitRequested msg)
	{
		if (treeRoot != null) {
			if (jEdit.getActiveView() != view) {
				config.setLastNode(treeRoot);
			}
			if (treeRoot.isGroup()) {
				closeGroup((VPTGroup)treeRoot, null, true);
			} else {
				closeProject((VPTProject)treeRoot, true);
			}
		}
	}


	/**
	 * Handles a ViewUpdate EditBus message. Checks only whether
	 * the EditPane was changed, and focus the file corresponding
	 * to the buffer on the EditPane on the PV tree.
	 */
	@EBHandler
	public void handleViewUpdate(ViewUpdate vu)
	{
		if (vu.getView() == view &&
			vu.getWhat() == ViewUpdate.EDIT_PANE_CHANGED &&
			config.getFollowCurrentBuffer())
		{
			PVActions.focusActiveBuffer(view, treeRoot);
		}
		if (vu.getWhat() == ViewUpdate.CLOSED) {
			if (vu.getView() == view) {
				config.setLastNode(treeRoot);
				unload();
			}
			viewers.remove(vu.getView());
		}
	}


	@EBHandler
	public void handleBufferUpdate(BufferUpdate msg)
	{
		handleBufferUpdateMessage(msg, treeRoot);
	}


	@EBHandler
	public void handleEditPaneUpdate(EditPaneUpdate msg)
	{
		if (msg.getWhat() == EditPaneUpdate.BUFFER_CHANGED
			&& config.getFollowCurrentBuffer()
			&& msg.getEditPane().getView() == view
			&& !isChangingBuffers)
		{
			PVActions.focusActiveBuffer(view, treeRoot);
			modifyViewTitle(view, treeRoot);
		}
	}


	/** Resets the auto-reimporter task. */
	@EBHandler
	public void handleProjectUpdate(ProjectUpdate msg)
	{
		if (msg.getProject() == treeRoot) {
			switch (msg.getType()) {
			case PROPERTIES_CHANGED:
				if (reimporter != null) {
					reimporter.stop();
				}
				reimporter = AutoReimporter.create(msg.getProject());
				break;
			}
			treePanel.reloadIconComposer(msg.getProject());
			repaint();
		}
	}

	//{{{ -handleBufferUpdateMessage(BufferUpdate, VPTNode) : boolean
	private void handleBufferUpdateMessage(BufferUpdate bu,
										   VPTNode where)
	{
		if (bu.getView() != null && bu.getView() != view) return;

		if (bu.getWhat() == BufferUpdate.SAVED) {
			if (where == null || !where.isProject())
				return;

			VPTProject p = (VPTProject) treeRoot;
			VPTNode f = p.getChildNode(bu.getBuffer().getPath());
			if (f != null)
				return;

			boolean ask = false;
			if (config.getAskImport() != ProjectViewerConfig.ASK_NEVER &&
				(config.getAskImport() == ProjectViewerConfig.ASK_ALWAYS ||
				 dontAsk == null || !dontAsk.contains(bu.getBuffer().getPath()))) {
				VFS vfs = VFSManager.getVFSForPath(bu.getBuffer().getPath());
				String fileParentPath = vfs.getParentOfPath(bu.getBuffer().getPath());
				String projectRootPath = p.getRootPath();
				ask = fileParentPath.startsWith(p.getRootPath()) &&
					  fileParentPath.length() > p.getRootPath().length() &&
					  fileParentPath.charAt(p.getRootPath().length()) == vfs.getFileSeparator();
			}

			// Try to import newly created files to the project
			if (ask) {
				int res = JOptionPane.YES_OPTION;
				JCheckBox cBox = null;
				if (config.getAskImport() != ProjectViewerConfig.AUTO_IMPORT) {
					JPanel panel = new JPanel();
					BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
					panel.setLayout(bl);

					JLabel msg = new JLabel(
						jEdit.getProperty("projectviewer.import_new",
							new Object[] { bu.getBuffer().getName(), p.getName() }));
					cBox = new JCheckBox(jEdit.getProperty("projectviewer.import_always_cb"));
					cBox.setSelected(false);
					panel.add(msg);
					panel.add(cBox);

					res = JOptionPane.showConfirmDialog(view,
							panel,
							jEdit.getProperty("projectviewer.import_new.title"),
							JOptionPane.YES_NO_OPTION);
				}

				if (res == JOptionPane.YES_OPTION) {
					new NewFileImporter(p, this, bu.getBuffer().getPath()).doImport();

					if (cBox != null && cBox.isSelected()) {
						config.setAskImport(ProjectViewerConfig.AUTO_IMPORT);
						JPanel panel = new JPanel();
						BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
						panel.setLayout(bl);
						panel.add(new JLabel(jEdit.getProperty("projectviewer.import_always_disable.1")));
						panel.add(new JLabel(jEdit.getProperty("projectviewer.import_always_disable.2")));
						JOptionPane.showMessageDialog(view, panel,
							jEdit.getProperty("projectviewer.import_new.title"),
							JOptionPane.INFORMATION_MESSAGE);
					}
				} else if (config.getAskImport() == ProjectViewerConfig.ASK_ONCE) {
					if (dontAsk == null) {
						dontAsk = new HashSet<String>();
					}
					dontAsk.add(bu.getBuffer().getPath());
				}
			}
		}

		// Notifies trees when a buffer is closed (so it should not be
		// underlined anymore) or opened (should underline it).
		if ((bu.getWhat() == BufferUpdate.CLOSED
			 || bu.getWhat() == BufferUpdate.LOADED
			 || bu.getWhat() == BufferUpdate.DIRTY_CHANGED)
		) {
			if (!noTitleUpdate)
				modifyViewTitle(view, treeRoot);
			if (where != null && where.isProject()) {
				VPTNode f = ((VPTProject)where).getChildNode(bu.getBuffer().getPath());
				if (f == null &&
					!bu.getBuffer().isUntitled() &&
					config.getShowAllWorkingFiles()) {
					f = new VPTFile(bu.getBuffer().getPath());
				}
				if (f != null) {
					if (bu.getWhat() == BufferUpdate.CLOSED) {
						getTreePanel().projectFileClosed(f);
					} else if (bu.getWhat() == BufferUpdate.LOADED) {
						getTreePanel().projectFileOpened(f);
					}

					if (!isClosingProject) {
						if (bu.getWhat() == BufferUpdate.CLOSED) {
							((VPTProject)where).removeOpenFile(bu.getBuffer().getPath());
						}
						ProjectViewer.nodeChanged(f);
					}
				}
			} else if (where != null) {
				for (int i = 0; i < where.getChildCount(); i++) {
					handleBufferUpdateMessage(bu, (VPTNode)where.getChildAt(i));
				}
			}
		}
	} //}}}

	//}}}

	//{{{ -class ConfigChangeListener
	/** Listens for changes in the PV configuration. */
	private class ConfigChangeListener implements PropertyChangeListener, Runnable {

		private boolean willRun = false;

		//{{{ +propertyChange(PropertyChangeEvent) : void
		/** Listens for property change events in the plugin's configuration.
		 *	Shows/Hides the trees, according to the user's wish.
		 *
		 * @param  evt	Description of Parameter
		 */
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals(ProjectViewerConfig.ASK_IMPORT_OPT)) {
				int opt = ((Integer)evt.getNewValue()).intValue();
				if (opt == ProjectViewerConfig.ASK_NEVER ||
						opt == ProjectViewerConfig.ASK_ONCE) {
					dontAsk = null;
				}
				return;
			}

			if (evt.getPropertyName().equals(ProjectViewerConfig.CASE_INSENSITIVE_SORT_OPT)) {
				treeRoot.sortChildren(true);
				String state = treePanel.getFolderTreeState(treeRoot);
				nodeStructureChanged(treeRoot);
				nodeStructureChangedFlat(treeRoot);
				treePanel.setFolderTreeState(treeRoot, state);
				repaint();
				return;
			}

			if (evt.getPropertyName().equals(ProjectViewerConfig.SHOW_FILES_OPT) ||
					evt.getPropertyName().equals(ProjectViewerConfig.SHOW_WFILES_OPT) ||
					evt.getPropertyName().equals(ProjectViewerConfig.SHOW_ALLWFILES_OPT) ||
					evt.getPropertyName().equals(ProjectViewerConfig.SHOW_FOLDERS_OPT)||
					evt.getPropertyName().equals(ProjectViewerConfig.SHOW_COMPACT_OPT) ||
					evt.getPropertyName().equals(ProjectViewerConfig.SHOW_FILTERED_OPT))
			{
				if (!willRun) {
					SwingUtilities.invokeLater(this);
					willRun = true;
				}
				return;
			}

			if (evt.getPropertyName().equals(ProjectViewerConfig.USER_CONTEXT_MENU) ||
				evt.getPropertyName().equals(ProjectViewerConfig.USER_MENU_FIRST))
			{
				treePanel.getContextMenu().userMenuChanged();
				return;
			}

		}//}}}

		//{{{ +run() : void
		/** "Run" method, called by the Swing runtime after a config option for one
		 *	or more of the trees has changed.
		 */
		public void run() {
			treePanel.loadGUI();
			ProjectViewer.this.revalidate();
			willRun = false;
		}//}}}

	} //}}}

	//{{{ -class ProjectLoader
	/** Loads a project in the background. */
	private class ProjectLoader implements Runnable {

		private String pName;
		private final JTree tree;
		private final DefaultTreeModel tModel;

		//{{{ +ProjectLoader(String) : <init>
		public ProjectLoader(String pName) {
			this.pName = pName;
			this.tree = getCurrentTree();
			this.tModel = (tree != null)
					? (DefaultTreeModel) tree.getModel() : null;
		} //}}}

		//{{{ +loadProject() : void
		public void loadProject() {
			// This method is called in the AWT Thread, so do some of the
			// processing here before we start the thread itself.
			treeRoot = null;
			setEnabled(false);
			if (tree != null) {
				tree.setModel(new DefaultTreeModel(
					new DefaultMutableTreeNode(
						jEdit.getProperty("projectviewer.loading_project",
							new Object[] { pName } ))));
			} else {
				setStatus(jEdit.getProperty("projectviewer.loading_project",
							new Object[] { pName } ));
			}

			isLoadingProject = true;

			WorkerThreadPool.getSharedInstance().addRequest(this);
		} //}}}

		//{{{ +run() : void
		public void run() {
			final VPTProject p;
			p = ProjectManager.getInstance().getProject(pName);

			synchronized (ProjectViewer.this) {
				isLoadingProject = false;
				ProjectViewer.this.notifyAll();
			}

			SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						if (tree != null) {
							tModel.setRoot(p);
							tree.setModel(tModel);
						}
						setRootNode(p);
						setEnabled(true);
					}
				}
			);
		} //}}}

	} //}}}

	//{{{ -class _ViewerEntry_
	/**
	 *	Holds information about what's active on what view, to allow active
	 *	nodes even with no dockable open.
	 *
	 *	@since	PV 2.1.0
	 */
	private static class ViewerEntry {
		public ProjectViewer dockable;
		public VPTNode node;
	} //}}}

	//{{{ -class Helper
	/**
	 *	Class to hold methods that require classes that may not be available,
	 *	so that PV behaves well when called from a BeanShell script.
	 */
	public class Helper {

		@EBHandler
		public void handleErrorSourceUpdate(ErrorSourceUpdate esu)
		{
			if (esu.getWhat() == ErrorSourceUpdate.ERROR_ADDED
				|| esu.getWhat() == ErrorSourceUpdate.ERROR_REMOVED)
			{
				VPTProject p = (VPTProject) treeRoot;
				ErrorSource.Error error = esu.getError();
				VPTNode f = p.getChildNode(error.getFilePath());
				if (f != null) {
					treePanel.nodeChanged(f);
				}
			}
			if (esu.getWhat() == ErrorSourceUpdate.ERROR_SOURCE_ADDED
				|| esu.getWhat() == ErrorSourceUpdate.ERROR_SOURCE_REMOVED
				|| esu.getWhat() == ErrorSourceUpdate.ERRORS_CLEARED)
			{
				treePanel.repaint();
			}
		}

	} //}}}

	public void focusOnDefaultComponent()
	{
		getCurrentTree().requestFocus();

	}

}

