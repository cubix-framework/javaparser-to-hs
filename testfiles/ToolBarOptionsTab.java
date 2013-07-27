/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package gruntspud.ui.preferences;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractGruntspudAction;
import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.GruntspudAction;
import gruntspud.ui.UIUtil;
import gruntspud.ui.GruntspudCheckBox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 * Description of the Class
 * 
 * @author magicthize @created 26 May 2002
 */
public class ToolBarOptionsTab extends AbstractOptionsTab implements ListSelectionListener, ActionListener {
	public final static String[] TOOLBAR_PLACEMENT = { "North", "East", "South", "West" };
	
	static DataFlavor[] supportedFlavors = new DataFlavor[1];

	static {
		try { 
			supportedFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType); 
		}
		catch (Exception ex) { 
			ex.printStackTrace(); 
		}
	}

	private JList hiddenTools;
	private JList shownTools;
	private ToolListModel hiddenModel;
	private ToolListModel shownModel;
	private JButton add;
	private JButton remove;
	private JButton addAll;
	private JButton removeAll;
	private JButton moveUp;
	private JButton moveDown;
	private GruntspudCheckBox smallIcons, showSelectiveText;
	private String oldActions;
	private JComboBox placement;
	/**
	 * Constructor for the GlobalOptionsTab object
	 */
	public ToolBarOptionsTab() {
		super("Tool Bar", UIUtil.getCachedIcon(Constants.ICON_TOOL_MAINTAIN));
	}
	/**
	 * DOCUMENT ME!
	 * 
	 * @param context DOCUMENT ME!
	 */
	public void init(GruntspudContext context) {
		super.init(context);
		setTabToolTipText("Options for the toolbar.");
		setTabLargeIcon(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_MAINTAIN));
		setLayout(new GridBagLayout());
		setTabMnemonic('r');
		setTabContext("UI");
		//
		hiddenTools = new DnDList(hiddenModel = new ToolListModel(), "Actions");
		hiddenTools.setCellRenderer(new ActionListCellRenderer());
		hiddenTools.addListSelectionListener(this);
		hiddenTools.setDragEnabled(true);
		//		hiddenTools.setDropTarget(shownTools);
		JScrollPane hiddenScroller = new JScrollPane(hiddenTools);
		hiddenScroller.setAutoscrolls(true);
		//
		shownTools = new DnDList(shownModel = new ToolListModel(), "Active");
		shownTools.setCellRenderer(new ActionListCellRenderer());
		shownTools.addListSelectionListener(this);
		shownTools.setDragEnabled(true);
		JScrollPane shownScroller = new JScrollPane(shownTools);
		shownScroller.setAutoscrolls(true);
		//
		JPanel b = new JPanel(new GridBagLayout());
		b.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(2, 8, 2, 8);
		gbc.weightx = 1.0;
		UIUtil.jGridBagAdd(b, add = new JButton(" Add > "), gbc, GridBagConstraints.REMAINDER);
		add.setMnemonic('a');
		add.addActionListener(this);
		UIUtil.jGridBagAdd(b, addAll = new JButton(" Add >> "), gbc, GridBagConstraints.REMAINDER);
		addAll.setMnemonic('l');
		addAll.addActionListener(this);
		UIUtil.jGridBagAdd(b, remove = new JButton(" < Remove "), gbc, GridBagConstraints.REMAINDER);
		remove.setMnemonic('r');
		remove.addActionListener(this);
		UIUtil.jGridBagAdd(b, removeAll = new JButton(" << Remove "), gbc, GridBagConstraints.REMAINDER);
		removeAll.setMnemonic('v');
		removeAll.addActionListener(this);
		UIUtil.jGridBagAdd(b, moveUp = new JButton(" Move Up "), gbc, GridBagConstraints.REMAINDER);
		moveUp.setMnemonic('u');
		moveUp.addActionListener(this);
		gbc.weighty = 1.0;
		UIUtil.jGridBagAdd(b, moveDown = new JButton(" Move Down "), gbc, GridBagConstraints.REMAINDER);
		moveDown.setMnemonic('d');
		moveDown.addActionListener(this);
		//
		JPanel m = new JPanel(new GridBagLayout());
		m.setOpaque(false);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.anchor = GridBagConstraints.CENTER;
		gbc2.fill = GridBagConstraints.BOTH;
		m.setBorder(BorderFactory.createTitledBorder("Actions"));
		gbc2.weighty = 1.0;
		gbc2.weightx = 2.0;
		UIUtil.jGridBagAdd(m, hiddenScroller, gbc2, 1);
		gbc2.weightx = 0.0;
		UIUtil.jGridBagAdd(m, b, gbc2, GridBagConstraints.RELATIVE);
		gbc2.weightx = 2.0;
		UIUtil.jGridBagAdd(m, shownScroller, gbc2, GridBagConstraints.REMAINDER);
		//
		JPanel o = new JPanel(new GridBagLayout());
		o.setOpaque(false);
		GridBagConstraints gbco = new GridBagConstraints();
		gbco.anchor = GridBagConstraints.WEST;
		gbco.fill = GridBagConstraints.HORIZONTAL;
		gbco.insets = new Insets(2, 2, 2, 2);

		o.setBorder(BorderFactory.createTitledBorder("Options"));
		gbco.weightx = 2.0;
		UIUtil.jGridBagAdd(o, smallIcons = new GruntspudCheckBox("Use small icons"), gbco, 1);
		smallIcons.setMnemonic('s');
		smallIcons.setSelected(context.getHost().getBooleanProperty(Constants.TOOL_BAR_SMALL_ICONS, false));
		smallIcons.addActionListener(this);
		UIUtil.jGridBagAdd(o, showSelectiveText = new GruntspudCheckBox("Show selective text"), gbco, 1);
		showSelectiveText.setMnemonic('s');
		showSelectiveText.setSelected(context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true));
		gbco.weightx = 1.0;
		gbco.anchor = GridBagConstraints.EAST;
		UIUtil.jGridBagAdd(o, new JLabel("Placement", JLabel.RIGHT), gbco, GridBagConstraints.RELATIVE);
		gbco.weightx = 1.0;
		UIUtil.jGridBagAdd(o, placement = new JComboBox(TOOLBAR_PLACEMENT), gbco, GridBagConstraints.REMAINDER);
		placement.setSelectedItem(context.getHost().getProperty(Constants.TOOL_BAR_POSITION, TOOLBAR_PLACEMENT[0]));

		//
		//  This panel
		setLayout(new BorderLayout());
		add(m, BorderLayout.CENTER);
		add(o, BorderLayout.SOUTH);
		//
		String actions = context.getHost().getProperty(Constants.TOOL_BAR_ACTIONS, Constants.TOOL_BAR_DEFAULT_ACTIONS);
		StringTokenizer s = new StringTokenizer(actions, ",");
		while (s.hasMoreTokens()) {
			String n = s.nextToken();
			if (n.equals(Constants.TOOL_BAR_SEPARATOR)) {
				shownModel.add(new SeparatorAction());
			}
			else {
				Action a = context.getViewManager().getAction(n);
				if (a != null) {
					shownModel.add(a);
				}
			}
		}
		Action[] a = context.getViewManager().getActions();
		hiddenModel.add(new SeparatorAction());
		for (int i = 0; i < a.length; i++) {
			if (!shownModel.containsAction((String) a[i].getValue(GruntspudAction.INTERNAL_NAME))) {
				hiddenModel.add(a[i]);
			}
		}
		setAvailableActions();
	}
	/**
	 * DOCUMENT ME!
	 * 
	 * @param evt DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == add) {
			int[] z = hiddenTools.getSelectedIndices();
			for (int i = z.length - 1; i >= 0; i--) {
				if (z[i] == 0) {
					shownModel.add(new SeparatorAction());
				}
				else {
					shownModel.add((Action) hiddenModel.getElementAt(z[i]));
					shownTools.ensureIndexIsVisible(shownModel.getSize() - 1);
					hiddenModel.removeActionAt(z[i]);
				}
			}
		}
		else
			if (evt.getSource() == addAll) {
				int z = hiddenModel.getSize();
				for (int i = z - 1; i >= 1; i--) {
					shownModel.add((Action) hiddenModel.getElementAt(i));
					hiddenModel.removeActionAt(i);
				}
			}
			else
				if (evt.getSource() == remove) {
					int[] z = shownTools.getSelectedIndices();
					for (int i = z.length - 1; i >= 0; i--) {
						Action a = (Action) shownModel.getElementAt(z[i]);
						if (!(a instanceof SeparatorAction)) {
							hiddenModel.add(a);
						}
						shownModel.removeActionAt(z[i]);
					}
				}
				else
					if (evt.getSource() == removeAll) {
						int z = shownModel.getSize();
						for (int i = z - 1; i >= 0; i--) {
							Action a = (Action) shownModel.getElementAt(i);
							if (!a.getValue(GruntspudAction.INTERNAL_NAME).equals(Constants.ACTION_ALL_TOOLS)) {
								if (!(a instanceof SeparatorAction)) {
									hiddenModel.add((Action) shownModel.getElementAt(i));
								}
								shownModel.removeActionAt(i);
							}
						}
					}
					else
						if (evt.getSource() == moveUp) {
							int i = shownTools.getSelectedIndex();
							shownModel.moveUp(i);
							shownTools.setSelectedIndex(i - 1);
							shownTools.ensureIndexIsVisible(i - 1);
						}
						else
							if (evt.getSource() == moveDown) {
								int i = shownTools.getSelectedIndex();
								shownModel.moveDown(shownTools.getSelectedIndex());
								shownTools.setSelectedIndex(i + 1);
								shownTools.ensureIndexIsVisible(i + 1);
							}
							else
								if (evt.getSource() == smallIcons) {
									shownModel.reload();
									hiddenModel.reload();
								}
	}
	/**
	 * DOCUMENT ME!
	 * 
	 * @param evt DOCUMENT ME!
	 */
	public void valueChanged(ListSelectionEvent evt) {
		setAvailableActions();
	}
	/**
	 * Description of the Method
	 * 
	 * @return Description of the Return Value
	 */
	public boolean validateTab() {
		return true;
	}
	/**
	 * Description of the Method
	 */
	public void tabSelected() {
	}
	/**
	 * Description of the Method
	 */
	public void applyTab() {
		getContext().getHost().setBooleanProperty(Constants.TOOL_BAR_SMALL_ICONS, smallIcons.isSelected());
		getContext().getHost().setProperty(Constants.TOOL_BAR_ACTIONS, shownModel.getActionListString());
		getContext().getHost().setProperty(Constants.TOOL_BAR_POSITION, (String) placement.getSelectedItem());
		getContext().getHost().setBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, showSelectiveText.isSelected());
	}
	private void setAvailableActions() {
		add.setEnabled(!hiddenTools.isSelectionEmpty());
		remove.setEnabled(
			!shownTools.isSelectionEmpty()
				&& !(((Action) shownTools.getSelectedValue())
					.getValue(GruntspudAction.INTERNAL_NAME)
					.equals(Constants.ACTION_ALL_TOOLS)));
		moveUp.setEnabled((shownTools.getSelectedIndices().length == 1) && (shownTools.getSelectedIndex() > 0));
		moveDown.setEnabled(
			(shownTools.getSelectedIndices().length == 1) && ((shownTools.getSelectedIndex() + 1) < shownModel.getSize()));
	}
	class ToolListModel extends AbstractListModel {
		private Vector validActions;
		ToolListModel() {
			validActions = new Vector();
		}
		public boolean containsAction(String n) {
			for (int i = 0; i < getSize(); i++) {
				if (((Action) getElementAt(i)).getValue(GruntspudAction.INTERNAL_NAME).equals(n)) {
					return true;
				}
			}
			return false;
		}
		public GruntspudAction getAction(String n) {
			for (int i = 0; i < getSize(); i++) {
				GruntspudAction act = (GruntspudAction)getElementAt(i);
				if (act.getValue(GruntspudAction.INTERNAL_NAME).equals(n)) {
					return act;
				}
			}
			return null;
		}
		public void clear() {
			validActions.removeAllElements();
			fireContentsChanged(this, -1, -1);
		}
		public void removeActionAt(int i) {
			validActions.removeElementAt(i);
			fireIntervalRemoved(this, i, i);
		}
		public void moveUp(int i) {
			Action a = (Action) getElementAt(i);
			validActions.removeElementAt(i);
			fireIntervalRemoved(this, i, i);
			validActions.insertElementAt(a, i - 1);
			fireIntervalAdded(this, i - 1, i - 1);
		}
		public void moveDown(int i) {
			Action a = (Action) getElementAt(i);
			validActions.removeElementAt(i);
			fireIntervalRemoved(this, i, i);
			validActions.insertElementAt(a, i + 1);
			fireIntervalAdded(this, i + 1, i + 1);
		}
		public void add(Action action) {
			int i = getSize();
			validActions.addElement(action);
			fireIntervalAdded(this, i, i);
		}
		public void reload() {
			fireContentsChanged(this, -1, -1);
		}
		public String getActionListString() {
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < getSize(); i++) {
				if (i > 0) {
					buf.append(',');
				}
				Action a = (Action) getElementAt(i);
				if (a instanceof SeparatorAction) {
					buf.append(Constants.TOOL_BAR_SEPARATOR);
				}
				else {
					buf.append((String) a.getValue(GruntspudAction.INTERNAL_NAME));
				}
			}
			return buf.toString();
		}
		public void setActions(Action[] actions) {
			validActions.removeAllElements();
			for (int i = 0; i < actions.length; i++) {
				validActions.addElement(actions[i]);
			}
			fireContentsChanged(this, -1, -1);
		}
		public Object getElementAt(int t) {
			return validActions.elementAt(t);
		}
		public int getSize() {
			return validActions.size();
		}
		/**
		 * @param action
		 */
		public void removeAction(GruntspudAction action) {
			int i = validActions.indexOf(action);
			if(i != -1) {
				validActions.remove(action);			
				fireIntervalRemoved(this, i, i);
			}
		}
		/**
		 * @param action
		 * @param dropRow
		 */
		public void insert(GruntspudAction action, int row) {
			validActions.insertElementAt(action, row);
			fireIntervalAdded(this, row, row);
			
		}
		/**
		 * @param action
		 * @return
		 */
		public int indexOf(GruntspudAction action) {
			return validActions.indexOf(action);
		}
	}
	class SeparatorAction extends AbstractGruntspudAction {
		SeparatorAction() {
			putValue(Action.NAME, "Separator");
			putValue(GruntspudAction.INTERNAL_NAME, "Separator");
			putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_NORMAL_REMOVE));
			putValue(DefaultGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_NORMAL_REMOVE));
			putValue(Action.SHORT_DESCRIPTION, "Separator");
			putValue(Action.LONG_DESCRIPTION, "Separator");
		}
		public void actionPerformed(ActionEvent evt) {
		}
	}

	class ActionListCellRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Action n = (Action) value;
			if (smallIcons.isSelected()) {
				setIcon((Icon) n.getValue(GruntspudAction.SMALL_ICON));
			}
			else {
				setIcon((Icon) n.getValue(GruntspudAction.ICON));
			}
			setText((String) n.getValue(Action.NAME));
			return this;
		}
	}

	public class DnDList extends JList implements DropTargetListener, DragSourceListener, DragGestureListener {
		DropTarget dropTarget = new DropTarget(this, this);
		DragSource dragSource = DragSource.getDefaultDragSource();
		int dropRow = -1;
		Color lineColor;
		String name;

		public DnDList(ToolListModel model, String name) {
			super(model);
			this.name = name; // this is only really for sussing out dnd
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_MOVE, this);
			Color c = getForeground();
			lineColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
			setBorder(BorderFactory.createEmptyBorder(2, 2, 12, 2));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dragEnter(DropTargetDragEvent dtde) {
			dtde.acceptDrag(DnDConstants.ACTION_MOVE);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dragOver(DropTargetDragEvent dtde) {
			Insets i = getBorder().getBorderInsets(this);
			if(dtde.getLocation().y > ( getSize().height - i.bottom ) ) {
				dropRow = getModel().getSize();
			}
			else {
				dropRow = locationToIndex(dtde.getLocation());
			}
			repaint();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
		 */
		public void dropActionChanged(DropTargetDragEvent dtde) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
		 */
		public void drop(DropTargetDropEvent dtde) {
			try {
				ActionWrapper wrapper =  (ActionWrapper)dtde.getTransferable().getTransferData(supportedFlavors[0]);
				GruntspudAction action = wrapper.action;
				if(getModel() == wrapper.model) {
					int idx = ((ToolListModel)getModel()).indexOf(action);
					if(idx > dropRow) {
						wrapper.model.removeAction(wrapper.action);
						wrapper.model.insert(action, dropRow);						
					}
					else if(idx < dropRow) {
						wrapper.model.insert(action, dropRow);		
						wrapper.model.removeAction(wrapper.action);				
					}
				}
				else {
					wrapper.model.removeAction(wrapper.action);
					((ToolListModel)getModel()).insert(action, dropRow);
				}
				dropRow = -1;
				repaint();
			}
			catch (Exception e) {
				Constants.UI_LOG.error(e);
			}

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
		 */
		public void dragExit(DropTargetEvent dte) {
			dropRow = -1;
			repaint();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragEnter(DragSourceDragEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dragOver(DragSourceDragEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dropActionChanged(java.awt.dnd.DragSourceDragEvent)
		 */
		public void dropActionChanged(DragSourceDragEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
		 */
		public void dragDropEnd(DragSourceDropEvent dsde) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
		 */
		public void dragExit(DragSourceEvent dse) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
		 */
		public void dragGestureRecognized(DragGestureEvent dge) {
			Constants.UI_LOG.debug(dge);
            GruntspudAction action  = (GruntspudAction)getSelectedValue();
            if (action == null) {
                getToolkit().beep();
            } else {
            	ActionTransferable transferable = new ActionTransferable(
            		new ActionWrapper(action, (ToolListModel)this.getModel()));
                dge.startDrag(DragSource.DefaultMoveDrop, transferable, this);
            }

		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (dropRow != -1) {
				g.setColor(lineColor);
				Rectangle bounds = getCellBounds(dropRow, dropRow);
				if(bounds == null) {
					Insets i = getBorder().getBorderInsets(this);
					g.fillRect(0, getSize().height - i.bottom - 3, 2, 7);
					g.fillRect(2, getSize().height - i.bottom - 1 , getSize().width - 6, 3);
					
				}
				else {
					g.fillRect(0, bounds.y - 3, 2, 7);
					g.fillRect(2, bounds.y - 1, bounds.x + bounds.width - 6, 3);
				}
			}
		}
	}
	
	class ActionWrapper {
		GruntspudAction action;
		ToolListModel model;
		
		ActionWrapper(GruntspudAction action, ToolListModel model) {
			this.action = action;
			this.model = model;
		}
	}
	
	class ActionTransferable implements Transferable {

		ActionWrapper wrapper;
		
		ActionTransferable(ActionWrapper wrapper) {
			this.wrapper = wrapper;
		}

	// Transferable methods.
		public Object getTransferData(DataFlavor flavor) {
		if (flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType)) 
			return wrapper;
		else 
			return null;
		}
	
		public DataFlavor[] getTransferDataFlavors() { 
			return supportedFlavors; 
		}
		
		public boolean isDataFlavorSupported(DataFlavor flavor) { 
			return flavor.isMimeTypeEqual(DataFlavor.javaJVMLocalObjectMimeType); 
		}
	}
}
