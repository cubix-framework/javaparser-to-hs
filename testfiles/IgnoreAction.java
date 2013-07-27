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
import ise.plugin.svn.command.Property;
import ise.plugin.svn.data.PropertyData;
import ise.plugin.svn.gui.IgnoreDialog;
import ise.plugin.svn.gui.PropertyPanel;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.library.GUIUtils;
import common.swingworker.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * ActionListener to show SVN properties.
 * This is not dependent on ProjectViewer.
 */
public class IgnoreAction extends SVNAction {

    private String path;
    private String username;
    private String password;


    /**
     * @param view the View in which to display results
     * @param data what to show
     */
    public IgnoreAction( View view, String path ) {
        this( view, path, null, null );
    }

    /**
     * @param view the View in which to display results
     * @param data what to show
     */
    public IgnoreAction( View view, String path, String username, String password ) {
        super( view, jEdit.getProperty( "ips.Ignore", "Ignore" ) );
        if ( path == null ) {
            throw new IllegalArgumentException( "path may not be null" );
        }
        this.path = path;
        this.username = username;
        this.password = password;
    }

    public void actionPerformed( ActionEvent ae ) {
        if ( path != null ) {

            // get settings from the user for what to ignore
            final IgnoreDialog dialog = new IgnoreDialog( getView(), path );
            GUIUtils.center( getView(), dialog );
            dialog.setVisible( true );
            if ( dialog.isCancelled() ) {
                return ;
            }

            /*
            System.out.println("+++++ dialog.getPath = " + dialog.getPath());
            System.out.println("+++++ dialog.getFilename = " + dialog.getFilename());
            System.out.println("+++++ dialog.getPattern = " + dialog.getPattern());
            System.out.println("+++++ dialog.getRecursive = " + dialog.getRecursive());
            */

            // set up the data transfer object
            final PropertyData data = new PropertyData();
            data.setOut( new ConsolePrintStream( getView() ) );

            List<String> paths = new ArrayList<String>();
            paths.add( dialog.getPath() );
            data.setPaths( paths );
            data.setUsername( username );
            data.setPassword( password );
            data.setRecursive( dialog.getRecursive() );
            data.setPathsAreURLs( false );
            data.setRevision( SVNRevision.WORKING );

            // set up the svn console
            getView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
            panel.showConsole();

            // property fetcher
            class Runner extends SwingWorker < TreeMap<String, Properties> , Object > {

                @Override
                public TreeMap<String, Properties> doInBackground() {
                    try {
                        // get the current properties
                        Property prop = new Property();
                        prop.doGetProperties( data );
                        Properties props = prop.getProperties().get( path );

                        // get any existing 'ignore' value
                        String to_ignore = null;
                        if ( props != null ) {
                            to_ignore = props.getProperty( "svn:ignore" );
                        }

                        // set 'ignore' property name
                        data.setName( "svn:ignore" );

                        // set the new ignore value
                        String filename = dialog.getPattern();
                        if ( filename == null ) {
                            filename = dialog.getFilename();
                        }
                        Set<String> set = new HashSet<String>();
                        if ( to_ignore != null ) {
                            String[] old_ignored = to_ignore.split( "\n" );
                            for ( String i : old_ignored ) {
                                set.add( i );
                            }
                        }
                        set.add( filename );
                        StringBuffer sb = new StringBuffer();
                        for ( String i : set ) {
                            sb.append( i ).append( "\n" );
                        }
                        data.setValue( sb.toString() );

                        // actually set the 'ignore' property
                        prop.doSetProperties( data );

                        // get the results to display to the user
                        TreeMap<String, Properties> rtn = prop.getProperties();
                        props = rtn.get( path );
                        if ( props != null ) {
                            props.put( "svn:ignore", sb.toString() );
                        }
                        else {
                            Properties p = new Properties();
                            p.put( "svn:ignore", sb.toString() );
                            rtn.put( path, p );
                        }
                        return rtn;
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
                        data.getOut().printError( "Stopped 'Ignore' action." );
                        data.getOut().close();
                    }
                    else {
                        data.getOut().printError( "Unable to stop 'Ignore' action." );
                    }
                    return cancelled;
                }

                @Override
                protected void done() {
                    if ( isCancelled() ) {
                        return ;
                    }

                    try {
                        TreeMap<String, Properties> results = get();
                        if ( results != null ) {
                            panel.addTab( jEdit.getProperty( "ips.Ignore", "Ignore" ), new PropertyPanel( getView(), results, data ) );
                        }
                        else {
                            JOptionPane.showMessageDialog( getView(), jEdit.getProperty( "ips.No_properties_found.", "No properties found." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                        }
                    }
                    catch ( Exception e ) {     // NOPMD
                        // ignored
                        //e.printStackTrace();
                    }
                }
            }

            // fetch the properties
            Runner runner = new Runner();
            panel.addWorker( "Ignore", runner );
            runner.execute();
        }
    }
}