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
package gruntspud.standalone;

import gruntspud.GruntspudContext;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class HTMLViewerFrame
    extends JFrame {
    private GruntspudContext context;
    private JButton close;
    private HTMLViewerPane viewer;

    /**
     * Creates a new HTMLViewerFrame object.
     *
     * @param context DOCUMENT ME!
     * @param url DOCUMENT ME!
     */
    public HTMLViewerFrame(GruntspudContext context, URL url) {
        super(url.toExternalForm());

        //
        this.context = context;

        //
        viewer = new HTMLViewerPane(context, url);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeViewer();
            }
        });

        //
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        close = new JButton("Close");
        close.setMnemonic('c');
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeViewer();
            }
        });
        b.add(close);

        //
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(viewer, BorderLayout.CENTER);
        getContentPane().add(b, BorderLayout.SOUTH);

        if (!context.getHost().isGeometryStored(JDK13GruntspudHost.
                                                PROP_HTML_VIEWER_GEOMETRY)) {
            setSize(400, 500);
            setLocation(100, 100);
        }
        else {
            context.getHost().loadGeometry(this,
                                           JDK13GruntspudHost.
                                           PROP_HTML_VIEWER_GEOMETRY);
        }

        setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param u DOCUMENT ME!
     */
    public void setURL(URL u) {
        viewer.setURL(u);
        setTitle(u.toExternalForm());
    }

    private void closeViewer() {
        context.getHost().saveGeometry(this,
                                       JDK13GruntspudHost.
                                       PROP_HTML_VIEWER_GEOMETRY);
        setVisible(false);
    }
}
