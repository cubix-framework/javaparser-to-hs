/*
Copyright (C) 2007  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package browser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.util.Log;

import tags.TagFileManager;
import tags.TagsPlugin;
import browser.TagDB.Record;
import browser.TagDB.RecordSet;

@SuppressWarnings("serial")
public class ClassHierarchy extends JPanel implements DefaultFocusComponent {
	private static final String MSG_NO_HIERARCHY = "class-browser.msg.no-hierarchy";

	private static final String MSG_TITLE = "class-browser.msg.title";

	private static final String MSG_NO_SELECTED_CLASS = "class-browser.msg.no-selected-class";

	private static final String CLASS_BROWSER_CLASS_HIERARCHY = "class-browser-class-hierarchy";

	private View view;

	private JTree tree;

	private JList members;

	private JSplitPane splitPane;

	private Font mainClassFont;

	private Font normalFont;

	private JPanel topPanel;

	private JToolBar hierarchyToolbar;

	private JToggleButton completeHierButton;

	private JToggleButton superTypeHierButton;

	private JToggleButton subTypeHierButton;

	private JToolBar memberToolbar;

	private JToggleButton selfMembersButton;

	private JToggleButton derivedMembersButton;

	private JToggleButton hideVariablesButton;

	private JToggleButton hideStaticButton;

	private JToggleButton hideNonPublicButton;

	DefaultMutableTreeNode completeRoot;

	DefaultMutableTreeNode superTypeRoot;

	DefaultMutableTreeNode subTypeRoot;

	Object mainClassObject = null;

	private TagDB db = null;

	private static ClassHierarchy instance;

	private Vector<String> derivedClasses = new Vector<String>();

	private Hashtable<String, Vector<Object>> membersHash = new Hashtable<String, Vector<Object>>();

	private Hashtable<String, Vector<Object>> derivedMembersHash = new Hashtable<String, Vector<Object>>();

	private DefaultTreeModel emptyHierarchy = new DefaultTreeModel(null);

	NonFieldMemberFilter nonFieldFilter = new NonFieldMemberFilter();

	NonStaticMemberFilter nonStaticFilter = new NonStaticMemberFilter();

	PublicMemberFilter publicFilter = new PublicMemberFilter();

	private int rootLevel = 0;

	private static ClassHierarchy theInstance = null;

	private Vector<Object> emptyMembers = new Vector<Object>();
	
	public ClassHierarchy(View view) {
		super(new BorderLayout());

		this.view = view;
		instance = this;
		buildTree();

		members = new JList();
		members.setCellRenderer(new MemberCellRenderer());
		members.addMouseListener(new MemberCellActionHandler(members));

		buildHierarchyToolbar();
		buildMemberToolbar();

		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(hierarchyToolbar, BorderLayout.NORTH);
		treePanel.add(new JScrollPane(tree), BorderLayout.CENTER);

		JPanel memberPanel = new JPanel();
		memberPanel.setLayout(new BorderLayout());
		memberPanel.add(memberToolbar, BorderLayout.NORTH);
		memberPanel.add(new JScrollPane(members), BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePanel,
				memberPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(jEdit.getIntegerProperty("classbrowser.divider_location",150));
		splitPane.setLastDividerLocation(jEdit.getIntegerProperty("classbrowser.last_divider_location",150));
		
		topPanel = new JPanel(new BorderLayout());
		topPanel.add(BorderLayout.CENTER, splitPane);

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.CENTER);

		db = new TagDB();
	}

	static public ClassHierarchy getInstance(View view)
	{
		if (theInstance == null)
			theInstance = new ClassHierarchy(view);
		return theInstance;
	}
	
	private void updateDB(View view) {
		boolean updated = db.removeTemporaryTagFiles();
		TagFileManager tagFileManager = TagsPlugin.getTagFileManager();
		Vector tagIndexFiles = tagFileManager.getTagIndexFiles(view, ".");
		for (int i = 0; i < tagIndexFiles.size(); i++) {
			String tagFile = (String) tagIndexFiles.get(i);
			boolean isTemporary = false;
			if (tagFile.equals(".") || tagFile.equals(".."))
			{
				String bufferPath = view.getBuffer().getPath();
				try {
					Vector tagFiles = tagFileManager.findTagIndexFiles(view, bufferPath, 1);
					if(tagFiles.isEmpty())
					{
						Log.log(Log.DEBUG, this, "Didn't find a tag file for: " + bufferPath);
						continue;
					}
					Log.log(Log.DEBUG, this, "Tag file search for " + bufferPath + " yielded " + tagFiles.elementAt(0));
					tagFile = (String)tagFiles.elementAt(0);
					isTemporary = true;
				} catch (IOException e) {
				}
			}
			if (db.addTagFile(tagFile, isTemporary))
				updated = true;
		}
		if (updated)
			derivedClasses = db.findMatches(db.getInheritsRegExp("\\S+"));
	}

	private Record findClass(String clazz) {
		RecordSet rs = db.getTag(clazz, derivedClasses);
		if (rs.isEmpty())
			rs = db.getTag(clazz);
		while (rs.next()) {
			Record r = rs.getRecord();
			if (db.isScopeTag(r))
				return r;
		}
		return null;
	}

	private void addSuperClasses(DefaultMutableTreeNode node,
			HashSet<String> classes) {
		Object obj = node.getUserObject();
		String name;
		if (obj instanceof Record) {
			Record tag = (Record) obj;
			name = tag.getName();
			String inheritsStr = tag.get(TagDB.INHERITS_COL);
			if (inheritsStr == null)
				return;
			String[] superClasses = inheritsStr.split(",");
			for (int i = 0; i < superClasses.length; i++) {
				String superClass = superClasses[i];
				classes.add(superClass);
				DefaultMutableTreeNode child = new DefaultMutableTreeNode(
						superClass);
				node.add(child);
				addSuperClasses(child, classes);
			}
		} else {
			name = (String) obj;
			Record clazzTag = findClass(name);
			if (clazzTag == null)
				return;
			node.setUserObject(clazzTag);
			addSuperClasses(node, classes);
		}
	}

	private void addSubClasses(DefaultMutableTreeNode node,
			HashSet<String> classes) {
		Object obj = node.getUserObject();
		String name;
		if (obj instanceof Record)
			name = ((Record) obj).getName();
		else
			name = (String) obj;
		RecordSet rs = db.getInherits(name, derivedClasses);
		while (rs.next()) {
			Record subclass = rs.getRecord();
			classes.add(subclass.getName());
			DefaultMutableTreeNode child = new DefaultMutableTreeNode(subclass);
			node.add(child);
			addSubClasses(child, classes);
		}
	}

	private void setClass(View view, String clazz) {
		long start = System.currentTimeMillis();
		updateDB(view);
		Record clazzTag = findClass(clazz);
		Object obj = (clazzTag != null) ? clazzTag : clazz;
		mainClassObject = obj;
		HashSet<String> classesInHierarchy = new HashSet<String>();
		classesInHierarchy.add(clazz);
		superTypeRoot = new DefaultMutableTreeNode(obj);
		addSuperClasses(superTypeRoot, classesInHierarchy);
		subTypeRoot = new DefaultMutableTreeNode(obj);
		addSubClasses(subTypeRoot, classesInHierarchy);
		if (hasSingleLeaf(superTypeRoot)) {
			createCompleteHierarchy(superTypeRoot, subTypeRoot);
			completeHierButton.setEnabled(true);
			completeHierButton.setVisible(true);
			completeHierButton.setSelected(true);
		} else {
			completeHierButton.setEnabled(false);
			completeHierButton.setVisible(false);
			completeRoot = null;
			superTypeHierButton.setSelected(true);
			rootLevel = 0;
		}
		if (classesInHierarchy.size() == 1 && clazzTag == null) {
			subTypeRoot = superTypeRoot = completeRoot = null;
			tree.setModel(emptyHierarchy);
			members.setListData(emptyMembers);
			setMembers();
			Log.log(Log.ERROR, ClassHierarchy.class, "No hierarchy for class '"
					+ clazz + "'");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null,
					jEdit.getProperty(MSG_NO_HIERARCHY) + "'" + clazz + "'.",
					jEdit.getProperty(MSG_TITLE),
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		updateTree();
		getMembers(classesInHierarchy);
		setMembers();
		long end = System.currentTimeMillis();
		Log.log(Log.DEBUG, ClassHierarchy.class, "Hierarchy of '" + clazz
				+ "' took " + (end - start) * .001 + " seconds.");
	}

	private boolean hasSingleLeaf(DefaultMutableTreeNode root) {
		while (root.getChildCount() == 1)
			root = (DefaultMutableTreeNode) root.getFirstChild();
		return (root.getChildCount() == 0);
	}

	private void createCompleteHierarchy(DefaultMutableTreeNode superRoot,
			DefaultMutableTreeNode subRoot) {
		// Add the super type hierarchy
		Enumeration e = superRoot.depthFirstEnumeration();
		DefaultMutableTreeNode current = null;
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
					.nextElement();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node
					.getUserObject());
			if (current == null) {
				completeRoot = current = newNode;
			} else {
				current.add(newNode);
				current = newNode;
			}
		}
		// Add the sub type hierarchy
		copyTree(current, subRoot);
		rootLevel = current.getLevel();
	}

	private void copyTree(DefaultMutableTreeNode parent,
			DefaultMutableTreeNode tree) {
		Enumeration e = tree.children();
		while (e.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e
					.nextElement();
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node
					.getUserObject());
			parent.add(newNode);
			copyTree(newNode, node);
		}
	}

	private String tagName(Object obj) {
		if (obj instanceof Record)
			return ((Record) obj).getName();
		return obj.toString();
	}

	private String getMemberId(Object member) {
		if (!(member instanceof Record))
			return member.toString();
		Record tag = (Record) member;
		StringBuffer id = new StringBuffer(tag.getName());
		String signature = tag.get(TagDB.SIGNATURE_COL);
		if (signature != null)
			id.append("," + signature);
		String kind = tag.get(TagDB.KIND_COL);
		if (kind != null)
			id.append("," + kind);
		return id.toString();
	}

	private boolean isInherited(Object member) {
		if (!(member instanceof Record))
			return true;
		Record tag = (Record) member;
		String access = tag.get(TagDB.ACCESS_COL);
		if (access == null)
			return true;
		return (!access.equals("private"));
	}

	private void buildDerivedMembers(String clazz) {
		HashSet<String> seen = new HashSet<String>();
		Vector<Object> derivedMembers = new Vector<Object>();
		LinkedList<String> classes = new LinkedList<String>();
		classes.add(clazz);
		while (!classes.isEmpty()) {
			String curClass = classes.removeFirst();
			Vector<Object> classMembers = membersHash.get(curClass);
			if (classMembers == null)
				continue;
			for (int i = 0; i < classMembers.size(); i++) {
				Object member = classMembers.get(i);
				String memberId = getMemberId(member);
				if ((!seen.contains(memberId))
						&& (curClass.equals(clazz) || isInherited(member))) {
					derivedMembers
							.add(new InheritedMember(db, member, curClass));
					seen.add(memberId);
				}
			}
			Record classTag = findClass(curClass);
			String inheritsStr = classTag.get(TagDB.INHERITS_COL);
			if (inheritsStr != null) {
				String[] children = inheritsStr.split(",");
				for (int i = 0; i < children.length; i++)
					classes.add(children[i]);
			}
		}
		Collections.sort(derivedMembers, new RecordComparator());
		derivedMembersHash.put(clazz, derivedMembers);
	}

	private String getSelectedClassName() {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			return null;
		Object obj = node.getUserObject();
		String clazz = tagName(obj);
		return clazz;
	}

	private void getMembers(HashSet<String> classes) {
		membersHash.clear();
		derivedMembersHash.clear();
		StringBuffer classStr = new StringBuffer("(");
		Iterator<String> it = classes.iterator();
		while (it.hasNext())
			classStr.append(it.next() + "|");
		classStr.replace(classStr.length() - 1, classStr.length(), ")");
		RecordSet rs = db.getMembers(classStr.toString());
		while (rs.next()) {
			Record member = rs.getRecord();
			String memberOf = member.get(TagDB.SCOPE_COL);
			if (memberOf == null)
				continue; // Shouldn't happen, but just in case ...
			Vector<Object> classMembers;
			if (membersHash.containsKey(memberOf))
				classMembers = membersHash.get(memberOf);
			else {
				classMembers = new Vector<Object>();
				membersHash.put(memberOf, classMembers);
			}
			classMembers.add(member);
		}
		// Sort the members (if not sorted)
		it = classes.iterator();
		while (it.hasNext()) {
			Vector<Object> classMembers = membersHash.get(it.next());
			if (classMembers == null)
				continue;
			Collections.sort(classMembers, new RecordComparator());
		}
	}

	private void setMemberListData(Vector<Object> elements) {
		Vector<Object> filteredList = elements;
		Vector<MemberFilter> filters = new Vector<MemberFilter>();
		if (hideVariablesButton.isSelected())
			filters.add(nonFieldFilter);
		if (hideStaticButton.isSelected())
			filters.add(nonStaticFilter);
		if (hideNonPublicButton.isSelected())
			filters.add(publicFilter);

		if (!filters.isEmpty()) {
			filteredList = new Vector<Object>();
			for (int i = 0; i < elements.size(); i++) {
				Object obj = elements.get(i);
				boolean pass = true;
				for (int j = 0; j < filters.size(); j++)
					pass = pass && filters.get(j).pass(obj);
				if (pass)
					filteredList.add(obj);
			}
		}
		members.setListData(filteredList);
	}

	private void setMembers() {
		String clazz = getSelectedClassName();
		if (clazz == null)
			return;
		if (derivedMembersButton.isSelected()) {
			if (clazz == null)
				return;
			if (!derivedMembersHash.containsKey(clazz))
				buildDerivedMembers(clazz);
			setMemberListData(derivedMembersHash.get(clazz));
		} else {
			if (membersHash.containsKey(clazz))
				setMemberListData(membersHash.get(clazz));
			else
				members.setListData(new Vector<String>());
		}
	}

	public static void set(View view) {
		view.getDockableWindowManager().showDockableWindow(
				CLASS_BROWSER_CLASS_HIERARCHY);
		String clazz = JOptionPane.showInputDialog(null, "Enter class name:");
		instance.setClass(view, clazz);
	}

	public static void setSelected(View view) {
		view.getDockableWindowManager().showDockableWindow(
				CLASS_BROWSER_CLASS_HIERARCHY);
		String selected = view.getTextArea().getSelectedText();
		if (selected == null)
			selected = TagsPlugin.getTagNameAtCursor(view.getTextArea());
		if (selected == null) {
			Log.log(Log.ERROR, ClassHierarchy.class,
			"No 'class' selected for hierarchy");
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null,
					jEdit.getProperty(MSG_NO_SELECTED_CLASS),
					jEdit.getProperty(MSG_TITLE),
					JOptionPane.PLAIN_MESSAGE);
			return;
		}
		instance.setClass(view, selected);
	}

	private void buildHierarchyToolbar() {
		hierarchyToolbar = new JToolBar();
		hierarchyToolbar.setFloatable(false);
		ActionListener l = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateTree();
			}
		};
		completeHierButton = new JToggleButton("All", false);
		completeHierButton.addActionListener(l);
		superTypeHierButton = new JToggleButton("Base", true);
		superTypeHierButton.addActionListener(l);
		subTypeHierButton = new JToggleButton("Derived", false);
		subTypeHierButton.addActionListener(l);
		ButtonGroup buttons = new ButtonGroup();
		buttons.add(completeHierButton);
		buttons.add(superTypeHierButton);
		buttons.add(subTypeHierButton);
		hierarchyToolbar.add(completeHierButton);
		hierarchyToolbar.add(superTypeHierButton);
		hierarchyToolbar.add(subTypeHierButton);
	}

	@SuppressWarnings("deprecation")
	private void buildMemberToolbar() {
		memberToolbar = new JToolBar();
		memberToolbar.setFloatable(false);
		selfMembersButton = new JToggleButton("Self", true);
		derivedMembersButton = new JToggleButton("Inherited", false);
		ActionListener l = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMembers();
			}
		};
		selfMembersButton.addActionListener(l);
		derivedMembersButton.addActionListener(l);
		ButtonGroup buttons = new ButtonGroup();
		buttons.add(selfMembersButton);
		buttons.add(derivedMembersButton);
		memberToolbar.add(selfMembersButton);
		memberToolbar.add(derivedMembersButton);
		// The "hide" buttons
		Insets margin = new Insets(0, 0, 0, 0);
		hideVariablesButton = new JToggleButton("F");
		hideVariablesButton.setMargin(margin);
		hideVariablesButton.addActionListener(l);
		hideStaticButton = new JToggleButton("S");
		hideStaticButton.setMargin(margin);
		hideStaticButton.addActionListener(l);
		hideNonPublicButton = new JToggleButton("NP");
		hideNonPublicButton.setMargin(margin);
		hideNonPublicButton.addActionListener(l);
		JPanel hidePanel = new JPanel();
		hidePanel.add(new JLabel("Hide:"));
		hidePanel.add(hideVariablesButton);
		hidePanel.add(hideStaticButton);
		hidePanel.add(hideNonPublicButton);
		memberToolbar.add(hidePanel);
	}

	private void updateTree() {
		DefaultMutableTreeNode root;
		if (completeHierButton.isSelected())
			root = completeRoot;
		else if (superTypeHierButton.isSelected())
			root = superTypeRoot;
		else
			root = subTypeRoot;
		tree.setModel(new DefaultTreeModel(root));
		tree.setSelectionRow(rootLevel);
	}

	private void buildTree() {
		DefaultTreeCellRenderer renderer = new HierarchyCellRenderer();
		tree = new JTree();
		tree.setModel(emptyHierarchy);
		tree.setCellRenderer(renderer);
		tree.addMouseListener(new HierarchyCellActionHandler(tree));
		renderer.setLeafIcon(null);
		renderer.setOpenIcon(null);
		renderer.setClosedIcon(null);

		tree.setScrollsOnExpand(true);
		tree.setShowsRootHandles(true);

		tree.putClientProperty("JTree.lineStyle", "Angled");

		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent arg0) {
				setMembers();
			}
		});

		normalFont = tree.getFont();
		mainClassFont = normalFont.deriveFont(Font.BOLD);
		// tree.addMouseListener(new MouseHandler());

		ToolTipManager.sharedInstance().registerComponent(tree);
	}

	public void focusOnDefaultComponent() {
		tree.requestFocus();
	}

	/******************************************************************************
	* jEdit plugin methods
	******************************************************************************/
	public void removeNotify() {
		jEdit.setIntegerProperty("classbrowser.divider_location",splitPane.getDividerLocation());
		jEdit.setIntegerProperty("classbrowser.last_divider_location",splitPane.getLastDividerLocation());
		super.removeNotify();
	}
	
	/***************************************************************************
	 * MemberFilter - used for filtering class members
	 **************************************************************************/
	interface MemberFilter {
		public boolean pass(Object o);
	}

	/***************************************************************************
	 * NonFieldMemberFilter - used for filtering out fields
	 **************************************************************************/
	class NonFieldMemberFilter implements MemberFilter {
		public boolean pass(Object o) {
			return (!((o instanceof Record) && ((Record) o).isVariable()));
		}
	}

	/***************************************************************************
	 * NonStaticMemberFilter - used for filtering out static fields & methods
	 **************************************************************************/
	class NonStaticMemberFilter implements MemberFilter {
		public boolean pass(Object o) {
			return (!((o instanceof Record) && ((Record) o).isStatic()));
		}
	}

	/***************************************************************************
	 * PublicMemberFilter - used for filtering out non-public fields & members
	 **************************************************************************/
	class PublicMemberFilter implements MemberFilter {
		public boolean pass(Object o) {
			return ((!(o instanceof Record)) || ((Record) o).isPublic());
		}
	}

	/***************************************************************************
	 * RecordComparator - used for sorting class members
	 **************************************************************************/
	public class RecordComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			int r = tagName(o1).compareTo(tagName(o2));
			if (r == 0)
				r = o1.toString().compareTo(o2.toString());
			return r;
		}
	}

	/***************************************************************************
	 * InheritedMember - display inherited member with defining class
	 **************************************************************************/
	public class InheritedMember extends TagDB.Record {
		public InheritedMember(TagDB db, Object member, String clazz) {
			db.super();
			if (member instanceof Record)
				info = ((Record) member).info;
			else {
				info = new Hashtable<String, String>();
				info.put(TagDB.TAG_COL, tagName(member));
				info.put(TagDB.SCOPE_COL, clazz);
			}
		}

		public String getMiddle() {
			return " - " + get(TagDB.SCOPE_COL) + " ";
		}
	}

	/***************************************************************************
	 * MouseHandler, Context- and Options menu
	 **************************************************************************/
	class MouseHandler extends MouseAdapter {
		public void mousePressed(MouseEvent evt) {
			if (evt.isConsumed())
				return;

			TreePath path1 = tree.getPathForLocation(evt.getX(), evt.getY());
			if (path1 == null)
				return;
			// goToSelectedNode();
		}

	}

	/***************************************************************************
	 * MemberCellRenderer
	 **************************************************************************/
	@SuppressWarnings("serial")
	class MemberCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list,
					value, index, isSelected, cellHasFocus);
			if (value instanceof Record) {
				ImageIcon icon = db.getIcon((Record) value);
				if (icon != null)
					label.setIcon(icon);
			}
			return label;
		}
	}

	/***************************************************************************
	 * ScopeCellRenderer
	 **************************************************************************/
	@SuppressWarnings("serial")
	class HierarchyCellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			JLabel label = (JLabel) super.getTreeCellRendererComponent(tree,
					value, isSelected, expanded, leaf, row, hasFocus);
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object obj = node.getUserObject();
			if (obj instanceof Record) {
				ImageIcon icon = db.getIcon((Record) obj);
				if (icon != null)
					label.setIcon(icon);
			}
			label
					.setFont((obj == mainClassObject) ? mainClassFont
							: normalFont);
			return label;
		}
	}

	/***************************************************************************
	 * MemberCellActionHandler
	 **************************************************************************/
	class MemberCellActionHandler extends MouseAdapter {
		protected JList list;

		public MemberCellActionHandler(JList l) {
			list = l;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				Object obj = list.getSelectedValue();
				if (obj instanceof Record) {
					((Record) obj).jump(view);
				}
			}
		}
	}

	/***************************************************************************
	 * HierarchyCellActionHandler
	 **************************************************************************/
	class HierarchyCellActionHandler extends MouseAdapter {
		protected JTree tree;

		public HierarchyCellActionHandler(JTree t) {
			tree = t;
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				TreePath path = tree.getSelectionPath();
				Object obj = ((DefaultMutableTreeNode) path
						.getLastPathComponent()).getUserObject();
				if (obj instanceof Record) {
					((Record) obj).jump(view);
				}
			}
		}
	}
}
