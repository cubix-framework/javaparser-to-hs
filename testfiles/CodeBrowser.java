/******************************************************************************
*   Copyright 2001, 2002, 2003, 2004 BITart Gerd Knops. All rights reserved.
*
*   Project   : CodeBrowser
*   File   : CodeBrowser.java
*   Author   : Gerd Knops gerti@BITart.com
*
*******************************************************************************
*                                    :mode=java:folding=indent:collapseFolds=1:
*   History:
*   020510 Creation of file
*
*******************************************************************************
*
*   Description:
*   Main class for the CodeBrowser plugin
*
*   $Id: CodeBrowser.java 7140 2006-09-27 19:36:10Z bambara $
*
*******************************************************************************
*
* DISCLAIMER
*
* BITart and Gerd Knops make no warranties, representations or commitments
* with regard to the contents of this software. BITart and Gerd Knops
* specifically disclaim any and all warranties, wether express, implied or
* statutory, including, but not limited to, any warranty of merchantability
* or fitness for a particular purpose, and non-infringement. Under no
* circumstances will BITart or Gerd Knops be liable for loss of data,
* special, incidental or consequential damages out of the use of this
* software, even if those damages were forseeable, or BITart or Gerd Knops
* was informed of their potential.
*
******************************************************************************/
package com.bitart.codebrowser;
/******************************************************************************
* Imports
******************************************************************************/

   import org.gjt.sp.jedit.*;
   import org.gjt.sp.jedit.msg.*;

   import org.gjt.sp.jedit.gui.*;
   import org.gjt.sp.jedit.textarea.*;
   import org.gjt.sp.jedit.search.*;
   
   import javax.swing.*;
   import javax.swing.tree.*;
   import javax.swing.event.*;
   import javax.swing.border.*;
   
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;

/*****************************************************************************/
public class CodeBrowser extends JPanel implements EBComponent
{
/**
    * 
    */
   private static final long serialVersionUID = 1L;

/******************************************************************************
* Vars
******************************************************************************/

   static public boolean DEBUG=false;
   
   View         currentView;
   Buffer         currentBuffer;
   JEditTextArea   currentTextArea;
   
   // UI
   static Font cellFont=null;
   static Font leafFont=null;
   
   JTree                  tree=null;
   CBRoot                  root=null;
   Object               previousNode = null;
   JList                  bufferList=null;
   JSplitPane               splitPane;
   private JButton          parseManualButton;

/******************************************************************************
* Factory methods
******************************************************************************/
public CodeBrowser(View view,Buffer buffer,String position)
   {
      currentView=view;
      currentBuffer=buffer;
      
      buildTree();
      buildBufferList();
      
      JScrollPane bufferScrollPane=new JScrollPane(bufferList);
      Dimension minimumSize=new Dimension(20,0);
      bufferScrollPane.setMinimumSize(minimumSize);
      bufferScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      
      JPanel treePanel=new JPanel();
      treePanel.setLayout(new BorderLayout());
      treePanel.add(new JScrollPane(tree),BorderLayout.CENTER);
      
      parseManualButton=new JButton("Parse");
      parseManualButton.setMargin(new Insets(2,2,2,2));
      if(OperatingSystem.isMacOSLF())
            parseManualButton.putClientProperty("JButton.buttonType","toolbar");
      parseManualButton.setToolTipText("manual parse: parse current buffer once");
      parseManualButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
            doParse(currentView.getBuffer(),true);
            currentTextArea.grabFocus();
         }
      });
      parseManualButton.setEnabled(!jEdit.getBooleanProperty("codebrowser.parse_automatic"));
      
      JPanel p=new JPanel();
      p.setBorder(new EmptyBorder(0,0,2,0));
      p.setOpaque(false);
      p.setLayout(new BorderLayout());
      p.add(parseManualButton,BorderLayout.WEST);
      p.add(new OptionsMenuButton(),BorderLayout.EAST);
      treePanel.add(p,BorderLayout.NORTH);
      
      splitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,bufferScrollPane,treePanel);
      splitPane.setOneTouchExpandable(true);
      splitPane.setDividerLocation(jEdit.getIntegerProperty("codebrowser.divider_location",150));
      splitPane.setLastDividerLocation(jEdit.getIntegerProperty("codebrowser.last_divider_location",150));
      
      setLayout(new BorderLayout());
      add(splitPane,BorderLayout.CENTER);
      
      this.addComponentListener(new ComponentAdapter(){
         public void componentResized(ComponentEvent e)
         {
            doParse(currentBuffer);
         }
         public void componentShown(ComponentEvent e)
         {
            doParse(currentBuffer);
         }
      });
      
      doParse(buffer,true);
      
      currentTextArea=view.getTextArea();
      currentTextArea.addCaretListener(new CaretHandler());
      //currentTextArea.addScrollListener(new ScrollHandler(currentTextArea));
   }

