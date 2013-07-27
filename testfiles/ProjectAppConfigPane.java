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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.AbstractTableModel;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;

import common.gui.FileTextField;

import projectviewer.gui.OptionPaneBase;
//}}}

/**
 *	Option pane for the external applications configuration.
 *
 *	@author		Matthew Payne
 *	@version	$Id: ProjectAppConfigPane.java 14837 2009-03-23 05:09:09Z vanza $
 */
public class ProjectAppConfigPane extends OptionPaneBase
							  	  implements ActionListener {

 	//{{{ Private members
	private JButton cmdAdd;
	private JButton cmdDelete;

	private FileTextField appField;
	private JTextField extField;

	private JTable appTable;
	private AppLauncher apps;
	private AppModel model;

	private int editingRow;
	//}}}


	public ProjectAppConfigPane()
	{
		super("projectviewer.optiongroup.external_apps",
			  "projectviewer.options.extenal_apps");
	}


	//{{{ #_init() : void
	protected void _init() {

		setLayout(new BorderLayout());

		JPanel input = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		input.setLayout(gb);

		JLabel extLabel = createLabel("extensions");
		JLabel appLabel = createLabel("application");

		appField = new FileTextField(true);
		extField = new JTextField();

		cmdAdd = new JButton("+");
		cmdAdd.addActionListener(this);
		cmdAdd.setToolTipText(jEdit.getProperty("projectviewer.common.add"));

		cmdDelete = new JButton("-");
		cmdDelete.addActionListener(this);
		cmdDelete.setToolTipText(jEdit.getProperty("projectviewer.common.delete"));

		// first line: labels

		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gb.setConstraints(extLabel, gbc);
		input.add(extLabel);

		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.gridx = 1;
		gbc.weightx = 2.0;
		gb.setConstraints(appLabel, gbc);
		input.add(appLabel);

		// second line: text fields and buttons

		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gb.setConstraints(extField, gbc);
		input.add(extField);

		gbc.gridx = 1;
		gbc.weightx = 2.0;
		gb.setConstraints(appField, gbc);
		input.add(appField);

		gbc.gridx = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gb.setConstraints(cmdAdd, gbc);
		input.add(cmdAdd);

		gbc.gridx = 3;
		gb.setConstraints(cmdDelete, gbc);
		input.add(cmdDelete);

		add(BorderLayout.NORTH, input);

		// third line: table
		apps = new AppLauncher();
		apps.copy(AppLauncher.getInstance());
		model = new AppModel(apps);
		appTable = new JTable(model);
		appTable.addMouseListener(new MouseHandler());

  		TableColumn col = appTable.getColumnModel().getColumn(1);
		col.setPreferredWidth(appTable.getColumnModel().getColumn(0).getWidth() * 5);
		JScrollPane jsp = new JScrollPane(appTable);
		add(BorderLayout.CENTER, jsp);

		this.editingRow = -1;
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == cmdAdd) {
			if (extField.getText().length() >= 1 &&
				appField.getTextField().getText().length() >=1)
			{
				if (this.editingRow > -1)
					deleteRow();
				apps.addAppExt(extField.getText(),
						replaceString(appField.getTextField().getText(), "\\", "/"));
				extField.setText("");
				appField.getTextField().setText("");
				model.requestRefresh();
			} else {
				this.editingRow = -1;
			}
		} else if (ae.getSource() == cmdDelete) {
			deleteRow();
		}

	} //}}}

	//{{{ -_replaceString(String, String, String)_ : String
	private static String replaceString(String aSearch, String aFind, String aReplace)
	{
		String result = aSearch;
		if (result != null && result.length() > 0) {
			int a = 0;
			int b = 0;
			while (true) {
				a = result.indexOf(aFind, b);
				if (a != -1) {
					result = result.substring(0, a) + aReplace + result.substring(a + aFind.length());
					b = a + aReplace.length();
				}
				else
				break;
			}
		}
		return result;
	} //}}}

	//{{{ -deleteRow() : void
	private void deleteRow() {
		// Deletes a row from the Table:
		int targetRow = -1;
		if (this.editingRow > -1) {
			targetRow = this.editingRow;
			this.editingRow = -1;
		} else if (appTable.getSelectedRowCount() > 0) {
			targetRow = appTable.getSelectedRow();
		}
		if (targetRow > -1) {
			Object keyCol = appTable.getValueAt(targetRow, 0);
			apps.removeAppExt(keyCol);
			model.requestRefresh();
		}
	} //}}}

	//{{{ +_save() : void
	public void _save()
	{
		try {
			AppLauncher launcher = AppLauncher.getInstance();
			launcher.copy(apps);
			launcher.storeExts();
		} catch (java.io.IOException e) {
			Log.log(Log.ERROR, this, e);
		}
	}//}}}

	//{{{ -class _AppModel_
	private static class AppModel extends AbstractTableModel {

		//{{{ +AppModel(AppLauncher) : <init>
		/**
		 * Constructs an AppList table model.
		 * @param appAssoc  the collection of extentions and associations
		 */
		public AppModel(AppLauncher appList) {
			appSet = appList.getAppList();
		} //}}}

		//{{{ +getRowCount() : int
		public int getRowCount() {
			return appSet.size();
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

			Iterator iter = appSet.iterator();
			int iCurrentRow = 0;

			while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry)iter.next();
				if (iCurrentRow == r)
				switch(c) {
					case 0: return entry.getKey();
					case 1: return entry.getValue();
				}
				Object key = entry.getKey();
				Object value = entry.getValue();
				iCurrentRow++;
			}
			return jEdit.getProperty("projectviewer.appconfig.no_value");
		} //}}}

		//{{{ +getColumnName(int) : String
		public String getColumnName(int c) {
			return (c == 0) ?
				jEdit.getProperty("projectviewer.appconfig.extension") :
				jEdit.getProperty("projectviewer.appconfig.application");
		} //}}}

		private Set appSet;

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
				int sel = appTable.rowAtPoint(me.getPoint());
				if (sel > -1) {
					editingRow = sel;
					extField.setText(appTable.getValueAt(sel, 0).toString());
					appField.getTextField().setText(appTable.getValueAt(sel, 1).toString());
				}
			}
		} //}}}

	} //}}}

}

