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

import gruntspud.GruntspudContext;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class TellMeAgainPane
    extends JPanel {
    //  Private instance variables
    private GruntspudContext context;
    private String property;
    private JCheckBox tellMeAgainCheckBox;

    /**
     * Can't be instantiated, use <code>showWarnAgainDialog()</code>
     *
     */
    private TellMeAgainPane(GruntspudContext context, String property,
                            String text, String checkBoxText, Icon icon) {
        super(new BorderLayout(0, 6));

        this.property = property;
        this.context = context;

        //  The check box
        tellMeAgainCheckBox = new JCheckBox( (checkBoxText == null)
                                            ? "Tell me about this again" :
                                            checkBoxText, true);
        tellMeAgainCheckBox.setBorder(BorderFactory.createEmptyBorder(8, 4, 8,
            4));
        tellMeAgainCheckBox.setHorizontalAlignment(SwingConstants.CENTER);

        //
        JPanel p = new JPanel(new BorderLayout());
        MultilineLabel ml = new MultilineLabel(text);
        ml.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
        p.add(ml, BorderLayout.CENTER);
        p.add(tellMeAgainCheckBox, BorderLayout.SOUTH);

        //  Icon panel
        JLabel i = new JLabel(icon);
        i.setVerticalAlignment(JLabel.NORTH);
        i.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 4));

        //  Build this panel
        add(p, BorderLayout.CENTER);
        add(i, BorderLayout.WEST);

        //
    }

    private void cleanUp() {
        if (!tellMeAgainCheckBox.isSelected()) {
            context.getHost().setBooleanProperty(property, false);
        }
    }

    /**
     * Show a 'Tell me again' dialog. <code>null</code> may be passed for
     * checkBoxMessage (uses 'Tell me about this again').
     *
     * @param context context
     * @param parent parent component
     * @param checkBoxMessage message to use for the checkbox (defaults to
     * @param property
     * @param text
     * @param title dialog title
     */
    public static void showTellMeAgainDialog(GruntspudContext context,
                                             JComponent parent,
                                             String checkBoxMessage,
                                             String property,
                                             String text, String title,
                                             Icon icon) {
        OptionDialog.Option close = new OptionDialog.Option("Close", "Close",
            'c');
        OptionDialog.Option[] opt = {
            close};
        showTellMeAgainDialog(context, parent, checkBoxMessage, property, text,
                              opt, title, icon);
    }

    /**
     * Show a 'Tell me again' dialog. <code>null</code> may be passed for
     * checkBoxMessage (uses 'Tell me about this again'). Options may be
     * specified.
     *
     * @param context context
     * @param parent parent component
     * @param checkBoxMessage message to use for the checkbox (defaults to
     * @param property
     * @param text
     * @param options options
     * @return option selected option
     * @param title dialog title
     */
    public static OptionDialog.Option showTellMeAgainDialog(
        GruntspudContext context, JComponent parent, String checkBoxMessage,
        String property, String text, OptionDialog.Option[] options,
        String title, Icon icon) {
        //
        if (text == null) {
            throw new IllegalArgumentException("text argument may not be null");
        }

        if (property == null) {
            throw new IllegalArgumentException(
                "property argument may not be null");
        }

        //  If the property is 'false', then don't show the dialog
        if (!context.getHost().getBooleanProperty(property, true)) {
            return null;
        }

        //
        TellMeAgainPane t = new TellMeAgainPane(context, property, text,
                                                checkBoxMessage, icon);
        t.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        //  Show the dialog
        OptionDialog.Option opt = OptionDialog.showOptionDialog(property,
            context, parent, options, t, title, options[0], null, false,
            true);
        t.cleanUp();

        return opt;
    }
}
