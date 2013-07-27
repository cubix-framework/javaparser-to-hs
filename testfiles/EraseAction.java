/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.actions;

import gruntspud.CVSCommandHandler;
import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ResourceUtil;
import gruntspud.ui.CommandProgressDialog;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.UIUtil;
import gruntspud.ui.view.ViewManager;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * An action to erase the selection (i.e delete it from the local workspace, but
 * not the repository). Files not under CVS control will also be deleted
 * 
 * @author magicthize
 */
public class EraseAction extends DefaultGruntspudAction implements Runnable {

    static ResourceBundle res = ResourceBundle.getBundle("gruntspud.actions.ResourceBundle");

    private CVSFileNode[] sel;

    private JCheckBox recurse, hidden;

    private JComponent parent;

    private boolean bypass;

    private CommandProgressDialog progressDialog;

    /**
     * Constructor for the EraseAction object
     * 
     * @param context
     *            context
     */
    public EraseAction(GruntspudContext context) {
        super(res, "eraseAction", context);
        putValue(GruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_ERASE));
        putValue(GruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_DELETE));
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.actions.GruntspudAction#checkAvailable()
     */
    public boolean checkAvailable() {
        ViewManager mg = getContext().getViewManager();
        CVSFileNode[] sel = mg.getSelectedNodes();

        return !CVSCommandHandler.getInstance().isCommandRunning() && mg.isHomeExists() && (sel != null) && (sel.length > 0)
                        && ((mg.getSelectedFileCount() == sel.length) || ((mg.getSelectedFileCount() == 0) && (sel.length == 1)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent evt) {
        sel = getContext().getViewManager().getNodesToPerformActionOn();
        parent = getContext().getHost().getMainComponent();
        recurse = new JCheckBox(res.getString("eraseAction.eraseRecursively.text"));
        recurse.setHorizontalAlignment(SwingConstants.CENTER);
        recurse.setMnemonic(ResourceUtil.getResourceMnemonic(res, "eraseAction.eraseRecursively.mnemonic"));

        hidden = new JCheckBox(res.getString("eraseAction.includeIgnoredAndHidden.text"));
        hidden.setHorizontalAlignment(SwingConstants.CENTER);
        hidden.setMnemonic(ResourceUtil.getResourceMnemonic(res, "eraseAction.includeIgnoredAndHidden.text"));
        hidden.setEnabled(false);
        recurse.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {
                hidden.setEnabled(recurse.isSelected());
            }
        });

        JPanel g = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.CENTER;
        UIUtil.jGridBagAdd(g, new MultilineLabel(MessageFormat.format(res.getString("eraseAction.confirmErase.text"),
                        new String[] { String.valueOf(sel.length)})), gbc, GridBagConstraints.REMAINDER);
        gbc.anchor = GridBagConstraints.WEST;
        UIUtil.jGridBagAdd(g, recurse, gbc, GridBagConstraints.REMAINDER);
        gbc.weighty = 1.0;
        UIUtil.jGridBagAdd(g, hidden, gbc, GridBagConstraints.REMAINDER);

        bypass = isBypassOptions(evt.getModifiers());

        if (!bypass) {
            if (JOptionPane.showConfirmDialog(parent, g, res.getString("eraseAction.confirmErase.title"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, UIUtil
                                            .getCachedIcon(Constants.ICON_TOOL_LARGE_ERASE)) == JOptionPane.NO_OPTION) { return; }
        }

        progressDialog = CommandProgressDialog.createDialog(parent, getContext(), Constants.CVS_COMMAND_PROGRESS_DIALOG_GEOMETRY,
                        null, "Directory:", "File:", "Command progress", true, true);
        progressDialog.setValue1Text("Erasing files ..");
        progressDialog.setValue1ToolTipText("");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress2Visible(false);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                progressDialog.setVisible(true);
            }

        });

        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            CVSFileNode[] nodes = sel;

            if (recurse.isSelected()) {
                Vector v = new Vector();
                progressDialog.setValue1Text("Gathering files");
                progressDialog.setString("");
                progressDialog.setIndeterminate(true);
                for (int i = 0; i < sel.length && !progressDialog.isCancelled(); i++) {
                    progressDialog.setProgressValue(i);
                    if (hidden.isSelected()) {
                        sel[i].setIncludeAllFiles(true);
                        sel[i].reset();
                    }

                    sel[i].recurseNodes(v, true, true);
                }

                nodes = new CVSFileNode[v.size()];
                v.copyInto(nodes);
            }
            
            if(progressDialog.isCancelled()) {
                return;
            }

            Constants.IO_LOG.info("Will erase " + sel.length + " files");

            boolean inCVS = false;

            if (!bypass) {
                for (int i = 0; (i < nodes.length) && !inCVS; i++) {
                    if (nodes[i].getEntry() != null) {
                        getContext().getHost().writeToConsole(
                                        getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_GRUNTSPUD),
                                        MessageFormat.format(res
                                                        .getString("eraseAction.consoleOutput.warning.underCVSControl.text"),
                                                        new String[] { nodes[i].getName()}));
                        inCVS = true;
                    }
                }

                if (inCVS
                                && (JOptionPane.showConfirmDialog(progressDialog,

                                res.getString("eraseAction.warning.underCVSControl.text"), res
                                                .getString("eraseAction.warning.underCVSControl.title"), JOptionPane.YES_NO_OPTION,
                                                JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)) { return; }
            }

            progressDialog.setProgressMaximum(nodes.length);
            progressDialog.setValue1Text("Erasing files");
            progressDialog.setIndeterminate(false);
            for (int i = 0; i < nodes.length && !progressDialog.isCancelled(); i++) {
                progressDialog.setProgressValue(i);
                File f = nodes[i].getFile();
                progressDialog.setValue1Text(f.getName());

                if (!f.exists()) {
                    getContext().getHost().writeToConsole(
                                    getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_GRUNTSPUD),
                                    MessageFormat.format(res.getString("eraseAction.consoleOutput.warning.doesNotExist.text"),
                                                    new String[] { nodes[i].getName()}));
                } else {
                    if (f.delete()) {
//                        getContext().getHost().writeToConsole(
//                                        getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_GRUNTSPUD),
//                                        MessageFormat.format(res.getString("eraseAction.consoleOutput.warning.erased.text"),
//                                                        new String[] { nodes[i].getName()}));
                    } else {
                        getContext().getHost().writeToConsole(
                                        getContext().getTextStyleModel().getStyle(Constants.OPTIONS_STYLE_ERRORS),
                                        MessageFormat.format(res.getString("eraseAction.consoleOutput.error.failedToErase.text"),
                                                        new String[] { nodes[i].getName()}));

                    }
                }
            }

            //
            for (int i = 0; i < sel.length; i++) {
                getContext().getViewManager().nodeUpdated((CVSFileNode) sel[i]);
            }
            getContext().getViewManager().resetNodeUpdateTimer();
        } finally {
            progressDialog.setVisible(false);
        }
    }

    private void recurseNodes(CVSFileNode node, Vector v, boolean includeDir) {
        if (node.isLeaf()) {
            v.addElement(node);
        } else {
            for (int i = 0; i < node.getChildCount() && !progressDialog.isCancelled(); i++) {
                CVSFileNode n = (CVSFileNode) node.getChildAt(i);
                recurseNodes(n, v, includeDir);
            }

            if (includeDir) {
                v.addElement(node);
            }
        }
    }
}