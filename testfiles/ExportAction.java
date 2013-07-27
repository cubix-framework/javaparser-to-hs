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
import ise.plugin.svn.data.UpdateData;
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
import ise.plugin.svn.data.ExportData;
import ise.plugin.svn.gui.ExportDialog;
import ise.plugin.svn.command.Export;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCopySource;

/**
 * ActionListener to perform an svn export.
 * This is not dependent on ProjectViewer.
 */
public class ExportAction extends SVNAction {

    private ExportData data = new ExportData();

    /**
     * @param view the View in which to display results
     * @param sourceFile what to export
     */
    public ExportAction( View view, List<File> sourceFiles, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Export", "Export" ) );
        if ( sourceFiles == null )
            throw new IllegalArgumentException( "sourceFile may not be null" );
        setUsername( username );
        setPassword( password );
        data.setSourceFiles( sourceFiles );
        data.setUsername( username );
        data.setPassword( password );
    }

    /**
     * @param view the View in which to display results
     * @param sourceUrl what to export
     */
    public ExportAction( View view, String username, String password, List<String> sourceUrls ) {
        super( view, jEdit.getProperty( "ips.Export", "Export" ) );
        if ( sourceUrls == null )
            throw new IllegalArgumentException( "sourceUrl may not be null" );
        try {
            List<SVNURL> urls = new ArrayList<SVNURL>();
            for ( String url : sourceUrls ) {
                urls.add( SVNURL.parseURIDecoded( url ) );
            }
            data.setSourceURLs( urls );
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException( e.getMessage() );
        }
        setUsername( username );
        setPassword( password );
        data.setUsername( username );
        data.setPassword( password );
    }

    public void actionPerformed( ActionEvent ae ) {

        data.setOut( new ConsolePrintStream( getView() ) );

        // show dialog
        ExportDialog dialog = new ExportDialog( getView(), data );
        GUIUtils.center( getView(), dialog );
        dialog.setVisible( true );
        data = dialog.getData();
        if ( data == null ) {
            return ;     // null data signals user canceled
        }

        if ( data.getUsername() == null ) {
            verifyLogin( data.getSourceFile() == null ? null : data.getSourceFile().toString() );
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
        logger.log( Level.INFO, "Exporting ..." );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }

        class Runner extends SwingWorker<UpdateData, Object> {

            @Override
            public UpdateData doInBackground() {
                try {
                    Export export = new Export( );
                    return export.export( data );
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
                    data.getOut().printError( "Stopped 'Export' action." );
                    data.getOut().close();
                }
                else {
                    data.getOut().printError( "Unable to stop 'Export' action." );
                }
                return cancelled;
            }

            @Override
            protected void done() {
                if ( isCancelled() ) {
                    return ;
                }

                try {
                    UpdateData export_data = get();
                    if ( data == null ) {
                        data.getOut().printError( "Unable to export." );
                        return ;
                    }
                    JPanel results_panel = new UpdateResultsPanel( getView(), export_data, true );
                    panel.addTab( jEdit.getProperty( "ips.Export", "Export" ), results_panel );
                    if ( data.getSourceFiles() != null ) {
                        for ( SVNCopySource source : data.getSourceFiles() ) {
                            String path = source.getFile().toString();
                            Buffer buffer = jEdit.getBuffer( path );
                            if ( buffer != null ) {
                                buffer.reload( getView() );
                            }
                        }
                    }
                }
                catch ( Exception e ) {
                    e.printStackTrace();
                }
            }
        }
        Runner runner = new Runner();
        panel.addWorker( "Export", runner );
        runner.execute();
    }
}