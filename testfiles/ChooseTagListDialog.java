/*
 * ChooseTagListDialog.java
 * Copyright (c) 2001, 2002 Kenrick Drew, Slava Pestov
 *
 * This file is part of TagsPlugin
 *
 * TagsPlugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * TagsPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: ChooseTagListDialog.java 7606 2004-11-07 15:52:36Z orutherfurd $
 */

package tags;

//{{{ imports
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.awt.Toolkit;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import org.gjt.sp.util.Log;
//}}}

class ChooseTagListDialog extends JDialog
{
	//{{{ private declarations
	protected ChooseTagList chooseTagList;
	protected JCheckBox keepDialogCheckBox;
	protected JButton cancel;
	protected View view;
	protected boolean canceled = false;
	protected boolean openNewView;
	//}}}

	//{{{ ChooseTagListDialog() constructor
	public ChooseTagListDialog(View view, Vector tagLines, boolean newView)
	{
		super(view, jEdit.getProperty("tag-collision-dlg.title"), false);

		this.view = view;

		getContentPane().setLayout(new BorderLayout());

		// label
		JLabel label = new JLabel(jEdit.getProperty("tag-collision-dlg.label"));

		// collision list
		chooseTagList = new ChooseTagList(tagLines);
		JScrollPane scrollPane = new JScrollPane(chooseTagList,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		chooseTagList.addKeyListener(keyListener);
		chooseTagList.addMouseListener(mouseListener);
		JPanel contentPanel = new JPanel(new BorderLayout(0,5));
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
		contentPanel.add(label, BorderLayout.NORTH);
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		// keep dialog
		keepDialogCheckBox = new JCheckBox(
						jEdit.getProperty("tags.enter-tag-dlg.keep-dialog.label"));
		keepDialogCheckBox.addActionListener(keepDialogListener);
		keepDialogCheckBox.setMnemonic(KeyEvent.VK_K);
		contentPanel.add(keepDialogCheckBox, BorderLayout.SOUTH);

		// OK/Cancel/Close buttons
		JButton ok = new JButton(jEdit.getProperty("common.ok"));
		getRootPane().setDefaultButton(ok);
		ok.addActionListener(okButtonListener);
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(cancelButtonListener);

		JPanel buttonPanelFlow = new JPanel(new FlowLayout());
		JPanel buttonPanelGrid = new JPanel(new GridLayout(1,0,5,0));
		buttonPanelFlow.add(buttonPanelGrid);
		buttonPanelGrid.add(ok);
		buttonPanelGrid.add(cancel);
		getContentPane().add(buttonPanelFlow, BorderLayout.SOUTH);

		// dialog setup
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		addKeyListener(keyListener);

		// show
		showDialog();

		scrollPane = null;
		contentPanel = null;
		ok = null;
		buttonPanelFlow = null;
		buttonPanelGrid = null;
	} //}}}

	//{{{ setDialogPosition()
	private void setDialogPosition()
	{
		if(view == null)
			return;
		Point mousePointer = TagsPlugin.getMousePointer();
		if(mousePointer != null && TagsPlugin.getPlaceDialogsUnderCursor())
		{
			Point p = new Point(mousePointer);
			Dimension dialog = this.getSize();
			p.x = p.x - (dialog.width / 2);
			p.y = p.y - (dialog.height / 2);
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			if(p.x + dialog.width > screen.width)
				p.x = screen.width - dialog.width;
			if(p.x < 0)
				p.x = 0;	// keep on screen
			if(p.y + dialog.height > screen.height)
				p.y = screen.height - dialog.height;
			if(p.y < 0)
				p.y = 0;	// keep on screen
			this.setLocation(p);
		}
		else
			setLocationRelativeTo(view);
	} //}}}

	//{{{ showDialog() method
	protected void showDialog()
	{
		pack();
		setDialogPosition();
		show();
		GUIUtilities.requestFocus(this, chooseTagList);
	} //}}}

	//{{{ followSelectedTag() method
	protected void followSelectedTag()
	{
		TagLine tagLine = (TagLine)chooseTagList.getSelectedValue();
		TagsPlugin.goToTagLine(view, tagLine, openNewView, tagLine.getTag());
	} //}}}

	//{{{ keepDialogListener
	protected ActionListener keepDialogListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			if (keepDialogCheckBox.isSelected())
				cancel.setText(jEdit.getProperty("common.close"));
			else
				cancel.setText(jEdit.getProperty("common.cancel"));
			chooseTagList.requestFocus();
		}
	}; //}}}

	//{{{ okButtonListener
	protected ActionListener okButtonListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			followSelectedTag();
			if (!keepDialogCheckBox.isSelected())
				dispose();
			/*
			else
				chooseTagList.requestFocus();
			*/
		}
	}; //}}}

	//{{{ cancelButtonListener
	protected ActionListener cancelButtonListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			canceled = true;
			dispose();
		}
	}; //}}}

	//{{{ keyListener
	protected KeyListener keyListener = new KeyListener()
	{
		//{{{ keyPressed() method
		public void keyPressed(KeyEvent e)
		{
			e = KeyEventWorkaround.processKeyEvent(e);
			if(e == null)
				return;
			
			switch (e.getKeyCode())
			{
				case KeyEvent.VK_ESCAPE:
					cancelButtonListener.actionPerformed(null);
					break;
				case KeyEvent.VK_UP:
					int selected = chooseTagList.getSelectedIndex();
					if (selected == 0)
						selected = chooseTagList.getModel().getSize() - 1;
					else if (getFocusOwner() == chooseTagList)
						return; // Let JList handle the event
					else
						selected = selected - 1;

					chooseTagList.setSelectedIndex(selected);
					chooseTagList.ensureIndexIsVisible(selected);

					e.consume();
					break;
				case KeyEvent.VK_DOWN:
					selected = chooseTagList.getSelectedIndex();
					if(selected == chooseTagList.getModel().getSize() - 1)
						selected = 0;
					else if(getFocusOwner() == chooseTagList)
						return; // Let JList handle the event
					else
						selected = selected + 1;

					chooseTagList.setSelectedIndex(selected);
					chooseTagList.ensureIndexIsVisible(selected);

					e.consume();
					break;
			}
		} //}}}

		public void keyReleased(KeyEvent e) {}

		///{{{ keyTyped() method
		public void keyTyped(KeyEvent e)
		{
			e = KeyEventWorkaround.processKeyEvent(e);
			if(e == null)
				return;

			switch (e.getKeyChar())
			{
				case KeyEvent.VK_1:
				case KeyEvent.VK_2:
				case KeyEvent.VK_3:
				case KeyEvent.VK_4:
				case KeyEvent.VK_5:
				case KeyEvent.VK_6:
				case KeyEvent.VK_7:
				case KeyEvent.VK_8:
				case KeyEvent.VK_9:
					if (getFocusOwner() != chooseTagList)
						return;

					/* There may actually be more than 9 items in the list, but since
					 * the user would have to scroll to see them either with the mouse
					 * or with the arrow keys, then they can select the item they want
					 * with those means.
					 */
					int selected = Character.getNumericValue(e.getKeyChar()) - 1;
					if (selected >= 0 && selected < chooseTagList.getModel().getSize())
					{
						chooseTagList.setSelectedIndex(selected);
						chooseTagList.ensureIndexIsVisible(selected);
						dispose();
					}
					break;
			}
		} //}}}

	}; //}}}

	//{{{ mouseListener
	protected MouseListener mouseListener = new MouseAdapter()
	{
		//{{{ mouseClicked() method
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				int selected = chooseTagList.locationToIndex(e.getPoint());
				chooseTagList.setSelectedIndex(selected);
				chooseTagList.ensureIndexIsVisible(selected);
				followSelectedTag();
				if (!keepDialogCheckBox.isSelected())
					dispose();
			}
		} //}}}
	}; //}}}

}

// :noTabs=false:tabSize=4:folding=explicit:collapseFolds=1:
