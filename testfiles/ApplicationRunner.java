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
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.gjt.sp.util.Log;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.JEditTextArea;
//}}}

/**
 *  Class for running an application: takes care of taking the text from jEdit's
 *	buffer and feeding it to the application, and doing what the user asks for
 *	with the returned data.
 *
 *	@author		Marcelo Vanzin
 *  @version	$Id: ApplicationRunner.java 9733 2007-06-09 04:36:30Z vanza $
 */
public final class ApplicationRunner {

	//{{{ Constants
	/** Returned text is put in a new bufer. */
	public static final int RETURN_NEW			= 0;
	/** Returned text substitutes the current selection. */
	public static final int RETURN_SELECTION	= 1;
	/** Returned text is appended to the current buffer. */
	public static final int RETURN_APPEND		= 2;
	/** Returned text replaces the current buffer. */
	public static final int RETURN_REPLACE		= 3;

	/** The whole buffer. */
	public static final int SOURCE_BUFFER		= 0;
	/** The current selection. */
	public static final int SOURCE_SELECTION	= 1;
	/** Don't pass anything, just run the command. */
	public static final int SOURCE_NONE			= 2;

	/** stdin */
	public static final int SRC_TYPE_STDIN		= 0;
	/** argument */
	public static final int SRC_TYPE_ARGUMENT	= 1;
	/** string */
	public static final int SRC_TYPE_STRING		= 2;
	//}}}

	//{{{ +_runApp(String, int, boolean, int, String)_ : void
	/**
	 *	Runs the command given. If "fromSelection" is true and no selection
	 *	exists, a warning is shown and nothing is executed. If the return type
	 *	is "RETURN_SELECTION" and no selection exists, text is appended at the
	 *	current cursor position.
	 *
	 *	<p>At the end, if there's anything in stderr, it is shown in a warning
	 *	dialog to the user, and an option to cancel the operation is given.</p>
	 *
	 *	<p>If there's anything in stdout, the chosen return action is executed;
	 *	otherwise, a warning is shown.</p>
	 *
	 *	<p>If for some reason we can't execute the command, an error message
	 *	is shown.</p>
	 *
	 *	@param	command		The command to execute, with all the switches
	 *						appended.
	 *	@param	source		Source of the data to be passed to the program
	 *						(see constants).
	 *	@param	howToSend	How to send the data to the external program.
	 *	@param	returnType	What to do with the text that is returned
	 *						(see constants).
	 *
	 *	@return	Whether execution was successful.
	 */
	public static boolean runApp(View view,
									String command,
									int source,
									int howToSend,
									int returnType) {

		Buffer buffer = view.getBuffer();
		JEditTextArea textArea = view.getTextArea();

		// translate '~'
		if (command.charAt(0) == '~') {
			String home = System.getProperty("user.home");
			if (home != null)
				command = home + command.substring(1);
		}
		CmdExec cmd = new CmdExec(command);

		// Defines the text to pass to the filter
		String param = null;
		switch (source) {
			case SOURCE_BUFFER:
				param = buffer.getText(0, buffer.getLength());
				break;

			case SOURCE_SELECTION:
				param = textArea.getSelectedText();
				if (param == null || param.length() == 0) {
					JOptionPane.showMessageDialog(view,
						jEdit.getProperty("textfilter.runner.no_selection.msg"),
						jEdit.getProperty("textfilter.runner.no_selection.title"),
						JOptionPane.ERROR_MESSAGE);
					return false;
				}
				break;

			case SOURCE_NONE:
				break;

			default:
				Log.log(Log.WARNING, ApplicationRunner.class, "Shouldn't reach this.");
		}

		// Defines the command to run
		switch (howToSend) {
			case SRC_TYPE_ARGUMENT:
				cmd.setArgumentLine(param);
				break;

			case SRC_TYPE_STRING:
				cmd.addArgument(param);
				break;

			default:
				cmd.setStdin(param);
		}


		try {
			// Run the command
			Process p = cmd.execute();

			try {
				p.waitFor();
			} catch (InterruptedException ie) {
				// annoying
			}

			boolean processOutput = true;

			// Checks for errors
			byte[] errors = cmd.getStderr();
			if (errors != null && errors.length > 0) {
				processOutput = showError(view, new String(errors), true);
			}

			// Checks output
			if (processOutput) {
				byte[] data = cmd.getStdout();

				if (data != null) {
					switch (returnType) {
						case RETURN_NEW:
							Buffer b = jEdit.newFile(view);
							b.insert(0, new String(data));
							view.setBuffer(b);
							break;

						case RETURN_SELECTION:
							if (textArea.getSelectionCount() > 0) {
								textArea.setSelectedText(new String(data));
							} else {
								buffer.insert(textArea.getCaretPosition(), new String(data));
							}
							break;

						case RETURN_APPEND:
							buffer.insert(buffer.getLength(), new String(data));
							break;

						case RETURN_REPLACE:
							int pos  = textArea.getCaretPosition();
							int scroll = textArea.getFirstLine();
							buffer.beginCompoundEdit();
							try {
								buffer.remove(0, buffer.getLength());
								buffer.insert(0, new String(data));
							} finally {
								buffer.endCompoundEdit();
							}
							if (buffer.getLength() < pos) {
								pos = buffer.getLength();
							}
							textArea.setCaretPosition(pos);
							textArea.setFirstLine(scroll);
							break;
						default:
							Log.log(Log.WARNING, ApplicationRunner.class, "Shouldn't reach this.");
					}
				} else {
					JOptionPane.showMessageDialog(view,
						jEdit.getProperty("textfilter.runner.no_output.msg"),
						jEdit.getProperty("textfilter.runner.no_output.msg"),
						JOptionPane.INFORMATION_MESSAGE);
				}

			} else {
				return false;
			}

		} catch (IOException ioe) {
			StringWriter sw = new StringWriter();
			ioe.printStackTrace(new PrintWriter(sw));
			showError(view, sw.toString(), false);
			return false;
		}
		return true;
	} //}}}

	//{{{ showError(String)
	/** Returns whether to continue processing output even if errors occurred. */
	private static boolean showError(View view, String error, boolean ask) {
		JPanel pane = new JPanel(new BorderLayout());
		JLabel msg;
		if (ask) {
			msg = new JLabel(jEdit.getProperty("textfilter.runner.stderr_msg"));
		} else {
			msg = new JLabel(jEdit.getProperty("textfilter.runner.error_msg"));
		}
		pane.add(BorderLayout.NORTH, msg);

		JTextArea text = new JTextArea(10, 60);
		text.setEditable(false);
		text.setText(error);
		text.setCaretPosition(0);
		pane.add(BorderLayout.CENTER, new JScrollPane(text));

		if (ask) {
			return (JOptionPane.showConfirmDialog(view, pane,
						jEdit.getProperty("textfilter.runner.stderr.title"),
						JOptionPane.YES_NO_OPTION)
							== JOptionPane.YES_OPTION);
		} else {
			JOptionPane.showMessageDialog(view, pane,
					"Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	} //}}}

}

