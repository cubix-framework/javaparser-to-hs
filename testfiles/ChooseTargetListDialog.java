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
 * $Id: ChooseTagListDialog.java,v 1.12 2004/11/07 15:52:34 orutherfurd Exp $
 */
/*
 * This file originates from the Tags Plugin version 2.0.1
 * whose copyright and licensing is seen above.
 * The original file was modified to become the derived work you see here
 * in accordance with Section 2 of the Terms and Conditions of the GPL v2.
 *
 * The derived work is called the CscopeFinder Plugin and is
 * Copyright 2006 Dean Hall.
 * Copyright (c) 2006 Alan Ezust
 *
 * 2006/08/09
 */

package cscopefinder;

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

class ChooseTargetListDialog extends JDialog
{
	//{{{ private declarations
	protected ChooseTargetList chooseTagList;
	protected JCheckBox keepDialogCheckBox;
	protected JButton cancel;
	protected View view;
	protected boolean canceled = false;
	protected boolean openNewView;
	//}}}

	//{{{ ChooseTargetListDialog() constructor
	public ChooseTargetListDialog(View view, Vector tagLines, boolean newView)
	{
		super(view, jEdit.getProperty("target-selection-dlg.title"), false);

		this.view = view;

		getContentPane().setLayout(new BorderLayout());

		// label
		JLabel label = new JLabel(jEdit.getProperty("target-selection-dlg.label"));

		// collision list
		chooseTagList = new ChooseTargetList(tagLines);
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
		keepDialogCheckBox = new JCheckBox(jEdit.getProperty(
                                    "target-selection-dlg.keep-dialog.label"));
		keepDialogCheckBox.setSelected(jEdit.getBooleanProperty("cscope.keep-dialog"));
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
		Point mousePointer = CscopeFinderPlugin.getMousePointer();
        setLocationRelativeTo(view);
	} //}}}

	//{{{ showDialog() method
	protected void showDialog()
	{
		pack();
		setDialogPosition();
		setVisible(true);
		GUIUtilities.requestFocus(this, chooseTagList);
	} //}}}

	//{{{ followSelectedTag() method
	protected void followSelectedTag()
	{
		TargetLine tagLine = (TargetLine)chooseTagList.getSelectedValue();
		CscopeFinderPlugin.goToTagLine(view, tagLine, openNewView, tagLine.getTag());
	} //}}}

	//{{{ keepDialogListener
	protected ActionListener keepDialogListener = new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			jEdit.setBooleanProperty("cscope.keep-dialog", keepDialogCheckBox.isSelected());
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
				chooseTargetList.requestFocus();
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
			
			int code = e.getKeyCode();
			int newSelect = -1;
			int numRows = chooseTagList.getVisibleRowCount()-1;
			int selected = chooseTagList.getSelectedIndex();
			switch (code)
			{
				case KeyEvent.VK_ESCAPE:
					cancelButtonListener.actionPerformed(null);
					break;
				case KeyEvent.VK_UP:
					selected = chooseTagList.getSelectedIndex();
					if (selected == 0)
						break;
					else if (getFocusOwner() == chooseTagList)
						return; // Let JList handle the event
					else
						selected = selected - 1;

					chooseTagList.setSelectedIndex(selected);
					chooseTagList.ensureIndexIsVisible(selected);

					e.consume();
					break;
				case KeyEvent.VK_PAGE_UP:
					newSelect = selected - numRows;
					if (newSelect < 0) newSelect = 0;
					chooseTagList.setSelectedIndex(newSelect);
					chooseTagList.ensureIndexIsVisible(newSelect);
					e.consume();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					newSelect = selected + numRows;
					if (newSelect >= chooseTagList.getModel().getSize()) newSelect = chooseTagList.getModel().getSize() - 1; 
					chooseTagList.setSelectedIndex(newSelect);
					chooseTagList.ensureIndexIsVisible(newSelect);
					e.consume();
					break;
					
				case KeyEvent.VK_DOWN:
					selected = chooseTagList.getSelectedIndex();
					if(selected == chooseTagList.getModel().getSize() - 1)
						break;
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
			int newSelect = -1;
			int numRows = chooseTagList.getVisibleRowCount()-1;
			int selected = chooseTagList.getSelectedIndex();
			int size = chooseTagList.getModel().getSize(); 
			switch (e.getKeyChar())
			{
			case KeyEvent.VK_UP:
				selected = chooseTagList.getSelectedIndex();
				if (selected == 0)
					break;
				else if (getFocusOwner() == chooseTagList)
					return; // Let JList handle the event
				else
					selected = selected - 1;

				chooseTagList.setSelectedIndex(selected);
				chooseTagList.ensureIndexIsVisible(selected);

				e.consume();
				break;
			case KeyEvent.VK_PAGE_UP:
				newSelect = selected - numRows;
				if (newSelect < 0) newSelect = 0;
				chooseTagList.setSelectedIndex(newSelect);
				chooseTagList.ensureIndexIsVisible(newSelect);
				e.consume();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				newSelect = selected + numRows;
				if (newSelect >= size) newSelect = size - 1; 
				chooseTagList.setSelectedIndex(newSelect);
				chooseTagList.ensureIndexIsVisible(newSelect);
				e.consume();
				break;
				
			case KeyEvent.VK_DOWN:
				selected = chooseTagList.getSelectedIndex();
				if(selected == size - 1)
					break;
				/* else if(getFocusOwner() == chooseTagList)
					return; // Let JList handle the event */
//				else
					selected = selected + 1;

				chooseTagList.setSelectedIndex(selected);
				chooseTagList.ensureIndexIsVisible(selected);

				e.consume();
				break;

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
					selected = Character.getNumericValue(e.getKeyChar()) - 1;
					if (selected >= 0 && selected < size)
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
				jEdit.setBooleanProperty("cscope.keep-dialog", keepDialogCheckBox.isSelected());
				if (!keepDialogCheckBox.isSelected())
					dispose();
			}
		} //}}}
	}; //}}}

}
