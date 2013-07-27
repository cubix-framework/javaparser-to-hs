/*
 * EasyOptionPane.java - an easy to use AbstractOptionPane.
 * Copyright (c) 2007 Marcelo Vanzin
 *
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
package common.gui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.JTextField;
import javax.swing.JRadioButton;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

/** An Optionpane with a meta-language interpreter for quickly creating GUIs.
 *
 *	An extension of AbstractOptionPane that makes it easier for
 *	implementing classes to create the gui by using a list of strings
 *	with the description of what the components should be.
 *
 *	<p>The pane is created by passing to this class a list of strings
 *	that describe the components to be added. These strings follow the
 *	format: <code>type,label,property[,config]</code>, where:</p>
 *
 *	<ul>
 *		<li>type: the type of the component to be added. For supported
 *		types, see below.</li>
 *		<li>label: the name of the property containing the label for the
 *		component, or <codE>null</code> for no label. If a "[label].tooltip"
 *		property exists, then the tooltip text for the component is set
 *		to the value of that property.</li>
 *		<li>property: the name of the property where the value for the
 *		component is stored.</li>
 *		<li>config: an optional string with extra configuration; this
 *		string is dependent on the type, and valid values are described
 *		below.</li>
 *	</ul>
 *
 *	<p>The following are the supported types and supported configuration
 *	strings:</p>
 *
 *	<table border="1">
 *	<tr>
 *		<th>Type</th>
 *		<th>Component</th>
 *		<th>Config String</th>
 *		<th>Saved Value</th>
 *	</tr>
 *
 *	<tr>
 *		<td>text</td>
 *		<td>JTextField</td>
 *		<td>None.</td>
 *		<td>Contents of text field.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>file, dir or filedir</td>
 *		<td>FileTextField</td>
 *		<td>"true" if it should force the file to exist.</td>
 *		<td>Contents of text field. Use the appropriate type depending
 *		on the restriction you want in the file selection.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>textarea</td>
 *		<td>JTextArea</td>
 *		<td>A string containing the number of rows of the textarea. By
 *		default, it's 5.</td>
 *		<td>Contents of text area.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>checkbox</td>
 *		<td>JCheckBox</td>
 *		<td>None.</td>
 *		<td>"true" if check box is selected, "false" otherwise.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>radio</td>
 *		<td>JRadioButton</td>
 *		<td>A string with the name of the radio button group where the
 *		group will be added. The group is created automatically.</td>
 *		<td>The index of the button in the group.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>combo</td>
 *		<td>JComboBox</td>
 *		<td>A string with a colon-separated list of values available in
 *		the combo box.</td>
 *		<td>The string value of the selected item.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>ecombo</td>
 *		<td>JComboBox (editable)</td>
 *		<td>A string with a colon-separated list of values available in
 *		the combo box.</td>
 *		<td>The string value of the selected item.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>label</td>
 *		<td>JLabel</td>
 *		<td>None. The property string is also not used for this type</td>
 *		<td>None.</td>
 *	</tr>
 *
 *	<tr>
 *		<td>sep</td>
 *		<td>A separator line. The property string is not necessary for
 *		this type.</td>
 *		<td>None.</td>
 *		<td>None.</td>
 *	</tr>
 *
 *	</table>
 *
 *	<p>It is possible to use custom types. In this case, the subclass
 *	should override {@link #createComponent(String,String,String,String)}
 *	and create the desired component.<p>
 *
 *	<p>By default, the properties are stored in jEdit's properties. The
 *	implementor can call {@link #setPropertyStore(Properties)} to set the
 *	properties object from where the values will be read and to where
 *	they will be written. Just remembed to call this method before the
 *	{@link #_init()} method is called, i.e., before the pane is	shown.</p>
 *
 *	<p>By default, if the contents of a text field are empty, the class
 *	will treat that as asking to "unset" the property, so retrieving its
 *	value from the store will return null. To control that, call
 *	{@link #setEmptyToNull(boolean)} appropriately.</p>
 *
 *  @author		Marcelo Vanzin
 *	@version	$Id$
 *	@since		CC 0.9.4
 */
