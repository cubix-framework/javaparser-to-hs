package sn;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.gjt.sp.jedit.View;

import sn.RefByDbAccess.RefByRecord;
import sn.RefByDbAccess.RefByRecordHandler;

import common.gui.HelpfulJTable;

@SuppressWarnings("serial")
public class RefByList extends JPanel {

	private View view;
	private JTextField text;
	private HelpfulJTable table;
	private SourceElementTableModel model;
	
	private class SourceElementTableModel extends AbstractTableModel {
		private final String [] Columns = { "Type", "Name", "Signature", "Kind", "Location" };
		private Vector<SourceElement> elements;

		public SourceElementTableModel() {
			elements = new Vector<SourceElement>();
		}
		public SourceElement getElement(int rowIndex) {
			if (rowIndex < 0 || rowIndex >= getRowCount())
				return null;
			return elements.get(rowIndex);
		}
		public void clear() {
			elements.clear();
		}
		public void addElement(SourceElement element) {
			elements.add(element);
		}
		public int getColumnCount() {
			return Columns.length;
		}
		public String getColumnName(int columnIndex) {
			return Columns[columnIndex];
		}
		public int getRowCount() {
			return elements.size();
		}
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex < 0 || rowIndex >= elements.size() ||
				columnIndex < 0 || columnIndex >= getColumnCount())
			{
				return null;
			}
			SourceElement element = elements.get(rowIndex);
			switch (columnIndex) {
			case 0: return element.namespace;
			case 1: return element.name;
			case 2: return element.sig;
			case 3: return element.kind;
			case 4: return element.getLocation();
			}
			return null;
		}
	}
	public RefByList(View view) {
		super(new BorderLayout());
		this.view = view;
		model = new SourceElementTableModel();
		table = new HelpfulJTable();
		table.setModel(model);
		table.setAutoResizeWithHeaders(true);
		table.setRowSelectionAllowed(true);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == false) {
					SourceElement refBy = (SourceElement)
						model.getElement(table.getSelectedRow());
					if (refBy != null)
						refBy.jumpTo(RefByList.this.view);
				}
			}
		});
		add(new JScrollPane(table), BorderLayout.CENTER);
		JPanel p = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Find:");
		p.add(l, BorderLayout.WEST);
		text = new JTextField(40);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					find(text.getText());
				else
					super.keyReleased(e);
			}
		});
		p.add(text, BorderLayout.CENTER);
		add(p, BorderLayout.NORTH);
	}
	
	private class ListRecordHandler implements RefByRecordHandler {
		private String dir;
		private String identifier;
		public ListRecordHandler(String dir, String identifier) {
			this.dir = dir;
			this.identifier = identifier;
		}
		public boolean handle(RefByRecord rec) {
			if (! rec.getIdentifier().equals(identifier))
				return false;
			model.addElement(rec.refBySourceElement(dir));
			return true;
		}
	}
	private void find(String identifier) {
		model.clear();
		RefByDbAccess db = new RefByDbAccess();
		db.lookup(identifier, new ListRecordHandler(db.getDir(), identifier));
		model.fireTableDataChanged();
	}
}
