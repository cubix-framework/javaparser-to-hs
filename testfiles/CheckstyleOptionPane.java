/*
* CheckstyleOptionPane.java - The Checkstyle plugin option panel
* :tabSize=8:indentSize=8:noTabs=false:
* :folding=explicit:collapseFolds=1:
*
* Copyright (C) 2009-2012 Matthieu Casanova
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
package com.kpouer.jedit.checkstyle;

//{{{ imports
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.jEdit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
//}}}

/**
* The option pane of the Checkstyle plugin.
*
* @author Matthieu Casanova
*/
public class CheckstyleOptionPane extends AbstractOptionPane 
{
	private JCheckBox embedded;
	private JComboBox embeddedStyles;
	private JCheckBox runOnSave;

	private JTextField style;
	private JButton browseButton;


	//{{{ CheckstyleOptionPane constructor
	public CheckstyleOptionPane()
	{
		super("checkstyle");
	} //}}}

	//{{{ _init() method
	@Override
	protected void _init()
	{
		addComponent(embedded = createCheckBox("checkstyle.defaultstyle.embedded"));
		EditPlugin plugin = jEdit.getPlugin("com.kpouer.jedit.checkstyle.CheckstylePlugin");
		String[] resources = plugin.getPluginJAR().getResources();
		Collection<String> styles = new ArrayList<String>();
		for (String resource : resources)
		{
			if (resource.startsWith("styles/") && resource.endsWith(".xml"))
			{
				styles.add(resource.substring(7,resource.length() - 4));
			}
		}

		addComponent(jEdit.getProperty("checkstyle.styles.label"), embeddedStyles = new JComboBox(styles.toArray()));
		embeddedStyles.setSelectedItem(jEdit.getProperty("checkstyle.defaultstyle.embedded.value"));



		style = new JTextField(20);
		style.setText(jEdit.getProperty("checkstyle.defaultstyle.file"));
		browseButton = new JButton("...");
		browseButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String[] choosenFolder =
				GUIUtilities.showVFSFileDialog(null,
				   			       style.getText(),
				   			       VFSBrowser.OPEN_DIALOG,
				   			       false);
				if (choosenFolder != null)
					style.setText(choosenFolder[0]);
			}
		});
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(style, BorderLayout.CENTER);
		panel.add(browseButton, BorderLayout.EAST);
		addComponent(jEdit.getProperty("checkstyle.defaultstyle.file.label"), panel);

		updateEmbedded();
		embedded.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateEmbedded();
			}
		});

		addComponent(runOnSave = createCheckBox("checkstyle.runonsave"));
	} //}}}

	private void updateEmbedded()
	{
		embeddedStyles.setEnabled(embedded.isSelected());
		browseButton.setEnabled(!embedded.isSelected());
		style.setEnabled(!embedded.isSelected());
	}

	//{{{ _save() method
	@Override
	protected void _save()
	{
		jEdit.setBooleanProperty("checkstyle.defaultstyle.embedded", embedded.isSelected());
		jEdit.setProperty("checkstyle.defaultstyle.embedded.value",embeddedStyles.getSelectedItem().toString());
		jEdit.setProperty("checkstyle.defaultstyle.file",style.getText());
		jEdit.setBooleanProperty("checkstyle.runonsave", runOnSave.isSelected());
	} //}}}

	//{{{ createCheckBox() method
	private static JCheckBox createCheckBox(String property)
	{
		JCheckBox checkbox = new JCheckBox(jEdit.getProperty(property + ".label"));
		checkbox.setSelected(jEdit.getBooleanProperty(property));
		return checkbox;
	} //}}}
}