/*
 * JBrowseOptionPane.java - JBrowse options panel
 *
 * Copyright (c) 1999-2000 George Latkiewicz, Andre Kaplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/


package sidekick.java.options;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import org.gjt.sp.jedit.AbstractOptionPane;

import sidekick.java.JavaParser;


/**
 * JBrowse option pane
 * @author George Latkiewicz
 * @author Andre Kaplan
 * @version $Id: JBrowseOptionPane.java 16257 2009-09-30 20:32:48Z daleanson $
**/
public class JBrowseOptionPane extends AbstractOptionPane
{
    // protected members inherited from AbstractOptionPane: y, gridBag

    // private state
    boolean isInitGui;
    boolean isInitModel;

    // private gui components

    private JPanel contents;

    // general options
    private JCheckBox cbxStatusBar;
    private JCheckBox cbxAutomaticParse;
    private JCheckBox cbxSort;

    private ButtonGroup bg = null;
    private JRadioButton rbSortByLine;
    private JRadioButton rbSortByName;
    private JRadioButton rbSortByVisibility;

    // filter options
    private JCheckBox cbxShowImports;
    private JCheckBox cbxShowFields;
    private JCheckBox cbxShowPrimitives;
    private JCheckBox cbxShowVariables;
    private JCheckBox cbxShowInitializers;
    private JCheckBox cbxShowGeneralizations;
    private JCheckBox cbxShowThrows;

    private JComboBox cmbTopLevelVis;
    private JComboBox cmbMemberVis;
    private int topLevelVisIndex;
    private int memberVisIndex;

    // display options
    private JCheckBox cbxShowArguments;
    private JCheckBox cbxShowArgumentNames;
    private JCheckBox cbxShowTypeArgs;
    private JCheckBox cbxShowErrors;
    private JCheckBox cbxShowNestedName;
    private JCheckBox cbxShowIconKeywords;
    private JCheckBox cbxShowMiscMod;
    private JCheckBox cbxShowIcons;
    private JCheckBox cbxShowIconsLikeEclipse;
    private JCheckBox cbxShowLineNum;

    private JComboBox cmbStyle;
    private int styleIndex = DisplayOptions.STYLE_UML;

    private JRadioButton rbVisSymbols;
    private JRadioButton rbVisWords;
    private JRadioButton rbVisNone;
    private JCheckBox cbxAbstractItalic;
    private JCheckBox cbxStaticUlined;
    private JCheckBox cbxTypeIsSuffixed;

    // Options object
    private GeneralOptions        options    = new GeneralOptions();
    private MutableFilterOptions  filterOpt  = options.getFilterOptions();
    private MutableDisplayOptions displayOpt = options.getDisplayOptions();

    // Property Accessor
    private PropertyAccessor props;

    private boolean batchUpdate = false;

    // Listeners
    private ActionListener defaultAction       = null;
    private ActionListener updateOptionsAction = null;
    private ActionListener setOptionsAction    = null;


    public JBrowseOptionPane() {
        this("sidekick.java");
    }


    public JBrowseOptionPane(String title) {
        super(title);
        setLayout(new BorderLayout());
        contents = new JPanel();
        contents.setLayout(gridBag = new GridBagLayout());
        add(new JScrollPane(contents));

        // It is the instantiating code's responsibility to call:
        // initGui(), initModel(), and setOptions() before displaying
        // Also either addDefaultListeners or addJBrowseListeners must be
        // called to allow the GUI to correctly respond to user interaction
    }


    /**
     * AbstractOptionPane implementation (_init and _save)
    **/
    public void _init() {
        this.setPropertyAccessor(new JEditPropertyAccessor());
        options.load(props);

        initGui();   // setup display from property values
        initModel(); // set GUI to model (as defined in Options object)
    }


    /**
     * AbstractOptionPane implementation (_init and _save)
     * The method called by the File->Plugin Options save button for
     * setting the JBrowse plugin options for all future sessions.
     * It saves the current view state to the associated property values.
    **/
    public void _save() {
        options.save(props);
        _init();
    }


    PropertyAccessor getPropertyAccessor() { return props; }


    void setPropertyAccessor(PropertyAccessor props) {
        this.props = props;
    }


    public boolean isInitGui() { return isInitGui; }


    public boolean isInitModel() { return isInitModel; }


    /**
     * Sets this OptionPane's options object to the state specified by the
     * the OptionPane's associated PropertyAccessor.
    **/
    public void load() {
        batchUpdate = true;
        options.load(props);
        batchUpdate = false;
    }


