/*
 * JCompilerPane.java - plugin options pane for JCompiler
 * (c) 1999, 2000 Kevin A. Burton and Aziz Sharif
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

package jcompiler;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;


/**
 * This is the option pane that jEdit displays for Plugin Options. 
 */
public class JCompilerPane
         extends AbstractOptionPane
         implements ActionListener, ChangeListener
{
    private JRadioButton saveOnCompile;
    private JRadioButton saveNotOnCompile;
    private JRadioButton saveAllOnCompile;
    private JRadioButton saveOnPkgCompile;
    private JRadioButton saveNotOnPkgCompile;
    private JRadioButton saveAllOnPkgCompile;
    private JCheckBox specifyOutputDirectory;
    private JCheckBox showDeprecation;
    private JCheckBox useJavaCP;
    private JCheckBox addPkg2CP;
    private JTextArea newCP;
    private JLabel cpLabel;
    private JTextField outputDirectory;
    private JTextField regexp;
    private JTextField regexpFilename;
    private JTextField regexpLineNo;
    private JTextField regexpMessage;
    private JButton pickDirectory;


    public JCompilerPane() {
        super("jcompiler");
    }


    public void _init() {
        // ========== General options ==========
        addSeparator("options.jcompiler.sep.general");
        
        saveOnCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.current"));
        saveAllOnCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.all"));
        saveNotOnCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.not"));
        saveOnPkgCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.current"));
        saveAllOnPkgCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.all"));
        saveNotOnPkgCompile = new JRadioButton(jEdit.getProperty(
            "options.jcompiler.autosave.not"));
            
        ButtonGroup group1 = new ButtonGroup();
        group1.add(saveOnCompile);
        group1.add(saveAllOnCompile);
        group1.add(saveNotOnCompile);
        
        ButtonGroup group2 = new ButtonGroup();
        group2.add(saveOnPkgCompile);
        group2.add(saveAllOnPkgCompile);
        group2.add(saveNotOnPkgCompile);
        
        addComponent(new JLabel(jEdit.getProperty(
            "options.jcompiler.autosave.compile")));
        addComponent(saveOnCompile);
        addComponent(saveAllOnCompile);
        addComponent(saveNotOnCompile);
        addComponent(new JLabel(jEdit.getProperty(
            "options.jcompiler.autosave.compilepkg")));
        addComponent(saveOnPkgCompile);
        addComponent(saveAllOnPkgCompile);
        addComponent(saveNotOnPkgCompile);
                
        // ========== Compiler options ==========
        addSeparator("options.jcompiler.sep.compiler");

        showDeprecation = new JCheckBox(jEdit.getProperty(
            "options.jcompiler.showdeprecated"));
        showDeprecation.setSelected(jEdit.getBooleanProperty(
            "jcompiler.showdeprecated", true));
        addComponent(showDeprecation);

        specifyOutputDirectory = new JCheckBox(jEdit.getProperty(
            "options.jcompiler.specifyoutputdirectory"));
        specifyOutputDirectory.setSelected(jEdit.getBooleanProperty(
            "jcompiler.specifyoutputdirectory", false));
        specifyOutputDirectory.addChangeListener(this);
        addComponent(specifyOutputDirectory);

        outputDirectory = new JTextField();
        String output = jEdit.getProperty("jcompiler.outputdirectory");
        outputDirectory.setText(output == null ? "" : output);
        pickDirectory = new JButton(GUIUtilities.loadIcon("Open24.gif"));
        pickDirectory.addActionListener(this);
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add(outputDirectory, BorderLayout.CENTER);
        outputPanel.add(pickDirectory, BorderLayout.EAST);
        addComponent(jEdit.getProperty("options.jcompiler.outputDirectory"),
            outputPanel);
            
        // ========== Classpath options ==========
        addSeparator("options.jcompiler.sep.classpath");
        
        addPkg2CP = new JCheckBox(jEdit.getProperty(
            "options.jcompiler.addpkg2cp"));
        addPkg2CP.setSelected(jEdit.getBooleanProperty(
            "jcompiler.addpkg2cp", true));
        addComponent(addPkg2CP);

        useJavaCP = new JCheckBox(jEdit.getProperty(
            "options.jcompiler.usejavacp"));
        useJavaCP.setSelected(jEdit.getBooleanProperty(
            "jcompiler.usejavacp", true));
        useJavaCP.addActionListener(this);
        addComponent(useJavaCP);

        cpLabel = new JLabel(); // set text later in enableVisibility()
        addComponent(cpLabel);
        
        newCP = new JTextArea(2, 40);
        newCP.setLineWrap(true);
        newCP.setMinimumSize(new Dimension(300, 20));
        addComponent(new JScrollPane(newCP));

        // ========== Error parsing options ==========
        addSeparator("options.jcompiler.sep.errorparsing");
        
        regexp = new JTextField(jEdit.getProperty("jcompiler.regexp"));
        addComponent(jEdit.getProperty("options.jcompiler.regexp"), regexp);
        
        regexpFilename = new JTextField(jEdit.getProperty(
            "jcompiler.regexp.filename"));
        addComponent(jEdit.getProperty("options.jcompiler.regexp.filename"),
            regexpFilename);
        
        regexpLineNo = new JTextField(jEdit.getProperty(
            "jcompiler.regexp.lineno"));
        addComponent(jEdit.getProperty("options.jcompiler.regexp.lineno"),
            regexpLineNo);
        
        regexpMessage = new JTextField(jEdit.getProperty(
            "jcompiler.regexp.message"));
        addComponent(jEdit.getProperty("options.jcompiler.regexp.message"),
            regexpMessage);

        // ========== misc setup ==========
        enableAutosave();
        enableOutputDirectory(jEdit.getBooleanProperty(
            "jcompiler.specifyoutputdirectory", false));
        enableVisibility();
    }


    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == pickDirectory) {
            // the "choose dir" button was pressed:
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            //chooser.setTitle("Select output directory");
            int retVal = chooser.showOpenDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    try {
                        String dirName = file.getCanonicalPath();
                        outputDirectory.setText(dirName);
                    } 
                    catch(IOException donothing) {
                        // shouldn't happen
                    }
                }
            }
        } else {
            enableVisibility();
        }
    }


    public void stateChanged(ChangeEvent e) {
        if (specifyOutputDirectory.isSelected()) {
            enableOutputDirectory(true);
        } else {
            enableOutputDirectory(false);
        }
    }


    public void _save() {
        jEdit.setBooleanProperty("jcompiler.javacompile.autosave", saveOnCompile.isSelected());
        jEdit.setBooleanProperty("jcompiler.javacompile.autosaveall", saveAllOnCompile.isSelected());
        jEdit.setBooleanProperty("jcompiler.javapkgcompile.autosave", saveOnPkgCompile.isSelected());
        jEdit.setBooleanProperty("jcompiler.javapkgcompile.autosaveall", saveAllOnPkgCompile.isSelected());
        jEdit.setBooleanProperty("jcompiler.showdeprecated", showDeprecation.isSelected());
        jEdit.setBooleanProperty("jcompiler.specifyoutputdirectory", specifyOutputDirectory.isSelected());
        jEdit.setBooleanProperty("jcompiler.usejavacp", useJavaCP.isSelected());
        jEdit.setBooleanProperty("jcompiler.addpkg2cp", addPkg2CP.isSelected());
        
        jEdit.setProperty("jcompiler.outputdirectory", outputDirectory.getText());
        jEdit.setProperty("jcompiler.regexp", regexp.getText());
        jEdit.setProperty("jcompiler.regexp.filename", regexpFilename.getText());
        jEdit.setProperty("jcompiler.regexp.lineno", regexpLineNo.getText());
        jEdit.setProperty("jcompiler.regexp.message", regexpMessage.getText());

        if (!useJavaCP.isSelected()) {
            jEdit.setProperty("jcompiler.classpath", newCP.getText());
        }
        
        if (saveOnCompile.isSelected()) {
            jEdit.setProperty("jcompiler.javacompile.autosave", "current");
        } else if (saveAllOnCompile.isSelected()) {
            jEdit.setProperty("jcompiler.javacompile.autosave", "all");
        } else {
            jEdit.setProperty("jcompiler.javacompile.autosave", "no");
        }
        
        if (saveOnPkgCompile.isSelected()) {
            jEdit.setProperty("jcompiler.javapkgcompile.autosave", "current");
        } else if (saveAllOnPkgCompile.isSelected()) {
            jEdit.setProperty("jcompiler.javapkgcompile.autosave", "all");
        } else {
            jEdit.setProperty("jcompiler.javapkgcompile.autosave", "no");
        }
    }


    private void enableOutputDirectory(boolean enable) {
        outputDirectory.setEnabled(enable);
        pickDirectory.setEnabled(enable);
    }


    private void enableAutosave() {
        String s = jEdit.getProperty("jcompiler.javacompile.autosave", "no");
        if ("current".equals(s)) {
            saveOnCompile.setSelected(true);
        } else if ("all".equals(s)) {
            saveAllOnCompile.setSelected(true);
        } else {
            saveNotOnCompile.setSelected(true);
        }
        s = jEdit.getProperty("jcompiler.javapkgcompile.autosave", "no");
        if ("current".equals(s)) {
            saveOnPkgCompile.setSelected(true);
        } else if ("all".equals(s)) {
            saveAllOnPkgCompile.setSelected(true);
        } else {
            saveNotOnPkgCompile.setSelected(true);
        }
    }


    private void enableVisibility() {
        if (useJavaCP.isSelected()) {
            cpLabel.setText(jEdit.getProperty(
                "options.jcompiler.usejavacp.true"));
            newCP.setText(System.getProperty("java.class.path"));
            newCP.setEnabled(false);
        } else {
            cpLabel.setText(jEdit.getProperty(
                "options.jcompiler.usejavacp.false"));
            newCP.setText(jEdit.getProperty("jcompiler.classpath"));
            newCP.setEnabled(true);
        }
    }
}

