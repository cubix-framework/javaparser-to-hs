/* % [{
% (C) Copyright 2008 Nicolas Carranza and individual contributors.
% See the CandyFolds-copyright.txt file in the CandyFolds distribution for a full
% listing of individual contributors.
%
% This file is part of CandyFolds.
%
% CandyFolds is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License,
% or (at your option) any later version.
%
% CandyFolds is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with CandyFolds.  If not, see <http://www.gnu.org/licenses/>.
% }] */
package candyfolds;

import candyfolds.config.ModeConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.geom.Line2D;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;
import javax.swing.text.Segment;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.buffer.BufferListener;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.textarea.TextAreaExtension;
import org.gjt.sp.jedit.textarea.TextAreaPainter;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

final class TextAreaExt
	extends TextAreaExtension {
	static final Logger L=Logger.getLogger(TextAreaExt.class.getName());
	static { L.setLevel(Level.ALL); }

	final CandyFoldsPlugin foldsPlugin;
	final EditPane editPane;
	private final TextAreaPainter painter;

	private LineInfo previousLineInfo=new LineInfo(this);
	private LineInfo currentLineInfo=new LineInfo(this);
	//private final LineInfo toolTipLineInfo=new LineInfo(this);

	private final FontMetricsInfo fontMetricsInfo=new FontMetricsInfo();
	private static final class FontMetricsInfo {

		private FontMetrics fontMetrics;
		int spaceWidth;
		int lineHeight;
		Stroke barStroke;

		void setFontMetrics(FontMetrics fontMetrics) {
			if(this.fontMetrics==fontMetrics)
				return;
			this.fontMetrics=fontMetrics;
			spaceWidth = fontMetrics.charWidth(' ');
			lineHeight=fontMetrics.getHeight();
			barStroke=new BasicStroke(spaceWidth/2.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
		}
	}

	private ModeConfig modeConfig;
	private String modeConfigName; // use it separated from modeConfig because modeConfig can be null!
	private final Line2D.Float line2D=new Line2D.Float();
	final Segment segment=new Segment();
	private final Rectangle rect=new Rectangle();

	TextAreaExt(CandyFoldsPlugin foldsPlugin, EditPane editPane) {
		this.foldsPlugin=foldsPlugin;
		this.editPane=editPane;
		this.painter=editPane.getTextArea().getPainter();
		painter.addExtension(this);
	}

	void clearModeConfig() {
		modeConfigName=null;
		modeConfig=null;
	}

	private void setModeConfig(Buffer buffer) {
		if(modeConfigName!=buffer.getMode().getName()) {
			modeConfigName=buffer.getMode().getName();
			modeConfig=foldsPlugin.getConfig().getModeConfig(modeConfigName);
			//Log.log(Log.NOTICE, this, "selected modeConfig for mode "+modeConfigName+": "+modeConfig);
		}
	}

	ModeConfig getModeConfig() {
		return modeConfig;
	}

	void remove() {
		painter.removeExtension(this);
	}

	@Override
	public void paintValidLine(Graphics2D g,
			int screenLine,
			int physicalLine,
			int start,
			int end,
			int y) {
	}

	@Override
	public synchronized void paintScreenLineRange(Graphics2D g,
			int firstLine,
			int lastLine,
			int[] physicalLines,
			int[] start,
			int[] end,
			int y,
			int lineHeight){
		//Log.log(Log.NOTICE, this, "painting range: firstLine="+firstLine+", lastLine=" + lastLine + ", y=" + y+"lineHeight="+lineHeight);
		//long nanoTime=System.nanoTime();
		Buffer buffer = editPane.getBuffer();
		setModeConfig(buffer);
		if(!getModeConfig().getEnabled())
			return;

		for(int i = 0; i < physicalLines.length; i++) {
			int line=physicalLines[i];
			if(line==-1)
				continue;
			if(i==0)
				currentLineInfo.eval(buffer, line);
			else
				currentLineInfo.eval(buffer, line, previousLineInfo);
			drawLineIndents(buffer, currentLineInfo, g, y);
			LineInfo swap=previousLineInfo;
			previousLineInfo=currentLineInfo;
			currentLineInfo=swap;
			y += lineHeight;
		}
		//Log.log(Log.NOTICE, this, "done painting range");
		//L.fine("new painting time="+(System.nanoTime()-nanoTime));
	}

	private void drawLineIndents(Buffer buffer, LineInfo lineInfo, Graphics2D g, int y) {
		fontMetricsInfo.setFontMetrics(painter.getFontMetrics());
		Stroke gStroke=g.getStroke();
		g.setStroke(fontMetricsInfo.barStroke);
		int x;
		int indent;
		int horizontalOffset=editPane.getTextArea().getHorizontalOffset();

		for (int i =lineInfo.getIndentsSize();--i>=1;) {// ATTENTION: omit the first
			indent =lineInfo.getIndent(i);
			if(indent==0 && !modeConfig.getShowStripOn0Indent())
				continue;
			//Log.log(Log.NOTICE, this, "painting indent="+indent);
			Color color=lineInfo.getStripConfig(i).getColor();
			if(color.getAlpha()==0)
				continue;
			g.setColor(color);
			x= (int)((indent+0.4f )* fontMetricsInfo.spaceWidth+horizontalOffset);
			g.drawLine(x, y,x, y+fontMetricsInfo.lineHeight);
		}
		g.setStroke(gStroke);
	}

	// DISABLED: does not worth the effort?
	/*
	@Override
	public synchronized String getToolTipText(int x, int y) {
		JEditTextArea ta=editPane.getTextArea();
		Buffer buffer = editPane.getBuffer();

		int offset = ta.xyToOffset(x, y, false);
		if ((offset == -1) || (offset >= buffer.getLength())) {
			return null;
		}

		int physicalLine = ta.getLineOfOffset(offset);
			toolTipLineInfo.eval(buffer, physicalLine);

		fontMetricsInfo.setFontMetrics(painter.getFontMetrics());
		rect.y=ta.offsetToXY(physicalLine, 0).y;
		rect.width=2*fontMetricsInfo.spaceWidth;
		rect.height=fontMetricsInfo.lineHeight;
		int indent;
		int horizontalOffset=ta.getHorizontalOffset();
		for (int i =toolTipLineInfo.indents.size();--i>=1;) { // ignore the first. Iteration from outer to inner fold. the first (i=0) is right over the first char of the line.
			indent =toolTipLineInfo.indents.get(i);
			if(indent==0)
				continue;
			rect.x= (int)(indent* fontMetricsInfo.spaceWidth+horizontalOffset);
			if(rect.contains(x, y)){
				//int foldLevel=toolTipLineInfo.foldLevels.get(i);
				String stripConfigName=toolTipLineInfo.stripConfigs.get(i).getName();
				buffer.getLineText(toolTipLineInfo.lines.get(i), segment);
				return segment.toString().trim()+"... : "+stripConfigName;
			}
		}
		return null;
		}*/
}