/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.standalone;

import gruntspud.ColorUtil;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.StringUtil;
import gruntspud.ui.ColorComboBox;
import gruntspud.ui.GruntspudCheckBox;
import gruntspud.ui.JNumericTextField;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.preferences.AbstractOptionsTab;
import gruntspud.ui.preferences.FontChooser;
import gruntspud.ui.preferences.FontLabel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class OtherOptionsTab
    extends AbstractOptionsTab
    implements ActionListener {
    private GruntspudCheckBox socksProxyEnabled;
    private GruntspudCheckBox httpProxyEnabled;
    private XTextField socksProxyHost;
    private XTextField httpProxyHost;
    private JNumericTextField socksProxyPort;
    private JNumericTextField httpProxyPort;
    private JNumericTextField consoleMaxSize;
    private XTextField httpNonProxyHosts;
    private StringListComboBox dateFormat;
    private JComboBox lafChooser;
    private GruntspudCheckBox dockHTMLViewerAsTab;
    private GruntspudCheckBox dockEditorAsTab;
    private GruntspudCheckBox htmlViewerSingleInstance;
    private GruntspudCheckBox enableToolBar;
    private GruntspudCheckBox enableMenuBar;
    private GruntspudCheckBox showMemoryMonitor;
    private GruntspudCheckBox useInternalHTMLViewer;
    private ColorComboBox consoleBackground;
    private FontLabel consoleFont;
    private JButton chooseConsoleFont;

    /**
     *  Constructor for the GlobalOptionsTab object
     *
     *@param  host  Description of the Parameter
     */
    public OtherOptionsTab() {
        super("Other", UIUtil.getCachedIcon(Constants.ICON_TOOL_PREFERENCES));
    }

    /**
     * DOCUMENT ME!
     *
     * @param context DOCUMENT ME!
     */
    public void init(GruntspudContext context) {
        super.init(context);

        setTabToolTipText("Other options.");
        setTabMnemonic('r');
        setTabLargeIcon(UIUtil.getCachedIcon(
            Constants.ICON_TOOL_LARGE_PREFERENCES));
        setTabContext("General");

        Insets i1 = new Insets(3, 3, 3, 3);
        Insets i2 = new Insets(3, 24, 3, 3);

        //  Network options
        JPanel r = new JPanel(new GridBagLayout());
        r.setOpaque(false);
        r.setBorder(BorderFactory.createTitledBorder("Network options"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = i1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 2.0;
        UIUtil.jGridBagAdd(r,
                           socksProxyEnabled = new GruntspudCheckBox("Use SOCKS proxy"),
                           gbc,
                           GridBagConstraints.REMAINDER);
        socksProxyEnabled.setSelected(context.getHost().getBooleanProperty(
            Constants.OPTIONS_OTHER_SOCKS_PROXY_ENABLED,
            false));
        socksProxyEnabled.setMnemonic('s');
        socksProxyEnabled.addActionListener(this);

        gbc.weightx = 0.0;
        gbc.insets = i2;

        JLabel l1 = new JLabel("Host:");
        UIUtil.jGridBagAdd(r, l1, gbc, GridBagConstraints.RELATIVE);
        gbc.insets = i1;
        UIUtil.jGridBagAdd(r,
                           socksProxyHost = new XTextField(context.getHost().
            getProperty(Constants.OPTIONS_OTHER_SOCKS_PROXY_HOST),
            12), gbc, GridBagConstraints.REMAINDER);
        l1.setLabelFor(socksProxyHost);
        gbc.insets = i2;

        JLabel l2 = new JLabel("Port:");
        UIUtil.jGridBagAdd(r, l2, gbc, GridBagConstraints.RELATIVE);
        gbc.insets = i1;
        UIUtil.jGridBagAdd(r,
                           socksProxyPort = new JNumericTextField(new Integer(0),
            new Integer(65535),
            new Integer(context.getHost().getIntegerProperty(Constants.
            OPTIONS_OTHER_SOCKS_PROXY_PORT,
            1080))), gbc, GridBagConstraints.REMAINDER);
        l2.setLabelFor(socksProxyPort);

        gbc.weightx = 2.0;
        UIUtil.jGridBagAdd(r,
                           httpProxyEnabled = new GruntspudCheckBox("Use HTTP proxy"),
                           gbc,
                           GridBagConstraints.REMAINDER);
        httpProxyEnabled.setSelected(context.getHost().getBooleanProperty(
            Constants.OPTIONS_OTHER_HTTP_PROXY_ENABLED,
            false));
        httpProxyEnabled.setMnemonic('s');
        httpProxyEnabled.addActionListener(this);

        gbc.weightx = 0.0;
        gbc.insets = i2;

        JLabel hl1 = new JLabel("Host:");
        UIUtil.jGridBagAdd(r, hl1, gbc, GridBagConstraints.RELATIVE);
        gbc.insets = i1;
        UIUtil.jGridBagAdd(r,
                           httpProxyHost = new XTextField(context.getHost().
            getProperty(Constants.OPTIONS_OTHER_HTTP_PROXY_HOST),
            12), gbc, GridBagConstraints.REMAINDER);
        hl1.setLabelFor(httpProxyHost);
        gbc.insets = i2;

        JLabel hl2 = new JLabel("Port:");
        UIUtil.jGridBagAdd(r, hl2, gbc, GridBagConstraints.RELATIVE);
        gbc.insets = i1;
        UIUtil.jGridBagAdd(r,
                           httpProxyPort = new JNumericTextField(new Integer(0),
            new Integer(65535),
            new Integer(context.getHost().getIntegerProperty(Constants.
            OPTIONS_OTHER_HTTP_PROXY_PORT,
            8080))), gbc, GridBagConstraints.REMAINDER);
        hl2.setLabelFor(httpProxyPort);
        gbc.weighty = 1.0;
        gbc.insets = i2;

        JLabel hl3 = new JLabel("Non proxy hosts:");
        UIUtil.jGridBagAdd(r, hl3, gbc, GridBagConstraints.RELATIVE);
        gbc.insets = i1;
        UIUtil.jGridBagAdd(r,
                           httpNonProxyHosts = new XTextField(context.getHost().
            getProperty(Constants.OPTIONS_OTHER_HTTP_NON_PROXY_HOSTS,
                        "")), gbc, GridBagConstraints.REMAINDER);
        httpNonProxyHosts.setToolTipText("Use a | character to separate hosts");
        hl3.setLabelFor(httpNonProxyHosts);

        //  Look and feel
        JPanel l = new JPanel(new GridBagLayout());
        l.setOpaque(false);
        l.setBorder(BorderFactory.createTitledBorder("Display"));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.weighty = 1.0;
        gbc1.insets = i1;
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.weightx = 0.0;
        UIUtil.jGridBagAdd(l, new JLabel("Look and feel"), gbc1,
                           GridBagConstraints.RELATIVE);
        gbc1.weightx = 1.0;
        UIUtil.jGridBagAdd(l,
                           lafChooser = new JComboBox(
            JDK13GruntspudHost.getAllLookAndFeelInfo()), gbc1,
                           GridBagConstraints.REMAINDER);
        lafChooser.setRenderer(new LAFRenderer());

        String sel = context.getHost().getProperty(JDK13GruntspudHost.PROP_LAF,
            UIManager.getLookAndFeel().getClass().getName());

        for (int i = 0; i < lafChooser.getModel().getSize(); i++) {
            if ( ( (UIManager.LookAndFeelInfo) lafChooser.getModel().
                  getElementAt(i)).getClassName()
                .equals(sel)) {
                lafChooser.setSelectedIndex(i);

                break;
            }
        }

        JPanel g = new JPanel(new GridLayout(1, 3));
        g.setOpaque(false);
        g.add(enableToolBar = new GruntspudCheckBox("Enable tool bar"));
        enableToolBar.addActionListener(this);
        enableToolBar.setMnemonic('t');
        enableToolBar.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_SHOW_TOOL_BAR,
            true));
        g.add(enableMenuBar = new GruntspudCheckBox("Enable menu bar"));
        enableMenuBar.setMnemonic('m');
        enableMenuBar.addActionListener(this);
        enableMenuBar.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_SHOW_MENU_BAR,
            true));
        g.add(showMemoryMonitor = new GruntspudCheckBox("Enable memory monitor"));
        showMemoryMonitor.setMnemonic('m');
        showMemoryMonitor.addActionListener(this);
        showMemoryMonitor.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_SHOW_MEMORY_MONITOR,
            false));

        gbc1.weightx = 2.0;
        gbc1.weighty = 1.0;
        UIUtil.jGridBagAdd(l, g, gbc1, GridBagConstraints.REMAINDER);

        //  Viewers
        JPanel v = new JPanel(new GridBagLayout());
        v.setOpaque(false);
        v.setBorder(BorderFactory.createTitledBorder("Viewers"));

        GridBagConstraints gbc3 = new GridBagConstraints();
        gbc3.insets = i1;
        gbc3.anchor = GridBagConstraints.NORTHWEST;
        gbc3.fill = GridBagConstraints.HORIZONTAL;
        gbc3.weightx = 1.0;
        UIUtil.jGridBagAdd(v,
                           dockEditorAsTab = new GruntspudCheckBox("Dock editor as tab"),
                           gbc3,
                           GridBagConstraints.REMAINDER);
        dockEditorAsTab.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_DOCK_EDITOR_AS_TAB,
            false));
        UIUtil.jGridBagAdd(v,
                           useInternalHTMLViewer = new GruntspudCheckBox(
            "Use internal HTML viewer"),
                           gbc3, GridBagConstraints.REMAINDER);
        useInternalHTMLViewer.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_USE_INTERNAL_HTML_VIEWER,
            true));
        useInternalHTMLViewer.addActionListener(this);
        gbc3.insets = i2;
        UIUtil.jGridBagAdd(v,
                           dockHTMLViewerAsTab = new GruntspudCheckBox(
            "Dock HTML viewer as tab"),
                           gbc3, GridBagConstraints.REMAINDER);
        dockHTMLViewerAsTab.setSelected(context.getHost().getBooleanProperty(
            JDK13GruntspudHost.PROP_DOCK_HTML_VIEWER_AS_TAB,
            false));
        gbc3.weighty = 1.0;
        UIUtil.jGridBagAdd(v,
                           htmlViewerSingleInstance = new GruntspudCheckBox(
            "Single instance of HTML viewer"), gbc3,
                           GridBagConstraints.REMAINDER);
        htmlViewerSingleInstance.setSelected(context.getHost()
                                             .getBooleanProperty(
            JDK13GruntspudHost.PROP_HTML_VIEWER_SINGLE_INSTANCE,
            true));

        //  Colors panel
        GridBagConstraints gbc4 = new GridBagConstraints();
        JPanel c = new JPanel(new GridBagLayout());
        c.setOpaque(false);
        c.setBorder(BorderFactory.createTitledBorder("Console"));
        gbc4.insets = new Insets(3, 3, 3, 3);
        gbc4.anchor = GridBagConstraints.NORTHWEST;
        gbc4.fill = GridBagConstraints.HORIZONTAL;

        gbc4.weightx = 0.0;
        UIUtil.jGridBagAdd(c, new JLabel("Background"), gbc4, 1);
        gbc4.weightx = 2.0;
        UIUtil.jGridBagAdd(c, consoleBackground = new ColorComboBox(), gbc4,
                           GridBagConstraints.REMAINDER);
        consoleBackground.setColor(ColorUtil.getColor(
            JDK13GruntspudHost.PROP_CONSOLE_BACKGROUND,
            UIManager.getColor("TextPane.background"), context));

        gbc4.weightx = 0.0;
        UIUtil.jGridBagAdd(c, new JLabel("Maximum size"), gbc4, 1);
        gbc4.weightx = 2.0;
        UIUtil.jGridBagAdd(c,
                           consoleMaxSize = new JNumericTextField(new Integer(
            65536),
            new Integer(Integer.MAX_VALUE),
            new Integer(context.getHost().getIntegerProperty(JDK13GruntspudHost.
            PROP_CONSOLE_MAX_SIZE,
            131072))), gbc4, GridBagConstraints.REMAINDER);

        gbc4.weightx = 1.0;
        gbc4.weighty = 1.0;
        UIUtil.jGridBagAdd(c, new JLabel("Font"), gbc4, 1);

        String s = context.getHost().getProperty(JDK13GruntspudHost.
                                                 PROP_CONSOLE_FONT,
                                                 "monospaced,0,10");
        Font font = StringUtil.stringToFont(s);

        if (font == null) {
            font = UIManager.getFont("EditorPane.font");

        }
        consoleFont = new FontLabel(font);
        gbc4.weightx = 1.0;
        UIUtil.jGridBagAdd(c, consoleFont, gbc4, GridBagConstraints.RELATIVE);
        gbc4.weightx = 0.0;
        UIUtil.jGridBagAdd(c, chooseConsoleFont = new JButton("Choose"), gbc4,
                           GridBagConstraints.REMAINDER);
        chooseConsoleFont.addActionListener(this);
        chooseConsoleFont.setMnemonic('c');

        //
        JPanel m = new JPanel(new BorderLayout());
        m.setOpaque(false);
        m.add(v, BorderLayout.WEST);
        m.add(c, BorderLayout.CENTER);

        //
        setLayout(new GridBagLayout());

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;
        UIUtil.jGridBagAdd(this, r, gbc2, GridBagConstraints.REMAINDER);
        UIUtil.jGridBagAdd(this, m, gbc2, GridBagConstraints.REMAINDER);
        gbc2.weighty = 1.0;
        UIUtil.jGridBagAdd(this, l, gbc2, GridBagConstraints.REMAINDER);

        setAvailableActions();
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        if ( (evt.getSource() == enableToolBar) && !enableToolBar.isSelected() &&
            !enableMenuBar.isSelected()) {
            enableMenuBar.setSelected(true);
        }
        else if ( (evt.getSource() == enableMenuBar) &&
                 !enableMenuBar.isSelected() && !enableToolBar.isSelected()) {
            enableToolBar.setSelected(true);
        }
        else if (evt.getSource() == chooseConsoleFont) {
            Font f = FontChooser.showDialog(this, consoleFont.getChosenFont(),
                                            getContext());

            if (f != null) {
                consoleFont.setChosenFont(f);
            }
        }

        setAvailableActions();
    }

    private void setAvailableActions() {
        socksProxyHost.setEnabled(socksProxyEnabled.isSelected());
        socksProxyPort.setEnabled(socksProxyEnabled.isSelected());
        httpProxyHost.setEnabled(httpProxyEnabled.isSelected());
        httpProxyPort.setEnabled(httpProxyEnabled.isSelected());
        httpNonProxyHosts.setEnabled(httpProxyEnabled.isSelected());
        dockHTMLViewerAsTab.setEnabled(useInternalHTMLViewer.isSelected());
        htmlViewerSingleInstance.setEnabled(useInternalHTMLViewer.isSelected());

        //  Prevent both the tool bar and menu bar being hidden at the same time
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public boolean validateTab() {
        return true;
    }

    /**
     *  Description of the Method
     */
    public void tabSelected() {
    }

    /**
     *  Description of the Method
     */
    public void applyTab() {
        //
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_OTHER_SOCKS_PROXY_ENABLED,
            socksProxyEnabled.isSelected());
        getContext().getHost().setProperty(Constants.
                                           OPTIONS_OTHER_SOCKS_PROXY_HOST,
                                           socksProxyHost.getText());
        getContext().getHost().setIntegerProperty(Constants.
            OPTIONS_OTHER_SOCKS_PROXY_PORT,
            ( (Integer) socksProxyPort.
             getValue()).intValue());
        getContext().getHost().setBooleanProperty(Constants.
            OPTIONS_OTHER_HTTP_PROXY_ENABLED,
            httpProxyEnabled.isSelected());
        getContext().getHost().setProperty(Constants.
                                           OPTIONS_OTHER_HTTP_PROXY_HOST,
                                           httpProxyHost.getText());
        getContext().getHost().setIntegerProperty(Constants.
                                                  OPTIONS_OTHER_HTTP_PROXY_PORT,
                                                  ( (Integer) httpProxyPort.
            getValue()).intValue());
        getContext().getHost().setProperty(Constants.
                                           OPTIONS_OTHER_HTTP_NON_PROXY_HOSTS,
                                           httpNonProxyHosts.getText());
        getContext().getHost().setProperty(JDK13GruntspudHost.
                                           PROP_CONSOLE_BACKGROUND,
                                           StringUtil.colorToString(
            consoleBackground.getColor()));
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_SHOW_TOOL_BAR,
                                                  enableToolBar.isSelected());
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_SHOW_MENU_BAR,
                                                  enableMenuBar.isSelected());
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_SHOW_MEMORY_MONITOR,
                                                  showMemoryMonitor.isSelected());


        String n = (lafChooser.getSelectedItem() == null) ? ""
            :
            ( (UIManager.LookAndFeelInfo) lafChooser.getSelectedItem()).
            getClassName();

        if (!UIManager.getLookAndFeel().getClass().getName().equals(n)) {
            try {
                JDK13GruntspudHost.setLookAndFeel(getContext(), n);
            }
            catch (Exception e) {
                Constants.UI_LOG.error("Failed to set look and feel " + n);
            }
        }

        getContext().getHost().setProperty(JDK13GruntspudHost.PROP_LAF, n);
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_DOCK_EDITOR_AS_TAB,
                                                  dockEditorAsTab.isSelected());
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_USE_INTERNAL_HTML_VIEWER,
                                                  useInternalHTMLViewer.
                                                  isSelected());
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
                                                  PROP_DOCK_HTML_VIEWER_AS_TAB,
                                                  dockHTMLViewerAsTab.
                                                  isSelected());
        getContext().getHost().setBooleanProperty(JDK13GruntspudHost.
            PROP_HTML_VIEWER_SINGLE_INSTANCE,
            htmlViewerSingleInstance.
            isSelected());

        Font f = consoleFont.getChosenFont();
        getContext().getHost().setProperty(JDK13GruntspudHost.PROP_CONSOLE_FONT,
                                           f.getName() + "," + f.getStyle() +
                                           "," + f.getSize());
    }

    class LAFRenderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);
            setText( ( (UIManager.LookAndFeelInfo) value).getName());

            return this;
        }
    }
}
