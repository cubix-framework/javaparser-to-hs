package infoviewer;

// import infoviewer.lucene.SearchPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.help.HelpSearchPanel;
import org.gjt.sp.jedit.help.HelpTOCPanel;
import org.gjt.sp.jedit.help.HelpViewerInterface;
import org.gjt.sp.util.Log;

/**
 * An infoviewer with a helpful side-bar. Reuses the same sidebar from the
 * original HelpViewer but is intended to be extended with other kinds of views
 * later.
 * 
 * @author ezust
 * @version $Id: HelpInfoViewer.java 20753 2012-01-12 04:14:36Z ezust $
 */
public class HelpInfoViewer extends InfoViewer implements HelpViewerInterface {

	public void toggleSideBar() {
		showSideBar = aToggleSidebar.isSelected();
		remove(centralComponent);
		innerPanel.remove(centralComponent);
		if (showSideBar) 
		{
			splitter.setLeftComponent(tabs);
			splitter.setRightComponent(scrViewer);
			centralComponent = splitter;
			splitter.setDividerLocation(jEdit.getIntegerProperty("helpviewer.splitter", 250));
			
		} else 
		{
			int dl = splitter.getDividerLocation();
			if (dl > 0) jEdit.setIntegerProperty("helpviewer.splitter", dl);
			centralComponent = scrViewer;
		}
		
		innerPanel.add(BorderLayout.CENTER, centralComponent);
		scrViewer.repaint();
		repaint();
	}

	public HelpInfoViewer() 
	{
		this(null, null, true);
	}

	public HelpInfoViewer(View view, String position) 
	{
		this(view, position, true);
	}
	
	public HelpInfoViewer(View view, String position, boolean sidebar) 
	{
		super(view, position);
		if (sidebar) setName("helpviewer");
		showSideBar = sidebar;
		
		// Log.log(Log.WARNING, this.getClass(), "HelpInfoViewer!");
		tabs = new JTabbedPane();
		toc = new HelpTOCPanel(this);
		tabs.addTab(jEdit.getProperty("helpviewer.toc.label"), toc);
		searchPanel = new HelpSearchPanel(this);
		tabs.addTab(jEdit.getProperty("helpviewer.search.label"), searchPanel);
//		tabs.addTab("Lucene", new SearchPanel(this));
		tabs.setMinimumSize(new Dimension(0, 20));
		scrViewer.setMinimumSize(new Dimension(0, 20));

		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, scrViewer);
		splitter.setBorder(null);
		if (showSideBar) {
			centralComponent = splitter;
			splitter.setDividerLocation(jEdit.getIntegerProperty("helpviewer.splitter", 250));
		}
		else 
		{
			centralComponent = scrViewer;
		}
		innerPanel.add(BorderLayout.CENTER, centralComponent);
		queueTOCReload();
		setVisible(true);
		SwingUtilities.invokeLater(new Runnable() 
			{
			public void run() 
			{
				splitter.setDividerLocation(jEdit.getIntegerProperty(
						"helpviewer.splitter", 250));
				requestFocus();
			}
		});
	}

	// {{{ dispose
	public void dispose() {
		int dl = splitter.getDividerLocation();
		if (dl > 0) jEdit.setIntegerProperty("helpviewer.splitter", dl);		
	} // }}}
		
	// {{{ queueTOCReload() method

	public void queueTOCReload() 
	{
		SwingUtilities.invokeLater(new Runnable() 
			{
			public void run() {
				queuedTOCReload = false;
				toc.load();
			}
		});
	} // }}}

	// {{{ ActionHandler class
	class ActionHandler implements ActionListener {
		// {{{ actionPerformed() class
		public void actionPerformed(ActionEvent evt) {
			Object source = evt.getSource();
			String url = evt.getActionCommand();
			if (!url.equals("")) {
				gotoURL(url, false, -1);
				return;
			}
		} // }}}
	} // }}}

	public void setTitle(String newTitle) {
		firePropertyChange("title", null, newTitle);
	}

	public Component getComponent() {
		return this;
	}

	public void gotoURL(String url, boolean addToHistory, int scrollPos) {

		if (baseURL == null) try {
			URI baseURI = new File(MiscUtilities.constructPath(
					jEdit.getJEditHome(), "doc")).toURI();
			baseURL = baseURI.toURL().toString();
		} catch (MalformedURLException mu) {
			Log.log(Log.ERROR, this, mu);
			// what to do?
		}
		if (url == null)
			return;
		url = url.trim();
		if (url.length() == 0)
			return;
		String shortURL;
		if(MiscUtilities.isURL(url))
		{
			if(url.startsWith(baseURL))
			{
				shortURL = url.substring(baseURL.length());
				if(shortURL.startsWith("/"))
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
			if(baseURL.endsWith("/"))
				url = baseURL + url;
			else
				url = baseURL + '/' + url;
		}
		
		try {
			URL u = new URL(url);
			gotoURL(u, addToHistory, scrollPos);
		} catch (MalformedURLException mu) {
			urlField.setText(url);
			showError(jEdit.getProperty("infoviewer.error.badurl.message",
					new Object[] { mu }));
		}
	}

	// {{{ Private members

	// {{{ Instance members
	private JSplitPane splitter;

	private JTabbedPane tabs;

	private HelpSearchPanel searchPanel;

	private HelpTOCPanel toc;

	private boolean queuedTOCReload;

	private boolean showSideBar = true;

	private Component centralComponent;
	// }}}

}
