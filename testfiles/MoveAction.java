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
import ise.plugin.svn.command.Copy;
import ise.plugin.svn.data.AddResults;
import ise.plugin.svn.data.CopyData;
import ise.plugin.svn.gui.AddResultsPanel;
import ise.plugin.svn.gui.CopyResultsPanel;
import ise.plugin.svn.gui.ErrorPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import common.swingworker.*;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JPanel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.msg.BufferUpdate;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.wc.SVNCopySource;


/**
 * ActionListener to perform an svn move, based on CopyAction
 * This is not dependent on ProjectViewer.
 */
public class MoveAction extends SVNAction {

    private CopyData data = null;

    private static final int W2W = 1;
    //private static final int W2U = 2;     // can't do a move like this
    //private static final int U2W = 3;     // can't do a move like this
    private static final int U2U = 4;

    /**
     * @param view the View in which to display results
     * @param data CopyData object containing the info for a copy of some sort
     */
    public MoveAction( View view, CopyData data ) {
        super( view, jEdit.getProperty( "ips.Move", "Move" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        this.data = data;
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( data != null ) {
            data.setOut( new ConsolePrintStream( getView() ) );

            if ( data.getUsername() == null ) {
                verifyLogin( data.getPaths() == null ? null : data.getPaths().get( 0 ) );
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

            getView().getDockableWindowManager().showDockableWindow( "subversion" );

            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            final Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Moving_...", "Moving ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            /**
             * working copy -> working copy
             * repository -> repository
             */
            class Runner extends SwingWorker<SVNCommitInfo, Object> {

                private int where2where;
                private String errorMessage = null;
                SVNCommitInfo result = null;

                @Override
                public SVNCommitInfo doInBackground() {
                    try {
                        data.setIsMove( true );
                        if ( data.getSourceFiles() != null ) {
                            where2where = W2W;
                            Copy copy = new Copy();
                            result = copy.copy( data );
                        }
                        else if ( data.getSourceURLs() != null ) {
                            where2where = U2U;
                            Copy copy = new Copy();
                            result = copy.copy( data );
                        }
                    }
                    catch ( Exception e ) {
                        errorMessage = e.getMessage();
                        data.getOut().printError( errorMessage );
                        e.printStackTrace();
                    }
                    finally {
                        data.getOut().close();
                    }
                    return result;
                }

                @Override
                public boolean cancel( boolean mayInterruptIfRunning ) {
                    boolean cancelled = super.cancel( mayInterruptIfRunning );
                    if ( cancelled ) {
                        data.getOut().printError( "Stopped 'Move' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Move' action." );
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
                            panel.addTab( jEdit.getProperty( "ips.Move_Error", "Move Error" ), error_panel );
                            return ;
                        }
                        result = get();
                        switch ( where2where ) {
                            case W2W:
                                // SVNCommitInfo in 'result' will be null in this
                                // case since there is no actual commit.  These
                                // files will be scheduled for svn add.
                                AddResults ar = new AddResults();
                                for ( SVNCopySource source : data.getSourceFiles() ) {
                                    ar.addPath( source.getFile().getAbsolutePath() );
                                }
                                JPanel results_panel = new AddResultsPanel( ar, AddResultsPanel.ADD, getView(), getUsername(), getPassword() );
                                panel.addTab( jEdit.getProperty( "ips.Move", "Move" ), results_panel );

                                // open the file(s) and signal ProjectViewer to possibly add the file
                                for ( SVNCopySource source : data.getSourceFiles() ) {
                                    File f = source.getFile();
                                    if ( !f.isDirectory() ) {
                                        Buffer buffer = jEdit.openFile( getView(), f.getAbsolutePath() );
                                        BufferUpdate bu = new BufferUpdate( buffer, getView(), BufferUpdate.SAVED );
                                        EditBus.send( bu );
                                    }
                                }
                                break;
                            case U2U:
                                // these cases result in an immediate commit, so
                                // the SVNCommitInfo objects in the map are valid
                                HashMap<String, SVNCommitInfo> results = new HashMap<String, SVNCommitInfo>();
                                for ( SVNCopySource source : data.getSourceURLs() ) {
                                    results.put( source.getURL().toString(), result );
                                }
                                results_panel = new CopyResultsPanel( results, data.getDestinationURL().toString(), true );
                                panel.addTab( jEdit.getProperty( "ips.Move", "Move" ), results_panel );
                                break;
                            default:
                                // this shouldn't happen, so I'll just quietly
                                // ignore this case
                        }
                    }
                    catch ( Exception e ) {
                        // ignored
                        e.printStackTrace();
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Move", runner );
            runner.execute();
        }
    }
}