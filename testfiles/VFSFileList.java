/*
* VFSFileList.java - GUI panel for list of files
*
* Copyright 2003 Robert McKinnon
* Copyright 2010 Eric Le Lay
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
package xml.gui;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Container;
import java.awt.Point;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
* GUI panel for list of files
* copied from XSLT Plugin: xslt/StylesheetPanel.java
*
* @author Robert McKinnon - robmckinnon@users.sourceforge.net
* @author Eric Le Lay - kerik-sf@users.sourceforge.net
*/
public class VFSFileList extends JPanel implements ListSelectionListener {
	
	private static final String LAST_ITEMS = "last-items";
	
	private View view;
	private DefaultListModel itemsListModel;
	private JList itemsList;
	private String propertyPrefix;
	
	private UsefulAction addItemAction;
	private UsefulAction removeItemAction;
	private UsefulAction upAction;
	private UsefulAction downAction;
	private UsefulAction openFileAction;
	
	/**
	* Constructor for the ItemPanel object.
	*/
	public VFSFileList(View view, String propertyPrefix) {
		this.view = view;
		this.propertyPrefix = propertyPrefix;
		
		addItemAction = new AddItemAction();
		removeItemAction = new RemoveItemAction();
		upAction = new MoveItemUpAction();
		downAction = new MoveItemDownAction();
		openFileAction = new OpenFileAction();
		
		this.itemsListModel = initItemListModel();
		this.itemsList = initItemList();
		
		JScrollPane editorComponent = new JScrollPane(itemsList);
		JToolBar toolBar = initToolBar();
		
		setLayout(new BorderLayout());
		add(editorComponent, BorderLayout.CENTER);
		add(toolBar, BorderLayout.EAST);
		
	}
	
	
	/**
	* Returns the selected item file name, or if none are selected returns null.
	*/
	public String getSelectedItem() {
		int selectedIndex = itemsList.getSelectedIndex();
		
		if(selectedIndex == -1) {
			return null;
		} else {
			return getItem(selectedIndex);
		}
	}
	
	
	/**
	* @return	item at index
	* @throws	IllegalArgumentException	if index is invalid
	*/
	public String getItem(int index) {
		if(index < 0 || index >= itemsListModel.size()) {
			throw new IllegalArgumentException("invalid index: "+index);
		}
		return (String)itemsListModel.get(index);
	}
	
	
	public Object[] getItems() {
		return itemsListModel.toArray();
	}
	
	public void setItems(String[] items) {
		itemsListModel.clear();
		
		for(int i = 0; i < items.length; i++) {
			itemsListModel.add(i, items[i]);
		}
		
		storeItems();
	}
	
	
	public boolean itemsExist() {
		return itemsListModel.size() > 0;
	}
	
