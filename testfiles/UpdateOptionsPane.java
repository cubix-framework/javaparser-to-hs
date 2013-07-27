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
import gruntspud.ui.GruntspudRadioButton;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.GlobalOptionsTab;
import gruntspud.ui.preferences.StickyOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class UpdateOptionsPane
    extends Tabber {
    private JCheckBox doNotRecurse;
    private JCheckBox resetStickyOnes;
    private JCheckBox pruneDirectories;
    private JCheckBox cleanCopy;
    private JCheckBox disableRCSDiff;
    private JCheckBox buildDirectories;
    private JRadioButton mergeNone;
    private JRadioButton mergeRevisionOrTag1;
    private JCheckBox mergeRevisionOrTag2;
    private StringListComboBox mergeRevisionOrTagName1;
    private StringListComboBox mergeRevisionOrTagName2;
    private UpdateGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private StickyOptionsTab stickyTab;
    private UpdateMergeTab mergeTab;
    private GruntspudContext context;

    /**
     *  Constructor for the UpdateOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public UpdateOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new UpdateGeneralTab();
        stickyTab = new StickyOptionsTab(UIUtil.getCachedIcon(
            Constants.ICON_TOOL_STICKY),
                                         UIUtil.getCachedIcon(Constants.
            ICON_TOOL_LARGE_STICKY), false);
        stickyTab.init(context);
        mergeTab = new UpdateMergeTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(generalTab);
        addTab(stickyTab);
        addTab(mergeTab);
        addTab(globalOptionsTab);
    }

    /**
     *  Gets the commandsForSettings attribute of the UpdateOptionsPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        UpdateCommand cmd = new UpdateCommand();
        cmd.setBuildDirectories(buildDirectories.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());
        cmd.setResetStickyOnes(resetStickyOnes.isSelected());
        cmd.setPruneDirectories(pruneDirectories.isSelected());
        cmd.setCleanCopy(cleanCopy.isSelected());

        String d = stickyTab.getSelectedDate();

        if ( (d != null) && (d.length() != 0)) {
            cmd.setUpdateByDate(d);

        }
        String r = stickyTab.getSelectedRevision();

        if ( (r != null) && (r.length() != 0)) {
            cmd.setUpdateByRevision(r);

        }
        cmd.setUseHeadIfNotFound(stickyTab.isUseHeadIfNotFound());

        if (mergeRevisionOrTag1.isSelected()) {
            String m1 = (String) mergeRevisionOrTagName1.getSelectedItem();

            if ( (m1 != null) && (m1.length() > 0)) {
                cmd.setMergeRevision1(m1);

                String m2 = (String) mergeRevisionOrTagName2.getSelectedItem();

                if ( (m2 != null) && (m2.length() > 0)) {
                    cmd.setMergeRevision1(m2);
                }
            }
        }

        return new Command[] {
            cmd};
    }

    class UpdateGeneralTab
        extends AbstractTab {
        private JButton browse;

        /**
         *  Constructor for the UpdateGeneralTab object
         */
        UpdateGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_UPDATE));
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_UPDATE));
            setTabToolTipText("General options for an update");
            setLayout(new GridBagLayout());
            setTabMnemonic('g');

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse directories (-l)",
                                         context.getHost().getBooleanProperty(
                Constants.DO_NOT_RECURSE,
                false));
            doNotRecurse.setMnemonic('d');
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);
            resetStickyOnes = new GruntspudCheckBox("Reset sticky ones (-l)",
                                            context.getHost().
                                            getBooleanProperty(Constants.
                RESET_STICKY_ONES,
                false));
            resetStickyOnes.setMnemonic('r');
            UIUtil.jGridBagAdd(this, resetStickyOnes, gbc,
                               GridBagConstraints.REMAINDER);
            pruneDirectories = new GruntspudCheckBox("Prune directories (-P)",
                                             context.getHost().
                                             getBooleanProperty(Constants.
                PRUNE_DIRECTORIES,
                true));
            pruneDirectories.setMnemonic('p');
            UIUtil.jGridBagAdd(this, pruneDirectories, gbc,
                               GridBagConstraints.REMAINDER);
            cleanCopy = new GruntspudCheckBox("Get the clean copy", false);
            ;
            cleanCopy.setMnemonic('p');
            UIUtil.jGridBagAdd(this, cleanCopy, gbc,
                               GridBagConstraints.REMAINDER);
            buildDirectories = new GruntspudCheckBox("Create missing directories",
                                             context.getHost().
                                             getBooleanProperty(Constants.
                UPDATE_GENERAL_BUILD_DIRECTORIES,
                false));
            buildDirectories.setMnemonic('b');
            gbc.weighty = 1.0;
            UIUtil.jGridBagAdd(this, buildDirectories, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.DO_NOT_RECURSE,
                                                 doNotRecurse.isSelected());
            context.getHost().setBooleanProperty(Constants.RESET_STICKY_ONES,
                                                 resetStickyOnes.isSelected());
            context.getHost().setBooleanProperty(Constants.PRUNE_DIRECTORIES,
                                                 pruneDirectories.isSelected());
            context.getHost().setBooleanProperty(Constants.
                UPDATE_GENERAL_BUILD_DIRECTORIES,
                buildDirectories.isSelected());
        }

        public void tabSelected() {
        }
    }

    class UpdateMergeTab
        extends AbstractTab
        implements ActionListener {
        /**
         *  Constructor for the UpdateMergeTab object
         */
        UpdateMergeTab() {
            super("Merge",
                  UIUtil.getCachedIcon(Constants.ICON_TOOL_MERGE));
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_MERGE));
            setTabToolTipText("Merge options");
            setLayout(new GridBagLayout());
            setTabMnemonic('g');

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            Insets i2 = new Insets(3, 24, 3, 3);
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;

            ButtonGroup bg = new ButtonGroup();

            UIUtil.jGridBagAdd(this,
                               mergeNone = new GruntspudRadioButton("None",
                ! (context.getHost().getBooleanProperty(Constants.
                UPDATE_MERGE_REVISION_OR_TAG_1,
                true))), gbc, GridBagConstraints.REMAINDER);
            bg.add(mergeNone);
            mergeNone.setMnemonic('n');
            mergeNone.addActionListener(this);

            UIUtil.jGridBagAdd(this,
                               mergeRevisionOrTag1 = new GruntspudRadioButton(
                "Only this revision or tag",
                context.getHost().getBooleanProperty(Constants.
                UPDATE_MERGE_REVISION_OR_TAG_1,
                false)), gbc, GridBagConstraints.REMAINDER);
            bg.add(mergeRevisionOrTag1);
            mergeRevisionOrTag1.setMnemonic('o');
            mergeRevisionOrTag1.addActionListener(this);

            gbc.insets = i2;

            UIUtil.jGridBagAdd(this,
                               mergeRevisionOrTagName1 = new StringListComboBox(
                context,
                context.getHost().getProperty(Constants.
                UPDATE_MERGE_REVISION_OR_TAG_NAME_1,
                ""), true), gbc,
                               GridBagConstraints.REMAINDER);

            gbc.insets = i1;

            UIUtil.jGridBagAdd(this,
                               mergeRevisionOrTag2 = new GruntspudCheckBox(
                ".. and this revision or tag",
                context.getHost().getBooleanProperty(Constants.
                UPDATE_MERGE_REVISION_OR_TAG_2,
                true)), gbc, GridBagConstraints.REMAINDER);
            mergeRevisionOrTag2.setMnemonic('a');
            mergeRevisionOrTag2.addActionListener(this);

            gbc.insets = i2;
            gbc.weighty = 1.0;

            UIUtil.jGridBagAdd(this,
                               mergeRevisionOrTagName2 = new StringListComboBox(
                context,
                context.getHost().getProperty(Constants.
                UPDATE_MERGE_REVISION_OR_TAG_NAME_2,
                ""), true), gbc,
                               GridBagConstraints.REMAINDER);

            setAvailableActions();
        }

        public void actionPerformed(ActionEvent evt) {
            setAvailableActions();
        }

        private void setAvailableActions() {
            mergeRevisionOrTagName1.setEnabled(mergeRevisionOrTag1.isSelected());
            mergeRevisionOrTag2.setEnabled(mergeRevisionOrTag1.isSelected());
            mergeRevisionOrTagName2.setEnabled(mergeRevisionOrTag1.isSelected() &&
                                               mergeRevisionOrTag2.isSelected() &&
                                               (mergeRevisionOrTagName1.
                                                getSelectedItem() != null) &&
                                               (mergeRevisionOrTagName1.
                                                getSelectedItem().toString().
                                                length() > 0));
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 UPDATE_MERGE_REVISION_OR_TAG_1,
                                                 mergeRevisionOrTag1.isSelected());
            context.getHost().setProperty(Constants.
                                          UPDATE_MERGE_REVISION_OR_TAG_NAME_1,
                                          mergeRevisionOrTagName1.
                                          getStringListPropertyString());
            context.getHost().setBooleanProperty(Constants.
                                                 UPDATE_MERGE_REVISION_OR_TAG_2,
                                                 mergeRevisionOrTag2.isSelected());
            context.getHost().setProperty(Constants.
                                          UPDATE_MERGE_REVISION_OR_TAG_NAME_2,
                                          mergeRevisionOrTagName2.
                                          getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }
}
