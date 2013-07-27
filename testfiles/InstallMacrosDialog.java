/*
 *  InstallMacrosDialog.java - macro install dialog box
 *  :tabSize=8:indentSize=8:noTabs=false:
 *  :folding=explicit:collapseFolds=1:
 *
 *  Copyright (C) 2002 Carmine Lucarelli
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
package macroManager;
//{{{ Imports
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.Log;


//}}}

/**
 *  Description of the Class
 */
public class InstallMacrosDialog extends EnhancedDialog
{
	final static int INSTALL = 0;
	final static int UPDATE = 1;


	//{{{ InstallPluginsDialog constructor
	/*
	 *  InstallPluginsDialog(JDialog dialog, Vector model, int mode)
	 *  {
	 *  super(JOptionPane.getFrameForComponent(dialog),
	 *  (mode == INSTALL
	 *  ? jEdit.getProperty("install-macros.title")
	 *  : jEdit.getProperty("update-macros.title")),true);
	 */
	/**
	 *  Constructor for the InstallMacrosDialog object
	 *
	 *@param  frame Description of the Parameter
	 */
	public InstallMacrosDialog(Frame frame)
	{
		super(frame, jEdit.getProperty("install-macros.title"), true);

		JPanel content = new JPanel(new BorderLayout(12, 12));
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);

		try
		{
			list = new MacroListDownloadProgress(InstallMacrosDialog.this, false)
				.getMacroList();
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR, this, "An error occurred " + e.getMessage());
		}

		if(list == null)
			return;

		//Vector temp = new Vector();
		//temp.addAll(list.macros.values());
		//macros = new JCheckBoxList(temp);
		macros = new JCheckBoxList(list.sortMacroList(MacroList.SORT_BY_NAME));
		macros.getSelectionModel().addListSelectionListener(new ListHandler());
		macros.getModel().addTableModelListener(new TableModelHandler());
		scroller = new JScrollPane(macros);
		scroller.setBorder(new TitledBorder(jEdit.getProperty("install-macros.refresh") + 
			" " + MacroList.timestamp));
		//scroller.setPreferredSize(new Dimension(200,0));
		content.add(BorderLayout.WEST, scroller);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new TitledBorder(jEdit.getProperty("install-macros"
			 + ".macro-info")));

		JPanel labelAndValueBox = new JPanel(new BorderLayout());

		JPanel labelBox = new JPanel(new GridLayout(6, 1, 0, 3));
		labelBox.setBorder(new EmptyBorder(0, 0, 3, 12));
		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
			 + ".info.name"), SwingConstants.RIGHT));
		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
			 + ".info.author"), SwingConstants.RIGHT));
		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
			 + ".info.size"), SwingConstants.RIGHT));
		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
			 + ".info.latest-version"), SwingConstants.RIGHT));
		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
			 + ".info.number-of-downloads"), SwingConstants.RIGHT));
