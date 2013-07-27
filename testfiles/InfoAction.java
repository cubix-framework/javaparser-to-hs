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

import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.command.Info;
import ise.plugin.svn.data.SVNData;
import ise.plugin.svn.gui.ErrorPanel;
import ise.plugin.svn.gui.OutputPanel;
import ise.plugin.svn.gui.SVNInfoPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import common.swingworker.SwingWorker;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import org.tmatesoft.svn.core.wc.SVNInfo;

public class InfoAction extends SVNAction {

    private List<String> paths = null;
    private boolean pathsAreUrls = false;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public InfoAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Info", "Info" ) );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
    }

    public InfoAction( View view, SVNData data ) {
        super( view, jEdit.getProperty( "ips.Info", "Info" ) );
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
            final SVNData data = new SVNData();
            data.setPaths( paths );
            data.setPathsAreURLs( pathsAreUrls );

            // don't need username/password if files are local since info
            // data is in the .svn folders locally
            if ( pathsAreUrls ) {
                if ( data.getUsername() == null ) {
                    verifyLogin( paths.get( 0 ) );
                    if ( isCanceled() ) {
                        return ;
                    }
                    data.setUsername( getUsername() );
                    data.setPassword( getPassword() );
                }
                else {
                    setUsername( data.getUsername() );
                    setPassword( data.getPassword() );
                }
            }

            data.setOut( new ConsolePrintStream( getView() ) );

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Fetching_info...", "Fetching info..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<List<SVNInfo>, Object> {
                private String errorMessage = null;

                @Override
                public List<SVNInfo> doInBackground() {
                    try {
                        Info info = new Info();
                        return info.info( data );
                    }
                    catch ( Exception e ) {
                        errorMessage = e.getMessage();
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
                        data.getOut().printError( "Stopped 'Info' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Info' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        if ( errorMessage != null ) {
                            JPanel error_panel = new ErrorPanel( errorMessage );
                            panel.addTab( jEdit.getProperty( "ips.Info_Error", "Info Error" ), error_panel );
                            return ;
                        }
                        JPanel info_panel = new SVNInfoPanel( get() );
                        panel.addTab( jEdit.getProperty( "ips.Info", "Info" ), info_panel );

                    }
                    catch ( Exception e ) {     // NOPMD
                        // ignored
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Info", runner );
            runner.execute();
        }
    }

}