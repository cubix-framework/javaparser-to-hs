/*
 * TagFileCacheDialog.java - View and manage Tag Index file cache.
 *
 * Copyright (c) 2004 Ollie Rutherfurd <oliver@jedit.org>
 *
 * This file is part of TagsPlugin
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
 * $Id: TagFileCacheDialog.java 7606 2004-11-07 15:52:36Z orutherfurd $
 */

package tags;

//{{{ imports
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
//}}}

public class TagFileCacheDialog extends EnhancedDialog
{
	//{{{ TagFileCacheDialog() constructor
	public TagFileCacheDialog(View view){
		super(view, jEdit.getProperty("tags-cache-dialog.title"), true);
		this.view = view;

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		list = new JList(TagsPlugin.getTagFileManager().getCache());
		list.setVisibleRowCount(8);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListHandler());
		content.add(BorderLayout.CENTER, new JScrollPane(list));

		ActionHandler actionHandler = new ActionHandler();
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel,BoxLayout.X_AXIS));
		southPanel.setBorder(new EmptyBorder(12,0,0,0));
		southPanel.add(Box.createGlue());
		reload = new JButton(jEdit.getProperty("tags.reload"));
		southPanel.add(reload);
		reload.addActionListener(actionHandler);
		southPanel.add(Box.createHorizontalStrut(6));
		remove = new JButton(jEdit.getProperty("common.remove"));
		southPanel.add(remove);
		remove.addActionListener(actionHandler);
		southPanel.add(Box.createHorizontalStrut(6));
		close = new JButton(jEdit.getProperty("common.close"));
		close.addActionListener(actionHandler);
		getRootPane().setDefaultButton(close);
		southPanel.add(close);
		close.setPreferredSize(remove.getPreferredSize());
		southPanel.add(Box.createGlue());
		content.add(BorderLayout.SOUTH,southPanel);

		updateButtons();

		pack();
		setLocationRelativeTo(view);
		show();
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		dispose();
	} //}}}

	//{{{ reload() method
	public void reload()
	{
		reload.setEnabled(false);
		remove.setEnabled(false);
		TagFileReader reader = (TagFileReader)list.getSelectedValue();
		reader.reload(view);
		reload.setEnabled(true);
		remove.setEnabled(true);
	} //}}}

	//{{{ remove()
	public void remove()
	{
		int index = list.getSelectedIndex();
		TagsPlugin.getTagFileManager().remove(index);
		list.setListData(TagsPlugin.getTagFileManager().getCache());
		if(index >= list.getModel().getSize() - 1)
			index = list.getModel().getSize() - 1;
		if(index >= 0)
			list.setSelectedIndex(index);
		updateButtons();
	} //}}}

	//{{{ cancel() method
	public void cancel()
	{
		dispose();
	} //}}}

	//{{{ updateButtons() method
	public void updateButtons()
	{
		boolean enabled = list.getSelectedIndex() > -1;
		reload.setEnabled(enabled);
		remove.setEnabled(enabled);
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == reload)
				reload();
			else if(source == remove)
				remove();
			else if(source == close)
				ok();
		}
	} //}}}

	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateButtons();
		}
	} //}}}

	//{{{ private declarations
	private JList list;
	private View view;
	private JButton reload, remove, close;
	//}}}
}

// :noTabs=false:tabSize=4:folding=explicit:collapseFolds=1:
