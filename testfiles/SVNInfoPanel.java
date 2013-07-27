/*
Copyright (c) 2007, Dale Anson
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the author nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ise.plugin.svn.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;

import java.text.SimpleDateFormat;
import javax.swing.table.*;

import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.wc.SVNInfo;

import ise.plugin.svn.library.TableCellViewer;
import ise.plugin.svn.command.SVNFormatUtil;

import org.gjt.sp.jedit.jEdit;

public class SVNInfoPanel extends JPanel {


    public SVNInfoPanel( List<SVNInfo> infos ) {
        super( new GridLayout( 0, 1, 0, 3 ) );
        for ( SVNInfo info : infos ) {
            addInfo( info );
        }
    }

    private void addInfo( SVNInfo info ) {
        if ( info == null ) {
            return ;
        }
        final DefaultTableModel info_table_model = new DefaultTableModel( 1, 2 );

        // load the table model
        if ( !info.isRemote() ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Path", "Path"), SVNFormatUtil.formatPath( info.getFile() ) } );
        }
        else if ( info.getPath() != null ) {
            String path = info.getPath();
            path = path.replace( '/', File.separatorChar );
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Path", "Path"), path} );
        }
        if ( info.getKind() != SVNNodeKind.DIR ) {
            String v = "";
            if ( info.isRemote() ) {
                v = SVNPathUtil.tail( info.getPath() );
            }
            else {
                v = info.getFile().getName();
            }
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Name", "Name"), v} );
        }
        if ( info.getURL() != null ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.URL", "URL"), info.getURL().toString() } );
        }
        if ( info.getRepositoryRootURL() != null ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Repository_Root", "Repository Root"), String.valueOf( info.getRepositoryRootURL() ) } );
        }
        if ( info.isRemote() && info.getRepositoryUUID() != null ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Repository_UUID", "Repository UUID"), info.getRepositoryUUID() } );
        }
        if ( info.getRevision() != null && info.getRevision().isValid() ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Revision", "Revision"), String.valueOf( info.getRevision() ) } );
        }
        if ( info.getKind() == SVNNodeKind.DIR ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Node_Kind", "Node Kind"), jEdit.getProperty("ips.directory", "directory")} );
        }
        else if ( info.getKind() == SVNNodeKind.FILE ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Node_Kind", "Node Kind"), jEdit.getProperty("ips.file", "file")} );
        }
        else if ( info.getKind() == SVNNodeKind.NONE ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Node_Kind", "Node Kind"), jEdit.getProperty("ips.none", "none")} );
        }
        else {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Node_Kind", "Node Kind"), jEdit.getProperty("ips.unknown", "unknown")} );
        }
        if ( info.getSchedule() == null && !info.isRemote() ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Schedule", "Schedule"), jEdit.getProperty("ips.normal", "normal")} );
        }
        else if ( !info.isRemote() ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Schedule", "Schedule"), info.getSchedule() } );
        }
        if ( info.getAuthor() != null ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Last_Changed_Author", "Last Changed Author"), info.getAuthor() } );
        }
        if ( info.getCommittedRevision() != null && info.getCommittedRevision().getNumber() >= 0 ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Last_Changed_Revision", "Last Changed Revision"), String.valueOf( info.getCommittedRevision() ) } );
        }
        if ( info.getCommittedDate() != null ) {
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Last_Changed_Date", "Last Changed Date"), formatDate( info.getCommittedDate() ) } );
        }
        if ( !info.isRemote() ) {
            if ( info.getTextTime() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Text_Last_Updated", "Text Last Updated"), formatDate( info.getTextTime() ) } );
            }
            if ( info.getPropTime() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Properties_Last_Updated", "Properties Last Updated"), formatDate( info.getPropTime() ) } );
            }
            if ( info.getChecksum() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Checksum", "Checksum"), info.getChecksum() } );
            }
            if ( info.getCopyFromURL() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Copied_From_URL", "Copied From URL"), String.valueOf( info.getCopyFromURL() ) } );
            }
            if ( info.getCopyFromRevision() != null && info.getCopyFromRevision().getNumber() >= 0 ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Copied_From_Revision", "Copied From Revision"), String.valueOf( info.getCopyFromRevision() ) } );
            }
            if ( info.getConflictOldFile() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Conflict_Previous_Base_File", "Conflict Previous Base File"), info.getConflictOldFile().getName() } );
            }
            if ( info.getConflictWrkFile() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Conflict_Previous_Working_File", "Conflict Previous Working File"), info.getConflictWrkFile().getName() } );
            }
            if ( info.getConflictNewFile() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Conflict_Current_Base_File", "Conflict Current Base File"), info.getConflictNewFile().getName() } );
            }
            if ( info.getPropConflictFile() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Conflict_Properties_File", "Conflict Properties File"), info.getPropConflictFile().getName() } );
            }
        }
        if ( info.getLock() != null ) {
            SVNLock lock = info.getLock();
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Lock_Token", "Lock Token"), lock.getID() } );
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Lock_Owner", "Lock Owner"), lock.getOwner() } );
            info_table_model.addRow( new String[] {jEdit.getProperty("ips.Lock_Created", "Lock Created"), formatDate( lock.getCreationDate() ) } );
            if ( lock.getComment() != null ) {
                info_table_model.addRow( new String[] {jEdit.getProperty("ips.Lock_Comment", "Lock Comment"), lock.getComment() } );
            }
        }

        info_table_model.removeRow(0);
        JPanel panel = new JPanel( new BorderLayout() );
        add( panel );
        panel.setBorder( new EtchedBorder() );
        BestRowTable info_table = new BestRowTable();
        info_table.setName("info_table");
        panel.add( info_table, BorderLayout.CENTER );
        info_table.setModel( info_table_model );
        TableColumn column1 = info_table.getColumnModel().getColumn( 1 );
        column1.setCellRenderer( new NoWrapCellRenderer() );
        info_table.getColumnModel().getColumn( 0 ).setMaxWidth( 150 );
        info_table.getColumnModel().getColumn( 0 ).setMinWidth( 150 );
        info_table.getColumnModel().getColumn( 1 ).setPreferredWidth( 600 );
        info_table.packRows();
        info_table.addMouseListener( new TableCellViewer( info_table ) );
    }

    private String formatDate( Date date ) {
        return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z (EE, d MMM yyyy)", Locale.getDefault() ).format( date );
    }
}
