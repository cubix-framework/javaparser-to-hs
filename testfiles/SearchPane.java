/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.editor;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ResourceUtil;
import gruntspud.ui.StringListComboBox;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A panel that may be used to edit a <code>Search</code> criteria
 * object.
 *
 * @author brett smith
 * @version 1.0
 */
public class SearchPane
    extends JPanel
    implements DocumentListener,
    ActionListener {
	
	static ResourceBundle res = ResourceBundle.getBundle(
	"gruntspud.editor.ResourceBundle");
	
    //  Private instance variables
    private StringListComboBox pattern;
    private JCheckBox caseSensitive;
    private JCheckBox wholeWords;
    private SearchCriteria criteria;
    private boolean changed;
    private GruntspudContext context;

    /**
     * Creates a new SearchPane object.
     *
     * @param context context
     */
    public SearchPane(GruntspudContext context) {
        this(new SearchCriteria(), context);
    }

    /**
     * Creates a new SearchPane object.
     *
     * @param criteria search criteria
     * @param context context
     */
    public SearchPane(SearchCriteria criteria, GruntspudContext context) {
        super(new BorderLayout());

        this.context = context;

        //
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1.0;

        JLabel l = new JLabel(res.getString("searchPane.find.text"));
        UIUtil.jGridBagAdd(p, l, gbc, GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(p,
                           pattern = new StringListComboBox(context,
            context.getHost().getProperty(Constants.
                                          EDITOR_SEARCH_DIALOG_HISTORY,
                                          ""), true), gbc,
                           GridBagConstraints.REMAINDER);
        try {
        	Method spdv = pattern.getClass().getMethod(
        					"setPrototypeDisplayValue", new Class[] { String.class } );
        	spdv.invoke(pattern, new Object[] { "WWWWWWWWWWWWWWWWWWWWWWW" });
        }
        catch(Throwable t) {        	
        }
        pattern.addActionListener(this);
        pattern.getEditorDocument().addDocumentListener(this);
        l.setDisplayedMnemonic('i');
        l.setLabelFor(pattern);

        //
        caseSensitive = new JCheckBox(res.getString("searchPane.caseSensitive.text"));
        caseSensitive.setMnemonic(ResourceUtil.getResourceMnemonic(res,
        			"searchPane.caseSensitive.mnemonic"));
        caseSensitive.addActionListener(this);
        wholeWords = new JCheckBox(res.getString("searchPane.wholeWords.text"));
        wholeWords.setMnemonic(ResourceUtil.getResourceMnemonic(res,
        			"searchPane.wholeWords.mnemonic"));
        wholeWords.addActionListener(this);

        JPanel f = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(3, 3, 3, 3);
        gbc2.anchor = GridBagConstraints.WEST;
        gbc2.fill = GridBagConstraints.NONE;
        UIUtil.jGridBagAdd(f, caseSensitive, gbc2, GridBagConstraints.REMAINDER);
        gbc2.weighty = 1.0;
        UIUtil.jGridBagAdd(f, wholeWords, gbc2, GridBagConstraints.REMAINDER);

        //
        add(p, BorderLayout.CENTER);
        add(f, BorderLayout.SOUTH);

        //  Set the current criteria
        setCriteria(criteria);
    }

    /**
     * Store the search history
     */
    public void cleanUp() {
        context.getHost().setProperty(Constants.EDITOR_SEARCH_DIALOG_HISTORY,
                                      pattern.getStringListPropertyString());
    }

    /**
     * Return if the search criteria has changed since the last
     * <code>applyCriteria</code> or <code>setCritieria</code>
     *
     * @return criteria has changed
     */
    public boolean isCriteriaChanged() {
        return changed;
    }
    
    public void actionPerformed(ActionEvent evt) {
        changed = true;
    }
    
    public void insertUpdate(DocumentEvent e) {
        changed = true;
    }
    
    public void removeUpdate(DocumentEvent e) {
        changed = true;
    }
    
    public void changedUpdate(DocumentEvent e) {
        changed = true;
    }

    /**
     * Set the <code>SearchCriteria</code> object that this panel
     * represents.
     *
     * @param criteria the search criteria
     */
    public void setCriteria(SearchCriteria criteria) {
        this.criteria = criteria;
        pattern.setSelectedItem(criteria.getPattern());
        caseSensitive.setSelected(criteria.isCaseSensitive());
        wholeWords.setSelected(criteria.isWholeWords());
        changed = false;
    }

    /**
     * Apply the current settings to the <code>SearchCriteria</code>
     * object that this panel represents
     */
    public void applyCriteria() {
        criteria.setCaseSensitive(caseSensitive.isSelected());
        criteria.setWholeWords(wholeWords.isSelected());
        criteria.setPattern(pattern.getSelectedItem().toString());
        changed = false;
    }

    /**
     * Return the <code>SearchCriteria</code> that this panel
     * represents
     *
     * @return search criteria
     */
    public SearchCriteria getCriteria() {
        return criteria;
    }

    /**
     * Get the search term component
     * 
     * @return search term component
     */
    public StringListComboBox getSearchTermComponent() {
        return pattern;
    }
}