    /**
     * Setup the GUI (with no current state).
     * This should only be called once in the constructor for this
     * JBrowseOptionPane.
    **/
    private void initGui() {
        if (isInitGui()) {
            return;   
        }
        // -----
        // Title
        // -----
        JLabel titleLabel = new JLabel(
                props.getProperty("options." + getName() + ".panel_label") + ":",
                        JLabel.LEFT );
        titleLabel.setFont(new Font("Helvetica", Font.BOLD + Font.ITALIC, 13));

        addComponent(titleLabel);

        // ---------------
        // General Options
        // ---------------
        JPanel generalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        cbxStatusBar = new JCheckBox(
                props.getProperty("options.sidekick.java.showStatusBar"));
        //generalPanel.add(cbxStatusBar);

        cbxAutomaticParse = new JCheckBox(
                props.getProperty("options.sidekick.java.automaticParse"));
        //generalPanel.add(cbxAutomaticParse);

        cbxSort = new JCheckBox(
                props.getProperty("options.sidekick.java.sort"));
        //generalPanel.add(cbxSort);

        addComponent(generalPanel);

        // --------------
        // Filter Options
        // --------------
        OptionPanel filterPanel = new OptionPanel();
        filterPanel.setBorder(this.createOptionBorder(
            " " + props.getProperty("options.sidekick.java.filterOptions") + " "
        ));

        JPanel attrPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        /* Attributes */
        cbxShowFields = new JCheckBox(
                props.getProperty("options.sidekick.java.showAttr") + " ");
        attrPanel.add(cbxShowFields);

        /* Primitive Attributes */
        cbxShowPrimitives = new JCheckBox(
                props.getProperty("options.sidekick.java.showPrimAttr"));
        attrPanel.add(cbxShowPrimitives);
        filterPanel.addComponent(attrPanel);

        /* imports */
        cbxShowImports = new JCheckBox(
                props.getProperty("options.sidekick.java.showImports") + " ");
        filterPanel.addComponent(cbxShowImports);

        /* local variables */
        cbxShowVariables = new JCheckBox(
                props.getProperty("options.sidekick.java.showVariables"));
        filterPanel.addComponent(cbxShowVariables);


        /* static initializers */
        cbxShowInitializers = new JCheckBox(
                props.getProperty("options.sidekick.java.showInitializers"));
        filterPanel.addComponent(cbxShowInitializers);

        /* Generalizations */
        cbxShowGeneralizations = new JCheckBox(
                props.getProperty("options.sidekick.java.showGeneralizations") + " ");
        filterPanel.addComponent(cbxShowGeneralizations);

        /* Throws */
        cbxShowThrows = new JCheckBox(
                props.getProperty("options.sidekick.java.showThrows") + " ");
        filterPanel.addComponent(cbxShowThrows);

        /* Visibility Level */
        JLabel visLevelLabel = new JLabel(
                props.getProperty("options.sidekick.java.visLevelLabel") );
        filterPanel.addComponent(visLevelLabel);

        /* Top-Level Visibility Options */
        String[] topLevelVisNames = { "package", "public" };
        cmbTopLevelVis = new JComboBox(topLevelVisNames);
        filterPanel.addComponent(props.getProperty("options.sidekick.java.topLevelVis"),
            cmbTopLevelVis);

        /* Member-Level Visibility Options */
        String[] memberVisNames = { "private", "package", "protected", "public" };
        cmbMemberVis = new JComboBox(memberVisNames);
        filterPanel.addComponent(props.getProperty("options.sidekick.java.memberVis"),
            cmbMemberVis);


        addComponent(filterPanel);

        // ---------------
        // Display Options
        // ---------------
        OptionPanel displayPanel = new OptionPanel();
        displayPanel.setBorder(this.createOptionBorder(
            " " + props.getProperty("options.sidekick.java.displayOptions") + " "
        ));

        /* Arguments */
        JPanel argPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        cbxShowArguments = new JCheckBox(
                props.getProperty("options.sidekick.java.showArgs") + " ");
        argPanel.add(cbxShowArguments);

        /* Argument Names */
        cbxShowArgumentNames = new JCheckBox(
                props.getProperty("options.sidekick.java.showArgNames"));
        argPanel.add(cbxShowArgumentNames);
        displayPanel.addComponent(argPanel);

        /* generics type arguments */
        cbxShowTypeArgs = new JCheckBox(
                props.getProperty("options.sidekick.java.showTypeArgs"));
        displayPanel.addComponent(cbxShowTypeArgs);

        /* qualify nested class/interface names */
        cbxShowNestedName = new JCheckBox(
                props.getProperty("options.sidekick.java.showNestedName"));
        displayPanel.addComponent(cbxShowNestedName);

        /* class/interface modifiers */
        cbxShowIconKeywords = new JCheckBox(
                props.getProperty("options.sidekick.java.showIconKeywords"));
        displayPanel.addComponent(cbxShowIconKeywords);

        /* misc. detail modifiers */
        cbxShowMiscMod = new JCheckBox(
                props.getProperty("options.sidekick.java.showMiscMod"));
        displayPanel.addComponent(cbxShowMiscMod);

        /* Icons */
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cbxShowIcons = new JCheckBox(
                props.getProperty("options.sidekick.java.showIcons") + " ");
        cbxShowIconsLikeEclipse = new JCheckBox(
                props.getProperty("options.sidekick.java.showIconsLikeEclipse"));
        iconPanel.add(cbxShowIcons);
        iconPanel.add(cbxShowIconsLikeEclipse);
        displayPanel.addComponent(iconPanel);

        /* Line Numbers */
        cbxShowLineNum = new JCheckBox(
                props.getProperty("options.sidekick.java.showLineNums"));
        displayPanel.addComponent(cbxShowLineNum);

        /* Sort */
        JPanel sortPanel = new JPanel(new BorderLayout());
        JPanel sortButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        rbSortByLine = new JRadioButton(props.getProperty("options.sidekick.java.sortByLine"));
        rbSortByLine.setActionCommand(props.getProperty("options.sidekick.java.sortByLine"));
        rbSortByName = new JRadioButton(props.getProperty("options.sidekick.java.sortByName"));
        rbSortByName.setActionCommand(props.getProperty("options.sidekick.java.sortByName"));
        rbSortByVisibility = new JRadioButton(props.getProperty("options.sidekick.java.sortByVisibility"));
        rbSortByVisibility.setActionCommand(props.getProperty("options.sidekick.java.sortByVisibility"));
        sortButtonPanel.add(rbSortByLine);
        sortButtonPanel.add(rbSortByName);
        sortButtonPanel.add(rbSortByVisibility);
        sortPanel.add(new JLabel(props.getProperty("options.sidekick.java.sortBy") + ":"), BorderLayout.WEST);
        sortPanel.add(sortButtonPanel, BorderLayout.CENTER);
        displayPanel.addComponent(sortPanel);
        bg = new ButtonGroup();
        bg.add(rbSortByLine);
        bg.add(rbSortByName);
        bg.add(rbSortByVisibility);


        /* show errors in ErrorList */
        cbxShowErrors = new JCheckBox(
                props.getProperty("options.sidekick.java.showErrors"));
        displayPanel.addComponent(cbxShowErrors);

        /* Display Style */
        String[] styleNames = {
            props.getProperty("options.sidekick.java.umlStyle"),
            props.getProperty("options.sidekick.java.javaStyle"),
            props.getProperty("options.sidekick.java.customStyle") };
        cmbStyle = new JComboBox(styleNames);
        displayPanel.addComponent(props.getProperty("options.sidekick.java.displayStyle"),
            cmbStyle);

        /* Custom Display Style Options */
        JLabel customOptionsLabel = new JLabel(
                props.getProperty("options.sidekick.java.customOptions"));
        displayPanel.addComponent(customOptionsLabel);

        JPanel visPanel = new JPanel(new BorderLayout());
        JPanel visButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        rbVisSymbols = new JRadioButton( props.getProperty("options.sidekick.java.custVisAsSymbol"));
        rbVisSymbols.setActionCommand( props.getProperty("options.sidekick.java.custVisAsSymbol"));
        rbVisWords = new JRadioButton( props.getProperty("options.sidekick.java.custVisAsWord"));
        rbVisWords.setActionCommand( props.getProperty("options.sidekick.java.custVisAsWord"));
        rbVisNone = new JRadioButton( props.getProperty("options.sidekick.java.custVisAsNone"));
        rbVisNone.setActionCommand( props.getProperty("options.sidekick.java.custVisAsNone"));
        visButtonPanel.add(rbVisSymbols);
        visButtonPanel.add(rbVisWords);
        visButtonPanel.add(rbVisNone);
        visPanel.add(new JLabel(props.getProperty("options.sidekick.java.useVisibility") + ":"), BorderLayout.WEST);
        visPanel.add(visButtonPanel, BorderLayout.CENTER);
        displayPanel.addComponent(visPanel);
        ButtonGroup vis_bg = new ButtonGroup();
        vis_bg.add(rbVisSymbols);
        vis_bg.add(rbVisWords);
        vis_bg.add(rbVisNone);

        cbxAbstractItalic = new JCheckBox(
                props.getProperty("options.sidekick.java.custAbsAsItalic"));

        cbxStaticUlined = new JCheckBox(
                props.getProperty("options.sidekick.java.custStaAsUlined"));

        cbxTypeIsSuffixed = new JCheckBox(
                props.getProperty("options.sidekick.java.custTypeIsSuffixed"));

        displayPanel.addComponent(cbxAbstractItalic);
        displayPanel.addComponent(cbxStaticUlined);
        displayPanel.addComponent(cbxTypeIsSuffixed);

        addComponent(displayPanel);

        addComponent(new JLabel("<html>&nbsp;&nbsp;<strong><i>" + props.getProperty("options.sidekick.java.reparseWarning")));

        this.addDefaultListeners();

        isInitGui = true;
    }


