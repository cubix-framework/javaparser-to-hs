package org.columba.core.gui.context;

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
import org.columba.core.context.ContextResultListenerAdapter;
import org.columba.core.context.api.IContextProvider;
import org.columba.core.context.api.IContextResultEvent;
import org.columba.core.context.api.IContextSearchManager;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

public class ContextResultBox extends JPanel {

	private final static Color borderColor2 = UIManager
			.getColor("controlShadow");

	private final static Color borderColor1 = UIManager
			.getColor("controlHighlight");

	private JXHyperlink link;

	private JXHyperlink moreLink;

	private JXCollapsiblePane collapsible;

	private IContextProvider provider;

	private IContextSearchManager contextSearchManager;

	public ContextResultBox(final IFrameMediator frameMediator,
			final IContextProvider provider,
			final IContextSearchManager contextSearchManager) {
		super();

		this.provider = provider;
		this.contextSearchManager = contextSearchManager;

		// get notified if search result arrived, to update view
		this.contextSearchManager
				.addResultListener(new MyContextResultListener());

		collapsible = new JXCollapsiblePane();
		// collapsible.getContentPane().setBackground(Color.WHITE);
		collapsible.add(provider.getView());
		collapsible.setCollapsed(true);

		Action toggleAction = collapsible.getActionMap().get(
				JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
				.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
				.getIcon("Tree.collapsedIcon"));
		link = new JXHyperlink(toggleAction);
		link.setText(provider.getName());
		link.setToolTipText(provider.getDescription());

		// link.setFont(link.getFont().deriveFont(Font.BOLD));
		link.setOpaque(true);
		// link.setBackground(titleBackground);
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
				provider.showMoreResults(frameMediator);
			}
		});

		setLayout(new BorderLayout());
		JPanel top = new JPanel();

		top.setBorder(new CompoundBorder(new SeparatorBorder(), BorderFactory
				.createEmptyBorder(2, 4, 2, 4)));

		top.setLayout(new BorderLayout());
		JLabel iconLabel = new JLabel();

		iconLabel.setIcon(provider.getIcon());

		iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 6));
		top.add(iconLabel, BorderLayout.WEST);
		top.add(link, BorderLayout.CENTER);
		top.add(moreLink, BorderLayout.EAST);
		add(top, BorderLayout.NORTH);
		add(collapsible, BorderLayout.CENTER);
	}

	public void showResults() {

		if (provider.isEnabledShowMoreLink()) {
			int count = provider.getTotalResultCount();
			moreLink.setEnabled(count > 0);

			if (count > 0) {
				moreLink.setText("Show More (" + count + ") ..");
			}
		}
	}

	class MyContextResultListener extends ContextResultListenerAdapter {

		@Override
		public void resultArrived(final IContextResultEvent event) {
			if (!event.getProviderName().equals(provider.getTechnicalName()))
				return;

			provider.showResult();
			showResults();

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
