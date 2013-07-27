package clearcase;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import java.awt.Dimension;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

public class UnCheckOutDialog extends JDialog implements ActionListener
{
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
    JCheckBox keepCheckOut = new JCheckBox();

    public UnCheckOutDialog(View view)
    {
		super(view, "Undo Checkout", true);
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
        UnCheckOutDialog UnCheckOutDialog = new UnCheckOutDialog();
        UnCheckOutDialog.pack();
        UnCheckOutDialog.show();
        System.exit(0);
    }
*/
    private void init()
    {
        titledBorder1 = new TitledBorder("");
        setContentPane(mainPanel);
        
        mainPanel.setLayout(borderLayout3);
        centerPanel.setLayout(centerPanelLayout);
        centerPanel.setBorder(titledBorder1);
        southPanel.setPreferredSize(new Dimension(40, 40));

        buttonOk.setText("Ok");
        buttonOk.addActionListener(this);
        buttonOk.setPreferredSize(new Dimension(73, 27));
        
        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(this);
        buttonCancel.setPreferredSize(new Dimension(73, 27));
        
        mainPanel.setPreferredSize(new Dimension(200, 80));
        keepCheckOut.setText("Keep file");
        
        mainPanel.add(centerPanel,  BorderLayout.CENTER);
        centerPanel.add(keepCheckOut,  BorderLayout.CENTER);
        
        mainPanel.add(southPanel,  BorderLayout.SOUTH);
        southPanel.add(buttonOk, null);
        southPanel.add(buttonCancel, null);
    }


    public boolean getKeepFile()
    {
        return keepCheckOut.isSelected();
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
            exitValue = "Ok";
            dispose();
        }
        else
        {
            dispose();
        }
    }
}