	public int getItemCount() {
		return itemsListModel.size();
	}
	
	
	public void setEnabled(boolean enabled){
		if(!enabled){
			addItemAction.setEnabled(false);
			removeItemAction.setEnabled(false);
			itemsList.clearSelection();
			itemsList.setEnabled(false);
		}else{
			addItemAction.setEnabled(true);
			itemsList.setEnabled(true);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
		boolean selectionExists = itemsList.getSelectedIndex() != -1;
		removeItemAction.setEnabled(selectionExists);
		upAction.setEnabled(selectionExists && (itemsListModel.getSize() > 1)
			&& (itemsList.getSelectedIndex() != 0));
		downAction.setEnabled(selectionExists && (itemsListModel.getSize() > 1)
			&& (itemsList.getSelectedIndex() < itemsListModel.getSize() - 1));
	}
	
	
	private DefaultListModel initItemListModel() {
		DefaultListModel itemsListModel = new DefaultListModel();
		List values = PropertyUtil.getEnumeratedProperty(propertyPrefix+LAST_ITEMS);
		Iterator it = values.iterator();
		while(it.hasNext()) {
			itemsListModel.addElement(it.next());
		}
		return itemsListModel;
	}
	
	
	private JList initItemList() {
		JList list = new JList(itemsListModel);
		list.setName(propertyPrefix);
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.getSelectionModel().addListSelectionListener(this);
		
		UsefulAction[] actions = new UsefulAction[] {openFileAction, null, upAction, downAction, null, addItemAction, removeItemAction };
		list.setComponentPopupMenu(UsefulAction.initMenu(actions));
		
		// open the selected item when ENTER is pressed
		list.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"),"open-file");
		list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),"open-file");
		list.getActionMap().put("open-file",openFileAction);
		return list;
	}
	
	private JToolBar initToolBar() {
		this.upAction.setEnabled(false);
		this.downAction.setEnabled(false);
		this.removeItemAction.setEnabled(itemsExist());
		
		JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
		toolBar.setFloatable(false);
		toolBar.add(addItemAction.getButton());
		toolBar.add(removeItemAction.getButton());
		toolBar.addSeparator();
		toolBar.add(upAction.getButton());
		toolBar.add(downAction.getButton());
		
		return toolBar;
	}
	
	
	private void storeItems() {
		List itemsList = Arrays.asList(itemsListModel.toArray());
		PropertyUtil.setEnumeratedProperty(propertyPrefix+LAST_ITEMS, itemsList);
	}
	
	/**
	 * add an item to the list.
	 * Fires a VFSBrowser up
	 */
	private class AddItemAction extends UsefulAction {
		
		AddItemAction(){
			super(VFSFileList.this.propertyPrefix+".add");
		}
		
		public void actionPerformed(ActionEvent e) {
			String path = null;
			
			if(itemsListModel.size() > 0) {
				path = MiscUtilities.getParentOfPath(getItem(0));
			}
			
			String[] selections;
			
			if(getTopLevelAncestor() != view && getTopLevelAncestor() instanceof JFrame){
				selections = GUIUtilities.showVFSFileDialog((JFrame)getTopLevelAncestor(), view, path, JFileChooser.OPEN_DIALOG, false);
			}else {
				selections = GUIUtilities.showVFSFileDialog(view, path, JFileChooser.OPEN_DIALOG, false);
			}
			
			if(selections != null) {
				itemsListModel.addElement(selections[0]);
				
				if((itemsList.getSelectedIndex() != -1) && (itemsListModel.getSize() > 1)) {
					downAction.setEnabled(true);
				}
				
				storeItems();
			}
			
			Container topLevelAncestor = VFSFileList.this.getTopLevelAncestor();
			
			if(topLevelAncestor instanceof JFrame) {
				((JFrame)topLevelAncestor).toFront();
			}
		}
		
	}
	
	/**
	 * remove selected item from the list
	 */
	private class RemoveItemAction extends UsefulAction {
		RemoveItemAction(){
			super(propertyPrefix+".remove");
		}
		
		public void actionPerformed(ActionEvent e) {
			int index = itemsList.getSelectedIndex();
			// when nothing is selected
			if(index < 0 )return;
			
			itemsListModel.remove(index);
			
			if(itemsListModel.getSize() > 0) {
				itemsList.setSelectedIndex(0);
			} else {
				removeItemAction.setEnabled(false);
			}
			
			storeItems();
		}
		
	}
	
	
	private abstract class MoveItemAction extends UsefulAction {
		private int increment;
		
		MoveItemAction(String actionName,int increment){
			super(actionName);
			this.increment = increment;
		}
		
		public void actionPerformed(ActionEvent e) {
			int selectedIndex = itemsList.getSelectedIndex();
			Object selected = itemsListModel.get(selectedIndex);
			itemsListModel.remove(selectedIndex);
			itemsListModel.insertElementAt(selected, selectedIndex + increment);
			itemsList.setSelectedIndex(selectedIndex + increment);
			storeItems();
		}
	}
	
	private void openFile(){
		String file = getSelectedItem();
		if (file != null)
		{
			jEdit.openFile(view, file);
		}
	}
	
	/**
	 * action to move selected item 1 step up in the list
	 */	
	private class MoveItemUpAction extends MoveItemAction {
		
		MoveItemUpAction() {
			super(propertyPrefix+".up",-1);
		}
		
	}
	
	/**
	 * action to move selected item 1 step down in the list
	 */
	private class MoveItemDownAction extends MoveItemAction {
		
		MoveItemDownAction() {
			super(propertyPrefix+".down",1);
		}
		
	}
	
	/**
	 * action to open selected item in jEdit
	 */
	private class OpenFileAction extends UsefulAction {
		
		OpenFileAction() {
			super("xml.gui.file.open");
		}
		
		public void actionPerformed(ActionEvent e) {
			openFile();
		}
		
	}
	
}