/*
 *  JythonDocker.java - Jython docker class
 *  Copyright (C) 2001 Carlos Quiroz
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

package jython;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * JythonDocker used for the new docking API. It just creates a window which
 * can be detected by the Dockables API. The plugin itself is responsible to
 * add the content of the panel.
 *
 * The class will call python function. It is exepected that the function takes
 * two arguments, view and the docker itself
 *
 * @author     Carlos Quiroz
 * @version    $Id: JythonDocker.java,v 1.3 2003/02/19 23:33:29 fruhstuck Exp $
 */
public class JythonDocker extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a JythonDocker which will call the function "function" in the given module, dir
	 * and plugin. It will display a standard message while loading
	 *
	 * @param plugin target Plugin
	 * @param dir where to find the module
	 * @param module the module
	 * @param function target function
	 * @param View target view
	 */
	public JythonDocker(String plugin, String dir, String module, String function, View view) {
		this(jEdit.getProperty("jython.docker.loading"), plugin, dir, module, function, view);
	}

	/**
	 * Creates a JythonDocker which will call the function "function" in the given module, dir
	 * and plugin. It will display a custom message while loading
	 *
	 * @param text custom message displayed during plugin loading
	 * @param plugin target Plugin
	 * @param dir where to find the module
	 * @param module the module
	 * @param function target function
	 * @param View target view
	 */
	public JythonDocker(String text, final String plugin, final String dir, final String module, final String function, final View view) {
		final JLabel label = new JLabel(text, JLabel.CENTER);
		setLayout(new BorderLayout());
		add(label);
		new Thread(new Runnable() {
			public void run() {
				jython.JythonExecutor.execPlugin(plugin,
					dir, module, function, new Object[] {view, JythonDocker.this});
				JythonDocker.this.remove(label);
			}
		}).start();
	}
}

// :indentSize=4:lineSeparator=\n:noTabs=false:tabSize=4:
