/*
 * XPathTool.java - GUI for evaluating XPath expressions
 *
 * Copyright (C) 2002 Greg Merrill
 *               2002, 2003, 2004 Robert McKinnon
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package xslt;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.apache.xml.utils.PrefixResolver;

import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.BufferAdapter;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.search.CurrentBufferSet;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * GUI for evaluating XPath expressions.
 *
 * @author Greg Merrill
 * @author Robert McKinnon
 */
public class XPathTool extends JPanel implements ListSelectionListener,
		ActionListener, DefaultFocusComponent, ItemListener {

	public static final String XPATH_ADAPTER_PROP="xpath.adapter";
	
	private View view;
	private final XsltAction grabNSAction = new GrabNSAction();
	private final BufferOrFileVFSSelector inputSelectionPanel;
	private final XPathExpressionPanel expressionPanel;
	private final KeyValuePanel nsPanel;
	private final EvaluatePanel evaluatePanel = new EvaluatePanel();
	private final JTextField dateTypeField = new JTextField();
	private final ResultsPanel resultValuePanel = new ResultsPanel("xpath.result.value");
	private final NodeSetResultsPanel nodeSetTablePanel = new NodeSetResultsPanel("xpath.result.node-set-summary");
	private final XmlFragmentsPanel xmlFragmentsPanel = new XmlFragmentsPanel("xpath.result.xml-fragments");
	
	private JPanel dataTypePanel;
	private boolean autoCompleteEnabled;
	private XPathAdapter adapter;

	public XPathTool(View view) {
		super(new GridBagLayout());
		this.view = view;

		expressionPanel = new XPathExpressionPanel(view);
		nsPanel = new KeyValuePanel("xpath.ns");
		
		inputSelectionPanel = new BufferOrFileVFSSelector(view,"xpath.source");
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(jEdit.getProperty("xpath.result.data-type.label")), BorderLayout.NORTH);
		dateTypeField.setName("xpath.result.data-type");
		panel.add(this.dateTypeField);
		dataTypePanel = new JPanel(new BorderLayout());
		dataTypePanel.add(panel, BorderLayout.NORTH);


		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(inputSelectionPanel, gbc);

		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = gbc.weighty = 1;
		gbc.gridy = 2;
		
		JSplitPane exprNSSplit = getSplitPane(expressionPanel,nsPanel,70);
		add(exprNSSplit, gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NONE;
		add(evaluatePanel, gbc);

		gbc = new GridBagConstraints();
		gbc.gridy = 4;
		gbc.weightx = 1;
		gbc.weighty = 6;
		gbc.fill = GridBagConstraints.BOTH;
		add(getSplitPane(), gbc);
		
	}


	public void focusOnDefaultComponent() {
		expressionPanel.focusOnDefaultComponent();
	}

	/**
	 * Creates parser, parses input source and returns resulting document.
	 */
	public static Document parse(InputSource source) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(xml.Resolver.instance());
		Document document = builder.parse(source);
		return document;
	}


	/**
	 * Returns string result of enclosing expression in a XPath <code>string</code> function and evaluating it.
	 */
	public static String evalString(Document document, String expression) throws TransformerException {
		XObject xObject = XPathAPI.eval(document, "string(" + expression + ")");
		return xObject.xstr().toString();
	}


	/**
	 * Returns string result of enclosing expression in a XPath <code>count</code> function and evaluating it.
	 */
	public static int evalCount(Document document, String expression) throws TransformerException {
		XObject xObject = XPathAPI.eval(document, "count(" + expression + ")");
		return Integer.parseInt(xObject.xstr().toString());
	}

	/**
	 * Clicks the evaluate XPath button.
	 */
	public void clickEvaluateButton() {
		evaluatePanel.button.doClick();
	}


	private XPathAdapter getXPath() {
		String xpath = jEdit.getProperty(XPATH_ADAPTER_PROP);
		if(adapter == null || !adapter.getClass().getName().equals(xpath)) {
			try {
				adapter = (XPathAdapter)Class.forName(xpath).newInstance();
			} catch(ClassNotFoundException e) {
				XSLTPlugin.processException(e,"error instantiating XPath engine",this);
			} catch(InstantiationException e) {
				XSLTPlugin.processException(e,"error instantiating XPath engine",this);
			} catch(IllegalAccessException e) {
				XSLTPlugin.processException(e,"error instantiating XPath engine",this);
			}
		}
		return adapter;
	}
	
	public void actionPerformed(ActionEvent event) {
		if(!inputSelectionPanel.isSourceFileDefined()) {
			GUIUtilities.message(this,"xpath.error.no-source",new Object[]{});
		} else {
			try {
				evaluateExpression();
	
			} catch (IllegalStateException e) {
				XSLTPlugin.processException(e, e.getMessage(), XPathTool.this);
			} catch (SAXException e) { // parse problem
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.buffer-unparseable"), XPathTool.this);
			} catch (IOException e) { // parse problem
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.buffer-unparseable"), XPathTool.this);
			} catch (TransformerException e) { // evaluation problem
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.expression-unevaluateable"), XPathTool.this);
			} catch (Exception e) { // catch-all
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.unkown-problem"), XPathTool.this);
			}
		}
	}


	public Document getCurrentDocument() throws Exception {
		String path = new String();
		InputSource inputSource = null;
		Document document = null;
		DocumentCache docCache = null;

		if (inputSelectionPanel.isFileSelected()) { //take input from file
			path = inputSelectionPanel.getSourceFile();
			return DocumentCache.getFromCache(path);
		} else { // take input from active buffer
			return DocumentCache.getFromCache(view.getBuffer());
		}

	}
	private void evaluateExpression() throws Exception, IOException, SAXException, TransformerException {
		Document document = getCurrentDocument();

		String expression = expressionPanel.getExpression();

		XPathAdapter xpath = getXPath();
		if(xpath != null) {
			XPathAdapter.Result result = xpath.evaluateExpression(document,nsPanel.getMap(),expression);
			Log.log(Log.DEBUG,this,"evaluateExpression returns : "+result);
			
			expressionPanel.addToHistory(expression);
	
			dateTypeField.setText(result.getType());
			setResultValue(result);
			setNodeSetResults(result);
			setXmlFragments(result);
		}
	}


	private JSplitPane getSplitPane() {
		JSplitPane bottomSplitPane = getSplitPane(nodeSetTablePanel, xmlFragmentsPanel, 150);
		JSplitPane middleSplitPane = getSplitPane(resultValuePanel, bottomSplitPane, 80);
		JSplitPane topSplitPane = getSplitPane(dataTypePanel, middleSplitPane, 35);
		return topSplitPane;
	}


	private JSplitPane getSplitPane(final JComponent top, final JComponent bottom, final int dividerLocation) {
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(dividerLocation);
		return splitPane;
	}

	private void setResultValue(final XPathAdapter.Result result) {
		final boolean isNodeSet = result.isNodeSet();
		EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						if(isNodeSet) {
							resultValuePanel.setLabelText(jEdit.getProperty("xpath.result.string-value.label"));
						} else {
							resultValuePanel.setLabelText(jEdit.getProperty("xpath.result.value.label"));
						}
						resultValuePanel.setText(result.getStringValue());
					} catch (Exception e) {
						resultValuePanel.setText("");
					}
					resultValuePanel.resetCaretPosition();
				}
		});
	}


	private void setXmlFragments(XPathAdapter.Result result) throws Exception {
		if (result.isNodeSet()) {
			try {
				XMLFragmentsString xmlFragments = result.toXMLFragmentsString();
				xmlFragmentsPanel.setXmlFragments(xmlFragments);
			} catch (Exception e) {
				xmlFragmentsPanel.setXmlFragments(null);
				throw e;
			}
		} else {
			xmlFragmentsPanel.setXmlFragments(null);
		}
	}


	/**
	 * Populates a node set table model with the results of an XPath evaluation, if
	 * the results are of type node-set.
	 * Note: there are four data types in the XPath 1.0 data model: node-set, string,
	 * number, and boolean.
	 *
	 * @param xObject XObject containing results
	 */
	private void setNodeSetResults(XPathAdapter.Result result) throws Exception {
		NodeSetTableModel tableModel = nodeSetTablePanel.getTableModel();
		if (result.isNodeSet()) {
			tableModel.resetRows(result.size());
			boolean isNodeWithName = false;
			boolean isNodeWithValue = false;
			XPathAdapter.XPathNode node;

			for (int i = 0; i < result.size(); i++) {
				node = result.get(i);

				if (node != null) {
					isNodeWithName = isNodeWithName || node.hasExpandedName();
					isNodeWithValue = isNodeWithValue || node.hasDomValue();

					tableModel.setNodeType(node.getType(), i);
					tableModel.setNodeName(node.getName(), i);
					tableModel.setNodeValue(node.getDomValue(), i);
				}
			}

			if (!isNodeWithName && !isNodeWithValue) {
				tableModel.removeNameOrValueColumn();
			} else if (!isNodeWithName) {
				tableModel.removeNameColumn();
			} else if (!isNodeWithValue) {
				tableModel.removeValueColumn();
			}
		} else {
			tableModel.resetRows(0);
		}
	}

	private void grabNamespaces(){
		if(inputSelectionPanel.isSourceFileDefined()) {
			
			Document document = null;
			try {
				document = getCurrentDocument();
			} catch (SAXException e) { // parse problem
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.buffer-unparseable"), XPathTool.this);
			} catch (IOException e) { // parse problem
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.buffer-unparseable"), XPathTool.this);
			} catch (Exception e) { // catch-all
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.unkown-problem"), XPathTool.this);
			}
			
			if(document != null) {
				Map<String,List<String>> bindings = new HashMap<String,List<String>>();
				
				if (document.getImplementation().hasFeature("traversal", "2.0")) {
					DocumentTraversal traversable = (DocumentTraversal) document;
					NodeIterator iterator = traversable.createNodeIterator(
						document, NodeFilter.SHOW_ELEMENT,null, true);
					
					Node node;
					while ((node = iterator.nextNode()) != null) {
						//Element e = (Element)node;
						NamedNodeMap attrs = node.getAttributes();
						for(int i=0;i<attrs.getLength();i++) {
							Node a  = attrs.item(i);
							
							if("xmlns".equals(a.getPrefix())) {
								String prefix = a.getLocalName();
								String ns = a.getNodeValue();
								List l;
								if(bindings.containsKey(prefix)){
									l = bindings.get(prefix);
								} else {
									l = new ArrayList<String>();
									bindings.put(prefix,l);
								}
								if(!l.contains(ns)) {
									l.add(ns);
								}
							}
						}
					}
					
					Map<String,String> finalMap = new HashMap<String,String>();
					for(Map.Entry<String,List<String>> binding : bindings.entrySet()) {
						String prefix;
						if("".equals(binding.getKey())) {
							prefix = "def";
						} else {
							prefix = binding.getKey();
						}
						int len = binding.getValue().size();
						if(!finalMap.containsKey(prefix) && len == 1) {
								finalMap.put(prefix,binding.getValue().get(0));
						} else {
							for(int i=0,j=0;i<len;j++){
								String uniq = prefix+j;
								if(!finalMap.containsKey(uniq)) {
									finalMap.put(uniq,binding.getValue().get(i));
									i++;
								}
							}
						}
					}
					
					Log.log(Log.DEBUG,this,"found:"+bindings);
					Log.log(Log.DEBUG,this,"found:"+finalMap);
					String[] keys = new String[finalMap.size()];
					String[] values = new String[finalMap.size()];
					int i=0;
					for(Map.Entry<String,String> binding : finalMap.entrySet()) {
						keys[i] = binding.getKey();
						values[i] = binding.getValue();
						i++;
					}
					
					nsPanel.setKeyValues(keys, values);
	
				} else {
					Log.log(Log.ERROR,this,"DomImplementation doesn't support DOM Traversal");
				}
			}
			
		} else {
			Log.log(Log.ERROR,this,"Source isn't defined");
			GUIUtilities.message(this,"xpath.ns.grab.error-no-source",new Object[]{});
		}
			
	}
	
	/**
	 * Panel housing the "Evaluate" button
	 */
	class EvaluatePanel extends JPanel {
		private JButton grabNamespaces;
		private JButton button;
		private JCheckBox autoCompleteCheck;

		EvaluatePanel() {
			
			grabNamespaces = grabNSAction.getButton();

			String iconName = jEdit.getProperty("xpath.evaluate.button.icon");
			String toolTipText = jEdit.getProperty("xpath.evaluate.button.tooltip");
			String shortcut = jEdit.getProperty("xpath.evaluate.shortcut");

			if (shortcut != null) {
				toolTipText += " (" + shortcut + ")";
			}

			URL url = XSLTProcessor.class.getResource(iconName);
			button = new JButton(new ImageIcon(url));
			button.setToolTipText(toolTipText);
			button.addActionListener(XPathTool.this);
			button.setName("xpath.evaluate");
			
			Dimension dimension = new Dimension(76, 30);
			button.setMinimumSize(dimension);
			button.setPreferredSize(dimension);

			add(grabNamespaces);
			add(button);
			autoCompleteCheck = new JCheckBox("Use auto-complete");
			autoCompleteCheck.addItemListener(XPathTool.this);
			add(autoCompleteCheck);
		}
	}

	private class GrabNSAction extends XsltAction {
		
		GrabNSAction(){
			super("xpath.ns.grab");
		}
		
		public void actionPerformed(ActionEvent e) {
			grabNamespaces();
		}
		
	}
	
  	/**
	 * JTextArea that let's tab key change focus to the next component.
	 */
	public class JTextAreaWithoutTab extends JTextArea {
		public boolean isManagingFocus() {
			return false;
		}
	}


	/**
	 * Panel housing the "Results" label & text area
	 */
	class ResultsPanel extends JPanel implements ActionListener {
		protected final JTextArea textArea = new JTextAreaWithoutTab();
		private final JLabel label = new JLabel();

		private JMenuItem searchMenuItem, clearSelectMenuItem;

		ResultsPanel(String name) {
			super(new BorderLayout());
			setLabelText(jEdit.getProperty(name+".label"));
			int width = (int) textArea.getMinimumSize().getWidth();
			int height = (int) textArea.getPreferredSize().getHeight();
			this.textArea.setMinimumSize(new Dimension(width, height));
			this.textArea.setName(name);
			
			add(label, BorderLayout.NORTH);
			add(new JScrollPane(this.textArea));
			
			JPopupMenu popup = new JPopupMenu();
			searchMenuItem = new JMenuItem("Hypersearch");
			searchMenuItem.addActionListener(this);
			popup.add(searchMenuItem);
			clearSelectMenuItem = new JMenuItem("Clear Selection");
			popup.add(clearSelectMenuItem);
			clearSelectMenuItem.addActionListener(this);
			textArea.setComponentPopupMenu(popup);
		}


		void setLabelText(String text) {
			this.label.setText(text);
		}


		void setText(String text) {
			this.textArea.setText(text);
		}


		void resetCaretPosition() {
			this.textArea.setCaretPosition(0);
		}


		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() == searchMenuItem) {
				String text = textArea.getSelectedText();
				if (text == null || text.length() == 0)
					return;
				SearchAndReplace.setSearchString(text);
				SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
				SearchAndReplace.hyperSearch(view);				
			}
			if (evt.getSource() == clearSelectMenuItem) 
				textArea.getHighlighter().removeAllHighlights();
		}

	}


	/**
	 * Handles the selection of a row on the node set results table,
	 * implements interface {@link javax.swing.event.ListSelectionListener}.
	 */
	public void valueChanged(ListSelectionEvent event) {
		int selectedRow = this.nodeSetTablePanel.table.getSelectedRow();
		this.xmlFragmentsPanel.highlightFragment(selectedRow);
	}


	/**
	 * Panel housing the XML fragments results.
	 */
	class XmlFragmentsPanel extends ResultsPanel {

		private XMLFragmentsString xmlFragments;
		private int selected;


		XmlFragmentsPanel(String name) {
			super(name);
		}


		public void highlightFragment(int index) {
			if (this.xmlFragments != null && index >= 0) {
				int startIndex = this.xmlFragments.getFragmentPosition(index);
				int endIndex = this.xmlFragments.getFragmentPosition(index + 1);

				textArea.getHighlighter().removeAllHighlights();

				try {
					textArea.getHighlighter().addHighlight(startIndex, endIndex, new DefaultHighlighter.DefaultHighlightPainter(new Color(200, 200, 200)));
				} catch (BadLocationException e) {
					throw new IllegalArgumentException(e.toString());
				}

				if (this.selected > index) {
					int temp = startIndex;
					startIndex = endIndex;
					endIndex = temp;
				}

				this.selected = index;

				textArea.setCaretPosition(startIndex);
				textArea.moveCaretPosition(endIndex);
			}
		}


		public void setXmlFragments(XMLFragmentsString xmlFragments) {
			this.xmlFragments = xmlFragments;
			this.selected = -1;

			if (xmlFragments == null) {
				setText("");
			} else {
				setText(xmlFragments.getString());
			}

			resetCaretPosition();
		}
	}


	/**
	 * Panel housing the "Results" label & text area
	 */
	class NodeSetResultsPanel extends JPanel {
		private final NodeSetTableModel tableModel = new NodeSetTableModel();
		private final JTable table = new JTable();


		NodeSetResultsPanel(String name) {
			super(new BorderLayout());

			table.getSelectionModel().addListSelectionListener(XPathTool.this);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setModel(tableModel);
			table.setName(name);
			add(new JLabel(jEdit.getProperty(name+".label")), BorderLayout.NORTH);
			JScrollPane tablePane = new JScrollPane(table);
			add(tablePane);
		}


		NodeSetTableModel getTableModel() {
			return this.tableModel;
		}

	}


	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.DESELECTED)
			autoCompleteEnabled = false;
		if (arg0.getStateChange() == ItemEvent.SELECTED)
			autoCompleteEnabled = true;		
	}


	/**
	 * @return Returns the autoCompleteEnabled.
	 */
	public boolean isAutoCompleteEnabled() {
		return autoCompleteEnabled;
	}

	public void handleMessage(EBMessage message) {
		if (!(message instanceof BufferUpdate))
			return;
		BufferUpdate bufUpd = (BufferUpdate)message;
		DocumentCache.handleMessage(bufUpd);
	}
	
	public void stop() {
		DocumentCache.destroyCache();
	}
}

