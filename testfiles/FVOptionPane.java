/*
 * FVOptionPane.java
 * Copyright (C) Sun Aug 20 MSD 2006 Denis Koryavov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package foldviewer;

import java.awt.GridBagConstraints;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.gui.ColorWellButton;
import org.gjt.sp.jedit.jEdit;

public class FVOptionPane extends AbstractOptionPane {
	private ColorWellButton bgColor;
	
	//{{{ constructor.
	public FVOptionPane() {
		super("foldviewer");
	} 
	//}}}
	
	//{{{ _init method.
	public void _init() {
		bgColor = new ColorWellButton(jEdit
			.getColorProperty("options.foldviewer.bgcolor"));
		addComponent(
			jEdit.getProperty("options.foldviewer.bgcolor.label"),
			bgColor, GridBagConstraints.HORIZONTAL);
	} //}}}
	
	//{{{ _save method.
	public void _save() {
		jEdit.setColorProperty("options.foldviewer.bgcolor", 
			bgColor.getSelectedColor());
	} //}}}
	
	// :collapseFolds=1:tabSize=8:indentSize=8:folding=explicit: 
} 

