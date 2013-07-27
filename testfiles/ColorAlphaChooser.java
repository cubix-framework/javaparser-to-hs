/* % [{
% (C) Copyright 2008 Nicolas Carranza and individual contributors.
% See the CandyFolds-copyright.txt file in the CandyFolds distribution for a full
% listing of individual contributors.
%
% This file is part of CandyFolds.
%
% CandyFolds is free software: you can redistribute it and/or modify
% it under the terms of the GNU General Public License as published by
% the Free Software Foundation, either version 3 of the License,
% or (at your option) any later version.
%
% CandyFolds is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU General Public License for more details.
%
% You should have received a copy of the GNU General Public License
% along with CandyFolds.  If not, see <http://www.gnu.org/licenses/>.
% }] */
package candyfolds.config.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

class ColorAlphaChooser {
	private Color color=Color.yellow;
	private Color alphaedColor;
	public final JPanel panel=new JPanel();
	private final JSlider slider=new JSlider(0, 255, 255);
	private final JTextField textField=new JTextField(3);
	private final JLabel colorLabel=new JLabel() {
		    @Override
		    public void paintComponent(Graphics g) {
			    Dimension d=getSize();
			    g.setColor(Color.white);
			    g.fillRect(0, 0, d.width, d.height);
			    if(color!=null) {
				    g.setColor(getAlphaedColor());
				    g.fillRect(0, 0, d.width, d.height);
			    }
		    }
	    };

	ColorAlphaChooser() {
		BoxLayout l=new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(l);
		panel.add(new JLabel("Alpha:"));
		panel.add(Box.createHorizontalStrut(3));
		slider.setMaximumSize(null);
		panel.add(slider);
		panel.add(Box.createHorizontalStrut(3));
		textField.setEditable(false);
		textField.setHorizontalAlignment(JTextField.CENTER);
		textField.setMaximumSize(textField.getPreferredSize());
		panel.add(textField);
		Dimension d=new Dimension(50, 15);
		colorLabel.setPreferredSize(d);
		colorLabel.setMaximumSize(d);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(colorLabel);

		slider.addChangeListener(new ChangeListener() {
			    @Override
			    public void stateChanged(ChangeEvent ev) {
				    update();
			    }
		    }
		                        );
	}

	private void update() {
		alphaedColor=null;
		textField.setText(String.valueOf(slider.getValue()));
		panel.repaint();
	}

	public void setColor(Color color) {
		setColor(color,slider.getValue());
	}

	public void setColor(Color color, int alpha) {
		if(alpha<0 || alpha>255)
			throw new IllegalArgumentException();
		this.color=color;
		slider.setValue(alpha);
		alphaedColor=null;
		panel.repaint();
	}

	public Color getAlphaedColor() {
		if(alphaedColor==null) {
			alphaedColor=new Color(color.getRed(), color.getGreen(), color.getBlue(), slider.getValue());
		}
		return alphaedColor;
	}

}
