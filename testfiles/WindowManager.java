package infonode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.properties.TabWindowProperties;
import net.infonode.docking.theme.BlueHighlightDockingTheme;
import net.infonode.docking.theme.ClassicDockingTheme;
import net.infonode.docking.theme.DefaultDockingTheme;
import net.infonode.docking.theme.DockingWindowsTheme;
import net.infonode.docking.theme.GradientDockingTheme;
import net.infonode.docking.theme.LookAndFeelDockingTheme;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.theme.SlimFlatDockingTheme;
import net.infonode.docking.theme.SoftBlueIceDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.util.Direction;

import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableLayout;
import org.gjt.sp.jedit.gui.DockableWindowContainer;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.PanelWindowContainer;
import org.gjt.sp.jedit.gui.DockableWindowFactory.Window;
import org.gjt.sp.jedit.msg.DockableWindowUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;

@SuppressWarnings("serial")
public class WindowManager extends DockableWindowManager {

	private static final String MAIN_VIEW_NAME = "Main";
	private static final String DEFAULT_FILE =
		Plugin.getConfigDirectory() + File.separator + "perspective.sav";
	private org.gjt.sp.jedit.View view;
	private DockableWindowFactory factory;
	private JEditViewMap viewMap;
	private RootWindow rootWindow;
	private JComponent center;
	private View mainView;
	private PanelWindowContainer topPanel, bottomPanel, leftPanel, rightPanel;
	private TabWindow leftTab, rightTab, bottomTab, topTab;
	private DockingWindowsTheme currentTheme = null;
	private HashSet<DockingWindow> dummyViews = new HashSet<DockingWindow>();
	private ViewCloseListener viewCloseListener = new ViewCloseListener();
	private ViewCreateListener viewCreateListener = new ViewCreateListener();
	private static DockingWindowsTheme [] themes = new DockingWindowsTheme[] {
			new BlueHighlightDockingTheme(),
			new ClassicDockingTheme(),
			new DefaultDockingTheme(),
			new GradientDockingTheme(),
			new LookAndFeelDockingTheme(),
			new ShapedGradientDockingTheme(),
			new SlimFlatDockingTheme(),
			new SoftBlueIceDockingTheme()
		};
	private static String [] themeNames;
	
	{
		themeNames = new String[themes.length];
		for (int i = 0; i < themes.length; i++)
			themeNames[i] = themes[i].getName();
	}

