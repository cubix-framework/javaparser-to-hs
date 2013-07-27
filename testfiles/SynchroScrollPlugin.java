/*
 * Copyright (C) 2012 Dale Anson
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

package synchroscroll;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.ScrollListener;
import org.gjt.sp.jedit.textarea.TextArea;

import java.util.HashMap;

import javax.swing.event.EventListenerList;

/**
 * Handles adding and removing scroll handlers to the text areas in the
 * various view to facilitate synchronized scrolling between text areas.
 */
public class SynchroScrollPlugin extends EBPlugin {

    // key for component property that stores the initial top line of the text area
    static final String BASELINE = "synchroscroll.BaseLine";

    // Key for property that prevents from recursive scroll event invoking.
    // It is necessary because when you use setFirstLine method, then
    // also scroll events for TextArea is invoke ... and so on.
    // (see ScrollHandler.java -> method scrolledVertically -> Proportional mode)
    static final String ISPROCESSING = "synchroscroll.IsProcessing";


    // map to keep track of which views are currently using syncroscrolling
    private static HashMap<View, Boolean> scrollingMap = new HashMap<View, Boolean>() {
        // don't ever return null, return false instead
        @Override
        public Boolean get( Object key ) {
            if ( key == null || super.get( key ) == null ) {
                return false;
            }
            return super.get( key );
        }
    };

    // map to keep track of which views are currently diffing with JDiff
    private static HashMap<View, Boolean> diffingMap = new HashMap<View, Boolean>() {
        // don't ever return null, return false instead
        @Override
        public Boolean get( Object key ) {
            if ( key == null || super.get( key ) == null ) {
                return false;
            }
            return super.get( key );
        }
    };

    public void start() { }

    // remove scrollhandlers from all text areas
    public void stop() {
        View[] views = jEdit.getViews();
        for ( View view : views ) {
            EditPane[] editPanes = view.getEditPanes();
            for ( EditPane editPane : editPanes ) {
                TextArea textArea = editPane.getTextArea();
                removeScrollHandler( textArea );
            }
        }
        scrollingMap.clear();
        diffingMap.clear();
    }

    /**
     * Adjusts synchroscrolling based on the message:
     * <ul>
     * <li>Turns off synchroscroll for a View when the View is closed.</li>
     * <li>Adds scroller to new edit pane in View.</li>
     * <li>Turns off synchroscroll if JDiff is on.</li>
     * </ul>
     * @param message The edit bus message to take action for.
     */
    public void handleMessage( EBMessage message ) {
        if ( message instanceof ViewUpdate ) {
            ViewUpdate vu = ( ViewUpdate ) message;
            if ( vu.CLOSED.equals( vu.getWhat() ) ) {
                scrollingMap.remove( vu.getView() );
                diffingMap.remove( vu.getView() );
            }
        } else if ( message instanceof EditPaneUpdate ) {
            EditPaneUpdate epu = ( EditPaneUpdate ) message;
            if ( epu.CREATED.equals( epu.getWhat() ) ) {
                EditPane editPane = epu.getEditPane();
                View view = editPane.getView();
                TextArea textArea = editPane.getTextArea();
                removeScrollHandler( textArea );
                if ( scrollingMap.get( view ) ) {
                    addScrollHandler( view, textArea );
                }
            }
        } else {
            try {
                // turn off synchroscrolling if JDiff is diffing.
                // Sort of a hack since I don't want to have to have JDiff as a
                // dependency, either at compile time or run time.
                String classname = message.getClass().getName();
                if ( classname != null && "jdiff.DiffMessage".equals( classname ) ) {
                    String what = ( String ) PrivilegedAccessor.invokeMethod( message, "getWhat", null );
                    String on = ( String ) PrivilegedAccessor.getValue( message, "ON" );
                    View view = ( View ) message.getSource();
                    if ( what != null && what.equals( on ) ) {
                        SynchroScrollPlugin.setScrolling( view, false );
                        diffingMap.put( view, true );
                    } else {
                        diffingMap.remove( view );
                    }
                }
            } catch ( Exception e ) {                // NOPMD
                // don't worry about it
            }
        }
    }

    public static boolean getPluginEnabled(View view) {
        return scrollingMap.containsKey(view) && scrollingMap.get(view);
    }

    /**
     * Turn on sychroscrolling if it was off, turn it off if it was on
     * @param view The View to toggle synchroscrolling for.
     */
    public static void toggleSynchroScroll( View view ) {
        if ( diffingMap.get( view ) ) {
            return;            // don't do anything while JDiff is working.
        }
        boolean scrolling = !scrollingMap.get( view );
        scrollingMap.put( view, scrolling );
        setScrolling( view, scrolling );
    }

    /**
     * Turn off synchroscrolling for the given View.
     * @param view The View to stop synchroscrolling.
     */
    public static void setSynchroScrollOff( View view ) {
        setScrolling( view, false );
    }

    // turn synchroscrolling on or off
    private static void setScrolling( View view, boolean scrolling ) {
        scrollingMap.put( view, scrolling );
        EditPane[] editPanes = view.getEditPanes();
        for ( EditPane editPane : editPanes ) {
            TextArea textArea = editPane.getTextArea();
            removeScrollHandler( textArea );
            if ( scrolling ) {
                addScrollHandler( view, textArea );
            }
        }
    }

    // adds a new ScrollHandler to the given textArea
    private static void addScrollHandler( View view, TextArea textArea ) {
        ScrollHandler scrollHandler = new ScrollHandler( view );
        textArea.addScrollListener( scrollHandler );
        textArea.putClientProperty( BASELINE, textArea.getFirstLine() );
    }

    // remove all of our ScrollHandlers from the given text area. There should
    // be only one. Another use of PrivilegedAccessor since TextArea doesn't
    // have a method to get the scroll handlers directly.
    private static void removeScrollHandler( TextArea textArea ) {
        try {
            EventListenerList listenerList = ( EventListenerList ) PrivilegedAccessor.getValue( textArea, "listenerList" );
            ScrollListener[] listeners = listenerList.getListeners( ScrollListener.class );
            for ( ScrollListener listener : listeners ) {
                if ( listener instanceof ScrollHandler ) {
                    textArea.removeScrollListener( listener );
                }
            }
            textArea.putClientProperty( BASELINE, null );
        } catch ( Exception e ) {            // NOPMD
            // don't worry about it
        }
    }
}