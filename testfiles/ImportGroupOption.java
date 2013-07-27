/*
 *  ImportGroupOption.java
 *  Copyright (C) 2002  Matthew Flower (MattFlower@yahoo.com)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package jimporter.grouping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import jimporter.JImporterPlugin;
import jimporter.options.JImporterOption;
import jimporter.options.JImporterOptionPane;
import jimporter.options.OptionSaveListener;
import org.gjt.sp.jedit.jEdit;

/**
 * This class is responsible for loading the value of all import groups from the
 * properties and managing the process of calling all imports groups with the
 * order to save themselves.
 *
 * @author Matthew Flower
 */
public class ImportGroupOption extends JImporterOption {
	final static String IMPORT_GROUP_VALUE_PREFIX = "jimporter.sorting.importgroup";
	private DefaultListModel listModel;
	private JList importGroup;

	/**
	 * This method creates a new ImportGroupOption instance.
	 */
	public ImportGroupOption() {
		super("");
	}

	/**
	 * Erase any existing import groups that are saved in the properties and ask
	 * all import groups to save themselves.
	 *
	 *@param importGroupItems A list of all group items that need to save themselves.
	 */
	public static void store(List importGroupItems) {
		//First, erase any existing stored groupings
		for (int i = 1; ; i++) {
			String test = jEdit.getProperty(IMPORT_GROUP_VALUE_PREFIX + ".list." + i + ".type");
			if (test != null) {
				jEdit.unsetProperty(IMPORT_GROUP_VALUE_PREFIX + ".list." + i + ".type");
				jEdit.unsetProperty(IMPORT_GROUP_VALUE_PREFIX + ".list." + i + ".value");
			} else {
				break;
			}
		}

		//Now store the properties
		Iterator groupingItems = importGroupItems.iterator();
		int j = 1;

		while (groupingItems.hasNext()) {
			ImportGroupItem igi = (ImportGroupItem) groupingItems.next();
			igi.store(j++);
		}
	}

	/**
	 * Load all of the import groups from the jEdit properties file.
	 *
	 *@return a <code>List</code> value containing all of the import group items.
	 */
	public static ImportGroupList load() {
		ImportGroupList importGroupList = new ImportGroupList();

		for (int i = 1; ; i++) {
			ImportGroupItem igi;
			String type = jEdit.getProperty(IMPORT_GROUP_VALUE_PREFIX + ".list." + i + ".type");

			if (type == null) {
				break;
			} else if (type.equals("package")) {
				igi = new PackageGroupItem(jEdit.getProperty(IMPORT_GROUP_VALUE_PREFIX + ".list." + i + ".value"));
			} else if (type.equals("whitespace")) {
				igi = new WhiteSpaceGroupItem();
			} else if (type.equals("allotherimports")) {
				igi = new AllOtherImportsItem();
			} else {
				break;
			}

			importGroupList.add(igi);
		}

		if (importGroupList.size() == 0) {
			importGroupList.add(new AllOtherImportsItem());
		}

		return importGroupList;
	}

	/**
	 * Construct the "options" pane for grouping.
	 *
	 * @param jiop An option pane that we are going to add ourselves to.
	 */
	public void createVisualPresentation(JImporterOptionPane jiop) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JLabel title = new JLabel("Import Grouping");
		jiop.addComponent(title);

		importGroup = new JList();
		importGroup.setBorder(new javax.swing.border.EtchedBorder());
		importGroup.setPreferredSize(new Dimension(200, 150));
		listModel = new DefaultListModel();
		importGroup.setModel(listModel);

		Iterator it = load().iterator();
		while (it.hasNext()) {
			listModel.addElement(it.next());
		}

		JPanel upDownPanel = new JPanel();
		upDownPanel.setLayout(new BoxLayout(upDownPanel, BoxLayout.Y_AXIS));
		upDownPanel.setBorder(new EmptyBorder(0,15,0,0));

