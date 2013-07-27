/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package gruntspud.ui;

import gruntspud.CVSCommandHandler;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.ReportingGruntspudAction;
import gruntspud.event.GruntspudCVSListener;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class OutputOptionWrapperPanel extends JPanel implements ActionListener {
  private JCheckBox outputToConsole, outputToReportDialog;

  private DefaultGruntspudAction action;

  private String key;

  private GruntspudContext context;

  private HashMap otherListeners;

  public OutputOptionWrapperPanel(GruntspudContext context,
      JComponent component, DefaultGruntspudAction action, String name) {
    super(new BorderLayout());
    add(component, BorderLayout.CENTER);
    this.action = action;
    this.context = context;
    //
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p.setBorder(BorderFactory.createTitledBorder("Output options"));
    p.add(new JLabel("Output to: "));
    key = Constants.OPTION_DIALOG_OUTPUT_OPTIONS_PREFIX + name;
    if (action instanceof ReportingGruntspudAction) {
      outputToReportDialog = new JCheckBox("Report");
      outputToReportDialog.setSelected(context.getHost().getBooleanProperty(
          key + "." + Constants.OPTION_DIALOG_OUTPUT_OPTIONS_REPORT_DIALOG,
          true));
      outputToReportDialog.addActionListener(this);
      p.add(outputToReportDialog);
    }
    outputToConsole = new JCheckBox("Console");
    outputToConsole.addActionListener(this);
    outputToConsole.setSelected(context.getHost().getBooleanProperty(
        key + "." + Constants.OPTION_DIALOG_OUTPUT_OPTIONS_CONSOLE, true));
    p.add(outputToConsole);
    otherListeners = new HashMap();
    EventListener[] l = CVSCommandHandler.getInstance()
        .getGruntspudCVSListeners();
    for (int i = 0; i < l.length; i++) {
      GruntspudCVSListener g = (GruntspudCVSListener) l[i];
      if (g.isOptionalListener()) {
        JCheckBox cb = new JCheckBox(g.getShortName());
        cb.setSelected(context.getHost().getBooleanProperty(
            key + "." + g.getShortName(), g.isSelectedByDefault()));
        cb.putClientProperty("listener", g);
        cb.addActionListener(this);
        p.add(cb);
        otherListeners.put(g.getShortName(), cb);
      }
    }
    action.setOutputToConsole(outputToConsole.isSelected());
    if (outputToReportDialog != null)
        ((ReportingGruntspudAction) action)
            .setOutputToReportDialog(outputToReportDialog.isSelected());
    add(p, BorderLayout.SOUTH);
    saveOutputOptions();
    setAvailableListeners();
  }

  private void setAvailableListeners() {
    java.util.List l = new java.util.ArrayList();
    for (Iterator i = otherListeners.keySet().iterator(); i.hasNext();) {
      String n = (String) i.next();
      JCheckBox cb = (JCheckBox) otherListeners.get(n);
      if (cb.isSelected()) {
        l.add((GruntspudCVSListener) cb.getClientProperty("listener"));
      }
    }
    GruntspudCVSListener[] ln = new GruntspudCVSListener[l.size()];
    l.toArray(ln);
    action.setEnabledOptionalListeners(ln);
  }

  public void actionPerformed(ActionEvent evt) {
    if (evt.getSource() == outputToConsole) {
      action.setOutputToConsole(outputToConsole.isSelected());
    } else if (evt.getSource() == outputToReportDialog) {
      ((ReportingGruntspudAction) action)
          .setOutputToReportDialog(outputToReportDialog.isSelected());
    }
    saveOutputOptions();
    setAvailableListeners();
  }

  protected void saveOutputOptions() {
    if(outputToConsole != null) {
      context.getHost().setBooleanProperty(
          key + "." + Constants.OPTION_DIALOG_OUTPUT_OPTIONS_CONSOLE,
          outputToConsole.isSelected());
    }
    if(outputToReportDialog != null) {
      context.getHost().setBooleanProperty(
          key + "." + Constants.OPTION_DIALOG_OUTPUT_OPTIONS_REPORT_DIALOG,
          outputToReportDialog.isSelected());
    }
    for (Iterator i = otherListeners.keySet().iterator(); i.hasNext();) {
      String n = (String) i.next();
      JCheckBox cb = (JCheckBox) otherListeners.get(n);
      context.getHost().setBooleanProperty(key + "." + cb.getText(),
          cb.isSelected());
    }
  }
}