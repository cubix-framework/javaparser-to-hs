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
* along with this program; if not, write to the Free Software_dbgtool
*
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/


package org.jymc.jpydebug.swing.ui;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.*;

import org.jymc.jpydebug.*;
import org.jymc.jpydebug.jedit.FtpBuffers;
import org.jymc.jpydebug.jedit.ImportNavigator;
import org.jymc.jpydebug.utils.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * @author jean-yves Main Python container Panel class
 */

public class PythonDebugContainer
implements PythonContainer
{
  public final static String VERSION  = "JpyDbg 0.0.18-005";
  public final static int    INACTIVE = 0;
  public final static int    STARTING = 1;
  public final static int    STARTED  = 2;

  private final static String _END_OF_LINE_   = "\n";
  private final static String _INACTIVE_TEXT_ = "inactive";
  private final static String _READY_TEXT_    = "ready";
  private final static String _BUSY_TEXT_     = "busy";
  private final static String _STRING_        = "<string>";
  private final static String _EOL_           = "/EOL/";
  private final static String _OK_            = "OK";

  private final static String _COMMAND_   = "CMD ";
  private final static String _BPSET_     = "BP+ ";
  private final static String _BPCLEAR_   = "BP- ";
  private final static String _BPCLEARALLINFILE_   = "BPF-- ";
  private final static String _DBG_       = "DBG ";
  private final static String _SETARGS_   = "SETARGS ";
  private final static String _READSRC_   = "READSRC ";
  private final static String _NEXT_      = "NEXT ";
  private final static String _STEP_      = "STEP ";
  private final static String _RUN_       = "RUN ";
  private final static String _STOP_      = "STOP ";
  private final static String _STACK_     = "STACK ";
  private final static String _THREAD_    = "THREAD ";
  private final static String _GLOBALS_   = "GLOBALS ";
  private final static String _EQUAL_     = "=";
  private final static String _SEMICOLON_ = ";";
  private final static String _SILENT_    = "silent";
  private final static String _LOCALS_    = "LOCALS ";
  private final static String _COMPOSITE_ = "COMPOSITE ";
  private final static String _DATA_      = "DATA ";
  private final static String _SPACE_     = " ";
  
  private final static String _PYTHREADS_ = "Python Threads" ;   

  private final static PythonDebugContainer _DUMMY_ =
    new PythonDebugContainer( null );

  private final static ImageIcon _DBGTAB_ICON_ =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/showprod.gif" ),
                  "showprod"
               );

  private final static ImageIcon _COMMAND_ICON_ =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/commandIcon.gif" ),
                  "command"
               );
  private final static ImageIcon _VAR_ICON_     =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/varicon.gif" ),
                  "variables"
               );

  private final static ImageIcon _BUSY_ =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/busy.gif" ),
                  "busy"
               );

  private final static ImageIcon _INACTIVE_  =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/stopped.gif" ),
                  "inactive"
               );
  private final static ImageIcon _ACTIVE_    =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/running.gif" ),
                  "active"
               );
  
  private final static ImageIcon _THREADS_ICON_    =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/threads.gif" ),
                  "threads"
               );
  private final static ImageIcon _THREAD_ICON_    =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/thread.gif" ),
                  "thread"
               );
  private final static ImageIcon _CURRENT_THREAD_ICON_    =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/CurrentThread.gif" ),
                  "currentthread"
               );
  
  public final static ImageIcon  IMPNAV_ICON =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/impnav.gif" ),
                  "impnav"
               );

  public final static ImageIcon ERROR_ICON =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/error.gif" ),
                  "error"
               );

  public final static ImageIcon PYTHON_ICON =
    new ImageIcon(
                  _DUMMY_.getClass().getResource( "images/python16.jpg" ),
                  "python16"
               );

  /** ckient debug agent */
  private PythonDebugClient _pyClient = new PythonDebugClient();

  /** debug pane Stdout container */
  private SwingEhnStatusBar _msgBar = new SwingEhnStatusBar();

  private DebugToolbar _dbgToolbar = new DebugToolbar();

  private _REPORT_TABPANE_   _reportTab   = new _REPORT_TABPANE_();
  private _SETOUT_PANEL_     _setoutPane;
  private _IMPORT_NAVIGATOR_ _importsPane;
  private _THREAD_PANEL_     _threadsPane;
  private boolean            _insideStack  = false;
  private boolean            _insideThread = false;
  private _SOUTH_PANEL_      _southPane;
  private _VARIABLE_TABPANE_ _varTab      = new _VARIABLE_TABPANE_();
  private _STATUS_BAR_       _statusBar;
  private boolean            _debugging   = false;

  private ActionListener     _sendCommandListener = new _SEND_COMMAND_();
  private EditableEnterCombo _command             =
    _dbgToolbar.buildCombo(
                           DebugEvent.COMMANDFIELD,
                           true,
                           _sendCommandListener
                        );
  private JButton            _dbgSend             =
    _dbgToolbar.buildButton( DebugEvent.SENDCOMMAND, _COMMAND_ICON_, null );

  private JComboBox            _stack       = new JComboBox();
  private _LOCAL_VARIABLES_    _locals;
  private _GLOBAL_VARIABLES_   _globals;
  private _DEBUGEVENT_MANAGER_ _evtListener = null;

  private Hashtable _breakpoints      = new Hashtable();
  private Hashtable _changedVariables = new Hashtable();
  private boolean   _bpPopulated      = false;
  private _DATAINPUT_    _dataInput = new _DATAINPUT_() ; 
  
  /** current composite introspection action */
  private CompositeCallback _cCallback = null;

  /** current debugger state */
  private int _state = INACTIVE;

  /** where IDE get populated with debugging events */
  private PluginEventListener _IDEPlug = null;

  /** parent container frame */
  private Container _parent;

  /** Python Thread List Vector */
  private _THREAD_MANAGER_ _threads = new _THREAD_MANAGER_();

  /** current session style */
  private boolean _remoteSession = false;

  /**
   * Creates a new PythonDebugContainer object.
   *
   * @param container DOCUMENT ME!
   */
  public PythonDebugContainer( Container container )
  {
    _parent = container;
    if (container != null)
    {

      // check if parent want beeing populated with debug events
      if (_parent instanceof PluginEventListener)
      {
        _IDEPlug = (PluginEventListener) _parent;
      }
      container.setLayout( new BorderLayout() );
      container.add(
                    BorderLayout.CENTER,
                    new _MAIN_PANEL_()
                 );
    }
  }

  private void setContext( int context )
  {
    switch (context)
    {

      case DebugToolbar.DEBUGGING:
      case DebugToolbar.STARTED:
        _statusBar.setRunning();

        break;

      case DebugToolbar.STOPPED:
        _statusBar.setNotRunning();

        break;
    }
    _dbgToolbar.setContext( context );
  }


  private void storeBreakPoint( String source, int line )
  {
    Hashtable list  = (Hashtable) _breakpoints.get( source );
    Integer   iLine = new Integer( line );
    if (list == null)
    {
      list = new Hashtable();
      _breakpoints.put( source, list );
    }
    list.put( iLine, iLine );
  }


  private void releaseBreakPoint( String source, int line )
  {
    Hashtable list  = (Hashtable) _breakpoints.get( source );
    Integer   iLine = new Integer( line );
    if (list != null)
    {
      list.remove( iLine );
      if (list.isEmpty())
        _breakpoints.remove( source );
    }
  }

  /*
   * public int get_threadCount()
   * {
   * return _threads.size() ;
   * }
   *
   * public Object[] get_threadList()
   * {
   * return _threads.toArray() ;
   * }
   */


  /**
   * populate a beakpoint setting to Python DBG server
   *
   * @param source
   * @param line
   */
  public void setBreakPoint( String source, int line )
  {
    storeBreakPoint( source, line );
    if (_state != INACTIVE)
    {

      // server running populate breakpoint
      breakpointSubcommand( _BPSET_, source, line );
    }
  }


  /**
   * clear a breakpoint on server
   *
   * @param source
   * @param line
   */
  public void clearBreakPoint( String source, int line )
  {
    releaseBreakPoint( source, line );
    if (_state != INACTIVE)

      // server running populate breakpoint
      breakpointSubcommand( _BPCLEAR_, source, line );
  }

  /**
   * clear all breakpoints in source on server
   *
   * @param source
   */
  public void clearServerFileBeakPoints( String source )
  {
    if (_state != INACTIVE)
      // server running populate breakpoint
      breakpointSubcommand( _BPCLEARALLINFILE_, source  , -1);
  }


  /**
   * on debugger starting event just populate all currently set breakpoints to
   * debugger's server side
   */
  public void populateBreakPoints()
  {
    Enumeration sources = _breakpoints.keys();
    Enumeration bps     = _breakpoints.elements();
    while (bps.hasMoreElements())
    {
      String      curSource = (String) sources.nextElement();
      Enumeration lines     = ((Hashtable) bps.nextElement()).elements();
      while (lines.hasMoreElements())
      {
        Integer curLine = (Integer) lines.nextElement();
        breakpointSubcommand(
                             _BPSET_,
                             curSource,
                             curLine.intValue()
                          );
      }
    }
    _bpPopulated = true;
  }


  private void debugSubcommand( String subcommand )
  {
    if (
        subcommand.equals( _STEP_ ) ||
                    subcommand.equals( _RUN_ ) ||
                    subcommand.equals( _NEXT_ )
       )
      _statusBar.setBusy();
    try
    {

      // populate user's variable changes though debugging interface
      populateVariableChanges();
      _pyClient.sendCommand( subcommand );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "debug subcommand failed : " + ex.getMessage() );
    }
  }


  private void breakpointSubcommand( String fx, String source, int line )
  {
    StringBuffer sent = new StringBuffer( fx );

    // make local / remote fname translation
    if (_evtListener != null)
      source = _evtListener.getBpFName( source );

    sent.append( source );
    if ( line != -1 )
    {  
      sent.append( _SPACE_ );
      sent.append( line );
    }  
    try
    {
      _pyClient.sendCommand( sent.toString() );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "breakpoint subcommand failed : " + ex.getMessage() );
    }
  }


  private void launchDebug( String candidate )
  {
    try
    {
      _pyClient.sendCommand( _DBG_ + candidate );
      _debugging = true;
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "launchDebug failed : " + ex.getMessage() );
    }
  }


  private void readSrc( String candidate, int lineno )
  {
    try
    {
      _pyClient.sendCommand( _READSRC_ + candidate + _SPACE_ + Integer.toString( lineno ) );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "readDebug failed : " + ex.getMessage() );
    }
  }


  private void setArgsCommand( String args )
  {
    try
    {
      _pyClient.sendCommand( _SETARGS_ + args );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "SETARGS command failed : " + ex.getMessage() );
    }
  }

  private void threadCommand()
  {
    try
    {
      _pyClient.sendCommand(_THREAD_);
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError("THREAD command failed : " + ex.getMessage());
    }
  }


  private void stackCommand()
  {
    try
    {
      _pyClient.sendCommand( _STACK_ );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "STACK command failed : " + ex.getMessage() );
    }
  }


  private void variableCommand( String command )
  {
    try
    {
      _pyClient.sendCommand( command );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "VARIABLE command failed : " + ex.getMessage() );
    }
  }


  /**
   * Send a composite varibale Inspect command to the debugger
   * @param callBack callback location 
   * @param varName composite variable name  
   */
  public void inspectCompositeCommand(
                                      CompositeCallback callBack,
                                      String            varName
                                   )
  {
    try
    {
      _cCallback = callBack;
      _pyClient.sendCommand( _COMPOSITE_ + varName );
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "INSPECT command failed : " + ex.getMessage() );
    }
  }


  private void populateVariableChanges()
  {
    if (_changedVariables.isEmpty())
      return; // nothing to do

    StringBuffer buffer = new StringBuffer( _COMMAND_ );
    buffer.append( _SPACE_ );
    buffer.append( _SILENT_ );
    buffer.append( _SPACE_ );

    Enumeration keys = _changedVariables.keys();
    while (keys.hasMoreElements())
    {
      String var = (String) keys.nextElement();
      buffer.append( var );
      buffer.append( _EQUAL_ );
      buffer.append( (String) _changedVariables.get( var ) );
      buffer.append( _SEMICOLON_ );
    }
    try
    {
      _pyClient.sendCommand( buffer.toString() );

      // cleanup container
      _changedVariables = new Hashtable();
    }
    catch (PythonDebugException ex)
    {
      _msgBar.setError( "SET VARIABLE command failed : " + ex.getMessage() );
    }
  }

  

  /**
   * populate toolbar action listeners
   */
  private void setActions()
  {
    Hashtable actions = new Hashtable();

    actions.put(
                DebugEvent.START,
                new _DEBUGGING_START_( false )
             );
    actions.put(
                DebugEvent.REMOTESTART,
                new _DEBUGGING_START_( true )
             );
    actions.put(
                DebugEvent.STOP,
                new _DEBUGGING_STOP_()
             );
    actions.put( DebugEvent.SENDCOMMAND, _sendCommandListener );
    actions.put(
                DebugEvent.STEPOVER,
                new _STEP_OVER_()
             );
    actions.put(
                DebugEvent.STEPINTO,
                new _STEP_INTO_()
             );
    actions.put(
                DebugEvent.RUN,
                new _RUN_()
             );
    actions.put(
                DebugEvent.TOGGLEJYTHON,
                new _PYTHON_CHANGED_()
             );
    actions.put(
                DebugEvent.PGMARGS,
                new _SHOW_ARGS_()
             );
    _dbgToolbar.setActions( actions );
  }


  /**
   * DOCUMENT ME!
   *
   * @param name   DOCUMENT ME!
   * @param value  DOCUMENT ME!
   * @param global DOCUMENT ME!
   */
  public void dbgVariableChanged( String name, String value, boolean global )
  {
    _changedVariables.put( name, value );
  }


  public void setCurrentPythonModuleName( String mName )
  {
    _dbgToolbar.set_curModule( mName );
  }


  /**
   * entered when Editor has changed parameters
   */
  public void parametersChanged()
  {
    _setoutPane.checkColoringChanges();
  }


  public PythonDebugClient get_pyClient()
  {
    return _pyClient;
  }


  /**
   * DOCUMENT ME!
   *
   * @throws PythonDebugException DOCUMENT ME!
   */
  public void terminate() throws PythonDebugException
  {

    // if debugger connection is alive
    if (_state != INACTIVE)
      _pyClient.sendCommand( _STOP_ + _END_OF_LINE_ );
  }


  /**
   * used for SHORTCUT activation
   */
  public void activateStepInto()
  {
    if (_state != INACTIVE)
      debugSubcommand( _STEP_ );

  }


  /**
   * DOCUMENT ME!
   */
  public void activateNext()
  {
    if (_state != INACTIVE)
      debugSubcommand( _NEXT_ );
  }


  /**
   * DOCUMENT ME!
   */
  public void activateContinue()
  {
    if (_state != INACTIVE)
      debugSubcommand( _RUN_ );
  }

  class _BREAKPOINTS_
  {
    private String    _src  = null;
    private Hashtable _list = null;

    public _BREAKPOINTS_( String src )
    {
      _src  = src;
      _list = new Hashtable();
    }


    public _BREAKPOINTS_( String src, Hashtable list )
    {
      _src  = src;
      _list = list;
    }

    public void addBP( int line )
    {
      _list.put(
                new Integer( line ),
                new Integer( line )
             );
    }


    public Hashtable get_list()
    {
      return _list;
    }


    public String get_src()
    {
      return _src;
    }

  }


  class _DEBUGEVENT_MANAGER_
  implements PythonDebugEventListener
  {
    private PluginEventListener _plug;

    /** current source under debug */
    private String _currentSource = null;

    /** current debugging source is remote */
    private boolean _remoteSource = false;

    /** remote source name / local tmp source location table */
    private Hashtable _hashSource = new Hashtable();

    /** remote local tmp source / remote source name location table */
    private Hashtable _remoteHashSource = new Hashtable();
    private int       _currentLine      = -1;

    /** 0 => end of program */
    private int _callLevel = 0;

    /* debug over FTP connection if not null */
    private String _ftpSource = null;

    public _DEBUGEVENT_MANAGER_( PluginEventListener pluggin )
    {
      _plug = pluggin;
    }


    private void dealWithCall( PythonDebugEvent e )
    {
      _callLevel++;
      if (_plug == null)
        _setoutPane.writeMessage( e.toString() );
    }


    public String getBpFName( String fName )
    {
      String remoteRef =
        (String) _remoteHashSource.get(
                                       MiscStatic.nonSensitiveFileName( fName )
                                    );
      if (remoteRef != null)
        return remoteRef;

      return fName;
    }


    private void dealWithReturn( PythonDebugEvent e )
    {
      _callLevel--;

      // end of python Program Reached
      if (_callLevel == 0)
      {
        _DEBUGGING_TERMINATOR_ terminator = new _DEBUGGING_TERMINATOR_();
        terminator.start();
      }
      if (_plug == null)
        _setoutPane.writeMessage( e.toString() );
    }


    private void populateToPlugin( int    event,
                                   String localFName,
                                   int    lineNo
                                )
    {
      try
      {
        if (_plug != null)
          _plug.newDebuggingEvent(
                                  new PluginEvent( event, localFName, lineNo )
                               );
      }
      catch (PythonDebugException ex)
      {
        _msgBar.setError( ex.getMessage() );
      }
    }


    private void populateLocalSource( PythonDebugEvent e )
    {
      if (e.get_fName() != null)
      {
        if (
            (_currentSource == null) || (!_currentSource.equals(e.get_fName() ))
           )
          populateToPlugin(
                           PluginEvent.NEWSOURCE,
                           e.get_fName(),
                           e.get_lineNo()
                        );
        else if (_currentLine != e.get_lineNo())
          populateToPlugin(
                           PluginEvent.NEWLINE,
                           e.get_fName(),
                           e.get_lineNo()
                        );
      }
    }


    private void startDebugger( PythonDebugEvent e )
    {
      if (
          (e.get_fName().endsWith( _STRING_ )) || // substituting .equals by
                                                              // endwith for Jython
                                                              // compatibility reasons
                      (e.get_lineNo() == 0)
         ) // go ahead on <string> at startup
        debugSubcommand( _STEP_ );
      else
      {
        if (_plug != null)
        {
          try
          {
            if (_remoteSource)
            {
              String localName = buildTempSource( e );

              // remote debugging name switch
              e.set_fName( localName );
            }
            if (_ftpSource != null)

              // ftp debugging case
              e.set_fName( _ftpSource );
            populateLocalSource( e );
          }
          catch (PythonDebugException ex)
          {
            _msgBar.setError( ex.getMessage() );
          }
        }

        // send BP set collection to server side debugger
        if (!_bpPopulated)
          populateBreakPoints();
        setContext( DebugToolbar.DEBUGGING );
      }
      if (_plug == null)
        _setoutPane.writeMessage( e.toString() );
    }


    private void debuggingSessionIsOver()
    {
      _state       = INACTIVE;
      _ftpSource   = null;
      _bpPopulated = false;
      setContext( DebugToolbar.STOPPED );
      _statusBar.display( _INACTIVE_TEXT_ );
      try
      {
        _pyClient.terminate();
      }
      catch (PythonDebugException evt)
      {
        _msgBar.setError( evt.getMessage() );
      }

      if (_plug != null)
        populateToPlugin( PluginEvent.ENDING, null, PluginEvent.UNDEFINED );
    }


    public void launcherMessage( PythonDebugEvent e )
    {
      synchronized (this)
      {
        switch (e.get_type())
        {

          case PythonDebugEvent.LAUNCHER_ENDING:
            if (!e.get_msgContent().equals( "0" ))
            {
              _setoutPane.writeError( "Debug session Abort =" + e.get_msgContent() );
              _msgBar.setError( "python jpydaemon launcher ABORTED" );
            }
            else
              _setoutPane.writeHeader( "Debug session normal end" );
            if (_state != INACTIVE)

              // force termination on this side
              debuggingSessionIsOver();

            break;

          case PythonDebugEvent.LAUNCHER_ERR:
            _setoutPane.writeError( e.get_msgContent() );
            _msgBar.setError( "python jpydaemon launcher SEVERE ERROR" );

            break;

          case PythonDebugEvent.LAUNCHER_MSG:
            _setoutPane.writeWarning( e.get_msgContent() );

            break;

          default:

            // pass message to plugging handle
            _setoutPane.writeMessage( "unmanaged DebugEvent : " + e.toString() );
        }
      }
    }


    private String extractFileName( String fName )
    {
      int slashPos     = fName.lastIndexOf( '/' );
      int backslashPos = fName.lastIndexOf( '\\' );
      if (slashPos != -1)
        return fName.substring( slashPos + 1 );
      if (backslashPos != -1)
        return fName.substring( backslashPos + 1 );

      return fName;
    }


    /**
     * get and build the temporary file resource
     *
     * @param  e current debugging event containing the original source
     *
     * @return the source file stored in local temp file
     */
    private String buildTempSource( PythonDebugEvent e )
                            throws PythonDebugException
    {
      String curTmp = (String) _hashSource.get( e.get_fName() );

      // first try to get the resource from the local hash if not the first time
      if (curTmp != null)
        return curTmp;

      // download and store locally
      String tmpDir = PythonDebugParameters.get_workDir();
      if (tmpDir == null)
        throw new PythonDebugException(
                                       "temporary workspace directory undefine check jpydbgoptions"
                                    );

      File tmpDirFile = new File( tmpDir );
      if (!tmpDirFile.isDirectory())
        throw new PythonDebugException(
                                       tmpDir +
                                       " workspace tmp dir is not an existing directory "
                                    );

      // store file locally
      File localfName =
        new File(
                 tmpDirFile,
                 extractFileName( e.get_fName() )
              );
      _hashSource.put(
                      e.get_fName(),
                      localfName.getAbsolutePath()
                   );
      _remoteHashSource.put(
                            MiscStatic.nonSensitiveFileName(localfName.getAbsolutePath() ),
                            e.get_fName()
                         );

      readSrc(
              e.get_fName(),
              e.get_lineNo()
           );

      return null; // waiting for READSRC event to complete
    }


    private void storeRemoteFile( PythonDebugEvent e )
    {
      String localSource = (String) _hashSource.get( e.get_fName() );
      _currentSource = null; // force reloading
      try
      {
        e.set_fName( localSource );

        PrintWriter wr = new PrintWriter( new FileWriter( localSource ) );
        wr.write( e.get_srcRead().toString() );
        wr.close();

        // set curent module to the remote debuggee name
        _dbgToolbar.set_curModule( localSource );
        populateLocalSource( e );
        _currentSource = localSource;
      }
      catch (IOException ex)
      {
        _msgBar.setError( "IOERROR storing remote dbg file : " + localSource + " : " + ex.getMessage() );
      }
    }


    private void formatException( String toFormat )
    {
      StringBuffer fmt  = new StringBuffer( toFormat );
      StringBuffer dest = new StringBuffer();
      int          ii   = 0;
      while (ii < fmt.length())
      {
        if ((fmt.charAt( ii ) == '\\') && (fmt.charAt( ii + 1 ) == 'n'))
        {
          _setoutPane.writeError( dest.toString() );
          dest = new StringBuffer();
          ii   = ii + 2;
        }
        else
          dest.append( fmt.charAt( ii++ ) );
      }
      if (dest.length() > 0)
        _setoutPane.writeError( dest.toString() );
    }
    /*
     * private void formatException ( String toFormat )
     * {
     * _TINY_PARSER_ parser = new _TINY_PARSER_(toFormat) ;
     *  while (  parser.nextToken('[') || parser.nextToken(',') )
     *  {
     *    if ( parser.nextToken('\'') ) // string start
     *    {
     *      if ( ! parser.nextToken('\'') ) // string end
     *      { _msgBar.setError("jpydbg internal error : EXCEPTION parse error on
     * :" + toFormat ) ;
     *        return ; // syntax error
     *      }
     *      _TINY_PARSER_ nParser = new _TINY_PARSER_( parser.get_curToken()) ;
     *      while( nParser.nextToken("\\n") )
     *        _setoutPane.writeError( nParser.get_curToken() );
     *    }
     *  }
     * }
     */

    public void newDebugEvent( PythonDebugEvent e )
    {
      switch (e.get_type())
      {

        case PythonDebugEvent.ABORTWAITING:
          _statusBar.display( _INACTIVE_TEXT_ );
          _statusBar.setConnected();
          _state     = INACTIVE;
          _debugging = false;
          // populate debugging stop
          if (_plug != null)
            populateToPlugin( PluginEvent.ENDING, null, PluginEvent.UNDEFINED );

          break;

        case PythonDebugEvent.DATAINPUT :
          if ( _state == STARTED ) 
          {    
            _dataInput.add() ;
            _setoutPane.writeWarning("Debuggee waiting for data input ...") ;
          }  
          break ;
  
        case PythonDebugEvent.WELLCOME:
          _state = STARTING;

          // cleanup any previous remote session flag
          _remoteSource = false;

          // check for remote source
          if (e.get_debuggee() != null)
          {
            _remoteSource     = true;
            _hashSource       = new Hashtable(); // cleanup remote debugging
                                                 // source hash
            _remoteHashSource = new Hashtable();
            _dbgToolbar.set_remoteModule( e.get_debuggee() );
          }

          // populate debugging stop
          if (_plug != null)
            populateToPlugin(
                             PluginEvent.STARTING,
                             null,
                             PluginEvent.UNDEFINED
                          );
          _statusBar.setConnected();
          _statusBar.display( _READY_TEXT_ );
          setContext( DebugToolbar.STARTED );
          // replace the CANCEL_WAITING action by a DEBUGGING_STOPPER
          _dbgToolbar.setAction(
                                DebugEvent.STOP,
                                new _DEBUGGING_STOP_()
                             );


          // check for candidate to debug after initial connection
          String debuggee = _dbgToolbar.get_curModule();
          if (debuggee.length() != 0)
          {

            // populate debuggee command line arguments
            setArgsCommand( _dbgToolbar.get_curModuleArguments() );

            // activate a DBG command on candidate
            if (FtpBuffers.isFtp( debuggee ))
            {
              _ftpSource = debuggee; // store buffer Path name
              launchDebug( FtpBuffers.checkBufferPath( debuggee ) );
            }
            else
              launchDebug( debuggee );
          }
          else
            _msgBar.setWarning( "no python debuggee provided " );

          break;

        case PythonDebugEvent.EXCEPTION:

          // debugging session in error
          _statusBar.resetBusy();
          formatException( e.get_msgContent() );
          _msgBar.setWarning(
                             "exception raising in Debuggee see 'stdout content' for complementary details"
                          );

          break;

        case PythonDebugEvent.STDOUT:

          String content = e.get_msgContent();
          if (content.length() > 0)
            if (content.equals( _EOL_ ))
              _setoutPane.messageAppend( _END_OF_LINE_ );
            else
              _setoutPane.messageAppend( content );

          break;

        case PythonDebugEvent.DEBUGCOMMAND:

          String result = e.get_msgContent();
          if (result.equals( _OK_ ))

            // normal termination of debuggee candidate inside a given session
            // => populate end of debugging session
            debuggingSessionIsOver();
          else

            // debugging session in error
            _msgBar.setError( e.get_msgContent() );

          break;

        case PythonDebugEvent.COMMAND:

          String returned = e.get_msgContent();

          // silent OK returned for internal launched commands
          // are not displayed
          if (!returned.equals( _SILENT_ ))
            _setoutPane.writeHeader( e.get_msgContent() );

          break;

        case PythonDebugEvent.SETARGS:

          // nothing to handle here
          break;

        case PythonDebugEvent.NOP:

          // nothing to handle here
          break;

        case PythonDebugEvent.COMMANDDETAIL:
          _setoutPane.writeError( e.get_msgContent() );

          break;

        case PythonDebugEvent.READSRC:
          if (e.get_retVal().equals( PythonDebugEvent.OK ))
            storeRemoteFile( e );
          else
            _msgBar.setError( "REMOTE READ Failure on : " + e.get_fName() + " : " + e.get_retVal() );

          break;


        case PythonDebugEvent.LINE:
          if (_state == STARTING)
            startDebugger( e );
          _statusBar.resetBusy();
          _currentLine   = e.get_lineNo();
          _currentSource = e.get_fName();
          if (_insideStack)
            // we must refresh the current stack pane content
            stackCommand();
          if (_insideThread)
            // we must refresh the current thread list
            threadCommand();

          break;

        case PythonDebugEvent.CALL:
          dealWithCall( e );

          break;

        case PythonDebugEvent.RETURN:
          dealWithReturn( e );

          break;

        case PythonDebugEvent.TERMINATE:
          debuggingSessionIsOver();

          break;

        case PythonDebugEvent.STACKLIST:
          Assert.that( e.get_stackList() != null );
          _stack.removeAllItems();

          Enumeration stackElements = e.get_stackList().elements();
          while (stackElements.hasMoreElements())
          {
            String curItem = (String) stackElements.nextElement();
            _stack.addItem( curItem );
          }
          _stack.setSelectedIndex( 0 );

          break;

        case PythonDebugEvent.THREADLIST:
          Assert.that(e.get_threadList() != null);
          _threads.set_threads( e.get_threadList() ) ;  
          break ; 
          
        case PythonDebugEvent.GLOBAL:
          _globals.newValues(
                             e.get_variables(),
                             e.get_types()
                          );

          break;

        case PythonDebugEvent.LOCAL:
          _locals.newValues(
                            e.get_variables(),
                            e.get_types()
                         );

          break;

        case PythonDebugEvent.COMPOSITE:
          if (_cCallback != null)
            _cCallback.callbackWithValuesSet(
                                             e.get_variables(),
                                             e.get_types()
                                          );

          break;

        default:

          // pass message to plugging handle
          _setoutPane.writeMessage( "unmanaged DebugEvent : " + e.toString() );

      }
    }
  }


  class _CANCEL_WAITING_CONNECTION_
  implements ActionListener
  {

    public void actionPerformed( ActionEvent evt )
    {
      try
      {
        _pyClient.abort( PythonDebugParameters.get_listeningPort() );
        setContext( DebugToolbar.STOPPED );
      }
      catch (PythonDebugException e)
      {
        _msgBar.setError( e.getMessage() );
      }
    }
  }


  class _DEBUGGING_STARTER_ extends Thread
  {

    public _DEBUGGING_STARTER_( boolean remote )
    {
      _remoteSession = remote;
    }

    public void toRun()
    {
      _msgBar.reset();
      _statusBar.display( "starting..." );

      String dbgHost = PythonDebugParameters.get_dbgHost();
      if (_remoteSession)
      {
        dbgHost = null;
        _dbgToolbar.set_enable( DebugEvent.START, false );
        _dbgToolbar.set_enable( DebugEvent.REMOTESTART, false );
        _dbgToolbar.set_enable( DebugEvent.STOP, true );
        _dbgToolbar.setAction(
                              DebugEvent.STOP,
                              new _CANCEL_WAITING_CONNECTION_()
                           );

        _statusBar.setWaiting();
        _statusBar.display( "waiting for incoming connection ..." );
      }
      try
      {
        if (_evtListener == null) // not yet inited
        {
          _evtListener = new _DEBUGEVENT_MANAGER_( _IDEPlug );
          _pyClient.setPythonDebugEventListener( _evtListener );
        }
        _pyClient.init(
                       dbgHost,
                       PythonDebugParameters.get_listeningPort(),
                       PythonDebugParameters.get_connectingPort(),
                       PythonDebugParameters.get_currentPathLocation(PythonDebugParameters.get_jythonActivated() ),
                       PythonDebugParameters.get_currentShellPath(),
                       PythonDebugParameters.get_jpydbgScript(),
                       PythonDebugParameters.get_jpydbgScriptArgs(),

                       // PythonDebugParameters.build_jythonArgsVector() ,
                       PythonDebugParameters.buildJythonArgs(PythonDebugParameters.get_jythonActivated() ),
                       PythonDebugParameters.get_codePage()
                    );
      }
      catch (PythonDebugException e)
      {
        _msgBar.setError( e.getMessage() );
      }
    }


    public void run()
    {
      toRun();
    }

  }


  class _DEBUGGING_TERMINATOR_ extends Thread
  {

    public void run()
    {
      _statusBar.display( "ending..." );
      try
      {
        terminate();
      }
      catch (PythonDebugException e)
      {
        _msgBar.setError( e.getMessage() );
      }
    }
  }


  class _DEBUGGING_START_
  implements ActionListener
  {
    private boolean _remote = false;

    public _DEBUGGING_START_( boolean remote )
    {
      _remote = remote;
    }

    /**
     * debuggger initialization startup action
     */
    public void actionPerformed( ActionEvent evt )
    {
      _DEBUGGING_STARTER_ starter = new _DEBUGGING_STARTER_( _remote );
      starter.start();
    }
  }


  class _DEBUGGING_STOP_
  implements ActionListener
  {

    /**
     * debuggger initialization startup action
     */
    public void actionPerformed( ActionEvent evt )
    {
      _DEBUGGING_TERMINATOR_ terminator = new _DEBUGGING_TERMINATOR_();
      terminator.start();
    }
  }

  class _APPLY_ 
  implements ActionListener
  {
    private JpyArgsConfigurationPanel _argPane ;    
    public _APPLY_ ( JpyArgsConfigurationPanel argPane )
    { _argPane = argPane ; }
    
    public void actionPerformed( ActionEvent evt )
    { 
      try 
      {        
        _argPane.save() ;
      } catch ( PythonDebugException e )  
      { _msgBar.setError(e.getMessage()) ; }
    }
  }
  
  class _SHOW_ARGS_
  implements ActionListener
  {

    /**
     * Show arguments TabPanel 
     */
    public void actionPerformed( ActionEvent evt )
    {
    JpyArgsConfigurationPanel argPane = new JpyArgsConfigurationPanel() ;
    JButton applyButton = new JButton("apply") ;  
    Object [] buttonRowObjects = new Object[] {
                                          "Ok" ,
                                          applyButton , 
                                          "Cancel" 
                                                      } ;
      applyButton.addActionListener( new _APPLY_(argPane) ) ;
      int value  = JOptionPane.showOptionDialog(
                                      _parent  ,
                                      argPane ,
                                      "reusable program arguments table" ,
                                      JOptionPane.DEFAULT_OPTION ,
                                      JOptionPane.PLAIN_MESSAGE ,
                                      DebugToolbar.PGMARGS_ICON ,
                                      buttonRowObjects ,
                                      applyButton
                                          ) ;
    }
  }


  class _STEP_OVER_
  implements ActionListener
  {
    public void actionPerformed( ActionEvent evt )
    {
      debugSubcommand( _NEXT_ );
    }
  }


  class _STEP_INTO_
  implements ActionListener
  {
    public void actionPerformed( ActionEvent evt )
    {
      debugSubcommand( _STEP_ );
    }
  }


  class _RUN_
  implements ActionListener
  {
    public void actionPerformed( ActionEvent evt )
    {
      debugSubcommand( _RUN_ );
    }
  }


  class _PYTHON_CHANGED_
  implements ActionListener
  {

    /**
     * just capture and populate the language chage to global static
     *
     * @param evt
     */
    public void actionPerformed( ActionEvent evt )
    {
      JToggleButton toggle   =
        (JToggleButton) _dbgToolbar.getButton( DebugEvent.TOGGLEJYTHON );
      boolean       isJython = toggle.isSelected();
      PythonDebugParameters.set_jythonActivated( isJython );

      // populate Switch if we're on the import pane
      if (_reportTab.getSelectedComponent() == _importsPane)
        _importsPane.switchPython();
    }
  }


  class _REPORT_TABPANE_ extends JTabbedPane
  {
    public _REPORT_TABPANE_()
    {
      super( SwingConstants.BOTTOM );
    }
  }


  class _VARIABLE_TABPANE_ extends JTabbedPane
  {
    public _VARIABLE_TABPANE_()
    {
      super( SwingConstants.TOP );
    }
  }


  class _STACK_ITEM_CHANGED_
  implements ItemListener,
             EBComponent
  {
    private String _fileName;
    private View   _view    = null;
    private int    _lineNum = -1;

    public _STACK_ITEM_CHANGED_()
    {
      EditBus.addToBus( this );
    }

    /**
     * Take control over some JEdit source panels events
     */
    public void handleMessage( EBMessage message )
    {
      if (_view == null)
        return;

      if (message instanceof BufferUpdate)
      {
        BufferUpdate bu = (BufferUpdate) message;
        if (bu.getWhat().equals( BufferUpdate.LOADED ))
          set_currentLine( _lineNum - 1 );
      }
    }


    private void set_currentLine( int line )
    {
      JEditTextArea ta = _view.getTextArea();
      ta.setCaretPosition( ta.getLineStartOffset( line ) );
      _view = null;
    }


    /* warning This tiny method is JEdit dependant */
    private void setCurrentLine( String toParse )
    {
      int linePos    = toParse.indexOf( '(' );
      int lineEndPos = toParse.indexOf( ')' );
      if (linePos == -1)
        return;
      _fileName = toParse.substring( 0, linePos ).trim();
      _lineNum  =
        Integer.parseInt( toParse.substring( linePos + 1, lineEndPos ) );
      _view = jEdit.getActiveView();

      Buffer buf = jEdit.openFile( _view, _fileName );
      _view.setBuffer( buf );
      if (buf.isLoaded())
        if (_lineNum > 0)
          set_currentLine( _lineNum - 1 );
        else
          set_currentLine( _lineNum );
    }


    public void itemStateChanged( ItemEvent e )
    {
      switch (e.getStateChange())
      {

        // capture current selected stack level in order
        // to refresh the variables display
        case ItemEvent.SELECTED:

          int pos = _stack.getSelectedIndex();
          variableCommand( _GLOBALS_ + Integer.toString( pos ) );
          variableCommand( _LOCALS_ + Integer.toString( pos ) );
          setCurrentLine( (String) _stack.getSelectedItem() );

          break;
      }
    }
  }


  class _CALL_STACK_PANEL_ extends JPanel
  {

    public _CALL_STACK_PANEL_()
    {
      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _stack );
      _stack.addItemListener( new _STACK_ITEM_CHANGED_() );
      super.setBorder(
                      Swing.buildBorder( "Call Stack", TitledBorder.LEFT, TitledBorder.TOP, Swing.BOXBOLDGRAY, Swing.BEVELRAISED )
                   );

    }
  }


  class _LOCAL_VARIABLES_ extends JPanel
  {
    private JTabbedPane _hostPane;

    // private PythonVariableTable _table = new PythonVariableTable(false) ;
    private PythonVariableTreeTable _table =
      new PythonVariableTreeTable( false );

    public _LOCAL_VARIABLES_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;
      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _table );
      super.setBorder(
                      Swing.buildBorder( "Python Objets Memory Browser", TitledBorder.LEFT, TitledBorder.TOP, Swing.BOXBOLDGRAY, Swing.BEVELLOWERED )
                   );
      _hostPane.addTab( "Local variables", _VAR_ICON_, this );
      _table.set_parent( PythonDebugContainer.this );

    }

    public void newValues( TreeMap values, TreeMap types )
    {
      _table.set_tableValue( values, types );
    }
  }


  class _GLOBAL_VARIABLES_ extends JPanel
  {

    private JTabbedPane _hostPane;

    // private PythonVariableTable _table = new PythonVariableTable(true) ;
    private PythonVariableTreeTable _table =
      new PythonVariableTreeTable( true );

    public _GLOBAL_VARIABLES_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;
      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _table );
      super.setBorder(
                      Swing.buildBorder( "Python Objets Memory Browser", TitledBorder.LEFT, TitledBorder.TOP, Swing.BOXBOLDGRAY, Swing.BEVELLOWERED )
                   );
      _hostPane.addTab( "Global variables", _VAR_ICON_, this );
      _table.set_parent( PythonDebugContainer.this );

    }

    public void newValues( TreeMap values, TreeMap types )
    {
      _table.set_tableValue( values, types );
    }
  }


  class _STACK_PANEL_ extends JPanel
  {
    JTabbedPane _hostPane;
    int         _pos;

    public _STACK_PANEL_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;
      setLayout( new BorderLayout() );
      _hostPane.addTab( "variables", _DBGTAB_ICON_, this );
      _pos = _hostPane.indexOfComponent( this );
      add(
          BorderLayout.NORTH,
          new _CALL_STACK_PANEL_()
       );

      _locals  = new _LOCAL_VARIABLES_( _varTab );
      _globals = new _GLOBAL_VARIABLES_( _varTab );

      add( BorderLayout.CENTER, _varTab );

    }

    public void setEnabled( boolean enabled )
    {
      _hostPane.setEnabledAt( _pos, enabled );
    }
  }
  
  class _THREAD_NODE_RENDERER_
  extends DefaultTreeCellRenderer
  { 
    public Component getTreeCellRendererComponent( JTree tree,
                                                   Object value, 
                                                   boolean sel, 
                                                   boolean expanded,
                                                   boolean leaf, 
                                                   int row, 
                                                   boolean hasFcus)
    {
      super.getTreeCellRendererComponent(tree,value,sel,
                                         expanded,leaf,row,hasFcus);

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
      
      
      Object nodeValue = node.getUserObject();
      if ( nodeValue instanceof String )
      {    
        setText((String) nodeValue );
        setIcon( _THREADS_ICON_ ) ; 
      }
      else if ( nodeValue instanceof PythonThreadInfos )  
      {
        PythonThreadInfos threadInfo = (PythonThreadInfos) nodeValue ; 
        setText( threadInfo.get_name() ) ;
        if ( threadInfo.isCurrent() )
          setIcon( _CURRENT_THREAD_ICON_ ) ;    
        else  
          setIcon(_THREAD_ICON_) ;
      }    
        
      return this ;
    }
  }
  
  

  class _THREAD_PANEL_ extends JPanel 
  implements DebuggerContextChangeListener
  {
    private JTabbedPane _hostPane ;
    private int         _pos      ;
    private JTree       _threadTree ;
    private JScrollPane _scroller = null ;
    private DefaultTreeModel _model  ;
    private DefaultMutableTreeNode _rootNode = new DefaultMutableTreeNode(_PYTHREADS_) ; 

    public _THREAD_PANEL_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;
      setLayout( new BorderLayout() );
      _hostPane.addTab( "Threads", _THREADS_ICON_, this );
      _pos = _hostPane.indexOfComponent( this );
      _model = new DefaultTreeModel(_rootNode);
      _threadTree = new JTree(_model) ;
      _threadTree.setShowsRootHandles(true) ; 
      _threadTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ; 
      _threadTree.setCellRenderer( new _THREAD_NODE_RENDERER_() ) ;
      _threadTree.putClientProperty("JTree.lineStyle" , "Angled") ; 
      _threadTree.setExpandsSelectedPaths(true) ;
      _scroller = new JScrollPane(_threadTree) ;
    
      // declare interest in thread state change
      _threads.addThreadListChangeListener(this) ; 
      add( BorderLayout.CENTER , _scroller ) ; 
      
    }

    public void setEnabled( boolean enabled )
    {
      _hostPane.setEnabledAt( _pos, enabled );
    }
    
    public void populateTree( Object threads[] ) 
    {
      _rootNode.removeAllChildren() ; // clean Everything
      for ( int ii = 0 ; ii < threads.length ; ii++ )
      {
      PythonThreadInfos inf = (PythonThreadInfos) threads[ii]  ;      
        _rootNode.add( new DefaultMutableTreeNode( inf ) ) ; 
      }  
      _threadTree.expandRow(0) ;
      _model.nodeStructureChanged(_rootNode) ;
    }
    
    /**
     * refresh the Thread Tree
    */
    public void fireContextChanged()
    {
      populateTree( _threads.getThreads() ) ;     
    }
  }


  class _IMPORT_NAVIGATOR_ extends JPanel
  {
    private JTabbedPane     _hostPane;
    private ImportNavigator _navigator;

    public _IMPORT_NAVIGATOR_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;
      setLayout( new BorderLayout() );
      _hostPane.addTab( "imports navigator", IMPNAV_ICON, this );
      _navigator = new ImportNavigator();
      add( BorderLayout.CENTER, _navigator );
    }

    public ImportNavigator get_navigator()
    {
      return _navigator;
    }


    public void switchPython()
    {
      _navigator.populateCurrent( true );
    }

  }

  class _DATAINPUT_
  {
    private Vector _dataInput = new Vector() ;
    
    public synchronized void add()
    {
      _dataInput.add(new Object()) ;    
    }    
    
    public synchronized boolean  hasInput()
    { return ! _dataInput.isEmpty() ; }
    
    public synchronized void processedInput( String curData )
    throws PythonDebugException
    {
      if ( hasInput() )
      {
          // process any pending data input    
          _pyClient.sendCommand( _DATA_ + curData );
          _dataInput.remove(0) ;
      }    
    }  
  }
  
  
  class _SEND_COMMAND_
  implements ActionListener
  {
    public void actionPerformed( ActionEvent e )
    {
      try
      {

        // System.out.println("entering _SEND_COMMAND_") ;
        String curSelected = _command.getText();
        if ((curSelected != null) && (curSelected.length() > 0))
        {
          _setoutPane.writeHeader( curSelected );
          if ( _dataInput.hasInput() )
              
          {
            _dataInput.processedInput(curSelected) ;
          }    
          else 
            _pyClient.sendCommand( _COMMAND_ + curSelected );
        }
      }
      catch (PythonDebugException ex)
      {
        _msgBar.setError( "SendCommand Exception occured : " + ex.getMessage() );
      }
    }
  }


  class _COMMAND_PANEL_ extends JPanel
  {

    public _COMMAND_PANEL_()
    {
      JLabel text = new JLabel( " command : " );
      setLayout( new BoxLayout( this, BoxLayout.X_AXIS ) );
      _command.setEnabled( false );
      _dbgSend.setEnabled( false );
      _dbgSend.setBorderPainted( false );
      _dbgSend.setRolloverEnabled( true );
      _dbgSend.setBorder( null );

      // _dbgSendLabel.setEnabled(false) ;
      // prevent height expansion of the _command JText field area
      Dimension d = _command.getMaximumSize();
      Dimension e = _command.getPreferredSize();
      d.height = e.height;
      _command.setMaximumSize( d );

      add( text );
      add( _command );
      add( Box.createRigidArea( new Dimension( 5, 5 ) ) );
      add( _dbgSend );
      add( Box.createRigidArea( new Dimension( 5, 5 ) ) );

      // add ( _dbgSendLabel ) ;
      super.setBorder(
                      Swing.buildBorder( "Python shell", TitledBorder.LEFT, TitledBorder.TOP, Swing.BOXBOLDGRAY, Swing.BEVELRAISED )
                   );
    }
  }


  class _SETOUT_PANEL_ extends JPanel
  {
    JTabbedPane _hostPane;

    SwingMessageArea _setOutTrace;
    int              _pos;

    public _SETOUT_PANEL_( JTabbedPane hostPane )
    {
      _hostPane = hostPane;

      // outTrace (to be populated later)
      _setOutTrace = new SwingMessageArea(
                                          PythonDebugParameters.get_shellFont(),
                                          PythonDebugParameters.get_shellBackground(),
                                          PythonDebugParameters.get_shellHeader(),
                                          PythonDebugParameters.get_shellError(),
                                          PythonDebugParameters.get_shellWarning(),
                                          PythonDebugParameters.get_shellMessage()
                                       );
      _setOutTrace.set_refresh( true );

      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _setOutTrace );
      add(
          BorderLayout.SOUTH,
          new _COMMAND_PANEL_()
       );

      _hostPane.addTab( "stdout content", _DBGTAB_ICON_, this );
      _pos = _hostPane.indexOfComponent( this );
    }

    public void setEnabled( boolean enabled )
    {
      _hostPane.setEnabledAt( _pos, enabled );
    }


    public void writeMessage( String msg )
    {
      _setOutTrace.message( msg );
    }


    public void messageAppend( String msg )
    {
      _setOutTrace.messageAppend( msg );
    }


    public void writeError( String msg )
    {
      _setOutTrace.error( msg );
    }


    public void writeHeader( String msg )
    {
      _setOutTrace.headerFooter( msg );
    }


    public void writeWarning( String msg )
    {
      _setOutTrace.warning( msg );
    }


    public void checkColoringChanges()
    {
      _setOutTrace.populateGUIInfos(
                                    PythonDebugParameters.get_shellFont(),
                                    PythonDebugParameters.get_shellBackground(),
                                    PythonDebugParameters.get_shellHeader(),
                                    PythonDebugParameters.get_shellError(),
                                    PythonDebugParameters.get_shellWarning(),
                                    PythonDebugParameters.get_shellMessage()
                                 );
    }
  }


  class _STATUS_BAR_ extends JPanel
  {
    private JLabel _elapsedTimeLabel = new JLabel( _INACTIVE_TEXT_ );
    private JLabel _cancel           = new JLabel( _INACTIVE_ );

    // private ActionListener _cancelAction = null ;
    private boolean        _busy   = false;
    private AnimatedCursor _cursor = new AnimatedCursor( _parent );

    public _STATUS_BAR_()
    {
      setLayout( new BorderLayout() );
      add( BorderLayout.CENTER, _elapsedTimeLabel );
      add( BorderLayout.EAST, _cancel );
    }

    public JLabel get_elapsedTimeLabel()
    {
      return _elapsedTimeLabel;
    }


    public void display( String text )
    {
      _elapsedTimeLabel.setText( text );
    }


    public void setWaiting()
    {

      // _cancelAction = new _CANCEL_WAITING_CONNECTION_() ;
      setRunning();
      _cancel.validate();
    }


    public void setConnected()
    {
      setNotRunning();
      _cancel.validate();
    }


    private void setStatusIcon( final ImageIcon icon )
    {
      SwingUtilities.invokeLater(
                                 new Runnable()
        {
          public void run()
          {
            _cancel.setIcon( icon );
          }
        }
                              );

    }


    public void setBusy()
    {
      _busy = true;
      display( _BUSY_TEXT_ );
      setStatusIcon( _BUSY_ );

      // Setup mouse cursor animation
      _cursor.startAnimation();
      new Thread( _cursor ).start();
      // _cursor.startWaitingCursor() ;
    }


    public void resetBusy()
    {
      if (_busy)
      {
        setStatusIcon( _ACTIVE_ );
        display( _READY_TEXT_ );

        // stop mouse cursor animation
        _cursor.stopAnimation();
        // _cursor.stopWaitingCursor() ;
      }
    }


    public void setRunning()
    {
      setStatusIcon( _ACTIVE_ );
    }


    public void setNotRunning()
    {
      setStatusIcon( _INACTIVE_ );
      // reset threads counters
      _threads.cleanup() ; 
      // stop mouse cursor animation
      _cursor.stopAnimation();
      // _cursor.stopWaitingCursor() ;
    }
  }


  class _SELECTED_TAB_CHANGED_
  implements ChangeListener
  {
    public void stateChanged( ChangeEvent e )
    {
      if (e.getSource() instanceof _REPORT_TABPANE_)
      {
        _REPORT_TABPANE_ curPane = (_REPORT_TABPANE_) e.getSource();
        Object           cur     = curPane.getSelectedComponent();
        if ((cur instanceof _STACK_PANEL_) &&
                        (_debugging))
        {

          // capturing user entering the stack / variables pane
          // System.out.println("state change entered") ;
          stackCommand();
          _insideStack = true;
          _insideThread = false ; 
        }
        else
        {
          if (cur instanceof _IMPORT_NAVIGATOR_)
          {
            _IMPORT_NAVIGATOR_ curNav = (_IMPORT_NAVIGATOR_) cur;

            // request for initer processing on first click
            curNav.get_navigator().initialTreePanelInit();
            _insideThread = false ;
          }
          else
          {
            if (cur instanceof _THREAD_PANEL_)
            {
              threadCommand();
              _insideThread = true;
            }    
          }    
          _insideStack = false;
        }
      }
    }
  }


  class _SOUTH_PANEL_ extends JPanel
  {


    public _SOUTH_PANEL_()
    {
      super( new BorderLayout() );

      _reportTab.addChangeListener( new _SELECTED_TAB_CHANGED_() );

      _setoutPane = new _SETOUT_PANEL_( _reportTab );
      new _STACK_PANEL_( _reportTab );
      _importsPane = new _IMPORT_NAVIGATOR_( _reportTab );
      _threadsPane = new _THREAD_PANEL_( _reportTab ) ;
      _statusBar   = new _STATUS_BAR_();

      add( BorderLayout.NORTH, _statusBar );
      add( BorderLayout.CENTER, _reportTab );
      setActions();
      add( BorderLayout.SOUTH, _msgBar );

      // a debugging eyecatcher
      _msgBar.setToolTipText( PythonDebugClient.VERSION );
    }


  }


  class _MAIN_PANEL_ extends JPanel
  {
    public _MAIN_PANEL_()
    {
      setLayout( new BorderLayout() );
      _southPane = new _SOUTH_PANEL_();

      // add debug bar view on top
      add( BorderLayout.NORTH, _dbgToolbar );

      // populate component version infos
      _dbgToolbar.setToolTipText( VERSION );
      add( BorderLayout.CENTER, _southPane );
      setContext( DebugToolbar.STOPPED );
    }
  }
  
  class _THREAD_MANAGER_
  {
    /** implement here a Vector of PythonThreadInfos collected from python env */   
    private Hashtable _threads = new Hashtable() ;  
    
    private DebuggerContextChangeListener _threadListChange = null ;
    
    
    public boolean isListeningBack()
    {
        if ( _threadListChange != null )
            return true ; 
        return false ;
    }
    
    
    /** feed back here form python side */
    public synchronized void set_threads( Vector threads)
    {
    Enumeration tlist     = threads.elements() ; 
    Vector deleted = new Vector() ;
    boolean hasChanged = false ; 
    Enumeration oldList = _threads.elements() ;
    
      while ( tlist.hasMoreElements() )
      {
      PythonThreadInfos pyThInf = (PythonThreadInfos) tlist.nextElement() ;
      PythonThreadInfos compared = (PythonThreadInfos)_threads.get( pyThInf.get_name() ) ; 
        if ( compared == null )
        {
          hasChanged = true ; 
          _threads.put(pyThInf.get_name() , pyThInf) ; 
        }  
        else
        {
          if ( compared.isCurrent() != pyThInf.isCurrent() )
            hasChanged = true ; 
          _threads.put(pyThInf.get_name() , pyThInf) ; 
              
        }    
      }    
      // look for deleted 
      while ( oldList.hasMoreElements() )
      {
      Object cur = oldList.nextElement() ;  
        if ( ! threads.contains(cur) )
        {    
          deleted.add(cur) ;    
          hasChanged = true ; 
        }  
      }      
      Enumeration delList = deleted.elements() ;
      // proceed with deletions
      while ( delList.hasMoreElements() )
      {
      PythonThreadInfos pyThInf = (PythonThreadInfos) delList.nextElement()  ;   
        _threads.remove( pyThInf.get_name() ) ;        
      }    
      // notify model back 
      if ( ( _threadListChange != null ) && ( hasChanged ) )
        _threadListChange.fireContextChanged() ;   
    }
    
    public void cleanup()
    { 
      _threads = new Hashtable() ; 
      // notify model back 
      if ( _threadListChange != null ) 
        _threadListChange.fireContextChanged() ;   
    }  
        
    public int getThreadCount()
    { 
      return _threads.size() ; 
    }
    
    public Object[] getThreads()
    { 
      Object[] returned = new Object[ _threads.size() ] ; 
      Enumeration tList = _threads.elements() ; 
      int ii = 0 ;
      while ( tList.hasMoreElements() )
      {
        returned[ii++] = tList.nextElement() ;  
      }    
      return returned ;
    }
    
    public void refreshThreads()
    {
      threadCommand() ;
    }
      
    /** used to populate back to ThreadModel window */
    public synchronized void addThreadListChangeListener( DebuggerContextChangeListener l )
    { 
      if (  _threadListChange == null )  
      {    
        _threadListChange = l ; 
        // refresh the ThreadList now to populate back accurate infos
        refreshThreads() ; 
      }  
    }

    /** used to populate back to ThreadModel window */
    public synchronized void removeThreadListChangeListener( DebuggerContextChangeListener l )
    { 
      if ( l == _threadListChange )    
        _threadListChange = null ; 
    }

  }
  
  
  class _STACK_MANAGER_
  {
    /** implement here a Vector of PythonThreadInfos collected from python env */   
    private Vector _stack = new Vector() ;  
    
    private DebuggerContextChangeListener _stackListChange = null ;
    
    
    public boolean isListeningBack()
    {
      if ( _stackListChange != null )
          return true ; 
      return false ;
    }
    
    public void clean()
    { _stack.removeAllElements() ; }
    
    public void add( Object o )
    { _stack.addElement(o) ; }
    
    public void notifyBack()
    {
      // notify model back 
      if ( _stackListChange != null ) 
        _stackListChange.fireContextChanged() ;   
        
    }
    
    public int getSize()
    { return _stack.size() ; }
    
    public StackInfo[] getStackList()
    {
    StackInfo[] returned = new StackInfo[_stack.size()]  ;
      for  ( int ii = 0 ; ii< _stack.size() ; ii++ )
      {
        if ( ii == 0 )
          returned[ii] = new StackInfo((String) _stack.elementAt(ii) , true ) ; // current    
        else
          returned[ii] = new StackInfo((String) _stack.elementAt(ii) , false ) ;     
      }     
      return returned ; 
    }
    
  }
 
  /** return the number of currently running Python threads */
  public int getThreadCount() 
  { return _threads.getThreadCount() ; }

  /** return the current python Thread list */
  public Object[] getThreads() 
  { return _threads.getThreads() ; }
  
}
