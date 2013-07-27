/*
 * CipherPlugin - A jEdit plugin as framework for cipher implementations
 * :tabSize=4:indentSize=4:noTabs=true:
 *
 * Copyright (C) 2007 Björn "Vampire" Kautler
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package cipher;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;

import javax.swing.Box.Filler;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;

import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.ExtendedGridLayout;
import org.gjt.sp.jedit.gui.ExtendedGridLayoutConstraints;
import org.gjt.sp.jedit.gui.TextAreaDialog;

import org.gjt.sp.jedit.io.FileVFS;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;

import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchAndReplace;

import org.gjt.sp.util.Log;

import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;

import projectviewer.config.ProjectViewerConfig;

import projectviewer.importer.FileImporter;

import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTRoot;

import static java.awt.Cursor.WAIT_CURSOR;

import static java.awt.Component.RIGHT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;

import static java.util.Calendar.YEAR;

import static javax.swing.SwingConstants.RIGHT;

import static org.gjt.sp.jedit.browser.VFSBrowser.CHOOSE_DIRECTORY_DIALOG;

import static org.gjt.sp.jedit.io.VFSFile.DIRECTORY;

import static org.gjt.sp.util.Log.ERROR;

/**
 * A wizard for creating new {@code Cipher} implementations easily.
 * 
 * @author Björn "Vampire" Kautler
 * @since CipherPlugin 0.1
 */
public class NewImplementationWizard extends EnhancedDialog {
    private JLabel directoryLabel;
    private JTextField directoryTextField;
    private JButton chooseDirectoryButton;
    private JLabel authorForenameLabel;
    private JTextField authorForenameTextField;
    private JLabel authorSurnameLabel;
    private JTextField authorSurnameTextField;
    private JLabel authorEmailLabel;
    private JTextField authorEmailTextField;
    private JLabel pluginNameLabel;
    private JTextField pluginNameTextField;
    private JLabel packageNameLabel;
    private JTextField packageNameTextField;
    private JLabel servicesAPINameLabel;
    private JTextField servicesAPINameTextField;
    private JLabel shortDescriptionLabel;
    private JTextField shortDescriptionTextField;
    private JLabel availabilityLabel;
    private JRadioButton alwaysAvailableRadioButton;
    private Filler availabilityFiller;
    private JRadioButton sometimesAvailableRadioButton;
    private JLabel dataTypeLabel;
    private JRadioButton byteArrayDataRadioButton;
    private Filler dataTypeFiller;
    private JRadioButton stringDataRadioButton;
    private JCheckBox openFilesCheckBox;
    private JCheckBox createProjectCheckBox;
    private JButton createButton;
    
