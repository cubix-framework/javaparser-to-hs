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
import gruntspud.GruntspudUtil;
import gruntspud.ui.UIUtil;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.editors.EditorsFileInfoContainer;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class EditorsFileInfoPane
    extends FileInfoPane {
    //  Private instance variables
    private JLabel file;

    //  Private instance variables
    private JLabel user;

    //  Private instance variables
    private JLabel date;

    //  Private instance variables
    private JLabel client;

    //  Private instance variables
    private JLabel directory;
    private Icon icon;
    private String text;

    /**
     * Constructor
     */
    public EditorsFileInfoPane(GruntspudContext context) {
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
        UIUtil.jGridBagAdd(this, new JLabel("Directory: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directory = new JLabel();
        directory.setFont(valFont);
        UIUtil.jGridBagAdd(this, directory, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("User: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        user = new JLabel();
        user.setFont(valFont);
        UIUtil.jGridBagAdd(this, user, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Date: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        date = new JLabel();
        date.setFont(valFont);
        UIUtil.jGridBagAdd(this, date, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, new JLabel("Client: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        client = new JLabel();
        client.setFont(valFont);
        UIUtil.jGridBagAdd(this, client, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        EditorsFileInfoContainer info = (EditorsFileInfoContainer) container;

        return info.getUser();
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
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_EDITORS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_EDITORS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "Editors";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        EditorsFileInfoContainer info = (EditorsFileInfoContainer) container;
        file.setText(info.getFile().getName());
        file.setToolTipText(container.getFile().getAbsolutePath());
        directory.setText(info.getFile().getParentFile().getAbsolutePath());
        user.setText(info.getUser());
        client.setText(info.getClient());
        date.setText(GruntspudUtil.formatDate(info.getDate(), getContext()));
    }
}
