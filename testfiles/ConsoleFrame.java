/*
 * ConsoleFrame.java - The console window
 * Copyright (C) 2000 Slava Pestov
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

import javax.swing.*;
import java.awt.*;
import org.gjt.sp.jedit.*;

public class ConsoleFrame extends JFrame
{
	public ConsoleFrame(View view)
	{
		this.view = view;

		tabs = new JTabbedPane();
		tabs.addTab("Hello",new JPanel());

		getContentPane().add(BorderLayout.CENTER,tabs);

		pack();
		GUIUtilities.loadGeometry(this,"console");
		show();
	}

	public void selectShell(String shell)
	{
	}

	public void run(String shell, String cmd)
	{
		// XXX: shell can be null
	}

	public void close()
	{
		setVisible(false);
	}

	// private members
	private View view;
	private JTabbedPane tabs;
}
