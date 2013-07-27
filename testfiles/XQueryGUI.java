/*
 * Created on Dec 8, 2003
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 *
 */
package xquery;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.gjt.sp.jedit.*;

import errorlist.DefaultErrorSource;
import errorlist.ErrorSource;

/**
 * @author Wim Le Page
 * @author Pieter Wellens
 * @version 0.6.0
 *
 */
public class XQueryGUI extends JPanel {

	private View view;
	
	private final SelectXmlInputPanel inputPanel = new SelectXmlInputPanel();
	private final SelectXQueryInputPanel queryPanel = new SelectXQueryInputPanel();
	private final EvaluatePanel evaluatePanel = new EvaluatePanel();
	
	private DefaultErrorSource errorSource = new DefaultErrorSource("XQuery plugin");
		
	/**
	 * This constructor just creates a SelectXmlInputPanel, a SelectXQueryInputPanel and a EvaluatePanel
	 * In other words it creates the complete GUI
	 * @param view from jEdit
	 * 
	 */
	public XQueryGUI (View view)
	{
		super(new BorderLayout(0,30));
		this.view = view;
		add(inputPanel, BorderLayout.NORTH);
		add(queryPanel);
		add(evaluatePanel, BorderLayout.SOUTH);
	}
	
	/** This class represents the GUI for XML input selection panel
	 * @author Wim Le Page
	 * @author Pieter Wellens
	 *
	 */
	private class SelectXmlInputPanel extends JPanel {

		public JButton browseButton;
		private JRadioButton noContextRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.noContext"));
		private JRadioButton bufferRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.buffer"));
		private JRadioButton fileRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.file"));
		private FileSelectionPanel browsePanel = new FileSelectionPanel(view, "xquery.selectXmlInput");
		private FolderSelectionPanel uriSelectionPanel = new FolderSelectionPanel(view, "xquery.selectBaseUriInput");
		private final JTextField sourceField = new JTextField();
		
		public SelectXmlInputPanel() {
			super(new BorderLayout());

			createRadioButtons();
			
			JPanel docPanel = new JPanel(new BorderLayout());
			docPanel.add(uriSelectionPanel, BorderLayout.SOUTH);
			uriSelectionPanel.setSelectionEnabled(true);
			
			JPanel radioPanel = new JPanel(new BorderLayout());
			JPanel noContextPanel = new JPanel(new BorderLayout());
			noContextPanel.add(new JLabel(jEdit.getProperty("xquery.selectXmlInput.label")), BorderLayout.NORTH);
			noContextPanel.add(noContextRadio, BorderLayout.CENTER);
			
			JPanel bufferPanel = new JPanel(new BorderLayout());
			bufferPanel.add(bufferRadio, BorderLayout.CENTER);
			
			radioPanel.add(noContextPanel,BorderLayout.NORTH);
			radioPanel.add(bufferPanel,BorderLayout.CENTER);
			
			JPanel filePanel = new JPanel(new BorderLayout());
			filePanel.add(fileRadio, BorderLayout.NORTH);
			filePanel.add(browsePanel);

			add(docPanel, BorderLayout.NORTH);
			add(radioPanel, BorderLayout.CENTER);
			add(filePanel, BorderLayout.SOUTH);
		}
	
		/**
		 * This is a helper function that creates and inits all the radiobuttons.
		 * It also creates the necessary ActionListeners
		 */
		private void createRadioButtons() {
			ActionListener noContextSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					browsePanel.setSelectionEnabled(false);
					queryPanel.setBufferButtonState(true);
				}};
			noContextRadio.addActionListener(noContextSelected);
			noContextRadio.setSelected(true);			
			
