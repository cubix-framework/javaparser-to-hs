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

package gruntspud.ui.preferences;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractBottomAction;
import gruntspud.actions.AbstractEditAction;
import gruntspud.actions.AbstractNormalAddAction;
import gruntspud.actions.AbstractNormalRemoveAction;
import gruntspud.actions.AbstractTopAction;
import gruntspud.file.FileTypeMapping;
import gruntspud.file.FileTypeMappingModel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.ToolBarTablePane;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Mini text editor
 * 
 * @author Brett Smiht @created 26 May 2002
 */
public class FileTypeMappingPane extends JPanel {
	//  Private instance variables
	private FileTypeMappingTable mappingTable;
	private Action upAction;
	private Action downAction;
	private Action addAction;
	private Action removeAction;
	private Action editAction;
	private GruntspudContext context;

	/**
	 * Creates a new FileTypeMappingPane object.
	 * 
	 * @param context DOCUMENT ME!
	 */
	public FileTypeMappingPane(GruntspudContext context) {
		super(new BorderLayout());
		
		setOpaque(false);

		//  Initialise
		this.context = context;

		//  Create the text area
		mappingTable = new FileTypeMappingTable(context);
		mappingTable.setBorder(null);
		mappingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
				setAvailableActions();
			}
		});
		mappingTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2)
					editSelected();
			}
		});

		//  Create the toolbar
		JToolBar toolBar = new JToolBar("File type tools");
		toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		toolBar.setBorder(null);
		toolBar.setFloatable(false);
		boolean showSelectiveText = context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
		toolBar.add(UIUtil.createButton(addAction = new AddAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(removeAction = new RemoveAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(editAction = new EditAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(upAction = new UpAction(), showSelectiveText, false));
		toolBar.add(UIUtil.createButton(downAction = new DownAction(), showSelectiveText, false));

		//  Build this
		add(new ToolBarTablePane(toolBar, mappingTable) {
			public Dimension getPreferredSize() {
				return new Dimension(420, 380);
			}
		}

		, BorderLayout.CENTER);

		//  Set the intially available actions
		setAvailableActions();
	}

	/**
	 * Save the profiles
	 */
	public void apply() {
		mappingTable.cleanUp();
	}

	/**
	 * Set what actions are available depending on state
	 */
	private void setAvailableActions() {
		removeAction.setEnabled(mappingTable.getSelectedRowCount() == 1);
		editAction.setEnabled(mappingTable.getSelectedRowCount() == 1);
		upAction.setEnabled((mappingTable.getSelectedRowCount() == 1) && (mappingTable.getSelectedRow() > 0));
		downAction.setEnabled((mappingTable.getSelectedRowCount() == 1)
				&& (mappingTable.getSelectedRow() < (mappingTable.getRowCount() - 1)));
	}

	/**
	 * Edit the selected mapping
	 */
	private void editSelected() {
		int idx = mappingTable.getSelectedRow();
		if (idx != -1) {
			FileTypeMapping p = ((FileTypeMappingModel) mappingTable.getModel()).getMappingAt(idx);
			OptionDialog.Option ok = new OptionDialog.Option("Update", "Update the file type", 'o');
			final OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel", 'c');
			final FileTypeMappingEditor mapping = new FileTypeMappingEditor(context, p, false);
			OptionDialog.Option opt = OptionDialog.showOptionDialog("editFileTypeMapping", context, FileTypeMappingPane.this,
					new OptionDialog.Option[]{ok, cancel}, mapping, "Edit file type", ok, new OptionDialog.Callback() {
						public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
							return (option == cancel) || mapping.validateTab();
						}

                        public void close(OptionDialog dialog, Option option) {
                            // TODO Auto-generated method stub
                            
                        }
					}

					, true, true);

			if (opt != ok) {
				return;
			}

			mapping.applyTab();
			mappingTable.repaint();

		}

	}

	//  Supporting classes
	//  Supporting classes
	class AddAction extends AbstractNormalAddAction {
		/**
		 * Constructor for the CutAction object
		 */
		AddAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			FileTypeMapping mapping = new FileTypeMapping();
			OptionDialog.Option ok = new OptionDialog.Option("Add", "Add the file type", 'o');
			final OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel", 'c');
			final FileTypeMappingEditor editor = new FileTypeMappingEditor(context, mapping);
			OptionDialog.Option opt = OptionDialog.showOptionDialog("addFileTypeMapping", context, FileTypeMappingPane.this,
					new OptionDialog.Option[]{ok, cancel}, editor, "Add file type", ok, new OptionDialog.Callback() {
						public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
							return (option == cancel) || editor.validateTab();
						}

                        public void close(OptionDialog dialog, Option option) {
                            // TODO Auto-generated method stub
                            
                        }
					}

					, false, true);

			if (opt != ok) {
				return;
			}

			editor.applyTab();
			context.getFileTypeMappingModel().addMapping(mapping);
		}
	}

	class UpAction extends AbstractTopAction {
		UpAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			int i = mappingTable.getSelectedRow();
			((FileTypeMappingModel) mappingTable.getModel()).moveUp(i);
			mappingTable.clearSelection();
			mappingTable.getSelectionModel().addSelectionInterval(i - 1, i - 1);
			mappingTable.scrollRectToVisible(mappingTable.getVisibleRect());
		}
	}

	class DownAction extends AbstractBottomAction {
		DownAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			int i = mappingTable.getSelectedRow();
			((FileTypeMappingModel) mappingTable.getModel()).moveDown(i);
			mappingTable.clearSelection();
			mappingTable.getSelectionModel().addSelectionInterval(i + 1, i + 1);
			mappingTable.scrollRectToVisible(mappingTable.getVisibleRect());
		}
	}

	class RemoveAction extends AbstractNormalRemoveAction {
		/**
		 * Constructor for the DeleteAction object
		 */
		RemoveAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			if (JOptionPane.showConfirmDialog(FileTypeMappingPane.this, "Are you sure you want to remove this file type?",
					"Remove file type", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIUtil.getCachedIcon(
							Constants.ICON_TOOL_LARGE_NORMAL_REMOVE)) == JOptionPane.YES_OPTION) {
				context.getFileTypeMappingModel().removeMappingAt(mappingTable.getSelectedRow());
			}
		}
	}

	class EditAction extends AbstractEditAction {
		/**
		 * Constructor for the DeleteAction object
		 */
		EditAction() {
			super();
		}

		public void actionPerformed(ActionEvent evt) {
			editSelected();
		}
	}
}