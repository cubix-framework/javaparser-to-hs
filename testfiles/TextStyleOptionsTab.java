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

package gruntspud.style;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.ColorComboBox;
import gruntspud.ui.UIUtil;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.icons.ColorIcon;
import gruntspud.ui.icons.CompoundIcon;
import gruntspud.ui.preferences.AbstractOptionsTab;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class TextStyleOptionsTab
    extends AbstractOptionsTab implements ActionListener, ChangeListener {

    private ColorComboBox foreground;
    private ColorComboBox background;
    private GruntspudCheckBox bold, italic;
    private JList styles;
    private TextStyleModel model;
    private boolean adjusting;


    /**
     *  Constructor for the GlobalOptionsTab object
     */
    public TextStyleOptionsTab() {
        super("Styles", UIUtil.getCachedIcon(Constants.ICON_TOOL_COLOR));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);

        setTabToolTipText("Set various color / font styles used in Gruntspud.");
        setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_COLOR));
        setLayout(new BorderLayout());
        setTabMnemonic('s');
        setTabContext("UI");

        try {
			model = (TextStyleModel) context.getTextStyleModel().clone();
		} catch (CloneNotSupportedException e1) {
		}
        styles = new JList(model);
        styles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styles.setCellRenderer(new StyleListCellRenderer());
        styles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
            	Constants.UI_LOG.debug("Style selected");
            	if(!adjusting) {
            		setSettingsForStyle();
            		
            	}
            }
        });
        JScrollPane stylesScroller = new JScrollPane(styles);
        JPanel s = new JPanel(new BorderLayout());
		s.setOpaque(false);
        s.add(stylesScroller, BorderLayout.CENTER);
        s.setBorder(BorderFactory.createTitledBorder("Styles"));

        //
        JPanel e = new JPanel(new GridBagLayout());
		e.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(e, new JLabel("Foreground"), gbc, 1);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(e, foreground = new ColorComboBox(true), gbc,
						   GridBagConstraints.RELATIVE);
		foreground.addChangeListener(this);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(e, bold = new GruntspudCheckBox("Bold"), gbc,
						   GridBagConstraints.REMAINDER);
		bold.addActionListener(this);
		bold.setMnemonic('b');
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(e, new JLabel("Background"), gbc, 1);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(e, background = new ColorComboBox(true), gbc,
						   GridBagConstraints.RELATIVE);
		background.addChangeListener(this);
		gbc.weightx = 0.0;
		UIUtil.jGridBagAdd(e, italic = new GruntspudCheckBox("Italic"), gbc,
						   GridBagConstraints.REMAINDER);
		italic.addActionListener(this);
		italic.setMnemonic('i');

        add(s, BorderLayout.CENTER);
		add(e, BorderLayout.SOUTH);

        if(styles.getModel().getSize() > 0)
            styles.setSelectedIndex(0);
        setSettingsForStyle();
    }
    
    public void actionPerformed(ActionEvent evt) {
    	if(!adjusting) {
    		applySettings();
    	}
    }

    private void applySettings() {
    	Constants.UI_LOG.debug("Applying settings");
        TextStyle style = model.getStyleAt(styles.getSelectedIndex());
        style.setForeground(foreground.getColor());
        style.setBackground(background.getColor());
        style.setItalic(italic.isSelected());
        style.setBold(bold.isSelected());
        model.updateStyle(style);
    }

    private void setSettingsForStyle() {
    	adjusting = true;
        int idx = styles.getSelectedIndex();
        if(idx == -1) {
            foreground.setEnabled(false);
            background.setEnabled(false);
            italic.setEnabled(false);
            bold.setEnabled(false);
            foreground.setColor(null);
            background.setColor(null);
        }
        else {
            TextStyle style = model.getStyleAt(idx);
            foreground.setEnabled(true);
            background.setEnabled(true);
            italic.setEnabled(true);
            bold.setEnabled(true);
            foreground.setColor(style.getForeground());
            background.setColor(style.getBackground());
            italic.setSelected(style.isItalic());
            bold.setSelected(style.isBold());
        }
		adjusting = false;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateTab() {
        return true;
    }

    /**
     *  Description of the Method
     */
    public void tabSelected() {
    }

    /**
     *  Description of the Method
     */
    public void applyTab() {
    	Constants.UI_LOG.debug("Applying new styles");
    	Iterator j = getContext().getTextStyleModel().styles();
    	for(Iterator i = model.styles(); i.hasNext(); ) {
    		TextStyle s1 = (TextStyle)j.next();
			TextStyle s2 = (TextStyle)i.next();
			s1.setBackground(s2.getBackground());
			s1.setForeground(s2.getForeground());
			s1.setBold(s2.isBold());
			s1.setItalic(s2.isItalic());
			getContext().getTextStyleModel().updateStyle(s1);
    	}
    }

    class StyleListCellRenderer extends DefaultListCellRenderer {

        private Color fg;
        private ColorIcon  backgroundIcon, foregroundIcon;
        private CompoundIcon icon;

        public StyleListCellRenderer() {
            super();
            backgroundIcon = new ColorIcon(Color.black, new Dimension(12, 12), styles.getForeground());
            foregroundIcon = new ColorIcon(Color.black, new Dimension(12, 12), styles.getForeground());
            icon = new CompoundIcon(backgroundIcon, foregroundIcon);
            icon.setGap(4);
            icon.setMargin(new Insets(1, 4, 1, 4));
            setVerticalTextPosition(JLabel.CENTER);
            setVerticalAlignment(JLabel.CENTER);
        }

        public Component getListCellRendererComponent(
            JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            TextStyle style = (TextStyle)value;
            Color bgi = style.getBackground();
            Color fgi = style.getForeground();
//            if(!isSelected)
//                setForeground(fgi == null ? styles.getForeground() : fgi);
            backgroundIcon.setColor(bgi);
            foregroundIcon.setColor(fgi);
            setIcon(icon);
            setFont(getFont().deriveFont( ( style.isBold() ? Font.BOLD : 0 ) + ( style.isItalic() ? Font.ITALIC : 0 ) ));
            return this;
        }
    }

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if(!adjusting) {
			applySettings();
		}		
	}
}
