package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.CheckBoxWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.util.FindReplace;
import org.columba.core.util.IEditableText;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@SuppressWarnings("serial")
public class FindReplaceDialog extends JDialog implements ActionListener{
	private static final String RESOURCE_PATH = "org.columba.core.i18n.dialog";
	
	private LabelWithMnemonic findLabel;
	private LabelWithMnemonic replaceLabel;
	
	private ButtonWithMnemonic findButton;
	private ButtonWithMnemonic replaceButton;
	private ButtonWithMnemonic replaceAllButton;
	private ButtonWithMnemonic closeButton;
	
	private CheckBoxWithMnemonic wholeWordCheckBox;
	private CheckBoxWithMnemonic caseSensCheckBox;
	private CheckBoxWithMnemonic findBackCheckBox;
	
	private JTextField findTextField;
	private JTextField replaceWithTextField;

	private IEditableText text;
	private FindReplace finder = new FindReplace();
	
	public FindReplaceDialog(IEditableText text) {
		setTitle(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","findreplace.title"));
		this.text = text;
		initComponents();
		setResizable(false);
		pack();
		setVisible(true);
	}
	
	private void createComponents(){
		
		findLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","findlabel"));
		replaceLabel = new LabelWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","replacewithlabel"));
		
		findTextField = new JTextField(16);
		replaceWithTextField = new JTextField(16);

		wholeWordCheckBox = new CheckBoxWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","matchword"));
		caseSensCheckBox = new CheckBoxWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","matchcase"));
		findBackCheckBox = new CheckBoxWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","findbackwards"));

		findButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","find"));
		replaceButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","replace"));
		replaceAllButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","replaceall"));
		closeButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","close"));

	}
	private void initComponents(){
		
		FormLayout layout = new FormLayout("pref, 6dlu, pref", // columns 
        "pref, 3dlu, pref"); // rows
		
		CellConstraints cc = new CellConstraints();
		
		createComponents();
		
		JPanel panelMain = new JPanel(layout);
		panelMain.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
		
		panelMain.add(createInputPanel(), cc.xy(1,1));
		panelMain.add(createButtonPanel(),cc.xywh(3,1,1,3));
		panelMain.add(createOptionPanel(),cc.xy(1,3));
		
		getContentPane().add(panelMain,BorderLayout.SOUTH);
		getContentPane().add(
				new DialogHeaderPanel(GlobalResourceLoader.getString(RESOURCE_PATH,
						"findreplace", "header_title"), GlobalResourceLoader
						.getString(RESOURCE_PATH, "findreplace", "header_description"),
						ImageLoader.getIcon(IconKeys.SEARCH)),
				BorderLayout.NORTH);
	}

	private JPanel createInputPanel(){
		FormLayout layout = new FormLayout("left:pref, 6dlu, left:pref",
				"pref, 3dlu, pref");
		CellConstraints cc = new CellConstraints();
		JPanel inputPanel = new JPanel(layout);
		
		inputPanel.add(findLabel,cc.xy(1,1));
		findLabel.setLabelFor(findTextField);
		inputPanel.add(findTextField,cc.xy(3,1));
		
		inputPanel.add(replaceLabel,cc.xy(1,3));
		replaceLabel.setLabelFor(replaceWithTextField);
		inputPanel.add(replaceWithTextField,cc.xy(3,3));
		
		return inputPanel;
	}
	
	private JPanel createOptionPanel(){
		// create a panel with a border and 3 options
		FormLayout layout = new FormLayout("3dlu, left:pref, 30dlu, right:pref, 3dlu", // columns 
        	"pref, 3dlu, pref"); // rows
		
		CellConstraints cc = new CellConstraints();
		
		JPanel optionPanel = new JPanel(layout);
		optionPanel.setBorder(BorderFactory.createTitledBorder(GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","options")));
	
		optionPanel.add(caseSensCheckBox,cc.xy(2,1));
		optionPanel.add(wholeWordCheckBox,cc.xywh(2,3,3,1));
		optionPanel.add(findBackCheckBox,cc.xy(4,1));
		
		return optionPanel;
	}


	private JPanel createButtonPanel(){
		// create a panel with all 4 buttons of the window
		FormLayout layout = new FormLayout("fill:pref", // columns 
			"pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel buttonPanel = new JPanel(layout);
		
		
		findButton.addActionListener(this);
		findButton.setActionCommand("FIND");
		this.getRootPane().setDefaultButton(findButton);

		replaceButton.addActionListener(this);
		replaceButton.setActionCommand("REPLACE");

		replaceAllButton.addActionListener(this);
		replaceAllButton.setActionCommand("REPLACEALL");

		closeButton.addActionListener(this);
		closeButton.setActionCommand("CLOSE");

		buttonPanel.add(findButton,cc.xy(1,1));
		buttonPanel.add(replaceButton,cc.xy(1,3));
		buttonPanel.add(replaceAllButton,cc.xy(1,5));
		buttonPanel.add(closeButton,cc.xy(1,7));
		
		return buttonPanel;
	}
	
	
	public void actionPerformed(ActionEvent e) {
		// if some action was performed
		String action = e.getActionCommand();

		String source = text.getText();

		// setup the finder
		finder.setPattern(findTextField.getText());
		finder.setSource(source);
		finder.setCaseSensitive(caseSensCheckBox.isSelected());
		finder.setMatchWholeWord(wholeWordCheckBox.isSelected());
		finder.setBackwards(findBackCheckBox.isSelected());
		finder.setReplaceWith(replaceWithTextField.getText());
		
		// find action performed
		if (action.equals("FIND")) {
			int position = finder.findnext();
			if(position>=0) {
				// mark found pattern
				mark(position,position + findTextField.getText().length());
				//text.grabFocus();
			} else
			{
				// delete marks
				mark(0, 0);
				JOptionPane.showMessageDialog(this, GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","string_not_found"));
			}
		}
		//replace action performed
		else if (action.equals("REPLACE")){
			int position = finder.replace();
			if (position>=0) {
				//text.grabFocus();
				text.setText(finder.getSource());
				mark(position,position + replaceWithTextField.getText().length());
			}
			else
			{
				mark(0, 0);
				JOptionPane.showMessageDialog(this, GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","string_not_found"));
			}
		}
		//replaceall action performed
		else if (action.equals("REPLACEALL")){
			finder.replaceAll();
			if (finder.getSource().equals(text.getText())) { 
				//text.grabFocus();
				text.setText(finder.getSource());
				JOptionPane.showMessageDialog(this, GlobalResourceLoader.getString(RESOURCE_PATH,"findreplace","string_not_found"));
			}
			else 
				text.setText(finder.getSource());
		}
		else if (action.equals("CLOSE")){
			this.dispose();
		}
	}
	
	// to mark a text in the JTextPane
	private void mark(int start, int end) {
		text.setCaretPosition(start);
		text.moveCaretPosition(end);
	}
}
