/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.ui.view;

import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudHost;
import gruntspud.SortCriteria;
import gruntspud.file.FileTypeMapping;
import gruntspud.ui.ListSearch;
import gruntspud.ui.ListSearchListener;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class TreeCVSView
    extends AbstractCVSView
    implements TreeSelectionListener, ListSearchListener {
    private CVSFileNodeTree tree;
    private boolean adjusting;
    private JScrollPane scroller;
    private CVSFileNode cwdNode;
    private SortCriteria sortCriteria;
    private String textMask;
    private ListSearch listSearch;
    private JPanel treeComponent;
    private CardLayout cardLayout;

    /**
     * Creates a new TreeCVSView object.
     *
     * @param sortCriteria DOCUMENT ME!
     * @param textMask DOCUMENT ME!
     */
    public TreeCVSView(SortCriteria sortCriteria, String textMask) {
        super("Folders", UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_HOME),
              "Tree view of CVS files");
        this.sortCriteria = sortCriteria;
        this.textMask = textMask;
    }

    /**
     * DOCUMENT ME!
     *
     * @param searchText DOCUMENT ME!
     */
    public void searchUpdated(String searchText) {
        Constants.UI_LOG.debug("Searching for node begining with " + searchText);
        CVSFileNode n = (CVSFileNode) tree.getModel().getRoot();
        CVSFileNode f = searchNode(n, searchText.toLowerCase());
        if (f != null) {
            setSelectedNodes(new CVSFileNode[] {f});
        }
    }

    private CVSFileNode searchNode(CVSFileNode n, String text) {
        if (n.getName().toLowerCase().startsWith(text)) {
            return n;
        }
        else if (!n.isLeaf() && n.isChildListLoaded() &&
                 tree.isExpanded(getPathForNode(n))) {
            for (int i = 0; i < n.getChildCount(); i++) {
                CVSFileNode f = (CVSFileNode) n.getChildAt(i);
                CVSFileNode r = searchNode(f, text);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     */
    public void searchCancelled() {

    }

    /**
     * DOCUMENT ME!
     */
    public void searchComplete(String searchText) {
        Constants.UI_LOG.debug("Search complete with '" + searchText + "'");
        searchUpdated(searchText);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean canClose() {
        return false;
    }

    private void initialiseTree(CVSFileNode node) {
        GruntspudHost host = getViewManager().getContext().getHost();
		String lineStyle = host.getProperty(
					Constants.OPTIONS_DISPLAY_EXPLORER_LINE_STYLE, "None");
		tree.putClientProperty("JTree.lineStyle", lineStyle);
        tree.setHideFileNodes(host.getBooleanProperty(
            Constants.OPTIONS_DISPLAY_HIDE_FILES_IN_TREE, false));
        ( (CVSFileNodeTreeCellRenderer) tree.getCellRenderer()).
            setShowSubstTypes(host.getBooleanProperty(
            Constants.OPTIONS_DISPLAY_SUBST_TYPES_IN_TREE, true));
        ( (CVSFileNodeTreeCellRenderer) tree.getCellRenderer()).setTextMask(
            textMask);        
        ((CVSFileNodeTreeCellRenderer) tree.getCellRenderer()).setHighlight(
        		getViewManager().getContext().getHost().getBooleanProperty(
        				Constants.OPTIONS_DISPLAY_HIGHLIGHT_READ_ONLY_AND_MISSING_FILES,
        				true));

        if ( (node != null) && node.getFile().exists()) {
            tree.setRootVisible(host.getBooleanProperty(
                Constants.OPTIONS_DISPLAY_SHOW_ROOT_TREE_NODE, false));
            tree.setEnabled(true);
        }
        else {
            tree.setRootVisible(true);
            tree.setEnabled(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cwdNode DOCUMENT ME!
     */
    public void setCWDNode(CVSFileNode cwdNode) {
        setSelectedNodes(new CVSFileNode[] {cwdNode});
    }

    /**
     * DOCUMENT ME!
     *
     * @param manager DOCUMENT ME!
     */
    public void start(ViewManager manager) {
        super.start(manager);
		CVSFileNode node = new CVSFileNode(manager.getContext(), null,
			null, new File("/DUMMY_FILE"), null);
        tree = new CVSFileNodeTree(node, textMask, manager.getContext());
        tree.addTreeSelectionListener(this);
        int insets = manager.getContext().getHost().getFileRendererInsets().top +
            manager.getContext().getHost().getFileRendererInsets().bottom;
        Icon icon = manager.getContext().getHost().getIcon(
            Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_OPEN);
        int rh = Math.max(
            insets + ( icon == null ? 0 : icon.getIconHeight() ),
            16 + insets);
        tree.setRowHeight(rh);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
				CVSFileNode[] s = getSelectedNodes();
                if (evt.getClickCount() == ( s != null && s.length > 0 
                				&& s[0].getFile().isDirectory() ? 3 : 2 ) ) {
                    if ( (s != null) && (s.length == 1)) {
                        getViewManager().getContext().openNode(s[0],
                            FileTypeMapping.OPEN_USING_DEFAULT);
                    }
                }
                else if ( evt.isPopupTrigger() ) {
                    popupMenu(evt);
                }
            }
            
            public void mouseReleased(MouseEvent evt) {
                //else if ( (evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
                if ( evt.isPopupTrigger() ) {
                    popupMenu(evt);
                }
            }
            
            public void mousePressed(MouseEvent evt) {
                //else if ( (evt.getModifiers() & MouseEvent.BUTTON3_MASK) > 0) {
                if ( evt.isPopupTrigger() ) {
                    popupMenu(evt);
                }
            }
        });

        //	ListSearch doesnt work on Java < 1.4
        if(Gruntspud.is14Plus())
            listSearch = new ListSearch(tree, this);

        // Create the 'Loading tree' panel
        JPanel loading = new JPanel(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading file tree ..", UIUtil.getCachedIcon(
        	Constants.ICON_TOOL_SMALL_STOP_COMMAND), JLabel.CENTER);
        loading.add(loadingLabel);
        loading.setOpaque(true);
        loading.setBackground(UIManager.getColor("Tree.background"));
		loading.setForeground(UIManager.getColor("Tree.foreground"));
		loading.setFont(UIManager.getFont("Tree.font"));

        //
        scroller = new JScrollPane(tree);
        treeComponent = new JPanel(cardLayout = new CardLayout());
        treeComponent.add("scroller", scroller);
        treeComponent.add("loading", loading);
        cardLayout.show(treeComponent, "scroller");
    }
    
    private void popupMenu(MouseEvent evt) {

        TreePath path = tree.getPathForLocation(evt.getX(),
            evt.getY());

        if (path != null) {
            boolean inSelection = false;
            TreePath[] sel = tree.getSelectionPaths();

            if (sel != null) {
                for (int i = 0;
                     (i < sel.length) && !inSelection;
                     i++) {
                    if (sel[i].equals(path)) {
                        inSelection = true;

                    }
                }
            }
            if (!inSelection) {
                tree.setSelectedFileNode( (CVSFileNode) path.
                    getLastPathComponent());
            }
        }

        getViewManager().showNodeContextMenu(tree, evt.getX(),
            evt.getY());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSFileNode getCWDNode() {
        return cwdNode;
    }

    /**
     * DOCUMENT ME!
     */
    public void stop() {
        super.stop();
        if(listSearch != null)
        	listSearch.removeSearch();
        tree.removeTreeSelectionListener(this);
    }

    private synchronized TreePath getPathForNode(CVSFileNode node) {
        return new TreePath( ( (DefaultTreeModel) tree.getModel()).
                            getPathToRoot(
            node));
    }

    /**
     * DOCUMENT ME!
     */
    public synchronized void resort() {
        ( (DefaultTreeModel) tree.getModel()).reload();
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public synchronized void updateNode(CVSFileNode node) {
    	if(node != null) {
	    	synchronized(tree.getTreeLock()) {
	        	((DefaultTreeModel)tree.getModel()).reload(node);
	    	}
    	}
    }

	/**
	 * DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 */
	public synchronized void refilterAndResort() {
		CVSFileNode node = getViewManager().getRootNode();
		if(node != null) {
            CVSFileNode[] sel = getSelectedNodes();
			((DefaultTreeModel)tree.getModel()).reload((CVSFileNode)tree.getModel().getRoot());
            setSelectedNodes(sel);
		}
	}

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public synchronized void reload(CVSFileNode node) {
    	synchronized(tree.getTreeLock()) {
	        adjusting = true;
	        if (node == getViewManager().getRootNode()) {
	            initialiseTree(node);
	        }
	        TreePath p = getPathForNode(node);
	        final Vector expanded = new Vector();
	        Enumeration e = tree.getExpandedDescendants(p);
	        while(e != null && e.hasMoreElements()) {
	        	expanded.addElement(e.nextElement());
	        }
	        boolean expand = tree.isExpanded(p);
            ( (DefaultTreeModel) tree.getModel()).reload(node);
	        tree.repaint();
            for (Iterator i = expanded.iterator(); i.hasNext(); ) {
                p = (TreePath) i.next();
                if (tree.getPathBounds(p) != null)
                    tree.expandPath(p);
            }
	        adjusting = false;
			fireViewEvent(new ViewEvent(this, ViewEvent.SELECTION_CHANGED, true));
    	}
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public synchronized void valueChanged(TreeSelectionEvent evt) {
        CVSFileNode n = tree.getSelectedFileNode();

        if (n != null) {
            CVSFileNode newCWD = null;

            if (n.isLeaf()) {
                newCWD = (CVSFileNode) n.getParent();
            }
            else {
                newCWD = n;

            }
            if (newCWD != cwdNode) {
                cwdNode = newCWD;
                fireViewEvent(new ViewEvent(this, ViewEvent.CWD_CHANGED,
                                            adjusting));
            }
        }

        fireViewEvent(new ViewEvent(this, ViewEvent.SELECTION_CHANGED,
                                    adjusting));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getViewComponent() {
        return treeComponent;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rootNode DOCUMENT ME!
     */
    public synchronized void setRootNode(CVSFileNode rootNode) {
        adjusting = true;
        tree.clearSelection();
        if (rootNode != tree.getModel().getRoot()) {
            ( (DefaultTreeModel) tree.getModel()).setRoot(rootNode);
        }
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param sel DOCUMENT ME!
     */
    public synchronized void setSelectedNodes(CVSFileNode[] sel) {
        adjusting = true;
        tree.setSelectedFileNodes(sel);
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSFileNode[] getSelectedNodes() {
        return tree.getSelectedFileNodes();
    }

	/**
	 * DOCUMENT ME!
	 *
	 * @param evt DOCUMENT ME!
	 */
	public synchronized void viewEventNotify(ViewEvent evt) {
		if(evt.getType() == ViewEvent.TREE_STARTED_LOADING) {
			cardLayout.show(treeComponent, "loading");
		}
		else if(evt.getType() == ViewEvent.TREE_FINISHED_LOADING) {
			cardLayout.show(treeComponent, "scroller");
		}
	}
}
