/*
 */
package gruntspud.ui.commandoptions;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.UIUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;

import org.netbeans.lib.cvsclient.command.log.LogCommand;


public class LogFilterTab
    extends AbstractTab
    implements ActionListener {
    private GruntspudContext context;
    private GruntspudCheckBox useDateFilter;
    private GruntspudCheckBox useRevisionFilter;
    private GruntspudCheckBox useStateFilter;
    private GruntspudCheckBox useUserFilter;
    private StringListComboBox dateFilter;
    private StringListComboBox revisionFilter;
    private StringListComboBox stateFilter;
    private StringListComboBox userFilter;

    /**
     *  Constructor for the CommitGeneralTab object
     */
    public LogFilterTab(GruntspudContext context, Icon icon, Icon largeIcon, String toolTipText, int mnemonic) {
        super("Filter", icon);
        this.context = context;
        setTabToolTipText(toolTipText);
        setLayout(new GridBagLayout());
        setTabMnemonic(mnemonic);
        setTabLargeIcon(largeIcon);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIUtil.jGridBagAdd(this,
                           useRevisionFilter = new GruntspudCheckBox("Revision"),
                           gbc,
                           GridBagConstraints.RELATIVE);
        useRevisionFilter.setSelected(context.getHost().getBooleanProperty(
            Constants.LOG_FILTER_USE_REVISION,
            false));
        useRevisionFilter.addActionListener(this);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           revisionFilter = new StringListComboBox(context,
            context.getHost().getProperty(Constants.LOG_FILTER_REVISION,
                                          ""), false), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, useDateFilter = new GruntspudCheckBox("Date"),
                           gbc, GridBagConstraints.RELATIVE);
        useDateFilter.setSelected(context.getHost().getBooleanProperty(
            Constants.LOG_FILTER_USE_DATE,
            false));
        useDateFilter.addActionListener(this);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           dateFilter = new StringListComboBox(context,
            context.getHost().getProperty(Constants.LOG_FILTER_DATE,
                                          ""), false), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, useStateFilter = new GruntspudCheckBox("State"),
                           gbc, GridBagConstraints.RELATIVE);
        useStateFilter.setSelected(context.getHost().getBooleanProperty(
            Constants.LOG_FILTER_USE_STATE,
            false));
        useStateFilter.addActionListener(this);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           stateFilter = new StringListComboBox(context,
            context.getHost().getProperty(Constants.LOG_FILTER_STATE,
                                          ""), false), gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(this, useUserFilter = new GruntspudCheckBox("User"),
                           gbc, GridBagConstraints.RELATIVE);
        useUserFilter.setSelected(context.getHost().getBooleanProperty(
            Constants.LOG_FILTER_USE_USER,
            false));
        useUserFilter.addActionListener(this);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(this,
                           userFilter = new StringListComboBox(context,
            context.getHost().getProperty(Constants.LOG_FILTER_USER,
                                          ""), false), gbc,
                           GridBagConstraints.REMAINDER);

        setAvailableActions();
    }
    
    public void setCommandForSettings(LogCommand cmd) {
      if (useDateFilter.isSelected() &&
          (dateFilter.getSelectedItem() != null) &&
          ( ( (String) dateFilter.getSelectedItem()).length() > 0)) {
          cmd.setRevisionFilter( (String) dateFilter.getSelectedItem());

      }
      if (useRevisionFilter.isSelected() &&
          (revisionFilter.getSelectedItem() != null) &&
          ( ( (String) revisionFilter.getSelectedItem()).length() > 0)) {
          cmd.setRevisionFilter( (String) revisionFilter.getSelectedItem());

      }
      if (useStateFilter.isSelected() &&
          (stateFilter.getSelectedItem() != null) &&
          ( ( (String) stateFilter.getSelectedItem()).length() > 0)) {
          cmd.setStateFilter( (String) stateFilter.getSelectedItem());

      }
      if (useUserFilter.isSelected() &&
          (userFilter.getSelectedItem() != null) &&
          ( ( (String) userFilter.getSelectedItem()).length() > 0)) {
          cmd.setUserFilter( (String) userFilter.getSelectedItem());

      }
    }
    
    public boolean isUseDateFilter() {
      return useDateFilter.isSelected();
    }
    
    public boolean isUseRevisionFilter() {
      return useRevisionFilter.isSelected();
    }
    
    public boolean isUseStateFilter() {
      return useStateFilter.isSelected();
    }
    
    public boolean isUseUserFilter() {
      return useUserFilter.isSelected();
    }
    
    public String getDateFilter() {
      return dateFilter.getSelectedItem().toString();
    }
    
    public String getRevisionFilter() {
      return revisionFilter.getSelectedItem().toString();
    }
    
    public String getStateFilter() {
      return stateFilter.getSelectedItem().toString();
    }
    
    public String getUserFilter() {
      return userFilter.getSelectedItem().toString();
    }

    public void actionPerformed(ActionEvent evt) {
        setAvailableActions();
    }

    private void setAvailableActions() {
        revisionFilter.setEnabled(useRevisionFilter.isSelected());
        dateFilter.setEnabled(useDateFilter.isSelected());
        stateFilter.setEnabled(useStateFilter.isSelected());
        userFilter.setEnabled(useUserFilter.isSelected());
    }

    public boolean validateTab() {
        return true;
    }

    public void applyTab() {
        context.getHost().setBooleanProperty(Constants.
                                             LOG_FILTER_USE_REVISION,
                                             useRevisionFilter.isSelected());
        context.getHost().setProperty(Constants.LOG_FILTER_REVISION,
                                      revisionFilter.
                                      getStringListPropertyString());

        context.getHost().setBooleanProperty(Constants.LOG_FILTER_USE_DATE,
                                             useDateFilter.isSelected());
        context.getHost().setProperty(Constants.LOG_FILTER_DATE,
                                      dateFilter.
                                      getStringListPropertyString());

        context.getHost().setBooleanProperty(Constants.LOG_FILTER_USE_STATE,
                                             useStateFilter.isSelected());
        context.getHost().setProperty(Constants.LOG_FILTER_STATE,
                                      stateFilter.
                                      getStringListPropertyString());

        context.getHost().setBooleanProperty(Constants.LOG_FILTER_USE_USER,
                                             useUserFilter.isSelected());
        context.getHost().setProperty(Constants.LOG_FILTER_USER,
                                      userFilter.
                                      getStringListPropertyString());
    }

    public void tabSelected() {
    }
}