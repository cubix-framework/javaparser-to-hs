/*
Copyright (c) 2007, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ise.plugin.svn;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import ise.plugin.svn.gui.StopPanel;
import ise.plugin.svn.library.*;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;

/**
 * Borrowed from Antelope:
 *
 * A simple log handler for SVNPlugin that shows the SVN output in a GUI.
 * This provides the contents of the "SVN Console" tab.
 * Level.INFO is logged in blue text.<br>
 * Level.WARNING is logged in green text.<br>
 * Level.SEVERE is logged in red text.<br>
 * All other levels are logged in black text.<br>
 *
 * @author    Dale Anson, danson@germane-software.com
 * @created   July 22, 2002
 */
public class SubversionGUILogHandler extends Handler implements Serializable {

    /**
     * The output area.
     */
    private JTextPane textPane;

    private boolean doTail = true;

    private JPanel contentPanel;

    private StopPanel stopPanel = null;

    private LinkedList<StyledMessage> messageQueue = new LinkedList<StyledMessage>();

    /**
     * Green
     */
    private Color GREEN = new Color(0, 153, 51);
    private Color foreground = Color.BLACK;

    private SimpleAttributeSet attributeSet = null;

    /**
     * Platform line separator.
     */
    public static final String LS = System.getProperty("line.separator");

    /**
     * Constructor
     */
    public SubversionGUILogHandler() {
        contentPanel = new JPanel(new BorderLayout());

        // this value lets the CloseableTabbedPane know not to allow closing
        // of the SVN Console.
        contentPanel.putClientProperty("isCloseable", Boolean.FALSE);

        textPane = new JTextPane();
        textPane.setName("svn console output");

        try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            if (laf.getID().equals("Nimbus")) {
                // stupid hack for Nimbus look and feel where JTextPane and
                // JEditorPane don't honor setBackground.
                textPane.setUI(new javax.swing.plaf.basic.BasicEditorPaneUI());
            }
            textPane.setBackground(jEdit.getColorProperty("view.bgColor"));
            foreground = jEdit.getColorProperty("view.fgColor");
            textPane.setForeground(foreground);
        } catch (Exception e) {
            textPane.setBackground(Color.WHITE);
            textPane.setForeground(Color.BLACK);
            foreground = Color.BLACK;
        }

        // use the same font as the jEdit text area
        Font currentFont = jEdit.getFirstView().getEditPane().getTextArea().getPainter().getFont();
        textPane.setFont(currentFont);

        textPane.setCaretPosition(0);

        attributeSet = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attributeSet, currentFont.getFamily());
        StyleConstants.setBold(attributeSet, currentFont.isBold());
        StyleConstants.setItalic(attributeSet, currentFont.isItalic());
        StyleConstants.setFontSize(attributeSet, currentFont.getSize());

        // layout the panel
        contentPanel.add(new JScrollPane(textPane), BorderLayout.CENTER);
        JPanel bottom_panel = new JPanel(new BorderLayout());
        bottom_panel.add(getControlPanel(), BorderLayout.EAST);
        bottom_panel.add(getStopPanel(), BorderLayout.WEST);
        contentPanel.add(bottom_panel, BorderLayout.SOUTH);

        // set the formatter
        setFormatter(new LogFormatter());

        messageProcessor.start();
    }

    public class LogFormatter extends Formatter {
        /**
         * @param record
         * @return Description of the Returned Value
         */
        public String format(LogRecord record) {
            return record.getMessage() + LS;
        }
    }

    /**
     * @return   The textArea value
     */
    public JTextComponent getTextComponent() {
        return textPane;
    }

    public Document getDocument() {
        return textPane.getDocument();
    }

    public JPanel getPanel() {
        return contentPanel;
    }

    /**
     * Finish out the log.
     */
    public void close() {
        if (getFormatter() != null) {
            publish(new LogRecord(Level.INFO, getFormatter().getTail(this)));
        }
    }

    /**
     * Flush the log.
     */
    public void flush() {
        if (getFormatter() != null) {
            publish(new LogRecord(Level.INFO, getFormatter().getTail(this)));
        }
    }

    /**
     * Starts the log.
     */
    public void open() {
        if (getFormatter() != null) {
            int index = textPane.getDocument().getLength();
            try {
                textPane.getDocument().insertString(index, getFormatter().getHead(SubversionGUILogHandler.this), null);
            } catch (Exception e) {                // NOPMD
                // Log.log( e );
            }
        }
    }

    /**
     * Appends the given record to the GUI.
     *
     * @param lr  the LogRecord to write.
     */
    public void publish(final LogRecord lr) {
        if (lr == null) {
            // nothing to do
            return;
        }
        if (textPane == null) {
            // nowhere to display the message
            return;
        }
        String msg = lr.getMessage();
        if (msg == null) {
            // no message to display
            return;
        }

        if (getFormatter() != null) {
            msg = getFormatter().format(lr);
        }

        if (lr.getLevel().equals(Level.WARNING)) {
            StyleConstants.setForeground(attributeSet, GREEN);
        } else if (lr.getLevel().equals(Level.SEVERE)) {
            StyleConstants.setForeground(attributeSet, Color.RED);
        } else if (lr.getLevel().equals(Level.INFO)) {
            StyleConstants.setForeground(attributeSet, foreground);
        } else {
            StyleConstants.setForeground(attributeSet, foreground);
        }
        queueMessage(new StyledMessage(msg, attributeSet));
    }

    private void queueMessage(StyledMessage sm) {
        messageQueue.addLast(sm);
    }

    private void processMessage(final StyledMessage sm) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    int index = textPane.getDocument().getLength();
                    int caret_position = textPane.getCaretPosition();
                    textPane.getDocument().insertString(index, sm.message, sm.attributes);
                    if (doTail) {
                        textPane.setCaretPosition(index + sm.message.length());
                    } else {
                        textPane.setCaretPosition(caret_position);
                    }

                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        } );
    }

    public StopPanel getStopPanel() {
        if (stopPanel == null) {
            stopPanel = new StopPanel();
        }
        return stopPanel;
    }

    private JPanel getControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JCheckBox tail_cb = new JCheckBox("Tail");
        tail_cb.setSelected(true);
        tail_cb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                doTail = tail_cb.isSelected();
                if (doTail) {
                    textPane.setCaretPosition(textPane.getDocument().getLength());
                }
            }
        }
       );
        RolloverButton clear_btn = new RolloverButton(GUIUtilities.loadIcon("Clear.png"));
        clear_btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                textPane.selectAll();
                textPane.replaceSelection("");
            }
        }
       );
        panel.add(tail_cb);
        panel.add(clear_btn);
        return panel;
    }

    public class StyledMessage {
        public String message;
        public SimpleAttributeSet attributes;

        public StyledMessage(String msg, SimpleAttributeSet set) {
            message = msg;
            attributes = set;
        }
    }

    Thread messageProcessor = new Thread() {
        public void run() {
            setPriority(Thread.MIN_PRIORITY);
            while (true) {
                while (!messageQueue.isEmpty()) {
                    try {
                        processMessage(messageQueue.removeFirst());
                    } catch (Exception e) {     // NOPMD
                        // ignored
                    }
                    yield();
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // ignored
                }
            }
        }
    };
}