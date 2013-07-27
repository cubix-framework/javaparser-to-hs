/*
 *  Copyright (C) 2003 Don Brown (mrdon@techie.com)
 *  Copyright (C) 2000, 2001 Greg Merrill (greghmerrill@yahoo.com)
 *  This file is part of Log Viewer, a plugin for jEdit (http://www.jedit.org).
 *  It is heavily  based off Follow (http://follow.sf.net).
 *  Log Viewer is free software; you can redistribute it and/or modify
 *  it under the terms of version 2 of the GNU General Public
 *  License as published by the Free Software Foundation.
 *  Log Viewer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License
 *  along with Log Viewer; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package logviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

// from jEdit:
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.util.Log;

/**
 *  The option pane to configure the log viewer
 *
 * @author    <a href="mailto:mrdon@techie.com">Don Brown</a>
 * @author    <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
 */
public class LogViewerOptionPane implements OptionPane {

    CfgPanel panel = null;
    LogViewerAttributes attribs;

    private TabPlacementValue TOP = new TabPlacementValue(JTabbedPane.TOP);
    private TabPlacementValue BOTTOM = new TabPlacementValue(JTabbedPane.BOTTOM);
    private TabPlacementValue LEFT = new TabPlacementValue(JTabbedPane.LEFT);
    private TabPlacementValue RIGHT = new TabPlacementValue(JTabbedPane.RIGHT);
    private TabPlacementValue[] ALL_TAB_PLACEMENT_VALUES =
            new TabPlacementValue[]{TOP, BOTTOM, LEFT, RIGHT};

    /**  Constructor */
    public LogViewerOptionPane() {
        attribs = new LogViewerAttributes();
        panel = new CfgPanel();
        init();
    }

    /**
     *  Gets the style display values
     *
     * @return    The styleDisplayValues value
     */
    private static String[] getStyleDisplayValues() {
        return new String[]{
                LogViewer.getProperty("dialog.Configure.font.plain.displayValue"),
                LogViewer.getProperty("dialog.Configure.font.bold.displayValue"),
                LogViewer.getProperty("dialog.Configure.font.italic.displayValue"),
                LogViewer.getProperty("dialog.Configure.font.boldItalic.displayValue")
                };
    }

    /**  Saves the values */
    public void save() {
        panel.save();
    }

    /**
     *  Gets the component
     *
     * @return    The component value
     */
    public Component getComponent() {
        if (panel == null) {
            panel = new CfgPanel();
        }
        return panel;
    }

    /**
     *  Gets the name
     *
     * @return    The name value
     */
    public String getName() {
        return LogViewerPlugin.NAME;
    }

    /**  Initializes the values */
    public void init() {
        panel.bufferSize_.setText(
                String.valueOf(attribs.getBufferSize())
                );
        panel.latency_.setText(String.valueOf(attribs.getLatency()));
        panel.tabPlacement_.setSelectedItem(
                new TabPlacementValue(attribs.getTabPlacement())
                );
        panel.confirmDelete_.setValue(attribs.confirmDelete());
        panel.confirmDeleteAll_.setValue(attribs.confirmDeleteAll());
        panel.autoScroll_.setValue(attribs.autoScroll());
        panel.font_.setFont(attribs.getFont());
    }

    /**
     *  Represents a boolean combo box
     *
     * @author    Don Brown
     */
    static class BooleanComboBox extends JComboBox {
        /**
         *  Constructor
         *
         * @param  trueDisplayValue   Text of true
         * @param  falseDisplayValue  Text of false
         */
        BooleanComboBox(String trueDisplayValue, String falseDisplayValue) {
            super(new String[]{trueDisplayValue, falseDisplayValue});
        }

        /**
         *  Sets the value
         *
         * @param  value  The new value value
         */
        public void setValue(boolean value) {
            if (value == true) {
                this.setSelectedIndex(0);
            }
            else {
                this.setSelectedIndex(1);
            }
        }

        /**
         *  Gets the value
         *
         * @return    The value value
         */
        public boolean getValue() {
            return (this.getSelectedIndex() == 0);
        }
    }

