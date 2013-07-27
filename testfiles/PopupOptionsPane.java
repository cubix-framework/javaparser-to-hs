/*
 * OpenIt jEdit Plugin (PopupOptionsPane.java) 
 *  
 * Copyright (C) 2003 Matt Etheridge (matt@etheridge.org)
 * Copyright (C) 2006 Denis Koryavov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
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

package org.etheridge.openit.options;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.etheridge.openit.*;
import org.gjt.sp.jedit.*;

/**
 * OptionPane for the popup window options.
 */
public class PopupOptionsPane extends AbstractOptionPane {
        private JCheckBox mClearWindowCheckBox;
        private JCheckBox mCaseSensitiveCheckBox;
        
        private JCheckBox mDisplayDirectoriesCheckBox;
        private JCheckBox mDisplayExtensionsCheckBox;
        private JCheckBox mDisplayIconsCheckBox;
        private JCheckBox mDisplaySizeCheckBox;
        
        private JCheckBox mPathsInJavaStyleCheckBox;
        private JCheckBox mJavaFileDisplayDirectoriesCheckBox;
        
        public PopupOptionsPane() {
                super("OpenIt.PopupOptionsPane");
        }
        
        //{{{ _init method.
        public void _init() {
                setLayout(new BorderLayout());
                
                // top panel
                JPanel topPlacerPanel = new JPanel(new BorderLayout());
                JPanel topGridPlacerPanel = new JPanel(new GridLayout(2,1));
                topGridPlacerPanel.add(createClearWindowPanel());
                topGridPlacerPanel.add(createCaseSensitivePanel());
                topPlacerPanel.add(topGridPlacerPanel);
                
                // bottom panel
                JPanel placerPanel = new JPanel(new BorderLayout());
                placerPanel.add(topPlacerPanel, BorderLayout.NORTH);
                placerPanel.add(createDisplayOptionsPanel(), BorderLayout.CENTER);
                add(placerPanel, BorderLayout.NORTH);
        } 
        //}}}
        
        //{{{ _save method.
        public void _save() {
                setProperties();
                SourcePathManager.getInstance().refreshSourcePath();
        } 
        //}}}
        
        //{{{ createClearWindowPanel method.
        private JPanel createClearWindowPanel() {
                JPanel clearWindowPanel = new JPanel(new BorderLayout(6,6));
                clearWindowPanel.setBorder(BorderFactory.createCompoundBorder
                        (BorderFactory.createTitledBorder(jEdit.getProperty("options.OpenIt.PopupOptionsPane.ClearPopup.title")), 
                                BorderFactory.createEmptyBorder(6,6,6,6)));
                
                mClearWindowCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.ClearPopup.ShouldClearPopup"));
                mClearWindowCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.POP_UP_CLEAN_ON_VISIBLE, true)); // default to true
                clearWindowPanel.add(mClearWindowCheckBox);
                
                return clearWindowPanel;
        } 
        //}}}
        
        //{{{ createCaseSensitivePanel method.
        private JPanel createCaseSensitivePanel() {
                JPanel caseSensitivePanel = new JPanel(new BorderLayout(6,6));
                caseSensitivePanel.setBorder(BorderFactory.createCompoundBorder
                        (BorderFactory.createTitledBorder(jEdit.getProperty("options.OpenIt.PopupOptionsPane.CaseSensitive.title")), 
                                BorderFactory.createEmptyBorder(6,6,6,6)));
                
                mCaseSensitiveCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.CaseSensitive.label"));
                mCaseSensitiveCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.POP_UP_CASE_SENSITIVE_FILE_MATCHING, false)); // default to true
                caseSensitivePanel.add(mCaseSensitiveCheckBox);
                
                return caseSensitivePanel;
        } 
        //}}}
        
        //{{{ createDisplayOptionsPanel method.
        private JPanel createDisplayOptionsPanel() {
                JPanel displayOptionsPanel = new JPanel(new GridLayout(4,1));
                displayOptionsPanel.setBorder(BorderFactory.createCompoundBorder
                        (BorderFactory.createTitledBorder(jEdit.getProperty("options.OpenIt.PopupOptionsPane.Display.title")), 
                                BorderFactory.createEmptyBorder(6,6,6,6)));
                
                mDisplayDirectoriesCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.Display.DisplayDirectory"));
                mDisplayDirectoriesCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.DISPLAY_DIRECTORIES, true));
                
                mPathsInJavaStyleCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.JavaFileDisplay.DisplayPackages"));
                mPathsInJavaStyleCheckBox.setEnabled(jEdit.getBooleanProperty(OpenItProperties.DISPLAY_DIRECTORIES, false));
                if (mPathsInJavaStyleCheckBox.isEnabled()){
                        mPathsInJavaStyleCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.PATHS_IN_JAVA_STYLE, false));
                }
                
                Box box = new Box(BoxLayout.X_AXIS);
                box.add(mDisplayDirectoriesCheckBox);
                mDisplayDirectoriesCheckBox.addItemListener(new ItemListener() {
                                public void itemStateChanged(ItemEvent e) {
                                        if (e.getStateChange() == ItemEvent.DESELECTED) {
                                                mPathsInJavaStyleCheckBox.setSelected(false);
                                        }
                                        mPathsInJavaStyleCheckBox.setEnabled(e.getStateChange() == ItemEvent.SELECTED); 
                                }
                });
                box.add(Box.createHorizontalStrut(10));
                box.add(mPathsInJavaStyleCheckBox);
                displayOptionsPanel.add(box);
                
                mDisplayExtensionsCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.Display.DisplayExtensions"));
                mDisplayExtensionsCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.DISPLAY_EXTENSIONS, true));
                displayOptionsPanel.add(mDisplayExtensionsCheckBox);
                
                mDisplayIconsCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.Display.DisplayIcons"));
                mDisplayIconsCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.DISPLAY_ICONS, true));
                displayOptionsPanel.add(mDisplayIconsCheckBox);
                
                mDisplaySizeCheckBox = new JCheckBox(jEdit.getProperty("options.OpenIt.PopupOptionsPane.Display.DisplaySize"));
                mDisplaySizeCheckBox.setSelected(jEdit.getBooleanProperty(OpenItProperties.DISPLAY_SIZE, false));
                displayOptionsPanel.add(mDisplaySizeCheckBox);
                
                return displayOptionsPanel;
        }
        //}}}
        
        //{{{ setProperties method.
        private void setProperties() {
                jEdit.setBooleanProperty(OpenItProperties.POP_UP_CLEAN_ON_VISIBLE, mClearWindowCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.POP_UP_CASE_SENSITIVE_FILE_MATCHING, mCaseSensitiveCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.DISPLAY_DIRECTORIES, mDisplayDirectoriesCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.PATHS_IN_JAVA_STYLE, mPathsInJavaStyleCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.DISPLAY_EXTENSIONS, mDisplayExtensionsCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.DISPLAY_ICONS, mDisplayIconsCheckBox.isSelected());
                jEdit.setBooleanProperty(OpenItProperties.DISPLAY_SIZE, mDisplaySizeCheckBox.isSelected());
        } 
        //}}}
        
        // :collapseFolds=1:tabSize=8:indentSize=8:folding=explicit:
}
