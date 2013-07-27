/*
 * AStylePlugin.java - a source code astyle plugin for jEdit
 * Copyright (c) 2001 Dirk Moebius (dmoebius@gmx.net)
 * Artistic Style (c) 1998-2001 Tal Davidson (davidsont@bigfoot.com)
 *
 * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package astyleplugin;


// import java.util.Vector;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.*;
// import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.gui.OptionsDialog;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
// import org.gjt.sp.jedit.options.BeanOptionPane;
import org.gjt.sp.util.Log;


/**
 * A plugin for beautifying C, C++ and Java source code,
 * using <b>Artistic Style</b> from Tal Davidson.
 *
 * @author <A HREF="mailto:dmoebius@gmx.net">Dirk Moebius</A>
 */
public class AStylePlugin extends EBPlugin {

	/*
	public void createOptionPanes(OptionsDialog optionsDialog) {
		OptionPane bop = new BeanOptionPane("astyleplugin", "astyleplugin.Formatter", this.getClass().getClassLoader());
		optionsDialog.addOptionPane(bop);
	}
*/

	public void handleMessage(EBMessage message) {
		if (message instanceof BufferUpdate) {
			BufferUpdate bu = (BufferUpdate) message;
			if (bu.getWhat() == BufferUpdate.SAVING) {
				String formatOnSave = jEdit.getProperty("astyleplugin.formatOnSave");
				if (formatOnSave != null)
					if (formatOnSave.equalsIgnoreCase("true"))
						beautify(bu.getBuffer(), bu.getView(), false);
			}
		}
	}


	/**
	 * Beautify the current buffer using AStyle.
	 *
	 * @param buffer  The buffer to be beautified.
	 * @param view  The view; may be null, if there is no current view.
	 * @param showErrorDialogs  If true, modal error dialogs will be shown
	 *        on error. Otherwise, the errors are silently logged. This is
	 *        used when the property "formatOnSave" is set.
	 */
	public static void beautify(Buffer buffer, View view, boolean showErrorDialogs) {
		if (buffer.isReadOnly()) {
			Log.log(Log.NOTICE, AStylePlugin.class, jEdit.getProperty("astyleplugin.error.isReadOnly.message"));
			if (showErrorDialogs)
				GUIUtilities.error(view, "astyleplugin.error.isReadOnly", null);
			return;
		}

		// ask, if current mode is not C/C++/Java:
		String mode = buffer.getMode().getName();
		if (!(mode.equals("java") || mode.equals("c") || mode.equals("cplusplus") || mode.equals("c++"))) {
			if (showErrorDialogs) {
				int answer = GUIUtilities.confirm(view, "astyleplugin.confirm.mode", null,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (answer != JOptionPane.OK_OPTION)
					return;
			} else {
				Log.log(Log.NOTICE, AStylePlugin.class, "buffer " + buffer.getName()
					+ " not beautified, because mode is not 'c', 'c++', 'cplusplus' or 'java'");
				return;
			}
		}

		// run the format routine synchronously on the AWT thread:
		VFSManager.runInAWTThread(new AStyleThread(buffer, view, showErrorDialogs));
	}

}

