/*
 * SessionPropertiesDialog.java - session properties dialog
 * Copyright (C) 2001 Dirk Moebius
 *
 * Based on GlobalOptionsDialog.java
 * Copyright (C) 1998, 1999, 2000, 2001 Slava Pestov
 * Portions copyright (C) 1999 mike dillon
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


import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.Vector;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;


/**
 * The dialog for session properties, showing the registered
 * session property panes.
 */
public class SessionPropertiesDialog extends EnhancedDialog
	implements ActionListener, TreeSelectionListener
{

	SessionPropertiesDialog(View view, String sessionName, SessionPropertyGroup rootGroup)
	{
		super(view, jEdit.getProperty("sessions.sessionproperties.title",
			new Object[] { sessionName }), true);

		this.rootGroup = rootGroup;

		view.showWaitCursor();

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JPanel stage = new JPanel(new BorderLayout());
		stage.setBorder(new EmptyBorder(0,6,0,0));
		content.add(stage, BorderLayout.CENTER);

		// currentLabel displays the path of the currently selected
		// PropertyPane at the top of the stage area
		currentLabel = new JLabel();
		currentLabel.setHorizontalAlignment(JLabel.LEFT);
		currentLabel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.black));
		stage.add(currentLabel, BorderLayout.NORTH);

		cardPanel = new JPanel(new CardLayout());
		cardPanel.setBorder(new EmptyBorder(5,0,0,0));
		stage.add(cardPanel, BorderLayout.CENTER);

		paneTree = new JTree(createTreeModel());
		paneTree.setCellRenderer(new CellRenderer());
		paneTree.putClientProperty("JTree.lineStyle", "Angled");
		paneTree.setShowsRootHandles(true);
		paneTree.setRootVisible(false);
		content.add(new JScrollPane(paneTree,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
			BorderLayout.WEST);

		JPanel buttons = new JPanel();
		buttons.setBorder(new EmptyBorder(12,0,0,0));
		buttons.setLayout(new BoxLayout(buttons,BoxLayout.X_AXIS));
		buttons.add(Box.createGlue());

		ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(this);
		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(6));
		getRootPane().setDefaultButton(ok);
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(this);
		buttons.add(cancel);
		buttons.add(Box.createHorizontalStrut(6));
		apply = new JButton(jEdit.getProperty("common.apply"));
		apply.addActionListener(this);
		buttons.add(apply);

		buttons.add(Box.createGlue());

		content.add(buttons, BorderLayout.SOUTH);

		// register the Options dialog as a TreeSelectionListener.
		// this is done before the initial selection to ensure that the
		// first selected OptionPane is displayed on startup.
		paneTree.getSelectionModel().addTreeSelectionListener(this);

		// select the first member of the root group
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)paneTree.getModel().getRoot();
		DefaultMutableTreeNode firstChild = (DefaultMutableTreeNode)root.getFirstChild();
		TreePath path = new TreePath(firstChild.getPath());
		paneTree.setSelectionPath(path);

		view.hideWaitCursor();

		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}


	// EnhancedDialog implementation

	public void ok()
	{
		ok(true);
	}


	public void cancel()
	{
		dispose();
	}

	// end EnhancedDialog implementation


	public void ok(boolean dispose)
	{
		// Save all session property panes
		rootGroup.save();
		// This will fire the PROPERTIES_CHANGED event
		jEdit.propertiesChanged();
		// Save settings to disk
		jEdit.saveSettings();
		// Fire the SessionPropertiesChanged event
		EditBus.send(new SessionPropertiesChanged(
			SessionManager.getInstance(),
			SessionManager.getInstance().getCurrentSessionInstance()));
		// get rid of this dialog if necessary
		if(dispose)
			dispose();
	}


	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();

		if(source == ok)
			ok();
		else if(source == cancel)
			cancel();
		else if(source == apply)
			ok(false);
	}


	public void valueChanged(TreeSelectionEvent evt)
	{
		TreePath path = evt.getPath();
		if(path == null)
			return;

		DefaultMutableTreeNode last = (DefaultMutableTreeNode) path.getLastPathComponent();
		Object obj = last.getUserObject();
		if(!(obj instanceof SessionPropertyPane))
			return;

		SessionPropertyPane pane = (SessionPropertyPane)obj;
		pane.init();
		((CardLayout)cardPanel.getLayout()).show(cardPanel, pane.getIdentifier());

		currentLabel.setText(labelForPath(path));

		pack();
	}


	private String labelForPath(TreePath path)
	{
		Object[] nodes = path.getPath();
		int lastIdx = nodes.length - 1;
		StringBuffer buf = new StringBuffer();
		String label = null;

		for(int i = paneTree.isRootVisible() ? 0 : 1; i <= lastIdx; i++)
		{
			Object obj = ((DefaultMutableTreeNode)nodes[i]).getUserObject();
			if(obj instanceof SessionPropertyPane)
				label = ((SessionPropertyPane)obj).getLabel();
			else if(obj instanceof SessionPropertyGroup)
				label = ((SessionPropertyGroup)obj).getLabel();
			else
				continue;

			if(label != null)
				buf.append(label);
			if(i != lastIdx)
				buf.append(": ");
		}

		return buf.toString();
	}


	private JTree paneTree;
	private JPanel cardPanel;
	private JLabel currentLabel;
	private JButton ok;
	private JButton cancel;
	private JButton apply;
	private SessionPropertyGroup rootGroup;


	private TreeModel createTreeModel()
	{
		rootGroup.sort();
		return new DefaultTreeModel(createGroupNode(rootGroup));
	}


	private DefaultMutableTreeNode createGroupNode(SessionPropertyGroup group)
	{
		DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group);
		Enumeration myEnum = group.getChildren();
		while(myEnum.hasMoreElements())
		{
			Object next = myEnum.nextElement();
			if(next instanceof SessionPropertyGroup)
				groupNode.add(createGroupNode((SessionPropertyGroup)next));
			else if(next instanceof SessionPropertyPane)
			{
				SessionPropertyPane pane = (SessionPropertyPane)next;
				groupNode.add(new DefaultMutableTreeNode(pane, false));
				cardPanel.add(pane, pane.getIdentifier());
			}
		}
		return groupNode;
	}


	/** A tree cell renderer that shows no icons. */
	private class CellRenderer extends DefaultTreeCellRenderer
	{
		public Component getTreeCellRendererComponent(
			JTree tree,
			Object value,
			boolean selected,
			boolean expanded,
			boolean isLeaf,
			int row,
			boolean hasFocus)
		{
			super.getTreeCellRendererComponent(tree,value,selected,expanded,isLeaf,row,hasFocus);
			setIcon(null);
			return this;
		}
	}

}
