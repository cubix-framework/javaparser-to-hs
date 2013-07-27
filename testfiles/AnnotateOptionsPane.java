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

import javax.swing.ButtonGroup;
import javax.swing.JLabel;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class AnnotateOptionsPane
    extends Tabber {
    private AnnotateGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudCheckBox doNotRecurse;
    private GruntspudCheckBox useHeadIfNotFound;
    private StringListComboBox date;
    private StringListComboBox revision;

    /**
     *  Constructor for the LogOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public AnnotateOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new AnnotateGeneralTab();
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
        AnnotateCommand cmd = new AnnotateCommand();

        if ( (date.getSelectedItem() != null) &&
            (date.getSelectedItem().toString().length() > 0)) {
            cmd.setAnnotateByDate(date.getSelectedItem().toString());

        }
        if ( (revision.getSelectedItem() != null) &&
            (revision.getSelectedItem().toString().length() > 0)) {
            cmd.setAnnotateByRevision(revision.getSelectedItem().toString());

        }
        cmd.setUseHeadIfNotFound(useHeadIfNotFound.isSelected());
        cmd.setRecursive(!doNotRecurse.isSelected());

        return new Command[] {
            cmd};
    }

    class AnnotateGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        AnnotateGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_ANNOTATE));
            setTabToolTipText("General annotate options");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_ANNOTATE));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            ButtonGroup bg = new ButtonGroup();

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("By Date "), gbc,
                               GridBagConstraints.RELATIVE);
            date = new StringListComboBox(context,
                                          context.getHost().getProperty(
                Constants.ANNOTATE_GENERAL_BY_DATE,
                ""), true);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, date, gbc, GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("By Revision "), gbc,
                               GridBagConstraints.RELATIVE);
            revision = new StringListComboBox(context,
                                              context.getHost().getProperty(
                Constants.ANNOTATE_GENERAL_BY_REVISION,
                ""), true);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, revision, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            useHeadIfNotFound = new GruntspudCheckBox("Use HEAD if not found");
            useHeadIfNotFound.setSelected(context.getHost().getBooleanProperty(
                Constants.ANNOTATE_GENERAL_USE_HEAD_IF_NOT_FOUND));
            UIUtil.jGridBagAdd(this, useHeadIfNotFound, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.ANNOTATE_GENERAL_DO_NOT_RECURSE));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setProperty(Constants.ANNOTATE_GENERAL_BY_DATE,
                                          date.getStringListPropertyString());
            context.getHost().setProperty(Constants.
                                          ANNOTATE_GENERAL_BY_REVISION,
                                          revision.getStringListPropertyString());
            context.getHost().setBooleanProperty(Constants.
                ANNOTATE_GENERAL_DO_NOT_RECURSE,
                doNotRecurse.isSelected());
            context.getHost().setBooleanProperty(Constants.
                ANNOTATE_GENERAL_USE_HEAD_IF_NOT_FOUND,
                useHeadIfNotFound.isSelected());
        }

        public void tabSelected() {
        }
    }
}
