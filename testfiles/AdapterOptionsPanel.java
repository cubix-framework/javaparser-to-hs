/*
 * Created on May 18, 2004
 *
 */
package xquery;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

/**
 * This class extends AbstractOptionPane so that it allows dynamic switching needed 
 * because we allow dynamic adapter loading.
 * We also added some more convenient functions for the adapterwriter.
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */
public class AdapterOptionsPanel extends AbstractOptionPane {

	/**
	 * The list of Boolean components already added to the layout manager.
	 */
	protected Vector booleanVector;
	
	/**
	 * The list of Selection components already added to the layout manager.
	 */
	protected Vector selectionVector;
	
	/**
	 * @param arg0
	 * 
	 * This constructor should never be called, it has to be implemented though
	 */
	public AdapterOptionsPanel(String arg0) {
		super("");
		setLayout(gridBag = new GridBagLayout());
		booleanVector = new Vector();
		selectionVector = new Vector();
	}
	
	public AdapterOptionsPanel()
	{
		super("");
		setLayout(gridBag = new GridBagLayout());
		booleanVector = new Vector();
		selectionVector = new Vector();
	} 

	public void _save()
	{
		for (int i=0; i < booleanVector.size();i++){
			PropertyComponent propComp = (PropertyComponent)booleanVector.elementAt(i);
			JCheckBox checkBox = (JCheckBox)(propComp.component);
			//System.err.println("xquery.adapter." + propComp.property + " == " + checkBox.isSelected());
			jEdit.setBooleanProperty("xquery.adapter." + propComp.property,checkBox.isSelected());
		}
		for (int i=0; i < selectionVector.size();i++){
			PropertyComponent propComp = (PropertyComponent)selectionVector.elementAt(i);
			JComboBox dropDown = (JComboBox)(propComp.component);
			//System.err.println("xquery.adapter." + propComp.property + " == " + (String)dropDown.getSelectedItem());
			jEdit.setProperty("xquery.adapter." + propComp.property,(String)dropDown.getSelectedItem());
		}			
	}
	

	/**
	 * Adds a separator component.
	 * @since jEdit 4.1pre7
	 */
	public void addSeparator()
	{
		addComponent(Box.createVerticalStrut(6));

		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.fill = GridBagConstraints.BOTH;
		cons.anchor = GridBagConstraints.WEST;
		cons.weightx = 1.0f;
		//cons.insets = new Insets(1,0,1,0);

		gridBag.setConstraints(sep,cons);
		add(sep);

		addComponent(Box.createVerticalStrut(6));
	} 

	/**
	 * Adds a separator component.
	 * @param label The separator label property
	 * @since jEdit 2.6pre2
	 */
	public void addSeparator(String label)
	{
		if(y != 0)
			addComponent(Box.createVerticalStrut(6));

		Box box = new Box(BoxLayout.X_AXIS);
		Box box2 = new Box(BoxLayout.Y_AXIS);
		box2.add(Box.createGlue());
		box2.add(new JSeparator(JSeparator.HORIZONTAL));
		box2.add(Box.createGlue());
		box.add(box2);
		JLabel l = new JLabel(label); // this is changed !
		l.setMaximumSize(l.getPreferredSize());
		box.add(l);
		Box box3 = new Box(BoxLayout.Y_AXIS);
		box3.add(Box.createGlue());
		box3.add(new JSeparator(JSeparator.HORIZONTAL));
		box3.add(Box.createGlue());
		box.add(box3);

		GridBagConstraints cons = new GridBagConstraints();
		cons.gridy = y++;
		cons.gridheight = 1;
		cons.gridwidth = GridBagConstraints.REMAINDER;
		cons.fill = GridBagConstraints.BOTH;
		cons.anchor = GridBagConstraints.WEST;
		cons.weightx = 1.0f;
		cons.insets = new Insets(1,0,1,0);

		gridBag.setConstraints(box,cons);
		add(box);
	}
	
	public JCheckBox addBooleanComponent(String label, String property, boolean def){
		JCheckBox checkBox = new JCheckBox(label);
		PropertyComponent propComp = new PropertyComponent(property, checkBox);
		booleanVector.add(propComp);
		checkBox.setSelected(jEdit.getBooleanProperty("xquery.adapter." + property, def));
		addComponent(checkBox, GridBagConstraints.HORIZONTAL);
		return checkBox;
	}	
	
	public JCheckBox addBooleanComponent(String label, String property){
		return addBooleanComponent(label,property,false);
	}
	
	public JComboBox addSelectionComponent(String label, String[] selections ,String property, String def){
		JComboBox dropDown = new JComboBox(selections);
		PropertyComponent propComp = new PropertyComponent(property, dropDown);
		selectionVector.add(propComp);
		dropDown.setSelectedItem(jEdit.getProperty("xquery.adapter." + property, def));
		addComponent(label, dropDown, GridBagConstraints.HORIZONTAL);
		return dropDown;
	}
	
	public JComboBox addSelectionComponent(String label, String[] selections ,String property){
		return addSelectionComponent(label,selections,property,selections[0]);
	}
	
	public Vector getBooleanComponents(){
		return booleanVector;
	}
	
	public Vector getSelectionComponents(){
		return selectionVector;
	}
	
	public class PropertyComponent {
		
		private String property;
		private Component component;
		
		PropertyComponent(String property, Component component){
			this.property = property;
			this.component = component;
		}
	}
}
