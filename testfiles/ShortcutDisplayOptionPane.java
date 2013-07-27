/*
 *  ShortcutDisplayOptionPane.java - Shortcuts options panel
 *  Copyright (C) 2005 Jeffrey Hoyt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package shortcutdisplay;

import java.util.Hashtable;

import org.gjt.sp.jedit.*;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.Box;
import javax.swing.JLabel;


/**
 *  Description of the Class
 *
 *@author     jchoyt
 *@created    November 20, 2005
 */
public class ShortcutDisplayOptionPane extends AbstractOptionPane {
    private JCheckBox showPopup;
    private JCheckBox sortByAction;
    private JSlider popupDelay;
    private int delay = 500;

    /**
     *  Constructor for the ShortcutDisplayOptionPane object
     */
    public ShortcutDisplayOptionPane() {
        super("shortcut-display-menu");
    }

    // protected members
    /**
     *  Description of the Method
     */
    protected void _init() {
        setLayout(new BorderLayout(12, 12));

        showPopup = new JCheckBox(jEdit.getProperty(
                    "options.shortcuts.popup.label"),
                jEdit.getBooleanProperty("options.shortcuts.displaypopup", true));
        sortByAction = new JCheckBox(jEdit.getProperty(
                    "options.shortcuts.popup.sort.label"),
                jEdit.getBooleanProperty("options.shortcuts.sortbyaction", false));
        delay = jEdit.getIntegerProperty("options.shortcut-display.popup.delay",
                500);
        popupDelay = new JSlider(200, 3000, delay);
        popupDelay.setPaintLabels(true);
        popupDelay.setMajorTickSpacing(500);
        popupDelay.setPaintTicks(true);
		Hashtable labelTable = new Hashtable();
		for(int i = 500; i <= 3000; i += 500)
		{
			labelTable.put(new Integer(i),new JLabel(
				String.valueOf((double)i / 1000.0)));
		}
        popupDelay.setLabelTable(labelTable);
        // popupDelay.setEnabled(keystrokeParse.isSelected());

        Box center = Box.createVerticalBox();
        center.add(showPopup);
        center.add(sortByAction);
        center.add(new JLabel(jEdit.getProperty(
                    "options.shortcuts.popup.delay.label")));
        center.add(popupDelay);

        add(BorderLayout.CENTER, center);
    }

    /**
     *  Description of the Method
     */
    protected void _save() {
        jEdit.setBooleanProperty("options.shortcuts.displaypopup",
            showPopup.isSelected());
        jEdit.setBooleanProperty("options.shortcuts.sortbyaction",
            sortByAction.isSelected());
        jEdit.setIntegerProperty("options.shortcut-display.popup.delay", popupDelay.getValue());
    }
}

