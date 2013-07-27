/*
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
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
package projectviewer.config;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import org.gjt.sp.jedit.ServiceManager;

import projectviewer.gui.OptionPaneBase;
import static projectviewer.config.ExtensionManager.ManagedService;

/**
 * Config pane for managing available extensions. This allows the user
 * to effectivelly disable extensions to ProjectViewer.
 *
 * The extension is still instantiated since jEdit's "service"
 * mechanism makes it pretty hard to correlate names to the service's
 * implementation class, so as part of doing that PV will cause the
 * service to be instantiated. So this only controls whether the
 * extension is show by the PV UI, not whether it's loaded at all.
 *
 * @author	Marcelo Vanzin
 * @since	PV 3.0.0
 */

public class ExtensionConfigPane
	extends OptionPaneBase
	implements ItemListener
{

	private List<ServiceData> svcs;
	private JComboBox types;
	private ExtensionManager mgr = ExtensionManager.getInstance();
	private ProjectViewerConfig config = ProjectViewerConfig.getInstance();


	public ExtensionConfigPane()
	{
		super("projectviewer.optiongroup.extensions",
			  "projectviewer.options.ext_cfg");
	}


	public void itemStateChanged(ItemEvent e)
	{
		ServiceData sd = (ServiceData) e.getItem();
		if (e.getStateChange() == ItemEvent.SELECTED) {
			add(BorderLayout.CENTER, sd.list);
			revalidate();
		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
			remove(sd.list);
			revalidate();
		}
	}


	protected void _init()
	{
		JLabel help;
		List<ManagedService> services = mgr.getServices();
		Collections.sort(services, new ServiceComparator());

		setLayout(new BorderLayout());

		types = new JComboBox();
		types.setRenderer(new ServiceRenderer());
		add(BorderLayout.NORTH, types);

		svcs = new ArrayList<ServiceData>();
		for (ManagedService ms : services) {
			ServiceData sd = new ServiceData(ms);
			types.addItem(sd);
			svcs.add(sd);
		}

		types.setSelectedItem(0);
		types.addItemListener(this);
		add(BorderLayout.CENTER, svcs.get(0).list);

		help = new JLabel(prop("help"));
		add(BorderLayout.SOUTH, help);
	}


	protected void _save()
	{
		for (ServiceData sd : svcs) {
			String type = sd.service.getServiceClass().getName();
			if (sd.cbs != null) {
				for (JCheckBox cb : sd.cbs) {
					Class clazz = ServiceManager.getService(type, cb.getText())
					                            .getClass();
					if (cb.isSelected()) {
						config.enableExtension(type, clazz.getName());
					} else {
						config.disableExtension(type, clazz.getName());
					}
				}
			}
		}
		mgr.reloadExtensions();
		config.save();
	}


	private class ServiceComparator implements Comparator<ManagedService>
	{

		public int compare(ManagedService o1,
						   ManagedService o2)
		{
			return o1.getServiceName().compareTo(o2.getServiceName());
		}

	}


	private class ServiceData
	{

		ServiceData(ManagedService svc)
		{
			String type = svc.getServiceClass().getName();
			String[] names = ServiceManager.getServiceNames(type);
			Arrays.sort(names);
			this.service = svc;
			if (names == null) {
				cbs = null;
				list = new JList();
			} else {
				cbs = new JCheckBox[names.length];
				for (int i = 0; i < cbs.length; i++) {
					Class clazz = ServiceManager.getService(type, names[i])
					                            .getClass();
					cbs[i] = new JCheckBox(names[i]);
					cbs[i].setSelected(config.isExtensionEnabled(type,
																 clazz.getName()));
				}
				list = new JList(cbs);
				list.setCellRenderer(new ServiceListRenderer());
				list.addMouseListener(new ServiceListMouseHandler());
			}
		}

		JList list;
		JCheckBox cbs[];
		ManagedService service;
	}


	private class ServiceRenderer extends DefaultListCellRenderer
	{

		public Component getListCellRendererComponent(JList list,
													  Object value,
													  int index,
													  boolean isSelected,
													  boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list, value, index,
											   isSelected, cellHasFocus);
			setText(((ServiceData)value).service.getServiceName());
			return this;
		}

	}


	private class ServiceListRenderer extends DefaultListCellRenderer
	{

		public Component getListCellRendererComponent(JList list,
													  Object value,
													  int index,
													  boolean isSelected,
													  boolean cellHasFocus)
		{
			JCheckBox item = (JCheckBox) value;
			item.setBackground(getBackground());
			item.setForeground(getForeground());
			item.setFont(getFont());
			item.setFocusPainted(false);
			return item;
		}

	}


	private class ServiceListMouseHandler extends MouseAdapter
	{

		public void mousePressed(MouseEvent e)
		{
			JList list = (JList) e.getSource();
			int index = list.locationToIndex(e.getPoint());

			if (index != -1) {
				JCheckBox checkbox = (JCheckBox) list.getModel()
				                                     .getElementAt(index);
				checkbox.setSelected(!checkbox.isSelected());
				repaint();
			}
		}

	}

}

