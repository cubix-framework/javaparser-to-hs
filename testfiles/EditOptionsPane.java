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
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.WatchList;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.Watch;
import org.netbeans.lib.cvsclient.command.edit.EditCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class EditOptionsPane
    extends Tabber {
    private EditGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudCheckBox checkUnedited;
    private GruntspudCheckBox forceEvenIfEdited;
    private GruntspudCheckBox doNotRecurse;
    private WatchList watch;

    /**
     *  Constructor for the EditOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public EditOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new EditGeneralTab();
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
        EditCommand cmd = new EditCommand();
        cmd.setCheckThatUnedited(checkUnedited.isSelected());
        cmd.setForceEvenIfEdited(forceEvenIfEdited.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());

        if (watch.getSelectedWatch() != null) {
            cmd.setTemporaryWatch(watch.getSelectedWatch());

        }
        return new Command[] {
            cmd};
    }

    class EditGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        EditGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_EDIT));
            setTabToolTipText("General edit options");
            setLayout(new GridBagLayout());
            setTabMnemonic('g');
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_EDIT));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;

            gbc.weightx = 2.0;
            checkUnedited = new GruntspudCheckBox("Check unedited");
            checkUnedited.setSelected(context.getHost().getBooleanProperty(
                Constants.EDIT_GENERAL_CHECK_UNEDITED,
                false));
            UIUtil.jGridBagAdd(this, checkUnedited, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            forceEvenIfEdited = new GruntspudCheckBox("Force even if edited");
            forceEvenIfEdited.setSelected(context.getHost().getBooleanProperty(
                Constants.EDIT_GENERAL_FORCE_EVEN_IF_EDITED,
                false));
            UIUtil.jGridBagAdd(this, forceEvenIfEdited, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.EDIT_GENERAL_DO_NOT_RECURSE,
                false));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;
            watch = new WatchList();
            watch.setSelectedWatchName(context.getHost().getProperty(Constants.
                EDIT_GENERAL_TEMP_WATCH,
                Watch.NONE.toString()));

            JScrollPane scroller = new JScrollPane(watch);
            JPanel p = new JPanel(new GridLayout(1, 1));
            p.setOpaque(false);
            p.setBorder(BorderFactory.createTitledBorder("Temporary watch"));
            p.add(scroller);
            UIUtil.jGridBagAdd(this, p, gbc, GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 EDIT_GENERAL_CHECK_UNEDITED,
                                                 checkUnedited.isSelected());
            context.getHost().setBooleanProperty(Constants.
                EDIT_GENERAL_FORCE_EVEN_IF_EDITED,
                forceEvenIfEdited.isSelected());
            context.getHost().setBooleanProperty(Constants.
                                                 EDIT_GENERAL_DO_NOT_RECURSE,
                                                 doNotRecurse.isSelected());

            String n = watch.getSelectedWatchName();
            context.getHost().setProperty(Constants.EDIT_GENERAL_TEMP_WATCH,
                                          (n == null) ? "" : n);
        }

        public void tabSelected() {
        }
    }
}
