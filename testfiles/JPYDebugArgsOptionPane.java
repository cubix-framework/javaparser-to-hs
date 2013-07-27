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

import java.awt.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.jymc.jpydebug.* ; 
import org.jymc.jpydebug.swing.ui.JpyArgsConfigurationPanel ; 


public class JPYDebugArgsOptionPane
extends AbstractOptionPane
{
    private JpyArgsConfigurationPanel _argsPane = null ; 	
  
	public JPYDebugArgsOptionPane()
	{
	  super("jpydebug-arguments");
	}
	
	/**
	 *  init option pane panel
	 */
	protected void _init()
	{
	  setLayout(new BorderLayout(0,6))  ;
      _argsPane = new JpyArgsConfigurationPanel()  ;
	  add( BorderLayout.CENTER , _argsPane);
	}	
	
	/**
	 *  save options properties
	 */
	protected void _save()
	{ 
	  try {	
	    _argsPane.save() ; 
	  } catch ( PythonDebugException e )
	  { Log.log( Log.ERROR , this , e.getMessage() ) ; }	
	}
}
