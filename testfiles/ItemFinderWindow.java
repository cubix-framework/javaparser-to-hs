/*
 * ItemFinderWindow.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2010 Matthieu Casanova
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
package gatchan.jedit.rfcreader;

import org.gjt.sp.jedit.GUIUtilities;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Matthieu Casanova
 */
public class ItemFinderWindow<E> extends JFrame
{
	/**
	 * This window will contains the scroll with the items.
	 */
	private final JWindow window;
	private final JTextField searchField;
	private final JList itemList;

	protected final RequestFocusWorker requestFocusWorker;
	private final ItemFinder<E> itemFinder;

	public ItemFinderWindow(ItemFinder<E> itemFinder)
	{
		this.itemFinder = itemFinder;
		setUndecorated(true);
		window = new JWindow(this);
		searchField = new JTextField();

		itemList = new JList(itemFinder.getModel());
		itemList.setBorder(BorderFactory.createEtchedBorder());
		if (itemFinder.getListCellRenderer() != null)
			itemList.setCellRenderer(itemFinder.getListCellRenderer());
		itemList.addKeyListener(new ItemListKeyAdapter(searchField));
		itemList.addMouseListener(new MyMouseAdapter());

		searchField.addKeyListener(new SearchFieldKeyAdapter());
		searchField.getDocument().addDocumentListener(new MyDocumentListener());
		JScrollPane scroll = new JScrollPane(itemList);
		window.setContentPane(scroll);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(itemFinder.getLabel()), BorderLayout.NORTH);
		panel.add(searchField, BorderLayout.CENTER);
		setContentPane(panel);
		window.pack();
		requestFocusWorker = new RequestFocusWorker(searchField);
		pack();
		if (itemFinder.getWidth() != -1)
			setSize(itemFinder.getWidth(), getHeight());
	}

	@Override
	public void setVisible(boolean b)
	{
		Rectangle bounds = getBounds();
		window.setLocation(bounds.x, bounds.y + bounds.height);
		GUIUtilities.requestFocus(this, searchField);
		window.setVisible(false);
		super.setVisible(b);
	}

	@Override
	public void dispose()
	{
		window.dispose();
		super.dispose();
	}

	private static boolean handledByList(KeyEvent e)
	{
		return e.getKeyCode() == KeyEvent.VK_DOWN ||
			e.getKeyCode() == KeyEvent.VK_UP ||
			e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ||
			e.getKeyCode() == KeyEvent.VK_PAGE_UP;
	}

	private void select()
	{
		E value = (E) itemList.getSelectedValue();
		if (value != null)
		{
			itemFinder.selectionMade(value);
			dispose();
		}
	}

	private class SearchFieldKeyAdapter extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if (handledByList(e))
			{
				itemList.dispatchEvent(e);
			}
			else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				dispose();
			}
			else if (e.getKeyCode() == KeyEvent.VK_ENTER)
			{
				select();
			}
		}
	}

	private static class ItemListKeyAdapter extends KeyAdapter
	{
		private final JTextField searchField;

		private ItemListKeyAdapter(JTextField searchField)
		{
			this.searchField = searchField;
		}

		@Override
		public void keyTyped(KeyEvent e)
		{
			searchField.dispatchEvent(e);
		}

		@Override
		public void keyPressed(KeyEvent e)
		{
			if (!handledByList(e))
			{
				searchField.dispatchEvent(e);
			}
		}
	}

	private class MyMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				select();
			}
		}
	}

	protected static class RequestFocusWorker implements Runnable
	{
		private final JTextField searchField;

		private RequestFocusWorker(JTextField searchField)
		{
			this.searchField = searchField;
		}

		public void run()
		{
			searchField.requestFocus();
		}
	}

	private class MyDocumentListener implements DocumentListener
	{
		public void insertUpdate(DocumentEvent e)
		{
			updateList(searchField.getText());
		}

		public void removeUpdate(DocumentEvent e)
		{
			updateList(searchField.getText());
		}

		public void changedUpdate(DocumentEvent e)
		{
			updateList(searchField.getText());
		}

		private void updateList(String s)
		{
			itemFinder.updateList(s);
			int size = itemList.getModel().getSize();
			if (size == 0)
			{
				itemList.clearSelection();
			}
			else
			{
				window.pack();
				window.setVisible(true);
				if (itemList.getSelectedIndex() == -1)
				{
					itemList.setSelectedIndex(0);
				}
			}
			EventQueue.invokeLater(requestFocusWorker);
		}
	}
}