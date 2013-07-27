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

import java.io.File;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.*;
import org.gjt.sp.jedit.gui.HistoryTextField;

import ise.java.awt.*;
import ise.plugin.svn.data.*;
import ise.plugin.svn.command.*;
import ise.plugin.svn.library.*;
import static ise.plugin.svn.gui.HistoryModelNames.*;
import ise.plugin.svn.gui.component.*;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;

public class SwitchDialog extends JDialog {
    // instance fields
    private View view = null;
    private String path = null;
    private HistoryTextField from = null;

    private SVNURL from_url = null;
    private SVNRevision revision = SVNRevision.HEAD;

    private boolean canceled = false;

    private UpdateData data = null;

    /**
     * @param view the parent frame
     * @param files the files to replace
     */
    public SwitchDialog( View view, UpdateData data ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Switch", "Switch" ), true );
        if ( data == null ) {
            throw new IllegalArgumentException( "no source file(s)" );
        }
        List<String> paths = data.getPaths();
        if ( paths.size() != 1 ) {
            String msg = jEdit.getProperty( "ips.Switch_can_only_be_applied_to_one_file_or_directory_at_a_time.", "Switch can only be applied to one file or directory at a time." );
            JOptionPane.showMessageDialog( view, msg, jEdit.getProperty( "ips.Switch_Error", "Switch Error" ), JOptionPane.ERROR_MESSAGE );
            throw new IllegalArgumentException( msg );
        }
        this.view = view;
        this.data = data;
        path = paths.get( 0 );
        if ( path == null ) {
            throw new IllegalArgumentException( "no source file" );
        }
        init();
    }

    protected void init() {
        KappaLayout layout = new KappaLayout();
        JPanel panel = new JPanel( layout );
        panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

        // set recursive value, if any of the nodes are a directory, set
        // recursive to true.  While we're at it, make a list of strings of
        // the node paths.
        boolean recursive = false;
        File file = new File( path );
        if ( file.isDirectory() ) {
            recursive = true;
        }
        List<String> paths = new ArrayList<String>();
        paths.add( path );
        data.setPaths( paths );
        data.setRecursive( recursive );

        // source for switch
        JLabel to_replace_label = new JLabel( jEdit.getProperty( "ips.Replace", "Replace" ) + " " + ( recursive ? jEdit.getProperty( "ips.the_files_in_this_directory>", "the files in this directory:" ) : jEdit.getProperty( "ips.this_file>", "this file:" ) ) );
        HistoryTextField to_replace_file = new HistoryTextField( PATH );
        to_replace_file.setText( path );
        to_replace_file.setEditable( false );

        final JCheckBox recursive_cb = new JCheckBox( jEdit.getProperty( "ips.Recursively_switch?", "Recursively switch?" ) );
        recursive_cb.setSelected( recursive );
        recursive_cb.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        data.setRecursive( recursive_cb.isSelected() );
                    }
                }
                                      );

        // revision selection panel
        final RevisionSelectionPanel revision_panel = new RevisionSelectionPanel( jEdit.getProperty( "ips.At_this_revision>", "At this revision:" ), SwingConstants.HORIZONTAL, false );

        // destination
        JLabel path_label = new JLabel( jEdit.getProperty( "ips.With_file(s)_from_this_location>", "With file(s) from this location:" ) );
        from = new HistoryTextField( COPY_PATH );
        from.setText( "" );
        from.setColumns( 30 );
        JButton browse_remote_btn = new JButton( jEdit.getProperty( "ips.Browse_Remote...", "Browse Remote..." ) );
        browse_remote_btn.setMnemonic( KeyEvent.VK_R );
        browse_remote_btn.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    final JDialog dialog = new JDialog( view, jEdit.getProperty( "ips.Select_Repository_Destination", "Select Repository Destination" ) );
                    dialog.setModal( true );
                    JPanel panel = new JPanel( new LambdaLayout() );
                    panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
                    final BrowseRepositoryPanel burp = new BrowseRepositoryPanel( view, false );
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
                                    from.setText( selection );
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
                        try {
                            from_url = SVNURL.parseURIDecoded( from.getText() );
                        }
                        catch ( Exception e ) {
                            JOptionPane.showMessageDialog( SwitchDialog.this, jEdit.getProperty( "ips.Destination_URL_is_invalid.", "Destination URL is invalid." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                            return ;
                        }
                        revision = revision_panel.getRevision();
                        canceled = false;
                        SwitchDialog.this.setVisible( false );
                        SwitchDialog.this.dispose();
                        from.addCurrentToHistory();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        canceled = true;
                        SwitchDialog.this.setVisible( false );
                        SwitchDialog.this.dispose();
                    }
                }
                                    );


        // add the components to the option panel
        panel.add( "0, 0, 8, 1, W, w, 3", to_replace_label );
        panel.add( "0, 1, 8, 1, W, w, 3", to_replace_file );
        panel.add( "0, 2, 8, 1, W, w, 3", recursive_cb );
        panel.add( "0, 3, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 4, 1, 1, W,  , 3", path_label );
        panel.add( "0, 5, 8, 1, 0, w, 3", from );
        panel.add( "0, 6, 1, 1, 0, w, 3", browse_remote_btn );
        panel.add( "0, 7, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 8, 8, 1, 0, w, 3", revision_panel );
        panel.add( "0, 9, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 10, 8, 1, E,  , 0", btn_panel );

        setContentPane( panel );
        pack();

        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    public UpdateData getData() {
        if ( canceled ) {
            return null;
        }
        data.setSVNRevision( revision );
        data.setURL( from_url );
        return data;
    }
}