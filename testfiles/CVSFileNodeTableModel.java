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
import gruntspud.ui.UIUtil;
import gruntspud.ui.icons.EmptyIcon;
import gruntspud.ui.icons.OverlayIcon;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public abstract class CVSFileNodeTableModel
    extends AbstractTableModel {
    private final static Icon EMPTY_ICON = new EmptyIcon(16, 16);
    private DateFormat dateFormat;
    private SortCriteria sortCriteria;

    /**
     *
     */
    public CVSFileNodeTableModel(SortCriteria sortCriteria) {
        dateFormat = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        this.sortCriteria = sortCriteria;
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
     * Set the date format
     *
     * @return
     */
    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;

        //        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
        fireTableDataChanged();
    }

    /**
     *  Gets the rowCount attribute of the CVSFileNodeTableModel object
     *
     *@return    The rowCount value
     */
    public int getRowCount() {
        int r = getFileNodeCount();

        return r;
    }

    /**
     *  Description of the Method
     *
     *@param  node  Description of the Parameter
     *@return       Description of the Return Value
     */
    public abstract int indexOf(CVSFileNode node);

    /**
     *  Gets the rootNode attribute of the CVSFileNodeTableModel object
     *
     *@return    The rootNode value
     */
    public abstract CVSFileNode getRootNode();

    /**
     *  Gets the rootNode attribute of the CVSFileNodeTableModel object
     *
     *@return    The rootNode value
     */
    public abstract void setRootNode(CVSFileNode root);

    /**
     *  Gets the fileNodeCount attribute of the CVSFileNodeTableModel object
     *
     *@return    The fileNodeCount value
     */
    public abstract int getFileNodeCount();

    /**
     *  Gets the fileNodeAt attribute of the CVSFileNodeTableModel object
     *
     *@param  r  Description of the Parameter
     *@return    The fileNodeAt value
     */
    public abstract CVSFileNode getFileNodeAt(int r);

    /**
     *  Gets the columnCount attribute of the CVSFileNodeTableModel object
     *
     *@return    The columnCount value
     */
    public int getColumnCount() {
        return 11;
    }

    /**
     *  Gets the columnName attribute of the CVSFileNodeTableModel object
     *
     *@param  columnIndex  Description of the Parameter
     *@return              The columnName value
     */
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "S";
            case 1:
                return "T";
            case 2:
                return "Name";
            case 3:
                return "Type";
            case 4:
                return "Revision";
            case 5:
                return "Date";
            case 6:
                return "Flags";
            case 7:
                return "Size";
            case 8:
                return "Local Status";
            case 9:
                return "Remote Status";
            default:
                return "Tag";
        }
    }

    /**
     *  Gets the columnClass attribute of the CVSFileNodeTableModel object
     *
     *@param  columnIndex  Description of the Parameter
     *@return              The columnClass value
     */
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Icon.class;
            case 1:
                return Icon.class;
            case 2:
                return CVSFileNode.class;
            case 3:
                return String.class;
            case 4:
                return String.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            case 7:
                return CVSFileNode.class;
            case 8:
                return String.class;
            case 9:
                return String.class;

            default:
                return String.class;
        }
    }

    /**
     *  Gets the valueAt attribute of the CVSFileNodeTableModel object
     *
     *@param  rowIndex     Description of the Parameter
     *@param  columnIndex  Description of the Parameter
     *@return              The valueAt value
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        CVSFileNode f = getFileNodeAt(rowIndex);

        if (f == null) {
            return null;
        }

        switch (columnIndex) {
            case 0:
                return f.getIcon(false);
            case 1:
            	Icon i = (f.getCVSSubstType() == null) ? EMPTY_ICON
                    : f.getCVSSubstType()
                    .getIcon();

        		if(i != null) {
					switch(f.getLineEndings()) {
						case CVSFileNode.WINDOWS_LINE_ENDINGS:
							i = new OverlayIcon(UIUtil.getCachedIcon(
								Constants.ICON_WINDOWS_LINE_ENDINGS), i, SwingConstants.CENTER);
							break;
						case CVSFileNode.UNIX_LINE_ENDINGS:
							i = new OverlayIcon(UIUtil.getCachedIcon(
								Constants.ICON_UNIX_LINE_ENDINGS), i, SwingConstants.CENTER);
							break;  
					}
				}
				return i;
            case 2:
                return f;
            case 3:
                String s3 = f.getFileTypeText();
                return s3 == null ? "" : s3;
            case 4:
                return (f.getEntry() == null) ? "" : f.getEntry().getRevision();
            case 5:
                return ( (f.getEntry() == null) ||
                        (f.getEntry().getLastModified() == null)) ? ""
                    : dateFormat.format(f.getEntry()
                                        .getLastModified());
            case 6:
                return (f.getEntry() == null || f.getEntry().getOptions() == null ) ? "" : f.getEntry().getOptions();
            case 7:
                return f;
            case 8:
                String s1 = f.getLocalStatusText(); 
                return s1 == null ? "" : s1;
            case 9:
                String s2 = f.getRemoteStatusText();
                return s2 == null ? "" : s2;
            default:
                return (f.getEntry() == null || f.getEntry().getTag() == null ) ? "" : f.getEntry().getTag();
        }
    }
}
