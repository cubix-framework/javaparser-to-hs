/*
 * DefaultSessionPropertyPane.java
 * Copyright (c) 2001 Dirk Moebius
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


package sessions;


import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Mode;


public class DefaultSessionPropertyPane extends SessionPropertyPane
	implements ActionListener
{

	public DefaultSessionPropertyPane(Session session)
	{
		super(session);
	}


	public String getIdentifier()
	{
		return "plugin.sessions.sessionproperties.default";
	}


	public String getLabel()
	{
		return jEdit.getProperty("sessions.sessionproperties.default.label");
	}


	public void _init()
	{
		// "Base directory:"
		JPanel dirPanel = new JPanel();
		dirPanel.add(
			tBasedir = new JTextField(session.getProperty(Session.BASE_DIRECTORY, ""), 20));

		dirPanel.add(
			bBrowse = new JButton(UIManager.getIcon("FileView.directoryIcon")));
		bBrowse.setMargin(new java.awt.Insets(0,0,0,0));
		bBrowse.addActionListener(this);
		addComponent(dirPanel);

		// "Preferred mode:"
		cMode = new JComboBox(getModeNames());
		cMode.setEditable(false);
		cMode.setSelectedItem(session.getProperty("mode", jEdit.getProperty("buffer.defaultMode")));
		addComponent("sessions.sessionproperties.default.mode", cMode);
	}


	public void _save()
	{
		session.setProperty(Session.BASE_DIRECTORY, tBasedir.getText());
		session.setProperty("mode", cMode.getSelectedItem() != null
			? cMode.getSelectedItem().toString()
			: jEdit.getProperty("buffer.defaultMode"));
	}


	private String[] getModeNames()
	{
		Mode[] modes = jEdit.getModes();
		String[] modeNames = new String[modes.length];
		for(int i = 0; i < modes.length; ++i)
			modeNames[i] = modes[i].getName();
		return modeNames;
	}


	public void actionPerformed(ActionEvent evt)
	{
		JFileChooser chooser = new javax.swing.JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			tBasedir.setText(chooser.getSelectedFile().getPath());
	}


	private JTextField tBasedir;
	private JButton bBrowse;
	private JComboBox cMode;

}
