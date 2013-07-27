package com.lipstikLF.delegate;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.util.LipstikGradients;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;



public final class LipstikTabbedPaneUI extends BasicTabbedPaneUI
{
    /**
     * The outer highlight color of the border.
     */
    private Color outerHighlight = LipstikLookAndFeel.getMyCurrentTheme().getControlHighlight();

    /**
     * The shadow color of the round corners.
     */
    private Color roundShadow = LipstikLookAndFeel.getMyCurrentTheme().getControlDarkShadow();

    /**
     * The shadow color of the outer border.
     */
    private Color outerShadow = LipstikLookAndFeel.getMyCurrentTheme().getBorderNormal();

    /**
     * The shadow color of the inner border.
     */
    private Color innerShadow = LipstikLookAndFeel.getMyCurrentTheme().getControlShadow();

    /**
     * Creates the UI delegate for the given component.
     *
     * @param c The component to create its UI delegate.
     * @return The UI delegate for the given component.
     */
    public static ComponentUI createUI(JComponent c)
    {
      return new LipstikTabbedPaneUI();
    }

    private void ensureCurrentLayout()
    {
        if (!tabPane.isValid())
            tabPane.validate();

    /* If tabPane doesn't have a peer yet, the validate() call will
     * silently fail.  We handle that by forcing a layout if tabPane
     * is still invalid.  See bug 4237677.
     */
        if (!tabPane.isValid())
        {
            TabbedPaneLayout layout = (TabbedPaneLayout)tabPane.getLayout();
            layout.calculateLayoutInfo();
        }
    }


    /**
     * Paint the component.
     *
     * @param g The graphics resource used to paint the component
     * @param c The component to paint.
     */
    public void paint(Graphics g, JComponent c)
    {
        int selectedIndex = tabPane.getSelectedIndex();
        int tabPlacement  = tabPane.getTabPlacement();
        int tabCount      = tabPane.getTabCount();

        ensureCurrentLayout();

        Rectangle iconRect = new Rectangle();
        Rectangle textRect = new Rectangle();
        Rectangle clipRect = g.getClipBounds();

        // Paint tabRuns of tabs from back to front
        for (int i = runCount - 1; i >= 0; i--)
        {
            int start = tabRuns[i];
            int next  = tabRuns[(i == runCount - 1) ? 0 : i + 1];
            int end   = (next != 0 ? next - 1 : tabCount - 1);
            for (int j = end; j >= start; j--)
            {
                if (rects[j].intersects(clipRect))
                    paintTab(g, tabPlacement, rects, j, iconRect, textRect);
            }
        }
        // Always paint selected tab
        // since it may overlap other tabs
        if (selectedIndex >= 0)
        {
            if (rects[selectedIndex].intersects(clipRect))
                paintTab(g, tabPlacement, rects, selectedIndex, iconRect, textRect);
        }
        // Paint content border
        paintContentBorder(g, tabPlacement, selectedIndex);
    }

