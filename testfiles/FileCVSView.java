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
import gruntspud.SortCriteria;
import gruntspud.file.FileTypeMapping;
import gruntspud.ui.SortableTableHeader;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Description of the Class
 * 
 * @author magicthize
 * @created 26 May 2002
 */
public class FileCVSView extends AbstractCVSView implements ListSelectionListener, ChangeListener, MouseListener {
    protected CVSFileNodeTable fileNodeTable;
    protected boolean adjusting;
    private JScrollPane scroller;
    private SortCriteria sortCriteria;
    private String tableGeometryPropertyPrefix;
    private String textMask;
    private boolean selected;
    protected JPanel tableComponent;
    protected CardLayout cardLayout;

    /**
     * Creates a new FileCVSView object.
     * 
     * @param sortCriteria
     *            DOCUMENT ME!
     * @param tableGeometryPropertyPrefix
     *            DOCUMENT ME!
     * @param textMask
     *            DOCUMENT ME!
     */
    public FileCVSView(SortCriteria sortCriteria, String tableGeometryPropertyPrefix, String textMask) {
        this(sortCriteria, tableGeometryPropertyPrefix, "File",
                        "Shows contents of currently selected directory as a table of files", textMask);
    }

    protected FileCVSView(SortCriteria sortCriteria, String tableGeometryPropertyPrefix, String name, String toolTipText,
                    String textMask) {
        super(name, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_BROWSE), toolTipText);

