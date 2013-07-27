/*
 *  StylesheetPanel.java - GUI panel for list of XSL parameters
 *
 *  Copyright (C) 2003 Robert McKinnon
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package xslt;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

import javax.swing.DefaultCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.JPopupMenu;
import javax.swing.BorderFactory;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableCellEditor;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * GUI panel for list of XSL parameters
 *
 * @author Robert McKinnon - robmckinnon@users.sourceforge.net
 */
public class StylesheetParameterPanel extends JPanel implements ListSelectionListener, TableModelListener {

  private StylesheetParameterTableModel parameterTableModel;
  private JTable parameterTable;
  private XsltAction addParameterAction = new AddParameterAction();
  private XsltAction removeParameterAction = new RemoveParameterAction();
  private static final String PARAMETER_NAME = "xslt.parameter.name";
  private static final String PARAMETER_VALUE = "xslt.parameter.value";
  private boolean isResetting = false;


  /**
   * Constructor for the XSLTProcessor object.
   */
  public StylesheetParameterPanel(MouseListener mouseListener) {
    super(new BorderLayout());

    this.parameterTableModel = initParameterTableModel();
    this.parameterTableModel.addTableModelListener(this);
    this.parameterTable = initParameterTable(this.parameterTableModel);
    parameterTable.addMouseListener(mouseListener);

    JLabel label = new JLabel(jEdit.getProperty("xslt.parameters.label"));
    JScrollPane tablePane = new JScrollPane(this.parameterTable);
    JToolBar toolBar = initToolBar();
    toolBar.setBorder(BorderFactory.createEmptyBorder(0,6,0,2));

    add(label, BorderLayout.NORTH);
    add(tablePane, BorderLayout.CENTER);
    add(toolBar, BorderLayout.EAST);
  }


  void stopEditing() {
    if(parameterTable.isEditing()) {
      TableCellEditor defaultEditor = parameterTable.getDefaultEditor(Object.class);
      if(defaultEditor != null) {
        defaultEditor.stopCellEditing();
      }
    }
  }


  JPopupMenu initStylesheetMenu(XsltAction fileOpenAction) {
    XsltAction[] actions = new XsltAction[]{addParameterAction, removeParameterAction};

    return XsltAction.initMenu(actions);
  }


  public void valueChanged(ListSelectionEvent e) {
    boolean selectionExists = this.parameterTable.getSelectedRow() != -1;
    this.removeParameterAction.setEnabled(selectionExists);
  }


  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    logEvent(e.getType(), row);

    if(!isResetting) {
      storeParameters();
    }
  }


  public void setParameters(String[] names, String[] values) {
    this.isResetting = true;
    this.parameterTableModel.clear();

    for(int i = 0; i < names.length; i++) {
      this.parameterTableModel.addParameter(names[i], values[i]);
    }

    this.isResetting = false;

    storeParameters();
  }


  private void logEvent(int event, int row) {
    String eventText = "";

    switch(event) {
      case TableModelEvent.UPDATE:
        eventText = "update ";
        break;
      case TableModelEvent.INSERT:
        eventText = "insert ";
        break;
      case TableModelEvent.DELETE:
        eventText = "delete ";
    }

    Log.log(Log.DEBUG, this, eventText + "row " + row);
  }


  private void storeParameters() {
    LinkedList nameList = new LinkedList();
    LinkedList valueList = new LinkedList();

    for(int i = 0; i < this.parameterTableModel.getRowCount(); i++) {
      String parameterName = this.parameterTableModel.getParameterName(i);
      if(!parameterName.equals("")) {
        nameList.add(parameterName);
        valueList.add(this.parameterTableModel.getParameterValue(i));
      }
    }

    PropertyUtil.setEnumeratedProperty(PARAMETER_NAME, nameList);
    PropertyUtil.setEnumeratedProperty(PARAMETER_VALUE, valueList);
  }


  /**
   * Returns a map with parameter name as the key, and parameter value as the value.
   */
  public Map getParametersMap() {
    Object[] names = PropertyUtil.getEnumeratedProperty(PARAMETER_NAME).toArray();
    Object[] values = PropertyUtil.getEnumeratedProperty(PARAMETER_VALUE).toArray();

    Map parameterMap = new HashMap();

    for(int i = 0; i < names.length; i++) {
      parameterMap.put(names[i], values[i]);
    }

    return parameterMap;
  }


  public int getParametersCount() {
    int rowCount = this.parameterTableModel.getRowCount();

    if(rowCount > 0 && getParameterName(rowCount - 1).equals("")) {
      rowCount--;
    }

    return rowCount;
  }


  public String getParameterName(int index) {
    return this.parameterTableModel.getParameterName(index);
  }


  public String getParameterValue(int index) {
    return this.parameterTableModel.getParameterValue(index);
  }


  private StylesheetParameterTableModel initParameterTableModel() {
    StylesheetParameterTableModel model = new StylesheetParameterTableModel();

    Object[] names = PropertyUtil.getEnumeratedProperty(PARAMETER_NAME).toArray();
    Object[] values = PropertyUtil.getEnumeratedProperty(PARAMETER_VALUE).toArray();

    for(int i = 0; i < names.length; i++) {
      model.addParameter((String)names[i], (String)values[i]);
    }

    return model;
  }


  private JTable initParameterTable(TableModel model) {
    JTable table = new JTable(model);
    table.getSelectionModel().addListSelectionListener(this);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    DefaultCellEditor defaultCellEditor = ((DefaultCellEditor)table.getDefaultEditor(String.class));
    Log.log(Log.DEBUG, this, "" + defaultCellEditor.getClickCountToStart());
    defaultCellEditor.setClickCountToStart(1);
    return table;
  }


  private JToolBar initToolBar() {
    removeParameterAction.setEnabled(false);

    JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
    toolBar.setFloatable(false);
    toolBar.add(addParameterAction.getButton());
    toolBar.add(removeParameterAction.getButton());
    return toolBar;
  }


  private class AddParameterAction extends XsltAction {
    public void actionPerformed(ActionEvent e) {
      StylesheetParameterPanel.this.stopEditing();
      parameterTableModel.addParameter("", "");
      int lastRow = parameterTableModel.getRowCount() - 1;

      parameterTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
      parameterTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
      parameterTable.requestFocus();
      parameterTable.editCellAt(lastRow, 0);
    }


    protected String getActionName() {
      return "parameters.add";
    }
  }


  private class RemoveParameterAction extends XsltAction {
    public void actionPerformed(ActionEvent e) {
      int selectedRow = parameterTable.getSelectedRow();
      if(selectedRow != -1) {
        parameterTableModel.removeParameter(selectedRow);

        if(selectedRow != 0 && selectedRow < parameterTableModel.getRowCount()) {
          parameterTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
          parameterTable.requestFocus();
        }
      }
    }


    protected String getActionName() {
      return "parameters.remove";
    }
  }

}