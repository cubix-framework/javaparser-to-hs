/*
 * :tabSize=4:indentSize=4:noTabs=true:
 * :folding=explicit:collapseFolds=1:
 *
 * (c) 2005 Marcelo Vanzin
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
package p4plugin.action;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.util.Log;

import common.gui.OkCancelButtons;

import projectviewer.ProjectViewer;
import projectviewer.action.Action;
import projectviewer.vpt.VPTNode;

import p4plugin.Perforce;
import p4plugin.config.P4Config;

/**
 *  A dialog that shows a list of currently pending changelists from the
 *  user according to the configuration set in the active project in the
 *  given view. If there's no active project, or there's some problem
 *  executing perforce, {@link #getChangeList(Component)} will return
 *  null.
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: ChangeListDialog.java 6451 2006-04-09 03:04:41Z vanza $
 *  @since      P4P 0.1
 */
public class ChangeListDialog implements Runnable,
                                         Perforce.Visitor,
                                         FocusListener
{

    private boolean allowOthers;
    private boolean showDefault;

    private JComboBox   options;
    private JTextField  other;
    private List        clists;

    private String      change;
    private View        view;
    private boolean     cancelled;

    /**
     *  Runs perforce to figure out the user's pending change lists.
     */
	public ChangeListDialog(View v) {
        this(v, true);
    }

    public ChangeListDialog(View v, boolean showDefault) {
        this(v, showDefault, false);
    }

    public ChangeListDialog(View v, boolean showDefault,
                                    boolean allowOthers)
    {
        this.view = v;
        P4Config cfg = P4Config.getProjectConfig(v);
        if (cfg != null) {
            List args = new LinkedList();
            if (cfg.getUser() != null) {
                args.add("-u");
                args.add(cfg.getUser());
            }
            if (cfg.getClient() != null) {
                args.add("-c");
                args.add(cfg.getClient());
            }
            args.add("-s");
            args.add("pending");

            Perforce p4 = new Perforce("changes",
                                       (String[]) args.toArray(new String[args.size()]));

            try {
               p4.exec(v).waitFor();
            } catch (Exception e) {
                Log.log(Log.ERROR, this, e);
                v.getStatus().setMessageAndClear(
                    jEdit.getProperty("p4plugin.action.error",
                                      new String[] { e.getMessage() }));
                return;
            }

            if (!p4.isSuccess()) {
                p4.showError(v);
                cancelled = true;
                return;
            }

            clists = new LinkedList();
            if (showDefault)
                clists.add(jEdit.getProperty("p4plugin.action.changelists.default"));
            p4.processOutput(this);


        }
        this.allowOthers = allowOthers;
        this.showDefault = showDefault;
        this.cancelled   = false;
	}

    /** Perforce.Visitor implementation. */
    public boolean process(String line) {
        int nStart = line.indexOf("Change") + 7;
        String number = line.substring(nStart, line.indexOf(" ", nStart));

        int dStart = line.indexOf("*pending*") + 11;
        String desc = line.substring(dStart, line.lastIndexOf("'"));

        clists.add(new ChangeList(number, desc));
        return true;
    }

    /**
     *  Shows the dialog to the user and returns the chosen changelist.
     *  "null" means the user chose the default change list. This method
     *  will return null if no active project was found, or if there was
     *  an error running perforce.
     *
     *  @throws IllegalArgumentException    When the user cancels the
     *                                      option dialog. Used for lack of
     *                                      a better way to do this.
     */
    public String getChangeList(Component parent) {
        if (clists == null) return null;

        options = new JComboBox(clists.toArray());
        options.addFocusListener(this);
        if (allowOthers) {
            options.setEditable(true);
            options.getEditor().getEditorComponent().addFocusListener(this);
        }

        JLabel msg  = new JLabel(jEdit.getProperty("p4plugin.action.changelists.msg"));
        other       = null;


        CLDialog dlg = new CLDialog(
            (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent),
            jEdit.getProperty("p4plugin.action.changelists.title"),
            options);
        dlg.setVisible(true);
        if (dlg.isApproved()) {
            Object item = options.getSelectedItem();
            String cl;
            if (item instanceof ChangeList) {
                cl = ((ChangeList)item).number;
            } else if (showDefault && options.getSelectedIndex() == 0) {
                cl = null;
            } else {
                cl = (String) item;
                if (cl == null || cl.length() == 0)
                    throw new IllegalArgumentException("operation cancelled");
            }
            return cl;
        }
        this.cancelled = true;
        throw new IllegalArgumentException("operation cancelled");
    }

    public void focusGained(FocusEvent e) {
        if (options != null && allowOthers)
            options.getEditor().selectAll();
    }

    public void focusLost(FocusEvent e) {
        // ignore.
    }

    public String getChange() {
        return change;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void run() {
        if (!cancelled) {
            try {
                if (view == null) {
                    view = jEdit.getActiveView();
                }
                this.change = getChangeList(view.getContentPane());
            } catch (IllegalArgumentException iae) {
                this.change = null;
            }
        }
    }

    private class ChangeList {

        public String number;
        public String description;

        public ChangeList(String number, String description) {
            this.number         = number;
            this.description    = description;
        }

        public String toString() {
            return number + " (" + description + ")";
        }

    }

    private class CLDialog extends EnhancedDialog {

        private boolean approved;

        public CLDialog(Frame parent, String title, final Component options) {
            super(parent, title, true);
            this.approved = false;

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(BorderLayout.NORTH, options);
            getContentPane().add(BorderLayout.SOUTH, new OkCancelButtons(this));

            pack();
            GUIUtilities.loadGeometry(this, "p4plugin.changelist_dialog");

            int x = (parent.getWidth() / 2 - getWidth() / 2);
            int y = (parent.getHeight() / 2 - getHeight() / 2);
            setLocation(x,y);

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    options.requestFocus();
                }
            });
        }

        public void ok() {
            approved = true;
            dispose();
        }

        public void cancel() {
            approved = false;
            dispose();
        }

        public boolean isApproved() {
            return approved;
        }

        public void dispose() {
            GUIUtilities.saveGeometry(this, "p4plugin.changelist_dialog");
            super.dispose();
        }

    }

}

