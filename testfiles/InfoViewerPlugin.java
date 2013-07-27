/*
 * InfoViewerPlugin.java - an info viewer plugin for jEdit
 * Copyright (C) 1999-2002 Dirk Moebius
 * based on the original jEdit HelpViewer by Slava Pestov.
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
 * 
 * Updated for Jedit 4.3 on 2005-09-09 by Alan Ezust
 * 
 */

package infoviewer;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.OptionGroup;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.help.HelpViewerInterface;
import org.gjt.sp.jedit.help.HelpViewer;
import org.gjt.sp.jedit.io.FileVFS;
import org.gjt.sp.util.Log;

public class InfoViewerPlugin extends EditPlugin
{

	// begin EditPlugin implementation
	public void start()
	{

	}

	static private boolean firstTime = true;
	public static void showHelp()
	{
		if (jEdit.getBooleanProperty("infoviewer.useforhelp")) 
		{
			View v = jEdit.getActiveView();
			DockableWindowManager dwm = v.getDockableWindowManager();
			dwm.showDockableWindow("helpviewer");
			if (firstTime) 
			{
				JComponent dockable = dwm.getDockable("helpviewer");
				dockable.setVisible(true);
				HelpViewerInterface viewer = (HelpViewerInterface) dockable;
				viewer.gotoURL("welcome.html", false, -1);
				firstTime = false;
			}
			dwm.showDockableWindow("helpviewer");
		}
		else new HelpViewer();
	}
	// end EditPlugin implementation
	/**
	 * Open selected text with preferred browser. The selected text should
	 * be an URL.
	 */
	public static void openSelectedText(View view)
	{
		String selection = view.getTextArea().getSelectedText();

		if (selection == null)
			GUIUtilities.error(view, "infoviewer.error.noselection", null);
		else
			openURL(view, selection);
	}

	/**
	 * Open current jEdit buffer with preferred browser.
	 */
	public static void openCurrentBuffer(View view)
	{
		Buffer buffer = view.getBuffer();
		String url = buffer.getPath();

		if (buffer.getVFS() instanceof FileVFS)
			url = "file:" + url;

		if (buffer.isDirty())
		{
			int result = GUIUtilities.confirm(view, "notsaved", new String[] { buffer
				.getName() }, javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
				javax.swing.JOptionPane.WARNING_MESSAGE);

			if (result == javax.swing.JOptionPane.YES_OPTION)
				buffer.save(view, null);
			else if (result != javax.swing.JOptionPane.NO_OPTION)
				return;
		}

		openURL(view, url);
	}

	/**
	 * Open an URL with the preferred browser.
	 * 
	 * @param view
	 *                where to display error messages.
	 * @param url
	 *                the URL. If null or empty, open an empty browser
	 *                window.
	 */
	public static void openURL(View view, String url)
	{
		String browsertype = jEdit.getProperty("infoviewer.browsertype");

		if (url == null)
			url = "";

		if (url.startsWith("jeditresource:"))
			browsertype = "internal";

		Log.log(Log.DEBUG, InfoViewerPlugin.class, "(" +
                 browsertype + "): openURL: "	+ url);

		if ("external".equals(browsertype))
			openURLWithOtherBrowser(view, url);
		else if ("class".equals(browsertype))
			openURLWithJavaMethod(view, url);
		else if ("firefox".equals(browsertype))
			openURLWithFirefox(view, url);
		else
			openURLWithInfoViewer(view, url);
	}

	public static void openURLWithInfoViewer(View view, String url)
	{
		DockableWindowManager mgr = view.getDockableWindowManager();
		mgr.showDockableWindow("infoviewer");
		InfoViewer iv = (InfoViewer) mgr.getDockable("infoviewer");
		iv.gotoURL(url, true, 0);
	}

	public static void openURLWithFirefox(View view, String url)
	{
		String[] args = new String[4];
		if (OperatingSystem.isWindows()) 
		{
			args[0]="cmd";
			args[1]="/C";
		} 
		else 
		{
			args[0] = "sh";
			args[1] = "-c";
		}
		args[2] = "firefox";
		args[3] = url;
		execProcess(view, args);
	}

	public static void openURLWithOtherBrowser(View view, String url)
	{
		String cmd = jEdit.getProperty("infoviewer.otherBrowser");
		String[] args = convertCommandString(cmd, url);
		execProcess(view, args);
	}

