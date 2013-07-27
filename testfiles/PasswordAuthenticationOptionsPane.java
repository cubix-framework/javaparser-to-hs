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

import gruntspud.GruntspudUtil;
import gruntspud.connection.ConnectionProfile;
import gruntspud.connection.pserver.CVSRootPServerConnection;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class PasswordAuthenticationOptionsPane
    extends AbstractAdditionalOptionsPane
    implements ActionListener {
    public final static String PASSWORD_AUTHENTICATION_TYPE =
        "password.authenticationType";
    public final static String DONT_ASK_PASSWORD = "password.dontAsk.password";
    public final static String TYPE_DONT_ASK = "dontAsk";
    public final static String TYPE_USE_CVSPASS = "useCVSPASS";
    public final static String TYPE_USE_PASSWORD_MANAGER = "usePasswordManager";

    //  Private instance variables
    private JPasswordField password;
    private JRadioButton useCVSPASS;
    private JRadioButton dontAsk;
    private JRadioButton usePasswordManager;
    private ConnectionProfile profile;
    private XTextField cvspassFile;
    private JButton browse;
    private JCheckBox oldFormat;

    /**
     *  Constructor for the PasswordAuthenticationOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public PasswordAuthenticationOptionsPane() {
        this(false);
    }

    /**
     *  Constructor for the PasswordAuthenticationOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public PasswordAuthenticationOptionsPane(boolean offerUseOfCVSPASS) {
        setLayout(new GridBagLayout());

        Insets i1 = new Insets(1, 1, 2, 2);
        Insets i2 = new Insets(1, 24, 2, 2);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = i1;

        ButtonGroup bg = new ButtonGroup();

        gbc.insets = i1;
        gbc.weightx = 2.0;
        usePasswordManager = new JRadioButton("Use Password Manager");
        UIUtil.jGridBagAdd(this, usePasswordManager, gbc,
                           GridBagConstraints.REMAINDER);
        usePasswordManager.setMnemonic('m');
        usePasswordManager.addActionListener(this);
        bg.add(usePasswordManager);

        if (offerUseOfCVSPASS) {
            useCVSPASS = new JRadioButton("Use ~/.cvspass");
            UIUtil.jGridBagAdd(this, useCVSPASS, gbc,
                               GridBagConstraints.REMAINDER);
            useCVSPASS.setMnemonic('c');
            useCVSPASS.addActionListener(this);
            bg.add(useCVSPASS);
            gbc.weightx = 1.0;
            gbc.insets = i2;
            cvspassFile = new XTextField(12);
            UIUtil.jGridBagAdd(this, cvspassFile, gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 0.0;
            browse = new JButton("Browse");
            browse.setMnemonic('b');
            browse.addActionListener(this);
            UIUtil.jGridBagAdd(this, browse, gbc, GridBagConstraints.REMAINDER);
            gbc.weightx = 2.0;
            oldFormat = new JCheckBox("Use old format (pre 1.11)");
            UIUtil.jGridBagAdd(this, oldFormat, gbc,
                               GridBagConstraints.REMAINDER);
            oldFormat.setMnemonic('e');
            oldFormat.addActionListener(this);
            gbc.insets = i1;
        }

        dontAsk = new JRadioButton("Use this password");
        dontAsk.setMnemonic('p');
        dontAsk.addActionListener(this);
        UIUtil.jGridBagAdd(this, dontAsk, gbc, GridBagConstraints.REMAINDER);
        gbc.insets = i2;
        bg.add(dontAsk);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, password = new JPasswordField(12), gbc,
                           GridBagConstraints.REMAINDER);

        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param enabled DOCUMENT ME!
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        usePasswordManager.setEnabled(enabled);
        dontAsk.setEnabled(enabled);

        if (useCVSPASS != null) {
            useCVSPASS.setEnabled(enabled);
            oldFormat.setEnabled(enabled);
        }

        setAvailableActions();
    }

    /**
     *
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browse) {
            File f = new File(cvspassFile.getText());
            JFileChooser chooser = new JFileChooser(f);
            chooser.setSelectedFile(f);
            chooser.setDialogTitle("Choose .cvspass file");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                cvspassFile.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }

        setAvailableActions();
    }

    private void setAvailableActions() {
        password.setEnabled(isEnabled() && dontAsk.isSelected());

        if (useCVSPASS != null) {
            boolean enabled = isEnabled() && useCVSPASS.isSelected();
            cvspassFile.setEnabled(enabled);
            browse.setEnabled(enabled);
            oldFormat.setEnabled(enabled);
        }
    }

    /**
     * Set the connection profile
     *
     * @param profile connection profile
     */
    public void setProfile(ConnectionProfile profile) {
        this.profile = profile;

        if (profile == null) {
            password.setText("");
            dontAsk.setSelected(true);
            cvspassFile.setText("");
            oldFormat.setSelected(false);
        }
        else {
            password.setText(profile.getProperty(DONT_ASK_PASSWORD, ""));

            String t = profile.getProperty(PASSWORD_AUTHENTICATION_TYPE,
                                           (useCVSPASS == null) ?
                                           TYPE_USE_PASSWORD_MANAGER
                                           : TYPE_USE_CVSPASS);

            if (t.equals(TYPE_DONT_ASK)) {
                dontAsk.setSelected(true);
            }
            else if (t.equals(TYPE_USE_PASSWORD_MANAGER)) {
                usePasswordManager.setSelected(true);
            }
            else if (t.equals(TYPE_USE_CVSPASS) && (useCVSPASS != null)) {
                useCVSPASS.setSelected(true);

            }
            if (cvspassFile != null) {
                cvspassFile.setText(profile.getProperty(
                    CVSRootPServerConnection.PROFILE_PROPERTY_CVSPASS_FILE,
                    System.getProperty("user.home") + File.separator +
                    ".cvspass"));
                oldFormat.setSelected(profile.getProperty(
                    CVSRootPServerConnection.
                    PROFILE_PROPERTY_CVSPASS_OLD_FORMAT,
                    "false").equals("true"));
            }
        }

        setAvailableActions();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateOptions() {
        if (profile == null) {
            return false;
        }

        if ( (useCVSPASS != null) && useCVSPASS.isSelected()) {
            InputStream in = null;

            try {
                File f = new File(cvspassFile.getText());

                if (!f.exists() || !f.canRead() || !f.isFile()) {
                    throw new Exception("Specified .cvspass file is invalid.");
                }

                in = new FileInputStream(f);

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                String l = r.readLine();

                if (l != null) {
                    if (l.startsWith("/") && oldFormat.isSelected()) {
                        throw new IOException(
                            "Specified .cvspass file is not pre 1.11 format");
                    }

                    if (!l.startsWith("/") && !oldFormat.isSelected()) {
                        throw new IOException(
                            "Specified .cvspass file is in pre 1.11 format");
                    }
                }
            }
            catch (Exception e) {
                GruntspudUtil.showErrorMessage(this, "Error", e);
                return false;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ioe) {
                    }
                }
            }
        }

        return true;
    }

    /**
     *  Description of the Method
     */
    public void applyOptions() {
        String pw = new String(password.getPassword());
        profile.setProperty(DONT_ASK_PASSWORD, pw);

        String t = (useCVSPASS == null) ? TYPE_USE_PASSWORD_MANAGER
            : TYPE_USE_CVSPASS;

        if (dontAsk.isSelected()) {
            t = TYPE_DONT_ASK;
        }
        else if (usePasswordManager.isSelected()) {
            t = TYPE_USE_PASSWORD_MANAGER;
        }
        else if ( (useCVSPASS != null) && useCVSPASS.isSelected()) {
            t = TYPE_USE_CVSPASS;

        }
        profile.setProperty(PASSWORD_AUTHENTICATION_TYPE, t);

        if (cvspassFile != null) {
            profile.setProperty(CVSRootPServerConnection.
                                PROFILE_PROPERTY_CVSPASS_FILE,
                                cvspassFile.getText());
            profile.setProperty(CVSRootPServerConnection.
                                PROFILE_PROPERTY_CVSPASS_OLD_FORMAT,
                                String.valueOf(oldFormat.isSelected()));
        }
    }
}
