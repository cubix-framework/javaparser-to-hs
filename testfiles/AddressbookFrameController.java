// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.addressbook.gui.frame;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.columba.addressbook.config.AddressbookConfig;
import org.columba.addressbook.folder.AddressbookTreeNode;
import org.columba.addressbook.folder.IContactStorage;
import org.columba.addressbook.gui.table.FilterToolbar;
import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tagging.ContactTagList;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.api.gui.frame.IContainer;
import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IDockable;
import org.columba.core.config.ViewItem;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.semantic.api.ISemanticContext;
import org.columba.core.gui.frame.DockFrameController;
import org.columba.core.gui.tagging.TagList;
import org.columba.core.gui.tagging.TagPopupMenu;
import org.columba.core.io.DiskIO;

/**
 * 
 * 
 * @author fdietz
 */
public class AddressbookFrameController extends DockFrameController implements
		AddressbookFrameMediator, TreeSelectionListener, ListSelectionListener {

	protected TreeController tree;

	protected TableController table;

	protected FilterToolbar filterToolbar;

	private IDockable contactListPanel;

	private IDockable treePanel;

	private IDockable tagListDockable;

	/**
	 * Constructor for AddressbookController.
	 */
	public AddressbookFrameController(ViewItem viewItem) {
		super(viewItem);

		tree = new TreeController(this);
		table = new TableController(this);
		filterToolbar = new FilterToolbar(table);

		// table should be updated when tree selection changes
		tree.getView().addTreeSelectionListener(table);

		// this is needed to update the titlebar
		tree.getView().addTreeSelectionListener(this);

		// getContainer().setContentPane(this);

		registerDockables();

		table.getView().getSelectionModel().addListSelectionListener(this);

		// initPerspective(this.perspective);
	}

	private void registerDockables() {

		JScrollPane treeScrollPane = new JScrollPane(tree.getView());
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		treePanel = registerDockable("addressbook_foldertree",
				AddressbookResourceLoader.getString("global",
						"dockable_foldertree"), treeScrollPane, null);

		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		JScrollPane tableScrollPane = new JScrollPane(table.getView());
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		p.add(tableScrollPane, BorderLayout.CENTER);
		p.add(filterToolbar, BorderLayout.NORTH);

		contactListPanel = registerDockable("addressbook_contactlist",
				AddressbookResourceLoader.getString("global",
						"dockable_contactlist"), p, null);

//		TagList tagList = new ContactTagList(this);
//		JScrollPane tagListScrollPane = new JScrollPane(tagList);
//		tagListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		tagListScrollPane
//				.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

//		tagListDockable = registerDockable("addressbook_taglist",
//				AddressbookResourceLoader.getString("global",
//						"dockable_taglist"), tagListScrollPane,
//				new TagPopupMenu(this, tagList));
//		tagList.setPopupMenu(new TagPopupMenu(this, tagList));

	}

	/**
	 * @return AddressbookTableController
	 */
	public TableController getTable() {
		return table;
	}

	/**
	 * @return AddressbookTreeController
	 */
	public TreeController getTree() {
		return tree;
	}

	/**
	 * @see org.columba.addressbook.gui.frame.AddressbookFrameMediator#addTableSelectionListener(javax.swing.event.ListSelectionListener)
	 */
	public void addTableSelectionListener(ListSelectionListener listener) {
		getTable().getView().getSelectionModel().addListSelectionListener(
				listener);
	}

	/**
	 * @see org.columba.addressbook.gui.frame.AddressbookFrameMediator#addTreeSelectionListener(javax.swing.event.TreeSelectionListener)
	 */
	public void addTreeSelectionListener(TreeSelectionListener listener) {
		getTree().getView().addTreeSelectionListener(listener);
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return AddressbookResourceLoader.getString(sPath, sName, sID);
	}

	/**
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent arg0) {
		AddressbookTreeNode selectedFolder = (AddressbookTreeNode) arg0
				.getPath().getLastPathComponent();

		if (selectedFolder != null) {
			fireTitleChanged(selectedFolder.getName());
		}
	}

	public void loadDefaultPosition() {
		super.dock(contactListPanel, IDock.REGION.CENTER);
		super.dock(treePanel, contactListPanel, IDock.REGION.WEST, 0.3f);

		super.setSplitProportion(treePanel, 0.3f);
		super.setSplitProportion(contactListPanel, 0.35f);
	}

	/** *********************** container callbacks ************* */

	public void extendMenu(IContainer container) {
		try {
			InputStream is = DiskIO
					.getResourceStream("org/columba/addressbook/action/menu.xml");
			container.extendMenu(this, is);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void extendToolBar(IContainer container) {
		try {
			File configDirectory = AddressbookConfig.getInstance()
					.getConfigDirectory();
			InputStream is2 = new FileInputStream(new File(configDirectory,
					"main_toolbar.xml"));
			container.extendToolbar(this, is2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void valueChanged(ListSelectionEvent event) {
		// return if selection change is in flux
		if (event.getValueIsAdjusting()) {
			return;
		}

		Object[] uids = getTable().getUids();

		if (uids.length == 1) {
			String id = (String) uids[0];

			// get selected folder
			IContactStorage store = getTree().getSelectedFolder();
			if (store == null)
				return;
			// retrieve contact model from folder
			IContactModel model = store.get(id);
			if (model == null)
				return;

			String lastname = model.getFamilyName();
			String firstname = model.getGivenName();
			String displayname = model.getSortString();
			String email = model.getPreferredEmail();

			// create empty value
			IStructureValue value = getSemanticContext().createValue();

			// create identity value
			IStructureValue identity = value.addChild(
					ISemanticContext.CONTEXT_NODE_IDENTITY,
					ISemanticContext.CONTEXT_NAMESPACE_CORE);
			if (displayname.toString() != null)
				identity.setString(ISemanticContext.CONTEXT_ATTR_DISPLAY_NAME,
						ISemanticContext.CONTEXT_NAMESPACE_CORE, displayname);
			if (email != null)
				identity.setString(ISemanticContext.CONTEXT_ATTR_EMAIL_ADDRESS,
						ISemanticContext.CONTEXT_NAMESPACE_CORE, email);
			if (firstname != null)
				identity.setString(ISemanticContext.CONTEXT_ATTR_FIRST_NAME,
						ISemanticContext.CONTEXT_NAMESPACE_CORE, firstname);
			if (lastname != null)
				identity.setString(ISemanticContext.CONTEXT_ATTR_LAST_NAME,
						ISemanticContext.CONTEXT_NAMESPACE_CORE, lastname);
		}
	}

}