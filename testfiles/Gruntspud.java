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
package gruntspud;

import gruntspud.authentication.GruntspudAuthenticator;
import gruntspud.file.ChmodFileReadOnlyHandler;
import gruntspud.file.FileModeHandler;
import gruntspud.file.FileTypeMapping;
import gruntspud.file.GruntspudFileUtils;
import gruntspud.file.UnixFileModeHandler;
import gruntspud.ui.LicensePanel;
import gruntspud.ui.TellMeAgainPane;
import gruntspud.ui.ToolBarSeparator;
import gruntspud.ui.UIUtil;
import gruntspud.ui.icons.OverlayIcon;
import gruntspud.ui.preferences.CVSFileFilterPane;
import gruntspud.ui.view.CVSFileNodeTable;
import gruntspud.ui.view.CVSFileNodeTree;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.io.File;
import java.io.IOException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.file.FileReadOnlyHandler;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.lib.cvsclient.file.WindowsFileReadOnlyHandler;
import org.netbeans.lib.cvsclient.util.Logger;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 *
 * @todo
 */
public class Gruntspud extends JPanel implements Constants {
  public final static String APPLICATION_NAME = "Gruntspud";

  public final static String APPLICATION_VERSION = VersionInfo.getVersion();

  public final static String GRUNTSPUD_HOME_PAGE_LOCATION = "http://gruntspud.sourceforge.net";

  private static float javaVersion;

  private static URL gruntspudHomePage;

