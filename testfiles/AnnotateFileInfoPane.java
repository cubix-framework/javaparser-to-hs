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

package gruntspud.ui.report;

import gruntspud.ColorUtil;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.UIUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateInformation;
import org.netbeans.lib.cvsclient.command.annotate.AnnotateLine;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class AnnotateFileInfoPane
    extends FileInfoPane {
    //  Private instance variables
    private JLabel file;

    //  Private instance variables
    private JLabel type;

    //  Private instance variables
    private JLabel directory;
    private AnnotateInfoTable annotation;

    /**
     * Constructor
     */
    public AnnotateFileInfoPane(GruntspudContext context) {
        super(context);
        setLayout(new GridBagLayout());

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;

        UIUtil.jGridBagAdd(this, new JLabel("File: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        file = new JLabel() {
            public Dimension getPreferredSize() {
                return new Dimension(260,
                                     super.getPreferredSize().height);
            }
        };
        file.setFont(valFont);
        UIUtil.jGridBagAdd(this, file, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Directory: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directory = new JLabel() {
            public Dimension getPreferredSize() {
                return new Dimension(260,
                                     super.getPreferredSize().height);
            }
        };
        directory.setFont(valFont);
        UIUtil.jGridBagAdd(this, directory, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 2.0;
        gbc.weighty = 1.0;

        JScrollPane scroller = new JScrollPane(annotation =
                                               new AnnotateInfoTable());
        scroller.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(4, 4, 4, 4),
            BorderFactory.createLineBorder(Color.black)));
        UIUtil.jGridBagAdd(this, scroller, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     */
    public void cleanUp() {
        UIUtil.saveTableMetrics(annotation,
                                Constants.ANNOTATE_INFO_TABLE_GEOMETRY,
                                getContext());
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        return "";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TableCellRenderer getInfoRenderer() {
        return null;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Class getInfoClass() {
        return String.class;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_ANNOTATE);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_ANNOTATE);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "Annotation";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        AnnotateInformation info = (AnnotateInformation) container;
        file.setText(info.getFile().getName());
        file.setToolTipText(container.getFile().getAbsolutePath());

        directory.setText(info.getFile().getParentFile().getName());
        directory.setToolTipText(info.getFile().getParentFile().getAbsolutePath());

        ( (AnnotateInfoTableModel) annotation.getModel()).setInfo( (
            AnnotateInformation) info);
    }

    //
    class AnnotateInfoTable
        extends JTable {
        AnnotateInfoTable() {
            super();
            setModel(new AnnotateInfoTableModel());
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            AnnotateTableCellRenderer w = new AnnotateTableCellRenderer();
            setDefaultRenderer(String.class, w);
            setDefaultRenderer(Integer.class, w);
            setBackground(ColorUtil.getColor(
                Constants.OPTIONS_EDITOR_BACKGROUND,
                UIManager.getColor("Table.background"), getContext()));
            setForeground(ColorUtil.getColor(
                Constants.OPTIONS_EDITOR_FOREGROUND,
                UIManager.getColor("Table.foreground"), getContext()));
            setShowHorizontalLines(false);
            UIUtil.restoreTableMetrics(this,
                                       Constants.ANNOTATE_INFO_TABLE_GEOMETRY,
                                       new int[] {40, 64, 40, 600}
                                       , getContext());
        }
    }

    class AnnotateInfoTableModel
        extends AbstractTableModel {
        private Vector lines;

        AnnotateInfoTableModel() {
            lines = new Vector();
        }

        public void setInfo(AnnotateInformation info) {
            lines.removeAllElements();

            AnnotateLine l = info.getFirstLine();

            while (l != null) {
                lines.addElement(l);
                l = info.getNextLine();
            }

            fireTableDataChanged();
        }

        public int getRowCount() {
            return lines.size();
        }

        public int getColumnCount() {
            return 4;
        }

        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "Line";
                case 1:
                    return "Author";
                case 2:
                    return "Revision";
                default:
                    return "";
            }
        }

        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return Integer.class;
                default:
                    return String.class;
            }
        }

        public Object getValueAt(int r, int c) {
            AnnotateLine l = (AnnotateLine) lines.elementAt(r);

            switch (c) {
                case 0:
                    return l.getLineNumInteger();
                case 1:
                    return l.getAuthor();
                case 2:
                    return l.getRevision();
                default:
                    return l.getContent();
            }
        }
    }

    class ContentWrapper {
        String content;

        ContentWrapper(String content) {
            this.content = content;
        }
    }

    class AnnotateTableCellRenderer
        extends DefaultTableCellRenderer {
        private Font fixedFont;
        private Font fixedBoldFont;

        AnnotateTableCellRenderer() {
            super();

            Font f = UIManager.getFont("Table.font");
            fixedFont = new Font("Monospaced", f.getStyle(), f.getSize());
            fixedBoldFont = fixedFont.deriveFont(fixedFont.getStyle() |
                                                 Font.BOLD);
        }

        public Component getTableCellRendererComponent(JTable table,
            Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                                                hasFocus, row, column);
            setFont( (column == 3) ? fixedFont : fixedBoldFont);

            return this;
        }
    }
}
