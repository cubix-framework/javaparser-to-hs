/*
 * RegistryOptionsPane.java - Registry Options for JythonInterpreter plugin.
 *
 * Copyright (C) 2003 Ollie Rutherfurd
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
 *
 * $Id: RegistryOptionsPane.java,v 1.2 2003/03/11 23:29:50 fruhstuck Exp $
 */

package jython.options;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

public class RegistryOptionsPane extends AbstractOptionPane
	implements ActionListener
{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	//{{{ constructor
	public RegistryOptionsPane()
	{
		super("jython.registry");
	} //}}}

	//{{{ _init() method
	public void _init()
	{
		JPanel panel;

		// whether or not to use custom values
		addComponent(override = new JCheckBox(jEdit.getProperty(
			"options.jython.overrideTitle"))
		);
		override.getModel().setSelected(
			jEdit.getBooleanProperty("options.jython.override")
		);
		override.addActionListener(this);

		// python.path
		addComponent(jEdit.getProperty("options.jython.registry.name.0"),
			path = new JTextField(
				jEdit.getProperty("options.jython.registry.value.0"))
		);

		// python.cachedir
		panel = new JPanel(new BorderLayout(2,2));
		browseForCache = new JButton("...");
		browseForCache.addActionListener(this);
		panel.add(cache = new JTextField(
			jEdit.getProperty("options.jython.registry.value.1"))
			, BorderLayout.CENTER);
		panel.add(browseForCache, BorderLayout.EAST);
		addComponent(jEdit.getProperty("options.jython.registry.name.1"),panel);

		// python.verbose
		DefaultComboBoxModel levels = new DefaultComboBoxModel();
		StringTokenizer st = new StringTokenizer(
			jEdit.getProperty("options.jython.python.verbose.values"));
		levels.addElement("");
		while(st.hasMoreElements())
		{
			levels.addElement(st.nextToken());
		}
		addComponent(jEdit.getProperty("options.jython.registry.name.2"),
			verbose = new JComboBox(levels)
		);
		verbose.setSelectedItem(
			jEdit.getProperty("options.jython.registry.value.2"));

		// python.security.respectJavaAccessibility
		addComponent(respectJavaAccessibility = new JCheckBox(
			jEdit.getProperty("options.jython.registry.name.3"))
		);
		respectJavaAccessibility.getModel().setSelected(
			jEdit.getBooleanProperty("options.jython.registry.value.3")
		);

		// Jython 2.5+ does not support jythonc anymore.
		// python.jythonc.compiler
//		panel = new JPanel(new BorderLayout(2,2));
//		browseForCompiler = new JButton("...");
//		browseForCompiler.addActionListener(this);
//		panel.add(
//			compiler = new JTextField(
//				jEdit.getProperty("options.jython.registry.value.4"))
//			,BorderLayout.CENTER
//		);
//		panel.add(browseForCompiler, BorderLayout.EAST);
//		addComponent(
//			jEdit.getProperty("options.jython.registry.name.4"),panel);
//
//		// python.jythonc.classpath
//		addComponent(jEdit.getProperty("options.jython.registry.name.5"),
//			classpath = new JTextField(
//				jEdit.getProperty("options.jython.registry.value.5"))
//		);
//
//		// python.jythonc.compileropts
//		addComponent(jEdit.getProperty("options.jython.registry.name.6"),
//			compilerOpts = new JTextField(
//				jEdit.getProperty("options.jython.registry.value.6"))
//		);

		// control shouldn't be enabled unless override is true
		enableControls();

	} //}}}

	//{{{ _save() method
	public void _save()
	{
		jEdit.setBooleanProperty("options.jython.override",
			override.getModel().isSelected());

		// python.path
		jEdit.setProperty("options.jython.registry.value.0", 
			path.getText());
		// python.cachedir
		jEdit.setProperty("options.jython.registry.value.1", 
			cache.getText());
		// python.verbose
		jEdit.setProperty("options.jython.registry.value.2", 
			(String)verbose.getSelectedItem());
		// python.security.respectJavaAccessibility
		jEdit.setBooleanProperty("options.jython.registry.value.3", 
			respectJavaAccessibility.getModel().isSelected());
		// Jython 2.5+ does not support jythonc anymore.
		// python.jythonc.compiler
//		jEdit.setProperty("options.jython.registry.value.4", 
//			compiler.getText());
//		// python.jythonc.classpath
//		jEdit.setProperty("options.jython.registry.value.5", 
//			classpath.getText());
//		// python.jythonc.compileropts
//		jEdit.setProperty("options.jython.registry.value.6", 
//			compilerOpts.getText());
	}
	//}}}

	//{{{ actionPerformed() method
	public void actionPerformed(ActionEvent evt)
	{
		if(evt.getSource() == override)
		{
			enableControls();
		}
		else if (evt.getSource() == browseForCache)
		{
			String[] dirs = GUIUtilities.showVFSFileDialog(null, 
				cache.getText(), VFSBrowser.CHOOSE_DIRECTORY_DIALOG, false);
			if(dirs != null && dirs.length == 1)
			{
				cache.setText(dirs[0]);
			}
		}
		// Jython 2.5+ does not support jythonc anymore.
//		else if(evt.getSource() == browseForCompiler)
//		{
//			String[] files = GUIUtilities.showVFSFileDialog(null,
//				compiler.getText(),
//				VFSBrowser.OPEN_DIALOG, false);
//			if(files != null && files.length == 1)
//			{
//				compiler.setText(files[0]);
//			}
//		}
	} //}}}

	//{{{ enableControls() method
	private void enableControls()
	{
		boolean enabled = override.getModel().isSelected();
		path.setEnabled(enabled);
		cache.setEnabled(enabled);
		browseForCache.setEnabled(enabled);
		verbose.setEnabled(enabled);
		respectJavaAccessibility.setEnabled(enabled);
//		compiler.setEnabled(enabled);
//		browseForCompiler.setEnabled(enabled);
//		classpath.setEnabled(enabled);
//		compilerOpts.setEnabled(enabled);
	} //}}}

	//{{{ instance variables
	private JCheckBox override;
	private JTextField path;
	private JTextField cache;
	private JButton browseForCache;
	private JComboBox verbose;
	private JCheckBox respectJavaAccessibility;
//	private JTextField compiler;
//	private JButton browseForCompiler;
//	private JTextField classpath;
//	private JTextField compilerOpts;
	//}}}
}

// :indentSize=4:lineSeparator=\n:noTabs=false:tabSize=4:folding=explicit:collapseFolds=1:
