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

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.PropertiesChanged;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

// {{{ NativeBrowser class
/**
 * 
 * NativeBrowser - a dockable native browser using NativeSwing SWT libraries from
 * the DJ Project (http://djproject.sourceforge.net/).
 *
 */
public class NativeBrowser extends JPanel
    implements NativeBrowserActions, DefaultFocusComponent {

	private static final long serialVersionUID = 4557772486347339631L;
	public static final String NAME = "nativebrowser";

	// {{{ Instance Variables
	
	private String homepage;

	private View view;

	private boolean floating;

    final JWebBrowser browser;
    
    private NativeBrowserToolPanel toolPanel;

	// }}}
	
    // {{{ Constructor
	/**
	 * 
	 * @param view the current jedit window
	 * @param position a variable passed in from the script in actions.xml,
	 * 	which can be DockableWindowManager.FLOATING, TOP, BOTTOM, LEFT, RIGHT, etc.
	 * 	see {@link DockableWindowManager} for possible values.
	 */
	public NativeBrowser(View view, String position) {
		super(new BorderLayout());
		this.view = view;
		this.floating = position.equals(DockableWindowManager.FLOATING);

		if (jEdit.getSettingsDirectory() != null) {
			this.homepage = jEdit.getProperty(NativeBrowserPlugin.OPTION_PREFIX
					+ "homepage");
			if (this.homepage == null || this.homepage.length() == 0) {
				this.homepage = "http://google.com/";
				jEdit.setProperty(
						NativeBrowserPlugin.OPTION_PREFIX + "homepage",
						this.homepage);
			}
		}

		this.toolPanel = new NativeBrowserToolPanel(this);
		add(toolPanel, BorderLayout.NORTH);

		if (floating)
			this.setPreferredSize(new Dimension(500, 250));

        browser = new JWebBrowser();
        add(browser, BorderLayout.CENTER);
	    browser.navigate(homepage);

	    SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	toolPanel.propertiesChanged();
	        }
	      });
	}
    // }}}

    // {{{ Member Functions
    
    // {{{ focusOnDefaultComponent
	public void focusOnDefaultComponent() {
		browser.requestFocus();
	}
    // }}}

    // {{{ handlePropertiesChanged
	@EBHandler
	public void handlePropertiesChanged(PropertiesChanged message) {
		propertiesChanged();
	}
    // }}}
    
    // {{{ propertiesChanged
	private void propertiesChanged() {
		this.homepage = jEdit
				.getProperty(NativeBrowserPlugin.OPTION_PREFIX + "homepage");
		if (this.homepage == null || this.homepage.length() == 0) {
			this.homepage = "http://google.com/";
			jEdit.setProperty(
					NativeBrowserPlugin.OPTION_PREFIX + "homepage",
					this.homepage);
		}
	}
    // }}}

	// These JComponent methods provide the appropriate points
	// to subscribe and unsubscribe this object to the EditBus.

    // {{{ addNotify
	public void addNotify() {
		super.addNotify();
		EditBus.addToBus(this);
	}
     // }}}
     
    // {{{ removeNotify
	public void removeNotify() {
		super.removeNotify();
		EditBus.removeFromBus(this);
	}
    // }}}
    
    // {{{ setMenuBarVisible
	public void setMenuBarVisible(boolean b) {
		browser.setMenuBarVisible(b);
	}
    // }}}
	
    // {{{ setMenuBarVisible
	public boolean isMenuBarVisible() {
		return browser.isMenuBarVisible();
	}
    // }}}
	
    // {{{ render
	public void render(String html) {
		browser.setHTMLContent(html);
	}
    // }}}
	
    // {{{ renderBuffer
	public void renderBuffer(Buffer buffer) {
		String html = buffer.getText(0, buffer.getLength());
		browser.setHTMLContent(html);
	}
    // }}}
	
	// NativeBrowserActions implementation

    // {{{ renderBuffer
	public void renderBuffer(View view) {
		String html = view.getEditPane().getTextArea().getText();
		browser.setHTMLContent(html);
	}
    // }}}
	
    // {{{ browse
	public void browse(View view, String sURL) {
		browser.navigate(sURL);
	}
    // }}}
	
    // {{{ forward
	public void forward() {
		browser.navigateForward();
	}
    // }}}
	
    // {{{ back
	public void back() {
		browser.navigateBack();
	}
    // }}}
	
    // {{{ reload
	public void reload() {
		browser.reloadPage();
	}
    // }}}
	
    // {{{ setHomepage
	public void setHomepage(View view, String sURL) {
		homepage=sURL;
		jEdit.setProperty(
				NativeBrowserPlugin.OPTION_PREFIX + "homepage",
				sURL);
	}
    // }}}
	
    // {{{ home
	public void home() {
		browser.navigate(homepage);
	}
    // }}}
	
    // {{{ toggleMenuBar
	public void toggleMenuBar() {
		browser.setMenuBarVisible(!browser.isMenuBarVisible());
		toolPanel.propertiesChanged();
	}
    // }}}
	
}
// }}}