/**
 * 
 * @author Scott Walters
 *
 * Stores XML dom objects for re-use. This is mainly 
 * a performance enhancement so large documents will not
 * have to be re-parsed. Instances of this class are
 * stored in the static map globalDocumentCache. The map is indexed by 
 * the File instance, if the document was read from a file,
 * or by the Buffer instance, if read from a buffer.  
 * 
 * 
 */

class DocumentCache {
	private Document document;
	private long fileUpdateTime;
	private Object sourceObject;
	private static Map globalDocumentCache;
	
	static {
		createCacheMap();
	}

	public static Document getFromCache(JEditBuffer sourceBuffer) throws Exception {
		DocumentCache cacheObj = getCacheObject(sourceBuffer);
		if (cacheObj.getDocument() == null) {
			InputSource inputSource = xml.Resolver.instance().resolveEntity("",((Buffer)sourceBuffer).getPath());				
			cacheObj.doParse(inputSource);
		}
		return cacheObj.getDocument();
	}
	
	public static Document getFromCache(String sourceFileName) throws Exception {
		File sourceFile = new File(sourceFileName);
		DocumentCache cacheObj = getCacheObject(sourceFile);
		if (cacheObj.getDocument() == null
				|| cacheObj.fileUpdateTime != sourceFile.lastModified()) {
			
			InputSource inputSource = new InputSource(sourceFileName);
			cacheObj.doParse(inputSource);
			cacheObj.fileUpdateTime = sourceFile.lastModified();
		}
		return cacheObj.getDocument();
	}
	
