package nativebrowser;
/*
 * NativeBrowser.java
 * part of the NativeBrowser plugin for the jEdit text editor
 * Copyright (C) 2010 François Rey
 * jedit at francois . rey . name
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
 *
 * $Id$
 */

// {{{ imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;

import chrriis.dj.nativeswing.swtimpl.components.JFlashPlayer;

import common.gui.ModalJFileChooser;

// {{{ NativeBrowser class
/**
 * 
 * NativeFlashPlayer - a dockable flash player using NativeSwing SWT libraries from
 * the DJ Project (http://djproject.sourceforge.net/).
 *
 */
public class NativeFlashPlayer extends JPanel
    implements NativeFlashPlayerActions, DefaultFocusComponent {

	public static final String NAME = "nativeflashplayer";
	private static final String PROP_PREFIX =
				NativeFlashPlayer.class.getName();
	private static final String DIALOG_SUFFIX = ".dialog";
	private static final String APPROVE_BUTTON_TEXT_SUFFIX = ".approve-button-text";
	private static final String DIALOG_CHOOSE_SWF =
				PROP_PREFIX + DIALOG_SUFFIX + ".choose-swf";

	// {{{ Instance Variables
	private static final long serialVersionUID = 455772835148631L;
	
	private View view;

	private boolean floating;

    final JFlashPlayer player;
    
    private NativeFlashPlayerToolPanel toolPanel;

	// }}}
	
    // {{{ Constructor
	/**
	 * 
	 * @param view the current jedit window
	 * @param position a variable passed in from the script in actions.xml,
	 * 	which can be DockableWindowManager.FLOATING, TOP, BOTTOM, LEFT, RIGHT, etc.
	 * 	see {@link DockableWindowManager} for possible values.
	 */
	public NativeFlashPlayer(View view, String position) {
		super(new BorderLayout());
		this.view = view;
		this.floating = position.equals(DockableWindowManager.FLOATING);

		this.toolPanel = new NativeFlashPlayerToolPanel(this);
		add(toolPanel, BorderLayout.NORTH);

		if (floating)
			this.setPreferredSize(new Dimension(500, 250));

        player = new JFlashPlayer();
        add(player, BorderLayout.CENTER);

	}
    // }}}

    // {{{ Member Functions
    
    // {{{ focusOnDefaultComponent
	public void focusOnDefaultComponent() {
		player.requestFocus();
	}
    // }}}

	// NativeFlashPlayerActions implementation

    // {{{ load
	public void load(String resource) {
		toolPanel.setAddress(resource);
		player.load(resource);
	}
    // }}}
	
    // {{{ play
	public void open() {
		JFileChooser chooser = new ModalJFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showDialog(null,
				jEdit.getProperty(DIALOG_CHOOSE_SWF + 
						APPROVE_BUTTON_TEXT_SUFFIX)) != 
							JFileChooser.APPROVE_OPTION) {
			return;
		}
		File swf = chooser.getSelectedFile();
		player.load(swf.getPath());
	}
    // }}}
	
    // {{{ play
	public void play() {
		player.play();
	}
    // }}}
	
    // {{{ pause
	public void pause() {
		player.pause();
	}
    // }}}
	
    // {{{ stop
	public void stop() {
		player.stop();
	}
    // }}}
	
	
}
// }}}
