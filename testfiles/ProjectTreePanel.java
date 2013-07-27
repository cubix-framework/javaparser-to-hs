/*
 * :tabSize=4:indentSize=4:noTabs=true:
 * :folding=explicit:collapseFolds=1:
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
 */
package projectviewer.vpt;

//{{{ Imports
import java.awt.BorderLayout;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import java.awt.dnd.DragSource;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;

import java.awt.event.KeyEvent;

import java.io.File;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;

import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;

import projectviewer.ProjectManager;
import projectviewer.ProjectPlugin;
import projectviewer.ProjectViewer;
import projectviewer.VFSHelper;

import projectviewer.action.Action;
import projectviewer.action.NodeRemoverAction;
import projectviewer.action.NodeRenamerAction;
import projectviewer.action.ReimportAction;
import projectviewer.action.UpAction;

import projectviewer.config.ProjectViewerConfig;
import projectviewer.config.VersionControlService;
//}}}

/**
 * A panel that contains the several trees for showing project data.
 * Also provider methods for managing and interacting with the
 * trees.
 *
 * @author     Marcelo Vanzin
 * @version    $Id: ProjectTreePanel.java 16366 2009-10-20 07:26:31Z vanza $
 * @since      PV 3.0.0
 */
public class ProjectTreePanel extends JPanel
{

    //{{{ Constants
	private final static char NOT_EXPANDED		= '0';
	private final static char EXPANDED			= '1';

    private final static ProjectViewerConfig config = ProjectViewerConfig.getInstance();
    //}}}

    private JTabbedPane treePane;
    private JTree folderTree;
    private List<PVTree> trees;

    private ProjectViewer viewer;

    private VPTContextMenu contextMenu;
    private VPTSelectionListener selectionListener;

    private DragSource dragSource;
    private TreeDragListener dragListener;


    public ProjectTreePanel(ProjectViewer viewer)
    {
        super(new BorderLayout());
        this.viewer = viewer;

        trees = new ArrayList<PVTree>();
		contextMenu = new VPTContextMenu(viewer);
		selectionListener = new VPTSelectionListener(viewer);

		dragListener = new TreeDragListener();
		dragSource = new DragSource();
    }


    /**
     * Returns the currently active tree.
     */
    public JTree getCurrentTree()
    {

        switch (trees.size()) {
        case 0:
            return null;

        case 1:
            return trees.get(0);

        default:
            return trees.get(treePane.getSelectedIndex());
        }
    }


    /**
     * Removes all trees from the panel.
     */
    public void removeAll()
    {
        setRoot(null);
        trees.clear();
        treePane = null;
        folderTree = null;
        super.removeAll();
    }


    /**
	 * Loads the trees (folders, files, working files) into the view,
     * deciding what to show according to the configuration of the
     * plugin.
	 */
	public void loadGUI()
    {
        JScrollPane scroller;
		removeAll();

		if (config.getShowFoldersTree()) {
			folderTree = createTree(new VPTFoldersTreeModel(viewer.getRoot()),
                                    false);
        }

		if (config.getShowFilesTree()) {
			createTree(new VPTFileListModel(viewer.getRoot()),
                       true);
		}

		if (config.getShowWorkingFilesTree()) {
			createTree(new VPTWorkingFileListModel(viewer.getRoot()),
                       true);
		}

		if (config.getShowCompactTree()) {
            createTree(new VPTCompactModel(viewer.getRoot()),
                       false);
		}

		if (config.getShowFilteredTree()) {
            createTree(new VPTFilteredModel(viewer.getRoot()),
                       true);
		}

        if (trees.size() == 1) {
            treePane = null;
            add(BorderLayout.CENTER, new JScrollPane(trees.get(0)));
        } else if (treePane != null) {
            add(BorderLayout.CENTER, treePane);
        }
        revalidate();
	}


    public JTree getFolderTree()
    {
        return folderTree;
    }


    public int getTreeCount()
    {
        return trees.size();
    }

	/**
	 *	Returns a String representing the state of the folder tree.
	 *
	 *	@see	#setFolderTreeState(VPTNode, String)
	 *	@return The state of the tree, starting at the given node, or
	 *			null if the folderTree is not visible.
	 */
	public String getFolderTreeState(VPTNode node)
    {
		if (folderTree != null) {
			ProjectTreeModel model = (ProjectTreeModel) folderTree.getModel();
			int start = folderTree.getRowForPath(new TreePath(model.getPathToRoot(node)));
			if (start >= 0) {
				StringBuilder state = new StringBuilder();
				if(folderTree.isExpanded(start)) {
					for(int i = start; i < folderTree.getRowCount(); i++) {
						VPTNode n = (VPTNode) folderTree.getPathForRow(i)
										.getLastPathComponent();
						if (!node.isNodeDescendant(n))
							break;
						if (folderTree.isExpanded(i)) {
							state.append(EXPANDED);
						} else {
							state.append(NOT_EXPANDED);
						}
					}
				}
				return state.toString();
			}
		}
		return null;
	}


