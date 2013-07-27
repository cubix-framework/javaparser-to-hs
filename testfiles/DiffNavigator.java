/*
Copyright 2003 J?rgen N?rgaard (jnp@anneli.dk)
This file is part of Gruntspud.
Gruntspud is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
JavaCVS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with JavaCVS; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package allensoft.diff;

import gruntspud.GruntspudContext;
import gruntspud.standalone.JDK13GruntspudHost;
import gruntspud.style.TextStyle;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JPanel;

/**
* DOCUMENT ME!
*
* @author $author$
*/
public class DiffNavigator extends JPanel {
	private List m_Left=null, m_Right=null;
	private int prefWidth=20;
	private int prefHeigth=20;
	private TextStyle m_NonExistantStyle;
	private TextStyle m_NoStyle,m_InsertStyle,m_DeleteStyle,m_ChangeStyle;
	private DiffViewer m_Parent=null;
	private GruntspudContext context;
	
	public DiffNavigator(DiffViewer parent, List left, List right, GruntspudContext context) {
		this.context = context;
		this.m_Parent=parent;
		this.m_Left=left;
		this.m_Right=right;
		
//	  setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));   

		m_NonExistantStyle=context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_NON_EXISTANT);
		setStyle(DiffType.NONE, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_IDENTICAL));
		setStyle(DiffType.INSERTION, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_INSERTION));
		setStyle(DiffType.DELETION, context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_DELETION));
		setStyle(DiffType.CHANGE,context.getTextStyleModel().getStyle(JDK13GruntspudHost.OPTIONS_STYLE_DIFF_CHANGE));
		addMouseListener(new ClickListener());
		
	}
	
	public synchronized void setStyle(DiffType type, TextStyle style) {
		if(type == DiffType.NONE) {
			m_NoStyle = style;
			setBackground(style.getBackground());
		} else if(type == DiffType.INSERTION)
		m_InsertStyle = style;
		
		else if(type == DiffType.DELETION)
			m_DeleteStyle = style;
		
		else
			m_ChangeStyle = style;
		
		repaint();
	}
	
	
	public TextStyle getStyle(DiffType type) {
		TextStyle style = m_NoStyle;
		if(type == DiffType.NONE)
			;
		else if(type == DiffType.INSERTION)
			style = m_InsertStyle;
		else if(type == DiffType.DELETION)
			style = m_DeleteStyle;
		else
			style = m_ChangeStyle;
		return style;
	}
	
	public Dimension getPreferredSize() {
		return new Dimension (prefWidth, prefHeigth);
	}
	
	public void paintComponent(Graphics g) {
		Insets insets=getInsets();
		int x=0+insets.left;
		int y=0+insets.top;
		
		super.paintComponent(g);
		
		setUpNavigator();
		
		if ( navigator!=null ) {
			g.drawImage(navigator, x, y, m_NonExistantStyle.getBackground(), null);		
		} else {
			System.err.println("Uups");
		}
		
	}
	private Image navigator=null;
	private Vector regions=null;
	private int effectiveHeight=-1;
	private	int maxLine=-1;
	
	private int currentDiff=-1;
	
	public void advanceDiff(boolean forward) {
		// forward == true
		// move forward/backward, circular if needed
		if ( regions.size()>0 ) {
			if ( currentDiff==-1 ) {
				currentDiff=0;				
			} else {
				currentDiff=(currentDiff+(forward?1:-1)) % regions.size();
				if ( currentDiff<0 ) {
					currentDiff=regions.size()-1;
				}
			}
			RangePair rp=(RangePair) regions.elementAt(currentDiff);
			m_Parent.setScrollPosition(rp.getFrom()-2);
		}
	}
	
  
	private void setUpNavigator() {
		Insets insets=getInsets();
		int w=getWidth()-(insets.left+insets.right);
		int h=getHeight()-(insets.top+insets.bottom);
		int x=0+insets.left;
		int y=0+insets.top;
		maxLine=m_Left.size();
		effectiveHeight=h;
		 // new Throwable().printStackTrace();
		Vector leftRanges=new Vector();
		Vector rightRanges=new Vector();
		
		navigator=createImage(w, h);
		
		Graphics g=navigator.getGraphics();
		
		g.setColor(m_NonExistantStyle.getBackground());
		g.fillRect(x,y,w,h);

		if (m_Left.size()>0) {
			Line line = (Line)m_Left.get(0);
			DiffType last=line.m_Type;
			int lastIdx=0;
			for (int i=1;i<m_Left.size();i++) {
				 line = (Line)m_Left.get(i);
				 if ( last!=line.m_Type ) {
					 if ( last!=DiffType.NONE) {
						leftRanges.add(new RangePair(lastIdx, i-1, last, getStyle(last).getBackground()));
					 }
				     lastIdx=i;
						 last=line.m_Type;
				 }
			}
			if (lastIdx<m_Left.size()-1 && last!=DiffType.NONE) {
				leftRanges.add(new RangePair(lastIdx, m_Left.size()-1, last, getStyle(last).getBackground()));
			}
		}
		for (int i=0;i<leftRanges.size();i++) {
			RangePair rp=(RangePair) leftRanges.elementAt(i);
			int xs[]=new int[4];
			int ys[]=new int[4];
			xs[0]=x;
			ys[0]=y+(int) (((float) rp.getFrom()*(float) h)/((float) maxLine));
			xs[1]=w;
			ys[1]=y+(int) (((float) rp.getFrom()*(float) h)/((float) maxLine));
			xs[2]=w;
			ys[2]=y+(int) (((float) (rp.getTo()+1)*(float) h)/((float) maxLine));
			xs[3]=x;
			ys[3]=y+(int) (((float) (rp.getTo()+1)*(float) h)/((float) maxLine));
			Polygon pol=new Polygon(xs,ys,xs.length);
			g.setColor(rp.getColor());
			g.fillPolygon(pol);
		}

		if (m_Right.size()>0) {
			Line line = (Line)m_Right.get(0);
			DiffType last=line.m_Type;
			int lastIdx=0;
			for (int i=1;i<m_Right.size();i++) {
				 line = (Line)m_Right.get(i);
				 if ( last!=line.m_Type ) {
					 if ( last!=DiffType.NONE) {
						rightRanges.add(new RangePair(lastIdx, i-1, last, getStyle(last).getBackground()));
					 }
				     lastIdx=i;
						 last=line.m_Type;
				 }
			}
			if (lastIdx<m_Right.size()-1 && last!=DiffType.NONE) {
				rightRanges.add(new RangePair(lastIdx, m_Right.size()-1, last, getStyle(last).getBackground()));
			}
		}
		for (int i=0;i<rightRanges.size();i++) {
			RangePair rp=(RangePair) rightRanges.elementAt(i);
			int xs[]=new int[4];
			int ys[]=new int[4];
			xs[0]=x;
			ys[0]=y+(int) (((float) rp.getFrom()*(float) h)/((float) maxLine));
			xs[1]=w;
			ys[1]=y+(int) (((float) rp.getFrom()*(float) h)/((float) maxLine));
			xs[2]=w;
			ys[2]=y+(int) (((float) (rp.getTo()+1)*(float) h)/((float) maxLine));
			xs[3]=x;
			ys[3]=y+(int) (((float) (rp.getTo()+1)*(float) h)/((float) maxLine));
			Polygon pol=new Polygon(xs,ys,xs.length);
			g.setColor(rp.getColor());
			g.fillPolygon(pol);
		}
		
		// find regions
		regions=new Vector();
		for (int i=0;i<leftRanges.size();i++) {
			RangePair rp=(RangePair) leftRanges.elementAt(i);
			regions.add(rp);
		}
		for (int i=0;i<rightRanges.size();i++) {
			RangePair rp=(RangePair) rightRanges.elementAt(i);
			if ( ! regions.contains(rp) ) {
				regions.add(rp);
			}
		}
	}
	
	private class RangePair {
		
		private int from, to;
		private DiffType status;
		private Color color;
		public RangePair(int from, int to, DiffType status, Color color) {
			this.from=from;
			this.to=to;
			this.status=status;
			this.color=color;
		}
		
		public int getFrom() { return from; }
		public int getTo() { return to; }
		public DiffType getStatus() { return status; }
		public Color getColor() { return color; }
		
		public boolean equals(Object other) {
			boolean res=false;
			if ( other instanceof RangePair ) {
				RangePair rp=(RangePair) other;
				res=(this==rp) || (rp==null?(false):(from==rp.from && to==rp.to && true));
			} else {
				res=false;
			}
			return res;
		}
		public String toString() {
			return "("+from+", " + to+") [" + status + ", " + color +"]";
		}
	}
	
	private class ClickListener implements MouseListener {
		public void mouseClicked(MouseEvent e) {
			m_Parent.setScrollPosition(((e.getY()*maxLine)/effectiveHeight)-2);
		}
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) {
		}
	}
}
