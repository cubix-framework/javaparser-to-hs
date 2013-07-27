/*
 * XInsertOptionPane.java
 * Copyright (C) 2001 Dominic Stolerman
 * dstolerman@jedit.org
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
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.AbstractTableModel;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.util.Log;

public class XInsertOptionPane extends AbstractOptionPane implements ActionListener {
  // public members
  public XInsertOptionPane() {
    super("xinsert");
  }
  
  public void _init() {
    setLayout(new BorderLayout());
    setBorder(new EmptyBorder(5,5,5,5));
    
    JPanel dispOpt = new JPanel(new BorderLayout());
    dispOpt.setBorder(new EmptyBorder(0,25,0,0));
    Box left = Box.createVerticalBox();
    Box right = Box.createVerticalBox();
    initInserts(left, right);
    
    Box options = Box.createHorizontalBox();
    left.add(Box.createGlue());
    right.add(Box.createGlue());
    options.add(left);
    options.add(right);
    dispOpt.add(options, BorderLayout.CENTER);
    
    displayDefaults = new JCheckBox(jEdit.getProperty("options.xinsert.display.defaults"));
    displayDefaults.addActionListener(this);
    JPanel dispDef = new JPanel(new BorderLayout());
    dispDef.setOpaque(false);
    dispDef.setBorder(new TitledBorder(new EtchedBorder(), jEdit.getProperty("options.xinsert.display.title")));
    dispDef.add(displayDefaults, BorderLayout.NORTH);
    dispDef.add(dispOpt, BorderLayout.CENTER);
    
    
    JPanel outerFilePanel = new JPanel(new BorderLayout());
    JPanel filePanel = new JPanel(new BorderLayout());
    filePanel.setOpaque(false);
    filePanel.setBorder(new TitledBorder(new EtchedBorder(), jEdit.getProperty("options.xinsert.inserts-directory")));
    directoryText = new JTextField();
    filePanel.add(directoryText, BorderLayout.CENTER);
    pickDirectory = new JButton(jEdit.getProperty("options.xinsert.choose-directory"));
    pickDirectory.addActionListener(this);
    directoryText.setText(jEdit.getProperty("xinsert.inserts-directory"));
    filePanel.add(pickDirectory, BorderLayout.EAST);
    outerFilePanel.add(filePanel, BorderLayout.NORTH);
    
    JPanel varPanel = new JPanel(new BorderLayout());
    varPanel.setBorder(new EmptyBorder(5,0,0,0));
    varPanel.add(new JLabel("Global Variables:"), BorderLayout.NORTH);
    vmodel = new VariablesModel();
    varsTable = new JTable(vmodel);
    varsTable.getTableHeader().setReorderingAllowed(false);
    Dimension d = varsTable.getPreferredSize();
    d.height = Math.min(d.height,100);
    JScrollPane scroller = new JScrollPane(varsTable);
    scroller.setPreferredSize(d);
    varPanel.add(scroller, BorderLayout.CENTER);
    outerFilePanel.add(varPanel, BorderLayout.CENTER);
    
    add(dispDef, BorderLayout.NORTH);
    add(outerFilePanel, BorderLayout.CENTER);
    
    displayDefaults.setSelected(jEdit.getBooleanProperty("xinsert.display.all", true));
    displaySettings();
    
  }
  
  public void initInserts(Box left, Box right) {
    Vector ins = new Vector(10);
    int i =0;
    String current;
    while((current = jEdit.getProperty("xinsert.inserts." + i)) != null) {
      ins.add(current);
      i++;
    }
    ins.add(jEdit.getProperty("xinsert.inserts.macros"));
    insertCheckBoxes = new InsertCheckBox[ins.size()];
    for(int j=0; j<insertCheckBoxes.length; j++) {
      insertCheckBoxes[j] = new InsertCheckBox((String)ins.get(j));
      if(j%2 == 0)
        left.add(insertCheckBoxes[j]);
      else
        right.add(insertCheckBoxes[j]);
    }
    
  }
  private void displaySettings() {
    if(displayDefaults.isSelected()) {
      for(int i=0; i<insertCheckBoxes.length; i++)
        insertCheckBoxes[i].setEnabled(true);
    }
    else {
      for(int i=0; i<insertCheckBoxes.length; i++)
        insertCheckBoxes[i].setEnabled(false);
    }
  }
  
  public void _save() {
    jEdit.setBooleanProperty("xinsert.display.all", displayDefaults.isSelected());
    for(int i=0; i<insertCheckBoxes.length; i++)
      insertCheckBoxes[i].saveState();
      Log.log(Log.DEBUG,this,"XInsert directory set to" + directoryText.getText());
      jEdit.setProperty("xinsert.inserts-directory", directoryText.getText());
      
      // if one cell is currently being edited, stop editing, so that we get its value
      // see https://sourceforge.net/tracker/index.php?func=detail&aid=2941806&group_id=588&atid=565475
      if(varsTable.getCellEditor()!=null){
      	  varsTable.getCellEditor().stopCellEditing();
      }
      XInsertPlugin.clearVariables();
      Iterator vis = vmodel.vars.iterator();
      while(vis.hasNext()) {
        VariablesModel.VarItem vi = (VariablesModel.VarItem)vis.next();
        if(vi.name != null && vi.name != "")
          XInsertPlugin.setVariable(vi.name, (vi.value == null ? "" : vi.value));
      }
  }
  
  
  // begin ActionListener implementation
  public void actionPerformed(ActionEvent evt) {
    if(evt.getSource() == pickDirectory) {
      JFileChooser chooser = new JFileChooser();
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retVal = chooser.showDialog(this,jEdit.getProperty("options.xinsert.choose-directory"));
      if(retVal == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        if(file != null) {
          try {
            String dirName = file.getCanonicalPath();
            directoryText.setText(dirName);
          }
          catch(IOException e) {
            // shouldn't happen
          }
        }
      }
    }
    else if(evt.getSource() == displayDefaults) {
      displaySettings();
    }
    else Log.log(Log.ERROR,this,"Action Event Not Defined"); 
  }
  // end ActionListener implementation
  
  // private members
  private JTable varsTable;
  private JButton pickDirectory;
  private JTextField directoryText;
  private JCheckBox displayDefaults;
  private InsertCheckBox[] insertCheckBoxes;
  private VariablesModel vmodel;
  
  class VariablesModel extends AbstractTableModel {
    public VariablesModel() {
      vars = new Vector(XInsertPlugin.getVariablesSize());
      Enumeration en = XInsertPlugin.getVariables();
      while(en.hasMoreElements()) {
        String name = (String)en.nextElement();
        vars.add(new VarItem(name, XInsertPlugin.getVariable(name)));
      }
    }
    
    public Class getColumnClass(int index) {
      return String.class;
    }
    
    public int getColumnCount() {
      return 2;
    }
    
    public int getRowCount() {
      return vars.size() + 1;
    }
    
    public Object getValueAt(int row, int column) {
      if(row == vars.size())
        return "";
        
        VarItem vi = (VarItem)vars.elementAt(row);
        if(column == 0)
          return vi.name;
        else
          return vi.value;
    }
    
    public boolean isCellEditable(int row, int col) {
      return true;
    }
    
    public void setValueAt(Object value, int row, int col) {
      // When we change the last row, another one is added...
      if(row == vars.size()) {
        String name = (col == 0 ? (String)value : null);
        String _value = (col == 1 ? (String)value : null);
        vars.addElement(new VarItem(name,_value));
        
        fireTableRowsUpdated(row,row + 1);
      }
      else {
        VarItem vi = (VarItem)vars.elementAt(row);
        switch(col) {
          case 0:
            vi.name = (String)value;
            break;
          case 1:
            vi.value = (String)value;
            break;
        }
        
        fireTableRowsUpdated(row,row);
      }
    }
    
    public String getColumnName(int index) {
      switch(index) {
        case 0:
          return "Name";
        case 1:
          return "Value";
        default:
          return null;
      }
    }
    
    Vector vars;
    
    class VarItem {
      VarItem() { }
      
      VarItem(String name, String value) {
        this.name = name;
        this.value = value;
      }
      
      String name;
      String value;
    }
  }
  
  private class InsertCheckBox extends JCheckBox {
    private InsertCheckBox(String insert) {
      this.insert = insert;
      setText(jEdit.getProperty("options.xinsert.display." + insert, "Unknown!"));
      setSelected(jEdit.getBooleanProperty("xinsert.display." + insert, true));
    }
    
    private void saveState() {
      jEdit.setBooleanProperty("xinsert.display." + insert, isSelected());
    }
    
    private String insert;
  }
}

