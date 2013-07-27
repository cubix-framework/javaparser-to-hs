/*
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

package net.sourceforge.jedit.jcompiler;
 
import org.gjt.sp.jedit.*;
 
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

//for clipboard
import java.awt.datatransfer.*;

/**
@author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
@version $Id: StackTraceDialog.java 7900 2000-02-01 10:06:22Z burtonator $
*/
public class StackTraceDialog extends JDialog implements ActionListener, WindowListener {

    private JTextArea text      = new JTextArea();
    private JButton   ok        = new JButton("OK");
    private JButton   cancel    = new JButton("Cancel");
    private String    stackData = "";
    
    /**
    @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
    @version $Id: StackTraceDialog.java 7900 2000-02-01 10:06:22Z burtonator $
    */
    public StackTraceDialog(Frame owner) {

        super( owner, "Enter Stack Trace" );
        
        GUIUtilities.loadGeometry( this, "jcompiler.StackTraceDialog" );

        this.addWindowListener(this);
        
        this.getContentPane().setLayout( new BorderLayout() );
        
        this.getContentPane().add( text, BorderLayout.CENTER );
        
        JPanel buttons = new JPanel();
        buttons.setLayout( new FlowLayout() );
        buttons.add( ok );
        buttons.add( cancel );
        this.getContentPane().add( buttons, BorderLayout.SOUTH );        
        
        ok.addActionListener( this );
        cancel.addActionListener( this );

        text.addMouseListener( new MouseHandler( text ) );
        
        this.setSize( new Dimension(600, 400) );
        this.setEnabled(true);
        this.setVisible(true);
        this.show();
        this.toFront();

    }


    /**
    @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
    @version $Id: StackTraceDialog.java 7900 2000-02-01 10:06:22Z burtonator $
    */
    public void actionPerformed(ActionEvent e) {
        
        if (e.getSource() == this.ok) {

            //FIX ME
            //JCompilerPlugin.setErrorData( text.getText() );
            
            this.dispose();

        } else if (e.getSource() == this.cancel) {
            this.close();
        }

        
    }

    /**
    @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
    @version $Id: StackTraceDialog.java 7900 2000-02-01 10:06:22Z burtonator $
    */
    public void close() {
        GUIUtilities.saveGeometry( this, "jcompiler.StackTraceDialog" );
        this.dispose();
    }
    
    public void windowActivated(WindowEvent e) {}
        
    public void windowClosed(WindowEvent e) {}
    
    public void windowClosing(WindowEvent e) {

        this.close();
        

    }

    public void windowDeactivated(WindowEvent e) {} 

    public void windowDeiconified(WindowEvent e) {} 

    public void windowIconified(WindowEvent e) {}

    public void windowOpened(WindowEvent e) {}
    

    /**
    @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
    @version $Id: StackTraceDialog.java 7900 2000-02-01 10:06:22Z burtonator $
    */
	class MouseHandler extends MouseAdapter implements ActionListener {

        private JMenuItem paste = null;
        private JPopupMenu menu = null;
        private JTextArea owner = null;

        public MouseHandler(JTextArea owner) {
            this.owner = owner;
        }

		public void mousePressed(MouseEvent evt) {

			if((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
				showFullMenu( evt.getX(), evt.getY() );

		}


        private void showFullMenu(int x, int y)	{
    
            this.paste = new JMenuItem("Paste");
            this.menu = new JPopupMenu();
    
            paste.addActionListener(this);
            menu.add(paste);
            menu.show( this.owner, x, y);
            
        }

        public void actionPerformed(ActionEvent e) {
            
            if (e.getSource() == this.paste) {
 
                this.owner.append( Registers.getRegister('$').toString() );

            }
            
        }

	}
    
    
}
