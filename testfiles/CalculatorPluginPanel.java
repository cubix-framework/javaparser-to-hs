/*
This file, unless otherwise noted, is wholly the work of Dale Anson,
danson@grafidog.com. The complete contents of this file is hereby 
released into the public domain, with no rights reserved. For questions, 
concerns, or comments, please email the author.
*/

package ise.plugin.calculator;

import ise.calculator.CalculatorPanel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Wrap the calculator in a new panel so it is accessible to jEdit without
 * modifying the original calculator code.
 * @author Dale Anson, Dec 2003
 */
public class CalculatorPluginPanel extends JPanel {
    public CalculatorPluginPanel() {
        CalculatorPanel cp = new CalculatorPanel();
        cp.setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        add(cp);
    }
}
