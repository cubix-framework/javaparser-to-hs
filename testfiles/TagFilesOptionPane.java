/*
 * TagFilesOptionPane.java
 *
 * Copyright 2004 Ollie Rutherfurd <oliver@jedit.org>
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA	02111-1307, USA.
 *
 * $Id: TagFilesOptionPane.java 14169 2008-12-13 15:19:52Z shlomy $
 */

package tags.options;

//{{{ imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.*;

import java.util.Vector;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

import tags.*;
//}}}

public class TagFilesOptionPane extends AbstractOptionPane
{
	//{{{ TagFilesOptionPane
	public TagFilesOptionPane()
	{
		super("tags.files");
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		addComponent(new JLabel(jEdit.getProperty(
			"options.tags.files.label")));

		Vector tagFiles = TagsPlugin.getTagFileManager().getTagFiles();
		listModel = new DefaultListModel();
		tagIndexFilename = TagsPlugin.getCurrentBufferTagFilename();

		for(int i=0; i < tagFiles.size(); i++)
			listModel.addElement(tagFiles.elementAt(i));

		disabledIcon = GUIUtilities.loadIcon("Cancel.png");
		enabledIcon = GUIUtilities.loadIcon("NextFile.png");
		currentIcon = GUIUtilities.loadIcon("FindInDir.png");
		recursiveIcon = GUIUtilities.loadIcon("FindAgain.png");

		JPanel panel = new JPanel(new BorderLayout());
		fileList = new JList(listModel);
		fileList.addListSelectionListener(new ListHandler());
		fileList.addMouseListener(new MouseHandler());
		fileList.setCellRenderer(new TagIndexFileCellRenderer());
		panel.add(BorderLayout.CENTER, new JScrollPane(fileList));

		ActionHandler actionHandler = new ActionHandler();

		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(3,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("common.add"));
		add.addActionListener(actionHandler);
		buttons.add(add);
		buttons.add(Box.createHorizontalStrut(6));
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("common.remove"));
		remove.addActionListener(actionHandler);
		buttons.add(remove);
		buttons.add(Box.createHorizontalStrut(6));
		edit = new RolloverButton(GUIUtilities.loadIcon("Properties.png"));
		edit.setToolTipText(jEdit.getProperty("options.tags.files.edit"));
		edit.addActionListener(actionHandler);
		buttons.add(edit);
		buttons.add(Box.createHorizontalStrut(6));
		moveUp = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
		moveUp.setToolTipText(jEdit.getProperty("common.moveUp"));
		moveUp.addActionListener(actionHandler);
		buttons.add(moveUp);
		buttons.add(Box.createHorizontalStrut(6));
		moveDown = new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
		moveDown.setToolTipText(jEdit.getProperty("common.moveDown"));
		moveDown.addActionListener(actionHandler);
		buttons.add(moveDown);
		buttons.add(Box.createHorizontalStrut(6));
		enable = new RolloverButton(GUIUtilities.loadIcon("Play.png"));
		enable.setToolTipText(jEdit.getProperty("options.tags.files.toggle-enable"));
		enable.addActionListener(actionHandler);
		buttons.add(enable);
		buttons.add(Box.createHorizontalStrut(6));
		disable = new RolloverButton(disabledIcon);
		disable.setToolTipText(jEdit.getProperty("options.tags.files.toggle-disable"));
		disable.addActionListener(actionHandler);
		buttons.add(disable);
		buttons.add(Box.createGlue());
		panel.add(buttons, BorderLayout.SOUTH);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = cons.REMAINDER;
		cons.gridwidth = cons.REMAINDER;
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = cons.weighty = 1.0f;
		gridBag.setConstraints(panel, cons);

		add(panel);

