/*
 * TagPromptDialog.java
 *
 * Copyright (c) 2004 Ollie Rutherfurd <oliver@jedit.org>
 *
 * This file is part of Tags plugin.
 *
 * TagsPlugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * TagsPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: TagPromptDialog.java 7606 2004-11-07 15:52:36Z orutherfurd $
 */

package tags;

//{{{ imports
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;

import javax.swing.JCheckBox;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//}}}

public class TagPromptDialog extends EnhancedDialog
{
	//{{{ TagPromptDialog constructor
	public TagPromptDialog(View view)
	{
		super(view, jEdit.getProperty("tags.enter-tag-dlg.title"), true);
		this.view = view;

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JPanel tagPanel = new JPanel(new BorderLayout(4,4));
		tagPanel.setBorder(new EmptyBorder(2,2,2,2));
		tagPanel.add(new JLabel(jEdit.getProperty("tags.enter-tag-dlg.tag-to.label")),
					BorderLayout.WEST);
		tagName = new HistoryTextField("tags.enter-tag.history", false ,false);
		tagName.setColumns(16);
		tagPanel.add(tagName, BorderLayout.CENTER);
		newView = new JCheckBox(
					jEdit.getProperty("tags.enter-tag-dlg.new-view-checkbox.label"));
		tagPanel.add(newView, BorderLayout.SOUTH);

		content.add(tagPanel, BorderLayout.CENTER);

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(6,0,0,12));
		buttons.add(Box.createGlue());
		ok = new JButton(jEdit.getProperty("common.ok"));
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(6));
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		ok.setPreferredSize(cancel.getPreferredSize());
		buttons.add(cancel);
		buttons.add(Box.createGlue());
		getRootPane().setDefaultButton(ok);
		ActionHandler handler = new ActionHandler();
		ok.addActionListener(handler);
		cancel.addActionListener(handler);
		tagName.addActionListener(handler);

		content.add(BorderLayout.SOUTH,buttons);

		pack();
		setLocationRelativeTo(view);
		show();
	} //}}}

	//{{{ isOK() method
	public boolean isOK()
	{
		return isOK;
	} //}}}

	//{{{ getNewView() method
	public boolean getNewView()
	{
		return newView.isSelected();
	} //}}}

	//{{{ getTag() method
	public String getTag(){
		return tagName.getText();
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		if(tagName.getText() != null && tagName.getText().trim().length() > 0)
		{
			tagName.addCurrentToHistory();
			dispose();
			isOK = true;
		}
		else
			Toolkit.getDefaultToolkit().beep();
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		dispose();
	} //}}}

	//{{{ private declarations
	private View view;
	private HistoryTextField tagName;
	private JCheckBox newView;
	private JButton ok, cancel;
	private boolean isOK = false;
	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == ok || source == tagName)
				ok();
			else if(source == cancel)
				cancel();
		}
	} //}}}
}

// :noTabs=false:tabSize=4:folding=explicit:collapseFolds=1:
