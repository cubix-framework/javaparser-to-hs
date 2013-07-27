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

import gruntspud.CVSSubstType;
import gruntspud.filter.CVSFileSubstTypeFilter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileSubstTypeFilterPane
    extends JPanel {
    private final static CVSSubstType[] ALL_SUBST_TYPES = {
        CVSSubstType.CVS_SUBST_TYPE_TEXT, CVSSubstType.CVS_SUBST_TYPE_BINARY,
        CVSSubstType.CVS_SUBST_TYPE_OLD_VALUES,
        CVSSubstType.CVS_SUBST_TYPE_ONLY_KEYWORDS,
        CVSSubstType.CVS_SUBST_TYPE_ONLY_VALUES,
        CVSSubstType.CVS_SUBST_TYPE_COPY,
        CVSSubstType.CVS_SUBST_TYPE_IGNORED
    };
    private GruntspudCheckBox[] filters;
    private CVSFileSubstTypeFilter filter;

    /**
     *  Constructor for the CVSFileTypeFilterPane object
     */
    public CVSFileSubstTypeFilterPane() {
        super(new GridBagLayout());

        //
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 1, 8);
        filters = new GruntspudCheckBox[ALL_SUBST_TYPES.length];

        for (int i = 0; i < ALL_SUBST_TYPES.length; i++) {
            gbc.weightx = 0.0;

            Icon icon = ALL_SUBST_TYPES[i].getIcon();
            UIUtil.jGridBagAdd(this, new JLabel(icon), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;

            if ( (i + 1) == ALL_SUBST_TYPES.length) {
                gbc.weighty = 1.0;

            }
            UIUtil.jGridBagAdd(this,
                               filters[i] = new GruntspudCheckBox(ALL_SUBST_TYPES[i].
                getName()), gbc,
                               GridBagConstraints.REMAINDER);
        }
    }

    /**
         *  Sets the cVSFileTypeFilter attribute of the CVSFileTypeFilterPane object
     *
     *@param  filter  The new cVSFileTypeFilter value
     */
    public void setCVSFileSubstTypeFilter(CVSFileSubstTypeFilter filter) {
        this.filter = filter;

        for (int i = 0; i < ALL_SUBST_TYPES.length; i++) {
            filters[i].setSelected( (filter == null) ||
                                   !filter.isFiltered(ALL_SUBST_TYPES[i]));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param enabled DOCUMENT ME!
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        for (int i = 0; i < ALL_SUBST_TYPES.length; i++) {
            filters[i].setEnabled(enabled);
        }
    }

    /**
         *  Gets the cVSFileTypeFilter attribute of the CVSFileTypeFilterPane object
     *
     *@return    The cVSFileTypeFilter value
     */
    public CVSFileSubstTypeFilter getCVSFileSubstTypeFilter() {
        return filter;
    }

    /**
     *  Description of the Method
     */
    public void apply() {
        filter.clearFilters();

        for (int i = 0; i < ALL_SUBST_TYPES.length; i++) {
            if (!filters[i].isSelected()) {
                filter.addSubstTypeToIgnore(ALL_SUBST_TYPES[i]);
            }
        }
    }
}
