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

package gruntspud.ui.report;

import gruntspud.CVSChangeStatus;
import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.SortCriteria;
import gruntspud.actions.AbstractBottomAction;
import gruntspud.actions.AbstractGotoAction;
import gruntspud.actions.AbstractNextAction;
import gruntspud.actions.AbstractPreviousAction;
import gruntspud.actions.AbstractTopAction;
import gruntspud.ui.ListSearch;
import gruntspud.ui.ListSearchListener;
import gruntspud.ui.SortableTableHeader;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class FileInfoListPane
    extends JPanel
    implements ListSearchListener {
    public final static int SORT_ON_FILE = 0;
    public final static int SORT_ON_TEXT = 1;

    //  Private instance variables
    private FileInfoContainer[] info;
    private GruntspudContext context;
    private FileInfoPane pane;
    private int idx;
    private JTable file;
    private JTextArea help;
    private Action nextAction;
    private Action previousAction;
    private Action topAction;
    private Action bottomAction;
    private Action gotoAction;
    private JLabel action;
    private int minimumPathLength = 0;
    private String minimumPath = null;
    private boolean allFilesInSameDir = false;
    private JSplitPane splitPane;
    private ListSearch listSearch;
    private SortCriteria sortCriteria;
    private Comparator comparator;

    /**
     *  Constructor
     */
    public FileInfoListPane(GruntspudContext context, FileInfoContainer[] info,
                            FileInfoPane pane) throws Exception {
        super(new BorderLayout());

        // first, look at the filenames.  find the least common denominator
        // of the path to chop off
        String path = null;
        String minPath = null;
        File theFile = null;

        for (int i = 0; i < info.length; i++) {
            theFile = info[i].getFile();

            if (minPath == null) {
                minPath = theFile.getParentFile().getAbsolutePath();
            }
            else {
                path = theFile.getAbsolutePath();

                // new file isn't in the same dir
                if (!path.startsWith(minPath)) {
                    // creep up the tree from this file's
                    // parent until we find something that
                    // is common with the minPath.
                    theFile = theFile.getParentFile();
                    path = theFile.getAbsolutePath();

                    while (!minPath.startsWith(path)) {
                        theFile = theFile.getParentFile();
                        path = theFile.getAbsolutePath();
                    }

                    minPath = path;
                }
            }
        }

        this.minimumPathLength = minPath.length();
        this.minimumPath = minPath;
        this.allFilesInSameDir = true;

        for (int i = 0; i < info.length; i++) {
            theFile = info[i].getFile();

            if (!theFile.getParentFile().getAbsolutePath().equals(minPath)) {
                this.allFilesInSameDir = false;
            }
        }

        //
        sortCriteria = new SortCriteria(FileInfoListPane.SORT_ON_FILE,
                                        SortCriteria.SORT_DESCENDING, false,
                                        ! (context.getHost().getBooleanProperty(
            Constants.OPTIONS_DISPLAY_CASE_INSENSITIVE_SORT, false)));

        Arrays.sort(info, comparator = new FileInfoContainerComparator());

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);

        this.context = context;
        this.pane = pane;
        this.info = info;
        this.idx = -1;

        //  Create the toolbar
        JToolBar toolBar = new JToolBar("Connection Profile tools");
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setBorder(null);
        toolBar.setFloatable(false);
		boolean showSelectiveText = context.getHost().getBooleanProperty(
				Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
        toolBar.add(UIUtil.createButton(previousAction = new PreviousAction(),
		showSelectiveText, false));
        toolBar.add(UIUtil.createButton(nextAction = new NextAction(), showSelectiveText,
                                        false));
        toolBar.add(UIUtil.createButton(topAction = new TopAction(), showSelectiveText,
                                        false));
        toolBar.add(UIUtil.createButton(bottomAction = new BottomAction(),
		showSelectiveText, false));
        toolBar.add(UIUtil.createButton(gotoAction = new GotoAction(), showSelectiveText,
                                        false));

        JPanel north = new JPanel(new BorderLayout());
        north.add(toolBar, BorderLayout.NORTH);

        JSeparator s = new JSeparator(JSeparator.HORIZONTAL);
        s.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        north.add(s);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT));
        stats.setBorder(BorderFactory.createTitledBorder("Statistics"));
        stats.add(new JLabel("Total: "));

        //
        JPanel helpPane = null;
        if(pane.getInfoClass() != null &&
        	pane.getInfoClass().isAssignableFrom(CVSChangeStatus.class)) {
            helpPane = new JPanel(new BorderLayout());
            helpPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            JPanel h = new JPanel(new BorderLayout());
            h.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(helpPane.getForeground()),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
            h.setBackground(UIManager.getColor("ToolTip.background"));
            h.setOpaque(true);
            h.add(new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_HELP)), BorderLayout.WEST);
            JScrollPane sc = new JScrollPane(help = new JTextArea("", 5, 20));
            sc.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
            sc.setBackground(UIManager.getColor("ToolTip.background"));
            h.add(sc, BorderLayout.CENTER);
            help.setBorder(null);
            help.setEditable(false);
            help.setBackground(UIManager.getColor("ToolTip.background"));
            help.setForeground(UIManager.getColor("ToolTip.foreground"));
            help.setOpaque(true);
            help.setWrapStyleWord(true);
            help.setLineWrap(true);
            help.setFont(UIManager.getFont("ToolTip.font"));
            helpPane.add(h, BorderLayout.CENTER);
        }

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
        JLabel fl = new JLabel(String.valueOf(info.length));
        fl.setFont(valFont);
        stats.add(fl);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 2, 0),
            BorderFactory.createTitledBorder("Action")));
        actionPanel.add(action = new JLabel(pane.getActionText(),
                                            pane.getActionIcon(), JLabel.LEFT));

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.add(actionPanel, BorderLayout.NORTH);
        westPanel.add(pane, BorderLayout.CENTER);

        file = new FileInfoTable();
        file.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UIUtil.restoreTableMetrics(file, Constants.FILE_INFO_TABLE_GEOMETRY,
                                   new int[] {140, 112}
                                   , context);
        file.getSelectionModel().addListSelectionListener(new
            ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                if (!evt.getValueIsAdjusting()) {
                    int idx = file.getSelectedRow();
                    if (idx != -1) {
                        setInfoAtIndex(idx);
                    }
                }
            }
        });
        ( (SortableTableHeader) file.getTableHeader()).addChangeListener(new
            ChangeListener() {
            public void stateChanged(ChangeEvent evt) {
                resort();
            }
        });

        JScrollPane scroller = new JScrollPane(file);
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 0, 2),
            BorderFactory.createTitledBorder("Files")));
        p.add(scroller, BorderLayout.CENTER);
        p.add(north, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout());
        left.add(p, BorderLayout.CENTER);

        //
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(stats, BorderLayout.NORTH);
        if(helpPane != null)
            bottom.add(helpPane, BorderLayout.CENTER);
        left.add(bottom, BorderLayout.SOUTH);

        //add(westPanel, BorderLayout.CENTER);
        //add(left, BorderLayout.WEST);
        splitPane.add(left);
        splitPane.add(westPanel);

        //splitPane.setDividerLocation((double)0.33);
        int loc = context.getHost().getIntegerProperty(Constants.
            FILE_INFO_DIVIDER_LOCATION_PREFIX +
            pane.getActionText(), -1);
        splitPane.setDividerLocation( (loc == -1) ? 350 : loc);
        splitPane.setResizeWeight( (double) 0.33);

        //file.sizeColumnsToFit(0);
        if(Gruntspud.is14Plus())
            listSearch = new ListSearch(file, this);

        nextInfo();
    }

    private void resort() {
        ( (FileInfoTableModel) file.getModel()).resort();
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getTextForFile(File f) {
        String filePath = f.getAbsolutePath();

        if (minimumPathLength == 0) {
            return filePath;
        }
        else {
            if ( (allFilesInSameDir) &&
                (f.getParentFile().getAbsolutePath().length() ==
                 minimumPathLength)) {
                return f.getName();
            }
            else {

                return "..." + filePath.substring(minimumPathLength);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param searchText DOCUMENT ME!
     */
    public void searchUpdated(String searchText) {
        for (int i = 0; i < info.length; i++) {
            if (info[i].getFile().getName().toLowerCase().startsWith(searchText.
                toLowerCase())) {
                file.getSelectionModel().clearSelection();
                file.getSelectionModel().addSelectionInterval(i, i);
                file.scrollRectToVisible(file.getCellRect(i, 0, true));

                break;
            }
        }
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
        searchUpdated(searchText);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public FileInfoPane getFileInfoPane() {
        return pane;
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUp() {
        UIUtil.saveTableMetrics(file, Constants.FILE_INFO_TABLE_GEOMETRY,
                                context);
        context.getHost().setIntegerProperty(Constants.
                                             FILE_INFO_DIVIDER_LOCATION_PREFIX +
                                             pane.getActionText(),
                                             splitPane.getDividerLocation());
        if(listSearch != null)
            listSearch.removeSearch();
        pane.cleanUp();
    }

    /**
     * Show the next file info
     */
    public void nextInfo() {
        if ( (idx + 1) < info.length) {
            setInfoAtIndex(idx + 1);
        }
    }

    /**
     * Show the previous info
     *
     */
    public void previousInfo() {
        if ( (idx - 1) > -1) {
            setInfoAtIndex(idx - 1);
        }
    }

    /**
     *
     */
    public boolean hasPrevious() {
        return idx > 0;
    }

    /**
     */
    public boolean hasNext() {
        return (idx + 1) < info.length;
    }

    private void setAvailableActions() {
        nextAction.setEnabled(hasNext());
        previousAction.setEnabled(hasPrevious());
        topAction.setEnabled( (info.length > 0) && (idx != 0));
        bottomAction.setEnabled( (info.length > 0) &&
                                (idx != (info.length - 1)));
        gotoAction.setEnabled(file.getSelectedRowCount() == 1);
    }

    /**
     * Show the info with the specified index
     *
     * @param idx indext
     */
    public void setInfoAtIndex(int idx) {
        this.idx = idx;

        pane.setFileInfo(info[idx]);

        if(help != null) {
            CVSChangeStatus cs = (CVSChangeStatus)file.getModel().getValueAt(idx, 1);
            if(cs != null)
                help.setText(cs.getDescription());
            else
                help.setText("There is no help available for this status.");
            help.setCaretPosition(0);
            help.scrollRectToVisible(help.getVisibleRect());
        }

        if (file.getSelectedRow() != idx) {
            file.getSelectionModel().setValueIsAdjusting(true);
            file.getSelectionModel().clearSelection();
            file.getSelectionModel().addSelectionInterval(idx, idx);
        }

        file.scrollRectToVisible(file.getVisibleRect());
        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     */
    public void gotoSelected() {
        int i = file.getSelectedRow();
        File f = info[i].getFile();
        CVSFileNode n = context.getViewManager().findNodeForPath(context.
            getViewManager()
            .getRootNode(),
            f, true);

        if (n != null) {
            context.getViewManager().showNode(n);
        }
    }

    class FileInfoContainerComparator
        implements Comparator {
        public boolean equals(Object other) {
            return (this == other);
        }

        public int compare(Object o1, Object o2) {
            int s = 0;

            switch (sortCriteria.getSortType()) {
                case FileInfoListPane.SORT_ON_TEXT:

                    String s1 = pane.getInfoValueForInfoContainer( (
                        FileInfoContainer) o1)
                        .toString();
                    String s2 = pane.getInfoValueForInfoContainer( (
                        FileInfoContainer) o2)
                        .toString();
                    s = s1.compareTo(s2);

                    break;
                default:
                    if (sortCriteria.isCaseSensitive()) {
                        s = ( (FileInfoContainer) o1).getFile().getAbsolutePath()
                            .compareTo( ( (FileInfoContainer) o2).getFile()
                                       .getAbsolutePath());
                    }
                    else {
                        s = ( (FileInfoContainer) o1).getFile().getAbsolutePath()
                            .compareToIgnoreCase( ( (FileInfoContainer) o2).
                                                 getFile()
                                                 .getAbsolutePath());
                    }
            }

            if (sortCriteria.getSortDirection() == SortCriteria.SORT_ASCENDING) {
                s = s * -1;

            }
            return s;
        }
    }

    class FileInfoTableModel
        extends AbstractTableModel {
        public void resort() {
            Arrays.sort(info, comparator);
            fireTableDataChanged();
        }

        public int getRowCount() {
            return info.length;
        }

        public int getColumnCount() {
            return 2;
        }

        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "File";
                default:
                    return "Info.";
            }
        }

        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return File.class;
                default:
                    return (pane.getInfoClass() == null) ? String.class
                        : pane.getInfoClass();
            }
        }

        public Object getValueAt(int r, int c) {
            switch (c) {
                case 0:
                    return info[r].getFile();
                default:
                    return pane.getInfoValueForInfoContainer(info[r]);
            }
        }
    }

    class FileInfoTableCellRenderer
        extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            setText(getTextForFile( (File) value));
            setToolTipText( ( (File) value).getAbsolutePath());

            return this;
        }

        public Dimension getMinimumSize() {
            return new Dimension(100, super.getPreferredSize().height);
        }
    }

    class FileStatusTableCellRenderer
        extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            setText( (String) value);

            return this;
        }

        public Dimension getMinimumSize() {
            return new Dimension(25, super.getPreferredSize().height);
        }
    }

    class NextAction
        extends AbstractNextAction {
        public void actionPerformed(ActionEvent evt) {
            nextInfo();
        }
    }

    class GotoAction
        extends AbstractGotoAction {
        public void actionPerformed(ActionEvent evt) {
            gotoSelected();
        }
    }

    class PreviousAction
        extends AbstractPreviousAction {
        public void actionPerformed(ActionEvent evt) {
            previousInfo();
        }
    }

    class TopAction
        extends AbstractTopAction {
        public void actionPerformed(ActionEvent evt) {
            setInfoAtIndex(0);
        }
    }

    class BottomAction
        extends AbstractBottomAction {
        public void actionPerformed(ActionEvent evt) {
            setInfoAtIndex(info.length - 1);
        }
    }

    class FileInfoTable
        extends JTable {
        FileInfoTable() {
            super();
            setModel(new FileInfoTableModel());
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setDefaultRenderer(File.class, new FileInfoTableCellRenderer());
            setDefaultRenderer(String.class, new FileStatusTableCellRenderer());

            if ( (pane.getInfoClass() != null) &&
                (pane.getInfoRenderer() != null)) {
                setDefaultRenderer(pane.getInfoClass(), pane.getInfoRenderer());

            }
            setTableHeader(new SortableTableHeader(context.getHost(), this,
                sortCriteria));
        }
    }
}