void buildTree()
   {
      if(tree!=null) return;
      
      DefaultTreeCellRenderer renderer;
      tree=new JTree();
      tree.setCellRenderer(renderer=new DefaultTreeCellRenderer()
      {
         
         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus
         )
         {
            Component c=super.getTreeCellRendererComponent(
               tree,value,sel,expanded,leaf,row,hasFocus);
            if(cellFont==null)
            {
               Font leafFont=c.getFont();
               if(leafFont==null) leafFont=tree.getFont();
               cellFont=leafFont.deriveFont(Font.BOLD|Font.ITALIC);
            }
            c.setFont((leaf)?leafFont:cellFont);
            
            if(value instanceof CBLeaf) setToolTipText(((CBLeaf)value).getToolTipText());
            
            
            return c;
         }
      });
      renderer.setLeafIcon(null);
      renderer.setOpenIcon(null);
      renderer.setClosedIcon(null);
      
      tree.setRootVisible(false);
      tree.setScrollsOnExpand(true);
      tree.setShowsRootHandles(true);
      
      tree.putClientProperty("JTree.lineStyle","Angled");
      
      tree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      
      tree.addMouseListener(new MouseHandler());
      
      tree.addTreeExpansionListener(new TreeExpansionListener()
      {
         public void treeExpanded(TreeExpansionEvent e)
         {
            remember(e,true);
         }
         
         public void treeCollapsed(TreeExpansionEvent e)
         {
            remember(e,false);
         }
         
         void remember(TreeExpansionEvent e, boolean flag)
         {
            Object o=e.getPath().getLastPathComponent();
            if(o instanceof CBType)
            {
               ((CBType)o).setState(flag);
            }
         }
      });
      
      ToolTipManager.sharedInstance().registerComponent(tree);
   }
   

void buildBufferList()
   {
      if(bufferList!=null) return;
      
      bufferList=new JList()
      {
         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         public String getToolTipText(MouseEvent e)
         {
            int index=locationToIndex(e.getPoint());
            Buffer b;
            if(index>=0 && (b=(Buffer)(getModel().getElementAt(index)))!=null)
            {
               return b.getPath();
            }
            
            return null;
         }
      };
      bufferList.setCellRenderer(new CBListCellRenderer());
      bufferList.setFont(tree.getFont());
      
      MouseListener mouseListener=new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            int index=bufferList.locationToIndex(e.getPoint());
            Buffer b;
            if(index>=0 && (b=(Buffer)(bufferList.getModel().getElementAt(index)))!=null)
            {
               if(e.isControlDown())
               {
                  //
                  // Close Buffer
                  //
                  if(DEBUG) System.err.println("Closing buffer "+b);
                  jEdit.closeBuffer(currentView,b);
               }
               else
               {
                  //
                  // Select buffer
                  //
                  currentView.setBuffer(b);
               }
            }
            e.consume();
         }
      };
      bufferList.addMouseListener(mouseListener);
      ToolTipManager.sharedInstance().registerComponent(bufferList);
      
      Dimension minimumSize=new Dimension(20,0);
      bufferList.setMinimumSize(minimumSize);
   }
