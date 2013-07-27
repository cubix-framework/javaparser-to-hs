/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.DFish.tools.calculator;

import javax.swing.JFrame;

/**
 *
 * @author yuyiwei
 */
public class JCalculatorApplication {
  public static void main(String[] args) {
    final JFrame f = new JFrame("JCalculator Application");
    JCalculatorPanel cp = new JCalculatorPanel();
    f.add(cp);

    f.setVisible(true);
    f.pack();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setEnabled(true);
    
    cp.focuseOnDefaultComponent();
  }

}
