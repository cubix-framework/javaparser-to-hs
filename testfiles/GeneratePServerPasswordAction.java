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

import gruntspud.GruntspudContext;
import gruntspud.JCrypt;
import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.GruntspudAction;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.icons.IconResource;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *  Action to invoke clipboard 'cut' actions
 *
 *@author     Brett Smith
 *@created    26 May 2002
 */
public class GeneratePServerPasswordAction
    extends DefaultGruntspudAction {
    private GruntspudContext context;
    
    static ResourceBundle res = ResourceBundle.getBundle(
        "gruntspud.connection.pserver.ResourceBundle",
        Locale.getDefault(), CVSRootPServerConnection.class.getClassLoader());

    /**
     *  Construct a new paste action
     *
     */
    public GeneratePServerPasswordAction(GruntspudContext context) {
      super(res,"generatePServerPasswordAction", context);
        putValue(Action.SMALL_ICON,
                 new IconResource("gruntspud/connection/pserver/smallgenpw.png"));
        putValue(GruntspudAction.ICON,
                 new IconResource("gruntspud/connection/pserver/genpw.png"));
        putValue(GruntspudAction.LARGE_ICON,
                 new IconResource("gruntspud/connection/pserver/largegenpw.png"));
        this.context = context;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean checkAvailable() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        JPanel t = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIUtil.jGridBagAdd(t, new JLabel("Password:"), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;

        final JPasswordField password = new JPasswordField(15);
        UIUtil.jGridBagAdd(t, password, gbc, GridBagConstraints.REMAINDER);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(t, new JLabel("Encrypted:"), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;

        final XTextField generated = new XTextField(15);
        generated.setEditable(false);
        UIUtil.jGridBagAdd(t, generated, gbc, GridBagConstraints.REMAINDER);

        JPanel z = new JPanel(new BorderLayout());
        z.add(new JLabel( (Icon) getValue(GruntspudAction.LARGE_ICON)),
              BorderLayout.WEST);
        z.add(t, BorderLayout.CENTER);

        final OptionDialog.Option generate = new OptionDialog.Option("Generate",
            "Generate encrypted password", 'g');
        OptionDialog.Option close = new OptionDialog.Option("Close", "Close",
            'c');
        final JComponent parent = context.getHost().getMainComponent();
        OptionDialog.showOptionDialog("generatePassword", context, parent,
                                      new OptionDialog.Option[] {generate,
                                      close}
                                      , z,
                                      "Generate Password", generate,
                                      new OptionDialog.Callback() {
            public boolean canClose(OptionDialog dialog,
                                    OptionDialog.Option option) {
                if (option == generate) {
                    char s1 = (char) ( (Math.random() * 26f) + (float) 'a');
                    char s2 = (char) ( (Math.random() * 26f) + (float) 'A');
                    generated.setText(JCrypt.crypt(String.valueOf(s1) +
                        String.valueOf(s2),
                        new String(password.getPassword())));

                    return false;
                }
                else {

                    return true;
                }
            }

            public void close(OptionDialog dialog, Option option) {
                // TODO Auto-generated method stub
                
            }
        }

        , false, true);
    }
}
