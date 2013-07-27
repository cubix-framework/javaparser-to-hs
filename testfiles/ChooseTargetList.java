/*
 * ChooseTagList.java
 *
 * Copyright (c) 2001, 2002 Kenrick Drew
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
 * $Id: ChooseTagList.java,v 1.11 2004/11/07 15:52:34 orutherfurd Exp $
 */
/*
 * This file originates from the Tags Plugin version 2.0.1
 * whose copyright and licensing is seen above.
 * The original file was modified to become the derived work you see here
 * in accordance with Section 2 of the Terms and Conditions of the GPL v2.
 *
 * The derived work is called the CscopeFinder Plugin and is
 * Copyright 2006 Dean Hall.
 *
 * 2006/08/09
 */

package cscopefinder;

//{{{ imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSManager;
//}}}

public class ChooseTargetList extends JList {

	//{{{ ChooseTargetList constructor
	public ChooseTargetList(Vector tagLines) {
		super();
		if(tagLines != null){
			setListData(tagLines);
			// Generally 8 is the magic number for the number of 
			// visible items/rows in a list or menu, but we do 8 
			// to 12 b/c each item is actually 2 or 3 rows
			setVisibleRowCount(Math.min(tagLines.size(),4));
		}
		else{
			setVisibleRowCount(4);
		}
		setSelectedIndex(0);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setCellRenderer(new TagListCellRenderer());
	} //}}}

	//{{{ TagListCellRenderer class
	static class TagListCellRenderer extends JPanel implements ListCellRenderer 
	{
		private static final int INDENT = 35; // in pixels

		//{{{ private declarations
		private JPanel tagAndExuberantPanel;
		private JPanel tagNameAndFilePanel;
		private JLabel indexLabel;
		private JLabel tagLabel;
		private JLabel pathLabel;
		private JLabel fileLabel;
		private JPanel searchPanel;
		private JLabel searchString;
		private JPanel exuberantPanel;
		private JLabel exuberantLabel;
		//}}}
		
		//{{{ TagListCellRenderer constructor
		public TagListCellRenderer()
		{
			super();

			// create components
			indexLabel = new JLabel();
			tagLabel = new JLabel();
			pathLabel = new JLabel();
			fileLabel = new JLabel();
			searchString = new JLabel();
			exuberantLabel = new JLabel();
			tagAndExuberantPanel = new JPanel(new BorderLayout());
			tagNameAndFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			exuberantPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
			searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));

			// setup
			Font plain = new Font("Monospaced", Font.PLAIN, 12);
			Font bold  = new Font("Monospaced", Font.BOLD, 12);
			indexLabel.setFont(plain); 
			tagLabel.setFont(bold);
			pathLabel.setFont(plain);
			fileLabel.setFont(bold);
			exuberantLabel.setFont(plain);
			searchString.setFont(plain);

			// Layout
			setLayout(new BorderLayout());
			add(BorderLayout.CENTER, tagAndExuberantPanel);
			tagAndExuberantPanel.add(BorderLayout.NORTH, tagNameAndFilePanel);
			tagNameAndFilePanel.add(indexLabel);
			tagNameAndFilePanel.add(tagLabel);
			tagNameAndFilePanel.add(pathLabel);
			tagNameAndFilePanel.add(fileLabel);
			tagAndExuberantPanel.add(BorderLayout.SOUTH, exuberantPanel);
			exuberantPanel.add(Box.createHorizontalStrut(INDENT));
			exuberantPanel.add(exuberantLabel);
			add(BorderLayout.SOUTH, searchPanel);
			searchPanel.add(Box.createHorizontalStrut(INDENT));
			searchPanel.add(searchString);
			plain = null;
			bold = null;
		} //}}}

		//{{{ getListCellRendererComponent() method
		public Component getListCellRendererComponent(JList list, Object value,
							int index, boolean isSelected, boolean cellHasFocus)
		{
			TargetLine tagLine = (TargetLine)value;

			// Index and tag
			if(tagLine.getIndex() <= 9)
				indexLabel.setText(" " + tagLine.getIndex() + ": ");
			else
				indexLabel.setText("    ");
			tagLabel.setText(tagLine.getTag());

			// Path
			String definitionFile = tagLine.getDefinitionFileName();
			VFS vfs = VFSManager.getVFSForPath(definitionFile);
			pathLabel.setText("  " + vfs.getParentOfPath(definitionFile));
			// space is added so that edge of popup is immediately next to end of label
			fileLabel.setText(vfs.getFileName(definitionFile) + " ");
			
			// exuberant info
			StringBuffer exuberantItems = null;
			if(tagLine.getExuberantInfoItems() != null)
			{
				int size = tagLine.getExuberantInfoItems().size();
				if(size > 0)
					exuberantItems = new StringBuffer();
				ExuberantInfoItem item = null;
				for(int i = 0; i < size; i++)
				{
					item = (ExuberantInfoItem) tagLine.getExuberantInfoItems().elementAt(i);
					exuberantItems.append(item.toHTMLString());
					if(i != (size - 1))
						exuberantItems.append(", ");
					else
						exuberantItems.append(" ");
				}
				item = null;
			}
			exuberantLabel.setText(exuberantItems != null ? exuberantItems.toString() : null);
			exuberantItems = null;

			// search string
			if(tagLine.getSearchString() != null)
				searchString.setText(tagLine.getSearchString().trim() + " ");
			else
				searchString.setText("Line:  " + tagLine.getDefinitionLineNumber());

			// get background and foreground colors
			Color background = (isSelected ? list.getSelectionBackground() : list.getBackground());
			Color foreground = (isSelected ? list.getSelectionForeground() : list.getForeground());
			// set backgrounds on panels
			setBackground(background);
			tagNameAndFilePanel.setBackground(background);
			tagAndExuberantPanel.setBackground(background);
			exuberantPanel.setBackground(background);
			searchPanel.setBackground(background);

			// set foregrounds on text labels
			indexLabel.setForeground(foreground);
			tagLabel.setForeground(foreground);
			pathLabel.setForeground(Color.blue);
			fileLabel.setForeground(Color.blue);
			searchString.setForeground(foreground);
			exuberantLabel.setForeground(foreground);

			return this;
		} //}}}
	} //}}}
}
