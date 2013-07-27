/**
 * PropsDialog.java - DAV properties dialog
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Taken from the FTP plugin, copyright (c) 2000 Slava Pestov
 * @author Slava Pestov
 * @author James Glaubiger
 * @version $$
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
package dav;

import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

public class PropsDialog extends EnhancedDialog implements ActionListener
{
	public PropsDialog(Component comp, String author, String authorEntry, String comment, String commentEntry)
	{
		super(JOptionPane.getFrameForComponent(comp),
			jEdit.getProperty("props.title"),true);
		
		//Set private fields
		this.author = author;
		this.authorEntry = authorEntry;
		this.comment = comment;
		this.commentEntry = commentEntry;
		
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(new EmptyBorder(12,12,12,0));
		setContentPane(content);

		JPanel panel = createFieldPanel(author,authorEntry,comment,commentEntry);
		content.add(panel,BorderLayout.NORTH);

		panel = new JPanel(new GridLayout(1,1));
		panel.setBorder(new EmptyBorder(6,0,0,12));

		content.add(panel,BorderLayout.CENTER);

		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.X_AXIS));
		panel.setBorder(new EmptyBorder(6,0,6,12));
		panel.add(Box.createGlue());
		ok = new JButton(jEdit.getProperty("common.ok"));
		ok.addActionListener(this);
		getRootPane().setDefaultButton(ok);
		panel.add(ok);
		panel.add(Box.createHorizontalStrut(6));
		cancel = new JButton(jEdit.getProperty("common.cancel"));
		cancel.addActionListener(this);
		panel.add(cancel);
		panel.add(Box.createGlue());

		content.add(panel,BorderLayout.SOUTH);

		pack();
		setLocationRelativeTo(comp);
		show();
	}

	// EnhancedDialog implementation
	public void ok()
	{
		commentField.requestFocus();

		//Append author if any was entered
		authorEntry = new String(authorEntryField.getText());
		
		if ( author.length() != 0 && authorEntry.length() != 0 ){
			author += "\n" + authorEntry;
		} else {
			author = authorEntry;
		}
		
		//Append comment if anything was entered
		commentEntry = new String(commentEntryField.getText());
		if ( commentEntry.length() != 0 )
			commentEntry = new Date() + "\n" + commentEntry;
		
		if ( comment.length() != 0 ){
			comment += "\n\n" + commentEntry;
		} else {
			comment = commentEntry;
		}
		
		isOK = true;
		dispose();
	}

	public void cancel()
	{
		dispose();
	}
	// end EnhancedDialog implementation

	public boolean isOK()
	{
		return isOK;
	}

	public String getAuthor()
	{
		return author;
	}

	public String getauthorEntry()
	{
		return authorEntry;
	}
	
	public String commentEntry()
	{
		return commentEntry;
	}

	public String getComment()
	{
		return comment;
	}

	public void actionPerformed(ActionEvent evt)
	{
		Object source = evt.getSource();
		if(source == ok)
			ok();
		else if(source == cancel)
			cancel();
	}

	// private members
	private JTextArea authorField;
	private JTextField authorEntryField;
	private JTextArea commentField;
	private JTextField commentEntryField;
	private JCheckBox passive;
	private String author;
	private String authorEntry;
	private String commentEntry;
	private String comment;
	private boolean isOK;
	private JButton ok;
	private JButton cancel;

	private JPanel createFieldPanel(String author, String authorEntry, String comment, String commentEntry)
	{
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.insets = new Insets(0,0,6,12);
		cons.gridwidth = cons.gridheight = 1;
		cons.gridx = cons.gridy = 0;
		cons.fill = GridBagConstraints.BOTH;
		
		//author label
		JLabel label = new JLabel(jEdit.getProperty("props.author"),
			SwingConstants.RIGHT);
		layout.setConstraints(label,cons);
		panel.add(label);
		//author field
		authorField = new JTextArea(author,10,25);
		if(author != null)
			authorField.setEnabled(false);
		cons.gridx = 1;
		cons.weightx = 1.0f;
		layout.setConstraints(authorField,cons);
		authorField.setEditable(false);
		panel.add(authorField);
		//author entry label
		label = new JLabel(jEdit.getProperty("props.authorEntry"),
			SwingConstants.RIGHT);
		cons.gridx = 0;
		cons.weightx = 0.0f;
		cons.gridy = 1;
		layout.setConstraints(label,cons);
		panel.add(label);
		//author entry field
		authorEntryField = new JTextField(authorEntry,70);
		cons.gridx = 1;
		cons.weightx = 1.0f;
		layout.setConstraints(authorEntryField,cons);
		panel.add(authorEntryField);
		//comment label
		label = new JLabel(jEdit.getProperty("props.comment"),
			SwingConstants.RIGHT);
		cons.gridx = 0;
		cons.weightx = 0.0f;
		cons.gridy = 2;
		layout.setConstraints(label,cons);
		panel.add(label);
		//comment field
		commentField = new JTextArea(comment,10,25);
		if(comment != null)
			commentField.setEnabled(false);
		cons.gridx = 1;
		cons.weightx = 1.0f;
		layout.setConstraints(commentField,cons);
		panel.add(commentField);
		//comment entry label
		label = new JLabel(jEdit.getProperty("props.commentEntry"),
			SwingConstants.RIGHT);
		cons.gridx = 0;
		cons.weightx = 0.0f;
		cons.gridy = 3;
		layout.setConstraints(label,cons);
		commentField.setEditable(false);
		panel.add(label);
		//comment entry field
		commentEntryField = new JTextField(commentEntry,70);
		cons.gridx = 1;
		cons.weightx = 1.0f;
		layout.setConstraints(commentEntryField,cons);
		panel.add(commentEntryField);

		return panel;
	}
}
