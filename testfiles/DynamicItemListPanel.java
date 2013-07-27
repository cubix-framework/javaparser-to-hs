package org.frapuccino.dynamicitemlistpanel;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DynamicItemListPanel extends JPanel {

	private static final String SEPARATOR_CHAR = ",";

	private static final String TRAILING = "...";

	private int rows;

	private Vector vector = new Vector();

	private JComponent trailingItem;

	private boolean useSeparator;
	
	private boolean showLastSeparator = true;

	public DynamicItemListPanel(int rows) {
		this(rows, null, false);
	}

	public DynamicItemListPanel(int rows, boolean useSeparator) {
		this(rows, null, useSeparator);
	}

	public DynamicItemListPanel(int rows, JComponent trailingItem,
			boolean useSeparator) {
		super();

		if (rows <= 0)
			throw new IllegalArgumentException("max row number must be >=0");

		this.rows = rows;
		this.trailingItem = trailingItem;
		this.useSeparator = useSeparator;

		if (this.trailingItem == null) {
			this.trailingItem = new JLabel(TRAILING);
			this.trailingItem.setOpaque(false);
		}

		setLayout(new DynamicItemListLayout(rows, this.trailingItem));

	}


	public void setShowLastSeparator(boolean show) {
		showLastSeparator = show;
	}
	
	public void addItem(JComponent c) {
		vector.add(c);

		DynamicItemListLayout layout = ((DynamicItemListLayout) getLayout());

		if (useSeparator) {

			// add separator to last item
			JPanel lastItem = null;
			if ( showLastSeparator )
				lastItem = (JPanel) layout.getLastItem();
			else
				lastItem = (JPanel) layout.getSecondLastItem();
			
			if (lastItem != null) {
				JLabel separatorItem = new JLabel(SEPARATOR_CHAR);
				//separatorItem.setBackground(getBackground());
				lastItem.add(separatorItem);
			}

			JPanel panel = new JPanel();
			panel.setOpaque(false);
			panel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
			panel.add(c);

			layout.addItem(panel);
		} else {
			layout.addItem(c);
		}

	}

	public Dimension getPreferredSize() {
		DynamicItemListLayout layout = ((DynamicItemListLayout) getLayout());

		return new Dimension(layout.getPrefWidth(), layout.getPrefHeight());
	}

}
