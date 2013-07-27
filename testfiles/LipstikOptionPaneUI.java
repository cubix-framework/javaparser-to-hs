package com.lipstikLF.delegate;


import java.awt.*;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;


public class LipstikOptionPaneUI extends BasicOptionPaneUI
{
	/**	Creates a new BasicOptionPaneUI instance. */
	public static ComponentUI createUI(JComponent x)
	{
		return new LipstikOptionPaneUI();
	}

    protected void addIcon(Container top) {
    	/* Create the icon. */
    	Icon sideIcon = getIcon();

    	if (sideIcon != null)
    	{
    	    JLabel iconLabel = new JLabel(sideIcon);

            iconLabel.setName("OptionPane.iconLabel");
    	    iconLabel.setVerticalAlignment(SwingConstants.CENTER);
    	    top.add(iconLabel, BorderLayout.BEFORE_LINE_BEGINS);
    	}
    }

	/**	Creates a separator for the associated JOptionPane */
	protected Container createSeparator()
	{
		return new JPanel()
		{
			public Dimension getPreferredSize()
			{
				return new Dimension(10,10);
			}
		};
	}
}
