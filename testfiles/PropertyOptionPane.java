package rename;

import java.awt.*;
import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.*;

public class PropertyOptionPane extends AbstractOptionPane {
	private String type;
	protected String[] idArray;
	private String suffix;

	protected PropertyTableModel model;
	protected JTable table;

	public PropertyOptionPane(String type, String[] idArray, String suffix) {
		super("rename."+type.toLowerCase()+"s");
		this.type = type;
		this.idArray = idArray;
		this.suffix = suffix;
	}

	PropertyOptionPane(String type, String suffix) {
		super("rename."+type.toLowerCase()+"s");
		this.type = type;
		this.suffix = suffix;
	}

	public void _init() {
		model = new PropertyTableModel(type,idArray,suffix);
		table = new JTable(model);
		addComponent(new JScrollPane(table),GridBagConstraints.BOTH);
	}

	public void _save() {
		if (table.isEditing())
			table.getCellEditor().stopCellEditing();
		for (int i = 0; i < table.getRowCount(); i++) {
			if (model.rowChanged(i)) {
				//Log.log(Log.DEBUG,this,"saved "+table.getValueAt(i,0));
				jEdit.setProperty(table.getValueAt(i,0).toString()+"."+suffix,table.getValueAt(i,1).toString());
			}
		}
	}
}
