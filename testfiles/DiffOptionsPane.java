/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package gruntspud.ui.commandoptions;

import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.FileNameTextField;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.GruntspudRadioButton;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.preferences.GlobalOptionsTab;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.diff.DiffCommand;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;

/**
 *  I hate the way diff works. High on list of rewrite material ...
 *
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class DiffOptionsPane extends Tabber {
  private GruntspudRadioButton localWithFile;

  private FileNameTextField file;

  private GruntspudRadioButton localWithSameRemote;

  private GruntspudRadioButton localWithDifferentRemote;

  private GruntspudRadioButton revisionWithRevision;

  private GruntspudRadioButton revision1DateType;

  private GruntspudRadioButton revision1RevisionOrTagOrBranchType;

  private GruntspudRadioButton revision2DateType;

  private GruntspudRadioButton revision2RevisionOrTagOrBranchType;

  private GruntspudRadioButton hostDiffViewer;

  private GruntspudRadioButton consoleDiffViewer;

  private GruntspudRadioButton otherDiffViewer;

  private StringListComboBox revision1Date;

  private StringListComboBox revision1RevisionOrTagOrBranch;

  private StringListComboBox revision2Date;

  private StringListComboBox revision2RevisionOrTagOrBranch;

  private GruntspudCheckBox ignoreCase;

  private GruntspudCheckBox ignoreBlankLines;

  private GruntspudCheckBox unifiedDiff;

  private GruntspudCheckBox ignoreAllWhitepsace;

  private GruntspudCheckBox ignoreSpaceChanged;

  private XTextField otherDiffViewerCommand;

  private GlobalOptionsTab globalOptionsTab;

  private DiffGeneralTab generalTab;

  private DiffIgnoreTab ignoreTab;

  private DiffViewerTab viewerTab;

  private GruntspudContext context;

  private JPanel rev1;

  private JPanel rev2;

  private CVSFileNode[] sel;

  /**
   * Constructor for the DiffOptionsPane object
   * 
   * @param host Description of the Parameter
   */
  public DiffOptionsPane(GruntspudContext context, CVSFileNode[] sel) {
    super();
    this.context = context;
    this.sel = sel;
    generalTab = new DiffGeneralTab();
    ignoreTab = new DiffIgnoreTab();
    viewerTab = new DiffViewerTab();
    globalOptionsTab = new GlobalOptionsTab();
    globalOptionsTab.init(context);
    addTab(generalTab);
    addTab(ignoreTab);
    addTab(viewerTab);
    addTab(globalOptionsTab);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isRevisionWithRevision() {
    return revisionWithRevision.isSelected();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isLocalWithFile() {
    return localWithFile.isSelected();
  }

  public File getFile() {
    return new File(file.getText());
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isLocalWithDifferentRemote() {
    return localWithDifferentRemote.isSelected();
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean isLocalWithSameRemote() {
    return localWithSameRemote.isSelected();
  }

  /**
   * Gets the commandsForSettings attribute of the DiffOptionsPane object
   * 
   * @return The commandsForSettings value
   */
  public Command[] getCommandsForSettings() {
    if (consoleDiffViewer.isSelected()) {
      DiffCommand cmd = new DiffCommand();
      cmd.setUnifiedDiff(unifiedDiff.isSelected());
      cmd.setIgnoreAllWhitespace(ignoreAllWhitepsace.isSelected());
      cmd.setIgnoreCase(ignoreCase.isSelected());
      cmd.setIgnoreBlankLines(ignoreBlankLines.isSelected());
      cmd.setIgnoreSpaceChange(ignoreSpaceChanged.isSelected());
      if (localWithDifferentRemote.isSelected()
          || revisionWithRevision.isSelected()) {
        if (revision1DateType.isSelected()
            && (revision1Date.getSelectedItem() != null)
            && (((String) revision1Date.getSelectedItem()).length() > 0)) {
          cmd.setBeforeDate1((String) revision1Date.getSelectedItem());
        }
        if (revision1RevisionOrTagOrBranchType.isSelected()
            && (revision1RevisionOrTagOrBranch.getSelectedItem() != null)
            && (((String) revision1RevisionOrTagOrBranch.getSelectedItem())
                .length() > 0)) {
          cmd.setRevision1((String) revision1RevisionOrTagOrBranch
              .getSelectedItem());
        }
        if (revisionWithRevision.isSelected()) {
          if (revision2DateType.isSelected()
              && (revision2Date.getSelectedItem() != null)
              && (((String) revision2Date.getSelectedItem()).length() > 0)) {
            cmd.setBeforeDate2((String) revision2Date.getSelectedItem());
          }
          if (revision2RevisionOrTagOrBranchType.isSelected()
              && (revision2RevisionOrTagOrBranch.getSelectedItem() != null)
              && (((String) revision2RevisionOrTagOrBranch.getSelectedItem())
                  .length() > 0)) {
            cmd.setRevision2((String) revision2RevisionOrTagOrBranch
                .getSelectedItem());
          }
        }
      }
      return new Command[]{cmd};
    } else {
      Command[] cmd = new Command[revisionWithRevision.isSelected() ? 2 : 1];
      UpdateCommand cmd1 = new UpdateCommand();
      if (revisionWithRevision.isSelected()
          || localWithDifferentRemote.isSelected()) {
        if (revision1DateType.isSelected()
            && (revision1Date.getSelectedItem() != null)
            && (((String) revision1Date.getSelectedItem()).length() > 0)) {
          cmd1.setUpdateByDate((String) revision1Date.getSelectedItem());
        }
        if (revision1RevisionOrTagOrBranchType.isSelected()
            && (revision1RevisionOrTagOrBranch.getSelectedItem() != null)
            && (((String) revision1RevisionOrTagOrBranch.getSelectedItem())
                .length() > 0)) {
          cmd1.setUpdateByRevision((String) revision1RevisionOrTagOrBranch
              .getSelectedItem());
        }
      }
      cmd1.setPipeToOutput(true);
      cmd[0] = cmd1;
      if (revisionWithRevision.isSelected()) {
        UpdateCommand cmd2 = new UpdateCommand();
        if (revisionWithRevision.isSelected()) {
          if (revision2DateType.isSelected()
              && (revision2Date.getSelectedItem() != null)
              && (((String) revision2Date.getSelectedItem()).length() > 0)) {
            cmd2.setUpdateByDate((String) revision2Date.getSelectedItem());
          }
          if (revision2RevisionOrTagOrBranchType.isSelected()
              && (revision2RevisionOrTagOrBranch.getSelectedItem() != null)
              && (((String) revision2RevisionOrTagOrBranch.getSelectedItem())
                  .length() > 0)) {
            cmd2.setUpdateByRevision((String) revision2RevisionOrTagOrBranch
                .getSelectedItem());
          }
        }
        cmd2.setPipeToOutput(true);
        cmd[1] = cmd2;
      }
      return cmd;
    }
  }

  class DiffGeneralTab extends AbstractTab implements ActionListener {
    /**
     * Constructor for the DiffGeneralTab object
     */
    DiffGeneralTab() {
      super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_DIFF));
      setTabToolTipText("General options for a diff");
      setLayout(new GridBagLayout());
      setTabMnemonic('g');
      setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_DIFF));
      Insets i1 = new Insets(3, 3, 3, 3);
      Insets i2 = new Insets(3, 24, 3, 3);
      rev1 = new JPanel(new GridBagLayout());
      rev1.setOpaque(false);
      rev1.setBorder(BorderFactory.createTitledBorder("Revision 1"));
      GridBagConstraints gbc1 = new GridBagConstraints();
      gbc1.insets = i1;
      gbc1.anchor = GridBagConstraints.NORTHWEST;
      gbc1.fill = GridBagConstraints.HORIZONTAL;
      ButtonGroup bg1 = new ButtonGroup();
      String rev1Type = context.getHost().getProperty(
          Constants.DIFF_GENERAL_REVISION_1_TYPE, "revisionOrTagOrBranch");
      revision1RevisionOrTagOrBranchType = new GruntspudRadioButton(
          "Revision/Tag/Branch", rev1Type.equals("revisionOrTagOrBranch"));
      revision1RevisionOrTagOrBranchType.setMnemonic('s');
      revision1RevisionOrTagOrBranchType.addActionListener(this);
      UIUtil.jGridBagAdd(rev1, revision1RevisionOrTagOrBranchType, gbc1,
          GridBagConstraints.REMAINDER);
      bg1.add(revision1RevisionOrTagOrBranchType);
      gbc1.insets = i2;
      revision1RevisionOrTagOrBranch = new StringListComboBox(context, context.getHost().getProperty(
          Constants.DIFF_GENERAL_REVISION_1_REVISION_OR_TAG_OR_BRANCH, ""),
          false);
      UIUtil.jGridBagAdd(rev1, revision1RevisionOrTagOrBranch, gbc1,
          GridBagConstraints.REMAINDER);
      gbc1.insets = i1;
      revision1DateType = new GruntspudRadioButton("Date", rev1Type.equals("date"));
      revision1DateType.setMnemonic('d');
      revision1DateType.addActionListener(this);
      UIUtil.jGridBagAdd(rev1, revision1DateType, gbc1,
          GridBagConstraints.REMAINDER);
      bg1.add(revision1DateType);
      gbc1.insets = i2;
      gbc1.weighty = 1.0;
      revision1Date = new StringListComboBox(context, context.getHost()
          .getProperty(Constants.DIFF_GENERAL_REVISION_1_DATE, ""), false);
      UIUtil.jGridBagAdd(rev1, revision1Date, gbc1,
          GridBagConstraints.REMAINDER);
      rev2 = new JPanel(new GridBagLayout());
      rev2.setOpaque(false);
      rev2.setBorder(BorderFactory.createTitledBorder("Revision 2"));
      GridBagConstraints gbc2 = new GridBagConstraints();
      gbc2.insets = i1;
      gbc2.anchor = GridBagConstraints.NORTHWEST;
      gbc2.fill = GridBagConstraints.HORIZONTAL;
      ButtonGroup bg2 = new ButtonGroup();
      String rev2Type = context.getHost().getProperty(
          Constants.DIFF_GENERAL_REVISION_2_TYPE, "revisionOrTagOrBranch");
      revision2RevisionOrTagOrBranchType = new GruntspudRadioButton(
          "Revision/Tag/Branch", rev1Type.equals("revisionOrTagOrBranch"));
      revision2RevisionOrTagOrBranchType.setMnemonic('b');
      revision2RevisionOrTagOrBranchType.addActionListener(this);
      UIUtil.jGridBagAdd(rev2, revision2RevisionOrTagOrBranchType, gbc2,
          GridBagConstraints.REMAINDER);
      bg2.add(revision2RevisionOrTagOrBranchType);
      gbc2.insets = i2;
      revision2RevisionOrTagOrBranch = new StringListComboBox(context, context.getHost().getProperty(
          Constants.DIFF_GENERAL_REVISION_2_REVISION_OR_TAG_OR_BRANCH, ""),
          false);
      UIUtil.jGridBagAdd(rev2, revision2RevisionOrTagOrBranch, gbc2,
          GridBagConstraints.REMAINDER);
      gbc2.insets = i1;
      revision2DateType = new GruntspudRadioButton("Date", rev2Type.equals("date"));
      revision2DateType.setMnemonic('t');
      revision2DateType.addActionListener(this);
      UIUtil.jGridBagAdd(rev2, revision2DateType, gbc2,
          GridBagConstraints.REMAINDER);
      bg2.add(revision2DateType);
      gbc2.insets = i2;
      gbc2.weighty = 1.0;
      revision2Date = new StringListComboBox(context, context.getHost()
          .getProperty(Constants.DIFF_GENERAL_REVISION_1_DATE, ""), false);
      //			LogCommand cmd[]={new LogCommand()};
      //			
      //			cmd[0].setDefaultBranch(true);
      //			cmd[0].setRecursive(true);
      //			cmd[0].setHeaderOnly(false);
      //			cmd[0].setHeaderAndDescOnly(false);
      //			cmd[0].setNoTags(false);
      //			CVSCommandHandler.getInstance().runCommandGroup(DiffOptionsPane.this,
      // context,
      // context.getViewManager().getNodesToPerformActionOn()[0].getFile(), cmd,
      // context.getViewManager().getNodesToPerformActionOn(), null, false,
      // null, null, new VersionAdapter(revision1RevisionOrTagOrBranch,
      // revision2RevisionOrTagOrBranch), null);
      UIUtil.jGridBagAdd(rev2, revision2Date, gbc2,
          GridBagConstraints.REMAINDER);
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = i1;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      ButtonGroup bg = new ButtonGroup();
      String viewerType = context.getHost().getProperty(
          Constants.DIFF_GENERAL_TYPE, "localWithSameRemote");
      localWithFile = new GruntspudRadioButton("Local with file");
      
      localWithFile.setMnemonic('f');
      localWithFile.addActionListener(this);
      UIUtil
          .jGridBagAdd(this, localWithFile, gbc, GridBagConstraints.REMAINDER);
      bg.add(localWithFile);
      gbc.insets = i2;
      String path = context.getHost().getProperty(Constants.DIFF_GENERAL_FILE,
          "");
      JFileChooser chooser = new JFileChooser(context.getViewManager()
          .getHome());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogTitle("Choose file to diff with");
      chooser.setApproveButtonText("Diff");
      chooser.setApproveButtonMnemonic('d');
      chooser
          .setApproveButtonToolTipText("Diff the selection with the selected file");
      chooser.setFileHidingEnabled(false);
      file = new FileNameTextField(null, path, 15, true, true, chooser, false);
      UIUtil.jGridBagAdd(this, file, gbc, GridBagConstraints.REMAINDER);
      gbc.insets = i1;
      localWithSameRemote = new GruntspudRadioButton("Local with same remote revision");
      localWithSameRemote.setMnemonic('s');
      localWithSameRemote.addActionListener(this);
      UIUtil.jGridBagAdd(this, localWithSameRemote, gbc,
          GridBagConstraints.REMAINDER);
      bg.add(localWithSameRemote);
      localWithDifferentRemote = new GruntspudRadioButton(
          "Local revision with different revision/tag/branch/date");
      

      localWithDifferentRemote.setMnemonic('d');
      localWithDifferentRemote.addActionListener(this);
      UIUtil.jGridBagAdd(this, localWithDifferentRemote, gbc,
          GridBagConstraints.REMAINDER);
      bg.add(localWithDifferentRemote);
      revisionWithRevision = new GruntspudRadioButton(
          "Two different revisions/tags/branches/dates");
      revisionWithRevision.setMnemonic('r');
      revisionWithRevision.addActionListener(this);
      UIUtil.jGridBagAdd(this, revisionWithRevision, gbc,
          GridBagConstraints.REMAINDER);
      bg.add(revisionWithRevision);

      if(viewerType.equals("revisionWithRevision")) {
        revisionWithRevision.setSelected(true);
      }
      else if(viewerType.equals("localWithFile")) {
        localWithFile.setSelected(true);
      } 
      else if(viewerType.equals("localWithDifferentRemote")){
        localWithDifferentRemote.setSelected(true);        
      }
      else {
        localWithSameRemote.setSelected(true);
      }      
      
      JPanel b = new JPanel(new GridLayout(1, 2));
      b.setOpaque(false);
      b.add(rev1);
      b.add(rev2);
      gbc.weighty = 1.0;
      UIUtil.jGridBagAdd(this, b, gbc, GridBagConstraints.REMAINDER);
      if (bg.getSelection() == null) {
        localWithSameRemote.setSelected(true);
      }
      setAvailableActions();
    }

    private void setAvailableActions() {
      boolean rev1Enabled = localWithDifferentRemote.isSelected()
          || revisionWithRevision.isSelected();
      revision1RevisionOrTagOrBranchType.setEnabled(rev1Enabled);
      revision1DateType.setEnabled(rev1Enabled);
      revision1RevisionOrTagOrBranch.setEnabled(rev1Enabled);
      revision1Date.setEnabled(rev1Enabled);
      localWithFile.setEnabled(sel.length == 1);
      file.setEnabled(localWithFile.isEnabled() && localWithFile.isSelected());
      boolean rev2Enabled = revisionWithRevision.isSelected();
      revision2RevisionOrTagOrBranchType.setEnabled(rev2Enabled);
      revision2DateType.setEnabled(rev2Enabled);
      revision2RevisionOrTagOrBranch.setEnabled(rev2Enabled);
      revision2Date.setEnabled(rev2Enabled);
    }

    public void actionPerformed(ActionEvent evt) {
      setAvailableActions();
    }

    public boolean validateTab() {
      if (localWithFile.isSelected()) {
        File f = new File(file.getText());
        if (!f.exists() || !f.canRead()) {
          JOptionPane.showMessageDialog(this, "File " + f.getPath()
              + " does not exist.");
          return false;
        }
      }
      return true;
    }

    public void applyTab() {
      context
          .getHost()
          .setProperty(
              Constants.DIFF_GENERAL_REVISION_1_TYPE,
              revision1RevisionOrTagOrBranchType.isSelected() ? "revisionOrTagOrBranch"
                  : (revision1DateType.isSelected() ? "date" : "none"));
      context
          .getHost()
          .setProperty(
              Constants.DIFF_GENERAL_REVISION_2_TYPE,
              revision2RevisionOrTagOrBranchType.isSelected() ? "revisionOrTagOrBranch"
                  : (revision2DateType.isSelected() ? "date" : "none"));
      context.getHost().setProperty(
          Constants.DIFF_GENERAL_TYPE,
          localWithFile.isSelected() ? "localWithFile" : (localWithSameRemote
              .isSelected() ? "localWithSameRemote" : (localWithDifferentRemote
              .isSelected() ? "localWithDifferentRemote"
              : "revisionWithRevision")));
      context.getHost()
          .setProperty(Constants.DIFF_GENERAL_FILE, file.getText());
      context.getHost().setProperty(
          Constants.DIFF_GENERAL_REVISION_1_REVISION_OR_TAG_OR_BRANCH,
          revision1RevisionOrTagOrBranch.getStringListPropertyString());
      context.getHost().setProperty(Constants.DIFF_GENERAL_REVISION_1_DATE,
          revision1Date.getStringListPropertyString());
      context.getHost().setProperty(
          Constants.DIFF_GENERAL_REVISION_2_REVISION_OR_TAG_OR_BRANCH,
          revision2RevisionOrTagOrBranch.getStringListPropertyString());
      context.getHost().setProperty(Constants.DIFF_GENERAL_REVISION_2_DATE,
          revision2Date.getStringListPropertyString());
    }

    public void tabSelected() {
    }
  }

  class DiffIgnoreTab extends AbstractTab {
    /**
     * Constructor for the DiffIgnoreTab object
     */
    DiffIgnoreTab() {
      super("Ignore", UIUtil.getCachedIcon(Constants.ICON_TOOL_IGNORE));
      setTabToolTipText("Things to ignore in the diff");
      setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_IGNORE));
      setLayout(new GridBagLayout());
      setTabMnemonic('i');
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(3, 3, 3, 3);
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1.0;
      ignoreCase = new GruntspudCheckBox("Ignore case", context.getHost()
          .getBooleanProperty(Constants.DIFF_IGNORE_CASE, false));
      ignoreCase.setMnemonic('c');
      UIUtil.jGridBagAdd(this, ignoreCase, gbc, GridBagConstraints.REMAINDER);
      ignoreBlankLines = new GruntspudCheckBox("Ignore blank lines", context.getHost()
          .getBooleanProperty(Constants.DIFF_IGNORE_BLANK_LINES, false));
      ignoreBlankLines.setMnemonic('b');
      UIUtil.jGridBagAdd(this, ignoreBlankLines, gbc,
          GridBagConstraints.REMAINDER);
      ignoreAllWhitepsace = new GruntspudCheckBox("Ignore all whitespace", context
          .getHost().getBooleanProperty(Constants.DIFF_IGNORE_ALL_WHITESPACE,
              false));
      ignoreAllWhitepsace.setMnemonic('w');
      UIUtil.jGridBagAdd(this, ignoreAllWhitepsace, gbc,
          GridBagConstraints.REMAINDER);
      gbc.weighty = 1.0;
      ignoreSpaceChanged = new GruntspudCheckBox("Ignore space changed", context
          .getHost().getBooleanProperty(Constants.DIFF_IGNORE_SPACE_CHANGED,
              false));
      ignoreSpaceChanged.setMnemonic('s');
      UIUtil.jGridBagAdd(this, ignoreSpaceChanged, gbc,
          GridBagConstraints.REMAINDER);
    }

    public boolean validateTab() {
      return true;
    }

    public void applyTab() {
      context.getHost().setBooleanProperty(Constants.DIFF_IGNORE_CASE,
          ignoreCase.isSelected());
      context.getHost().setBooleanProperty(Constants.DIFF_IGNORE_BLANK_LINES,
          ignoreBlankLines.isSelected());
      context.getHost().setBooleanProperty(
          Constants.DIFF_IGNORE_ALL_WHITESPACE,
          ignoreAllWhitepsace.isSelected());
      context.getHost().setBooleanProperty(Constants.DIFF_IGNORE_SPACE_CHANGED,
          ignoreSpaceChanged.isSelected());
    }

    public void tabSelected() {
    }
  }

  class DiffViewerTab extends AbstractTab implements ActionListener {
    /**
     * Constructor for the DiffViewerTab object
     */
    DiffViewerTab() {
      super("Viewer", UIUtil.getCachedIcon(Constants.ICON_TOOL_FILE_TYPES));
      setTabToolTipText("Select the viewer to show the differences");
      setLayout(new GridBagLayout());
      setTabMnemonic('v');
      setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_FILE_TYPES));
      GridBagConstraints gbc = new GridBagConstraints();
      Insets i1 = new Insets(3, 3, 3, 3);
      Insets i2 = new Insets(3, 24, 3, 3);
      gbc.insets = i1;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 2.0;
      ButtonGroup bg = new ButtonGroup();
      String viewerType = context.getHost().getProperty(
          Constants.DIFF_VIEWER_TYPE, "console");
      if (context.getHost().getDiffViewerName() != null) {
        hostDiffViewer = new GruntspudRadioButton(
            context.getHost().getDiffViewerName(), viewerType
                .equals(Constants.DIFF_VIEWER_HOST));
        hostDiffViewer.setMnemonic('h');
        hostDiffViewer.addActionListener(this);
        UIUtil.jGridBagAdd(this, hostDiffViewer, gbc,
            GridBagConstraints.REMAINDER);
        bg.add(hostDiffViewer);
      }
      consoleDiffViewer = new GruntspudRadioButton("Gruntspud console", viewerType
          .equals(Constants.DIFF_VIEWER_CONSOLE));
      consoleDiffViewer.setMnemonic('c');
      consoleDiffViewer.addActionListener(this);
      UIUtil.jGridBagAdd(this, consoleDiffViewer, gbc,
          GridBagConstraints.REMAINDER);
      bg.add(consoleDiffViewer);
      gbc.insets = i2;
      gbc.weightx = 2.0;
      unifiedDiff = new GruntspudCheckBox("Unified diff.", context
          .getHost().getBooleanProperty(Constants.DIFF_VIEWER_UNIFIED_DIFF, false));
      unifiedDiff.setMnemonic('u');
      UIUtil.jGridBagAdd(this, unifiedDiff, gbc, GridBagConstraints.REMAINDER);
      gbc.insets = i1;
      gbc.weightx = 0.0;
      
      otherDiffViewer = new GruntspudRadioButton("Other diff viewer", viewerType
          .equals(Constants.DIFF_VIEWER_OTHER));
      otherDiffViewer.setMnemonic('o');
      otherDiffViewer.addActionListener(this);
      UIUtil.jGridBagAdd(this, otherDiffViewer, gbc,
          GridBagConstraints.REMAINDER);
      bg.add(otherDiffViewer);
      gbc.insets = i2;
      gbc.weightx = 2.0;
      UIUtil.jGridBagAdd(this, new JLabel("Command"), gbc,
          GridBagConstraints.RELATIVE);
      gbc.weighty = 1.0;
      UIUtil.jGridBagAdd(this, otherDiffViewerCommand = new XTextField(context
          .getHost().getProperty(Constants.DIFF_VIEWER_OTHER_COMMAND, "diff"),
          15), gbc, GridBagConstraints.REMAINDER);
      setAvailableActions();
    }

    public void actionPerformed(ActionEvent evt) {
      setAvailableActions();
    }

    public boolean validateTab() {
      return true;
    }

    private void setAvailableActions() {
      otherDiffViewerCommand.setEnabled(otherDiffViewer.isSelected());
      unifiedDiff.setEnabled(consoleDiffViewer.isSelected());
    }

    public void applyTab() {
      context.getHost().setProperty(
          Constants.DIFF_VIEWER_TYPE,
          otherDiffViewer.isSelected() ? Constants.DIFF_VIEWER_OTHER
              : (consoleDiffViewer.isSelected() ? Constants.DIFF_VIEWER_CONSOLE
                  : Constants.DIFF_VIEWER_HOST));
      context.getHost().setProperty(Constants.DIFF_VIEWER_OTHER_COMMAND,
          otherDiffViewerCommand.getText());
      context.getHost().setBooleanProperty(Constants.DIFF_VIEWER_UNIFIED_DIFF,
          unifiedDiff.isSelected());
    }

    public void tabSelected() {
    }
  }
  /*
   * private class VersionAdapter extends GruntspudCVSAdapter { private
   * StringListComboBox jbox1,jbox2; VersionAdapter(StringListComboBox jbox1,
   * StringListComboBox jbox2) { this.jbox1=jbox1; this.jbox2=jbox2; } public
   * String getShortName(){return "what"; }
   * 
   * public void fileInfoGenerated(FileInfoEvent arg0) { LogInformation info =
   * (LogInformation) arg0.getInfoContainer(); List revs=info.getRevisionList();
   * 
   * String verStr=null; for (int i = revs.size()-1; i>=0; i--) {
   * LogInformation.Revision rev=(LogInformation.Revision) revs.get(i);
   * jbox1.addAndSelectString(""+rev.getNumber() + " " +rev.getMessage());
   * jbox2.addAndSelectString(""+rev.getNumber() + " " +rev.getMessage()); }
   *  } public void commandGroupFinished() {
   *  }
   *  }
   */
}