  private static GruntspudAuthenticator authenticator;
  static {
    UIUtil.cacheIcon(ICON_STATUS_NEEDS_ATTENTION,
        "images/overlay_subfolder_attention.png");
    UIUtil.cacheIcon(ICON_STATUS_MODULE_OVERLAY, "images/overlay_module.png");
    UIUtil.cacheIcon(ICON_STATUS_PROJECT_OVERLAY, "images/overlay_project.png");
    UIUtil.cacheIcon(ICON_STATUS_ERASED_OVERLAY, "images/overlay_erased.png");
    UIUtil.cacheIcon(ICON_STATUS_REMOVED_OVERLAY, "images/overlay_removed.png");
    UIUtil.cacheIcon(ICON_STATUS_ADDED_OVERLAY, "images/overlay_added.png");
    UIUtil.cacheIcon(ICON_STATUS_OUT_OF_DATE_OVERLAY,
        "images/overlay_out_of_date.png");
    UIUtil.cacheIcon(ICON_STATUS_IN_CVS_OVERLAY, "images/overlay_in_cvs.png");
    UIUtil.cacheIcon(ICON_STATUS_CONFLICTS_OVERLAY,
        "images/overlay_conflict.png");
    UIUtil.cacheIcon(ICON_STATUS_NEEDS_CHECKOUT_OVERLAY,
        "images/overlay_needs_checkout.png");
    UIUtil.cacheIcon(ICON_STATUS_NEEDS_MERGE_OVERLAY,
        "images/overlay_needs_merge.png");
    UIUtil.cacheIcon(ICON_STATUS_NEEDS_PATCH_OVERLAY,
        "images/overlay_needs_patch.png");
    UIUtil.cacheIcon(ICON_WINDOWS_LINE_ENDINGS,
        "images/overlay_windows_line_endings.png");
    UIUtil.cacheIcon(ICON_UNIX_LINE_ENDINGS,
        "images/overlay_unix_line_endings.png");
    UIUtil.cacheIcon(ICON_STATUS_ERASED, new OverlayIcon(UIUtil
        .getCachedIcon(ICON_STATUS_ERASED_OVERLAY), UIManager
        .getIcon("Tree.leafIcon"), SwingConstants.CENTER));
    UIUtil.cacheIcon(ICON_STATUS_REMOVED, new OverlayIcon(UIUtil
        .getCachedIcon(ICON_STATUS_REMOVED_OVERLAY), UIManager
        .getIcon("Tree.leafIcon"), SwingConstants.CENTER));
    UIUtil.cacheIcon(ICON_STATUS_ADDED, new OverlayIcon(UIUtil
        .getCachedIcon(ICON_STATUS_ADDED_OVERLAY), UIManager
        .getIcon("Tree.leafIcon"), SwingConstants.CENTER));
    UIUtil.cacheIcon(ICON_STATUS_OUT_OF_DATE, new OverlayIcon(UIUtil
        .getCachedIcon(ICON_STATUS_OUT_OF_DATE_OVERLAY), UIManager
        .getIcon("Tree.leafIcon"), SwingConstants.CENTER));
    UIUtil.cacheIcon(ICON_STATUS_CONFLICTS, new OverlayIcon(UIUtil
        .getCachedIcon(ICON_STATUS_CONFLICTS_OVERLAY), UIManager
        .getIcon("Tree.leafIcon"), SwingConstants.CENTER));
    UIUtil.cacheIcon(ICON_TYPE_BINARY, "images/type_binary.png");
    UIUtil.cacheIcon(ICON_TYPE_TEXT, "images/type_text.png");
    UIUtil.cacheIcon(ICON_TYPE_COPY, "images/type_copy.png");
    UIUtil.cacheIcon(ICON_TYPE_UNICODE, "images/type_unicode.png");
    UIUtil.cacheIcon(ICON_TYPE_IGNORE, "images/type_ignore.png");
    UIUtil.cacheIcon(ICON_TYPE_DIRECTORY, "images/type_directory.png");
    UIUtil.cacheIcon(ICON_TYPE_DEFAULT_LOCKER, "images/type_defaultLocker.png");
    UIUtil.cacheIcon(ICON_TYPE_OLD_VALUES, "images/type_oldValues.png");
    UIUtil.cacheIcon(ICON_TYPE_ONLY_KEYWORDS, "images/type_onlyKeywords.png");
    UIUtil.cacheIcon(ICON_TYPE_ONLY_VALUES, "images/type_onlyValues.png");
    UIUtil.cacheIcon(ICON_STATUS_ACTIVE, "images/greenledon.png");
    UIUtil.cacheIcon(ICON_STATUS_IDLE, "images/greenledoff.png");
    UIUtil.cacheIcon(ICON_STATUS_ERROR, "images/redledon.png");
    UIUtil.cacheIcon(ICON_TOOL_CHECKOUT, "images/tool_checkout.png");
    UIUtil.cacheIcon(ICON_TOOL_IMPORT, "images/tool_import.png");
    UIUtil.cacheIcon(ICON_TOOL_UPDATE, "images/tool_update.png");
    UIUtil.cacheIcon(ICON_TOOL_COMMIT, "images/tool_commit.png");
    UIUtil.cacheIcon(ICON_TOOL_ADD, "images/tool_add.png");
    UIUtil.cacheIcon(ICON_TOOL_REMOVE, "images/tool_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_PREFERENCES, "images/tool_preferences.png");
    UIUtil.cacheIcon(ICON_TOOL_STATUS, "images/tool_status.png");
    UIUtil.cacheIcon(ICON_TOOL_HOME, "images/tool_home.png");
    UIUtil.cacheIcon(ICON_TOOL_UP, "images/tool_up.png");
    UIUtil.cacheIcon(ICON_TOOL_GO, "images/tool_go.png");
    UIUtil.cacheIcon(ICON_TOOL_BROWSE, "images/tool_browse.png");
    UIUtil.cacheIcon(ICON_TOOL_RELOAD, "images/tool_reload.png");
    UIUtil.cacheIcon(ICON_TOOL_STOP_COMMAND, "images/tool_stop_command.png");
    UIUtil.cacheIcon(ICON_TOOL_DIFF, "images/tool_diff.png");
    UIUtil.cacheIcon(ICON_TOOL_LOG, "images/tool_log.png");
    UIUtil.cacheIcon(ICON_TOOL_CUT, "images/tool_cut.png");
    UIUtil.cacheIcon(ICON_TOOL_COPY, "images/tool_copy.png");
    UIUtil.cacheIcon(ICON_TOOL_PASTE, "images/tool_paste.png");
    UIUtil.cacheIcon(ICON_TOOL_DELETE, "images/tool_delete.png");
    UIUtil.cacheIcon(ICON_TOOL_OPEN, "images/tool_open.png");
    UIUtil.cacheIcon(ICON_TOOL_SAVE, "images/tool_save.png");
    UIUtil.cacheIcon(ICON_TOOL_SAVE_AS, "images/tool_save_as.png");
    UIUtil.cacheIcon(ICON_TOOL_CONNECT, "images/tool_connect.png");
    UIUtil.cacheIcon(ICON_TOOL_NORMAL_ADD, "images/tool_normal_add.png");
    UIUtil.cacheIcon(ICON_TOOL_NORMAL_REMOVE, "images/tool_normal_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_EDIT, "images/tool_edit.png");
    UIUtil.cacheIcon(ICON_TOOL_UNEDIT, "images/tool_unedit.png");
    UIUtil.cacheIcon(ICON_TOOL_EDITORS, "images/tool_editors.png");
    UIUtil.cacheIcon(ICON_TOOL_DEFAULT, "images/tool_default.png");
    UIUtil.cacheIcon(ICON_TOOL_MAINTAIN, "images/tool_maintain.png");
    UIUtil.cacheIcon(ICON_TOOL_CLEAR, "images/tool_new.png");
    UIUtil.cacheIcon(ICON_TOOL_LEFT, "images/tool_left.png");
    UIUtil.cacheIcon(ICON_TOOL_RIGHT, "images/tool_right.png");
    UIUtil.cacheIcon(ICON_TOOL_TOP, "images/tool_top.png");
    UIUtil.cacheIcon(ICON_TOOL_BOTTOM, "images/tool_bottom.png");
    UIUtil.cacheIcon(ICON_TOOL_OUTPUT, "images/tool_print_preview.png");
    UIUtil.cacheIcon(ICON_TOOL_COLOR, "images/tool_color.png");
    UIUtil.cacheIcon(ICON_TOOL_ERASE, "images/tool_delete.png");
    UIUtil.cacheIcon(ICON_TOOL_IGNORE, "images/tool_ignore.png");
    UIUtil.cacheIcon(ICON_TOOL_NEW_FOLDER, "images/tool_new_folder.png");
    UIUtil.cacheIcon(ICON_TOOL_NEW_FILE, "images/tool_new_file.png");
    UIUtil.cacheIcon(ICON_TOOL_HISTORY, "images/tool_history.png");
    UIUtil.cacheIcon(ICON_TOOL_ANNOTATE, "images/tool_annotate.png");
    UIUtil.cacheIcon(ICON_TOOL_EXIT, "images/tool_exit.png");
    UIUtil.cacheIcon(ICON_TOOL_TAG, "images/tool_tag.png");
    UIUtil.cacheIcon(ICON_TOOL_REMOVE_TAG, "images/tool_remove_tag.png");
    UIUtil.cacheIcon(ICON_TOOL_WATCH, "images/tool_watch.png");
    UIUtil.cacheIcon(ICON_TOOL_WATCHERS, "images/tool_watchers.png");
    UIUtil.cacheIcon(ICON_TOOL_LOGIN, "images/tool_connect.png");
    UIUtil.cacheIcon(ICON_TOOL_LOGOUT, "images/tool_disconnect.png");
    UIUtil.cacheIcon(ICON_TOOL_DETAILS, "images/tool_details.png");
    UIUtil.cacheIcon(ICON_TOOL_ABOUT, "images/tool_about.png");
    UIUtil.cacheIcon(ICON_TOOL_TOGGLE_FLAT_MODE, "images/tool_flat_mode.png");
    UIUtil.cacheIcon(ICON_TOOL_CLOSE_VIEW, "images/tool_close_view.png");
    UIUtil.cacheIcon(ICON_TOOL_FILTER, "images/tool_filter.png");
    UIUtil.cacheIcon(ICON_TOOL_HELP, "images/tool_help.png");
    UIUtil.cacheIcon(ICON_TOOL_WEB, "images/tool_web.png");
    UIUtil.cacheIcon(ICON_TOOL_VIEW_CVS, "images/tool_viewcvs.png");
    UIUtil.cacheIcon(ICON_TOOL_SEARCH, "images/tool_search.png");
    UIUtil.cacheIcon(ICON_TOOL_RETRIEVE, "images/tool_retrieve.png");
    UIUtil.cacheIcon(ICON_TOOL_TOGGLE_VIEW_DOCK, "images/tool_dock.png");
    UIUtil.cacheIcon(ICON_TOOL_RESOLVE, "images/tool_resolve.png");
    UIUtil.cacheIcon(ICON_TOOL_LOCK, "images/tool_lock.png");
    UIUtil.cacheIcon(ICON_TOOL_HOME_UP, "images/tool_home_up.png");
    UIUtil.cacheIcon(ICON_TOOL_HOME_GOTO, "images/tool_home_goto.png");
    UIUtil.cacheIcon(ICON_TOOL_HOME_BROWSE, "images/tool_home_browse.png");
    UIUtil.cacheIcon(ICON_TOOL_UNDO, "images/tool_undo.png");
    UIUtil.cacheIcon(ICON_TOOL_REDO, "images/tool_redo.png");
    UIUtil.cacheIcon(ICON_TOOL_SYSTEM, "images/tool_system.png");
    UIUtil.cacheIcon(ICON_TOOL_DISPLAY, "images/tool_display.png");
    UIUtil.cacheIcon(ICON_TOOL_GLOBAL, "images/tool_global.png");
    UIUtil.cacheIcon(ICON_TOOL_FILE_TYPES, "images/tool_filetypes.png");
    UIUtil.cacheIcon(ICON_TOOL_PLUGIN, "images/tool_plugin.png");
    UIUtil
        .cacheIcon(ICON_TOOL_INSTALL_PLUGIN, "images/tool_install_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_REMOVE_PLUGIN, "images/tool_remove_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_UPDATE_PLUGIN, "images/tool_update_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_CLOSE_ALL_VIEWS,
        "images/tool_close_all_views.png");
    UIUtil.cacheIcon(ICON_TOOL_PROJECT, "images/tool_project.png");
    UIUtil.cacheIcon(ICON_TOOL_PROJECTS, "images/tool_projects.png");
    UIUtil.cacheIcon(ICON_TOOL_ADD_AS_PROJECT, "images/tool_add_project.png");
    UIUtil.cacheIcon(ICON_TOOL_EDITOR, "images/tool_editor.png");
    UIUtil.cacheIcon(ICON_TOOL_NOTES, "images/tool_notes.png");
    UIUtil.cacheIcon(ICON_TOOL_MERGE, "images/tool_merge.png");
    UIUtil.cacheIcon(ICON_TOOL_STICKY, "images/tool_sticky.png");
    
    UIUtil.cacheIcon(ICON_TOOL_LARGE_REMOVE, "images/tool_large_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_COMMIT, "images/tool_large_commit.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_CHECKOUT, "images/tool_large_checkout.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_UPDATE, "images/tool_large_update.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_ADD, "images/tool_large_add.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_IMPORT, "images/tool_large_import.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_PREFERENCES,
        "images/tool_large_preferences.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_STOP_COMMAND,
        "images/tool_large_stop_command.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_DIFF, "images/tool_large_diff.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_LOG, "images/tool_large_log.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_CONNECT, "images/tool_large_connect.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_NORMAL_ADD,
        "images/tool_large_normal_add.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_NORMAL_REMOVE,
        "images/tool_large_normal_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_EDIT, "images/tool_large_edit.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_UNEDIT, "images/tool_large_unedit.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_EDITORS, "images/tool_large_editors.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_DEFAULT, "images/tool_large_default.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_CLEAR, "images/tool_large_new.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_MAINTAIN, "images/tool_large_maintain.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_LEFT, "images/tool_large_left.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_RIGHT, "images/tool_large_right.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_TOP, "images/tool_large_top.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_BOTTOM, "images/tool_large_bottom.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_STATUS, "images/tool_large_status.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_OUTPUT,
        "images/tool_large_print_preview.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_COLOR, "images/tool_large_color.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_DISPLAY, "images/tool_large_display.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_ERASE, "images/tool_large_delete.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_IGNORE, "images/tool_large_ignore.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_NEW_FOLDER,
        "images/tool_large_new_folder.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_NEW_FILE, "images/tool_large_new_file.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_HISTORY, "images/tool_large_history.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_ANNOTATE, "images/tool_large_annotate.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_EXIT, "images/tool_large_exit.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_TAG, "images/tool_large_tag.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_REMOVE_TAG,
        "images/tool_large_remove_tag.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_WATCH, "images/tool_large_watch.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_WATCHERS, "images/tool_large_watchers.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_LOGIN, "images/tool_large_connect.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_LOGOUT, "images/tool_large_disconnect.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_DETAILS, "images/tool_large_details.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_TOGGLE_FLAT_MODE,
        "images/tool_large_flat_mode.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_CLOSE_VIEW,
        "images/tool_large_close_view.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_FILTER, "images/tool_large_filter.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_HELP, "images/tool_large_help.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_WEB, "images/tool_large_web.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_VIEW_CVS, "images/tool_large_viewcvs.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_SEARCH, "images/tool_large_search.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_RETRIEVE, "images/tool_large_retrieve.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_TOGGLE_VIEW_DOCK,
        "images/tool_large_dock.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_SYSTEM, "images/tool_large_system.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_RESOLVE, "images/tool_large_resolve.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_LOCK, "images/tool_large_lock.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_SERVER_IDENTITY,
        "images/tool_large_server_identity.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_GLOBAL, "images/tool_large_global.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_FILE_TYPES,
        "images/tool_large_filetypes.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_PLUGIN, "images/tool_large_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_INSTALL_PLUGIN,
        "images/tool_large_install_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_REMOVE_PLUGIN,
        "images/tool_large_remove_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_UPDATE_PLUGIN,
        "images/tool_large_update_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_CLOSE_ALL_VIEWS,
        "images/tool_large_close_all_views.png");
    UIUtil
        .cacheIcon(ICON_TOOL_LARGE_PROJECTS, "images/tool_large_projects.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_PROJECT, "images/tool_large_project.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_ADD_AS_PROJECT,
        "images/tool_large_add_project.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_EDITOR, "images/tool_large_editor.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_NOTES, "images/tool_large_notes.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_MERGE, "images/tool_large_merge.png");
    UIUtil.cacheIcon(ICON_TOOL_LARGE_STICKY, "images/tool_large_sticky.png");
    
    UIUtil.cacheIcon(ICON_TOOL_SMALL_ADD, "images/tool_small_add.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_REMOVE, "images/tool_small_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_UPDATE, "images/tool_small_update.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_COMMIT, "images/tool_small_commit.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_CHECKOUT, "images/tool_small_checkout.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_STATUS, "images/tool_small_status.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_DIFF, "images/tool_small_diff.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_CUT, "images/tool_small_cut.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_COPY, "images/tool_small_copy.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_PASTE, "images/tool_small_paste.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_DELETE, "images/tool_small_delete.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_LOG, "images/tool_small_log.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_RELOAD, "images/tool_small_reload.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HOME, "images/tool_small_home.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_UP, "images/tool_small_up.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_CONNECT, "images/tool_small_connect.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_PREFERENCES,
        "images/tool_small_preferences.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_IMPORT, "images/tool_small_import.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_ERASE, "images/tool_small_delete.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_IGNORE, "images/tool_small_ignore.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_NEW_FOLDER,
        "images/tool_small_new_folder.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_NEW_FILE, "images/tool_small_new_file.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HISTORY, "images/tool_small_history.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_ANNOTATE, "images/tool_small_annotate.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_EXIT, "images/tool_small_exit.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_TAG, "images/tool_small_tag.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_REMOVE_TAG,
        "images/tool_small_remove_tag.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_MAINTAIN, "images/tool_small_maintain.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_NORMAL_REMOVE,
        "images/tool_small_normal_remove.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_NORMAL_ADD,
        "images/tool_small_normal_add.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_STOP_COMMAND,
        "images/tool_small_stop_command.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_BROWSE, "images/tool_small_browse.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_GO, "images/tool_small_go.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_WATCH, "images/tool_small_watch.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_WATCHERS, "images/tool_small_watchers.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_LOGIN, "images/tool_small_connect.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_LOGOUT, "images/tool_small_disconnect.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_ABOUT, "images/tool_small_about.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_EDIT, "images/tool_small_edit.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_UNEDIT, "images/tool_small_unedit.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_EDITORS, "images/tool_small_editors.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_DETAILS, "images/tool_small_details.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_TOGGLE_FLAT_MODE,
        "images/tool_small_flat_mode.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_CLOSE_VIEW,
        "images/tool_small_close_view.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_FILTER, "images/tool_small_filter.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HELP, "images/tool_small_help.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_WEB, "images/tool_small_web.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_VIEW_CVS, "images/tool_small_viewcvs.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_DEFAULT, "images/tool_small_default.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_SEARCH, "images/tool_small_search.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_RETRIEVE, "images/tool_small_retrieve.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_TOGGLE_VIEW_DOCK,
        "images/tool_small_dock.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_RESOLVE, "images/tool_small_resolve.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_LOCK, "images/tool_small_lock.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_SAVE, "images/tool_small_save.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HOME_UP, "images/tool_small_home_up.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HOME_GOTO,
        "images/tool_small_home_goto.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_HOME_BROWSE,
        "images/tool_small_home_browse.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_SYSTEM, "images/tool_small_system.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_DISPLAY, "images/tool_small_display.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_GLOBAL, "images/tool_small_global.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_FILE_TYPES,
        "images/tool_small_filetypes.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_PLUGIN, "images/tool_small_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_INSTALL_PLUGIN,
        "images/tool_small_install_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_REMOVE_PLUGIN,
        "images/tool_small_remove_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_UPDATE_PLUGIN,
        "images/tool_small_update_plugin.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_CLOSE_ALL_VIEWS,
        "images/tool_small_close_all_views.png");
    UIUtil
        .cacheIcon(ICON_TOOL_SMALL_PROJECTS, "images/tool_small_projects.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_PROJECT, "images/tool_small_project.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_ADD_AS_PROJECT,
        "images/tool_small_add_project.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_NOTES, "images/tool_small_notes.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_MERGE, "images/tool_small_merge.png");
    UIUtil.cacheIcon(ICON_TOOL_SMALL_STICKY, "images/tool_small_sticky.png");
    UIUtil.cacheIcon(GRUNTSPUD_LOGO, "images/gruntspud.png");
    UIUtil.cacheIcon(GRUNTSPUD_LOGO_ANIMATION, "images/anifish.gif");
    UIUtil.cacheIcon(GRUNTSPUD_LOGO_STATIC, "images/fish.gif");
  }
  static {
    try {
      gruntspudHomePage = new URL(GRUNTSPUD_HOME_PAGE_LOCATION);
    } catch (MalformedURLException murle) {
    }
    javaVersion = 1.4f;
    String javaVersionString = System.getProperty("java.version");
    try {
      int idx = javaVersionString.indexOf('.');
      if(idx != -1) {
        idx = javaVersionString.indexOf('.', idx + 1);
        if(idx != -1) {
          javaVersion = Float.parseFloat(javaVersionString.substring(0, idx));
        }
      }
    }
    catch(NumberFormatException nfe) {
    }
  }

  //  Private instance variables
  private CVSFileNodeTree tree;

  private boolean floating;

  private CVSFileNode homeNode;

  private StatusLabel cvsRootLabel;

  private CVSFileNodeTable fileNodeTable;

  private boolean adjusting;

  private CVSFileFilterPane filterPane;

  //    private BasicListener cvsListener;
  private GlobalOptions globalOptions;
  private GruntspudHost host;
  private File home;
  private JSplitPane accessorySplit;
  private GruntspudContext context;
  private JPopupMenu popup;
  private JSplitPane split;
  private JToolBar buttons;
  private JMenu openMenu;
  private boolean pluginManagerStarted;

  /**
   * Construct a new instance of Gruntspud. An implementation of
   * <code>GruntspudHost</code> must be provided to provide required services,
   * such as a preferences backend.
   * 
   * @param host host implementation
   */
  public Gruntspud(GruntspudHost host) {
    //  If enabled, log all CVS output to GRUNTSPUD.in and GRUNTSPUD.out in
    //  the users home directory
    if (host.getBooleanProperty(Constants.OPTIONS_SYSTEM_LOG_CVS_IO, false)) {
      Logger.setLogging(System.getProperty("user.home") + File.separator
          + "GRUNTSPUD");
      //  Its a cheat i know but it seems to always work ok
    }
    System.setProperty("javacvs.multiple_commands_warning", "false");
    //
    this.host = host;
    System.setProperty("gruntspud.hideMenuIcons", String.valueOf(host
        .getBooleanProperty(Constants.OPTIONS_SYSTEM_HIDE_MENU_ICONS, false)));
    System.setProperty("gruntspud.disableKeyboardAccelerators", String
        .valueOf(host.getBooleanProperty(
            Constants.OPTIONS_SYSTEM_DISABLE_KEYBOARD_ACCELERATORS, false)));
    //  Context
    context = new DefaultGruntspudContext(host);
    
    context.getPluginManager().start();//
  }

  public void start() throws Exception {
    context.getViewManager().start(context);
    if(!host.getBooleanProperty(Constants.LICENSE_ACCEPTED, false)) {
      LicensePanel licensePanel = new LicensePanel();
      String[] licenseResources = {"/LICENSE.txt", "/APACHE_LICENSE.txt",
          "/PLUGSPUD_LICENSE.txt", "/SPL_LICENSE.txt"};
      for (int i = 0; i < licenseResources.length; i++) {
        try {
          URL resource = getClass().getResource(
              licenseResources[i]);
          licensePanel.addLicenseText(resource);
        } catch (Exception e) {
          Constants.UI_LOG.error(e);
        }
      }
      if (!licensePanel.showLicense(this)) { 
        throw new Exception("License rejected"); 
      }
      host.setBooleanProperty(Constants.LICENSE_ACCEPTED, true);
    }
    // Disable keyboard accelerators?
    //  Enabled logging to the console if required
    if (host.getBooleanProperty(Constants.OPTIONS_SYSTEM_DEBUG_TO_CONSOLE,
        false)) {
      org.apache.log4j.Logger.getRootLogger().addAppender(
          new GruntspudConsoleAppender());
      //  Are there connection profiles?
    }
    if (context.getConnectionProfileModel().getRowCount() == 0) {
      TellMeAgainPane.showTellMeAgainDialog(context, this,
          "Warn me about this again",
          Constants.MESSAGE_WARN_ABOUT_NO_CONNECTION_PROFILES,
          "Gruntspud currently has no connection profiles\n"
              + "configured. Before you can use any remote cvs\n"
              + "commands, you must set one connection profile\n"
              + "for each CVSROOT that you wish use. You can\n"
              + "find connection profiles in the preferences\n" + "dialog.",
          "No connection profiles", UIUtil
              .getCachedIcon(Constants.ICON_TOOL_LARGE_CONNECT));
      //  Listen for changes in where the tool bar is
    }
    addContainerListener(new ContainerAdapter() {
      public void componentAdded(ContainerEvent e) {
        if (Gruntspud.this.isShowing()) {
          //	Need to be on event dispatch thread as event order seems to have
          // changed??
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              Gruntspud.this.host.setProperty(Constants.TOOL_BAR_POSITION,
                  UIUtil.getToolBarPosition(buttons, Gruntspud.this));
            }
          });
        }
      }
    });
  }

