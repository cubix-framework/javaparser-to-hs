package console.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JPanel;


/**
 * Arranges JPanels in a stacked layout, and gives a convenient
 * interface for adding/switching by name.
 * @author ezust
 *
 */
public class PanelStack extends JPanel implements ActionListener
{
	HashMap<String, JPanel> panels;
	JPanel current;
	
	public PanelStack() {
		panels = new HashMap<String, JPanel>();
	
		setLayout(new BorderLayout());
		current = null;
		
	}
	
	/**
	 * @deprecated - use @ref add(String, JPanel)
	 */
	public void add(Object o) {
		throw new RuntimeException ("Don't call this.");
	}
	
	public void add(String name, JPanel panel) {
		panels.put(name, panel);
		
	}

	public boolean raise(String name) {
		JPanel p = panels.get(name);
		if (p != null) {
			if (p==current) return true;
			if (current != null) {
				remove(current);
			}
			add(p, BorderLayout.CENTER);
			revalidate();
			repaint();
			current = p;
			return true;
		}
		return false;
	}
	
	public JPanel get(String name) {
		return panels.get(name);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String name = e.getActionCommand();
		raise(name);
	}
}
