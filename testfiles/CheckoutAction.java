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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;
import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;
import projectviewer.config.ProjectOptions;
import projectviewer.config.VersionControlService;
import projectviewer.importer.RootImporter;
import projectviewer.vpt.*;
import ise.plugin.svn.gui.CheckoutDialog;
import ise.plugin.svn.gui.OutputPanel;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.io.*;
import ise.plugin.svn.data.*;
import ise.plugin.svn.*;
import java.util.logging.*;
import ise.plugin.svn.command.*;
import common.swingworker.SwingWorker;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;


public class CheckoutAction extends SVNAction implements PropertyChangeListener {

    private CheckoutData data = null;

    /**
     * @param view the View in which to display results
     * @param data information necessary to do a checkout
     */
    public CheckoutAction( View view, CheckoutData data ) {
        super( view, jEdit.getProperty( "ips.Checkout", "Checkout" ) );
        if ( data == null )
            throw new IllegalArgumentException( "data may not be null" );
        this.data = data;
        setUsername( data.getUsername() );
        setPassword( data.getPassword() );
    }

    public void propertyChange( PropertyChangeEvent pce ) {
        // check for done
        if ( "done".equals( pce.getPropertyName() ) ) {
            createProject( pce.getNewValue().toString() );
        }
    }

    public void actionPerformed( ActionEvent ae ) {
        CheckoutDialog dialog = new CheckoutDialog( getView(), data.getURL() );
        GUIUtils.center( getView(), dialog );
        dialog.setVisible( true );
        data = dialog.getValues();
        if ( data == null ) {
            return ;        // user canceled
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

        data.setOut( new ConsolePrintStream( getView() ) );

        getView().getDockableWindowManager().showDockableWindow( "subversion" );
        final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
        panel.showConsole();
        Logger logger = panel.getLogger();
        logger.log( Level.INFO, jEdit.getProperty( "ips.Check_out_...", "Check out ..." ) );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }

        class Runner extends SwingWorker<Long, Object> {

            @Override
            public Long doInBackground() {
                try {
                    Checkout checkout = new Checkout();
                    return checkout.doCheckout( data );
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
                    data.getOut().printError( "Stopped 'Checkout' action." );
                    data.getOut().close();
                }
                else {
                    data.getOut().printError( "Unable to stop 'Checkout' action." );
                }
                return cancelled;
            }

            @Override
            protected void done() {
                if ( isCancelled() ) {
                    return ;
                }

                try {
                    Long revision = get();
                    if ( revision == null ) {
                        throw new Exception( jEdit.getProperty( "ips.Checkout_failed.", "Checkout failed." ) ); // NOPMD
                    }
                    data.getOut().print( jEdit.getProperty( "ips.Checkout_completed,_revision", "Checkout completed, revision" ) + " " + revision );
                    firePropertyChange( "done", "false", revision.toString() );
                }
                catch ( Exception e ) {
                    data.getOut().printError( e.getMessage() );
                }
            }

        }
        Runner runner = new Runner();
        runner.addPropertyChangeListener( this );
        panel.addWorker( "Checkout", runner );
        runner.execute();
    }

    private void createProject( String revision ) {
        int make_project = JOptionPane.showConfirmDialog( getView(),
                jEdit.getProperty( "ips.Checkout_complete_at_revision", "Checkout complete at revision" ) + " " + revision + ".\n" +
                jEdit.getProperty( "ips.Would_you_like_to_create_a_project_from_these_files?", "Would you like to create a project from these files?" ),
                jEdit.getProperty( "ips.Create_Project?", "Create Project?" ),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE );
        if ( make_project != JOptionPane.YES_OPTION ) {
            return ;
        }

        // use the directory name that the user entered for the location of the
        // checkout as the default project name.  The UI will let the user
        // change it if they want.
        String path = data.getPaths().get( 0 );
        int index = path.lastIndexOf( "/" );
        index = index == -1 ? 0 : index + 1;
        String project_name = path.substring( index );
        final VPTProject project = new VPTProject( project_name );

        // set some project properties so the user doesn't have to
        project.setRootPath( path );
        saveProjectSVNInfo( project.getName() );
        project.setProperty( VersionControlService.VC_SERVICE_KEY, "Subversion" );

        // show the 'create project' dialog
        ProjectOptions.run( project, true, null );

        // get the group as set in the 'create project' dialog
        VPTGroup group = ( VPTGroup ) project.getParent();
        if ( group == null ) {
            group = VPTRoot.getInstance();
        }

        // actually add the project to ProjectManager and set it as the active project
        ProjectManager.getInstance().addProject( project, group );
        ProjectViewer.setActiveNode( jEdit.getActiveView(), project );

        // import the checked out files into the project. This next line is a suggestion
        // from Marcelo that will automatically choose the 'Use CVS or SVN Entries' for
        // importing the files.
        projectviewer.importer.ImportUtils.saveFilter( project.getProperties(), new projectviewer.importer.CVSEntriesFilter(), "projectviewer.import" );
        RootImporter ipi = new RootImporter( project, null, ProjectViewer.getViewer( jEdit.getActiveView() ), jEdit.getActiveView() );
        // DONE: this doesn't work any more, change in PV API? It appears the locking code
        // has been removed from PV and this next line isn't needed any more.
        //ipi.setLockProject( false );
        ipi.doImport();

        // now show ProjectViewer
        getView().getDockableWindowManager().showDockableWindow( "projectviewer" );
    }

    private void saveProjectSVNInfo( String projectName ) {
        if ( projectName == null || projectName.length() == 0 ) {
            return ;
        }

        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".url",
            ( data.getURL() == null ? "" : data.getURL() )
        );

        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".username",
            ( getUsername() == null ? "" : getUsername() )
        );

        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".password",
            getPassword()
        );
    }

}