    /**
     * Constructs a new modal {@code NewImplementationWizard}.
     * 
     * @param directory The directory where to create the new implementation
     * @throws IllegalArgumentException If directory is not of type {@code VFSFile.DIRECTORY}
     * @see #init()
     */
    private NewImplementationWizard(@Nullable VFSFile directory) {
        super(jEdit.getActiveView(),jEdit.getProperty("cipher.new-implementation-wizard.title"),true);
        
        if ((null != directory) && (DIRECTORY != directory.getType())) {
            throw new IllegalArgumentException("directory is not of type VFSFile.DIRECTORY");
        }
        
        directoryLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.directory.label"),RIGHT);
        directoryLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        directoryTextField = new JTextField(null == directory ? jEdit.getProperty("options.cipher.directory") : directory.getPath());
        directoryTextField.setEditable(false);
        directoryTextField.setColumns(25);
        directoryTextField.setCaretPosition(0);
        directoryLabel.setLabelFor(directoryTextField);
        
        chooseDirectoryButton = new JButton(jEdit.getProperty("cipher.new-implementation-wizard.choose-button.label"));
        Dimension dim = chooseDirectoryButton.getMaximumSize();
        dim.height = Integer.MAX_VALUE;
        chooseDirectoryButton.setMaximumSize(dim);
        
        authorForenameLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.author-forename.label"),RIGHT);
        authorForenameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        authorForenameTextField = new JTextField(jEdit.getProperty("options.cipher.authorForename"));
        authorForenameTextField.setColumns(25);
        authorForenameLabel.setLabelFor(authorForenameTextField);
        
        authorSurnameLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.author-surname.label"),RIGHT);
        authorSurnameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        authorSurnameTextField = new JTextField(jEdit.getProperty("options.cipher.authorSurname"));
        authorSurnameTextField.setColumns(25);
        authorSurnameLabel.setLabelFor(authorSurnameTextField);
        
        authorEmailLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.author-email.label"),RIGHT);
        authorEmailLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        authorEmailTextField = new JTextField(jEdit.getProperty("options.cipher.authorEmail"));
        authorEmailTextField.setColumns(25);
        authorEmailLabel.setLabelFor(authorEmailTextField);
        
        pluginNameLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.plugin-name.label"),RIGHT);
        pluginNameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        pluginNameTextField = new JTextField(jEdit.getProperty("options.cipher.pluginName"));
        pluginNameTextField.setColumns(25);
        pluginNameLabel.setLabelFor(pluginNameTextField);
        
        packageNameLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.package-name.label"),RIGHT);
        packageNameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        packageNameTextField = new JTextField(jEdit.getProperty("options.cipher.packageName"));
        packageNameTextField.setColumns(25);
        packageNameLabel.setLabelFor(packageNameTextField);
        
        servicesAPINameLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.services-api-name.label"),RIGHT);
        servicesAPINameLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        servicesAPINameTextField = new JTextField(jEdit.getProperty("options.cipher.servicesAPIName"));
        servicesAPINameTextField.setColumns(25);
        servicesAPINameLabel.setLabelFor(servicesAPINameTextField);
        
        shortDescriptionLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.short-description.label"),RIGHT);
        shortDescriptionLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        shortDescriptionTextField = new JTextField(jEdit.getProperty("options.cipher.shortDescription"));
        shortDescriptionTextField.setColumns(25);
        shortDescriptionLabel.setLabelFor(shortDescriptionTextField);
        
        availabilityLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.availability.label"),RIGHT);
        availabilityLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        boolean alwaysAvailable = jEdit.getBooleanProperty("options.cipher.alwaysAvailable",true);
        alwaysAvailableRadioButton = new JRadioButton(jEdit.getProperty("cipher.new-implementation-wizard.always-available.label"),alwaysAvailable);
        availabilityLabel.setLabelFor(alwaysAvailableRadioButton);
        
        availabilityFiller = new Filler(availabilityLabel.getMinimumSize(),
                                        availabilityLabel.getPreferredSize(),
                                        availabilityLabel.getMaximumSize());
        
        sometimesAvailableRadioButton = new JRadioButton(jEdit.getProperty("cipher.new-implementation-wizard.sometimes-available.label"),!alwaysAvailable);
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(alwaysAvailableRadioButton);
        buttonGroup.add(sometimesAvailableRadioButton);
        
        dataTypeLabel = new JLabel(jEdit.getProperty("cipher.new-implementation-wizard.data-type.label"),RIGHT);
        dataTypeLabel.setAlignmentX(RIGHT_ALIGNMENT);
        
        boolean byteArrayData = jEdit.getBooleanProperty("options.cipher.byteArrayData",true);
        byteArrayDataRadioButton = new JRadioButton(jEdit.getProperty("cipher.new-implementation-wizard.byte-array-data.label"),byteArrayData);
        dataTypeLabel.setLabelFor(byteArrayDataRadioButton);
        
        dataTypeFiller = new Filler(dataTypeLabel.getMinimumSize(),
                                    dataTypeLabel.getPreferredSize(),
                                    dataTypeLabel.getMaximumSize());
        
        stringDataRadioButton = new JRadioButton(jEdit.getProperty("cipher.new-implementation-wizard.string-data.label"),!byteArrayData);
        
        buttonGroup = new ButtonGroup();
        buttonGroup.add(byteArrayDataRadioButton);
        buttonGroup.add(stringDataRadioButton);
        
        openFilesCheckBox = new JCheckBox(jEdit.getProperty("cipher.new-implementation-wizard.open-files.label"),jEdit.getBooleanProperty("options.cipher.openFiles",false));
        
        createProjectCheckBox = new JCheckBox(jEdit.getProperty("cipher.new-implementation-wizard.create-project.label"),jEdit.getBooleanProperty("options.cipher.createProject",true));
        if (null == jEdit.getPlugin("projectviewer.ProjectPlugin")) {
            createProjectCheckBox.setSelected(false);
            createProjectCheckBox.setEnabled(false);
        } else {
            directoryTextField.getDocument().addDocumentListener(new CreateProjectEnabledStateChanger());
        }
        
        createButton = new JButton(jEdit.getProperty("cipher.new-implementation-wizard.create-button.label"));
        dim = createButton.getMaximumSize();
        dim.width = Integer.MAX_VALUE;
        createButton.setMaximumSize(dim);
        getRootPane().setDefaultButton(createButton);
        
        setLayout(new ExtendedGridLayout(5,5,new Insets(5,5,5,5)));
        add(directoryLabel,null);
        add(directoryTextField,null);
        add(chooseDirectoryButton,null);
        
        add(authorForenameLabel,new ExtendedGridLayoutConstraints(1,authorForenameLabel));
        add(authorForenameTextField,new ExtendedGridLayoutConstraints(1,2,1,authorForenameTextField));
        
        add(authorSurnameLabel,new ExtendedGridLayoutConstraints(2,authorSurnameLabel));
        add(authorSurnameTextField,new ExtendedGridLayoutConstraints(2,2,1,authorSurnameTextField));
        
        add(authorEmailLabel,new ExtendedGridLayoutConstraints(3,authorEmailLabel));
        add(authorEmailTextField,new ExtendedGridLayoutConstraints(3,2,1,authorEmailTextField));
        
        add(pluginNameLabel,new ExtendedGridLayoutConstraints(4,pluginNameLabel));
        add(pluginNameTextField,new ExtendedGridLayoutConstraints(4,2,1,pluginNameTextField));
        
        add(packageNameLabel,new ExtendedGridLayoutConstraints(5,packageNameLabel));
        add(packageNameTextField,new ExtendedGridLayoutConstraints(5,2,1,packageNameTextField));
        
        add(servicesAPINameLabel,new ExtendedGridLayoutConstraints(6,servicesAPINameLabel));
        add(servicesAPINameTextField,new ExtendedGridLayoutConstraints(6,2,1,servicesAPINameTextField));
        
        add(shortDescriptionLabel,new ExtendedGridLayoutConstraints(7,shortDescriptionLabel));
        add(shortDescriptionTextField,new ExtendedGridLayoutConstraints(7,2,1,shortDescriptionTextField));
        
        add(availabilityLabel,new ExtendedGridLayoutConstraints(8,availabilityLabel));
        add(alwaysAvailableRadioButton,new ExtendedGridLayoutConstraints(8,2,1,alwaysAvailableRadioButton));
        add(availabilityFiller,new ExtendedGridLayoutConstraints(9,availabilityFiller));
        add(sometimesAvailableRadioButton,new ExtendedGridLayoutConstraints(9,2,1,sometimesAvailableRadioButton));
        
        add(dataTypeLabel,new ExtendedGridLayoutConstraints(10,dataTypeLabel));
        add(byteArrayDataRadioButton,new ExtendedGridLayoutConstraints(10,2,1,byteArrayDataRadioButton));
        add(dataTypeFiller,new ExtendedGridLayoutConstraints(11,dataTypeFiller));
        add(stringDataRadioButton,new ExtendedGridLayoutConstraints(11,2,1,stringDataRadioButton));
        
        add(openFilesCheckBox,new ExtendedGridLayoutConstraints(12,3,1,openFilesCheckBox));
        
        add(createProjectCheckBox,new ExtendedGridLayoutConstraints(13,3,1,createProjectCheckBox));
        
        add(createButton,new ExtendedGridLayoutConstraints(14,3,1,createButton));
    }
    
