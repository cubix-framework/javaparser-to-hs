/*
* Modified for TaskList, original borrowed from:
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

package tasklist.options;

// Imports
import javax.swing.table.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.*;
import org.gjt.sp.jedit.*;

import tasklist.TaskListPlugin;

/**
 * An option pane to configure the task list mode parser.
 */
public class TaskListModesOptionPane extends AbstractOptionPane {

    String[] parseTypes = {
        jEdit.getProperty("options.tasklist.modes.comments", "Comments"),
        jEdit.getProperty("options.tasklist.modes.none", "None"), 
        jEdit.getProperty("options.tasklist.modes.all", "All")
    };
    
    public TaskListModesOptionPane() {
        super( "tasklist.modes" );
    }

    // _init() method
    public void _init() {
        setLayout( new BorderLayout() );
        setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
        
        JPanel topPanel = new JPanel( new BorderLayout() );
        topPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 16, 0 ) );
        JLabel description = new JLabel( jEdit.getProperty("options.tasklist.modes.parse.label", "Parse Tasks for these Modes:") );
        topPanel.add( BorderLayout.CENTER, description );
        
        JPanel centerPanel = new JPanel(new BorderLayout());
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
    private ParserTypeTableModel tableModel;


    // createTableScroller() method
    private JScrollPane createTableScroller() {
        tableModel = createModel();
        JTable table = new JTable( tableModel );
        table.getTableHeader().setReorderingAllowed( false );
        table.setColumnSelectionAllowed( false );
        table.setRowSelectionAllowed( false );
        table.setCellSelectionEnabled( false );
        ParseTypeCellRenderer comboBox = new ParseTypeCellRenderer( parseTypes );
        table.setRowHeight( comboBox.getPreferredSize().height );
        TableColumn column = table.getColumnModel().getColumn( 1 );
        column.setCellRenderer( new DefaultTableCellRenderer() );
        column.setCellEditor( new DefaultCellEditor( new ParseTypeCellRenderer( parseTypes ) ) );

        Dimension d = table.getPreferredSize();
        d.height = Math.min( d.height, 50 );
        JScrollPane scroller = new JScrollPane( table );
        scroller.setPreferredSize( d );
        return scroller;
    }

    private ParserTypeTableModel createModel() {
        return new ParserTypeTableModel();
    }

    class ParseTypeCellRenderer extends JComboBox implements TableCellRenderer {
        ParseTypeCellRenderer( String[] items ) {
            super( items );
            ParseTypeCellRenderer.this.setRequestFocusEnabled( false );
            setEditable(false);
        }

        public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
            setSelectedItem( value );
            return this;
        }
    }

    class ParserTypeTableModel extends AbstractTableModel {
        private List<Entry> entries;
    
        ParserTypeTableModel() {
            Mode [] modes = jEdit.getModes();
            entries = new ArrayList<Entry>( modes.length );
            for ( Mode mode : modes ) {
                entries.add( new Entry( mode.getName() ) );
            }
            Collections.sort( entries );
        }
    
        public int getColumnCount() {
            return 2;
        }
    
        public int getRowCount() {
            return entries.size();
        }
    
        public Class getColumnClass( int col ) {
            switch ( col ) {
                case 0:
                case 1:
                    return String.class;
                default:
                    throw new InternalError();
            }
        }
    
        public Object getValueAt( int row, int col ) {
            Entry entry = ( Entry ) entries.get( row );
            switch ( col ) {
                case 0:
                    return entry.mode;
                case 1:
                    return parseTypes[entry.parseType];
                default:
                    throw new InternalError();
            }
        }
    
        public boolean isCellEditable( int row, int col ) {
            return col == 1;
        }
    
        public void setValueAt( Object value, int row, int col ) {
            if ( col == 0 ) {
                return ;
            }
    
            Entry entry = entries.get( row );
            switch ( col ) {
                case 1:
                    for (int i = 0; i < parseTypes.length; i++) {
                        if (parseTypes[i].equals(value)) {
                            entry.parseType = i;
                            break;
                        }
                    }
                    break;
                default:
                    throw new InternalError();
            }
    
            fireTableRowsUpdated( row, row );
        }
    
        public String getColumnName( int index ) {
            switch ( index ) {
                case 0:
                    return jEdit.getProperty( "options.tasklist.modes.modeColumnName", "Mode" );
                case 1:
                    return jEdit.getProperty( "options.tasklist.modes.parseTypeColumnName", "Parse type" );
                default:
                    throw new InternalError();
            }
        }
    
        public void save() {
            for ( Entry entry : entries ) {
                entry.save();
            }
        }
    
        class Entry implements Comparable<Entry> {
            String mode;
            int parseType = TaskListPlugin.COMMENT;
    
            Entry( String modeName ) {
                mode = modeName;
                parseType = jEdit.getIntegerProperty( "mode." + mode + ".tasklist.parseType", TaskListPlugin.COMMENT );
            }
            
            public String toString() {
                return "Entry[mode:" + mode + ", parseType:" + parseType + "]";   
            }
    
            void save() {
                // Comment is default type, no need to save it
                if ( parseType == TaskListPlugin.COMMENT) {  
                    jEdit.resetProperty( "mode." + mode + ".tasklist.parseType" );
                }
                else {
                    jEdit.setIntegerProperty( "mode." + mode + ".tasklist.parseType", parseType );
                }
            }
    
            public int compareTo( Entry a ) {
                return mode.compareToIgnoreCase( a.mode );
            }
        }
    }
}