//		labelBox.add(new JLabel(jEdit.getProperty("install-macros"
//			+ ".info.description"),SwingConstants.RIGHT));
		labelAndValueBox.add(BorderLayout.WEST, labelBox);

		JPanel valueBox = new JPanel(new GridLayout(6, 1, 0, 3));
		valueBox.setBorder(new EmptyBorder(0, 0, 3, 0));
		valueBox.add(name = new JLabel());
		valueBox.add(author = new JLabel());
		valueBox.add(size = new JLabel());
		valueBox.add(latestVersion = new JLabel());
		valueBox.add(numberOfDownloads = new JLabel());
		valueBox.add(Box.createGlue());
		labelAndValueBox.add(BorderLayout.CENTER, valueBox);

		panel.add(BorderLayout.NORTH, labelAndValueBox);

		description = new JTextArea(6, 50);
		description.setEditable(false);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);

		panel.add(BorderLayout.CENTER, new JScrollPane(description));

		content.add(BorderLayout.CENTER, panel);

		JPanel panel2 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		Box totalSizeBox = new Box(BoxLayout.X_AXIS);
		totalSizeBox.add(new JLabel(jEdit.getProperty("install-macros.totalSize")));
		totalSizeBox.add(Box.createHorizontalStrut(12));
		totalSizeBox.add(totalSize = new JLabel());
		//c.gridx = 0;
		//c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.LINE_START;
		c.insets = new Insets(0, 0, 10, 0);
		panel2.add(totalSizeBox, c);

		ButtonGroup grp = new ButtonGroup();
		installUser = new JRadioButton();
		installUser.addActionListener(new ActionHandler());
		String settings = jEdit.getSettingsDirectory();
		if(settings == null)
		{
			settings = jEdit.getProperty("install-macros.none");
			installUser.setEnabled(false);
		}
		else
		{
			settings = MiscUtilities.constructPath(settings, "macros");
			installUser.setEnabled(true);
		}
		String[] args = {settings};
		installUser.setText("  " + jEdit.getProperty("install-macros.user", args));
		grp.add(installUser);
		//c.gridx = 0;
		//c.gridy = 1;
		c.insets = new Insets(0, 0, 0, 0);
		//c.anchor = GridBagConstraints.LINE_START;
		panel2.add(installUser, c);

		installSystem = new JRadioButton();
		installSystem.addActionListener(new ActionHandler());
		String jEditHome = jEdit.getJEditHome();
		if(jEditHome == null)
		{
			jEditHome = jEdit.getProperty("install-macros.none");
			installSystem.setEnabled(false);
		}
		else
		{
			jEditHome = MiscUtilities.constructPath(jEditHome, "macros");
			installSystem.setEnabled(true);
		}
		args[0] = jEditHome;
		installSystem.setText("  " + jEdit.getProperty("install-macros.system", args));
		grp.add(installSystem);
		//c.gridx = 0;
		//c.gridy = 2;
		//c.anchor = GridBagConstraints.LINE_START;
		panel2.add(installSystem, c);

		installCustom = new JRadioButton();
		installCustom.addActionListener(new ActionHandler());
		installCustom.setText("  " + jEdit.getProperty("install-macros.custom"));
		grp.add(installCustom);
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 10, 5);
		panel2.add(installCustom, c);

		choosedir = new RolloverButton(GUIUtilities.loadIcon("OpenFolder.png"));
		choosedir.setToolTipText(jEdit.getProperty("install-macros.choosedir"));
		//choosedir = new JButton("...");
		choosedir.addActionListener(new ActionHandler());
		c.anchor = GridBagConstraints.CENTER;
		panel2.add(choosedir, c);

		customDir = new JTextField("", 30);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel2.add(customDir, c);

		String work = jEdit.getProperty("intall-macors.custom.directory");
		if(work != null)
		{
			installCustom.setSelected(true);
			customDir.setEnabled(true);
			customDir.setText(work);
		}
		else if(installUser.isEnabled())
		{
			customDir.setEnabled(false);
			choosedir.setEnabled(false);
			installUser.setSelected(true);
		}
		else
		{
			customDir.setEnabled(false);
			choosedir.setEnabled(false);
			installSystem.setSelected(true);
		}
		
		refreshList = new JButton(jEdit.getProperty("install-macros.refreshList"));
		refreshList.addActionListener(new ActionHandler());
		//c.gridx = 0;
		c.gridy = 4;
		//c.insets = new Insets(0, 0, 0, 5);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		panel2.add(refreshList, c);

		install = new JButton(jEdit.getProperty("install-macros.install"));
		install.setEnabled(false);
		getRootPane().setDefaultButton(install);
		install.addActionListener(new ActionHandler());
