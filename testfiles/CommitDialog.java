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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

import ise.java.awt.KappaLayout;
import ise.java.awt.LambdaLayout;
import ise.plugin.svn.data.CommitData;
import ise.plugin.svn.data.PropertyData;
import ise.plugin.svn.library.PropertyComboBox;
import ise.plugin.svn.PVHelper;
import ise.plugin.svn.io.ConsolePrintStream;
import ise.plugin.svn.io.NullOutputStream;
import ise.plugin.svn.command.Property;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Dialog for obtaining a comment for a commit.
 */
public class CommitDialog extends JDialog {
    // instance fields
    private View view = null;
    private Map<String, String> nodes = null;

    private String projectRoot = null;
    private Properties bugtraqProperties = new Properties();

    private JButton okButton;
    private JButton cancelButton;
    private JTextField bugField = null;
    private JTextPane comment = null;
    private PropertyComboBox commentList = null;
    private DefaultTableModel fileTableModel;
    private LoginPanel login = null;
    private JCheckBox recursiveCheckbox = null;

    private String logRegex0 = null;
    private String logRegex1 = null;
    private boolean foundBugId = false;

    private CommitData commitData = null;
    private String commitMessageTemplate = null;


    public CommitDialog( View view, Map<String, String> nodes ) {
        this( view, nodes, false );
    }

    public CommitDialog( View view, Map<String, String> nodes, boolean showLogin ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Commit", "Commit" ), true );
        if ( nodes == null ) {
            throw new IllegalArgumentException( "nodes may not be null" );
        }
        this.view = view;
        this.nodes = nodes;
        init( showLogin );
    }

    protected void init( boolean showLogin ) {
        installDefaults();
        installComponents( showLogin );
        installListeners();
    }

    private void installDefaults() {
        projectRoot = PVHelper.getProjectRoot( view );
        commitData = new CommitData();

        // create the table model to show the files to be committed.  The
        // table has 3 columns, first column is a checkbox to allow the user
        // to unselect files to be committed, second column is filename,
        // third column is file status (modified, added, etc).
        fileTableModel = new DefaultTableModel(
                    new String[] {
                        "", jEdit.getProperty( "ips.File", "File" ), jEdit.getProperty( "ips.Status", "Status" )
                    }, nodes.size() ) {
                    public Class getColumnClass( int index ) {
                        if ( index == 0 ) {
                            return Boolean.class;
                        }
                        else {
                            return super.getColumnClass( index );
                        }

                    }
                };

        // load the table model, determine if recursive, accumulate file paths
        boolean recursive = false;
        List<String> paths = new ArrayList<String>();
        int i = 0;
        Set < Map.Entry < String, String >> set = nodes.entrySet();
        for ( Map.Entry<String, String> me : set ) {
            String path = me.getKey();
            String status = me.getValue() == null ? "" : me.getValue();
            if ( path != null ) {
                File file = new File( path );
                if ( file.isDirectory() ) {
                    recursive = true;
                }
                paths.add( path );
                fileTableModel.setValueAt( true, i, 0 );
                fileTableModel.setValueAt( path, i, 1 );
                fileTableModel.setValueAt( status, i, 2 );
                ++i;
            }
        }
        commitData.setPaths( paths );
        commitData.setRecursive( recursive );

        // load commit properties from bugtraq and tsvn properties
        loadCommitProperties( paths.get( 0 ) );

        if ( jEdit.getBooleanProperty( "ise.plugin.svn.useTsvnTemplate", false ) ) {
            commitMessageTemplate = bugtraqProperties.getProperty( "tsvn:logtemplate" );
        }

        String logregex = bugtraqProperties.getProperty( "bugtraq:logregex" );
        if ( logregex != null && logregex.length() > 0 ) {
            if ( logregex.indexOf( '\n' ) > 0 ) {
                String[] parts = logregex.split( "\n" );
                logRegex0 = parts[ 0 ];
                logRegex1 = parts[ 1 ];
            }
            else {
                logRegex0 = logregex;
            }
        }
        if ( logRegex0 == null ) {
            // if no logregex, use bugtraq:message as the regex
            String regex = bugtraqProperties.getProperty( "bugtraq:message" );  // NOPMD
            if ( regex != null ) {
                logRegex0 = regex.replaceAll( "%BUGID%", "(.*?)" ) + "$";
            }
        }
    }

