/*
Copyright (c) 2007, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ise.plugin.svn.gui;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import ise.plugin.svn.action.CommitAction;
import ise.plugin.svn.action.RevertAction;
import ise.plugin.svn.data.AddResults;
import ise.plugin.svn.data.DeleteResults;
import ise.plugin.svn.library.GUIUtils;
import ise.java.awt.LambdaLayout;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

/**
 * Used for both Add and Revert, and now Delete and Resolved, and lock, unlock
 * and remote delete.
 */
public class AddResultsPanel extends JPanel {
    public static final int ADD = 0;
    public static final int REVERT = 1;
    public static final int DELETE = 2;
    public static final int RESOLVED = 3;
    public static final int LOCK = 4;
    public static final int UNLOCK = 5;
    public static final int REMOTE_DELETE = 6;

    private View view = null;
    private int action;
    private String username = null;
    private String password = null;

    public AddResultsPanel( AddResults results, int action, View view, String username, String password ) {
        super( new LambdaLayout() );
        if ( action < 0 || action > 6 ) {
            throw new IllegalArgumentException( "invalid action: " + action );
        }
        this.view = view;
        this.action = action;
        this.username = username;
        this.password = password;

        boolean top = false;    // indicate good messages are displayed
        LambdaLayout.Constraints con = LambdaLayout.createConstraint();
        con.a = LambdaLayout.W;
        con.s = "wh";
        con.p = 3;

        // show paths scheduled for add
        List<String> paths = results.getPaths();
        if ( paths != null && paths.size() > 0 ) {
            JPanel top_panel = new JPanel( new BorderLayout() );
            String good_label_text = "";
            switch ( action ) {
                case ADD:
                    good_label_text = jEdit.getProperty( "ips.Scheduled_for_add", "Scheduled for add" ) + ":";
                    break;
                case REVERT:
                    good_label_text = jEdit.getProperty( "ips.Reverted", "Reverted" ) + ":";
                    break;
                case DELETE:
                    good_label_text = jEdit.getProperty( "ips.Scheduled_for_delete", "Scheduled for delete" ) + ":";
                    break;
                case RESOLVED:
                    good_label_text = jEdit.getProperty( "ips.Resolved", "Resolved" ) + ":";
                    break;
                case LOCK:
                    good_label_text = jEdit.getProperty( "ips.Locked", "Locked" ) + ":";
                    break;
                case UNLOCK:
                    good_label_text = jEdit.getProperty( "ips.Unlocked", "Unlocked" ) + ":";
                    break;
                case REMOTE_DELETE:
                    DeleteResults dr = ( DeleteResults ) results;
                    good_label_text = jEdit.getProperty( "ips.Deleted_from_repository,_new_revision", "Deleted from repository, new revision" ) + " " + dr.getRevision() + ":";
                    break;
            }
            JLabel good_label = new JLabel( good_label_text );

            // data to display in a table, single column
            String[][] data = new String[ paths.size() ][ 1 ];
            Iterator it = paths.iterator();
            for ( int i = 0; it.hasNext(); i++ ) {
                String path = ( String ) it.next();
                data[ i ][ 0 ] = path;
            }

            // create the table, one column to contain the filename
            JTable good_table = new JTable( data, new String[] {jEdit.getProperty( "ips.Path", "Path" ) } );
            good_table.addMouseListener( new TableMouseListener( good_table ) );

            top_panel.add( good_label, BorderLayout.NORTH );
            top_panel.add( GUIUtils.createTablePanel( good_table ), BorderLayout.CENTER );
            add( top_panel, con );
            top = true;
        }

        // show paths that had a problem
        Map<String, String> error_map = results.getErrorPaths();
        if ( error_map != null && error_map.size() > 0 ) {
            JPanel bottom_panel = new JPanel( new BorderLayout() );
            String bad_label_text = "";
            switch ( action ) {
                case ADD:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_schedule_for_add", "Unable to schedule for add" ) + ":";
                    break;
                case REVERT:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_revert", "Unable to revert" ) + ":";
                    break;
                case DELETE:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_schedule_for_delete", "Unable to schedule for delete" ) + ":";
                    break;
                case RESOLVED:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_resolve", "Unable to resolve" ) + ":";
                    break;
                case LOCK:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_lock", "Unable to lock" ) + ":";
                    break;
                case UNLOCK:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_unlock", "Unable to unlock" ) + ":";
                    break;
                case REMOTE_DELETE:
                    bad_label_text = jEdit.getProperty( "ips.Unable_to_delete_from_repository", "Unable to delete from repository" ) + ":";
                    break;
            }
            JLabel bad_label = new JLabel( bad_label_text );

            String[][] data = new String[ error_map.size() ][ 2 ];
            Set < Map.Entry < String, String >> set = error_map.entrySet();
            int i = 0;
            for ( Map.Entry entry : set ) {
                String path = ( String ) entry.getKey();
                String msg = ( String ) entry.getValue();
                data[ i ][ 0 ] = path;
                data[ i ][ 1 ] = msg;
                ++i;
            }

            JTable bad_table = new JTable( data, new String[] {jEdit.getProperty( "ips.Path", "Path" ), jEdit.getProperty( "ips.Error_Message", "Error Message" ) } );

            if ( top ) {
                ++con.y;
                add( LambdaLayout.createVerticalStrut( 6, true ), con );
            }
            bottom_panel.add( bad_label, BorderLayout.NORTH );
            bottom_panel.add( GUIUtils.createTablePanel( bad_table ), BorderLayout.CENTER );
            add( bottom_panel, con );
        }

    }

    /**
     * MouseListener to popup context menu on the table.
     */
    class TableMouseListener extends MouseAdapter {
        private JTable table = null;
        public TableMouseListener( JTable table ) {
            TableMouseListener.this.table = table;
        }

        public void mouseReleased( MouseEvent me ) {
            handleClick( me );
        }

        public void mousePressed( MouseEvent me ) {
            handleClick( me );
        }

        private void handleClick( MouseEvent me ) {
            if ( me.isPopupTrigger() && ( AddResultsPanel.this.action == ADD || AddResultsPanel.this.action == DELETE ) ) {
                JPopupMenu popup = getPopupMenu( table );
                if ( popup != null ) {
                    GUIUtils.showPopupMenu( popup, table, me.getX(), me.getY() );
                }
            }
            else if ( me.getClickCount() == 2 ) {
                // on double-click, open file in jEdit
                String filename = ( String ) table.getValueAt( table.getSelectedRow(), table.getSelectedColumn() );
                jEdit.openFile( view, filename );
            }
        }
    }

    /**
     * Create the context menu.
     */
    private JPopupMenu getPopupMenu( final JTable table ) {
        int[] rows = table.getSelectedRows();
        if ( rows.length == 0 ) {
            return null;
        }

        JPopupMenu popup = new JPopupMenu();
        TreeMap<String, String> paths = new TreeMap<String, String>();
        for ( int row : rows ) {
            paths.put( ( String ) table.getValueAt( rows[ row ], 0 ), "" );
        }

        JMenuItem mi = new JMenuItem( jEdit.getProperty( "ips.Commit", "Commit" ) );
        popup.add( mi );
        mi.addActionListener( new CommitAction( view, paths, username, password ) );

        mi = new JMenuItem( jEdit.getProperty( "ips.Revert", "Revert" ) );
        popup.add( mi );
        ArrayList<String> files = new ArrayList<String>( paths.keySet() );
        mi.addActionListener( new RevertAction( view, files, username, password ) );

        return popup;
    }
}