/*
 * VFSFileTextField.java
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

package xml.gui;

import java.awt.BorderLayout;
import java.awt.Container;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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

/**
 * a text field and a "browse" button to select the file.
 * @author Eric Le Lay - kerik-sf@users.sourceforge.net
 */
public class VFSFileTextField extends JPanel{
	private static final String LAST_SOURCE = ".last-source";

	private View view;
	private String propertyPrefix;
	
	private JTextField sourceField;
	private JButton browseButton;
	private UsefulAction sourceSelectAction;
	private UsefulAction openFileAction;
	
	public VFSFileTextField(View view, String propertyPrefix) {
		super(new BorderLayout());
		this.view = view;
		this.propertyPrefix = propertyPrefix;
		
		sourceSelectAction = new SourceSelectAction(propertyPrefix);
		openFileAction = new OpenFileAction();
		add(initSourceField(),BorderLayout.CENTER);
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

		sourceField.addFocusListener(new FocusListener(){
				private String valueOnEnter;
				
				public void focusGained(FocusEvent fe){
					valueOnEnter = sourceField.getText();
				}
				
				public void focusLost(FocusEvent fe){
					String valueOnExit = sourceField.getText();
					if(!valueOnExit.equals(valueOnEnter)){
						VFSFileTextField.this.firePropertyChange("file",valueOnEnter,valueOnExit);
					}
				}
		});
		browseButton = sourceSelectAction.getButton();
		
		Box container = Box.createHorizontalBox();
		container.add(sourceField);
		container.add(Box.createHorizontalStrut(6));
		container.add(browseButton);
		return container;
	}
	
	private void chooseFile() {
		String path = null;

		if(getFile() != null && !getFile().equals(""))
			path = MiscUtilities.getParentOfPath(getFile());

		String[] selections;
		
		Container topLevelAncestor = getTopLevelAncestor();

		if(topLevelAncestor != view && topLevelAncestor instanceof JFrame){
			selections = GUIUtilities.showVFSFileDialog((JFrame)topLevelAncestor, view, path, JFileChooser.OPEN_DIALOG, false);
		}else {
			selections = GUIUtilities.showVFSFileDialog(view, path, JFileChooser.OPEN_DIALOG, false);
		}

		if(selections != null) {
				setFile(selections[0]);
		}


		if(topLevelAncestor instanceof JFrame) {
			((JFrame)topLevelAncestor).toFront();
		}
	}
	
	public String getFile() {
		return sourceField.getText();
	}
	
	public void setFile(String file) {
		sourceField.setText(file);
		jEdit.setProperty(propertyPrefix+LAST_SOURCE, file);
		firePropertyChange("file",null,file);        
	}

	public boolean isFileDefined() {
		return !getFile().equals(jEdit.getProperty(propertyPrefix+".browse.prompt"));
	}
	
	@Override
	public void setEnabled(boolean enabled){
		browseButton.setEnabled(enabled);
		sourceField.setEnabled(enabled);
	}

	private JPopupMenu initMenu() {
		UsefulAction[] actions = new UsefulAction[] {openFileAction, null, sourceSelectAction};
		return UsefulAction.initMenu(actions);
	}
	
	private void openFile(){
		String file = getFile();
		if (file != null 
			&& !file.equals(jEdit.getProperty(propertyPrefix+".browse.prompt")))
		{
			jEdit.openFile(view, file);
		}
	}
	
	private class SourceSelectAction extends UsefulAction {
		
		SourceSelectAction(String propertyPrefix){
			super(propertyPrefix+".select");
		}

		public void actionPerformed(ActionEvent e) {
			chooseFile();
		}
	}

	private class OpenFileAction extends UsefulAction {

		OpenFileAction() {
			super("xml.gui.file.open");
		}

		public void actionPerformed(ActionEvent e) {
			openFile();
		}

	}

}