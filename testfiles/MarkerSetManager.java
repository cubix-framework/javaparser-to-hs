package marker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import marker.MarkerSetsPlugin.ChangeListener;
import marker.MarkerSetsPlugin.Event;
import marker.tree.FileTreeBuilder;
import marker.tree.FlatTreeBuilder;
import marker.tree.FolderTreeBuilder;
import marker.tree.MarkerTreeBuilder;
import marker.tree.SourceLinkTree;
import marker.tree.SourceLinkTree.SourceLinkParentNode;
import marker.tree.SourceLinkTree.SourceLinkTreeModelListener;
import marker.tree.SourceLinkTree.SourceLinkTreeNodeRenderer;
import marker.tree.SourceLinkTree.SubtreePopupMenuProvider;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;


@SuppressWarnings("serial")
public class MarkerSetManager extends JPanel {
	
	private static final String MSG_BASE = "marker-set-manager.msg.";
	private static final String MSG_GROUP_BY = MSG_BASE + "groupBy";
	private static final String MSG_ACTIVE_MARKER_SET = MSG_BASE + "activeMarkerSet";
	private static final String MSG_NEW = MSG_BASE + "new";
	private static final String MSG_BUFFER_SCOPE = MSG_BASE + "bufferScope";
	private static final String MSG_SET_SHORTCUT = MSG_BASE + "setShortcut";
	private static final String MSG_SHORTCUT_FOR_MARKER = MSG_BASE + "shortcutLabel";
	private View view;
	private SourceLinkTree markers;
	private JComboBox structure;
	private MarkerTreeBuilder [] builders;
	private DefaultComboBoxModel activeModel;
	private JComboBox active;
	private JCheckBox bufferScope;
	private boolean selfUpdate;
	private boolean selfChangeActive;
	private JButton next;
	private JButton prev;
	
