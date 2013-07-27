/*
 * PreserveWhitespaceOptionsPane.java
 *
 * Copyright (c) 2003 Robert McKinnon
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
package xml.indent;

import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;

public class PreserveWhitespaceOptionsPane extends AbstractOptionPane {

  private JList elementList;
  private DefaultListModel elementListModel;
  private JButton add;
  private JButton remove;

  public PreserveWhitespaceOptionsPane() {
    super("xmlindenter");
  }

  protected void _init() {
    setLayout(new BorderLayout());

    JLabel label = new JLabel(jEdit.getProperty("options.xmlindenter.caption"));
    label.setBorder(new EmptyBorder(0, 0, 6, 0));
    add(BorderLayout.NORTH, label);

    elementListModel = new DefaultListModel();
    int i = 0;
    String element;
    while((element = jEdit.getProperty("xmlindenter.preserve-whitespace-element." + i)) != null) {
      elementListModel.addElement(element);
      i++;
    }

    elementList = new JList(elementListModel);
    add(BorderLayout.CENTER, new JScrollPane(elementList));
    elementList.addListSelectionListener(new ListHandler());

    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    buttons.setBorder(new EmptyBorder(6, 0, 0, 0));

    add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
    add.setToolTipText(jEdit.getProperty("options.xmlindenter.add"));
    add.addActionListener(new ActionHandler());
    buttons.add(add);
    remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
    remove.setToolTipText(jEdit.getProperty("options.xmlindenter.remove"));
    remove.addActionListener(new ActionHandler());
    buttons.add(remove);
    buttons.add(Box.createGlue());

    add(BorderLayout.SOUTH, buttons);

    updateEnabled();
  }

  protected void _save() {
    int i;
    for (i = 0; i < elementListModel.getSize(); i++) {
      String element = (String)elementListModel.getElementAt(i);
      jEdit.setProperty("xmlindenter.preserve-whitespace-element." + i, element);
    }

    jEdit.unsetProperty("xmlindenter.preserve-whitespace-element." + i);
    jEdit.setProperty("xmlindenter.preserve-whitespace-element.modified", "true");
  }

  private void updateEnabled() {
    boolean selected = (elementList.getSelectedValue() != null);
    remove.setEnabled(selected);
  }

  class ActionHandler implements ActionListener {
    public void actionPerformed(ActionEvent event) {
      if (event.getSource() == add) {
        String property = "options.xmlindenter.dialog";
        String name = GUIUtilities.input(PreserveWhitespaceOptionsPane.this, property, "");

        if(name == null || name.length() == 0)
          return;

        elementListModel.addElement(name);

      } else if (event.getSource() == remove) {
        elementListModel.removeElementAt(elementList.getSelectedIndex());
        updateEnabled();
      }
    }
  }

  class ListHandler implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent evt) {
      updateEnabled();
    }
  }
}
