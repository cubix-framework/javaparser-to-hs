/*
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
package projectviewer.ui.tree;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import projectviewer.*;


/**
 * Returns the cells of the project tree.
 */
public class ProjectTreeRenderer extends DefaultTreeCellRenderer {

   private Font normalFont;
   private Font openedFont;

   private Icon projectIcon;
   private Icon fileViewIcon;

   /**
    * Create a new <code>ProjectTreeRenderer</code>.
    */
   public ProjectTreeRenderer() {
      projectIcon    = new ImageIcon(getClass().getResource( "/projectviewer/ui/resources/Project.gif" ));
      fileViewIcon   = new ImageIcon(getClass().getResource( "/projectviewer/ui/resources/FileView.gif" ));

      normalFont = UIManager.getFont("Tree.font");
      openedFont = normalFont.deriveFont(Font.BOLD);
   }


   /**
    * Returns a cell renderer.
    */
   public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                 boolean sel, boolean expanded,
                                                 boolean leaf, int row,
                                                 boolean focus)
   {
      Component c = super.getTreeCellRendererComponent(tree, getDisplayValue(value), 
                                                       sel, expanded, leaf, row,
                                                       focus);
      Icon icon = getIcon(value, expanded, leaf);
      if (icon != null) {
         setIcon(icon);
      }
      Font font = getFont(value, tree);
      if (font != null) {
         setFont(font);
      }
      return c;
   }
   
   /**
    * Returns the display value to be rendered.
    */
   protected Object getDisplayValue(Object actualValue) {
      if (actualValue instanceof Project) {
         return ((Project) actualValue).getName();
      } else if (actualValue instanceof ProjectArtifact) {
         return ((ProjectArtifact) actualValue).getName();
      }
      return actualValue;
   }

   /**
    * Returns the icon to render for this value.  Return <code>null</code> to
    * use the default icons.
    */
   protected Icon getIcon(Object value, boolean expanded, boolean leaf) {
      if (value instanceof Project || value instanceof ProjectProxyNode) {
         return projectIcon;
      } else if (value instanceof FileView) {
         return fileViewIcon;
      }
      return null;
   }

   /**
    * Returns the font to use for the specified file.
    */
   protected Font getFont(Object value, JTree tree) {
      if ( !(value instanceof ProjectFile) ) {
         return normalFont;
      }
      ProjectFile file = (ProjectFile) value;
      if (file.isOpened()) {
         return openedFont;
      }
      return normalFont;
   }

}
