/*
 * MethodListCellRenderer.java
 * Copyright (c) 1999, 2000, 2001, 2002 CodeAid team
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


package org.jymc.jpydebug.jedit.popup;

import java.awt.*;
import javax.swing.*;
import java.util.* ; 

//import jane.lang.*;
import org.jymc.jpydebug.*;

public class MethodListCellRenderer 
extends JPanel 
implements ListCellRenderer
{
  private final static String _CONTINUATION_ = ", "  ;
  private final static String _OPEN_PAR_ = "(" ;
  private final static String _CLOSE_PAR_ = ")" ;
  
    private JLabel _nameLabel;
    private JLabel _paramLabel;
    private JLabel _closeLabel;


    public MethodListCellRenderer(PythonSyntaxTreeNode[] listData) 
    {
      _nameLabel = new JLabel();
      _paramLabel = new JLabel();
      _closeLabel = new JLabel();

      JPanel panel2 = new JPanel();
      panel2.setOpaque(false);
      panel2.setLayout(new BorderLayout());
      panel2.add(BorderLayout.WEST, _paramLabel);
      panel2.add(BorderLayout.CENTER, _closeLabel);

      setLayout(new BorderLayout());
      add(BorderLayout.WEST, _nameLabel);
      add(BorderLayout.CENTER, panel2);

      if (listData.length > 0) 
        _nameLabel.setText(listData[0].get_nodeName() + _OPEN_PAR_);
       
    }


    public void setFont(Font f) 
    {
      super.setFont(f);
      if (_nameLabel != null) 
      {
      Font smallerFont = new Font("SansSerif", Font.PLAIN,
                                   Math.max(f.getSize() - 4, 10));
        _nameLabel.setFont(f);
        _paramLabel.setFont(smallerFont);
        _closeLabel.setFont(f);
      }
    }


    public int getNameOffset() 
    {
      return _nameLabel.getPreferredSize().width;
    }

    public Component getListCellRendererComponent(JList list, 
                                                  Object value,
                                                  int index, 
                                                  boolean isSelected,
                                                  boolean cellHasFocus) 
    {
    PythonSyntaxTreeNode mi = (PythonSyntaxTreeNode) value;
      _nameLabel.setText(mi.get_nodeName() + _OPEN_PAR_);

      StringBuffer params = new StringBuffer();
      Enumeration paramNames = mi.get_argList().elements();
      boolean first = true ; 
      while ( paramNames.hasMoreElements())
      {
      PythonSyntaxTreeNode cur = (PythonSyntaxTreeNode) paramNames.nextElement() ;
        if ( first )
          first = false ; 
        else
          params.append(_CONTINUATION_) ; 
        params.append( cur.get_nodeName()) ;
      }

      _paramLabel.setText(params.toString());
      _closeLabel.setText(_CLOSE_PAR_);

      Color background = (
          isSelected ? list.getSelectionBackground() : list.getBackground()
      );
      Color foreground = (
          isSelected ? list.getSelectionForeground() : list.getForeground()
      );
      setBackground(background);
      _nameLabel.setForeground(foreground);
      _paramLabel.setForeground(foreground);
      _closeLabel.setForeground(foreground);

      return this;
    }

    /* REPORTED AS NEVER USED LOCALLY 
    private static String lastName(String name) 
    {
    int i = name.lastIndexOf('.');
      return (i >= 0) ? name.substring(i + 1) : name;
    }
    */
}

