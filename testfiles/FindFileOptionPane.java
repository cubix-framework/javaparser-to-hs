/**
 * FindFileDialog.java - The main search dialog.
 * :folding=explicit:collapseFolds=1:
 *
 * Option pane for the plugin.
 * @author Nicholas O'Leary
 * @version $Id: FindFileOptionPane.java 14668 Thu Oct 16 16:03:43 CDT 2008 keeleyt83 $
 */

package findfile;

//{{{ imports

import javax.swing.*;
import java.awt.*;

import org.gjt.sp.jedit.*;
//}}}

public class FindFileOptionPane extends AbstractOptionPane {
    //{{{ Private members
    private JCheckBox multipleResults;
    private JCheckBox recursiveSearch;
    private JCheckBox useModeDetection;
    private JCheckBox hidePath;
    private JCheckBox openResults;
    private JCheckBox keepDialog;
    private JTextField archiveFilter;
    private JComboBox sortBy;
    //}}}

    //{{{ Final constants
    protected static final String OPTION_PANE = "FindFilePlugin.option-pane";
    protected static final String OPTION_PANE_LABELS = OPTION_PANE + "-labels.";
    protected static final String OPTION_PANE_DEFAULTS = OPTION_PANE + "-defaults.";
    protected static final String OPTIONS = "options.FindFilePlugin.";
    //}}}

    public FindFileOptionPane() {
        super("FindFilePlugin-options");
    }

    //{{{ _init

    @Override public void _init() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 1; gbc.weighty = 1;
        gbc.insets = new Insets(2,2,2,2);

        JPanel general = new JPanel(new GridBagLayout());
        general.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty(OPTION_PANE_LABELS + "generalSettings")));
        multipleResults = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "multipleResults"));
        multipleResults.setSelected(jEdit.getProperty(OPTIONS + "multipleResults","true").equals("true"));

        general.add(multipleResults,gbc);

        useModeDetection = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "useModeDetection"));
        useModeDetection.setSelected(jEdit.getProperty(OPTIONS + "useModeDetection","true").equals("true"));

        gbc.gridy = 1;
        general.add(useModeDetection,gbc);

        hidePath = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "hidePath"));
        hidePath.setSelected(jEdit.getProperty(OPTIONS + "hidePath", "false").equals("true"));

        gbc.gridy = 2;
        general.add(hidePath,gbc);

        addComponent(general,GridBagConstraints.BOTH);

        gbc.gridy = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        JPanel defaults = new JPanel(new GridBagLayout());
        defaults.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty(OPTION_PANE_LABELS + "defaultSettings")));
        recursiveSearch = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "recursiveSearch"));
        recursiveSearch.setSelected(jEdit.getProperty(OPTIONS + "recursiveSearch","false").equals("true"));

        defaults.add(recursiveSearch,gbc);

        gbc.gridy = 1;
        openResults = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "openAllResults"));
        openResults.setSelected(jEdit.getProperty(OPTIONS + "openAllResults", "false").equals("true"));

        defaults.add(openResults,gbc);

        gbc.gridy = 2;
        keepDialog = new JCheckBox(jEdit.getProperty(OPTION_PANE_LABELS + "keepDialog"));
        keepDialog.setSelected(jEdit.getProperty(OPTIONS + "keepDialog", "false").equals("true"));

        defaults.add(keepDialog,gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3;
        sortBy = new JComboBox();
        sortBy.addItem("path");
        sortBy.addItem("filename");
        sortBy.setSelectedItem(jEdit.getProperty(OPTIONS + "sortBy","path"));
        gbc.weightx = 0;
        defaults.add(new JLabel(jEdit.getProperty("FindFilePlugin.option-pane-labels.sortBy") + "  "));
        gbc.gridx = 1;
        gbc.weightx = 1;
        defaults.add(sortBy,gbc);

        addComponent(defaults,GridBagConstraints.BOTH);

        gbc.gridx = 0;
        gbc.weightx = 0;
        JPanel archive = new JPanel(new GridBagLayout());
        archive.setBorder(BorderFactory.createTitledBorder(jEdit.getProperty(OPTION_PANE_LABELS + "archiveSettings")));
        archive.add(new JLabel(jEdit.getProperty(OPTION_PANE_LABELS + "archiveFilter")),gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        String archiveFilterValue = jEdit.getProperty(OPTION_PANE_DEFAULTS + "archiveFilter");
        if (archiveFilterValue == null) {
            archiveFilterValue = jEdit.getProperty(OPTION_PANE_DEFAULTS + "archiveFilter");
        }
        archiveFilter = new JTextField(archiveFilterValue, 25);
        archive.add(archiveFilter,gbc);
        addComponent(archive,GridBagConstraints.BOTH);
    }//}}}

    //{{{ _save
    @Override public void _save() {
        jEdit.setProperty(OPTIONS + "multipleResults", (multipleResults.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "recursiveSearch", (recursiveSearch.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "hidePath", (hidePath.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "useModeDetection", (useModeDetection.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "sortBy", sortBy.getSelectedItem().toString());
        jEdit.setProperty(OPTIONS + "archiveFilter",archiveFilter.getText());
        jEdit.setProperty(OPTIONS + "openAllResults", (openResults.isSelected() ? "true" : "false"));
        jEdit.setProperty(OPTIONS + "keepDialog", (keepDialog.isSelected() ? "true" : "false"));
    }//}}}
}