		updateButtons();
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		Vector tagFiles = new Vector();
		for(int i=0; i < listModel.size(); i++)
		{
			TagFile tagFile = (TagFile)listModel.elementAt(i);
			jEdit.setProperty("tags.tagfile.path." + i, tagFile.getPath());
			jEdit.setBooleanProperty("tags.tagfile.enabled." + i, tagFile.isEnabled());
			if (tagFile.isCurrentDirIndexFile()) {
				jEdit.setProperty("options.tags.current-buffer-file-name", tagIndexFilename);
			}
		}
		jEdit.unsetProperty("tags.tagfile.path." + listModel.size());
        jEdit.unsetProperty("tags.tagfile.enabled." + listModel.size());
	} //}}}

	//{{{ editEntry() method
	public void editEntry()
	{
		int index = fileList.getSelectedIndex();
		TagFile tagFile = (TagFile)listModel.elementAt(index);
		if(!tagFile.isCurrentDirIndexFile())
		{
			String[] files = null;
			String dir = MiscUtilities.getParentOfPath(tagFile.getPath());
			View view = GUIUtilities.getView(TagFilesOptionPane.this);
			files = GUIUtilities.showVFSFileDialog(view, dir, 
							VFSBrowser.OPEN_DIALOG, false);
			if(files != null)
			{
				tagFile.setPath(files[0]);
				listModel.set(index, tagFile);
			}
		}
		else
		{
			TagFileDialog dialog = new TagFileDialog(
					TagFilesOptionPane.this, tagIndexFilename, 
					tagFile.getPath().equals(TagFile.SEARCH_DIRECTORY_AND_PARENTS));
			if(dialog.isOK())
			{
				tagIndexFilename = dialog.getFilename();
				tagFile.setPath(dialog.getSearchParents() ? 
					TagFile.SEARCH_DIRECTORY_AND_PARENTS : TagFile.SEARCH_DIRECTORY);
				fileList.repaint();
			}
		}
	} //}}}

	//{{{ updateButtons() method
	protected void updateButtons()
	{
		int index = fileList.getSelectedIndex();
		TagFile tagFile = (TagFile)fileList.getSelectedValue();
		remove.setEnabled(index != -1 && listModel.getSize() != 0 && 
				(tagFile == null || !tagFile.isCurrentDirIndexFile()));
		moveUp.setEnabled(index > 0);
		moveDown.setEnabled(index >= 0 && index < listModel.getSize()-1);
		edit.setEnabled(tagFile != null);
		enable.setEnabled(tagFile != null && !tagFile.isEnabled());
		disable.setEnabled(tagFile != null && tagFile.isEnabled());
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == add)
			{
				String[] files = null;
				View view = GUIUtilities.getView(TagFilesOptionPane.this);
				files = GUIUtilities.showVFSFileDialog(view, null, 
								VFSBrowser.OPEN_DIALOG, false);
				if(files != null)
				{
					int index = fileList.getSelectedIndex();
					if(index == -1)
						index = listModel.getSize();
					else
						index++;
					listModel.insertElementAt(new TagFile(files[0], true), index);
					fileList.setSelectedIndex(index);
					fileList.ensureIndexIsVisible(index);
					updateButtons();
				}
			}
			else if(source == remove)
			{
				int index = fileList.getSelectedIndex();
				listModel.removeElementAt(index);
				if(listModel.getSize() > 0)
					fileList.setSelectedIndex(index-1);
				else
					fileList.setSelectedIndex(index);
				updateButtons();
			}
			else if(source == moveUp)
			{
				int index = fileList.getSelectedIndex();
				Object selected = fileList.getSelectedValue();
				listModel.removeElementAt(index);
				listModel.insertElementAt(selected,index-1);
				fileList.setSelectedIndex(index-1);
				fileList.ensureIndexIsVisible(index-1);
			}
			else if(source == moveDown)
			{
				int index = fileList.getSelectedIndex();
				Object selected = fileList.getSelectedValue();
				listModel.removeElementAt(index);
				listModel.insertElementAt(selected,index+1);
				fileList.setSelectedIndex(index+1);
				fileList.ensureIndexIsVisible(index+1);
			}
			else if(source == enable || source == disable)
			{
				int index = fileList.getSelectedIndex();
				TagFile tagFile = (TagFile)listModel.getElementAt(index);
				tagFile.setEnabled(!tagFile.isEnabled());
				listModel.set(index, tagFile);
				updateButtons();
			}
			else if(source == edit)
			{
				editEntry();
			}
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

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			if(evt.getClickCount() == 2)
			{
				editEntry();
			}
		}
	} //}}}

	//{{{ TagIndexFileCellRenderer class
	class TagIndexFileCellRenderer extends DefaultListCellRenderer
	{
		//{{{ getListCellRendererComponent() method
		public Component getListCellRendererComponent(JList list, Object value,
													   int index, boolean isSelected,
													   boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
			TagFile tagFile = (TagFile)value;

			setText(tagFile.getPath());
			setIcon(tagFile.isEnabled() ? enabledIcon : disabledIcon);

			if(tagFile.isCurrentDirIndexFile())
			{
				if(tagFile.isEnabled())
				{
					if(tagFile.getPath().equals(TagFile.SEARCH_DIRECTORY))
						setIcon(currentIcon);
					else
						setIcon(recursiveIcon);
				}
				setText(tagIndexFilename);
			}

			return this;
		} //}}}
	} //}}}

	//{{{ private declarations
	String tagIndexFilename;
	JList fileList;
	JButton add, remove, moveUp, moveDown, edit, enable, disable;
	DefaultListModel listModel;
	Icon disabledIcon, enabledIcon, currentIcon, recursiveIcon;
	//}}}
}

class TagFileDialog extends EnhancedDialog
{
	//{{{ TagPromptDialog constructor
	public TagFileDialog(Component comp, String tagFilename, boolean searchParents)
	{
		super(GUIUtilities.getParentDialog(comp),
				jEdit.getProperty("options.tags.files.index.title"),
				true);

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JPanel panel = new JPanel(new BorderLayout(4,4));
		panel.setBorder(new EmptyBorder(2,2,2,2));
		panel.add(new JLabel(jEdit.getProperty("options.tags.files.index.filename")),
					BorderLayout.WEST);
		
		filename = new JTextField(tagFilename,20);
		panel.add(filename, BorderLayout.CENTER);
		this.searchParents = new JCheckBox(
					jEdit.getProperty("options.tags.files.index.search-parents"),
					searchParents);
		panel.add(this.searchParents, BorderLayout.SOUTH);

		content.add(panel, BorderLayout.CENTER);

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

		content.add(BorderLayout.SOUTH,buttons);

		pack();
		setLocationRelativeTo(GUIUtilities.getParentDialog(comp));
		show();
	} //}}}

	//{{{ isOK() method
	public boolean isOK()
	{
		return isOK;
	} //}}}

	//{{{ getSearchParents() method
	public boolean getSearchParents()
	{
		return searchParents.isSelected();
	} //}}}

	//{{{ getFilename() method
	public String getFilename(){
		return filename.getText();
	} //}}}

	//{{{ ok() method
	public void ok()
	{
		if(filename.getText() != null && filename.getText().trim().length() > 0)
		{
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
	private JTextField filename;
	private JCheckBox searchParents;
	private JButton ok, cancel;
	private boolean isOK = false;
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

// :collapseFolds=1:noTabs=false:deepIndent=false:folding=explicit:
