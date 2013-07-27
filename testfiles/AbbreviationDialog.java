package superabbrevs.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.gui.EnhancedDialog;

import superabbrevs.AbbrevsOptionPaneController;
import superabbrevs.JEditInterface;
import superabbrevs.SuperAbbrevsPlugin;

import com.google.inject.Inject;

public class AbbreviationDialog extends EnhancedDialog {

	private static final int defaultModifier = OperatingSystem.isMacOS() ? 
			InputEvent.META_MASK : InputEvent.CTRL_MASK;
	
    private static final KeyStroke controlEnter = 
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, defaultModifier);
	
    private AbbrevsManagerPane abbrevsOptionPane;

    private SaveAction saveAction = new SaveAction();
    private CancelAction cancelAction = new CancelAction();
    
    private JButton saveButton = new JButton(saveAction);
	private JButton cancelButton = new JButton(cancelAction);

	private JPanel contentPane;
	
	private static final boolean modal = false;
    
    @Inject 
    public AbbreviationDialog(JEditInterface jedit, AbbrevsOptionPaneController controller) {
        super(jedit.getView(), "Abbreviations", modal);

        initComponents();

        Properties p = loadWindowState();
        
        if (p != null) {
            int height = new Integer(p.getProperty("AbbrevsDialog.height")).intValue();
            int width = new Integer(p.getProperty("AbbrevsDialog.width")).intValue();
            int x = new Integer(p.getProperty("AbbrevsDialog.x")).intValue();
            int y = new Integer(p.getProperty("AbbrevsDialog.y")).intValue();
            
            this.setBounds(x, y, width, height); 
        } else {
            this.setLocationRelativeTo(jedit.getView());
        }

        setEnterEnabled(false);
        
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        
        abbrevsOptionPane = new AbbrevsManagerPane(controller);
        abbrevsOptionPane.setVisible(true);
        contentPane.add(abbrevsOptionPane, BorderLayout.CENTER);
        contentPane.add(createButtonPanel(), BorderLayout.SOUTH);
        
        setContentPane(contentPane);
        setupKeyboardShortcuts();
    }

	private void setupKeyboardShortcuts() {
		// register actions
        ActionMap actionMap = contentPane.getActionMap();
        actionMap.put(saveAction.getValue(Action.NAME), saveAction);

        // Bind keyboard shortcuts
        InputMap inputMap = contentPane.getInputMap(contentPane.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(controlEnter, saveAction.getValue(Action.NAME));
	}

	private Component createButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.add(saveButton);
		buttonPanel.add(cancelButton);
		
		return buttonPanel;
	}

	private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Abbreviation editor");
        setName("abbrevsDialog");

        pack();
    }
    
    public void ok() {
		abbrevsOptionPane.save();
		close();
    }

    public void cancel() {
    	abbrevsOptionPane.cancel();
    	close();
    }

    private void close() {
		saveWindowState();
        setVisible(false);
	}
    
    private Properties loadWindowState() {
        Properties p = new Properties();
        InputStream in = SuperAbbrevsPlugin.getResourceAsStream(
                SuperAbbrevsPlugin.class, "AbbrevsDialog.properties");
        try {    
            p.load(in);
            return p;
        } catch (Exception ex) {
            return null;
        } finally {
            try { in.close(); } catch (Exception ex) {}
        }
    }

    private void saveWindowState() {
        Properties p = new Properties();
        p.setProperty("AbbrevsDialog.height", "" + this.getHeight());
        p.setProperty("AbbrevsDialog.width", "" + this.getWidth());
        p.setProperty("AbbrevsDialog.x", "" + this.getX());
        p.setProperty("AbbrevsDialog.y", "" + this.getY());

        OutputStream out = SuperAbbrevsPlugin.getResourceAsOutputStream(
                SuperAbbrevsPlugin.class, "AbbrevsDialog.properties");

        try {
            p.store(out, "Saving the window state");
        } catch (Exception ex) {
            JOptionPane.showConfirmDialog(this, ex.getMessage(), "Exception",
                    JOptionPane.ERROR);
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }
    
    private class SaveAction extends AbstractAction {

		public SaveAction() {
            putValue(Action.NAME, "Save");
            putValue(Action.ACCELERATOR_KEY,controlEnter);
            putValue(Action.SHORT_DESCRIPTION, 
                    "Saves the edited abbreviations");
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
        	ok();
        }
    }
    
    private class CancelAction extends AbstractAction {

		public CancelAction() {
            putValue(Action.NAME, "Cancel");
            putValue(Action.SHORT_DESCRIPTION, 
                    "Cancels the edited abbreviations");
            setEnabled(true);
        }

        public void actionPerformed(ActionEvent e) {
        	cancel();
        }
    }
}
