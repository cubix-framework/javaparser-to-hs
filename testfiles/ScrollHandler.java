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

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.ScrollListener;
import org.gjt.sp.jedit.textarea.TextArea;

/**
 * Synchronizes both horizontal and vertical scrolling between all text areas
 * in a View.
 */
public class ScrollHandler implements ScrollListener {

    View view;

    ScrollHandler( View view ) {
        this.view = view;
    }

    /**
     * Synchronizes horizontal scrolling between all text areas in the View.
     * @param textArea The text area doing the scrolling.
     */
    public void scrolledHorizontally( TextArea textArea ) {
        int offset = textArea.getHorizontalOffset();
        EditPane[] editPanes = view.getEditPanes();
        for ( EditPane editPane : editPanes ) {
            TextArea other = editPane.getTextArea();
            other.setHorizontalOffset( offset );
        }
    }

    /**
     * Synchronizes vertical scrolling between all text areas in the View.
     * @param textArea The text area doing the scrolling.
     */
    public void scrolledVertically( TextArea textArea ) {
        int firstLine = textArea.getFirstLine();
        Integer BL = ( Integer ) textArea.getClientProperty( SynchroScrollPlugin.BASELINE );
        int baseline = BL == null ? 0 : BL.intValue();
        int offset = firstLine - baseline;
        EditPane[] editPanes = view.getEditPanes();

        Boolean standardMode = jEdit.getBooleanProperty( "options.SynchroScrollPluginPane.standardMode.value" );

        if ( standardMode ) {
            for ( EditPane editPane : editPanes ) {
                TextArea other = editPane.getTextArea();
                Integer otherBL = ( Integer ) other.getClientProperty( SynchroScrollPlugin.BASELINE );
                int otherBaseline = otherBL == null ? 0 : otherBL.intValue();
                int otherOffset = otherBaseline + offset;
                otherOffset = otherOffset < 0 ? 0 : otherOffset;
                otherOffset = otherOffset >= other.getLineCount() ? other.getLineCount() - 1 : otherOffset;
                other.setFirstLine( otherOffset );
            }
        } else {
            // Proportional mode

            // Prevents recursive scroll event invoking
            if ( jEdit.getBooleanProperty( SynchroScrollPlugin.ISPROCESSING ) == false ) {
                jEdit.setBooleanProperty( SynchroScrollPlugin.ISPROCESSING, true );

                float sourceNormalizedScrollPosition = ( float ) firstLine / ( float ) ( textArea.getLineCount() - textArea.getVisibleLines() + 1 );

                for ( EditPane otherEditPane : editPanes ) {
                    TextArea otherTextArea = otherEditPane.getTextArea();
                    int otherNewFirstLine = ( int ) ( sourceNormalizedScrollPosition * ( otherTextArea.getLineCount() - otherTextArea.getVisibleLines() + 1 ) );
                    otherTextArea.setFirstLine( otherNewFirstLine );
                }
                jEdit.setBooleanProperty( SynchroScrollPlugin.ISPROCESSING, false );
            }
        }
    }
}