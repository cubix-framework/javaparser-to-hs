/*
Copyright (C) 2007  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package perspective;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.PerspectiveManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.XMLUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Plugin extends EditPlugin {
	static public final String OPTION_PREFIX = "options.debugger.";
	public void start()	{
	}
	public void stop() {
	}
	
	static public void save(View view) {
		String name = JOptionPane.showInputDialog(null, "Perspective name:",
				"Save perspective", JOptionPane.QUESTION_MESSAGE);
		if (name == null)
			return;
		DockableWindowManager dockMan = view.getDockableWindowManager();
		Hashtable<String, String[]> dockables = new Hashtable<String, String[]>();
		dockables.put(DockableWindowManager.LEFT,
				dockMan.getLeftDockingArea().getDockables());
		dockables.put(DockableWindowManager.RIGHT,
				dockMan.getRightDockingArea().getDockables());
		dockables.put(DockableWindowManager.TOP,
				dockMan.getTopDockingArea().getDockables());
		dockables.put(DockableWindowManager.BOTTOM,
				dockMan.getBottomDockingArea().getDockables());
		PrintWriter w;
		try {
			w = new PrintWriter(new FileWriter(getConfigFile(name)));
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			w.println();
			w.println("<perspective>");
			Enumeration<String> keys = dockables.keys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				w.println(" <" + key + ">");
				String [] windows = dockables.get(key);
				for (int i = 0; i < windows.length; i++) {
					String window = windows[i];
					boolean visible = dockMan.isDockableWindowVisible(window);
					w.println("  <window name=\"" + window +
							"\" visible=\"" + visible + "\" />");
				}
				w.println(" </" + key + ">");
			}
			w.println("</perspective>");
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static String getConfigFile(String name) {
		return getConfigDirectory() + File.separator + name + ".xml";
	}
	private static String getConfigDirectory() {
		String dir = jEdit.getSettingsDirectory() + File.separator + "perspective";
		File f = new File(dir);
		if (! f.exists())
			f.mkdir();
		return dir;
	}

	static private void remove(String [] dockables) {
		for (int i = 0; i < dockables.length; i++)
			jEdit.setProperty(dockables[i] + ".dock-position",
					DockableWindowManager.FLOATING);
	}
	static public void load(View view) {
		File dir = new File(getConfigDirectory());
		if (! dir.canRead())
			return;
		String[] names = dir.list(new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".xml");
			}
		});
		String[] perspectives = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			perspectives[i] = names[i].substring(0, names[i].length() - 4);
		}
		int sel = JOptionPane.showOptionDialog(jEdit.getActiveView(),
				"Select perspective:", "Load perspective",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, perspectives, null);
		if (sel == JOptionPane.CLOSED_OPTION)
			return;
		String selected = getConfigFile(perspectives[sel]);
		PerspectiveHandler handler = new PerspectiveHandler();
		try
		{
			XMLUtilities.parseXML(new FileInputStream(selected), handler);
		}
		catch(IOException e)
		{
			Log.log(Log.ERROR,PerspectiveManager.class,e);
		}
		DockableWindowManager dockMan = view.getDockableWindowManager();
		// Remove all existing dockables
		remove(dockMan.getLeftDockingArea().getDockables());
		remove(dockMan.getRightDockingArea().getDockables());
		remove(dockMan.getTopDockingArea().getDockables());
		remove(dockMan.getBottomDockingArea().getDockables());
		// Now put all the dockables from the loaded perspective
		Hashtable<String, Vector<String> > dockables = handler.getDockables(); 
		Enumeration<String> keys = dockables.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			Vector<String> windows = dockables.get(key);
			for (int i = 0; i < windows.size(); i++)
			{
				String name = windows.get(i);
				jEdit.setProperty(name + ".dock-position", key);
				dockMan.hideDockableWindow(name);
			}
		}
		jEdit.propertiesChanged();
		// Restore the "visible" state
		HashSet<String> visible = handler.getVisible();
		Iterator<String> it = visible.iterator();
		while (it.hasNext()) {
			String name = it.next();
			dockMan.showDockableWindow(name);
		}
	}
	
	static class PerspectiveHandler extends DefaultHandler {

		private String area = null;
		private Hashtable<String, Vector<String> > dockables =
			new Hashtable<String, Vector<String> >();
		private static final HashSet<String> areas = new HashSet<String>();
		private HashSet<String> visible = new HashSet<String>();
		
		PerspectiveHandler() {
			areas.add(DockableWindowManager.LEFT);
			areas.add(DockableWindowManager.RIGHT);
			areas.add(DockableWindowManager.TOP);
			areas.add(DockableWindowManager.BOTTOM);
		}
		@Override
		public void startElement(String nsURI, String localName,
				String qualifiedName, Attributes attr) throws SAXException
		{
			if (areas.contains(localName)) {
				area = localName;
				return;
			}
			if (localName.equalsIgnoreCase("window")) {
				String name = attr.getValue("name");
				if (name == null)
					return;
				if (! dockables.containsKey(area))
					dockables.put(area, new Vector<String>());
				dockables.get(area).add(name);
				String isVisible = attr.getValue("visible");
				if (isVisible == null)
					return;
				if (isVisible.equals("true"))
					visible.add(name);
			}
		}
		public Hashtable<String, Vector<String> > getDockables() {
			return dockables;
		}
		public HashSet<String> getVisible() {
			return visible;
		}
	}
}
