package sn;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.gjt.sp.jedit.View;

import sn.RefByDbAccess.RefByRecord;
import sn.RefByDbAccess.RefByRecordHandler;

@SuppressWarnings("serial")
public class RefByTree extends JPanel {

	private View view;
	private JTextField text;
	private JTree tree;
	private DefaultTreeModel model;
	private SourceElementTreeNode root;
	private SourceElementTreeNode lastClicked;
	
	private class SourceElementTreeNode extends DefaultMutableTreeNode {
		private String desc;
		private Vector<SourceElement> elements;
		int index = 0;
		private boolean childrenCreated = false;
		public SourceElementTreeNode(SourceElement element) {
			elements = new Vector<SourceElement>();
			if (element == null) {	// root node
				childrenCreated = true;
				return;
			}
			add(element);
			desc = element.getRepresentativeName();
			index = 0;
		}
		public void addChild(SourceElement element) {
			String desc = element.getRepresentativeName();
			if (getChildCount() > 0) {
				TreeNode child = getFirstChild();
				while (child != null) {
					SourceElementTreeNode childNode = (SourceElementTreeNode) child;
					String childDesc = childNode.toString();
					if (childDesc.equals(desc)) {
						childNode.add(element);
						return;
					}
					child = childNode.getNextSibling();
				}
			}
			SourceElementTreeNode node = new SourceElementTreeNode(element);
			add(node);
		}
		public String toString() {
			return desc;
		}
		public void add(SourceElement element) {
			elements.add(element);
		}
		public SourceElement getNext() {
			if (index >= elements.size())
				index = 0;
			return elements.get(index++);
		}
		public void reset() {
			index = 0;
		}
		@Override
		public int getChildCount() {
			if (! childrenCreated) {
				childrenCreated = true;
				find(elements.get(0).getQualifiedName(), this);
			}
			return super.getChildCount();
		}
		@Override
		public boolean isLeaf() {
			if (childrenCreated)
				return (getChildCount() == 0);
			return false;
		}
	}
	public RefByTree(View view) {
		super(new BorderLayout());
		this.view = view;
		root = new SourceElementTreeNode(null);
		model = new DefaultTreeModel(root);
		tree = new JTree(model);
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);
		renderer.setLeafIcon(null);
		tree.setCellRenderer(renderer);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				SourceElementTreeNode n = (SourceElementTreeNode)
					e.getPath().getLastPathComponent();
				if (lastClicked != null)
					lastClicked.reset();
				lastClicked = n;
				SourceElement refBy = n.getNext();
				if (refBy != null)
					refBy.jumpTo(RefByTree.this.view);
			}
		});
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TreePath tp = tree.getPathForLocation(e.getX(), e.getY());
				if (tp == null)
					return;
				SourceElementTreeNode n = (SourceElementTreeNode)
					tp.getLastPathComponent();
				if (n == null)
					return;
				if (lastClicked != n)
					return;
				SourceElement refBy = n.getNext();
				if (refBy != null)
					refBy.jumpTo(RefByTree.this.view);
			}
		});
		add(new JScrollPane(tree), BorderLayout.CENTER);
		JPanel p = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Find:");
		p.add(l, BorderLayout.WEST);
		text = new JTextField(40);
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					find(text.getText());
				else
					super.keyReleased(e);
			}
		});
		p.add(text, BorderLayout.CENTER);
		add(p, BorderLayout.NORTH);
	}
	
	private class TreeRecordHandler implements RefByRecordHandler {
		private String dir;
		private String identifier;
		private SourceElementTreeNode parent;
		
		public TreeRecordHandler(String dir, String identifier, SourceElementTreeNode parent) {
			this.dir = dir;
			this.identifier = identifier;
			this.parent = parent;
		}
		public boolean handle(RefByRecord rec) {
			if (! rec.getIdentifier().equals(identifier))
				return false;
			SourceElement element = rec.refBySourceElement(dir);
			parent.addChild(element);
			return true;
		}
	}
	private void find(String identifier) {
		find(identifier, root);
	}
	private void find(final String identifier, final SourceElementTreeNode parent) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				parent.removeAllChildren();
				RefByDbAccess db = new RefByDbAccess();
				db.lookup(identifier, new TreeRecordHandler(db.getDir(), identifier, parent));
				model.nodeStructureChanged(parent);
			}
		});
	}
}
