package dockingFrames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;

import bibliothek.extension.gui.dock.preference.PreferenceDialog;
import bibliothek.extension.gui.dock.preference.PreferenceModel;
import bibliothek.extension.gui.dock.preference.PreferenceTreeDialog;
import bibliothek.extension.gui.dock.preference.PreferenceTreeModel;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CPreferenceModel;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.action.predefined.CMinimizeAction;
import bibliothek.gui.dock.common.event.CDockableAdapter;
import bibliothek.gui.dock.common.event.CDockableStateListener;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.layout.ThemeMap;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.CContentAreaCenterLocation;
import bibliothek.gui.dock.common.location.CFlapIndexLocation;
import bibliothek.gui.dock.common.location.CRectangleLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.gui.dock.event.DockHierarchyEvent;
import bibliothek.gui.dock.event.DockHierarchyListener;
import bibliothek.gui.dock.event.DockStationListener;
import bibliothek.gui.dock.util.ComponentWindowProvider;
import bibliothek.util.xml.XElement;

@SuppressWarnings("serial")
public class DfWindowManager extends DockableWindowManager
{
	private static final String RESTORE_LAYOUT = "RestoreViewAfterToggle";
	private Map<String, JEditDockable> created = new HashMap<String, JEditDockable>();
	private CControl control;
	private static JEditDockableFactory factory;
	private CWorkingArea mainArea;
	private DefaultSingleCDockable mainDockable;
	private DfDockingArea top, left, bottom, right;
	private JEditDockable focused;
	private CPreferenceModel prefs;
	private JEditDockStationListener listener;
	private JEditDockHierarchyListener hierarchyListener;
	private HashSet<DockStation> listenedStations;
	private CDockableStateListener stateListener;
	private boolean loadingLayout;
	private int toggleViewIndex;

	public DfWindowManager(View view, DockableWindowFactory instance,
			ViewConfig config)
	{
		super(view, instance, config);
		setLayout(new BorderLayout());
		control = new CControl(new ComponentWindowProvider(this));
		factory = new JEditDockableFactory();
		control.addMultipleDockableFactory("dockables", factory);
		add(BorderLayout.CENTER, control.getContentArea());
		mainArea = control.createWorkingArea("main");
		CGrid grid = new CGrid();
		grid.add(0, 0, 1, 1, mainArea);
		control.getContentArea().deploy(grid);
		setVisible(true);
		prefs = new CPreferenceModel(control);
		control.setPreferenceModel(prefs);
		control.addFocusListener(new CFocusListener()
		{
			public void focusGained(CDockable dockable)
			{
				if (dockable instanceof JEditDockable)
					focused = (JEditDockable) dockable;
			}
			public void focusLost(CDockable dockable)
			{
				if (focused == dockable)
					focused = null;
			}
		});
		listener = new JEditDockStationListener();
		listenedStations = new HashSet<DockStation>();
		hierarchyListener = new JEditDockHierarchyListener();
		stateListener = new DockableStateListener();
		loadingLayout = false;
		toggleViewIndex = -1;
	}

	static public void showPreferenceDialog(View view)
	{
		DfWindowManager dwm = (DfWindowManager) view.getDockableWindowManager();
		CControl control = dwm.getControl();
		Component owner = control.intern().getController().findRootWindow();
		PreferenceModel model = control.getPreferenceModel();
		if (model instanceof PreferenceTreeModel)
			PreferenceTreeDialog.openDialog((PreferenceTreeModel)model, owner);
		else
			PreferenceDialog.openDialog(model, owner);
	}

	@Override
	public void setMainPanel(JPanel panel)
	{
		mainDockable = new DefaultSingleCDockable("mainPanel", panel);
		mainDockable.getContentPane().setMinimumSize(new Dimension(10, 10));
		mainArea.add(mainDockable);
		mainDockable.setTitleShown(false);
        mainDockable.setVisible(true);
	}

	private JEditDockable getJEditDockable(String name)
	{
		JEditDockable d = created.get(name);
		if (d == null)
		{
			d = createJEditDockable(name, false);
			if (d == null)
				return null;
			control.add(d);
		}
		return d;
	}

	private CLocation getTargetLocation(JEditDockable d, String position)
	{
		DockingArea targetArea = null;
		if (position.equals(DockableWindowManager.BOTTOM))
			targetArea = getBottomDockingArea();
		else if (position.equals(DockableWindowManager.TOP))
			targetArea = getTopDockingArea();
		else if (position.equals(DockableWindowManager.RIGHT))
			targetArea = getRightDockingArea();
		else if (position.equals(DockableWindowManager.LEFT))
			targetArea = getLeftDockingArea();
		if (targetArea != null)
			return ((DfDockingArea) targetArea).getTargetLocationFor(d);
		return null;
	}

