/*
* 20:47:04 04/06/00
*
* XTree.java - A tree used for XInsert system
* Original Version Copyright (C) 1999 Romain Guy - guy.romain@bigfoot.com
* Potion copyright (C) 2000 Richard Lowe
* Copyright (C) 2000 Dominic Stolerman - dominic@sspd.org.uk
* www.chez.com/powerteam/jext
* Changes (c) 2005 Martin Raspe - raspe@biblhertz.it
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

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.textarea.*;

public class XTree extends JPanel { 
  // the following was replaced by a private class "TreeListener"
  // implements TreeSelectionListener, ActionListener

  private XTreeTree tree;
  private HashMap map;
  private TreeListener treeListener;
  private ActionListener escapeListener = new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
		  view.getTextArea().requestFocus();
		  view.toFront();
	  	}
	};
  private View view;
  private static Vector inserts;
  private DefaultTreeModel treeModel;
  private JButton expand, collapse, reload;
  private JCheckBox carriageReturn, executeScript;
  private boolean treeJustCollapsed = false;

  // nested submenus
  private int rootIndex;
  private XTreeNode root;
  private Stack menuStack = null;
  private XTreeObject xtreeObj = null;
  
  //private int clicks;
  private int lineNo;

  // get access to tree widget (needed for XInsertActions)
  public XTreeTree getTree() {
    return tree;
    }

  public void addMenu(String nodeName) {
    xtreeObj = new XTreeObject(new XTreeNode(nodeName), 0);
    if (menuStack.empty()) {
      treeModel.insertNodeInto(xtreeObj.getXTreeNode(), root, rootIndex);
      rootIndex++;
      }
    else {
      //Log.log(Log.DEBUG, this, "Adding menu on menu stack: " + nodeName);
      XTreeObject obj = (XTreeObject) menuStack.peek();
      treeModel.insertNodeInto(xtreeObj.getXTreeNode(), obj.getXTreeNode(), obj.getIndex());
      obj.incrementIndex();
      }
    menuStack.push(xtreeObj);
    }

  public void closeMenu() {
    try {
      xtreeObj = (XTreeObject) menuStack.pop();
      }
    catch (Exception e) {
      xtreeObj = null;
      }
    }

  public void addVariable(String key, String value) {
    XTreeObject obj = (XTreeObject)menuStack.peek();
    XTreeNode node;
    if (obj != null)
      node = obj.getXTreeNode();
    else
      node = root;
    node.addVariable(key, value);
    }

  public void addInsert(String nodeName, String content, int script) {
    inserts.addElement(new XTreeItem(content, script));
    XTreeNode node = new XTreeNode(nodeName, inserts.size());
    if (xtreeObj == null) {
      treeModel.insertNodeInto(node, root, rootIndex);
      ++rootIndex;
      }
    else {
      XTreeObject obj = (XTreeObject) menuStack.peek();
      treeModel.insertNodeInto(node, obj.getXTreeNode(), obj.getIndex());
      obj.incrementIndex();
      }
    }

  public XTree(View view) {
    super();
    this.view = view;
    setLayout(new BorderLayout());

    /* Use default icons
    UIManager.put("Tree.expandedIcon", Utilities.getIcon("images/down_arrow.gif"));
    UIManager.put("Tree.collapsedIcon", Utilities.getIcon("images/right_arrow.gif"));
    UIManager.put("Tree.leftChildIndent", new Integer(5));
    UIManager.put("Tree.rightChildIndent", new Integer(7));
    */

    root = new XTreeNode("XInsert");
    treeModel = new DefaultTreeModel(root);
    tree = new XTreeTree(treeModel);
    ToolTipManager.sharedInstance().registerComponent(tree);
    tree.putClientProperty("JTree.lineStyle", "Angled");
    // tree.addTreeSelectionListener(this);
    // add a special tree listener
    treeListener = new TreeListener();
    tree.addMouseListener(treeListener);
    // respond to keyboard events
    tree.registerKeyboardAction(
      treeListener,
        KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
        WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    tree.registerKeyboardAction(
      treeListener,
        KeyStroke.getKeyStroke(' '),
        WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );
    tree.registerKeyboardAction(
      escapeListener,
	KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
	WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
	);
    tree.setCellRenderer(new XTreeCellRenderer());
    init();

    JPanel pane = new JPanel(new BorderLayout());
    //No mnemonics as they are confusing/ do not work when docked.

    pane.add(collapse = new JButton(Utilities.getIcon("images/button_collapse.gif")),BorderLayout.WEST);
    collapse.setToolTipText(jEdit.getProperty("xtree.collapse.button"));
    //collapse.setMnemonic(jEdit.getProperty("xtree.collapse.mnemonic").charAt(0));
    collapse.addActionListener(treeListener);

    pane.add(reload = new JButton(Utilities.getIcon("images/menu_reload.gif")),BorderLayout.CENTER);
    reload.setToolTipText(jEdit.getProperty("xtree.reload.button"));
    //reload.setMnemonic(jEdit.getProperty("xtree.reload.mnemonic").charAt(0));
    reload.addActionListener(treeListener);

    pane.add(expand = new JButton(Utilities.getIcon("images/button_expand.gif")),BorderLayout.EAST);
    expand.setToolTipText(jEdit.getProperty("xtree.expand.button"));
    //expand.setMnemonic(jEdit.getProperty("xtree.expand.mnemonic").charAt(0));
    expand.addActionListener(treeListener);

    add(pane, BorderLayout.NORTH);
    add(new JScrollPane(tree), BorderLayout.CENTER);

    JPanel optionPane = new JPanel(new BorderLayout());
    optionPane.add(carriageReturn = new JCheckBox(jEdit.getProperty("xtree.carriage.label")), BorderLayout.NORTH);
    carriageReturn.setSelected(jEdit.getBooleanProperty("xtree.carriage", false));
    carriageReturn.addActionListener(treeListener);

    optionPane.add(executeScript = new JCheckBox(jEdit.getProperty("xtree.execute.label")), BorderLayout.CENTER);
    executeScript.setSelected(jEdit.getBooleanProperty("xtree.execute", true));
    if (jEdit.getProperty("xtree.execute") == null)
      executeScript.setSelected(true);
    executeScript.addActionListener(treeListener);

    add(optionPane, BorderLayout.SOUTH);
    }

  private void init() {
    inserts = new Vector(200);
    menuStack = new Stack();
    rootIndex = 0;
    if(jEdit.getBooleanProperty("xinsert.display.all", true)) {
      int i =0;
      String current;
      while((current = jEdit.getProperty("xinsert.inserts." + i)) != null) {
        loadInternalInsert(current);
        i++;
        }
      }
    //Macros menu
    if(jEdit.getBooleanProperty("xinsert.display.macros", true)) {
      addMenu("Macros");
      Vector vec = Macros.getMacroHierarchy();
      Iterator iter = vec.iterator();
      while(iter.hasNext()) {
        Object o = iter.next();
        if(o instanceof Vector) {
          loadMacroVector((Vector)o);
          }
        else if(o instanceof String) {
          loadNamedMacro(Macros.getMacro((String)o));
          }
        else {
          loadNamedMacro((Macros.Macro)o);
          }
        }
      closeMenu();
      }
    build();
    fillMap();
    tree.expandRow(0);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    }

  private void loadMacroVector(Vector v) {
    Iterator iter = v.iterator();
    addMenu(iter.next().toString());
    while(iter.hasNext()) {
      Object o = iter.next();
      if(o instanceof Vector) {
        loadMacroVector((Vector)o);
        }
      else {
        loadNamedMacro(Macros.getMacro(o.toString())); //NDT fix...
        }
      }
    closeMenu();
    }

  private void loadNamedMacro(Macros.Macro macro) {
    addInsert(macro.getLabel(), macro.getName(), XTreeItem.NAMED_MACRO_TYPE);
    }

  private void loadInternalInsert(String fileName) {
    try {
      if(jEdit.getBooleanProperty("xinsert.display." + fileName, true)) {
        if(!XInsertReader.read(
	    this, 
	    XTree.class.getResourceAsStream("xml/" + fileName + ".insert.xml"), 
	    "xml/" + fileName + ".insert.xml"))
          Log.log(Log.ERROR,this,("Resource not found: " + fileName));
        else
          Log.log(Log.NOTICE,this,("Resource loaded: " + fileName));
        }
      }
    catch(NullPointerException e) {
      Log.log(Log.ERROR,this,("Resource not found: " + fileName));
      }
    }

  private void build() {
    String dir = jEdit.getProperty("xinsert.inserts-directory");
    if(!dir.endsWith(File.separator))
      dir = dir + File.separator;
    File f = new File(dir);
    if (!f.exists())
      f.mkdirs();
    String inserts[] = Utilities.getWildCardMatches(dir, "*.insert.xml", false);
    if (inserts == null)
      return;
    try {
      String fileName;
      for (int i = 0; i < inserts.length; i++) {
        fileName = dir + inserts[i];
        if (XInsertReader.read(this, new FileInputStream(fileName), fileName)) {
          String[] args = { inserts[i] };
          System.out.println(jEdit.getProperty("xtree.loaded", args));
          }
        }
      }
    catch (FileNotFoundException fnfe) {}
    }

  private void reload() {
    root.removeAllChildren();
    init();
    treeModel.reload();
    tree.repaint();
    }

  public void reload(DefaultTreeModel model) {
    tree.setModel(model);
    }

  // main action, called from treelistener.actionPerformed 
  // insert selected item, shift focus to text area, 
  // stay selected (for future keyboard inserts)
  public void treeAction() {
    if (tree.isSelectionEmpty()) return;
    XTreeNode node = (XTreeNode) tree.getSelectionPath().getLastPathComponent();
    if (node.getIndex() != -1) insert(node);
    view.getTextArea().requestFocus();
    view.toFront();
    // source.clearSelection();
    }

  private void insert(XTreeNode node) {
    if (!view.getTextArea().isEditable()) {
      view.getToolkit().beep();
      return;
      }
    XTreeItem item = (XTreeItem) inserts.elementAt(node.getIndex() - 1);
    String data = item.getContent();
    int type = item.getType();
    if(type == XTreeItem.TEXT_TYPE || !executeScript.isSelected())
      XScripter.insertText(view, data, node);
    else if(type == XTreeItem.MACRO_TYPE) {
      Log.log(Log.DEBUG, this, "Running Macro...");
      XScripter.runMacro(view, (String) node.getUserObject(), data);
      return;
      }
    else if(type == XTreeItem.XINSERT_SCRIPT_TYPE) {
      Log.log(Log.DEBUG, this, "Running XInsert Script ...");
      XScripter.runXInsertScript(view, data, node);
      }
    else if(type == XTreeItem.NAMED_MACRO_TYPE) {
      Log.log(Log.DEBUG, this, "Running Named Macro ...");
      XScripter.runNamedMacro(view, (String) node.getUserObject(), data);
      return;
      }
    else if(type == XTreeItem.ACTION_TYPE) {
      Log.log(Log.DEBUG, this, "Invoking Action " + data + " ...");
      XScripter.invokeAction(view, (String) node.getUserObject(), data);
      return;
      }
    else if(type == XTreeItem.REFERENCE_TYPE) {
      // new: lookup a referenced item by path [hertzhaft]
      Log.log(Log.DEBUG, this, "Resolving XInsert reference " + data + " ...");
      XTreeNode ref = getXTreeNodeByPath(data);
      if (ref == null) {
	 Log.log(Log.DEBUG, this, "Could not resolve path: " + data);
	 return;
      	 }
      // Log.log(Log.DEBUG, this, ref.toString());
      XTreeItem refitem = (XTreeItem) inserts.elementAt(ref.getIndex()-1);
      // Log.log(Log.DEBUG, this, refitem.toString());
      if (refitem.getType() == XTreeItem.REFERENCE_TYPE) { 
	 Log.log(Log.DEBUG, this, "Chained references are not allowed: " + data);
	 return;
      	 }
      insert(ref);
      }
    else {
      // non of the known insert types. Insert the node content
      XScripter.insertText(view, data, node);
      Log.log(Log.DEBUG, this, node + ": Unknown insert type");
      }
    }

  // construct a reference lookup map for nodes by stringified tree paths 
  public void fillMap() {
	map = new HashMap();
	fillItem("", root);
	}
	
  // visit all nodes and append their paths  
  public void fillItem(String name, XTreeNode node) {
  	String nodename = name + "/" + (String) node.getUserObject();
        // Log.log(Log.DEBUG, this, nodename + node.toString());
	map.put(nodename, node);
	Enumeration list = node.children();
	while (list.hasMoreElements())
		fillItem(nodename, (XTreeNode) list.nextElement());
  	}

  // get a node by its path, needed for reference lookup
  public XTreeNode getXTreeNodeByPath(String path) {
	return (XTreeNode) map.get("/XInsert" + path);
  	}

  private static final ImageIcon plainLeaf = Utilities.getIcon("images/tree_leaf.gif");
  private static final ImageIcon scriptLeaf = Utilities.getIcon("images/tree_leaf_script_x.gif");
  private static final ImageIcon macroLeaf = Utilities.getIcon("images/tree_leaf_macro.gif");
  private static final ImageIcon namedmacroLeaf = Utilities.getIcon("images/tree_leaf_namedmacro.gif");
  private static final ImageIcon referenceLeaf = Utilities.getIcon("images/tree_leaf_reference.gif");
  private static final ImageIcon actionLeaf = Utilities.getIcon("images/tree_leaf_action.gif");
  private static final ImageIcon errorLeaf = Utilities.getIcon("images/tree_leaf_error.gif");

  class XTreeCellRenderer extends DefaultTreeCellRenderer {
    XTreeCellRenderer() {
      super();
      /*openIcon = Utilities.getIcon("images/tree_open.gif");
      closedIcon = Utilities.getIcon("images/tree_close.gif");*/
      // commented out (MR): why change default behaviour here?
      // textSelectionColor = Color.red;
      // borderSelectionColor = tree.getBackground();
      // backgroundSelectionColor = tree.getBackground();
      }

    public Component getTreeCellRendererComponent(JTree source, Object value, boolean sel,
        boolean expanded, boolean leaf, int row,
        boolean hasFocus) {
      if (leaf) {
        TreePath path = source.getPathForRow(row);
        if (path != null) {
          XTreeNode node = (XTreeNode) path.getLastPathComponent();
          int index = node.getIndex();
          if (index != -1) {
            int type = ((XTreeItem) inserts.elementAt(index - 1)).getType();
            switch (type) {
            case XTreeItem.TEXT_TYPE:
              leafIcon = plainLeaf;
              break;
            case XTreeItem.XINSERT_SCRIPT_TYPE:
              leafIcon = scriptLeaf;
              break;
            case XTreeItem.MACRO_TYPE:
              leafIcon = macroLeaf;
              break;
            case XTreeItem.NAMED_MACRO_TYPE:
              leafIcon = namedmacroLeaf;
              break;
            case XTreeItem.ACTION_TYPE:
              leafIcon = actionLeaf;
              break;
            case XTreeItem.REFERENCE_TYPE:
              leafIcon = referenceLeaf;
              break;
            default:
              leafIcon = errorLeaf;
              }
            }
          }
        }
      return super.getTreeCellRendererComponent(source, value, sel, expanded, leaf, row, hasFocus);
      }
    }

  private class XTreeTree extends JTree {

    public XTreeTree(TreeModel model) {
      super(model);
      }

    public String getToolTipText(MouseEvent e) {
      if(e == null)
        return null;
      TreePath tPath = tree.getPathForLocation(e.getX(), e.getY());
      if(tPath != null) {
        XTreeNode node = (XTreeNode) tPath.getLastPathComponent();
        if(!node.isLeaf())
          return null;
        try {
          XTreeItem item = (XTreeItem) inserts.elementAt(node.getIndex()-1);
          int type = item.getType();
          String content = item.getContent();
          if(type == XTreeItem.TEXT_TYPE)
            return (content.length() > 30) ? content.substring(0, 30) + " ..." : content;
          else if(type == XTreeItem.MACRO_TYPE)
            return "Macro";
          else if(type == XTreeItem.XINSERT_SCRIPT_TYPE)
            return "Script";
          else if(type == XTreeItem.ACTION_TYPE)
            return "Action: " + content;
          else if(type == XTreeItem.NAMED_MACRO_TYPE)
            return "Named Macro";
          else if(type == XTreeItem.REFERENCE_TYPE)
            return "Ref => " + content;
          else
            return "Error: " + content;
          }
        catch( ArrayIndexOutOfBoundsException ex) {
          //   Log.log(Log.ERROR, XTree.class, "getTreeToolTip() throws "
          //    + ex.getClass().getName() + " exception.");
          //   Log.log(Log.ERROR, XTree.class, "TreePath is " + tPath.toString());
          //   Log.log(Log.ERROR, XTree.class, "TreeNode object is " +
          //    node.toString());
          return null;
          }
        }
      else
        return null;
      }
    }

    /**
     * Private class that acts as a listener to the tree of clips.
     * Inserts a clip either on double-click or when space is hit.
     */
  private class TreeListener
	  extends    MouseAdapter
	  implements ActionListener {

     public void mouseClicked(MouseEvent e) {
	// changed (MR): respond only to double clicks
	// todo: right click popup to add/change entries
	if ((e.getClickCount() == 2) 
	&& ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK)) {
          Log.log(Log.DEBUG, XTree.this, "Mouse Clicked");
          int selRow = tree.getRowForLocation(e.getX(), e.getY());
          if(selRow == -1) return;
          TreePath tPath = tree.getPathForLocation(e.getX(), e.getY());
          if(tPath == null) return;
          XTreeNode node = (XTreeNode) tPath.getLastPathComponent();
          if(!node.isLeaf()) return;
          if(node.getChildCount() != 0) return;
          if(node.getIndex() != -1)
              insert(node);
          view.requestFocus();
          view.toFront();
          }
        }
     public void actionPerformed(ActionEvent evt) {
        Object o = evt.getSource();
        if (o == tree)
          treeAction();
        else if (o == expand) {
          for (int i = 0; i < tree.getRowCount(); i++)
	    tree.expandRow(i);
	  }
        else if (o == collapse) {
          for (int i = tree.getRowCount(); i >= 0; i--)
	    tree.collapseRow(i);
          }
        else if (o == reload)
          reload();
        else if (o == carriageReturn)
          jEdit.setBooleanProperty("xtree.carriage", carriageReturn.isSelected());
        else if (o == executeScript)
          jEdit.setBooleanProperty("xtree.execute", executeScript.isSelected());
          }
       }
}

// End of XTree.java
