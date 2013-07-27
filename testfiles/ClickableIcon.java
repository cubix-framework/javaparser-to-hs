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
package org.frapuccino.iconpanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.UIManager;

/**
 * Component for the IconPanel.
 *
 * @author fdietz
 * @author redsolo
 */
public class ClickableIcon extends JComponent  {

    private boolean selected;

    private int panelIndex;

    private Color selectionForeground;

    private Color selectionBackground;

    private Color foreground;

    private Color background;

    private JLabel icon;

    private JLabel label;
    private String labelText;

    private String iconTooltip;

    /**
     * Creates a new ClickableIcon.
     * @param factory the factory to use when creating new labels.
     * @param image image to display.
     * @param text text to display under the image.
     * @param index the index in the icon panel.
     */
    public ClickableIcon(OneSizeLabelFactory factory, Icon image, String text, int index) {
        selectionForeground = UIManager.getColor("List.selectionForeground");
        selectionBackground = UIManager.getColor("List.selectionBackground");
        foreground = UIManager.getColor("List.foreground");
        background = UIManager.getColor("List.background");

        setLayout(new BorderLayout());

        labelText = text;
        label = factory.createLabel(labelText);
        label.setOpaque(true);
        label.setBackground(background);
        label.setForeground(foreground);
        label.setHorizontalAlignment(JLabel.CENTER);

        icon = new JLabel(image);
        icon.setOpaque(true);
        icon.setBackground(background);
        icon.setForeground(foreground);

        add(icon, BorderLayout.CENTER);
        add(label, BorderLayout.SOUTH);

        selected = false;

        panelIndex = index;
    }

    /**
     * Set if this component is selected or not.
     * @param set if the component is selected or not.
     */
    public void setSelection(boolean set) {
        selected = set;

        if (set) {
            label.setForeground(selectionForeground);
            label.setBackground(selectionBackground);
        } else {
            label.setBackground(background);
            label.setForeground(foreground);
        }
    }

    /**
     * Returns if this component is selected or not.
     * @return if this component is selected or not.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Returns the index/position for this component in the panel.
     * @return the index/position for this component in the panel.
     */
    public int getIndex() {
        return panelIndex;
    }
    
    public void setIndex(int index) {
    	panelIndex = index;
    }

    /**
     * @return returns the tooltip.
     */
    public String getToolTip() {
        return iconTooltip;
    }

    /**
     * @param tooltip The tooltip for this icon.
     */
    public void setToolTip(String tooltip) {
        iconTooltip = tooltip;
    }

    /**
     * Returns the text on the label.
     * @return the text on the label.
     */
    public String getText() {
        return labelText;
    }
}
