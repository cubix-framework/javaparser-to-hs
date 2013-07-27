/*
* ModesOptionPane.java [org.gjt.sp.jedit.options.ContextOptionPane]
* Copyright (C) 2000, 2001 Slava Pestov
*
* Modified by Jakub Roztocil <j.roztocil@gmail.com>
*
* $Id: ModesOptionPane.java 19606 2011-06-20 17:08:59Z ezust $
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
*
*/


package contextmenu;

//{{{ Imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.StandardUtilities;
//}}}

public class ModesOptionPane extends AbstractOptionPane implements ActionListener {


	//{{{ private
	private JComboBox modesCombo;
	private JList actionList;
	private DefaultListModel actionListModel;
	private JTextField menuLabel;
	private HashMap changedModes;
	private RolloverButton add;
	private RolloverButton remove;
	private RolloverButton moveUp, moveDown;
	private String prevSelected;
	//}}}

	//{{{ ModesOptionPane()
    public ModesOptionPane() {
        super("contextmenu-modes");
    } //}}}

	//{{{ _init()
	protected void _init() {

		setLayout(new BorderLayout());

		changedModes = new HashMap();

		// modes combo
		JPanel topPanel = new JPanel(new GridLayout(3,2));
		topPanel.add(new JLabel(jEdit.getProperty("contextmenu.modes-label")));
		modesCombo = new JComboBox(getModeNames());
		modesCombo.addActionListener(this);
		topPanel.add(modesCombo);
		topPanel.add(new JLabel(jEdit.getProperty("contextmenu.menu-name.label")));
		menuLabel = new JTextField();
		menuLabel.setToolTipText(jEdit.getProperty("contextmenu.menu-name.tooltip"));
		topPanel.add(menuLabel);
		add(BorderLayout.NORTH, topPanel);
		topPanel.add(new JLabel(jEdit.getProperty("contextmenu.actions-label")));
		createList();
		createButtons();
		if (jEdit.getActiveView().getBuffer() != null) {
			modesCombo.setSelectedItem(jEdit.getActiveView().getBuffer().getMode().getName());
			modeSelected();
		}
	} //}}}

	//{{{ _save()
	protected void _save() {
		modeSelected();
		Iterator it = changedModes.keySet().iterator();
		while (it.hasNext()) {
			String modeName = (String)it.next();
			HashMap modeMenu = (HashMap)changedModes.get(modeName);
			String label = (String)modeMenu.get("label");
			String actions = (String)modeMenu.get("actions");
			String propertyName = "mode." + modeName + ".contextmenu";
			if (actions.equals("")) {
				jEdit.resetProperty(propertyName);
				jEdit.resetProperty(propertyName + ".label");
				continue;
			}
			jEdit.setProperty(propertyName, actions);
			jEdit.setProperty(propertyName + ".label", label);
		}
	} //}}}

