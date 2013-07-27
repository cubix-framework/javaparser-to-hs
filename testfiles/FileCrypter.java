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

import java.awt.Dimension;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JRadioButton;

import javax.swing.Box.Filler;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.MiscUtilities;

import org.gjt.sp.jedit.browser.VFSFileChooserDialog;

import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.ExtendedGridLayout;
import org.gjt.sp.jedit.gui.ExtendedGridLayoutConstraints;
import org.gjt.sp.jedit.gui.TextAreaDialog;

import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;

import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.Log;

import static java.awt.Component.RIGHT_ALIGNMENT;

import static java.awt.event.ItemEvent.SELECTED;

import static javax.swing.SwingConstants.RIGHT;

import static org.gjt.sp.jedit.browser.VFSBrowser.CHOOSE_DIRECTORY_DIALOG;
import static org.gjt.sp.jedit.browser.VFSBrowser.OPEN_DIALOG;

import static org.gjt.sp.jedit.io.VFSFile.FILE;

import static org.gjt.sp.util.Log.ERROR;

/**
 * A dialog for encrypting and decrypting files and directories.
 * 
 * @author Björn "Vampire" Kautler
 * @since CipherPlugin 0.1
 */
public class FileCrypter extends EnhancedDialog {
    private enum NewFileHandling {
        OVERWRITE,
        OTHER_DIRECTORY,
        SUFFIX
    }
    
    private boolean encrypt;
    private VFSFile[] files;
    private NewFileHandling newFileHandling;
    @SuppressWarnings(value = "SE_BAD_FIELD",
                      justification = "The class will not be serialized")
    private Cipher cipher;
    private Object[] additionalInformation;
    private String password;
    
    private CipherOptionPane cipherOptionPane;
    private JLabel newFileHandlingLabel;
    private Filler firstColumnFiller;
    private JRadioButton overwriteRadioButton;
    private JRadioButton otherDirectoryRadioButton;
    private Filler directoryFiller;
    private JLabel directoryLabel;
    private JTextField directoryTextField;
    private JButton chooseDirectoryButton;
    private JRadioButton suffixRadioButton;
    private Filler suffixFiller;
    private JLabel suffixLabel;
    private JTextField suffixTextField;
    private JButton cryptButton;
    