  /**
   * DOCUMENT ME!
   * 
   * @param viz DOCUMENT ME!
   */
  public void setToolBarVisible(boolean viz) {
    buttons.setVisible(viz);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static GruntspudAuthenticator getAuthenticator() {
    return authenticator;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static URL getHomePage() {
    return gruntspudHomePage;
  }

  /**
   * DOCUMENT ME!
   */
  public void init() {
    //boolean wasFlatMode = context.getViewManager().isFlatMode();
    
    //
    context.getViewManager().reset();
    boolean showSelectiveText = context.getHost().getBooleanProperty(
        Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
    //  Tool bar
    String buttonsPosition = host.getProperty(Constants.TOOL_BAR_POSITION,
        BorderLayout.NORTH);
    buttons = new JToolBar("Gruntspud Tools", (buttonsPosition
        .equals(BorderLayout.NORTH) || buttonsPosition
        .equals(BorderLayout.SOUTH)) ? JToolBar.HORIZONTAL : JToolBar.VERTICAL);
    buttons.setFloatable(true);
    //buttons.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
    //
    boolean smallIcon = host.getBooleanProperty(Constants.TOOL_BAR_SMALL_ICONS,
        false);
    String actions = host.getProperty(Constants.TOOL_BAR_ACTIONS,
        Constants.TOOL_BAR_DEFAULT_ACTIONS);
    StringTokenizer t = new StringTokenizer(actions, ",");
    while (t.hasMoreTokens()) {
      String z = t.nextToken();
      if (z.equals(Constants.TOOL_BAR_SEPARATOR)) {
        buttons.add(new ToolBarSeparator());
      } else {
        Action a = getContext().getViewManager().getAction(z);
        if (a == null) {
          Constants.UI_LOG.warn("The action '" + z
              + "' that was on the toolbar "
              + "can no longer be found. This may be due to the removal "
              + "of a plugin or it is a core action that is no longer "
              + "valid.");
        } else {
          buttons.add(UIUtil.createButton(a, showSelectiveText, smallIcon));
        }
      }
    }
    //
    JPanel upper = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.weightx = 1.0;
    UIUtil.jGridBagAdd(upper, context.getViewManager()
        .getHomeLocationComponent(), gbc, GridBagConstraints.REMAINDER);
    gbc.insets = new Insets(2, 0, 0, 0);
    UIUtil.jGridBagAdd(upper, new JSeparator(JSeparator.HORIZONTAL), gbc,
        GridBagConstraints.REMAINDER);
    //
    JPanel main = new JPanel(new BorderLayout());
    main.add(upper, BorderLayout.NORTH);
    main.add(context.getViewManager(), BorderLayout.CENTER);
    //  file read only handler
    setHandlers();
    removeAll();
    setLayout(new BorderLayout());
    add(buttons, buttonsPosition);
    //  If this host has an accessory component, then add a split pane
    //  containing that main view and the accessory component
    JComponent accessory = host.getAccessoryComponent();
    if (accessory != null) {
      accessorySplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, main,
          accessory);
      accessorySplit.setResizeWeight(0.9d);
      accessorySplit.setDividerSize(9);
      accessorySplit.setOneTouchExpandable(true);
      if (host.getIntegerProperty(Constants.SPLIT_ACCESSORY_DIVIDER_LOCATION,
          -1) == -1) {
        accessorySplit.setDividerLocation(0.75d);
      } else {
        accessorySplit.setDividerLocation(host
            .getIntegerProperty(Constants.SPLIT_ACCESSORY_DIVIDER_LOCATION));
      }
      add(accessorySplit, BorderLayout.CENTER);
    }
    //  Otherwise just the normal view with split pane
    else {
      accessorySplit = null;
      add(main, BorderLayout.CENTER);
    }
    revalidate();
    //  Set the authenticator
    try {
      authenticator = new GruntspudAuthenticator(context);
      Authenticator.setDefault(authenticator);
      authenticator.init(context);
    } catch (IOException ioe) {
      GruntspudUtil.showErrorMessage(this, "Error", ioe);
    }
//    if(wasFlatMode != context.getViewManager().isFlatMode()) {
//        context.getViewManager().toggleFlatMode();
//    }
  }

  /**
   * @param f Description of the Parameter
   * @exception IOException Description of the Exception
   */
  public GruntspudContext getContext() {
    return context;
  }

  public void updateUI() {
    super.updateUI();
    if (buttons != null) buttons.updateUI();
  }

  /**
   * DOCUMENT ME!
   */
  public void cleanUp() {
    context.cleanUp();
    if (accessorySplit != null) {
      host.setIntegerProperty(Constants.SPLIT_ACCESSORY_DIVIDER_LOCATION,
          accessorySplit.getDividerLocation());
    }
  }

  private void setHandlers() {
    /* these OS names came from http://www.tolstoy.com/samizdat/sysprops.html */
    FileReadOnlyHandler handler = null;
    FileModeHandler modeHandler = null;
    if (!host.getBooleanProperty(
        Constants.OPTIONS_SYSTEM_DISABLE_NATIVE_PERMISSION_HANDLER, false)) {
      String osName = System.getProperty("os.name", "UNKNOWN");
      if (osName.toLowerCase().startsWith("windows")
          || osName.toLowerCase().startsWith("os/2")) {
        handler = new WindowsFileReadOnlyHandler();
        Constants.SYSTEM_LOG
            .info("Using ATTRIB command to set read only attribute on files.");
      } else if (osName.equalsIgnoreCase("linux")
          || osName.equalsIgnoreCase("solaris")
          || osName.equalsIgnoreCase("SunOS")
          || osName.equalsIgnoreCase("mpe/ix")
          || osName.equalsIgnoreCase("hp-ux") || osName.equalsIgnoreCase("aix")
          || osName.equalsIgnoreCase("freebsd")
          || osName.equalsIgnoreCase("irix")
          || osName.equalsIgnoreCase("digital unix") ||
          //    these two are a bit of a stab in the dark - need some feedback
          osName.equalsIgnoreCase("darwin")
          || osName.equalsIgnoreCase("mac os x")) {
        handler = new ChmodFileReadOnlyHandler();
        Constants.SYSTEM_LOG
            .info("Using chmod command to set file attributes.");
        modeHandler = new UnixFileModeHandler();
        //modeHandler = new NativeUnixFileModeHandler();
      }
      //  No handlers
      if (handler == null) {
        JOptionPane.showMessageDialog(this,
            "No command to use for this platform. Please submit a feature "
                + "request detailing the platform you use and the command "
                + "you would normally use to set a file to be read only.",
            "No read only command available", JOptionPane.WARNING_MESSAGE);
      } else {
        FileUtils.setFileReadOnlyHandler(handler);
        //  If there is a file mode handler to use, register it. This will be
        // used
        //  in preference to the read only handler
      }
      if (modeHandler != null) {
        GruntspudFileUtils.setFileModeHandler(modeHandler);
      }
    } else {
      Constants.SYSTEM_LOG.info("Using Java to set file permissions.");
    }
  }

  /**
   * Description of the Method
   * 
   * @param commandString Description of the Parameter
   */
  public void runCommand(String commandString) {
    Vector arguments = new Vector();
    boolean inDoubleQuote = false;
    boolean inSingleQuote = false;
    boolean escaped = false;
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < commandString.length(); i++) {
      char ch = commandString.charAt(i);
      if ((ch == '\\') && !escaped && !inSingleQuote) {
        escaped = true;
      } else if ((ch == '\'') && !inDoubleQuote && !escaped) {
        inSingleQuote = !inSingleQuote;
      } else if ((ch == '"') && !inDoubleQuote && !escaped) {
        inDoubleQuote = !inDoubleQuote;
      } else if ((ch == ' ') && !inDoubleQuote && !inSingleQuote && !escaped) {
        arguments.addElement(buf.toString());
        buf.setLength(0);
      } else {
        buf.append(ch);
      }
    }
    if (buf.length() != 0) {
      arguments.addElement(buf.toString());
    }
    String[] args = new String[arguments.size()];
    arguments.copyInto(args);
    runCommand(args);
  }

  /**
   * Description of the Method
   * 
   * @param arguments Description of the Parameter
   * @param node Description of the Parameter
   */
  private void cvs(String[] arguments, CVSFileNode node) {
  }

  /**
   * Description of the Method
   * 
   * @param arguments Description of the Parameter
   * @exception IllegalArgumentException Description of the Exception
   */
  private void cd(String[] arguments) throws IllegalArgumentException {
    CVSFileNode node = null;
    if (arguments.length == 0) {
      node = (CVSFileNode) tree.getModel().getRoot();
    } else if (arguments[0].equals("/")) {
      node = (CVSFileNode) tree.getModel().getRoot();
    } else if (arguments[0].equals("..")
        && (fileNodeTable.getRootNode().getParent() != null)) {
      node = (CVSFileNode) fileNodeTable.getRootNode().getParent();
    } else {
      CVSFileNode base = arguments[0].startsWith("/") ? (CVSFileNode) tree
          .getModel().getRoot() : fileNodeTable.getRootNode();
      StringTokenizer t = new StringTokenizer(arguments[0], "/");
      CVSFileNode current = base;
      boolean found = false;
      while (t.hasMoreTokens()) {
        String s = t.nextToken();
        found = false;
        if (s.length() != 0) {
          for (int i = 0; (i < current.getChildCount()) && !found; i++) {
            CVSFileNode z = (CVSFileNode) current.getChildAt(i);
            if (z.getName().equals(s)) {
              found = true;
              current = z;
            }
          }
          if (!found) {
            break;
          }
        }
      }
      if (found) {
        node = current;
      }
    }
    if (node == null) { throw new IllegalArgumentException("No such directory."); }
    context.getViewManager().setSelectedNode(node);
  }

  /**
   * Description of the Method
   * 
   * @param command Description of the Parameter
   */
  public void runCommand(String[] command) {
    try {
      if (command.length == 0) { throw new IllegalArgumentException(); }
      String cmd = command[0];
      String[] arguments = new String[command.length - 1];
      System.arraycopy(command, 1, arguments, 0, arguments.length);
      if (cmd.equalsIgnoreCase("cd")) {
        cd(arguments);
      } else if (cmd.equalsIgnoreCase("cvs")) {
        cvs(arguments, fileNodeTable.getRootNode());
      } else {
        throw new IllegalArgumentException("No such command.");
      }
    } catch (IllegalArgumentException ie) {
      context.getHost().showException(ie, "Command failed");
    }
  }

  /**
   * Description of the Method
   */
  private void openSelectedFile() {
    CVSFileNode sel = tree.getSelectedFileNode();
    if ((sel != null) && (sel.getFile() != null) && sel.isLeaf()) {
      context.openNode(sel, FileTypeMapping.OPEN_USING_DEFAULT);
    }
  }

  /**
   * Description of the Method
   * 
   * @param parent Description of the Parameter
   * @param modal Description of the Parameter
   * @param title Description of the Parameter
   * @return Description of the Return Value
   */
  public static JDialog createDialog(Component parent, boolean modal,
      String title) {
    JDialog d = null;
    Window w = SwingUtilities.getWindowAncestor(parent);
    if (parent == null) { return null; }
    if (w instanceof Dialog) {
      d = new JDialog((Dialog) w, title, modal);
    } else if (w instanceof Frame) {
      d = new JDialog((Frame) w, title, modal);
    } else {
      d = new JDialog((Frame) null, title, modal);
      ;
    }
    return d;
  }

  /**
   * Return if this is a 1.4+ runtime
   * 
   * @return is a 1.4+ runtime
   */
  public static boolean is14Plus() {
    return javaVersion >= 1.4f;
  }

  /**
   * Get the major.minor version of the Java runtime in use
   * 
   * @return is a 1.4+ runtime
   */
  public static float getMajorMinorJavaVersion() {
    return javaVersion;
  }

  //  Statics
  //  Supporting classes
  class StatusLabel extends JLabel {
    private int width;

    /**
     * Constructor for the StatusLabel object
     * 
     * @param width Description of the Parameter
     */
    public StatusLabel(int width) {
      super(" ");
      setBorder(BorderFactory.createLoweredBevelBorder());
      this.width = width;
    }

    public Dimension getPreferredSize() {
      FontMetrics fm = javax.swing.text.StyleContext.getDefaultStyleContext()
          .getFontMetrics(getFont());
      return new Dimension((fm.stringWidth("W") * width) + 4, super
          .getPreferredSize().height);
    }
  }

  class CVSFileNodeWrapper implements Comparable {
    CVSFileNode node;

    /**
     * Constructor for the CVSFileNodeWrapper object
     * 
     * @param node Description of the Parameter
     */
    CVSFileNodeWrapper(CVSFileNode node) {
      this.node = node;
    }

    public int compareTo(Object o) {
      CVSFileNode other = ((CVSFileNodeWrapper) o).node;
      int i = node.getFile().getParent().compareTo(other.getFile().getParent());
      if (i == 0) {
        i = node.getCVSSubstType().getName().compareTo(
            other.getCVSSubstType().getName());
        if (i == 0) {
          return node.getName().compareTo(other.getName());
        } else {
          return i;
        }
      } else {
        return i;
      }
    }
  }

  //    class BasicListener extends CVSAdapter
  //    {
  //        private final StringBuffer taggedLine = new StringBuffer();
  //
  //        public void messageSent(MessageEvent evt)
  //        {
  //            String line = evt.getMessage();
  //            Color c = evt.isError() ? Color.red :
  //                    Color.blue.darker();
  //            if (evt.isTagged())
  //            {
  //                String message = evt.parseTaggedMessage(taggedLine, evt.getMessage());
  //                if (message != null)
  //                {
  //                    host.writeToConsole(c, message);
  //                }
  //            }
  //            else
  //            {
  //                host.writeToConsole(c, line);
  //            }
  //        }
  //    }
  class GruntspudConsoleAppender extends org.apache.log4j.AppenderSkeleton {
    public boolean requiresLayout() {
      return false;
    }

    public void close() {
    }

    public void append(org.apache.log4j.spi.LoggingEvent le) {
      if (context != null) {
        host
            .writeToConsole(context.getTextStyleModel().getStyle(
                Constants.OPTIONS_STYLE_GRUNTSPUD), String.valueOf(le
                .getMessage()));
      }
    }
  }
}