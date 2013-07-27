package python.shell;
/**
 * @author Damien Radtke
 * class PythonShellOptionPane
 * TODO: comment
 */
//{{{ Imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.jEdit;
//}}}
public class PythonShellOptionPane extends AbstractOptionPane {
	private JTextField exec;
	public PythonShellOptionPane() {
		super("python-shell");
	}

	protected void _init() {
		exec = new JTextField(
			jEdit.getProperty("options.python-shell.exec"));
		JButton browse = new JButton(
			jEdit.getProperty("vfs.browser.browse.label"));
		browse.addActionListener(new BrowseHandler());

		JPanel comp = new JPanel();
		comp.setLayout(new BoxLayout(comp, BoxLayout.X_AXIS));
		comp.add(exec);
		comp.add(browse);

		addComponent(jEdit.getProperty("options.python-shell.textfield-label"),
			comp);
	}

	protected void _save() {
		jEdit.setProperty("options.python-shell.exec", exec.getText());
	}

	class BrowseHandler implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			VFSFileChooserDialog dialog = new VFSFileChooserDialog(
				jEdit.getActiveView(), System.getProperty("user.dir")+File.separator,
				VFSBrowser.OPEN_DIALOG, false, true);
			String[] files = dialog.getSelectedFiles();
			if (files != null) {
				exec.setText(files[0]);
			}
		}
	}

}
