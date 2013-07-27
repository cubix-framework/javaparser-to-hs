/**
* PyLintConfigurationPanel.java
*
* Copyright (C) 2005,2006 Jean-Yves Mengant
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

package org.jymc.jpydebug.swing.ui;
import java.awt.BorderLayout;
import org.jymc.jpydebug.utils.FileChooserPane;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import org.jymc.jpydebug.PythonDebugParameters;


/**
 * Cross IDE Pylint Configuration pane
 * @author jean-yves
 */
public class PyLintConfigurationPanel 
extends BasicConfigurationPanel
{

  public final static String LOCALPYLINT_PROPERTY =
    "jpydebug-dbgoptions.pylint";

  public final static String  USEPYLINT_PROPERTY         =
    "jpydebug-pylint.usepylint";
  public final static String USEPYLINT_PROPERTY_LABEL =
    "options.jpydebug-pylint.usepylint";
  private final static String _USEPYLINT_PROPERTY_LABEL_DEFAULT_ =
    "Use pylint";

  public final static String  PYLINT_LOCATION_PROPERTY         =
    "jpydebug-pylint.location";
  public final static String PYLINT_LOCATION_PROPERTY_LABEL =
    "options.jpydebug-pylint.location";
  private final static String _PYLINT_LOCATION_PROPERTY_LABEL_DEFAULT_ =
    "Pylint location(lint.py)";

  public final static String  PYLINT_FATAL_PROPERTY         =
    "jpydebug-pylint.fatal";
  public final static String PYLINT_FATAL_PROPERTY_LABEL =
    "options.jpydebug-pylint.fatal";
  private final static String _PYLINT_FATAL_PROPERTY_LABEL_DEFAULT_ =
    "Communicate FATAL";

  public final static String  PYLINT_ERROR_PROPERTY         =
    "jpydebug-pylint.error";
  public final static String PYLINT_ERROR_PROPERTY_LABEL =
    "options.jpydebug-pylint.error";
  private final static String _PYLINT_ERROR_PROPERTY_LABEL_DEFAULT_ =
    "Communicate ERROR";

  public final static String  PYLINT_WARNING_PROPERTY         =
    "jpydebug-pylint.warning";
  public final static String PYLINT_WARNING_PROPERTY_LABEL =
    "options.jpydebug-pylint.warning";
  private final static String _PYLINT_WARNING_PROPERTY_LABEL_DEFAULT_ =
    "Communicate WARNING";

  public final static String  PYLINT_CONVENTION_PROPERTY         =
    "jpydebug-pylint.convention";
  public final static String PYLINT_CONVENTION_PROPERTY_LABEL =
    "options.jpydebug-pylint.convention";
  private final static String _PYLINT_CONVENTION_PROPERTY_LABEL_DEFAULT_ =
    "Communicate CONVENTION";

  public final static String  PYLINT_REFACTOR_PROPERTY         =
    "jpydebug-pylint.refactor";
  public final static String PYLINT_REFACTOR_PROPERTY_LABEL =
    "options.jpydebug-pylint.refactor";
  private final static String _PYLINT_REFACTOR_PROPERTY_LABEL_DEFAULT_ =
    "Communicate REFACTOR";

  public final static String  PYLINT_ARGUMENT_PROPERTY         =
    "jpydebug-pylint.arguments";
  public final static String PYLINT_ARGUMENT_PROPERTY_LABEL =
    "options.jpydebug-pylint.arguments";
  private final static String _PYLINT_ARGUMENT_PROPERTY_LABEL_DEFAULT_ =
    "Complementary arguments to Pylint";

  private final static String _PYLINT_DEFAULT_OPTIONS_ =
    "--persistent=n --comment=n --disable-msg=W0103,W0131,C0103,W0312,W0511";


  private final static Dimension _FILLER_        = new Dimension( 0, 5 );
  private JTextField             _arguments;
  private JTextField             _pyLintLocation;

  private JCheckBox _usePyLint;
  private JCheckBox _fatal;
  private JCheckBox _errors;
  private JCheckBox _warnings;
  private JCheckBox _convention;
  private JCheckBox _refactor;

  /**
   * Creates a new instance of PyLintConfigurationPanel
   */
  public PyLintConfigurationPanel(){ }
  
  /**
   * populate panel infos
  */
  public void doMyLayout()
  {
    setLayout( new BorderLayout() ) ; 
    add( BorderLayout.CENTER , new _PYLINT_PANE_() ) ;
  }

  class _PANE_VERTICAL_ELEMENT_ extends JPanel
  {
    public _PANE_VERTICAL_ELEMENT_( JComponent label, JComponent field )
    {
      setLayout( new GridLayout( 1, 1 ) );

      Box b = Box.createHorizontalBox();
      b.add( label );
      b.add( Box.createHorizontalStrut( 10 ) );
      b.add( Box.createVerticalGlue() );

      // prevent component to resize in ugly ways
      Dimension pref =
        new Dimension(
                      field.getMaximumSize().width,
                      field.getPreferredSize().height
                   );
      field.setMaximumSize( pref );

      b.add( field );
      b.add( Box.createVerticalGlue() );
      add( b );
    }
  }


  class _PYLINT_PANE_ 
  extends JPanel
  {

    public _PYLINT_PANE_()
    {
      setLayout( new GridLayout( 1, 1 ) );

      Box b = Box.createVerticalBox();

      b.add( Box.createVerticalGlue() );

      _usePyLint = new JCheckBox();
      _usePyLint.setSelected( PyLintConfigurationPanel.super.get_booleanValue( USEPYLINT_PROPERTY , false ) );
      JLabel                  useLintLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( USEPYLINT_PROPERTY_LABEL , _USEPYLINT_PROPERTY_LABEL_DEFAULT_) );
      _PANE_VERTICAL_ELEMENT_ e1           =
        new _PANE_VERTICAL_ELEMENT_( _usePyLint, useLintLabel );
      b.add( e1 );
      b.add( Box.createRigidArea( _FILLER_ ) ); // filler

      _pyLintLocation = new JTextField( 20 );
      _pyLintLocation.setText( PyLintConfigurationPanel.super.get_value(PYLINT_LOCATION_PROPERTY, ""));
      JLabel jpyLocationLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_LOCATION_PROPERTY_LABEL , _PYLINT_LOCATION_PROPERTY_LABEL_DEFAULT_ ) );
      b.add( new FileChooserPane( jpyLocationLabel, _pyLintLocation, null ) );

      _arguments = new JTextField( 30 );
      _arguments.setText( PyLintConfigurationPanel.super.get_value( PYLINT_ARGUMENT_PROPERTY, "" ) );
      if (_arguments.getText().trim().length() == 0)
        // Empty initial case
        _arguments.setText( _PYLINT_DEFAULT_OPTIONS_ );
      JLabel                  jpyArgLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_ARGUMENT_PROPERTY_LABEL , _PYLINT_ARGUMENT_PROPERTY_LABEL_DEFAULT_ ) );
      _PANE_VERTICAL_ELEMENT_ e7          =
        new _PANE_VERTICAL_ELEMENT_( jpyArgLabel, _arguments );
      b.add( e7 );

      b.add( Box.createVerticalGlue() );
      b.setBorder( new TitledBorder( "PyLint usage options" ) );
      _fatal = new JCheckBox();
      _fatal.setSelected(
                         PyLintConfigurationPanel.super.get_booleanValue( PYLINT_FATAL_PROPERTY, false )
                      );
      JLabel                  fatalLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_FATAL_PROPERTY_LABEL , _PYLINT_FATAL_PROPERTY_LABEL_DEFAULT_ ) );
      _PANE_VERTICAL_ELEMENT_ e2         =
        new _PANE_VERTICAL_ELEMENT_( _fatal, fatalLabel );
      b.add( e2 );

      _errors = new JCheckBox();
      _errors.setSelected(
                          PyLintConfigurationPanel.super.get_booleanValue(  PYLINT_ERROR_PROPERTY, false )
                       );
      JLabel                  errorLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_ERROR_PROPERTY_LABEL , _PYLINT_ERROR_PROPERTY_LABEL_DEFAULT_) );
      _PANE_VERTICAL_ELEMENT_ e3         =
        new _PANE_VERTICAL_ELEMENT_( _errors, errorLabel );
      b.add( e3 );
      _warnings = new JCheckBox();
      _warnings.setSelected(
                            PyLintConfigurationPanel.super.get_booleanValue( PYLINT_WARNING_PROPERTY, false )
                         );

      JLabel                  warningsLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_WARNING_PROPERTY_LABEL , _PYLINT_WARNING_PROPERTY_LABEL_DEFAULT_) );
      _PANE_VERTICAL_ELEMENT_ e4            =
        new _PANE_VERTICAL_ELEMENT_( _warnings, warningsLabel );
      b.add( e4 );

      _convention = new JCheckBox();
      _convention.setSelected(
                              PyLintConfigurationPanel.super.get_booleanValue(PYLINT_CONVENTION_PROPERTY, false )
                           );

      JLabel                  conventionLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_CONVENTION_PROPERTY_LABEL , _PYLINT_CONVENTION_PROPERTY_LABEL_DEFAULT_ ) );
      _PANE_VERTICAL_ELEMENT_ e5              =
        new _PANE_VERTICAL_ELEMENT_( _convention, conventionLabel );
      b.add( e5 );

      _refactor = new JCheckBox();
      _refactor.setSelected(
                            PyLintConfigurationPanel.super.get_booleanValue( PYLINT_REFACTOR_PROPERTY, false )
                         );

      JLabel                  refactorLabel =
        new JLabel( PyLintConfigurationPanel.super.get_label( PYLINT_REFACTOR_PROPERTY_LABEL , _PYLINT_REFACTOR_PROPERTY_LABEL_DEFAULT_ ) );
      _PANE_VERTICAL_ELEMENT_ e6            =
        new _PANE_VERTICAL_ELEMENT_( _refactor, refactorLabel );
      b.add( e6 );
      b.add( Box.createRigidArea( _FILLER_ ) ); // filler

      add( b );
    }
  }

  public void populateFields()
  {
    PythonDebugParameters.set_usePyLint (_usePyLint.isSelected () ) ;
    PythonDebugParameters.set_pyLintConvention ( _convention.isSelected () ) ;
    PythonDebugParameters.set_pyLintError ( _errors.isSelected () ) ;
    PythonDebugParameters.set_pyLintFatal ( _fatal.isSelected () ) ;
    PythonDebugParameters.set_pyLintLocation ( _pyLintLocation.getText () ) ;
    PythonDebugParameters.set_pyLintArgs ( _arguments.getText () ) ;
    PythonDebugParameters.set_pyLintRefactor ( _refactor.isSelected () ) ;
    PythonDebugParameters.set_pyLintWarning ( _warnings.isSelected () ) ; 
  }
  

}
