package activator;

import static activator.PluginList.ACTIVATED;
import static activator.PluginList.LOADED;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.gjt.sp.jedit.jEdit;

import common.gui.util.ConstraintFactory;

public class ActivationPanel extends JPanel implements ActionListener,
		MouseListener {

	private JTable table;
	private JButton activate = new JButton(jEdit.getProperty("activator.Activate", "Activate"));
	private JButton deactivate = new JButton(jEdit.getProperty("activator.Deactivate", "Deactivate"));
	private JButton load = new JButton(jEdit.getProperty("activator.Load", "Load"));
	private JButton unload = new JButton(jEdit.getProperty("activator.Unload", "Unload"));

	private boolean showLibraries = true;

	private Color background = jEdit.getColorProperty("view.bgColor");
	private Color foreground = jEdit.getColorProperty("view.fgColor");

	/**
	@SuppressWarnings("static-access")
	*/
	public ActivationPanel() {
	    this(true);
	}

	public ActivationPanel(boolean showButtons) {
	    showLibraries = showButtons;
		load.setEnabled(false);
		unload.setEnabled(false);
		activate.setEnabled(false);
		deactivate.setEnabled(false);
		load.addActionListener(this);
		unload.addActionListener(this);
		activate.addActionListener(this);
		deactivate.addActionListener(this);
		setLayout(new GridBagLayout());
		ActivationTableModel model = new ActivationTableModel();
		table = new JTable(model);
		table.addMouseListener(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setDefaultRenderer(table.getColumnClass(0), new StringRenderer());
		table.setDefaultRenderer(table.getColumnClass(2), new CheckBoxRenderer());
		ConstraintFactory cf = new ConstraintFactory();
		add(new JScrollPane(table), cf.buildConstraints(0, 0, 10, 1, cf.N, cf.BOTH));
		if (showButtons) {
            add(load, cf.buildConstraints(0, 1, 1, 1, cf.CENTER, cf.NONE, 0, 0));
            add(unload, cf.buildConstraints(1, 1, 1, 1, cf.CENTER, cf.NONE, 0, 0));
            add(activate, cf.buildConstraints(2, 1, 1, 1, cf.CENTER, cf.NONE, 0, 0));
            add(deactivate, cf.buildConstraints(3, 1, 1, 1, cf.CENTER, cf.NONE, 0, 0));
		}
		int row = jEdit.getIntegerProperty("activator.rowselected", 1);
		ListSelectionModel smodel = table.getSelectionModel();
		smodel.setSelectionInterval(row, row);
		mouseReleased(null);
	}

	public void setVisible(boolean isVisible) {
		if (isVisible) {
		int row = jEdit.getIntegerProperty("activator.rowselected", 1);
		ListSelectionModel smodel = table.getSelectionModel();
		smodel.setSelectionInterval(row, row);
		mouseReleased(null);
		}
		super.setVisible(isVisible);
	}

	// {{{ actionPerformed()
	public void actionPerformed(ActionEvent e) {
		int row = table.getSelectedRow();

		if (row < 0) {
			return;
		}
		PluginList.Plugin plugin = (PluginList.Plugin) table.getValueAt(row, 0);

		StopWatch sw = new StopWatch();

		if (e.getSource() == load) {
			sw.start();
			PluginManager.loadPluginJAR(plugin.getFile().toString());
			sw.stop();
			jEdit.getActiveView().getStatus().setMessage(
					plugin + " " + jEdit.getProperty("activator.loaded_in", "loaded in") + " " + sw);
		}
		if (e.getSource() == unload) {
			sw.start();
			PluginManager.unloadPluginJAR(plugin.getJAR());
			sw.stop();
			jEdit.getActiveView().getStatus().setMessage(
					plugin + " " + jEdit.getProperty("activator.unloaded_in", "unloaded in") + " " + sw);
		}
		if (e.getSource() == activate) {
			sw.start();
			plugin.getJAR().activatePlugin();
			sw.stop();
			jEdit.getActiveView().getStatus().setMessage(
					plugin + " " + jEdit.getProperty("activator.activated_in", "activated in") + " " + sw);
		}
		if (e.getSource() == deactivate) {
			sw.start();
			plugin.getJAR().deactivatePlugin(false);
			sw.stop();
			jEdit.getActiveView().getStatus().setMessage(
					plugin + " " + jEdit.getProperty("activator.deactivated_in", "deactivated in") + " " + sw);
		}
		// Re-select the thing that was selected
		ListSelectionModel smodel =table.getSelectionModel();
		smodel.setSelectionInterval(row, row);
		// Fix up the buttons below
		mouseReleased(null);
	}// }}}

	// {{{ MouseListener impl

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent unused) {
		int row = table.getSelectedRow();
		table.updateUI();
		PluginList.Plugin plugin;
		if (row >= 0) {
			jEdit.setIntegerProperty("activator.rowselected", row);
			plugin = (PluginList.Plugin) table.getValueAt(row, 0);
			if (plugin == null) return;
			load.setVisible(!plugin.isLoaded());
			unload.setVisible(plugin.isLoaded());
			activate.setVisible(!plugin.isActivated());
			deactivate.setVisible(plugin.isActivated());
			load.setEnabled(!plugin.isLoaded());
			unload.setEnabled(plugin.isLoaded());
			activate.setEnabled(!plugin.isActivated());
			deactivate.setEnabled(plugin.isActivated());
			if (plugin.getStatus() == PluginList.ERROR || plugin.isLibrary()) {
				activate.setEnabled(false);
			}
		} else {
			load.setVisible(true);
			unload.setVisible(true);
			activate.setVisible(true);
			deactivate.setVisible(true);
			load.setEnabled(false);
			unload.setEnabled(false);
			activate.setEnabled(false);
			deactivate.setEnabled(false);
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	// }}}

	// {{{ ActivationTableModel
	class ActivationTableModel extends DefaultTableModel implements Observer {

		private static final long serialVersionUID = 8707449444197059917L;

		public ActivationTableModel() {
			PluginList.getInstance().addObserver(this);
		}

		public int getRowCount() {
		    if (showLibraries) {
		        return PluginList.getInstance().size();
		    }
		    else {
		        return PluginList.getInstance().pluginCount();
		    }
		}

		public int getColumnCount() {
			return 3;
		}

		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return jEdit.getProperty("activator.Plugin", "Plugin");
			case 1:
				return jEdit.getProperty("activator.State", "State");
			case 2:
			    return jEdit.getProperty("activator.Startup", "Start up");
			default:
				return jEdit.getProperty("activator.error", "error");
			}
		}

		public Class getColumnClass(int col) {
			switch (col) {
			case 2:
			    return Boolean.class;
			default:
				return String.class;
			}
		}

		public Object getValueAt(int row, int col) {
			PluginList.Plugin p = PluginList.getInstance().get(row);
			if (col == 0) {
				return p;
			}

			if (col == 1) {
				return p.getStatus();
			}

			if (col == 2) {
			    return p.canLoadOnStartup() && p.loadOnStartup();
			}
			return jEdit.getProperty("activator.error", "error");
		}

		public void setValueAt(Object value, int row, int col) {
		    if (col == 2) {
		        PluginList.Plugin p = PluginList.getInstance().get(row);
		        if (!p.isLibrary()) {
		            boolean b = ((Boolean)value).booleanValue();
		            p.setLoadOnStartup(b);
		        }
		    }
		}

		public boolean isCellEditable(int row, int col) {
		    boolean rtn = false;
		    if (col == 2) {
                PluginList.Plugin p = PluginList.getInstance().get(row);
                rtn = !p.isLibrary();
		    }
		    return rtn;
		}

		public void update(Observable o, Object arg) {
			fireTableDataChanged();
		}
	}// }}}


    // {{{ StringRenderer
    class StringRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = -3823797564394450958L;

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            setForeground(ActivationPanel.this.foreground);
            setBackground(isSelected ? table.getSelectionBackground() : ActivationPanel.this.background);

            if (column == 1) {
                String displayName = value.toString();
                if (value == LOADED) {
                    displayName = "<html><font color=yellow>&#9830;</font> " + displayName;
                } else if (value == ACTIVATED) {
                    displayName = "<html><font color=green>&#9830;</font> " + displayName;
                } else if (value == PluginList.ERROR) {
                    displayName = "<html><font color=red>&#9830;</font> " + displayName;
                }
                setText(displayName);
            }
            return this;
        }
    } // }}}
    
    // {{{ CheckBoxRenderer
    // checkbox renderer for the "Start up" column, only renders a checkbox
    // if the row represents a plugin, not a library jar.
    public class CheckBoxRenderer extends DefaultTableCellRenderer {
    
        private final BooleanRenderer booleanRenderer = new BooleanRenderer();
    
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                              boolean isSelected, boolean hasFocus, int row, int column) {
    
            setBackground(isSelected ? table.getSelectionBackground() : ActivationPanel.this.background);

            // if value is null, don't show a checkbox
            if ( value == null ) {
                return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            }
            
            // if this row represents a library jar, don't show a checkbox
			PluginList.Plugin p = PluginList.getInstance().get(row);
            if (p.isLibrary()) {
                return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            }
            
            // do show a checkbox for plugins
            return booleanRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    
        // the actual checkbox renderer
        private class BooleanRenderer extends JCheckBox implements TableCellRenderer
        {
            private final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    
            public BooleanRenderer() {
                super();
                setHorizontalAlignment(JLabel.CENTER);
                setBorderPainted(true);
            }
    
            public Component getTableCellRendererComponent(JTable table, Object value,
                                   boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }
            else {
                setForeground(ActivationPanel.this.foreground);
                setBackground(ActivationPanel.this.background);
            }
                setSelected((value != null && ((Boolean)value).booleanValue()));
    
                if (hasFocus) {
                    setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                } else {
                    setBorder(noFocusBorder);
                }
                return this;
            }
        }
    }  // }}}
} // }}}