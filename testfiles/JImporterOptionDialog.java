/*
 *  JImporterOptionDialog.java - A class that will display the JImporter options
 *  outside of the standard "global options" dialog.  
 *  Copyright (C) 2002  Matthew Flower (MattFlower@yahoo.com)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package jimporter.options;


import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.jEdit;

/**
 * This class will display the JImporter options when the user clicks on options
 * from the menus.  I'm not really all that great of a Swing programmer, so I'm
 * going to have to thank Dirk Moebius heavily for this code, which I mostly 
 * swiped from JCompiler.  (Thanks, Dirk.)
 *
 * @author Matthew Flower
 * @since version 0.2
 */
public class JImporterOptionDialog extends EnhancedDialog implements ActionListener {
    /**
     * A constructor that creates an object of type JImporterOptionDialog.  
     *
     * @param view a <code>View</code> object that will allow the dialog box to center 
     * itself in the current window.
     */
    public JImporterOptionDialog(View view) {
        super(view, jEdit.getProperty("options.jimporter.label"), true);
        view.showWaitCursor();
        try {
            JPanel content = new JPanel(new BorderLayout());
            content.setBorder(new EmptyBorder(5,8,8,8));
            content.setLayout(new BorderLayout());
            setContentPane(content);
               
            classpathPane = new ClasspathOptionPane();
            classpathPane.setBorder(new EmptyBorder(5,5,5,5));
            classpathPane.init();
            
            miscellaneousPane = new MiscellaneousOptionPane();
            miscellaneousPane.setBorder(new EmptyBorder(5,5,5,5));
            miscellaneousPane.init();
            
            sortingPane = new SortingOptionPane();
            sortingPane.setBorder(new EmptyBorder(5,5,5,5));
            sortingPane.init();
            
            JTabbedPane paneContainer = new JTabbedPane();
            paneContainer.addTab(jEdit.getProperty("options."+classpathPane.getName()+".label"), classpathPane);
            paneContainer.addTab(jEdit.getProperty("options."+miscellaneousPane.getName()+".label"), miscellaneousPane);
            paneContainer.addTab(jEdit.getProperty("options."+sortingPane.getName() + ".label"), sortingPane);
            
            content.add(paneContainer, BorderLayout.CENTER);
            
            JPanel buttons = new JPanel();
            buttons.setBorder(new EmptyBorder(12,0,0,0));
            buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
            buttons.add(Box.createGlue());                       
            
            ok = new JButton(jEdit.getProperty("common.ok"));
            ok.addActionListener(this);
            
            buttons.add(ok);
            buttons.add(Box.createHorizontalStrut(6));
            
            getRootPane().setDefaultButton(ok);
            
            cancel = new JButton(jEdit.getProperty("common.cancel"));
            cancel.addActionListener(this);
            buttons.add(cancel);
            buttons.add(Box.createHorizontalStrut(6));
            
            apply = new JButton(jEdit.getProperty("common.apply"));
            apply.addActionListener(this);
            buttons.add(apply);
            
            buttons.add(Box.createGlue());
            content.add(buttons, BorderLayout.SOUTH);
        } finally {             
            view.hideWaitCursor();
        }
        pack();
        setLocationRelativeTo(view);
        show();
    }
    
    /**
     * This method is called if the user clicks on the ok button from the 
     * JImporter pane in the Global options.  The method is defined by the parent
     * class.
     */
    public void ok() {
        ok(true);
    }
    
    /**
     * This method, defined by the parent class, is called when the user clicks
     * on the cancel button when the global options dialog is on the JImporter
     * option pane.
     */
    public void cancel() {
        dispose();
    }
    
    /**
     * User has clicked okay, save all the settings and signal to jEdit that things
     * have changed.
     *
     * @param dispose a <code>boolean</code> value indicating whether the dialog
     * box should automatically disposed of.
     */
    public void ok(boolean dispose) {
        //Save the settings the user has just set
        classpathPane.save();
        miscellaneousPane.save();
        sortingPane.save();
        
        //Indicate to jEdit that changes have occurred
        jEdit.propertiesChanged();
        jEdit.saveSettings();
        
        //If the param has requested it, dispose of the dialog box.
        if (dispose)
            dispose();        
    }
    
    /**
     * This method is called in response to any button click to dispatch the 
     * click to the correct method.
     *
     * @param evt an <code>ActionEvent</code> object that contains information
     * about the button click.
     */
    public void actionPerformed(ActionEvent evt) {
        Object source = evt.getSource();
        
        if (source == ok) {
            ok();
        } else if (source == cancel) {
            cancel();
        } else if (source == apply) {
            ok(false);
        }
    }
    
    private JButton ok;
    private JButton cancel;
    private JButton apply;
    private AbstractOptionPane classpathPane;
    private AbstractOptionPane miscellaneousPane;
    private AbstractOptionPane sortingPane;
}
