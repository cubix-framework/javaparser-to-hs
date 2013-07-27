/**
 * JdbcOptionPane.java - Sql Plugin
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

package sql.options;

import java.awt.BorderLayout;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.gjt.sp.jedit.jEdit;

import sql.SqlPlugin;

import common.gui.pathbuilder.ClasspathFilter;
import common.gui.pathbuilder.PathBuilder;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class JdbcOptionPane extends SqlOptionPane
{
	private TreeMap jdbcClassPath;

	private PathBuilder pathBuilder;

	private final static String PROP_LAST_DIRECTORY = "sql.jdbc.lastDirectory";

	/**
	 *  Constructor for the SqlOptionPane object
	 *
	 * @since
	 */
	public JdbcOptionPane()
	{
		super("sql_jdbc");
	}


	/**
	 *Description of the Method
	 *
	 * @since
	 */
	public void _init()
	{
		super._init();

		JPanel panel = new JPanel();
		{
			panel.setLayout(new BorderLayout(5, 5));
			panel.setBorder(createTitledBorder("sql.options.jdbc.classpath.label"));
			pathBuilder = new PathBuilder();
			{
				pathBuilder.setMoveButtonsEnabled(false);
				pathBuilder.setFileFilter(new ClasspathFilter());
				pathBuilder.setFileSelectionMode(JFileChooser.FILES_ONLY);
			}
			panel.add(pathBuilder, BorderLayout.CENTER);
		}
		add(panel, BorderLayout.NORTH);

		final String paths[] = SqlPlugin.getJdbcClassPath();
		jdbcClassPath = new TreeMap();
		for (int i = paths.length; --i >= 0;)
			jdbcClassPath.put(paths[i], paths[i]);

		pathBuilder.setPathArray((String[]) jdbcClassPath.values().toArray(new String[0]));
		pathBuilder.setStartDirectory(jEdit.getProperty(PROP_LAST_DIRECTORY));
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void _save()
	{
		SqlPlugin.setJdbcClassPath(pathBuilder.getPathArray());

		SqlPlugin.registerJdbcClassPath();

		jEdit.setProperty(PROP_LAST_DIRECTORY, pathBuilder.getStartDirectory());
	}
}

