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

package gruntspud.jedit;

import gruntspud.CVSFileNode;
import gruntspud.CVSSubstType;
import gruntspud.Constants;
import gruntspud.Gruntspud;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudHost;
import gruntspud.style.TextStyle;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.OptionsTab;
import gruntspud.ui.Tabber;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import jdiff.DualDiff;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.help.HelpViewer;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditorExiting;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.VFSUpdate;
import org.netbeans.lib.cvsclient.file.FileStatus;

import com.hexidec.filter.RGBGreyscaleFilter;

/**
 * A CVS front end for jEdit.
 * 
 * @author magicthize
 * @created 14 May 2002
 */
public class JEditGruntspudHost extends JPanel implements GruntspudHost, EBComponent {
    public final static String AUTO_POPUP_CONSOLE = "gruntspud.jedit.autoPopupConsole";
    public final static String MONOCHROME_ICONS = "gruntspud.jedit.monochomeIcons";

    /*
     * public final static String ERROR_LIST_REPORT_ADD =
     * "gruntspud.jedit.errorList.reportAdd"; public final static String
     * ERROR_LIST_REPORT_REMOVE = "gruntspud.jedit.errorList.reportAdd"; public
     * final static String ERROR_LIST_REPORT_PATCHED =
     * "gruntspud.jedit.errorList.reportPatched"; public final static String
     * ERROR_LIST_REPORT_MODIFIED = "gruntspud.jedit.errorList.reportPatched";
     * public final static String ERROR_LIST_REPORT_UNKNOWN =
     * "gruntspud.jedit.errorList.reportConflicts"; public final static String
     * ERROR_LIST_REPORT_CONFLICTS =
     * "gruntspud.jedit.errorList.reportConflicts"; public final static String
     * ERROR_LIST_REPORT_UPDATED = "gruntspud.jedit.errorList.reportUpdated";
     * public final static String ERROR_LIST_REPORT_EXCEPTIONS =
     * "gruntspud.jedit.errorList.reportExceptions"; public final static String
     * ERROR_LIST_REPORT_STDERR = "gruntspud.jedit.errorList.reportStdErr";
     * public final static String ERROR_LIST_REPORT_CHANGED =
     * "gruntspud.jedit.errorList.reportChanged"; public final static String
     * ERROR_LIST_REPORT_TAGGED = "gruntspud.jedit.errorList.reportTagged";
     */

    public final static Insets FILE_RENDERER_INSETS = new Insets(1, 0, 1, 0);
    public final static Icon DEFAULT_FOLDER_OPEN_ICON = GUIUtilities.loadIcon("OpenFolder.png");
    public final static Icon DEFAULT_FOLDER_CLOSED_ICON = GUIUtilities.loadIcon("Folder.png");
    public final static Icon DEFAULT_LEAF_ICON = GUIUtilities.loadIcon("File.png");
    public final static Icon DEFAULT_LEAF_OPEN_ICON = GUIUtilities.loadIcon("OpenFile.png");
    public final static Icon DEFAULT_TREE_EXPANDED = GUIUtilities.loadIcon("arrow2.png");
    public final static Icon DEFAULT_TREE_COLLAPSED = GUIUtilities.loadIcon("arrow1.png");

    private UpdateLock UPDATE_LOCK = new UpdateLock();

    //
    private static boolean log4jinitialised;

    //
    private org.gjt.sp.jedit.View view;
    private Gruntspud gruntspud;
    private File tmpDir;
    private boolean firstLoadDone;
    private Method infoViewerMethod;
    private GruntspudContext context;
    private String statusLine;
    private String infoLine;
    private HashMap bwImageCache = new HashMap();
    private String propertyPrefix = "";

    /**
     * Creates a new JEditGruntspudHost object.
     * 
     * @param view DOCUMENT ME!
     */
    public JEditGruntspudHost(org.gjt.sp.jedit.View view) {
        super(new BorderLayout());

        // Look for the InfoViewer plugin to see if it can be used to show HTML
        try {
            Class c = Class.forName("infoviewer.InfoViewerPlugin");

            if (c != null) {
                infoViewerMethod = c.getMethod("openURL", new Class[] {
                                org.gjt.sp.jedit.View.class, String.class
                });
            }
        } catch (Exception e) {
        }

        BasicConfigurator.configure();
        if (getBooleanProperty(Constants.OPTIONS_SYSTEM_DEBUG, false)) {
            Logger.getRootLogger().setLevel(Level.DEBUG);
        } else {
            Logger.getRootLogger().setLevel(Level.INFO);
        }

        if (getBooleanProperty(JEditGruntspudHost.MONOCHROME_ICONS, true)) {
            UIUtil.setImageFilter(new RGBGreyscaleFilter());
        }

        //
        //
        this.view = view;
        gruntspud = new Gruntspud(this);
        context = gruntspud.getContext();
        try {
            gruntspud.start();
            gruntspud.init();
            gruntspud.getContext().getViewManager().reload(null);
            add(gruntspud, BorderLayout.CENTER);

            int n = Math.abs(hashCode());

            while (true) {
                tmpDir = new File(new File(System.getProperty("java.io.tmpdir")), "gruntspud_" + n + "_tmp");

                if (!tmpDir.exists()) {
                    if (!tmpDir.mkdir()) {
                        throw new Error("Could not create temporary directory");
                    } else {

                        break;
                    }
                } else {
                    n++;
                }
            }

            context.registerOptionsTab(gruntspud.jedit.OtherOptionsTab.class);
            tmpDir.deleteOnExit();
        } catch (Exception e) {

        }

    }

    public GruntspudContext getContext() {
        return context;
    }

    public Icon getIcon(String name) {
        if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_OPEN)) {
            return DEFAULT_FOLDER_OPEN_ICON;
        } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_CLOSED)) {
            return DEFAULT_FOLDER_CLOSED_ICON;
        } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_LEAF)) {
            return DEFAULT_LEAF_ICON;
        } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_LEAF_OPEN)) {
            return DEFAULT_LEAF_OPEN_ICON;
        } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_TREE_EXPANDED)) {
            return DEFAULT_TREE_EXPANDED;
        } else if (name.equals(Constants.ICON_TOOL_SMALL_DEFAULT_TREE_COLLAPSED)) {
            return DEFAULT_TREE_COLLAPSED;
        } else {
            return UIUtil.getCachedIcon(name);
        }
    }

    public Insets getFileRendererInsets() {
        return FILE_RENDERER_INSETS;
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
        if (style != null && style.getForeground() == null) {
            style = (TextStyle) style.clone();
            style.setForeground(VFS.getDefaultColorFor(node.getCanonicalPath()));
        }
        return style;
    }

    public boolean isNodeOpenedInEditor(CVSFileNode node) {
        String canPath = node.getCanonicalPath();
        if (canPath != null) {
            return jEdit.getBuffer(node.getCanonicalPath()) != null;
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     */
    public void setup() {
    }

    /**
     * 
     */
    public boolean isExitActionAvailable() {
        return false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public boolean canExit() {
        /** @todo prevent exit if command running */
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    public void exit() {
        gruntspud.cleanUp();
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The property value
     */
    public JComponent getAccessoryComponent() {
        return null;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public String getHTMLViewerName() {
        return (infoViewerMethod == null) ? null : "Infoviewer plugin";
    }

    /**
     * View some HTML given a URL.
     * 
     * @param u Description of the Parameter
     */
    public void viewHTML(URL u) {
        try {
            infoViewerMethod.invoke(null, new Object[] {
                            view, u.toExternalForm()
            });
        } catch (Exception e) {
        }
    }

    /**
     * Return the name of the host
     */
    public String getName() {
        return "jEdit";
    }

    /**
     * Gets the diffViewerName attribute of the JEditPlugin object
     * 
     * @return The diffViewerName value
     */
    public String getDiffViewerName() {
        return "jEdit's Diff plugin";
    }

    /**
     * Gets the localizedString attribute of the JEditPlugin object
     * 
     * @param name Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The localizedString value
     */
    public String getLocalizedString(String name, String defaultValue) {
        return defaultValue;
    }

    /**
     * Description of the Method
     * 
     * @param f Description of the Parameter
     * @exception IOException Description of the Exception
     */
    public void openNode(CVSFileNode node) throws IOException {

        // TODO can we / do we need to support encoding in jEdit?
        if (node.isLeaf() && node.getFile().exists() && node.getFile().canRead()
                        && (CVSSubstType.CVS_SUBST_TYPE_BINARY != node.getCVSSubstType())) {
            Buffer buffer = jEdit.openFile(view, node.getFile().getAbsolutePath());
            view.setBuffer(buffer);
        }
    }

    /**
     * Description of the Method
     * 
     * @param textColor Description of the Parameter
     * @param message Description of the Parameter
     */
    public void writeToConsole(TextStyle textStyle, String message) {
        if (view != null) {
            Color c = textStyle.getForeground();
            console.Console console = GruntspudPlugin.getConsole(view, getBooleanProperty(JEditGruntspudHost.AUTO_POPUP_CONSOLE,
                            false));
            if (c == null) {
                c = console.getForeground();
                if (c == null) {
                    c = Color.black;
                }
            }
            console.getOutput().print(c == null ? console.getForeground() : c, message == null ? "<null>" : message);
        }
    }

    /**
     * Description of the Method
     * 
     * @param text Description of the Parameter
     * @param message Description of the Parameter
     */
    public void writeToStatusLine(Color c, String message, Icon icon) {
        statusLine = message;
        setJEditStatusText();
    }

    /**
     * Description of the Method
     * 
     * @param text Description of the Parameter
     * @param message Description of the Parameter
     */
    public void writeToInfoLine(Color c, String message, Icon icon) {
        infoLine = message;
        setJEditStatusText();
    }

    private void setJEditStatusText() {
        if (view != null) {
            StringBuffer buf = new StringBuffer();

            if (statusLine != null) {
                buf.append(statusLine);

            }
            if (buf.length() > 0) {
                buf.append("     ");

            }
            if (infoLine != null) {
                buf.append(infoLine);

            }
            view.getStatus().setMessage(buf.toString());
        }
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
        StringWriter w = new StringWriter();
        PrintWriter pw = new PrintWriter(w);

        if (message != null) {
            pw.println(message);

        }
        if (t != null) {
            t.printStackTrace(pw);

        }
        writeToConsole(gruntspud.getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_ERRORS), w.toString());
    }

    /**
     * Description of the Method
     * 
     * @param msg Description of the Parameter
     */
    public void handleMessage(EBMessage msg) {
        if (msg instanceof VFSUpdate) {
            final VFSUpdate upd = (VFSUpdate) msg;
            if (gruntspud != null) {
                File f = new File(upd.getPath());
                context.getViewManager().fileUpdated(f);
                context.getViewManager().resetNodeUpdateTimer();
            }
        }

        if (msg instanceof BufferUpdate) {
            BufferUpdate update = (BufferUpdate) msg;
            if (update.getBuffer() != null && update.getWhat() == BufferUpdate.SAVED) {
                final File f = new File(update.getBuffer().getPath());
                if (f.exists()) {
                    Thread t = new Thread() {
                        public void run() {
                            GruntspudPlugin.getErrorSource().removeFileErrors(f.getAbsolutePath());
                        }
                    };
                    t.start();
                }
            } else if (update.getBuffer() != null
                            && (update.getWhat() == BufferUpdate.CLOSED || update.getWhat() == BufferUpdate.LOADED)) {
                /** @todo how do i find out if this is a local vfs? */
                final File f = new File(update.getBuffer().getPath());
                if (f.exists()) {
                    Thread t = new Thread() {
                        public void run() {
                            CVSFileNode n = context.getViewManager().fileUpdated(f);
                            context.getViewManager().resetNodeUpdateTimer();
                            if (n != null && n.getLocalStatus() == FileStatus.HAS_CONFLICTS) {
                                GruntspudPlugin.getErrorSource().addConflictErrors(n.getFile());
                            }
                        }
                    };
                    t.start();
                }
            }
        }

        if (msg instanceof PropertiesChanged) {
            PropertiesChanged pch = (PropertiesChanged) msg;

            if (gruntspud != null) {
                gruntspud.init();
            }
        }

        if (msg instanceof EditorExiting) {
            gruntspud.cleanUp();
        }
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The property value
     */
    public JComponent getMainComponent() {
        return gruntspud;
    }

    /**
     * Gets the tmpDir attribute of the JEditPlugin object
     * 
     * @return The tmpDir value
     */
    public File getTmpDir() {
        return tmpDir;
    }

    /**
     * DOCUMENT ME!
     */
    public void reset() {
        gruntspud.init();
    }

    /**
     * Gets the useCanonicalPaths attribute of the JEditPlugin object
     * 
     * @return The useCanonicalPaths value
     */
    public boolean isUseCanonicalPaths() {
        return true;
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key) {
        return jEdit.getProperty(key);
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The property value
     */
    public String getProperty(String key, String defaultValue) {
        return jEdit.getProperty(key, defaultValue);
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The property value
     */
    public void unsetProperty(String key) {
        jEdit.unsetProperty(key);
    }

    /**
     * Gets the property attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The property value
     */
    public Properties getProperties() {
        return jEdit.getProperties();
    }

    /**
     * Gets the booleanProperty attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @return The booleanProperty value
     */
    public boolean getBooleanProperty(String key) {
        return jEdit.getBooleanProperty(key);
    }

    /**
     * Gets the booleanProperty attribute of the JEditPlugin object
     * 
     * @param key Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The booleanProperty value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        return jEdit.getBooleanProperty(key, defaultValue);
    }

    /**
     * Gets the booleanProperty attribute of the GruntspudHost object
     * 
     * @param key Description of the Parameter
     * @return The booleanProperty value
     */
    public int getIntegerProperty(String key) {
        return getIntegerProperty(key, 0);
    }

    /**
     * Gets the booleanProperty attribute of the GruntspudHost object
     * 
     * @param key Description of the Parameter
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
     * Sets the property attribute of the JEditPlugin object
     * 
     * @param key The new property value
     * @param value The new property value
     */
    public void setProperty(String key, String value) {
        jEdit.setProperty(key, value);
    }

    /**
     * Sets the booleanProperty attribute of the JEditPlugin object
     * 
     * @param key The new booleanProperty value
     * @param value The new booleanProperty value
     */
    public void setBooleanProperty(String key, boolean value) {
        jEdit.setBooleanProperty(key, value);
    }

    /**
     * Inform of when a command starts running and stops running
     * 
     * @param commandRunning command is running
     */
    public void setCommandRunning(boolean commandRunning) {
        // logo.setAnimate(commandRunning);
    }

    /**
     * Description of the Method
     * 
     * @param component Description of the Parameter
     * @param key Description of the Parameter
     */
    public void saveGeometry(Window component, String key) {
        GUIUtilities.saveGeometry(component, key);
    }

    /**
     * Description of the Method
     * 
     * @param component Description of the Parameter
     * @param key Description of the Parameter
     */
    public void loadGeometry(Window component, String key) {
        GUIUtilities.loadGeometry(component, key);
    }

    /**
     * Description of the Method
     * 
     * @param key Description of the Parameter
     */
    public boolean isGeometryStored(String key) {
        return !getProperty(key + ".width", "").equals("");
    }

    /**
     * Description of the Method
     * 
     * @param f1 Description of the Parameter
     * @param f2 Description of the Parameter
     */
    public void diff(File f1, String title1, File f2, String title2, String encoding) {
        // TODO can we / do we need to support encoding in jEdit?
        String s = f1.getAbsolutePath();
        String s1 = f2.getAbsolutePath();
        VFS vfs = view.getBuffer().getVFS();
        String s2 = vfs.getParentOfPath(view.getBuffer().getPath());
        VFS vfs1 = VFSManager.getVFSForPath(s);
        org.gjt.sp.jedit.io.VFS.DirectoryEntry directoryentry = null;

        try {
            directoryentry = vfs1._getDirectoryEntry(null, s, null);
        } catch (IOException ioexception) {
        }

        if (((directoryentry == null) || (directoryentry.type != 0)) && !MiscUtilities.isURL(s)) {
            s = vfs.constructPath(s2, s);

            try {
                directoryentry = vfs._getDirectoryEntry(null, s, null);
            } catch (IOException ioexception1) {
            }
        }

        VFS vfs2 = VFSManager.getVFSForPath(s1);
        org.gjt.sp.jedit.io.VFS.DirectoryEntry directoryentry1 = null;

        try {
            directoryentry1 = vfs2._getDirectoryEntry(null, s1, null);
        } catch (IOException ioexception2) {
        }

        if (((directoryentry1 == null) || (directoryentry1.type != 0)) && !MiscUtilities.isURL(s1)) {
            s1 = vfs.constructPath(s2, s1);

            try {
                directoryentry1 = vfs._getDirectoryEntry(null, s1, null);
            } catch (IOException ioexception3) {
            }
        }

        int i = 0;

        if ((directoryentry == null) || (directoryentry.type != 0)) {
            i |= 1;

        }
        if ((directoryentry1 == null) || (directoryentry1.type != 0)) {
            i |= 2;

        }
        if (i > 0) {
            GUIUtilities.error(view, "jdiff.file-not-found", new Object[] {
                new Integer(i)
            });

            return;
        }

        Buffer buffer = jEdit.openFile(view, s);
        Buffer buffer1 = jEdit.openFile(view, s1);

        if (buffer == null) {
            GUIUtilities.error(view, "jdiff.file-not-opened", new Object[] {
                new Integer(0)
            });

            return;
        }

        if (buffer1 == null) {
            GUIUtilities.error(view, "jdiff.file-not-opened", new Object[] {
                new Integer(1)
            });

            return;
        }

        EditPane[] aeditpane = view.getEditPanes();

        if (aeditpane.length != 2) {
            if (aeditpane.length > 2) {
                view.unsplit();

            }
            view.splitVertically();
            aeditpane = view.getEditPanes();
        }

        aeditpane[0].setBuffer(buffer);
        aeditpane[1].setBuffer(buffer1);

        if (!DualDiff.isEnabledFor(view)) {
            try {
                DualDiff.toggleFor(view);
            } catch (Throwable t) {
            }
        }
    }

    /**
     * Reload the current tree
     */
    public void reload() {
        gruntspud.getContext().getViewManager().reload(null);
    }

    /**
     * Show a dialog containing the preferences for Gruntspud.
     */
    public void showPreferences() {
        final Tabber preferences = new Tabber();
        UIUtil.setTabLayoutPolicy(preferences, 1);

        // UIUtil.setTabLayoutPolicy(preferences, 1);
        int j = -1;
        int x = 0;
        for (Iterator i = context.optionsTabs(); i.hasNext();) {
            Class c = (Class) i.next();
            try {
                OptionsTab o = (OptionsTab) c.newInstance();
                o.init(context);
                if (o.getTabTitle().equals("Connection")) {
                    j = x;
                }
                preferences.addTab(o);
            } catch (Throwable t) {
                t.printStackTrace();
                Constants.UI_LOG.error(t);
            }
            x++;
        }
        if (j != -1) {
            preferences.setSelectedIndex(j);

        }
        OptionDialog.Option ok = new OptionDialog.Option("Ok", "Ok", 'o');
        OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel", 'c');
        OptionDialog.Option opt = OptionDialog.showOptionDialog("Preferences", context, gruntspud, new OptionDialog.Option[] {
                        ok, cancel
        }, preferences, "Preferences", ok, new OptionDialog.Callback() {
            public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
                return preferences.validateTabs();
            }

            public void close(OptionDialog dialog, Option option) {
                // TODO Auto-generated method stub

            }
        });

        if (opt != ok) {
            return;
        }

        if (preferences.validateTabs()) {
            preferences.applyTabs();
            reset();
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param resource DOCUMENT ME!
     */
    public void showHelp(String resource) {
        new HelpViewer(GruntspudPlugin.class.getResource("/help/html/index.html"));
    }

    /**
     * DOCUMENT ME!
     */
    public void updateComponentTreeUI() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                /** @todo there is probably overkill going on here */
                Frame[] frames = Frame.getFrames();
                for (int i = 0; i < frames.length; i++) {
                    updateFullComponentTreeUI(frames[i]);
                    updateAllDialogComponentTreeUIs(frames[i]);
                }
                if (gruntspud.getContext() != null && gruntspud.getContext().getViewManager() != null)
                    SwingUtilities.updateComponentTreeUI(gruntspud.getContext().getViewManager());
            }
        });
    }

    private static void updateFullComponentTreeUI(Component component) {
        updateFullComponentTreeUI0(component);
        component.invalidate();
        component.validate();
        component.repaint();
    }

    private static void updateFullComponentTreeUI0(Component c) {
        if (c instanceof JComponent) {
            if (c instanceof JTree) {
                JTree tree = (JTree) c;
                if (tree.getCellRenderer() instanceof Component) {
                    if (tree.getCellRenderer() instanceof DefaultTreeCellRenderer) {
                        updateDefaultTreeCellRenderer((DefaultTreeCellRenderer) tree.getCellRenderer());
                    } else {
                        updateFullComponentTreeUI0((Component) tree.getCellRenderer());
                    }
                }
            }
            ((JComponent) c).updateUI();
        }
        Component[] children = null;
        if (c instanceof JMenu) {
            children = ((JMenu) c).getMenuComponents();
        } else if (c instanceof Container) {
            children = ((Container) c).getComponents();
        }
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                updateFullComponentTreeUI0(children[i]);
            }
        }
    }

    private static void updateDefaultTreeCellRenderer(DefaultTreeCellRenderer r) {
        r.setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
        r.setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        r.setOpenIcon(UIManager.getIcon("Tree.openIcon"));

        r.setTextSelectionColor(UIManager.getColor("Tree.selectionForeground"));
        r.setTextNonSelectionColor(UIManager.getColor("Tree.textForeground"));
        r.setBackgroundSelectionColor(UIManager.getColor("Tree.selectionBackground"));
        r.setBackgroundNonSelectionColor(UIManager.getColor("Tree.textBackground"));
        r.setBorderSelectionColor(UIManager.getColor("Tree.selectionBorderColor"));
    }

    private static void updateAllDialogComponentTreeUIs(Window win) {
        Window[] children = win.getOwnedWindows();

        for (int i = 0; i < children.length; i++) {
            if (win instanceof Frame) {
                continue;
            }
            updateFullComponentTreeUI(win);
            updateAllDialogComponentTreeUIs(win);
        }
    }

    class UpdateLock {
    }
}