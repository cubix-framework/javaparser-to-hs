package com.lipstikLF.delegate;

import com.lipstikLF.util.LipstikBorderFactory;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

public class LipstikDefaultTreeCellRenderer extends JLabel implements TreeCellRenderer
{
    /** Last tree the renderer was painted in. */
    private JTree tree;

    /** Is the value currently selected. */
    protected boolean selected;
    /** True if has focus. */
    protected boolean hasFocus;
    /** True if draws focus border around icon as well. */
    private boolean drawsFocusBorderAroundIcon;

    // Icons
    /** Icon used to show non-leaf nodes that aren't expanded. */
    private transient Icon closedIcon;

    /** Icon used to show leaf nodes. */
    transient protected Icon leafIcon;

    /** Icon used to show non-leaf nodes that are expanded. */
    transient protected Icon openIcon;

    // Colors
    /** Color to use for the foreground for selected nodes. */
    private Color textSelectionColor;

    /** Color to use for the foreground for non-selected nodes. */
    private Color textNonSelectionColor;

    /** Color to use for the background when a node is selected. */
    private Color backgroundSelectionColor;

    /** Color to use for the background when the node isn't selected. */
    protected Color backgroundNonSelectionColor;

    /** Color to use for the focus indicator when the node has focus. */
    private Color borderSelectionColor;

    /**
      * Returns a new instance of DefaultTreeCellRenderer.  Alignment is
      * set to left aligned. Icons and text color are determined from the
      * UIManager.
      */
    public LipstikDefaultTreeCellRenderer()
    {
        setHorizontalAlignment(JLabel.LEFT);

        setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        setOpenIcon(UIManager.getIcon("Tree.openIcon"));

        setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
        setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
        Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
        drawsFocusBorderAroundIcon = (value != null && ((Boolean)value).booleanValue());
    }


    /**
      * Returns the default icon, for the current laf, that is used to
      * represent non-leaf nodes that are expanded.
      */
    public static Icon getDefaultOpenIcon()
    {
	    return UIManager.getIcon("Tree.openIcon");
    }

    /**
      * Returns the default icon, for the current laf, that is used to
      * represent non-leaf nodes that are not expanded.
      */
    public static Icon getDefaultClosedIcon()
    {
	    return UIManager.getIcon("Tree.closedIcon");
    }

    /**
      * Returns the default icon, for the current laf, that is used to
      * represent leaf nodes.
      */
    public Icon getDefaultLeafIcon()
    {
	    return UIManager.getIcon("Tree.leafIcon");
    }

    /**
      * Sets the icon used to represent non-leaf nodes that are expanded.
      */
    public void setOpenIcon(Icon newIcon)
    {
	    openIcon = newIcon;
    }

    /**
      * Returns the icon used to represent non-leaf nodes that are expanded.
      */
    public Icon getOpenIcon()
    {
	    return openIcon;
    }

    /**
      * Sets the icon used to represent non-leaf nodes that are not expanded.
      */
    public void setClosedIcon(Icon newIcon)
    {
	    closedIcon = newIcon;
    }

    /**
      * Returns the icon used to represent non-leaf nodes that are not
      * expanded.
      */
    public Icon getClosedIcon()
    {
	    return closedIcon;
    }

    /**
      * Sets the icon used to represent leaf nodes.
      */
    public void setLeafIcon(Icon newIcon)
    {
	    leafIcon = newIcon;
    }

    /**
      * Returns the icon used to represent leaf nodes.
      */
    public Icon getLeafIcon()
    {
	    return leafIcon;
    }

    /**
      * Sets the color the text is drawn with when the node is selected.
      */
    public void setTextSelectionColor(Color newColor)
    {
	    textSelectionColor = newColor;
    }

    /**
      * Returns the color the text is drawn with when the node is selected.
      */
    public Color getTextSelectionColor()
    {
	    return textSelectionColor;
    }

    /**
      * Sets the color the text is drawn with when the node isn't selected.
      */
    public void setTextNonSelectionColor(Color newColor)
    {
	    textNonSelectionColor = newColor;
    }

    /**
      * Returns the color the text is drawn with when the node isn't selected.
      */
    public Color getTextNonSelectionColor()
    {
	    return textNonSelectionColor;
    }

    /**
      * Sets the color to use for the background if node is selected.
      */
    public void setBackgroundSelectionColor(Color newColor)
    {
	    backgroundSelectionColor = newColor;
    }


    /**
      * Returns the color to use for the background if node is selected.
      */
    public Color getBackgroundSelectionColor()
    {
	    return backgroundSelectionColor;
    }

    /**
      * Sets the background color to be used for non selected nodes.
      */
    public void setBackgroundNonSelectionColor(Color newColor)
    {
	    backgroundNonSelectionColor = newColor;
    }

    /**
      * Returns the background color to be used for non selected nodes.
      */
    public Color getBackgroundNonSelectionColor()
    {
	    return backgroundNonSelectionColor;
    }

    /**
      * Sets the color to use for the border.
      */
    public void setBorderSelectionColor(Color newColor)
    {
	    borderSelectionColor = newColor;
    }

