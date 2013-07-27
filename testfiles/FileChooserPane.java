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

package org.jymc.jpydebug.utils;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.* ;

/** 
 *  
 * File chooser used by misc internal options panel
 *
*/

public class FileChooserPane
extends JPanel
implements ActionListener
{
  JTextField _fNameField ;
  
  private boolean _directories = false ; 
  
  public void chooseDirectories( boolean directories )
  { _directories = directories ; }



  public FileChooserPane( JLabel title ,
                          JTextField fNameField ,
                          JButton fileButton 
                        )
  {
    super() ;
    setLayout( new GridLayout(1,1) ) ;
    Box box = Box.createHorizontalBox() ;
    
    _fNameField = fNameField ;
    
    // prevent component to resize in ugly ways
    Dimension pref = new Dimension( 
                                    _fNameField.getMaximumSize().width ,
                                    _fNameField.getPreferredSize().height 
                                  ) ; 
    _fNameField.setMaximumSize( pref) ;
    
    if ( title != null )
      box.add(title ) ;

    box.add( Box.createHorizontalStrut(10)) ;
    box.add( fNameField ) ;
    box.add( Box.createHorizontalStrut(10)) ;
    
	// add to current pane if not already defined
	if ( fileButton == null ) 
	{
	 fileButton = new JButton("...") ;
	 box.add( fileButton )   ;
	}
      
	fileButton.addActionListener(this);
	add(box) ;
  }

  public void actionPerformed ( ActionEvent e )
  {
  JFileChooser chooser ;     
    if ( _fNameField.getText().length() == 0 ) 
      chooser = new JFileChooser() ;
    else 
      chooser = new JFileChooser(_fNameField.getText()) ;    
    if ( _directories )
      chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY) ;

    chooser.showOpenDialog(null) ; 
    File selFile = chooser.getSelectedFile() ; 
      
    if( selFile != null )
      _fNameField.setText(selFile.getAbsolutePath());
  }

}

