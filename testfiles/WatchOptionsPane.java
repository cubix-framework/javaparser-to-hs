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
import gruntspud.ui.WatchModeComboBox;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.Watch;
import org.netbeans.lib.cvsclient.command.watch.WatchCommand;
import org.netbeans.lib.cvsclient.command.watch.WatchMode;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class WatchOptionsPane
    extends Tabber {
    private WatchGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private GruntspudCheckBox doNotRecurse;
    private WatchList watch;
    private WatchModeComboBox watchMode;

    /**
     *  Constructor for the EditOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public WatchOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        generalTab = new WatchGeneralTab();
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
        WatchCommand cmd = new WatchCommand();
        cmd.setRecursive(!doNotRecurse.isSelected());

        if (watch.getSelectedWatch() != null) {
            cmd.setWatch(watch.getSelectedWatch());

        }
        if (watchMode.getSelectedWatchMode() != null) {
            cmd.setWatchMode(watchMode.getSelectedWatchMode());

        }
        return new Command[] {
            cmd};
    }

    class WatchGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the WatchGeneralTab object
         */
        WatchGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_WATCH));
            setTabToolTipText("General watch options");
            setLayout(new GridBagLayout());
            setTabMnemonic('w');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_WATCH));

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.BOTH;

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Mode "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            watchMode = new WatchModeComboBox();
            watchMode.setSelectedWatchModeName(context.getHost().getProperty(
                Constants.WATCH_GENERAL_WATCH_MODE,
                WatchMode.OFF.toString()));
            UIUtil.jGridBagAdd(this, watchMode, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            UIUtil.jGridBagAdd(this, new JSeparator(JSeparator.HORIZONTAL),
                               gbc, GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                Constants.WATCH_GENERAL_DO_NOT_RECURSE,
                false));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;
            watch = new WatchList();
            watch.setSelectedWatchName(context.getHost().getProperty(Constants.
                WATCH_GENERAL_WATCH,
                Watch.NONE.toString()));

            JScrollPane scroller = new JScrollPane(watch);
            JPanel p = new JPanel(new GridLayout(1, 1));
            p.setBorder(BorderFactory.createTitledBorder("Watch type"));
            p.add(scroller);
            p.setOpaque(false);
            UIUtil.jGridBagAdd(this, p, gbc, GridBagConstraints.REMAINDER);

            setAvailableActions();

            watchMode.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    setAvailableActions();
                }
            });
        }

        private void setAvailableActions() {
            watch.setEnabled( (watchMode.getSelectedWatchMode() ==
                               WatchMode.ADD) ||
                             (watchMode.getSelectedWatchMode() ==
                              WatchMode.REMOVE));
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 WATCH_GENERAL_DO_NOT_RECURSE,
                                                 doNotRecurse.isSelected());
            context.getHost().setProperty(Constants.WATCH_GENERAL_WATCH_MODE,
                                          (watchMode.getSelectedWatchModeName() == null) ?
                                          ""
                                          : watchMode.getSelectedWatchModeName());

            String n = watch.getSelectedWatchName();
            context.getHost().setProperty(Constants.WATCH_GENERAL_WATCH,
                                          (n == null) ? "" : n);
        }

        public void tabSelected() {
        }
    }
}
