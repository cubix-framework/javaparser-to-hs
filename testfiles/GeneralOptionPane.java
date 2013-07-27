/*
 * Created on Dec 15, 2003
 * @author Wim le Page
 * @author Pieter Wellens
 *
 */
package xquery.options;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

/**
 * @author Wim le Page
 * @author Pieter Wellens
 * @version 0.7.0
 *
 */
public class GeneralOptionPane extends AbstractOptionPane implements ActionListener {

	private JComboBox dropDown;
	private JCheckBox monitorPerformance = new JCheckBox(jEdit.getProperty("xquery.options.monitorPerformance.label"));
	private JCheckBox savePerformance = new JCheckBox(jEdit.getProperty("xquery.options.savePerformance.label"));
	private ButtonGroup displayGroup = new ButtonGroup();
	private JRadioButton noDisplay = new JRadioButton(jEdit.getProperty("xquery.options.display.no"));
	private JRadioButton toBuffer = new JRadioButton(jEdit.getProperty("xquery.options.display.buffer"));
	private JRadioButton toInfoViewer = new JRadioButton(jEdit.getProperty("xquery.options.display.infoviewer"));
	private JRadioButton toExternal = new JRadioButton(jEdit.getProperty("xquery.options.display.external"));
	private JTextField externalText = new JTextField(jEdit.getProperty("xquery.performance.external.text"));	
	private JCheckBox useIndenter = new JCheckBox(jEdit.getProperty("xquery.options.indent.label"));

	public GeneralOptionPane(){
		super("xquery.general");
	}

