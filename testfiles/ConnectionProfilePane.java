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
import gruntspud.actions.AbstractEditAction;
import gruntspud.actions.AbstractNormalAddAction;
import gruntspud.actions.AbstractNormalRemoveAction;
import gruntspud.actions.AbstractSetDefaultAction;
import gruntspud.connection.ConnectionProfile;
import gruntspud.connection.ConnectionProfileModel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.ToolBarTablePane;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  Mini text editor
 *
 *@author     Brett Smiht
 *@created    26 May 2002
 */
public class ConnectionProfilePane
    extends JPanel {
    //  Private instance variables
    private ConnectionProfileTable profileTable;
    private Action addAction;
    private Action removeAction;
    private Action editAction;
    private Action setDefaultAction;
    private GruntspudContext context;

    /**
     * Creates a new ConnectionProfilePane object.
     *
     * @param context DOCUMENT ME!
     */
    public ConnectionProfilePane(GruntspudContext context) {
        super(new BorderLayout());
		
		setOpaque(false);

        //  Initialise
        this.context = context;

        //  Create the text area
        profileTable = new ConnectionProfileTable(context);
        profileTable.setBorder(null);
        profileTable.getSelectionModel().addListSelectionListener(new
            ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                setAvailableActions();
            }
        });
        profileTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent evt) {
        		if(evt.getClickCount() == 2)
        			editSelected();        		
        	}
        });

        //  Create the toolbar
        JToolBar toolBar = new JToolBar("Connection Profile tools");
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setBorder(null);
        toolBar.setFloatable(false);
        boolean showSelectiveText = context.getHost().getBooleanProperty(
        		Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
        toolBar.add(UIUtil.createButton(addAction = new AddAction(), showSelectiveText,
                                        false));
        toolBar.add(UIUtil.createButton(removeAction = new RemoveAction(),
		showSelectiveText, false));
        toolBar.add(UIUtil.createButton(editAction = new EditAction(), showSelectiveText,
                                        false));
        toolBar.add(UIUtil.createButton(
            setDefaultAction = new SetDefaultAction(), false, false));

        //  Build this
        add(new ToolBarTablePane(toolBar, profileTable) {
            public Dimension getScrollPanePreferredSize() {
                return new Dimension(420, 380);
            }
        }

        , BorderLayout.CENTER);

        //  Set the intially available actions
        setAvailableActions();
    }
    
    /**
     * Edit the selected profile
     */
    private void editSelected() {
    	int idx = profileTable.getSelectedRow();
    	if(idx != -1) {
			ConnectionProfile p = ( (ConnectionProfileModel) profileTable.
								   getModel()).getConnectionProfileAt(idx);
			OptionDialog.Option ok = new OptionDialog.Option("Update",
				"Update the connection", 'o');
			final OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
				"Cancel", 'c');
			final ConnectionProfileEditor profile = new ConnectionProfileEditor(
				context,
				p, false);
			OptionDialog.Option opt = OptionDialog.showOptionDialog(
				"editconnection",
				context, ConnectionProfilePane.this,
				new OptionDialog.Option[] {ok, cancel}
				, profile,
				"Edit connection", ok,
				new OptionDialog.Callback() {
				public boolean canClose(OptionDialog dialog,
										OptionDialog.Option option) {
					return (option == cancel) || profile.validateTab();
				}

                public void close(OptionDialog dialog, Option option) {
                    // TODO Auto-generated method stub
                    
                }
			}

			, true, true, new Dimension(640, 400));

			if (opt != ok) {
				return;
			}
			profile.applyTab();
			profileTable.repaint();    		
    	} 
    }

    /**
     * Save the profiles
     */
    public void apply() {
        profileTable.cleanUp();
        context.getConnectionProfileModel().apply();
    }

    /**
     *  Set what actions are available depending on state
     */
    private void setAvailableActions() {
        removeAction.setEnabled(profileTable.getSelectedRowCount() == 1);
        editAction.setEnabled(profileTable.getSelectedRowCount() == 1);
        setDefaultAction.setEnabled(profileTable.getSelectedRowCount() == 1);
    }

    //  Supporting classes
    //  Supporting classes
    class SetDefaultAction
        extends AbstractSetDefaultAction {
        /**
         *  Constructor for the SetDefaultAction object
         */
        SetDefaultAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            context.getConnectionProfileModel().setDefaultProfile( ( (
                ConnectionProfileModel) profileTable.getModel()).
                getConnectionProfileAt(
                profileTable.getSelectedRow()));
            profileTable.repaint();
        }
    }

    class AddAction
        extends AbstractNormalAddAction {
        /**
         *  Constructor for the CutAction object
         */
        AddAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            ConnectionProfile p = new ConnectionProfile();
            OptionDialog.Option ok = new OptionDialog.Option("Add",
                "Add the connection", 'o');
            final OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
                "Cancel", 'c');
            final ConnectionProfileEditor profile = new ConnectionProfileEditor(
                context,
                p);
            OptionDialog.Option opt = OptionDialog.showOptionDialog(
                "addconnection",
                context, ConnectionProfilePane.this,
                new OptionDialog.Option[] {ok, cancel}
                , profile,
                "Add connection", ok,
                new OptionDialog.Callback() {
                public boolean canClose(OptionDialog dialog,
                                        OptionDialog.Option option) {
                    return (option == cancel) || profile.validateTab();
                }

                public void close(OptionDialog dialog, Option option) {
                    // TODO Auto-generated method stub
                    
                }
            }

            , true, true, new Dimension(640, 400));

            if (opt != ok) {
                return;
            }

            profile.applyTab();
            context.getConnectionProfileModel().addProfile(p);
            profileTable.repaint();
        }
    }

    class RemoveAction
        extends AbstractNormalRemoveAction {
        /**
         *  Constructor for the DeleteAction object
         */
        RemoveAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            if (JOptionPane.showConfirmDialog(ConnectionProfilePane.this,
                "Are you sure you want to remove this profile?",
                "Remove connection profile", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_NORMAL_REMOVE)) ==
                JOptionPane.YES_OPTION) {
                context.getConnectionProfileModel().removeProfileAt(
                    profileTable.getSelectedRow());
                profileTable.repaint();
            }
        }
    }

    class EditAction
        extends AbstractEditAction {
        /**
         *  Constructor for the DeleteAction object
         */
        EditAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
        	editSelected();
        }
    }
}
