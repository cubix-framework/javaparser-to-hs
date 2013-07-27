/*
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009, 2011 Matthieu Casanova
 * Copyright (C) 2009, 2011 Shlomy Reinstein
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package gatchan.jedit.lucene;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

@SuppressWarnings("serial")
public class NewIndexDialog extends JDialog
{
	public static final String OPTION = "lucene.option.";
	public static final String MESSAGE = "lucene.message.";
	private static final String GEOMETRY = OPTION + "NewLuceneIndexDialog";
	private JTextField name;
	private JComboBox analyzer;
	private boolean accepted;

	private void saveGeometry()
	{
		GUIUtilities.saveGeometry(this, GEOMETRY);
	}

	public NewIndexDialog(Frame frame)
	{
		this(frame, null);
	}

	public NewIndexDialog(Frame frame, String initialName)
	{
		super(frame, jEdit.getProperty(MESSAGE + "NewIndexDialogTitle"), true);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent evt)
			{
				saveGeometry();
			}
		});
		setLayout(new GridLayout(0, 1));
		// Name panel
		JPanel p = new JPanel();
		add(p);
		p.add(new JLabel(jEdit.getProperty(MESSAGE + "IndexName")));
		name = new JTextField(30);
		p.add(name);
		if (initialName != null)
		{
			name.setText(initialName);
			name.setEditable(false);
		}
		// Analyzer panel
		p = new JPanel(new FlowLayout(FlowLayout.LEADING));
		add(p);
		p.add(new JLabel(jEdit.getProperty(MESSAGE + "Analyzer")));
		analyzer = new JComboBox(AnalyzerFactory.getAnalyzerNames());
		p.add(analyzer);
		String defaultAnalyzer = jEdit.getProperty(LucenePlugin.LUCENE_DEFAULT_ANALYZER);
		if (defaultAnalyzer != null)
			analyzer.setSelectedItem(defaultAnalyzer);
		// Button panel
		JPanel buttons = new JPanel();
		add(buttons);
		JButton ok = new JButton("Ok");
		buttons.add(ok);
		final JButton cancel = new JButton("Cancel");
		buttons.add(cancel);
		ok.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				saveGeometry();
				save();
				setVisible(false);
			}
		});
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setVisible(false);
			}
		});

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener cancelListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				cancel.doClick();
			}
		};
		rootPane.registerKeyboardAction(cancelListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		GUIUtilities.loadGeometry(this, GEOMETRY);
	}

	private void save()
	{
		accepted = true;
	}

	public String getIndexName()
	{
		return name.getText();
	}

	public String getIndexAnalyzer()
	{
		return analyzer.getSelectedItem().toString();
	}

	public boolean accepted()
	{
		return accepted;
	}
}
