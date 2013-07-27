package ise.plugin.svn.gui.dateselector;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.awt.*;
import java.util.Date;
import java.util.Calendar;
import org.gjt.sp.jedit.jEdit;

/** A calendar-dispaly/date-selection widget.
 *  Here's what it looks like:
 *  <blockquote>
 * <img src="../../../images/DateSelector.gif">
 *  </blockquote>
 * "Today" is highlighted.
 * Select a date by clicking on it.
 * The background is transparant by default &mdash; it's grey here because
 * the underlying window is grey.
 * <p>
    <img src="../../../images/NavigableDateSelector.gif">
 *  This "raw" date selector can be "decorated" in several
 *  ways to make it more useful.
 *  First, you can add a navigation bar to the bottom
 * to advances the
 * calandar by one month (single arrow) or one year (double arrow)
 * forwards (right-pointing arrow) or backwards (left-pointing arrow).
 * "Today" is highlighted.
 * Navigation bars are specified using a Gang-of-Four "Decorator"
 * object that wraps the raw <code>DateSelectorPanel</code>
 * Both the wrapper and the underlying panel implement the
 * <code>DateSelectory</code> interface, so can be use
 * used interchangably. The following code creates the
 * date selector at right.
 * <pre>
 * DateSelector selector = new DateSelectorPanel();
 * selector = new NavigableDateSelector( selector );
 * </pre>
 * The same thing can be accomplished with a convenience constuctor that
 * creates the wrapped DateSelectorPanel for you:
 * <pre>
 * DateSelector selector = new NavigableDateSelector();
 * </pre>
 * <p>
 * <img src="../../../images/Titled_navigable_date_selector.gif">
 * The other augmentation of interest is a title that shows the
 *  month name and year that's displayed. (there's an example at right).
 *  Use the same decoration strategy as before to add the title:
 * <pre>
 * DateSelector selector = new DateSelectorPanel();
 * selector = new NavigableDateSelector( selector );
 * selector = new TitledDateSelector   ( selector );
 * </pre>
 * You can leave out the navigation bar by ommiting the
 * second line of the foregoing code.
 * Again, a convenience constructor is provided to create a
 * titled date selector (without the navigation bar) as follows:
 * <pre>
 * DateSelector selector = new TitledDateSelector();
 * </pre>
 * <p>
 * <img src="../../../images/DateSelectorDialog.gif">
 * The final variant is the lightweight popup dialog shown at right.
 * It can be dragged around by the title bar (though dragging can
 * be disabled) and closed by clicking on the "close" icon on the
 * upper right. As before, use a decorator to manufacture a dialog:
 * <pre>
 * DateSelector selector = new DateSelectorPanel();
 * selector = new NavigableDateSelector( selector ); // add navigation
 * selector = new DateSelectorDialog   ( selector );
 * </pre>
 * Note that you don't need a title because one is supplied for you
 * in the dialog-box title bar. Also as before, a convenience
 * constructor to create a navigable dialog box like the one at
 * right:
 * <pre>
 * DateSelector = new Date_selectcor_dialog();
 * <pre>
 * All the earlier examples create a claendar for the current
 * month. Several methods are provided, below, to change the date
 * in your program. For the most part, they work like simliar
 * methods of the {@link Calendar} class.
 * <DL>
 * <DT><b>Known Problems</b>
 * <DD>
 * The month names are hard coded (in English). Future versions
 * will load these strings from a resource bundle. The week layout
 * (S M T W Th F Sa Su) is the default layout for the underlying
 * {@link Calendar}, which should change with Locale as appropriate.
 * This feature has not been tested, however.
 * </DD>
 * </DL>
 *
 * @see ise.plugin.svn.gui.dateselector.DateSelector
 * @see ise.plugin.svn.gui.dateselector.DateSelectorDialog
 * @see ise.plugin.svn.gui.dateselector.NavigableDateSelector
 * @see ise.plugin.svn.gui.dateselector.TitledDateSelector
 */

