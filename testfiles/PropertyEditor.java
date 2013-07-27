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
import java.awt.CardLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.HistoryTextField;

import ise.java.awt.KappaLayout;
import ise.java.awt.LambdaLayout;
import ise.plugin.svn.data.PropertyData;
import ise.plugin.svn.PVHelper;
import ise.plugin.svn.library.FileUtilities;
import static ise.plugin.svn.gui.HistoryModelNames.*;


/**
 * Dialog for adding files and directories.
 */
public class PropertyEditor extends JDialog {
    // instance fields --
    // parent frame
    private View view = null;

    // property name that might have been passed in by the user
    private String name = null;

    // value that goes with 'name' that might have been passed in by the user
    private String value = null;

    // passed as parameter to constructor, used to decide which property name
    // list to show
    private boolean isDirectory = false;

    // transfer object for returning user choices
    private PropertyData propertyData = null;

    // default property names that can be applied to files.  Most get text
    // values, but a few get specific values, see comments below for each
    // property name.
    private String[] defaultFilePropNames = new String[] {
                "",                                                 // user defined
                "svn:executable",
                "svn:mime-type",
                "svn:ignore",
                "svn:keywords",                                     // Date, Revision, Author, HeadURL, Id
                "svn:eol-style",                                    // native, CRLF, CR, LF
                "svn:externals",
                "svn:special" };

    // default property names that can be applied to directories.  Most get
    // text values, but a few get specific values, see comments below for each
    // property name.
    private String[] defaultDirPropNames = new String[] {
                "",                                                 // user defined
                "svn:mime-type",
                "svn:ignore",
                "svn:keywords",                                     // Date, Revision, Author, HeadURL, Id
                "svn:eol-style",                                    // native, CRLF, CR, LF
                "svn:externals",
                "svn:special",
                "bugtraq:url",
                "bugtraq:warnifnoissue",                            // boolean
                "bugtraq:label",
                "bugtraq:message",
                "bugtraq:number",                                   // boolean
                "bugtraq:append",                                   // boolean
                "bugtraq:logregex"};

    // names of properties that only take a boolean value
    private String[] booleanNames = new String[] {"bugtraq:warnifnoissue", "bugtraq:number", "bugtraq:append"};

    // boolean values as understood by svn
    private String[] booleanChoices = new String[] {"true", "false"};

    // name of keyword property
    // TODO: is using an array of any value?
    private String[] keywordNames = new String[] {"svn:keywords"};

    // keywords understood by svn
    private String[] keywordChoices = new String[] {"Date", "Revision", "Author", "HeadURL", "Id"};

    // name of eol-style property
    // TODO: is using an array of any value?
    private String[] eolNames = new String[] {"svn:eol-style"};

    // eol-style values understood by svn
    private String[] eolChoices = new String[] {"native", "CRLF", "CR", "LF"};

    // components
    private JButton browseButton;
    private HistoryTextField fileHistory;
    private JRadioButton fileButton;
    private JRadioButton textButton;
    private JTextArea textValue;
    private JList listValue;
    private JPanel valueEntryArea;
    private JComboBox propChooser;
    private JCheckBox recursiveCheckbox;
    private JButton okButton;
    private JButton cancelButton;

    // card names for CardLayout for value selection area
    private final String TEXTAREA = "textarea";
    private final String LISTAREA = "listArea";

    /**
     * @param view parent frame for this dialog
     * @param name the name of an svn property, can be null
     * @param value the value of the property specified by 'name', can be null.  If
     * null, indicates that the property should be removed/deleted.
     * @param isDirectory pass 'true' to indicate that this property editor should
     * be used for directory properties.
     */
    public PropertyEditor( View view, String name, String value, boolean isDirectory ) {
        super( ( JFrame ) view, jEdit.getProperty( "ips.Property_Editor", "Property Editor" ), true );
        this.view = view;
        this.name = name;
        this.value = value;
        this.isDirectory = isDirectory;
        _init();
    }

    // initialize the dialog, build the gui, action listeners, etc.
    protected void _init() {
        installComponents();
        installListeners();
    }

    private void installComponents() {

        // for returning to the caller
        propertyData = new PropertyData();

        // file property names, these can be set as a jEdit property.  Defaults
        // to string array defined above.
        String file_props = jEdit.getProperty( "ise.plugin.svn.gui.PropertyEditor.defaultFilePropNames" );
        String[] filePropNames =
            file_props == null || file_props.length() == 0 ?
            defaultFilePropNames :
            file_props.split( "[,]" );

        // directory property names, these can be set as a jEdit property.
        // Defaults to string array defined above.
        String dir_props = jEdit.getProperty( "ise.plugin.svn.gui.PropertyEditor.defaultDirPropNames" );
        String[] dirPropNames =
            dir_props == null || dir_props.length() == 0 ?
            defaultDirPropNames :
            dir_props.split( "[,]" );

        // main panel/content pane for the dialog
        JPanel panel = new JPanel( new LambdaLayout() );
        panel.setBorder( new EmptyBorder( 12, 11, 11, 12 ) );

        // property name chooser, fill with the appropriate file or directory
        // property names
        JLabel prop_name_label = new JLabel( jEdit.getProperty( "ips.Property_name>", "Property name:" ) );
        propChooser = new JComboBox( isDirectory ? dirPropNames : filePropNames );
        propChooser.setEditable( true );

        // value entry panel, text entry or from file -- depending on the selected
        // property name, change to text field, boolean list, keyword list, or
        // eol list.  boolean and eol are single choice, keyword list is multiple
        // selection.
        JPanel value_entry_panel = new JPanel( new LambdaLayout() );
        value_entry_panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), jEdit.getProperty( "ips.Property_value", "Property value" ) ),
                BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) )
            );

        // there are 2 radio buttons, the top one lets the user enter or select
        // property values, the second lets the user specify a file with the
        // property data.
        textButton = new JRadioButton( jEdit.getProperty( "ips.Enter_a_textValue>", "Enter a text value:" ) );
        textButton.setSelected( true );
        fileButton = new JRadioButton( jEdit.getProperty( "ips.Or_load_value_from_file>", "Or load value from file:" ) );
        ButtonGroup bg = new ButtonGroup();
        bg.add( textButton );
        bg.add( fileButton );

        // for the area where the user can either enter a text value for the
        // property or select from a list, use a card layout to switch between
        // a text area or a jlist.
        valueEntryArea = new JPanel( new CardLayout() );

        // always set the actual value as text in this text area
        textValue = new JTextArea( 8, 30 );
        if ( value != null ) {
            textValue.setText( value );
        }
        JPanel text_area_panel = new JPanel( new BorderLayout() );
        text_area_panel.add( new JScrollPane( textValue ), BorderLayout.CENTER );
        valueEntryArea.add( text_area_panel, TEXTAREA );

        // this list is used for keywords, eol-style, and boolean values.  The
        // list model is changed as appropriate.
        listValue = new JList();
        listValue.setVisibleRowCount( 8 );
        JPanel listValue_panel = new JPanel( new BorderLayout() );
        listValue_panel.add( new JScrollPane( listValue ), BorderLayout.CENTER );
        valueEntryArea.add( listValue_panel, LISTAREA );

        // let the user choose a file for property data
        fileHistory = new HistoryTextField( PATH );
        fileHistory.setColumns( 30 );
        fileHistory.setEnabled( false );

        browseButton = new JButton( jEdit.getProperty( "ips.Browse...", "Browse..." ) );
        browseButton.setEnabled( false );

        // lay out value entry panel
        value_entry_panel.add( "0, 0, 7, 1, W, w, 3", textButton );
        value_entry_panel.add( "0, 1, 1, 1", KappaLayout.createHorizontalStrut( 11, true ) );
        value_entry_panel.add( "0, 2, 7, 1, W, wh, 3", valueEntryArea );
        value_entry_panel.add( "0, 3, 1, 1", KappaLayout.createVerticalStrut( 6, true ) );
        value_entry_panel.add( "0, 4, 7, 1, W, w, 3", fileButton );
        value_entry_panel.add( "0, 5, 1, 1", KappaLayout.createHorizontalStrut( 11, true ) );
        value_entry_panel.add( "1, 6, 5, 1, W, w, 3", fileHistory );
        value_entry_panel.add( "6, 6, 1, 1, E,  , 3", browseButton );

        // recursive checkbox
        recursiveCheckbox = new JCheckBox( jEdit.getProperty( "ips.Apply_recursively?", "Apply recursively?" ) );
        recursiveCheckbox.setSelected( false );
        recursiveCheckbox.setEnabled( isDirectory );

        // ok and cancel buttons
        KappaLayout kl = new KappaLayout();
        JPanel btn_panel = new JPanel( kl );
        okButton = new JButton( jEdit.getProperty( "ips.Ok", "Ok" ) );
        cancelButton = new JButton( jEdit.getProperty( "ips.Cancel", "Cancel" ) );
        btn_panel.add( "0, 0, 1, 1, 0, w, 3", okButton );
        btn_panel.add( "1, 0, 1, 1, 0, w, 3", cancelButton );
        kl.makeColumnsSameWidth( 0, 1 );

        // add the components to the main content pane
        panel.add( "0, 0, 1, 1, W,  , 6", prop_name_label );
        panel.add( "1, 0, 5, 1, W, w, 4", propChooser );

        panel.add( "0, 1, 6, 1, W, wh, 3", value_entry_panel );
        panel.add( "0, 2, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 6, true ) );

        panel.add( "0, 3, 6, 1, W,  , 6", recursiveCheckbox );
        panel.add( "0, 4, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );

        panel.add( "0, 5, 6, 1, E,  , 6", btn_panel );

        setContentPane( panel );
        pack();

        // all happy now, so set the parameter that was passed in so everything
        // looks pretty
        propChooser.setSelectedItem( name == null ? "" : name );

    }

    private void installListeners() {
        browseButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    String[] filename = GUIUtilities.showVFSFileDialog( view, PVHelper.getProjectRoot( view ), VFSBrowser.OPEN_DIALOG, false );
                    if ( filename != null && filename.length > 0 ) {
                        fileHistory.setText( filename[ 0 ] );
                    }
                }
            }
        );

        // action listener for radio buttons, enable parts as needed
        ActionListener al = new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        fileHistory.setEnabled( fileButton.isSelected() );
                        browseButton.setEnabled( fileButton.isSelected() );
                        textValue.setEnabled( textButton.isSelected() );
                        listValue.setEnabled( textButton.isSelected() );
                    }
                };
        textButton.addActionListener( al );
        fileButton.addActionListener( al );

        // add a listener to the listValue so that when the user picks from
        // the list, the selected values are put into the textValue in the
        // right format.  textValue always holds the actual value that will
        // be sent to svn, the list is just a convenience for the user.
        listValue.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged( ListSelectionEvent lse ) {
                    JList list = ( JList ) lse.getSource();
                    Object[] values = list.getSelectedValues();
                    StringBuffer sb = new StringBuffer();
                    for ( int i = 0; i < values.length; i++ ) {
                        sb.append( values[ i ].toString() ).append( ' ' );
                    }
                    textValue.setText( sb.toString().trim() );
                }
            }
        );

        // item listener for prop name chooser to use the right property value
        // selector
        propChooser.addItemListener(
            new ItemListener() {
                public void itemStateChanged( ItemEvent ie ) {
                    final String choice = ( String ) ie.getItem();
                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {

                                // check for boolean property name, if found,
                                // show the boolean value list
                                for ( String item : booleanNames ) {
                                    if ( item.equals( choice ) ) {
                                        listValue.setListData( booleanChoices );
                                        listValue.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
                                        if ( value != null ) {
                                            listValue.setSelectedIndex( value.equals( "true" ) ? 0 : 1 );
                                        }
                                        CardLayout cl = ( CardLayout ) valueEntryArea.getLayout();
                                        cl.show( valueEntryArea, LISTAREA );
                                        textButton.setText( jEdit.getProperty( "ips.Choose_a_value>", "Choose a value:" ) );
                                        PropertyEditor.this.repaint();
                                        return ;
                                    }
                                }

                                // check for keyword property name, if found,
                                // show the keyword value list
                                for ( String item : keywordNames ) {
                                    if ( item.equals( choice ) ) {
                                        listValue.setListData( keywordChoices );
                                        // there can be multiple keywords
                                        listValue.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
                                        if ( value != null ) {
                                            String[] values = value.split( " " );
                                            int[] selected = new int[ values.length ];
                                            for ( int j = 0; j < values.length; j++ ) {
                                                for ( int i = 0; i < keywordChoices.length; i++ ) {
                                                    if ( keywordChoices[ i ].equals( values[ j ] ) ) {
                                                        selected[ j ] = i;
                                                        continue;
                                                    }
                                                }
                                            }
                                            listValue.setSelectedIndices( selected );
                                        }
                                        CardLayout cl = ( CardLayout ) valueEntryArea.getLayout();
                                        cl.show( valueEntryArea, LISTAREA );
                                        textButton.setText( jEdit.getProperty( "ips.Choose_one_or_more_values>", "Choose one or more values:" ) );
                                        PropertyEditor.this.repaint();
                                        return ;
                                    }
                                }

                                // check for eol-style property name, if found,
                                // show the eol-style value list
                                for ( String item : eolNames ) {
                                    if ( item.equals( choice ) ) {
                                        listValue.setListData( eolChoices );
                                        listValue.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
                                        if ( value != null ) {
                                            for ( int i = 0; i < eolChoices.length; i++ ) {
                                                if ( eolChoices[ i ].equals( value ) ) {
                                                    listValue.setSelectedIndex( i );
                                                    return ;
                                                }
                                            }
                                        }
                                        CardLayout cl = ( CardLayout ) valueEntryArea.getLayout();
                                        cl.show( valueEntryArea, LISTAREA );
                                        textButton.setText( jEdit.getProperty( "ips.Choose_a_value>", "Choose a value:" ) );
                                        PropertyEditor.this.repaint();
                                        return ;
                                    }
                                }

                                // everything else is free-form text
                                CardLayout cl = ( CardLayout ) valueEntryArea.getLayout();
                                cl.show( valueEntryArea, TEXTAREA );
                                textButton.setText( "Enter a text value:" );
                                PropertyEditor.this.repaint();
                            }
                        }
                    );
                }
            }
        );

        recursiveCheckbox.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    propertyData.setRecursive( recursiveCheckbox.isSelected() );
                }
            }
        );

        okButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {

                    // get user-selected/entered property name
                    Object item = propChooser.getSelectedItem();
                    if ( item != null && item.toString().length() > 0 ) {
                        propertyData.setName( item.toString() );

                        if ( textButton.isSelected() ) {
                            // get user selected/entered property value.  The lists put their values here also.
                            propertyData.setValue( textValue.getText() == null ? "" : textValue.getText() );
                        }
                        else {
                            // load properties from a file the user has selected
                            String filename = fileHistory.getText();
                            if ( filename == null || filename.length() == 0 ) {
                                JOptionPane.showMessageDialog( view, jEdit.getProperty( "ips.No_filename_entered_for_property_value.", "No filename entered for property value." ), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                                fileHistory.requestFocusInWindow();
                                return ;
                            }
                            try {
                                Reader reader = new BufferedReader( new FileReader( filename ) );
                                StringWriter writer = new StringWriter();
                                FileUtilities.copy( reader, writer );
                                propertyData.setValue( writer.toString() );
                            }
                            catch ( Exception e ) {
                                JOptionPane.showMessageDialog( view, jEdit.getProperty( "ips.Unable_to_read_property_value_from_file>", "Unable to read property value from file:" ) + "\n" + e.getMessage(), jEdit.getProperty( "ips.Error", "Error" ), JOptionPane.ERROR_MESSAGE );
                                return ;
                            }
                        }
                    }

                    // all done
                    PropertyEditor.this.setVisible( false );
                    PropertyEditor.this.dispose();
                    fileHistory.addCurrentToHistory();
                }
            }
        );

        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    propertyData = null;
                    PropertyEditor.this.setVisible( false );
                    PropertyEditor.this.dispose();
                }
            }
        );
    }


    /**
     * @return null indicates user cancelled
     */
    public PropertyData getPropertyData() {
        return propertyData;
    }

    // for testing
    public static void main ( String[] args ) {
        PropertyEditor pe = new PropertyEditor( null, null, null, true );
        pe.setVisible( true );
    }
}