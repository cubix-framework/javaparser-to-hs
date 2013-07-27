package org.columba.core.gui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.columba.core.gui.frame.api.IComponentBox;
import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

public class ComponentBoxContainer extends JPanel {

	private final static Color borderColor2 = UIManager.getColor("controlShadow");
	private final static Color borderColor1 = UIManager.getColor("controlHighlight");

	private JXHyperlink link;

	private JXCollapsiblePane collapsible;

	private IComponentBox compBox;

	public ComponentBoxContainer(IComponentBox compBox) {
		this.compBox = compBox;

		collapsible = new JXCollapsiblePane();
		collapsible.add(compBox.getView());
		collapsible.setCollapsed(true);

		Action toggleAction = collapsible.getActionMap().get(
				JXCollapsiblePane.TOGGLE_ACTION);
		// use the collapse/expand icons from the JTree UI
		toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager
				.getIcon("Tree.expandedIcon"));
		toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager
				.getIcon("Tree.collapsedIcon"));
		link = new JXHyperlink(toggleAction);

		link.setText(compBox.getName());
		link.setToolTipText(compBox.getDescription());

		link.setOpaque(true);
		link.setFocusPainted(false);

		link.setUnclickedColor(UIManager.getColor("Label.foreground"));
		link.setClickedColor(UIManager.getColor("Label.foreground"));

		Border border1 = new CompoundBorder(new SeparatorBorder(),
				BorderFactory.createEmptyBorder(2, 4, 2, 4));

//		Border border = new CompoundBorder(BorderFactory.createEmptyBorder(2,
//				4, 2, 4), border1);

		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(compBox.getIcon());
		//iconLabel.setOpaque(true);
		iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 6));

		JPanel top = new JPanel();
		top.setBorder(border1);
		top.setLayout(new BorderLayout());
		//top.setOpaque(true);

		top.add(iconLabel, BorderLayout.WEST);
		top.add(link, BorderLayout.CENTER);

		setLayout(new BorderLayout());

		add(top, BorderLayout.NORTH);
		add(collapsible, BorderLayout.CENTER);
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