	public static void openURLWithJavaMethod(View view, String url)
	{
		String clazz = jEdit.getProperty("infoviewer.class");
		String method = jEdit.getProperty("infoviewer.method");
		Class c = null;
		Object obj = null;

		try
		{
			c = Class.forName(clazz);
		}
		catch (Throwable e)
		{
			GUIUtilities.error(view, "infoviewer.error.classnotfound",
				new Object[] { clazz });
			return;
		}

		if (method == null || (method != null && method.length() == 0))
		{
			// no method: try to find URL or String or empty
			// constructor
			Constructor constr = null;
			try
			{
				constr = c.getConstructor(new Class[] { URL.class });
				if (constr != null)
					obj = constr.newInstance(new Object[] { new URL(url) });
			}
			catch (Exception ex)
			{
				Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
			}

			if (obj == null)
			{
				try
				{
					constr = c.getConstructor(new Class[] { String.class });
					if (constr != null)
						obj = constr.newInstance(new Object[] { url });
				}
				catch (Exception ex)
				{
					Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
				}
			}

			if (obj == null)
			{
				try
				{
					constr = c.getConstructor(new Class[0]);
					if (constr != null)
						obj = constr.newInstance(new Object[0]);
				}
				catch (Exception ex)
				{
					Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
				}
			}

			if (obj == null)
			{
				GUIUtilities.error(view, "infoviewer.error.classnotfound",
					new Object[] { clazz });
				return;
			}
		}
		else
		{
			// there is a method name:
			Method meth = null;
			boolean ok = false;

			try
			{
				meth = c.getDeclaredMethod(method, new Class[] { URL.class });
				if (meth != null)
				{
					obj = meth.invoke(null, new Object[] { new URL(url) });
					ok = true;
				}
			}
			catch (Exception ex)
			{
				Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
			}

			if (!ok)
			{
				try
				{
					meth = c.getDeclaredMethod(method,
						new Class[] { String.class });
					if (meth != null)
					{
						obj = meth.invoke(null, new Object[] { url });
						ok = true;
					}
				}
				catch (Exception ex)
				{
					Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
				}
			}

			if (!ok)
			{
				try
				{
					meth = c.getDeclaredMethod(method, new Class[0]);
					if (meth != null)
					{
						obj = meth.invoke(null, new Object[0]);
						ok = true;
					}
				}
				catch (Exception ex)
				{
					Log.log(Log.DEBUG, InfoViewerPlugin.class, ex);
				}
			}

			if (!ok)
			{
				GUIUtilities.error(view, "infoviewer.error.methodnotfound",
					new Object[] { clazz, method });
				return;
			}
		}

		if (obj != null)
		{
			if (obj instanceof Window)
			{
				((Window) obj).setVisible(true);
			}
			else if (obj instanceof JComponent)
			{
				JFrame f = new JFrame("Infoviewer JWrapper");
				f.getContentPane().add((JComponent) obj);
				f.pack();
				f.setVisible(true);
			}
			else if (obj instanceof Component)
			{
				Frame f = new Frame("Infoviewer Wrapper");
				f.add((Component) obj);
				f.pack();
				f.setVisible(true);
			}
		}
	}

	/**
	 * converts the command string, which may contain "$u" as placeholders
	 * for an url, into an array of strings, tokenized at the space char.
	 * Characters in the command string may be escaped with '\\', which in
	 * the case of space prevents tokenization.
	 * 
	 * @param command
	 *                the command string.
	 * @param url
	 *                the URL, as String.
	 * @return the space separated parts of the command string, as array of
	 *         Strings.
	 */
	private static String[] convertCommandString(String command, String url)
	{
		Vector args = new Vector();
		StringBuffer arg = new StringBuffer();
		boolean foundDollarU = false;
		boolean inQuotes = false;
		int end = command.length() - 1;

		for (int i = 0; i <= end; i++)
		{
			char c = command.charAt(i);
			switch (c)
			{
			case '$':
				if (i == end)
					arg.append(c);
				else
				{
					char c2 = command.charAt(++i);
					if (c2 == 'u')
					{
						arg.append(url);
						foundDollarU = true;
					}
					else
					{
						arg.append(c);
						arg.append(c2);
					}
				}
				break;

			case '"':
				inQuotes = !inQuotes;
				break;

			case ' ':
				if (inQuotes)
					arg.append(c);
				else
				{
					String newArg = arg.toString().trim();
					if (newArg.length() > 0)
						args.addElement(newArg);
					arg = new StringBuffer();
				}
				break;

			case '\\': // quote char, only for backwards
					// compatibility
				if (i == end)
					arg.append(c);
				else
				{
					char c2 = command.charAt(++i);
					if (c2 != '\\')
						arg.append(c);
					arg.append(c2);
				}
				break;

			default:
				arg.append(c);
				break;
			}
		}

		String newArg = arg.toString().trim();
		if (newArg.length() > 0)
			args.addElement(newArg);

		if (!foundDollarU && url.length() > 0)
			args.addElement(url);

		String[] result = new String[args.size()];
		args.copyInto(result);

		for (int i = 0; i < result.length; i++)
			Log.log(Log.DEBUG, InfoViewerPlugin.class, "args[" + i + "]=" + result[i]);

		return result;
	}

	private static void execProcess(View view, String[] args)
	{
		try
		{
			Runtime.getRuntime().exec(args);
		}
		catch (Exception ex)
		{
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < args.length; i++)
			{
				buf.append(args[i]);
				buf.append('\n');
			}
			GUIUtilities.error(view, "infoviewer.error.invokeBrowser", new Object[] {
				ex, buf.toString() });
		}
	}

}
