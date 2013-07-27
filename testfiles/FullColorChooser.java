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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

class FullColorChooser {
	private static final FullColorChooser INSTANCE=new FullColorChooser();

	final JPanel panel=new JPanel(new BorderLayout());
	private JColorChooser colorChooser=new JColorChooser();
	private ColorAlphaChooser colorAlphaChooser=new ColorAlphaChooser();

	{
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			    @Override
			    public void stateChanged(ChangeEvent ev) {
				    update();
			    }
		    }
		                                                  );

		panel.add(colorChooser);
		panel.add(colorAlphaChooser.panel, BorderLayout.SOUTH);
		update();
	}

	private void update() {
		colorAlphaChooser.setColor(colorChooser.getColor());
	}
	
	public void setColor(Color color, int alpha){
		colorChooser.setColor(color);
		colorAlphaChooser.setColor(color, alpha);
	}

	public Color getColor() {
		return colorAlphaChooser.getAlphaedColor();
	}

	public static Color showDialog(Component parent, String title, Color initialColor) {
		//System.out.println("showing FullColorChoosser: "+initialColor+", alpha="+initialColor.getAlpha());
		INSTANCE.setColor(initialColor, initialColor.getAlpha());
		if(
		  JOptionPane.showConfirmDialog(parent, INSTANCE.panel, title, JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION
		)
			return INSTANCE.getColor();
		return null;
	}

}