	public MarkerSetManager(View view)
	{
		super(new BorderLayout());
		this.view = view;
		JPanel northPanel = new JPanel();
		add(northPanel, BorderLayout.NORTH);
		builders = new MarkerTreeBuilder[] {
			new FlatTreeBuilder(),
			new FileTreeBuilder(),
			new FolderTreeBuilder()
		};
		JPanel structurePanel = new JPanel();
		structurePanel.setAlignmentX(0);
		northPanel.add(structurePanel);
		structurePanel.add(new JLabel(jEdit.getProperty(MSG_GROUP_BY)));
		structure = new JComboBox(builders);
		structurePanel.add(structure);
		markers = new SourceLinkTree(view);
		markers.setCellRenderer(new MarkerSetRenderer());
		add(new JScrollPane(markers), BorderLayout.CENTER);
		structure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MarkerTreeBuilder builder =
					(MarkerTreeBuilder) structure.getSelectedItem();
				markers.setBuilder(builder);
			}
		});
		structure.setSelectedIndex(0);
		JPanel activePanel = new JPanel();
		northPanel.add(activePanel);
		activePanel.add(new JLabel(jEdit.getProperty(MSG_ACTIVE_MARKER_SET)));
		activeModel = new DefaultComboBoxModel();
		active = new JComboBox(activeModel);
		activePanel.add(active);
		updateActiveComboBox();
		selfChangeActive = false;
		active.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				String selected = (String) active.getSelectedItem();
				if (selected == null)
					return;
				selfChangeActive = true;
				MarkerSetsPlugin.setActiveMarkerSet(selected);
				selfChangeActive = false;
			}
		});
		JButton newMarkerSet = new JButton(jEdit.getProperty(MSG_NEW));
		northPanel.add(newMarkerSet);
		newMarkerSet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MarkerSetsPlugin.setActiveMarkerSet(MarkerSetManager.this.view);
			}
		});
		markers.addSourceLinkTreeModelListener(new MarkerTreeListener());
		selfUpdate = false;
		bufferScope = new JCheckBox(jEdit.getProperty(MSG_BUFFER_SCOPE));
		northPanel.add(bufferScope);
		bufferScope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				updateTree();
			}
		});
		MarkerSetsPlugin.addChangeListener(new ChangeListener() {
			public void changed(Event e, FileMarker m, MarkerSet ms) {
				updateTree();
				if (! selfChangeActive)
					updateActiveComboBox();
			}
		});
		prev = new RolloverButton(GUIUtilities.loadIcon("ArrowL.png"));
		northPanel.add(prev);
		prev.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileMarker m = MarkerSetsPlugin.getActiveMarkerSet().prevMarker(
					MarkerSetManager.this.view);
				if (m != null);
					markers.select(m);
			}
		});
		next = new RolloverButton(GUIUtilities.loadIcon("ArrowR.png"));
		northPanel.add(next);
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileMarker m = MarkerSetsPlugin.getActiveMarkerSet().nextMarker(
					MarkerSetManager.this.view);
				if (m != null)
					markers.select(m);
			}
		});
		updateTree();
	}

	private void updateActiveComboBox()
	{
		ItemListener [] ils = active.getItemListeners();
		for (ItemListener il: ils)
			active.removeItemListener(il);
		activeModel.removeAllElements();
		for (String name: MarkerSetsPlugin.getMarkerSetNames())
			activeModel.addElement(name);
		for (ItemListener il: ils)
			active.addItemListener(il);
		active.setSelectedItem(MarkerSetsPlugin.getActiveMarkerSet().getName());
	}

	public void bufferChanged(Buffer b)
	{
		if (bufferScope.isSelected())
			updateTree();
	}
	
	public void updateTree()
	{
		if (selfUpdate)
			return;
		markers.clear();
		Vector<String> names = MarkerSetsPlugin.getMarkerSetNames();
		String path = null;
		if (bufferScope.isSelected())
			path = view.getBuffer().getPath();
		String activeName = MarkerSetsPlugin.getActiveMarkerSet().getName();
		addMarkerSetToTree(path, activeName);
		names.remove(activeName);
		for (String name: names)
			addMarkerSetToTree(path, name);
		for (int i = 0; i < markers.getRowCount(); i++)
			markers.expandRow(i);
	}

	private void addMarkerSetToTree(String path, String name)
	{
		MarkerSet ms = MarkerSetsPlugin.getMarkerSet(name);
		SourceLinkParentNode msNode = markers.addSourceLinkParent(
			new MarkerSetNode(ms));
		Vector<FileMarker> children = ms.getMarkers();
		for (FileMarker marker: children)
			if (path == null || (marker.file.equals(path)))
				msNode.addSourceLink(marker);
	}

	private class MarkerSetNode implements SubtreePopupMenuProvider
	{
		private MarkerSet ms;
		public MarkerSetNode(MarkerSet ms)
		{
			this.ms = ms;
		}
		public MarkerSet getMarkerSet()
		{
			return ms;
		}
		public void addPopupMenuItemsFor(JPopupMenu popup,
				SourceLinkParentNode parent, DefaultMutableTreeNode node)
		{
			Object userObj = node.getUserObject();
			if (! (userObj instanceof FileMarker))
				return;
			final FileMarker marker = (FileMarker) userObj;
			popup.add(new AbstractAction(jEdit.getProperty(
				MarkerSetManager.MSG_SET_SHORTCUT))
			{
				public void actionPerformed(ActionEvent e) {
					String shortcut = JOptionPane.showInputDialog(
						view, jEdit.getProperty(MarkerSetManager.MSG_SHORTCUT_FOR_MARKER));
					if (shortcut == null || shortcut.length() == 0)
						return;
					marker.setShortcut(shortcut);
				}
			});
		}
		
	}
	
	private class MarkerTreeListener implements SourceLinkTreeModelListener
	{
		public void nodeRemoved(DefaultMutableTreeNode node,
			SourceLinkParentNode parent, Vector<DefaultMutableTreeNode> leafs)
		{
			if (parent == null)
				return;
			selfUpdate = true;
			Object userObject = parent.getUserObject();
			MarkerSet ms = ((MarkerSetNode) userObject).getMarkerSet();
			if (node == parent)
				MarkerSetsPlugin.removeMarkerSet(ms);
			else {
				for (DefaultMutableTreeNode leaf: leafs)
					ms.remove((FileMarker) leaf.getUserObject());
			}
			selfUpdate = false;
		}
	}

	private class MarkerSetRenderer extends SourceLinkTreeNodeRenderer
	{
		private HashMap<MarkerSet, MarkerSetIcon> icons;
		
		public MarkerSetRenderer() {
			super();
			icons = new HashMap<MarkerSet, MarkerSetIcon>();
		}
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus)
		{
			Component c = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			if (obj instanceof MarkerSetNode)
			{
				MarkerSet ms = ((MarkerSetNode) obj).getMarkerSet();
				JLabel l = (JLabel) c;
				l.setText(ms.getName());
				MarkerSetIcon icon = icons.get(ms);
				if (icon == null)
				{
					icon = new MarkerSetIcon(ms);
					icons.put(ms, icon);
				}
				l.setIcon(icon);
			}
			return c;
		}
		
		private class MarkerSetIcon implements Icon
		{
			private MarkerSet ms;
			public MarkerSetIcon(MarkerSet ms) {
				this.ms = ms;
			}
			public Color getColor() {
				return ms.getColor();
			}
			public int getIconHeight() {
				return getDefaultIconHeight();
			}
			public int getIconWidth() {
				return getDefaultIconWidth();
			}
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Color prevColor = g.getColor();
				g.setColor(ms.getColor());
				g.fillRect(0, 0, getIconWidth(), getDefaultIconHeight());
				g.setColor(prevColor);
				
			}
		}
	}
}
