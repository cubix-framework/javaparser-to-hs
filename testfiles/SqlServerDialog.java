/**
 * SqlServerDialog.java - Sql Plugin
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.Log;

import sql.*;

/**
 *  Description of the Class
 *
 * @author     svu
 */
public class SqlServerDialog extends JDialog
{
	private JTextField nameField;
	private JTextField statementDelimiterRegexField;

	private JComboBox serverTypeList;

	private Map controls = new HashMap();
	private CardLayout serverTypeCards;
	private JPanel serverTypePanel;

	private SqlServerRecord rec;
	private int mode;

	private JButton okBtn;
	private JButton cancelBtn;

	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int ADD_MODE = 0;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int DEL_MODE = 1;
	/**
	 *  Description of the Field
	 *
	 * @since
	 */
	public final static int EDIT_MODE = 2;

	private final static String EMPTY_CARD = "__empty__";


	/**
	 *  Constructor for the SqlServerDialog object
	 *
	 * @param  f     Description of Parameter
	 * @param  rec   Description of Parameter
	 * @param  mode  Description of Parameter
	 * @since
	 */
	public SqlServerDialog(JDialog d, SqlServerRecord rec, int mode)
	{
		super(d, jEdit.getProperty("sql.serverconfig.title"), true);
		this.rec = rec;
		this.mode = mode;
		init();
	}


