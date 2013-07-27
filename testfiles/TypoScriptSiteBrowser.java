/***************************************************************
*  Copyright notice
*
*  (c) 2005,2006 Neil Bertram (neil@tasmanstudios.co.nz)
*  All rights reserved
*
*  This plugin is part of the Typo3 project. The Typo3 project is
*  free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  The GNU General Public License can be found at
*  http://www.gnu.org/copyleft/gpl.html.
*  A copy is found in the textfile GPL.txt
*
*
*  This plugin is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  This copyright notice MUST APPEAR in all copies of the source!
***************************************************************/
/**
 * $Id: TypoScriptSiteBrowser.java 10411 2006-01-29 03:28:55Z n31l $
 * 
 * This is the dockable site browser used for opening a remote
 * TYPO3 template
 *
 * @author      Neil Bertram <neil@tasmanstudios.co.nz>
 */

package typoscript;



import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;

/**
 * The dockable site browser panel, displays a tree of sites which dynamically
 * load page tree on opening and allows the opening of templates through a context menu
 * or buttons at the bottom of the panel
 */
public class TypoScriptSiteBrowser extends JPanel implements DefaultFocusComponent {
	private JScrollPane siteTreePane;
	private JTree siteTree;
	protected DefaultMutableTreeNode treeRootNode;
	protected DefaultTreeModel siteTreeModel;
	protected JButton btnOpenConstants;
	protected JButton btnOpenSetup;
	protected View view;
	protected Vector localSitesConfig;
	
	private JPanel siteLoadPane;
	
	public TypoScriptSiteBrowser(View ourView, String position) {
		super(new BorderLayout());
		view = ourView;
		
		treeRootNode = new DefaultMutableTreeNode("ROOT"); // not visible
		
		localSitesConfig = (Vector)TypoScriptPlugin.siteConfig.clone();
		Iterator iter = localSitesConfig.iterator();	
		while (iter.hasNext()) {
			treeRootNode.add(new T3PageNode((T3Site)iter.next()));
		}
		
		siteTreeModel = new DefaultTreeModel(treeRootNode);
		siteTree = new JTree(siteTreeModel);
		
		siteTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		ToolTipManager.sharedInstance().registerComponent(siteTree);
		siteTree.setCellRenderer(new T3TreeRenderer());
		siteTree.setRootVisible(false);
		siteTree.addTreeExpansionListener(new TreeExpansionListener() {
			public void treeExpanded(TreeExpansionEvent e) {
				T3PageNode opened = (T3PageNode)e.getPath().getLastPathComponent();
				if (e.getPath().getPathCount() == 2) { // is root page
					opened.loadTree(siteTreeModel);
				}
			}
			public void treeCollapsed(TreeExpansionEvent e) {
				; // ignore
			}
		});
		siteTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				if (siteTree.getLastSelectedPathComponent() instanceof T3PageNode) {
					T3PageNode selected = (T3PageNode)siteTree.getLastSelectedPathComponent();
					if (selected.hasTemplate()) {
						// Enable the open buttons
						btnOpenConstants.setEnabled(true);
						btnOpenSetup.setEnabled(true);
						return;
					}
				}
				btnOpenConstants.setEnabled(false);
				btnOpenSetup.setEnabled(false);
			}
		});
		siteTree.addMouseListener(new MouseAdapter() {
			// Some systems trigger popups on mousedown, others mouseup (see MouseEvent.isPopupTrigger())
			public void mousePressed(MouseEvent e) {
				this.mouseReleased(e); // cheating!
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					int row = siteTree.getRowForLocation(e.getX(), e.getY());
					siteTree.setSelectionRow(row);
					T3PageNode curNode = (T3PageNode)siteTree.getLastSelectedPathComponent();
					if (curNode.popupMenu != null) {
						curNode.popupMenu.show(siteTree, e.getX(), e.getY(), siteTree, view);
					}
				}
			}
		});
		siteTree.setShowsRootHandles(false);
		
		siteTreePane = new JScrollPane(siteTree);
		this.add(siteTreePane, BorderLayout.CENTER);
		
		siteLoadPane = new JPanel();
		btnOpenConstants = new JButton("Constants");
		btnOpenSetup = new JButton("Setup");
		btnOpenConstants.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				T3PageNode selected = (T3PageNode)siteTree.getLastSelectedPathComponent();
				selected.open(siteTree, "constants", view);
			}
		});
		btnOpenSetup.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				T3PageNode selected = (T3PageNode)siteTree.getLastSelectedPathComponent();
				selected.open(siteTree, "setup", view);
			}
		});
		btnOpenConstants.setEnabled(false);
		btnOpenSetup.setEnabled(false);

		siteLoadPane.add(btnOpenConstants);
		siteLoadPane.add(btnOpenSetup);

		this.add(siteTreePane, BorderLayout.CENTER);
		this.add(siteLoadPane, BorderLayout.PAGE_END);	
	}

	/**
	 * Called when someone focuses the browser panel
	 */
	public void focusOnDefaultComponent() {
		;
	}
	
	/**
	 * Called by the main plugin class to inform us of an options-save event on the EditBus
	 */
	public void checkForPossibleSitesUpdate() {
		if (!localSitesConfig.equals(TypoScriptPlugin.siteConfig)) {
			// The list has changed. Update
			localSitesConfig = (Vector)TypoScriptPlugin.siteConfig.clone();
			Iterator iter = localSitesConfig.iterator();	
			treeRootNode.removeAllChildren();
			while (iter.hasNext()) {
				treeRootNode.add(new T3PageNode((T3Site)iter.next()));
			}
			siteTreeModel.nodeStructureChanged(treeRootNode);
		}
	}
}