    /**
     * This method sets the GUI representation of the model to the state
     * specified  by the current option object's state.
    **/
    public void initModel() {
        if (isInitModel()) {
            return;   
        }
        //Log.log(Log.DEBUG, this, "initModel: " + this.getName());
        batchUpdate = true;

        // General Options
        cbxStatusBar.getModel().setSelected( options.getShowStatusBar() );
        cbxAutomaticParse.getModel().setSelected( options.getAutomaticParse() );
        cbxSort.getModel().setSelected( options.getSort() );

        // Filter Options
        cbxShowImports.getModel().setSelected( filterOpt.getShowImports());
        cbxShowFields.getModel().setSelected( filterOpt.getShowFields() );
        cbxShowPrimitives.getModel().setSelected( filterOpt.getShowPrimitives() );
        cbxShowVariables.getModel().setSelected( filterOpt.getShowVariables());
        cbxShowInitializers.getModel().setSelected( filterOpt.getShowInitializers() );
        cbxShowGeneralizations.getModel().setSelected( filterOpt.getShowGeneralizations() );
        cbxShowThrows.getModel().setSelected( filterOpt.getShowThrows() );

        cmbTopLevelVis.setSelectedIndex( filterOpt.getTopLevelVisIndex() );
        cmbMemberVis.setSelectedIndex(   filterOpt.getMemberVisIndex() );

        // Display Options
        cbxShowArguments.getModel().setSelected(     displayOpt.getShowArguments() );
        cbxShowArgumentNames.getModel().setSelected( displayOpt.getShowArgumentNames() );
        cbxShowTypeArgs.getModel().setSelected(      displayOpt.getShowTypeArgs() );
        cbxShowErrors.getModel().setSelected(        displayOpt.getShowErrors());
        cbxShowNestedName.getModel().setSelected(    displayOpt.getShowNestedName() );
        cbxShowIconKeywords.getModel().setSelected(  displayOpt.getShowIconKeywords() );
        cbxShowMiscMod.getModel().setSelected(       displayOpt.getShowMiscMod() );
        cbxShowIcons.getModel().setSelected(         displayOpt.getShowIcons() );
        cbxShowIconsLikeEclipse.getModel().setSelected(         displayOpt.getShowIconsLikeEclipse());
        cbxShowLineNum.getModel().setSelected(       displayOpt.getShowLineNum() );
        rbSortByLine.getModel().setSelected( rbSortByLine.getActionCommand().equals(displayOpt.getSortBy()));
        rbSortByName.getModel().setSelected( rbSortByName.getActionCommand().equals(displayOpt.getSortBy()));
        rbSortByVisibility.getModel().setSelected( rbSortByVisibility.getActionCommand().equals(displayOpt.getSortBy()));

        cmbStyle.setSelectedIndex(displayOpt.getStyleIndex() );

        rbVisSymbols.getModel().setSelected(      displayOpt.getVisSymbols() );
        rbVisWords.getModel().setSelected(        displayOpt.getVisWords() );
        rbVisNone.getModel().setSelected(         displayOpt.getVisNone());
        cbxAbstractItalic.getModel().setSelected( displayOpt.getAbstractItalic() );
        cbxStaticUlined.getModel().setSelected(   displayOpt.getStaticUlined() );
        cbxTypeIsSuffixed.getModel().setSelected( displayOpt.getTypeIsSuffixed() );

        // Set enabled/disabled on showArgumentNames, showPrimitives checkboxes
        if(cbxShowArguments.getModel().isSelected()) {
            cbxShowArgumentNames.getModel().setEnabled(true);
        } else {
            cbxShowArgumentNames.getModel().setSelected(false);
            cbxShowArgumentNames.getModel().setEnabled(false);
        }

        if(cbxShowFields.getModel().isSelected()) {
            cbxShowPrimitives.getModel().setEnabled(true);
        } else {
            cbxShowPrimitives.getModel().setSelected(false);
            cbxShowPrimitives.getModel().setEnabled(false);
        }

        refreshDisplayOptions(styleIndex);

        isInitModel = true;
        batchUpdate = false;
    }


