package com.lipstikLF.delegate;


import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.RootPaneUI;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.theme.LipstikColorTheme;
import com.lipstikLF.util.LipstikBorderFactory;
import com.lipstikLF.util.LipstikGradients;
import com.lipstikLF.util.LipstikListenerFactory;
import com.lipstikLF.util.LipstikBorderFactory.OptionalMatteBorder;



public class LipstikTitlePane extends JComponent
{
	private static final int IMAGE_HEIGHT = 16;
	private static final int IMAGE_WIDTH = 16;
	

	/** PropertyChangeListener added to the JRootPane. */
	private PropertyChangeListener propertyChangeListener;

    /** JMenuBar, typically renders the system menu items. */
    private JMenuBar menuBar;
	
    /** Action used to close the Window. */
    private Action closeAction;

	/** Action used to iconify the Frame. */
	private Action iconifyAction;

	/** Action to restore the Frame size. */
	private Action restoreAction;

	/** Action to restore the Frame size. */
	private Action maximizeAction;

	/** Button used to maximize or restore the Frame. */
	private JButton toggleButton;

	/** Button used to maximize or restore the Frame. */
	private JButton iconifyButton;

	/** Button used to maximize or restore the Frame. */
	private JButton closeButton;

	/** Icon used for toggleButton when window is normal size. */
	private Icon maximizeIcon;

	/** Icon used for toggleButton when window is maximized. */
	private Icon minimizeIcon;
	
	/** Listens for changes in the state of the Window listener to 
	 *  update the state of the widgets.
	 */
	private WindowListener windowListener;

	/** Window we're currently in. */
	private Window window;

	/** JRootPane rendering for. */
	private JRootPane rootPane;

	/** Buffered Frame.state property. As state isn't bound, this is 
	 *  kept to determine when to avoid updating widgets.
	 */
	private int state;
	
	public LipstikTitlePane(JRootPane root, RootPaneUI ui)
	{
		this.rootPane = root;
		state = -1;

		installSubcomponents();
		installDefaults();

		setLayout(createLayout());
	}

	/**
	 * Uninstalls the necessary state.
	 */
	public void uninstall()
	{
		uninstallListeners();
		window = null;
		removeAll();
	}

	/**
	 * Installs the necessary listeners.
	 */
	private void installListeners()
	{
		if (window != null)
		{
			windowListener = new WindowHandler();
			propertyChangeListener = new PropertyChangeHandler();			
			window.addWindowListener(windowListener);
			window.addPropertyChangeListener(propertyChangeListener);
		}
	}

	/**
	 * Uninstalls the necessary listeners.
	 */
	private void uninstallListeners()
	{
		if (window != null)
		{
			window.removeWindowListener(windowListener);
			window.removePropertyChangeListener(propertyChangeListener);
		}
	}

	/**
	 * Returns the <code>JRootPane</code> this was created for.
	 */
	public JRootPane getRootPane()
	{
		return rootPane;
	}

	/**
	 * Returns the decoration style of the <code>JRootPane</code>.
	 */
	private int getWindowDecorationStyle()
	{
		return getRootPane().getWindowDecorationStyle();
	}

	public void addNotify()
	{
		super.addNotify();
		uninstallListeners();

		if ((window=SwingUtilities.getWindowAncestor(this)) != null)
		{
			if (window instanceof Frame)
				setState(((Frame) window).getExtendedState());
			else
				setState(0);
			
			setActive(window.isActive());
			installListeners();
		}
	}

	public void removeNotify()
	{
		super.removeNotify();

		uninstallListeners();
		window = null;
	}

	/**
	 * Adds any sub-Components contained in the <code>MetalTitlePane</code>.
	 */
	private void installSubcomponents()
	{
		int style = getWindowDecorationStyle();
		if (style == JRootPane.FRAME)
		{
			createActions();
			createButtons();
			add(createMenuBar());			
			add(iconifyButton);
			add(toggleButton);
			add(closeButton);
		}
		else 
		if (style == JRootPane.PLAIN_DIALOG ||
            style == JRootPane.INFORMATION_DIALOG ||
            style == JRootPane.ERROR_DIALOG ||
            style == JRootPane.COLOR_CHOOSER_DIALOG ||
            style == JRootPane.FILE_CHOOSER_DIALOG ||
            style == JRootPane.QUESTION_DIALOG ||
            style == JRootPane.WARNING_DIALOG) 
		{
			createActions();
			createButtons();
			add(closeButton);
		}		
	}

