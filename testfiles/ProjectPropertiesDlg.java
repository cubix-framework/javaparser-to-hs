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
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 
package projectviewer.config;

//{{{ Imports
// Import Java
import java.io.File;

// Import AWT/Swing
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.WindowConstants;

// Import jEdit
import org.gjt.sp.util.Log;

import projectviewer.Project;
import projectviewer.ProjectViewer;
import projectviewer.ProjectManager;
import projectviewer.ProjectDirectory;
//}}}

/**
 *  A dialog for configuring the properties of a project.
 *
 *  @author	 Marcelo Vanzin
 *  "	 "	 Matt Payne (made slight changes for urlRoot
 */
public class ProjectPropertiesDlg extends JDialog implements ActionListener {

	//{{{ Static Methods & Variables
	
	private final static int ERROR  = -1;
	public final static int CANCEL = 0;
	public final static int OK	 = 1;
	
	private static ProjectPropertiesDlg instance;
	
	//{{{ run() method
	/**
	 *  <p>Shows the dialog to edit the properties of the provided
	 *  project. If the project is <i>null</i>, creates a new one,
	 *  unless the user hits "Cancel" (then <i>null</i> is returned).</p>
	 *
	 *  <p>If "refresh" is true, and the project is modified, then the
	 *  viewer instance passed is refreshed.</p>
	 *
	 *  @param  viewer  The current ProjectViewer instance.
	 *  @param  proj	The project to be edited, or null to create a new one.
	 *  @param  refresh If the viewer should be refreshed after modifying the project.
	 */
	public static Project run(ProjectViewer owner, Project proj) {
		ProjectPropertiesDlg dialog = new ProjectPropertiesDlg(owner);
		dialog.setProject(proj);
		dialog.setLocationRelativeTo(owner);
		dialog.show();
		
		if (dialog.getResult() == OK) {
			ProjectManager.getInstance().sortProjectList();
			owner.refresh();
			
			if (ProjectViewerConfig.getInstance().getSaveOnChange()) {
			 Project currProj = owner.getCurrentProject();	
			// Matthew Payne: must test for null ie can't do this if there is no current project
			if (currProj != null)
					currProj.save();
			}
		}
		
		return dialog.getProject();
	} //}}}
	
	//}}}
	
	//{{{ Instance Variables
	
	private int result;
	private Project project;
	
	private JTextField projName;
	private JTextField projRoot;
	private JTextField projURLRoot;
	
	private JButton	chooseRoot;
	private JButton	updateProject;
	private JButton	cancel;

	//}}}
	
	//{{{ Constructors
	
	/** Builds the dialog. */
	private ProjectPropertiesDlg(ProjectViewer owner) {
		super(JOptionPane.getFrameForComponent(owner));
		loadGUI();
		setModal(true);
	}
	
	//}}}
	
	//{{{ Public Methods
	
	public void actionPerformed(ActionEvent e) {
		JButton jb = (JButton) e.getSource();
		
		if (jb == chooseRoot) {
			chooseRoot();
		} else if (jb == updateProject) {
			result = updateProject();
			if (result == OK) {
				hide();
			}
		} else if (jb == cancel) {
			result = CANCEL;
			hide();
		}
		
	}
	
	//}}}
	
	//{{{ Private Methods

	//{{{ getResult() method
	private int getResult() {
		return result;
	} //}}}

	//{{{ getProject() method	
	private Project getProject() {
		return project;
	} //}}}
	
	//{{{ setProject() method
	private void setProject(Project p) {
		this.project = p;
		
		if (p != null) {
			projName.setText(p.getName());
			projRoot.setText(p.getRoot().getPath());
			projRoot.setToolTipText(projRoot.getText());
			projURLRoot.setText(p.getURLRoot());
			setTitle("Edit project: " + p.getName());
		} else {
			projName.setText("");
			projRoot.setText("");
			projRoot.setToolTipText(projRoot.getText());
			projURLRoot.setText("http://<projecturl>");
		
			setTitle("Create new project");
		}
	} //}}}
	