    public void addDefaultListeners() {
        ActionListener defaultListener = this.getDefaultAction();

        // general options
        this.cbxStatusBar.addActionListener(defaultListener);
        this.cbxStatusBar.addActionListener(defaultListener);
        this.cbxAutomaticParse.addActionListener(defaultListener);
        this.cbxSort.addActionListener(defaultListener);

        // filter options
        this.cbxShowImports.addActionListener(defaultListener);
        this.cbxShowFields.addActionListener(defaultListener);
        this.cbxShowPrimitives.addActionListener(defaultListener);
        this.cbxShowVariables.addActionListener(defaultListener);
        this.cbxShowInitializers.addActionListener(defaultListener);
        this.cbxShowGeneralizations.addActionListener(defaultListener);
        this.cbxShowThrows.addActionListener(defaultListener);
        this.cmbTopLevelVis.addActionListener(defaultListener);
        this.cmbMemberVis.addActionListener(defaultListener);

        // display options
        this.cbxShowArguments.addActionListener(defaultListener);
        this.cbxShowArgumentNames.addActionListener(defaultListener);
        this.cbxShowTypeArgs.addActionListener(defaultListener);
        this.cbxShowErrors.addActionListener(defaultListener);
        this.cbxShowNestedName.addActionListener(defaultListener);
        this.cbxShowIconKeywords.addActionListener(defaultListener);
        this.cbxShowMiscMod.addActionListener(defaultListener);
        this.cbxShowIcons.addActionListener(defaultListener);
        this.cbxShowIconsLikeEclipse.addActionListener(defaultListener);
        this.cbxShowLineNum.addActionListener(defaultListener);
        rbSortByLine.addActionListener(defaultListener);
        rbSortByName.addActionListener(defaultListener);
        rbSortByVisibility.addActionListener(defaultListener);

        this.cmbStyle.addActionListener(defaultListener);

        this.rbVisSymbols.addActionListener(defaultListener);
        this.rbVisWords.addActionListener(defaultListener);
        this.rbVisNone.addActionListener(defaultListener);
        this.cbxAbstractItalic.addActionListener(defaultListener);
        this.cbxStaticUlined.addActionListener(defaultListener);
        this.cbxTypeIsSuffixed.addActionListener(defaultListener);
    }


