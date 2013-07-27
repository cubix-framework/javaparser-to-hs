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
package projectviewer.action;

//{{{ Imports
import java.awt.event.ActionEvent;

import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.MiscUtilities;

import projectviewer.vpt.VPTNode;
//}}}

/**
 *	Opens a node in jEdit using a specific character encoding. The list of
 *	encodings is provided by jEdit's MiscUtilities class, so this action
 *	will only be available in jEdit 4.2pre5 and later.
 *
 *	@author		Marcelo Vanzin
 *	@version	$Id: OpenWithEncodingAction.java 9946 2007-07-08 00:59:18Z vanza $
 */
public class OpenWithEncodingAction extends Action {

	private JMenuItem mi_other;

	//{{{ +getText() : String
	/** Returns the text to be shown on the button and/or menu item. */
	public String getText() {
		return jEdit.getProperty("projectviewer.action.open_with_encoding");
	} //}}}

	//{{{ +getMenuItem() : JComponent
	public JComponent getMenuItem() {
		if (cmItem == null) {
			JMenu item = new JMenu(getText());
			String[] encodings = MiscUtilities.getEncodings(false);

			if (encodings != null)
			for (int i = 0; i < encodings.length; i++) {
				JMenuItem mi = new JMenuItem(encodings[i]);
				mi.addActionListener(this);
				item.add(mi);
			}

			item.addSeparator();
			mi_other = new JMenuItem(jEdit.getProperty("vfs.browser.other-encoding.label"));
			mi_other.addActionListener(this);
			item.add(mi_other);
			cmItem = item;
		}
		return cmItem;
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Creates a new project. */
	public void actionPerformed(ActionEvent e) {
		VPTNode file = viewer.getSelectedNode();
		Buffer b = jEdit.getBuffer(file.getNodePath());
		String encoding;
		if (e.getSource() == mi_other) {
			encoding = JOptionPane.showInputDialog(viewer,
				jEdit.getProperty("encoding-prompt.message"),
				jEdit.getProperty("encoding-prompt.title"),
				JOptionPane.QUESTION_MESSAGE);
			if (encoding == null)
				return;
		} else {
			encoding = ((JMenuItem)e.getSource()).getText();
		}

		if (b == null) {
			Hashtable props = new Hashtable();
			props.put(Buffer.ENCODING, encoding);
			jEdit.openFile(viewer.getView(), null, file.getNodePath(), false, props);
		} else {
			b.setStringProperty(Buffer.ENCODING, encoding);
			b.propertiesChanged();
		}
	} //}}}

	//{{{ +prepareForNode(VPTNode) : void
	/** Enable action only for openable nodes. */
	public void prepareForNode(VPTNode node) {
		cmItem.setVisible(node != null && node.canOpen());
		if (node != null && node.canOpen()) {
			Buffer b = jEdit.getBuffer(node.getNodePath());
			JMenu menu = (JMenu) cmItem;
			if (b == null) {
				menu.setText(jEdit.getProperty("projectviewer.action.open_with_encoding"));
			} else {
				menu.setText(jEdit.getProperty("projectviewer.action.set_encoding"));
			}

			for (int i = 0; i < menu.getItemCount(); i++) {
				JMenuItem mi = menu.getItem(i);
				if (mi != null && mi != mi_other) {
					String encoding = mi.getText();
					mi.setVisible(
						!"true".equals(jEdit.getProperty("encoding.opt-out." + encoding)));
				}
			}
		}
	} //}}}

}

