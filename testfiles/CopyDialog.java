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

// imports
import java.awt.Dimension;
import java.io.File;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.*;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.browser.VFSBrowser;

import ise.java.awt.*;
import ise.plugin.svn.data.*;
import ise.plugin.svn.command.*;
import ise.plugin.svn.library.*;
import static ise.plugin.svn.gui.HistoryModelNames.*;
import ise.plugin.svn.gui.component.*;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class CopyDialog extends JDialog {
    // instance fields
    private View view = null;
    private List<File> toCopy = null;
    private List<String> urlsToCopy = null;
    private HistoryTextField path = null;
    private TableModel fileTableModel = null;
    private JTextArea comment = null;
    private PropertyComboBox commentList = null;

    private String defaultLocalDestination = null;
    private String defaultRemoteDestination = null;
    private SVNRevision revision = SVNRevision.HEAD;

    private boolean destinationIsLocal = true;  // if true, copying to local file system

    private boolean canceled = true;

    /**
     * @param view parent frame
     * @param files the local files to copy
     * @param defaultLocalDestination local file system destination
     * @param defaultLocalDestination remote repository destination
     */
    public CopyDialog( View view, List<File> files, String defaultLocalDestination, String defaultRemoteDestination ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Copy", "Copy" ), true );
        if ( files == null || files.size() == 0 ) {
            throw new IllegalArgumentException( "no source file(s) to copy" );
        }
        this.view = view;
        this.toCopy = files;
        this.defaultLocalDestination = defaultLocalDestination == null ? "" : defaultLocalDestination;
        this.defaultRemoteDestination = defaultRemoteDestination == null ? "" : defaultRemoteDestination;
        init();
    }

    /**
     * @param view parent frame
     * @param defaultLocalDestination local file system destination
     * @param defaultLocalDestination remote repository destination
     * @param files the remote urls to copy
     */
    public CopyDialog( View view, String defaultLocalDestination, String defaultRemoteDestination, List<String> urls ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Copy", "Copy" ), true );
        if ( urls == null || urls.size() == 0 ) {
            throw new IllegalArgumentException( "no source url(s) to copy" );
        }
        this.view = view;
        this.urlsToCopy = urls;
        this.defaultLocalDestination = defaultLocalDestination == null ? "" : defaultLocalDestination;
        this.defaultRemoteDestination = defaultRemoteDestination == null ? "" : defaultRemoteDestination;
        destinationIsLocal = false;
        init();
    }

    protected void init() {
        KappaLayout layout = new KappaLayout();
        JPanel panel = new JPanel( layout );
        panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        JLabel to_copy_label = null;
        if ( toCopy != null ) {
            to_copy_label = new JLabel( jEdit.getProperty( "ips.Copy", "Copy" ) + " " + ( toCopy.size() == 1 ? jEdit.getProperty( "ips.this_file>", "this file:" ) : jEdit.getProperty( "ips.these_files>", "these files:" ) ) );
        }
        else {
            to_copy_label = new JLabel( jEdit.getProperty( "ips.Copy", "Copy" ) + " " + ( urlsToCopy.size() == 1 ? jEdit.getProperty( "ips.this_URL>", "this URL:" ) : jEdit.getProperty( "ips.these_URLs>", "these URLs:" ) ) );
        }

        BestRowTable file_table = new BestRowTable();

        // create table model
        fileTableModel = new DefaultTableModel( new String[] {
                    ( toCopy != null ? jEdit.getProperty( "ips.File", "File" ) : jEdit.getProperty( "ips.URL", "URL" ) )
                }
                , toCopy != null ? toCopy.size() : urlsToCopy.size() ) ;

        // fill table model.  If directory, add a checkbox defaulting to checked
        // indicating the copy should recursively copy the directory.
        if ( toCopy != null ) {
            for ( int row = 0; row < toCopy.size(); row++ ) {
                File file = toCopy.get( row );
                fileTableModel.setValueAt( file.getAbsolutePath(), row, 0 );
            }
        }
        else {
            for ( int row = 0; row < urlsToCopy.size(); row++ ) {
                String url = urlsToCopy.get( row );
                fileTableModel.setValueAt( url, row, 0 );
            }
        }
        file_table.setModel( fileTableModel );
        file_table.getColumnModel().getColumn( 0 ).setPreferredWidth( 500 );
        file_table.packRows();

        // revision selection panel
        final RevisionSelectionPanel revision_panel = new RevisionSelectionPanel( jEdit.getProperty( "ips.Copy_from_this_revision>", "Copy from this revision:" ), SwingConstants.HORIZONTAL, true );

        // destination
        JLabel path_label = new JLabel( jEdit.getProperty( "ips.To_this_location>", "To this location:" ) );
        path = new HistoryTextField( COPY_PATH );
        path.setText( defaultLocalDestination );
        path.setColumns( 30 );
        JButton browse_local_btn = new JButton( jEdit.getProperty( "ips.Browse_Local...", "Browse Local..." ) );
        browse_local_btn.setMnemonic( KeyEvent.VK_B );
        browse_local_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        String[] dirs = GUIUtilities.showVFSFileDialog( view, defaultLocalDestination, VFSBrowser.CHOOSE_DIRECTORY_DIALOG, false );
                        if ( dirs != null && dirs.length > 0 ) {
                            String filename = dirs[ 0 ];
                            File f = new File( filename );
                            path.setText( f.getAbsolutePath() );
                            destinationIsLocal = true;
                        }
                    }
                }
                                          );
        JButton browse_remote_btn = new JButton( jEdit.getProperty( "ips.Browse_Remote...", "Browse Remote..." ) );
        browse_remote_btn.setMnemonic( KeyEvent.VK_R );
        browse_remote_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    final JDialog dialog = new JDialog( view, jEdit.getProperty( "ips.Select_Repository_Destination", "Select Repository Destination" ) );
                    dialog.setModal( true );
                    JPanel panel = new JPanel( new LambdaLayout() );
                    panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
                    final BrowseRepositoryPanel burp = new BrowseRepositoryPanel( view, defaultRemoteDestination, false );
                    panel.add( "0, 0, 1, 1, 0, wh, 3", burp );
                    KappaLayout btn_layout = new KappaLayout();
                    JPanel button_panel = new JPanel( btn_layout );
                    JButton ok_btn = new JButton( jEdit.getProperty( "ips.Ok", "Ok" ) );
                    ok_btn.setMnemonic( KeyEvent.VK_O );
                    ok_btn.addActionListener(
                        new ActionListener() {
                            public void actionPerformed( ActionEvent ae ) {
                                String selection = burp.getSelectionPath();
                                dialog.setVisible( false );
                                dialog.dispose();
                                if ( selection != null && selection.length() > 0 ) {
                                    path.setText( selection );
                                    destinationIsLocal = false;
                                }
                            }
                        }
                    );
                    JButton cancel_btn = new JButton( jEdit.getProperty( "ips.Cancel", "Cancel" ) );
                    cancel_btn.setMnemonic( KeyEvent.VK_C );
                    cancel_btn.addActionListener(
                        new ActionListener() {
                            public void actionPerformed( ActionEvent ae ) {
                                dialog.setVisible( false );
                                dialog.dispose();
                            }
                        }
                    );
                    button_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
                    button_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
                    btn_layout.makeColumnsSameWidth( 0, 1 );

                    panel.add( "0, 1, 1, 1", KappaLayout.createStrut( 350, 11, false ) );
                    panel.add( "0, 2, 1, 1, E, , 3", button_panel );
                    dialog.setContentPane( panel );
                    dialog.pack();
                    GUIUtils.center( view, dialog );
                    dialog.getRootPane().setDefaultButton( ok_btn );
                    ok_btn.requestFocus();
                    dialog.setVisible( true );
                }
            }
        );

        JLabel comment_label = new JLabel( jEdit.getProperty( "ips.Enter_comment_for_this_copy>", "Enter comment for this copy:" ) );
        comment = new JTextArea( 3, 30 );
        comment.setLineWrap( true );
        comment.setWrapStyleWord( true );

        // list for previous comments
        commentList = new PropertyComboBox( "ise.plugin.svn.comment." );
        commentList.setEditable( false );
        commentList.addItemListener( new ItemListener() {
                    public void itemStateChanged( ItemEvent e ) {
                        if ( PropertyComboBox.SELECT.equals( commentList.getSelectedItem().toString() ) ) {
                            return ;
                        }
                        comment.setText( commentList.getSelectedItem().toString() );
                    }
                }
                                   );

        // ok and cancel buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( jEdit.getProperty( "ips.Ok", "Ok" ) );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        JButton cancel_btn = new JButton( jEdit.getProperty( "ips.Cancel", "Cancel" ) );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        if ( path == null || path.getText().length() == 0 ) {
                            JOptionPane.showMessageDialog( CopyDialog.this, jEdit.getProperty( "ips.Directory_is_required.", "Directory is required." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                            return ;
                        }
                        revision = revision_panel.getRevision();
                        canceled = false;
                        CopyDialog.this._save();
                        CopyDialog.this.setVisible( false );
                        CopyDialog.this.dispose();
                        path.addCurrentToHistory();
                        commentList.save();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        canceled = true;
                        CopyDialog.this.setVisible( false );
                        CopyDialog.this.dispose();
                    }
                }
                                    );


        // add the components to the option panel
        panel.add( "0, 0, 8, 1, W,  , 3", to_copy_label );
        JScrollPane file_scroller = new JScrollPane( file_table );
        file_scroller.getViewport().setPreferredSize( new Dimension( 500, Math.min( file_table.getBestHeight(), 50 ) ) );
        panel.add( "0, 1, 8, 1, W, w, 3", file_scroller );


        panel.add( "0, 2, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 10, true ) );

        panel.add( "0, 3, 8, 1, 0, w, 3", revision_panel );

        panel.add( "0, 4, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 10, true ) );

        panel.add( "0, 5, 1, 1, W,  , 3", path_label );
        panel.add( "0, 6, 8, 1, 0, w, 3", path );
        panel.add( "0, 7, 1, 1, 0, w, 3", browse_local_btn );
        panel.add( "1, 7, 1, 1, 0, w, 3", browse_remote_btn );
        layout.makeColumnsSameWidth( 0, 1 );

        panel.add( "0, 8, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 9, 8, 1, W,  , 3", comment_label );
        panel.add( "0, 10, 8, 1, W, wh, 3", new JScrollPane( comment ) );

        if ( commentList != null && commentList.getModel().getSize() > 0 ) {
            commentList.setPreferredSize( new Dimension( 500, commentList.getPreferredSize().height ) );
            panel.add( "0, 11, 8, 1, W,  , 3", new JLabel( jEdit.getProperty( "ips.Select_a_previous_comment>", "Select a previous comment:" ) ) );
            panel.add( "0, 12, 8, 1, W, w, 3", commentList );
        }

        panel.add( "0, 13, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 10, true ) );
        panel.add( "0, 14, 8, 1, E,  , 0", btn_panel );

        setContentPane( panel );
        pack();

        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    protected void _save() {
        String msg = comment.getText();
        if ( msg == null || msg.length() == 0 ) {
            msg = jEdit.getProperty( "ips.no_comment", "no comment" );
        }
        commentList.addValue( msg );
        commentList.save();
    }

    public CopyData getData() {
        if ( canceled ) {
            return null;
        }
        CopyData cd = new CopyData();
        if ( toCopy != null ) {
            List<File> files = new ArrayList<File>();
            for ( int row = 0; row < fileTableModel.getRowCount(); row++ ) {
                String filename = ( String ) fileTableModel.getValueAt( row, 0 );
                files.add( new File( filename ) );
            }

            cd.setSourceFiles( files );
        }
        else {
            List<SVNURL> urls = new ArrayList<SVNURL>();
            for ( int row = 0; row < fileTableModel.getRowCount(); row++ ) {
                String url = ( String ) fileTableModel.getValueAt( row, 0 );
                try {
                    SVNURL source = SVNURL.parseURIDecoded( url );
                    urls.add( source );
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }

            cd.setSourceURLs( urls );
        }

        cd.setRevision( revision );

        if ( destinationIsLocal ) {
            cd.setDestinationFile( new File( path.getText() ) );
        }
        else {
            try {
                cd.setDestinationURL( SVNURL.parseURIDecoded( path.getText() ) );
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
        }

        String msg = comment.getText();
        if ( msg == null || msg.length() == 0 ) {
            msg = jEdit.getProperty( "ips.no_comment", "no comment" );
        }
        cd.setMessage( msg );

        return cd;
    }
}