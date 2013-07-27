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
import ise.plugin.svn.command.Revert;
import ise.plugin.svn.data.SVNData;
import ise.plugin.svn.data.AddResults;
import common.swingworker.*;
import ise.plugin.svn.gui.AddResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;


/**
 * ActionListener to perform an svn revert.
 * This is not dependent on ProjectViewer.
 */
public class RevertAction extends SVNAction {
    private List<String> paths = null;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public RevertAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Revert", "Revert" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( paths != null && paths.size() > 0 ) {
            final SVNData data = new SVNData();

            // get the paths
            boolean recursive = false;
            for ( String path : paths ) {
                if ( path != null ) {
                    File file = new File( path );
                    if ( file.isDirectory() ) {
                        recursive = true;
                    }
                }
            }

            // user confirmations
            if ( recursive ) {
                // have the user verify they want a recursive revert
                int response = JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.Recursively_revert_all_files_in_selected_directories?", "Recursively revert all files in selected directories?" ), jEdit.getProperty( "ips.Recursive_Revert?", "Recursive Revert?" ), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.CANCEL_OPTION ) {
                    return ;
                }
                recursive = response == JOptionPane.YES_OPTION;
            }
            else {
                // have the user confirm they really want to revert
                int response = JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.Revert_selected_files?", "Revert selected files?" ), jEdit.getProperty( "ips.Confirm_Revert", "Confirm Revert" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                if ( response == JOptionPane.NO_OPTION ) {
                    return ;
                }
            }
            data.setRecursive( recursive );
            data.setPaths( paths );

            if ( getUsername() == null ) {
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

            data.setOut( new ConsolePrintStream( getView() ) );

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Reverting_...", "Reverting ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<AddResults, Object> {

                @Override
                public AddResults doInBackground() {
                    try {
                        Revert revert = new Revert();
                        return revert.revert( data );
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
                        data.getOut().printError( "Stopped 'Revert' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Revert' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        AddResults results = get();
                        JPanel results_panel = new AddResultsPanel( results, AddResultsPanel.REVERT, getView(), getUsername(), getPassword() );
                        panel.addTab( jEdit.getProperty( "ips.Revert", "Revert" ), results_panel );
                        for ( String path : results.getPaths() ) {
                            updateStatus(path);
                            Buffer buffer = jEdit.getBuffer( path );
                            if ( buffer != null ) {
                                buffer.reload( RevertAction.this.getView() );
                            }
                        }
                    }
                    catch ( Exception e ) {     // NOPMD
                        // ignored
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Revert", runner );
            runner.execute();
        }
    }

}