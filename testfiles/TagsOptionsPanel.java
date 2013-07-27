/*
 * TagsOptionsPanel.java
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

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;

class TagsOptionsPanel extends AbstractOptionPane {

  /***************************************************************************/
  // This panel
		// Row 1
		protected JPanel parserPanel_;  // labeled panel (not currently labeled)
			protected JPanel parserButtonPanel_;  // panel w/ radio group
				protected ButtonGroup parserGroup_;
	
		// Row 2
		protected JPanel tagFilesPanel_; // labeled panel (not currently labeled)
			protected JPanel tagFilesUIPanel_;  // panel w/ tag files ui components
				
				// North
				protected JPanel tagFilesOptionsPanel_;
					protected JCheckBox useCurrentBufTagFileCheckBox_;
          protected JPanel curBufFilenamePanel_;
            protected JLabel curBufFileNameLabel_;
            protected JTextField curBufFileNameField_;
					protected JCheckBox searchAllFilesCheckBox_;
           
				
				// Center
				protected JScrollPane pane_;
					protected JList list_;
						protected DefaultListModel listModel_;
				 
				// South
				protected JPanel buttonPanel_;  // panel w/ buttons
					protected JButton addButton_;
					protected JButton removeButton_;
					protected JButton moveUpButton_;
					protected JButton moveDownButton_;
			 
		// Row 3
		protected JCheckBox debugCheckBox_;
		 
  protected int origParserType_;
  
  static protected boolean newCurBufStuff_ = false;
  
  /***************************************************************************/
  public TagsOptionsPanel() {
    super("tags");
  }
  
  /***************************************************************************/
  public void _init() {
    
    setup();

    Log.log(Log.DEBUG, this, "Current tag index files (init):");
    int numFiles = Tags.tagFiles_.size();
    TagFile tf = null;
    for (int i = 0; i < numFiles; i++)
    {
      tf = (TagFile) Tags.tagFiles_.elementAt(i);
      if (tf != null)
      {
        Log.log(Log.DEBUG, this, tf.toDebugString());
        listModel_.addElement(tf);
      }
    }
    tf = null;
  }
  
  /***************************************************************************/
  public void _save() {
    Tags.setSearchAllTagFiles(searchAllFilesCheckBox_.isSelected());
    Tags.setUseCurrentBufTagFile(useCurrentBufTagFileCheckBox_.isSelected());
    
    jEdit.setProperty("options.tags.current-buffer-file-name", 
                      curBufFileNameField_.getText());
    
    TagsPlugin.debug_ = debugCheckBox_.isSelected();

    // remove all tag index files and reload with the ones from the pane
    Log.log(Log.DEBUG, this, "Tag index files (from pane):");    
    Tags.clearTagFiles();
    int numTagFiles = listModel_.getSize();
    TagFile tf = null;
    for (int i = 0; i < numTagFiles; i++)
    {
      tf = (TagFile) listModel_.get(i);
      if (tf != null)
      {
        Log.log(Log.DEBUG, this, tf.toDebugString());
        Tags.tagFiles_.add(tf);
      }
    }
    tf = null;
  }
  
  /***************************************************************************/
  protected void setup() {
    
    createComponents();
    setupComponents();
    
    JRadioButton b;
    parserGroup_ = new ButtonGroup();
    origParserType_ = Tags.getParserType();
    for (int i = 0; i < Tags.NUM_PARSERS; i++) {
      b = new JRadioButton(Tags.parsers_[i].toString());
      if (i == origParserType_)
        b.setSelected(true);

      b.setActionCommand(String.valueOf(i));
      b.addActionListener(parserRadioListener_);
      
      parserGroup_.add(b);
      parserButtonPanel_.add(b);
      b = null;
    }
    
    placeComponents();
    
    updateGUI();
  }
  
  /***************************************************************************/
  protected void createComponents() {

		parserPanel_ = new JPanel(new GridLayout(0,1,0,0));
			parserButtonPanel_ = new JPanel(new GridLayout(0,1,0,0));
				//parserPanel_.setBorder(
				//   BorderFactory.createTitledBorder(
				//                       jEdit.getProperty(TagsPlugin.OPTION_PREFIX +
				//                                         "tag-file-type.title")));
			parserButtonPanel_.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
			
		tagFilesPanel_ = new JPanel(new GridLayout(0,1,0,0));
			//tagFilesPanel_.setBorder(
			//  BorderFactory.createTitledBorder(jEdit.getProperty(
			//               TagsPlugin.OPTION_PREFIX + "tag-search-files.label")));
			
			tagFilesUIPanel_ = new JPanel(new BorderLayout(5,5));
			tagFilesUIPanel_.setBorder(BorderFactory.createEmptyBorder(0,5,5,5));
			
				tagFilesOptionsPanel_ = new JPanel(new GridLayout(0,1,0,0));
			
					useCurrentBufTagFileCheckBox_ = new JCheckBox(
		            jEdit.getProperty(
                       "options.tags.tag-search-current-buff-tag-file.label"));
          curBufFilenamePanel_ = new JPanel(new BorderLayout(5,0));
             curBufFileNameLabel_ = new JLabel(jEdit.getProperty(
                               "options.tags.current-buffer-file-name.label"));
             curBufFileNameField_ = new JTextField(jEdit.getProperty(
                             "options.tags.current-buffer-file-name"));
					searchAllFilesCheckBox_ = new JCheckBox(
								 jEdit.getProperty("options.tags.tag-search-all-files.label"));
																	
				listModel_ = new DefaultListModel();
		
				list_ = new JList(listModel_);
				pane_ = new JScrollPane(list_, 
																JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
																JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					
				buttonPanel_ = new JPanel(new GridLayout(1,0,5,5));
					addButton_ = new JButton(
									jEdit.getProperty("options.tags.tag-search-files-add.label"));
					removeButton_ = new JButton(
							 jEdit.getProperty("options.tags.tag-search-files-remove.label"));
					moveUpButton_ = new JButton(
						  jEdit.getProperty("options.tags.tag-search-files-move-up.label"));
					moveDownButton_ = new JButton(
					  jEdit.getProperty("options.tags.tag-search-files-move-down.label"));
			
    debugCheckBox_ = new JCheckBox("Debug");
  }
  
  /***************************************************************************/
  protected void setupComponents() {
    
    useCurrentBufTagFileCheckBox_.addActionListener(useCurBufTagFileListener_);
    
    curBufFileNameLabel_.setLabelFor(curBufFileNameField_);
    
    addButton_.addActionListener(addButtonListener_);
    removeButton_.addActionListener(removeButtonListener_);
    moveUpButton_.addActionListener(moveUpButtonListener_);
    moveDownButton_.addActionListener(moveDownButtonListener_);
    list_.addListSelectionListener(listSelectionListener_);
    
    curBufFileNameField_.getDocument().addDocumentListener(
                                                      curBufFilenameListener_);
    
    useCurrentBufTagFileCheckBox_.setSelected(Tags.getUseCurrentBufTagFile());
    searchAllFilesCheckBox_.setSelected(Tags.getSearchAllTagFiles());
                                    
    addButton_.setMnemonic(KeyEvent.VK_A);
    removeButton_.setMnemonic(KeyEvent.VK_R);
    moveUpButton_.setMnemonic(KeyEvent.VK_U);
    moveDownButton_.setMnemonic(KeyEvent.VK_D);
    
    debugCheckBox_.setSelected(TagsPlugin.debug_);
  }
  
  /***************************************************************************/
  protected void placeComponents() {
    
    // See Note 1 in Tags.java
    //addSeparator("options.tags.tag-file-type.title");
    //parserPanel_.add(parserButtonPanel_);
    //addComponent(parserPanel_);

    //addSeparator("options.tags.tag-search-files.label");
    tagFilesOptionsPanel_.add(useCurrentBufTagFileCheckBox_);
      curBufFilenamePanel_.add(curBufFileNameLabel_, BorderLayout.WEST);
      curBufFilenamePanel_.add(curBufFileNameField_, BorderLayout.CENTER);
    tagFilesOptionsPanel_.add(curBufFilenamePanel_);
    tagFilesOptionsPanel_.add(searchAllFilesCheckBox_);
    tagFilesUIPanel_.add(tagFilesOptionsPanel_, BorderLayout.NORTH);
    tagFilesPanel_.add(tagFilesUIPanel_);

    tagFilesUIPanel_.add(pane_, BorderLayout.CENTER);
    
    buttonPanel_.add(addButton_);    
    buttonPanel_.add(removeButton_);    
    buttonPanel_.add(moveUpButton_);    
    buttonPanel_.add(moveDownButton_);
    tagFilesUIPanel_.add(buttonPanel_, BorderLayout.SOUTH);    

    addComponent(tagFilesPanel_);

    //addComponent(debugCheckBox_);
  }
  
  /***************************************************************************/
  protected ActionListener addButtonListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      int selectedIndex = list_.getSelectedIndex();
      if (selectedIndex == -1)
        selectedIndex = 0;

      String newTagFiles[] = null;

      newTagFiles = GUIUtilities.showVFSFileDialog(null, null, 
                                                   VFSBrowser.OPEN_DIALOG,
                                                   false);
        
      if (newTagFiles != null) 
      {
        for (int i = 0; i < newTagFiles.length; i++) 
        {
          listModel_.add(selectedIndex, new TagFile(newTagFiles[i], 
                                                    TagFile.DEFAULT_CATAGORY));
        }
        
        list_.setSelectedIndex(selectedIndex);
        list_.ensureIndexIsVisible(selectedIndex);
      }
      
      updateGUI();
    }
  };

  /***************************************************************************/
  protected ActionListener removeButtonListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      int selectedIndices[] = list_.getSelectedIndices();
      for (int i = selectedIndices.length - 1; i >= 0; i--) 
      {
        listModel_.removeElementAt(selectedIndices[i]);      
      }
      
      int newSize = listModel_.size();
      if (newSize != 0) {
        int index = selectedIndices[0];
        if (index > (newSize - 1))
          index = newSize - 1;
        list_.setSelectedIndex(index);
        list_.ensureIndexIsVisible(index);
      }
      
      updateGUI();
      
    }
  };
  
  /***************************************************************************/
  protected ActionListener moveUpButtonListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      moveItem(-1);
      updateGUI();
    }
  };

  /***************************************************************************/
  protected ActionListener moveDownButtonListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      moveItem(+1);
      updateGUI();
    }
  };

  /***************************************************************************/
  protected ActionListener parserRadioListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      try {
        int parserType = Integer.parseInt(e.getActionCommand());
        Tags.setParserType(parserType);
      }
      catch (NumberFormatException nfe) {
        Log.log(Log.DEBUG, this, nfe);
      }
    }
  };
  
  /***************************************************************************/
  protected void moveItem(int indexDir) {
    int selectedIndex = list_.getSelectedIndex();
    int newIndex = 0;
    Object element = list_.getSelectedValue();

    newIndex = selectedIndex + indexDir;

    listModel_.removeElementAt(selectedIndex);
    listModel_.add(newIndex, element);
    
    list_.setSelectedIndex(newIndex);
    list_.ensureIndexIsVisible(newIndex);
  }


  /***************************************************************************/
  protected ListSelectionListener listSelectionListener_ =
                                                  new ListSelectionListener() {
    public void valueChanged(ListSelectionEvent e) {
      updateGUI(); 
    }
  };
  
  /***************************************************************************/
  protected ActionListener useCurBufTagFileListener_ = new ActionListener() {
    public void actionPerformed(ActionEvent e) 
    {
      updateGUI();
      
      if (useCurrentBufTagFileCheckBox_.isSelected())  // add
      {
        String path = curBufFileNameField_.getText();
        path = path.trim();

        TagFile tf = new TagFile(path, TagFile.DEFAULT_CATAGORY);
        tf.currentDirIndexFile_ = true;
        
        if (newCurBufStuff_)
          listModel_.add(0, tf);
      }
      else  // remove
      {
        int numTagIndexFiles = listModel_.getSize();
        TagFile tf = null;
        for (int i = 0; i < numTagIndexFiles; i++)
        {
          tf = (TagFile) listModel_.get(i);
          if (tf.currentDirIndexFile_)
          {
            if (newCurBufStuff_)
              listModel_.removeElementAt(i);
            break;
          }
        }
      }
    }
  };

  /***************************************************************************/
  protected DocumentListener curBufFilenameListener_ = new DocumentListener() 
  {
    public void changedUpdate(DocumentEvent e) { update(e); }
    public void insertUpdate(DocumentEvent e) { update(e); }
    public void removeUpdate(DocumentEvent e) { update(e); }
    
    public void update(DocumentEvent e) 
    {
      String path = curBufFileNameField_.getText();
      path = path.trim();
      
      int numTagIndexFiles = listModel_.getSize();
      TagFile tf = null;
      for (int i = 0; i < numTagIndexFiles; i++)
      {
        tf = (TagFile) listModel_.get(i);
        if (tf.currentDirIndexFile_)
        {
          Log.log(Log.DEBUG, this, tf.path_ + " > " + path);
          if (newCurBufStuff_)
          {
            TagFile newTagFile = new TagFile(path, TagFile.DEFAULT_CATAGORY);
            newTagFile.currentDirIndexFile_ = true;

            listModel_.removeElementAt(i);
            listModel_.add(i, newTagFile);
            
            newTagFile = null;
          }
        }
      }
      
      path = null;
      tf = null;
    }
  };
  
  
  /***************************************************************************/
  protected void updateGUI() {
    int selectedIndices[] = list_.getSelectedIndices();
    int selectionCount = (selectedIndices != null) ? selectedIndices.length : 0;
    int numItems = listModel_.size();
    removeButton_.setEnabled(selectionCount != 0 && numItems != 0);
    moveUpButton_.setEnabled(selectionCount == 1 && selectedIndices[0] != 0 && 
                             numItems != 0);
    moveDownButton_.setEnabled(selectionCount == 1 && 
                            selectedIndices[0] != (listModel_.getSize() - 1) &&
                            numItems != 0);
                     
    boolean selected = useCurrentBufTagFileCheckBox_.isSelected();
    curBufFileNameLabel_.setEnabled(selected);
    curBufFileNameField_.setEnabled(selected);
  }
}
