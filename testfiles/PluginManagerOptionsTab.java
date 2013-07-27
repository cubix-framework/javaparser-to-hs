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

package gruntspud.plugin;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.AbstractOptionsTab;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;

import plugspud.PluginManagerPane;

/**
 * Options for plugins
 *
 * @author magicthize
 */
public class PluginManagerOptionsTab
    extends AbstractOptionsTab {
    private GruntspudContext context;
    private PluginManagerPane plugins;

    /**
     * Constructor for the PluginManagerOptionsTab object
     */
    public PluginManagerOptionsTab() {
        super("Plugins", UIUtil.getCachedIcon(Constants.ICON_TOOL_PLUGIN));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);

        setTabToolTipText("Plugins extend the functionality of Gruntspud. You " +
                          "can install and remove plugins from here.");
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_PLUGIN));
        setTabMnemonic('u');
        setTabContext("Plugins");

        plugins = new PluginManagerPane(context.getPluginManager(), context);
        plugins.setBorder(BorderFactory.createEmptyBorder(2, 2, 6, 6));
		plugins.setOpaque(false);

        setLayout(new BorderLayout());
        add(plugins, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean validateTab() {
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    public void tabSelected() {
    }

    /**
     * DOCUMENT ME!
     */
    public void applyTab() {
        plugins.cleanUp();
    }
}
