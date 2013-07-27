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

package ise.plugin.svn.pv;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.JOptionPane;
import ise.plugin.svn.action.TimeLapseAction;
import ise.plugin.svn.data.DiffData;


/**
 * Action for ProjectViewer's context menu to execute an svn diff between a
 * working copy and a remote revision.  Allows just one node to be selected in
 * PV, and that node must be a file, not a directory, otherwise, does an svn
 * diff, not a JDiff diff.
 */
public class TimeLapseActor extends NodeActor {

    public void actionPerformed( ActionEvent ae ) {
        if ( nodes != null && nodes.size() == 1 && !hasDirectory ) {
            DiffData data = new DiffData();
            data.setPaths( getNodePaths() );
            data.setUsername( username );
            data.setPassword( password );
            data.setRecursive( false );
            data.setSvnDiff( false );
            TimeLapseAction action = new TimeLapseAction( view, data );
            action.actionPerformed( ae );
        }
        else {
            JOptionPane.showMessageDialog( view, "Nothing selected for time lapse.", "Error", JOptionPane.ERROR_MESSAGE );
        }
    }
}