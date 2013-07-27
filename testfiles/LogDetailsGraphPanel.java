/*
 * Copyright 2001 Nicholas Allen (nallen@freenet.co.uk) This file is part of
 * JavaCVS. JavaCVS is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. JavaCVS is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with JavaCVS; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package allensoft.javacvs.client.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.lib.cvsclient.command.log.LogInformation;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 */
public class LogDetailsGraphPanel extends JPanel {
  private LogDetailsGraph m_Graph;
  private LogInformation logInformation;

  /**
   * Creates a new LogDetailsGraphPanel object.
   */
  public LogDetailsGraphPanel() {
    super(new BorderLayout());
    m_Graph = new LogDetailsGraph();
    add(new JScrollPane(m_Graph), BorderLayout.CENTER);
    final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 50);
    add(slider, BorderLayout.WEST);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        int n = slider.getValue();
        if (n > 50) m_Graph.setScale(1.0 + ((n - 50) / 10.0));
        else
          m_Graph.setScale(n / 50.0);
      }
    });
  }
  
  public void setLogInformation(LogInformation logInformation, List sortedRevisions) {
    this.logInformation = logInformation;
    Branch rootBranch = new Branch("1");
    for(Iterator i = sortedRevisions.iterator(); i.hasNext(); ) {
      LogInformation.Revision rev = (LogInformation.Revision)i.next();
      String revNumber = rev.getNumber();
      Revision revision = new Revision(rev, rev.getBranches() != null, logInformation.getSymNamesForRevision(revNumber)); 
      if(rev.getBranches() != null) {
        StringTokenizer t = new StringTokenizer(rev.getBranches(), " ");
        while(t.hasMoreTokens()) {
          String newBranchName = t.nextToken();
          Branch newBranch = new Branch(newBranchName);
          revision.addBranch(newBranch);
        }
      }
      Branch addTo = findBranch(rootBranch, revision);
      if(addTo != null) {
        addTo.m_Revisions.add(revision);
      }
    }
    m_Graph.setRootBranch(rootBranch);
    
  }
  
  private Branch findBranch(Branch branch, Revision revision) {
    for(int i = branch.m_Revisions.size() -1 ; i >= 0; i--) {
      Revision r = (Revision)branch.m_Revisions.get(i);
      for(int j = r.m_Branches.size() -1 ; j >= 0 ; j--) {
        Branch b = findBranch((Branch)r.m_Branches.get(j), revision);
        if(b != null) {
          return b;
        }        
      }
    }
    if(revision.m_RevisionDetails.getNumber().startsWith(branch.m_Branch + ".")) {
      return branch;
    }
    return null;
  }
  
  private Branch m_RootBranch;

  private FontMetrics m_FontMetrics;

  private double m_dScale = 1;

  private int m_nMaxWidth;

  private int m_nMaxHeight;

  private Color m_BranchStartColor = Color.pink;

  private Color m_RevisionColor = new Color(120, 220, 130);

  /**
   * Displays log details as a graph in a panel.
   * 
   * @author Nicholas Allen
   */
  class LogDetailsGraph extends JPanel {

    /**
     * Creates a new LogDetailsGraph object.
     */
    public LogDetailsGraph() {
      setBackground(Color.white);
      setToolTipText("<html>1<br>2<br>3");
      setPreferredSize(new Dimension(300, 300));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param details DOCUMENT ME!
     */
    public void setRootBranch(Branch branch) {
      m_RootBranch = branch;
      m_FontMetrics = null;
      repaint();
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public double getScale() {
      return m_dScale;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param d DOCUMENT ME!
     */
    public void setScale(double d) {
      m_dScale = d;
      updatePreferredSize();
    }

    private void updatePreferredSize() {
      if (m_FontMetrics != null) {
        setPreferredSize(new Dimension((int) (m_nMaxWidth * m_dScale),
            (int) (m_nMaxHeight * m_dScale)));
        revalidate();
        repaint();
      }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param e DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getToolTipText(MouseEvent e) {
      Revision r = getRevisionAtPoint(e.getX(), e.getY());
      if (r != null) return r.getToolTipText();
      return null;
    }

    private Revision getRevisionAtPoint(int x, int y) {
      if (m_RootBranch != null)
          return m_RootBranch.getRevisionAtPoint((int) (x / m_dScale),
              (int) (y / m_dScale));
      return null;
    }

    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      if (m_FontMetrics == null) {
        m_FontMetrics = g.getFontMetrics(getFont());
        if (m_RootBranch != null) {
          m_nMaxWidth = 0;
          m_nMaxHeight = 0;
          m_RootBranch.calculateSizes();
          m_RootBranch.layout(20, 20);
          updatePreferredSize();
        }
        return;
      }
      g2.scale(m_dScale, m_dScale);
      if (m_RootBranch != null) m_RootBranch.paint(g);
    }
  }

  /** Represent a branch on the graph which has a sequence of revisions. */
  private class Branch {
    private int m_nX;

    private int m_nY;

    private int m_nWidth;

    private int m_nHeight;

    private boolean m_bLayedOut = false;

    private java.util.List m_Revisions = new ArrayList();
    
    private String m_Branch;

    Branch(String sBranch) {
      m_Branch = sBranch;
    }

    public Rectangle getBounds() {
      return new Rectangle(m_nX, m_nY, m_nWidth, m_nHeight);
    }

    public void calculateSizes() {
      m_nWidth = 0;
      m_nHeight = 0;
      Iterator i = m_Revisions.iterator();
      while (i.hasNext()) {
        Revision r = (Revision) i.next();
        r.calculateSizes();
        if (r.m_nWidth > m_nWidth) m_nWidth = r.m_nWidth;
        m_nHeight += r.m_nHeight;
      }
      m_nHeight += (50 * (m_Revisions.size() - 1));
      m_bLayedOut = false;
    }

    public void layout(int x, int y) {
      m_nX = x;
      m_nY = y;
      m_bLayedOut = true;
      y += m_nHeight;
      for (int i = m_Revisions.size() - 1; i >= 0; i--) {
        Revision r = ((Revision) m_Revisions.get(i));
        y -= r.m_nHeight;
        r.layout(x + ((m_nWidth - r.m_nWidth) / 2), y);
        y -= 50;
      }
    }

    private Revision getRevisionAtPoint(int x, int y) {
      Iterator i = m_Revisions.iterator();
      while (i.hasNext()) {
        Revision r = (Revision) i.next();
        Revision result = r.getRevisionAtPoint(x, y);
        if (result != null) return result;
      }
      return null;
    }

    /**
     * Gets the current overlap of any branches that have already been layed out
     * with the supplied rectangle. Returns null if there is no overlap.
     */
    private Rectangle getBranchOverlap(Rectangle bounds) {
      if (m_bLayedOut) {
        Rectangle overlap = bounds.intersection(new Rectangle(m_nX, m_nY,
            m_nWidth, m_nHeight));
        if (overlap != null) return overlap;
      }
      Iterator i = m_Revisions.iterator();
      while (i.hasNext()) {
        Revision r = (Revision) i.next();
        Rectangle overlap = r.getBranchOverlap(bounds);
        if (overlap != null) return overlap;
      }
      return null;
    }

    public void paint(Graphics g) {
      int x = m_nX + (m_nWidth / 2);
      for (int i = 0; i < m_Revisions.size(); i++) {
        Revision r = (Revision) m_Revisions.get(i);
        r.paint(g);
        if (i < (m_Revisions.size() - 1))
            g.drawLine(x, r.m_nY + r.m_nHeight, x, r.m_nY + r.m_nHeight + 50);
      }
    }
  }

  /**
   * Represents one Revision object on the graph that displays details about a
   * revision. A revision also contains a list of branches that started at this
   * revision.
   */
  private class Revision {
    private LogInformation.Revision m_RevisionDetails;

    private java.util.List m_Branches = new ArrayList();

    private int m_nX;

    private int m_nY;

    private int m_nWidth;

    private int m_nHeight;

    private boolean m_bBranchStart;

    private String m_sTooltip;
    
    private List m_lTags;

    Revision(LogInformation.Revision revisionDetails, boolean branchStart, List tags) {
      m_RevisionDetails = revisionDetails;
      m_bBranchStart = branchStart;
      m_lTags = tags;
    }
    
    void addBranch(Branch branch) {
      m_Branches.add(branch);
    }

    private Revision getRevisionAtPoint(int x, int y) {
      if (new Rectangle(m_nX, m_nY, m_nWidth, m_nHeight).contains(x, y))
          return this;
      for (int i = 0; i < m_Branches.size(); i++) {
        Branch b = ((Branch) m_Branches.get(i));
        Revision result = b.getRevisionAtPoint(x, y);
        if (result != null) return result;
      }
      return null;
    }

    /*
     * Calculates the size this revision should be based on revision label, tags
     * and font metrics.
     */
    public void calculateSizes() {
      // Calculate maximum label width (revision number and tags).
      m_nWidth = m_FontMetrics.stringWidth(m_RevisionDetails.getNumber());
      if (m_RevisionDetails.getAuthor() != null)
          m_nWidth = Math.max(m_FontMetrics.stringWidth(m_RevisionDetails
              .getAuthor()), m_nWidth);
      for (int i = 0; i < m_lTags.size(); i++)
        m_nWidth = Math.max(m_FontMetrics.stringWidth(((LogInformation.SymName)m_lTags.get(i)).getName()), m_nWidth);
      m_nHeight = m_FontMetrics.getHeight()
          * (m_lTags.size() + 2);
      // Allow a 10 pixel border around the revision box.
      m_nWidth += (10 * 2);
      m_nHeight += (10 * 2);
      Iterator i = m_Branches.iterator();
      while (i.hasNext())
        ((Branch) i.next()).calculateSizes();
    }

    public void layout(int x, int y) {
      m_nX = x;
      m_nY = y;
      x += (m_nWidth + 50);
      // Layout branches from this revision
      for (int i = 0; i < m_Branches.size(); i++) {
        Branch b = ((Branch) m_Branches.get(i));
        // Find next suitable location for this branch. This is where the bounds
        // of the branch don't overlap any other branch.
        while (true) {
          Rectangle bounds = new Rectangle(x, y, x + b.m_nWidth, y
              + b.m_nHeight);
          Rectangle overlap = getBranchOverlap(bounds);
          if (overlap != null) x = overlap.x + overlap.width + 50;
          else
            break;
        }
        b.layout(x, y);
        x += (b.m_nWidth + 50);
      }
      if ((m_nX + m_nWidth) > m_nMaxWidth) m_nMaxWidth = m_nX + m_nWidth;
      if ((m_nY + m_nHeight) > m_nMaxHeight) m_nMaxHeight = m_nY + m_nHeight;
    }

    private Rectangle getBranchOverlap(Rectangle bounds) {
      Iterator i = m_Branches.iterator();
      while (i.hasNext()) {
        Branch b = (Branch) i.next();
        Rectangle overlap = b.getBranchOverlap(bounds);
        if (overlap != null) return overlap;
      }
      return null;
    }

    public void paint(Graphics g) {
      if (m_bBranchStart) {
        g.setColor(m_BranchStartColor);
        g.fillRoundRect(m_nX, m_nY, m_nWidth, m_nHeight, 25, 25);
        g.setColor(Color.black);
        g.drawRoundRect(m_nX, m_nY, m_nWidth, m_nHeight, 25, 25);
      } else {
        g.setColor(m_RevisionColor);
        g.fillRect(m_nX, m_nY, m_nWidth, m_nHeight);
        g.setColor(Color.black);
        g.drawRect(m_nX, m_nY, m_nWidth, m_nHeight);
      }
      drawCenteredText(g, m_RevisionDetails.getAuthor(), 0);
      drawCenteredText(g, m_RevisionDetails.getNumber(), 1);
      int y = m_nY + 10 + m_FontMetrics.getHeight();
      g.drawLine(m_nX, y, m_nX + m_nWidth, y);
      y += m_FontMetrics.getHeight();
      g.drawLine(m_nX, y, m_nX + m_nWidth, y);
      for (int i = 0; i < m_lTags.size(); i++)
        drawCenteredText(g, ((LogInformation.SymName)m_lTags.get(i)).getName(), i + 2);
      // Paint all the branches from this revision
      Iterator i = m_Branches.iterator();
      y = m_nY + (m_nHeight / 2);
      while (i.hasNext()) {
        Branch b = ((Branch) i.next());
        b.paint(g);
        // If this branch has revisions then draw a horizontal line to the first
        // revision
        if (b.m_Revisions.size() > 0) {
          Revision r = (Revision) b.m_Revisions.get(0);
          g.drawLine(m_nX + m_nWidth, y, r.m_nX, r.m_nY + (r.m_nHeight / 2));
        }
      }
    }

    private void drawCenteredText(Graphics g, String sText, int nRow) {
      if (sText == null) return;
      g.drawString(sText, m_nX
          + ((m_nWidth - m_FontMetrics.stringWidth(sText)) / 2), m_nY + 10
          + m_FontMetrics.getAscent() + (nRow * m_FontMetrics.getHeight()));
    }

    public String getToolTipText() {
      if (m_sTooltip == null) {
        String s = m_RevisionDetails.getMessage();
        if ((s == null) || (s.trim().length() == 0))
            return "<html>No log message";
        StringBuffer b = new StringBuffer();
        b.append("<html>");
        if (m_RevisionDetails.getDate() != null) {
          b.append("<b>");
          b.append(DateFormat.getDateTimeInstance().format(
              m_RevisionDetails.getDate()));
          b.append("</b><br>");
        }
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c == '\n') b.append("<br>");
          else
            b.append(c);
        }
        m_sTooltip = b.toString();
      }
      return m_sTooltip;
    }
  }
}