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
import gruntspud.GruntspudUtil;

import java.awt.Component;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileNodeFileSizeTableCellRenderer
    extends DefaultTableCellRenderer {
    private final static NumberFormat FORMAT = new DecimalFormat();

    static {
        FORMAT.setMinimumFractionDigits(1);
        FORMAT.setMaximumFractionDigits(1);
    }

    /**
     * Creates a new CVSFileNodeFileSizeTableCellRenderer object.
     */
    public CVSFileNodeFileSizeTableCellRenderer() {
        super();
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    /**
     *  Gets the tableCellRendererComponent attribute of the
     *  CVSFileNodeTableCellRenderer object
     *
     *@param  table       Description of the Parameter
     *@param  value       Description of the Parameter
     *@param  isSelected  Description of the Parameter
     *@param  hasFocus    Description of the Parameter
     *@param  row         Description of the Parameter
     *@param  column      Description of the Parameter
     *@return             The tableCellRendererComponent value
     */
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                                            row, column);

        CVSFileNode n = (CVSFileNode) value;
        if(n == null) {
            setText("<null>");
        }
        else {
            if (n.isLeaf()) {
                setText(GruntspudUtil.formatFileSize(n.getFile().length()));
            }
            else {
                setText(n.getChildCount() + " items");

            }
        }
        return this;
    }
}
