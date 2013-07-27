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

package gruntspud.ui.preferences;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.UIUtil;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class FileTypeMappingTable
    extends JTable {
    //  Private instance variables
    private GruntspudContext context;

    /**
     *  Constructor for the CVSFileNodeTable object
     */
    public FileTypeMappingTable(GruntspudContext context) {
        super();
        this.context = context;
        setModel(context.getFileTypeMappingModel());
        setShowGrid(false);
        setAutoResizeMode(0);
        setRowHeight(18);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        UIUtil.restoreTableMetrics(this,
                                   Constants.FILE_TYPE_MAPPING_TABLE_GEOMETRY,
                                   new int[] {256, 240}
                                   ,
                                   context);
        setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUp() {
        UIUtil.saveTableMetrics(this,
                                Constants.FILE_TYPE_MAPPING_TABLE_GEOMETRY,
                                context);
    }

    /**
     *  Gets the scrollableTracksViewportHeight attribute of the
     *  CVSFileNodeTable object
     *
     *@return    The scrollableTracksViewportHeight value
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
}
