/*
 * Copyright 2001 Nicholas Allen (nallen@freenet.co.uk) This file is part of
 * JavaCVS. JavaCVS is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version. JavaCVS is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * General Public License along with JavaCVS; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */
package allensoft.diff;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractNextAction;
import gruntspud.actions.AbstractPreviousAction;
import gruntspud.style.TextStyle;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Reader;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
//import allensoft.util.*;
/** Views differences side by side in a JSplitPane. */
public class DiffViewer extends JPanel {
	private static Rectangle g_WindowBounds;
	private JSplitPane m_SplitPane;
	private DiffDisplay m_Left;
	private DiffDisplay m_Right;
	private DiffNavigator m_Navi;
	private JPanel m_NextPrevPanel;
	private JScrollPane m_LeftScroll;
	private Action nextAction, previousAction;
	/**
	 * Creates a new DiffViewer object.
	 * 
	 * @param sLeftTitle
	 *            DOCUMENT ME!
	 * @param sRightTitle
	 *            DOCUMENT ME!
	 * @param in
	 *            DOCUMENT ME!
	 * @param parser
	 *            DOCUMENT ME!
	 * @param bInvert
	 *            DOCUMENT ME!
	 * 
	 * @throws DiffException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public DiffViewer(
		String sLeftTitle,
		String sRightTitle,
		Reader in,
		DiffParser parser,
		boolean bInvert,
		GruntspudContext context)
		throws DiffException, IOException {
		super(new BorderLayout());
		m_Left = createDiffDisplay(context);
		m_Right = createDiffDisplay(context);
		if (bInvert)
			parser = new ReverseDiffParser(parser);
		// Initialize the diff displays with the appropriate lines
		DiffDisplayInitializer initializer =
			new DiffDisplayInitializer(in, parser, m_Left, m_Right, bInvert);
		initializer.run();
		m_LeftScroll =
			new JScrollPane(
				m_Left,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
			public boolean isFocusTraversable() {
				return true;
			}
		};
		m_LeftScroll.setBorder(BorderFactory.createLineBorder(Color.black));
		final JScrollPane rightScroll =
			new JScrollPane(
				m_Right,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
			public boolean isFocusTraversable() {
				return true;
			}
		};
		rightScroll.setBorder(BorderFactory.createLineBorder(Color.black));
		final JCheckBox synchronizeScrollBars =
			new JCheckBox("Synchronize scroll bars", true);
		final JButton previousButton = UIUtil.createButton(previousAction = new PreviousAction(), true, false);
		final JButton nextButton = UIUtil.createButton(nextAction = new NextAction(), true, false);
		Color background = m_Left.getStyle(DiffType.NONE).getBackground();
		m_LeftScroll.getViewport().setBackground(background);
		rightScroll.getViewport().setBackground(background);
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(new JLabel(sLeftTitle), BorderLayout.NORTH);
		leftPanel.add(m_LeftScroll, BorderLayout.CENTER);
		m_Navi = new DiffNavigator(this, m_Left.getLines(), m_Right.getLines(), context);
		m_Navi.setBorder(BorderFactory.createLineBorder(Color.black));
		leftPanel.add(m_Navi, BorderLayout.EAST);
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JLabel(sRightTitle), BorderLayout.NORTH);
		rightPanel.add(rightScroll, BorderLayout.CENTER);
		m_SplitPane =
			new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT,
				false,
				leftPanel,
				rightPanel);
		m_SplitPane.setOneTouchExpandable(true);
		m_SplitPane.setDividerSize(7);
		// This method only exists in 1.3 so we try and catch the
		// NoSuchMethodError in case
		// it is run on 1.2.X.
		try {
			m_SplitPane.setResizeWeight(0.5);
		} catch (NoSuchMethodError e) {
		}
		add(m_SplitPane, BorderLayout.CENTER);
		add(
			createNextPrevPanel(
				synchronizeScrollBars,
				previousButton,
				nextButton),
			BorderLayout.SOUTH);
		// Add listeners to synchronize scroll bars if enabled.
		m_LeftScroll.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (synchronizeScrollBars.isSelected()) {
					Point p = m_LeftScroll.getViewport().getViewPosition();
					rightScroll.getViewport().setViewPosition(p);
				}
			}
		});
		rightScroll.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (synchronizeScrollBars.isSelected()) {
					Point p = rightScroll.getViewport().getViewPosition();
					m_LeftScroll.getViewport().setViewPosition(p);
				}
			}
		});
		setAvailableActions();
	}
	/**
	 * Creates a new DiffViewer object.
	 * 
	 * @param sLeftTitle
	 *            DOCUMENT ME!
	 * @param sRightTitle
	 *            DOCUMENT ME!
	 * @param in
	 *            DOCUMENT ME!
	 * @param parser
	 *            DOCUMENT ME!
	 * 
	 * @throws DiffException
	 *             DOCUMENT ME!
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public DiffViewer(
		String sLeftTitle,
		String sRightTitle,
		Reader in,
		DiffParser parser,
		GruntspudContext context)
		throws DiffException, IOException {
		this(sLeftTitle, sRightTitle, in, parser, false, context);
	}
  
	public void setAvailableActions() {
    
	}
  
	/**
	 * DOCUMENT ME!
	 * 
	 * @param proportionalLocation
	 *            DOCUMENT ME!
	 */
	public void setDividerLocation(double proportionalLocation) {
		m_SplitPane.setDividerLocation(proportionalLocation);
	}
	/** Creates a DiffDisplay used to display the differences for a file. */
	protected DiffDisplay createDiffDisplay(GruntspudContext context) {
		return new DiffDisplay(context);
	}
	/** Gets the tab size. */
	public int getTabSize() {
		return m_Left.getTabSize();
	}
	/** Sets the tab size. */
	public void setTabSize(int nTabSize) {
		m_Left.setTabSize(nTabSize);
		m_Right.setTabSize(nTabSize);
	}
	/** Gets the font used to display the differences. */
	public Font getDisplayFont() {
		return m_Left.getDisplayFont();
	}
	/** Sets the font used to display the differences. */
	public void setDisplayFont(Font font) {
		m_Left.setDisplayFont(font);
		m_Right.setDisplayFont(font);
	}
	/** Gets the style used to display differences of the supplied type. */
	public TextStyle getStyle(DiffType type) {
		return m_Left.getStyle(type);
	}
	/** Sets the style used to display differences for the supplied type. */
	public void setStyle(DiffType type, TextStyle style) {
		m_Left.setStyle(type, style);
		m_Right.setStyle(type, style);
	}
	void setScrollPosition(int lineNo) {
		Point p = new Point(0, m_Left.lineYPos(lineNo));
		m_LeftScroll.getViewport().setViewPosition(p);
	}
	private JPanel createNextPrevPanel(
		JCheckBox synchronizeScrollBars,
		JButton prev,
		JButton next) {
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gbc;
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(2, 2, 2, 2);
		UIUtil.jGridBagAdd(p, synchronizeScrollBars, gbc, 1);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(p, prev, gbc, GridBagConstraints.RELATIVE);
		UIUtil.jGridBagAdd(p, next, gbc, GridBagConstraints.REMAINDER);	
		return p;
	}
	/**
	 * A DiffProcessor which adds appropriate lines to the DiffViewers to
	 * display the differences graphically.
	 */
	private static class DiffDisplayInitializer extends DiffProcessor {
		private DiffDisplay m_File1;
		private DiffDisplay m_File2;
		private boolean m_bInvert;
		public DiffDisplayInitializer(
			Reader in,
			DiffParser parser,
			DiffDisplay file1,
			DiffDisplay file2,
			boolean bInvert) {
			super(in, parser);
			m_File1 = file1;
			m_File2 = file2;
			m_bInvert = bInvert;
		}
		protected void printLineInFile1(
			int nLineNum,
			DiffType type,
			String sLine) {
			if (m_bInvert) {
				if (type == DiffType.INSERTION)
					type = DiffType.DELETION;
				else if (type == DiffType.DELETION)
					type = DiffType.INSERTION;
				m_File2.addLine(nLineNum, type, sLine);
			} else
				m_File1.addLine(nLineNum, type, sLine);
		}
		protected void printNonExistantLineInFile1() {
			if (m_bInvert)
				m_File2.addNonExistantLine();
			else
				m_File1.addNonExistantLine();
		}
		protected void printLineInFile2(
			int nLineNum,
			DiffType type,
			String sLine) {
			if (m_bInvert) {
				if (type == DiffType.INSERTION)
					type = DiffType.DELETION;
				else if (type == DiffType.DELETION)
					type = DiffType.INSERTION;
				m_File1.addLine(nLineNum, type, sLine);
			} else
				m_File2.addLine(nLineNum, type, sLine);
		}
		protected void printNonExistantLineInFile2() {
			if (m_bInvert)
				m_File1.addNonExistantLine();
			else
				m_File2.addNonExistantLine();
		}
	}
	/**
	 * A DiffParser which reverses the differences from a supplied DiffParser.
	 * This is useful to generate the contents of a file when you know the
	 * differences and the result of applying the differences but
	 */
	private static class ReverseDiffParser implements DiffParser {
		private DiffParser m_Parser;
		public ReverseDiffParser(DiffParser parser) {
			m_Parser = parser;
		}
		public Difference getNextDifference() throws DiffException {
			Difference d = m_Parser.getNextDifference();
			if (d == null)
				return null;
			if (d instanceof Insertion) {
				Insertion i = (Insertion) d;
				return new Deletion(
					i.getStartLineInFile2(),
					i.getEndLineInFile2(),
					i.getStartLineInFile1(),
					i.getInsertedText());
			} else if (d instanceof Deletion) {
				Deletion del = (Deletion) d;
				return new Insertion(
					del.getStartLineInFile2(),
					del.getStartLineInFile1(),
					del.getEndLineInFile1(),
					del.getDeletedText());
			} else {
				Change c = (Change) d;
				return new Change(
					d.getStartLineInFile2(),
					c.getEndLineInFile2(),
					c.getStartLineInFile1(),
					c.getEndLineInFile1(),
					c.getToText(),
					c.getFromText());
			}
		}
	}
	class NextAction
		extends AbstractNextAction {
		public void actionPerformed(ActionEvent evt) {
			m_Navi.advanceDiff(true);
			repaint();
			setAvailableActions();
		}
	}
	class PreviousAction
		extends AbstractPreviousAction {
		public void actionPerformed(ActionEvent evt) {
			m_Navi.advanceDiff(false);
			repaint();
			setAvailableActions();
		}
	}
}
