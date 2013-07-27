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
import gruntspud.ui.UIUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class OutputOptionsTab
    extends AbstractOptionsTab {
    private JCheckBox outputToConsole;
    private JCheckBox outputToReportDialog;
    private GruntspudContext context;

    /**
     *  Constructor for the GlobalOptionsTab object
     *
     *@param  host  Description of the Parameter
     */
    public OutputOptionsTab() {
        super("Output", UIUtil.getCachedIcon(Constants.ICON_TOOL_OUTPUT));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        this.context = context;

        setTabToolTipText("Options for the way any responses are presented.");
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_OUTPUT));
        setLayout(new GridBagLayout());
        setTabMnemonic('a');
        setTabContext("UI");

        //  General output options
        outputToConsole = new JCheckBox("Output to console",
                                        context.getHost().getBooleanProperty(
            Constants.OPTIONS_OUTPUT_TO_CONSOLE,
            true));
        outputToConsole.setMnemonic('c');
        outputToReportDialog = new JCheckBox("Output to report dialog",
                                             context.getHost().
                                             getBooleanProperty(Constants.
            OPTIONS_OUTPUT_TO_REPORT_DIALOG,
            true));
        outputToReportDialog.setMnemonic('r');

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 2.0;

        UIUtil.jGridBagAdd(this, outputToConsole, gbc,
                           GridBagConstraints.REMAINDER);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, outputToReportDialog, gbc,
                           GridBagConstraints.REMAINDER);
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
        context.getHost().setBooleanProperty(Constants.
                                             OPTIONS_OUTPUT_TO_CONSOLE,
                                             outputToConsole.isSelected());
        context.getHost().setBooleanProperty(Constants.
                                             OPTIONS_OUTPUT_TO_REPORT_DIALOG,
                                             outputToReportDialog.isSelected());
    }
}
