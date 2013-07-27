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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;

import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.Log;

import projectviewer.VFSHelper;
import projectviewer.vpt.VPTDirectory;
import projectviewer.vpt.VPTFile;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;

/**
 *	Node property provider that shows basic information about projects
 *	and VFS nodes.
 *
 *  @author		Marcelo Vanzin
 *	@version	$Id: GeneralNodeProperties.java 16366 2009-10-20 07:26:31Z vanza $
 *	@since		PV 3.0.0
 */
class GeneralNodeProperties implements NodePropertyProvider
{

	public boolean isNodeSupported(VPTNode node)
	{
		return node.isProject() || node.isFile() || node.isDirectory();
	}


	public String getTitle()
	{
		return getProperty("title");
	}


	public Component getComponent(VPTNode node)
	{
		return new NodePropertiesPane(node);
	}


	private static String getProperty(String name)
	{
		return jEdit.getProperty("projectviewer.action.properties.general." + name);
	}


	private static class NodePropertiesPane extends JPanel
											implements ActionListener
	{
		private final DecimalFormat fmt;
		private final GridBagLayout gbl;
		private final GridBagConstraints gbc;
		private final VPTNode node;

		private JLabel nodeSzLabel;

		public NodePropertiesPane(VPTNode node)
		{
			fmt = new DecimalFormat("#,###");
			gbl = new GridBagLayout();
			gbc = new GridBagConstraints();
			this.node = node;

			setLayout(gbl);
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.weightx = 0.0;

			/* Top pane: icon and node name. */
			JLabel nodeIcon = new JLabel(node.getIcon(false));
			nodeIcon.setMinimumSize(new java.awt.Dimension(64, 64));
			nodeIcon.setPreferredSize(new java.awt.Dimension(64, 64));
			_add(nodeIcon, 1);

			JLabel nodeName = new JLabel(node.getName());
			_add(nodeName, GridBagConstraints.REMAINDER);

			/* Horizontal rule separating top pane. */
			JSeparator sep = new JSeparator();
			_add(sep, GridBagConstraints.REMAINDER);

			VFSFile f;
			try {
				f = VFSHelper.getFile(node.getNodePath());
			} catch (java.io.IOException ioe) {
				Log.log(Log.WARNING, this, ioe);
				f = null;
			}

			/* Node-specific information. */
			if (node.isProject()) {
				VPTProject p = (VPTProject) node;
				JLabel text;

				/* Root path. */
				text = new JLabel(getProperty("root_path"));
				_add(text, 1);

				text = new JLabel(p.getRootPath());
				_add(text, GridBagConstraints.REMAINDER);

				/* Web root. */
				if (p.getURL() != null) {
					text = new JLabel(getProperty("proj_url"));
					_add(text, 1);

					text = new JLabel(p.getURL());
					_add(text, GridBagConstraints.REMAINDER);
				}

				/* File count. */
				text = new JLabel(getProperty("file_count"));
				_add(text, 1);

				text = new JLabel(fmt.format(p.getOpenableNodes().size()));
				_add(text, GridBagConstraints.REMAINDER);

				/* Disk size; calculated on demand. */
				addSizeWidget(f, true);
			} else {
				JLabel text;

				/* Location. */
				text = new JLabel(getProperty("location"));
				_add(text, 1);

				text = new JLabel(node.getNodePath());
				_add(text, GridBagConstraints.REMAINDER);

				/*
				 * Size. For directories, it's only calculated
				 * when requested.
				 */
				addSizeWidget(f, !node.isFile());

				/* Modification time. */
				if (f != null) {
					String lastMod = f.getExtendedAttribute(VFS.EA_MODIFIED);
					if (lastMod == null) {
						lastMod = getProperty("unknown");
					}

					text = new JLabel(getProperty("modified"));
					_add(text, 1);

					text = new JLabel(lastMod);
					_add(text, GridBagConstraints.REMAINDER);
				}

			}

			/* Vertical filler. */
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weighty = 3.0;
			_add(javax.swing.Box.createGlue(), GridBagConstraints.REMAINDER);
		}


		/** Calculate the size of the directory being shown. */
		public void actionPerformed(ActionEvent ae)
		{
			if (VFSHelper.isLocal(node.getNodePath())) {
				File f = new File(node.getNodePath());
				long len = IOUtilities.fileLength(f);
				nodeSzLabel.setText(formatSize(fmt, len));
			}
		}


		private void addSizeWidget(VFSFile f,
								   boolean calcOnDemand)
		{
			JLabel text;

			text = new JLabel(getProperty("size"));
			_add(text, 1);

			if (calcOnDemand || f == null) {
				text = new JLabel(getProperty("unknown"));
			} else {
				text = new JLabel(formatSize(fmt, f.getLength()));
			}
			gbc.gridwidth = 1;
			gbc.weightx = 3.0;
			gbl.setConstraints(text, gbc);

			if (calcOnDemand) {
				JButton calc;

				add(text);
				calc = new JButton(getProperty("calculate"));
				calc.addActionListener(this);
				calc.setEnabled(f != null);
				_add(calc, GridBagConstraints.REMAINDER);
				nodeSzLabel = text;
			} else {
				_add(text, GridBagConstraints.REMAINDER);
			}
		}


		private void _add(Component c, int width)
		{
			gbc.gridwidth = width;
			gbc.weightx = (width == GridBagConstraints.REMAINDER) ? 2.0 : 1.0;
			gbl.setConstraints(c, gbc);
			add(c);
		}


		private String formatSize(DecimalFormat fmt, long size)
		{
			if (size < 1024L) {
				return fmt.format(size) + " B";
			} else if (size < 1024L * 1024L) {
				return fmt.format(size / 1024) + " kB";
			} else {
				return fmt.format(size / 1024 / 1024) + " MB";
			}
		}
	}
}

