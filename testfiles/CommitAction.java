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

package ise.plugin.svn.action;

import ise.plugin.svn.gui.OutputPanel;

import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.command.Commit;
import ise.plugin.svn.data.CommitData;
import ise.plugin.svn.gui.CommitDialog;
import ise.plugin.svn.gui.CommitResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.*;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;
import java.util.logging.*;
import javax.swing.JPanel;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;

import org.tmatesoft.svn.core.wc.*;


/**
 * ActionListener to perform an svn commit.
 * This is not dependent on ProjectViewer.
 */
public class CommitAction extends SVNAction {

    private CommitDialog dialog = null;
    private TreeMap<String, String> paths = null; // <path, status>, where status is added, modified, etc

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public CommitAction( View view, TreeMap<String, String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Commit", "Commit" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( paths != null && paths.size() > 0 ) {
            // check for /tag/ and warn user if it appears they are
            // trying to commit to a tag directory
            List<String> possible_tags = new ArrayList<String>();
            SVNWCClient client = SVNClientManager.newInstance().getWCClient();
            Set<String> keys = paths.keySet();
            for ( String path : keys ) {
                try {
                    SVNInfo info = client.doInfo( new File( path ), SVNRevision.WORKING );
                    if ( info != null && info.getURL().toString().indexOf( "/tags/" ) > -1 ) {
                        possible_tags.add( path );
                    }
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
            if ( possible_tags != null && possible_tags.size() > 0 ) {
                StringBuffer msg = new StringBuffer();
                msg.append( "It appears you may be attempting to commit some files to a tag:\n\n" );
                for ( String path : possible_tags ) {
                    msg.append( path ).append( "\n" );
                }
                msg.append( "\n" );
                msg.append( "Are you sure you want to commit these files?" );
                int no = JOptionPane.showConfirmDialog( getView(), msg, jEdit.getProperty( "ips.Confirm_Commit", "Confirm Commit" ), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                if ( no == JOptionPane.NO_OPTION ) {
                    return ;
                }
            }

            dialog = new CommitDialog( getView(), paths, false );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            final CommitData data = dialog.getCommitData();
            if ( data == null ) {
                return ;     // null means user canceled
            }

            if ( getUsername() == null ) {
                verifyLogin( ( String ) paths.firstKey() );
                if ( isCanceled() ) {
                    return ;
                }
            }
            data.setUsername( getUsername() );
            data.setPassword( getPassword() );

            data.setOut( new ConsolePrintStream( getView() ) );

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            final Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Committing_...", "Committing ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<CommitData, Object> {

                @Override
                public CommitData doInBackground() {
                    try {
                        Commit commit = new Commit( );
                        return commit.commit( data );
                    }
                    catch ( Exception e ) {
                        data.getOut().printError( e.getMessage() );
                    }
                    finally {
                        data.getOut().close();
                    }
                    return null;
                }

                @Override
                public boolean cancel( boolean mayInterruptIfRunning ) {
                    boolean cancelled = super.cancel( mayInterruptIfRunning );
                    if ( cancelled ) {
                        data.getOut().printError( "Stopped 'Commit' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Commit' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        JPanel results_panel = new CommitResultsPanel( get() );
                        panel.addTab( jEdit.getProperty( "ips.Commit", "Commit" ), results_panel );

                        // fix for 2081908
                        for ( String path : paths.keySet() ) {
                            updateStatus(path);
                            Buffer buffer = jEdit.getBuffer( path );
                            if ( buffer != null ) {
                                buffer.reload( getView() );
                            }
                        }
                    }
                    catch ( Exception e ) {     // NOPMD
                        // ignored
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Commit", runner );
            runner.execute();
        }
    }
}