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
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.log.LogCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class LogOptionsPane
    extends Tabber {
    private LogGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private LogFilterTab filterTab;
    private GruntspudContext context;
    private GruntspudCheckBox defaultBranch;
    private GruntspudCheckBox doNotRecurse;
    private GruntspudCheckBox headerOnly;
    private GruntspudCheckBox headerAndDescOnly;
    private GruntspudCheckBox noTags;
    private GruntspudCheckBox enabled;

    public LogOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new LogGeneralTab();
        filterTab = new LogFilterTab( context, UIUtil.getCachedIcon(Constants.ICON_TOOL_FILTER), UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_FILTER), "Filter the log output", 'f');
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(generalTab);
        addTab(filterTab);
        addTab(globalOptionsTab);
    }
    
    public Command[] getCommandsForSettings() {
        LogCommand cmd = new LogCommand();
        cmd.setDefaultBranch(defaultBranch.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());
        cmd.setHeaderOnly(headerOnly.isSelected());
        cmd.setHeaderAndDescOnly(!headerOnly.isSelected() &&
                                 headerAndDescOnly.isSelected());
        cmd.setNoTags(noTags.isSelected());
        filterTab.setCommandForSettings(cmd);
        return new Command[] {
            cmd};
    }

    class LogGeneralTab
        extends AbstractTab
        implements ActionListener {
        LogGeneralTab() {
            super("Log", UIUtil.getCachedIcon(Constants.ICON_TOOL_LOG));
            setTabToolTipText("Show the logs for the selection");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_LOG));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            doNotRecurse = new GruntspudCheckBox("Do not recurse directories (-l)");
            doNotRecurse.setMnemonic('d');
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.LOG_GENERAL_DO_NOT_RECURSE,
                false));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);

            defaultBranch = new GruntspudCheckBox("Log on to the default branch (-b)");
            defaultBranch.setMnemonic('f');
            defaultBranch.setSelected(context.getHost().getBooleanProperty(
                Constants.LOG_GENERAL_DEFAULT_BRANCH,
                false));
            UIUtil.jGridBagAdd(this, defaultBranch, gbc,
                               GridBagConstraints.REMAINDER);

            headerOnly = new GruntspudCheckBox("Header only");
            headerOnly.setMnemonic('h');
            headerOnly.setSelected(context.getHost().getBooleanProperty(
                Constants.LOG_GENERAL_HEADER_ONLY,
                false));
            UIUtil.jGridBagAdd(this, headerOnly, gbc,
                               GridBagConstraints.REMAINDER);
            headerOnly.addActionListener(this);

            headerAndDescOnly = new GruntspudCheckBox("Header and description only");
            headerAndDescOnly.setMnemonic('d');
            headerAndDescOnly.setSelected(context.getHost().getBooleanProperty(
                Constants.LOG_GENERAL_HEADER_AND_DESC_ONLY,
                false));
            UIUtil.jGridBagAdd(this, headerAndDescOnly, gbc,
                               GridBagConstraints.REMAINDER);
            headerAndDescOnly.addActionListener(this);

            gbc.weighty = 1.0;
            noTags = new GruntspudCheckBox("No tags only (-n)");
            noTags.setMnemonic('d');
            noTags.setSelected(context.getHost().getBooleanProperty(Constants.
                LOG_GENERAL_NO_TAGS,
                false));
            UIUtil.jGridBagAdd(this, noTags, gbc, GridBagConstraints.REMAINDER);

            setAvailableActions();
        }

        public void actionPerformed(ActionEvent evt) {
            setAvailableActions();
        }

        private void setAvailableActions() {
            headerOnly.setEnabled(!headerAndDescOnly.isSelected());
            headerAndDescOnly.setEnabled(!headerOnly.isSelected());
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 LOG_GENERAL_DO_NOT_RECURSE,
                                                 doNotRecurse.isSelected());
            context.getHost().setBooleanProperty(Constants.
                                                 LOG_GENERAL_DEFAULT_BRANCH,
                                                 defaultBranch.isSelected());
            context.getHost().setBooleanProperty(Constants.
                                                 LOG_GENERAL_HEADER_ONLY,
                                                 headerOnly.isSelected());
            context.getHost().setBooleanProperty(Constants.
                LOG_GENERAL_HEADER_AND_DESC_ONLY,
                headerAndDescOnly.isSelected());
            context.getHost().setBooleanProperty(Constants.LOG_GENERAL_NO_TAGS,
                                                 noTags.isSelected());
        }

        public void tabSelected() {
        }
    }
}
