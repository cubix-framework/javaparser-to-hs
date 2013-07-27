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
 * $Id: ChooseTagListPopup.java 16411 2009-10-24 02:51:34Z voxmea $
 */

/* This is pretty much ripped from gui/CompleteWord.java */

package tags;

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

class ChooseTagListPopup extends JWindow 
{
	//{{{ private declarations
	private ChooseTagList chooseTagList;
	private JLabel helpLabel;
	private Vector tagLines;
	private View view;
	private boolean numberKeyProcessed = false;
	private boolean openNewView;
	//}}}

	//{{{ ChooseTagListPopup constructor
	public ChooseTagListPopup(View view, Vector tagLines, boolean newView)
	{
		super(view);
		this.tagLines = tagLines;
		this.view = view;
		this.openNewView = newView;

		// create components
		chooseTagList = new ChooseTagList(tagLines);
		helpLabel = new JLabel(jEdit.getProperty("tag-collision-popup-help.label"));
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
		show();

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
		Rectangle screenSize = GUIUtilities.getScreenBounds();
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
		TagLine tagLine = (TagLine)chooseTagList.getSelectedValue();
		TagsPlugin.goToTagLine(view, tagLine, openNewView, tagLine.getTag());
		dispose();
	} //}}}

	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter	
	{
		//{{{ keyTyped() method
		public void keyTyped(KeyEvent evt)
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if (evt == null)
				return;
			
			switch (evt.getKeyChar())
			{
				case ' ':
					new ChooseTagListDialog(view, tagLines, openNewView);
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
					int selected = Character.getNumericValue(evt.getKeyChar()) - 1;
					if (selected >= 0 && 
						selected < chooseTagList.getModel().getSize())
					{
						chooseTagList.setSelectedIndex(selected);
						selected();
						numberKeyProcessed = true;
					}
					evt.consume();
			}
			
			evt = null;
		} //}}}

		//{{{ keyPressed() method
		public void keyPressed(KeyEvent evt) 
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if (evt == null)
				return;
			//{{{ evt.getKeyCode() switch
			switch(evt.getKeyCode()) 
			{
				case KeyEvent.VK_TAB:
				case KeyEvent.VK_ENTER:
					selected();
					evt.consume();
					break;
				case KeyEvent.VK_ESCAPE:
					dispose();
					evt.consume();
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

					evt.consume();
					break;
				case KeyEvent.VK_DOWN:
					selected = chooseTagList.getSelectedIndex();
					if (selected == chooseTagList.getModel().getSize() - 1)
						selected = 0;
					else if (getFocusOwner() == chooseTagList)
						return; // Let JList handle the event
					else
						selected = selected + 1;
					
					chooseTagList.setSelectedIndex(selected);
					chooseTagList.ensureIndexIsVisible(selected);
					
					evt.consume();
					break;
				case KeyEvent.VK_SPACE:
				case KeyEvent.VK_1:
				case KeyEvent.VK_2:
				case KeyEvent.VK_3:
				case KeyEvent.VK_4:
				case KeyEvent.VK_5:
				case KeyEvent.VK_6:
				case KeyEvent.VK_7:
				case KeyEvent.VK_8:
				case KeyEvent.VK_9:
					evt.consume();  /* so that we don't automatically dismiss */
					break;

				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_PAGE_DOWN:
					break;

				default:
					dispose();
					evt.consume();
					break;
			} //}}}
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