    /**
     * Constructs a new modal {@code FileCrypter}.
     * 
     * @param encrypt Whether to encrypt or decrypt the files
     * @param files The files to encrypt or decrypt
     * @see #init()
     */
    private FileCrypter(boolean encrypt, @Nullable VFSFile... files) {
        super(jEdit.getActiveView(),jEdit.getProperty(encrypt ? "cipher.file-encrypter.title" : "cipher.file-decrypter.title"),true);
        
        this.encrypt = encrypt;
        this.files = files;
        
        cipherOptionPane = new CipherOptionPane(false,false);
        
        newFileHandlingLabel = new JLabel(jEdit.getProperty("cipher.file-crypter.new-file-handling.label"));
        
        Dimension dim = new Dimension(20,0);
        firstColumnFiller = new Filler(dim,dim,dim);
        directoryFiller = new Filler(dim,dim,dim);
        suffixFiller = new Filler(dim,dim,dim);
        
        overwriteRadioButton = new JRadioButton(jEdit.getProperty("cipher.file-crypter.overwrite.label"),false);
        dim = overwriteRadioButton.getMaximumSize();
        dim.width = Integer.MAX_VALUE;
        overwriteRadioButton.setMaximumSize(dim);
        newFileHandlingLabel.setLabelFor(overwriteRadioButton);
        
        otherDirectoryRadioButton = new JRadioButton(jEdit.getProperty("cipher.file-crypter.other-directory.label"),false);
        dim = otherDirectoryRadioButton.getMaximumSize();
        dim.width = Integer.MAX_VALUE;
        otherDirectoryRadioButton.setMaximumSize(dim);
        
        directoryLabel = new JLabel(jEdit.getProperty("cipher.file-crypter.directory.label"));
        
        directoryTextField = new JTextField(jEdit.getProperty("options.cipher.file-crypter.directory"));
        directoryTextField.setEditable(false);
        directoryTextField.setColumns(25);
        directoryTextField.setCaretPosition(0);
        directoryLabel.setLabelFor(directoryTextField);
        
        chooseDirectoryButton = new JButton(jEdit.getProperty("cipher.file-crypter.choose-button.label"));
        dim = chooseDirectoryButton.getMaximumSize();
        dim.height = Integer.MAX_VALUE;
        chooseDirectoryButton.setMaximumSize(dim);
        
        suffixRadioButton = new JRadioButton(jEdit.getProperty("cipher.file-crypter.suffix-radio-button.label"),false);
        dim = suffixRadioButton.getMaximumSize();
        dim.width = Integer.MAX_VALUE;
        suffixRadioButton.setMaximumSize(dim);
        
        suffixLabel = new JLabel(jEdit.getProperty("cipher.file-crypter.suffix.label"));
        
        suffixTextField = new JTextField(jEdit.getProperty("options.cipher.file-crypter.suffix"));
        suffixTextField.setColumns(25);
        suffixTextField.setCaretPosition(0);
        suffixLabel.setLabelFor(suffixTextField);
        
        cryptButton = new JButton(jEdit.getProperty(encrypt ? "cipher.file-crypter.encrypt-button.label" : "cipher.file-crypter.decrypt-button.label"));
        dim = cryptButton.getMaximumSize();
        dim.width = Integer.MAX_VALUE;
        cryptButton.setMaximumSize(dim);
        getRootPane().setDefaultButton(cryptButton);
        
        NewFileHandling newFileHandling = Enum.valueOf(NewFileHandling.class,jEdit.getProperty("options.cipher.file-crypter.new-file-handling","OVERWRITE"));
        switch (newFileHandling) {
            case OVERWRITE:
                overwriteRadioButton.setSelected(true);
                break;
                
            case OTHER_DIRECTORY:
                otherDirectoryRadioButton.setSelected(true);
                break;
                
            case SUFFIX:
                suffixRadioButton.setSelected(true);
                break;
                
            default:
                throw new InternalError("missing case branch for NewFileHandling: " + newFileHandling);
        }
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(overwriteRadioButton);
        buttonGroup.add(otherDirectoryRadioButton);
        buttonGroup.add(suffixRadioButton);
    
        setLayout(new ExtendedGridLayout(5,5,new Insets(5,5,5,5)));
        
        add(cipherOptionPane,new ExtendedGridLayoutConstraints(0,5,1,cipherOptionPane));
        
        add(newFileHandlingLabel,new ExtendedGridLayoutConstraints(1,5,1,newFileHandlingLabel));
        
        add(firstColumnFiller,new ExtendedGridLayoutConstraints(2,1,5,firstColumnFiller));
        
        add(overwriteRadioButton,new ExtendedGridLayoutConstraints(2,4,1,overwriteRadioButton));
        
        add(otherDirectoryRadioButton,new ExtendedGridLayoutConstraints(3,4,1,otherDirectoryRadioButton));
        
        add(directoryFiller,new ExtendedGridLayoutConstraints(4,directoryFiller));
        add(directoryLabel,new ExtendedGridLayoutConstraints(4,directoryLabel));
        add(directoryTextField,new ExtendedGridLayoutConstraints(4,directoryTextField));
        add(chooseDirectoryButton,new ExtendedGridLayoutConstraints(4,chooseDirectoryButton));
        
        add(suffixRadioButton,new ExtendedGridLayoutConstraints(5,4,1,suffixRadioButton));
        
        add(suffixFiller,new ExtendedGridLayoutConstraints(6,suffixFiller));
        add(suffixLabel,new ExtendedGridLayoutConstraints(6,suffixLabel));
        add(suffixTextField,new ExtendedGridLayoutConstraints(6,2,1,suffixTextField));
        
        add(cryptButton,new ExtendedGridLayoutConstraints(7,5,1,cryptButton));
    }
    
