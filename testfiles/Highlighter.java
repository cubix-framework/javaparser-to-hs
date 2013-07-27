/*
* Highlighter.java - The Highlighter is the texteara painter
* :tabSize=8:indentSize=8:noTabs=false:
* :folding=explicit:collapseFolds=1:
*
* Copyright (C) 2004, 2010 Matthieu Casanova
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
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.search.SearchMatcher;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.TextAreaExtension;

import java.awt.*;
import java.util.regex.PatternSyntaxException;
//}}}

/**
 * The Highlighter is the TextAreaExtension that will look for some String to
 * highlightList in the textarea and draw a rectangle in it's background.
 *
 * @author Matthieu Casanova
 */
class Highlighter extends TextAreaExtension implements HighlightChangeListener
{
	private final JEditTextArea textArea;
	private final Point point = new Point();
	private FontMetrics fm;

	private final HighlightManager highlightManager;
	private AlphaComposite blend;
	private float alpha;
	public static boolean square;

	public static Color squareColor;

	public static final int MAX_LINE_LENGTH = 10000;

	//{{{ Highlighter constructor
	Highlighter(JEditTextArea textArea)
	{
		alpha = ((float)jEdit.getIntegerProperty(HighlightOptionPane.PROP_ALPHA, 50)) / 100f;
		blend = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		highlightManager = HighlightManagerTableModel.getManager();
		this.textArea = textArea;
	} //}}}

	//{{{ setAlphaComposite() method
	public void setAlphaComposite(float alpha)
	{
		if (this.alpha != alpha)
		{
			this.alpha = alpha;
			blend = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		}
	} //}}}

	//{{{ paintScreenLineRange() method
	@Override
	public void paintScreenLineRange(Graphics2D gfx, int firstLine, int lastLine, int[] physicalLines, int[] start, int[] end, int y, int lineHeight)
	{
		fm = textArea.getPainter().getFontMetrics();
		if (highlightManager.isHighlightEnable() &&
		    highlightManager.countHighlights() != 0 ||
		    HighlightManagerTableModel.currentWordHighlight.isEnabled() ||
		    HighlightManagerTableModel.selectionHighlight.isEnabled())
			super.paintScreenLineRange(gfx, firstLine, lastLine, physicalLines, start, end, y, lineHeight);
	} //}}}

	//{{{ paintValidLine() method
	/**
	 * Called by the text area when the extension is to paint a
	 * screen line which has an associated physical line number in
	 * the buffer. Note that since one physical line may consist of
	 * several screen lines due to soft wrap, the start and end
	 * offsets of the screen line are passed in as well.
	 *
	 * @param gfx The graphics context
	 * @param screenLine The screen line number
	 * @param physicalLine The physical line number
	 * @param start The offset where the screen line begins, from
	 * the start of the buffer
	 * @param end The offset where the screen line ends, from the
	 * start of the buffer
	 * @param y The y co-ordinate of the top of the line's
	 * bounding box
	 * @since jEdit 4.0pre4
	 */
	@Override
	public void paintValidLine(Graphics2D gfx,
				   int screenLine,
				   int physicalLine,
				   int start,
				   int end,
				   int y)
	{
		JEditBuffer buffer = textArea.getBuffer();
		int lineStartOffset = buffer.getLineStartOffset(physicalLine);
		int lineEndOffset = buffer.getLineEndOffset(physicalLine);
		int length = buffer.getLineLength(physicalLine);

		int screenToPhysicalOffset = start - lineStartOffset;


		int l = length - screenToPhysicalOffset - lineEndOffset + end;
		if (l > MAX_LINE_LENGTH)
			l = MAX_LINE_LENGTH;
		CharSequence lineContent = buffer.getSegment(lineStartOffset + screenToPhysicalOffset,
			l);
		if (lineContent.length() == 0)
			return;

		CharSequence tempLineContent = lineContent;
		try
		{
			highlightManager.getReadLock();
			for (int i = 0; i < highlightManager.countHighlights(); i++)
			{
				Highlight highlight = highlightManager.getHighlight(i);
				highlight(highlight, buffer, gfx, physicalLine, y, screenToPhysicalOffset,
					tempLineContent);
				tempLineContent = lineContent;
			}
		}
		finally
		{
			highlightManager.releaseLock();
		}
		tempLineContent = lineContent;
		if (textArea.getSelectionCount() == 0)
		{
			highlight(HighlightManagerTableModel.currentWordHighlight, buffer, gfx, physicalLine, y,
				screenToPhysicalOffset, tempLineContent);
		}
		else
		{
			highlight(HighlightManagerTableModel.selectionHighlight, buffer, gfx, physicalLine, y,
				screenToPhysicalOffset, tempLineContent);
		}
	} //}}}

