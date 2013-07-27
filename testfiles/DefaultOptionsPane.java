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

import gruntspud.GruntspudContext;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Icon;

import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Command;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class DefaultOptionsPane
    extends Tabber {
    private DefaultGeneralTab generalTab;
    private GlobalOptionsTab globalOptionsTab;
    private GruntspudContext context;
    private Icon icon, largeIcon;
    private String text;
    private GruntspudCheckBox doNotRecurse;
    private BasicCommand command;
    private String doNotRecursePropertyName;

    /**
     *  Constructor for the EditOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public DefaultOptionsPane(GruntspudContext context, Icon icon, String text,
                              String doNotRecursePropertyName,
                              BasicCommand command, Icon largeIcon) {
        super();

        this.doNotRecursePropertyName = doNotRecursePropertyName;
        this.context = context;
        this.icon = icon;
        this.largeIcon = largeIcon;
        this.text = text;
        this.command = command;

        generalTab = new DefaultGeneralTab();
        globalOptionsTab = new GlobalOptionsTab();
        globalOptionsTab.init(context);

        addTab(generalTab);
        addTab(globalOptionsTab);
    }

    /**
     *  Gets the commandsForSettings attribute of the CommitInfoPane object
     *
     *@return    The commandsForSettings value
     */
    public Command[] getCommandsForSettings() {
        command.setRecursive(false);

        return new Command[] {
            command};
    }

    class DefaultGeneralTab
        extends AbstractTab {
        /**
         *  Constructor for the CommitGeneralTab object
         */
        DefaultGeneralTab() {
            super("General", icon);
            setTabToolTipText(text);
            setLayout(new GridBagLayout());
            setTabMnemonic('g');
            setTabLargeIcon(largeIcon);

            GridBagConstraints gbc = new GridBagConstraints();
            Insets i1 = new Insets(3, 3, 3, 3);
            gbc.insets = i1;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.weightx = 2.0;
            gbc.weighty = 1.0;
            doNotRecurse = new GruntspudCheckBox("Do not recurse");
            doNotRecurse.setSelected(context.getHost().getBooleanProperty(
                doNotRecursePropertyName,
                false));
            UIUtil.jGridBagAdd(this, doNotRecurse, gbc,
                               GridBagConstraints.REMAINDER);
        }

        public boolean validateTab() {
            return true;
        }

        public void applyTab() {
            context.getHost().setBooleanProperty(doNotRecursePropertyName,
                                                 doNotRecurse.isSelected());
        }

        public void tabSelected() {
        }
    }
}
