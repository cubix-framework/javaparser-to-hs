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
import ise.plugin.svn.data.CheckoutData;
import ise.plugin.svn.gui.DirTreeNode;
import ise.plugin.svn.io.ConsolePrintStream;
import common.swingworker.*;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.JTree;
import javax.swing.tree.*;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

public class BrowseRepositoryAction extends SVNAction {

    private JTree tree = null;
    private DirTreeNode node = null;
    private CheckoutData data = null;

    public BrowseRepositoryAction( View view, JTree tree, DirTreeNode node, CheckoutData data ) {
        super( view, jEdit.getProperty( "ips.Browse_Repository", "Browse Repository" ) );
        this.tree = tree;
        this.node = node;
        this.data = data;
        if ( tree == null || node == null || data == null || data.getURL() == null ) {
            throw new IllegalArgumentException( "neither tree, node, nor url can be null" );
        }
    }

    public void actionPerformed( ActionEvent ae ) {
        data.setOut( new ConsolePrintStream( getView() ) );
        getView().getDockableWindowManager().showDockableWindow( "subversion" );
        getView().getDockableWindowManager().showDockableWindow( "subversion.browser" );
        final OutputPanel panel = SVNPlugin.getOutputPanel( getView() );
        panel.showConsole();
        Logger logger = panel.getLogger();
        logger.log( Level.INFO, jEdit.getProperty( "ips.Fetching_repository_info_for", "Fetching repository info for" ) + "\n" + data.getURL() + "..." );
        for ( Handler handler : logger.getHandlers() ) {
            handler.flush();
        }

        class Runner extends SwingWorker < List<DirTreeNode>, Object> {

            @Override
            public List<DirTreeNode> doInBackground() {
                tree.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
                tree.setEditable( false );
                try {
                    BrowseRepository br = new BrowseRepository( );
                    return node.isExternal() ? br.getRepository( node, data ) : br.getRepository( data );
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
                    data.getOut().printError( "Stopped 'Browse Repository' action." );
                    data.getOut().close();
                }
                else {
                    data.getOut().printError( "Unable to stop 'Browse Repository' action." );
                }
                return cancelled;
            }

            @Override
            protected void done() {
                if ( isCancelled() ) {
                    return ;
                }

                try {
                    List<DirTreeNode> children = get();
                    for ( DirTreeNode child : children ) {
                        node.add( child );
                    }
                    DefaultTreeModel model = ( DefaultTreeModel ) tree.getModel();
                    model.nodeStructureChanged( node );
                    TreePath path = new TreePath( node.getPath() );
                    tree.revalidate();
                    tree.repaint();
                    tree.expandPath( path );
                }
                catch ( Exception e ) {     // NOPMD
                    // ignored
                }
                finally {
                    tree.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
                    tree.setEditable( true );
                    tree.requestFocus();
                }
            }
        }
        Runner runner = new Runner();
        panel.addWorker( "Browse Repository", runner );
        runner.execute();
    }
}