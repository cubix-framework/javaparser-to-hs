/**
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
* JpyDbgCofigurationPanel.java
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
import javax.swing.event.* ; 
import javax.swing.border.* ; 
import java.awt.* ; 
import org.jymc.jpydebug.utils.* ; 
import org.jymc.jpydebug.* ; 
import java.io.* ;

/**
 *
 * @author jean-yves
 */
public class JpyDbgConfigurationPanel
extends BasicConfigurationPanel
{
   public final static String PYTHONPATH = "PYTHONPATH.txt" ;
   public final static String JYTHONPATH = "JYTHONPATH.txt" ;
  
   public  final static String TEMPDIR_PROPERTY = "jpydebug-dbgoptions.workdir" ;
   public final static String  TEMPDIR_PROPERTY_LABEL = "options.jpydebug-dbgoptions.workdir" ;
   private final static String _TEMPDIR_PROPERTY_LABEL_DEFAULT_ = "temporary work directory" ;
	
   public final static String BREAKPOINT_LINE_COLOR = "jpydebug-dbgoptions.breakpointlinecolor" ; 
   public final static String BREAKPOINT_LINE_COLOR_LABEL = "options.jpydebug-dbgoptions.breakpointlinecolor" ;
   private final static String _BREAKPOINT_LINE_COLOR_LABEL_DEFAULT_ = "breakpoint line color" ;

   public final static String CURRENT_DEBUG_LINE_LABEL = "options.jpydebug-dbgoptions.currentdebugline" ; 
   public final static String CURRENT_DEBUG_LINE_LABEL_DEFAULT  = "current debug line" ; 
   public final static String CURRENT_LINE_COLOR = "jpydebug-dbgoptions.currentLineColor" ;
   public final static String CURRENT_LINE_BORDER_COLOR = "jpydebug-dbgoptions.currentlinebordercolor" ;
    
   public final static String DEBUGGINGHOST_PROPERTY = "jpydebug-dbgoptions.hostname" ; 
   public final static String DEBUGGINGHOST_PROPERTY_LABEL = "options.jpydebug-dbgoptions.hostname" ; 
   private final static String _DEBUGGINGHOST_PROPERTY_LABEL_DEFAULT_ = "dbg server host" ; 

   public final static String JPYLOCATION_PROPERTY = "jpydebug-dbgoptions.jpydebug" ;
   public final static String JPYLOCATION_PROPERTY_LABEL = "options.jpydebug-dbgoptions.jpydebug" ;
   private final static String _JPYLOCATION_PROPERTY_LABEL_DEFAULT_ = "JpyDebug script path location" ;
	
   public final static String JPYARGS_PROPERTY = "jpydebug-dbgoptions.jpydebugargs" ; 
   public final static String JPYARGS_PROPERTY_LABEL = "options.jpydebug-dbgoptions.jpydebugargs" ; 
   private final static String _JPYARGS_PROPERTY_LABEL_DEFAULT_ = "jpy daemon script startup arguments" ; 

   public final static String CONNECTINGPORT_PROPERTY = "jpydebug-dbgoptions.connectingport" ; 
   public final static String CONNECTINGPORT_PROPERTY_LABEL = "options.jpydebug-dbgoptions.connectingport" ; 
   private final static String _CONNECTINGPORT_PROPERTY_LABEL_DEFAULT_ = "connecting port" ; 

   public final static String CALLBACKPORT_PROPERTY = "jpydebug-dbgoptions.callbackport" ; 
   public final static String CALLBACKPORT_PROPERTY_LABEL = "options.jpydebug-dbgoptions.callbackport" ; 
   private final static String _CALLBACKPORT_PROPERTY_LABEL_DEFAULT_ = "listening port" ; 

   public final static String AUTOCOMPLETION_PROPERTY = "jpydebug-dbgoptions.autocompletion" ; 
   public final static String AUTOCOMPLETION_PROPERTY_LABEL = "options.jpydebug-dbgoptions.autocompletion" ;
   private final static String _AUTOCOMPLETION_PROPERTY_LABEL_DEFAULT_ = "autocompletion" ;
	
   public final static String AUTOCOMPLETION_DELAY_PROPERTY = "jpydebug-dbgoptions.autocompletion.delay" ; 
   public final static String AUTOCOMPLETION_DELAY_PROPERTY_LABEL = "options.jpydebug-dbgoptions.autocompletion.delay" ;
   private final static String _AUTOCOMPLETION_DELAY_PROPERTY_LABEL_DEFAULT_ = "autocompletion delay" ;
	
   public final static String CODEPAGE_PROPERTY = "jpydebug-dbgoptions.codepage" ; 
   public final static String CODEPAGE_PROPERTY_LABEL = "options.jpydebug-dbgoptions.codepage" ;
   private final static String _CODEPAGE_PROPERTY_LABEL_DEFAULT_ = "remote page code" ;
	
   public final static String DEBUG_TRACE = "jpydebug-dbgoptions.debug" ; 
   public final static String DEBUG_TRACE_LABEL = "options.jpydebug-dbgoptions.debug" ;
   private final static String _DEBUG_TRACE_LABEL_DEFAULT_ = "activate debug traces" ;

   public final static String DEBUG_DYNAMIC_EVALUATION = "jpydebug-dbgoptions.dynamicevaluation" ; 
   public final static String DEBUG_DYNAMIC_EVALUATION_LABEL = "options.jpydebug-dbgoptions.dynamicevaluation" ;
   private final static String _DEBUG_DYNAMIC_EVALUATION_LABEL_DEFAULT_ = "Debug time editor mouse over expression evaluation" ;

   // remove starting in V0.0.014
   // private JTextField _tempDir   ;
   
   // remove starting in V0.0.014
   // private JTextField _jpyLocation    ;
   private JTextField _pyDaemonArgs    ;
   private JTextField _connectingPort   ;  
   private JTextField _callbackPort   ;  
	
   private JTextField  _debuggingHost  ; 
   private JTextField  _codePage ;
	
	
   private JCheckBox _trace  ; 
	
   private JCheckBox _autoCompletion ;
   private JTextField _autoCompletionDelay ;
	
   private JCheckBox _dynamicEvaluation ; 
	
   private final static Dimension _FILLER_ = new Dimension(0,5) ;

  class _DEBUGGER_PANE_
  extends JPanel
  {
	  
    public  _DEBUGGER_PANE_()
    {
      setLayout(new GridLayout(1,1)  )  ;
	    
      Box b = Box.createVerticalBox() ;
	    
      b.add (Box.createVerticalGlue() );

	 // remove in V0.0.014 for simplification purposes 
     // _jpyLocation = new JTextField(20) ; 
     // _jpyLocation.setText( JpyDbgConfigurationPanel.this.get_value( JPYLOCATION_PROPERTY , "" ) ) ;
     // JLabel jpyLocationLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(JPYLOCATION_PROPERTY_LABEL , _JPYLOCATION_PROPERTY_LABEL_DEFAULT_) ) ; 
     // b.add( new FileChooserPane( jpyLocationLabel , _jpyLocation , null ) ) ;
     //  b.add( Box.createRigidArea( _FILLER_ )) ; // filler


     _pyDaemonArgs = new JTextField(30) ; 
     _pyDaemonArgs.setText( JpyDbgConfigurationPanel.this.get_value( JPYARGS_PROPERTY , "" ) ) ;
     JLabel jpyArgLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(JPYARGS_PROPERTY_LABEL, _JPYARGS_PROPERTY_LABEL_DEFAULT_) ) ; 
     VerticalComponentPanel e1 = new VerticalComponentPanel( jpyArgLabel , _pyDaemonArgs ) ; 
     b.add( e1 ) ;
     b.add( Box.createRigidArea( _FILLER_ )) ; // filler

	 // remove in V0.0.014 for simplification purposes 
     //_tempDir = new JTextField(20);
     //_tempDir.setText(JpyDbgConfigurationPanel.this.get_value( TEMPDIR_PROPERTY, "./" ));
     //JLabel fileLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(TEMPDIR_PROPERTY_LABEL , _TEMPDIR_PROPERTY_LABEL_DEFAULT_) ) ;
     //b.add( new FileChooserPane( fileLabel , _tempDir , null ) ) ;
     //b.add( Box.createRigidArea( _FILLER_ )) ; // filler

     _callbackPort = new JTextField(5) ; 
     _callbackPort.setText( Integer.toString( JpyDbgConfigurationPanel.this.get_integerValue( CALLBACKPORT_PROPERTY , 29100 ) ) ) ;
     JLabel callbackPortLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(CALLBACKPORT_PROPERTY_LABEL,_CALLBACKPORT_PROPERTY_LABEL_DEFAULT_) ) ; 
     VerticalComponentPanel e2 = new VerticalComponentPanel( callbackPortLabel , _callbackPort ) ; 
     b.add(e2) ;
     b.add( Box.createRigidArea( _FILLER_ )) ; // filler

     _trace = new JCheckBox() ; 
     _trace.setSelected( JpyDbgConfigurationPanel.this.get_booleanValue( DEBUG_TRACE , false ) ) ;
     JLabel traceLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label( DEBUG_TRACE_LABEL , _DEBUG_TRACE_LABEL_DEFAULT_) ) ; 
     VerticalComponentPanel e3 = new VerticalComponentPanel( traceLabel , _trace ) ; 
     b.add(e3) ;
     b.add( Box.createRigidArea( _FILLER_ )) ; // filler

     _dynamicEvaluation = new JCheckBox() ; 
     _dynamicEvaluation.setSelected( JpyDbgConfigurationPanel.this.get_booleanValue( DEBUG_DYNAMIC_EVALUATION , false ) ) ;
     JLabel dynamicEvaluationLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(DEBUG_DYNAMIC_EVALUATION_LABEL , _DEBUG_DYNAMIC_EVALUATION_LABEL_DEFAULT_) ) ; 
     VerticalComponentPanel e4 = new VerticalComponentPanel( dynamicEvaluationLabel , _dynamicEvaluation ) ; 
     b.add(e4) ;
     b.add( Box.createRigidArea( _FILLER_ )) ; // filler

     b.add( Box.createVerticalGlue() ) ;
     b.setBorder( new TitledBorder("Local Python's environment description") ) ;
		
     add(b) ;
  }  
  }  
 
  class _REMOTING_PANE_
  extends JPanel
  {
    public _REMOTING_PANE_()
    {
      setLayout( new GridLayout(1,1)) ; 
	    
      Box b = Box.createVerticalBox() ;
	    
      b.add( Box.createVerticalGlue() ) ;

      _debuggingHost = new JTextField(20) ; 		
      _debuggingHost.setText( JpyDbgConfigurationPanel.this.get_value( DEBUGGINGHOST_PROPERTY, "localhost" ) ) ; 
      JLabel hostLabel = new JLabel(JpyDbgConfigurationPanel.this.get_label(DEBUGGINGHOST_PROPERTY_LABEL,_DEBUGGINGHOST_PROPERTY_LABEL_DEFAULT_)) ;
        
      VerticalComponentPanel e1 = new VerticalComponentPanel( hostLabel , _debuggingHost ) ; 
        
      b.add(e1) ; 
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      b.add( Box.createVerticalGlue() ) ;

      _connectingPort = new JTextField(5) ; 
      _connectingPort.setText( Integer.toString( JpyDbgConfigurationPanel.this.get_integerValue( CONNECTINGPORT_PROPERTY , -1) ) ) ;
      JLabel connectingPortLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(CONNECTINGPORT_PROPERTY_LABEL,_CONNECTINGPORT_PROPERTY_LABEL_DEFAULT_) ) ; 
      VerticalComponentPanel e2 = new VerticalComponentPanel( connectingPortLabel , _connectingPort ) ; 

      b.add(e2) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      b.add( Box.createVerticalGlue() ) ;
		
      _codePage = new JTextField(10) ; 
      _codePage.setText( JpyDbgConfigurationPanel.this.get_value( CODEPAGE_PROPERTY  , "" ) ) ;
      JLabel codepageLabel = new JLabel(JpyDbgConfigurationPanel.this.get_label(CODEPAGE_PROPERTY_LABEL,_CODEPAGE_PROPERTY_LABEL_DEFAULT_ ) ) ; 
      VerticalComponentPanel e3 = new VerticalComponentPanel( codepageLabel , _codePage ) ; 

      b.add(e3) ;
      b.add( Box.createRigidArea( _FILLER_ )) ; // filler

      b.add( Box.createVerticalGlue() ) ;
		
      b.setBorder( new TitledBorder("debugging host") ) ;
		
      add(b) ;
   }
		
  }
 
  
  class _COMPLETION_PANE_
  extends JPanel
  {
    public _COMPLETION_PANE_()	
    {
      _autoCompletion = new JCheckBox() ; 
      _autoCompletion.setSelected( JpyDbgConfigurationPanel.this.get_booleanValue( AUTOCOMPLETION_PROPERTY , false ) ) ;
      JLabel compLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(AUTOCOMPLETION_PROPERTY_LABEL, _AUTOCOMPLETION_PROPERTY_LABEL_DEFAULT_ ) ) ; 
      VerticalComponentPanel e1 = new VerticalComponentPanel( compLabel , _autoCompletion ) ; 
      super.add(e1) ;
      add( Box.createRigidArea( _FILLER_ )) ; // filler

      _autoCompletionDelay = new JTextField(5) ; 
      _autoCompletionDelay.setText( Integer.toString( JpyDbgConfigurationPanel.this.get_integerValue( AUTOCOMPLETION_DELAY_PROPERTY , 200 ) ) ) ;
      JLabel jpyLocationLabel = new JLabel( JpyDbgConfigurationPanel.this.get_label(AUTOCOMPLETION_DELAY_PROPERTY_LABEL , _AUTOCOMPLETION_DELAY_PROPERTY_LABEL_DEFAULT_) ) ; 
      VerticalComponentPanel e2 = new VerticalComponentPanel( jpyLocationLabel , _autoCompletionDelay ) ; 
      super.add(e2) ;

      setBorder( new TitledBorder("Python code completion facilities") ) ;
    }
  }
  
  public static String buildPythonPathFileName(boolean isJython )
  {
    
    StringBuffer wk = new StringBuffer( PythonDebugParameters.ideFront.getSettingsDirectory() ) ;
    
      wk.append( File.separatorChar ) ; 
      if ( isJython )
        wk.append(JYTHONPATH)  ;
      else 
        wk.append(PYTHONPATH) ;
    return wk.toString() ;
  }
  
  /**
  *  init option pane panel
  */
  public void doMyLayout( JPanel colorPane )
  {
    setLayout( new BorderLayout() ) ;	
    // populate coloring properties and setup configuration panes
    add( BorderLayout.NORTH ,  new _DEBUGGER_PANE_() ) ; 
    add( BorderLayout.CENTER,new _REMOTING_PANE_() ) ; 	
    JPanel deepSouth = new JPanel(new BorderLayout()) ; 
    deepSouth.add( BorderLayout.NORTH , colorPane )  ; 
    deepSouth.add( BorderLayout.SOUTH ,new _COMPLETION_PANE_() )  ; 
    add( BorderLayout.SOUTH, deepSouth ) ; 
    
    _debuggingHost.setText( super.get_value( DEBUGGINGHOST_PROPERTY , ""  ));
    // _jpyLocation.setText(super.get_value( JPYLOCATION_PROPERTY , "" ));
    _connectingPort.setText(Integer.toString( super.get_integerValue(CONNECTINGPORT_PROPERTY,-1 ))) ;
    _callbackPort.setText(Integer.toString( super.get_integerValue(CALLBACKPORT_PROPERTY,-1 ))) ;
    _pyDaemonArgs.setText(super.get_value( JPYARGS_PROPERTY , "") ) ;
    _autoCompletion.setSelected(super.get_booleanValue(AUTOCOMPLETION_PROPERTY,true)) ;
    _autoCompletionDelay.setText(Integer.toString( super.get_integerValue(AUTOCOMPLETION_DELAY_PROPERTY,200))) ;
    _trace.setSelected(super.get_booleanValue(DEBUG_TRACE , true)) ;
    _dynamicEvaluation.setSelected(super.get_booleanValue(DEBUG_DYNAMIC_EVALUATION,true)) ;
    _codePage.setText(super.get_value(CODEPAGE_PROPERTY, "")) ; 
    
    PythonDebugParameters.set_pyPathLocation(buildPythonPathFileName(PythonDebugParameters.get_jythonActivated())) ;   
    
  }
  
  /**
     populate from GUI fields to memory area
  */
  public void populateFields()
  {
    PythonDebugParameters.set_dbgHost( _debuggingHost.getText()) ;
    // PythonDebugParameters.set_jpydbgScript( _jpyLocation.getText())  ;
    PythonDebugParameters.set_connectingPort( checkNumericalField(_connectingPort)) ;
    PythonDebugParameters.set_listeningPort( checkNumericalField(_callbackPort) ) ;
    PythonDebugParameters.set_jpydbgScriptArgs( _pyDaemonArgs.getText()) ;
    PythonDebugParameters.set_autocompletion(_autoCompletion.isSelected()) ;
    PythonDebugParameters.set_autoCompletionDelay( checkNumericalField(_autoCompletionDelay)) ;
    PythonDebugParameters.set_debugTrace( _trace.isSelected()) ;
    PythonDebugParameters.set_debugDynamicEvaluation( _dynamicEvaluation.isSelected()) ;
    PythonDebugParameters.set_codePage( _codePage.getText()) ;  
  }
  
  private int checkNumericalField( JTextField field )
  {
  int converted = -1 ; 
    field.setForeground( Color.BLACK ) ;	
    try {
      converted = Integer.parseInt( field.getText() ) ;
    } catch( NumberFormatException e )
    {
      java.awt.Toolkit.getDefaultToolkit().beep();
      field.setForeground( Color.RED ) ;	
    }	   
    return converted ;	
  }

  /**
  * Creates a new instance of JpyDbgConfigurationPanel
  */
  public JpyDbgConfigurationPanel()
  {}
  
}
