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

import gruntspud.Constants;
import gruntspud.GruntspudContext;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Description of the Class
 *
 * @author magicthize
 */
public class OptionDialog
    extends JDialog
    implements ActionListener {
    
    private final static HashMap windowMap = new HashMap();
    
    //
    private Option selectedOption;
    private Callback callback;
    private JButton defaultButton;
    private GruntspudContext context;
    private String name;
    

    /**
     * Constructor for the OptionDialog object
     *
     * @param parent Description of the Parameter
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param title Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     * @param context DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param icon DOCUMENT ME!
     */
    public OptionDialog(JDialog parent, Option[] options, Object message,
                        String title, Option defaultOption, Callback callback,
                        GruntspudContext context, boolean modal, Icon icon, String name) {
        super(parent, title, modal);
        init(options, message, defaultOption, callback, context, icon, name);
    }

    /**
     * Constructor for the OptionDialog object
     *
     * @param parent Description of the Parameter
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param title Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     * @param context DOCUMENT ME!
     * @param modal DOCUMENT ME!
     * @param icon DOCUMENT ME!
     */
    public OptionDialog(JFrame parent, Option[] options, Object message,
                        String title, Option defaultOption, Callback callback,
                        GruntspudContext context, boolean modal, Icon icon, String name) {
        super(parent, title, modal);
        init(options, message, defaultOption, callback, context, icon, name);
    }

    /**
     * Description of the Method
     *
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     * @param context DOCUMENT ME!
     * @param icon DOCUMENT ME!
     */
    private void init(Option[] options, Object message, Option defaultOption,
                      Callback callback, GruntspudContext context, Icon icon, String name) {
        //
        this.callback = callback;
        this.context = context;
        this.name = name;
        
        if(!isModal() && name != null) {
            if(windowMap.containsKey(name)) {
                Constants.UI_LOG.debug("Non modal OptionDialog " + name + " already exists");
                throw new OptionDialogOpenException("Non modal OptionDialog already open.", (OptionDialog)windowMap.get(name));
            }
            windowMap.put(name, this);
            setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }

        JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 2));
        b.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        for (int i = 0; i < options.length; i++) {
            JButton button = new JButton(options[i].text);

            if (options[i] == defaultOption) {
                button.setDefaultCapable(options[i] == defaultOption);
                defaultButton = button;
            }

            button.setMnemonic(options[i].mnemonic);
            button.setToolTipText(options[i].tooltip);
            button.putClientProperty("option", options[i]);
            button.addActionListener(this);
            b.add(button);
        }

        JPanel x = new JPanel(new BorderLayout());
        x.add(new GruntspudLogo(context), BorderLayout.WEST);
        x.add(b, BorderLayout.CENTER);

        //
        JPanel s = new JPanel(new BorderLayout());
        s.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        s.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.NORTH);
        s.add(x, BorderLayout.SOUTH);

        //
        JPanel z = new JPanel(new BorderLayout());
        z.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        //
        if (message instanceof JComponent) {
            z.add( (JComponent) message, BorderLayout.CENTER);
        }
        else {
            z.add(new MultilineLabel(String.valueOf(message)),
                  BorderLayout.CENTER);

            //  Icon panel
        }
        JLabel i = null;

        if (icon != null) {
            i = new JLabel(icon);
            i.setVerticalAlignment(JLabel.NORTH);
            i.setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));
        }
        

        //  Build this panel
        JPanel y = new JPanel(new BorderLayout());
        y.setLayout(new BorderLayout());
        y.add(z, BorderLayout.CENTER);

        if (i != null) {
            y.add(i, BorderLayout.WEST);

        }
        y.add(s, BorderLayout.SOUTH);

        //
        getContentPane().setLayout(new GridLayout(1,1));
        getContentPane().add(y);
        
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                closeOptionsDialog();
            }            
        });

        //
        //pack();
    }
    
    private boolean closeOptionsDialog() {
        if(OptionDialog.this.name != null) {
            windowMap.remove(OptionDialog.this.name);
        }
        if ( (callback == null) || callback.canClose(this, selectedOption)) {
            if(callback != null) {
                callback.close(this, selectedOption);
            }
            context.getHost().saveGeometry(this,
                                           Constants.OPTION_DIALOG_GEOMETRY_PREFIX +
                                           name);
            return true;
        }
        return false;        
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JButton getDefaultButton() {
        return defaultButton;
    }

    /**
     * Gets the selectedOption attribute of the OptionDialog object
     *
     * @return The selectedOption value
     */
    private Option getSelectedOption() {
        return selectedOption;
    }

    /**
     * Description of the Method
     *
     * @param evt Description of the Parameter
     */
    public void actionPerformed(ActionEvent evt) {
        selectedOption = (Option) ( (JButton) evt.getSource()).
            getClientProperty(
            "option");
        if(closeOptionsDialog()) {
            setVisible(false);
        }
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param context Description of the Parameter
     * @param parent Description of the Parameter
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param title Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     *
     * @return Description of the Return Value
     */
    public static Option showOptionDialog(String name,
                                          GruntspudContext context,
                                          Component parent, Option[] options,
                                          Object message, String title,
                                          Option defaultOption,
                                          Callback callback) {
        return showOptionDialog(name, context, parent, options, message, title,
                                defaultOption, callback, true, false);
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param context Description of the Parameter
     * @param parent Description of the Parameter
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param title Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     * @param resizable DOCUMENT ME!
     * @param pack DOCUMENT ME!
     *
     * @return Description of the Return Value
     */
    public static Option showOptionDialog(String name,
                                          GruntspudContext context,
                                          Component parent, Option[] options,
                                          Object message, String title,
                                          Option defaultOption,
                                          Callback callback,
                                          boolean resizable, boolean pack) {
        return showOptionDialog(name, context, parent, options, message, title,
                                defaultOption, callback, resizable, pack, null);
    }

    /**
     * Description of the Method
     *
     * @param name Description of the Parameter
     * @param context Description of the Parameter
     * @param parent Description of the Parameter
     * @param options Description of the Parameter
     * @param message Description of the Parameter
     * @param title Description of the Parameter
     * @param defaultOption Description of the Parameter
     * @param callback Description of the Parameter
     * @param resizable DOCUMENT ME!
     * @param pack DOCUMENT ME!
     * @param initialSize DOCUMENT ME!
     *
     * @return Description of the Return Value
     */
    public static Option showOptionDialog(String name,
                                          GruntspudContext context,
                                          Component parent, Option[] options,
                                          Object message, String title,
                                          Option defaultOption,
                                          Callback callback,
                                          boolean resizable, boolean pack,
                                          Dimension initialSize) {
        return showOptionDialog(name, context, parent, options, message, title,
                                defaultOption, callback, resizable, pack,
                                initialSize, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param name DOCUMENT ME!
     * @param context DOCUMENT ME!
     * @param parent DOCUMENT ME!
     * @param options DOCUMENT ME!
     * @param message DOCUMENT ME!
     * @param title DOCUMENT ME!
     * @param defaultOption DOCUMENT ME!
     * @param callback DOCUMENT ME!
     * @param resizable DOCUMENT ME!
     * @param pack DOCUMENT ME!
     * @param initialSize DOCUMENT ME!
     * @param icon DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Option showOptionDialog(String name,
                                          GruntspudContext context,
                                          Component parent, Option[] options,
                                          Object message, String title,
                                          Option defaultOption,
                                          Callback callback,
                                          boolean resizable, boolean pack,
                                          Dimension initialSize, Icon icon) {
        return showOptionDialog(name, context, parent, options, message, title, defaultOption, callback, resizable, pack, initialSize, icon, true);
        
    }

        public static Option showOptionDialog(String name,
                                              GruntspudContext context,
                                              Component parent, Option[] options,
                                              Object message, String title,
                                              Option defaultOption,
                                              Callback callback,
                                              boolean resizable, boolean pack,
                                              Dimension initialSize, Icon icon, boolean modal) {
        //
        OptionDialog dialog = null;
        Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
            parent);

        try {
	        if (w instanceof JFrame) {
	            dialog = new OptionDialog( (JFrame) w, options, message, title,
	                                      defaultOption, callback, context, modal,
	                                      icon, name);
	        }
	        else if (w instanceof JDialog) {
	            dialog = new OptionDialog( (JDialog) w, options, message, title,
	                                      defaultOption, callback, context, modal,
	                                      icon, name);
	        }
	        else {
	            dialog = new OptionDialog( (JFrame)null, options, message, title,
	                                      defaultOption, callback, context, modal,
	                                      icon, name);
	
	        }
	        if (!context.getHost().isGeometryStored(Constants.
	                                                OPTION_DIALOG_GEOMETRY_PREFIX +
	                                                name)) {
	            if (initialSize != null) {
	                dialog.setSize(initialSize);
	            }
	            else {
	                dialog.pack();
	
	            }
	            UIUtil.positionComponent(SwingConstants.CENTER, dialog);
	        }
	        else {
	            context.getHost().loadGeometry(dialog,
	                                           Constants.
	                                           OPTION_DIALOG_GEOMETRY_PREFIX + name);
	
	            if (pack) {
	                dialog.pack();
	            }
	        }
	
	        if (dialog.getDefaultButton() != null) {
	            dialog.getRootPane().setDefaultButton(dialog.getDefaultButton());
	
	        }
	        dialog.setResizable(resizable);
	        dialog.setVisible(true);
	        if(modal) {
	
	        return dialog.getSelectedOption();
	        }
        }
        catch(OptionDialogOpenException odeo) {
            Constants.UI_LOG.debug(odeo);
            Toolkit.getDefaultToolkit().beep();
            odeo.getDialog().toFront();
        }
        return null;
    }
        
    public class OptionDialogOpenException extends Error {
        OptionDialog dialog;
        
        public OptionDialogOpenException(String message, OptionDialog dialog) {
            super(message);
            this.dialog = dialog;
        }
        
        public OptionDialog getDialog() {
            return dialog;
        }
    }

    public interface Callback {
        public boolean canClose(OptionDialog dialog, Option option);
        public void close(OptionDialog dialog, Option option);
    }

    public static class Option {
        private String text;
        private String tooltip;
        private int mnemonic;

        /**
         * Constructor for the Option object
         *
         * @param text Description of the Parameter
         * @param toolTip Description of the Parameter
         * @param mnemonic Description of the Parameter
         */
        public Option(String text, String tooltip, int mnemonic) {
            this.text = text;
            this.tooltip = tooltip;
            this.mnemonic = mnemonic;
        }

        public String getText() {
            return text;
        }
    }
}
