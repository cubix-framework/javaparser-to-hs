/*
 * DiffOptions - Options for the "Diff" dirty line provider.
 *
 * Copyright (C) 2009 Shlomy Reinstein
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
package lcm.providers.diff;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.ColorWellButton;

import lcm.DirtyLineProviderOptions;
import lcm.LCMOptions;

public class DiffOptions implements DirtyLineProviderOptions
{
	public static final String PROP_PREFIX = LCMOptions.PROP_PREFIX + ".diff.";
	public static final String ADD_COLOR_PROP = PROP_PREFIX + "addColor";
	public static final String REMOVE_COLOR_PROP = PROP_PREFIX + "removeColor";
	public static final String CHANGE_COLOR_PROP = PROP_PREFIX + "changeColor";
	private ColorWellButton addColor, removeColor, changeColor;

	private void addComponent(JPanel pane, String label, JComponent comp)
	{
		JPanel optionPanel = new JPanel(new BorderLayout());
		optionPanel.add(new JLabel(label), BorderLayout.CENTER);
		optionPanel.add(comp, BorderLayout.EAST);
		pane.add(optionPanel);
	}
	public void initOptions(JPanel pane)
	{
		pane.setLayout(new GridLayout(0, 1));
		addColor = new ColorWellButton(getAddColor());
		addComponent(pane, jEdit.getProperty(
			"messages.LCMPlugin.diff.addColor"), addColor);
		removeColor = new ColorWellButton(getRemoveColor());
		addComponent(pane, jEdit.getProperty(
			"messages.LCMPlugin.diff.removeColor"), removeColor);
		changeColor = new ColorWellButton(getChangeColor());
		addComponent(pane, jEdit.getProperty(
			"messages.LCMPlugin.diff.changeColor"), changeColor);
	}

	public void saveOptions()
	{
		jEdit.setColorProperty(ADD_COLOR_PROP, addColor.getSelectedColor());
		jEdit.setColorProperty(REMOVE_COLOR_PROP, removeColor.getSelectedColor());
		jEdit.setColorProperty(CHANGE_COLOR_PROP, changeColor.getSelectedColor());
	}

	public static Color getAddColor()
	{
		return jEdit.getColorProperty(ADD_COLOR_PROP, Color.GREEN);
	}
	public static Color getRemoveColor()
	{
		return jEdit.getColorProperty(REMOVE_COLOR_PROP, Color.RED);
	}
	public static Color getChangeColor()
	{
		return jEdit.getColorProperty(CHANGE_COLOR_PROP, Color.ORANGE);
	}
}
