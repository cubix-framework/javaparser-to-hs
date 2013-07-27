/*
 * TagListCellRenderer.java
 * Copyright (c) 2001, 2002 Kenrick Drew
 * kdrew@earthlink.net
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
 */

package tags;

import java.io.*;
import java.lang.*;
import java.lang.System.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

class TagListCellRenderer extends JPanel implements ListCellRenderer 
{
  /***************************************************************************/
  private static final int INDENT = 35; // in pixels
  
  /***************************************************************************/
  private JPanel tagAndExuberantPanel_;
  
  private JPanel tagNameAndFilePanel_;
  private JLabel indexLabel_;
  private JLabel tagLabel_;
  private JLabel pathLabel_;
  private JLabel fileLabel_;
  
  private JPanel searchPanel_;
  private JLabel searchString_;

  private JPanel exuberantPanel_;
  private JLabel exuberantLabel_;
  
  private String fileSeperator;
  
  /***************************************************************************/
  public TagListCellRenderer() 
  {
    super();
    
    fileSeperator = System.getProperty("file.separator");
    
    // create components
    indexLabel_ = new JLabel();
    tagLabel_ = new JLabel();
    pathLabel_ = new JLabel();
    fileLabel_ = new JLabel();
    searchString_ = new JLabel();
    exuberantLabel_ = new JLabel();
    tagAndExuberantPanel_ = new JPanel(new BorderLayout());
    tagNameAndFilePanel_ = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
    exuberantPanel_ = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
    searchPanel_ = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
    
    // setup
    Font plain = new Font("Monospaced", Font.PLAIN, 12);
    Font bold  = new Font("Monospaced", Font.BOLD, 12);
    indexLabel_.setFont(plain); 
    tagLabel_.setFont(bold);
    pathLabel_.setFont(plain);
    fileLabel_.setFont(bold);
    if (false)
    {
      Font labelFont = (Font) UIManager.get("Label.font");
      exuberantLabel_.setFont(new Font(labelFont.getName(), Font.PLAIN, 
                                       labelFont.getSize()));
    }
    else
      exuberantLabel_.setFont(plain);
    searchString_.setFont(plain);
    
    //setOpaque(false);
    
    // Layout
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, tagAndExuberantPanel_);
      tagAndExuberantPanel_.add(BorderLayout.NORTH, tagNameAndFilePanel_);
				tagNameAndFilePanel_.add(indexLabel_);
				tagNameAndFilePanel_.add(tagLabel_);
				tagNameAndFilePanel_.add(pathLabel_);
				tagNameAndFilePanel_.add(fileLabel_);
	    tagAndExuberantPanel_.add(BorderLayout.SOUTH, exuberantPanel_);
        exuberantPanel_.add(Box.createHorizontalStrut(INDENT));
        exuberantPanel_.add(exuberantLabel_);
    add(BorderLayout.SOUTH, searchPanel_);
      searchPanel_.add(Box.createHorizontalStrut(INDENT));
      searchPanel_.add(searchString_);
  }

  /***************************************************************************/
  public Component getListCellRendererComponent(JList list, Object value,
                                                int index, boolean isSelected,
                                                boolean cellHasFocus) {
    
    TagLine tagLine = (TagLine) value;
    
    // Index and tag
    if (tagLine.index_ <= 9)
      indexLabel_.setText(" " + tagLine.index_ + ": ");
    else
      indexLabel_.setText("    ");
    tagLabel_.setText(tagLine.tag_);

    // Path
    File file = new File(tagLine.definitionFile_);
    pathLabel_.setText("  " + file.getParent() + fileSeperator);
    // space is added so that edge of popup is immediately next to end of label
    fileLabel_.setText(file.getName() + " ");

    // exuberant info
    StringBuffer exuberantItems = null;
    if (tagLine.exuberantInfoItems_ != null)
    {
      int size = tagLine.exuberantInfoItems_.size();
      if (size > 0)
        exuberantItems = new StringBuffer();
      ExuberantInfoItem item = null;
      for (int i = 0; i < size; i++)
      {
        item = (ExuberantInfoItem) tagLine.exuberantInfoItems_.elementAt(i);
        exuberantItems.append(item.toHTMLString());
        if (i != (size - 1))
          exuberantItems.append(", ");
        else
          exuberantItems.append(" ");
      }
      item = null;
    }
    exuberantLabel_.setText(exuberantItems != null ? exuberantItems.toString() :
                                                     null);
    exuberantItems = null;
    
    // search string
    if (tagLine.origSearchString_ != null)
      searchString_.setText(tagLine.origSearchString_.trim() + " ");
    else
      searchString_.setText("Line:  " + tagLine.definitionLineNumber_);

    // get background and foreground colors      
    Color background = (isSelected ? list.getSelectionBackground()
                                                       : list.getBackground());
    Color foreground = (isSelected ? list.getSelectionForeground()
                                                       : list.getForeground());
    // set backgrounds on panels
    setBackground(background);
    tagNameAndFilePanel_.setBackground(background);
    tagAndExuberantPanel_.setBackground(background);
    exuberantPanel_.setBackground(background);
    searchPanel_.setBackground(background);
    
    // set foregrounds on text labels
    indexLabel_.setForeground(foreground);
    tagLabel_.setForeground(foreground);
    pathLabel_.setForeground(Color.blue);
    fileLabel_.setForeground(Color.blue);
    searchString_.setForeground(foreground);
    exuberantLabel_.setForeground(foreground);
    
    return this;
  }
}
