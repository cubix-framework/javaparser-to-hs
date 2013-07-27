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

import java.awt.Dimension;
import java.io.File;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.jEdit;

import ise.java.awt.*;
import ise.plugin.svn.data.*;
import ise.plugin.svn.command.*;
import ise.plugin.svn.gui.component.*;
import ise.plugin.svn.library.*;
import static ise.plugin.svn.gui.HistoryModelNames.*;

import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNCopySource;

public class ExportDialog extends JDialog {
    // instance fields
    private View view = null;
    private ExportData data = null;

    private HistoryTextField path = null;
    private JCheckBox recursive_cb = null;
    private RevisionSelectionPanel revision_panel = null;
    private RevisionSelectionPanel peg_revision_panel = null;
    private JComboBox eol = null;
    private JCheckBox force = null;
    private boolean canceled = false;

    public ExportDialog( View view, ExportData data ) {
        super( ( JFrame ) view, jEdit.getProperty("ips.Export", "Export"), true );
        this.view = view;
        if ( data == null ) {
            throw new IllegalArgumentException( "data cannot be null" );
        }
        if ( data.getSourceFiles() == null && data.getSourceURLs() == null ) {
            throw new IllegalArgumentException( "no source file(s) to copy" );
        }
        this.data = data;
        init();
    }

