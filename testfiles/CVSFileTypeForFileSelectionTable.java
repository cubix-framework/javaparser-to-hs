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

package gruntspud.ui;

import gruntspud.CVSFileNode;
import gruntspud.CVSFileTypeUtil;
import gruntspud.CVSSubstType;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class CVSFileTypeForFileSelectionTable
    extends JTable {
    private CVSSubstType[] types;
    private CVSFileNode[] nodes;

    /**
     * Creates a new CVSFileTypeForFileSelectionTable object.
     *
     * @param nodes DOCUMENT ME!
     * @param includeIgnore DOCUMENT ME!
     */
    public CVSFileTypeForFileSelectionTable(CVSFileNode[] nodes,
                                            boolean includeIgnore) {
        super();
        this.nodes = nodes;
        types = new CVSSubstType[nodes.length];
        setModel(new CVSFileTypeForFileTableModel(nodes));
        setShowGrid(false);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        setRowHeight(18);
        setDefaultRenderer(CVSSubstType.class, new SubstTypeTableCellRenderer());
        setDefaultEditor(CVSSubstType.class,
                         new SubstTypeTableCellEditor(includeIgnore));

        int[] s = {
            256, 96};

        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            getColumnModel().getColumn(i).setPreferredWidth(s[i]);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPreferredScrollableViewportSize() {
        return new Dimension(352,
                             super.getPreferredScrollableViewportSize().height);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScrollableTracksViewportHeight() {
        Component parent = getParent();

        if (parent instanceof JViewport) {
            return parent.getHeight() > getPreferredSize().height;
        }
        else {

            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSSubstType[] getTypes() {
        return types;
    }

    //public int getRowCount()
    class CVSFileTypeForFileTableModel
        extends AbstractTableModel {
        private CVSFileNode[] nodes;

        CVSFileTypeForFileTableModel(CVSFileNode[] nodes) {
            this.nodes = nodes;

            for (int i = 0; i < nodes.length; i++) {
                types[i] = CVSFileTypeUtil.getSubstTypeForLineEndings(
                	CVSFileTypeUtil.getLineEndings(nodes[i].getFile()));
            }
        }

        public int getRowCount() {
            return nodes.length;
        }

        public boolean isCellEditable(int r, int c) {
            return ( (c == 1) &&
                    (types[r] != CVSSubstType.CVS_SUBST_TYPE_DIRECTORY)) ? true : false;
        }

        public CVSFileNode getFileNodeAt(int r) {
            return nodes[r];
        }

        public int getColumnCount() {
            return 2;
        }

        public void setValueAt(Object val, int r, int c) {
            types[r] = (CVSSubstType) val;
            fireTableRowsUpdated(r, r);
        }

        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Name";
                default:
                    return "Type";
            }
        }

        public Class getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return String.class;
                default:
                    return CVSSubstType.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            CVSFileNode f = getFileNodeAt(rowIndex);

            switch (columnIndex) {
                case 0:
                    return f.getName();
                default:
                    return types[rowIndex];
            }
        }
    }
}
