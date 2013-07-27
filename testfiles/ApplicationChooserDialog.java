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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import common.gui.OkCancelButtons;
//}}}

/**
 *  A dialog for choosing an application for execution.
 *
 *	@author		Marcelo Vanzin
 *  @version	$Id: ApplicationChooserDialog.java 9733 2007-06-09 04:36:30Z vanza $
 */
public class ApplicationChooserDialog extends EnhancedDialog
										implements ActionListener {

	//{{{ Private members
	private final boolean createAction;

	private HistoryTextField actionName;
	private HistoryTextField command;

	private JRadioButton srcBuffer;
	private JRadioButton srcSelection;
	private JRadioButton srcNone;

	private JRadioButton typeStdIn;
	private JRadioButton typeArg;
	private JRadioButton typeString;

	private JRadioButton destNew;
	private JRadioButton destSelection;
	private JRadioButton destAppend;
	private JRadioButton destReplace;

	private JButton browse;

	private View view;
	//}}}

	//{{{ +ApplicationChooser(boolean) : <init>
	/**
	 *	Shows a dialog with inputs that give information about how to execute
	 *	an external application.
	 *
	 *	@param	createAction	Whether the dialog will be used to create a
	 *							new EditAction for automatically running an
	 *							application (true), or just to run a chosen
	 *							application once.
	 */
	public ApplicationChooserDialog(boolean createAction) {
		super(JOptionPane.getFrameForComponent(jEdit.getActiveView()),
			(createAction) ?
				jEdit.getProperty("textfilter.chooser.create_action") :
				jEdit.getProperty("textfilter.chooser.run_filter"),
			true);
		this.createAction = createAction;
		this.view = jEdit.getActiveView();

		JEditTextArea textArea = jEdit.getActiveView().getTextArea();

		// builds the dialog
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		getContentPane().setLayout(gbl);

		//{{{ Action name
		if (createAction) {
			JLabel aLabel = new JLabel(jEdit.getProperty("textfilter.chooser.action_name"));
			gbc.weightx = 0.0;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
			gbl.setConstraints(aLabel, gbc);
			getContentPane().add(aLabel);

			actionName = new HistoryTextField("textfilter.action_name");
			actionName.setColumns(30);
			gbc.weightx = 0.0;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(actionName, gbc);

			getContentPane().add(actionName);
		}
		//}}}

		//{{{ Command
		JLabel label = new JLabel(jEdit.getProperty("textfilter.chooser.command"));
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(label, gbc);
		getContentPane().add(label);

		command = new HistoryTextField("textfilter.comand");
		command.setColumns(30);
		if (jEdit.getProperty("text_filer.last_command") != null)
			command.setText(jEdit.getProperty("text_filer.last_command"));

		gbc.weightx = 2.0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbl.setConstraints(command, gbc);
		getContentPane().add(command);

		browse = new JButton(jEdit.getProperty("textfilter.chooser.browse"));
		browse.addActionListener(this);
		gbc.weightx = 0.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.NONE;
		gbl.setConstraints(browse, gbc);
		getContentPane().add(browse);
		//}}}

		//{{{ Some configuration

		//{{{ source type
		ButtonGroup sType = new ButtonGroup();
		JPanel srcType = new JPanel(new GridLayout(3, 1));
		srcType.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty("textfilter.chooser.src")));

		typeStdIn = new JRadioButton(jEdit.getProperty("textfilter.chooser.src.stdin"));
		typeStdIn.setToolTipText(jEdit.getProperty("textfilter.chooser.src.srdin.tooltip"));
		sType.add(typeStdIn);
		srcType.add(typeStdIn);

		typeArg = new JRadioButton(jEdit.getProperty("textfilter.chooser.src.argument"));
		typeArg.setToolTipText(jEdit.getProperty("textfilter.chooser.argument.tooltip"));
		sType.add(typeArg);
		srcType.add(typeArg);

		typeString = new JRadioButton(jEdit.getProperty("textfilter.chooser.src.string"));
		typeString.setToolTipText(jEdit.getProperty("textfilter.chooser.src.string.tooltip"));
		sType.add(typeString);
		srcType.add(typeString);

		typeStdIn.setSelected(true);
		//}}}

		//{{{ source
		ButtonGroup source = new ButtonGroup();
		JPanel src = new JPanel(new GridLayout(3, 1));
		src.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty("textfilter.chooser.text")));

		srcBuffer = new JRadioButton(jEdit.getProperty("textfilter.chooser.text.buffer"));
		srcBuffer.setToolTipText(jEdit.getProperty("textfilter.chooser.text.buffer.tooltip"));
		source.add(srcBuffer);
		src.add(srcBuffer);

		srcSelection = new JRadioButton(jEdit.getProperty("textfilter.chooser.text.selection"));
		srcSelection.setToolTipText(jEdit.getProperty("textfilter.chooser.text.selection.tooltip"));
		source.add(srcSelection);
		src.add(srcSelection);

		srcNone = new JRadioButton(jEdit.getProperty("textfilter.chooser.text.none"));
		srcNone.setToolTipText(jEdit.getProperty("textfilter.chooser.text.none.tooltip"));
		source.add(srcNone);
		src.add(srcNone);

		if (textArea.getSelectionCount() > 0)
			srcSelection.setSelected(true);
		else
			srcBuffer.setSelected(true);
		//}}}

		//{{{ return type
		ButtonGroup destination = new ButtonGroup();
		JPanel dest = new JPanel(new GridLayout(3, 1));
		dest.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty("textfilter.chooser.dest")));

		destNew = new JRadioButton(jEdit.getProperty("textfilter.chooser.dest.new"));
		destNew.setToolTipText(jEdit.getProperty("textfilter.chooser.dest.new.tooltip"));
		destination.add(destNew);
		dest.add(destNew);

		destSelection = new JRadioButton(jEdit.getProperty("textfilter.chooser.dest.selection"));
		destSelection.setToolTipText(jEdit.getProperty("textfilter.chooser.dest.selection.tooltip"));
		destination.add(destSelection);
		dest.add(destSelection);

		destAppend = new JRadioButton(jEdit.getProperty("textfilter.chooser.dest.append"));
		destAppend.setToolTipText(jEdit.getProperty("textfilter.chooser.dest.append.tooltip"));
		destination.add(destAppend);
		dest.add(destAppend);

		destReplace = new JRadioButton(jEdit.getProperty("textfilter.chooser.dest.replace"));
		destReplace.setToolTipText(jEdit.getProperty("textfilter.chooser.dest.replace.tooltip"));
		destination.add(destReplace);
		dest.add(destReplace);

		if (textArea.getSelectionCount() > 0)
			destSelection.setSelected(true);
		else
			destNew.setSelected(true);
		//}}}

		JPanel switches = new JPanel(new FlowLayout());
		switches.add(srcType);
		switches.add(src);
		switches.add(dest);

		gbl.setConstraints(switches, gbc);
		getContentPane().add(switches);

		//}}}

		OkCancelButtons buttons = new OkCancelButtons(this);
		if (createAction)
			buttons.setOkText(jEdit.getProperty("textfilter.chooser.createBtn"));
		else
			buttons.setOkText(jEdit.getProperty("textfilter.chooser.runBtn"));

		gbl.setConstraints(buttons, gbc);
		getContentPane().add(buttons);

		// show dialog
		pack();
		GUIUtilities.loadGeometry(this, "textfilter.application_dialog");
		show();
	} //}}}

	//{{{ +ok() : void
	public void ok() {
		// gets the options
		int howToSend;
		if (typeStdIn.isSelected())
			howToSend = ApplicationRunner.SRC_TYPE_STDIN;
		else if (typeArg.isSelected())
			howToSend = ApplicationRunner.SRC_TYPE_ARGUMENT;
		else
			howToSend = ApplicationRunner.SRC_TYPE_STRING;

		int srcIdx;
		if (srcBuffer.isSelected())
			srcIdx = ApplicationRunner.SOURCE_BUFFER;
		else if (srcSelection.isSelected())
			srcIdx = ApplicationRunner.SOURCE_SELECTION;
		else
			srcIdx = ApplicationRunner.SOURCE_NONE;

		int destIdx;
		if (destNew.isSelected())
			destIdx = ApplicationRunner.RETURN_NEW;
		else if (destSelection.isSelected())
			destIdx = ApplicationRunner.RETURN_SELECTION;
		else if (destAppend.isSelected())
			destIdx = ApplicationRunner.RETURN_APPEND;
		else
			destIdx = ApplicationRunner.RETURN_REPLACE;

		if (createAction) {
			ActionManager am = ActionManager.getInstance();
			// creates a new action
			String aName = actionName.getText();
			if (aName == null || aName.trim().length() == 0) {
				JOptionPane.showMessageDialog(view,
					jEdit.getProperty("textfilter.chooser.error.no_name"),
					jEdit.getProperty("textfilter.chooser.error"),
					JOptionPane.ERROR_MESSAGE);
				actionName.requestFocus();
				return;
			} else if (am.hasAction(aName)) {
				if (JOptionPane.showConfirmDialog(view,
						jEdit.getProperty("textfilter.chooser.error.action_exists",
							new Object[] { aName.toString() }),
						jEdit.getProperty("textfilter.chooser.error.action_exists.title"),
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
					return;
				}
			}

			String comm = command.getText();
			if (comm == null || comm.trim().length() == 0) {
				JOptionPane.showMessageDialog(view,
					jEdit.getProperty("textfilter.chooser.error.no_command"),
					jEdit.getProperty("textfilter.chooser.error"),
					JOptionPane.ERROR_MESSAGE);
				command.requestFocus();
				return;
			}

			// ok to continue
			am.addAction(new FilterAction(aName.trim(), comm.trim(), howToSend,
											srcIdx, destIdx));
			command.addCurrentToHistory();
			actionName.addCurrentToHistory();
		} else {
			// runs the app
			if (ApplicationRunner.runApp(view, command.getText(), srcIdx, howToSend, destIdx)) {
				command.addCurrentToHistory();
				jEdit.setProperty("text_filter.last_command", command.getText());
			}
		}

		GUIUtilities.saveGeometry(this, "textfilter.application_dialog");
		setVisible(false);
	} //}}}

	//{{{ +cancel() : void
	public void cancel() {
		GUIUtilities.saveGeometry(this, "textfilter.application_dialog");
		setVisible(false);
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == browse){
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				command.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		}
	} //}}}

}

