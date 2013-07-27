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
import ise.plugin.svn.command.Log;
import ise.plugin.svn.data.LogData;
import ise.plugin.svn.data.LogResults;
import ise.plugin.svn.gui.LogDialog;
import ise.plugin.svn.gui.LogResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.library.Logger;
import common.swingworker.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JPanel;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * ActionListener to perform an svn log.
 * This is not dependent on ProjectViewer.
 */
public class LogAction extends SVNAction {

    private List<String> paths = null;
    private boolean pathsAreUrls = false;
    private LogData data = null;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public LogAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Log", "Log" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
    }

    public LogAction( View view, LogData data ) {
        super( view, jEdit.getProperty( "ips.Log", "Log" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        if ( data.getPaths() == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = data.getPaths();
        this.pathsAreUrls = data.pathsAreURLs();
        setUsername( data.getUsername() );
        setPassword( data.getPassword() );
    }

    public void actionPerformed( ActionEvent ae ) {

        if ( paths != null && paths.size() > 0 ) {
            data = new LogData();
            data.setPaths( paths );
            data.setPathsAreURLs( pathsAreUrls );


            LogDialog dialog = new LogDialog( getView(), data );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            data = dialog.getData();
            if ( data == null ) {

                return ;     // null data signals user canceled
            }

            if ( getUsername() == null ) {
                verifyLogin( paths.get( 0 ) );
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
            java.util.logging.Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Fetching_log_...", "Fetching log ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker < LogResults , Object > {

                @Override
                public LogResults doInBackground() {
                    try {

                        Log log = new Log( );

                        log.doLog( data );

                        return log.getLogEntries();
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
                        data.getOut().printError( "Stopped 'Log' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Log' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        final LogResults results = get();

                        if ( results == null ) {
                            return ;
                        }

                        JPanel results_panel = new LogResultsPanel( results, data.getShowPaths(), getView(), getUsername(), getPassword() );

                        panel.addTab( jEdit.getProperty( "ips.Log", "Log" ), results_panel );
                    }
                    catch ( Exception e ) {
                        // ignored
                        e.printStackTrace();
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Log", runner );
            runner.execute();
        }
    }
}