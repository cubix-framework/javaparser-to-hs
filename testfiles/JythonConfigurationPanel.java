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
public class JythonConfigurationPanel
extends BasicConfigurationPanel
{
    public final static String LOCALJYTHONJVM_PROPERTY_LABEL = "options.jpydebug-dbgoptions.jythonjvm" ; 
    public final static String LOCALJYTHONJVM_PROPERTY = "jpydebug-dbgoptions.jythonjvm" ; 
    private final static String _LOCALJYTHONJVM_PROPERTY_LABEL_DEFAULT_ = "Jython Jvm location(java.exe file)" ; 

    public final static String JYTHON_ENVIRONMENT = "jpydebug-dbgoptions.jython" ; 
    public final static String JYTHON_ENVIRONMENT_LABEL = "options.jpydebug-dbgoptions.jython" ;
    private final static String _JYTHON_ENVIRONMENT_LABEL_DEFAULT_ = "use Jython as default Python Debug environment" ;
    

    public final static String JYTHON_HOME = "jpydebug-dbgoptions.jythonhome" ; 
    public final static String JYTHON_HOME_LABEL = "options.jpydebug-dbgoptions.jythonhome" ;
    private final static String _JYTHON_HOME_LABEL_DEFAULT_ = "Jython Home Path" ;

    private JTextField _jythonHome ; 
    private JTextField _localJythonJvm  ; 
    private JCheckBox _jythonEnvironment ;
    private PythonPathTreePanel _pythonPathPanel = new PythonPathTreePanel();
    private JButton _checker = new JButton("Press here to check your Jython configuration"); 

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
      setLayout( new BorderLayout()) ;
      Box b = Box.createVerticalBox() ;

      b.add( Box.createVerticalGlue() ) ;
        
      _localJythonJvm = new JTextField(20) ; 
      _localJythonJvm.setText( super.get_value( LOCALJYTHONJVM_PROPERTY , "" ) ) ;
      JLabel jJyLocationLabel = new JLabel( super.get_label(LOCALJYTHONJVM_PROPERTY_LABEL , _LOCALJYTHONJVM_PROPERTY_LABEL_DEFAULT_) ) ; 
      b.add( new FileChooserPane( jJyLocationLabel , _localJythonJvm , null ) ) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      _jythonHome = new JTextField(20) ; 
      _jythonHome.setText( super.get_value( JYTHON_HOME , "" ) ) ;
      JLabel jythonHomeLabel = new JLabel( super.get_label(JYTHON_HOME_LABEL , _JYTHON_HOME_LABEL_DEFAULT_ ) ) ; 
      FileChooserPane chooser = new FileChooserPane( jythonHomeLabel , _jythonHome , null ) ; 
      chooser.chooseDirectories(true) ; 
      b.add( chooser ) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      _jythonEnvironment = new JCheckBox() ; 
      _jythonEnvironment.setSelected( super.get_booleanValue( JYTHON_ENVIRONMENT , false ) ) ;
      JLabel jythonLabel = new JLabel( super.get_label(JYTHON_ENVIRONMENT_LABEL , _JYTHON_ENVIRONMENT_LABEL_DEFAULT_) ) ; 
      _PANE_VERTICAL_ELEMENT_ e3 = new _PANE_VERTICAL_ELEMENT_( jythonLabel , _jythonEnvironment ) ; 
      b.add(e3) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler
      b.setBorder( new TitledBorder("JAVA PYTHON(Jython) context properties") ) ;
        
      _pythonPathPanel.init(true) ;
        
      _checker.addActionListener( action ) ; 

      add(BorderLayout.NORTH, b);
      add(BorderLayout.CENTER , _pythonPathPanel) ;
      add(BorderLayout.SOUTH , _checker ) ; 
             
    }
    
    /**
       populate fields values to memory area
    */ 
    public void populateFields()
    {
      PythonDebugParameters.set_jythonHome(_jythonHome.getText() ) ;
      PythonDebugParameters.set_jythonShellJvm( _localJythonJvm.getText()) ;
      PythonDebugParameters.set_jythonActivated( _jythonEnvironment.isSelected()) ; 
    }
  
    public PythonPathTreePanel get_pythonPathPanel()
    { return _pythonPathPanel ; }
    
    public JythonConfigurationPanel()
    {}   
}
