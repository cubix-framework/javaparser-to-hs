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

import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Mini text editor
 *
 *@author     Brett Smiht
 *@created    26 May 2002
 */
public class GruntspudLogo
    extends JPanel {
    //  Private instance variables
    private Icon staticIcon;

    //  Private instance variables
    private Icon aniIcon;
    private boolean animate;
    private JLabel fish;
    private GruntspudContext context;

    /**
     *  Constructor
     *
     *@param  host                        Description of the Parameter
     */
    public GruntspudLogo(GruntspudContext context) {
        super(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        this.context = context;

        MouseListener l = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                homePage();
            }
        };

        //
        Icon logoIcon = UIUtil.getCachedIcon(Constants.GRUNTSPUD_LOGO);
        staticIcon = UIUtil.getCachedIcon(Constants.GRUNTSPUD_LOGO_STATIC);
        aniIcon = UIUtil.getCachedIcon(Constants.GRUNTSPUD_LOGO_ANIMATION);

        JLabel gl = new JLabel(logoIcon);
        gl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gl.setToolTipText(Gruntspud.getHomePage().getHost());
        gl.addMouseListener(l);

        fish = new JLabel(staticIcon);
        fish.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fish.setToolTipText(Gruntspud.getHomePage().getHost());
        fish.addMouseListener(l);

        //
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(Gruntspud.getHomePage().getHost());

        add(gl);
        add(fish);
    }

    private void homePage() {
        context.getHost().viewHTML(Gruntspud.getHomePage());
    }

    /**
     * DOCUMENT ME!
     *
     * @param animate DOCUMENT ME!
     */
    public synchronized void setAnimate(boolean animate) {
        if (animate == this.animate) {
            return;
        }

        this.animate = animate;

        if (animate) {
            fish.setIcon(aniIcon);
        }
        else {
            fish.setIcon(staticIcon);
        }
    }
}
