/*
 * ConsoleShellPane.java - Shell interaction tab
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
import java.awt.event.*;
import java.awt.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;

public class ConsoleShellPane extends ConsoleOutputPane
{
	public ConsoleShellPane(ConsoleFrame frame, String shellName, Shell shell)
	{
		super(frame);

		this.shell = shell;
		this.shellName = shellName;

		command = new HistoryTextField("console." + shellName);
		command.addActionListener(new ActionHandler());
		add(BorderLayout.NORTH,command);

		shell.printInfoMessage(this);
	}

	public void run(String cmd)
	{
		// Add to history
		command.getModel().addItem(cmd);

		// Record the command
		InputHandler.MacroRecorder recorder = getView()
			.getInputHandler().getMacroRecorder();
		if(recorder != null)
		{
			recorder.actionPerformed(jEdit.getAction("console-shell"),shellName);
			recorder.actionPerformed(jEdit.getAction("console"),cmd);
		}

		if(cmd.trim().equalsIgnoreCase("clear"))
		{
			clear();
			return;
		}

		printInfo("> " + cmd);

		shell.stop();
		shell.execute(getView(),cmd,ConsoleShellPane.this);
	}

	public HistoryTextField getCommandField()
	{
		return command;
	}

	// private members
	private Shell shell;
	private String shellName;
	private HistoryTextField command;

	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			if(evt.getSource() == command)
			{
				String cmd = command.getText();
				if(cmd == null || cmd.length() == 0)
					return;

				command.setText(null);
				run(cmd);
			}
		}
	}
}
