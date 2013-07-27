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

import gruntspud.CVSFileTypeUtil;
import gruntspud.file.DirectoryStatus;
import gruntspud.filter.CVSFileDirectoryFilter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class CVSFileDirectoryFilterPane
    extends JPanel {
    private final static DirectoryStatus[] ALL_DIRECTORY_STATUS = {
        DirectoryStatus.ATTENTION, DirectoryStatus.PROJECT,
        DirectoryStatus.MODULE, DirectoryStatus.UP_TO_DATE,
        DirectoryStatus.PLAIN
    };
    private GruntspudCheckBox[] filters;
    private CVSFileDirectoryFilter filter;

    /**
     * Creates a new CVSFileStatusFilterPane object.
     */
    public CVSFileDirectoryFilterPane() {
        super(new GridBagLayout());

        //
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 1, 8);
        filters = new GruntspudCheckBox[ALL_DIRECTORY_STATUS.length];

        for (int i = 0; i < ALL_DIRECTORY_STATUS.length; i++) {
            gbc.weightx = 0.0;

            Icon icon = CVSFileTypeUtil.getIconForStatus(ALL_DIRECTORY_STATUS[i]);
            UIUtil.jGridBagAdd(this, new JLabel(icon), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;

            if ( (i + 1) == ALL_DIRECTORY_STATUS.length) {
                gbc.weighty = 1.0;

            }
            UIUtil.jGridBagAdd(this,
                               filters[i] = new GruntspudCheckBox( (ALL_DIRECTORY_STATUS[i] == null)
                ? "Unknown" : ALL_DIRECTORY_STATUS[i].toString()), gbc,
                               GridBagConstraints.REMAINDER);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param enabled DOCUMENT ME!
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (int i = 0; i < ALL_DIRECTORY_STATUS.length; i++) {
            filters[i].setEnabled(enabled);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param filter DOCUMENT ME!
     */
    public void setCVSFileDirectoryFilter(CVSFileDirectoryFilter filter) {
        this.filter = filter;

        for (int i = 0; i < ALL_DIRECTORY_STATUS.length; i++) {
            filters[i].setSelected( (filter == null) ||
                                   !filter.isFiltered(ALL_DIRECTORY_STATUS[i]));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSFileDirectoryFilter getCVSFileDirectoryFilter() {
        return filter;
    }

    /**
     * DOCUMENT ME!
     */
    public void apply() {
        filter.clearFilters();

        for (int i = 0; i < ALL_DIRECTORY_STATUS.length; i++) {
            if (!filters[i].isSelected()) {
                filter.addStatusToIgnore(ALL_DIRECTORY_STATUS[i]);
            }
        }
    }
}
