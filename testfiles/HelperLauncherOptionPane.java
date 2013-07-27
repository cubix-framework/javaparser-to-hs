/*
 *  HelperLauncherOptionPane.java - Panel in jEdit's Global Options dialog
 *  Copyright (C) 2007 Carmine Lucarelli
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
package helperlauncher;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.jEdit;

public class HelperLauncherOptionPane extends AbstractOptionPane
{

	final static String PROPERTY = "HelperLauncher.property.";
	final static String NAME = "glob";
	final static String VALUE = "value";

	private PropertiesTable _table;
	private JCheckBox _noPrompt;



	public HelperLauncherOptionPane()
	{
		super("Helper Launcher");
	}


	public void _init()
	{
//		setLayout(new BorderLayout(0,6));

//		JPanel north = new JPanel(new GridLayout(2, 1, 10, 10));
//		north.add(new JLabel(jEdit.getProperty("HelperLauncher.options.usage")));
		if (OperatingSystem.isWindows()) {
			_noPrompt = new JCheckBox(jEdit.getProperty(HelperLauncherOptionPane.PROPERTY + "UseWindows.label"));
			_noPrompt.setSelected(jEdit.getBooleanProperty(HelperLauncherOptionPane.PROPERTY + "UseWindows"));
			addComponent(_noPrompt);
		}
		else _noPrompt = null;
		//north.add(_noPrompt );
		
		//add(BorderLayout.NORTH, north);
		
		_table = new PropertiesTable(HelperLauncherPlugin.getGlobalProperties());
		JScrollPane scrollPane = new JScrollPane(_table);
		scrollPane.setPreferredSize(new Dimension(300, 300));
		//add(BorderLayout.CENTER, scrollPane);
		addComponent(scrollPane, GridBagConstraints.BOTH);
	}


	/**
	 *  Called when the options dialog's `OK' button is pressed. This should save
	 *  any properties saved in this option pane.
	 *
	 * @since
	 */
	public void _save()
	{

		if ( _table.getCellEditor() != null )
			_table.getCellEditor().stopCellEditing();

		if (_noPrompt != null)
			jEdit.setBooleanProperty(HelperLauncherOptionPane.PROPERTY + 
				"UseWindows", _noPrompt.isSelected());

		// get rid of old settings
		deleteGlobalProperties();
		// put in the new ones

		Properties properties = _table.getProperties();
		Enumeration propertyNames = properties.propertyNames();
		for ( int i = 0; propertyNames.hasMoreElements(); i++ ) {
			String name = (String) propertyNames.nextElement();
			String value = properties.getProperty( name );

			// Check if there is anything to save.
			if ( name == null || value == null )
				continue;

			if ( name.trim().length() > 0 && value.trim().length() > 0 ) {
				jEdit.setProperty( PROPERTY + ( i + 1 ) + NAME, name );
				jEdit.setProperty( PROPERTY + ( i + 1 ) + VALUE, value );
			}
		}
	}


	public void tableChanged( TableModelEvent e )
	{
		repaint();
	}


	private void deleteGlobalProperties()
	{
		String name;
		int counter = 1;
		while ( ( name = jEdit.getProperty( PROPERTY + counter + NAME ) ) != null ) {
			jEdit.setProperty( PROPERTY + counter + NAME, null );
			jEdit.setProperty( PROPERTY + counter + VALUE, null );
			counter++;
		}
	}

}


