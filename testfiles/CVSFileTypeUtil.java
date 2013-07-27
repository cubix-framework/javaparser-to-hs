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

package gruntspud;

import gruntspud.file.DirectoryStatus;
import gruntspud.ui.UIUtil;
import gruntspud.ui.icons.OverlayIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

import org.netbeans.lib.cvsclient.file.FileStatus;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileTypeUtil {
    //
    private static Method fsIconMethod;

    //
    private static Method fsTypeDescriptionMethod;
    
    /**
     * Get the substiution type given a line endings type
     * 
     * @param lineEndings
     * @return subst. type
     */
    public static CVSSubstType getSubstTypeForLineEndings(int lineEndings) {
    	switch(lineEndings) {
    		case CVSFileNode.BINARY:
    			return CVSSubstType.CVS_SUBST_TYPE_BINARY;
			case CVSFileNode.UNIX_LINE_ENDINGS:
			case CVSFileNode.WINDOWS_LINE_ENDINGS:
			case CVSFileNode.UNKNOWN_LINE_ENDINGS:
				return CVSSubstType.CVS_SUBST_TYPE_TEXT;
			case CVSFileNode.DIRECTORY:
				return CVSSubstType.CVS_SUBST_TYPE_DIRECTORY;
			default:
				return null;
    	}
    }

    /**
     *  Description of the Method
     *
     *@param  file  Description of the Parameter
     *@return       Description of the Return Value
     */
    public static int getLineEndings(File file) {
        if (file.isDirectory()) {
            return CVSFileNode.DIRECTORY;
        }

        FileInputStream in = null;
        
        int lineEndings = CVSFileNode.UNKNOWN_LINE_ENDINGS;

        try {
            in = new FileInputStream(file);

            byte[] buf = new byte[1024];
            int z = in.read(buf);
            int text = 0;
            int win = 0;
            int unix = 0;
            byte l = 0;

            for (int i = 0; i < z; i++) {
                /** @todo is more required here ??????? */
                if ( (buf[i] == 9) || (buf[i] == 26) || (buf[i] == 10) ||
                    (buf[i] == 13) || ( buf[i] >= 32 && buf[i] != 127) ) {
                    if(l == 13 && buf[i] == 10) {
						win++;
                    }
                    else if(l != 13 && buf[i] == 10) {
                    	unix++;
                    }
                    text++;
                }
                l = buf[i];
            }

            if (text == z) {
				lineEndings = win > unix ? CVSFileNode.WINDOWS_LINE_ENDINGS :
                	( unix > win ? CVSFileNode.UNIX_LINE_ENDINGS : 
                		CVSFileNode.UNKNOWN_LINE_ENDINGS);
            }
            else {
                lineEndings = CVSFileNode.BINARY;
			}
        }
        catch (IOException ioe) {
        	Constants.IO_LOG.error(ioe);
        }
        finally {
            GruntspudUtil.closeStream(in);
        }
        
        return lineEndings;
    }

    /**
     * DOCUMENT ME!
     *
     * @param status DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Icon getIconForStatus(FileStatus status) {
        return getIconForStatus(status, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param status DOCUMENT ME!
     * @param base DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Icon getIconForStatus(FileStatus status, Icon base) {
        Icon icon = null;

        if (base == null) {
            base = UIManager.getIcon("Tree.leafIcon");
        }
        icon = new OverlayIcon(getStatusOverlayIcon(status, base),  base,
            SwingConstants.CENTER);
        return icon;
    }
    
    public static Icon getStatusOverlayIcon(FileStatus status, Icon defaultIcon) {
      if (status == FileStatus.HAS_CONFLICTS) {
          return UIUtil.getCachedIcon(
              Constants.ICON_STATUS_CONFLICTS_OVERLAY);
      }
      else if (status == FileStatus.NEEDS_CHECKOUT) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_NEEDS_CHECKOUT_OVERLAY);
      }
      else if (status == FileStatus.NEEDS_MERGE) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_NEEDS_MERGE_OVERLAY);
      }
      else if (status == FileStatus.NEEDS_PATCH) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_NEEDS_PATCH_OVERLAY);
      }
      else if (status == FileStatus.ADDED) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_ADDED_OVERLAY);
      }
      else if (status == FileStatus.REMOVED) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_REMOVED_OVERLAY);
      }
      else if (status == FileStatus.UP_TO_DATE) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_IN_CVS_OVERLAY);
      }
      else if (status == null) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_ERASED_OVERLAY);
      }
      else if (status == FileStatus.MODIFIED) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_OUT_OF_DATE_OVERLAY);
      }
      else if (status == FileStatus.UNKNOWN) {
          return defaultIcon;
      }
      else if (status == null) {
        return UIUtil.getCachedIcon(
            Constants.ICON_STATUS_ERASED_OVERLAY);
      }
      return null;
      
    }

    /**
     * DOCUMENT ME!
     *
     * @param status DOCUMENT ME!
     * @param base DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Icon getIconForStatus(DirectoryStatus status) {
        Icon base = UIUtil.getCachedIcon(
            Constants.ICON_TOOL_SMALL_DEFAULT_FOLDER_CLOSED);
        Icon icon = null;

        if (status == DirectoryStatus.PROJECT) {
            icon = new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_PROJECT_OVERLAY), base,
                                   SwingConstants.CENTER);
        }
        else if (status == DirectoryStatus.MODULE) {
            icon = new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_MODULE_OVERLAY), base,
                                   SwingConstants.CENTER);
        }
        else if (status == DirectoryStatus.ATTENTION) {
            icon = new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_NEEDS_ATTENTION), base,
                                   SwingConstants.CENTER);
        }
        else if (status == DirectoryStatus.UP_TO_DATE) {
            icon = new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_IN_CVS_OVERLAY), base,
                                   SwingConstants.CENTER);
        }
        else  {
            icon = base;
        }
        return icon;
    }


    /**
     * Only for 1.4
     * @param expanded
     * @return
     */
    public static Icon getFileSystemIcon(File file) {
        if (Gruntspud.is14Plus()) {
            try {
                Method m = FileSystemView.class.getMethod("getSystemIcon",
                    new Class[] {File.class});

                if (m != null) {
                    return (Icon) m.invoke(FileSystemView.getFileSystemView(),
                                           new Object[] {file});
                }
            }
            catch (Exception ioe) {
                ioe.printStackTrace();
            }
        }

        return UIManager.getIcon("Tree.leafIcon");
    }

    /**
     * Only for 1.4
     * @param expanded
     * @return
     */
    public static String getFileSystemTypeDescriptionName(File file) {
        String s = null;

        if (file.isDirectory()) {
            s = "File Folder";
        }
        else if (!file.exists()) {
            s = "Missing!";
        }
        else {
            if (Gruntspud.is14Plus()) {
                try {
                    Method m = FileSystemView.class.getMethod(
                        "getSystemTypeDescription",
                        new Class[] {File.class});

                    if (m != null) {
                        s = (String) m.invoke(FileSystemView.getFileSystemView(),
                                              new Object[] {file});
                    }
                }
                catch (Exception ioe) {
                }
            }
        }

        if (s == null) {
            int idx = file.getName().lastIndexOf('.');

            if (idx != -1) {
                s = file.getName().substring(idx + 1).toUpperCase();
            }
        }

        return s;
    }

    public static String createToolTipTextForNode(CVSFileNode node) {
      StringBuffer buf = new StringBuffer();
      buf.append("<html>");
      if(node.isNeedsAttention()) {
        buf.append("<h3><i>");
        buf.append("<img src=\"");
        buf.append(((ImageIcon)UIUtil.getCachedIcon(
            Constants.ICON_STATUS_NEEDS_ATTENTION)).getDescription());
        buf.append("\">");
        buf.append("Needs attention");
        buf.append("</i></h3>");
        
      }
      else if (node.isProjectRoot()) {
        buf.append("<h3><i>");
        buf.append("<img src=\"");
        buf.append(((ImageIcon)UIUtil.getCachedIcon(
            Constants.ICON_STATUS_PROJECT_OVERLAY)).getDescription());
        buf.append("\">");
        buf.append("Project");
        buf.append("</i></h3>");
      }
      else if (node.isModuleRoot()) {
        buf.append("<h3><i>");
        buf.append("<img src=\"");
        buf.append(((ImageIcon)UIUtil.getCachedIcon(
            Constants.ICON_STATUS_MODULE_OVERLAY)).getDescription());
        buf.append("\">");
        buf.append("Module Root");
        buf.append("</i></h3>");
      }
      else {
        FileStatus status = node.getOverallStatus();
        if(status != null) {
          Icon icon = getStatusOverlayIcon(node.getOverallStatus(), null);
          buf.append("<h3><i>");        
          if(icon != null) {
            buf.append("<img src=\"");
            buf.append(((ImageIcon)icon).getDescription());
            buf.append("\">");
          }
          buf.append(status.toString());
          buf.append("</i></h3>");
        }
        else {
          buf.append("<h3><i><b>");
            buf.append("<img src=\"");
            buf.append(((ImageIcon)UIUtil.getCachedIcon(
                Constants.ICON_STATUS_ERASED_OVERLAY)).getDescription());
            buf.append("\">");
          buf.append("<font color=\"red\">Missing!</font>");
          buf.append("</b></i></h3>");
          
        }
      }
      buf.append("<p>");
      buf.append(node.getFile().getAbsolutePath());
      buf.append("</p>");
      return buf.toString();
    }
}