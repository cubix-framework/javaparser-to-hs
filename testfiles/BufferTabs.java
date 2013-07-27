/*
 * BufferTabs.java - Part of the BufferTabs plugin for jEdit.
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 * Copyright (C) 1999, 2000 Jason Ginchereau
 * Copyright (C) 2000, 2001, 2002, 2003 Andre Kaplan
 * Copyright (C) 2001 Joe Laffey
 * Copyright (C) 2003 Kris Kopicki
 * Copyright (C) 2003 Chris Samuels
 * Copyright (C) 2008 Matthieu Casanova
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


package buffertabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.bufferset.BufferSet;
import org.gjt.sp.jedit.bufferset.BufferSetListener;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.ThreadUtilities;

/**
 * A tabbed pane that contains a text area.  The text area's buffer is
 * changed when a tab is selected, and when the buffer is changed in
 * another way, the tabs get updated.
 *
 * The tabs provide a popup menu that may be accessed by right-clicking
 * on a tab. This popup menu provides a number of functions relating to
 * the buffer.
 *
 * @author Jason Ginchereau
 * @author Andre Kaplan
 * @author Joe Laffey
 * @author Chris Samuels
 * @author Matthieu Casanova
 * @author Shlomy Reinstein
 */
@SuppressWarnings("serial")
public class BufferTabs extends JTabbedPane implements BufferSetListener
{
	private static final String SORT_BUFFERS = "sortBuffers";
	private final EditPane editPane;
	private final JComponent textArea;

	private final ChangeHandler changeHandler;
	private final MouseHandler mouseHandler;
	private final MouseMotionHandler mouseMotionHandler;

	private final Set<Buffer> knownBuffers;
	private TabbedPaneUI ui;
	private TabbedPaneUI bshUI;

	/**
	 * Creates a new set of buffer tabs that is attached to an EditPane,
	 * and contains the EditPane's text area.
	 * @param editPane the editpane where the bufferTabs will be attached
	 */
	public BufferTabs(EditPane editPane)
	{
		this.editPane = editPane;
		textArea = editPane.getTextArea();

		changeHandler = new ChangeHandler();
		changeHandler.setEnabled(true);
		mouseHandler = new MouseHandler();
		mouseMotionHandler = new MouseMotionHandler();
		knownBuffers = Collections.synchronizedSet(new HashSet<Buffer>());
	}

	/**
	 * Initializes tabs and starts listening for events.
	 */
	public synchronized void start()
	{
		propertiesChanged(); //CES
		BufferSet bufferSet = editPane.getBufferSet();
		bufferSet.getAllBuffers(this);
		bufferSet.addBufferSetListener(this);

		EditBus.addToBus(this);

		int index = bufferSet.indexOf(editPane.getBuffer());

		updateColorAt(getSelectedIndex());

		if (index >= 0)
		{
			setSelectedIndex(index);
			updateHighlightAt(index);
		}

		addChangeListener(changeHandler);

		// Mouse Listener for popup menu support
		addMouseListener(mouseHandler);

		// Mouse Motion listener for moving cursoMouseMotionHandlerr
		addMouseMotionListener(mouseMotionHandler);
	}


	/**
	 * Stops listening for events.
	 */
	public synchronized void stop()
	{
		EditBus.removeFromBus(this);
		removeChangeListener(changeHandler);

		removeMouseListener(mouseHandler);

		removeMouseMotionListener(mouseMotionHandler);
	}

	private static boolean areBuffersSorted()
	{
		return jEdit.getBooleanProperty(SORT_BUFFERS);
	}

	private static void stopBufferSorting()
	{
		jEdit.setBooleanProperty(SORT_BUFFERS, false);
		jEdit.propertiesChanged();
	}

	/**
	 * Gets the EditPane this tab set is attached to.
	 */
	public EditPane getEditPane()
	{
		return editPane;
	}

	/*
	 ** BufferUpdate message handling.
	 */
	@EBHandler
	public void handleBufferUpdate(BufferUpdate bu)
	{
		Buffer buffer = bu.getBuffer();
		BufferSet bufferSet = editPane.getBufferSet();
		if (bu.getWhat() == BufferUpdate.DIRTY_CHANGED ||
			bu.getWhat() == BufferUpdate.CREATED)
		{
			int index = bufferSet.indexOf(buffer);
			if (index >= 0  && index < getTabCount())
			{
				updateTitleAt(index);
			}
		}
		else if (bu.getWhat() == BufferUpdate.LOADED)
		{
			int index = bufferSet.indexOf(buffer);
			if (index >= 0  && index < getTabCount())
			{
				updateTitleAt(index);
				updateHighlightAt(index);
			}
		}
		else if (bu.getWhat() == BufferUpdate.SAVED)
		{
			Buffer buff = bu.getBuffer();
			int index = bufferSet.indexOf(buff);
			if (index >= 0  && index < getTabCount())
			{
				setToolTipTextAt(index, buff.getPath());
			}
		}
	}

