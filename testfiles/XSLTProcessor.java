/*
 * XSLTProcessor.java - GUI for performing XSL Transformations
 *
 * Copyright 2002 Greg Merrill
 *           2002, 2003 Robert McKinnon
 *           2010 Eric Le Lay
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
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.Writer;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JCheckBox;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.util.Log;

import errorlist.*;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

/**
 * GUI for performing XSL Transformations.
 *
 * @author Greg Merrill
 * @author Robert McKinnon - robmckinnon@users.sourceforge.net
 * @author Eric Le Lay
 */
public class XSLTProcessor extends JPanel implements DefaultFocusComponent {
	public static final String  DOCKABLE_NAME = "xslt-processor";
	private static final String OPEN_RESULT = "xslt.open-result";
	private static final String THREE_WAY   = "xslt.three-way";

	private View view;
	private BufferOrFileVFSSelector resultPanel;
	private XsltAction saveSettingsAction = new SaveSettingsAction();
	private XsltAction loadSettingsAction = new LoadSettingsAction();
	private XsltAction transformAction = new TransformAction();

	private BufferOrFileVFSSelector inputSelectionPanel;
	private StylesheetPanel stylesheetPanel;
	private KeyValuePanel parameterPanel;
	private JButton transformButton;
	private JCheckBox openResultCheckBox;
	private JCheckBox threeWayCheckBox;

	/**
	 * Constructor for the XSLTProcessor object.
	 *
	 */
	public XSLTProcessor(View theView, String position) {
		super();

		this.view = theView;
		boolean sideBySide = position.equals(DockableWindowManager.TOP) || position.equals(DockableWindowManager.BOTTOM);

		initThreeWayCheckBox();
		initOpenResultCheckBox();
		this.resultPanel = new BufferOrFileVFSSelector(view,"xslt.result");
		this.inputSelectionPanel = new BufferOrFileVFSSelector(view,"xslt.source");
		this.stylesheetPanel = new StylesheetPanel(view, this);
		this.parameterPanel = new KeyValuePanel("xslt.parameters");

		JComponent transformPanel = initTransformToolBar();

		if (sideBySide) {
			createHorizontalLayout(inputSelectionPanel, transformPanel);
		} else {
			createVerticalLayout(inputSelectionPanel, transformPanel);
		}

		setThreeWay(threeWayCheckBox.isSelected());

	}

	public void focusOnDefaultComponent() {
		if(transformButton != null) {
			transformButton.requestFocus();
		}
	}

	private void createVerticalLayout(JComponent sourcePanel, JComponent transformPanel) {
		setLayout(new GridBagLayout());
		
		sourcePanel.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
		stylesheetPanel.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
		transformPanel.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
		resultPanel.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
		openResultCheckBox.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.BOTH;

		add(threeWayCheckBox,c);

		c.gridy += c.gridheight;
		c.gridheight = 4;
		add(sourcePanel,c);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, stylesheetPanel, parameterPanel);
		splitPane.setOneTouchExpandable(true);
		
		c.gridy += c.gridheight;
		c.gridheight = 10;
		c.weighty = 1;
		add(splitPane,c);
		
		c.gridy += c.gridheight;
		c.gridheight = 1;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		add(transformPanel,c);
		c.gridy += c.gridheight;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		add(resultPanel,c);
		c.gridy += c.gridheight;
		c.gridheight = 1;
		c.fill = GridBagConstraints.NONE;
		add(openResultCheckBox,c);
	}


	private void createHorizontalLayout(JComponent sourcePanel, JComponent transformPanel) {
		sourcePanel.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
		stylesheetPanel.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
		transformPanel.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
		resultPanel.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));
		openResultCheckBox.setBorder(BorderFactory.createEmptyBorder(6,0,0,0));

		JPanel sourceAndResultPanel = new JPanel(new BorderLayout());
		sourceAndResultPanel.add(sourcePanel, BorderLayout.NORTH);
		sourceAndResultPanel.add(resultPanel, BorderLayout.CENTER);
		sourceAndResultPanel.add(openResultCheckBox, BorderLayout.SOUTH);

		JPanel extraParameterPanel = new JPanel(new BorderLayout());
		extraParameterPanel.add(threeWayCheckBox,BorderLayout.NORTH);
		extraParameterPanel.add(parameterPanel, BorderLayout.CENTER);
		extraParameterPanel.add(transformPanel, BorderLayout.SOUTH);
		
		JSplitPane split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,stylesheetPanel,extraParameterPanel);
		split1.setDividerLocation(0.5);
		split1.setResizeWeight(0.5);
		split1.setOneTouchExpandable(true);
		JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,split1,sourceAndResultPanel);
		split2.setDividerLocation(0.66);
		split2.setResizeWeight(0.66);
		split2.setOneTouchExpandable(true);

