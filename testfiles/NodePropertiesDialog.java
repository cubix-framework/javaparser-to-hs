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
package projectviewer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.EnhancedDialog;

import projectviewer.config.ExtensionManager;
import projectviewer.vpt.VPTNode;

/**
 *	A dialog that shows a tabbed pane with information about the
 *	selected node. PV provides a default properties pane that shows
 *	available information about the VFS node (URL, size, etc).
 *
 *	Other plugins can extend the dialog by providing implementations
 *	of "NodePropertyProvider" through the jEdit services mechanism.
 *
 *  @author		Marcelo Vanzin
 *	@version	$Id: NodePropertiesDialog.java 12668 2008-05-16 06:21:27Z vanza $
 *	@since		PV 3.0.0
 */
public class NodePropertiesDialog extends EnhancedDialog
									implements ActionListener
{

	private VPTNode node;

	public NodePropertiesDialog(VPTNode node,
								Component parent)
	{
		super(JOptionPane.getFrameForComponent(parent),
			  jEdit.getProperty("projectviewer.node_properties_dlg.title",
								new Object[] { node.getName() }),
			  true);
		this.node = node;

		/* Load the providers into the tab pane. */
		List<Object> exts = ExtensionManager.getInstance()
											.loadExtensions(NodePropertyProvider.class);
		NodePropertyProvider general = new GeneralNodeProperties();
		JTabbedPane tabs = new JTabbedPane();

		if (general.isNodeSupported(node)) {
			tabs.add(general.getTitle(), new ProviderWrapper(general));
		}
		if (exts != null) {
			for (Object o : exts) {
				NodePropertyProvider npp = (NodePropertyProvider) o;
				if (npp.isNodeSupported(node)) {
					tabs.add(npp.getTitle(), new ProviderWrapper(npp));
				}
			}
		}

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(BorderLayout.CENTER, tabs);

		/* Add the "close" button. */
		JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton closeBtn = new JButton(jEdit.getProperty("projectviewer.node_properties_dlg.close"));
		closeBtn.addActionListener(this);
		closePanel.add(closeBtn);
		getContentPane().add(BorderLayout.SOUTH, closePanel);

		pack();
		GUIUtilities.loadGeometry(this, getClass().getName());
	}


	public void ok()
	{
		GUIUtilities.saveGeometry(this, getClass().getName());
		dispose();
	}


	public void cancel()
	{
		ok();
	}


	public void actionPerformed(ActionEvent ae)
	{
		ok();
	}

	/**	Wrapper for a provider, to only instantiate the UI when needed. */
	private class ProviderWrapper extends JPanel
	{
		private boolean instantiated;
		private NodePropertyProvider provider;

		public ProviderWrapper(NodePropertyProvider provider)
		{
			super(new BorderLayout());
			this.instantiated = false;
			this.provider = provider;
		}

		public void setVisible(boolean visible)
		{
			if (visible && !instantiated) {
				add(BorderLayout.CENTER, provider.getComponent(node));
			}
			super.setVisible(visible);
		}

	}

}

