/**
 * FindFileDialog.java - The main search dialog.
 * :folding=explicit:collapseFolds=1:
 *
 * @author Nicholas O'Leary
 * @version $Id: FindFileDialog.java 14668 Thu Oct 16 16:01:01 CDT 2008 keeleyt83 $
 */

package findfile;

//{{{ imports
import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.io.VFSManager;
//}}}

public class FindFileDialog extends EnhancedDialog implements ActionListener {

    //{{{ Private members
    private HistoryTextField directoryField;
    private HistoryTextField filterField;
    private JCheckBox recursive;
    private JCheckBox openAllResults;
    private JCheckBox keepDialog;
    private JButton findButton;
    private JButton closeButton;
    private JButton chooseButton;
    private View view;

    private static final Map<View, FindFileDialog> viewHash = new HashMap<View, FindFileDialog>();

    protected static final String OPTIONS = "options.FindFilePlugin.";
    protected static final String SEARCH_PROPS = "FindFilePlugin.searchHistory";
    //}}}

    //{{{ Constructor
    /**
     * Constructor.
     * @param view The active View.
     */
    public FindFileDialog(View view) {
        this(view,null);
    }//}}}

    //{{{ Constructor
    /**
     * Constructor.
     * @param view The active View.
     * @param path The initial search path.
     */
    // TODO: This is gross.
    public FindFileDialog(View view, String path) {
        super(view,jEdit.getProperty("FindFilePlugin.search-dialog.title"),false);
        this.setResizable(false);
        GUIUtilities.loadGeometry(this,"FindFilePlugin");
        this.view = view;

        JLabel directoryLabel, filterLabel, settingsLabel;
        Dimension buttonDim = new Dimension (90, 25);
        HistoryModel model;
                        
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(12,12,12,12));
        setContentPane(content);

        JPanel innerContent = new JPanel(new GridBagLayout());
        innerContent.setBorder(new EmptyBorder(0,0,10,0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,3,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        filterField = new HistoryTextField("FindFilePlugin.filter");

        model = filterField.getModel();
		if(model.getSize() != 0) {
			filterField.setText(model.getItem(0));
        } else {
			filterField.setText("*.*");
		}

        filterField.setColumns(20);
        filterField.addActionListener(this);
        filterLabel = new JLabel(jEdit.getProperty("FindFilePlugin.search-dialog.filter.label"));
        filterLabel.setDisplayedMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.filter.mnemonic").charAt(0));
        filterLabel.setBorder(new EmptyBorder(0,0,0,12));
        filterLabel.setLabelFor(filterField);

        innerContent.add(filterLabel, gbc);
        gbc.gridx = 1;
        innerContent.add(filterField, gbc);
        gbc.gridy = 1;
        gbc.gridx = 0;
                
        // Initialize directory objects and add them to panel.

        directoryField = new HistoryTextField("FindFilePlugin.path");

        model = directoryField.getModel();
        if (path != null) {
            directoryField.setText(path);
        } else if(model.getSize() != 0) {
			directoryField.setText(model.getItem(0));
        } else {
			directoryField.setText(getDefaultPath());
		}
        
        directoryField.setColumns(40);
        directoryField.addActionListener(this);
        directoryLabel = new JLabel(jEdit.getProperty("FindFilePlugin.search-dialog.directory.label"));
        directoryLabel.setBorder(new EmptyBorder(0,0,0,12));
        directoryLabel.setDisplayedMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.directory.mnemonic").charAt(0));
        directoryLabel.setLabelFor(directoryField);

        chooseButton = new JButton(jEdit.getProperty("FindFilePlugin.search-dialog.chooseButton.text"));
        chooseButton.setMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.chooseButton.mnemonic").charAt(0));
        chooseButton.setPreferredSize(buttonDim);
        chooseButton.addActionListener(this);

        innerContent.add(directoryLabel,gbc);
        gbc.gridx = 1;
        innerContent.add(directoryField,gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0,0,3,0);
        innerContent.add(chooseButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        innerContent.setBorder(new EmptyBorder(0,0,0,0));

        // Initialize checkboxes and add to panel.
        settingsLabel = new JLabel(jEdit.getProperty("FindFilePlugin.search-dialog.settings.label"));

        innerContent.add(settingsLabel, gbc);
        gbc.gridy = 3;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        Box settingsBox = Box.createHorizontalBox();

        recursive = new JCheckBox(jEdit.getProperty("FindFilePlugin.search-dialog.recursive.label"));
        recursive.setSelected(jEdit.getProperty(OPTIONS + "recursiveSearch","false").equals("true"));
        recursive.setMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.recursive.mnemonic").charAt(0));
        settingsBox.add(recursive);
        settingsBox.add(Box.createHorizontalStrut(10));
        
        openAllResults = new JCheckBox(jEdit.getProperty("FindFilePlugin.search-dialog.openAllResults.label"));
        openAllResults.setSelected(jEdit.getProperty(OPTIONS + "openAllResults","false").equals("true"));
        openAllResults.setMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.openAllResults.mnemonic").charAt(0));
        settingsBox.add(openAllResults);
        settingsBox.add(Box.createHorizontalStrut(10));

        keepDialog = new JCheckBox(jEdit.getProperty("FindFilePlugin.search-dialog.keepDialog.label"));
        keepDialog.setSelected(jEdit.getProperty(OPTIONS + "keepDialog","false").equals("true"));
        keepDialog.setMnemonic(jEdit.getProperty("FindFilePlugin.search-dialog.keepDialog.mnemonic").charAt(0));
        settingsBox.add(keepDialog);
        
        // Initialize buttons and add to panel.
        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        findButton = new JButton(jEdit.getProperty("FindFilePlugin.search-dialog.findButton.text"));
        findButton.addActionListener(this);
        findButton.setPreferredSize(buttonDim);
        this.getRootPane().setDefaultButton(findButton);
        buttons.add(findButton);
        buttons.add(Box.createHorizontalStrut(6));

        closeButton = new JButton(jEdit.getProperty("FindFilePlugin.search-dialog.closeButton.text"));
        closeButton.addActionListener(this);
        closeButton.setPreferredSize(buttonDim);
        buttons.add(closeButton);

        // Add everything together.
        content.add(BorderLayout.NORTH, innerContent);
        content.add(BorderLayout.CENTER, settingsBox);
        content.add(BorderLayout.SOUTH,buttons);
        pack();
    }//}}}

    //{{{ ok
    /**
     * Called when the dialog is confirmed.
     * Performs the search.
     */
    public void ok() {
        saveSettings();
        view.getDockableWindowManager().addDockableWindow("FindFilePlugin");
        FindFileResults results = (FindFileResults) view.getDockableWindowManager().getDockable("FindFilePlugin");
        if (results == null) {
            setVisible(false);
            return;
        }
        results.searchStarted();
        SearchOptions options = new SearchOptions();
        options.path = directoryField.getText();
        options.filter = filterField.getText();
        options.recursive = recursive.isSelected();
        options.openResults = openAllResults.isSelected();
        FindFileRequest request = new FindFileRequest(view,results,options);
        VFSManager.runInWorkThread(request);
        GUIUtilities.saveGeometry(this,"FindFilePlugin");
        
        if (!keepDialog.isSelected()) {
            setVisible(false);
        }
    }
    ///}}}

    //{{{ cancel
    /**
     * Called when the dialog is canceled.
     */
    public void cancel() {
        GUIUtilities.saveGeometry(this,"FindFilePlugin");
        setVisible(false);
    }//}}}

    //{{{ actionPerformed
    /**
     * Action handler for dialog buttons.
     */
    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        // Includes the fields b/c thatt is the source returned on an enter key press.
        if (obj.equals(findButton)  || obj.equals(directoryField) || obj.equals(filterField)) {
            ok();
        } else if (obj.equals(closeButton)) {
            cancel();
        } else if (obj.equals(chooseButton)) {
            String[] dirs = GUIUtilities.showVFSFileDialog(
                FindFileDialog.this,
                view,
                directoryField.getText(),
                VFSBrowser.CHOOSE_DIRECTORY_DIALOG,
                false);
            if(dirs != null)
                directoryField.setText(dirs[0]);
        } else {
            //JOptionPane.showMessageDialog(this, obj.toString());
        }
    }//}}}

    //{{{ getFindFileDialog
    /*
     * Returns a reference to the currently open search dialog or creates a new
     * one if none is open. Use this instead of the constructor.
     */
    public static FindFileDialog getFindFileDialog (View view) {
        FindFileDialog dialog = viewHash.get(view);
        if (dialog == null) {
            dialog = new FindFileDialog(view);
            viewHash.put(view, dialog);
        }
        return dialog;
    }
    //}}}

    //{{{ dispose
    /*
     * Removes the current instance from the hash whenever it is destroyed.
     */
    @Override public void dispose() {
		viewHash.remove(view);
		super.dispose();
	}
    //}}}

    //{{{ saveSettings
    /*
     * Saves settings and previous search parameters.
     */
    private void saveSettings() {
        directoryField.addCurrentToHistory();
        filterField.addCurrentToHistory();
        jEdit.setProperty(OPTIONS + "recursiveSearch", (recursive.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "openAllResults", (openAllResults.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "keepDialog", (keepDialog.isSelected() ? "true" : "false"));
    }//}}}

    //{{{ getDefaultPath
    /*
     * Returns the user's default path.
     */
    private String getDefaultPath() {
        String path;
        String userHome = System.getProperty("user.home");
        String defaultPath = jEdit.getProperty("vfs.browser.defaultPath");
        
        if (defaultPath.equals("home")) {
            path = userHome;
        } else {
            if (defaultPath.equals("working")) {
                path = System.getProperty("user.dir");
            } else {
                if (defaultPath.equals("buffer")) {
                    if (view != null) {
                        Buffer buffer = view.getBuffer();
                        path = buffer.getDirectory();
                    } else {
                        path = userHome;
                    }
                } else {
                    path = userHome;
                }
            }
        }
        
        return path;
    }//}}}

}
