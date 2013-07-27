/***************************************************************
*  Copyright notice
*
*  (c) 2005,2006 Neil Bertram (neil@tasmanstudios.co.nz)
*  All rights reserved
*
*  This plugin is part of the Typo3 project. The Typo3 project is
*  free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  The GNU General Public License can be found at
*  http://www.gnu.org/copyleft/gpl.html.
*  A copy is found in the textfile GPL.txt
*
*
*  This plugin is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  This copyright notice MUST APPEAR in all copies of the source!
***************************************************************/
/**
 * $Id: T3TreeRenderer.java 10413 2006-02-26 02:52:20Z n31l $
 * 
 * This renderer represents page nodes in the TypoScriptSiteBrowser
 * dockable's JTree. It sets appropriate icons (loaded from the icons
 * directory) and sets template titles bold.
 *
 * @author      Neil Bertram <neil@tasmanstudios.co.nz>
 */
package typoscript;

import java.awt.Component;
import java.awt.Font;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Represents the visual appearance of nodes in the site browser tree
 * eg. template nodes are shown in bold, and icons represent certain "doktypes"
 * 
 * Icons courtesy of TYPO3 crystal skin and Crystal SVG from KDE icon set
 */
public class T3TreeRenderer extends DefaultTreeCellRenderer {
	private Icon pageIcon;
	private Icon sysfolderIcon;
	private Icon shortcutIcon;
	private Icon rootIcon;
	private Icon unknownIcon;
	private Icon templateIcon;
	private Icon loadingIcon;
	
	public T3TreeRenderer() {
		super();
		
		pageIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/page.png"));
		sysfolderIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/sysfolder.png"));
		shortcutIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/shortcut.png"));
		rootIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/root.png"));
		unknownIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/unknown.png"));
		templateIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/template.png"));
		loadingIcon = new ImageIcon(TypoScriptPlugin.class.getResource("/typoscript/icons/wait.png"));
	}
	
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
		// If this is the (invisible) root node or one of the "Tree loading" nodes, just set the clock icon
		if (!(value instanceof T3PageNode)) {
			setIcon(loadingIcon);
			return this;
		}
		T3PageNode pageNode = (T3PageNode)value;
		if (pageNode.hasTemplate()) {
			Font boldFont = new Font("Default", Font.BOLD, 12);
			this.setFont(boldFont);
		} else {
			Font normalFont = new Font("Default", 0, 12);
			this.setFont(normalFont);
		}
		
		// For any page that's not a root node, set the tooltip to its UID
		// for a root, display its URL
		// for a template, display the template UID
		if (pageNode.getType() > 0) {
			// standard page
			setToolTipText("Page UID " + String.valueOf(pageNode.getUid()));
		} else if (pageNode.getType() == -5) {
			// template
			setToolTipText("Template UID " + String.valueOf(pageNode.getTemplateUID()));
		} else {
			// Site root
			try {
				setToolTipText(pageNode.getSite().getUrlBase().toString());
			} catch (Exception e) {
				// Ignore
			}
		}
		
		// Set the icon
		switch(pageNode.getType()) {
		case -1: setIcon(rootIcon); break;		// Root of a site (globe icon)
		case 1: setIcon(pageIcon); break;		// a standard page
		case 4: setIcon(shortcutIcon); break;	// a shortcut to a page
		case 254: setIcon(sysfolderIcon); break;// a sysfolder
		case -5: setIcon(templateIcon); break;	// a template (extension template anyway)
		default: setIcon(unknownIcon); break;	// something I haven't bothered to find an icon for (lots of types)
		}
		
		return this;
	}
}