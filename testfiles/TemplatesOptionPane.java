// $Id: TemplatesOptionPane.java 20086 2011-10-16 10:55:59Z kerik-sf $
/*
 * TemplatesOptionPane.java - A concrete implementation of
 * org.gjt.sp.jedit.AbstractOptionPane which allows modification
 * of user-configurable options for the Templates plugin.
 * Copyright (C) 1999 Steve Jakob
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
package templates;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import org.gjt.sp.jedit.*;
/**
 * This class creates an OptionPane for use with jEdit's "Plugins Options"
 * function.
 */
public class TemplatesOptionPane extends AbstractOptionPane implements ActionListener
{
	protected JTextField templateTextField;
	protected JTextField velocityTextField;
	protected JCheckBox passThruCheckBox;

	//Constructors
	public TemplatesOptionPane() {
		super("Templates.general");
	}

	//Accessors & Mutators

	//Implementors
	public void _init() {
		this.initGUI();
	}
	
	public void _save() {
		TemplatesPlugin.setTemplateDir(templateTextField.getText());
		TemplatesPlugin.setVelocityDir(velocityTextField.getText());
		TemplatesPlugin.setAcceleratorPassThruFlag(passThruCheckBox.isSelected());
	}
	
	/**
	 * Initialize the TemplatesOptionPane.
	 * @param textFieldStr A string containing the current Templates directory.
	 */
	private void initGUI() {
		String templateFieldStr = TemplatesPlugin.getTemplateDir();
		String velocityFieldStr = TemplatesPlugin.getVelocityDirectory();
		boolean accelPassThruFlag = TemplatesPlugin.getAcceleratorPassThruFlag();
		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(5,5,5,5));
		// Panel for template directory
		JPanel templateDirPanel = new JPanel();
		BorderLayout bl = new BorderLayout();
		bl.setHgap(5);
		templateDirPanel.setLayout(bl);
		JLabel chooseDirLabel = new JLabel(jEdit.getProperty(
				"options.Templates.general.choose-dir-msg"));
		templateDirPanel.add(chooseDirLabel, BorderLayout.NORTH);
		templateTextField = new JTextField(templateFieldStr);
		templateDirPanel.add(templateTextField, BorderLayout.CENTER);
		JButton templateChooseBtn = new JButton(jEdit.getProperty(
				"options.Templates.general.choose-btn-msg"));
		templateChooseBtn.setActionCommand("chooseTemplateDir");
		templateChooseBtn.addActionListener(this);
		templateDirPanel.add(templateChooseBtn, BorderLayout.EAST);
		// Panel for Velocity directory
		JPanel velocityDirPanel = new JPanel();
		BorderLayout bl2 = new BorderLayout();
		bl2.setHgap(5);
		velocityDirPanel.setLayout(bl2);
		JLabel velocityDirLabel = new JLabel(jEdit.getProperty(
				"options.Templates.general.velocity-dir-msg"));
		velocityDirPanel.add(velocityDirLabel, BorderLayout.NORTH);
		velocityTextField = new JTextField(velocityFieldStr);
		velocityDirPanel.add(velocityTextField, BorderLayout.CENTER);
		JButton velocityChooseBtn = new JButton(jEdit.getProperty(
				"options.Templates.general.choose-btn-msg"));
		velocityChooseBtn.setActionCommand("chooseVelocityDir");
		velocityChooseBtn.addActionListener(this);
		velocityDirPanel.add(velocityChooseBtn, BorderLayout.EAST);
		
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.add(templateDirPanel);
		masterPanel.add(velocityDirPanel);
		// Add warning message
		JTextArea warningText = new JTextArea(jEdit.getProperty(
				"options.Templates.general.dir-warning-msg"));
		// warningText.setFont(warningText.getFont().deriveFont(Font.ITALIC));
		warningText.setLineWrap(true);
		warningText.setWrapStyleWord(true);
		warningText.setOpaque(false);
		warningText.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
		masterPanel.add(warningText);
		// Add checkbox for Accelerator Pass-through flag
		passThruCheckBox = new JCheckBox(
				jEdit.getProperty("options.Templates.general.accelPassThru-msg"),
				accelPassThruFlag);
		passThruCheckBox.setHorizontalTextPosition(
				javax.swing.SwingConstants.LEADING);
		passThruCheckBox.setHorizontalAlignment(
				javax.swing.SwingConstants.LEFT);
		// For some reason, even with horizontal alignment set to LEFT, when 
		// the check box is added directly in the BoxLayout, it gets right-
		// aligned and cut off. Adding the checkbox to a JPanel with a 
		// BorderLayout gives the desired appearance.
		JPanel passThruPanel = new JPanel();
		passThruPanel.setLayout(new BorderLayout());
		passThruPanel.add(passThruCheckBox, BorderLayout.CENTER);
		masterPanel.add(passThruPanel);
		add(masterPanel, BorderLayout.NORTH);
	}
	

	/**
	 * Display the file chooser and respond to the user's selection.
	 * @param evt The ActionEvent corresponding to the user's button press.
	 */
	public void actionPerformed(ActionEvent evt) {
		String startDir = templateTextField.getText();
		if ("chooseVelocityDir".equals(evt.getActionCommand())) {
			startDir = velocityTextField.getText();
		}
		JFileChooser chooser = new JFileChooser(startDir);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int retVal = chooser.showDialog(this,jEdit.getProperty(
					"options.Templates.general.choose-btn-msg"));
		if(retVal == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			if(file != null)
			{
				try
				{
					String dirName = file.getCanonicalPath();
					if ("chooseVelocityDir".equals(evt.getActionCommand())) {
						velocityTextField.setText(dirName);
					} else {
						templateTextField.setText(dirName);
					}
				}
				catch(IOException e)
				{
					// shouldn't happen
				}
			}
		}
	}
	
}
	/*
	 * Change Log:
	 * $Log$
	 * Revision 1.4  2002/09/11 18:57:35  sjakob
	 * Added ability (configurable in Global Options) to "pass through" abbreviations from
	 * the "Expand Accelerator" action to jEdit's abbreviation expansion function, in cases
	 * where no matching template accelerator is found.
	 *
	 * Revision 1.3  2002/07/29 14:11:46  sjakob
	 * Added "Velocity directory" field to TemplatesOptionPane "General" tab.
	 *
	 * Revision 1.2  2002/07/24 15:40:45  sjakob
	 * Removed compatability code for jEdit versions prior to 2.4.6, as the Templates
	 * plugin now requires at least version 4.0.
	 *
	 * Revision 1.1  2002/04/30 19:26:10  sjakob
	 * Integrated Calvin Yu's Velocity plugin into Templates to support dynamic templates.
	 *
	 * Revision 1.5  2002/02/26 03:36:46  sjakob
	 * BUGFIX: Templates directory path is no longer stored in a jEdit property if
	 * it is equal to the default Templates path (requested by Mike Dillon).
	 *
	 * Revision 1.4  2002/02/22 02:34:36  sjakob
	 * Updated Templates for jEdit 4.0 actions API changes.
	 * Selection of template menu items can now be recorded in macros.
	 *
	 * Revision 1.3  2001/02/23 19:31:39  sjakob
	 * Added "Edit Template" function to Templates menu.
	 * Some Javadoc cleanup.
	 *
	 * Revision 1.2  2000/04/21 05:32:58  sjakob
	 * Modified TemplatesOptionPane for compliance with new OptionPane interface
	 * introducted in jEdit 2.4pre6 (now supports "lazy" instantiation of dialog).
	 * The class is still compatible with earlier versions, though.
	 *
	 * Revision 1.1  1999/12/21 05:00:52  sjakob
	 * Added options pane for "Plugin options" to allow user to select template directory.
	 * Recursively scan templates directory and subdirectories.
	 * Add subdirectories to "Templates" menu as submenus.
	 * Added online documentation, as well as README.txt and CHANGES.txt.
	 *
	 */


