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

package gruntspud.connection.ext;

import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.preferences.AbstractAdditionalOptionsPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JLabel;

/**
 * Provides a component for editing the additional options required for an
 * <strong>:ext</strong> connection.
 *
 *@author     magicthize
 */
public class AdditionalExtConnectionOptions
    extends AbstractAdditionalOptionsPane {
    //  Private instance variables
    static ResourceBundle res = ResourceBundle.getBundle(
        "gruntspud.connection.ext.ResourceBundle",
        Locale.getDefault(),
        AdditionalExtConnectionOptions.class.getClassLoader());
    private XTextField cvsRSH;
    private XTextField cvsRSHAdditionalArguments;
    private ConnectionProfile profile;

    /**
     *  Constructor for the AdditionalExtConnectionOptions object
     */
    public AdditionalExtConnectionOptions() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel(res.getString("additionalExtConnectionOptions.cvsRSH.text")), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this, cvsRSH = new XTextField(15), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, new JLabel(res.getString("additionalExtConnectionOptions.cvsRSH.arguments.text")), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           cvsRSHAdditionalArguments = new XTextField(15), gbc,
                           GridBagConstraints.REMAINDER);
    }

    /* (non-Javadoc)
     * @see gruntspud.ui.preferences.AbstractAdditionalOptionsPane#setProfile(gruntspud.connection.ConnectionProfile)
     */
    public void setProfile(ConnectionProfile profile) {
        this.profile = profile;
        cvsRSH.setText(profile.getProperty(
            CVSRootExtServerConnection.PROFILE_PROPERTY_CVS_RSH, "ssh"));
        cvsRSHAdditionalArguments.setText(profile.getProperty(
            CVSRootExtServerConnection.
            PROFILE_PROPERTY_CVS_RSH_ADDITIONAL_ARGUMENTS,
            ""));
    }

    /* (non-Javadoc)
     * @see gruntspud.ui.preferences.AbstractAdditionalOptionsPane#validateOptions()
     */
    public boolean validateOptions() {
        if (profile == null) {
            return false;
        }

        return true;
    }

    /* (non-Javadoc)
     * @see gruntspud.ui.preferences.AbstractAdditionalOptionsPane#applyOptions()
     */
    public void applyOptions() {
        profile.setProperty(CVSRootExtServerConnection.PROFILE_PROPERTY_CVS_RSH,
                            cvsRSH.getText());
        profile.setProperty(CVSRootExtServerConnection.
                            PROFILE_PROPERTY_CVS_RSH_ADDITIONAL_ARGUMENTS,
                            cvsRSHAdditionalArguments.getText());
    }
}
