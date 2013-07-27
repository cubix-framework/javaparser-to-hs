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

import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ise.plugin.svn.action.*;
import ise.plugin.svn.library.FileUtilities;
import ise.plugin.svn.library.GUIUtils;
import ise.java.awt.KappaLayout;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.gjt.sp.jedit.View;
import jdiff.DualDiffManager;
import jdiff.DiffMessage;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.msg.EditPaneUpdate;

public class ResolveConflictDialog extends JDialog implements EBComponent {
    private ButtonGroup bg = null;
    private JRadioButton merge_rb = new JRadioButton( jEdit.getProperty( "ips.Do_manual_merge_with_JDiff", "Do manual merge with JDiff" ) );
    private JRadioButton keep_mine_rb = new JRadioButton( jEdit.getProperty( "ips.Keep_mine", "Keep mine" ) );
    private JRadioButton keep_theirs_rb = new JRadioButton( jEdit.getProperty( "ips.Use_theirs", "Use theirs" ) );

    private View view = null;
    private SVNStatus status = null;

    private JPanel mine_panel;
    private JPanel theirs_panel;

    /**
     * Presents a dialog to the user so they may resolve the conflicts in the
     * given file.  Based on the filename, this constructor attempts to find
     * files with the same name plus a ".mine" and a ".rXXX" extenstion and
     * uses those files to initiate JDiff to perform a manual merge or a file
     * copy to keep one or the other.
     * @param view the parent frame for this dialog
     * @param filename the name of the file to resolve conflicts for
     */
    public ResolveConflictDialog( View view, String filename ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Resolve_Conflict_for", "Resolve Conflict for" ) + " " + filename, true );
        File file = new File( filename );
        File workFile = new File( filename + ".mine" );
        File newFile = findNewFile( file );
        MySVNStatus status = new MySVNStatus( file, newFile, workFile );
        init( view, status );
    }

    /**
     * Presents a dialog to the user so they may resolve the conflicts in the
     * given file.  Based on the filename, this constructor attempts to find
     * files with the same name plus a ".mine" and a ".rXXX" extenstion and
     * uses those files to initiate JDiff to perform a manual merge or a file
     * copy to keep one or the other.
     * @param view the parent frame for this dialog
     * @param status an SVNStatus containing the info necessary to allow the
     * user to resolve conflicts.
     */
    public ResolveConflictDialog( View view, SVNStatus status ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Resolve_Conflict_for", "Resolve Conflict for" ) + " " + status.getFile().getName(), true );
        init( view, status );
    }

    /**
     * Finds a file named like the given file, but with ".rNNN" appended to the name,
     * where NNN represents a revision number.
     * Since it is possible that there are several such files, this method returns
     * the file with the largest number.
     */
    private File findNewFile( File file ) {
        File dir = file.getParentFile();
        final String toMatch = file.getName() + ".r";
        File[] files = dir.listFiles( new FileFilter() {
                    public boolean accept( File f ) {
                        return f.getName().startsWith( toMatch );
                    }
                }
                                    );
        if ( files.length == 0 ) {
            return null;
        }
        Arrays.sort( files, new Comparator<File>() {
                    public int compare( File a, File b ) {
                        Integer first = new Integer( a.getName().substring( toMatch.length() ) );
                        Integer second = new Integer( b.getName().substring( toMatch.length() ) );
                        return first.compareTo( second );
                    }
                }
                   );
        return files[ files.length - 1 ];
    }

