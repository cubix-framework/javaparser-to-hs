/*
* Copyright (c) 2008, Dale Anson
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

package ise.plugin.svn.gui.component;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.*;

import ise.java.awt.KappaLayout;
import ise.plugin.svn.gui.dateselector.*;
import ise.plugin.svn.library.GUIUtils;

import org.tmatesoft.svn.core.wc.SVNRevision;

import org.gjt.sp.jedit.jEdit;

/**
 * Basic implementation of the UI for the RevisionSelectionPanel.  Look and feels
 * should subclass as appropriate.  This class handles the actual layout of the
 * panel.  This is the "V" in MVC.
 */
public class BasicRevisionSelectionPanelUI extends RevisionSelectionPanelUI implements ChangeListener, PropertyChangeListener {

    private RevisionSelectionPanel controller;
    private RenderPane renderPane;

    private static Color background = jEdit.getColorProperty( "view.bgColor", Color.WHITE );
    private static Color foreground = jEdit.getColorProperty( "view.fgColor", Color.BLACK );


    public static ComponentUI createUI( JComponent c ) {
        return new BasicRevisionSelectionPanelUI();
    }

    public void installUI( JComponent c ) {
        controller = ( RevisionSelectionPanel ) c;
        controller.setLayout( createLayoutManager() );
        installDefaults();
        installComponents();
        installListeners();
    }

    /**
     * Install default values for colors, fonts, borders, etc.
     */
    public void installDefaults() {
        controller.setLayout( createLayoutManager() );
        controller.setBorder( BorderFactory.createTitledBorder( BorderFactory.createEtchedBorder(), controller.getModel().getTitle() ) );
    }

    /**
     * Create and install any sub-components.
     */
    public void installComponents() {
        renderPane = new RenderPane();
        controller.add( renderPane, BorderLayout.CENTER );
    }

