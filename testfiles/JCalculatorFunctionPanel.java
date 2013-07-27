/*
 * JCalculatorFunctionPanel.java
 *
 * Created on 14 / gener / 2008, 10:05
 */

/*
 * JCalculatorFunctionPanel.java
 *
 * Created on 14 / gener / 2008, 10:05
 */
package org.DFish.tools.calculator;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gjt.sp.jedit.gui.HistoryTextField;

/**
 *
 * @author  yuyiwei
 */
public class JCalculatorFunctionPanel extends javax.swing.JPanel {
  private javax.swing.JLabel lblInfo;
  private javax.swing.JTextField txtInput;


  /** Creates new form JCalculatorFunctionPanel */
  public JCalculatorFunctionPanel() {
    initComponents();
    customerComponents();
  }

  private void initComponents() {

    txtInput = new HistoryTextField("plugin.jcalculator", true);
    lblInfo = new JLabel(" ");
    GridBagLayout gridbag;
    GridBagConstraints c;

    gridbag = new GridBagLayout();
    c = new GridBagConstraints();

    setLayout(gridbag);
    
    c.gridx = 0;
    c.gridy = 0;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridwidth = GridBagConstraints.REMAINDER;
    c.gridheight = 1;
    c.weightx = 1;
    c.weighty = 0;
    c.anchor = GridBagConstraints.NORTH;
    c.insets.top = 8;
    c.insets.bottom = 2;
    c.insets.left = 8;
    c.insets.right = 8;
    gridbag.setConstraints(txtInput, c);
    add(txtInput);

    c.gridy = 1;
    gridbag.setConstraints(lblInfo, c);
    add(lblInfo);

    c.gridy = 2;
    c.gridheight = GridBagConstraints.REMAINDER;
    c.weighty = 1;
    c.anchor = GridBagConstraints.CENTER;
    c.fill = GridBagConstraints.BOTH;
    JPanel pLast = new JPanel();
    gridbag.setConstraints(pLast, c);
    add(pLast);
  }
  
  public void customerComponents() {
    txtInput.setHorizontalAlignment(JTextField.RIGHT);

    lblInfo.setOpaque(true);
    lblInfo.setBackground(Color.LIGHT_GRAY);
    lblInfo.setHorizontalAlignment(JLabel.RIGHT);
    lblInfo.setVerticalAlignment(JLabel.CENTER);
  }

  public javax.swing.JLabel getInfoComponent() {
    return lblInfo;
  }

  public javax.swing.JTextField getInputComponet() {
    return txtInput;
  }
}
