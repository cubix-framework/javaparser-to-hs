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

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

/**
 *  Facade for log viewer settings
 *
 * @author    <a href="mailto:mrdon@techie.com">Don Brown</a>
 * @author    <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
 */
class LogViewerAttributes {

    final static String userHome = System.getProperty("user.home");
    final static String propertyPrototypeFileName =
            "followApp.properties.prototype";
    final static int bufferSize = 32768;
    final static String followedFilesKey = "followedFiles";
    final static String tabPlacementKey = "tabs.placement";
    final static String selectedTabIndexKey = "tabs.selectedIndex";
    final static String lastFileChooserDirKey = "fileChooser.lastDir";
    final static String bufferSizeKey = "bufferSize";
    final static String latencyKey = "latency";
    final static String attributesVersionKey = "attributesVersion";
    final static String fontFamilyKey = "fontFamily";
    final static String fontStyleKey = "fontStyle";
    final static String fontSizeKey = "fontSize";
    final static String confirmDeleteKey = "confirmDelete";
    final static String confirmDeleteAllKey = "confirmDeleteAll";
    final static String wordWrapKey = "wordWrap";
    final static String autoScrollKey = "autoScroll";

    EnumeratedProperties properties_;
    private EnumeratedProperties defaultProperties_;
    private LogViewerAttributes defaultAttributes_;

    /**  Constructor */
    LogViewerAttributes() {
        properties_ = new EnumeratedProperties();
    }

    /**
     *  Whether to confirm deletes
     *
     * @return    True if they should be confirmed
     */
    public boolean confirmDelete() {
        return getBoolean(confirmDeleteKey);
    }
    /**
     *  Sets the confirmDelete flag
     *
     * @param  value  The new confirmDelete value
     */
    public void setConfirmDelete(boolean value) {
        setBoolean(confirmDeleteKey, value);
    }
    
    public boolean wordWrap() {
      return getBoolean(wordWrapKey);  
    }
    
    public void setWordWrap(boolean value) {
      setBoolean(wordWrapKey, value);  
    }

    /**
     *  Whether to confirm delete all actions
     *
     * @return    True if should be confirmed
     */
    public boolean confirmDeleteAll() {
        return getBoolean(confirmDeleteAllKey);
    }
    /**
     *  Sets the confirmDeleteAll flag
     *
     * @param  value  The new confirmDeleteAll value
     */
    public void setConfirmDeleteAll(boolean value) {
        setBoolean(confirmDeleteAllKey, value);
    }

    /**
     *  Whether to autoscroll
     *
     * @return    True if should autoscroll
     */
    public boolean autoScroll() {
        return getBoolean(autoScrollKey);
    }
    /**
     *  Sets the autoScroll flag
     *
     * @param  value  The new autoScroll value
     */
    public void setAutoScroll(boolean value) {
        setBoolean(autoScrollKey, value);
    }

    /**
     *  Gets a list of followed files
     *
     * @return    The iterator of the list
     */
    Iterator getFollowedFiles() {
        List fileNames = properties_.getEnumeratedProperty(followedFilesKey);
        List files = new ArrayList();
        Iterator i = fileNames.iterator();
        while (i.hasNext()) {
            files.add(new File((String) i.next()));
        }
        return files.iterator();
    }

