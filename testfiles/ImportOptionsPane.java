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

import gruntspud.CVSSubstType;
import gruntspud.CVSSubstTypeFilter;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.ConnectionProfileChooserPane;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.MessageTab;
import gruntspud.ui.ScanningSubstTypeFilterModel;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.SubstTypeFilterPane;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ProgressMonitor;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.importcmd.ImportBuilder;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.util.IgnoreFileFilter;
import org.netbeans.lib.cvsclient.util.SimpleStringPattern;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class ImportOptionsPane
    extends JFileChooser
    implements PropertyChangeListener {
    private ImportGeneralTab generalTab;
    private MessageTab messageTab;
    private GlobalOptionsTab globalOptionsTab;
    private ImportFilterTab filesTab;
    private GruntspudContext context;
    private StringListComboBox module;
    private StringListComboBox releaseTag;
    private StringListComboBox vendorTag;
    private StringListComboBox vendorBranch;
    private Tabber tabber;
    private IgnoreFileFilter ignoreFileFilter;
    private ScanningSubstTypeFilterModel filterModel;
    private ConnectionProfileChooserPane connectionProfile;

    /**
     *  Constructor for the CommitInfoPane object
     *
     *@param  host  Description of the Parameter
     */
    public ImportOptionsPane(GruntspudContext context) {
        super();

        this.context = context;

        tabber = new Tabber();
        tabber.addTab(generalTab = new ImportGeneralTab());

        messageTab = new MessageTab("Log message for the import",
                                    UIUtil.getCachedIcon(Constants.
            ICON_TOOL_NOTES),
                                    UIUtil.getCachedIcon(Constants.
            ICON_TOOL_LARGE_NOTES), 25);
        messageTab.init(context);
        tabber.addTab(messageTab);

        filesTab = new ImportFilterTab();
        tabber.addTab(filesTab);

        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);
        tabber.addTab(globalOptionsTab);

        tabber.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        setAccessory(tabber);
        setApproveButtonMnemonic('o');
        setApproveButtonText("Import");
        setApproveButtonToolTipText("Start the import");
        setDialogTitle("Import a local directory to a remote repository");
        setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        setMultiSelectionEnabled(false);

        //        rescanCurrentDirectory();
        //        filesTab.rescan();
        addPropertyChangeListener(this);

        setCurrentDirectory(new File(context.getHost().getProperty(Constants.
            IMPORT_LAST_DIRECTORY,
            System.getProperty("user.home"))));

        String s = context.getHost().getProperty(Constants.
                                                 IMPORT_SELECTED_DIRECTORY,
                                                 "");

        if (!s.equals("")) {
            setSelectedFile(new File(s));
        }
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *           and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(JFileChooser.
                                         DIRECTORY_CHANGED_PROPERTY) ||
            evt.getPropertyName().equals(JFileChooser.
                                         SELECTED_FILE_CHANGED_PROPERTY)) {
            filesTab.setRootDirectory(getSelectedFile());
        }
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateOptions() {
        if (messageTab.getText().equals("")) {
            JOptionPane.showMessageDialog(this, "Message must be supplied.",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            tabber.setSelectedIndex(1);
            messageTab.grabFocus();

            return false;
        }

        return tabber.validateTabs();
    }

    /**
     *  Description of the Method
     */
    public void applyOptions() {
        context.getHost().setProperty(Constants.IMPORT_LAST_DIRECTORY,
                                      getCurrentDirectory().getAbsolutePath());
        context.getHost().setProperty(Constants.IMPORT_SELECTED_DIRECTORY,
                                      getSelectedFile().getAbsolutePath());
        tabber.applyTabs();
    }

    /**
     *
     * @return
     */
    public IgnoreFileFilter getIgnoreFileFilter() {
        return new ImportIgnoreFilter();
    }

    /**
     *
     * @return
     */
    public ConnectionProfile getProfile() {
        return connectionProfile.getSelectedProfile();
    }

    /**
     *  Gets the commandsForSettings attribute of the CommitInfoPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        ImportCommand cmd = new ImportCommand();
        ImportBuilder ib;
        cmd.setLogMessage(messageTab.getText());
        cmd.setModule( (String) module.getSelectedItem());
        cmd.setReleaseTag( (String) releaseTag.getSelectedItem());
        cmd.setVendorTag( (String) vendorTag.getSelectedItem());

        if ( (vendorBranch.getSelectedItem() != null) &&
            (vendorBranch.getSelectedItem().toString().length() > 0)) {
            cmd.setVendorBranch( (String) vendorBranch.getSelectedItem());

        }
        cmd.setWrappers(filesTab.getWrappers());

        return new Command[] {
            cmd};
    }

    class ImportGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitMessageTab object
         */
        ImportGeneralTab() {
            super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_IMPORT));
            setTabToolTipText("General options for the import");
            setLayout(new GridBagLayout());
            setTabMnemonic('m');
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_IMPORT));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.BOTH;

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Module: "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               module = new StringListComboBox(context,
                context.getHost().getProperty(Constants.MODULES, ""),
                false), gbc, GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Release tag: "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               releaseTag = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              IMPORT_GENERAL_RELEASE_TAG,
                                              ""), true), gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Vendor tag: "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               vendorTag = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              IMPORT_GENERAL_VENDOR_TAG,
                                              ""), false), gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Vendor branch: "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            UIUtil.jGridBagAdd(this,
                               vendorBranch = new StringListComboBox(context,
                context.getHost().getProperty(Constants.
                                              IMPORT_GENERAL_VENDOR_BRANCH,
                                              ""), true), gbc,
                               GridBagConstraints.REMAINDER);

            gbc.weightx = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;
            UIUtil.jGridBagAdd(this, new JLabel("Profile:"), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            UIUtil.jGridBagAdd(this,
                               connectionProfile = new ConnectionProfileChooserPane(context), gbc,
                               GridBagConstraints.REMAINDER);
            gbc.weighty = 1.0;
            gbc.weightx = 2.0;
            UIUtil.jGridBagAdd(this,Box.createHorizontalStrut(1), gbc,
                GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            try {
                if ( (module.getSelectedItem() == null) ||
                    (module.getSelectedItem().toString().length() == 0)) {
                    throw new Exception("No module specified");
                }

                if ( (vendorTag.getSelectedItem() == null) ||
                    (vendorTag.getSelectedItem().toString().length() == 0)) {
                    throw new Exception("No vendor tag specified");
                }

                if ( (releaseTag.getSelectedItem() == null) ||
                    (releaseTag.getSelectedItem().toString().length() == 0)) {
                    throw new Exception("No release tag specified");
                }
            }
            catch (Exception e) {
                GruntspudUtil.showErrorMessage(this, "Error", e);
                return false;
            }

            return true;
        }

        public void applyTab() {
            context.getHost().setProperty(Constants.MODULES,
                                          module.getStringListPropertyString());
            context.getHost().setProperty(Constants.IMPORT_GENERAL_RELEASE_TAG,
                                          releaseTag.
                                          getStringListPropertyString());
            context.getHost().setProperty(Constants.IMPORT_GENERAL_VENDOR_TAG,
                                          vendorTag.getStringListPropertyString());
            context.getHost().setProperty(Constants.
                                          IMPORT_GENERAL_VENDOR_BRANCH,
                                          vendorBranch.
                                          getStringListPropertyString());
        }

        public void tabSelected() {
        }
    }

    class ImportFilterTab
        extends AbstractTab
        implements Runnable {
        private SubstTypeFilterPane filter;
        private GruntspudCheckBox automaticallyRescan;
        private JButton rescan;
        private File root;
        private ProgressMonitor monitor;

        /**
         *  Constructor for the CommitMessageTab object
         */
        ImportFilterTab() {
            super("Filter", UIUtil.getCachedIcon(Constants.ICON_TOOL_FILTER));
            setTabToolTipText("Filter the files types to include in the import");
            setTabLargeIcon(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_FILTER));
            setLayout(new BorderLayout());
            setTabMnemonic('f');

            JPanel s = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            s.setOpaque(false);
            s.add(automaticallyRescan = new GruntspudCheckBox("Automatically rescan"));
            automaticallyRescan.setSelected(context.getHost()
                                            .getBooleanProperty(Constants.
                IMPORT_AUTOMATICALLY_RESCAN));
            s.add(automaticallyRescan);
            s.add(rescan = new JButton("Rescan"));
            rescan.setMnemonic('r');
            rescan.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    rescan();
                }
            });

            //
            filterModel = new ScanningSubstTypeFilterModel(context);
            filter = new SubstTypeFilterPane(filterModel, true,
                                             Constants.
                                             IMPORT_FILE_TABLE_GEOMETRY,
                                             context);

            JScrollPane filterScroller = new JScrollPane(filter);
            JScrollPane scroller = new JScrollPane(filter);

            add(scroller, BorderLayout.CENTER);
            add(s, BorderLayout.SOUTH);
        }

        public void setRootDirectory(File root) {
            this.root = root;

            if (automaticallyRescan.isSelected()) {
                rescan();
            }
        }

        private synchronized void rescan() {
            if (root != null) {
                monitor = new ProgressMonitor(ImportOptionsPane.this,
                                              "Scanning files",
                                              "Scanning files", 0, 100);
                monitor.setMillisToDecideToPopup(50);
                monitor.setMillisToPopup(50);

                Thread t = new Thread(this);
                t.start();
            }
        }

        public Map getWrappers() {
            HashMap map = new HashMap();

            for (int i = 0; i < filterModel.getRowCount(); i++) {
                CVSSubstTypeFilter filter = filterModel.getSubstTypeFilterAt(i);

                if (filter.getType() != CVSSubstType.CVS_SUBST_TYPE_IGNORED) {
                    String f = null;

                    if (filter.getExtension().startsWith(".")) {
                        f = "*" + filter.getExtension();
                    }
                    else {
                        f = filter.getExtension();

                    }
                    map.put(new SimpleStringPattern(f),
                            filter.getType().getKeywordSubstitutionOptions());
                }
            }

            return map;
        }

        public void run() {
            filterModel.setRootDirectory(root, monitor);
            monitor.close();
        }

        public boolean validateTab() {
            try {
                if (filterModel.getRowCount() == 0) {
                    throw new IOException(
                        "File types havn't been scanned or there are no " +
                        "files to import");
                }
            }
            catch (Exception e) {
                GruntspudUtil.showErrorMessage(this, "Error", e);
                return false;
            }

            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(Constants.
                                                 IMPORT_AUTOMATICALLY_RESCAN,
                                                 automaticallyRescan.isSelected());
            filter.cleanUp();
        }

        public void tabSelected() {
        }
    }

    class ImportIgnoreFilter
        implements IgnoreFileFilter {
        public boolean shouldBeIgnored(File directory, String noneCvsFile) {
            for (int i = 0; i < filterModel.getRowCount(); i++) {
                CVSSubstTypeFilter filter = filterModel.getSubstTypeFilterAt(i);

                if (filter.getType() == CVSSubstType.CVS_SUBST_TYPE_IGNORED) {
                    String f = null;

                    if ( (filter.getExtension().startsWith(".") &&
                          noneCvsFile.endsWith(filter.getExtension())) ||
                        (!filter.getExtension().startsWith(".") &&
                         noneCvsFile.equals(filter.getExtension()))) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
