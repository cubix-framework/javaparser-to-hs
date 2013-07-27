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

import gruntspud.SortCriteria;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class SortableHeaderRenderer
    extends JLabel
    implements TableCellRenderer {
    private Border border;
    private boolean showSortIcons;
    private int[] sorts;
    private Icon upSortIcon;
    private Icon downSortIcon;
    private Dimension lastSize;

    /**
     * Creates a new SortableHeaderRenderer object.
     *
     * @param model DOCUMENT ME!
     * @param showSortIcons DOCUMENT ME!
     * @param sortCriteria DOCUMENT ME!
     */
    public SortableHeaderRenderer(TableColumnModel model,
                                  boolean showSortIcons,
                                  SortCriteria sortCriteria) {
        super("");

        //  Init
        sorts = new int[model.getColumnCount()];
        sorts[sortCriteria.getSortType()] = sortCriteria.getSortDirection();
        upSortIcon = new UpSortIcon();
        downSortIcon = new DownSortIcon();

        setForeground(UIManager.getColor("TableHeader.foreground"));
        setBackground(UIManager.getColor("TableHeader.background"));
        setFont(getFont().deriveFont(10f));
        setBorder(BorderFactory.createCompoundBorder(UIManager.getBorder(
            "TableHeader.cellBorder"),
            BorderFactory.createEmptyBorder(0, 2, 0, 2)));

        //
        setHorizontalTextPosition(JLabel.LEFT);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getMinimumSize() {
        return new Dimension(1, 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param table DOCUMENT ME!
     * @param value DOCUMENT ME!
     * @param isSelected DOCUMENT ME!
     * @param hasFocus DOCUMENT ME!
     * @param row DOCUMENT ME!
     * @param column DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        //
        switch (sorts[column]) {
            case SortCriteria.SORT_ASCENDING:
                setIcon(upSortIcon);

                break;
            case SortCriteria.SORT_DESCENDING:
                setIcon(downSortIcon);

                break;
            default:
                setIcon(null);

                break;
        }

        //
        setText(value.toString());

        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param ShowSortIcons DOCUMENT ME!
     */
    public void setShowSortIcons(boolean ShowSortIcons) {
        this.showSortIcons = showSortIcons;
    }

    /**
     * DOCUMENT ME!
     *
     * @param col DOCUMENT ME!
     */
    public void clearSort(int col) {
        sorts[col] = SortCriteria.NO_SORT;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isShowSortIcons() {
        return showSortIcons;
    }

    /**
     * DOCUMENT ME!
     *
     * @param col DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int reverseSort(int col) {
        return sorts[col] = ( (sorts[col] == SortCriteria.SORT_ASCENDING)
                             ? SortCriteria.SORT_DESCENDING :
                             SortCriteria.SORT_ASCENDING);
    }

    /**
     * DOCUMENT ME!
     *
     * @param col DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int nextSort(int col) {
        return sorts[col] = ( (sorts[col] == SortCriteria.SORT_ASCENDING)
                             ? SortCriteria.SORT_DESCENDING
                             : ( (sorts[col] == SortCriteria.SORT_DESCENDING)
                                ? SortCriteria.NO_SORT :
                                SortCriteria.SORT_ASCENDING));
    }

    /**
     * DOCUMENT ME!
     *
     * @param col DOCUMENT ME!
     * @param sortType DOCUMENT ME!
     */
    public void setSort(int col, int sortType) {
        sorts[col] = sortType;
    }

    /**
     * DOCUMENT ME!
     *
     * @param i DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getSort(int i) {
        return sorts[i];
    }

    class DownSortIcon
        implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(getBackground().darker());
            g.drawLine(x, y, (x + getIconWidth()) - 1, y);
            g.drawLine(x, y, (x + (getIconWidth() / 2)) - 2,
                       (y + getIconHeight()) - 2);
            g.drawLine(x + 1, y + 1, (x + (getIconWidth() / 2)) - 1,
                       (y + getIconHeight()) - 1);
            g.setColor(getBackground().brighter());
            g.drawLine( (x + getIconWidth()) - 2, y + 1,
                       x + (getIconWidth() / 2), (y + getIconHeight()) - 1);
            g.drawLine( (x + getIconWidth()) - 1, y + 1,
                       x + (getIconWidth() / 2) + 1, (y + getIconHeight()) - 2);
        }

        public int getIconWidth() {
            return 8;
        }

        public int getIconHeight() {
            return 7;
        }
    }

    class UpSortIcon
        implements Icon {
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(getBackground().darker());
            g.drawLine(x + 1, (y + getIconHeight()) - 1,
                       (x + (getIconWidth() / 2)) - 1, y + 2);
            g.drawLine(x, y + getIconHeight(), (x + (getIconWidth() / 2)) - 1,
                       y + 1);
            g.setColor(getBackground().brighter());
            g.drawLine(x, y + getIconHeight(), (x + getIconWidth()) - 1,
                       y + getIconHeight());
            g.drawLine( (x + getIconWidth()) - 1, y + getIconHeight(),
                       x + (getIconWidth() / 2) + 1, y + 2);
            g.drawLine( (x + getIconWidth()) - 2, (y + getIconHeight()) - 1,
                       x + (getIconWidth() / 2), y + 1);
        }

        public int getIconWidth() {
            return 8;
        }

        public int getIconHeight() {
            return 7;
        }
    }
}
