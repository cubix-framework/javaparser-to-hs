package com.illengineer.jcc.jedit;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;


public class MessageDialog extends JDialog {
	public MessageDialog() {
		super();
		initComponents();
	}

	public MessageDialog(Frame owner) {
		super(owner);
		initComponents();
	}

	public MessageDialog(Dialog owner) {
		super(owner);
		initComponents();
		pack();
	}
	
	public void showDlg(String title, String msg) {
	    setTitle(title);
	    messageLabel.setText(msg);
	    setVisible(true);
	    repaintDlg();
	    try {
		// Forgive this crusty hack, but without a little sleep, Mac OS X
		// displays a blank window about 75% of the time.
		Thread.sleep(100);
	    } catch (Exception ex) {}
	    repaintDlg();
	}
	
	public void closeDlg() {
	    setVisible(false);
	}
	
	public void repaintDlg() {
	    _repaintImmediately(panel1);
	}
	
	static void _repaintImmediately(JComponent component) {
	    component.paintImmediately(0, 0, component.getWidth(), component.getHeight());
	}

	private void initComponents() {
		panel1 = new JPanel();
		messageLabel = new JLabel();

		//======== this ========
		setTitle("Message");
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout(10, 10));

		//======== panel1 ========
		{
			panel1.setBorder(new CompoundBorder(
				new EmptyBorder(10, 10, 10, 10),
				new BevelBorder(BevelBorder.LOWERED)));
			panel1.setLayout(new BorderLayout());

			//---- messageLabel ----
			messageLabel.setText("Message Here");
			messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
			panel1.add(messageLabel, BorderLayout.CENTER);
		}
		contentPane.add(panel1, BorderLayout.CENTER);
		setSize(365, 165);
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
		
		
	}

	public JPanel panel1;
	public JLabel messageLabel;
}
