/*
 * TrangGUI.java
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2010 Eric Le Lay
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 *
 * This class has been inspired by 
 * jing-trang-20090818/mod/trang/src/main/com/thaiopensource/relaxng/translate/Driver.java
 */
package xml.translate;


//{{{ imports

//{{{ standard imports
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.CardLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
//}}}

// jEdit
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

// common controls
import common.gui.OkCancelButtons;
import common.gui.ListPanel;
import common.gui.pathbuilder.PathBuilder;
import ise.java.awt.KappaLayout;

import xml.gui.VFSFileTextField;
import xml.gui.VFSFileList;

//}}}

/**
 * graphical front-end to Trang operations.
 */
public class TrangGUI extends EnhancedDialog {
	private static final String OUTPUT_TYPE_PROP = "xml.translate.output-type";
	
	private View view;
	private Buffer buffer;
	private VFSFileTextField inputField;
	private VFSFileTextField outputField;
	private JComboBox inputTypeBox;
	private JComboBox outputTypeBox;
	private Map<String, Options> inputOptions;
	private Map<String, Options> outputOptions;

	public TrangGUI(View parent, Buffer buffer){
		super(parent, "Trang GUI", false);
		
		this.view = view;
		this.buffer = buffer;
		inputOptions = new HashMap<String,Options>();
		outputOptions = new HashMap<String,Options>();

		Container c = getContentPane();
		
		c.setLayout(new KappaLayout());
		
		JLabel l;
		l = new JLabel("Input:");
		c.add(l, "0, 0, 2, 1, E, 0, 5");
		
		inputField = new VFSFileTextField(view, "xml.translate.input");
		inputField.setFile(buffer.getPath());
		inputField.addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(PropertyChangeEvent pce){
					if("file".equals(pce.getPropertyName())){
						String path = (String)pce.getNewValue();
						final String extension = path.substring(path.length() - 3);
				
						if(Arrays.asList(TrangTranslator.inputTypes).contains(extension))
						{
							SwingUtilities.invokeLater(new Runnable(){
									public void run(){
										inputTypeBox.setSelectedItem(extension);
									}
							});
						}
					}
				}
		});
		c.add(inputField, "2, 0, 7, 1, W, w, 5");
		
		
		l = new JLabel("Input Type:");
		c.add(l, "0, 1, 2, 1, E, 0, 5");

		inputTypeBox = new JComboBox(TrangTranslator.inputTypes);
		inputTypeBox.setName("input-type");
		c.add(inputTypeBox, "2, 1, 3, 1, W, 0, 5");
		
		final CardLayout inputOptionsLayout = new CardLayout();
		
		final JPanel inputOptionsPanel = new JPanel(inputOptionsLayout);
		inputOptionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK),"Options..."));

		inputOptions.put("rng",new Options(""));
		inputOptions.put("rnc",new Options(""));
		inputOptions.put("xml",new XMLOptions());
		inputOptions.put("dtd",new DTDOptions());

		outputOptions.put("rng",new Options(""));
		outputOptions.put("rnc",new Options(""));
		outputOptions.put("dtd",new Options(""));
		outputOptions.put("xsd",new XSDOptions());

		for(Map.Entry<String,Options> options: inputOptions.entrySet()){
			inputOptionsPanel.add(options.getValue(),options.getKey());
		}
		
		c.add(inputOptionsPanel, "2, 2, 8, 8, 0, hw, 5");
		
		inputTypeBox.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent evt) {
				String newInputType = (String)inputTypeBox.getSelectedItem();

				inputOptionsLayout.show(inputOptionsPanel, (String)evt.getItem());

				for(Options o:outputOptions.values()){
					o.inputTypeChanged(newInputType);
				}
			}
		});

		String _guessed = TrangTranslator.guessInputType(buffer);
		if(_guessed == null){
			_guessed = "rng";
		}
		final String guessed = _guessed;
		
		inputTypeBox.setSelectedItem(guessed);
		
		l = new JLabel("Output:");
		c.add(l, "0, 10, 2, 1, E, 0, 5");
		
		String outputType = jEdit.getProperty(OUTPUT_TYPE_PROP,"rng");
		String output;
		if(buffer.getPath().endsWith(guessed)){
			output = buffer.getPath().replaceAll(guessed+"$",outputType);
		}else{
			output = buffer.getPath() + "." + outputType;
		}

		outputField = new VFSFileTextField(view,"xml.translate.output");
		outputField.setFile(buffer.getPath());
		c.add(outputField, "2, 10, 7, 1, W, w, 5");

		l = new JLabel("Output Type:");
		c.add(l, "0, 11, 2, 1, E, 0, 5");

		outputTypeBox = new JComboBox(TrangTranslator.outputTypes);
		outputTypeBox.setName("output-type");
		c.add(outputTypeBox, "2, 11, 7, 1, W, w, 5");
		
		final CardLayout outputOptionsLayout = new CardLayout();
		
		final JPanel outputOptionsPanel = new JPanel(outputOptionsLayout);

		for(Map.Entry<String,Options> options: outputOptions.entrySet()){
			outputOptionsPanel.add(options.getValue(),options.getKey());
		}

		c.add(outputOptionsPanel, "2, 12, 8, 8, 0, hw, 5");

		outputTypeBox.addItemListener(new ItemListener(){
			private String oldOutputType = guessed;

			public void itemStateChanged(ItemEvent evt) {
				String newOutputType = (String)outputTypeBox.getSelectedItem();

				outputOptionsLayout.show(outputOptionsPanel, newOutputType);

				for(Options o:inputOptions.values()){
					o.outputTypeChanged(newOutputType);
				}
				
				if(outputField.getFile().endsWith(oldOutputType)){
					String out = outputField.getFile();
					outputField.setFile(out.substring(0,out.length()-3)+newOutputType);
					oldOutputType = newOutputType;
				}
			}
		});
		
		outputTypeBox.setSelectedItem(guessed);

		c.add(new OkCancelButtons(this),"0, 20, 10, 1, S, 5");
		pack();
	}
	
	public void ok(){
		List<String> inputs;
		List<String> inputParams;
		List<String> outputParams;
		Options selectedInputOptions = inputOptions.get((String)inputTypeBox.getSelectedItem());
		Options selectedOutputOptions = outputOptions.get((String)outputTypeBox.getSelectedItem());
		
		if(selectedInputOptions instanceof XMLOptions){
			XMLOptions xo = (XMLOptions)selectedInputOptions;
			inputs = new ArrayList<String>();
			inputs.add(inputField.getFile());
			inputs.addAll(xo.getOptions());
			inputParams = Collections.<String>emptyList();
		}else{
			inputs = Collections.singletonList(inputField.getFile());
			inputParams = selectedInputOptions.getOptions();
		}
		outputParams = selectedOutputOptions.getOptions();
		
		jEdit.setProperty(OUTPUT_TYPE_PROP,(String)outputTypeBox.getSelectedItem());
		
		selectedInputOptions.saveOptions();
		selectedOutputOptions.saveOptions();

		TrangTranslator.translate(view,
			(String)inputTypeBox.getSelectedItem(),
			inputs,
			inputParams,
			(String)outputTypeBox.getSelectedItem(),
			outputField.getFile(),
			outputParams);
		
		setVisible(false);
	}
	
	public void cancel(){
		setVisible(false);
	}
	
	
	class Options extends JPanel{
		private int y;
		private String propertyPrefix;
		protected Map<String,Option> options;
		
		Options(String propertyPrefix){
			super(new KappaLayout());
			this.propertyPrefix=propertyPrefix;
			options = new HashMap<String,Option>();
		}
		
		protected final void addStringOption(String name){
			JLabel l = new JLabel(jEdit.getProperty(propertyPrefix+name+".label"));
			add(l,"0, "+y+", 1, 1, E, 0, 5");
			
			StringOption option = new StringOption(name);
			option.setToolTipText(jEdit.getProperty(propertyPrefix+name+".tooltip"));
			option.setParamValue(jEdit.getProperty(propertyPrefix+name));
			options.put(name,option);
			add(option, "1, "+y+", 8, 1, W, w, 5");
			y++;
		}
		
		protected final void addCustomOption(String name,JComponent comp, int h){
			JLabel l = new JLabel(jEdit.getProperty(propertyPrefix+name+".label"));
			add(l,"0, "+y+", 1, 1, E, 0, 5");
			add(comp, "1, "+y+", 8, "+h+", W, w, 5");
			y+=h;
		}
		
		protected final void addCheckOption(String name, boolean isNoOption){
			CheckOption option = new CheckOption(name
				,jEdit.getProperty(propertyPrefix+name+".label")
				,isNoOption);
			option.setToolTipText(jEdit.getProperty(propertyPrefix+name+".tooltip"));
			option.setParamValue(jEdit.getProperty(propertyPrefix+name));
			options.put(name,option);
			add(option, "1, "+y+", 8, 1, W, 0, 5");
			y++;
		}

		protected final void addComboOption(String name, String[] values){
			JLabel l = new JLabel(jEdit.getProperty(name+".label"));
			add(l,"0, "+y+", 1, 1, E, 0, 5");
			ComboOption option = new ComboOption(name,values);
			option.setToolTipText(jEdit.getProperty(propertyPrefix+name+".tooltip"));
			option.setParamValue(jEdit.getProperty(propertyPrefix+name));
			options.put(name,option);
			add(option, "1, "+y+", 8, 1, W, w, 5");
			y++;
		}

		public List<String> getOptions(){
			List<String> res = new ArrayList<String>();
			for(Option o: options.values()){
				if(o.hasValue()){
					res.add(o.getParamValue());
				}
			}
			return res;
		}
		
		public void saveOptions(){
			for(Option o: options.values()){
				jEdit.setProperty(propertyPrefix+o.getName(),o.getParamValue());
			}
		}
		
		public void inputTypeChanged(String newInputType){}
		public void outputTypeChanged(String newOutputType){}
	}
	
	class XMLOptions extends Options{
		private VFSFileList list;

		XMLOptions(){
			super("xml.translate.xml.");
			setLayout(new BorderLayout());
			
			JLabel l = new JLabel(jEdit.getProperty("xml.translate.xml-inputs.label"));
			l.setAlignmentX(l.LEFT_ALIGNMENT);
			add(l,BorderLayout.NORTH);
			
			list = new VFSFileList(view,"xml.translate.xml-inputs");
			add(list, BorderLayout.CENTER);
		}
		
		public List<String> getOptions(){
			Object[] paths = list.getItems();
			ArrayList<String> al = new ArrayList<String>();
			for(int i=0;i<paths.length;i++){
				al.add((String)paths[i]);
			}
			return al;
		}
		
	}
	
	class DTDOptions extends Options{
		JTextArea namespaces;
		
		DTDOptions(){
			super("xml.translate.dtd.");
			
			addStringOption("xmlns");
			addCustomOption("namespaces",initNamespaces(),2);
			addStringOption("colon-replacement");
			addStringOption("element-define");
			addCheckOption("inline-attlist",true);
			addStringOption("attlist-define");
			addStringOption("any-name");
			addCheckOption("strict-any",false);
			addStringOption("annotation-prefix");
			addCheckOption("generate-start",false);
			
		}
		
		private JComponent initNamespaces(){
			namespaces = new JTextArea(3,30);
			namespaces.setName("namespaces");
			namespaces.setText(jEdit.getProperty("xml.translate.dtd.namespaces",""));
			namespaces.setToolTipText(jEdit.getProperty("xml.translate.dtd.namespaces.tooltip"));
			namespaces.addKeyListener(new KeyAdapter(){
					public void keyPressed(KeyEvent e){
						if(KeyEvent.VK_ENTER == e.getKeyCode()){
							namespaces.setText(namespaces.getText()+"\n");
							e.consume();
						}
					}
					public void keyReleased(KeyEvent e){
						if(KeyEvent.VK_ENTER == e.getKeyCode()){
							e.consume();
						}
					}
			});
			JScrollPane scr = new JScrollPane(namespaces);
			//scr.setVerticalScrollBarPolicy(scr.VERTICAL_SCROLLBAR_NEVER);
			return scr;
		}
		
		public void outputTypeChanged(String newOutputType){
			options.get("inline-attlist").setParamValue(
				"xsd".equals(newOutputType)? 
					"inline-attlist" : "no-inline-attlist");
		}

		@Override
		public List<String> getOptions(){
			List<String> opts = super.getOptions();
			String[] nsa = namespaces.getText().split("\\s+");
			for(String ns : nsa){
				if(!"".equals(ns))opts.add(ns);
			}
			return opts;
		}
		
		@Override
		public void saveOptions(){
			super.saveOptions();
			jEdit.setProperty("xml.translate.dtd.namespaces",namespaces.getText());
		}

		
	}
	
	class XSDOptions extends Options{
		
		XSDOptions(){
			super("xml.translate.xsd.");
			
			String[] process = {"strict","lax","skip"};
			addCheckOption("disable-abstract-elements",false);
			addComboOption("any-process-contents",process);
			addComboOption("any-attribute-process-contents",process);
		}

		public void inputTypeChanged(String newInputType){
			options.get("any-process-contents")
				.setParamValue("dtd".equals(newInputType) ? "strict" :"skip");
		}
	}
	
	interface Option {
		public String getParamValue();
		public void setParamValue(String value);
		public boolean hasValue();
		public String getName();
	}
	
	class StringOption extends JTextField implements Option{
		
		StringOption(String name){
			super(20);
			setName(name);
		}
		
		public String getParamValue(){
			return getName()+"="+getText();
		}
		
		public void setParamValue(String value){
			if(value.startsWith(getName()+"=")){
				setText(value.substring(getName().length()+1));
			}else{
				setText(value);
			}
		}
		
		public boolean hasValue(){
			return getText().length()>0;
		}
	}
	
	class CheckOption extends JCheckBox implements Option{
		private boolean noOption;
		
		CheckOption(String name, String label, boolean noOption){
			super(label);
			setName(name);
			this.noOption = noOption;
			
		}
		
		public String getParamValue(){
			if(isSelected()){
				return getName();
			}else{
				if(noOption){
					return "no-"+getName();
				}else{
					return null;
				}
			}
		}
		
		public void setParamValue(String value){
			setSelected(getName().equals(value));
		}
		
		public boolean hasValue(){
			// if xxx doesn't have a no-xxx variant, then don't use it if it's not selected
			return noOption || isSelected();
		}
	}
	
	class ComboOption extends JComboBox implements Option{
		
		ComboOption(String name, String[] values){
			super(values);
			setName(name);
		}
		
		public String getParamValue(){
			return getName()+"="+getSelectedItem();
		}
		
		public void setParamValue(String value){
			if(value.startsWith(getName()+"=")){
				setSelectedItem(value.substring(getName().length()+1));
			}else{
				setSelectedItem(value);
			}
		}

		public boolean hasValue(){
			return true;
		}
	}
	
}
