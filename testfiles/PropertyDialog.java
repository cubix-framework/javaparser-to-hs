/*
 *  PropertiesDialog.java - Dialog to enter properties at runtime.
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;

public class PropertyDialog extends JDialog
{

	private PropertiesTable _propertiesTable = new PropertiesTable();
	private boolean _isCanceled = false;

	public PropertyDialog( Frame frame, String title )
	{
		super( frame, title, true );

		JScrollPane scrollPane = new JScrollPane( _propertiesTable );
		scrollPane.setPreferredSize( new Dimension( 300, 300 ) );
		
		_propertiesTable.setRowHeight(22);

		getContentPane().add( scrollPane, BorderLayout.CENTER );

		JPanel buttons = new JPanel();
		buttons.setLayout( new GridLayout( 2, 1 ) );

		final JCheckBox noPrompt = new JCheckBox( jEdit.getProperty( AntFarmPlugin.OPTION_PREFIX + "suppress-poperties-label" ) );
		noPrompt.setSelected( jEdit.getBooleanProperty( AntFarmPlugin.OPTION_PREFIX + "suppress-properties" ) );

		JButton buildButton = new JButton( "Build..." );
		buildButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed( ActionEvent e )
				{
					jEdit.setBooleanProperty( AntFarmPlugin.OPTION_PREFIX
						 + "suppress-properties", noPrompt.isSelected() );
					EditBus.send(new PropertiesChanged(null));

					if ( _propertiesTable.getCellEditor() != null )
						_propertiesTable.getCellEditor().stopCellEditing();

					setVisible( false );
				}
			} );

		JButton cancelButton = new JButton( "Cancel");
		cancelButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed( ActionEvent e)
				{
					_isCanceled = true;
					setVisible( false );
				}
			} );
			
		JPanel buildButtonPanel = new JPanel();
		buildButtonPanel.add( buildButton );
		buildButtonPanel.add( cancelButton );
		buttons.add( buildButtonPanel );
		buttons.add( noPrompt );

		getContentPane().add( buttons, BorderLayout.SOUTH );
		pack();
		setLocationRelativeTo(frame);
	}


	public Properties getProperties()
	{
		return _propertiesTable.getProperties();
	}

	public boolean isCanceled()
	{
		return _isCanceled;
	}
}