    /**
     * Install any action listeners, mouse listeners, etc.
     */
    public void installListeners() {
        controller.addChangeListener( this );
        controller.addPropertyChangeListener( this );
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallUI( JComponent c ) {
        c.setLayout( null );
        uninstallListeners();
        uninstallComponents();
        uninstallDefaults();
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallDefaults() {}

    /**
     * Tear down and clean up.
     */
    public void uninstallComponents() {
        controller.removeAll();
    }

    /**
     * Tear down and clean up.
     */
    public void uninstallListeners() {
        controller.removeChangeListener( this );
    }

    public void stateChanged( ChangeEvent event ) {
        uninstallComponents();
        installComponents();
        renderPane.setEnabled( controller.getModel().isEnabled() );
        controller.invalidate();
        controller.validate();
    }

    public void propertyChange( PropertyChangeEvent pce ) {
        if ( pce == null ) {
            return ;
        }
        if ( equals( pce.getSource() ) ) {
            // avoid endless recursion
            return ;
        }
        renderPane.propertyChange( pce );
    }


    protected LayoutManager createLayoutManager() {
        return new BorderLayout();
    }

    public class RenderPane extends JPanel {

        private ButtonGroup bg = null;
        private JRadioButton head_rb = new JRadioButton( jEdit.getProperty( "ips.HEAD", "HEAD" ) );
        private JRadioButton base_rb = new JRadioButton( jEdit.getProperty( "ips.BASE", "BASE" ) );
        private JRadioButton working_rb = new JRadioButton( jEdit.getProperty( "ips.WORKING", "WORKING" ) );
        private JRadioButton revision_number_rb = new JRadioButton( jEdit.getProperty( "ips.Revision", "Revision" ) + ":" );
        private RevisionTextField revision_number = null;
        private JRadioButton date_rb = new JRadioButton( jEdit.getProperty( "ips.Date", "Date" ) + ":" );
        private JSpinner date_spinner = null;
        private JButton date_popup = null;

        public RenderPane() {
            initUI();
            initListeners();
        }

        private void initUI() {
            setLayout( new KappaLayout() );
            bg = new ButtonGroup();
            bg.add( revision_number_rb );
            bg.add( date_rb );
            bg.add( head_rb );
            bg.add( base_rb );
            bg.add( working_rb );
            
            // set up the revision number entry field
            revision_number = new RevisionTextField();
            revision_number.setText( "0" );
            revision_number.setRevisionModel( controller.getModel() );
            revision_number.setHorizontalAlignment( JTextField.RIGHT );
            revision_number.setPreferredSize( new Dimension( 150, revision_number.getPreferredSize().height ) );
            revision_number.setForeground( foreground );
            revision_number.setBackground( background );
            revision_number.setEnabled( false );
            revision_number.getDocument().addDocumentListener(
                // notify listeners of changes to the revision, this allows another
                // RevisionSelectionPanel to synchronize its revision field to this
                // revision field
                new DocumentListener() {
                    public void changedUpdate( DocumentEvent de ) {
                        changed( de );
                    }

                    public void insertUpdate( DocumentEvent de ) {
                        changed( de );
                    }

                    public void removeUpdate( DocumentEvent de ) {
                        changed( de );
                    }

                    private void changed( DocumentEvent de ) {
                        PropertyChangeEvent pce = new PropertyChangeEvent( BasicRevisionSelectionPanelUI.this, "revision", null, SVNRevision.parse( revision_number.getText() ) );
                        controller.firePropertyChanged( pce );
                    }
                }
            );

            // set up date chooser
            date_spinner = getDateChooser();
            ImageIcon icon = new ImageIcon( RevisionSelectionPanel.class.getClassLoader().getResource( "ise/plugin/svn/gui/dateselector/images/10px.calendar.icon.gif" ) );
            date_popup = icon == null ? new JButton( "D" ) : new JButton( icon );
            date_popup.setMargin( new Insets( 1, 1, 1, 1 ) );

            if ( controller.getModel().getDirection() == SwingConstants.HORIZONTAL ) {
                add( "0, 0, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 6, true ) );
                if ( controller.getModel().getShowHead() ) {
                    add( "0, 1, 1, 1, W, , 3", head_rb );
                }
                if ( controller.getModel().getShowBase() ) {
                    add( "0, 2, 1, 1, W, , 3", base_rb );
                }
                if ( controller.getModel().getShowWorking() ) {
                    add( "0, 3, 1, 1, W, , 3", working_rb );
                }
                add( "1, 1, 1, 1, 0,  , 0", KappaLayout.createHorizontalStrut( 21, true ) );
                if ( controller.getModel().getShowNumber() ) {
                    add( "2, 1, 1, 1, W, , 3", revision_number_rb );
                    add( "3, 1, 1, 1, W, , 3", revision_number );
                }
                if ( controller.getModel().getShowDate() ) {
                    add( "2, 2, 1, 1, W, , 3", date_rb );
                    add( "3, 2, 1, 1, W, , 3", date_spinner );
                    add( "4, 2, 1, 1,  , , 3", date_popup );
                }
            }
            else {
                add( "0, 0, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 6, true ) );
                if ( controller.getModel().getShowHead() ) {
                    add( "0, 1, 2, 1, W, , 3", head_rb );
                }
                if ( controller.getModel().getShowBase() ) {
                    add( "0, 2, 2, 1, W, , 3", base_rb );
                }
                if ( controller.getModel().getShowWorking() ) {
                    add( "0, 3, 2, 1, W, , 3", working_rb );
                }
                if ( controller.getModel().getShowNumber() ) {
                    add( "0, 4, 1, 1, W, w, 3", revision_number_rb );
                    add( "1, 4, 1, 1, W, , 3", revision_number );
                }
                if ( controller.getModel().getShowDate() ) {
                    add( "0, 5, 1, 1, W, , 3", date_rb );
                    add( "1, 5, 1, 1, W, , 3", date_spinner );
                    add( "2, 5, 1, 1,  , , 3", date_popup );
                }
            }
            
            adjustRadioButtons();

            controller.repaint();
        }

        private void initListeners() {
            ActionListener al = new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            revision_number.setEnabled ( revision_number_rb.isSelected() );
                            date_spinner.setEnabled ( date_rb.isSelected() );
                            date_popup.setEnabled( date_rb.isSelected() );
                        }
                    };

            revision_number_rb.addActionListener( al );
            date_rb.addActionListener( al );
            head_rb.addActionListener( al );
            base_rb.addActionListener( al );
            if ( controller.getModel().getShowWorking() ) {
                working_rb.addActionListener( al );
            }

            revision_number_rb.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        RenderPane.this.revision_number.setEnabled( revision_number_rb.isSelected() );
                        if ( revision_number.isEnabled() ) {
                            Number number = Integer.parseInt( revision_number.getText() );
                            controller.getModel().setRevision( SVNRevision.create( number.longValue() ) );
                        }
                    }
                }
            );
            date_rb.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        date_spinner.setEnabled( date_rb.isSelected() );
                        date_popup.setEnabled( date_rb.isSelected() );
                        if ( date_spinner.isEnabled() ) {
                            Date date = ( Date ) date_spinner.getValue();
                            controller.getModel().setRevision( SVNRevision.create( date ) );
                        }
                    }
                }
            );
            head_rb.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        controller.getModel().setRevision( SVNRevision.HEAD );
                    }
                }
            );
            base_rb.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        controller.getModel().setRevision( SVNRevision.BASE );
                    }
                }
            );
            if ( controller.getModel().getShowWorking() ) {
                working_rb.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            controller.getModel().setRevision( SVNRevision.WORKING );
                        }
                    }
                );
            }

            date_popup.addActionListener(
                new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        Dialog parent = GUIUtils.getParentDialog( controller );
                        final DateSelectorDialog dsd = new DateSelectorDialog( parent );
                        dsd.setLocation( new Point( parent.getLocation().x + date_popup.getLocation().x, parent.getLocation().y + date_popup.getLocation().y ) );
                        dsd.addActionListener(
                            new ActionListener() {
                                public void actionPerformed( ActionEvent ae ) {
                                    Date date = dsd.getSelectedDate();
                                    if ( date != null ) {
                                        date_spinner.getModel().setValue( date );
                                    }
                                }
                            }
                        );
                        dsd.setVisible( true );
                    }
                }
            );
        }

        private JSpinner getDateChooser() {
            Calendar calendar = Calendar.getInstance();
            Date initDate = calendar.getTime();
            Date latestDate = calendar.getTime();
            calendar.add( Calendar.YEAR, -10 );
            Date earliestDate = calendar.getTime();
            SpinnerDateModel model = new SpinnerDateModel( initDate, earliestDate, latestDate, Calendar.DAY_OF_MONTH );
            date_spinner = new JSpinner( model ) {
                        public void processFocusEvent( FocusEvent fe ) {
                            try {
                                commitEdit();
                            }
                            catch ( java.text.ParseException pe ) {}    // NOPMD
                        }
                    }
                    ;
            JSpinner.DateEditor date_editor = new JSpinner.DateEditor( date_spinner, jEdit.getProperty( "ips.DateFormat", "dd MMM yyyy HH:mm" ) );
            date_editor.getTextField().addPropertyChangeListener( new PropertyChangeListener() {
                        // this is necessary because JSpinner won't automatically keep its internal
                        // model up to date.
                        public void propertyChange( PropertyChangeEvent pce ) {
                            try {
                                date_spinner.commitEdit();
                            }
                            catch ( java.text.ParseException pe ) {}    // NOPMD
                        }
                    }
                                                                );
            date_spinner.setEditor( date_editor );
            date_spinner.addChangeListener( new ChangeListener() {
                        public void stateChanged( ChangeEvent ce ) {
                            if ( date_spinner.isEnabled() ) {
                                Date date = ( Date ) date_spinner.getValue();
                                controller.getModel().setRevision( SVNRevision.create( date ) );
                            }
                        }
                    }
                                          );
            date_spinner.setPreferredSize( new Dimension( 150, date_spinner.getPreferredSize().height ) );
            date_spinner.setForeground( foreground );
            date_spinner.setBackground( background );
            date_spinner.setEnabled( false );
            return date_spinner;
        }
        
        private void adjustRadioButtons() {
                SVNRevision newRevision = controller.getModel().getRevision();

                if ( controller.getModel().getShowHead() && SVNRevision.HEAD.equals( newRevision ) ) {
                    head_rb.setSelected( true );
                }
                else if ( controller.getModel().getShowBase() && SVNRevision.BASE.equals( newRevision ) ) {
                    base_rb.setSelected( true );
                }
                else if ( controller.getModel().getShowWorking() && SVNRevision.WORKING.equals( newRevision ) ) {
                    working_rb.setSelected( true );
                }
                else if ( newRevision.getDate() != null ) {
                    date_rb.setSelected( true );
                    date_spinner.setValue( newRevision.getDate() );
                }
                else {
                    long number = newRevision.getNumber();
                    if ( number == -1 ) {
                        number = 0;
                    }
                    revision_number_rb.setSelected( true );
                    revision_number.setText( String.valueOf( number ) );
                }
                repaint();
        }

        public void propertyChange( PropertyChangeEvent pce ) {
            if ( "revision".equals( pce.getPropertyName() ) ) {
                adjustRadioButtons();
            }
        }

        public void setEnabled( boolean b ) {
            head_rb.setEnabled( b );
            base_rb.setEnabled( b );
            revision_number_rb.setEnabled( b );
            date_rb.setEnabled( b );
            working_rb.setEnabled( b );
            revision_number.setEnabled( revision_number_rb.isSelected() );
            date_spinner.setEnabled( date_rb.isSelected() );
            date_popup.setEnabled( date_rb.isSelected() );
            repaint();
        }
    }

}