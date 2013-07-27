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

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.UIUtil;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.status.StatusInformation;
import org.netbeans.lib.cvsclient.file.FileStatus;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class StatusFileInfoPane
    extends FileInfoPane {
    //  Private instance variables
    private JLabel file;

    //  Private instance variables
    private JLabel status;

    //  Private instance variables
    private JLabel workingRevision;

    //  Private instance variables
    private JLabel repositoryRevision;

    //  Private instance variables
    private JLabel stickyTag;

    //  Private instance variables
    private JLabel stickyDate;

    //  Private instance variables
    private JLabel stickyOptions;

    //  Private instance variables
    private JLabel symNameForTag;

    //  Private instance variables
    private JLabel directory;
    private JList tags;

    /**
     * Constructor
     */
    public StatusFileInfoPane(GruntspudContext context) {
        super(context);
        setLayout(new GridBagLayout());

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;

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
        UIUtil.jGridBagAdd(this, new JLabel("Directory: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directory = new JLabel();
        directory.setFont(valFont);
        UIUtil.jGridBagAdd(this, directory, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Status: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        status = new JLabel();
        status.setFont(valFont);
        UIUtil.jGridBagAdd(this, status, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Working revision: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        workingRevision = new JLabel();
        workingRevision.setFont(valFont);
        UIUtil.jGridBagAdd(this, workingRevision, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Repository revision: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        repositoryRevision = new JLabel();
        repositoryRevision.setFont(valFont);
        UIUtil.jGridBagAdd(this, repositoryRevision, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Sticky tag: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        stickyTag = new JLabel();
        stickyTag.setFont(valFont);
        UIUtil.jGridBagAdd(this, stickyTag, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Sticky date: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        stickyDate = new JLabel();
        stickyDate.setFont(valFont);
        UIUtil.jGridBagAdd(this, stickyDate, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Sticky options: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        stickyOptions = new JLabel();
        stickyOptions.setFont(valFont);
        UIUtil.jGridBagAdd(this, stickyOptions, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Existing tags: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        tags = new JList(new DefaultListModel());
        tags.setFont(valFont);

        JScrollPane scroller = new JScrollPane(tags);
        UIUtil.jGridBagAdd(this, scroller, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        StatusInformation info = (StatusInformation) container;

        FileStatus st = info.getStatus();

        return st == null ? "<Unknown>" : info.getStatus().toString();
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
        return String.class;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_STATUS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_STATUS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "File status report";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        StatusInformation info = (StatusInformation) container;
        file.setText(info.getFile().getName());
        file.setToolTipText("<html><b><i>Local</i></b>: " +
                            container.getFile().getAbsolutePath() +
                            "<br><b><i>Remote:</i></b> " +
                            info.getRepositoryFileName() +
                            "</html>");
        directory.setText(info.getFile().getParentFile().getName());
        directory.setToolTipText(info.getFile().getParentFile().getAbsolutePath());
        status.setText(info.getStatusString());
        workingRevision.setText(info.getWorkingRevision());
        repositoryRevision.setText(info.getRepositoryRevision());
        stickyTag.setText(info.getStickyTag());
        stickyDate.setText(info.getStickyDate());
        stickyOptions.setText(info.getStickyOptions());

        java.util.List l = info.getAllExistingTags();
        ( (DefaultListModel) tags.getModel()).clear();

        for (int i = 0; i < l.size(); i++) {
            ( (DefaultListModel) tags.getModel()).addElement(l.get(i));
        }

        //        File: README.txt       	Status: Up-to-date
        //   Working revision:	1.6
        //   Repository revision:	1.6	/cvsroot/gruntspud/gruntspud/README.txt,v
        //   Sticky Tag:		(none)
        //   Sticky Date:		(none)
        //   Sticky Options:	(none)
    }
}
