/*
 * VariablesOptionPane.java - Variables options panel
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=2:
 *
 * Copyright (C) 1999, 2000, 2001, 2002 Slava Pestov
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

package superabbrevs.gui;

//{{{ Imports
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.StandardUtilities;

import superabbrevs.SuperAbbrevs;
//}}}

//{{{ VariablesOptionPane class
/**
 * I modified Slava Pestov code
 * @author Sune Simonsen 
 */
public class VariablesOptionPane extends AbstractOptionPane {
	//{{{ VariablesOptionPane constructor
	public VariablesOptionPane(View view)
	{
		super("superabbrevs.variables");
		
		this.view = view;
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		setLayout(new BorderLayout());

		ActionHandler actionHandler = new ActionHandler();
		
		variablesModel = new VariablesModel(SuperAbbrevs.loadVariables());

		variablesTable = new JTable(variablesModel);
		variablesTable.getColumnModel().getColumn(1).setCellRenderer(
			new Renderer());
		//variablesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		variablesTable.getTableHeader().setReorderingAllowed(false);
		variablesTable.getTableHeader().addMouseListener(new HeaderMouseHandler());
		variablesTable.getSelectionModel().addListSelectionListener(
			new SelectionHandler());
		variablesTable.getSelectionModel().setSelectionMode(
			ListSelectionModel.SINGLE_SELECTION);
		
		Dimension d = variablesTable.getPreferredSize();
		d.height = Math.min(d.height,200);
		JScrollPane scroller = new JScrollPane(variablesTable);
		scroller.setPreferredSize(d);
		add(BorderLayout.CENTER,scroller);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(6,0,0,0));

		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("options.abbrevs.add"));
		add.addActionListener(actionHandler);
		buttons.add(add);
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("options.abbrevs.remove"));
		remove.addActionListener(actionHandler);
		buttons.add(remove);
		buttons.add(Box.createGlue());
		
		add(BorderLayout.SOUTH,buttons);
		
		// Set the width of the columns
		variablesTable.getColumnModel().getColumn(0).setMinWidth(100);
		variablesTable.getColumnModel().getColumn(1).setPreferredWidth(550);
		
		updateEnabled();
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		if(variablesTable.getCellEditor() != null)
			variablesTable.getCellEditor().stopCellEditing();
		
		SuperAbbrevs.saveVariables(variablesModel.toHashtable());
	} //}}}
	
	//{{{ Private members

	//{{{ Instance variables
	private JComboBox setsComboBox;
	private JTable variablesTable;
	private VariablesModel variablesModel;
	private JButton add;
	private JButton remove;
	private View view;
	//}}}

	//{{{ updateEnabled() method
	private void updateEnabled()
	{
		int selectedRow = variablesTable.getSelectedRow();
		remove.setEnabled(selectedRow != -1);
	} //}}}

	//{{{ add() method
	private void add(VariablesModel variablesModel, String name,
		String value)
	{
		for(int i = 0; i < variablesModel.getRowCount(); i++)
		{
			if(variablesModel.getValueAt(i,0).equals(name))
			{
				variablesModel.remove(i);
				break;
			}
		}

		variablesModel.add(name,value);
		updateEnabled();
	} //}}}

	//{{{ HeaderMouseHandler class
	class HeaderMouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			switch(variablesTable.getTableHeader().columnAtPoint(evt.getPoint()))
			{
			case 0:
				((VariablesModel)variablesTable.getModel()).sort(0);
				break;
			case 1:
				((VariablesModel)variablesTable.getModel()).sort(1);
				break;
			}
		}
	} //}}}

	//{{{ SelectionHandler class
	class SelectionHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateEnabled();
		}
	} //}}}
	
	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			VariablesModel variablesModel = (VariablesModel)variablesTable.getModel();

			Object source = evt.getSource();
			if(source == add)
			{
				add(variablesModel,"","");
			}
			else if(source == remove)
			{
				int selectedRow = variablesTable.getSelectedRow();
				variablesModel.remove(selectedRow);
				updateEnabled();
			}
		}
	} //}}}
	
	//{{{ Renderer class
	static class Renderer extends DefaultTableCellRenderer
	{
		public Component getTableCellRendererComponent(
			JTable table,
			Object value,
			boolean isSelected,
			boolean cellHasFocus,
			int row,
			int col)
		{
			String valueStr = value.toString();

			// workaround for Swing's annoying processing of
			// labels starting with <html>, which often breaks
			if(valueStr.toLowerCase().startsWith("<html>"))
				valueStr = " " + valueStr;
			return super.getTableCellRendererComponent(table,valueStr,
				isSelected,cellHasFocus,row,col);
		}
	} //}}}
	
	//}}}
} //}}}

