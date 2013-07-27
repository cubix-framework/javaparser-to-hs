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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.View;

import ise.plugin.svn.data.DiffData;
import ise.java.awt.KappaLayout;
import ise.plugin.svn.gui.component.*;
import org.gjt.sp.jedit.jEdit;


public class DiffDialog extends JDialog {

    private String path1 = null;
    private String path2 = null;
    private DiffData data = null;

    /**
     * @param view the parent frame
     * @param path the local working file to diff against.
     */
    public DiffDialog( View view, String path ) {
        super( ( JFrame ) view, jEdit.getProperty("ips.Diff", "Diff"), true );
        DiffData data = new DiffData();
        data.addPath( path );
        data.setPathsAreURLs(false);
        path1 = path;
        this.data = data;
        init();
    }

    public DiffDialog( View view, DiffData data ) {
        super( ( JFrame ) view, "Diff", true );
        this.data = data;
        init();
    }

    private void init() {
        if ( data == null ) {
            throw new IllegalArgumentException( "data may not be null" );
        }

        JPanel panel = new JPanel( new KappaLayout() );
        panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        final RevisionSelectionPanel rsp = new RevisionSelectionPanel( path1 != null ? jEdit.getProperty("ips.Diff_Against>", "Diff Against:") : jEdit.getProperty("ips.Diff_Multiple_Files_Against>", "Diff Multiple Files Against:") );

        final JCheckBox svn_diff = new JCheckBox( jEdit.getProperty("ips.Create_SVN_diff", "Create SVN diff") );
        if ( data.getRecursive() || data.getSvnDiff() ) {
            // if recursive or svn diff, only possible to do an svn diff, so
            // must select and disable the checkbox
            svn_diff.setSelected( true );
            svn_diff.setEnabled( false );
        }
        svn_diff.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    data.setSvnDiff( svn_diff.isSelected() );
                }
            }
        );

        // create button panel
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( jEdit.getProperty("ips.Ok", "Ok") );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        JButton cancel_btn = new JButton( jEdit.getProperty("ips.Cancel", "Cancel") );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        // add button actions
        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // get revision of remote file to diff against
                        data.setRevision1( rsp.getRevision() );
                        DiffDialog.this.setVisible( false );
                        DiffDialog.this.dispose();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // user canceled, set data to null to signal as much.
                        data = null;
                        DiffDialog.this.setVisible( false );
                        DiffDialog.this.dispose();
                    }
                }
                                    );

        // layout main panel
        if ( path1 != null ) {
            // if just 1 file, display the name of the file
            JPanel file_panel = new JPanel( new BorderLayout() );
            file_panel.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), jEdit.getProperty("ips.File_to_diff>", "File to diff:") ) );
            file_panel.add( new JLabel( path1 ), BorderLayout.CENTER );
            panel.add( file_panel, "0, 0, 1, 1, W" );
            panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 1, 1, 1" );
        }
        panel.add( rsp, "0, 2, 1, 1, W, w" );
        panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 3, 1, 1" );
        panel.add( svn_diff, "0, 4, 1, 1, W, w" );
        panel.add( KappaLayout.createVerticalStrut( 11 ), "0, 5, 1, 1" );
        panel.add( btn_panel, "0, 6, 1, 1, E" );
        setContentPane( panel );
        pack();
        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    public DiffData getData() {
        return data;
    }
}