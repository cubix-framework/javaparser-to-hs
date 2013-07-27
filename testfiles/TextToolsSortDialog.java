/*
 * TextToolsSortDialog.java - sort options, adapted version from Carmine Lucarelli
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2002 Rudolf Widmann
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


//{{{ Imports
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.gui.EnhancedDialog;
//}}}

/**
 * An option dialog for JSort options.
 * @author Carmine Lucarelli
 */
public class TextToolsSortDialog extends EnhancedDialog implements ActionListener
{

	//{{{ TextToolsSortDialog constructor
	/**
	  * Constructor.  Sets up and shows the GUI
	  */
	public TextToolsSortDialog(View view, JEditTextArea textArea)
	{
		super(view, jEdit.getProperty("text-tools.sortadvanced.label"), false);
			
		this.view = view;
		this.textArea = textArea;
//		this.data = data;
//		this.selection = selection;
		
		view.showWaitCursor();
		
		sortTableModel = new SortTableModel();
		
		// preset sortTable if there is a rect selection
		boolean rectSel = false;
		int[] selRows =	TextToolsSorting.getRectSelectionRows(textArea);
		if (selRows != null) {
			// we have rectangular selection: assign values to 1st row of table
			sortTableModel.setValueAt(new Integer(selRows[0]+1), 0, 0);
			sortTableModel.setValueAt(new Integer(selRows[1]+1), 0, 1);
			rectSel = true;
		}
		
		sortTable = new JTable(sortTableModel);
		TableColumnModel cMod = sortTable.getColumnModel();
		sortTable.setTableHeader((new SortTableHeader(cMod)));
		sortTable.setRowHeight(25);
		
		sortTable.setPreferredScrollableViewportSize(new Dimension(430, 200));
		
		JScrollPane scroll = new JScrollPane(sortTable);
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(5,8,8,8));
		content.setLayout(new BorderLayout());
		setContentPane(content);
		content.add(scroll, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(12,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());
		
		ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(this);
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(6));
		getRootPane().setDefaultButton(ok);
		
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		buttons.add(Box.createHorizontalStrut(6));
		
		clear = new JButton("Clear");
		clear.addActionListener(this);
		buttons.add(clear);
		buttons.add(Box.createHorizontalStrut(6));
		
		help = new JButton("Help");
		help.addActionListener(this);
		buttons.add(help);
		buttons.add(Box.createHorizontalStrut(6));
		
		buttons.add(Box.createGlue());
		
		content.add(buttons, BorderLayout.SOUTH);
		
		delDupsCheckBox = new JCheckBox(
			jEdit.getProperty("text-tools.sortadvanced.delete-identic-lines"));
		onlySelectionCheckBox = new JCheckBox(
			jEdit.getProperty("text-tools.sortadvanced.sort-only-selection"));
			
