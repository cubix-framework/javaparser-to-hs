package ctagsinterface.dialogs;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

import ctagsinterface.db.TagDB;
import ctagsinterface.main.CtagsInterfacePlugin;

@SuppressWarnings("serial")
public class ChangeDbSettings extends JDialog {
	
	static public final String OPTION = CtagsInterfacePlugin.OPTION;
	static public final String MESSAGE = CtagsInterfacePlugin.MESSAGE;
	static private String DIALOG_GEOMETRY =
		OPTION + "dbSettingsDialogGeometry";
	JComboBox dbPreset;
	JTextField dbClass;
	JTextField dbConnection;
	JTextField dbUser;
	JTextField dbPassword;
	JTextField dbOnExit;
	JTextField dbMappingsFile;
	JCheckBox rebuildNewDb;
	
	private void saveGeometry() {
		GUIUtilities.saveGeometry(this, DIALOG_GEOMETRY);
	} 
	
	public ChangeDbSettings(Frame frame) {
		super(frame, jEdit.getProperty(
			MESSAGE + "changeDbSettingsDialogTitle"), true);
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				saveGeometry();	
			}
		});
		setLayout(new GridBagLayout());
		
		JPanel dbPanel = new JPanel();
		dbPanel.setLayout(new GridBagLayout());
		dbPanel.setBorder(new TitledBorder(jEdit.getProperty(
			MESSAGE + "dbTitle")));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = c.gridy = 0;
		JPanel dbPresetPanel = new JPanel();
		dbPresetPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbPreset")));
		Vector<String> presets = new Vector<String>();
		presets.add(TagDB.CUSTOM_DB);
		String presetStr = jEdit.getProperty(TagDB.DB_PRESETS);
		if ((presetStr != null) && (! presetStr.isEmpty())) {
			String [] items = presetStr.split(",");
			for (String item: items)
				presets.add(item);
		}
		dbPreset = new JComboBox(presets);
		dbPresetPanel.add(dbPreset);
		JButton savePreset = new JButton("Save");
		dbPresetPanel.add(savePreset);
		JButton savePresetAs = new JButton("Save as...");
		dbPresetPanel.add(savePresetAs);
		final JButton removePreset = new JButton("Remove");
		dbPresetPanel.add(removePreset);
		dbPanel.add(dbPresetPanel, c);
		JPanel dbClassPanel = new JPanel();
		dbClassPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbClass")));
		dbClass = new JTextField(jEdit.getProperty(TagDB.DB_CLASS), 40);
		dbClassPanel.add(dbClass);
		c.gridy++;
		dbPanel.add(dbClassPanel, c);
		JPanel dbConnectionPanel = new JPanel();
		dbConnectionPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbConnection")));
		dbConnection = new JTextField(jEdit.getProperty(TagDB.DB_CONNECTION), 40);
		dbConnectionPanel.add(dbConnection);
		c.gridy++;
		dbPanel.add(dbConnectionPanel, c);
		JPanel dbUserPanel = new JPanel();
		dbUserPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbUser")));
		dbUser = new JTextField(jEdit.getProperty(TagDB.DB_USER), 20);
		dbUserPanel.add(dbUser);
		c.gridy++;
		dbPanel.add(dbUserPanel, c);
		JPanel dbPasswordPanel = new JPanel();
		dbPasswordPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbPassword")));
		dbPassword = new JTextField(jEdit.getProperty(TagDB.DB_PASSWORD), 20);
		dbPasswordPanel.add(dbPassword);
		c.gridy++;
		dbPanel.add(dbPasswordPanel, c);
		JPanel dbOnExitPanel = new JPanel();
		dbOnExitPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbOnExit")));
		dbOnExit = new JTextField(jEdit.getProperty(TagDB.DB_ON_EXIT), 20);
		dbOnExitPanel.add(dbOnExit);
		c.gridy++;
		dbPanel.add(dbOnExitPanel, c);
		JPanel dbMappingsPanel = new JPanel();
		dbMappingsPanel.add(new JLabel(jEdit.getProperty(MESSAGE + "dbMappingsFile")));
		dbMappingsFile = new JTextField(jEdit.getProperty(TagDB.DB_MAPPINGS_FILE), 20);
		dbMappingsPanel.add(dbMappingsFile);
		JButton dbMappingsFileBrowse = new JButton("...");
		dbMappingsPanel.add(dbMappingsFileBrowse);
		c.gridy++;
		dbPanel.add(dbMappingsPanel, c);
		rebuildNewDb = new JCheckBox(
			jEdit.getProperty(MESSAGE + "rebuildNewDb"), false);
		c.gridy++;
		dbPanel.add(rebuildNewDb, c);
		c = new GridBagConstraints();
		c.insets = new Insets(5,5,5,5);
		c.gridx = c.gridy = 0;
		add(dbPanel);
		JPanel buttons = new JPanel();
		JButton ok = new JButton("Ok");
		buttons.add(ok);
		final JButton cancel = new JButton("Cancel");
		buttons.add(cancel);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.gridy++;
		add(buttons, c);

		dbPreset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String preset = (String) dbPreset.getSelectedItem();
				removePreset.setEnabled(! preset.equals(TagDB.CUSTOM_DB));
				dbClass.setText(TagDB.getDbPropertyByPreset(TagDB.DB_CLASS, preset));
				dbConnection.setText(TagDB.getDbPropertyByPreset(TagDB.DB_CONNECTION, preset));
				dbUser.setText(TagDB.getDbPropertyByPreset(TagDB.DB_USER, preset));
				dbPassword.setText(TagDB.getDbPropertyByPreset(TagDB.DB_PASSWORD, preset));
				dbOnExit.setText(TagDB.getDbPropertyByPreset(TagDB.DB_ON_EXIT, preset));
				dbMappingsFile.setText(TagDB.getDbPropertyByPreset(TagDB.DB_MAPPINGS_FILE, preset));
			}
		});
		savePreset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePreset((String) dbPreset.getSelectedItem(), false);
			}			
		});
		savePresetAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String preset = JOptionPane.showInputDialog("DB preset name:");
				if ((preset == null) || preset.isEmpty() || (! preset.matches("\\w+")))
					return;
				savePreset(preset, true);
				dbPreset.setSelectedItem(preset);
			}
		});
		removePreset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = dbPreset.getSelectedIndex();
				dbPreset.removeItemAt(index);
				savePresetList();
				if (index == dbPreset.getItemCount())
					index--;
				dbPreset.setSelectedIndex(index);
			}
		});
		String selected = jEdit.getProperty(TagDB.DB_SELECTED_PRESET);
		if (! presets.contains(selected))
			selected = TagDB.CUSTOM_DB;
		dbPreset.setSelectedItem(selected);
		
		dbMappingsFileBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(dbMappingsFile.getText());
				int ret = fc.showOpenDialog(ChangeDbSettings.this);
				if (ret != JFileChooser.APPROVE_OPTION)
					return;
				dbMappingsFile.setText(fc.getSelectedFile().getAbsolutePath());
			}
		});
		
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveGeometry();
				save();
				setVisible(false);
			}
		});
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener cancelListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel.doClick();
			}
		};
		rootPane.registerKeyboardAction(cancelListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		pack();
		GUIUtilities.loadGeometry(this, DIALOG_GEOMETRY);
	}
	
	private void save() {
		String selectedPreset = (String) dbPreset.getSelectedItem();
		jEdit.setProperty(TagDB.DB_SELECTED_PRESET, selectedPreset);
		savePreset(selectedPreset, false);
		// Make the new settings take effect immediately.
		CtagsInterfacePlugin.switchDatabase(rebuildNewDb.isSelected());
	}
	
	private void savePresetList() {
		StringBuffer presets = new StringBuffer();
		for (int i = 0; i < dbPreset.getItemCount(); i++) {
			String preset = (String) dbPreset.getItemAt(i);
			if (preset.equals(TagDB.CUSTOM_DB))
				continue;
			if (presets.length() > 0)
				presets.append(",");
			presets.append(preset);
		}
		jEdit.setProperty(TagDB.DB_PRESETS, presets.toString());
	}
	
	private void savePreset(String preset, boolean checkNew) {
		if (checkNew) {
			int i;
			for (i = 0; i < dbPreset.getItemCount(); i++) {
				if (dbPreset.getItemAt(i).equals(preset))
					break;
			}
			if (i == dbPreset.getItemCount()) {
				// New preset!
				((DefaultComboBoxModel)dbPreset.getModel()).insertElementAt(preset, 1);
				savePresetList();
			}
		}
		TagDB.setDbPropertyByPreset(TagDB.DB_CLASS, preset, dbClass.getText());
		TagDB.setDbPropertyByPreset(TagDB.DB_CONNECTION, preset, dbConnection.getText());
		TagDB.setDbPropertyByPreset(TagDB.DB_USER, preset, dbUser.getText());
		TagDB.setDbPropertyByPreset(TagDB.DB_PASSWORD, preset, dbPassword.getText());
		TagDB.setDbPropertyByPreset(TagDB.DB_ON_EXIT, preset, dbOnExit.getText());
		TagDB.setDbPropertyByPreset(TagDB.DB_MAPPINGS_FILE, preset, dbMappingsFile.getText());
	}
	
}
