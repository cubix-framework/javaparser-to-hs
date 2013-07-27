package org.frapuccino.htmleditor.demo;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import org.frapuccino.DemoComponent;
import org.frapuccino.action.FJCheckBoxMenuItem;
import org.frapuccino.action.FJRadioButtonMenuItem;
import org.frapuccino.action.FJToggleButton;
import org.frapuccino.htmleditor.HtmlEditorController;

public class HtmlEditorDemo implements DemoComponent {

	private HtmlEditorController c;

	private JToolBar toolbar;

	private JMenuBar menubar;

	private JPanel panel;

	public HtmlEditorDemo() {
		c = new HtmlEditorController();

		toolbar = new JToolBar();
		toolbar.add(new FJToggleButton(c.getBoldFormatAction()));
		toolbar.add(new FJToggleButton(c.getItalicFormatAction()));
		toolbar.add(new FJToggleButton(c.getUnderlineFormatAction()));
		toolbar.add(new FJToggleButton(c.getStrikeoutFormatAction()));
		toolbar.add(new FJToggleButton(c.getTeletyperFormatAction()));
		toolbar.addSeparator();
		JToggleButton tb1 = new FJToggleButton(c.getLeftJustifyAction());
		toolbar.add(tb1);
		JToggleButton tb2 = new FJToggleButton(c.getCenterJustifyAction());
		toolbar.add(tb2);
		JToggleButton tb3 = new FJToggleButton(c.getRightJustifyAction());
		toolbar.add(tb3);
		ButtonGroup group1 = new ButtonGroup();
		group1.add(tb1);
		group1.add(tb2);
		group1.add(tb3);

		menubar = new JMenuBar();
		JMenu formatMenu = new JMenu("Format");
		formatMenu.add(new FJCheckBoxMenuItem(c.getBoldFormatAction()));
		formatMenu.add(new FJCheckBoxMenuItem(c.getItalicFormatAction()));
		formatMenu.add(new FJCheckBoxMenuItem(c.getUnderlineFormatAction()));
		formatMenu.add(new FJCheckBoxMenuItem(c.getStrikeoutFormatAction()));
		formatMenu.add(new FJCheckBoxMenuItem(c.getTeletyperFormatAction()));
		menubar.add(formatMenu);
		JMenu alignMenu = new JMenu("Alignment");
		JRadioButtonMenuItem m1 = new FJRadioButtonMenuItem(c
				.getLeftJustifyAction());
		alignMenu.add(m1);
		JRadioButtonMenuItem m2 = new FJRadioButtonMenuItem(c
				.getCenterJustifyAction());
		alignMenu.add(m2);
		JRadioButtonMenuItem m3 = new FJRadioButtonMenuItem(c
				.getRightJustifyAction());
		alignMenu.add(m3);
		ButtonGroup group2 = new ButtonGroup();
		group2.add(m1);
		group2.add(m2);
		group2.add(m3);

		menubar.add(alignMenu);

		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(menubar, BorderLayout.NORTH);
		topPanel.add(toolbar, BorderLayout.CENTER);
		panel.add(topPanel, BorderLayout.NORTH);
		panel.add(c.getView(), BorderLayout.CENTER);

	}

	public JComponent getComponent() {
		return panel;
	}

	public String getDemoName() {
		return "HTML Editor";
	}

	public String getDescription() {
		return "HTML Editor based on javax.swing.JTextPane";
	}

}