	@Override
	public void showDockableWindow(String name)
	{
		final JEditDockable d = getJEditDockable(name);
		if (d == null)
			return;
		String position = getDockablePosition(name);
		if (position.equals(DockableWindowManager.FLOATING))
			d.setExtendedMode(ExtendedMode.EXTERNALIZED);
		else
		{
			CLocation loc = getTargetLocation(d, position);
			if (loc != null)
				d.setLocation(loc);
		}
		if (! d.isVisible())
			d.setVisible(true);
		Timer t = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				d.toFront();
				focusDockable(d.getName());
			}
		});
		t.setRepeats(false);
		t.start();
	}

	private void setTheme(String name)
	{
		ThemeMap themes = control.getThemes();
		themes.select(name);
	}

	public CControl getControl()
	{
		return control;
	}

	private void addDockStationListeners()
	{
		for (JEditDockable d: created.values())
			d.registerListeners();
	}

	@Override
	public void applyDockingLayout(DockingLayout docking)
	{
		loadingLayout = false;
		if (docking != null)
		{
			DfDockingLayout layout = (DfDockingLayout) docking;
			String filename = layout.getPersistenceFilename();
			if (filename != null)
			{
				try
				{
					control.readXML(new File(filename));
					addDockStationListeners();
					return;
				}
				catch (IOException e)
				{
				}
			}
		}
		loadingLayout = true;
		super.applyDockingLayout(docking);
		loadingLayout = false;
		addDockStationListeners();
	}

	@Override
	protected void dockingPositionChanged(String dockableName,
			String oldPosition, String newPosition)
	{
		showDockableWindow(dockableName);
	}

	@Override
	public JComponent floatDockableWindow(String name)
	{
		JEditDockable d = getJEditDockable(name);
		d.setExtendedMode(ExtendedMode.EXTERNALIZED);
		d.setVisible(true);
		return d.getWindow();
	}

	private void setDockablePosition(String name, String position)
	{
		jEdit.setProperty(name + ".dock-position", position);
	}

	@Override
	public void toggleDockAreas()
	{
		if (toggleViewIndex == -1)
		{
			toggleViewIndex = 0;
			for (View v: jEdit.getViews())
			{
				if (v == view)
					break;
				toggleViewIndex++;
			}
			getDockingLayout(view.getViewConfig()).saveLayout(
				RESTORE_LAYOUT, toggleViewIndex);
			((DfDockingArea)getBottomDockingArea()).minimize();
			((DfDockingArea)getTopDockingArea()).minimize();
			((DfDockingArea)getRightDockingArea()).minimize();
			((DfDockingArea)getLeftDockingArea()).minimize();
		}
		else
		{
			DockingLayout layout = getDockingLayout(view.getViewConfig()); 
			layout.loadLayout(RESTORE_LAYOUT, toggleViewIndex);
			applyDockingLayout(layout);
			toggleViewIndex = -1;
		}
		view.getTextArea().requestFocus();
	}

	private class DfDockingArea implements DockingArea
	{
		private String position;
		private JEditDockable recent;
		public DfDockingArea(String position)
		{
			this.position = position;
		}
		public String getPosition() { return position; }
		private JEditDockable getCurrentDockable()
		{
			Vector<JEditDockable> dockables = getAreaDockables(false);
			for (JEditDockable d: dockables)
			{
				if (d.isVisible())
					return d;
			}
			return null;
		}
		public String getCurrent()
		{
			JEditDockable d = getCurrentDockable();
			if (d == null)
				return null;
			return d.getName();
		}
		public void show(String name)
		{
			if (name == null) // hide
			{
				recent = getCurrentDockable();
				Vector<JEditDockable> dockables = getAreaDockables(false);
				for (JEditDockable d: dockables)
					hideDockableWindow(d.getName());
			}
			else // show
			{
				setDockablePosition(name, position);
				showDockableWindow(name);
			}
		}
		public void showMostRecent()
		{
			if (recent != null)
				recent.setExtendedMode(ExtendedMode.NORMALIZED);
		}
		private Point getLocation(Container c)
		{
			Point p = new Point();
			SwingUtilities.convertPointToScreen(p, c);
			return p;
		}
		public boolean belongsToArea(JEditDockable d, boolean includeMinimized)
		{
			Container main = mainDockable.getContentPane();
			JComponent c = d.getWindow();
			if (c == null)
				return false;
			Point mainPos = getLocation(main);
			Point cPos = getLocation(c);
			if (DockableWindowManager.BOTTOM.equals(position))
				return (cPos.getY() >= (mainPos.getY() + main.getHeight()));
			else if (DockableWindowManager.TOP.equals(position))
				return ((cPos.getY() + c.getHeight()) <= mainPos.getY());
			else if (DockableWindowManager.LEFT.equals(position))
				return ((cPos.getX() + c.getWidth()) <= mainPos.getX());
			else if (DockableWindowManager.RIGHT.equals(position))
				return (cPos.getX() >= (mainPos.getX() + main.getWidth()));
			return false;
		}
		private Vector<JEditDockable> getAreaDockables(boolean includeMinimized)
		{
			Vector<JEditDockable> dockables = new Vector<JEditDockable>();
			for (JEditDockable d: created.values())
			{
				if (belongsToArea(d, includeMinimized))
					dockables.add(d);
			}
			return dockables;
		}
		public void minimize()
		{
			Vector<JEditDockable> dockables = getAreaDockables(false);
			for (JEditDockable dockable: dockables)
				minimizeDockableInArea(dockable, this);
		}
		public String [] getDockables()
		{
			return getDockables(true);
		}
		public String [] getDockables(boolean includeMinimized)
		{
			Vector<JEditDockable> dockables = getAreaDockables(includeMinimized);
			String [] names = new String[dockables.size()];
			for (int i = 0; i < dockables.size(); i++)
				names[i] = dockables.get(i).getName();
			return names;
		}
		public CLocation getTargetLocationFor(JEditDockable d)
		{
			// If the dockable is already in this area, do not move/resize it
			if ((! loadingLayout) && belongsToArea(d, false))
				return d.getBaseLocation();
			// Otherwise, find the largest dockable in this area and use its location
			JEditDockable preferred = null;
			int preferredSize = 0;
			Vector<JEditDockable> dockables = getAreaDockables(false);
			for (JEditDockable dockable: dockables)
			{
				JComponent window = dockable.getWindow();
				int size = window.getWidth() * window.getHeight();
				if (size > preferredSize)
				{
					preferredSize = size;
					preferred = dockable;
				}
			}
			if (preferred != null)
				return preferred.getBaseLocation();
			// Otherwise, use a default location
			CContentAreaCenterLocation center = CLocation.base().normal();
			CRectangleLocation loc = null;
			if (position.equals(DockableWindowManager.BOTTOM))
				loc = center.rectangle(0.0, 0.8, 1.0, 0.2);
			else if (position.equals(DockableWindowManager.TOP))
				loc = center.rectangle(0.0, 0.0, 1.0, 0.2);
			else if (position.equals(DockableWindowManager.RIGHT))
				loc = center.rectangle(0.8, 0.2, 0.2, 0.6);
			else if (position.equals(DockableWindowManager.LEFT))
				loc = center.rectangle(0.0, 0.2, 0.2, 0.6);
			return loc.stack();
		}
	}

	@Override
	public void closeCurrentArea()
	{
		if (focused == null)
			return;
		DfDockingArea area = getDockingAreaOf(focused); 
		if (area != null)
			area.show(null);
	}

	@Override
	public DockingLayout getDockingLayout(ViewConfig config)
	{
		DfDockingLayout layout = new DfDockingLayout(this);
		return layout;
	}

	@Override
	public void hideDockableWindow(String name)
	{
		JEditDockable d = created.get(name);
		if (d == null)
			return;
		if (! minimizeDockable(d))
			d.setExtendedMode(ExtendedMode.MINIMIZED);
		view.getTextArea().requestFocus();
	}

	@Override
	public boolean isDockableWindowDocked(String name)
	{
		JEditDockable d = created.get(name);
		if (d == null)
			return false;
		return (d.getExtendedMode() == ExtendedMode.NORMALIZED);
	}

	@Override
	public boolean isDockableWindowVisible(String name)
	{
		JComponent c = getDockable(name);
		if (c == null)
			return false;
		return c.isVisible();
	}

	@Override
	protected void propertiesChanged()
	{
		super.propertiesChanged();
		String selectedTheme = DfOptionPane.getThemeName();
		setTheme(selectedTheme);
	}

	public void disposeDockableWindow(String name)
	{
		JEditDockable d = created.get(name);
		if (d == null)
			return;
		d.setVisible(false);
		created.remove(name);
		control.remove(d);
	}

	private DfDockingArea getDockingAreaOf(JEditDockable dockable)
	{
		DfDockingArea area = (DfDockingArea) getTopDockingArea(); 
		if (area.belongsToArea(dockable, false))
			return area;
		area = (DfDockingArea) getBottomDockingArea(); 
		if (area.belongsToArea(dockable, false))
			return area;
		area = (DfDockingArea) getLeftDockingArea();
		if (area.belongsToArea(dockable, false))
			return area;
		area = (DfDockingArea) getRightDockingArea();
		if (area.belongsToArea(dockable, false))
			return area;
		return null;
	}

	private boolean minimizeDockable(JEditDockable d)
	{
		DfDockingArea area = getDockingAreaOf(d);
		if (area == null)
			return false;
		return minimizeDockableInArea(d, area);
	}

	private boolean minimizeDockableInArea(JEditDockable d, DfDockingArea area)
	{
		CBaseLocation loc = CLocation.base();
		CFlapIndexLocation mloc = null;
		if (area.getPosition().equals(DockableWindowManager.BOTTOM))
			mloc = loc.minimalSouth();
		else if (area.getPosition().equals(DockableWindowManager.TOP))
			mloc = loc.minimalNorth();
		else if (area.getPosition().equals(DockableWindowManager.LEFT))
			mloc = loc.minimalWest();
		else if (area.getPosition().equals(DockableWindowManager.RIGHT))
			mloc = loc.minimalEast();
		if (mloc == null)
			return false;
		d.setLocation(mloc.stack());
		d.setExtendedMode(ExtendedMode.MINIMIZED);
		return true;
	}

	private class MinimizeAction extends CMinimizeAction
	{
		protected MinimizeAction()
		{
			super(control);
		}
		@Override
		public void action(CDockable dockable)
		{
			if (dockable instanceof JEditDockable)
			{
				if (minimizeDockable((JEditDockable) dockable))
					return;
			}
			super.action(dockable);
		}
	}

	private JEditDockable createJEditDockable(String name, boolean fake)
	{
		JComponent window = getDockable(name);
		if (window == null)
		{
			if (fake)
			{
				// Create a fake panel only if the dockable exists
				if (super.factory.getDockableWindowFactory(name) != null)
					window = new JPanel();
			}
			else
				window = createDockable(name);
		}
		if (window == null)
			return null;
		String title = getDockableTitle(name);
		final JEditDockable d = new JEditDockable(factory, name, title,
			window, fake);
		created.put(name, d);
		MinimizeAction minimizeAction = new MinimizeAction();
		d.putAction(CDockable.ACTION_KEY_MINIMIZE, minimizeAction);
		d.addCDockableStateListener(stateListener);
		return d;
	}

	private class JEditDockHierarchyListener implements DockHierarchyListener
	{
		public void controllerChanged(DockHierarchyEvent event)
		{
			//System.err.println("controllerChanged: " + ((JEditDockable)(((CommonDockable) event.getDockable()).getDockable())).getName());
		}
		public void hierarchyChanged(DockHierarchyEvent event)
		{
			//System.err.println("hierarchyChanged: " +	((JEditDockable)(((CommonDockable) event.getDockable()).getDockable())).getName());
			DockStation station = event.getDockable().getDockParent();
			if (station == null)
				return;
			if (! listenedStations.contains(station))
			{
				listenedStations.add(station);
				station.addDockStationListener(listener);
			}
		}
	}

	private static class JEditDockStationListener implements DockStationListener
	{
		private JEditDockable getJEditDockable(Dockable d)
		{
			if (d instanceof CommonDockable)
			{
				CommonDockable commonDockable = (CommonDockable) d;
				CDockable cDockable = commonDockable.getDockable();
				if (cDockable instanceof JEditDockable)
					return (JEditDockable) cDockable;
			}
			return null;
		}
		public void dockableAdded(DockStation station, Dockable dockable)
		{
			JEditDockable d = getJEditDockable(dockable);
			if (d == null)
				return;
			//System.err.println("added: " + d.getName());
		}
		public void dockableAdding(DockStation station, Dockable dockable)
		{
			JEditDockable d = getJEditDockable(dockable);
			if (d == null)
				return;
			//System.err.println("adding: " + d.getName());
		}
		public void dockableRemoved(DockStation station, Dockable dockable)
		{
			JEditDockable d = getJEditDockable(dockable);
			if (d == null)
				return;
			//System.err.println("removed: " + d.getName());
		}
		public void dockableRemoving(DockStation station, Dockable dockable)
		{
			JEditDockable d = getJEditDockable(dockable);
			if (d == null)
				return;
			//System.err.println("removing: " + d.getName());
		}
		public void dockableSelected(DockStation station,
				Dockable oldSelection, Dockable newSelection)
		{
			JEditDockable d = getJEditDockable(newSelection);
			if (d == null)
				return;
			//System.err.println("selected: " + d.getName());
		}
		public void dockableVisibiltySet(DockStation station,
				Dockable dockable, boolean visible)
		{
			JEditDockable d = getJEditDockable(dockable);
			if (d == null)
				return;
			//System.err.println("visibilitySet: " + d.getName() + " - " + visible);
			if (visible)
				d.madeVisible();
		}
	}
	private class JEditDockableFactory implements MultipleCDockableFactory<JEditDockable, JEditDockableLayout>
	{
		public JEditDockableLayout create()
		{
			return new JEditDockableLayout();
		}
		public JEditDockable read(JEditDockableLayout layout)
		{
			return createJEditDockable(layout.getName(), true);
		}
		public JEditDockableLayout write(JEditDockable dockable)
		{
			return new JEditDockableLayout(dockable.getName());
		}
		public boolean match(JEditDockable dockable, JEditDockableLayout layout)
		{
			return layout.getName().equals(dockable.getName());
		}
	}
	private static class JEditDockableLayout implements MultipleCDockableLayout
	{
		private String name;
		public JEditDockableLayout()
		{
		}
		public JEditDockableLayout(String name)
		{
			this.name = name;
		}
		public String getName()
		{
			return name;
		}
		public void readStream(DataInputStream in) throws IOException
		{
			name = in.readUTF();
		}
		public void readXML(XElement element)
		{
			name = element.getString();
		}
		public void writeStream(DataOutputStream out) throws IOException
		{
			out.writeUTF(name);
		}
		public void writeXML(XElement element)
		{
			element.setString(name);
		}
	}
	private class JEditDockable extends DefaultMultipleCDockable
	{
		private String name;
		private JComponent window;
		private boolean fake;

		public JEditDockable(JEditDockableFactory factory, String name,
			String title, JComponent window, boolean fake)
		{
			super(factory, title, window);
			getContentPane().setMinimumSize(new Dimension(10, 10));
			this.name = name;
			this.window = window;
			this.fake = fake;
			setCloseable(true);
		}
		public String getName() { return name; }
		public JComponent getWindow() { return window; }
		public void registerListeners()
		{
			if (! fake)
				return;
			if (intern() == null)
				return;
			DockStation station = intern().getDockParent();
			if (station == null)
				return;
			if (station.isVisible(intern()))
				madeVisible();
			else
			{
				intern().addDockHierarchyListener(hierarchyListener);
				if (! listenedStations.contains(station))
				{
					listenedStations.add(station);
					station.addDockStationListener(listener);
				}
			}
		}
		public void madeVisible()
		{
			// Replace fake dockables with real ones when needed
			if (fake)
			{
				fake = false;
				window = createDockable(getName());
				getContentPane().removeAll();
				getContentPane().add(window, BorderLayout.CENTER);
				DockStation station = intern().getDockParent();
				if (station.getDockableCount() == 0)
				{
					station.removeDockStationListener(listener);
					listenedStations.remove(station);
				}
				intern().removeDockHierarchyListener(hierarchyListener);
			}
		}
	}

	private class DockableStateListener extends CDockableAdapter
	{
		public void visibilityChanged(CDockable dockable)
		{
			if ((dockable instanceof JEditDockable) && (! dockable.isVisible()))
				created.remove(((JEditDockable) dockable).getName());
		}
	}

	@Override
	public DockingArea getBottomDockingArea()
	{
		if (bottom == null)
			bottom = new DfDockingArea(DockableWindowManager.BOTTOM);
		return bottom;
	}

	@Override
	public DockingArea getLeftDockingArea()
	{
		if (left == null)
			left = new DfDockingArea(DockableWindowManager.LEFT);
		return left;
	}

	@Override
	public DockingArea getRightDockingArea()
	{
		if (right == null)
			right = new DfDockingArea(DockableWindowManager.RIGHT);
		return right;
	}

	@Override
	public DockingArea getTopDockingArea()
	{
		if (top == null)
			top = new DfDockingArea(DockableWindowManager.TOP);
		return top;
	}
}
