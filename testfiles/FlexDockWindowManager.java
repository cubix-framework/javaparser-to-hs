package flexdock;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.flexdock.docking.DockingConstants;
import org.flexdock.view.Viewport;
import org.gjt.sp.jedit.PerspectiveManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;

@SuppressWarnings("serial")
public class FlexDockWindowManager extends DockableWindowManager {

	private Viewport port;
	private org.flexdock.view.View main;
	private Map<String, org.flexdock.view.View> regions =
		new HashMap<String, org.flexdock.view.View>();
	private Map<String, Float> splits =
		new HashMap<String, Float>();
		
	public FlexDockWindowManager(View view, DockableWindowFactory instance,
			ViewConfig config)
	{
		super(view, instance, config);
		setLayout(new BorderLayout());
		port = new Viewport();
		add(port, BorderLayout.CENTER);
		PerspectiveManager.setPerspectiveDirty(true);
		splits.put(DockingConstants.NORTH_REGION, 0.2f);
		splits.put(DockingConstants.WEST_REGION, 0.2f);
		splits.put(DockingConstants.SOUTH_REGION, 0.2f);
		splits.put(DockingConstants.EAST_REGION, 0.6f);
	}

	@Override
	protected void dockingPositionChanged(String name,
		String oldPosition, String newPosition)
	{
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
		return null;
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

	@Override
	public void applyDockingLayout(DockingLayout docking)
	{
		// 'docking' is null if jEdit was started without a perspective file
		boolean loaded = false;
		if (docking != null)
		{
		}
		if (! loaded) // No saved layout - just use the docking positions specified by jEdit properties
			super.applyDockingLayout(null);
	}

	@Override
	public void showDockableWindow(String name)
	{
		JComponent window = getDockable(name);
		String position = getDockablePosition(name); 
		if (window == null)
			window = createDockable(name);
		if (window == null)
			return;
		String title = getDockableTitle(name);
		org.flexdock.view.View v = new org.flexdock.view.View(name, title);
		v.setContentPane(window);
		String region;
		if (position.equals(DockableWindowManager.LEFT))
			region = DockingConstants.WEST_REGION;
		else if (position.equals(DockableWindowManager.RIGHT))
			region = DockingConstants.EAST_REGION;
		else if (position.equals(DockableWindowManager.TOP))
			region = DockingConstants.NORTH_REGION;
		else
			region = DockingConstants.SOUTH_REGION;
		org.flexdock.view.View regionView = regions.get(region);
		if (regionView == null) {
			main.dock(v, region, splits.get(region));
			regions.put(region, v);
		} else {
			regionView.dock(v, DockingConstants.CENTER_REGION);
		}
	}

	@Override
	public void setMainPanel(JPanel panel)
	{
		main = new org.flexdock.view.View("main", "main");
		main.setContentPane(panel);
		port.dock(main);
	}

	public class FlexDockDockingArea implements DockingArea {
		public void showMostRecent() {
		}
		public String getCurrent() {
			return null;
		}
		public void show(String name) {
		}
	}
	
	public DockingArea getBottomDockingArea() {
		return null;
	}

	public DockingArea getLeftDockingArea() {
		return null;
	}

	public DockingArea getRightDockingArea() {
		return null;
	}

	public DockingArea getTopDockingArea() {
		return null;
	}

	@Override
	public void disposeDockableWindow(String name) {
	}

}
