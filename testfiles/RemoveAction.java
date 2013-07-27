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


package gruntspud.actions;

import gruntspud.CVSCommandHandler;
import gruntspud.CVSFileNode;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ResourceUtil;
import gruntspud.ui.UIUtil;
import gruntspud.ui.report.FileInfoPane;
import gruntspud.ui.report.RemoveFileInfoPane;
import gruntspud.ui.view.ViewManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.remove.RemoveCommand;

/**
 * Action to remove files locally and mark for removal from CVS repository.
 *
 * @author     magicthize
 */
public class RemoveAction
    extends ReportingGruntspudAction {
    static ResourceBundle res = ResourceBundle.getBundle(
        "gruntspud.actions.ResourceBundle");

    /**
     *  Constructor for the RemoveAction object
     * 
     * @param context context
     */
    public RemoveAction(GruntspudContext context) {
        super(res, "removeAction", context);

        putValue(Action.NAME, Constants.ACTION_REMOVE);
        putValue(GruntspudAction.ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_REMOVE));
        putValue(Action.SHORT_DESCRIPTION, "Remove selection");
        putValue(Action.LONG_DESCRIPTION,
                 "Remove the current selection from CVS");
        putValue(Action.MNEMONIC_KEY, new Integer('r'));
        putValue(DefaultGruntspudAction.SMALL_ICON,
                 UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_REMOVE));
        setUpdatesFiles(true);
    }

    /* (non-Javadoc)
     * @see gruntspud.actions.ReportingGruntspudAction#createFileInfoPane()
     */
    public FileInfoPane createFileInfoPane() {
        return new RemoveFileInfoPane(getContext());
    }

    /* (non-Javadoc)
     * @see gruntspud.actions.ReportingGruntspudAction#getFileInfoName()
     */
    public String getFileInfoName() {
        return "removeFileInfo";
    }

    /* (non-Javadoc)
     * @see gruntspud.actions.ReportingGruntspudAction#getFileInfoText()
     */
    public String getFileInfoText() {
        return res.getString("removeAction.fileInfoText");
    }

    /* (non-Javadoc)
     * @see gruntspud.actions.GruntspudAction#checkAvailable()
     */
    public boolean checkAvailable() {
        ViewManager mg = getContext().getViewManager();
        CVSFileNode[] sel = mg.getSelectedNodes();

        return!CVSCommandHandler.getInstance().isCommandRunning() &&
            mg.isHomeExists() && (sel != null) && (sel.length > 0) &&
            ( (mg.getSelectedFileCount() == sel.length) ||
             ( (mg.getSelectedFileCount() == 0) && (sel.length == 1))) &&
            (mg.getSelectedInCVSCount() == sel.length);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        Component parent = getParentComponentForEvent(evt);
        CVSFileNode[] sel = getContext().getViewManager()
            .getNodesToPerformActionOn();
        JPanel p = new JPanel(new BorderLayout());
        JCheckBox recurse = new JCheckBox(res.getString("removeAction.removeRecursively.text"));
        recurse.setHorizontalAlignment(SwingConstants.CENTER);
        recurse.setMnemonic(ResourceUtil.getResourceMnemonic(res, "removeAction.removeRecursively.mnemonic"));
        p.add(new JLabel(MessageFormat.format(res.getString("removeAction.confirm.text"),
        							new String[] { String.valueOf(sel.length) } ) ), BorderLayout.CENTER);
        p.add(recurse, BorderLayout.SOUTH);

        if ( ( (sel.length > 1) || ( (sel.length == 1) && !sel[0].isLeaf()))) {
           if(isBypassOptions(evt.getModifiers())) {
               recurse.setSelected(true);
           }
           else {
              if(JOptionPane.showConfirmDialog(parent, p, res.getString("removeAction.confirm.title"),
                                           JOptionPane.YES_NO_OPTION,
                                           JOptionPane.WARNING_MESSAGE,
                                           UIUtil.getCachedIcon(
                Constants.ICON_TOOL_LARGE_REMOVE)) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
        RemoveCommand cmd = new RemoveCommand();
        cmd.setDeleteBeforeRemove(true);
        cmd.setRecursive(recurse.isSelected());


        CVSCommandHandler.getInstance().runCommandGroup(parent, getContext(), null,
            new Command[] {cmd}
            , sel, null, false, null, null, this, getEnabledOptionalListeners());
    }
}
