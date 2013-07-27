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
package launcher.keyword;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import launcher.Launcher;
import launcher.LauncherType;
import launcher.LauncherTypeOptionPane;

import org.gjt.sp.jedit.jEdit;

//}}}

/**
 * Option pane for the external applications configuration.
 * 
 * @author Matthew Payne, Francois Rey
 */
public class KeywordSearchLauncherTypeOptionPane extends
		LauncherTypeOptionPane implements ActionListener {

	// {{{ .props Properties
	public static final String OPT_PREFIX = LauncherType.OPT_BASE_PREFIX
			+ KeywordSearchLauncherType.INSTANCE.getPropertyPrefix();
	public static final String OPT_URL_FORMAT = OPT_PREFIX + ".url-format";
	public static final String OPT_LABEL_FORMAT = OPT_PREFIX + ".label-format";
	public static final String PROP_NO_VALUE = OPT_PREFIX + ".no-value"
			+ LABEL_SUFFIX;
	// }}}

	// {{{ Private members
	private JComboBox browserLauncherChoice;

	private JButton cmdAdd;
	private JButton cmdDelete;

//	private FileTextField urlFormatField;
//	private JTextField labelFormatField;

	private JTable table;
	private DefaultTableModel tableModel;

	// }}}

	public KeywordSearchLauncherTypeOptionPane() {
		super(KeywordSearchLauncherType.INSTANCE.getPropertyPrefix());
	}

	// {{{ #_init() : void
	protected void _init() {

		// Default launcher
		browserLauncherChoice =
			addDefaultLauncherComboBox(KeywordSearchLauncherType.INSTANCE);

		// Table
		JPanel keywordSearchTablePane = buildKeywordSearchTablePane();
		addComponent(keywordSearchTablePane, GridBagConstraints.BOTH);

		loadKeywordSearches();

	} // }}}

	private JPanel buildKeywordSearchTablePane() {
		JPanel keywordSearchTablePane = new JPanel();
		keywordSearchTablePane.setLayout(new BorderLayout());
		
		JPanel input = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		input.setLayout(gb);

		cmdAdd = createButton(OPT_ADD);
		cmdAdd.addActionListener(this);

		cmdDelete = createButton(OPT_DELETE);
		cmdDelete.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gb.setConstraints(cmdAdd, gbc);
		input.add(cmdAdd);

		gbc.gridx = 1;
		gb.setConstraints(cmdDelete, gbc);
		input.add(cmdDelete);

		keywordSearchTablePane.add(BorderLayout.NORTH, input);

		table = new JTable();
		table.setAutoCreateRowSorter(true);
		JScrollPane jsp = new JScrollPane(table);
		keywordSearchTablePane.add(BorderLayout.CENTER, jsp);
		
		return keywordSearchTablePane;
	}


	// {{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == cmdAdd) {
			tableModel.addRow(new Object[]{"",""});
		} else if (ae.getSource() == cmdDelete) {
			int selected = table.getSelectedRow(); 
			if (selected >= 0)
				tableModel.removeRow(selected);
		}	} // }}}

	// {{{ +_save() : void
	public void _save() {
		KeywordSearchLauncherType.INSTANCE
				.setDefaultLauncher((Launcher) browserLauncherChoice
						.getSelectedItem());
		saveKeywordSearches();
	}// }}}

	public void loadKeywordSearches() {

		Properties keywordSearches = 
				KeywordSearchLauncherType.INSTANCE.loadKeywordSearches();
		if (keywordSearches == null)
			return;
		List<Object[]> rows = new ArrayList<Object[]>();
		for (Object _key : keywordSearches.keySet()) {
			String labelFormat = (String) _key;
			String urlFormat = keywordSearches.getProperty(labelFormat);
			rows.add(new Object[]{labelFormat, urlFormat});
		}

		Object[] columnNames = new String[]{
				jEdit.getProperty(OPT_LABEL_FORMAT + LABEL_SUFFIX, "Missing label prop"),
				jEdit.getProperty(OPT_URL_FORMAT + LABEL_SUFFIX, "Missing label prop")};
		
		Object[][] rowsArray = new Object[keywordSearches.size()][2];
		tableModel =
			new DefaultTableModel(rows.toArray(rowsArray), columnNames);
		
		table.setModel(tableModel);
	} // }}}

	//{{{ +saveKeywordSearches() : void
	public void saveKeywordSearches()  {
		Properties keywordSearches = new Properties();
		TableModel model = table.getModel();
		for (int i = 0; i < model.getRowCount(); i++) {
			String labelFormat =
				model.getValueAt(i, 0).toString();
			String urlFormat =
				model.getValueAt(i, 1).toString();
			keywordSearches.put(labelFormat, urlFormat);
		}
		KeywordSearchLauncherType.INSTANCE
			.saveKeywordSearches(keywordSearches);
		
	} //}}}

}
