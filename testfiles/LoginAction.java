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

package gruntspud.connection.pserver;

import gruntspud.CVSCommandHandler;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.GruntspudAction;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.ConnectionProfileChooserPane;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.netbeans.lib.cvsclient.connection.StandardScrambler;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class LoginAction
    extends DefaultGruntspudAction {
    /**
     *  Constructor for the RemoveAction object
     */
    public LoginAction(GruntspudContext context) {
        super(context);

        putValue(Action.NAME, Constants.ACTION_LOGIN);
        putValue(GruntspudAction.ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_LOGIN));
        putValue(Action.SHORT_DESCRIPTION, "Login");
        putValue(Action.LONG_DESCRIPTION,
                 "Login into the repository (pserver only)");
        putValue(Action.MNEMONIC_KEY, new Integer('l'));
        putValue(DefaultGruntspudAction.SMALL_ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_LOGIN));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean checkAvailable() {
        return!CVSCommandHandler.getInstance().isCommandRunning();
    }

    /**
     *  Description of the Method
     *
     *@param  evt  Description of the Parameter
     */
    public void actionPerformed(final ActionEvent evt) {
        login(getContext(),
              "Please choose the connection profile you wish\n" +
              "to login to and type in the password.", null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     * @param text DOCUMENT ME!
     * @param selectedProfile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static String login(GruntspudContext context, String text,
                               ConnectionProfile selectedProfile) {
        MultilineLabel l = new MultilineLabel(text);
        l.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 0));

        final ConnectionProfileChooserPane conx = new
            ConnectionProfileChooserPane(context);
        conx.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        if (selectedProfile != null) {
            conx.setEnabled(false);
            conx.setSelectedName(selectedProfile.getName());
        }

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 16, 8, 16));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(p, new JLabel("Password: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;

        JPasswordField password = new JPasswordField(12);
        UIUtil.jGridBagAdd(p, password, gbc, GridBagConstraints.REMAINDER);

        JPanel n = new JPanel(new BorderLayout());
        n.add(l, BorderLayout.NORTH);
        n.add(conx, BorderLayout.CENTER);
        n.add(p, BorderLayout.SOUTH);

        JPanel z = new JPanel(new BorderLayout());
        z.add(new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_CONNECT)),
              BorderLayout.WEST);
        z.add(n, BorderLayout.CENTER);

        //  Show the dialog
        OptionDialog.Option login = new OptionDialog.Option("Login", "Login",
            'l');
        OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
            "Cancel", 'c');
        OptionDialog.Option opt = OptionDialog.showOptionDialog("login",
            context, context.getHost().getMainComponent(),
            new OptionDialog.Option[] {login, cancel}
            , z, "Login", login,
            null, true, true);

        if ( (opt != login) || (conx.getSelectedProfile() == null)) {
            return null;
        }

        ConnectionProfile profile = conx.getSelectedProfile();

        if (profile.getCVSRoot().getConnectionType().equals("pserver")) {
            String current = CVSRootPServerConnection.lookupEncodedPassword(
                profile);

            if (current != null) {
                JOptionPane.showMessageDialog(context.getHost()
                                              .getMainComponent(),
                    "Already logged in. You should logout first.",
                    "Already logged in", JOptionPane.ERROR_MESSAGE);

                return null;
            }
            else {
                CVSRootPServerConnection.storePassword(context, profile,
                    new String(password.getPassword()));
            }
        }
        else {
            JOptionPane.showMessageDialog(context.getHost().getMainComponent(),
                "You can only 'login' into a pserver connection type.",
                "Error", JOptionPane.ERROR_MESSAGE);

            return null;
        }

        //  Login ok

        /** @todo check the password with the server */
        return StandardScrambler.getInstance().scramble(new String(
            password.getPassword()));
    }
}
