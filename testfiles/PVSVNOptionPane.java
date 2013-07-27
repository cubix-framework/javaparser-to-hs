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

package ise.plugin.svn.gui;

// imports
import java.io.File;
import java.util.*;
import javax.swing.*;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.HistoryTextField;

import ise.java.awt.KappaLayout;
import ise.plugin.svn.SVNPlugin;
import ise.plugin.svn.PVHelper;
import ise.plugin.svn.library.PasswordHandler;
import common.swingworker.SwingWorker;
import ise.plugin.svn.data.*;
import ise.plugin.svn.command.*;
import ise.plugin.svn.io.*;
import static ise.plugin.svn.gui.HistoryModelNames.*;

import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;

/**
 * Option pane for setting the url, username, and password for subversion via
 * ProjectViewer.
 */
public class PVSVNOptionPane extends AbstractOptionPane {

    private JLabel url_label;
    private HistoryTextField url;
    private JLabel username_label;
    private HistoryTextField username;
    private JLabel password_label;
    private JPasswordField password;
    private JLabel fileformat_label;
    private JComboBox fileformat;

    private int wcVersion = -1;

    private String projectName = null;
    private File projectRoot = new File( PVHelper.getProjectRoot( jEdit.getActiveView() ) );

    private static final String internalName = "ise.plugin.svn.pv.options";

    public PVSVNOptionPane( String projectName ) {
        super( internalName );
        setLayout( new KappaLayout() );
        this.projectName = projectName;
    }

    /** Initialises the option pane. */
    protected void _init() {
        if ( projectName == null ) {
            projectName = getProjectName();
        }

        // url field
        url_label = new JLabel( jEdit.getProperty( PVHelper.PREFIX + "url.label" ) );
        url = new HistoryTextField( URL );
        url.setText( jEdit.getProperty( PVHelper.PREFIX + projectName + ".url" ) );
        url.setColumns( 30 );

        // username field
        username_label = new JLabel( jEdit.getProperty( PVHelper.PREFIX + "username.label" ) );
        username = new HistoryTextField( USERNAME );
        username.setText( jEdit.getProperty( PVHelper.PREFIX + projectName + ".username" ) );
        username.setColumns( 30 );

        // password field
        password_label = new JLabel( jEdit.getProperty( PVHelper.PREFIX + "password.label" ) );
        String pwd = jEdit.getProperty( PVHelper.PREFIX + projectName + ".password" );
        pwd = PasswordHandler.decryptPassword( pwd );
        password = new JPasswordField( pwd, 30 );

        // subversion file format
        fileformat_label = new JLabel( jEdit.getProperty( "ips.Subversion_file_format>", "Subversion file format:" ) );
        fileformat = new JComboBox( new String[] {"1.3", "1.4", "1.5", "1.6"} );
        fileformat.setEditable( false );
        String wc_item;
        switch ( getWCVersion() ) {
            case SVNAdminAreaFactory.WC_FORMAT_13:
                wc_item = "1.3";
                break;
            case SVNAdminAreaFactory.WC_FORMAT_14:
                wc_item = "1.4";
                break;
            case SVNAdminAreaFactory.WC_FORMAT_15:
                wc_item = "1.5";
                break;
            case SVNAdminAreaFactory.WC_FORMAT_16:
            default:
                wc_item = "1.6";
                break;

        }
        fileformat.setSelectedItem( wc_item );

        // initially, some parts are not visible, they are made visible in the
        // swing worker thread.
        url.setVisible( false );
        username_label.setVisible( false );
        username.setVisible( false );
        password_label.setVisible( false );
        password.setVisible( false );
        fileformat_label.setVisible( false );
        fileformat.setVisible( false );

        // add the components to the option panel
        add( "0, 0, 3, 1, W,  , 3", new JLabel( "<html><b>" + jEdit.getProperty( "ips.Subversion_Settings", "Subversion Settings" ) + "</b>" ) );

        add( "0, 1, 1, 1, E,  , 3", url_label );
        add( "1, 1, 2, 1, 0, w, 3", url );

        add( "0, 2, 1, 1, E,  , 3", username_label );
        add( "1, 2, 2, 1, 0, w, 3", username );

        add( "0, 3, 1, 1, E,  , 3", password_label );
        add( "1, 3, 2, 1, 0, w, 3", password );

        add( "0, 4, 1, 1, E,  , 3", fileformat_label );
        add( "1, 4, 2, 1, 0, w, 3", fileformat );

        ( new Runner() ).execute();
    }