    /**
     * Initializes this {@code FileCrypter} and shows it.
     */
    private void init() {
        cipherOptionPane.init();
        ItemListener changeListener = new DetailsEnablerAndDisabler();
        otherDirectoryRadioButton.addItemListener(changeListener);
        suffixRadioButton.addItemListener(changeListener);
        chooseDirectoryButton.addActionListener(new DirectoryChooser());
        cryptButton.addActionListener(new Crypter());
        
        pack();
        GUIUtilities.loadGeometry(this,"cipher.file-crypter");
        setVisible(true);
    }
    
    /**
     * Creates a new modal {@code FileCrypter},
     * initializes it and shows it. If files is null or empty,
     * a filechooser is shown first.
     * 
     * @param encrypt Whether to encrypt or decrypt the files
     * @param files The files to encrypt or decrypt. If {@code files} is 
     *              {@code null} or empty, a dialog is shown
     * @return The newly created {@code FileCrypter}
     */
    @CheckForNull
    public static FileCrypter newInstance(boolean encrypt, @Nullable VFSFile... files) {
        if ((null == files) || (0 == files.length)) {
            VFSFileChooserDialog fileChooser = new VFSFileChooserDialog(jEdit.getActiveView(),null,OPEN_DIALOG,true);
            if (null != fileChooser.getSelectedFiles()) {
                files = fileChooser.getBrowser().getSelectedFiles();
            }
        }
        if ((null == files) || (0 == files.length)) {
            return null;
        }
        FileCrypter instance = new FileCrypter(encrypt,files);
        instance.init();
        return instance;
    }
    
    /**
     * Convenience method for encrypting a set of files.
     * 
     * @param files The files to encrypt. If {@code files} is
     *              {@code null} or empty, a dialog is shown
     */
    public static void encrypt(@Nullable VFSFile... files) {
        newInstance(true,files);
    }
    
    /**
     * Convenience method for decrypting a set of files.
     * 
     * @param files The files to decrypt. If {@code files} is
     *              {@code null} or empty, a dialog is shown
     */
    public static void decrypt(@Nullable VFSFile... files) {
        newInstance(false,files);
    }
    
    /**
     * <p>Called when en-/decrypt button or Enter key is pressed.</p>
     * 
     * <p>Encrypts or decrypts the given files
     * with the given parameters.</p>
     * 
     * @see org.gjt.sp.jedit.gui.EnhancedDialog#ok()
     */
    @Override
    public void ok() {
        try {
            GUIUtilities.saveGeometry(this,"cipher.file-crypter");
            
            if (overwriteRadioButton.isSelected()) {
                newFileHandling = NewFileHandling.OVERWRITE;
            } else if (otherDirectoryRadioButton.isSelected()) {
                newFileHandling = NewFileHandling.OTHER_DIRECTORY;
            } else {
                newFileHandling = NewFileHandling.SUFFIX;
            }
            
            jEdit.setProperty("options.cipher.file-crypter.new-file-handling",newFileHandling.name());
            jEdit.setProperty("options.cipher.file-crypter.directory",directoryTextField.getText());
            jEdit.setProperty("options.cipher.file-crypter.suffix",suffixTextField.getText());
            jEdit.propertiesChanged();
            jEdit.saveSettings();
            
            cipher = CipherPlugin.getCipher(cipherOptionPane.getCipherName());
            if (null == cipher) {
                return;
            }
            additionalInformation = CipherPlugin.getAdditionalInformation(cipher);
            if (null == additionalInformation) {
                return;
            }
            PasswordDialog passwordDialog = PasswordDialog.newInstance();
            password = passwordDialog.getPassword();
            if (null == password) {
                return;
            }
            
            if (!cryptFiles("",files)) {
                GUIUtilities.error(this,encrypt ? "cipher.error.error-while-encrypting-files" : "cipher.error.error-while-decrypting-files",null);
            }
        } finally {
            dispose();
        }
    }
    
