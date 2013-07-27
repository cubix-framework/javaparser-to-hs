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

package projectviewer.config;

//{{{ Imports
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.OperatingSystem;

import projectviewer.ProjectViewer;
import projectviewer.gui.OptionPaneBase;
//}}}

/**
 *  <p>Option pane to configure the ProjectViewer plugin.</p>
 *
 *  @author		Marcelo Vanzin
 *	@version	$Id: ProjectViewerOptionsPane.java 16690 2009-12-21 01:22:31Z vanza $
 */
public class ProjectViewerOptionsPane extends OptionPaneBase
										implements ActionListener {

	//{{{ Instance variables
	private ProjectViewerConfig config;
	private JScrollPane			component;

	private JCheckBox useExternalApps;
	private JCheckBox closeFiles;
	private JCheckBox rememberOpen;
	private JCheckBox deleteNotFoundFiles;

	private JRadioButton autoImport;
	private JRadioButton askAlways;
	private JRadioButton askOnce;
	private JRadioButton askNever;

	private JCheckBox showFoldersTree;
	private JCheckBox showFilesTree;
	private JCheckBox showWorkingFilesTree;
	private JCheckBox showAllWorkingFiles;
	private JCheckBox showCompactTree;
	private JCheckBox showFilteredTree;
	private JCheckBox useSystemIcons;
	private JCheckBox showProjectInTitle;
	private JCheckBox caseInsensitiveSort;
	private JCheckBox followCurrentBuffer;

	private JTextArea importGlobs;
	private JTextField excludeDirs;

	private JCheckBox	useInfoViewer;
	private JTextField	browserExecPath;
	//}}}

	public ProjectViewerOptionsPane(String name)
	{
		super(name, "projectviewer.options");
		config = ProjectViewerConfig.getInstance();
	}

	/** Initializes the option pane. */
	protected void _init()
	{
		//{{{ general options
		addSeparator("options.projectviewer.general-opt.label");

		useExternalApps = addCheckBox("use_external_apps",
									  config.getUseExternalApps());

		closeFiles = addCheckBox("close_on_change",
								 config.getCloseFiles());

		rememberOpen = addCheckBox("remember_open",
								   config.getRememberOpen());

		deleteNotFoundFiles = addCheckBox("delete_stale",
										  config.getDeleteNotFoundFiles());

		//{{{ Button group: "ask import"
		addComponent(createLabel("ask_import"));

		JPanel pane = new JPanel(new FlowLayout());
		ButtonGroup bg = new ButtonGroup();

		autoImport = new JRadioButton(prop("ask_import.auto_import"));
		bg.add(autoImport);
		pane.add(autoImport);
		askAlways = new JRadioButton(prop("ask_import.always"));
		bg.add(askAlways);
		pane.add(askAlways);
		askOnce = new JRadioButton(prop("ask_import.once"));
		bg.add(askOnce);
		pane.add(askOnce);
		askNever = new JRadioButton(prop("ask_import.never"));
		bg.add(askNever);
		pane.add(askNever);

		switch (config.getAskImport()) {
			case ProjectViewerConfig.AUTO_IMPORT:
				autoImport.setSelected(true);
				break;

			case ProjectViewerConfig.ASK_ALWAYS:
				askAlways.setSelected(true);
				break;

			case ProjectViewerConfig.ASK_ONCE:
				askOnce.setSelected(true);
				break;

			case ProjectViewerConfig.ASK_NEVER:
				askNever.setSelected(true);
				break;
		}

		addComponent(pane);
		//}}}

		//}}}

		//{{{ gui options
		addSeparator("options.projectviewer.gui-opt.label");

		showFoldersTree = addCheckBox("show_folders",
									  config.getShowFoldersTree());

		showFilesTree = addCheckBox("show_files",
									config.getShowFilesTree());

		showWorkingFilesTree = new JCheckBox(jEdit.getProperty("projectviewer.options.show_working_files"));
		String tooltip = jEdit.getProperty("show_working_files.tooltip");
		if (tooltip != null) {
			showWorkingFilesTree.setToolTipText(tooltip);
		}

		showWorkingFilesTree.setSelected(config.getShowWorkingFilesTree());
		showWorkingFilesTree.addActionListener(this);
		showAllWorkingFiles = new JCheckBox(jEdit.getProperty("projectviewer.options.show_all_working_files"));
		tooltip = jEdit.getProperty("show_all_working_files.tooltip");
		if (tooltip != null) {
			showAllWorkingFiles.setToolTipText(tooltip);
		}
		showAllWorkingFiles.setEnabled(showWorkingFilesTree.isSelected());
		showAllWorkingFiles.setSelected(config.getShowAllWorkingFiles());

		JPanel workingFilesPanel = new JPanel();
		workingFilesPanel.add(showWorkingFilesTree);
		workingFilesPanel.add(showAllWorkingFiles);
		addComponent(workingFilesPanel);

		showCompactTree = addCheckBox("show_compact_tree",
									  config.getShowCompactTree());

		showFilteredTree = addCheckBox("show_filtered_tree",
									   config.getShowFilteredTree());

		useSystemIcons = addCheckBox("use_system_icons",
									 config.getUseSystemIcons());

		showProjectInTitle = addCheckBox("show_project_in_title",
										 config.getShowProjectInTitle());

		caseInsensitiveSort = addCheckBox("case_insensitive_sort",
										  config.getCaseInsensitiveSort());

		followCurrentBuffer = addCheckBox("follow_current_buffer",
										  config.getFollowCurrentBuffer());

		//}}}

		//{{{ importer options
		addSeparator("options.projectviewer.importer-opt.label");

		importGlobs = new JTextArea(5, 4);
		importGlobs.setLineWrap(true);
		if (config.getImportGlobs() != null) {
			importGlobs.setText(config.getImportGlobs());
		}
		importGlobs.setToolTipText(prop("import_globs.tooltip"));
		addComponent(prop("import_globs"),
					 new JScrollPane(importGlobs,
									 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
									 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),
					 GridBagConstraints.BOTH);

		excludeDirs = new JTextField(5);
		if (config.getExcludeDirs() != null) {
			excludeDirs.setText(config.getExcludeDirs());
		}
		addComponent(excludeDirs, "ignore_dir");
		//}}}

		//{{{ web project options
		addSeparator("options.projectviewer.web-prj-opt.label");

		useInfoViewer = addCheckBox("use_info_viewer",
									config.getUseInfoViewer());
		useInfoViewer.addActionListener(this);
		useInfoViewer.setEnabled(config.isInfoViewerAvailable());

		browserExecPath = new JTextField(5);
		if (config.getBrowserPath() != null) {
			browserExecPath.setText(config.getBrowserPath());
		}
		addComponent(browserExecPath, "browser_path");
		browserExecPath.setEnabled(!useInfoViewer.isSelected());
		//}}}

	} //}}}

	//{{{ #_save() : void
	/** Saves the options. */
	protected void _save() {
		config.setUseExternalApps(useExternalApps.isSelected());
		config.setCloseFiles(closeFiles.isSelected());
		config.setRememberOpen(rememberOpen.isSelected());
		config.setDeleteNotFoundFiles(deleteNotFoundFiles.isSelected());

		config.setShowFoldersTree(showFoldersTree.isSelected());
		config.setShowFilesTree(showFilesTree.isSelected());
		config.setShowWorkingFilesTree(showWorkingFilesTree.isSelected());
		config.setShowAllWorkingFiles(showAllWorkingFiles.isSelected());
		config.setShowCompactTree(showCompactTree.isSelected());
		config.setShowFilteredTree(showFilteredTree.isSelected());
		config.setUseSystemIcons(useSystemIcons.isSelected());

		config.setShowProjectInTitle(showProjectInTitle.isSelected());

		config.setCaseInsensitiveSort(caseInsensitiveSort.isSelected());
		config.setFollowCurrentBuffer(followCurrentBuffer.isSelected());

		if (askAlways.isSelected()) {
			config.setAskImport(ProjectViewerConfig.ASK_ALWAYS);
		} else if (askOnce.isSelected()) {
			config.setAskImport(ProjectViewerConfig.ASK_ONCE);
		} else if (askNever.isSelected()) {
			config.setAskImport(ProjectViewerConfig.ASK_NEVER);
		} else {
			config.setAskImport(ProjectViewerConfig.AUTO_IMPORT);
		}

		config.setImportGlobs(importGlobs.getText());
		config.setExcludeDirs(excludeDirs.getText());
		config.setBrowserpath(browserExecPath.getText());
		config.setUseInfoViewer(useInfoViewer.isSelected());
		config.save();

		ProjectViewer pv = ProjectViewer.getViewer(jEdit.getActiveView());
		if (pv != null) {
			pv.repaint();
		}
	} //}}}

	//{{{ +getComponent() : Component
	public Component getComponent() {
		if (component == null) {
			component = new JScrollPane(super.getComponent());
		}
		return component;
	} //}}}

	//{{{ +actionPerformed(ActionEvent) : void
	/** Waits for events in some of the fields.. */
	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() == useInfoViewer) {
			browserExecPath.setEnabled(!useInfoViewer.isSelected());
		} else if (ae.getSource() == showWorkingFilesTree) {
			showAllWorkingFiles.setEnabled(showWorkingFilesTree.isSelected());
			if (!showWorkingFilesTree.isSelected()) {
				showAllWorkingFiles.setSelected(false);
			}
		}

	} //}}}

}

