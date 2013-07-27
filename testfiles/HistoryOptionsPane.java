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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.history.HistoryCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class HistoryOptionsPane
    extends Tabber {
    private HistoryReportTab reportTab;
    private HistoryOptionsTab optionsTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudRadioButton reportEverything;
    private GruntspudRadioButton reportCheckouts;
    private GruntspudRadioButton reportCommits;
    private GruntspudRadioButton reportTags;
    private GruntspudRadioButton reportModules;
    private GruntspudRadioButton reportEventType;
    private GruntspudCheckBox reportEventTypeRelease;
    private GruntspudCheckBox reportEventTypeCheckout;
    private GruntspudCheckBox reportEventTypeTag;
    private GruntspudCheckBox reportEventTypeExport;
    private GruntspudCheckBox forAllUsers;
    private GruntspudCheckBox lastEventOfProject;
    private StringListComboBox modules;
    private StringListComboBox forUsers;
    private StringListComboBox sinceDate;
    private StringListComboBox sinceRevision;
    private StringListComboBox sinceTag;
    private StringListComboBox timezone;
    private StringListComboBox containsString;
    private StringListComboBox lastEventForFile;

    /**
     *  Constructor for the LogOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public HistoryOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        reportTab = new HistoryReportTab();
        optionsTab = new HistoryOptionsTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(reportTab);
        addTab(optionsTab);
        addTab(globalOptionsTab);
    }

    /**
     *  Gets the commandsForSettings attribute of the CommitInfoPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        HistoryCommand cmd = new HistoryCommand();
        cmd.setForWorkingDirectory(true);

        //  Report type
        if (reportEverything.isSelected()) {
            cmd.setReportEverything(true);
        }
        else if (reportCheckouts.isSelected()) {
            cmd.setReportCheckouts(true);
        }
        else if (reportCommits.isSelected()) {
            cmd.setReportCommits(true);
        }
        else if (reportTags.isSelected()) {
            cmd.setReportTags(true);
        }
        else if (reportModules.isSelected() &&
                 (modules.getSelectedItem() != null) &&
                 (modules.getSelectedItem().toString().length() > 0)) {
            cmd.setReportOnModule(tokenize(modules.getSelectedItem().toString()
                                           .trim(), " "));
        }
        else if (reportEventType.isSelected()) {
            StringBuffer buf = new StringBuffer();

            if (reportEventTypeRelease.isSelected()) {
                buf.append("F");

            }
            if (reportEventTypeCheckout.isSelected()) {
                buf.append("O");

            }
            if (reportEventTypeExport.isSelected()) {
                buf.append("E");

            }
            if (reportEventTypeTag.isSelected()) {
                buf.append("T");

            }
            cmd.setReportEventType(buf.toString());
        }

        if (!forAllUsers.isSelected() && (forUsers.getSelectedItem() != null) &&
            (forUsers.getSelectedItem().toString().length() > 0)) {
            cmd.setForUsers(tokenize(forUsers.getSelectedItem().toString().trim(),
                                     " "));
        }
        else {
            cmd.setForAllUsers(true);

        }
        if ( (sinceDate.getSelectedItem() != null) &&
            (sinceDate.getSelectedItem().toString().length() > 0)) {
            cmd.setSinceDate(sinceDate.getSelectedItem().toString().trim());

        }
        if ( (sinceRevision.getSelectedItem() != null) &&
            (sinceRevision.getSelectedItem().toString().length() > 0)) {
            cmd.setSinceRevision(sinceRevision.getSelectedItem().toString()
                                 .trim());

        }
        if ( (sinceTag.getSelectedItem() != null) &&
            (sinceTag.getSelectedItem().toString().length() > 0)) {
            cmd.setSinceTag(sinceTag.getSelectedItem().toString().trim());

        }
        if ( (timezone.getSelectedItem() != null) &&
            (timezone.getSelectedItem().toString().length() > 0)) {
            cmd.setTimeZone(timezone.getSelectedItem().toString().trim());

        }
        if ( (sinceDate.getSelectedItem() != null) &&
            (sinceDate.getSelectedItem().toString().length() > 0)) {
            cmd.setSinceDate(sinceDate.getSelectedItem().toString().trim());

        }
        if ( (containsString.getSelectedItem() != null) &&
            (containsString.getSelectedItem().toString().length() > 0)) {
            cmd.setShowBackToRecordContaining(containsString.getSelectedItem()
                                              .toString().trim());

        }
        cmd.setLastEventOfProject(lastEventOfProject.isSelected());

        if ( (lastEventForFile.getSelectedItem() != null) &&
            (lastEventForFile.getSelectedItem().toString().length() > 0)) {
            cmd.setLastEventForFile(tokenize(lastEventForFile.getSelectedItem()
                                             .toString().trim(),
                                             " "));

        }
        return new Command[] {
            cmd};
    }

    private String[] tokenize(String string, String delim) {
        StringTokenizer s = new StringTokenizer(string.trim(), delim);
        String[] m = new String[s.countTokens()];

        for (int i = 0; i < m.length; i++) {
            m[i] = s.nextToken();

        }
        return m;
    }

    class HistoryReportTab
        extends AbstractTab
        implements ActionListener {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        HistoryReportTab() {
            super("Report", UIUtil.getCachedIcon(Constants.ICON_TOOL_HISTORY));
            setTabToolTipText("Report type options");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_HISTORY));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            Insets i2 = new Insets(3, 24, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            ButtonGroup bg = new ButtonGroup();

            gbc.weightx = 1.0;
            reportEverything = new GruntspudRadioButton("Everything (-e)");
            bg.add(reportEverything);
            reportEverything.setMnemonic('e');
            reportEverything.addActionListener(this);
            reportEverything.setSelected(context.getHost()
                                         .getProperty(Constants.
                HISTORY_REPORT_TYPE,
                Constants.HISTORY_REPORT_TYPE_EVERYTHING).equals(Constants.
                HISTORY_REPORT_TYPE_EVERYTHING));
            UIUtil.jGridBagAdd(this, reportEverything, gbc,
                               GridBagConstraints.REMAINDER);

            reportCheckouts = new GruntspudRadioButton("Checkouts (-o)");
            bg.add(reportCheckouts);
            reportCheckouts.setMnemonic('c');
            reportCheckouts.addActionListener(this);
            reportCheckouts.setSelected(context.getHost()
                                        .getProperty(Constants.
                HISTORY_REPORT_TYPE,
                Constants.HISTORY_REPORT_TYPE_EVERYTHING).equals(Constants.
                HISTORY_REPORT_TYPE_CHECKOUTS));
            UIUtil.jGridBagAdd(this, reportCheckouts, gbc,
                               GridBagConstraints.REMAINDER);

            reportCommits = new GruntspudRadioButton("Commits (-c)");
            bg.add(reportCommits);
            reportCommits.setMnemonic('o');
            reportCommits.addActionListener(this);
            reportCommits.setSelected(context.getHost()
                                      .getProperty(Constants.
                HISTORY_REPORT_TYPE,
                Constants.HISTORY_REPORT_TYPE_EVERYTHING).equals(Constants.
                HISTORY_REPORT_TYPE_COMMITS));
            UIUtil.jGridBagAdd(this, reportCommits, gbc,
                               GridBagConstraints.REMAINDER);

            reportTags = new GruntspudRadioButton("All tags (-T)");
            bg.add(reportTags);
            reportTags.setMnemonic('o');
            reportTags.addActionListener(this);
            reportTags.setSelected(context.getHost()
                                   .getProperty(Constants.HISTORY_REPORT_TYPE,
                                                Constants.
                                                HISTORY_REPORT_TYPE_EVERYTHING).
                                   equals(Constants.HISTORY_REPORT_TYPE_TAGS));
            UIUtil.jGridBagAdd(this, reportTags, gbc,
                               GridBagConstraints.REMAINDER);

            reportModules = new GruntspudRadioButton("Modules (-m)");
            bg.add(reportModules);
            reportModules.setMnemonic('o');
            reportModules.addActionListener(this);
            reportModules.setToolTipText("Space separated list of modules");
            reportModules.setSelected(context.getHost()
                                      .getProperty(Constants.
                HISTORY_REPORT_TYPE,
                Constants.HISTORY_REPORT_TYPE_EVERYTHING).equals(Constants.
                HISTORY_REPORT_TYPE_MODULES));
            UIUtil.jGridBagAdd(this, reportModules, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.insets = i2;
            modules = new StringListComboBox(context,
                                             context.getHost().getProperty(
                Constants.HISTORY_REPORT_MODULES,
                ""), false);
            UIUtil.jGridBagAdd(this, modules, gbc, GridBagConstraints.REMAINDER);

            gbc.insets = i1;
            reportEventType = new GruntspudRadioButton("Event type (-m)");
            bg.add(reportEventType);
            reportEventType.addActionListener(this);
            reportEventType.setMnemonic('e');
            reportEventType.setSelected(context.getHost()
                                        .getProperty(Constants.
                HISTORY_REPORT_TYPE,
                Constants.HISTORY_REPORT_TYPE_EVERYTHING).equals(Constants.
                HISTORY_REPORT_TYPE_EVENT_TYPES));
            UIUtil.jGridBagAdd(this, reportEventType, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.insets = i2;
            reportEventTypeRelease = new GruntspudCheckBox("Release (F)");
            reportEventTypeRelease.setSelected(context.getHost()
                                               .getBooleanProperty(Constants.
                HISTORY_REPORT_RELEASE_EVENT_TYPE,
                true));
            UIUtil.jGridBagAdd(this, reportEventTypeRelease, gbc,
                               GridBagConstraints.REMAINDER);

            reportEventTypeCheckout = new GruntspudCheckBox("Checkout (O)");
            reportEventTypeCheckout.setSelected(context.getHost()
                                                .getBooleanProperty(Constants.
                HISTORY_REPORT_CHECKOUT_EVENT_TYPE,
                true));
            UIUtil.jGridBagAdd(this, reportEventTypeCheckout, gbc,
                               GridBagConstraints.REMAINDER);

            reportEventTypeExport = new GruntspudCheckBox("Export (E)");
            reportEventTypeExport.setSelected(context.getHost()
                                              .getBooleanProperty(Constants.
                HISTORY_REPORT_EXPORT_EVENT_TYPE,
                true));
            UIUtil.jGridBagAdd(this, reportEventTypeExport, gbc,
                               GridBagConstraints.REMAINDER);

            reportEventTypeTag = new GruntspudCheckBox("Tag (T)");
            reportEventTypeTag.setSelected(context.getHost().getBooleanProperty(
                Constants.HISTORY_REPORT_TAG_EVENT_TYPE,
                true));
            UIUtil.jGridBagAdd(this, reportEventTypeTag, gbc,
                               GridBagConstraints.REMAINDER);

            setAvailableActions();
        }

        public void actionPerformed(ActionEvent evt) {
            setAvailableActions();
        }

        private void setAvailableActions() {
            reportEventTypeCheckout.setEnabled(reportEventType.isSelected());
            reportEventTypeExport.setEnabled(reportEventType.isSelected());
            reportEventTypeRelease.setEnabled(reportEventType.isSelected());
            reportEventTypeTag.setEnabled(reportEventType.isSelected());
            modules.setEnabled(reportModules.isSelected());
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            String t = Constants.HISTORY_REPORT_TYPE_EVERYTHING;

            if (reportCheckouts.isSelected()) {
                t = Constants.HISTORY_REPORT_TYPE_CHECKOUTS;
            }
            else if (reportCommits.isSelected()) {
                t = Constants.HISTORY_REPORT_TYPE_COMMITS;
            }
            else if (reportTags.isSelected()) {
                t = Constants.HISTORY_REPORT_TYPE_TAGS;
            }
            else if (reportModules.isSelected()) {
                t = Constants.HISTORY_REPORT_TYPE_MODULES;
                context.getHost().setProperty(Constants.HISTORY_REPORT_MODULES,
                                              modules.
                                              getStringListPropertyString());
            }
            else if (reportEventType.isSelected()) {
                t = Constants.HISTORY_REPORT_TYPE_MODULES;
                context.getHost().setBooleanProperty(Constants.
                    HISTORY_REPORT_CHECKOUT_EVENT_TYPE,
                    reportEventTypeCheckout.isSelected());
                context.getHost().setBooleanProperty(Constants.
                    HISTORY_REPORT_EXPORT_EVENT_TYPE,
                    reportEventTypeExport.isSelected());
                context.getHost().setBooleanProperty(Constants.
                    HISTORY_REPORT_RELEASE_EVENT_TYPE,
                    reportEventTypeRelease.isSelected());
                context.getHost().setBooleanProperty(Constants.
                    HISTORY_REPORT_TAG_EVENT_TYPE,
                    reportEventTypeTag.isSelected());
            }

            context.getHost().setProperty(Constants.HISTORY_REPORT_TYPE, t);
        }

        public void tabSelected() {
        }
    }

    class HistoryOptionsTab
        extends AbstractTab
        implements ActionListener {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        HistoryOptionsTab() {
            super("Options", UIUtil.getCachedIcon(Constants.ICON_TOOL_HISTORY));
            setTabToolTipText("Options for the report");
            setLayout(new GridBagLayout());
            setTabMnemonic('l');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_HISTORY));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            Insets i2 = new Insets(3, 24, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.weightx = 1.0;
            lastEventOfProject = new GruntspudCheckBox("Last event of project");
            lastEventOfProject.setMnemonic('a');
            lastEventOfProject.addActionListener(this);
            lastEventOfProject.setSelected(context.getHost().getBooleanProperty(
                Constants.HISTORY_OPTIONS_LAST_EVENT_OF_PROJECT,
                false));
            UIUtil.jGridBagAdd(this, lastEventOfProject, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Last event for file "), gbc,
                               GridBagConstraints.RELATIVE);
            lastEventForFile = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                HISTORY_OPTIONS_LAST_EVENT_FOR_FILE,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, lastEventForFile, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 1.0;
            forAllUsers = new GruntspudCheckBox("For all users");
            forAllUsers.setMnemonic('a');
            forAllUsers.addActionListener(this);
            forAllUsers.setSelected(context.getHost().getBooleanProperty(
                Constants.HISTORY_OPTIONS_FOR_ALL_USERS,
                true));
            UIUtil.jGridBagAdd(this, forAllUsers, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            gbc.insets = i2;
            UIUtil.jGridBagAdd(this, new JLabel("Just these users "), gbc,
                               GridBagConstraints.RELATIVE);
            forUsers = new StringListComboBox(context,
                                              context.getHost().getProperty(
                Constants.HISTORY_OPTIONS_FOR_USERS,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, forUsers, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            gbc.insets = i1;
            UIUtil.jGridBagAdd(this, new JLabel("Since date "), gbc,
                               GridBagConstraints.RELATIVE);
            sinceDate = new StringListComboBox(context,
                                               context.getHost().getProperty(
                Constants.HISTORY_OPTIONS_SINCE_DATE,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, sinceDate, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Since revision "), gbc,
                               GridBagConstraints.RELATIVE);
            sinceRevision = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              HISTORY_OPTIONS_SINCE_REVISION,
                                              ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, sinceRevision, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Since tag "), gbc,
                               GridBagConstraints.RELATIVE);
            sinceTag = new StringListComboBox(context,
                                              context.getHost().getProperty(
                Constants.HISTORY_OPTIONS_SINCE_TAG,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, sinceTag, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Timezone "), gbc,
                               GridBagConstraints.RELATIVE);
            timezone = new StringListComboBox(context,
                                              context.getHost().getProperty(
                Constants.HISTORY_OPTIONS_TIMEZONE,
                ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, timezone, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            gbc.weighty = 1.0;
            UIUtil.jGridBagAdd(this, new JLabel("Contains string "), gbc,
                               GridBagConstraints.RELATIVE);
            containsString = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              HISTORY_OPTIONS_CONTAINS_STRING,
                                              ""), false);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this, containsString, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;

            setAvailableActions();
        }

        public void actionPerformed(ActionEvent evt) {
            setAvailableActions();
        }

        private void setAvailableActions() {
            forUsers.setEnabled(!forAllUsers.isSelected());
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 HISTORY_OPTIONS_FOR_ALL_USERS,
                                                 forAllUsers.isSelected());

            if (forAllUsers.isSelected()) {
                context.getHost().setProperty(Constants.
                                              HISTORY_OPTIONS_FOR_USERS,
                                              forUsers.
                                              getStringListPropertyString());

            }
            context.getHost().setProperty(Constants.HISTORY_OPTIONS_SINCE_DATE,
                                          sinceDate.getStringListPropertyString());
            context.getHost().setProperty(Constants.
                                          HISTORY_OPTIONS_SINCE_REVISION,
                                          sinceRevision.
                                          getStringListPropertyString());
            context.getHost().setProperty(Constants.HISTORY_OPTIONS_SINCE_TAG,
                                          sinceTag.getStringListPropertyString());
            context.getHost().setProperty(Constants.HISTORY_OPTIONS_TIMEZONE,
                                          timezone.getStringListPropertyString());
            context.getHost().setProperty(Constants.
                                          HISTORY_OPTIONS_CONTAINS_STRING,
                                          containsString.
                                          getStringListPropertyString());
            context.getHost().setBooleanProperty(Constants.
                HISTORY_OPTIONS_LAST_EVENT_OF_PROJECT,
                lastEventOfProject.isSelected());
            context.getHost().setProperty(Constants.
                                          HISTORY_OPTIONS_LAST_EVENT_FOR_FILE,
                                          lastEventForFile.
                                          getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }
}