	public static void destroyCache() {
		createCacheMap();
	}
	
	public static void handleMessage(BufferUpdate bufUpd) {
		DocumentCache cacheObj = (DocumentCache) globalDocumentCache.get(bufUpd.getBuffer());		
		if (cacheObj == null)
			return;
		cacheObj.processBusMessage(bufUpd);
	}

	private static DocumentCache getCacheObject(Object key) throws Exception {
		DocumentCache cacheObj = (DocumentCache) globalDocumentCache.get(key);
		if (cacheObj != null)
			return cacheObj;
		
		Constructor ctor = 
			DocumentCache.class.getDeclaredConstructor(new Class[]{key.getClass()});
		cacheObj = (DocumentCache) ctor.newInstance(new Object[]{key});
		globalDocumentCache.put(key, cacheObj);
		return cacheObj;
	}
	
	private static void createCacheMap() {
		globalDocumentCache = Collections.synchronizedMap(new HashMap());		
	}
	
	private DocumentCache(Object srcObject) {
		this.sourceObject = srcObject;
	}
	private DocumentCache(Buffer sourceBuffer) {
		this((Object)sourceBuffer);
		sourceBuffer.addBufferListener(
				new BufferAdapter() {
					public void contentInserted(JEditBuffer buffer, 
							int startLine, int offset, 
							int numLines, int length) {
						synchronized(this) {
							if (buffer == sourceObject)
								document = null;
						}
					}
					public void contentRemoved(JEditBuffer buffer, 
							int startLine, int offset, 
							int numLines, int length) {
						synchronized(this) {
							if (buffer == sourceObject)
								document = null;
						}
					}
				});		
	}
	
	private DocumentCache(File sourcefile) {
		this((Object)sourcefile);
		fileUpdateTime = sourcefile.lastModified();
	}
	
	private synchronized void processBusMessage(BufferUpdate bufUpd) {
		Object action = bufUpd.getWhat();
		if (action.equals(BufferUpdate.CLOSED))
			globalDocumentCache.remove(this);
		else if (action.equals(BufferUpdate.LOADED))
			document = null;
	}

	private synchronized void doParse(InputSource src) throws ParserConfigurationException, IOException, SAXException {
		document = XPathTool.parse(src);
	}
	
	public Document getDocument() {
		return document;
	}
	
	
}