//{{{ VariablesModel class
class VariablesModel extends AbstractTableModel {
	Vector variables;
	int lastSort;

	//{{{ VariablesModel constructor
	VariablesModel(Hashtable variablesHash)
	{
		variables = new Vector();

		if(variablesHash != null)
		{
			Enumeration nameEnum = variablesHash.keys();
			Enumeration valueEnum = variablesHash.elements();

			while(nameEnum.hasMoreElements())
			{
				variables.addElement(new Variable((String)nameEnum.nextElement(),
					(String)valueEnum.nextElement()));
			}

			sort(0);
		}
	} //}}}
	
	//{{{ getColumnClass method
	/**
	 * Method getColumnClass(int columnIndex)
	 * Returns Object.class regardless of columnIndex.
	 */
	public Class getColumnClass(int columnIndex) {
		// All the coloums is strings
		return "String".getClass();
	}
	//}}}

	//{{{ sort() method
	void sort(int col)
	{
		lastSort = col;
		Collections.sort(variables,new VariableCompare(col));
		fireTableDataChanged();
	} //}}}

	//{{{ add() method
	void add(String name, String value)
	{
		variables.addElement(new Variable(name,value));
		sort(lastSort);
	} //}}}

	//{{{ remove() method
	void remove(int index)
	{
		variables.removeElementAt(index);
		fireTableStructureChanged();
	} //}}}

	//{{{ toHashtable() method
	public Hashtable toHashtable()
	{
		Hashtable hash = new Hashtable();
		for(int i = 0; i < variables.size(); i++)
		{
			Variable variable = (Variable)variables.elementAt(i);
			if(variable.name.length() > 0
				&& variable.value.length() > 0)
			{
				hash.put(variable.name,variable.value);
			}
		}
		return hash;
	} //}}}

	//{{{ getColumnCount() method
	public int getColumnCount()
	{
		return 2;
	} //}}}

	//{{{ getRowCount() method
	public int getRowCount()
	{
		return variables.size();
	} //}}}

	//{{{ getValueAt() method
	public Object getValueAt(int row, int col)
	{
		Variable variable = (Variable)variables.elementAt(row);
		switch(col)
		{
		case 0:
			return variable.name;
		case 1:
			return variable.value;
		default:
			return null;
		}
	} //}}}

	//{{{ isCellEditable() method
	public boolean isCellEditable(int row, int col)
	{
		return true;
	} //}}}

	//{{{ setValueAt() method
	public void setValueAt(Object value, int row, int col)
	{
		if(value == null)
			value = "";

		Variable variable = (Variable)variables.elementAt(row);

		if(col == 0)
			variable.name = (String)value;
		else
			variable.value = (String)value;

		fireTableRowsUpdated(row,row);
	} //}}}

	//{{{ getColumnName() method
	public String getColumnName(int index)
	{
		switch(index)
		{
		case 0:
			return jEdit.getProperty("options.superabbrevs.variables.variable");
		case 1:
			return jEdit.getProperty("options.superabbrevs.variables.value");
		default:
			return null;
		}
	} //}}}

	//{{{ VariableCompare class
	class VariableCompare implements Comparator
	{
		//{{{ field int col
		private int col;
		/**
		 * Getter function for the field col
		 */ 
		public int getCol() {
			return col;
		}
		//}}}

		VariableCompare(int col)
		{
			this.col = col;
		}

		public int compare(Object obj1, Object obj2)
		{
			Variable v1 = (Variable)obj1;
			Variable v2 = (Variable)obj2;

			if(col == 0)
			{
				String name1 = v1.name.toLowerCase();
				String name2 = v2.name.toLowerCase();

				return StandardUtilities.compareStrings(
					name1,name2,true);
			}
			else
			{
				String value1 = v1.value.toLowerCase();
				String value2 = v2.value.toLowerCase();

				return StandardUtilities.compareStrings(
					value1,value2,true);
			}
			
			
		}
		
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof VariableCompare)) return false;
			
			VariableCompare variableCompare = (VariableCompare)obj;
			
			return col == variableCompare.col;
		}
	} //}}}
} //}}}

//{{{ Variable class
class Variable
{
	Variable() {}

	Variable(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	String name;
	String value;
} //}}}