	/**
	 * Installs the fonts and necessary properties on the MetalTitlePane.
	 */
	private void installDefaults()
	{
		setFont(UIManager.getFont("InternalFrame.titleFont", getLocale()));
	}

	/**
	 * Returns the <code>JMenuBar</code> displaying the appropriate system
	 * menu items.
	 */
	protected JMenuBar createMenuBar()
	{
		menuBar = new SystemMenuBar();
		menuBar.setOpaque(false);
		menuBar.setFocusable(false);
		menuBar.setBorderPainted(false);
		menuBar.add(createMenu());
		return menuBar;
	}

	/**
	 * Closes the Window.
	 */
	private void close()
	{
		Window window = getWindow();
		if (window != null)
			window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Iconifies the Frame.
	 */
	private void iconify()
	{
		Frame frame = getFrame();
		if (frame != null)
			frame.setExtendedState(state | Frame.ICONIFIED);
	}

	/**
	 * Maximizes the Frame.
	 */
	private void maximize()
	{
		Frame frame = getFrame();
		if (frame != null)
			frame.setExtendedState(state | Frame.MAXIMIZED_BOTH);
	}

	/**
	 * Restores the Frame size.
	 */
	private void restore()
	{
		Frame frame = getFrame();
		if (frame == null)
			return;

		if ((state & Frame.ICONIFIED) != 0)
			frame.setExtendedState(state & ~Frame.ICONIFIED);
		else
			frame.setExtendedState(state & ~Frame.MAXIMIZED_BOTH);

	}

	/**
	 * Create the <code>Action</code> s that get associated with the buttons
	 * and menu items.
	 */
	private void createActions()
	{
		closeAction = new CloseAction();
		iconifyAction = new IconifyAction();
		restoreAction = new RestoreAction();
		maximizeAction = new MaximizeAction();
	}

	/**
	 * Returns the <code>JMenu</code> displaying the appropriate menu items
	 * for manipulating the Frame.
	 */
	private JMenu createMenu()
	{
		JMenu menu = new JMenu("WW");
		menu.setOpaque(false);
		if (getWindowDecorationStyle() == JRootPane.FRAME)
			addMenuItems(menu);

		return menu;
	}

	/**
	 * Adds the necessary <code>JMenuItem</code> s to the passed in menu.
	 */
	private void addMenuItems(JMenu menu)
	{
		JMenuItem mi = menu.add(restoreAction);
		int mnemonic = getInt("MetalTitlePane.restoreMnemonic", -1);

		if (mnemonic != -1)
			mi.setMnemonic(mnemonic);

		mi = menu.add(iconifyAction);
		mnemonic = getInt("MetalTitlePane.iconifyMnemonic", -1);
		if (mnemonic != -1)
			mi.setMnemonic(mnemonic);

		if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH))
		{
			mi = menu.add(maximizeAction);
			mnemonic = getInt("MetalTitlePane.maximizeMnemonic", -1);
			if (mnemonic != -1)
				mi.setMnemonic(mnemonic);
		}

		menu.add(new JSeparator());

