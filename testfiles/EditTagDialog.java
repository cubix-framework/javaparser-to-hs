/*
 * EditTagDialog.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2003 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml;

//{{{ Imports
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.StandardUtilities;
import xml.completion.*;
import xml.completion.ElementDecl.AttributeDecl;
import org.xml.sax.helpers.NamespaceSupport;
//}}}

public class EditTagDialog extends EnhancedDialog
{

	//{{{ EditTagDialog constructor
	/**
	 * elementName might not equal element.name due to case insensitivity
	 * in HTML files.
	 */
	EditTagDialog(View view, String elementName, ElementDecl element,
		Map<String, Object> attributeValues, boolean elementEmpty, Map entityHash,
		List ids, boolean html, Map<String, String> namespaces, Map<String, String> namespacesToInsert)
	{
		super(view,jEdit.getProperty("xml-edit-tag.title"),true);

		this.elementName = elementName;
		this.element = element;
		this.entityHash = entityHash;
		this.html = html;
		this.namespaces = namespaces;
		this.namespacesToInsert = namespacesToInsert;

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		//{{{ Top panel with element name, empty toggle
		JPanel top = new JPanel(new BorderLayout(6,0));
		top.setBorder(new EmptyBorder(0,0,12,0));

		top.add(BorderLayout.WEST,new JLabel(
			jEdit.getProperty("xml-edit-tag.element-name")));

		top.add(BorderLayout.CENTER,new JLabel(element.name));

		empty = new JCheckBox(jEdit.getProperty("xml-edit-tag.empty"));
		if(element.empty)
		{
			empty.setSelected(true);
			empty.setEnabled(false);
		}
		else
			empty.setSelected(elementEmpty);
		empty.addActionListener(new ActionHandler());

		top.add(BorderLayout.EAST,empty);
		content.add(BorderLayout.NORTH,top);
		//}}}

		//{{{ Attribute table
		JPanel center = new JPanel(new BorderLayout());
		attributeModel = createAttributeModel(element.attributes,
			attributeValues,ids);
		attributes = new AttributeTable();
		attributes.setModel(new AttributeTableModel());
		attributes.setRowHeight(new JComboBox(new String[] { "template" })
			.getPreferredSize().height);

		attributes.getTableHeader().setReorderingAllowed(false);
		attributes.getColumnModel().getColumn(0).setPreferredWidth(30);
		attributes.setColumnSelectionAllowed(false);
		attributes.setRowSelectionAllowed(false);
		attributes.setCellSelectionEnabled(false);

		JScrollPane scroller = new JScrollPane(attributes);
		Dimension size = scroller.getPreferredSize();
		size.height = Math.min(size.width,200);
		scroller.setPreferredSize(size);
		center.add(BorderLayout.CENTER,scroller);
		//}}}

		//{{{ Preview field
		JPanel previewPanel = new JPanel(new BorderLayout());
		previewPanel.setBorder(new EmptyBorder(12,0,0,0));
		previewPanel.add(BorderLayout.NORTH,new JLabel(jEdit.getProperty(
			"xml-edit-tag.preview")));

		preview = new JTextArea(5,5);
		preview.setLineWrap(true);
		preview.setWrapStyleWord(true);
		preview.setEditable(false);
		previewPanel.add(BorderLayout.CENTER,new JScrollPane(preview));

		center.add(BorderLayout.SOUTH,previewPanel);
		//}}}

		content.add(BorderLayout.CENTER,center);

		//{{{ Buttons
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(12,0,0,0));

		buttons.add(Box.createGlue());
		buttons.add(ok = new JButton(jEdit.getProperty("common.ok")));
		ok.addActionListener(new ActionHandler());
		getRootPane().setDefaultButton(ok);

		buttons.add(Box.createHorizontalStrut(6));

		buttons.add(cancel = new JButton(jEdit.getProperty("common.cancel")));
		cancel.addActionListener(new ActionHandler());
		buttons.add(cancel);

		buttons.add(Box.createGlue());

		content.add(BorderLayout.SOUTH,buttons);
		//}}}

		updateTag();

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(view);
		// request focus so the dialog can be disposed of by hitting Escape key
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				requestFocus();
			}
		});
		setVisible(true);
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		int row = attributes.getSelectedRow();
		int column = attributes.getSelectedColumn();

		if(row != -1 && column != -1)
		{
			if(attributes.getCellEditor(row,column) != null)
				attributes.getCellEditor(row,column).stopCellEditing();
		}

		isOK = true;
		dispose();
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		isOK = false;
		dispose();
	} //}}}

	//{{{ getNewTag() method
	public String getNewTag()
	{
		return (isOK ? newTag : null);
	} //}}}

	//{{{ isEmpty() method
	public boolean isEmpty()
	{
		return empty.isSelected();
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private boolean html;
	private String elementName;
	private ElementDecl element;
	private Map entityHash;
	private JCheckBox empty;
	private List<Attribute> attributeModel;
	private JTable attributes;
	private JTextArea preview;
	private JButton ok;
	private JButton cancel;
	private String newTag;
	private boolean isOK;
	private Map<String, String> namespaces;
	private Map<String, String> namespacesToInsert;
	//}}}

	//{{{ createAttributeModel() method
	private ArrayList<Attribute> createAttributeModel(List declaredAttributes,
		Map<String,Object> attributeValues, List ids)
	{
		ArrayList<String> stringIDs = new ArrayList<String>(ids.size());
		for(int i = 0; i < ids.size(); i++)
		{
			stringIDs.add(((IDDecl)ids.get(i)).id);
		}

		ArrayList<Attribute> attributeModel = new ArrayList<Attribute>();
		// keep original list of namespaces to insert, otherwise it gets polluted by namespaces of attributes not inserted
		Map<String,String> localNamespacesToInsert = new HashMap<String,String>(namespacesToInsert);
		for(int i = 0; i < declaredAttributes.size(); i++)
		{
			AttributeDecl attr =
				(AttributeDecl)
				declaredAttributes.get(i);

			String attrName = composeName(attr.name, attr.namespace, namespaces, localNamespacesToInsert);

			boolean set;
			String value = (String)attributeValues.get(attrName);
			if(value == null)
			{
				set = false;
				value = attr.value;
			}
			else
				set = true;

			if(attr.required)
				set = true;

			ArrayList values;
			if(attr.type.equals("IDREF")
				&& stringIDs.size() > 0)
			{
				values = stringIDs;
				if(value == null)
					value = (String)stringIDs.get(0);
			}
			else
			{
				values = attr.values;
				if(value == null && values != null
					&& values.size() > 0)
					value = (String)values.get(0);
			}
			
			attributeModel.add(new Attribute(set,attrName,attr.namespace,
				value,values,attr.type,attr.required));
		}

		Collections.sort(attributeModel,new AttributeCompare());

		return attributeModel;
	} //}}}

	//{{{ updateTag() method
	private void updateTag()
	{
		int tagNameCase = TextUtilities.getStringCase(elementName);
		// only append namespaces really used or in the original namespacesToInsert
		Map<String,String> localNamespacesToInsert = new HashMap<String,String>(namespacesToInsert);
		
		StringBuilder buf = new StringBuilder("<");
		buf.append(elementName);

		for(int i = 0; i < attributeModel.size(); i++)
		{
			Attribute attr = (Attribute)attributeModel.get(i);
			if(!attr.set)
				continue;

			buf.append(' ');
			String attrName = attr.name;
			if(html)
			{
				switch(tagNameCase)
				{
				case TextUtilities.UPPER_CASE:
					attrName = attr.name.toUpperCase();
					break;
				case TextUtilities.LOWER_CASE:
					attrName = attr.name.toLowerCase();
					break;
				case TextUtilities.TITLE_CASE:
					attrName = TextUtilities.toTitleCase(
						attr.name);
					break;
				}
			}
			else
			{
				String prefix = XmlParsedData.getElementNamePrefix(attrName);
				if(!"".equals(attr.namespace)
					&& !NamespaceSupport.XMLNS.equals(attr.namespace)
					&& !namespaces.containsKey(attr.namespace)
					&& !localNamespacesToInsert.containsKey(attr.namespace))
				{
					localNamespacesToInsert.put(attr.namespace,prefix);
				}
			}
			buf.append(attrName);

			if(html && attr.name.equals(attr.value.value))
			{
				continue;
			}

			buf.append("=\"");
			if(attr.value.value != null)
			{
				buf.append(XmlActions.charactersToEntities(
					attr.value.value,entityHash));
			}
			buf.append("\"");
		}

		appendNamespaces(localNamespacesToInsert,buf);

		if(empty.isSelected() && !html)
			buf.append("/");

		buf.append(">");

		newTag = buf.toString();

		preview.setText(newTag);
	} //}}}

	//}}}

	//{{{ Inner classes

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == empty)
				updateTag();
			else if(evt.getSource() == ok)
				ok();
			else if(evt.getSource() == cancel)
				cancel();
		}
	} //}}}

	//{{{ Attribute class
	static class Attribute
	{
		//{{{ Instance variables
		boolean set;

		String name;
		String namespace;
		Value value;
		String type;
		boolean required;
		//}}}

		//{{{ Attribute constructor
		Attribute(boolean set, String name, String namespace,
			String value, ArrayList values,
			String type, boolean required)
		{
			this.set = set;
			this.name = name;
			this.namespace = namespace;
			this.value = new Value(value,values);
			this.type = type;
			this.required = required;
		} //}}}

		//{{{ Value class
		static class Value
		{
			String value;
			ArrayList values;

			Value(String value, ArrayList values)
			{
				this.value = value;
				this.values = values;
			}

			public String toString()
			{
				return value;
			}
		} //}}}
	} //}}}

	//{{{ AttributeCompare class
	static class AttributeCompare implements java.util.Comparator<Attribute>
	{
		public int compare(Attribute attr1, Attribute attr2)
		{
			// put required attributes at the top
			if(attr1.required && !attr2.required)
				return -1;
			else if(!attr1.required && attr2.required)
				return 1;
			else
			{
				return StandardUtilities.compareStrings(
					attr1.name,attr2.name,true);
			}
		}
	} //}}}

	static ComboValueRenderer comboRenderer = new ComboValueRenderer();

	//{{{ AttributeTable class
	class AttributeTable extends JTable
	{
		//{{{ getCellEditor() method
		public TableCellEditor getCellEditor(int row, int column)
		{
			Object value = getModel().getValueAt(row,column);
			if(value instanceof Attribute.Value)
				return comboRenderer;

			return super.getCellEditor(row,column);
		} //}}}

		//{{{ getCellRenderer() method
		public TableCellRenderer getCellRenderer(int row, int column)
		{
			Object value = getModel().getValueAt(row,column);
			if(value instanceof Attribute.Value)
				return comboRenderer;

			return super.getCellRenderer(row,column);
		} //}}}
	} //}}}

	//{{{ AttributeTableModel class
	class AttributeTableModel extends AbstractTableModel
	{
		//{{{ getColumnCount() method
		public int getColumnCount()
		{
			return 4;
		} //}}}

		//{{{ getRowCount() method
		public int getRowCount()
		{
			return attributeModel.size();
		} //}}}

		//{{{ getColumnClass() method
		public Class getColumnClass(int col)
		{
			if(col == 0)
				return Boolean.class;
			else
				return String.class;
		} //}}}

		//{{{ getColumnName() method
		public String getColumnName(int col)
		{
			switch(col)
			{
			case 0:
				return jEdit.getProperty("xml-edit-tag.set");
			case 1:
				return jEdit.getProperty("xml-edit-tag.attribute");
			case 2:
				return jEdit.getProperty("xml-edit-tag.type");
			case 3:
				return jEdit.getProperty("xml-edit-tag.value");
			default:
				throw new InternalError();
			}
		} //}}}

		//{{{ isCellEditable() method
		public boolean isCellEditable(int row, int col)
		{
			if(col != 1 && col != 2)
				return true;
			else
				return false;
		} //}}}

		//{{{ getValueAt() method
		public Object getValueAt(int row, int col)
		{
			Attribute attr = (Attribute)attributeModel.get(row);
			switch(col)
			{
			case 0:
				return new Boolean(attr.set);
			case 1:
				return attr.name;
			case 2:
				if(attr.required)
				{
					if(attr.type.startsWith("("))
						return jEdit.getProperty("xml-edit-tag.required");
					else
						return attr.type + ", " + jEdit.getProperty("xml-edit-tag.required");
				}
				else if(attr.type.startsWith("("))
					return "";
				else
					return attr.type;
			case 3:
				if(attr.value.values != null)
					return attr.value;
				else
					return attr.value.value;
			default:
				throw new InternalError();
			}
		} //}}}

		//{{{ setValueAt() method
		public void setValueAt(Object value, int row, int col)
		{
			Attribute attr = (Attribute)attributeModel.get(row);
			switch(col)
			{
			case 0:
				if(attr.required)
					return;

				attr.set = ((Boolean)value).booleanValue();
				break;
			case 3:
				String sValue;
				if(value instanceof IDDecl)
					sValue = ((IDDecl)value).id;
				else
					sValue = value.toString();
				if(equal(attr.value.value,sValue))
					return;

				attr.set = true;
				attr.value.value = sValue;
				break;
			}

			fireTableRowsUpdated(row,row);

			updateTag();
		} //}}}

		//{{{ equal() method
		private boolean equal(String str1, String str2)
		{
			if(str1 == null || str1.length() == 0)
			{
				if(str2 == null || str2.length() == 0)
					return true;
				else
					return false;
			}
			else
			{
				if(str2 == null)
					return false;
				else
					return str1.equals(str2);
			}
		} //}}}
	} //}}}

	//{{{ ComboValueRenderer class
	static class ComboValueRenderer extends DefaultCellEditor
		implements TableCellRenderer
	{
		JComboBox editorCombo;
		JComboBox renderCombo;

		//{{{ ComboValueRenderer constructor
		ComboValueRenderer()
		{
			this(new JComboBox());
		} //}}}

		//{{{ ComboValueRenderer constructor
		// this is stupid. why can't you reference instance vars
		// in a super() invocation?
		ComboValueRenderer(JComboBox comboBox)
		{
			super(comboBox);
			this.editorCombo = comboBox;
			editorCombo.setEditable(true);
			this.renderCombo = new JComboBox();
			renderCombo.setEditable(true);
		} //}}}

		//{{{ getTableCellEditorComponent() method
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			Attribute.Value _value = (Attribute.Value)value;
			editorCombo.setModel(new DefaultComboBoxModel(
				_value.values.toArray()));
			return super.getTableCellEditorComponent(table,
				_value.value,isSelected,row,column);
		} //}}}

		//{{{ getTableCellRendererComponent() method
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			Attribute.Value _value = (Attribute.Value)value;
			renderCombo.setModel(new DefaultComboBoxModel(
				_value.values.toArray()));
			renderCombo.setSelectedItem(_value.value);
			return renderCombo;
		} //}}}
	} //}}}

	//}}}

	/**
	 * create a qualified name from localname, ns and given namespaces bindings.
	 * If no namespace, returns localname directly.
	 * Otherwise, pre:localname is returned, where 
	 * pre is taken from namespacesToInsert then if not found from namespaces, then is generated to be unique
	 * among namespaces and namespacesToInsert (always of the form nsNUMBER).
	 * @param localname				local name
	 * @param ns					namespace localname is in
	 * @param namespaces			already declared namespaces
	 * @param namespacesToInsert	new namespaces (IN/OUT: a new binding may be added)
	 * @return	the qualified name
	 */
	public static String composeName(String localname, String ns, Map<String,String> namespaces, Map<String, String> namespacesToInsert){

		if(ns == null || "".equals(ns))
		{
			return localname;
		}
		else
		{
			// prefer getting from namespacesToInsert, for the case when EditTagDialog is called on a tag redefining a namespace
			String pre = namespacesToInsert.get(ns);
			if(pre == null)
			{
					pre = namespaces.get(ns);
			}
			if(pre == null)
			{
				// special case for the predefined XML namespace
				if(NamespaceSupport.XMLNS.equals(ns))
				{
					pre = "xml";
				}
				else
				{
					pre = generatePrefix(namespaces, namespacesToInsert);
					namespacesToInsert.put(ns,pre);
				}
			}

			if("".equals(pre)){
				return localname;
			}else{
				return pre + ":" + localname;
			}

		}
	}
	
	/**
	 * append xmlns:XX="..." namespace bindings for each item in namespacesToInsert to buf
	 * @param namespacesToInsert	namespaces to declare
	 * @param buf					buffer to append to
	 */
	public static void appendNamespaces(Map<String, String> namespacesToInsert, StringBuilder buf)
	{
		if(namespacesToInsert.isEmpty())return;
		
		for(Map.Entry<String, String> en: namespacesToInsert.entrySet())
		{
			buf.append(' ');
			buf.append("xmlns:").append(en.getValue()).append("=\"").append(en.getKey()).append('"');
		}
	}
	
	
	/**
	 * create open and close tags for an ElementDecl (with required attributes but without starting &lt; angle bracket).
	 * if elementDecl.isEmpty, close tag will be empty and open tag will be standalone (if not html)
	 * @param data	
	 * @param elementDecl			element to create tags for
	 * @param namespaces			namespaces in scope (no need to redeclare them)
	 * @param namespacesToInsert	namespaces needed but not declared (will be declared)
	 * @param withEndOfTag			shall the '>' be appended at the end of open tag ?
	 * @return	{openTag,closeTag}
	 */
	public static StringBuilder[] composeTag(XmlParsedData data, ElementDecl elementDecl, Map<String,String> namespaces, Map<String, String> namespacesToInsert, boolean withEndOfTag){
		StringBuilder buf = new StringBuilder();
		StringBuilder close = new StringBuilder();
		String ns = elementDecl.completionInfo.namespace;
		String elementName = EditTagDialog.composeName(elementDecl.name, ns, namespaces, namespacesToInsert);
		buf.append(elementName);
		
		buf.append(elementDecl.getRequiredAttributesString(namespaces, namespacesToInsert));

		EditTagDialog.appendNamespaces(namespacesToInsert,buf);

		if(withEndOfTag)
		{
			if(elementDecl.empty)
			{
				if(data.html)
					buf.append(">");
				else
					buf.append(XmlActions.getStandaloneEnd());

			}
			else
			{
				buf.append(">");
				close.append("</").append(elementName).append(">");
			}
		}
		return new StringBuilder[]{buf, close};
	}

	/**
	 * generate a new prefix, unique among namespaces bindings
	 * 
	 * @param namespaces	namespaces bindings not to override
	 * @return	new prefix
	 */
	public static String generatePrefix(Map<String,String> ... namespaces) {
		String pre;
		for(int i=0;;i++){
			pre = "ns"+i;
			boolean notSeen = true;
			for(Map<String,String> namespace: namespaces){
				notSeen &= !namespace.containsKey(pre);
				if(!notSeen)break;
			}
			if(notSeen)return pre;
		}
	}
}
