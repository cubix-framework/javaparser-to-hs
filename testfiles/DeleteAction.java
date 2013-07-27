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
import ise.plugin.svn.command.Delete;
import ise.plugin.svn.data.AddResults;
import ise.plugin.svn.data.DeleteData;
import ise.plugin.svn.data.DeleteResults;
import ise.plugin.svn.gui.AddResultsPanel;
import ise.plugin.svn.gui.DeleteDialog;
import ise.plugin.svn.gui.RemoteDeleteDialog;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.SwingWorker;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * ActionListener to perform an svn delete.
 * This is not dependent on ProjectViewer.
 */
public class DeleteAction extends SVNAction {

    private DeleteData data = null;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be deleted
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public DeleteAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Delete", "Delete" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        setUsername( username );
        setPassword( password );

        data = new DeleteData();
        data.setPaths( paths );
        data.setUsername( username );
        data.setPassword( password );
    }

    public DeleteAction( View view, DeleteData data ) {
        super( view, jEdit.getProperty( "ips.Delete", "Delete" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        this.data = data;
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( data.getPaths() != null && data.getPaths().size() > 0 ) {



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
            data.setOut( new ConsolePrintStream( getView() ) );

            // show dialog
            if ( !data.pathsAreURLs() ) {
                // working copy delete
                DeleteDialog dialog = new DeleteDialog( getView(), data, false );
                GUIUtils.center( getView(), dialog );
                dialog.setVisible( true );
                data = dialog.getData();
                if ( data == null ) {
                    return ;     // null data signals user canceled
                }
            }
            else {
                // remote copy delete -- show path(s) to delete and commit
                // message textbox and dropdown.
                RemoteDeleteDialog dialog = new RemoteDeleteDialog( getView(), data );
                GUIUtils.center( getView(), dialog );
                dialog.setVisible( true );
                data = dialog.getData();
                if ( data == null ) {
                    return ;     // null data signals user canceled
                }
                JOptionPane.showConfirmDialog( getView(), jEdit.getProperty( "ips.This_WILL_delete_files_from_the_repository.", "This WILL delete files from the repository." ), jEdit.getProperty( "ips.Confirm_Delete", "Confirm Delete" ), JOptionPane.WARNING_MESSAGE );
            }

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Deleting_...", "Deleting ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<DeleteResults, Object> {

                @Override
                public DeleteResults doInBackground() {
                    try {
                        Delete delete = new Delete( );
                        return delete.delete( data );
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
                        data.getOut().printError( "Stopped 'Delete' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Delete' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        AddResults results = ( AddResults ) get();
                        JPanel results_panel = new AddResultsPanel( results, data.pathsAreURLs() ? AddResultsPanel.REMOTE_DELETE : AddResultsPanel.DELETE, getView(), getUsername(), getPassword() );
                        panel.addTab( jEdit.getProperty( "ips.Delete", "Delete" ), results_panel );
                        for (String path : data.getPaths()) {
                            updateStatus(path);    
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Delete", runner );
            runner.execute();
        }
    }

    public DeleteData getData() {
        return data;
    }
}