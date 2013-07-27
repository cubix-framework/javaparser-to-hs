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

public class BasicDiffLineOverviewUI extends DiffLineOverviewUI implements ChangeListener {

    private DiffLineOverview diffLineOverview = null;
    private MergeToolBar mergeToolBar = null;
    private LineRendererPane lineRendererPane = null;

    /**
     * Required by super class.
     * @param c not used
     * @return one of these
     */
    public static ComponentUI createUI( JComponent c ) {
        return new BasicDiffLineOverviewUI();
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
        diffLineOverview = ( DiffLineOverview ) c;

        installDefaults();
        installComponents();
        installListeners();
    }

    /**
     * Install default values for colors, fonts, borders, etc.
     */
    public void installDefaults() {
        diffLineOverview.setLayout( createLayoutManager() );
        diffLineOverview.setBorder( new EmptyBorder( 1, 1, 1, 1 ) );
    }

    /**
     * Create and install any sub-components.
     */
    public void installComponents() {
        mergeToolBar = new MergeToolBar( diffLineOverview.getView() );
        lineRendererPane = new LineRendererPane( diffLineOverview.getView() );
        int orientation = jEdit.getIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.HORIZONTAL );
        boolean show_line_diff = jEdit.getBooleanProperty( "jdiff.show-line-diff", true );
        switch ( orientation ) {
            case MergeToolBar.VERTICAL:
                diffLineOverview.add( mergeToolBar, BorderLayout.NORTH );
                if ( show_line_diff ) {
                    diffLineOverview.add( lineRendererPane, BorderLayout.CENTER );
                }
                break;
            case MergeToolBar.COMPACT:
                diffLineOverview.add( mergeToolBar, BorderLayout.WEST );
                if ( show_line_diff ) {
                    diffLineOverview.add( lineRendererPane, BorderLayout.CENTER );
                }
                break;
            default:
                JPanel panel = new JPanel();
                panel.add(mergeToolBar);
                diffLineOverview.add( panel, BorderLayout.NORTH );
                if ( show_line_diff ) {
                    diffLineOverview.add( lineRendererPane, BorderLayout.CENTER );
                }
                break;
        }
        diffLineOverview.repaint();
    }

    /**
     * Install any action listeners, mouse listeners, etc.
     */
    public void installListeners() {
        diffLineOverview.addChangeListener(this);
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallDefaults() {
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallComponents() {
        diffLineOverview.removeAll( );
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallListeners() {
        diffLineOverview.removeChangeListener( this );
    }

    public void stateChanged( ChangeEvent event ) {
        uninstallUI(diffLineOverview);
        installUI(diffLineOverview);
    }

    /**
     * @return a BorderLayout
     */
    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }
}