/******************************************************************************
* Implementation
******************************************************************************/
void doParse(Buffer buffer)
   {
      if(!jEdit.getBooleanProperty("codebrowser.parse_automatic",true)) return;
      doParse(buffer,false);
   }

void doParse(Buffer buffer,boolean force)
   {
      if(buffer==null) return;
      if(buffer.getMode()==null) return;
      
      if(isVisibleInDock())
      {
         //System.err.println("Parsing!!!");
         currentBuffer=buffer;
         
         //System.err.println("buffer: "+buffer);
         //System.err.println("mode  : "+buffer.getMode());
         String m=buffer.getMode().getName();
         //System.err.println("Mode: "+m);
         
         String path=buffer.getPath();
         //System.err.println("Will parse: "+path);

         // get parsing result from history, if available
         root=ParserHistory.getEntry(path,m);
         if(root!=null)
         {
            tree.setModel(new DefaultTreeModel(root));
         }
         else
         {
            boolean tmpUsed=false;
            File f=null;
            if(buffer.getPath()==null)   // Probably remote file such as ftp or sftp
            {
               String prefix=buffer.getName();
               String suffix=null;
               int idx=prefix.indexOf(".");
               if(idx>0)
               {
                  suffix=prefix.substring(idx);
                  prefix=prefix.substring(0,idx);
               }
               
               try
               {
                  f=File.createTempFile(
                     prefix,
                     suffix
                  );
               
                  FileWriter fw=new FileWriter(f);
                  int size=buffer.getLength();
                  int offset=0;
                  while(size>0)
                  {
                     int c=16*1024;
                     if(c>size) c=size;
                     fw.write(buffer.getText(offset,c));
                     offset+=c;
                     size-=c;
                  }
                  fw.close();
                  
                  tmpUsed=true;
               }
               catch(Exception e)
               {
               }
               if(tmpUsed) path=f.getAbsolutePath();
            }
            tree.setModel(new DefaultTreeModel(root=new CBRoot(path,m)));
            ParserHistory.setEntry(buffer.getPath(), m, root);
            if(tmpUsed) f.delete();
         }
         //System.err.println("Root: "+root);
         //System.err.println("Model: "+tree.getModel());
         
         //
         // Expand types user last had expanded for this language
         //
         root.setSorted(jEdit.getBooleanProperty("codebrowser.do_sort",true),null);
         root.expandPaths(tree);
         updateBuffers();
         HighlightCurrentTag(currentTextArea.getCaretLine());
      }
   }
   
void updateBuffers()
   {
      bufferList.setFont(tree.getFont());
      bufferList.setListData(jEdit.getBuffers());
      bufferList.setSelectedValue(currentBuffer,true);
   }
   
boolean isDocked()
   {
      return currentView.getDockableWindowManager().isDockableWindowDocked("codebrowser");
   }
   
boolean isVisibleInDock()
   {
      return currentView.getDockableWindowManager().isDockableWindowVisible("codebrowser");
   }

void HighlightCurrentTag(int caretPos){
   Object[] path = new Object[3];
   Object o = new Object();
   int leafPosition;
   int minimum = currentTextArea.getBufferLength();
   int positionDiff = 0;
   CBLeaf result = null;
   TreeNode tempTreeNode = null;
   for(int index=0;index<root.getChildCount();index++){
      for(int index2=0;index2<root.getChildAt(index).getChildCount();index2++){
         o = root.getChildAt(index).getChildAt(index2);
         if(o instanceof CBLeaf){
            CBLeaf leaf=(CBLeaf)o;
            leafPosition = leaf.getPatternPosition();
            positionDiff = caretPos-leafPosition;
            if(positionDiff<0) continue;
            if(positionDiff<minimum){
               minimum = positionDiff;
               result = leaf;
               tempTreeNode = root.getChildAt(index);
            }
         }
      }
   }
   if(result!=null){
      if(DEBUG) System.err.println("nearest Tag is "+result.getName());
      path[0]=tree.getModel().getRoot();
      path[1]=tempTreeNode;
      path[2]=result;
      TreePath path1 = new TreePath(path);
      tree.setSelectionPath(path1);
   }
}
/******************************************************************************
* jEdit plugin methods
******************************************************************************/
public void addNotify()
    {
      if(DEBUG) System.err.println("addNotify!");
      super.addNotify();
      EditBus.addToBus(this);
      doParse(currentBuffer);
    }

