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

import ise.plugin.svn.library.*;
import ise.plugin.svn.data.PropertyData;
import ise.java.awt.KappaLayout;
import ise.java.awt.LambdaLayout;
import ise.plugin.svn.command.Property;
import ise.plugin.svn.io.ConsolePrintStream;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * A panel to display SVN properties.
 *
 * @author    Dale Anson
 */
public class PropertyPanel extends JPanel {

    private View view = null;
    private Map<String, Properties> results = null;
    private PropertyData originalData = null;

    /**
     * Show properties for a single file.
     * @param view parent view/frame
     * @param filename the file the props are for
     * @param props the svn properties associated with the file
     */
    public PropertyPanel( View view, String filename, Properties props ) {
        this.view = view;
        results = new HashMap<String, Properties>();
        results.put( filename, props );
        init();
    }

    /**
     * Show properties for multiple files.
     * @param view parent view/frame
     * @param results a map of filename -> properties
     * @param data the original data object used to request the properties.
     */
    public PropertyPanel( View view, Map<String, Properties> results, PropertyData data ) {
        this.view = view;
        this.results = results;
        this.originalData = data;
        init();
    }

    private void init( ) {
        // if there aren't any results, then say so using the property display
        if ( results == null || results.size() == 0 ) {
            results = new HashMap<String, Properties>();
            Properties p = new Properties();
            p.setProperty( "Error", "No properties available." );
            results.put( "Error", p );
        }

        // main panel
        JPanel properties_panel = new JPanel( new LambdaLayout() );
        //properties_panel.setLayout( new BoxLayout( properties_panel, BoxLayout.Y_AXIS ) );
        LambdaLayout.Constraints con = LambdaLayout.createConstraint();
        con.x = 0;
        con.y = 0;
        con.w = 1;
        con.h = 1;
        con.a = LambdaLayout.W;
        con.s = "w";
        con.p = 3;


        // create sub-panels, one per file found in results
        int row = 0;
        for ( String fn : results.keySet() ) {
            final String filename = fn;
            // fetch the properties and load them into a table
            Properties props = results.get(filename);
            final BestRowTable props_table = new BestRowTable( );

            // declare buttons here so table action listeners can react
            final JButton add_btn = new JButton( jEdit.getProperty( "ips.Add", "Add" ) );
            final JButton edit_btn = new JButton( jEdit.getProperty( "ips.Edit", "Edit" ) );
            final JButton delete_btn = new JButton( jEdit.getProperty( "ips.Delete", "Delete" ) );

            // set up table model and selection model
            final DefaultTableModel model = new DefaultTableModel(
                        new String[] {
                            jEdit.getProperty( "ips.Name", "Name" ), jEdit.getProperty( "ips.Value", "Value" )
                        }, props.size() );
            props_table.setModel( model );
            props_table.setRowSelectionAllowed( true );
            props_table.setColumnSelectionAllowed( false );
            DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
            selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
            props_table.setSelectionModel( selectionModel );
            selectionModel.addListSelectionListener( new ListSelectionListener() {
                        public void valueChanged( ListSelectionEvent lse ) {
                            edit_btn.setEnabled( props_table.getSelectedRow() > -1 );
                            delete_btn.setEnabled( props_table.getSelectedRow() > -1 );
                        }
                    }
                                                   );

            // fill the table
            int i = 0;
            for (Enumeration en = props.propertyNames(); en.hasMoreElements();) {
                String key = (String)en.nextElement();
                String value = props.getProperty(key);
                if ( value == null ) {
                    continue;
                }
                model.setValueAt( key, i, 0 );
                model.setValueAt( value, i, 1 );
                ++i;
            }

            TableColumnModel column_model = props_table.getColumnModel();
            TableColumn column0 = column_model.getColumn( 0 );  // name
            column0.setPreferredWidth( 120 );
            column0.setCellRenderer( new NoWrapCellRenderer() );
            TableColumn column1 = column_model.getColumn( 1 );  // value
            column1.setPreferredWidth( 200 );
            column1.setCellRenderer( new NoWrapCellRenderer() );

            // add a mouse listener for the popup
            props_table.addMouseListener( new TableCellViewer( props_table ) );

            // create and add a panel with this result
            JPanel panel = new JPanel( new LambdaLayout() );
            panel.setBorder( new CompoundBorder( new EtchedBorder(), new EmptyBorder( 3, 3, 3, 3 ) ) );
            JLabel filename_label = new JLabel( jEdit.getProperty( "ips.Properties_for>", "Properties for:" ) + " " + filename, JLabel.LEFT );
            panel.add( filename_label, "0, 0, 1, 1, W, w, 3" );
            props_table.packRows();
            panel.add( GUIUtils.createTablePanel( props_table ), "0, 1, 1, 1, 0, wh, 3" );

            // set up the add, edit, and delete buttons if for working copy
            if ( !originalData.getRemote() ) {
                KappaLayout kl = new KappaLayout();
                JPanel btn_panel = new JPanel( kl );
                add_btn.setEnabled( true );
                edit_btn.setEnabled( false );
                delete_btn.setEnabled( false );

                // button action listeners
                add_btn.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            File f = new File(filename);
                            PropertyEditor dialog = new PropertyEditor( view, null, null, f.isDirectory() );
                            GUIUtils.center( view, dialog );
                            dialog.setVisible( true );
                            PropertyData data = dialog.getPropertyData();
                            if ( data == null ) {
                                return ;     // user canceled
                            }
                            model.addRow( new String[] {data.getName(), data.getValue() } );
                            data.addPath( filename );
                            data.setOut( new ConsolePrintStream( view ) );
                            if ( originalData != null ) {
                                data.setUsername( originalData.getUsername() );
                                data.setPassword( originalData.getPassword() );
                            }
                            data.setRevision(SVNRevision.WORKING);
                            Property property = new Property();
                            try {
                                property.doSetProperties( data );
                            }
                            catch ( Exception e ) {
                                e.printStackTrace();
                            }
                            SwingUtilities.invokeLater( new Runnable() {
                                        public void run() {
                                            props_table.doLayout();
                                        }
                                    }
                                                      );
                        }
                    }
                );
                edit_btn.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            int row = props_table.getSelectedRow();
                            if ( row > -1 ) {
                                String key = ( String ) model.getValueAt( row, 0 );
                                String value = ( String ) model.getValueAt( row, 1 );
                                PropertyEditor dialog = new PropertyEditor( view, key, value, true );
                                GUIUtils.center( view, dialog );
                                dialog.setVisible( true );
                                PropertyData data = dialog.getPropertyData();
                                if ( data == null ) {
                                    return ;     // user canceled
                                }
                                model.setValueAt( data.getName(), row, 0 );
                                model.setValueAt( data.getValue(), row, 1 );
                                data.setOut( new ConsolePrintStream( view ) );
                                data.addPath( filename );
                                if ( originalData != null ) {
                                    data.setUsername( originalData.getUsername() );
                                    data.setPassword( originalData.getPassword() );
                                }
                                Property property = new Property();
                                try {
                                    property.doSetProperties( data );
                                }
                                catch ( Exception e ) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                );
                delete_btn.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            int row = props_table.getSelectedRow();
                            String key = ( String ) model.getValueAt( row, 0 );
                            int confirm = JOptionPane.showConfirmDialog( view,
                                    jEdit.getProperty( "ips.Delete_property_named", "Delete property named" ) + " " + key + "?",
                                    jEdit.getProperty( "ips.Confirm_Delete?", "Confirm Delete?" ),
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE );
                            if ( confirm != JOptionPane.YES_OPTION ) {
                                return ;
                            }
                            PropertyData data = new PropertyData();
                            data.setOut( new ConsolePrintStream( view ) );
                            data.addPath( filename );
                            data.setName( key );
                            data.setValue( null );
                            if ( originalData != null ) {
                                data.setUsername( originalData.getUsername() );
                                data.setPassword( originalData.getPassword() );
                            }
                            Property property = new Property();
                            try {
                                property.doSetProperties( data );
                            }
                            catch ( Exception e ) {
                                e.printStackTrace();
                            }
                            model.removeRow( row );
                        }
                    }
                );

                btn_panel.add( add_btn, "0, 0, 1, 1, 0, w, 0" );
                btn_panel.add( edit_btn, "1, 0, 1, 1, 0, w, 0" );
                btn_panel.add( delete_btn, "2, 0, 1, 1, 0, w, 0" );
                kl.makeColumnsSameWidth( new int[] {0, 1, 2} );

                panel.add( btn_panel, "0, 2, 1, 1, E, 0, 3" );
            }

            properties_panel.add( panel, con );
            ++con.y;
            ++row;
        }


        // construct this panel
        setLayout( new BorderLayout() );
        setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );

        JScrollPane js = new JScrollPane( properties_panel );
        add( js, BorderLayout.CENTER );
    }

}