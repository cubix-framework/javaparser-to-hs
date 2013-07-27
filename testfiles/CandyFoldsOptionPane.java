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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import candyfolds.CandyFoldsPlugin;
import candyfolds.config.Config;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.OptionPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

public class CandyFoldsOptionPane
	implements OptionPane {
	private static CandyFoldsOptionPane INSTANCE;

	public static CandyFoldsOptionPane getInstance() {
		//Log.log(Log.NOTICE, CandyFoldsOptionPane.class, "getting INSTANCE");
		if(INSTANCE==null) {
			INSTANCE=new CandyFoldsOptionPane();
		}
		INSTANCE.reset();
		View view=jEdit.getActiveView();
		if(view!=null) {
			Buffer buffer=view.getBuffer();
			if(buffer!=null) {
				INSTANCE.modesCB.setSelectedItem(buffer.getMode());
				INSTANCE.setupModeConfigP();
			}
		}
		return INSTANCE;
	}

	private Config config;
	final JPanel panel=new JPanel(new BorderLayout());
	final JComboBox modesCB=new JComboBox();
	final ModeConfigPanel modeConfigP=new ModeConfigPanel();
	final JCheckBox usesDefaultModeConfigCB=new JCheckBox("Use Only "+Config.DEFAULT_MODE_CONFIG_NAME+" Configuration");


	{
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		Box box=new Box(BoxLayout.X_AXIS);
		box.add(new JLabel("Mode: "));
		box.add(box.createHorizontalStrut(3));
		box.add(modesCB);
		box.add(box.createHorizontalStrut(5));
		box.add(usesDefaultModeConfigCB);
		box.add(box.createHorizontalGlue());
		panel.add(box, BorderLayout.NORTH);

		panel.add(modeConfigP.panel);

		modesCB.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent ev) {
				    setupModeConfigP();
			    }
		    }
		                         );
		usesDefaultModeConfigCB.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent ev) {
				    String modeName=getSelectedModeConfigName();
				    if(modeName==null)
					    return;
				    //Log.log(Log.NOTICE, this, "changing default mode config on "+modeName);
				    if(usesDefaultModeConfigCB.isSelected()) {
					    //Log.log(Log.NOTICE, this, "setting it to default.");
					    config.setModeConfigToDefault(modeName);
				    } else {
					    config.setModeConfigToNew(modeName);
				    }
				    setupModeConfigP(modeName);
			    }
		    }
		                                         );
	}

	private void setupModeConfigP() {
		setupModeConfigP(getSelectedModeConfigName());
	}

	private void setupModeConfigP(String modeName) {
		if(modeName==null)
			return;
		if(modeName==Config.DEFAULT_MODE_CONFIG_NAME) {
			modeConfigP.setModeConfig(config.getModeConfig(Config.DEFAULT_MODE_CONFIG_NAME));
			usesDefaultModeConfigCB.setVisible(false);
			modeConfigP.panel.setVisible(true);
		} else {
			usesDefaultModeConfigCB.setVisible(true);
			if(config.usesDefaultModeConfig(modeName)) {
				modeConfigP.panel.setVisible(false);
				usesDefaultModeConfigCB.setSelected(true);
			} else {
				modeConfigP.setModeConfig(config.getModeConfig(modeName));
				modeConfigP.panel.setVisible(true);
				usesDefaultModeConfigCB.setSelected(false);
			}
		}
	}

	private String getSelectedModeConfigName() {
		if(config==null)
			return null;
		Object mode=modesCB.getSelectedItem();
		if(mode==null)
			return null;
		String modeConfigName;
		if(mode==Config.DEFAULT_MODE_CONFIG_NAME)
			return Config.DEFAULT_MODE_CONFIG_NAME;
		return ((Mode)mode).getName();
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public String getName() {
		return "CandyFolds";
	}

	private void reset() {
		config=new Config();
		if(modesCB.getItemCount()==0) {
			modesCB.addItem(Config.DEFAULT_MODE_CONFIG_NAME);
			for(Mode mode: jEdit.getModes())
				modesCB.addItem(mode);
		}
		setupModeConfigP();
	}

	@Override
	public void init() {
		//Log.log(Log.NOTICE, this, "init");
	}

	@Override
	public void save() {
		if(config==null)
			return;
		CandyFoldsPlugin plugin=CandyFoldsPlugin.getInstance();
		if(plugin==null)
			return;
		modeConfigP.save();
		config.store();
		plugin.setConfig(config);
		reset();
	}

}
