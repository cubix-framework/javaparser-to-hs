/*:folding=indent:
* ProjectViewerPanel.java - Displays project files in a tree.
* Copyright (C) 2003 Anthony Roy
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
package uk.co.antroy.latextools;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import uk.co.antroy.latextools.macros.ProjectMacros;


public class ProjectViewerPanel
    extends JPanel
    implements MouseListener {

    //~ Instance/static variables .............................................

    private JTree tree;
    private View view;
    //private Buffer buffer;

    //~ Constructors ..........................................................

    public ProjectViewerPanel(View view, Buffer buffer) {
        //this.buffer = buffer;
        this.view = view;
        tree = new JTree(ProjectMacros.getProjectFiles(view, buffer));
        tree.setShowsRootHandles(true);
        tree.addMouseListener(this);
        tree.setToggleClickCount(3);
        setLayout(new BorderLayout());
        add(tree, BorderLayout.CENTER);
    }

    //~ Methods ...............................................................

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {

        TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)selPath.getLastPathComponent();

        if (node == null) {

            return;
        }

        String fileName = node.getUserObject().toString();
        Buffer buff = jEdit.getBuffer(fileName);

        if (e.getClickCount() == 2) {

            if (buff == null) {
                jEdit.openFile(view, fileName);
            }
        } else if (e.getClickCount() == 1) {

            if (buff != null) {

                if ((e.getModifiers() & e.ALT_MASK) == e.ALT_MASK) {
                    jEdit.closeBuffer(view, buff);
                } else {
                    view.setBuffer(buff);
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }
}
