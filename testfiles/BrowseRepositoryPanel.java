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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import ise.plugin.svn.action.*;
import ise.plugin.svn.command.BrowseRepository;
import ise.plugin.svn.data.RepositoryData;
import ise.plugin.svn.gui.br.*;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.library.PrivilegedAccessor;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Registers;

import org.tmatesoft.svn.core.wc.*;

/**
 * Tree display for an SVN repository.
 */
public class BrowseRepositoryPanel extends JPanel {

    private View view = null;
    private RepositoryComboBox chooser = null;
    private String repositoryUrl = null;
    private JTree tree = null;
    //private JPopupMenu popupMenu = null;
    private String username = null;
    private String password = null;

    private JButton new_btn;
    private JButton edit_btn;
    private JButton remove_btn;
    private JButton refresh_btn;

    public BrowseRepositoryPanel( View view ) {
        this( view, true );
    }

    public BrowseRepositoryPanel( View view, boolean full ) {
        super( new BorderLayout() );
        this.view = view;
        init( full, null );
    }

    public BrowseRepositoryPanel( View view, String defaultDestination ) {
        this( view, defaultDestination, true );
    }

    public BrowseRepositoryPanel( View view, String defaultDestination, boolean full ) {
        super( new BorderLayout() );
        this.view = view;
        init( full, defaultDestination );
    }

    private void init( boolean full, String repositoryName ) {

        setName( "browse repository panel" );

        // for button panel, defined below.
        JPanel button_panel = null;

        // repository chooser
        chooser = new RepositoryComboBox();
        chooser.setName( "repository chooser" );

        // the repository tree.  This is lazy loaded.
        tree = new JTree( new DefaultTreeModel( new DirTreeNode( jEdit.getProperty( "ips.SVN_Browser", "SVN Browser" ), false ) ) );
        tree.setCellRenderer( new CellRenderer() );
        tree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
        ToolTipManager.sharedInstance().registerComponent( tree );

        // on expansion, call the repository and fetch the children
        tree.addTreeExpansionListener( new TreeExpansionListener() {
                    public void treeCollapsed( TreeExpansionEvent event ) {}

                    public void treeExpanded( TreeExpansionEvent event ) {
                        TreePath path = event.getPath();
                        DirTreeNode node = ( DirTreeNode ) path.getLastPathComponent();
                        if ( node.getChildCount() == 0 ) {
                            RepositoryData data = chooser.getSelectedRepository();
                            data = new RepositoryData( data );
                            String url;
                            if ( node.isExternal() ) {
                                url = node.getRepositoryLocation();
                            }
                            else {
                                Object[] parts = path.getPath();
                                StringBuilder sb = new StringBuilder();
                                String part = parts[ 0 ].toString();
                                while ( part.endsWith( "/" ) ) {
                                    part = part.substring( 0, part.length() - 1 );
                                }
                                sb.append( part );
                                for ( int i = 1; i < parts.length; i++ ) {
                                    sb.append( "/" ).append( parts[ i ].toString() );
                                }
                                url = sb.toString();
                            }
                            data.setURL( url );
                            BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, node, data );
                            action.actionPerformed( null );
                        }
                    }
                }
                                     );

