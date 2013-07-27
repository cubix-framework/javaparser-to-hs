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

package gruntspud.ui.preferences;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.UIUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class GlobalOptionsTab
    extends AbstractOptionsTab
    implements ActionListener {
    private GruntspudCheckBox checkoutReadOnly;
    private GruntspudCheckBox doNoChanges;
    private GruntspudCheckBox useGzip;
    private GruntspudCheckBox noHistoryLogging;
    private GruntspudCheckBox traceExecution;
    private GruntspudCheckBox veryQuiet;
    private GruntspudCheckBox moderatelyQuiet;

    /**
     *  Constructor for the GlobalOptionsTab object
     */
    public GlobalOptionsTab() {
        super("Global", UIUtil.getCachedIcon(Constants.ICON_TOOL_GLOBAL));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);
        setTabToolTipText("Options that are common to most or all " +
                          "CVS commands.");
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_GLOBAL));
        setLayout(new GridBagLayout());
        setTabMnemonic('a');
        setTabContext("General");

        checkoutReadOnly = new GruntspudCheckBox("Checkout read only",
                                         context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_CHECKOUT_READ_ONLY));
        doNoChanges = new GruntspudCheckBox("Do no changes",
                                    context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_DO_NO_CHANGES));
        useGzip = new GruntspudCheckBox("Compression",
                                context.getHost().getBooleanProperty(Constants.
            OPTIONS_GLOBAL_USE_GZIP));
        noHistoryLogging = new GruntspudCheckBox("No history logging",
                                         context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_NO_HISTORY_LOGGING));
        traceExecution = new GruntspudCheckBox("Trace execution",
                                       context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_TRACE_EXECUTION));
        moderatelyQuiet = new GruntspudCheckBox("Moderately quiet",
                                        context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_MODERATELY_QUIET));
        veryQuiet = new GruntspudCheckBox("Very quiet",
                                  context.getHost().getBooleanProperty(
            Constants.OPTIONS_GLOBAL_VERY_QUIET));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 3.0;
        UIUtil.jGridBagAdd(this, checkoutReadOnly, gbc,
                           GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, doNoChanges, gbc, GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, useGzip, gbc, GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, noHistoryLogging, gbc,
                           GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, traceExecution, gbc,
                           GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, moderatelyQuiet, gbc,
                           GridBagConstraints.REMAINDER);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, veryQuiet, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateTab() {
        return true;
    }

    /**
     *  Description of the Method
     */
    public void tabSelected() {
    }

    /**
     *  Description of the Method
     */
    public void applyTab() {
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_GLOBAL_CHECKOUT_READ_ONLY,
            checkoutReadOnly.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
                                                  OPTIONS_GLOBAL_DO_NO_CHANGES,
                                                  doNoChanges.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
                                                  OPTIONS_GLOBAL_USE_GZIP,
                                                  useGzip.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_GLOBAL_NO_HISTORY_LOGGING,
            noHistoryLogging.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_GLOBAL_TRACE_EXECUTION,
            traceExecution.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_GLOBAL_MODERATELY_QUIET,
            moderatelyQuiet.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
                                                  OPTIONS_GLOBAL_VERY_QUIET,
                                                  veryQuiet.isSelected());
    }
}
