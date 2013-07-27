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
import javax.swing.table.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.EmptyBorder;
import ise.java.awt.LambdaLayout;
import ise.plugin.svn.action.*;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.data.UpdateData;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * Shows the results of an update.  Conflicted files, updated files,
 * added files, and deleted files are shown in separate tables.
 */
public class UpdateResultsPanel extends JPanel {

    private View view = null;
    private UpdateData data = null;

    public UpdateResultsPanel( View view, UpdateData results ) {
        this( view, results, false );
    }

    public UpdateResultsPanel( View view, UpdateData results, boolean isExport ) {
        super( new BorderLayout() );
        this.view = view;
        this.data = results;

        setBorder( new EmptyBorder( 3, 3, 3, 3 ) );

        JPanel contentPanel = new JPanel(new LambdaLayout());
        JLabel label;
        if ( isExport ) {
            label = new JLabel( jEdit.getProperty("ips.Exported_at_revision>", "Exported at revision:") + " " + results.getRevision() );
        }
        else {
            label = new JLabel( jEdit.getProperty("ips.Updated_to_revision>", "Updated to revision:") + " " + results.getRevision() );
        }
        add( label, BorderLayout.NORTH );
        
        LambdaLayout.Constraints con = LambdaLayout.createConstraint();
        con.a = LambdaLayout.W;
        con.y = 0;
        con.s = "wh";
        con.p = 3;
        
        
        
        boolean added = false;

        List<String> list = results.getConflictedFiles();
        if ( list != null ) {
            int size = list.size();
            ++con.y;
            contentPanel.add( createPanel( size + " " + jEdit.getProperty("ips.file", "file") + ( size != 1 ? "s" : "" ) + " " + jEdit.getProperty("ips.with_conflicts>", "with conflicts:"), list ), con );
            added = true;
        }

        list = results.getUpdatedFiles();
        if ( list != null ) {
            int size = list.size();
            ++con.y;
            contentPanel.add( createPanel( jEdit.getProperty("ips.Updated", "Updated") + " " + size + " " + jEdit.getProperty("ips.file", "file") + ( size != 1 ? "s" : "" ) + ":", list ), con );
            added = true;
        }

        list = results.getAddedFiles();
        if ( list != null ) {
            int size = list.size();
            ++con.y;
            if ( isExport ) {
                contentPanel.add( createPanel( jEdit.getProperty("ips.Exported", "Exported") + " " + size + " " + jEdit.getProperty("ips.file", "file") + ( size != 1 ? "s" : "" ) + ":", list ), con );
            }
            else {
                contentPanel.add( createPanel( jEdit.getProperty("ips.Added", "Added") + " " + size + " " + jEdit.getProperty("ips.file", "file") + ( size != 1 ? "s" : "" ) + ":", list ), con );
            }
            added = true;
        }

        list = results.getDeletedFiles();
        if ( list != null ) {
            int size = list.size();
            ++con.y;
            contentPanel.add( createPanel( jEdit.getProperty("ips.Deleted", "Deleted") + " " + size + " " + jEdit.getProperty("ips.file", "file") + ( size != 1 ? "s" : "" ) + ":", list ), con );
            added = true;
        }

        if ( !added ) {
            label.setText( label.getText() + " " + jEdit.getProperty("ips.(Already_up_to_date.)", "(Already up to date.)") );
        }
        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createPanel( String title, List<String> values ) {
        JLabel label = new JLabel( title );
        String[][] data = new String[ values.size() ][ 1 ];
        for ( int i = 0; i < values.size(); i++ ) {
            data[ i ][ 0 ] = values.get( i );
        }
        JTable table = new JTable( data, new String[] {jEdit.getProperty("ips.Path>", "Path:")} );
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBorder( new EtchedBorder() );
        panel.add( label, BorderLayout.NORTH );
        panel.add( GUIUtils.createTablePanel( table ), BorderLayout.CENTER );
        table.addMouseListener(new TableMouseListener(table));
        return panel;
    }

    /**
     * MouseListener to popup context menu on the tree.
     */
    class TableMouseListener extends MouseAdapter {

        private JTable table = null;

        public TableMouseListener( JTable table ) {
            this.table = table;
        }

        public void mouseReleased( MouseEvent me ) {
            handleClick( me );
        }

        public void mousePressed( MouseEvent me ) {
            handleClick( me );
        }

        private void handleClick( MouseEvent me ) {
            if ( me.isPopupTrigger() ) {
                if ( table.getSelectedRows().length == 0 ) {
                    int row = table.rowAtPoint( me.getPoint() );
                    int col = table.columnAtPoint( me.getPoint() );
                    table.setRowSelectionInterval( row, row );
                    table.setColumnSelectionInterval( col, col );
                }
                GUIUtils.showPopupMenu( createPopupMenu( table ), table, me.getX(), me.getY() );
            }
            else if ( me.getClickCount() == 2 ) {
                // for double-click on a text file, open the file in jEdit
                int row = table.rowAtPoint( me.getPoint() );
                String path = ( String ) table.getValueAt( row, 0 );
                if ( path == null || path.length() == 0 ) {
                    return ;
                }
                jEdit.openFile( view, path );
            }
        }
    }

    /**
     * Create the context menu.
     */
    private JPopupMenu createPopupMenu( final JTable table ) {
        // update, commit, revert, add, log, need to add others as appropriate
        final JPopupMenu pm = new JPopupMenu();

        JMenuItem mi = new JMenuItem( jEdit.getProperty("ips.Info", "Info") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        int[] rows = table.getSelectedRows();
                        List<String> paths = new ArrayList<String>();
                        for ( int row : rows ) {
                            String path = ( String ) table.getValueAt( row, 0 );
                            if ( path == null || path.length() == 0 ) {
                                continue ;
                            }
                            paths.add( path );
                        }
                        InfoAction action = new InfoAction( view, paths, data.getUsername(), data.getPassword() );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Log", "Log") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        int[] rows = table.getSelectedRows();
                        List<String> paths = new ArrayList<String>();
                        for ( int row : rows ) {
                            String path = ( String ) table.getValueAt( row, 0 );
                            if ( path == null || path.length() == 0 ) {
                                continue ;
                            }
                            paths.add( path );
                        }
                        LogAction action = new LogAction( view, paths, data.getUsername(), data.getPassword() );
                        action.actionPerformed( ae );
                    }
                }
                            );
        mi = new JMenuItem( jEdit.getProperty("ips.Resolve_Conflicts", "Resolve Conflicts") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        int[] rows = table.getSelectedRows();
                        if ( rows.length > 1 ) {
                            JOptionPane.showMessageDialog( view, jEdit.getProperty("ips.Please_select_one_file_at_a_time.", "Please select one file at a time."), jEdit.getProperty("ips.Error", "Error"), JOptionPane.ERROR_MESSAGE );
                            return ;
                        }
                        String path = ( String ) table.getValueAt( rows[0], 0 );
                        ResolveConflictDialog dialog = new ResolveConflictDialog( view, path );
                        dialog.setVisible( true );
                    }
                }
                            );
        return pm;
    }
}