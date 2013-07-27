/*
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009 Shlomy Reinstein
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package buffertabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.jEdit;

@SuppressWarnings("serial")
class BufferTabComponent extends JPanel
{
	private static CloseIcon icon = new CloseIcon();
	private static Dimension iconDimension =
		new Dimension(icon.getIconWidth(), icon.getIconHeight());
	private BufferTabs pane;
	
	BufferTabComponent(BufferTabs bufferTabs) {
		super(new FlowLayout(FlowLayout.CENTER, 5, 0));
		pane = bufferTabs;
		setOpaque(false);
		JLabel l = new BufferTabLabel(this);
		add(l);
		JLabel close = new JLabel(icon);
		close.setPreferredSize(iconDimension);
		close.setForeground(Color.BLACK);
		add(close);
		close.addMouseListener(new BufferTabCloseButtonListener(this));
	}
	
	private static class CloseIcon implements Icon {
		private static final int width = 9;
		private static final int height = 11;
		private static final int top = 3;
		public void paintIcon(Component c, Graphics g, int x, int y) {
			g.drawLine(0, top, width - 2, height - 1);
			g.drawLine(1, top, width - 1, height - 1);
			g.drawLine(width - 1, top, 1, height - 1);
			g.drawLine(width - 2, top, 0, height - 1);
		}
		public int getIconWidth() {
			return width;
		}
		public int getIconHeight() {
			return height;
		}
	}
	
	private class BufferTabCloseButtonListener extends MouseAdapter {
		
		private BufferTabComponent component;
		
		BufferTabCloseButtonListener(BufferTabComponent component) {
			this.component = component;
		}
		
		@Override
		public void mouseEntered(MouseEvent e) {
			JLabel close = (JLabel)e.getSource();
			close.setForeground(Color.RED);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JLabel close = (JLabel)e.getSource();
			close.setForeground(Color.BLACK);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int index = pane.indexOfTabComponent(component);
			if (index < 0)
				return;
			EditPane editPane = pane.getEditPane();
			jEdit.closeBuffer(editPane,
				editPane.getBufferSet().getBuffer(index));
		}
	}
	
	private class BufferTabLabel extends JLabel {
		
		private BufferTabComponent component;
		
		BufferTabLabel(BufferTabComponent component) {
			this.component = component;
		}
		
		@Override
		public Icon getIcon() {
			int index = pane.indexOfTabComponent(component);
			if (index < 0)
				return null;
			return pane.getIconAt(index);
		}
		@Override
		public String getText() {
			int index = pane.indexOfTabComponent(component);
			if (index < 0)
				return null;
			return pane.getTitleAt(index);
		}
	}

}