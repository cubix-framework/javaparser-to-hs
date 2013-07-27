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
import gruntspud.ui.MessageTab;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.commit.CommitCommand;

/**
 * Description of the Class
 * 
 * @author magicthize
 * @created 26 May 2002
 */
public class CommitOptionsPane extends Tabber {
    private JCheckBox doNotRecurse;
    private JCheckBox noModuleProgram;
    private JCheckBox forceCommit;
    private JCheckBox usePrevious;
    private MessageTab messageTab;
    private CommitGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private StringListComboBox revisionOrBranch;
    private StringListComboBox previous;

    /**
     * Constructor for the CommitInfoPane object
     * 
     * @param host
     *            Description of the Parameter
     */
    public CommitOptionsPane(GruntspudContext context) {
        super();
        this.context = context;
        messageTab = new MessageTab("Message to log for the commit", UIUtil.getCachedIcon(Constants.ICON_TOOL_NOTES), UIUtil
                        .getCachedIcon(Constants.ICON_TOOL_LARGE_NOTES), 25);
        messageTab.init(context);
        generalTab = new CommitGeneralTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(messageTab);
        addTab(generalTab);
        addTab(globalOptionsTab);
    }

    /**
     * Gets the commandsForSettings attribute of the CommitInfoPane object
     * 
     * @return The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        CommitCommand cmd = new CommitCommand();
        cmd.setMessage(messageTab.getText().trim().equals("") ? "no message" : messageTab.getText());
        cmd.setForceCommit(forceCommit.isSelected());
        cmd.setNoModuleProgram(noModuleProgram.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());

        if ((revisionOrBranch.getSelectedItem() != null) && (((String) revisionOrBranch.getSelectedItem()).length() > 0)) {
            cmd.setToRevisionOrBranch((String) revisionOrBranch.getSelectedItem());

        }
        return new Command[]{cmd};
    }

    class CommitGeneralTab extends AbstractTab {
        /**
         * Constructor for the CommitGeneralTab object
         */
        CommitGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_COMMIT));
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_COMMIT));
            setTabToolTipText("General options for the commit");
            setLayout(new GridBagLayout());
            setTabMnemonic('g');

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse directories (-l)");
            doNotRecurse.setMnemonic('d');
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(Constants.COMMIT_GENERAL_DO_NOT_RECURSE, false));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc, GridBagConstraints.REMAINDER);
            gbc.weightx = 2.0;
            forceCommit = new GruntspudCheckBox("Force commit (-f)");
            forceCommit.setMnemonic('f');
            forceCommit.setSelected(false);
            UIUtil.jGridBagAdd(this, forceCommit, gbc, GridBagConstraints.REMAINDER);
            gbc.weightx = 2.0;
            noModuleProgram = new GruntspudCheckBox("No module programs (-n)");
            noModuleProgram.setMnemonic('n');
            noModuleProgram.setSelected(context.getHost().getBooleanProperty(Constants.COMMIT_GENERAL_NO_MODULE_PROGRAM, false));
            UIUtil.jGridBagAdd(this, noModuleProgram, gbc, GridBagConstraints.REMAINDER);
            gbc.weighty = 1.0;
            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Revision/Branch"), gbc, GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, revisionOrBranch = new StringListComboBox(context, context.getHost().getProperty(
                            Constants.COMMIT_GENERAL_REVISION_OR_BRANCH, ""), true), gbc, GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.COMMIT_GENERAL_DO_NOT_RECURSE, doNotRecurse.isSelected());
            context.getHost().setBooleanProperty(Constants.COMMIT_GENERAL_NO_MODULE_PROGRAM, noModuleProgram.isSelected());
            context.getHost().setProperty(Constants.COMMIT_GENERAL_REVISION_OR_BRANCH,
                            revisionOrBranch.getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }
}