    /**
     * Initializes this {@code NewImplementationWizard} and shows it.
     */
    private void init() {
        chooseDirectoryButton.addActionListener(new DirectoryChooser());
        createButton.addActionListener(new ImplementationCreator());
        
        pack();
        GUIUtilities.loadGeometry(this,"cipher.new-implementation-wizard");
        setVisible(true);
    }
    
    /**
     * Creates a new modal {@code NewImplementationWizard},
     * initializes it and shows it.
     * 
     * @param directory The directory where to create the new implementation
     * @throws IllegalArgumentException If directory is not of type {@code VFSFile.DIRECTORY}
     * @return The newly created {@code NewImplementationWizard}
     */
    @NonNull
    public static NewImplementationWizard newInstance(@Nullable VFSFile directory) {
        NewImplementationWizard instance = new NewImplementationWizard(directory);
        instance.init();
        return instance;
    }
    
    /**
     * <p>Called when create button or Enter key is pressed.</p>
     * 
     * <p>Creates the new implementation out of the given
     * parameters if all are supplied.</p>
     * 
     * @see org.gjt.sp.jedit.gui.EnhancedDialog#ok()
     */
    @SuppressWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION",
                      justification = "The looping condition needs it as string, so there is no difference to use it as is or make it with StringBuilder")
    @Override
    public void ok() {
        String directory = directoryTextField.getText();
        String authorForename = authorForenameTextField.getText();
        String authorSurname = authorSurnameTextField.getText();
        String authorEmail = authorEmailTextField.getText();
        String pluginName = pluginNameTextField.getText();
        String packageName = packageNameTextField.getText();
        String servicesAPIName = servicesAPINameTextField.getText();
        String shortDescription = shortDescriptionTextField.getText();
        boolean alwaysAvailable = alwaysAvailableRadioButton.isSelected();
        boolean byteArrayData = byteArrayDataRadioButton.isSelected();
        boolean openFiles = openFilesCheckBox.isSelected();
        boolean createProject = createProjectCheckBox.isSelected();
        
        if ((directory.length() == 0) ||
            (authorForename.length() == 0) ||
            (authorSurname.length() == 0) ||
            (authorEmail.length() == 0) ||
            (pluginName.length() == 0) ||
            (packageName.length() == 0) ||
            (servicesAPIName.length() == 0) ||
            (shortDescription.length() == 0)) {
            GUIUtilities.error(NewImplementationWizard.this,"cipher.error.fill-all-fields",null);
        } else {
            String noWhitespacePluginName = pluginName.replaceAll(" ","");
            String newParent = MiscUtilities.constructPath(directory,noWhitespacePluginName);
            VFS vfs = VFSManager.getVFSForPath(newParent);
            Object session = vfs.createVFSSession(newParent,NewImplementationWizard.this);
            VFSFile newParentFile;
            try {
                newParentFile = vfs._getFile(session,newParent,NewImplementationWizard.this);
                vfs._endVFSSession(session,NewImplementationWizard.this);
            } catch (IOException ioe) {
                Log.log(ERROR,NewImplementationWizard.this,ioe);
                new TextAreaDialog(NewImplementationWizard.this,"cipher.error.error-while-checking-directory",ioe);
                return;
            }
            if (null != newParentFile) {
                GUIUtilities.error(NewImplementationWizard.this,"cipher.error.directory-already-exists",new String[] { newParent });
                return;
            }
            NewImplementationWizard.this.setCursor(Cursor.getPredefinedCursor(WAIT_CURSOR));
            GUIUtilities.saveGeometry(this,"cipher.new-implementation-wizard");
            jEdit.setProperty("options.cipher.directory",directory);
            jEdit.setProperty("options.cipher.authorForename",authorForename);
            jEdit.setProperty("options.cipher.authorSurname",authorSurname);
            jEdit.setProperty("options.cipher.authorEmail",authorEmail);
            jEdit.setProperty("options.cipher.pluginName",pluginName);
            jEdit.setProperty("options.cipher.packageName",packageName);
            jEdit.setProperty("options.cipher.servicesAPIName",servicesAPIName);
            jEdit.setProperty("options.cipher.shortDescription",shortDescription);
            jEdit.setBooleanProperty("options.cipher.alwaysAvailable",alwaysAvailable);
            jEdit.setBooleanProperty("options.cipher.byteArrayData",byteArrayData);
            jEdit.setBooleanProperty("options.cipher.openFiles",openFiles);
            jEdit.setBooleanProperty("options.cipher.createProject",createProject);
            jEdit.propertiesChanged();
            jEdit.saveSettings();
            
            String[] files;
            if (byteArrayData) {
                files = new String[] {
                    "jeditresource:Cipher.jar!/res/docs/PluginName.html.template",
                    "jeditresource:Cipher.jar!/res/docs/fdl.xml.template",
                    "jeditresource:Cipher.jar!/res/docs/users-guide.xml.template",
                    "jeditresource:Cipher.jar!/res/src/packageName/PluginNamePlugin.java.template",
                    "jeditresource:Cipher.jar!/res/src/packageName/PluginNameForByteArrays.java.template",
                    "jeditresource:Cipher.jar!/res/PluginName.props.template",
                    "jeditresource:Cipher.jar!/res/build.xml.template",
                    "jeditresource:Cipher.jar!/res/services.xml.template"
                };
            } else {
                files = new String[] {
                    "jeditresource:Cipher.jar!/res/docs/PluginName.html.template",
                    "jeditresource:Cipher.jar!/res/docs/fdl.xml.template",
                    "jeditresource:Cipher.jar!/res/docs/users-guide.xml.template",
                    "jeditresource:Cipher.jar!/res/src/packageName/PluginNamePlugin.java.template",
                    "jeditresource:Cipher.jar!/res/src/packageName/PluginNameForStrings.java.template",
                    "jeditresource:Cipher.jar!/res/PluginName.props.template",
                    "jeditresource:Cipher.jar!/res/build.xml.template",
                    "jeditresource:Cipher.jar!/res/services.xml.template"
                };
            }
            String packagePath = packageName.replaceAll("\\.","/");
            String[] newFiles = new String[] {
                "docs/" + noWhitespacePluginName + ".html",
                "docs/fdl.xml",
                "docs/users-guide.xml",
                "src/" + packagePath + "/" + noWhitespacePluginName + "Plugin.java",
                "src/" + packagePath + "/" + noWhitespacePluginName + ".java",
                noWhitespacePluginName + ".props",
                "build.xml",
                "services.xml"
            };
            String path = MiscUtilities.constructPath(newParent,"docs");
            session = vfs.createVFSSession(path,NewImplementationWizard.this);
            try {
                vfs._mkdir(session,path,NewImplementationWizard.this);
                vfs._endVFSSession(session,NewImplementationWizard.this);
                path = MiscUtilities.constructPath(newParent,"src",packagePath);
                session = vfs.createVFSSession(path,NewImplementationWizard.this);
                vfs._mkdir(session,path,NewImplementationWizard.this);
                vfs._endVFSSession(session,NewImplementationWizard.this);
            } catch (IOException ioe) {
                Log.log(ERROR,NewImplementationWizard.this,ioe);
                new TextAreaDialog(NewImplementationWizard.this,"cipher.error.error-creating-directories",ioe);
                dispose();
                return;
            }
            Iterator<String> newFilesIterator = Arrays.asList(newFiles).iterator();
            try {
                for (String file : files) {
                    String newPath = MiscUtilities.constructPath(newParent,newFilesIterator.next());
                    VFS.copy(null,file,newPath,NewImplementationWizard.this,false);
                }
            } catch (IOException ioe) {
                Log.log(ERROR,NewImplementationWizard.this,ioe);
                new TextAreaDialog(NewImplementationWizard.this,"cipher.error.error-copying-files",ioe);
                dispose();
                return;
            }
            
            SearchAndReplace.setBeanShellReplace(false);
            SearchAndReplace.setIgnoreCase(false);
            SearchAndReplace.setRegexp(false);
            SearchAndReplace.setSearchFileSet(new DirectoryListSet(newParent,"*",true));
            View view = jEdit.getActiveView();
            
            SearchAndReplace.setSearchString("@shortDescription@");
            SearchAndReplace.setReplaceString(shortDescription);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@noWhitespacePluginName@");
            SearchAndReplace.setReplaceString(noWhitespacePluginName);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@currentYear@");
            SearchAndReplace.setReplaceString(Integer.toString(Calendar.getInstance().get(YEAR)));
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@authorForename@");
            SearchAndReplace.setReplaceString(authorForename);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@authorSurname@");
            SearchAndReplace.setReplaceString(authorSurname);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@authorEmail@");
            SearchAndReplace.setReplaceString(authorEmail);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@pluginName@");
            SearchAndReplace.setReplaceString(pluginName);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@packageName@");
            SearchAndReplace.setReplaceString(packageName);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            SearchAndReplace.setSearchString("@servicesAPIName@");
            SearchAndReplace.setReplaceString(servicesAPIName);
            SearchAndReplace.replaceAll(view,true);
            
            VFSManager.waitForRequests();
            if (alwaysAvailable) {
                SearchAndReplace.setSearchString("@availabilityDocumentation@");
                SearchAndReplace.setReplaceString("<p>This implementation is always available.</p>");
                SearchAndReplace.replaceAll(view,true);
                
                VFSManager.waitForRequests();
                SearchAndReplace.setSearchString("@availabilityReturn@");
                SearchAndReplace.setReplaceString("{@code true}");
                SearchAndReplace.replaceAll(view,true);
                
                VFSManager.waitForRequests();
                SearchAndReplace.setSearchString("@availabilityCheck@");
                SearchAndReplace.setReplaceString("return true;");
                SearchAndReplace.replaceAll(view,true);
            } else {
                SearchAndReplace.setSearchString("@availabilityDocumentation@");
                SearchAndReplace.setReplaceString(
                           "<p>TODO: Document availability here</p>\n" +
                    "     * <p>This implementation is sometimes unavailable.</p>");
                SearchAndReplace.replaceAll(view,true);
                
                VFSManager.waitForRequests();
                SearchAndReplace.setSearchString("@availabilityReturn@");
                SearchAndReplace.setReplaceString("Whether the implementation is currently available");
                SearchAndReplace.replaceAll(view,true);
                
                VFSManager.waitForRequests();
                SearchAndReplace.setSearchString("@availabilityCheck@");
                SearchAndReplace.setReplaceString(
                            "// TODO: implement availability check here\n" +
                    "        if (true) {\n" +
                    "            return true;\n" +
                    "        } else {\n" +
                    "            return false;\n" +
                    "        }");
                SearchAndReplace.replaceAll(view,true);
            }
            
            VFSManager.waitForRequests();
            if (openFiles) {
                jEdit.openFiles(view,newParent,newFiles);
            }
            
            if (createProject) {
                String projectName = pluginName;
                ProjectManager projectManager = ProjectManager.getInstance();
                while (projectManager.hasProject(projectName)) {
                    projectName += " 42";
                }
                VPTProject project = new VPTProject(projectName);
                project.setRootPath(newParent);
                ProjectViewer viewer = ProjectViewer.getViewer(view);
                CipherImporter importer = new CipherImporter(project,viewer);
                importer.doImport();
                projectManager.addProject(project,VPTRoot.getInstance());
                if (viewer != null) {
                    viewer.setRootNode(project);
                } else {
                    ProjectViewerConfig.getInstance().setLastNode(project);
                }
            }
            
            dispose();
        }
    }
    
    /**
     * <p>Called when ESC button is pressed or window is closed.</p>
     * 
     * @see org.gjt.sp.jedit.gui.EnhancedDialog#cancel()
     */
    @Override
    public void cancel() {
        GUIUtilities.saveGeometry(this,"cipher.new-implementation-wizard");
        dispose();
    }
    
    /**
     * Offers a directory chooser dialog and sets the directory
     * text fields text to the chosen directory.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private class DirectoryChooser implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param ae The {@code ActionEvent} of the action that occured
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(@Nullable ActionEvent ae) {
            String[] chosenDirectories = GUIUtilities.showVFSFileDialog(jEdit.getActiveView(),
                                                                        directoryTextField.getText(),
                                                                        CHOOSE_DIRECTORY_DIALOG,
                                                                        false);
            if (null != chosenDirectories) {
                directoryTextField.setText(chosenDirectories[0]);
                directoryTextField.setCaretPosition(0);
            }
        }
    }
    
    /**
     * Monitors the directory textfield and disables the create project
     * checkbox if a non-local directory got chosen, or enables it if a
     * local directory got chosen.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private class CreateProjectEnabledStateChanger implements DocumentListener {
        private boolean wasSelected;
        
        /**
         * Constructs a new {@code CreateProjectEnabledStateChanger}.
         */
        @CheckReturnValue(explanation = "The listener should get attached to some document")
        public CreateProjectEnabledStateChanger() {
            if (!(VFSManager.getVFSForPath(directoryTextField.getText()) instanceof FileVFS)) {
                wasSelected = createProjectCheckBox.isSelected();
                createProjectCheckBox.setSelected(false);
                createProjectCheckBox.setEnabled(false);
            }
        }
        
        /**
         * Gives notification that there was an insert into the document.
         * The range given by the DocumentEvent bounds the freshly inserted region.
         * 
         * @param de the document event
         */
        public void insertUpdate(@Nullable DocumentEvent de) {
            if (VFSManager.getVFSForPath(directoryTextField.getText()) instanceof FileVFS) {
                if (!createProjectCheckBox.isEnabled()) {
                    createProjectCheckBox.setEnabled(true);
                    createProjectCheckBox.setSelected(wasSelected);
                }
            } else {
                if (createProjectCheckBox.isEnabled()) {
                    wasSelected = createProjectCheckBox.isSelected();
                    createProjectCheckBox.setSelected(false);
                    createProjectCheckBox.setEnabled(false);
                }
            }
        }
        
        /**
         * Gives notification that a portion of the document has been removed.
         * The range is given in terms of what the view last saw
         * (that is, before updating sticky positions).
         * 
         * @param de the document event
         */
        public void removeUpdate(@Nullable DocumentEvent de) {
            // we are not interested in these events
        }
        
        /**
         * Gives notification that an attribute or set of attributes changed.
         * 
         * @param de the document event
         */
        public void changedUpdate(@Nullable DocumentEvent de) {
            // we are not interested in these events
        }
    }
    
    /**
     * Creates the new implementation out of the given
     * parameters if all are supplied.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private class ImplementationCreator implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param ae The {@code ActionEvent} of the action that occured
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(@Nullable ActionEvent ae) {
            ok();
        }
    }
    
    /**
     * Imports the files of the newly created project.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private static class CipherImporter extends FileImporter {
        /**
         * Constructs a new {@code CipherImporter}.
         * 
         * @param node The node in which to import the files
         * @param viewer The viewer that is used
         */
        @CheckReturnValue(explanation = "The Importer should be used to import some files")
        public CipherImporter(@Nullable VPTNode node, @Nullable ProjectViewer viewer) {
            super(node, viewer);
        }
        
        /**
         * This method does the actual import.
         * 
         * @see projectviewer.importer.Importer#internalDoImport()
         */
        @Override
        @CheckForNull
        protected Collection internalDoImport() {
            String state = null;
            if (viewer != null) {
                state = viewer.getFolderTreeState(project);
            }
            addTree(new File(project.getRootPath()),project,null,false);
            if (state != null) {
                postAction = new NodeStructureChange(project,state);
            }
            return null;
        }
    }
}