public class EasyOptionPane extends AbstractOptionPane
{

	private boolean		emptyToNull = true;
	private List 		cspec;
	private Properties	pstore;

	private Map			cinst;
	private Map			rgroups;

	/**
	 *	Creates an empty option pane.
	 *
	 *	@param	name		Internal options pane name.
	 */
	public EasyOptionPane(String name)
	{
		super(name);
	}

	/**
	 *	Creates an empty option pane with the given component list.
	 *
	 *	@param	name		Internal options pane name.
	 *	@param	components	String with whitespace-delimited component specs,
	 *						as described in the javadoc for the class.
	 */
	public EasyOptionPane(String name, String components)
	{
		super(name);
		StringTokenizer st = new StringTokenizer(components);
		List lst = new LinkedList();
		while (st.hasMoreTokens()) {
			String next = st.nextToken();
			if (next.length() > 0) {
				lst.add(next);
			}
		}
		setComponentSpec(lst);
	}

	/**
	 *	Creates an empty option pane with the given component list.
	 *
	 *	@param	name		Internal options pane name.
	 *	@param	components	List of component specs, as described in the
	 *						javadoc for the class.
	 */
	public EasyOptionPane(String name, List components)
	{
		super(name);
		setComponentSpec(components);
	}

	public void _init()
	{
		if (cspec == null) {
			return;
		}
		cinst = new HashMap();
		for (Iterator it = cspec.iterator(); it.hasNext(); ) {
			JComponent jcomp;
			String comp = (String) it.next();
			StringTokenizer st = new StringTokenizer(comp, ",");

			if (st.countTokens() == 0) {
				Log.log(Log.ERROR, this, "Invalid config string (1): " + comp);
				continue;
			}

			String type = st.nextToken();

			if (st.countTokens() < 2 && !"sep".equals(type)
				&& !"label".equals(type))
			{
				Log.log(Log.ERROR, this, "Invalid config string (2): " + comp);
				continue;
			}
			String label = null;
			String tooltip = null;
			if (st.hasMoreTokens()) {
				label = st.nextToken();
				if ("null".equals(label)) {
					label = null;
				} else if (!"sep".equals(type)) {
					tooltip = jEdit.getProperty(label + ".tooltip", (String)null);
					label = jEdit.getProperty(label, label);
				}
			}
			String prop = null;
			String value = null;
			if (st.hasMoreTokens()) {
				prop = st.nextToken();
			}
			String config = null;
			if (st.hasMoreTokens()) {
				config = st.nextToken();
			}
			if (st.hasMoreTokens()) {
				Log.log(Log.WARNING, this, "component string has unused data: " + comp);
			}

			if (prop != null) {
				value = getProperty(prop, null);
			}

			if ("checkbox".equals(type)) {
				jcomp = createCheckBox(label, value, config);
				label = null;
			} else if ("combo".equals(type)) {
				jcomp = createComboBox(value, config, false);
			} else if ("dir".equals(type)) {
				jcomp = createFileTextField(value, config, JFileChooser.DIRECTORIES_ONLY);
			} else if ("ecombo".equals(type)) {
				jcomp = createComboBox(value, config, true);
			} else if ("file".equals(type)) {
				jcomp = createFileTextField(value, config, JFileChooser.FILES_ONLY);
			} else if ("filedir".equals(type)) {
				jcomp = createFileTextField(value, config, JFileChooser.FILES_AND_DIRECTORIES);
			} else if ("label".equals(type)) {
				jcomp = new JLabel(label);
				addComponent(jcomp);
				continue;
			} else if ("radio".equals(type)) {
				if (config == null) {
					Log.log(Log.WARNING, this, "Radio button with no group: " + comp);
					continue;
				}
				jcomp = createRadioButton(label, value, config);
				label = null;

				// add the radio group to the map, instead of the
				// radio button
				addComponent(jcomp);
				cinst.put(prop, rgroups.get(config));
				continue;
			} else if ("sep".equals(type)) {
				if (label != null) {
					addSeparator(label);
				} else {
					addSeparator();
				}
				continue;
			} else if ("text".equals(type)) {
				jcomp = createTextField(value, config);
			} else if ("textarea".equals(type)) {
				jcomp = createTextArea(value, config);
			} else {
				Object ocomp = createComponent(type, label, value, config);
				if (ocomp == null) {
					Log.log(Log.WARNING, this, "Unknown type: " + type);
					continue;
				} else {
					cinst.put(prop, ocomp);
					continue;
				}
			}

			cinst.put(prop, jcomp);

			if (tooltip != null) {
				jcomp.setToolTipText(tooltip);
			}

			if (label != null) {
				addComponent(label, jcomp);
			} else {
				addComponent(jcomp);
			}
		}

	}

