/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.ui;

import gruntspud.ui.icons.CompoundIcon;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.io.File;
import java.io.FileFilter;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class DirectorySelector
    extends JTree {
    //  Private instance variables
    private FileFilter filter;

    /**
     *  Constructor for the DirectorySelector object
     */
    public DirectorySelector() {
        this(null);
    }

    /**
     *  Constructor for the DirectorySelector object
     *
     *@param  root  Description of the Parameter
     */
    public DirectorySelector(File root) {
        super();
        setRootDirectory(root);
        setCellRenderer(new CheckableTreeCellRenderer());
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                toggleAtPoint(evt.getPoint());
            }
        });
    }

    /**
     *  Sets the fileFilter attribute of the DirectorySelector object
     *
     *@param  filter  The new fileFilter value
     */
    public void setFileFilter(FileFilter filter) {
        this.filter = filter;
        repaint();
    }

    /**
     *  Return an array files based on the criteria the current tree
     *
     *@param  selectedOnly  return only files that are currently selected
     *@param  filter        Description of the Parameter
     *@return               array of files
     */
    public File[] getAllFiles(boolean selectedOnly, FileFilter filter) {
        Vector v = new Vector();
        scanNodes( (FileNode) (getModel()).getRoot(), v, selectedOnly, filter);

        return new File[v.size()];
    }

    /**
     *  Description of the Method
     *
     *@param  node          Description of the Parameter
     *@param  v             Description of the Parameter
     *@param  selectedOnly  Description of the Parameter
     *@param  filter        Description of the Parameter
     */
    private void scanNodes(FileNode node, Vector v, boolean selectedOnly,
                           FileFilter filter) {
        File f = node.getFile();

        if ( ( (selectedOnly && node.isSelected()) || !selectedOnly) &&
            ( (filter == null) || ( (filter != null) && filter.accept(f)))) {
            v.addElement(f);

            if (!node.isLeaf()) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    scanNodes( (FileNode) node.getChildAt(i), v, selectedOnly,
                              filter);
                }
            }
        }
    }

    /**
     *  Gets the rootDirectory attribute of the DirectorySelector object
     *
     *@return    The rootDirectory value
     */
    public File getRootDirectory() {
        return ( (FileNode) getModel().getRoot()).getFile();
    }

    /**
     *  Sets the rootDirectory attribute of the DirectorySelector object
     *
     *@param  root  The new rootDirectory value
     */
    public void setRootDirectory(File root) {
        if (root == null) {
            ( (DefaultTreeModel) getModel()).setRoot(null);
        }
        else {
            ( (DefaultTreeModel) getModel()).setRoot(new FileNode(root));
        }
    }

    /**
     *  The main program for the DirectorySelector class
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });

        final DirectorySelector tree = new DirectorySelector(new File(
            System.getProperty("user.home")));
        f.getContentPane().setLayout(new BorderLayout());
        f.getContentPane().add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JCheckBox toggle = new JCheckBox("Disable", false);
        toggle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                tree.setEnabled(!toggle.isSelected());
            }
        });
        p.add(toggle);
        f.getContentPane().add(p, BorderLayout.SOUTH);
        f.pack();
        f.setVisible(true);
    }

    /**
     *  Description of the Method
     *
     *@param  point  Description of the Parameter
     */
    public void toggleAtPoint(Point point) {
        TreePath p = getPathForLocation(point.x, point.y);

        if (p != null) {
            TreeNode n = (TreeNode) p.getLastPathComponent();

            if (n instanceof FileNode) {
                FileNode node = (FileNode) p.getLastPathComponent();
                boolean someParentIsDeselected = false;
                FileNode par = (FileNode) node.getParent();

                while ( (par != null) && !someParentIsDeselected) {
                    if (!par.isSelected()) {
                        someParentIsDeselected = true;
                    }
                    else {
                        par = (FileNode) par.getParent();
                    }
                }

                boolean filtered = (filter != null) && node.getFile().isFile() &&
                    !filter.accept(node.getFile());

                if (!filtered && isEnabled() && !someParentIsDeselected) {
                    node.setSelected(!node.isSelected());
                    repaint();
                }
            }
        }
    }

    //  Supporting classes
    class FileNode
        extends DefaultMutableTreeNode {
        private boolean selected;
        private File file;
        private FileNode[] children;

        /**
         *  Constructor for the FileNode object
         *
         *@param  file  Description of the Parameter
         */
        public FileNode(File file) {
            super(file);
            this.file = file;
            selected = true;
        }

        public boolean isLeaf() {
            return file.isFile();
        }

        public int getChildCount() {
            loadChildren();

            return isLeaf() ? 0 : ( (children == null) ? 1 : children.length);
        }

        public void loadChildren() {
            if (file.isFile()) {
                return;
            }

            if (children == null) {
                File[] f = file.listFiles();

                if (f != null) {
                    children = new FileNode[f.length];

                    for (int i = 0; i < f.length; i++) {
                        children[i] = new FileNode(f[i]);
                        children[i].setParent(this);

                        //                    children[i].setParent(this);
                    }
                }
            }
        }

        public TreeNode getChildAt(int i) {
            loadChildren();

            return children[i];
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public File getFile() {
            return file;
        }

        public boolean isSelected() {
            return selected;
        }
    }

    class CheckableTreeCellRenderer
        extends JCheckBox
        implements TreeCellRenderer {
        protected boolean selected;
        protected boolean hasFocus;
        private boolean drawsFocusBorderAroundIcon;
        protected Color textSelectionColor;
        protected Color textNonSelectionColor;
        protected Color backgroundSelectionColor;
        protected Color backgroundNonSelectionColor;
        protected JTree tree;
        protected boolean someParentIsDeselected;
        protected CompoundIcon compoundIcon;
        protected File file;
        protected Stroke stroke;
        protected boolean filtered;
        protected Color borderSelectionColor;

        /**
         *  Constructor for the CheckableTreeCellRenderer object
         */
        public CheckableTreeCellRenderer() {
            setFont(UIManager.getFont("Tree.font"));
            setOpaque(false);
            setBorder(UIManager.getBorder("Label.border"));
            setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
            setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
            setBackgroundSelectionColor(UIManager.getColor(
                "Tree.selectionBackground"));
            setBackgroundNonSelectionColor(UIManager.getColor(
                "Tree.textBackground"));
            setBorderSelectionColor(UIManager.getColor(
                "Tree.selectionBorderColor"));

            Object value = UIManager.get("Tree.drawsFocusBorderAroundIcon");
            drawsFocusBorderAroundIcon = ( (value != null) &&
                                          ( (Boolean) value).booleanValue());
            compoundIcon = new CompoundIcon();
        }

        public void setTextSelectionColor(Color newColor) {
            textSelectionColor = newColor;
        }

        public Color getTextSelectionColor() {
            return textSelectionColor;
        }

        public void setTextNonSelectionColor(Color newColor) {
            textNonSelectionColor = newColor;
        }

        public Color getTextNonSelectionColor() {
            return textNonSelectionColor;
        }

        public void setBackgroundSelectionColor(Color newColor) {
            backgroundSelectionColor = newColor;
        }

        public Color getBackgroundSelectionColor() {
            return backgroundSelectionColor;
        }

        public void setBackgroundNonSelectionColor(Color newColor) {
            backgroundNonSelectionColor = newColor;
        }

        public Color getBackgroundNonSelectionColor() {
            return backgroundNonSelectionColor;
        }

        public void setBorderSelectionColor(Color newColor) {
            borderSelectionColor = newColor;
        }

        public Color getBorderSelectionColor() {
            return borderSelectionColor;
        }

        public void setFont(Font font) {
            if (font instanceof FontUIResource) {
                font = null;

            }
            super.setFont(font);
        }

        public Font getFont() {
            Font font = super.getFont();

            if ( (font == null) && (tree != null)) {
                font = tree.getFont();

            }
            return font;
        }

        public void setBackground(Color color) {
            if (color instanceof ColorUIResource) {
                color = null;

            }
            super.setBackground(color);
        }

        public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
            stroke = new BasicStroke(3);

            FileNode n = (FileNode) value;
            file = n.getFile();
            this.tree = tree;
            this.hasFocus = hasFocus;
            setText(file.getName());
            setSelected(n.isSelected());

            if (sel) {
                setForeground(getTextSelectionColor());
            }
            else {
                setForeground(getTextNonSelectionColor());

            }
            someParentIsDeselected = false;

            FileNode p = (FileNode) n.getParent();

            while ( (p != null) && !someParentIsDeselected) {
                if (!p.isSelected()) {
                    someParentIsDeselected = true;
                }
                else {
                    p = (FileNode) p.getParent();
                }
            }

            filtered = (filter != null) && file.isFile() &&
                !filter.accept(file);

            if (filtered || !tree.isEnabled() || someParentIsDeselected) {
                setEnabled(false);
            }
            else {
                setEnabled(true);

            }
            compoundIcon.setIcon1(UIManager.getIcon("CheckBox.icon"));

            Icon treeIcon = null;

            if (n.isLeaf()) {
                treeIcon = UIManager.getIcon("Tree.leafIcon");
            }
            else if (expanded) {
                treeIcon = UIManager.getIcon("Tree.openIcon");
            }
            else {
                treeIcon = UIManager.getIcon("Tree.closedIcon");

            }
            compoundIcon.setIcon2(treeIcon);
            setComponentOrientation(tree.getComponentOrientation());
            selected = sel;

            return this;
        }

        public Icon getIcon() {
            return compoundIcon;
        }

        public void paint(Graphics g) {
            Color bColor;

            if (selected) {
                bColor = getBackgroundSelectionColor();
            }
            else {
                bColor = getBackgroundNonSelectionColor();

                if (bColor == null) {
                    bColor = getBackground();
                }
            }

            int imageOffset = -1;

            if (bColor != null) {
                Icon currentI = getIcon();

                imageOffset = getLabelStart();
                g.setColor(bColor);

                if (getComponentOrientation().isLeftToRight()) {
                    g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset,
                               getHeight());
                }
                else {
                    g.fillRect(0, 0, getWidth() - 1 - imageOffset, getHeight());
                }
            }

            if (hasFocus) {
                if (drawsFocusBorderAroundIcon) {
                    imageOffset = 0;
                }
                else if (imageOffset == -1) {
                    imageOffset = getLabelStart();

                }
                Color bsColor = getBorderSelectionColor();

                if (bsColor != null) {
                    g.setColor(bsColor);

                    if (getComponentOrientation().isLeftToRight()) {
                        g.drawRect(imageOffset, 0,
                                   getWidth() - 1 - imageOffset,
                                   getHeight() - 1);
                    }
                    else {
                        g.drawRect(0, 0, getWidth() - 1 - imageOffset,
                                   getHeight() - 1);
                    }
                }
            }

            super.paint(g);

            if (filtered) {
                g.setColor(Color.red);
                ( (Graphics2D) g).setRenderingHint(RenderingHints.
                    KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                double d = getSize().height / 2d;
                Line2D l = new Line2D.Double(0, d, getSize().width, d);
                ( (Graphics2D) g).setStroke(stroke);
                ( (Graphics2D) g).draw(l);
            }
        }

        private int getLabelStart() {
            Icon currentI = getIcon();

            if ( (currentI != null) && (getText() != null)) {

                //  1.4
                //                return currentI.getIconWidth() + Math.max(0, getIconTextGap() - 1);
                //  1.3
                return currentI.getIconWidth() + 1;
            }

            return 0;
        }

        public Dimension getPreferredSize() {
            Dimension retDimension = super.getPreferredSize();

            if (retDimension != null) {
                retDimension = new Dimension(retDimension.width + 3,
                                             retDimension.height);

            }
            return retDimension;
        }

        public void validate() {
        }

        public void revalidate() {
        }

        public void repaint(long tm, int x, int y, int width, int height) {
        }

        public void repaint(Rectangle r) {
        }

        protected void firePropertyChange(String propertyName, Object oldValue,
                                          Object newValue) {
            // Strings get interned...
            if (propertyName == "text") {
                super.firePropertyChange(propertyName, oldValue, newValue);
            }
        }

        public void firePropertyChange(String propertyName, byte oldValue,
                                       byte newValue) {
        }

        public void firePropertyChange(String propertyName, char oldValue,
                                       char newValue) {
        }

        public void firePropertyChange(String propertyName, short oldValue,
                                       short newValue) {
        }

        public void firePropertyChange(String propertyName, int oldValue,
                                       int newValue) {
        }

        public void firePropertyChange(String propertyName, long oldValue,
                                       long newValue) {
        }

        public void firePropertyChange(String propertyName, float oldValue,
                                       float newValue) {
        }

        public void firePropertyChange(String propertyName, double oldValue,
                                       double newValue) {
        }

        public void firePropertyChange(String propertyName, boolean oldValue,
                                       boolean newValue) {
        }
    }
}
