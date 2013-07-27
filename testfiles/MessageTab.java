/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.ui;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.editor.MiniTextEditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class MessageTab
    extends AbstractTab {
    private JCheckBox usePrevious;
    private MiniTextEditor textEditor;
    private StringListComboBox previous;
    private int preferredCharacterWidth;
    private boolean adjusting;

    /**
     *  Constructor for the CommitMessageTab object
     */
    public MessageTab(String tabToolTipText, Icon icon, Icon largeIcon,
                      final int preferredCharacterWidth) {
        super("Message", icon);
        setTabToolTipText(tabToolTipText);
        setTabLargeIcon(largeIcon);
        this.preferredCharacterWidth = preferredCharacterWidth;
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);
        setLayout(new BorderLayout());
        setTabMnemonic('m');
        textEditor = new MiniTextEditor(context, true, false, true,
                                        Constants.MINI_TEXT_EDITOR_WORD_WRAP, true,
                                        MiniTextEditor.PLAIN_EDITOR);
        textEditor.setPreferredSize(new Dimension(400, 400));
        textEditor.setOpaque(false);
        textEditor.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(0, 0, 4, 0),
            BorderFactory.createLineBorder(Color.black)));

        previous = new StringListComboBox(context,
                                          context.getHost().getProperty(
            Constants.COMMIT_MESSAGE_PREVIOUS,
            ""), false);   
        if(previous.getSelectedItem() != null) {
            textEditor.setText( (String) previous.getSelectedItem());
        }
        previous.setRenderer(new LargeStringListCellRenderer(80) {
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(getFont());

                return new Dimension(fm.stringWidth("W") *
                                     preferredCharacterWidth,
                                     super.getPreferredSize().height);
            }
        });
        previous.setEditable(false);

//        if (previous.getModel().getSize() > 0) {
//            previous.getModel().setSelectedItem(previous.getModel()
//                                                .getElementAt(0));
//
//        }
        previous.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(!adjusting)
                    textEditor.setText( (String) previous.getSelectedItem());
            }
        });

        usePrevious = new JCheckBox("Use previous ");
        usePrevious.setMnemonic('p');
        usePrevious.setOpaque(false);
        usePrevious.setSelected(context.getHost().getBooleanProperty(Constants.
            COMMIT_MESSAGE_USE_PREVIOUS,
            false));
        usePrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                checkUsePrevious();
            }
        });
        checkUsePrevious();

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 2.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, textEditor, gbc, GridBagConstraints.REMAINDER);
        gbc.weighty = 0.0;
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, usePrevious, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this, previous, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     */
    public void grabFocus() {
        textEditor.grabFocus();
    }

    private void checkUsePrevious() {
        if (usePrevious.isSelected() && (textEditor.getText().length() == 0) &&
            (previous.getModel().getSize() > 0)) {
            textEditor.setText(previous.getModel().getElementAt(0).toString());
        }
    }

    public boolean validateTab() {
        return true;
    }

    public void applyTab() {
        if (!previous.containsString(textEditor.getText())) {
            adjusting = true;
            previous.addString(textEditor.getText());
            adjusting = false;
        }
		getContext().getHost().setProperty(Constants.
										   COMMIT_MESSAGE_PREVIOUS,
										   previous.
										   getStringListPropertyString());
        getContext().getHost().setBooleanProperty(Constants.
                                                  COMMIT_MESSAGE_USE_PREVIOUS,
                                                  usePrevious.isSelected());
		textEditor.cleanUp();
    }
    
    public void setText(String text) {
      textEditor.setText(text);
    }

    public String getText() {
        return textEditor.getText();
    }

    public boolean isUsePrevious() {
        return usePrevious.isSelected();
    }

    public void tabSelected() {
    }
}
