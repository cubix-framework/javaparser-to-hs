/**
 * ResultSetWindow.java - Sql Plugin
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
import java.sql.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.Element;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.util.*;

import common.gui.*;

import sql.*;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class ResultSetWindow extends JPanel
{

	protected View view;
	protected JLabel status;
	protected JTabbedPane notebook;

	public final static String RESULT_SETS_BUF_PROPERTY = "sql.resultSetPanels";

	/**
	 *  Constructor for the ResultSetWindow object
	 *
	 * @param  view  Description of Parameter
	 * @since
	 */
	public ResultSetWindow(View view)
	{
		this.view = view;

		setLayout(new BorderLayout());

		notebook = new JTabbedPane();
		add(BorderLayout.CENTER, notebook);

		status = new JLabel();
		final JPanel p = new JPanel(new BorderLayout());
		p.add(BorderLayout.WEST, status);

		final JButton closeAllBtn = new JButton(jEdit.getProperty("sql.resultSet.closeAll"));
		closeAllBtn.setToolTipText(jEdit.getProperty("sql.resultSet.closeAll.tooltip"));

		p.add(BorderLayout.EAST, closeAllBtn);
		add(BorderLayout.SOUTH, p);
		closeAllBtn.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        while (notebook.getComponentCount() != 0)
					{
						final Component child = notebook.getComponent(0);
						((ResultSetPanel)child).stopRequerying();
					        notebook.remove(child);
					}
			        }
		        });

		notebook.addContainerListener(new ContainerListener()
		                              {
			                              public void componentAdded(ContainerEvent e)
			                              {
				                              componentRemoved(e);
			                              }

			                              public void componentRemoved(ContainerEvent e)
			                              {
				                              closeAllBtn.setEnabled(notebook.getComponentCount() != 0);
			                              }
		                              });

		closeAllBtn.setEnabled(false);

		status.setText(jEdit.getProperty("sql.resultSet.status",
		                                 new Object[]{new Integer(SqlUtils.getThreadGroup().getNumberOfRequest())}));

		SqlUtils.getThreadGroup().addListener(
		        new SqlThreadGroup.Listener()
		        {
			        public void groupChanged(final int numberOfActiveThreads)
			        {
				        SwingUtilities.invokeLater(
				                new Runnable()
				                {
					                public void run()
					                {
						                final Object[] args = {new Integer(numberOfActiveThreads)};
						                status.setText(jEdit.getProperty("sql.resultSet.status", args));
					                }
				                });
			        }
		        });
	}


	/**
	 *  Gets the Name attribute of the ResultSetWindow object
	 *
	 * @return    The Name value
	 * @since
	 */
	public String getName()
	{
		return SqlPlugin.resultSetWinName;
	}


	/**
	 *  Gets the Component attribute of the ResultSetWindow object
	 *
	 * @return    The Component value
	 * @since
	 */
	public Component getComponent()
	{
		return this;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  data  The feature to be added to the DataSet attribute
	 * @since
	 */
	public void addDataSet(final ResultSetPanel.Data data)
	{
		final SqlServerRecord sqlServer = data.getServerRecord();
		final String query = data.getQueryText();

		final ResultSetPanel newResultSetPanel = new ResultSetPanel(data, notebook);

		// Append the panel to the notebook and select
		final ImageIcon ii = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/ResultSetWindowTab.png")));
		notebook.addTab("", ii, newResultSetPanel, newResultSetPanel.getFormattedQuery());
		notebook.setSelectedComponent(newResultSetPanel);

		// Add to resultSet Panels
		final Buffer activeBuf = jEdit.getActiveView().getBuffer();
		if (activeBuf != null)
		{
			java.util.List resultSetPanels = (java.util.List)activeBuf.getProperty(RESULT_SETS_BUF_PROPERTY);
			if (resultSetPanels == null)
			{
				resultSetPanels = new ArrayList();
				activeBuf.setProperty(RESULT_SETS_BUF_PROPERTY, resultSetPanels);
			}
			resultSetPanels.add(newResultSetPanel);
		}

		// On closing RSP - remove it from RSPL
		notebook.addContainerListener(new ContainerAdapter()
		                              {
			                              public void componentRemoved(ContainerEvent evt)
			                              {
				                              final Component child = evt.getChild();
				                              final java.util.List resultSetPanels = (java.util.List)activeBuf.getProperty(RESULT_SETS_BUF_PROPERTY);
				                              if (resultSetPanels != null && child == newResultSetPanel)
					                              try {
						                              Log.log(Log.DEBUG, ResultSetWindow.class,
						                                      "Removing page " + child + " from the list of resultSets");
						                              resultSetPanels.remove(newResultSetPanel);
					                              } catch (Exception ex)
					                              { /* anything can happen but it does not matter here */ }
			                              }
		                              });
	}


	public void updateDataSet(JComponent rsp, ResultSetPanel.Data data)
	{
		final ResultSetPanel resultSetPanel = (ResultSetPanel)rsp;
		resultSetPanel.updateDataSet(data);
		revalidate();
	}

	public static class BufferListener implements EBComponent
	{

		public void handleMessage(EBMessage msg)
		{
			if (!(msg instanceof BufferUpdate))
				return;

			final BufferUpdate umsg = (BufferUpdate) msg;
			if (umsg.getWhat() != umsg.CLOSED)
				return;

			if (!ResultSetPanel.getCloseWithBuffer())
				return;

			final Buffer buffer = umsg.getBuffer();
			final java.util.List resultSetPanels = (java.util.List)buffer.getProperty(RESULT_SETS_BUF_PROPERTY);
			if (resultSetPanels != null)
			{
				for (Iterator i = resultSetPanels.iterator(); i.hasNext();)
				{
					final Component page = (Component)i.next();
					Log.log(Log.DEBUG, ResultSetWindow.class,
					        "Removing page " + page + " from the notebook and the list of resultSetPanels");
					i.remove();
					final Container notebook = page.getParent();
					if (notebook != null)   // probably the page was already removed from the notebook?
						notebook.remove(page);
				}
			}
		}
	}
}

