/*
 * Created on Dec 9, 2003
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */
package xquery;


import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/** This is an extension of the Abstract class selectionPanel
 * @author Wim le Page
 * @author Pieter Wellens
 * @version 0.6.0
 *
 */

public class FileSelectionPanel extends SelectionPanel  {
	
	public FileSelectionPanel(View view, String propLabel) {
		super(view, propLabel);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent event) {
		  String[] selections = GUIUtilities.showVFSFileDialog(jEdit.getActiveView(), "", JFileChooser.OPEN_DIALOG, false);
		  if(selections != null) {
			sourceField.setText(selections[0]);
			jEdit.setProperty(propLabel + ".last-source", selections[0]);
		  }	
	}		
}
