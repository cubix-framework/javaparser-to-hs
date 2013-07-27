package foldTools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.gjt.sp.jedit.ServiceManager;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

@SuppressWarnings("serial")
public class CompositeHandlerDialog extends JDialog
{
	private static final String MESSAGE = "messages.foldTools.handlerDialog.";
	private JTextField name;
	private DefaultListModel model;
	private JList modes;
	private JButton ok, cancel;
	private boolean cancelled;

	private static String getProp(String prop)
	{
		return jEdit.getProperty(MESSAGE + prop);
	}
	public CompositeHandlerDialog(View parent)
	{
		super(parent, getProp("addHandlerDialog"), true);
		init();
	}

	public CompositeHandlerDialog(View parent, HandlerItem handler)
	{
		super(parent, getProp("editHandlerDialog"), true);
		init();
		initData(handler);
	}

	private void init()
	{
		setLayout(new BorderLayout());
		JPanel p = new JPanel();
		add(p, BorderLayout.NORTH);
		p.add(new JLabel(getProp("compositeName")));
		name = new JTextField(40);
		p.add(name);
		p = new JPanel();
		p.add(new JLabel(getProp("modes")));
		model = new DefaultListModel();
		initModel();
		modes = new JList(model);
		add(new JScrollPane(modes), BorderLayout.CENTER);
		p = new JPanel();
		add(p, BorderLayout.SOUTH);
		ok = new JButton(getProp("ok"));
		p.add(ok);
		cancel = new JButton(getProp("cancel"));
		p.add(cancel);
		pack();
		ActionListener al = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cancelled = (e.getSource() == cancel);
				if (! cancelled)
				{
					String n = name.getText();
					if (n == null || n.length() == 0)
					{
						JOptionPane.showMessageDialog(
							CompositeHandlerDialog.this, getProp("noName"));
						return;
					}
					if (modes.getSelectedIndices().length == 0)
					{
						JOptionPane.showMessageDialog(
								CompositeHandlerDialog.this, getProp("noModes"));
							return;
					}
				}
				setVisible(false);
			}
		};
		ok.addActionListener(al);
		cancel.addActionListener(al);
	}
	private void initModel()
	{
		String [] modes = ServiceManager.getServiceNames(Plugin.FOLD_HANDLER_SERVICE);
		for (String mode: modes)
			model.addElement(mode);
	}
	private void initData(HandlerItem handler)
	{
		name.setText(handler.name);
		name.select(0, name.getText().length());
		Vector<Integer> selection = new Vector<Integer>();
		for (String mode: handler.modes)
		{
			int i = model.indexOf(mode);
			if (i >= 0)
				selection.add(Integer.valueOf(i));
		}
		int [] indices = new int[selection.size()];
		for (int i = 0; i < selection.size(); i++)
			indices[i] = ((Integer)selection.get(i)).intValue(); 
		modes.setSelectedIndices(indices);
	}
	public boolean wasCancelled()
	{
		return cancelled;
	}
	public HandlerItem getHandler()
	{
		int [] sel = modes.getSelectedIndices();
		String [] modeNames = new String[sel.length];
		for (int i = 0; i < sel.length; i++)
			modeNames[i] = (String) model.get(sel[i]);
		return new HandlerItem(name.getText(), modeNames);
	}
}
