/*
 * jEdit edit mode settings:
 * :mode=java:tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
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

package javainsight;


// GUI support
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

// buildtools support
import javainsight.buildtools.packagebrowser.JavaClass;

// jEdit properties support
import org.gjt.sp.jedit.jEdit;


/**
 * A popup menu for the classpath package tree.
 *
 * @author Dirk Moebius
 * @version $Id: TreePopup.java 9294 2001-04-22 20:18:37Z dmoebius $
 */
class TreePopup extends JPopupMenu {

    public TreePopup(ClasspathManager classpathMgr, Object userObject) {
        super();
        this.classpathMgr = classpathMgr;

        Font font = UIManager.getFont("MenuItem.font");
        Font fontPlain = new Font(font.getName(), Font.PLAIN, font.getSize());
        Font fontBold = new Font(font.getName(), Font.BOLD, font.getSize());
        ActionHandler ah = new ActionHandler();
        String view = jEdit.getProperty("javainsight.tree.view", "view-packages");
        boolean viewFlat = jEdit.getBooleanProperty("javainsight.tree.viewIsFlat", false);
        ButtonGroup viewGroup = new ButtonGroup();
        JMenuItem mi;

        // menu item "Decompile with Jode"
        if (userObject != null && userObject instanceof JavaClass) {
            mi = new JMenuItem(jEdit.getProperty("javainsight.tree.menu.decompile.label"));
            mi.setActionCommand("decompile");
            mi.addActionListener(ah);
            mi.setFont(fontBold);
            add(mi);
            addSeparator();
        }

        // menu item "Add Archive..."
        mi = new JMenuItem(jEdit.getProperty("javainsight.tree.menu.add-archives.label"));
        mi.setActionCommand("add-archives");
        mi.addActionListener(ah);
        mi.setFont(fontPlain);
        add(mi);
        addSeparator();

        // menu item "Classpath View"
        mi = new JRadioButtonMenuItem(jEdit.getProperty("javainsight.tree.menu.view-classpath.label"), view.equals("view-classpath"));
        mi.setActionCommand("view-classpath");
        mi.addActionListener(ah);
        mi.setFont(fontPlain);
        add(mi);
        viewGroup.add(mi);

        // menu item "Package View"
        mi = new JRadioButtonMenuItem(jEdit.getProperty("javainsight.tree.menu.view-packages.label"), view.equals("view-packages"));
        mi.setActionCommand("view-packages");
        mi.addActionListener(ah);
        mi.setFont(fontPlain);
        add(mi);
        viewGroup.add(mi);

        // menu item "Flat Packages"
        mi = new JCheckBoxMenuItem(jEdit.getProperty("javainsight.tree.menu.view-flat.label"), viewFlat);
        mi.setActionCommand("view-flat");
        mi.addActionListener(ah);
        mi.setFont(fontPlain);
        add(mi);
    }


    private ClasspathManager classpathMgr;


    class ActionHandler implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            String actionCommand = evt.getActionCommand();
            if (actionCommand.equals("decompile")) {
                classpathMgr.decompileAction();
            }
            else if (actionCommand.equals("add-archives")) {
                classpathMgr.addArchivesAction();
            }
            else if (actionCommand.equals("view-classpath")) {
                classpathMgr.setView(actionCommand);
            }
            else if (actionCommand.equals("view-packages")) {
                classpathMgr.setView(actionCommand);
            }
            else if (actionCommand.equals("view-flat")) {
                classpathMgr.toggleFlatView();
            }
        }
    }

}
