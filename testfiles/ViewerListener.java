/*
 * $Id: ViewerListener.java 5878 2002-10-25 22:16:32Z vanza $
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

//standard GUI stuff
import java.awt.Cursor;
import java.awt.event.*;
import java.io.File;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.*;

import org.gjt.sp.util.Log;
import projectviewer.config.ProjectPropertiesDlg;
import projectviewer.config.ProjectViewerConfig;

/** Listen to all buttons and GUI events and respond to them. */
public final class ViewerListener implements ActionListener, ItemListener, Runnable {

	private ProjectViewer viewer;
	private Launcher launcher;
	private boolean paused;
	private FileFilter nonProjectFileFilter;
	
	int lastSelectedIndex;


	public ViewerListener(ProjectViewer instance, Launcher launcher) {
		this.viewer = instance;
		this.launcher = launcher;
		paused = false;
	}

	/** Pause this listener. Any events received will be ignored. */
	public void pause() {
		paused = true;
	}

	/** Resume this listener. Any events received will not be handled. */
	public void resume() {
		paused = false;
	}

	/** Listen to specific GUI events.
	 *
	 * @param  evt  Description of Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
		if(paused) {
			return;
		}

		viewer.setStatus(" ");
		Object source = evt.getSource();

		if(source == this.viewer.createProjectBtn) {
			this.createProject();
		}
		else if(source == this.viewer.addFileBtn) {
			this.addFileToProject();
		}
		else if(source == this.viewer.removeFileBtn) {
			new RemovalManager(viewer).handleSelectionRemoval(false);
		}
		else if(source == this.viewer.removeAllFilesBtn) {
			this.removeAllFilesFromProject();
		}
		else if(source == this.viewer.openAllBtn) {
			this.openAllFilesInProject();
		}
		else if(source == this.viewer.expandBtn) {
			viewer.expandAll();
		}
		else if(source == this.viewer.contractBtn) {
			viewer.collapseAll();
		}
		else if(source == this.viewer.launchBrowserBtn) {
			// this.showConfig(); // will need to be commented out
			this.launchBrowser();
		}

	}

	/** Handle project combo changes.
	 *
	 * @param  evt  Description of Parameter
	 */
	public void itemStateChanged(ItemEvent evt) {
		if(paused || evt.getStateChange() == ItemEvent.DESELECTED) return;
		
		if(evt.getItem() instanceof Project) {
			viewer.setCurrentProject((Project) evt.getItem());
		} else {
			if(evt.getItem().toString().equals(ProjectViewer.CREATE_NEW_PROJECT)) {
				SwingUtilities.invokeLater(this);
			} else {
				viewer.setCurrentProject(null);
			}
		}
	}

	/** Returns an instance of {@link ProjectFilesImporter}.
	 *
	 * @return    The importer value
	 */
	private ProjectFileImporter getImporter() {
		return new ProjectFileImporter(viewer);
	}


	/** Launched the selected project file in the web-browser, against the webserver.
	 * browser property must be set for jedit & urlRoot must be set for the given project
	 */
	private void launchBrowser() {
		/*
		 * need to get browser setting
		 */
		String sURLRoot = viewer.getCurrentProject().getURLRoot();
		String sURL;
		String browserExecPath = ProjectViewerConfig.getInstance().getBrowserPath();
		if(sURLRoot == "") {
			JOptionPane.showMessageDialog(viewer, "Web URL Not set for project");
			return;
		}

		if(viewer.isFileSelected()) {
			ProjectFile fileToView = viewer.getSelectedFile();

			/*
			 * Produce the url of the file based upon the projects urlRoot
			 */
			sURL = sURLRoot + fileToView.getPath().toString().substring(viewer.getCurrentProject().getRoot().getPath().length());
			//sURL = sURLRoot + fileToView.getPath();
			JOptionPane.showMessageDialog(viewer, sURL);

			Runtime rt = Runtime.getRuntime();
			String[] callAndArgs = {browserExecPath, sURL};
			try {
				Process child = rt.exec(callAndArgs);
				child.wait(4);
				System.out.println("Process exit code is: " + child.exitValue());
			}
			catch(IOException e) {
				System.err.println("IOException starting process!");
			}
			catch(InterruptedException e) {
				System.err.println("Interrupted waiting for process!");
			}
		}
		else {
			JOptionPane.showMessageDialog(viewer, "No File selected");
		}
	}

	/**
	 *	"Comestic" hack to let the combo box close before showing the 
	 *	"new project" dialog.
	 */
	public void run() {
		createProject();
	}
	
	/** Create a new Project */
	private void createProject() {
		Project project = ProjectPropertiesDlg.run(viewer, null);
		if(project == null) {
			viewer.projectCombo.setSelectedIndex(lastSelectedIndex);
			return;
		}

		if(ProjectManager.getInstance().hasProject(project.getName())) {
			JOptionPane.showMessageDialog(viewer, "There is currently a project with this name.");
			viewer.projectCombo.setSelectedIndex(lastSelectedIndex);
			return;
		}

		project.setLoaded(true);
		ProjectManager.getInstance().addProject(project);
		viewer.setCurrentProject(project);

		File prjHome = project.getRoot().toFile();
		int confirmed = JOptionPane.showConfirmDialog(this.viewer,
				"Do you want to import files from " + prjHome + "?",
				"Import files?",
				JOptionPane.YES_NO_OPTION);
		viewer.showWaitCursor();
		if(confirmed == JOptionPane.YES_OPTION) {
			getImporter().doImport(prjHome);
		}

		viewer.refresh();
		viewer.showDefaultCursor();
	}

	/** Prompt the user to a file, get the current project, and then add the file
	 *  to the project.
	 */
	private void addFileToProject() {
		getImporter().doImport();
	}

	/** Prompt the user if he want to remove all file from a projects. */
	private void removeAllFilesFromProject() {
		int answer = JOptionPane.showConfirmDialog(viewer,
				"Are you sure you want to remove all files from the current project?",
				"Remove all files?",
				JOptionPane.YES_NO_OPTION);

		if(answer == JOptionPane.YES_OPTION) {
			viewer.getCurrentProject().removeAllFiles();
		}
	}

	/** Progmatically open all files under the current project... */
	private void openAllFilesInProject() {
		viewer.showDefaultCursor();
		for(Iterator i = viewer.getCurrentProject().projectFiles(); i.hasNext(); ) {
			launcher.launchFile((ProjectFile)i.next());
		}
		viewer.showDefaultCursor();
	}
}

