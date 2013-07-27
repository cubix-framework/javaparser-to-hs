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

import common.swingworker.*;

import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.command.BrowseRepository;
import ise.plugin.svn.command.Diff;
import ise.plugin.svn.command.Info;
import ise.plugin.svn.data.DiffData;
import ise.plugin.svn.gui.DiffDialog;
import ise.plugin.svn.gui.OutputPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jdiff.DualDiffManager;

import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * ActionListener to perform sort of an svn diff.  While subversion can do a diff,
 * I'm delegating to the JDiff plugin to create and display the diff.
 * This is not dependent on ProjectViewer.
 * TODO: fix this class so the two constructors that don't take a DiffData create
 * a DiffData out of the parameters.
 */
public class DiffAction extends SVNAction {

    private DiffDialog dialog = null;

    private String path1 = null;
    private String path2 = null;
    private String revision1 = null;
    private String revision2 = null;

    private DiffData data = null;
    private Logger logger = null;

    /**
     * Diff a file against a previous version of the same file.
     * @param view the View in which to display results
     * @param path the name of a local file to be diffed.  A dialog will be shown
     * to let the user pick the revision to diff against.
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public DiffAction( View view, String path, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Diff", "Diff" ) );
        if ( path == null || path.length() == 0 ) {
            throw new IllegalArgumentException( "path may not be null" );
        }
        this.path1 = path;
        setUsername( username );
        setPassword( password );
    }

    /**
     * Do a diff of a single file given 2 different revisions of the file.
     * @param view the View in which to display results
     * @param path the name of a local file to be diffed. No dialog will be shown
     * here, the revisions must have already been selected.
     * @param revision1 a revision of path
     * @param revision2 another revision of path
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public DiffAction( View view, String path, String revision1, String revision2, String username, String password ) {
        super( view, "Diff" );
        if ( path == null || path.length() == 0 ) {
            throw new IllegalArgumentException( "path may not be null" );
        }
        if ( revision1 == null || revision2 == null ) {
            throw new IllegalArgumentException( "neither revision may be null, " + ( revision1 == null ? "revision1" : "revision2" ) + " is null." );
        }
        this.path1 = path;
        this.revision1 = revision1;
        this.revision2 = revision2;
        setUsername( username );
        setPassword( password );
    }

    /**
     * Assumes that the DiffData contains everything needed to perform the diff.
     * @param view The view in which to display the results.
     * @param data the DiffData object containing everything necessary to perform the diff.
     */
    public DiffAction( View view, DiffData data ) {
        super( view, jEdit.getProperty( "ips.Diff", "Diff" ) );
        if ( data == null ) {
            throw new IllegalArgumentException( "data may not be null" );
        }
        this.data = data;
        if ( data.getPaths() == null || data.getPaths().size() == 0 ) {
            throw new IllegalArgumentException( "paths may not be null" );
        }
        setUsername( data.getUsername() );
        setPassword( data.getPassword() );
    }