public class DateSelectorPanel extends JPanel implements DateSelector {
    private String[] months =
        { jEdit.getProperty( "ips.Jan", "Jan" ), jEdit.getProperty( "ips.Feb", "Feb" ),
          jEdit.getProperty( "ips.Mar", "Mar" ), jEdit.getProperty( "ips.Apr", "Apr" ),
          jEdit.getProperty( "ips.May", "May" ), jEdit.getProperty( "ips.Jun", "Jun" ),
          jEdit.getProperty( "ips.Jul", "Jul" ), jEdit.getProperty( "ips.Aug", "Aug" ),
          jEdit.getProperty( "ips.Sep", "Sep" ), jEdit.getProperty( "ips.Oct", "Oct" ),
          jEdit.getProperty( "ips.Nov", "Nov" ), jEdit.getProperty( "ips.Dec", "Dec" )
        };

    private static final int DAYS_IN_WEEK = 7;  // days in a week
    private static final int MAX_WEEKS = 6; // maximum weeks in any month

    private Date selected = null;

    private Calendar calendar = Calendar.getInstance();
    {
        calendar.set( Calendar.HOUR, 0 );
        calendar.set( Calendar.MINUTE, 0 );
        calendar.set( Calendar.SECOND, 0 );
    }

    // The calendar that's displayed on the screen

    private final Calendar today = Calendar.getInstance();

    // An ActionListener that fields all events coming in from the
    // calendar
    //
    private final Button_handler day_listener = new Button_handler();

    // "days" is not a two-dimensional array. I drop buttons into
    // a gridLayout and let the layout manager worry about
    // what goes where. The first buttion is the first day of the
    // first week on the grid, the 8th button is the first day of the
    // second week of the grid, and so forth.

    private JButton[] days = new JButton[ DAYS_IN_WEEK * MAX_WEEKS ];
    {
        for ( int i = 0; i < days.length; i++ ) {
            JButton day = new JButton( "--" );
            days[ i ] = day;
            day.setBorder ( new EmptyBorder( 1, 2, 1, 2 ) );
            day.setFocusPainted ( false );
            day.setActionCommand ( "D" );
            day.addActionListener ( day_listener );
            day.setOpaque ( false );
        }
    }

    /** Create a DateSelector representing the current date.
     */
    public DateSelectorPanel() {
        JPanel calendar_display = new JPanel();
        calendar_display.setOpaque( false );
        calendar_display.setBorder( BorderFactory.createEmptyBorder( 5, 3, 0, 1 ) );
        calendar_display.setLayout( new GridLayout( MAX_WEEKS /*rows*/, DAYS_IN_WEEK /*columns*/ ) );

        for ( int i = 0; i < days.length; ++i )
            calendar_display.add( days[ i ] );

        setOpaque( false );
        setLayout( new BorderLayout() );
        add( calendar_display, BorderLayout.CENTER );
        updateCalendarDisplay();
    }

    /** Create a DateSelectorPanel for an arbitrary date.
     *  @param initial_date Calendar will display this date. The specified
     *       date is highlighted as "today".
     *  @see #DateSelectorPanel(int,int,int)
     */

    public DateSelectorPanel( Date initial_date ) {
        this();
        calendar.setTime( initial_date );
        today. setTime( initial_date );
        updateCalendarDisplay();
    }

    /** Create a DateSelectorPanel for an arbitrary date.
     * @param year the full year (e.g. 2003)
     * @param month the month id (0=january, 1=feb, etc. [this is the
     *    convention supported by the other date classes])
     * @param day the day of the month. This day will be highlighted
     *    as "today" on the displayed calendar. Use 0 to suppress
     *    the highlighting.
     *  @see #DateSelectorPanel(Date)
     */

    public DateSelectorPanel( int year, int month, int day ) {
        this();
        calendar.set( year, month, day );
        if ( day != 0 )
            today.set( year, month, day );
        updateCalendarDisplay();
    }

    /************************************************************************
     * List of observers.
     */

    private ActionListener subscribers = null;