    protected void init() {
        KappaLayout layout = new KappaLayout();
        JPanel panel = new JPanel( layout );
        panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

        // source for export
        List<String> paths = new ArrayList<String>();
        if ( data.getSourceFiles() != null ) {
            SVNCopySource[] sources = data.getSourceFiles();
            for (SVNCopySource source : sources) {
                paths.add(source.getFile().toString());
            }
        }
        else {
            SVNCopySource[] sources = data.getSourceURLs();
            for (SVNCopySource source : sources) {
                paths.add(source.getURL().toString());
            }
        }

        JLabel file_label = new JLabel( jEdit.getProperty("ips.Export", "Export") + " " + ( paths.size() == 1 ? jEdit.getProperty("ips.this_file>", "this file:") : jEdit.getProperty("ips.these_files>", "these files:") ) );
        BestRowTable file_table = new BestRowTable();
        final DefaultTableModel file_table_model = new DefaultTableModel(
                    new String[] {
                        "", jEdit.getProperty("ips.File", "File")
                    }, paths.size() ) {
                    public Class getColumnClass( int index ) {
                        if ( index == 0 ) {
                            return Boolean.class;
                        }
                        else {
                            return super.getColumnClass( index );
                        }

                    }
                };
        file_table.setModel( file_table_model );

        // load the table model
        int i = 0;
        for ( Object path : paths ) {
            if ( path != null ) {
                file_table_model.setValueAt( true, i, 0 );
                file_table_model.setValueAt( path, i, 1 );
                ++i;
            }
        }
        file_table.getColumnModel().getColumn( 0 ).setMaxWidth( 25 );
        file_table.getColumnModel().getColumn( 1 ).setPreferredWidth( 625 );
        file_table.packRows();

        recursive_cb = new JCheckBox( jEdit.getProperty("ips.Recursive?", "Recursive?") );
        recursive_cb.setSelected( true );

        // revision selection panels
        revision_panel = new RevisionSelectionPanel( jEdit.getProperty("ips.Export_from_this_revision>", "Export from this revision:"), SwingConstants.VERTICAL, data.getSourceURLs() == null );
        peg_revision_panel = new RevisionSelectionPanel( jEdit.getProperty("ips.Using_this_peg_revision>", "Using this peg revision:"), SwingConstants.VERTICAL, false, false, true, false, false );

        // destination
        JLabel path_label = new JLabel( jEdit.getProperty("ips.Export_to_this_directory>", "Export to this directory:") );
        path = new HistoryTextField(PATH);
        path.setText("");
        path.setColumns(30);
        JButton browse_local_btn = new JButton( jEdit.getProperty("ips.Browse_Local...", "Browse Local...") );
        browse_local_btn.setMnemonic(KeyEvent.VK_B);
        browse_local_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        String[] dirs = GUIUtilities.showVFSFileDialog( view, System.getProperty( "user.home" ), VFSBrowser.CHOOSE_DIRECTORY_DIALOG, false );
                        if ( dirs != null && dirs.length > 0 ) {
                            String filename = dirs[ 0 ];
                            File f = new File( filename );
                            path.setText( f.getAbsolutePath() );
                        }
                    }
                }
                                          );

        eol = new JComboBox( new String[] {"native", "CRLF (Windows)", "LF (Unix)", "CR (Old Mac)"} );
        eol.setEditable( false );
        eol.setSelectedItem( "native" );

        force = new JCheckBox(jEdit.getProperty("ips.Overwrite_existing_files?", "Overwrite existing files?"));

        // ok and cancel buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        JButton ok_btn = new JButton( jEdit.getProperty("ips.Ok", "Ok") );
        ok_btn.setMnemonic(KeyEvent.VK_O);
        JButton cancel_btn = new JButton( jEdit.getProperty("ips.Cancel", "Cancel") );
        cancel_btn.setMnemonic(KeyEvent.VK_C);
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", ok_btn );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancel_btn );
        kl.makeColumnsSameWidth( 0, 1 );

        ok_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        if ( path.getText() == null || path.getText().length() == 0 ) {
                            JOptionPane.showMessageDialog( view, jEdit.getProperty("ips.Please_select_a_destination_directory_for_the_export.", "Please select a destination directory for the export."), jEdit.getProperty("ips.Error", "Error"), JOptionPane.ERROR_MESSAGE );
                            return ;
                        }
                        if ( data.getSourceFiles() != null ) {
                            List<File> paths = new ArrayList<File>();
                            for ( int row = 0; row < file_table_model.getRowCount(); row++ ) {
                                Boolean selected = ( Boolean ) file_table_model.getValueAt( row, 0 );
                                if ( selected ) {
                                    paths.add( new File( file_table_model.getValueAt( row, 1 ).toString() ) );
                                }
                            }

                            if ( paths.size() == 0 ) {
                                // nothing to add, bail out
                                canceled = true;
                            }
                            else {
                                data.setSourceFiles( paths );
                                canceled = false;
                            }
                        }
                        else if ( data.getSourceURLs() != null ) {
                            List<SVNURL> paths = new ArrayList<SVNURL>();
                            try {
                                for ( int row = 0; row < file_table_model.getRowCount(); row++ ) {
                                    Boolean selected = ( Boolean ) file_table_model.getValueAt( row, 0 );
                                    if ( selected ) {
                                        paths.add( SVNURL.parseURIDecoded( file_table_model.getValueAt( row, 1 ).toString()) );
                                    }
                                }
                            }
                            catch ( Exception e ) {
                                throw new IllegalArgumentException( e.getMessage() );
                            }

                            if ( paths.size() == 0 ) {
                                // nothing to add, bail out
                                canceled = true;
                            }
                            else {
                                data.setSourceURLs( paths );
                                canceled = false;
                            }
                        }
                        else {
                            canceled = true;
                        }
                        if ( canceled ) {
                            data = null;
                        }
                        else {
                            data.setRecursive( recursive_cb.isSelected() );
                            data.setRevision( revision_panel.getRevision() );
                            data.setPegRevision( peg_revision_panel.getRevision() );
                            data.setDestinationFile( new File( path.getText() ) );
                            String[] line_ender = eol.getSelectedItem().toString().split( " " );
                            data.setEOLStyle( line_ender[ 0 ] );
                            data.setForce(force.isSelected());
                        }
                        ExportDialog.this.setVisible( false );
                        ExportDialog.this.dispose();
                        path.addCurrentToHistory();
                    }
                }
                                );

        cancel_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        canceled = true;
                        ExportDialog.this.setVisible( false );
                        ExportDialog.this.dispose();
                    }
                }
                                    );


        // add the components to the option panel
        JScrollPane file_scroller = new JScrollPane( file_table );
        file_scroller.getViewport().setPreferredSize( new Dimension( 600, Math.min( file_table.getBestHeight(), 250 ) ) );
        panel.add( "0, 0, 8, 1, W, w, 3", file_label );
        panel.add( "0, 1, 8, 1, W, w, 3", file_scroller );
        panel.add( "0, 2, 8, 1, W, w, 3", recursive_cb );
        panel.add( "0, 3, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        LambdaLayout lam = new LambdaLayout();
        JPanel revision_panel_holder = new JPanel(lam);
        revision_panel_holder.add( "0, 0, 1, 1, 0, w, 3", revision_panel );
        revision_panel_holder.add( "1, 0, 1, 1, 0, wh, 3", peg_revision_panel );
        lam.makeColumnsSameWidth(0, 1);
        panel.add( "0, 4, 8, 1, 0, w, 0", revision_panel_holder );
        panel.add( "0, 5, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 6, 8, 1, W,  , 3", path_label );
        panel.add( "0, 7, 8, 1, w, w, 3", path );
        panel.add( "0, 8, 2, 1, W,  , 3", browse_local_btn );
        panel.add( "4, 8, 2, 1, W,  , 0", force );
        panel.add( "0, 9, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 10, 1, 1, W,  , 3", new JLabel( jEdit.getProperty("ips.End-of-line_style>", "End-of-line style:") ) );
        panel.add( "1, 10, 2, 1, 0, w, 3", eol );
        panel.add( "0, 12, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 14, 8, 1, E,  , 0", btn_panel );

        setContentPane( panel );
        pack();

        getRootPane().setDefaultButton(ok_btn);
        ok_btn.requestFocus();
    }

    public ExportData getData() {
        if ( canceled ) {
            return null;
        }
        return data;
    }

    public static void main (String[] args) {
        ExportData data = new ExportData();
        List<File> files = new ArrayList<File>();
        files.add(new File(System.getProperty("user.home")));
        data.setSourceFiles(files);
        ExportDialog ed = new ExportDialog(null, data);
        ed.setVisible(true);
    }
}
