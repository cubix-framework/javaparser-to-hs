/*
 * DockerOptionPane.java
 * :tabSize=3:indentSize=3:noTabs=true:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Calvin Yu
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

package docker;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.gui.DockableWindowManager;

/**
 * Option pane for the docker plugin.
 */
@SuppressWarnings("serial")
public class DockerOptionPane extends AbstractOptionPane {
   private JCheckBox topEnabled, leftEnabled, bottomEnabled, rightEnabled;
   private DockablesComboBox addAutoHideOverrideCombo;
   private DockablesList autoHideOverrides;
   private DockerConfig config;
   private JButton removeAutoHideButton;

   private String[] dockables = DockableWindowManager.getRegisteredDockableWindows();

   /**
    * Create a new <code>DockerOptionPane</code>
    */
   public DockerOptionPane() {
      super("docker");
      config = DockerPlugin.getPlugin().getConfig();
   }

   /**
    * Initialize this option pane.
    */
   public void _init() {
      JPanel autoHide = new JPanel(new FlowLayout(FlowLayout.LEFT));

      topEnabled = createAutoHideCheckBox(DockableWindowManager.TOP);
      autoHide.add(topEnabled);

      leftEnabled = createAutoHideCheckBox(DockableWindowManager.LEFT);
      autoHide.add(leftEnabled);

      bottomEnabled = createAutoHideCheckBox(DockableWindowManager.BOTTOM);
      autoHide.add(bottomEnabled);

      rightEnabled = createAutoHideCheckBox(DockableWindowManager.RIGHT);
      autoHide.add(rightEnabled);

      addComponent(config.getProperty("label.auto-hide-enabled"), autoHide);

      addComponent(createAutoHideOverridePanel());
      filterCombo();
   }

   /**
    * Save the option.
    */
   public void _save()
   {
      config.setAutoHideEnabled(DockableWindowManager.TOP,
                                          topEnabled.isSelected());
      config.setAutoHideEnabled(DockableWindowManager.LEFT,
                                          leftEnabled.isSelected());
      config.setAutoHideEnabled(DockableWindowManager.BOTTOM,
                                          bottomEnabled.isSelected());
      config.setAutoHideEnabled(DockableWindowManager.RIGHT,
                                          rightEnabled.isSelected());
      List<String> overrides = autoHideOverrides.getDockables();
      config.setAutoHideOverrides(overrides);
   }

   private JComponent createAutoHideOverridePanel() {
      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();

      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0; gbc.gridy = 0;
      gbc.gridwidth = 2;
      gbc.insets = new Insets(0, 0, 11, 0);
      gbc.weightx = 1;
      panel.add(new JLabel(config.getProperty("label.auto-hide-overrides")), gbc);

      addAutoHideOverrideCombo = new DockablesComboBox();
      gbc.gridy++;
      gbc.gridwidth = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = .999;
      gbc.insets = new Insets(0, 0, 11, 11);
      panel.add(addAutoHideOverrideCombo, gbc);

      JButton addAutoHideOverrideButton =
            new JButton(config.getProperty("label.add-auto-hide-override"));
      addAutoHideOverrideButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            autoHideOverrides.addDockable(addAutoHideOverrideCombo.getSelectedDockableName());
         }
      });
      gbc.gridx++;
      gbc.weightx = .001;
      gbc.ipadx = 11;
      gbc.insets = new Insets(0, 0, 11, 0);
      panel.add(addAutoHideOverrideButton, gbc);

      autoHideOverrides = new DockablesList();
      autoHideOverrides.getModel().addListDataListener(new ListDataListener() {
         public void contentsChanged(ListDataEvent evt) {}
         public void intervalAdded(ListDataEvent evt) {
            filterCombo();
         }
         public void intervalRemoved(ListDataEvent evt) {
            filterCombo();
         }
      });
      autoHideOverrides.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent evt) {
            removeAutoHideButton.setEnabled(autoHideOverrides.hasSelection());
         }
      });
      for (Iterator<String> i = config.getAutoHideOverrides().iterator(); i.hasNext();) {
         autoHideOverrides.addDockable(i.next());
      }
      gbc.gridx = 0; gbc.gridy++;
      gbc.gridheight = 2;
      gbc.weightx = .999;
      gbc.ipadx = 0;
      gbc.insets = new Insets(0, 0, 0, 11);
      panel.add(new JScrollPane(autoHideOverrides), gbc);

      removeAutoHideButton =
         new JButton(config.getProperty("label.remove-auto-hide-override"));
      removeAutoHideButton.setEnabled(false);
      removeAutoHideButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            autoHideOverrides.removeDockable(autoHideOverrides.getSelectedIndex());
         }
      });
      gbc.anchor = GridBagConstraints.NORTH;
      gbc.gridx++;
      gbc.gridheight = 1;
      gbc.weightx = .001;
      gbc.ipadx = 11;
      gbc.insets = new Insets(0, 0, 0, 0);
      panel.add(removeAutoHideButton, gbc);

      gbc.gridy++;
      gbc.fill = GridBagConstraints.BOTH;
      panel.add(Box.createVerticalStrut(10), gbc);

      return panel;
   }

   /**
    * Create a auto hide checkbox.
    */
   private JCheckBox createAutoHideCheckBox(String name) {
      JCheckBox checkBox = new JCheckBox(config.getProperty("label." + name + "-dock"));
      checkBox.setSelected(config.isAutoHideEnabled(name));
      return checkBox;
   }

   private void filterCombo() {
      List<String> overridingDockables = autoHideOverrides.getDockables();
      List<String> availDocks = new ArrayList<String>(overridingDockables.size());
      for (int i=0; i<dockables.length; i++) {
         if (!overridingDockables.contains(dockables[i]))
            availDocks.add(dockables[i]);
      }
      addAutoHideOverrideCombo.setDockables(availDocks);
   }
}

