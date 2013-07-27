package groovy;
/**
 * @author Damien Radtke
 * class GroovyProviderOptionPane
 * TODO: comment
 */
//{{{ Imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JSeparator;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.jEdit;
//}}}
public class GroovyProviderOptionPane extends AbstractOptionPane {
	
	private GroovyPlugin plugin;

	private JRadioButton groovyIncluded;
	private JRadioButton groovyCustom;
	private ButtonGroup group;
	
	private JButton groovyBrowse;
	private JTextField groovyPath;
	
	public GroovyProviderOptionPane() {
		super("groovy-provider");
		plugin = (GroovyPlugin) jEdit.getPlugin("groovy.GroovyPlugin");
	}
	
	protected void _init() {
		ButtonHandler handler = new ButtonHandler();
		// Radio buttons
		JPanel groovyPanel = new JPanel();
		groovyPanel.setLayout(new BoxLayout(groovyPanel, BoxLayout.X_AXIS));
		groovyPanel.add(groovyIncluded = new JRadioButton(jEdit.getProperty(
			"options.groovy-provider.included.label")));
		groovyPanel.add(groovyCustom = new JRadioButton(jEdit.getProperty(
			"options.groovy-provider.custom.label")));
		ButtonGroup groovyGroup = new ButtonGroup();
		groovyGroup.add(groovyIncluded);
		groovyGroup.add(groovyCustom);
		groovyPanel.add(new JSeparator(JSeparator.VERTICAL));
		groovyPanel.add(groovyPath = new JTextField());
		groovyBrowse = new JButton(jEdit.getProperty("vfs.browser.browse.label"));
		groovyBrowse.addActionListener(new BrowseHandler(groovyPath));
		groovyPanel.add(groovyBrowse);
		String groovy = jEdit.getProperty(GroovyPlugin.groovyProp);
		if (groovy == null) {
			groovyIncluded.setSelected(true);
			groovyPath.setEnabled(false);
			groovyBrowse.setEnabled(false);
		} else {
			groovyCustom.setSelected(true);
			groovyPath.setText(groovy);
		}
		groovyIncluded.addActionListener(handler);
		groovyCustom.addActionListener(handler);
		addComponent("", groovyPanel);
	}
	
	protected void _save() {
		if (groovyIncluded.isSelected()) {
			plugin.setGroovyJar();
		} else {
			plugin.setGroovyJar(groovyPath.getText());
		}
		plugin.setVars();
	}
	
	class ButtonHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source == groovyIncluded) {
				groovyPath.setEnabled(false);
				groovyBrowse.setEnabled(false);
			} else if (source == groovyCustom) {
				groovyPath.setEnabled(true);
				groovyBrowse.setEnabled(true);
			}
		}

	}

	class BrowseHandler implements ActionListener {
		private JTextField txt;
		public BrowseHandler(JTextField txt) {
			this.txt = txt;
		}
		public void actionPerformed(ActionEvent e) {
			VFSFileChooserDialog dialog = new VFSFileChooserDialog(
				jEdit.getActiveView(), System.getProperty("user.dir")+File.separator,
				VFSBrowser.OPEN_DIALOG, false, true);
			String[] files = dialog.getSelectedFiles();
			if (files != null) {
				txt.setText(files[0]);
			}
		}
	}
}
