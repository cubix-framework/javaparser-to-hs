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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class MultilineLabel
    extends JPanel {
    private GridBagConstraints constraints;
    private String text;

    /**
     * Creates a new MultilineLabel object.
     */
    public MultilineLabel() {
        this("");
    }

    /**
     * Creates a new MultilineLabel object.
     *
     * @param text DOCUMENT ME!
     */
    public MultilineLabel(String text) {
        super(new GridBagLayout());
        constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.NONE;
        setText(text);
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     */
    public void setFont(Font f) {
        super.setFont(f);

        for (int i = 0; i < getComponentCount(); i++) {
            getComponent(i).setFont(f);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param text DOCUMENT ME!
     */
    public void setText(String text) {
        this.text = text;
        removeAll();

        StringTokenizer tok = new StringTokenizer(text, "\n");
        constraints.weighty = 0.0;
        constraints.weightx = 1.0;

        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();

            if (!tok.hasMoreTokens()) {
                constraints.weighty = 1.0;

            }
            UIUtil.jGridBagAdd(this, new JLabel(t), constraints,
                               GridBagConstraints.REMAINDER);
        }

        revalidate();
        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getText() {
        return text;
    }
}
