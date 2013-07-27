/*
* JDiffOptionPane.java
* Copyright (c) 2000, 2001, 2002 Andre Kaplan
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


package jdiff.options;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JPanel;

import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.jEdit;

import jdiff.component.MergeToolBar;

// from CommonControls plugin
import ise.java.awt.KappaLayout;

/**
 * Handles the GUI options for JDiff.
 */
public class JDiffGUIOptionPane implements OptionPane {

    private JPanel panel = null;

    // ui options
    private JCheckBox autoShowDockable;
    private JCheckBox beepOnError;
    private JCheckBox showLineDiff;
    private JCheckBox restoreView;
    private JCheckBox restoreCaret;
    private JCheckBox synchroScroll;
    private JCheckBox horizScroll;
    private JCheckBox selectWord;

    // dockable ui options
    private JRadioButton horizontal;
    private JRadioButton vertical;
    private JRadioButton compact;

    /**
     * @return the name of this panel
     */
    public String getName() {
        return "jdiff.gui";
    }

    /**
     * @return the panel to display the general options for JDiff
     */
    public Component getComponent() {
        if ( panel == null ) {
            createPanel();
        }
        return panel;
    }

    /**
     * Initialize the panel, create the components and apply current settings.
     */
    public void init() {
        if ( panel == null ) {
            createPanel();
        }
        autoShowDockable.setSelected( jEdit.getBooleanProperty( "jdiff.auto-show-dockable", false ) );
        showLineDiff.setSelected( jEdit.getBooleanProperty( "jdiff.show-line-diff", true ) );
        synchroScroll.setSelected( jEdit.getBooleanProperty( "jdiff.synchroscroll-on", true ) );
        horizScroll.setSelected( jEdit.getBooleanProperty( "jdiff.horiz-scroll", false ) );
        selectWord.setSelected( jEdit.getBooleanProperty( "jdiff.select-word", false ) );
        selectWord.setEnabled( horizScroll.isSelected() );
        beepOnError.setSelected( jEdit.getBooleanProperty( "jdiff.beep-on-error", true ) );
        restoreView.setSelected( jEdit.getBooleanProperty( "jdiff.restore-view", true ) );
        restoreCaret.setSelected( jEdit.getBooleanProperty( "jdiff.restore-caret", true ) );

        int orientation = jEdit.getIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.HORIZONTAL );
        switch ( orientation ) {
            case MergeToolBar.VERTICAL:
                horizontal.setSelected( false );
                vertical.setSelected( true );
                compact.setSelected( false );
                break;
            case MergeToolBar.COMPACT:
                horizontal.setSelected( false );
                vertical.setSelected( false );
                compact.setSelected( true );
                break;
            default:
                horizontal.setSelected( true );
                vertical.setSelected( false );
                compact.setSelected( false );
                break;
        }
    }

    // actually create and layout the option panel here
    private void createPanel() {
        // UI options
        JLabel ui_options_label = new JLabel( "<html><b>" + jEdit.getProperty( "options.ui-options.label", "UI Options:" ) + "</b>" );
        autoShowDockable = createCheckBox( "jdiff.auto-show-dockable", false );
        showLineDiff = createCheckBox( "jdiff.show-line-diff", true );
        synchroScroll = createCheckBox( "jdiff.synchroscroll-on", true );
        horizScroll = createCheckBox( "jdiff.horiz-scroll", false );
        selectWord = createCheckBox( "jdiff.select-word", false );
        selectWord.setEnabled( horizScroll.isSelected() );
        beepOnError = createCheckBox( "jdiff.beep-on-error", true );
        restoreView = createCheckBox( "jdiff.restore-view", true );
        restoreCaret = createCheckBox( "jdiff.restore-caret", true );

        // only enable selectWord if horizScroll is selected
        horizScroll.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    boolean selected = ( ( JCheckBox ) ae.getSource() ).isSelected();
                    selectWord.setEnabled( selected );
                    selectWord.setSelected( selected );
                }
            }
        );

        // dockable UI options
        int orientation = jEdit.getIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.HORIZONTAL );
        horizontal = new JRadioButton( jEdit.getProperty( "options.jdiff.toolbar-horizontal" ) );
        vertical = new JRadioButton( jEdit.getProperty( "options.jdiff.toolbar-vertical" ) );
        compact = new JRadioButton( jEdit.getProperty( "options.jdiff.toolbar-compact" ) );

        ButtonGroup button_group = new ButtonGroup();
        button_group.add( horizontal );
        button_group.add( vertical );
        button_group.add( compact );
        switch ( orientation ) {
            case MergeToolBar.VERTICAL:
                horizontal.setSelected( false );
                vertical.setSelected( true );
                compact.setSelected( false );
                break;
            case MergeToolBar.COMPACT:
                horizontal.setSelected( false );
                vertical.setSelected( false );
                compact.setSelected( true );
                break;
            default:
                horizontal.setSelected( true );
                vertical.setSelected( false );
                compact.setSelected( false );
                break;
        }
        JLabel orientation_label = new JLabel( "<html><b>" + jEdit.getProperty( "options.toolbar-orientation.label", "Merge Toolbar Orientation:" ) + "</b>");

        panel = new JPanel();
        panel.setLayout( new KappaLayout() );
        panel.setBorder( BorderFactory.createEmptyBorder( 12, 11, 11, 12 ) );
        panel.add( "0,  9, 2, 1, W, 0, 2", ui_options_label );
        panel.add( "0, 10, 2, 1, W, 0, 2", autoShowDockable );
        panel.add( "0, 11, 2, 1, W, 0, 2", showLineDiff );
        panel.add( "0, 12, 2, 1, W, 0, 2", synchroScroll );
        panel.add( "0, 13, 2, 1, W, 0, 2", horizScroll );
        panel.add( "0, 14, 1, 1, W, 0, 2", KappaLayout.createHorizontalStrut( 16, true ) );
        panel.add( "1, 15, 1, 1, W, 0, 2", selectWord );
        panel.add( "0, 16, 2, 1, W, 0, 2", beepOnError );
        panel.add( "0, 17, 2, 1, W, 0, 2", restoreView );
        panel.add( "0, 18, 2, 1, W, 0, 2", restoreCaret );

        panel.add( "0, 19, 2, 1, W, 0, 2", KappaLayout.createVerticalStrut( 11 ) );
        panel.add( "0, 20, 2, 1, W, 0, 2", orientation_label );
        panel.add( "0, 21, 2, 1, W, 0, 2", horizontal );
        panel.add( "0, 22, 2, 1, W, 0, 2", vertical );
        panel.add( "0, 23, 2, 1, W, 0, 2", compact );
    }

    /**
     * Save the user settings for the general options.
     */
    public void save() {
        if ( vertical.isSelected() ) {
            jEdit.setIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.VERTICAL );
        }
        else if ( compact.isSelected() ) {
            jEdit.setIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.COMPACT );
        }
        else {
            jEdit.setIntegerProperty( "jdiff.toolbar-orientation", MergeToolBar.HORIZONTAL );

        }

        jEdit.setBooleanProperty( "jdiff.auto-show-dockable", autoShowDockable.isSelected() );
        jEdit.setBooleanProperty( "jdiff.show-line-diff", showLineDiff.isSelected() );
        jEdit.setBooleanProperty( "jdiff.synchroscroll-on", synchroScroll.isSelected() );
        jEdit.setBooleanProperty( "jdiff.horiz-scroll", horizScroll.isSelected() );
        jEdit.setBooleanProperty( "jdiff.select-word", selectWord.isSelected() );
        jEdit.setBooleanProperty( "jdiff.beep-on-error", beepOnError.isSelected() );
        jEdit.setBooleanProperty( "jdiff.restore-view", restoreView.isSelected() );
        jEdit.setBooleanProperty( "jdiff.restore-caret", restoreCaret.isSelected() );

        // virtual overview has been removed, since it hasn't worked since jEdit 4.2,
        // so make sure the property is false
        jEdit.setBooleanProperty( "jdiff.global-virtual-overview", false );
    }


    private JCheckBox createCheckBox( String property, boolean defaultValue ) {
        JCheckBox cb = new JCheckBox( jEdit.getProperty( "options." + property ) );
        cb.setSelected( jEdit.getBooleanProperty( property, defaultValue ) );
        return cb;
    }
}