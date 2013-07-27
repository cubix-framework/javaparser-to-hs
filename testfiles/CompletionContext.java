/*
 * CompletionContext.java
 *
 * Created on October 7, 2006, 3:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jymc.jpydebug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

/**
 * Singleton Completion context shared by Jedit / netbeans completion context
 * @author jean-yves
 */
public class CompletionContext
{
  private final static String _STRDOT_= "." ;
  private final static char _DOT_     = '.' ;
  private final static String _OPENPAR_ = "(" ;
  private final static String _SELF_ = "self" ;
  private final static ArrayList _EMPTYLIST_ = new ArrayList() ;
  private final static String _DELIMS_  = _DOT_ + _OPENPAR_ ;
  
  private static CompletionContext _me = null ;
  
  private _INNERCONTEXT_ _contextFirst = null   ; 
  private _INNERCONTEXT_ _contextLast = null   ; 

  private String _moduleName ; 
  
  private static CompletionClient _completer = null ;
  private String _curStringContext = null ;
  
  /** Creates a new instance of CompletionContext */
  public CompletionContext()
  {}
  
  /**
     manage the singleton 
  */
  public static CompletionContext getInstance()
  { 
    if ( _me == null ) 
      _me= new CompletionContext() ;
    return _me ; 
  }
  
  
  private _INNERCONTEXT_ newTopContext( _INNERCONTEXT_ parent ,
                                        String token , 
                                        int line ,
                                        String path 
                                      )  
  {
  _INNERCONTEXT_  newContext  = new _INNERCONTEXT_( token , parent , line , path ) ;
    if ( parent != null )
      parent.setNext(newContext) ;
    else
    {
      // refresh full token link
      _moduleName = token ; 
      _contextFirst = newContext ;
      _contextLast = newContext ;
    }  
    newContext.setPrev(parent) ;
    return newContext ;
  }                                  
  
  private _INNERCONTEXT_ scanContext( _INNERCONTEXT_ from , 
                                      String token , 
                                      int line ,
                                      String path 
                                    )
  {
    
    if ( from == null ) // last node
    {
      _contextLast = newTopContext( _contextLast , token , line , path ) ;
      if ( _contextFirst == null )
        _contextFirst = _contextLast ;
      return _contextLast ;
    }  
    else
    { 
      if ( ! from.isContextOf(token)  )
      { 
          // reset context from here with new name 
          from = newTopContext(from.prev() , token , line , path) ;
      }  
    }
    return from ;
  }
  
  private _INNERCONTEXT_ getContext( String strContext , int line , String bufferPath , boolean endsWithDots )
  {
  StringTokenizer parser   = new StringTokenizer( strContext , _DELIMS_ ) ; 
  boolean hasDots = strContext.indexOf(_DOT_) != -1 ;
  _INNERCONTEXT_ curContextCandidate  = null ;
  _INNERCONTEXT_ nextContextCandidate = _contextFirst ;
    if ( hasDots ) // scan only if token contains at least one dot
    {  
      while ( parser.hasMoreTokens() )
      {
        _curStringContext = parser.nextToken() ;
        if ( ( parser.hasMoreTokens() ) || ( endsWithDots ) )
        {
          // when last token is reach if does not end with dots use parentContext + filter 
          curContextCandidate = scanContext( nextContextCandidate , _curStringContext , line , bufferPath ) ;
          if ( curContextCandidate != null )
            nextContextCandidate = curContextCandidate.next() ;
          else
            nextContextCandidate = null ;
        }  
      }
    }
    return curContextCandidate ; 
  }
  
  public String get_moduleName() 
  { return _moduleName ; }
  
  public ArrayList parseContext( String strContext , int line , String bufferPath )
  {
  // get Syntax tree  
  boolean endsWithDots = strContext.endsWith(_STRDOT_)   ;
  _INNERCONTEXT_ curContext = getContext( strContext , line , bufferPath , endsWithDots) ; 
    if ( curContext == null )
      return _EMPTYLIST_ ; 
    
    PythonSyntaxTreeNode curNode = curContext.get_semantics(endsWithDots) ;
  
    if ( curNode == null )
      return _EMPTYLIST_ ; // no valuable completions
  
    // next build array List out of it 
    ArrayList  returned = new ArrayList() ;
      
      String curName = curContext.get_resolvArgs() ;
      int lastDot = curName.lastIndexOf(_DOT_) ;
      if ( lastDot == -1 )
        _moduleName = curName ; 
      else 
        _moduleName = curName.substring( 0 , lastDot ) ;

      if ( strContext.startsWith(_SELF_)  )
      {
      Enumeration classes = curNode.get_classList().elements() ;
        if ( classes.hasMoreElements() )
        {  
          curNode = (PythonSyntaxTreeNode) classes.nextElement() ;
        } 
      } 
      // populate found dependencies in list 
      if ( curNode.hasClass() )
      {
      Enumeration curList = curNode.get_classElements() ; 
        while( curList.hasMoreElements() )
          returned.add( curList.nextElement() ) ;
      }  
      if ( curNode.hasFields() )
      {
      Enumeration curList = curNode.get_fields() ; 
        while( curList.hasMoreElements() )
          returned.add( curList.nextElement() ) ;
      }  
      if ( curNode.hasMethods() )
      {
      Enumeration curList = curNode.get_methodElements() ; 
        while( curList.hasMoreElements() )
          returned.add( curList.nextElement() ) ;
      }  
      
      if ( ! endsWithDots )
        returned = filter( returned  , _curStringContext ) ;
      
      return returned ; 
  
  }
  
