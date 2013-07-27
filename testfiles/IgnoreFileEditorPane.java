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

package gruntspud.ui.ignore;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.actions.AbstractNormalAddAction;
import gruntspud.actions.AbstractNormalRemoveAction;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;
import gruntspud.ui.XTextField;
import gruntspud.ui.OptionDialog.Option;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class IgnoreFileEditorPane
    extends JPanel {
    private File file;
    private GruntspudContext context;
    private JList ignoreList;
    private Action addAction;
    private Action removeAction;

    /**
     *  Constructor for the DiffOptionsPane object
     *
     *@param  host  Description of the Parameter
     */
    public IgnoreFileEditorPane(File file, GruntspudContext context) {
        super(new BorderLayout());
		
		setOpaque(false);

        this.file = file;
        this.context = context;

        //  Create the text area
        ignoreList = new JList(new IgnoreFileModel(context, file));
        ignoreList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                setAvailableActions();
            }
        });
        ignoreList.setVisibleRowCount(10);

        //  Create the toolbar
        JToolBar toolBar = new JToolBar("Ignore file editor tools");
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setBorder(null);
        toolBar.setFloatable(false);
		boolean showSelectiveText = context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);
        toolBar.add(UIUtil.createButton(addAction = new AddAction(), showSelectiveText,
                                        false));
        toolBar.add(UIUtil.createButton(removeAction = new RemoveAction(),
		showSelectiveText, false));

        //
        JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setOpaque(false);
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        JLabel l = new JLabel(file.getAbsolutePath(), JLabel.CENTER);
        l.setToolTipText(file.getAbsolutePath());

        //  Build this
        add(topPanel, BorderLayout.NORTH);

        JScrollPane ignoreListScroller = new JScrollPane(ignoreList);
        add(ignoreListScroller, BorderLayout.CENTER);
        add(l, BorderLayout.SOUTH);

        //  Set the intially available actions
        setAvailableActions();
    }

    /**
     * Save the profiles
     */
    public void save() {
        ( (IgnoreFileModel) ignoreList.getModel()).save();
    }

    /**
     *  Set what actions are available depending on state
     */
    private void setAvailableActions() {
        removeAction.setEnabled(ignoreList.getSelectedIndices().length == 1);
    }

    class AddAction
        extends AbstractNormalAddAction {
        /**
         *  Constructor for the CutAction object
         */
        AddAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            JPanel n = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
            final XTextField pattern = new XTextField(15);
            n.add(new JLabel("Pattern: "));
            n.add(pattern);

            JPanel z = new JPanel(new BorderLayout());
            z.add(new JLabel(UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_NEW_FOLDER)),
                  BorderLayout.WEST);
            z.add(n);

            OptionDialog.Option ok = new OptionDialog.Option("Add",
                "Add an ignore pattern", 'o');
            OptionDialog.Option cancel = new OptionDialog.Option("Cancel",
                "Cancel", 'c');
            OptionDialog.Option opt = OptionDialog.showOptionDialog(
                "ignorePattern",
                context, IgnoreFileEditorPane.this,
                new OptionDialog.Option[] {ok, cancel}
                , z,
                "Add ignore pattern", ok,
                new OptionDialog.Callback() {
                public boolean canClose(OptionDialog dialog,
                                        OptionDialog.Option option) {
                    return true;
                }

                public void close(OptionDialog dialog, Option option) {
                    // TODO Auto-generated method stub
                    
                }
            }

            , false, true);

            if ( (opt != ok) || (pattern.getText().length() == 0)) {
                return;
            }

            ( (IgnoreFileModel) ignoreList.getModel()).addPattern(pattern.
                getText());
        }
    }

    class RemoveAction
        extends AbstractNormalRemoveAction {
        /**
         *  Constructor for the DeleteAction object
         */
        RemoveAction() {
            super();
        }

        public void actionPerformed(ActionEvent evt) {
            ( (IgnoreFileModel) ignoreList.getModel()).removePatternAt(
                ignoreList.getSelectedIndex());
        }
    }
}
