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

import javax.swing.* ;
import javax.swing.border.* ;

import org.jymc.jpydebug.PythonDebugClient;
import org.jymc.jpydebug.PythonDebugEvent;
import org.jymc.jpydebug.PythonDebugEventListener;
import org.jymc.jpydebug.PythonDebugException;

import java.awt.*    ; 
import java.awt.event.*;

/**
 * @author jean-yves
 *
 * This class implement a very basic rough non GUI interface to the          
 * python debugger deamon => IT IS ONLY USEFULL FOR BASIC DEBUGGING     
 * OF THE DEBUGGER'S SEMANTIC itself                      
 */
public class ClientDebuggerShell
extends Thread
{
  private final static String _COMPONENT_NAME_ = "Debug Rough Command Interface(DRCI) " ;
  private final static String _END_OF_LINE_ = "\n" ; 

  private PythonDebugClient _dbgCli = null ; 
  private _MESSAGE_LIST_ _msgList = new _MESSAGE_LIST_() ; 
  private JTextField  _command = new JTextField(50)       ; 
  private JButton _dbgSend = new JButton( "send") ;
  private _MAIN_PANEL_ _mainPanel = new _MAIN_PANEL_() ; 

  private String _lastCommand = null ;
  private String _args[]  = null ; 

  public JPanel get_MainPanel() 
  { return _mainPanel ; }

  public String get_lastCommand()
  { return _lastCommand ; }
  
  class _SEND_COMMAND_
  implements ActionListener
  {
  	private String parseCommand( String in )
  	{
  	StringBuffer returned = new StringBuffer(in) ; 
  	int ii = 0 ; 
  	  while ( ii < returned.length() )
  	  {
  	    if ( returned.charAt(ii) == '\\' ) 
  	    {
  	      returned.deleteCharAt(ii) ;
  	      switch ( returned.charAt(ii) ) 
  	      {
			case 'n' :
			  returned.setCharAt( ii , '\n' ) ; 	
			case 'r' :
			  returned.setCharAt( ii , '\n' ) ; 	
  	      }
  	    } 
  	    ii++ ; 	
  	  }
  	  return returned.toString() ; 	
  	}
  	
    public void actionPerformed( ActionEvent e )
    {
      try {  
        _dbgCli.sendCommand( parseCommand(_command.getText()) + _END_OF_LINE_) ; 
      } catch ( PythonDebugException ex )
      { _msgList.addMessage( "SendCommand Exception occured : " + ex.getMessage() ) ; }
    }    
  }

  class _COMMAND_PANEL_ 
  extends JPanel
  {
    
    public _COMMAND_PANEL_()
    {
    JLabel text = new JLabel(" command : ") ;     
      setLayout( new FlowLayout() ) ;
      _command.setEnabled(false) ; 
      _dbgSend.setEnabled(false) ; 
      add( text ) ; 
      add(_command) ; 
      add(_dbgSend) ; 
      _dbgSend.addActionListener( new _SEND_COMMAND_() ) ; 
      super.setBorder( new TitledBorder("Python Dbg commands" ) ) ;      
    }
  }

  class _MESSAGE_LIST_ 
  extends JList
  implements PythonDebugEventListener 
  {
    private DefaultListModel _model = new DefaultListModel() ;
    
    public void addMessage( String message )
    { 
      _model.addElement( message ) ; 
      super.invalidate() ; 
    }
    
	public void newDebugEvent ( PythonDebugEvent e )
	{addMessage( e.toString() ) ; }

	public void launcherMessage ( PythonDebugEvent e )
	{addMessage( e.toString() ) ; }

    public _MESSAGE_LIST_()
    {
      super.setModel(_model) ;       
    }   
  }

  class _MAIN_PANEL_ 
  extends JPanel
  {
    public _MAIN_PANEL_()
    {
      setLayout( new BorderLayout() ) ; 
      add( BorderLayout.NORTH , new _COMMAND_PANEL_() ) ; 
      _msgList.setBorder( new TitledBorder("Python Dbg messages" ) ) ; 
      add( BorderLayout.CENTER , new JScrollPane(_msgList ) ) ;     
    }    
  }

  private String getComponentWelcomeString()
  {
    return ( _COMPONENT_NAME_ + PythonDebugClient.VERSION ) ;
  }

  public void set_args( String args[] ) 
  { _args = args ; }

  /**
    get a user's input command line ( used  by simpleTester )
  */
  /* REPORTED AS NOT USED LOCALLY
  private String getLine()
  throws PythonDebugException
  {
  StringBuffer buf = new StringBuffer(80) ;
  int c ;
    try
    {
      while( ( c = System.in.read() ) != -1 )
      {
      char ch = (char) c ;
        if ( ch == '\n' )
          break ;
        buf.append(ch) ;
      }
      return buf.toString() ;
    } catch ( IOException e )
    { throw new PythonDebugException("KBD read error : " + e.getMessage() ) ; }
  }
  */
  
  public void run()
  {
  String host = null ; 
  int port    = PythonDebugClient.DEFAULTPORT ; 
    if ( _args.length > 1)
      host = _args[1] ; 
    if ( _args.length > 2 )
    {
      try {
        port = Integer.parseInt(_args[2]) ;     
      } catch ( NumberFormatException e )
      { _msgList.addMessage( "invalid port : " + _args[2] + " ignored") ;}        
    }
           
    _msgList.addMessage( getComponentWelcomeString() ) ; 
    if ( host != null )
      _msgList.addMessage("connecting to python debug server...") ; 
    else 
      _msgList.addMessage("waiting for JpyDeamon incoming connection...") ; 
        
    _dbgCli = new PythonDebugClient() ; 
    _dbgCli.setPythonDebugEventListener(_msgList) ; 
    try {
      _dbgCli.init( host , port , -1 , "None" , null , null , null , null ,null) ; 
      _msgList.addMessage("Connected and ready") ; 
      _dbgSend.setEnabled(true) ; 
      _command.setEnabled(true) ; 
    }
    catch ( PythonDebugException e )
    { _msgList.addMessage( e.getMessage() ) ; }  
  }


  public void terminate()
  throws PythonDebugException
  {
    if ( _dbgCli != null )
      _dbgCli.terminate() ; 
    _dbgCli = null ;      
  }

  public static void main(String[] args)
  {
  final ClientDebuggerShell dbg = new ClientDebuggerShell() ; 
  JFrame myFrame = new JFrame(  _COMPONENT_NAME_ + PythonDebugClient.VERSION ) ; 
  myFrame.addWindowListener( 
	  new WindowAdapter() 
	  {  public void windowClosing( WindowEvent e ) 
		 { 
		   try { dbg.terminate() ; } catch ( Exception ex ) { ex.printStackTrace() ; } ; 
		   System.exit(0) ;
		 }  
	  }        
	) ; 
	dbg.set_args(args) ;   
	dbg.start() ;     
	myFrame.getContentPane().setLayout( new BorderLayout() )  ; 
	myFrame.getContentPane().add( BorderLayout.CENTER , dbg.get_MainPanel() ) ;  
	myFrame.pack() ; 
	myFrame.setVisible(true) ; 
  }
}
