/*
 * jEdit editor settings:
 * :mode=java:tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * MultipleEntriesDialog.java
 * Copyright (C) 2000 Dirk Moebius
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
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;


/**
 * This is a chooser dialog, that is displayed, when there are multiple
 * entries for the current keyword. The user has to select one, or cancel
 * the dialog with ESC.
 */
public class MultipleEntriesDialog extends EnhancedDialog implements JIndexListener {

    private static final Dimension TOPIC_LIST_SIZE = new Dimension(300,150);
    private static final Insets vertSpaceTop = new Insets(5,5,0,5);
    private static final Insets vertSpaceNone = new Insets(0,5,0,5);
    private static final Insets vertSpaceBoth = new Insets(5,5,5,5);
    private static final int fixedCellHeight = new JLabel("Mg_").getPreferredSize().height;

    private JIndex index;
    private View view;
    private String lastSearchString;
    private HelpfulJList topicList;
    private JIndexDockable.TopicListModel tlmodel;
    private JLabel status;


    public MultipleEntriesDialog(View view, String searchString) {
        super(view, jEdit.getProperty("jindex.chooser.title", new Object[] {searchString}), false);

        this.view = view;

        // info label
        Font small = new Font("Dialog", Font.PLAIN, 10);
        JLabel info = new JLabel(jEdit.getProperty("jindex.chooser.info"));
        info.setFont(small);

        // list of topics found
        tlmodel = new JIndexDockable.TopicListModel();
        topicList = new HelpfulJList(tlmodel);
        topicList.setFixedCellHeight(fixedCellHeight);
        topicList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        topicList.setCellRenderer(new JIndexDockable.TopicListCellRenderer());
        topicList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2)
                    ok();
            }
        });

        // scrollpane for list of topics
        JScrollPane scrTopicList = new JScrollPane(topicList);
        scrTopicList.setPreferredSize(TOPIC_LIST_SIZE);

        // status label
        status = new JLabel(jEdit.getProperty("jindex.frame.status.label"));
        status.setFont(small);
        status.setBorder(new BevelBorder(BevelBorder.LOWERED));

        // general layout
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrTopicList, BorderLayout.CENTER);
        getContentPane().add(info, BorderLayout.NORTH);
        getContentPane().add(status, BorderLayout.SOUTH);
        setSize(new Dimension(300,150));
        setLocationRelativeTo(view);
        GUIUtilities.loadGeometry(this, "jindex.chooser");

        // misc setup
        JIndexHolder.getInstance().addJIndexListener(this);
        index = JIndexHolder.getInstance().getIndex(); // note: the index may still be null

        // if index is not yet loaded, make window visible to see progress:
        if (index == null)
            setVisible(true);
    }


    public void indexChanged(JIndexChangeEvent e) {
        JIndex newIndex = e.getIndex();
        int newStatus = e.getStatus();

        if (newStatus == JIndexHolder.STATUS_OK && newIndex != null) {
            // set new index:
            index = newIndex;
            setBusy(false);
            setStatusText(jEdit.getProperty("jindex.frame.status.ready"));
            tlmodel.setEntries(null);
            if (lastSearchString != null)
                search(lastSearchString);
        } else if (newStatus == JIndexHolder.STATUS_LOADING) {
            setBusy(true);
            setStatusText(jEdit.getProperty("jindex.frame.status.loading"));
        } else {
            setBusy(false);
            if (newStatus != JIndexHolder.STATUS_NOT_EXISTS && newStatus != JIndexHolder.STATUS_OK)
                setStatusText(jEdit.getProperty("jindex.frame.status.error"));
        }
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


    public void ok() {
        showDocOnSelectedEntry();
        setVisible(false);
        JIndexHolder.getInstance().removeJIndexListener(this);
        index = null;
        dispose();
    }


    public void cancel() {
        setVisible(false);
        JIndexHolder.getInstance().removeJIndexListener(this);
        index = null;
        dispose();
    }


    public void setVisible(boolean vis) {
        super.setVisible(vis);
        if (!vis)
            GUIUtilities.saveGeometry(this, "jindex.chooser");
    }


    public void search(final String searchstring) {
        if (index == null) {
            setVisible(true);
            setStatusText(jEdit.getProperty("jindex.frame.status.notloaded"));
            lastSearchString = searchstring;
            return;
        }

        if (searchstring == null)
            return; // nothing to search for

        // set dialog title
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setTitle(jEdit.getProperty("jindex.chooser.title", new Object[] {searchstring}));
            }
        });

        // search in index for searchstring
        JIndex.SearchResult res = index.search(searchstring, true);

        if (res.entries == null) {
            // nothing found
            setStatusText(jEdit.getProperty("jindex.frame.status.keywordnotfound",
                new Object[] { res.searchstring }
            ));
            Toolkit.getDefaultToolkit().beep();
        } else {
            setSearchResult(res.entries, index.getKeywordAt(res.keywordpos));
            if (res.entries.length == 1 && "true".equals(jEdit.getProperty("jindex.fastDisplay"))) {
                // one entry found for this searchstring, and the user
                // wants fast display of help contents.
                showDocOnEntry(res.entries[0]);
                setVisible(false);
            } else {
                setVisible(true);
                topicList.requestFocus();
            }
        }

        lastSearchString = null;
    }


    private void setSearchResult(IndexEntry[] entries, String keyword) {
        tlmodel.setEntries(entries);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                topicList.setSelectedIndex(0);
            }
        });

        setStatusText(jEdit.getProperty("jindex.frame.status.countEntries",
            new Object[] {
                new Integer(entries == null ? 0 : entries.length),
                keyword
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

}

