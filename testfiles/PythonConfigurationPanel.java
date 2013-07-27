/**
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
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

import javax.swing.* ; 
import javax.swing.border.* ;
import java.awt.* ; 
import java.awt.event.* ; 
import org.jymc.jpydebug.utils.* ;
import org.jymc.jpydebug.* ;

/**
 *
 * @author jean-yves
 */
public class PythonConfigurationPanel
extends BasicConfigurationPanel
{
    public final static String LOCALPYTHON_PROPERTY_LABEL = "options.jpydebug-dbgoptions.pythoninterpreter" ; 
    private final static String _LOCALPYTHON_PROPERTY_LABEL_DEFAULT_ = "Python interpreter location" ; 
    public final static String LOCALPYTHON_PROPERTY = "jpydebug-dbgoptions.pythoninterpreter" ; 


    private JTextField _localPython    ; 
    private PythonPathTreePanel _pythonPathPanel = new PythonPathTreePanel();
    private JButton _checker  = new JButton("Press here to check your CPython configuration"); 
    
    private final static Dimension _FILLER_ = new Dimension(0,5) ;
  
    class _PANE_VERTICAL_ELEMENT_
    extends JPanel 
    {
      public _PANE_VERTICAL_ELEMENT_( JComponent label , JComponent field )
      {
        setLayout ( new FlowLayout( FlowLayout.LEFT ,5,5) ) ;   
        add(label) ; 
        add(field) ; 
      } 
    }
    
    /**
       layout panel components
    */
    public void doMyLayout( ActionListener action )
    {
      // setLayout( new GridLayout(1,1)) ;
      setLayout( new BorderLayout() ) ; 
      Box b = Box.createVerticalBox() ;
        
      b.add( Box.createVerticalGlue()) ;

      _localPython = new JTextField(20) ; 
      _localPython.setText( super.get_value( LOCALPYTHON_PROPERTY , "" ) ) ;
      JLabel pythonLabel = new JLabel( super.get_label( LOCALPYTHON_PROPERTY_LABEL , _LOCALPYTHON_PROPERTY_LABEL_DEFAULT_) ) ; 
      b.add( new FileChooserPane( pythonLabel , _localPython , null ) ) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      b.add( Box.createVerticalGlue()) ;
       
      b.setBorder( new TitledBorder("C PYTHON context properties") ) ;
        
      _pythonPathPanel.init(false) ;
        
      _checker.addActionListener( action ) ;
        
      add(BorderLayout.NORTH,b) ;
      add(BorderLayout.CENTER,_pythonPathPanel) ;
      add(BorderLayout.SOUTH , _checker ) ; 
    }
    
    public void populateFields()
    {
      PythonDebugParameters.set_pythonShellPath(_localPython.getText()) ;
    }
  
    public PythonPathTreePanel get_pythonPathPanel()
    { return _pythonPathPanel ; }
    
    public PythonConfigurationPanel()
    {}   
}