    /** Add a listener that's notified when the user scrolls the
     *  selector or picks a date.
     *  @see DateSelector
     */
    public synchronized void addActionListener( ActionListener l ) {
        subscribers = AWTEventMulticaster.add( subscribers, l );
    }

    /** Remove a listener.
     *  @see DateSelector
     */
    public synchronized void removeActionListener( ActionListener l ) {
        subscribers = AWTEventMulticaster.remove( subscribers, l );
    }

    /** Notify the listeners of a scroll or select
     */
    private void fireActionEvent( int id, String command ) {
        if ( subscribers != null )
            subscribers.actionPerformed( new ActionEvent( this, id, command ) );
    }

    /***********************************************************************
     * Handle clicks from the buttons that represent calendar days.
     */
    private class Button_handler implements ActionListener {
        public void actionPerformed( ActionEvent e ) {
            if ( e.getActionCommand().equals( "D" ) ) {
                String text = ( ( JButton ) e.getSource() ).getText();

                if ( text.length() > 0 )     //  <=0 means click on blank square. Ignore.
                { calendar.set
                    ( calendar.get( Calendar.YEAR ),    // Reset the calendar
                      calendar.get( Calendar.MONTH ),    // to be the choosen
                      Integer.parseInt( text )      // date.
                    );
                    selected = calendar.getTime();
                    fireActionEvent( SELECT_ACTION, selected.toString() );
                }
            }
        }
    }

    //----------------------------------------------------------------------

    private JButton highlighted = null;

    private void clearHighlight() {
        if ( highlighted != null ) {
            highlighted.setBackground( Color.WHITE );
            highlighted.setForeground( Color.BLACK );
            highlighted.setOpaque( false );
            highlighted = null;
        }
    }

    private void highlight( JButton cell ) {
        highlighted = cell;
        cell.setBackground( ise.plugin.svn.gui.dateselector.Colors.DARK_RED );
        cell.setForeground( Color.WHITE );
        cell.setOpaque( true );
    }
    //----------------------------------------------------------------------

    /** Redraw the buttons that comprise the calandar to display the current month */

