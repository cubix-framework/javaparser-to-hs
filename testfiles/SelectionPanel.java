/*
 * Created on Feb 11, 2004
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */
package xquery;

/**
 * @author Wim Le Page
 * @author Pieter Wellens
 * @version Feb 11, 2004
 *
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * This is a widget Panel providing a label, a browseButton and a textfield
 * Everything will be automatically remembered by jEdit if you use this widget.
 * FileSelectionPanel and FolderSelectionPanel are derived from this class
 * @author Wim le Page
 * @author Pieter Wellens
 * @version 0.6.0
 *
 */

public abstract class SelectionPanel extends JPanel implements ActionListener  {

	private JButton browseButton;
	protected JTextField sourceField = new JTextField();
	protected View view;
	
	protected String propLabel;
	
	/**
	 * @param view from jEdit
	 * @param propLabel represents the label that you want this widget to be created with
	 */
	public SelectionPanel(View view, String propLabel) {
		super(new BorderLayout(3,0));
		
		this.view = view;
		this.propLabel = propLabel;
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		JLabel sourceLabel = new JLabel(jEdit.getProperty(propLabel +".browselabel"));
		leftPanel.add(sourceLabel, BorderLayout.NORTH);
	
		String lastSource = jEdit.getProperty(propLabel + ".last-source");
		if(lastSource == null) {
		  sourceField.setText(jEdit.getProperty(propLabel +".prompt"));
		} else {
		  sourceField.setText(lastSource);
		}
		
		sourceField.setEnabled(false);
		leftPanel.add(sourceField, BorderLayout.SOUTH);
	
		createBrowseButton();
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(browseButton,BorderLayout.SOUTH);
			
		add(leftPanel);
		add(rightPanel, BorderLayout.EAST);
	}
	
	/**
	 * Helper function to create the browseButton. Loading the image etc
	 */
	private void createBrowseButton() {
		String iconName = jEdit.getProperty(propLabel + ".button.icon");
		String toolTipText = jEdit.getProperty(propLabel + ".button.tooltip");
		String shortcut = jEdit.getProperty("xquery.selectInput.shortcut");

		if(shortcut != null) {
		  toolTipText += " (" + shortcut + ")";
		}

		URL url = XQueryGUI.class.getResource(iconName);
		browseButton = new JButton(new ImageIcon(url));
		
		browseButton.setToolTipText(toolTipText);
		browseButton.addActionListener(this);

		Dimension dimension = new Dimension(30, 30);
		browseButton.setMinimumSize(dimension);
		browseButton.setMaximumSize(dimension);
		browseButton.setPreferredSize(dimension);
		browseButton.setEnabled(false);	
	};

	/** This usually is the path to a file or directory
	 * @return a String respresenting the Text in the textfield.
	 * 
	 */
	public String getSourceFieldText() {
		return sourceField.getText();
	};		
	
	/**
	 * @param b enables/disables the button and the textfield
	 */
	public void setSelectionEnabled(boolean b) {
		sourceField.setEnabled(b);
		browseButton.setEnabled(b);
	};
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public abstract void actionPerformed(ActionEvent event);		
}
