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

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.status.StatusCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class StatusOptionsPane
    extends Tabber {
    private StatusGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudCheckBox includeTags;
    private GruntspudCheckBox updateExplorer;

    /**
     *  Constructor for the StatusOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public StatusOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new StatusGeneralTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(generalTab);
        addTab(globalOptionsTab);
    }

    public Command[] getCommandsForSettings() {
        StatusCommand cmd = new StatusCommand();
        cmd.setIncludeTags(includeTags.isSelected());
        return new Command[] {
            cmd};
    }

    public boolean isUpdateTree() {
        return updateExplorer.isSelected();
    }

    class StatusGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the StatusGeneralTab object
         */
        StatusGeneralTab() {
            super("Log", UIUtil.getCachedIcon(Constants.ICON_TOOL_STATUS));
            setTabToolTipText("Show the status for the selection");
            setLayout(new GridBagLayout());
            setTabMnemonic('s');
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.
                                                 ICON_TOOL_LARGE_STATUS));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            includeTags = new GruntspudCheckBox("Include tags");
            includeTags.setMnemonic('i');
            includeTags.setSelected(context.getHost().getBooleanProperty(
                Constants.STATUS_GENERAL_INCLUDE_TAGS,
                false));
            UIUtil.jGridBagAdd(this, includeTags, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weighty = 1.0;
            updateExplorer = new GruntspudCheckBox(
                "Update status of files in the explorer");
            updateExplorer.setMnemonic('u');
            updateExplorer.setSelected(context.getHost().getBooleanProperty(
                Constants.STATUS_GENERAL_UPDATE_EXPLORER,
                false));
            UIUtil.jGridBagAdd(this, updateExplorer, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 STATUS_GENERAL_INCLUDE_TAGS,
                                                 includeTags.isSelected());
            context.getHost().setBooleanProperty(Constants.
                                                 STATUS_GENERAL_UPDATE_EXPLORER,
                                                 updateExplorer.isSelected());
        }

        public void tabSelected() {
        }
    }
}
