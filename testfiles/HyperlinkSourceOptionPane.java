/*
 * HyperlinkSourceOptionPane.java - Hyperlink source configuration panel
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

import javax.swing.*;
import java.awt.*;

import org.gjt.sp.jedit.*;
//}}}


/**
 * An option pane to configure the mode - hyperlink source associations.
 *
 * @author Matthieu Casanova
 * @version $Id: Buffer.java 8190 2006-12-07 07:58:34Z kpouer $
 */
public class HyperlinkSourceOptionPane extends AbstractOptionPane
{
	//{{{ HyperlinkSourceOptionPane constructor
	public HyperlinkSourceOptionPane()
	{
		super("hyperlink.source");
	} //}}}

	//{{{ _init() method
	@Override
    public void _init()
	{
		setLayout(new BorderLayout());
		JTable table = ModeServiceAssociationTableModel.getTable(HyperlinkSource.SERVICE,
									 jEdit.getProperty("options.hyperlink.source.source"),
									 jEdit.getProperty(HyperlinkSource.DEFAULT_PROPERTY+".label"),
									 HyperlinkSource.PROPERTY);
		tableModel = (ModeServiceAssociationTableModel) table.getModel();
		Dimension d = table.getPreferredSize();
		d.height = Math.min(d.height,50);
		JScrollPane scroller = new JScrollPane(table);
		scroller.setPreferredSize(d);
		add(BorderLayout.CENTER, scroller);
	} //}}}

	//{{{ _save() method
	@Override
    public void _save()
	{
		tableModel.save();
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private ModeServiceAssociationTableModel tableModel;
	//}}}

	//}}}
} //}}}