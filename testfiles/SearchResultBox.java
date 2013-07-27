package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.search.api.IResultPanel;
import org.columba.core.search.ResultListenerAdapter;
import org.columba.core.search.api.IResultEvent;
import org.columba.core.search.api.ISearchCriteria;
import org.columba.core.search.api.ISearchManager;
import org.columba.core.search.api.ISearchProvider;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

public class SearchResultBox extends JPanel {

	private final static Color borderColor2 = UIManager
	.getColor("controlShadow");

private final static Color borderColor1 = UIManager
	.getColor("controlHighlight");

	private JXHyperlink link;

	private JXHyperlink moreLink;

	private JXCollapsiblePane collapsible;

	private IResultPanel resultPanel;

	private ISearchCriteria criteria;

	private ISearchProvider searchProvider;

	/**
	 *
	 * @param mediator
	 * @param p
	 * @param criteria
	 *            can be <code>null</code>
	 * @param resultPanel
	 */
	public SearchResultBox(final IFrameMediator mediator,
			final ISearchProvider p, final ISearchCriteria criteria,
			IResultPanel resultPanel) {
		this.resultPanel = resultPanel;
		this.criteria = criteria;
		this.searchProvider = p;

		collapsible = new JXCollapsiblePane();
		//collapsible.getContentPane().setBackground(Color.WHITE);
		collapsible.add(resultPanel.getView());

		Action toggleAction = collapsible.getActionMap().get(
				JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
				.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
				.getIcon("Tree.collapsedIcon"));
		link = new JXHyperlink(toggleAction);
		if (criteria != null)
			link.setText(criteria.getTitle());
		else
			link.setText(p.getName());

		if (criteria != null)
			link.setToolTipText(criteria.getDescription());
		else
			link.setToolTipText(p.getDescription());

		// link.setFont(link.getFont().deriveFont(Font.BOLD));
		link.setOpaque(true);
		//link.setBackground(titleBackground);
		link.setFocusPainted(false);

		link.setUnclickedColor(UIManager.getColor("Label.foreground"));
		link.setClickedColor(UIManager.getColor("Label.foreground"));

		moreLink = new JXHyperlink();
		moreLink.setEnabled(false);
		moreLink.setText("Show More ..");
		Font font = UIManager.getFont("Label.font");
		Font smallFont = new Font(font.getName(), font.getStyle(), font
				.getSize() - 2);
		moreLink.setFont(smallFont);
		moreLink.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ( criteria != null)
				p.showAllResults(mediator, "test", criteria.getTechnicalName());
				else
				p.showAllResults(mediator, "test", null);
			}
		});

		setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setOpaque(true);

		Border border1 = new CompoundBorder(new SeparatorBorder(),
				BorderFactory.createEmptyBorder(2, 4, 2, 4));

		Border border = new CompoundBorder(BorderFactory.createEmptyBorder(2,
				4, 2, 4), border1);

		top.setBorder(border1);
		//top.setBackground(titleBackground);
		top.setLayout(new BorderLayout());
		JLabel iconLabel = new JLabel();
		//iconLabel.setBackground(titleBackground);
		iconLabel.setIcon(p.getIcon());
		iconLabel.setOpaque(true);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 6));
		top.add(iconLabel, BorderLayout.WEST);
		top.add(link, BorderLayout.CENTER);
		top.add(moreLink, BorderLayout.EAST);
		add(top, BorderLayout.NORTH);
		add(collapsible, BorderLayout.CENTER);
	}

	public void installListener(ISearchManager searchManager) {
		searchManager.addResultListener(new MyResultListener());
	}

	class MyResultListener extends ResultListenerAdapter {
		@Override
		public void resultArrived(IResultEvent event) {
			if (event.getProviderName() == null)
				return;

			if (!event.getProviderName().equals(
					resultPanel.getProviderTechnicalName()))
				return;
			if (event.getSearchCriteria() != null) {
				if (!event.getSearchCriteria().getTechnicalName().equals(
						resultPanel.getSearchCriteriaTechnicalName()))
					return;
			}

			if (criteria != null)
				link.setText(criteria.getTitle());
			else
				link.setText(searchProvider.getName());

			if (criteria != null)
				link.setToolTipText(criteria.getDescription());
			else
				link.setToolTipText(searchProvider.getDescription());

			if (event.getTotalResultCount() == 0) {
				moreLink.setText("Show More ..");
				moreLink.setEnabled(false);
			} else {
				moreLink.setText("Show More" + " ("
						+ event.getTotalResultCount() + ") ..");
				moreLink.setEnabled(true);
			}

			revalidate();
		}

		MyResultListener() {
		}
	}

	/**
	 * The border between the stack components. It separates each component with
	 * a fine line border.
	 */
	class SeparatorBorder implements Border {

		boolean isFirst(Component c) {
			return c.getParent() == null || c.getParent().getComponent(0) == c;
		}

		public Insets getBorderInsets(Component c) {
			// if the collapsible is collapsed, we do not want its border to be
			// painted.
			if (c instanceof JXCollapsiblePane) {
				if (((JXCollapsiblePane) c).isCollapsed()) {
					return new Insets(0, 0, 0, 0);
				}
			}
			return new Insets(isFirst(c) ? 4 : 1, 0, 1, 0);
		}

		public boolean isBorderOpaque() {
			return true;
		}

		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			g.setColor(borderColor1);
			if (isFirst(c)) {
				g.drawLine(x, y + 2, x + width, y + 2);
			}
//			g.setColor(borderColor2);
//			g.drawLine(x, y + height - 1, x + width, y + height - 1);
		}
	}

}
