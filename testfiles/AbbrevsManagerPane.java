/*
 * AbbrevsOptionPane.java
 *
 * Created on 27. januar 2007, 22:09
 */
package superabbrevs.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.OperatingSystem;

import com.google.inject.Inject;

import superabbrevs.AbbrevsOptionPaneController;
import superabbrevs.SuperAbbrevsPlugin;
import superabbrevs.gui.controls.ModesComboBox;
import superabbrevs.gui.controls.abbreviationlist.AbbreviationJList;
import superabbrevs.model.Abbreviation;

/**
 * The abbreviation pane that is used to manage all the abbreviations in the 
 * plugin.
 */
public class AbbrevsManagerPane extends JPanel {
	private static final int defaultModifier = OperatingSystem.isMacOS() ? 
			InputEvent.META_MASK : InputEvent.CTRL_MASK;
	
    private static final KeyStroke controlNKeyStroke = 
            KeyStroke.getKeyStroke(KeyEvent.VK_N, defaultModifier);
    private static final KeyStroke controlRKeyStroke = 
            KeyStroke.getKeyStroke(KeyEvent.VK_R, defaultModifier);
    private static final KeyStroke deleteKeyStroke = 
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
    
    private AbbrevsOptionPaneController controller;
    private AbbrevsManagerPane mainPanel;
    private AddAbbrevAction addAction = new AddAbbrevAction();
    private RemoveAbbrevAction removeAction = new RemoveAbbrevAction();
    private RenameAbbrevAction renameAction = new RenameAbbrevAction();

    @Inject
    public AbbrevsManagerPane(AbbrevsOptionPaneController controller) {
        this.mainPanel = this;
        this.controller = controller;
        
        initComponents();
        loadWindowState();
        setupKeyboardShortcuts();

        requestFocusForComponent(abbrevsJList);
    }

    private void initComponents() {
    	addJMenuItem.setAction(addAction);
        removeAddJMenuItem.setAction(removeAction);
        renameJMenuItem.setAction(renameAction);

        abbrevsJPopupMenu.add(addJMenuItem);
        abbrevsJPopupMenu.add(removeAddJMenuItem);
        abbrevsJPopupMenu.add(renameJMenuItem);

        GridBagConstraints constraints = new GridBagConstraints();
        abbrevsJPanel.setLayout(new GridBagLayout());
        
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 100;
        constraints.gridy = 0;
        constraints.insets = new Insets(7,7,0,0);
        abbrevsJPanel.add(createModePanel(), constraints);
        constraints.insets = new Insets(4,14,0,7);
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 100;
        abbrevsJPanel.add(createAbbrevListPanel(), constraints);
        constraints.gridy = 2;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weighty = 0;
        constraints.insets = new Insets(7,7,7,0);
        abbrevsJPanel.add(createAbbrevListButtonPanel(), constraints);
        
        abbrevsEditorJSplitPane.setDividerLocation(220);
        abbrevsEditorJSplitPane.setLeftComponent(abbrevsJPanel);
        abbrevsEditorJSplitPane.setRightComponent(abbrevEditorPane);
        
        setLayout(new BorderLayout());
        add(abbrevsEditorJSplitPane, java.awt.BorderLayout.CENTER);
        
        modesJComboBox.bind();
    }

