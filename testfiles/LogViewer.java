/*
*  Copyright (C) 2003 Don Brown (mrdon@techie.com)
*  Copyright (C) 2000, 2001 Greg Merrill (greghmerrill@yahoo.com)
*  This file is part of Log Viewer, a plugin for jEdit (http://www.jedit.org).
*  It is heavily  based off Follow (http://follow.sf.net).
*  Log Viewer is free software; you can redistribute it and/or modify
*  it under the terms of version 2 of the GNU General Public
*  License as published by the Free Software Foundation.
*  Log Viewer is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*  You should have received a copy of the GNU General Public License
*  along with Log Viewer; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package logviewer;

//{{{ imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

// from jEdit:
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.DockableWindowUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.util.Log;
//}}}

/**
 * This is the main class of the Log Viewer plugin.
 *
 * @author    <a href="mailto:mrdon@techie.com">Don Brown</a>
 * @author    <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
 * @version   $Revision: 13471 $
 * @see       #main(String[])
 */
public class LogViewer extends JPanel implements EBComponent {

    //{{{ variables
    final static String fileSeparator = System.getProperty("file.separator");
    final static String messageLineSeparator = "\n";
    int currentCursor_ = Cursor.DEFAULT_CURSOR;
    Cursor defaultCursor_;
    Cursor waitCursor_;

    LogViewerAttributes attributes_;
    Map fileToFollowingPaneMap_ = new HashMap();
    JTabbedPane tabbedPane_;
    JPopupMenu popupMenu_;
    JCheckBoxMenuItem wordWrapMI_;
    JCheckBoxMenuItem tailMI_;

    boolean floating;
    View view;

    // Actions
    Open open_;
    Close close_;
    Refresh refresh_;
    Reload reload_;
    Top top_;
    Bottom bottom_;
    Find find_;
    WordWrap wordWrap_;
    Clear clear_;
    ClearAll clearAll_;
    Delete delete_;
    DeleteAll deleteAll_;
    About about_;
    Tail tail_;

    SystemInterface systemInterface_;
    private MouseListener rightClickListener_;

    private boolean initFilesDone = false;

    //}}}

    //{{{ constructor
    /**
     * Constructor
     *
     * @param view      The view the plugin belongs in
     * @param position  The position the plugin is docked in
     */
    public LogViewer(View view, String position) {
        super(new BorderLayout());

        systemInterface_ = new DefaultSystemInterface(LogViewer.this);

        initAttributes();
        this.view = view;
        this.floating = position.equals(DockableWindowManager.FLOATING);

        // initialize tabbedPane, but wait to open files until after frame
        // initialization
        tabbedPane_ = new JTabbedPane(attributes_.getTabPlacement());

        //initAttributes();
        initActions();
        initPopupMenu();

        if (floating) {
            this.setPreferredSize(new Dimension(500, 250));
        }

        tabbedPane_.addMouseListener(getRightClickListener());
        add(BorderLayout.CENTER, tabbedPane_);

        // Open files from attributes; this is done after the frame is complete
        // and all components have been added to it to make sure that the frame
        // can be shown absolutely as soon as possible. If we put this code
        // before frame creation (as in v1.0), frame creation may take longer
        // because there are more threads (spawned in the course of open())
        // contending for processor time.
        // -- danson -- put initFiles in a component listener so that the files
        // aren't even opened until the LogViewer is visible. This also delays
        // loading of the files until after jEdit is up and running, so jEdit
        // starts faster.
        addComponentListener(
            new ComponentAdapter() {
                int tabLastVisible = -1;

                public void componentShown(ComponentEvent ce) {
                    if (!initFilesDone)
                        initFiles();
                    if (tabLastVisible > -1) {
                        tabbedPane_.setSelectedIndex(tabLastVisible);
                    }
                }

                public void componentHidden(ComponentEvent ce) {
                    tabLastVisible = tabbedPane_.getSelectedIndex();
                }
            }
                );

    }

    //}}}

    //{{{ property methods
    /**
     * Gets the property from jEdit
     *
     * @param key  The key to lookup
     * @return     The message value
     */
    public static String getProperty(String key) {
        return jEdit.getProperty(LogViewerPlugin.PROPERTY_PREFIX + key);
    }

    /**
     * Sets the property to jEdit
     *
     * @param key  The key to set
     * @param val  The new property value
     */
    public static void setProperty(String key, String val) {
        jEdit.setProperty(LogViewerPlugin.PROPERTY_PREFIX + key, val);
    }

    //}}}

    //{{{ setDocked method
    /**
     * Sets the docked attribute of the LogViewer object
     *
     * @param docked  The new docked value
     */
    public void setDocked(boolean docked) {
        floating = !docked;
    }
    //}}}

    public boolean isDocked() {
        return !floating;
    }

    public void setFloating(boolean b) {
        floating = b;
    }

    public boolean isFloating() {
        return floating;
    }

    //{{{ shutdown method
    /** Performs closing tasks */
    public void shutdown() {
        if (tabbedPane_.getTabCount() > 0) {
            attributes_.setSelectedTabIndex(tabbedPane_.getSelectedIndex());
        }
    }
    //}}}

    //{{{ getView method
    /**
     * Gets the view attribute of the LogViewer object
     *
     * @return   The view value
     */
    public View getView() {
        return view;
    }
    //}}}

    //{{{ Action facade methods
    /** Opens a file */
    public void open() {
        open_.actionPerformed(null);
    }

    /** Closes a file */
    public void close() {
        close_.actionPerformed(null);
    }

    /** force a read of a file */
    public void refresh() {
        refresh_.actionPerformed(null);
        tabbedPane_.invalidate();
    }

    /** Description of the Method */
    public void reload() {
        reload_.actionPerformed(null);
        tabbedPane_.invalidate();
    }

    /** Goes to the top of a file */
    public void top() {
        top_.actionPerformed(null);
    }

    /** Goes to the bottom of a file */
    public void bottom() {
        bottom_.actionPerformed(null);
    }

    /** Description of the Method */
    public void find() {
        find_.actionPerformed(null);
    }

    /** Clears the contents of a file */
    public void clear() {
        clear_.actionPerformed(null);
    }

    /** Clears the contents of all opened files */
    public void clearAll() {
        clearAll_.actionPerformed(null);
    }

    /** Deletes the contents of a file */
    public void delete() {
        delete_.actionPerformed(null);
    }

    /** Deletes the contents of all opened files */
    public void deleteAll() {
        deleteAll_.actionPerformed(null);
    }

    /** Word wraps the contents of a file */
    public void wordWrap() {
        wordWrap_.actionPerformed(null);
    }

    /** Displays info about the plugin */
    public void about() {
        about_.actionPerformed(null);
    }
    //}}}

    //{{{ EBComponent methods
    /** Adds a notify for this plugin */
    public void addNotify() {
        super.addNotify();
        EditBus.addToBus(this);
        reinitFiles();
    }

    /** Removes a notify for this plugin */
    public void removeNotify() {
        super.removeNotify();
        EditBus.removeFromBus(this);
        for (Iterator it = fileToFollowingPaneMap_.values().iterator(); it.hasNext(); ) {
            ((FileFollowingPane)it.next()).stopFollowing();
        }
    }

    /**
     * Handles a jEdit message
     *
     * @param message  The message
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof PropertiesChanged) {
            Iterator followers =
                    fileToFollowingPaneMap_.values().iterator();
            FileFollowingPane pane;
            while (followers.hasNext()) {
                pane = (FileFollowingPane) followers.next();
                pane.getFileFollower().setBufferSize(
                        attributes_.getBufferSize()
                        );
                pane.getFileFollower().setLatency(attributes_.getLatency());
                //pane.getTextArea().setFont( attributes_.getFont() );
                pane.getComponent().setFont(attributes_.getFont());
                pane.setAutoPositionCaret(attributes_.autoScroll());
                tabbedPane_.invalidate();
                tabbedPane_.repaint();
            }
            tabbedPane_.setTabPlacement(
                    attributes_.getTabPlacement()
                    );
            tabbedPane_.invalidate();
        }
        else if (message instanceof DockableWindowUpdate) {
            DockableWindowUpdate dwu = (DockableWindowUpdate)message;
            String dockable = dwu.getDockable();
            if (dockable != null && dockable.equals("log-viewer")) {
                setFloating(!floating);
            }

        }
    }

    //}}}

    // {{{ init methods

    /** Initializes the attributes */
    void initAttributes() {
        attributes_ = new LogViewerAttributes();
    }

    /** Initializes the actions */
    void initActions() {
        open_ = new Open(this, getProperty("open.label"));
        close_ = new Close(this, getProperty("close.label"));
        refresh_ = new Refresh(this, getProperty("refresh.label"));
        reload_ = new Reload(this, getProperty("reload.label"));
        top_ = new Top(this, getProperty("firstLine.label"));
        bottom_ = new Bottom(this, getProperty("lastLine.label"));
        find_ = new Find(this, getProperty("find.label"));
        clear_ = new Clear(this, getProperty("clear.label"));
        clearAll_ = new ClearAll(this, getProperty("clearAll.label"));
        delete_ = new Delete(this, getProperty("delete.label"));
        deleteAll_ = new DeleteAll(this, getProperty("deleteAll.label"));
        wordWrap_ = new WordWrap(this, getProperty("wordWrap.label"));
        wordWrapMI_ = new JCheckBoxMenuItem(wordWrap_);
        tail_ = new Tail(this, getProperty("tail.label"));
        tailMI_ = new JCheckBoxMenuItem(tail_);
        tailMI_.setSelected(true);
        about_ = new About(this, getProperty("about.label"));
    }

    /** Initializes the popup menu */
    void initPopupMenu() {
        popupMenu_ = new JPopupMenu();
        popupMenu_.add(open_);
        popupMenu_.add(close_);
        popupMenu_.addSeparator();
        popupMenu_.add(refresh_);
        popupMenu_.add(reload_);
        popupMenu_.add(top_);
        popupMenu_.add(bottom_);
        popupMenu_.add(tailMI_);
        popupMenu_.add(find_);
        popupMenu_.addSeparator();
        popupMenu_.add(wordWrapMI_);
        popupMenu_.addSeparator();
        popupMenu_.add(clear_);
        popupMenu_.add(clearAll_);
        popupMenu_.add(delete_);
        popupMenu_.add(deleteAll_);
    }

    void reinitFiles() {
        initFilesDone = false;
        initFiles();
    }

    /** Initializes the previously opened files */
    void initFiles() {

        if (initFilesDone)
            return;

        Iterator i = attributes_.getFollowedFiles();
        StringBuffer nonexistentFilesBuffer = null;
        int nonexistentFileCount = 0;
        File file;
        while (i.hasNext()) {
            file = (File) i.next();
            if (file.exists()) {
                open(file, false, false);
            }
            else {
                // This file has been deleted since the previous execution. Remove it
                // from the list of followed files
                attributes_.removeFollowedFile(file);
                nonexistentFileCount++;
                if (nonexistentFilesBuffer == null) {
                    nonexistentFilesBuffer = new StringBuffer(file.getAbsolutePath());
                }
                else {
                    nonexistentFilesBuffer.append(file.getAbsolutePath());
                }
                nonexistentFilesBuffer.append(messageLineSeparator);
            }
        }
        /* this is annoying...
        if (nonexistentFileCount > 0) {
            // Alert the user of the fact that one or more files have been
            // deleted since the previous execution
            String message = MessageFormat.format(
                    getProperty("message.filesDeletedSinceLastExecution.text"),
                    new Object[]{
                    new Long(nonexistentFileCount),
                    nonexistentFilesBuffer.toString()
                    }
                    );
            JOptionPane.showMessageDialog(
                    this,
                    message,
                    getProperty("message.filesDeletedSinceLastExecution.title"),
                    JOptionPane.WARNING_MESSAGE
                    );
        }
        */
        if (tabbedPane_.getTabCount() > 0) {
            if (tabbedPane_.getTabCount() > attributes_.getSelectedTabIndex()) {
                tabbedPane_.setSelectedIndex(attributes_.getSelectedTabIndex());
            }
            else {
                tabbedPane_.setSelectedIndex(0);
            }
        }
        else {
            close_.setEnabled(false);
            refresh_.setEnabled(false);
            reload_.setEnabled(false);
            top_.setEnabled(false);
            bottom_.setEnabled(false);
            clear_.setEnabled(false);
            clearAll_.setEnabled(false);
            delete_.setEnabled(false);
            deleteAll_.setEnabled(false);
            find_.setEnabled(false);
            wordWrap_.setEnabled(false);
        }

        for (int x = 0; x < tabbedPane_.getTabCount(); x++) {
            ((FileFollowingPane) tabbedPane_.getComponentAt(x)).startFollowing();
        }

        initFilesDone = true;
    }
    //}}}

