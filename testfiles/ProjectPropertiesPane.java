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
import java.awt.BorderLayout;
import java.awt.Point;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.io.VFSManager;

import projectviewer.ProjectManager;
import projectviewer.VFSHelper;

import projectviewer.gui.GroupMenu;
import projectviewer.gui.OptionPaneBase;

import projectviewer.vpt.VPTGroup;
import projectviewer.vpt.VPTProject;
import projectviewer.vpt.VPTRoot;
//}}}

/**
 *  A dialog for configuring the properties of a project.
 *
 *  @author		Marcelo Vanzin
 *  @author		Matt Payne (made slight changes for urlRoot)
 */
class ProjectPropertiesPane extends OptionPaneBase
							implements ActionListener,
									   ItemListener
{

	public final static String DEFAULT_URL = "http://";

	//{{{ Instance Variables

	private int result;
	private VPTProject project;

	private JTextField projName;
	private JTextField projRoot;
	private JTextField projURLRoot;

	private JButton	chooseRoot;
	private JButton chooseGroup;
	private JComboBox vcServices;
	private JPopupMenu groupPopupMenu;

	private ProjectOptions options;
	private String currVC;

	private boolean ok;
	private boolean isNew;

	//}}}

	//{{{ Constructors

	ProjectPropertiesPane(ProjectOptions options,
						  VPTProject p,
						  boolean isNew)
	{
		super("projectviewer.project_props",
			  "projectviewer.project.options");
		this.options = options;
		this.project = p;
		this.ok = true;
		this.isNew = isNew;
	}

	//}}}

	//{{{ actionPerformed(ActionEvent) method
	/**
	 *  Shows a file chooser so the user can choose the root directory of
	 *  its project. In case the user chooses a directory, the corresponding
	 *  JTextField is updated to show the selection.
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == chooseRoot) {
			String path;
			String root = projRoot.getText().trim();
			VFSFileChooserDialog chooser;

			if (root.length() > 0) {
				path = VFSManager.getVFSForPath(root).getParentOfPath(root);
			} else {
				path = jEdit.getProperty("projectviewer.filechooser.directory",
										 System.getProperty("user.home"));
			}

			chooser = new VFSFileChooserDialog(GUIUtilities.getParentDialog(this),
											   jEdit.getActiveView(),
											   path,
											   VFSBrowser.CHOOSE_DIRECTORY_DIALOG,
											   false,
											   false);


			chooser.setTitle(prop("root_dialog"));
			chooser.setVisible(true);

			if (chooser.getSelectedFiles() != null) {
				path = chooser.getSelectedFiles()[0];
				jEdit.setProperty("projectviewer.filechooser.directory", path);
				projRoot.setText(path);
				projRoot.setToolTipText(projRoot.getText());

				if (projName.getText() != null && projName.getText().length() == 0) {
					String name = VFSManager.getVFSForPath(path)
					                        .getFileName(path);
					projName.setText(name);
				}
			}
		} else if (ae.getSource() == chooseGroup) {
			if (groupPopupMenu == null) {
				GroupMenu gm = new GroupMenu(null, false, false, this);
				groupPopupMenu = new JPopupMenu();
				gm.populate(groupPopupMenu, VPTRoot.getInstance(), jEdit.getActiveView());
			}

			Point p = chooseGroup.getLocation();
			groupPopupMenu.show(this, (int) p.getX(), (int) p.getY() + chooseGroup.getHeight());
		} else if (ae.getSource() instanceof VPTGroup) {
			project.setParent((VPTGroup) ae.getSource());
			chooseGroup.setText(((VPTGroup) ae.getSource()).getName());
			groupPopupMenu.setVisible(false);
		}

	} //}}}


	public void itemStateChanged(ItemEvent e)
	{
		VersionControlService svc = null;
		String svcname = (String) e.getItem();

		if (!svcname.equals(prop("vc_none"))) {
			svc = (VersionControlService)
				ServiceManager.getService(VersionControlService.class.getName(),
										  svcname);
		}

		if (svc != null) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				options.addOptions(svc);
			} else if (e.getStateChange() == ItemEvent.DESELECTED) {
				options.removeOptions(svc);
			}
		}
	}


	//{{{ _save() method
	/** Updates the project with the info supplied by the user. */
	protected void _save() {
		String name = projName.getText().trim();
		ok = true;

		if (name.length() == 0) {
			JOptionPane.showMessageDialog(
				this,
				prop("no_name"),
				prop("error.title"),
				JOptionPane.ERROR_MESSAGE
			 );
			 ok = false;
		}

		if (isNew && ProjectManager.getInstance().hasProject(name)) {
			JOptionPane.showMessageDialog(
				this,
				prop("name_exists"),
				prop("error.title"),
				JOptionPane.ERROR_MESSAGE
			 );
			 ok = false;
		}

		String root = projRoot.getText().trim();
		if (root.length() == 0) {
			JOptionPane.showMessageDialog(
				this,
				prop("no_root"),
				prop("error.title"),
				JOptionPane.ERROR_MESSAGE
			 );
			 ok = false;
		} else if (!VFSHelper.pathExists(root)) {
			JOptionPane.showMessageDialog(
				this,
				prop("root_error"),
				prop("error.title"),
				JOptionPane.ERROR_MESSAGE
			 );
			 ok = false;
		}

		String urlRoot = projURLRoot.getText().trim();

		if (ok) {
			project.setName(name);
			project.setRootPath(root);
			if (urlRoot.length() != 0 && !urlRoot.equals(DEFAULT_URL)) {
				project.setURL(urlRoot);
			} else {
				project.setURL(null);
			}

			String vcsvc = (String) vcServices.getSelectedItem();
			if (vcServices.getSelectedIndex() > 0) {
				project.setProperty(VersionControlService.VC_SERVICE_KEY,
									vcsvc);
			} else {
				project.removeProperty(VersionControlService.VC_SERVICE_KEY);
			}
			if (currVC != null && !currVC.equals(vcsvc)) {
				VersionControlService svc;
				svc = (VersionControlService)
					ServiceManager.getService(VersionControlService.class.getName(),
											  currVC);
				if (svc != null) {
					svc.dissociate(project);
				}
			}
		}
	} //}}}

	//{{{ _init() method
	/** Load the GUI components of the dialog. */
	protected void _init() {
		String tmp;

		// Project name
		projName = new JTextField(project.getName());
		addComponent(projName, "name");

		// Project root
		JPanel rootPane = new JPanel(new BorderLayout());

		projRoot = new JTextField(project.getRootPath());
		projRoot.setToolTipText(projRoot.getText());
		rootPane.add(BorderLayout.CENTER, projRoot);

		chooseRoot = new JButton(prop("root_choose"));
		chooseRoot.addActionListener(this);
		rootPane.add(BorderLayout.EAST, chooseRoot);
		addComponent(rootPane, "root");

		// URL Root for web projects.  Used to launch files in web browser against webserver
		projURLRoot = new JTextField((project.getURL() != null) ? project.getURL()
								                                : DEFAULT_URL);
		addComponent(projURLRoot, "url_root");

		// The group where the project will be attached
		VPTGroup parent = (VPTGroup) project.getParent();
		if (parent == null) {
			parent = VPTRoot.getInstance();
		}
		chooseGroup = new JButton(parent.getName());
		chooseGroup.addActionListener(this);
		addComponent(chooseGroup, "parent_group");

		// Version control services
		int selidx = 0;
		ProjectViewerConfig config = ProjectViewerConfig.getInstance();
		String[] vcs = ServiceManager.getServiceNames(VersionControlService.class.getName());
		currVC = project.getProperty(VersionControlService.VC_SERVICE_KEY);
		vcServices = new JComboBox();
		vcServices.addItem(prop("vc_none"));

		for (String vc : vcs) {
			if (config.isExtensionEnabled(VersionControlService.class.getName(), vc)) {
				vcServices.addItem(vc);
				if (currVC != null && vc.equals(currVC)) {
					selidx = vcServices.getItemCount() - 1;
				}
			}
		}

		if (vcServices.getItemCount() > 1) {
			vcServices.addItemListener(this);
			vcServices.setSelectedIndex(selidx);
			addComponent(vcServices, "vc_service");
		}

	} //}}}

	//{{{ isOK() method
	protected boolean isOK() { return ok; } //}}}

}

