/*
 * JCompilerOptionDialog.java - options dialog for JCompiler option panes
 * Copyright (C) 2000,2001 Dirk Moebius
 *
 * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package jcompiler.options;


import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;


/**
 * An option dialog for JCompiler options.
 * @author Dirk Moebius
 */
public class JCompilerOptionDialog extends EnhancedDialog
	implements ActionListener
{

	public JCompilerOptionDialog(View view)
	{
		super(view, jEdit.getProperty("options.jcompiler.label"), true);

		view.showWaitCursor();

		paneGeneral = new JCompilerOptionPaneGeneral();
		paneGeneral.setBorder(new EmptyBorder(5,5,5,5));
		paneGeneral.init();

		paneCompiler = new JCompilerOptionPaneCompiler();
		paneCompiler.setBorder(new EmptyBorder(5,5,5,5));
		paneCompiler.init();

		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(5,8,8,8));
		content.setLayout(new BorderLayout());
		setContentPane(content);

		JTabbedPane stage = new JTabbedPane();
		stage.addTab(jEdit.getProperty("options." + paneCompiler.getName() + ".label"), paneCompiler);
		stage.addTab(jEdit.getProperty("options." + paneGeneral.getName() + ".label"), paneGeneral);
		content.add(stage, BorderLayout.CENTER);

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

		view.hideWaitCursor();
		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}


	//{{{ EnhancedDialog implementation
	public void ok()
	{
		ok(true);
	}


	public void cancel()
	{
		dispose();
	}
	//}}} end EnhancedDialog implementation


	public void ok(boolean dispose)
	{
		paneGeneral.save();
		paneCompiler.save();

		jEdit.propertiesChanged();
		jEdit.saveSettings();

		// get rid of this dialog if necessary
		if (dispose)
			dispose();
	}


	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();

		if (source == ok)
			ok();
		else if (source == cancel)
			cancel();
		else if (source == apply)
			ok(false);
	}


	private JButton ok;
	private JButton cancel;
	private JButton apply;
	private AbstractOptionPane paneGeneral;
	private AbstractOptionPane paneCompiler;
}

