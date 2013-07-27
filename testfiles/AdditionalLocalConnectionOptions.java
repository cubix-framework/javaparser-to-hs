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

package gruntspud.connection.local;

import gruntspud.ResourceUtil;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.FileNameTextField;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.preferences.AbstractAdditionalOptionsPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

/**
 * A <strong>:local</strong> connection is similar to <strong>:ext</strong> except it
 * assumes the CVS command is avaiable locally. This class provides a component for
 * any additioanl arguments to the command that might be required.
 *
 * @author     magicthize
 */
public class AdditionalLocalConnectionOptions
    extends AbstractAdditionalOptionsPane
    implements ActionListener {
    //  Private instance variables
    static ResourceBundle res = ResourceBundle.getBundle(
        "gruntspud.connection.local.ResourceBundle",
        Locale.getDefault(),
        AdditionalLocalConnectionOptions.class.getClassLoader());
    private XTextField alternativeCVSCommand;
    private ConnectionProfile profile;
    private JButton browse;

    /**
     *  Constructor for the AdditionalLocalConnectionOptions object
     */
    public AdditionalLocalConnectionOptions() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, new JLabel(res.getString("additionalLocalConnectionOptions.alternativeCVS.text")),
                           gbc, 1);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           alternativeCVSCommand = new FileNameTextField(null,
            "",
            15, true, true),
                           gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, browse = new JButton(res.getString("additionalLocalConnectionOptions.browse.text")), gbc,
                           GridBagConstraints.RELATIVE);
        browse.setMnemonic(ResourceUtil.getResourceMnemonic(res, 
        		"additionalLocalConnectionOptions.browse.mnemonic"));
        browse.addActionListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browse) {
            File f = new File(alternativeCVSCommand.getText().equals("")
                              ? System.getProperty("user.dir")
                              : alternativeCVSCommand.getText());
            JFileChooser chooser = new JFileChooser(f);
            chooser.setSelectedFile(f);
            chooser.setDialogTitle("Choose application ..");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String s = chooser.getSelectedFile().getAbsolutePath();
                alternativeCVSCommand.setText("\"" + s + "\"");
            }
        }
    }

    /* (non-Javadoc)
     * @see gruntspud.ui.preferences.AbstractAdditionalOptionsPane#setProfile(gruntspud.connection.ConnectionProfile)
     */
    public void setProfile(ConnectionProfile profile) {
        this.profile = profile;
        alternativeCVSCommand.setText(profile.getProperty(
            CVSRootLocalConnection.PROFILE_PROPERTY_ALTERNATIVE_CVS_COMMAND,
            "cvs"));
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
        profile.setProperty(CVSRootLocalConnection.
                            PROFILE_PROPERTY_ALTERNATIVE_CVS_COMMAND,
                            alternativeCVSCommand.getText());
    }
}
