/* $Id: TreeRenderer.java 5859 2002-10-21 15:15:50Z ensonic $
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
package projectviewer.tree;

import javax.swing.tree.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.*;
import java.net.URL;

import projectviewer.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.util.Log;

/** Used by the jTree to change the background font for subscribed projects.
 *
 * @author     <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
 */
public final class TreeRenderer extends DefaultTreeCellRenderer {

	private static Font leafFont = UIManager.getFont("Tree.font");
	private static Font folderFont = leafFont.deriveFont(Font.BOLD);

	private static Icon fileClosedIcon = GUIUtilities.loadIcon("File.png");
	private static Icon fileOpenedIcon = GUIUtilities.loadIcon("OpenFile.png");
	private static Icon dirClosedIcon = GUIUtilities.loadIcon("Folder.png");
	private static Icon dirOpenedIcon = GUIUtilities.loadIcon("OpenFolder.png");
	private static Icon projectIcon = GUIUtilities.loadIcon("DriveSmall.png");

	private static Color treeSelectionForeground = UIManager.getColor("Tree.selectionForeground");
	private static Color treeNoSelectionForeground = UIManager.getColor("Tree.textForeground");
	private static Color treeSelectionBackground = UIManager.getColor("Tree.selectionBackground");
	private static Color treeNoSelectionBackground = UIManager.getColor("Tree.textBackground");
	
	
	private boolean underlined;

	public TreeRenderer() {
		//Log.log(Log.DEBUG,this,"TreeRenderer()");
		/*
		 * Font f = UIManager.getFont("Tree.font");
		 * normalFont = new Font(f.getName(), Font.PLAIN, f.getSize());
		 * openedFont = new Font(f.getName(), Font.BOLD , f.getSize());
		 * treeSelectionForeground = UIManager.getColor("Tree.selectionForeground");
		 * treeNoSelectionForeground = UIManager.getColor("Tree.textForeground");
		 * treeSelectionBackground = UIManager.getColor("Tree.selectionBackground");
		 * treeNoSelectionBackground = UIManager.getColor("Tree.textBackground");
		 * fileIcon      = UIManager.getIcon("Tree.leafIcon");
		 * dirClosedIcon = UIManager.getIcon("Tree.closedIcon");
		 * dirOpenIcon   = UIManager.getIcon("Tree.openIcon");
		 * URL url = getClass().getResource("/projectviewer/icons/Project.gif");
		 * projectIcon = new ImageIcon(url);
		 */
		setBorder(new EmptyBorder(1,0,1,0));
		//setOpaque(true);
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded,
			boolean leaf, int row,
			boolean focus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, focus);

		//Log.log(Log.DEBUG,this,"getTreeCellRendererComponent() : "+value.toString());

		//-- change colors to highlight selected items
		if(sel) {
			setBackground(treeSelectionBackground);
			setForeground(treeSelectionForeground);
		}
		else {
			setBackground(treeNoSelectionBackground);
			setForeground(treeNoSelectionForeground);
		}

		//-- set font
		setFont(leaf ? leafFont : folderFont);
		
		//-- set foreground color
		if(value instanceof ProjectFile) {
			//-/treeCellRenderer.setFont(getFontForFile((ProjectFile)value));
			underlined=((ProjectFile)value).isOpened();
			if(!sel) {
				setForeground(VFS.getDefaultColorFor(value.toString()));
			}
		}
		else {
			underlined=false;
			//-/treeCellRenderer.setFont(leafFont);
		}

		//-- set the icons for these entries
		if(value instanceof Project) {
			//Log.log(Log.DEBUG,this," setIcon(projectIcon)="+projectIcon);
			setIcon(projectIcon);
		}
		else if(value instanceof ProjectDirectory) {
			if(expanded) {
				//Log.log(Log.DEBUG,this," setIcon(dirOpenedIcon)="+dirOpenedIcon);
				setIcon(dirOpenedIcon);
			}
			else {
				//Log.log(Log.DEBUG,this," setIcon(dirClosedIcon)="+dirClosedIcon);
				setIcon(dirClosedIcon);
			}
		}
		else if(value instanceof ProjectFile) {
			if(((ProjectFile)value).isOpened()) {
				//Log.log(Log.DEBUG,this," setIcon(fileOpenedIcon)="+fileOpenedIcon);
				setIcon(fileOpenedIcon);
			}
			else {
				//Log.log(Log.DEBUG,this," setIcon(fileClosedIcon)="+fileClosedIcon);
				setIcon(fileClosedIcon);
			}
		}
		else {	// should not happen at all
			//Log.log(Log.DEBUG,this," setIcon() for unknow object !!");
			if(leaf) {
				if(((ProjectFile)value).isOpened()) {
					setIcon(fileOpenedIcon);
				}
				else {
					setIcon(fileClosedIcon);
				}
			}
			else if(expanded) {
				setIcon(dirOpenedIcon);
			}
			else {
				setIcon(dirClosedIcon);
			}
		}

		setText(value.toString());
		//Log.log(Log.DEBUG,this,"  getTreeCellRendererComponent() : tree.isEnabled()="+tree.isEnabled());
		//-- as the tree is never disabled, we can save this call
		//treeCellRenderer.setEnabled(tree.isEnabled());
		return this;
		//return treeCellRenderer;
	}

	public void paintComponent(Graphics g) {
		//Log.log(Log.DEBUG,this,"paintComponent() : "+underlined+" text="+getText());
		if(underlined) {
			FontMetrics fm = getFontMetrics(getFont());
			int x, y;
			if(getIcon() == null) {
				//Log.log(Log.DEBUG,this,"  no Icon");
				x = 0;
				y = fm.getAscent() + 2;
			}
			else {
				//Log.log(Log.DEBUG,this,"  with Icon : w="+getIcon().getIconWidth()+" h="+getIcon().getIconHeight());
				x = getIcon().getIconWidth() + getIconTextGap();
				y = Math.max(fm.getAscent() + 2,16);
			}
			//Log.log(Log.DEBUG,this,"  coords : xs="+x+" ys="+y+" xe="+(x+fm.stringWidth(getText())));
			g.setColor(getForeground());
			g.drawLine(x,y,x + fm.stringWidth(getText()),y);
		}

		super.paintComponent(g);
	}

	/** Returns the font to use for the specified file.
	 *
	 * @param  file  Description of Parameter
	 * @return       The fontForFile value
	 */
	//protected Font getFontForFile(ProjectFile file) {
	//	return file.isOpened() ? openedFont : normalFont;
	//}
}

