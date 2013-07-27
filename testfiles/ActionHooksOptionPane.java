/*
 * ActionHooksOptionPane.java - ActionHooks Plugin
 *
 * Copyright 2003,2004 Ollie Rutherfurd <oliver@rutherfurd.net>
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
 * $Id: ActionHooksOptionPane.java,v 1.3 2004/02/01 19:40:39 orutherfurd Exp $
 */
package actionhooks.options;

//{{{ imports
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.StandardUtilities;

import actionhooks.*;
//}}}

@SuppressWarnings("serial")
public class ActionHooksOptionPane extends AbstractOptionPane
{
	//{{{ ActionHooksOptionPane constructor
	public ActionHooksOptionPane()
	{
		super("actionhooks");
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		Vector<String> eventNames = ActionHooksPlugin.getEvents();
		ActionHandler actionHandler = new ActionHandler();

		JPanel eventsPanel = new JPanel(new BorderLayout());
		JPanel mainPanel = new JPanel(new BorderLayout());
		setLayout(new BorderLayout());
		enabled = new JCheckBox(jEdit.getProperty("options.actionhooks.enabled"));
		enabled.setSelected(ActionHooksPlugin.getEnabled());
		add(BorderLayout.SOUTH, enabled);
		eventsPanel.add(BorderLayout.WEST, 
			new JLabel(jEdit.getProperty("options.actionhooks.event")));
		Collections.sort(eventNames,
			new StandardUtilities.StringCompare<String>(true));
		events = new JComboBox(eventNames);
		events.addActionListener(actionHandler);
		eventsPanel.add(BorderLayout.CENTER, events);
		mainPanel.setBorder(BorderFactory.createTitledBorder(
			jEdit.getProperty("options.actionhooks.bindings")));
		mainPanel.add(BorderLayout.NORTH,eventsPanel);
		mainPanel.add(BorderLayout.CENTER,
			new JScrollPane(actions = new JList()));
		actions.addListSelectionListener(new ListHandler());

		for (String event: eventNames)
		{
			Vector<EditAction> _actions =
				ActionHooksPlugin.getActionsForEvent(event);
			//Log.log(Log.DEBUG, this, "Loading actions for " + event); // ##
			DefaultListModel model = new DefaultListModel();
			//Log.log(Log.DEBUG, this, "Actions for " + event + " are " + _actions); // ##
			for(EditAction action: _actions)
				model.addElement(new Action(action.getName(), action.getLabel()));
			listModels.put(event, model);
		}

		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(3,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("options.actionhooks.add"));
		add.addActionListener(actionHandler);
		buttons.add(add);
		buttons.add(Box.createHorizontalStrut(6));
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("options.actionhooks.remove"));
		remove.addActionListener(actionHandler);
		buttons.add(remove);
		buttons.add(Box.createHorizontalStrut(6));
		moveUp = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
		moveUp.setToolTipText(jEdit.getProperty("options.actionhooks.moveUp"));
		moveUp.addActionListener(actionHandler);
		buttons.add(moveUp);
		buttons.add(Box.createHorizontalStrut(6));
		moveDown = new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
		moveDown.setToolTipText(jEdit.getProperty("options.actionhooks.moveDown"));
		moveDown.addActionListener(actionHandler);
		buttons.add(moveDown);
		buttons.add(Box.createGlue());
		mainPanel.add(BorderLayout.SOUTH, buttons);

		add(BorderLayout.CENTER, mainPanel);

		displayActions();
		updateButtons();
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		Vector<String> eventNames = ActionHooksPlugin.getEvents();
		for (String event: eventNames)
		{
			Vector<String> _actions = new Vector<String>();
			StringBuilder buffer = new StringBuilder();
			DefaultListModel model = listModels.get(event);
			for(int j=0; j < model.size(); j++)
			{
				if(j > 0)
					buffer.append(",");

				Action action = (Action)model.elementAt(j);
				_actions.addElement(action.name);
				buffer.append(action.name);
			}
			ActionHooksPlugin.setActionsForEvent(event,_actions);
			jEdit.setProperty(event + ".actions", buffer.toString());
		}

		jEdit.setBooleanProperty("actionhooks.enabled", enabled.isSelected());

	} //}}}

	//{{{ declarations
	private JComboBox events;
	private JList actions;
	// XXX rename to models
	private HashMap<String, DefaultListModel> listModels =
		new HashMap<String, DefaultListModel>();
	private JCheckBox enabled;
	private RolloverButton add;
	private RolloverButton remove;
	private RolloverButton moveUp, moveDown;
	//}}}

	//{{{ updateButtons() method
	private void updateButtons()
	{
		int index = actions.getSelectedIndex();
		ListModel listModel = actions.getModel();
		remove.setEnabled(index != -1 && listModel.getSize() != 0);
		moveUp.setEnabled(index > 0);
		moveDown.setEnabled(index != -1 && index < listModel.getSize()-1);
	} //}}}

	//{{{ displayActions() method
	private void displayActions()
	{
		String event = (String)events.getSelectedItem();
		//Log.log(Log.DEBUG, this, "displaying actions for " + event);	// ##
		DefaultListModel model = listModels.get(event);
		actions.setModel(model);
		//Log.log(Log.DEBUG, this, "model.size(): " + model.size() + ", " 
		//	+ model.toString());	// ##
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == events)
			{
				displayActions();
				updateButtons();
			}
			else if(source == add)
			{
				SelectActionDialog dialog = new SelectActionDialog(
					ActionHooksOptionPane.this);
				EditAction action = dialog.getSelection();
				if(action == null)
					return;

				int index = actions.getSelectedIndex();
				DefaultListModel model = (DefaultListModel)actions.getModel();

				if(index == -1)
					index = model.getSize();
				else
					index++;

				model.insertElementAt(new Action(action.getName(),
					action.getLabel()),index);
				actions.setSelectedIndex(index);
				actions.ensureIndexIsVisible(index);
			}
			else if(source == remove)
			{
				DefaultListModel model = (DefaultListModel)actions.getModel();
				int index = actions.getSelectedIndex();
				model.removeElementAt(index);
				if(model.getSize() > 0)
				{
					if(model.getSize() == index)
						actions.setSelectedIndex(index-1);
					else
						actions.setSelectedIndex(index);
				}
				updateButtons();
			}
			else if(source == moveUp)
			{
				DefaultListModel model = (DefaultListModel)actions.getModel();
				int index = actions.getSelectedIndex();
				Object selected = actions.getSelectedValue();
				model.removeElementAt(index);
				model.insertElementAt(selected,index-1);
				actions.setSelectedIndex(index-1);
				actions.ensureIndexIsVisible(index-1);
			}
			else if(source == moveDown)
			{
				DefaultListModel model = (DefaultListModel)actions.getModel();
				int index = actions.getSelectedIndex();
				Object selected = actions.getSelectedValue();
				model.removeElementAt(index);
				model.insertElementAt(selected,index+1);
				actions.setSelectedIndex(index+1);
				actions.ensureIndexIsVisible(index+1);
			}
		}
	} //}}}

	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateButtons();
		}
	} //}}}
}

@SuppressWarnings("serial")
class SelectActionDialog extends EnhancedDialog
{
	//{{{ SelectActionDialog() constructor
	public SelectActionDialog(Component comp)
	{
		super(GUIUtilities.getParentDialog(comp),
				jEdit.getProperty("options.actionhooks.select-action.title"),
				true);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		ActionHandler actionHandler = new ActionHandler();

		ActionSet[] actionsList = jEdit.getActionSets();
		Vector<ActionSet> actionSets =
			new Vector<ActionSet>(actionsList.length);
		for (ActionSet actionSet: actionsList)
		{
			if(actionSet.getActionCount() != 0)
				actionSets.addElement(actionSet);
		}
		Collections.sort(actionSets, new Comparator<ActionSet>()
		{
			public int compare(ActionSet actionSet1, ActionSet actionSet2)
			{
				return actionSet1.getLabel().compareTo(actionSet2.getLabel());
			}
		});
		JPanel p = new JPanel();
		p.add(new JLabel(
			jEdit.getProperty("options.actionhooks.actionSetCombo.label")));
		combo = new JComboBox(actionSets);
		combo.addActionListener(actionHandler);
		p.add(combo);
		content.add(BorderLayout.NORTH, p);

		list = new JList();
		list.setVisibleRowCount(8);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		content.add(BorderLayout.CENTER, new JScrollPane(list));

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
		ok.setPreferredSize(cancel.getPreferredSize());
		cancel.addActionListener(actionHandler);
		southPanel.add(cancel);
		southPanel.add(Box.createGlue());
		content.add(BorderLayout.SOUTH,southPanel);

		updateList();

		pack();
		setLocationRelativeTo(GUIUtilities.getParentDialog(comp));
		setVisible(true);
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
	public EditAction getSelection()
	{
		if(!isOK)
			return null;
		Action action = (Action)list.getSelectedValue();
		if(action != null)
			return jEdit.getAction(action.name);
		return null;
	} //}}}

	//{{{ private declarations
	private boolean isOK;
	private JComboBox combo;
	private JList list;
	private JButton ok, cancel;
	//}}}

	//{{{ updateList() method
	private void updateList()
	{
		ActionSet actionSet = (ActionSet)combo.getSelectedItem();
		EditAction[] actions = actionSet.getActions();
		Vector<Action> listModel = new Vector<Action>(actions.length);

		for (EditAction action: actions)
		{
			String label = action.getLabel();
			if(label == null)
				continue;
			listModel.addElement(new Action(action.getName(),label));
		}
		Collections.sort(listModel, new ActionCompare());
		list.setListData(listModel);
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == ok)
				ok();
			else if(source == cancel)
				cancel();
			else if(source == combo)
				updateList();
		}
	} //}}}

	//{{{ ActionCompare
	static class ActionCompare implements Comparator<Action>
	{
		public int compare(Action a1, Action a2)
		{
			return StandardUtilities.compareStrings(a1.label, a2.label, true);
		}
	} //}}}
}

//{{{ Action class
class Action
{
	//{{{ Action constructor
	Action(String name, String label)
	{
		this.name = name;
		this.label = GUIUtilities.prettifyMenuLabel(label);
	} //}}}

	//{{{ toString() method
	public String toString()
	{
		return label;
	} //}}}

	//{{{ declarations
	String name;
	String label;
	//}}}

} //}}}


// :collapseFolds=1:noTabs=false:lineSeparator=\n:tabSize=4:indentSize=4:deepIndent=false:folding=explicit:
