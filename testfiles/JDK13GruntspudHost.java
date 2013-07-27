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
package gruntspud.standalone;

import gruntspud.CVSFileNode;
import gruntspud.CVSSubstType;
import gruntspud.CVSUtil;
import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudHost;
import gruntspud.GruntspudUtil;
import gruntspud.VersionInfo;
import gruntspud.actions.GruntspudAction;
import gruntspud.style.TextStyle;
import gruntspud.ui.BrowserLauncher;
import gruntspud.ui.FolderBar;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.OptionsTab;
import gruntspud.ui.ScrollingPanel;
import gruntspud.ui.Tab;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;
import gruntspud.ui.icons.IconResource;
import gruntspud.ui.view.View;
import gruntspud.ui.view.ViewEvent;
import gruntspud.ui.view.ViewListener;
import gruntspud.ui.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.netbeans.lib.cvsclient.file.FileStatus;

/**
 * Description of the Class
 * 
 * @author magicthize
 */
public class JDK13GruntspudHost extends JFrame implements GruntspudHost,
    ViewListener {
  ///
  private static UIManager.LookAndFeelInfo[] allLookAndFeelInfo;

  public final static Insets FILE_RENDERER_INSETS = new Insets(1, 0, 0, 0);

  //  Icons specfic to standalone
  public final static String ICON_TOOL_LARGE_TIPS = "icon.tool.largeTips";

  public final static String ICON_TOOL_LARGE_INDEX = "icon.tool.largeIndex";

  public final static String ICON_TOOL_LARGE_CONTENTS = "icon.tool.largeContents";

  public final static String ICON_TOOL_INDEX = "icon.tool.index";

  public final static String ICON_TOOL_CONTENTS = "icon.tool.contents";

  public final static String ICON_TOOL_SMALL_INDEX = "icon.tool.smallIndex";

  public final static String ICON_TOOL_SMALL_CONTENTS = "icon.tool.smallContents";

  //  Properties specific to standalone
  public final static String PROP_LAF = "gruntspudSA.laf";

  public final static String PROP_GEOMETRY = "gruntspudSA.geometry";

  public final static String PROP_EDITOR_FONT = "gruntspudSA.editor.font";

  public final static String PROP_EDITOR_WORD_WRAP = "gruntspudSA.editor.wordWrap";

  public final static String PROP_HELP_DIVIDER_LOCATION = "gruntspudSA.helpDividerLocation";

  public final static String PROP_HELP_GEOMETRY = "gruntspudSA.helpGeometry";

  public final static String PROP_USE_INTERNAL_HTML_VIEWER = "gruntspudSA.useInternalHTMLViewer";

  public final static String PROP_HTML_VIEWER_GEOMETRY = "gruntspudSA.htmlViewerGeometry";

  public final static String PROP_HTML_VIEWER_SINGLE_INSTANCE = "gruntspudSA.htmlViewerSingleInstance";

  public final static String PROP_DOCK_EDITOR_AS_TAB = "gruntspudSA.dockEditorAsTab";

  public final static String PROP_DOCK_HTML_VIEWER_AS_TAB = "gruntspudSA.dockHTMLViewerAsTab";

  public final static String PROP_CONSOLE_BACKGROUND = "gruntspudSA.console.background";

  public final static String PROP_CONSOLE_MAX_SIZE = "gruntspudSA.console.maxSize";

  public final static String PROP_CONSOLE_FONT = "gruntspudSA.console.font";

  public final static String PROP_CONSOLE_DIVIDER_LOCATION = "gruntspudSA.console.dividerLocation";

  public final static String PROP_CONSOLE_LAST_SAVE_LOCATION = "gruntspudSA.console.lastSaveLocation";

  public final static String PROP_SHOW_TIPS_ON_STARTUP = "gruntspudSA.showTipsOnStartup";

  public final static String PROP_NEXT_TIP_INDEX = "gruntspudSA.nextTipIndex";

  public final static String PROP_SHOW_MENU_BAR = "gruntspudSA.showMenuBar";

  public final static String PROP_SHOW_TOOL_BAR = "gruntspudSA.showToolBar";

  public final static String PROP_SHOW_MEMORY_MONITOR = "gruntspudSA.showMemoryMonitor";

  //  Diff viewer
  public final static String PROP_DIFF_VIEWER_HORIZONTAL_SPLIT = "gruntspudSA.diffViewer.horizontalSplit";

  public final static String PROP_DIFF_VIEWER_VERTICAL_SPLIT = "gruntspudSA.diffViewer.verticalSplit";

  public final static String PROP_DIFF_VIEWER_TABLE_GEOMETRY = "gruntspudSA.diffViewer.tableGeometry";

  //  Resource names specific to standalone
  public final static String TIPS_RESOURCE_NAME = "resources/tips.txt";

  //  Other specific to standalone
  public final static String CROSS_PLATFORM_LAF = "CROSS_PLATFORM";

  //	Styles specific to standalone
  public final static String OPTIONS_STYLE_DIFF_NON_EXISTANT = "diff.nonExistant";

  public final static String OPTIONS_STYLE_DIFF_IDENTICAL = "diff.identical";

  public final static String OPTIONS_STYLE_DIFF_INSERTION = "diff.insertion";

  public final static String OPTIONS_STYLE_DIFF_DELETION = "diff.deletion";

  public final static String OPTIONS_STYLE_DIFF_CHANGE = "diff.change";
  static {
    //  This is necessary for the plugins to work in the web start version
    Policy.setPolicy(new JDK13GruntspudHostPolicy());
    //
    UIUtil.cacheIcon(ICON_TOOL_LARGE_TIPS, "images/tool_large_tips.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_INDEX, "images/tool_large_index.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_CONTENTS, "images/tool_large_contents.png");
    UIUtil.cacheIcon(ICON_TOOL_INDEX, "images/tool_index.png");
    UIUtil.cacheIcon(ICON_TOOL_CONTENTS, "images/tool_contents.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_INDEX, "images/tool_small_index.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_CONTENTS, "images/tool_small_contents.png");
    
    // Add a load of look and feels
    List l = new ArrayList();
    try {
      l.addAll(Arrays.asList(UIManager.getInstalledLookAndFeels()));
    } catch (Throwable t) {
    }
    l.add(new UIManager.LookAndFeelInfo("Default",
        UIManager.getLookAndFeel().getClass().getName()));
    l.add(new UIManager.LookAndFeelInfo(
        "Cross Platform", UIManager.getCrossPlatformLookAndFeelClassName()));
    l.add(new UIManager.LookAndFeelInfo("System",
        UIManager.getSystemLookAndFeelClassName()));
    try {
        l.add(new UIManager.LookAndFeelInfo("NimROD", Class.forName("com.nilo.plaf.nimrod.NimRODLookAndFeel").getName()));
    }
    catch(Exception e) {        
    }
    allLookAndFeelInfo = (UIManager.LookAndFeelInfo[])l.toArray(new UIManager.LookAndFeelInfo[l.size()]);
    
    
  }

  private Gruntspud gruntspud;

  private GruntspudContext context;

  private SimpleConsole console;

  private Properties properties;

  private File tmpDir;

  private File prefFile;

  private boolean commandRunning;

  private JDialog helpDialog;

  private HTMLViewerFrame htmlViewerFrame;

  private Vector openFiles;

  private Icon defaultFolderOpenIcon, defaultFolderClosedIcon, defaultLeafIcon,
      defaultLeafOpenIcon;

  /**
   * Constructor for the JDK13GruntspudHost object
   * 
   * @param properties DOCUMENT ME!
   * @param prefFile DOCUMENT ME!
   * 
   * @exception IOException Description of the Exception
   */
  private JDK13GruntspudHost(Properties properties, File prefFile) throws Exception {
    super("Gruntspud " + VersionInfo.getVersion());
    getDefaultIcons();
    this.properties = properties;
    this.prefFile = prefFile;
    openFiles = new Vector();
    setIconImage(((ImageIcon) UIUtil.loadIconForResource("images/fish.png"))
        .getImage());
    //
    gruntspud = new Gruntspud(this);
    context = gruntspud.getContext();
    console = new SimpleConsole(context);
    gruntspud.start();
    setJMenuBar(context.getViewManager().getMenuBar());
    getContentPane().setLayout(new GridLayout(1, 1));
    getContentPane().add(gruntspud);
    if (getProperty(PROP_GEOMETRY) != null) {
      loadGeometry(this, PROP_GEOMETRY);
    } else {
      setSize(new Dimension(780, 580));
      UIUtil.positionComponent(SwingConstants.CENTER, this);
    }
    int n = Math.abs(hashCode());
    while (true) {
      tmpDir = new File(new File(System.getProperty("java.io.tmpdir")),
          "gruntspud_" + n + "_tmp");
      if (!tmpDir.exists()) {
        if (!tmpDir.mkdir()) {
          throw new IOException("Could not create temporary directory");
        } else {
          break;
        }
      } else {
        n++;
      }
    }
    tmpDir.deleteOnExit();
    context.getViewManager().addViewListener(this);
    context.registerOptionsTab(gruntspud.standalone.OtherOptionsTab.class);
    context.getTextStyleModel().addStyle(
        new TextStyle(OPTIONS_STYLE_DIFF_IDENTICAL, "Diff identical",
            Color.black, Color.white, false, false));
    context.getTextStyleModel().addStyle(
        new TextStyle(OPTIONS_STYLE_DIFF_NON_EXISTANT, "Diff missing",
            Color.black, Color.lightGray, false, false));
    context.getTextStyleModel().addStyle(
        new TextStyle(OPTIONS_STYLE_DIFF_INSERTION, "Diff insertion",
            Color.black, new Color(120, 220, 130), false, false));
    context.getTextStyleModel().addStyle(
        new TextStyle(OPTIONS_STYLE_DIFF_DELETION, "Diff deletion",
            Color.black, Color.pink, false, false));
    context.getTextStyleModel().addStyle(
        new TextStyle(OPTIONS_STYLE_DIFF_CHANGE, "Diff change", Color.black,
            new Color(70, 170, 230), false, false));
    try {
      Class macOS = Class.forName("gruntspud.standalone.os.MacOSX");
      Class argC[] = {ViewManager.class};
      Object arg[] = {context.getViewManager()};
      Method init = macOS.getMethod("init", argC);
      Object obj = macOS.newInstance();
      init.invoke(obj, arg);
    } catch (Throwable t) {
      // not on macos
    }
  }

  /**
   *  
   */
  public TextStyle getNodeStyle(CVSFileNode node) {
    FileStatus s = node.getOverallStatus();
    TextStyle style = null;
    if (s != null) {
      style = context.getTextStyleModel().getStyle("status." + s.toString());
    }
    if (style == null) {
      if (!node.isLeaf()) {
        style = new TextStyle();
        style.setBold(true);
      } else
        style = context.getTextStyleModel().getStyle("status.Erased");
    }
    return style;
  }

  private void getDefaultIcons() {
    defaultFolderClosedIcon = UIManager.getIcon("Tree.closedIcon");
    defaultFolderOpenIcon = UIManager.getIcon("Tree.openIcon");
    defaultLeafIcon = UIManager.getIcon("Tree.leafIcon");
    defaultLeafOpenIcon = defaultLeafIcon;
  }

  public Icon getIcon(String name) {
    if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_OPEN)) {
      return defaultFolderOpenIcon;
    } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_CLOSED)) {
      return defaultFolderClosedIcon;
    } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_LEAF)) {
      return defaultLeafIcon;
    } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_LEAF_OPEN)) {
      return defaultLeafOpenIcon;
    } else {
      return UIUtil.getCachedIcon(name);
    }
  }

  public Insets getFileRendererInsets() {
    return FILE_RENDERER_INSETS;
  }

  public boolean isNodeOpenedInEditor(CVSFileNode node) {
    for (Iterator i = openFiles.iterator(); i.hasNext();) {
      if (((File) i.next()).equals(node.getFile())) { return true; }
    }
    return false;
  }

  private GruntspudContext getContext() {
    return context;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param viewEvent DOCUMENT ME!
   */
  public void viewEvent(ViewEvent viewEvent) {
    if (viewEvent.getType() == ViewEvent.VIEW_REMOVED) {
      if (viewEvent.getSource() instanceof DockedEditorView) {
        File f = ((DockedEditorView) viewEvent.getSource()).getFile();
        for (int i = 0; i < openFiles.size(); i++) {
          File z = (File) openFiles.elementAt(i);
          if (f.equals(z)) {
            CVSFileNode n = context.getViewManager().findNodeForPath(
                context.getViewManager().getRootNode(), z, false);
            if (n != null) {
              n.reset();
            }
            openFiles.removeElementAt(i);
            break;
          }
        }
        gruntspud.repaint();
      }
    }
  }

  private static Properties loadProperties(File file) throws IOException {
    Properties p = new Properties();
    File old = new File(file.getAbsolutePath() + ".old");
    if (old.exists()) {
      int opt = JOptionPane.showConfirmDialog(null,
          "An old preferences file exists. This was probably caused\n"
              + "an error whilst shutting down a previous instance and your\n"
              + "preferences may have been corrupted. Do you wish to revert\n"
              + "to these settings?", "Old preferences exist",
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
      if (opt == JOptionPane.YES_OPTION) {
        if (!file.delete())
            throw new IOException("Could not delete current preferences file "
                + file.getAbsolutePath());
        if (!old.renameTo(file))
            throw new IOException("Could not rename " + old.getAbsolutePath()
                + " to " + file.getAbsolutePath());
      }
      if (opt == JOptionPane.CANCEL_OPTION)
          throw new IOException("User cancelled loading of preferences.");
      if (!old.delete()) {
        Constants.SYSTEM_LOG.warn("Could not remove old preferences file "
            + old.getAbsolutePath());
      }
    }
    if (file.exists()) {
      InputStream in = null;
      try {
        in = new FileInputStream(file);
        p.load(in);
      } finally {
        if (in != null) {
          in.close();
        }
      }
    }
    return p;
  }

  /**
   * Gets the property attribute of the JEditPlugin object
   * 
   * @return The property value
   */
  public JComponent getAccessoryComponent() {
    return console;
  }

  /**
   * If this host is capable of displaying HTML, this method should return a
   * name for the HTML viewer.
   * 
   * @return the name of the diff viewer
   */
  public String getHTMLViewerName() {
    return "Gruntspud HTML viewer";
  }

  /**
   * View some HTML given a URL.
   * 
   * @param u Description of the Parameter
   */
  public void viewHTML(URL u) {
    if (getBooleanProperty(PROP_USE_INTERNAL_HTML_VIEWER, true)) {
      if (getBooleanProperty(PROP_DOCK_HTML_VIEWER_AS_TAB, false)) {
        HTMLViewerView v = null;
        if (!getBooleanProperty(PROP_HTML_VIEWER_SINGLE_INSTANCE, true)) {
          Constants.UI_LOG
              .debug("Opening new instance of docked Gruntspud HTML viewer for "
                  + u);
          v = new HTMLViewerView(context, u);
          context.getViewManager().addView(v);
        } else {
          Constants.UI_LOG
              .debug("Using single instance of docked Gruntspud HTML viewer for "
                  + u);
          int c = context.getViewManager().getViewCount();
          for (int i = 0; (i < c) && (v == null); i++) {
            View view = context.getViewManager().getViewAt(i);
            if (view.getClass().equals(HTMLViewerView.class)) {
              v = (HTMLViewerView) view;
            }
          }
          if (v == null) {
            v = new HTMLViewerView(context, u);
            context.getViewManager().addView(v);
          } else {
            v.setURL(u);
          }
        }
        context.getViewManager().setSelectedView(v);
      } else {
        if (!getBooleanProperty(PROP_HTML_VIEWER_SINGLE_INSTANCE, false)) {
          Constants.UI_LOG
              .debug("Opening new instance of docked Gruntspud HTML viewer for "
                  + u);
          new HTMLViewerFrame(context, u);
        } else {
          Constants.UI_LOG
              .debug("Using single instance of docked Gruntspud HTML viewer for "
                  + u);
          if (htmlViewerFrame == null) {
            htmlViewerFrame = new HTMLViewerFrame(context, u);
          } else {
            htmlViewerFrame.setURL(u);
            htmlViewerFrame.setVisible(true);
          }
        }
      }
    } else {
      try {
        Constants.SYSTEM_LOG.info("Opening URL " + u.toExternalForm());
        BrowserLauncher.openURL(u.toExternalForm());
      } catch (IOException ioe) {
        GruntspudUtil.showErrorMessage(this, "Error", ioe);
      }
    }
  }

  /**
   * Gets the property attribute of the JEditPlugin object
   * 
   * @return The property value
   */
  public JComponent getMainComponent() {
    return gruntspud;
  }

  /**
   * DOCUMENT ME!
   */
  public void reset() {
    if (getBooleanProperty(Constants.OPTIONS_OTHER_SOCKS_PROXY_ENABLED, false)) {
      System.setProperty("socksProxyHost", getProperty(
          Constants.OPTIONS_OTHER_SOCKS_PROXY_HOST, "localhost"));
      System.setProperty("socksProxyPort", String.valueOf(getIntegerProperty(
          Constants.OPTIONS_OTHER_SOCKS_PROXY_PORT, 1080)));
    } else {
      System.getProperties().remove("socksProxyHost");
      System.getProperties().remove("socksProxyPort");
    }
    if (getBooleanProperty(Constants.OPTIONS_OTHER_HTTP_PROXY_ENABLED, false)) {
      System.setProperty("http.proxySet", "true");
      System.setProperty("http.proxyHost", getProperty(
          Constants.OPTIONS_OTHER_HTTP_PROXY_HOST, "localhost"));
      System.setProperty("host.proxyPort", String.valueOf(getIntegerProperty(
          Constants.OPTIONS_OTHER_HTTP_PROXY_PORT, 8080)));
      System.setProperty("http.nonProxyHosts", getProperty(
          Constants.OPTIONS_OTHER_HTTP_NON_PROXY_HOSTS, ""));
    } else {
      System.getProperties().remove("http.proxySet");
      System.getProperties().remove("http.proxyHost");
      System.getProperties().remove("http.proxyPort");
      System.getProperties().remove("http.nonProxyHosts");
    }
    getJMenuBar().setVisible(getBooleanProperty(PROP_SHOW_MENU_BAR, true));
    gruntspud.init();
    gruntspud.setToolBarVisible(getBooleanProperty(PROP_SHOW_TOOL_BAR, true));
    console.init();
    writeToConsole(context.getTextStyleModel().getStyle(
        Constants.OPTIONS_STYLE_GRUNTSPUD), Gruntspud.APPLICATION_NAME + " - "
        + Gruntspud.APPLICATION_VERSION);
    writeToConsole(context.getTextStyleModel().getStyle(
        Constants.OPTIONS_STYLE_GRUNTSPUD),
        "See Help -> About for more information.");
  }

  /**
   *  
   */
  public boolean isExitActionAvailable() {
    return true;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public boolean canExit() {
    if (commandRunning) {
      if (JOptionPane.showConfirmDialog(this,
          "A command is running, are you sure?", "Exit Gruntspud",
          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIUtil
              .getCachedIcon(Constants.ICON_TOOL_LARGE_EXIT)) == JOptionPane.NO_OPTION) { return false; }
    }
    return true;
  }

  /**
   * DOCUMENT ME!
   */
  public void exit() {
    cleanUp();
    System.exit(0);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param resource DOCUMENT ME!
   */
  public void showHelp(String resource) {
      try {
	      URL res = getClass().getClassLoader().getResource("help/html/index.html");
	      if(res != null && resource != null && !resource.equals("")) {
	          res = new URL(res, resource);          
	      }
	      else {
		      if(res == null) {
		          throw new IOException("Could not location help resource.");
		      }
	      }
	      viewHTML(res);
      }
      catch(Exception ex) {
          Constants.UI_LOG.error(ex); // TODO error dialog
      }
  }

  /**
   * Return the name of the host
   * 
   * @return DOCUMENT ME!
   * 
   * @exception IOException Description of the Exception
   */
  public String getName() {
    return "Gruntspud";
  }

  /**
   * Inform of when a command starts running and stops running
   * 
   * @param commandRunning command is running
   */
  public void setCommandRunning(boolean commandRunning) {
    this.commandRunning = commandRunning;
    console.setCommandRunning(commandRunning);
  }

  /**
   * Gets the property attribute of the JEditPlugin object
   * 
   * @param key Description of the Parameter
   */
  public void unsetProperty(String key) {
    properties.remove(key);
  }

  /**
   * Description of the Method
   * 
   * @param f1 Description of the Parameter
   * @param title1 DOCUMENT ME!
   * @param f2 Description of the Parameter
   * @param title2 DOCUMENT ME!
   */
  public void diff(File f1, String title1, File f2, String title2, String encoding) {
    writeToConsole(context.getTextStyleModel().getStyle(
        Constants.OPTIONS_STYLE_GRUNTSPUD), "Diffing " + f1.getAbsolutePath()
        + " with " + f2.getAbsolutePath() + " using encoding " + encoding);
    DiffViewerView v = new DiffViewerView(context, f1, title1, f2, title2, encoding);
    context.getViewManager().addView(v);
    context.getViewManager().setSelectedView(v);
  }

  /**
   * Gets the tmpDir attribute of the JDK14GruntspudHost object
   * 
   * @return The tmpDir value
   */
  public File getTmpDir() {
    return tmpDir;
  }

  /**
   * Gets the diffViewerName attribute of the JDK14GruntspudHost object
   * 
   * @return The diffViewerName value
   */
  public String getDiffViewerName() {
    return "Gruntspud Diff Viewer";
  }

  /**
   * Gets the localizedString attribute of the JDK14GruntspudHost object
   * 
   * @param name Description of the Parameter
   * @param defaultValue Description of the Parameter
   * 
   * @return The localizedString value
   */
  public String getLocalizedString(String name, String defaultValue) {
    return defaultValue;
  }

  /**
   * Description of the Method
   * 
   * @param c Description of the Parameter
   * @param message Description of the Parameter
   */
  public void writeToConsole(TextStyle s, String message) {
    console.writeMessage(s, message);
  }

  /**
   * Description of the Method
   * 
   * @param c Description of the Parameter
   * @param message Description of the Parameter
   * @param icon DOCUMENT ME!
   */
  public void writeToStatusLine(Color c, String message, Icon icon) {
    console.writeToStatusLine(c, message, icon);
  }

  /**
   * Description of the Method
   * 
   * @param c Description of the Parameter
   * @param message Description of the Parameter
   * @param icon DOCUMENT ME!
   */
  public void writeToInfoLine(Color c, String message, Icon icon) {
    console.writeToInfoLine(c, message, icon);
  }

  /**
   * Description of the Method
   * 
   * @param t Description of the Parameter
   */
  public void showException(Throwable t) {
    showException(t, null);
  }

  /**
   * Description of the Method
   * 
   * @param t Description of the Parameter
   * @param message Description of the Parameter
   */
  public void showException(Throwable t, String message) {
    if (message != null) {
      System.err.println(message);
    }
    t.printStackTrace();
  }

  /**
   * Gets the property attribute of the JEditPlugin object
   * 
   * @return The property value
   */
  public Properties getProperties() {
    return properties;
  }

  /**
   * Gets the property attribute of the JDK14GruntspudHost object
   * 
   * @param key Description of the Parameter
   * 
   * @return The property value
   */
  public String getProperty(String key) {
    return getProperty(key, null);
  }

  /**
   * Gets the property attribute of the JDK14GruntspudHost object
   * 
   * @param key Description of the Parameter
   * @param defaultValue Description of the Parameter
   * 
   * @return The property value
   */
  public String getProperty(String key, String defaultValue) {
    String val = properties.getProperty(key, defaultValue);
    return val;
  }

  /**
   * Gets the booleanProperty attribute of the JDK14GruntspudHost object
   * 
   * @param key Description of the Parameter
   * 
   * @return The booleanProperty value
   */
  public boolean getBooleanProperty(String key) {
    return getBooleanProperty(key, false);
  }

  /**
   * Gets the booleanProperty attribute of the JDK14GruntspudHost object
   * 
   * @param key Description of the Parameter
   * @param defaultValue Description of the Parameter
   * 
   * @return The booleanProperty value
   */
  public boolean getBooleanProperty(String key, boolean defaultValue) {
    String p = getProperty(key, String.valueOf(defaultValue));
    return (p != null) && p.equalsIgnoreCase("true");
  }

  /**
   * Gets the booleanProperty attribute of the GruntspudHost object
   * 
   * @param key Description of the Parameter
   * 
   * @return The booleanProperty value
   */
  public int getIntegerProperty(String key) {
    return getIntegerProperty(key, 0);
  }

  /**
   * Gets the booleanProperty attribute of the GruntspudHost object
   * 
   * @param key Description of the Parameter
   * @param defaultValue DOCUMENT ME!
   * 
   * @return The booleanProperty value
   */
  public int getIntegerProperty(String key, int defaultValue) {
    try {
      return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
    } catch (NumberFormatException nfe) {
      return defaultValue;
    }
  }

  /**
   * Sets the booleanProperty attribute of the GruntspudHost object
   * 
   * @param key The new booleanProperty value
   * @param value The new booleanProperty value
   */
  public void setIntegerProperty(String key, int value) {
    setProperty(key, String.valueOf(value));
  }

  /**
   * Sets the property attribute of the JDK14GruntspudHost object
   * 
   * @param key The new property value
   * @param value The new property value
   */
  public void setProperty(String key, String value) {
    if (value == null) {
      properties.remove(key);
    } else {
      properties.put(key, value);
    }
  }

  /**
   * Sets the booleanProperty attribute of the JDK14GruntspudHost object
   * 
   * @param key The new booleanProperty value
   * @param value The new booleanProperty value
   */
  public void setBooleanProperty(String key, boolean value) {
    setProperty(key, String.valueOf(value));
  }

  /**
   * Description of the Method
   * 
   * @param node Description of the Parameter
   * 
   * @exception IOException Description of the Exception
   */
  public void openNode(final CVSFileNode node) throws IOException {
    if (node.isLeaf() && node.getFile().exists() && node.getFile().canRead()
        && (CVSSubstType.CVS_SUBST_TYPE_BINARY != node.getCVSSubstType())) {
      for (int i = 0; i < openFiles.size(); i++) {
        if (((File) openFiles.elementAt(i)).equals(node.getFile())) {
          Toolkit.getDefaultToolkit().beep();
          return;
        }
      }
      String encoding = CVSUtil.getEncodingForRoot(context, node.getCVSRoot());
      openFiles.addElement(node.getFile());
      node.reset();
      gruntspud.repaint();
      boolean conflict = (node.getEntry() != null)
          && node.getEntry().hadConflicts();
      if (getBooleanProperty(PROP_DOCK_EDITOR_AS_TAB, false)) {
        DockedEditorView v = new DockedEditorView(context, node.getIcon(false),
            conflict);
        context.getViewManager().addView(v);
        context.getViewManager().setSelectedView(v);
        v.openFile(node.getFile(), encoding);
      } else {
        SimpleEditorFrame f = new SimpleEditorFrame(context, conflict);
        f.openFile(node.getFile(), encoding);
        f.addWindowListener(new WindowAdapter() {
          public void windowClosed(WindowEvent evt) {
            openFiles.removeElement(node.getFile());
            node.reset();
            gruntspud.repaint();
          }
        });
      }
    }
  }

  /**
   * Description of the Method
   * 
   * @param component Description of the Parameter
   * @param key Description of the Parameter
   */
  public void saveGeometry(java.awt.Window component, String key) {
    StringBuffer buf = new StringBuffer();
    buf.append(component.getLocation().x);
    buf.append(',');
    buf.append(component.getLocation().y);
    buf.append(',');
    buf.append(component.getSize().width);
    buf.append(',');
    buf.append(component.getSize().height);
    setProperty(key, buf.toString());
  }

  /**
   * Description of the Method
   * 
   * @param component Description of the Parameter
   * @param key Description of the Parameter
   */
  public void loadGeometry(java.awt.Window component, String key) {
    String geo = getProperty(key);
    if (geo != null) {
      StringTokenizer t = new StringTokenizer(geo, ",");
      try {
        component.setLocation(Integer.parseInt(t.nextToken()), Integer
            .parseInt(t.nextToken()));
        component.setSize(Integer.parseInt(t.nextToken()), Integer.parseInt(t
            .nextToken()));
      } catch (Exception e) {
      }
    } else {
      component.pack();
      UIUtil.positionComponent(SwingConstants.CENTER, component);
    }
  }

  /**
   * Description of the Method
   * 
   * @param key Description of the Parameter
   * 
   * @return DOCUMENT ME!
   */
  public boolean isGeometryStored(String key) {
    return !getProperty(key, "").equals("");
  }

  /**
   * Description of the Method
   * 
   * @param command Description of the Parameter
   */
  public void runCommand(String command) {
    gruntspud.runCommand(command);
  }

  /**
   * Gets the useCanonicalPaths attribute of the JDK14GruntspudHost object
   * 
   * @return The useCanonicalPaths value
   */
  public boolean isUseCanonicalPaths() {
    return false;
  }

  /**
   * Description of the Method
   */
  public void cleanUp() {
    gruntspud.cleanUp();
    saveGeometry(this, PROP_GEOMETRY);
    FileOutputStream out = null;
    /*
     * Write to a temporary file incase anything goes wrong during the write.
     * Preferences have been known to go missing when you get OutOfMemory for
     * example
     */
    File old = new File(prefFile.getAbsolutePath() + ".old");
    if (prefFile.exists() && !prefFile.renameTo(old)) {
      Constants.SYSTEM_LOG.error("Could not rename preferences file "
          + prefFile.getAbsolutePath() + " to .old");
    } else {
      try {
        out = new FileOutputStream(prefFile);
        properties.store(out, "Gruntspud");
        out.flush();
        if (old.exists() && !old.delete())
            throw new IOException("Could not delete old preferences file "
                + old.getAbsolutePath());
      } catch (IOException ioe) {
        ioe.printStackTrace();
      } finally {
        GruntspudUtil.closeStream(out);
      }
    }
  }

  /**
   * Show a dialog containing the preferences for Gruntspud.
   */
  public void showPreferences() {
      
      
    //final Tabber preferences = new Tabber();
    //UIUtil.setTabLayoutPolicy(preferences, 1);
    final ToolBarTabber preferences = new ToolBarTabber();
    for (Iterator i = context.optionsTabs(); i.hasNext();) {
      Class c = (Class) i.next();
      try {
        OptionsTab o = (OptionsTab) c.newInstance();
        // horrid
        o.init(context);
        preferences.addTab(o, o.getTabTitle().equals("Connection"));
        //preferences.addTab(o);
      } catch (Throwable t) {
        t.printStackTrace();
        Constants.UI_LOG.error(t);
      }
    }
    OptionDialog.Option ok = new OptionDialog.Option("Ok", "Ok", 'o');
    OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel",
        'c');
    OptionDialog.Option opt = OptionDialog.showOptionDialog("Preferences",
        context, gruntspud, new OptionDialog.Option[]{ok, cancel}, preferences,
        "Preferences", ok, new OptionDialog.Callback() {
          public boolean canClose(OptionDialog dialog,
              OptionDialog.Option option) {
            return preferences.validateTabs();
          }

        public void close(OptionDialog dialog, Option option) {
            // TODO Auto-generated method stub
            
        }
        }, true, false, new Dimension(610, 530));
    if (opt != ok) { return; }
    preferences.applyTabs();
    reset();
  }

  /**
   * Return all of the available look and feels
   * 
   * @return array of look and feel info
   */
  public static UIManager.LookAndFeelInfo[] getAllLookAndFeelInfo() {
    return allLookAndFeelInfo;
  }

  /**
   * Set the look and feel
   * 
   * @param context DOCUMENT ME!
   * @param className laf class name
   * 
   * @throws Exception on any error setting the look and feel
   */
  public static void setLookAndFeel(GruntspudContext context, String className)
      throws Exception {
    LookAndFeel laf = (LookAndFeel) Class.forName(className).newInstance();
    if (laf.getClass().getName().equals(
        "com.oyoaha.swing.plaf.oyoaha.OyoahaLookAndFeel")
        && !System.getProperty("oyoaha.theme").equals("")) {
      Method m = UIManager.getLookAndFeel().getClass().getMethod(
          "setOyoahaTheme", new Class[]{File.class});
      File f = new File(System.getProperty("oyoaha.theme"));
      m.invoke(laf, new File[]{f});
    }
    //  Now actually set the look and feel
    Constants.SYSTEM_LOG.info("Setting look and feel " + laf.getName() + " ("
        + laf.getClass().getName() + ") - current is "
        + UIManager.getLookAndFeel().getClass());
    if (laf != null
        && !laf.getClass().getName().equals(
            UIManager.getLookAndFeel().getClass().getName())) {
      UIManager.setLookAndFeel(laf);
      UIManager.put("EditorPane.font", UIManager.getFont("TextArea.font"));
    }
    //  Update the component tree
    if (context != null) {
      SwingUtilities
          .updateComponentTreeUI(context.getHost().getMainComponent());
      Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class,
          context.getHost().getMainComponent());
      if (w != null) {
        SwingUtilities.updateComponentTreeUI(w);
      }
    }
    //  Change some of the icons
    UIManager.put("OptionPane.errorIcon", new IconResource(
        "images/dialog-error4.png"));
    UIManager.put("OptionPane.informationIcon", new IconResource(
        "images/dialog-information.png"));
    UIManager.put("OptionPane.warningIcon", new IconResource(
        "images/dialog-warning2.png"));
    UIManager.put("OptionPane.questionIcon", new IconResource(
        "images/dialog-question3.png"));
  }

  /**
   * DOCUMENT ME!
   */
  public void updateComponentTreeUI() {
    getDefaultIcons();
    Frame[] frames = Frame.getFrames();
    for (int i = 0; i < frames.length; i++) {
      SwingUtilities.updateComponentTreeUI(frames[i]);
      updateAllDialogComponentTreeUIs(frames[i]);
    }
    if (context != null) {
      if (context.getViewManager() != null)
          SwingUtilities.updateComponentTreeUI(context.getViewManager());
    }
  }

  private static void updateAllDialogComponentTreeUIs(Window win) {
    Window[] children = win.getOwnedWindows();
    for (int i = 0; i < children.length; i++) {
      if (win instanceof Frame) {
        continue;
      }
      SwingUtilities.updateComponentTreeUI(win);
      updateAllDialogComponentTreeUIs(win);
    }
  }

  /**
   * The main program for the JDK14GruntspudHost class
   * 
   * @param args The command line arguments
   * 
   * @exception Exception Description of the Exception
   */
  public static void main(String[] args) throws Exception {
    //  Load the properties
    File prefFile = GruntspudUtil.getPreferenceFile("gruntspud.properties",
        false);
    Properties p = loadProperties(prefFile);
    //  Enabled logging if required
    if ((VersionInfo.getVersion().equals("HEAD") && !System.getProperty(
        "gruntspud.disableDebugging", "false").equals("true"))
        || p.getProperty(Constants.OPTIONS_SYSTEM_DEBUG, "false")
            .equals("true")) {
      //  First see if there is a log4j.xml in the preferences directory
      File log4jFile = GruntspudUtil.getPreferenceFile("log4j.properties",
          false);
      URL log4jURL = (log4jFile.exists() && log4jFile.canRead()) ? log4jFile
          .toURL() : Gruntspud.class.getClassLoader().getResource(
          "resources/log4j.properties");
      System.out.println("Loading log4j properties from " + log4jURL);
      PropertyConfigurator.configure(log4jURL);
      
    }
    //  Disable logging
    else {
      BasicConfigurator.configure();
      Logger.getRootLogger().setLevel(Level.OFF);
    }
    //  Set the look and feel.
    String laf = p.getProperty(PROP_LAF, CROSS_PLATFORM_LAF);
    try {
      if (!laf.equals("")) {
        setLookAndFeel(null, laf);
      }
    } catch (Exception e) {
      Constants.UI_LOG.error("Could not set look and feel.", e);
    }
    //  Create the host and watch for window closing events
    final JDK13GruntspudHost h = new JDK13GruntspudHost(p, prefFile);
    h.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    h.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        if (h.canExit()) {
          h.exit();
        }
      }
    });
    //  Make Gruntspud visibile and initialise it
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        h.setVisible(true);
        h.reset();        
      }
    });
    //  Show the tips
    if (h
        .getBooleanProperty(JDK13GruntspudHost.PROP_SHOW_TIPS_ON_STARTUP, true)) {
      final JTips tips = new JTips(h, h.getIntegerProperty(
          JDK13GruntspudHost.PROP_NEXT_TIP_INDEX, 0), h.getContext());
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          tips.setVisible(true);
          tips.dispose();
          h.setIntegerProperty(JDK13GruntspudHost.PROP_NEXT_TIP_INDEX, tips
              .getTipIndex() + 1);
          h.setBooleanProperty(JDK13GruntspudHost.PROP_SHOW_TIPS_ON_STARTUP, tips
              .isShow());   
        }
      });
    }
  }

  class ToolBarTabber extends JPanel {
    TabToolBar toolBar;

    Vector tabs;

    FolderBar folderBar;

    JPanel viewPane;

    CardLayout layout;

    ScrollingPanel scrolling;

    ToolBarTabber() {
      super(new BorderLayout());
      tabs = new Vector();
      toolBar = new TabToolBar();
      scrolling = new ScrollingPanel(toolBar);
      scrolling.setBorder(BorderFactory.createLoweredBevelBorder());
      folderBar = new FolderBar(" ", UIUtil.EMPTY_ICON,
          FolderBar.NORMAL_FOLDER_BAR);
      folderBar.setBorder(BorderFactory.createCompoundBorder(BorderFactory
          .createLoweredBevelBorder(), BorderFactory.createEmptyBorder(0, 0, 4,
          0)));
      toolBar.setFolderBar(folderBar);
      JPanel centerPane = new JPanel(new BorderLayout());
      centerPane.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
      viewPane = new JPanel(layout = new CardLayout());
      centerPane.add(folderBar, BorderLayout.NORTH);
      centerPane.add(viewPane, BorderLayout.CENTER);
      add(scrolling, BorderLayout.WEST);
      add(centerPane, BorderLayout.CENTER);
    }

    public void applyTabs() {
      for (int i = 0; i < tabs.size(); i++) {
        ((Tab) tabs.elementAt(i)).applyTab();
      }
    }

    public boolean validateTabs() {
      for (int i = 0; i < tabs.size(); i++) {
        Tab t = (Tab) tabs.elementAt(i);
        if (!t.validateTab()) { return false; }
      }
      return true;
    }

    public void addTab(Tab tab) {
      addTab(tab, false);
    }

    public void addTab(Tab tab, boolean sel) {
      String c = (tab.getTabContext() == null) ? "Unknown" : tab
          .getTabContext();
      ToolAction action = new ToolAction(tab.getTabIcon(), tab
          .getTabLargeIcon(), tab.getTabTitle(), tab.getTabToolTipText(), tab
          .getTabMnemonic(), layout, viewPane, c);
      tabs.addElement(tab);
      viewPane.add(tab.getTabComponent(), tab.getTabTitle());
      toolBar.addIcon(action);
      if (sel) {
        layout.show(viewPane, tab.getTabTitle());
        folderBar.setAction(action);
        toolBar.setSelectedContext(c);
      }
      scrolling.setAvailableActions();
    }
  }

  class ToolAction extends AbstractAction implements GruntspudAction {
    CardLayout layout;

    JPanel viewPane;

    ToolAction(Icon icon, Icon largeIcon, String name, String description,
        int mnemonic, CardLayout layout, JPanel viewPane, String context) {
      super(name);
      putValue(GruntspudAction.LARGE_ICON, largeIcon);
      putValue(GruntspudAction.ICON, icon);
      putValue(GruntspudAction.LONG_DESCRIPTION, description);
      putValue(GruntspudAction.CONTEXT, context);
      this.layout = layout;
      this.viewPane = viewPane;
    }

    public boolean checkAvailable() {
      return true;
    }

    public void actionPerformed(ActionEvent evt) {
      layout.show(viewPane, (String) getValue(Action.NAME));
    }
  }

  static class JDK13GruntspudHostPolicy extends Policy {
    PermissionCollection coll;

    JDK13GruntspudHostPolicy() {
      super();
      coll = new JDK13GruntspudHostPermissions();
      coll.add(new java.security.AllPermission());
    }

    public PermissionCollection getPermissions(ProtectionDomain domain) {
      return coll;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
      return coll;
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {
      return true;
    }

    public void refresh() {
    }
  }

  static class JDK13GruntspudHostPermissions extends PermissionCollection {
    private Vector perms;

    private boolean readOnly;

    JDK13GruntspudHostPermissions() {
      perms = new Vector();
    }

    public void add(Permission permission) {
      perms.addElement(permission);
    }

    public boolean implies(Permission permission) {
      return true;
    }

    public Enumeration elements() {
      return perms.elements();
    }

    public void setReadOnly() {
      readOnly = true;
    }

    public boolean isReadOnly() {
      return readOnly;
    }
  }
}