	//{{{ highlight() method
	private void highlight(Highlight highlight,
			       JEditBuffer buffer,
			       Graphics2D gfx,
			       int physicalLine,
			       int y,
			       int screenToPhysicalOffset,
			       CharSequence tempLineContent)
	{
		if (!highlight.isEnabled() ||
		    !highlight.isValid() ||
		    (highlight.getScope() == Highlight.BUFFER_SCOPE &&
		     highlight.getBuffer() != buffer))
		{
			return;
		}

		SearchMatcher searchMatcher = highlight.getSearchMatcher();
		try
		{
			int i = 0;
			SearchMatcher.Match match = null;
			while (true)
			{
				match = searchMatcher.nextMatch(tempLineContent,
								i == 0,
								true,
								match == null,
								false);
				if (match == null || match.end == match.start)
					break;
				int offset = match.start + i +
					     screenToPhysicalOffset + textArea.getLineStartOffset(physicalLine);
				Selection selectionAtOffset = textArea.getSelectionAtOffset(offset);
				if (selectionAtOffset == null)
				{
					int caretOffsetInLine = textArea.getCaretPosition() - textArea.getLineStartOffset(textArea.getCaretLine());
					int endOffset = match.end + i + screenToPhysicalOffset;
					int startOffset = match.start + i + screenToPhysicalOffset;
					if (highlight != HighlightManagerTableModel.currentWordHighlight ||
					    textArea.getCaretLine() != physicalLine ||
					     caretOffsetInLine < startOffset || caretOffsetInLine > endOffset)
					{
						_highlight(highlight.getColor(), gfx, physicalLine, startOffset, endOffset, y, true);
					}
					else
					{
						_highlight(highlight.getColor(), gfx, physicalLine, startOffset, endOffset, y, false);
					}
				}
				highlight.updateLastSeen();
				i += match.end;
				int length = tempLineContent.length() - match.end;
				if (length <= 0)
					break;
				tempLineContent = tempLineContent.subSequence(match.end,
					length + match.end);
			}
		}
		catch (PatternSyntaxException e)
		{
			// the regexp was invalid
			highlight.setValid(false);
		}
	} //}}}

	//{{{ _highlight() method
	private void _highlight(Color highlightColor,
				Graphics2D gfx,
				int physicalLine,
				int startOffset,
				int endOffset,
				int y,
				boolean filled)
	{
		Point p = textArea.offsetToXY(physicalLine, startOffset, point);
		if (p == null)
		{
			// The start offset was not visible
			return;
		}
		int startX = p.x;

		p = textArea.offsetToXY(physicalLine, endOffset, point);
		if (p == null)
		{
			// The end offset was not visible
			return;
		}
		int endX = p.x;
		Color oldColor = gfx.getColor();
		Composite oldComposite = gfx.getComposite();
		gfx.setColor(highlightColor);
		gfx.setComposite(blend);
		if (filled)
			gfx.fillRoundRect(startX, y, endX - startX, fm.getHeight() - 1, 5, 5);

		if (square)
		{
			gfx.setColor(squareColor);
			gfx.drawRoundRect(startX, y, endX - startX, fm.getHeight() - 1,5,5);
		}
		else if (!filled)
		{
			gfx.drawRoundRect(startX, y, endX - startX, fm.getHeight() - 1,5,5);
		}

		gfx.setColor(oldColor);
		gfx.setComposite(oldComposite);
	} //}}}

	//{{{ highlightUpdated() method
	public void highlightUpdated(boolean highlightEnabled)
	{
		int firstLine = textArea.getFirstPhysicalLine();
		int lastLine = textArea.getLastPhysicalLine();
		textArea.invalidateLineRange(firstLine, lastLine);
	} //}}}
}
