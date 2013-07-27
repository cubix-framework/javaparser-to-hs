/*
 * BufferOptionsDialog.java - Dialog for buffer options of WhiteSpace plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2011 Jarek Czekalski
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

package whitespace;

//{{{ Imports
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import whitespace.options.WhiteSpaceOptionPane;
//}}}

public class BufferOptionsDialog extends EnhancedDialog
{
	
	//{{{ BufferOptionsDialog constructor
	public BufferOptionsDialog(Buffer buffer, View view, WhiteSpaceModel model)
	{
		super(view,jEdit.getProperty("whitespace.buffer-options-dialog.title"),true);
		this.view = view;
		this.buffer = buffer;
		// this.listModel = model;
		
		JPanel content = new JPanel(new BorderLayout());
		setContentPane(content);

		options = new BufferOptionsOptionPane(model);
		((JPanel)options).setBorder(new EmptyBorder(0,12,12,12));
		options.init();
		content.add((JPanel)options, BorderLayout.NORTH);
		
		JPanel buttons = new JPanel();
		ok = new JButton(jEdit.getProperty("common.ok"));
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		getRootPane().setDefaultButton(ok);
		ok.addActionListener(new ActionHandler());
		cancel.addActionListener(new ActionHandler());
		buttons.add(ok);
		buttons.add(cancel);
		content.add(buttons, BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		options.save();
		WhiteSpaceHighlight.updateTextAreas(buffer);
		BlockHighlight.updateTextAreas(buffer);
		FoldHighlight.updateTextAreas(buffer);
		dispose();
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		dispose();
	} //}}}

	//{{{ Private members
	private OptionPane options;
	private Buffer buffer;
	//}}}

	//{{{ Instance variables
	private View view;
	private JButton ok;
	private JButton cancel;
	//}}}

	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == ok)
				ok();
			else if(source == cancel)
				cancel();
		}
	} //}}}

}
