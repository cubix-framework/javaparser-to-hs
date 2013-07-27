/*
* Modified for Beauty, original borrowed from:
* ParserOptionPane.java - Sidekick parsers configuration panel
*
* Copyright (C) 2005 Matthieu Casanova
* Portions copyright (C) 2000, 2003 Slava Pestov
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package beauty.options;

// Imports
import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.*;

import beauty.beautifiers.Beautifier;


// BeautifierOptionPane class
/**
 * An option pane to configure the mode to beautifier associations.
 *
 * @author Matthieu Casanova, Dale Anson
 */
public class BeautifierOptionPane extends AbstractOptionPane {

    public BeautifierOptionPane() {
        super( "beauty.beautifiers" );
    }

    // _init() method
    public void _init() {
        setLayout( new BorderLayout() );
        setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
        
        JPanel topPanel = new JPanel( new BorderLayout() );
        topPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 16, 0 ) );
        JLabel description = new JLabel( jEdit.getProperty("beauty.msg.<html><b>Assign_beautifiers_to_modes", "<html><b>Assign beautifiers to modes") );
        topPanel.add( BorderLayout.CENTER, description );
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        final JCheckBox defaultIndenter = new JCheckBox( jEdit.getProperty("beauty.msg.Use_jEdit_indenter_for_undefined_modes", "Use jEdit indenter for undefined modes") );
        defaultIndenter.setSelected( jEdit.getBooleanProperty( "beauty.useBuiltInIndenter", true ) );
        defaultIndenter.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    jEdit.setBooleanProperty( "beauty.useBuiltInIndenter", defaultIndenter.isSelected() );
                }
            }
        );
        centerPanel.add(BorderLayout.NORTH, defaultIndenter);
        centerPanel.add(BorderLayout.CENTER, createTableScroller());
        
        add( BorderLayout.NORTH, topPanel );
        add( BorderLayout.CENTER, centerPanel );
    }

    // _save() method
    public void _save() {
        tableModel.save();
    }

    // Private members

    // Instance variables
    private MyTableModel tableModel;


    // createTableScroller() method
    private JScrollPane createTableScroller() {
        tableModel = createModel();
        JTable table = new JTable( tableModel );
        table.getTableHeader().setReorderingAllowed( false );
        table.setColumnSelectionAllowed( false );
        table.setRowSelectionAllowed( false );
        table.setCellSelectionEnabled( false );
        String [] serviceNames = ServiceManager.getServiceNames( Beautifier.SERVICE_NAME );
        Vector beautifierList = new Vector( serviceNames.length + 2 );
        for ( int i = 0; i < serviceNames.length; i++ ) {
            beautifierList.add( serviceNames[ i ] );
        }
        Collections.sort( beautifierList,
                new Comparator() {
                    public int compare( Object a, Object b ) {
                        return a.toString().compareToIgnoreCase( b.toString() );
                    }
                }
                        );
        beautifierList.add( 0, MyTableModel.BEAUTIFIER_NONE );
        beautifierList.add( 0, null );
        BeautifierCellRenderer comboBox = new BeautifierCellRenderer( beautifierList );
        table.setRowHeight( comboBox.getPreferredSize().height );
        TableColumn column = table.getColumnModel().getColumn( 1 );
        column.setCellRenderer( comboBox );
        column.setCellEditor( new DefaultCellEditor( new BeautifierCellRenderer( beautifierList ) ) );

        Dimension d = table.getPreferredSize();
        d.height = Math.min( d.height, 50 );
        JScrollPane scroller = new JScrollPane( table );
        scroller.setPreferredSize( d );
        return scroller;
    }

    // createWindowModel() method
    private MyTableModel createModel() {
        return new MyTableModel();
    }



    // DockPositionCellRenderer class
    class BeautifierCellRenderer extends JComboBox implements TableCellRenderer {
        BeautifierCellRenderer( Vector vector ) {
            super( vector );
            BeautifierCellRenderer.this.setRequestFocusEnabled( false );
        }

        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            setSelectedItem( value );
            return this;
        }
    }
}

// WindowTableModel class
class MyTableModel extends AbstractTableModel {
    private Vector modes;

    public static final String BEAUTIFIER_NONE = "none";

    // WindowTableModel constructor
    MyTableModel() {
        Mode [] modes = jEdit.getModes();
        this.modes = new Vector( modes.length );
        for ( int i = 0; i < modes.length; i++ ) {
            this.modes.addElement( new Entry( modes[ i ].getName() ) );
        }
        java.util.Collections.sort( this.modes );
    }

    // getColumnCount() method
    public int getColumnCount() {
        return 2;
    }

    // getRowCount() method
    public int getRowCount() {
        return modes.size();
    }

    // getColumnClass() method
    public Class getColumnClass( int col ) {
        switch ( col ) {
            case 0:
            case 1:
                return String .class;
            default:
                throw new InternalError();
        }
    }

    // getValueAt() method
    public Object getValueAt( int row, int col ) {
        Entry modeBeautifier = ( Entry ) modes.elementAt( row );
        switch ( col ) {
            case 0:
                return modeBeautifier.mode;
            case 1:
                return modeBeautifier.beautifier;
            default:
                throw new InternalError();
        }
    }

    // isCellEditable() method
    public boolean isCellEditable( int row, int col ) {
        return col == 1;
    }

    // setValueAt() method
    public void setValueAt( Object value, int row, int col ) {
        if ( col == 0 ) {
            return ;
        }


        Entry modeBeautifier = ( Entry ) modes.elementAt( row );
        switch ( col ) {
            case 1:
                modeBeautifier.beautifier = ( String ) value;
                break;
            default:
                throw new InternalError();
        }

        fireTableRowsUpdated( row, row );
    }

    // getColumnName() method
    public String getColumnName( int index ) {
        switch ( index ) {
            case 0:
                return jEdit.getProperty( "options.beauty.beautifiers.mode" );
            case 1:
                return jEdit.getProperty( "options.beauty.beautifiers.beautifier" );
            default:
                throw new InternalError();
        }
    }

    // save() method
    public void save() {
        for ( int i = 0; i < modes.size(); i++ ) {
            ( ( Entry ) modes.elementAt( i ) ).save();
        }
    }

    // Entry class
    class Entry implements Comparable {
        String mode;
        String beautifier = null;

        Entry( String mode ) {
            this.mode = mode;
            beautifier = jEdit.getProperty( "mode." + this.mode + ".beauty.beautifier" );

        }

        void save() {
            if ( beautifier == BEAUTIFIER_NONE ) {
                jEdit.resetProperty( "mode." + mode + ".beauty.beautifier" );
            }
            else {
                jEdit.setProperty( "mode." + mode + ".beauty.beautifier",
                        beautifier );
            }

        }

        public int compareTo( Object a ) {
            return this.mode.compareToIgnoreCase( ( ( Entry ) a ).mode );
        }
    }
}