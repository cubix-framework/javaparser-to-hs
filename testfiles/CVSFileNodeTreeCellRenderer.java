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

package gruntspud.ui.view;

import gruntspud.CVSFileNode;
import gruntspud.CVSFileTypeUtil;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.style.TextStyle;
import gruntspud.ui.UIUtil;
import gruntspud.ui.icons.CompoundIcon;
import gruntspud.ui.icons.OverlayIcon;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.netbeans.lib.cvsclient.admin.Entry;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileNodeTreeCellRenderer
    extends DefaultTreeCellRenderer {
    private JTree tree;
    private boolean showSubstTypes;
    private boolean nonExistant;
    private boolean unWriteable;
    private boolean underlined;
    private String textMask;
    private CVSFileNode node;
    private GruntspudContext context;
    private boolean highlight;

    public CVSFileNodeTreeCellRenderer(GruntspudContext context) {
        super();
        this.context = context;
        Insets i = context.getHost().getFileRendererInsets();
        setBorder(BorderFactory.createEmptyBorder(i.top, i.left, i.bottom,
                                                  i.right));
    }

    /**
     * DOCUMENT ME!
     *
     * @param showSubstTypes DOCUMENT ME!
     */
    public void setShowSubstTypes(boolean showSubstTypes) {
        this.showSubstTypes = showSubstTypes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param showSubstTypes DOCUMENT ME!
     */
    public void setHighlight(boolean highlight) {
    	this.highlight = highlight;
    }

    /**
     * DOCUMENT ME!
     *
     * @param textMask DOCUMENT ME!
     */
    public void setTextMask(String textMask) {
        this.textMask = textMask;
    }

    /**
     *  Gets the treeCellRendererComponent attribute of the
     *  CVSFileNodeTreeCellRenderer object
     *
     *@param  tree      Description of the Parameter
     *@param  value     Description of the Parameter
     *@param  sel       Description of the Parameter
     *@param  expanded  Description of the Parameter
     *@param  leaf      Description of the Parameter
     *@param  row       Description of the Parameter
     *@param  hasFocus  Description of the Parameter
     *@return           The treeCellRendererComponent value
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                                           row, hasFocus);
        this.tree = tree;

        node = (CVSFileNode) value;

		if(node == null)
		{
            setOpaque(false);
			setText("<null>");
            setFont(tree.getFont());
			nonExistant = true;
			unWriteable = false;
		}
		else
		{
	        Entry entry = node.getEntry();
            TextStyle style = node.getStyle();
            if(sel) {
                setOpaque(false);
            }
            else {

                setForeground(style == null || style.getForeground() == null ? tree.getForeground() :
                              style.getForeground());
                if (style != null && style.getBackground() != null) {
                    setOpaque(true);
                    setBackground(style.getBackground());
                }
                else {
                    setOpaque(false);
                    setBackground(tree.getBackground());
                }
            }
            if(style != null) {
                setFont(tree.getFont().deriveFont( (style.isBold() ? Font.BOLD : 0) +
                    (style.isItalic() ? Font.ITALIC : 0)));
            }
            else {
                setFont(tree.getFont());
            }

	        underlined = node.isOpen();
	    	setText(node.getFormattedText(textMask));
	    	setToolTipText(CVSFileTypeUtil.createToolTipTextForNode(node));
	    	nonExistant = !node.getFile().exists();
	    	unWriteable = !node.getFile().canWrite();
			Icon i = null;
			if (showSubstTypes) {
				
				i = new CompoundIcon(node.getIcon(expanded),
										 (node.getCVSSubstType() == null) ?
										 UIUtil.EMPTY_SMALL_ICON
										 : node.getCVSSubstType()
										 .getIcon());


				if(i != null) {
					switch(node.getLineEndings()) {
						case CVSFileNode.WINDOWS_LINE_ENDINGS:
							i = new OverlayIcon(UIUtil.getCachedIcon(
								Constants.ICON_WINDOWS_LINE_ENDINGS), i, SwingConstants.CENTER);
							break;
						case CVSFileNode.UNIX_LINE_ENDINGS:
							i = new OverlayIcon(UIUtil.getCachedIcon(
								Constants.ICON_UNIX_LINE_ENDINGS), i, SwingConstants.CENTER);
							break;  
					}
				}
			}
			else {
				i = node.getIcon(expanded);
			}
			setIcon(i);
		}

        return this;
    }

    /**
     * DOCUMENT ME!
     *
     * @param g DOCUMENT ME!
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (underlined) {
            FontMetrics fm = getFontMetrics(getFont());
            int x, y;
            if (getIcon() == null) {
                x = 0;
                y = fm.getAscent() + 2;
            }
            else {
                x = getIcon().getIconWidth() + getIconTextGap();
                y = Math.max(fm.getAscent() + 2, 16);
            }
            g.setColor(getForeground());
            g.drawLine(x, y, x + fm.stringWidth(getText()), y);
        }

        if (highlight && ( nonExistant || unWriteable) ) {
            g.setColor(nonExistant ? UIUtil.TRANSPARENT_BLUE
                       : UIUtil.TRANSPARENT_RED);

            Insets i = (getBorder() == null) ? UIUtil.EMPTY_INSETS
                : getBorder().getBorderInsets(this);
            int y = getSize().height / 2;
            int iw = getIcon().getIconWidth();
            g.drawRect(iw + 1, y, getSize().width - iw - i.right - i.left + 2,
                       1);
        }
    }
}
