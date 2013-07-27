/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.DFish.tools.calculator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.jEdit;

/**
 *
 * @author yuyiwei
 */
public class JCalculatorToolPanel extends JPanel{
  private JRadioButton opHex;
  private JRadioButton opDec;
  private JRadioButton opOct;
  private JRadioButton opBin;
  private ButtonGroup optionsGroup;
  private JCalculatorPanel cPanel;
  
  public JCalculatorToolPanel(JCalculatorPanel p){
    cPanel = p;
    
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    
    // options for Hex, Dec, Oct, Bin
    Box optionsBox = new Box(BoxLayout.X_AXIS);
    opHex = new JRadioButton("Hex");
    opDec = new JRadioButton("Dec");
    opOct = new JRadioButton("Oct");
    opBin = new JRadioButton("Bin");
    
    optionsGroup = new ButtonGroup();
    optionsGroup.add(opHex);
    optionsGroup.add(opDec);
    optionsGroup.add(opOct);
    optionsGroup.add(opBin);
    optionsGroup.setSelected(opDec.getModel(), true);
    
    optionsBox.add(opHex);
    optionsBox.add(opDec);
    optionsBox.add(opOct);
    optionsBox.add(opBin);
    optionsBox.add(Box.createHorizontalGlue());
    
    add(optionsBox);

		add(Box.createGlue());

    add(makeCustomButton("jcalculator.calculate", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCalculatorToolPanel.this.cPanel.calculate();
			}
		}));
		add(makeCustomButton("jcalculator.clear", new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JCalculatorToolPanel.this.cPanel.resetCalculator();
			}
		}));

  }

	private AbstractButton makeCustomButton(String name, ActionListener listener) {
		String toolTip = jEdit.getProperty(name.concat(".label"));
		AbstractButton b = new RolloverButton(GUIUtilities.loadIcon(jEdit
				.getProperty(name + ".icon")));
		if (listener != null) {
			b.addActionListener(listener);
			b.setEnabled(true);
		} else {
			b.setEnabled(false);
		}
		b.setToolTipText(toolTip);
		return b;
	}
  
  public ButtonGroup getButtonGroup(){
    return optionsGroup;
  }
  
  public int getSelectSystem(){
    ButtonModel m = optionsGroup.getSelection();
    if(m == opHex.getModel()) return 16;
    if(m == opOct.getModel()) return 8;
    if(m == opBin.getModel()) return 2;
    return 10;
  }
}
