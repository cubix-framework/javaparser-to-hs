/*
 *  PropertiesOptionPane.java - Panel in jEdit's Global Options dialog
 *  Copyright (C) 2001 Brian Knowles
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
package antfarm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelEvent;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

public class PropertiesOptionPane extends AbstractOptionPane
{

	final static String PROPERTY = "AntFarm.property.";
	final static String NAME = ".name";
	final static String VALUE = ".value";

	private PropertiesTable _table;
	private JCheckBox _noPrompt;



	public PropertiesOptionPane()
	{
		super( "Properties" );
	}


	public void _init()
	{
		setLayout(new BorderLayout(0,6));

		add( BorderLayout.NORTH, new JLabel(
			jEdit.getProperty( AntFarmPlugin.OPTION_PREFIX + "set-global-properties-label" )
			 ) );

		_table = new PropertiesTable( AntFarmPlugin.getGlobalProperties() );

		JScrollPane scrollPane = new JScrollPane( _table );
		scrollPane.setPreferredSize( new Dimension( 300, 300 ) );
		add( BorderLayout.CENTER, scrollPane );

		_noPrompt = new JCheckBox( jEdit.getProperty( AntFarmPlugin.OPTION_PREFIX + "suppress-poperties-label" ) );
		
		_noPrompt.setSelected( jEdit.getBooleanProperty( AntFarmPlugin.OPTION_PREFIX + "suppress-properties" ) );

		
		add( BorderLayout.SOUTH, _noPrompt );
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

		jEdit.setBooleanProperty( AntFarmPlugin.OPTION_PREFIX + "suppress-properties",
			_noPrompt.isSelected()
			 );

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


