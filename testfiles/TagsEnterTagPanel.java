/*
 * TagsEnterTagPanel.java
 * Copyright (c) 2001 Kenrick Drew
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

import org.gjt.sp.jedit.*;

class TagsEnterTagPanel extends JPanel {

  /***************************************************************************/
  static protected boolean debug_ = false;
  
  /***************************************************************************/
  
  // This panel
    protected JPanel labelAndFieldPanel_ = null;
      protected JLabel textFieldLabel_ = null;
      protected JTextField tagFuncTextField_ = null;
      protected JCheckBox  otherWindowCheckBox_ = null;
  
  /***************************************************************************/
  public TagsEnterTagPanel(String initialValue, boolean otherWindow) {
    createComponents();
    setupComponents();
    placeComponents();

    reinitialize(initialValue, otherWindow);
    
    updateGUI();
  }
  
  /***************************************************************************/
  public void setDebug(boolean debug) { debug_ = debug; }

  /***************************************************************************/
  public void reinitialize(String initialValue, boolean otherWindow) {
    tagFuncTextField_.setText(initialValue);
    if (initialValue != null) {
      tagFuncTextField_.setSelectionStart(0);
      tagFuncTextField_.setSelectionEnd(initialValue.length());
    }
    otherWindowCheckBox_.setSelected(otherWindow);
  }
  
  /***************************************************************************/
  protected JTextField getFuncTextField() { return tagFuncTextField_; }
  
  /***************************************************************************/
  public String getFuncName() {
    String funcName = tagFuncTextField_.getText();
    if (funcName != null) {
      funcName = funcName.trim();
      if (funcName.length() == 0)
        funcName = null;
    }
    return funcName;
  }

  /***************************************************************************/
  public boolean getOtherWindow() { 
    return otherWindowCheckBox_.isSelected(); 
  }

  /***************************************************************************/
  protected void createComponents() {
    labelAndFieldPanel_ = new JPanel();
    textFieldLabel_ = new JLabel(
              jEdit.getProperty("tags.enter-tag-dlg.tag-to.label"));
    tagFuncTextField_ = new JTextField(16);
    otherWindowCheckBox_ = new JCheckBox(
              jEdit.getProperty("tags.enter-tag-dlg.new-view-checkbox.label"));
  }
  
  /***************************************************************************/
  protected void setupComponents() {
    labelAndFieldPanel_.setLayout(new BorderLayout(5,5));

    setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
    
    setLayout(new BorderLayout());    

    otherWindowCheckBox_.setMnemonic(KeyEvent.VK_V);

    textFieldLabel_.setLabelFor(tagFuncTextField_);
  }
  
  /***************************************************************************/
  protected void placeComponents() {
    labelAndFieldPanel_.add(textFieldLabel_, BorderLayout.WEST);
    labelAndFieldPanel_.add(tagFuncTextField_, BorderLayout.CENTER);
    labelAndFieldPanel_.add(otherWindowCheckBox_, BorderLayout.SOUTH);

    add(labelAndFieldPanel_, BorderLayout.NORTH);
  }
  
  /***************************************************************************/
  protected void updateGUI() {
    tagFuncTextField_.requestFocus();
  }
  
  /***************************************************************************/
  static public void main(String args[]) {
    debug_ = true;
    
    TagsEnterTagPanel panel = new TagsEnterTagPanel("get_cur_draw", true);
    JFrame frame = new JFrame("Enter And Follow Tag");
    frame.getContentPane().setLayout(new BorderLayout());
    frame.getContentPane().add(panel, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);
  }
}
