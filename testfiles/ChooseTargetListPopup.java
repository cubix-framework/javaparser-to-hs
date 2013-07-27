/*
 * ChooseTagListPopup.java
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
 * $Id: ChooseTagListPopup.java,v 1.10 2004/11/07 15:52:34 orutherfurd Exp $
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

/* This is pretty much ripped from gui/CompleteWord.java 
*  Copyright (c) 2006 Alan Ezust
*/

package cscopefinder;

//{{{ imports
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import org.gjt.sp.util.Log;
//}}}

class ChooseTargetListPopup extends JWindow 
{
	//{{{ private declarations
	private ChooseTargetList chooseTagList;
	private JLabel helpLabel;
	private Vector tagLines;
	private View view;
	private boolean numberKeyProcessed = false;
	private boolean openNewView;
	//}}}

	//{{{ ChooseTargetListPopup constructor
	public ChooseTargetListPopup(View view, Vector tagLines, boolean newView)
	{
		super(view);
		this.tagLines = tagLines;
		this.view = view;
		this.openNewView = newView;

		// create components
		chooseTagList = new ChooseTargetList(tagLines);
		helpLabel = new JLabel(jEdit.getProperty(
										"target-selection-popup-help.label"));
		// setup components
		chooseTagList.addMouseListener(new MouseHandler());
		helpLabel.setBorder(BorderFactory.createEmptyBorder(0,4,2,0));
		// place components
		/*
		 * stupid scrollbar policy is an attempt to work around
		 * bugs people have been seeing with IBM's JDK -- 7 Sep 2000 
		 * Comment from Slava
		 */
		JScrollPane scroller = new JScrollPane(chooseTagList,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		getContentPane().add(scroller, BorderLayout.CENTER);
		getContentPane().add(helpLabel, BorderLayout.SOUTH);

		GUIUtilities.requestFocus(this,chooseTagList);

		pack();
		setLocation();
		setVisible(true);

		KeyHandler keyHandler = new KeyHandler();
		addKeyListener(keyHandler);
		getRootPane().addKeyListener(keyHandler);
		chooseTagList.addKeyListener(keyHandler);
		this.view.setKeyEventInterceptor(keyHandler);
	} //}}}

	//{{{ setLocation() method
	public void setLocation()
	{
		JEditTextArea textArea = view.getTextArea();

		int caretLine = textArea.getCaretLine();
		int lineIdx = textArea.getCaretPosition() - // offsets from beg of file
		textArea.getLineStartOffset(caretLine);

		Point location = textArea.offsetToXY(textArea.getCaretPosition());
		// modify Y to be below cursor
		location.setLocation(location.getX(), 
			location.getY() + textArea.getPainter().getFontMetrics().getHeight());

		SwingUtilities.convertPointToScreen(location, textArea.getPainter());

		// make sure it fits on screen
		Dimension d = getSize();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if(location.x + d.width > screenSize.width)
		{
			if(d.width >= screenSize.width)
				/* In this intance we should actually resize the number of columns in 
				 * the tag index filename, but for now just position it so that you 
				 * can at least read the left side of the dialog
				 */
				location.x = 0;
			else
				location.x = screenSize.width - d.width;
		}
		if(location.y + d.height > screenSize.height)
			location.y = screenSize.height - d.height;

		setLocation(location);

		textArea = null;
		location = null;
		d = null;
		screenSize = null;
	} //}}}

	//{{{ dispose() method
	public void dispose()	
	{
		this.view.setKeyEventInterceptor(null);
		super.dispose();
		this.view.getTextArea().requestFocus();
	} //}}}

	//{{{ selected() method
	private void selected()
	{
		TargetLine tagLine = (TargetLine)chooseTagList.getSelectedValue();
		CscopeFinderPlugin.goToTagLine(view, tagLine, openNewView, tagLine.getTag());
		dispose();
	} //}}}

	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter	
	{
		//{{{ keyTyped() method
		
		public void keyPressed(KeyEvent evt) {
			int ch = evt.getKeyCode();
			int selected = chooseTagList.getSelectedIndex();
			int numRows = chooseTagList.getVisibleRowCount()-1;
			int size = chooseTagList.getModel().getSize();
			int newSelect = -1;
			switch (ch)
			{
			case KeyEvent.VK_PAGE_UP:
				newSelect = selected - numRows;
				if (newSelect < 0) newSelect = 0;
				chooseTagList.setSelectedIndex(newSelect);
				chooseTagList.ensureIndexIsVisible(newSelect);
				evt.consume();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				newSelect = selected + numRows;
				if (newSelect >= size) newSelect = size - 1; 
				chooseTagList.setSelectedIndex(newSelect);
				chooseTagList.ensureIndexIsVisible(newSelect);
				evt.consume();
				break;
			case KeyEvent.VK_UP:
				evt.consume();
				if(selected == 0)
					break;
/*					else if(getFocusOwner() == chooseTagList)
					break; */
				selected = selected - 1;
				chooseTagList.setSelectedIndex(selected);
				chooseTagList.ensureIndexIsVisible(selected);
				break;
			case KeyEvent.VK_DOWN:
				evt.consume();
				if(selected >= size) 
					break;
/*					if(getFocusOwner() == chooseTagList)
					break; */
				selected = selected + 1;
				chooseTagList.setSelectedIndex(selected);
				chooseTagList.ensureIndexIsVisible(selected);
				break;					

			}
			
		}
		
		public void keyTyped(KeyEvent evt)
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if (evt == null)
				return;
			int selected = chooseTagList.getSelectedIndex();
			int size = chooseTagList.getModel().getSize();
			int newSelect = -1;
			char ch = evt.getKeyChar();
			switch (ch) 
			{
				case KeyEvent.VK_ENTER:
					selected();
					evt.consume();
					break;

				case KeyEvent.VK_TAB:
				case KeyEvent.VK_ESCAPE:
					dispose();
					evt.consume();
					break;				
				case ' ':
					new ChooseTargetListDialog(view, tagLines, openNewView);
					dispose();
					evt.consume();
					break;

				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					if (numberKeyProcessed) // Since many components have this handler
						return;
					
					/* There may actually be more than 9 items in the list, but since
					 * the user would have to scroll to see them either with the mouse
					 * or with the arrow keys, then they can select the item they want
					 * with those means.
					 */
					selected = Character.getNumericValue(evt.getKeyChar()) - 1;
					if (selected >= 0 && selected < size) 
					{
						chooseTagList.setSelectedIndex(selected);
						selected();
						numberKeyProcessed = true;
					}
					evt.consume();
			}
			
			evt = null;
		} //}}}
	} //}}}

	//{{{ MouseHandler
	class MouseHandler extends MouseAdapter	
	{
		public void mouseClicked(MouseEvent evt) 
		{
			selected();
		}
	} //}}}
}

// :collapseFolds=1:noTabs=false:lineSeparator=\r\n:tabSize=4:indentSize=4:deepIndent=false:folding=explicit:
