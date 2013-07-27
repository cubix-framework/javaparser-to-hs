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

package gruntspud.ui.preferences;

import gruntspud.GruntspudContext;
import gruntspud.ui.JNumericTextField;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Choose a font
 *
 * @author Brett Smith
 */
public class FontChooser
    extends JPanel {
    private static FontNameListModel fontNameListModel;
    private JList fontNameList;
    private JList fontSizeList;
    private JTextField fontName;
    private Font chosenFont;
    private JCheckBox bold;
    private JCheckBox italic;
    private JNumericTextField fontSize;
    private JLabel preview;

    /**
     * Construct a <code>FontChooser</code>
     *
     * @param none
     */
    public FontChooser() {
        this(null);
    }

    /**
     * Construct a <code>FontChooser</code>
     *
     * @param font initiali font
     */
    public FontChooser(Font font) {
        super(new BorderLayout());

        //  Lazily create the font list name model
        if (fontNameListModel == null) {
            fontNameListModel = new FontNameListModel();

            //  Create the name list
        }
        fontNameList = new JList(fontNameListModel);
        fontNameList.setCellRenderer(new FontNameListCellRenderer());
        fontNameList.setVisibleRowCount(7);
        fontNameList.setAutoscrolls(true);
        fontNameList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!fontNameList.isSelectionEmpty() &&
                    !e.getValueIsAdjusting()) {
                    try {
                        fontName.setText( ( (Font) fontNameList.
                                           getSelectedValue()).getName());
                    }
                    catch (IllegalStateException iee) {
                    }

                    changeFontBasedOnState();
                }
            }
        });

        //  Create the font style selection panel
        JPanel stylePanel = new JPanel(new GridBagLayout());
        stylePanel.setBorder(BorderFactory.createTitledBorder("Font style"));

        GridBagConstraints gBC = new GridBagConstraints();
        gBC.fill = GridBagConstraints.BOTH;
        gBC.anchor = GridBagConstraints.CENTER;
        gBC.weighty = 0.0;
        gBC.insets = new Insets(2, 2, 2, 2);

        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                changeFontBasedOnState();
            }
        };

        UIUtil.jGridBagAdd(stylePanel, bold = new JCheckBox("Bold"), gBC,
                           GridBagConstraints.REMAINDER);
        bold.addActionListener(l);
        bold.setMnemonic('b');
        UIUtil.jGridBagAdd(stylePanel, italic = new JCheckBox("Italic"), gBC,
                           GridBagConstraints.REMAINDER);
        italic.setMnemonic('i');
        italic.addActionListener(l);

        //  Create the font size list
        //  @todo make this more specific to the font. not sure how yet :-)
        fontSizeList = new JList(new Integer[] {
                                 new Integer(8), new Integer(9), new Integer(10),
                                 new Integer(11), new Integer(12),
                                 new Integer(14),
                                 new Integer(16), new Integer(18),
                                 new Integer(20),
                                 new Integer(22), new Integer(24),
                                 new Integer(26),
                                 new Integer(28), new Integer(36),
                                 new Integer(48),
                                 new Integer(72)
        });
        fontSizeList.setVisibleRowCount(4);
        fontSizeList.setAutoscrolls(true);
        fontSizeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!fontNameList.isSelectionEmpty() &&
                    !e.getValueIsAdjusting()) {
                    try {
                        fontSize.setValue( ( (Integer) fontSizeList.
                                            getSelectedValue()));
                    }
                    catch (IllegalStateException iee) {
                    }

                    changeFontBasedOnState();
                }
            }
        });

        //  Create the font size selection panel
        JPanel sizePanel = new JPanel(new GridBagLayout());
        sizePanel.setBorder(BorderFactory.createTitledBorder("Font size"));

        GridBagConstraints gBC3 = new GridBagConstraints();
        gBC3.fill = GridBagConstraints.BOTH;
        gBC3.anchor = GridBagConstraints.WEST;
        gBC3.weightx = 1.0;
        gBC3.weighty = 0.0;
        gBC3.insets = new Insets(2, 2, 2, 2);
        UIUtil.jGridBagAdd(sizePanel, new JLabel("Size:"), gBC3,
                           GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(sizePanel,
                           fontSize = new JNumericTextField(new Integer(4),
            new Integer(999)),
                           gBC3, GridBagConstraints.REMAINDER);
        fontSize.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changeFontBasedOnState();
            }

            public void removeUpdate(DocumentEvent e) {
                changeFontBasedOnState();
            }

            public void changedUpdate(DocumentEvent e) {
                changeFontBasedOnState();
            }
        });
        gBC3.weighty = 1.0;
        UIUtil.jGridBagAdd(sizePanel, new JScrollPane(fontSizeList), gBC3,
                           GridBagConstraints.REMAINDER);

        //  Create the panel where selection of the font name takes place
        JPanel namePanel = new JPanel(new GridBagLayout());
        namePanel.setBorder(BorderFactory.createTitledBorder("Font name"));

        GridBagConstraints gBC2 = new GridBagConstraints();
        gBC2.fill = GridBagConstraints.BOTH;
        gBC2.anchor = GridBagConstraints.WEST;
        gBC2.weightx = 1.0;
        gBC2.weighty = 0.0;
        gBC2.insets = new Insets(2, 2, 2, 2);
        UIUtil.jGridBagAdd(namePanel, new JLabel("Name:"), gBC2,
                           GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(namePanel, fontName = new JTextField(10), gBC2,
                           GridBagConstraints.REMAINDER);
        gBC2.weighty = 1.0;
        UIUtil.jGridBagAdd(namePanel, new JScrollPane(fontNameList), gBC2,
                           GridBagConstraints.REMAINDER);

        //  Create the preview label
        preview = new JLabel("Some sample text") {
            public Dimension getMinimumSize() {
                return new Dimension(super.getPreferredSize().width, 64);
            }

            public Dimension getPreferredSize() {
                return getMinimumSize();
            }
        };
        preview.setBackground(Color.white);
        preview.setForeground(Color.black);
        preview.setOpaque(true);
        preview.setHorizontalAlignment(SwingConstants.CENTER);
        preview.setBorder(BorderFactory.createLineBorder(Color.black));

        //  Create the preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        previewPanel.add(preview, BorderLayout.CENTER);

        //  Create the right panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(stylePanel, BorderLayout.NORTH);
        rightPanel.add(sizePanel, BorderLayout.CENTER);

        //  Listen for changes in the font name and select the closest font in
        //  the list
        fontName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                findClosestFont();
            }

            public void removeUpdate(DocumentEvent e) {
                findClosestFont();
            }

            public void changedUpdate(DocumentEvent e) {
                findClosestFont();
            }
        });

        //
        add(namePanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(previewPanel, BorderLayout.SOUTH);

        //  Create the panel where the font name may be selected
        setChosenFont(font);
    }

    private void findClosestFont() {
        for (int i = 0; i < fontNameList.getModel().getSize(); i++) {
            Font f = (Font) fontNameList.getModel().getElementAt(i);

            if (f.getName().toLowerCase().startsWith(fontName.getText()
                .toLowerCase())) {
                fontNameList.setSelectedIndex(i);
                fontNameList.scrollRectToVisible(fontNameList.getCellBounds(i,
                    i));
                changeFontBasedOnState();

                break;
            }
        }
    }

    private void findClosestSize() {
        for (int i = 0; i < fontNameList.getModel().getSize(); i++) {
            Integer f = (Integer) fontSizeList.getModel().getElementAt(i);

            if (String.valueOf(f).startsWith(fontSize.getValue().toString())) {
                fontSizeList.setSelectedIndex(i);
//                fontSizeList.scrollRectToVisible(fontSizeList.getCellBounds(i,
//                    i));

				fontSizeList.scrollRectToVisible(fontSizeList.getBounds());

                break;
            }
        }
    }

    private void changeFontBasedOnState() {
        Font f = ( (Font) fontNameList.getSelectedValue());

        if (f != null) {
            int size = fontSize.getValue().intValue();
            int style = (bold.isSelected() ? Font.BOLD : 0) |
                (italic.isSelected() ? Font.ITALIC : 0);
            chosenFont = new Font(f.getName(), style, size);
            preview.setFont(chosenFont);
        }
    }

    /**
     * Set the currently chosen font
     *
     * @param font font
     */
    public void setChosenFont(Font f) {
        //  We cant have a null font, so default to the one for JLabel
        if (f == null) {
            f = UIManager.getFont("Label.font");

            //  Sort out the selections
        }
        fontName.setText(f.getName());
        findClosestFont();
        fontSize.setValue(new Integer(f.getSize()));
        bold.setSelected(f.isBold());
        italic.setSelected(f.isItalic());
        chosenFont = f;
        preview.setFont(f);
    }

    /**
     * Get the currently chosen font
     *
     * @return font font
     */
    public Font getChosenFont() {
        return chosenFont;
    }

    /**
     * Show a chooser dialog
     */
    public static Font showDialog(JComponent parent, Font initialFont,
                                  GruntspudContext context) {
        //  Create the font chooser
        final FontChooser fc = new FontChooser(initialFont);
        fc.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        OptionDialog.Option sel = new OptionDialog.Option("Select",
            "Select font", 's');
        OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
            "Cancel selection of font", 'c');
        OptionDialog.Option opt = OptionDialog.showOptionDialog("fontChooser",
            context, parent, new OptionDialog.Option[] {sel, cancel}
            , fc,
            "Choose font", sel,
            new OptionDialog.Callback() {
            public boolean canClose(OptionDialog dialog,
                                    OptionDialog.Option option) {
                return true;
            }

            public void close(OptionDialog dialog, Option option) {
                // TODO Auto-generated method stub
                
            }
        });

        if (opt != sel) {
            return null;
        }

        //  Return the chosen font - if any
        return fc.getChosenFont();
    }

    //  font name list model
    static class FontNameListModel
        extends AbstractListModel
        implements Runnable {
        private Vector fonts;

        FontNameListModel() {
            fonts = new Vector();

            Thread t = new Thread(this);
            t.start();
        }

        public void run() {
            Font[] f = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAllFonts();

            for (int i = 0; i < f.length; i++) {
                fonts.addElement(f[i]);

            }
            fireContentsChanged(this, 0, getSize() - 1);
        }

        public Object getElementAt(int i) {
            return fonts.elementAt(i);
        }

        public int getSize() {
            return fonts.size();
        }
    }

    //  Render just the font name in the list
    class FontNameListCellRenderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);
            setText( ( (Font) value).getName());

            return this;
        }
    }
}
