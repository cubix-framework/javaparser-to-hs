//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.frapuccino;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import org.frapuccino.addresscombobox.AddressComboBoxDemo;
import org.frapuccino.awt.WindowsUtil;
import org.frapuccino.checkablelist.CheckableListDemo;
import org.frapuccino.checkboxlist.CheckBoxListDemo;
import org.frapuccino.common.FrameBuilder;
import org.frapuccino.dynamicitemlistpanel.DynamicItemListDemo;
import org.frapuccino.editablelist.EditableListDemo;
import org.frapuccino.htmleditor.demo.HtmlEditorDemo;
import org.frapuccino.iconpanel.IconPanelDemo;
import org.frapuccino.swing.DynamicFileTransferHandlerDemo;
import org.frapuccino.swing.JTreeUtil;
import org.frapuccino.swing.MultipleTransferHandlerDemo;
import org.frapuccino.swing.SortedTreeModelDecorator;
import org.frapuccino.swing.SortedTreeModelDemo;
import org.frapuccino.threadarcs.ThreadArcsDemo;
import org.frapuccino.treetable.TreeTableDemo;
import org.frapuccino.treetable2.TreeTableDemo2;

/**
 * Demo application for all demos in the frapuccino module.
 *
 * @author redsolo
 */
public class AllDemos extends JPanel implements ActionListener, TreeSelectionListener {

    private JSplitPane mainSplitPane;
    private JSplitPane demoSplitPane;
    private JTextPane demoDescription;

    private MutableTreeNode treeRootNode;
    private DefaultTreeModel treeModel;

    /**
     * Creates a panel with all demo panels in a tabbed pane.
     */
    public AllDemos() {
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        demoSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        demoDescription = new JTextPane();
        demoDescription.setEditorKit(new HTMLEditorKit());
        demoDescription.setEditable(false);

        treeRootNode = new DefaultMutableTreeNode("Frappucino demos");
        treeModel = new DefaultTreeModel(treeRootNode);

        JTree tree = new JTree(new SortedTreeModelDecorator(treeModel));
        tree.getSelectionModel().addTreeSelectionListener(this);

        mainSplitPane.setLeftComponent(new JScrollPane(tree));
        mainSplitPane.setRightComponent(demoSplitPane);
        mainSplitPane.setDividerLocation(0.25);

        demoSplitPane.setDividerLocation(0.25);
        demoSplitPane.setResizeWeight(0.25);
        demoSplitPane.setTopComponent(new JScrollPane(demoDescription));
        demoSplitPane.setBottomComponent(new JLabel(""));

        addDemo(new EditableListDemo());
        addDemo(new CheckableListDemo());
        addDemo(new CheckBoxListDemo());
        addDemo(new IconPanelDemo());
        addDemo(new MultipleTransferHandlerDemo());
        addDemo(new SortedTreeModelDemo());
        addDemo(new DynamicFileTransferHandlerDemo());
        addDemo(new AddressComboBoxDemo());
        addDemo(new TreeTableDemo2());
        addDemo(new TreeTableDemo());
        addDemo(new ThreadArcsDemo());
        addDemo(new DynamicItemListDemo());
        addDemo(new HtmlEditorDemo());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(exitButton);

        JTreeUtil.expandAll(tree, true);

        setLayout(new BorderLayout());
        add(mainSplitPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(600, 500));
    }

    /**
     * Adds the demo component.
     * @param component the demo component.
     */
    private void addDemo(DemoComponent component) {
        treeModel.insertNodeInto(new DemoComponentTreeNode(component), treeRootNode, treeModel.getChildCount(treeRootNode));
    }

    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }

    /** {@inheritDoc} */
    public void valueChanged(TreeSelectionEvent e) {
        if (e.getPath().getLastPathComponent() instanceof DemoComponentTreeNode) {
            DemoComponentTreeNode demoNode = (DemoComponentTreeNode) e.getPath().getLastPathComponent();
            DemoComponent component = demoNode.getDemoComponent();
            demoSplitPane.setBottomComponent(component.getComponent());
            demoDescription.setText("<html><body><b>" + component.getDemoName() + "</b><hr>"
                    + component.getDescription() + "</body></html>");
            demoDescription.setCaretPosition(0);
        }
    }

    /**
     * Starts the demo.
     *
     * @param args arguments that are ignored.
     */
    public static void main(String[] args) {
        JFrame frame = FrameBuilder.createFrame(new AllDemos(), false);
        frame.setTitle("Frapuccino demo");

        WindowsUtil.centerInScreen(frame);
        frame.setVisible(true);
    }
}
