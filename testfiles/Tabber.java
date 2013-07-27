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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class Tabber
    extends JTabbedPane {
    /**
     *  Constructor for the Tabber object
     */
    public Tabber() {
        this(TOP);
    }

    /**
     *  Constructor for the Tabber object
     *
     *@param  tabPlacement  Description of the Parameter
     */
    public Tabber(int tabPlacement) {
        super(tabPlacement);
        addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (getSelectedIndex() != -1) {
                    getTabAt(getSelectedIndex()).tabSelected();
                }
            }
        });
    }

    /**
     *  Gets the tabAt attribute of the Tabber object
     *
     *@param  i  Description of the Parameter
     *@return    The tabAt value
     */
    public Tab getTabAt(int i) {
        return ( (TabPanel) getComponentAt(i)).getTab();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateTabs() {
        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = ( (TabPanel) getComponentAt(i)).getTab();

            if (!tab.validateTab()) {
                setSelectedIndex(i);

                return false;
            }
        }

        return true;
    }

    /**
     *  Description of the Method
     */
    public void applyTabs() {
        for (int i = 0; i < getTabCount(); i++) {
            Tab tab = ( (TabPanel) getComponentAt(i)).getTab();
            tab.applyTab();
        }
    }

    /**
     *  Adds a feature to the Tab attribute of the Tabber object
     *
     *@param  tab  The feature to be added to the Tab attribute
     */
    public void addTab(Tab tab) {
        addTab(tab.getTabTitle(), new TabPanel(tab));
    }

    class TabPanel
        extends JPanel {
        private Tab tab;

        /**
         *  Constructor for the TabPanel object
         *
         *@param  tab  Description of the Parameter
         */
        TabPanel(Tab tab) {
            super(new BorderLayout());
            this.tab = tab;
            setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            setOpaque(false);

            JPanel c = new JPanel(new BorderLayout());
            c.setOpaque(false);
            c.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
            c.add(new JLabel(tab.getTabLargeIcon()), BorderLayout.WEST);

            MultilineLabel box = new MultilineLabel(tab.getTabToolTipText() == null ?
                "" : tab.getTabToolTipText());
            box.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
            box.setOpaque(false);
            c.add(box, BorderLayout.CENTER);

            JPanel z = new JPanel(new BorderLayout());
            z.setOpaque(false);
            z.add(c, BorderLayout.CENTER);

            JSeparator s = new JSeparator(JSeparator.HORIZONTAL);
            s.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            z.add(s, BorderLayout.SOUTH);
            add(z, BorderLayout.NORTH);
            tab.getTabComponent().setOpaque(false);
            add(tab.getTabComponent(), BorderLayout.CENTER);
        }

        public Tab getTab() {
            return tab;
        }
    }
}
