/*
 * SideKickTree.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2003 Slava Pestov
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

package sidekick;

// {{{ Imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Stack;
import java.util.HashSet;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.JLabel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PositionChanging;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.EnhancedTreeCellRenderer;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.util.StringList;
// }}}

/**
 * The Structure Browser dockable.  One instance is created for each View.
 */
public class SideKickTree extends JPanel implements DefaultFocusComponent
{

        // {{{ Instance variables
        private RolloverButton parseBtn;
        
        private Icon parseIcon;
        private Icon stopIcon;

        private JComboBox parserCombo;
        protected JTree tree;
        // protected JEditorPane status;
        protected JTextArea status;
        private JPanel topPanel;
        private JSplitPane splitter;
        private boolean statusShowing = false;
        private Buffer lastParsedBuffer = null;
        private JToolBar filterBox;
        protected JPopupMenu configMenu;
        protected JCheckBoxMenuItem onChange;
        protected JCheckBoxMenuItem followCaret;
        protected JCheckBoxMenuItem onSave;

        protected View view;
        private Timer caretTimer;

        protected SideKickParsedData data;

        private int autoExpandTree = 0;
        private JPanel toolBox;
        private JPanel parserPanel = null;

        private JTextField searchField;
        // }}}

        // {{{ SideKickTree constructor
        public SideKickTree(View view, boolean docked)
        {
                super(new BorderLayout());

                this.view = view;

                topPanel = new JPanel(new BorderLayout());

                // create toolbar with parse button
                JToolBar buttonBox = new JToolBar();
                buttonBox.setFloatable(false);
                filterBox = new JToolBar();
                filterBox.setLayout(new BorderLayout());
                filterBox.setFloatable(false);

                parseIcon = GUIUtilities.loadIcon("Parse.png");
		stopIcon = GUIUtilities.loadIcon(jEdit.getProperty("hypersearch-results.stop.icon"));                
                parseBtn = new RolloverButton(parseIcon);
                
                parseBtn.setToolTipText(jEdit.getProperty("sidekick-tree.parse"));
                parseBtn.setMargin(new Insets(0,0,0,0));
                parseBtn.setRequestFocusEnabled(false);
                parseBtn.setEnabled(true);
                ActionListener ah = new ActionHandler();
                parseBtn.addActionListener(ah);
                
                RolloverButton propsBtn = new RolloverButton(GUIUtilities.loadIcon("ButtonProperties.png"));
                propsBtn.setToolTipText(jEdit.getProperty("sidekick-tree.mode-options"));
                propsBtn.addActionListener(new SideKickProperties());

                configMenu = new JPopupMenu("Parse");
                followCaret = new JCheckBoxMenuItem("Follow Caret");

                configMenu.add(followCaret);
                // configMenu = new PopupMenu("Parse on...");
                JMenuItem item = new JMenuItem("Parse on...");
                item.setEnabled(false);
                configMenu.add(item);

                onChange = new JCheckBoxMenuItem("Buffer change");
                onChange.setState(SideKick.isParseOnChange());
                onSave = new JCheckBoxMenuItem("Buffer save");
                onSave.setState(SideKick.isParseOnSave());
                configMenu.add(onChange);
                configMenu.add(onSave);
                parseBtn.setComponentPopupMenu(configMenu);
                onChange.addActionListener(ah);
                onSave.addActionListener(ah);
                followCaret.addActionListener(ah);
                JLabel search = new JLabel(jEdit.getProperty("sidekick-tree.filter.label") + " ");
                searchField = new JTextField();
                searchField.setToolTipText(jEdit.getProperty("sidekick-tree.filter.tooltip"));
                RolloverButton clearSearchBtn = new RolloverButton(GUIUtilities.loadIcon("22x22/actions/edit-clear.png"));
                clearSearchBtn.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent ae)
                        {
                                searchField.setText("");
                                updateFilter();
                        }
                }
               );
                clearSearchBtn.setToolTipText(jEdit.getProperty("sidekick-tree.clear-filter.tooltip"));

                buttonBox.add(parseBtn);
                buttonBox.add(propsBtn);
                filterBox.add(search, BorderLayout.WEST);
                filterBox.add(searchField, BorderLayout.CENTER);
                filterBox.add(clearSearchBtn, BorderLayout.EAST);

                buttonBox.add(Box.createGlue());

                parserCombo = new JComboBox();
                reloadParserCombo();
                parserCombo.setToolTipText(jEdit.getProperty("sidekick-tree.parsercombo.tooltip"));

                buttonBox.add(parserCombo);
                parserCombo.addActionListener(ah);
                parserCombo.addActionListener(new ActionListener()
                {
                        public void actionPerformed(ActionEvent ae)
                        {
                                searchField.setText("");
                                updateFilter();
                        }
                }
               );

                toolBox = new JPanel(new BorderLayout());
                toolBox.add(BorderLayout.NORTH, buttonBox);
                toolBox.add(BorderLayout.SOUTH, filterBox);

                topPanel.add(BorderLayout.NORTH, toolBox);

                // create a faux model that will do until a real one arrives
                TreeModel emptyModel = new DefaultTreeModel(new DefaultMutableTreeNode(null));
                emptyModel = new FilteredTreeModel((DefaultTreeModel) emptyModel, true);
                tree = buildTree(emptyModel);
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                KeyHandler kh = new KeyHandler();
                tree.addKeyListener(kh);
                if (docked)
                {
                        tree.addMouseMotionListener(new MouseHandler());
                }
                searchField.addKeyListener(kh);

                // looks bad with the OS X L&F, apparently...
                if (!OperatingSystem.isMacOSLF())
                {
                        tree.putClientProperty("JTree.lineStyle", "Angled");
                }

                tree.setVisibleRowCount(10);
                tree.setCellRenderer(new Renderer());

                topPanel.add(BorderLayout.CENTER, new JScrollPane(tree));

                status = new JTextArea();
                // status.setContentType("text/html");
                status.setEditable(false);
                status.setBackground(jEdit.getColorProperty("view.bgColor"));
                status.setForeground(jEdit.getColorProperty("view.fgColor"));
                status.setFont(view.getEditPane().getTextArea().getPainter().getFont());
                JScrollPane status_scroller = new JScrollPane(status);

                splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, topPanel, status_scroller);

                status_scroller.setMinimumSize(new Dimension(0, 30));
                splitter.setOneTouchExpandable(true);
                splitter.setResizeWeight(1.0f);
                int location = splitter.getSize().height - splitter.getInsets().bottom - splitter.getDividerSize() - status_scroller.getMinimumSize().height;
                location = jEdit.getIntegerProperty("sidekick.splitter.location", location);

                splitter.setDividerLocation(location);

                // add(splitter);

                propertiesChanged();

                CaretHandler caretListener = new CaretHandler();

                EditPane[] editPanes = view.getEditPanes();
                for (int i = 0; i < editPanes.length; i++)
                {
                        JEditTextArea textArea = editPanes[i].getTextArea();
                        textArea.putClientProperty(CaretHandler.class, caretListener);
                        textArea.addCaretListener(caretListener);
                }

                update();
        }        // }}}

        public void showStopButton(final boolean show)
        {
                SwingUtilities.invokeLater (new Runnable()
                {
                        public void run()
                        {
                        	if (show && parseIcon.equals(parseBtn.getIcon()))
                        	{
                        		parseBtn.setToolTipText(jEdit.getProperty("sidekick-tree.stop-parsing"));
                        		parseBtn.setIcon(stopIcon);
                        		
                        	}
                        	else 
                        	{
                        		parseBtn.setToolTipText(jEdit.getProperty("sidekick-tree.parse"));
                        		parseBtn.setIcon(parseIcon);
                        	}
                        }
                }
               );
        }

        // {{{ focusOnDefaultComponent() method
        public void focusOnDefaultComponent()
        {
                tree.requestFocusInWindow();
        }        // }}}

        // {{{ addNotify() method
        public void addNotify()
        {
                super.addNotify();
                EditBus.addToBus(this);
        }        // }}}

        // {{{ removeNotify() method
        public void removeNotify()
        {
                super.removeNotify();
                EditBus.removeFromBus(this);
        }        // }}}

        // {{{ selectPath() method
        protected void selectPath(TreePath path)
        {
                tree.setSelectionPath(path);
                Rectangle r = tree.getPathBounds(path);
                if (r != null)
                {
                        r.width = 1;
                        tree.scrollRectToVisible(r);
                }
        }        // }}}

        // {{{ handleEditPaneUpdate() method
        @EBHandler
        public void handleEditPaneUpdate(EditPaneUpdate epu)
        {
                EditPane editPane = epu.getEditPane();
                if (epu.getWhat() == EditPaneUpdate.CREATED)
                {
					CaretHandler listener = new CaretHandler();
					JEditTextArea textArea = editPane.getTextArea();
					textArea.putClientProperty(CaretHandler.class, listener);
					textArea.addCaretListener(listener);
                }
        }        // }}}

        // {{{ handlePropertiesChanged method
        @EBHandler
        public void handlePropertiesChanged(PropertiesChanged msg)
        {
                propertiesChanged();
        }        // }}}

        // {{{ handleSideKickUpdate() method
        @EBHandler
        public void handleSideKickUpdate(SideKickUpdate msg)
        {
                if (msg.getView() == view)
                {
                        update();
                }
        }        // }}}

        // {{{ setStatus() method
        public void setStatus(String msg)
        {
                status.setText(msg);
        }        // }}}

        // {{{ addData method
        protected void addData(Object obj, Stack<String> keys)
        {
                if (obj instanceof DefaultMutableTreeNode)
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
                        String cur_key = "";
                        FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                        try
                        {
                                Asset a = (Asset) node.getUserObject();
                                if (a != null)
                                {
                                        cur_key = a.getName();
                                }
                        }
                        catch (ClassCastException ex)
                        {
                                if (node.toString() != null)
                                {
                                        cur_key = node.toString();
                                }
                        }
                        keys.push(cur_key);
                        if (model.isLeaf(node))
                        {
                                for (String key : keys)
                                {
                                        model.addSearchKey(node, key);
                                }
                        }

                        Enumeration<DefaultMutableTreeNode> e;
                        for (e = node.children(); e.hasMoreElements();)
                        {
                                addData(e.nextElement(), keys);
                        }

                        keys.pop();
                }
                else
                {
                        Log.log(Log.DEBUG, this, "addData called on a node that isn't a treenode!!!!!!!!!");                        // how exciting!
                }
        }        // }}}

        // {{{ updateSearchData() method
        protected void updateSearchData()
        {
                DefaultMutableTreeNode root;
                FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                root = (DefaultMutableTreeNode) model.getRoot();
                addData(root, new Stack<String>());
        }        // }}}

        // {{{ update() method
        protected void update()
        {
                onChange.setState(SideKick.isParseOnChange());
                onSave.setState(SideKick.isParseOnSave());
                Buffer parsedBuffer = view.getBuffer();
                SideKickParser parser = SideKickPlugin.getParserForBuffer(parsedBuffer);
                if (parser != null)
                {
                        Object item = parserCombo.getSelectedItem();
                        if (item != parser.getName())
                        {
                                parserCombo.setSelectedItem(parser.getName());
                        }
                }

                data = SideKickParsedData.getParsedData(view);
                if (parser == null || data == null)
                {
                        DefaultMutableTreeNode root = new DefaultMutableTreeNode(parsedBuffer.getName());
                        root.insert(new DefaultMutableTreeNode(jEdit.getProperty("sidekick-tree.not-parsed")),0);
                        tree.setModel(new FilteredTreeModel(new DefaultTreeModel(root), true));
                        lastParsedBuffer = null;
                }
                else
                {
                        tree.setModel(new FilteredTreeModel(data.tree, true));
                        lastParsedBuffer = parsedBuffer;
                        if (SideKick.isFollowCaret())
                        {
                                expandTreeAt(view.getTextArea().getCaretPosition());
                        }
                }
                updateSearchData();

                if (data != null && data.expansionModel != null)
                {
                        // collapse all rows, then expand per the expansion model
                        for (int i = tree.getRowCount() - 1; i >= 0; i--)
                        {
                                tree.collapseRow(i);
                        }
                        for (Integer row : data.expansionModel)
                        {
                                tree.expandRow(row);
                        }
                }
                else
                {
                        if (autoExpandTree == -1)
                        {
                                expandAll(true);
                        }
                        else if (autoExpandTree == 0)
                        {
                                expandAll(false);
                        }
                        else if (autoExpandTree > 0)
                        {
                                tree.expandRow(0);
                                for (int i = 1; i < autoExpandTree; i++)
                                {
                                        for (int j = tree.getRowCount() - 1; j > 0; j--)
                                        {
                                                tree.expandRow(j);
                                        }

                                }
                        }
                }

                if (searchField.getText().length() != 0)
                {
                        updateFilter();
                }

        }        // }}}

        // {{{ expandAll() methods
        /**
         * Expand or collapse all nodes in the tree.
         * @param expand if true, expand all nodes, if false, collapse all nodes
         */
        public void expandAll(boolean expand)
        {
                TreeNode root = (TreeNode) tree.getModel().getRoot();
                expandAll(new TreePath(root), expand);
        }

        // recursive method to traverse children
        private void expandAll(final TreePath parent, final boolean expand)
        {
                SwingUtilities.invokeLater(new Runnable()
                {
                        public void run()
                        {
                                TreeNode node = (TreeNode) parent.getLastPathComponent();
                                if (node.getChildCount() >= 0)
                                {
                                        for (Enumeration e = node.children(); e.hasMoreElements();)
                                        {
                                                TreeNode n = (TreeNode) e.nextElement();
                                                TreePath path = parent.pathByAddingChild(n);
                                                expandAll(path, expand);
                                        }
                                }

                                // expansion or collapse must be done from the bottom up
                                if (expand)
                                {
                                        tree.expandPath(parent);
                                }
                                else
                                {
                                        tree.collapsePath(parent);
                                }
                        }
                } );
        }        // }}}

        // {{{ buildTree() method
        protected JTree buildTree(TreeModel model)
        {
                return new CustomTree(model);
        }        // }}}

        // {{{ buildActionListener() method
        /**
         * Creates an action listener for the parse button.
         */
        protected ActionListener buildActionListener()
        {
                return new ActionHandler();
        }        // }}}

        // {{{ propertiesChanged() method
        protected void propertiesChanged()
        {
                followCaret.setSelected(SideKick.isFollowCaret());
                Mode m = view.getBuffer().getMode();
                String mode = m != null ? m.getName() : null;
                autoExpandTree = AbstractModeOptionPane.getIntegerProperty(mode, SideKick.AUTO_EXPAND_DEPTH, 1);
                // autoExpandTree = ModeOptions.getAutoExpandTreeDepth();

                if (AbstractModeOptionPane.getBooleanProperty(mode, SideKick.SHOW_STATUS))
                {
                        if (!statusShowing)
                        {
                                remove(topPanel);
                                splitter.setTopComponent(topPanel);
                                add(splitter);
                        }
                        statusShowing = true;
                }
                else
                {
                        remove(splitter);
                        splitter.remove(topPanel);
                        add(topPanel);
                        statusShowing = false;
                }

                // show or hide the filter box
                final boolean showFilter = jEdit.getBooleanProperty(SideKick.SHOW_FILTER, true);
                if (showFilter != filterBox.isVisible())
                {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                                public void run()
                                {
                                        filterBox.setVisible(showFilter);
                                        searchField.setEnabled(showFilter);
                                }
                        } );
                }
        }        // }}}

        // {{{ parserList() method
        /** @return a list of parsers, sorted, with special choices
         * on top */
        public static StringList parserList()
        {
                String[] serviceNames = ServiceManager.getServiceNames(SideKickParser.SERVICE);
                Arrays.sort(serviceNames, new StandardUtilities.StringCompare<String>(true));
                StringList sl = new StringList();
                sl.add(SideKickPlugin.NONE);
                sl.add(SideKickPlugin.DEFAULT);
                sl.addAll(serviceNames);
                return sl;
        }        // }}}

        // {{{ reloadParserCombo() method
        void reloadParserCombo()
        {
                parserCombo.setModel(new DefaultComboBoxModel(parserList().toArray()));
                SideKickParser currentParser = SideKickPlugin.getParserForBuffer(view.getBuffer());
                if (currentParser != null)
                {
                        String name = currentParser.getName();
                        if (name == null)
                        {
                        	name = SideKickPlugin.DEFAULT;	
                        }
                        parserCombo.setSelectedItem(name);
                }
                else
                {
                        String pp = view.getBuffer().getStringProperty(SideKickPlugin.PARSER_PROPERTY);
                        if (pp == SideKickPlugin.NONE)
                        {
                                parserCombo.setSelectedItem(SideKickPlugin.NONE);
                        }
                        else
                        {
                                parserCombo.setSelectedItem(SideKickPlugin.DEFAULT);
                        }
                }
        }        // }}}

        // {{{ addParserPanel() method
        void addParserPanel(SideKickParser parser)
        {
                JPanel newParserPanel = parser.getPanel();
                boolean returnFocusToSearchField = searchField.hasFocus();
                if (newParserPanel != parserPanel)
                {
                        if (parserPanel != null)
                        {
                                toolBox.remove(parserPanel);
                                parserPanel = null;
                        }
                        if (newParserPanel != null)
                        {
                                toolBox.add(BorderLayout.WEST, newParserPanel);
                                parserPanel = newParserPanel;
                        }
                }
                if (returnFocusToSearchField)
                {
                        focusOnDefaultComponent();
                }
        }        // }}}

        // {{{ removeParserPanel() method
        void removeParserPanel()
        {
                if (parserPanel != null)
                {
                        toolBox.remove(parserPanel);
                        parserPanel = null;
                }
        }        // }}}

        // {{{ expandTreeWithDelay() method
        /**
         * Expands the tree after a delay.
         * The delay timer is restarted each time this method is called.
         */
        protected void expandTreeWithDelay()
        {
                if (caretTimer != null)
                {
                        caretTimer.stop();
                }
                else
                {
                        caretTimer = new Timer(0, new ActionListener()
                        {
                                public void actionPerformed(ActionEvent evt)
                                {
                                        // If the filter is *not* persistent, then clear
                                        // it when the tree is expanded for the current
                                        // caret position.
                                        if (!jEdit.getBooleanProperty("sidekick.persistentFilter"))
                                        {
                                                if (searchField.getText().length() > 0)		// NOPMD
                                                {
                                                        searchField.setText("");
                                                        updateFilter(false);
                                                }
                                        }
                                        TextArea textArea = view.getTextArea();
                                        int caret = textArea.getCaretPosition();
                                        Selection s = textArea.getSelectionAtOffset(caret);
                                        expandTreeAt(s == null ? caret : s.getStart());
                                }
                        } );
                        caretTimer.setInitialDelay(500);
                        caretTimer.setRepeats(false);
                }
                caretTimer.start();
        }        // }}}

        // {{{ expandTreeAt() method
        protected void expandTreeAt(int dot)
        {
                if (data == null)
                {
                        return;
                }

                TreePath treePath = data.getTreePathForPosition(dot);
                if (treePath != null)
                {
                        tree.expandPath(treePath);
                        tree.setSelectionPath(treePath);
                        if (jEdit.getBooleanProperty("sidekick.scrollToVisible"))
                        {
                                Rectangle r = tree.getPathBounds(treePath);
                                if (r != null)
                                {
                                        r.width = 1;
                                        tree.scrollRectToVisible(r);
                                }
                                else
                                {
                                        tree.scrollPathToVisible(treePath);
                                }
                        }
                }
        }        // }}}

        protected void expandCurrentNode()
        {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                TreePath path = new TreePath(node.getPath());
                tree.expandPath(path);
        }

        protected void collapseCurrentNode()
        {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                TreePath path = new TreePath(node.getPath());
                tree.collapsePath(path);
        }

        // {{{ Inner classes

        // {{{ CustomTree class
        /**
         * A JTree with added mouse handling.  Other plugins providing similar trees
         * can extend CustomTree and override the mouse methods.
         */
        protected class CustomTree extends JTree
        {
                protected CustomTree(TreeModel model)
                {
                        super(model);
                }
                
		public Enumeration<TreePath> getExpandedDescendants(TreePath parent) 
		{
			return null;
		}
		
                protected void processMouseEvent(MouseEvent evt)
                {
                        switch (evt.getID() )
                        {
                                // {{{ MOUSE_PRESSED...
                                case MouseEvent.MOUSE_PRESSED:
                                        TreePath path = getPathForLocation(evt.getX(), evt.getY());
                                        if (path != null)
                                        {
                                                Object value = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();

                                                if (value instanceof IAsset)
                                                {
                                                        IAsset asset = (IAsset) value;

                                                        JEditTextArea textArea = view.getTextArea();
                                                        EditPane editPane = view.getEditPane();

                                                        if (evt.getClickCount() == 2)
                                                        {
                                                                doubleClicked(view, asset, path);
                                                        }
                                                        else if (evt.isShiftDown())
                                                        {
                                                                shiftClick(view, asset, path);
                                                        }
                                                        else if (evt.isControlDown())
                                                        {
                                                                controlClick(view, asset, path);
                                                        }
                                                        else
                                                        {
                                                                EditBus.send(new PositionChanging(editPane));
                                                                textArea.setCaretPosition(asset.getStart().getOffset());
                                                        }

                                                }
                                                if (!jEdit.getBooleanProperty("sidekick.persistentFilter"))
                                                {
                                                        if (searchField.getText().length() > 0)		// NOPMD
                                                        {
                                                                searchField.setText("");
                                                                updateFilter(false);
                                                        }
                                                }
                                        }
                                        super.processMouseEvent(evt);
                                        searchField.requestFocusInWindow();
                                        if (path != null)
                                        {
                                                selectPath(path);
                                        }
                                        break;                                        // }}}
                                        // {{{ MOUSE_EXITED...
                                case MouseEvent.MOUSE_EXITED:
                                        view.getStatus().setMessage(null);
                                        super.processMouseEvent(evt);
                                        break;                                        // }}}
                                default:
                                        super.processMouseEvent(evt);
                                        break;
                        }
                }

                protected void doubleClicked(View view, IAsset asset, TreePath path)
                {
                }

                protected void shiftClick(View view, IAsset asset, TreePath path)
                {
                        JEditTextArea textArea = view.getTextArea();
                        textArea.setCaretPosition(asset.getEnd().getOffset());
                        Selection.Range range = new Selection.Range(asset.getStart().getOffset(), asset.getEnd().getOffset());
                        textArea.addToSelection(range);
                }

                protected void controlClick(View view, IAsset asset, TreePath path)
                {
                        JEditTextArea textArea = view.getTextArea();
                        textArea.getDisplayManager().narrow(textArea.getLineOfOffset(asset.getStart().getOffset()), textArea.getLineOfOffset(asset.getEnd().getOffset()));
                }
        }        // }}}

        // {{{ ActionHandler class
        class ActionHandler implements ActionListener
        {
                /** A counter for counting how deep in recursion we are.
                 *  Since a call to reloadParserCombo can cause itemselected events
                 *  from the parserCombo,
                 */
                int level =0;
                public void actionPerformed(ActionEvent evt)
                {

                        // Workaround to avoid infinite recursion as a result of parsercombos
                        // updating
                        synchronized (this)
                        {
                                if (evt.getSource() == parseBtn)
                                {
                                        level = 0;
                                }
                                level++;
                                if (level > 1)
                                {
                                        level--;
                                        return;
                                }
                        }
                        Buffer b = view.getBuffer();
                        jEdit.setIntegerProperty("sidekick.splitter.location", splitter.getDividerLocation());
                        if (evt.getSource() == onSave)
                        {
                                SideKick.setParseOnSave(onSave.isSelected());
                                propertiesChanged();
                        }
                        if (evt.getSource() == followCaret)
                        {
                                boolean v = followCaret.isSelected();
                                SideKick.setGlobalFollowCaret(followCaret.isSelected());
                                if (v)
                                {
                                        onChange.setSelected(true);
                                }
                                propertiesChanged();
                        }
                        else if (evt.getSource() == onChange)
                        {
                                boolean v = onChange.isSelected();
                                SideKick.setParseOnChange(v);
                                if (!v)
                                {
                                        followCaret.setSelected(false);
                                }
                                propertiesChanged();
                        }
                        else if (evt.getSource() == parserCombo)
                        {
                                Object selectedParser = parserCombo.getSelectedItem();
                                // String preferredParser = b.getStringProperty(SideKickPlugin.PARSER_PROPERTY);
                                if (selectedParser.toString().equals(SideKickPlugin.NONE))
                                {
                                        b.setProperty("usermode", Boolean.TRUE);
                                        SideKickPlugin.setParserForBuffer(b, selectedParser.toString());
                                }
                                else if (selectedParser.toString().equals(SideKickPlugin.DEFAULT))
                                {
                                        b = view.getBuffer();
                                        b.setProperty("usermode", Boolean.FALSE);
                                        Mode m = b.getMode();
                                        if (m == null)
                                        {
                                                Log.log(Log.ERROR, this, "SideKick: can't determine mode of current buffer:" + b);
                                        }
                                        else
                                        {
                                                SideKickParser newParser = SideKickPlugin.getParserForMode(m);
                                                if (newParser == null)
                                                {
                                                        SideKickPlugin.setParserForBuffer(b, SideKickPlugin.NONE);
                                                }
                                                else
                                                {
                                                        SideKickPlugin.setParserForBuffer(b, newParser.getName());
                                                }
                                        }
                                }
                                else
                                {
                                        SideKickPlugin.setParserForBuffer(b, selectedParser.toString());
                                        b.setProperty("usermode", Boolean.TRUE);
                                }
                                propertiesChanged();

                        }
                        else if (evt.getSource() == parseBtn && stopIcon.equals(parseBtn.getIcon()))
                        {
                                SideKickPlugin.stop(view);
                                SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						parseBtn.setIcon(parseIcon);
						parseBtn.setToolTipText(jEdit.getProperty("sidekick-tree.parse"));
					}
				});
                        }
                        if (evt.getSource() == parseBtn || evt.getSource() == parserCombo)
                        {
                        	if (evt.getSource() == parseBtn) 
                        	{
                        		SwingUtilities.invokeLater(new Runnable()
						{
							public void run()
							{
								parseBtn.setIcon(stopIcon);
								parseBtn.setToolTipText(jEdit.getProperty("sidekick-tree.stop-parsing"));

							}
						});	
                        	}
                                level = 0;
                                Object usermode = b.getProperty("usermode");
                                if (usermode == null || usermode == Boolean.FALSE)
                                {
                                        SideKickParser sp = SideKickPlugin.getParserForBuffer(b);
                                        if (sp == null)
                                        {
                                                return;
                                        }
                                        else
                                        {
                                                reloadParserCombo();
                                        }
                                }
                                SideKickPlugin.parse(view, true);
                        }
                        level--;
                }
        }        // }}}

        // {{{ CaretHandler class
        class CaretHandler implements CaretListener
        {
                public void caretUpdate(CaretEvent evt)
                {
                        if (!view.getBuffer().equals(lastParsedBuffer))
                        {
                                return;
                        }
                        if (evt.getSource() == view.getTextArea() && SideKick.isFollowCaret())
                        {
                                expandTreeWithDelay();
                        }
                }
        }        // }}}

        protected void find_visible_nodes(HashSet<TreePath> set, DefaultMutableTreeNode node)
        {
                TreePath path = new TreePath(node.getPath());
                if (tree.isVisible(path) && tree.isExpanded(path))
                {
                        set.add(new TreePath(node.getPath()));
                        for (Enumeration e = node.children(); e.hasMoreElements();)
                        {
                                find_visible_nodes(set, (DefaultMutableTreeNode) e.nextElement());
                        }

                }
        }

        protected void filter_visible_nodes(FilteredTreeModel model, HashSet<TreePath> visible, DefaultMutableTreeNode node)
        {
                if (!jEdit.getBooleanProperty(SideKick.SHOW_FILTER))
                {
                        return;
                }

                TreePath path = new TreePath(node.getPath());
                if (!visible.contains(path))
                {
                        return;
                }

                tree.expandPath(path);
                for (Enumeration e = node.children(); e.hasMoreElements();)
                {
                        filter_visible_nodes(model, visible, (DefaultMutableTreeNode) e.nextElement());
                }
        }

        public void updateFilter(boolean with_delay)
        {
                if (!jEdit.getBooleanProperty(SideKick.SHOW_FILTER))
                {
                        return;
                }

                FilteredTreeModel ftm = (FilteredTreeModel) tree.getModel();

                if (searchField.getText().length() == 0)
                {
                        ftm.clearFilter();
                        ftm.reset();
                        if (autoExpandTree == -1)
                        {
                                expandAll(true);
                        }
                        else if (autoExpandTree == 0)
                        {
                                expandAll(false);
                        }
                        else if (autoExpandTree > 0)
                        {
                                tree.expandRow(0);
                                for (int i = 1; i < autoExpandTree; i++)
                                {
                                        for (int j = tree.getRowCount() - 1; j > 0; j--)
                                        {
                                                tree.expandRow(j);
                                        }

                                }
                        }

                        if (SideKick.isFollowCaret() && with_delay)
                        {
                                expandTreeWithDelay();
                        }
                }
                else
                {
                        HashSet<TreePath> visible = new HashSet<TreePath>();
                        find_visible_nodes(visible, (DefaultMutableTreeNode) ftm.getRoot());
                        ftm.filterByText(searchField.getText());
                        DefaultMutableTreeNode root = (DefaultMutableTreeNode) ftm.getRoot();
                        if (jEdit.getBooleanProperty(SideKick.FILTER_VISIBLE))
                        {
                                filter_visible_nodes(ftm, visible, root);
                        }
                        else
                        {
                                expandAll(true);
                        }
                }
        }

        public void updateFilter()
        {
                updateFilter(true);
        }

        public void setSearchFilter(String text)
        {
                searchField.setText(text);
                updateFilter();
        }
        public String getSearchFilter()
        {
                return searchField.getText();
        }

        // {{{ KeyHandler class
        class KeyHandler extends KeyAdapter
        {

                protected void next()
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                        if (node == null)
                        {
                                node = (DefaultMutableTreeNode) model.getRoot();
                        }

                        // standard tree movement for next:
                        // If selected node has children and selected node is expanded,
                        // then next is the first child of the selected node, otherwise,
                        // next is the next sibling.
                        // If selected node is a leaf, then next is next sibling.
                        // If next sibling is null, that means selected node is the
                        // last child of the parent node, so next is parent.nextSibling
                        if (node.getChildCount() > 0)
                        {
                                DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode) node.getFirstChild();
                                TreePath path = new TreePath(firstChild.getPath());
                                if (tree.isVisible(path))
                                {
                                        node = firstChild;
                                }
                                else
                                {
                                        node = (DefaultMutableTreeNode) node.getNextSibling();
                                }
                        }
                        else
                        {
                                // node is a leaf
                                DefaultMutableTreeNode next = (DefaultMutableTreeNode) node.getNextSibling();
                                if (next == null)
                                {
                                        // must be last child of parent
                                        next = (DefaultMutableTreeNode) ((DefaultMutableTreeNode) node.getParent()).getNextSibling();
                                }
                                node = next;
                        }

                        if (node != null)
                        {
                                TreePath p = new TreePath(node.getPath());
                                selectPath(p);
                        }
                }

                protected void nextLeaf()
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                        if (node == null)
                        {
                                node = (DefaultMutableTreeNode) model.getRoot();
                        }
                        if (model.isLeaf(node))
                        {
                                node = node.getNextLeaf();
                        }
                        else
                        {
                                Enumeration<DefaultMutableTreeNode> e = node.depthFirstEnumeration();
                                node = e.nextElement();
                        }
                        if (node != null)
                        {
                                while ((node != null) && !(model.isVisible(node) && tree.isVisible(new TreePath(node.getPath()))))
                                {
                                        node = node.getNextLeaf();
                                }
                                if (node != null)
                                {
                                        TreePath p = new TreePath(node.getPath());
                                        selectPath(p);
                                }
                        }
                }

                protected void prev()
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                        if (node == null)
                        {
                                node = (DefaultMutableTreeNode) model.getRoot();
                        }

                        // standard movement for previous:
                        // Initially, previous is the previous sibling.
                        // If previous sibling is null, then that means the current node
                        // is the first child of the parent node, so previous is the
                        // parent node.
                        // If previous sibling has children and is expanded, then previous
                        // is the last child of the previous sibling.
                        DefaultMutableTreeNode prev = node.getPreviousSibling();
                        if (prev == null)
                        {                                // could be first child
                                node = (DefaultMutableTreeNode) node.getParent();
                        }
                        else if (prev.getChildCount() > 0)
                        {
                                DefaultMutableTreeNode lastChild = (DefaultMutableTreeNode) prev.getLastChild();
                                TreePath path = new TreePath(lastChild.getPath());
                                if (tree.isVisible(path))
                                {
                                        node = lastChild;
                                }
                                else
                                {
                                        node = prev;
                                }
                        }
                        else
                        {
                                node = prev;
                        }

                        if (node != null)
                        {
                                TreePath p = new TreePath(node.getPath());
                                selectPath(p);
                        }
                }

                protected void prevLeaf()
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                        FilteredTreeModel model = (FilteredTreeModel) tree.getModel();
                        if (node == null)
                        {
                                node = (DefaultMutableTreeNode) model.getRoot();
                        }
                        // If the node isn't a leaf, use depthFirstEnumeration to find the next
                        // leaf (which moves us forward), then get the previous (to get the previous
                        // node from where we started).
                        if (!model.isLeaf(node))
                        {
                                Enumeration<DefaultMutableTreeNode> e = node.depthFirstEnumeration();
                                node = e.nextElement();
                        }
                        node = node.getPreviousLeaf();
                        if (node != null)
                        {
                                while ((node != null) && !(model.isVisible(node) && tree.isVisible(new TreePath(node.getPath()))))
                                {
                                        node = node.getPreviousLeaf();
                                }
                                if (node != null)
                                {
                                        TreePath p = new TreePath(node.getPath());
                                        selectPath(p);
                                }
                        }
                }

                public void keyPressed(KeyEvent evt)
                {
                        if (caretTimer != null)
                        {
                                caretTimer.stop();
                        }

                        switch (evt.getKeyCode() )
                        {
                                case KeyEvent.VK_ESCAPE:
                                        evt.consume();
                                        if (searchField.getText().length() == 0)
                                        {
                                                view.getDockableWindowManager().hideDockableWindow(SideKickPlugin.NAME);
                                        }
                                        else
                                        {
                                                searchField.setText("");
                                                updateFilter();
                                        }
                                        break;
                                case KeyEvent.VK_ENTER:
                                        evt.consume();

                                        TreePath path = tree.getSelectionPath();

                                        if (path != null)
                                        {
                                                Object value = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();

                                                if (value instanceof IAsset)
                                                {
                                                        IAsset asset = (IAsset) value;

                                                        JEditTextArea textArea = view.getTextArea();

                                                        if (evt.isShiftDown())
                                                        {
                                                                textArea.setCaretPosition(asset.getEnd().getOffset());
                                                                textArea.addToSelection(new Selection.Range(asset.getStart().getOffset(), asset.getEnd().getOffset() + 1));
                                                        }
                                                        else
                                                        {
                                                                if (!jEdit.getBooleanProperty("sidekick.persistentFilter"))
                                                                {
                                                                        if (searchField.getText().length() > 0)		// NOPMD
                                                                        {
                                                                                searchField.setText("");
                                                                                updateFilter();
                                                                        }
                                                                }
                                                                textArea.setCaretPosition(asset.getStart().getOffset());
                                                                selectPath(path);
                                                                textArea.requestFocus();
                                                        }
                                                }
                                        }
                                        break;
                                case KeyEvent.VK_BACK_SPACE:
                                        evt.consume();
                                        if (searchField.getText().length() <= 1)
                                        {
                                                searchField.setText("");
                                        }
                                        else
                                        {
                                                String s = searchField.getText();
                                                s = s.substring(0, s.length() - 1);
                                                searchField.setText(s);
                                        }
                                        updateFilter();
                                        break;
                                case KeyEvent.VK_DOWN:
                                        evt.consume();
                                        if (evt.isControlDown())
                                        {
                                                nextLeaf();
                                        }
                                        else
                                        {
                                                next();
                                        }
                                        break;
                                case KeyEvent.VK_UP:
                                        evt.consume();
                                        if (evt.isControlDown())
                                        {
                                                prevLeaf();
                                        }
                                        else
                                        {
                                                prev();
                                        }
                                        break;
                                case KeyEvent.VK_LEFT:
                                        collapseCurrentNode();
                                        break;
                                case KeyEvent.VK_RIGHT:
                                        expandCurrentNode();
                                        break;
                                case KeyEvent.VK_PAGE_UP:
                                        {
                                                evt.consume();

                                                int offset = tree.getScrollableUnitIncrement(tree.getParent().getBounds(), javax.swing.SwingConstants.VERTICAL, 0);
                                                for (int i = 0; i < offset; ++i)
                                                {
                                                        if (evt.isControlDown())
                                                        {
                                                                prevLeaf();
                                                        }
                                                        else
                                                        {
                                                                prev();
                                                        }
                                                }
                                        }
                                        break;
                                case KeyEvent.VK_PAGE_DOWN:
                                        {
                                                evt.consume();

                                                int offset = tree.getScrollableUnitIncrement(tree.getParent().getBounds(), javax.swing.SwingConstants.VERTICAL, 0);
                                                for (int i = 0; i < offset; ++i)
                                                {
                                                        if (evt.isControlDown())
                                                        {
                                                                nextLeaf();
                                                        }
                                                        else
                                                        {
                                                                next();
                                                        }
                                                }
                                        }
                                        break;
                                default:
                                        break;
                        }
                }

                public void keyTyped(KeyEvent evt)
                {
                        Character c = evt.getKeyChar();
                        // TODO: What is the correct combo here to filter
                        // non-identifier characters?
                        if (Character.isLetterOrDigit(c) || (" _!@$%^&*()_+-=[]{};':\",.<>/?\\|".indexOf(c) != -1))
                        {
                                evt.consume();
                                searchField.setText(searchField.getText() + c);
                                updateFilter();
                        }
                }
        }        // }}}

        // {{{ MouseHandler class
        class MouseHandler extends MouseMotionAdapter
        {
                public void mouseMoved(MouseEvent evt)
                {
                        TreePath path = tree.getPathForLocation(evt.getX(), evt.getY());
                        if (path == null)
                        {
                                view.getStatus().setMessage(null);
                        }
                        else
                        {
                                Object value = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();

                                if (value instanceof IAsset)
                                {
                                        String info = ((IAsset) value).getShortString();
                                        view.getStatus().setMessage(info);
                                }
                        }
                }
        }        // }}}

        // {{{ SidekickProperties class
        /**
         * This class creates an options dialog containing an optionpane
         * for each SideKick service, as well as one for SideKick itself.
         * This properties pane is mode-sensitive.
         *
         * sidekick options, and one for the specific plugin's option pane.
         */
        class SideKickProperties implements ActionListener
        {

                public void actionPerformed(ActionEvent e)
                {
                        try
                        {
                                new ModeOptionsDialog(view);
                        }
                        catch (Exception ex)
                        {
                                Log.log(Log.ERROR, this, "dialog create failed", ex);
                        }

                }

        }        // }}}

        // {{{ Renderer class
        class Renderer extends EnhancedTreeCellRenderer
        {
        		@Override
                protected void configureTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
                {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                        Object nodeValue = node.getUserObject();
                        if (nodeValue instanceof IAsset)
                        {
                                IAsset asset = (IAsset) node.getUserObject();

                                setText(asset.getShortString());
                                setIcon(asset.getIcon());
                        }
                        // is root?
                        else if (node.getParent() == null)
                        {
                                setIcon(org.gjt.sp.jedit.browser.FileCellRenderer.fileIcon);
                        }
                        else
                        {
                                setIcon(null);
                        }
                }

				@Override
				protected TreeCellRenderer newInstance()
				{
					return new Renderer();
				}
        }        // }}}


        // }}}
}