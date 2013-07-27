/*
 *  :tabSize=4:indentSize=4:noTabs=true:
 *
 *
 *  $Source$
 *  Copyright (C) 2004 Jeffrey Hoyt
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
package shortcutdisplay;


import shortcutdisplay.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.util.*;

/**
 *  Pop up for the shortcut display
 *
 *@author     jchoyt
 *@created    April 29, 2004
 */
class ShortcutDialog extends JDialog
{

    /**
     *  Constructor for the ShortcutDialog object
     *
     *@param  model  Description of the Parameter
     */
    public ShortcutDialog( ShortcutTableModel model )
    {
        init( model );
    }


    /**
     *  Constructor for the ShortcutDialog object
     *
     *@param  bindings  Description of the Parameter
     */
    public ShortcutDialog( Map bindings )
    {
        ShortcutTableModel model = new ShortcutTableModel( bindings );
        init( model );
    }


    /**
     *  Description of the Method
     *
     *@param  model  Description of the Parameter
     */
    protected void init( ShortcutTableModel model )
    {
        getContentPane().setLayout( new BorderLayout() );
        getContentPane().add( buildTablePanel( model ), BorderLayout.CENTER );
        setLocationRelativeTo( jEdit.getActiveView() );
        setLocation( calculateLocation() );
        setTitle( "Complete shortcuts that start with what you typed" );
        setFocusableWindowState( false );
    }


    /**
     *  calculates a location that is 75% from the left edge of the view and 20%
     *  down. This seems to provide a good fit, at least for the two resolutions
     *  I use most.
     *
     *@return    Description of the Return Value
     */
    protected Point calculateLocation()
    {
        Dimension dim = jEdit.getActiveView().getSize();
        int x = jEdit.getIntegerProperty( "shortcutdisplay.xlocation", 3 * dim.width / 4 );
        int y = jEdit.getIntegerProperty( "shortcutdisplay.ylocation", dim.height / 5 );
        return new Point( x, y );
    }


    /**
     *  Builds and returns a JPanel containing a single JTable
     *
     *@param  model  Description of the Parameter
     *@return        Description of the Return Value
     */
    public JPanel buildTablePanel( ShortcutTableModel model )
    {
        JPanel ret = new JPanel();
        ret.setLayout( new BorderLayout() );
        // ret.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        JTable table = new JTable( model );
        table.getColumnModel().getColumn( 0 ).setPreferredWidth( 10 * model.maxActionLength );
        table.getColumnModel().getColumn( 1 ).setPreferredWidth( 10 * model.maxShortcutLength );
        JScrollPane scroller = new JScrollPane( table );
        scroller.setPreferredSize( new Dimension( (int)table.getPreferredSize().getWidth() + 10, 1000 ) );
        JScrollBar jsb = scroller.getVerticalScrollBar();
        int inc = jsb.getUnitIncrement();
        jsb.setUnitIncrement(inc*20);
        ret.add( scroller, BorderLayout.CENTER );
        return ret;
    }
}


