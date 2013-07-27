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
import gruntspud.GruntspudUtil;
import gruntspud.file.FileTypeMapping;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.FileNameTextField;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *  Mini text editor
 *
 *@author     Brett Smiht
 *@created    26 May 2002
 */
public class FileTypeMappingEditor
    extends AbstractTab
    implements ActionListener {
    //  Private instance variables
    private GruntspudContext context;
    private FileTypeMapping mapping;
    private JRadioButton openUsingHost;
    private JRadioButton openUsingOS;
    private JRadioButton openUsingApplication;
    private JRadioButton disable;
    private JRadioButton openUsingHTMLViewer;
    private FileNameTextField application;
    private XTextField description;
    private XTextField patterns;
    private JButton browse;
    private boolean isNew;
    private Vector types;

    /**
     *  Constructor
     *
     *@param  host                        Description of the Parameter
     */
    public FileTypeMappingEditor(GruntspudContext context,
                                 FileTypeMapping mapping) {
        this(context, mapping, true);
    }

    /**
     * Creates a new FileTypeMappingEditor object.
     *
     * @param context DOCUMENT ME!
     * @param mapping DOCUMENT ME!
     * @param isNew DOCUMENT ME!
     */
    public FileTypeMappingEditor(GruntspudContext context,
                                 FileTypeMapping mapping, boolean isNew) {
        super("General",
              UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_PREFERENCES));

        this.context = context;
        this.mapping = mapping;

        setTabToolTipText("File type options.");
        setLayout(new BorderLayout());
        setTabMnemonic('c');

        //
        JPanel o = new JPanel(new GridBagLayout());
        o.setBorder(BorderFactory.createTitledBorder("Open with .."));

        Insets i1 = new Insets(1, 1, 2, 2);
        Insets i2 = new Insets(1, 24, 2, 2);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = i1;

        ButtonGroup bg = new ButtonGroup();

        gbc.insets = i1;
        gbc.weightx = 2.0;
        disable = new JRadioButton("Disable");
        UIUtil.jGridBagAdd(o, disable, gbc, GridBagConstraints.REMAINDER);
        disable.setMnemonic('d');
        disable.addActionListener(this);
        bg.add(disable);

        openUsingHost = new JRadioButton("Open using " +
                                         context.getHost().getName());
        UIUtil.jGridBagAdd(o, openUsingHost, gbc, GridBagConstraints.REMAINDER);
        openUsingHost.setMnemonic('o');
        openUsingHost.addActionListener(this);
        bg.add(openUsingHost);

        if (context.getHost().getHTMLViewerName() != null) {
            openUsingHTMLViewer = new JRadioButton("Open using " +
                context.getHost().getHTMLViewerName());
            UIUtil.jGridBagAdd(o, openUsingHTMLViewer, gbc,
                               GridBagConstraints.REMAINDER);
            openUsingHTMLViewer.setMnemonic('o');
            openUsingHTMLViewer.addActionListener(this);
            bg.add(openUsingHTMLViewer);
        }

        openUsingOS = new JRadioButton("Open using O/S");
        UIUtil.jGridBagAdd(o, openUsingOS, gbc, GridBagConstraints.REMAINDER);
        openUsingOS.setMnemonic('o');
        openUsingOS.addActionListener(this);
        bg.add(openUsingOS);

        openUsingApplication = new JRadioButton("Use this application");
        openUsingApplication.setMnemonic('a');
        openUsingApplication.addActionListener(this);
        UIUtil.jGridBagAdd(o, openUsingApplication, gbc, 1);
        UIUtil.jGridBagAdd(o, new JLabel(), gbc, GridBagConstraints.REMAINDER);
        gbc.insets = i2;
        bg.add(openUsingApplication);
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(o, application = new FileNameTextField(null, "", 25, true, true),
                           gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(o, browse = new JButton("Browse"), gbc,
                           GridBagConstraints.REMAINDER);
        browse.addActionListener(this);

        //
        JPanel d = new JPanel(new GridBagLayout());
        d.setBorder(BorderFactory.createTitledBorder("Details"));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.insets = i1;

        gbc2.insets = i1;
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(d, new JLabel("Description:"), gbc2,
                           GridBagConstraints.REMAINDER);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(d, description = new XTextField(10), gbc2,
                           GridBagConstraints.REMAINDER);

        gbc2.insets = i1;
        gbc2.weightx = 0.0;
        gbc2.weighty = 1.0;
        UIUtil.jGridBagAdd(d, new JLabel("Patterns:"), gbc2,
                           GridBagConstraints.REMAINDER);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(d, patterns = new XTextField(10), gbc2,
                           GridBagConstraints.REMAINDER);

        //
        JPanel r = new JPanel(new BorderLayout());
        r.add(d, BorderLayout.NORTH);
        r.add(o, BorderLayout.CENTER);

        add(r, BorderLayout.CENTER);

        setFileTypeMapping(mapping, isNew);
    }

    /**
     * DOCUMENT ME!
     */
    public void tabSelected() {
    }

    /**
     * DOCUMENT ME!
     */
    public void applyTab() {
        if (mapping != null) {
            mapping.setDescription(description.getText());
            mapping.setApplication(application.getText());

            if (disable.isSelected()) {
                mapping.setAction(FileTypeMapping.DISABLE);
            }
            else if (openUsingApplication.isSelected()) {
                mapping.setAction(FileTypeMapping.OPEN_USING_APPLICATION);
            }
            else if (openUsingHost.isSelected()) {
                mapping.setAction(FileTypeMapping.OPEN_USING_HOST);
            }
            else if (openUsingHTMLViewer.isSelected()) {
                mapping.setAction(FileTypeMapping.OPEN_USING_HTML_VIEWER);
            }
            else if (openUsingOS.isSelected()) {
                mapping.setAction(FileTypeMapping.OPEN_USING_OS);

            }
            mapping.setPatternsFromString(patterns.getText());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean validateTab() {
        try {
            if (description.getText().length() == 0) {
                throw new Exception("You must supply a description.");
            }

            if (patterns.getText().length() == 0) {
                throw new Exception("You must supply at least one or " +
                    "more comma separated file patterns, e.g. *.java,*.c");
            }

            if (openUsingApplication.isSelected() &&
                (application.getText().length() == 0)) {
                throw new Exception("You must supply the command that runs " +
                                    "the required application.");
            }
        }
        catch (Exception e) {
            GruntspudUtil.showErrorMessage(this, "Error", e);
            JOptionPane.showMessageDialog(this, e.getMessage(), "File type",
                                          JOptionPane.ERROR_MESSAGE);

            return false;
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param mapping DOCUMENT ME!
     */
    public void setFileTypeMapping(FileTypeMapping mapping) {
        setFileTypeMapping(mapping, true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param mapping DOCUMENT ME!
     * @param isNew DOCUMENT ME!
     */
    public void setFileTypeMapping(FileTypeMapping mapping, boolean isNew) {
        this.isNew = isNew;

        switch (mapping.getAction()) {
            case FileTypeMapping.DISABLE:
                disable.setSelected(true);

                break;
            case FileTypeMapping.OPEN_USING_HOST:
                openUsingHost.setSelected(true);

                break;
            case FileTypeMapping.OPEN_USING_OS:
                openUsingOS.setSelected(true);

                break;
            case FileTypeMapping.OPEN_USING_APPLICATION:
                openUsingApplication.setSelected(true);

                break;
            case FileTypeMapping.OPEN_USING_HTML_VIEWER:
                openUsingHTMLViewer.setSelected(true);

                break;
        }

        application.setText( (mapping.getApplication() == null) ? ""
                            : mapping.getApplication());
        description.setText( (mapping.getDescription() == null) ? ""
                            : mapping.getDescription());
        patterns.setText(mapping.getPatternsAsString());
        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browse) {
            File f = new File(application.getText());
            JFileChooser chooser = new JFileChooser(f);
            chooser.setSelectedFile(f);
            chooser.setDialogTitle("Choose application ..");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String s = chooser.getSelectedFile().getAbsolutePath();
                application.setText("\"" + s + "\"");
            }
        }

        setAvailableActions();
    }

    private void setAvailableActions() {
        application.setEnabled(openUsingApplication.isSelected());
        browse.setEnabled(openUsingApplication.isSelected());
    }
}
