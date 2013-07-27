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

package gruntspud.connection.server;

import gruntspud.GruntspudContext;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.AbstractAdditionalOptionsPane;
import gruntspud.ui.preferences.PasswordAuthenticationOptionsPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class AdditionalServerConnectionOptions
    extends AbstractAdditionalOptionsPane {
    //  Private instance variables
    static ResourceBundle res = ResourceBundle.getBundle(
        "gruntspud.connection.server.ResourceBundle",
        Locale.getDefault(),
        AdditionalServerConnectionOptions.class.getClassLoader());
    private JCheckBox useREXEC;
    private PasswordAuthenticationOptionsPane password;
    private ConnectionProfile profile;

    /**
     *  Constructor for the GlobalOptionsTab object
     *
     *@param  host  Description of the Parameter
     */
    public AdditionalServerConnectionOptions() {
        setLayout(new BorderLayout());

        //  Type panel
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel type = new JPanel(new GridBagLayout());
        gbc.insets = new Insets(1, 1, 2, 2);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIUtil.jGridBagAdd(type,
                           useREXEC = new JCheckBox(res.getString(
            "useREXECInsteadOfRCMD")),
                           gbc, GridBagConstraints.REMAINDER);
        useREXEC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setAvailableActions();
            }
        });
        gbc.insets = new Insets(1, 24, 2, 2);
        password = new PasswordAuthenticationOptionsPane(false);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(type, password, gbc, GridBagConstraints.REMAINDER);

        //  Create the warning
        TitledBorder titledBorder = new TitledBorder(res.getString("warning"));
        titledBorder.setTitleColor(Color.red);

        MultilineLabel textBox = new MultilineLabel(res.getString("warningText"));
        textBox.setBorder(titledBorder);

        JPanel t = new JPanel(new FlowLayout(FlowLayout.CENTER));
        t.add(textBox);

        //  This panel
        add(t, BorderLayout.SOUTH);
        add(type, BorderLayout.CENTER);

        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);
        password.init(context);
    }

    /**
     * Set the connection profile
     *
     * @param profile connection profile
     */
    public void setProfile(ConnectionProfile profile) {
        this.profile = profile;
        useREXEC.setSelected(profile.getProperty(
            CVSRootServerConnection.PROFILE_PROPERTY_USE_REXEC, "false")
                             .equals("true"));
        password.setProfile(profile);
        setAvailableActions();
    }

    private void setAvailableActions() {
        password.setEnabled(useREXEC.isSelected());
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateOptions() {
        if (useREXEC.isSelected()) {
            return password.validateOptions();
        }

        return true;
    }

    /**
     *  Description of the Method
     */
    public void applyOptions() {
        profile.setProperty(CVSRootServerConnection.PROFILE_PROPERTY_USE_REXEC,
                            String.valueOf(useREXEC.isSelected()));

        if (useREXEC.isSelected()) {
            password.applyOptions();
        }
    }
}