public void removeNotify()
    {
       if(DEBUG) System.err.println("removeNotify!");
      jEdit.setIntegerProperty("codebrowser.divider_location",splitPane.getDividerLocation());
      jEdit.setIntegerProperty("codebrowser.last_divider_location",splitPane.getLastDividerLocation());
      super.removeNotify();
        EditBus.removeFromBus(this);
    }
   
/******************************************************************************
* EBComponent interface
******************************************************************************/
public void handleMessage(EBMessage message)
   {
      //System.err.println("EBMessage: "+message);
      if(message instanceof EditPaneUpdate)
      {
         EditPaneUpdate epu=(EditPaneUpdate)message;
         //EditPane editPane=(EditPane)(message.getSource());
         EditPane editPane=epu.getEditPane();
         JEditTextArea textArea=editPane.getTextArea();
         if(epu.getWhat()==EditPaneUpdate.BUFFER_CHANGED)
         {
            if(!isDocked() || currentView==editPane.getView())
            {
               //System.err.println("EBMessage will cause parse");
               currentView=editPane.getView();
               currentTextArea=textArea;
               currentBuffer = currentView.getBuffer();
               bufferList.setSelectedValue(currentBuffer,true);
               doParse(currentBuffer);
            }
         }
         else if(epu.getWhat()==EditPaneUpdate.CREATED)
         {
         }
      }
      else
      if(message instanceof BufferUpdate)
      {
         BufferUpdate bu=(BufferUpdate)message;
         if(bu.getWhat()==BufferUpdate.SAVED
            || bu.getWhat()==BufferUpdate.PROPERTIES_CHANGED
            || bu.getWhat()==BufferUpdate.LOADED)
         {
            Buffer b=bu.getBuffer();
            // buffer changed, so last parsing is obsolete
            ParserHistory.removeEntry(b.getPath(), b.getMode().getName());
            if(b==currentBuffer) doParse(b);
         }
         else
         if(bu.getWhat()==BufferUpdate.CLOSED
            || bu.getWhat()==BufferUpdate.CREATED
            || bu.getWhat()==BufferUpdate.DIRTY_CHANGED
            || bu.getWhat()==BufferUpdate.PROPERTIES_CHANGED
         )
         {
            updateBuffers();
         }
      }
      // Only supported in jEdit 4.1 and later
      else
      if(message instanceof ViewUpdate)
      {
         ViewUpdate vu=(ViewUpdate)message;
         if(vu.getWhat()==ViewUpdate.EDIT_PANE_CHANGED)
         {
            View view=(View)vu.getSource();
            if(!isDocked() || currentView==view)
            {
               currentBuffer=view.getBuffer();
               bufferList.setSelectedValue(currentBuffer,true);
               doParse(currentBuffer);
            }
         }
      }
   }
   