	/**
	 *  Gets the Result attribute of the SqlServerDialog object
	 *
	 * @return    The Result value
	 * @since
	 */
	public SqlServerRecord getResult()
	{
		return rec;
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void init()
	{
		final Pane pane = new Pane();

		nameField = new JTextField(15);

		pane.addC(jEdit.getProperty("sql.name.label"), nameField);

		pane.addS("");

		final JPanel p = new JPanel(new BorderLayout(5, 5));
		statementDelimiterRegexField = new JTextField(15);
		p.add(BorderLayout.CENTER, statementDelimiterRegexField);
		final JButton sdrResetBtn = new JButton(jEdit.getProperty("sql.statementDelimiterRegex.reset.label"));
		sdrResetBtn.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        statementDelimiterRegexField.setText("");
			        }
		        });
		final JPanel p1 = new JPanel(new BorderLayout(5, 5));
		p1.add(BorderLayout.WEST, sdrResetBtn);
		final JButton sdrCopyBtn = new JButton(jEdit.getProperty("sql.statementDelimiterRegex.copy.label"));
		sdrCopyBtn.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        resetStatementDelimiterRegex();
			        }
		        });
		p1.add(BorderLayout.EAST, sdrCopyBtn);
		p.add(BorderLayout.SOUTH, p1);
		pane.addC(jEdit.getProperty("sql.statementDelimiterRegex.label"), p);

		pane.addS("sql.config.label");

		final Map types = SqlServerType.getAllTypes();
		final java.util.List typeNames = new ArrayList();

		for (Iterator e = types.values().iterator(); e.hasNext();)
		{
			final String n = ((SqlServerType) e.next()).getName();
			typeNames.add(n);
		}

		serverTypeList = new JComboBox(typeNames.toArray());
		pane.addC(jEdit.getProperty("sql.serverType.label"), serverTypeList);

		pane.addS("");

		serverTypePanel = new JPanel(serverTypeCards = new CardLayout());

		serverTypePanel.add(EMPTY_CARD, new JPanel());

		for (Iterator e = SqlServerType.getAllTypes().values().iterator(); e.hasNext();)
		{
			final SqlServerType type = (SqlServerType) e.next();
			final String name = type.getName();
			final Pane serverPane = new Pane();
			final Map serverTypeControls = new HashMap();

			for (Iterator ep = type.getConnectionParameters().values().iterator();
			                ep.hasNext();)
			{
				final SqlServerType.ConnectionParameter param
				= (SqlServerType.ConnectionParameter) ep.next();

				JTextField tf;
				if (rec.PROP_PASSWORD.equals(param.getName()))
					tf = new JPasswordField(param.getDefaultValue(), 15);
				else
					tf = new JTextField(param.getDefaultValue(), 15);

				serverTypeControls.put(param.getName(), tf);
				final String paramDesc = param.getDescription();
				serverPane.addC(paramDesc.substring(0, 1).toUpperCase() + paramDesc.substring(1), tf);
			}
			final String dsdr = regex2Text(type.getDefaultStatementDelimiterRegex());
			final JLabel defStmtDelimiterRegexLbl = new JLabel(dsdr);
			serverPane.addC(jEdit.getProperty("sql.defaultStatementDelimiterRegex.label"), defStmtDelimiterRegexLbl);

			controls.put(type.getName(), serverTypeControls);
			serverTypePanel.add(name, serverPane);
		}

		serverTypeList.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        final String name = (String) serverTypeList.getSelectedItem();
				        serverTypeCards.show(serverTypePanel,
				                             name == null ? EMPTY_CARD : name);
				        //          resetStatementDelimiterRegex();
			        }
		        });

		getContentPane().setLayout(new BorderLayout());
		Box hp = Box.createHorizontalBox();
		hp.add(hp.createHorizontalStrut(10));
		Box vp = Box.createVerticalBox();
		vp.add(pane);
		vp.add(serverTypePanel);
		hp.add(vp);
		hp.add(hp.createHorizontalStrut(10));
		getContentPane().add(BorderLayout.CENTER, hp);

		hp = Box.createHorizontalBox();
		hp.add(hp.createHorizontalStrut(10));
		hp.add(BorderLayout.WEST, okBtn = new JButton("OK"));
		hp.add(hp.createHorizontalStrut(10));
		hp.add(hp.createHorizontalGlue());
		hp.add(BorderLayout.EAST, cancelBtn = new JButton("Cancel"));
		hp.add(hp.createHorizontalStrut(10));

		vp = Box.createVerticalBox();
		vp.add(vp.createVerticalStrut(10));
		vp.add(hp);
		vp.add(vp.createVerticalStrut(10));
		getContentPane().add(BorderLayout.SOUTH, vp);

		okBtn.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        if (!validateParams())
					        return;
				        save();

				        SqlServerDialog.this.setVisible(false);
			        }
		        });

		cancelBtn.addActionListener(
		        new ActionListener()
		        {
			        public void actionPerformed(ActionEvent evt)
			        {
				        rec = null;
				        SqlServerDialog.this.setVisible(false);
			        }
		        });

		serverTypeCards.show(serverTypePanel, EMPTY_CARD);
		serverTypeList.setSelectedItem(null);

		this.load();

		nameField.setEnabled(ADD_MODE == mode);

		statementDelimiterRegexField.setEnabled(DEL_MODE != mode);

		serverTypeList.setEnabled(ADD_MODE == mode);

		for (Iterator e = controls.values().iterator(); e.hasNext();)
		{
			Map serverControls = (Map) e.next();
			for (Iterator e1 = serverControls.values().iterator(); e1.hasNext();)
			{
				JTextField tf = (JTextField) e1.next();
				tf.setEnabled(DEL_MODE != mode);
			}
		}

		pack();
		//setResizable( false );
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Returned Value
	 * @since
	 */
	public boolean validateParams()
	{
		if (mode != ADD_MODE)
			return true;

		final String typeName = (String) serverTypeList.getSelectedItem();
		if (typeName == null)
		{
			GUIUtilities.message(this,
			                     "sql.configurationError",
			                     new Object[]{jEdit.getProperty("sql.emptyType")});
			return false;
		}

		final String name = nameField.getText();

		if (name == null || "".equals(name))
		{
			GUIUtilities.message(this,
			                     "sql.configurationError",
			                     new Object[]{jEdit.getProperty("sql.emptyName")});
			return false;
		}

		if (!SqlServerRecord.isValidName(name))
		{
			GUIUtilities.message(this,
			                     "sql.configurationError",
			                     new Object[]{jEdit.getProperty("sql.illegalName")});
			return false;
		}

		return true;
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void load()
	{
		if (rec == null)
			return;

		nameField.setText(rec.getName());
		statementDelimiterRegexField.setText(regex2Text(rec.getStatementDelimiterRegex()));

		final String typeName = rec.getServerType().getName();

		serverTypeCards.show(serverTypePanel, typeName);
		serverTypeList.setSelectedItem(typeName);

		final Map serverControls = (Map) controls.get(typeName);
		for (Iterator e = serverControls.keySet().iterator(); e.hasNext();)
		{
			final String propName = (String) e.next();
			final JTextField tf = (JTextField) serverControls.get(propName);
			tf.setText(rec.getProperty(propName));
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @since
	 */
	public void save()
	{
		final String typeName = (String) serverTypeList.getSelectedItem();
		if (ADD_MODE == mode)
		{
			final SqlServerType type = SqlServerType.getByName(typeName);
			rec = new SqlServerRecord();
			rec.setName(nameField.getText());
			rec.setServerType(type);
		}

		final Map serverControls = (Map) controls.get(typeName);
		for (Iterator e = serverControls.keySet().iterator(); e.hasNext();)
		{
			final String propName = (String) e.next();
			final JTextField tf = (JTextField) serverControls.get(propName);
			rec.setProperty(propName, tf.getText());
		}
		rec.setStatementDelimiterRegex(text2Regex(statementDelimiterRegexField.getText()));
	}


	/**
	 *  Description of the Method
	 */
	protected void resetStatementDelimiterRegex()
	{
		final String typeName = (String) serverTypeList.getSelectedItem();
		if (typeName == null)
			return;
		final SqlServerType type = SqlServerType.getByName(typeName);
		if (type == null)
			return;

		statementDelimiterRegexField.setText(regex2Text(type.getDefaultStatementDelimiterRegex()));
	}


	/**
	 *  Description of the Method
	 *
	 * @param  regexp  Description of Parameter
	 * @return         Description of the Returned Value
	 */
	protected static String regex2Text(String regexp)
	{
		return regexp == null ? null :
		       regexp.replaceAll("\\n", "\\n").replaceAll("\\r", "\\r");
	}


	/**
	 *  Description of the Method
	 *
	 * @param  text  Description of Parameter
	 * @return       Description of the Returned Value
	 */
	protected static String text2Regex(String text)
	{
		return text == null ? null :
		       text.replaceAll("\\\\n", "\\\\n").replaceAll("\\\\r", "\\\\r");
	}


	protected static class Pane extends AbstractOptionPane
	{
		/**
		 *  Constructor for the Pane object
		 *
		 * @since
		 */
		public Pane()
		{
			super("?");
		}


		public void addC(String s, Component c)
		{
			addComponent(s, c);
		}


		public void addS(String s)
		{
			addSeparator(s);
		}
	}
}

