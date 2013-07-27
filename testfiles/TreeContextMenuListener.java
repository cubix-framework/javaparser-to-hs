/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more detaProjectTreeSelectionListenerils.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package projectviewer.tree;

//{{{ Imports
// Import Swing/AWT
import java.io.File;
import java.util.Hashtable;

import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

// Import jEdit
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.search.*;

// Import ProjectViewer
import projectviewer.Project;
import projectviewer.ProjectFile;
import projectviewer.ProjectFileSet;
import projectviewer.ProjectViewer;
import projectviewer.RemovalManager;
import projectviewer.ProjectDirectory;
import projectviewer.ProjectFileImporter;

import projectviewer.config.ProjectPropertiesDlg;
import projectviewer.config.ProjectViewerConfig;
import projectviewer.config.AppLauncher;
//}}}

/**
 *  <p>Listener for mouse events in nodes of the tree. This listener is
 *  responsible for building the context-menus for right button clicks
 *  in the nodes of the tree.</p>
 *
 *  @author	 Marcelo Vanzin
 */
public class TreeContextMenuListener extends MouseAdapter implements ActionListener {

	//{{{ Instance Variables
	private final ProjectViewer viewer;
	private FileFilter nonProjectFileFilter;

	
	private JPopupMenu projectMenu;
	private JMenuItem  properties;
	private JMenuItem  reimport;
	private JMenuItem  removeProject;
	private JMenuItem  addFile;
	private JMenuItem  archiveProject;
	
	private JPopupMenu dirMenu;
	private JMenu	  dirSubRemove;
	private JMenuItem  addFileDir;
	private JMenuItem  removeDir;
	private JMenuItem  deleteDir;
	private JMenuItem  renameDir;
	private JMenuItem  searchDir;
	
	private JPopupMenu fileMenu;
	private JMenu fileMenuSubRemove;
	private JMenuItem  removeFile;
	private JMenuItem  deleteFile;
	

	private JMenuItem  renameFile;
	private JMenuItem  miLaunchBrowser;
	private JMenuItem  miBuildFile;
	private JMenuItem  miExtenalOpen;
	
	private JPopupMenu multipleSelMenu;
	private JMenuItem  removeMulti;
	private JMenuItem  deleteMulti;
  
	private AppLauncher appList;
	//}}}
	
	//{{{ Constructors
	
	/**
	 *  Constructs a listener that will ask the provided viewer instance for
	 *  information about the nodes clicked.
	 */
	public TreeContextMenuListener(ProjectViewer viewer) {
		this.viewer = viewer;
		loadGUI();
		appList = AppLauncher.getInstance();
	}
	
	//}}}
	
	//{{{ Public Methods
	
	//{{{ Event Handling
	
	//{{{ mousePressed() method
	/** Context-menus are shown on the "pressed" event. */
	public void mousePressed(MouseEvent me) {
		JTree tree = (JTree) me.getSource();

		if (SwingUtilities.isRightMouseButton(me)) {
			TreePath tp = tree.getClosestPathForLocation(me.getX(),me.getY());
			if (tp != null && !tree.isPathSelected(tp)) {
				if ((me.getModifiers() & MouseEvent.CTRL_MASK) == MouseEvent.CTRL_MASK) {
					tree.addSelectionPath(tp);
				} else {
					tree.setSelectionPath(tp);
				}
			}
		}

		miLaunchBrowser.setEnabled(this.isURLSet());
		
		if (me.isPopupTrigger()) {
			handleMouseEvent(me);
		}
	} //}}}
	
	//{{{ mouseReleased() method
	/** Context-menus are shown on the "pressed" event. */
	public void mouseReleased(MouseEvent me) {
		if (me.isPopupTrigger()) {
			handleMouseEvent(me);
		}
	} //}}}
	
