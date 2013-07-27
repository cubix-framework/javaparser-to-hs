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
public class TagOptionsPane
    extends Tabber {
    private TagGeneralTab generalTab;
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
    public TagOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new TagGeneralTab();
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
        cmd.setCheckThatUnmodified(checkUnmodified.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());
        cmd.setMakeBranchTag(branchTag.isSelected());
        cmd.setOverrideExistingTag(overideExistingTag.isSelected());
        cmd.setTag(tag.getSelectedItem().toString());

        if ( (tagByDate.getSelectedItem() != null) &&
            (tagByDate.getSelectedItem().toString().length() > 0)) {
            cmd.setTagByDate(tagByDate.getSelectedItem().toString());
        }
        else if ( (tagByRevision.getSelectedItem() != null) &&
                 (tagByRevision.getSelectedItem().toString().length() > 0)) {
            cmd.setTagByDate(tagByRevision.getSelectedItem().toString());

        }
        return new Command[] {
            cmd};
    }

    class TagGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        TagGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_TAG));
            setTabToolTipText("General tag options");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_TAG));

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
            checkUnmodified = new GruntspudCheckBox("Check unmodified");
            checkUnmodified.setSelected(context.getHost().getBooleanProperty(
                Constants.TAG_GENERAL_CHECK_UNMODIFIED));
            UIUtil.jGridBagAdd(this, checkUnmodified, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            branchTag = new GruntspudCheckBox("Branch tag");            
            UIUtil.jGridBagAdd(this, branchTag, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            overideExistingTag = new GruntspudCheckBox("Overide existing tags");
            overideExistingTag.setSelected(context.getHost().getBooleanProperty(
                Constants.TAG_GENERAL_OVERIDE_EXISTING_TAG));
            UIUtil.jGridBagAdd(this, overideExistingTag, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.TAG_GENERAL_DO_NO_RECURSE));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("By Date "), gbc,
                               GridBagConstraints.RELATIVE);
            tagByDate = new StringListComboBox(context,
                                               context.getHost().getProperty(
                Constants.TAG_GENERAL_TAG_BY_DATE,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, tagByDate, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            UIUtil.jGridBagAdd(this, new JLabel("By Revision "), gbc,
                               GridBagConstraints.RELATIVE);
            tagByRevision = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              TAG_GENERAL_TAG_BY_REVISION,
                                              ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, tagByRevision, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 TAG_GENERAL_DO_NO_RECURSE,
                                                 doNotRecurse.isSelected());
            context.getHost().setBooleanProperty(Constants.
                                                 TAG_GENERAL_CHECK_UNMODIFIED,
                                                 checkUnmodified.isSelected());
            context.getHost().setBooleanProperty(Constants.
                TAG_GENERAL_OVERIDE_EXISTING_TAG,
                overideExistingTag.isSelected());
            context.getHost().setProperty(Constants.TAG_GENERAL_TAG,
                                          tag.getStringListPropertyString());
            context.getHost().setProperty(Constants.TAG_GENERAL_TAG_BY_DATE,
                                          tagByDate.getStringListPropertyString());
            context.getHost().setProperty(Constants.TAG_GENERAL_TAG_BY_REVISION,
                                          tagByRevision.
                                          getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }
}