    /**
     * Paints the backround of a given tab.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tab to paint.
     * @param tabIndex The index of the tab to paint.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     * @param isSelected True if the tab to paint is selected otherwise false.
     */
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
      int x, int y, int w, int h, boolean isSelected)
    {
      if (isSelected)
      {
          g.setColor(LipstikLookAndFeel.getMyCurrentTheme().getControl());
          g.fillRect(x, y, w, h);
      }
      else
          LipstikGradients.drawGradient(g, LipstikLookAndFeel.getMyCurrentTheme().getControl(), null, x,y, w-1, h-1, true);
    }

    /**
     * Paints the border of a given tab.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tab to paint.
     * @param selectedIndex The index of the selected tab.
     */
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex)
    {
      int width = tabPane.getWidth();
      int height = tabPane.getHeight();
      Insets insets = tabPane.getInsets();

      int x = insets.left;
      int y = insets.top;
      int w = width - insets.right - insets.left;
      int h = height - insets.top - insets.bottom;

      switch (tabPlacement)
      {
        case LEFT:
          x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
          w -= (x - insets.left);
          break;
        case RIGHT:
          w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
          break;
        case BOTTOM:
          h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
          break;
        case TOP:
        default:
          y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
          h -= (y - insets.top);
      }
      paintContentBorderTopEdge(g, tabPlacement, selectedIndex, x, y, w, h);
      paintContentBorderLeftEdge(g, tabPlacement, selectedIndex, x, y, w, h);
      paintContentBorderBottomEdge(g, tabPlacement, selectedIndex, x, y, w, h);
      paintContentBorderRightEdge(g, tabPlacement, selectedIndex, x, y, w, h);

    }

    /**
     * Paints the top edge of the content border.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tabs.
     * @param selectedIndex The index of the selected tab.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     */
    protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
      int selectedIndex, int x, int y, int w, int h)
    {
        boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();

        g.setColor(outerShadow);
        if (tabPlacement != TOP || selectedIndex < 0)
        {
            g.drawLine(x, y, x+w-1, y);
            g.setColor(outerHighlight);
            g.drawLine(x+1, y+1, x+w-1, y+1);
        }
        else
        {
            Rectangle selRect = rects[selectedIndex];
            if (selRect.x > 0 && (tabRuns[0] != selectedIndex || !leftToRight))
            {
                g.drawLine(x, y, selRect.x-1, y);
                g.setColor(outerHighlight);
                g.drawLine(x, y+1, selRect.x+1, y+1);
            }
            else
                g.setColor(outerHighlight);

            if (selRect.x + selRect.width < x + w)
            {
                g.drawLine(selRect.x + selRect.width-1, y+1, x+w-1, y+1);
                g.setColor(outerShadow);
                g.drawLine(selRect.x + selRect.width, y, x+w-2, y);
            }
        }
    }

    /**
     * Paints the bottom edge of the content border.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tabs.
     * @param selectedIndex The index of the selected tab.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     */
    protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
      int selectedIndex, int x, int y, int w, int h)
    {
        boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();

        y += h-1;
        g.setColor(outerShadow);
        if (tabPlacement != BOTTOM || selectedIndex < 0)
        {
            g.drawLine(x, y, x+w-1, y);
            g.setColor(innerShadow);
            g.drawLine(x+1, y-1, x+w-1, y-1);
        }
        else
        {
            Rectangle selRect = rects[selectedIndex];
            if (selRect.x > 0 && (tabRuns[0] != selectedIndex || !leftToRight))
            {
                g.drawLine(x, y, selRect.x-1, y);
                g.setColor(innerShadow);
                g.drawLine(x+1, y-1, selRect.x+1, y-1);
            }
            else
                g.setColor(innerShadow);

            if (selRect.x + selRect.width < x + w)
            {
                g.drawLine(selRect.x + selRect.width-1, y-1, x+w-2, y-1);
                g.setColor(outerShadow);
                g.drawLine(selRect.x + selRect.width, y, x+w-1, y);
            }
        }
    }

    /**
     * Paints the left edge of the content border.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tabs.
     * @param selectedIndex The index of the selected tab.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     */
    protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
      int selectedIndex, int x, int y, int w, int h)
    {
        g.setColor(outerShadow);
        if (tabPlacement != LEFT || selectedIndex < 0)
        {
            g.drawLine(x, y, x, y+h-2);
            g.setColor(outerHighlight);
            g.drawLine(x+1, y+1, x+1, y+h-3);
        }
        else
        {
            Rectangle selRect = rects[selectedIndex];
            if (tabRuns[0] != selectedIndex)
            {
                g.drawLine(x, y, x, selRect.y-1);
                g.setColor(outerHighlight);
                g.drawLine(x+1, y+1, x+1, selRect.y);
            }
            else
                g.setColor(outerHighlight);

            if (selRect.y + selRect.height < y+h)
            {
                g.drawLine(x+1, selRect.y + selRect.height, x+1, y+h-3);
                g.setColor(outerShadow);
                g.drawLine(x, selRect.y + selRect.height, x, y+h-2);
            }
        }
    }


    /**
     * Paints the right edge of the content border.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tabs.
     * @param selectedIndex The index of the selected tab.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     */
    protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
      int selectedIndex, int x, int y, int w, int h)
    {
        g.setColor(outerShadow);
        x += w-1;
        if (tabPlacement != RIGHT || selectedIndex < 0)
        {
            g.drawLine(x, y, x, y+h-2);
            g.setColor(innerShadow);
            g.drawLine(x-1, y+1, x-1, y+h-3);
        }
        else
        {
            Rectangle selRect = rects[selectedIndex];
            if (tabRuns[0] != selectedIndex)
            {
                g.drawLine(x, y, x, selRect.y - 1);
                g.setColor(innerShadow);
                g.drawLine(x-1, y+1, x-1, selRect.y);
            }
            else
                g.setColor(innerShadow);

            if (selRect.y + selRect.height < y + h)
            {
                g.drawLine(x-1 , selRect.y + selRect.height, x-1, y+h-3);
                g.setColor(outerShadow);
                g.drawLine(x, selRect.y + selRect.height, x, y+h-2);
            }
        }
    }

    /**
     * Draws the border around each tab.
     *
     * @param g The graphics context.
     * @param tabPlacement The placement of the tabs.
     * @param tabIndex The index of the tab to paint.
     * @param x The x coordinate of the top left corner.
     * @param y The y coordinate of the top left corner.
     * @param w The width.
     * @param h The height.
     * @param isSelected True if the tab to paint is selected otherwise false.
     */
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
            int x, int y, int w, int h, boolean isSelected)
    {
        int tabCount = tabPane.getTabCount();

        boolean leftToRight = tabPane.getComponentOrientation().isLeftToRight();
        boolean last  = tabIndex==lastTabInRun(tabCount, runCount - 1);
        boolean first = tabIndex==tabRuns[runCount-1]; // is first tab

        Color background = tabPane.getParent().getBackground();

        w--;
        if (x < 0) x = 0;

        g.setColor(outerShadow);
        switch (tabPlacement)
        {
        case LEFT:
            g.translate(x, y);
            g.drawLine(0, 0, w, 0);
            g.drawLine(0, 1, 0, h - 1);
            g.drawLine(0, h, w, h);
            g.setColor(outerHighlight);
            g.drawLine(1, 1, w, 1);
            g.drawLine(1, 2, 1, h - 1);
            if (isSelected || tabRuns[runCount - 1] == tabIndex)
            {
                g.setColor(roundShadow);
                g.drawLine(1, 0, 1, 0);
                g.drawLine(0, 1, 1, 1);
                g.setColor(background);
                g.drawLine(0, 0, 0, 0);
            }
            if (isSelected || tabIndex == lastTabInRun(tabCount, runCount - 1))
            {
                g.setColor(roundShadow);
                g.drawLine(1, h - 1, 1, h);
                g.drawLine(0, h - 1, 1, h - 1);
                g.setColor(background);
                g.drawLine(0, h, 0, h);
            }
            g.translate(-x, -y);
            break;
        case RIGHT:
            g.translate(x, y);
            g.drawLine(0, 0, w, 0);
            g.drawLine(w, 1, w, h - 1);
            g.drawLine(0, h, w, h);
            g.setColor(outerHighlight);
            g.drawLine(0, 1, w - 1, 1);
            if (isSelected || tabRuns[runCount - 1] == tabIndex)
            {
                g.setColor(roundShadow);
                g.drawLine(w - 1, 0, w - 1, 0);
                g.drawLine(w - 1, 1, w, 1);
                g.setColor(background);
                g.drawLine(w, 0, w, 0);
            }
            if (isSelected || tabIndex == lastTabInRun(tabCount, runCount - 1))
            {
                g.setColor(roundShadow);
                g.drawLine(w - 1, h - 1, w - 1, h);
                g.drawLine(w - 1, h - 1, w, h - 1);
                g.setColor(background);
                g.drawLine(w, h, w, h);
            }
            g.translate(-x, -y);
            break;
        case BOTTOM:
            g.translate(x, y);
            g.drawLine(0, h, w, h);
            g.drawLine(w, 0, w, h);
            if (isSelected || shouldDrawLeftEdge(tabIndex, leftToRight))
            {
                g.drawLine(0, 0, 0, h-1);
                if (isSelected)
                {
                    g.setColor(outerHighlight);
                    g.drawLine(1, 0, 1, h-2);
                }
            }
            g.setColor(innerShadow);
            g.drawLine(1, h-1, w-1, h-1);
            if (isSelected || (first && leftToRight) || (last && (!leftToRight)))
            {
                g.setColor(roundShadow);
                g.drawLine(1, h, 1, h);
                g.drawLine(0, h-1, 1, h-1);
                g.setColor(background);
                g.drawLine(0, h, 0, h);
            }
            if (isSelected || (last && leftToRight) || (first && (!leftToRight)))
            {
                g.setColor(roundShadow);
                g.drawLine(w-1, h, w-1, h);
                g.drawLine(w-1, h-1, w, h-1);
                g.setColor(background);
                g.drawLine(w, h, w, h);
            }
            g.translate(-x, -y);
            break;
        case TOP:
        default:
            g.translate(x, y);
            g.drawLine(0, 0, w, 0);
            g.drawLine(w, 0, w, h-1);
            if (isSelected || shouldDrawLeftEdge(tabIndex, leftToRight))
            {
                g.drawLine(0, 0, 0, h-1);
                g.setColor(outerHighlight);
                g.drawLine(1, 2, 1, h-1);
            }
            else
                g.setColor(outerHighlight);

            g.drawLine(1, 1, w-1, 1);
            if (isSelected || (leftToRight && first) || (!leftToRight && last))
            {
                g.setColor(roundShadow);
                g.drawLine(1, 0, 1, 0);
                g.drawLine(0, 1, 1, 1);
                g.setColor(background);
                g.drawLine(0, 0, 0, 0);
            }
            if (isSelected || (leftToRight && last) || (!leftToRight && first))
            {
                g.setColor(roundShadow);
                g.drawLine(w - 1, 0, w - 1, 0);
                g.drawLine(w - 1, 1, w, 1);
                g.setColor(background);
                g.drawLine(w, 0, w, 0);
            }
            g.translate(-x, -y);
        }
    }

    /*
     * Copied here from super(super)class to avoid labels being centered on
     * vertical tab runs if they consist of icon and text
     */
    protected void layoutLabel(int tabPlacement, FontMetrics metrics,
            int tabIndex, String title, Icon icon, Rectangle tabRect,
            Rectangle iconRect, Rectangle textRect, boolean isSelected)
    {
        textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
        Rectangle calcRectangle = new Rectangle(tabRect);

        if (isSelected)
        {
            Insets calcInsets = getSelectedTabPadInsets(tabPlacement);
            calcRectangle.x += calcInsets.left;
            calcRectangle.y += calcInsets.top;
            calcRectangle.width -= calcInsets.left + calcInsets.right;
            calcRectangle.height -= calcInsets.bottom + calcInsets.top;
        }

        int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
        int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);

        if ((tabPlacement == RIGHT || tabPlacement == LEFT) && icon != null && title != null && !title.equals(""))
        {
            SwingUtilities.layoutCompoundLabel(tabPane, metrics, title, icon,
                    SwingConstants.CENTER, SwingConstants.LEFT,
                    SwingConstants.CENTER, SwingConstants.TRAILING,
                    calcRectangle, iconRect, textRect, textIconGap);
            xNudge += 4;
        }
        else
        {
            SwingUtilities.layoutCompoundLabel(tabPane, metrics, title, icon,
                    SwingConstants.CENTER, SwingConstants.CENTER,
                    SwingConstants.CENTER, SwingConstants.TRAILING,
                    calcRectangle, iconRect, textRect, textIconGap);
            iconRect.y += calcRectangle.height % 2;
        }

        iconRect.x += xNudge;
        iconRect.y += yNudge;
        textRect.x += xNudge;
        textRect.y += yNudge;
    }

    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected)
    {
    }

    protected boolean shouldRotateTabRuns(int tabPlacement)
    {
        return true;
    }

    /**
     * determine if tab is first in any run
     * @param tabIndex
     * @param leftToRight
     * @return true if tab is the first one in any of runs
     */
    private boolean shouldDrawLeftEdge(int tabIndex, boolean leftToRight)
    {
        int tabCount =  tabPane.getTabCount();
        for (int i=0; i<runCount; i++)
            if (leftToRight)
            {
                if (tabIndex == tabRuns[i])
                    return true;
            }
            else
            if (tabIndex == lastTabInRun(tabCount, i))
                return true;

        return false;
    }

}