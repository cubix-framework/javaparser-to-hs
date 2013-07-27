package clearcase;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.StringTokenizer;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class AddToSourceControlDialog extends JDialog implements ActionListener, FocusListener
{
    String comment;
	public static final String DEFAULT_COMMENT = "I am adding this because...";
	
    /**
    * The exit value for this dialog "Cancel" or "Ok"
    */
    String exitValue = "Cancel";
    View view;
    BorderLayout mainPanelLayout = new BorderLayout();
    TitledBorder titledBorder1;
    BorderLayout centerPanelLayout = new BorderLayout();
    JPanel mainPanel = new JPanel();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel centerPanel = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JPanel southPanel = new JPanel();
    JButton buttonCancel = new JButton();
    JButton buttonOk = new JButton();
    JCheckBox keepCheckedOut = new JCheckBox();
    JCheckBox checkOutParentDirectory = new JCheckBox();
	JTextArea commentText = new JTextArea();
	JPanel checkBoxPanel = new JPanel();
	
    public AddToSourceControlDialog(View view)
    {
		super(view, "Add to Source Control", true);
        this.view = view;
		view.showWaitCursor();
        init();
		pack();
		setLocationRelativeTo(view);
		view.hideWaitCursor();
		show();
    }
/*
    public static void main(String[] args)
    {
        AddToSourceControlDialog AddToSourceControlDialog = new AddToSourceControlDialog();
        AddToSourceControlDialog.pack();
        AddToSourceControlDialog.show();
        System.exit(0);
    }
*/
    private void init()
    {
        titledBorder1 = new TitledBorder("");
        setContentPane(mainPanel);
        
        mainPanel.setLayout(mainPanelLayout);
        checkBoxPanel.setLayout(new GridLayout(2, 1));
        centerPanel.setLayout(centerPanelLayout);
        centerPanel.setBorder(titledBorder1);
        southPanel.setPreferredSize(new Dimension(40, 40));

        commentText.setText(DEFAULT_COMMENT);
        commentText.setTabSize(0);
		commentText.addFocusListener(this);
		
        buttonOk.setText("Ok");
        buttonOk.addActionListener(this);
        buttonOk.setPreferredSize(new Dimension(73, 27));
        
        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(this);
        buttonCancel.setPreferredSize(new Dimension(73, 27));
        
        mainPanel.setPreferredSize(new Dimension(300, 240));
        keepCheckedOut.setText("Keep checked out");
        checkOutParentDirectory.setText("Check out/in parent directory");
        
        mainPanel.add(centerPanel,  BorderLayout.CENTER);
        checkBoxPanel.add(keepCheckedOut);
        checkBoxPanel.add(checkOutParentDirectory);
        centerPanel.add(checkBoxPanel, BorderLayout.SOUTH);
        centerPanel.add(commentText, BorderLayout.CENTER);

        mainPanel.add(southPanel,  BorderLayout.SOUTH);
        southPanel.add(buttonOk, null);
        southPanel.add(buttonCancel, null);
    }


    public boolean getKeepCheckedOut()
    {
        return keepCheckedOut.isSelected();
    }
    
    public boolean getCheckOutInParentDirectory()
    {
        return checkOutParentDirectory.isSelected();
    }
    
    public String getExitValue()
    {
        return exitValue;
    }
    
	public String getComment ()
	{
		return comment;
	}
	
    public void actionPerformed(ActionEvent event) 
    {
        String command = event.getActionCommand();

        if(command.equals("Ok"))
        {
            StringTokenizer newlineStripper = new StringTokenizer (commentText.getText(), "\n");
			StringBuffer strippedComment = new StringBuffer();
			while (newlineStripper.hasMoreElements())
			{
				strippedComment.append(newlineStripper.nextElement() + " ");
			}

			comment = strippedComment.toString();
			
			exitValue = "Ok";
            dispose();
        }
        else
        {
            dispose();
        }
    }
	
	public void focusGained(FocusEvent evt) 
	{
		String tempComment = commentText.getText();
		if(tempComment.equals (DEFAULT_COMMENT))
		{
			commentText.selectAll();
		}	
    }

    public void focusLost(FocusEvent evt) 
	{
    }
    
}