	//{{{ actionPerformed() method
	/** Listener for actions on the JMenuItems of the popup menus. */
	public void actionPerformed(ActionEvent ae) {
		JMenuItem src = (JMenuItem) ae.getSource();
		
		if (src == properties) {
			ProjectPropertiesDlg.run(viewer,viewer.getCurrentProject());
		} else if (src == reimport) {
			Project p = viewer.getCurrentProject();
			p.removeAllFiles();
			int count = new ProjectFileImporter(viewer).doImport(p.getRoot().toFile());
			viewer.setStatus("Imported " + count + " files into project \"" + p.getName() + "\"."); 		
			new ProjectFileImporter(viewer).doImport(p.getRoot().toFile());
		} else if (src == archiveProject) {
			runArchiveProject();
		
		} else if (src == addFile) {
			this.addFileToProject();
		} else if (src == addFileDir) {
			ProjectDirectory dir = (ProjectDirectory) viewer.getSelectedNode();
			this.addFileToProject(dir.toFile().getPath());
		} else if (src == renameDir) {
			renameDirectory();
		} else if (src == searchDir) {
			searchDir();
		} else if (src == renameFile) {
			renameFile();
		} else if (src == miLaunchBrowser) {
			launchBrowser();
		} else if (src == miBuildFile) {
			setBuildFile();  
		} else if (src == miExtenalOpen) {
			launchExternal(viewer.getSelectedFile());   
		} else if (src == removeProject ||
				   src == removeDir ||
				   src == removeFile ||
				   src == removeMulti) {
			// Removes nodes (from projects, or projects themselves)
			new RemovalManager(viewer).handleSelectionRemoval(false);
		} else if (src == deleteFile ||
				   src == deleteDir ||
				   src == deleteMulti) {
			// Deletes nodes from disk
			// (projects are only removed, nothing is deleted!)
			new RemovalManager(viewer).handleSelectionRemoval(true);
		}
		
	} //}}}
	
	//}}}
	
	//}}}
	
	//{{{ Private Methods
	
	//{{{ setBuildFile() method
	private void setBuildFile() {
		if (viewer.isFileSelected()) {
			ProjectFile buildFile = viewer.getSelectedFile();
			viewer.getCurrentProject().setBuildFile(buildFile.toFile());
		}
	} //}}}

	//{{{ isURLSet() method
	private boolean isURLSet() {
		String sURLRoot = viewer.getCurrentProject().getURLRoot();
		if (sURLRoot.trim() == "" || sURLRoot.equals("http://<projecturl>"))
			return false;
		else 
			return true;
	}
	
	
	//}}}
	//{{{ launchBrowser() method
		private void launchBrowser() {
		/* need to get browser setting */
		String sURLRoot = viewer.getCurrentProject().getURLRoot();
		String sURL;
		String browserExecPath = ProjectViewerConfig.getInstance().getBrowserPath();
		if (!this.isURLSet()) {
			JOptionPane.showMessageDialog(viewer, "Web URL Not set for project");
			return; 
		}
	
		if (viewer.isFileSelected()) {
			ProjectFile fileToView = viewer.getSelectedFile();
			
			/* Produce the url of the file based upon the projects urlRoot */
			sURL = sURLRoot + fileToView.getPath().toString().substring(viewer.getCurrentProject().getRoot().getPath().length());
			
			//JOptionPane.showMessageDialog(viewer, sURL);
			
			Runtime rt = Runtime.getRuntime();
			String[] callAndArgs = { browserExecPath, sURL };
			try {
			   Process child = rt.exec(callAndArgs);
			   child.wait(4);
			   System.out.println("Process exit code is: " + child.exitValue());
			   }
			catch(java.io.IOException e) {
			System.err.println(
			"IOException starting process!");
			}
			catch(InterruptedException e) {
			   System.err.println(
			   "Interrupted waiting for process!");
			}
		} else { 
			JOptionPane.showMessageDialog(viewer, "No File selected");
		}	
	} //}}}
	