			ActionListener bufferSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					browsePanel.setSelectionEnabled(false);
					queryPanel.setBufferButtonState(false);
				}};
			bufferRadio.addActionListener(bufferSelected);
			
			ActionListener fileSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					browsePanel.setSelectionEnabled(true);
					queryPanel.setBufferButtonState(true);
				}};
			fileRadio.addActionListener(fileSelected);
			
			ButtonGroup radioGroup = new ButtonGroup();
			radioGroup.add(noContextRadio);
			radioGroup.add(bufferRadio);
			radioGroup.add(fileRadio);		
		}


		/** This is needed because buffer must be disabled if chosen in XQueryInputPane and vice versa
		 * @param b is a boolean that enables/disables the bufferButton
		 * 
		 */
		public void setBufferButtonState(boolean b) {
			bufferRadio.setEnabled(b);
		}
		
		/**
		 * @return true if file is selected, else false
		 */
		public boolean isFileSelected() {
			return fileRadio.isSelected();
		};

		/**
		 * @return true if buffer is selected, else false
		 */
		public boolean isBufferSelected() {
			return bufferRadio.isSelected();
		};
	}
	

	/** This class represents the GUI to select the XQuery Input
	 * @author Wim Le Page
	 * @author Pieter Wellens
	 *
	 */
	private class SelectXQueryInputPanel extends JPanel{

		private JRadioButton bufferRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.buffer"));
		private JRadioButton fileRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.file"));
		private JRadioButton paneRadio = new JRadioButton(jEdit.getProperty("xquery.selectInput.pane")); 
		private FileSelectionPanel browsePanel = new FileSelectionPanel(view, "xquery.selectXQueryInput");
		private JTextArea queryTextArea = new JTextArea(6,6);

		private JButton evalButton;
		
		public SelectXQueryInputPanel() {
			super(new BorderLayout());
			
			createRadioButtons();
			JPanel bufferPanel = new JPanel(new BorderLayout());
			bufferPanel.add(new JLabel(jEdit.getProperty("xquery.selectXQueryInput.label")), BorderLayout.NORTH);
			bufferPanel.add(bufferRadio);
			
			JPanel filePanel = new JPanel(new BorderLayout());

			filePanel.add(fileRadio, BorderLayout.NORTH);
			filePanel.add(browsePanel);

			JPanel panePanel = new JPanel(new BorderLayout());
			panePanel.add(paneRadio, BorderLayout.NORTH);
			panePanel.add(new JScrollPane(queryTextArea));
			
			JPanel upperPanel = new JPanel(new BorderLayout());
			upperPanel.add(bufferPanel, BorderLayout.NORTH);
			upperPanel.add(filePanel);

			add(upperPanel, BorderLayout.NORTH);
			add(panePanel);
		}

		/**
		 * This is a helper function that creates and inits all the radiobuttons.
		 * It also creates the necessary ActionListeners
		 */
		private void createRadioButtons() {
			ActionListener bufferSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					browsePanel.setSelectionEnabled(false);
					setPaneSelectionEnabled(false);
					inputPanel.setBufferButtonState(false);
				}};
			bufferRadio.addActionListener(bufferSelected);
			
			ActionListener paneSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					setPaneSelectionEnabled(true);
					browsePanel.setSelectionEnabled(false);
					inputPanel.setBufferButtonState(true);
				}};
			paneRadio.addActionListener(paneSelected);	
			
			ActionListener fileSelected = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					browsePanel.setSelectionEnabled(true);
					setPaneSelectionEnabled(false);
					inputPanel.setBufferButtonState(true);
				}};
			fileRadio.addActionListener(fileSelected);
			
			fileRadio.setSelected(true);
			browsePanel.setSelectionEnabled(true);
			setPaneSelectionEnabled(false);
			

			ButtonGroup radioGroup = new ButtonGroup();
			radioGroup.add(bufferRadio);
			radioGroup.add(fileRadio);
			radioGroup.add(paneRadio);	
		};
		
		/**
		 * @param b is a boolean that deselectes/selects the Pane
		 */
		private void setPaneSelectionEnabled(boolean b) {
			queryTextArea.setEnabled(b);
		};

		/** This is needed because buffer must be disabled if chosen in ContextInputPane and vice versa
		 * @param b is a boolean that enables/disables the bufferButton
		 * 
		 */
		public void setBufferButtonState(boolean b) {
			bufferRadio.setEnabled(b);
		}
		
		/**
		 * @return true if file is selected, else false
		 */
		public boolean isFileSelected() {
			return fileRadio.isSelected();
		};

		/**
		 * @return true if buffer is selected, else false
		 */
		public boolean isBufferSelected() {
			return bufferRadio.isSelected();
		};

		/**
		 * @return true if pane is selected, else false
		 */
		public boolean isPaneSelected() {
			return paneRadio.isSelected();
		};

	}
	
	/** This class represents the GUI for the evaluationPanel at the bottom
	 * @author Wim Le Page
	 * @author Pieter Wellens
	 *
	 */
	public class EvaluatePanel extends JPanel implements ActionListener {
	
		private JButton evalButton;
	
		/**
		 * Constructor for the EvaluatePanel, it creates the GUI
		 */
		public EvaluatePanel() {
		
			  String iconName = jEdit.getProperty("xquery.evaluate.button.icon");
			  String toolTipText = jEdit.getProperty("xquery.evaluate.button.tooltip");
			  String shortcut = jEdit.getProperty("xquery.evaluate.shortcut");

			  if(shortcut != null) {
				toolTipText += " (" + shortcut + ")";
			  }

			  URL url = XQueryGUI.class.getResource(iconName);
			  evalButton = new JButton(new ImageIcon(url));
			  evalButton.setToolTipText(toolTipText);
			  evalButton.addActionListener(this);

			  Dimension dimension = new Dimension(90, 30);
			  evalButton.setMinimumSize(dimension);
			  evalButton.setPreferredSize(dimension);
		  
			  add(evalButton);
		}

				
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			evaluate();
		}
		
		
		/**
		 * This method creates the adapter, sets the baseURI, sets all the chosen general options (Performance, Indent,...) and
		 * finally evaluates the Query. After this the performance output is displayed as chosen
		 */
		private void evaluate() {
			saveInputSettings();
	
			errorSource.clear();
			ErrorSource.unregisterErrorSource(errorSource);	

			String contextURI = "";
			String baseURI = "";
			try {
				/* constructing the adapter */
				Properties props = getProperties();
				Adapter adapter = getAdapter(props);
				
				/* enabling/disabling performance monitoring */
				adapter.setPerformanceEnabled(jEdit.getBooleanProperty("xquery.performance.selected"));
				
				/* setting base uri */
				baseURI = getURI();
				if(!baseURI.trim().equals("")) {			
					adapter.setBaseUri(baseURI);
				}
				
				/* setting the context */
				setContext(adapter);

				/* evaluating the query */			
				String result = evaluateQuery(adapter);
				
				/* displaying results */
				if (!result.trim().equals("")) {
					Buffer buffer = jEdit.newFile(view);
					buffer.insert(0, result);
					buffer.setMode("xml");
					if (jEdit.getBooleanProperty("xquery.indent.selected")) {
						EditAction ea = jEdit.getAction("xmlindenter.indent");
						ea.invoke(view);
					}
				} else {
					JOptionPane.showMessageDialog(null, "Empty result from adapter." , "Adapter warning", JOptionPane.WARNING_MESSAGE);
				}
				
				/* displaying performance output */
				if (jEdit.getBooleanProperty("xquery.performance.selected")) {
					//System.err.println("handlePerformanceOutput call with performance: " + adapter.getPerformance());
					handlePerformanceOutput(adapter.getPerformance());
				}
			} catch (NonContextualAdapterException ae) {
				//ae.printStackTrace();
				JOptionPane.showMessageDialog(null, ae.toString() , "Error executing Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (ContextualAdapterException ae) {
				
				/*JOptionPane.showMessageDialog(null,ae.toString() , "THE PATH IS:" + getXQueryPath(), 
								JOptionPane.ERROR_MESSAGE );*/

				ErrorSource.registerErrorSource(errorSource);			
				errorSource.addError(new DefaultErrorSource.DefaultError(errorSource, ErrorSource.ERROR, getXQueryPath(), ae.getLine(), 
									ae.getStart(), ae.getEnd() ,ae.getMessage()));
				//ae.printStackTrace();
			} catch (AdapterException ae) {
				//ae.printStackTrace();
				JOptionPane.showMessageDialog(null, ae.getMessage() , "Error executing Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (URISyntaxException urie) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, urie.getMessage() , "Error in the base uri", JOptionPane.ERROR_MESSAGE );
			} catch (NoClassDefFoundError cnf) {
				//cnf.printStackTrace();
				JOptionPane.showMessageDialog(null, cnf.getMessage() ,"Error executing Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (ClassNotFoundException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error finding Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (SecurityException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Security error in Adapter",JOptionPane.ERROR_MESSAGE );
			} catch (NoSuchMethodException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error calling Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (IllegalArgumentException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error calling Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (InstantiationException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error instantiating Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (IllegalAccessException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error accessing Adapter", JOptionPane.ERROR_MESSAGE );
			} catch (InvocationTargetException e) {
				//e.printStackTrace();
				JOptionPane.showMessageDialog(null, e.getMessage() ,"Error invocing Adapter", JOptionPane.ERROR_MESSAGE );
			}
		}

		/**  to save settings for next session
		 */
		private void saveInputSettings() {
			jEdit.setProperty("xquery.selectXmlInput.last-source", inputPanel.browsePanel.getSourceFieldText());
			jEdit.setProperty("xquery.selectBaseUriInput.last-source", inputPanel.uriSelectionPanel.getSourceFieldText());
			jEdit.setProperty("xquery.selectXQueryInput.last-source", queryPanel.browsePanel.getSourceFieldText());
		}


		/**
		 * @return the properties 
		 */
		private Properties getProperties() {
			Properties props = new Properties();
			return props;
		}


		/**	This method dynamically creates an instance of teh selected adapter
		 * @param props that are needed for constructing the adapter
		 * @return the newly created adapter
		 * @throws ClassNotFoundException
		 * @throws SecurityException
		 * @throws NoSuchMethodException
		 * @throws IllegalArgumentException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * 
		 */
		private Adapter getAdapter(Properties props) throws ClassNotFoundException, SecurityException, 
											NoSuchMethodException, IllegalArgumentException, 
											InstantiationException, IllegalAccessException, 
											InvocationTargetException 
		{
			Adapter adapter = (Adapter) ServiceManager.getService(Adapter.class, jEdit.getProperty("xquery.adapter.selection",
					"SaxonAdapter"));
			if (adapter == null)
			{
				adapter = (Adapter) ServiceManager.getService(Adapter.class, "SaxonAdapter");
			}
			if (adapter == null)
				throw new ClassNotFoundException(Adapter.class + " with name " +jEdit.getProperty("xquery.adapter.selection",
					"SaxonAdapter") );
			adapter.setProperties(props);
			return adapter;
		}
		
		/**
		 * @return the String representing the baseURI
		 * @throws URISyntaxException
		 * 
		 */
		private String getURI() throws URISyntaxException
		{
			String uri = "";
			uri = inputPanel.uriSelectionPanel.getSourceFieldText();
			if (uri.trim().equals(jEdit.getProperty("xquery.selectBaseUriInput.prompt").trim())) {
				return "";
			} else if(!uri.trim().equals("")) {
				File base = new File(uri);
				if (!base.isDirectory()) {
					throw new URISyntaxException(
						"The path '"
							+ uri
							+ "' does not point to a directory", uri);
				}
				URI urii = new URI(uri.toString());
				return urii.toString();
			}
			return "";
		}
		
		/** This method returns the path to the Context, this is different depending on buffer of file input
		 * @return the path to the Context file
		 * 
		 */
		private String getContextPath() {

			String context = "";
			if (inputPanel.isBufferSelected()) {
				View activeView = jEdit.getActiveView();
				Buffer activeBuffer = activeView.getBuffer();
				context = activeBuffer.getPath();				
			} else if (inputPanel.isFileSelected()) {
				context = inputPanel.browsePanel.getSourceFieldText();
			}
			return context;
		}

		/** This method returns the path to the XQuery, this is different depending on buffer of file input
		 * @return the path of the Xquery
		 * 
		 */
		private String getXQueryPath(){
			String path = "";
			if (queryPanel.isBufferSelected()) {
				View activeView = jEdit.getActiveView();
				Buffer activeBuffer = activeView.getBuffer();
				path = activeBuffer.getPath();
			} else if (queryPanel.isFileSelected()) {
				path = queryPanel.browsePanel.getSourceFieldText();
			}
			return path;
		}
		
		
		private void setContext(Adapter adapter) {
			String contextURI = "";
			contextURI = getContextPath();
			if(!contextURI.trim().equals("")) {
				adapter.loadContextFromFile(contextURI);
			}
			if (inputPanel.isBufferSelected()) {
				View activeView = jEdit.getActiveView();
				Buffer activeBuffer = activeView.getBuffer();
				if (activeBuffer.isDirty()){
					/*String context = activeBuffer.getText(0,activeBuffer.getLength());
					adapter.loadContexFromString(context);*/
					JOptionPane.showMessageDialog(null, "Dirty buffer" ,"Error loading context", JOptionPane.ERROR_MESSAGE );
				} else {
					adapter.loadContextFromFile(getContextPath());
				}		
			} else if (inputPanel.isFileSelected()) {
				adapter.loadContextFromFile(getContextPath());
			}
		}
		
		
		/** This method is called in evaluate to finally evaluate the Query
		 * @param adapter that is needed for further evaluation, it communicates with the API
		 * @return the result String
		 * 
		 */
		private String evaluateQuery(Adapter adapter) {
			if (queryPanel.isPaneSelected()){
				String xq = queryPanel.queryTextArea.getText();
				return adapter.evaluateFromString(xq);
			} else if (queryPanel.isBufferSelected() ) {
				View activeView = jEdit.getActiveView();
				Buffer activeBuffer = activeView.getBuffer();
				if (activeBuffer.isDirty()){
					String xq = activeBuffer.getText(0,activeBuffer.getLength());
					return adapter.evaluateFromString(xq);
				} else {
					String queryPath = getXQueryPath();
					return adapter.evaluateFromFile(queryPath);
				}
			} else if (queryPanel.isFileSelected()) {
				String queryPath = getXQueryPath();
				return adapter.evaluateFromFile(queryPath);
				/*
				File file = new File(path);
				try {
					FileReader reader = new FileReader(file);
					BufferedReader breader = new BufferedReader(reader);
					while (breader.ready()) {
							xq += breader.readLine() + "\n";
					}
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(null, e2.toString() ,"Error reading XQuery", JOptionPane.ERROR_MESSAGE );
					//e2.printStackTrace();
				}
				*/
			}
			return "";
		}
		
		/** This method displays the performance as chosen in the optionspane
		 * @param performance String that needs to be displayed as chosen in the options
		 * 
		 */
		private void handlePerformanceOutput(String performance) {
			try {
				File perfFile;
				if (jEdit.getBooleanProperty("xquery.performance.save")){
					//create file from filename asked in popup
					String[] selections = GUIUtilities.showVFSFileDialog(jEdit.getActiveView(), "performance_output", JFileChooser.SAVE_DIALOG, false);
					if (selections != null) {
						//System.err.println(selections[0]);
						perfFile = new File(selections[0]);
					} else {
						//selection failed, create temporary file
						perfFile = File.createTempFile("XQuery","");
					}
				} else {
					//create temporary file
					perfFile = File.createTempFile("XQuery","");
				}
				perfFile.createNewFile();
				FileWriter fw = new FileWriter(perfFile);
				fw.write(performance);
				fw.close();
				if (jEdit.getBooleanProperty("xquery.performance.tobuffer")) {
					//open file in buffer
					jEdit.openFile(view, perfFile.getAbsolutePath());
				} else if (jEdit.getBooleanProperty("xquery.performance.toinfoviewer")) {
					//open (html) file with infoviewer
					//System.err.println("opening in viewer : " + perfFile.toURL().toString());
					URL u = new URL("file", null, perfFile.toString());
					infoviewer.InfoViewerPlugin.openURL(view, u.toString());
				} else if (jEdit.getBooleanProperty("xquery.performance.toexternal")) {
					//open file with external program 
					String command = jEdit.getProperty("xquery.performance.external.text");
					command = command.replaceAll("<p>", perfFile.getAbsolutePath());
					//System.err.println("exec command: " + command);
					Runtime.getRuntime().exec(command);
				} else {
					//do nothing
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.toString() ,"Error writing Performance", JOptionPane.ERROR_MESSAGE );
				//e.printStackTrace();
			}
					
		}
		
	}
	
	/** static method that allows adapter writers to use jEdit functionality without importing jEdit
	 * @param prop
	 * @return the valuefield(true or false) of the property
	 * 
	 */
	public static boolean getBooleanProperty(String prop){
			return jEdit.getBooleanProperty("xquery.adapter." + prop);
	}
	
	/** static method that allows adapter writers to use jEdit functionality without importing jEdit
	 * @param prop
	 * @return the valuefield of the property
	 * 
	 */
	public static String getProperty(String prop){
		return jEdit.getProperty("xquery.adapter." + prop);
	}
	
	/** static method that allows adapter writers to use jEdit functionality without importing jEdit
	 * @param prop
	 * @param bool
	 * 
	 */
	public static void setBooleanProperty(String prop, boolean bool){
		jEdit.setBooleanProperty("xquery.adapter." + prop, bool);
	}

	/** static method that allows adapter writers to use jEdit functionality without importing jEdit
	 * @param prop String
	 * @param string that you wish to set
	 * 
	 */
	public static void setProperty(String prop, String string){
		jEdit.setProperty("xquery.adapter." + prop, string);
	}
	

}