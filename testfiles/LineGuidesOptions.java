package org.jedit.plugins.columnruler;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.Log;

/**
 *  Option pane for custom marks and guides.
 *
 * @author     Brad Mace
 * @version    $Revision: 1.2 $ $Date: 2006-03-17 16:27:52 $
 */
public class LineGuidesOptions extends AbstractOptionPane implements ActionListener {
	private Map<DynamicMark, JCheckBox> dynamicMarks;
	private java.util.List<StaticMark> staticMarks;
	private JButton addButton;
	private JButton removeButton;
	private JTable table;
	private GuideTableModel model;
	private Comparator columnComparator;

	public LineGuidesOptions() {
		super("columnruler.lineguides");
	}

	protected void _init() {
		columnComparator = new ColumnComparator();
		dynamicMarks = new HashMap<DynamicMark, JCheckBox>();

		String[] services = ServiceManager.getServiceNames("org.jedit.plugins.columnruler.DynamicMark");
		JPanel dynamicGuides = new JPanel(new GridLayout(services.length, 1));
		dynamicGuides.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dynamic Marks/Guides"));
		
		for (String service : services) {
			DynamicMark m = (DynamicMark) ServiceManager.getService("org.jedit.plugins.columnruler.DynamicMark", service);
			JCheckBox box = new JCheckBox("Show "+m.getName()+" Mark");
			dynamicMarks.put(m, box);
			if (m.isVisible()) {
				box.setSelected(true);
			}
			dynamicGuides.add(box);
		}
		
		addComponent(dynamicGuides, GridBagConstraints.HORIZONTAL);

		MarkManager mm = MarkManager.getInstance();
		staticMarks = new ArrayList<StaticMark>();
		try {
			for (StaticMark m : mm.getMarks()) {
				staticMarks.add((StaticMark) m.clone());
			}
		} catch (CloneNotSupportedException e) {
			Log.log(Log.ERROR, this, "Couldn't load existing marks");
		}
		
		model = new GuideTableModel();
		table = new JTable(model);
		addComponent(createTablePanel(), GridBagConstraints.BOTH);
		
	}


	protected void _save() {
		for (DynamicMark mark : dynamicMarks.keySet()) {
			JCheckBox box = dynamicMarks.get(mark);
			mark.setVisible(box.isSelected());
			jEdit.setBooleanProperty(mark.getPropertyPrefix()+".visible", mark.isVisible());
		}

		MarkManager mm = MarkManager.getInstance();
		mm.removeAll();
		for (StaticMark m : staticMarks) {
			mm.addMark(m, false);
		}
		
		mm.save();
		jEdit.getActiveView().getTextArea().repaint();
	}

	//{{{ actionPerformed()
	public void actionPerformed(ActionEvent evt) {
		Object obj = evt.getSource();

		if (obj == addButton) {
			addGuide();
		} else if (obj == removeButton) {
			int[] rows = table.getSelectedRows();
			for (int i = rows.length-1; i >= 0; i--) {
				removeGuide(rows[i]);
			}
		}
	}//}}}

	private JPanel createTablePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Static Marks/Guides"));

		JPanel buttonPanel = new JPanel();
		addButton = new JButton("Add Mark/Guide");
		addButton.addActionListener(this);
		buttonPanel.add(addButton);
		removeButton = new JButton("Remove Mark(s)/Guide(s)");
		removeButton.addActionListener(this);
		buttonPanel.add(removeButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		return panel;
	}

	//{{{ add/remove guide
	public void addGuide() {
		MarkDialog dialog = new MarkDialog(null, "Add Mark/Guide");
		dialog.pack();
		dialog.setVisible(true);
	}

	public void removeGuide(int index) {
		staticMarks.remove(index);
		model.fireTableDataChanged();
	}
	//}}}

	//{{{ Inner Classes

	//{{{ GuideTableModel
	/**
	 *  Description of the Class
	 *
	 * @author     Brad Mace
	 * @version    $Revision: 1.2 $ $Date: 2006-03-17 16:27:52 $
	 */
	class GuideTableModel extends AbstractTableModel {
		public GuideTableModel() { }

		public String getColumnName(int col) {
			switch (col) {
							case 0:
								return "Name";
							case 1:
								return "Column";
							case 2:
								return "Color";
							default:
								return "error";
			}
		}

		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return staticMarks.size();
		}

		public Object getValueAt(int row, int col) {
			StaticMark mark = staticMarks.get(row);
			switch (col) {
							case 0:
								return mark.getName();
							case 1:
								return new Integer(mark.getColumn());
							case 2:
								return mark.getColor();
							default:
								return "error";
			}
		}
	}//}}}

	//{{{ ColumnComparator
	/**
	 *  Description of the Class
	 *
	 * @author     Brad Mace
	 * @version    $Revision: 1.2 $ $Date: 2006-03-17 16:27:52 $
	 */
	class ColumnComparator implements Comparator {
		public int compare(Object a, Object b) {
			StaticMark first = (StaticMark) a;
			StaticMark second = (StaticMark) b;
			return first.getColumn() - second.getColumn();
		}

		public boolean equals(Object other) {
			return other instanceof ColumnComparator;
		}
	}//}}}

	//}}}

}