	/**
	 * EditPaneUpdate message handling.
	 */
	@EBHandler
	public void handleEditPaneUpdate(EditPaneUpdate epu)
	{
		EditPane editPane = epu.getEditPane();
		if (editPane == this.editPane)
		{
			if (epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED)
			{
				try
				{
					Buffer buffer = editPane.getBuffer();
					int index = editPane.getBufferSet().indexOf(buffer);
					if (!knownBuffers.contains(buffer))
					{
						// we don't know this buffer yet, let's add it now by simulation
						// of a bufferAdded event
						if (index != -1)
						{
							bufferAdded(buffer, index);
						}
					}
					changeHandler.setEnabled(false);
					updateColorAt(getSelectedIndex());
					setSelectedIndex(index);
					updateHighlightAt(index);
				}
				finally
				{
					changeHandler.setEnabled(true);
				}
			}
		}
	}

	public void bufferRemoved(Buffer buffer, int index)
	{
		try
		{
			changeHandler.setEnabled(false);
			knownBuffers.remove(buffer);
			removeTabAt(index);


			if (getTabCount() > 0 && super.indexOfComponent(textArea) == -1)
			{
				setComponentAt(0, textArea);
				textArea.setVisible(true);
			}

		}
		finally
		{
			changeHandler.setEnabled(true);
		}
	}

	public void bufferMoved(Buffer buffer, int oldIndex, int newIndex)
	{
		try
		{
			changeHandler.setEnabled(false);
			bufferRemoved(buffer, oldIndex);
			bufferAdded(buffer, newIndex);
		}
		finally
		{
			changeHandler.setEnabled(true);
		}
	}

	public void bufferAdded(Buffer buffer, int index)
	{
		if (knownBuffers.contains(buffer))
		{
			// the buffer is already known, it was maybe added by a setBuffer() in the EditPane
			return;
		}
		try
		{
			changeHandler.setEnabled(false);
			knownBuffers.add(buffer);

			Component component = null;
			if (super.indexOfComponent(textArea) == -1)
			{
				component = textArea;
			}

			insertTab(buffer.getName(), null, component, buffer.getPath(), index);
			updateTitleAt(index);
			if (component == textArea)
			{
				textArea.setVisible(true);
			}
		}
		finally
		{
			changeHandler.setEnabled(true);
		}

		if (editPane.getBuffer() == buffer)
		{
			setSelectedIndex(index);
			updateHighlightAt(index);
		}
		else
			updateColorAt(index);
	}

	private void setTabComponent(final int index, final boolean set)
	{
		try
		{
			final Method m = getClass().getMethod("setTabComponentAt",
					new Class[] {int.class, Component.class});
			if (m != null)
			{
				Runnable runnable = new Runnable()
				{
					public void run()
					{
						BufferTabComponent tab;
						if (set)
							tab = new BufferTabComponent(BufferTabs.this);
						else
							tab = null;
						try
						{
							m.invoke(BufferTabs.this, index, tab);
						}
						catch (IllegalAccessException e)
						{
							Log.log(Log.ERROR, this, e);
						}
						catch (InvocationTargetException e)
						{
							Log.log(Log.ERROR, this, e);
						}
					}
				};
				ThreadUtilities.runInDispatchThread(runnable);
			}
		}
		catch (NoSuchMethodException e)
		{
			Log.log(Log.ERROR, this, e);
		}
	}

	public void bufferSetSorted()
	{
		BufferSet bufferSet = editPane.getBufferSet();
		removeAll();
		knownBuffers.clear();
		bufferSet.getAllBuffers(this);
	}

