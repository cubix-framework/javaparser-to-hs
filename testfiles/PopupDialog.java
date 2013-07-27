package ise.plugin.svn.gui.dateselector;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

// Test this class by instantiating DateSelector

/** A PopupDialog is a clean, lightweight, "modal" window intended for
 *  simple pop-up user-interface widgets. The  frame, as shown at right,
 *  <img src="../../../images/PopupDialog.gif" align=right>
 *  is a single-pixel-wide
 *  line; the title bar holds only
 *  the title text and a small "close-window" icon.
 *  The dialog
 *  box can be dragged around on the screen by grabbing the title
 *  bar (and closed by clicking on the icon), but the user can't
 *  resize it, minimize it, etc. (Your program can do so, of course).
 *  <p>
 *  The "close" icon in the current implementation is an image
 *  loaded as a "resource" from the CLASSPATH. The file must be
 *  located at
 * <blockquote>
 * $CLASSPATH/images/8px.red.X.gif
 * </blockquote>
 * where <em>$CLASSPATH</em> is any directory on your CLASSPATH.
 *  If the class can't find the image file, it uses the character
 *  "X" instead.
 *  The main problem with this approach is that you can't change
 *  the color of the close icon to math the title-bar colors.
 *  Future versions of this class will fix the problem by rendering
 *  the image internally.
 */

public class PopupDialog extends JDialog {
    private Color TITLE_BAR_COLOR = ise.plugin.svn.gui.dateselector.Colors.LIGHT_YELLOW;
    private Color CLOSE_BOX_COLOR = ise.plugin.svn.gui.dateselector.Colors.DARK_RED;

    private JLabel title = new JLabel( "xxxxxxxxxxxxxx" );
    {
        title.setHorizontalAlignment( SwingConstants.CENTER );
        title.setOpaque( false );
        title.setFont( title.getFont().deriveFont( Font.BOLD ) );
    }

    private JPanel header = new JPanel();
    {
        header.setBackground( TITLE_BAR_COLOR );
        header.setLayout( new BorderLayout() );
        header.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
        header.add( title , BorderLayout.CENTER );
        header.add( create_close_button() , BorderLayout.EAST );
    }

    private JPanel content_pane = new JPanel();
    {
        content_pane.setLayout( new BorderLayout() );
    }

    public PopupDialog( Frame owner ) {
        super( owner );
        setModal( true );
    }
    public PopupDialog( Dialog owner ) {
        super( owner );
        setModal( true );
    }

    /* code common to all constructors */
    {
        init_dragable();

        setUndecorated( true );
        JPanel contents = new JPanel();
        contents.setBorder( BorderFactory.createLineBorder( Color.BLACK, 1 ) );
        contents.setLayout( new BorderLayout() );
        contents.add( header, BorderLayout.NORTH );
        contents.add( content_pane, BorderLayout.CENTER );
        contents.setBackground( Color.WHITE );

        setContentPane( contents ); // , BorderLayout.CENTER );
        setLocation( 100, 100 );
    }

    private JButton create_close_button() {
        URL image = getClass().getClassLoader().getResource(
                    "ise/plugin/svn/gui/dateselector/images/8px.red.X.gif" );

        JButton b = ( image != null ) ? new JButton( new ImageIcon( image ) )
                : new JButton( "  X  " )
                ;

        Border outer = BorderFactory.createLineBorder( CLOSE_BOX_COLOR, 1 );
        Border inner = BorderFactory.createEmptyBorder( 2, 2, 2, 2 );

        b.setBorder( BorderFactory.createCompoundBorder( outer, inner ) );

        b.setOpaque( false );
        b.addActionListener
        ( new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                  PopupDialog.this.setVisible( false );
                  PopupDialog.this.dispose();
              }
          }
        );

        b.setFocusable( false );
        return b;
    }

    /** Set the dialog title to the indicated text */
    public void setTitle( String text ) {
        title.setText( text );
    }

    //----------------------------------------------------------------------
    // Drag support.
    //
    private Point reference_position = new Point( 0, 0 );
    private MouseMotionListener movement_handler;
    private MouseListener click_handler;

    private void init_dragable() {
        movement_handler =
            new MouseMotionAdapter() {
                public void mouseDragged( MouseEvent e ) { // The reference posistion is the (window relative)
                    // cursor postion when the click occured. The
                    // current_mouse-position is mouse position
                    // now, and the deltas represent the disance
                    // moved.

                    Point current_mouse_position = e.getPoint();
                    Point current_window_location = getLocation();

                    int delta_x = current_mouse_position.x - reference_position.x;
                    int delta_y = current_mouse_position.y - reference_position.y;

                    // Move the window over by the computed delta. This move
                    // effectivly shifts the window-relative current-mouse
                    // position back to the original reference position.

                    current_window_location.translate( delta_x, delta_y );
                    setLocation( current_window_location );
                }
            };

        click_handler =
            new MouseAdapter() {
                public void mousePressed( MouseEvent e ) {
                    reference_position = e.getPoint(); // start of the drag
                }
            };

        setDragable( true );
    }

    /** Turn dragability on or off.
     */
    public void setDragable( boolean on ) {
        if ( on ) {
            title.addMouseMotionListener ( movement_handler );
            title.addMouseListener ( click_handler );
        }
        else {
            title.removeMouseMotionListener ( movement_handler );
            title.removeMouseListener ( click_handler );
        }
    }

    /** Add your widgets to the window returned by this method, in
     *  a manner similar to a JFrame. Do not modify the Poup_dialog
     *  itself. The returned container is a {@link JPanel JPanel}
     *  with a preinstalled {@link BorderLayout}.
     *  By default, it's colored colored dialog-box gray.
     *  @return the content pane.
     */
    public Container getContentPane() {
        return content_pane;
    }

    /** Change the color of the text and background in the title bar.
     *  The "close" icon is always
     *  {@linkplain ise.plugin.svn.gui.dateselector.Colors#DARK_RED dark red}
     *  so it will be hard to see if the background color is also
     *  a dark red).
     *  @param foreground the text color
     *  @param background the background color
     */
    public void change_titlebar_colors( Color foreground, Color background ) {
        title.setForeground ( foreground );
        header.setBackground( background );
    }

    //----------------------------------------------------------------------
    private static class Test {
        public static void main( String[] args ) {
            final JFrame main = new JFrame( "Hello" );
            final JDialog dialog = new PopupDialog( main );
            final JButton b = new JButton( "close" );

            b.addActionListener
            ( new ActionListener() {
                  public void actionPerformed( ActionEvent e ) {
                      dialog.setVisible( false );
                      dialog.dispose();
                  }
              }
            );
            main.getContentPane().add( new JLabel( "Main window" ) );
            main.pack();
            main.setVisible(true);

            System.out.println( "Creating dialog" );
            dialog.getContentPane().add( b );
            dialog.pack();

            System.out.println( "Displaying dialog" );
            dialog.setVisible(true);
            System.out.println( "Dialog shut down" );


            System.out.println( "Display nondragable in different colors" );

            PopupDialog d = ( PopupDialog ) dialog;
            d.change_titlebar_colors( Color.WHITE, Color.BLACK );
            d.setDragable( false );

            dialog.setVisible(true);
            System.exit( 0 );
        }
    }
}
