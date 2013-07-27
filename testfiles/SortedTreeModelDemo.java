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
package org.frapuccino.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.frapuccino.DemoComponent;
import org.frapuccino.common.FrameBuilder;
import org.frapuccino.swing.SortedJTree;

/**
 * A swing demo to show of the tree model sorter.
 *
 * @author redsolo
 */
public class SortedTreeModelDemo extends JPanel implements DemoComponent, ItemListener, ActionListener, TreeSelectionListener {

    private JCheckBox useDecorator;

    private SortedJTree tree;

    private TreeModel unsortedTreeModel;

    private DefaultMutableTreeNode rootNode;

    private JTextField nodeField;
    private JButton updateNodeButton;

    /**
     * Demos the @see SortedTreeModelDecorator class.
     */
    public SortedTreeModelDemo() {
        super();

        setLayout(new BorderLayout());

        useDecorator = new JCheckBox("Sorted tree model");
        useDecorator.addItemListener(this);
        tree = new SortedJTree();
        tree.getSelectionModel().addTreeSelectionListener(this);
        tree.setSortingEnabled(false);
        nodeField = new JTextField();
        updateNodeButton = new JButton("Update");
        updateNodeButton.addActionListener(this);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BorderLayout());
        textPanel.add(nodeField, BorderLayout.CENTER);
        textPanel.add(updateNodeButton, BorderLayout.EAST);

        rootNode = new DefaultMutableTreeNode();
        unsortedTreeModel = new DefaultTreeModel(rootNode);

        rootNode.add(new DefaultMutableTreeNode("aa"));
        rootNode.add(new DefaultMutableTreeNode("zz"));
        DefaultMutableTreeNode hNode = new DefaultMutableTreeNode("h");
        hNode.add(new DefaultMutableTreeNode("xx"));
        hNode.add(new DefaultMutableTreeNode("gg"));
        hNode.add(new DefaultMutableTreeNode("tt"));
        rootNode.add(hNode);
        rootNode.add(new DefaultMutableTreeNode("qq"));
        rootNode.add(new DefaultMutableTreeNode("dd"));
        DefaultMutableTreeNode bNode = new DefaultMutableTreeNode("bb");
        bNode.add(new DefaultMutableTreeNode("gg"));
        bNode.add(new DefaultMutableTreeNode("cc"));
        bNode.add(new DefaultMutableTreeNode("dd"));
        rootNode.add(bNode);

        tree.setModel(unsortedTreeModel);

        add(useDecorator, BorderLayout.NORTH);
        add(tree, BorderLayout.CENTER);
        add(textPanel, BorderLayout.SOUTH);
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public String getDescription() {
        return "A TreeModel decorator that sorts any Treemodel object. By default the tree model"
            + " is sorted alphabetically from the user object toString() method."
            + " The decorator can take any Comparator object when sorting the tree model."
            + " Note that this implementation is not yet finished.";
    }

    /** {@inheritDoc} */
    public String getDemoName() {
        return "Sorted tree model";
    }

    /**
     * Starts the demo.
     * @param args ignored arguments.
     */
    public static void main(String[] args) {

        JFrame frame = FrameBuilder.createFrame(new SortedTreeModelDemo());
        frame.setVisible(true);
    }

    /**
     * Returns a suitable demo component.
     * @return a component.
     */
    public static JComponent createDemoComponent() {
        return new SortedTreeModelDemo();
    }

    /** {@inheritDoc} */
    public void actionPerformed(ActionEvent e) {
        TreePath path = tree.getSelectionPath();

        if (path != null) {
            DefaultMutableTreeNode node = ((DefaultMutableTreeNode) path.getLastPathComponent());
            node.setUserObject(nodeField.getText());
            tree.getModel().valueForPathChanged(path, nodeField.getText());
        }
    }

    /** {@inheritDoc} */
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getPath();
        nodeField.setText(path.getLastPathComponent().toString());
    }

    /** {@inheritDoc} */
    public void itemStateChanged(ItemEvent e) {

        if (useDecorator.isSelected()) {
            tree.setSortingEnabled(true);
        } else {
            tree.setSortingEnabled(false);
        }
    }
}
