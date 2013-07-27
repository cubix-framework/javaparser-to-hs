package projectbuilder.command;
// imports {{{
import projectbuilder.command.Entry;
import common.gui.OkCancelButtons;
import projectviewer.vpt.VPTProject;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.browser.VFSBrowser;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
// }}} imports
public class RunSettingDialog extends EnhancedDialog implements ActionListener {
	private View view;
	private VPTProject proj;
	private JTabbedPane notebook;
	private JTextField system_cmd;
	private JTextField all_name;
	public String data = null;
	public RunSettingDialog(View view, VPTProject proj, Entry old_entry) {
		super(view, (old_entry == null) ? "Add Run Setting" : "Modify Run Setting", true);
		this.view = view;
		this.proj = proj;
		
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Name:   "));
		namePanel.add(all_name = new JTextField(20));
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		notebook = new JTabbedPane();
		notebook.addTab("System", null, buildSystemTab(), "Use a system command");
		panel.add(BorderLayout.NORTH, namePanel);
		panel.add(BorderLayout.CENTER, notebook);
		panel.add(BorderLayout.SOUTH, new OkCancelButtons(this));
		
		if (old_entry != null) {
			Entry.ParsedCommand entry = old_entry.parse();
			String type = entry.type();
			all_name.setText(old_entry.getName());
			if (type.equals("SYSTEM")) {
				notebook.setSelectedIndex(0);
				system_cmd.setText(entry.getProperty("cmd"));
			}
		}
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		add(panel);
		setPreferredSize(new Dimension(450, 250));
		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}
	private JPanel buildSystemTab() {
		JPanel tab = new JPanel();
		JPanel panel = new JPanel();
		tab.setLayout(new BoxLayout(tab, BoxLayout.PAGE_AXIS));
		tab.add(Box.createVerticalGlue());
		tab.add(panel);
		//tab.add(Box.createRigidArea(new Dimension(0, 4)));
		tab.add(new JLabel("Commands are run in the project's root directory"));
		tab.add(Box.createVerticalGlue());
		tab.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		panel.add(new JLabel("Command:   "));
		panel.add(system_cmd = new JTextField(20));
		return tab;
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
	}
	public void ok() {
		String name = all_name.getText();
		if (name.trim().length() == 0) {
			all_name.grabFocus();
			return;
		}
		String title = notebook.getTitleAt(notebook.getSelectedIndex());
		if (title.equals("System")) {
			String cmd = system_cmd.getText();
			if (cmd.trim().length() == 0) {
				system_cmd.grabFocus();
				return;
			}
			data = name+":SYSTEM[cmd="+cmd+"]";
		}
		dispose();
	}
	public void cancel() {
		dispose();
	}
}
