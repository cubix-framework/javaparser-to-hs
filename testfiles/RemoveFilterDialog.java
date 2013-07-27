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
package textfilter;

//{{{ Imports
import java.awt.BorderLayout;
import java.awt.Component;

import java.util.Arrays;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.EnhancedDialog;

import common.gui.OkCancelButtons;
//}}}

/**
 *  A dialog for removing an existing filter.
 *
 *	@author		Marcelo Vanzin
 *  @version	$Id: RemoveFilterDialog.java 25 2006-01-31 05:55:34Z vanza $
 */
public class RemoveFilterDialog extends EnhancedDialog {

	private JComboBox actions;

	//{{{ +RemoveFilterDialog() : <init>
	public RemoveFilterDialog() {
		super(JOptionPane.getFrameForComponent(jEdit.getActiveView()),
			jEdit.getProperty("textfilter.removedlg.title"), true);


		if (ActionManager.getInstance().getActionSet().getActionCount() == 0) {
			JOptionPane.showMessageDialog(jEdit.getActiveView(),
				jEdit.getProperty("textfilter.removedlg.no_actions.msg"),
				jEdit.getProperty("textfilter.removedlg.no_actions.title"),
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		getContentPane().setLayout(new BorderLayout());

		JLabel msg = new JLabel(jEdit.getProperty("textfilter.removedlg.choose_filter"));
		getContentPane().add(BorderLayout.NORTH, msg);

		actions = new JComboBox();
		actions.setRenderer(new EAListRenderer());

		EditAction[] lst = ActionManager.getInstance().getActionSet().getActions();
		Arrays.sort(lst, new EditActionComparator());
		for (int i = 0; i < lst.length; i++)
			actions.addItem(lst[i]);

		getContentPane().add(BorderLayout.CENTER, actions);

		getContentPane().add(BorderLayout.SOUTH, new OkCancelButtons(this));

		pack();
		GUIUtilities.loadGeometry(this, "textfilter.remove_dialog");
		show();
	} //}}}

	//{{{ +ok() : void
	public void ok() {
		FilterAction fa = (FilterAction) actions.getSelectedItem();
		if (jEdit.getProperty(fa.getName() + ".shortcut") != null)
			jEdit.unsetProperty(fa.getName() + ".shortcut");
		if (jEdit.getProperty(fa.getName() + ".shortcut2") != null)
			jEdit.unsetProperty(fa.getName() + ".shortcut2");
		ActionManager.getInstance().removeAction(fa);
		GUIUtilities.saveGeometry(this, "textfilter.remove_dialog");
		setVisible(false);
	} //}}}

	//{{{ +cancel() : void
	public void cancel() {
		GUIUtilities.saveGeometry(this, "textfilter.remove_dialog");
		setVisible(false);
	} //}}}

	//{{{ -class EAListRenderer
	private class EAListRenderer extends DefaultListCellRenderer {

		//{{{ +getListCellRendererComponent(JList, Object, int, boolean, boolean) : Component
		public Component getListCellRendererComponent(JList list,
							Object value, int index, boolean isSelected,
							boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			setText(((EditAction)value).getLabel());
			return this;
		} //}}}

	} //}}}

}

