/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
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
package projectviewer.vpt;

//{{{ Imports
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeCellRenderer;

import projectviewer.PVActions;
//}}}

/**
 *	Used by the jTree to underline the file name when it's opened.
 *
 *  @author		<A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 *	@author		Marcelo Vanzin
 *	@version	$Id: VPTCellRenderer.java 15232 2009-05-09 21:56:15Z vanza $
 */
public final class VPTCellRenderer extends DefaultTreeCellRenderer {

	//{{{ Constants
	private final static Font leafFont 	= UIManager.getFont("Tree.font");
	private final static Font folderFont = leafFont.deriveFont(Font.BOLD);

	/** Don't clip the node string if it doesn't fit in the tree. */
	public final static int CLIP_NOCLIP		= 0;
	/** Clip the start of the node string if needed. */
	public final static int CLIP_START		= 1;
	/** Clip the end of the node string if needed. */
	public final static int CLIP_END		= 2;
	//}}}

	//{{{ Private members
	private IconComposer ic;
	private JTree tree;
	private VPTNode node;
	private boolean expanded;
	private boolean selected;
	private boolean useTooltips;
	private int row;
	//}}}

	/**
	 *	Constructs a new renderer that optionally sets the node's tooltip
	 *	to the the VPTNode's path, for openable nodes.
	 */
	VPTCellRenderer(boolean useTooltips)
	{
		setBorder(new EmptyBorder(1,0,1,0));
		this.useTooltips = useTooltips;
	}


	void setIconComposer(IconComposer ic)
	{
		this.ic = ic;
	}


	//{{{ +getTreeCellRendererComponent(JTree, Object, boolean, boolean, boolean, int, boolean) : Component
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded,
			boolean leaf, int row,
			boolean focus) {
		try {
			this.node = (VPTNode) value;
			setFont(node.getAllowsChildren() ? folderFont : leafFont);
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
			setText(node.getName());
			setIcon(VPTProject.projectIcon);
			this.tree = tree;
			this.row = row;
			this.node = node;
			this.expanded = expanded;
			this.selected = sel;
		} catch (ClassCastException cce) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);
			this.node = null;
			setText(value.toString());
		}
		return this;
	} //}}}

	//{{{ +paintComponent(Graphics) : void
	public void paintComponent(Graphics g) {
		if (this.node != null) {
			FontMetrics fm;
			boolean underlined;

			Icon icon = node.getIcon(expanded);
			if (ic != null && (node.isFile() || node.isDirectory())) {
				icon = ic.composeIcon(node, icon);
			}

			setIcon(icon);
			setBackground(node.getBackgroundColor(selected));
			setForeground(node.getForegroundColor(selected));
			underlined = (node.canOpen() && node.isOpened());
			if (useTooltips && node.canOpen()) {
				setToolTipText(node.getNodePath());
			}

			fm = getFontMetrics(getFont());

			//{{{ see if we need to clip the text
			if (node.getClipType() != CLIP_NOCLIP) {
				Rectangle bounds = tree.getRowBounds(row);
				String toShow = getText();

				int maxWidth = tree.getParent().getWidth() - (int) bounds.getX();
				if (getIcon() != null) {
					maxWidth  -= (getIcon().getIconWidth() + getIconTextGap());
				}

				toShow = PVActions.clipText(toShow,
											maxWidth,
											fm,
											node.getClipType() == CLIP_END);
				setText(toShow);
			} //}}}

			// underlines the string if needed
			if (underlined) {
				int x, y;
				y = getHeight() - 3;
				x = (getIcon() == null)
						? 0
						: getIcon().getIconWidth() + getIconTextGap();
				g.setColor(getForeground());
				g.drawLine(x, y, x + fm.stringWidth(getText()), y);
			}
		}

		super.paintComponent(g);
	} //}}}


	/**
	 * Returns the desired row height based on the given node. This is
	 * used by the tree handling code to set a fixed row height for the
	 * trees.
	 *
	 * @param	node	Node on which to base the row height.
	 *
	 * @return The desired row height for the given node.
	 */
	protected int getRowHeight(VPTNode node)
	{
		FontMetrics fm = getFontMetrics(folderFont);
		/* 16 is the size of the tree icons, plus 2 pixels for padding. */
		return Math.max(fm.getHeight(), 16) + 2;
	}

}

