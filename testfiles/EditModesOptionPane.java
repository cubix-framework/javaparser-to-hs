/*
 * EditModesOptionPane.java - ShortcutSaver plugin's editmodes option pane
 *
 * Copyright (C) 2003, 2007 Carmine Lucarelli
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

package shortcutSaver;

import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;

public class EditModesOptionPane extends AbstractOptionPane
{
	private int paneNumber;
	
	public EditModesOptionPane(int paneNumber)
	{
		super("shortcutSaver.editmodes");
		this.paneNumber = paneNumber;
	}

	protected void _init()
	{
//		setLayout(new BorderLayout());
//		JPanel north = new JPanel(new GridLayout(2, 2, 5, 10));

		String label = jEdit.getProperty("options.shortcutSaver.editmodes.caption");
		String work = jEdit.getProperty("shortcutSaver.ActionLabel" + paneNumber);
		if(work == null)
			work = "Action " + paneNumber;
		menuName = new JTextField(work);
		menuName.setHorizontalAlignment(SwingConstants.LEFT);
		addComponent(label, menuName);
		
		label = jEdit.getProperty("options.shortcutSaver.editmodes.shortcut");
		work = jEdit.getProperty("shortcutSaver-action" + paneNumber + ".shortcut");
		if(work == null)
			work = "";
		JLabel current = new JLabel(work);
		current.setFont(new Font(current.getFont().getName(), Font.BOLD, current.getFont().getSize()));
		addComponent(label, current);

		addComponent(createModeTableScroller(), GridBagConstraints.BOTH);

/*		JLabel label = new JLabel(jEdit.getProperty(
			"options.shortcutSaver.editmodes.caption"));
		label.setBorder(new EmptyBorder(0,0,6,0));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		north.add(label);
		String work = jEdit.getProperty("shortcutSaver.ActionLabel" + paneNumber);
		if(work == null)
			work = "Action " + paneNumber;
		menuName = new JTextField(work);
		menuName.setHorizontalAlignment(SwingConstants.LEFT);
		north.add(menuName);

		label = new JLabel(jEdit.getProperty(
			"options.shortcutSaver.editmodes.shortcut"));
		label.setBorder(new EmptyBorder(0,0,6,0));
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		north.add(label);
		work = jEdit.getProperty("shortcutSaver-action" + paneNumber + ".shortcut");
		if(work == null)
			work = "";
		label = new JLabel(work);
		label.setBorder(new EmptyBorder(0,0,6,0));
		label.setHorizontalAlignment(SwingConstants.LEFT);
		north.add(label); */
		

		//add(BorderLayout.NORTH, north);
		//add(BorderLayout.CENTER,createModeTableScroller());
	}

	protected void _save()
	{
		jEdit.setProperty("shortcutSaver.ActionLabel" + paneNumber, menuName.getText());
		model.save();
	}

	private ModeTableModel model;
	private JTable table;
	private JTextField menuName;
	
	
	private JScrollPane createModeTableScroller()
	{
		model = new ModeTableModel(paneNumber);
		table = new JTable(model);
		table.getTableHeader().setReorderingAllowed(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.addMouseListener(new TableMouseHandler());

		Dimension d = table.getPreferredSize();
		d.height = Math.min(d.height,200);
		JScrollPane scroller = new JScrollPane(table);
		scroller.setPreferredSize(d);
		return scroller;
	}


	class TableMouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			int row = table.getSelectedRow();
			ShortcutsDialog dialog = new ShortcutsDialog(GUIUtilities.getParentDialog(
				EditModesOptionPane.this), (String)model.getValueAt(row, 0), (String)model.getValueAt(row, 1), 
				model.getIsChainedAt(row));
			if(dialog.isOK())
			{
				table.setValueAt(dialog.getAction(), row, 1);
				((ModeTableModel)table.getModel()).setChainedAt(row, dialog.isChained());
			}
		}
	}
} 

class ModeTableModel extends AbstractTableModel
{
	private ArrayList modes;
	private int paneNumber;

	ModeTableModel(int paneNumber)
	{
		this.paneNumber = paneNumber;

		Mode[] _modes = jEdit.getModes();
		modes = new ArrayList(_modes.length + 1);
		modes.add(new Entry("Default"));
		for(int i = 0; i < _modes.length; i++)
		{
			modes.add(new Entry(_modes[i].getName()));
		}
	} 

	public int getColumnCount()
	{
		return 2;
	} 

	public int getRowCount()
	{
		return modes.size();
	} 

	public Class getColumnClass(int col)
	{
		return String.class;
	} 

	public Object getValueAt(int row, int col)
	{
		Entry mode = (Entry)modes.get(row);
		switch(col)
		{
			case 0:
				return mode.name;
			case 1:
				return (mode.action == null ? "" : mode.action);
			default:
				throw new InternalError();
		}
	} 

	public boolean getIsChainedAt(int row)
	{
		Entry mode = (Entry)modes.get(row);
		return mode.chained;
	}
	
	public boolean isCellEditable(int row, int col)
	{
		return (col != 0);
	} 

	public void setValueAt(Object value, int row, int col)
	{
		if(col == 0)
			return;

		Entry mode = (Entry)modes.get(row);
		switch(col)
		{
			case 1:
				mode.action = (String)value;
				break;
			default:
				throw new InternalError();
		}

		fireTableRowsUpdated(row,row);
	} 

	public void setChainedAt(int row, boolean isChained)
	{
		Entry mode = (Entry)modes.get(row);
		mode.chained = isChained;
	}
	
	public String getColumnName(int index)
	{
		switch(index)
		{
			case 0:
				return jEdit.getProperty("options.shortcutSaver.editmodes.mode");
			case 1:
				return jEdit.getProperty("options.shortcutSaver.editmodes.actions");
			default:
				throw new InternalError();
		}
	}

	public void save()
	{
		for(int i = 0; i < modes.size(); i++)
		{
			((Entry)modes.get(i)).save();
		}
	}

	class Entry
	{
		String name;
		String action;
		boolean chained;
		String prefix;
		
		Entry(String name)
		{
			this.name = name;
			action = jEdit.getProperty(ActionUtils.PROP_PREFIX + paneNumber + "." + name, "");
			chained = jEdit.getBooleanProperty(ActionUtils.PROP_PREFIX + paneNumber + "." + 
				name + ".chained", false);
			prefix = ActionUtils.PROP_PREFIX + paneNumber + "." + name;
		}
		
		void save()
		{
			if(action.length() > 0)
				jEdit.setProperty(prefix, action);
			else
				jEdit.unsetProperty(prefix);
			if(chained)
				jEdit.setBooleanProperty(prefix + ".chained", true); 
			else
				jEdit.unsetProperty(prefix + ".chained");
		}
	}
}
