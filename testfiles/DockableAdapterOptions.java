/*
 * Created on Apr 28, 2004
 *
 */
package xquery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.gjt.sp.jedit.View;

import xquery.options.AdapterOptionPane;

/** This class makes it possible to dock the Adapter Options in jEdit. Making them much more accesible.
 * @author Wim le Page
 * @author Pieter Wellens
 *
 */
public class DockableAdapterOptions extends JPanel {
	
	private View view;
	private AdapterOptionPane optionPane = new AdapterOptionPane();
	
	private JPanel buttonPanel = new JPanel();
	private JButton applyButton = new JButton("Apply");
	private JButton updateButton = new JButton("Refresh");
	
	public DockableAdapterOptions(View view) throws Exception{
			super(new BorderLayout(0,30));
			this.view = view;
						
			optionPane._init();
			if (!optionPane.exception) {
				createButtons();
				optionPane._save(); // this seems to be necessary because jedit's property file is not read everytime --> it's the property-Object that is queried
				add(optionPane, BorderLayout.NORTH);
				add(buttonPanel);
			} 
			//else {
			//	JOptionPane.showMessageDialog(null,"Error Creating OptionsPanel","Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			//}
	}
	
	private void createButtons() {
		ActionListener updateClicked = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				optionPane._init();
				optionPane.repaint();
				optionPane.updateUI();
			}};
		updateButton.addActionListener(updateClicked);
		
		ActionListener applyClicked = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				optionPane._save();
			}};
		applyButton.addActionListener(applyClicked);
		
		Dimension dimension = new Dimension(90, 30);
		applyButton.setMinimumSize(dimension);
		applyButton.setMaximumSize(dimension);
		applyButton.setPreferredSize(dimension);
		buttonPanel.add(applyButton);
		
		updateButton.setToolTipText("Reads latest saved options for active Adapter");
		updateButton.setMinimumSize(dimension);
		updateButton.setMaximumSize(dimension);
		updateButton.setPreferredSize(dimension);
		buttonPanel.add(updateButton);
	}
	
}
