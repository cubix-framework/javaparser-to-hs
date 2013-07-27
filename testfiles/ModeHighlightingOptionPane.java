/*
 * ModeHighlightingOptionPane.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2011 Evan Wright
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
 
package modelighting;

//{{{ Imports
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.StyleEditor;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.StandardUtilities;
//}}}

public class ModeHighlightingOptionPane extends AbstractOptionPane
{
	public static final EmptyBorder noFocusBorder = new EmptyBorder(1, 1, 1, 1);

	//{{{ ModeHighlighterOptionPane constructor
	public ModeHighlightingOptionPane()
	{
		super("syntax");
	}
	//}}}

	//{{{ Protected members

	//{{{ _init() method
	@Override
	protected void _init()
	{
		ActionHandler actionHandler = new ActionHandler();
		
		this.modeCombo = buildModeCombo(actionHandler);
		addComponent(jEdit.getProperty(PREFIX + "mode"), modeCombo);
		
		// Try to initialize to the mode of the currently-active style set
		View currentView = jEdit.getActiveView();
		if (currentView != null)
		{
			Mode currentMode = currentView.getBuffer().getMode();
			if (currentMode != null && !currentMode.getName().equals("text"))
			{
				// Only select the current mode if a mode-specific style set is
				// currently active
				String activeStyleSet = StyleSettings.getActiveStyleSetName(currentMode.getName());
				if (activeStyleSet != null && activeStyleSet.indexOf("global/") != 0)
				{
					selectMode(currentMode.getName());
				}
			}
		}
		refreshModeName();
		
		// Combo box to select which style set in the current mode to edit
		styleSetCombo = new JComboBox();
		addComponent(jEdit.getProperty(PREFIX + "style-sets"), styleSetCombo);
		styleSetCombo.addActionListener(actionHandler);
		updateStyleSets(modeName, styleSetCombo, null);
		
		addButtonPanel(actionHandler);
		
		addStyleBox();
		refreshStyleBox();
		
		addSeparator();
		
		// Combo box to set the currently-active style set for the selected mode. The
		// choices also include the global style sets
		activeStyleSetCombo = new JComboBox();
		addComponent(jEdit.getProperty(PREFIX + "active-style-set"), activeStyleSetCombo);
		activeStyleSetCombo.addActionListener(actionHandler);
		refreshActiveStyleSets();
		
		initialized = true;
	} //}}}

	//{{{ _save() method
	@Override
	protected void _save()
	{	
		styleModel.save();
		
		ActiveStyleSetChoice activeChoice =
			(ActiveStyleSetChoice)activeStyleSetCombo.getSelectedItem();
		StyleSettings.setActiveStyleSet(modeName, activeChoice.getName());
	} //}}}

	//}}}

	//{{{ Private members
	
	private static final String PREFIX = "options.mode-highlighting.";
	
	//{{{ Instance variables
	private StyleTableModel styleModel;
	private JTable styleTable;
	private JComboBox modeCombo;
	private JComboBox styleSetCombo;
	private JComboBox activeStyleSetCombo;
	private JButton newButton;
	private JButton copyButton;
	private JButton deleteButton;
	
	private String modeName;
	private String initialStyleSet;
	private boolean initialized = false;
	//}}}
	
	//{{{ refreshStyleSets() method
	/**
	 * Refreshes the list of style sets for the currently-selected mode.
	 */
	private void refreshStyleSets(String initialSelection)
	{	
		updateStyleSets(modeName, styleSetCombo, initialSelection);
		
		if (styleSetCombo.getItemCount() == 0)
		{	
			styleSetCombo.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		else
		{
			styleSetCombo.setEnabled(true);
			deleteButton.setEnabled(true);
		}
	} //}}}
	
	//{{{ updateStyleSets() method
	/**
	 * Loads the list of style sets for the given mode and update a combo box
	 * @param initialSelection the style set to select initially, or null to select the
	 * 	currently active style set for the given mode
	 */
	private void updateStyleSets(String mode, JComboBox comboBox, String initialSelection)
	{
		String[] styleSets = StyleSettings.loadStyleSetNames(modeName);
		
		styleSetCombo.removeAllItems();
		for (String styleSet : styleSets)
		{	
			styleSetCombo.addItem(styleSet);
		}
		
		// Try to select the currently-active style set for this mode
		if (initialSelection == null)
		{
			initialSelection = StyleSettings.getActiveStyleSetName(mode);
		}
		
		for (String styleSet : styleSets)
		{
			if (styleSet.equals(initialSelection))
			{
				styleSetCombo.setSelectedItem(styleSet);
			}
		}
	} //}}}	
	
	//{{{ refreshActiveStyleSets() method
	/**
	 * Refreshes the list of choices for the active style set of the currently-selected mode.
	 */
	private void refreshActiveStyleSets()
	{
		activeStyleSetCombo.removeAllItems();		
		if (modeName != null)
		{
			// Add the option for the active style set for this mode to always be the
			// currently-active global style set
			activeStyleSetCombo.addItem(new ActiveStyleSetChoice(
				jEdit.getProperty(PREFIX + "global-default"), null));
			String[] styleSets = StyleSettings.loadStyleSetNames(modeName); 			
			for (String localSet : styleSets)
			{
				activeStyleSetCombo.addItem(
					new ActiveStyleSetChoice(localSet, localSet));
			}
		}
		
		String[] globalStyleSets = StyleSettings.loadStyleSetNames(null);
		for (String globalSet : globalStyleSets)
		{	
			activeStyleSetCombo.addItem(new ActiveStyleSetChoice(globalSet, globalSet));
		}	
		
		// Select the style set which is actually currently active
		String activeStyleSet = StyleSettings.getActiveStyleSetName(modeName);
		
		for (int i = 0; i < activeStyleSetCombo.getItemCount(); i++)
		{
			ActiveStyleSetChoice choice =
				(ActiveStyleSetChoice)activeStyleSetCombo.getItemAt(i);
		
			boolean match = false;
			if (choice.getName() == null)
			{
				if (activeStyleSet == null)
				{
					match = true;
				}
				else
				{
					match = false;
				}
			}
			else if (choice.getName().equals(activeStyleSet))
			{
				match = true;
			}
			
			if (match)
			{
				activeStyleSetCombo.setSelectedIndex(i);
				return;
			}
		}
		
		Log.log(Log.ERROR, this, "Could not find style " + activeStyleSet +
			" in mode " + modeName);
	} //}}}
	
	//{{{ refreshStyleBox() method
	/**
	 * Refreshes the style-editing box with the currently-selected style set
	 */
	private void refreshStyleBox()
	{	
		String styleSetName = (String)styleSetCombo.getSelectedItem();
		StyleSet styleSet;
		if (styleSetName == null)
		{
			// Fill the style table with dummy values. They won't be saved because
			// the style set name is null
			SyntaxStyle defaultStyle = StyleSettings.loadDefaultStyle();
			styleSet = new StyleSet(null, null, defaultStyle);
			styleTable.setEnabled(false);
		}
		else
		{
			styleSet = StyleSettings.loadStyleSet(modeName, styleSetName);
			styleTable.setEnabled(true);
		}
		
		updateStyleBox(styleSet);
	} //}}}
	
	//{{{ updateStyleBox() method
	/**
	 * Loads a style set into the style-editing box.
	 */
	private void updateStyleBox(StyleSet styleSet)
	{
		styleModel = new StyleTableModel(styleSet);
		styleTable.setModel(styleModel);
		
		TableColumn styleColumn = styleTable.getColumnModel().getColumn(1);
		styleColumn.setCellRenderer(new StyleTableModel.StyleRenderer());
	} //}}}
	
	//{{{ addStyleBox() method
	/**
	 * Adds the box containing the style-editing table to the option pane.
	 */
	private void addStyleBox()
	{	
		styleTable = new JTable();
		styleTable.setRowSelectionAllowed(false);
		styleTable.setColumnSelectionAllowed(false);
		styleTable.setCellSelectionEnabled(false);
		styleTable.getTableHeader().setReorderingAllowed(false);
		styleTable.addMouseListener(new MouseHandler());
		
		JPanel styleBox = new JPanel();
		styleBox.setLayout(new BorderLayout(6, 6));
		styleBox.add(styleTable);
		
		// Make this box as large as possible in both directions
		addComponent(styleBox, GridBagConstraints.BOTH); 
	} //}}}
	
	//{{{ buildModeCombo() method
	/**
	 * Adds the combo box that allows the user to select an edit mode to modify.
	 */
	private JComboBox buildModeCombo(ActionListener actionHandler)
	{
		// List of all modes + "<global>"
		Mode[] modes = jEdit.getModes();
		Arrays.sort(modes,new StandardUtilities.StringCompare<Mode>(true));
		Object[] modeOptions = new Object[modes.length + 1];
		modeOptions[0] = jEdit.getProperty(PREFIX + "global");
		System.arraycopy(modes, 0, modeOptions, 1, modes.length);
		
		JComboBox modeCombo = new JComboBox(modeOptions);
		modeCombo.addActionListener(actionHandler);
		
		return modeCombo;
	} //}}}
	
	//{{{ refreshModeName() method
	/**
	 * Update the mode name from the mode combo box
	 */
	private void refreshModeName()
	{
		Object selection = modeCombo.getSelectedItem();
		
		if (selection instanceof Mode)
		{
			modeName = ((Mode)selection).getName();
		}
		else
		{
			// The "default" combo box option will fall through here because
			// it is a String
			modeName = null;
		}
	} //}}}

	//{{{ addButtonPanel() method
	/**
	 * Add a panel of buttons for adding/copying/deleting style sets for the current mode
	 */
	private void addButtonPanel(ActionHandler actionHandler)
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 0));
		
		String newLabel = jEdit.getProperty(PREFIX + "buttons.new");
		buttonPanel.add(newButton = new JButton(newLabel));
		newButton.addActionListener(actionHandler);
		
		String copyLabel = jEdit.getProperty(PREFIX + "buttons.copy");
		buttonPanel.add(copyButton = new JButton(copyLabel));
		copyButton.addActionListener(actionHandler);
		
		String deleteLabel = jEdit.getProperty(PREFIX + "buttons.delete");
		buttonPanel.add(deleteButton = new JButton(deleteLabel));
		deleteButton.addActionListener(actionHandler);
		
		addComponent(buttonPanel, GridBagConstraints.HORIZONTAL);
	} //}}}
	
	//{{{ selectMode() method
	private void selectMode(String modeName)
	{	
		for (int i = 0; i < modeCombo.getItemCount(); i++)
		{
			Object item = modeCombo.getItemAt(i);
			if (item instanceof Mode)
			{
				Mode mode = (Mode)item;
				if (mode.getName().equals(modeName))
				{
					modeCombo.setSelectedIndex(i);
					return;
				}
			}
			else
			{
				// The "<global>" mode choice is a String, so it will fall through
				// here
				if (modeName == null)
				{
					modeCombo.setSelectedIndex(i);
					return;
				}
			}
		}
		
		Log.log(Log.ERROR, this, "Selecting non-existent mode");
	} //}}}
	
	//{{{ selectStyleSet() method
	private void selectStyleSet(String mode, String name)
	{
		// As soon as the mode actually gets changed, in the action listener, this
		// will be selected as the current style set
		initialStyleSet = name;
		selectMode(mode);
	} //}}}
	
	//{{{ copyStyleSet() method
	private void copyStyleSet(String mode, String name)
	{
		String fromMode = modeName;
		String fromName = (String)styleSetCombo.getSelectedItem();
		
		// Save and copy the current style set
		styleModel.save();
		StyleSet copy = StyleSettings.loadStyleSet(fromMode, fromName);
		copy.setModeName(mode);
		copy.setName(name);
		
		StyleSettings.createStyleSet(mode, name, copy);
		selectStyleSet(mode, name);
	} //}}}
	
	//{{{ validate() method
	/**
	 * Determines whether a new style set with the given name can be created in the given mode
	 * Shows an error dialog and returns null if the name is invalid or if another style set by
	 * that name already exists. Otherwise, returns the name, with the correct "global/" prepended if
	 * the global mode is currently selected.
	 */
	private String validate(String mode, String name)
	{
		// Chop off the global/ if it is already in the name
		if (mode == null && name.indexOf("global/") == 0)
		{
			name = name.substring(7);
		}
		
		// Make sure there are no illegal characters in the name
		if (name.indexOf('/') != -1 || name.indexOf(',') != -1)
		{
			String errMessage = jEdit.getProperty(PREFIX + "name-error");
			JOptionPane.showMessageDialog(this, errMessage,
				jEdit.getProperty("common.error"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		if (mode == null)
		{
			name = "global/" + name;
		}
		
		String[] styleSets = StyleSettings.loadStyleSetNames(mode);
		for (String styleSet : styleSets)
		{
			if (styleSet.equals(name))
			{
				String errMessage = jEdit.getProperty(PREFIX + "exists-error");
				JOptionPane.showMessageDialog(this, errMessage,
					jEdit.getProperty("common.error"), JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		
		return name;				
	} //}}}
	
	//{{{ Inner classes
	
	//{{{ MouseHandler class
	private class MouseHandler extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent evt)
		{
			// When no style set is selected, the table is disabled for editing
			if (!styleTable.isEnabled())
				return;
			
			int row = styleTable.rowAtPoint(evt.getPoint());
			if(row == -1)
				return;

			SyntaxStyle style;
			SyntaxStyle current = (SyntaxStyle)styleModel.getValueAt(row,1);
			String token = (String) styleModel.getValueAt(row, 0);
			JDialog dialog = GUIUtilities.getParentDialog(ModeHighlightingOptionPane.this);
			if (dialog != null)
				style = new StyleEditor(dialog, current, token).getStyle();
			else
			{
				View view = GUIUtilities.getView(ModeHighlightingOptionPane.this);
				style = new StyleEditor(view, current, token).getStyle();
			}
			if(style != null)
				styleModel.setValueAt(style,row,1);
		}
	} //}}}
	
	//{{{ ActionHandler class
	private class ActionHandler implements ActionListener
	{	
		@Override
		public void actionPerformed(ActionEvent event)
		{
			if (!initialized)
				return;
			
			if (event.getSource() == modeCombo)
			{
				_save();
				
				// We update the mode name after _save() so that the previous-selected mode
				// is the one that gets saved
				refreshModeName();
				
				refreshStyleSets(initialStyleSet);
				refreshActiveStyleSets();		
			}
			else if (event.getSource() == styleSetCombo)
			{
				if (styleModel != null)
					styleModel.save();
				
				refreshStyleBox();
			}
			else if (event.getSource() == newButton)
			{
				JDialog dialog = GUIUtilities.getParentDialog(ModeHighlightingOptionPane.this);
				String newName = new NewStyleSetDialog(dialog).getName();
				
				// Create the new style set and write it to the properties
				if (newName != null)
				{	
					styleModel.save();
					
					SyntaxStyle defaultStyle = StyleSettings.loadDefaultStyle();
					StyleSet blankStyleSet = new StyleSet(modeName, newName, defaultStyle);
					StyleSettings.createStyleSet(modeName, newName, blankStyleSet);
					selectStyleSet(modeName, newName);
				}
			}
			else if (event.getSource() == deleteButton)
			{				
				JDialog dialog = GUIUtilities.getParentDialog(ModeHighlightingOptionPane.this);
				String name = (String)styleSetCombo.getSelectedItem();
				
				if (modeName == null &&
					StyleSettings.getActiveStyleSetName(null).equals(name))
				{
					String errMessage = 
						jEdit.getProperty(PREFIX + "active-global-error");
						
					JOptionPane.showMessageDialog(dialog,
						errMessage,
						jEdit.getProperty("common.error"),
						JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String message = jEdit.getProperty(PREFIX + "confirm-delete");
				String title = jEdit.getProperty(PREFIX + "confirm-delete-title");
					
				int choice = JOptionPane.showConfirmDialog(dialog,
					message,
					title,
					JOptionPane.YES_NO_OPTION);
				if (choice == JOptionPane.YES_OPTION)
				{
					StyleSettings.deleteStyleSet(modeName, name);
					
					// Null the style model pointing to the now non-existent style set, so that it doesn't get
					// saved automatically when the style set combo gets reset
					styleModel = null;
		
					refreshStyleSets(null);
					refreshActiveStyleSets();
				}
			}
			else if (event.getSource() == copyButton)
			{
				JDialog dialog = GUIUtilities.getParentDialog(ModeHighlightingOptionPane.this);
				
				String oldName = (String)styleSetCombo.getSelectedItem();
				if (oldName.indexOf("global/") == 0)
				{
					oldName = oldName.substring(7);
				}
				
				CopyStyleSetDialog copyDialog = new CopyStyleSetDialog(dialog, oldName);
				
				String mode = copyDialog.getMode();
				String name = copyDialog.getName();
				if (name != null)
				{
					copyStyleSet(mode, name);
				}
			}
		}
	} //}}}

	//{{{ StyleTableModel class
	private static class StyleTableModel extends AbstractTableModel
	{
		private final List<StyleChoice> styleChoices;
		private final StyleSet styleSet;

		//{{{ StyleTableModel constructor
		StyleTableModel(StyleSet styleSet)
		{
			this.styleSet = styleSet;
			SyntaxStyle[] styles = styleSet.getTokenStyles();
			SyntaxStyle[] foldLineStyles = styleSet.getFoldLineStyles();
			
			styleChoices = new Vector<StyleChoice>(Token.ID_COUNT);
			
			// start at 1 not 0 to skip Token.NULL
			for(int i = 1; i < Token.ID_COUNT; i++)
			{
				String tokenName = Token.tokenToString((byte)i);
				styleChoices.add(new StyleChoice(tokenName, -1, styles[i]));
			}
			
			for (int i = 0; i <= 3; i++)
			{
				String label = jEdit.getProperty(PREFIX + "foldLine." + i);
				styleChoices.add(new StyleChoice(label, i, foldLineStyles[i]));
			}

			Collections.sort(styleChoices, new StandardUtilities.StringCompare<StyleChoice>(true));
		} //}}}

		//{{{ getColumnCount() method
		public int getColumnCount()
		{
			return 2;
		} //}}}

		//{{{ getRowCount() method
		public int getRowCount()
		{
			return styleChoices.size();
		} //}}}

		//{{{ getValueAt() method
		public Object getValueAt(int row, int col)
		{
			StyleChoice ch = styleChoices.get(row);
			switch(col)
			{
				case 0:
					return ch.label;
				case 1:
					return ch.style;
				default:
					return null;
			}
		} //}}}

		//{{{ setValueAt() method
		@Override
		public void setValueAt(Object value, int row, int col)
		{
			StyleChoice ch = styleChoices.get(row);
			if(col == 1)
				ch.style = (SyntaxStyle)value;
			
			fireTableRowsUpdated(row, row);
		} //}}}

		//{{{ getColumnName() method
		@Override
		public String getColumnName(int index)
		{
			switch(index)
			{
				case 0:
					return jEdit.getProperty(PREFIX + "object");
				case 1:
					return jEdit.getProperty(PREFIX + "style");
				case 2:
					return "Default?";
				default:
					return null;
			}
		} //}}}

		//{{{ save() method
		public void save()
		{
			if (styleSet.getName() == null)
			{
				// No actual style set is selected, so don't save
				return;
			}
			
			for (StyleChoice ch : styleChoices)
			{	
				String propertyName;
				if (ch.level == -1)
				{
					StyleSettings.setTokenStyle(
						styleSet.getModeName(),
						styleSet.getName(),
						Token.stringToToken(ch.label),
						ch.style);
				}
				else
				{
					StyleSettings.setFoldLineStyle(
						styleSet.getModeName(),
						styleSet.getName(),
						ch.level,
						ch.style);
				}
			}
		} //}}}
		
		//{{{ StyleChoice class
		private static class StyleChoice
		{
			private String label;
			private int level;
			private SyntaxStyle style;

			StyleChoice(String label, int level, SyntaxStyle style)
			{
				this.label = label;
				this.level = level;
				this.style = style;
			}

			// for sorting
			@Override
			public String toString()
			{
				return label;
			}
		} //}}}

		//{{{ StyleRenderer class
		static class StyleRenderer extends JLabel
			implements TableCellRenderer
		{
			//{{{ StyleRenderer constructor
			StyleRenderer()
			{
				setOpaque(true);
				setBorder(ModeHighlightingOptionPane.noFocusBorder);
				setText("Hello World");
			} //}}}

			//{{{ getTableCellRendererComponent() method
			public Component getTableCellRendererComponent(
				JTable table,
				Object value,
				boolean isSelected,
				boolean cellHasFocus,
				int row,
				int col)
			{
				if (value != null)
				{
					SyntaxStyle style = (SyntaxStyle)value;
					setForeground(style.getForegroundColor());
					if (style.getBackgroundColor() != null)
						setBackground(style.getBackgroundColor());
					else
					{
						// this part sucks
						setBackground(jEdit.getColorProperty(
							"view.bgColor"));
					}
					
					Font font = new Font("Monospaced", style.getFont().getStyle(), 12);
					setFont(font);
				}

				setBorder(cellHasFocus ? UIManager.getBorder(
					"Table.focusCellHighlightBorder")
				                       : ModeHighlightingOptionPane.noFocusBorder);
				return this;
			} //}}}
		} //}}}
	} //}}}

	//{{{ ActiveStyleSetChoice class
	/** Represents a choice in the "active style set" combo box **/
	private class ActiveStyleSetChoice
	{
		ActiveStyleSetChoice(String label, String name)
		{
			this.label = label;
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return label;
		}
		
		public String getName()
		{
			return name;
		}
		
		private String label;
		private String name;
	} //}}}

	//{{{ NewStyleSetDialog
	private class NewStyleSetDialog extends EnhancedDialog implements ActionListener
	{
		//{{{ NewStyleSetDialog constructor
		public NewStyleSetDialog(JDialog parent)
		{
			super(parent, jEdit.getProperty(PREFIX + "dialogs.new-style-set.title"), true);

			JPanel content = new JPanel(new GridBagLayout());
			content.setBorder(new EmptyBorder(12, 12, 12, 12));
			setContentPane(content);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(12, 12, 12, 12);
			
			constraints.gridx = 0;
			constraints.gridy = 0;
			String labelString =
				jEdit.getProperty(PREFIX + "dialogs.new-style-set.style-set-name");
			content.add(new JLabel(labelString), constraints);
			
			textField = new JTextField();
			Dimension d = textField.getPreferredSize();
			d.width = 200;
			textField.setPreferredSize(d);
			
			constraints.gridx = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			content.add(textField, constraints);
			
			JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.gridwidth = 2;
			content.add(buttonPanel, constraints);
			
			buttonPanel.add(okButton = new JButton(jEdit.getProperty("common.ok")));
			okButton.addActionListener(this);
			buttonPanel.add(cancelButton = new JButton(jEdit.getProperty("common.cancel")));
			cancelButton.addActionListener(this);
			
			pack();
			setLocationRelativeTo(parent);
			
			setResizable(false);
			setVisible(true);
		} //}}}
		
		//{{{ actionPerformed() method
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			
			if (source == okButton)
			{
				ok();
			}
			else if (source == cancelButton)
			{
				cancel();
			}
		} //}}}
		
		//{{{ ok() method
		public void ok()
		{
			String name = ModeHighlightingOptionPane.this.
				validate(modeName, textField.getText());
			if (name != null)
			{
				newName = name;
				dispose();
			}
		} //}}}
		
		//{{{ cancel() method
		public void cancel()
		{	
			dispose();
		} //}}}
		
		//{{{ getName() method
		public String getName()
		{
			return newName;
		} //}}}
		
		//{{{ Private members
		private String newName; 
		private JTextField textField;
		private JButton okButton;
		private JButton cancelButton;
		//}}}
	} //}}}

	//{{{ CopyStyleSetDialog
	private class CopyStyleSetDialog extends EnhancedDialog implements ActionListener
	{
		//{{{ CopyStyleSetDialog constructor
		public CopyStyleSetDialog(JDialog parent, String oldName)
		{
			super(parent, "Copy Style Set", true);

			JPanel content = new JPanel(new GridBagLayout());
			content.setBorder(new EmptyBorder(12, 12, 12, 12));
			setContentPane(content);
			
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(12, 12, 12, 12);
			
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.EAST;
			content.add(new JLabel("Copy to mode:"), constraints);
			
			constraints.gridx = 1;
			constraints.gridy = 0;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			modeCombo = buildModeCombo(this);
			content.add(modeCombo, constraints);
			
			constraints.gridx = 0;
			constraints.gridy = 1;
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.EAST;
			content.add(new JLabel("New style set name: "), constraints);
			
			constraints.gridx = 1;
			constraints.gridy = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.anchor = GridBagConstraints.CENTER;
			content.add(textField = new JTextField(oldName), constraints);
			Dimension d = textField.getPreferredSize();
			d.width = 200;
			textField.setPreferredSize(d);
			
			JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
			constraints.gridx = 0;
			constraints.gridy = 2;
			constraints.gridwidth = 2;
			content.add(buttonPanel, constraints);
			
			buttonPanel.add(okButton = new JButton(jEdit.getProperty("common.ok")));
			okButton.addActionListener(this);
			buttonPanel.add(cancelButton = new JButton(jEdit.getProperty("common.cancel")));
			cancelButton.addActionListener(this);
			
			pack();
			setLocationRelativeTo(parent);
			
			setResizable(false);
			setVisible(true);
		} //}}}
		
		//{{{ actionPerformed() method
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			
			if (source == okButton)
			{
				ok();
			}
			else if (source == cancelButton)
			{
				cancel();
			}
		} //}}}
		
		//{{{ ok() method
		public void ok()
		{
			// Set the name and mode fields to be accessed by the caller
			
			Object selection = modeCombo.getSelectedItem();
			if (selection instanceof Mode)
			{
				mode = ((Mode)selection).getName();
			}
			else
			{
				// The "default" combo box option will fall through here because
				// it is a String
				mode = null;
			}
			
			name = ModeHighlightingOptionPane.this.validate(mode, textField.getText());
			if (name != null)
			{	
				dispose();
			}
		} //}}}
		
		//{{{ cancel() method
		public void cancel()
		{	
			dispose();
		} //}}}
		
		//{{{ Accessor methods
		public String getMode()
		{
			return mode;
		}
		
		public String getName()
		{
			return name;
		}
		//}}}
		
		//{{{ Private members
		private JTextField textField;
		private JComboBox modeCombo;
		private JButton okButton;
		private JButton cancelButton;
		
		private String mode;
		private String name;
		//}}}
	}
	//}}}
	
	//}}}	

	//}}}
}

