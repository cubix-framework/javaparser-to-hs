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

public class ConstantsEditor extends JDialog {

    private String LS = System.getProperty("line.separator");

    public ConstantsEditor(JDialog parent, File file) {
        this(parent, "Edit Constant", file);
    }

    public ConstantsEditor(final JDialog parent, String constant_title, final File constant_file) {
        super(parent, constant_title, true);
        JPanel contents = new JPanel(new LambdaLayout());

        try {
            String name = "";
            String value = "";
            String uncertainty = "";
            String units = "";
            if (constant_file.exists() && constant_file.length() > 0) {
                FunctionReader fr = new FunctionReader(constant_file);
                String desc = fr.getDescription();
                String[] split = desc.split("<br>");
                name = split[0].substring(split[0].indexOf(' ')).trim();
                value = split[1].substring(split[1].indexOf(' ')).trim();
                uncertainty = split[2].substring(split[2].indexOf(' ')).trim();
                units = split[3].substring(split[3].indexOf(' ')).trim();
            }

            final JTextField name_field = new JTextField(name);
            final JButton name_view = new JButton("View");
            final JTextField value_field = new JTextField();
            value_field.setDocument(new RegisterDocument());
            value_field.setText(value);
            final JTextField uncertainty_field = new JTextField(uncertainty);
            final JButton uncertainty_view = new JButton("View");
            final JTextField units_field = new JTextField(units);
            final JButton units_view = new JButton("View");

            ActionListener view_listener = new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    JButton btn = (JButton) ae.getSource();
                    String text = "";
                    if (btn.equals(name_view)) {
                        text = name_field.getText();
                    }
                    else if (btn.equals(uncertainty_view)) {
                        text = uncertainty_field.getText();
                    }
                    else if (btn.equals(units_view)) {
                        text = units_field.getText();
                    }
                    JOptionPane.showMessageDialog(ConstantsEditor.this, "<html>" + text, "View", JOptionPane.PLAIN_MESSAGE);
                }
            } ;

            name_view.addActionListener(view_listener);
            uncertainty_view.addActionListener(view_listener);
            units_view.addActionListener(view_listener);

            KappaLayout btn_layout = new KappaLayout();
            JPanel btn_panel = new JPanel(btn_layout);
            JButton save_btn = new JButton("Save");
            JButton cancel_btn = new JButton("Cancel");
            btn_panel.add(save_btn, "0, 0, 1, 1, E, w,3");
            btn_panel.add(cancel_btn, "1, 0, 1, 1, W, w,3");
            btn_layout.makeColumnsSameWidth(0, 1);

            contents.add(new JLabel("Name:"), "0, 0, 1, 1, 0, w, 3");
            contents.add(name_field, "1, 0, 5, 1, 0, w, 3");
            contents.add(name_view, "6, 0, 1, 1, 0, w, 3");
            contents.add(new JLabel("Value:"), "0, 1, 1, 1, 0, w, 3");
            contents.add(value_field, "1, 1, 6, 1, 0, w, 3");
            contents.add(new JLabel("Uncertainty:"), "0, 2, 1, 1, 0, w, 3");
            contents.add(uncertainty_field, "1, 2, 5, 1, 0, wh, 3");
            contents.add(uncertainty_view, "6, 2, 1, 1, 0, w, 3");
            contents.add(new JLabel("Units:"), "0, 3, 1, 1, 0, w, 3");
            contents.add(units_field, "1, 3, 5, 1, 0, wh, 3");
            contents.add(units_view, "6, 3, 1, 1, 0, w, 3");
            contents.add(btn_panel, "0, 4, 7, 1, 0,, 6");

            save_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    String name = name_field.getText();
                    if (GUIUtils.isEmptyOrBlank(name)) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: Constant name must be entered.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String value = value_field.getText();
                    if (GUIUtils.isEmptyOrBlank(value)) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: Value must be entered.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String desc = "<html>Constant: " + name + "<br>Value: " + value + "<br>Uncertainty: " + uncertainty_field.getText().trim() + "<br>Units: " + units_field.getText().trim();
                    FunctionWriter fw = new FunctionWriter(constant_file);
                    fw.setConstant(true);
                    try {
                        fw.write(name, desc, value);
                    }
                    catch(IOException e) {
                        JOptionPane.showMessageDialog(parent, "Unable to save: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    setVisible(false);
                    dispose();
                }
            }
           );
            cancel_btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                    dispose();
                }
            }
           );
            setContentPane(contents);
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
        new ConstantsEditor(new JDialog(), "test", new File("c:/Documents and Settings/danson/.calc/const7543.calc"));
    }
}
