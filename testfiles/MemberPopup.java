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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import javax.swing.*;

import org.jymc.jpydebug.*;

public class MemberPopup 
extends CodeAidPopup 
implements MouseListener
{
    protected JList list;
    protected Object[] allMembers;
    private String oldTypedText;
    private JLabel className;

    private static ImageIcon classIcon = new ImageIcon(MemberPopup.class.getResource("/class.gif"));


    public MemberPopup(List listData) 
    { this( listData.toArray(new Object[listData.size()])); }


    public MemberPopup(Object[] listData) 
    {
      oldTypedText = "";
      allMembers = listData;
      list = new JList(allMembers);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setCellRenderer(createCellRenderer(allMembers));
      if (list.getModel().getSize() > 0) 
      {
        className = new JLabel(((PythonSyntaxTreeNode) list.getModel()
            .getElementAt(0)).get_declaringClassName());
      } 
      else 
      {
        className = new JLabel("N/A");
      }
    }


    /**
     * Sets the SelectedIndex attribute of the MemberPopup object
     */
    public void setSelectedIndex(int index) 
    {
      // for some reason this line makes it much faster
      list.ensureIndexIsVisible(index);

      list.setSelectedIndex(index);
      int rowCount = list.getVisibleRowCount();
      list.ensureIndexIsVisible(Math.max(index - (rowCount - 1) / 2, 0));
      list.ensureIndexIsVisible(
          Math.min(index + rowCount / 2, list.getModel().getSize() - 1)
      );

      if (hint != null) 
        hint.setMember((PythonSyntaxTreeNode) list.getSelectedValue());
    }


    /**
     * Gets the NameOffset attribute of the MemberPopup object
     */
    public int getNameOffset() 
    { return 1 + ((MemberListCellRenderer) list.getCellRenderer()).getNameOffset();}


    public void mouseClicked(MouseEvent evt) 
    { useCurrentSelection(evt);}


    public void mouseReleased(MouseEvent evt) {}


    public void mousePressed(MouseEvent evt) {}


    public void mouseEntered(MouseEvent evt) {}


    public void mouseExited(MouseEvent evt) {}


    /**
     * Create the component that the popup will display.
     */
    protected JComponent createPopupComponent() 
    {
    JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createLoweredBevelBorder());
      GridBagConstraints gbc = new GridBagConstraints();

      className.setIcon(classIcon);
      className.setHorizontalAlignment(JLabel.LEFT);
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      gbc.weighty = .001;
      panel.add(className, gbc);

      list.addMouseListener(this);
      list.setVisibleRowCount(Math.min(allMembers.length, 5));
      ((Component) list.getCellRenderer()).setFont(textArea.getPainter().getFont());
      JScrollPane scroll = new JScrollPane(list);
      scroll.setBorder(null);
      gbc.gridy++;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weighty = .999;
      panel.add(scroll, gbc);
      return panel;
    }


    protected ListCellRenderer createCellRenderer(Object[] listData) 
    {return new MemberListCellRenderer(listData);}


    /**
     * Gets the KeyHandler attribute of the MemberPopup object
     */
    protected KeyListener createKeyEventInterceptor() 
    {return new KeyHandler(); }


    protected void updateTypedText() 
    {
    DefaultListModel model = (
          (list.getModel() instanceof DefaultListModel)
        ? (DefaultListModel) list.getModel()
        : new DefaultListModel()
    );

      if (!model.isEmpty() && typedText.startsWith(oldTypedText)) 
      {
        // Text added, start with current list
        for (int i = 0; i < model.getSize(); i++) 
        {
          if (!model.elementAt(i).toString().startsWith(typedText)) 
          {
            model.removeElementAt(i);
            i--;
          }
        }
      } 
      else 
      {
        // Text removed, rebuild list.
        model.clear();
        for (int i = 0; i < allMembers.length; i++) 
        {
          if (typedText.length() == 0 || allMembers[i].toString().startsWith(typedText)) 
            model.addElement(allMembers[i]);
        }
      }
      
      oldTypedText = typedText;
      list.setModel(model);
      if (model.isEmpty() || 
          (model.getSize()==1 && model.elementAt(0).toString().equals(typedText) )) 
      {
        list.getSelectionModel().clearSelection();
        dispose();
        return;
      } 
      setSelectedIndex(0);
  }


  protected void useCurrentSelection(EventObject evt) 
  {
  PythonSyntaxTreeNode value = (PythonSyntaxTreeNode)list.getSelectedValue();
    if (value != null) 
    {
    String s = value.get_nodeName();
      if ( value.isClass()) 
      {
        // check , if in an import line --> no auto-import
        // int actLine = textArea.getCaretLine();
        // String line = textArea.getLineText(actLine);
      } 
      else if (value.isMethod()) 
      {
        if ( value.get_argList() == null ) 
        {
          s += "()";
        }
      }
      /*
      else if (value instanceof MemberInfo) {
              s = ((MemberInfo) value).getName();
          } else {
              s = value.toString();
          }*/

      String text = typedText;
      dispose();
      textArea.requestFocus();
      if (text.length() > 0) 
        s= s.substring(text.length() );
          
      textArea.getBuffer().insert(textArea.getCaretPosition(), s);
      
      if ( value.isMethod()  &&  value.get_argList() != null ) 
        textArea.userInput('(');

      textArea.requestFocus();
    }
  }


    
    /**
     * Extends {@link CodeAidPopup#KeyHandler} to provider more key handling.
     */
    protected class KeyHandler 
    extends CodeAidPopup.KeyHandler
    {
      public void keyTyped(KeyEvent evt) 
      {
        super.keyTyped(evt);
        char c = evt.getKeyChar();
        if (c != KeyEvent.CHAR_UNDEFINED && c != '\b' && c != '\t') 
        {
          if (Character.isJavaIdentifierPart(c)) 
            updateTypedText();
          else 
            dispose();
        }
      }


      public void keyPressed(KeyEvent evt) 
      {
        switch (evt.getKeyCode()) 
        {
          case KeyEvent.VK_UP:
            setSelectedIndex(Math.max(list.getSelectedIndex() - 1, 0));
            evt.consume();
            break;

          case KeyEvent.VK_DOWN:
            setSelectedIndex(Math.min(list.getSelectedIndex() + 1,
            list.getModel().getSize() - 1));
            evt.consume();
            break;
          
          case KeyEvent.VK_PAGE_UP:
            setSelectedIndex(Math.max(list.getSelectedIndex() -
            list.getVisibleRowCount(), 0));
            evt.consume();
            break;

          case KeyEvent.VK_PAGE_DOWN:
            setSelectedIndex(Math.min(list.getSelectedIndex() +
            list.getVisibleRowCount(),
            list.getModel().getSize() - 1));
            evt.consume();
            break;

          case KeyEvent.VK_ENTER:// Fall-through, enter or tab have the same behaviour
          case KeyEvent.VK_TAB:
             useCurrentSelection(evt);
             evt.consume();
             break;

          case KeyEvent.VK_BACK_SPACE:
          super.keyPressed(evt);
            updateTypedText();
             break;

          default:
            super.keyPressed(evt);
            break;
        }
      }
    }
}