        // on double click on a text file, fetch the file contents and show the file in jEdit
        if ( full ) {
            tree.addMouseListener( new MouseAdapter() {
                        public void mouseClicked( MouseEvent me ) {
                            if ( me.getClickCount() == 2 ) {
                                TreePath path = tree.getClosestPathForLocation( me.getX(), me.getY() );
                                openFile( path, null );
                            }

                        }
                    }
                                 );

            // add listener to trigger context menu popup
            tree.addMouseListener( new TreeMouseListener() );

            // create the context menu
            //popupMenu = createPopupMenu();

            // create the control buttons -- add repository
            Icon new_icon = GUIUtilities.loadIcon( "New.png" );
            new_btn = new JButton( new_icon );
            new_btn.setName( "new repository" );
            Dimension dim = new Dimension( new_icon.getIconWidth() + ( new_btn.getInsets().top * 2 ), new_icon.getIconHeight() + ( new_btn.getInsets().top * 2 ) );
            new_btn.setSize( dim );
            new_btn.setPreferredSize( dim );
            new_btn.setMaximumSize( dim );
            new_btn.setToolTipText( jEdit.getProperty( "ips.Add_new_repository", "Add new repository" ) );
            new_btn.addActionListener( new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            AddRepositoryDialog dialog = new AddRepositoryDialog( getView() );
                            GUIUtils.center( getView(), dialog );
                            dialog.setVisible( true );
                            RepositoryData data = dialog.getValues();
                            if ( data == null ) {
                                return ;     // user canceled
                            }
                            //String name = data.getName() == null || data.getName().equals( "" ) ? data.getURL() : data.getName();
                            chooser.addRepository( data );
                            DirTreeNode root = new DirTreeNode( data.getURL(), false );
                            tree.setModel( new DefaultTreeModel( root ) );
                            BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, root, data );
                            action.actionPerformed( ae );
                        }
                    }
                                     );

            // edit repository properties
            Icon edit_icon = GUIUtilities.loadIcon( "Preferences.png" );
            edit_btn = new JButton( edit_icon );
            edit_btn.setName( "edit repository" );
            dim = new Dimension( edit_icon.getIconWidth() + ( edit_btn.getInsets().top * 2 ), edit_icon.getIconHeight() + ( edit_btn.getInsets().top * 2 ) );
            edit_btn.setSize( dim );
            edit_btn.setPreferredSize( dim );
            edit_btn.setMaximumSize( dim );
            edit_btn.setToolTipText( jEdit.getProperty( "ips.Edit_repository_properties", "Edit repository properties" ) );
            edit_btn.setEnabled( false );
            edit_btn.addActionListener( new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            RepositoryData old_data = chooser.getSelectedRepository();
                            AddRepositoryDialog dialog = new AddRepositoryDialog( getView(), new RepositoryData( old_data ) );
                            GUIUtils.center( getView(), dialog );
                            dialog.setVisible( true );
                            RepositoryData new_data = dialog.getValues();  // null indicates user canceled
                            if ( new_data != null ) {
                                chooser.removeRepository( old_data );
                                chooser.save( new_data );
                                DirTreeNode root = new DirTreeNode( new_data.getURL(), false );
                                tree.setModel( new DefaultTreeModel( root ) );
                                BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, root, new_data );
                                action.actionPerformed( ae );
                            }
                        }
                    }
                                      );

            // remove repository from chooser
            Icon remove_icon = GUIUtilities.loadIcon( "Minus.png" );
            remove_btn = new JButton( remove_icon );
            remove_btn.setName( "remove repository" );
            dim = new Dimension( remove_icon.getIconWidth() + ( remove_btn.getInsets().top * 2 ), remove_icon.getIconHeight() + ( remove_btn.getInsets().top * 2 ) );
            remove_btn.setSize( dim );
            remove_btn.setPreferredSize( dim );
            remove_btn.setMaximumSize( dim );
            remove_btn.setToolTipText( jEdit.getProperty( "ips.Remove_repository_from_browser", "Remove repository from browser" ) );
            remove_btn.setEnabled( false );
            remove_btn.addActionListener( new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            RepositoryData data = chooser.getSelectedRepository();
                            if ( data != null ) {
                                int delete = JOptionPane.showConfirmDialog( BrowseRepositoryPanel.this.view, jEdit.getProperty( "ips.Remove_repository_location", "Remove repository location" ) + " " + data.getURL() + " ?\n" + jEdit.getProperty( "ips.This_only_removes_this_repository_from_the_browser,_it_does_not_delete_any_files.", "This only removes this repository from the browser, it does not delete any files." ), jEdit.getProperty( "ips.Confirm_Remove", "Confirm Remove" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                                if ( delete == JOptionPane.YES_OPTION ) {
                                    chooser.removeRepository( data );
                                }
                            }
                        }
                    }
                                        );

            // reload tree with current selection
            Icon refresh_icon = GUIUtilities.loadIcon( "Reload.png" );
            refresh_btn = new JButton( refresh_icon );
            refresh_btn.setName( "refresh" );
            dim = new Dimension( refresh_icon.getIconWidth() + ( refresh_btn.getInsets().top * 2 ), refresh_icon.getIconHeight() + ( refresh_btn.getInsets().top * 2 ) );
            refresh_btn.setSize( dim );
            refresh_btn.setPreferredSize( dim );
            refresh_btn.setMaximumSize( dim );
            refresh_btn.setToolTipText( jEdit.getProperty( "ips.Refresh", "Refresh" ) );
            refresh_btn.setEnabled( false );

            // create a panel to hold the buttons
            button_panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 1 ) );
            button_panel.add( new_btn );
            button_panel.add( edit_btn );
            button_panel.add( remove_btn );
            button_panel.add( refresh_btn );
        }

        // action listener for repository chooser
        ActionListener al = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        RepositoryData data = chooser.getSelectedRepository();
                        if ( data != null ) {
                            username = data.getUsername();
                            password = data.getPassword();
                            repositoryUrl = data.getURL();
                            DirTreeNode root = new DirTreeNode( data.getURL(), false );
                            tree.setModel( new DefaultTreeModel( root ) );
                            BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, root, data );
                            action.actionPerformed( ae );
                        }
                        else {
                            tree.setModel( new DefaultTreeModel( new DirTreeNode( jEdit.getProperty( "ips.SVN_Browser", "SVN Browser" ), false ) ) );
                        }
                        if ( edit_btn != null ) {
                            edit_btn.setEnabled( data != null );
                        }
                        if ( remove_btn != null ) {
                            remove_btn.setEnabled( data != null );
                        }
                        if ( refresh_btn != null ) {
                            refresh_btn.setEnabled( data != null );
                        }
                    }
                };
        chooser.addActionListener( al );

        if ( full ) {
            ActionListener refresh_al = new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            // if there is a selected node, refresh just that node,
                            // otherwise, refresh the entire tree
                            RepositoryData data = chooser.getSelectedRepository();
                            TreePath path = tree.getSelectionPath();
                            DirTreeNode node = null;
                            if ( path != null ) {
                                node = ( DirTreeNode ) path.getLastPathComponent();
                                node.removeAllChildren();
                            }
                            else {
                                // refresh the entire tree
                                node = new DirTreeNode( data.getURL(), false );
                                tree.setModel( new DefaultTreeModel( node ) );
                                BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, node, data );
                                action.actionPerformed( null );
                                return ;
                            }
                            data = new RepositoryData( data );
                            String url;
                            if ( node.isExternal() ) {
                                url = node.getRepositoryLocation();
                            }
                            else {
                                Object[] parts = path.getPath();
                                String part = parts[ 0 ].toString();
                                while ( part.endsWith( "/" ) ) {
                                    part = part.substring( 0, part.length() - 1 );
                                }
                                StringBuilder sb = new StringBuilder();
                                sb.append( part );
                                for ( int i = 1; i < parts.length; i++ ) {
                                    sb.append( "/" ).append( parts[ i ].toString() );
                                }
                                url = sb.toString();
                            }
                            data.setURL( url );
                            BrowseRepositoryAction action = new BrowseRepositoryAction( getView(), tree, node, data );
                            action.actionPerformed( null );
                        }
                    };
            refresh_btn.addActionListener( refresh_al );
        }
        if ( repositoryName != null ) {
            chooser.setSelectedItem( repositoryName );
        }

        // create a panel to hold the buttons and the repository chooser
        JPanel top_panel = new JPanel( new BorderLayout() );
        if ( full ) {
            top_panel.add( button_panel, BorderLayout.NORTH );
        }
        top_panel.add( chooser, BorderLayout.CENTER );

        // fill in the main panel
        add( top_panel, BorderLayout.NORTH );


        add( new JScrollPane( tree ), BorderLayout.CENTER );
    }

    public String getUrl( TreePath path ) {
        DirTreeNode node = ( DirTreeNode ) path.getLastPathComponent();
        RepositoryData data = chooser.getSelectedRepository();
        String url;
        String filepath;
        if ( node.isExternal() ) {
            String rep = node.getRepositoryLocation();
            url = rep.substring( 0, rep.lastIndexOf( '/' ) );
            filepath = rep.substring( rep.lastIndexOf( '/' ) + 1 );
            return url + filepath;
        }
        else {
            url = data.getURL();
            url = url.endsWith( "/" ) ? url : url + "/";
            Object[] parts = path.getPath();
            StringBuilder sb = new StringBuilder();
            for ( int i = 1; i < parts.length; i++ ) {
                sb.append( "/" ).append( parts[ i ].toString() );
            }
            filepath = sb.toString();
            while ( filepath.startsWith( "/" ) ) {
                filepath = filepath.substring( 1 );
            }
            return url + filepath;
        }
    }

    public void openFile( TreePath path, SVNRevision rev ) {
        // for double-click on a text file, open the file in jEdit
        DirTreeNode node = ( DirTreeNode ) path.getLastPathComponent();
        if ( node.isLeaf() ) {
            // show the wait cursor
            tree.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
            tree.setEditable( false );

            // leaf nodes should be files, not directories.
            // get url and path for the selected file
            RepositoryData data = chooser.getSelectedRepository();
            String url;
            String filepath;
            Object[] parts = path.getPath();
            StringBuilder sb = new StringBuilder();
            for ( int i = 1; i < parts.length; i++ ) {
                sb.append( "/" ).append( parts[ i ].toString() );
            }
            filepath = sb.toString().substring( 1 );
            if ( node.isExternal() ) {
                String rep = node.getRepositoryLocation();
                url = rep.substring( 0, rep.lastIndexOf( '/' ) );
                filepath = rep.substring( rep.lastIndexOf( '/' ) + 1 );
            }
            else {
                url = data.getURL();
            }

            // maybe ask the user for the revision they want
            SVNRevision revision = rev;
            if ( revision == null ) {
                RevisionDialog rd = new RevisionDialog( BrowseRepositoryPanel.this.view, jEdit.getProperty( "ips.Select_Revision_to_View", "Select Revision to View" ) );
                GUIUtils.center( BrowseRepositoryPanel.this.getView(), rd );
                rd.setVisible( true );
                revision = rd.getData();
                if ( revision == null ) {
                    return ;
                }
            }

            // fetch the file contents
            BrowseRepository br = new BrowseRepository();
            File outfile = br.getFile( url, filepath, revision, data.getUsername(), data.getPassword() );
            if ( outfile != null ) {
                jEdit.openFile( getView(), outfile.getAbsolutePath() );
            }
            tree.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
            tree.setEditable( true );
        }
    }

    public void setRoot( DirTreeNode root ) {
        if ( root != null ) {
            tree.setModel( new DefaultTreeModel( root, false ) );
        }
    }

    public View getView() {
        return view;
    }

    public String getSelectionPath() {
        TreePath tp = tree.getSelectionPath();
        StringBuilder sb = new StringBuilder();
        for ( Object part : tp.getPath() ) {
            String p = part.toString();
            if ( p.endsWith( "/" ) ) {
                p = p.substring( 0, p.length() - 1 );
            }
            sb.append( p ).append( "/" );
        }
        return sb.toString();
    }

    /**
     * MouseListener to popup context menu on the tree.
     */
    class TreeMouseListener extends MouseAdapter {
        public void mouseReleased( MouseEvent me ) {
            handleClick( me );
        }

        public void mousePressed( MouseEvent me ) {
            handleClick( me );
        }

        private void handleClick( MouseEvent me ) {
            if ( me.isPopupTrigger() ) {
                if ( tree.getSelectionCount() == 0 ) {
                    TreePath path = tree.getClosestPathForLocation( me.getX(), me.getY() );
                    tree.addSelectionPath( path );
                }
                GUIUtils.showPopupMenu( createPopupMenu(), tree, me.getX(), me.getY() );
            }
        }
    }

    private ActionListener createPopupMenuActionListener( final BRAction internal ) {
        return new ActionListener() {
                   public void actionPerformed( ActionEvent ae ) {
                       internal.parent = BrowseRepositoryPanel.this;
                       internal.init( view, repositoryUrl, tree, username, password );
                       internal.actionPerformed( ae );
                   }
               };
    }

    /**
     * Create the context menu.
     */
    private JPopupMenu createPopupMenu() {
        // update, commit, revert, add, log, need to add others as appropriate
        final JPopupMenu pm = new JPopupMenu();
        if ( chooser != null ) {
            RepositoryData rd = chooser.getSelectedRepository();
            if ( rd != null ) {
                repositoryUrl = rd.getURL();
            }
        }

        String pbase = "ise.plugin.svn.gui.br.";
        for ( int i = 1; i < 25; i++ ) {
            String label = jEdit.getProperty( pbase + "label." + i );
            if ( label == null ) {
                continue;
            }
            if ( label.equals( "-" ) ) {
                pm.addSeparator();
                continue;
            }
            String classname = jEdit.getProperty( pbase + "code." + i );
            if ( classname == null ) {
                continue;
            }
            JMenuItem item = null;
            try {
                BRAction action = ( BRAction ) PrivilegedAccessor.getNewInstance( classname, null );
                item = new JMenuItem( label );
                item.addActionListener( createPopupMenuActionListener( action ) );
                pm.add( item );
            }
            catch ( Exception e ) {
                // class not found or instantiation exception, don't worry
                // about it, assume it's a typo
                //e.printStackTrace();
                continue;
            }
        }

        // add copy to clipboard command
        pm.addSeparator();
        JMenuItem item = new JMenuItem( jEdit.getProperty( "ips.Copy_URL_to_clipboard", "Copy URL to clipboard" ) );
        item.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    TreePath path = tree.getSelectionPath();
                    if ( path == null ) {
                        JOptionPane.showMessageDialog( BrowseRepositoryPanel.this.view, jEdit.getProperty( "ips.Nothing_selected", "Nothing selected" ), jEdit.getProperty( "ips.Nothing_selected,_please_select_an_item_from_the_tree.", "Nothing selected, please select an item from the tree." ), JOptionPane.ERROR_MESSAGE );
                        return ;
                    }
                    String url = getUrl( path );
                    Registers.getRegister( '$' ).setTransferable( new StringSelection( url ) );
                }
            }
        );
        pm.add( item );

        return pm;
    }

    class CellRenderer extends DefaultTreeCellRenderer {
        public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus ) {

            Component r = super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus );

            if ( r instanceof JLabel ) {
                JLabel label = ( JLabel ) r;
                DirTreeNode node = ( DirTreeNode ) value;
                StringBuilder text;
                if ( node.isExternal() ) {
                    // Use view.style.digit color since this should look good
                    // with the current look and feel.
                    text = new StringBuilder();
                    text.append( "<html><font color=\"" ).append( jEdit.getProperty( "view.style.digit" ) ).append( "\">" ).append( node.toString() );
                    label.setToolTipText( "<html><b>External: </b> " + node.getRepositoryLocation() );
                }
                else {
                    text = new StringBuilder( node.toString() );
                }
                if ( node.hasProperties() ) {
                    text.append( " *" );
                }
                label.setText( text.toString() );
            }
            return r;
        }
    }
}