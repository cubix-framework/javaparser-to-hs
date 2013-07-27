/*
 * LaTeXOptionPane.java - General options.
 *
 * Copyright (C) 2002 Anthony Roy
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package uk.co.antroy.latextools.options;


//{{{ Imports
import java.awt.GridBagConstraints;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;


//}}}
public class LaTeXOptionPane
    extends AbstractOptionPane {

    //~ Instance/static variables .............................................

    private JTextField compileCommandField;
    private JTextField compileExtField;
    private JTextField compileShowErrorsSwitch;
    private JTextField bibtexCommandField;
    private JTextField viewerCommandField;
    private JTextField viewerExtField;
    private JTextField classpathField;
    private JCheckBox compileDetach;
    private JCheckBox cStyleErrors;
    private JCheckBox bibtexDetach;
    private JCheckBox viewerDetach;

    //~ Constructors ..........................................................

    public LaTeXOptionPane() {
        super("general");
    }

    //~ Methods ...............................................................

    protected void _init() {
        initGeneral();
    }

    protected void _save() {
        saveGeneral();
    }

    private void initGeneral() {
	    addSeparator("options.latextools.general.compilation");
        
        compileCommandField = new JTextField(30);
        compileCommandField.setText(jEdit.getProperty("latex.compile.command"));

//        JPanel p1 = new JPanel();
        JLabel compileLabel = new JLabel("Default Compile Command");
        addComponent(compileLabel, compileCommandField);
        
        
        compileExtField = new JTextField(jEdit.getProperty("latex.compile.ext"));
        JLabel compileExtLabel = new JLabel("Default Compile Extension");
        addComponent(compileExtLabel, compileExtField);
        
        compileDetach = new JCheckBox("Detach Compilation Process?");
        compileDetach.getModel().setSelected(jEdit.getBooleanProperty(
                                                         "latex.compile.detach"));
        addComponent(compileDetach);
        
        cStyleErrors = new JCheckBox(jEdit.getProperty("options.latextools.general.errorlist"));
        
        cStyleErrors.getModel().setSelected(jEdit.getBooleanProperty(
                                                        "latex.compile.parse-errors"));
        addComponent(cStyleErrors);
        
        compileShowErrorsSwitch = new JTextField(30);
        compileShowErrorsSwitch.setText(jEdit.getProperty("latex.compile.c-errors"));
        String showErrTooltip = jEdit.getProperty("options.latextools.general.compilation.errortooltip");
        compileShowErrorsSwitch.setToolTipText(showErrTooltip);
        
        JLabel errorStyleLabel = new JLabel("Latex error style switch");

        addComponent(errorStyleLabel, compileShowErrorsSwitch);

        bibtexCommandField = new JTextField(jEdit.getProperty("latex.bibtex.command"));
        JLabel dfbclabel = new JLabel("Default BibTeX Command");
        addComponent(dfbclabel, bibtexCommandField);
        
        bibtexDetach = new JCheckBox("Detach BibTeX Process?");
        bibtexDetach.getModel().setSelected(jEdit.getBooleanProperty(
                                                        "latex.bibtex.detach"));
        addComponent(bibtexDetach);
        
        addSeparator("options.latextools.general.viewer");
        
        viewerCommandField = new JTextField(30);
        viewerCommandField.setText(jEdit.getProperty(
                                               "latex.viewoutput.command"));

        JLabel dfc = new JLabel("Default Viewer Command");
        addComponent(dfc, viewerCommandField);
        
        viewerExtField = new JTextField(jEdit.getProperty("latex.viewoutput.ext"));
        JLabel dfe = new JLabel("Default Viewer Extension");
        addComponent(dfe, viewerExtField );
        
        viewerDetach = new JCheckBox("Detach Viewer Process?");
        viewerDetach.getModel().setSelected(jEdit.getBooleanProperty(
                                                        "latex.viewoutput.detach"));
        
        
        addComponent(viewerDetach);
        addSeparator("options.latextools.general.classpath");
        
        classpathField = new JTextField( jEdit.getProperty("latex.classpath.dirs") , 40);
        
        classpathField.setToolTipText(jEdit.getProperty("options.latextools.general.classpath.tooltip"));
        addComponent(classpathField, GridBagConstraints.HORIZONTAL);
    }

    private void saveGeneral() {
        jEdit.setProperty("latex.compile.command", 
                          compileCommandField.getText());
        jEdit.setProperty("latex.compile.ext", compileExtField.getText());
        jEdit.setBooleanProperty("latex.compile.detach", 
                                 compileDetach.getModel().isSelected());
        jEdit.setProperty("latex.bibtex.command", bibtexCommandField.getText());
        jEdit.setBooleanProperty("latex.bibtex.detach", 
                                 bibtexDetach.getModel().isSelected());
        jEdit.setBooleanProperty("latex.compile.parse-errors", 
                                 cStyleErrors.getModel().isSelected());
        jEdit.setProperty("latex.viewoutput.command", 
                          viewerCommandField.getText());
        jEdit.setProperty("latex.viewoutput.ext", viewerExtField.getText());
        jEdit.setBooleanProperty("latex.viewoutput.detach", 
                                 viewerDetach.getModel().isSelected());
        jEdit.setProperty("latex.classpath.dirs", classpathField.getText());
        jEdit.setProperty("latex.compile.c-errors", compileShowErrorsSwitch.getText());
    }
}
