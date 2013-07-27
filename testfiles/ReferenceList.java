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

package browser;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JPanel;

import org.gjt.sp.jedit.View;

@SuppressWarnings("serial")
public class ReferenceList extends GlobalResultsView {
	
	static private HashMap<View, ReferenceList> viewMap =
		new HashMap<View, ReferenceList>();
	
	static public JPanel getViewDockable(View view, String position) {
		ReferenceList instance = instanceFor(view);
		JPanel p = new JPanel(new BorderLayout());
		p.add(instance, BorderLayout.CENTER);
		return p;
	}
	static public ReferenceList instanceFor(View view) {
		ReferenceList instance = viewMap.get(view);
		if (instance == null) {
			instance = new ReferenceList(view);
			viewMap.put(view, instance);
		}
		return instance;
	}
	
	private ReferenceList(final View view) {
		super(view);
	}

	@Override
	protected String getParam() {
		return "-r -x -a";
	}
}
