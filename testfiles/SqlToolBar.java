/**
 * SqlToolBar.java - Sql Plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 *
 * Copyright (C) 2001 Sergey V. Udaltsov
 * svu@users.sourceforge.net
 *
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

package sql;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import org.gjt.sp.jedit.*;

import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.*;

import projectviewer.vpt.*;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class SqlToolBar
			extends JToolBar
			implements EBComponent
{

	protected JComboBox serverList;

	protected final Map preprocessorButtons = new HashMap();

	private View view;
	private JComboBox combo;

	private JButton execSelection;
	private JButton execBuffer;
	private JButton loadObject;
	private JButton requery;

	private JLabel title;

	private final static String SHOW_TITLE_PROP = "sql.toolbar.server.showTitle";
	private final static String SHOW_TOOLBAR_PROP = "sql.toolbar.showToolBar";


	/**
	 *Constructor for the SqlToolBar object
	 *
	 * @param  view     Description of Parameter
	 * @param  project  Description of Parameter
	 */
	public SqlToolBar(View view, final VPTProject project)
	{
		super();
		this.view = view;

		final Insets nullInsets = new Insets(0, 0, 0, 0);

		serverList = new JComboBox(SqlServerRecord.getAllNames(project));

		serverList.setSelectedItem(SqlUtils.getSelectedServerName(project));
		serverList.setEditable(false);
		serverList.addItemListener(
		        new ItemListener()
		        {
			        public void itemStateChanged(ItemEvent evt)
			        {
				        if (evt.getStateChange() == evt.SELECTED)
				        {
					        final Object sel = evt.getItem();
					        if (!(sel == null ||
					                        sel == SqlUtils.getSelectedServerName(project)))
						        SqlUtils.setSelectedServerName(project, sel.toString());
				        }
			        }
		        });

		final Toolkit toolkit = Toolkit.getDefaultToolkit();

		final Image execBufferImage = toolkit.getImage(getClass().getResource("/icons/execBuffer.png"));
		final Image execSelectionImage = toolkit.getImage(getClass().getResource("/icons/execSelection.png"));
		final Image loadObjectImage = toolkit.getImage(getClass().getResource("/icons/loadObject.png"));
		final Image repeatLastQueryImage = toolkit.getImage(getClass().getResource("/icons/repeatLastQuery.png"));

		execBuffer = new JButton(new ImageIcon(execBufferImage));
		execBuffer.setMargin(nullInsets);
		execBuffer.setToolTipText(jEdit.getProperty("sql.toolbar.execBuffer.tooltip"));
		execBuffer.setFocusPainted(false);

		execBuffer.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        SqlTextPublisher.publishBuffer(SqlToolBar.this.view,
				                                       SqlUtils.getSelectedServerName(project));
			        }
		        });

		execSelection = new JButton(new ImageIcon(execSelectionImage));
		execSelection.setMargin(nullInsets);
		execSelection.setToolTipText(jEdit.getProperty("sql.toolbar.execSelection.tooltip"));
		execSelection.setFocusPainted(false);

		execSelection.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        SqlTextPublisher.publishSelection(SqlToolBar.this.view,
				                                          SqlUtils.getSelectedServerName(project));
			        }
		        });

		loadObject = new JButton(new ImageIcon(loadObjectImage));
		loadObject.setMargin(nullInsets);
		loadObject.setToolTipText(jEdit.getProperty("sql.toolbar.loadObject.tooltip"));
		loadObject.setFocusPainted(false);

		loadObject.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        SqlPlugin.loadObject(SqlToolBar.this.view,
				                             SqlUtils.getSelectedServerName(project));
			        }
		        });

		requery = new JButton(new ImageIcon(repeatLastQueryImage));
		requery.setMargin(nullInsets);
		requery.setToolTipText(jEdit.getProperty("sql.toolbar.repeatLastQuery.tooltip"));
		requery.setFocusPainted(false);

		requery.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        SqlTextPublisher.repeatLastQuery(SqlToolBar.this.view,
				                                         SqlUtils.getSelectedServerName(project));
			        }
		        });

		setFloatable(false);
		putClientProperty("JToolBar.isRollover", Boolean.TRUE);

		addSeparator(new Dimension(5, 5));    // just add a little space at begin, looks better

		add(serverList);
		addSeparator();
		add(execSelection);
		add(execBuffer);
		addSeparator();
		add(loadObject);
		addSeparator();
		add(requery);
		addSeparator();

		final JButton preprocessorsButton = new JButton(jEdit.getProperty("sql.toolbar.preprocessors.label"));
		preprocessorsButton.setToolTipText(jEdit.getProperty("sql.toolbar.preprocessors.tooltip"));
		preprocessorsButton.setIcon(GUIUtilities.loadIcon("ToolbarMenu.gif"));
		preprocessorsButton.setHorizontalTextPosition(SwingConstants.LEADING);
		preprocessorsButton.setRequestFocusEnabled(false);
		preprocessorsButton.setMargin(new Insets(1, 1, 1, 1));

		final JPopupMenu ppm = new JPopupMenu();

		final Map preprocessors = SqlTextPublisher.getPreprocessors();
		for (Iterator i = preprocessors.keySet().iterator(); i.hasNext();)
		{
			final String className = (String) i.next();
			final Preprocessor prep = (Preprocessor) preprocessors.get(className);
			final JCheckBoxMenuItem cb = new JCheckBoxMenuItem(jEdit.getProperty(className + ".label"), prep.isEnabled());
			cb.setFocusPainted(false);
			cb.setToolTipText(jEdit.getProperty(className + ".tooltip"));

			preprocessorButtons.put(className, cb);
			ppm.add(cb);
			prep.addEnabledStateListener(
			        new PropertyChangeListener()
			        {
				        public void propertyChange(PropertyChangeEvent evt)
				        {
					        if ((cb.getSelectedObjects() != null) != prep.isEnabled())
						        cb.setSelected(prep.isEnabled());
				        }
			        });
			cb.addChangeListener(
			        new ChangeListener()
			        {
				        public void stateChanged(ChangeEvent evt)
				        {
					        if ((cb.getSelectedObjects() != null) != prep.isEnabled())
						        prep.setEnabled(cb.getSelectedObjects() != null);
				        }
			        });
		}


		preprocessorsButton.addMouseListener(new MouseAdapter()
		                                     {
			                                     public void mousePressed(MouseEvent evt)
			                                     {
				                                     if (ppm.isVisible())
				                                     {
					                                     ppm.setVisible(false);
					                                     return;
				                                     }

				                                     GUIUtilities.showPopupMenu(
				                                             ppm, preprocessorsButton, 0,
				                                             preprocessorsButton.getHeight(),
				                                             false);
			                                     }
		                                     });

		add(preprocessorsButton);
		add(Box.createGlue());

		updateTitle();
	}


	/**
	 * adds itself to EditBus on display
	 */
	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);
	}


	/**
	 * removes itself from EditBus on undisplay
	 */
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  message  Description of Parameter
	 */
	public void handleMessage(EBMessage message)
	{
		if (message instanceof PropertiesChanged)
			updateTitle();
		else if (message instanceof SqlServerChanged)
			handleSqlServerChanged((SqlServerChanged) message);
		else if (message instanceof SqlServerListChanged)
			handleSqlServerListChanged((SqlServerListChanged) message);

	}


	private void handleSqlServerChanged(SqlServerChanged msg)
	{
		final String newServer = msg.getNewServer();
		SwingUtilities.invokeLater(
		        new Runnable()
		        {
			        public void run()
			        {
				        serverList.setSelectedItem(newServer);
			        }
		        });
	}


	private void handleSqlServerListChanged(SqlServerListChanged msg)
	{
		final VPTProject project = SqlUtils.getProject(view);
		SwingUtilities.invokeLater(
		        new Runnable()
		        {
			        public void run()
			        {
				        serverList.setModel(new DefaultComboBoxModel(SqlServerRecord.getAllNames(project)));
				        serverList.setSelectedItem(SqlUtils.getSelectedServerName(project));
			        }
		        });
	}


	private void updateTitle()
	{
		if (showTitle())
			addTitle();
		else
			removeTitle();
	}


	private void addTitle()
	{
		if (title != null)
			return;// already added

		title = new JLabel(jEdit.getProperty("sql.toolbar.server.title"));
		add(title, 0);
		revalidate();
	}


	private void removeTitle()
	{
		if (title == null)
			return;// already removed

		remove(title);
		revalidate();
		title = null;
	}


	/**
	 *  Description of the Method
	 */
	public static void clearProperties()
	{
		SqlPlugin.unsetGlobalProperty(SHOW_TOOLBAR_PROP);
		SqlPlugin.unsetGlobalProperty(SHOW_TITLE_PROP);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  show  Description of Parameter
	 */
	public static void showToolBar(boolean show)
	{
		SqlPlugin.setGlobalProperty(SHOW_TOOLBAR_PROP, new Boolean(show).toString());
	}


	/**
	 *  Description of the Method
	 *
	 * @param  show  Description of Parameter
	 */
	public static void showTitle(boolean show)
	{
		SqlPlugin.setGlobalProperty(SHOW_TITLE_PROP, new Boolean(show).toString());
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 */
	public static boolean showToolBar()
	{
		return Boolean.valueOf(SqlPlugin.getGlobalProperty(SHOW_TOOLBAR_PROP)).booleanValue();
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 */
	public static boolean showTitle()
	{
		return Boolean.valueOf(SqlPlugin.getGlobalProperty(SHOW_TITLE_PROP)).booleanValue();
	}

}

