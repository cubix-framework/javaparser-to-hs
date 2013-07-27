/*
 * XmlTree.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2001 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml;

//{{{ Imports
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Vector;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;
import xml.parser.*;
//}}}

public class XmlTree extends JPanel implements EBComponent
{
	//{{{ XmlTree constructor
	public XmlTree(View view, boolean docked)
	{
		super(new BorderLayout());

		this.view = view;

		// create toolbar with parse button
		JToolBar buttonBox = new JToolBar();
		buttonBox.setFloatable(false);

		parseBtn = new RolloverButton(GUIUtilities.loadIcon("Parse.png"));
		parseBtn.setToolTipText(jEdit.getProperty("xml-tree.parse"));
		parseBtn.setMargin(new Insets(0,0,0,0));
		parseBtn.setRequestFocusEnabled(false);
		parseBtn.addActionListener(new ActionHandler());
		buttonBox.add(parseBtn);
		buttonBox.add(Box.createGlue());

		add(BorderLayout.NORTH,buttonBox);

		// create a faux model that will do until a real one arrives
		DefaultTreeModel emptyModel = new DefaultTreeModel(
			new DefaultMutableTreeNode(null));
		tree = new CustomTree(emptyModel);
		if(docked)
			tree.addMouseMotionListener(new MouseHandler());

		// looks bad with the OS X L&F, apparently...
		if(!OperatingSystem.isMacOSLF())
			tree.putClientProperty("JTree.lineStyle", "Angled");

		tree.setVisibleRowCount(10);
		tree.setCellRenderer(new Renderer());

		add(BorderLayout.CENTER,new JScrollPane(tree));

		propertiesChanged();

		CaretHandler caretListener = new CaretHandler();

		EditPane[] editPanes = view.getEditPanes();
		for(int i = 0; i < editPanes.length; i++)
		{
			editPanes[i].getTextArea().addCaretListener(
				caretListener);
		}

		update();
	} //}}}

	//{{{ requestDefaultFocus() method
	public boolean requestDefaultFocus()
	{
		tree.requestFocus();
		return true;
	} //}}}

	//{{{ addNotify() method
	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);
	} //}}}

	//{{{ removeNotify() method
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);
	} //}}}

	//{{{ update() method
	public void update()
	{
		DefaultTreeModel model = (DefaultTreeModel)view.getEditPane()
			.getClientProperty(XmlPlugin.ELEMENT_TREE_PROPERTY);
		if(model == null)
		{
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(view.getBuffer().getName());
			model = new DefaultTreeModel(root);

			root.insert(new DefaultMutableTreeNode(
				jEdit.getProperty("xml-tree.not-parsed")),0);

			model.reload(root);
			tree.setModel(model);
		}
		else
		{
			tree.setModel(model);
			expandTagAt(view.getTextArea().getCaretPosition());
		}
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		//{{{ EditPaneUpdate
		if(msg instanceof EditPaneUpdate)
		{
			EditPaneUpdate epu = (EditPaneUpdate)msg;
			EditPane editPane = epu.getEditPane();

			if(epu.getWhat() == EditPaneUpdate.CREATED)
				editPane.getTextArea().addCaretListener(new CaretHandler());
		} //}}}
		if(msg instanceof PropertiesChanged)
			propertiesChanged();
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private RolloverButton parseBtn;
	private JTree tree;

	private int showAttributes;
	private boolean parse;

	private View view;
	private Timer caretTimer;
	//}}}

	//{{{ propertiesChanged() method
	private void propertiesChanged()
	{
		showAttributes = jEdit.getIntegerProperty("xml.show-attributes",0);
	} //}}}

	//{{{ expandTagWithDelay() method
	private void expandTagWithDelay()
	{
		// if keystroke parse timer is running, do nothing
		// if(keystrokeTimer != null && keystrokeTimer.isRunning())
			// return;

		if(caretTimer != null)
			caretTimer.stop();

		caretTimer = new Timer(0,new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				expandTagAt(view.getTextArea().getCaretPosition());
			}
		});

		caretTimer.setInitialDelay(500);
		caretTimer.setRepeats(false);
		caretTimer.start();
	} //}}}

	//{{{ expandTagAt() method
	private void expandTagAt(int dot)
	{
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)tree
			.getModel().getRoot();

		if(root.getChildCount() == 0)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
			root.getChildAt(0);
		if(node.getUserObject() instanceof XmlTag)
		{
			Vector _path = new Vector();
			expandTagAt(node,dot,_path);
			_path.addElement(node);
			_path.addElement(root);

			Object[] path = new Object[_path.size()];
			for(int i = 0; i < path.length; i++)
				path[i] = _path.elementAt(path.length - i - 1);

			TreePath treePath = new TreePath(path);
			tree.expandPath(treePath);
			tree.setSelectionPath(treePath);
			tree.scrollPathToVisible(treePath);
		}
	} //}}}

	//{{{ expandTagAt() method
	private boolean expandTagAt(TreeNode node, int dot, Vector path)
	{
		int childCount = node.getChildCount();
		Object userObject = ((DefaultMutableTreeNode)node).getUserObject();
		XmlTag tag = (XmlTag)userObject;

		if(childCount == 0 && userObject instanceof XmlTag)
		{
			// check if the caret in inside this tag
			if(dot >= tag.start.getOffset()
				&& (tag.end == null
				|| dot <= tag.end.getOffset()))
				return true;
		}
		else
		{
			// check if any of our children contain the caret
			for(int i = childCount - 1; i >= 0; i--)
			{
				TreeNode _node = node.getChildAt(i);
				if(expandTagAt(_node,dot,path))
				{
					path.addElement(_node);
					return true;
				}
			}

			// check if the caret in inside this tag
			if(dot >= tag.start.getOffset()
				&& (tag.end == null
				|| dot <= tag.end.getOffset()))
				return true;
		}

		return false;
	} //}}}

	//}}}

	//{{{ Inner classes

	//{{{ CustomTree class
	class CustomTree extends JTree
	{
		CustomTree(TreeModel model)
		{
			super(model);
		}

		protected void processMouseEvent(MouseEvent evt)
		{
			switch(evt.getID())
			{
			//{{{ MOUSE_PRESSED...
			case MouseEvent.MOUSE_PRESSED:
				TreePath path = getPathForLocation(
					evt.getX(),evt.getY());
				if(path != null)
				{
					Object value = ((DefaultMutableTreeNode)path
						.getLastPathComponent()).getUserObject();

					if(value instanceof XmlTag)
					{
						XmlTag tag = (XmlTag)value;

						JEditTextArea textArea = view.getTextArea();

						textArea.setCaretPosition(tag.start.getOffset());
						if(evt.getClickCount() == 2)
						{
							expandPath(path);
							XmlActions.showEditTagDialog(view);
							return;
						}
					}
				}

				super.processMouseEvent(evt);
				break; //}}}
			//{{{ MOUSE_EXITED...
			case MouseEvent.MOUSE_EXITED:
				view.getStatus().setMessage(null);
				super.processMouseEvent(evt);
				break; //}}}
			default:
				super.processMouseEvent(evt);
				break;
			}
		}
	} //}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			XmlPlugin.getParser(view).parse(true);
		}
	} //}}}

	//{{{ CaretHandler class
	class CaretHandler implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			if(evt.getSource() == view.getTextArea())
				expandTagWithDelay();
		}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseMotionAdapter
	{
		public void mouseMoved(MouseEvent evt)
		{
			TreePath path = tree.getPathForLocation(
				evt.getX(),evt.getY());
			if(path == null)
				view.getStatus().setMessage(null);
			else
			{
				Object value = ((DefaultMutableTreeNode)path
					.getLastPathComponent()).getUserObject();

				if(value instanceof XmlTag)
				{
					view.getStatus().setMessage(((XmlTag)value)
						.attributeString);
				}
			}
		}
	} //}}}

	//{{{ Renderer class
	class Renderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree,value,sel,
				expanded,leaf,row,hasFocus);

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			Object nodeValue = node.getUserObject();
			if(nodeValue instanceof XmlTag)
			{
				XmlTag tag = (XmlTag)node.getUserObject();

				switch(showAttributes)
				{
				case 0:
					setText(tag.name);
					break;
				case 1:
					setText(tag.idAttributeString);
					break;
				case 2:
					setText(tag.attributeString);
					break;
				}
				setIcon(tag.empty ? XmlListCellRenderer.EMPTY_ELEMENT_ICON
					: XmlListCellRenderer.ELEMENT_ICON);
			}
			// is root?
			else if(node.getParent() == null)
			{
				setIcon(org.gjt.sp.jedit.browser.FileCellRenderer
					.fileIcon);
			}
			else
				setIcon(null);

			return this;
		}
	} //}}}

	//}}}
}
