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
package p4plugin.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

import projectviewer.ProjectViewer;
import projectviewer.vpt.VPTNode;

import p4plugin.P4Shell;
import p4plugin.Perforce;

/**
 *  Calls p4 on the selected file with a configurable command. If requested,
 *  shows the change list dialog before executing the command, so the user
 *  can choose a specific change list for the action. Designed with commands
 *  like "add", "edit" and "delete" in mind.
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: P4FileAction.java 6450 2006-04-08 23:16:54Z vanza $
 *  @since      P4P 0.1
 */
public class P4FileAction extends AbstractP4Action {

    private String cmd;
    private String path;

    public P4FileAction(String cmd, boolean defaultChangeList) {
        this(cmd, defaultChangeList, true);
    }

    public P4FileAction(String cmd, boolean defaultChangeList, boolean showDefault) {
        super(getActionName(cmd, !defaultChangeList), !defaultChangeList, showDefault);
        this.cmd = cmd;
    }

    /**
     *  If the path is set, it will be used instead of the current
     *  selection in ProjectViewer.
     */
    public void setPath(String path) {
        this.path = path;
    }

    protected String getCommand() {
        return cmd;
    }

    protected String[] getArgs(ActionEvent ae) {
        if (path == null) {
            VPTNode node = viewer.getSelectedNode();
            return new String[] { node.getNodePath() };
        } else {
            return new String[] { path };
        }
    }

    public void prepareForNode(VPTNode node) {
        getMenuItem().setVisible(node != null && node.isFile());
    }

    /**
     *  If the buffer is dirty, refuse to run since I don't want
     *  to have to mess around with jEdit's I/O thread pool to enable
     *  recalling this action after the user has chosen to reload
     *  the contents of the buffer.
     */
    public void actionPerformed(ActionEvent ae) {
        if (path == null) {
            VPTNode node = viewer.getSelectedNode();
            Buffer b = jEdit.getBuffer(node.getNodePath());
            if (b != null && b.isDirty()) {
                JOptionPane.showMessageDialog(
                    viewer.getView().getContentPane(),
                    jEdit.getProperty("p4plugin.action.buffer_dirty.msg"),
                    jEdit.getProperty("p4plugin.action.buffer_dirty.title"),
                    JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        super.actionPerformed(ae);
    }

    protected void postProcess(Perforce p4) {
        if (path == null) {
            VPTNode node = viewer.getSelectedNode();
            Buffer b = jEdit.getBuffer(node.getNodePath());
            if (b != null) {
                if (cmd.equals("revert")) {
                    b.reload(viewer.getView());
                } else {
                    b.checkFileStatus(viewer.getView());
                }
            }
            ProjectViewer.nodeChanged(viewer.getSelectedNode());
        } else {
            jEdit.getBuffer(path).checkFileStatus(jEdit.getActiveView());
        }
        showInShell(p4);
    }

}

