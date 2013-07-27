/**
* Copyright (C) 2003,2004,2005 Jean-Yves Mengant
*
* JpyShellConfigurationPanel.java
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

import org.jymc.jpydebug.utils.FontSelector;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import javax.swing.JButton ; 
import javax.swing.Box ; 
import javax.swing.border.EmptyBorder ; 
import javax.swing.JColorChooser ; 
import java.awt.Color ; 
import java.awt.Font ; 
import java.awt.Dimension ; 
import java.awt.BorderLayout ; 
import java.awt.FlowLayout ; 
import java.awt.event.ActionListener ; 
import java.awt.event.ActionEvent ; 
import org.jymc.jpydebug.PythonDebugParameters ;
import javax.swing.JPanel ;
import java.awt.GridLayout ;


/**
 *
 * @author jean-yves
 */
public class JpyShellConfigurationPanel 
extends BasicConfigurationPanel
{
  private final static String _EMPTY_ = "" ;
  public final static String JPYSHELL_FONT_LABEL = "options.jpydebug-dbgoptions.jpyshellFont" ;
  private final static String _JPYSHELL_FONT_LABEL_DEFAULT_ = "Shell font" ;
  public final static String JPYSHELL_FONT = "jpydebug-dbgoptions.jpyshellFont" ;
  public final static String JPYSHELL_BACKGROUND_COLOR_LABEL = "options.jpydebug-dbgoptions.jpyshellBackgroundColor" ;
  private final static String _JPYSHELL_BACKGROUND_COLOR_LABEL_DEFAULT_ = "Shell Background color" ;
  public final static String JPYSHELL_BACKGROUND_COLOR = "jpydebug-dbgoptions.jpyshellBackgroundColor" ;
  public final static String JPYSHELL_MSG_COLOR_LABEL = "options.jpydebug-dbgoptions.jpyshellMsgColor" ;
  public final static String _JPYSHELL_MSG_COLOR_LABEL_DEFAULT_ = "Shell Message Color" ;
  public final static String JPYSHELL_MSG_COLOR = "jpydebug-dbgoptions.jpyshellMsgColor" ;
  public final static String JPYSHELL_WNG_COLOR_LABEL = "options.jpydebug-dbgoptions.jpyshellWngColor" ;
  private final static String _JPYSHELL_WNG_COLOR_LABEL_DEFAULT_ = "Shell Warning color" ;
  public final static String JPYSHELL_WNG_COLOR = "jpydebug-dbgoptions.jpyshellWngColor" ;
  public final static String JPYSHELL_HEADER_COLOR_LABEL = "options.jpydebug-dbgoptions.jpyshellHeaderColor" ;
  private final static String _JPYSHELL_HEADER_COLOR_LABEL_DEFAULT_ = "Shell Header color" ;
  public final static String JPYSHELL_HEADER_COLOR = "jpydebug-dbgoptions.jpyshellHeaderColor" ;
  public final static String JPYSHELL_ERROR_COLOR_LABEL = "options.jpydebug-dbgoptions.jpyshellErrorColor" ;
  private final static String _JPYSHELL_ERROR_COLOR_LABEL_DEFAULT_ = "Shell Error Color" ;
  public final static String JPYSHELL_ERROR_COLOR = "jpydebug-dbgoptions.jpyshellErrorColor" ;
  
  private _FONTBUTTON_ _fontSelector ;   
  private JButton _msgColor         ;
  private JButton _wngColor ;
  private JButton _errorColor ;
  private JButton _headerColor ;
  private JButton _backgroundColor ; 
  
  /** Creates a new instance of JpyShellConfigurationPanel */
  public JpyShellConfigurationPanel() 
  {}

  class _COLORBUTTON_ 
  extends JButton
  {
    public _COLORBUTTON_(String property , String label )
    {
      super (" ") ; 
      setText(label) ; 
      super.setForeground(JpyShellConfigurationPanel.super.get_colorValue(property));
      addActionListener(new ActionHandler());
      setRequestFocusEnabled(false);
    }
    
    public Dimension getMinimumSize()
    {
      return new Dimension( 500 , getPreferredSize().height ) ; 
    }
    
    public Dimension getMaximumSize()
    {
      return new Dimension( 500 , getPreferredSize().height ) ; 
    }
  }
  
  class _FONTBUTTON_ 
  extends FontSelector
  {
    public _FONTBUTTON_(Font font )
    {
      super (font) ; 
    }
    
    public Dimension getMinimumSize()
    {
      return new Dimension( 500 , getPreferredSize().height ) ; 
    }
    
    public Dimension getMaximumSize()
    {
      return new Dimension( 500 , getPreferredSize().height ) ; 
    }
  }
  
  private JButton createColorButton(String property , String label ) 
  { return new _COLORBUTTON_( property , label ) ; }

  /**
    layout panel components
  */
  public void doMyLayout( ActionListener action )
  {
    // setLayout( new GridLayout(1,1)) ;
    setLayout( new BorderLayout() ) ; 

    _fontSelector = new _FONTBUTTON_(super.get_fontValue(JPYSHELL_FONT));
    _msgColor = createColorButton(JPYSHELL_MSG_COLOR , super.get_label(JPYSHELL_MSG_COLOR_LABEL,_JPYSHELL_MSG_COLOR_LABEL_DEFAULT_));
    _wngColor = createColorButton(JPYSHELL_WNG_COLOR , super.get_label(JPYSHELL_WNG_COLOR_LABEL,_JPYSHELL_WNG_COLOR_LABEL_DEFAULT_));
    _errorColor  = createColorButton(JPYSHELL_ERROR_COLOR , super.get_label(JPYSHELL_ERROR_COLOR_LABEL,_JPYSHELL_ERROR_COLOR_LABEL_DEFAULT_));
    _headerColor = createColorButton(JPYSHELL_HEADER_COLOR, super.get_label(JPYSHELL_HEADER_COLOR_LABEL,_JPYSHELL_HEADER_COLOR_LABEL_DEFAULT_));
    _backgroundColor = createColorButton(JPYSHELL_BACKGROUND_COLOR , super.get_label(JPYSHELL_BACKGROUND_COLOR_LABEL,_JPYSHELL_BACKGROUND_COLOR_LABEL_DEFAULT_)); 
    
    Box b = Box.createVerticalBox() ;
    
    b.setBorder( new EmptyBorder(10,10,10,10)) ;
    
    b.add(Box.createVerticalStrut(50) ) ; 
    b.add(_fontSelector) ;
    b.add(Box.createVerticalStrut(10) ) ; 
    b.add(_backgroundColor) ;
    b.add(Box.createVerticalStrut(10) ) ; 
    b.add(_msgColor) ;
    b.add(Box.createVerticalStrut(10) ) ; 
    b.add(_wngColor) ;
    b.add(Box.createVerticalStrut(10) ) ; 
    b.add(_headerColor) ;
    b.add(Box.createVerticalStrut(10) ) ; 
    b.add(_errorColor) ;
    
    add(BorderLayout.CENTER , b) ; 
  }
  
  class ActionHandler 
  implements ActionListener
  {
    public void actionPerformed(ActionEvent evt)
    {
    JButton button = (JButton)evt.getSource();
    Color c = JColorChooser.showDialog(  JpyShellConfigurationPanel.this,
                                         "Color chooser" ,
                                         button.getBackground());
      if(c != null)
	button.setForeground(c);
    }
  } 
  
  /**
     populate fields values to memory area
  */ 
  public void populateFields()
  {
    PythonDebugParameters.set_shellFont( _fontSelector.getFont()) ;
    PythonDebugParameters.set_shellBackground( _backgroundColor.getForeground() ) ;
    PythonDebugParameters.set_shellError( _errorColor.getForeground() ) ; 
    PythonDebugParameters.set_shellWarning( _wngColor.getForeground() ) ; 
    PythonDebugParameters.set_shellHeader( _headerColor.getForeground() ) ; 
    PythonDebugParameters.set_shellMessage( _msgColor.getForeground() ) ; 
  }
  
}
