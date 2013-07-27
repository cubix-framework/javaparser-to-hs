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

package gruntspud.ui.report;

import gruntspud.Constants;
import gruntspud.GruntspudContext;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URLEncoder;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class ReportFrame
    extends JFrame {
    private GruntspudContext context;
    private String frameGeometryKey;
    private FileInfoListPane infoListPane;
    private JButton close;

    /**
     * Creates a new ReportFrame object.
     *
     * @param context DOCUMENT ME!
     * @param infoListPane DOCUMENT ME!
     */
    public ReportFrame(GruntspudContext context, FileInfoListPane infoListPane) {
        super(infoListPane.getFileInfoPane().getActionText());

        Icon i = infoListPane.getFileInfoPane().getActionIcon();

        if (i instanceof ImageIcon) {
            setIconImage( ( (ImageIcon) i).getImage());

            //
        }
        this.context = context;
        this.infoListPane = infoListPane;

        try {
            //            this.frameGeometryKey = URLEncoder.encode(
            //                Constants.FILE_INFO_FRAME_GEOMETRY_PREFIX + getTitle(), "UTF-8");
            this.frameGeometryKey = URLEncoder.encode(Constants.
                FILE_INFO_FRAME_GEOMETRY_PREFIX +
                getTitle());
        }
        catch (Exception e) {
            Constants.UI_LOG.error(e);
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeReport();
            }
        });

        //
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        close = new JButton("Close");
        close.setMnemonic('c');
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                closeReport();
            }
        });
        b.add(close);

        //
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(infoListPane, BorderLayout.CENTER);
        getContentPane().add(b, BorderLayout.SOUTH);

        if ( (frameGeometryKey == null) ||
            !context.getHost().isGeometryStored(frameGeometryKey)) {
            setSize(400, 360);
            setLocation(110, 110);
        }
        else {
            String geom = context.getHost().getProperty(frameGeometryKey);
            context.getHost().loadGeometry(this, frameGeometryKey);
        }

        setVisible(true);
    }

    private void closeReport() {
        infoListPane.cleanUp();
        infoListPane = null;

        if (frameGeometryKey != null) {
            context.getHost().saveGeometry(this, frameGeometryKey);

        }
        dispose();
    }
}
