package ctags.sidekick;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.GUIUtilities;

@SuppressWarnings("serial")
public class ObjectProcessorEditor extends JDialog {
	private static final String GEOMETRY = "tree.mapper.editor.geometry";
	private IObjectProcessor processor;
	private ObjectProcessorManager manager;
	private JComboBox processorList;
	private JTextArea descriptionTA;
	private AbstractObjectEditor editor;
	private JPanel editorWrapper;

	public ObjectProcessorEditor(JDialog parent, ObjectProcessorManager manager) {
		super(parent, manager.getProcessorTypeName() + " editor", true);
		loadGeometry();
		this.manager = manager;
		processor = null;
		addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
            	saveGeometry();	
            }
        });
		setLayout(new GridBagLayout());
		JPanel processorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = c.gridy = 0;
		add(processorPanel, c);
		processorPanel.add(new JLabel("Name:"));
		processorList = new JComboBox();
		processorPanel.add(processorList);
		processorList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				updateProcessorUI();
			}
		});
		JPanel descriptionPanel = new JPanel(new FlowLayout());
		descriptionPanel.setBorder(new TitledBorder("Description"));
		descriptionTA = new JTextArea(3, 60);
		descriptionTA.setLineWrap(true);
		descriptionTA.setWrapStyleWord(true);
		descriptionTA.setBackground(getBackground());
		descriptionPanel.add(descriptionTA);
		c.gridy = 1;
		c.gridheight = 3;
		add(descriptionPanel, c);
		
		editor = null;
		editorWrapper = new JPanel(new BorderLayout());
		c.gridy = 4;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		add(editorWrapper, c);

		Vector<String> names = manager.getProcessorNames();
		Collections.sort(names);
		for (int i = 0; i < names.size(); i++)
			processorList.addItem(names.get(i));
		
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close(true);
			}
		});
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close(false);
			}
		});
		buttons.add(ok);
		buttons.add(cancel);
		c.gridy = 5;
		c.fill = GridBagConstraints.NONE;
		add(buttons, c);
		pack();
		setVisible(true);
	}

	private void updateProcessorUI() {
		if (editor != null)
			editorWrapper.remove(editor);
		editor = null;
		String name = (String) processorList.getSelectedItem();
		processor = manager.getProcessor(name);
		if (processor != null) {
			descriptionTA.setText(processor.getDescription());
			processor = processor.getClone();
			editor = processor.getEditor();
			if (editor != null)
				editorWrapper.add(editor, BorderLayout.CENTER);
		}
		pack();
	}

	protected void close(boolean b) {
		if (b) {
			if (editor != null)
				if (editor.canClose())
					editor.save();
				else
					return;
		} else
			processor = null;
		saveGeometry();
		setVisible(false);
	}

	public IObjectProcessor getProcessor() {
		return processor;
	}
	private void loadGeometry() {
		GUIUtilities.loadGeometry(this, GEOMETRY);
	}
	private void saveGeometry() {
		GUIUtilities.saveGeometry(this, GEOMETRY);
	}

}
