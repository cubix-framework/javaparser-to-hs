package org.columba.addressbook.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import org.columba.addressbook.facade.DialogFacade;
import org.columba.api.exception.ServiceNotFoundException;
import org.columba.contact.search.ContactSearchResult;
import org.columba.core.facade.ServiceFacadeRegistry;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.search.api.ISearchResult;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

public class SearchResultList extends JXList {

	private DefaultListModel listModel;
	private JPopupMenu contextMenu;
	
	public SearchResultList() {
		super();

		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new MyListCellRenderer());

		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { new RolloverHighlighter(new Color(248, 248,
						248), Color.white) }));
		setRolloverEnabled(true);

		addMouseListener(new DoubleClickListener() {
			@Override
			public void doubleClick(MouseEvent event) {
				ISearchResult selected = (ISearchResult) getSelectedValue();
				new DialogFacade().openContactDialog(selected.getLocation());
			}
		});

		add(getPopupMenu());
		addMouseListener(new MyMouseListener());
	}

	private JPopupMenu getPopupMenu() {
		if (contextMenu != null)
			return contextMenu;

		contextMenu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Open..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ISearchResult selected = (ISearchResult)getSelectedValue();
				new DialogFacade().openContactDialog(selected.getLocation());
			}
		});
		contextMenu.add(item);

		item = new JMenuItem("Compose Message..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ContactSearchResult selected = (ContactSearchResult) getSelectedValue();
				
				String address = selected.getDescription();

				try {
					org.columba.mail.facade.IDialogFacade facade = (org.columba.mail.facade.IDialogFacade) ServiceFacadeRegistry
							.getInstance()
							.getService(
									org.columba.mail.facade.IDialogFacade.class);
					facade.openComposer(address);
				} catch (ServiceNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		contextMenu.add(item);
		return contextMenu;
	}
	
	public void addAll(List<ISearchResult> result) {
		Iterator<ISearchResult> it = result.iterator();
		while (it.hasNext()) {
			listModel.addElement(it.next());
		}
	}

	public void add(ISearchResult result) {
		listModel.addElement(result);
	}

	public void clear() {
		listModel.clear();
	}


	class MyMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			handleEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handlePopupEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handlePopupEvent(e);
		}

		/**
		 * @param e
		 */
		private void handlePopupEvent(MouseEvent e) {
			Point p = e.getPoint();
			if (e.isPopupTrigger()) {
				// check if a single entry is selected
				if (getSelectedIndices().length <= 1) {
					// select new item
					int index = locationToIndex(p);
					setSelectedIndex(index);
				}
				// show context menu
				getPopupMenu().show(e.getComponent(), p.x, p.y);
			}
		}

		/**
		 * @param e
		 */
		private void handleEvent(MouseEvent e) {
		}
	}
	
	class MyListCellRenderer extends JPanel implements ListCellRenderer {

		private JLabel iconLabel = new JLabel();

		private JLabel titleLabel = new JLabel();

		private JXHyperlink descriptionLabel = new JXHyperlink();

		private JPanel centerPanel;

		private Border lineBorder = new HeaderSeparatorBorder(new Color(230,
				230, 230));

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());

			centerPanel.add(titleLabel, BorderLayout.NORTH);
			centerPanel.add(descriptionLabel, BorderLayout.CENTER);
			add(iconLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);

			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 8));

			centerPanel.setOpaque(false);
			setOpaque(true);

		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				 setBackground(list.getSelectionBackground());
				 setForeground(list.getSelectionForeground());

			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());

			}

			ISearchResult result = (ISearchResult) value;

			titleLabel.setText(result.getTitle());
			iconLabel.setIcon(ImageLoader.getSmallIcon(IconKeys.USER));
			descriptionLabel.setText(result.getDescription());

			return this;
		}

	}

	class HeaderSeparatorBorder extends AbstractBorder {

		protected Color color;

		public HeaderSeparatorBorder(Color color) {
			super();

			this.color = color;
		}

		/**
		 * Paints the border for the specified component with the specified
		 * position and size.
		 * 
		 * @param c
		 *            the component for which this border is being painted
		 * @param g
		 *            the paint graphics
		 * @param x
		 *            the x position of the painted border
		 * @param y
		 *            the y position of the painted border
		 * @param width
		 *            the width of the painted border
		 * @param height
		 *            the height of the painted border
		 */
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			Color oldColor = g.getColor();
			g.setColor(color);
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);

			g.setColor(oldColor);
		}

		/**
		 * Returns the insets of the border.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 */
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 1, 0);
		}

		/**
		 * Reinitialize the insets parameter with this Border's current Insets.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 * @param insets
		 *            the object to be reinitialized
		 */
		public Insets getBorderInsets(Component c, Insets insets) {
			insets.left = insets.top = insets.right = insets.bottom = 1;
			return insets;
		}

	}
}
