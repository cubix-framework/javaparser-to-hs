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
package projectviewer.action;

//{{{ Imports
import java.awt.event.ActionEvent;

import java.io.File;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTProject;
import projectviewer.config.ProjectViewerConfig;

import infoviewer.InfoViewerPlugin;
//}}}

/**
 *	Opens the selected file in the configured web-browser. Files that are under
 *	the project root are treated differently: the project's URL root is used
 *	to build the URL. For other files, or if the projects URL is not set, the
 *	absolute path to the file is given to the browser.
 *
 *	@author		Marcelo Vanzin (based on code from Matthew Payne)
 *	@version	$Id: LaunchBrowserAction.java 6344 2005-05-16 07:25:48Z vanza $
 */
public class LaunchBrowserAction extends Action {

	//{{{ Private members
	private VPTFile file;
	//}}}

	//{{{ +LaunchBrowserAction() : <init>
	/** Default constructor. */
	public LaunchBrowserAction() {
		this(null);
	} //}}}

	//{{{ +LaunchBrowserAction(VPTFile) : <init>
	/**
	 *	Instantiates a launcher for the given file.
	 */
	public LaunchBrowserAction(VPTFile file) {
		super("projectviewer_wrapper_launch");
		this.file = file;
	} //}}}

	//{{{ +getText() : String
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return jEdit.getProperty("projectviewer.action.launch_browser");
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Creates a new project. */
	public void actionPerformed(ActionEvent e) {
		if (file == null) {
			file = (VPTFile) viewer.getSelectedNode();
		}
		VPTProject p = VPTNode.findProjectFor(file);

		String sURL;
		if (p.getURL() != null && file.getNodePath().startsWith(p.getRootPath())) {
			sURL = p.getURL() + file.getNodePath().substring(p.getRootPath().length() + 1);
		} else {
			sURL = "file://" + file.getNodePath();
		}

		if (File.separatorChar == '\\') {
			// windows mozilla doesn't seem to like '\\'
			sURL = sURL.replace('\\', '/');
		}

		if (ProjectViewerConfig.getInstance().getUseInfoViewer()) {
			Helper.launchInfoViewer(viewer.getView(), sURL);
		} else {
			try {
				Runtime rt = Runtime.getRuntime();
				rt.exec(new String[] { ProjectViewerConfig.getInstance().getBrowserPath(), sURL });
			} catch(java.io.IOException ioe) {
				JOptionPane.showMessageDialog(viewer,
					jEdit.getProperty("projectviewer.launcher.io_error", new Object[] { ioe.getMessage() }),
					jEdit.getProperty("projectviewer.error"),
					JOptionPane.ERROR_MESSAGE);
			}
		}
	} //}}}

	//{{{ +prepareForNode(VPTNode) : void
	/** Enable action only for the root node. */
	public void prepareForNode(VPTNode node) {
		cmItem.setVisible(node != null && node.isFile());
	} //}}}

	//{{{ +class _Helper_
	/**
	 *	Class to hold references to classes that may not be available, so this
	 *	class can behave well when called from a BeanShell script.
	 */
	public static class Helper {

		//{{{ +_launchInfoViewer(View, String)_ : void
		public static void launchInfoViewer(View view, String url) {
			InfoViewerPlugin iv =
				(InfoViewerPlugin) jEdit.getPlugin("infoviewer.InfoViewerPlugin", true);
			if( iv != null) {
				iv.openURL(view, url);
			}
		} //}}}

	} //}}}

}

