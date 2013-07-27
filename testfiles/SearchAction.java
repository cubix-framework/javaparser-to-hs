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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.search.SearchDialog;
import org.gjt.sp.jedit.search.SearchFileSet;

import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;

import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;
import projectviewer.VFSHelper;
import projectviewer.config.ProjectViewerConfig;

import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTNode;
//}}}

/**
 *	Opens the search dialog for the selected directory/project.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: SearchAction.java 16366 2009-10-20 07:26:31Z vanza $
 */
public class SearchAction extends Action {

	private VPTNode node = null;

	//{{{ +SearchAction() : <init>
	public SearchAction() {
		super("projectviewer_wrapper_search");
	} //}}}


	public SearchAction(VPTNode node)
	{
		super("projectviewer_wrapper_search");
		this.node = node;
	}

	//{{{ +getText() : String
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return jEdit.getProperty("projectviewer.action.hypersearch");
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Opens the search dialog according to the current context. */
	public void actionPerformed(ActionEvent e) {
		VPTNode searchIn = this.node;
		if (searchIn == null && viewer != null) {
			searchIn = viewer.getSelectedNode();
		}
		if (searchIn == null) {
			searchIn = ProjectViewer.getActiveProject(jEdit.getActiveView());
		}
		if (searchIn != null) {
			if (searchIn.isProject()) {
				ProjectManager mgr = ProjectManager.getInstance();
				if (!mgr.isLoaded(searchIn.getName())) {
					searchIn = mgr.getProject(searchIn.getName());
				}
			} else if (searchIn.isLeaf()) {
				searchIn = (VPTNode) searchIn.getParent();
			}

			String selected = jEdit.getActiveView().getTextArea().getSelectedText();
			SearchAndReplace.setSearchFileSet(new NodeFileSet(searchIn));
			SearchDialog.showSearchDialog(jEdit.getActiveView(),
										  selected,
										  SearchDialog.DIRECTORY);
		} else {
			JOptionPane.showMessageDialog(
					(viewer != null) ? (Component) viewer : (Component) jEdit.getActiveView(),
					jEdit.getProperty("projectviewer.action.search.error"),
					jEdit.getProperty("projectviewer.error"),
					JOptionPane.ERROR_MESSAGE);
		}
	} //}}}

	//{{{ +prepareForNode(VPTNode) : void
	/** Enable action only for the root node. */
	public void prepareForNode(VPTNode node) {
		cmItem.setVisible(true);
		if (node != null) {
			if (node.isDirectory()) {
				((JMenuItem)cmItem).setText(
					jEdit.getProperty("projectviewer.action.hypersearch_dir"));
			} else if (node.isProject()) {
				((JMenuItem)cmItem).setText(
					jEdit.getProperty("projectviewer.action.hypersearch_project"));
			} else {
				((JMenuItem)cmItem).setText(
					jEdit.getProperty("projectviewer.action.hypersearch_parent"));
			}
		} else {
			cmItem.setVisible(false);
		}
	} //}}}

	//{{{ +class _NodeFileSet_
	/**
	 *	Implements a SearchFileSet representing files that are children of a given
	 *	node and its children.
	 *
	 *	@since	PV 2.1.0 (was private before)
	 */
	public static class NodeFileSet extends DirectoryListSet {

		//{{{ Private Members
		private boolean skipBinary;
		private Pattern pFilter;
		private VPTNode node;
		//}}}

		//{{{ +NodeFileSet(VPTNode) : <init>
		public NodeFileSet(VPTNode node) {
			super(null, "*", true);
			this.node = node;
			this.skipBinary = jEdit.getBooleanProperty("search.skipBinary.toggle", false);
		}
		//}}}

		//{{{ +getDirectory() : String
		/** Returns the path to the node. */
		public String getDirectory() {
			return node.getNodePath();
		} //}}}

		//{{{ #_getFiles(Component) : String[]
		/** Returns an array with the files to be searched. */
		protected String[] _getFiles(Component comp) {
			if (node == null) {
				return new String[0];
			}

			String filter = getFileFilter();
			if (filter != null && filter.length() > 0 && !filter.equals("*")) {
				pFilter = Pattern.compile(StandardUtilities.globToRE(filter));
			}

			Set<String> fileset = new HashSet<String>();
			addFiles(node, fileset);
			return fileset.toArray(new String[fileset.size()]);
		}
		//}}}

		//{{{ -addFiles(VPTNode, HashSet) : void
		/**
		 *	Adds all the files below the given node to the list of search files,
		 *	recursively.
		 */
		private void addFiles(VPTNode node,
							  Set<String> fileset)
		{
			Enumeration e = node.children();
			if (e != null)
			while(e.hasMoreElements()) {
				VPTNode n = (VPTNode) e.nextElement();
				if (n.isFile()) {
					String url;
					if (pFilter != null &&
						!pFilter.matcher(n.getNodePath()).matches())
					{
						// filtered out.
						continue;
					}

					url = ((VPTFile)n).getURL();
					if (skipBinary && VFSHelper.isLocal(url)) {
						InputStream is = null;
						try {
							is = new FileInputStream(url);
							if (MiscUtilities.isBinary(is)) {
								continue;
							}
						} catch (IOException ioe) {
							Log.log(Log.ERROR, this, ioe);
							continue;
						} finally {
							if (is != null)  try { is.close(); } catch (Exception ex) { }
						}
					}
					fileset.add(url);
				} else if (n.getAllowsChildren() && isRecursive()) {
					addFiles(n, fileset);
				}
			}
		} //}}}


		/**
		 * Sets the directory where to search inside the project. This
		 * method is really unoptimized since PV doesn't keep a mapping
		 * of directory paths to VPTNode instances.
		 *
		 * @param	directory	Directory where to perform the search.
		 */
		public void setDirectory(String directory)
		{
			if (!directory.equals(node.getNodePath())) {
				node = findDirectory(VPTNode.findProjectFor(node), directory);
			}
			if (node == null) {
				Log.log(Log.WARNING, this,
						"Directory not found in project: " + directory);
			}
		}


		/**
		 * Recursively look for a directory node matching the given path.
		 *
		 * @param	n		Node where to start search.
		 * @param	path	Path being searched.
		 *
		 * @return Directory (or project) node matching the path, or null.
		 */
		private VPTNode findDirectory(VPTNode n,
									  String path)
		{
			if (path.equals(n.getNodePath())) {
				return n;
			}
			for (int i = 0; i < n.getChildCount(); i++) {
				VPTNode child = (VPTNode) n.getChildAt(i);
				if (child.isDirectory()) {
					child = findDirectory(child, path);
					if (child != null) {
						return child;
					}
				}
			}
			return null;
		}

	} //}}}

}