	//{{{ chooseRoot() method
	/**
	 *  Shows a file chooser so the user can choose the root directory of
	 *  its project. In case the user chooses a directory, the corresponding
	 *  JTextField is updated to show the selection.
	 */
	private void chooseRoot() {	
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Enter the root directory for the project:");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		String root = projRoot.getText().trim();
		if (root.length() > 0) {
		   chooser.setSelectedFile(new File(root));
		}
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			root = chooser.getSelectedFile().getAbsolutePath();
			if (project != null) {
				String oldRoot = project.getRoot().getPath();
				/* 
				* Matthew Payne = if old Root is "" then allow change is well
				* re: "&& (oldRoot.trim().length() > 0)"
				*/
				if ( !(oldRoot.startsWith(root) &&
					   root.length() < oldRoot.length()) && (oldRoot.trim().length() > 0) ) { 
					project.changeRoot(new ProjectDirectory(root));
					JOptionPane.showMessageDialog(
						this, 
						"Changing to a root that isn't parent of the previous root " +
						"is not supported.",
						"Error: unsupported change",
						JOptionPane.ERROR_MESSAGE
					);
					return;
				
					   }
			}
				
			projRoot.setText(root);
			projRoot.setToolTipText(projRoot.getText());
		}

	} //}}}
	
	//{{{ updateProject() method
	/** Updates the project with the info supplied by the user. */
	private int updateProject() {
		String name = projName.getText().trim();
		
		if (name.length() == 0) {
			JOptionPane.showMessageDialog(
				this, 
				"Please specify a name for the project.",
				"Error: no name supplied",
				JOptionPane.ERROR_MESSAGE
			 );
			 return ERROR;
		} 
		
		String root = projRoot.getText().trim();
		if (root.length() == 0) {
			JOptionPane.showMessageDialog(
				this, 
				"Please specify a root for the project.",
				"Error: no root supplied",
				JOptionPane.ERROR_MESSAGE
			 );
			 return ERROR;
		} 
		
		String urlRoot = projURLRoot.getText().trim();
	
		if (project == null) { 
			project = new Project();
			project.setName(name);
			project.setRoot(new ProjectDirectory(root));
			project.setURLRoot(urlRoot);
		} else {
			project.setName(name);
			project.setURLRoot(urlRoot);
			project.changeRoot(new ProjectDirectory(root));
		}
		
		return OK;
	} //}}}
	
	//{{{ loadGUI() method
	/** Load the GUI components of the dialog. */
	private void loadGUI() {
		
		// Ignores the close window button
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		
		// Builds the dialog
		
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(3,3,3,3);
		getContentPane().setLayout(gridbag);
		
		// Project name
		
		JLabel label = new JLabel("Project name:");
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gridbag.setConstraints(label,gc);
		getContentPane().add(label);
		
		projName = new JTextField();
		gc.weightx = 1;
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridwidth = 2;
		gridbag.setConstraints(projName,gc);
		getContentPane().add(projName);
		
		// Project root
		label = new JLabel("Root directory:");
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gridbag.setConstraints(label,gc);
		getContentPane().add(label);

		projRoot = new JTextField();
		projRoot.setEnabled(false);
		projRoot.setPreferredSize(
			new Dimension(50, (int)projRoot.getPreferredSize().getHeight())
		);
		
		gc.weightx = 1;
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gridbag.setConstraints(projRoot,gc);
		getContentPane().add(projRoot);
	
	
		chooseRoot = new JButton("Choose");   
		chooseRoot.addActionListener(this);
		gc.weightx = 0;
		gc.gridx = 2;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gridbag.setConstraints(chooseRoot,gc);
		getContentPane().add(chooseRoot);

		// URL Root for web projects.  Used to launch files in web browser against webserver
	
		label = new JLabel("Web URL Root(optional)");
		label.setToolTipText("sets the URL for a web project e.g. http://<projecturl>");
		gc.weightx = 0;
		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = 1;
		gridbag.setConstraints(label, gc);
	
		getContentPane().add(label);
		projURLRoot = new JTextField();
		projURLRoot.setToolTipText("http://<projecturl>");
	
		gc.weightx = 1;
		gc.gridx = 1;
		gc.gridy = 2;
		gc.gridwidth = 2;
		gridbag.setConstraints(projURLRoot, gc);
		getContentPane().add(projURLRoot);

		// Lower buttons
		
		JPanel panel = new JPanel();
		gc.weightx = 1;
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 3;
		gridbag.setConstraints(panel,gc);
		getContentPane().add(panel);
		
		updateProject = new JButton("OK");
		updateProject.addActionListener(this);
		panel.add(updateProject);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		panel.add(cancel);
		
		updateProject.setPreferredSize(cancel.getPreferredSize());
   
		// Finishing
		setResizable(false);
		setSize(new Dimension(350,180));
	} //}}}
	
	//}}}
	
}