    /**
     * Build the GUI for the dialog.
     */
    private void init( View view, SVNStatus status ) {
        if ( status == null ) {
            throw new IllegalArgumentException( "status cannnot be null" );
        }
        this.view = view;
        this.status = status;

        EditBus.addToBus( this );

        JPanel contents = new JPanel( new KappaLayout() );
        contents.setBorder( new EmptyBorder( 6, 6, 6, 6 ) );

        // conflict choices
        bg = new ButtonGroup();
        bg.add( merge_rb );
        bg.add( keep_mine_rb );
        bg.add( keep_theirs_rb );
        merge_rb.setSelected( true );

        // ok and cancel buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( "Ok" );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        ok_btn.addActionListener( getOkActionListener() );
        JButton cancel_btn = new JButton( "Cancel" );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        cancel_btn.addActionListener( getCancelActionListener() );
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        // layout the parts
        contents.add( "0, 0, , , W, , 3", new JLabel( jEdit.getProperty( "ips.Select_resolution_method>", "Select resolution method:" ) ) );
        contents.add( "0, 1, , , W, , 3", KappaLayout.createVerticalStrut( 6, true ) );
        contents.add( "0, 2, , , W, , 3", merge_rb );
        contents.add( "0, 3, , , W, , 3", keep_mine_rb );
        contents.add( "0, 4, , , W, , 3", keep_theirs_rb );
        contents.add( "0, 5, , , W, , 0", KappaLayout.createVerticalStrut( 10, true ) );
        contents.add( "0, 6, , , E, , 0", btn_panel );
        setContentPane( contents );
        pack();
        GUIUtils.center( view, this );
        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    /**
     * Action for the Ok button.
     */
    private ActionListener getOkActionListener() {
        return new ActionListener() {
                   public void actionPerformed( ActionEvent ae ) {
                       ResolveConflictDialog.this.setVisible( false );
                       ResolveConflictDialog.this.dispose();
                       Runnable runner = new Runnable() {
                                   public void run() {
                                       if ( keep_mine_rb.isSelected() ) {
                                           doKeepMine();
                                       }
                                       else if ( keep_theirs_rb.isSelected() ) {
                                           doKeepTheirs();
                                       }
                                       else {
                                           doManualMerge();
                                       }
                                   }
                               };
                       SwingUtilities.invokeLater( runner );
                   }
               };
    }

    /**
     * Action for the Cancel button.
     */
    private ActionListener getCancelActionListener() {
        return new ActionListener() {
                   public void actionPerformed( ActionEvent ae ) {
                       ResolveConflictDialog.this.setVisible( false );
                       ResolveConflictDialog.this.dispose();
                   }
               };
    }

    /**
     * Keep mine means copy filename.mine to filename and mark the file resolved.
     */
    private void doKeepMine() {
        try {
            FileUtilities.copy( status.getConflictWrkFile(), status.getFile() );
            resolve();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Keep theirs means copy filename.rXXX to filename and mark the file resolved.
     */
    private void doKeepTheirs() {
        try {
            FileUtilities.copy( status.getConflictNewFile(), status.getFile() );
            resolve();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Mark filename as resolved.
     */
    private void resolve() {
        List<String> paths = new ArrayList<String>();
        paths.add( status.getFile().getAbsolutePath() );
        ResolvedAction action = new ResolvedAction( view, paths, null, null, true );
        action.actionPerformed( null );
    }

    /**
     * Do a manual merge via JDiff.
     * TODO: there are threading issues here.  The 'Keep this file' buttons don't work as expected,
     * the resolve command doesn't seem to get run, and the view doesn't necessarily unsplit.
     */
    private void doManualMerge() {
        try {
            final File mine = status.getConflictWrkFile();
            final File theirs = status.getConflictNewFile();

            if ( mine == null && theirs == null ) {
                JOptionPane.showMessageDialog( view, jEdit.getProperty( "ips.Unable_to_fetch_contents_for_comparison.", "Unable to fetch contents for comparison." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                return ;
            }

            // show JDiff
            view.unsplit();
            DualDiffManager.toggleFor( view );

            Runnable runner = new Runnable() {
                        public void run() {
                            // set the edit panes in the view
                            final EditPane[] editPanes = view.getEditPanes();
                            mine_panel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
                            theirs_panel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

                            SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            // show theirs in the left edit pane
                                            final EditPane[] editPanes = view.getEditPanes();
                                            editPanes[ 0 ].setBuffer( jEdit.openFile( view, theirs.getAbsolutePath() ) );
                                            JButton theirs_btn = new JButton( jEdit.getProperty( "ips.Keep_this_file", "Keep this file" ) );
                                            theirs_btn.setToolTipText( jEdit.getProperty( "ips.When_done_merging,_click_this_button_to_keep_this_file_as_the_merged_file.", "When done merging, click this button to keep this file as the merged file." ) );
                                            theirs_btn.addActionListener(
                                                new ActionListener() {
                                                    public void actionPerformed( ActionEvent ae ) {
                                                        try {
                                                            // copy the contents of the selected buffer to the file with the conflicts
                                                            String buffer_text = editPanes[ 1 ].getTextArea().getText();
                                                            StringReader reader = new StringReader( buffer_text );
                                                            FileWriter writer = new FileWriter( status.getFile(), false );
                                                            FileUtilities.copy( reader, writer );

                                                            // close the working files
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( mine.getAbsolutePath() ) );
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( theirs.getAbsolutePath() ) );
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( status.getFile().getAbsolutePath() ) );

                                                            // remove buttons from text area
                                                            editPanes[ 0 ].getTextArea().removeTopComponent( mine_panel );
                                                            editPanes[ 1 ].getTextArea().removeTopComponent( theirs_panel );

                                                            // close JDiff and unsplit the view
                                                            DualDiffManager.toggleFor( view );
                                                            Runnable r2d2 = new Runnable() {
                                                                        public void run() {
                                                                            view.unsplit();

                                                                            // open the cleaned up file
                                                                            jEdit.openFile( view, status.getFile().getAbsolutePath() );

                                                                            // mark file as resolved
                                                                            if ( hasConflictMarkers( editPanes[ 0 ].getTextArea().getText() ) ) {
                                                                                int rtn = JOptionPane.showConfirmDialog( view, jEdit.getProperty( "ips.This_file_appears_to_contain_SVN_conflict_markers.", "This file appears to contain SVN conflict markers." ) + "\n" + jEdit.getProperty( "ips.Are_you_sure_you_want_to_use_this_file_as_is?", "Are you sure you want to use this file as is?" ), jEdit.getProperty( "ips.Possible_Conflict_Markers", "Possible Conflict Markers" ), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                                                                                if ( rtn == JOptionPane.NO_OPTION ) {
                                                                                    return ;
                                                                                }
                                                                            }
                                                                            ResolveConflictDialog.this.resolve();
                                                                        }
                                                                    };
                                                            SwingUtilities.invokeLater( r2d2 );
                                                        }
                                                        catch ( Exception e ) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            );
                                            theirs_panel.add( theirs_btn );

                                            // add a handler to the edit bus to remove the 'keep this file' button
                                            // when the view is unsplit
                                            EBComponent handler = new EBComponent() {
                                                        public void handleMessage( EBMessage message ) {
                                                            if ( message instanceof EditPaneUpdate ) {
                                                                EditPaneUpdate epu = ( EditPaneUpdate ) message;
                                                                EditPane editPane = epu.getEditPane();
                                                                View view = editPane.getView();
                                                                if ( epu.getWhat() == EditPaneUpdate.DESTROYED ) {
                                                                    editPanes[ 0 ].getTextArea().removeTopComponent( theirs_panel );
                                                                    view.repaint();
                                                                    EditBus.removeFromBus( this );
                                                                }
                                                            }
                                                        }
                                                    };
                                            EditBus.addToBus( handler );
                                            editPanes[ 0 ].getTextArea().addTopComponent( theirs_panel );

                                            // do an explicit repaint of the view to clean up the display
                                            view.repaint();
                                        }
                                    }
                                                      );

                            SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {

                                            // show mine in the right edit pane
                                            final EditPane[] editPanes = view.getEditPanes();
                                            editPanes[ 1 ].setBuffer( jEdit.openFile( view, mine.getAbsolutePath() ) );
                                            JButton mine_btn = new JButton( jEdit.getProperty( "ips.Keep_this_file", "Keep this file" ) );
                                            mine_btn.setToolTipText( jEdit.getProperty( "ips.When_done_merging,_click_this_button_to_keep_this_file_as_the_merged_file.", "When done merging, click this button to keep this file as the merged file." ) );
                                            mine_btn.addActionListener(
                                                new ActionListener() {
                                                    public void actionPerformed( ActionEvent ae ) {
                                                        try {
                                                            // copy the contents of the selected buffer to the file with the conflicts
                                                            String buffer_text = editPanes[ 0 ].getTextArea().getText();
                                                            StringReader reader = new StringReader( buffer_text );
                                                            FileWriter writer = new FileWriter( status.getFile(), false );
                                                            FileUtilities.copy( reader, writer );

                                                            // close the working files
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( mine.getAbsolutePath() ) );
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( theirs.getAbsolutePath() ) );
                                                            jEdit._closeBuffer( view, jEdit.getBuffer( status.getFile().getAbsolutePath() ) );

                                                            // remove buttons from text area
                                                            editPanes[ 0 ].getTextArea().removeTopComponent( mine_panel );
                                                            editPanes[ 1 ].getTextArea().removeTopComponent( theirs_panel );

                                                            // close JDiff and unsplit the view
                                                            DualDiffManager.toggleFor( view );
                                                            Runnable r2d2 = new Runnable() {
                                                                        public void run() {
                                                                            view.unsplit();

                                                                            // open the cleaned up file
                                                                            jEdit.openFile( view, status.getFile().getAbsolutePath() );

                                                                            // mark file as resolved
                                                                            if ( hasConflictMarkers( editPanes[ 1 ].getTextArea().getText() ) ) {
                                                                                int rtn = JOptionPane.showConfirmDialog( view, jEdit.getProperty( "ips.This_file_appears_to_contain_SVN_conflict_markers.", "This file appears to contain SVN conflict markers." ) + "\n" + jEdit.getProperty( "ips.Are_you_sure_you_want_to_use_this_file_as_is?", "Are you sure you want to use this file as is?" ), jEdit.getProperty( "ips.Possible_Conflict_Markers", "Possible Conflict Markers" ), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE );
                                                                                if ( rtn == JOptionPane.NO_OPTION ) {
                                                                                    return ;
                                                                                }
                                                                            }
                                                                            ResolveConflictDialog.this.resolve();
                                                                        }
                                                                    };
                                                            SwingUtilities.invokeLater( r2d2 );
                                                        }
                                                        catch ( Exception e ) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            );
                                            mine_panel.add( mine_btn );

                                            // add a handler to the edit bus to remove the 'keep this file' button
                                            // when the view is unsplit
                                            EBComponent handler = new EBComponent() {
                                                        public void handleMessage( EBMessage message ) {
                                                            if ( message instanceof EditPaneUpdate ) {
                                                                EditPaneUpdate epu = ( EditPaneUpdate ) message;
                                                                EditPane editPane = epu.getEditPane();
                                                                View view = editPane.getView();
                                                                if ( epu.getWhat() == EditPaneUpdate.DESTROYED ) {
                                                                    editPanes[ 1 ].getTextArea().removeTopComponent( mine_panel );
                                                                    view.repaint();
                                                                    EditBus.removeFromBus( this );
                                                                }
                                                            }
                                                        }
                                                    };
                                            EditBus.addToBus( handler );
                                            editPanes[ 1 ].getTextArea().addTopComponent( mine_panel );

                                            // do an explicit repaint of the view to clean up the display
                                            view.repaint();
                                        }
                                    }
                                                      );

                            // show the jdiff dockable
                            view.getDockableWindowManager().showDockableWindow( "jdiff-lines" );
                        }
                    };
            SwingUtilities.invokeLater( runner );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @return true if the given string contains svn conflict markers
     */
    private boolean hasConflictMarkers( String s ) {
        Pattern conflict_start = Pattern.compile( "^<<<<<<<" );
        Pattern conflict_middle = Pattern.compile( "^=======" );
        Pattern conflict_end = Pattern.compile( "^>>>>>>>" );
        Matcher start_matcher = conflict_start.matcher( s );
        Matcher middle_matcher = conflict_middle.matcher( s );
        Matcher end_matcher = conflict_end.matcher( s );
        return start_matcher.matches() || middle_matcher.matches() || end_matcher.matches() ;
    }

    public void handleMessage( EBMessage msg ) {
        if ( msg instanceof DiffMessage ) {
            DiffMessage dmsg = ( DiffMessage ) msg;
            if ( DiffMessage.OFF.equals( dmsg.getWhat() ) ) {
                EditPane[] editPanes = view.getEditPanes();
                for ( EditPane pane : editPanes ) {
                    pane.getTextArea().removeTopComponent( mine_panel );
                    pane.getTextArea().removeTopComponent( theirs_panel );
                }
            }
        }
    }

    class MySVNStatus extends SVNStatus {
        public MySVNStatus( File file, File conflictNewFile, File conflictWrkFile ) {
            super(
                null,                // SVNURL url,
                file,                // File file,
                null,                // SVNNodeKind kind,
                null,                // SVNRevision revision,
                null,                // SVNRevision committedRevision,
                null,                // Date committedDate,
                null,                // String author,
                null,                // SVNStatusType contentsStatus,
                null,                // SVNStatusType propertiesStatus,
                null,                // SVNStatusType remoteContentsStatus,
                null,                // SVNStatusType remotePropertiesStatus,
                false,               // boolean isLocked,
                false,               // boolean isCopied,
                false,               // boolean isSwitched,
                false,               // boolean isFileExternal,
                conflictNewFile,     // File conflictNewFile,
                null,                // File conflictOldFile,
                conflictWrkFile,     // File conflictWrkFile,
                null,                // File projRejectFile,
                null,                // String copyFromURL,
                null,                // SVNRevision copyFromRevision,
                null,                // SVNLock remoteLock,
                null,                // SVNLock localLock,
                null,                // Map entryProperties,
                null,                // String changelistName,
                -1,                  // int wcFormatVersion,
                null);               // SVNTreeConflictDescription treeConflict
        }
    }
}