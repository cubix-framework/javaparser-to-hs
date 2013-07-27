/*
 * HexToolsPlugin
 * Copyright (C) 2010 Jürgen Weber
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


package de.jwi.jedit.hextools;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.StandardUtilities;

/**
 * @author Juergen Weber (WJ3369) created 05.12.2010
 * 
 */
public class HexDialog extends EnhancedDialog
{
	private static final String HEX_TOOLS_PLUGIN_CHOICE = "HexToolsPlugin.Choice";

	public static final String CLIP = "clip";

	public static final String SAVE = "save";

	private int selections, bytes;

	public String encoding;

	public boolean isOKClosed = false;

	public String radioChoice;

	public boolean nonPrintingAsDot;


	public HexDialog(JFrame frame, int selections, int bytes, String encoding)
	{
		super(frame, "HexTools", true);
		this.selections = selections;
		this.bytes = bytes;
		this.encoding = encoding;
		init();
	}


	private void init()
	{
		JPanel content = new JPanel(new GridLayout(0, 1));
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);

		JLabel title1, title2;
		JButton okButton = null, cancelButton = null;

		title1 = new JLabel(String.format("found %d selection(s)", selections));
		title2 = new JLabel(String.format("containing %d bytes", bytes));


		content.add(title1);
		content.add(title2);


		JRadioButton[] radioButtons = new JRadioButton[2];

		radioButtons[0] = new JRadioButton("Save as ..");
		radioButtons[0].setActionCommand(SAVE);

		radioButtons[1] = new JRadioButton("Copy to Clipboard as String");
		radioButtons[1].setActionCommand(CLIP);

		final ButtonGroup group = new ButtonGroup();
		group.add(radioButtons[0]);
		group.add(radioButtons[1]);

		content.add(radioButtons[0]);
		content.add(radioButtons[1]);

		String radioChoiceSaved = jEdit.getProperty(HEX_TOOLS_PLUGIN_CHOICE);
		
		int n = SAVE.equals(radioChoiceSaved) ? 0 : 1;
		
		radioButtons[n].setSelected(true);

		final JCheckBox npcdot = new JCheckBox("Nonprinting characters as dot");
		npcdot.setSelected(true);
		content.add(npcdot);

		JPanel encodingPanel = new JPanel(new GridLayout(1, 0));
		encodingPanel.setBorder(new EmptyBorder(12, 0, 0, 0));


		String[] encodings = MiscUtilities.getEncodings(true);
		Arrays.sort(encodings,
				new StandardUtilities.StringCompare<String>(true));
		final JComboBox jComboBoxEncoding = new JComboBox(encodings);
		jComboBoxEncoding.setEditable(true);
		jComboBoxEncoding.setSelectedItem(encoding);

		encodingPanel.add(jComboBoxEncoding);

		content.add(encodingPanel);

		ActionListener actionListener = new ActionListener()
		{

			public void actionPerformed(ActionEvent evt)
			{
				JButton source = (JButton) evt.getSource();
				String actionCommand = source.getActionCommand();
				if (actionCommand.equals("ok"))
				{
					radioChoice = group.getSelection().getActionCommand();

					encoding = (String) jComboBoxEncoding.getSelectedItem();

					nonPrintingAsDot = npcdot.isSelected();

					jEdit.setProperty(HEX_TOOLS_PLUGIN_CHOICE,radioChoice);
					
					ok();
				}

				else if (actionCommand.equals("cancel"))
				{
					cancel();
				}
			}
		};


		JPanel buttons = new JPanel(new GridLayout(1, 0));
		buttons.setBorder(new EmptyBorder(12, 0, 0, 0));

		okButton = new JButton("OK");
		okButton.setActionCommand("ok");
		okButton.addActionListener(actionListener);
		getRootPane().setDefaultButton(okButton);
		buttons.add(okButton);

		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(actionListener);
		buttons.add(cancelButton);

		content.add(buttons);

		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	public void ok()
	{
		isOKClosed = true;
		dispose();
	}

	public void cancel()
	{
		dispose();
	}

	public static void showDialog()
	{
		JFrame frame = new JFrame("HexDialog");

		new HexDialog(frame, 5, 7, "");
	}

	public static void main(String[] args)
	{
		showDialog();
	}


}