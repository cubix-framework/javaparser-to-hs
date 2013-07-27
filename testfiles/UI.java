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
package projectviewer.ui;

import java.awt.*;
import java.io.File;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import projectviewer.ProjectPlugin;

/**
 * UI helper methods.
 */
public class UI
{

   /**
    * Center this dialog.
    */
   public static void center(Window window)
   {
      Dimension scrn = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension win = window.getSize();
      window.setLocation((scrn.width - win.width) / 2, (scrn.height - win.height) / 2);
   }

   /**
    * Ask a user to retrieve a file.  Returns <code>null</code> if the user did
    * not select one.
    */
   public static File getFile(View view, String curDir)
   {
      String[] files = GUIUtilities
                       .showVFSFileDialog(view, curDir, VFSBrowser.OPEN_DIALOG, false);
      return (files == null) ? null : new File(files[0]);
   }

   /**
    * Use a file dialog to retrieve a list of files.
    */
   public static String[] getFiles(View view, String curDir)
   {
      return GUIUtilities
             .showVFSFileDialog(view, curDir, VFSBrowser.OPEN_DIALOG, true);
   }

   /**
    * Show a yes/no confirm dialog.
    */
   public static boolean confirmYesNo(Component c, String name, Object[] args)
   {
      int result = GUIUtilities.confirm(c, ProjectPlugin
         .getPropertyName("confirm", name), args, JOptionPane.YES_NO_OPTION,
         JOptionPane.QUESTION_MESSAGE);
      return (result == JOptionPane.YES_OPTION);
   }

   /**
    * Show an input dialog to the user.
    */
   public static String input(Component c, String name, Object value)
   {
      return GUIUtilities.input(c, ProjectPlugin.getPropertyName("input", name), value);
   }

   /**
    * Show a message dialog.
    */
   public static void message(Component c, String name, Object[] args)
   {
      GUIUtilities.message(c, ProjectPlugin.getPropertyName("message", name), args);
   }

   /**
    * Convert the given project name to its fully qualified property name.
    */
   private static String toFullPropertyName(String propName, String cat)
   {
      return ProjectPlugin.NAME + "." + propName + "." + cat;
   }

}