    public void removeDefaultListeners() {
        ActionListener defaultListener = this.getDefaultAction();

        // general options
        this.cbxStatusBar.removeActionListener(defaultListener);
        this.cbxStatusBar.removeActionListener(defaultListener);
        this.cbxAutomaticParse.removeActionListener(defaultListener);
        this.cbxSort.removeActionListener(defaultListener);

        // filter options
        this.cbxShowImports.removeActionListener(defaultListener);
        this.cbxShowFields.removeActionListener(defaultListener);
        this.cbxShowPrimitives.removeActionListener(defaultListener);
        this.cbxShowVariables.removeActionListener(defaultListener);
        this.cbxShowInitializers.removeActionListener(defaultListener);
        this.cbxShowGeneralizations.removeActionListener(defaultListener);
        this.cbxShowThrows.removeActionListener(defaultListener);
        this.cmbTopLevelVis.removeActionListener(defaultListener);
        this.cmbMemberVis.removeActionListener(defaultListener);

        // display options
        this.cbxShowArguments.removeActionListener(defaultListener);
        this.cbxShowArgumentNames.removeActionListener(defaultListener);
        this.cbxShowTypeArgs.removeActionListener(defaultListener);
        this.cbxShowErrors.removeActionListener(defaultListener);
        this.cbxShowNestedName.removeActionListener(defaultListener);
        this.cbxShowIconKeywords.removeActionListener(defaultListener);
        this.cbxShowMiscMod.removeActionListener(defaultListener);
        this.cbxShowIcons.removeActionListener(defaultListener);
        this.cbxShowIconsLikeEclipse.removeActionListener(defaultListener);
        this.cbxShowLineNum.removeActionListener(defaultListener);
        rbSortByLine.removeActionListener(defaultListener);
        rbSortByName.removeActionListener(defaultListener);
        rbSortByVisibility.removeActionListener(defaultListener);

        this.cmbStyle.removeActionListener(defaultListener);

        this.rbVisSymbols.removeActionListener(defaultListener);
        this.rbVisWords.removeActionListener(defaultListener);
        this.rbVisNone.removeActionListener(defaultListener);
        this.cbxAbstractItalic.removeActionListener(defaultListener);
        this.cbxStaticUlined.removeActionListener(defaultListener);
        this.cbxTypeIsSuffixed.removeActionListener(defaultListener);
    }


