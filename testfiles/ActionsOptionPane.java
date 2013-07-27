package ctagsinterface.options;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.EditAction;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.RolloverButton;

import ctagsinterface.main.CtagsInterfacePlugin;
import ctagsinterface.main.QueryAction;

@SuppressWarnings("serial")
public class ActionsOptionPane extends AbstractOptionPane {

	static public final String OPTION = CtagsInterfacePlugin.OPTION;
	static public final String MESSAGE = CtagsInterfacePlugin.MESSAGE;
	static public final String ACTIONS = OPTION + "actions.";
	JList actions;
	DefaultListModel actionsModel;
	
	public ActionsOptionPane() {
		super("CtagsInterface-Actions");
		setBorder(new EmptyBorder(5, 5, 5, 5));
		
		actionsModel = new DefaultListModel();
		QueryAction[] queries = loadActions();
		for (int i = 0; i < queries.length; i++)
			actionsModel.addElement(queries[i]);
		actions = new JList(actionsModel);
		JScrollPane scroller = new JScrollPane(actions);
		scroller.setBorder(BorderFactory.createTitledBorder(
				jEdit.getProperty(MESSAGE + "actions")));
		addComponent(scroller, GridBagConstraints.HORIZONTAL);
		JPanel buttons = new JPanel();
		JButton add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		buttons.add(add);
		JButton remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		buttons.add(remove);
		JButton edit = new JButton("Edit");
		buttons.add(edit);
		addComponent(buttons);

		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				EditAction action = new ActionEditor().getAction();
				if (action != null)
					actionsModel.addElement(action);
			}
		});
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int i = actions.getSelectedIndex();
				if (i >= 0)
					actionsModel.removeElementAt(i);
			}
		});
		edit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				int i = actions.getSelectedIndex();
				if (i < 0)
					return;
				QueryAction action = (QueryAction) actionsModel.getElementAt(i);
				action = new ActionEditor(action).getAction();
				if (action != null)
					actionsModel.setElementAt(action, i);
			}
		});
	}

	static public QueryAction[] loadActions() {
		int n = jEdit.getIntegerProperty(ACTIONS + "size", 0);
		QueryAction[] actionArr = new QueryAction[n];
		for (int i = 0; i < n; i++)
			actionArr[i] = new QueryAction(i);
		return actionArr;
	}
	
	public void save() {
		jEdit.setIntegerProperty(ACTIONS + "size", actionsModel.size());
		for (int i = 0; i < actionsModel.size(); i++) {
			QueryAction qa = (QueryAction) actionsModel.getElementAt(i);
			qa.save(i);
		}
		CtagsInterfacePlugin.updateActions();
	}

	public class ActionEditor extends JDialog {
		
		QueryAction action;
		JTextField query;
		JTextField name;
		JButton ok;
		JButton cancel;
		
		public ActionEditor(QueryAction qa) {
			super(jEdit.getActiveView(), jEdit.getProperty(MESSAGE + "actionEditorTitle"),
				true);
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			JPanel p = new JPanel();
			p.add(new JLabel(jEdit.getProperty(MESSAGE + "actionName")));
			name = new JTextField(30);
			p.add(name);
			p.setAlignmentX(LEFT_ALIGNMENT);
			c.anchor = GridBagConstraints.NORTHWEST;
			c.gridx = c.gridy = 0;
			c.gridwidth = c.gridheight = 1;
			add(p, c);
			p = new JPanel();
			p.add(new JLabel(jEdit.getProperty(MESSAGE + "sqlQuery")));
			query = new JTextField(60);
			p.add(query);
			p.setAlignmentX(LEFT_ALIGNMENT);
			c.gridy++;
			add(p, c);
			p = new JPanel();
			JButton ok = new JButton("Ok");
			p.add(ok);
			JButton cancel = new JButton("Cancel");
			p.add(cancel);
			p.setAlignmentX(LEFT_ALIGNMENT);
			c.gridy++;
			add(p, c);
			
			ok.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					action = new QueryAction(name.getText(), query.getText());
					dispose();
				}
			});
			cancel.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					dispose();
				}
			});
			action = qa;
			if (action != null) {
				name.setText(action.getName());
				query.setText(action.getQuery());
			}
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}

		public ActionEditor() {
			this(null);
		}
		
		public QueryAction getAction() {
			return action;
		}
	}

}
