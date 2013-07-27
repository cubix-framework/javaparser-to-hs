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

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.editor.MiniTextEditor;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class SimpleEditorFrame
    extends JFrame
    implements ActionListener,
    WindowListener {
    private GruntspudContext context;
    private JButton close;
    private MiniTextEditor editor;

    /**
     * Creates a new SimpleEditorFrame object.
     *
     * @param context DOCUMENT ME!
     * @param conflicts DOCUMENT ME!
     */
    public SimpleEditorFrame(GruntspudContext context, boolean conflicts) {
        super("<New Document");

        //
        this.context = context;

        //
        editor = new MiniTextEditor(context, true, true, false,
                                    JDK13GruntspudHost.PROP_EDITOR_WORD_WRAP, false,
                                    conflicts ?
                                    MiniTextEditor.CONFLICT_RESOLVER
                                    : MiniTextEditor.LINE_NUMBERED_EDITOR);
        setIconImage( ( (ImageIcon) UIUtil.loadIconForResource(
            "images/fish.png")).getImage());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        //
        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        close = new JButton("Close");
        close.setMnemonic('c');
        close.addActionListener(this);
        b.add(close);

        //
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(editor, BorderLayout.CENTER);
        getContentPane().add(b, BorderLayout.SOUTH);

        if (!context.getHost().isGeometryStored(Constants.EDITOR_GEOMETRY)) {
            setBounds(100, 100, 600, 500);
        }
        else {
            context.getHost().loadGeometry(this, Constants.EDITOR_GEOMETRY);
        }

        setVisible(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     */
    public void openFile(File file, String encoding) {
        editor.openFile(file, encoding);
        setTitle(file.getName());
        editor.grabFocus();
    }

    private void checkForChangesAndClose() {
        if (editor.isChanged()) {
            int opt = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Save now?", "Unsaved changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (opt != JOptionPane.CANCEL_OPTION) {
                if (opt == JOptionPane.YES_OPTION) {
                    editor.save(editor.getFile(), false);
                }
            }
            else {

                return;
            }
        }

        editor.cleanUp();
        context.getHost().saveGeometry(this, Constants.EDITOR_GEOMETRY);
        dispose();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowActivated(WindowEvent evt) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowDeactivated(WindowEvent evt) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowClosing(WindowEvent evt) {
        checkForChangesAndClose();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowClosed(WindowEvent evt) {
        editor.cleanUp();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowIconified(WindowEvent evt) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowDeiconified(WindowEvent evt) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void windowOpened(WindowEvent evt) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        checkForChangesAndClose();
    }
}
