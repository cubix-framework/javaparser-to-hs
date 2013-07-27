/*
 * SessionPropertyPane.java - option pane for session properties
 * Copyright (C) 2001 Dirk Moebius
 *
 * Loosely modelled after AbstractOptionPane.java
 * Copyright (C) 1998, 1999, 2000, 2001 Slava Pestov
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


package sessions;


import java.awt.*;
import java.util.Enumeration;
import javax.swing.border.EmptyBorder;
import javax.swing.*;
import javax.swing.tree.TreeNode;
import org.gjt.sp.jedit.jEdit;


/**
 * An abstract option pane for session properties.
 * All session option panes must inherit from this class.
 */
public abstract class SessionPropertyPane extends JPanel
{
	/**
	 * Creates a new session property pane.
	 * @param session  the session.
	 */
	public SessionPropertyPane(Session session)
	{
		this.session = session;
		setLayout(gridBag = new GridBagLayout());
	}


	/**
	 * Return a unique identifier for this pane.
	 * Typically this identifier should include your plugin's name,
	 * eg. "myplugin.sessionproperties.firstPane".
	 */
	public abstract String getIdentifier();


	/**
	 * Return the visible label of this property pane.
	 */
	public abstract String getLabel();


	/**
	 * This method should create the property pane's GUI.
	 * Use <code>session.getProperty(String key)</code> to retrieve properties
	 * of the session belonging to this option pane.
	 * Use <code>setLayout(LayoutManager)</code> at the start of the
	 * method, if you want a different LayoutManager than the default,
	 * GridBayLayout.
	 */
	public abstract void _init();


	/**
	 * Called when the options dialog's "ok" button is clicked.
	 * This should save any properties being edited in this option
	 * pane.
	 * Save properties to the session belonging to this instance with
	 * <code>session.setProperty(String key, String value)</code>.
	 */
	public abstract void _save();


	public final String toString()
	{
		return getLabel();
	}


	/**
	 * The session instance for this option pane.
	 * Save properties to this session belonging to this instance with
	 * <code>session.setProperty(String key, String value)</code>,
	 * retrieve session properties with
	 * <code>session.getProperty(String key)</code>.
	 */
	protected Session session;

	/** The default layout manager. */
	protected GridBagLayout gridBag;

	/** The number of components already added to the layout manager. */
	protected int y;


	/**
	 * Adds two components to the pane. The two components are
	 * aligned left to right, forming a row. With each invocation of
	 * addComponent(), a new row is created.
	 *
	 * @param leftComp  The left component
	 * @param rightComp  The right component
	 * @throws IllegalStateException  if the layout manager of this panel
	 *   has been changed to something other than GridBagLayout.
	 *   You cannot use this method if the layout is not gridbag.
	 */
	protected void addComponent(Component leftComp, Component rightComp)
	{
		if(!(getLayout() instanceof GridBagLayout))
			throw new IllegalStateException("current layout manager is no GridBagLayout, current is: "
				+ getLayout().getClass().getName());

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = 1;
		cons.weightx = 0.0f;
		cons.fill = GridBagConstraints.BOTH;

		gridBag.setConstraints(leftComp,cons);
		add(leftComp);

		cons.gridx = 1;
		cons.weightx = 1.0f;
		gridBag.setConstraints(rightComp,cons);
		add(rightComp);
	}


	/**
	 * Adds a labeled component to the pane. Components are
	 * added in a vertical fashion, one per row. The label is
	 * displayed to the left of the component.
	 *
	 * @param label The jEdit property for the label text, NOT the label
	 *   text itself!
	 * @param comp The component on the right.
	 * @throws IllegalStateException  if the layout manager of this panel
	 *   has been changed to something other than GridBagLayout.
	 *   You cannot use this method if the layout is not gridbag.
	 */
	protected void addComponent(String labelProperty, Component comp)
	{
		String label = jEdit.getProperty(labelProperty);
		if(label == null)
			label = labelProperty;

		JLabel l = new JLabel(label, SwingConstants.RIGHT);
		l.setBorder(new EmptyBorder(0,0,0,12));
		addComponent(l, comp);
	}


	/**
	 * Adds a component to the option pane. Components are
	 * added in a vertical fashion, one per row.
	 *
	 * @param comp The component
	 * @throws IllegalStateException  if the layout manager of this panel
	 *   has been changed to something other than GridBagLayout.
	 *   You cannot use this method if the layout is not gridbag.
	 */
	protected void addComponent(Component comp)
	{
		if(!(getLayout() instanceof GridBagLayout))
			throw new IllegalStateException("current layout manager is no GridBagLayout, current is: "
				+ getLayout().getClass().getName());

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = cons.REMAINDER;
		cons.fill = GridBagConstraints.NONE;
		cons.anchor = GridBagConstraints.WEST;
		cons.weightx = 1.0f;

		gridBag.setConstraints(comp,cons);
		add(comp);
	}


	/**
	 * Adds a separator component.
	 *
	 * @param label The separator label property
	 * @throws IllegalStateException  if the layout manager of this panel
	 *   has been changed to something other than GridBagLayout.
	 *   You cannot use this method if the layout is not gridbag.
	 */
	protected void addSeparator(String label)
	{
		if(!(getLayout() instanceof GridBagLayout))
			throw new IllegalStateException("current layout manager is no GridBagLayout, current is: "
				+ getLayout().getClass().getName());

		Box box = new Box(BoxLayout.X_AXIS);
		Box box2 = new Box(BoxLayout.Y_AXIS);
		box2.add(Box.createGlue());
		box2.add(new JSeparator(JSeparator.HORIZONTAL));
		box2.add(Box.createGlue());
		box.add(box2);
		JLabel l = new JLabel(" " + jEdit.getProperty(label) + " ");
		l.setMaximumSize(l.getPreferredSize());
		box.add(l);
		Box box3 = new Box(BoxLayout.Y_AXIS);
		box3.add(Box.createGlue());
		box3.add(new JSeparator(JSeparator.HORIZONTAL));
		box3.add(Box.createGlue());
		box.add(box3);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = cons.REMAINDER;
		cons.fill = GridBagConstraints.BOTH;
		cons.anchor = GridBagConstraints.WEST;
		cons.weightx = 1.0f;

		gridBag.setConstraints(box,cons);
		add(box);
	}


	void init()
	{
		if(!initialized)
		{
			initialized = true;
			_init();
		}
	}


	void save()
	{
		if(initialized)
			_save();
	}


	private boolean initialized;

}