    private void installComponents( boolean showLogin ) {
        JPanel panel = new JPanel( new BorderLayout() );
        JPanel topPanel = new JPanel( new LambdaLayout() );
        JPanel bottomPanel = new JPanel( new LambdaLayout() );
        panel.setBorder( new EmptyBorder( 12, 11, 11, 12 ) );
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // top panel
        // field for bug number
        String bug_label_name = bugtraqProperties.getProperty( "bugtraq:label" );
        if ( bug_label_name != null || bugtraqProperties.getProperty( "bugtraq:message" ) != null ) {
            JLabel bug_label = new JLabel( bug_label_name == null ? jEdit.getProperty( "ips.Bug_ID>", "Bug ID:" ) : bug_label_name );
            bugField = new JTextField( 10 );
            String bugtraq_number = bugtraqProperties.getProperty( "bugtraq:number" );
            if ( bugtraq_number != null && isTrue( bugtraq_number ) ) {
                ( ( AbstractDocument ) bugField.getDocument() ).setDocumentFilter( new NumericDocumentFilter() );
            }

            // add the components to the option panel
            topPanel.add( "0, 0, 1, 1, W,  , 3", bug_label );
            topPanel.add( "1, 0, 1, 1, W, w, 3", bugField );
            topPanel.add( "0, 1, 1, 1, 0,  , 0", LambdaLayout.createVerticalStrut( 11, true ) );
        }
        JLabel label = new JLabel( jEdit.getProperty( "ips.Enter_comment_for_this_commit>", "Enter comment for this commit:" ) );
        topPanel.add( "0, 2, 6, 1, W,  , 3", label );

        // middle panel        
        // text area for comment entry, autofill with tsvn template if it is available
        comment = new JTextPane();
        comment.setBackground( view.getBackground() );
        comment.setCaretColor( view.getEditPane().getTextArea().getPainter().getCaretColor() );
        comment.setSelectionColor( view.getEditPane().getTextArea().getPainter().getSelectionColor() );
        if ( commitMessageTemplate != null ) {
            comment.setText( commitMessageTemplate );
        }
        JScrollPane scroller = new JScrollPane(comment);
        scroller.getViewport().setPreferredSize( new Dimension( 400, 100 ) );
        panel.add( BorderLayout.CENTER, scroller );

        // bottom panel
        // list for previous comments
        commentList = new PropertyComboBox( "ise.plugin.svn.comment." );
        commentList.setEditable( false );
        commentList.setPreferredSize( new Dimension( 400, commentList.getPreferredSize().height ) );

        JLabel file_label = new JLabel( jEdit.getProperty( "ips.Committing_these_files>", "Committing these files:" ) );
        BestRowTable file_table = new BestRowTable();
        file_table.setModel( fileTableModel );

        // table column widths, first column is checkbox to let user unselect file for commit,
        // second column is filename, third column is status (modified, added, etc)
        file_table.getColumnModel().getColumn( 0 ).setMaxWidth( 25 );
        file_table.getColumnModel().getColumn( 1 ).setWidth( 250 );
        file_table.getColumnModel().getColumn( 2 ).setWidth( 50 );
        file_table.packRows();

        // recursive checkbox, auto check if commitData indicated recursive
        recursiveCheckbox = new JCheckBox( jEdit.getProperty( "ips.Recursively_commit?", "Recursively commit?" ) );
        recursiveCheckbox.setSelected( commitData.getRecursive() );

        // possible login
        login = new LoginPanel( commitData.getPaths().get( 0 ) );
        login.setVisible( showLogin );

        // buttons
        LambdaLayout kl = new LambdaLayout();
        JPanel btn_panel = new JPanel( kl );
        okButton = new JButton( jEdit.getProperty( "ips.Ok", "Ok" ) );
        okButton.setMnemonic( KeyEvent.VK_O );
        cancelButton = new JButton( jEdit.getProperty( "ips.Cancel", "Cancel" ) );
        cancelButton.setMnemonic( KeyEvent.VK_C );
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", okButton );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancelButton );
        kl.makeColumnsSameWidth( 0, 1 );
        
        
        bottomPanel.add( "0, 4, 6, 1, W,  , 3", new JLabel( jEdit.getProperty( "ips.Select_a_previous_comment>", "Select a previous comment:" ) ) );
        bottomPanel.add( "0, 5, 6, 1, W, w, 3", commentList );
        bottomPanel.add( "0, 6, 1, 1, 0,  , 0", LambdaLayout.createVerticalStrut( 11, true ) );

        bottomPanel.add( "0, 7, 6, 1, W,  , 3", file_label );
        JScrollPane file_scroller = new JScrollPane( file_table );
        file_scroller.getViewport().setPreferredSize( new Dimension( 400, Math.min( file_table.getBestHeight(), 200 ) ) );
        bottomPanel.add( "0, 8, 6, 1, W, w, 3", file_scroller );

        if ( commitData.getRecursive() ) {
            bottomPanel.add( "0, 9, 1, 1, 0,  , 0", LambdaLayout.createVerticalStrut( 6, true ) );
            bottomPanel.add( "0, 10, 6, 1, W,  , 3", recursiveCheckbox );
        }

        if ( showLogin ) {
            bottomPanel.add( "0, 11, 1, 1, 0,  , 3", LambdaLayout.createVerticalStrut( 11, true ) );
            bottomPanel.add( "0, 12, 6, 1, 0, w", login );
        }

        bottomPanel.add( "0, 13, 1, 1, 0,  , 3", LambdaLayout.createVerticalStrut( 11, true ) );
        bottomPanel.add( "0, 14, 6, 1, E,  , 0", btn_panel );

        setContentPane( panel );
        pack();
        
        getRootPane().setDefaultButton( okButton );
        okButton.requestFocus();
    }

