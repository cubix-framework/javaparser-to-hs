/* :folding=explicit:collapseFolds=1: */

/*
 * $Id: PasteDialog.java 11523 2004-11-07 21:54:45Z spestov $
 *
 * Copyright (C) 2004 Slava Pestov.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * DEVELOPERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package lisppaste;

import javax.swing.border.EmptyBorder;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.List;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

public class PasteDialog extends EnhancedDialog
{
	//{{{ PasteDialog constructor
	public PasteDialog(View view, String paste)
	{
		super(view,jEdit.getProperty("lisp-paste.dialog-title"),true);

		this.view = view;

		JPanel content = new JPanel(new BorderLayout(12,0));
		content.setBorder(new EmptyBorder(12,12,12,12));
		setContentPane(content);

		JPanel fields = new JPanel(new GridBagLayout());

		GridBagConstraints labelC = new GridBagConstraints();
		labelC.fill = GridBagConstraints.HORIZONTAL;
		labelC.gridx = 0;
		labelC.gridy = 0;
		labelC.insets = new Insets(0,0,12,12);

		GridBagConstraints fieldC = new GridBagConstraints();
		fieldC.fill = GridBagConstraints.HORIZONTAL;
		fieldC.gridx = 1;
		fieldC.gridy = 0;
		fieldC.insets = new Insets(0,0,12,12);
		fieldC.weighty = 0.0f;

		fields.add(new JLabel(jEdit.getProperty(
			"lisp-paste.channel")),labelC);
		channel = createChannelList();
		fields.add(channel,fieldC);

		labelC.gridx = 2;
		reloadChannels = new RolloverButton(GUIUtilities.loadIcon("Reload.png"));
		reloadChannels.setToolTipText(jEdit.getProperty(
			"lisp-paste.reload-channels"));
		reloadChannels.addActionListener(new ActionHandler());
		fields.add(reloadChannels,labelC);

		labelC.gridx = 0;
		labelC.gridy++;
		fieldC.gridy++;

		fields.add(new JLabel(jEdit.getProperty(
			"lisp-paste.user")),labelC);
		user = new JTextField(15);

		String userName = jEdit.getProperty("lisp-paste.user.value");
		if(userName == null)
			userName = System.getProperty("user.name");
		user.setText(userName);

		fields.add(user,fieldC);

		labelC.gridy++;
		fieldC.gridy++;

		fields.add(new JLabel(jEdit.getProperty(
			"lisp-paste.title")),labelC);
		title = new JTextField();
		fieldC.weightx = 1.0f;
		fieldC.gridwidth = 3;
		fieldC.insets = new Insets(0,0,12,0);
		fields.add(title,fieldC);

		content.add(BorderLayout.NORTH,fields);

		contents = new JTextArea(10,60);
		contents.setText(paste);
		content.add(BorderLayout.CENTER,new JScrollPane(contents));

		Box buttons = new Box(BoxLayout.X_AXIS);
		buttons.setBorder(new EmptyBorder(12,0,0,0));
		buttons.add(Box.createGlue());

		ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(new ActionHandler());
		getRootPane().setDefaultButton(ok);

		buttons.add(ok);
		buttons.add(Box.createHorizontalStrut(12));

		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(new ActionHandler());

		buttons.add(cancel);
		buttons.add(Box.createGlue());

		content.add(BorderLayout.SOUTH,buttons);

		pack();
		setLocationRelativeTo(view);
		show();
	} //}}}
	
	//{{{ ok() method
	public void ok()
	{
		String u = user.getText();
		String t = title.getText();
		String c = contents.getText();
		if(u.length() == 0 || t.length() == 0 || c.length() == 0)
		{
			getToolkit().beep();
			return;
		}

		save();
		dispose();
		VFSManager.runInWorkThread(new LispPasteRequest(view,
			(String)channel.getSelectedItem(),u,t,c));
	} //}}}
	
	//{{{ cancel() method
	public void cancel()
	{
		save();
		dispose();
	} //}}}
	
	//{{{ Private members
	private View view;
	private JComboBox channel;
	private RolloverButton reloadChannels;
	private JTextField user;
	private JTextField title;
	private JTextArea contents;
	private JButton ok, cancel;
	
	//{{{ createChannelList() method
	private JComboBox createChannelList()
	{
		JComboBox channel = new JComboBox();
		channel.setEditable(true);

		String list = jEdit.getProperty("lisp-paste.channels");
		StringTokenizer st = new StringTokenizer(list);
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		while(st.hasMoreTokens())
			model.addElement(st.nextToken());

		channel.setModel(model);

		return channel;
	} //}}}

	//{{{ save() method
	private void save()
	{
		jEdit.setProperty("lisp-paste.user.value",user.getText());

		ComboBoxModel model = channel.getModel();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < model.getSize(); i++)
		{
			if(i != 0)
				buf.append(' ');
			buf.append((String)model.getElementAt(i));
		}
		
		jEdit.setProperty("lisp-paste.channels",buf.toString());
	} //}}}

	//{{{ reloadChannels() method
	private void reloadChannels()
	{
		reloadChannels.setEnabled(false);

		final ChannelListRequest channelListRequest = new ChannelListRequest(view);
		VFSManager.runInWorkThread(channelListRequest);
		VFSManager.runInAWTThread(new Runnable()
		{
			public void run()
			{
				setChannelList(channelListRequest.getChannelList());
				reloadChannels.setEnabled(true);
			}
		});
	} //}}}

	//{{{ setChannelList() method
	private void setChannelList(List list)
	{
		if(list == null)
			return;

		String chan = (String)channel.getSelectedItem();
		channel.setModel(new DefaultComboBoxModel(list.toArray()));
		channel.setSelectedItem(chan);
	} //}}}

	//}}}
	
	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent evt)
		{
			Object source = evt.getSource();
			if(source == ok)
				ok();
			else if(source == cancel)
				cancel();
			else if(source == reloadChannels)
				reloadChannels();
		}
	} //}}}
}