    // #_save() : void
    /** Saves properties from the option pane. */
    protected void _save() {
        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".url",
            ( url == null ? "" : url.getText() )
        );
        url.addCurrentToHistory();

        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".username",
            ( username == null ? "" : username.getText() )
        );
        username.addCurrentToHistory();

        char[] pwd_chars = password == null ? new char[ 0 ] : password.getPassword();
        String pwd = new String( pwd_chars );
        for ( int i = 0; i < pwd_chars.length; i++ ) {
            pwd_chars[ i ] = '0';
        }
        pwd = PasswordHandler.encryptPassword( pwd );
        jEdit.setProperty(
            PVHelper.PREFIX + projectName + ".password",
            pwd
        );

        // possibly change working copy format
        int current_wc_format = getWCVersion();
        final String new_wc_format = ( String ) fileformat.getSelectedItem();
        int wc_format;
        if ( "1.3".equals( new_wc_format ) ) {
            wc_format = SVNAdminAreaFactory.WC_FORMAT_13;
        }
        else if ( "1.4".equals( new_wc_format ) ) {
            wc_format = SVNAdminAreaFactory.WC_FORMAT_14;
        }
        else if ( "1.5".equals( new_wc_format ) ) {
            wc_format = SVNAdminAreaFactory.WC_FORMAT_15;
        }
        else {
            wc_format = SVNAdminAreaFactory.WC_FORMAT_16;
        }
        if ( wc_format != current_wc_format ) {
            // put this in a SwingWorker, otherwise, it takes a long time on
            // large projects and causes the UI to hang until it is done.
            final int wcf = wc_format;
            jEdit.getActiveView().getDockableWindowManager().showDockableWindow( "subversion" );
            final OutputPanel panel = SVNPlugin.getOutputPanel( jEdit.getActiveView() );
            panel.showConsole();
            class Runner extends SwingWorker < Object , Object > {
                ConsolePrintStream out = new ConsolePrintStream( jEdit.getActiveView() );
                @Override
                protected Object doInBackground() {
                    try {
                        out.println(jEdit.getProperty("ips.Converting_svn_working_copy_format_to_", "Converting svn working copy format to ") + new_wc_format + "...");
                        SVNWCClient wc_client = SVNClientManager.newInstance().getWCClient();
                        wc_client.doSetWCFormat( projectRoot, wcf );
                    }
                    catch ( Exception e ) {
                        JOptionPane.showMessageDialog( jEdit.getActiveView(), jEdit.getProperty( "ips.Unable_to_convert_working_copy_file_format>", "Unable to convert working copy file format:" ) + "\n" + e.getMessage(), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                    }
                    return null;
                }
                @Override
                public boolean cancel( boolean mayInterruptIfRunning ) {
                    out.printError( jEdit.getProperty("ips.Unable_to_stop_conversion_of_working_file_format.", "Unable to stop conversion of working file format.") );
                    return false;
                }
                @Override
                protected void done() {
                    out.println(jEdit.getProperty("ips.Completed_converting_working_copy_format_to_", "Completed converting working copy format to ") + new_wc_format + ".");
                    out.close();
                }
            }
            Runner runner = new Runner();
            panel.addWorker( jEdit.getProperty("ips.Converting", "Converting"), runner );
            runner.execute();
        }
    }

    private int getWCVersion() {
        if ( wcVersion != -1 ) {
            return wcVersion;
        }
        try {
            SVNStatusClient st_client = SVNClientManager.newInstance().getStatusClient();
            SVNStatus status = st_client.doStatus( projectRoot, false );
            wcVersion = status.getWorkingCopyFormat();
            return wcVersion;
        }
        catch ( Exception e ) {
            return jEdit.getIntegerProperty( "ise.plugin.svn.defaultWCVersion", SVNAdminAreaFactory.WC_FORMAT_15 );

        }
    }

    private String getProjectName() {
        projectName = PVHelper.getProjectName( ( View ) SwingUtilities.getRoot( this ) ) == null ? "" : PVHelper.getProjectName( ( View ) SwingUtilities.getRoot( this ) );
        return projectName;
    }

    private String getProjectRoot() {
        String project_root = PVHelper.getProjectRoot( ( View ) SwingUtilities.getRoot( this ) ) == null ? "" : PVHelper.getProjectRoot( ( View ) SwingUtilities.getRoot( this ) );
        return project_root;
    }

    class Runner extends SwingWorker<List<SVNInfo>, Object> {

        @Override
        public List<SVNInfo> doInBackground() {
            try {
                // adjust the UI while fetching the svn info
                url_label.setText( jEdit.getProperty( "ips.Just_a_moment,_attempting_to_fetch_current_SVN_info...", "Just a moment, attempting to fetch current SVN info..." ) );

                // fetch any existing svn info
                java.util.List<String> info_path = new java.util.ArrayList<String>();
                info_path.add( getProjectRoot() );
                SVNData info_data = new SVNData();
                info_data.setOut( new ConsolePrintStream( new NullOutputStream() ) );
                info_data.setPaths( info_path );
                info_data.setPathsAreURLs( false );
                Info info = new Info();
                List<SVNInfo> results = info.info( info_data );
                info_data.getOut().close();
                return results;
            }
            catch ( Exception e ) {     // NOPMD
                // ignored
            }
            return null;
        }

        @Override
        protected void done() {
            try {
                // populate url field from existing svn info, if available
                url_label.setText( jEdit.getProperty( PVHelper.PREFIX + "url.label" ) );
                List<SVNInfo> info_results = get();
                String url_text = null;
                if ( info_results != null && info_results.size() > 0 ) {
                    SVNInfo svn_info = info_results.get( 0 );
                    if ( svn_info != null && svn_info.getURL() != null ) {
                        url_text = svn_info.getURL().toString();
                    }
                }
                if ( url_text != null ) {
                    url.setText( url_text );
                }

                // make the UI visible
                url.setVisible( true );
                username_label.setVisible( true );
                username.setVisible( true );
                password_label.setVisible( true );
                password.setVisible( true );
                fileformat_label.setVisible( true );
                fileformat.setVisible( true );
            }
            catch ( Exception e ) {     // NOPMD
                // ignored
            }
        }
    }

}