	public void setEditorComponent(Component c) {
		center.add(c, BorderLayout.CENTER);
	}
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (DockableLayout.TOP_TOOLBARS.equals(constraints))
			center.add(comp, BorderLayout.NORTH);
		else if (DockableLayout.BOTTOM_TOOLBARS.equals(constraints))
			center.add(comp, BorderLayout.SOUTH);
		else
			super.addImpl(comp, constraints, index);
	}
	@Override
	public void construct(org.gjt.sp.jedit.View view, DockableWindowFactory factory,
			ViewConfig config) {
		this.view = view;
		this.factory = factory;
		viewMap = new JEditViewMap(this);
		center = new JPanel(new BorderLayout());
		mainView = new View(MAIN_VIEW_NAME, null, center);
		mainView.setName(MAIN_VIEW_NAME);
		mainView.getViewProperties().setAlwaysShowTitle(false);
		viewMap.addView(MAIN_VIEW_NAME, mainView);
		rootWindow = DockingUtil.createRootWindow(viewMap, true);
		rootWindow.getRootWindowProperties().getWindowAreaProperties().setBackgroundColor(center.getBackground());
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		rootWindow.getWindowBar(Direction.UP).setEnabled(true);
		rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
		rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);

		setLayout(new BorderLayout());
		add(rootWindow, BorderLayout.CENTER);
		invalidate();
		topPanel = new PanelWindowContainer(this,TOP,config.topPos);
		leftPanel = new PanelWindowContainer(this,LEFT,config.leftPos);
		bottomPanel = new PanelWindowContainer(this,BOTTOM,config.bottomPos);
		rightPanel = new PanelWindowContainer(this,RIGHT,config.rightPos);
	}
	@Override
	public DockableWindowManager getNewInstance() {
		return new WindowManager();
	}
	public void init() {
		EditBus.addToBus(this);
	}
	@Override
	public void applyViewConfig(ViewConfig config) {
		if (new File(DEFAULT_FILE).exists())
			load(DEFAULT_FILE);
		else {
			convertViewConfig(config);
		}
	}
	private void convertViewConfig(ViewConfig config) {
		leftTab = createTabWindow(Direction.LEFT, Direction.UP);
		rightTab = createTabWindow(Direction.RIGHT, Direction.DOWN);
		bottomTab = createTabWindow(Direction.UP, Direction.RIGHT);
		topTab = createTabWindow(Direction.UP, Direction.RIGHT);
		setViewLayout();
		
		String [] dockables = factory.getRegisteredDockableWindows();
		for (int i = 0; i < dockables.length; i++) {
			String dockable = dockables[i];
			String pos = getDockablePosition(dockable);
			if (pos == null || pos.equals(DockableWindowManager.FLOATING))
				continue;
			View v = createDummyView(dockable);
			TabWindow tw = null;
			if (pos.equals(DockableWindowManager.LEFT))
				tw = leftTab;
			else if (pos.equals(DockableWindowManager.RIGHT))
				tw = rightTab;
			else if (pos.equals(DockableWindowManager.BOTTOM))
				tw = bottomTab;
			else if (pos.equals(DockableWindowManager.TOP))
				tw = topTab;
			if (tw != null) {
				tw.addTab(v);
			}
		}
		minimizeTabWindows(topTab, Direction.UP);
		minimizeTabWindows(bottomTab, Direction.DOWN);
		minimizeTabWindows(leftTab, Direction.LEFT);
		minimizeTabWindows(rightTab, Direction.RIGHT);
		super.applyViewConfig(config);
	}
	private void minimizeTabWindows(TabWindow tw, Direction dir) {
		for (int i = 0; i < tw.getChildWindowCount(); i++) {
			DockingWindow w = tw.getChildWindow(i);
			//w.minimize(dir);
			w.addListener(viewCreateListener);
		}
		tw.setVisible(false);
	}
	private void setViewLayout() {
		DockingWindow sw = null;
		sw = addArea(leftTab, mainView, true, true, 0.25f);
		sw = addArea(rightTab, sw, true, false, 0.75f);
		sw = addArea(bottomTab, sw, false, false, 0.75f);
		sw = addArea(topTab, sw, false, true, 0.25f);
		rootWindow.setWindow(sw);
	}
	
	private TabWindow createTabWindow(Direction side, Direction dir) {
		TabWindow tw = new TabWindow();
		TabWindowProperties twp = tw.getTabWindowProperties();
		twp.getTabbedPanelProperties().setTabAreaOrientation(side);
		twp.getTabProperties().getTitledTabProperties().getNormalProperties().setDirection(dir);
		return tw;
	}
	private DockingWindow addArea(TabWindow tw, DockingWindow dw, boolean isHorizontal, boolean areaFirst, float divider) {
		/*
		if (tw.getChildWindowCount() == 0)
			return dw;
			*/
		DockingWindow w1 = (areaFirst ? tw : dw);
		DockingWindow w2 = (areaFirst ? dw : tw);
		return new SplitWindow(isHorizontal, divider, w1, w2);
	}
	public String getDockableTitle(String name) {
		String title = jEdit.getProperty(name + ".longtitle");
		if (title == null)
			return getDockableShortTitle(name);
		else
			return title;
	}
	private String getDockableShortTitle(String name) 
	{
		String title = jEdit.getProperty(name + ".title");
		if(title == null)
			return "NO TITLE PROPERTY: " + name;
		else
			return title;
	}
	@Override
	public void close() {
		save(DEFAULT_FILE);
		for (int i = 0; i < viewMap.getViewCount(); i++) {
			View v = viewMap.getViewAtIndex(i);
			viewMap.removeView(v.getName());
		}
	}
	@Override
	public void closeCurrentArea() {
		// This is never called.
	}
	@Override
	public JPopupMenu createPopupMenu(DockableWindowContainer container,
			String dockable, boolean clone) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public JComponent floatDockableWindow(String name) {
		View v = constructDockableView(name);
		leftTab.addTab(v);
		v.undock(new Point(0,0));
		return (JComponent)v.getComponent();
	}
	@Override
	public PanelWindowContainer getBottomDockingArea() {
		return bottomPanel;
	}
	@Override
	public JComponent getDockable(String name) {
		View v = viewMap.getView(name);
		if (v == null || dummyViews.contains(v))
			return null;
		return (JComponent)v.getComponent();
	}
	@Override
	public PanelWindowContainer getLeftDockingArea() {
		return leftPanel;
	}
	@Override
	public PanelWindowContainer getRightDockingArea() {
		return rightPanel;
	}
	@Override
	public PanelWindowContainer getTopDockingArea() {
		return topPanel;
	}
	@Override
	public org.gjt.sp.jedit.View getView() {
		return view;
	}
	private void updateProperties()
	{
		if (view.isPlainView())
			return;

		String[] windowList = factory.getRegisteredDockableWindows();
		Vector<String> notify = new Vector<String>();
		for (int i = 0; i < windowList.length; i++) {
			String name = windowList[i];
			String position = getDockablePosition(name);
			if (position.equals(DockableWindowManager.FLOATING))
				continue;
			if (viewMap.getView(name) != null)
				continue;
			showDockableWindowNoNotify(name);
			notify.add(name);
		}
		for (int i = 0; i < notify.size(); i++) {
			notifyActivation(notify.get(i));
		}
	}

	@Override
	public void handleMessage(EBMessage msg) {
		if (msg instanceof PropertiesChanged)
			updateProperties();
/*
		if (msg instanceof DockableWindowUpdate)
		{
			if(((DockableWindowUpdate)msg).getWhat()
				== DockableWindowUpdate.PROPERTIES_CHANGED)
				propertiesChanged();
		}
		else if(msg instanceof PluginUpdate)
		{
			PluginUpdate pmsg = (PluginUpdate)msg;
			if(pmsg.getWhat() == PluginUpdate.LOADED)
			{
				Iterator<DockableWindowFactory.Window> iter = factory.getDockableWindowIterator();

				while(iter.hasNext())
				{
					DockableWindowFactory.Window w = iter.next();
					if(w.plugin == pmsg.getPluginJAR())
						addEntry(w);
				}

				propertiesChanged();
			}
			else if(pmsg.isExiting())
			{
				// we don't care
			}
			else if(pmsg.getWhat() == PluginUpdate.DEACTIVATED)
			{
				Iterator<Entry> iter = getAllPluginEntries(
					pmsg.getPluginJAR(),false);
				while(iter.hasNext())
				{
					Entry entry = iter.next();
					if(entry.container != null)
						entry.container.remove(entry);
				}
			}
			else if(pmsg.getWhat() == PluginUpdate.UNLOADED)
			{
				Iterator<Entry> iter = getAllPluginEntries(
					pmsg.getPluginJAR(),true);
				while(iter.hasNext())
				{
					Entry entry = iter.next();
					if(entry.container != null)
					{
						entry.container.unregister(entry);
						entry.win = null;
						entry.container = null;
					}
				}
			}
		}
		*/
	}
	@Override
	public boolean isDockableWindowDocked(String name) {
		return getDockable(name) != null;
	}
	@Override
	public boolean isDockableWindowVisible(String name) {
		return isDockableWindowDocked(name);
	}
	@Override
	public void hideDockableWindow(String name) {
		/*
		Object reason = DockableWindowUpdate.DEACTIVATED;
		EditBus.send(new DockableWindowUpdate(this, reason, name));
		Dockable d = DockingManager.getDockable(name);
		DockingManager.close(d);
		DockingManager.unregisterDockable(d);
		windows.remove(name);
		*/
	}
	private String getDockablePosition(String name) {
		return jEdit.getProperty(name + ".dock-position");
	}
	View createDummyView(String name) {
		View v = createDockableView(name, new JPanel());
		dummyViews.add(v);
		System.err.println("Constructing dummy view: " + name);
		return v;
	}
	private View createDockableView(String name, JComponent c) {
		View v = new View(getDockableTitle(name), null, c);
		v.setName(name);
		viewMap.addView(name, v);
		v.addListener(viewCloseListener);
		return v;
	}
	public View constructDockableView(String name) {
		System.err.println("Constructing dockable " + name);
		String position = getDockablePosition(name);
		Window w = factory.getDockableWindowFactory(name);
		JComponent c = w.createDockableWindow(view, position);
		return createDockableView(name, c);
	}
	@Override
	public void showDockableWindow(String name) {
		showDockableWindowNoNotify(name);
		notifyActivation(name);
	}
	private void notifyActivation(String name) {
		Object reason = DockableWindowUpdate.ACTIVATED;
		EditBus.send(new DockableWindowUpdate(this, reason, name));
	}
	private void showDockableWindowNoNotify(String name) {
		String position = getDockablePosition(name);
		View v = viewMap.getView(name);
		if (v == null) {
			v = constructDockableView(name);
			TabWindow tw = null;
			if (position.equals(DockableWindowManager.LEFT))
				tw = leftTab;
			else if (position.equals(DockableWindowManager.RIGHT))
				tw = rightTab;
			else if (position.equals(DockableWindowManager.BOTTOM))
				tw = bottomTab;
			else if (position.equals(DockableWindowManager.TOP))
				tw = topTab;
			if (tw != null) {
				if (v.getWindowParent() != tw)
					tw.addTab(v);
				tw.setVisible(true);
			} else { // floating
				FloatingWindow fw = rootWindow.createFloatingWindow(
					new Point(0, 0), v.getPreferredSize(), v);
				fw.getTopLevelAncestor().setVisible(true);
			}
		}
		else {
			viewCreateListener.checkFirstShow(v);
			v = viewMap.getView(name);
		}
		v.getWindowParent().setVisible(true);
		v.makeVisible();
	}
	public Component add(Component comp, int index) {
		//return mainView.add(comp, index);
		return super.add(comp, index);
	}
	public void load(String file) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			rootWindow.read(ois);
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Now setup the tab windows
		Vector<TabWindow> tabs = new Vector<TabWindow>();
		Iterator<DockingWindow> vs = dummyViews.iterator();
		while (vs.hasNext()) {
			DockingWindow w = vs.next();
			w.addListener(viewCreateListener);
			DockingWindow dw = w.getWindowParent();
			if (dw instanceof TabWindow)
				tabs.add((TabWindow) dw);
		}
		if (! tabs.isEmpty()) {
			leftTab = rightTab = topTab = bottomTab = tabs.get(0);
		}
		for (int i = 0; i < tabs.size(); i++) {
			TabWindow tw = tabs.get(i);
			DockingWindow selected = tw.getSelectedWindow();
			viewCreateListener.checkFirstShow(selected);
		}
	}
	public void save(String file) {
		ObjectOutputStream ous;
		try {
			ous = new ObjectOutputStream(new FileOutputStream(file));
			rootWindow.write(ous);
			ous.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	private void setTheme(String theme) {
		if (theme == null)
			return;
		for (int i = 0; i < themeNames.length; i++)
			if (themeNames[i].equals(theme)) {
				if (currentTheme != null)
					rootWindow.getRootWindowProperties().replaceSuperObject(
						currentTheme.getRootWindowProperties(), themes[i].getRootWindowProperties());
				else
					rootWindow.getRootWindowProperties().addSuperObject(themes[i].getRootWindowProperties());
				currentTheme = themes[i];
				return;
			}
	}
	public void selectTheme() {
		String defaultTheme = (currentTheme != null) ? currentTheme.getName() : themeNames[0];
		String theme = (String) JOptionPane.showInputDialog(view,
			"Select a theme:", "Themes", 0, null, themeNames, defaultTheme);
		setTheme(theme);
	}

	private class ViewCloseListener extends DockingWindowAdapter {
		@Override
		public void windowClosed(DockingWindow window) {
			dummyViews.remove(window);
			viewMap.removeView(window.getName());
		}
	}
	private class ViewCreateListener extends DockingWindowAdapter {
		private void checkFirstShow(DockingWindow window) {
			if (! dummyViews.remove(window))
				return;
			DockingWindow parent = window.getWindowParent();
			View v = constructDockableView(window.getName());
			parent.replaceChildWindow(window, v);
			v.makeVisible();
		}
		@Override
		public void windowMaximized(DockingWindow window) {
			checkFirstShow(window);
		}
		@Override
		public void windowRestored(DockingWindow window) {
			checkFirstShow(window);
		}
		@Override
		public void windowShown(DockingWindow window) {
			checkFirstShow(window);
		}
		@Override
		public void windowDocked(DockingWindow window) {
			checkFirstShow(window);
		}
		@Override
		public void windowUndocked(DockingWindow window) {
			checkFirstShow(window);
		}
	}
}
