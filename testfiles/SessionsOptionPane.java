/*
 * SessionsOptionPane.java - plugin options pane for Sessions plugin
 * Copyright (c) 2000,2001 Dirk Moebius
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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.AbstractOptionPane;


/**
 * This is the option pane that jEdit displays for Session plugin's options.
 */
public class SessionsOptionPane extends AbstractOptionPane implements ActionListener
{

	private JCheckBox bAutoSave;
	private JCheckBox bAskSave;
	private JCheckBox bCloseAll;
	private JCheckBox bShowToolBar;
	private JRadioButton bShowBelowToolBar;
	private JRadioButton bShowJEditToolBar;
	private JRadioButton bShowInsideBufferList;
	private JCheckBox bShowTitle;
	private JCheckBox bChangeFSBDirectory;
	private JCheckBox bShowSessionNameInTitleBar;
	private JCheckBox bShowSessionPrefixInTitleBar;
	private SpinnerNumberModel maxSessionListSizeModel;
	private JSpinner  maxSessionListSize;


	public SessionsOptionPane()
	{
		super("sessions");
	}


	public void _init()
	{
		bAutoSave = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.autoSave"),
			jEdit.getBooleanProperty("sessions.switcher.autoSave", true)
		);
		bAutoSave.addActionListener(this);
		
		boolean askSave = jEdit.getBooleanProperty("sessions.switcher.askSave", false);
		bAskSave = new JCheckBox(jEdit.getProperty("options.sessions.switcher.askSave"), askSave);
		bAskSave.setEnabled(bAutoSave.isSelected());
		
		bCloseAll = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.closeAll"),
			jEdit.getBooleanProperty("sessions.switcher.closeAll", true)
		);

		bShowToolBar = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.showToolBar"),
			jEdit.getBooleanProperty("sessions.switcher.showToolBar", false)
		);
		bShowToolBar.addActionListener(this);

		boolean showJEditToolBar = jEdit.getBooleanProperty("sessions.switcher.showJEditToolBar", false);
		boolean showInsideBufferList = jEdit.getBooleanProperty("sessions.switcher.showInsideBufferList", false);
		boolean showBelowToolBar = !(showJEditToolBar || showInsideBufferList);

		bShowJEditToolBar = new JRadioButton(jEdit.getProperty("options.sessions.switcher.showJEditToolBar"), showJEditToolBar);
		bShowJEditToolBar.setEnabled(bShowToolBar.isSelected());

		bShowInsideBufferList = new JRadioButton(jEdit.getProperty("options.sessions.switcher.showInsideBufferList"), showInsideBufferList);
		bShowInsideBufferList.setEnabled(bShowToolBar.isSelected() && isBufferListAvailable());

		bShowBelowToolBar = new JRadioButton(jEdit.getProperty("options.sessions.switcher.showBelowToolBar"), showBelowToolBar);
		bShowBelowToolBar.setEnabled(bShowToolBar.isSelected());

		ButtonGroup group = new ButtonGroup();
		group.add(bShowJEditToolBar);
		group.add(bShowInsideBufferList);
		group.add(bShowBelowToolBar);

		bShowTitle = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.showTitle"),
			jEdit.getBooleanProperty("sessions.switcher.showTitle", true)
		);
		bShowTitle.setEnabled(bShowToolBar.isSelected());

		bChangeFSBDirectory = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.changeFSBDirectory"),
			jEdit.getBooleanProperty("sessions.switcher.changeFSBDirectory", false)
		);
		
		bShowSessionNameInTitleBar = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.showSessionNameInTitleBar"),
			jEdit.getBooleanProperty("sessions.switcher.showSessionNameInTitleBar", true)
		);
		bShowSessionNameInTitleBar.addActionListener(this);
		
		bShowSessionPrefixInTitleBar = new JCheckBox(
			jEdit.getProperty("options.sessions.switcher.showSessionPrefixInTitleBar"),
			jEdit.getBooleanProperty("sessions.switcher.showSessionPrefixInTitleBar", true)
		);
		
		// Max list size for session switcher combo
		Box maxSessListSizePanel = new Box(BoxLayout.X_AXIS);
		maxSessListSizePanel.add(new JLabel(
			jEdit.getProperty("options.sessions.switcher.maxListSize.label"))
		);
		Integer maxListSize = 
			jEdit.getIntegerProperty("options.sessions.switcher.maxListSize", 8);
		maxSessionListSizeModel = new SpinnerNumberModel(
			maxListSize,
			new Integer(5),
			new Integer(50), 
			new Integer(1)
		);
		maxSessionListSize = new JSpinner(maxSessionListSizeModel);
		maxSessionListSize.setMaximumSize(maxSessionListSize.getPreferredSize());
		maxSessListSizePanel.add(maxSessionListSize);
		maxSessListSizePanel.add(maxSessListSizePanel.createHorizontalGlue());
		
		addComponent(bAutoSave);
		addComponent("    ", bAskSave);
		addComponent(bCloseAll);
		addComponent(bShowToolBar);
		addComponent("    ", bShowBelowToolBar);
		addComponent("    ", bShowJEditToolBar);
		addComponent("    ", bShowInsideBufferList);
		addComponent("    ", bShowTitle);
		addComponent("    ", maxSessListSizePanel);
		addComponent(bChangeFSBDirectory);
		addComponent(bShowSessionNameInTitleBar);
		addComponent("    ", bShowSessionPrefixInTitleBar);
	}


	public void _save()
	{
		jEdit.setBooleanProperty("sessions.switcher.autoSave", bAutoSave.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.askSave", bAskSave.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.closeAll", bCloseAll.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showToolBar", bShowToolBar.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showJEditToolBar", bShowJEditToolBar.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showInsideBufferList", bShowInsideBufferList.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showTitle", bShowTitle.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.changeFSBDirectory", bChangeFSBDirectory.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showSessionNameInTitleBar", bShowSessionNameInTitleBar.isSelected());
		jEdit.setBooleanProperty("sessions.switcher.showSessionPrefixInTitleBar", bShowSessionPrefixInTitleBar.isSelected());
		jEdit.setIntegerProperty("options.sessions.switcher.maxListSize", (Integer)maxSessionListSizeModel.getNumber());
	}


	public void actionPerformed(ActionEvent e)
	{
		
		if (e.getSource() == bShowToolBar)
		{
			bShowBelowToolBar.setEnabled(bShowToolBar.isSelected());
			bShowJEditToolBar.setEnabled(bShowToolBar.isSelected());
			bShowInsideBufferList.setEnabled(bShowToolBar.isSelected() && isBufferListAvailable());
			bShowTitle.setEnabled(bShowToolBar.isSelected());
			
			bShowSessionPrefixInTitleBar.setEnabled(bShowSessionNameInTitleBar.isSelected());
		}
		else if (e.getSource() == bAutoSave)
		{
			bAskSave.setEnabled(bAutoSave.isSelected());
			bAskSave.setSelected(false);
		}
		
	}


	private boolean isBufferListAvailable()
	{
		// FIXME: is it sufficient to check only first view?!?
		return SessionsPlugin.isBufferListAvailable(jEdit.getFirstView());
	}
}
