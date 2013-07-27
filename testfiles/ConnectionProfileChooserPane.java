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

package gruntspud.ui;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.connection.ConnectionProfile;
import gruntspud.connection.ConnectionProfileListModel;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.preferences.ConnectionOptionTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Description of the Class
 * 
 * @author magicthize
 * @created 26 May 2002
 */
public class ConnectionProfileChooserPane extends JPanel {
  //  Private instanve variables
  private JComboBox connectionProfileComboBox;
  //private JList connectionProfileList;
  private JButton maintain;
  private ConnectionProfileListModel listModel;
  private GruntspudContext context;

  /**
   * Constructor for the HomeLocationPane object
   * 
   * @param host
   *          Description of the Parameter
   */
  public ConnectionProfileChooserPane(GruntspudContext context) {
    super(new GridBagLayout());

    this.context = context;
    setOpaque(false);

    //
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0, 0, 0, 2);
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    listModel = new ConnectionProfileListModel(context
        .getConnectionProfileModel());
    UIUtil.jGridBagAdd(this, connectionProfileComboBox = new JComboBox(listModel) , gbc, GridBagConstraints.RELATIVE);
        
       connectionProfileComboBox.setMaximumRowCount(20);
    connectionProfileComboBox
        .setRenderer(new ConnectionProfileComboBoxRenderer(context));
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.EAST;
    gbc.insets = new Insets(0, 2, 0, 0);
    maintain = UIUtil.createButton(Constants.ICON_TOOL_SMALL_CONNECT,
        "Maintain", new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
            maintain();
          }
        });
    maintain.setRolloverEnabled(true);
    maintain.setBorder(null);
    UIUtil.jGridBagAdd(this, maintain, gbc, GridBagConstraints.REMAINDER);
  }

  private void maintain() {
    OptionDialog.Option ok = new OptionDialog.Option("Ok", "Ok", 'o');
    OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel",
        'c');
    final ConnectionOptionTab conx = new ConnectionOptionTab();
    conx.init(context);

    OptionDialog.Option opt = OptionDialog.showOptionDialog("maintain",
        context, this, new OptionDialog.Option[]{ok, cancel}, conx,
        "Maintain Connection Profiles", ok, new OptionDialog.Callback() {
          public boolean canClose(OptionDialog dialog,
              OptionDialog.Option option) {
            return conx.validateTab();
          }

        public void close(OptionDialog dialog, Option option) {
            // TODO Auto-generated method stub
            
        }
        });

    if (opt != ok) {
      return;
    }

    if (conx.validateTab()) {
      conx.applyTab();
      ((ConnectionProfileListModel) connectionProfileComboBox.getModel())
          .refresh();
    }
  }

  public void setSelectedProfile(ConnectionProfile profile) {
    this.connectionProfileComboBox.setSelectedItem(profile);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param enabled
   *          DOCUMENT ME!
   */
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    connectionProfileComboBox.setEnabled(enabled);
    maintain.setEnabled(enabled);
  }

  /**
   *  
   */
  public void setSelectedName(String name) {
    for (int i = 0; i < listModel.getSize(); i++) {
      ConnectionProfile pr = (ConnectionProfile) listModel.getElementAt(i);
      if (pr.getName().equals(name)) {
        connectionProfileComboBox.setSelectedIndex(i);
        return;
      }
    }
    connectionProfileComboBox.setSelectedItem(null);
  }

  /**
   * Gets the selectedFile attribute of the HomeLocationPane object
   * 
   * @return The selectedFile value
   */
  public ConnectionProfile getSelectedProfile() {
    int sel = connectionProfileComboBox.getSelectedIndex();
    return sel == -1 ? null : (ConnectionProfile) listModel.getElementAt(sel);
  }
}