	//{{{ launchExternal() method
	/**
	 * Uses the AppLauncher to determine if a custom application found for the selected file type
	 * If an app is found, appLaunch launch the file into the external application
	 *
	 * @param  file  Description of Parameter
	 */
	private void launchExternal(ProjectFile file) {
		appList.launchApp(file.toFile(), viewer);
	} //}}}

	//{{{ handleMouseEvent() method
	/** Handles the mouse event internally. */
	private void handleMouseEvent(MouseEvent me) {
		JTree tree = viewer.getCurrentTree();
		
		if (tree.getSelectionCount() == 0) {
			return;
		} else if (tree.getSelectionCount() > 1) {
			// Multiple selections.
			multipleSelMenu.show(me.getComponent(), me.getX(), me.getY());
		} else {
			// Single selection, detects the node type and shows
			// the corresponding popup menu.
			Object node = viewer.getSelectedNode();
			
			if (node instanceof Project) {
				projectMenu.show(me.getComponent(), me.getX(), me.getY());
			} else if (node instanceof ProjectDirectory) {
				dirMenu.show(me.getComponent(), me.getX(), me.getY());
			} else if (node instanceof ProjectFile) {
				// "Beautify" the "Open with..." entry
				String app = appList.getAppName(((ProjectFile)node).toFile());
				if (app != null) {
					int idx = app.lastIndexOf("/");
					if (idx != -1) {
						app = app.substring(idx + 1, app.length());
					}
					miExtenalOpen.setText("Open with \"" + app + "\"");
				} else {
					miExtenalOpen.setText("Open with...");
				}
				// show the menu
				fileMenu.show(me.getComponent(), me.getX(), me.getY());
			}
		}
	} //}}}
	
	//{{{ loadGUI() method
	/** Constructs the menus' GUI. */
	private void loadGUI() {
		JMenuItem tmp;
		Font font = new Font("Arial", Font.ITALIC, 10);
		
		// Project menu
		projectMenu = new JPopupMenu();
		tmp = new JMenuItem("Selected project");
		tmp.setEnabled(false);
		tmp.setFont(font);
		projectMenu.add(tmp);
		projectMenu.addSeparator();
		
		
		addFile = new JMenuItem("Add File");
		addFile.addActionListener(this);
		projectMenu.add(addFile);
		projectMenu.addSeparator();
		
		properties = new JMenuItem("Properties");
		properties.addActionListener(this);
		projectMenu.add(properties);
		
		reimport = new JMenuItem("Re-Import Files");
		reimport.addActionListener(this);
		projectMenu.add(reimport);
		
		removeProject = new JMenuItem("Remove project");
		removeProject.addActionListener(this);
		projectMenu.add(removeProject);

		projectMenu.addSeparator();
		archiveProject = new JMenuItem("Archive Project");
		archiveProject.addActionListener(this);
		projectMenu.add(archiveProject);		
		
		// Directory menu
		dirMenu = new JPopupMenu();
		tmp = new JMenuItem("Selected directory");
		tmp.setEnabled(false);
		tmp.setFont(font);
		dirMenu.add(tmp);
		dirMenu.addSeparator();
        addFileDir = new JMenuItem("Add File");
		addFileDir.addActionListener(this);
		dirMenu.add(addFileDir);
		
		renameDir = new JMenuItem("Rename");
		renameDir.addActionListener(this);
		dirMenu.add(renameDir);

		dirSubRemove = new JMenu("Delete from");
		
		removeDir = new JMenuItem("Project");
		removeDir.addActionListener(this);
		dirSubRemove.add(removeDir);
		
		deleteDir = new JMenuItem("Disk (and project)");
		deleteDir.addActionListener(this);
		dirSubRemove.add(deleteDir);
		dirMenu.add(dirSubRemove);
		searchDir = new JMenuItem("Search in Directory");
		searchDir.addActionListener(this);
		dirMenu.addSeparator();
		dirMenu.add(searchDir);
		
		// File menu
		fileMenu = new JPopupMenu();
		tmp = new JMenuItem("Selected file");
		tmp.setEnabled(false);
		tmp.setFont(font);
		fileMenu.add(tmp);
		fileMenu.addSeparator();
		
		// sutter2k: need to tap in here for preview in browser
		miLaunchBrowser= new JMenuItem("Preview in Browser");
		miLaunchBrowser.addActionListener(this);
		
		fileMenu.add(miLaunchBrowser);
		fileMenu.addSeparator();
		
		renameFile = new JMenuItem("Rename");
		renameFile.addActionListener(this);
		fileMenu.add(renameFile);
		
		fileMenuSubRemove = new JMenu("Delete from");
		removeFile = new JMenuItem("Project");
		removeFile.addActionListener(this);
		fileMenuSubRemove.add(removeFile);
		//fileMenu.add(removeFile);
		
		
		deleteFile = new JMenuItem("Disk (and project)");
		deleteFile.addActionListener(this);
		fileMenuSubRemove.add(deleteFile);
		//fileMenu.add(deleteFile);
		fileMenu.add(fileMenuSubRemove);
		
		fileMenu.addSeparator();
	   
		
		// danson, added for build file selection
		miBuildFile = new JMenuItem("Set as Build File");
		miBuildFile.addActionListener(this);
		fileMenu.add(miBuildFile);
	
		// sutter2k: added for opening/running files with external applications
		miExtenalOpen = new JMenuItem("Open With (xxx)");
		miExtenalOpen.addActionListener(this);
		fileMenu.add(miExtenalOpen);
   
   
		// Menu to show when multiple nodes are selected
		multipleSelMenu = new JPopupMenu();
		tmp = new JMenuItem("Multiple selection");
		tmp.setEnabled(false);
		tmp.setFont(font);
		multipleSelMenu.add(tmp);
		multipleSelMenu.addSeparator();
		
		removeMulti = new JMenuItem("Remove from project");
		removeMulti.addActionListener(this);
		multipleSelMenu.add(removeMulti);
		
		deleteMulti = new JMenuItem("Delete from disk");
		deleteMulti.addActionListener(this);
		multipleSelMenu.add(deleteMulti);
		
	} //}}}
		

