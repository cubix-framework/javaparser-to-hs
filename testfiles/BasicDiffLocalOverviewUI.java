/*
* Copyright (c) 2008, Dale Anson
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

package jdiff.component.ui;

import java.awt.*;
import java.awt.event.*;

import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;

import jdiff.DualDiffManager;
import jdiff.JDiffPlugin;
import jdiff.component.*;
import jdiff.util.Diff;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;

public class BasicDiffLocalOverviewUI extends DiffLocalOverviewUI implements MouseListener, ChangeListener, CaretListener {

    private DiffLocalOverview diffLocalOverview;
    private LocalRendererPane localRendererPane;

    private Rectangle leftBorder;
    private Rectangle rightBorder;
    private int pixelsPerLine = 1;
    private int leftVisibleLineCount;
    private int rightVisibleLineCount;
    private Rectangle leftRectangle;
    private Rectangle rightRectangle;
    private Rectangle centerRectangle;
    private HashMap<Diff.Change, Point> leftConnectors = new HashMap<Diff.Change, Point>();
    private HashMap<Diff.Change, Point> rightConnectors = new HashMap<Diff.Change, Point>();

    public static ComponentUI createUI( JComponent c ) {
        return new BasicDiffLocalOverviewUI();
    }

    public void installUI( JComponent c ) {
        diffLocalOverview = ( DiffLocalOverview ) c;
        diffLocalOverview.setLayout( createLayoutManager() );
        installDefaults();
        installComponents();
        installListeners();

    }

    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();

        diffLocalOverview = null;
    }

    public void installDefaults() {}

    public void installComponents() {
        localRendererPane = new LocalRendererPane();
        diffLocalOverview.add( localRendererPane, BorderLayout.CENTER );
    }

    public void installListeners() {
        diffLocalOverview.addMouseListener( this );
        diffLocalOverview.addChangeListener( this );
        diffLocalOverview.getModel().getLeftTextArea().addCaretListener( this );
        diffLocalOverview.getModel().getRightTextArea().addCaretListener( this );
    }

    public void uninstallDefaults() {}

    public void uninstallComponents() {
        diffLocalOverview.remove( localRendererPane );
        diffLocalOverview = null;
    }

    public void uninstallListeners() {
        diffLocalOverview.removeMouseListener( this );
        diffLocalOverview.removeChangeListener( this );
        diffLocalOverview.getModel().getLeftTextArea().removeCaretListener( this );
        diffLocalOverview.getModel().getRightTextArea().removeCaretListener( this );
    }

    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    public void stateChanged( ChangeEvent event ) {
        localRendererPane.repaint();
    }

    public class LocalRendererPane extends JPanel {

        public LocalRendererPane( ) {
            Dimension dim = getPreferredSize();
            dim.width = 36;
            setPreferredSize( dim );
        }

        public void paintComponent( Graphics gfx ) {
            super.paintComponent( gfx );

            DiffTextAreaModel model = diffLocalOverview.getModel();
            if ( model == null ) {
                return ;
            }

            // draw 3 rectangles the height of the text area.  The left rectangle
            // shows lines in the left text area that are different from those in
            // the right text area, the right rectangle shows lines in the right
            // text area that are different from those in the left text area, and
            // the center area connects the corresponding left and right changed
            // areas.
            leftVisibleLineCount = model.getLeftTextArea().getVisibleLines();
            rightVisibleLineCount = model.getRightTextArea().getVisibleLines();
            pixelsPerLine = model.getLeftTextArea().getPainter().getLineHeight();

            // default width is 40 pixels, set in DiffOverview
            Rectangle size = getBounds();
            gfx.setColor( getBackground() );
            gfx.fillRect( 0, 0, size.width, size.height );

            // slightly narrower rectangle, leaves a 4 pixel border on the left and
            // right sides to separate the diff area from the text area and its
            // scroll bar.  This could be a little narrower, maybe even 1 since the
            // diff rectangles have a black border around them.
            centerRectangle = new Rectangle( 4, 0, size.width - 8, size.height );   // width = 32

            // for drawing the diffs for the left text area
            leftRectangle = new Rectangle(
                        centerRectangle.x,                                            // 4
                        centerRectangle.y,                                            // 0
                        centerRectangle.width / 3,                                    // (40 - 4 - 4) / 3 = 8
                        Math.max( 1, pixelsPerLine * leftVisibleLineCount )
                    );

            // for drawing the diffs for the right text area
            rightRectangle = new Rectangle(
                        centerRectangle.x + ( centerRectangle.width - ( centerRectangle.width / 3 ) ),                     // 4 + (32 - 8) = 28
                        centerRectangle.y,                                            // 0
                        centerRectangle.width / 3,                                    // 8
                        Math.max( 1, pixelsPerLine * rightVisibleLineCount )
                    );

            // borders for the left and right rectangles
            leftBorder = new Rectangle( leftRectangle );
            rightBorder = new Rectangle( rightRectangle );

            // make the left and right rectangles match the view background
            gfx.setColor( jEdit.getColorProperty( "view.bgColor", Color.WHITE ) );
            gfx.fillRect( leftRectangle.x, leftRectangle.y, leftRectangle.width, leftRectangle.height );
            gfx.fillRect( rightRectangle.x, rightRectangle.y, rightRectangle.width, rightRectangle.height );

            // clear hunk cursors
            paintCurrentHunkCursor( gfx, null );

            // draw the diff areas in the left and right rectangles, and draw the
            // connecting line between corresponding diffs in the center rectangle
            fillLeft( gfx, model );
            fillRight( gfx, model );
            fillCenter( gfx, model );

            // draw the borders around the left and right rectangles
            gfx.setColor( Color.black );
            gfx.drawRect( leftBorder.x - 1, leftBorder.y, leftBorder.width + 1, leftBorder.height - 1 );
            gfx.drawRect( rightBorder.x - 1, rightBorder.y, rightBorder.width + 1, rightBorder.height - 1 );
        }
    }

    // draw the diff areas in the left rectangle
    private void fillLeft( Graphics gfx, DiffTextAreaModel model ) {
        // get the visible lines, only need to draw hunks between these lines
        int leftFirstLine = model.getLeftTextArea().getFirstPhysicalLine();
        int leftLastLine = model.getLeftTextArea().getLastPhysicalLine() + 1;

        // map of line number to hunk
        HashMap<Integer, Diff.Change> leftHunkMap = model.getLeftHunkMap();

        // output color for diff area
        Color color;

        if ( leftHunkMap != null ) {
            // go through each of the visible lines, see if there is a corresponding
            // diff for that line
            for ( int i = leftFirstLine; i <= leftLastLine; i++ ) {
                Diff.Change hunk = leftHunkMap.get( i );
                int first_visible_line = 0;
                int visible_lines = 0;
                if ( hunk != null ) {
                    // found a diff for a line, set the color and size.  Set the
                    // size all at once for the height of the hunk to minimize looping.
                    if ( hunk.lines0 == 0 ) {
                        color = JDiffPlugin.overviewInvalidColor;
                        leftRectangle.height = 1;
                        leftRectangle.y = centerRectangle.y + ( ( hunk.first0 - leftFirstLine ) * pixelsPerLine );
                    }
                    else {
                        color = hunk.lines1 == 0 ? JDiffPlugin.overviewDeletedColor : JDiffPlugin.overviewChangedColor;
                        // might be in the middle of a hunk because the hunk is
                        // scrolling off the top of the screen
                        visible_lines = hunk.first0 >= leftFirstLine ? hunk.lines0 : Math.max( 1, hunk.last0 - leftFirstLine + 1 );
                        first_visible_line = visible_lines == 1 ? hunk.first0 : hunk.last0 - visible_lines + 1;
                        leftRectangle.height = Math.max( 1, pixelsPerLine * visible_lines );
                        leftRectangle.y = centerRectangle.y + ( ( first_visible_line - leftFirstLine ) * pixelsPerLine );
                    }
                    gfx.setColor( color );

                    // draw the hunk
                    gfx.fillRect( leftRectangle.x, leftRectangle.y, leftRectangle.width, leftRectangle.height );

                    // remember the coordinates to draw the connector later on
                    leftConnectors.put( hunk, new Point( leftRectangle.x + leftRectangle.width + 1, leftRectangle.y ) );

                    // draw the "move it right" arrow
                    if ( hunk.lines0 > 0 ) {
                        gfx.setColor( Color.BLACK );
                        int arrow_height = ( pixelsPerLine - 2 ) % 2 == 0 ? pixelsPerLine - 3 : pixelsPerLine - 2;
                        for ( int j = 0; j < 6; j++ ) {
                            gfx.drawLine( leftRectangle.x + 2 + j, leftRectangle.y + 1 + j,
                                    leftRectangle.x + 2 + j, leftRectangle.y + j + arrow_height - ( 2 * j ) );
                        }
                    }
                    // skip any other lines covered by this hunk
                    i += visible_lines;
                }
            }

            // if the left text area is the active area and the caret line for the
            // left text area is in a hunk, draw the hunk cursor to indicate the
            // current hunk.  Don't trigger a paintCurrentHunkCursor just because
            // the caret line is in a hunk, the caret doesn't move in the inactive
            // text area, the caret in the inactive text area shouldn't trigger a
            // hunk cursor repaint, only the caret in the active text area should
            // do that.
            int caret_line = model.getLeftTextArea().getCaretLine();
            Diff.Change hunk = leftHunkMap.get( caret_line );
            // here's a NPE waiting to happen...
            if ( hunk != null && model.getLeftTextArea().getView().getEditPane().getTextArea().equals( model.getLeftTextArea() ) ) {
                paintCurrentHunkCursor( gfx, hunk );
            }
        }
    }

    private void fillRight( Graphics gfx, DiffTextAreaModel model ) {
        int rightFirstLine = model.getRightTextArea().getFirstPhysicalLine();
        int rightLastLine = model.getRightTextArea().getLastPhysicalLine() + 1;
        HashMap<Integer, Diff.Change> rightHunkMap = model.getRightHunkMap();
        Color color;
        if ( rightHunkMap != null ) {
            for ( int i = rightFirstLine; i <= rightLastLine; i++ ) {
                Diff.Change hunk = rightHunkMap.get( i );
                int first_visible_line = 0;
                int visible_lines = 0;
                if ( hunk != null ) {
                    if ( hunk.lines1 == 0 ) {
                        color = JDiffPlugin.overviewInvalidColor;
                        rightRectangle.height = 1;
                        rightRectangle.y = centerRectangle.y + ( ( hunk.first1 - rightFirstLine ) * pixelsPerLine );
                    }
                    else {
                        color = hunk.lines0 == 0 ? JDiffPlugin.overviewInsertedColor : JDiffPlugin.overviewChangedColor;
                        // might be in the middle of a hunk because the hunk is
                        // scrolling off the top of the screen
                        visible_lines = hunk.first1 >= rightFirstLine ? hunk.lines1 : Math.max( 1, hunk.last1 - rightFirstLine + 1 );
                        first_visible_line = visible_lines == 1 ? hunk.first1 : hunk.last1 - visible_lines + 1;
                        rightRectangle.height = Math.max( 1, pixelsPerLine * visible_lines );
                        rightRectangle.y = centerRectangle.y + ( ( first_visible_line - rightFirstLine ) * pixelsPerLine );
                    }
                    gfx.setColor( color );
                    gfx.fillRect( rightRectangle.x, rightRectangle.y, rightRectangle.width, rightRectangle.height );

                    // remember the coordinates to draw the connector later on
                    rightConnectors.put( hunk, new Point( rightRectangle.x - 1, rightRectangle.y ) );

                    // draw the "move it left" arrow
                    if ( hunk.lines1 > 0 ) {
                        gfx.setColor( Color.BLACK );
                        int arrow_height = ( pixelsPerLine - 2 ) % 2 == 0 ? pixelsPerLine - 3 : pixelsPerLine - 2;
                        int center = arrow_height / 2 + 1;
                        for ( int j = 0; j < 6; j++ ) {
                            gfx.drawLine( rightRectangle.x + 1 + j, rightRectangle.y + center - j,
                                    rightRectangle.x + 1 + j, rightRectangle.y + center + j );
                        }
                    }
                    i += visible_lines;
                }
            }
            int caret_line = model.getRightTextArea().getCaretLine();
            Diff.Change hunk = rightHunkMap.get( caret_line );
            if ( hunk != null && model.getRightTextArea().getView().getEditPane().getTextArea().equals( model.getRightTextArea() ) ) {
                paintCurrentHunkCursor( gfx, hunk );
            }
        }
    }

    private void fillCenter( Graphics gfx, DiffTextAreaModel model ) {
        // draw a line to connect corresponding diff blocks in the left and
        // right rectangles.
        int leftFirstLine = model.getLeftTextArea().getFirstPhysicalLine();
        int leftLastLine = model.getLeftTextArea().getLastPhysicalLine() + 1;
        HashMap<Integer, Diff.Change> leftHunkMap = model.getLeftHunkMap();     // line number -> hunk
        int rightFirstLine = model.getRightTextArea().getFirstPhysicalLine();
        int rightLastLine = model.getRightTextArea().getLastPhysicalLine() + 1;

        if ( leftHunkMap != null ) {
            gfx.setColor( Color.BLACK );
            for ( int leftLine = leftFirstLine; leftLine <= leftLastLine; leftLine++ ) {
                Diff.Change leftHunk = leftHunkMap.get( leftLine );
                if ( leftHunk != null && leftHunk.first0 == leftLine && leftHunk.first1 >= rightFirstLine && leftHunk.first1 < rightLastLine ) {
                    Point leftPoint = leftConnectors.get( leftHunk );
                    Point rightPoint = rightConnectors.get( leftHunk );
                    if ( leftPoint != null && rightPoint != null ) {
                        gfx.drawLine( leftPoint.x, leftPoint.y, rightPoint.x, rightPoint.y );
                    }
                }
            }
        }
    }

    public void mouseClicked( MouseEvent e ) {
        DiffTextAreaModel model = diffLocalOverview.getModel();
        if ( model == null ) {
            return ;
        }
        if ( leftBorder.contains( e.getX(), e.getY() ) ) {
            // handle click on left side
            int line_number = ( e.getY() / pixelsPerLine ) + model.getLeftTextArea().getFirstPhysicalLine();
            moveRight( line_number );
        }
        else if ( rightBorder.contains( e.getX(), e.getY() ) ) {
            // handle click on right side
            int line_number = ( e.getY() / pixelsPerLine ) + model.getRightTextArea().getFirstPhysicalLine();
            moveLeft( line_number );
        }
    }

    // copies a diff starting at the given line number in the left text area and
    // replaces the corresponding diff in the right text area
    public void moveRight( int line_number ) {
        // TODO:  why is this method here?  It belongs in DualDiff.  The model
        // has a reference to the DualDiff.  Fix the looping to better use the
        // hunk offsets in Diff.Change.
        DiffTextAreaModel model = diffLocalOverview.getModel();
        if ( model == null ) {
            return ;
        }
        JEditTextArea leftTextArea = model.getLeftTextArea();
        JEditTextArea rightTextArea = model.getRightTextArea();

        Diff.Change hunk = model.getEdits();
        for ( ; hunk != null; hunk = hunk.next ) {
            // find the hunk pertaining to this line number
            if ( ( hunk.first0 + Math.max( 0, hunk.lines0 - 1 ) ) < line_number ) {
                continue;   // before this line, keep looking
            }

            if ( hunk.first0 > line_number ) {
                // after this line, didn't find a line with a corresponding hunk
                if ( jEdit.getBooleanProperty( "jdiff.beep-on-error" ) ) {
                    leftTextArea.getToolkit().beep();
                }
                break;
            }

            // on a line with a right arrow --
            // get the text from the left text area to move to the right
            leftTextArea.selectNone();
            int line_count = leftTextArea.getLineCount();
            int start_sel = leftTextArea.getLineStartOffset( hunk.first0 );
            int end_sel;
            if (hunk.first0 + hunk.lines0 >= line_count - 1) {
                // at bottom of buffer, select to end of buffer
                end_sel = leftTextArea.getBufferLength();
            }
            else {
                end_sel = leftTextArea.getLineStartOffset( hunk.first0 + hunk.lines0 );
            }
            leftTextArea.setCaretPosition( start_sel );
            Selection.Range leftSelection;
            if ( hunk.lines0 == 0 ) {
                leftSelection = new Selection.Range( start_sel, start_sel );
            }
            else {
                leftSelection = new Selection.Range( start_sel, end_sel );
            }
            String leftText = leftTextArea.getSelectedText( leftSelection );

            // replace text on right with text from left
            rightTextArea.selectNone();
            line_count = rightTextArea.getLineCount();
            if ( hunk.first1 >= line_count - 1 ) {
                // at bottom of buffer, need special handling
                String rightlinesep = rightTextArea.getBuffer().getStringProperty(Buffer.LINESEP);
                if (rightTextArea.getText().endsWith(rightlinesep)) {
                    rightlinesep = "";
                }
                rightTextArea.setText(rightTextArea.getText() + rightlinesep + leftText);
                rightTextArea.goToBufferEnd(false);
            }
            else {
                start_sel = rightTextArea.getLineStartOffset( hunk.first1 );
                end_sel = rightTextArea.getLineStartOffset( hunk.first1 + hunk.lines1 );
                rightTextArea.setCaretPosition( start_sel );
                Selection.Range selection;
                if ( hunk.lines1 == 0 ) {
                    selection = new Selection.Range( start_sel, start_sel );
                }
                else {
                    selection = new Selection.Range( start_sel, end_sel );
                }
                rightTextArea.setSelectedText( selection, leftText );
            }
            rightTextArea.selectNone();
            DualDiffManager.refreshFor( rightTextArea.getView() );
            break;
        }
    }

    // copies a diff starting at the given line number in the right text area and
    // replaces the corresponding diff in the left text area
    public void moveLeft( int line_number ) {
        // TODO:  why is this method here?  It belongs in DualDiff.  The model
        // has a reference to the DualDiff.  Fix the looping to better use the
        // hunk offsets in Diff.Change.
        DiffTextAreaModel model = diffLocalOverview.getModel();
        if ( model == null ) {
            return ;
        }
        JEditTextArea leftTextArea = model.getLeftTextArea();
        JEditTextArea rightTextArea = model.getRightTextArea();
        Diff.Change hunk = model.getEdits();
        for ( ; hunk != null; hunk = hunk.next ) {
            // find the hunk pertaining to this line number
            if ( ( hunk.first1 + Math.max( 0, hunk.lines1 - 1 ) ) < line_number ) {
                continue;   // before this line, keep looking
            }

            if ( hunk.first1 > line_number ) {
                // after this line, didn't find a line with a corresponding hunk
                if ( jEdit.getBooleanProperty( "jdiff.beep-on-error" ) ) {
                    rightTextArea.getToolkit().beep();
                }
                break;
            }

            // on a line with a left arrow --
            // get the text from the right text area to move to the left
            rightTextArea.selectNone();
            int line_count = rightTextArea.getLineCount();
            int start_sel = rightTextArea.getLineStartOffset( hunk.first1 );
            int end_sel;
            if (hunk.first1 + hunk.lines1 >= line_count - 1) {
                // at bottom of buffer. select to tend of buffer
                end_sel = rightTextArea.getBufferLength();
            }
            else {
                end_sel = rightTextArea.getLineStartOffset( hunk.first1 + hunk.lines1 );
            }
            rightTextArea.setCaretPosition( start_sel );
            Selection.Range rightSelection;
            if ( hunk.lines1 == 0 ) {
                rightSelection = new Selection.Range( start_sel, start_sel );
            }
            else {
                rightSelection = new Selection.Range( start_sel, end_sel );
            }
            String rightText = rightTextArea.getSelectedText( rightSelection );

            // replace text on left with text from right
            leftTextArea.selectNone();
            line_count = leftTextArea.getLineCount();
            if(hunk.first0 >= line_count - 1) {
                String leftlinesep = leftTextArea.getBuffer().getStringProperty(Buffer.LINESEP);
                if (leftTextArea.getText().endsWith(leftlinesep)) {
                    leftlinesep = "";
                }
                leftTextArea.setText(leftTextArea.getText() + leftlinesep + rightText);
            }
            else {
                start_sel = leftTextArea.getLineStartOffset( hunk.first0 );
                end_sel = leftTextArea.getLineStartOffset( hunk.first0 + hunk.lines0 );
                leftTextArea.setCaretPosition( start_sel );
                Selection.Range leftSelection;
                if ( hunk.lines0 == 0 ) {
                    leftSelection = new Selection.Range( start_sel, start_sel );
                }
                else {
                    leftSelection = new Selection.Range( start_sel, end_sel );
                }
                leftTextArea.setSelectedText( leftSelection, rightText );
            }
            leftTextArea.selectNone();
            DualDiffManager.refreshFor( leftTextArea.getView() );
            break;
        }
    }

    public void mouseEntered( MouseEvent e ) {}
    public void mouseExited( MouseEvent e ) {}
    public void mousePressed( MouseEvent e ) {}
    public void mouseReleased( MouseEvent e ) {}

    /**
     * Handle caret movement in the text areas, paint the hunk cursors as
     * appropriate.
     */
    public void caretUpdate( final CaretEvent e ) {
        if ( e.getSource().equals( diffLocalOverview.getModel().getLeftTextArea() ) ) {
            paintCurrentHunkCursor( null, inLeftHunk() );
        }
        else if ( e.getSource().equals( diffLocalOverview.getModel().getRightTextArea() ) ) {
            paintCurrentHunkCursor( null, inRightHunk() );
        }
    }

    // check if the caret line in the left text area is in a hunk.  If so, return
    // the hunk, otherwise, return null.
    private Diff.Change inLeftHunk() {
        DiffTextAreaModel model = diffLocalOverview.getModel();
        HashMap<Integer, Diff.Change> leftHunkMap = model.getLeftHunkMap();
        if ( leftHunkMap != null ) {
            int caret_line = model.getLeftTextArea().getCaretLine();
            if ( leftHunkMap.get( caret_line ) != null && caret_line >= model.getLeftTextArea().getFirstPhysicalLine() && caret_line <= model.getLeftTextArea().getLastPhysicalLine() ) {
                return leftHunkMap.get( caret_line );
            }
        }
        return null;
    }

    // check if the caret line in the right text area is in a hunk.  If so, return
    // the hunk, otherwise, return null.
    private Diff.Change inRightHunk() {
        DiffTextAreaModel model = diffLocalOverview.getModel();
        HashMap<Integer, Diff.Change> rightHunkMap = model.getRightHunkMap();
        if ( rightHunkMap != null ) {
            int caret_line = model.getRightTextArea().getCaretLine();
            if ( rightHunkMap.get( caret_line ) != null && caret_line >= model.getRightTextArea().getFirstPhysicalLine() && caret_line <= model.getRightTextArea().getLastPhysicalLine() ) {
                return rightHunkMap.get( caret_line );
            }
        }
        return null;
    }

    /**
     * Paint the cursors for the current hunk.  The "current" hunk is the hunk
     * in the active text area that contains the caret for that text area.
     * @param gfx graphics context, if null, will use graphics context from diffLocalOverview component
     * @param currentHunk the diff to draw cursors for.  If null, will clear all cursors.
     */
    private void paintCurrentHunkCursor( Graphics gfx, Diff.Change currentHunk ) {
        if ( gfx == null ) {
            gfx = diffLocalOverview.getGraphics();
        }
        if ( gfx == null ) {
            return ;        // no graphics context to draw on
        }

        // clear the current hunk cursors, if any
        if ( leftRectangle == null || rightRectangle == null ) {
            return ;
        }
        DiffTextAreaModel model = diffLocalOverview.getModel();
        int leftFirstLine = model.getLeftTextArea().getFirstPhysicalLine();
        int leftLastLine = model.getLeftTextArea().getLastPhysicalLine() + 1;
        gfx.setColor( localRendererPane.getBackground() );
        gfx.drawRect( leftRectangle.x - 3, 0, 1, ( leftLastLine - leftFirstLine ) * pixelsPerLine );

        int rightFirstLine = model.getRightTextArea().getFirstPhysicalLine();
        int rightLastLine = model.getRightTextArea().getLastPhysicalLine() + 1;
        gfx.setColor( localRendererPane.getBackground() );
        gfx.drawRect( rightRectangle.x + rightRectangle.width + 1, 0, 1, ( rightLastLine - rightFirstLine ) * pixelsPerLine );

        if ( currentHunk == null ) {
            return ;    // nothing to draw
        }

        HashMap<Integer, Diff.Change> leftHunkMap = model.getLeftHunkMap();
        if ( leftHunkMap != null ) {
            // paint left cursor for current hunk
            for ( int i = leftFirstLine; i <= leftLastLine; i++ ) {
                Diff.Change hunk = leftHunkMap.get( i );
                if ( hunk != null && hunk.equals( currentHunk ) ) {
                    if ( hunk.lines0 == 0 ) {
                        leftRectangle.height = 1;
                    }
                    else {
                        // possibly adjust for partially visible hunk, hunk may be scrolled partially off screen
                        int adjust = leftFirstLine > hunk.first0 ? leftFirstLine - hunk.first0 : 0;
                        leftRectangle.height = Math.max( 1, pixelsPerLine * (hunk.lines0 - adjust ) );
                    }
                    leftRectangle.y = centerRectangle.y + ( ( i - leftFirstLine ) * pixelsPerLine );
                    gfx.setColor( JDiffPlugin.leftCursorColor );
                    gfx.drawRect( leftRectangle.x - 3, leftRectangle.y, 1, leftRectangle.height - 1 );
                    break;
                }
            }
        }
        HashMap<Integer, Diff.Change> rightHunkMap = model.getRightHunkMap();
        if ( rightHunkMap != null ) {
            // paint right cursor for current hunk
            for ( int i = rightFirstLine; i <= rightLastLine; i++ ) {
                Diff.Change hunk = rightHunkMap.get( i );
                if ( hunk != null && hunk.equals( currentHunk ) ) {
                    if ( hunk.lines1 == 0 ) {
                        rightRectangle.height = 1;
                    }
                    else {
                        // possibly adjust for partially visible hunk, hunk may be scrolled partially off screen
                        int adjust = rightFirstLine > hunk.first1 ? rightFirstLine - hunk.first1 : 0;
                        rightRectangle.height = Math.max( 1, pixelsPerLine * (hunk.lines1 - adjust) );
                    }
                    rightRectangle.y = centerRectangle.y + ( ( i - rightFirstLine ) * pixelsPerLine );
                    gfx.setColor( JDiffPlugin.rightCursorColor );
                    gfx.drawRect( rightRectangle.x + rightRectangle.width + 1, rightRectangle.y, 1, rightRectangle.height - 1 );
                    break;
                }
            }
        }
    }
}