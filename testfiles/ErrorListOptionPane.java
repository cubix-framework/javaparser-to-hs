/*
 * ErrorListOptionPane.java - Error list options panel
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2005 Slava Pestov
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

package errorlist;

//{{{ Imports
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
//}}}

public class ErrorListOptionPane extends AbstractOptionPane
{
	//{{{ ErrorListOptionPane constructor
	public ErrorListOptionPane()
	{
		super("error-list");
	} //}}}

	//{{{ _init() method
	protected void _init()
	{
		addComponent(showOnError = new JCheckBox(jEdit.getProperty(
			"options.error-list.showOnError")));
		showOnError.getModel().setSelected(jEdit.getBooleanProperty(
			"error-list.showOnError"));

		addComponent(autoCloseOnNoErrors = new JCheckBox(jEdit.getProperty(
			"options.error-list.autoCloseOnNoErrors")));
		addComponent(autoRefocusTextArea = new JCheckBox(jEdit.getProperty(
			"options.error-list.autoRefocusTextArea")));
		autoCloseOnNoErrors.getModel().setSelected(jEdit.getBooleanProperty(
			"error-list.autoCloseOnNoErrors"));
		autoRefocusTextArea.getModel().setSelected(jEdit.getBooleanProperty(
			"error-list.autoRefocusTextArea"));
		
		addComponent(showErrorOverview = new JCheckBox(jEdit.getProperty(
			"options.error-list.showErrorOverview")));
		showErrorOverview.getModel().setSelected(jEdit.getBooleanProperty(
			"error-list.showErrorOverview"));

		addComponent(showUnderlines = new JCheckBox(jEdit.getProperty(
			"options.error-list.showUnderlines")));
		showUnderlines.setSelected(jEdit.getBooleanProperty(
			ErrorListPlugin.SHOW_UNDERLINES));

		addComponent(showIconsInGutter = new JCheckBox(jEdit.getProperty(
			"options.error-list.gutterIcons")));
		showIconsInGutter.setSelected(jEdit.getBooleanProperty(
			ErrorListPlugin.SHOW_ICONS_IN_GUTTER));
		
		addComponent(jEdit.getProperty("options.error-list.warningColor"),
			warningColor = new ColorWellButton(jEdit.getColorProperty(
			"error-list.warningColor")));

		addComponent(jEdit.getProperty("options.error-list.errorColor"),
			errorColor = new ColorWellButton(jEdit.getColorProperty(
			"error-list.errorColor")));

		boolean inclusion = jEdit.getBooleanProperty(ErrorListPlugin.IS_INCLUSION_FILTER);
		isInclusionFilter = new JRadioButton(jEdit.getProperty(
			"options.error-list.isInclusionFilter"), inclusion);
		isExclusionFilter = new JRadioButton(jEdit.getProperty(
			"options.error-list.isExclusionFilter"), (! inclusion));
		ButtonGroup group = new ButtonGroup();
		group.add(isInclusionFilter);
		group.add(isExclusionFilter);
		JPanel filterPane = new JPanel(new GridLayout(0, 1));
		TitledBorder filterBorder = new TitledBorder(jEdit.getProperty(
			"options.error-list.filenameFilters"));
		filterPane.setBorder(filterBorder);
		filterPane.add(isInclusionFilter);
		filterPane.add(isExclusionFilter);
		filenameFilter = new JTextField(jEdit.getProperty(
			ErrorListPlugin.FILENAME_FILTER));
		filterPane.add(filenameFilter);
		addComponent(filterPane, GridBagConstraints.HORIZONTAL);
	} //}}}

	//{{{ _save() method
	protected void _save()
	{
		jEdit.setBooleanProperty("error-list.showOnError",showOnError
			.getModel().isSelected());
		jEdit.setBooleanProperty("error-list.showErrorOverview",
			showErrorOverview.getModel().isSelected());
		jEdit.setBooleanProperty("error-list.autoCloseOnNoErrors",
			autoCloseOnNoErrors.getModel().isSelected());
		jEdit.setBooleanProperty("error-list.autoRefocusTextArea",
			autoRefocusTextArea.getModel().isSelected());
		jEdit.setColorProperty("error-list.warningColor",
			warningColor.getSelectedColor());
		jEdit.setColorProperty("error-list.errorColor",
			errorColor.getSelectedColor());
		jEdit.setBooleanProperty(ErrorListPlugin.IS_INCLUSION_FILTER,
			isInclusionFilter.isSelected());
		jEdit.setProperty(ErrorListPlugin.FILENAME_FILTER,
			filenameFilter.getText());
		jEdit.setBooleanProperty(ErrorListPlugin.SHOW_UNDERLINES,
			showUnderlines.isSelected());
		jEdit.setBooleanProperty(ErrorListPlugin.SHOW_ICONS_IN_GUTTER,
			showIconsInGutter.isSelected());
	} //}}}

	//{{{ Private members
	private JCheckBox showOnError;
	private JCheckBox showErrorOverview;
	private JCheckBox autoCloseOnNoErrors;
	private JCheckBox autoRefocusTextArea; 
	private ColorWellButton warningColor;
	private ColorWellButton errorColor;
	private JRadioButton isInclusionFilter;
	private JRadioButton isExclusionFilter;
	private JTextField filenameFilter;
	private JCheckBox showUnderlines;
	private JCheckBox showIconsInGutter;
	//}}}
}
