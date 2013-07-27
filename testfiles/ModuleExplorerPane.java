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

import gruntspud.CVSCommandHandler;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.connection.ConnectionProfile;
import gruntspud.connection.ConnectionProfileListModel;
import gruntspud.event.GruntspudCVSListener;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.preferences.ConnectionOptionTab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.checkout.ModuleListInformation;
import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.FileAddedEvent;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.FileUpdatedEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;
import org.netbeans.lib.cvsclient.event.ModuleExpansionEvent;
import org.netbeans.lib.cvsclient.event.TerminationEvent;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class ModuleExplorerPane
    extends JPanel
    implements GruntspudCVSListener {
    private JComboBox connectionProfile;
    private JList modules;
    private JButton maintain, reload;
    private Vector fileInfo;
    private GruntspudContext context;
    private boolean adjusting;

    /**
     * Creates a new ModuleExplorerPane object.
     *
     * @param context DOCUMENT ME!
     */
    public ModuleExplorerPane(GruntspudContext context) {
        super(new BorderLayout());

        this.context = context;

        //
        JPanel c = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        UIUtil.jGridBagAdd(c, new JLabel("Profile:"), gbc, 1);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(c,
                           connectionProfile = new JComboBox(
            new ConnectionProfileListModel(
            context.getConnectionProfileModel())), gbc, 1);
        connectionProfile.setRenderer(new ConnectionProfileComboBoxRenderer(
            context));
        connectionProfile.setSelectedItem(null);
        gbc.weightx = 0.0;
		reload = UIUtil.createButton(Constants.ICON_TOOL_SMALL_RELOAD, "Reload",
									   new ActionListener() {
			public void actionPerformed(ActionEvent evt) {

				if (!adjusting) {
					final CheckoutCommand cmd = new CheckoutCommand();
					cmd.setShowModulesWithStatus(true);
					CVSCommandHandler.getInstance().runCommandGroup(
						ModuleExplorerPane.this, ModuleExplorerPane.this.context, null,
						new Command[] {cmd}
						, null, null, false, null,
						( (ConnectionProfile) connectionProfile.getSelectedItem()), 
						ModuleExplorerPane.this, null);
				}
			}
		});
		reload.setBorder(null);
		UIUtil.jGridBagAdd(c, reload, gbc, GridBagConstraints.RELATIVE);
        maintain = UIUtil.createButton(Constants.ICON_TOOL_SMALL_CONNECT, "Maintain",
                                       new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                maintain();
            }
        });
		maintain.setBorder(null);
        UIUtil.jGridBagAdd(c, maintain, gbc, GridBagConstraints.REMAINDER);

        //
        JPanel t = new JPanel(new BorderLayout());
        t.add(c, BorderLayout.NORTH);
        t.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        //
        JPanel b = new JPanel(new BorderLayout());
        b.setBorder(BorderFactory.createTitledBorder("Modules"));
        modules = new JList(new ModuleListModel());
        modules.setCellRenderer(new ModuleListCellRenderer());

        JScrollPane moduleScroller = new JScrollPane(modules);
        b.add(moduleScroller, BorderLayout.CENTER);

        //
        add(t, BorderLayout.NORTH);
        add(b, BorderLayout.CENTER);
    }

    public String getShortName() {
        return "ModuleExplorer";
    }


    /**
     * DOCUMENT ME!
     */
    public void commandGroupStarted(Command[] cmd) {
        fileInfo = new Vector();
    }




    /**
     * DOCUMENT ME!
     */
    public void initListener(Component parent) {
    }
    
    /**
     * DOCUMENT ME!
     */
    public void commandUnitStarted(Command cmd) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param info DOCUMENT ME!
     * @param parent DOCUMENT ME!
     */
    public void commandGroupFinished() {
        FileInfoContainer[] i = new FileInfoContainer[fileInfo.size()];
        fileInfo.toArray(i);
        ( (ModuleListModel) modules.getModel()).setModules(i);
        fileInfo = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param info DOCUMENT ME!
     * @param parent DOCUMENT ME!
     */
    public void commandUnitFinished() {
    }

    public boolean isOptionalListener() {
        return false;
    }

    /* (non-Javadoc)
     * @see gruntspud.event.GruntspudCVSListener#isSelectedByDefault()
     */
    public boolean isSelectedByDefault() {
      // TODO Auto-generated method stub
      return false;
    }


    /**
     * DOCUMENT ME!
     *
     * @param t DOCUMENT ME!
     */
    public void commandException(Throwable t) {
    }

    public void messageSent(MessageEvent evt) {
    }

    public void fileAdded(FileAddedEvent e) {
    }

    public void fileRemoved(FileRemovedEvent e) {

    }

    public void fileUpdated(FileUpdatedEvent e) {
    }

    public void commandTerminated(TerminationEvent e) {
    }

    public void fileInfoGenerated(FileInfoEvent e) {
    	fileInfo.add(e.getInfoContainer());
    	
    }

    public void moduleExpanded(ModuleExpansionEvent e) {
    }


    private void maintain() {
        OptionDialog.Option ok = new OptionDialog.Option("Ok", "Ok", 'o');
        OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
            "Cancel", 'c');
        final ConnectionOptionTab conx = new ConnectionOptionTab();
        conx.init(context);

        OptionDialog.Option opt = OptionDialog.showOptionDialog("maintain",
            context, this, new OptionDialog.Option[] {ok, cancel}
            , conx,
            "Maintain Connection Profiles", ok,
            new OptionDialog.Callback() {
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
            adjusting = true;
            ( (ConnectionProfileListModel) connectionProfile.getModel()).
                refresh();
            adjusting = false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getSelectedModule() {
        ModuleListInformation i = (ModuleListInformation) modules.
            getSelectedValue();

        return (i == null) ? null : i.getModuleName();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public ConnectionProfile getSelectedConnectionProfile() {
        return ( (ConnectionProfile) connectionProfile.getSelectedItem());
    }

    public void messageSent(BinaryMessageEvent arg0) {
        throw new RuntimeException("Not yet implemented");

    }

    public void fileToRemove(FileToRemoveEvent arg0) {
        throw new RuntimeException("Not yet implemented");

    }

    class ModuleListModel
        extends AbstractListModel {
        private FileInfoContainer[] modules;

        public int getSize() {
            return (modules == null) ? 0 : modules.length;
        }

        public Object getElementAt(int i) {
            return modules[i];
        }

        public void setModules(FileInfoContainer[] modules) {
            this.modules = modules;
            fireContentsChanged(this, 0, getSize() - 1);
        }
    }
}