	private JPanel createAbbrevListPanel() {
		JPanel panel = new JPanel(new BorderLayout(0,7));
		
		abbrevsJLabel.setDisplayedMnemonic('A');
        abbrevsJLabel.setLabelFor(abbrevsJList);
        abbrevsJLabel.setText("Abbreviations:");
        panel.add(abbrevsJLabel, BorderLayout.NORTH);
        
        abbrevsJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        abbrevsJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                abbrevsJListMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                abbrevsJListMouseReleased(evt);
            }
        });
        abbrevsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                abbrevsJListValueChanged(evt);
            }
        });
        
        jScrollPane.setViewportView(abbrevsJList);
        panel.add(jScrollPane, BorderLayout.CENTER);
        
        return panel;
	}

	private JPanel createAbbrevListButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		addJButton.setAction(addAction);
        addJButton.setText("Add...");
        addJButton.getAccessibleContext().setAccessibleName("addJButton");
        addJButton.setFocusable(false);
        panel.add(addJButton);

        removeJButton.setAction(removeAction);
        removeJButton.setText("Remove");
        removeJButton.setEnabled(false);
        removeJButton.setFocusable(false);
        panel.add(removeJButton);
        return panel;
	}

	private JPanel createModePanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		modeJLabel.setDisplayedMnemonic('M');
        modeJLabel.setLabelFor(modesJComboBox);
        modeJLabel.setText("Mode:");
        panel.add(modeJLabel);
    	
    	modesJComboBox = new ModesComboBox(controller.getModeService());
        modesJComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                modesJComboBoxActionPerformed(evt);
            }
        });
        panel.add(modesJComboBox);
        
        return panel;
	}
    private void abbrevsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {
    	Abbreviation selectedAbbrev = abbrevsJList.getSelectedValue();
    	updateComponentState();
		abbrevEditorPane.setAbbrev(selectedAbbrev);
    }

    private void updateComponentState() {
		boolean hasSelectedValue = abbrevsJList.hasSelectedValue();
		renameAction.setEnabled(hasSelectedValue);
		removeAction.setEnabled(hasSelectedValue);
	}

	private void modesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        loadSelectedMode();
    }

	private void loadSelectedMode() {
		String modeName = modesJComboBox.getSelectedItem();
        abbrevsJList.setMode(controller.loadMode(modeName));
	}

    private void abbrevsJListMouseReleased(java.awt.event.MouseEvent evt) {
        maybeShowPopup(evt);
    }

    private void abbrevsJListMousePressed(java.awt.event.MouseEvent evt) {
        maybeShowPopup(evt);
    }

    /**
     * Shows a popup menu if the abbreviation list is rigth clicked.
     * @param evt the mouse event that will be tested if it is a popup trigger.
     */
    private void maybeShowPopup(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            int selection = abbrevsJList.locationToIndex(new Point(evt.getX(), evt.getY()));
            setSelection(selection);
            abbrevsJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Request focus for the given user interface component.
     * @param component the interface component that should be given focus.
     */
    private void requestFocusForComponent(final JComponent component) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                component.requestFocus();
            }
        });
    }

    /**
     * Set the selection in the abbreviation list to the given index.
     * @param selection the index that the should be selected in the 
     * abbreviation list.
     */
    private void setSelection(int selection) {
        if (selection == -1) {
            abbrevsJList.clearSelection();
        } else {
            abbrevsJList.setSelectedIndex(selection);
        }
    }

    public void save()  {
    	abbrevEditorPane.saveActiveAbbrev();
    	try {
            controller.saveAbbrevs();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not write the file abbreviation file",
                    "Write to file error", JOptionPane.ERROR_MESSAGE);
        }
        saveWindowState();
    }
    
    public void cancel() {
    	
    	loadSelectedMode();
    }

    /**
     * Setup the keyboard shortcuts for the dialog 
     */
    private void setupKeyboardShortcuts() {
        // register actions
        ActionMap actionMap = getActionMap();
        actionMap.put(addAction.getValue(Action.NAME), addAction);
        actionMap.put(removeAction.getValue(Action.NAME), removeAction);
        actionMap.put(renameAction.getValue(Action.NAME), renameAction);

        // Bind keyboard shortcuts
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(controlNKeyStroke,addAction.getValue(Action.NAME));
        inputMap.put(controlRKeyStroke,renameAction.getValue(Action.NAME));
        inputMap.put(deleteKeyStroke,removeAction.getValue(Action.NAME));
    }

    /**
     * An action that enableds the user to create new abbreviations
     */
    private class AddAbbrevAction extends AbstractAction {

        public AddAbbrevAction() {
            putValue(Action.NAME, "Add...");
            putValue(Action.SMALL_ICON,
                    new javax.swing.ImageIcon(
                    getClass().getResource("/superabbrevs/gui/icons/Plus.png")));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_D);
            putValue(Action.ACCELERATOR_KEY,controlNKeyStroke);
            putValue(Action.SHORT_DESCRIPTION, "Add a new abbreviation");
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(mainPanel,
                    "Enter the name of the new abbreviation");
            if (name != null && !name.trim().equals("")) {
            	abbrevsJList.addAbbrev(name);
            }
        }
    }

    /**
     * An action that enableds the user to delete abbreviations
     */
    private class RemoveAbbrevAction extends AbstractAction {

        public RemoveAbbrevAction() {
            putValue(Action.NAME, "Remove...");
            putValue(Action.SMALL_ICON,
                    new javax.swing.ImageIcon(
                    getClass().getResource("/superabbrevs/gui/icons/Minus.png")));
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
            putValue(Action.ACCELERATOR_KEY,deleteKeyStroke);
            putValue(Action.SHORT_DESCRIPTION, "Remove the selected abbreviation");
            setEnabled(false);
        }

        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(mainPanel,
                    "Do you really wish to delete the selected abbreviation",
                    "Delete Abbreviation", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
            	abbrevsJList.removeSelectedAbbreviation();
            }
        }
    }

    /**
     * An action that enableds the user to rename abbreviations
     */
    private class RenameAbbrevAction extends AbstractAction {

        public RenameAbbrevAction() {
            putValue(Action.NAME, "Rename...");
            putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
            putValue(Action.ACCELERATOR_KEY,controlRKeyStroke);
            putValue(Action.SHORT_DESCRIPTION, 
                    "Rename the selected abbreviation");
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
            Abbreviation abbrev = abbrevsJList.getSelectedValue();

            String name = JOptionPane.showInputDialog(mainPanel,
                    "Enter the new name for the abbreviation", abbrev.getName());
            // if the new name is different from the old name the 
            // abbreviation list will have to be resorted.
            if (name != null && !"".equals(name.trim()) && 
                    !name.equals(abbrev.getName())) {
                abbrev.setName(name);
                abbrevsJList.grabFocus();
            }
        }
    }
    
    /**
     * Load the settings for the state of the controls in the windows from a 
     * property file and update the state of the window with the loaded values.
     */
    private void loadWindowState() {
        InputStream in = SuperAbbrevsPlugin.getResourceAsStream(
                SuperAbbrevsPlugin.class, "AbbrevsOptionPane.properties");
        Properties p = new Properties();

        try {
            p.load(in);
            
            int dl = new Integer(p.getProperty("AbbrevsOptionPane.divider_location"));
            abbrevsEditorJSplitPane.setDividerLocation(dl);
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(this, ex.getMessage(), "Exception",
            		JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        } finally {
            try { in.close(); } catch (Exception ex) {}
        }
    }

    /**
     * Save the settings for the state of the controls in the windows to a 
     * property file.
     */
    private void saveWindowState() {
        Properties p = new Properties();
        p.setProperty("AbbrevsOptionPane.divider_location", "" + 
                abbrevsEditorJSplitPane.getDividerLocation());

        OutputStream out = SuperAbbrevsPlugin.getResourceAsOutputStream(
                SuperAbbrevsPlugin.class, "AbbrevsOptionPane.properties");

        try {
            p.store(out, "Saving the window state");
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(this, ex.getMessage(), "Exception",
                    JOptionPane.ERROR);
        } finally {
            try { out.close(); } catch (Exception e) {}
        }
    }
    
    private AbbrevsEditorPane abbrevEditorPane = new AbbrevsEditorPane();
    private JSplitPane abbrevsEditorJSplitPane = new JSplitPane();
    private JLabel abbrevsJLabel = new JLabel();
    private AbbreviationJList abbrevsJList = new AbbreviationJList();
    private JPanel abbrevsJPanel = new JPanel();
    private JPopupMenu abbrevsJPopupMenu = new JPopupMenu();
    private JButton addJButton = new JButton();
    private JMenuItem addJMenuItem = new JMenuItem();
    private JScrollPane jScrollPane = new JScrollPane();
    private JLabel modeJLabel = new JLabel();
    private ModesComboBox modesJComboBox;
    private JMenuItem removeAddJMenuItem =  new JMenuItem();
    private JButton removeJButton = new JButton();
    private JMenuItem renameJMenuItem = new JMenuItem();    
}