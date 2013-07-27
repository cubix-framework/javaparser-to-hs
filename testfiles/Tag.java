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

package ise.plugin.svn.gui.br;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.tree.TreePath;
import ise.plugin.svn.action.*;
import ise.plugin.svn.gui.*;
import ise.plugin.svn.data.*;
import ise.plugin.svn.library.GUIUtils;
import java.util.*;


public class Tag extends BRAction {

    public void actionPerformed( ActionEvent ae ) {
        TreePath[] tree_paths = tree.getSelectionPaths();
        if ( tree_paths.length == 0 ) {
            return ;
        }
        if ( tree_paths.length > 1 ) {
            JOptionPane.showMessageDialog( view, "Please select a single entry.", "Too many selections", JOptionPane.ERROR_MESSAGE );
            return ;
        }
        String from_url = null;
        String defaultDestination = null;
        for ( TreePath path : tree_paths ) {    // should be a single loop
            if ( path != null ) {
                Object[] parts = path.getPath();
                StringBuilder from = new StringBuilder();
                StringBuilder to = new StringBuilder();
                String preface = parts[ 0 ].toString();
                if ( preface.endsWith( "/" ) ) {
                    preface = preface.substring( 0, preface.length() - 1 );
                }
                from.append( preface );
                to.append( preface );
                for ( int i = 1; i < parts.length; i++ ) {
                    from.append( "/" ).append( parts[ i ].toString() );
                }
                for ( int i = 1; i < parts.length - 1; i++ ) {
                    if ( parts[ i ].toString().equals( "branches" ) ) {
                        continue;
                    }
                    to.append( "/" ).append( parts[ i ].toString() );
                }
                from_url = from.toString();
                defaultDestination = to.append( "/tags" ).toString();
                break;
            }
        }

        TagBranchDialog dialog = new TagBranchDialog( view, TagBranchDialog.TAG_DIALOG, from_url, defaultDestination );
        GUIUtils.center( view, dialog );
        dialog.setVisible( true );
        CopyData cd = dialog.getData();
        if ( cd != null ) {
            if ( username != null && password != null ) {
                cd.setUsername( username );
                cd.setPassword( password );
            }
            cd.setTitle( "Tag" );
            CopyAction action = new CopyAction( view, cd );
            action.actionPerformed( null );
        }
    }
}
