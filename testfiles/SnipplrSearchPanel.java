/*
SnipplrSearchPanel.java
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
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;


import org.apache.xmlrpc.XmlRpcException;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
//}}}

/**
 * The dockable search panel for the Snipplr plugin. It will allow users to
 * search their code snippets on Snipplr. It displays the search results
 * and allows you to paste the snippets to the open buffer.
 *
 * You can only search snippets you own or are in your favorites because
 * of limitations in the Snipplr API.
 *
 * @
 */
public class SnipplrSearchPanel extends JPanel implements EBComponent, ClipboardOwner {
    
    //{{{ SnipplrSearchPanel constructor
    
    public SnipplrSearchPanel() {
        
        setLayout(new BorderLayout());
        
        JPanel topPanel = new JPanel();
        GridBagLayout gridBag = new GridBagLayout();
        topPanel.setLayout(gridBag);
        
        int y = 0;
        
        JLabel label = new JLabel(jEdit.getProperty("snipplr-search.tags.label"));
        
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = 1;
        cons.weightx = 0.0f;
        cons.insets = new Insets(1,0,1,0);
        cons.fill = GridBagConstraints.BOTH;

        gridBag.setConstraints(label,cons);
        topPanel.add(label);

        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.gridx = 1;
        cons.weightx = 1.0f;
        gridBag.setConstraints(m_tagsField,cons);
        topPanel.add(m_tagsField);
        
        m_tagsField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    SnipplrService.languagesList();
                } catch (Exception e2) {}
                search();
            }
        });
        
        cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = cons.REMAINDER;
        cons.fill = GridBagConstraints.NONE;
        cons.anchor = GridBagConstraints.EAST;
        cons.weightx = 1.0f;
        cons.insets = new Insets(1,0,1,0);

        gridBag.setConstraints(m_searchButton,cons);
        topPanel.add(m_searchButton);
        
        m_searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                search();
            }
        });
        
        m_results.addMouseListener(new ResultsPopup());
        
       // cons = new GridBagConstraints();
       // cons.gridy = y++;
       // cons.gridheight = 1;
       // cons.gridwidth = cons.REMAINDER;
       // cons.fill = GridBagConstraints.BOTH;
       // cons.anchor = GridBagConstraints.CENTER;
       // cons.weightx = 1.0f;
       // cons.insets = new Insets(1,0,1,0);

       // gridBag.setConstraints(m_results, cons);
        add(BorderLayout.NORTH, topPanel);
        add(BorderLayout.CENTER, new JScrollPane(m_results));
        
    }//}}}
    
    //{{{ addNotify() method
    public void addNotify() {
        super.addNotify();
        EditBus.addToBus(this);
    } //}}}

    //{{{ removeNotify() method
    public void removeNotify() {
        super.removeNotify();
        EditBus.removeFromBus(this);
    } //}}}
    
    //{{{ handleMessage()
    
    public void handleMessage(EBMessage message) {
        if (message instanceof SnipplrListUpdate) {
            SnipplrListUpdate update = (SnipplrListUpdate)message;
            if (update.isError()) {
                GUIUtilities.error(null,"snipplr.request.error", new String[] { update.getError().getMessage() });
                m_results.setModel(new SnippetListModel(new ArrayList<Snippet>()));
            } else {
                //update the result list.
                List<Snippet> snippets = update.getResult();
                if (snippets != null && snippets.size() > 0) {
                    m_results.setModel(new SnippetListModel(snippets));
                    m_results.setEnabled(true);
                } else {
                    m_results.setModel(new MessageListModel(jEdit.getProperty("snipplr-search.no.results.msg")));
                }
            }
        }
    }//}}}
    
    //{{{ ClipboardOwner methods
    
    //{{{ lostOwnership()
    
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // Nothing right now.
    }//}}}
    
    //}}}
    
    //{{{ Private members
    
    //{{{ ResultsPopup
    
    private class ResultsPopup extends MouseAdapter {
        
        //{{{ mousePressed()
        
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}
        
        //{{{ mouseReleased()
        
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }//}}}
        
        //{{{ maybeShowPopup()
        
        private void maybeShowPopup(MouseEvent e) {
            final int index = m_results.locationToIndex(new Point(e.getX(), e.getY()));
            if (e.isPopupTrigger() && m_results.isEnabled() && index != -1 && m_results.getCellBounds(index, index).contains(e.getX(), e.getY())) {
                m_results.setSelectedIndex(index);
                
                JPopupMenu popup = new JPopupMenu();
                JMenuItem popupMenuItem = new JMenuItem(jEdit.getProperty("snipplr-snippet.copy-source.label"));
                popupMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            Snippet snippet = (Snippet)m_results.getModel().getElementAt(index);
                            snippet.load();
                            
                            Clipboard clipBoard = getToolkit().getSystemClipboard();
                            clipBoard.setContents(new StringSelection(snippet.getSource()), SnipplrSearchPanel.this);
                        } catch (XmlRpcException e) {
                            GUIUtilities.error(null,"snipplr.request.error", new String[] { e.getMessage() });
                        }
                    }
                });
                popup.add(popupMenuItem);
                
                popupMenuItem = new JMenuItem(jEdit.getProperty("snipplr-snippet.buffer-insert.label"));
                popupMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            Snippet snippet = (Snippet)m_results.getModel().getElementAt(index);
                            snippet.load();
                            
                            Buffer buf = jEdit.getActiveView().getBuffer();
                            JEditTextArea textarea = jEdit.getActiveView().getTextArea();
                            
                            buf.insert(textarea.getCaretPosition(), snippet.getSource());
                            
                        } catch (XmlRpcException e) {
                            GUIUtilities.error(null,"snipplr.request.error", new String[] { e.getMessage() });
                        }
                    }
                });
                popup.add(popupMenuItem);
                
                popupMenuItem = new JMenuItem(jEdit.getProperty("snipplr-snippet.edit-snippet.label"));
                popupMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        try {
                            Snippet snippet = (Snippet)m_results.getModel().getElementAt(index);
                            snippet.load();
                            
                            new SnippetForm(jEdit.getActiveView(), snippet);
                        } catch (XmlRpcException e) {
                            GUIUtilities.error(null,"snipplr.request.error", new String[] { e.getMessage() });
                        }
                    }
                });
                popup.add(popupMenuItem);
                
                popupMenuItem = new JMenuItem(jEdit.getProperty("snipplr-snippet.delete-snippet.label"));
                popupMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        if (GUIUtilities.confirm(null,
                                             "snipplr-snippet.delete.confirm",
                                             new String[] {},
                                             JOptionPane.YES_NO_OPTION,
                                             JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
                        {
                            try {
                                Snippet snippet = (Snippet)m_results.getModel().getElementAt(index);
                                if (SnipplrService.snippetDelete(snippet)) {
                                    //Remove current element
                                    ((SnippetListModel)m_results.getModel()).removeElementAt(index);
                                } else {
                                    GUIUtilities.error(null,"snipplr.request.error", new String[] { "Could not delete the snippet." });
                                }
                            } catch (XmlRpcException e) {
                                GUIUtilities.error(null,"snipplr.request.error", new String[] { e.getMessage() });
                            }
                        }
                    }
                });
                popup.add(popupMenuItem);
                
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }//}}}
        
    }//}}}
    
    //{{{ MessageListModel
    private class MessageListModel implements ListModel {
        
        public MessageListModel(String message) {
            m_message = message;
        }
        
        public void addListDataListener(ListDataListener l) {
            //not supported
        }
        
        public void removeListDataListener(ListDataListener l) {
            //not supported
        }
        
        public int getSize() {
            return 1;
        }
        
        public Object getElementAt(int index) {
            if (index == 0) {
                return m_message;
            } else {
                return null;
            }
        }
        
        private String m_message;
    }//}}}
    
    //{{{ SnippetListModel
    
    private class SnippetListModel implements ListModel {
        
        private List<Snippet> m_list;
        private ArrayList<ListDataListener> m_listeners;
        
        public SnippetListModel(List<Snippet> list) {
            m_list = list;
            m_listeners = new ArrayList<ListDataListener>();
        }
        
        public void addListDataListener(ListDataListener l) {
            Log.log(Log.DEBUG,this,"addListDataListener");
            if (l != null) {
                m_listeners.add(l);
            }
        }
        
        public void removeListDataListener(ListDataListener l) {
            if (l != null) {
                m_listeners.remove(l);
            }
        }
        
        public int getSize() {
            return m_list.size();
        }
        
        public Object getElementAt(int index) {
            return m_list.get(index);
        }
        
        public Object removeElementAt(int index) {
            Object element = m_list.remove(index);
            Iterator<ListDataListener> itr = m_listeners.iterator();
            while (itr.hasNext()) {
                itr.next().intervalRemoved(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED,index,index));
            }
            return element;
        }
    }//}}}
    
    //{{{ search()
    private void search() {
        m_results.setModel(new MessageListModel(jEdit.getProperty("snipplr-search.loading.msg")));
        m_results.setEnabled(false);
        SnipplrService.snippetList(m_tagsField.getText(), "", 1);
    }//}}}
    
    private JTextField m_tagsField = new JTextField(20); 
    private JButton m_searchButton = new JButton(jEdit.getProperty("snipplr-search.search.label"));
    private JList m_results = new JList();
    
    //}}}
}
