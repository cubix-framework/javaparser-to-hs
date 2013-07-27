package activator;


import java.awt.Color;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.PluginJAR;

import common.gui.util.ConstraintFactory;

public class ReloadPanel extends JPanel implements Observer {
    List <PluginList.Plugin> plugins = new ArrayList<PluginList.Plugin>();
    HashMap<File, PluginList.Plugin> confirmed = new HashMap<File, PluginList.Plugin>();
    ConstraintFactory cf = new ConstraintFactory();

    public ReloadPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.GRAY);
        update();
        PluginList.getInstance().addObserver(this);
    }

    public void update(Observable o, Object arg) {
        update();
    }

    public void update() {
        plugins.clear();
        removeAll();
        PluginList pl = PluginList.getInstance();
        int numPlugins = pl.size();
        PluginJAR jar;
        for (int i=0; i<numPlugins; ++i) {
            PluginList.Plugin plugin = pl.get(i);
            jar = plugin.getJAR();
            if (jar == null || jar.getPlugin() == null) {
                continue;
            }
            confirmed.put(plugin.getFile(), plugin);

        }

        plugins.addAll(confirmed.values());
        Collections.sort(plugins,new PluginComparator());
        int row = 0;

        for (PluginList.Plugin plugin : plugins) {
            jar = plugin.getJAR();

            if (jar.getPlugin() == null) {
                continue;
            }
            String status = PluginManager.getPluginStatus(jar);
            String display_name = plugin.toString();
            if (status.equals("Loaded")) {
                display_name = "<html><font color=yellow>&#9830;</font> " + display_name;
            } else if (status.equals(PluginList.ACTIVATED)) {
                display_name = "<html><font color=green>&#9830;</font> " + display_name;
            } else if (status.equals(PluginList.NOT_LOADED)) {
                display_name = "<html><font color=gray>&#9830;</font> " + display_name;
            } else if (status.equals(PluginList.ERROR)) {
                display_name = "<html><font color=red>&#9830;</font> " + display_name;
            }
            JButton button = new JButton(new Reload(jar, display_name));
            button.setToolTipText(jEdit.getProperty("activator.Click_to_reload", "Click to reload:") + " " + plugin.toString());
            button.setHorizontalAlignment(SwingConstants.LEFT);
            //			add(name,cf.buildConstraints(0,row,1,1));
            add(button, cf.buildConstraints(1,row,1,1));
            row++;
        }
    }
}


