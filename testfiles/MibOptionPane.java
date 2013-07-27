/*
 * MibOptionPane.java - The option pane of the MibSideKick plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009 Matthieu Casanova
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

package gatchan.jedit.mibsidekick;

//{{{ Imports
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Collection;
import java.io.File;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
//}}}


/**
 * @author Matthieu Casanova
 */
public class MibOptionPane extends AbstractOptionPane
{
	private DefaultListModel listModel;
	private static final String SEARCH_LIST_PROPS = "MibSideKick.searchPaths";

	private JButton addButton;
	private JButton removeButton;
	private JList searchList;

	//{{{ RFCReaderOptionPane constructor
	public MibOptionPane()
	{
		super("rfcreader");
	} //}}}

	//{{{ _init() method
	public void _init()
	{
		listModel = new DefaultListModel();
		searchList = new JList(listModel);
		Collection<File> pathList = getPaths();
		for (File path : pathList)
		{
			addPath(path);
		}
		JScrollPane scroll = new JScrollPane(searchList);
		scroll.setBorder(BorderFactory.createTitledBorder(
			jEdit.getProperty("messages.mibsidekick.searchPath")));
		addComponent(scroll, GridBagConstraints.HORIZONTAL);


		addButton = new JButton(jEdit.getProperty("messages.mibsidekick.addPath"));
		removeButton = new JButton(jEdit.getProperty("messages.mibsidekick.removePath"));
		MyActionListener actionListener = new MyActionListener();
		addButton.addActionListener(actionListener);
		removeButton.addActionListener(actionListener);
		JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,5));
		buttonPanel.add(addButton);
                buttonPanel.add(removeButton);
		addComponent(buttonPanel);
	} //}}}

	private void addPath(File path)
	{
		if (!listModel.contains(path))
			listModel.addElement(path);
	}

	//{{{ _save() method
	public void _save()
	{
		int size = listModel.getSize();
		if (size == 0)
		{
			jEdit.setProperty(SEARCH_LIST_PROPS, null);
		}
		else
		{
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < size; i++)
			{
				builder.append(((File)listModel.get(i)).getAbsolutePath());
				if (i < size - 1)
					builder.append(File.pathSeparatorChar);
			}
			jEdit.setProperty(SEARCH_LIST_PROPS, builder.toString());
		}
	} //}}}

	static Collection<File> getPaths()
	{
		Collection<File> pathList = new ArrayList<File>();
		String paths = jEdit.getProperty(SEARCH_LIST_PROPS);
		if (paths != null)
		{
			StringTokenizer tokenizer = new StringTokenizer(paths, File.pathSeparator);
			while (tokenizer.hasMoreTokens())
			{
				pathList.add(new File(tokenizer.nextToken()));
			}
		}
		return pathList;
	}

	private class MyActionListener implements ActionListener
	{
		private JFileChooser directoryChooser;

		public void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if (source == addButton)
			{
				if (directoryChooser == null)
				{
					directoryChooser = new JFileChooser();
					directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					directoryChooser.setMultiSelectionEnabled(true);
					directoryChooser.setApproveButtonText("Choose");
					directoryChooser.setDialogTitle("Choose Search Path(s)");
					directoryChooser.setFileFilter(new DirectoryFilter());
				}



				int returnVal = directoryChooser.showOpenDialog(MibOptionPane.this);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File[] selectedFiles = directoryChooser.getSelectedFiles();
					for (int i = 0; i < selectedFiles.length; i++)
					{
						addPath(selectedFiles[i]);
					}
				}
			}
			else if (source == removeButton)
			{
				Object[] paths = searchList.getSelectedValues();
				for (Object path : paths)
				{
					listModel.removeElement(path);
				}
			}
		}

		private class DirectoryFilter extends FileFilter
		{
			public boolean accept(File f)
			{
				return f.isDirectory();
			}

			public String getDescription()
			{
				return "Accepts only directories";
			}
		}
	}
} //}}}