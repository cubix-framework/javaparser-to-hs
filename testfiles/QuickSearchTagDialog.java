package ctagsinterface.main;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;

import ctagsinterface.db.Query;
import ctagsinterface.db.TagDB;
import ctagsinterface.options.ProjectsOptionPane;

@SuppressWarnings("serial")
public class QuickSearchTagDialog extends JDialog {

	enum Mode {
		SUBSTRING,
		PREFIX
	};
	Mode mode;
	JTextField name;
	JList tags;
	DefaultListModel model;
	View view;
	Vector<QuickSearchTag> tagNames;
	Query baseQuery;
	Timer filterTimer;
	
	/** This window will contains the scroll with the items. */
	private final JWindow window = new JWindow(this);
	
	public QuickSearchTagDialog(View view, Mode mode) {
		super(view, "Search tag", false);
		this.view = view;
		this.mode = mode;
		JPanel p = new JPanel();
		p.add(new JLabel("Type part of the tag name:"));
		name = new JTextField(30);
		p.add(name);
		add(p, BorderLayout.NORTH);
		model = new DefaultListModel();
		tags = new JList(model);
		tags.setBorder(BorderFactory.createEtchedBorder());
		tags.setCellRenderer(new TagListCellRenderer());
		window.setContentPane(new JScrollPane(tags));
		name.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				setFilter();
			}
			public void insertUpdate(DocumentEvent e) {
				setFilter();
			}
			public void removeUpdate(DocumentEvent e) {
				setFilter();
			}
		});
		name.addKeyListener(new KeyAdapter()
			{
				public void keyPressed(KeyEvent e) {
					if (handledByList(e)) {
						tags.dispatchEvent(e);
					} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
						setVisible(false);
					} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jumpToSelected();
					}
				}
			}
		);
		tags.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent arg0) {
				jumpToSelected();
			}
		});
		tags.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				name.dispatchEvent(e);
			}
				    
			public void keyPressed(KeyEvent e) {
				if (!handledByList(e)) {
					name.dispatchEvent(e);
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
					jumpToSelected();
				}
			}
		});
		filterTimer = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delayedSetFilter();
			}
		});
		filterTimer.setRepeats(false);
		prepareData();
		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}

	private void prepareData() {
		TagDB db = CtagsInterfacePlugin.getDB();
		Query q = new Query();
		q.setColumns(new Object [] {TagDB.TAGS_TABLE + ".*", TagDB.FILES_NAME});
		q.setTables(new Object [] {TagDB.TAGS_TABLE, TagDB.FILES_TABLE});
		q.addCondition(TagDB.TAGS_FILE_ID + "=" + TagDB.FILES_ID);
		
		if (ProjectsOptionPane.getSearchActiveProjectOnly()) {
			String project = CtagsInterfacePlugin.getProjectWatcher().getActiveProject(view);
			if (project != null) {
				Query projectQuery = new Query();
				projectQuery.setColumn(TagDB.MAP_FILE_ID);
				projectQuery.setTables(new Object [] {TagDB.MAP_TABLE, TagDB.ORIGINS_TABLE});
				projectQuery.addCondition(TagDB.MAP_TABLE + "." + TagDB.MAP_ORIGIN_ID +
					"=" + TagDB.ORIGINS_TABLE + "." + TagDB.ORIGINS_ID);
				projectQuery.addCondition(TagDB.ORIGINS_TABLE + "." + TagDB.ORIGINS_NAME +
					"=" + TagDB.quote(project));
				projectQuery.addCondition(TagDB.ORIGINS_TABLE + "." + TagDB.ORIGINS_TYPE +
						"=" + TagDB.quote(TagDB.PROJECT_ORIGIN));
				q.addCondition("EXISTS (" + projectQuery.toString() + ")");
			}
		}
		switch (mode) {
		case SUBSTRING:
			try {
				tagNames = new Vector<QuickSearchTag>();
				ResultSet rs = db.query(q);
				while (rs.next())
					tagNames.add(new QuickSearchTag(rs));
				rs.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			break;
		case PREFIX:
			baseQuery = q;
			break;
		}
	}

	protected void jumpToSelected() {
		QuickSearchTag t = (QuickSearchTag) tags.getSelectedValue();
		CtagsInterfacePlugin.jumpTo(view, t.file, t.line);
		dispose();
	}

	protected void setFilter() {
		if (filterTimer.isRunning())
			filterTimer.restart();
		else
			filterTimer.start();
	}
	
	protected void delayedSetFilter() {
		model.removeAllElements();
		String input = name.getText();
		if (! input.isEmpty()) {
			switch (mode) {
			case SUBSTRING:
				for (int i = 0; i < tagNames.size(); i++) {
					QuickSearchTag t = tagNames.get(i);
					if (t.name.contains(input))
						model.addElement(t);
				}
				break;
			case PREFIX:
				TagDB db = CtagsInterfacePlugin.getDB();
				Vector<Object> conditions = baseQuery.getConditions();
				Object prefixCondition = db.field(TagDB.TAGS_TABLE, TagDB.TAGS_NAME) +
					" LIKE " + TagDB.quote(input + "%"); 
				conditions.add(prefixCondition);
				baseQuery.setConditions(conditions);
				try {
					ResultSet rs = db.query(baseQuery);
					while (rs.next())
						model.addElement(new QuickSearchTag(rs));
					rs.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				conditions.remove(prefixCondition);
				baseQuery.setConditions(conditions);
				break;
			}
		}
		if (model.isEmpty())
		{
			window.setVisible(false);
		}
		else
		{
			tags.setVisibleRowCount(Math.min(10, model.size()));
			window.pack();
			window.setVisible(true);
		}
	}
	
	private static boolean handledByList(KeyEvent e) {
		return e.getKeyCode() == KeyEvent.VK_DOWN ||
		e.getKeyCode() == KeyEvent.VK_UP ||
		e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ||
		e.getKeyCode() == KeyEvent.VK_PAGE_UP;
	}
	
	public void setVisible(boolean b) {
		Rectangle bounds = getBounds();
		window.setLocation(bounds.x, bounds.y + bounds.height);
		GUIUtilities.requestFocus(this, name);
		window.setVisible(false);
		super.setVisible(b);
	}
	
	static private class QuickSearchTag {
		String file;
		int line;
		String name;
		String desc;
		String kind;
		public QuickSearchTag(ResultSet rs) {
			StringBuffer text = new StringBuffer();
			try {
				name = rs.getString(TagDB.TAGS_NAME);
				text.append(rs.getString(TagDB.TAGS_NAME));
				kind = rs.getString(TagDB.extension2column("kind"));
				if (kind != null)
					text.append(" (" + kind + ")");
				file = rs.getString(TagDB.FILES_NAME);
				String lineStr = rs.getString(TagDB.extension2column("line"));
				if (lineStr != null)
					line = Integer.valueOf(lineStr);
				else
					line = -1;
			} catch (SQLException e) {
				e.printStackTrace();
			}
			desc = text.toString();
			if (isValid())
				desc = desc + "   [" + file + ":" + line + "]";
		}
		public boolean isValid() {
			return (desc.length() > 0 && file != null && line >= 0);
		}
		public String toString() {
			return desc;
		}
		public ImageIcon getIcon() {
			return KindIconProvider.getIcon(kind);
		}
	}

	public class TagListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel) super.getListCellRendererComponent(
				list, value, index, isSelected, cellHasFocus);
			if (value instanceof QuickSearchTag) {
				ImageIcon icon = ((QuickSearchTag)value).getIcon();
				if (icon != null)
					l.setIcon(icon);
			}
			return l;
		}

	}

}
