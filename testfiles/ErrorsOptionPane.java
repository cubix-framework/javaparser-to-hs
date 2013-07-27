/*
 * ErrorsOptionPane.java - Error pattern option pane
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003, 2005 Slava Pestov, Alan Ezust
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

package console.options;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;

import console.ErrorListModel;
import console.ErrorMatcher;
import console.gui.PanelStack;
//}}}

//{{{ ErrorsOptionPane class
/**
 * A view/editor for an ErrorListModel.
 * 
 * Shows a list of the current ErrorMatchers which can be used, and permits the easy
 * editing of them.
 */
public class ErrorsOptionPane extends AbstractOptionPane
{
	//{{{ Instance variables
	
	// Model for storing all of the ErrorMatchers
	private ErrorListModel errorListModel;

	// View for the list of errors
	private JList errorList;

	PanelStack panelStack;
	
	private JButton add;
	private JButton remove;
	private JButton reset;
	private JButton up;
	private JButton down;
	//}}}

	
	// {{{ Public members
	
	//{{{ ErrorsOptionPane constructor
	public ErrorsOptionPane()
	{
		super("console.errors");
	} //}}}
	
	// }}}
	
	//{{{ Protected members

	//{{{ _init() method
	protected void _init()
	{

		setLayout(new BorderLayout());
		addComponent(Box.createVerticalStrut(6));

		errorListModel = ErrorListModel.load();
		errorList = new JList();
		errorList.setModel(errorListModel);
		JScrollPane jsp =new JScrollPane(errorList); 
		jsp.setMinimumSize(new Dimension(125, 300));
		errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorList.addListSelectionListener(new ListHandler());
		errorList.addMouseListener(new MouseHandler());
		errorList.setVisibleRowCount(5);

		// JSplitPane errors = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		// errors.add(jsp);
		String title = jEdit.getProperty("options.console.errors.caption");
		jsp.setBorder(new TitledBorder(title));
		
		Box westBox = new Box(BoxLayout.Y_AXIS);
		westBox.add(jsp);
		// add(jsp, BorderLayout.WEST);

		
		panelStack = new PanelStack();
//		errors.add(panelStack);
		//add(errors, BorderLayout.CENTER);
		add(panelStack, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(6,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));

/*		buttons.add(add = new JButton(jEdit.getProperty(
			"options.console.errors.add")));
		buttons.add(remove = new JButton(jEdit.getProperty(
			"options.console.errors.remove"))); */
		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("common.add"));
		buttons.add(add);
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("common.remove"));
		buttons.add(remove);
		up = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
		up.setToolTipText(jEdit.getProperty("common.moveUp"));
		buttons.add(up);
		down= new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
		down.setToolTipText(jEdit.getProperty("common.moveDown"));
		buttons.add(down);
		reset = new RolloverButton(GUIUtilities.loadIcon("Reload.png"));
		reset.setToolTipText(jEdit.getProperty("options.console.errors.reload.tooltip"));
		buttons.add(reset);

	        ActionHandler handler = new ActionHandler();
	        add.addActionListener(handler);
		remove.addActionListener(handler);
		up.addActionListener(handler);
		down.addActionListener(handler);
		reset.addActionListener(handler);
		westBox.add(buttons);
//		add(buttons, BorderLayout.SOUTH);
		add(westBox, BorderLayout.WEST);
		errorList.setSelectedIndex(0);
		updateButtons();


	} //}}}

	protected void _load()
	{
		errorListModel = ErrorListModel.load();
	}
	
	//{{{ _save() method
	protected void _save()
	{
		errorListModel.save();
	} //}}}

	//}}}
	
	//{{{ Private members

	//{{{ updateButtons() method
	
	private void updateButtons()
	{
		ErrorMatcher matcher = (ErrorMatcher)errorList.getSelectedValue();
		if (matcher != null)
		{
			String internalName = matcher.internalName();
			if (!panelStack.raise(internalName)) {
				ErrorMatcherPanel panel = new ErrorMatcherPanel(internalName, matcher);
				
				panelStack.add(internalName, panel);
				panelStack.raise(internalName);
				validateTree();
			}
		}
	} //}}}

	//}}}

	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			
			if (source == reset) {
				errorListModel.reset();
				errorList.setModel(errorListModel);
				errorList.repaint();
			}
			
			if(source == add)
			{
				/* Open a dialog and ask for the name: */
				String matcherNamePrompt = jEdit.getProperty("options.console.errors.name");
				String matcherName = JOptionPane.showInputDialog(matcherNamePrompt);
				ErrorMatcher matcher = new ErrorMatcher(matcherName);
				matcher.name = matcherName;
				matcher.user = true;
				int index = errorList.getSelectedIndex() + 1;
				errorListModel.insertElementAt(matcher,index);
				errorList.setSelectedIndex(index);
				errorList.repaint();
			}
			else if(source == remove)
			{
				int index = errorList.getSelectedIndex();
				errorListModel.removeElementAt(index);
				if (errorListModel.size() > index)
					errorList.setSelectedIndex(index);
				else if (! errorListModel.isEmpty())
					errorList.setSelectedIndex(errorListModel.size() - 1);
				errorList.repaint();
			}
			
			else if(source == up)
			{
				int index = errorList.getSelectedIndex();
				ErrorMatcher selected = errorListModel.get(index);
				errorListModel.removeElementAt(index);
				errorListModel.insertElementAt(selected,index-1);
				errorList.setSelectedIndex(index-1);
				errorList.repaint();
			}
			else if(source == down)
			{
				int index = errorList.getSelectedIndex();
				ErrorMatcher matcher = errorListModel.get(index);
				errorListModel.removeElementAt(index);
				errorListModel.insertElementAt(matcher, index+1);
				errorList.setSelectedIndex(index+1);
				errorList.repaint();
			} 

		}
	} //}}}

	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateButtons();
		}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			if(evt.getClickCount() == 2)
			{
				updateButtons();
			}
		}
	} //}}}
} //}}}

