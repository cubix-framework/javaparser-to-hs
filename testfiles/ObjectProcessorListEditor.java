package ctags.sidekick.options;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.RolloverButton;

import sidekick.ModeOptionPaneController;

import ctags.sidekick.IObjectProcessor;
import ctags.sidekick.ListObjectProcessor;
import ctags.sidekick.ObjectProcessorEditor;
import ctags.sidekick.ObjectProcessorManager;

@SuppressWarnings("serial")
public class ObjectProcessorListEditor extends JPanel
	implements ModeOptionPaneController.ModeOptionPaneDelegate {

	JList list;
	ObjectProcessorManager manager;
	DefaultListModel model;
	
	public ObjectProcessorListEditor(ObjectProcessorManager manager)
	{
		setLayout(new BorderLayout());
		this.manager = manager;
		String title = manager.getProcessorTypeName() + "s";
		TitledBorder b = new TitledBorder(title);
		
		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(b);
		add(topPanel);
		
		list = new JList();
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		topPanel.add(new JScrollPane(list), BorderLayout.CENTER);
		
		JButton add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				addProcessor();
			}
		});
		JButton remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
		remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				removeProcessor();
			}
		});
		JButton up = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
		up.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				moveProcessorUp();
			}
		});
		JButton down = new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
		down.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				moveProcessorDown();
			}
		});

		JPanel buttons = new JPanel(new GridLayout(1, 0));
		buttons.add(add);
		buttons.add(remove);
		buttons.add(up);
		buttons.add(down);
		topPanel.add(buttons, BorderLayout.SOUTH);
	}

	protected void addProcessor() {
		IObjectProcessor processor = new ObjectProcessorEditor(
				GUIUtilities.getParentDialog(this), manager).getProcessor();
		if (processor != null) {
			int index = list.getSelectedIndex();
			model.add(index + 1, processor);
			list.setSelectedIndex(index + 1);
		}
	}

	protected void removeProcessor() {
		int index = list.getSelectedIndex();
		if (index >= 0) {
			model.remove(index);
			if (index < model.size())
				list.setSelectedIndex(index);
		}
	}

	private void moveProcessorDown() {
		int index = list.getSelectedIndex();
		if (index < model.size() - 1)
			moveProcessor(index, index + 1);
	}
	private void moveProcessorUp() {
		int index = list.getSelectedIndex();
		if (index > 0)
			moveProcessor(index, index - 1);
	}
	private void moveProcessor(int from, int to) {
		IObjectProcessor current = (IObjectProcessor) model.get(from);
		IObjectProcessor other = (IObjectProcessor) model.get(to);
		model.set(to, current);
		model.set(from, other);
		list.setSelectedIndex(to);
	}

	public JComponent getUIComponent() {
		return this;
	}

	public Object createModeProps(String mode) {
		DefaultListModel model = new DefaultListModel();
		ListObjectProcessor processor = manager.getProcessorForMode(mode);
		Vector<IObjectProcessor> processors = processor.getProcessors();
		for (int i = 0; i < processors.size(); i++)
			model.addElement(processors.get(i));
		return model;
	}

	public void resetModeProps(String mode) {
		manager.resetProcessorForMode(mode);
	}

	public void saveModeProps(String mode, Object props) {
		DefaultListModel m = (DefaultListModel) props;
		ListObjectProcessor processor = manager.createProcessorForMode(mode);
		for (int i = 0; i < m.getSize(); i++)
			processor.add((IObjectProcessor) m.get(i));
		manager.setProcessorForMode(mode, processor);
	}

	public void updatePropsFromUI(Object props) {
		// Nothing to do, the model is connected directly to the table 
	}

	public void updateUIFromProps(Object props) {
		model = (DefaultListModel) props;
		list.setModel(model);
	}

	public boolean hasModeProps(String mode) {
		return manager.hasProcessorForMode(mode);
	}

}