    //{{{ open method
    /**
     * Opens a file. Warning: This method should be called only from (1) the
     * FollowApp initializer (before any components are realized) or (2) from
     * the event dispatching thread.
     *
     * @param file                 The file to open
     * @param addFileToAttributes  Whether to add the file to the opened files
     *      list
     * @param startFollowing       Whether to start tailing the file
     */
    void open(File file, boolean addFileToAttributes, boolean startFollowing) {
        FileFollowingPane fileFollowingPane =
                (FileFollowingPane) fileToFollowingPaneMap_.get(file);
        if (fileFollowingPane != null) {
            // File is already open; merely select its tab
            tabbedPane_.setSelectedComponent(fileFollowingPane);
        }
        else {
            fileFollowingPane = new FileFollowingPane(
                    file,
                    attributes_.getBufferSize(),
                    attributes_.getLatency(),
                    attributes_.autoScroll()
                    );


            ///JTextArea ffpTextArea = fileFollowingPane.getTextArea();
            javax.swing.JComponent ffpTextArea = fileFollowingPane.getComponent();
            ffpTextArea.setFont(attributes_.getFont());
            ffpTextArea.addMouseListener(getRightClickListener());
            fileToFollowingPaneMap_.put(file, fileFollowingPane);
            if (startFollowing) {
                fileFollowingPane.startFollowing();
            }
            tabbedPane_.addTab(
                    file.getName(),
                    null,
                    fileFollowingPane,
                    file.getAbsolutePath()
                    );
            tabbedPane_.setSelectedIndex(tabbedPane_.getTabCount() - 1);
            if (!close_.isEnabled()) {
                close_.setEnabled(true);
                refresh_.setEnabled(true);
                reload_.setEnabled(true);
                top_.setEnabled(true);
                bottom_.setEnabled(true);
                clear_.setEnabled(true);
                clearAll_.setEnabled(true);
                delete_.setEnabled(true);
                deleteAll_.setEnabled(true);
                find_.setEnabled(true);
                wordWrap_.setEnabled(true);
            }
            if (addFileToAttributes) {
                attributes_.addFollowedFile(file);
            }
        }
    }
    //}}}

    //{{{ open method
    /**
     * Opens a file
     *
     * @param file                 The file to open
     * @param addFileToAttributes  Whether to add the file to the opened file
     *      list
     */
    void open(File file, boolean addFileToAttributes) {
        open(file, addFileToAttributes, true);
    }
    //}}}

    //{{{ setCursor method
    /**
     * Warning: This method should be called only from the event dispatching
     * thread.
     *
     * @param cursorType  may be Cursor.DEFAULT_CURSOR or Cursor.WAIT_CURSOR
     */
    void setCursor(int cursorType) {
        if (cursorType == currentCursor_) {
            return;
        }
        switch (cursorType) {
            case Cursor.DEFAULT_CURSOR:
                if (defaultCursor_ == null) {
                    defaultCursor_ = Cursor.getDefaultCursor();
                }
                this.setCursor(defaultCursor_);
                break;
            case Cursor.WAIT_CURSOR:
                if (waitCursor_ == null) {
                    waitCursor_ = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                }
                this.setCursor(waitCursor_);
                break;
            default:
                throw new IllegalArgumentException(
                        "Supported cursors are Cursor.DEFAULT_CURSOR and Cursor.WAIT_CURSOR"
                        );
        }
        currentCursor_ = cursorType;
    }
    //}}}

    //{{{ getSelectedFileFollowingPane method
    /**
     * Gets the currently selected file following pane
     *
     * @return   The selectedFileFollowingPane value
     */
    FileFollowingPane getSelectedFileFollowingPane() {
        return (FileFollowingPane) tabbedPane_.getSelectedComponent();
    }
    //}}}

    //{{{ getAllFileFollowingPanes method
    /**
     * Gets the all the file following panes
     *
     * @return   The list of file following panes
     */
    List getAllFileFollowingPanes() {
        int tabCount = tabbedPane_.getTabCount();
        List allFileFollowingPanes = new ArrayList();
        for (int i = 0; i < tabCount; i++) {
            allFileFollowingPanes.add(tabbedPane_.getComponentAt(i));
        }
        return allFileFollowingPanes;
    }
    //}}}

    //{{{ getRightClickListener method
    /**
     * Lazy initializer for the right-click listener which invokes a popup menu
     *
     * @return   The popup listener
     */
    private MouseListener getRightClickListener() {
        if (rightClickListener_ == null) {
            rightClickListener_ =
                new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            Component source = e.getComponent();
                            FileFollowingPane ffp = getSelectedFileFollowingPane();
                            if (ffp != null) {
                                wordWrapMI_.setSelected(ffp.getWordWrap());
                                tailMI_.setSelected(ffp.autoPositionCaret());
                            }
                            popupMenu_.show(source, e.getX(), e.getY());
                        }
                    }
                };
        }
        return rightClickListener_;
    }

    //}}}
}

