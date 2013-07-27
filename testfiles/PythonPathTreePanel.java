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

package org.jymc.jpydebug.swing.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.* ; 
import javax.swing.tree.*;

import org.jymc.jpydebug.PythonPath;
import org.jymc.jpydebug.PythonPathElement;

  
/**
 * PythonPath Tree representation 
 * 
 * Python System Path 
 * 
 * Complementary User Paths
 * 
 * @author jean-yves
 *
 */
class _MUTABLE_ROOT_NODE_
extends DefaultMutableTreeNode
{
  
  public _MUTABLE_ROOT_NODE_( Object dataNode  )
  { 
    super(dataNode) ; 
  }
}

class _SELECTABLE_ROOT_NODE_
extends DefaultMutableTreeNode
{
  public String getToolTipText()
  { return ("select node and right click for PYTHONPATH context menu actions") ;  }
  
  public _SELECTABLE_ROOT_NODE_( Object dataNode )
  { 
    super(dataNode) ; 
  }
}

class _PYTHONPATH_ELEMENT_NODE_
extends DefaultMutableTreeNode
{
  public _PYTHONPATH_ELEMENT_NODE_( PythonPathElement dataNode )
  { super(dataNode) ; }
  
  public String getNodeName() 
  { return ((PythonPathElement) super.getUserObject()).get_value() ; }
}



class _PYTHONLIB_ELEMENT_NODE_
extends DefaultMutableTreeNode
{
  public _PYTHONLIB_ELEMENT_NODE_( File lib )
  { super(lib) ; }
  
  public String getNodeName() 
  { return ((File) super.getUserObject()).getAbsolutePath() ; }
}

class _TEXT_NODE_
{
  private String _text ; 
  private boolean _root ; 
  
  public _TEXT_NODE_( String text , boolean root  )
  { 
    _text = text ; 
    _root = root ; 
  }
  
  public boolean is_root()
  { return _root ; }
  
  
  public String get_text()
  { return _text ; }
}


