/*
SnippetForm.java
:tabSize=4:indentSize=4:noTabs=true:
:folding=explicit:collapseFolds=1:

This file written by Ian Lewis (IanLewis@member.fsf.org)
Copyright (C) 2007 Ian Lewis

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
Optionally, you may find a copy of the GNU General Public License
from http://www.fsf.org/copyleft/gpl.txt
*/

package snipplr;

//{{{ Imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;

import org.apache.xmlrpc.XmlRpcException;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.util.Vector;
//}}}

public class SnippetForm extends EnhancedDialog {
    
    //{{{ SnippetForm constructor
    
    public SnippetForm(Frame parent, Snippet snippet) {
        super(parent, jEdit.getProperty("snipplr-snippet.edit-snippet.label"), true);
        
        m_snippet = snippet;
        
        //setLayout(new BorderLayout());
        
        JPanel content = new JPanel(new BorderLayout(12,12));
        content.setBorder(new EmptyBorder(12,12,12,12));
        setContentPane(content);
        
        JPanel panel = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        panel.setLayout(gridBag);
        
        int y = 0;
        
        //TODO: add a link at the top to allow you to open the browser to the
        //snippet
        
        // label = new JLabel(jEdit.getProperty("snipplr-snippet.url.label"));
        
        // cons = new GridBagConstraints();
        // cons.gridy = y++;
        // cons.gridheight = 1;
        // cons.gridwidth = 1;
        // cons.weightx = 0.0f;
        // cons.insets = new Insets(1,0,1,0);
        // cons.fill = GridBagConstraints.BOTH;
        
        // gridBag.setConstraints(label,cons);
        // panel.add(label);
        
        // m_url = new JTextField(snippet.getURL());
        // cons.fill = GridBagConstraints.HORIZONTAL;
        // cons.gridx = 1;
        // cons.weightx = 1.0f;
        // gridBag.setConstraints(m_url,cons);
        // panel.add(m_url);
        
        JLabel label = new JLabel(jEdit.getProperty("snipplr-snippet.title.label"));
        
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = 1;
        cons.weightx = 0.0f;
        cons.insets = new Insets(1,0,1,0);
        cons.fill = GridBagConstraints.BOTH;
        
        gridBag.setConstraints(label,cons);
        panel.add(label);
        
        m_title = new JTextField(snippet.getTitle());
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridx = 1;
        cons.weightx = 1.0f;
        gridBag.setConstraints(m_title,cons);
        panel.add(m_title);
        
        label = new JLabel(jEdit.getProperty("snipplr-snippet.tags.label"));
        
        cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = 1;
        cons.weightx = 0.0f;
        cons.insets = new Insets(1,0,1,0);
        cons.fill = GridBagConstraints.BOTH;
        
        gridBag.setConstraints(label,cons);
        panel.add(label);
        
        m_tags = new JTextField(snippet.getTagsString());
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridx = 1;
        cons.weightx = 1.0f;
        gridBag.setConstraints(m_tags,cons);
        panel.add(m_tags);
        
        label = new JLabel(jEdit.getProperty("snipplr-snippet.language.label"));
        
        cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = 1;
        cons.weightx = 0.0f;
        cons.insets = new Insets(1,0,1,0);
        cons.fill = GridBagConstraints.BOTH;
        
        gridBag.setConstraints(label,cons);
        panel.add(label);
        
        m_language = new JComboBox(new Vector<Language>(LanguageMapper.getLanguages().values()));
        if (snippet.getLanguage() == null) {
            m_language.setSelectedItem(LanguageMapper.languageSearch(jEdit.getActiveView().getBuffer().getMode().getName()).getHumanReadableName());
        } else {
            m_language.setSelectedItem(snippet.getLanguage());
        }
        cons.fill = GridBagConstraints.BOTH;
        cons.gridx = 1;
        cons.weightx = 1.0f;
        gridBag.setConstraints(m_language,cons);
        panel.add(m_language);
        
        // label = new JLabel(jEdit.getProperty("snipplr-snippet.comment.label"));
        
        // cons = new GridBagConstraints();
        // cons.gridy = y+=3;
        // cons.gridx = 0;
        // cons.gridheight = 3;
        // cons.gridwidth = 1;
        // cons.weightx = 0.0f;
        // cons.insets = new Insets(1,0,1,0);
        // cons.fill = GridBagConstraints.BOTH;
        
        // gridBag.setConstraints(label,cons);
        // panel.add(label);
        
        // m_comment = new JTextArea(snippet.getComment());
        // JScrollPane scrollPane = new JScrollPane(m_comment);
        // cons.fill = GridBagConstraints.BOTH;
        // cons.gridx = 1;
        // cons.weightx = 1.0f;
        // cons.weighty = 1.0f;
        // gridBag.setConstraints(scrollPane,cons);
        // panel.add(scrollPane);
        
        label = new JLabel(jEdit.getProperty("snipplr-snippet.source.label"));
        
        cons = new GridBagConstraints();
        cons.gridy = y+=3;
        cons.gridx = 0;
        cons.gridheight = 3;
        cons.gridwidth = 1;
        cons.weightx = 0.0f;
        cons.insets = new Insets(1,0,1,0);
        cons.fill = GridBagConstraints.BOTH;
        
        gridBag.setConstraints(label,cons);
        panel.add(label);
        
        m_source = new JTextArea(snippet.getSource());
        JScrollPane scrollPane = new JScrollPane(m_source);
        cons.fill = GridBagConstraints.BOTH;
        cons.gridx = 1;
        cons.weightx = 1.0f;
        cons.weighty = 2.0f;
        gridBag.setConstraints(scrollPane,cons);
        panel.add(scrollPane);
        
        getContentPane().add(BorderLayout.CENTER, panel);
        
        Box buttons = new Box(BoxLayout.X_AXIS);
        buttons.add(Box.createGlue());
        
        //If the snippet is new then we can enable the save button.
        if (snippet.getId() == null) {
            m_okButton = new JButton(jEdit.getProperty("snipplr.save"));
            m_okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    ok();
                }
            });
            buttons.add(m_okButton);
            
            buttons.add(Box.createHorizontalStrut(6));
        }
        
        m_cancelButton = new JButton(jEdit.getProperty("common.cancel"));
        m_cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancel();
            }
        });
        buttons.add(m_cancelButton);
        
        buttons.add(Box.createGlue());
        
        getContentPane().add(buttons, BorderLayout.SOUTH);
        
        //we have multi-line text areas so don't handle Enter.
        setEnterEnabled(false);
        
        //The snipplr service does not provide a mechanism to update snippets.
        if (snippet.getId() != null) {
            m_title.setEnabled(false);
            m_tags.setEnabled(false);
            m_language.setEnabled(false);
            // m_comment.setEnabled(false);
            m_source.setEnabled(false);
        }
        
        GUIUtilities.loadGeometry(this, parent, m_name);
        
        // updateSize();
        
        setVisible(true);
        
    }//}}}
    
    //{{{ ok()
    public void ok() {
        //TODO: Save the Snippet to the server.
        //Only save the snippet if it's new.
        //JOptionPane.showMessageDialog(this, "Save!");
        if (m_snippet.getId() == null) {
            try {
                //Save the snippet and post it.
                m_snippet.setTitle(m_title.getText());
                m_snippet.setTagsString(m_tags.getText());
                m_snippet.setLanguage((Language)m_language.getSelectedItem());
                m_snippet.setSource(m_source.getText());
                
                SnipplrService.snippetPost(m_snippet);
                dispose();
            } catch (XmlRpcException e) {
                Log.log(Log.ERROR, SnipplrService.class, e);
                GUIUtilities.error(this,"snipplr.request.error", new String[] { e.getMessage() });
            }
        }
    }//}}}
    
    //{{{ cancel()
    
    public void cancel() {
        dispose();
    }//}}}
    
    //{{{ dispose()
    
    public void dispose() {
        GUIUtilities.saveGeometry(this, m_name);
        super.dispose();
    }//}}}
    
    //{{{ Private members
    
    //{{{ updateSize() method
	private void updateSize() {
		Dimension currentSize = getSize();
		Dimension requestedSize = getPreferredSize();
		Dimension newSize = new Dimension(
			Math.max(currentSize.width,requestedSize.width),
			Math.max(currentSize.height,requestedSize.height)
		);
        boolean update = false;
		if (newSize.width < 300) {
			newSize.width = 300;
            update = true;
        }
		if (newSize.height < 200) {
			newSize.height = 200;
            update = true;
        }
		if (update) {
            setSize(newSize);
        }
		validate();
	} //}}}
    
    private static final String m_name = "edit.snippet";
    private JButton m_okButton;
    private JButton m_cancelButton;
    private Snippet m_snippet;
    private JTextField m_title;
    private JTextField m_tags;
    // private JTextField m_url;
    private JComboBox m_language;
    // private JTextArea m_comment;
    private JTextArea m_source;
    //}}}
    
}