    /**
     * Allows this option pane to reflect any user interaction directly to
     * JBrowse
    **/
    public void addJBrowseListeners(JavaParser jbrowse) {
        // general options
        ActionListener statusBarOptionAction =
            this.createAction(jbrowse.getStatusBarOptionAction());
        this.cbxStatusBar.addActionListener(statusBarOptionAction);

        ActionListener resizeAction =
            this.createAction(jbrowse.getResizeAction());
        this.cbxStatusBar.addActionListener(resizeAction);

        this.cbxAutomaticParse.addActionListener(this.getDefaultAction());

        ActionListener sortOptionAction =
            this.createAction(jbrowse.getSortOptionAction());
        this.cbxSort.addActionListener(sortOptionAction);

        // filter options
        ActionListener filterOptionAction =
            this.createAction(jbrowse.getFilterOptionAction());
        this.cbxShowImports.addActionListener(filterOptionAction);
        this.cbxShowFields.addActionListener(filterOptionAction);
        this.cbxShowPrimitives.addActionListener(filterOptionAction);
        this.cbxShowVariables.addActionListener(filterOptionAction);
        this.cbxShowInitializers.addActionListener(filterOptionAction);
        this.cbxShowGeneralizations.addActionListener(filterOptionAction);
        this.cmbTopLevelVis.addActionListener(filterOptionAction);
        this.cmbMemberVis.addActionListener(filterOptionAction);

        // display options
        ActionListener displayOptionAction =
            this.createAction(jbrowse.getDisplayOptionAction());
        this.cbxShowArguments.addActionListener(displayOptionAction);
        this.cbxShowArgumentNames.addActionListener(displayOptionAction);
        this.cbxShowTypeArgs.addActionListener(displayOptionAction);
        this.cbxShowErrors.addActionListener(displayOptionAction);
        this.cbxShowThrows.addActionListener(displayOptionAction);
        this.cbxShowNestedName.addActionListener(displayOptionAction);
        this.cbxShowIconKeywords.addActionListener(displayOptionAction);
        this.cbxShowMiscMod.addActionListener(displayOptionAction);
        this.cbxShowIcons.addActionListener(displayOptionAction);
        this.cbxShowIconsLikeEclipse.addActionListener(displayOptionAction);
        this.cbxShowLineNum.addActionListener(displayOptionAction);
        rbSortByLine.addActionListener(displayOptionAction);
        rbSortByName.addActionListener(displayOptionAction);
        rbSortByVisibility.addActionListener(displayOptionAction);

        this.cmbStyle.addActionListener(displayOptionAction);

        this.rbVisSymbols.addActionListener(displayOptionAction);
        this.rbVisWords.addActionListener(displayOptionAction);
        this.rbVisNone.addActionListener(displayOptionAction);
        this.cbxAbstractItalic.addActionListener(displayOptionAction);
        this.cbxStaticUlined.addActionListener(displayOptionAction);
        this.cbxTypeIsSuffixed.addActionListener(displayOptionAction);
    }


    private ActionListener getDefaultAction() {
        if (this.defaultAction == null) {
            this.defaultAction = this.createDefaultAction();
        }
        return this.defaultAction;
    }


    private ActionListener getUpdateOptionsAction() {
        if (this.updateOptionsAction == null) {
            this.updateOptionsAction = new UpdateOptionsAction();
        }
        return this.updateOptionsAction;
    }


    private ActionListener getSetOptionsAction() {
        if (this.setOptionsAction == null) {
            this.setOptionsAction = new SetOptionsAction();
        }
        return this.setOptionsAction;
    }


    private ActionListener createDefaultAction() {
        return this.createAction(null);
    }


    private ActionListener createAction(ActionListener jbrowseAction) {
        Condition batchUpdateCondition = new BatchUpdateCondition();

        if (jbrowseAction == null) {
            return (
                new CompoundAction(
                    this.getUpdateOptionsAction(),
                    new ConditionalAction(
                        batchUpdateCondition,
                        null,
                        this.getSetOptionsAction()
                    )
                )
            );
        } else {
            return (
                new CompoundAction(
                    this.getUpdateOptionsAction(),
                    new ConditionalAction(
                        batchUpdateCondition,
                        null,
                        new CompoundAction(
                            this.getSetOptionsAction(), jbrowseAction
                        )
                    )
                )
            );
        }
    }


    /**
     * Set the enabled and selected/index state of all the display options
     * that are dependant on the cmbStyle control. The state to be set to
     * is determined by the passed styleIndex value. This method is called
     * on init() and upon each change to the sytleIndex via its associated
     * cmbStyle JComboBox.
    **/
    private void refreshDisplayOptions(int styleIndex) {
        if (styleIndex == DisplayOptions.STYLE_UML) {
            // UML
            rbVisSymbols.getModel().setSelected(true);
            rbVisWords.getModel().setSelected(false);
            rbVisNone.getModel().setSelected(false);
            cbxAbstractItalic.getModel().setSelected(true);
            cbxStaticUlined.getModel().setSelected(true);
            cbxTypeIsSuffixed.getModel().setSelected(true);

            rbVisSymbols.getModel().setEnabled(false);
            rbVisWords.getModel().setEnabled(false);
            rbVisNone.getModel().setEnabled(false);
            cbxAbstractItalic.getModel().setEnabled(false);
            cbxStaticUlined.getModel().setEnabled(false);
            cbxTypeIsSuffixed.getModel().setEnabled(false);

        } else if (styleIndex == DisplayOptions.STYLE_JAVA) {
            // Java
            rbVisSymbols.getModel().setSelected(false);
            rbVisWords.getModel().setSelected(true);
            rbVisNone.getModel().setSelected(false);
            cbxAbstractItalic.getModel().setSelected(false);
            cbxStaticUlined.getModel().setSelected(false);
            cbxTypeIsSuffixed.getModel().setSelected(false);

            rbVisSymbols.getModel().setEnabled(false);
            rbVisWords.getModel().setEnabled(false);
            rbVisNone.getModel().setEnabled(false);
            cbxAbstractItalic.getModel().setEnabled(false);
            cbxStaticUlined.getModel().setEnabled(false);
            cbxTypeIsSuffixed.getModel().setEnabled(false);

        } else if (styleIndex == DisplayOptions.STYLE_CUSTOM) {
            // Custom
            rbVisSymbols.getModel().setEnabled(true);
            rbVisWords.getModel().setEnabled(true);
            rbVisNone.getModel().setEnabled(true);
            cbxAbstractItalic.getModel().setEnabled(true);
            cbxStaticUlined.getModel().setEnabled(true);
            cbxTypeIsSuffixed.getModel().setEnabled(true);

        } 
    }


