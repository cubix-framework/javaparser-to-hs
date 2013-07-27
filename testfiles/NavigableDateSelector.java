package ise.plugin.svn.gui.dateselector;


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Date;
import java.util.Calendar;
import java.net.URL;

/** This class is wrapper for a {@link DateSelector} that adds a
 *  navigation bar to manipulate the wrapped selector.
 *  See {@link DateSelectorPanel} for a description and picture
 *  of date selectors.
 * <DL>
 * <DT><b>Images</b>
 * <DD>
 * <P>
 *  The navigation-bar arrows in the current implementation are images
 *  loaded as a "resource" from the CLASSPATH. Four files are used:
 * <blockquote>
 * $CLASSPATH/images/10px.red.arrow.right.double.gif<br>
 * $CLASSPATH/images/10px.red.arrow.left.double.gif<br>
 * $CLASSPATH/images/10px.red.arrow.right.gif<br>
 * $CLASSPATH/images/10px.red.arrow.left.gif
 * </blockquote>
 * where <em>$CLASSPATH</em> is any directory on your CLASSPATH.
 *  If the <code>DateSelectorPanel</code>
 *  can't find the image file, it uses character representations
 *  (<code>"&gt;"</code>, <code>"&gt;&gt;"</code>,
 *  <code>"&lt;"</code>, <code>"&lt;&lt;"</code>).
 *  The main problem with this approach is that you can't change
 *  the color of the arrows without changing the image files. On
 *  the plus side, arbitrary images can be used for the movement
 *  icons.
 *  Future versions of this class will provide some way for you
 *  to specify that the arrows be rendered internally in colors
 *  that you specify at run time.
 * </DD>
 * </DL>
 *  @see DateSelector
 *  @see DateSelectorPanel
 *  @see DateSelectorDialog
 *  @see TitledDateSelector
 */

public class NavigableDateSelector extends JPanel implements DateSelector {
    private DateSelector selector;

    // Names of images files used for the navigator bar.
    private static final String NEXT_YEAR = "ise/plugin/svn/gui/dateselector/images/10px.red.arrow.right.double.gif";
    private static final String NEXT_MONTH = "ise/plugin/svn/gui/dateselector/images/10px.red.arrow.right.gif";
    private static final String PREVIOUS_YEAR = "ise/plugin/svn/gui/dateselector/images/10px.red.arrow.left.double.gif";
    private static final String PREVIOUS_MONTH = "ise/plugin/svn/gui/dateselector/images/10px.red.arrow.left.gif";

    // These constants are used both to identify the button, and
    // as the button caption in the event that the appropriate
    // immage file can't be located.

    private static final String FORWARD_MONTH = ">" ;
    private static final String FORWARD_YEAR = ">>" ;
    private static final String BACK_MONTH = "<" ;
    private static final String BACK_YEAR = "<<" ;


    private JPanel navigation = null;

    /** Wrap an existing DateSelector to add a a navigation bar
     *  modifies the wrapped DateSelector.
     */

    public NavigableDateSelector( DateSelector selector ) {
        this.selector = selector;
        setBorder( null );
        setOpaque( false );
        setLayout( new BorderLayout() );
        add( ( JPanel ) selector, BorderLayout.CENTER );

        navigation = new JPanel();
        navigation.setLayout( new FlowLayout() );
        navigation.setBorder( null );
        navigation.setBackground( ise.plugin.svn.gui.dateselector.Colors.LIGHT_YELLOW );
        navigation.add( makeNavigationButton( BACK_YEAR ) );
        navigation.add( makeNavigationButton( BACK_MONTH ) );
        navigation.add( makeNavigationButton( FORWARD_MONTH ) );
        navigation.add( makeNavigationButton( FORWARD_YEAR ) );

        add( navigation, BorderLayout.SOUTH );
    }
    /**
     * Create a navigable date selector by wrapping the indicated one.
     * @param selector the raw date selector to wrap;
     * @param background_color the background color of the navigation
     *   bar (or null for transparent). The default color is
     *   {@link ise.plugin.svn.gui.dateselector.Colors#LIGHT_YELLOW}.
     * @see #setBackground
     */

    public NavigableDateSelector( DateSelector selector, Color background_color ) {
        this( selector );
        navigation.setBackground( background_color );
    }

    /** Convenience constructor. Creates the wrapped DateSelector
     *  for you. (It creates a {@link DateSelectorPanel} using
     *  the no-arg constructor.
     */

    public NavigableDateSelector() {
        this( new DateSelectorPanel() );
    }

    public void changeNavigationBarColor( Color background_color ) {
        if ( background_color != null )
            navigation.setBackground( background_color );
        else
            navigation.setOpaque( false );
    }

    private final NavigationHandler navigation_listener = new NavigationHandler();

    /** Handle clicks from the navigation-bar buttons. */

    private class NavigationHandler implements ActionListener {
        public void actionPerformed( ActionEvent e ) {
            String direction = e.getActionCommand();

            if ( direction == FORWARD_YEAR )
                selector.roll( Calendar.YEAR, true );
            else if ( direction == BACK_YEAR )
                selector.roll( Calendar.YEAR, false );
            else if ( direction == FORWARD_MONTH ) {
                selector.roll( Calendar.MONTH, true );
                if ( selector.get( Calendar.MONTH ) == Calendar.JANUARY )
                    selector.roll( Calendar.YEAR, true );
            }
            else if ( direction == BACK_MONTH ) {
                selector.roll( Calendar.MONTH, false );
                if ( selector.get( Calendar.MONTH ) == Calendar.DECEMBER )
                    selector.roll( Calendar.YEAR, false );
            }
            else {
            assert false: "Unexpected direction"
                ;
            }
        }
    }

    private JButton makeNavigationButton( String caption ) {
        ClassLoader loader = getClass().getClassLoader();
        URL image =
            ( caption == FORWARD_YEAR ) ? loader.getResource( NEXT_YEAR ) :
            ( caption == BACK_YEAR ) ? loader.getResource( PREVIOUS_YEAR ) :
            ( caption == FORWARD_MONTH ) ? loader.getResource( NEXT_MONTH ) :
            loader.getResource( PREVIOUS_MONTH ) ;

        JButton b = ( image != null ) ? new JButton( new ImageIcon( image ) )
                : new JButton( caption )
                ;
        b.setBorder( new EmptyBorder( 0, 4, 0, 4 ) );
        b.setFocusPainted( false );
        b.setActionCommand( caption );
        b.addActionListener( navigation_listener );
        b.setOpaque( false );
        return b;
    }

    public synchronized void addActionListener( ActionListener l ) {
        selector.addActionListener( l );
    }
    public synchronized void removeActionListener( ActionListener l ) {
        selector.removeActionListener( l );
    }
    public Date getSelectedDate() {
        return selector.getSelectedDate();
    }
    public Date getCurrentDate() {
        return selector.getCurrentDate();
    }
    public void roll( int f, boolean up ) {
        selector.roll( f, up );
    }
    public int get( int f ) {
        return selector.get( f );
    }
}
