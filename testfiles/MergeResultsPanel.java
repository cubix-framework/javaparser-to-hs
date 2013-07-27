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
import ise.plugin.svn.data.MergeResults;
import ise.plugin.svn.action.*;
import org.gjt.sp.jedit.jEdit;

/**
 * Shows the results of a dry-run merge.  Non-dry-run merge results are shown
 * in a StatusResultsPanel.  There are no actions associated with this display.
 */
public class MergeResultsPanel extends JPanel {

    private JTree tree = null;

    private static Color background = jEdit.getColorProperty("view.bgColor", Color.WHITE);
    private static Color selection = jEdit.getColorProperty("view.selectionColor", Color.LIGHT_GRAY);


    public MergeResultsPanel( MergeResults results ) {
        super( new BorderLayout() );

        setBorder( new EmptyBorder( 3, 3, 3, 3 ) );

        JTextArea label = new JTextArea();
        label.setText(jEdit.getProperty("ips.Merge_Dry_Run_Results>", "Merge Dry Run Results:") + "\n" + results.getCommandLineEquivalent() );
        label.setEditable(false);
        label.setBorder( new EmptyBorder( 6, 3, 6, 3 ) );

        boolean added = false;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode();

        List<String> list = results.getConflicted();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Files_with_conflicts>", "Files with conflicts:"), list ) );
            added = true;
        }

        list = results.getAdded();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Added_files>", "Added files:"), list ) );
            added = true;
        }

        list = results.getDeleted();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Deleted_files>", "Deleted files:"), list ) );
            added = true;
        }

        list = results.getMerged();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Merged_files>", "Merged files:"), list ) );
            added = true;
        }

        list = results.getSkipped();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Skipped_files>", "Skipped files:"), list ) );
            added = true;
        }

        list = results.getUpdated();
        if ( list != null ) {
            root.add( createNode( jEdit.getProperty("ips.Updated_files>", "Updated files:"), list ) );
            added = true;
        }

        if ( added ) {
            tree = new JTree( root );
            tree.setRootVisible( false );
            tree.setCellRenderer(new CellRenderer());
            for ( int i = 0; i < tree.getRowCount(); i++ ) {
                tree.expandRow( i );
            }
            add( tree, BorderLayout.CENTER );
        }
        else {
            label.setText( label.getText() + "\n\n" + jEdit.getProperty("ips.(All_files_up_to_date.)", "(All files up to date.)") );
        }
        add( label, BorderLayout.NORTH );
    }

    private DefaultMutableTreeNode createNode( String title, List<String> values ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( title );
        for ( String filename : values ) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode( filename );
            node.add( child );
        }
        return node;
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

            r.setBackground( sel ? MergeResultsPanel.selection : MergeResultsPanel.background );
            return r;
        }
    }
}
