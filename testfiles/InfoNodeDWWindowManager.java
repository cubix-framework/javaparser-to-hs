package infonode;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.properties.TabWindowProperties;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.StringViewMap;
import net.infonode.util.Direction;

import org.gjt.sp.jedit.PerspectiveManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;

@SuppressWarnings("serial")
public class InfoNodeDWWindowManager extends DockableWindowManager {

	static private String MAIN_VIEW_NAME = "main";
	private RootWindow rootWindow = null;
	private StringViewMap views;
	private net.infonode.docking.View mainView;
	private JPanel center;
	private Map<String, TabWindow> tabWindows;
	
	public InfoNodeDWWindowManager(View view, DockableWindowFactory instance,
			ViewConfig config)
	{
		super(view, instance, config);
		setLayout(new BorderLayout());
		views = new StringViewMap();
		center = new JPanel(new BorderLayout());
		mainView = new net.infonode.docking.View(MAIN_VIEW_NAME, null, center);
		mainView.setName(MAIN_VIEW_NAME);
		mainView.getViewProperties().setAlwaysShowTitle(false);
		views.addView(mainView);
		rootWindow = DockingUtil.createRootWindow(views, true);
		rootWindow.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(center.getBackground());
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getWindowBar(Direction.UP).setEnabled(true);
		rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
		rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
		setLayout(new BorderLayout());
		add(rootWindow, BorderLayout.CENTER);
		tabWindows = new HashMap<String, TabWindow>();
		tabWindows.put(DockableWindowManager.LEFT, createTabWindow(Direction.LEFT, Direction.UP));
		tabWindows.put(DockableWindowManager.RIGHT, createTabWindow(Direction.RIGHT, Direction.DOWN));
		tabWindows.put(DockableWindowManager.BOTTOM, createTabWindow(Direction.UP, Direction.RIGHT));
		tabWindows.put(DockableWindowManager.TOP, createTabWindow(Direction.UP, Direction.RIGHT));
		SplitWindow sw = new SplitWindow(true, 0.25f, tabWindows.get(DockableWindowManager.LEFT), mainView);
		sw = new SplitWindow(true, 0.75f, sw, tabWindows.get(DockableWindowManager.RIGHT));
		sw = new SplitWindow(false, 0.25f, tabWindows.get(DockableWindowManager.TOP), sw);
		sw = new SplitWindow(false, 0.75f, sw, tabWindows.get(DockableWindowManager.BOTTOM));
		rootWindow.setWindow(sw);
		tabWindows.get(DockableWindowManager.TOP).setVisible(false);
		tabWindows.get(DockableWindowManager.BOTTOM).setVisible(false);
		tabWindows.get(DockableWindowManager.LEFT).setVisible(false);
		tabWindows.get(DockableWindowManager.RIGHT).setVisible(false);
		PerspectiveManager.setPerspectiveDirty(true);
	}

	private TabWindow createTabWindow(Direction side, Direction dir)
	{
		TabWindow tw = new TabWindow();
		TabWindowProperties twp = tw.getTabWindowProperties();
		twp.getTabbedPanelProperties().setTabAreaOrientation(side);
		twp.getTabProperties().getTitledTabProperties().getNormalProperties().setDirection(dir);
		return tw;
	}
	
	@Override
	protected void dockingPositionChanged(String name,
		String oldPosition, String newPosition)
	{
		showDockableWindow(name);
	}

	@Override
	public void closeCurrentArea()
	{
	}

	@Override
	public JComponent floatDockableWindow(String name)
	{
		return null;
	}

	@Override
	public DockingLayout getDockingLayout(ViewConfig config)
	{
		InfoNodeDWDockingLayout layout = null;//new InfoNodeDWDockingLayout(wm);
		return layout;
	}

	@Override
	public void hideDockableWindow(String name)
	{
	}

	@Override
	public boolean isDockableWindowDocked(String name)
	{
		return false;
	}

	@Override
	public boolean isDockableWindowVisible(String name)
	{
		return false;
	}

	private void loadInfoNodeDWLayout(String filename)
	{
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
			//PersistenceDelegateCallback callback = new PersistenceCallback();
			//wm.getPersistenceDelegate().merge(inputStream, MergePolicy.RESET, callback);
			inputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void applyDockingLayout(DockingLayout docking)
	{
		// 'docking' is null if jEdit was started without a perspective file
		boolean loaded = false;
		if (docking != null)
		{
			InfoNodeDWDockingLayout layout = (InfoNodeDWDockingLayout) docking;
			String filename = layout.getLayoutFilename();
			if (filename != null) {
				java.io.File f = new File(filename);
				if (f.exists()) {
					loadInfoNodeDWLayout(filename);
					loaded = true;
				}
			}
		}
		if (! loaded) // No saved layout - just use the docking positions specified by jEdit properties
			super.applyDockingLayout(null);
	}

	@Override
	public void showDockableWindow(String name)
	{
		net.infonode.docking.View v = views.getView(name);
		if (v == null) {
			JComponent c = createDockable(name);
			v = new net.infonode.docking.View(getDockableTitle(name), null, c);
			v.setName(name);
			views.addView(v);
			String position = getDockablePosition(name);
			TabWindow tw = tabWindows.get(position);
			if (tw != null) {
				if (v.getWindowParent() != tw)
					tw.addTab(v);
				tw.setVisible(true);
			}
		}
		v.requestFocus();
	}

	/*
	private void activateToolWindow(ToolWindow tw)
	{
		ToolWindowAnchor anchor = tw.getAnchor();
		if (anchor != null)
		{
			ToolWindowBar bar = wm.getToolWindowBar(anchor);
			if (! bar.isVisible())
				bar.setVisible(true);
		}
		tw.setActive(true);
		focusDockable(tw.getId());
	}
	*/

	@Override
	public void setMainPanel(JPanel panel)
	{
		center.add(panel, BorderLayout.CENTER);
	}

	public class InfoNodeDWDockingArea implements DockingArea {
		public InfoNodeDWDockingArea() {//ToolWindowAnchor anchor)
		}
		public void showMostRecent() {
		}
		public String getCurrent() {
			return null;
		}
		public void show(String name) {
		}
	}
	
	public DockingArea getBottomDockingArea() {
		return new InfoNodeDWDockingArea();//ToolWindowAnchor.BOTTOM);
	}

	public DockingArea getLeftDockingArea() {
		return new InfoNodeDWDockingArea();//ToolWindowAnchor.LEFT);
	}

	public DockingArea getRightDockingArea() {
		return new InfoNodeDWDockingArea();//ToolWindowAnchor.RIGHT);
	}

	public DockingArea getTopDockingArea() {
		return new InfoNodeDWDockingArea();//ToolWindowAnchor.TOP);
	}

	@Override
	public void dockableTitleChanged(String dockable, String newTitle) {
		/*
		ToolWindow tw = getToolWindow(dockable);
		if (tw != null)
			tw.setTitle(newTitle);
			*/
	}

	@Override
	protected void applyAlternateLayout(boolean alternateLayout) {
		//setPushAwayMode();
	}

	public void disposeDockableWindow(String name) {
		/*ToolWindow tw = getToolWindow(name);
		if (tw != null)
			wm.unregisterToolWindow(name);
			*/
	}

}
