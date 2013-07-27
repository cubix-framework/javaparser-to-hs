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
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.UIUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class StickyOptionsTab
    extends AbstractOptionsTab
    implements ActionListener {
    private GruntspudCheckBox useHeadIfNotFound;
    private GruntspudCheckBox useByDate;
    private GruntspudCheckBox useByRevision;
    private StringListComboBox byDate;
    private StringListComboBox byRevision;
    private boolean rememberSettings;

    /**
     *  Constructor for the StickyOptionsTab object
     */
    public StickyOptionsTab(Icon icon, Icon largeIcon, boolean rememberSettings) {
        this(icon, largeIcon, rememberSettings, null, null);
    }

    /**
     *  Constructor for the StickyOptionsTab object
     */
    public StickyOptionsTab(Icon icon, Icon largeIcon,
                            boolean rememberSettings, String name, String text) {
        super( (name == null) ? "Sticky" : name, icon);
        setTabToolTipText( (text == null) ? "Sticky update options" : text);
        setTabLargeIcon(largeIcon);
        this.rememberSettings = rememberSettings;
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);
        setLayout(new GridBagLayout());
        setTabMnemonic('g');

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIUtil.jGridBagAdd(this, useByDate = new GruntspudCheckBox(), gbc, 1);
        useByDate.setSelected(getContext().getHost().getBooleanProperty(
            Constants.STICKY_BY_DATE_ENABLED,
            false) && rememberSettings);
        useByDate.addActionListener(this);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("By date"), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           byDate = new StringListComboBox(getContext(),
            getContext().getHost().getProperty(Constants.STICKY_BY_DATE,
                                               ""), true), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, useByRevision = new GruntspudCheckBox(), gbc, 1);
        useByRevision.setSelected(getContext().getHost().getBooleanProperty(
            Constants.STICKY_BY_REVISION_ENABLED,
            false) && rememberSettings);
        useByRevision.addActionListener(this);
        UIUtil.jGridBagAdd(this, new JLabel("By revision/tag/branch"), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           byRevision = new StringListComboBox(getContext(),
            getContext().getHost().getProperty(Constants.STICKY_BY_REVISION,
                                               ""), true), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this,
                           useHeadIfNotFound = new GruntspudCheckBox(
            "Use HEAD if not found"), gbc,
                           GridBagConstraints.REMAINDER);
        useHeadIfNotFound.setMnemonic('h');
        useHeadIfNotFound.setSelected(getContext().getHost().getBooleanProperty(
            Constants.STICKY_USE_HEAD_IF_NOT_FOUND,
            false));

        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        setAvailableActions();
    }

    private void setAvailableActions() {
        byDate.setEnabled(useByDate.isSelected());
        byRevision.setEnabled(useByRevision.isSelected());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean validateTab() {
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    public void applyTab() {
        getContext().getHost().setProperty(Constants.STICKY_BY_DATE,
                                           byDate.getStringListPropertyString());
        getContext().getHost().setBooleanProperty(Constants.
                                                  STICKY_BY_DATE_ENABLED,
                                                  useByDate.isSelected());
        getContext().getHost().setProperty(Constants.STICKY_BY_REVISION,
                                           byRevision.
                                           getStringListPropertyString());
        getContext().getHost().setBooleanProperty(Constants.
                                                  STICKY_BY_REVISION_ENABLED,
                                                  useByRevision.isSelected());
        getContext().getHost().setBooleanProperty(Constants.
                                                  STICKY_USE_HEAD_IF_NOT_FOUND,
                                                  useHeadIfNotFound.isSelected());
    }

    /**
     * DOCUMENT ME!
     */
    public void tabSelected() {
    }

    /**
     * Return if 'Use Head if not found' is selected
     *
     * @return use head if not found
     */
    public boolean isUseHeadIfNotFound() {
        return useHeadIfNotFound.isSelected();
    }

    /**
     * Return if 'by date' should be used
     *
     * @return use by date
     */
    public boolean isByDate() {
        return useByDate.isSelected();
    }

    /**
     * Return if 'by revision' should be used
     *
     * @return use by revision
     */
    public boolean isByRevision() {
        return useByRevision.isSelected();
    }

    /**
     * Return the selected date to use
     *
     * @return selected revision
     */
    public String getSelectedDate() {
        return isByDate() ? (String) byDate.getSelectedItem() : null;
    }

    /**
     * Return the revision date to use
     *
     * @return selected revision
     */
    public String getSelectedRevision() {
        return isByRevision() ? (String) byRevision.getSelectedItem() : null;
    }
}
