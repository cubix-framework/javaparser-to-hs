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
import gruntspud.filter.CVSFileFilter;
import gruntspud.project.Project;
import gruntspud.style.TextStyle;
import gruntspud.ui.UIUtil;
import gruntspud.ui.icons.OverlayIcon;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.SwingConstants;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.netbeans.lib.cvsclient.admin.DateComparator;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.file.FileStatus;
import org.netbeans.lib.cvsclient.util.IgnoreFileFilter;

/**
 *  This holds everything about a single node in the file heirarchy such if it is in CVS (or not),
 *  all of its children (if any), its status, if it has a file that exists associated with it etc etc.
 *  If the node can have children, the list is not actually loaded until a client requests for the
 *  number of children available. This prevents the entire directory structure from being loaded upon
 *  creation of the root node, nodes are only loaded when the parent is expanded by the user.
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class CVSFileNode
    implements MutableTreeNode, Comparable {
    public final static int SORT_ON_STATUS = 0;
    public final static int SORT_ON_KEYWORD_SUBST = 1;
    public final static int SORT_ON_NAME = 2;
    public final static int SORT_ON_TYPE = 3;
    public final static int SORT_ON_REVISION = 4;
    public final static int SORT_ON_DATE = 5;
    public final static int SORT_ON_FLAGS = 6;
    public final static int SORT_ON_SIZE = 7;
    public final static int SORT_ON_LOCAL_STATUS = 8;
    public final static int SORT_ON_REMOTE_STATUS = 9;
    public final static int SORT_ON_TAG = 10;
    
    public final static int LINE_ENDINGS_UNCHECKED = -1;
	public final static int WINDOWS_LINE_ENDINGS = 0;
	public final static int UNIX_LINE_ENDINGS = 1;
	public final static int UNKNOWN_LINE_ENDINGS = 2;
	public final static int BINARY = 3;
	public final static int DIRECTORY = 4;
    

    //  Private instance variables
    private TextStyle style;
    private Project project;
    private FileStatus remoteStatus;
    private DirectoryStatus directoryStatus;
    private boolean directoryStatusChecked;
    private boolean needsAttention;
    private boolean selected, canPathChecked, expandedIcon, styleChecked;
    private String canPath;
    private boolean openChecked, open;
    private File file;
    private Vector children;
    private Vector allChildren;
    private CVSRoot cvsRoot;
    private Entry entry;
    private IgnoreFileFilter ignoreFilter;
    private int lineEndings;
    private CVSSubstType cvsSubstType;
    private boolean cvsSubstTypeChecked;
    private boolean cvsRootChecked;
    private MutableTreeNode parent;
    private Object userObject;
    private GruntspudContext context;
    private boolean module;
    private boolean moduleChecked;
    private boolean projectRoot;
    private Icon icon;
    private SortCriteria sortCriteria;
    private boolean includeAllFiles;
    private String fileTypeText;
    private HashMap formattedText;
    private boolean copy;

    /**
     *  Constructor for the CVSFileNode object
     *
     *@param  host             application hosting gruntspud
     *@param  filter           file filter
     *@param  entry            the CVS entry (if any) associated with this node
     *@param  file             t
     */
    public CVSFileNode(GruntspudContext context, SortCriteria sortCriteria,
                       Entry entry, File file, CVSFileNode parent) {
        this.context = context;

        formattedText = new HashMap();

        //
        selected = true;
        children = null;
        allChildren = null;

        //
        this.sortCriteria = sortCriteria;
        this.entry = entry;
        this.file = file;
        copy = file.getName().startsWith(".#");
        setParent(parent);
        context.getViewManager().cacheFileNode(this);

        initialize();

    }

    public Project getProject() {
        return project;
    }

    //  This should be changed to a Comparator
    public static int compare(CVSFileNode t, CVSFileNode n,
                              SortCriteria sortCriteria) {
        int s = 0;

        if (sortCriteria.isFoldersFirst() && t.isLeaf() && !n.isLeaf()) {
            s = 1;
        }
        else if (sortCriteria.isFoldersFirst() && n.isLeaf() && !t.isLeaf()) {
            s = -1;
        }
        else {
            switch (sortCriteria.getSortType()) {
                case CVSFileNode.SORT_ON_STATUS:
                    s = new FileStatusWrapper(
                        t.getOverallStatus()).compareTo(new FileStatusWrapper(
                        n.getOverallStatus()));
                    break;
                case CVSFileNode.SORT_ON_KEYWORD_SUBST:
                    if ( (t.getEntry() == null) && (n.getEntry() != null)) {
                        s = -1;
                    }
                    else if ( (t.getEntry() != null) && (n.getEntry() == null)) {
                        s = 1;
                    }
                    else if ( (t.getEntry() == null) && (n.getEntry() == null)) {
                        s = 0;
                    }
                    else {
                        s = t.getCVSSubstType().compareTo(n.getCVSSubstType());
                    }
                    break;
                case CVSFileNode.SORT_ON_NAME:
                	String f1 = null;
                	String f2 = null;
                	if(t.getName().startsWith(".#")) {
                		f1 = t.getFile().getParentFile().getAbsolutePath() +
							File.separator + t.getFile().getName().substring(2);
                	}
                	else {
                		f1 = t.getFile().getAbsolutePath();                		
                	}
                	if(n.getName().startsWith(".#")) {
                		f2 = n.getFile().getParentFile().getAbsolutePath() +
						File.separator + n.getFile().getName().substring(2);
                	}
                	else {
                		f2 = n.getFile().getAbsolutePath();                		
                	}
                    if (sortCriteria.isCaseSensitive()) {
                    	s = f1.compareTo(f2);
                    }
                    else {
                    	s = f1.compareToIgnoreCase(f2);
                    }
                    break;
                case CVSFileNode.SORT_ON_TYPE:
                    s = t.getFileTypeText().compareTo(n.getFileTypeText());

                    break;
                case CVSFileNode.SORT_ON_REVISION:
                    s = new Long(getLongRevision(t.getRevision())).compareTo(new
                        Long(
                        getLongRevision(n.getRevision())));

                    break;
                case CVSFileNode.SORT_ON_DATE:

                    Date d1 = (t.getEntry() == null) ? null
                        : t.getEntry()
                        .getLastModified();
                    Date d2 = (n.getEntry() == null) ? null
                        : n.getEntry()
                        .getLastModified();

                    if ( (d1 == null) && (d2 == null)) {
                        s = 0;
                    }
                    else if ( (d1 == null) && (d2 != null)) {
                        s = -1;
                    }
                    else if ( (d1 != null) && (d2 == null)) {
                        s = 1;
                    }
                    else {
                        s = d1.compareTo(d2);

                    }
                    break;
                case CVSFileNode.SORT_ON_FLAGS:
                    s = t.getFlags().compareTo(n.getFlags());

                    break;
                case CVSFileNode.SORT_ON_TAG:
                    if ( (t.getTag() == null) && (n.getTag() != null)) {
                        s = -1;
                    }
                    else if ( (t.getTag() != null) && (n.getTag() == null)) {
                        s = 1;
                    }
                    else if ( (t.getTag() == null) && (n.getTag() == null)) {
                        s = 0;
                    }
                    else {
                        s = t.getTag().compareTo(n.getTag());
                    }
                    break;
                case CVSFileNode.SORT_ON_LOCAL_STATUS:
                    s = new FileStatusWrapper(
                        t.getLocalStatus()).compareTo(new FileStatusWrapper(n.
                        getLocalStatus()));
                    break;
                case CVSFileNode.SORT_ON_REMOTE_STATUS:
                    s = new FileStatusWrapper(
                        t.getRemoteStatus()).compareTo(new FileStatusWrapper(n.
                        getRemoteStatus()));
                    break;
                case CVSFileNode.SORT_ON_SIZE:

                    if (n.isLeaf()) {
                        s = new Long(t.getFile().length()).compareTo(new Long(
                            n.getFile().length()));
                    }
                    else {
                        s = new Integer(n.getChildCount()).compareTo(new
                            Integer(
                            n.getChildCount()));

                    }
                    break;
            }

            //  Reverse the sort if required
            if (sortCriteria.getSortDirection() == SortCriteria.SORT_ASCENDING) {
                s = s * -1;
            }
        }

        return s;
    }

    public String getCanonicalPath() {
        if (!canPathChecked) {
            try {
                canPath = getFile().getCanonicalPath();
            }
            catch (IOException ioe) {
                Constants.UI_LOG.error("Could not get canonical path");
            }
            canPathChecked = true;
        }
        return canPath;
    }

    /**
     * DOCUMENT ME!
     *
     * @param revision DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static long getLongRevision(String revision) {
        String[] s1 = StringUtil.splitString(revision, '.');
        int j = 1;
        long tot = 0;

        try {
            for (int i = s1.length - 1; i >= 0; i--) {
                int z = Integer.parseInt(s1[i]);
                tot = tot + (z * j);
                j = j * 100000; //  surely there will never be a revision this large
            }
        }
        catch (NumberFormatException nfe) {
        }

        return tot;
    }

    /**
     * DOCUMENT ME!
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int compareTo(Object o) {
        return compare(this, ( (CVSFileNode) o), sortCriteria);
    }

    /**
     * Return the tag (may be blank)
     *
     * @return
     */
    public String getTag() {
        return (entry == null || entry.getTag() == null) ? "" : entry.getTag();
    }

    /**
     * Return the tag (may be blank)
     *
     * @return
     */
    public String getLocalStatusText() {
        return entry == null ? "" : (getLocalStatus() == null ?
                                     "Missing!" : getLocalStatus().toString());
    }

    /**
     * Return the tag (may be blank)
     *
     * @return
     */
    public String getOverallStatusText() {
        String s = getRemoteStatusText();
        return s.length() == 0 ? getLocalStatusText() : s;
    }

    /**
     * Return the tag (may be blank)
     *
     * @return
     */
    public String getRemoteStatusText() {
        return remoteStatus == null ? "Unknown" : remoteStatus.toString();
    }

    /**
     * Return the file type
     *
     * @return
     */
    public String getFileTypeText() {
        if (fileTypeText == null) {
            fileTypeText = CVSFileTypeUtil.getFileSystemTypeDescriptionName(
                getFile());

        }
        return (fileTypeText == null) ? "" : fileTypeText;
    }

    /**
     * Return the revision text (may be blank)
     *
     * @return revision text
     */
    public String getFlags() {
        return ( (entry == null) || (entry.getOptions() == null)) ? ""
            : entry.getOptions();
    }

    /**
     * Return the revision text (may be blank)
     *
     * @return revision text
     */
    public String getRevision() {
        return (entry == null) ? "" : entry.getRevision();
    }

    /**
     *  Does this node allow children (i.e. is it a directory)
     *
     *@return    The allowsChildren value
     */
    public boolean getAllowsChildren() {
        return (entry == null) ? file.isDirectory() : entry.isDirectory();
    }

    /**
     *  Give the index in the list of children for this for a specified node
     *
     *@param  node  the node to find
     *@return       index of node in child list
     */
    public int getIndex(TreeNode node) {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }
        return (children == null) ? ( -1) : children.indexOf(node);
    }

    /**
     *  Return an enumeration of all the children in this node (if any).
     *
     *@return    enumeration of children
     */
    public Enumeration children() {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }
        return children.elements();
    }

    /**
     *  Add a new child node
     */
    public void addChild(CVSFileNode node) {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }
        children.add(node);
    }

    /**
     *  Adds <code>child</code> to the receiver at <code>index</code>. <code>child</code>
     *  will be messaged with <code>setParent</code>.
     *
     *@param  child  Description of the Parameter
     *@param  index  Description of the Parameter
     */
    public void insert(MutableTreeNode child, int index) {
        if (children == null) {
            children = new Vector();

        }
        children.insertElementAt(child, index);
    }

    /**
     *  Removes the child at <code>index</code> from the receiver.
     *
     *@param  index  Description of the Parameter
     */
    public void remove(int index) {
        CVSFileNode n = (CVSFileNode)getChildAt(index);
        if(n != null) {
            children.remove(n);
            allChildren.remove(n);
        }
    }

    /**
         *  Removes <code>node</code> from the receiver. <code>setParent</code> will
     *  be messaged on <code>node</code>.
     *
     *@param  node  Description of the Parameter
     */
    public void remove(MutableTreeNode node) {
        children.remove(node);
        allChildren.remove(node);
    }

    /**
     *  Resets the user object of the receiver to <code>object</code>.
     *
     *@param  object  The new userObject value
     */
    public void setUserObject(Object object) {
        this.userObject = userObject;
    }

    /**
     *  Removes the receiver from its parent.
     */
    public void removeFromParent() {
        if (getParent() != null) {
            ( (CVSFileNode) getParent()).remove(this);
        }
    }

    /**
     *  Sets the parent of the receiver to <code>newParent</code>.
     *
     *@param  parent  The new parent value
     */
    public void setParent(MutableTreeNode parent) {
        this.parent = parent;
    }

    /**
     *  Sets the cVSFileType attribute of the CVSFileNode object
     *
     *@param  cvsFileType  The new cVSFileType value
     */
    public void setCVSSubstType(CVSSubstType cvsFileType) {
        this.cvsSubstType = cvsSubstType;
        cvsSubstTypeChecked = true;
    }

    /**
     *  Gets the cVSFileType attribute of the CVSFileNode object
     *
     *@return    The cVSFileType value
     */
    public CVSSubstType getCVSSubstType() {
        if (!cvsSubstTypeChecked) {
            if(isCopy()) {
                cvsSubstType = CVSSubstType.CVS_SUBST_TYPE_COPY;
            }
            else if (getParent() != null) {
                CVSFileNode p = (CVSFileNode) getParent();

                if ( (p.getIgnoreFilter() != null) && (p.getFile() != null)) {
                    boolean ignore = p.getIgnoreFilter().shouldBeIgnored(p.
                        getFile(),
                        getName());

                    if (ignore) {
                        cvsSubstType = CVSSubstType.CVS_SUBST_TYPE_IGNORED;
                    }
                }
            }

            if ( (cvsSubstType == null) && (entry != null)) {
                if (isLeaf()) {
                    if (entry.getOptions() == null) {
                        cvsSubstType = CVSSubstType.CVS_SUBST_TYPE_TEXT;
                    }
                    else {
                        cvsSubstType = CVSSubstType.getSubstTypeForString(entry.
                            getOptions());
                    }
                }
                else {
                    cvsSubstType = CVSSubstType.CVS_SUBST_TYPE_DIRECTORY;
                }
            }

            cvsSubstTypeChecked = true;
        }

        return cvsSubstType;
    }

    /**
     *  Description of the Method
     */
    public synchronized void reset() {
        if(file != null) {
            try {
//                if(entry != null && cvsRoot != null) {
//                    String dir = CVSUtil.getRepositoryForDirectory(file.getParentFile());
//                    Constants.CVS_LOG.debug("Updating admin data for " + entry.getName() + ". Parent is " +
//                        file.getParent() + ", Repository=" + cvsRoot.getRepository() + " Repository For Directory =" + dir);
//                    CVSUtil.getHandler().updateAdminData(
//                        file.getParent(), dir, entry, CVSUtil.getGlobalOptions(context, null));
//                }
                entry = CVSUtil.getHandler().getEntry(file);

            }
            catch(IOException ioe) {
                Constants.IO_LOG.error("Could not get CVS entry for file " + file.getAbsolutePath(), ioe);
            }
        }
        initialize();
    }

    private void initialize() {
        if(children != null) {
            context.getViewManager().removeCachedChildNodes(this);
        }
        style = null;
        icon = null;
        project = null;
        //remoteStatus = null;
        needsAttention = false;
        selected  = false;
        canPathChecked = false;
        expandedIcon = false;
        styleChecked = false;
        canPath = null;
        openChecked = false;
        open = false;
        children = null;
        allChildren = null;
        cvsRoot = null;
        ignoreFilter = null;
        cvsSubstType = null;
        cvsSubstTypeChecked = false;
        cvsRootChecked = false;
        module = false;
        moduleChecked = false;
        lineEndings = context.getHost().getBooleanProperty(
        	Constants.OPTIONS_DISPLAY_SHOW_LINE_ENDINGS, false) ?
        		 LINE_ENDINGS_UNCHECKED : UNKNOWN_LINE_ENDINGS;
        directoryStatus = null;
        directoryStatusChecked = false;
        fileTypeText = null;
        formattedText.clear();
        project = null;
        projectRoot = false;
        if(parent != null) {
            project = ((CVSFileNode)parent).getProject();
        }
        if(project == null) {
            project = context.getProjectListModel().getProjectForFile(file);
        }
        try {
            if(project != null)
                projectRoot = project.getHome().getCanonicalFile().equals(file.getCanonicalFile());
        }
        catch(IOException ioe) {
            Constants.SYSTEM_LOG.error("Could not determine if " + file.getAbsolutePath() + " is the project root.", ioe);
        }
        if(project != null && context.getViewManager().getProject() != null && context.getViewManager().getProject() == project) {
            filterAndSortChildren();
        }
    }

    public boolean isOpen() {
        if (!openChecked) {
            open = context.getHost().isNodeOpenedInEditor(this);
            openChecked = true;
        }
        return open;
    }
    
    public int getLineEndings() {
    	if(lineEndings == LINE_ENDINGS_UNCHECKED)  {
    		lineEndings = CVSFileTypeUtil.getLineEndings(getFile());
    	}
    	return lineEndings;
    }    

    /**
     *  Description of the Method
     */
    public void loadIgnoreFilter() {
        if (!includeAllFiles && (ignoreFilter == null) && (file != null) &&
            file.isDirectory()) {
            ignoreFilter = CVSUtil.getCompleteIgnoreFileFilter(context, file);
        }
    }

    /**
     *  Gets the ignored attribute of the CVSFileNode object
     *
     *@return    The ignored value
     */
    public boolean isIgnored() {
        return CVSSubstType.CVS_SUBST_TYPE_IGNORED == getCVSSubstType();
    }

    /**
     *  Gets the ignoreFilter attribute of the CVSFileNode object
     *
     *@return    The ignoreFilter value
     */
    public IgnoreFileFilter getIgnoreFilter() {
        if (ignoreFilter == null) {
            loadIgnoreFilter();

        }
        return ignoreFilter;
    }

    /**
     *  Gets the foreground attribute of the CVSFileNode object
     *
     *@return    The foreground value
     */
    public Color getForeground() {
        return ( (getEntry() != null) && !getFile().canRead()) ? Color.red : null;
    }

    public TextStyle getStyle() {
        if (!styleChecked) {
            if(getEntry() == null && !getFile().canRead()) {
                style = context.getTextStyleModel().getStyle("file.Unreadable");
            }
            else {
                if(!isLeaf()) {
                    if(isNeedsAttention()) {
                        style = context.getTextStyleModel().getStyle("folder.RequiresAttention");
                    }
                    else if(isProjectRoot()) {
                        style = context.getTextStyleModel().getStyle("folder.Project");
                    }
                    else if(isModuleRoot()) {
                        style = context.getTextStyleModel().getStyle("folder.Module");
                    }
                    else {
                        style = context.getTextStyleModel().getStyle("folder.Plain");
                    }
                }
            }
            if(style == null)
                style = context.getHost().getNodeStyle(this);
            styleChecked = true;
        }
        return style;
    }

    /**
     *  Gets the icon attribute of the CVSFileNode object
     *
     *@param  expanded  Description of the Parameter
     *@return           The icon value
     */
    public Icon getIcon(boolean expanded) {
        if (expanded != expandedIcon) {
            icon = null;
        }
        if (icon == null) {
            icon = getIconForStatus(getOverallStatus(), getBaseIcon(expanded));
            expandedIcon = expanded;
        }

        //  Unknown
        return icon;
    }

    public Icon getIconForStatus(FileStatus status, Icon base) {
    	
        if(isNeedsAttention()) {
            return new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_NEEDS_ATTENTION), base,
                                   SwingConstants.CENTER);
        }
        else if (isProjectRoot()) {
            return new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_PROJECT_OVERLAY), base,
                                   SwingConstants.CENTER);
        }
        else if (isModuleRoot()) {
            return new OverlayIcon(UIUtil.getCachedIcon(
                Constants.ICON_STATUS_MODULE_OVERLAY), base,
                                   SwingConstants.CENTER);
        }
        else {
            return CVSFileTypeUtil.getIconForStatus(status, base);
        }

    }

    public Icon getBaseIcon(boolean expanded) {
        boolean useSystemIcons = context.getHost().getBooleanProperty(Constants.
            OPTIONS_DISPLAY_USE_SYSTEM_ICONS,
            true);
        if ( (file != null) && file.exists()) {
            return file.isDirectory()
                ?
                (expanded ?
                 context.getHost().getIcon(Constants.
                                           ICON_TOOL_SMALL_DEFAULT_FOLDER_OPEN)
                 :
                 context.getHost().getIcon(Constants.
                ICON_TOOL_SMALL_DEFAULT_FOLDER_CLOSED))
                : (useSystemIcons ? CVSFileTypeUtil.getFileSystemIcon(file)
                   : (isOpen() ? context.getHost().getIcon(
                Constants.ICON_TOOL_SMALL_DEFAULT_LEAF_OPEN) :
                      context.getHost().getIcon(Constants.
                ICON_TOOL_SMALL_DEFAULT_LEAF)));
        }
        else {
            return isLeaf() ? (isOpen() ? context.getHost().getIcon(
                Constants.ICON_TOOL_SMALL_DEFAULT_LEAF_OPEN) :
                               context.getHost().getIcon(Constants.
                ICON_TOOL_SMALL_DEFAULT_LEAF))
                : (expanded
                   ?
                   context.getHost().getIcon(Constants.
                ICON_TOOL_SMALL_DEFAULT_FOLDER_OPEN)
                   :
                   context.getHost().getIcon(Constants.
                ICON_TOOL_SMALL_DEFAULT_FOLDER_CLOSED));
        }

    }

    /**
     *  Gets the binary attribute of the CVSFileNode object
     *
     *@return    The binary value
     */
    public boolean isBinary() {
        return CVSSubstType.CVS_SUBST_TYPE_BINARY == getCVSSubstType();
    }

    /**
     *  Gets the text attribute of the CVSFileNode object
     *
     *@return    The text value
     */
    public boolean isText() {
        return CVSSubstType.CVS_SUBST_TYPE_IGNORED == getCVSSubstType();
    }

    /**
     *  Gets the unicode attribute of the CVSFileNode object
     *
     *@return    The unicode value
     */
    public boolean isUnicode() {
        return CVSSubstType.CVS_SUBST_TYPE_IGNORED == getCVSSubstType();
    }

    /**
     *  Gets the name attribute of the CVSFileNode object
     *
     *@return    The name value
     */
    public String getName() {
        return (file == null) ? entry.getName() : file.getName();
    }

    /**
     *  Gets the name attribute of the CVSFileNode object
     *
     *@return    The name value
     */
    public String getFormattedText(String format) {
        //  If no formatting
        if ( (format == null) || !isLeaf()) {
            return getName();
        }

        //  Otherwise parse the pattern
        String ft = (String)formattedText.get(format);
        if(ft == null) {
            int l = format.length();
            StringBuffer buf = new StringBuffer(20);
            char ch = ' ';

            for (int i = 0; i < l; i++) {
                ch = format.charAt(i);

                if ( (ch == '%') && ( (i + 1) < l)) {
                    i++;
                    ch = format.charAt(i);

                    switch (ch) {
                        case 'n':
                            buf.append(getName());

                            break;
                        case 'r':
                            buf.append(getRevision());

                            break;
                        case 's':
                            buf.append(getOverallStatusText());
                            break;
                        case 'l':
                            buf.append(getLocalStatusText());
                            break;
                        case 'R':
                            buf.append(getRemoteStatusText());
                            break;
                        case 't':
                            buf.append(getTag());
                            break;
                        case 'f':
                            buf.append(getFlags());
                            break;
                        case 'P':
                            buf.append("..");

                            CVSFileNode n = context.getViewManager().getCWDNode();

                            if (n != null) {
                                String nf = n.getFile().getAbsolutePath();
                                String f = getFile().getAbsolutePath();

                                if (nf.length() < f.length()) {
                                    buf.append(f.substring(nf.length()));
                                }
                                else {
                                    buf.append(f);
                                }
                            }

                            break;
                        case 'p':
                            buf.append(getFile().getAbsolutePath());

                            break;
                        default:
                            buf.append(ch);
                    }
                }
                else {
                    buf.append(ch);
                }
            }

            ft = buf.toString();
            formattedText.put(format, ft);
        }

        return ft;
    }

    /**
     *  Gets the entry attribute of the CVSFileNode object
     *
     *@return    The entry value
     */
    public Entry getEntry() {
        return entry;
    }

    /**
     *  Gets the parent attribute of the CVSFileNode object
     *
     *@return    The parent value
     */
    public TreeNode getParent() {
        return parent;
    }

    /**
     *  Gets the cVSRoot attribute of the CVSFileNode object
     *
     *@return    The cVSRoot value
     */
    public CVSRoot getCVSRoot() {
        if (!cvsRootChecked) {
            if (getFile() != null) {
            	
                cvsRoot = CVSUtil.getCVSRoot(getFile(), context);

                if ( (cvsRoot == null) && (getParent() != null)) {
                    cvsRoot = ( (CVSFileNode) getParent()).getCVSRoot();
                }
            }
            else if (getParent() != null) {
                cvsRoot = ( (CVSFileNode) getParent()).getCVSRoot();

            }
            cvsRootChecked = true;
        }

        return cvsRoot;
    }

    /**
     *  Gets the moduleRoot attribute of the CVSFileNode object
     *
     *@return    The moduleRoot value
     */
    public boolean isProjectRoot() {
        return projectRoot;
    }


    /**
     *  Gets the moduleRoot attribute of the CVSFileNode object
     *
     *@return    The moduleRoot value
     */
    public boolean isModuleRoot() {
        if (!moduleChecked) {
            if (getFile().isFile() || !getFile().exists()) {
                module = false;
            }
            else {
                String repository = CVSUtil.getRepositoryForDirectory(getFile());

//          May be checked-out under a different name
//            module = (repository != null) &&
//                repository.equals(getFile().getName());

                module = (repository != null) &&
                    repository.length() > 0 &&
                    repository.indexOf('/') == -1;
            }

            moduleChecked = true;
        }

        return module;
    }

    /**
     *  Gets the remote status
     *
     *@return remote status
     */
    public FileStatus getRemoteStatus() {
        return remoteStatus;
    }

    /**
     *  Sets the remote status
     *
     *@return remoteStatus remote status
     */
    public void setRemoteStatus(FileStatus remoteStatus) {
        this.remoteStatus = remoteStatus;
    }

    /**
     *  Gets the entry attribute of the CVSFileNode object
     *
     *@param  name             Description of the Parameter
     *@return                  The entry value
     *@exception  IOException  Description of the Exception
     */
    public Entry getEntry(String name) throws IOException {
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            CVSFileNode n = (CVSFileNode) i.next();

            if ( (n.getEntry() != null) && n.getEntry().getName().equals(name)) {
                return n.getEntry();
            }
        }

        return null;
    }

    /**
     *  Gets the leaf attribute of the CVSFileNode object
     *
     *@return    The leaf value
     */
    public boolean isLeaf() {
        return (entry == null) ? (!getFile().isDirectory()) :
            (!entry.isDirectory());
    }

    /**
     *  Gets the childCount attribute of the CVSFileNode object
     *
     *@return    The childCount value
     */
    public int getChildCount() {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }
        return isLeaf() ? 0 : ( (children == null) ? 1 : children.size());
    }
    
    public int getUnfilteredChildCount() {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }
        return isLeaf() ? 0 : ( (allChildren == null) ? 1 : allChildren.size());    	
    }

    /**
     *  Description of the Method
     */
    public synchronized void loadChildren() {
//    	if(Constants.SYSTEM_LOG.isDebugEnabled())
//        	Constants.SYSTEM_LOG.debug("Loading children for " + getName());
//        if ( (allChildren == null) && (file != null) && file.isDirectory()) {
//            allChildren = new Vector();
//            children = new Vector();
        allChildren = new Vector();
        children = new Vector();

        try {
            File[] f = getFile().listFiles();

            if (f != null) {
                for (int i = 0; i < f.length && !Thread.interrupted() && !context.getViewManager().isStopTreeLoad(); i++) {
                    Entry entry = CVSUtil.getHandler().getEntry(f[i]);
                    CVSFileNode n = new CVSFileNode(context, sortCriteria,
                        entry, f[i], this);
                    n.setIncludeAllFiles(includeAllFiles);
                    allChildren.addElement(n);
                }
            }

            for (Iterator i = CVSUtil.getHandler().getEntries(getFile());
                 i.hasNext()  && !Thread.interrupted() && !context.getViewManager().isStopTreeLoad(); ) {
                Entry e = (Entry) i.next();
                boolean found = false;

                for (int j = 0; (j < allChildren.size()) && !found; j++) {
                    if ( ( (CVSFileNode) allChildren.elementAt(j)).getName()
                        .equals(e.getName())) {
                        found = true;
                    }
                }

                if (!found) {
                    CVSFileNode n = new CVSFileNode(context, sortCriteria,
                        e, new File(getFile(), e.getName()), this);
                    n.setIncludeAllFiles(includeAllFiles);
                    allChildren.addElement(n);
                }
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public boolean isNeedsAttention() {
        return needsAttention;
    }

    public synchronized void filterAndSortChildren() {
        if (!isLeaf() && !isChildListLoaded() && project != null && context.getViewManager().getProject() == project)
            loadChildren();
        if (!isLeaf() && !isChildListLoaded()) {
            return;
        }
        needsAttention = false;
        directoryStatus = null;
        directoryStatusChecked = false;
		icon = null;
		style = null;
		styleChecked = false;
        CVSFileFilter filter = context.getFilterModel().getSelectedFilter();
        CVSFileNode n = null;
        FileStatus s = null;
        if(!isLeaf())
        {
        	children.removeAllElements();
			for (Iterator i = allChildren.iterator(); i.hasNext() && !context.getViewManager().isStopTreeLoad(); ) {
				n = (CVSFileNode) i.next();
				if(n.isLeaf()) {
					if (includeAllFiles || (filter == null) || filter.acceptNode(n)) {
						n.setParent(this);
						if (!needsAttention && project != null) {
							s = n.getOverallStatus();
							if (FileStatus.UP_TO_DATE != s) {
								needsAttention = true;
							}
						}
						children.addElement(n);
					}
				}
				else {
					if(!n.isIgnored()) {
						n.filterAndSortChildren();
						if (!needsAttention) {
							needsAttention = n.isNeedsAttention();
						}
						if(includeAllFiles || ( filter == null ) || ( project == null ) || filter.acceptNode(n)) {
							children.addElement(n);
						}
					}
				}
			}
			Collections.sort(children);
        }
    }

    /**
     *  Gets the childListLoaded attribute of the CVSFileNode object
     *
     *@return    The childListLoaded value
     */
    public boolean isChildListLoaded() {
        return (children == null || allChildren == null) ? false : true;
    }

    /**
     *  Gets the childAt attribute of the CVSFileNode object
     *
     *@param  i  Description of the Parameter
     *@return    The childAt value
     */
    public TreeNode getChildAt(int i) {
        if(!isChildListLoaded()) {
            loadChildren();
            filterAndSortChildren();
        }

        if (children == null) {
            return null;
        }
        else {
            return (TreeNode) children.elementAt(i);
        }
    }

    /**
     *  Gets the childAt attribute of the CVSFileNode object
     *
     *@param  i  Description of the Parameter
     *@return    The childAt value
     */
    public TreeNode getUnfilteredChildAt(int i) {
    	if(!isChildListLoaded()) {
    		loadChildren();
    		filterAndSortChildren();
    	}

    	if (allChildren == null) {
    		return null;
    	}
    	else {
    		return (TreeNode) allChildren.elementAt(i);
    	}
    }

    /**
     *  Sets the selected attribute of the CVSFileNode object
     *
     *@param  selected  The new selected value
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     *  Gets the file attribute of the CVSFileNode object
     *
     *@return    The file value
     */
    public File getFile() {
        return file;
    }

    /**
     *  Gets the selected attribute of the CVSFileNode object
     *
     *@return    The selected value
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Return if this node is a copy
     *
     * @return copy
     */
    public boolean isCopy() {
        return copy;
    }

    /**
     * DOCUMENT ME!
     *
     * @param v DOCUMENT ME!
     * @param includeDir DOCUMENT ME!
     * @param singleCopy DOCUMENT ME!
     */
    public void recurseNodes(Vector v, boolean includeDir, boolean singleCopy) {
        recurseNodes(this, v, includeDir, singleCopy);
    }

    private void recurseNodes(CVSFileNode node, Vector v, boolean includeDir,
                              boolean singleCopy) {
        if (node.isLeaf()) {
            if (!singleCopy || (singleCopy && (v.indexOf(node) == -1))) {
                v.addElement(node);
            }
        }
        else {
            for (int i = 0; i < node.getChildCount(); i++) {
                CVSFileNode n = (CVSFileNode) node.getChildAt(i);

                if (n != null) {
                    recurseNodes(n, v, includeDir, singleCopy);
                }
            }

            if (includeDir) {
                v.addElement(node);
            }
        }
    }

    /**
     * Return the overall status for this node. Remote status takes precedence
     */
    public FileStatus getOverallStatus() {
        return getRemoteStatus() == null ? getLocalStatus() : getRemoteStatus();
    }

    public DirectoryStatus getDirectoryStatus() {
        if(!directoryStatusChecked) {
            if(isNeedsAttention()) {
                directoryStatus = DirectoryStatus.ATTENTION;
            }
            else if(isProjectRoot()) {
                directoryStatus = DirectoryStatus.PROJECT;
            }
            else if(isModuleRoot()) {
                directoryStatus = DirectoryStatus.MODULE;
            }
            else if(!isLeaf()) {
                if(getLocalStatus() == FileStatus.UP_TO_DATE)
                    directoryStatus = DirectoryStatus.UP_TO_DATE;
                else
                    directoryStatus = DirectoryStatus.PLAIN;
            }
            else {
                directoryStatus = null;
            }
            directoryStatusChecked = true;
        }
        return directoryStatus;
    }

    /**
     * Return the file status for this node
     */
    public FileStatus getLocalStatus() {
        if (entry == null) {
            return FileStatus.UNKNOWN;
        }

        File file = getFile();

        if ( (entry != null) && entry.isNewUserFile()) {
            return FileStatus.ADDED;
        }

        if ( (entry != null) && entry.isUserFileToBeRemoved()) {
            return FileStatus.REMOVED;
        }

        if ( (entry != null) && (file != null) && !file.exists()) {
            return null;
        }

        if ( (entry != null) && (file != null) &&
            ( (entry.getLastModified() != null) &&
            !DateComparator.getInstance().equals(file.lastModified(),
                                                 entry.getLastModified().
                                                 getTime()) || ( entry != null && entry.getLastModified() == null && "Result of merge".equals(entry.getConflict())) )) {
            return FileStatus.MODIFIED;
        }

        if ( (entry != null) && entry.hadConflicts()) {
            return FileStatus.HAS_CONFLICTS;
        }

        return FileStatus.UP_TO_DATE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param includeAllFiles DOCUMENT ME!
     */
    public void setIncludeAllFiles(boolean includeAllFiles) {
        this.includeAllFiles = includeAllFiles;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        return getName();
    }

    static class FileStatusWrapper
        implements Comparable {

        FileStatus status;

        public FileStatusWrapper(FileStatus status) {
            this.status = status;
        }

        public int getImportance() {
            if (FileStatus.HAS_CONFLICTS == status) {
                return 90;
            }
            if (FileStatus.REMOVED == status) {
                return 80;
            }
            if (FileStatus.ADDED == status) {
                return 70;
            }
            if (FileStatus.MODIFIED == status) {
                return 60;
            }
            if (FileStatus.NEEDS_CHECKOUT == status) {
                return 50;
            }
            if (FileStatus.NEEDS_MERGE == status) {
                return 40;
            }
            if (FileStatus.NEEDS_PATCH == status) {
                return 30;
            }
            if (FileStatus.UP_TO_DATE == status) {
                return 20;
            }
            if (FileStatus.UNKNOWN == status) {
                return 10;
            }
            return 0;
        }

        public int compareTo(Object o) {
            if (o == null) {
                return 100;
            }
            else {
                FileStatusWrapper s = (FileStatusWrapper) o;
                int i1 = getImportance();
                int i2 = s.getImportance();
                return i1 - i2;
            }
        }
    }
}
