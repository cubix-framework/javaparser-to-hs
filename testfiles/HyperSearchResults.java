
/*
 * HyperSearchResults.java - HyperSearch results
 * :tabSize=4:indentSize=4:noTabs=false:
 *
 * Copyright (C) 1998, 1999, 2000, 2001 Slava Pestov
 * Portions copyright (C) 2002 Peter Cox
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

package xsearch;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

/**
 * HyperSearch results window.
 * @author Slava Pestov, adapted by Rudi Widmann
 * @version $Id: HyperSearchResults.java 15793 2009-07-28 20:10:25Z ezust $
 */
public class HyperSearchResults extends JPanel implements EBComponent,
	DefaultFocusComponent
{
	public static final String NAME = "xsearch-hypersearch-results";

	//{{{ HyperSearchResults constructor
	public HyperSearchResults(View view)
	{
		super(new BorderLayout());

		this.view = view;

		caption = new JLabel();

		Box toolBar = new Box(BoxLayout.X_AXIS);
		toolBar.add(caption);
		toolBar.add(Box.createGlue());
		ActionHandler ah = new ActionHandler();

		previousResultButton = new RolloverButton(GUIUtilities.loadIcon("ArrowL.png"));
		previousResultButton.setToolTipText(jEdit.getProperty("xhypersearch-results.prev.label"));
		previousResultButton.addActionListener(ah);
		toolBar.add(previousResultButton);

		nextResultButton = new RolloverButton(GUIUtilities.loadIcon("ArrowR.png"));
		nextResultButton.setToolTipText(jEdit.getProperty("xhypersearch-results.next.label"));
		nextResultButton.addActionListener(ah);
		toolBar.add(nextResultButton);

		clear = new RolloverButton(GUIUtilities.loadIcon("Clear.png"));
		clear.setToolTipText(jEdit.getProperty(
			"hypersearch-results.clear.label"));
		clear.addActionListener(ah);
		toolBar.add(clear);

		multi = new RolloverButton();
		multi.setToolTipText(jEdit.getProperty(
			"hypersearch-results.multi.label"));
		multi.addActionListener(ah);
		toolBar.add(multi);

		add(BorderLayout.NORTH, toolBar);

		resultTreeRoot = new DefaultMutableTreeNode();
		resultTreeModel = new DefaultTreeModel(resultTreeRoot);
		resultTree = new JTree(resultTreeModel);
		resultTree.setCellRenderer(new ResultCellRenderer());
		resultTree.setVisibleRowCount(16);
		resultTree.setRootVisible(false);
		resultTree.setShowsRootHandles(true);

		// looks bad with the OS X L&F, apparently...
		if(!OperatingSystem.isMacOSLF())
			resultTree.putClientProperty("JTree.lineStyle", "Angled");

		resultTree.setEditable(false);

		resultTree.addKeyListener(new KeyHandler());
		resultTree.addMouseListener(new MouseHandler());

		JScrollPane scrollPane = new JScrollPane(resultTree);
		Dimension dim = scrollPane.getPreferredSize();
		dim.width = 400;
		scrollPane.setPreferredSize(dim);
		add(BorderLayout.CENTER, scrollPane);
	} //}}}

	//{{{ focusOnDefaultComponent() method
	public void focusOnDefaultComponent()
	{
		resultTree.requestFocus();
	} //}}}

	//{{{ addNotify() method
	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);
		multiStatus = jEdit.getBooleanProperty(
			"hypersearch-results.multi");
		updateMultiStatus();
	} //}}}

	//{{{ setSearchStatus() method
	public void setSearchStatus(String status)
	{
		caption.setText(status);
	} //}}}


	//{{{ removeNotify() method
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);
		jEdit.setBooleanProperty("hypersearch-results.multi",multiStatus);
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		if(msg instanceof BufferUpdate)
		{
			BufferUpdate bmsg = (BufferUpdate)msg;
			Buffer buffer = bmsg.getBuffer();
			Object what = bmsg.getWhat();
			if(what == BufferUpdate.LOADED ||
				what == BufferUpdate.CLOSED)
			{
				ResultVisitor visitor = null;
				if (what == BufferUpdate.LOADED)
				{
					visitor = new BufferLoadedVisitor();
				}
				else // BufferUpdate.CLOSED
				{
					visitor = new BufferClosedVisitor();
				}
				// impl note: since multiple searches now allowed,
				// extra level in hierarchy
				for(int i = resultTreeRoot.getChildCount() - 1; i >= 0; i--)
				{
					DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode)
						resultTreeRoot.getChildAt(i);
					for(int j = searchNode.getChildCount() - 1;
						j >= 0; j--)
					{

						DefaultMutableTreeNode bufferNode = (DefaultMutableTreeNode)
							searchNode.getChildAt(j);

						for(int k = bufferNode.getChildCount() - 1;
							k >= 0; k--)
						{
							Object userObject =
								((DefaultMutableTreeNode)bufferNode
								.getChildAt(k)).getUserObject();
							HyperSearchResult result = (HyperSearchResult)
									userObject;

							if(buffer.getPath().equals(result.path))
								visitor.visit(buffer,result);
						}
					}
				}
			}
		}
	} //}}}

	//{{{ getTreeModel() method
	public DefaultTreeModel getTreeModel()
	{
		return resultTreeModel;
	} //}}}

	//{{{ getTree() method
	/**
	 * Returns the result tree.
	 * @since jEdit 4.1pre9
	 */
	public JTree getTree()
	{
		return resultTree;
	} //}}}

	//{{{ searchStarted() method
	public void searchStarted()
	{
		caption.setText(jEdit.getProperty("hypersearch-results.searching",
			new String[] { SearchAndReplace.getSearchString() }));
	} //}}}

	//{{{ searchFailed() method
	public void searchFailed()
	{
		caption.setText(jEdit.getProperty("hypersearch-results.no-results",
			new String[] { SearchAndReplace.getSearchString() }));

		// collapse all nodes, as suggested on user mailing list...
		for(int i = 0; i < resultTreeRoot.getChildCount(); i++)
		{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
				resultTreeRoot.getChildAt(i);
			resultTree.collapsePath(new TreePath(new Object[] {
				resultTreeRoot, node }));
		}
	} //}}}

	//{{{ searchDone() method
	public void searchDone(final DefaultMutableTreeNode searchNode)
	{
		final int nodeCount = searchNode.getChildCount();
		if (nodeCount < 1)
		{
			searchFailed();
			return;
		}

		caption.setText(jEdit.getProperty("hypersearch-results.done",
			new String[] { SearchAndReplace.getSearchString() }));

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				if(!multiStatus)
				{
					for(int i = 0; i < resultTreeRoot.getChildCount(); i++)
					{
						resultTreeRoot.remove(0);
					}
				}

				resultTreeRoot.add(searchNode);
				resultTreeModel.reload(resultTreeRoot);

				TreePath lastNode = null;

				for(int i = 0; i < nodeCount; i++)
				{
					lastNode = new TreePath(
						((DefaultMutableTreeNode)
						searchNode.getChildAt(i))
						.getPath());

					resultTree.expandPath(lastNode);
				}

				resultTree.scrollPathToVisible(
					new TreePath(new Object[] {
					resultTreeRoot,searchNode }));
			}
		});
	} //}}}

	public void previousResult()
	{
		neighbourResult(-1);
	}

	public void nextResult()
	{
		neighbourResult(1);
	}

	public void neighbourResult(int increment)
	{
		TreePath path = resultTree.getSelectionPath();
		if(path == null)
			return;
		int row = resultTree.getRowForPath(path);
		TreePath nextPath = resultTree.getPathForRow(row+increment);
		if (nextPath != null)
		{
			resultTree.setSelectionPath(nextPath);
			goToSelectedNode();
		}
	}


	//{{{ Private members
	private View view;

	private JLabel caption;
	private JTree resultTree;
	private DefaultMutableTreeNode resultTreeRoot;
	private DefaultTreeModel resultTreeModel;

	private RolloverButton clear;
	private RolloverButton multi;
	private RolloverButton previousResultButton;
	private RolloverButton nextResultButton;
	private boolean multiStatus;

	//{{{ updateMultiStatus() method
	private void updateMultiStatus()
	{
		if(multiStatus)
			multi.setIcon(GUIUtilities.loadIcon("MultipleResults.png"));
		else
			multi.setIcon(GUIUtilities.loadIcon("SingleResult.png"));
	} //}}}

	//{{{ goToSelectedNode() method
	private void goToSelectedNode()
	{
		TreePath path = resultTree.getSelectionPath();
		if(path == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path
			.getLastPathComponent();
		Object value = node.getUserObject();

		if(node.getParent() == resultTreeRoot)
		{
			// do nothing if clicked "foo (showing n occurrences in m files)"
		}
		else if(value instanceof String)
		{
			Buffer buffer = jEdit.openFile(view,(String)value);
			if(buffer == null)
				return;

			view.goToBuffer(buffer);

			// fuck me dead
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					resultTree.requestFocus();
				}
			});
		}
		else if (value instanceof HyperSearchResult)
		{
			((HyperSearchResult)value).goTo(view);
		}
	} //}}}

	//{{{ toggleAllNodes() method
	private void toggleAllNodes(boolean expand)
		{
			int nodeCount = resultTreeRoot.getChildCount();
			if (nodeCount > 0) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)resultTreeRoot.getFirstChild();
				for(int i = 0; i < nodeCount; i++)
				{
					TreePath childPath = new TreePath(childNode.getPath());
					if (expand)
						expandPath(99, childPath);
					else
						expandPath(0, childPath);
					childNode = childNode.getNextSibling();
				}
			}
		}

		//{{{ expandSelectedNode() method
	private void expandSelectedNode(int level)
		{
			expandPath(level, resultTree.getSelectionPath());
		}
		//{{{ expandSelectedNode() method
	private void expandPath(int level, TreePath path)
		{
			if(path == null)
				return;
			if (level > 0)
				resultTree.expandPath(path);
			else
				resultTree.collapsePath(path);

			DefaultMutableTreeNode expandNode = (DefaultMutableTreeNode)path
				.getLastPathComponent();
				int expandNodeChildCount = expandNode.getChildCount();
				if(expandNodeChildCount != 0) {
					DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)expandNode.getFirstChild();
					for(int j = 0; j < expandNodeChildCount; j++)
					{
						TreePath childPath = new TreePath(childNode.getPath());
						if (level > 1)
							resultTree.expandPath(childPath);
						else if (level > 0)
							resultTree.collapsePath(childPath);

						int nextChildCount = childNode.getChildCount();
						if(nextChildCount != 0) {
							DefaultMutableTreeNode lineNode = (DefaultMutableTreeNode)childNode.getFirstChild();
							for(int k = 0; k < nextChildCount; k++)
							{
								TreePath linePath = new TreePath(lineNode.getPath());
								if (level > 2)
									resultTree.expandPath(linePath);
								else if (level > 1)
									resultTree.collapsePath(linePath);
								lineNode = lineNode.getNextSibling();
							}
						}
						childNode = childNode.getNextSibling();
					}
				}
		}

	//}}}

	private void writeNodeToBuffer(TreePath path)
	{
		DefaultMutableTreeNode startNode;
		int level;
		if (path == null)
		{
			// root
			startNode = resultTreeRoot;
		}
		else
		{
			startNode = (DefaultMutableTreeNode)path.getLastPathComponent();
		}
		new ResultWriter(startNode).write();
	}

	//{{{ ActionHandler class
	public class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == clear)
			{
				resultTreeRoot.removeAllChildren();
				resultTreeModel.reload(resultTreeRoot);
			}
			else if(source == multi)
			{
				multiStatus = !multiStatus;
				updateMultiStatus();

				if(!multiStatus)
				{
					for(int i = resultTreeRoot.getChildCount() - 2; i >= 0; i--)
					{
						resultTreeModel.removeNodeFromParent(
							(MutableTreeNode)resultTreeRoot
							.getChildAt(i));
					}
				}
			}
			else if(source == previousResultButton)
			{
				previousResult();
			}
			else if(source == nextResultButton)
			{
				nextResult();
			}
		}
	} //}}}

	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
				view.getDockableWindowManager().hideDockableWindow(NAME);
				evt.consume();
			}
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
			{
				goToSelectedNode();

				// fuck me dead
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						resultTree.requestFocus();
					}
				});

				evt.consume();
			}
		}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		//{{{ mousePressed() method
		public void mousePressed(MouseEvent evt)
		{
			if(evt.isConsumed())
				return;

			TreePath path1 = resultTree.getPathForLocation(
				evt.getX(),evt.getY());
			if(path1 == null)
				return;

			resultTree.setSelectionPath(path1);
			if (GUIUtilities.isPopupTrigger(evt))
				showPopupMenu(evt);
			else
			{
				goToSelectedNode();

				view.toFront();
				view.requestFocus();
				view.getTextArea().requestFocus();
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
				popupMenu.add(new ExpandTreeNodeAllAction());
				popupMenu.add(new ExpandTreeNode0Action());
				popupMenu.add(new RemoveTreeNodeAction());
				popupMenu.add(new ExpandTreeNode1Action());
				popupMenu.add(new ExpandTreeNode2Action());
				popupMenu.add(new ExpandAllNodesAction());
				popupMenu.add(new CollapseAllNodesAction());
				popupMenu.add(new WriteNodeToBufferAction());
				popupMenu.add(new WriteAllNodesToBufferAction());
			}

			GUIUtilities.showPopupMenu(popupMenu,evt.getComponent(),
				evt.getX(),evt.getY());
			evt.consume();
		} //}}}

		//}}}
	} //}}}

	//{{{ RemoveTreeNodeAction class
	class RemoveTreeNodeAction extends AbstractAction
	{
		public RemoveTreeNodeAction()
		{
			super(jEdit.getProperty("hypersearch-results.remove-node"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			TreePath path = resultTree.getSelectionPath();
			if(path == null)
				return;

			MutableTreeNode value = (MutableTreeNode)path
				.getLastPathComponent();
			resultTreeModel.removeNodeFromParent(value);
		}
	}//}}}

	//{{{ ExpandTreeNodeAllAction class
	class ExpandTreeNodeAllAction extends AbstractAction
	{
		public ExpandTreeNodeAllAction()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.expand-node"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			expandSelectedNode(99);
		}
	}//}}}

	//{{{ ExpandTreeNode0Action class (collapse Node)
	class ExpandTreeNode0Action extends AbstractAction
	{
		public ExpandTreeNode0Action()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.expand-node-0-level"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			expandSelectedNode(0);
		}
	}//}}}

	//{{{ ExpandTreeNode1Action class
	class ExpandTreeNode1Action extends AbstractAction
	{
		public ExpandTreeNode1Action()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.expand-node-1-level"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			expandSelectedNode(1);
		}
	}//}}}

	//{{{ ExpandTreeNode2Action class
	class ExpandTreeNode2Action extends AbstractAction
	{
		public ExpandTreeNode2Action()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.expand-node-2-level"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			expandSelectedNode(2);
		}
	}//}}}

	//{{{ ExpandAllNodesAction class
	class ExpandAllNodesAction extends AbstractAction
	{
		public ExpandAllNodesAction()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.expand-all-nodes"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			toggleAllNodes(true);
		}
	}//}}}

	//{{{ CollapseAllNodesAction class
	class CollapseAllNodesAction extends AbstractAction
	{
		public CollapseAllNodesAction()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.collapse-all-nodes"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			toggleAllNodes(false);
		}
	}//}}}

	//{{{ RemoveAllTreeNodesAction class
	class RemoveAllTreeNodesAction extends AbstractAction
	{
		public RemoveAllTreeNodesAction()
		{
			super(jEdit.getProperty("hypersearch-results.remove-all-nodes"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			resultTreeRoot = new DefaultMutableTreeNode();
			resultTreeModel = new DefaultTreeModel(resultTreeRoot);
			resultTree.setModel(resultTreeModel);
		}
	}//}}}

	//{{{ WriteNodeToBufferAction class
	class WriteNodeToBufferAction extends AbstractAction
	{
		public WriteNodeToBufferAction()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.write-node-to-buffer"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			writeNodeToBuffer(resultTree.getSelectionPath());
		}
	}//}}}

	class WriteAllNodesToBufferAction extends AbstractAction
	{
		public WriteAllNodesToBufferAction()
		{
			super(jEdit.getProperty("xsearch-hypersearch-results.write-all-nodes-to-buffer"));
		}

		public void actionPerformed(ActionEvent evt)
		{
			writeNodeToBuffer(null);
		}
	}//}}}

	class ResultWriter
	{
		public ResultWriter(DefaultMutableTreeNode root) {
			this.root = root;
			this.level = root.getLevel();
			nodeCascade[level] = root;
			// fill nodeCascade with parents
			for (int i=level;i>0;i--) {
				nodeCascade[i-1] = (DefaultMutableTreeNode)nodeCascade[i].getParent();
			}
			Log.log(Log.DEBUG, BeanShell.class,"+++ HyperSearchResults.662: level = "+level);
			textArea = jEdit.getActiveView().getTextArea();
		}
		public void write()
		{
			/*******************************************************************
			* Hyper search result tree structure
			* root
			* + searchNode
			*   + fileNode
			*     + results
			*   + fileNode
			*     + results
			* + searchNode
			*   + fileNode
			*     + results
			*       (+ result range)
			*******************************************************************/
			jEdit.newFile(view);
			writeHeader();
			int rootChildCount = (level < 1) ? root.getChildCount() : 1;
			if(rootChildCount == 0)
			{
				textArea.setSelectedText(
					"Search items not found\n\nEnd of report\n");
			}
			else
			{
				DefaultMutableTreeNode searchNode;
				if (level < 1)
					searchNode = (DefaultMutableTreeNode)root.getFirstChild();
				else
					searchNode = nodeCascade[1];
				for(int i = 0; i < rootChildCount; ++i)
				{
					fileCount = 0;
					hitCount = 0;
					writeSearchHeader(searchNode);
					if (i == rootChildCount-1)
						writeSearchParameters();  // write only for last result
					searchCount++;
					int searchChildCount = (level < 2) ? searchNode.getChildCount() : 1;
					if(searchChildCount == 0)
					{
						textArea.setSelectedText(
						"Search term not found\n\nEnd of report\n");
					}
					DefaultMutableTreeNode fileNode;
					if (level < 2)
						fileNode = (DefaultMutableTreeNode)searchNode.getFirstChild();
					else
						fileNode = nodeCascade[2];
					for(int j = 0; j < searchChildCount; ++j)
					{
						writeResultsForFile(fileNode);
						fileNode = fileNode.getNextSibling();
					}
					searchNode = searchNode.getNextSibling();
										writeFileFooter();
				}
			writeFooter();
			}
		}

		private void writeSearchHeader(DefaultMutableTreeNode node)
		{
			//node = (DefaultMutableTreeNode)node;
			if(node == null) return;
			int childCount = node.getChildCount();
			if( childCount == 0) return;
			sb.setLength(0);
			String searchHeader = "Results for search item: \""+node.getUserObject().toString()+"\"\n";
			StringBuffer underline = new StringBuffer();
			for (int i=1;i<searchHeader.length();i++) {
				underline.append("=");
			}
			sb.append("\t"+searchHeader + "\t"+underline.toString());
			sb.append("\n");
			textArea.setSelectedText(sb.toString());
		}

		private void writeResultsForFile(DefaultMutableTreeNode node)
		{
			//node = (DefaultMutableTreeNode)node;
			if(node == null) return;
			int childCount = node.getChildCount();
			if( childCount == 0) return;
			++fileCount;
			hitCount += childCount;
			sb.setLength(0);
			sb.append("\tMatched file: \t");
			sb.append(node.getUserObject().toString());
			sb.append("\n");
			DefaultMutableTreeNode lineNode = (DefaultMutableTreeNode)node.getFirstChild();
			if(lineNode == null) return;
			for( int i = 0; i < childCount; ++i)
			{
				if(lineNode == null)
				{
					sb.append("\t\tNull node for i = " + String.valueOf(i));
				}
				else
				{
					sb.append("\t\tline ");
					sb.append(lineNode.getUserObject().toString());
					// check if line node has a line range
					int lineRangeCount = lineNode.getChildCount();
					if (lineRangeCount > 0)
					{
						DefaultMutableTreeNode lineRangeNode = (DefaultMutableTreeNode)lineNode.getFirstChild();
						sb.append("\n\t\t\tline range:");
						for (int lr = 0; lr < lineRangeCount; lr++)
						{
							sb.append("\n\t\t\t\t");
							sb.append(lineRangeNode.getUserObject().toString());
							lineRangeNode = lineRangeNode.getNextSibling();
						}
					}
				}
				sb.append('\n');
				lineNode = lineNode.getNextSibling();
			}
			sb.append("\n\tNumber of occurrences: ");
			sb.append(String.valueOf(childCount));
			sb.append("\n");

			textArea.setSelectedText(sb.toString());
		}

		void writeHeader()
		{
			sb.append("Hypersearch report written on ");
			SimpleDateFormat f = new SimpleDateFormat("EE MMM d, yyyy h:mm a z");
			sb.append(f.format( new Date()));
			sb.append("\n\n");
			textArea.setSelectedText(sb.toString());
		}
		void writeSearchParameters()
		{
			sb.setLength(0);
			sb.append("\tUsed search parameters for ");
			if(SearchAndReplace.getRegexp())
				sb.append("regular expression:  ");
			else
				sb.append("search term:  ");
			sb.append(SearchAndReplace.getSearchString());
			sb.append("  (case ");
			if(SearchAndReplace.getIgnoreCase())
				sb.append("in");
			sb.append("sensitive)\n");
			sb.append("\tSearch file set type ");
			sb.append(writeSearchFileSetType());
			sb.append('\n');
			textArea.setSelectedText(sb.toString());
		}

		void writeFileFooter()
		{
			sb.setLength(0);
			sb.append("\tTotal of ");
			sb.append(String.valueOf(hitCount));
			sb.append(" occurrences found in ");
			sb.append(String.valueOf(fileCount));
			sb.append(" files\n\n");

			textArea.setSelectedText(sb.toString());
		}
		void writeFooter()
		{
			sb.setLength(0);
			sb.append("Total of ");
			sb.append(String.valueOf(searchCount));
			sb.append(" search results reported \n\nEnd of report\n");
			textArea.setSelectedText(sb.toString());
		}

		String writeSearchFileSetType()
		{
			StringBuffer result = new StringBuffer();
			org.gjt.sp.jedit.search.SearchFileSet fileSet = SearchAndReplace.getSearchFileSet();
			if(fileSet instanceof org.gjt.sp.jedit.search.CurrentBufferSet)
				result.append("current buffer");
			else if(fileSet instanceof org.gjt.sp.jedit.search.AllBufferSet)
				result.append("all open buffers with file mask '")
					  .append(((org.gjt.sp.jedit.search.AllBufferSet)fileSet).getFileFilter())
					  .append('\'');
			else if(fileSet instanceof org.gjt.sp.jedit.search.DirectoryListSet)
			{
				fileSet = (org.gjt.sp.jedit.search.DirectoryListSet)fileSet;
				result.append("all files in \n")
					  .append(((org.gjt.sp.jedit.search.DirectoryListSet)fileSet).getDirectory())
					  .append('\n');
				if(((org.gjt.sp.jedit.search.DirectoryListSet)fileSet).isRecursive())
					result.append("(and subdirectories) ");
				result.append("with file mask '")
					  .append(((org.gjt.sp.jedit.search.DirectoryListSet)fileSet).getFileFilter())
					  .append('\'');
			}
			else
				result.append("unknown file set");
			return result.toString();
		}
		private JEditTextArea textArea;
		private DefaultMutableTreeNode root;
		private DefaultMutableTreeNode[] nodeCascade = new DefaultMutableTreeNode[5];
		private int level;
		private StringBuffer sb = new StringBuffer();
		private int searchCount = 0;
		private int fileCount = 0;
		private int hitCount = 0;
	}
	//{{{ ResultCellRenderer class
	class ResultCellRenderer extends DefaultTreeCellRenderer
	{
		Font plainFont, boldFont;

		//{{{ ResultCellRenderer constructor
		ResultCellRenderer()
		{
			plainFont = UIManager.getFont("Tree.font");
			if(plainFont == null)
				plainFont = jEdit.getFontProperty("metal.secondary.font");
			boldFont = new Font(plainFont.getName(),Font.BOLD,
				plainFont.getSize());
		} //}}}

		//{{{ getTreeCellRendererComponent() method
		public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean sel, boolean expanded,
			boolean leaf, int row, boolean hasFocus)
		{
			Component comp = super.getTreeCellRendererComponent(tree,value,sel,
				expanded,leaf,row,hasFocus);
			setIcon(null);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

			if (node.getParent() == resultTreeRoot)
			{
				ResultCellRenderer.this.setFont(boldFont);
				int bufferCount = node.getChildCount();
				int resultCount = 0;
				for (int i = 0; i < bufferCount; i++)
				{
					resultCount += node.getChildAt(i).getChildCount();
				}
				Object[] pp = { node.toString(), new Integer(resultCount), new Integer(bufferCount) };
				setText(jEdit.getProperty("hypersearch-results.result-caption",pp));
			}
			else if(node.getUserObject() instanceof String)
			{
				// file name
				ResultCellRenderer.this.setFont(boldFont);
				int count = node.getChildCount();
				if(count == 1)
				{
					setText(jEdit.getProperty("hypersearch-results"
						+ ".file-caption1",new Object[] {
						node.getUserObject()
						}));
				}
				else
				{
					setText(jEdit.getProperty("hypersearch-results"
						+ ".file-caption",new Object[] {
						node.getUserObject(),
						new Integer(count)
						}));
				}
			}
			else
			{
				ResultCellRenderer.this.setFont(plainFont);
			}

			return this;
		} //}}}
	} //}}}

	// these are used to eliminate code duplication. i don't normally use
	// the visitor or "template method" pattern, but this code was contributed
	// by Peter Cox and i don't feel like changing it around too much.

	//{{{ ResultVisitor interface
	interface ResultVisitor
	{
		public void visit(Buffer buffer, HyperSearchResult result);
	} //}}}

	//{{{ BufferLoadedVisitor class
	class BufferLoadedVisitor implements ResultVisitor
	{
		public void visit(Buffer buffer, HyperSearchResult result)
		{
			result.bufferOpened(buffer);
		}
	} //}}}

	//{{{ BufferClosedVisitor class
	class BufferClosedVisitor implements ResultVisitor
	{
		public void visit(Buffer buffer, HyperSearchResult result)
		{
			result.bufferClosed();
		}
	} //}}}



}
