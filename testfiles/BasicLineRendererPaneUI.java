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
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;

import org.gjt.sp.jedit.jEdit;
import jdiff.component.*;

public class BasicLineRendererPaneUI extends DiffLineOverviewUI implements ChangeListener {

    private LineRendererPane lineRendererPane = null;
    private JScrollPane scrollPane = null;
    private DiffLineModel model;
    private LineRenderer lineRenderer = null;
    private Color fgColor = jEdit.getColorProperty( "view.fgColor", Color.BLACK );

    /**
     * Required by super class.
     * @param c not used
     * @return one of these
     */
    public static ComponentUI createUI( JComponent c ) {
        return new BasicLineRendererPaneUI();
    }

    /**
     * Configures the specified component appropriate for the look and feel.
     * This method is invoked when the <code>ComponentUI</code> instance is being installed
     * as the UI delegate on the specified component.  This method should
     * completely configure the component for the look and feel,
     * including the following:
     * <ol>
     * <li>Install any default property values for color, fonts, borders,
     *     icons, opacity, etc. on the component.  Whenever possible,
     *     property values initialized by the client program should <i>not</i>
     *     be overridden.
     * </li><li>Install a <code>LayoutManager</code> on the component if necessary.
     * </li><li>Create/add any required sub-components to the component.
     * </li><li>Create/install event listeners on the component.
     * </li><li>Create/install a <code>PropertyChangeListener</code> on the component in order
     *     to detect and respond to component property changes appropriately.
     * </li><li>Install keyboard UI (mnemonics, traversal, etc.) on the component.
     * </li><li>Initialize any appropriate instance data.
     * </li></ol>
     */
    public void installUI( JComponent c ) {
        lineRendererPane = ( LineRendererPane ) c;

        installDefaults();
        installComponents();
        installListeners();
    }

    /**
     * Install default values for colors, fonts, borders, etc.
     */
    public void installDefaults() {
        lineRendererPane.setLayout( createLayoutManager() );
        lineRendererPane.setBorder( new EmptyBorder( 1, 1, 1, 1 ) );
    }

    /**
     * Create and install any sub-components.
     */
    public void installComponents() {
        lineRenderer = new LineRenderer( );
        scrollPane = new JScrollPane( lineRenderer );
        lineRendererPane.add( scrollPane, BorderLayout.CENTER );
    }