	private void addFileToProject() {
		addFileToProject(viewer.getCurrentProject().getRoot().getPath());
	
	}
	
	private void runArchiveProject () {

		javax.swing.JFileChooser chooser = viewer.createFileChooser();
		if(nonProjectFileFilter == null) {
			nonProjectFileFilter =
				new FileFilter() {
					public String getDescription() {
						return "Archive File Types";
					}

					public boolean accept(File f) {
						String name = f.getName();
						if (name.endsWith(".zip")) return true;
    					else if (name.endsWith(".jar")) return true;
    					else if (name.endsWith(".war")) return true;
    					else if (name.endsWith(".ear")) return true;
						else if (f.isDirectory()) return true;
    					return false;
					}
				};
		}
		
		chooser.setFileFilter(nonProjectFileFilter);
		chooser.setCurrentDirectory(new File (viewer.getCurrentProject().getRoot().getPath())); 
		
 	   if((chooser.showSaveDialog(this.viewer) != javax.swing.JFileChooser.APPROVE_OPTION)) {
			return;
		}

	    projectviewer.archiver.ProjectZipper pz = new projectviewer.archiver.ProjectZipper();
		pz.createProjectAchive(new File (chooser.getSelectedFile().getAbsolutePath()), 	
								viewer.getCurrentProject().projectFiles(),
								viewer.getCurrentProject().getRoot().getPath()
								); 
	
		viewer.getCurrentProject().importFile(
				new ProjectFile(chooser.getSelectedFile().getAbsolutePath()));
		
	}
	