    /**
     * Recursively encrypts or decrypts all files given to the
     * method and all files in directories given to the method.
     * 
     * @param relativePath The relative from the initial directory up to the current one,
     *                     for encryption or decryption into another directory
     *                     to be able to rebuild the directory structure
     * @param files The files and directories to encrypt or decrypt
     * @return Whether all encryption or decryption was fine
     */
    @CheckReturnValue(explanation = "If false is returned, something went wrong")
    private boolean cryptFiles(@NonNull String relativePath, @NonNull VFSFile... files) {
        for (VFSFile file : files) {
            VFS vfs = file.getVFS();
            VFS newVfs = null;
            String path = file.getPath();
            Object session = vfs.createVFSSession(path,this);
            Object sessionNew = null;
            Object sessionNewParent = null;
            InputStream in = null;
            ByteArrayInputStream bais = null;
            OutputStream out = null;
            ByteArrayOutputStream baos = null;
            try {
                if (FILE == file.getType()) {
                    in = vfs._createInputStream(session,path,false,this);
                    baos = new ByteArrayOutputStream();
                    if (!IOUtilities.copyStream(null,in,baos,false)) {
                        GUIUtilities.error(this,encrypt ? "cipher.error.error-while-encrypting-file" : "cipher.error.error-while-decrypting-file",new Object[] { path });
                        continue;
                    }
                    baos.flush();
                    byte[] cryptResult;
                    synchronized (cipher) {
                        if (encrypt) {
                            cipher.setRawData(baos.toByteArray());
                        } else {
                            cipher.setEncryptedData(baos.toByteArray());
                        }
                        cipher.setEntropy(password);
                        cipher.setAdditionalInformation(additionalInformation);
                        if (encrypt) {
                            cryptResult = cipher.encryptToByteArray();
                        } else {
                            cryptResult = cipher.decryptToByteArray();
                        }
                    }
                    if (null == cryptResult) {
                        GUIUtilities.error(this,encrypt ? "cipher.error.error-while-encrypting-file" : "cipher.error.error-while-decrypting-file",new Object[] { path });
                        continue;
                    }
                    bais = new ByteArrayInputStream(cryptResult);
                    String newPath;
                    switch (newFileHandling) {
                        case OVERWRITE:
                            newPath = path;
                            newVfs = vfs;
                            break;
                            
                        case OTHER_DIRECTORY:
                            if (0 < relativePath.length()) {
                                newPath = MiscUtilities.constructPath(directoryTextField.getText(),relativePath);
                            } else {
                                newPath = directoryTextField.getText();
                            }
                            newPath = MiscUtilities.constructPath(newPath,file.getName());
                            newVfs = VFSManager.getVFSForPath(newPath);
                            break;
                            
                        case SUFFIX:
                            newPath = path + suffixTextField.getText();
                            newVfs = vfs;
                            break;
                            
                        default:
                            throw new InternalError("missing case branch for NewFileHandling: " + newFileHandling);
                    }
                    String newPathParent = MiscUtilities.getParentOfPath(newPath);
                    sessionNewParent = newVfs.createVFSSession(newPathParent,this);
                    newVfs._mkdir(sessionNewParent,newPathParent,this);
                    sessionNew = newVfs.createVFSSession(newPath,this);
                    out = newVfs._createOutputStream(sessionNew,newPath,this);
                    if (!IOUtilities.copyStream(null,bais,out,false)) {
                        GUIUtilities.error(this,encrypt ? "cipher.error.error-while-encrypting-file" : "cipher.error.error-while-decrypting-file",new Object[] { path });
                        continue;
                    }
                    VFSManager.sendVFSUpdate(newVfs,newPath,true);
                } else {
                    String newRelativePath;
                    if (0 < relativePath.length()) {
                        newRelativePath = MiscUtilities.concatPath(relativePath,file.getName());
                    } else {
                        newRelativePath = file.getName();
                    }
                    if (!cryptFiles(newRelativePath,vfs._listFiles(session,path,this))) {
                        return false;
                    }
                }
            } catch (IOException ioe) {
                Log.log(ERROR,this,ioe);
                new TextAreaDialog(this,encrypt ? "cipher.error.error-while-encrypting-files" : "cipher.error.error-while-decrypting-files",ioe);
                return false;
            } finally {
                try {
                    vfs._endVFSSession(session,this);
                } catch (IOException ioe) {
                    // just ignore it, we are not interested
                }
                try {
                    if (null != newVfs) {
                        newVfs._endVFSSession(sessionNew,this);
                    }
                } catch (IOException ioe) {
                    // just ignore it, we are not interested
                }
                try {
                    if (null != newVfs) {
                        newVfs._endVFSSession(sessionNewParent,this);
                    }
                } catch (IOException ioe) {
                    // just ignore it, we are not interested
                }
                try {
                    if (null != out) {
                        out.flush();
                    }
                } catch (IOException ioe) {
                    // just ignore it, we are not interested
                }
                IOUtilities.closeQuietly(in);
                IOUtilities.closeQuietly(bais);
                IOUtilities.closeQuietly(out);
                IOUtilities.closeQuietly(baos);
            }
        }
        return true;
    }
    