    /**
     * Install any action listeners, mouse listeners, etc.
     */
    public void installListeners() {
        lineRendererPane.addChangeListener( this );
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();

        lineRendererPane = null;
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallDefaults() {}

    /**
     * Tear down and clean up.
     */
    public void uninstallComponents() {
        lineRenderer = null;
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallListeners() {
        lineRendererPane.removeChangeListener( this );
    }

    public void stateChanged( ChangeEvent event ) {
        // paint lines or clear, depends on the view and the model.
        lineRenderer.repaint();
        if ( lineRendererPane.getModel() != null ) {
            // auto scroll so first diff is visible
            String leftLine = lineRendererPane.getModel().getLeftLine();
            String rightLine = lineRendererPane.getModel().getRightLine();
            String longLine = leftLine.length() > rightLine.length() ? leftLine : rightLine;
            String shortLine = leftLine.length() <= rightLine.length() ? leftLine : rightLine;
            int offset = shortLine.length();
            for ( int i = 0; i < shortLine.length(); i++ ) {
                if ( shortLine.charAt( i ) != longLine.charAt( i ) ) {
                    offset = i;
                    break;
                }
            }
            int max_length = longLine.length();
            float percent = ( float ) offset / ( float ) max_length;
            final int vp_offset = ( int ) ( ( float ) scrollPane.getViewport().getViewRect().width * percent );
            Point p = scrollPane.getViewport().getViewPosition();
            p.x = vp_offset;
            scrollPane.getViewport().setViewPosition( p );
        }
    }

    /**
     * @return a BorderLayout
     */
    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    /**
     * Panel to display line differences.  A single line from the left text area is
     * show directly above the corresponding line from the right text area.  Both
     * lines are color coded to highlight any differences between them, which makes
     * it easy to spot differences.
     */
    public class LineRenderer extends JPanel {

        private int leftMargin = 6;

        private int preferredWidth = 600;
        private int preferredHeight = 100;
        private Dimension minimumSize = new Dimension( 600, 100 );

        public LineRenderer( ) {
            setBorder( BorderFactory.createLineBorder( Color.black ) );
        }

        /**
         * @return 600 x 100
         */
        public Dimension getPreferredSize() {
            return new Dimension( preferredWidth, preferredHeight );
        }

        public Dimension getMinimumSize() {
            return minimumSize;
        }

        private boolean isSplit() {
            if ( lineRendererPane.getView() == null ) {
                return false;
            }
            return lineRendererPane.getView().getEditPanes().length == 2;
        }

        public void paintComponent( Graphics gfx ) {

            // suggest anti-aliasing for the font display.  This is for Java 1.5,
            // jEdit also allows subpixel anti-alias, but that's a 1.6 thing and
            // would require reflection
            if ( !"none".equals( jEdit.getProperty( "view.antiAlias" ) ) ) {
                ( ( java.awt.Graphics2D ) gfx ).setRenderingHint(
                    java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                );
            }
            super.paintComponent( gfx );

            // clear the display area
            Rectangle all = getBounds();
            gfx.setColor( lineRendererPane.getBackground() );
            gfx.fillRect( 0, 0, all.width, all.height );

            model = lineRendererPane.getModel();
            if ( model == null ) {
                return ;
            }
            
            if ( !isSplit() ) {
                return ;
            }

            String leftLine = model.getLeftLine();
            String rightLine = model.getRightLine();

            // set up the font
            Font font = lineRendererPane.getFont();
            gfx.setFont( font );
            FontMetrics fm = gfx.getFontMetrics();

            preferredHeight = 8 * fm.getHeight();

            // draw the characters, left line above the right line
            // draw "Left" and bounding line with start and end ticks
            int x = leftMargin;
            gfx.setColor( fgColor );
            gfx.drawString( "Left", x, fm.getHeight() );
            int left_width = fm.stringWidth( leftLine );
            int tick_height = fm.getHeight() / 2;
            int y = fm.getHeight() + tick_height;
            gfx.drawLine( x, y, x + left_width, y );
            gfx.drawLine( x, y, x, y + tick_height );
            gfx.drawLine( x + left_width, y, x + left_width, y + tick_height );
            preferredWidth = Math.max( minimumSize.width, x + left_width + leftMargin );

            // draw text of left line
            Color color;
            x = leftMargin;
            int y0 = 3 * fm.getHeight();
            int y1 = y0 + fm.getHeight() + 3;
            java.util.List<Character> leftChars = model.getLeftCharacters();
            java.util.List<Color> leftColors = model.getLeftColors();
            for ( int i = 0; i < leftChars.size(); i++ ) {
                char c = leftChars.get( i );
                if (c == '\n') {
                    c = 'n';
                }
                if (c == '\r') {
                    c = 'r';
                }
                color = leftColors.get( i );
                gfx.setColor( color );
                gfx.fillRect( x, y0 - fm.getHeight() + fm.getDescent(), fm.charWidth( c ), fm.getHeight() );
                gfx.setColor( fgColor );
                gfx.drawString( String.valueOf( c ), x, y0 );
                x += fm.charWidth( c );
            }

            // draw text of right line
            x = leftMargin;
            java.util.List<Character> rightChars = model.getRightCharacters();
            java.util.List<Color> rightColors = model.getRightColors();
            for ( int i = 0; i < rightChars.size(); i++ ) {
                char c = rightChars.get( i );
                if (c == '\n') {
                    c = 'n';
                }
                if (c == '\r') {
                    c = 'r';
                }
                color = rightColors.get( i );
                gfx.setColor( color );
                gfx.fillRect( x, y1 - fm.getHeight() + fm.getDescent(), fm.charWidth( c ), fm.getHeight() );
                gfx.setColor( fgColor );
                gfx.drawString( String.valueOf( c ), x, y1 );
                x += fm.charWidth( c );
            }

            // draw "Right" and bounding line with start and end ticks
            x = leftMargin;
            gfx.setColor( fgColor );
            gfx.setFont( font );
            gfx.drawString( "Right", x, 6 * fm.getHeight() );
            int right_width = fm.stringWidth( rightLine );
            y = y1 + ( fm.getHeight() / 2 );
            gfx.drawLine( x, y, x + right_width, y );
            gfx.drawLine( x, y, x, y - tick_height );
            gfx.drawLine( x + right_width, y, x + right_width, y - tick_height );
            preferredWidth = Math.max( preferredWidth, x + right_width + leftMargin );

            scrollPane.revalidate();
        }
    }
}