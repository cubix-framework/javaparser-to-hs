package myDoggy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.DockableWindowUpdate;
import org.noos.xing.mydoggy.Content;
import org.noos.xing.mydoggy.DockedTypeDescriptor;
import org.noos.xing.mydoggy.FloatingTypeDescriptor;
import org.noos.xing.mydoggy.PersistenceDelegateCallback;
import org.noos.xing.mydoggy.PushAwayMode;
import org.noos.xing.mydoggy.ToolWindow;
import org.noos.xing.mydoggy.ToolWindowAction;
import org.noos.xing.mydoggy.ToolWindowAnchor;
import org.noos.xing.mydoggy.ToolWindowBar;
import org.noos.xing.mydoggy.ToolWindowManager;
import org.noos.xing.mydoggy.ToolWindowType;
import org.noos.xing.mydoggy.PersistenceDelegate.MergePolicy;
import org.noos.xing.mydoggy.plaf.MyDoggyToolWindowManager;
import org.noos.xing.mydoggy.plaf.ui.CustomDockableDescriptor;
import org.noos.xing.mydoggy.plaf.ui.DockableDescriptor;
import org.noos.xing.mydoggy.plaf.ui.MyDoggyKeySpace;

@SuppressWarnings("serial")
public class MyDoggyWindowManager extends DockableWindowManager {

	private MyDoggyToolWindowManager wm = null;
	private boolean addAnchorButtons = true;
	
	private class ToggleBarDockableDescriptor extends CustomDockableDescriptor
	{
		public ToggleBarDockableDescriptor(MyDoggyToolWindowManager manager,
				ToolWindowAnchor anchor)
		{
			super(manager, anchor);
			setAvailable(true);
			setAnchor(anchor, 0);
			setAnchorPositionLocked(true);
		}
		@Override
		public boolean isAvailableCountable() {
			return false;
		}
		public JComponent getRepresentativeAnchor(Component parent) {
            if (representativeAnchor == null) {
                representativeAnchor = new ToggleBarButton(manager.getToolWindowBar(anchor));
            }
            return representativeAnchor;
		}

		public void updateRepresentativeAnchor() {
		}
		
