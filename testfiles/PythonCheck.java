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

package org.jymc.jpydebug.jedit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.jymc.jpydebug.*;
import org.jymc.jpydebug.swing.ui.*;

import javax.swing.* ; 
import java.awt.* ; 
import java.io.File;
import java.util.* ; 


/**
 * Ckeck Python/Jython button pressed to check for Python/jython env
 * @author jean-yves
 *
 */
public class PythonCheck
implements ActionListener ,
           PythonDebugEventListener
{
  private final static String _SIMPLEPY_ = "simplepy.py" ; 
  private final static String _SIMPLEJY_ = "simplejy.py" ; 
  
  private boolean _isJython = false ; 
  private JButton _source = null ; 
  private StringBuffer _msgBuffer = new StringBuffer() ; 

  private void populateError( String errorMessage )
  {
    _source.setText( "FAILED :" + errorMessage ) ; 
    _source.setBackground(Color.RED) ;
    _source.invalidate() ; 
  }
  
  private void populateSuccess()
  {
    _source.setText( "Test SUCCEEDED : press here to check again"  ) ; 
    _source.setBackground(Color.GREEN) ;
    _source.invalidate() ; 
  }
  
  public  PythonCheck( boolean isJython )
  {
    _isJython = isJython ;
  }
  
  public void actionPerformed(ActionEvent e )
  {
    _source = (JButton) e.getSource() ;  
    System.out.println("entering Python check") ; 
    _msgBuffer = new StringBuffer() ; 
    String pgm = _SIMPLEPY_ ; 
    Vector jyArgs = null ; 
    Vector pyArgs = new Vector() ;
    String pythonPGM = null ; 
    if ( _isJython ) 
    {
      pgm = _SIMPLEJY_ ;
      jyArgs = PythonDebugParameters.buildJythonArgs(_isJython) ;  
      pythonPGM = PythonDebugParameters.get_jythonShellJvm () ; 
    }
    else 
      pythonPGM = PythonDebugParameters.get_pythonShellPath () ; 
      

    String testPath = PythonDebugParameters.get_jpydbgScript () ; 
    System.out.println("testPath =" + testPath ) ; 
    if ( ( testPath != null ) && ( testPath.length() > 0 ) )
    {
    File tmp = new File(testPath) ;
    testPath = tmp.getParent() + File.separator + pgm ;     
    }
    else 
    {
      populateError("Debugger's options MUST BE CONFIGURED first") ; 
      return ; 
    }
    pyArgs.addElement(testPath) ; 
   
    
    PythonInterpretor tester = new PythonInterpretor( pythonPGM , pyArgs , jyArgs ) ; 
    tester.addPythonDebugEventListener(this) ; 
    tester.doTheJob() ;
  }
  
  /** implemented to comply PythonDebugEventListener interface only */
  public void newDebugEvent( PythonDebugEvent e ){}

  /** capture script execution ending */
  public void launcherMessage( PythonDebugEvent e )
  {  
    synchronized(this)
    {
      switch ( e.get_type() )
      {
        case PythonDebugEvent.LAUNCHER_ENDING :
          // error running Test
          if ( ! e.get_msgContent().equals("0") )
          {
            populateError("execution failed with internal retcode =" + e.get_msgContent()  ) ; 
            JOptionPane.showMessageDialog ( _source , 
                                            _msgBuffer.toString () , 
                                            "Python Test Failure" ,
                                            JOptionPane.ERROR_MESSAGE ) ;
          }  
          else 
            populateSuccess() ;
             ; 
          break ;   

          case PythonDebugEvent.LAUNCHER_ERR :
            // populate launching error
            populateError("PYTHON lAUNCHING ERROR =" + e.get_msgContent() ) ;
            break ;   

          case PythonDebugEvent.LAUNCHER_MSG :
            //_stdout.append( e.get_msgContent() ) ; 
            // just log to Jedit log
            _msgBuffer.append (e.get_msgContent()) ; 
            _msgBuffer.append ('\n') ; 
            // System.out.println(e.get_msgContent()) ; 
            break ;   
                
          default :
            // we should not be there so just populate error if so 
            populateError("UNEXPECTED PYTHON LAUNCHING EVENT RECEIVED") ; 
          } 
        }
    }
}