        //
        this.textMask = textMask;
        this.sortCriteria = sortCriteria;
        this.tableGeometryPropertyPrefix = tableGeometryPropertyPrefix;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean preserveViewOnInit() {
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getViewToolTipText() {
        CVSFileNode cwd = getCWDNode();
        StringBuffer buf = new StringBuffer();
        buf.append(getViewName());

        if ((cwd != null) && !(cwd.isLeaf() && !cwd.getFile().exists())) {
            buf.append(" - ");
            buf.append(cwd.getName());
            buf.append(" (");
            buf.append(getModel().getRowCount());
            buf.append(" items)");
        }

        return buf.toString();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean canClose() {
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    public void resort() {
        ((CVSFileNodeTableModel) fileNodeTable.getModel()).fireTableDataChanged();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public CVSFileNodeTableModel getModel() {
        return (CVSFileNodeTableModel) fileNodeTable.getModel();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public CVSFileNode getCWDNode() {
        return fileNodeTable.getRootNode();
    }

    protected void startTable(CVSFileNodeTableModel model) {
        fileNodeTable = new CVSFileNodeTable(tableGeometryPropertyPrefix);
        fileNodeTable.init(getViewManager().getContext(), model, sortCriteria, textMask);
        fileNodeTable.getSelectionModel().addListSelectionListener(this);
        ((SortableTableHeader) fileNodeTable.getTableHeader()).addChangeListener(this);
        fileNodeTable.addMouseListener(this);

        // Create the 'Loading tree' panel
        JPanel loading = new JPanel(new BorderLayout());
        JLabel loadingLabel = new JLabel("Loading file tree ..", UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_STOP_COMMAND),
                        JLabel.CENTER);
        loading.add(loadingLabel);
        loading.setOpaque(true);
        loading.setBackground(UIManager.getColor("Table.background"));
        loading.setForeground(UIManager.getColor("Table.foreground"));
        loading.setFont(UIManager.getFont("Table.font"));

        //
        scroller = new JScrollPane(fileNodeTable);
        tableComponent = new JPanel(cardLayout = new CardLayout());
        tableComponent.add("scroller", scroller);
        tableComponent.add("loading", loading);
        cardLayout.show(tableComponent, "scroller");
    }

    public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            CVSFileNode n = fileNodeTable.getSelectedFileNode();

            if (n != null) {
                if (n.isLeaf()) {
                    getViewManager().getContext().openNode(n, FileTypeMapping.OPEN_USING_DEFAULT);
                } else {
                    setCWDNode(n);
                    fireViewEvent(new ViewEvent(FileCVSView.this, ViewEvent.CWD_CHANGED, false));
                }
            }
        } else if (evt.isPopupTrigger()) {
            popupMenu(evt);
        }
    }

    public void changeEvent(ChangeEvent evt) {
        getViewManager().refilterAndResort();
    }

    private void popupMenu(MouseEvent evt) {

        int row = fileNodeTable.rowAtPoint(evt.getPoint());

        if (row != -1) {
            boolean inSelection = false;
            int[] sel = fileNodeTable.getSelectedRows();

            if (sel != null) {
                for (int i = 0; (i < sel.length) && !inSelection; i++) {
                    if (sel[i] == row) {
                        inSelection = true;

                    }
                }
            }
            if (!inSelection) {
                fileNodeTable.getSelectionModel().clearSelection();
                fileNodeTable.getSelectionModel().addSelectionInterval(row, row);
            }
        }

        getViewManager().showNodeContextMenu(fileNodeTable, evt.getX(), evt.getY());
    }

    /**
     * DOCUMENT ME!
     * 
     * @param manager
     *            DOCUMENT ME!
     */
    public void start(ViewManager manager) {
        super.start(manager);
        startTable(new DefaultCVSFileNodeTableModel(sortCriteria));
    }

    /**
     * DOCUMENT ME!
     */
    public void stop() {
        super.stop();
        fileNodeTable.cleanUp();
        if (getViewManager().isViewVisible(this)) {
            fileNodeTable.saveColumnPositions();
        }
        fileNodeTable.getSelectionModel().removeListSelectionListener(this);
        ((SortableTableHeader) fileNodeTable.getTableHeader()).removeChangeListener(this);
        fileNodeTable.removeMouseListener(this);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param evt
     *            DOCUMENT ME!
     */
    public void valueChanged(ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            fireViewEvent(new ViewEvent(this, ViewEvent.SELECTION_CHANGED, adjusting));
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void viewSelected() {
        selected = true;
    }

    /**
     * DOCUMENT ME!
     */
    public void viewDeselected() {
        fileNodeTable.saveColumnPositions();
        selected = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     *            DOCUMENT ME!
     */
    public void updateNode(CVSFileNode node) {
        if (node == getCWDNode())
            getModel().fireTableDataChanged();
        else {
            int i = getModel().indexOf(node);
            if (i != -1)
                getModel().fireTableRowsUpdated(i, i);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     *            DOCUMENT ME!
     */
    public void refilterAndResort() {
        getModel().fireTableDataChanged();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param node
     *            DOCUMENT ME!
     */
    public void reload(CVSFileNode node) {
        if (getViewManager().isViewVisible(this)) {
            adjusting = true;

            CVSFileNode rn = null;

            if (getCWDNode() != null && node.getFile().equals(getCWDNode().getFile())) {
                rn = node;
            } else {
                int s = fileNodeTable.getModel().getRowCount();

                for (int i = 0; (i < s) && (rn == null); i++) {
                    CVSFileNode n = ((CVSFileNodeTableModel) fileNodeTable.getModel()).getFileNodeAt(i);

                    if (n == node) {
                        rn = (CVSFileNode) n.getParent();
                    }
                }
            }

            if (rn != null) {
                fileNodeTable.setRootNode(rn);
            } else {
                Constants.UI_LOG.error("Node to reload is not in the current table");

            }
            fileNodeTable.repaint();
            adjusting = false;
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public JComponent getViewComponent() {
        return tableComponent;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param rootNode
     *            DOCUMENT ME!
     */
    public void setRootNode(CVSFileNode rootNode) {
        adjusting = true;
        fileNodeTable.setRootNode(rootNode);

        //        setSelectedNodes(new CVSFileNode[] { rootNode } );
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param cwdNode
     *            DOCUMENT ME!
     */
    public void setCWDNode(CVSFileNode cwdNode) {
        adjusting = true;
        fileNodeTable.setRootNode(cwdNode);
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param sel
     *            DOCUMENT ME!
     */
    public void setSelectedNodes(CVSFileNode[] sel) {
        adjusting = true;
        fileNodeTable.setSelectedFileNodes(sel);
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public CVSFileNode[] getSelectedNodes() {
        return fileNodeTable.getSelectedFileNodes();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param evt
     *            DOCUMENT ME!
     */
    public void viewEventNotify(ViewEvent evt) {
        if (evt.getType() == ViewEvent.TREE_STARTED_LOADING) {
            cardLayout.show(tableComponent, "loading");
        } else if (evt.getType() == ViewEvent.TREE_FINISHED_LOADING) {
            cardLayout.show(tableComponent, "scroller");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            popupMenu(evt);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            popupMenu(evt);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {
    }
}