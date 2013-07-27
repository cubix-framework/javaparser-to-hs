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
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.actions.AbstractNormalRemoveAction;
import gruntspud.authentication.GruntspudAuthenticator;
import gruntspud.authentication.PasswordPair;
import gruntspud.ui.IconTableCellRenderer;
import gruntspud.ui.ToolBarTablePane;
import gruntspud.ui.UIUtil;
import gruntspud.ui.GruntspudCheckBox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

/**
 *  Mini text editor
 *
 *@author     Brett Smiht
 *@created    26 May 2002
 */
public class PasswordManagerPane
    extends JPanel
    implements ActionListener {
    //  Private instance variables
    private Action removeAction;
    private GruntspudContext context;
    private PasswordManagerTable table;
    private GruntspudCheckBox encryptPasswords;
    private JButton changeMasterPassword;

    /**
     * Creates a new PasswordManagerPane object.
     *
     * @param context DOCUMENT ME!
     */
    public PasswordManagerPane(GruntspudContext context) {
        super(new BorderLayout());

		setOpaque(false);
		
        //  Initialise
        this.context = context;

        //  Create the toolbar
        JToolBar toolBar = new JToolBar("Password manager tools");
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setBorder(null);
        toolBar.setFloatable(false);
        toolBar.add(UIUtil.createButton(removeAction = new RemoveAction(),
                                        false, false));

        //  Create the text area
        table = new PasswordManagerTable();
        table.setBorder(null);
        table.getSelectionModel().addListSelectionListener(new
            ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                setAvailableActions();
            }
        });

        //  Build this
        add(new ToolBarTablePane(toolBar, table) {
            public Dimension getPreferredSize() {
                return new Dimension(420, 380);
            }
        }

        , BorderLayout.CENTER);

        //
        if (context.getEncrypter() != null) {
            JPanel bottomPanel = new JPanel(new GridBagLayout());
			bottomPanel.setOpaque(false);
            bottomPanel.setBorder(BorderFactory.createTitledBorder("Options"));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(bottomPanel,
                               encryptPasswords = new GruntspudCheckBox(
                "Encrypt passwords",
                Gruntspud.getAuthenticator().getPasswordPairList()
                .isEncrypted()), gbc,
                               GridBagConstraints.RELATIVE);
            encryptPasswords.setMnemonic('c');
            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(bottomPanel,
                               changeMasterPassword = new JButton("Change"),
                               gbc,
                               GridBagConstraints.REMAINDER);
            changeMasterPassword.setToolTipText("Change the master password");
            changeMasterPassword.setMnemonic('c');
            changeMasterPassword.addActionListener(this);
            changeMasterPassword.setEnabled(encryptPasswords.isSelected());
            add(bottomPanel, BorderLayout.SOUTH);
        }

        //  Set the intially available actions
        setAvailableActions();
    }

    private void changeMasterPassword() {
        char[][] pw = Gruntspud.getAuthenticator()
            .showMasterPasswordConfirmationDialog(context,
                                                  this,
            "Enter the new master password to use to encrypt\n" +
            "your password list. You also need to provide the\n" +
            "old password to continue.", "Enter master password", true);

        if (pw != null) {
            try {
                Gruntspud.getAuthenticator().getPasswordPairList()
                    .setMasterPassword(pw[0]);
                Gruntspud.getAuthenticator().getPasswordPairList()
                    .changeMasterPassword(pw[1]);
                Gruntspud.getAuthenticator().getPasswordPairList()
                    .savePasswordFile();
            }
            catch (IOException ioe) {
                GruntspudUtil.showErrorMessage(this, "Error", ioe);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == changeMasterPassword) {
            changeMasterPassword();
        }
    }

    /**
     * Save the profiles
     */
    public void apply() {
        GruntspudAuthenticator auth = Gruntspud.getAuthenticator();
        table.cleanUp();

        if ( (encryptPasswords != null) &&
            (auth.getPasswordPairList().isEncrypted() !=
             encryptPasswords.isSelected())) {
            try {
                if (encryptPasswords.isSelected()) {
                    char[][] pw = auth.showMasterPasswordConfirmationDialog(
                        context,
                        this,
                        "Enter the new master password to use to encrypt\n" +
                        "your password list", "Enter master password", false);

                    if (pw != null) {
                        auth.getPasswordPairList().setMasterPassword(pw[1]);
                        auth.getPasswordPairList().encryptAllPairs();
                        auth.getPasswordPairList().savePasswordFile();
                    }
                }
                else {
                    String message = null;

                    while (true) {
                        char[] p = auth.getMasterPassword(message, this);

                        if (p == null) {
                            break;
                        }

                        if (!auth.getPasswordPairList().checkMasterPassword(p)) {
                            message = "Incorrect password";
                        }
                        else {
                            auth.getPasswordPairList().setMasterPassword(p);
                            auth.getPasswordPairList().decryptAllPairs();
                            auth.getPasswordPairList().savePasswordFile();

                            break;
                        }
                    }
                }
            }
            catch (IOException ioe) {
                GruntspudUtil.showErrorMessage(this, "Error", ioe);
            }
        }
    }

    /**
     *  Set what actions are available depending on state
     */
    private void setAvailableActions() {
        removeAction.setEnabled(table.getSelectedRowCount() == 1);
    }

    //  Supporting classes
    class RemoveAction
        extends AbstractNormalRemoveAction {
        /**
         *  Constructor for the DeleteAction object
         */
        RemoveAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            if (JOptionPane.showConfirmDialog(PasswordManagerPane.this,
                "Are you sure you want to remove this password?",
                "Remove stored password", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                try {
                    int i = table.getSelectedRow();
                    Gruntspud.getAuthenticator().getPasswordPairList()
                        .removeElementAt(i);
                    ( (PasswordManagerTableModel) table.getModel()).
                        fireTableRowsDeleted(i,
                                             i);
                    Gruntspud.getAuthenticator().getPasswordPairList()
                        .savePasswordFile();
                }
                catch (IOException ioe) {
                    GruntspudUtil.showErrorMessage(PasswordManagerPane.this,
                        "Error", ioe);
                }
            }
        }
    }

    class PasswordManagerTable
        extends JTable {
        public PasswordManagerTable() {
            super();
            setModel(new PasswordManagerTableModel());
            setShowGrid(false);
            setAutoResizeMode(0);
            setRowHeight(18);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setDefaultRenderer(Icon.class, new IconTableCellRenderer());
            UIUtil.restoreTableMetrics(this,
                                       Constants.
                                       PASSWORD_MANAGER_TABLE_GEOMETRY,
                                       new int[] {24, 60, 180, 240}
                                       , context);
            setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
        }

        public void cleanUp() {
            UIUtil.saveTableMetrics(this,
                                    Constants.PASSWORD_MANAGER_TABLE_GEOMETRY,
                                    context);
        }

        public boolean getScrollableTracksViewportHeight() {
            Component parent = getParent();

            if (parent instanceof JViewport) {
                return parent.getHeight() > getPreferredSize().height;
            }
            else {

                return false;
            }
        }
    }

    class PasswordManagerTableModel
        extends AbstractTableModel {
        PasswordManagerTableModel() {
        }

        public int getRowCount() {
            return Gruntspud.getAuthenticator().getPasswordPairList().size();
        }

        public Object getValueAt(int r, int c) {
            PasswordPair p = Gruntspud.getAuthenticator().getPasswordPairList()
                .getPasswordPairAt(r);

            switch (c) {
                case 0:
                    return p.isPersistant()
                        ? UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_SAVE) : null;
                case 1:
                    return p.getKey().getProtocol();
                case 2:
                    return p.getKey().getHost();
                default:
                    return p.getKey().getScheme();
            }
        }

        public int getColumnCount() {
            return 4;
        }

        public Class getColumnClass(int c) {
            return (c == 0) ? Icon.class : String.class;
        }

        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "*";
                case 1:
                    return "Protocol";
                case 2:
                    return "Host";
                default:
                    return "Scheme";
            }
        }
    }
}
