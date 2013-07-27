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

import javax.swing.* ; 
import javax.swing.event.*; 
import javax.swing.tree.* ; 

import java.awt.* ; 
import java.awt.event.* ; 
import java.util.*;

import org.jymc.jpydebug.*;

import java .io.* ;
import org.jymc.jpydebug.swing.ui.*;


/**
 * @author jean-yves
 *
 * A clean an lean portable Navigator to be used With non Jedit hosts
 * interface NETBEANS
 */
public class JpyDbgNavigator
extends JPanel
{
  public final static String IMPORT_FAILURE = "import failure" ; 
  public final static String BUILTIN = "builtin" ;
  private final static String _BINARY_TOOLTIP_ = "python binary module " ; 
  private final static String _BUILTIN_TOOLTIP_ = "python builtin module" ; 
  private final static String _LOAD_FAILURE_ = "python module load failed : CHECK PYTHONPATH" ; 
  private final static String _PYTHON_SUFFIX_ = ".py" ; 
  
  private _TREE_ _importsTree ; 
  private DefaultTreeModel _model  ;
  private DefaultMutableTreeNode _rootNode = new _MUTABLE_ROOT_NODE_(new _TEXT_NODE_("Module",true,false)) ; 
  // private PythonPathPanel _pythonPathPane = new PythonPathPanel() ; 
  private PythonPathTreePanel _pythonPathPane = new PythonPathTreePanel() ; 
  private String _currentSource = null ; 
  
  private boolean _treePanelInited = false ; 
  
  /** to be used when Jython environment is requested */
  private JythonInterpretor _jython = new JythonInterpretor(PythonDebugParameters.get_jythonHome()) ; 
  
  class _MUTABLE_ROOT_NODE_
  extends DefaultMutableTreeNode
  {
    public String getToolTipText()
    { return ("Double click here in order to refresh module tree") ;  }
    
    public _MUTABLE_ROOT_NODE_( Object dataNode )
    { super(dataNode) ; }
  }
  
  class _TREE_ 
  extends JTree
  {
    public String getToolTipText(MouseEvent evt) 
    {
      if (getRowForLocation(evt.getX(), evt.getY()) == -1) return null;    
      TreePath curPath = getPathForLocation(evt.getX(), evt.getY());
      if ( curPath.getLastPathComponent().getClass() == _MUTABLE_ROOT_NODE_.class )
	    return ((_MUTABLE_ROOT_NODE_)curPath.getLastPathComponent()).getToolTipText();

      DefaultMutableTreeNode curNode = (DefaultMutableTreeNode)curPath.getLastPathComponent();
      Object curInstance = curNode.getUserObject() ; 
      // add here tooltip customization for BUILTIN and BINARY module imports
      if ( curInstance instanceof PythonTreeNode )
      {
      PythonTreeNode cur = (PythonTreeNode)curInstance ;
        if ( cur.is_binary() ) 
	      return(_BINARY_TOOLTIP_ + cur.get_location()) ;   
	    if ( cur.is_builtin() ) 
	      return(_BUILTIN_TOOLTIP_) ; 
	    if ( cur.hasLoadFailed() )
	      return(_LOAD_FAILURE_) ; 
        return(cur.get_location()) ; 
      }
      // return null on other nodes since tooltips are not applicable
      return null ; 
    }
    
    public _TREE_ ( DefaultTreeModel model )
    { super(model) ; }
  }

  public JpyDbgNavigator( String sourceName )
  {
    _currentSource = sourceName ; 
    setLayout( new BorderLayout() ) ; 
	
    _model = new DefaultTreeModel(_rootNode);
    _importsTree = new _TREE_(_model) ;
    _importsTree.setShowsRootHandles(true) ; 
    _importsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ; 
    _importsTree.setCellRenderer( new _IMPORT_CELL_RENDERER_() ) ;
    _importsTree.putClientProperty("JTree.lineStyle" , "Angled") ; 
    _TREE_SELECTION_ sel = new _TREE_SELECTION_() ; 
    _importsTree.addTreeSelectionListener(sel) ;
    ToolTipManager.sharedInstance().registerComponent(_importsTree);

    // capture double click on root node
    _importsTree.addMouseListener(sel) ; 
  }

  class _TEXT_NODE_
  {
    private String _text ; 
    private boolean _root ; 
    private boolean _error ; 
    
    public _TEXT_NODE_( String text , boolean root , boolean error )
    { 
      _text = text ; 
      _root = root ; 
      _error = error ; 
    }
    
    public boolean is_root()
    { return _root ; }
    
    public boolean is_error()
    { return _error ; }
    
    public String get_text()
    { return _text ; }
  }
  
  class _TREE_SELECTION_
  extends MouseAdapter
  implements TreeSelectionListener , 
             PythonTreeNodeEventListener
  {
    private DefaultMutableTreeNode _curNode ; 
    private PythonTreeNode _selected ; 
    
    /**
    *  just capture double click gesture on a given node 
    * 
    */
    public void mouseClicked(MouseEvent e) 
    {
      if (e.getClickCount() == 2) 
      {
      JTree curTree = (JTree)e.getComponent();
      TreePath path = curTree.getPathForLocation(e.getX(), e.getY());
        _curNode= (DefaultMutableTreeNode)path.getLastPathComponent() ;  
        if ( _curNode.getClass() == _MUTABLE_ROOT_NODE_.class) 
          // Double click on top => force reload of tree
          populateCurrent(true) ;  
        else 
        {
	      // Jump to current source line 
	     PythonDebugParameters.ideFront.displaySourceLine( _selected.getSourceName() ,
	                                                       _selected.getLineNo()
	                                                    ) ;
        }
      }
    }  
    
    public void newTreeNodeEvent( PythonTreeNodeEvent evt )
    {
    PythonSyntaxTreeNode syntaxNode = evt.get_node() ; 
    // save previously resolved location and short name
    syntaxNode.set_location(_selected.get_location());
    syntaxNode.set_nodeName(_selected.getShortString()) ;
    PythonTreeNode newNode = new PythonTreeNode(_currentSource, syntaxNode ) ; 
      // make node point to new Semantics
      _curNode.setUserObject(newNode) ; 
      // build imports node Tree
	  if ( syntaxNode.get_imports() != null )
        populateTree( _curNode , 
                      syntaxNode.get_imports() , 
                      PythonDebugParameters.get_jythonActivated()
                    ) ; 
	  // even if the imports are empty the icon needs to be changed
      _model.nodeChanged(_curNode) ; 
      JPYPythonParser.removePythonTreeNodeEventListener(this) ;
    }
    
    public void valueChanged( TreeSelectionEvent evt )
    {
    TreePath path = evt.getPath() ;
      _curNode = (DefaultMutableTreeNode)path.getLastPathComponent() ; 
      Object curObject = _curNode.getUserObject() ;
      if ( curObject instanceof PythonTreeNode )
        _selected = (PythonTreeNode)curObject ;
      else
        return ; 
      
      // deal only with module nodes 
      if ( _selected.get_type() != PythonSyntaxTreeNode.MODULE_TYPE )
        return ; 
      
      if (PythonDebugParameters.get_debugTrace())
        PythonDebugParameters.ideFront.logDebug( this,"selected import tree node =" + _selected.getShortString() ) ;
      
      String source = _selected.get_location() ;
      if ( source != null  ) 
      {  
        source =  _pythonPathPane.locatePythonSource(source) ; 
      }    
      _model.nodeChanged(_curNode) ; 
    }
  }
  
  class _IMPORT_CELL_RENDERER_
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
	  
	  if ( nodeValue instanceof _TEXT_NODE_  )
	  {
	  _TEXT_NODE_ cur =  (_TEXT_NODE_)nodeValue ; 
	    setText( cur.get_text() );
	    if ( cur.is_root())
	      setIcon(PythonDebugContainer.PYTHON_ICON) ; 
	    if ( cur.is_error())
	      setIcon(PythonDebugContainer.ERROR_ICON) ; 
	  }
	  else if ( nodeValue instanceof PythonTreeNode )
	  {
	  PythonTreeNode cur = (PythonTreeNode) nodeValue ; 
	    setText(cur.getShortString()) ; 
	    setIcon(cur.getIcon() ) ;
	  }
	  
      return this ;
	}
  }
  
  private void scanElements( DefaultMutableTreeNode root , 
                             Object list 
                           ) 
  {
    if ( list == null )
      return ; 
    Enumeration elements = null ;	
    if ( list instanceof Hashtable )
      elements = ((Hashtable)list).elements() ; 
    else if ( list instanceof Vector )  
      elements = ((Vector)list).elements() ; 
    while ( elements.hasMoreElements() )
      populateTree( root , (PythonSyntaxTreeNode)elements.nextElement() , 
                    PythonDebugParameters.get_jythonActivated()
                  ) ; 	
  }

  
  private void populateTree( DefaultMutableTreeNode root , 
                             PythonSyntaxTreeNode node ,
                             boolean isJython
                           )
  {
  DefaultMutableTreeNode parent = null ; 
    
    if ( node == null )
      // this may be the case when a non python source is
      // current Buffer and import navigator is selected
      return ; 
    
    switch ( node.get_type() )
    {	
      case PythonSyntaxTreeNode.MODULE_TYPE :
	    PythonTreeNode curNode ;
	    if ( isJython )
	    {
	     curNode = new JythonTreeNode(null,node,_jython) ;  
	     curNode.identify() ;
	    }   
	    else 
	    {  
	     curNode = new PythonTreeNode(_currentSource,node) ;  
	     curNode.identify() ;
	    }  
	    parent=  new DefaultMutableTreeNode(curNode ) ;
	    root.add(parent) ;
	    // scan imports
	    if ( node.get_imports() != null )
	      populateTree( parent , node.get_imports() , isJython ) ;
	    //  scan classes
	    scanElements( parent , node.get_classList()) ; 
	    //  scan stand alone methods
	    scanElements( parent , node.get_methodList() ) ; 	 
	    break ; 
		  
       case PythonSyntaxTreeNode.IMPORT_TYPE :
	     parent=  new DefaultMutableTreeNode( new PythonTreeNode(node)) ;
	     root.add(parent) ;
	     scanElements( parent , node.get_moduleList()) ; 
	     break ; 
	   case PythonSyntaxTreeNode.CLASS_TYPE :
	      parent = new DefaultMutableTreeNode( new  PythonTreeNode(_currentSource,node)) ;
	      root.add(parent) ;
	      // scan child methods 
	      scanElements( parent , node.get_methodList() ) ; 
	      break ; 
		  
	   case PythonSyntaxTreeNode.METHOD_TYPE :
	      parent = new DefaultMutableTreeNode(new  PythonTreeNode(_currentSource,node)) ;
	      root.add(parent) ;
	      // scan arguments methods 
	      scanElements( parent , node.get_argList() ) ; 
	      break ; 
		
	   case PythonSyntaxTreeNode.ARG_TYPE :
	      parent = new DefaultMutableTreeNode(new PythonTreeNode(_currentSource,node));
	      root.add(parent) ;
          break ; 	 
    }	
  }
  
  public void initialTreePanelInit()
  {
    if ( _treePanelInited )
      return ; 
    populateCurrent(true) ;  
    _treePanelInited = true ; 
    // add scroll to pane 
    JScrollPane p = new JScrollPane() ;
    p.setViewportView(_importsTree) ;
    _pythonPathPane.init(PythonDebugParameters.get_jythonActivated()) ;
    add( BorderLayout.CENTER , p ) ;
  }
  
  public  void populateCurrent(boolean refresh )
  {
    // synchronize the python path 
    _pythonPathPane.readPath(PythonDebugParameters.get_jythonActivated()) ; 
    _pythonPathPane.reloaded(PythonDebugParameters.get_jythonActivated()) ; 
    
    if ( _currentSource == null )
      _currentSource = PythonDebugParameters.ideFront.getCurrentSource()  ; 
    if ( _currentSource != null )
    {  
      if ( _currentSource.endsWith(_PYTHON_SUFFIX_) )
      {  
        if ( PythonDebugParameters.get_jythonActivated() )
        {  
          _jython.set_Path( _pythonPathPane.getPath() ) ;
          String srcDir =  new File(_currentSource).getParent() ;
	      _jython.add_Path(srcDir) ;
        }  
        // synchronize with the sidekick tree
        _rootNode.removeAllChildren() ;
        try {
          PythonSyntaxTreeNode curNode = PythonInspector.launchInspector( _currentSource , 
                                                                          refresh
                                                                        ) ; 
          // process pyLint if requested 
          PylintInspector.launchPyLint(_currentSource) ; 
          if ( curNode != null )
          {  
            curNode.set_location (_currentSource) ;
            // launching Pylint inspector here is not requested (poluting Import navigator switch)
            populateTree( _rootNode , 
                          curNode ,
                          PythonDebugParameters.get_jythonActivated()
                        ) ; 
          }
        } catch ( PythonDebugException e )
        { _rootNode.add( new DefaultMutableTreeNode( new _TEXT_NODE_( e.getMessage(),false,true))) ;  }
        // reload tree view
        _model.reload() ; 
      }  
    }  
  }  
}

