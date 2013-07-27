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
import gruntspud.CVSSubstType;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ResourceUtil;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.SubstTypeListCellRenderer;
import gruntspud.ui.SubstTypeListModel;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.add.AddCommand;

/**
 * Action to create a new file in the selected directory. The user is given the choice to add the file to CVS and / or open it in
 * the default editor.
 * 
 * @author magicthize @created 26 May 2002
 */
public class NewFileAction extends AddAction {
	static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle");

	/**
	 * Constructor for the NewFileAction object
	 * 
	 * @param context context
	 */
	public NewFileAction(GruntspudContext context) {
		super(res, "newFileAction", context);
		putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_NEW_FILE));
		putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_NEW_FILE));
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent evt) {
		final CVSFileNode sel = getContext().getViewManager().getNodesToPerformActionOn()[0];
		AddFileOptionsPane opts = new AddFileOptionsPane(sel);
		final OptionDialog.Option ok = new OptionDialog.Option(res.getString("newFileAction.optionDialog.option.ok.text"), res
				.getString("newFileAction.optionDialog.option.ok.toolTipText"),
				ResourceUtil.getResourceMnemonic(res, "newFileAction.optionDialog.option.ok.mnemonic"));
		OptionDialog.Option cancel = new OptionDialog.Option(res.getString("newFileAction.optionDialog.option.cancel.text"), res
				.getString("newFileAction.optionDialog.option.cancel.toolTipText"),
				ResourceUtil.getResourceMnemonic(res, "newFileAction.optionDialog.option.cancel.mnemonic"));
		Component parent = getParentComponentForEvent(evt);
		OptionDialog.Option opt = OptionDialog.showOptionDialog("newFile", getContext(), parent, new OptionDialog.Option[]{ok,
				cancel},
				opts, res.getString("newFileAction.optionDialog.title"), ok, new OptionDialog.Callback() {
					public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
						return true;
					}

                    public void close(OptionDialog dialog, Option option) {
                        // TODO Auto-generated method stub
                        
                    }
				}

				, false, true);

		if ((opt != ok) || opts.getFileName().equals("")) {
			return;
		}

		File f = new File(sel.getFile(), opts.getFileName());

		if (f.exists()
				&& (JOptionPane.showConfirmDialog(parent, res.getString("newFileAction.warning.fileExists.text"), res.getString(
						"newFileAction.warning.fileExists.title"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)) {
			return;
		}

		FileOutputStream out = null;

		try {
			out = new FileOutputStream(f);
		}
		catch (IOException ioe) {
			GruntspudUtil.showErrorMessage(getContext().getHost().getMainComponent(), res.getString(
					"newFileAction.error.fileCouldNotBeCreated.text"), res.getString(
					"newFileAction.error.fileCouldNotBeCreated.title"), ioe);
		}
		finally {
			GruntspudUtil.closeStream(out);
		}

		getContext().getViewManager().fileUpdated(f);
		getContext().getViewManager().resetNodeUpdateTimer();

		final CVSFileNode newNode = getContext().getViewManager().findNodeForPath(sel, f, true);

		if (newNode != null) {
			if (opts.isAddToCVS()) {
				final AddCommand cmd = new AddCommand();
				CVSCommandHandler
						.getInstance()
						.runCommandGroup(parent, getContext(), null, new Command[]{cmd}, new CVSFileNode[]{newNode},
								new CVSSubstType[]{opts.getType()}, false, null, null, this, getEnabledOptionalListeners());
			}

			if (opts.isEditFile()) {
				try {
					getContext().getHost().openNode(newNode);
				}
				catch (IOException ioe) {
					GruntspudUtil
							.showErrorMessage(getContext().getHost().getMainComponent(), res.getString(
									"newFileAction.error.fileCouldNotBeOpened.text"), res.getString(
									"newFileAction.error.fileCouldNotBeOpened.title"), ioe);
				}
			}
		}

	}

	class AddFileOptionsPane extends JPanel {
		private JCheckBox addToCVS;
		private JCheckBox editFile;
		private JComboBox type;
		private XTextField fileName;
		private CVSFileNode[] sel;

		AddFileOptionsPane(CVSFileNode sel) {
			super(new BorderLayout());

			JPanel t = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
			t.add(new JLabel(res.getString("newFileAction.createIn.text")));

			JLabel x = new JLabel(sel.getName());
			x.setToolTipText(sel.getFile().getAbsolutePath());
			x.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD));
			t.add(x);

			JPanel n = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
			fileName = new XTextField(25);
			fileName.grabFocus();
			n.add(new JLabel(res.getString("newFileAction.name.text")));
			n.add(fileName);

			JPanel e = new JPanel(new BorderLayout());
			e.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
			e.add(n, BorderLayout.CENTER);
			e.add(t, BorderLayout.NORTH);

			JPanel g = new JPanel(new GridBagLayout());
			g.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

			GridBagConstraints gbc = new GridBagConstraints();
			gbc.insets = new Insets(1, 1, 1, 1);
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1.0;
			editFile = new JCheckBox(res.getString("newFileAction.editFile.text"));
			UIUtil.jGridBagAdd(g, editFile, gbc, GridBagConstraints.REMAINDER);
			e.add(g, BorderLayout.SOUTH);

			if ((sel != null) && (sel.getCVSRoot() != null)) {
				addToCVS = new JCheckBox(res.getString("newFileAction.addToCVS.text"));
				addToCVS.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
				type = new JComboBox(new SubstTypeListModel(new CVSSubstType[]{CVSSubstType.CVS_SUBST_TYPE_TEXT,
						CVSSubstType.CVS_SUBST_TYPE_DEFAULT_LOCKER, CVSSubstType.CVS_SUBST_TYPE_OLD_VALUES,
						CVSSubstType.CVS_SUBST_TYPE_ONLY_KEYWORDS, CVSSubstType.CVS_SUBST_TYPE_ONLY_VALUES}));
				addToCVS.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						type.setEnabled(addToCVS.isSelected());
					}
				});
				type.setRenderer(new SubstTypeListCellRenderer());
				type.setEnabled(false);
				UIUtil.jGridBagAdd(g, addToCVS, gbc, GridBagConstraints.REMAINDER);
				gbc.insets.left = 24;
				UIUtil.jGridBagAdd(g, type, gbc, GridBagConstraints.REMAINDER);
				gbc.insets.left = 1;
			}

			add(new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_NEW_FILE)), BorderLayout.WEST);
			add(e, BorderLayout.CENTER);
		}

		private boolean isAddToCVS() {
			return addToCVS.isSelected();
		}

		public boolean isEditFile() {
			return editFile.isSelected();
		}

		public CVSSubstType getType() {
			return (CVSSubstType) type.getSelectedItem();
		}

		public String getFileName() {
			return fileName.getText();
		}
	}
}