package gruntspud.ui;

import gruntspud.Constants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

public class LicensePanel extends JPanel {
  
  private JTextArea licenseText;
  private JRadioButton accept, decline;
  
  public LicensePanel() {
    super(new BorderLayout());    
    JPanel t = new JPanel(new BorderLayout());
    t.add(new FolderBar("Gruntspud Licenses", UIUtil.getCachedIcon(Constants.GRUNTSPUD_LOGO_STATIC)), BorderLayout.NORTH);
    MultilineLabel l = new MultilineLabel("Please read and accept or decline the licenses below.\n" +
        "Gruntspud contains components from a number of sources that\n" +
    "may be made available under licenses other than the GPL.");
    l.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    t.add(l, BorderLayout.SOUTH);
    add(t, BorderLayout.NORTH);
    licenseText = new JTextArea();
    licenseText.setEditable(false);
    JScrollPane scroller = new JScrollPane(licenseText);
    scroller.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
        BorderFactory.createLineBorder(Color.black)));
    add(scroller, BorderLayout.CENTER);
    ButtonGroup bg = new ButtonGroup();
    accept = new JRadioButton("I accept these terms and conditions and wish to continue");
    accept.setMnemonic('a');
    accept.setHorizontalAlignment(JRadioButton.LEFT);
    bg.add(accept);
    decline = new JRadioButton("I decline these terms and conditions and wish to exit");
    decline.setMnemonic('d');
    decline.setHorizontalAlignment(JRadioButton.LEFT);
    bg.add(decline);
    JPanel p = new JPanel(new BorderLayout());
    p.add(accept, BorderLayout.NORTH);
    p.add(decline, BorderLayout.SOUTH);
    add(p, BorderLayout.SOUTH);    
  }
  
  public void addLicenseText(URL resource) throws IOException {
    InputStream in = null;
    try {
      in = resource.openStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line = null;
      while( ( line = reader.readLine() ) != null ) {
        licenseText.getDocument().insertString(licenseText.getDocument().getLength(), line + "\n", null);
      }
    }
    catch(BadLocationException ble) {
      throw new IOException("Could not add license.");
    }
    finally {
     if(in != null) {
       try {
         in.close();
       }
       catch(IOException ioe) {
       }
     }
    }    
  }
  
  public boolean showLicense(Component parent) {
    licenseText.setCaretPosition(0);
    licenseText.scrollRectToVisible(licenseText.getVisibleRect());
    Window w = (Window)SwingUtilities.getAncestorOfClass(Window.class, parent);
    JDialog dialog = null;
    if(w instanceof Dialog) {
      dialog = new JDialog((Dialog)w, "License", true);
    }
    else {
      dialog = new JDialog((Frame)w, "License", true);
    }
    JPanel b = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton next = new JButton("Continue");
    final JDialog d = dialog;
    next.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        if(!accept.isSelected() && !decline.isSelected()) {
          JOptionPane.showMessageDialog(LicensePanel.this, "You must select accept or decline license.");
        }
        else {
          d.dispose();
        }
      }      
    });
    b.add(next);
    dialog.getContentPane().add(this, BorderLayout.CENTER);
    dialog.getContentPane().add(b, BorderLayout.SOUTH);
    dialog.setSize(new Dimension(420,480));
    UIUtil.positionComponent(SwingConstants.CENTER, dialog);
    dialog.setVisible(true);
    return accept.isSelected();
  }
}