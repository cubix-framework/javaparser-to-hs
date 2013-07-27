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
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class ToolBarTablePane
    extends JPanel {
    /**
     *  Constructor for
     *
     */
    public ToolBarTablePane(JToolBar toolBar, JTable table) {
        super(new BorderLayout());
        setOpaque(false);

        JPanel t = new JPanel(new BorderLayout());
		t.setOpaque(false);
		toolBar.setOpaque(false);
        t.add(toolBar, BorderLayout.NORTH);

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		sep.setOpaque(false);
        t.add(sep, BorderLayout.CENTER);

        JScrollPane scroller = new JScrollPane(table) {
            public Dimension getPreferredSize() {
                Dimension d = getScrollPanePreferredSize();

                return (d == null) ? super.getPreferredSize() : d;
            }
        };

        ;
        scroller.setOpaque(false);
        scroller.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 0, 0, 0),
            BorderFactory.createLineBorder(UIManager.getColor(
            "Label.foreground"))));
        add(t, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getScrollPanePreferredSize() {
        return null;
    }
}
