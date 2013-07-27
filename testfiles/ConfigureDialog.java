/*
 * jEdit editor settings:
 * :mode=java:tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * ConfigureDialog.java - JIndex configure dialog
 * Copyright (C) 1999 Dirk Moebius
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

package jindex;

import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.Vector;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.EnhancedDialog;


public class ConfigureDialog extends EnhancedDialog {

    // class members
    private static JFileChooser chooser = null;

    // private members
    private JButton bAdd;
    private JButton bDel;
    private JButton bOk;
    private JButton bCancel;
    private JButton bRecreate;
    private JTable apiTable;
    private ApiTableModel apiModel;


    public ConfigureDialog(Frame parent) {
        super(parent, jEdit.getProperty("options.jindex.createIndex"), true);

        // create library table
        JScrollPane scrollTable = createLibraryTable();

        // create "Add"/"Remove" library buttons
        JPanel boxLibButtons = new JPanel(new BorderLayout(5,5));
        boxLibButtons.add(bAdd = new JButton(jEdit.getProperty("options.jindex.addLibEntry")), BorderLayout.NORTH);
        JPanel boxLibButtons2 = new JPanel(new BorderLayout(5,5));
        boxLibButtons2.add(bDel = new JButton(jEdit.getProperty("options.jindex.delLibEntry")), BorderLayout.NORTH);
        boxLibButtons.add(boxLibButtons2, BorderLayout.CENTER);

        // create library panel
        Box boxLib = Box.createHorizontalBox();
        boxLib.add(scrollTable);
        boxLib.add(Box.createHorizontalStrut(10));
        boxLib.add(boxLibButtons);

        // create "Ok"/"Cancel"/"Recreate" buttons
        Box boxButtons = Box.createHorizontalBox();
        boxButtons.add(Box.createHorizontalGlue());
        boxButtons.add(bOk = new JButton(jEdit.getProperty("options.jindex.ok")));
        boxButtons.add(Box.createHorizontalStrut(5));
        boxButtons.add(bCancel = new JButton(jEdit.getProperty("options.jindex.cancel")));
        boxButtons.add(Box.createHorizontalStrut(5));
        boxButtons.add(bRecreate = new JButton(jEdit.getProperty("options.jindex.recreate")));
        boxButtons.add(Box.createHorizontalGlue());

        // complete layout
        JPanel stage = new JPanel(new BorderLayout(10,10));
        stage.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        stage.add(new JLabel(jEdit.getProperty( "options.jindex.table.label")), BorderLayout.NORTH);
        stage.add(boxLib, BorderLayout.CENTER);
        stage.add(boxButtons, BorderLayout.SOUTH);

        getContentPane().add(stage);

        ActionHandler ah = new ActionHandler();
        bOk.addActionListener(ah);
        bCancel.addActionListener(ah);
        bRecreate.addActionListener(ah);
        bAdd.addActionListener(ah);
        bDel.addActionListener(ah);

        pack();
        if (parent != null)
            setLocationRelativeTo(parent);
        setVisible(true);
    }


    private JScrollPane createLibraryTable() {
        apiModel = new ApiTableModel();

        apiTable = new JTable(apiModel);
        apiTable.getTableHeader().setReorderingAllowed(false);
        apiTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        apiTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        apiTable.setPreferredScrollableViewportSize(new Dimension(630, 200));
        apiTable.setDefaultRenderer(String.class, new EditableTableCellRenderer());
        apiTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        apiTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        apiTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        apiTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        String[] visibilities = new String[] {
            jEdit.getProperty("options.jindex.table.public"),
            jEdit.getProperty("options.jindex.table.protected"),
            jEdit.getProperty("options.jindex.table.package"),
            jEdit.getProperty("options.jindex.table.private")
        };


        TableColumn col3 = apiTable.getColumnModel().getColumn(3);
        // cell renderer for column 3:
        ComboCellRenderer combo = new ComboCellRenderer(visibilities);
        combo.setRequestFocusEnabled(false);
        col3.setCellRenderer(combo);
        // cell editor for column 3:
        combo = new ComboCellRenderer(visibilities);
        combo.setRequestFocusEnabled(false);
        col3.setCellEditor(new DefaultCellEditor(combo));
        
        apiTable.setRowHeight(combo.getPreferredSize().height);

        return new JScrollPane(apiTable);
    }


    /**
     * create/update index file
     */
    private void createIndex() {
        if (JIndexHolder.getInstance().indexExists()) {
            // warning: index already exists. Overwrite?
            int result = JOptionPane.showConfirmDialog(null,
                jEdit.getProperty("options.jindex.error.overwrite.message",
                    new Object[] { JIndexHolder.getInstance().getIndexFilename()}
                ),
                jEdit.getProperty("options.jindex.error.overwrite.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            if (result != JOptionPane.YES_OPTION)
                return;
        }

        apiModel.save();

        JIndexHolder.getInstance().createIndex(apiModel.getLibs());
    }


    protected void createFileChooser() {
        if (chooser != null) return;

        chooser = new JFileChooser();
        chooser.setDialogTitle(
            jEdit.getProperty("options.jindex.addLibEntry.title"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() ||
                       f.getName().toLowerCase().endsWith(".jar") ||
                       f.getName().toLowerCase().endsWith(".zip");
            }
            public String getDescription() {
                return jEdit.getProperty(
                    "options.jindex.addLibEntry.filefilter");
            }
        });
    }


    private void addEntry() {
        createFileChooser();
        int retVal = chooser.showOpenDialog(ConfigureDialog.this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String fname = f.getAbsolutePath();
            if (apiModel.exists(fname)) {
                GUIUtilities.error(null, "options.jindex.error.libexists", null);
            } else {
                String doc = GUIUtilities.input(null,
                                                "options.jindex.inputDoc",
                                                "file:/");
                if (doc != null) {
                    if (doc.charAt(doc.length()-1) != '/')
                        doc += '/';
                    apiModel.add(new LibEntry(fname, doc, false,
                                              LibEntry.PROTECTED));
                }
            }
        }
    }


    public void ok() {
        // check entries, output errors, if any
        if (apiModel.entriesOk()) {
            int status = JIndexHolder.getInstance().getStatus();

            if (apiModel.libsChanged || status == JIndexHolder.STATUS_NOT_EXISTS || status == JIndexHolder.STATUS_LOAD_ERROR)
                createIndex();
            else
                apiModel.save();

            setVisible(false);
            dispose();
        }
    }


    public void cancel() {
        setVisible(false);
        dispose();
    }


    private class ActionHandler implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            JButton button = (JButton) evt.getSource();
            if (button == bAdd) {
                addEntry();
            }
            else if (button == bDel) {
                // delete selected table entry
                int rows[] = apiTable.getSelectedRows();
                if (rows.length == 0) {
                    GUIUtilities.error(null,
                        "options.jindex.error.noselection", null);
                } else {
                    for (int i = rows.length - 1; i >= 0; i--) {
                        apiModel.delete(rows[i]);
                    }
                }
            }
            else if (button == bOk) {
                ok();
            }
            else if (button == bRecreate) {
                // check entries, output errors, if any
                if (apiModel.entriesOk())
                    createIndex();
            }
            else if (button == bCancel) {
                cancel();
            }
        }

    } // inner class ActionHandler


    private class ApiTableModel extends AbstractTableModel {

        private Vector libs;
        public boolean libsChanged = false;


        ApiTableModel() {
            libs = new Vector();
            int count = 0;
            // load libs from properties:
            do {
                String lname = jEdit.getProperty("jindex.lib.name." + count);
                if (lname == null || lname.length() == 0) break;
                String ldoc = jEdit.getProperty("jindex.lib.doc." + count);
                boolean isOldJavaDoc = "true".equals(jEdit.getProperty(
                    "jindex.lib.oldjdoc." + count));
                String vis = jEdit.getProperty("jindex.lib.visibility." + count);
                add(new LibEntry(lname, ldoc, isOldJavaDoc, string2vis(vis)));
                count++;
            } while (true);
            libsChanged = false;
        }


        private int string2vis(String vis) {
            int val = LibEntry.PROTECTED;
            if (jEdit.getProperty("options.jindex.table.public").equals(vis))
                val = LibEntry.PUBLIC;
            else if (jEdit.getProperty("options.jindex.table.package").equals(vis))
                val = LibEntry.PACKAGE;
            else if (jEdit.getProperty("options.jindex.table.private").equals(vis))
                val = LibEntry.PRIVATE;
            return val;
        }


        private String vis2string(int vis) {
            switch (vis) {
                case LibEntry.PUBLIC:
                    return jEdit.getProperty("options.jindex.table.public");
                case LibEntry.PACKAGE:
                    return jEdit.getProperty("options.jindex.table.package");
                case LibEntry.PRIVATE:
                    return jEdit.getProperty("options.jindex.table.private");
                default:
            }
            return jEdit.getProperty("options.jindex.table.protected");
        }


        public void add(LibEntry l) {
            libs.addElement(l);
            libsChanged = true;
            fireTableRowsInserted(libs.size()-1, libs.size()-1);
        }


        public void delete(int row) {
            libs.removeElementAt(row);
            libsChanged = true;
            fireTableRowsDeleted(row, row);
        }


        public boolean exists(String filename) {
            for (Enumeration e = libs.elements(); e.hasMoreElements(); ) {
                LibEntry l = (LibEntry) e.nextElement();
                if (filename.equals(l.lib)) return true;
            }
            return false;
        }


        public LibEntry[] getLibs() {
            LibEntry[] l_array = new LibEntry[libs.size()];
            libs.copyInto(l_array);
            return l_array;
        }


        public int getColumnCount() {
            return 4;
        }


        public int getRowCount() {
            return libs.size();
        }


        public Object getValueAt(int row, int col) {
            Object obj = null;
            if (row < libs.size()) {
                LibEntry l = (LibEntry) libs.elementAt(row);
                switch (col) {
                    case 0: obj = l.lib; break;
                    case 1: obj = l.doc; break;
                    case 2: obj = new Boolean(l.isOldJavaDoc); break;
                    case 3: obj = vis2string(l.visibility); break;
                }
            }
            return obj;
        }


        public boolean isCellEditable(int row, int col) {
            // return col > 0;
            return true;
        }


        public void setValueAt(Object value, int row, int col) {
            LibEntry l = (LibEntry) libs.elementAt(row);
            switch (col) {
                case 1:
                    l.doc = value.toString();
                    if (l.doc.charAt(l.doc.length()-1) != '/')
                        l.doc += '/';
                    break;
                case 2:
                    l.isOldJavaDoc = ((Boolean)value).booleanValue();
                    break;
                case 3:
                    int old_visibility = l.visibility;
                    l.visibility = string2vis(value.toString());
                    if (old_visibility != l.visibility) {
                        libsChanged = true;
                    }
                    break;
                default:
                    break;
            }
            fireTableRowsUpdated(row,row);
        }


        public String getColumnName(int index) {
            String ret = "";
            switch (index) {
                case 0: ret = jEdit.getProperty("options.jindex.table.col0"); break;
                case 1: ret = jEdit.getProperty("options.jindex.table.col1"); break;
                case 2: ret = jEdit.getProperty("options.jindex.table.col2"); break;
                case 3: ret = jEdit.getProperty("options.jindex.table.col3"); break;
            }
            return ret;
        }


        public Class getColumnClass(int index) {
            Class c = null;
            switch (index) {
                case 0: c = String.class; break;
                case 1: c = String.class; break;
                case 2: c = Boolean.class; break;
                case 3: c = String.class; break;
            }
            return c;
        }


        public void save() {
            int count = 0;
            for (int i = 0; i < libs.size(); i++) {
                LibEntry l = (LibEntry) libs.elementAt(i);
                if (l == null) continue;
                if (l.lib == null || l.lib.length() == 0) continue;
                if (l.doc == null || l.doc.length() == 0) continue;
                jEdit.setProperty("jindex.lib.name." + count, l.lib);
                jEdit.setProperty("jindex.lib.doc." + count, l.doc);
                jEdit.setProperty("jindex.lib.oldjdoc." + count,
                    l.isOldJavaDoc ? "true" : "false");
                jEdit.setProperty("jindex.lib.visibility." + count,
                    vis2string(l.visibility));
                count++;
            }
            jEdit.unsetProperty("jindex.lib.name." + count);
            jEdit.unsetProperty("jindex.lib.doc." + count);
            jEdit.unsetProperty("jindex.lib.oldjdoc." + count);
            jEdit.unsetProperty("jindex.lib.visibility." + count);
        }


        public boolean entriesOk() {
            if (libs.size() == 0) {
                GUIUtilities.error(null, "options.jindex.error.nolibs", null);
                return false;
            }

            boolean error = false;
            for (int i = 0; i < libs.size(); i++) {
                LibEntry l = (LibEntry) libs.elementAt(i);
                // check lib file name
                if (l.lib == null || l.lib.length() == 0) {
                    GUIUtilities.error(null,
                        "options.jindex.error.libmissing",
                        new Object[] { new Integer(i + 1) }
                    );
                    error = true;
                    continue;
                }
                // check lib file
                if (l.getLibFile() == null) {
                    GUIUtilities.error(null,
                        "options.jindex.error.libwrong",
                        new Object[] { l.lib, new Integer(i + 1) }
                    );
                    error = true;
                    continue;
                }
                // check doc folder name
                if (l.doc == null || l.doc.length() == 0) {
                    GUIUtilities.error(null,
                        "options.jindex.error.docmissing",
                        new Object[] { new Integer(i + 1) }
                    );
                    error = true;
                    continue;
                }
                // check doc folder url
                try {
                    java.net.URL u = new java.net.URL(l.doc);
                }
                catch (java.net.MalformedURLException e) {
                    GUIUtilities.error(null,
                        "options.jindex.error.docwrong",
                        new Object[] { l.doc, new Integer(i + 1), e }
                    );
                    error = true;
                    continue;
                }
            } // while
            return !error;
        }

    } // inner class ApiTableModel


    protected final static Color colNorm    = UIManager.getColor("Table.background");
    protected final static Color colNormSel = UIManager.getColor("Table.selectionBackground");
    protected final static Color colDis     = UIManager.getColor("Label.background");
    protected final static Color colDisSel  = colDis.darker();


    private class EditableTableCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col)
        {
            //JLabel comp = (JLabel) super.getTableCellRendererComponent(
            //    table, value, isSelected, hasFocus, row, col
            //);
            super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, col
            );

            setOpaque(true);

            if (table.isCellEditable(row, col))
                setBackground(isSelected ? colNormSel : colNorm);
            else
                setBackground(isSelected ? colDisSel: colDis);

            return this;
        }

    }


    private class ComboCellRenderer extends JComboBox implements TableCellRenderer {

        ComboCellRenderer(String[] model) { super(model); }

        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
        {
            setSelectedItem(value);
            return this;
        }

    }

}
