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

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import ise.plugin.svn.gui.OutputPanel;

import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.command.Resolved;
import ise.plugin.svn.data.SVNData;
import ise.plugin.svn.data.AddResults;
import common.swingworker.*;
import ise.plugin.svn.gui.AddResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;


import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * ActionListener to perform an svn add.
 * This is not dependent on ProjectViewer.
 */
public class ResolvedAction extends SVNAction {

    private List<String> paths = null;
    private boolean force = false;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public ResolvedAction( View view, List<String> paths, String username, String password ) {
        this( view, paths, username, password, false );
    }

    public ResolvedAction( View view, List<String> paths, String username, String password, boolean force ) {
        super( view, jEdit.getProperty( "ips.Resolved", "Resolved" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
        this.force = force;
    }

    public void actionPerformed( ActionEvent ae ) {
        if ( paths != null && paths.size() > 0 ) {
            final SVNData data = new SVNData();

            // get the paths
            boolean recursive = false;
            for ( String path : paths ) {
                File f = new File( path );
                if ( f.exists() && f.isDirectory() ) {
                    recursive = true;
                    break;
                }
            }

            // user confirmations
            if ( recursive ) {
                // have the user verify they want a recursive resolve
                int response = JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.Recursively_resolve_all_files_in_selected_directories?", "Recursively resolve all files in selected directories?" ), jEdit.getProperty( "ips.Recursive_Resolved?", "Recursive Resolved?" ), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.CANCEL_OPTION ) {
                    return ;
                }
                recursive = response == JOptionPane.YES_OPTION;
            }
            else if ( !force ) {
                // have the user confirm they really want to resolve
                int response = JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.Resolve_selected_files?", "Resolve selected files?" ), jEdit.getProperty( "ips.Confirm_Resolve", "Confirm Resolve" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.NO_OPTION ) {
                    return ;
                }
            }

            if ( getUsername() == null ) {
                verifyLogin( paths.get( 0 ) );
                if ( isCanceled() ) {
                    return ;
                }
            }
            data.setUsername( getUsername() );
            data.setPassword( getPassword() );

            data.setPaths( paths );

            data.setOut( new ConsolePrintStream( getView() ) );

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Resolving_...", "Resolving ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<AddResults, Object> {

                @Override
                public AddResults doInBackground() {
                    try {
                        Resolved resolve = new Resolved();
                        return resolve.resolve( data );
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
                        data.getOut().printError( "Stopped 'Resolved' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Resolved' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        JPanel results_panel = new AddResultsPanel( get(), AddResultsPanel.RESOLVED, getView(), getUsername(), getPassword() );
                        panel.addTab( jEdit.getProperty( "ips.Resolved", "Resolved" ), results_panel );
                        for (String path : paths) {
                            updateStatus(path);   
                        }
                    }
                    catch ( Exception e ) {
                        System.err.println( e.getMessage() );
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Resolved", runner );
            runner.execute();
        }
    }
}