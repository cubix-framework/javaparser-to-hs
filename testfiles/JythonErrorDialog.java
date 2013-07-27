/*
 *  JythonErrorDialog.java - Jython macro execution error dialog box.
 *  Copyright (C) 2003 Ollie Rutherfurd, based on BeanShellErrorDialog.java
 *  Copyright (C) 2001 Slava Pestov
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package jython;

//{{{ imports
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.border.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
//}}}

public class JythonErrorDialog extends EnhancedDialog
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//{{{ constructors
	public JythonErrorDialog(View view, Throwable t)
	{
		this("jython.jython-macro-error.title", "jython.jython-macro-error.message", view, t);
	}


	public JythonErrorDialog(String titlekey, String messagekey, View view, Throwable t)
	{
		super(view, jEdit.getProperty(titlekey), true);

		JPanel content = new JPanel(new BorderLayout(12,12));
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		Box iconBox = new Box(BoxLayout.Y_AXIS);
		iconBox.add(new JLabel(UIManager.getIcon("OptionPane.errorIcon")));
		iconBox.add(Box.createGlue());
		content.add(BorderLayout.WEST, iconBox);

		JPanel centerPanel = new JPanel(new BorderLayout(6,6));
		centerPanel.add(BorderLayout.NORTH, new JLabel(
			jEdit.getProperty(messagekey)));

		JTextArea textArea = new JTextArea(10,80);
		// using t.toString() instead of printing stack trace
		// so that only the jython traceback is printed -- not
		// any of the java code
		textArea.setText(t.toString());
		textArea.setLineWrap(true);
		textArea.setCaretPosition(0);
		centerPanel.add(BorderLayout.CENTER, new JScrollPane(textArea));

		content.add(BorderLayout.CENTER, centerPanel);

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.add(Box.createGlue());
		JButton ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(new ActionHandler());
		buttons.add(ok);
		buttons.add(Box.createGlue());
		content.add(BorderLayout.SOUTH, buttons);

		getRootPane().setDefaultButton(ok);

		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		dispose();
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		dispose();
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			dispose();
		}
	} //}}}
}

// :indentSize=4:lineSeparator=\n:noTabs=false:tabSize=4:folding=explicit:collapseFolds=1:
