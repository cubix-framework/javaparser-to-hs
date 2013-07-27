/*
 * ShortcutsDialog.java - Shortcut picking dialog
 * Copyright (C) 2003, 2007 Carmine Lucarelli
 *
 * Originally copied from ShortcutsOptionPane.java
 * Copyright (C) 1999, 2000, 2001 Slava Pestov
 * Copyright (C) 2001 Dirk Moebius
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package shortcutSaver;

import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.StandardUtilities;

/**
 * Action lists dialog
 */
public class ShortcutsDialog extends EnhancedDialog implements ActionListener
{
	public ShortcutsDialog(Dialog parent, String editMode, String actionName, boolean isChained)
	{
		super(jEdit.getActiveView(), jEdit.getProperty("shortcutSaver.frame.title"), true);

		allBindings = new Vector();

		enableEvents(AWTEvent.KEY_EVENT_MASK);

		// create a panel with a BoxLayout. Can't use Box here
		// because Box doesn't have setBorder().
		JPanel content = new JPanel(new GridLayout(0,1,0,6))
		{
			/**
			 * Returns if this component can be traversed by pressing the
			 * Tab key. This returns false.
			 */
			public boolean isManagingFocus()
			{
				return false;
			}

			/**
			 * Makes the tab key work in Java 1.4.
			 * @since jEdit 3.2pre4
			 */
			public boolean getFocusTraversalKeysEnabled()
			{
				return false;
			}
		};
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		getContentPane().setLayout(new BorderLayout(12,12));

		initModels();

		selectModel = new JComboBox(models);
		selectModel.addActionListener(this);

		JPanel north = new JPanel(new GridLayout(3, 2, 5, 10));
		String[] args = { editMode };
		JLabel label = new JLabel(jEdit.getProperty("shortcutSaver.current.label", args));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		north.add(label);
		action = new TextFieldWithClear(actionName);
		north.add(action);

		label = new JLabel(jEdit.getProperty("shortcutSaver.actionset.label"));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		north.add(label);
		north.add(selectModel);

		north.add(new JLabel(""));
		chain = new JCheckBox("  Check to chain actions");
		chain.setHorizontalAlignment(SwingConstants.RIGHT);
		chain.setSelected(isChained);
		north.add(chain);

		JPanel south = new JPanel();
		ok = new JButton(jEdit.getProperty("shortcutSaver.ok.label"));
		ok.addActionListener(this);
		cancel = new JButton(jEdit.getProperty("shortcutSaver.cancel.label"));
		cancel.addActionListener(this);
		south.add(ok);
		south.add(cancel);
		
		keyTable = new JTable(currentModel);
		keyTable.getTableHeader().setReorderingAllowed(false);
		keyTable.addMouseListener(new TableMouseHandler());
		Dimension d = keyTable.getPreferredSize();
		d.height = Math.min(d.height,200);
		JScrollPane scroller = new JScrollPane(keyTable);
		scroller.setPreferredSize(d);

		getContentPane().add(BorderLayout.NORTH,north);
		getContentPane().add(BorderLayout.SOUTH,south);
		getContentPane().add(BorderLayout.CENTER,scroller);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		pack();
		setLocationRelativeTo(getParent());
		setResizable(true);
		GUIUtilities.loadGeometry(this, "shortcutsDialog.window");
		show();
	}

	private boolean isOK = false;
	
	//{{{ Enhanced Dialog implementation
	public void ok()
	{
		this.isOK = true;
		GUIUtilities.saveGeometry(this, "shortcutsDialog.window");
		dispose();
	}

	public boolean isOK()
	{
		return isOK;
	}

	public boolean isChained()
	{
		return chain.isSelected();
	}
	
	public void cancel()
	{
		GUIUtilities.saveGeometry(this, "shortcutsDialog.window");
		dispose();
	}
	//}}}

	public String getAction()
	{
		return action.getText();
	}

