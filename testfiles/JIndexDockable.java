/*
 * jEdit editor settings:
 * :mode=java:tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * JIndexHoldable.java
 * Copyright (C) 1999 2000 Dirk Moebius
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

package jindex;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Vector;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
// deprecated -- commented bty maeste -- import org.gjt.sp.jedit.gui.DockableWindow;
import org.gjt.sp.util.Log;


/**
 * This is the component, that displays the alphabetically sorted list of
 * index keywords, the search field and the "found entries" list.
 */
// deprecated -- commented bty maeste -- public class JIndexDockable extends JPanel implements DockableWindow, JIndexListener {
public class JIndexDockable extends JPanel implements  JIndexListener {

    public static final String DOCKABLE_NAME = "plugin.JIndexPlugin";

    private static final Dimension KEY_LIST_SIZE = new Dimension(300,400);
    private static final Dimension TOPIC_LIST_SIZE = new Dimension(300,150);
    private static final Insets vertSpaceTop = new Insets(5,5,0,5);
    private static final Insets vertSpaceNone = new Insets(0,5,0,5);
    private static final Insets vertSpaceBoth = new Insets(5,5,5,5);
    private static final int fixedCellHeight = new JLabel("Mg_").getPreferredSize().height;

    final static ImageIcon classIcon = new ImageIcon(JIndexDockable.class.getResource("images/class.gif"));
    final static ImageIcon interIcon = new ImageIcon(JIndexDockable.class.getResource("images/interf.gif"));
    final static ImageIcon constrIcon = new ImageIcon(JIndexDockable.class.getResource("images/constr.gif"));
    final static ImageIcon methodIcon = new ImageIcon(JIndexDockable.class.getResource("images/method.gif"));
    final static ImageIcon fieldIcon = new ImageIcon(JIndexDockable.class.getResource("images/field.gif"));

    private JIndex index;
    private View view;
    private JTextField searchField;
    private HelpfulJList keyList;
    private HelpfulJList topicList;
    private TopicListModel tlmodel;
    private JLabel status;
    private JSplitPane splitpane;