	/* (non-Javadoc)
	 * @see org.gjt.sp.jedit.AbstractOptionPane#_init()
	 */
	public void _init()
	{
		ActionListener monitorPerformanceListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				setPerformanceEnabled(monitorPerformance.isSelected());
			}
		};
		monitorPerformance.addActionListener(monitorPerformanceListener);

		monitorPerformance.setSelected(jEdit.getBooleanProperty("xquery.performance.selected"));
		setPerformanceEnabled(jEdit.getBooleanProperty("xquery.performance.selected"));
		
		savePerformance.setSelected(jEdit.getBooleanProperty("xquery.performance.save"));

		initRadioButtons();
		
		// performance options
        addSeparator("options.xquery.general.performance.label");
        addComponent(Box.createVerticalStrut(20));
		addComponent(monitorPerformance);
		addComponent(savePerformance);
		addComponent(noDisplay);
		addComponent(toBuffer);
		addComponent(toInfoViewer);
		addComponent(toExternal);
		addComponent(jEdit.getProperty("xquery.performance.external.editlabel"),externalText);

        addComponent(Box.createVerticalStrut(20));		
		addSeparator("options.xquery.general.postprocess.label");
        addComponent(Box.createVerticalStrut(20));

        useIndenter.setSelected(jEdit.getBooleanProperty("xquery.indent.selected"));
        addComponent(useIndenter);
		
        
        //postProcessing options
        addComponent(Box.createVerticalStrut(20));		
		addSeparator("options.xquery.general.adapter.label");
        addComponent(Box.createVerticalStrut(20));	
        
		JLabel adapterLabel = new JLabel(jEdit.getProperty("xquery.options.adapter.label"));
		addComponent(adapterLabel, GridBagConstraints.HORIZONTAL);
		
		
		// Adapter Selection options
		File jeditJarsDir = new File((jEdit.getSettingsDirectory()+ "/jars/"));
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("Adapter.jar");
			}
		};
		
		String[] filePaths = jeditJarsDir.list(filter);
		Vector pathVector = new Vector();
		for(int i=0; i < filePaths.length;i++){
			pathVector.add(filePaths[i].substring(0,(filePaths[i].length()-11))); // throw away Adapter.jar extension
		}
		
		ActionListener dropDownListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				jEdit.setProperty("xquery.adapter.selection", ((String)(dropDown.getSelectedItem()))+"Adapter");
			}
		};
		
		if (pathVector.size() != 0) {
			dropDown = new JComboBox(pathVector);
			if ((jEdit.getProperty("xquery.adapter.selection") == null) && pathVector.contains("Saxon"))
					dropDown.setSelectedItem("Saxon");
			else {
				String lastSelection = jEdit.getProperty("xquery.adapter.selection");
				dropDown.setSelectedItem(lastSelection.substring(0,lastSelection.length()-7)); // Throw Adapter extension away
			}
			addComponent(dropDown, GridBagConstraints.HORIZONTAL);
		} else {
			JLabel warningLabel = new JLabel("No Adapters found in "+ jeditJarsDir.getAbsolutePath() + ". Make sure they end with Adapter.jar");
			addComponent(warningLabel, GridBagConstraints.HORIZONTAL);
		}
		dropDown.addActionListener(dropDownListener);
	}

	/* (non-Javadoc)
	 * @see org.gjt.sp.jedit.AbstractOptionPane#_save()
	 */
	public void _save()
	{
		//save all the options so they will be remembered
		jEdit.setBooleanProperty("xquery.performance.selected",monitorPerformance.isSelected());
		jEdit.setBooleanProperty("xquery.performance.save",savePerformance.isSelected());
		jEdit.setBooleanProperty("xquery.performance.nodisplay",noDisplay.isSelected());
		jEdit.setBooleanProperty("xquery.performance.tobuffer",toBuffer.isSelected());
		jEdit.setBooleanProperty("xquery.performance.toinfoviewer",toInfoViewer.isSelected());
		jEdit.setBooleanProperty("xquery.performance.toexternal",toExternal.isSelected());
		jEdit.setBooleanProperty("xquery.indent.selected",useIndenter.isSelected());
		jEdit.setProperty("xquery.performance.external.text", externalText.getText());
		jEdit.setProperty("xquery.adapter.selection", ((String)(dropDown.getSelectedItem()))+"Adapter");
	}
	
    /**
     * Called when one of the radio buttons is clicked.
     */
    public void actionPerformed(ActionEvent e)
    {
		externalText.setEditable(toExternal.isSelected());
    }
	
	/**
	 * This is a helper function that inits all the radiobuttons
	 */
	private void initRadioButtons(){

		noDisplay.addActionListener(this);
		toBuffer.addActionListener(this);
		toInfoViewer.addActionListener(this);
		toExternal.addActionListener(this);

		displayGroup.add(noDisplay);
		displayGroup.add(toBuffer);
		displayGroup.add(toInfoViewer);
		displayGroup.add(toExternal);	
		
		if (jEdit.getBooleanProperty("xquery.performance.nodisplay")) {
			displayGroup.setSelected(noDisplay.getModel(),true);
			externalText.setEditable(false);
		} else if (jEdit.getBooleanProperty("xquery.performance.tobuffer")) {
			displayGroup.setSelected(toBuffer.getModel(),true);
			externalText.setEditable(false);
		} else if (jEdit.getBooleanProperty("xquery.performance.toinfoviewer")) {
			displayGroup.setSelected(toInfoViewer.getModel(),true);
			externalText.setEditable(false);
		} else if (jEdit.getBooleanProperty("xquery.performance.toexternal")){
			displayGroup.setSelected(toExternal.getModel(),true);
			externalText.setEditable(true);
		} else {
			displayGroup.setSelected(noDisplay.getModel(),true);
			externalText.setEditable(false);
		}
	}

	/**
	 * This is a helper function that enables or disables all the performance options
	 */
	private void setPerformanceEnabled(boolean enabled){
		savePerformance.setEnabled(enabled);
		externalText.setEditable(enabled && toExternal.isSelected()); 
		noDisplay.setEnabled(enabled);
		toBuffer.setEnabled(enabled);
		toInfoViewer.setEnabled(enabled);
		toExternal.setEnabled(enabled);	
	}

}
