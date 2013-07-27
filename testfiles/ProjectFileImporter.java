/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
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

//{{{ Imports
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.gjt.sp.util.Log;

import projectviewer.config.ProjectViewerConfig;
import projectviewer.filters.*;
//}}}

/** 
 *	Imports files into projects.
 *	
 *	@version	$Id: ProjectFileImporter.java 5893 2002-11-07 10:25:31Z ensonic $
 */
public final class ProjectFileImporter {

	//{{{ Private variables
	private ProjectViewer viewer;
	private Project project;
	//}}}

	//{{{ Constructors
	
	/** 
	 *	Constructs a new file importer for the given instance of the
	 *	ProjectViewer. The current project will be used as the target
	 *	of the imports.
	 *
	 *	@param  aViewer  Description of Parameter
	 */
	public ProjectFileImporter(ProjectViewer aViewer) {
		viewer = aViewer;
		project = viewer.getCurrentProject();
	}
	
	//}}}

	//{{{ doImport() method
	/** 
	 *	Import project files. This method will show a file chooser from where
	 *	the user will choose files and/or directories to include in his project.
	 *	Files will be added and directories will be traversed, looking for files.
	 */
	public void doImport() {
		JFileChooser chooser = viewer.createFileChooser();
		chooser.setFileFilter(
			new javax.swing.filechooser.FileFilter() {
				public String getDescription() {
					return "Non Project Files";
				}

				public boolean accept(File f) {
					return f.isDirectory() || !project.isProjectFile(f.getAbsolutePath());
				}
			}
		);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);
		chooser.setAcceptAllFileFilterUsed(false);
		if(chooser.showOpenDialog(this.viewer) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File[] chosen = chooser.getSelectedFiles();
		if (chosen != null && chosen.length > 0) {
			File f = chosen[0];
			String pRoot = project.getRoot().getPath();
			if (!f.getAbsolutePath().startsWith(pRoot)) {
				JOptionPane.showMessageDialog(viewer,
						"The selected files should be below the project root\"" + pRoot + "\".",
						"Note",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			doImport(chosen);
		}
		
		if(ProjectViewerConfig.getInstance().getSaveOnChange()) {
			viewer.getCurrentProject().save();
		}
	} //}}}

	//{{{ doImport(File[]) method
	/** 
	 *	Import a list of files into the project.
	 *
	 *	@param  directory  Description of Parameter
	 */
	public void doImport(File[] files) {
		int count = 0;
		// TODO: Perform import in background ?
		for (int i = 0; i < files.length; i++) {
			count += doImport(files[i]);
		}
		viewer.setStatus("Imported " + count + " files into project \"" + project.getName() + "\"."); 
	} //}}}
	
	//{{{ doImport(File) method
	/**
	 *	Import a file into the project. If the file is a directory, traverse
	 *	the directory looking for files that match the import properties
	 *	defined by the user.
	 */
	public int doImport(File f) {
		if (f.isDirectory()) {
			List files = new ArrayList();
			buildFileList(f, files);
			if (files.size() > 0) {
				project.importFiles(files);
			}
			return files.size();
		} else {
			project.importFile(new ProjectFile(f.getAbsolutePath()));
			return 1;
		}
	} //}}}

	//{{{ getFilter() method
	/** 
	 *	Returns a filter that uses the import properties defined by the user to
	 *	filter files.
	 *
	 *	@return	 The filter instance
	 *
	 *  @todo have different filter implementations (e.g. for CVS/Entries files)
	 */
	private FileFilter getFilter() {
		//return new CVSFilter();
		// the default filter
		return new PatternFilter();
	}//}}}

	//{{{ buildFileList(File,List) method
	/** 
	 *	Traverse the directory looking for files, and add those files to
	 *	the given list.
	 *
	 *	@param  directory	The directory to search.
	 *	@param  files		Where to store the files found.
	 */
	private void buildFileList(File directory, List files) {
		File[] fileArray = directory.listFiles(getFilter());

		for (int i = 0; i < fileArray.length; ++i) {
			if (fileArray[i].isDirectory()) {
				buildFileList(fileArray[i], files);
			}
			else {
				String path = fileArray[i].getAbsolutePath();
				if (!viewer.getCurrentProject().isProjectFile(path))
					files.add(new ProjectFile(path));
			}
		}
	} //}}}
}