	public void _save() {
		if (cinst == null) {
			return;
		}

		for (Iterator it = cinst.keySet().iterator(); it.hasNext(); ) {
			String prop = (String) it.next();
			Object comp = cinst.get(prop);
			setProperty(prop, parseComponent(comp, prop));
		}
	}

	/**
	 *	Sets whether empty strings in text fields should be converted to
	 *	"null" when saving the values. This means the property will be
	 *	"unset" is it's empty.
	 */
	public void setEmptyToNull(boolean flag) {
		this.emptyToNull = flag;
	}

	/**
	 *	Sets the internal component spec list. This will overwrite the
	 *	existing list, if any. The behavior of the pane is undefined if
	 *	this is called after {@link #_init()} has been called.
	 */
	protected void setComponentSpec(List components)
	{
		this.cspec = components;
	}

	/**
	 *	Sets the properties object where the properties will be saved to.
	 *	If the object is null, the jEdit properties store will be used.
	 */
	protected void setPropertyStore(Properties p)
	{
		this.pstore = p;
	}

	/**
	 *	Returns the instance of the component that has been linked to
	 *	the given property name. This will return null if called before
	 *	{@link #_init()}. This might not return a JComponent if the
	 *	property is for a radio group (in which case it returns a
	 *	ButtonGroup).
	 */
	protected Object getComponent(String prop)
	{
		return (cinst != null) ? cinst.get(prop) : null;
	}

	/**
	 *	Returns the value of the named property in the internal property
	 *	store, of the given default value if the value is null.
	 */
	protected String getProperty(String name, String dflt)
	{
		if (pstore == null) {
			return jEdit.getProperty(name, dflt);
		} else {
			String ret = pstore.getProperty(name);
			return (ret == null) ? dflt : ret;
		}
	}

	/**
	 *	Sets the value of the named property in the internal property
	 *	store.
	 */
	protected void setProperty(String name, String val)
	{
		if (val != null && val.length() == 0 && emptyToNull) {
			val = null;
		}
		if (pstore == null) {
			if (val != null) {
				jEdit.setProperty(name, val);
			} else {
				jEdit.unsetProperty(name);
			}
		} else {
			if (val != null) {
				pstore.setProperty(name, val);
			} else {
				pstore.remove(name);
			}
		}
	}

	/**
	 *	Removes the named property from the internal property store.
	 */
	protected void removeProperty(String name)
	{
		if (pstore == null) {
			jEdit.unsetProperty(name);
		} else {
			pstore.remove(name);
		}
	}

	/**
	 *	Removes all properties related to a component as defined in the
	 *	component spec list. If called before {@link #_init()}, this does
	 *	nothing.
	 */
	protected void cleanup()
	{
		if (cinst == null) {
			return;
		}
		for (Iterator it = cinst.keySet().iterator(); it.hasNext(); ) {
			String prop = (String) it.next();
			removeProperty(prop);
		}
	}

	/**
	 *	If an unknown type is found in the component spec, this method
	 *	will be called. If it returns any object, the object will be added
	 *	to the component map, but nothing will be automatically added to
	 *	the GUI. If null is	returned, an error will be logged.
	 *
	 *	@param	type	The type string from the spec.
	 *	@param	label	The label string (not the key for lookup!).
	 *	@param	value	The value of the property, retrieved from the store.
	 *	@param	config	The config string from the spec.
	 */
	protected Object createComponent(String type, String label,
									 String value, String config)
	{
		return null;
	}

