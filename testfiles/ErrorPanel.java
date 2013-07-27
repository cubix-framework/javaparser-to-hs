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
import javax.swing.*;
import javax.swing.border.*;
import org.gjt.sp.jedit.jEdit;


/**
 * A panel to display an error message.
 *
 * @author    Dale Anson
 */
public class ErrorPanel extends JPanel {

    private String errorMessage = null;

    public ErrorPanel( String message ) {
        errorMessage = message;
        init();
    }

    private void init( ) {
        JLabel message = new JLabel();
        if ( errorMessage == null || errorMessage.length() == 0 ) {
            message.setText( "<html><font color=red>" + jEdit.getProperty("ips.Unknown_error,_check_SVN_Console_output.", "Unknown error, check SVN Console output.") );
        }
        else {
            errorMessage = errorMessage.replaceAll("\\n", "<br>");
            message.setText( "<html><font color=red>" + errorMessage );
        }

        // construct this panel
        setLayout( new BorderLayout() );
        setBorder( new javax.swing.border.EmptyBorder( 6, 6, 6, 6 ) );
        add( message, BorderLayout.CENTER );
    }

}
