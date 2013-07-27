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

/**
 * $Log: ScrollingPanel.java,v $
 * Revision 1.5  2003/11/22 17:33:35  t_magicthize
 * Started work on internationalisation. Split the gruntspud.ui package up into more manageable parts. Added option to show text on some actions.
 *
 * Revision 1.4  2003/07/21 20:25:11  t_magicthize
 * Preparation for release.
 *
 * Revision 1.3  2003/03/30 19:21:51  t_magicthize
 * See RELEASE_NOTES.txt (0.4.0-beta)
 *
 * Revision 1.2  2003/01/30 23:37:54  t_magicthize
 * Global source format using jalopy
 *
 * Revision 1.1  2002/12/23 01:22:09  t_magicthize
 * Many improvements. The new preferences dialog UI in the standalone version. Many other small bug fixes.
 *
 * Revision 1.2  2001/12/11 22:24:45  Hymndinner
 * Added 'Log' statements to all source in this tree.
 *
 */
package gruntspud.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.plaf.basic.BasicArrowButton;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 */
public class ScrollingPanel
    extends JPanel
    implements ActionListener {
    protected JButton north;
    protected JButton south;
    protected JViewport viewport;
    protected int incr = 48;

    /**
     * Creates a new ScrollingPanel object.
     *
     * @param component DOCUMENT ME!
     */
    public ScrollingPanel(Component component) {
        setLayout(new BorderLayout());
        north = new BasicArrowButton(BasicArrowButton.NORTH);
        south = new BasicArrowButton(BasicArrowButton.SOUTH);
        viewport = new JViewport();
        add(north, BorderLayout.NORTH);
        add(viewport, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        viewport.setView(component);
        north.addActionListener(this);
        south.addActionListener(this);
        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param incr DOCUMENT ME!
     */
    public void setIncrement(int incr) {
        System.out.println("Setting increment to " + incr);
        this.incr = incr;
    }

    //	public void setBounds(int x, int y, int w, int h)
    //	{
    //		super.setBounds(x, y, w, h);
    //		Dimension view = new Dimension(w, h);
    //		Dimension pane = viewport.getView().getPreferredSize();
    //		viewport.setViewPosition(new Point(0, 0));
    //		remove(north);
    //		if (pane.height >= view.height)
    //		{
    //			add("South", south);
    //		}
    //		else
    //		{
    //			remove(south);
    //		}
    //		doLayout();
    //	}
    public void actionPerformed(ActionEvent event) {
        /*
           Dimension view = getSize();
           Dimension pane = viewport.getView().getSize();
           Point top = viewport.getViewPosition();
           if (event.getSource() == north)
           {
           Point newPoint = new Point(top.x, top.y - incr);
           viewport.setViewPosition(newPoint);
               }
               if (event.getSource() == south)
               {
           Point newPoint = new Point(top.x, top.y + incr);
           if( ( newPoint.y + viewport.getViewSize().height )  >
               ( pane.getSize().height ) )
             newPoint.y = pane.getSize().height - viewport.getViewSize().height;
           viewport.setViewPosition(newPoint);
               }
         */
        Dimension view = getSize();
        Dimension pane = viewport.getView().getPreferredSize();
        Point top = viewport.getViewPosition();

        if (event.getSource() == north) {
            if (top.y < incr) {
                viewport.setViewPosition(new Point(0, 0));
            }
            else {
                viewport.setViewPosition(new Point(0, top.y - incr));
            }
        }

        if (event.getSource() == south) {
            int max = pane.height - view.height;

            if (top.y > (max - incr)) {
                view = viewport.getExtentSize();
                max = pane.height - view.height;
                viewport.setViewPosition(new Point(0, max));
            }
            else {
                viewport.setViewPosition(new Point(0, top.y + incr));
            }
        }

        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     */
    public void setAvailableActions() {
        Dimension view = getSize();
        Dimension pane = viewport.getView().getPreferredSize();
        Point top = viewport.getViewPosition();
        north.setEnabled(top.y > 0);
        south.setEnabled( (top.y + view.height) < pane.height);
    }
}
