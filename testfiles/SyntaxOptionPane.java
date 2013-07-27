/*
* $Revision: 13763 $
* $Date: 2008-09-23 06:02:41 -0400 (Tue, 23 Sep 2008) $
* $Author: kerik-sf $
*
* Copyright (C) 2008 Eric Le Lay
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

package cswilly.jeditPlugins.spell;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.*;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.util.*;

import org.gjt.sp.jedit.syntax.Token;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.util.Log;

import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.JCheckBoxList;
import common.gui.OkCancelButtons;

import static cswilly.jeditPlugins.spell.SyntaxHandlingManager.*;

public class SyntaxOptionPane
extends AbstractOptionPane
{
	
	private ProfileTableModel model;
	
	public SyntaxOptionPane()
	{
		super( "spellcheck.syntax" );
	}
	
	public void _init()
	{
		
		// warning if syntax-sensitivity is disabled
		boolean disabled = jEdit.getBooleanProperty( SyntaxHandlingManager.GLOBAL_DISABLE_PROP);
		if(disabled){
			JTextArea area = new JTextArea(jEdit.getProperty("options.SpellCheck.syntax.warning-disabled"));
			area.setEditable(false);
			area.setBackground(getBackground());
			area.setForeground(java.awt.Color.red);
			addComponent(area);
			addComponent(Box.createVerticalStrut(30));
		}
		
		model = new ProfileTableModel();
		
		JTable table = new JTable(model);	  
		table.setDefaultRenderer(byte[].class,
			new TokenTypeRenderer());
        table.setDefaultEditor(byte[].class,
			new TokenTypePicker());
        table.setDefaultEditor(String.class,
			new KeepEnterEditor());
		
		
		//dimensions
		TableColumn column = null;
		int[]widths = {60,80,200,60};
		for (int i = 0; i < 4; i++) {
			column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(widths[i]);
		}
		table.setPreferredScrollableViewportSize(new Dimension(400,200));
		
		addComponent(new JScrollPane(table));
		
		JPanel controlPanel = new JPanel();
		JButton bout = new JButton(new AddAction(model));
		bout.setName("addProfile");
		controlPanel.add(bout);
		
		RemoveAction remove = new RemoveAction(model);
		table.getSelectionModel().addListSelectionListener(remove);
		bout = new JButton(remove);
		bout.setName("removeProfile");
		controlPanel.add(bout);
		addComponent(controlPanel);
		addComponent(Box.createVerticalStrut(50));
		JTextArea area = new JTextArea(jEdit.getProperty("options.SpellCheck.syntax.description"));
		area.setEditable(false);
		area.setBackground(getBackground());
		addComponent(area);
	}
	
	public void _save()
	{
		model.save();
	}
	
	/**
	 * keeps a list of SyntaxHandlingManager.Profile and derive the data from it.
	 */
	private class ProfileTableModel extends AbstractTableModel{
		/** list of profiles */
		private List<Profile> profiles;
		
		/** load profiles and init model... */
		ProfileTableModel(){
			Profile[] pro = SyntaxHandlingManager.loadProfiles();
			profiles = new ArrayList<Profile>(pro.length);
			for(int i=0;i<pro.length;i++){
				profiles.add(pro[i]);
			}
		}
		
		public int getRowCount(){
			return profiles.size();
		}
		
		public int getColumnCount(){
			return 4;
		}
		
		public String getColumnName(int column){
			switch(column){
			case 0: return "Default";
			case 1: return "Name";
			case 2: return "Modes";
			case 3: return "Include";
				default : return null;
			}
		}
		
		public Class getColumnClass(int column){
			switch(column){
			case 0 : return Boolean.class;
			case 1 : return String.class;
			case 2 : return String.class;
			case 3 : return byte[].class;
				default : return null;
			}
		}
		
		public Object getValueAt(int row, int column){
			if(row<0||row>=profiles.size())return null;
			Profile p = profiles.get(row);
			switch(column){
			case 0: return p.isDefault?Boolean.TRUE:Boolean.FALSE;
			case 1: return p.name;
			case 2:
				String s = "";
				if(p.modes.length>0){
					for(String mode:p.modes)s+=","+mode;
					s = s.substring(1);
				}
				return s;
			case 3: return p.tokenTypesToInclude;
				default : return null;
			}
		}
		@Override
		public void setValueAt(Object o, int row, int column){
			System.out.println("setValueAt("+row+","+column+","+o);
			if(row<0||row>=profiles.size())return;
			if(column<0||column>=getColumnCount())return;
			if(o==null)return;
			Profile p = profiles.get(row);
			if(column==0){
				p.isDefault = ((Boolean)o).booleanValue();
				if(p.isDefault){
					for(int i=0;i<profiles.size();i++){
						Profile op=profiles.get(i);
						if(op.isDefault && p!=op){
							op.isDefault=false;
							fireTableRowsUpdated(i,i);
						}
					}
				}
			}if(column==1)p.name = (String)o;
			else if(column==2){
				String[] values = ((String)o).split("\\s*,\\s*");
				p.modes = values;
			}else if(column==3){
				p.tokenTypesToInclude=(byte[])o;
			}
			fireTableRowsUpdated(row,row);
		}
		
		public boolean isCellEditable(int row, int column){
			return true;
		}
		
		/**
		 * add a new profile
		 * @param	p	non-null new profile to add
		 */
		void add(Profile p){
			profiles.add(p);
			fireTableRowsInserted(profiles.size(),profiles.size());
		}
		
		/**
		 * remove an existing profile
		 * @param	index	index of the profile in the list
		 */
		void remove(int index){
			if(index<0|| index>=profiles.size())throw new IllegalArgumentException("illegal profile index to remove :"+index);
			profiles.remove(index);
			fireTableRowsDeleted(index,index);
		}
		
		/**
		 * persist in properties the current model
		 */
		void save(){
			SyntaxHandlingManager.clearProfiles();
			SyntaxHandlingManager.saveProfiles(profiles.toArray(new Profile[profiles.size()]));
		}
	}
	
	/**
	 * Selecting a list of token types from Token.TOKEN_TYPES,
	 * via a list of checkboxes
	 */
	private static class TokenListDialog extends EnhancedDialog{
		private boolean confirm = false;
		private JCheckBoxList list;
		
		TokenListDialog(JDialog parent){
			super(parent,jEdit.getProperty("options.SpellCheck.syntax.dialog.title"),true);
			setSize(new Dimension(300,250));
			getContentPane().add(BorderLayout.NORTH,new JLabel(jEdit.getProperty("options.SpellCheck.syntax.dialog.message")));
			list = new JCheckBoxList(Token.TOKEN_TYPES);
			getContentPane().add(BorderLayout.CENTER,new JScrollPane(list));
			JButton bout = new JButton(jEdit.getProperty("options.SpellCheck.syntax.dialog.select-all"));
			bout.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent ae){
						list.selectAll();
					}
			});
			getContentPane().add(BorderLayout.EAST,bout);
			getContentPane().add(BorderLayout.SOUTH,new OkCancelButtons(this));		  
		}
		
		public void ok(){
			confirm = true;
			setVisible(false);
		}
		
		public void cancel(){
			confirm = false;
			setVisible(false);
		}
		
		public byte[] pickTokenKinds(byte[] alreadySelected){
			Arrays.sort(alreadySelected);
			JCheckBoxList.Entry[] entries = new JCheckBoxList.Entry[Token.ID_COUNT];
			for(byte i=0;i<Token.ID_COUNT;i++){
				boolean selected = Arrays.binarySearch(alreadySelected,i)>=0;
				entries[i] = new JCheckBoxList.Entry(selected,Token.TOKEN_TYPES[i]);
			}
			list.setModel(entries);
			setVisible(true);
			if(confirm){
				Object[] values = list.getValues();
				byte[] selected = new byte[Token.ID_COUNT];
				int selCount = 0;
				for(int i=0;i<values.length;i++){
					if(((JCheckBoxList.Entry)values[i]).isChecked()){
						selected[selCount++] = (byte)i;
					}
				}
				byte[] ret = new byte[selCount];
				System.arraycopy(selected,0,ret,0,selCount);
				return ret;
			}else{
				return null;
			}
		}
		
	}
	
	private static class TokenTypeRenderer extends DefaultTableCellRenderer{
		private final String lbl = jEdit.getProperty("options.SpellCheck.syntax.edit");
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return super.getTableCellRendererComponent(table,lbl,isSelected,hasFocus,row,column);
		}
	}
	
	/**
	* clicking in the cell fires a TokenListDialog to pick token types to include.
	*/
	private static class TokenTypePicker extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, ActionListener{
		byte[] currentTokens;
		JButton button;
		TokenListDialog chooser;
		
		public TokenTypePicker() {
			button = new JButton(jEdit.getProperty("options.SpellCheck.syntax.edit"));
			button.addActionListener(this);
			button.setBorderPainted(false);
			//Set up the dialog that the button brings up.
			
		}
		
		public void actionPerformed(ActionEvent e) {
			byte[] newTokens = chooser.pickTokenKinds(currentTokens);
			if(newTokens!=null)currentTokens = newTokens;
			fireEditingStopped(); //Make the renderer reappear.
			
		}
		
		//Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return currentTokens;
		}
		
		//Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column)
		{
			if(chooser==null)chooser = new TokenListDialog((JDialog)table.getTopLevelAncestor());
			currentTokens = (byte[])value;
			return button;
		}
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			return button;
		}
	}
	
	/**
	* Similar to defaultCellEditor, but listens to ENTER key, 
	* to prevent EnhancedDialog from consuming the event and lose the text.
	*/
	private static class KeepEnterEditor extends AbstractCellEditor
	implements TableCellEditor, ActionListener
	{
		JTextField field;
		
		public KeepEnterEditor() {
			field = new JTextField();
			field.addActionListener(this);
			field.addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent evt){
						if(evt.getKeyCode() == KeyEvent.VK_ENTER)
							field.postActionEvent();
					}
			});
		}
		
		public void actionPerformed(ActionEvent e) {
			fireEditingStopped();
		}
		
		public Object getCellEditorValue() {
			return field.getText();
		}
		
		public Component getTableCellEditorComponent(JTable table,
			Object value,
			boolean isSelected,
			int row,
			int column)
		{
			field.setText((String)value);
			return field;
		}
	}
	
	/**
	* add a new profile to the table
	*/
	private class AddAction extends AbstractAction{
		private ProfileTableModel model;
		AddAction(ProfileTableModel model){
			super(jEdit.getProperty("options.SpellCheck.syntax.add.name"));
			putValue(Action.SHORT_DESCRIPTION,jEdit.getProperty("options.SpellCheck.syntax.add.tooltip"));
			this.model = model;
		}
		
		public void actionPerformed(ActionEvent evt){
			Profile p = new Profile("",new byte[0]);
			p.modes = new String[0];
			model.add(p);
		}
	}
	
	
	/** remove selected profile from the table */
	private static class RemoveAction extends AbstractAction implements ListSelectionListener{
		private ProfileTableModel model;
		private int selRow;
		RemoveAction(ProfileTableModel model){
			super(jEdit.getProperty("options.SpellCheck.syntax.remove.name"));
			putValue(Action.SHORT_DESCRIPTION,jEdit.getProperty("options.SpellCheck.syntax.remove.tooltip"));
			this.model = model;
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent evt){
			model.remove(selRow);
		}
		
		//listen to selections
		public void valueChanged(ListSelectionEvent e){
			ListSelectionModel lsModel = (ListSelectionModel)e.getSource();
			selRow = lsModel.getMinSelectionIndex();
			setEnabled(selRow>=0);
		}
	}
}
