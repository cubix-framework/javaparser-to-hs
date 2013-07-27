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

import gruntspud.actions.GruntspudAction;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class FolderBar
    extends JPanel {
    public final static int SMALL_FOLDER_BAR = 0;
    public final static int NORMAL_FOLDER_BAR = 1;
    public final static int LARGE_FOLDER_BAR = 2;
    private JLabel textLabel;
    private JLabel iconLabel;
    private boolean large;
    private GruntspudAction action;
    private int type;

    /**
     * Creates a new FolderBar object.
     */
    public FolderBar() {
        this(null, null);
    }

    /**
     * Creates a new FolderBar object.
     *
     * @param text DOCUMENT ME!
     * @param icon DOCUMENT ME!
     */
    public FolderBar(String text, Icon icon) {
        this(text, icon, SMALL_FOLDER_BAR);
    }

    /**
     * Creates a new FolderBar object.
     *
     * @param text DOCUMENT ME!
     * @param icon DOCUMENT ME!
     * @param type DOCUMENT ME!
     */
    public FolderBar(String text, Icon icon, int type) {
        super(new BorderLayout());
        this.type = type;
        setOpaque(true);
        setBackground(getBackground().darker());
        add(textLabel = new JLabel(), BorderLayout.CENTER);
        add(iconLabel = new JLabel(), BorderLayout.WEST);

        switch (type) {
            case SMALL_FOLDER_BAR:
                iconLabel.setIcon(UIUtil.EMPTY_SMALL_ICON);

                break;
            case NORMAL_FOLDER_BAR:
                textLabel.setFont(textLabel.getFont().deriveFont(22f));
                iconLabel.setIcon(UIUtil.EMPTY_ICON);

                break;
            case LARGE_FOLDER_BAR:
                textLabel.setFont(textLabel.getFont().deriveFont(36f));
                iconLabel.setIcon(UIUtil.EMPTY_LARGE_ICON);

                break;
        }

        textLabel.setVerticalAlignment(JLabel.CENTER);
        textLabel.setVerticalTextPosition(JLabel.BOTTOM);
        textLabel.setForeground(Color.lightGray);
        iconLabel.setVerticalAlignment(JLabel.CENTER);
        setIcon(icon);
        setText(text);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public GruntspudAction getAction() {
        return action;
    }

    /**
     * DOCUMENT ME!
     *
     * @param action DOCUMENT ME!
     */
    public void setAction(GruntspudAction action) {
        this.action = action;

        switch (type) {
            case SMALL_FOLDER_BAR:
                setIcon( (Icon) action.getValue(GruntspudAction.SMALL_ICON));

                break;
            case NORMAL_FOLDER_BAR:
                setIcon( (Icon) action.getValue(GruntspudAction.ICON));

                break;
            case LARGE_FOLDER_BAR:
                setIcon( (Icon) action.getValue(GruntspudAction.LARGE_ICON));

                break;
        }

        setText( (String) action.getValue(Action.NAME));
    }

    /**
     * DOCUMENT ME!
     *
     * @param text DOCUMENT ME!
     */
    public void setText(String text) {
        textLabel.setText(text);
    }

    /**
     * DOCUMENT ME!
     *
     * @param icon DOCUMENT ME!
     */
    public void setIcon(Icon icon) {
        iconLabel.setIcon(icon);
    }
}
