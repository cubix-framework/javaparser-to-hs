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

package gruntspud.ui.report;

import gruntspud.CVSFileNode;
import gruntspud.CVSSubstType;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.SizeableLabel;
import gruntspud.ui.UIUtil;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.PipedFileInformation;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class RetrieveFileInfoPane
    extends FileInfoPane
    implements ActionListener {
    //  Private instance variables
    private JLabel file;

    //  Private instance variables
    private JLabel repositoryFile;

    //  Private instance variables
    private JLabel revision;
    private JButton revert;
    private JButton saveAs;
    private Icon icon;
    private String text;
    private PipedFileInformation info;

    /**
     * Constructor
     */
    public RetrieveFileInfoPane(GruntspudContext context) {
        super(context);

        setLayout(new GridBagLayout());

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIUtil.jGridBagAdd(this, new JLabel("File: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        file = new JLabel() {
            public Dimension getPreferredSize() {
                return new Dimension(260,
                                     super.getPreferredSize().height);
            }
        };
        file.setFont(valFont);
        UIUtil.jGridBagAdd(this, file, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Repository file: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        repositoryFile = new SizeableLabel(25);
        repositoryFile.setFont(valFont);
        UIUtil.jGridBagAdd(this, repositoryFile, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Repository revision: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        revision = new SizeableLabel();
        revision.setFont(valFont);
        UIUtil.jGridBagAdd(this, revision, gbc, GridBagConstraints.REMAINDER);

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(3, 3, 16, 3);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        JPanel z = new JPanel(new GridBagLayout());
        z.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 16));
        revert = new JButton("Revert",
                             UIUtil.getCachedIcon(Constants.ICON_TOOL_RETRIEVE));
        revert.setMnemonic('r');
        revert.addActionListener(this);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(z, revert, gbc2, GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(z,
                           new MultilineLabel(
            "Replace the current working revision\n" +
            "with this revision retrieved from the\n" + "repository."),
                           gbc2, GridBagConstraints.REMAINDER);
        gbc2.weightx = 0.0;
        saveAs = new JButton("Save As",
                             UIUtil.getCachedIcon(Constants.ICON_TOOL_SAVE_AS));
        saveAs.setMnemonic('a');
        saveAs.addActionListener(this);
        UIUtil.jGridBagAdd(z, saveAs, gbc2, GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(z,
                           new MultilineLabel(
            "Save the revision retrieved from the\n" +
            "directory to a local file."), gbc2,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 2.0;
        gbc.weighty = 2.0;
        UIUtil.jGridBagAdd(this, z, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        CVSFileNode node = getContext().getViewManager().findNodeForPath(
            getContext()
            .getViewManager()
            .getRootNode(),
            info.getFile(), false);

        if (node == null) {
            JOptionPane.showMessageDialog(this,
                "Could not locate file in current workspace.", "Error",
                JOptionPane.ERROR_MESSAGE);

        }
        if ( (CVSSubstType.CVS_SUBST_TYPE_BINARY == node.getCVSSubstType()) &&
            (JOptionPane.showConfirmDialog(this,
            "Retrieval of binary files is not yet supported", "Error",
            JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) ==
             JOptionPane.NO_OPTION)) {
            return;
        }

        File f = null;

        if (evt.getSource() == revert) {
            if (JOptionPane.showConfirmDialog(this,
                                              "You have chosen to revert " +
                                              info.getFile().getName() + "\n" +
                                              "to revision " +
                                              info.getRepositoryRevision() +
                                              ". This will mean " +
                                              "any changes\n" +
                "you have made since your last commit\n" +
                "will be lost. Are you sure?", "Warning",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) ==
                JOptionPane.NO_OPTION) {
                return;
            }

            f = info.getFile();
        }
        else if (evt.getSource() == saveAs) {
            JFileChooser chooser = new JFileChooser(info.getFile()
                .getParentFile());
            chooser.setSelectedFile(f);
            chooser.setDialogTitle(
                "Choose file to save retrieved revision to ..");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (chooser.getSelectedFile().equals(info.getFile()) &&
                    (JOptionPane.showConfirmDialog(this,
                    "You have chosen to revert " +
                    info.getFile().getName() + "\n" + "to revision " +
                    info.getRepositoryRevision() + ". This will mean " +
                    "any changes\n" +
                    "you have made since your last commit\n" +
                    "will be lost. Are you sure?", "Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)) {
                    return;
                }

                f = chooser.getSelectedFile();
            }
            else {

                return;
            }
        }

        //  Copy the retrieved file to the selected file
        FileOutputStream out = null;
        FileInputStream in = null;

        try {
            Constants.IO_LOG.info("Opening " + f.getAbsolutePath() +
                                  " for writing (" + f.length() + " bytes)");
            out = new FileOutputStream(f);
            Constants.IO_LOG.info("Opening " +
                                  info.getTempFile().getAbsolutePath() +
                                  " for reading (" +
                                  info.getTempFile().length() + " bytes)");
            in = new FileInputStream(info.getTempFile());

            byte[] buf = new byte[65536];
            int z = 0;

            while (true) {
                z = in.read(buf);

                if (z == -1) {
                    break;
                }

                out.write(buf, 0, z);
                out.flush();
            }
        }
        catch (IOException ioe) {
            GruntspudUtil.showErrorMessage(this, "Error", ioe);
        }
        finally {
            GruntspudUtil.closeStream(in);
            GruntspudUtil.closeStream(out);

            CVSFileNode parentNode = getContext().getViewManager()
                .findNodeForPath(getContext()
                                 .getViewManager()
                                 .getRootNode(),
                                 f.getParentFile(), true);

            if (parentNode == null) {
                Constants.UI_LOG.error(
                    "File is not within the current home directory");
            }
            else {
                getContext().getViewManager().reload(parentNode);
            }
        }
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        PipedFileInformation info = (PipedFileInformation) container;

        return info.getRepositoryRevision();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TableCellRenderer getInfoRenderer() {
        return null;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Class getInfoClass() {
        return null;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_RETRIEVE);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_RETRIEVE);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "Retrieve";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        info = (PipedFileInformation) container;
        file.setText(info.getFile().getName());
        repositoryFile.setText(info.getRepositoryFileName());
        repositoryFile.setToolTipText(info.getRepositoryFileName());
        revision.setText(info.getRepositoryRevision());
    }
}
