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

import javax.swing.*;
import javax.swing.plaf.ComponentUI;

import jdiff.JDiffPlugin;
import jdiff.component.*;
import jdiff.util.Diff;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;

public class BasicDiffGlobalPhysicalOverviewUI extends DiffGlobalPhysicalOverviewUI implements MouseListener {

    private DiffGlobalPhysicalOverview diffGlobalPhysicalOverview;
    private LocalRendererPane localRendererPane;

    private double pixelsPerLine = 1;
    private Rectangle leftRectangle;
    private Rectangle rightRectangle;

    public static ComponentUI createUI( JComponent c ) {
        return new BasicDiffGlobalPhysicalOverviewUI();
    }

    public void installUI( JComponent c ) {
        diffGlobalPhysicalOverview = ( DiffGlobalPhysicalOverview ) c;
        diffGlobalPhysicalOverview.setLayout( createLayoutManager() );
        installDefaults();
        installComponents();
        installListeners();

    }

    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();

        diffGlobalPhysicalOverview = null;
    }

    public void installDefaults() {}

    public void installComponents() {
        localRendererPane = new LocalRendererPane();
        diffGlobalPhysicalOverview.add( localRendererPane, BorderLayout.CENTER );
    }

    public void installListeners() {
        diffGlobalPhysicalOverview.addMouseListener( this );
    }

    public void uninstallDefaults() {}

    public void uninstallComponents() {}

    public void uninstallListeners() {}

    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }


    public class LocalRendererPane extends JPanel {

        public LocalRendererPane( ) {
            Dimension dim = getPreferredSize();
            dim.width = 36;
            setPreferredSize( dim );
        }

        public void paintComponent( Graphics gfx ) {
            super.paintComponent( gfx );

            DiffTextAreaModel model = diffGlobalPhysicalOverview.getModel();
            if ( model == null ) {
                return ;
            }

            int leftLineCount = model.getLeftLineCount();
            int rightLineCount = model.getRightLineCount();

            Rectangle size = getBounds();

            gfx.setColor( getBackground() );
            gfx.fillRect( 0, 0, size.width, size.height );

            Rectangle inner = new Rectangle( 4, 4, size.width - 8, size.height - 8 );

            int lines = Math.max( leftLineCount, rightLineCount );
            pixelsPerLine = ( ( double ) inner.height ) / lines;

            Rectangle left = new Rectangle(
                        inner.x,
                        inner.y,
                        inner.width / 3,
                        Math.max( 1, ( int ) Math.round( pixelsPerLine * leftLineCount ) )
                    );
            Rectangle right = new Rectangle(
                        inner.x + ( inner.width - left.width ),
                        inner.y,
                        left.width,
                        Math.max( 1, ( int ) Math.round( pixelsPerLine * rightLineCount ) )
                    );

            leftRectangle = new Rectangle( left );
            rightRectangle = new Rectangle( right );

            Color leftColor = JDiffPlugin.overviewInvalidColor;
            Color rightColor = JDiffPlugin.overviewInvalidColor;

            gfx.setColor( Color.black );
            gfx.drawRect( left.x - 1, left.y - 1, left.width + 1, left.height + 1 );
            gfx.drawRect( right.x - 1, right.y - 1, right.width + 1, right.height + 1 );

            gfx.setColor( jEdit.getColorProperty("view.bgColor", Color.WHITE) );
            gfx.fillRect( left.x, left.y, left.width, left.height );
            gfx.fillRect( right.x, right.y, right.width, right.height );

            Diff.Change hunk = model.getEdits();

            int leftOffset = 0;
            int rightOffset = 0;
            for ( ; hunk != null; hunk = hunk.next ) {
                leftOffset = hunk.first0;
                rightOffset = hunk.first1;

                if ( hunk.lines1 == 0 && hunk.lines0 != 0 ) { // DELETE
                    leftColor = JDiffPlugin.overviewDeletedColor;
                    rightColor = JDiffPlugin.overviewInvalidColor;
                }
                else if ( hunk.lines1 != 0 && hunk.lines0 == 0 ) { // INSERT
                    leftColor = JDiffPlugin.overviewInvalidColor;
                    rightColor = JDiffPlugin.overviewInsertedColor;
                }
                else { // CHANGE
                    leftColor = JDiffPlugin.overviewChangedColor;
                    rightColor = JDiffPlugin.overviewChangedColor;
                }

                left.y = inner.y + ( int ) Math.round( leftOffset * pixelsPerLine );
                right.y = inner.y + ( int ) Math.round( rightOffset * pixelsPerLine );
                left.height = Math.max( 1, ( int ) Math.round( hunk.lines0 * pixelsPerLine ) );
                right.height = Math.max( 1, ( int ) Math.round( hunk.lines1 * pixelsPerLine ) );
                gfx.setColor( leftColor );
                gfx.fillRect( left.x, left.y, left.width, left.height );
                gfx.setColor( rightColor );
                gfx.fillRect( right.x, right.y, right.width, right.height );

                gfx.setColor( Color.black );
                gfx.drawLine( left.x + left.width + 1, left.y, right.x - 1, right.y );

                // Display the textArea cursor
                this.paintCursor( gfx, model );
            }
        }

        public void paintCursor( Graphics gfx, DiffTextAreaModel model ) {
            int leftLineCount = model.getLeftLineCount();
            int rightLineCount = model.getRightLineCount();
            JEditTextArea leftTextArea = model.getLeftTextArea();
            JEditTextArea rightTextArea = model.getRightTextArea();

            Rectangle size = getBounds();

            Rectangle inner = new Rectangle( 4, 4, size.width - 8, size.height - 8 );

            int lines = Math.max( leftLineCount, rightLineCount );
            double pixelsPerLine = ( ( double ) inner.height ) / lines;

            int physicalFirstLine0 = leftTextArea.getFirstPhysicalLine();
            int physicalLastLine0 = leftTextArea.getLastPhysicalLine();
            Rectangle leftCursor = new Rectangle(
                        inner.x, inner.y + ( ( int ) Math.round( pixelsPerLine * physicalFirstLine0 ) ),
                        inner.width / 3,
                        Math.max( 1, ( int ) Math.round( pixelsPerLine * Math.min( leftLineCount, physicalLastLine0 - physicalFirstLine0 + 1 ) ) )
                    );

            int physicalFirstLine1 = rightTextArea.getFirstPhysicalLine();
            int physicalLastLine1 = rightTextArea.getLastPhysicalLine();
            Rectangle rightCursor = new Rectangle(
                        inner.x + ( inner.width - leftCursor.width ),
                        inner.y + ( ( int ) Math.round( pixelsPerLine * physicalFirstLine1 ) ),
                        leftCursor.width,
                        Math.max( 1, ( int ) Math.round( pixelsPerLine * Math.min( rightLineCount, physicalLastLine1 - physicalFirstLine1 + 1 ) ) )
                    );

            gfx.setColor( JDiffPlugin.leftCursorColor );
            gfx.drawRect( leftCursor.x, leftCursor.y, leftCursor.width - 1, leftCursor.height - 1 );
            gfx.setColor( JDiffPlugin.rightCursorColor );
            gfx.drawRect( rightCursor.x, rightCursor.y, rightCursor.width - 1, rightCursor.height - 1 );
        }
    }

    public void mouseClicked( MouseEvent e ) {
        DiffTextAreaModel model = diffGlobalPhysicalOverview.getModel();
        if ( model == null ) {
            return ;
        }
        JEditTextArea leftTextArea = model.getLeftTextArea();
        JEditTextArea rightTextArea = model.getRightTextArea();
        int line_number = 0;
        if ( leftRectangle.contains( e.getX(), e.getY() ) || rightRectangle.contains( e.getX(), e.getY() ) ) {
            line_number = ( int ) ( ( double ) e.getY() / pixelsPerLine );
            leftTextArea.setFirstPhysicalLine( Math.min( line_number, leftTextArea.getLineCount() ) );
            rightTextArea.setFirstPhysicalLine( Math.min( line_number, rightTextArea.getLineCount() ) );
        }
    }

    public void mouseEntered( MouseEvent e ) {}
    public void mouseExited( MouseEvent e ) {}
    public void mousePressed( MouseEvent e ) {}
    public void mouseReleased( MouseEvent e ) {}
}