	/**
	 *	Sets the folder tree state from the given String.
	 *
	 *	@see	#getFolderTreeState(VPTNode)
	 */
	public void setFolderTreeState(VPTNode node, String state)
    {
		if (folderTree != null && state != null) {
			ProjectTreeModel model = (ProjectTreeModel) folderTree.getModel();
			int start = folderTree.getRowForPath(new TreePath(model.getPathToRoot(node)));
			for(int i = 0; i < state.length(); i++) {
				int row = start + i;
				if (row >= folderTree.getRowCount())
					break;

				TreePath path = folderTree.getPathForRow(row);
				if (path == null)
					return;

				VPTNode n = (VPTNode) path.getLastPathComponent();
				if (!node.isNodeDescendant(n))
					break;

				if (state.charAt(i) == EXPANDED) {
					folderTree.expandRow(row);
				}
			}
		}
	}

    /**
     *  Returns the context menu.
     */
    public VPTContextMenu getContextMenu()
    {
        return contextMenu;
    }

    //{{{ Tree notification methods

    /**
     * Sets the root node of all trees.
     */
    public void setRoot(VPTNode root)
    {
        if (root != null && root.isProject()) {
            reloadIconComposer((VPTProject)root);
        }
        for (JTree tree : trees) {
            VPTCellRenderer renderer = (VPTCellRenderer) tree.getCellRenderer();
            tree.setRowHeight(renderer.getRowHeight(root));
            ((ProjectTreeModel)tree.getModel()).setRoot(root);
        }
    }


    /**
     * Reload the icon composer for the trees when a project has
     * been editer.
     */
    public void reloadIconComposer(VPTProject proj)
    {
        VersionControlService svc = null;
        String svcname = ((VPTProject)proj).getProperty(VersionControlService.VC_SERVICE_KEY);
        if (svcname != null) {
            svc = (VersionControlService)
                ServiceManager.getService(VersionControlService.class.getName(),
                                          svcname);
        }

        IconComposer ic = new IconComposer(svc);
        for (JTree tree : trees) {
            VPTCellRenderer renderer = (VPTCellRenderer) tree.getCellRenderer();
            renderer.setIconComposer(ic);
        }
    }


