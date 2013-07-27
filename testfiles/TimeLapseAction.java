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
import ise.plugin.svn.command.SVNKit;
import ise.plugin.svn.data.DiffData;
import ise.plugin.svn.io.ConsolePrintStream;
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
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.io.SVNFileRevision;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc.SVNClientManager;


public class TimeLapseAction extends SVNAction {


    private String path1 = null;
    private String revision1 = null;
    private String revision2 = null;

    private DiffData data;
    private Logger logger = null;

    /**
     * Do a time lapse of the given file.
     * @param view the View in which to display results
     * @param path the name of a local file to be diffed.  A dialog will be shown
     * to let the user pick the revision to diff against.
     * @param username the username for the svn repository
     * @param password the password for the username
     */
    public TimeLapseAction( View view, String path, String username, String password ) {
        super( view, jEdit.getProperty( "ips.TimeLapse", "TimeLapse" ) );
        if ( path == null || path.length() == 0 )
            throw new IllegalArgumentException( "path may not be null" );
        this.path1 = path;
        setUsername( username );
        setPassword( password );
    }

    public TimeLapseAction( View view, DiffData data ) {
        super( view, jEdit.getProperty( "ips.TimeLapse", "Time Lapse" ) );
        this.data = data;
        if ( data.getPaths() == null || data.getPaths().size() != 1 ) {
            throw new IllegalArgumentException( "paths may not be null" );
        }
        this.path1 = data.getPaths().get( 0 );

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

            // diffing two repository versions of the same file
            data = new DiffData();
            data.addPath( path1 );
            data.setRevision1( SVNRevision.parse( revision1 ) );
            data.setRevision2( SVNRevision.parse( revision2 ) );

            if ( getUsername() == null && data.getUsername() == null ) {
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
            panel.showConsole( );

            logger = panel.getLogger();

            SwingWorker runner = new TimeLapseRunner();
            panel.addWorker( "Time Lapse", runner );
            runner.execute();
        }
    }

    /**
     * Uses JDiff to show the differences between 2 files.
     */
    class TimeLapseRunner extends SwingWorker < File[], Object > {

        // - get list of revisions for the specified file
        // - get contents of latest and previous revisions, show in right and left
        // panes, respectively, using JDiff -- show full content? Just svn diff?
        // Perforce shows full content with changes merged into a single buffer,
        // Jonathan's time lapse shows just svn diff.  Cache these.
        // - add slider at top? In dockable?
        // - option to show blame? Older version of Perforce showed blame in edit
        // pane, new versions show detail in dockable.

        private SVNRepository repository = null;

        /**
         * Get the revisions for path1, only returns those revisions for which path1
         * actually had a change.
         * @return a list of SVNFileRevisions pertaining to path1.
         */
        private List<SVNFileRevision> getRevisions() {
            List<SVNFileRevision> revisions = new ArrayList<SVNFileRevision>();
            try {
                SVNURL fullUrl = svnUrl( path1 );
                String url = fullUrl.removePathTail().toString();
                repository = getRepository( url );
                String filePath = fullUrl.getPath().replaceAll( ".*/", "" );
                if ( repository != null ) {
                    Collection revs = repository.getFileRevisions( filePath, null, 0, repository.getLatestRevision() );
                    for ( Object rev : revs ) {
                        revisions.add( ( SVNFileRevision ) rev );
                    }
                }
            }
            catch ( Exception e ) {
                e.printStackTrace();
            }
            return revisions;
        }

        /**
         * Normalizes the given file path or URL.
         *
         * @param filePathOrUrl  Subversion URL or working-copy file path
         * @return  the corresponding Subversion URL
         */
        private SVNURL svnUrl( String filePathOrUrl ) throws SVNException {
            SVNURL svnUrl;
            if ( new File( filePathOrUrl ).exists() ) {
                SVNKit.setupLibrary();
                SVNClientManager clientManager = SVNClientManager.newInstance( SVNWCUtil.createDefaultOptions( true ), data.getUsername(), data.getDecryptedPassword() );
                svnUrl = clientManager.getWCClient().doInfo( new File( filePathOrUrl ), SVNRevision.WORKING ).getURL();
            }
            else {
                svnUrl = SVNURL.parseURIEncoded( filePathOrUrl );
            }
            return svnUrl;
        }

        /**
         * @return the repository for the given url
         */
        public SVNRepository getRepository( String url ) throws SVNException {
            if ( repository != null ) {
                return repository;
            }

            try {
                SVNKit.setupLibrary();
                // create repository
                repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );

                // set up authentication
                ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(SVNPlugin.getSvnStorageDir(), data.getUsername(), data.getDecryptedPassword() );
                repository.setAuthenticationManager( authManager );
            }
            catch ( SVNException svne ) {
                // perhaps a malformed URL is the cause of this exception
                log( "Error while creating an SVNRepository for location '"
                     + url + "': " + svne.getMessage() );
                return null;
            }
            return repository;
        }

        @Override
        public File[] doInBackground() {
            try {
                log( "Time Lapse, fetching revisions for " + path1 + "..." );
                getRevisions();

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
                    JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                    return ;
                }
                final File remote1 = files[ 0 ];
                final File remote2 = files[ 1 ];

                if ( remote1 == null && remote2 == null ) {
                    JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
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
}