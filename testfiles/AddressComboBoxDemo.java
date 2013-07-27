// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.frapuccino.addresscombobox;

import java.awt.BorderLayout;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.frapuccino.DemoComponent;

/**
 * @author fdietz
 * 
 */
public class AddressComboBoxDemo implements DemoComponent {

	/**
	 * 
	 */
	public AddressComboBoxDemo() {
		super();

	}

	/**
	 * @see org.frapuccino.DemoComponent#getComponent()
	 */
	public JComponent getComponent() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JTextField textField = new JTextField();
		new CommaSeparatedAutoCompleter(textField, Arrays.asList(new Object[] {
				"Axel", "Bart", "Carl", "Dame", "Esel", "Furz" }), true);
		panel.add(textField, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * @see org.frapuccino.DemoComponent#getDemoName()
	 */
	public String getDemoName() {
		return "Email Address Editable JComboBox";
	}

	/**
	 * @see org.frapuccino.DemoComponent#getDescription()
	 */
	public String getDescription() {
		return getDemoName();
	}
}