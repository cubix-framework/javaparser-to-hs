/*
 * HighlightList.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2004, 2010 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package gatchan.highlight;

//{{{ Imports
import org.gjt.sp.jedit.*;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//}}}

/**
 * The dockable panel that will contains a list of all your highlights.
 *
 * @author Matthieu Casanova
 * @version $Id: HighlightList.java,v 1.17 2005/09/12 20:07:37 kpouer Exp $
 */
public final class HighlightList extends JPanel implements HighlightChangeListener
{
	private JPopupMenu popupMenu;
	private JMenuItem remove;

	private final JTable table;
	private final HighlightManagerTableModel tableModel;
	private HighlightList.RemoveAction removeAction;
	private final JCheckBox enableHighlights = new JCheckBox("enable");
	private JCheckBoxMenuItem permanentScope;
	private JCheckBoxMenuItem sessionScope;
	private JCheckBoxMenuItem bufferScope;
	private MyActionListener actionListener;

	//{{{ HighlightList constructor
	public HighlightList()
	{
		super(new BorderLayout());

		tableModel = HighlightManagerTableModel.getInstance();
		table = new JTable(tableModel);
		table.setDragEnabled(false);
		final HighlightCellRenderer renderer = new HighlightCellRenderer();
		table.setRowHeight(renderer.getPreferredSize().height);

		table.setDefaultRenderer(Highlight.class, renderer);

		final TableColumnModel columnModel = table.getColumnModel();

		columnModel.getColumn(2).setCellEditor(new ButtonCellEditor(tableModel));
		columnModel.getColumn(3).setCellEditor(new ButtonCellEditor(tableModel));

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		final TableColumn col1 = columnModel.getColumn(0);
		col1.setPreferredWidth(26);
		col1.setMinWidth(26);
		col1.setMaxWidth(26);
		col1.setResizable(false);

		final TableColumn col3 = columnModel.getColumn(2);
		col3.setPreferredWidth(26);
		col3.setMinWidth(26);
		col3.setMaxWidth(26);
		col3.setResizable(false);
		final TableColumn col4 = columnModel.getColumn(3);
		col4.setPreferredWidth(26);
		col4.setMinWidth(26);
		col4.setMaxWidth(26);
		col4.setResizable(false);

		table.setDefaultEditor(Highlight.class, new HighlightCellEditor());
		table.setDefaultEditor(Boolean.class, table.getDefaultEditor(Boolean.class));
		table.setTableHeader(null);

		table.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				final int row = table.rowAtPoint(e.getPoint());
				if (row == -1) return;
				if (GUIUtilities.isRightButton(e.getModifiers()))
				{
					showPopupMenu(e, row);
				}
			}
		});

		final JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		final JButton newButton = new JButton(GUIUtilities.loadIcon("New.png"));
		newButton.setToolTipText("Add an highlight");
		final JButton clear = new JButton(GUIUtilities.loadIcon("Clear.png"));
		clear.setToolTipText("Remove all highlights");
		enableHighlights.setSelected(true);
		enableHighlights.setToolTipText("Enable / disable highlights");
		actionListener = new MyActionListener(newButton, clear);
		newButton.addActionListener(actionListener);
		clear.addActionListener(actionListener);
		enableHighlights.addActionListener(actionListener);
		toolBar.add(newButton);
		toolBar.add(clear);
		toolBar.add(enableHighlights);
		add(toolBar, BorderLayout.NORTH);
		final JScrollPane scroll = new JScrollPane(table);
		add(scroll);
	} //}}}

	//{{{ showPopupMenu() method
	/**
	 * Show the popup menu of the highlight panel.
	 *
	 * @param e   the mouse event
	 * @param row the selected row
	 */
	private void showPopupMenu(MouseEvent e, int row)
	{
		if (popupMenu == null)
		{
			popupMenu = new JPopupMenu();
			removeAction = new RemoveAction(tableModel);
			remove = popupMenu.add(removeAction);
			permanentScope = new JCheckBoxMenuItem("permanent");
			sessionScope = new JCheckBoxMenuItem("session");
			bufferScope = new JCheckBoxMenuItem("buffer");
			popupMenu.add(permanentScope);
			popupMenu.add(sessionScope);
			popupMenu.add(bufferScope);
			permanentScope.addActionListener(actionListener);
			sessionScope.addActionListener(actionListener);
			bufferScope.addActionListener(actionListener);
		}
		Highlight highlight;
		try
		{
			tableModel.getReadLock();
			highlight = tableModel.getHighlight(row);
		}
		finally
		{
			tableModel.releaseLock();
		}
		actionListener.setHighlight(highlight, row);
		int scope = highlight.getScope();
		permanentScope.setSelected(scope == Highlight.PERMANENT_SCOPE);
		sessionScope.setSelected(scope == Highlight.SESSION_SCOPE);
		bufferScope.setSelected(scope == Highlight.BUFFER_SCOPE);

		remove.setEnabled(tableModel.getRowCount() > 0);
		removeAction.setRow(row);
		GUIUtilities.showPopupMenu(popupMenu, e.getComponent(), e.getX(), e.getY());
		e.consume();
	} //}}}

	//{{{ addNotify() method
	public void addNotify()
	{
		super.addNotify();
		HighlightManagerTableModel.getManager().addHighlightChangeListener(this);
	} //}}}

	//{{{ removeNotify() method
	public void removeNotify()
	{
		super.removeNotify();
		HighlightManager highlightManager = HighlightManagerTableModel.getManager();

		// if unloading plugin
		if (highlightManager != null)
			highlightManager.removeHighlightChangeListener(this);
	} //}}}

	//{{{ highlightUpdated() method
	public void highlightUpdated(boolean highlightEnable)
	{
		enableHighlights.setSelected(highlightEnable);
	} //}}}

	//{{{ RemoveAction class
	/**
	 * The remove action that will remove an highlight from the table.
	 *
	 * @author Matthieu Casanova
	 */
	private static final class RemoveAction extends AbstractAction
	{
		private int row;

		private final HighlightManagerTableModel tableModel;

		RemoveAction(HighlightManagerTableModel tableModel)
		{
			super("remove");
			this.tableModel = tableModel;
		}

		private void setRow(int row)
		{
			this.row = row;
		}

		public final void actionPerformed(ActionEvent e)
		{
			tableModel.removeRow(row);
		}
	} //}}}

	//{{{ MyActionListener class
	/**
	 * The actionListener that will handle buttons and checkbox of the HighlightList.
	 *
	 * @author Matthieu Casanova
	 */
	private final class MyActionListener implements ActionListener
	{
		private final JButton newButton;
		private final JButton clear;

		private Highlight highlight;
		private int row;

		MyActionListener(JButton newButton,
				 JButton clear)
		{
			this.newButton = newButton;
			this.clear = clear;
		}


		private void setHighlight(Highlight highlight, int row)
		{
			this.highlight = highlight;
			this.row = row;
		}

		public final void actionPerformed(ActionEvent e)
		{
			Object source = e.getSource();
			if (clear == source)
			{
				tableModel.removeAll();
			}
			else if (newButton == source)
			{
				View activeView = jEdit.getActiveView();
				HighlightPlugin.highlightDialog(activeView, activeView.getTextArea());
			}
			else if (enableHighlights == source)
			{
				tableModel.setHighlightEnable(enableHighlights.isSelected());
			}
			else if (source == permanentScope)
			{
				highlight.setScope(Highlight.PERMANENT_SCOPE);
				highlight.setBuffer(null);
				tableModel.fireTableRowsUpdated(row, row);
			}
			else if (source == sessionScope)
			{
				highlight.setScope(Highlight.SESSION_SCOPE);
				highlight.setBuffer(null);
				tableModel.fireTableRowsUpdated(row, row);
			}
			else if (source == bufferScope)
			{
				highlight.setScope(Highlight.BUFFER_SCOPE);
				highlight.setBuffer(jEdit.getActiveView().getBuffer());
				tableModel.fireTableRowsUpdated(row, row);
			}

		}
	} //}}}
}
