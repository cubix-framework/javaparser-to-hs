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
import javax.swing.JOptionPane;
import javax.swing.JPanel;


import ise.plugin.svn.gui.OutputPanel;
import ise.plugin.svn.SVNPlugin;

import ise.plugin.svn.command.Status;
import ise.plugin.svn.data.SVNData;
import ise.plugin.svn.data.StatusData;
import ise.plugin.svn.gui.StatusResultsPanel;
import common.swingworker.*;
import ise.plugin.svn.io.ConsolePrintStream;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * Collects status of working copy files from PV tree.
 */
public class StatusAction extends SVNAction {

    private SVNData data = null;
    private String title = jEdit.getProperty( "ips.Status", "Status" );

    public StatusAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Status", "Status" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        setUsername( username );
        setPassword( password );

        data = new SVNData();
        data.setPaths( paths );
        data.setUsername( username );
        data.setPassword( password );
    }

    public StatusAction( View view, SVNData data ) {
        super( view, jEdit.getProperty( "ips.Status", "Status" ) );
        this.data = data;
        setUsername( data.getUsername() );
        setPassword( data.getPassword() );
    }

    /**
     * After a merge, this action is called to show the user the results of
     * the merge.  This method lets the merge command set the tab title to
     * "Merge".
     */
    public void setTabTitle( String title ) {
        if ( title != null && title.length() > 0 ) {
            this.title = title;
        }
    }

    public void actionPerformed( ActionEvent ae ) {
        if ( data.getUsername() == null ) {
            verifyLogin( data.getPaths().get( 0 ) );
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

        if ( data.getOut() == null ) {
            data.setOut( new ConsolePrintStream( getView() ) );
        }

        int response = JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.Check_status_against_repository?", "Check status against repository?" ), jEdit.getProperty( "ips.Status", "Status" ), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( response == JOptionPane.CANCEL_OPTION ) {
            return ;
        }
        data.setRemote( response == JOptionPane.YES_OPTION );

        getView().getDockableWindowManager().showDockableWindow( "subversion" );
        final OutputPanel output_panel = SVNPlugin.getOutputPanel( getView() );
        output_panel.showConsole();
        Logger logger = output_panel.getLogger();
        logger.log( Level.INFO, jEdit.getProperty( "ips.Gathering_status_...", "Gathering status ..." ) );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }

        class Runner extends SwingWorker<StatusData, Object> {

            @Override
            public StatusData doInBackground() {
                try {
                    Status status = new Status();
                    return status.getStatus( data );
                }
                catch ( Exception e ) {
                    data.getOut().printError( e.getMessage() );
                    e.printStackTrace();
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
                    data.getOut().printError( "Stopped 'Status' action." );
                    data.getOut().close();
                }
                else {
                    data.getOut().printError( "Unable to stop 'Status' action." );
                }
                return cancelled;
            }

            @Override
            protected void done() {
                if ( isCancelled() ) {
                    return ;
                }

                try {
                    JPanel panel = new StatusResultsPanel( get(), getView(), getUsername(), getPassword() );
                    output_panel.addTab( title, panel );
                }
                catch ( Exception e ) {
                    System.err.println( e.getMessage() );
                    e.printStackTrace();
                }
            }
        }
        Runner runner = new Runner();
        output_panel.addWorker( "Status", runner );
        runner.execute();
    }

}