/******************************************************************************
* MouseHandler, Context- and Options menu
******************************************************************************/
class MouseHandler extends MouseAdapter
   {
      //{{{ mousePressed() method
      public void mousePressed(MouseEvent evt)
      {
         if(evt.isConsumed()) return;

         TreePath path1=tree.getPathForLocation(evt.getX(),evt.getY());
         if(path1==null) return;
         
         //tree.setSelectionPath(path1);
         
         if(GUIUtilities.isPopupTrigger(evt))
         {
            showPopupMenu(evt);
         }
         else
         {
            goToSelectedNode();
         
            // Deselect Tree
            //tree.clearSelection();
            
            // Close dock
            if(isVisibleInDock() && jEdit.getBooleanProperty("codebrowser.autodockclose",false))
            {
               currentView.getDockableWindowManager().closeCurrentArea();
            }
            
            // focus current textarea
            currentTextArea.grabFocus();
            //System.err.println("Auto-unfold: "+jEdit.getBooleanProperty("codebrowser.do_unfold",true));
            if(jEdit.getBooleanProperty("codebrowser.do_unfold",true))
            {
               currentTextArea.expandFold(true);
               
               Selection[] sel=currentTextArea.getSelection();
               int pos=currentTextArea.getCaretPosition();
               
               currentTextArea.goToNextLine(true);
               currentTextArea.expandFold(true);
               
               currentTextArea.setCaretPosition(pos);
               currentTextArea.setSelection(sel);
            }
         }
      } //}}}

      //{{{ Private members
      private JPopupMenu popupMenu;

      //{{{ showPopupMenu method
      private void showPopupMenu(MouseEvent evt)
      {
         if (popupMenu == null)
         {
            popupMenu = new JPopupMenu();
            popupMenu.add(new InsertAction());
            popupMenu.add(new HyperSearchAction());
            popupMenu.add(new CopyAction());
            popupMenu.add(new CopyAppendAction());
         }

         GUIUtilities.showPopupMenu(popupMenu,evt.getComponent(),
            evt.getX(),evt.getY());
         evt.consume();
      } //}}}
      
      // former method of TreeSelectionListener: valueChanged(TreeSelectionEvent e)
      private int repeatCount = 0;
      private void goToSelectedNode()
      {
         Object o=tree.getLastSelectedPathComponent();
         if (o != previousNode) repeatCount = 0;
         else ++repeatCount;
         previousNode = o;

         if(o instanceof CBLeaf)
         {
            EditPane pane = currentView.getEditPane();
            EditBus.send(new CaretChanging(currentView.getTextArea()));
            CBLeaf leaf=(CBLeaf)o;
            
            String pattern=leaf.getPattern();
            
            // Save current Serach panel values
            String searchString=SearchAndReplace.getSearchString();
            boolean ignoreCase=SearchAndReplace.getIgnoreCase();
            boolean useRegexp=SearchAndReplace.getRegexp();
            boolean reverse=SearchAndReplace.getReverseSearch();
            boolean bean=SearchAndReplace.getBeanShellReplace();
            boolean wrap=SearchAndReplace.getAutoWrapAround();
            
            // Set for our search
            SearchAndReplace.setSearchString(pattern);
            SearchAndReplace.setIgnoreCase(false);
            SearchAndReplace.setRegexp(true);
            SearchAndReplace.setReverseSearch(false);
            SearchAndReplace.setBeanShellReplace(false);
            SearchAndReplace.setAutoWrapAround(true);
            SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
            
            // Search
            if(DEBUG) System.err.println("Searching for: "+pattern);
            try
            {
               //find first
               SearchAndReplace.find(currentView,currentBuffer,0);
               for (int i=0; i<repeatCount; ++i) {
                  SearchAndReplace.find(currentView);
               }
               // If the pattern occurs more than once, find correct palce
               
               TreeNode parent=leaf.getParent();
               int idx=parent.getIndex(leaf);
               int i;
               for(i=0;i<idx;i++)
               {
                  if(pattern.equals(((CBLeaf)parent.getChildAt(i)).getPattern()))
                  {
                     if(DEBUG) System.err.println("(i="+i+")<"+idx+"; '"+pattern+"' eq '"+((CBLeaf)parent.getChildAt(i)).getPattern()+"'");
                     SearchAndReplace.find(currentView);
                  }
               }
               
            }
            catch(Exception e){
               throw new RuntimeException (e);
            }
            
            // Restore previous values
            SearchAndReplace.setSearchString(searchString);
            SearchAndReplace.setIgnoreCase(ignoreCase);
            SearchAndReplace.setRegexp(useRegexp);
            SearchAndReplace.setReverseSearch(reverse);
            SearchAndReplace.setBeanShellReplace(bean);
            SearchAndReplace.setAutoWrapAround(wrap);
            
         }
      }
      //}}}
   } //}}}
   
   //{{{ AutoParseAction class
class AutoParseAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public AutoParseAction()
      {
         super(jEdit.getProperty("codebrowser.optionsmenu.autoparse"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         JCheckBoxMenuItem mi=(JCheckBoxMenuItem)evt.getSource();
         boolean state=mi.getState();
         jEdit.setBooleanProperty("codebrowser.parse_automatic",state);
         parseManualButton.setEnabled(!state);
         if(state)
         {
            doParse(currentView.getBuffer());
         }
      }
   }//}}}
   
   //{{{ SortAction class
class SortAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public SortAction()
      {
         super(jEdit.getProperty("codebrowser.optionsmenu.sort"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         JCheckBoxMenuItem mi=(JCheckBoxMenuItem)evt.getSource();
         boolean state=mi.getState();
         jEdit.setBooleanProperty("codebrowser.do_sort",state);
         root.setSorted(state,tree);
      }
   }//}}}
   
   //{{{ AutoUnfoldAction class
class AutoUnfoldAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public AutoUnfoldAction()
      {
         super(jEdit.getProperty("codebrowser.optionsmenu.autounfold"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         JCheckBoxMenuItem mi=(JCheckBoxMenuItem)evt.getSource();
         boolean state=mi.getState();
         jEdit.setBooleanProperty("codebrowser.do_unfold",state);
      }
   }//}}}
   
   //{{{ AutoCloseDockAction class
class AutoCloseDockAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public AutoCloseDockAction()
      {
         super(jEdit.getProperty("codebrowser.optionsmenu.autodockclose"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         JCheckBoxMenuItem mi=(JCheckBoxMenuItem)evt.getSource();
         boolean state=mi.getState();
         jEdit.setBooleanProperty("codebrowser.autodockclose",state);
      }
   }//}}}
   
   //{{{ CopyAction class
class CopyAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public CopyAction()
      {
         super(jEdit.getProperty("codebrowser.action.copy"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         Object o=tree.getLastSelectedPathComponent();
         if(o instanceof CBLeaf)
         {
            Registers.setRegister('$', ((CBLeaf)o).getName());
         }
      }
   }//}}}
   
   //{{{ InsertAction class
class InsertAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public InsertAction()
      {
         super(jEdit.getProperty("codebrowser.action.insert"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         Object o=tree.getLastSelectedPathComponent();
         if(o instanceof CBLeaf)
         {
            currentTextArea.setSelectedText(((CBLeaf)o).getName());
         }
      }
   }//}}}
   
   //{{{ CopyAppendAction class
class CopyAppendAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public CopyAppendAction()
      {
         super(jEdit.getProperty("codebrowser.action.copy-append"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         Object o=tree.getLastSelectedPathComponent();
         if(o instanceof CBLeaf)
         {
            // code part copied/adapted from org.gjt.sp.jedit.Registers.append
            char register = '$';
            String separator = "\n";
            String selection = ((CBLeaf)o).getName();
            Registers.Register reg = Registers.getRegister(register);
            if(reg != null)
            {
               String registerContents = reg.toString();
               if(registerContents != null)
               {
                  if(registerContents.endsWith(separator))
                     selection = registerContents + selection;
                  else
                     selection = registerContents + separator + selection;
               }
            }
            Registers.setRegister(register,selection);
            HistoryModel.getModel("clipboard").addItem(selection);
         }
      }
   }//}}}

   //{{{ HyperSearchAction class
class HyperSearchAction extends AbstractAction
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      public HyperSearchAction()
      {
         super(jEdit.getProperty("codebrowser.action.hypersearch"));
      }
      public void actionPerformed(ActionEvent evt)
      {
         Object o=tree.getLastSelectedPathComponent();
         if(o instanceof CBLeaf)
         {
            //hypersearch with current options
            //don't reset search settings, because hypersearch runs in different thread
            SearchAndReplace.setSearchString(((CBLeaf)o).getName());
            SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
            SearchAndReplace.hyperSearch(currentView,false);
         }
      }
   }//}}}

   //{{{ OptionsMenuButton class
   
class OptionsMenuButton extends JButton
   {
      /**
    * 
    */
   private static final long serialVersionUID = 1L;
      OptionsMenu   popup;
      
      OptionsMenuButton()
      {
         setText(jEdit.getProperty("codebrowser.optionsmenu.label"));
         setIcon(GUIUtilities.loadIcon("ToolbarMenu.gif"));
         setHorizontalTextPosition(SwingConstants.LEADING);
         
         popup=new OptionsMenu();
         
         OptionsMenuButton.this.setRequestFocusEnabled(false);
         OptionsMenuButton.this.setMargin(new Insets(3,3,3,3));
         OptionsMenuButton.this.addMouseListener(new OptionsMouseHandler());

         if(OperatingSystem.isMacOSLF())
            OptionsMenuButton.this.putClientProperty("JButton.buttonType","toolbar");
      }
      
      //{{{ MouseHandler class
      
      class OptionsMouseHandler extends MouseAdapter
      {
         public void mousePressed(MouseEvent evt)
         {
            if(!popup.isVisible())
            {
               //popup.update();

               GUIUtilities.showPopupMenu(
                  popup,OptionsMenuButton.this,0,
                  OptionsMenuButton.this.getHeight(),
                  false);
            }
            else
            {
               popup.setVisible(false);
            }
         }
      } //}}}
      
      //{{{ OptionsMenu class
      class OptionsMenu extends JPopupMenu
      {
         /**
          * 
          */
         private static final long serialVersionUID = 1L;

         OptionsMenu()
         {
            JCheckBoxMenuItem mi;
            
            mi=new JCheckBoxMenuItem();
            mi.setState(jEdit.getBooleanProperty("codebrowser.parse_automatic",true));
            mi.setAction(new AutoParseAction());
            add(mi);
            
            mi=new JCheckBoxMenuItem();
            mi.setState(jEdit.getBooleanProperty("codebrowser.do_sort",true));
            mi.setAction(new SortAction());
            add(mi);
            
            mi=new JCheckBoxMenuItem();
            mi.setState(jEdit.getBooleanProperty("codebrowser.do_unfold",true));
            mi.setAction(new AutoUnfoldAction());
            add(mi);
            
            mi=new JCheckBoxMenuItem();
            mi.setState(jEdit.getBooleanProperty("codebrowser.autodockclose",false));
            mi.setAction(new AutoCloseDockAction());
            add(mi);
         }
      }

   }//}}}
   class CaretHandler extends EventListenerList implements CaretListener{
      
      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      public CaretHandler(){}
      
      public void caretUpdate(CaretEvent e){
         HighlightCurrentTag(currentTextArea.getCaretLine());
      }
      
   }
   
   /*class ScrollHandler extends EventListenerList implements ScrollListener{
      
      JEditTextArea textArea;
      
      public ScrollHandler(JEditTextArea text){
         textArea = text;
      }
      
      public void scrolledHorizontally(JEditTextArea textArea){
         
      }
      
      public void scrolledVertically(JEditTextArea textArea){
         System.err.println("Posi "+textArea.getMagicCaretPosition());
         System.err.println("1 "+textArea.getFirstPhysicalLine());
         System.err.println("1 "+textArea.getLastPhysicalLine());
         int caretPos = textArea.getFirstPhysicalLine()+
         ((textArea.getLastPhysicalLine()-textArea.getFirstPhysicalLine())/2);
         textArea.setMagicCaretPosition(caretPos);
         System.err.println("Pos "+caretPos);
      }
   }*/
   
}

/*************************************************************************EOF*/


