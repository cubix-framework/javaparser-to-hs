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
package projectviewer.config;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;

import java.util.ArrayList;
import java.util.List;

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;

import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;
import projectviewer.gui.OptionPaneBase;
import projectviewer.vpt.VPTFilterData;
import projectviewer.vpt.VPTProject;
//}}}

/**
 *	Option pane for the filter configuration.
 *
 *	@author		Rudolf Widmann, reused code from Matthew Payne
 *	@version	$Id: ProjectFilterPane.java 14937 2009-04-16 04:39:42Z vanza $
 */
public class ProjectFilterPane extends OptionPaneBase
							   implements ActionListener
{

	//{{{ Static constants

	private final static String EXTENSIONS_TEXT  = "Filter:";
	private final static String APPLICATION_TEXT = "Name:";
	private final static int MOVE_ROW_UP = -1;
	private final static int MOVE_ROW_DOWN = 1;

	//}}}

 	//{{{ Private members
	private JButton cmdAdd;
	private JButton cmdDelete;

	private JButton cmdDown;
	private JButton cmdUp;

	private JTextField filterName;
	private JTextField extField;

	private JTable filterTable;
	private FilterTabelModel model;

	private int editingRow;
	private VPTProject proj;
	//}}}

	public ProjectFilterPane(VPTProject proj)
	{
		super("projectviewer.optiongroup.filter",
			  "projectviewer.filterconfig");
		this.proj = proj;
	}

	//{{{ #_init() : void
	protected void _init() {
		setLayout(new BorderLayout());

		// First and second lines: labels and text fiels
		JPanel input = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		input.setLayout(gb);

		JLabel extLabel = createLabel("extension");
		JLabel filterLabel = createLabel("filter");

		filterName = new JTextField();
		extField = new JTextField();

		// TODO: add ignoreCase-checkbox
		// TODO: implement ignoreCase-filtering

		cmdAdd = new JButton("+");
		cmdAdd.addActionListener(this);
		cmdAdd.setToolTipText(jEdit.getProperty("projectviewer.common.add"));

		cmdDelete = new JButton(jEdit.getProperty("projectviewer.common.delete"));
		cmdDelete.addActionListener(this);

		cmdDown = new JButton(prop("down"));
		cmdUp = new JButton(prop("up"));

		// first line: labels

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 1.0;
		gb.setConstraints(extLabel, gbc);
		input.add(extLabel);

		gbc.gridx++;
		gbc.weightx = 2.0;
		gb.setConstraints(filterLabel, gbc);
		input.add(filterLabel);

		// second line: text fields and buttons

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gb.setConstraints(extField, gbc);
		input.add(extField);


		gbc.gridx++;
		gb.setConstraints(filterName, gbc);
		input.add(filterName);

		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0.0;
		gbc.gridx++;
		gb.setConstraints(cmdAdd, gbc);
		input.add(cmdAdd);

		add(BorderLayout.NORTH, input);

		// Center widget: table

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;

		// load global filter list
		if (proj == null) {
			model = new FilterTabelModel(ProjectManager.getInstance().getGlobalFilterList());
		} else {
			model = new FilterTabelModel(proj.getFilterList());
		}
		filterTable = new JTable(model);
		filterTable.addMouseListener(new MouseHandler());

  		TableColumn col = filterTable.getColumnModel().getColumn(1);
		col.setPreferredWidth(filterTable.getColumnModel().getColumn(0).getWidth() * 5);
		JScrollPane jsp = new JScrollPane(filterTable);
		gb.setConstraints(jsp, gbc);
		add(BorderLayout.CENTER, jsp);

		// last line: down/up
		JPanel upDownPane = new JPanel(new FlowLayout());
		upDownPane.add(cmdDelete);
		upDownPane.add(cmdDown);
		upDownPane.add(cmdUp);

		add(BorderLayout.SOUTH, upDownPane);

		editingRow = -1;
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == cmdAdd) {
			if (extField.getText().length() >= 1 && filterName.getText().length() >=1) {
				int index = filterTable.getSelectedRow();
				if (index == -1)
					index = filterTable.getRowCount();
				model.addRow(index, new VPTFilterData(filterName.getText(), extField.getText()));
				extField.setText("");
				filterName.setText("");
				model.requestRefresh();
			 }
		} else if (ae.getSource() == cmdDelete) {
			if (editingRow > -1) {
				model.deleteRow(editingRow);
				editingRow = -1;
			} else {
				model.deleteRow(filterTable.getSelectedRow());
			}
			model.requestRefresh();
		} else if (ae.getSource() == cmdUp) {
			model.moveRow(filterTable.getSelectedRow(), MOVE_ROW_UP);
			model.requestRefresh();
		} else if (ae.getSource() == cmdDown) {
			model.moveRow(filterTable.getSelectedRow(), MOVE_ROW_DOWN);
			model.requestRefresh();
		}

	} //}}}

	//{{{ +_save() : void
	public void _save()
	{
		if (proj == null) {
			ProjectManager.getInstance().setGlobalFilterList(model.getFilterList());
		} else {
			proj.setFilterList(model.getFilterList());
			ProjectViewer.nodeStructureChangedFlat(proj);
		}
	}//}}}

	//{{{ -class _FilterTabelModel_
	private static class FilterTabelModel extends AbstractTableModel {

		//{{{ +FilterTabelModel(List) : <init>
		/**
		 * @param appAssoc  the collection of extentions and associations
		 */
		public FilterTabelModel(List<VPTFilterData> filterList)
		{
			List<VPTFilterData> empty = java.util.Collections.emptyList();
			if (filterList == empty) {
				this.filterList = new ArrayList<VPTFilterData>();
			} else {
				this.filterList = filterList;
			}
		} //}}}

		//{{{ +getFilterList() : List
		public List<VPTFilterData> getFilterList() {
			return filterList;
		} //}}}

		//{{{ +addRow(int, VPTFilterData) : void
		public void addRow(int index, VPTFilterData row) {
			filterList.add(index, row);
		} //}}}

		//{{{ +deleteRow(int) : void
		public void deleteRow(int rowIndex) {
			if (rowIndex >= filterList.size() || rowIndex < 0)
				Log.log(Log.DEBUG, ProjectFilterPane.class,"+++ .312: index too high: rowIndex = "+rowIndex);
			else
				filterList.remove(rowIndex);
		} //}}}

		//{{{ +moveRow(int, int) : void
		public void moveRow(int rowIndex, int increment) {
			if (rowIndex >= filterList.size() || rowIndex < 0)
				Log.log(Log.DEBUG, ProjectFilterPane.class,"+++ .362: index out of range: rowIndex = "+rowIndex);
			else
			{
				int newIndex = rowIndex + increment;
				if (newIndex < filterList.size() && newIndex >= 0)
				{
					VPTFilterData moveEntry = filterList.get(rowIndex);
					filterList.remove(rowIndex);
					filterList.add(newIndex, moveEntry);
				}
			}
		} //}}}

		//{{{ +getRowCount() : int
		public int getRowCount() {
			return filterList.size();
		} //}}}

		//{{{ +requestRefresh() : void
		public void requestRefresh() {
			/* Used to refresh the table */
			super.fireTableDataChanged();
		} //}}}

		//{{{ +getColumnCount() : int
		public int getColumnCount() {
			return 2;
		} //}}}

		//{{{ +getValueAt(int, int) : Object
		public Object getValueAt(int r, int c) {
			VPTFilterData fd = filterList.get(r);
				switch(c) {
					case 0: return fd.getGlob();
					case 1: return fd.getName();
				}
			return jEdit.getProperty("projectviewer.filterconfig.no_value");
		} //}}}

		//{{{ +getColumnName(int) : String
		public String getColumnName(int c) {
			return (c == 0) ?
				jEdit.getProperty("projectviewer.filterconfig.extension") :
				jEdit.getProperty("projectviewer.filterconfig.filter");
		} //}}}

		private List<VPTFilterData> filterList;

	} //}}}

	//{{{ -class MouseHandler
	private class MouseHandler extends MouseAdapter {

		//{{{ +mouseClicked(MouseEvent) : void
		public void mouseClicked(MouseEvent me) {
			if (SwingUtilities.isLeftMouseButton(me)
				&& me.getClickCount() == 2)
			{
				// edit the current line; this means setting a variable
				// that says "this is the row we're editing" and then
				// filling the fields with the info.
				int sel = filterTable.rowAtPoint(me.getPoint());
				if (sel > -1) {
					editingRow = sel;
					extField.setText(filterTable.getValueAt(sel, 0).toString());
					filterName.setText(filterTable.getValueAt(sel, 1).toString());
				}
			}
		} //}}}

	} //}}}

}