		JButton sortUp = new JButton("Move up", new ImageIcon(JImporterPlugin.class.getResource("/images/up.gif")));
		sortUp.setHorizontalAlignment(SwingConstants.LEFT);
		sortUp.setPreferredSize(new Dimension(150, 28));
		sortUp.setMinimumSize(new Dimension(150, 28));
		sortUp.setMaximumSize(new Dimension(150, 28));
		sortUp.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					upClicked();
				}
			});

		JButton sortDown = new JButton("Move down", new ImageIcon(JImporterPlugin.class.getResource("/images/down.gif")));
		sortDown.setHorizontalAlignment(SwingConstants.LEFT);
		sortDown.setPreferredSize(new Dimension(150, 28));
		sortDown.setMinimumSize(new Dimension(150, 28));
		sortDown.setMaximumSize(new Dimension(150, 28));
		sortDown.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					downClicked();
				}
			});

		JButton addImport = new JButton("Add package", new ImageIcon(JImporterPlugin.class.getResource("/images/add.gif")));
		addImport.setHorizontalAlignment(SwingConstants.LEFT);
		addImport.setPreferredSize(new Dimension(150, 28));
		addImport.setMinimumSize(new Dimension(150, 28));
		addImport.setMaximumSize(new Dimension(150, 28));
		addImport.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addClicked();
				}
			});

		JButton removeImport = new JButton("Remove package", new ImageIcon(JImporterPlugin.class.getResource("/images/remove.gif")));
		removeImport.setHorizontalAlignment(SwingConstants.LEFT);
		removeImport.setPreferredSize(new Dimension(150, 28));
		removeImport.setMinimumSize(new Dimension(150, 28));
		removeImport.setMaximumSize(new Dimension(150, 28));
		removeImport.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					removeClicked();
				}
			});

		JButton addSpace = new JButton("Add Whitespace", new ImageIcon(JImporterPlugin.class.getResource("/images/addspace.gif")));
		addSpace.setHorizontalAlignment(SwingConstants.LEFT);
		addSpace.setPreferredSize(new Dimension(150, 28));
		addSpace.setMinimumSize(new Dimension(150, 28));
		addSpace.setMaximumSize(new Dimension(150, 28));
		addSpace.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					addSpaceClicked();
				}
			});

		upDownPanel.add(sortUp);
		upDownPanel.add(sortDown);
		upDownPanel.add(addImport);
		upDownPanel.add(removeImport);
		upDownPanel.add(addSpace);

		panel.add(importGroup, BorderLayout.WEST);
		panel.add(upDownPanel);
		jiop.addComponent(panel);

		jiop.addSaveListener(
			new OptionSaveListener() {
				public void saveChanges() {
					store(Arrays.asList(listModel.toArray()));
				}
			});
	}

	/**
	 * This method is called when someone clicks on the "Up" button in the import
	 * group option pane to make that group higher in the list.
	 */
	private void upClicked() {
		int selectedIndex = importGroup.getSelectedIndex();
		ImportGroupItem importToMove = (ImportGroupItem) listModel.elementAt(selectedIndex);

		//Make sure we aren't already at the top of the JList
		if (selectedIndex == 0) {
			return;
		} else {
			listModel.removeElementAt(importGroup.getSelectedIndex());
			listModel.add(selectedIndex - 1, importToMove);
			importGroup.setSelectedIndex(selectedIndex - 1);
		}
	}

	/**
	 * This method is called when someone clicks the "Down" button in the import
	 * list to make that group lower in the list.
	 */
	private void downClicked() {
		int selectedIndex = importGroup.getSelectedIndex();
		ImportGroupItem importToMove = (ImportGroupItem) listModel.elementAt(selectedIndex);

		//Make sure we aren't at the bottom of the list
		if (selectedIndex == listModel.size() - 1) {
			return;
		} else {
			listModel.removeElementAt(selectedIndex);
			listModel.add(selectedIndex + 1, importToMove);
			importGroup.setSelectedIndex(selectedIndex + 1);
		}
	}

	/**
	 * This method is called when someone clicks the "add" button to add a package
	 * name to the import group list.
	 */
	private void addClicked() {
		String inputString = JOptionPane.showInputDialog(importGroup, "Please enter an package name", "Add import group", JOptionPane.PLAIN_MESSAGE);

		if (inputString != null) {
			PackageGroupItem packagePattern = new PackageGroupItem(inputString);
			listModel.addElement(packagePattern);
		}
	}

	/**
	 * This method is called when someone clicks the "remove" button to remove
	 * a import group that is in the import group list.
	 */
	private void removeClicked() {
		ImportGroupItem ii = (ImportGroupItem)listModel.get(importGroup.getSelectedIndex());
		if (ii instanceof AllOtherImportsItem) {
			JOptionPane.showMessageDialog(null, "You cannot remove the default import grouping.",
				"Unable to remove grouping", JOptionPane.ERROR_MESSAGE);
			return;
		}

		listModel.remove(importGroup.getSelectedIndex());
	}

	/**
	 * This method is called when someone requests that a line of whitespace be
	 * added to the import group list.
	 */
	private void addSpaceClicked() {
		listModel.addElement(new WhiteSpaceGroupItem());
	}
}