    private void updateCalendarDisplay() {
        try {
            setVisible( false ); // improves paint speed & reduces flicker

            clearHighlight();

            // The buttons that comprise the calendar are in a single
            // dimentioned array that was added to a 6x7 grid layout in
            // order. Because of the linear structure, it's easy to
            // lay out the calendar just by changing the labels on
            // the buttons. Here's the algorithm used below
            //
            //  1) find out the offset to the first day of the month.
            //  2) clear everything up to that offset
            //  3) add the days of the month
            //  4) clear everything else

            int month = calendar.get( Calendar.MONTH );
            int year = calendar.get( Calendar.YEAR );

            fireActionEvent( CHANGE_ACTION, months[ month ] + " " + year );

            calendar.set( year, month, 1 ); // first day of the current month.

            int first_day_offset = calendar.get( Calendar.DAY_OF_WEEK );  /* 1 */

            assert Calendar.SUNDAY == 0;
            assert first_day_offset < days.length;

            int i = 0;
            while ( i < first_day_offset - 1 )            /* 2 */
                days[ i++ ].setText( "" );

            int day_of_month = 1;
            for ( ; i < days.length; ++i )            /* 3 */
            {
                // Can't get calendar.equals(today) to work, so do it manually

                if ( calendar.get( Calendar.MONTH ) == today.get( Calendar.MONTH )
                        && calendar.get( Calendar.YEAR ) == today.get( Calendar.YEAR )
                        && calendar.get( Calendar.DATE ) == today.get( Calendar.DATE ) ) {
                    highlight( days[ i ] );
                }

                days[ i ].setText( String.valueOf( day_of_month ) );

                calendar.roll( Calendar.DATE,    /*up=*/ true ); // forward one day

                day_of_month = calendar.get( Calendar.DATE );
                if ( day_of_month == 1 )
                    break;
            }

            // Note that we break out of the previous loop with i positioned
            // at the last day we added, thus the following ++ *must* be a
            // preincrement becasue we want to start clearing at the cell
            // after that.

            while ( ++i < days.length )             /* 4 */
                days[ i ].setText( "" );

            setVisible( true );
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /** Create a naviagion button with an image appropriate to the caption.
     * The <code>caption</code> argument is used as the button's "action command."
     * This method is public only because it has to be. (It overrides a public
     * method.) Pretend it's not here.
     */

    public void addNotify() {
        super.addNotify();
        int month = calendar.get( Calendar.MONTH );
        int year = calendar.get( Calendar.YEAR );
        fireActionEvent( CHANGE_ACTION, months[ month ] + " " + year );
    }

    /** Returns the {@link Date Date} selected by the user or null if
     *  the window was closed without selecting a date. The returned
     *  Date has hours, minutes, and seconds values of 0.
     */

    public Date getSelectedDate() {
        return selected;
    }

    /** Returns the currently displayed {@link Date Date}.  */
    public Date getCurrentDate() {
        return calendar.getTime();
    }

    /** Works just like {@link Calendar#roll(int,boolean)}.  */
    public void roll( int field, boolean up ) {
        calendar.roll( field, up );
        updateCalendarDisplay();
    }

    /** Works just like {@link Calendar#roll(int,int)}.  */
    public void roll( int field, int amount ) {
        calendar.roll( field, amount );
        updateCalendarDisplay();
    }

    /** Works just like {@link Calendar#set(int,int,int)}
     * Sets "today" (which is higlighted) to the indicated day.
     */
    public void set( int year, int month, int date ) {
        calendar.set( year, month, date );
        today.set( year, month, date );
        updateCalendarDisplay();
    }

    /** Works just like {@link Calendar#get(int)} */
    public int get( int field ) {
        return calendar.get( field );
    }

    /** Works just like {@link Calendar#setTime(Date)},
     * Sets "today" (which is higlighted) to the indicated day.
     */
    public void setTime( Date d ) {
        calendar.setTime( d );
        today.setTime( d );
        updateCalendarDisplay();
    }

    /** Works just like {@link Calendar#getTime} */
    public Date getTime( ) {
        return calendar.getTime();
    }

    /** Return a Calendar object that represents the currently-displayed
     *  month and year. Modifying this object will not affect the
     *  current panel.
     *  @return a Calendar representing the panel's state.
     */

    public Calendar getCalendar() {
        Calendar c = Calendar.getInstance();
        c.setTime( calendar.getTime() );
        return c;
    }

    /** Change the display to match the indicated calendar. This Calendar
     *  argument is used only to provide the new date/time information.
     *  Modifying it after a call to the current method will not affect
     *  the DateSelectorPanel at all.
     * Sets "today" (which is higlighted) to the indicated day.
     *  @param calendar A calendar positioned t the date to display.
     */

    public void setFromCalendar( Calendar calendar ) {
        this.calendar.setTime( calendar.getTime() );
        today.setTime( calendar.getTime() );
        updateCalendarDisplay();
    }
    //----------------------------------------------------------------------
    private static class Test {
        public static void main( String[] args ) {
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
            frame.getContentPane().setLayout( new FlowLayout() );

            DateSelector left = new TitledDateSelector( new NavigableDateSelector() );
            DateSelector center = new NavigableDateSelector();
            DateSelector right = new DateSelectorPanel( 1900, 1, 2 );

            ( ( NavigableDateSelector ) center ).changeNavigationBarColor( null ); // transparent

            ActionListener l =
                new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        System.out.println( e.getActionCommand() );
                    }
                };

            left.addActionListener ( l );
            center.addActionListener( l );
            right.addActionListener ( l );

            JPanel white = new JPanel();    // proove that it's transparent.
            white.setBackground( Color.WHITE );
            white.add( ( JPanel ) center );

            frame.getContentPane().add( ( JPanel ) left ); // I hate these casts, but they're
            frame.getContentPane().add( white ); // mandated by the fact that
            frame.getContentPane().add( ( JPanel ) right ); // Component is not an interface.

            frame.pack();
            frame.setVisible( true );
        }
    }
}