	public void propertiesChanged()
	{
		int layoutPolicy = BufferTabsOptionPane.getWrapTabsProperty() ?
			JTabbedPane.WRAP_TAB_LAYOUT : JTabbedPane.SCROLL_TAB_LAYOUT;
		setTabLayoutPolicy(layoutPolicy);
		TabbedPaneUI currentUI = getUI();
		boolean myUI = currentUI.getClass().getCanonicalName().endsWith("MyUI");
		if (jEdit.getBooleanProperty("buffertabs.nostretch", false))
		{
			if (currentUI instanceof BasicTabbedPaneUI && !myUI)
			{
				int mod = currentUI.getClass().getModifiers();
				boolean extensible = !Modifier.isFinal(mod) &&
					!Modifier.isAbstract(mod) && Modifier.isPublic(mod);
				if (extensible && bshUI == null)
				{
					ui = currentUI;
					String name = getUI().getClass().getCanonicalName();
					String bsh = "class MyUI extends " + name + "{\n" +
						"	protected boolean shouldPadTabRun(int tabPlacement, int run) {\n" +
						"		return false;\n" +
						"	}\n" +
						"}\n" +
						"return new MyUI();";
					bshUI = (TabbedPaneUI) BeanShell.eval(
						null, BeanShell.getNameSpace(), bsh);
				}
				try
				{
					if (bshUI != null)
						setUI(bshUI);
				}
				catch (Exception e)
				{
					setUI(ui);
				}
			}
		}
		else
		{
			if (myUI)
				try
				{
					setUI(ui);
				}
				catch (Exception e)
				{
					setUI(bshUI);
				}
		}
		if (ColorTabs.instance().isEnabled() != jEdit.getBooleanProperty("buffertabs.color-tabs"))
		{
			ColorTabs.instance().setEnabled(!ColorTabs.instance().isEnabled());

			//Turn off all color features
			if (!ColorTabs.instance().isEnabled())
			{
				for (int i = getTabCount() - 1; i >= 0; i--)
				{
					setBackgroundAt(i, null);
					setForegroundAt(i, null);
				}

				getUI().uninstallUI(this);
				UIManager.getDefaults().put("TabbedPane.selected", null);
				getUI().installUI(this);
			}
		}

		if (ColorTabs.instance().isEnabled())
		{
			ColorTabs.instance().setMuteColors(jEdit.getBooleanProperty("buffertabs.color-mute"));
			ColorTabs.instance().setColorVariation(jEdit.getBooleanProperty("buffertabs.color-variation"));
			ColorTabs.instance().setForegroundColorized(jEdit.getBooleanProperty("buffertabs.color-foreground"));

			if (ColorTabs.instance().isSelectedColorized() != jEdit.getBooleanProperty("buffertabs.color-selected"))
			{
				ColorTabs.instance().setSelectedColorized(!ColorTabs.instance().isSelectedColorized());

				//Turn off all colorhighlight
				if (!ColorTabs.instance().isSelectedColorized())
				{
					try
					{
						getUI().uninstallUI(this);
						UIManager.getDefaults().put("TabbedPane.selected", null);
						getUI().installUI(this);
					}
					catch (Exception e)
					{
						Log.log(Log.ERROR, BufferTabs.class, "propertiesChanged: 3 " + e.toString());
					}
				}


			}
			if (ColorTabs.instance().isSelectedColorized())
			{
				ColorTabs.instance().setSelectedForegroundColorized(jEdit.getBooleanProperty("buffertabs.color-selected-foreground"));
			}

			ColorTabs.instance().propertiesChanged();

			int selectedIndex = getSelectedIndex();
			for (int i = getTabCount() - 1; i >= 0; i--)
			{
				if (selectedIndex == i)
				{
					updateHighlightAt(i);
				}
				else
					updateColorAt(i);
			}
		}
	}

	private class ChangeHandler implements ChangeListener
	{
		private boolean enabled = true;


		public boolean isEnabled()
		{
			return enabled;
		}


		public void setEnabled(boolean enabled)
		{
			this.enabled = enabled;
		}


		/**
		 * Sets the EditPane's buffer when a tab is selected.
		 */
		public synchronized void stateChanged(ChangeEvent e)
		{
			int index = getSelectedIndex();
			BufferSet bufferSet = editPane.getBufferSet();
			if (index >= 0 && isEnabled() && bufferSet.size() > index)
			{
				Buffer buffer = bufferSet.getBuffer(index);
				if (buffer != null)
				{
					int selectedIndex = bufferSet.indexOf(editPane.getBuffer());
					if (selectedIndex >= 0)
					{
						updateColorAt(selectedIndex);
					}
					editPane.setBuffer(buffer);
					updateHighlightAt(index); //CES
				}
			}
		}
	}

