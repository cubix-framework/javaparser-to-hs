/*
This file, unless otherwise noted, is wholly the work of Dale Anson,
danson@grafidog.com. The complete contents of this file is hereby 
released into the public domain, with no rights reserved. For questions, 
concerns, or comments, please email the author.
*/

package ise.calculator;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class FunctionEditor extends JDialog {

    private String LS = System.getProperty("line.separator");

    public FunctionEditor(JDialog parent, File file) {
        this(parent, "Edit Function", file);
    }

    public FunctionEditor(final JDialog parent, String title, final File function_file) {
        super(parent, title, true);
        JPanel contents = new JPanel(new LambdaLayout());

        try {
            String name = "";
            String desc = "";
            String function = "";

            if (function_file.exists() && function_file.length() > 0) {
                FunctionReader fr = new FunctionReader(function_file);
                name = fr.getName();
                desc = fr.getDescription();
                function = fr.getFunction();
            }

            final JTextField name_field = new JTextField(name);
            final JButton name_view = new JButton("View");
            name_view.setToolTipText("Shows how the function name will look in the menu.");
            final JTextField desc_field = new JTextField(desc);
            final JButton desc_view = new JButton("View");
            desc_view.setToolTipText("Shows how the function description will look in the tool tip.");
            final JTextArea ta = new JTextArea(function, 10, 25);
            ActionListener view_listener = new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JButton btn = (JButton) ae.getSource();
                    String text = "";
                    if (btn.equals(name_view)) {
                        text = name_field.getText();
                    }
                    else if (btn.equals(desc_view)) {
                        text = desc_field.getText();
                    }
                    JOptionPane.showMessageDialog(FunctionEditor.this, text, "View", JOptionPane.PLAIN_MESSAGE);
                }
            };

            name_view.addActionListener(view_listener);
            desc_view.addActionListener(view_listener);

            KappaLayout btn_layout = new KappaLayout();
            JPanel btn_panel = new JPanel(btn_layout);
            JButton save_btn = new JButton("Save");
            JButton cancel_btn = new JButton("Cancel");
            btn_panel.add(save_btn, "0, 0, 1, 1, E, w,3");
            btn_panel.add(cancel_btn, "1, 0, 1, 1, W, w,3");
            btn_layout.makeColumnsSameWidth(0, 1);

            contents.add(new JLabel("Function Name:"), "0, 0, 1, 1, 0, w, 3");
            contents.add(name_field, "1, 0, 5, 1, 0, w, 3");
            contents.add(name_view, "6, 0, 1, 1, 0, w, 3");
            contents.add(new JLabel("Description:"), "0, 1, 1, 1, 0, w, 3");
            contents.add(desc_field, "1, 1, 5, 1, 0, w, 3");
            contents.add(desc_view, "6, 1, 1, 1, 0, w, 3");
            contents.add(new JLabel("Function:"), "0, 2, 7, 1, 0, w, 3");
            contents.add(new JScrollPane(ta), "0, 3, 7, 1, 0, wh, 3");
            contents.add(btn_panel, "2, 4, 2, 1, E, w, 3");

            save_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String name = name_field.getText();
                    if (GUIUtils.isEmptyOrBlank(name)) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: Function name must be entered.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String desc = desc_field.getText();
                    if (GUIUtils.isEmptyOrBlank(desc)) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: Function description must be entered.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String function = ta.getText();
                    if (GUIUtils.isEmptyOrBlank(function)) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: Function steps must be entered.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    FunctionWriter fw = new FunctionWriter(function_file);
                    try {
                        fw.write(name, desc, function);
                    }
                    catch(IOException e) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    setVisible(false);
                    dispose();
                }
            });
            
            cancel_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                    dispose();
                }
            });
            
            setContentPane(contents);
            setSize(400, 400);
            pack();
            GUIUtils.center(parent, this);
            setVisible(true);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(parent, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new FunctionEditor(new JDialog(), "test", new File("c:/Documents and Settings/danson/.calc/calc_12058.calc"));
    }
}
