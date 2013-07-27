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

package gruntspud.ui.commandoptions;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.tag.TagCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class RemoveTagOptionsPane
    extends Tabber {
    private RemoveTagGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudCheckBox doNotRecurse;
    private GruntspudCheckBox checkUnmodified;
    private GruntspudCheckBox branchTag;
    private GruntspudCheckBox overideExistingTag;
    private StringListComboBox tag;
    private StringListComboBox tagByDate;
    private StringListComboBox tagByRevision;

    /**
     *  Constructor for the LogOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public RemoveTagOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new RemoveTagGeneralTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(generalTab);
        addTab(globalOptionsTab);
    }

    /**
     *  Gets the commandsForSettings attribute of the CommitInfoPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        TagCommand cmd = new TagCommand();
        cmd.setRecursive(!doNotRecurse.isSelected());
        cmd.setDeleteTag(true);
        cmd.setTag(tag.getSelectedItem().toString());

        return new Command[] {
            cmd};
    }

    class RemoveTagGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        RemoveTagGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_TAG));
            setTabToolTipText("General remove tag options");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_REMOVE_TAG));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Tag "), gbc,
                               GridBagConstraints.RELATIVE);
            tag = new StringListComboBox(context,
                                         context.getHost().getProperty(
                Constants.TAG_GENERAL_TAG, ""),
                                         false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, tag, gbc, GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.TAG_GENERAL_DO_NO_RECURSE));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 TAG_GENERAL_DO_NO_RECURSE,
                                                 doNotRecurse.isSelected());
            context.getHost().setProperty(Constants.TAG_GENERAL_TAG,
                                          tag.getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }
}
