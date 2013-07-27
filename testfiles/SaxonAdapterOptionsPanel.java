/*
 * Created on Apr 21, 2004
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */

package saxonadapter;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import xquery.AdapterOptionsPanel;
import xquery.XQueryGUI;

/**
 * @author Wim le Page
 * @author Pieter Wellens
 *
 */
public class SaxonAdapterOptionsPanel extends AdapterOptionsPanel {
	
	
	private JTextField uriClassText = new JTextField(XQueryGUI.getProperty("saxon.uriclass"));
	private JCheckBox useUri;
	
	public SaxonAdapterOptionsPanel(){
		super();
	}
	
	protected void _init(){
		addSeparator("Saxon Options");
		addSelectionComponent("Tree DataStructure to use",new String[] {"Standard Tree", "Tiny Tree"}, "saxon.tree", "Tiny Tree");
		addBooleanComponent("Omit XML declarations", "saxon.omitdecl", true);
		addBooleanComponent("Explain optimized query expression", "saxon.explain", false);
		addBooleanComponent("Line Numbering", "saxon.linenumber", false);
		addBooleanComponent("Disallow calls to Java Methods", "saxon.nojava", false);
		
		useUri = addBooleanComponent("Use specified URIResolver class", "saxon.useuriclass", false);
		uriClassText.setEditable(useUri.isSelected());
		addComponent(uriClassText, GridBagConstraints.HORIZONTAL);
		
		ActionListener uriSelected = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				uriClassText.setEditable(useUri.isSelected());
			}};
		useUri.addActionListener(uriSelected);
		
		addBooleanComponent("Strip whitespace text nodes", "saxon.stripws", false);
		addBooleanComponent("Display timing information", "saxon.timing", false);
		addBooleanComponent("Trace query execution", "saxon.tracequery", false);
		addBooleanComponent("Trace calls to external Java functions", "saxon.tracejava", false);
		//addBooleanComponent("Wrap result sequence in XML elements", "saxon.wrap", false);
		
	}
	
	public void _save(){
		super._save();
		// uriClassText is the only component that has to be saved "manually" because all the rest is added 
		// with addBooleanComponent or addSelectionComponent and these options are saved automaticaly
		XQueryGUI.setProperty("saxon.uriclass",uriClassText.getText());
	}

}
