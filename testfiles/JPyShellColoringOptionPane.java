/**
* Copyright (C) 2003 Jean-Yves Mengant
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

package org.jymc.jpydebug.jedit;


import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import javax.swing.* ; 
import javax.swing.border.* ; 
import java.awt.event.* ; 
import java.awt.*    ; 
import org.jymc.jpydebug.swing.ui.* ;
import org.jymc.jpydebug.PythonDebugParameters ;

/**
 * @author jean-yves
 *
 * Python Shell coloring option pane 
 * 
 */
public class JPyShellColoringOptionPane
extends AbstractOptionPane
{
 
  private final static String _EMPTY_ = "" ; 
  private JpyShellConfigurationPanel _colorPanel = new JpyShellConfigurationPanel() ; 
  
  public JPyShellColoringOptionPane()
  {
    super("pythonshell-options");
  }
	

  protected void _init()
  {
    
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_FONT_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_FONT_LABEL)) ; 
    _colorPanel.set_fontValue( JpyShellConfigurationPanel.JPYSHELL_FONT , 
                               jEdit.getFontProperty(JpyShellConfigurationPanel.JPYSHELL_FONT)
	                     );
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_BACKGROUND_COLOR_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_BACKGROUND_COLOR_LABEL)) ; 
    _colorPanel.set_colorValue( JpyShellConfigurationPanel.JPYSHELL_BACKGROUND_COLOR , 
	                        GUIUtilities.parseColor( 
                                   jEdit.getProperty( JpyShellConfigurationPanel.JPYSHELL_BACKGROUND_COLOR )));
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_HEADER_COLOR_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_HEADER_COLOR_LABEL)) ; 
    _colorPanel.set_colorValue( JpyShellConfigurationPanel.JPYSHELL_HEADER_COLOR , 
	                        GUIUtilities.parseColor( 
	                          jEdit.getProperty( JpyShellConfigurationPanel.JPYSHELL_HEADER_COLOR )));
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_MSG_COLOR_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_MSG_COLOR_LABEL)) ; 
    _colorPanel.set_colorValue( JpyShellConfigurationPanel.JPYSHELL_MSG_COLOR , 
	                        GUIUtilities.parseColor( 
	                          jEdit.getProperty( JpyShellConfigurationPanel.JPYSHELL_MSG_COLOR )));
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_WNG_COLOR_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_WNG_COLOR_LABEL)) ; 
    _colorPanel.set_colorValue( JpyShellConfigurationPanel.JPYSHELL_WNG_COLOR , 
	                        GUIUtilities.parseColor( 
	                          jEdit.getProperty( JpyShellConfigurationPanel.JPYSHELL_WNG_COLOR )));
    _colorPanel.set_label( JpyShellConfigurationPanel.JPYSHELL_ERROR_COLOR_LABEL ,
	                   jEdit.getProperty(JpyShellConfigurationPanel.JPYSHELL_ERROR_COLOR_LABEL)) ; 
    _colorPanel.set_colorValue( JpyShellConfigurationPanel.JPYSHELL_ERROR_COLOR , 
	                        GUIUtilities.parseColor( 
	                           jEdit.getProperty( JpyShellConfigurationPanel.JPYSHELL_ERROR_COLOR )));
    _colorPanel.doMyLayout(null) ;
    super.setLayout(new BorderLayout()) ; 
    super.setBorder( new TitledBorder ( "Python Shell coloring options")) ;
    super.add(BorderLayout.CENTER,_colorPanel);
  }
  
  protected void _save()
  {
    // populateFileds first 
    _colorPanel.populateFields() ;
    // functional properties
    jEdit.setFontProperty( JpyShellConfigurationPanel.JPYSHELL_FONT , 
                            PythonDebugParameters.get_shellFont() ) ;
    jEdit.setColorProperty( JpyShellConfigurationPanel.JPYSHELL_BACKGROUND_COLOR , 
	                    PythonDebugParameters.get_shellBackground()  ) ; 
    jEdit.setColorProperty( JpyShellConfigurationPanel.JPYSHELL_ERROR_COLOR , 
	                    PythonDebugParameters.get_shellError() ) ; 
    jEdit.setColorProperty( JpyShellConfigurationPanel.JPYSHELL_HEADER_COLOR , 
	                    PythonDebugParameters.get_shellHeader() ) ; 
    jEdit.setColorProperty( JpyShellConfigurationPanel.JPYSHELL_MSG_COLOR , 
	                    PythonDebugParameters.get_shellMessage() ) ; 
    jEdit.setColorProperty( JpyShellConfigurationPanel.JPYSHELL_WNG_COLOR , 
	                    PythonDebugParameters.get_shellWarning() ) ; 
		
  }
  
  private JButton createColorButton(String property) 
  {
  JButton b = new JButton(" ");
    b.setBackground(GUIUtilities.parseColor(jEdit.getProperty(property)));
    b.addActionListener(new ActionHandler());
    b.setRequestFocusEnabled(false);
    return b;
  }
   
  class ActionHandler 
  implements ActionListener
  {
    public void actionPerformed(ActionEvent evt)
    {
    JButton button = (JButton)evt.getSource();
    Color c = JColorChooser.showDialog(JPyShellColoringOptionPane.this,
      jEdit.getProperty("colorChooser.title"),
      button.getBackground());
      if(c != null)
	button.setBackground(c);
    }
  } 

}
