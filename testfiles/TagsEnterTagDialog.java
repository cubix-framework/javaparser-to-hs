/*
 * TagsEnterTagDialog.java
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
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.gui.*;

public class TagsEnterTagDialog extends JDialog {
  
  static protected boolean debug_ = false;

  /***************************************************************************/
  protected JPanel contentPanel_ = null;
    protected TagsEnterTagPanel enterTagPanel_ = null;

  protected JPanel buttonPanelFlow_ = null;
      protected JPanel buttonPanelGrid_ = null;
        protected JButton okButton_ = null;
        protected JButton cancelButton_ = null;
      
  protected View view_ = null;
  
  protected boolean returnValue_ = true;
  
  /***************************************************************************/
  public TagsEnterTagDialog(View view, String initialValue, 
                           boolean otherWindow) {

    super(view, jEdit.getProperty("tags.enter-tag-dlg.title"), true);
    
    view_ = view;
    
    createComponents(initialValue, otherWindow);
    setupComponents();
    placeComponents();
  }
  
  /***************************************************************************/
  public boolean showDialog() {
    pack();

    // Place dialog in center of screen
    setLocationRelativeTo(view_);

    // Set focus to text field
    GUIUtilities.requestFocus(this, enterTagPanel_.tagFuncTextField_);

    // show dialog
    show();
    
    return returnValue_;
  }

  /***************************************************************************/
  public String getFuncName() {
    String funcName = enterTagPanel_.getFuncName();
    if (funcName != null) {
      funcName = funcName.trim();
      if (funcName.length() == 0)
        funcName = null;
    }
    return funcName;
  }

  /***************************************************************************/
  public boolean getOtherWindow() { 
    return enterTagPanel_.getOtherWindow();
  }

  /***************************************************************************/
  protected void createComponents(String initialValue, boolean otherWindow) {
    contentPanel_ = new JPanel(new BorderLayout());
    enterTagPanel_ = new TagsEnterTagPanel(initialValue, otherWindow);

    buttonPanelFlow_ = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanelGrid_ = new JPanel(new GridLayout(1,0,5,5));
    okButton_ = new JButton(jEdit.getProperty("options.tags.tag-ok.label"));
    cancelButton_ = 
               new JButton(jEdit.getProperty("options.tags.tag-cancel.label"));
  }
  
  /***************************************************************************/
  protected void setupComponents() {
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    enterTagPanel_.setDebug(debug_);

    getContentPane().setLayout(new BorderLayout());
    
    okButton_.addActionListener(okButtonListener_);
    cancelButton_.addActionListener(cancelButtonListener_);

    okButton_.setMnemonic(KeyEvent.VK_O);
    cancelButton_.setMnemonic(KeyEvent.VK_C);

    enterTagPanel_.getFuncTextField().addKeyListener(keyListener_);
    addKeyListener(keyListener_);

    getRootPane().setDefaultButton(okButton_);
  }
  
  /***************************************************************************/
  protected void placeComponents() {
    
    buttonPanelGrid_.add(okButton_);
    buttonPanelGrid_.add(cancelButton_);
    
    buttonPanelFlow_.add(buttonPanelGrid_);
    
    getContentPane().add(enterTagPanel_, BorderLayout.CENTER);
    getContentPane().add(buttonPanelFlow_, BorderLayout.SOUTH);
  }

  /*+*************************************************************************/
  protected ActionListener okButtonListener_ = new ActionListener() 
  {
    public void actionPerformed(ActionEvent e) 
    {
      returnValue_ = true;
      dispose();
    }
  };
  
  /*+*************************************************************************/
  protected ActionListener cancelButtonListener_ = new ActionListener() 
  {
    public void actionPerformed(ActionEvent e) 
    {
      returnValue_ = false;
      dispose();
    }
  };

  /*+*************************************************************************/
  protected KeyListener keyListener_ = new KeyListener() 
  {
   public void keyReleased(KeyEvent e) {}
   public void keyTyped(KeyEvent e) {} 
   public void keyPressed(KeyEvent e) 
    {
      e = KeyEventWorkaround.processKeyEvent(e);
      if (e == null)
        return;
    
      switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
          cancelButtonListener_.actionPerformed(null);
          e.consume();
          break;
       case KeyEvent.VK_ENTER:
         if (getFocusOwner() == enterTagPanel_.tagFuncTextField_)
         {
           okButtonListener_.actionPerformed(null);
           e.consume();
         }
         break;
      }
   }
  };
}
