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

import gruntspud.CVSFileNode;
import gruntspud.CVSFileTypeUtil;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.file.GruntspudFileMode;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Description of the Class
 *
 * @author magicthize
 */
public class DirectoryDetailsPane
    extends JPanel
    implements ActionListener {
    private static DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG,
            DateFormat.LONG);
    }

    private JLabel directory;
    private JLabel type;
    private JLabel files;
    private JLabel directories;
    private JLabel lastModified;
    private JLabel access;
    private JLabel size;
    private JCheckBox recurse;
    private CVSFileNode node;
    private GruntspudContext context;
    private long totalFiles;
    private long totalDirectories;
    private long totalSize;
    private Thread recurseThread;
    private boolean recurseRunning;

    /**
     * Constructor
     *
     * @param node DOCUMENT ME!
     * @param context DOCUMENT ME!
     */
    public DirectoryDetailsPane(CVSFileNode node, GruntspudContext context) {
        this(context);
        setNode(node);
    }

    /**
     * Constructor
     *
     * @param context DOCUMENT ME!
     */
    public DirectoryDetailsPane(GruntspudContext context) {
        super(new BorderLayout());
        this.context = context;

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.BOTH;

        UIUtil.jGridBagAdd(p, new JLabel("Directory: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directory = new JLabel();
        directory.setIcon(UIUtil.EMPTY_SMALL_ICON);
        directory.setFont(valFont);
        UIUtil.jGridBagAdd(p, directory, gbc, GridBagConstraints.REMAINDER);

        UIUtil.jGridBagAdd(p, new JLabel("Type: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        type = new JLabel();
        type.setFont(valFont);
        UIUtil.jGridBagAdd(p, type, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(p, new JLabel("Files: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        files = new JLabel();
        files.setIcon(UIUtil.EMPTY_SMALL_ICON);
        files.setFont(valFont);
        UIUtil.jGridBagAdd(p, files, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(p, new JLabel("Directories: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directories = new JLabel();
        directories.setIcon(UIUtil.EMPTY_SMALL_ICON);
        directories.setFont(valFont);
        UIUtil.jGridBagAdd(p, directories, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(p, new JLabel("Size: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        size = new JLabel();
        size.setIcon(UIUtil.EMPTY_SMALL_ICON);
        size.setFont(valFont);
        UIUtil.jGridBagAdd(p, size, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(p, new JLabel("Access: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        access = new JLabel();
        access.setIcon(UIUtil.EMPTY_SMALL_ICON);
        access.setFont(valFont);
        UIUtil.jGridBagAdd(p, access, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(p, new JLabel("Last modified: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        lastModified = new JLabel();
        lastModified.setIcon(UIUtil.EMPTY_SMALL_ICON);
        lastModified.setFont(valFont);
        UIUtil.jGridBagAdd(p, lastModified, gbc, GridBagConstraints.REMAINDER);

        add(p, BorderLayout.CENTER);

        JPanel r = new JPanel(new BorderLayout());
        r.setBorder(BorderFactory.createTitledBorder("Options"));
        recurse = new JCheckBox("Recurse");
        recurse.setSelected(context.getHost().getBooleanProperty(Constants.
            FILE_DETAILS_RECURSE_DIRECTORIES,
            false));
        recurse.setMnemonic('r');
        recurse.addActionListener(this);
        r.add(recurse, BorderLayout.CENTER);

        add(r, BorderLayout.SOUTH);
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUp() {
        context.getHost().setBooleanProperty(Constants.
                                             FILE_DETAILS_RECURSE_DIRECTORIES,
                                             recurse.isSelected());
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        setSizes();
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public void setNode(CVSFileNode node) {
        this.node = node;

        //  Directory name
        directory.setText(node.getName());
        directory.setToolTipText(node.getFile().getAbsolutePath());

        //  Directory type
        String typeText = CVSFileTypeUtil.getFileSystemTypeDescriptionName(node.
            getFile());
        type.setText( ( (typeText == null) || typeText.equals("")) ? "File"
                     : typeText);
        type.setIcon(node.getIcon(false));

        //  Access
        String accessText = null;
        Color accessColor = null;
        String modeText = null;

        if (!node.getFile().exists()) {
            accessText = "Doesn't exist";
            accessColor = Color.red;
        }
        else {
        	GruntspudFileMode mode = GruntspudFileMode.getFileModeForFile(node.getFile());
            modeText = mode.toString();

            if (!node.getFile().canRead() && !node.getFile().canWrite()) {
                accessText = "No read / No write";
                accessColor = Color.red;
            }
            else if (!node.getFile().canWrite() && node.getFile().canRead()) {
                accessText = "Read only";
                accessColor = Color.red;
            }
            else if (node.getFile().canWrite() && !node.getFile().canRead()) {
                accessText = "Write only";
                accessColor = Color.red;
            }
            else if (node.getFile().canWrite() && node.getFile().canRead()) {
                accessText = "Read / Write";
            }
        }

        access.setText(accessText +
                       ( (modeText == null) ? "" : (" (" + modeText + ")")));
        access.setForeground( (accessColor != null) ? accessColor
                             : UIManager.getColor(
            "Label.foreground"));

        //  Last modified
        if (node.getFile().exists()) {
            lastModified.setText(DATE_FORMAT.format(
                new Date(node.getFile().lastModified())));
        }
        else {
            lastModified.setText("N/A");

            //
        }
        setSizes();
    }

    private void setSizes() {
        totalFiles = 0;
        totalDirectories = 0;
        totalSize = 0;

        if ( (recurseThread != null) && recurseThread.isAlive()) {
            recurseRunning = false;

            try {
                recurseThread.join();
            }
            catch (InterruptedException ie) {
            }
        }

        if (node != null) {
            Runnable r = new Runnable() {
                public void run() {
                    recurseRunning = true;
                    setNodeSizes(node, recurse.isSelected());
                }
            };

            recurseThread = new Thread(r);
            recurseThread.start();
        }
    }

    private void setNodeSizes(CVSFileNode node, boolean recurse) {
        if(!node.isChildListLoaded()) {
            node.loadChildren();
            node.filterAndSortChildren();
        }
        for (int i = 0; (i < node.getChildCount()) && recurseRunning; i++) {
            CVSFileNode n = (CVSFileNode) node.getChildAt(i);

            if (n.isLeaf()) {
                totalFiles++;
                totalSize += n.getFile().length();
            }
            else {
                if (recurse) {
                    setNodeSizes(n, true);

                }
                totalDirectories++;
            }
        }

        files.setText(String.valueOf(totalFiles));
        directories.setText(String.valueOf(totalDirectories));
        size.setText(GruntspudUtil.formatFileSize(totalSize));
    }
}
