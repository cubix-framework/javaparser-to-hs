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
import org.gjt.sp.jedit.jEdit;

import ise.plugin.svn.data.DiffData;
import ise.java.awt.KappaLayout;
import ise.plugin.svn.gui.component.*;

/**
 * Dialog to allow the user to select the revisions for 1 or 2 remote files to
 * diff.
 */
public class RemoteDiffDialog extends JDialog {

    private DiffData data = null;

    /**
     * @param view the parent frame
     * @param data contains the paths to be diffed.
     */
    public RemoteDiffDialog( View view, final DiffData dd ) {
        super( ( JFrame ) view, jEdit.getProperty("ips.Diff", "Diff"), true );
        if ( dd == null ) {
            throw new IllegalArgumentException( "data may not be null" );
        }
        this.data = dd;

        // do some validation on the paths
        java.util.List<String> paths = data.getPaths();
        String path1 = paths.get( 0 );
        String path2 = paths.get( 1 );

        // validate at least first path is present
        int count = 0;
        if ( path1 == null || path1.length() == 0 ) {
            throw new IllegalArgumentException( "cannot compare empty path" );
        }
        count = 1;
        if ( path2 != null && path2.length() > 0 ) {
            count = 2;
        }
        else {
            // comparing 2 revisions of same file, so put first filename in
            // the place for the 2nd file name
            paths.add( 1, path1 );
        }

        // set up the main panel.  Layout is:
        // path1
        // revision selection panel for path1
        // path2 (if count == 2)
        // revision selection panel for path2 (if count == 2, else 2nd revision for path1)
        // button panel
        JPanel panel = new JPanel( new KappaLayout() );
        panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        // path1 display and revision chooser
        JPanel file_panel1 = new JPanel( new BorderLayout() );
        file_panel1.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), jEdit.getProperty("ips.File_to_diff>", "File to diff:") ) );
        file_panel1.add( new JLabel( path1 ), BorderLayout.CENTER );
        final RevisionSelectionPanel rsp1 = new RevisionSelectionPanel( count == 1 ? jEdit.getProperty("ips.Diff_Revision_1>", "Diff Revision 1:") : jEdit.getProperty("ips.Diff_Revision>", "Diff Revision:") );

        // path2 display and revision chooser
        JPanel file_panel2 = null;
        if ( count == 2 ) {
            file_panel2 = new JPanel( new BorderLayout() );
            file_panel2.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), jEdit.getProperty("ips.File_to_diff>", "File to diff:") ) );
            file_panel2.add( new JLabel( path2 ), BorderLayout.CENTER );
        }
        final RevisionSelectionPanel rsp2 = new RevisionSelectionPanel( count == 1 ? jEdit.getProperty("ips.Diff_Revision_2", "Diff Revision 2") : jEdit.getProperty("ips.Diff_Revision>", "Diff Revision:") );

        // svn diff?
        final JCheckBox svn_diff = new JCheckBox(jEdit.getProperty("ips.Create_SVN_diff", "Create SVN diff"));
        svn_diff.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    data.setSvnDiff(svn_diff.isSelected());
                }
            }
        );

        // button panel
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( jEdit.getProperty("ips.Ok", "Ok") );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        JButton cancel_btn = new JButton( jEdit.getProperty("ips.Cancel", "Cancel") );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        // layout main panel
        panel.add( file_panel1, "0, 0, 1, 1, W" );
        panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 1, 1, 1" );
        panel.add( rsp1, "0, 2, 1, 1, W, w" );
        panel.add( KappaLayout.createVerticalStrut( 11 ), "0, 3, 1, 1" );
        if ( count == 2 ) {
            panel.add( file_panel2, "0, 4, 1, 1, W" );
            panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 5, 1, 1" );
        }
        panel.add( rsp2, "0, 6, 1, 1, W, w" );
        panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 7, 1, 1" );
        ///panel.add( svn_diff, "0, 8, 1, 1, W, w" );
        panel.add( KappaLayout.createVerticalStrut( 11 ), "0, 9, 1, 1" );
        panel.add( btn_panel, "0, 10, 1, 1, E" );
        setContentPane( panel );
        pack();

        // action listeners for ok and cancel buttons
        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        data.setRevision1( rsp1.getRevision() );
                        data.setRevision2( rsp2.getRevision() );
                        RemoteDiffDialog.this.setVisible( false );
                        RemoteDiffDialog.this.dispose();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // user canceled, set data to null to signal as much.
                        data = null;
                        RemoteDiffDialog.this.setVisible( false );
                        RemoteDiffDialog.this.dispose();
                    }
                }
                                    );

        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    /**
     * @return null signals user clicked cancel button
     */
    public DiffData getData() {
        return data;
    }
}