		private class ToggleBarButton extends JLabel
		{
			ToolWindowBar twb;
			public ToggleBarButton(ToolWindowBar bar)
			{
				twb = bar;
				if (twb == null)
					return;
				setIcon();
            	addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (twb != null)
							twb.setVisible(! twb.isVisible());
					}
            	});
            	twb.addPropertyChangeListener("visible", new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent evt) {
						setIcon();
					}
            	});
			}
			void setIcon()
			{
				Icon icon;
				if (twb.isVisible())
					icon = UIManager.getIcon(MyDoggyKeySpace.CONTENT_PAGE_CLOSE);
				else
					icon = UIManager.getIcon(MyDoggyKeySpace.CONTENT_PAGE_MAXIMIZE);
				setIcon(icon);
			}
			
			public DockableDescriptor getDockableDescriptor() {
				return ToggleBarDockableDescriptor.this;
			}
		}
	}
	
	public MyDoggyWindowManager(View view, DockableWindowFactory instance,
			ViewConfig config)
	{
		super(view, instance, config);
		UIManager.put(MyDoggyKeySpace.DEBUG, false);
		setLayout(new BorderLayout());
		wm = new MyDoggyToolWindowManager();
		wm.getTypeDescriptorTemplate(ToolWindowType.DOCKED).setAnimating(
			OptionPane.getEnableAnimationsProp());
		add(wm, BorderLayout.CENTER);
	}

	@Override
	protected void dockableLoaded(String name, String position)
	{
		if (position == null || position.equals(DockableWindowManager.FLOATING))
			return;
		ToolWindow tw = createFakeToolWindow(name);
		tw.setAvailable(true);
	}
	
	@Override
	protected void dockingPositionChanged(String name,
		String oldPosition, String newPosition)
	{
		showDockableWindow(name);
		ToolWindow tw = getToolWindow(name);
		if (tw != null)
		{
			ToolWindowAnchor anchor = position2anchor(getDockablePosition(name));
			if (anchor != tw.getAnchor())
				tw.setAnchor(anchor);
		}
	}

	@Override
	public void closeCurrentArea()
	{
		Object activeWindowId = wm.getActiveToolWindowId();
		if (activeWindowId == null)
			return;
		ToolWindow tw = wm.getToolWindow(activeWindowId);
		if (tw == null)
			return;
		ToolWindowAnchor anchor = tw.getAnchor();
		if (anchor != null)
			wm.getToolWindowBar(anchor).setVisible(false);
	}

	@Override
	public JComponent floatDockableWindow(String name)
	{
		ToolWindow tw = wm.getToolWindow(name);
		if (tw == null)
			tw = createToolWindow(name);
		if (tw == null)
			return null;
		tw.setType(ToolWindowType.FLOATING);
		tw.setActive(true);
		return (JComponent) tw.getComponent();
	}

	@Override
	public DockingLayout getDockingLayout(ViewConfig config)
	{
		MyDoggyDockingLayout layout = new MyDoggyDockingLayout(wm);
		return layout;
	}

	private String getToolWindowID(String dockableName)
	{
		return dockableName;
	}
	
	private ToolWindow getToolWindow(String dockableName)
	{
		return wm.getToolWindow(getToolWindowID(dockableName));
	}
	
	@Override
	public void hideDockableWindow(String name)
	{
		ToolWindow tw = getToolWindow(name);
		if (tw == null)
			return;
		tw.setVisible(false);
		EditBus.send(new DockableWindowUpdate(this, DockableWindowUpdate.DEACTIVATED,
			name));
		if (tw.getType() == ToolWindowType.DOCKED ||
			tw.getType() == ToolWindowType.SLIDING)
		{
			// See if another window has become active
			tw = getCurrentToolWindow(tw.getAnchor());
			if (tw != null)
				EditBus.send(new DockableWindowUpdate(this,
					DockableWindowUpdate.ACTIVATED, tw.getId()));
		}
	}

	@Override
	public boolean isDockableWindowDocked(String name)
	{
		ToolWindow tw = getToolWindow(name);
		if (tw == null)
			return false;
		return (tw.getType() == ToolWindowType.DOCKED);
	}

	@Override
	public boolean isDockableWindowVisible(String name)
	{
		ToolWindow tw = getToolWindow(name);
		if (tw == null)
			return false;
		return (tw.isVisible());
	}

	private void loadMyDoggyLayout(String filename)
	{
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(filename);
			PersistenceDelegateCallback callback = new PersistenceCallback();
			wm.getPersistenceDelegate().merge(inputStream, MergePolicy.RESET, callback);
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
			MyDoggyDockingLayout layout = (MyDoggyDockingLayout) docking;
			String filename = layout.getLayoutFilename();
			if (filename != null) {
				java.io.File f = new File(filename);
				if (f.exists()) {
					loadMyDoggyLayout(filename);
					loaded = true;
				}
			}
		}
		if (! loaded) // No saved layout - just use the docking positions specified by jEdit properties
			super.applyDockingLayout(null);
		if (addAnchorButtons) {
			new ToggleBarDockableDescriptor(wm, ToolWindowAnchor.TOP);
			new ToggleBarDockableDescriptor(wm, ToolWindowAnchor.BOTTOM);
			new ToggleBarDockableDescriptor(wm, ToolWindowAnchor.LEFT);
			new ToggleBarDockableDescriptor(wm, ToolWindowAnchor.RIGHT);
			addAnchorButtons = false;
		}
	}

	public class PersistenceCallback implements PersistenceDelegateCallback
	{
		public Content contentNotFound(ToolWindowManager toolWindowManager,
				String contentId, PersistenceNode node) {
			return null;
		}

		public ToolWindow toolwindowNotFound(
				ToolWindowManager toolWindowManager, String toolWindowId,
				PersistenceNode node) {
			if (! node.getBoolean("visible", true))
				return createFakeToolWindow(toolWindowId);
			return createToolWindow(toolWindowId);
		}

		public String validate(PersistenceNode node, String attribute,
				String attributeValue, Object attributeDefaultValue) {
			return attributeValue;
		}
	}
	
	private ToolWindow createFakeToolWindow(String name)
	{
		JComponent window = new JPanel(new BorderLayout());
		if (window == null)
			return null;
		String title = getDockableTitle(name);
		String position = getDockablePosition(name); 
		String id = getToolWindowID(name);
		ToolWindowAnchor anchor = position2anchor(position);
		ToolWindow tw = wm.registerToolWindow(id, title, null, window, anchor);
		initToolWindowsDescriptors(name, tw);
		PropertyChangeListener listener = new VisibilityChangeListener(tw, name,
			window, true);
		tw.addPropertyChangeListener("visible", listener);
		tw.getRepresentativeAnchorDescriptor().setPreviewEnabled(OptionPane.getEnablePreviewProp());
		return tw;
	}
	
	private class VisibilityChangeListener implements PropertyChangeListener
	{
		ToolWindow tw;
		String name;
		JComponent window;
		boolean fake;
		public VisibilityChangeListener(ToolWindow tw, String name,
			JComponent window, boolean fake)
		{
			this.tw = tw;
			this.name = name;
			this.window = window;
			this.fake = fake;
		}
		public void propertyChange(PropertyChangeEvent evt)
		{
			if (fake)
			{
				if (! tw.isVisible())
					return;
				JComponent comp = createDockable(name);
				window.add(comp, BorderLayout.CENTER);
				fake = false;
			}
			Object reason = tw.isVisible() ? DockableWindowUpdate.ACTIVATED :
				DockableWindowUpdate.DEACTIVATED;
			EditBus.send(new DockableWindowUpdate(MyDoggyWindowManager.this,
				reason, name));
		}
	}

	private ToolWindow createToolWindow(String name)
	{
		JComponent window = getDockable(name);
		String position = getDockablePosition(name); 
		if (window == null)
			window = createDockable(name);
		if (window == null)
			return null;
		String id = getToolWindowID(name);
		String title = getDockableTitle(name);
		ToolWindowAnchor anchor = position2anchor(position);
		ToolWindow tw = wm.registerToolWindow(id, title, null, window, anchor);
		initToolWindowsDescriptors(name, tw);
		PropertyChangeListener listener = new VisibilityChangeListener(tw,
			name, window, false);
		tw.addPropertyChangeListener("visible", listener);
		return tw;
	}

	private void initToolWindowsDescriptors(String name, ToolWindow tw) {
		tw.getRepresentativeAnchorDescriptor().setTitle(shortTitle(name));
		tw.getTypeDescriptor(ToolWindowType.DOCKED).setIdVisibleOnTitleBar(false);
		DockedTypeDescriptor dockedDescriptor = tw.getTypeDescriptor(DockedTypeDescriptor.class);
		dockedDescriptor.addToolWindowAction(new FloatingFreeAction());
		dockedDescriptor.addToolWindowAction(new RemoveDockableAction(), 0);
		setFloatingProperties(tw);
		tw.getRepresentativeAnchorDescriptor().setPreviewEnabled(OptionPane.getEnablePreviewProp());
	}

	private void setFloatingProperties(ToolWindow tw) {
		setFloatingDescriptorProperties((FloatingTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.FLOATING));
		setFloatingDescriptorProperties((FloatingTypeDescriptor) tw.getTypeDescriptor(ToolWindowType.FLOATING_FREE));
	}

	private void setFloatingDescriptorProperties(FloatingTypeDescriptor floatDescriptor) {
		floatDescriptor.setAlwaysOnTop(OptionPane.getFloatOnTopProp());
		floatDescriptor.setOsDecorated(OptionPane.getFloatOsDecorationsProp());
		floatDescriptor.setAddToTaskBar(OptionPane.getFloatAddToTaskBarProp());
		floatDescriptor.addToolWindowAction(new RemoveDockableAction(), 0);
	}

	private static abstract class DockableAction extends ToolWindowAction {
		public DockableAction(String name, Icon icon, String text, String tooltip) {
			super(name, icon);
			setText(text);
			setTooltipText(tooltip);
			setVisibleOnMenuBar(true);
			setVisibleOnTitleBar(true);
			setVisible(true);
		}
	}
	private static class RemoveDockableAction extends DockableAction {
		public RemoveDockableAction() {
			super("RemoveDockableAction",
				UIManager.getIcon(MyDoggyKeySpace.HIDE_TOOL_WINDOW),
				"Remove", "Remove dockable completely");
		}

		public void actionPerformed(ActionEvent e) {
			ToolWindowManager wm = getToolWindow().getDockableManager();
			String id = getToolWindow().getId();
			wm.unregisterToolWindow(id);
		}
	}
	
	private static class FloatingFreeAction extends DockableAction {
		public FloatingFreeAction() {
			super("FloatingFreeAction",
				UIManager.getIcon(MyDoggyKeySpace.FLOATING_INACTIVE),
				"Floating free", "Floating free (without an anchor button)");
		}
		
		@Override
		public void setToolWindow(final ToolWindow toolWindow) {
			super.setToolWindow(toolWindow);
			toolWindow.addPropertyChangeListener("type", new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					if (evt.getOldValue() == ToolWindowType.FLOATING) {
						toolWindow.getRepresentativeAnchorDescriptor().setVisible(true);
					}
				}
			});
		}

		public void actionPerformed(ActionEvent e) {
			toolWindow.getRepresentativeAnchorDescriptor().setVisible(false);
			toolWindow.setType(ToolWindowType.FLOATING);
		}
	}
	
	@Override
	public void showDockableWindow(String name)
	{
		ToolWindow tw = getToolWindow(name);
		if (tw != null)
		{
			activateToolWindow(tw);
			return;
		}
		tw = createToolWindow(name);
		if (DockableWindowManager.FLOATING.equals(getDockablePosition(name)))
			tw.setType(ToolWindowType.FLOATING_FREE);
		else
			tw.setType(ToolWindowType.DOCKED);
		activateToolWindow(tw);
	}

	private void activateToolWindow(ToolWindow tw)
	{
		ToolWindowAnchor anchor = tw.getAnchor();
		if (anchor != null)
		{
			ToolWindowBar bar = wm.getToolWindowBar(anchor);
			if (! bar.isVisible())
				bar.setVisible(true);
		}

		// If another window is currently active, it is becoming deactivated
		ToolWindow current = getCurrentToolWindow(anchor);
		if (current != null && current != tw)
			EditBus.send(new DockableWindowUpdate(this, DockableWindowUpdate.DEACTIVATED,
				current.getId()));
		
		tw.setActive(true);
		final ToolWindow twToFocus = tw;
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				focusDockable(twToFocus.getId());
				EditBus.send(new DockableWindowUpdate(MyDoggyWindowManager.this,
					DockableWindowUpdate.ACTIVATED, twToFocus.getId()));
			}
		});
	}

	private ToolWindowAnchor position2anchor(String position)
	{
		if (position.equals(DockableWindowManager.LEFT))
			return ToolWindowAnchor.LEFT;
		if (position.equals(DockableWindowManager.TOP))
			return ToolWindowAnchor.TOP;
		if (position.equals(DockableWindowManager.RIGHT))
			return ToolWindowAnchor.RIGHT;
		return ToolWindowAnchor.BOTTOM;
	}

	@Override
	public void setMainPanel(JPanel panel)
	{
		wm.getContentManager().addContent("main", "main", null, panel);
	}

	public class MyDoggyDockingArea implements DockingArea {
		ToolWindowAnchor anchor;
		public MyDoggyDockingArea(ToolWindowAnchor anchor) {
			this.anchor = anchor;
		}
		public void showMostRecent() {
			wm.getToolWindowBar(anchor).setVisible(true);
		}
		private ToolWindow getCurrentToolWindow() {
			return MyDoggyWindowManager.this.getCurrentToolWindow(anchor);
		}
		public String getCurrent() {
			ToolWindow current = getCurrentToolWindow();
			if (current == null)
				return null;
			return current.getId();
		}
		public void show(String name) {
			ToolWindow tw;
			if (name == null)
			{	// Hide the visible windows in this area
				wm.getToolWindowBar(anchor).setVisible(false);
			}
			else
			{	// Show the window
				showDockableWindow(name);
				tw = getToolWindow(name);
				if (tw != null)
					tw.setAnchor(anchor);
			}
		}
		public String[] getDockables() {
			ToolWindow[] tools = wm.getToolsByAnchor(anchor);
			String [] docked = new String[tools.length];
			for (int i = 0; i < tools.length; i++)
				docked[i] = tools[i].getId();
			return docked;
		}
	}
	
	public ToolWindow getCurrentToolWindow(ToolWindowAnchor anchor)
	{
		ToolWindow[] tools = wm.getToolsByAnchor(anchor);
		for (ToolWindow tw: tools)
			if (tw.isActive())
				return tw;
		for (ToolWindow tw: tools)
			if (tw.isVisible())
				return tw;
		return null;
	}
	
	public DockingArea getBottomDockingArea() {
		return new MyDoggyDockingArea(ToolWindowAnchor.BOTTOM);
	}

	public DockingArea getLeftDockingArea() {
		return new MyDoggyDockingArea(ToolWindowAnchor.LEFT);
	}

	public DockingArea getRightDockingArea() {
		return new MyDoggyDockingArea(ToolWindowAnchor.RIGHT);
	}

	public DockingArea getTopDockingArea() {
		return new MyDoggyDockingArea(ToolWindowAnchor.TOP);
	}

	@Override
	protected void propertiesChanged() {
		super.propertiesChanged();
		setPushAwayMode();
		wm.getTypeDescriptorTemplate(ToolWindowType.DOCKED).setAnimating(
			OptionPane.getEnableAnimationsProp());
		// Update floating properties
		ToolWindow[] windows = wm.getToolWindows();
		for (ToolWindow w: windows) {
			setFloatingProperties(w);
			w.getRepresentativeAnchorDescriptor().setPreviewEnabled(OptionPane.getEnablePreviewProp());
		}
	}
	
	private void setPushAwayMode() {
		if (! OptionPane.getUseAlternateLayoutProp())
			wm.getToolWindowManagerDescriptor().setPushAwayMode(OptionPane.getPushAwayModeProp());
		else
			wm.getToolWindowManagerDescriptor().setPushAwayMode(getAlternateLayoutProp() ?
					PushAwayMode.VERTICAL : PushAwayMode.HORIZONTAL);
	}

	@Override
	public void dockableTitleChanged(String dockable, String newTitle) {
		ToolWindow tw = getToolWindow(dockable);
		if (tw != null)
			tw.setTitle(newTitle);
	}

	@Override
	protected void applyAlternateLayout(boolean alternateLayout) {
		setPushAwayMode();
	}

	public void disposeDockableWindow(String name) {
		ToolWindow tw = getToolWindow(name);
		if (tw != null)
			wm.unregisterToolWindow(name);
	}

}
