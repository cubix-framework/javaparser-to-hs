/*
 *  Copyright (C) 2003 Don Brown (mrdon@techie.com)
 *  Copyright (C) 2000, 2001 Greg Merrill (greghmerrill@yahoo.com)
 *  This file is part of Log Viewer, a plugin for jEdit (http://www.jedit.org).
 *  It is heavily  based off Follow (http://follow.sf.net).
 *  Log Viewer is free software; you can redistribute it and/or modify
 *  it under the terms of version 2 of the GNU General Public
 *  License as published by the Free Software Foundation.
 *  Log Viewer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Log Viewer; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package logviewer;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.AbstractAction;

/**
 *  Deletes the contents of all opened log files
 *
 * @author    <a href="mailto:mrdon@techie.com">Don Brown</a>
 * @author    <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
 */
class DeleteAll extends AbstractAction {

    LogViewer app;

    /**
     *  Constructor for the DeleteAll action
     *
     * @param  app   The main class
     * @param  name  The name of the action
     */
    public DeleteAll(LogViewer app, String name) {
        super(name);
        this.app = app;
    }

    /**
     *  Performs the action
     *
     * @param  e  The action event
     */
    public void actionPerformed(ActionEvent e) {
        if (app.attributes_.confirmDeleteAll()) {
            Frame parent = app.isFloating() ? GUIUtils.getRootFrame(app) : app.getView();
            DisableableConfirm confirm = new DisableableConfirm(
                    parent,
                    app.getProperty("dialog.confirmDeleteAll.title"),
                    app.getProperty("dialog.confirmDeleteAll.message"),
                    app.getProperty("dialog.confirmDeleteAll.confirmButtonText"),
                    app.getProperty("dialog.confirmDeleteAll.doNotConfirmButtonText"),
                    app.getProperty("dialog.confirmDeleteAll.disableText")
                    );
            confirm.pack();
            GUIUtils.center(confirm, parent);
            confirm.setVisible(true);
            if (confirm.markedDisabled()) {
                app.attributes_.setConfirmDeleteAll(false);
            }
            if (confirm.markedConfirmed()) {
                performDelete();
            }
        }
        else {
            performDelete();
        }
    }

    /**  Performs the delete */
    private void performDelete() {
        app.setCursor(Cursor.WAIT_CURSOR);
        List allFileFollowingPanes = app.getAllFileFollowingPanes();
        Iterator i = allFileFollowingPanes.iterator();
        FileFollowingPane fileFollowingPane;
        try {
            while (i.hasNext()) {
                fileFollowingPane = (FileFollowingPane) i.next();
                fileFollowingPane.clear();
            }
            app.setCursor(Cursor.DEFAULT_CURSOR);
        }
        catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            app.setCursor(Cursor.DEFAULT_CURSOR);
            JOptionPane.showMessageDialog(
                    app,
                    app.getProperty("message.unableToDeleteAll.text"),
                    app.getProperty("message.unableToDeleteAll.title"),
                    JOptionPane.WARNING_MESSAGE
                    );
        }
    }

}

