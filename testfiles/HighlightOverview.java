/*
 * HighlightOverview.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009 Szalai Endre
 * Portions Copyright (C) 2009, 2011 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package gatchan.highlight;

//{{{ Imports
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.search.SearchMatcher;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.IntegerArray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//}}}

/**
 * @author Szalai Endre
 */
public class HighlightOverview extends JPanel implements HighlightChangeListener
{
	private final IntegerArray items;

	private final JEditTextArea textArea;

	private static final int ITEM_HEIGHT = 4;
	private static final int OVERVIEW_WIDTH = 12;
	private static final int ITEM_BORDER = 2;
	private static final int ITEM_WIDTH = OVERVIEW_WIDTH - 2 * ITEM_BORDER;
	// To be in the same line as the scrollbar
	private static final int Y_OFFSET = 16;
	private static final Dimension preferredSize = new Dimension(OVERVIEW_WIDTH, 0);
	private Color color;

	//{{{ HighlightOverview constructor
	public HighlightOverview(final JEditTextArea textArea)
	{
		Font ff = getFont();
		Font f = new Font(ff.getName(), Font.BOLD, 8);
		setFont(f);
		this.textArea = textArea;
		items = new IntegerArray(32);
		setRequestFocusEnabled(false);
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				int lineCount = textArea.getLineCount();
				int line = yToLine(e.getY(), lineCount);
				textArea.setFirstLine(Math.max(line - textArea.getVisibleLines() / 2, 0));
			}
		});
	} //}}}

	//{{{ highlightUpdated() method
	public void highlightUpdated(boolean highlightEnabled)
	{
		items.clear();
		if (!highlightEnabled || !HighlightManagerTableModel.currentWordHighlight.isEnabled() ||
			textArea.getSelectionCount() != 0)
		{
			repaint();
			return;
		}

		int offset = 0;
		JEditBuffer buffer = textArea.getBuffer();
		int end = buffer.getLength();
		boolean endOfLine = buffer.getLineEndOffset(
				buffer.getLineOfOffset(end)) - 1 == end;
		SearchMatcher matcher = HighlightManagerTableModel.currentWordHighlight.getSearchMatcher();
		int lastResult = -1;
		int counter;
		for(counter = 0; ; counter++)
		{
			boolean startOfLine = buffer.getLineStartOffset(
				buffer.getLineOfOffset(offset)) == offset;

			SearchMatcher.Match match = matcher.nextMatch(
				buffer.getSegment(offset, end - offset),
				startOfLine,endOfLine,counter == 0,
				false);
			if(match == null)
				break;

			int newLine = buffer.getLineOfOffset(
				offset + match.start);
			if(lastResult != newLine)
			{
				items.add(newLine);
				lastResult = newLine;
			}
			int nextLine = newLine + 1;
			if (nextLine >= buffer.getLineCount())
				break;
			offset = buffer.getLineStartOffset(nextLine);
		}
		View view = textArea.getView();
		if (view.isActive())
		{
			if (view.getTextArea() == textArea)
				view.getStatus().setMessage(counter + " lines contains the current word");
		}
		repaint();
	} //}}}

	//{{{ paintComponent() method
	@Override
	public void paintComponent(Graphics gfx)
	{
		super.paintComponent(gfx);
		if (items.getSize() == 0)
			return;

		int lineCount = textArea.getLineCount();
//		gfx.drawString(String.valueOf(count), 0, 10);
		if (color != null)
			gfx.setColor(color);
		else
			gfx.setColor(HighlightManagerTableModel.currentWordHighlight.getColor());

		for (int i = 0;i<items.getSize();i++)
		{
			int y = lineToY(items.get(i), lineCount);
			gfx.fillRect(ITEM_BORDER, y, ITEM_WIDTH, ITEM_HEIGHT);
		}
	} //}}}
                     
	//{{{ lineToY() method
	private int lineToY(int line, int lineCount)
	{
		return Y_OFFSET + (getHeight() - 2 * Y_OFFSET) * line / lineCount - ITEM_BORDER;
	} //}}}

	//{{{ yToLine() method
	private int yToLine(int y, int lineCount)
	{
		return (y + ITEM_BORDER - Y_OFFSET) * lineCount / (getHeight() - 2 * Y_OFFSET);
	} //}}}

	//{{{ getPreferredSize() method
	@Override
	public Dimension getPreferredSize()
	{
		return preferredSize;
	} //}}}

	public void setOverviewColor(Color color)
	{
		this.color = color;
	}
}
