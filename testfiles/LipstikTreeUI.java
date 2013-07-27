package com.lipstikLF.delegate;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class LipstikTreeUI extends BasicTreeUI
{
    private boolean linesEnabled = true;
    private PropertyChangeListener lineStyleHandler;

    private static final String TREE_LINE_STYLE_KEY = "JTree.lineStyle";
    private static final String TREE_LINE_STYLE_NONE_VALUE = "None";

    /**
     * Create the UI delegate for the given component
     *
     * @param c The component for which to create the ui delegate
     * @return The created ui delegate
     */
    public static ComponentUI createUI(JComponent c)
    {
        return new LipstikTreeUI();
    }

    /**
     * Install the UI delegate for the given component
     *
     * @param c The component for which to install the ui delegate.
     */
    public void installUI(JComponent c)
    {
        super.installUI(c);
        updateLineStyle(c.getClientProperty(TREE_LINE_STYLE_KEY));
        lineStyleHandler = new LineStyleHandler();
        c.addPropertyChangeListener(lineStyleHandler);
    }

    /**
     * Uninstall the UI delegate for the given component
     *
     * @param c The component for which to install the ui delegate.
     */
    public void uninstallUI(JComponent c)
    {
        c.removePropertyChangeListener(lineStyleHandler);
        super.uninstallUI(c);
    }

    /**
      * Returns the default cell renderer that is used to do the
      * stamping of each node.
      */
    protected TreeCellRenderer createDefaultCellRenderer()
    {
	    return new LipstikDefaultTreeCellRenderer();
    }

//    private static BasicStroke stroke=new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 2.0f, 2.0f }, 2.0f);
    private static BasicStroke stroke=new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] { 1.0f, 1.0f }, 1.0f);

    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top, int bottom)
    {
        if (linesEnabled)
        	drawDashedVerticalLine(g, x, top, bottom);
    }

    protected void drawDashedVerticalLine(Graphics g, int x, int y1, int y2)
    {
	    Graphics2D g2d=(Graphics2D)g;
	    Stroke s = g2d.getStroke();
	    g2d.setStroke(stroke);
	    g2d.drawLine(x, y1, x, y2);
	    g2d.setStroke(s);
    }

    protected void paintHorizontalLine(Graphics g, JComponent c, int y, int left, int right)
    {
        if (linesEnabled)
            drawDashedHorizontalLine(g, y, left+1, right);
    }

    protected void drawDashedHorizontalLine(Graphics g, int y, int x1, int x2)
    {
        Graphics2D g2d=(Graphics2D)g;
        Stroke s = g2d.getStroke();
        g2d.setStroke(stroke);
        g2d.drawLine(x1, y, x2, y);
        g2d.setStroke(s);
    }

    // Draws the icon centered at (x,y)
    protected void drawCentered(Component c, Graphics graphics, Icon icon, int x, int y)
    {
        icon.paintIcon(c, graphics, x - icon.getIconWidth() / 2 - 1, y - icon.getIconHeight() / 2);
    }

    // Helper Code ************************************************************

    private void updateLineStyle(Object lineStyle)
    {
        linesEnabled = !TREE_LINE_STYLE_NONE_VALUE.equals(lineStyle);
    }

    // Listens for changes of the line style property
    private class LineStyleHandler implements PropertyChangeListener
    {
        public void propertyChange(PropertyChangeEvent e)
        {
            String name = e.getPropertyName();
            Object value = e.getNewValue();
            if (name.equals(TREE_LINE_STYLE_KEY))
                updateLineStyle(value);
        }
    }

}