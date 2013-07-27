/*
 * :tabSize=4:indentSize=4:noTabs=true:
 * :folding=explicit:collapseFolds=1:
 *
 * (c) 2005 Marcelo Vanzin
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package p4plugin;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

import p4plugin.action.P4FileAction;

/**
 *  Listens for key events on the jEdit text area and asks the
 *  user whether he wants to do a "p4 edit" if he tries to type
 *  into a read-only buffer that is managed by a project using
 *  Perforce.
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: BufferKeyHandler.java 6458 2006-06-12 05:17:23Z vanza $
 *  @since      P4P 0.1
 */
public class BufferKeyHandler implements KeyListener {

    /**
     *  P4Plugin will set this to true when the interceptor should
     *  be removed but is currently null. This happens when the user
     *  executes an action, using a keyboard shortcut, that would
     *  cause the interceptor to be removed. But since this class
     *  sets the interceptor to null so that normal keyboard event
     *  processing is done by the view, P4Plugin notifies this class
     *  that it shouldn't reinstall the interceptor in this case.
     */
    protected boolean removeInterceptor = false;

    public void keyPressed(KeyEvent e) {
        processEvent(e);
    }

    public void keyReleased(KeyEvent e) {
        processEvent(e);
    }

    public void keyTyped(KeyEvent e) {
        if (processEvent(e))
            return;
        removeInterceptor = false;

        View v = jEdit.getActiveView();
        int res = JOptionPane.showConfirmDialog(
                    v.getContentPane(),
                    jEdit.getProperty("p4plugin.buffer_handler.msg"),
                    jEdit.getProperty("p4plugin.buffer_handler.title"),
                    JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            P4FileAction action = new P4FileAction("edit", false);
            action.setPath(jEdit.getActiveView().getBuffer().getPath());
            action.actionPerformed(null);
        } else {
            // ask only once.
            v.setKeyEventInterceptor(null);
        }
    }

    /**
     *  Send the event to jEdit before having the plugin interpret
     *  it. Returns whether the key interceptor should stop
     *  execution of its actions for this event or not.
     */
    private boolean processEvent(KeyEvent e) {
        View v = jEdit.getActiveView();
        v.setKeyEventInterceptor(null);
        jEdit.getActiveView().processKeyEvent(e, true);
        if (removeInterceptor)
            return true;
        v.setKeyEventInterceptor(this);
        return e.isConsumed();
    }

}

