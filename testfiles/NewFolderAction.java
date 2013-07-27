/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.actions;

import gruntspud.CVSCommandHandler;
import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ResourceUtil;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.report.AddFileInfoPane;
import gruntspud.ui.report.FileInfoPane;
import gruntspud.ui.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.add.AddCommand;

/**
 * An action to create a new folder within the currently selected folder
 * 
 * @author magicthize 
 */
public class NewFolderAction extends ReportingGruntspudAction {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle");

	/**
	 * Constructor for the NewFolderAction object
	 * 
	 * @param context context
	 */
	public NewFolderAction(GruntspudContext context) {
		super(res, "newFolderAction", context);
		putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_NEW_FOLDER));
		putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_NEW_FOLDER));
		setUpdatesFiles(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gruntspud.actions.GruntspudAction#checkAvailable()
	 */
	public boolean checkAvailable() {
		ViewManager mg = getContext().getViewManager();
		CVSFileNode[] sel = mg.getSelectedNodes();

        return !CVSCommandHandler.getInstance().isCommandRunning() && mg.isHomeExists()
				&& ((sel == null) || ((sel.length < 2) && (mg.getSelectedFileCount() == 0)));
	}

	/* (non-Javadoc)
	 * @see gruntspud.actions.ReportingGruntspudAction#createFileInfoPane()
	 */
	public FileInfoPane createFileInfoPane() {
		return new AddFileInfoPane(getContext());
	}

	/* (non-Javadoc)
	 * @see gruntspud.actions.ReportingGruntspudAction#getFileInfoName()
	 */
	public String getFileInfoName() {
		return "addFileInfo";
	}

	/* (non-Javadoc)
	 * @see gruntspud.actions.ReportingGruntspudAction#getFileInfoText()
	 */
	public String getFileInfoText() {
		return res.getString("addFolderAction.fileInfoText");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent evt) {
		final CVSFileNode sel = getContext().getViewManager().getNodesToPerformActionOn()[0];
		JPanel t = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		t.add(new JLabel(res.getString("newFolderAction.createIn.text")));

		JLabel x = new JLabel(sel.getName());
		x.setToolTipText(sel.getFile().getAbsolutePath());
		x.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
		t.add(x);

		JPanel n = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		final XTextField folderName = new XTextField(25);
		n.add(new JLabel(res.getString("newFolderAction.name.text")));
		n.add(folderName);

		JPanel e = new JPanel(new BorderLayout());
		e.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		e.add(n, BorderLayout.CENTER);
		e.add(t, BorderLayout.NORTH);

		JCheckBox addToCVS = null;

		if ((sel != null) && (sel.getCVSRoot() != null)) {
			addToCVS = new JCheckBox(res.getString("newFolderAction.addToCVS.text"));
			addToCVS.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
			addToCVS.setHorizontalAlignment(SwingConstants.CENTER);
			e.add(addToCVS, BorderLayout.SOUTH);
		}

		JPanel z = new JPanel(new BorderLayout());
		z.add(new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_NEW_FOLDER)), BorderLayout.WEST);
		z.add(e);
		final OptionDialog.Option ok = new OptionDialog.Option(res.getString("newFileAction.optionDialog.option.ok.text"), res
				.getString("newFolderAction.optionDialog.option.ok.toolTipText"),
				ResourceUtil.getResourceMnemonic(res, "newFolderAction.optionDialog.option.ok.mnemonic"));
		OptionDialog.Option cancel = new OptionDialog.Option(res.getString("newFolderAction.optionDialog.option.cancel.text"), res
				.getString("newFolderAction.optionDialog.option.cancel.toolTipText"),
				ResourceUtil.getResourceMnemonic(res, "newFolderAction.optionDialog.option.cancel.mnemonic"));

		Component parent = getParentComponentForEvent(evt);
		OptionDialog.Option opt = OptionDialog.showOptionDialog("newFolder", getContext(), parent, new OptionDialog.Option[]{ok,
				cancel}, z,
				"New Folder", ok, new OptionDialog.Callback() {
					public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
						return true;
					}

                    public void close(OptionDialog dialog, Option option) {
                        // TODO Auto-generated method stub
                        
                    }
				}

				, false, true);

		if ((opt != ok) || folderName.getText().equals("")) {
			return;
		}

		File f = new File(sel.getFile(), folderName.getText());
		getContext()
				.getHost()
				.writeToConsole(getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_GRUNTSPUD), MessageFormat
						.format(res.getString("newFolderAction.consoleOutput.info.creatingFolder"), new String[]{f.getAbsolutePath()}))		;

		if (!f.mkdir()) {
			getContext()
					.getHost()
					.writeToConsole(getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_ERRORS), MessageFormat
							.format(res.getString("newFolderAction.consoleOutput.error.failedToCreateFolder"), new String[]{f
									.getAbsolutePath()}))			;
			JOptionPane.showMessageDialog(parent, res.getString("newFolderAction.error.folderCouldNotBeCreated.text"), res
					.getString("newFolderAction.error.folderCouldNotBeCreated.title"),
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			getContext().getViewManager().fileUpdated(f);
			getContext().getViewManager().resetNodeUpdateTimer();
			if ((addToCVS != null) && addToCVS.isSelected()) {
				CVSFileNode newNode = getContext().getViewManager().findNodeForPath(sel, f, true);

				if (newNode != null) {
					AddCommand cmd = new AddCommand();
					CVSCommandHandler
							.getInstance()
							.runCommandGroup(parent, getContext(), null, new Command[]{cmd}, new CVSFileNode[]{newNode}, null,
									false, null, null, this, getEnabledOptionalListeners());
				}
			}
		}
	}
}