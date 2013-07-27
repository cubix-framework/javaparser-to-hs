/*
 * JpyArgsConfigurationPanel.java
 *
 * Created on December 28, 2005, 1:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jymc.jpydebug.swing.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import org.jymc.jpydebug.* ; 

/**
 * Portable cross IDE configuration panel for Python programs arguments
 *
 * @author jean-yves
 */
public class JpyArgsConfigurationPanel
extends BasicConfigurationPanel
{

  private JTable _table ; 
  private _PROPERTIESTABLEMODEL_ _model;
  private Hashtable _arguments ; 


	class _PROPERTIESTABLEMODEL_ 
	extends AbstractTableModel
	{

		private String[] columnNames;
		private Vector names;
		private Vector values;


		public _PROPERTIESTABLEMODEL_()
		{
			columnNames = new String[2];
			columnNames[0] = "configuration name";
			columnNames[1] = "Program Argument Value";

			names = new Vector( 10 );
			values = new Vector( 10 );

			String name = "";
			Vector keysv = PythonDebuggingProps.getConfigurations() ; 
			Enumeration keys = keysv.elements() ;			
			while ( keys.hasMoreElements() ) 
			{
			  name = (String) keys.nextElement();
			  names.addElement( name );
			  values.addElement( PythonDebuggingProps.getConfigurationProperty( name ) );
			}
			names.addElement( "" );
			values.addElement( "" );
		}


		public void setValueAt( Object value, int row, int col )
		{
			if ( ( (String) value ).length() == 0 ) {
				names.removeElementAt( row );
				values.removeElementAt( row );
				fireTableRowsDeleted( row, row );
				if ( names.size() < 1 ) {
					insertBlankRow( row );
				}
			}
			else {
				if ( col == 0 ) {
					names.setElementAt( value, row );
				}
				else {
					values.setElementAt( value, row );
				}
				if ( row == names.size() - 1 ) {
					insertBlankRow( row );
				}
			}

			fireTableCellUpdated( row, col );
		}


		public int getColumnCount()
		{
			return columnNames.length;
		}


		public int getRowCount()
		{
			return names.size();
		}


		public String getColumnName( int col )
		{
			return columnNames[col];
		}

        public Hashtable get_arguments()
        { return _arguments ; }

		public Object getValueAt( int row, int col )
		{

			if ( row >= names.size() || row >= names.size() )
				return null;

			if ( col == 0 ) {
				return names.elementAt( row );
			}
			return values.elementAt( row );
		}


		/*
		 *  JTable uses this method to determine the default renderer/
		 *  editor for each cell.  If we didn't implement this method,
		 *  then the last column would contain text ("true"/"false"),
		 *  rather than a check box.
		 */
		public Class getColumnClass( int c )
		{
			return getValueAt( 0, c ).getClass();
		}


		/*
		 *  Don't need to implement this method unless your table's
		 *  editable.
		 */
		public boolean isCellEditable( int row, int col )
		{
			return true;
		}


		private void insertBlankRow( int row )
		{
			names.addElement( "" );
			values.addElement( "" );
			fireTableRowsInserted( row + 1, row + 1 );
		}
	}
    
	private void setUpNameColumn( TableColumn column )
	{
	  column.setPreferredWidth( 75 );
	}


	private void setUpValueColumn( TableColumn column )
	{
	  column.setPreferredWidth( 75 );
	}
	
	private void saveProperties()
	{
	int counter = _table.getRowHeight();
	String name = "";
	String value = "";
	  for ( int i = 0; i <= counter; i++ ) 
	  {
		name = (String) _table.getValueAt( i, 0 );
		value = (String)_table.getValueAt( i, 1 );
		if ( name == null || value == null )
		  continue;
		PythonDebuggingProps.setConfigurationProperty( name, value );
	  }
	}
	
	private void cleanup()
	{
	Vector keysv = PythonDebuggingProps.getConfigurations() ; 
	Enumeration keys = keysv.elements() ;			
	String name = "";
	  while ( keys.hasMoreElements() ) 
	  {
		name = (String) keys.nextElement();
		PythonDebuggingProps.removeConfigurationProperty( name );
	  }
	}

    

    /** Creates a new instance of JpyArgsConfigurationPanel */
    public JpyArgsConfigurationPanel ()
    {
	  setLayout(new BorderLayout(0,6))  ;
	  // fetch configuration properties 
		
	  //Table building
	  _table = new JTable();
         _model = new _PROPERTIESTABLEMODEL_();
	  
	  _table.setModel(_model);
	  setUpNameColumn( _table.getColumnModel().getColumn( 0 ) );
	  setUpValueColumn( _table.getColumnModel().getColumn( 1 ) );
	  _table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

	  JScrollPane scrollPane = new JScrollPane(_table) ;  
	  add(BorderLayout.CENTER ,scrollPane);
	  setBorder( new TitledBorder("Run time initial arguments configurations") ) ;  
    }
    
    /** save argument configuration to file */
    public void save()
    throws PythonDebugException
    {
	  cleanup() ; // cleanup previous property content first	
	  saveProperties() ;	
          PythonDebuggingProps.save() ; 
    }
    
}    

  