  /**
  * filter the actually populated list with the after dot typed character
  */
  public ArrayList filter( Collection c , String b )
  {
  ArrayList returned = new ArrayList() ; 
  Iterator it = c.iterator() ;  
    while ( it.hasNext() )
    {
    PythonSyntaxTreeNode cur = (PythonSyntaxTreeNode)it.next() ;
      if ( cur.get_nodeName().startsWith(b))
        returned.add(cur) ;
    }  
    return returned ;   
  }
    
  
  
  class _INNERCONTEXT_ 
  {
    private String _content ; 
    private PythonSyntaxTreeNode _semantics ;
    private StringBuffer _resolvArgs = new StringBuffer() ;
    private int          _line ;
    private String       _path = null ; 
    private _INNERCONTEXT_ _next = null ; 
    private _INNERCONTEXT_ _prev ;
    
    public _INNERCONTEXT_( String content , _INNERCONTEXT_ parent , int line , String path)
    {
      _content = content ;
      _prev = parent ; 
      _line = line ;
      _path = path ;
      if ( parent != null )
      {  
        parent.setNext( this ) ;
        setPrev(parent) ;
        _resolvArgs.append(parent.get_resolvArgs()) ;
      }
      _resolvArgs.append(content) ;
    }
    
    public String get_content()
    { return _content ; }
    
    private PythonSyntaxTreeNode lookFor ( PythonSyntaxTreeNode semantics , String name )
    {
      if ( semantics == null )
        return null ; 
      
      if ( name.equals(_SELF_) ) // specific self case 
        return semantics ; 
      
      if ( semantics.get_nodeName().equals(name) )
        return semantics ; 
      
      Enumeration classes = semantics.get_classElements() ;
      Enumeration modules = semantics.get_moduleElements() ;
      if ( classes != null )
        return ( lookFor( (PythonSyntaxTreeNode)classes.nextElement() , name ) ) ;
      if ( modules != null )
        return ( lookFor( (PythonSyntaxTreeNode)modules.nextElement() , name ) ) ;
      return null ;
    }
    
    private void preparePythonContext() 
    {
      if ( _completer == null )
      {
        _completer = new CompletionClient() ;     
        // use consecutive JpyDbg listening port for completion
        try {
          _completer.init( PythonDebugParameters.get_listeningPort()+1) ; 
        } catch ( PythonDebugException e) 
        { 
          JOptionPane.showMessageDialog( null , 
                       "completion launch failure :" + e.getMessage()  , 
                       "completion failure",
                       JOptionPane.ERROR_MESSAGE
                        ) ;   
        }
      } 
      if ( _completer != null )
      try {    
        _completer.match( _resolvArgs.toString() ,  _path , _line ) ;  
        _semantics = lookFor( _completer.get_topNode() , _content ) ;
        // look for requested subNode 
        
      } catch ( PythonDebugException e )
      { JOptionPane.showMessageDialog( null , 
                       "CompletionDaemon error  :" + e.getMessage()  , 
                       "completion failure",
                       JOptionPane.ERROR_MESSAGE
                        ) ;   
      }
      
    }
    
    public PythonSyntaxTreeNode get_semantics( boolean isFinal )
    { 
      
      if ( _semantics == null )
      {
        if ( isFinal )
        {  
          _resolvArgs.append('.') ;
          preparePythonContext() ; // start or check for completion server connection
        }  
      }  
      return _semantics ; 
    }
    
    public boolean isContextOf( String token )
    {
      if ( token.equals(_content) )
        return true ;
      return false ;
    }
    
    public void setPrev( _INNERCONTEXT_ prev  )
    { _prev = prev ; }
    
    public _INNERCONTEXT_ prev() 
    { return _prev ; }
    
    public void setNext( _INNERCONTEXT_ next  )
    { _next = next ; }
    
    public _INNERCONTEXT_ next() 
    { return _next ; }
    
    public String get_resolvArgs()
    { return _resolvArgs.toString() ; }
    
  }
  
}