    /**
      * Returns the color the border is drawn.
      */
    public Color getBorderSelectionColor()
    {
	    return borderSelectionColor;
    }

    /**
     * Subclassed to map <code>FontUIResource</code>s to null. If
     * <code>font</code> is null, or a <code>FontUIResource</code>, this
     * has the effect of letting the font of the JTree show
     * through. On the other hand, if <code>font</code> is non-null, and not
     * a <code>FontUIResource</code>, the font becomes <code>font</code>.
     */
    public void setFont(Font font)
    {
        if(font instanceof FontUIResource)
            font = null;
        super.setFont(font);
    }

    /**
     * Gets the font of this component.
     * @return this component's font; if a font has not been set
     * for this component, the font of its parent is returned
     */
    public Font getFont()
    {
        Font font = super.getFont();

        if (font == null && tree != null)
        {
            // Strive to return a non-null value, otherwise the html support
            // will typically pick up the wrong font in certain situations.
            font = tree.getFont();
        }
        return font;
    }

    /**
     * Subclassed to map <code>ColorUIResource</code>s to null. If
     * <code>color</code> is null, or a <code>ColorUIResource</code>, this
     * has the effect of letting the background color of the JTree show
     * through. On the other hand, if <code>color</code> is non-null, and not
     * a <code>ColorUIResource</code>, the background becomes
     * <code>color</code>.
     */
    public void setBackground(Color color)
    {
        if(color instanceof ColorUIResource)
            color = null;
        super.setBackground(color);
    }

    /**
      * Configures the renderer based on the passed in components.
      * The value is set from messaging the tree with
      * <code>convertValueToText</code>, which ultimately invokes
      * <code>toString</code> on <code>value</code>.
      * The foreground color is set based on the selection and the icon
      * is set based on on leaf and expanded.
      */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf, int row,
						  boolean hasFocus)
    {

        String stringValue = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

        this.tree = tree;
        this.hasFocus = hasFocus;

        setText(stringValue);
        if(sel)
            setForeground(getTextSelectionColor());
        else
            setForeground(getTextNonSelectionColor());

        // There needs to be a way to specify disabled icons.
        if (!tree.isEnabled())
        {
            setEnabled(false);
            if (leaf)
                setDisabledIcon(getLeafIcon());
            else
            if (expanded)
                setDisabledIcon(getOpenIcon());
            else
                setDisabledIcon(getClosedIcon());
        }
        else
        {
            setEnabled(true);
            if (leaf)
                setIcon(getLeafIcon());
            else
            if (expanded)
                setIcon(getOpenIcon());
            else
                setIcon(getClosedIcon());
        }
        setComponentOrientation(tree.getComponentOrientation());

        selected = sel;
        return this;
    }

    /**
     * Paint the component.
     *
     * @param g The graphics resource used to paint the component
     */
    public void paint(Graphics g)
    {
	    Color bColor;
        int imageOffset = -1;
        boolean leftToRight = getComponentOrientation().isLeftToRight();

        if (selected && hasFocus)
	        bColor = getBackgroundSelectionColor();
        else
        {
	        bColor = getBackgroundNonSelectionColor();
	        if (bColor == null)
		        bColor = getBackground();
	    }

	    if (bColor != null)
        {
	        imageOffset = getLabelStart();
	        g.setColor(bColor);
	        if (leftToRight)
	            g.fillRect(imageOffset, 0, getWidth() - imageOffset, getHeight());
	        else
	            g.fillRect(0, 0, getWidth() - imageOffset, getHeight());
	    }

	    if (selected || hasFocus)
        {
	        if (drawsFocusBorderAroundIcon)
		        imageOffset = 0;
	        else
            if (imageOffset == -1)
		        imageOffset = getLabelStart();

	        if (leftToRight)
                LipstikBorderFactory.paintFocusBorder(g, imageOffset, 0, getWidth() - imageOffset, getHeight());
	        else
                LipstikBorderFactory.paintFocusBorder(g, 0, 0, getWidth() - imageOffset, getHeight());
        }
	    super.paint(g);
    }


    private int getLabelStart()
    {
	    Icon currentI = getIcon();
	    if  (currentI != null && getText() != null)
	        return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 2);
	    return 0;
    }

    /**
     * Overrides <code>JComponent.getPreferredSize</code> to
     * return slightly wider preferred size value.
     */
    public Dimension getPreferredSize()
    {
	    Dimension retDimension = super.getPreferredSize();
    	if (retDimension != null)
	        retDimension = new Dimension(retDimension.width + 3, retDimension.height);
	    return retDimension;
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void validate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void invalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void revalidate() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(long tm, int x, int y, int width, int height) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void repaint(Rectangle r) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    *
    * @since 1.5
    */
    public void repaint() {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
        // Strings get interned...
        if (propertyName.equals("text"))
            super.firePropertyChange(propertyName, oldValue, newValue);
    }

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, char oldValue, char newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, short oldValue, short newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, long oldValue, long newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, float oldValue, float newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, double oldValue, double newValue) {}

   /**
    * Overridden for performance reasons.
    * See the <a href="#override">Implementation Note</a>
    * for more information.
    */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
}
