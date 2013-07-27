/*
 * HyperlinkOptionPane.java - Hyperlink options panel
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2007 Matthieu Casanova
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

package gatchan.jedit.hyperlinks;

//{{{ Imports
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.gui.ColorWellButton;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.StandardUtilities;
import org.gjt.sp.util.StringList;

import javax.swing.*;
import java.util.Arrays;
//}}}


/**
 * @author Matthieu Casanova
 * @version $Id: Buffer.java 8190 2006-12-07 07:58:34Z kpouer $
 */
public class HyperlinkOptionPane extends AbstractOptionPane
{
	//{{{ HyperlinkSourceOptionPane constructor
	public HyperlinkOptionPane()
	{
		super("hyperlink");
	} //}}}

	//{{{ _init() method
	@Override
    public void _init()
	{
		hyperlinkColor = new ColorWellButton(jEdit.getColorProperty("options.hyperlink.color.value"));
		String[] serviceNames = ServiceManager.getServiceNames(HyperlinkSource.SERVICE);
		Arrays.sort(serviceNames, new StandardUtilities.StringCompare(true));
		StringList sl = new StringList();
		sl.add(HyperlinkSource.NONE);
		sl.addAll(serviceNames);

		defaultSource = new JComboBox(sl.toArray());
		String defaultSourceName = jEdit.getProperty(HyperlinkSource.DEFAULT_PROPERTY);
		defaultSource.setSelectedItem(defaultSourceName);
		addComponent(jEdit.getProperty("options.hyperlink.color.label"), hyperlinkColor);
		addComponent(jEdit.getProperty(HyperlinkSource.DEFAULT_PROPERTY + ".label"), defaultSource);
	} //}}}


	//{{{ _save() method
	@Override
    public void _save()
	{
		jEdit.setColorProperty("options.hyperlink.color.value", hyperlinkColor.getSelectedColor());
		String selected = (String) defaultSource.getSelectedItem();
		if (selected == HyperlinkSource.NONE)
		{
			jEdit.unsetProperty(HyperlinkSource.DEFAULT_PROPERTY);
		}
		else
		{
			jEdit.setProperty(HyperlinkSource.DEFAULT_PROPERTY, selected);
		}
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private ColorWellButton hyperlinkColor;
	private JComboBox defaultSource;
	//}}}

	//}}}
} //}}}