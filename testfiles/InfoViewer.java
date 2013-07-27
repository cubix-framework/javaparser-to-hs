/*
 * InfoViewer.java - Info viewer for HTML, txt
 * Copyright (C) 2000-2002 Dirk Moebius
 * Based on HTMLViewer.java Copyright (C) 1999 Slava Pestov
 *
 * :tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
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

package infoviewer;

import infoviewer.actions.InfoViewerAction;
import infoviewer.actions.ToggleSidebar;
import infoviewer.workaround.EnhancedJEditorPane;
import infoviewer.workaround.EnhancedJToolBar;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;


import javax.accessibility.AccessibleHypertext;
import javax.accessibility.AccessibleText;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.StyleSheet;

import org.gjt.sp.jedit.ActionContext;
import org.gjt.sp.jedit.ActionSet;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.HistoryModel;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.io.FileVFS;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;

/**
 * an info viewer for jEdit. It uses a Swing JEditorPane to display the HTML,
 * and implements a URL history, bookmarks and some other web browsing
 * functions.
 *
 * @author Dirk Moebius
 * @author Slava Pestov
 */
public class InfoViewer extends JPanel implements HyperlinkListener, PropertyChangeListener,
	EBComponent, DefaultFocusComponent
{
	// {{{ Proteced Members
	protected JPanel outerPanel;

	protected JPanel innerPanel;

	protected MyScrollPane scrViewer;

	// }}}

	/**
	 * Creates a new info viewer instance.
	 *
	 * @param view
	 *                where this dockable is docked into.
	 * @param position
	 *                docking position.
	 */
	public InfoViewer(View view, String position)
	{
		setName("infoviewer");
		if (position == null)
			position = DockableWindowManager.FLOATING;
		setLayout(new BorderLayout());
		this.view = view;
		this.isDocked = !(position.equals(DockableWindowManager.FLOATING));
		this.history = new History();
		this.historyhandler = new URLButtonHandler(false);
		this.bookmarkhandler = new URLButtonHandler(true);

		// initialize actions
		createActions();

		/** this is for handling the "esc" key only */
		KeyHandler escKeyHandler = new KeyHandler();
		addKeyListener(escKeyHandler);
		JRootPane root = getRootPane();
		if (root != null)
			root.addKeyListener(escKeyHandler);

		// the menu
		JMenuBar mb = createMenu();
		// the toolbar
		JToolBar tb = createToolbar();
		// the url address bar
		JPanel addressBar = createAddressBar();
		addressBar.addKeyListener(escKeyHandler);
		// the status bar
		JPanel statusBar = createStatusBar();

		// the viewer
		viewer = new EnhancedJEditorPane();
		viewer.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		viewer.addKeyListener(escKeyHandler);
		viewer.setEditable(false);
		viewer.setFocusable(true);
		Font vf = jEdit.getFontProperty("metal.secondary.font");
		viewer.setFont(jEdit.getFontProperty("helpviewer.font", vf));
		viewer.addHyperlinkListener(this);
		viewer.addPropertyChangeListener(this);
		viewer.addMouseListener(new MouseHandler());

		scrViewer = new MyScrollPane(viewer);
		scrViewer.setFocusable(false);
		viewer.setArrowKeyHandler(new ArrowKeyHandler());
		// HTMLEditorKit is not yet in use here

		// the inner content: url textfield, viewer, status bar
		String appearancePrefix = "infoviewer.appearance."
			+ (isDocked ? "docked." : "floating.");
		innerPanel = new JPanel(new BorderLayout());

		innerPanel.add(scrViewer, BorderLayout.CENTER);
		if (jEdit.getBooleanProperty(appearancePrefix + "showAddressbar"))
			innerPanel.add(addressBar, BorderLayout.NORTH);
		if (jEdit.getBooleanProperty(appearancePrefix + "showStatusbar"))
			innerPanel.add(statusBar, BorderLayout.SOUTH);

		// the outer content: toolbar, inner content
		outerPanel = new JPanel(new BorderLayout());
		outerPanel.add(innerPanel, BorderLayout.CENTER);
		if (jEdit.getBooleanProperty(appearancePrefix + "showToolbar"))
			outerPanel.add(tb, BorderLayout.NORTH);

		// overall layout: menu, outer content
		if (jEdit.getBooleanProperty(appearancePrefix + "showMenu"))
			add(mb, BorderLayout.NORTH);
		add(outerPanel, BorderLayout.CENTER);

		updateStatus();
		updateTimers();

		// show start URL (either homepage or current buffer)
		if (jEdit.getBooleanProperty("infoviewer.autoupdate")
			&& (jEdit.getBooleanProperty("infoviewer.autoupdate.onSwitch")
				|| jEdit.getBooleanProperty("infoviewer.autoupdate.onSave") || jEdit
				.getBooleanProperty("infoviewer.autoupdate.onChange")))
		{
			// auto-update and sync with buffer: open current buffer
			// at startup
			gotoBufferURL();
		}
		else
		{
		    if (jEdit.getBooleanProperty("infoviewer.restorePreviousPage") && urlField.getModel().getSize() > 0) {
		        // Restore previous page on startup
		        gotoURL(urlField.getModel().getItem(0), true, 0);
		    }
            else {
                // open homepage at startup
                String home = jEdit.getProperty("infoviewer.homepage");
                currentURL = new TitledURLEntry("Infoviewer Homepage", home);
                if (home != null)
                    gotoURL(home, true, 0);
            }
        }
		urlField.addKeyListener(escKeyHandler);
		setFocusCycleRoot(true);
		Caret c = viewer.getCaret();
		c.setVisible(true);
	}

	protected Document getDocument()
	{
		return viewer.getDocument();
	}

	public TitledURLEntry getCurrentURL()
	{
		String url = urlField.getText();
		if (url == null || url.length() < 1)
			return null;
		currentURL = new TitledURLEntry(title.getText(), urlField.getText());
		int scrollBarPos = scrViewer.getVerticalScrollBar().getValue();
		currentURL.setScrollBarPos(scrollBarPos);
		return currentURL;
	}

	// {{{ gotoURL()
	public void gotoURL(String url)
	{
		gotoURL(url, true, 0);
	}

	public void gotoURL(String url, boolean addToHistory, int vertPos)
	{
		try
		{
			gotoURL(new URL(url), addToHistory, vertPos);
		}
		catch (MalformedURLException mfue)
		{

		}
	}

	/**
	 * Displays the specified URL in the HTML component.
	 *
	 * @param url
	 *                The URL as String
	 * @param addToHistory
	 *                Should the URL be added to the back/forward history?
	 */
	public void gotoURL(TitledURLEntry entry, boolean addToHistory)
	{
		String url = entry.getURL();
		try
		{
			URI baseURI = new File(MiscUtilities.constructPath(jEdit.getJEditHome(), "doc")).toURI();
			baseURL = baseURI.toURL().toString();
		}
		catch (MalformedURLException mu)
		{
			Log.log(Log.ERROR, this, mu);
			// what to do?
		}
		if (MiscUtilities.isURL(url))
		{
			if (url.startsWith(baseURL))
			{
				shortURL = url.substring(baseURL.length());
				if (shortURL.startsWith("/"))
					shortURL = shortURL.substring(1);
			}
			else
			{
				shortURL = url;
			}
		}
		else
		{
			shortURL = url;
			if (baseURL.endsWith("/"))
				url = baseURL + url;
			else
				url = baseURL + '/' + url;
		}

		if (url == null)
			return;
		url = url.trim();
		if (url.length() == 0)
			return;

		try
		{
			URL u = new URL(url);
			gotoURL(u, addToHistory, entry.getScrollBarPos());
		}
		catch (MalformedURLException mu)
		{
			urlField.setText(url);
			showError(props("infoviewer.error.badurl.message", new Object[] { mu }));
		}
	}


	/**
	 * Convenience function
	 *
	 * @param url
	 * @param addToHistory
	 */
	public void gotoURL(URL url, boolean addToHistory)
	{
		gotoURL(url, addToHistory, 0);
	}

	/**
	 * Displays the specified URL in the HTML component.
	 *
	 * @param url
	 *                The URL
	 * @param addToHistory
	 *                Should the URL be added to the back/forward history?
	 */
	public void gotoURL(URL url, boolean addToHistory, final int scrollBarPos)
	{
		if (url == null)
			return;
		String urlText = url.toString().trim();
		if (urlText.length() == 0)
			return;

		if (addToHistory) 
		{
			history.add(getCurrentURL());
			HistoryModel hm = urlField.getModel();			
			hm.addItem(urlText);
		}

		urlField.setText(urlText);
		viewer.setCursor(Cursor.getDefaultCursor());
		currentURL = new TitledURLEntry(urlText, urlText, scrollBarPos);
		currentStatus = LOADING;

		updateStatus();
		updateGoMenu();

		try
		{
			// viewer.getEditorKit().createDefaultDocument();
			viewer.setPage(url);

			// the style of the viewer
			if (viewer.getEditorKit() instanceof HTMLEditorKit)
			{
				HTMLEditorKit htmlEditorKit = (HTMLEditorKit) (viewer
					.getEditorKit());
				// HTMLDocument
				// doc=(HTMLDocument)viewer.getDocument();
				// Log.log(Log.DEBUG, this, "htmleditorkit in
				// use");
				StyleSheet styles;
				// StyleSheet
				// styles=htmlEditorKit.getStyleSheet();
				// if(doc!=null) {
				// Log.log(Log.DEBUG, this, "styles from doc");
				// styles=doc.getStyleSheet();
				// code below dies with NPE then
				// }
				// else {
				// Log.log(Log.DEBUG, this, "styles from
				// editor");
				styles = htmlEditorKit.getStyleSheet();
				// }
				Enumeration rules;
				if (styles != null)
				{
					// list available styles (which contain
					// 'font-size')
					rules = styles.getStyleNames();
					while (rules.hasMoreElements())
					{
						String name = (String) rules.nextElement();
						Style rule = styles.getStyle(name);
						if (rule.toString().indexOf("font-size") > -1)
						{
							Log.log(Log.DEBUG, this, name + "[old] : "
								+ rule.toString());
						}
					}

					// make body fontsize smaller
					Style bodyrule = styles.getStyle("body");
					Style bodyruleparent = (Style) bodyrule.getResolveParent();
					if (bodyrule != null)
					{
						styles.removeStyle("body");
						Style newbodyrule = styles.addStyle("body",
							bodyruleparent);

						if (bodyruleparent != null)
							Log.log(Log.DEBUG, this, "bodyrule.p="
								+ bodyruleparent.toString());
						Log.log(Log.DEBUG, this, "bodyrule.1="
							+ bodyrule.toString());
						// String
						// val=(String)bodyrule.getAttribute("font-size");
						// Log.log(Log.DEBUG, this,
						// "body.font-size="+val);
						// bodyrule.removeAttribute("font-size");
						Enumeration attrs = bodyrule.getAttributeNames();
						if (attrs != null)
						{
							// Log.log(Log.DEBUG,
							// this, "copying
							// attributes");
							while (attrs.hasMoreElements())
							{
								Object name = attrs.nextElement();
								// Log.log(Log.DEBUG,
								// this, "
								// attribute.name="+name.toString());
								if (!name.toString().equals("font-size"))
								{
									newbodyrule.addAttribute(name,
											bodyrule.getAttribute(name));
								}
							}
						}

						// Action myaction=new
						// StyledEditorKit.FontSizeAction("new
						// font size",
						// Integer.parseInt(size));
						// myaction.actionPerformed(null);

						// HTMLDocument doc =
						// ((HTMLDocument)
						// viewer.getDocument());
						// doc.setCharacterAttributes(0,
						// doc.getLength(), newbodyrule,
						// true);

						/*
						 * Log.log(Log.DEBUG, this,
						 * "bodyrule.2="+bodyrule.toString());
						 * bodyrule.addAttribute("font-size","10pt");
						 * Log.log(Log.DEBUG, this,
						 * "bodyrule.3="+bodyrule.toString());
						 * newbodyrule.addAttributes(bodyrule);
						 */
						Log.log(Log.DEBUG, this, "bodyrule.2="
							+ newbodyrule.toString());
					}
					// styles.setBaseFontSize(1);
					// htmlEditorKit.setStyleSheet(styles);
					viewer.repaint();

					// list available styles (which contain
					// 'font-size')
					rules = styles.getStyleNames();
					while (rules.hasMoreElements())
					{
						String name = (String) rules.nextElement();
						Style rule = styles.getStyle(name);
						if (rule.toString().indexOf("font-size") > -1)
						{
							Log.log(Log.DEBUG, this, name + "[new] : "
								+ rule.toString());
						}
					}
				}
				else
				{
					// Log.log(Log.WARNING, this, "empty
					// style set");
				}
			}
			else
			{
				// Log.log(Log.WARNING, this, "unexpected kind
				// of editorkit in use");
			}
		}
		catch (FileNotFoundException fnf)
		{
			String[] args = { urlText };
			showError(props("infoviewer.error.filenotfound.message", args));
		}
		catch (IOException io)
		{
			Log.log(Log.ERROR, this, io);
			String[] args = { urlText, io.getMessage() };
			showError(props("infoviewer.error.ioerror.message", args));
		}
		catch (Exception ex)
		{
			Log.log(Log.ERROR, this,
				"JEditorPane.setPage() threw an exception, probably a Swing bug:", ex);
		}
		finally
		{
			updateTimers();
			previousScrollBarValue = scrollBarPos;
		}
	}

	/**
	 * Show the contents of the current jEdit buffer in InfoViewer.
	 */
	public void gotoBufferURL()
	{
		Buffer buffer = view.getBuffer();
		String url = buffer.getPath();
		if (buffer.getVFS() instanceof FileVFS)
			url = "file:" + url;
		gotoURL(url, false, 0);
	}

	/**
	 * Go forward in history. Beep if that's not possible.
	 */
	public void forward()
	{
		TitledURLEntry ent = history.getNext(getCurrentURL());

		if (ent == null)
			getToolkit().beep();
		else
			gotoURL(ent, false);
	}

	/**
	 * Go back in history. Beep, if that's not possible.
	 */
	public void back()
	{
		TitledURLEntry prevURL = history.getPrevious(getCurrentURL());

		if (prevURL == null) {
			getToolkit().beep();
		}
		else {
			gotoURL(prevURL, false);
		}
	}

	/**
	 * Reload the current URL.
	 */
	public void reload()
	{
		if (currentURL == null)
			return;

		previousScrollBarValue = scrViewer.getVerticalScrollBar().getValue();
		// Clear the viewer and flush viewers' memorized URL:
		viewer.getDocument().putProperty(Document.StreamDescriptionProperty, null);
		gotoURL(getCurrentURL(), false);
	}

	/**
	 * Add the current page to the bookmark list.
	 */
	public void addToBookmarks()
	{
		if (currentURL == null)
		{
			GUIUtilities.error(null, "infoviewer.error.nourl", null);
			return;
		}

		jEdit.setProperty("infoviewer.bookmarks.title." + bookmarks.getSize(), currentURL
			.getTitle());
		jEdit.setProperty("infoviewer.bookmarks.url." + bookmarks.getSize(), currentURL
			.getURL());
		bookmarks.add(currentURL);

		jEdit.unsetProperty("infoviewer.bookmarks.title." + bookmarks.getSize());
		jEdit.unsetProperty("infoviewer.bookmarks.url." + bookmarks.getSize());

		// add menu item
		JMenuItem mi = new JMenuItem(currentURL.getTitle());
		mBmarks.add(mi);
		mi.setActionCommand(currentURL.getURL());
		mi.addActionListener(bookmarkhandler);
	}

	/**
	 * Return the JEditorPane instance that is used to view HTML and text
	 * URLs.
	 */
	public JEditorPane getViewer()
	{
		return viewer;
	}

	/**
	 * From interface HyperlinkListener: called when a hyperlink is clicked,
	 * entered or leaved.
	 */
	public void hyperlinkUpdate(HyperlinkEvent evt)
	{
		URL url = evt.getURL();
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		{
			if (evt instanceof HTMLFrameHyperlinkEvent)
			{
				((HTMLDocument) viewer.getDocument())
					.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) evt);
			}
			else
			{
				if (url != null)
					gotoURL(url, true, 0);
			}
		}
		else if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED)
		{
			viewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			if (url != null)
				setStatusText(url.toString());
		}
		else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED)
		{
			viewer.setCursor(Cursor.getDefaultCursor());
			updateStatus();
		}
	}

	/**
	 * From interface PropertyChangeListener: called, when a property is
	 * changed. This is used to listen for "page" property change events,
	 * which occur, when the page is loaded completely.
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		if ("page".equals(e.getPropertyName()))
			pageComplete();
	}

	/**
	 * From interface EBComponent: Listen for messages on the EditBus.
	 * Currently it listens for PropertiesChanged messages, to update any
	 * bookmark changes.
	 */
	public void handleMessage(EBMessage msg)
	{
		if (msg instanceof EditPaneUpdate)
		{
			EditPaneUpdate emsg = (EditPaneUpdate) msg;
			EditPane editPane = emsg.getEditPane();
			if (editPane == view.getEditPane())
			{
				if (emsg.getWhat() == EditPaneUpdate.BUFFER_CHANGED)
				{
					if (jEdit.getBooleanProperty("infoviewer.autoupdate")
						&& jEdit
							.getBooleanProperty("infoviewer.autoupdate.onSwitch"))
					{
						gotoBufferURL();
					}
				}
			}
		}
		else if (msg instanceof BufferUpdate)
		{
			BufferUpdate bmsg = (BufferUpdate) msg;
			if (bmsg.getWhat() == BufferUpdate.DIRTY_CHANGED
				&& bmsg.getBuffer() == view.getBuffer()
				&& !bmsg.getBuffer().isDirty())
			{
				// buffer save detected
				if (jEdit.getBooleanProperty("infoviewer.autoupdate")
					&& jEdit.getBooleanProperty("infoviewer.autoupdate.onSave") )
				{
					reload();
				}
			}
		}
		else if (msg instanceof PropertiesChanged)
		{
			updateBookmarksMenu();
			updateGoMenu();
			updateTimers();
		}
	}

	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);
	}

	public void focusAddressBar()
	{
		urlField.requestFocus(true);
	}

	public void focusOnDefaultComponent()
	{
		viewer.requestFocus();
	}

	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);

		if (periodicTimer != null)
			periodicTimer.stop();
	}

	public String getShortURL()
	{
		return shortURL;
	}

	public String getBaseURL()
	{
		return baseURL;
	}

	// {{{ createActions ()
	private void createActions()
	{
		aOpenFile = new infoviewer.actions.open_file();
		aOpenBuffer = new infoviewer.actions.open_buffer();
		aEditURL = new infoviewer.actions.edit_url();
		aReload = new infoviewer.actions.reload();
		aClose = new infoviewer.actions.close();
		aCopy = new infoviewer.actions.copy();
		aSelectAll = new infoviewer.actions.select_all();
		aBack = new infoviewer.actions.back();
		aOpenLocation = new infoviewer.actions.OpenLocation();
		aForward = new infoviewer.actions.forward();
		aHome = new infoviewer.actions.home();
		aBookmarksAdd = new infoviewer.actions.bookmarks_add();
		aBookmarksEdit = new infoviewer.actions.bookmarks_edit();
		aToggleSidebar = new infoviewer.actions.ToggleSidebar();

		aAbout = new infoviewer.actions.about();
		aFollowLink = new infoviewer.actions.follow_link();
	}

	// }}}

	// {{{ createMenu()
	private JMenuBar createMenu()
	{
		// File menu
		JMenu mFile = new JMenu(props("infoviewer.menu.file"));
		mFile.setMnemonic(props("infoviewer.menu.file.mnemonic").charAt(0));
		mFile.add(aOpenFile);
		mFile.add(aOpenBuffer);
		mFile.add(aEditURL);
		mFile.add(aReload);
		mFile.add(new JSeparator());
		mFile.add(aClose);

		// Edit menu
		JMenu mEdit = new JMenu(props("infoviewer.menu.edit"));
		mEdit.setMnemonic(props("infoviewer.menu.edit.mnemonic").charAt(0));
		mEdit.add(aCopy);
		mEdit.add(aSelectAll);

		// View menu
		JMenu mView = new JMenu(props("infoviewer.menu.view"));
		mView.setMnemonic(props("infoviewer.menu.view.mnemonic").charAt(0));
		JMenuItem item = aToggleSidebar.menuItem();
		mView.add(item);

		// Goto menu

		mGoto = new JMenu(props("infoviewer.menu.goto"));
		mGoto.setMnemonic(props("infoviewer.menu.goto.mnemonic").charAt(0));
		updateGoMenu();

		// Bookmarks menu
		mBmarks = new JMenu(props("infoviewer.menu.bmarks"));
		mBmarks.setMnemonic(props("infoviewer.menu.bmarks.mnemonic").charAt(0));
		updateBookmarksMenu();

		// Help menu
		mHelp = new JMenu(props("infoviewer.menu.help"));
		mHelp.setMnemonic(props("infoviewer.menu.help.mnemonic").charAt(0));
		updateHelpMenu();

		// Menubar
		JMenuBar mb = new JMenuBar();
		mb.add(mFile);
		mb.add(mEdit);
		mb.add(mView);
		mb.add(mGoto);
		mb.add(mBmarks);
		mb.add(mHelp);

		return mb;
	}

	// }}}

	// {{{
	private JToolBar createToolbar()
	{
		EnhancedJToolBar tb = new EnhancedJToolBar(JToolBar.HORIZONTAL);

		tb.add(aBack);
		tb.add(aForward);
		tb.add(aReload);
		tb.add(aHome);
		tb.add(aOpenFile);
		tb.add(aEditURL);
		tb.add(aOpenBuffer);

		tb.add(Box.createHorizontalGlue());

		bStartStop = new JButton(ICON_ANIM)
		{
			private static final long serialVersionUID = 3350768542711107896L;

			// Otherwise the animated gif keeps calling this method
			// even when
			// the component is no longer visible, causing a memory
			// leak.
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int w,
				int h)
			{
				if (!isDisplayable())
					return false;
				else
					return super.imageUpdate(img, infoflags, x, y, w, h);
			}

		};
		bStartStop.setDisabledIcon(ICON_NOANIM);
		bStartStop.setBorderPainted(false);
		bStartStop.setEnabled(false);
		tb.add(bStartStop);

		return tb;
	}

	// }}}

	public void toggleSideBar()
	{
	}

	private JPanel createAddressBar()
	{
		// the url textfield
		urlField = new HistoryTextField("infoviewer");
		urlField.setFocusAccelerator('l');
		urlField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				gotoURL(urlField.getText(), true, -1);
			}
		});

		// url textfield and label
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(props("infoviewer.label.gotoURL")), BorderLayout.WEST);
		panel.add(urlField, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createStatusBar()
	{
		// the status text field
		status = new JLabel(GREET);
		status.setBorder(new BevelBorder(BevelBorder.LOWERED));
		status.setFont(new Font("Dialog", Font.PLAIN, 10));
		status.setMinimumSize(new Dimension(100, status.getPreferredSize().height));

		// the title text field
		title = new JLabel("No Document");
		title.setBorder(new BevelBorder(BevelBorder.LOWERED));
		title.setFont(new Font("Dialog", Font.PLAIN, 10));
		title.setMinimumSize(new Dimension(100, title.getPreferredSize().height));

		// status and title field
		JPanel statusBar = new JPanel(new GridLayout(1, 0));
		statusBar.add(status);
		statusBar.add(title);

		return statusBar;
	}

	/**
	 * Update the bookmarks menu according to the bookmarks stored in the
	 * properties.
	 */
	private synchronized void updateBookmarksMenu()
	{
		mBmarks.removeAll();
		mBmarks.add(aBookmarksAdd);
		mBmarks.add(aBookmarksEdit);
		mBmarks.add(new JSeparator());

		// add bookmarks
		bookmarks = new Bookmarks();
		for (int i = 0; i < bookmarks.getSize(); i++)
		{
			String title = bookmarks.getTitle(i);
			if (title.length() > 0 && title.charAt(0) == '-')
				mBmarks.add(new JSeparator());
			else
			{
				JMenuItem mi = new JMenuItem(title);
				mBmarks.add(mi);
				mi.setActionCommand(bookmarks.getURL(i));
				mi.addActionListener(bookmarkhandler);
			}
		}
	}

	private void updateHelpMenu()
	{
		mHelp.removeAll();
		mHelp.add(aAbout);

		// add a menu item for the docs
		JMenuItem mi = new JMenuItem(props("infoviewer.menu.help.readme"));
		mi.setActionCommand(props("infoviewer.menu.help.readme.url"));
		mi.addActionListener(bookmarkhandler);
		mi.setMnemonic(props("infoviewer.menu.help.readme.mnemonic").charAt(0));
		mHelp.add(mi);
	}

	private synchronized void updateGoMenu()
	{
		mGoto.removeAll();
		mGoto.add(aOpenLocation);
		mGoto.add(aBack);
		mGoto.add(aForward);
		mGoto.add(aHome);
		mGoto.add(new JSeparator());

		// add history
		TitledURLEntry[] entr = history.getGoMenuEntries();
		int pos = history.getHistoryPos();

		for (int i = 0; i < entr.length; i++)
		{
			JMenuItem mi = new JMenuItem(entr[i].getTitle(),
				entr[i].equals(currentURL) ? ICON_CHECK : ICON_NOCHECK);

			mi.setActionCommand(entr[i].getURL());
			mi.addActionListener(historyhandler);
			mGoto.add(mi);
		}
	}

	private synchronized void updateGoMenuTitles()
	{
		TitledURLEntry[] entr = history.getGoMenuEntries();
		for (int i = 0; i < entr.length; i++)
		{

			JMenuItem mi = mGoto.getItem(i + 5);
			if (mi == null)
			{
				mi = new JMenuItem();
				mGoto.add(mi);
			}
			mi.setText(entr[i].getTitle());
		}
	}

	private void updateActions()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				aForward.setEnabled(history.hasNext());
				aBack.setEnabled(history.hasPrevious());
				aEditURL.setEnabled(currentURL != null);
				bStartStop.setEnabled(currentStatus == LOADING);
			}
		});
	}

	private void updateStatus()
	{
		switch (currentStatus)
		{
		case LOADING:
			setStatusText(props("infoviewer.status.loading", new Object[] { currentURL
				.getURL() }));
			break;
		case READY:
			int size = viewer.getDocument().getLength();
			setStatusText(props("infoviewer.status.ready", new Integer[] { new Integer(
				size) }));
			break;
		case ERROR:
			setStatusText(props("infoviewer.status.error"));
			break;
		default:
			setStatusText(GREET);
			break;
		}

		updateActions();
	}

	private void updateTimers()
	{
		if (periodicTimer != null)
			periodicTimer.stop();

		if (jEdit.getBooleanProperty("infoviewer.autoupdate"))
		{
			if (jEdit.getBooleanProperty("infoviewer.autoupdate.periodically"))
			{
				try
				{
					periodicDelay = Integer
						.parseInt(jEdit
							.getProperty("infoviewer.autoupdate.periodically.delay"));
				}
				catch (NumberFormatException e)
				{
					periodicDelay = 20000;
				}

				periodicTimer = new Timer(periodicDelay, new ActionListener()
				{
					public void actionPerformed(ActionEvent evt)
					{
						if (currentStatus != LOADING && currentURL != null)
						{
							Log.log(Log.DEBUG, this,
								"periodic update (every "
									+ periodicDelay + "ms): "
									+ currentURL);
							reload();
						}
					}
				});

				periodicTimer.setInitialDelay(periodicDelay);
				periodicTimer.setRepeats(true);
				periodicTimer.setCoalesce(true);
				periodicTimer.start();
			}
		}
	}

	private void pageComplete()
	{
		// restore previous vertical scrollbar value, if page was
		// reloaded
		if (previousScrollBarValue >= 0)
		{
			JScrollBar jsb = scrViewer.getVerticalScrollBar();
			if (previousScrollBarValue < jsb.getMaximum())
				jsb.setValue(previousScrollBarValue);
			else jsb.setValue(jsb.getMaximum());
		}

		// try to get the title of the document
		Document doc = viewer.getDocument();
		if (doc != null)
		{
			String newTitle = getTitleFromDocument(doc);
			if (currentURL != null)
			{
				currentURL.setTitle(newTitle);
			}
			// set the new window title
			setTitle(newTitle);
			// update title in the "Go" menu history
			updateGoMenuTitles();
		}

		currentStatus = READY;
		updateStatus();
	}

	/** try to get the title of the document */
	private String getTitleFromDocument(Document doc)
	{
		Object obj = doc.getProperty(Document.TitleProperty);
		if (obj == null)
			return currentURL != null ? currentURL.getURL()
				: props("infoviewer.notitle");
		else
			return obj.toString();
	}

	private void setStatusText(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				status.setText(text);
			}
		});
	}

	private void setTitle(final String text)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				title.setText(text);
			}
		});
	}


	protected void dismiss()
	{
		DockableWindowManager dwm = jEdit.getActiveView().getDockableWindowManager();
		String name = getName();
		dwm.hideDockableWindow(name);
	}

	protected void showError(String errortext)
	{
		viewer.getDocument().putProperty(Document.StreamDescriptionProperty, null);
		viewer.getEditorKit().createDefaultDocument();
		viewer.setContentType("text/html");
		viewer.setText("<html><head></head><body>\n" + "<h1>Error</h1><p>\n" + errortext
			+ "\n</body></html>");
		currentURL = null;
		currentStatus = ERROR;
		updateStatus();
	}

	/** convenience method for jEdit.getProperty(String). */
	private static String props(String key)
	{
		return jEdit.getProperty(key);
	}

	/** convenience method for jEdit.getProperty(String,Object[]). */
	private static String props(String key, Object[] args)
	{
		return jEdit.getProperty(key, args);
	}


	// greet string
	private final static String GREET = props("infoviewer.greetstring", new Object[] {
		props("infoviewer.title"), props("plugin.infoviewer.InfoViewerPlugin.version") });

	// status numbers for updateStatus()
	private final static int LOADING = 1;

	private final static int READY = 2;

	private final static int ERROR = 3;

	// icons
	private final static ImageIcon ICON_ANIM = new ImageIcon(InfoViewer.class
		.getResource("images/fish_anim.gif"));

	private final static ImageIcon ICON_NOANIM = new ImageIcon(InfoViewer.class
		.getResource("images/fish.gif"));

	private final static ImageIcon ICON_CHECK = new ImageIcon(InfoViewer.class
		.getResource("images/checkmenu_check.gif"));

	private final static ImageIcon ICON_NOCHECK = new ImageIcon(InfoViewer.class
		.getResource("images/checkmenu_nocheck.gif"));

	// infoviewer actions
	private InfoViewerAction aOpenFile;

	private InfoViewerAction aOpenBuffer;

	private InfoViewerAction aEditURL;

	private InfoViewerAction aReload;

	private InfoViewerAction aClose;

	private InfoViewerAction aCopy;

	private InfoViewerAction aSelectAll;

	private InfoViewerAction aBack;

	private InfoViewerAction aOpenLocation;

	private InfoViewerAction aForward;

	private InfoViewerAction aHome;

	private InfoViewerAction aBookmarksAdd;

	private InfoViewerAction aBookmarksEdit;

	private InfoViewerAction aAbout;

	protected ToggleSidebar aToggleSidebar;

	private infoviewer.actions.follow_link aFollowLink;

	// gui elements
	private JLabel status;

	private JLabel title;

	private EnhancedJEditorPane viewer;

	protected HistoryTextField urlField;

	private JButton bStartStop;

	private JMenu mGoto;

	private JMenu mBmarks;

	private JMenu mHelp;

	// misc
	private View view;

	private TitledURLEntry currentURL;

	private int currentStatus;

	private Bookmarks bookmarks;

	private History history;

	private URLButtonHandler bookmarkhandler;

	private URLButtonHandler historyhandler;

	private boolean isDocked;

	private Timer periodicTimer;

	private int periodicDelay;

	private int previousScrollBarValue;

	protected String baseURL;

	private String shortURL;

	private ActionContext actionContext;

	private ActionSet actionSet;

	private class URLButtonHandler implements ActionListener
	{
		private boolean addToHistory = true;

		public URLButtonHandler(boolean addToHistory)
		{
			this.addToHistory = addToHistory;
		}

		/**
		 * A bookmark was selected in the Bookmarks menu. Open the
		 * corresponding URL in the InfoViewer. The URL will be added to
		 * the history, if this URLButtonHandler was initialized with
		 * <code>addToHistory = true</code>.
		 */
		public void actionPerformed(ActionEvent evt)
		{
			String cmd = evt.getActionCommand();
			gotoURL(cmd, addToHistory, -1);
		}
	}

	private class MouseHandler extends MouseAdapter
	{
		JPopupMenu popup = null;

		public void mousePressed(MouseEvent evt)
		{
			if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
			{
				evt.consume();

				AccessibleText txt = viewer.getAccessibleContext()
					.getAccessibleText();

				if (txt != null && txt instanceof AccessibleHypertext)
				{
					AccessibleHypertext hyp = (AccessibleHypertext) txt;
					int charIndex = hyp.getIndexAtPoint(evt.getPoint());
					int linkIndex = hyp.getLinkIndex(charIndex);
					if (linkIndex >= 0)
					{
						// user clicked on a link
						aFollowLink.setEnabled(true);
						aFollowLink.setClickPoint(evt.getPoint());
					}
					else
						aFollowLink.setEnabled(false);
				}

				JPopupMenu popup = getPopup();
				popup.show(viewer, evt.getX() - 1, evt.getY() - 1);
			}
		}

		private JPopupMenu getPopup()
		{
			if (popup == null)
			{
				popup = new JPopupMenu();
				popup.add(aBack);
				popup.add(aForward);
				popup.addSeparator();
				popup.add(aEditURL);
				popup.add(aOpenBuffer);
				popup.add(aReload);
				popup.addSeparator();
				popup.add(aFollowLink);
			}
			return popup;
		}
	}


	public class ArrowKeyHandler
	{
		int[] keys;

		public ArrowKeyHandler()
		{
			keys = new int[] { KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
				KeyEvent.VK_RIGHT, KeyEvent.VK_HOME, KeyEvent.VK_END,
				KeyEvent.VK_PAGE_UP, KeyEvent.VK_PAGE_DOWN };
			Arrays.sort(keys);
		}

		public void processKeyEvent(KeyEvent e)
		{
			if (Arrays.binarySearch(keys, e.getID()) > -1)
			{
				scrViewer.processKeyEvent(e);
				e.consume();
			}
		}

	}

	class KeyHandler extends KeyAdapter
	{
		public void keyPressed(KeyEvent evt)
		{
			if (evt.getKeyCode() == KeyEvent.VK_ESCAPE)
			{
				dismiss();
				evt.consume();
			}
		}
	}

	class MyScrollPane extends JScrollPane
	{
		public MyScrollPane(JComponent c)
		{
			super(c);
		};

		public void processKeyEvent(KeyEvent e)
		{
			super.processKeyEvent(e);
		}

		private static final long serialVersionUID = 390984816401470412L;
	}

	public static final long serialVersionUID = 1236527;

}
