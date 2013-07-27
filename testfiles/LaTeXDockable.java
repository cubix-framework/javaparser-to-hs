/*:folding=indent:
 * LaTeXDockable.java - LaTeX Tool panel..
 * Copyright (C) 2003 Anthony Roy
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
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
package uk.co.antroy.latextools;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gjt.sp.jedit.jEdit;

import sidekick.SideKickPlugin;

import uk.co.antroy.latextools.macros.ErrorFindingMacros;
import uk.co.antroy.latextools.macros.ProjectMacros;
import uk.co.antroy.latextools.macros.UtilityMacros;
import uk.co.antroy.latextools.parsers.NavigationList;

/** 
 * LaTeX Dockable GUI element that enables the user to 
 * select a filter for the Structure browser (see {@link NavigationList}) 
 * and display some info about the (La)TeX file. 
 */
public class LaTeXDockable extends AbstractToolPanel {

    //~ Instance/static variables .............................................

    private JComboBox nav_list = new JComboBox();
    private static final LaTeXDockable instance = new LaTeXDockable();
    private JComponent infoPanel = new JLabel("");
    private JLabel infoLabel = new JLabel("");
    private JLabel navig;
    private static final String DISPLAY_IMAGE = "View Image";
    private static final String INFO = "Information";
    private static final String DUPLICATES = "Duplicates";
    private static final String ORPHANS = "Orphans";
    private boolean notTex = true;

    //~ Constructors ..........................................................

    private LaTeXDockable() {
        super(null, null, "LaTeX Tools");

        nav_list = new JComboBox(NavigationList.getNavigationData().toArray());
        
        NavigationList nl = NavigationList.getDefaultGroup();
        nav_list.setSelectedItem(nl);
        navig = new JLabel("Structure Browser: show");

        JPanel controls = new JPanel();
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        controls.add(navig);
        controls.add(nav_list);
        addButton(INFO, UtilityMacros.getIcon("info.png"), controls);
        addButton(DISPLAY_IMAGE, UtilityMacros.getIcon("image.png"), controls);
        addButton(DUPLICATES, UtilityMacros.getIcon("duplicate.png"), controls);
        
        addButton(ORPHANS, UtilityMacros.getIcon("orphan.png"), controls);
        
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(controls);
        this.add(infoPanel);
        this.setPreferredSize(new Dimension(500, 300));

        LaTeXDockableListener listener = new LaTeXDockableListener();
        nav_list.addActionListener(listener);
    }

    //~ Methods ...............................................................

    public static LaTeXDockable getInstance() {

        return instance;
    }

    public JComboBox getComboBox() {

        return nav_list;
    }

    public synchronized void setInfoPanel(JComponent panel, String label) {
        this.remove(infoPanel);
        this.remove(infoLabel);
        this.infoPanel = panel;

        Dimension d = new Dimension(300, 300);
        infoPanel.setPreferredSize(d);
        infoLabel = new JLabel("<html><font color='#0000aa'><b>" + label);
        this.add(infoLabel);
        this.add(infoPanel);
        this.sendUpdateEvent("latextools-navigation-dock");
    }

    public JComponent getInfoPanel() {

        return infoPanel;
    }

    public void refresh() {
        view = jEdit.getActiveView();
        buffer = view.getEditPane().getBuffer();

        if (!ProjectMacros.isTeXFile(buffer)) {
            this.setInfoPanel(new JLabel(""), "<html><b>Not a TeX File.");
            notTex = true;
            super.refresh();
        } else {

            if (notTex) {
                ProjectMacros.showInformation(view, buffer);
                notTex = false;
            }
        }
    }

    public void reload() {
    }

    private void addButton(String toolTip, Icon image, Container controls) {

        JButton button = new JButton(new ButtonAction(toolTip, image));
        button.setToolTipText(toolTip);
        controls.add(button);
    }

    //~ Inner classes .........................................................

    private class ButtonAction
        extends AbstractAction {

        //~ Constructors ......................................................

        private ButtonAction(String name, Icon icon) {
            super();
            putValue(Action.LONG_DESCRIPTION, name);
            putValue(Action.ACTION_COMMAND_KEY, name);
            putValue(Action.SMALL_ICON, icon);
        }

        //~ Methods ...........................................................

        public void actionPerformed(ActionEvent e) {

            if (!ProjectMacros.isTeXFile(buffer)) {
                setInfoPanel(new JLabel(""), "<html><b>Not a TeX File.");

                return;
            }

            String command = e.getActionCommand();

            if (command.equals(DISPLAY_IMAGE)) {
                ImageViewer.showInInfoPane(view, buffer);
            } else if (command.equals(DUPLICATES)) {
                ErrorFindingMacros.displayDuplicateLabels(view, buffer);
            } else if (command.equals(ORPHANS)) {
                ErrorFindingMacros.displayOrphanedRefs(view, buffer);
            } else if (command.equals(INFO)) {
                ProjectMacros.showInformation(view, buffer);
            }
        }
    }

    private class LaTeXDockableListener
        implements ActionListener {

        //~ Constructors ......................................................

        private LaTeXDockableListener() {
        }

        //~ Methods ...........................................................

        public void actionPerformed(ActionEvent e) {
        	NavigationList nl = (NavigationList) nav_list.getSelectedItem();
        	NavigationList.setDefaultGroup(nl);
        	SideKickPlugin.parse(jEdit.getActiveView(), true);
        }
    }
}