    /**
	 *	Notify all trees of a change in a node's structure.
	 */
	public void nodeStructureChanged(VPTNode node)
    {
        VPTNode root = node.isGroup() ? node
                                      : VPTNode.findProjectFor(node);
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            if (model.isCustom()) {
                model.nodeStructureChanged(root);
            } else {
                model.nodeStructureChanged(node);
            }
        }
    }


    /**
	 * Notify all trees of a change in a node.
	 */
	public void nodeChanged(VPTNode node)
    {
        for (JTree tree : trees) {
            ((ProjectTreeModel)tree.getModel()).nodeChanged(node);
        }
        node.changed();
    }


    /**
	 * Notify trees of nodes being inserted into a parent.
	 */
	public void nodesWereInserted(VPTNode parent, int[] indexes)
    {
        VPTNode root = parent.isGroup() ? parent
                                        : VPTNode.findProjectFor(parent);
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            if (model.isCustom()) {
                model.nodeStructureChanged(root);
            } else {
                model.nodesWereInserted(parent, indexes);
            }
        }
    }


    /**
	 * Notify trees of nodes being removed from the parent.
	 */
	public void nodesWereRemoved(VPTNode parent,
                                 int[] indexes,
                                 Object[] removed)
    {
        VPTNode root = parent.isGroup() ? parent
                                        : VPTNode.findProjectFor(parent);
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            if (model.isCustom()) {
                model.nodeStructureChanged(root);
            } else {
                model.nodesWereRemoved(parent, indexes, removed);
            }
        }
    }


    /**
	 * Notify only the "flat" trees about a change in a node.
	 */
	public void flatStructureChanged(VPTNode node)
    {
        if (!node.isGroup()) {
            node = VPTNode.findProjectFor(node);
        }
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            if (model.isCustom()) {
                model.nodeStructureChanged(node);
            }
        }
    }


    /**
     * Notifies the trees that a project's file was opened.
     */
    public void projectFileOpened(VPTNode file)
    {
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            model.fileOpened(file);
        }
    }


    /**
     * Notifies the trees that a project's file was closed.
     */
    public void projectFileClosed(VPTNode file)
    {
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            model.fileClosed(file);
        }
    }

    /**
     * Notifies the trees that a project's file was closed.
     */
    public void projectClosed(VPTProject p)
    {
        for (JTree tree : trees) {
            ProjectTreeModel model = (ProjectTreeModel) tree.getModel();
            model.projectClosed(p);
        }
    }

    //}}}


	/**
     * Creates a new tree to be added to the viewer.
     */
	private JTree createTree(ProjectTreeModel model,
                             boolean useTooltips)
    {
		PVTree tree = new PVTree(model);

		tree.setCellRenderer(new VPTCellRenderer(useTooltips));
		if (useTooltips) {
			ToolTipManager.sharedInstance().registerComponent(tree);
		}

		// don't change order!
		tree.addMouseListener(selectionListener);
		tree.addMouseListener(contextMenu);
		tree.addTreeSelectionListener(selectionListener);

		// drag support
		dragSource.createDefaultDragGestureRecognizer(tree,
			DnDConstants.ACTION_COPY, dragListener);

        trees.add(tree);

        if (treePane == null) {
            treePane = new JTabbedPane();
        }
        treePane.addTab(jEdit.getProperty(model.getName(), model.getName()),
                        new JScrollPane(tree));

        return tree;
	}


    //{{{ class PVTree
    /** Listens for key events in the trees. */
    private class PVTree extends JTree
    {

        public PVTree(ProjectTreeModel model)
        {
            super(model);
        }

        public void processKeyEvent(KeyEvent e)
        {
            Action action = null;
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    DockableWindowManager dwm
                        = viewer.getView().getDockableWindowManager();
                    dwm.hideDockableWindow("projectviewer");
                    e.consume();
                    break;

                case KeyEvent.VK_ENTER:
                    TreePath[] paths = getSelectionPaths();
                    for (int i = 0; i < paths.length; i++) {
                        VPTNode n = (VPTNode) paths[i].getLastPathComponent();
                        if (n.isFile()) {
                            n.open();
                        }
                    }
                    e.consume();
                    break;

                case KeyEvent.VK_BACK_SPACE:
                    action = new UpAction();
                    break;

                case KeyEvent.VK_DELETE:
                    action = new NodeRemoverAction(true);
                    break;

                case KeyEvent.VK_F5:
                    action = new ReimportAction();
                    break;

                case KeyEvent.VK_F2:
                    action = new NodeRenamerAction();
                    break;

                default:
                    super.processKeyEvent(e);
                }
            } else {
                super.processKeyEvent(e);
            }

            if (action != null) {
                e.consume();
                action.setViewer(viewer);
                action.actionPerformed(null);
            }
        }


        /**
         *  If trying to expand unloaded projects, load them before expansion
         *  occurs.
         */
        public void expandPath(TreePath path)
        {
            VPTNode n = (VPTNode) path.getLastPathComponent();
            if (n.isProject()
                && !ProjectManager.getInstance().isLoaded(n.getName()))
            {
                synchronized (n) {
                    if (!ProjectManager.getInstance().isLoaded(n.getName())) {
                        viewer.setStatus(jEdit.getProperty("projectviewer.loading_project",
                                         new Object[] { n.getName() } ));
                        ProjectManager.getInstance().getProject(n.getName());
                    }
                }
            }
            super.expandPath(path);

            if (n.isProject() || n.isGroup()) {
                for (PVTree tree : trees) {
                    if (tree != this) {
                        tree.expand(path);
                    }
                }
            }
        }


        /** Keeps trees syncd w.r.t. projects and groups. */
        public void collapsePath(TreePath path)
        {
            super.collapsePath(path);
            VPTNode n = (VPTNode) path.getLastPathComponent();
            if (n.isProject() || n.isGroup()) {
                for (PVTree tree : trees) {
                    if (tree != this) {
                        tree.collapse(path);
                    }
                }
            }
        }


        /**
         *  Used internally to bypass the overridden "expandPath()" method and
         *  keep the different trees synced w.r.t. projects and groups.
         */
        private void expand(TreePath path)
        {
            super.expandPath(path);
        }


        /**
         *  Used internally to bypass the overridden "expandPath()" method and
         *  keep the different trees synced w.r.t. projects and groups.
         */
        private void collapse(TreePath path)
        {
            super.collapsePath(path);
        }

    } //}}}

    //{{{ class TreeDragListener
    /**
     *  Implements a DragGestureListener for the trees, that will detect when
     *  the user tries to drag a file to somewhere. Other kinds of nodes will
     *  be ignored.
     */
    private class TreeDragListener implements DragGestureListener
    {

        public void dragGestureRecognized(DragGestureEvent dge)
        {
            JTree tree = getCurrentTree();
            TreePath path = tree.getPathForLocation( (int) dge.getDragOrigin().getX(),
                                (int) dge.getDragOrigin().getY());

            if (path != null) {
                VPTNode n = (VPTNode) path.getLastPathComponent();
                if (n.isFile() && VFSHelper.isLocal(((VPTFile)n).getURL())) {
                    VFSFile file = ((VPTFile)n).getFile();
                    if (file != null && file.getVFS() == VFSManager.getFileVFS()) {
                        File f = new File(((VPTFile)n).getURL());
                        dge.startDrag(DragSource.DefaultCopyDrop,
                                      new FileListTransferable(f));
                    }
                }
            }
        }

    } //}}}

    //{{{ class _FileListTransferable_
    /** A transferable for a file. */
    private static class FileListTransferable
        extends LinkedList<File>
        implements Transferable
    {

        public FileListTransferable(File file)
        {
            super.add(file);
        }


        public Object getTransferData(DataFlavor flavor)
        {
            if (flavor == DataFlavor.javaFileListFlavor) {
                return this;
            }
            return null;
        }


        public DataFlavor[] getTransferDataFlavors()
        {
            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
        }


        public boolean isDataFlavorSupported(DataFlavor flavor)
        {
            return (flavor == DataFlavor.javaFileListFlavor);
        }

    } //}}}

}

