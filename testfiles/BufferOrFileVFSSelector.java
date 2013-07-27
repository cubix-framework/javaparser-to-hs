/*
 * BufferOrFileVFSSelector.java
 *
 * Copyright 2010 Eric Le Lay
 * abstracted from InputSelectionPanel.java@r17377
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
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JComponent;
import javax.swing.Box;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;


import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

/**
 * buffer/file radio buttons + a text field and a "browse" button to select
 * the file.
 * @author Eric Le Lay - kerik-sf@users.sourceforge.net
 */
public class BufferOrFileVFSSelector extends BufferOrFileSelector {
	private static final String LAST_SOURCE = ".last-source";

	private View view;
	
	private JTextField sourceField;
	private JButton browseButton;
	private XsltAction sourceSelectAction;
	private XsltAction openFileAction;
	
	BufferOrFileVFSSelector(View view, String propertyPrefix) {
		super (propertyPrefix);
		this.view = view;
		sourceSelectAction = new SourceSelectAction(propertyPrefix);
		openFileAction = new OpenFileAction();
		setSourceField(initSourceField());
		setFileSelectionEnabled(isFileSelected());
	}
	

	private JComponent initSourceField() {
		sourceField = new JTextField();
		InputMap im = sourceField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = sourceField.getActionMap();
        im.put(KeyStroke.getKeyStroke("ENTER"), "OpenFile");
        am.put("OpenFile", openFileAction);
		
        sourceField.setName(propertyPrefix+".prompt");
		sourceField.setComponentPopupMenu(initMenu());

		String lastSource = jEdit.getProperty(propertyPrefix+LAST_SOURCE);
		if(lastSource == null) {
			sourceField.setText(jEdit.getProperty(propertyPrefix+".browse.prompt"));
		} else {
			sourceField.setText(lastSource);    
		}
		
		sourceField.setEnabled(false); //because buffer is primarily selected
		
		browseButton = sourceSelectAction.getButton();
		browseButton.setEnabled(false); //because buffer is primarily selected
		
		Box container = Box.createHorizontalBox();
		container.add(sourceField);
		container.add(Box.createHorizontalStrut(6));
		container.add(browseButton);
		return container;
	}
	
	private void chooseFile() {
		String path = null;

		if(getSourceFile() != null && !getSourceFile().equals(""))
			path = MiscUtilities.getParentOfPath(getSourceFile());

		String[] selections;
		
		if(getTopLevelAncestor() != view && getTopLevelAncestor() instanceof JFrame){
			selections = GUIUtilities.showVFSFileDialog((JFrame)getTopLevelAncestor(), view, path, JFileChooser.OPEN_DIALOG, false);
		}else {
			selections = GUIUtilities.showVFSFileDialog(view, path, JFileChooser.OPEN_DIALOG, false);
		}

		if(selections != null) {
				setSourceFile(selections[0]);
		}

		Container topLevelAncestor = getTopLevelAncestor();

		if(topLevelAncestor instanceof JFrame) {
			((JFrame)topLevelAncestor).toFront();
		}
	}
	
	public String getSourceFile() {
		return sourceField.getText();
	}
	
	void setSourceFile(String sourceFile) {
		sourceField.setText(sourceFile);
		jEdit.setProperty(propertyPrefix+LAST_SOURCE, sourceFile);
	}

	public boolean isSourceFileDefined() {
		return !isFileSelected()
			|| !getSourceFile().equals(jEdit.getProperty(propertyPrefix+".browse.prompt"));
	}
	
	protected void setFileSelectionEnabled(boolean enabled){
		browseButton.setEnabled(enabled);
		sourceField.setEnabled(enabled);
	}

	private JPopupMenu initMenu() {
		XsltAction[] actions = new XsltAction[] {openFileAction, null, sourceSelectAction};
		return XsltAction.initMenu(actions);
	}
	
	private void openFile(){
		String file = getSourceFile();
		if (file != null 
			&& !file.equals(jEdit.getProperty(propertyPrefix+".browse.prompt")))
		{
			jEdit.openFile(view, file);
		}
	}
	
	private class SourceSelectAction extends XsltAction {
		
		SourceSelectAction(String propertyPrefix){
			super(propertyPrefix+".select");
		}

		public void actionPerformed(ActionEvent e) {
			chooseFile();
		}
	}

	private class OpenFileAction extends XsltAction {

		OpenFileAction() {
			super("xslt.file.open");
		}

		public void actionPerformed(ActionEvent e) {
			openFile();
		}

	}

}