    private void installListeners() {
        comment.getDocument().addDocumentListener( new CommitMessageDocumentListener( comment, logRegex0, logRegex1 ) );
        commentList.addItemListener( new ItemListener() {
                    public void itemStateChanged( ItemEvent e ) {
                        if ( PropertyComboBox.SELECT.equals( commentList.getSelectedItem().toString() ) ) {
                            return ;
                        }
                        comment.setText( commentList.getSelectedItem().toString() );
                    }
                }
                                   );
        okButton.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        // check/validate for entry in bug field
                        if ( warn() ) {
                            return ;
                        }

                        // get the paths
                        List<String> paths = new ArrayList<String>();
                        for ( int row = 0; row < fileTableModel.getRowCount(); row++ ) {
                            Boolean selected = ( Boolean ) fileTableModel.getValueAt( row, 0 );
                            if ( selected ) {
                                paths.add( ( String ) fileTableModel.getValueAt( row, 1 ) );
                            }
                        }

                        if ( paths.size() == 0 ) {
                            // nothing to commit, bail out
                            commitData = null;
                        }
                        else {
                            commitData.setPaths( paths );
                            String msg = comment.getText();
                            if ( msg == null || msg.length() == 0 ) {
                                msg = jEdit.getProperty( "ips.no_comment", "no comment" );
                            }
                            else {
                                if ( commentList != null ) {
                                    commentList.addValue( msg );
                                }
                            }
                            if ( bugtraqProperties.getProperty( "bugtraq:message" ) != null && bugField != null && bugField.getText().length() > 0 ) {
                                String bugtraq_message = bugtraqProperties.getProperty( "bugtraq:message" );
                                bugtraq_message = bugtraq_message.replaceAll( "\n", " " );  // bugtraq standard says message must be single line
                                String bug_text = bugField.getText();
                                bug_text = bug_text.replaceAll( ", ", "," );  // bugtraq standard says commas must not have space before or after
                                bug_text = bug_text.replaceAll( " ,", "," );
                                bugtraq_message = bugtraq_message.replaceAll( "%BUGID%", bug_text );
                                boolean append = isTrue( bugtraqProperties.getProperty( "bugtraq:append" ) );
                                if ( append ) {
                                    msg += "\n" + bugtraq_message;
                                }
                                else {
                                    msg = bugtraq_message + "\n" + msg;
                                }
                            }
                            commitData.setCommitMessage( msg );

                            commitData.setUsername( login.getUsername() );
                            commitData.setPassword( login.getPassword() );
                            commitData.setRecursive( recursiveCheckbox.isSelected() );
                        }
                        CommitDialog.this._save();
                        CommitDialog.this.setVisible( false );
                        CommitDialog.this.dispose();
                    }
                }
                                  );

        cancelButton.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        commitData = null;
                        CommitDialog.this.setVisible( false );
                        CommitDialog.this.dispose();
                    }
                }
                                      );

    }

    // check for entry in bug field, or if bugtraq:logregex is set, check for
    // bug id in comment
    private boolean warn() {
        String bugtraq_warn = bugtraqProperties.getProperty( "bugtraq:warnifnoissue" );
        boolean warn = isTrue( bugtraq_warn );
        if ( warn && !foundBugId && bugField != null && bugField.getText().length() == 0 ) {
            // no bug entered in bug field, and no bug id found via logregex
            int ignore = JOptionPane.showConfirmDialog( CommitDialog.this.view, "Okay to commit without bug number?\n", "Confirm Commit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
            return ignore != JOptionPane.YES_OPTION;
        }
        else if ( warn && !foundBugId && logRegex0 != null ) {
            // bug id is entered in bug field, need to check it against logregex if there is one.
            String bug_text = bugField.getText();
            bug_text = bug_text.replaceAll( ", ", "," );  // bugtraq standard says commas must not have space before or after
            bug_text = bug_text.replaceAll( " ,", "," );
            String regex = logRegex1 == null ? logRegex0 : logRegex1;
            Pattern p = Pattern.compile( regex );
            Matcher m = p.matcher( bug_text );
            if ( !m.find() ) {
                int ignore = JOptionPane.showConfirmDialog( CommitDialog.this.view, "The bug number appears to be invalid.  Use it anyway?\n", "Confirm Bug ID", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
                return ignore != JOptionPane.YES_OPTION;
            }
        }
        return false;
    }

    protected void _save() {
        if ( commentList != null ) {
            commentList.save();
        }
    }

    public CommitData getCommitData() {
        return commitData;
    }

    // load the tsvn and bugtraq properties associated with the given path.
    // This loads properties starting with tsvn: and bugtraq: from the given
    // path, then works up through parent paths until the project root is
    // reached.
    private void loadCommitProperties( String path ) {
        if ( projectRoot == null ) {
            return ;
        }
        if ( path == null ) {
            return ;
        }
        PropertyData data = new PropertyData();
        data.setOut( new ConsolePrintStream( new NullOutputStream() ) );
        data.setPathsAreURLs( false );
        data.setRecursive( false );
        data.setAskRecursive( false );
        data.setRevision( SVNRevision.WORKING );
        data.setPegRevision( SVNRevision.UNDEFINED );
        String[] credentials = PVHelper.getSVNLogin( path );
        data.setUsername( credentials[ 0 ] );
        data.setPassword( credentials[ 1 ] );
        Property cmd = new Property();

        while ( path.startsWith( projectRoot ) ) {
            data.addPath( path );
            try {
                cmd.doGetProperties( data );
                if ( cmd.getProperties() != null ) {
                    TreeMap map = cmd.getProperties();
                    for ( Object key : map.keySet() ) {
                        Properties props = cmd.getProperties().get( key );
                        for ( Object name : props.keySet() ) {
                            if ( ( name.toString().startsWith( "tsvn:" ) || name.toString().startsWith( "bugtraq:" ) ) && !bugtraqProperties.containsKey( name ) ) {
                                bugtraqProperties.setProperty( name.toString(), props.getProperty( name.toString() ) );
                                //System.out.println( "+++++ " + name.toString() + " = " + props.getProperty( name.toString() ) );
                            }
                        }
                    }
                }
            }
            catch ( Exception e ) {
                return ;
            }

            // up to parent directory
            File f = new File( path );
            path = f.getParent();
            if ( path == null ) {
                break;
            }
            data.setPaths( null );
        }
    }

    private boolean isTrue( String value ) {
        return "true".equals( value ) || "yes".equals( value );
    }

    class NumericDocumentFilter extends DocumentFilter {
        public void insertString( FilterBypass fb,
                int offset, String string, AttributeSet attr )
        throws BadLocationException {
            if ( string == null ) {
                return ;
            }
            if ( isNumeric( string ) ) {
                super.insertString( fb, offset, string, attr );
            }
        }

        public void remove( DocumentFilter.FilterBypass fb,
                int offset,
                int length )
        throws BadLocationException {
            super.remove( fb, offset, length );
        }

        public void replace( FilterBypass fb, int offset,
                int length, String text, AttributeSet attrs )
        throws BadLocationException {
            if ( text == null ) {
                return ;
            }
            if ( isNumeric( text ) ) {
                super.replace( fb, offset, length, text, attrs );
            }
        }

        // allow numbers and commas
        private boolean isNumeric( String string ) {
            for ( char c : string.toCharArray() ) {
                if ( ! Character.isDigit( c ) && c != ',' ) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Highlights bug patterns as specified in bugtraq:logregex property.
     */
    class CommitMessageDocumentListener implements DocumentListener {
        private WordSearcher searcher = null;

        public CommitMessageDocumentListener( JTextPane comp, String regex0, String regex1 ) {
            searcher = new WordSearcher( comp, regex0, regex1 );
        }

        public void insertUpdate( DocumentEvent evt ) {
            foundBugId = searcher.search();
        }

        public void removeUpdate( DocumentEvent evt ) {
            foundBugId = searcher.search();
        }

        public void changedUpdate( DocumentEvent evt ) {}
    }
}