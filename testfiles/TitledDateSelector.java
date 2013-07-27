package ise.plugin.svn.gui.dateselector;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.util.Date;

/************************************************************************
 *  This class is a GoF "Decorator" that augements the "raw"
 *  </code>DateSelectorPanel</code> with
 *  a title that displays the month name and year.
 *  The title updates automatically as the user navigates.
 *  Here's a picture:
 *  <blockquote>
 * <img style="border: 0 0 0 0;" src="../../../images/DateSelectorPanel.gif">
 *  </blockquote>
 *  Create a titled date selector like this:
 *  <pre>
 *  DateSelector selector = new DateSelectorPanel(); // or other constructor.
 *  selector = new TitledDateSelector(selector);
 *  </pre>
 *  This wrapper absorbs the {@link DateSelector#CHANGE_ACTION}
 *  events: listeners that you register on the wrapper will be sent
 *  only {@link DateSelector#SELECT_ACTION} events.
 *  (Listeners that are registered on the wrapped
 *  <code>DateSelector</code> object will be notified of all events,
 *  however.
 *
 *  @see DateSelector
 *  @see DateSelectorPanel
 *  @see DateSelectorDialog
 *  @see NavigableDateSelector
 */

public class TitledDateSelector extends JPanel implements DateSelector {
    private DateSelector selector;
    private final JLabel title = new JLabel( "XXXX" );

    /** Wrap an existing DateSelector to add a title bar showing
     *  the displayed month and year. The title changes as the
     *  user navigates.
     */

    public TitledDateSelector( DateSelector selector ) {
        this.selector = selector;

        title.setHorizontalAlignment( SwingConstants.CENTER );
        title.setOpaque ( true );
        title.setBackground ( ise.plugin.svn.gui.dateselector.Colors.LIGHT_YELLOW );
        title.setFont ( title.getFont().deriveFont( Font.BOLD ) );

        selector.addActionListener
        ( new ActionListener() {
              public void actionPerformed( ActionEvent e ) {
                  if ( e.getID() == DateSelectorPanel.CHANGE_ACTION )
                      title.setText( e.getActionCommand() );
                  else
                      my_subscribers.actionPerformed( e );
              }
          }
        );

        setOpaque( false );
        setLayout( new BorderLayout() );
        add( title, BorderLayout.NORTH );
        add( ( JPanel ) selector, BorderLayout.CENTER );
    }

    /** This constructor lets you specify the background color of the
     *  title strip that holds the month name and year (the default
     *  is light yellow).
     *
     *  @param label_background_color the color of the title bar, or
     *   null to make it transparent.
     */
    public TitledDateSelector( DateSelector selector, Color label_background_color ) {
        this( selector );
        if ( label_background_color == null )
            title.setOpaque( false );
        else
            title.setBackground( label_background_color );
    }

    private ActionListener my_subscribers = null;
    public synchronized void addActionListener( ActionListener l ) {
        my_subscribers = AWTEventMulticaster.add( my_subscribers, l );
    }
    public synchronized void removeActionListener( ActionListener l ) {
        my_subscribers = AWTEventMulticaster.remove( my_subscribers, l );
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