    private void log( String msg ) {
        logger.log( Level.INFO, msg );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }
    }

    public void actionPerformed( ActionEvent ae ) {
        if ( ( path1 != null && path1.length() > 0 ) || data != null ) {

            // pick or set the revisions
            if ( data == null && revision1 == null && path2 == null ) {
                // if here, then the first constructor was called, the user is
                // wanting to diff a local file against a remote version of the
                // file. Show a DiffDialog to get the revision of the remote file.
                dialog = new DiffDialog( getView(), path1 );
                GUIUtils.center( getView(), dialog );
                dialog.setVisible( true );
                data = dialog.getData();
                if ( data == null ) {
                    return ;     // null means user canceled
                }
            }
            else if ( data == null ) {
                // diffing two repository versions of the same file
                data = new DiffData();
                data.addPath( path1 );
                data.setRevision1( SVNRevision.parse( revision1 ) );
                data.setRevision2( SVNRevision.parse( revision2 ) );
            }

            // don't need username/password if diffing against base
            if ( !data.getRevision1().equals( SVNRevision.BASE ) && getUsername() == null && data.getUsername() == null ) {
                verifyLogin( data.getPaths() == null ? null : data.getPaths().get( 0 ) );
                if ( isCanceled() ) {
                    return ;
                }
                data.setUsername( getUsername() );
                data.setPassword( getPassword() );
            }
            else if ( data.getUsername() != null ) {
                setUsername( data.getUsername() );
                setPassword( data.getPassword() );
            }
            else {
                data.setUsername( getUsername() );
                data.setPassword( getPassword() );
            }

            // set up the console output
            data.setOut( new ConsolePrintStream( getView() ) );

            // show the svn console
            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            //panel.showConsole( );

            logger = panel.getLogger();
            log( data.toString() );

            SwingWorker runner;
            if ( data.getSvnDiff() ) {
                runner = new SVNDiffRunner();
            }
            else {
                runner = new JDiffRunner();
            }
            panel.addWorker( "Diff", runner );
            runner.execute();
        }
    }

    private boolean lessThan( SVNRevision rev1, SVNRevision rev2 ) {
        if ( rev1.getDate() != null && rev2.getDate() != null ) {
            return rev1.getDate().getTime() < rev2.getDate().getTime();
        }
        return rev1.getNumber() < rev2.getNumber();
    }

    /**
     * Uses JDiff to show the differences between 2 files.
     */
    class JDiffRunner extends SwingWorker < File[], Object > {

        @Override
        public File[] doInBackground() {
            try {
                log( jEdit.getProperty( "ips.Preparing_to_diff...", "Preparing to diff..." ) );
                log( data.toString() );
                String url = null;
                String svn_path = null;
                File remote1 = null;
                File remote2 = null;

                // fetch repository and path info about the file
                Info info = new Info( );
                List<SVNInfo> infos = info.getInfo( data );
                if ( infos.size() == 0 ) {
                    return null;
                }
                SVNInfo svn_info = infos.get( 0 );
                url = svn_info.getRepositoryRootURL().toString();
                svn_path = svn_info.getURL().toString();
                svn_path = svn_path.substring( url.length() );
                BrowseRepository br = new BrowseRepository();

                // there should always be one remote revision to fetch for diffing against a working copy
                // or for diffing against another revision
                log( jEdit.getProperty( "ips.Diff,_fetching_file_data...", "Diff, fetching file data..." ) );
                if ( data.getRevision1().equals( SVNRevision.WORKING ) ) {
                    remote1 = new File( data.getPaths().get( 0 ) );
                }
                else if ( data.getRevision1().equals( SVNRevision.BASE ) ) {
                    // copy contents of BASE revision to tmp dir...
                    // create the temp file name
                    // TODO: can't I do this with File methods rather than string manipulation?
                    String filepath = data.getPaths().get( 0 );
                    int index = filepath.lastIndexOf( '.' );
                    index = index < 0 ? 0 : index;
                    if ( index == 0 ) {
                        int slash_index = filepath.lastIndexOf( '/' );
                        if ( slash_index > 0 && slash_index < filepath.length() ) {
                            index = slash_index + 1;
                        }
                    }
                    String filename = System.getProperty( "java.io.tmpdir" ) + '/' + filepath.substring( 0, index ) + "-BASE" + filepath.substring( index );

                    // create the temp file
                    remote1 = new File( filename );
                    if ( remote1.exists() ) {
                        remote1.delete();
                    }
                    remote1.deleteOnExit();     // automatic cleanup
                    remote1.getParentFile().mkdirs();

                    // copy the file contents to the temp file
                    BufferedOutputStream os = new BufferedOutputStream( new FileOutputStream( remote1 ) );
                    SVNClientManager cm = SVNClientManager.newInstance();
                    SVNWCClient client = cm.getWCClient();
                    client.doGetFileContents( new File( filepath ), SVNRevision.UNDEFINED, SVNRevision.BASE, false, os );
                    os.flush();
                    os.close();
                }
                else {
                    remote1 = br.getFile( url, svn_path, data.getRevision1(), data.getUsername(), data.getPassword() );
                }

                // there may be a second remote revision for diffing between 2 remote revisions
                remote2 = null;
                if ( data.getRevision2() != null ) {
                    log( jEdit.getProperty( "ips.Diff,_fetching_revision_data...", "Diff, fetching revision data..." ) );
                    remote2 = br.getFile( url, svn_path, data.getRevision2(), data.getUsername(), data.getPassword() );

                    // sort, oldest revision first
                    boolean lessThan = lessThan( data.getRevision2(), data.getRevision1() );
                    if ( lessThan ) {
                        File temp = remote1;
                        remote1 = remote2;
                        remote2 = temp;
                        temp = null;
                    }
                }
                File[] files = new File[ 2 ];
                files[ 0 ] = remote1;
                files[ 1 ] = remote2;
                return files;
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
                    JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ) + "\nNo files.", jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                    return ;
                }
                final File remote1 = files[ 0 ];
                final File remote2 = files[ 1 ];

                if ( remote1 == null && remote2 == null ) {
                    JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ) + "\nNull files.", jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                    return ;
                }
                if ( ( remote1 != null && remote1.isDirectory() ) || ( remote2 != null && remote2.isDirectory() ) ) {
                    JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_compare_directories.", "Unable to compare directories." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                    return ;
                }

                // show JDiff
                DualDiffManager.toggleFor( getView() );

                SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                // set the edit panes in the view
                                EditPane[] editPanes = getView().getEditPanes();

                                if ( remote2 == null ) {
                                    // show the local working copy in the right edit pane
                                    editPanes[ 1 ].setBuffer( jEdit.openFile( getView(), path1 ) );
                                }
                                else {
                                    // or show the 2nd remote revision in the right edit pane
                                    editPanes[ 1 ].setBuffer( jEdit.openFile( getView(), remote2.getAbsolutePath() ) );
                                }

                                // do an explicit repaint of the view to clean up the display
                                getView().repaint();
                            }
                        }
                                          );
                SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                // set the edit panes in the view
                                EditPane[] editPanes = getView().getEditPanes();

                                // always show the 1st remote revision in the left edit pane
                                editPanes[ 0 ].setBuffer( jEdit.openFile( getView(), remote1.getAbsolutePath() ) );

                                // do an explicit repaint of the view to clean up the display
                                getView().repaint();
                            }
                        }
                                          );

            }
            catch ( Exception e ) {
                e.printStackTrace();
                log( "Error: " + e.getMessage() );
            }
        }
    }

    class SVNDiffRunner extends SwingWorker < String, Object > {

        @Override
        public String doInBackground() {
            try {
                log( jEdit.getProperty( "ips.Preparing_SVN_diff...", "Preparing SVN diff..." ) );
                Diff diff = new Diff();
                return diff.diff( data );
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
                String filediff = get();
                if ( filediff != null ) {
                    jEdit.newFile( getView() ).insert( 0, filediff );
                    log( jEdit.getProperty( "ips.SVN_Diff_created.", "SVN Diff created." ) );
                }
                else {
                    log( jEdit.getProperty( "ips.Unable_to_create_SVN_diff.", "Unable to create SVN diff." ) );
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
                log( "Error: " + e.getMessage() );
            }
        }
    }

}