		/*
		dontSortCheckBox = new JCheckBox(
			jEdit.getProperty("text-tools.sortadvanced.dont-sort"));
		delDupsCheckBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dontSortCheckBox.setEnabled(delDupsCheckBox.isSelected());
				dontSortCheckBox.setSelected(false);
			}
		});
		dontSortCheckBox.setEnabled(false);
		*/
		JPanel checkBoxes = new JPanel();
		if (rectSel) checkBoxes.add(onlySelectionCheckBox);
		checkBoxes.add(delDupsCheckBox);
		// checkBoxes.add(dontSortCheckBox);  not used, this is an extra action
		content.add(checkBoxes, BorderLayout.NORTH);
		view.hideWaitCursor();
		pack();
		GUIUtilities.loadGeometry(this, "texttools-sort-control");
		setLocationRelativeTo(view);
		setVisible(true);
	} //}}}
	
	//{{{ EnhancedDialog methods
	
	//{{{ ok() method
	public void ok()
	{
		ok(true);
	} //}}}
	
	//{{{ cancel() method
	public void cancel()
	{
		dispose();
	}//}}}

	//}}}
	
	//{{{ ok() method
	/**
	  * run the sort
	  */
	public void ok(boolean dispose)
	{
		jsort = new JSort(delDupsCheckBox.isSelected());
		Log.log(Log.DEBUG, this, " delDups is set to " + delDupsCheckBox.isSelected());

		for(int i = 0; i < 10; i++)
		{
			int start = ((Integer)sortTableModel.getValueAt(i, 0)).intValue();
			int end = ((Integer)sortTableModel.getValueAt(i, 1)).intValue();
			if (onlySelectionCheckBox.isSelected()) {
				start = 0;
				end = 10000;  // whole selection will be sorted
			}
			boolean ascending = !((Boolean)sortTableModel.getValueAt(i, 2)).booleanValue();
			boolean ignoreCase = ((Boolean)sortTableModel.getValueAt(i, 3)).booleanValue();
			boolean textType = ((Boolean)sortTableModel.getValueAt(i, 4)).booleanValue();
			boolean trim = ((Boolean)sortTableModel.getValueAt(i, 5)).booleanValue();
			boolean delDupRange = ((Boolean)sortTableModel.getValueAt(i, 6)).booleanValue();
			if(end == 0 && ascending && !ignoreCase && !textType && !trim )
			{
				break;
			}
			Log.log(Log.DEBUG, this, "Sort constraint: " + start + " " + end + " " + ascending
				+ " " + ignoreCase + " " + textType + " " + trim);
			jsort.addSortConstraint(start, end, ascending, ignoreCase, textType, trim, delDupRange);
		}
		
		if (!TextToolsSorting.sortAndInsertData(textArea, jsort,
			onlySelectionCheckBox.isSelected()))
			return;
		/* moved to textToolsSorting
		data = TextToolsPlugin.getData(textArea, onlySelectionCheckBox.isSelected());
		// Macros.message(view,"data got");
		
    if(data == null)
    {
      // an error occurred, so ditch
      return;
    }
    else if(data.size() == 1)
    {
      // lets not sort a single record
      return;
    }
    boolean selection = false;
    if(textArea.getSelectedLines().length > 1)
    {
      selection = true;
    }

		//display data
		Iterator di = data.iterator();
		while(di.hasNext())
		{
Log.log(Log.DEBUG, BeanShell.class,"TextToolsSortDialog.231: di.next() = "+di.next());
		}
		
 		jsort.sort(data);
		Iterator iter = data.iterator();
		StringBuffer sb = new StringBuffer();
		while(iter.hasNext())
		{
			sb.append(iter.next() + "\n");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		if(selection)
		{
			textArea.setSelectedText(sb.toString());
		}
		else
		{
			textArea.setText(sb.toString());
		}
		*/

		GUIUtilities.saveGeometry(this, "texttools-sort-control");

		// get rid of this dialog if necessary
		if (dispose)
			dispose();
	}  //}}}
	
	//{{{ actionPerformed() method
	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();

		if (source == ok)
		{
			ok();
		}
		else if (source == cancel)
		{
			cancel();
		}
		else if (source == clear)
		{
			sortTableModel.clear();
		}
		else if(source == help)
		{
			showHelp();
		}
		/*
		else if(source == delDupsCheckBox)
		{
			if (delDupsCheckBox.isSelected())
				dontSortCheckBox.setEnabled(true);
			else {
				dontSortCheckBox.setEnabled(false);
				dontSortCheckBox.setSelected(false);
			}
		}
		*/
	} //}}}
	
	//{{{ showHelp() method
	/**
	* Shows JSort help in the jEdit help viewer
	*/
	private void showHelp()
	{
		java.net.URL helpUrl = TextToolsSortDialog.class.getResource("TextTools.html");
		if (helpUrl == null)
		{
			Log.log(Log.NOTICE, this, "Help URL is null, cannot display help");
		}
		else 
		{
			new org.gjt.sp.jedit.help.HelpViewer(helpUrl.toString());
		}
	} //}}}
	
	//{{{ SortTableModel class
	/**
	  * Our table model
	  */
	private class SortTableModel extends AbstractTableModel
	{
		//{{{ SortTableModel constructor
		public SortTableModel()
		{
			columnNames = new String[7];
			columnNames[0] = "Start";
			columnNames[1] = "End";
			columnNames[2] = "Desc";
			columnNames[3] = "ICase";
			columnNames[4] = "Numeric";
			columnNames[5] = "Trim";
			columnNames[6] = "DelDups";
			
			clear();
		}//}}}
		
		//{{{ clear() method
		public void clear()
		{
			startColumns = new int[10];
			endColumns = new int[10];
			ignoreCases = new boolean[10];
			directions = new boolean[10];
			types = new boolean[10];
			trims = new boolean[10];
			delDups = new boolean[10];
			fireTableDataChanged();
		} //}}}
		
		//{{{ setValueAt() method
		public void setValueAt(Object value, int row, int col)
		{
			switch(col)
			{
				case 0:
					startColumns[row] = ((Integer)value).intValue();
					break;
				case 1:
					endColumns[row] = ((Integer)value).intValue();
					break;
				case 2:
					directions[row] = ((Boolean)value).booleanValue();
					break;
				case 3:
					ignoreCases[row] = ((Boolean)value).booleanValue();
					break;
				case 4:
					types[row] = ((Boolean)value).booleanValue();
					break;
				case 5:
					trims[row] = ((Boolean)value).booleanValue();
					break;
				case 6:
					delDups[row] = ((Boolean)value).booleanValue();
					break;
			}
			fireTableCellUpdated(row, col);
		} //}}}
		
		//{{{ getColumnCount() method
		public int getColumnCount()
		{
			return columnNames.length;
		} //}}}
		
		//{{{ getRowCount() method
		public int getRowCount()
		{
			return startColumns.length;
		} //}}}
		
		//{{{ getColumnName() method
		public String getColumnName(int col)
		{
			return columnNames[col];
		} //}}}
		
		//{{{ getValueAt() method
		public Object getValueAt(int row, int col)
		{
			switch(col)
			{
				case 0:
					return new Integer(startColumns[row]);
				case 1:
					return new Integer(endColumns[row]);
				case 2:
					return new Boolean(directions[row]);
				case 3:
					return new Boolean(ignoreCases[row]);
				case 4:
					return new Boolean(types[row]);
				case 5:
					return new Boolean(trims[row]);
				case 6:
					return new Boolean(delDups[row]);
			}
			return null;
		} //}}}
		
		//{{{ getColumnClass() method
		/**
		 *  JTable uses this method to determine the default renderer/
		 *  editor for each cell.  If we didn't implement this method,
		 *  then the last column would contain text ("true"/"false"),
		 *  rather than a check box.
		 */
		public Class getColumnClass(int c)
		{
			return getValueAt(0, c).getClass();
		} //}}}
		
		//{{{ isCellEditable() method
		/*
		 *  Don't need to implement this method unless your table's
		 *  editable.
		 */
		public boolean isCellEditable(int row, int col)
		{
			return true;
		} //}}}
		
		//{{{ Private members
		private String[] columnNames;
		private int[] startColumns;
		private int[] endColumns;
		private boolean[] ignoreCases;
		private boolean[] directions;
		private boolean[] types;
		private boolean[] trims;
		private boolean[] delDups;
		//}}}
		
	}  //}}}
	
	//{{{ SortTableHeader class
	private class SortTableHeader extends JTableHeader
	{
		//{{{ SortTableHeader constructor
		public SortTableHeader(TableColumnModel tcom)
		{
			super(tcom);
		} //}}}
		
		//{{{ getToolTipText()
		public String getToolTipText(MouseEvent event)
		{
			Point p = event.getPoint();
			switch(this.columnAtPoint(p))
			{
				case 0:
					return "Start column for the sort field";
				case 1:
					return "End column for the sort field";
				case 2:
					return "Enable for descending sort order";
				case 3:
					return "Enable for case insensitive sort";
				case 4:
					return "Enable for numeric type sort";
				case 5:
					return "Trim whitespace before sorting";
			}
			return null;
		}//}}}
	} //}}}
	
	//{{{ Private members
	/**
	  * The class with the sort routines.
	  */
	private JSort jsort;
	
	/**
	  * The data to be sorted
	  */
	private java.util.List data;
	
	/**
	  * The view containing the buffer that contained the data to be sorted (whew!!)
	  */
	private View view;
	
	/**
	  * The text area we'll write the results to
	  */
	private JEditTextArea textArea;
	
	/**
	  * Did the data come from a selection within the buffer?
	  */
	private boolean selection;

	// gui components
	private JCheckBox onlySelectionCheckBox;
	private JCheckBox dontSortCheckBox;  // used if you only want to delete duplicates
	private JCheckBox delDupsCheckBox;
	private JButton ok;
	private JButton cancel;
	private JButton clear;
	private JButton help;
	private SortTableModel sortTableModel;
	private JTable sortTable;

	//}}}
}