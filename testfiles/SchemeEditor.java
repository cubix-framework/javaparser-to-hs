package editorscheme;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import ise.java.awt.KappaLayout;

import java.io.File;
import java.io.IOException;

import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.StandardUtilities;

import org.gjt.sp.util.Log;

public class SchemeEditor extends JPanel {

    private JComboBox schemeChooser;
    private JCheckBox applyAutomatically;
    private JButton newButton;
    private JButton deleteButton;
    private JButton saveButton;
    private Set<EditorPanel> panels = new HashSet<EditorPanel>();

    public SchemeEditor() {
        super();
        installUI(null);
        installListeners();
    }
    
    public SchemeEditor(EditorScheme scheme) {
        super();
        installUI(scheme);
        installListeners();
    }

    private void installUI(EditorScheme scheme) {
        setLayout(new BorderLayout());

        // scheme chooser.  Only load editable schemes.
        List<EditorScheme> schemes = EditorSchemePlugin.getSchemes();
        schemeChooser = new JComboBox(schemes.toArray(new EditorScheme[schemes.size()]));
        schemeChooser.setRenderer(new EditorSchemeSelectorDialog.EditorSchemeListCellRenderer());
        if (scheme != null) {
            schemeChooser.setSelectedItem(scheme);   
        }

        // new button
        newButton = new JButton(jEdit.getProperty("editor-scheme.new", "New"));

        // delete button
        deleteButton = new JButton(jEdit.getProperty("editor-scheme.delete", "Delete"));
        deleteButton.setEnabled(! schemes.get(0).getReadOnly());

        // automatically apply checkbox
        applyAutomatically = new JCheckBox(jEdit.getProperty("editor-scheme.autoapply.label", "Automatically Apply"));
        applyAutomatically.setSelected(jEdit.getBooleanProperty("editor-scheme.autoapply", true));
        if (schemes.get(0).getReadOnly()) {
            applyAutomatically.setSelected(false);
            applyAutomatically.setEnabled(false);
        }

        // save button
        saveButton = new JButton(jEdit.getProperty("editor-scheme.save"));
        saveButton.setEnabled(! schemes.get(0).getReadOnly());

        // control panel
        JPanel controlPanel = new JPanel(new KappaLayout());
        controlPanel.add("0, 0, 1, 1, W, h, 3", schemeChooser);
        controlPanel.add("1, 0, 1, 1, 0, h, 3", newButton);
        controlPanel.add("2, 0, 1, 1, 0, h, 3", deleteButton);
        controlPanel.add("3, 0, 1, 1, 0, h, 3", applyAutomatically);
        controlPanel.add("4, 0, 1, 1, 0, h, 3", saveButton);

        // tabs
        JTabbedPane tabs = new JTabbedPane();
        String propertyGroups = jEdit.getProperty("editor-scheme.property-groups");
        String[] groups = propertyGroups.split("\\s+");
        for (String group : groups) {
            String tabName = jEdit.getProperty("editor-scheme." + group + ".name");
            EditorPanel panel = new EditorPanel((EditorScheme) schemeChooser.getItemAt(0), group);
            panels.add(panel);
            schemeChooser.addActionListener(panel);
            JScrollPane scroller = new JScrollPane(panel);
            scroller.getVerticalScrollBar().setUnitIncrement(10);
            tabs.add(tabName, scroller);
        }

        add(controlPanel, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    private void installListeners() {
        schemeChooser.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                EditorScheme scheme = (EditorScheme) schemeChooser.getSelectedItem();
                deleteButton.setEnabled(! scheme.getReadOnly());
                saveButton.setEnabled(! scheme.getReadOnly());
                if (scheme.getReadOnly()) {
                    if (applyAutomatically.isSelected()) {
                        applyAutomatically.setSelected(false);
                    }
                    applyAutomatically.setEnabled(false);
                } else {
                    applyAutomatically.setSelected(jEdit.getBooleanProperty("editor-scheme.autoapply", true));
                    applyAutomatically.setEnabled(true);
                }
                for (EditorPanel panel : panels) {
                    panel.setScheme(scheme);
                }
                scheme.apply();
            }
        }
       );

        newButton.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newScheme();
            }
        }
       );

        deleteButton.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                deleteScheme();
            }
        }
       );

        applyAutomatically.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                jEdit.setBooleanProperty("editor-scheme.autoapply", applyAutomatically.isSelected());
            }
        }
       );

        saveButton.addActionListener (new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                EditorScheme scheme = (EditorScheme) schemeChooser.getSelectedItem();
                try {
                    scheme.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
       );
    }

    void deleteScheme() {
        EditorScheme scheme = (EditorScheme)schemeChooser.getSelectedItem();    
        if (scheme.getReadOnly()) {
            // can't delete a read-only scheme
            return;   
        }
        String filename = scheme.getFilename();
        File file = new File(filename);
        file.delete();
        schemeChooser.removeItem(scheme);
    }
    
    void newScheme() {
        String name = "";
        String fileName = "";
        try {
            name = GUIUtilities.input(this, "editor-scheme.save-current", "");
            if (name == null) {
                return;
            }

            fileName = MiscUtilities.constructPath (EditorScheme.getDefaultDir(), name.replace(' ', '_') + EditorScheme.EXTENSION);

            File file = new File(fileName);
            if (file.exists()) {
                String[] args = new String[]{ file.getPath() } ;
                GUIUtilities.error(this, "editor-scheme.schemeexists", args);
                return;
            }

            EditorScheme scheme = new EditorScheme();
            scheme.setName(name);
            scheme.setFilename(fileName);
            scheme.getFromCurrent();
            scheme.setReadOnly(false);
            scheme.save();

            int i;
            for (i = 0; i < schemeChooser.getItemCount(); i++) {
                EditorScheme s = (EditorScheme) schemeChooser.getItemAt(i);
                if (StandardUtilities.compareStrings(s.getName(), name, true) > 1) {
                    break;
                }
            }
            schemeChooser.insertItemAt(scheme, i);
            schemeChooser.setSelectedIndex(i);
        } catch (IOException ioe) {
            String[] args = new String[]{ name, ioe.toString(), } ;

            GUIUtilities.error(this, "editor-scheme.saveerror", args);

            Log.log(Log.ERROR, EditorSchemePlugin.class, "error saving [" + name + "] to [" + fileName + "]: " + ioe.toString());
        }
    }

}