    public GeneralOptions getOptions() { return options; }


    /**
     * The method that sets the option object's state to reflect the values
     * specified by the current state of the JBrowseOptionPane.
    **/
    private void setOptions() {
        // General Options
        options.setShowStatusBar( cbxStatusBar.getModel().isSelected() );
        options.setAutomaticParse( cbxAutomaticParse.getModel().isSelected() );
        options.setSort( cbxSort.getModel().isSelected() );

        // Filter Options
        filterOpt.setShowImports( cbxShowImports.getModel().isSelected() );
        filterOpt.setShowFields( cbxShowFields.getModel().isSelected() );
        filterOpt.setShowPrimitives( cbxShowPrimitives.getModel().isSelected() );
        filterOpt.setShowVariables( cbxShowVariables.getModel().isSelected());
        filterOpt.setShowInitializers( cbxShowInitializers.getModel().isSelected() );
        filterOpt.setShowGeneralizations( cbxShowGeneralizations.getModel().isSelected() );
        filterOpt.setShowThrows( cbxShowThrows.getModel().isSelected() );

        filterOpt.setTopLevelVisIndex( topLevelVisIndex );
        filterOpt.setMemberVisIndex( memberVisIndex );

        // Display Options
        displayOpt.setShowArguments( cbxShowArguments.getModel().isSelected() );
        displayOpt.setShowArgumentNames( cbxShowArgumentNames.getModel().isSelected() );
        displayOpt.setShowTypeArgs( cbxShowTypeArgs.getModel().isSelected());
        displayOpt.setShowErrors(cbxShowErrors.getModel().isSelected());
        displayOpt.setShowNestedName( cbxShowNestedName.getModel().isSelected() );
        displayOpt.setShowIconKeywords( cbxShowIconKeywords.getModel().isSelected() );
        displayOpt.setShowMiscMod( cbxShowMiscMod.getModel().isSelected() );
        displayOpt.setShowIcons(cbxShowIcons.getModel().isSelected());
        displayOpt.setShowIconsLikeEclipse(cbxShowIconsLikeEclipse.getModel().isSelected());
        displayOpt.setShowLineNum( cbxShowLineNum.getModel().isSelected() );
        displayOpt.setSortBy(bg.getSelection().getActionCommand());
        displayOpt.setStyleIndex( styleIndex );

        displayOpt.setVisSymbols( rbVisSymbols.getModel().isSelected() );
        displayOpt.setVisWords( rbVisWords.getModel().isSelected() );
        displayOpt.setVisNone( rbVisNone.getModel().isSelected() );
        displayOpt.setAbstractItalic( cbxAbstractItalic.getModel().isSelected() );
        displayOpt.setStaticUlined( cbxStaticUlined.getModel().isSelected() );
        displayOpt.setTypeIsSuffixed( cbxTypeIsSuffixed.getModel().isSelected() );
    }


    /**
     * Adds a component to the JBrowse option pane.
     * <ul>
     *   <li>Components are added in a vertical fashion, one per row</li>
     *   <li>Components fill the horizontal space</li>
     * </ul>
     * Overrides org.gjt.sp.jedit.AbstractOptionPane#addComponent(java.awt.Component)
    **/
    public void addComponent(Component comp) {
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = 1;
        cons.gridwidth = cons.REMAINDER;
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.anchor = GridBagConstraints.CENTER;
        cons.weightx = 1.0f;

        gridBag.setConstraints(comp,cons);
        contents.add(comp);
    }


