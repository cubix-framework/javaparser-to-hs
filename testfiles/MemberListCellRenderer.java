/*
 * MemberListCellRenderer.java
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
import java.util.*;
import javax.swing.*;

import org.jymc.jpydebug.jedit.*;
import org.jymc.jpydebug.*;


public class MemberListCellRenderer 
extends JPanel 
implements ListCellRenderer
{
    private final static String _EMPTY_ = "" ; 
    protected JLabel _typeLabel;
    protected JLabel _nameLabel;
    protected JLabel _paramLabel;
    protected JLabel _closeLabel;

    private static ImageIcon classIcon = new ImageIcon(JPYJeditPlugin.class.getResource("images/class.gif"));
    private static ImageIcon methodIcon = new ImageIcon(JPYJeditPlugin.class.getResource("images/method.gif"));
    private static ImageIcon fieldIcon = new ImageIcon(JPYJeditPlugin.class.getResource("images/field.gif"));
    private static ImageIcon moduleIcon = new ImageIcon(JPYJeditPlugin.class.getResource("images/module.gif"));
//    private static ImageIcon constructorIcon = new ImageIcon(JPYJeditPlugin.class.getResource("images/constructor.gif"));


    public MemberListCellRenderer(Object[] listData) 
    {
      setLayout(new GridBagLayout());
      setOpaque(true);
      GridBagConstraints gbc = new GridBagConstraints();

      _typeLabel = new JLabel();
      _typeLabel.setOpaque(false);
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weightx = .001;
      gbc.insets = new Insets(0, 0, 3, 3);
      add(_typeLabel, gbc);

      _nameLabel = new JLabel();
      _nameLabel.setOpaque(false);
      gbc.insets = new Insets(0, 0, 3, 0);
      add(_nameLabel, gbc);

      _paramLabel = new JLabel();
      _paramLabel.setOpaque(false);
      add(_paramLabel, gbc);

      _closeLabel = new JLabel();
      _closeLabel.setOpaque(false);
      gbc.weightx = .997;
      add(_closeLabel, gbc);
    }


    public void setFont(Font f) 
    {
      super.setFont(f);
      if (_nameLabel != null) 
      {
      Font smallerFont = new Font("SansSerif", Font.PLAIN, Math.max(f.getSize() - 4, 10));
        _typeLabel.setFont(smallerFont);
        _nameLabel.setFont(f);
        _paramLabel.setFont(smallerFont);
        _closeLabel.setFont(f);
        _typeLabel.setPreferredSize(null);
        _typeLabel.setText("ObjectObject");
        _typeLabel.setPreferredSize(_typeLabel.getPreferredSize());
        _typeLabel.setMinimumSize(_typeLabel.getPreferredSize());
      }  
    }


    public int getNameOffset() 
    { return _typeLabel.getPreferredSize().width; }


    public Component getListCellRendererComponent( JList list, 
                                                   Object v,
                                                   int index , 
                                                   boolean isSelected ,
                                                   boolean cellHasFocus ) 
    {
      PythonSyntaxTreeNode value = (PythonSyntaxTreeNode) v ;   
      if (value.isMethod() ) 
        createMethodText(value);
      else if (value.isClass()) 
        createClassText(value);
      else if (value.isModule()) 
        createModuleText(value);
      else if (value.isField()) 
        createFieldText(value);
      else 
        createUnknownText(value);

      Color background = (
          isSelected ? list.getSelectionBackground() : list.getBackground()
        );
      Color foreground = (
            isSelected ? list.getSelectionForeground() : list.getForeground()
        );
      setBackground(background);
      _typeLabel.setForeground(foreground);
      _nameLabel.setForeground(foreground);
      _paramLabel.setForeground(foreground);
      _closeLabel.setForeground(Color.blue);

      return this;
    }

    protected void createMethodText(PythonSyntaxTreeNode value) 
    {
      _typeLabel.setText("");

      _nameLabel.setText(value.get_nodeName());

      StringBuffer params = new StringBuffer() ;
      
      
      Vector arguments = value.get_argList() ;
      if ( arguments != null )
      {
      Enumeration argList = arguments.elements() ; 
        while( argList.hasMoreElements() ) 
        {
        PythonSyntaxTreeNode cur = (PythonSyntaxTreeNode) argList.nextElement() ;
          params.append( cur.get_nodeName()) ; 
          if (argList.hasMoreElements())
          {  
            params.append(',') ;
            params.append(' ') ; 
          }  
        }
      }
      
      _paramLabel.setText("(" + params.toString() + ")");
      _closeLabel.setText("");
      _typeLabel.setIcon(methodIcon);
    }


    protected void createClassText(PythonSyntaxTreeNode value) 
    {
      _typeLabel.setText(_EMPTY_);
      _paramLabel.setText(_EMPTY_) ;
      _nameLabel.setText(value.get_nodeName());
      _typeLabel.setIcon(classIcon);
      _closeLabel.setText(_EMPTY_);
    }

    protected void createModuleText(PythonSyntaxTreeNode value) 
    {
      _typeLabel.setText(_EMPTY_);
      _paramLabel.setText(_EMPTY_) ;
      _nameLabel.setText(value.get_nodeName());
      _typeLabel.setIcon(moduleIcon);
      _closeLabel.setText(_EMPTY_);
    }

    protected void createFieldText(PythonSyntaxTreeNode value) 
    {
      _typeLabel.setText(_EMPTY_);
      _paramLabel.setText(_EMPTY_) ;
      _nameLabel.setText(value.get_nodeName());
      _typeLabel.setIcon(fieldIcon);
      _closeLabel.setText(_EMPTY_);
    }


    protected void createUnknownText(Object value) 
    {
      _typeLabel.setText(_EMPTY_);
      _nameLabel.setText(value.toString());
      _paramLabel.setText(_EMPTY_);
      _closeLabel.setText(_EMPTY_);
    }

    /* REPORTED AS NEVER USED LOCALLY 
    private static String lastName(String name) 
    {
      int i = name.lastIndexOf('.');
      return (i >= 0) ? name.substring(i + 1) : name;
    }
    */
}

