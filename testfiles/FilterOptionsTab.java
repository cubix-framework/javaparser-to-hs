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

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

/**
 * UI component for editing of display options
 *
 * @author magicthize
 */
public class FilterOptionsTab
    extends AbstractOptionsTab {

    private FilterPane filterPane;

    /**
     * Constructor for the DisplayOptionsTab object
     */
    public FilterOptionsTab() {
        super("Filter", UIUtil.getCachedIcon(Constants.ICON_TOOL_FILTER));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);

        setTabToolTipText("Define your own named filters.");
        setTabMnemonic('f');
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_FILTER));
        setTabContext("General");

        JPanel e = new JPanel(new BorderLayout());
		e.setOpaque(false);
        filterPane = new FilterPane(context);
        e.add(filterPane);
        setLayout(new GridLayout(1, 1));
        add(e, BorderLayout.CENTER);
    }

    public void tabSelected() {

    }

    /**
     * Validate the options
     *
     * @return <code>true</code> if the options were ok
     */
    public boolean validateTab() {
        return true;
    }

    /**
     * Apply the options
     */
    public void applyTab() {
        filterPane.apply();
    }
}
