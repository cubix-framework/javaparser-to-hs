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

package gruntspud.ui.commandoptions;

import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.ConnectionProfileChooserPane;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.ModuleExplorerPane;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.icons.EmptyIcon;
import gruntspud.ui.preferences.AbstractOptionsTab;
import gruntspud.ui.preferences.GlobalOptionsTab;
import gruntspud.ui.preferences.StickyOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.export.ExportCommand;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CheckoutOptionsPane
    extends JFileChooser {
    private GruntspudCheckBox doNotRecurse;
    private GruntspudCheckBox resetStickyOnes;
    private GruntspudCheckBox pruneDirectories;
    private GruntspudCheckBox export;
    private GruntspudCheckBox useAsHome;
    private CheckoutGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private StringListComboBox module;
    private StringListComboBox checkoutAs;
    private StickyOptionsTab stickyTab;
    private Tabber tabber;
    private JButton browseForModule;
    private ConnectionProfileChooserPane connectionProfile;

    /**
     *  Constructor for the CheckoutOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public CheckoutOptionsPane(GruntspudContext context) {
        super();

        File h = new File(context.getHost().getProperty(Constants.
            CHECKOUT_LAST_DIRECTORY,
            System.getProperty("user.home")));

        if (Gruntspud.is14Plus()) {
            setCurrentDirectory(h);
        }
        else {
            setCurrentDirectory(h.getParentFile());
            setSelectedFile(h);
        }

        this.context = context;

        tabber = new Tabber();
        generalTab = new CheckoutGeneralTab();
        generalTab.init(context);
        tabber.addTab(generalTab);
        stickyTab = new StickyOptionsTab(UIUtil.getCachedIcon(
            Constants.ICON_TOOL_STICKY),
                                         UIUtil.getCachedIcon(Constants.
            ICON_TOOL_LARGE_STICKY), false);
        stickyTab.init(context);
        tabber.addTab(stickyTab);
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);
        tabber.addTab(globalOptionsTab);
        tabber.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        setAccessory(tabber);
        setApproveButtonMnemonic('o');
        setApproveButtonText("Checkout");
        setApproveButtonToolTipText("Start the checkout");
        setDialogTitle("Checkout to local directory");
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setMultiSelectionEnabled(false);
    }

    /**
     * Return the selected connection profile
     *
     * @return connection profile
     */
    public ConnectionProfile getSelectedConnectionProfile() {
		return connectionProfile.getSelectedProfile();
	}

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateOptions() {
        File f = getDestinationDirectory();
		Constants.UI_LOG.debug("Destination directory is " + f.getAbsolutePath());
        if (f.exists()) {
            File z = new File(f,
                              (getCheckoutAsName() == null || getCheckoutAsName().equals("")) ? getModuleName()
                              : getCheckoutAsName());
			Constants.UI_LOG.debug("Checkout to " + f.getAbsolutePath());

            if (z.exists() &&
                (JOptionPane.showConfirmDialog(this,
                "Directory already exists. Are you sure?",
                "Directory exists", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)) {
                return false;
            }
        }

        return tabber.validateTabs();
    }

    /**
     *  Description of the Method
     */
    public void applyOptions() {
        File f = getCurrentDirectory();

        if (f != null) {
            context.getHost().setProperty(Constants.CHECKOUT_LAST_DIRECTORY,
                                          f.getAbsolutePath());

        }
        tabber.applyTabs();
    }

    /**
     *  Description of the Method
     */
    public void tabSelected() {
    }

    /**
     * Is this an export?
     *
     * @return an export
     */
    public boolean isExport() {
        return export.isSelected();
    }

    /**
     *  Gets the commandsForSettings attribute of the CommitInfoPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        if (export.isSelected()) {
            ExportCommand cmd = new ExportCommand();

            cmd.setModules(new String[] { String.valueOf(module.getSelectedItem()) });
            cmd.setPruneDirectories(pruneDirectories.isSelected());
            cmd.setRecursive(!doNotRecurse.isSelected());

            if ( (checkoutAs.getSelectedItem() != null) &&
                (checkoutAs.getSelectedItem().toString().length() > 0)) {
                cmd.setExportDirectory(checkoutAs.getSelectedItem().toString());

                //  Sticky options
            }
            String d = stickyTab.getSelectedDate();

            if ( (d != null) && (d != null)) {
                cmd.setExportByDate(stickyTab.getSelectedDate());

            }
            String r = stickyTab.getSelectedRevision();

            if ( (r != null) && (r != null)) {
                cmd.setExportByRevision(stickyTab.getSelectedRevision());

            }
            cmd.setUseHeadIfNotFound(stickyTab.isUseHeadIfNotFound());

            return new Command[] {
                cmd};
        }
        else {
            CheckoutCommand cmd = new CheckoutCommand();

            //  General
            cmd.setModule(String.valueOf(module.getSelectedItem()));
            cmd.setPruneDirectories(pruneDirectories.isSelected());
            cmd.setResetStickyOnes(resetStickyOnes.isSelected());
            cmd.setRecursive(!doNotRecurse.isSelected());

            if ( (checkoutAs.getSelectedItem() != null) &&
                (checkoutAs.getSelectedItem().toString().length() > 0)) {
                cmd.setCheckoutDirectory(checkoutAs.getSelectedItem().toString());

                //  Sticky options
            }
            String d = stickyTab.getSelectedDate();

            if ( (d != null) && (d != null)) {
                cmd.setCheckoutByDate(stickyTab.getSelectedDate());

            }
            String r = stickyTab.getSelectedRevision();

            if ( (r != null) && (r != null)) {
                cmd.setCheckoutByRevision(stickyTab.getSelectedRevision());

            }
            cmd.setUseHeadIfNotFound(stickyTab.isUseHeadIfNotFound());

            //
            return new Command[] {
                cmd};
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public File getDestinationDirectory() {
        File f = getSelectedFile();

        if (f == null) {
            f = getCurrentDirectory();

        }
        return f;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getModuleName() {
        return (String) module.getSelectedItem();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getCheckoutAsName() {
        return (String) checkoutAs.getSelectedItem();
    }

    class CheckoutGeneralTab
        extends AbstractOptionsTab
        implements ActionListener {
        /**
         *  Constructor for the CheckoutGeneralTab object
         */
        CheckoutGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_CHECKOUT));
        }

        public void init(GruntspudContext context) {
            super.init(context);
            setTabToolTipText("General options for an update");
            setLayout(new GridBagLayout());
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_CHECKOUT));
            setTabMnemonic('g');

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            UIUtil.jGridBagAdd(this, new JLabel("Module:"), gbc, 1);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               module = new StringListComboBox(context,
                context.getHost().getProperty(Constants.MODULES, ""),
                true), gbc, GridBagConstraints.RELATIVE);
            gbc.weightx = 0.0;
            browseForModule = UIUtil.createButton(Constants.ICON_TOOL_SMALL_BROWSE,
                                                  "Browse",
                                                  new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    browseForModule();
                }
            });
            browseForModule.setBorder(null);
            UIUtil.jGridBagAdd(this, browseForModule, gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Checkout as:"), gbc, 1);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               checkoutAs = new StringListComboBox(context,
                context.getHost().getProperty(Constants.CHECKOUT_AS, ""),
                true), gbc, GridBagConstraints.RELATIVE);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel(new EmptyIcon(16, 16)), gbc,
                               GridBagConstraints.REMAINDER);

            UIUtil.jGridBagAdd(this, new JLabel("Profile:"), gbc, 1);
            gbc.weightx = 2.0;
            gbc.fill = GridBagConstraints.BOTH;
            
            UIUtil.jGridBagAdd(this,
                               connectionProfile = new ConnectionProfileChooserPane(context),
								gbc, GridBagConstraints.REMAINDER);
			gbc.weighty = 0.0;			
            gbc.weighty = 1.0;
            UIUtil.jGridBagAdd(this,
                Box.createHorizontalStrut(1),
					gbc, GridBagConstraints.REMAINDER);
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            UIUtil.jGridBagAdd(this, new JSeparator(JSeparator.HORIZONTAL),
                               gbc, GridBagConstraints.REMAINDER);

            export = new GruntspudCheckBox("No CVS files (export)",
                                   context.getHost().getBooleanProperty(
                Constants.CHECKOUT_USE_EXPORT_INSTEAD,
                false));
            export.setMnemonic('x');
            export.addActionListener(this);
            UIUtil.jGridBagAdd(this, export, gbc, GridBagConstraints.REMAINDER);

            gbc.weightx = 2.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse directories (-l)",
                                         context.getHost().getBooleanProperty(
                Constants.DO_NOT_RECURSE,
                false));
            doNotRecurse.setMnemonic('d');
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);
            resetStickyOnes = new GruntspudCheckBox("Reset sticky ones (-l)",
                                            context.getHost().
                                            getBooleanProperty(Constants.
                RESET_STICKY_ONES,
                false));
            resetStickyOnes.setMnemonic('r');
            UIUtil.jGridBagAdd(this, resetStickyOnes, gbc,
                               GridBagConstraints.REMAINDER);
            pruneDirectories = new GruntspudCheckBox("Prune directories (-P)",
                                             context.getHost().
                                             getBooleanProperty(Constants.
                PRUNE_DIRECTORIES,
                true));
            pruneDirectories.setMnemonic('p');
            UIUtil.jGridBagAdd(this, pruneDirectories, gbc,
                               GridBagConstraints.REMAINDER);

            useAsHome = new GruntspudCheckBox("Use module root as home directory",
                                      context.getHost().getBooleanProperty(
                Constants.CHECKOUT_USE_AS_HOME,
                true));
            useAsHome.setMnemonic('h');
            UIUtil.jGridBagAdd(this, useAsHome, gbc,
                               GridBagConstraints.REMAINDER);
            gbc.weighty = 1.0;
            UIUtil.jGridBagAdd(this,
                Box.createHorizontalStrut(1),
					gbc, GridBagConstraints.REMAINDER);

            setAvailableActions();
        }

        private void browseForModule() {
            OptionDialog.Option select = new OptionDialog.Option("Select",
                "Select Module", 's');
            OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
                "Cancel", 'c');
            final ModuleExplorerPane me = new ModuleExplorerPane(context);
            OptionDialog.Option opt = OptionDialog.showOptionDialog("modules",
                context, this,
                new OptionDialog.Option[] {select, cancel}
                , me,
                "Module Explorer", select,
                new OptionDialog.Callback() {
                public boolean canClose(OptionDialog dialog,
                                        OptionDialog.Option option) {
                    return true;
                }

                public void close(OptionDialog dialog, Option option) {
                    // TODO Auto-generated method stub
                    
                }
            });

            if (opt != select) {
                return;
            }

            ConnectionProfile p = me.getSelectedConnectionProfile();

            if (p != null) {
            	connectionProfile.setSelectedProfile(p);
            }

            String m = me.getSelectedModule();

            if (m != null) {
                module.setSelectedItem(m);
            }
        }

        public void actionPerformed(ActionEvent evt) {
            setAvailableActions();
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            getContext().getHost().setBooleanProperty(Constants.DO_NOT_RECURSE,
                doNotRecurse.isSelected());
            getContext().getHost().setBooleanProperty(Constants.
                RESET_STICKY_ONES,
                resetStickyOnes.isSelected());
            getContext().getHost().setBooleanProperty(Constants.
                PRUNE_DIRECTORIES,
                pruneDirectories.isSelected());
            getContext().getHost().setProperty(Constants.MODULES,
                                               module.
                                               getStringListPropertyString());
            getContext().getHost().setProperty(Constants.CHECKOUT_AS,
                                               checkoutAs.
                                               getStringListPropertyString());
            getContext().getHost().setBooleanProperty(Constants.
                CHECKOUT_USE_EXPORT_INSTEAD,
                export.isSelected());
            getContext().getHost().setBooleanProperty(Constants.
                CHECKOUT_USE_AS_HOME,
                useAsHome.isSelected());
        }

        public void tabSelected() {
        }

        private void setAvailableActions() {
            resetStickyOnes.setEnabled(!export.isSelected());
        }
    }
}
