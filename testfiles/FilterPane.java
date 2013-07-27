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

package gruntspud.ui.preferences;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.filter.CVSFileFilter;
import gruntspud.filter.CVSFileFilterModel;
import gruntspud.ui.FilterListCellRenderer;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class FilterPane extends JPanel
    implements ActionListener {

    protected CVSFileFilterPane filterPane;
    private JButton apply;
    private JButton newFilter;
    private JButton removeFilter;
    private JComboBox filter;
    private GruntspudContext context;

    /**
     * Creates a new FilterView object.
     */
    public FilterPane(GruntspudContext context) {
        super(new BorderLayout());
        setOpaque(false);

        this.context = context;


        //
        JPanel top = new JPanel(new GridBagLayout());
		top.setOpaque(false); 
        top.setBorder(BorderFactory.createTitledBorder("Preset filter"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        UIUtil.jGridBagAdd(top, new JLabel("Preset:"), gbc, 1);
        gbc.weightx = 1.0;
        UIUtil.jGridBagAdd(top,
                           filter = new JComboBox(context.getFilterModel()), gbc,
                           1);
        filter.setRenderer(new FilterListCellRenderer());
        filter.addActionListener(this);
        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(top, newFilter = new JButton("New"), gbc,
                           GridBagConstraints.RELATIVE);
        newFilter.setMnemonic('n');
        newFilter.addActionListener(this);
        UIUtil.jGridBagAdd(top, removeFilter = new JButton("Remove"), gbc,
                           GridBagConstraints.REMAINDER);
        removeFilter.setMnemonic('r');
        removeFilter.addActionListener(this);

        //
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottom.setOpaque(false);
        bottom.add(apply = new JButton("Apply"));
        apply.setMnemonic('a');
        apply.addActionListener(this);

        //
        filterPane = new CVSFileFilterPane();		
		filterPane.setOpaque(false);
        filterPane.setFilter( (CVSFileFilter) filter.getSelectedItem());

        //
        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(filterPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

    }

    public void setFilter(CVSFileFilter filter) {
        filterPane.setFilter(filter);
    }

    public void apply() {

        filterPane.apply();
        context.getViewManager().refilterAndResort();

    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == apply) {
            apply();
        }
        else if (evt.getSource() == filter) {
            filterPane.setFilter( (CVSFileFilter) filter.getSelectedItem());
        }
        else if (evt.getSource() == newFilter) {
            newFilter();
        }
        else if (evt.getSource() == removeFilter) {
            context.getFilterModel().removeFilter( (
                CVSFileFilter) filter.getSelectedItem());

        }
        setAvailableActions();
    }

    private void newFilter() {
        JPanel n = new JPanel(new GridBagLayout());
        n.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(n,
                           new JLabel(UIUtil.getCachedIcon(Constants.
            ICON_TOOL_LARGE_FILTER)),
                           gbc, 1);
        UIUtil.jGridBagAdd(n, new JLabel("Name: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;

        final XTextField filterName = new XTextField(15);
        UIUtil.jGridBagAdd(n, filterName, gbc, GridBagConstraints.RELATIVE);

        OptionDialog.Option create = new OptionDialog.Option("Create",
            "Create filter", 'f');
        final OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
            "Cancel", 'c');
        OptionDialog.Option opt = OptionDialog.showOptionDialog("newFilter",
            context, this,
            new OptionDialog.Option[] {create, cancel}
            , n, "New Filter",
            create,
            new OptionDialog.Callback() {
            public boolean canClose(OptionDialog dialog,
                                    OptionDialog.Option option) {
                if (option != cancel) {
                    if (filterName.getText().length() == 0) {
                        JOptionPane.showMessageDialog(FilterPane.this,
                            "Filter must have a name", "Error",
                            JOptionPane.ERROR_MESSAGE);

                        return false;
                    }
                    else {
                        CVSFileFilterModel model = context.getFilterModel();

                        for (int i = 0; i < model.getSize(); i++) {
                            if (model.getFilterAt(i).getName().equals(
                                filterName.getText())) {
                                JOptionPane.showMessageDialog(FilterPane.this,
                                    "Filter that that name already exists",
                                    "Error", JOptionPane.ERROR_MESSAGE);

                                return false;
                            }
                        }
                    }
                }

                return true;
            }

            public void close(OptionDialog dialog, Option option) {
                // TODO Auto-generated method stub
                
            }
        }

        , false, true);

        if (opt != create) {
            return;
        }

        CVSFileFilter filter = new CVSFileFilter(filterName.getText());
        context.getFilterModel().addFilter(filter);
        context.getFilterModel().setSelectedItem(filter);
    }

    private void setAvailableActions() {
        CVSFileFilter f = (CVSFileFilter) filter.getSelectedItem();
        removeFilter.setEnabled( (f != null) && !f.isPreset());
    }
}
