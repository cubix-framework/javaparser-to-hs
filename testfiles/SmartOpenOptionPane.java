/*
 * jEdit - Programmer's Text Editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright © 2011-2012 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.kpouer.jedit.smartopen;

//{{{ Imports
import java.awt.GridBagConstraints;
import java.util.regex.Pattern;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import common.gui.VFSPathFileList;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.StandardUtilities;
//}}}

/**
 * @author Matthieu Casanova
 */
public class SmartOpenOptionPane extends AbstractOptionPane
{
	private JCheckBox inMemoryIndex;
	private JCheckBox indexProject;
	private JCheckBox toolbar;
	private JTextField includeFilesTF;
	private JTextField excludeFilesTF;
	private static Pattern include;
	private static Pattern exclude;

	private VFSPathFileList paths;

	//{{{ SmartOpenOptionPane constructor
	public SmartOpenOptionPane()
	{
		super("smartopen");
	} //}}}

	//{{{ _init() method
	@Override
	protected void _init()
	{
		setBorder(new EmptyBorder(5, 5, 5, 5));
		toolbar = new JCheckBox(jEdit.getProperty("options.smartopen.toolbar.label"));
		toolbar.setSelected(jEdit.getBooleanProperty("options.smartopen.toolbar"));
		addComponent(toolbar);
		addSeparator();
		paths = new VFSPathFileList("options.ancestor.paths");
		addComponent(paths, GridBagConstraints.BOTH);
		includeFilesTF = new JTextField(jEdit.getProperty("options.smartopen.IncludeGlobs"));
		addComponent(jEdit.getProperty("options.smartopen.IncludeGlobs.label"), includeFilesTF);
		excludeFilesTF = new JTextField(jEdit.getProperty("options.smartopen.ExcludeGlobs"));
		addComponent(jEdit.getProperty("options.smartopen.ExcludeGlobs.label"), excludeFilesTF);

		indexProject = new JCheckBox(jEdit.getProperty("options.smartopen.projectindex.label"));
		indexProject.setSelected(jEdit.getBooleanProperty("options.smartopen.projectindex"));
		addComponent(indexProject);

		inMemoryIndex = new JCheckBox(jEdit.getProperty("options.smartopen.memoryindex.label"));
		inMemoryIndex.setSelected(jEdit.getBooleanProperty("options.smartopen.memoryindex"));
		addComponent(inMemoryIndex);
	} //}}}

	//{{{ _save() method
	@Override
	protected void _save()
	{
		jEdit.setProperty("options.smartopen.IncludeGlobs", includeFilesTF.getText());
		jEdit.setProperty("options.smartopen.ExcludeGlobs", excludeFilesTF.getText());
		jEdit.setBooleanProperty("options.smartopen.projectindex", indexProject.isSelected());
		jEdit.setBooleanProperty("options.smartopen.toolbar", toolbar.isSelected());
		jEdit.setBooleanProperty("options.smartopen.memoryindex", inMemoryIndex.isSelected());
		paths.save();
		updateFilter();
	} //}}}

	//{{{ globToPattern() method
	private static Pattern globToPattern(String filter)
	{
		String[] parts = filter.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String part : parts)
		{
			if (sb.length() > 0)
				sb.append('|');
			String regexp = StandardUtilities.globToRE(part);
			sb.append(regexp);
		}
		return Pattern.compile(sb.toString());
	} //}}}

	//{{{ accept() method
	public static boolean accept(CharSequence path)
	{
		if (include == null || exclude == null)
			updateFilter();
		return include.matcher(path).matches() &&
			!exclude.matcher(path).matches();
	} //}}}

	//{{{ updateFilter() method
	private static void updateFilter()
	{
		include = globToPattern(jEdit.getProperty("options.smartopen.IncludeGlobs"));
		exclude = globToPattern(jEdit.getProperty("options.smartopen.ExcludeGlobs"));
	} //}}}
}
