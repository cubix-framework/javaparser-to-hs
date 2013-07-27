/*          DO WHAT THE FRAK YOU WANT TO PUBLIC LICENSE (WTFPL)
                    Version 3, March 2012
	    Based on the wtfpl: http://sam.zoy.org/wtfpl/

 Copyright (C) 2012 Alan Ezust 

 Everyone is permitted to copy and distribute verbatim or modified
 copies of this license document, and changing it is allowed as long
 as the name is changed.

            DO WHAT THE FRAK YOU WANT TO PUBLIC LICENSE
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

  0. You just DO WHAT THE FRAK YOU WANT TO. 
*/


package qt4jedit;

import java.awt.GridBagConstraints;

import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;


public class Qt4jEditOptions extends AbstractOptionPane {
	
	public JTextField pathToAssistantField;
	String oldPath;
	
	public Qt4jEditOptions() {
		super("qt4jedit");
	}

	protected void _init() {
		oldPath = jEdit.getProperty("qt4jedit.path-to-assistant", "assistant");
		pathToAssistantField = new JTextField(oldPath);
		addComponent(jEdit.getProperty("options.qt4jedit.path-to-assistant"), pathToAssistantField);
        
        JTextArea info = new JTextArea();
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		info.setOpaque(false);
		String text = jEdit.getProperty("options.qt4jedit.assistant.info");
		info.append(text);
		info.setEditable(false);
		 
        addComponent(info, GridBagConstraints.BOTH);
        
	}

	protected void _save() {
		String newPath = pathToAssistantField.getText();
		if (newPath != oldPath) {
			jEdit.setProperty("qt4jedit.path-to-assistant", newPath);
			Qt4jEditPlugin.instance().updateCommand();
		}
	}
}

