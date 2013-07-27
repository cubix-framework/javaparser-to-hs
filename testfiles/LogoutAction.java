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
import gruntspud.CVSFileNode;
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
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class LogoutAction
    extends DefaultGruntspudAction {
    /**
     *  Constructor for the RemoveAction object
     */
    public LogoutAction(GruntspudContext context) {
        super(context);

        putValue(Action.NAME, Constants.ACTION_LOGOUT);
        putValue(GruntspudAction.ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_LOGOUT));
        putValue(Action.SHORT_DESCRIPTION, "Logout");
        putValue(Action.LONG_DESCRIPTION,
                 "Logout from the repository (pserver only)");
        putValue(Action.MNEMONIC_KEY, new Integer('o'));
        putValue(DefaultGruntspudAction.SMALL_ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_LOGOUT));
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
        ConnectionProfile selectedProfile = null;
        CVSFileNode sel = getContext().getViewManager().getSelectedNode();
        MultilineLabel l = new MultilineLabel(
            "Please select the connection profile you wish\n" +
            "to logout of.");
        l.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 0));

        final ConnectionProfileChooserPane conx = new
            ConnectionProfileChooserPane(getContext());
        conx.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        if ( (sel != null) && (sel.getCVSRoot() != null)) {
            conx.setSelectedName(getContext().getConnectionProfileModel()
                                 .getProfileForCVSRoot(sel.getCVSRoot())
                                 .getName());

        }
        JPanel n = new JPanel(new BorderLayout());
        n.add(l, BorderLayout.NORTH);
        n.add(conx, BorderLayout.CENTER);

        JPanel z = new JPanel(new BorderLayout());
        z.add(new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_LOGOUT)),
              BorderLayout.WEST);
        z.add(n, BorderLayout.CENTER);

        //  Show the dialog
        OptionDialog.Option logout = new OptionDialog.Option("Logout",
            "Logout", 'l');
        OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
            "Cancel", 'c');
        OptionDialog.Option opt = OptionDialog.showOptionDialog("logout",
            getContext(), getContext().getHost().getMainComponent(),
            new OptionDialog.Option[] {logout, cancel}
            , z, "Logout",
            logout, null, true, true);

        if ( (opt != logout) || (conx.getSelectedProfile() == null)) {
            return;
        }

        ConnectionProfile profile = conx.getSelectedProfile();

        if (profile.getCVSRoot().getConnectionType().equals("pserver")) {
            String current = CVSRootPServerConnection.lookupEncodedPassword(
                profile);

            if (current == null) {
                JOptionPane.showMessageDialog(getContext().getHost()
                                              .getMainComponent(),
                                              "Not logged in.", "Warning",
                                              JOptionPane.WARNING_MESSAGE);
            }
            else {
                CVSRootPServerConnection.unstorePassword(getContext(), profile);
            }
        }
        else {
            JOptionPane.showMessageDialog(getContext().getHost()
                                          .getMainComponent(),
                "You can only 'logout' of a pserver connection type.", "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
