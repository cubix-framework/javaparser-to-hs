/*
   Copyright 2001 Nicholas Allen (nallen@freenet.co.uk)
   This file is part of JavaCVS.
   JavaCVS is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2 of the License, or
   (at your option) any later version.
   JavaCVS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with JavaCVS; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package allensoft.diff;

import gruntspud.GruntspudContext;
import gruntspud.standalone.JDK13GruntspudHost;
import gruntspud.style.TextStyle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;


/* A component that displays differences with colored text and background. */
public class DiffDisplay extends JPanel implements Scrollable {
    private java.util.List m_Lines = new ArrayList(50);
    private Font m_DisplayFont;
    private FontMetrics m_FontMetrics = null;
    private int m_nTabSize;
    private TextStyle m_NoStyle;
    private TextStyle m_InsertStyle;
    private TextStyle m_DeleteStyle;
    private TextStyle m_ChangeStyle;
    private TextStyle m_NonExistantStyle;
    private JComponent m_RowHeader = null;
    private char[] m_Chars = new char[1];
    private int m_nMarginWidth;

    /* Creates a new DiffDisplay using the supplied font for the text and tab size for
       the tabs. Lines of text to be displayed should be added with the addLine method. */
    public DiffDisplay(Font font, int nTabSize, GruntspudContext context) {
        setPreferredSize(new Dimension(100, 100));
        m_DisplayFont = font;
        m_nTabSize = nTabSize;

		setNonExistantStyle(context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_NON_EXISTANT));
		setStyle(DiffType.NONE, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_IDENTICAL));
		setStyle(DiffType.INSERTION, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_INSERTION));
		setStyle(DiffType.DELETION, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_DELETION));
		setStyle(DiffType.CHANGE,context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_CHANGE));
		
    }

    /* Creates a DiffDisplay using a monospaced font and tab size for
       the tabs. Lines of text to be displayed should be added with the addLine method. */
    public DiffDisplay(int nTabSize, GruntspudContext context) {
        this(new Font("Monospaced", Font.PLAIN, 12), nTabSize, context);
    }

    /* Creates a DiffDisplay using a monospaced font and a tab size of 4 for
       the tabs. Lines of text to be displayed should be added with the addLine method. */
    public DiffDisplay(GruntspudContext context) {
        this(4, context);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    /**
     * DOCUMENT ME!
     *
     * @param visibleRect DOCUMENT ME!
     * @param orientation DOCUMENT ME!
     * @param direction DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        if(m_FontMetrics != null) {
            if(orientation == SwingConstants.VERTICAL)
                return m_FontMetrics.getHeight();

            return m_FontMetrics.charWidth(' ');
        }

        return 10;
    }

    /**
     * DOCUMENT ME!
     *
     * @param visibleRect DOCUMENT ME!
     * @param orientation DOCUMENT ME!
     * @param direction DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect,
        int orientation, int direction) {
        if(orientation == SwingConstants.VERTICAL)
            return visibleRect.height;

        return visibleRect.width;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /** Adds a line to be displayed. This method should only be called
       before displaying this component. It should not be called after the
       component has been displayed. */
    public void addLine(int nLineNum, DiffType type, String sLine) {
       m_Lines.add(new Line(nLineNum, type, sLine));
    }

    /**
     * DOCUMENT ME!
     */
    public void addNonExistantLine() {
        m_Lines.add(new Line(-1, DiffType.NONE, ""));
    }

    /** Gets the font used to display the text. */
    public Font getDisplayFont() {
        return m_DisplayFont;
    }

    /** Sets the font used to display the text. */
    public synchronized void setDisplayFont(Font font) {
        m_DisplayFont = font;
        m_FontMetrics = null;
        calculatePreferredSize();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int getTabSize() {
        return m_nTabSize;
    }

    /**
     * DOCUMENT ME!
     *
     * @param nTabSize DOCUMENT ME!
     */
    public void setTabSize(int nTabSize) {
        m_nTabSize = nTabSize;
        calculatePreferredSize();
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TextStyle getStyle(DiffType type) {
        TextStyle style = m_NoStyle;

        if(type == DiffType.NONE)
            ;

        else if(type == DiffType.INSERTION)
            style = m_InsertStyle;

        else if(type == DiffType.DELETION)
            style = m_DeleteStyle;

        else
            style = m_ChangeStyle;

        return style;
    }

    /**
     * DOCUMENT ME!
     *
     * @param type DOCUMENT ME!
     * @param style DOCUMENT ME!
     */
    public synchronized void setStyle(DiffType type, TextStyle style) {
        if(type == DiffType.NONE) {
            m_NoStyle = style;
            setBackground(style.getBackground());
        } else if(type == DiffType.INSERTION)
            m_InsertStyle = style;

        else if(type == DiffType.DELETION)
            m_DeleteStyle = style;

        else
            m_ChangeStyle = style;

        repaint();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TextStyle getNonExistantStyle() {
        return m_NonExistantStyle;
    }

    /**
     * DOCUMENT ME!
     *
     * @param style DOCUMENT ME!
     */
    public synchronized void setNonExistantStyle(TextStyle style) {
        m_NonExistantStyle = style;
    }

    protected synchronized void paintComponent(Graphics g) {
        super.paintComponent(g);

        // We can only calculate the size of this component from a FontMetrics which
        // can only be obtained from a Graphics object so we need to compute our preferred
        // size in this method.
        if(m_FontMetrics == null) {
            m_FontMetrics = g.getFontMetrics(m_DisplayFont);
            calculatePreferredSize();

            return;
        }

        g.setFont(m_FontMetrics.getFont());

        // Paint lines that need painting
        Rectangle clipBounds = g.getClipBounds();
        paintLines(g, clipBounds.y / m_FontMetrics.getHeight(),
            (clipBounds.y + clipBounds.height) / m_FontMetrics.getHeight());

        // Draw left margin line
        g.setColor(Color.gray);
        g.drawLine(m_nMarginWidth, clipBounds.y, m_nMarginWidth,
            clipBounds.y + clipBounds.height);

        // Draw right margin line
        int x = getPreferredSize().width - 1;
        g.drawLine(x, clipBounds.y, x, clipBounds.y + clipBounds.height);

        // Draw line showing end of file
        int y = (m_Lines.size() * m_FontMetrics.getHeight()) - 1;
        g.drawLine(0, y, getSize().width, y);
        
        // Paint to the end of the viewport
        if(getParent() instanceof JViewport) {
          Dimension s = ((JViewport)getParent()).getSize();
          y++;
          //g.setColor(getBackground());
          g.setColor(Color.red);
          g.fillRect(0, y, s.width, s.height - y);
        }
    }

    private void calculatePreferredSize() {
        int nWidth = 100;

        if(m_FontMetrics == null)
            return;

        m_nMarginWidth = (String.valueOf(m_Lines.size()).length() + 1) * m_FontMetrics.charWidth('9');

        for(int i = 0; i < m_Lines.size(); i++) {
            Line line = (Line)m_Lines.get(i);

            // Calculate width of line
            String sLine = line.m_sLine;
            int length = sLine.length();
            int x = 0;
            int nSpaceWidth = m_FontMetrics.charWidth(' ');

            for(int nChar = 0, nActual = 0; nChar < length; nChar++) {
                char c = sLine.charAt(nChar);

                if(c == '\t') {
                    int nNumSpaces = (((nActual / m_nTabSize) + 1) * m_nTabSize) -
                        nActual;
                    nActual += nNumSpaces;
                    x += (nSpaceWidth * nNumSpaces);

                    continue;
                } else if(c == ' ') {
                    nActual++;
                    x += nSpaceWidth;

                    continue;
                } else {
                    nActual++;
                    x += m_FontMetrics.charWidth(c);
                }
            }

            nWidth = Math.max(nWidth, x);
        }

        setPreferredSize(new Dimension(m_nMarginWidth + nWidth,
                m_FontMetrics.getHeight() * m_Lines.size()));
        revalidate();
        repaint();
    }

    private void paintLines(Graphics g, int nStart, int nEnd) {
        for(int i = nStart; (i <= nEnd) && (i < m_Lines.size()); i++)
            paintLine(g, i);
    }

		public int lineYPos(int lineNo) {
			if (lineNo<0) {
				lineNo=0;
			} else if (lineNo>=m_Lines.size()) {
				lineNo=m_Lines.size()-1;
			}
			return lineNo * m_FontMetrics.getHeight();
		}
		
    private void paintLine(Graphics g, int nLine) {
        Line line = (Line)m_Lines.get(nLine);
        int nWidth = getSize().width;
        TextStyle style = (line.m_nLineNum == -1) ? m_NonExistantStyle
                                              : getStyle(line.m_Type);
        int y = lineYPos(nLine);

        g.setColor(style.getBackground());
        g.fillRect(0, y, nWidth, m_FontMetrics.getHeight());

        y += m_FontMetrics.getAscent();

        // Draw line number in margin
        if(line.m_nLineNum != -1) {
            if(line.m_Type == DiffType.NONE)
                g.setColor(Color.red);

            else
                g.setColor(Color.black);

            g.drawString(String.valueOf(line.m_nLineNum), 0, y);
        }

        // Draw characters
        g.setColor(style.getForeground());

        String sLine = line.m_sLine;
        int length = sLine.length();
        int x = m_nMarginWidth;
        int nSpaceWidth = m_FontMetrics.charWidth(' ');
        Rectangle clipBounds = g.getClipBounds();
        int nStartClipX = clipBounds.x;
        int nEndClipX = clipBounds.x + clipBounds.width;

        for(int nChar = 0, nActual = 0; nChar < length; nChar++) {
            char c = sLine.charAt(nChar);

            if(c == '\t') {
                int nNumSpaces = (((nActual / m_nTabSize) + 1) * m_nTabSize) -
                    nActual;
                nActual += nNumSpaces;
                x += (nSpaceWidth * nNumSpaces);

                continue;
            } else if(c == ' ') {
                nActual++;
                x += nSpaceWidth;

                continue;
            } else {
                int nCharWidth = m_FontMetrics.charWidth(c);
                int x2 = x + nCharWidth;

                // Only draw char if needed
                if(!((x2 < nStartClipX) || (x > nEndClipX))) {
                    m_Chars[0] = c;
                    g.drawChars(m_Chars, 0, 1, x, y);
                }

                if(x > nEndClipX)
                    break;

                nActual++;
                x += nCharWidth;
            }
        }
    }
		
		java.util.List getLines() {
			return m_Lines;
		}

}
