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

import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Tip of the day
 */
public class JTips
    extends JDialog
    implements ActionListener {
    //
    private final static int RAISED = 1;
    private final static int LOWERED = 2;

    //  Private instance variables
    private String filename;
    private JButton next;
    private JButton close;
    private JCheckBox show;
    private TipTextPane text;
    private int tipIndex;

    /**
     * Construct a new tip of the day dialog given a parent component,
     * location for tip file, and tip to start at
     *
     * @param parent parent compoent
     * @param tipFileLocation
     * @param tipIndex tip to start at
     * @throws IOException
     */
    public JTips(JFrame parent, int tipIndex, GruntspudContext context) throws
        IOException {
        super(parent, "Tip of the Day", true);

        //  Create the icon panel
        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setBackground(Color.gray);

        JLabel icon = new JLabel(UIUtil.getCachedIcon(
            JDK13GruntspudHost.ICON_TOOL_LARGE_TIPS));
        icon.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        icon.setVerticalAlignment(JLabel.CENTER);
        icon.setHorizontalAlignment(JLabel.CENTER);
        iconPanel.add(icon, BorderLayout.NORTH);

        //  Create the title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Did you know...");
        title.setBorder(new EmptyBorder(10, 10, 6, 0));
        title.setFont(new Font("Helvetica", Font.PLAIN, 18));
        titlePanel.setBorder(new EdgeBorder(EdgeBorder.SOUTH));

        //        titlePanel.setPreferredSize(new Dimension(46, 46));
        titlePanel.add(title, BorderLayout.CENTER);

        //  Create the tip text
        text = new TipTextPane();
        text.setBackground(getBackground());
        text.setForeground(getForeground());
        text.setContentType("text/html");

        HTMLEditorKit htmlKit = (HTMLEditorKit) (text.
                                                 getEditorKitForContentType(
            "text/html"));
        htmlKit.setStyleSheet(new GruntspudStyleSheet(context));

        HTMLDocument htmlDoc = (HTMLDocument) text.getDocument();
        text.setEditorKit(htmlKit);

        JScrollPane textScroller = new JScrollPane(text);
        textScroller.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        //  Create the center panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.add(titlePanel, BorderLayout.NORTH);
        centerPanel.add(textScroller, BorderLayout.CENTER);

        //  Tips panel
        JPanel tipsPanel = new JPanel(new BorderLayout());
        tipsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10),
            BorderFactory.createBevelBorder(BevelBorder.LOWERED)));
        tipsPanel.add(centerPanel, BorderLayout.CENTER);
        tipsPanel.add(iconPanel, BorderLayout.WEST);

        //  Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.add(next = new JButton("Next Tip"));
        buttonPanel.add(close = new JButton("Close"));

        //  Show panel
        JPanel showPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        showPanel.add(show = new JCheckBox("Show tips at startup", true));

        //  Nav. panel
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BorderLayout());
        navPanel.add("East", buttonPanel);
        navPanel.add("West", showPanel);

        //  Build this panel
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(navPanel, BorderLayout.SOUTH);
        getContentPane().add(tipsPanel, BorderLayout.CENTER);

        next.addActionListener(this);
        close.addActionListener(this);
        show.addActionListener(this);

        //  Move to the first tip
        setTipIndex(tipIndex);

        //  Set size and position
        setSize(new Dimension(425, 280));
        UIUtil.positionComponent(SwingConstants.CENTER, this);
    }

    /**
     * Next tip
     */
    private void nextTip() {
        setTipIndex(tipIndex + 1);
    }

    /**
     * Return the tip index
     *
     * @return current tip index
     */
    public int getTipIndex() {
        return tipIndex;
    }

    /**
     * Set the tip index
     *
     * @param tipIndex tip index to move to
     */
    public void setTipIndex(int tip) {
        tipIndex = tip;

        while (true) {
            InputStream in = null;

            try {
                String r = "resources/tips/tips" + tipIndex + ".html";
                in = getClass().getClassLoader().getResourceAsStream(r);

                if (in == null) {
                    throw new IOException("Could not find tip " + tipIndex);
                }

                BufferedReader reader = new BufferedReader(new
                    InputStreamReader(
                    in));
                StringBuffer buf = new StringBuffer();
                String line = null;

                while ( (line = reader.readLine()) != null) {
                    buf.append(line);
                    buf.append(" ");
                }

                text.setText(buf.toString());
                text.setCaretPosition(0);
                text.scrollRectToVisible(text.getVisibleRect());

                /*
                   javax.swing.text.MutableAttributeSet attr =
                       new javax.swing.text.SimpleAttributeSet();
                     attr.addAttribute(javax.swing.text.StyleConstants.FontFamily,
                       UIManager.getFont("TextArea.font").getFamily());
                   attr.addAttribute(javax.swing.text.StyleConstants.FontSize,
                     new Integer(UIManager.getFont("TextArea.font").getSize()));
                   text.getStyledDocument().setCharacterAttributes(0,
                       text.getStyledDocument().getLength(), attr, false);
                 */
                break;
            }
            catch (Exception e) {
                if (tipIndex == 0) {
                    break;
                }
                else {
                    tipIndex = 0;
                }
            }
            finally {
                GruntspudUtil.closeStream(in);
            }
        }
    }

    /**
     * Get if to show the tips
     * @return
     */
    public boolean isShow() {
        return show.isSelected();
    }

    /**
     * Action performed
     *
     * @param event
     */
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (source == close) {
            setVisible(false);
        }
        else if (source == next) {
            nextTip();
        }
    }

    class TipTextPane
        extends JTextPane {
        TipTextPane() {
            super();
            setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        public boolean isFocusTraversable() {
            return false;
        }
    }

    class EdgeBorder
        implements Border, SwingConstants {
        int edge = NORTH;
        int lift = LOWERED;

        EdgeBorder() {
            this(NORTH);
        }

        EdgeBorder(int edge) {
            this.edge = edge;
        }

        public Insets getBorderInsets(Component component) {
            switch (edge) {
                case SOUTH:
                    return new Insets(0, 0, 2, 0);

                case EAST:
                    return new Insets(0, 2, 0, 0);

                case WEST:
                    return new Insets(0, 0, 0, 2);

                default:
                    return new Insets(2, 0, 0, 0);
            }
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public void paintBorder(Component component, Graphics g, int x, int y,
                                int w, int h) {
            if (lift == RAISED) {
                g.setColor(component.getBackground().brighter());
            }
            else {
                g.setColor(component.getBackground().darker());
            }

            switch (edge) {
                case SOUTH:
                    g.drawLine(x, (y + h) - 2, w, (y + h) - 2);

                    break;

                case EAST:
                    g.drawLine( (x + w) - 2, y, (x + w) - 2, y + h);

                    break;

                case WEST:
                    g.drawLine(x + 1, y, x + 1, y + h);

                    break;

                default:
                    g.drawLine(x, y, x + w, y);
            }

            if (lift == RAISED) {
                g.setColor(component.getBackground().darker());
            }
            else {
                g.setColor(component.getBackground().brighter());
            }

            switch (edge) {
                case SOUTH:
                    g.drawLine(x, (y + h) - 1, w, (y + h) - 1);

                    break;

                case EAST:
                    g.drawLine( (x + w) - 1, y, (x + w) - 1, y + h);

                    break;

                case WEST:
                    g.drawLine(x + 1, y, x + 1, y + h);

                    break;

                default:
                    g.drawLine(x, y + 1, x + w, y + 1);
            }
        }
    }
}
