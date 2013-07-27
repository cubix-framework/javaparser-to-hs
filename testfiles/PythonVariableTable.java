/**
* Copyright (C) 2003 Jean-Yves Mengant
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


package org.jymc.jpydebug.swing.ui;

import java.util.* ; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.* ;
import javax.swing.table.*;

 
/**
 * @author jean-yves
 *
 * JTable container for Python Variable display
 *
 */
public class PythonVariableTable
extends JPanel
{
  private JTable _table ; 
  private _HASHTABLEMODEL_ _model;
  private PythonDebugContainer _parent ; 
  /** true when table conatins global variables references */
  private boolean _global = false ;

  public PythonVariableTable( boolean global )
  {
	super(new BorderLayout());
	//Table building
	_table = new JTable();
	_model = new _HASHTABLEMODEL_(_table , new String[]{" Variable name "," Value "});
	_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	_table.setModel(_model);
	_global = global ; 

	this.add(new JScrollPane(_table), BorderLayout.CENTER );
	
  }	
  
  public void set_parent( PythonDebugContainer parent )
  { _parent = parent ; } 

  public void set_tableValue( Hashtable values )
  {
    _model.setNewValues(values) ; 	
  }

  
  
  
   public class _HASHTABLEMODEL_
   extends DefaultTableModel
   {
	 private String[] _columnNames = null ;
  
	 private Vector _data = null  ; 
	 private Hashtable _hashValue ; 
	 private _ROW_SIZER_ _rSizer  ;
  
  	
	 class _ROW_SIZER_ 
	 {
	   private JTable _t ;
	   private DefaultTableColumnModel _cm ;

	   public _ROW_SIZER_( JTable t )
	   {
		 _t = t ;
		 _cm = (DefaultTableColumnModel)_t.getColumnModel() ;
	   }

	   private int getHeaderWidth( TableColumn column )
	   {
	   JTableHeader h = _t.getTableHeader()  ;
	   TableCellRenderer hr = h.getDefaultRenderer() ;
	   Component c =  hr.getTableCellRendererComponent( _t ,
														column.getHeaderValue() ,
														false , false , 0 , 0 ) ;
		 return c.getPreferredSize().width + 20 ;
	   }

	   public void resize( int col )
	   {
	   TableColumn column = _cm.getColumn(col) ;
	   int width = 0 ;
	   int maxw = getHeaderWidth(column)  ;

		 for ( int ii = 0 ; ii < _t.getRowCount() ;  ii++ )
		 {
		 TableCellRenderer r = _t.getCellRenderer(ii,col) ;
		 Component c = r.getTableCellRendererComponent( _t ,
														_t.getValueAt(ii,col) ,
														false , false ,
														ii , col
													  ) ;
			width = c.getPreferredSize().width + 20;
			maxw = width > maxw ? width : maxw ;
		 }
		 if ( maxw > 0 )
		 {
		   // setting preferred has few impact on column resizing
		   column.setPreferredWidth(maxw) ;
		   column.setMinWidth(maxw) ;
		   //column.setMaxWidth(maxw) ;
		   column.setWidth(maxw);
		   adjustColumn2(maxw);
		 }
	   }
	
	   private void adjustColumn2(int col1Width) 
	   {
		 TableColumn column2 = _cm.getColumn(1) ;
		 int size = _t.getParent().getWidth()- col1Width;
		 column2.setPreferredWidth(size);
		 column2.setWidth(size);
	   }
	 }
  
	 public _HASHTABLEMODEL_( JTable table , String columnNames[] ) 
	 {	
	   _rSizer = new _ROW_SIZER_(table) ; 
	   _columnNames = columnNames       ; 
	 }
	 
	 public int getColumnCount()
	 { return _columnNames.length ; }
    
	 public int getRowCount()
	 {
	   if ( _data == null )
		 return 0 ;
	   return _data.size() ;   	
	 }
    
	 public Object getValueAt( int r , int c ) 
	 {
	   if ( _data != null )
	   {
	   Vector row = (Vector) _data.elementAt(r) ;
		 return row.elementAt(c) ; 
	   }  
	   return null ;  	 
	 }	
    
	 public void setValueAt( Object newValue , int row , int column )
	 {
	 Vector rowData = (Vector) _data.elementAt(row) ; 
	   if ( rowData == null )
	   {
		 rowData = new Vector() ; 
		 _data.addElement(rowData) ; 
	   }
	   Object key = rowData.elementAt(0)  ; 
	   rowData.setElementAt(newValue,1)   ; 
	   if ( (key != null ) && ( newValue != null ) )
	   {
		 _hashValue.put( key , newValue ) ; 
		 // populate newDataValue to python side
		 if ( _parent != null )
		   _parent.dbgVariableChanged((String)key , (String)newValue ,_global ) ;  
	   }	 
	 }
      
	 public void setNewValues( Hashtable newValues )
	 {
	   _hashValue = newValues ; 	
	   Enumeration keys = newValues.keys() ; 
	   _data = new Vector() ; 
	   while ( keys.hasMoreElements() )
	   {
	   Vector row = new Vector() ; 
	   Object key = keys.nextElement() ; 	
		 row.addElement( key ) ; 
		 row.addElement( newValues.get(key) ) ;	
		 _data.addElement(row) ; 
	   }
      
	   final _ROW_SIZER_ _rs = _rSizer;
	   SwingUtilities.invokeLater(new Runnable(){
	   public void run() {
		   _rs.resize(0);
		   _rs.resize(1);
		 }
	   });

	   super.fireTableDataChanged() ;
        	
	 }
    
	 public String getColumnName( int c )
	 { return _columnNames[c] ; }
   }
  
   public static void main(String[] args)
   {
   final PythonVariableTable dbg = new PythonVariableTable(false) ; 
   JFrame myFrame = new JFrame(  "simple test" ) ; 
   Hashtable testH = new Hashtable() ;
	 testH.put("nom1" , "Valeur3") ; 
	 testH.put("nom3" , "Valeur4") ; 
   
	 dbg.set_tableValue(testH) ; 
     
	 myFrame.addWindowListener( 
		 new WindowAdapter() 
		 {  public void windowClosing( WindowEvent e ) 
			{ 
			  System.exit(0) ;
			}  
		 }        
	   ) ; 
	 myFrame.getContentPane().setLayout( new BorderLayout() )  ; 
	 myFrame.getContentPane().add( BorderLayout.CENTER , dbg ) ;  
	 myFrame.pack() ; 
	 myFrame.setVisible(true) ; 
   }
 

}