//		setLayout(new GridBagLayout());
//		add(stylesheetPanel);
//		add(extraParameterPanel);
//		add(panel);
		setLayout(new BorderLayout());
		add(split2,BorderLayout.CENTER);
	}


	/**
	 * Performs the transform action.
	 */
	public void clickTransformButton() {
		transformAction.actionPerformed(null);
	}


	/**
	 * Performs the load settings action.
	 */
	public void clickLoadSettingsButton() {
		loadSettingsAction.actionPerformed(null);
	}


	/**
	 * Performs the save settings action.
	 */
	public void clickSaveSettingsButton() {
		saveSettingsAction.actionPerformed(null);
	}


	public boolean isThreeWay() {
		return threeWayCheckBox.isSelected();
	}
	
	public void setThreeWay(boolean threeWay) {
		inputSelectionPanel.setEnabled(!threeWay);
		stylesheetPanel.setEnabled(!threeWay);
		resultPanel.setEnabled(!threeWay);
		openResultCheckBox.setEnabled(!threeWay);
		if(threeWayCheckBox.isSelected() != threeWay) {
			threeWayCheckBox.setSelected(threeWay);
		}
	}
	
	private void initThreeWayCheckBox(){

		threeWayCheckBox = new JCheckBox(jEdit.getProperty("xslt.three-way.label"));
		threeWayCheckBox.setSelected(jEdit.getBooleanProperty(THREE_WAY));
		threeWayCheckBox.setName("three-way");
		threeWayCheckBox.setToolTipText("xslt.three-way.tooltip");
		
		// save preference for next time
		threeWayCheckBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					setThreeWay(threeWayCheckBox.isSelected());
					jEdit.setBooleanProperty(THREE_WAY,threeWayCheckBox.isSelected());
				}
			});
	}
	
	private void initOpenResultCheckBox() {

		openResultCheckBox = new JCheckBox(jEdit.getProperty("xslt.result.open-result.label"));
		openResultCheckBox.setSelected(jEdit.getBooleanProperty(OPEN_RESULT));
		openResultCheckBox.setName("open-result");
		openResultCheckBox.setToolTipText("xslt.result.open-result.tooltip");
		
		// save preference for next time
		openResultCheckBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					jEdit.setBooleanProperty(OPEN_RESULT,openResultCheckBox.isSelected());
				}
			});
	}


	private JComponent initTransformToolBar() {

		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
		toolBar.setFloatable(false);

		toolBar.add(loadSettingsAction.getButton());
		toolBar.add(saveSettingsAction.getButton());
		toolBar.addSeparator();
		transformButton = transformAction.getButton();
		toolBar.add(transformButton);

		return toolBar;
	}


	public View getView() {
		return view;
	}

	public BufferOrFileVFSSelector getInputSelectionPanel() {
		return inputSelectionPanel;
	}

	public StylesheetPanel getStylesheetPanel() {
		return stylesheetPanel;
	}


	public KeyValuePanel getParameterPanel() {
		return parameterPanel;
	}

	public BufferOrFileVFSSelector getResult() {
		return this.resultPanel;
	}

	public String getResultFile() {
		if(isThreeWay()){
			int[] iBuffers = getThreeWayBuffers();
			return view.getEditPanes()[iBuffers[2]].getBuffer().getPath();
		} else if(getResult().isFileSelected()) {
			return getInputSelectionPanel().getSourceFile();
		} else {
			Buffer b = jEdit.newFile(view);
			String path = b.getPath();
			jEdit.closeBuffer(view,b);
			return path;
		}
	}

	public void setStylesheets(String[] stylesheets) {
		this.stylesheetPanel.setStylesheets(stylesheets);
	}


	public void setStylesheetParameters(String[] names, String[] values) {
		this.parameterPanel.setKeyValues(names, values);
	}


	/**
	 * Attempts to load settings from a user specified file.
	 */
	private void loadSettings() {
		try {
			XsltSettings settings = new XsltSettings(this);
			settings.loadFromFile();
		} catch (Exception e) {
			Log.log(Log.ERROR, this, e.toString());
		}
	}


	/**
	 * Attempts to save settings to a user specified file.
	 */
	private void saveSettings() {
		try {
			XsltSettings settings = new XsltSettings(this);
			settings.writeToFile();
		} catch (IOException e) {
			Log.log(Log.ERROR, this, e.toString());
		}
	}

	public boolean shouldOpenResult() {
		return openResultCheckBox.isSelected();
	}

	private void transform() {
		if(isThreeWay()) {
			if(view.getEditPanes().length != 3) {
				XSLTPlugin.showMessageDialog("xslt.transform.message.three-edit-panes", this);
			} else {
				doTransform();
			}
		} else if (!getStylesheetPanel().stylesheetsExist()) {
			XSLTPlugin.showMessageDialog("xslt.transform.message.no-stylesheets", this);
		} else if (!inputSelectionPanel.isSourceFileDefined()) {
			XSLTPlugin.showMessageDialog("xslt.transform.message.no-source", this);
		} else if (!resultPanel.isSourceFileDefined()) {
			XSLTPlugin.showMessageDialog("xslt.transform.message.no-result", this);
		} else {
			doTransform();
		}
	}


	private void doTransform() {
		ErrorListenerToErrorList listener = new ErrorListenerToErrorList("");
		try {
			Date start = new Date();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			String path;
			InputSource inputSource;
			boolean outputToBuffer;
			Buffer outputBuffer;
			Result result;
			Writer outputWriter;
			File resultFile;
			Object[] stylesheets;
			
			if (isThreeWay()) {
				EditPane[] editPanes = view.getEditPanes();
				int[] iBuffers = getThreeWayBuffers();
				
				// input
				path = editPanes[iBuffers[0]].getBuffer().getPath();
				
				// stylesheet
				stylesheets = new Object[]{editPanes[iBuffers[1]].getBuffer().getPath()};
				
				// output
				outputBuffer = editPanes[iBuffers[2]].getBuffer();
				
				if(outputBuffer.isReadOnly()){
					//put the result in the correct edit pane
					outputBuffer = jEdit.newFile(editPanes[iBuffers[2]]);
				}
				
				outputToBuffer = true;
				outputWriter = new StringWriter();
				result = new StreamResult(outputWriter);
				result.setSystemId(xml.PathUtilities.pathToURL(outputBuffer.getPath()));
				resultFile = null;

			} else {
				if (inputSelectionPanel.isFileSelected()) {
					path = inputSelectionPanel.getSourceFile();
				} else {
					Buffer buffer = view.getBuffer();
					path = buffer.getPath();
				}
				
				// get the stylesheets now, before the current buffer has changed
				stylesheets = getStylesheets();
				
				outputToBuffer = !resultPanel.isFileSelected();
				if(outputToBuffer) {
					resultFile = null;
					outputBuffer = jEdit.newFile(view);
					outputWriter = new StringWriter();
					result = new StreamResult(outputWriter);
					result.setSystemId(xml.PathUtilities.pathToURL(outputBuffer.getPath()));
				} else {
					resultFile = new File(resultPanel.getSourceFile());
					outputBuffer = jEdit.getBuffer(resultFile.getAbsolutePath());
					outputWriter = new FileWriter(resultFile);
					result = new StreamResult(outputWriter);
					result.setSystemId(resultFile.toURI().toString());
				}
			}

			// clear any existing error
			XSLTPlugin.getErrorSource().clear();
			
			inputSource = xml.Resolver.instance().resolveEntity(/*publicId*/null,path);
			
			XSLTUtilities.transform(inputSource, stylesheets , getStylesheetParameters(), result, listener);

			// close the output (is it necessary ?)
			outputWriter.flush();
			outputWriter.close();
			
			if(outputToBuffer){
				// pull the content to the Untitled buffer
				outputBuffer.remove(0,outputBuffer.getLength());
				outputBuffer.insert(0,outputWriter.toString());
			}else if (outputBuffer == null) {
				// output to file, which wasn't open before
				if(shouldOpenResult()) {
					jEdit.openFile(view, resultFile.getAbsolutePath());
				}
			} else {
				// output to file, which was already open
				outputBuffer.reload(view);
			}

			logTimeTaken(start);

		} catch(SAXParseException spe){
			listener.sendSAXError(spe);
			String message = jEdit.getProperty("xslt.transform.message.failure");
			XSLTPlugin.processException(spe, message, XSLTProcessor.this);

		} catch (Exception e) {
			String message = jEdit.getProperty("xslt.transform.message.failure");
			XSLTPlugin.processException(e, message, XSLTProcessor.this);
		} finally {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public int[] getThreeWayBuffers(){
		int[] res = new int[3];
		
		Buffer[] buffers = new Buffer[3];
		EditPane[] editPanes = view.getEditPanes();
		int iXML = -1,iXSLT=-1, iResult = -1;
		
		for(int i=0;i<3;i++) {
			buffers[i] = editPanes[i].getBuffer();
			if("xsl".equals(buffers[i].getMode().getName())) {
				// why should it be Source XSL -> XSL Transform -> result
				// and not Source XML -> XSL Transform -> XSL result ?
				// So the canonical ordering is used when there are 2 XSL
				if(iXSLT == -1){
					iXSLT = i;
				}else{
					iXSLT = 3;
				}
			} else if(iXML == -1 && "xml".equals(buffers[i].getMode().getName())) {
				// only the first XML buffer will be seen as input
				iXML = i;
			} else {
				iResult = i;
			}
		}
		
		if(iXML == -1 ||  iXSLT == -1 || iXSLT == 3) {
			//ambiguous, so resort to default positioning
			iXML = 0;
			iXSLT = 1;
			iResult = 2;
		}
		
		// input
		res[0] = iXML;
		
		// stylesheet
		res[1] = iXSLT;
		
		// output
		res[2] = iResult;
		
		return res;
	}
	
	
	public String getInput() {
		if(isThreeWay()){
			int[] iBuffers = getThreeWayBuffers();
			return view.getEditPanes()[iBuffers[0]].getBuffer().getPath();
		} else if(getInputSelectionPanel().isFileSelected()) {
			return getInputSelectionPanel().getSourceFile();
		} else {
			return view.getBuffer().getPath();
		}
	}
	
	public Object[] getStylesheets() {
		if(isThreeWay()){
			int[] iBuffers = getThreeWayBuffers();
			return new Object[]{ (view.getEditPanes()[iBuffers[1]]).getBuffer().getPath()};
		} else if (getStylesheetPanel().isFileSelected()) {
			return getStylesheetPanel().getStylesheets();
		} else {
			Buffer buffer = view.getBuffer();
			return new Object[]{buffer.getPath()};
		}
	}


	private Map getStylesheetParameters() {
		return getParameterPanel().getMap();
	}


	private void logTimeTaken(Date start) {
		Date end = new Date();
		long timeTaken = end.getTime() - start.getTime();
		long secondsTaken = timeTaken / 1000;
		long partialSecondsTaken = timeTaken % 1000;
		Object[] param = {secondsTaken + "." + partialSecondsTaken};
		String status = jEdit.getProperty("xslt.transform.message.success", param);
		Log.log(Log.MESSAGE, this, status);
	}

	private class TransformAction extends XsltAction {
		
		TransformAction() {
			super("xslt.transform");
		}

		public void actionPerformed(ActionEvent e) {
			transform();
		}

		protected Dimension getButtonDimension() {
			Dimension dimension = new Dimension(74, 30);
			return dimension;
		}
	}


	private class SaveSettingsAction extends XsltAction {
		
		SaveSettingsAction() {
			super("xslt.settings.save");
		}

		public void actionPerformed(ActionEvent e) {
			saveSettings();
		}

	}


	private class LoadSettingsAction extends XsltAction {
		
		LoadSettingsAction() {
			super("xslt.settings.load");
		}
		
		public void actionPerformed(ActionEvent e) {
			loadSettings();
		}

	}

}