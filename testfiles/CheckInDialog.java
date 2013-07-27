package clearcase;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import java.awt.Dimension;
import java.awt.BorderLayout;

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

public class CheckInDialog extends JDialog implements ActionListener, FocusListener
{
    /**
    * ClearCase comment for checked in file. 
    */
    String comment = null;
	public static final String DEFAULT_COMMENT = "I modified this because...";

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
    JTextArea commentText = new JTextArea();
    BorderLayout borderLayout4 = new BorderLayout();
    JPanel southPanel = new JPanel();
    JButton buttonCancel = new JButton();
    JButton buttonOk = new JButton();

    public CheckInDialog(View view)
    {
		super(view, "Checkin", true);
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
        CheckInDialog checkInDialog = new CheckInDialog();
        checkInDialog.pack();
        checkInDialog.show();
        System.exit(0);
    }
*/
    private void init()
    {
        titledBorder1 = new TitledBorder("Checkin Comment");
        //this.getContentPane().setLayout(mainPanelLayout);
        //this.setContentPane(mainPanel);
        setContentPane(mainPanel);
        mainPanel.setLayout(borderLayout3);
        centerPanel.setLayout(centerPanelLayout);
        centerPanel.setBorder(titledBorder1);

        commentText.setText(DEFAULT_COMMENT);
        commentText.setTabSize(0);
		commentText.addFocusListener(this);

        southPanel.setPreferredSize(new Dimension(40, 40));
        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(this);
        buttonCancel.setPreferredSize(new Dimension(73, 27));
        
        buttonOk.setPreferredSize(new Dimension(73, 27));
        buttonOk.setText("Ok");
        buttonOk.addActionListener(this);
        
        mainPanel.setPreferredSize(new Dimension(300, 200));
        mainPanel.add(centerPanel,  BorderLayout.CENTER);
        
        centerPanel.add(commentText, BorderLayout.CENTER);
        
        mainPanel.add(southPanel,  BorderLayout.SOUTH);
        southPanel.add(buttonOk, null);
        southPanel.add(buttonCancel, null);
    }

    public String getComment()
    {
        return comment;
    }
    
    public String getExitValue()
    {
        return exitValue;
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