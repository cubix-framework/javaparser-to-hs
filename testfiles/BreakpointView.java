/*
Copyright (C) 2007  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package perl.breakpoints;

import perl.breakpoints.BreakpointList.BreakpointListListener;
import perl.core.Debugger;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;


@SuppressWarnings("serial")
public class BreakpointView extends JPanel {
	static private Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
	private JList list;
	private DefaultListModel model;
	private JButton editButton;
	private JButton deleteButton;
	private JButton gotoButton;
	private JButton addWatchpointButton;
	private int lastSelection = -1;
	
	public BreakpointView() {
		setLayout(new BorderLayout());
		list = new JList();
		list.setCellRenderer(new BreakpointCellRenderer());
		list.addMouseListener(new MouseAdapter()
        {
           public void mousePressed(MouseEvent e)
           {
              int index = list.locationToIndex(e.getPoint());
              if (index != -1 && index == lastSelection) {
                 BreakpointCheckBox bp =
                	 (BreakpointCheckBox)list.getModel().getElementAt(index);
                 bp.setSelected(!bp.isSelected());
                 repaint();
              }
              lastSelection = index;
           }
        });
		add(new JScrollPane(list), BorderLayout.CENTER);
		
		// Create the toolbar
		JToolBar buttons = new JToolBar();
		buttons.setFloatable(false);
		editButton = new JButton("Edit");
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BreakpointCheckBox cb =
					(BreakpointCheckBox) list.getSelectedValue();
				BreakpointEditor ed = new BreakpointEditor(cb.get());
				ed.setVisible(true);
			}
		});
		buttons.add(editButton);
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BreakpointCheckBox cb =
					(BreakpointCheckBox) list.getSelectedValue();
				cb.get().remove();
				model.removeElement(cb);
			}
		});
		buttons.add(deleteButton);
		gotoButton = new JButton("Goto");
		gotoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BreakpointCheckBox cb =
					(BreakpointCheckBox) list.getSelectedValue();
				Breakpoint bp = cb.get();
				Debugger.getInstance().getFrontEnd().goTo(
						bp.getFile(), bp.getLine());
			}
		});
		buttons.add(gotoButton);
		addWatchpointButton = new JButton("Add watchpoint");
		addWatchpointButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				WatchpointEditor ed = new WatchpointEditor();
				ed.setVisible(true);
			}
		});
		buttons.add(addWatchpointButton);
		add(buttons, BorderLayout.NORTH);
		
		model = new DefaultListModel();
		list.setModel(model);
		updateList();
		BreakpointList.getInstance().addListListener(new BreakpointListListener() {
			public void breakpointAdded(Breakpoint bp) {
				updateList();
			}
			public void breakpointRemoved(Breakpoint bp) {
				updateList();
			}
			public void breakpointChanged(Breakpoint bp) {
				updateList();
			}
		});
	}
	
	private void updateList() {
		model.removeAllElements();
		Vector<Breakpoint> brks = BreakpointList.getInstance().getBreakpoints();
		for (int i = 0; i < brks.size(); i++)
		{
			Breakpoint bp = (Breakpoint)brks.get(i);
			model.addElement(new BreakpointCheckBox(bp));
		}
	}
	
	static private class BreakpointCheckBox extends JCheckBox {
		private Breakpoint bp;
		
		public BreakpointCheckBox(Breakpoint bpt) {
			super(bpt.isBreakpoint() ?
					bpt.getFile() + ":" + bpt.getLine() :
					"On " + bpt.getWhat() + " " + bpt.getWhen());
	      	bp = bpt;
	      	setSelected(bp.isEnabled());
    	}

		@Override
		public void setSelected(boolean b) {
			super.setSelected(b);
			bp.setEnabled(b);
		}
		
		public boolean equals(BreakpointCheckBox bp) {
			return (this.bp == bp.bp);
		}
		public Breakpoint get() {
			return bp;
		}
	}
	static private class BreakpointCellRenderer implements ListCellRenderer
	{
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus)
		{
			BreakpointCheckBox cb = (BreakpointCheckBox)value;
			cb.setBackground(isSelected ?
					list.getSelectionBackground() : list.getBackground());
			cb.setForeground(isSelected ?
					list.getSelectionForeground() : list.getForeground());
			cb.setEnabled(list.isEnabled());
			cb.setFont(list.getFont());
			cb.setFocusPainted(false);
			cb.setBorderPainted(true);
			cb.setBorder(isSelected ?
					UIManager.getBorder("List.focusCellHighlightBorder") :
					noFocusBorder);
			return cb;
		}
	}
}