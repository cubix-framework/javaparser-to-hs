/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * ContextOptionPane.java - Context menu options panel
 * Copyright (C) 2000, 2001 Slava Pestov
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

package pvdebug;

//{{{ Imports
import java.awt.BorderLayout;

import javax.swing.JLabel;

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.AbstractOptionPane;
//}}}

/**
 *	An option pane to be used for debugging purposes.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: DummyOptionPane.java 14867 2009-03-30 05:53:17Z vanza $
 */
public class DummyOptionPane extends AbstractOptionPane {

	//{{{ +DummyOptionPane(String) : <init>
	public DummyOptionPane(String name) {
		super(name);
	}
	//}}}

	//{{{ #_init() : void
	protected void _init()
	{
		setLayout(new BorderLayout());

		JLabel caption = new JLabel("Dummy option pane");
		add(BorderLayout.CENTER,caption);
	} //}}}

	//{{{ #_save() : void
	protected void _save() {
		Log.log(Log.ERROR, this, "Dummy save");
	} //}}}

}

