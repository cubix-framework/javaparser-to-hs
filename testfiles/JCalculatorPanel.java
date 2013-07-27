/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.DFish.tools.calculator;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.DFish.tools.calculator.plugin.JCalculatorPlugin;
import org.gjt.sp.jedit.jEdit;

/**
 *
 * @author yuyiwei
 */
public class JCalculatorPanel extends JPanel {

  private JCalculatorToolPanel toolPanel;
  private JTextField txtInput;
  private JLabel lblInfo;
  private JCalculatorParser parser;
  private Number resultNumber;
  private String resultString;
  private ButtonGroup butGroup;
  
  private Clipboard clipboard;

  private int currentSystem;
  
  public JCalculatorPanel() {
    initAll();
    initParser();
    applyActions();
    
    currentSystem = toolPanel.getSelectSystem();
  }

  private void initAll() {
    setLayout(new BorderLayout());

    toolPanel = new JCalculatorToolPanel(this);
    add(BorderLayout.NORTH, toolPanel);
    butGroup = toolPanel.getButtonGroup();

    JCalculatorFunctionPanel funPanel = new JCalculatorFunctionPanel();
    txtInput = funPanel.getInputComponet();
    lblInfo = funPanel.getInfoComponent();

    add(BorderLayout.CENTER, funPanel);
    
    clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }

  private void initParser() {
    parser = new JCalculatorParser();
    resultNumber = null;
  }

  private void applyActions() {
    for(Enumeration<AbstractButton> v = butGroup.getElements(); v.hasMoreElements();){
      v.nextElement().addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          currentSystem = toolPanel.getSelectSystem();
          calculate();
        }
      });
    }
    
    txtInput.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          resetCalculator();
        }
      }
    });
    
    txtInput.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        calculate();
      }
    });
    
    lblInfo.setToolTipText("Click to save calculate result into clipboard");
    
    lblInfo.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e){
        StringSelection clipString = new StringSelection(
          getResult(jEdit.getProperty(JCalculatorPlugin.OPTION_PREFIX+"onlyResult").equals("true")));
        clipboard.setContents(clipString, null);
      }
    });
  }

  protected String string2Html(String str) {
    str = str.replaceAll("<", "&lt;");
    str = str.replaceAll(">", "&rt;");
    str = str.replaceAll(" ", "&nbsp;");
    
    return str;
  }
  
  protected String formatString(String str){
    
    if(jEdit.getProperty(JCalculatorPlugin.OPTION_PREFIX+"changeOperator").equals("true")){
      str = str.replaceAll("\\*", "x");
      //str = str.replaceAll("\\*", "&times;");
      //str = str.replaceAll("/", "&divide;");
    }
    if(jEdit.getProperty(JCalculatorPlugin.OPTION_PREFIX+"removeblank").equals("true")){
      str = str.replaceAll(" ", "");
    }
    if(jEdit.getProperty(JCalculatorPlugin.OPTION_PREFIX+"toUpcase").equals("true")){
      str = str.toUpperCase();
    }
    
    return str;
  }
  
  protected String formatResult(Number n){
    switch(currentSystem){
      case 16:
        return "0x" + Long.toHexString(n.longValue()).toUpperCase();
      case 8:
        return "0" + Long.toOctalString(n.longValue());
      case 2:
        return "b" + Long.toBinaryString(n.longValue());
      default:
        return n.toString();
    }
  }

  protected String getResult(boolean onlyResult){
    if(onlyResult){
      return formatResult(resultNumber);
    } else {
      return resultString + " = " + formatResult(resultNumber);
    }
  }
  
  public void focuseOnDefaultComponent() {
    txtInput.requestFocus();
  }

  public void resetCalculator() {
    txtInput.setText("");
    lblInfo.setText("");
  }

  public void calculate(String s){
    txtInput.setText(s);
    if(!txtInput.isFocusOwner()){
      txtInput.requestFocus();
    }
    calculate();
  }
  
  public void calculate() {
    String str = txtInput.getText();

    if ((str == null) || (str.length() == 0)) {
      lblInfo.setText("");
      return;
    }

    // scan this foluma
    resultNumber = parser.calculate(str);
    if(resultNumber == null){
      parser.PointOutError(txtInput);
      return;
    }
    
    resultString = formatString(str);

    String labelText = "<html><font color=green>" + string2Html(resultString) + "</font>" + "<b>&nbsp;=&nbsp;</b>" + "<font color=blue>" + formatResult(resultNumber) + "</font></html>";
    lblInfo.setText(labelText);
  }
}
