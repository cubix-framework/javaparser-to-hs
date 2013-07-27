/* 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package projectviewer;

// Import Java
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

// Import AWT/Swing
import javax.swing.JTree;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;

// Import jEdit
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

import projectviewer.event.ProjectEvent;
import projectviewer.event.ProjectListener;
import projectviewer.config.ProjectViewerConfig;

/**
 *  <p>Miscleaneous methods to remove things from projects.</p>
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: RemovalManager.java 5862 2002-10-22 12:51:27Z ensonic $
 */
public final class RemovalManager implements ProjectListener {
    
    //--------------- Static Members
    
    private final static int FILE  = 1;
    private final static int DIR   = 2;
    private final static int PROJ  = 3;
    private final static int MULTI = 4;
    
    /**
     *  Receives a collection of TreePath objects and returns the underlying
     *  objects selected, removing a child when its parent has also been
     *  selected.
     */
    private static List getSelectedArtifacts(TreePath[] paths) {
        TreePath last = null;
        ArrayList objs = new ArrayList();
        
        for (int i = 0; i < paths.length; i++) {
            if (last != null && !paths[i].isDescendant(last)) {
                last = null;
            } 
            
            if (last == null) {
                last = paths[i];
                objs.add(paths[i].getLastPathComponent());
            }
        }
        
        return objs;
    }

    //--------------- Instance Variables
    
    private ProjectViewer viewer;
    private boolean willDelete;
    
    //--------------- Constructor
    
    /**
     *  Constructs a new RemovalManager to process the removal of selected
     *  nodes in the current tree of the provided ProjectViewer instance.
     */
    public RemovalManager(ProjectViewer aViewer) {
        this.viewer = aViewer;
    }
    
    //--------------- Public Methods
    
    /**
     *  <p>Method to handle the removal of a selection made in the tree. This is
     *  used from inside the listeners to handle the selections the same way.</p>
     *
     *  <p>The method analyses which objects are selected, firing the respective
     *  remove action for the object. Projects selected are removed. Files and
     *  directories selected are removed and, optionally, deleted from disk.
     *  There's no option to remove a project and all its files/dirs (yet?).</p>
     *  
     *  <p>Confirmation dialogs are shown for the following actions: removal of
     *  directories from the project, removal of projects, physical removal
     *  of any kind (files or dirs), and multiple selections. No confirmation is
     *  asked for single file removals.</p>
     *
     *  @param  tree    The tree where to look for the selection.
     *  @param  delete  Whether files and directories should be removed from disk.
     */
    public void handleSelectionRemoval(boolean delete) {
        JTree tree = viewer.getCurrentTree();
        Project p = viewer.getCurrentProject();
        willDelete = delete; 
        p.addProjectListener(this);
        
        switch (tree.getSelectionCount()) {
            case 0:
                // No Selecion!
                return;
                
            case 1: {
                // Single selection!
                remove(tree.getLastSelectedPathComponent(),delete,true);
                break;
            }
                
            default: {
                if (confirmAction(delete,MULTI)) {
                    // Any other thing == multiple selection
                    List sel = getSelectedArtifacts(tree.getSelectionPaths());
                    for (Iterator i = sel.iterator(); i.hasNext(); ) {
                        remove(i.next(),delete,false);
                    }
                }
            }
        }
        
        p.removeProjectListener(this);
    }
    

    /** 
     *  Asks the user if he reeeeeally wants to delete the selection he's
     *  made.
     *
     *  @param  delete  If the selection will be deleted or only removed
     *                  from the project;
     *  @param  type    Type of object being removed. 1=File 2=Directory 3=Project
     *                  4=Multiple objects
     */
    private boolean confirmAction(boolean delete, int type) {
        String message = null;
        
        // Sanity check
        if (type > MULTI || type < FILE) return false;
        
        if (delete) {
            switch (type) {
                case FILE: // File
                    message = "Do you really want to delete the selected file from disk?";
                    break;
                    
                case DIR: // Directory
                    message = "Do you really want to recursively delete the selected directory from disk?";
                    break;
                    
                case MULTI: // Multiple
                    message = "Do you really want to delete the selected nodes from disk?";
                    break;
                    
                default: // No other deletion is supported (yet?)
                    return false;
            }
        } else {
            switch(type) {
                case FILE: // File
                    message = "Do you really want to delete the selected file from disk?";
                    break;
                    
                case DIR: // Directory
                    message = "Do you really want to remove the selected directory from the project?";
                    break;
                    
                case PROJ: // Project
                    message = "Do you really want to remove the selected project?";
                    break;
                    
                case MULTI: // Multiple
                    message = "Do you really want to remove the selected nodes?";
                    break;
                    
                default: // Won't happen, but...
            }
        }
        
        if (message == null) return true;
        
      int choice = 
            JOptionPane.showConfirmDialog(
                viewer,
            message,
                "Confirmation of action:",
            JOptionPane.YES_NO_OPTION
            );
      return (choice == JOptionPane.YES_OPTION);
    }
    
    /**
     *  Removes an object from a project (or the project, if the object is one).
     *
     *  @param  o       The object.
     *  @param  delete  If deletion from disk should occur.
     *  @param  ask     Is we should ask for confirmation.
     */
    private void remove(Object o, boolean delete, boolean ask) {
		boolean removed = false;
		
		if(o == null) {
			Log.log(Log.DEBUG, this, "Removing: unexpected object o==null");
			return;
		}
        if (o instanceof ProjectFile) {
            if (!delete || (!ask || confirmAction(delete,FILE))) {
                Log.log(Log.DEBUG, this, "Removing: " + o);
                Project p = viewer.getCurrentProject();
                p.removeFile((ProjectFile)o,delete);
				removed = true;
            }
        }
		else if (o instanceof ProjectDirectory) {
            if (!ask || confirmAction(delete,DIR)) {
                Log.log(Log.DEBUG, this, "Removing: " + o);
                Project p = viewer.getCurrentProject();
                p.removeDirectory((ProjectDirectory)o,delete);
				removed = true;
            }
        }
		else if (o instanceof Project) {
            if (!ask || confirmAction(false,PROJ)) {
                Log.log(Log.DEBUG, this, "Removing: " + o);
                ProjectManager.getInstance().removeProject((Project)o);
                viewer.setCurrentProject(null);
                viewer.refresh();
				removed = true;
            }
        }
		if (ProjectViewerConfig.getInstance().getSaveOnChange()) {
			Project p = viewer.getCurrentProject();
			// is it always null ?
			if(p != null) p.save();
		}
		Log.log(Log.DEBUG, this, "  end");
    }
    
    //--------------- Project Listener interface
    
   public void fileOpened(ProjectEvent evt) { }

   public void fileClosed(ProjectEvent evt) { }

   public void fileRemoved(ProjectEvent evt) { 
        if (willDelete) {
            ProjectFile aFile = (ProjectFile) evt.getArtifact();
            Buffer buf = aFile.getBuffer();
            if (buf != null) {
                jEdit.closeBuffer(viewer.getView(),buf);
            }
        }
    }

   public void directoryRemoved(ProjectEvent evt)  { }

   public void directoryAdded(ProjectEvent evt) { }

   public void fileAdded(ProjectEvent evt) { }  
   
   public void buildFileSelected(ProjectEvent evt) {
      // no-op  
   }
   
}