//		c.gridx = 1;
//		c.gridy = 4;
		c.anchor = GridBagConstraints.CENTER;
		panel2.add(install, c);

		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(new ActionHandler());
		//c.gridx = 2;
		//c.gridy = 4;
		panel2.add(cancel, c);

		sort = new JButton(jEdit.getProperty("install-macros.sort-by-date"));
		sort.addActionListener(new ActionHandler());
		//c.gridx = 3;
		//c.gridy = 4;
		panel2.add(sort, c);

		searchField = new JTextField(jEdit.getProperty("install-macros.searchField"));
		/*searchField.getInputMap(JComponent.WHEN_FOCUSED).put(
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "callSort");
		searchField.getActionMap().put("callSort", new AbstractAction() {
			public void actionPerformed(ActionEvent e)
			{
				sort();
			}
		});*/
		searchField.addKeyListener(new KeyHandler());
		searchField.addFocusListener(
			new FocusAdapter()
			{
				public void focusGained(FocusEvent evt)
				{
					searchField.selectAll();
				}
			});
		searchField.addMouseListener(
			new MouseAdapter()
			{
				public void mouseClicked(MouseEvent evt)
				{
					searchField.selectAll();
				}
			});

		searchField.setColumns(20);
		//c.gridx = 4;
		//c.gridy = 4;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel2.add(searchField, c);

		content.add(BorderLayout.SOUTH, panel2);

		updateTotalSize();

		pack();
		setLocationRelativeTo(frame);
		show();
	}

	//}}}

	//{{{ ok() method
	/**
	 *  Description of the Method
	 */
	public void ok()
	{
		if(installCustom.isSelected())
			jEdit.setProperty("intall-macors.custom.directory", customDir.getText());
		else
			jEdit.unsetProperty("intall-macors.custom.directory");

		macroManager.Roster roster = new macroManager.Roster();
		installMacros(roster);
		if(roster.isEmpty())
			return;
		new MacroManagerProgress(InstallMacrosDialog.this, roster);

		// rescan the macros to update the menu
		Macros.loadMacros();
		dispose();
	}

	//}}}


	//{{{ cancel() method
	/**
	 *  Description of the Method
	 */
	public void cancel()
	{
		cancelled = true;

		dispose();
	}

	//}}}


	//{{{ installPlugins() method
	void installMacros(macroManager.Roster roster)
	{
		if(cancelled)
			return;

		String installDirectory;
		if(installUser == null || installUser.isSelected())
			installDirectory = MiscUtilities.constructPath(
				jEdit.getSettingsDirectory(), "macros");

		else if(installSystem.isSelected())
			installDirectory = MiscUtilities.constructPath(
				jEdit.getJEditHome(), "macros");

		else
			installDirectory = customDir.getText();

		Object[] selected = macros.getCheckedValues();
		for(int i = 0; i < selected.length; i++)
			((MacroList.Macro)selected[i]).install(roster, installDirectory);

	}

	//}}}


	//{{{ Private members

	//{{{ Instance variables
	private JCheckBoxList macros;
	private JLabel name;
	private JLabel author;
	private JLabel size;
	private JLabel latestVersion;
	private JLabel numberOfDownloads;
	private JTextArea description;
	private JLabel totalSize;
	private JLabel dateLabel;
	private JRadioButton installUser;
	private JRadioButton installSystem;
	private JRadioButton installCustom;
	private JTextField customDir;
	private JScrollPane scroller;
	private JButton install;
	private JButton cancel;
	private JButton sort;
	private JButton refreshList;
	private JButton choosedir;
	private JTextField searchField;
