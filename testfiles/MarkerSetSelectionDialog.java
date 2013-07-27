package marker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.ColorWellButton;

@SuppressWarnings("serial")
public class MarkerSetSelectionDialog extends JDialog {
	
	private static final Color DEFAULT_COLOR = Color.black;
	private JTextField name;
	private ColorWellButton color;
	private JList markerSets;
	private String selected;
	private Color selectedColor;

	static public String askForMarkerSet(View view, String active)
	{
		MarkerSetSelectionDialog dlg = new MarkerSetSelectionDialog(view, active);
		dlg.setVisible(true);
		return dlg.selected;
	}
	
	static private String DIALOG_GEOMETRY = MarkerSetsPlugin.OPTION +
		"MarkerSetSelectionDialogGeometry";
	
	private void saveGeometry() {
		GUIUtilities.saveGeometry(this, DIALOG_GEOMETRY);
	} 
	
	public MarkerSetSelectionDialog(View view, String active)
	{
		super(view, true);
		setLayout(new BorderLayout());
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				saveGeometry();	
			}
		});
		JPanel namePanel = new JPanel(new BorderLayout());
		add(namePanel, BorderLayout.NORTH);
		namePanel.add(new JLabel("Marker set:"), BorderLayout.WEST);
		name = new JTextField(30);
		namePanel.add(name, BorderLayout.CENTER);
		color = new ColorWellButton(DEFAULT_COLOR);
		namePanel.add(color, BorderLayout.EAST);
		markerSets = new JList(MarkerSetsPlugin.getMarkerSetNames());
		add(new JScrollPane(markerSets), BorderLayout.CENTER);
		markerSets.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;
				String selected = markerSets.getSelectedValue().toString();
				name.setText(selected);
				Color c;
				MarkerSet ms = MarkerSetsPlugin.getMarkerSet(selected);
				if (ms != null)
					c = ms.getColor();
				else
					c = DEFAULT_COLOR;
				color.setSelectedColor(c);
			}
		});
		markerSets.setSelectedValue(active, true);
		JPanel buttons = new JPanel();
		add(buttons, BorderLayout.SOUTH);
		final JButton ok = new JButton("Ok");
		buttons.add(ok);
		JButton cancel = new JButton("Cancel");
		buttons.add(cancel);
		ActionListener closeButtonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close(e.getSource() == ok);
			}
		};
		ok.addActionListener(closeButtonListener);
		cancel.addActionListener(closeButtonListener);
		pack();
		GUIUtilities.loadGeometry(this, DIALOG_GEOMETRY);
	}
	public void close(boolean ok)
	{
		if (ok)
		{
			selected = name.getText();
			selectedColor = color.getSelectedColor();
		}
		else
		{
			selected = null;
			selectedColor = null;
		}
		saveGeometry();
		setVisible(false);
	}
	public String getSelectedName()
	{
		return selected;
	}
	public Color getSelectedColor()
	{
		return selectedColor;
	}
}
