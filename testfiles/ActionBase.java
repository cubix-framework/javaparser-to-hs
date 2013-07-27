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
package projectviewer;

import java.awt.event.ActionEvent;
import javax.swing.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;
import projectviewer.*;
import projectviewer.ui.ProjectViewer;


/**
 * A action to create a view.
 */
public abstract class ActionBase extends AbstractAction
         implements ProjectAction
{

   protected ProjectViewer projectViewer;

   /**
    * Create a new <code>ActionBase</code>.
    */
   public ActionBase(String aName, ProjectViewer aProjectViewer)
   {
      super(ProjectPlugin.getProperty("tree-action", aName));
      projectViewer = aProjectViewer;
   }

   /**
    * Create a new <code>ActionBase</code>.
    */
   public ActionBase(ProjectViewer aProjectViewer)
   {
      projectViewer = aProjectViewer;
   }

   /**
    * Create a new <code>ActionBase</code>.
    */
   public ActionBase(String aName)
   {
      this(aName, null);
   }

   /**
    * Set the {@link ProjectViewer}.
    */
   public void setProjectViewer(ProjectViewer aProjectViewer)
   {
      projectViewer = aProjectViewer;
   }

   /**
    * Create a new <code>View</code>.
    */
   public void actionPerformed(ActionEvent evt)
   {
      try {
         performAction();
      } catch (Throwable e) {
         ProjectPlugin.error(e);
         JOptionPane.showMessageDialog(projectViewer.getView(), e.getMessage(),
                                       "Project Viewer Error", JOptionPane.ERROR_MESSAGE);
      }
   }

   /**
    * Perform the action.  Any {@link ProjectException}s thrown will be handled appropriately.
    */
   protected void performAction() throws ProjectException {}

   /**
    * Returns the jEdit view.
    */
   protected View getView()
   {
      return projectViewer.getView();
   }

   /**
    * Returns the plugin object.
    */
   protected ProjectPlugin getPlugin()
   {
      return projectViewer.getPlugin();
   }

   /**
    * Returns the project manager object.
    */
   protected ProjectManager getProjectManager()
   {
      return getPlugin().getProjectManager();
   }

}
