package org.columba.calendar.ui.box;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.resourceloader.IconKeys;
import org.columba.calendar.resourceloader.ResourceLoader;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

public class CalendarList extends JXList {

	public CalendarList() {
		super();

		setCellRenderer(new MyListCellRenderer());

		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { new RolloverHighlighter(new Color(248, 248,
						248), Color.white) }));
		setRolloverEnabled(true);

	}

	public void addAll(List<IEventInfo> list) {
		Iterator<IEventInfo> it = list.iterator();
		while (it.hasNext()) {
			addElement(it.next());
		}
	}

	public void add(IEventInfo result) {
		addElement(result);
	}

	/**
	 * ********************** filtering
	 * *********************************************
	 */

	/**
	 * Associates filtering document listener to text component.
	 */

	public void installJTextField(JTextField input) {
		if (input != null) {
			FilteringModel model = (FilteringModel) getModel();
			input.getDocument().addDocumentListener(model);
		}
	}

	/**
	 * Disassociates filtering document listener from text component.
	 */

	public void uninstallJTextField(JTextField input) {
		if (input != null) {
			FilteringModel model = (FilteringModel) getModel();
			input.getDocument().removeDocumentListener(model);
		}
	}

	/**
	 * Doesn't let model change to non-filtering variety
	 */

	public void setModel(ListModel model) {
		if (!(model instanceof FilteringModel)) {
			throw new IllegalArgumentException();
		} else {
			super.setModel(model);
		}
	}

	/**
	 * Adds item to model of list
	 */
	public void addElement(IEventInfo element) {
		((FilteringModel) getModel()).addElement(element);
	}

	class MyListCellRenderer extends JPanel implements ListCellRenderer {

		private JLabel iconLabel = new JLabel();

		private JLabel titleLabel = new JLabel();

		private JLabel descriptionLabel = new JLabel();

		private JPanel centerPanel;

		private JLabel startDateLabel = new JLabel();

		private Border lineBorder = new HeaderSeparatorBorder(new Color(230,
				230, 230));

		private DateFormat df = DateFormat.getDateTimeInstance();

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());

			JPanel titlePanel = new JPanel();
			titlePanel.setLayout(new BorderLayout());
			titlePanel.add(titleLabel, BorderLayout.WEST);
			titlePanel.add(startDateLabel, BorderLayout.EAST);

			centerPanel.add(titlePanel, BorderLayout.NORTH);
			centerPanel.add(descriptionLabel, BorderLayout.CENTER);
			centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
			add(iconLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);

			descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(
					Font.ITALIC));

			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 8));

			centerPanel.setOpaque(false);
			titlePanel.setOpaque(false);
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

			IEventInfo result = (IEventInfo) value;

			titleLabel.setText(result.getEvent().getSummary());
			iconLabel.setIcon(ResourceLoader
					.getSmallIcon(IconKeys.NEW_APPOINTMENT));
			if (result.getEvent().getLocation() != null
					&& result.getEvent().getLocation().length() > 0)
				descriptionLabel.setText(result.getEvent().getLocation());
			else
				descriptionLabel.setText("no Location specified");

			startDateLabel.setText(df.format(result.getEvent().getDtStart().getTime()));
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
