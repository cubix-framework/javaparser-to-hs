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

package gruntspud.ui.report;

import gruntspud.CVSFileNode;
import gruntspud.CVSFileTypeUtil;
import gruntspud.CVSSubstType;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.VersionInfo;
import gruntspud.file.GruntspudFileMode;
import gruntspud.ui.UIUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.file.FileStatus;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class FileDetailsPane
    extends JPanel {
    private final static DecimalFormat SIZE_FORMAT = new DecimalFormat("");
    private static DateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = SimpleDateFormat.getDateTimeInstance(DateFormat.LONG,
            DateFormat.LONG);

        //        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT+0000"));
    }

    private JLabel file;
    private JLabel type;
    private JLabel size;
    private JLabel access;
    private JLabel locallyLastModified;
    private JLabel remotelyLastModified;
    private JLabel keywordSubstitution;
    private JLabel flags;
    private JLabel localStatus, remoteStatus;
    private JLabel tag;
    private JLabel conflict;
    private JLabel revision;
    private JLabel nodeHash;
    private GruntspudContext context;

    /**
     * Constructor
     */
    public FileDetailsPane(GruntspudContext context, CVSFileNode node) {
        this();
        this.context = context;
        setNode(node);
    }

    /**
     * Constructor
     */
    public FileDetailsPane() {
        super(new GridBagLayout());

        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        UIUtil.jGridBagAdd(this, new JLabel("File: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        file = new JLabel();
        file.setIcon(UIUtil.EMPTY_SMALL_ICON);
        file.setFont(valFont);
        UIUtil.jGridBagAdd(this, file, gbc, GridBagConstraints.REMAINDER);

        if(VersionInfo.getVersion().equals("HEAD")) {
            gbc.weightx = 0.0;
            UIUtil.jGridBagAdd(this, new JLabel("Node hash: "), gbc,
                               GridBagConstraints.RELATIVE);
            gbc.weightx = 1.0;
            nodeHash = new JLabel();
            nodeHash.setFont(valFont);
            nodeHash.setIcon(UIUtil.EMPTY_SMALL_ICON);
            UIUtil.jGridBagAdd(this, nodeHash, gbc, GridBagConstraints.REMAINDER);

        }


        gbc.weightx = 0.0;

        UIUtil.jGridBagAdd(this, new JLabel("Type: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        type = new JLabel();
        type.setFont(valFont);
        UIUtil.jGridBagAdd(this, type, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l4 = new JLabel("Local status: ");
        UIUtil.jGridBagAdd(this, l4, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        localStatus = new JLabel();
        localStatus.setIcon(UIUtil.EMPTY_SMALL_ICON);
        l4.setLabelFor(localStatus);
        localStatus.setFont(valFont);
        UIUtil.jGridBagAdd(this, localStatus, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l4b = new JLabel("Remote status: ");
        UIUtil.jGridBagAdd(this, l4b, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        remoteStatus = new JLabel();
        remoteStatus.setIcon(UIUtil.EMPTY_SMALL_ICON);
        l4b.setLabelFor(remoteStatus);
        remoteStatus.setFont(valFont);
        UIUtil.jGridBagAdd(this, remoteStatus, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l2 = new JLabel("Keyword Substitution: ");
        UIUtil.jGridBagAdd(this, l2, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        keywordSubstitution = new JLabel();
        l2.setLabelFor(keywordSubstitution);
        keywordSubstitution.setFont(valFont);
        UIUtil.jGridBagAdd(this, keywordSubstitution, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Size: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        size = new JLabel();
        size.setIcon(UIUtil.EMPTY_SMALL_ICON);
        size.setFont(valFont);
        UIUtil.jGridBagAdd(this, size, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Access: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        access = new JLabel();
        access.setIcon(UIUtil.EMPTY_SMALL_ICON);
        access.setFont(valFont);
        UIUtil.jGridBagAdd(this, access, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(this, new JLabel("Locally last modified: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        locallyLastModified = new JLabel();
        locallyLastModified.setIcon(UIUtil.EMPTY_SMALL_ICON);
        locallyLastModified.setFont(valFont);
        UIUtil.jGridBagAdd(this, locallyLastModified, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l0 = new JLabel("Remotely last modified: ");
        UIUtil.jGridBagAdd(this, l0, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        remotelyLastModified = new JLabel();
        remotelyLastModified.setIcon(UIUtil.EMPTY_SMALL_ICON);
        l0.setLabelFor(remotelyLastModified);
        remotelyLastModified.setFont(valFont);
        UIUtil.jGridBagAdd(this, remotelyLastModified, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        gbc.weightx = 1.0;

        JLabel l6 = new JLabel("Conflict: ");
        UIUtil.jGridBagAdd(this, l6, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        conflict = new JLabel();
        conflict.setIcon(UIUtil.EMPTY_SMALL_ICON);
        l6.setLabelFor(conflict);
        conflict.setFont(valFont);
        UIUtil.jGridBagAdd(this, conflict, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l1 = new JLabel("Revision: ");
        UIUtil.jGridBagAdd(this, l1, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        revision = new JLabel();
        l1.setLabelFor(revision);
        revision.setIcon(UIUtil.EMPTY_SMALL_ICON);
        revision.setFont(valFont);
        UIUtil.jGridBagAdd(this, revision, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l3 = new JLabel("Flags: ");
        UIUtil.jGridBagAdd(this, l3, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        flags = new JLabel();
        flags.setFont(valFont);
        flags.setIcon(UIUtil.EMPTY_SMALL_ICON);
        l3.setLabelFor(flags);
        UIUtil.jGridBagAdd(this, flags, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;

        JLabel l5 = new JLabel("Tag: ");
        UIUtil.jGridBagAdd(this, l5, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        tag = new JLabel();
        l5.setLabelFor(tag);
        tag.setIcon(UIUtil.EMPTY_SMALL_ICON);
        tag.setFont(valFont);
        UIUtil.jGridBagAdd(this, tag, gbc, GridBagConstraints.REMAINDER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public void setNode(CVSFileNode node) {
        if(nodeHash != null) {
            nodeHash.setText("#" + node.hashCode());
        }
        file.setText(node.getName());
        file.setToolTipText(node.getFile().getAbsolutePath());

        String typeText = CVSFileTypeUtil.getFileSystemTypeDescriptionName(node.
            getFile());
        type.setText( ( (typeText == null) || typeText.equals("")) ? "File"
                     : typeText);
        Icon icn = null;
        if(node.getFile().exists())
            icn = CVSFileTypeUtil.getFileSystemIcon(node.getFile());
        else
            icn = UIUtil.EMPTY_SMALL_ICON;
        type.setIcon(icn);

        long length = node.getFile().length();
        size.setToolTipText(String.valueOf(length) + " bytes");
        size.setText(GruntspudUtil.formatFileSize(length));

        String accessText = null;
        Color accessColor = null;
        String modeText = null;

        if (!node.getFile().exists()) {
            accessText = "Doesn't exist";
            accessColor = Color.red;
        }
        else {
        	GruntspudFileMode mode = GruntspudFileMode.getFileModeForFile(node.getFile());
            modeText = mode.toString();

            if (!node.getFile().canRead() && !node.getFile().canWrite()) {
                accessText = "No read / No write";
                accessColor = Color.red;
            }
            else if (!node.getFile().canWrite() && node.getFile().canRead()) {
                accessText = "Read only";
                accessColor = Color.red;
            }
            else if (node.getFile().canWrite() && !node.getFile().canRead()) {
                accessText = "Write only";
                accessColor = Color.red;
            }
            else if (node.getFile().canWrite() && node.getFile().canRead()) {
                accessText = "Read / Write";
            }
        }

        access.setText(accessText +
                       ( (modeText == null) ? "" : (" (" + modeText + ")")));
        access.setForeground( (accessColor != null) ? accessColor
                             : UIManager.getColor(
            "Label.foreground"));

        if (node.getFile().exists()) {
            locallyLastModified.setText(DATE_FORMAT.format(
                new Date(node.getFile().lastModified())));
        }
        else {
            locallyLastModified.setText("N/A");

        }
        Entry e = node.getEntry();

        FileStatus local = node.getLocalStatus();
        localStatus.setText(node.getLocalStatusText());
        localStatus.setIcon(node.getIconForStatus(local, node.getBaseIcon(false)));

        FileStatus remote = node.getRemoteStatus();
        remoteStatus.setText(node.getRemoteStatusText());
        remoteStatus.setIcon(node.getIconForStatus(remote, node.getBaseIcon(false)));

        if (e != null) {
            keywordSubstitution.setEnabled(true);
            remotelyLastModified.setEnabled(true);
            flags.setEnabled(true);
            localStatus.setEnabled(true);
            remoteStatus.setEnabled(true);
            tag.setEnabled(true);
            conflict.setEnabled(true);
            revision.setEnabled(true);

            CVSSubstType type = node.getCVSSubstType();
            keywordSubstitution.setIcon(type.getIcon());
            keywordSubstitution.setText(type.getName());

            if (e.getLastModified() != null) {
                remotelyLastModified.setText(DATE_FORMAT.format(
                    e.getLastModified()));

                Date locDate = new Date(node.getFile().lastModified());
                long loc = node.getFile().lastModified();
                Date remoDate = e.getLastModified();
                long remo = remoDate.getTime();
                long dif = loc - remo;
                long tzDiff = TimeZone.getDefault().getRawOffset();

                /*
                   String s =
                   "TIME DEBUG: " +
                       loc + " - " + remo + " = " + dif + "ms (" +
                     ( dif / 1000 ) + "s) TZ diff= " + tzDiff + " GMTLocal=" +
                     locDate.toGMTString() + " GMTRemote=" + remoDate.toGMTString();
                   context.debugMessage(s);
                   remotelyLastModified.setToolTipText(s);
                 */
            }
            else {
                remotelyLastModified.setText("N/A");

            }
            flags.setText(e.getOptions());

            tag.setText(node.getTag());

            conflict.setText(e.getConflict());

            revision.setText(e.getRevision());
        }
        else {
            remotelyLastModified.setText("N/A");
            keywordSubstitution.setText("N/A");
            keywordSubstitution.setIcon(null);
            flags.setText("N/A");
            tag.setText("N/A");
            conflict.setText("N/A");
            revision.setText("N/A");

            keywordSubstitution.setEnabled(false);
            remotelyLastModified.setEnabled(false);
            flags.setEnabled(false);
            tag.setEnabled(false);
            conflict.setEnabled(false);
            revision.setEnabled(false);
        }
    }
}