		mi = menu.add(closeAction);
		mnemonic = getInt("MetalTitlePane.closeMnemonic", -1);
		if (mnemonic != -1)
			mi.setMnemonic(mnemonic);
	}

	/**
	 * Returns a <code>JButton</code> appropriate for placement on the
	 * TitlePane.
	 */
	private JButton createTitleButton()
	{
		JButton button = new JButton();

		button.setFocusPainted(false);
		button.setFocusable(false);
		button.setOpaque(false);
		return button;
	}

	/**
	 * Creates the Buttons that will be placed on the TitlePane.
	 */
	private void createButtons()
	{
		OptionalMatteBorder handyEmptyBorder = LipstikBorderFactory.getOptionalMatteBorder();
		MouseAdapter handler = LipstikListenerFactory.getFrameButtonMouseHandler();

		maximizeIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
		minimizeIcon = UIManager.getIcon("InternalFrame.minimizeIcon");

		closeButton = createTitleButton();
		closeButton.setAction(closeAction);
		closeButton.setText(null);
		//closeButton.putClientProperty("paintActive", Boolean.TRUE);
		closeButton.setBorder(handyEmptyBorder);
		closeButton.getAccessibleContext().setAccessibleName("Close");
		closeButton.setIcon(UIManager.getIcon("InternalFrame.closeIcon"));
		closeButton.setOpaque(true);
		closeButton.addMouseListener(handler);
		
		iconifyButton = createTitleButton();
		iconifyButton.setAction(iconifyAction);
		iconifyButton.setText(null);
		//iconifyButton.putClientProperty("paintActive", Boolean.TRUE);
		iconifyButton.setBorder(handyEmptyBorder);
		iconifyButton.getAccessibleContext().setAccessibleName("Iconify");
		iconifyButton.setIcon(UIManager.getIcon("InternalFrame.iconifyIcon"));
		iconifyButton.setOpaque(true);
		iconifyButton.addMouseListener(handler);
		
		toggleButton = createTitleButton();
		toggleButton.setAction(restoreAction);
		//toggleButton.putClientProperty("paintActive", Boolean.TRUE);
		toggleButton.setBorder(handyEmptyBorder);
		toggleButton.getAccessibleContext().setAccessibleName("Maximize");
		toggleButton.setIcon(maximizeIcon);
		toggleButton.setOpaque(true);
		toggleButton.addMouseListener(handler);
	}

	/**
	 * Returns the <code>LayoutManager</code> that should be installed on the
	 * <code>MetalTitlePane</code>.
	 */
	private LayoutManager createLayout()
	{
		return new TitlePaneLayout();
	}

	/**
	 * Updates state dependant upon the Window's active state.
	 */
	private void setActive(boolean isActive)
	{
		if (getWindowDecorationStyle() == JRootPane.FRAME)
		{
			Boolean activeB = isActive ? Boolean.TRUE : Boolean.FALSE;

			iconifyButton.putClientProperty("paintActive", activeB);
			closeButton.putClientProperty("paintActive", activeB);
			toggleButton.putClientProperty("paintActive", activeB);
		}
		// Repaint the whole thing as the Borders that are used have
		// different colors for active vs inactive
		getRootPane().repaint();
	}

	
	/**
	 * Sets the state of the Window.
	 */
	private void setState(int state)
	{
		setState(state, false);
	}

	/**
	 * Sets the state of the window. If <code>updateRegardless</code> is true
	 * and the state has not changed, this will update anyway.
	 */
	private void setState(int state, boolean updateRegardless)
	{
		Window w = getWindow();

		if (w != null && getWindowDecorationStyle() == JRootPane.FRAME)
		{
			if (this.state == state && !updateRegardless)
				return;

			Frame frame = getFrame();
			if (frame != null)
			{
				if (frame.isResizable())
				{
					if ((state & Frame.MAXIMIZED_BOTH) != 0)
					{
						updateToggleButton(restoreAction, minimizeIcon);
						maximizeAction.setEnabled(false);
						restoreAction.setEnabled(true);
					}
					else
					{
						updateToggleButton(maximizeAction, maximizeIcon);
						maximizeAction.setEnabled(true);
						restoreAction.setEnabled(false);
					}
					if (toggleButton.getParent() == null || iconifyButton.getParent() == null)
					{
						toggleButton.setText(null);
						
						add(toggleButton);
						add(iconifyButton);
						revalidate();
						repaint();
					}
				}
				else
				{
					maximizeAction.setEnabled(false);
					restoreAction.setEnabled(false);
					if (toggleButton.getParent() != null)
					{
						remove(toggleButton);
						revalidate();
						repaint();
					}
				}
			}
			else
			{
				// Not contained in a Frame
				maximizeAction.setEnabled(false);
				restoreAction.setEnabled(false);
				iconifyAction.setEnabled(false);
				remove(toggleButton);
				remove(iconifyButton);
				revalidate();
				repaint();
			}
			closeAction.setEnabled(true);
			this.state = state;
		}
	}

	/**
	 * Updates the toggle button to contain the Icon <code>icon</code>, and
	 * Action <code>action</code>.
	 */
	private void updateToggleButton(Action action, Icon icon)
	{
		toggleButton.setAction(action);
		toggleButton.setIcon(icon);
		toggleButton.setText(null);
	}

	/**
	 * Returns the Frame rendering in. This will return null if the <code>JRootPane</code>
	 * is not contained in a <code>Frame</code>.
	 */
	private Frame getFrame()
	{
		Window window = getWindow();

		if (window instanceof Frame)
		{
			return (Frame) window;
		}
		return null;
	}

	/**
	 * Returns the <code>Window</code> the <code>JRootPane</code> is
	 * contained in. This will return null if there is no parent ancestor of the
	 * <code>JRootPane</code>.
	 */
	public Window getWindow()
	{
		return window;
	}


	/**	Paints this component. */
	public void paintComponent(Graphics g)
	{
		LipstikColorTheme theme = LipstikLookAndFeel.getMyCurrentTheme();

		if (getFrame() != null) 
			setState(getFrame().getExtendedState());
	
		Color backColor;
		Color fontColor;
		Color buttonColor;
		
		// Draw gradient	
		if (window.isActive())
		{
			backColor = UIManager.getColor("InternalFrame.activeTitleBackground");
			fontColor = UIManager.getColor("InternalFrame.activeTitleForeground");
			buttonColor = theme.getInternalButtonBackground();
		}
		else
		{
			backColor = UIManager.getColor("InternalFrame.inactiveTitleBackground");
			fontColor = UIManager.getColor("InternalFrame.inactiveTitleForeground");
			buttonColor = theme.getInternalButtonInactive();
		}

		LipstikGradients.drawGradient(g, backColor, null, 0,0, getWidth(), getHeight(), true);

		if (toggleButton != null)
			toggleButton.setBackground(buttonColor);
		if (iconifyButton != null)
			iconifyButton.setBackground(buttonColor);
		if (closeButton != null)
			closeButton.setBackground(buttonColor);
		
		String title = null;
		if (window instanceof Frame)
			title = ((Frame) window).getTitle();
		else 
		if (window instanceof Dialog)
			title = ((Dialog) window).getTitle();
				
		// Draw icon and text
		if (title != null)
		{		
			g.setFont(UIManager.getFont("InternalFrame.font"));
			g.setColor(fontColor);
			FontMetrics fm = g.getFontMetrics();
			int yOffset= ((getHeight() - fm.getHeight()) / 2) + fm.getAscent() + 1;
			int xOffset= 8;
			
			if (menuBar != null) 
				xOffset+=IMAGE_WIDTH;
			
			g.drawString(title, xOffset, yOffset);
		}
	}
	
	static int getInt(Object key, int defaultValue) 
	{
		Object value = UIManager.get(key);

		if (value instanceof Integer) 
			return ((Integer)value).intValue();
		
		if (value instanceof String) 
		{
			try 
			{
				return Integer.parseInt((String)value);
			} 
			catch (NumberFormatException nfe) 
			{
                System.err.println(nfe);
            }
		}
		return defaultValue;
	}	

	/**
	 * Actions used to <code>close</code> the <code>Window</code>.
	 */
	private class CloseAction extends AbstractAction
	{
		public CloseAction()
		{
			super(UIManager.getString("MetalTitlePane.closeTitle", getLocale()));
		}

		public void actionPerformed(ActionEvent e)
		{
			close();
		}
	}

	/**
	 * Actions used to <code>iconfiy</code> the <code>Frame</code>.
	 */
	private class IconifyAction extends AbstractAction
	{
		public IconifyAction()
		{
			super(UIManager.getString("MetalTitlePane.iconifyTitle", getLocale()));
		}

		public void actionPerformed(ActionEvent e)
		{
			iconify();
		}
	}

	/**
	 * Actions used to <code>restore</code> the <code>Frame</code>.
	 */
	private class RestoreAction extends AbstractAction
	{
		public RestoreAction()
		{
			super(UIManager.getString("MetalTitlePane.restoreTitle", getLocale()));
		}

		public void actionPerformed(ActionEvent e)
		{
			restore();
		}
	}

	/**
	 * Actions used to <code>restore</code> the <code>Frame</code>.
	 */
	private class MaximizeAction extends AbstractAction
	{
		public MaximizeAction()
		{
			super(UIManager.getString("MetalTitlePane.maximizeTitle", getLocale()));
		}

		public void actionPerformed(ActionEvent e)
		{
			maximize();
		}
	}

	/**
	 * Class responsible for drawing the system menu. Looks up the image to draw
	 * from the Frame associated with the <code>JRootPane</code>.
	 */
	private class SystemMenuBar extends JMenuBar
	{
		public void paint(Graphics g)
		{
			Frame frame = getFrame();
			Image image = (frame != null) ? frame.getIconImage() : null;
			
			if (isOpaque())
			{
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}		

			if (image != null)
				g.drawImage(image, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
			else
			{
				Icon icon = UIManager.getIcon("InternalFrame.icon");
				if (icon != null)
					icon.paintIcon(this, g, 0, 0);
			}
		}
		public Dimension getMinimumSize()
		{
			return getPreferredSize();
		}
		public Dimension getPreferredSize()
		{
			Dimension size = super.getPreferredSize();

			return new Dimension(
				Math.max(IMAGE_WIDTH, size.width),
				Math.max(size.height, IMAGE_HEIGHT));
		}
	}

	/**
	 * This inner class is marked &quot;public&quot; due to a compiler bug. This
	 * class should be treated as a &quot;protected&quot; inner class.
	 * Instantiate it only within subclasses of <Foo>.
	 */
	private class TitlePaneLayout implements LayoutManager
	{
		public void addLayoutComponent(String name, Component c)
		{
		}
		public void removeLayoutComponent(Component c)
		{
		}
		public Dimension preferredLayoutSize(Container c)
		{
			int height = computeHeight(c);
			return new Dimension(height, height);
		}

		public Dimension minimumLayoutSize(Container c)
		{
			return preferredLayoutSize(c);
		}

		private int computeHeight(Container c)
		{
			FontMetrics fm = c.getGraphics().getFontMetrics();
			return fm.getHeight()+5;
		}

		public void layoutContainer(Container c)
		{
			
			boolean leftToRight = c.getComponentOrientation().isLeftToRight();

			int w = getWidth();
			int spacing = 3;
			int buttonHeight, buttonWidth;
			
			if (closeButton != null && closeButton.getIcon() != null)
			{
				buttonHeight = closeButton.getIcon().getIconHeight()+5;
				buttonWidth = closeButton.getIcon().getIconWidth()+5;
			}
			else
			{
				buttonHeight = IMAGE_HEIGHT;
				buttonWidth = IMAGE_WIDTH;
			}
			
			int x = leftToRight ? spacing : w - buttonWidth - spacing; 
			int y = getHeight()/2-buttonHeight/2;
			
	
			if (menuBar != null)
				menuBar.setBounds(x, y, IMAGE_WIDTH, IMAGE_HEIGHT);
			
			x = leftToRight ? w : 0;
			
			if (closeButton != null)
			{
				x += leftToRight ? -spacing - buttonWidth : spacing;
				closeButton.setBounds(x, y, buttonWidth, buttonHeight);
				if (!leftToRight)
					x += buttonWidth;
			}

			if (toggleButton != null && toggleButton.isEnabled())
			{
				x += leftToRight ? -spacing - buttonWidth : spacing;
				toggleButton.setBounds(x, y, buttonWidth, buttonHeight);
				if (!leftToRight)
					x += buttonWidth;
			}

			if (iconifyButton != null)
			{
				x += leftToRight ? -spacing - buttonWidth : spacing;
				iconifyButton.setBounds(x, y, buttonWidth, buttonHeight);
				if (!leftToRight)
					x += buttonWidth;
			}
		}
	}

	/**
	 * PropertyChangeListener installed on the Window. Updates the necessary
	 * state as the state of the Window changes.
	 */
	private class PropertyChangeHandler implements PropertyChangeListener
	{
		public void propertyChange(PropertyChangeEvent pce)
		{
			String name = pce.getPropertyName();

			// Frame.state isn't currently bound.
			if ("resizable".equals(name) || "state".equals(name))
			{
				Frame frame = getFrame();

				if (frame != null)
					setState(frame.getExtendedState(), true);
			
				if ("resizable".equals(name))
					getRootPane().repaint();
			}
			else if ("title".equals(name))
			{
				repaint();
			}
			else if ("componentOrientation".equals(name))
			{
				revalidate();
				repaint();
			}
		}
	}

	/**
	 * WindowListener installed on the Window, updates the state as necessary.
	 */
	private class WindowHandler extends WindowAdapter
	{
		public void windowActivated(WindowEvent ev)
		{
			setActive(true);
		}

		public void windowDeactivated(WindowEvent ev)
		{
			setActive(false);
		}
	}
}
