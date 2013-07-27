/*
 * CatalogsOptionPane.java - Catalog manager options panel
 * Copyright (C) 2001, 2002 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml.options;

import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;

public class CatalogsOptionPane extends AbstractOptionPane
{
	public CatalogsOptionPane()
	{
		super("xml.catalogs");
	}

	// protected members
	protected void _init()
	{
		setLayout(new BorderLayout());

		JLabel label = new JLabel(jEdit.getProperty("options.xml.catalogs.caption"));
		label.setBorder(new EmptyBorder(0,0,6,0));
		add(BorderLayout.NORTH,label);

		catalogListModel = new DefaultListModel();
		int i = 0;
		String catalog;
		while((catalog = jEdit.getProperty("xml.catalog." + i)) != null)
		{
			catalogListModel.addElement(catalog);
			i++;
		}

		add(BorderLayout.CENTER,new JScrollPane(
			catalogList = new JList(catalogListModel)));
		catalogList.addListSelectionListener(new ListHandler());

		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.setBorder(new EmptyBorder(6,0,0,0));

		add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.setToolTipText(jEdit.getProperty("options.xml.catalogs.add"));
		add.addActionListener(new ActionHandler());
		buttons.add(add);
		remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.setToolTipText(jEdit.getProperty("options.xml.catalogs.remove"));
		remove.addActionListener(new ActionHandler());
		buttons.add(remove);
		buttons.add(Box.createGlue());

		add(BorderLayout.SOUTH,buttons);

		updateEnabled();
	}

	protected void _save()
	{
		int i;
		for(i = 0; i < catalogListModel.getSize(); i++)
		{
			jEdit.setProperty("xml.catalog." + i,
				(String)catalogListModel.getElementAt(i));
		}

		jEdit.unsetProperty("xml.catalog." + i);
	}

	// private members
	private JList catalogList;
	private DefaultListModel catalogListModel;
	private JButton add;
	private JButton remove;

	private void updateEnabled()
	{
		boolean selected = (catalogList.getSelectedValue() != null);
		remove.setEnabled(selected);
	}

	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == add)
			{
				String[] files = GUIUtilities.showVFSFileDialog(
					null,null,VFSBrowser.OPEN_DIALOG,true);
				if(files == null)
					return;

				for(int i = 0; i < files.length; i++)
				{
					catalogListModel.addElement(files[i]);
				}
			}
			else if(evt.getSource() == remove)
			{
				catalogListModel.removeElementAt(
					catalogList.getSelectedIndex());
				updateEnabled();
			}
		}
	}

	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateEnabled();
		}
	}
}
