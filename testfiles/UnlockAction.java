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
import ise.plugin.svn.command.Lock;
import ise.plugin.svn.data.CommitData;
import ise.plugin.svn.data.LockResults;
import ise.plugin.svn.gui.LockDialog;
import ise.plugin.svn.gui.AddResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.*;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JPanel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;


/**
 * ActionListener to perform an svn lock.
 * This is not dependent on ProjectViewer.
 */
public class UnlockAction extends SVNAction {

    private LockDialog dialog = null;
    private List<String> paths = null;
    private boolean remote = false;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     * @param remote true if attempting to lock a remote repository file
     */
    public UnlockAction( View view, List<String> paths, String username, String password, boolean remote ) {
        super( view, jEdit.getProperty( "ips.Unlock", "Unlock" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
        this.remote = remote;
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( paths != null && paths.size() > 0 ) {
            dialog = new LockDialog( getView(), paths, false, remote );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            final CommitData data = dialog.getData();
            if ( data == null ) {
                return ;     // null means user canceled
            }

            if ( getUsername() == null ) {
                verifyLogin( data.getPaths() == null ? null : data.getPaths().get( 0 ) );
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
            logger.log( Level.INFO, jEdit.getProperty( "ips.Unlocking_...", "Unlocking ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<LockResults, Object> {

                @Override
                public LockResults doInBackground() {
                    try {
                        Lock lock = new Lock();
                        return lock.unlock( data );
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
                        data.getOut().printError( "Stopped 'Unlock' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Unlock' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        JPanel results_panel = new AddResultsPanel( get(), AddResultsPanel.UNLOCK, getView(), getUsername(), getPassword() );
                        panel.addTab( jEdit.getProperty( "ips.Unlocked", "Unlocked" ), results_panel );
                    }
                    catch ( Exception e ) {     // NOPMD
                        // ignored
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Unlock", runner );
            runner.execute();
        }
    }
}