    /**
     * Creates the border of the option panels
    **/
    private Border createOptionBorder(String title) {
        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createBevelBorder(BevelBorder.LOWERED),
                title, TitledBorder.CENTER, TitledBorder.TOP
            ),
            BorderFactory.createEmptyBorder(0, 3, 1, 1)
        );

        return border;
    }


    /**
     * Implements the option pane logic and interactions between components
    **/
    private class UpdateOptionsAction implements ActionListener {
        public void actionPerformed(ActionEvent evt)
        {
            Object actionSource = evt.getSource();

            // Filter Options
            if (actionSource == cbxShowFields) {
                if(cbxShowFields.getModel().isSelected()) {
                    cbxShowPrimitives.getModel().setEnabled(true);
                } else {
                    cbxShowPrimitives.getModel().setSelected(false);
                    cbxShowPrimitives.getModel().setEnabled(false);
                }
            } else if (actionSource == cmbTopLevelVis) {
                topLevelVisIndex = cmbTopLevelVis.getSelectedIndex();
            } else if (actionSource == cmbMemberVis) {
                memberVisIndex = cmbMemberVis.getSelectedIndex();
            }

            // Display Style Options
            else if (actionSource == cmbStyle) {
                styleIndex = cmbStyle.getSelectedIndex();
                refreshDisplayOptions(styleIndex);
            }
            else if (actionSource == cbxShowArguments) {
                if(cbxShowArguments.getModel().isSelected()) {
                    cbxShowArgumentNames.getModel().setEnabled(true);
                } else {
                    cbxShowArgumentNames.getModel().setSelected(false);
                    cbxShowArgumentNames.getModel().setEnabled(false);
                }
            }
        }
    }


    /**
     * Action to encapsulate a call to JBrowseOption.setOptions
    **/
    private class SetOptionsAction implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            JBrowseOptionPane.this.setOptions();
        }
    }


    private class CompoundAction implements ActionListener {
        private ActionListener[] listeners;


        public CompoundAction(ActionListener listener1,
                              ActionListener listener2
        ) {
            this.listeners = new ActionListener[] {
                listener1, listener2
            };
        }


        public CompoundAction(ActionListener[] listeners) {
            this.listeners = listeners;
        }


        public void actionPerformed(ActionEvent evt) {
            for (int i = 0; i < this.listeners.length; i++) {
                if (this.listeners[i] != null) {
                    this.listeners[i].actionPerformed(evt);
                }
            }
        }
    }


    /**
     * Condition interface
    **/
    interface Condition {
        boolean test();
    }


    private class BatchUpdateCondition implements Condition {
        public boolean test() {
            return JBrowseOptionPane.this.batchUpdate;
        }
    }


    private class ConditionalAction implements ActionListener {
        private Condition condition;
        private ActionListener trueAction;
        private ActionListener falseAction;


        public ConditionalAction(Condition condition,
                                 ActionListener trueAction,
                                 ActionListener falseAction
        ) {
            this.condition = condition;
            this.trueAction = trueAction;
            this.falseAction = falseAction;
        }


        public void actionPerformed(ActionEvent evt) {
            if (this.condition.test()) {
                if (this.trueAction != null) {
                    this.trueAction.actionPerformed(evt);
                }
            } else {
                if (this.falseAction != null) {
                    this.falseAction.actionPerformed(evt);
                }
            }
        }
    }


    /**
     * This class is used to for panels that require a gridBag layout for
     * placement into (for example) an OptionPane.
     */
    static class OptionPanel extends JPanel
    {
        /**
         * The layout manager.
         */
        protected GridBagLayout gridBag;

        /**
         * The number of components already added to the layout manager.
         */
        protected int y;

        /**
         * Creates a new option pane.
         * @param name The internal name
         */
        public OptionPanel() {
            setLayout(gridBag = new GridBagLayout());
        }


        /**
         * Adds a labeled component to the option pane.
         * @param label The label
         * @param comp The component
         */
        protected void addComponent(String label, Component comp) {
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridy = y++;
            cons.gridheight = 1;
            cons.gridwidth = 3;
            cons.fill = GridBagConstraints.BOTH;
            cons.weightx = 1.0f;

            cons.gridx = 0;
            JLabel l = new JLabel(label,SwingConstants.RIGHT);
            gridBag.setConstraints(l,cons);
            add(l);

            cons.gridx = 3;
            cons.gridwidth = 1;
            gridBag.setConstraints(comp,cons);
            add(comp);
        }


        /**
         * Adds a component to the option pane.
         * @param comp The component
         */
        protected void addComponent(Component comp) {
            GridBagConstraints cons = new GridBagConstraints();
            cons.gridy = y++;
            cons.gridheight = 1;
            cons.gridwidth = cons.REMAINDER;
            cons.fill = GridBagConstraints.HORIZONTAL;
            cons.anchor = GridBagConstraints.WEST;
            cons.weightx = 1.0f;

            gridBag.setConstraints(comp,cons);
            add(comp);
        }
    }
}