//	private JButton search;

	private MacroList list;

	private boolean cancelled;
	private boolean sortedByDate = false;
	private Thread thread;


	//}}}

	//{{{ updateInfo() method
	private void updateInfo()
	{
		Object selected = macros.getSelectedValue();
		if(selected instanceof MacroList.Macro)
		{
			MacroList.Macro macro = (MacroList.Macro)selected;
			name.setText(macro.name);
			author.setText(macro.author);
			size.setText(String.valueOf((macro.size / 1024)) + " Kb");
			latestVersion.setText(macro.version);
			numberOfDownloads.setText(macro.hits);
			description.setText(macro.description);
			description.setCaretPosition(0);
		}
		else
		{
			name.setText(null);
			author.setText(null);
			size.setText(null);
			latestVersion.setText(null);
			description.setText(null);
		}
	}

	//}}}


	//{{{ updateTotalSize() method
	private void updateTotalSize()
	{
		ArrayList selectedMacros = new ArrayList();

		Object[] selected = macros.getCheckedValues();
		install.setEnabled(selected.length != 0);

		for(int i = 0; i < selected.length; i++)
		{
			MacroList.Macro macro = (MacroList.Macro)selected[i];
			if(!selectedMacros.contains(macro))
				selectedMacros.add(macro);
		}

		int _totalSize = 0;
		for(int i = 0; i < selectedMacros.size(); i++)
			_totalSize += ((MacroList.Macro)selectedMacros.get(i)).size;

		totalSize.setText(String.valueOf(_totalSize / 1024) + " Kb");
	}

	//}}}


	//}}}

	//{{{ sort method
	void sort()
	{
		int constraint;
		if(sort.getText().equals(jEdit.getProperty("install-macros.sort-by-date")))
		{
			constraint = MacroList.SORT_BY_DATE;
			sort.setText(jEdit.getProperty("install-macros.sort-by-set"));
		}
		else if(sort.getText().equals(jEdit.getProperty("install-macros.sort-by-name")))
		{
			constraint = MacroList.SORT_BY_NAME;
			sort.setText(jEdit.getProperty("install-macros.sort-by-date"));
		}
		else
		{
			constraint = MacroList.SORT_BY_SET;
			sort.setText(jEdit.getProperty("install-macros.sort-by-name"));
		}
		macros.setModel(list.sortMacroList(constraint));
		//macros.setModel(list.macros);
		macros.getSelectionModel().addListSelectionListener(new ListHandler());
		macros.getModel().addTableModelListener(new TableModelHandler());
	}

	//}}}


	//{{{ search method
	void search()
	{
		String srch = searchField.getText();
		Log.log(Log.DEBUG, this, "searching for " + srch);
		if(srch == null || srch.length() == 0)
		{
			//macros.setModel(list.macros);
			//macros.getSelectionModel().addListSelectionListener(new ListHandler());
			//macros.getModel().addTableModelListener(new TableModelHandler());
		}

		Vector results = list.searchMacroList(srch);
		if(results.size() > 0)
		{
			macros.setModel(results);
			macros.getSelectionModel().addListSelectionListener(new ListHandler());
			macros.getModel().addTableModelListener(new TableModelHandler());
			searchField.selectAll();
		}
		else
		{
			searchField.setText("No matches");
			searchField.selectAll();
		}
	}

	//}}}


	//{{{ refreshList method
	void refreshList()
	{
		try
		{
			list = new MacroListDownloadProgress(InstallMacrosDialog.this, true)
				.getMacroList();
		}
		catch(Exception e)
		{
			Log.log(Log.ERROR, this, "An error occurred " + e.getMessage());
		}
		Vector temp = new Vector();
		temp.addAll(list.macros.values());
		macros.setModel(temp);
		macros.getSelectionModel().addListSelectionListener(new ListHandler());
		macros.getModel().addTableModelListener(new TableModelHandler());
		scroller.setBorder(new TitledBorder(jEdit.getProperty("install-macros.refresh") + 
			" " + MacroList.timestamp));
	}

	//}}}


	//{{{ updateButtons method
	private void updateButtons()
	{
		if(installUser.isSelected() || installSystem.isSelected())
		{
			customDir.setEnabled(false);
			choosedir.setEnabled(false);
		}
		else
		{
			customDir.setEnabled(true);
			choosedir.setEnabled(true);
		}
	}


	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == install)
				ok();
			else if(source == cancel)
				cancel();
			else if(source == sort)
				sort();
			else if(source == refreshList)
				refreshList();
			else if(source == installUser || source == installSystem || source == installCustom)
				updateButtons();
			else if(source == choosedir)
			{
				String work = customDir.getText();
				JFileChooser chooser = null;
				if(work != null && work.length() > 0)
					chooser = new JFileChooser(work);
				else
					chooser = new JFileChooser();
				chooser.setDialogTitle(
					jEdit.getProperty("install-macros.filechooser.title"));
				chooser.setDialogType(JFileChooser.OPEN_DIALOG);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.addChoosableFileFilter(
					new FileFilter()
					{
						public boolean accept(File f)
						{
							return f.isDirectory();
						}


						public String getDescription()
						{
							return jEdit.getProperty(
								"install-macros.filechooser.filefilter");
						}
					});
				int retVal = chooser.showOpenDialog(InstallMacrosDialog.this);
				if(retVal == JFileChooser.APPROVE_OPTION)
				{
					java.io.File f = chooser.getSelectedFile();
					customDir.setText(f.getAbsolutePath());
				}
			}

//			else if(search == sort)
//				search();
		}
	}

	//}}}


	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter
	{
		public void keyReleased(KeyEvent evt)
		{
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
			{
				search();
				evt.consume();
			}
		}
		public void keyPressed(KeyEvent evt)
		{
			// if they have a macro selected, make sure we
			// don't skip the search and download it
			if(evt.getKeyCode() == KeyEvent.VK_ENTER)
				evt.consume();
		}
	}

	//}}}


	//{{{ ListHandler class
	class ListHandler implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent evt)
		{
			updateInfo();
		}
	}

	//}}}


	//{{{ TableModelHandler class
	class TableModelHandler implements TableModelListener
	{
		public void tableChanged(TableModelEvent e)
		{
			updateTotalSize();
		}
	}
	//}}}
}