	/**
	 * Updates the color of the given tab
	 */
	private void updateColorAt(int index)
	{
		if (!ColorTabs.instance().isEnabled())
		{
			return;
		}
		if (index < 0)
		{
			return;
		}

		Buffer buffer = editPane.getBufferSet().getBuffer(index);
		String name = buffer.getName();

		/* Setting the background/foreground below may throw an
		 * ArrayIndexOutOfBounds exception due to bugs in BasicTabbedPaneUI,
		 * so catch it here to avoid interfering with the upper-level code.
		 */
		try
		{
			Color color = ColorTabs.instance().getDefaultColorFor(name);
			if (!ColorTabs.instance().isForegroundColorized())
			{
				setBackgroundAt(index, color);
				setForegroundAt(index, null);
			}
			else
			{
				setForegroundAt(index, color);
				setBackgroundAt(index, null);
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			// Log the exception, but ignore it, it's a JRE bug.
			Log.log(Log.WARNING, this, "The following is a JRE bug:", e);
		}
	}


	/**
	 * Force the Look and Feel to use the given color as its 'selected' color.
	 * TODO: This may cause side-effects with other tab panes.
	 */
	private void updateHighlightAt(int index)
	{
		if (index < 0)
		{
			return;
		}
		if (ColorTabs.instance().isEnabled()
		    && ColorTabs.instance().isSelectedColorized())
		{
			if (index == getSelectedIndex())
			{
				Buffer buffer = editPane.getBufferSet().getBuffer(index);
				String name = buffer.getName();
				Color color = ColorTabs.instance().getDefaultColorFor(name);
				Color selected;

				if (!ColorTabs.instance().isSelectedForegroundColorized())
				{
					selected = ColorTabs.instance().alterColorHighlight(color);
					setBackgroundAt(index, selected);
					setForegroundAt(index, null);
				}
				else
				{
					selected = ColorTabs.instance().alterColorDarken(color);
					setForegroundAt(index, selected);
					setBackgroundAt(index, null);
				}
			}
		}
	}


	private void updateTitleAt(int index)
	{
		if (index < 0 || index >= getTabCount())
		{
			return;
		}
		setTabComponent(index,
			jEdit.getBooleanProperty("buffertabs.closeButton", true));
		Buffer buffer = editPane.getBufferSet().getBuffer(index);
		String title = buffer.getName();
		Icon icon = null;
		if (jEdit.getBooleanProperty("buffertabs.icons", true))
		{
			icon = buffer.getIcon();
		}
		else
		{
			if (buffer.isDirty())
			{
				title += "*";
			}
			if (buffer.isNewFile())
			{
				title += " (new)";
			}
		}
		setTitleAt(index, title);
		setIconAt(index, icon);
	}


	public synchronized void updateTitles()
	{
		propertiesChanged(); //CES
		for (int index = getTabCount() - 1; index >= 0; index--)
		{
			updateTitleAt(index);
		}
	}

	public void setTabPlacement(String location)
	{
		location = location.toLowerCase();
		int placement = BOTTOM;
		if ("top".equals(location))
		{
			placement = TOP;
		}
		else if ("left".equals(location))
		{
			placement = LEFT;
		}
		else if ("right".equals(location))
		{
			placement = RIGHT;
		}
		setTabPlacement(placement);
	}


	/**
	 * Overridden so the JEditTextArea is at every index.
	 */
	@Override
	public Component getComponentAt(int index)
	{
		if (changeHandler.isEnabled() && index >= 0 && index < getTabCount())
		{
			return textArea;
		}
		else
		{
			return super.getComponentAt(index);
		}
	}


	/**
	 * Overridden so the JEditTextArea is at every index.
	 */
	@Override
	public int indexOfComponent(Component component)
	{
		if (component == null)
			return super.indexOfComponent(textArea);
		return super.indexOfComponent(component);
	}


	@Override
	protected String paramString()
	{
		int index = getSelectedIndex();
		if (index >= 0)
		{
			return getTitleAt(index);
		}
		else
		{
			return "";
		}
	}

	@Override
	public boolean isFocusable()
	{
		return false;
	}

	@Override
	public boolean isRequestFocusEnabled()
	{
		return false;
	}

	public int getTabAt(int x, int y)
	{

		for (int index = 0; index < getTabCount(); index++)
		{
			Rectangle rect = getBoundsAt(index);
			if (rect.contains(x, y))
			{
				return index;
			}
		}
		return -1;
	}


	private static int moving = -1;

	class ReorderBuffersDisabledDialog extends JDialog
	{
		private static final String GEOMETRY =
			"buffertabs.reorderBuffersDisabledDialog.geometry";
		private final JRadioButton disableSorting;
		private final JRadioButton keepSorting;

		ReorderBuffersDisabledDialog(Frame frame)
		{
			super(frame, jEdit.getProperty(
				"buffertabs.reorderBuffersDisabled.label"), true);
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosing(WindowEvent evt)
				{
					saveGeometry();
				}
			});
			setLayout(new GridBagLayout());

			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.WEST;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = new Insets(5, 5, 5, 5);
			JLabel message = new JLabel(jEdit.getProperty(
				"buffertabs.bufferTabPositioningDisabled.label"));
			c.gridx = 0;
			c.gridy = 0;
			c.gridheight = 2;
			add(message, c);
			disableSorting = new JRadioButton(
				jEdit.getProperty("buffertabs.disableBufferSorting.label"),
				true);
			c.gridy += c.gridheight;
			c.gridheight = 1;
			add(disableSorting, c);
			keepSorting = new JRadioButton(
					jEdit.getProperty("buffertabs.keepBufferSorting.label"));
			c.gridy++;
			add(keepSorting, c);
			ButtonGroup options = new ButtonGroup();
			options.add(disableSorting);
			options.add(keepSorting);

			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					saveGeometry();
					save();
					setVisible(false);
				}
			});
			c.gridy++;
			c.fill = GridBagConstraints.NONE;
			add(close, c);
			pack();
			GUIUtilities.loadGeometry(this, GEOMETRY);
		}

		private void save()
		{
			if (disableSorting.isSelected())
				stopBufferSorting();
		}

		private void saveGeometry()
		{
			GUIUtilities.saveGeometry(this, GEOMETRY);
		}
	}

	/**
	 * An inner class used to handle a popup menu when right-clicking on the
	 * tab pane. The actions currently apply to the frontmost buffer, which is
	 * automatically the tab that is clicked because the buffer comes to the
	 * front before the popup menu is displayed.
	 */
	class MouseHandler extends MouseAdapter
	{


		/**
		 * Handles the right-click, displaying the popup
		 */
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (GUIUtilities.isPopupTrigger(e))
			{
				if (!jEdit.getBooleanProperty("buffertabs.usePopup", true))
				{
					return;
				}

				// Request focus to this buffer so we close/reload/save the
				// right buffer!
				editPane.focusOnTextArea();

				//nab our popup
				JPopupMenu popupMenu = BufferTabsPlugin.getRightClickPopup();
				if (popupMenu == null)
				{
					return;
				}

				int x = e.getX();
				int y = e.getY();

				// Display it!
				popupMenu.show(e.getComponent(), x, y);
			}
			else
			{
				// if middle button close the buffer
				if (SwingUtilities.isMiddleMouseButton(e)
				    && jEdit.getBooleanProperty("buffertabs.close-tab-on.single-middle-click"))
				{
					// set the focus on the selected buffer
					int tabIndex = getTabAt(e.getX(), e.getY());
					if (tabIndex < 0)
						return;

					editPane.focusOnTextArea();
					jEdit.closeBuffer(editPane, editPane.getBufferSet().getBuffer(tabIndex));
				}
				if (SwingUtilities.isLeftMouseButton(e))
				{
					moving = getTabAt(e.getX(), e.getY());
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				if (moving != -1)
				{
					int mv = moving;
					moving = -1;
					int index = getTabAt(e.getX(), e.getY());
					if (index != -1 && index != mv)
					{
						boolean movingEnabled = true;
						if (areBuffersSorted())
						{
							JDialog dlg = new ReorderBuffersDisabledDialog(
								editPane.getView());
							dlg.setVisible(true);
							movingEnabled = !areBuffersSorted();
						}
						if (movingEnabled)
						{
							jEdit.moveBuffer(editPane, mv, index);
						}
					}
					else
					{
						// set the focus to the selected tab
						editPane.focusOnTextArea();
					}

					// always reset the moving indicator
					setCursor(Cursor.getDefaultCursor());
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e))
			{
				boolean doubleClick = e.getClickCount() == 2;
				boolean closeTab = jEdit.getBooleanProperty("buffertabs.close-tab-on.double-left-click");
				boolean toggleDocks = jEdit.getBooleanProperty("buffertabs.toggle-docks-on.double-left-click");
				if (doubleClick)
				{
					if (closeTab)
					{
						//set the focus on the selected buffer
						int tabIndex = getTabAt(e.getX(), e.getY());
						editPane.focusOnTextArea();
						jEdit.closeBuffer(editPane, editPane.getBufferSet().getBuffer(tabIndex));
					}
					else if (toggleDocks)
					{
						//set the focus on the selected buffer
						editPane.focusOnTextArea();
						jEdit.getActiveView().getDockableWindowManager().toggleDockAreas();
					}
				}
			}
		}

	}

	class MouseMotionHandler extends MouseMotionAdapter
	{
		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (moving != -1)
			{
				if (getCursor() != Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			}
			else
			{
				setCursor(Cursor.getDefaultCursor());
			}
		}
	}
}
