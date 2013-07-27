/*
 * CompleteWordList.java - Modified Slava's CompleteWord, 
 * since I can't successfully extend it :(
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2001 Slava Pestov
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

package jump;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.TextUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import org.gjt.sp.jedit.syntax.KeywordMap;
import org.gjt.sp.jedit.textarea.JEditTextArea;

public class CompleteWordList extends JWindow
{
	boolean isGlobalSearch;


	public CompleteWordList(View view, String word, Vector completions, Point location,
		String noWordSep, boolean isGlobalSearch)
	{
		super(view);
		this.isGlobalSearch = isGlobalSearch; 
		this.noWordSep = noWordSep;

		setContentPane(new JPanel(new BorderLayout())
		{
			/**
			 * Returns if this component can be traversed by pressing the
			 * Tab key. This returns false.
			 */
			public boolean isManagingFocus()
			{
				return false;
			}

			/**
			 * Makes the tab key work in Java 1.4.
			 */
			public boolean getFocusTraversalKeysEnabled()
			{
				return false;
			}
		});

		this.view = view;
		this.textArea = view.getTextArea();
		this.buffer = view.getBuffer();
		this.word = word;

		words = new JList(completions);

		words.setVisibleRowCount(Math.min(completions.size(),8));

		words.addMouseListener(new MouseHandler());
		words.setSelectedIndex(0);
		words.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		words.setCellRenderer(new Renderer());

		// stupid scrollbar policy is an attempt to work around
		// bugs people have been seeing with IBM's JDK -- 7 Sep 2000
		JScrollPane scroller = new JScrollPane(words,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		getContentPane().add(scroller, BorderLayout.CENTER);

		GUIUtilities.requestFocus(this,words);

		pack();
		setLocation(location);
		show();

		KeyHandler keyHandler = new KeyHandler();
		addKeyListener(keyHandler);
		words.addKeyListener(keyHandler);
		view.setKeyEventInterceptor(keyHandler);
	}

	public void dispose()
	{
		view.setKeyEventInterceptor(null);
		super.dispose();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				textArea.requestFocus();
			}
		});
	}

	private Vector getCompletions(String sel)
	{ 
		//TagsBinSearcher tbs;
		Vector tags = new Vector();
		ProjectBuffer currentTags = JumpPlugin.getActiveProjectBuffer();
		if (currentTags == null) return null;
		tags = currentTags.ctagsBuffer.getEntriesByStartPrefix(sel);
		Vector completions = new Vector();
		String entry;
		for(int i=0; i<tags.size(); i++)
		{
			entry = (String) tags.get(i);
			completions.add(new Completion(entry, false));
		}
		MiscUtilities.quicksort(completions,new MiscUtilities.StringICaseCompare());
		return completions;
	}


	private static String completeWord(String line, int offset, String noWordSep)
	{
		// '+ 1' so that findWordEnd() doesn't pick up the space at the start
		int wordEnd = TextUtilities.findWordEnd(line,offset + 1,noWordSep);
		return line.substring(offset,wordEnd);
	}

	private View view;
	private JEditTextArea textArea;
	private Buffer buffer;
	private String word;
	private JList words;
	private String noWordSep;



	private void insertSelected()
	{
		textArea.setSelectedText(words.getSelectedValue().toString()
			.substring(word.length()));
		dispose();
	}

	static class Completion
	{
		String text;
		boolean keyword;

		public Completion(String text, boolean keyword)
		{
			this.text = text;
			this.keyword = keyword;
		}

		public String toString()
		{
			return text;
		}

		public boolean equals(Object obj)
		{
			if(obj instanceof Completion)
				return ((Completion)obj).text.equals(text);
			else
				return false;
		}
	}

	static class Renderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list,null,index,
				isSelected,cellHasFocus);

			Completion comp = (Completion)value;

			if(index < 9)
				setText((index + 1) + ": " + comp.text);
			else if(index == 9)
				setText("0: " + comp.text);
			else
				setText(comp.text);

			if(comp.keyword)
				setFont(list.getFont().deriveFont(Font.BOLD));
			else
				setFont(list.getFont());

			return this;
		}
	}

	class KeyHandler extends KeyAdapter
	{
		//{{{ keyPressed() method
		public void keyPressed(KeyEvent evt)
		{
			switch(evt.getKeyCode())
			{
			case KeyEvent.VK_TAB:
			case KeyEvent.VK_ENTER:
				insertSelected();
				evt.consume();
				break;
			case KeyEvent.VK_ESCAPE:
				dispose();
				evt.consume();
				break;
			case KeyEvent.VK_UP:
				int selected = words.getSelectedIndex();

				if(selected == 0)
					selected = words.getModel().getSize() - 1;
				else if(getFocusOwner() == words)
					return;
				else
					selected = selected - 1;

				words.setSelectedIndex(selected);
				words.ensureIndexIsVisible(selected);

				evt.consume();
				break;
			case KeyEvent.VK_DOWN:
				/* int */ selected = words.getSelectedIndex();

				if(selected == words.getModel().getSize() - 1)
					selected = 0;
				else if(getFocusOwner() == words)
					return;
				else
					selected = selected + 1;

				words.setSelectedIndex(selected);
				words.ensureIndexIsVisible(selected);

				evt.consume();
				break;
			case KeyEvent.VK_BACK_SPACE:
				if(word.length() == 1)
				{
					textArea.backspace();
					evt.consume();
					dispose();
				}
				else
				{
					word = word.substring(0,word.length() - 1);
					textArea.backspace();
					int caret = textArea.getCaretPosition();
					KeywordMap keywordMap = buffer.getKeywordMapAtOffset(caret);

					//Vector completions = getCompletions(buffer,word,
						//keywordMap,noWordSep,caret);
					Vector completions = getCompletions(word);
					
					if(completions.size() == 0)
						dispose();

					words.setListData(completions);
					words.setSelectedIndex(0);
					words.setVisibleRowCount(Math.min(completions.size(),8));

					pack();

					evt.consume();
				}
				break;
			default:
				if(evt.isActionKey()
					|| evt.isControlDown()
					|| evt.isAltDown()
					|| evt.isMetaDown())
				{
					dispose();
					view.processKeyEvent(evt);
				}
				break;
			}
		}

		public void keyTyped(KeyEvent evt)
		{
			char ch = evt.getKeyChar();
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if(evt == null)
				return;

			if(Character.isDigit(ch))
			{
				int index = ch - '0';
				if(index == 0)
					index = 9;
				else
					index--;
				if(index < words.getModel().getSize())
				{
					words.setSelectedIndex(index);
					textArea.setSelectedText(words.getModel()
						.getElementAt(index).toString()
						.substring(word.length()));
					dispose();
					return;
				}
				else
					/* fall through */;
			}

			if(ch != '\b')
			{
				/* eg, foo<C+b>, will insert foobar, */
				if(!Character.isLetter(ch) && noWordSep.indexOf(ch) == -1)
				{
					insertSelected();
					textArea.userInput(ch);
					dispose();
					return;
				}

				textArea.userInput(ch);

				word = word + ch;
				int caret = textArea.getCaretPosition();
				//KeywordMap keywordMap = buffer.getKeywordMapAtOffset(caret);

				//Vector completions = getCompletions(buffer,word,keywordMap,
					//noWordSep,caret);
					
				Vector completions = getCompletions(word);
					
				if(completions.size() == 0)
				{
					dispose();
					return;
				}

				words.setListData(completions);
				words.setSelectedIndex(0);
			}
		}
	}

	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			insertSelected();
		}
	}
}
