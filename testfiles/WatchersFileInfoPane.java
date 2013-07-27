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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.watchers.WatchersInformation;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class WatchersFileInfoPane
    extends FileInfoPane {
    //  Private instance variables
    private JLabel file;
    private JComboBox watchers;
    private DefaultComboBoxModel model;
    private JLabel watchingCommit;
    private JLabel watchingEdit;
    private JLabel watchingUnedit;
    private JLabel tempWatchingCommit;
    private JLabel tempWatchingEdit;
    private JLabel tempWatchingUnedit;
    private JLabel watches;
    private int maxChars;

    /**
     * Constructor
     */
    public WatchersFileInfoPane(GruntspudContext context) {
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

        watchers = new JComboBox(model = new DefaultComboBoxModel());
        watchers.setRenderer(new WatchInfoListCellRenderer());
        watchers.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                showInfoForSelectedWatch();
            }
        });

        JPanel s = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(3, 3, 3, 3);
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Watching Commit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, watchingCommit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        watchingCommit.setFont(valFont);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Watching Edit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, watchingEdit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        watchingEdit.setFont(valFont);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Watching Unedit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, watchingUnedit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        watchingUnedit.setFont(valFont);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Temporarily watching Commit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, tempWatchingCommit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        tempWatchingCommit.setFont(valFont);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Temporarily Watching Edit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, tempWatchingEdit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        tempWatchingEdit.setFont(valFont);
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(s, new JLabel("Temporarily Watching Unedit:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, tempWatchingUnedit = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        tempWatchingUnedit.setFont(valFont);
        gbc2.weightx = 0.0;
        gbc2.weighty = 1.0;
        UIUtil.jGridBagAdd(s, new JLabel("Watches:"), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(s, watches = new JLabel(), gbc2,
                           GridBagConstraints.REMAINDER);
        watches.setFont(valFont);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Watches"));
        p.add(watchers, BorderLayout.NORTH);
        p.add(s, BorderLayout.CENTER);

        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, p, gbc, GridBagConstraints.REMAINDER);

        showInfoForSelectedWatch();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        return "";
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
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_WATCHERS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_WATCHERS);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "Watchers";
    }

    private void showInfoForSelectedWatch() {
        WatchersInformation.Watcher watcher = (WatchersInformation.Watcher)
            watchers.getSelectedItem();

        if (watcher != null) {
            watchingCommit.setText(watcher.isWatchingCommit() ? "Yes" : "No");
            watchingEdit.setText(watcher.isWatchingEdit() ? "Yes" : "No");
            watchingUnedit.setText(watcher.isWatchingUnedit() ? "Yes" : "No");
            tempWatchingCommit.setText(watcher.isTempWatchingCommit() ? "Yes"
                                       : "No");
            tempWatchingEdit.setText(watcher.isTempWatchingEdit() ? "Yes" :
                                     "No");
            tempWatchingUnedit.setText(watcher.isTempWatchingUnedit() ? "Yes"
                                       : "No");
            watches.setText(watcher.getWatches());
        }
        else {
            watchingCommit.setText("N/A");
            watchingEdit.setText("N/A");
            watchingUnedit.setText("N/A");
            tempWatchingCommit.setText("N/A");
            tempWatchingEdit.setText("N/A");
            tempWatchingUnedit.setText("N/A");
            watches.setText("N/A");
        }

        setAvailableActions();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        WatchersInformation info = (WatchersInformation) container;
        model.removeAllElements();

        if (info.getWatchersIterator() != null) {
            for (Iterator i = info.getWatchersIterator(); i.hasNext(); ) {
                WatchersInformation.Watcher w = (WatchersInformation.Watcher) i.
                    next();
                model.addElement(w);
            }
        }

        showInfoForSelectedWatch();
    }

    private void setAvailableActions() {
        watchers.setEnabled(model.getSize() > 0);
    }

    class WatchInfoListCellRenderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);

            WatchersInformation.Watcher w = (WatchersInformation.Watcher) value;

            if (w != null) {
                setText(w.getUserName());
            }
            else {
                setText("");

            }
            return this;
        }
    }
}
