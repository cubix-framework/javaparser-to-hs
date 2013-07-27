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
import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.border.EmptyBorder;
import ise.plugin.svn.data.StatusData;
import ise.plugin.svn.action.*;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.pv.VersionControlState;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

/**
 * Shows the results of a status check.
 */
public class StatusResultsPanel extends JPanel {

    private View view = null;
    private String username = null;
    private String password = null;

    private JTree tree = null;

    private boolean hasConflicts = false;

    private static Color background = jEdit.getColorProperty("view.bgColor", Color.WHITE);
    private static Color selection = jEdit.getColorProperty("view.selectionColor", Color.LIGHT_GRAY);

    private VersionControlState versionControlState = VersionControlState.getInstance();


    public StatusResultsPanel( StatusData results, View view, String username, String password ) {
        super( new BorderLayout() );

        this.view = view;
        this.username = username;
        this.password = password;

        setBorder( new EmptyBorder( 3, 3, 3, 3 ) );

        String revision_msg;
        if (results.getRevision() == -1) {
            revision_msg = jEdit.getProperty("ips.Status_checked_locally>", "Status checked locally:");
        }
        else {
            revision_msg = jEdit.getProperty("ips.Status_checked_against_revision>", "Status checked against revision:") + " " + results.getRevision();
        }
        JLabel label = new JLabel( revision_msg );
        label.setBorder( new EmptyBorder( 6, 3, 6, 3 ) );
        add( label, BorderLayout.NORTH );

        boolean added = false;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        List<SVNStatus> list = results.getConflicted();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.conflicted", "Files with conflicts (need fixed?):" ), list, VersionControlState.CONFLICT ) );
            added = true;
            hasConflicts = true;
        }

