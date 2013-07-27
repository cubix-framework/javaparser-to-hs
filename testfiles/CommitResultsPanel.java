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

import java.text.SimpleDateFormat;
import java.awt.BorderLayout;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.EmptyBorder;
import ise.plugin.svn.library.GUIUtils;
import ise.plugin.svn.library.ListOps;
import ise.plugin.svn.data.CommitData;
import ise.plugin.svn.data.CommitInfo;
import org.gjt.sp.jedit.jEdit;

public class CommitResultsPanel extends JPanel {



    public CommitResultsPanel( CommitData results ) {
        super( new BorderLayout() );
        setBorder( new EmptyBorder( 3, 3, 3, 3 ) );

        JLabel label = new JLabel( jEdit.getProperty("ips.Committed>", "Committed:") );
        CommitInfo info = results.getInfo();
        String revision = String.valueOf( info.getRevision() );
        String author = info.getAuthor();
        String date = new SimpleDateFormat( jEdit.getProperty("ips.yyyy-MM-dd_HH>mm>ss_Z", "yyyy-MM-dd HH:mm:ss Z"), Locale.getDefault() ).format( info.getDate() );

        List<String> paths = ListOps.toList(ListOps.toSet(results.getPaths()));
        Collections.sort( paths );
        String[][] data = new String[ paths.size() ][ 4 ];
        int i = 0;
        for ( String path : paths ) {
            data[ i ][ 0 ] = path;
            data[ i ][ 1 ] = revision;
            data[ i ][ 2 ] = date;
            data[ i ][ 3 ] = author;
            ++i;
        }
        JTable table = new JTable( data, new String[] {jEdit.getProperty("ips.Path", "Path"), jEdit.getProperty("ips.Revision", "Revision"), jEdit.getProperty("ips.Date", "Date"), jEdit.getProperty("ips.Author", "Author")} );
        TableColumnModel column_model = table.getColumnModel();
        TableColumn column0 = column_model.getColumn( 1 );
        column0.setMaxWidth( 60 );
        column0.setPreferredWidth( 60 );
        TableColumn column1 = column_model.getColumn( 2 );
        column1.setMaxWidth( 200 );
        column1.setPreferredWidth( 200 );
        TableColumn column2 = column_model.getColumn( 3 );
        column2.setMaxWidth( 180 );
        column2.setPreferredWidth( 180 );


        add( label, BorderLayout.NORTH );
        add( GUIUtils.createTablePanel(table), BorderLayout.CENTER );
    }

}
