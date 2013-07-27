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
import ise.plugin.svn.command.BrowseRepository;
import ise.plugin.svn.data.DiffData;
import ise.plugin.svn.gui.RemoteDiffDialog;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPane;
import jdiff.DualDiffManager;

/**
 * ActionListener to perform sort of an svn diff.  While subversion can do a diff,
 * I'm delegating to the JDiff plugin to create and display the diff.
 * This is not dependent on ProjectViewer.  This version does a diff of 2 repository
 * items.
 *
 * TODO: figure out why this class is necessary, can't DiffAction handle this case?
 */
public class RemoteDiffAction extends SVNAction {

    private DiffData data;
    private Logger logger = null;

    public RemoteDiffAction( View view, DiffData data ) {
        super( view, jEdit.getProperty( "ips.Remote_Diff", "Remote Diff" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        this.data = data;
        if ( data.pathsAreURLs() == false ) {
            throw new IllegalArgumentException( "RemoteDiffAction is for remote diffs, the given paths must be repository URLs." );
        }
    }

    private void log( String msg ) {
        logger.log( Level.INFO, msg );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }
    }

    public void actionPerformed( ActionEvent ae ) {
        // RemoteDiffDialog validates that there are exactly 1 or 2 paths in the
        // diff data
        RemoteDiffDialog dialog = new RemoteDiffDialog( getView(), data );
        GUIUtils.center( getView(), dialog );
        dialog.setVisible( true );
        data = dialog.getData();
        if ( data == null ) {
            return ;     // null means user canceled
        }

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

        // set up the console output
        data.setOut( new ConsolePrintStream( getView() ) );

        // show the svn console
        getView().getDockableWindowManager().showDockableWindow( "subversion" );
        final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
        panel.showConsole( );

        logger = panel.getLogger();
        log( jEdit.getProperty( "ips.Preparing_to_diff...", "Preparing to diff..." ) );

        class Runner extends SwingWorker < File[], Object > {

            @Override
            public File[] doInBackground() {
                try {
                    log( data.toString() );
                    //System.out.println("+++++ RemoteDiffAction");
                    String path1 = data.getPaths().get( 0 );
                    if ( path1.startsWith( data.getURL() ) ) {
                        path1 = path1.substring( data.getURL().length() );
                    }
                    if ( path1.startsWith( "/" ) ) {
                        path1 = path1.substring( 1 );
                    }
                    String path2 = data.getPaths().get( 1 );
                    if ( path2 == null ) {
                        path2 = path1;
                    }
                    if ( path2.startsWith( data.getURL() ) ) {
                        path2 = path2.substring( data.getURL().length() );
                    }
                    if ( path2.startsWith( "/" ) ) {
                        path2 = path2.substring( 1 );
                    }
                    //System.out.println("+++++ path1 = " + path1);
                    //System.out.println("+++++ path2 = " + path2);

                    BrowseRepository br = new BrowseRepository();
                    File remote1 = br.getFile( data.getURL(), path1, data.getRevision1(), data.getUsername(), data.getPassword() );
                    File remote2 = br.getFile( data.getURL(), path2, data.getRevision2(), data.getUsername(), data.getPassword() );
                    //System.out.println("+++++ file1 = " + remote1);
                    //System.out.println("+++++ file2 = " + remote2);

                    File[] files = new File[ 2 ];
                    files[ 0 ] = remote1;
                    files[ 1 ] = remote2;
                    return files;
                }
                catch ( Exception e ) {
                    e.printStackTrace();
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
                    data.getOut().printError( "Stopped 'Diff' action." );
                    data.getOut().close();
                }
                else {
                    data.getOut().printError( "Unable to stop 'Diff' action." );
                }
                return cancelled;
            }

            @Override
            protected void done() {
                if ( isCancelled() ) {
                    return ;
                }

                try {
                    File[] files = get();
                    if ( files == null ) {
                        JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                        return ;
                    }
                    final File remote1 = files[ 0 ];
                    final File remote2 = files[ 1 ];

                    if ( remote1 == null || remote2 == null ) {
                        JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                        return ;
                    }
                    if ( remote1.isDirectory() || remote2.isDirectory() ) {
                        JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_compare_directories.", "Unable to compare directories." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                        return ;
                    }

                    // show JDiff
                    getView().unsplit();
                    DualDiffManager.toggleFor( getView() );

                    Runnable r = new Runnable() {
                                public void run() {
                                    // set the edit panes in the view
                                    EditPane[] editPanes = getView().getEditPanes();

                                    // always show the 1st remote revision in the left edit pane
                                    editPanes[ 0 ].setBuffer( jEdit.openFile( getView(), remote1.getAbsolutePath() ) );

                                    editPanes[ 1 ].setBuffer( jEdit.openFile( getView(), remote2.getAbsolutePath() ) );

                                    // show the jdiff dockable
                                    getView().getDockableWindowManager().showDockableWindow( "jdiff-lines" );

                                    // do an explicit repaint of the view to clean up the display
                                    getView().repaint();
                                }
                            };
                    SwingUtilities.invokeLater( r );
                }
                catch ( Exception e ) {
                    // ignored
                    e.printStackTrace();
                }
            }
        }
        Runner runner = new Runner();
        panel.addWorker( "Diff", runner );
        runner.execute();
    }
}