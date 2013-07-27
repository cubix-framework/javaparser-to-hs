/*
 * TagsPlugin.java
 * Copyright (c) 2001, 2002 Kenrick Drew (kdrew@earthlink.net)
 * Copyright (c) 2003, 2004 Ollie Rutherfurd (oliver@jedit.org)
 * Copyright (c) 2007 Shlomy Reinstein (shlomy@users.sourceforge.net)
 *
 * This file is part of the Tags plugin.
 *
 * TagsPlugin is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * TagsPlugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA	02111-1307, USA.
 *
 * $Id: TagsPlugin.java 10867 2007-10-10 13:01:00Z shlomy $
 */

package tags;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;

@SuppressWarnings("serial")
public class ChooseTagListDockable extends JPanel
	implements DefaultFocusComponent {

	private View view;
	private ChooseTagList chooseTagList = null;
	private JScrollPane scroller = null;
	private JMenu filterMenu = null; 
	private Vector<TagLine> origTagLines = null;
	
	public ChooseTagListDockable(View view) {
		super(new BorderLayout());
		this.view = view;
		JMenuBar menuBar = new JMenuBar();
		add(menuBar, BorderLayout.NORTH);
		filterMenu = new JMenu("Filter");
		menuBar.add(filterMenu);
		setTagLines(new Vector<TagLine>());
	}
	
	public void setTagLines(Vector<TagLine> tagLines) {
		origTagLines = tagLines;
		updateTagLines(tagLines);
		Map<String, HashSet<String>> attributes =
			new HashMap<String, HashSet<String>>();
		for (int i = 0; i < tagLines.size(); i++) {
			TagLine l = (TagLine) tagLines.get(i);
			Vector<ExuberantInfoItem> items = l.getExuberantInfoItems();
			for (int j = 0; j < items.size(); j++) {
				ExuberantInfoItem item = (ExuberantInfoItem) items.get(j);
				String [] parts = item.toString().split(":", 2);
				if (parts.length < 2)
					continue;
				HashSet<String> set = attributes.get(parts[0]);
				if (set == null) {
					set = new HashSet<String>();
					attributes.put(parts[0], set);
				}
				set.add(parts[1]);
			}
		}
		filterMenu.removeAll();
		Iterator<String> it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String att = it.next();
			HashSet<String> valueSet = attributes.get(att);
			if (valueSet.size() > 1 && valueSet.size() <= 20) {
				JMenu attrMenu = new JMenu(att);
				filterMenu.add(attrMenu);
				Iterator<String> valueIt = valueSet.iterator();
				while (valueIt.hasNext()) {
					String val = valueIt.next();
					JMenuItem valItem = new JMenuItem(val);
					attrMenu.add(valItem);
					valItem.addActionListener(new FilterHandler(att, val));
				}
			}
		}
		revalidate();
	}

	private void updateTagLines(Vector<TagLine> tagLines) {
		if (scroller != null)
			remove(scroller);
		chooseTagList = new ChooseTagList(tagLines);
		scroller = new JScrollPane(chooseTagList,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(scroller, BorderLayout.CENTER);
		chooseTagList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selected();
			}
		});
		chooseTagList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() >= '1' && e.getKeyChar() <= '9') {
					int selected = Character.getNumericValue(e.getKeyChar()) - 1;
					if (selected >= 0 && 
						selected < chooseTagList.getModel().getSize())
					{
						chooseTagList.setSelectedIndex(selected);
						selected();
						e.consume();
					}
				}
			}
		});
	}
	
	private void selected()
	{
		TagLine tagLine = (TagLine)chooseTagList.getSelectedValue();
		TagsPlugin.goToTagLine(view, tagLine, false, tagLine.getTag());
	}

	public void focusOnDefaultComponent() {
		if (chooseTagList != null)
			chooseTagList.requestFocus();
	}
	
	private class FilterHandler implements ActionListener {
		private String att;
		private String val;
		public FilterHandler(String attr, String value) {
			att = attr;
			val = value;
		}
		public void actionPerformed(ActionEvent e) {
			filter(att, val);
		}
	}

	public void filter(String att, String val) {
		AttributeValueFilter filter = new AttributeValueFilter(att, val);
		Vector<TagLine> tagLines = origTagLines;
		Vector<TagLine> filtered = new Vector<TagLine>();
		for (int i = 0; i < tagLines.size(); i++) {
			TagLine l = (TagLine) tagLines.get(i);
			if (filter.pass(l))
				filtered.add(l);
		}
		updateTagLines(filtered);
		revalidate();
	}
}
