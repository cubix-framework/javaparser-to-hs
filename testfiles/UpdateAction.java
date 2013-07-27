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
import ise.plugin.svn.command.Update;
import ise.plugin.svn.data.UpdateData;
import ise.plugin.svn.gui.UpdateDialog;
import ise.plugin.svn.gui.UpdateResultsPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.SwingWorker;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

/**
 * ActionListener to perform an svn update.
 * This is not dependent on ProjectViewer.
 */
public class UpdateAction extends SVNAction {

    private List<String> paths = null;
    private UpdateData data = null;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public UpdateAction( View view, List<String> paths, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Update", "Update" ) );
        if ( paths == null )
            throw new IllegalArgumentException( "paths may not be null" );
        this.paths = paths;
        setUsername( username );
        setPassword( password );
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( paths != null && paths.size() > 0 ) {
            data = new UpdateData();
            data.setPaths( paths );

            boolean recursive = false;
            for ( String path : paths ) {
                if ( path != null ) {
                    File file = new File( path );
                    if ( file.isDirectory() ) {
                        recursive = true;
                    }
                }
            }
            data.setRecursive( recursive ); // if recursive is false here, it means paths contains only files, no directories

            if ( getUsername() == null ) {
                verifyLogin( data.getPaths() == null ? null : data.getPaths().get( 0 ) );
                if ( isCanceled() ) {
                    return ;
                }
            }
            data.setUsername( getUsername() );
            data.setPassword( getPassword() );

            data.setOut( new ConsolePrintStream( getView() ) );

            // show dialog
            UpdateDialog dialog = new UpdateDialog( getView(), data, false );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            data = dialog.getData();
            if ( data == null ) {
                return ;     // null data signals user canceled
            }


            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Updating_...", "Updating ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<UpdateData, Object> {

                @Override
                public UpdateData doInBackground() {
                    try {
                        Update update = new Update( );
                        return update.doUpdate( data );
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
                        data.getOut().printError( "Stopped 'Update' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Update' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        UpdateData data = get();
                        if ( data == null ) {
                            return ;
                        }
                        JPanel results_panel = new UpdateResultsPanel( getView(), data );
                        panel.addTab( jEdit.getProperty( "ips.Update", "Update" ), results_panel );
                        for ( String path : data.getPaths() ) {
                            updateStatus(path);
                            Buffer buffer = jEdit.getBuffer( path );
                            if ( buffer != null ) {
                                buffer.reload( getView() );
                            }
                        }
                        if ( data.getConflictedFiles() != null ) {
                            StringBuffer sb = new StringBuffer();
                            for ( String path : data.getConflictedFiles() ) {
                                sb.append( path ).append( '\n' );
                            }
                            String filelist = sb.toString();
                            if ( filelist.length() > 0 ) {
                                JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.One_or_more_files_have_conflicts>", "One or more files have conflicts:" ) + "\n\n" + filelist, jEdit.getProperty( "ips.Conflicts", "Conflicts" ), JOptionPane.WARNING_MESSAGE );
                            }
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Update", runner );
            runner.execute();
        }
    }

    public UpdateData getData() {
        return data;
    }
}