/**
 * SqlUtils.java - Sql Plugin
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
import java.util.*;
import java.sql.*;
import java.text.*;

import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.*;

import errorlist.*;

import projectviewer.*;
import projectviewer.vpt.*;

import sql.*;
import sql.preprocessors.*;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class SqlUtils
{
	protected static DefaultErrorSource errorSource = null;

	protected static SqlThreadGroup sqlThreadGroup;


	/**
	 *  Sets the SelectedServerName attribute of the SqlUtils class
	 *
	 * @param  name     The new SelectedServerName value
	 * @param  project  The new SelectedServerName value
	 * @since
	 */
	public final static void setSelectedServerName(VPTProject project, final String name)
	{
		if (name != null)
			SqlPlugin.setLocalProperty(project, "sql.currentServerName", name);
		else
			SqlPlugin.unsetLocalProperty(project, "sql.currentServerName");

		new Thread()
		{
			public void run()
			{
				EditBus.send(new SqlServerChanged(
				                     null, name));
			}
		}.start();
	}


	/**
	 *  Gets the ProjectForView attribute of the SqlUtils class
	 *
	 * @param  view  Description of Parameter
	 * @return       The ProjectForView value
	 */
	public static VPTProject getProject(View view)
	{
		return ProjectViewer.getActiveProject(view);
	}


	/**
	 *  Gets the SelectedServerName attribute of the SqlUtils class
	 *
	 * @param  project  Description of Parameter
	 * @return          The SelectedServerName value
	 * @since
	 */
	public final static String getSelectedServerName(VPTProject project)
	{
		return SqlPlugin.getLocalProperty(project, "sql.currentServerName");
	}


	/**
	 *  Gets the ServerRecord attribute of the SqlUtils class
	 *
	 * @param  serverName  Description of Parameter
	 * @param  project     Description of Parameter
	 * @return             The ServerRecord value
	 * @since
	 */
	public static SqlServerRecord getServerRecord(final VPTProject project, String serverName)
	{
		if (serverName != null)
		{
			final SqlServerRecord rec = SqlServerRecord.get(project, serverName);
			if (rec != null && rec.hasValidProperties())
				return rec;
		}

		runInAWTThreadNoWait(
		        new Runnable()
		        {
			        public void run()
			        {
				        GUIUtilities.error(jEdit.getActiveView(), "sql.noSettings", null);
			        }
		        });
		return null;
	}


	/**
	 *  Gets the ThreadGroup attribute of the SqlUtils class
	 *
	 * @return    The ThreadGroup value
	 * @since
	 */
	public static SqlThreadGroup getThreadGroup()
	{
		return sqlThreadGroup;
	}


	/**
	 *  Gets the ErrorSource attribute of the SqlUtils class
	 *
	 * @return    The ErrorSource value
	 */
	public static DefaultErrorSource getErrorSource()
	{
		if (errorSource == null)
		{
			errorSource = new DefaultErrorSource(SqlPlugin.NAME);
			ErrorSource.registerErrorSource(errorSource);
		}

		return errorSource;
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public static void init()
	{
		sqlThreadGroup = new SqlThreadGroup("SQL Queries");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  conn              Description of Parameter
	 * @param  userName          Description of Parameter
	 * @param  objName           Description of Parameter
	 * @param  objType           Description of Parameter
	 * @param  rec               Description of Parameter
	 * @return                   Description of the Returned Value
	 * @exception  SQLException  Description of Exception
	 * @since
	 */
	public static String loadObjectText(Connection conn,
	                                    SqlServerRecord rec,
	                                    String userName,
	                                    String objName,
	                                    String objType)
	throws SQLException
	{
		return loadObjectText(conn,
		                      rec,
		                      "selectCodeObjectLines",
		                      userName,
		                      objName,
		                      objType);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  conn              Description of Parameter
	 * @param  rec               Description of Parameter
	 * @param  userName          Description of Parameter
	 * @param  objName           Description of Parameter
	 * @param  objType           Description of Parameter
	 * @param  stmtTag           Description of Parameter
	 * @return                   Description of the Returned Value
	 * @exception  SQLException  Description of Exception
	 */
	public static String loadObjectText(Connection conn,
	                                    SqlServerRecord rec,
	                                    String stmtTag,
	                                    String userName,
	                                    String objName,
	                                    String objType)
	throws SQLException
	{
		Log.log(Log.DEBUG, SqlUtils.class,
		        "Loading the object " + userName + "/" +
		        objName + "/" +
		        objType);

		PreparedStatement pstmt = null;
		try
		{
			pstmt = rec.prepareStatement(
			                conn,
			                stmtTag,
			                new Object[]{userName, objName, objType});
			if (pstmt == null)
				return null;

			final ResultSet rs = executeQuery(pstmt);

			final StringBuffer sb = new StringBuffer();
			while (rs.next())
				sb.append(rs.getString("sourceCodeLine"));

			return new String(sb);
		} finally
		{
			rec.releaseStatement(pstmt);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  pstmt             Description of Parameter
	 * @return                   Description of the Returned Value
	 * @exception  SQLException  Description of Exception
	 */
	public static ResultSet executeQuery(PreparedStatement pstmt)
	throws SQLException
	{
		final long start = System.currentTimeMillis();
		final ResultSet rv = pstmt.executeQuery();
		final long end = System.currentTimeMillis();
		final long delta = end - start;
		Log.log(Log.DEBUG, SqlUtils.class,
		        "Query time: " + delta + "ms");
		return rv;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  view              Description of Parameter
	 * @param  conn              Description of Parameter
	 * @param  rec               Description of Parameter
	 * @param  userName          Description of Parameter
	 * @return                   Description of the Returned Value
	 * @exception  SQLException  Description of Exception
	 * @since
	 */
	public static Object[] loadObjectList(final View view,
	                                      Connection conn,
	                                      final SqlServerRecord rec,
	                                      String userName)
	throws SQLException
	{
		PreparedStatement pstmt = null;
		try
		{
			pstmt = rec.prepareStatement(conn,
			                             "selectUserObjects",
			                             new Object[]{userName});
			if (pstmt == null)
			{
				runInAWTThreadNoWait(
				        new Runnable()
				        {
					        public void run()
					        {
						        GUIUtilities.message(view,
						                             "sql.noStoredProcedures",
						                             new Object[]{rec.getServerType().getName()});
					        }
				        });
				return null;
			}
			final ResultSet rs = executeQuery(pstmt);
			final java.util.List rv = new ArrayList();

			while (rs.next())
			{
				final DbCodeObject obj = new DbCodeObject(rs.getString("name"),
				                         rs.getString("type"),
				                         rs.getString("valid"));
				rv.add(obj);
			}
			return rv.toArray();
		} finally
		{
			rec.releaseStatement(pstmt);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  view     Description of Parameter
	 * @param  ex       Description of Parameter
	 * @param  sqlText  Description of Parameter
	 * @param  rec      Description of Parameter
	 * @since
	 */
	public static void processSqlException(final View view,
	                                       final SQLException ex,
	                                       final String sqlText,
	                                       final SqlServerRecord rec)
	{
		runInAWTThreadNoWait(
		        new Runnable()
		        {
			        public void run()
			        {
				        GUIUtilities.message(view,
				                             "sql.sqlException",
				                             new Object[]{ex, ex.getMessage(), rec.getName()});
			        }
		        });
		Log.log(Log.ERROR, SqlUtils.class,
		        "Error running SQL:" + sqlText);
		Log.log(Log.ERROR, SqlUtils.class, ex);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  r  Description of Parameter
	 * @since
	 */
	public static void runInAWTThreadAndWait(Runnable r)
	{
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			try
			{
				SwingUtilities.invokeAndWait(r);
			} catch (Exception ex)
			{
				Log.log(Log.ERROR, SqlUtils.class,
				        "Error running " + r + " in AWT Thread:");
				Log.log(Log.ERROR, SqlUtils.class,
				        ex);
			}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  r  Description of Parameter
	 * @since
	 */
	public static void runInAWTThreadNoWait(Runnable r)
	{
		if (SwingUtilities.isEventDispatchThread())
			r.run();
		else
			SwingUtilities.invokeLater(r);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  view  Description of Parameter
	 */
	public static void showCurrentServerChooser(View view)
	{
		final VPTProject project = SqlUtils.getProject(view);
		final Map servers = SqlServerRecord.getAllRecords(project);

		final Object[] serverList = new Object[servers.size()];
		int i = 0;
		for (Iterator e = servers.keySet().iterator(); e.hasNext();)
			serverList[i++] = e.next();

		Arrays.sort(serverList);
		String selection = getSelectedServerName(project);

		selection = (String) JOptionPane.showInputDialog(view,
		                jEdit.getProperty("sql.serverchooser.prompt"),
		                jEdit.getProperty("sql.serverchooser.title"),
		                JOptionPane.INFORMATION_MESSAGE,
		                SqlPlugin.getIcon(),
		                serverList,
		                selection);

		if (selection != null)
			setSelectedServerName(project, selection);
	}

}

