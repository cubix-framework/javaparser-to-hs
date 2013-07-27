/*
 *  KeyValuePanel.java - GUI panel for list of XSL parameters and XPath Namespaces
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
 * GUI panel for list of XSL parameters and XPath Namespaces
 *
 * @author Robert McKinnon - robmckinnon@users.sourceforge.net
 */
public class KeyValuePanel extends JPanel implements ListSelectionListener, TableModelListener {

  private KeyValueTableModel model;
  private JTable parameterTable;
  private XsltAction addKeyValueAction;
  private XsltAction removeKeyValueAction;
  private static final String NAME = ".name";
  private static final String VALUE = ".value";
  private boolean isResetting = false;
  private final String name;

  public KeyValuePanel(String name) {
    super(new BorderLayout());

    this.name = name;
    this.model = initKeyValueTableModel();
    this.model.addTableModelListener(this);
    this.parameterTable = initKeyValueTable(this.model);

    addKeyValueAction = new AddKeyValueAction();
  	removeKeyValueAction = new RemoveKeyValueAction();
    XsltAction[] actions = new XsltAction[]{addKeyValueAction, removeKeyValueAction};
    parameterTable.setComponentPopupMenu(XsltAction.initMenu(actions));

    JLabel label = new JLabel(jEdit.getProperty(name+".label"));
    JScrollPane tablePane = new JScrollPane(this.parameterTable);
    JToolBar toolBar = initToolBar();
    toolBar.setBorder(BorderFactory.createEmptyBorder(0,6,0,2));

    add(label, BorderLayout.NORTH);
    add(tablePane, BorderLayout.CENTER);
    add(toolBar, BorderLayout.EAST);
  }


  void stopEditing() {
  	  System.err.println(parameterTable.isEditing());
    if(parameterTable.isEditing()) {
      TableCellEditor defaultEditor = parameterTable.getDefaultEditor(Object.class);
      if(defaultEditor != null) {
        defaultEditor.stopCellEditing();
      }
    }
  }


  public void valueChanged(ListSelectionEvent e) {
    boolean selectionExists = this.parameterTable.getSelectedRow() != -1;
    this.removeKeyValueAction.setEnabled(selectionExists);
  }


  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    logEvent(e.getType(), row);

    if(!isResetting) {
      storeKeyValues();
    }
  }


  public void setKeyValues(String[] names, String[] values) {
    this.isResetting = true;
    this.model.clear();

    for(int i = 0; i < names.length; i++) {
      this.model.addKeyValue(names[i], values[i]);
    }

    this.isResetting = false;

    storeKeyValues();
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


  private void storeKeyValues() {
    LinkedList nameList = new LinkedList();
    LinkedList valueList = new LinkedList();

    for(int i = 0; i < this.model.getRowCount(); i++) {
      String parameterName = this.model.getKeyValueName(i);
      if(!parameterName.equals("")) {
        nameList.add(parameterName);
        valueList.add(this.model.getKeyValueValue(i));
      }
    }

    PropertyUtil.setEnumeratedProperty(name+NAME, nameList);
    PropertyUtil.setEnumeratedProperty(name+VALUE, valueList);
  }


  /**
   * Returns a map with parameter name as the key, and parameter value as the value.
   */
  public Map getMap() {
  	// get live parameters, even if we were still editing the value (didn't press ENTER)
  	stopEditing();
    Object[] names = PropertyUtil.getEnumeratedProperty(name+NAME).toArray();
    Object[] values = PropertyUtil.getEnumeratedProperty(name+VALUE).toArray();

    Map parameterMap = new HashMap();

    for(int i = 0; i < names.length; i++) {
      parameterMap.put(names[i], values[i]);
    }
    System.out.println(parameterMap);

    return parameterMap;
  }


  public int getCount() {
    int rowCount = this.model.getRowCount();

    if(rowCount > 0 && getName(rowCount - 1).equals("")) {
      rowCount--;
    }

    return rowCount;
  }


  public String getName(int index) {
    return this.model.getKeyValueName(index);
  }


  public String getValue(int index) {
    return this.model.getKeyValueValue(index);
  }


  private KeyValueTableModel initKeyValueTableModel() {
    KeyValueTableModel model = new KeyValueTableModel(name);

    Object[] names = PropertyUtil.getEnumeratedProperty(name+NAME).toArray();
    Object[] values = PropertyUtil.getEnumeratedProperty(name+VALUE).toArray();

    for(int i = 0; i < names.length; i++) {
      model.addKeyValue((String)names[i], (String)values[i]);
    }

    return model;
  }


  private JTable initKeyValueTable(TableModel model) {
  	  JTable table = new JTable(model){
  	  	/**
  	  	 * lets the JTable fill the whole ViewPort, allowing for
  	  	 * the context-menu to  be shown even if there is no parameter yet
  	  	 * {@link http://explodingpixels.wordpress.com/2008/10/05/making-a-jtable-fill-the-view-without-extension/}
  	  	 */  
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return getParent() instanceof javax.swing.JViewport
					&& getPreferredSize().height < getParent().getHeight();
		}
  	  };
    table.setName(name);
    table.getSelectionModel().addListSelectionListener(this);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    DefaultCellEditor defaultCellEditor = ((DefaultCellEditor)table.getDefaultEditor(String.class));
    defaultCellEditor.setClickCountToStart(1);
    return table;
  }


  private JToolBar initToolBar() {
    removeKeyValueAction.setEnabled(false);

    JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);
    toolBar.setFloatable(false);
    toolBar.add(addKeyValueAction.getButton());
    toolBar.add(removeKeyValueAction.getButton());
    return toolBar;
  }


  private class AddKeyValueAction extends XsltAction {
  	
    AddKeyValueAction(){
      super(name+".add");
    }

    public void actionPerformed(ActionEvent e) {
      KeyValuePanel.this.stopEditing();
      model.addKeyValue("", "");
      int lastRow = model.getRowCount() - 1;

      parameterTable.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
      parameterTable.getSelectionModel().setSelectionInterval(lastRow, lastRow);
      parameterTable.requestFocus();
      parameterTable.editCellAt(lastRow, 0);
    }

  }


  private class RemoveKeyValueAction extends XsltAction {

    RemoveKeyValueAction(){
      super(name+".remove");
    }

    public void actionPerformed(ActionEvent e) {
      int selectedRow = parameterTable.getSelectedRow();
      if(selectedRow != -1) {
      	// fix an exception when removing a parameter when it is still
      	// being edited, then adding a parameter (stopEditing() is then
      	// called while the row has been removed)
      	KeyValuePanel.this.stopEditing();
        model.removeKeyValue(selectedRow);

        if(selectedRow != 0 && selectedRow < model.getRowCount()) {
          parameterTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
          parameterTable.requestFocus();
        }
      }
    }

  }

}