    /**
     *  The configuration panel
     *
     * @author    Don Brown
     */
    class CfgPanel extends JPanel {

        JTextField bufferSize_;
        JTextField latency_;
        JComboBox tabPlacement_;
        BooleanComboBox confirmDelete_;
        BooleanComboBox confirmDeleteAll_;
        BooleanComboBox autoScroll_;
        BooleanComboBox wordWrap_;
        FontSelector font_;

        /**  Constructor */
        CfgPanel() {
            //contentPane.setBorder(
            //        BorderFactory.createEmptyBorder(12, 12, 11, 11)
            //        );

            JPanel configPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.ipadx = 4;

            // buffer size
            gbc.gridy = 0;
            configPanel.add(
                    new JLabel(LogViewer.getProperty("dialog.Configure.bufferSize.label")),
                    gbc
                    );
            bufferSize_ = new JTextField();
            bufferSize_.setHorizontalAlignment(JTextField.RIGHT);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(bufferSize_, gbc);
            JButton bufferSizeInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.bufferSize.title"),
                    LogViewer.getProperty("WhatIsThis.bufferSize.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(bufferSizeInfo, gbc);

            // latency
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(LogViewer.getProperty("dialog.Configure.latency.label")),
                    gbc
                    );
            latency_ = new JTextField();
            latency_.setHorizontalAlignment(JTextField.RIGHT);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(latency_, gbc);
            JButton latencyInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.latency.title"),
                    LogViewer.getProperty("WhatIsThis.latency.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(latencyInfo, gbc);

            // tab placement
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(LogViewer.getProperty("dialog.Configure.tabPlacement.label")),
                    gbc
                    );
            tabPlacement_ = new JComboBox(ALL_TAB_PLACEMENT_VALUES);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(tabPlacement_, gbc);
            JButton tabPlacementInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.tabPlacement.title"),
                    LogViewer.getProperty("WhatIsThis.tabPlacement.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(tabPlacementInfo, gbc);

            // confirm delete
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(LogViewer.getProperty("dialog.Configure.confirmDelete.label")),
                    gbc
                    );
            confirmDelete_ = new BooleanComboBox(
                    LogViewer.getProperty("dialog.Configure.confirmDelete.yes.displayValue"),
                    LogViewer.getProperty("dialog.Configure.confirmDelete.no.displayValue")
                    );
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(confirmDelete_, gbc);
            JButton confirmDeleteInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.confirmDelete.title"),
                    LogViewer.getProperty("WhatIsThis.confirmDelete.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(confirmDeleteInfo, gbc);

            // confirm delete all
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(
                    LogViewer.getProperty("dialog.Configure.confirmDeleteAll.label")
                    ),
                    gbc
                    );
            confirmDeleteAll_ = new BooleanComboBox(
                    LogViewer.getProperty("dialog.Configure.confirmDeleteAll.yes.displayValue"),
                    LogViewer.getProperty("dialog.Configure.confirmDeleteAll.no.displayValue")
                    );
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(confirmDeleteAll_, gbc);
            JButton confirmDeleteAllInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.confirmDeleteAll.title"),
                    LogViewer.getProperty("WhatIsThis.confirmDeleteAll.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(confirmDeleteAllInfo, gbc);

            // autoscroll
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(
                    LogViewer.getProperty("dialog.Configure.autoScroll.label")
                    ),
                    gbc
                    );
            autoScroll_ = new BooleanComboBox(
                    LogViewer.getProperty("dialog.Configure.autoScroll.yes.displayValue"),
                    LogViewer.getProperty("dialog.Configure.autoScroll.no.displayValue")
                    );
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(autoScroll_, gbc);
            JButton autoScrollInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.autoScroll.title"),
                    LogViewer.getProperty("WhatIsThis.autoScroll.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(autoScrollInfo, gbc);

            // font
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.ipadx = 4;
            configPanel.add(
                    new JLabel(
                    LogViewer.getProperty("dialog.Configure.font.label")
                    ),
                    gbc
                    );
            font_ = new FontSelector(attribs.getFont());
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.ipadx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            configPanel.add(font_, gbc);
            JButton fontInfo = new WhatIsThis(
                    LogViewer.getProperty("WhatIsThis.font.title"),
                    LogViewer.getProperty("WhatIsThis.font.text")
                    );
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            configPanel.add(fontInfo, gbc);

            add(configPanel, BorderLayout.CENTER);
        }

        /**  Saves the values */
        void save() {
            // Validate fields
            StringBuffer invalidFieldsMessage = new StringBuffer();
            if (!isPositiveInteger(bufferSize_.getText())) {
                invalidFieldsMessage.append(
                        LogViewer.getProperty("dialog.Configure.bufferSizeInvalid.text")
                        );
                invalidFieldsMessage.append(LogViewer.messageLineSeparator);
                invalidFieldsMessage.append(LogViewer.messageLineSeparator);
            }
            if (!isPositiveInteger(latency_.getText())) {
                invalidFieldsMessage.append(
                        LogViewer.getProperty("dialog.Configure.latencyInvalid.text")
                        );
                invalidFieldsMessage.append(LogViewer.messageLineSeparator);
                invalidFieldsMessage.append(LogViewer.messageLineSeparator);
            }

            if (invalidFieldsMessage.length() > 0) {
                JOptionPane.showMessageDialog(
                        null,
                        invalidFieldsMessage.toString(),
                        LogViewer.getProperty("dialog.Configure.invalidFieldsDialog.title"),
                        JOptionPane.ERROR_MESSAGE
                        );
            }
            else {
                attribs.setBufferSize(bufferSize_.getText());
                attribs.setLatency(latency_.getText());
                attribs.setTabPlacement(
                        ((TabPlacementValue) tabPlacement_.getSelectedItem()).value_
                        );
                attribs.setConfirmDelete(confirmDelete_.getValue());
                attribs.setConfirmDeleteAll(confirmDeleteAll_.getValue());
                attribs.setAutoScroll(autoScroll_.getValue());
                Font selectedFont;
                selectedFont = font_.getFont();

                attribs.setFont(selectedFont);
            }
        }

        /**
         *  If the value is a positive value
         *
         * @param  value  The value
         * @return        True if the value is a positive integer
         */
        private boolean isPositiveInteger(String value) {
            try {
                int intValue = Integer.parseInt(value);
                if (intValue < 1) {
                    return false;
                }
                return true;
            }
            catch (NumberFormatException nfe) {
                return false;
            }
        }
    }

