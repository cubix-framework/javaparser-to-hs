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

import java.awt.* ;
import java.awt.event.* ;
import javax.swing.* ;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.*;


/**
 * @author jean-yves
 *
 * Utility panel for Color Chooser purpose
 * 
 */

public class JPYColorChooserHelper
{

  private final static String _HTML_ = "<html>" ;
  private final static String _EHTML_ = "</html>" ;
  private final static String _FONTSIZE_ = "view.fontsize" ;
  private final static String _FONT_ = "view.font" ;
  private final static String _STYLE_KEYWORD2_ = "view.style.keyword2" ;
  private final static String _BGCOLOR_ = "view.bgColor" ;
  private final static String _BOLD_ = "<B>" ;
  private final static String _EBOLD_ = "</B>" ;
  private final static String _ITALIC_ = "<I>" ;
  private final static String _EITALIC_ = "</I>" ;
  private final static String _EMPTY_ = "" ;
  private final static String _FONTFACE_ = "<FONT FACE=" ;
  private final static String _EFONT_ = "</FONT>" ;
  private final static String _SIZE_ = " size=" ;
  private final static String _COLOR_ = " color=" ;
  private final static String _ENDTAG_ = " >" ;
  private final static String _IMPORT_ = "import " ; 
  private final static String _MYSTUFF_ = "mystuff" ; 
  private final static String _NULLCOLOR_ = "#000000" ; 


  private String _htmlLabel = getHTMLLabel() ; 
  private Component _parent ; 
  private String    _title  ; 
		
		
  private String getHTMLLabel() 
  {
	StringBuffer buf = new StringBuffer(_HTML_) ;
	int fontsize = Integer.parseInt(jEdit.getProperty(_FONTSIZE_));
	String fontName = jEdit.getProperty(_FONT_);
	SyntaxStyle sk2 = GUIUtilities.parseStyle(jEdit.getProperty(_STYLE_KEYWORD2_),fontName,fontsize);

	fontsize = 2;
	String sk2ColorStr = GUIUtilities.getColorHexString(sk2.getForegroundColor());
	String bold = _EMPTY_;
	String fBold= _EMPTY_;
	if (sk2.getFont().isBold()) 
	{
	  bold=_BOLD_;
	  fBold = _EBOLD_;
	}
	String italic = _EMPTY_;
	String fitalic= _EMPTY_;
	if (sk2.getFont().isItalic()) 
	{
	  italic=_ITALIC_;
	  fitalic = _EITALIC_ ;
	}

	buf.append(_FONTFACE_+fontName+_SIZE_+fontsize+_COLOR_+sk2ColorStr+_ENDTAG_);
	buf.append(bold+italic+_IMPORT_+fBold+fitalic+_EFONT_);
	buf.append(_FONTFACE_+fontName+_SIZE_+fontsize+_COLOR_+_NULLCOLOR_+_ENDTAG_);
	buf.append(_MYSTUFF_+ _EFONT_);
	buf.append(_EHTML_);
	return buf.toString();
  }

  public JPanel createColorPanel(String property, String propertyBorder)
  {
  JPanel p = new JPanel(new BorderLayout());
	p.setBorder(BorderFactory.createMatteBorder(2,2,2,2,GUIUtilities.parseColor(jEdit.getProperty(propertyBorder))) );
	JButton b = new JButton(_htmlLabel);
	b.setHorizontalAlignment(SwingConstants.LEFT );
	b.setBackground(GUIUtilities.parseColor(jEdit.getProperty(_BGCOLOR_)));
	b.setPreferredSize(new Dimension(400,20) );
	b.addActionListener(new _ACTIONHANDLERBORDER_(p));
	b.setRequestFocusEnabled(false);
	p.add(b);
	return p;
  } 
	  
  public JButton createColorButton(String property)
  {
  JButton b = new JButton(_htmlLabel);
	b.setBackground(GUIUtilities.parseColor(jEdit.getProperty(property)));

	b.setHorizontalAlignment(SwingConstants.LEFT);
	b.setPreferredSize(new Dimension(400,20) );
	b.addActionListener(new _ACTIONHANDLER_());
	b.setRequestFocusEnabled(false);
	return b;
  } 
		
  public JPYColorChooserHelper( String title , Component c  )
  { 
    _parent = c ; 
    _title  = title ; 
  }

  class _ACTIONHANDLERBORDER_ 
  implements ActionListener
  {
	private JPanel _pane ; 
	  	
	public _ACTIONHANDLERBORDER_( JPanel p )
	{ _pane  = p ; }
	  	
	public void actionPerformed(ActionEvent evt)
	{
	Color c = JColorChooser.showDialog( _parent ,
						  jEdit.getProperty(_title),
						  ((javax.swing.border.MatteBorder)_pane.getBorder()).getMatteColor() );
	  if(c != null)
		_pane.setBorder(BorderFactory.createMatteBorder(2,2,2,2,c) );
	}
  } 


  class _ACTIONHANDLER_ 
  implements ActionListener
  {
	public void actionPerformed(ActionEvent evt)
	{
	JButton button = (JButton)evt.getSource();
	Color c = JColorChooser.showDialog(_parent,
						jEdit.getProperty(_title),
						button.getBackground());
	  if(c != null)
		button.setBackground(c);
	}
  } 

}
	