	//{{{ createButtons()
	private  void createButtons() {

		ButtonActionListener buttonListener = new ButtonActionListener();

		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(3,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("options.toolbar.add"));
		add.addActionListener(buttonListener);
		buttons.add(add);
		buttons.add(Box.createHorizontalStrut(6));
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("options.toolbar.remove"));
		remove.addActionListener(buttonListener);
		buttons.add(remove);
		buttons.add(Box.createHorizontalStrut(6));
		moveUp = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
		moveUp.setToolTipText(jEdit.getProperty("options.toolbar.moveUp"));
		moveUp.addActionListener(buttonListener);
		buttons.add(moveUp);
		buttons.add(Box.createHorizontalStrut(6));
		moveDown = new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
		moveDown.setToolTipText(jEdit.getProperty("options.toolbar.moveDown"));
		moveDown.addActionListener(buttonListener);
		buttons.add(moveDown);
		buttons.add(Box.createHorizontalStrut(6));
		buttons.add(Box.createGlue());
		updateButtons();
		add(BorderLayout.SOUTH, buttons);
	} //}}}

	//{{{ createList()
	private void createList() {
		actionListModel = new DefaultListModel();
		actionList = new JList(actionListModel);
		actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(BorderLayout.CENTER,new JScrollPane(actionList));
		actionList.addListSelectionListener(new ListHandler());
	} //}}}

	//{{{ getModeNames()
	private static String[] getModeNames() {
		Mode[] modes = jEdit.getModes();
		String[] names = new String[modes.length];
		for (int i = 0; i < modes.length; i++) {
			names[i] = modes[i].getName();
		}
		return names;
	} //}}}

	//{{{ modeSelected()
	private void modeSelected() {

		String modeName = (String) modesCombo.getSelectedItem();

		// save values for previos mode
		if (prevSelected != null && !modeName.equals("")) {
			HashMap modeMenu = new HashMap(2);
			String actions = "";
			String menuLabelText = menuLabel.getText().equals("") ? prevSelected : menuLabel.getText();
			modeMenu.put("label", menuLabelText);
			for (int i = 0; i < actionListModel.getSize(); i++) {
				actions += ((ActionListItem)actionListModel.getElementAt(i)).getName();
				if (i < actionListModel.getSize() - 1) {
					actions += " ";
				}
			}
			modeMenu.put("actions", actions);
			changedModes.put(prevSelected, modeMenu);
		}

		// clear list
		while (actionListModel.getSize() != 0) {
			actionListModel.removeElementAt(0);
		}

		String menuLabelText = jEdit.getProperty("mode." + modeName + ".contextmenu.label");
		if (menuLabelText == null) {
			menuLabelText = modeName;
		}
		menuLabel.setText(menuLabelText);

		// add items for selected mode
		StringTokenizer st;
		if (changedModes.containsKey(modeName)) {
			st = new StringTokenizer((String)((HashMap)changedModes.get(modeName)).get("actions"));
		} else {
			st = ContextMenuPlugin.getActionsForMode(modeName);
		}
		if (st != null) {
			while (st.hasMoreTokens()) {
				String actionName = st.nextToken();
				if (actionName.equals("-")) {
					actionListModel.addElement(new ActionListItem());
				} else {
					EditAction action = jEdit.getAction(actionName);
					if (action != null) {
						actionListModel.addElement(new ActionListItem(action));
					}
				}
			}
		}
		prevSelected = modeName;
		updateButtons();
	} //}}}

	//{{{ actionPerformed() method
	public void actionPerformed(ActionEvent evt) {
		modeSelected();
	} //}}}

	//{{{ ListItem class
	static class ActionListItem
	{
		String name;
		String label;

		ActionListItem() {
			this.name = "-";
			this.label = "-";
		}

		ActionListItem(EditAction action) {
			this.name = action.getName();
			this.label = GUIUtilities.prettifyMenuLabel(action.getLabel());
		}

		public String getName() {
			return this.name;
		}

		public String getLabel() {
			return this.label;
		}

		public String toString() {
			return this.label;
		}

	} //}}}

	//{{{ updateButtons() method
	private void updateButtons() {
		int index = actionList.getSelectedIndex();
		add.setEnabled(modesCombo.getSelectedIndex() > 0);
		remove.setEnabled(index != -1 && actionListModel.getSize() != 0);
		moveUp.setEnabled(index > 0);
		moveDown.setEnabled(index != -1 && index != actionListModel.getSize() - 1);
	} //}}}

	//{{{ ButtonActionListener class
	class ButtonActionListener implements ActionListener {

		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();
			if (source == add) {
				ModesOptionPane.ContextMenuEditDialog dialog = new ModesOptionPane.ContextMenuEditDialog(
												ModesOptionPane.this,
											(ModesOptionPane.ActionListItem)actionList.getSelectedValue());
				ModesOptionPane.ActionListItem selection = dialog.getSelection();
				if(selection == null)
					return;

				int index = actionList.getSelectedIndex();
				if (index == -1) {
					index = actionListModel.getSize();
				} else {
					index++;
				}
				actionListModel.insertElementAt(selection,index);
				actionList.setSelectedIndex(index);
				actionList.ensureIndexIsVisible(index);
			} else if (source == remove) {
				int index = actionList.getSelectedIndex();
				actionListModel.removeElementAt(index);
				if (actionListModel.getSize() != 0) {
					if (actionListModel.getSize() == index) {
						actionList.setSelectedIndex(index-1);
					} else {
						actionList.setSelectedIndex(index);
					}
				}
			} else if (source == moveUp) {
				int index = actionList.getSelectedIndex();
				Object selected = actionList.getSelectedValue();
				actionListModel.removeElementAt(index);
				actionListModel.insertElementAt(selected,index-1);
				actionList.setSelectedIndex(index-1);
				actionList.ensureIndexIsVisible(index-1);
			} else if (source == moveDown) {
				int index = actionList.getSelectedIndex();
				Object selected = actionList.getSelectedValue();
				actionListModel.removeElementAt(index);
				actionListModel.insertElementAt(selected,index+1);
				actionList.setSelectedIndex(index+1);
				actionList.ensureIndexIsVisible(index+1);

			}
			updateButtons();
			//Log.log(Log.DEBUG, this, source);
		}

	}//}}}

	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt) {
			updateButtons();
		}

	} //}}}

	//{{{ ContextMenuEditDialog class
	class ContextMenuEditDialog extends EnhancedDialog
	{
		//{{{ ContextMenuEditDialog constructor
		public ContextMenuEditDialog(Component comp,
			ModesOptionPane.ActionListItem current)
		{
			super(GUIUtilities.getParentDialog(comp),
				jEdit.getProperty("contextmenu.add-dialog-title"),
				true);


			JPanel content = new JPanel(new BorderLayout());
			content.setBorder(new EmptyBorder(12,12,12,12));
			setContentPane(content);

			ActionHandler actionHandler = new ActionHandler();

			ButtonGroup grp = new ButtonGroup();
			JPanel typePanel = new JPanel(new GridLayout(3,1,6,6));
			typePanel.setBorder(new EmptyBorder(0,0,6,0));
			typePanel.add(new JLabel(jEdit.getProperty("options.toolbar.edit.caption")));

			separator = new JRadioButton(jEdit.getProperty("options.toolbar.edit.separator"));
			separator.addActionListener(actionHandler);
			grp.add(separator);
			typePanel.add(separator);

			action = new JRadioButton(jEdit.getProperty("options.toolbar.edit.action"));
			action.addActionListener(actionHandler);
			grp.add(action);
			action.setSelected(true);
			typePanel.add(action);
			content.add(BorderLayout.NORTH,typePanel);




			JPanel actionPanel = new JPanel(new BorderLayout(6,6));

			ActionSet[] actionsList = jEdit.getActionSets();
			TreeSet<ActionSet> vec = new TreeSet<ActionSet>(new ActionSetCompare());
			for(int i = 0; i < actionsList.length; i++) {
				ActionSet actionSet = actionsList[i];
				if(actionSet.getActionCount() != 0)
					vec.add(actionSet);
			}
			
			combo = new JComboBox(vec.toArray());
			combo.addActionListener(actionHandler);
			actionPanel.add(BorderLayout.NORTH,combo);

			list = new JList();
			list.setVisibleRowCount(8);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			actionPanel.add(BorderLayout.CENTER,new JScrollPane(list));


			content.add(BorderLayout.CENTER,actionPanel);

			JPanel southPanel = new JPanel();
			southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.X_AXIS));
			southPanel.setBorder(new EmptyBorder(12,0,0,0));
			southPanel.add(Box.createGlue());
			ok = new JButton(jEdit.getProperty("common.ok"));
			ok.addActionListener(actionHandler);
			getRootPane().setDefaultButton(ok);
			southPanel.add(ok);
			southPanel.add(Box.createHorizontalStrut(6));
			cancel = new JButton(jEdit.getProperty("common.cancel"));
			cancel.addActionListener(actionHandler);
			southPanel.add(cancel);
			southPanel.add(Box.createGlue());

			content.add(BorderLayout.SOUTH,southPanel);

			if(current == null || current.getName().equals("-")) {
				combo.setSelectedIndex(0);
			} else {
				ActionSet set = jEdit.getActionSetForAction(current.getName());
				combo.setSelectedItem(set);
				updateList();
				list.setSelectedValue(current,true);
			}
			pack();
			setLocationRelativeTo(GUIUtilities.getParentDialog(comp));
			setVisible(true);
			updateEnabled();
		} //}}}

		//{{{ ok() method
		public void ok()
		{
			isOK = true;
			dispose();
		} //}}}

		//{{{ cancel() method
		public void cancel()
		{
			dispose();
		} //}}}

		//{{{ getSelection() method
		public ActionListItem getSelection()
		{
			if (!isOK) {
				return null;
			}

			if (separator.isSelected()) {
				return new ActionListItem();
			} else if(action.isSelected()) 	{
				return (ActionListItem)list.getSelectedValue();
			}
			else
				throw new InternalError();
		} //}}}

		//{{{ updateEnabled() method
		private void updateEnabled()
		{
			combo.setEnabled(action.isSelected());
			list.setEnabled(action.isSelected());
		} //}}}

		//{{{ Private members

		//{{{ Instance variables
		private boolean isOK;
		private JComboBox combo;
		private JList list;
		private JButton ok, cancel;
		private JRadioButton separator, action;
		//}}}


		//{{{ updateList() method
		private void updateList()
		{
			ActionSet actionSet = (ActionSet)combo.getSelectedItem();
			EditAction[] actions = actionSet.getActions();
			Vector listModel = new Vector(actions.length);

			for(int i = 0; i < actions.length; i++)
			{
				EditAction action = actions[i];
				String label = action.getLabel();
				if (label == null) {
					continue;
				}
				listModel.addElement(new ModesOptionPane.ActionListItem(action));
			}
			Collections.sort(listModel, new ModesOptionPane.MenuItemCompare());
			list.setListData(listModel);
		} //}}}

		//}}}

		//{{{ ActionHandler class
		class ActionHandler implements ActionListener
		{
			public void actionPerformed(ActionEvent evt)
			{
				Object source = evt.getSource();
				if (source instanceof JRadioButton) {
					updateEnabled();
				} else  if (source == ok) {
					ok();
				} else if (source == cancel) {
					cancel();
				} else if (source == combo) {
					updateList();
				}
			}
		} //}}}





	} //}}}

	//{{{ MenuItemCompare class
	static class MenuItemCompare implements Comparator {
		public int compare(Object action1, Object action2) {
			return StandardUtilities.compareStrings(
				((ActionListItem)action1).getLabel(),
				((ActionListItem)action2).getLabel(),
				true);
		}
	} //}}}

	static class ActionSetCompare implements Comparator<ActionSet> {
		@Override
		public int compare(ActionSet o1, ActionSet o2)
		{
			return StandardUtilities.compareStrings(o1.getLabel(), o2.getLabel(), true);
		}
		
	}

}



/* :folding=explicit:collapseFolds=1:tabSize=4:indentSize=4:noTabs=false: */
