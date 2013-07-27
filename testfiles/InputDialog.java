/*
 *
 * InputDialog.java
 * Copyright (C) 2001 Dominic Stolerman
 * dstolerman@jedit.org
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
 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InputDialog extends JDialog 
{
	public static final double MIN_WIDTH  = 290.0;
	public static final double MIN_HEIGHT =  110.0;
	
	public InputDialog(Frame owner, String key, String message, String defValue) {
		super(owner, "Please input a value for " + key, true);
		parent = owner;
		init( key,  message,  defValue);
		}
	
	public InputDialog(Frame owner, String key, String message, String defValue, String[] opts, boolean allowUser) {
		super(owner, "Please input a value for " + key, true);
		parent = owner;
		init( key,  message,  defValue,  opts,  allowUser);
		}
	
	public InputDialog(Dialog owner, String key, String message, String defValue) {
		super(owner, "Please input a value for " + key, true);
		parent = owner;
		init( key,  message,  defValue);
		}
	
	public InputDialog(Dialog owner, String key, String message, String defValue, String[] opts, boolean allowUser) {
		super(owner, "Please choose a value for " + key, true);
		parent = owner;
		init( key, message, defValue, opts, allowUser);
		}
	
	public String getValue() {
		return selected;
		}
	
	public String showDialog() {
		pack();
		setLocationRelativeTo(parent);
		show();
		return getValue();
		}
	
	public Dimension getMinimumSize() {
		Dimension supMin = super.getMinimumSize();
		return new Dimension(
			(int)Math.max(supMin.getWidth(), MIN_WIDTH),
			(int)Math.max(supMin.getHeight(), MIN_HEIGHT)
			);
		}
	
	public Dimension getPreferredSize() {
		Dimension supMin = super.getPreferredSize();
		return new Dimension(
			(int)Math.max(supMin.getWidth(), MIN_WIDTH),
			(int)Math.max(supMin.getHeight(), MIN_HEIGHT)
			);
		}
	
	private void init(String key, String message, String defValue, String[] opts, boolean allowUser) {
		centPanel = new CentrePanel();
		centPanel.addComboBox(opts, defValue, allowUser);
		init(message);
		}
	
	private void init(String key, String message, String defValue) {
		centPanel = new CentrePanel();
		centPanel.addTextBox(defValue);
		init(message);
		}
	
	private void init(String message) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					okJB.doClick();
					}
				}
			);
		setResizable(false);
		panel = new JPanel();
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		panel.setLayout(gb);
		c.gridx = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx =  0.5;
		c.weighty = 0.5;

		c.gridy = 0;
		label = new JLabel(message);
		label.setHorizontalAlignment(SwingConstants.LEFT);
		gb.setConstraints(label, c);
		panel.add(label);

		c.gridy = 1;
		gb.setConstraints(centPanel, c);
		panel.add(centPanel);

		c.gridy = 2;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.SOUTHEAST;
		okJB = new JButton("OK");
		getRootPane().setDefaultButton(okJB);
		okJB.addActionListener(al);
		gb.setConstraints(okJB, c);
		panel.add(okJB);

		setContentPane(panel);
		//getRootPane().registerKeyboardAction(al, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		}

	ActionListener al = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
			selected = centPanel.getValue();
			setVisible(false);
			dispose();
			}
		};

	private JLabel label;
	private JButton okJB;
	private JPanel panel;
	private CentrePanel centPanel;
	private Window parent;

	String selected;
	
	private class CentrePanel extends JPanel {
		public CentrePanel() {
			super();
			setLayout(new BorderLayout());
			}

		public void addTextBox(String defValue)	{
			box = null;
			add(text = new JTextField(defValue, 20), BorderLayout.CENTER);
			if(defValue !=null) {
				text.setSelectionStart(0);
				text.setSelectionEnd(defValue.length());
				}
			text.addActionListener(al);
			}

		public void addComboBox(String[] opts, String defValue, boolean editable) {
			text = null;
			add(box = new JComboBox(opts));
			if(defValue != null) {
				boolean defValInc = false;
				for(int i=0; i < opts.length; i++) {
					if(opts[i].equals(defValue)) {
						box.setSelectedIndex(i);
						defValInc = true;
						}
					}
				if(!defValInc) {
					box.addItem(defValue);
					box.setSelectedItem(defValue);
					}
				}
			box.setEditable(editable);
 			box.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selected = centPanel.getValue();
						}
					}
				);
		}
		
		public String getValue() {
			if(box == null)
				return text.getText();
			else
				return (String)box.getSelectedItem();
			}
		
		private JTextField text;
		private JComboBox box;
	}
}

