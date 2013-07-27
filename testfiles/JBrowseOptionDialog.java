/*
* JBrowseOptionDialog.java
* Copyright (c) 2000 Andre Kaplan
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/


package sidekick.java.options;

import sidekick.java.JavaParser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.gjt.sp.jedit.jEdit;


public class JBrowseOptionDialog extends JDialog implements ActionListener {
    
    private JavaParser parent;
    JBrowseOptionPane optionPane;

    // private members
    private JButton btnSetAsDefaults;
    private JButton btnRestoreDefaults;

    private boolean initialized = false;


    public JBrowseOptionDialog( JavaParser parent, String title ) {
        // jdk1.1 + swing 1.1.1fcs and earlier don't have the following
        // super(parent, title);
        super();         // fix for jdk1.1
        setTitle( title ); // fix for jdk1.1
        this.parent = parent;

        optionPane = new JBrowseOptionPane( "jbrowse_options" );
        optionPane.setPropertyAccessor( new JEditPropertyAccessor() );
        optionPane.load();

        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( BorderLayout.CENTER, optionPane );

        JPanel buttons = new JPanel();
        btnSetAsDefaults = new JButton( "Set As Defaults" );
        btnSetAsDefaults.addActionListener( this );
        buttons.add( btnSetAsDefaults );

        btnRestoreDefaults = new JButton( "Restore Defaults" );
        btnRestoreDefaults.addActionListener( this );
        buttons.add( btnRestoreDefaults );
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                JBrowseOptionDialog.this.setVisible(false);
                JBrowseOptionDialog.this.dispose();
            }
        });
        buttons.add(closeBtn);
        
        getContentPane().add( BorderLayout.SOUTH, buttons );
    }


    /**
     * Returns the option object associated with this OptionDialog's
     * JBrowseOptionPane.
     */
    public GeneralOptions getOptions() {
        return optionPane.getOptions();
    }


    public void init() {
        optionPane.init();
        if ( !initialized ) {
            optionPane.removeDefaultListeners();
            optionPane.addJBrowseListeners( parent );
            pack();
            initialized = true;
        }
    }


    public void actionPerformed( ActionEvent evt ) {
        Object source = evt.getSource();
        if ( source == btnSetAsDefaults ) {
            optionPane.save();
        }
        else if ( source == btnRestoreDefaults ) {
            boolean wasShowingStatusBar = getOptions().getShowStatusBar();
            optionPane.load();
            optionPane.initModel();
            parent.parse();

            // Display or hide the status bar
            boolean setShowingStatusBar = getOptions().getShowStatusBar();
            if ( wasShowingStatusBar != setShowingStatusBar ) {
                /// danson, I don't think this is necessary any more
                ///parent.setStatusVisible(setShowingStatusBar);
                ///parent.setPreferredSize();
            }
        }
    }
}
