package projectbuilder.options;
// imports {{{
import projectbuilder.command.Entry;
import projectbuilder.command.BuildSettingDialog;
import projectbuilder.command.RunSettingDialog;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.util.Log;
import projectviewer.vpt.VPTProject;
// }}} imports
public class CommandList extends JPanel {
	private String type;
	private DefaultListModel listModel;
	private JList list;
	private JButton add;
	private JButton remove;
	private JButton move_up;
	private JButton move_down;
	private JButton modify;
	private JComboBox box;
	private VPTProject proj;
	public CommandList(final VPTProject proj, final String type) {
		super(new BorderLayout());
		this.proj = proj;
		this.type = type;
		JPanel listPanel = new JPanel(new BorderLayout());
		listModel = new DefaultListModel();
        list = new JList( listModel );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        add = new RolloverButton(GUIUtilities.loadIcon("Plus.png"));
        remove = new RolloverButton(GUIUtilities.loadIcon("Minus.png"));
        move_up = new RolloverButton(GUIUtilities.loadIcon("ArrowU.png"));
        move_down = new RolloverButton(GUIUtilities.loadIcon("ArrowD.png"));
        modify = new RolloverButton(GUIUtilities.loadIcon("Properties.png"));
        remove.setToolTipText(jEdit.getProperty("common.remove"));
        add.setToolTipText(jEdit.getProperty("common.add"));
        move_up.setToolTipText(jEdit.getProperty("common.moveUp"));
        move_down.setToolTipText(jEdit.getProperty("common.moveDown"));
        modify.setToolTipText("Modify");
        buttons.add(add);
        buttons.add(remove);
        buttons.add(move_up);
        buttons.add(move_down);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(modify);
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(250, 100));
        listPanel.add(BorderLayout.CENTER, pane);
        listPanel.add(BorderLayout.SOUTH, buttons);
        JPanel boxPanel = new JPanel(new BorderLayout());
        // NOTE: Probably shouldn't hardcode this in. Might  use jEdit properties
        if (type.equals("build")) {
        	boxPanel.add(BorderLayout.WEST, new JLabel("Build with:  "));
        	boxPanel.add(BorderLayout.CENTER, box = new JComboBox());
        	add(BorderLayout.NORTH, new JLabel("Build"));
        } else if (type.equals("run")) {
        	boxPanel.add(BorderLayout.WEST, new JLabel("Run with:  "));
        	boxPanel.add(BorderLayout.CENTER, box = new JComboBox());
        	add(BorderLayout.NORTH, new JLabel("Run"));
        }
        add(BorderLayout.CENTER, listPanel);
        add(BorderLayout.SOUTH, boxPanel);
        add.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		if (type.equals("build")) {
					BuildSettingDialog dialog = new BuildSettingDialog(jEdit.getActiveView(), proj, null);
					if (dialog.data != null) {
						listModel.addElement(new Entry(dialog.data));
						updateBox();
					}
				} else if (type.equals("run")) {
					RunSettingDialog dialog = new RunSettingDialog(jEdit.getActiveView(), proj, null);
					if (dialog.data != null) {
						listModel.addElement(new Entry(dialog.data));
						updateBox();
					}
				}
        	}
        });
        remove.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int index = list.getSelectedIndex();
        		Entry setting = (Entry) listModel.get(index);
        		if (setting == null) return;
        		int choice = GUIUtilities.confirm(jEdit.getActiveView(),
        			"projectBuilder.msg.remove-setting", new String[] { type+": "+setting.toString() },
        			JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        		if (choice == JOptionPane.YES_OPTION) {
        			// Remove it
        			listModel.remove(index);
        			updateBox();
        		}
        	}
        });
        move_up.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int index = list.getSelectedIndex();
        		if (listModel.get(index) == null || index == 0) return;
        		String text = proj.getProperty("projectBuilder.command."+type+"."+index);
        		proj.setProperty("projectBuilder.command."+type+"."+index,
        			proj.getProperty("projectBuilder.command."+type+"."+(index-1)));
        		proj.setProperty("projectBuilder.command."+type+"."+(index-1), text);
        		populate();
        		list.setSelectedIndex(index-1);
        	}
        });
        move_down.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int index = list.getSelectedIndex();
        		if (listModel.get(index) == null || index == (listModel.getSize()-1)) return;
        		String text = proj.getProperty("projectBuilder.command."+type+"."+index);
        		proj.setProperty("projectBuilder.command."+type+"."+index,
        			proj.getProperty("projectBuilder.command."+type+"."+(index+1)));
        		proj.setProperty("projectBuilder.command."+type+"."+(index+1), text);
        		populate();
        		list.setSelectedIndex(index+1);
        	}
        });
        modify.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		int index = list.getSelectedIndex();
        		Entry entry = (Entry) listModel.get(index);
        		if (entry == null) return;
        		if (type.equals("build")) {
					BuildSettingDialog dialog = new BuildSettingDialog(jEdit.getActiveView(), proj, entry);
					if (dialog.data != null) {
						listModel.set(index, new Entry(dialog.data));
						updateBox();
					}
				} else if (type.equals("run")) {
					RunSettingDialog dialog = new RunSettingDialog(jEdit.getActiveView(), proj, entry);
					if (dialog.data != null) {
						listModel.set(index, new Entry(dialog.data));
						updateBox();
					}
				}
        	}
        });
        populate();
	}
	private void updateBox() {
		box.removeAllItems();
		String saved = proj.getProperty("projectBuilder.command."+type);
		boolean boxIsValid = false;
		for (int i = 0; i < listModel.getSize(); i++) {
			Entry entry = (Entry) listModel.get(i);
			box.addItem(entry);
			if (entry.getProp().equals(saved)) {
				box.setSelectedItem(entry);
				boxIsValid = true;
			}
		}
		if (!boxIsValid) box.setSelectedItem(null);
	}
	private void populate() {
		listModel.clear();
		for (int i = 0; true; i++) {
			String prop = proj.getProperty("projectBuilder.command."+type+"."+i);
			if (prop == null) break;
			Entry cmd = new Entry(prop);
			listModel.addElement(cmd);
		}
		updateBox();
	}
	public void save() {
		try {
			for (int i = 0; i<listModel.getSize(); i++) {
				proj.setProperty("projectBuilder.command."+type+"."+i, ((Entry) listModel.get(i)).getProp());
			}
			for (int j=listModel.getSize(); true; j++) {
				if (proj.getProperty("projectBuilder.command."+type+"."+j) == null) break;
				proj.removeProperty("projectBuilder.command."+type+"."+j);
			}
			Entry entry = (Entry) box.getSelectedItem();
			if (entry != null)
				proj.setProperty("projectBuilder.command."+type, entry.getProp());
			else
				proj.removeProperty("projectBuilder.command."+type);
			//jEdit.saveSettings();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
