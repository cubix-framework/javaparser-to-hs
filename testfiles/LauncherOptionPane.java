/*
 *  LauncherOptionPane.java - Panel in jEdit's Global Options dialog
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
package launcher;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

public final class LauncherOptionPane extends AbstractOptionPane
	implements ItemListener
{
	private static final String OPT_PREFIX = LauncherPlugin.OPT_PREFIX;
	private static final String PROP_PREFIX = LauncherPlugin.PROP_PREFIX;
	private static final String LABEL_SUFFIX = ".label";
	
	private static final String OPT_SELECT_TYPE = OPT_PREFIX + ".select-type";
	
	private static final String PROP_SEPARATOR = PROP_PREFIX + ".separator";
	
	private static final String PROP_NO_OPTIONS = PROP_PREFIX + ".no-options";
	
	private static final String ERR_DUPLICATE_LABEL = LauncherPlugin.ERR_PREFIX +
														".duplicate-label";

	private Map<String,OptionPane> optionPanelsByLabel = new HashMap<String,OptionPane>();
	private Map<String,LauncherType> launcherTypesByLabel = new HashMap<String,LauncherType>();
	private JComboBox launcherTypeChoice;
	private JPanel optionPanelContainer;
	private Component currentOptionPanel;
	private AbstractOptionPane noOptionsPanel;	
	
	private static String lastSelectedLauncherType = null;

	public LauncherOptionPane()
	{
		super(LauncherPlugin.PROP_PREFIX);
	}


	public void _init()
	{
		// Get list of launcher types
		String[] names = ServiceManager.getServiceNames(
				LauncherType.LAUNCHER_TYPE_SERVICE_NAME);
		String[] labels = new String[names.length];
		int i = 0;
		for (String name : names) {
			LauncherType launcherType = (LauncherType)ServiceManager.getService(
					LauncherType.LAUNCHER_TYPE_SERVICE_NAME, name);
			String label = launcherType.getLabel();
			labels[i++] = label;
			LauncherType previous = launcherTypesByLabel.put(label, launcherType);
			if (previous != null) {
				// We can't have two LauncherTypes with identical labels
				// If so, display and log error and continue ignoring previous
				Object[] args = new String[]{
						previous.getServiceName(),
						launcherType.getServiceName()};
				Log.log(Log.ERROR, this.getParent(),
						jEdit.getProperty(ERR_DUPLICATE_LABEL, args));
				GUIUtilities.error(this, ERR_DUPLICATE_LABEL, args);
			}
		}
		launcherTypeChoice = new JComboBox(labels);
		if (lastSelectedLauncherType != null)
			launcherTypeChoice.setSelectedItem(lastSelectedLauncherType);
		addComponent( jEdit.getProperty( OPT_SELECT_TYPE + LABEL_SUFFIX ),
							launcherTypeChoice);

		addSeparator(PROP_SEPARATOR + LABEL_SUFFIX);
		addComponent( optionPanelContainer = new JPanel( new BorderLayout() ) , GridBagConstraints.BOTH);
		itemStateChanged( null );
		launcherTypeChoice.addItemListener( this );
		
	}


	/**
	 *  Called when the options dialog's `OK' button is pressed. This should save
	 *  any properties saved in this option pane.
	 *
	 * @since
	 */
	public void _save()
	{
		for (OptionPane pane: optionPanelsByLabel.values()) {
			pane.save();
		}
		lastSelectedLauncherType = launcherTypeChoice.getSelectedItem().toString();
		LauncherPlugin plugin = (LauncherPlugin)jEdit.getPlugin(LauncherPlugin.class.getName());
		plugin.reload();
	}

	/**
	 * Handle a change in the combo box.
	 */
	public final void itemStateChanged( ItemEvent evt ) {
		try {
			String launcherTypeLabel = launcherTypeChoice.getSelectedItem().toString();
			OptionPane panel = optionPanelsByLabel.get(launcherTypeLabel);
			if (panel == null) {
				LauncherType launcherType = launcherTypesByLabel.get(launcherTypeLabel);
				panel = launcherType.getOptionPane();
				if (panel == null) {
					if (noOptionsPanel == null) {
						noOptionsPanel = new AbstractOptionPane(PROP_NO_OPTIONS);
						noOptionsPanel.add(new JLabel(jEdit.getProperty(PROP_NO_OPTIONS)));
					}
					panel = noOptionsPanel;
				}
				optionPanelsByLabel.put(launcherTypeLabel, panel);
				panel.init();
			}
			if (currentOptionPanel != null)
				optionPanelContainer.remove(currentOptionPanel);
			currentOptionPanel = panel == null ? null : panel.getComponent();
			if (panel != null) {
				optionPanelContainer.add(currentOptionPanel);
			}
			invalidate();
			revalidate();
			repaint();
		}
		catch ( Exception e ) {
			Log.log(Log.ERROR, this, e );
			GUIUtilities.error(this, LauncherPlugin.ERR_EXCEPTION, new String[]{e.toString()} );
		}
	}

}


