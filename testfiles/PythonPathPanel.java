/** 
* Copyright (C) 2003-2004 Jean-Yves Mengant
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

package org.jymc.jpydebug.jedit;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.* ; 
import javax.swing.table.*;

import org.jymc.jpydebug.*;

/**
 * Standard panel representation for Python Path Management
 * Use It WhereEver Needed 
 * @author jean-yves
 *
 */
public class PythonPathPanel
extends JPanel
{
  private JTable _pathElements  ;
  private PythonPath _path  = new PythonPath() ; 
  private _PATH_MODEL_ _lmodel = new _PATH_MODEL_(_path) ; 
  private JScrollPane _scroller = null ;
  
  public Vector getPath()
  { return _path.get_Path() ; }
  
  public void reloaded()
  { _lmodel.fireTableDataChanged() ; }
  
  public PythonPathPanel()
  {
    setLayout( new BorderLayout() ) ;
    _pathElements = new JTable(_lmodel) ;
    _scroller = new JScrollPane( _pathElements) ; 
    _pathElements.getColumnModel().getColumn(0).setCellRenderer(new _PATH_RENDERER_()) ; 
    _pathElements.addMouseListener(new _PATH_POPUP_(_pathElements,_lmodel)) ;
    add( BorderLayout.CENTER , _scroller ) ;  
  }
  
  public void readPath()
  { _path.readPath(PythonDebugParameters.get_jythonActivated()) ; }
  
  public void writePath()
  { _path.writePath(PythonDebugParameters.get_jythonActivated()) ; }

  public String locatePythonSource( String source )
  {
  PythonPathElement curP = _path.locatePythonSource(source) ;  
    if ( curP != null ) 
    {
      // select the associated current table index  
      int row = _lmodel.setSelectedPath(curP) ;
      // and position path on it
      JViewport vp = _scroller.getViewport() ; 
      Rectangle rect = _pathElements.getCellRect(row,0,true) ; 
      vp.setViewPosition(new Point(rect.x,rect.y) ) ;
      _pathElements.revalidate() ;
      _pathElements.repaint() ; 
      return curP.get_candidate()  ;
    }
    // file may exist without matching path
    File f = new File(source) ; 
    if ( f.isFile() )
      return source ; 
    return null ; 
  }

  class _PATH_POPUP_
  extends MouseAdapter
  {
      private JPopupMenu _popup = null ; 
      private JTable _parent = null ; 
      private _PATH_MODEL_ _tModel = null ; 
      
    public _PATH_POPUP_(JTable parent , _PATH_MODEL_ model )
    { 
      _parent = parent ; 
      _tModel = model ; 
    } 
      
      private void createPopup(MouseEvent e)
      {
        // if no selection line triggerred return
        if ( _parent.getSelectedRow() == -1)
          return ; 
          
        if ( e.isPopupTrigger() )
        {
        _popup = new JPopupMenu() ; 
          JMenuItem mi = _popup.add(new JMenuItem("insert new path line")) ;      
          mi.addActionListener(new ActionListener()
              {  public void actionPerformed( ActionEvent ev )
                 {
                  int sel = _parent.getSelectedRow() ;
                    if ( sel == - 1)
                      _tModel.insert(0) ; 
                    else
                      _tModel.insert(sel) ; 
                 }
              } 
            )  ; 
          JMenuItem mi1 = _popup.add(new JMenuItem("delete selected line")) ;     
          mi1.addActionListener(new ActionListener()
              {  public void actionPerformed( ActionEvent ev )
                {
                int sel = _parent.getSelectedRow() ;
                  if ( sel != -1 )
                    _tModel.remove(sel) ; 
                }
              } 
            )  ; 
          JMenuItem mi2 = _popup.add(new JMenuItem("save PYTHONPATH")) ;      
          mi2.addActionListener(new ActionListener()
              { public void actionPerformed( ActionEvent ev )
                { PythonPathPanel.this.writePath() ; }
              } 
            )  ; 
          _popup.show(_parent , e.getX(), e.getY() ) ;                    
        }
      }
      public void mouseReleased( MouseEvent e )
      { createPopup(e) ; }
      public void mousePressed( MouseEvent e )
      { createPopup(e) ;}
      public void mouseClicked( MouseEvent e )
      { createPopup(e) ;}
  }

  class _PATH_RENDERER_
  extends DefaultTableCellRenderer
  {
    
    
    
    public Component getTableCellRendererComponent( JTable table ,
                                                    Object value ,
                                                    boolean isSelected ,
                                                    boolean isFocused , 
                                                    int row ,
                                                    int column 
                                                   )
    {
    Component component = super.getTableCellRendererComponent(table,value,isSelected,isFocused,row,column) ;
      if ( value instanceof PythonPathElement )
      {  
      PythonPathElement current = (PythonPathElement)value ; 
        if ( current != null )
        {
          if ( current.is_curResolver())
          {  
            super.setBackground(Color.darkGray) ;
            super.setForeground(Color.lightGray) ;
          }
          else
          {
            super.setBackground(Color.white) ;
            super.setForeground(Color.blue) ;
          }
        }
      }  
      return component ;
    }
  }
  
}

class _PATH_MODEL_
extends AbstractTableModel
{
  private PythonPathElement _EMPTY_ = new PythonPathElement("") ; 
  private String [] _colNames = { " PYTHONPATH " } ;
  private PythonPathElement _selectedPath = null ;
  private PythonPath _path  ; 
  
  public _PATH_MODEL_( PythonPath path )
  { _path = path ; }
  
  public int getRowCount()
  { return _path.size() ; }
  
  public int getColumnCount()
  { return _colNames.length ; }
  
  public Object getValueAt(int row , int col )
  { return _path.elementAt(row) ; }
  
  public String getColumnName( int colIndex )
  { return _colNames[colIndex] ; }
  
  public void setValueAt ( Object value , int row , int col  )
  { 
    PythonPathElement content ;
    if ( value instanceof String ) 
      content = new PythonPathElement((String)value) ; 
    else   
      content = (PythonPathElement) value ; 
    String strVal = content.get_value() ;
  
    if ( strVal.trim().length() == 0  )
    {
      if ( _path.size() > 1)
        _path.removeElementAt(row) ;
    }  
    else if ( row >= getRowCount() )
      _path.addElement(content) ;
    else 
    {  
      _path.setElementAt(content , row) ; 
      if ( row+1 == _path.size())
        _path.addElement(_EMPTY_) ;
        
    }  
  }
  
  public boolean isCellEditable( int row , int col )
  { return true ;  }
  
  public Enumeration get_lines()
  { return _path.get_lines() ; }
  
  public int setSelectedPath(PythonPathElement selected)
  {
    if ( _selectedPath != null )
      // unselect previous
      _selectedPath.set_curResolver(false) ; 
    // set the new one
    selected.set_curResolver(true) ; 
    // and store it
    _selectedPath = selected ;
    super.fireTableDataChanged();
    return _path.indexOf(selected) ;
  }
  
  public void remove( int line )
  { _path.removeElementAt(line) ; }
  
  public void insert( int after )
  {
    _path.insertElementAt(_EMPTY_ , after ) ;   
    super.fireTableDataChanged();
  }
  
}