    /**
     * <p>Called when ESC button is pressed or window is closed.</p>
     * 
     * @see org.gjt.sp.jedit.gui.EnhancedDialog#cancel()
     */
    @Override
    public void cancel() {
        GUIUtilities.saveGeometry(this,"cipher.file-crypter");
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
     * Monitors the radio buttons and disables or enables the appropriate
     * details input widgets.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private class DetailsEnablerAndDisabler implements ItemListener {
        /**
         * Constructs a new {@code DetailsEnablerAndDisabler}.
         */
        public DetailsEnablerAndDisabler() {
            if (!otherDirectoryRadioButton.isSelected()) {
                directoryLabel.setEnabled(false);
                directoryTextField.setEnabled(false);
                chooseDirectoryButton.setEnabled(false);
            }
            if (!suffixRadioButton.isSelected()) {
                suffixLabel.setEnabled(false);
                suffixTextField.setEnabled(false);
            }
        }
        
        /**
         * Invoked when an item has been selected or deselected by the user.
         * The code written for this method performs the operations
         * that need to occur when an item is selected (or deselected).
         * 
         * @param ie The item event occured
         */
        public void itemStateChanged(ItemEvent ie) {
            if (SELECTED == ie.getStateChange()) {
                if (otherDirectoryRadioButton == ie.getSource()) {
                    directoryLabel.setEnabled(true);
                    directoryTextField.setEnabled(true);
                    chooseDirectoryButton.setEnabled(true);
                } else {
                    suffixLabel.setEnabled(true);
                    suffixTextField.setEnabled(true);
                }
            } else {
                if (otherDirectoryRadioButton == ie.getSource()) {
                    directoryLabel.setEnabled(false);
                    directoryTextField.setEnabled(false);
                    chooseDirectoryButton.setEnabled(false);
                } else {
                    suffixLabel.setEnabled(false);
                    suffixTextField.setEnabled(false);
                }
            }
        }
    }
    
    /**
     * Encrypts or decrypts the given files
     * with the given parameters.
     * 
     * @author Björn "Vampire" Kautler
     * @since CipherPlugin 0.1
     */
    private class Crypter implements ActionListener {
        /**
         * Invoked when an action occurs.
         * 
         * @param ae The {@code ActionEvent} of the action that occured
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            ok();
        }
    }
}