    /**
     *  Represents a tab placement value
     *
     * @author    Don Brown
     */
    private class TabPlacementValue {
        /**  The value */
        public int value_;
        /**  The display value */
        public String displayValue_;

        /**
         *  Constructor
         *
         * @param  value  The value
         */
        public TabPlacementValue(int value) {
            value_ = value;
            switch (value) {
                case JTabbedPane.TOP:
                    displayValue_ = LogViewer.getProperty(
                            "dialog.Configure.tabPlacement.Top.displayValue"
                            );
                    break;
                case JTabbedPane.BOTTOM:
                    displayValue_ = LogViewer.getProperty(
                            "dialog.Configure.tabPlacement.Bottom.displayValue"
                            );
                    break;
                case JTabbedPane.LEFT:
                    displayValue_ = LogViewer.getProperty(
                            "dialog.Configure.tabPlacement.Left.displayValue"
                            );
                    break;
                case JTabbedPane.RIGHT:
                    displayValue_ = LogViewer.getProperty(
                            "dialog.Configure.tabPlacement.Right.displayValue"
                            );
                    break;
                default:
                    throw new IllegalArgumentException(
                            "int value must be one of the tab placement values from JTabbedPane"
                            );
            }
        }

        /**
         *  Shows the display value
         *
         * @return    The display value
         */
        public String toString() {
            return displayValue_;
        }

        /**
         *  Checks if equal
         *
         * @param  o  The compared object
         * @return    True if equal
         */
        public boolean equals(Object o) {
            if (o.getClass() == getClass()) {
                return value_ == ((TabPlacementValue) o).value_;
            }
            return false;
        }
    }
}

