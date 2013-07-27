/*
 * TextToolsBlockHandling.java - a Java class for the jEdit text editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2002 Rudolf Widmann
 * Rudi.Widmann@web.de
 *
 * 1) inserts or fills a given string or number into a selection
 * 2) converts spaces to tabs
 *	based on:
 *	- TextUtilities.spacesToTabs
 *	- MiscUtilities.createWhiteSpace
 *
 * Checked for jEdit 4.0 API
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

//{{{ Imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.Log;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import java.util.*;
//}}}

public class TextToolsBlockHandlingDialog extends JDialog implements KeyListener, ActionListener
{
	
	//{{{ TextToolsBlockHandlingDialog constructor
	public TextToolsBlockHandlingDialog(View view) {
		super(view, title, false);
		this.view = view;
		
		Container content = getContentPane();
		content.setLayout(new BorderLayout());

		// create dialog object and set its features
		//    title = "Insert / Fill Textblock";
		//    dialog = new JDialog(view, title, false);
		//    content = new JPanel(new BorderLayout());
		//    content.setBorder(new EmptyBorder(12, 12, 12, 12));
		//    dialog.setContentPane(content);
		// dialog fields:
		// - insertField:		TextField for text to be inserted
		// - incrementField:	TextField for increment value
		// - overwriteCheckBox:	toggle insert/overwrite
		// - leadingZerosCheckBox:	toggle leading zeros/blanks
		// - okButton:		start execution
		// - cancelButton:		discard dialog
		fieldPanel = new JPanel(new GridBagLayout());
		Insets ins = new Insets(5,5,5,5);
		insertField = new HistoryTextField("macro.block-insert-fill");
		insertLabel = new JLabel("Text to be inserted at every selected line   ");
		incrementField = new JTextField(5);
		incrementLabel = new JLabel("Increment");
		incrementField.setEnabled(false);
		incrementLabel.setEnabled(false);
		overwriteCheckBox = new JCheckBox("overwrite", false);
		overwriteCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				incrementField.setEnabled(overwriteCheckBox.isSelected());
				incrementLabel.setEnabled(overwriteCheckBox.isSelected());
				incrementField.setText("");
			}
		});
		leadingZerosCheckBox = new JCheckBox("leading Zeros", true);
		leadingZerosCheckBox.setEnabled(false);
		fieldPanel.add(insertLabel, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));
		fieldPanel.add(insertField, new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, ins, 0, 0));
		fieldPanel.add(incrementLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.NONE, ins, 0, 0));
		fieldPanel.add(incrementField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));
		fieldPanel.add(leadingZerosCheckBox, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));
		fieldPanel.add(overwriteCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE, ins, 0, 0));
		content.add(fieldPanel, "Center");
		
		// add a panel containing the buttons
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(new EmptyBorder(12, 50, 0, 50));
		buttonPanel.add(Box.createGlue());
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		ok.setPreferredSize(cancel.getPreferredSize());
		this.getRootPane().setDefaultButton(ok);  // set focus to "ok-button"
		buttonPanel.add(ok);
		buttonPanel.add(Box.createHorizontalStrut(6));
		buttonPanel.add(cancel);
		buttonPanel.add(Box.createGlue());
		content.add(buttonPanel, "South");
		
		// register this method as an ActionListener for
		// the buttons and text fields
		ok.addActionListener(this);
		cancel.addActionListener(this);
		insertField.addActionListener(this);
		incrementField.addActionListener(this);
		
		// add key listener to incrementField, to enable leadingZerosCheckBox
		incrementField.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e)	{
					leadingZerosCheckBox.setEnabled(true);
				}
				public void keyReleased(KeyEvent e)	{};
				
				public void keyTyped(KeyEvent e) {};
		});
		
		addKeyListener(this);
		
		// supply default values: skip (use hard coded)
		/*
		incrementField.setText(getTempIntProperty("searchRowLeft"));
		rightRowField.setText(getTempIntProperty("searchRowRight"));
		*/
		// incrementField.setText("14");
		// insertField.setText("MMHSX");
		
		// locate the dialog in the center of the
		// editing pane and make it visible
		this.pack();
		this.setLocationRelativeTo(view);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	} //}}}
	
	//{{{ KeyListener methods
	
	//{{{ keyPressed() method
	// this method will be called when any key is pressed
	//  KeyAdapter keyAdapter = new KeyAdapter() {	// doesnt work in beanShell ==> define all methods
	public void keyPressed(KeyEvent e) {
		// Macros.message(view,"pressed: "+e.getKeyChar());
		if (e.getKeyChar() == 27) {
			dispose();
		}
	} //}}}
	
	//{{{ keyReleased() method
	
	public void keyReleased(KeyEvent e) {} //}}}
	
	//{{{ keyTyped() method
	
	public void keyTyped(KeyEvent e) {} //}}}
	
	//}}}
	
	//{{{ actionPerformed() method
	// this method will be called when a button is clicked
	// or when ENTER is pressed
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() != cancel)
		{
			if (isDialogOk()) {
				TextToolsBlockHandling.doBlockAction(view,
								      incrementValue,
								      insertValue,
								      insertText,
								      overwriteBlock,
								      increment,
								      leadingZeros
				);
				dispose();
			}
		} else
			dispose();
	}//}}}

	//{{{ isDialogOk() method
	private boolean isDialogOk() 
	{
		int startRange;
		int endRange;
		// init user parameters
		incrementValue = 0;
		insertValue = 0;
		boolean trace=true;
		overwriteBlock = overwriteCheckBox.isSelected();
		increment = false;
		leadingZeros = leadingZerosCheckBox.isSelected();
		
		JTextField errorField=null;
		JLabel errorLabel=null;
		
		/*********************************************************
		 * evaluate parameters
		 *********************************************************/
		try
		{
			// get	insertText
			insertText = insertField.getText();
			// eval increment
			if (incrementField.getText().length() > 0) {
				increment = true;
				errorField = incrementField;
				errorLabel = incrementLabel;
				incrementValue = Integer.parseInt(errorField.getText());
				// if (incrementValue < 1) throw new NumberFormatException();
			}
			errorField = insertField;
			errorLabel = insertLabel;
			if (insertText.length() == 0 ) throw new NumberFormatException();
			if (increment) {
				insertValue = Integer.parseInt(insertText);
			}
		}
		catch (NumberFormatException e)
		{
			JOptionPane.showMessageDialog(null,
				"Wrong input :"+errorField.getText(), "Field: "+errorLabel.getText(),
				JOptionPane.ERROR_MESSAGE
			);
			errorField.requestFocus();
			return false;
		}
		insertField.addCurrentToHistory();
		return true;
	} //}}}
	
	//{{{ Protected members
	View	view;
	// dialog fields:
	// - insertField:		TextField for text to be inserted
	// - incrementField:	TextField for increment value
	// - overwriteCheckBox:	toggle insert/overwrite
	// - leadingZerosCheckBox:	toggle leading zeros/blanks
	// - okButton:		start execution
	// - cancelButton:		discard dialog
	final static String title  = "Insert / Fill Textblock";
	//  JPanel content;
	JPanel fieldPanel;
	JPanel buttonPanel;
	
	HistoryTextField 	insertField;
	JTextField 		incrementField;
	JLabel	 		insertLabel;
	JLabel 			incrementLabel;
	JCheckBox 		overwriteCheckBox;
	JCheckBox 		leadingZerosCheckBox;
	//}}}
	
	//{{{ Private members
	// result from user input
	private int incrementValue;
	private int insertValue;
	private String insertText;
	private boolean overwriteBlock;
	private boolean increment;
	private boolean leadingZeros;
	private JButton ok;
	private JButton cancel;
	//}}}
	
}