	/**
	 *	This method is called to transform the contents of a component
	 *	into a string to be persisted. The default implementation
	 *	understands all the available types described in this class's
	 *	javadoc. If you need special treatment for some specific
	 *	component, or you added a custom component that is not handled
	 *	by the default implementation, override this method.
	 *
	 *	@param	comp	The component added to the component map.
	 * 	@param	name	The name of the property attached to the component.
	 */
	protected String parseComponent(Object comp, String name) {
		String ret = null;
		if (comp instanceof ButtonGroup) {
			Enumeration buttons = ((ButtonGroup)comp).getElements();
			int idx = 0;
			while (buttons.hasMoreElements()) {
				AbstractButton abtn = (AbstractButton) buttons.nextElement();
				if (abtn.isSelected()) {
					break;
				}
				idx++;
			}
			ret = String.valueOf(idx);
		} else if (comp instanceof FileTextField) {
			ret = ((FileTextField)comp).getTextField().getText();
		} else if (comp instanceof JCheckBox) {
			boolean val = ((JCheckBox)comp).isSelected();
			ret = String.valueOf(val);
		} else if (comp instanceof JComboBox) {
			ret = ((JComboBox)comp).getSelectedItem().toString();
		} else if (comp instanceof JTextComponent) {
			ret = ((JTextComponent)comp).getText();
		} else {
			Log.log(Log.WARNING, this, "Unhandled component type: " + comp.getClass().getName());
		}
		return ret;
	}

	private JCheckBox createCheckBox(String label, String prop, String config)
	{
		JCheckBox ret = new JCheckBox(label);
		ret.setSelected("true".equalsIgnoreCase(prop));
		return ret;
	}

	private JComboBox createComboBox(String prop,
	                                 String config,
                                     boolean editable)
	{
		JComboBox ret = new JComboBox();
		ret.setEditable(editable);

		boolean found = false;
		int idx = 0;
		if (config != null) {
			StringTokenizer vals = new StringTokenizer(config, ":");
			while (vals.hasMoreTokens()) {
				String next = vals.nextToken();
				ret.addItem(next);
				if (next.equals(prop)) {
					found = true;
				} else if (!found && vals.hasMoreTokens()) {
					idx++;
				}
			}
		}

		if (!found && prop != null) {
			ret.addItem(prop);
		}
		if (ret.getItemCount() > 0) {
			ret.setSelectedIndex(idx);
		}

		return ret;
	}

	private FileTextField createFileTextField(String prop, String config, int mode)
	{
		FileTextField ret = new FileTextField("true".equalsIgnoreCase(config));
		if (prop != null) {
			ret.getTextField().setText(prop);
		}
		ret.setFileSelectionMode(mode);
		return ret;
	}

	private JRadioButton createRadioButton(String label, String prop, String config)
	{
		JRadioButton ret = new JRadioButton(label);
		if (config != null) {
			if (rgroups == null) {
				rgroups = new HashMap();
			}

			ButtonGroup grp = (ButtonGroup) rgroups.get(config);
			if (grp == null) {
				grp = new ButtonGroup();
				rgroups.put(config, grp);
			}

			grp.add(ret);

			int idx = 0;
			try {
				idx = Integer.parseInt(prop);
			} catch (NumberFormatException nfe) {
			}

			ret.setSelected(idx == grp.getButtonCount());
		}

		return ret;
	}

	private JTextField createTextField(String prop, String config)
	{
		JTextField ret = new JTextField();
		if (prop != null) {
			ret.setText(prop);
		}
		return ret;
	}

	private JTextArea createTextArea(String prop, String config)
	{
		JTextArea ret = new JTextArea();
		if (prop != null) {
			ret.setText(prop);
		}
		if (config != null) {
			try {
				ret.setRows(Integer.parseInt(config));
			} catch (NumberFormatException nfe) {
				ret.setRows(5);
			}
		}
		return ret;
	}

}
