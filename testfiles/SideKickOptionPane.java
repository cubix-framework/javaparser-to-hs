/*
 * SideKickOptionPane.java - SideKick options panel
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2003 Slava Pestov
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

package sidekick;

//{{{ Imports
import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.event.*;
import java.awt.*;
import java.util.Hashtable;
import org.gjt.sp.jedit.*;
//}}}

public class SideKickOptionPane extends AbstractOptionPane
{
	public static final String SHOW_TOOL_BAR = "sidekick.showToolBar";
	public static final String SPLIT_COMBO = "sidekick.splitCombo";
	public static final String SINGLE_ICON_IN_COMBO = "sidekick.singleIconInCombo";

	//{{{ SideKickOptionPane constructor
	public SideKickOptionPane()
	{
		super("sidekick");
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		JPanel toolbarPanel = new JPanel(new GridLayout(0, 1));
		toolbarPanel.setBorder(new TitledBorder(jEdit.getProperty(
			"options.sidekick.toolBar.label")));
		addComponent(toolbarPanel);
		showToolBar = new JCheckBox(jEdit.getProperty(
			"options.sidekick.showToolBar.label"));
		showToolBar.setSelected(jEdit.getBooleanProperty(SHOW_TOOL_BAR));
		toolbarPanel.add(showToolBar);
		splitCombo = new JCheckBox(jEdit.getProperty(
			"options.sidekick.splitCombo.label"));
		splitCombo.setSelected(jEdit.getBooleanProperty(SPLIT_COMBO));
		toolbarPanel.add(splitCombo);
		singleIconInCombo = new JCheckBox(jEdit.getProperty(
			"options.sidekick.singleIconInCombo.label"));
		singleIconInCombo.setSelected(jEdit.getBooleanProperty(SINGLE_ICON_IN_COMBO));
		toolbarPanel.add(singleIconInCombo);
		addComponent(showToolTips = new JCheckBox(jEdit.getProperty(
			"options.sidekick.showToolTips.label")));
		showToolTips.setSelected(jEdit.getBooleanProperty(
			"sidekick.showToolTips"));
		addComponent(showStatusWindow = new JCheckBox(jEdit.getProperty(
			"options.sidekick.showStatusWindow.label")));
		showStatusWindow.setSelected(jEdit.getBooleanProperty("sidekick.showStatusWindow"));
		addComponent(treeFollowsCaret = new JCheckBox(jEdit.getProperty(
			"options.sidekick.tree-follows-caret")));
		treeFollowsCaret.setSelected(SideKick.isGlobalFollowCaret());
		
		treeFollowsCaret.addActionListener(new ActionHandler());

		addComponent(scrollToVisible = new JCheckBox(jEdit.getProperty(
			"options.sidekick.scrollToVisible.label")));
		scrollToVisible.setSelected(jEdit.getBooleanProperty(
			"sidekick.scrollToVisible"));
		
		// checkbox to show/hide filter, indent next 2 checkboxes
		addComponent(showFilter = 
			     new JCheckBox(jEdit.getProperty("options.sidekick.showFilter.label", "Show filter text box")));
		showFilter.setSelected(jEdit.getBooleanProperty(SideKick.SHOW_FILTER, true));
		
		JPanel filterOptionPanel = new JPanel(new GridLayout(2, 1));
		filterOptionPanel.setBorder(BorderFactory.createEmptyBorder(0, 21, 0, 0));
		filterOptionPanel.add(persistentFilter = 
			     new JCheckBox(jEdit.getProperty("options.sidekick.persistentFilter.label")));
		persistentFilter.setSelected(jEdit.getBooleanProperty("sidekick.persistentFilter"));
		filterOptionPanel.add(filterVisibleAssets = 
			     new JCheckBox(jEdit.getProperty("options.sidekick.filter-visible-assets.label")));
		filterVisibleAssets.setSelected(jEdit.getBooleanProperty("sidekick.filter-visible-assets"));
		addComponent(filterOptionPanel);
		
		showFilter.addActionListener( 
			new ActionListener() {
				public void actionPerformed( ActionEvent ae ) {
					persistentFilter.setEnabled(showFilter.isSelected());
					filterVisibleAssets.setEnabled(showFilter.isSelected());
				}
			}
		);
		
		addComponent(jEdit.getProperty("options.sidekick.auto-expand-tree-depth"),
			autoExpandTreeDepth = new JComboBox());
		autoExpandTreeDepth.addActionListener(new ActionHandler());
		autoExpandTreeDepth.addItem(jEdit.getProperty("options.sidekick.all", "All"));
		for (int i = 0; i <= 10; i++)
			autoExpandTreeDepth.addItem(String.valueOf(i));
		String depth = String.valueOf(jEdit.getIntegerProperty(
			"sidekick-tree.auto-expand-tree-depth", 1));
		autoExpandTreeDepth.setSelectedItem(depth);



		JPanel parsingSettings = new JPanel();
		parsingSettings.setBorder(new TitledBorder("Auto parsing Settings"));
		parsingSettings.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		parsingSettings.add(buttonPanel, BorderLayout.NORTH);
		
		buttonPanel.add(bufferChangeParse = new JCheckBox(jEdit.getProperty(
			"options.sidekick.buffer-change-parse")));
		bufferChangeParse.setSelected(SideKick.isParseOnChange());
		bufferChangeParse.addActionListener(new ActionHandler());

		buttonPanel.add(bufferSaveParse = new JCheckBox(jEdit.getProperty(
			"options.sidekick.buffer-save-parse")));
		bufferSaveParse.setSelected(SideKick.isParseOnSave());
		bufferSaveParse.addActionListener(new ActionHandler());
		
		buttonPanel.add(keystrokeParse = new JCheckBox(jEdit.getProperty(
			"options.sidekick.keystroke-parse")));
		keystrokeParse.setSelected(jEdit.getBooleanProperty(
			"buffer.sidekick.keystroke-parse"));
		keystrokeParse.addActionListener(new ActionHandler());

		int autoParseDelayValue;
		try
		{
			autoParseDelayValue = Integer.parseInt(jEdit.getProperty("sidekick.auto-parse-delay"));
		}
		catch(NumberFormatException nf)
		{
			autoParseDelayValue = 1500;
		}

		parsingSettings.add(new JLabel(jEdit.getProperty("options.sidekick.auto-parse-delay")), BorderLayout.CENTER);
		parsingSettings.add(autoParseDelay = new JSlider(200,3000,autoParseDelayValue), BorderLayout.SOUTH);
		addComponent(parsingSettings);
		Hashtable labelTable = new Hashtable();
		for(int i = 500; i <= 3000; i += 500)
		{
			labelTable.put(new Integer(i),new JLabel(
				String.valueOf((double)i / 1000.0)));
		}
		autoParseDelay.setLabelTable(labelTable);
		autoParseDelay.setPaintLabels(true);
		autoParseDelay.setMajorTickSpacing(500);
		autoParseDelay.setPaintTicks(true);

		autoParseDelay.setEnabled(keystrokeParse.isSelected());

		
		JPanel codeCompletionsPanel = new JPanel();
		codeCompletionsPanel.setBorder(new TitledBorder(jEdit.getProperty(
			"options.sidekick.code-completion.label")));
		codeCompletionsPanel.setLayout(new BorderLayout()); 
		
		// addSeparator("options.sidekick.code-completion.label");
		JPanel completionsCheckboxes = new JPanel();
		completionsCheckboxes.setLayout(new FlowLayout());
		completionsCheckboxes.add(completeInstantToggle = new JCheckBox(jEdit.getProperty(
			"options.sidekick.complete-instant.toggle")));
		completeInstantToggle.setSelected(jEdit.getBooleanProperty("sidekick.complete-instant.toggle"));
		completeInstantToggle.addActionListener(new ActionHandler());

		completionsCheckboxes.add(completeDelayToggle = new JCheckBox(jEdit.getProperty(
			"options.sidekick.complete-delay.toggle")));
		completeDelayToggle.setSelected(jEdit.getBooleanProperty("sidekick.complete-delay.toggle"));
		completeDelayToggle.addActionListener(new ActionHandler());
		codeCompletionsPanel.add(completionsCheckboxes, BorderLayout.NORTH);
		
		int completeDelayValue = jEdit.getIntegerProperty("sidekick.complete-delay",500);

		codeCompletionsPanel.add(new JLabel(jEdit.getProperty("options.sidekick.complete-delay")), BorderLayout.CENTER);
		codeCompletionsPanel.add(completeDelay = new JSlider(0,1500,completeDelayValue), BorderLayout.SOUTH);
		addComponent(codeCompletionsPanel);

		autoCompletePopupGetFocus = new JCheckBox(
			jEdit.getProperty("options.sidekick.auto-complete-popup-get-focus"),
			jEdit.getBooleanProperty("sidekick.auto-complete-popup-get-focus"));
		addComponent(autoCompletePopupGetFocus);

		labelTable = new Hashtable();
		for(int i = 0; i <= 1500; i += 250)
		{
			labelTable.put(new Integer(i),new JLabel(
				String.valueOf((double)i / 1000.0)));
		}
		completeDelay.setLabelTable(labelTable);
		completeDelay.setPaintLabels(true);
		completeDelay.setMajorTickSpacing(250);
		completeDelay.setPaintTicks(true);

		completeDelay.setEnabled(completeDelayToggle.isSelected());
		
		addComponent(jEdit.getProperty("options.sidekick.complete-popup.accept-characters"),
			acceptChars = new JTextField(
				jEdit.getProperty("sidekick.complete-popup.accept-characters")));

		addComponent(jEdit.getProperty("options.sidekick.complete-popup.insert-characters"),
			insertChars = new JTextField(
				jEdit.getProperty("sidekick.complete-popup.insert-characters")));
		
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		SideKick.setParseOnSave(bufferSaveParse.isSelected());
		SideKick.setParseOnChange(bufferChangeParse.isSelected());
		jEdit.setBooleanProperty("buffer.sidekick.keystroke-parse",
			keystrokeParse.isSelected());
		jEdit.setProperty("sidekick.auto-parse-delay",String.valueOf(
			autoParseDelay.getValue()));
		SideKick.setGlobalFollowCaret(treeFollowsCaret.isSelected());
		jEdit.setBooleanProperty(SHOW_TOOL_BAR, showToolBar.isSelected());
		jEdit.setBooleanProperty(SPLIT_COMBO, splitCombo.isSelected());
		jEdit.setBooleanProperty(SINGLE_ICON_IN_COMBO, singleIconInCombo.isSelected());
		jEdit.setBooleanProperty("sidekick.showToolTips", showToolTips.isSelected());
		jEdit.setBooleanProperty("sidekick.showStatusWindow", showStatusWindow.isSelected());
		jEdit.setBooleanProperty("sidekick.scrollToVisible", scrollToVisible.isSelected());
		jEdit.setBooleanProperty(SideKick.SHOW_FILTER, showFilter.isSelected());
		jEdit.setBooleanProperty("sidekick.persistentFilter", persistentFilter.isSelected());
		jEdit.setBooleanProperty("sidekick.filter-visible-assets", filterVisibleAssets.isSelected());
		int depth = 0;
		String value = (String)autoExpandTreeDepth.getSelectedItem();
		depth = value.equals(jEdit.getProperty("options.sidekick.all", "All")) ? -1 : Integer.parseInt(value);
		jEdit.setIntegerProperty("sidekick-tree.auto-expand-tree-depth",
			depth);
		jEdit.setBooleanProperty("sidekick.complete-instant.toggle",
			completeInstantToggle.isSelected());
		jEdit.setBooleanProperty("sidekick.complete-delay.toggle",
			completeDelayToggle.isSelected());
		jEdit.setIntegerProperty("sidekick.complete-delay",
			completeDelay.getValue());
		jEdit.setBooleanProperty("sidekick.auto-complete-popup-get-focus",
			autoCompletePopupGetFocus.isSelected());
		jEdit.setProperty("sidekick.complete-popup.accept-characters",acceptChars.getText());
		jEdit.setProperty("sidekick.complete-popup.insert-characters",insertChars.getText());
	} //}}}

	//{{{ Private members
	private JCheckBox bufferChangeParse;
	private JCheckBox bufferSaveParse;
	private JCheckBox keystrokeParse;
	private JSlider autoParseDelay;
	private JCheckBox treeFollowsCaret;
	private JCheckBox scrollToVisible;
	private JComboBox autoExpandTreeDepth;
	private JCheckBox completeInstantToggle;
	private JCheckBox completeDelayToggle;
	private JSlider completeDelay;
	private JCheckBox autoCompletePopupGetFocus;
	private JCheckBox showToolBar;
	private JCheckBox splitCombo;
	private JCheckBox singleIconInCombo;
	private JCheckBox showToolTips;
	private JCheckBox showStatusWindow;
	private JTextField acceptChars;
	private JTextField insertChars;
	private JCheckBox persistentFilter;
	private JCheckBox showFilter;
	private JCheckBox filterVisibleAssets;
	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == keystrokeParse)
			{
				autoParseDelay.setEnabled(keystrokeParse.isSelected());
				if(keystrokeParse.isSelected())
					bufferChangeParse.setSelected(true);
					bufferSaveParse.setSelected(true);
			}
			else if(source == completeDelayToggle)
			{
				completeDelay.setEnabled(completeDelayToggle.isSelected());
			}
		}
	} //}}}
}