        list = results.getOutOfDate();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.outofdate", "Out of date files (need updated?):" ), list, VersionControlState.NEED_UPDATE ) );
            added = true;
        }

        list = results.getModified();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.modified", "Modified files (need committed?):" ), list, VersionControlState.LOCAL_MOD ) );
            added = true;
        }

        list = results.getAdded();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.added", "Added files (need committed?):" ), list, VersionControlState.LOCAL_ADD ) );
            added = true;
        }

        list = results.getUnversioned();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.unversioned", "Unversioned files (need added?):" ), list, VersionControlState.UNVERSIONED ) );
            added = true;
        }

        list = results.getDeleted();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.deleted", "Deleted files (need committed?):" ), list, VersionControlState.DELETED ) );
            added = true;
        }

        list = results.getMissing();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.missing", "Missing files (need deleted?):" ), list, VersionControlState.DELETED ) );
            added = true;
        }

        list = results.getLocked();
        if (list != null) {
            root.add( createNode(jEdit.getProperty("ips.Locked_files>", "Locked files:"), list, VersionControlState.LOCKED));
            added = true;
        }

        if ( added ) {
            tree = new JTree( root );
            tree.setRootVisible( false );
            tree.setCellRenderer(new CellRenderer());
            for ( int i = 0; i < tree.getRowCount(); i++ ) {
                tree.expandRow( i );
            }
            tree.addMouseListener( new TreeMouseListener() );
            add( tree, BorderLayout.CENTER );
        }
        else {
            label.setText( label.getText() + " " + jEdit.getProperty("ips.(All_files_up_to_date.)", "(All files up to date.)") );
        }
    }

    private DefaultMutableTreeNode createNode( String title, List<SVNStatus> values, int iconIndex ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( title );
        for ( SVNStatus status : values ) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode( new Status(status, iconIndex) );
            node.add( child );
        }
        return node;
    }

    class Status {
        private SVNStatus status;
        int iconIndex = 0;

        public Status(SVNStatus s, int iconIndex) {
            status = s;
            this.iconIndex = iconIndex;
        }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(status.getFile().toString());
            boolean contents_changed = SVNStatusType.STATUS_MODIFIED.equals(status.getContentsStatus());
            boolean properties_changed = SVNStatusType.STATUS_MODIFIED.equals(status.getPropertiesStatus());
            if (contents_changed) {
                sb.append(" (").append(jEdit.getProperty("ips.Contents", "Contents")).append(" ");
            }
            if (properties_changed) {
                sb.append(contents_changed ? jEdit.getProperty("ips._and_Properties", " and Properties") : " (" + jEdit.getProperty("ips.Properties", "Properties"));
            }
            if (contents_changed || properties_changed) {
                sb.append(" ").append(jEdit.getProperty("ips.modified", "modified")).append(")");
            }
            return sb.toString();
        }

        public String getFilename() {
            return status.getFile().getAbsolutePath();
        }

        public SVNStatus getStatus() {
            return status;
        }

        public Icon getIcon() {
            return versionControlState.getIcon(iconIndex);
        }
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

        private void handleClick( MouseEvent e ) {
            TreePath path = tree.getClosestPathForLocation( e.getX(), e.getY() );
            Status status = null;
            if ( path.getPathCount() > 2 ) {
                status = ( Status ) ( ( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject();
            }
            if ( e.isPopupTrigger() && tree.getSelectionCount() > 0 ) {
                GUIUtils.showPopupMenu( createPopupMenu(status), tree, e.getX(), e.getY() );
            }
            else if ( e.getClickCount() == 2 && status != null) {
                // for double-click on a text file, open the file in jEdit
                jEdit.openFile( view, status.getFilename() );
            }
        }
    }

    /**
     * Create the context menu.
     */
    private JPopupMenu createPopupMenu(final Status status) {
        // update, commit, revert, add, log, need to add others as appropriate
        final JPopupMenu pm = new JPopupMenu();

        JMenuItem mi;

        if (hasConflicts && status != null && status.getStatus().getConflictWrkFile() != null && status.getStatus().getConflictWrkFile().exists()) {
            mi = new JMenuItem( jEdit.getProperty("ips.Resolve_Conflicts", "Resolve Conflicts") );
            pm.add( mi );
            mi.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        ResolveConflictDialog dialog = new ResolveConflictDialog(view, status.getStatus());
                        dialog.setVisible(true);
                    }
                }
            );
        }

        mi = new JMenuItem( jEdit.getProperty("ips.Update", "Update") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        UpdateAction action = new UpdateAction( view, paths, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Commit", "Commit") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        TreeMap<String, String> paths = new TreeMap<String, String>();
                        StringBuilder message = new StringBuilder();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                DefaultMutableTreeNode type = ( DefaultMutableTreeNode ) path.getPathComponent( 1 );
                                String pathname = ((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename();
                                if ( type != null ) {
                                    String comp = type.getUserObject().toString();
                                    // really should get these strings replaced
                                    if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.conflicted", "Files with conflicts" ) ) ) {
                                        message.append( pathname ).append( " " ).append(jEdit.getProperty("ips.has_conflicts.", "has conflicts.")).append("\n" );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.outofdate", "Out of date" ) ) ) {
                                        message.append( pathname ).append( " " ).append(jEdit.getProperty("ips.is_out_of_date.", "is out of date.")).append("\n" );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.modified", "Modified" ) ) ) {
                                        paths.put( pathname, jEdit.getProperty("ips.Modified", "Modified") );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.added", "Added" ) ) ) {
                                        paths.put( pathname, jEdit.getProperty("ips.Added", "Added") );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.unversioned", "Unversioned" ) ) ) {
                                        message.append( pathname ).append( " " ).append(jEdit.getProperty("ips.is_not_under_version_control.", "is not under version control.")).append("\n" );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.deleted", "Deleted" ) ) ) {
                                        paths.put( pathname, jEdit.getProperty("ips.Deleted", "Deleted") );
                                    }
                                    else if ( comp.startsWith( jEdit.getProperty( "ise.plugin.svn.gui.StatusResultsPanel.missing", "Missing" ) ) ) {
                                        message.append( pathname ).append( " ").append(jEdit.getProperty("ips.is_missing.", "is missing.")).append("\n" );
                                    }
                                }
                            }
                        }
                        String error = message.toString();
                        if ( error.length() > 0 && paths.size() == 0 ) {
                            JOptionPane.showMessageDialog( view, jEdit.getProperty("ips.Cannot_commit_selected_files>", "Cannot commit selected files:") + "\n\n" + error, jEdit.getProperty("ips.Cannot_commit_selected_files", "Cannot commit selected files"), JOptionPane.ERROR_MESSAGE );
                        }
                        else if ( error.length() > 0 && paths.size() > 0 ) {
                            int answer = JOptionPane.showConfirmDialog( view, jEdit.getProperty("ips.Cannot_commit_all_selected_files>", "Cannot commit all selected files:") + "\n\n" + error + "\n" + jEdit.getProperty("ips.Commit_remaining_file_anyway?", "Commit remaining file anyway?"), jEdit.getProperty("ips.Cannot_commit_all_selected_files", "Cannot commit all selected files"), JOptionPane.WARNING_MESSAGE );
                            if ( answer == JOptionPane.YES_OPTION ) {
                                CommitAction action = new CommitAction( view, paths, username, password );
                                action.actionPerformed( ae );
                            }
                        }
                        else {
                            CommitAction action = new CommitAction( view, paths, username, password );
                            action.actionPerformed( ae );

                        }
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Revert", "Revert") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        RevertAction action = new RevertAction( view, paths, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Diff", "Diff") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length != 1 ) {
                            JOptionPane.showMessageDialog( view, jEdit.getProperty("ips.Please_select_a_single_entry.", "Please select a single entry."), jEdit.getProperty("ips.Too_many_selections", "Too many selections"), JOptionPane.ERROR_MESSAGE );
                            return ;
                        }
                        TreePath tree_path = tree_paths[0];
                        String path = ((Status)(( DefaultMutableTreeNode ) tree_path.getLastPathComponent() ).getUserObject()).getFilename();
                        DiffAction action = new DiffAction( view, path, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Add", "Add") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        AddAction action = new AddAction( view, paths, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Log", "Log") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        LogAction action = new LogAction( view, paths, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Delete", "Delete") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        DeleteAction action = new DeleteAction( view, paths, username, password );
                        action.actionPerformed( ae );
                    }
                }
                            );

        mi = new JMenuItem( jEdit.getProperty("ips.Unlock", "Unlock") );
        pm.add( mi );
        mi.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        TreePath[] tree_paths = tree.getSelectionPaths();
                        if ( tree_paths.length == 0 ) {
                            return ;
                        }
                        List<String> paths = new ArrayList<String>();
                        for ( TreePath path : tree_paths ) {
                            if ( path != null && path.getPathCount() > 2 ) {
                                paths.add(((Status)(( DefaultMutableTreeNode ) path.getLastPathComponent() ).getUserObject()).getFilename() );
                            }
                        }
                        UnlockAction action = new UnlockAction( view, paths, username, password, false );
                        action.actionPerformed( ae );
                    }
                }
                            );

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

            JLabel r = (JLabel)super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus );
            Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
            if (userObject instanceof Status) {
                r.setIcon(((Status)userObject).getIcon());
            }

            r.setBackground( sel ? StatusResultsPanel.selection : StatusResultsPanel.background );
            return r;
        }
    }

}
