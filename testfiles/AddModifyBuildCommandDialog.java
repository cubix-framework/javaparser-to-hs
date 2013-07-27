package projectbuilder.build;
// imports {{{
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.browser.VFSBrowser;

import projectviewer.ProjectViewer;
// }}} imports
public class AddModifyBuildCommandDialog extends EnhancedDialog {
	private int text_width = 20;
	private JPanel panel;
	private JRadioButton system;
	private JRadioButton ant;
	private JTextField system_cmd;
	private JTextField ant_target;
	private JTextField ant_buildfile;
	private JButton ant_buildfile_browse;
	private View view;
	private String cmd;
	
	public AddModifyBuildCommandDialog(final View view, String def) {
		super(view, "Add/Modify Build Command", true);
		this.view = view;
		
		panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(Box.createVerticalGlue());
		
		JPanel system_panel = new JPanel();
		system_panel.setLayout(new BoxLayout(system_panel, BoxLayout.LINE_AXIS));
		system_panel.add(system = new JRadioButton("System Command:  "));
		system_panel.add(system_cmd = new JTextField(text_width));
		panel.add(system_panel);
		
		panel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel ant_panel = new JPanel();
		ant_panel.setLayout(new BoxLayout(ant_panel, BoxLayout.LINE_AXIS));
		ant_panel.add(ant = new JRadioButton("Build with Ant:  "));
		ant_panel.add(Box.createHorizontalGlue());
		panel.add(ant_panel);
		JPanel target_panel = new JPanel();
		target_panel.setLayout(new BoxLayout(target_panel, BoxLayout.LINE_AXIS));
		target_panel.add(new JLabel("Target:  "));
		target_panel.add(ant_target = new JTextField(text_width));
		target_panel.add(Box.createHorizontalGlue());
		JPanel buildfile_panel = new JPanel();
		buildfile_panel.setLayout(new BoxLayout(buildfile_panel, BoxLayout.LINE_AXIS));
		buildfile_panel.add(new JLabel("Build file:  "));
		buildfile_panel.add(ant_buildfile = new JTextField(text_width));
		buildfile_panel.add(ant_buildfile_browse = new JButton("Browse..."));
		panel.add(target_panel);
		panel.add(buildfile_panel);
		
		ButtonGroup radio = new ButtonGroup();
		radio.add(system);
		radio.add(ant);
		
		if (def.startsWith("ANT[")) {
			Properties props = BuildCommand.parseAntCommand(def);
			ant.setSelected(true);
			ant_target.setEnabled(true);
			ant_buildfile.setEnabled(true);
			ant_buildfile_browse.setEnabled(true);
			system_cmd.setEnabled(false);
			ant_target.setText(props.getProperty("target", ""));
			ant_buildfile.setText(props.getProperty("buildfile", ""));
		} else {
			system.setSelected(true);
			ant_target.setEnabled(false);
			ant_buildfile.setEnabled(false);
			ant_buildfile_browse.setEnabled(false);
			system_cmd.requestFocusInWindow();
			ant_buildfile.setText(ProjectViewer.getActiveProject(view).getRootPath()+File.separator+"build.xml");
		}
		
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == ant_buildfile_browse) {
					VFSFileChooserDialog dialog = new VFSFileChooserDialog(view,
						ProjectViewer.getActiveProject(view).getRootPath()+File.separator+"build.xml",
						VFSBrowser.OPEN_DIALOG, false);
					String[] selected = dialog.getSelectedFiles();
					if (selected == null || selected.length == 0) return;
					ant_buildfile.setText(selected[0]);
				} else if (source == system) {
					system_cmd.setEnabled(true);
					ant_target.setEnabled(false);
					ant_buildfile.setEnabled(false);
					ant_buildfile_browse.setEnabled(false);
				} else if (source == ant) {
					system_cmd.setEnabled(false);
					ant_target.setEnabled(true);
					ant_buildfile.setEnabled(true);
					ant_buildfile_browse.setEnabled(true);
				}
			}
		};
		system.addActionListener(listener);
		ant.addActionListener(listener);
		ant_buildfile_browse.addActionListener(listener);
		
		panel.add(new common.gui.OkCancelButtons(this));
		
		panel.add(Box.createVerticalGlue());
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		add(panel);
		pack();
		setLocationRelativeTo(view);
		setVisible(true);
	}
	public AddModifyBuildCommandDialog(final View view) {
		this(view, "");
	}
	public void ok() {
		if (ant.isSelected()) {
			String target = ant_target.getText();
			String buildfile = ant_buildfile.getText();
			cmd = "ANT[target="+target+",buildfile="+buildfile+"]";
			if (!new File(buildfile).exists()) {
				GUIUtilities.error(view, "projectBuilder.msg.invalid-build-file", null);
			} else {
				dispose();
			}
		} else {
			cmd = system_cmd.getText();
			dispose();
		}
	}
	public void cancel() {
		cmd = null;
		dispose();
	}
	public String getValue() {
		return cmd;
	}
}
