/*
 * BufferOptionsDialog.java - Dialog for buffer options of WhiteSpace plugin
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2011 Jarek Czekalski
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package whitespace;

// import {{{
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;
// }}}

public class BufferOptionsOptionPane extends AbstractOptionPane
									 implements ActionListener
	{

	// string constants {{{
	private static final String asOpts[] = {
		" space-highlight",
		">leading-space-highlight",
		">inner-space-highlight",
		">trailing-space-highlight",
		"-",
		" tab-highlight",
		">leading-tab-highlight",
		">inner-tab-highlight",
		">trailing-tab-highlight",
		"-",
		" whitespace-highlight",
		" block-highlight",
		" fold-highlight",
		" fold-tooltip",
		"-options.whitespace.onBufferSave.label",
		" remove-trailing-white-space",
		" soft-tabify-leading-white-space",
		" tabify-leading-white-space",
		" untabify-leading-white-space"
	};
	// }}}

	private Map<String, JCheckBox> cbmap;
	private WhiteSpaceModel model;

	// constructor {{{
	public BufferOptionsOptionPane(WhiteSpaceModel model) {
		super( jEdit.getProperty("whitespace.buffer-options-dialog") );
		cbmap = new HashMap<String, JCheckBox>();
		this.model = model;
		}
	//}}}

	// init method {{{
	protected void _init() {
		for (int i=0; i<asOpts.length; i++)
		{
			if (!asOpts[i].startsWith("-"))
			{
				String sOption = asOpts[i].substring(1);
				String sProp = "white-space.toggle-" + sOption + ".label";
				if (sOption.equals("space-highlight"))
					sProp = "options.whitespace.space-highlight.label";
				if (sOption.equals("tab-highlight"))
					sProp = "options.whitespace.tab-highlight.label";
				JCheckBox cb = new JCheckBox(jEdit.getProperty(sProp));
				WhiteSpaceModel.Option o = model.getOption(sOption);
				cb.setSelected(o.isEnabled());
				cb.addActionListener(this);
				cb.setName(sOption);
				cbmap.put(sOption, cb);
				addComponent(cb, GridBagConstraints.EAST);

				if (asOpts[i].startsWith(">"))
					moveRight(cb);
				
			}
			else
			{
				String sepProp = asOpts[i].substring(1);
				if (sepProp.length()==0)
					addSeparator();
				else
					addSeparator(sepProp);
			}
		}
		updateState(null, false);
	}
	// }}}

	// save method {{{
	protected void _save() {
		// jEdit.setBooleanProperty( "sidekick.java.showArgs", argumentsCheckBox.isSelected() );
		for (int i=0; i<asOpts.length; i++)
		{
			if (!asOpts[i].startsWith("-"))
			{
				String sOption = asOpts[i].substring(1);
				JCheckBox cb = cbmap.get(sOption);
				WhiteSpaceModel.Option o = model.getOption(sOption);
				o.setEnabled(cb.isSelected());
			}
		}
	}
	// }}}

	// actionPerformed method {{{
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox)e.getSource();
			String name = cb.getName();
			updateState(name, cb.isSelected());
		}
	}
	// }}}

	// updateState method {{{
	/** updates state of the checkboxes - enabled/disabled
	 *	@param name The name of the checkbox that triggered the method.
	 *		   May be null.
	 *	@param on If name is specified, then it holds the value of this checkbox
	 */
	private void updateState(String name, boolean on)
	{
		boolean bTabs = cbmap.get("tab-highlight").isSelected();
		cbmap.get("leading-tab-highlight").setEnabled(bTabs);
		cbmap.get("inner-tab-highlight").setEnabled(bTabs);
		cbmap.get("trailing-tab-highlight").setEnabled(bTabs);

		boolean bSpaces = cbmap.get("space-highlight").isSelected();
		cbmap.get("leading-space-highlight").setEnabled(bSpaces);
		cbmap.get("inner-space-highlight").setEnabled(bSpaces);
		cbmap.get("trailing-space-highlight").setEnabled(bSpaces);

		if (name != null && name.indexOf("tabify")>=0 && on)
		{
			cbmap.get("soft-tabify-leading-white-space").setSelected(false);
			cbmap.get("tabify-leading-white-space").setSelected(false);
			cbmap.get("untabify-leading-white-space").setSelected(false);
			cbmap.get(name).setSelected(true);
		}
	}
	//}}}

	// moveRight method {{{
	/** indents the component on its panel */
	private void moveRight(Component c)
	{
		LayoutManager layout = getLayout();
		if (layout instanceof GridBagLayout) {
			GridBagLayout bag = (GridBagLayout)layout;
			GridBagConstraints cons = bag.getConstraints(c);
			cons.insets.left = c.getFontMetrics(c.getFont()).getHeight();
			bag.setConstraints(c, cons);
		}
	}
	//}}}
}



