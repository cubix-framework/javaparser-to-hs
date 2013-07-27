/*
 * MacroListDownloadProgress.java - Macro list download progress dialog
 * Copyright (C) 2002 Carmine Lucarelli
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

package macroManager;


import javax.swing.border.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.InterruptedIOException;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.xml.sax.SAXException;

class MacroListDownloadProgress extends JDialog
{
	MacroListDownloadProgress(Dialog dialog, boolean refresh)
	{
		super(JOptionPane.getFrameForComponent(dialog), jEdit.getProperty("macro-list.progress.title"),true);

		this.window = window;
		this.refresh = refresh;

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JLabel caption = new JLabel(jEdit.getProperty("macro-list.progress.caption"));
		caption.setBorder(new EmptyBorder(0,0,12,0));
		content.add(BorderLayout.NORTH,caption);

		Box box = new Box(BoxLayout.X_AXIS);
		box.add(Box.createGlue());
		JButton stop = new JButton(jEdit.getProperty("macro-list.progress.stop"));
		stop.addActionListener(new ActionHandler());
		stop.setMaximumSize(stop.getPreferredSize());
		box.add(stop);
		box.add(Box.createGlue());
		content.add(BorderLayout.CENTER,box);

		addWindowListener(new WindowHandler());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setSize(50, 50);
		pack();
		setLocationRelativeTo(window);
		setResizable(false);
		show();
	}

	MacroList getMacroList()
	{
		return list;
	}

	// private members
	private Frame window;
	private MacroList list;
	private DownloadThread thread;

	private boolean refresh;

	class DownloadThread extends Thread
	{
		public void run()
		{
			try
			{
				list = new MacroList(refresh);
				dispose();
			}
			catch(SAXException xe)
			{
				dispose();

				//int line = xe.getLine();
				String path = jEdit.getProperty("macro-manager.url");
				String message = xe.getMessage();
				Log.log(Log.ERROR,this,path + ": " + message);
				// String[] pp = { path, String.valueOf(line), message };
				// GUIUtilities.error(window,"macro-list.xmlerror",pp);
			}
			catch(Exception e)
			{
				dispose();

				Log.log(Log.ERROR,this,e);
				String[] pp = { e.toString() };
				GUIUtilities.error(window,"macro-list.ioerror",pp);
			}
		}
	}

	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			thread.stop();
			dispose();
		}
	}

	class WindowHandler extends WindowAdapter
	{
		boolean done;

		public void windowOpened(WindowEvent evt)
		{
			if(done)
				return;

			done = true;
			thread = new DownloadThread();
			thread.start();
		}

		public void windowClosing(WindowEvent evt)
		{
			thread.stop();
		}
	}
}