    /**
     *  Whether the file is being followed
     *
     * @param  file             The file to check
     * @return                  true if any File in the List of followed Files
     *      (getFollowedFiles()) has the same Canonical Path as the supplied
     *      File
     * @exception  IOException  If something goes wrong
     */
    boolean followedFileListContains(File file)
             throws IOException {
        Iterator i = getFollowedFiles();
        while (i.hasNext()) {
            File nextFile = (File) i.next();
            if (nextFile.getCanonicalPath().equals(file.getCanonicalPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     *  Adds a followed file
     *
     * @param  file  The file to add
     */
    void addFollowedFile(File file) {
        List fileNames = properties_.getEnumeratedProperty(followedFilesKey);
        fileNames.add(file.getAbsolutePath());
        properties_.setEnumeratedProperty(
                followedFilesKey,
                fileNames
                );
    }

    /**
     *  Removes the followed file
     *
     * @param  file  The file
     */
    void removeFollowedFile(File file) {
        List fileNames = properties_.getEnumeratedProperty(followedFilesKey);
        fileNames.remove(file.getAbsolutePath());
        properties_.setEnumeratedProperty(
                followedFilesKey,
                fileNames
                );
    }

    /**
     *  Gets where the tabs should be placed
     *
     * @return    Where the tabs should be placed
     */
    int getTabPlacement() {
        return getInt(tabPlacementKey, JTabbedPane.TOP);
    }
    /**
     *  Sets where the tabs should be placed
     *
     * @param  tabPlacement  The new tab location
     */
    void setTabPlacement(int tabPlacement) {
        setInt(tabPlacementKey, tabPlacement);
    }

    /**
     *  Gets the index of the selected tab
     *
     * @return    The selectedTabIndex value
     */
    int getSelectedTabIndex() {
        try {
            return getInt(selectedTabIndexKey, 0);
        }
        catch (NumberFormatException e) {
            setSelectedTabIndex(0);
            return 0;
        }
    }
    /**
     *  Sets which tab is selected
     *
     * @param  selectedTabIndex  The new selectedTabIndex value
     */
    void setSelectedTabIndex(int selectedTabIndex) {
        setInt(selectedTabIndexKey, selectedTabIndex);
    }

    /**
     *  Gets the last used directory
     *
     * @return    The directory
     */
    File getLastFileChooserDirectory() {
        String dir = LogViewer.getProperty(lastFileChooserDirKey);
        if (dir == null) {
            dir = userHome;
        }
        return new File(dir);
    }
    /**
     *  Sets the last used directory
     *
     * @param  file  The directory
     */
    void setLastFileChooserDirectory(File file) {
    	if (file != null)
    		LogViewer.setProperty(lastFileChooserDirKey, file.getAbsolutePath());
    }

    /**
     *  Gets the buffer size
     *
     * @return    The bufferSize value
     */
    int getBufferSize() {
        return getInt(bufferSizeKey, 100);
    }
    /**
     *  Sets the buffer size
     *
     * @param  bufferSize  The new bufferSize value
     */
    void setBufferSize(int bufferSize) {
        setInt(bufferSizeKey, bufferSize);
    }
    /**
     *  Sets the buffer size
     *
     * @param  bufferSize  The new bufferSize value
     */
    void setBufferSize(String bufferSize) {
        setBufferSize(Integer.parseInt(bufferSize));
    }

    /**
     *  Gets the latency
     *
     * @return    The latency value
     */
    int getLatency() {
        return getInt(latencyKey, 1000);
    }
    /**
     *  Sets the latency
     *
     * @param  latency  The new latency value
     */
    void setLatency(int latency) {
        setInt(latencyKey, latency);
    }
    /**
     *  Sets the latency
     *
     * @param  latency  The new latency value
     */
    void setLatency(String latency) {
        setLatency(Integer.parseInt(latency));
    }

    /**
     *  Gets the font
     *
     * @return    The font value
     */
    Font getFont() {
        Font font = new Font(
                LogViewer.getProperty(fontFamilyKey),
                getInt(fontStyleKey, 0),
                getInt(fontSizeKey, 0)
                );
        return font;
    }
    /**
     *  Sets the font
     *
     * @param  font  The new font value
     */
    void setFont(Font font) {
        LogViewer.setProperty(fontFamilyKey, font.getFontName());
        setInt(fontStyleKey, font.getStyle());
        setInt(fontSizeKey, font.getSize());
    }

    /**
     *  Gets the property as an int
     *
     * @param  key  The key of the property
     * @param  def  The value to use if none is found
     * @return      The int value
     */
    private int getInt(String key, int def) {
        String val = LogViewer.getProperty(key);
        if (val != null) {
            def = Integer.parseInt(LogViewer.getProperty(key));
        }
        return def;
    }
    /**
     *  Sets the property
     *
     * @param  key    The property key
     * @param  value  The new int value
     */
    private void setInt(String key, int value) {
        LogViewer.setProperty(key, String.valueOf(value));
    }
    /**
     *  Gets the boolean property
     *
     * @param  key  The property key
     * @return      The boolean value
     */
    private boolean getBoolean(String key) {
        return "true".equals(LogViewer.getProperty(key));
    }
    /**
     *  Sets the boolean property
     *
     * @param  key    The property key
     * @param  value  The new boolean value
     */
    private void setBoolean(String key, boolean value) {
        LogViewer.setProperty(key, String.valueOf(value));
    }
}

