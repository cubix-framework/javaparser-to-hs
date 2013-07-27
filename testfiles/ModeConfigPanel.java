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

import candyfolds.config.ModeConfig;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class ModeConfigPanel {
	final JPanel panel=new JPanel(new BorderLayout());
	private ModeConfig modeConfig;
	final JCheckBox enabledCB=new JCheckBox("Enable CandyFolds for this mode");
	final JCheckBox useBigLinesForStripConfigsCB=new JCheckBox("Prefer matching against lines with more than 1 non-space characters");
	final JCheckBox showStripOn0IndentCB=new JCheckBox("Show indentation-guide on left edge");
	private final StripConfigsTable stripConfigsTable=new StripConfigsTable();
	private final JPanel tablePanel=new JPanel(new BorderLayout());
	private final StripConfigsOpPanel stripConfigsOpPanel=new StripConfigsOpPanel(stripConfigsTable);

	{
		JScrollPane sp=new JScrollPane(stripConfigsTable.table);
		sp.setPreferredSize(new Dimension(10, 10));
		sp.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
		tablePanel.add(sp);
		tablePanel.add(stripConfigsOpPanel.panel, BorderLayout.SOUTH);
		//tablePanel.setBorder(BorderFactory.createTitledBorder("Candies"));
		panel.add(tablePanel);
		
		Box box=new Box(BoxLayout.Y_AXIS);
		enabledCB.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent ev) {
				    if(modeConfig!=null)
					    modeConfig.setEnabled(enabledCB.isSelected());
				    updateView();
			    }
		    }
		                           );
		box.add(enabledCB);
		useBigLinesForStripConfigsCB.addActionListener(new ActionListener(){
			    @Override
			    public void actionPerformed(ActionEvent ev){
			    	if(modeConfig!=null)
			    		modeConfig.setUseBigLinesForStripConfigs(useBigLinesForStripConfigsCB.isSelected());
			    	updateView();
			    }
		    });
		box.add(useBigLinesForStripConfigsCB);
		showStripOn0IndentCB.addActionListener(new ActionListener(){
			    @Override
			    public void actionPerformed(ActionEvent ev){
			    	if(modeConfig!=null)
			    		modeConfig.setShowStripOn0Indent(showStripOn0IndentCB.isSelected());
			    	updateView();
			    }
		    });
		box.add(showStripOn0IndentCB);
		panel.add(box, BorderLayout.NORTH);
		
		updateView();
	}

	void setModeConfig(ModeConfig modeConfig) {
		this.modeConfig=modeConfig;
		stripConfigsTable.setModeConfig(modeConfig);
		updateView();
	}

	private void updateView() {
		if(modeConfig!=null) {
			enabledCB.setSelected(modeConfig.getEnabled());
			boolean isDefault=modeConfig!=modeConfig.config.defaultModeConfig;
			enabledCB.setVisible(isDefault);
			useBigLinesForStripConfigsCB.setSelected(modeConfig.getUseBigLinesForStripConfigs());
			showStripOn0IndentCB.setSelected(modeConfig.getShowStripOn0Indent());
			stripConfigsOpPanel.panel.setVisible(
			  modeConfig!=modeConfig.config.defaultModeConfig);
		}
		useBigLinesForStripConfigsCB.setEnabled(enabledCB.isSelected());
		showStripOn0IndentCB.setEnabled(enabledCB.isSelected());
		setEnabledStripConfigs(enabledCB.isSelected());
	}

	private void setEnabledStripConfigs(boolean enabled) {
		stripConfigsTable.table.setEnabled(enabled);
		stripConfigsOpPanel.setEnabled(enabled);
	}

	void save(){
		stripConfigsTable.save();
	}
}