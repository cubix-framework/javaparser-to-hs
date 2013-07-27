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

import ise.plugin.svn.PVHelper;
import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.command.Switch;
import ise.plugin.svn.data.UpdateData;
import ise.plugin.svn.gui.SwitchDialog;
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
 * ActionListener to perform an svn switch.
 * This is not strictly dependent on ProjectViewer, however, if there are files
 * added or deleted as a result of the switch, this class will ask the user if
 * the project files should be reimported.
 */
public class SwitchAction extends SVNAction {

    private UpdateData data = null;

    /**
     * @param view the View in which to display results
     * @param paths a list of paths to be added
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public SwitchAction( View view, UpdateData data ) {
        super( view, jEdit.getProperty( "ips.Switch", "Switch" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        this.data = data;
    }


    public void actionPerformed( ActionEvent ae ) {
        if ( data.getPaths() != null && data.getPaths().size() > 0 ) {

            data.setOut( new ConsolePrintStream( getView() ) );

            // show dialog
            SwitchDialog dialog = new SwitchDialog( getView(), data );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            data = dialog.getData();
            if ( data == null ) {
                return ;     // null data signals user canceled
            }


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

            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();
            Logger logger = panel.getLogger();
            logger.log( Level.INFO, jEdit.getProperty( "ips.Switching_...", "Switching ..." ) );
            for ( Handler handler : logger.getHandlers() ) {
                handler.flush();
            }

            class Runner extends SwingWorker<UpdateData, Object> {

                @Override
                public UpdateData doInBackground() {
                    try {
                        Switch switcher = new Switch( );
                        return switcher.doSwitch( data );
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
                        data.getOut().printError( "Stopped 'Switch' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Switch' action." );
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
                        JPanel results_panel = new UpdateResultsPanel( getView(), data );
                        panel.addTab( jEdit.getProperty( "ips.Switch", "Switch" ), results_panel );

                        // reload affected buffers
                        for ( String path : data.getPaths() ) {
                            Buffer buffer = jEdit.getBuffer( path );
                            if ( buffer != null ) {
                                buffer.reload( getView() );
                            }
                        }

                        // offer to reload project files if there are added or deleted files
                        if ( ( data.getAddedFiles() != null && data.getAddedFiles().size() > 0 ) ||
                                ( data.getDeletedFiles() != null && data.getDeletedFiles().size() > 0 ) ) {
                            PVHelper.reimportProjectFiles( getView() );
                        }
                    }
                    catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
            }
            Runner runner = new Runner();
            panel.addWorker( "Switch", runner );
            runner.execute();
        }
    }

    public UpdateData getData() {
        return data;
    }
}