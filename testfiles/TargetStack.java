/*
 * TagStack.java - part of the TagsPlugin plugin.
 *
 * Copyright (C) 2003 Ollie Rutherfurd (oliver@rutherfurd.net)
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
 *
 */
/*
 * This file originates from the Tags Plugin version 2.0.1
 * whose copyright and licensing is seen above.
 * The original file was modified to become the derived work you see here
 * in accordance with Section 2 of the Terms and Conditions of the GPL v2.
 *
 * The derived work is called the CscopeFinder Plugin and is
 * Copyright 2006 Dean Hall.
 *
 * 2006/08/09
 */

package cscopefinder;

//{{{ imports
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * Target stack navigation window.
 */
public class TargetStack
	extends JPanel
	implements DefaultFocusComponent
{

	//{{{ TargetStack constructor
	public TargetStack(View view)
	{
		super(new BorderLayout());

		this.view = view;
		listModel = CscopeFinderPlugin.getTagStack(view);
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new PositionCellRenderer());
		list.addMouseListener(new MouseHandler());
		add(new JScrollPane(list),BorderLayout.CENTER);
	} //}}}

	//{{{ focusOnDefaultComponent() method
	public void focusOnDefaultComponent()
	{
		list.requestFocus();
	} //}}}

	//{{{ private members
	private View view;
	private JList list;
	private TargetStackModel listModel;
	private JPopupMenu popupMenu;
	private JMenuItem goTo = null;
	private JMenuItem remove = null;
	private JMenuItem pop = null;
	private JMenuItem clear = null;
	//}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			int index = list.locationToIndex(new Point(evt.getX(),
													evt.getY()));
			if(index > -1 && index != list.getSelectedIndex())
				list.setSelectedIndex(index);

			if(GUIUtilities.isPopupTrigger(evt))
			{
				showPopupMenu(evt);
			}
			else
			{
				if(evt.getClickCount() > 1)
				{
					TargetStackPosition pos = (TargetStackPosition)
											list.getSelectedValue();
					if(pos != null)
						pos.goTo(view);
					view.requestFocus();
				}
			}
		}
	} //}}}

	//{{{ showPopupMenu() method
	private void showPopupMenu(MouseEvent evt)
	{
		if(popupMenu == null)
		{
			popupMenu = new JPopupMenu();
			goTo = popupMenu.add(new GoToAction());
			remove = popupMenu.add(new RemoveAction());
			pop = popupMenu.add(new PopAction());
			clear = popupMenu.add(new ClearAction());
		}

		goTo.setEnabled(list.getSelectedValue() != null);
		remove.setEnabled(listModel.size() > 0);
		pop.setEnabled(listModel.size() > 0);
		clear.setEnabled(listModel.size() > 0);

		GUIUtilities.showPopupMenu(popupMenu, evt.getComponent(),
			evt.getX(), evt.getY());
		evt.consume();
	} //}}}

	//{{{ ClearAction class
	class ClearAction extends AbstractAction
	{
		public ClearAction()
		{
			super(jEdit.getProperty("cscopefinder.stack-clear"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			listModel.clear();
		}
	} //}}}

	//{{{ GoToAction class
	class GoToAction extends AbstractAction
	{
		public GoToAction()
		{
			super(jEdit.getProperty("cscopefinder.stack-goto"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			TargetStackPosition pos = null;
			pos = (TargetStackPosition)list.getSelectedValue();
			pos.goTo(view);
		}
	} //}}}

	//{{{ PopAction class
	class PopAction extends AbstractAction
	{
		public PopAction()
		{
			super(jEdit.getProperty("cscopefinder.stack-pop"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			CscopeFinderPlugin.popPosition(view);
		}
	} //}}}

	//{{{ RemoveAction class
	class RemoveAction extends AbstractAction
	{
		public RemoveAction()
		{
			super(jEdit.getProperty("cscopefinder.stack-remove"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			TargetStackPosition pos = null;
			pos = (TargetStackPosition)list.getSelectedValue();
			if(pos != null)
				listModel.removeElement(pos);
			else
				Toolkit.getDefaultToolkit().beep();
		}
	} //}}}

}


class PositionCellRenderer
	extends JPanel
	implements ListCellRenderer
{
	// XXX should be able to decide whether to have
	//		before & after lines

	//{{{ PositionCellRenderer constructor
	PositionCellRenderer()
	{
		super();
		setBorder(new EtchedBorder());

		lineNumber = new JLabel();
		lineBefore = new JLabel();
		line = new JLabel();
		lineAfter = new JLabel();
		file = new JLabel();
		directory = new JLabel();

		// XXX use props
		Font plain = new Font("Monospaced", Font.PLAIN, 12);
		Font bold = new Font("Monospaced", Font.BOLD, 12);

		lineNumber.setFont(plain);
		file.setFont(plain);
		directory.setFont(plain);
		lineBefore.setFont(plain);
		line.setFont(bold);
		lineAfter.setFont(plain);

		filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		filePanel.add(lineNumber);
		filePanel.add(file);
		filePanel.add(directory);

		linesPanel = new JPanel(new GridLayout(3,1));
		linesPanel.add(lineBefore);
		linesPanel.add(line);
		linesPanel.add(lineAfter);

		setLayout(new BorderLayout());
		add(BorderLayout.NORTH,filePanel);
		add(BorderLayout.CENTER,linesPanel);
	} //}}}

	//{{{ getListCellRendererComponent() method
	public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus)
	{
		TargetStackPosition p = (TargetStackPosition)value;
		StringBuffer b = new StringBuffer("" + p.getLineNumber());
		// hackish way to add a fixed number of spaces before filename
		while(b.length() < 6)
			b.append(" ");
		lineNumber.setText(b.toString());
		file.setText(p.getName());
		directory.setText(" (" + p.getDirectory() + ")");
		lineBefore.setText(p.getLineBefore() + " ");
		line.setText(p.getLine() + " ");
		lineAfter.setText(p.getLineAfter() + " ");

		Color background = (isSelected ? list.getSelectionBackground()
							: list.getBackground());
		Color foreground = (isSelected ? list.getSelectionForeground()
							: list.getForeground());

		setBackground(background);
		filePanel.setBackground(background);
		lineNumber.setBackground(background);
		file.setBackground(background);
		directory.setBackground(background);
		linesPanel.setBackground(background);
		lineBefore.setBackground(background);
		line.setBackground(background);
		lineAfter.setBackground(background);

		setForeground(foreground);
		filePanel.setForeground(foreground);
		lineNumber.setForeground(Color.BLUE);
		file.setForeground(Color.BLUE);
		directory.setForeground(Color.BLUE);
		linesPanel.setForeground(foreground);
		lineBefore.setForeground(foreground);
		line.setForeground(foreground);
		lineAfter.setForeground(foreground);

		return this;
	} //}}}

	//{{{ private members
	private JPanel filePanel;
	private JPanel linesPanel;
	private JLabel lineNumber;
	private JLabel file;
	private JLabel directory;
	private JLabel lineBefore;
	private JLabel line;
	private JLabel lineAfter;
	//}}}
}
