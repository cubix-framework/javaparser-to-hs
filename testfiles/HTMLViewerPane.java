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

package gruntspud.standalone;

import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractNextAction;
import gruntspud.actions.AbstractPreviousAction;
import gruntspud.actions.AbstractReloadAction;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.ToolBarSeparator;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class HTMLViewerPane
    extends JPanel
    implements HyperlinkListener {
    private JEditorPane htmlViewer;
    private GruntspudContext context;
    private Action forwardAction;
    private Action backAction;
    private Action reloadAction;
    private URL location;
    private History history;
    private StringListComboBox address;
    private boolean adjusting;

    /**
     * Creates a new HTMLViewerPane object.
     *
     * @param context DOCUMENT ME!
     * @param url DOCUMENT ME!
     */
    public HTMLViewerPane(GruntspudContext context, URL url) {
        super(new BorderLayout());

        history = new History();

        this.context = context;

        JToolBar toolbar = new JToolBar();
        toolbar.setBorderPainted(false);
        toolbar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolbar.setFloatable(false);
		boolean showSelectiveText = context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
        toolbar.add(UIUtil.createButton(backAction = new BackAction(), showSelectiveText,
                                        false));
        toolbar.add(UIUtil.createButton(forwardAction = new ForwardAction(),
		showSelectiveText, false));
        toolbar.add(UIUtil.createButton(reloadAction = new ReloadAction(),
		showSelectiveText, false));
        toolbar.add(new ToolBarSeparator());
        
        JPanel addressBar = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        UIUtil.jGridBagAdd(addressBar, new JLabel("Address:"), gbc, GridBagConstraints.RELATIVE);
        address = new StringListComboBox(context, "", false);
        address.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(!adjusting) {
	                try {
	                    setURL(new URL(String.valueOf(address.getSelectedItem())));                    
	                }
	                catch(MalformedURLException murle) {
	                    Constants.UI_LOG.error(murle);
	                }                
                }
            }
        });
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(addressBar, address, gbc, GridBagConstraints.REMAINDER);
        
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(toolbar, BorderLayout.WEST);
        topBar.add(addressBar, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout());
        top.add(topBar, BorderLayout.NORTH);
        top.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        htmlViewer = new JEditorPane();
        htmlViewer.addHyperlinkListener(this);
        htmlViewer.setEditable(false);
        htmlViewer.setForeground(Color.black);
        htmlViewer.setBackground(Color.white);
        htmlViewer.setSelectedTextColor(Color.white);
        htmlViewer.setSelectionColor(Color.blue.darker());

        JScrollPane scroller = new JScrollPane(htmlViewer);

        add(top, BorderLayout.NORTH);
        add(scroller, BorderLayout.CENTER);

        setURL(url);
    }

    private void setAvailableActions() {
        reloadAction.setEnabled(location != null);
        backAction.setEnabled(history.getIndex() > 0);
        forwardAction.setEnabled( (history.getIndex() + 1) < history.getSize());
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public URL getURL() {
        return location;
    }

    /**
     * DOCUMENT ME!
     *
     * @param hyperlinkevent DOCUMENT ME!
     */
    public void xhyperlinkUpdate(HyperlinkEvent hyperlinkevent) {
        Constants.UI_LOG.debug("Hyperlink " + hyperlinkevent);
        if (hyperlinkevent.getEventType().equals(javax.swing.event.
                                                 HyperlinkEvent.EventType.
                                                 ACTIVATED)) {
            URL url = hyperlinkevent.getURL();
            setURL(url);
        }
    }

    public void hyperlinkUpdate(HyperlinkEvent evt) {
        URL url = evt.getURL();
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (evt instanceof HTMLFrameHyperlinkEvent) {
                ( (HTMLDocument) htmlViewer.getDocument())
                    .processHTMLFrameHyperlinkEvent(
                    (HTMLFrameHyperlinkEvent) evt);
            }
            else {
                if (url != null)
                    setURL(url, true);
            }
        }
        else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            htmlViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
            htmlViewer.setCursor(Cursor.getDefaultCursor());
        }
    }


    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     * @param addToHistory DOCUMENT ME!
     * @param s DOCUMENT ME!
     */
    public synchronized void setURL(final URL url, final boolean addToHistory,
                       final String s) {
        adjusting = true;
        Constants.UI_LOG.debug("Setting URL " + url);
        address.addAndSelectString(url.toString());
        Gruntspud.getAuthenticator().setParentComponent(this);

        Runnable r = new Runnable() {
            public void run() {
                try {
                    try {
                        location = url;

                        if (location == null) {
                            htmlViewer.setContentType("text/html");
                            htmlViewer.setText(buildMessageHTML(
                                "No help available",
                                "No help resource was specified for this category"));
                        }
                        else {
                            htmlViewer.setPage(location +
                                               ( (s != null) ? ("#" + s) : ""));

                            if (addToHistory) {
                                history.addPage(location);
                            }
                        }
                    }
                    catch (Throwable t) {
                        t.printStackTrace();
                        htmlViewer.setContentType("text/html");
                        htmlViewer.setText(buildMessageHTML(
                            "Page could not be loaded", t.getMessage()));

                        return;
                    }

                    return;
                }
                finally {
                    setAvailableActions();
                }
            }
        };

        SwingUtilities.invokeLater(r);
        adjusting = false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     * @param flag DOCUMENT ME!
     */
    public void setURL(URL url, boolean flag) {
        setURL(url, flag, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     */
    public void setURL(URL url) {
        setURL(url, true, null);
    }

    /**
     * DOCUMENT ME!
     */
    public void back() {
        setURL(history.back(), false);
    }

    /**
     * DOCUMENT ME!
     */
    public void forward() {
        setURL(history.forward(), false);
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     * @param s1 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String buildMessageHTML(String s, String s1) {
        StringBuffer stringbuffer;
        (stringbuffer = new StringBuffer()).append("<HTML>");
        stringbuffer.append("<BODY>");
        stringbuffer.append("<CENTER>");
        stringbuffer.append("<H3>");
        stringbuffer.append(s);
        stringbuffer.append("</H3>");
        stringbuffer.append("</CENTER>");
        stringbuffer.append("<P>");
        stringbuffer.append(s1);
        stringbuffer.append("</P>");
        stringbuffer.append("</BODY>");
        stringbuffer.append("</HTML>");

        return stringbuffer.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param url DOCUMENT ME!
     * @param s DOCUMENT ME!
     */
    public void setURL(URL url, String s) {
        setURL(url, true, s);
    }

    /**
     * DOCUMENT ME!
     */
    public void reload() {
        setURL(getURL(), false);
    }

    class ForwardAction
        extends AbstractNextAction {
        public void actionPerformed(ActionEvent evt) {
            HTMLViewerPane.this.forward();
        }
    }

    class BackAction
        extends AbstractPreviousAction {
        public void actionPerformed(ActionEvent evt) {
            HTMLViewerPane.this.back();
        }
    }

    class ReloadAction
        extends AbstractReloadAction {
        public void actionPerformed(ActionEvent evt) {
            HTMLViewerPane.this.reload();
        }
    }

    private class History
        extends AbstractListModel {
        private int historyIndex = 0;
        private Vector history = null;

        public History() {
            history = new Vector();
            historyIndex = -1;
        }

        public void clearHistory() {
            history.removeAllElements();
            historyIndex = -1;
            fireContentsChanged(this, 0, 0);
        }

        public void addPage(URL url) {
            for (int i = history.size() - 1; i > historyIndex; i--) {
                history.removeElementAt(i);

            }
            historyIndex++;
            history.addElement(url);
            fireContentsChanged(this, 0, history.size() - 1);
        }

        public int getSize() {
            return history.size();
        }

        public Object getElementAt(int i) {
            return history.elementAt(i);
        }

        public int getIndex() {
            return historyIndex;
        }

        public URL back() {
            if (historyIndex > 0) {
                historyIndex--;

                return (URL) getElementAt(historyIndex);
            }
            else {
                throw new ArrayIndexOutOfBoundsException(
                    "At the start of the history.");
            }
        }

        public URL forward() {
            if (historyIndex < history.size()) {
                historyIndex++;

                return (URL) getElementAt(historyIndex);
            }
            else {
                throw new ArrayIndexOutOfBoundsException(
                    "At the end of the history.");
            }
        }
    }
}
