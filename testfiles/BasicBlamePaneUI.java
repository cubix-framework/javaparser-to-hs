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

package ise.plugin.svn.gui.component;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;

import org.gjt.sp.jedit.jEdit;

/**
 * UI for the BlamePane to be displayed left of the right scroll bar of the
 * main text area.
 */
public class BasicBlamePaneUI extends BlamePaneUI implements ChangeListener, MouseListener {

    private BlamePane blamePane;
    private BlameRendererPane blameRendererPane;
    private int pixelsPerLine = 12;     // default, this will be calculated later

    public static ComponentUI createUI( JComponent c ) {
        return new BasicBlamePaneUI();
    }

    public void installUI( JComponent c ) {
        blamePane = ( BlamePane ) c;
        blamePane.setLayout( createLayoutManager() );
        installDefaults();
        installComponents();
        installListeners();

    }

    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
    }

    public void installDefaults() {}

    public void installComponents() {
        blameRendererPane = new BlameRendererPane();
        blamePane.add( blameRendererPane, BorderLayout.CENTER );
    }

    public void installListeners() {
        blamePane.addChangeListener( this );
        blamePane.addMouseListener( this );
    }

    public void uninstallDefaults() {}

    public void uninstallComponents() {
        blamePane.remove( blameRendererPane );
    }

    public void uninstallListeners() {
        blamePane.removeChangeListener( this );
    }

    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    public void stateChanged( ChangeEvent event ) {
        blameRendererPane.repaint();
    }

    /**
     * On mouse click on the BlamePane, move the cursor in the text area to the
     * corresponding line.  This makes line highlight (if turned on) line up
     * nicely with the appropriate line in the BlamePane.
     */
    public void mouseClicked( MouseEvent e ) {
        BlameModel model = blamePane.getModel();
        if ( model == null ) {
            return ;
        }
        int line_number = ( e.getY() / pixelsPerLine ) + model.getTextArea().getFirstPhysicalLine();
        model.getTextArea().setCaretPosition( model.getTextArea().getLineStartOffset( line_number ), false );
    }

    public void mouseEntered( MouseEvent e ) {}
    public void mouseExited( MouseEvent e ) {}
    public void mousePressed( MouseEvent e ) {}
    public void mouseReleased( MouseEvent e ) {}

    public class BlameRendererPane extends JPanel {
        public BlameRendererPane( ) {
            // need a model and some blame
            BlameModel model = blamePane.getModel();
            if ( model == null ) {
                throw new IllegalArgumentException( "blame model is null" );
            }
            if ( model.getBlame() == null ) {
                throw new IllegalArgumentException( "no blame found in model" );
            }

            // calculate the proper width by finding the widest line of the blame
            int max_width = 0;
            FontMetrics fm = model.getTextArea().getPainter().getFontMetrics();
            for ( String line : model.getBlame() ) {
                int width = fm.stringWidth( line );
                max_width = width > max_width ? width : max_width;
            }
            Dimension dim = getPreferredSize();
            dim.width = max_width + 3;  // 3 extra pixels just to give some separation from the text area
            setPreferredSize( dim );
        }

        public void paintComponent( Graphics gfx ) {
            super.paintComponent( gfx );

            // paint the background
            Rectangle size = getBounds();
            gfx.setColor( getBackground() );
            gfx.fillRect( 0, 0, size.width, size.height );

            // get the visible lines, draw the corresponding blame lines
            BlameModel model = blamePane.getModel();
            pixelsPerLine = model.getTextArea().getPainter().getFontMetrics().getHeight();
            int firstLine = model.getTextArea().getFirstPhysicalLine();
            int lastLine = model.getTextArea().getLastPhysicalLine();
            int caretLine = model.getTextArea().getCaretLine();
            Color foreground = jEdit.getColorProperty( "view.fgColor", Color.BLACK );
            Color highlight = jEdit.getColorProperty( "view.lineHighlightColor", Color.WHITE );
            gfx.setColor( foreground );
            java.util.List<String> blame = model.getBlame();
            int descent = gfx.getFontMetrics().getDescent();
            for ( int i = firstLine; i <= lastLine; i++ ) {
                if ( i == caretLine ) {
                    gfx.setColor( highlight );
                    gfx.fillRect( 0, ( i - firstLine ) * pixelsPerLine, size.width, pixelsPerLine );
                    gfx.setColor( foreground );
                }
                if ( i >= 0 && i < blame.size() ) {
                    gfx.drawString( blame.get( i ), 3, ( ( i - firstLine + 1 ) * pixelsPerLine ) - descent );
                }
            }
        }
    }
}