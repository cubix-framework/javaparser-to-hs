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
import gruntspud.ui.CharSetComboBox;
import gruntspud.ui.FileNameTextField;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.GruntspudCheckBox;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class SystemOptionsTab
    extends AbstractOptionsTab
    implements ActionListener {
    private GruntspudCheckBox debug;
    private GruntspudCheckBox debugToConsole;
    private GruntspudCheckBox hideMenuIcons;
    private GruntspudCheckBox disableNativePermissionHandler;
    private GruntspudCheckBox logCVSIO;
    private XTextField userIgnoreFile, pluginUpdatesURL;
    private GruntspudCheckBox disableKeyboardAccelerators;
    private CharSetComboBox defaultEncoding;

    /**
     *  Constructor for the SystemOptionsTab object
     */
    public SystemOptionsTab() {
        super("System", UIUtil.getCachedIcon(Constants.ICON_TOOL_SYSTEM));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);

        setTabToolTipText("System related options.");
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_SYSTEM));
        setTabMnemonic('s');
        setTabContext("Advanced");

        //  Paths
        JPanel s = new JPanel(new GridBagLayout());
		
		s.setOpaque(false);

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(3, 3, 3, 3);
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;

        gbc1.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("User .cvsignore file:"), gbc1,
                           GridBagConstraints.RELATIVE);
        userIgnoreFile = new FileNameTextField(null,
                                               context.getHost().
                                               getProperty(Constants.
            OPTIONS_SYSTEM_USER_IGNORE_FILE,
            System.getProperty("user.home") + File.separator +
            ".cvsignore"), 0, true, true);
        gbc1.weightx = 1.0;
        UIUtil.jGridBagAdd(s, userIgnoreFile, gbc1,
                           GridBagConstraints.REMAINDER);

        gbc1.weighty = 1.0;
        gbc1.weightx = 0.0;

        pluginUpdatesURL = new XTextField(
            context.getHost().getProperty(Constants.
                                          OPTIONS_SYSTEM_PLUGIN_UPDATES_URL,
            "http://gruntspud.sourceforge.net/plugins/${gruntspud-version}"), 20);
        UIUtil.jGridBagAdd(s, new JLabel("Plugin updates URL "), gbc1,
                           GridBagConstraints.RELATIVE);
        gbc1.weightx = 1.0;
        UIUtil.jGridBagAdd(s, pluginUpdatesURL, gbc1,
                           GridBagConstraints.REMAINDER);

        gbc1.weightx = 0.0;

        defaultEncoding = new CharSetComboBox();
        defaultEncoding.setSelectedEncoding(context.getHost().getProperty(
            Constants.OPTIONS_SYSTEM_DEFAULT_ENCODING));
        UIUtil.jGridBagAdd(s, new JLabel("Default encoding "), gbc1,
                           GridBagConstraints.RELATIVE);
        gbc1.weightx = 1.0;
        UIUtil.jGridBagAdd(s, defaultEncoding, gbc1,
                           GridBagConstraints.REMAINDER);

        //  General output options
        JPanel p = new JPanel(new GridBagLayout());		
		p.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 2.0;

        debug = new GruntspudCheckBox("Enable debugging",
                              context.getHost().getBooleanProperty(Constants.
            OPTIONS_SYSTEM_DEBUG,
            false));
        debug.setMnemonic('d');
        debug.addActionListener(this);
        UIUtil.jGridBagAdd(p, debug, gbc, GridBagConstraints.REMAINDER);

        debugToConsole = new GruntspudCheckBox("Debug to console",
                                       context.getHost().getBooleanProperty(
            Constants.OPTIONS_SYSTEM_DEBUG_TO_CONSOLE,
            false));
        debugToConsole.setMnemonic('d');
        UIUtil.jGridBagAdd(p, debugToConsole, gbc, GridBagConstraints.REMAINDER);

        logCVSIO = new GruntspudCheckBox(
            "Log CVS I/O (~/GRUNTSPUD.in and ~/GRUNTSPUD.out)",
            context.getHost().getBooleanProperty(Constants.
                                                 OPTIONS_SYSTEM_LOG_CVS_IO,
                                                 false));
        logCVSIO.setMnemonic('c');
        UIUtil.jGridBagAdd(p, logCVSIO, gbc, GridBagConstraints.REMAINDER);

        gbc.weighty = 1.0;
        disableNativePermissionHandler = new GruntspudCheckBox(
            "Disable native file permissions handler",
            context.getHost().getBooleanProperty(Constants.
            OPTIONS_SYSTEM_DISABLE_NATIVE_PERMISSION_HANDLER,
            false));
        disableNativePermissionHandler.setMnemonic('d');
        UIUtil.jGridBagAdd(p, disableNativePermissionHandler, gbc,
                           GridBagConstraints.REMAINDER);
        disableKeyboardAccelerators = new GruntspudCheckBox(
            "Disable keyboard accelerators",
            context.getHost().getBooleanProperty(Constants.
            OPTIONS_SYSTEM_DISABLE_KEYBOARD_ACCELERATORS,
            false));
        disableKeyboardAccelerators.setMnemonic('k');
        UIUtil.jGridBagAdd(p, disableKeyboardAccelerators, gbc,
                           GridBagConstraints.REMAINDER);
        hideMenuIcons = new GruntspudCheckBox(
            "Hide menu icons",
            context.getHost().getBooleanProperty(Constants.
            OPTIONS_SYSTEM_HIDE_MENU_ICONS,
            false));
        hideMenuIcons.setMnemonic('h');
        UIUtil.jGridBagAdd(p, hideMenuIcons, gbc,
                           GridBagConstraints.REMAINDER);

        JLabel l = new JLabel(
            "Changing these options require that you restart " +
            context.getHost().getName());
        l.setForeground(Color.red);
        l.setHorizontalAlignment(JLabel.CENTER);

        setLayout(new GridBagLayout());

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(this, s, gbc2, GridBagConstraints.REMAINDER);
        gbc2.insets = new Insets(2, 40, 2, 2);
        UIUtil.jGridBagAdd(this, p, gbc2, GridBagConstraints.REMAINDER);
        gbc2.weighty = 1.0;
        gbc2.anchor = GridBagConstraints.SOUTH;
        UIUtil.jGridBagAdd(this, l, gbc2, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        setAvailableActions();
    }

    private void setAvailableActions() {
        debugToConsole.setEnabled(debug.isSelected());
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
                                                  OPTIONS_SYSTEM_DEBUG,
                                                  debug.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_SYSTEM_DEBUG_TO_CONSOLE,
            debugToConsole.isSelected());
        getContext().getHost().setProperty(Constants.
            OPTIONS_SYSTEM_DEFAULT_ENCODING,
            defaultEncoding.getSelectedEncoding());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_SYSTEM_DISABLE_NATIVE_PERMISSION_HANDLER,
            disableNativePermissionHandler.isSelected());
        getContext().getHost().setProperty(Constants.
                                           OPTIONS_SYSTEM_USER_IGNORE_FILE,
                                           userIgnoreFile.getText());
        getContext().getHost().setBooleanProperty(Constants.
                                                  OPTIONS_SYSTEM_LOG_CVS_IO,
                                                  logCVSIO.isSelected());
        getContext().getHost().setProperty(
            Constants.OPTIONS_SYSTEM_PLUGIN_UPDATES_URL,
            pluginUpdatesURL.getText());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_SYSTEM_DISABLE_KEYBOARD_ACCELERATORS,
            disableKeyboardAccelerators.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_SYSTEM_HIDE_MENU_ICONS,
            hideMenuIcons.isSelected());

    }
}
