/*
 * XPathExpressionPanel.java - Holds XPath expression text field
 *
 * Copyright 2004 Pitje
 * 		     2004 Robert McKinnon
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * Panel housing the soure selection options
 * @author Pitje
 * @author Robert McKinnon - robmckinnon@users.sourceforge.net
 */
class XPathInputSelectionPanel extends JPanel implements ActionListener {

	private static final String USE_FILE_INPUT = "xpath.use-file-input";
	private static final String LAST_SOURCE = "xpath.last-source";

	private View view;
	private JRadioButton bufferRadio;
	private JRadioButton fileRadio;
	private JButton browseButton;
	private final JTextField sourceField = new JTextField();
	
	XPathInputSelectionPanel(View view) {
		super(new BorderLayout());
		
		this.view = view;
		boolean useFileInput = jEdit.getBooleanProperty(USE_FILE_INPUT);
		createRadioButtons(useFileInput);
		
		JPanel radioPanel = new JPanel(new BorderLayout());
		radioPanel.add(new JLabel(jEdit.getProperty("xpath.select.label")), BorderLayout.NORTH);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(bufferRadio);
		buttonPanel.add(fileRadio);
		radioPanel.add(buttonPanel, BorderLayout.WEST);

		JPanel sourceFieldPanel = new JPanel(new GridLayout(2,1));
		sourceFieldPanel.add(new JLabel(jEdit.getProperty("xpath.browse.label")));
		
		if(jEdit.getProperty(LAST_SOURCE) == null) {
			sourceField.setText(jEdit.getProperty("xpath.browse.prompt"));
		} else {
			sourceField.setText(jEdit.getProperty(LAST_SOURCE));
		}

		sourceField.setEnabled(useFileInput);
		sourceFieldPanel.add(sourceField);

		createBrowseButton();
		JPanel browseButtonPanel = new JPanel(new BorderLayout());		
		browseButtonPanel.add(browseButton, BorderLayout.SOUTH);
		
		JPanel fileSelectionPanel = new JPanel(new BorderLayout(3, 0));
		fileSelectionPanel.add(sourceFieldPanel);
		fileSelectionPanel.add(browseButtonPanel, BorderLayout.EAST);
		
		add(radioPanel, BorderLayout.NORTH);
		add(fileSelectionPanel, BorderLayout.SOUTH);
	}
	
	private void createRadioButtons(boolean useFileInput) {
		ButtonGroup radioGroup = new ButtonGroup();

		bufferRadio = new JRadioButton(jEdit.getProperty("xpath.select.buffer"));
		bufferRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFileSelectionEnabled(false);
			}
		});

		fileRadio = new JRadioButton(jEdit.getProperty("xpath.select.file"));
		fileRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFileSelectionEnabled(true);
			}
		});

		if(useFileInput) {
			fileRadio.setSelected(true);
		} else {
			bufferRadio.setSelected(true);
		}

		radioGroup.add(bufferRadio);
		radioGroup.add(fileRadio);		
	}
	
	private void createBrowseButton() {
		String iconName = jEdit.getProperty("xpath.browse.button.icon");
		String toolTipText = jEdit.getProperty("xpath.browse.button.tooltip");

		String shortcut = jEdit.getProperty("xpath.select.shortcut");
		
		if(shortcut != null) {
			toolTipText += " (" + shortcut + ")";
		}
		URL url = XSLTProcessor.class.getResource(iconName);
		
		browseButton = new JButton(new ImageIcon(url));
		browseButton.setToolTipText(toolTipText);
		browseButton.addActionListener(this);
		
		Dimension dimension = new Dimension(30, 30);
		browseButton.setMinimumSize(dimension);
		browseButton.setMaximumSize(dimension);
		browseButton.setPreferredSize(dimension);
		browseButton.setEnabled(false);
	}
	
	public String getSourceFieldText() {
		return sourceField.getText();
	};		
	
	public void setFileSelectionEnabled(boolean enableFileSelection) {
		sourceField.setEnabled(enableFileSelection);
		browseButton.setEnabled(enableFileSelection);
		jEdit.setBooleanProperty(USE_FILE_INPUT, enableFileSelection);
	};
	
	public boolean isFileSelected() {
		return fileRadio.isSelected();
	};
	
	public boolean isBufferSelected() {
		return bufferRadio.isSelected();
	};

	public void actionPerformed(ActionEvent event) {	
		String[] selections = GUIUtilities.showVFSFileDialog(view, "", JFileChooser.OPEN_DIALOG, false);
		if(selections != null) {
			sourceField.setText(selections[0]);
			jEdit.setProperty(LAST_SOURCE, selections[0]);
		}
	}
}
