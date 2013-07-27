/*
 * Gruntspud Copyright (C) 2002 Brett Smith. Written by: Brett Smith
 * <t_magicthize@users.sourceforge.net> This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU Library General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details. You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package gruntspud.ui.preferences;

import gruntspud.CVSRoot;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.connection.ConnectionPlugin;
import gruntspud.connection.ConnectionProfile;
import gruntspud.ui.AbstractTab;
import gruntspud.ui.CharSetComboBox;
import gruntspud.ui.FileNameTextField;
import gruntspud.ui.JNumericTextField;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import plugspud.Plugin;
import plugspud.PluginManager;

/**
 * Mini text editor
 * 
 * @author Brett Smiht @created 26 May 2002
 */
public class ConnectionProfileEditor extends AbstractTab implements
    ActionListener, DocumentListener {
  public final static String[] ACCESS_TYPES = {"Default (Global options)",
      "Read / Write", "Read only"};

  public final static String[] COMPRESSION_TYPES = {"Default (Global options)",
      "Enabled", "Disabled"};

  public final static String[] LINE_ENDING_TYPES = {"Default", "Unix",
      "Windows", "Ignore"};

  //  Private instance variables
  private XTextField name;

  private GruntspudContext context;

  private JComboBox connectionType;

  private XTextField user;

  private XTextField hostName;

  private FileNameTextField repository;

  private XTextField webCVSURL;

  private JNumericTextField port;

  private JLabel cvsRoot;

  private ConnectionProfile profile;

  private CVSRoot currentRoot;

  private boolean isNew;

  private Vector types;

  private JPanel additionalOptionsContainer;

  private CardLayout additionalOptionsLayout;

  private JComboBox access;

  private JComboBox compression;

  private JComboBox lineEndings;

  private CharSetComboBox encoding;

  /**
   * Constructor
   * 
   * @param host Description of the Parameter
   */
  public ConnectionProfileEditor(GruntspudContext context,
      ConnectionProfile profile) {
    this(context, profile, true);
  }

  /**
   * Creates a new ConnectionProfileEditor object.
   * 
   * @param context DOCUMENT ME!
   * @param profile DOCUMENT ME!
   * @param isNew DOCUMENT ME!
   */
  public ConnectionProfileEditor(GruntspudContext context,
      ConnectionProfile profile, boolean isNew) {
    super("General", UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_CONNECT));
    this.context = context;
    this.profile = profile;
    setTabToolTipText("General connection options.");
    setLayout(new GridBagLayout());
    setTabMnemonic('c');
    types = new Vector();
    Plugin[] plugins = context.getPluginManager().getPluginsOfClass(
        ConnectionPlugin.class);
    for (int j = 0; j < plugins.length; j++) {
      types.addElement(plugins[j]);
    }
    Collections.sort(types, new ConnectionPluginComparator());
    additionalOptionsLayout = new CardLayout();
    additionalOptionsContainer = new JPanel(additionalOptionsLayout);
    for (Iterator i = types.iterator(); i.hasNext();) {
      ConnectionPlugin p = (ConnectionPlugin) i.next();
      AbstractAdditionalOptionsPane o = p.getOptionsComponent();
      SwingUtilities.updateComponentTreeUI(o);
      additionalOptionsContainer.add(p.getConnectionType(),
          (o == null) ? new NoAdditionalConnectionOptionsPane() : o);
    }
    additionalOptionsContainer.setBorder(BorderFactory
        .createTitledBorder("Additional Options"));
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Name"), gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, name = new XTextField(15) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Type"), gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, connectionType = new JComboBox(types) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    if (connectionType.getModel().getSize() > 0) {
      connectionType.setSelectedIndex(0);
    }
    connectionType.setRenderer(new ConnectionPluginComboBoxRenderer());
    connectionType.addActionListener(this);
    gbc.weightx = 0.0;
    UIUtil
        .jGridBagAdd(p, new JLabel("User "), gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, user = new XTextField(15) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    user.getDocument().addDocumentListener(this);
    gbc.weightx = 0.0;
    UIUtil
        .jGridBagAdd(p, new JLabel("Host "), gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, hostName = new XTextField(15) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    hostName.getDocument().addDocumentListener(this);
    gbc.weightx = 0.0;
    UIUtil
        .jGridBagAdd(p, new JLabel("Port "), gbc, GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, port = new JNumericTextField(new Integer(-1),
        new Integer(65535)) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    port.getDocument().addDocumentListener(this);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Repository "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, repository = new FileNameTextField(null, "", 15,
        false) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    repository.getDocument().addDocumentListener(this);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("ViewCVS base URL "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, webCVSURL = new XTextField(15) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Access "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, access = new JComboBox(ACCESS_TYPES) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Compression "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, compression = new JComboBox(COMPRESSION_TYPES) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Line endings "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, lineEndings = new JComboBox(LINE_ENDING_TYPES) {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    gbc.weighty = 1.0;
    gbc.weightx = 0.0;
    UIUtil.jGridBagAdd(p, new JLabel("Encoding "), gbc,
        GridBagConstraints.RELATIVE);
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(p, encoding = new CharSetComboBox() {
      public Dimension getMinimumSize() {
        return new Dimension(120, super.getMinimumSize().height);
      }
    }, gbc, GridBagConstraints.REMAINDER);
    encoding.setEditable(true);
    cvsRoot = new JLabel(" ");
    cvsRoot.setHorizontalAlignment(JLabel.CENTER);
    cvsRoot.setForeground(Color.red);
    cvsRoot.setFont(UIManager.getFont("Label.font").deriveFont(10f));
    cvsRoot.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setLayout(new BorderLayout());
    add(p, BorderLayout.CENTER);
    add(additionalOptionsContainer, BorderLayout.EAST);
    add(cvsRoot, BorderLayout.SOUTH);
    setConnectionProfile(profile, isNew);
  }

  /**
   * DOCUMENT ME!
   */
  public void tabSelected() {
  }

  /**
   * DOCUMENT ME!
   */
  public void applyTab() {
    if ((currentRoot != null) && (profile != null)) {
      if (profile.getCVSRoot() == null) {
        profile.setCVSRoot(currentRoot);
      }
      profile.setName(name.getText());
      profile.getCVSRoot().setConnectionType(currentRoot.getConnectionType());
      profile.getCVSRoot().setUser(currentRoot.getUser());
      profile.getCVSRoot().setHost(currentRoot.getHost());
      profile.getCVSRoot().setPort(currentRoot.getPort());
      profile.getCVSRoot().setRepository(currentRoot.getRepository());
      profile.setWebCVSURL(webCVSURL.getText());
      profile.setAccess((access.getSelectedIndex() == -1) ? 0 : access
          .getSelectedIndex());
      profile.setCompression((compression.getSelectedIndex() == -1) ? 0
          : compression.getSelectedIndex());
      profile.setLineEndings((lineEndings.getSelectedIndex() == -1) ? 0
          : lineEndings.getSelectedIndex());
      profile.setEncoding(encoding.getSelectedEncoding());
      int i = connectionType.getSelectedIndex();
      ((ConnectionPlugin) types.elementAt(i)).getOptionsComponent()
          .applyOptions();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean validateTab() {
    ConnectionPlugin t = (ConnectionPlugin) connectionType.getSelectedItem();
    try {
      if (name.getText().equals("")) { throw new IOException(
          "You must give the profile a unique name"); }
      if (t == null) { throw new IOException(
          "You must select a connection type"); }
      if (repository.getText().equals("")) { throw new IOException(
          "You must specify a repository path"); }
      t.validateCVSRoot(currentRoot);
    } catch (Exception e) {
      GruntspudUtil.showErrorMessage(this, "Connection Details", e);
      return false;
    }
    if (!t.getOptionsComponent().validateOptions()) { return false; }
    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param profile DOCUMENT ME!
   */
  public void setConnectionProfile(ConnectionProfile profile) {
    setConnectionProfile(profile, true);
  }

  private void showSelectedAdditional() {
    if (connectionType.getSelectedItem() != null) {
      additionalOptionsLayout.show(additionalOptionsContainer,
          ((ConnectionPlugin) connectionType.getSelectedItem())
              .getConnectionType());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param profile DOCUMENT ME!
   * @param isNew DOCUMENT ME!
   */
  public void setConnectionProfile(ConnectionProfile profile, boolean isNew) {
    String t = ((profile == null) || (profile.getCVSRoot() == null)) ? null
        : profile.getCVSRoot().getConnectionType();
    boolean found = false;
    for (int i = 0; (i < types.size()) && !found; i++) {
      ConnectionPlugin w = (ConnectionPlugin) types.elementAt(i);
      if (w.getConnectionType().equals(t)) {
        connectionType.setSelectedIndex(i);
        found = true;
      }
    }
    if (!found) {
      for (int i = 0; i < connectionType.getModel().getSize(); i++) {
        if (((ConnectionPlugin) connectionType.getModel().getElementAt(i))
            .getConnectionType().equals("local")) {
          connectionType.setSelectedIndex(i);
          break;
        }
      }
    }
    encoding.setSelectedEncoding(profile.getEncoding());
    name.setText((profile == null) ? "" : profile.getName());
    user
        .setText(((profile == null) || (profile.getCVSRoot() == null) || (profile
            .getCVSRoot().getUser() == null)) ? "" : profile.getCVSRoot()
            .getUser());
    hostName
        .setText(((profile == null) || (profile.getCVSRoot() == null) || (profile
            .getCVSRoot().getHost() == null)) ? "" : profile.getCVSRoot()
            .getHost());
    port.setValue(new Integer(
        ((profile == null) || (profile.getCVSRoot() == null)) ? (-1) : profile
            .getCVSRoot().getPort()));
    repository
        .setText(((profile == null) || (profile.getCVSRoot() == null) || (profile
            .getCVSRoot().getRepository() == null)) ? "" : profile.getCVSRoot()
            .getRepository());
    webCVSURL
        .setText(((profile == null) || (profile.getWebCVSURL() == null)) ? ""
            : profile.getWebCVSURL());
    access.setSelectedIndex((profile == null) ? (-1) : profile.getAccess());
    compression.setSelectedIndex((profile == null) ? (-1) : profile
        .getCompression());
    lineEndings.setSelectedIndex((profile == null) ? (-1) : profile
        .getLineEndings());
    this.isNew = isNew;
    for (int i = 0; i < types.size(); i++) {
      ((ConnectionPlugin) types.elementAt(i)).getOptionsComponent().setProfile(
          profile);
    }
    showSelectedAdditional();
    setAvailableActions();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt DOCUMENT ME!
   */
  public void actionPerformed(ActionEvent evt) {
    setAvailableActions();
    showSelectedAdditional();
  }

  private void setAvailableActions() {
    name.setEnabled(isNew);
    ConnectionPlugin w = ((ConnectionPlugin) connectionType.getSelectedItem());
    user.setEnabled((w != null) && w.isUserRequired());
    hostName.setEnabled((w != null) && w.isHostnameRequired());
    port.setEnabled((w != null) && w.isPortRequired());
    repository.setAutoComplete(w != null
        && w.getConnectionType().equals("local"));
    currentRoot = new CVSRoot();
    currentRoot.setConnectionType((w == null) ? null : w.getConnectionType());
    currentRoot.setUser((user.getText().length() == 0) ? null : user.getText());
    currentRoot.setHost((hostName.getText().length() == 0) ? null : hostName
        .getText());
    currentRoot.setRepository((repository.getText().length() == 0) ? null
        : repository.getText());
    int val = ((Integer) port.getValue()).intValue();
    currentRoot.setPort((val == 0) ? (-1) : val);
    String r = currentRoot.toString();
    cvsRoot.setText(r.length() == 0 ? " " : r);
    cvsRoot.setToolTipText(r);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e DOCUMENT ME!
   */
  public void insertUpdate(DocumentEvent e) {
    setAvailableActions();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e DOCUMENT ME!
   */
  public void removeUpdate(DocumentEvent e) {
    setAvailableActions();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e DOCUMENT ME!
   */
  public void changedUpdate(DocumentEvent e) {
    setAvailableActions();
  }

  class ConnectionPluginComboBoxRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value,
        int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected,
          cellHasFocus);
      ConnectionPlugin p = (ConnectionPlugin) value;
      setText(((p == null) || (p.getConnectionType() == null)) ? "<No connection type!>"
          : p.getConnectionType());
      setIcon((p == null) ? null : p.getIcon());
      if (p != null) {
        setToolTipText(context.getPluginManager().getPluginProperties(p)
            .getProperty(PluginManager.PLUGIN_SHORT_DESCRIPTION));
      } else {
        setToolTipText("");
      }
      return this;
    }
  }

  class ConnectionPluginComparator implements Comparator {
    public boolean equals(Object other) {
      return (this.equals(other));
    }

    public int compare(Object o1, Object o2) {
      return ((ConnectionPlugin) o1).getConnectionType().compareTo(
          ((ConnectionPlugin) o2).getConnectionType());
    }
  }
}