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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import ise.plugin.svn.data.UpdateData;
import ise.java.awt.KappaLayout;
import ise.plugin.svn.gui.component.*;

public class UpdateDialog extends JDialog {

    private UpdateData data = null;

    /**
     * @param view the parent frame
     * @param path the local working file to diff against.
     */
    public UpdateDialog( View view, UpdateData data ) {
        this( view, data, false );
    }

    public UpdateDialog( View view, UpdateData data, boolean showLogin ) {

        super( ( JFrame ) view, jEdit.getProperty("ips.Update", "Update"), true );
        if ( data == null ) {
            throw new IllegalArgumentException( "data may not be null" );
        }
        this.data = data;
        init( showLogin );
    }

    private void init( boolean showLogin ) {
        JPanel panel = new JPanel( new KappaLayout() );
        panel.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        final RevisionSelectionPanel rsp = new RevisionSelectionPanel( jEdit.getProperty("ips.Update_To>", "Update To:"), SwingConstants.HORIZONTAL, false );

        final JCheckBox recursive_cb = new JCheckBox( jEdit.getProperty("ips.Recursive", "Recursive") );
        recursive_cb.setSelected( data.getRecursive() );
        recursive_cb.setVisible( data.getRecursive() );

        // possible username and password values
        final LoginPanel login;
        if ( data.getUsername() == null ) {
            login = new LoginPanel(data.getPaths().get(0));
        }
        else {
            login = new LoginPanel(data.getUsername(), data.getPassword());
        }

        // username and password may not need to be visible
        login.setVisible(showLogin);

        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( jEdit.getProperty("ips.Ok", "Ok") );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        JButton cancel_btn = new JButton( jEdit.getProperty("ips.Cancel", "Cancel") );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // get revision to update to
                        getData().setSVNRevision( rsp.getRevision() );

                        getData().setRecursive( recursive_cb.isSelected() );

                        getData().setUsername(login.getUsername());
                        getData().setPassword(login.getPassword());

                        UpdateDialog.this.setVisible( false );
                        UpdateDialog.this.dispose();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        UpdateDialog.this.data = null;
                        UpdateDialog.this.setVisible( false );
                        UpdateDialog.this.dispose();
                    }
                }
                                    );

        panel.add( rsp, "0, 0, 1, 1, W, w" );
        if ( data.getRecursive() ) {
            panel.add( KappaLayout.createVerticalStrut( 6 ), "0, 1, 1, 1" );
            panel.add( recursive_cb, "0, 2, 1, 1, W, w, 3" );
        }

        if ( showLogin ) {
            panel.add( "0, 3, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );
            panel.add( "0, 4, 1, 1, 0, w", login);
        }

        panel.add( KappaLayout.createVerticalStrut( 11 ), "0, 6, 1, 1" );
        panel.add( btn_panel, "0, 7, 1, 1, E" );
        setContentPane( panel );
        pack();
        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    public UpdateData getData() {
        return data;
    }
}
