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

package org.jymc.jpydebug.jedit.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.syntax.DefaultTokenHandler;
import org.gjt.sp.jedit.syntax.Token;

import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

import org.jymc.jpydebug.CompletionClient;
import org.jymc.jpydebug.CompletionContext;
import org.jymc.jpydebug.PythonDebugException;
import org.jymc.jpydebug.PythonDebugParameters;
import org.jymc.jpydebug.PythonSyntaxTreeNode;
import org.jymc.jpydebug.jedit.PythonJeditPanel;

public class JeditCompletionContext
{
  private static final char _DOT_='.';
  private static final char _STARTMETHOD_='(';
  private static final String _SELF_ = "self" ; 

  private boolean _isMethodStatement=false;
  private char _lastChar;
  private String _currentLine;
  private Buffer _buffer;
  private PythonSyntaxTreeNode _sourceTree ; 
    //private ClassInfoFinder classInfoFinder;
    //private SourceTree sourceTree;
  private Collection _memberList= new ArrayList();
  private PythonSyntaxTreeNode _topNode = null ;
  private boolean _isClass  = false ; 
  private Collection _startMemberList= new ArrayList();
  private int _line;
  private int _offset;
  private int _col;
  private String _wholeBuffer;
  private String _plusText;
  private JEditTextArea _ta;
  
  private String _moduleName = null ; 
  
  private StringBuffer _filter = new StringBuffer() ;
  
  private static CompletionClient _completer = null ;
    
  public JeditCompletionContext() {}
    
  public JeditCompletionContext(JEditTextArea ta , JeditCompletionContext parent) 
  throws UncompletedException 
  {
    System.out.println("entering competionCOntext constructor") ;  
    _ta=ta;
    _buffer = (Buffer)_ta.getBuffer();
        
    _offset = _ta.getCaretPosition();
    if (_offset < 2) 
      throw new UncompletedException("OFFSET <2");

    _lastChar= _buffer.getText(_offset - 2, 2).charAt(1);
    _line = _ta.getLineOfOffset(_offset);
    _col = _offset - _ta.getLineStartOffset(_line);
    _currentLine = _ta.getLineText(_line);
    _wholeBuffer = _ta.getText();
    
    CompletionContext cc = CompletionContext.getInstance() ;
    _memberList = cc.parseContext( getPreceedingToken() , _line , _buffer.getPath() ) ;
    _moduleName = cc.get_moduleName() ;
    
  }
  
  public String get_moduleName()
  { return _moduleName ; }
  
  public PythonSyntaxTreeNode get_topNode()
  { return _topNode ; }
  
  private PythonSyntaxTreeNode lookFor( Enumeration en , String name )
  {
    while ( (en != null) && (en.hasMoreElements()) )
    {  
    PythonSyntaxTreeNode  wkNode = (PythonSyntaxTreeNode) en.nextElement() ;
      if ( wkNode.get_nodeName().equals(name))
        return wkNode ;
    } 
    return  null ;
  }
  
  private PythonSyntaxTreeNode findNode( PythonSyntaxTreeNode curNode , String name )
  {
  Enumeration classes = curNode.get_classElements() ;
  Enumeration modules = curNode.get_moduleElements() ;
    PythonSyntaxTreeNode returned = lookFor( classes , name ) ;
    if ( returned == null )
      returned = lookFor( modules , name ) ;
    return returned ; 
  }
  
    private void preparePythonContext() 
    throws UncompletedException 
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
         
    }
    
    private String getPreceedingToken()
    {
    int pos = _offset-1 ;
    int start = _ta.getLineStartOffset(_line) ;
    int len = 0 ; 
    char curChar = _buffer.getText(pos,1).charAt(0) ; 
      while ( (pos != start) && ( curChar != ' ' ) )
      {  
        pos-- ; 
        len++ ; 
        curChar = _buffer.getText(pos,1).charAt(0) ; 
      }  
      return _buffer.getText(pos+1,len) ; 
    }
    
    
    private void prepareList()
    throws UncompletedException     
    {
      _plusText = getPreceedingToken() ; 
      if (this.isContextInLiteralOrComment()) 
          return;

      // _sourceTree.match(_plusText,_memberList) ; 
      
      if ( _completer != null )
      try {    
        _completer.match( _plusText.toString() ,  _buffer.getPath() , _line ) ;    
      } catch ( PythonDebugException e )
      { throw new UncompletedException ("CompletionDaemon error =" + e.getMessage()) ; }
      
    }
    
    public StringBuffer get_filter()
    { return _filter  ;}
    
    public boolean isMethodContext()
    {   return _isMethodStatement; }
    
    public PythonSyntaxTreeNode getMemberNode()
    {   return _topNode ;}

    /**
      build an arrayList out of topNode
    */ 
    public Collection getMemberList()
    {
       return _memberList ;
    }
    
    public String getPlusText()
    { 
      if ( _plusText == null )
        return null ;
      return _plusText.toString() ; 
    }
    
    public char getLastChar()
    { return _lastChar; }
    public String getCurrentLine()
    {return _currentLine;}
    
    public Buffer getBuffer()
    {return _buffer; }
/*    public ClassInfoFinder getClassInfoFinder()
    {
        return classInfoFinder;
    }
*/    
 //   public PythonSyntaxTreeNode getSourceTree()
 //   { return _sourceTree; }
    public int getLine()
    { return _line;}
    public int getOffset()
    { return _offset;}
    public int getCol()
    { return _col;}
    public String getWholeBuffer()
    { return _wholeBuffer;}
    
    public JEditTextArea getTa()
    { return _ta; }

    public boolean isContextInPythonDoc() 
    {
      if (this.getCurrentLine().trim().startsWith("#")) 
        return true;
      return false;
    }

    public boolean isContextInLiteralOrComment() 
    {
      //take a look if jedit have an API for that
      DefaultTokenHandler tokenHandler= new DefaultTokenHandler();
      _buffer.markTokens(this.getLine(),tokenHandler);
      Token token= tokenHandler.getTokens();
      if (token != null) 
      {
        while( token.offset+token.length < _offset - _buffer.getLineStartOffset(_line)) 
        {
          token= token.next;
          if(token == null) 
          {
            if (PythonJeditPanel.isDebugEnabled() ) Log.log(Log.DEBUG,this,"NULL--return false");
            return false;
          }
        }
        if (token.id == Token.LITERAL1 || token.id == Token.LITERAL2 ||
            token.id == Token.COMMENT1 ) 
        {
          if ( PythonJeditPanel.isDebugEnabled() ) Log.log(Log.DEBUG,this,"return true");
            return true;
        }
      }
      if ( PythonJeditPanel.isDebugEnabled()) Log.log(Log.DEBUG,this,"DEFAULT--return false");
      return false;
    }


    
} // -- end class JeditCompletionContext