public class PythonPathTreePanel
extends JPanel 
{
  private final static String _JAR_SUFFIX_ = ".jar" ;
  private final static String _DLL_SUFFIX_ = ".dll" ;
  private final static String _PYD_SUFFIX_ = ".pyd" ;
  private final static String _SO_SUFFIX_= ".so" ;
  private final static String _PYTHONPATH_ = "PYTHONPATH" ;
  private final static String _PYTHON_SYSTEM_PATH_ = "Python System Libs" ;
  // private final static String _PYTHON_USER_PATH_ = "Python User Libs" ;
  private final static String _PYTHON_COMPILED_LIBS_ = "Compiled Libs founds in PYTHONPATHes(dlls)" ;
  
  private final static String _REMOVE_ = "remove" ; 
  private final static String _INSERT_ = "add" ; 
  private final static String _INSERT_JAR_ = "add jar" ; 
  private final static String _SAVE_ = "save" ; 

  private final static PythonPathTreePanel _DUMMY_ = new PythonPathTreePanel() ;
  
  public final static ImageIcon PYTHONPATH_ICON  =  new ImageIcon(
      _DUMMY_.getClass().getResource("images/python16.jpg") ,
                             "pythonpath"
   ) ;
  public final static ImageIcon PYTHONPATH_FOLDER  =  new ImageIcon(
      _DUMMY_.getClass().getResource("images/pythonpathfolder.gif") ,
                             "pythonpath"
   ) ;
  public final static ImageIcon PYTHONPATH_PATH  =  new ImageIcon(
      _DUMMY_.getClass().getResource("images/pythonpathpath.gif") ,
                             "pythonpath"
   ) ;
  
  private JTree _pathTree  ;
  private PythonPath _path  = new PythonPath() ; 
  private JScrollPane _scroller = null ;
  private DefaultTreeModel _model  ;
  private _MUTABLE_ROOT_NODE_ _rootNode = new _MUTABLE_ROOT_NODE_(new _TEXT_NODE_(_PYTHONPATH_,true)) ; 
  // private boolean _isJython = false ; 
  private _SELECTABLE_ROOT_NODE_ _systemPathNode = new _SELECTABLE_ROOT_NODE_(new _TEXT_NODE_(_PYTHON_SYSTEM_PATH_,false)) ;
  private DefaultMutableTreeNode _libsNode = new DefaultMutableTreeNode(new _TEXT_NODE_(_PYTHON_COMPILED_LIBS_,false)) ; 
  
  private _BUTTON_PANE_ _buttonPane = new _BUTTON_PANE_() ; 
  
  
  
  class _PYTHONPATH_NODE_RENDERER_
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
          setIcon(PYTHONPATH_ICON) ; 
        else 
          setIcon(PYTHONPATH_FOLDER ) ;
      }
      else if ( nodeValue instanceof PythonPathElement )  
      {
        PythonPathElement cur = (PythonPathElement) nodeValue ; 
        setText(cur.get_value()) ; 
        setIcon(PYTHONPATH_PATH ) ;
      }
      else if ( nodeValue instanceof File )
      {
        File cur = (File) nodeValue ; 
        setText(cur.getAbsolutePath()) ; 
        setIcon(PYTHONPATH_PATH ) ;
      }
      return this ;
    }
  }
  
  class _LIB_FILEFILTER_
  implements FileFilter
  {
    private boolean _isJython ; 
    
    public _LIB_FILEFILTER_ ( boolean isJython )
    { _isJython = isJython ; }
    
    public boolean accept(File candidate ) 
    {
      if ( candidate .isDirectory())
        return false ; 
      
      if ( _isJython )
      {  
        if ( candidate.getName().endsWith(_JAR_SUFFIX_) ) 
          return true ; 
      }  
      else 
        if (  candidate.getName().endsWith(_SO_SUFFIX_)  ||
              candidate.getName().endsWith(_PYD_SUFFIX_)  ||
              candidate.getName().endsWith(_DLL_SUFFIX_) 
            ) 
          return true ; 
      return false ; 
    }
  }

  class _FIXED_BUTTON_LISTENER_
  implements ActionListener 
  {
    private boolean _isJython = false ; 
    
    public void  set_jython( boolean isjython )
    { _isJython = isjython ; }
    
    
    private void selectPath( DefaultMutableTreeNode dmtn, int type )
    {
      JFileChooser chooser = new JFileChooser() ;     
      chooser.setFileSelectionMode( type ) ;

      chooser.showOpenDialog(null) ; 
      File selFile = chooser.getSelectedFile() ; 
      
        
      if( selFile != null )
      {
        PythonPathElement cur = new PythonPathElement(selFile.getAbsolutePath()) ; 
        dmtn.add( new _PYTHONPATH_ELEMENT_NODE_(cur)) ;
        // populate to path
        _path.addElement(cur) ;
        // refresh display when children added 
        ((DefaultTreeModel )_pathTree.getModel()).nodeStructureChanged(dmtn); 
      }
    }
    
    public void actionPerformed(ActionEvent ae) 
    {
    DefaultMutableTreeNode dmtn, node;

    TreePath path = _pathTree.getSelectionPath();
      if ( path == null )
      {
        // default to system root
        _pathTree.setSelectionRow(1) ;
        path = _pathTree.getSelectionPath(); 
      }
      if ( path != null )
      {  
        dmtn = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (ae.getActionCommand().equals(_INSERT_JAR_)) 
          selectPath(_systemPathNode,JFileChooser.FILES_ONLY) ; 
        if (ae.getActionCommand().equals(_INSERT_)) 
          selectPath(_systemPathNode,JFileChooser.DIRECTORIES_ONLY) ; 
        if (ae.getActionCommand().equals(_REMOVE_)) 
        {
        TreePath selected[] = _pathTree.getSelectionPaths() ; 
          for ( int ii=0 ; ii < selected.length ; ii++ ) 
          { 
          TreePath curPath = selected[ii] ; 
          TreePath parentPath = curPath.getParentPath() ; 
          node  =  (DefaultMutableTreeNode)curPath.getLastPathComponent() ;
          node.removeFromParent();
            _path.removeElementAt(_path.indexOf(node.getUserObject())) ;
        
            ((DefaultTreeModel )_pathTree.getModel()).nodeStructureChanged(dmtn);
            if ( _pathTree.isCollapsed(parentPath))
              _pathTree.expandPath(parentPath) ;
          }
        }
        if (ae.getActionCommand().equals(_SAVE_)) 
        {
          _path.writePath(_isJython) ;
        }
        else 
          refreshLibs(_isJython) ;
      }  
    }  
  } 
  
  class _FIXED_BUTTON_ 
  extends JButton
  {
     public _FIXED_BUTTON_(String id )
     {
       super(id) ; 
     }
     
     public Dimension getMinimumSize()
     { 
       return new Dimension(90,super.getMinimumSize().height )  ; 
     }
    
     public Dimension getMaximumSize()
     { 
       return new Dimension(90,super.getMaximumSize().height )  ; 
     }
    
  }
  
  class _BUTTON_PANE_
  extends Box
  {
    private _FIXED_BUTTON_ _addjar = new _FIXED_BUTTON_(_INSERT_JAR_) ; 
    private _FIXED_BUTTON_LISTENER_ _buttonAction = new _FIXED_BUTTON_LISTENER_() ;
    
    public _BUTTON_PANE_()
    {
      super(BoxLayout.Y_AXIS) ;
      super.setBounds(10,10,10,10) ;
      
      _FIXED_BUTTON_ add = new _FIXED_BUTTON_(_INSERT_) ; 
      add.addActionListener(_buttonAction) ;
      
      _addjar.addActionListener(_buttonAction ) ;
      
      _FIXED_BUTTON_ remove = new _FIXED_BUTTON_(_REMOVE_) ; 
      remove.addActionListener(_buttonAction) ;
      
      _FIXED_BUTTON_ save = new _FIXED_BUTTON_(_SAVE_) ; 
      save.addActionListener(_buttonAction) ;

      super.add(add)  ;
      super.add(_addjar) ;
        
      super.add( remove)  ; 
      super.add( save)  ; 
    }
    
    public void setJython( boolean enabled )
    { 
      _addjar.setEnabled(enabled) ; 
      _buttonAction.set_jython(enabled) ;
    }
    
  }
  
  
  public PythonPathTreePanel()
  {
    super.setLayout( new BorderLayout() ) ; 
  }
  
  public void init( boolean isJython )
  {
    _model = new DefaultTreeModel(_rootNode);
    _pathTree = new JTree(_model) ;
    _pathTree.setShowsRootHandles(true) ; 
    _pathTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION) ; 
    _pathTree.setCellRenderer( new _PYTHONPATH_NODE_RENDERER_() ) ;
    _pathTree.putClientProperty("JTree.lineStyle" , "Angled") ; 
    _pathTree.setExpandsSelectedPaths(true) ;
    TreeSelectionModel selModel = _pathTree.getSelectionModel() ;
    selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) ;
    _scroller = new JScrollPane(_pathTree) ;
    ToolTipManager.sharedInstance().registerComponent(_pathTree);

    
    // Initialy populate Path tree
    populateCurrent(isJython) ; 
    
    add( BorderLayout.CENTER , _scroller ) ; 
    add( BorderLayout.EAST , _buttonPane  ) ;
  }
  
  private Enumeration populatePaths( DefaultMutableTreeNode parent , boolean isJython)
  {
    _path.readPath(isJython) ;
    Enumeration pathList = _path.get_Path().elements() ; 
    while ( pathList.hasMoreElements() )
    {
    PythonPathElement cur = (PythonPathElement) pathList.nextElement() ;
      parent.add( new _PYTHONPATH_ELEMENT_NODE_(cur) ) ;
    }
    return _path.get_Path().elements() ;
  }
  
  private void populateLib( File dir , boolean isJython )
  {
    if ( dir.isDirectory() )
    {
    File dlls[] =   dir.listFiles(new _LIB_FILEFILTER_(isJython) ) ;  
      if ( dlls != null )
      {    
        for ( int ii = 0 ; ii < dlls.length ; ii++ )
          _libsNode.add( new _PYTHONLIB_ELEMENT_NODE_(dlls[ii]) ) ;
      }  
    }
  }
  
  private void populateLibs( Enumeration path ,   boolean isJython)
  {
    while ( path.hasMoreElements() )
    {
    PythonPathElement cur = (PythonPathElement) path.nextElement() ;
    File curDir = new File( cur.get_value()) ; 
      populateLib(curDir , isJython) ;
    }  
    
  }
  
  /**
   * On add remove paths action refresh tree DLL / Jar tree content
   */
  private void refreshLibs(boolean isJython)
  {
    _libsNode.removeAllChildren() ;  // cleanup first 
    Enumeration children = _systemPathNode.children() ; 
    while( children.hasMoreElements() ) 
    {
      _PYTHONPATH_ELEMENT_NODE_ cur = (_PYTHONPATH_ELEMENT_NODE_)  children.nextElement() ;
      populateLib( new File(cur.getNodeName()) , isJython ) ;       
    }
        
  }
  
  private void populateCurrent( boolean isJython )
  {
    _rootNode.removeAllChildren() ;
    _systemPathNode.removeAllChildren() ;
    _rootNode.add( _systemPathNode );

    Enumeration systemPaths = populatePaths(_systemPathNode , isJython) ;
    
    _rootNode.add( _libsNode);
   
    populateLibs(systemPaths , isJython ) ; 
    
    // refresh
    _model.nodeStructureChanged(_rootNode) ;
    
    // check for Jython jar Button
    _buttonPane.setJython( isJython ) ;
      
    
    if ( _pathTree.isCollapsed(new TreePath(_systemPathNode.getPath() ) ) )
      _pathTree.expandPath(new TreePath(_systemPathNode.getPath())) ;

  }
  
  public void readPath( boolean isJython )
  { _path.readPath(isJython) ; }
  
  public void writePath(boolean isJython )
  { _path.writePath(isJython) ; }

  public void reloaded(boolean isJython )
  { 
    if ( _pathTree != null ) // check for inited
      populateCurrent(isJython) ;  
  }
  
  public Vector getPath()
  { return _path.get_Path() ; }
  

  private DefaultMutableTreeNode lookFor( String source )
  {
  Enumeration children =  _systemPathNode.children() ; 
    while ( children.hasMoreElements()) 
    {
    _PYTHONPATH_ELEMENT_NODE_ cur = (_PYTHONPATH_ELEMENT_NODE_)  children.nextElement() ;
      if ( cur.getNodeName().equals(source) )
        return cur ; 
    }
    return null ;
  }
  
  public String locatePythonSource( String source )
  {
  PythonPathElement curP = _path.locatePythonSource(source) ;  
    if ( curP != null ) 
    {
      // select and view the corresponding source path treeNode
      DefaultMutableTreeNode located = lookFor(curP.get_value()) ;
      if ( located != null )
      {  
      TreePath locatedP = new TreePath(located.getPath() );
        _pathTree.scrollPathToVisible(locatedP) ;
        _pathTree.setSelectionPath(locatedP) ; 
      }
      return curP.get_candidate()  ;
    }
    // file may exist without matching path
    File f = new File(source) ; 
    if ( f.isFile() )
      return source ; 
    return null ; 
  }



  
  
  /**
   * Sample Unit testing
   * @param args
   */
  public static void main(String[] args)
  {
    JFrame myFrame = new JFrame(  "simple test" ) ; 
    
    PythonPathTreePanel dbg = new PythonPathTreePanel() ; 
    // PythonPathTreePanel dbg = new PythonPathTreePanel(true) ;  // Jython case
    
    dbg.init(true) ; 
    
     myFrame.addWindowListener( 
         new WindowAdapter() 
         {  public void windowClosing( WindowEvent e ) 
            { 
              System.exit(0) ;
            }  
         }        
       ) ; 
     myFrame.getContentPane().setLayout( new BorderLayout() )  ; 
     myFrame.getContentPane().add( BorderLayout.CENTER , dbg ) ;  
     myFrame.pack() ; 
     myFrame.setVisible(true) ; 

  }

}
