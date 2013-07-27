package projectbuilder.actions;
//{{{ imports
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.bsh.NameSpace;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;
import projectbuilder.ProjectBuilderPlugin;
import projectviewer.vpt.VPTProject;
//}}}
public class RunBeanshellDialog extends JDialog {
	public static void run(View view, VPTProject proj) {
		if (proj.getProperty("project.type") != null && proj.getProperty("project.bsh") != null) {
			RunBeanshellDialog dialog = new RunBeanshellDialog(view, proj);
			dialog.setVisible(true);
		}
	}
	
	private VPTProject project;
	private JList list;
	private View view;
	public RunBeanshellDialog(final View view, VPTProject proj) {
		super(view, "["+proj.getName()+"] Run...", true);
		this.project = proj;
		this.view = view;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel contents = new JPanel();
		contents.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		setContentPane(contents);
		
		contents.setLayout(new BorderLayout());
		ArrayList<ArrayList<String>> scripts = ProjectBuilderPlugin.getBeanshellScripts(project);
		String lastSelected = jEdit.getProperty("projectbuilder.bsh.last-selected");
		int lastSelectedIndex = 0;
		ScriptItem[] listData = new ScriptItem[scripts.size()];
		for (int i = 0; i<scripts.size(); i++) {
			listData[i] = new ScriptItem(scripts.get(i));
			if (listData[i].toString().equals(lastSelected)) {
				lastSelectedIndex = i;
			}
		}
		list = new JList(listData);
		list.setSelectedIndex(lastSelectedIndex);
		list.ensureIndexIsVisible(lastSelectedIndex);
		contents.add(BorderLayout.CENTER, new JScrollPane(list));
		contents.add(BorderLayout.SOUTH, new JLabel(jEdit.getProperty("projectbuilder.msg.run-beanshell")));
		list.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int code = e.getKeyCode();
					if (code == KeyEvent.VK_ENTER) {
						// Run
						if (!list.getSelectedValue().toString().equals("-")) {
							runBsh();
							dispose();
						}
					}
					else if (code == KeyEvent.VK_ESCAPE) {
						dispose();
					}
				}
		});
		list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						runBsh();
						dispose();
					}
				}
		});
		setSize(250, 200);
		setLocationRelativeTo(view);
	}
	
	protected void runBsh() {
		ScriptItem item = (ScriptItem) list.getSelectedValue();
		jEdit.setTemporaryProperty("projectbuilder.bsh.last-selected", item.toString());
		NameSpace namespace = new NameSpace(BeanShell.getNameSpace(), "Project Builder Script");
		try {
			namespace.setVariable("project", project);
			String bsh = MiscUtilities.constructPath(ProjectBuilderPlugin.getTemplatePathForProject(project),
				item.getPath());
			BeanShell._runScript(view, bsh, null, namespace);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	
	class ScriptItem {
		private ArrayList<String> data;
		public ScriptItem(ArrayList<String> data) {
			this.data = data;
		}
		
		public String toString() {
			return data.get(0);
		}
		
		public String getPath() {
			return data.get(1);
		}
	}
}