    public JIndexDockable(View view) {
        super(new BorderLayout());
        this.view = view;
        Log.log(Log.DEBUG,this,"View: "+view);
        
        // search field
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { search(); }
            public void removeUpdate(DocumentEvent e) { search(); }
            public void changedUpdate(DocumentEvent e) { search(); }
            private void search() {
                if (index == null) {
                    setStatusText(prop("status.notloaded"));
                    return;
                }
                String searchstring = searchField.getText().trim();
                setSearchResult(index.search(searchstring, false));
            }
        });

        // label for search field
        JLabel l1 = new JLabel(prop("search.label"));
        l1.setDisplayedMnemonic(prop("search.mnemonic").charAt(0));
        l1.setLabelFor(searchField);

        // list of keywords
        keyList = new HelpfulJList(new KeyListModel());
        keyList.setPrototypeCellValue("W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_W_");
        keyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (index == null) return;
                int keyPos = keyList.getSelectedIndex();
                if (keyPos < 0 || keyPos >= index.getNumKeywords()) return;
                updateTopicList(index.getEntriesAt(keyPos), keyPos);
            }
        });

        keyList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2)
                    if (topicList.getModel().getSize() == 1)
                        showDocOnSelectedEntry();
                    else
                        topicList.requestFocus();
            }
        });

        // label for list of keywords
        JLabel l2 = new JLabel(prop("index.label"));
        l2.setDisplayedMnemonic(prop("index.mnemonic").charAt(0));
        l2.setLabelFor(keyList);

        // scrollpane for list of keywords
        JScrollPane scrKeyList = new JScrollPane(keyList);
        scrKeyList.setPreferredSize(KEY_LIST_SIZE);
        scrKeyList.setColumnHeaderView(l2);

        // list of topics found
        tlmodel = new TopicListModel();

        topicList = new HelpfulJList(tlmodel);
        topicList.setFixedCellHeight(fixedCellHeight);
        topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topicList.setCellRenderer(new TopicListCellRenderer());
        topicList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2)
                    showDocOnSelectedEntry();
            }
        });

        // label for list of topics
        JLabel l3 = new JLabel(prop("topics.label"));
        l3.setDisplayedMnemonic(prop("topics.mnemonic").charAt(0));
        l3.setLabelFor(topicList);

        // scrollpane for list of topics
        JScrollPane scrTopicList = new JScrollPane(topicList);
        scrTopicList.setPreferredSize(TOPIC_LIST_SIZE);
        scrTopicList.setColumnHeaderView(l3);

        // splitpane with keylist and topiclist
        splitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrKeyList, scrTopicList);
        splitpane.setOneTouchExpandable(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String divLoc = jEdit.getProperty("jindex.dividerLocation");
                if (divLoc != null)
                    splitpane.setDividerLocation(Integer.parseInt(divLoc));
            }
        });

        // status label
        status = new JLabel(prop("status.label"));
        status.setBorder(new BevelBorder(BevelBorder.LOWERED));
        status.setFont(new Font("Dialog", Font.PLAIN, 10));

        // search field with label
        Box top = Box.createHorizontalBox();
        top.add(l1);
        top.add(searchField);

        // general layout
        this.add(top, BorderLayout.NORTH);
        this.add(splitpane, BorderLayout.CENTER);
        this.add(status, BorderLayout.SOUTH);

        // set default size
        setSize(new Dimension(220,500));  // faster than pack()

        // we like to be notified about JIndex and status changes:
        JIndexHolder.getInstance().addJIndexListener(this);

        // initialize both lists:
        index = JIndexHolder.getInstance().getIndex(); // note: the index may still be null
        Log.log(Log.DEBUG,this,"Calling init");
        init();
        Log.log(Log.DEBUG,this,"Init called");
    }


    public void indexChanged(JIndexChangeEvent e) {
        JIndex newIndex = e.getIndex();
        int newStatus = e.getStatus();
        Log.log(Log.DEBUG, this, "got index change, newStatus=" + newStatus);

        if (newStatus == JIndexHolder.STATUS_OK && newIndex != null) {
            index = newIndex;
            init();
            setBusy(false);
            setStatusText(prop("status.ready"));
        } else if (newStatus == JIndexHolder.STATUS_LOADING) {
            setBusy(true);
            setStatusText(prop("status.loading"));
        } else {
            setBusy(false);
            if (newStatus != JIndexHolder.STATUS_NOT_EXISTS && newStatus != JIndexHolder.STATUS_OK)
                setStatusText(prop("status.error"));
        }
    }


    public String getName() {
        return DOCKABLE_NAME;
    }


    public Component getComponent() {
        return this;
    }


    /**
     * Invoked when the component is removed; saves the position of the
     * split pane divider
     */
    public void removeNotify() {
        super.removeNotify();
        JIndexHolder.getInstance().removeJIndexListener(this);
        jEdit.setProperty("jindex.dividerLocation", Integer.toString(splitpane.getDividerLocation()));
    }


    private void init() {
        keyList.setModel(new KeyListModel());
        tlmodel.setEntries(null);
    }


    private void setStatusText(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                status.setText(text);
            }
        });
    }


    private void setBusy(boolean busy) {
        int cursorType = busy ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR;
        setCursor(Cursor.getPredefinedCursor(cursorType));
    }


    private String prop(String key) {
        return jEdit.getProperty("jindex.frame." + key);
    }


    public void search(final String searchstring) {
        if (index == null)
            return; // there is no index, probably because of error

        if (searchstring == null)
            return; // nothing to search for

        // search in index for searchstring
        JIndex.SearchResult res = index.search(searchstring, true);

        if (res.entries == null) {
            // nothing found
            setStatusText(jEdit.getProperty("jindex.frame.status.keywordnotfound",
                new Object[] { res.searchstring }
            ));
            Toolkit.getDefaultToolkit().beep();
        } else {
            setSearchResult(res);
            if (res.entries.length == 1 && "true".equals(jEdit.getProperty("jindex.fastDisplay"))) {
                // one entry found for this searchstring, and the user
                // wants fast display of help contents.
                showDocOnEntry(res.entries[0]);
            }
        }
    }


    private void setSearchResult(final JIndex.SearchResult res) {
        final int keyPos = res.keywordpos >= 0 ? res.keywordpos : -res.keywordpos;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                keyList.setSelectedIndex(keyPos);
                keyList.ensureIndexIsVisible(keyPos);
                updateTopicList(res.entries, keyPos);
            }
        });
    }


    private void updateTopicList(IndexEntry[] entries, int keyPos) {
        tlmodel.setEntries(entries);
        topicList.setSelectedIndex(0);
        setStatusText(jEdit.getProperty("jindex.frame.status.countEntries",
            new Object[] {
                new Integer(entries == null ? 0 : entries.length),
                index.getKeywordAt(keyPos)
            }
        ));
    }


    private void showDocOnSelectedEntry() {
        IndexEntry e = (IndexEntry) topicList.getSelectedValue();
        if (e != null)
            showDocOnEntry(e);
    }


    private void showDocOnEntry(IndexEntry e) {
        setStatusText(jEdit.getProperty("jindex.frame.status.open", new Object[] { e.name } ));
        String url = e.getCompleteURL();
        Log.log(Log.DEBUG, this, "showDocOnEntry: " + url);
        openURL(url);
    }


    private void openURL(String url) {
        infoviewer.InfoViewerPlugin.openURL(view, url);
    }


    public static class TopicListModel extends AbstractListModel
    {
        public int getSize() { return entries == null ? 0 : entries.length; }

        public Object getElementAt(int i) {
            if (entries == null)
                return "";
            else
                return entries[i];
        }

        public void setEntries(IndexEntry[] entries) {
            this.entries = entries;
            fireContentsChanged(this, -1, -1);
        }

        private IndexEntry[] entries = null;
    }


    private class KeyListModel extends AbstractListModel
    {
        public int getSize() { return index == null ? 0 : index.getNumKeywords(); }
        public Object getElementAt(int i) { return index == null ? null : index.getKeywordAt(i); }
        public void fireContentsChanged() { fireContentsChanged(this, -1, -1); }
    }


    public static class TopicListCellRenderer extends JLabel implements ListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            IndexEntry e = (IndexEntry) value;
            this.setText(e.name);

            switch (e.type) {
                case IndexEntry.CLAZZ:           this.setIcon(classIcon); break;
                case IndexEntry.INTERFACE:       this.setIcon(interIcon); break;
                case IndexEntry.CONSTRUCTOR:     this.setIcon(constrIcon); break;
                case IndexEntry.METHOD: default: this.setIcon(methodIcon); break;
                case IndexEntry.FIELD:           this.setIcon(fieldIcon); break;
            }

            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }

            this.setFont(list.getFont());

             // Swing needs this, otherwise it doesn't paint
             // the background color:
            this.setOpaque(true);

            return this;
        }
    }

}