	private void initModels()
	{
		models = new Vector();
		ActionSet[] actionSets = jEdit.getActionSets();
		for(int i = 0; i < actionSets.length; i++)
		{
			ActionSet actionSet = actionSets[i];
			if(actionSet.getActionCount() != 0)
			{
				models.addElement(createModel(actionSet.getLabel(),
					actionSet.getActionNames()));
			}
		}
		Collections.sort(models,new StandardUtilities.StringCompare(true));
		currentModel = (ShortcutsModel)models.elementAt(0);
	}

	private ShortcutsModel createModel(String modelLabel, String[] actions)
	{
		Vector bindings = new Vector(actions.length);

		for(int i = 0; i < actions.length; i++)
		{
			String name = actions[i];
			String label = jEdit.getProperty(actions[i] + ".label");
			// Skip certain actions this way
			if(label == null)
				continue;

			label = GUIUtilities.prettifyMenuLabel(label);
			addBindings(name,label,bindings);
		}

		return new ShortcutsModel(modelLabel,bindings);
	}

	private void addBindings(String name, String label, Vector bindings)
	{
		ActionBean bean = new ActionBean(name, label);
		bindings.addElement(bean);
	}

	private void addAction(String actionName)
	{
		String work = action.getText().trim();
		if(work.length() == 0)
			action.setText(actionName);
		else
			action.setText(action.getText().trim() + " " + actionName);
	}

	// private members
	private JTable keyTable;
	private Vector models;
	private ShortcutsModel currentModel;
	private JComboBox selectModel;
	private Vector allBindings;
	private JTextField action;
	private JButton ok;
	private JButton cancel;
	private JCheckBox chain;

	class TableMouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			int row = keyTable.getSelectedRow();
			int col = keyTable.getSelectedColumn();
			if(row != -1 && evt.getClickCount() == 2)
			{
				addAction(currentModel.getBindingAt(row,col-1).name);
			}
		}
	}

	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == ok)
			ok();
		else if(evt.getSource() == cancel)
			cancel();
		else
		{
			ShortcutsModel newModel
				= (ShortcutsModel)selectModel.getSelectedItem();
	
			if(currentModel != newModel)
			{
				currentModel = newModel;
				keyTable.setModel(currentModel);
			}
		}
	}

	class ShortcutsModel extends AbstractTableModel implements Comparator
	{
		private Vector bindings;
		private String name;

		ShortcutsModel(String name, Vector bindings)
		{
			this.name = name;
			this.bindings = bindings;
			sort();
		}

		public int getColumnCount()
		{
			return 1;
		}

		public int getRowCount()
		{
			return bindings.size();
		}

		public Object getValueAt(int row, int col)
		{
			switch(col)
			{
				case 0:
					return getBindingAt(row,0).label;
				default:
					return null;
			}
		}

		public void sort()
		{
			Collections.sort(bindings, this);
			fireTableDataChanged();
		}

		public void setValueAt(Object value, int row, int col)
		{
		}

		public String getColumnName(int index)
		{
			switch(index)
			{
			case 0:
				return jEdit.getProperty("shortcutSaver.actioncolumn.label");
			default:
				return null;
			}
		}

		public ActionBean getBindingAt(int row, int nr)
		{
			ActionBean bean = (ActionBean)bindings.elementAt(row);
			return bean;
		}

		public String toString()
		{
			return name;
		}
		
		// Comparator implementation
		public int compare(Object o1, Object o2)
		{
			String label1 = ((ActionBean)o1).label;
			String label2 = ((ActionBean)o2).label;
			return label1.compareTo(label2);
		}
		
		public boolean equals(Object o1, Object o2)
		{
			String label1 = ((ActionBean)o1).label;
			String label2 = ((ActionBean)o2).label;
			return label1.equals(label2);
		}
	}
	
	private class ActionBean
	{
		public String name;
		public String label;

		public ActionBean(String name, String label)
		{
			this.name = name;
			this.label = label;
		}
	}
}
