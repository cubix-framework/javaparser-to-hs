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

import gruntspud.filter.CVSFileFilter;
import gruntspud.ui.CVSFileDirectoryFilterPane;
import gruntspud.ui.CVSFileStatusFilterPane;
import gruntspud.ui.CVSFileSubstTypeFilterPane;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileFilterPane
    extends JPanel {
    private CVSFileFilter filter;
    private CVSFileStatusFilterPane cvsFileStatusFilterPane;
    private CVSFileSubstTypeFilterPane cvsFileTypeFilterPane;
    private CVSFileDirectoryFilterPane cvsFileDirectoryFilterPane;

    /**
     *  Constructor for the FilterPane object
     *
     *@param  filter  Description of the Parameter
     */
    public CVSFileFilterPane() {
        super(new BorderLayout());

        //
        cvsFileTypeFilterPane = new CVSFileSubstTypeFilterPane();
		cvsFileTypeFilterPane.setOpaque(false);
        cvsFileTypeFilterPane.setBorder(BorderFactory.createTitledBorder(
            "File types"));

        //
        cvsFileStatusFilterPane = new CVSFileStatusFilterPane();
		cvsFileStatusFilterPane.setOpaque(false);
        cvsFileStatusFilterPane.setBorder(BorderFactory.createTitledBorder(
            "File status"));

        //
        cvsFileDirectoryFilterPane = new CVSFileDirectoryFilterPane();
		cvsFileDirectoryFilterPane.setOpaque(false);
        cvsFileDirectoryFilterPane.setBorder(BorderFactory.createTitledBorder(
            "Directory status"));


        //
        JPanel west = new JPanel(new BorderLayout());
		west.setOpaque(false);
        west.add(cvsFileTypeFilterPane, BorderLayout.CENTER);
        west.add(cvsFileDirectoryFilterPane, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);
        top.add(west, BorderLayout.CENTER);
        top.add(cvsFileStatusFilterPane, BorderLayout.EAST);

        //
        add(top, BorderLayout.NORTH);
    }

    /**
     *  Description of the Method
     *
     *@param  evt  Description of the Parameter
     */
    public void apply() {
        cvsFileTypeFilterPane.apply();
        cvsFileStatusFilterPane.apply();
        cvsFileDirectoryFilterPane.apply();
    }

    /**
     *  Sets the filter attribute of the FilterPane object
     *
     *@param  filter  The new filter value
     */
    public void setFilter(CVSFileFilter filter) {
        this.filter = filter;
        cvsFileTypeFilterPane.setCVSFileSubstTypeFilter(filter.
            getCVSFileSubstTypeFilter());
        cvsFileTypeFilterPane.setEnabled(!filter.isPreset());
        cvsFileStatusFilterPane.setCVSFileStatusFilter(filter.
            getCVSFileStatusFilter());
        cvsFileStatusFilterPane.setEnabled(!filter.isPreset());
        cvsFileDirectoryFilterPane.setCVSFileDirectoryFilter(filter.
            getCVSFileDirectoryFilter());
        cvsFileDirectoryFilterPane.setEnabled(!filter.isPreset());

    }

    /**
     *  Gets the filter attribute of the FilterPane object
     *
     *@return    The filter value
     */
    public CVSFileFilter getFilter() {
        return filter;
    }
}