	//{{{ addFileToProject() method
	/** Prompt the user to a file, get the current project, and then add the file
	 *  to the project.
	 */
	private void addFileToProject(String dirPath) {
		javax.swing.JFileChooser chooser = viewer.createFileChooser();
		if(nonProjectFileFilter == null) {
			nonProjectFileFilter =
				new FileFilter() {
					public String getDescription() {
						return "Non Project Files";
					}

					public boolean accept(File f) {
						return !viewer.getCurrentProject().isProjectFile(f.getAbsolutePath());
					}
				};
		}
		
		chooser.setFileFilter(nonProjectFileFilter);
		chooser.setCurrentDirectory(new File(dirPath)); 

 	   if(chooser.showOpenDialog(this.viewer) != javax.swing.JFileChooser.APPROVE_OPTION) {
			return;
		}

		viewer.getCurrentProject().importFile(
				new ProjectFile(chooser.getSelectedFile().getAbsolutePath()));

		if(ProjectViewerConfig.getInstance().getSaveOnChange()) {
			viewer.getCurrentProject().save();
		}

	}//}}}
	
	//{{{ renameFile() method
	/** 
	 *  Renames the currently selected file in the viewer, both in the project
	 *  view and on disk. To update the view, the file is removed from the
	 *  project, and a new ProjectFile is added.
	 */
	private void renameFile() {
		ProjectFile file = (ProjectFile) viewer.getSelectedNode();
		String newName = (String)
		
			JOptionPane.showInputDialog(
				viewer,
				"Enter the new name of the file:",
				"Rename file",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				file.toFile().getName()
			);
			
		if (newName != null) {
			Project p = viewer.getCurrentProject();
			
			File oldFile = file.toFile();

			Buffer b = jEdit.getBuffer(oldFile.getAbsolutePath());
			if (b != null) {
				jEdit.closeBuffer(viewer.getView(),b);
			}
			
			File newFile = new File(oldFile.getParent() + File.separator + newName);
			if (!oldFile.renameTo(newFile)) {
				JOptionPane.showMessageDialog(
					viewer,
					"Could not rename selected file!",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			}
			
			if (b != null) {
				jEdit.openFile(viewer.getView(),newFile.getAbsolutePath());
			}
			
			p.removeFile(file);
			p.importFile(new ProjectFile(newFile.getAbsolutePath()));
		}
	} //}}}
	
	//{{{ renameDirectory() method
	/**
	 *  Renames a directory in a project. All files below the modified directory
	 *  are also modified to point to the new directory name.
	 */
	private void renameDirectory() {
		ProjectDirectory dir = (ProjectDirectory) viewer.getSelectedNode();
		String oldName = dir.toFile().getName();
		String newName = (String)
			JOptionPane.showInputDialog(
				viewer,
				"Enter the new name of the directory:",
				"Rename directory",
				JOptionPane.PLAIN_MESSAGE,
				null,
				null,
				oldName
			);
			
		if (newName != null && !oldName.equals(newName)) {
			if (!dir.changeName(newName)) {
				JOptionPane.showMessageDialog(
					viewer,
					"Could not rename selected file!",
					"Error",
					JOptionPane.ERROR_MESSAGE
				);
			} else {
				viewer.refresh();
			}
		}
	
	} //}}}
	
	//{{{ searchDir() method
	/** Popups up a Search and Replace dialog for the currently selected directory.
	 */
	private void searchDir() {
		ProjectDirectory dir = (ProjectDirectory) viewer.getSelectedNode();
		//-- version 1
		//org.gjt.sp.jedit.browser.VFSBrowser browser =(org.gjt.sp.jedit.browser.VFSBrowser) jEdit.getLastView().getDockableWindowManager().getDockable("vfs.browser");
		//browser.setDirectory(dir.getPath());
		//browser.searchInDirectory();
		//-- version 2
		/** @todo develop an own class which implements 'org.gjt.sp.jedit.search.SearchFileSet'
		 *  and returns the proper files
		 */
		//SearchAndReplace.setSearchFileSet(new DirectoryListSet(dir.getPath(),"*",true));
		SearchAndReplace.setSearchFileSet(new ProjectFileSet(dir));
		SearchDialog.showSearchDialog(jEdit.getLastView(), null, SearchDialog.DIRECTORY);
	}//}}}
	//}}}
}
