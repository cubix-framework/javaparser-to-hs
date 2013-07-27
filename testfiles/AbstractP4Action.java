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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

import common.threads.WorkRequest;
import common.threads.WorkerThreadPool;

import projectviewer.PVActions;
import projectviewer.action.Action;

import p4plugin.P4Shell;
import p4plugin.Perforce;

/**
 *  Common base class for p4 actions.
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: AbstractP4Action.java 6450 2006-04-08 23:16:54Z vanza $
 *  @since      P4P 0.1
 */
public abstract class AbstractP4Action extends Action {

    protected boolean askForChangeList;
    protected boolean showDefaultCL;

    protected static String getActionName(String cmd, boolean useCL) {
        StringBuffer sb = new StringBuffer("p4plugin_").append(cmd);
        if (useCL)
            sb.append("_cl");
        return sb.toString();
    }

    protected AbstractP4Action() {
        this(false);
    }

    /**
     *  Creates a p4 action that optionally asks for a change list
     *  to be chosen before executing the command.
     */
    protected AbstractP4Action(boolean askForChangeList) {
        this.askForChangeList   = askForChangeList;
        this.showDefaultCL      = true;
    }

    /**
     *  Creates a p4 action tied to a jEdit action.
     */
    protected AbstractP4Action(String action, boolean askForChangeList) {
        this(action, askForChangeList, true);
    }

    /**
     *  Creates a p4 action tied to a jEdit action.
     */
    protected AbstractP4Action(String action, boolean askForChangeList,
                                              boolean showDefaultCL)
    {
        super(action);
        this.askForChangeList   = askForChangeList;
        this.showDefaultCL      = showDefaultCL;
    }

    /**
     *  Should return the command that is passed to p4.
     */
	protected abstract String getCommand();

    /**
     *  Implementations should return a string array with the arguments
     *  to the p4 command according to the event (or the current state
     *  of the class).
     */
	protected abstract String[] getArgs(ActionEvent ae);

    /** Returns the menu item text. */
    public String getText() {
        if (askForChangeList) {
            return jEdit.getProperty("p4plugin.action." + getCommand() + "-clist");
        } else {
            return jEdit.getProperty("p4plugin.action." + getCommand());
        }
    }

    /** Shows the change list dialog if asked to, and invokes perforce. */
    public void actionPerformed(ActionEvent ae) {
        if (askForChangeList) {
            CListChooser chooser = new CListChooser(showDefaultCL, ae);
            WorkerThreadPool.getSharedInstance().runRequests(
                                new Runnable[] { chooser });
        } else {
            invokePerforce(null, ae);
        }
    }

    /** Calls the perforce command and does proper error reporting. */
    protected void invokePerforce(String clist, ActionEvent ae) {
        String[] args = getArgs(ae);
        if (clist != null) {
            int size = (args != null) ? args.length : 0;
            String[] tmp = new String[size+2];
            tmp[0] = "-c";
            tmp[1] = clist;
            if (args != null) {
                System.arraycopy(args, 0, tmp, 2, args.length);
            }
            args = tmp;
        }

        final Perforce p4 = new Perforce(getCommand(), args);
        View v = (viewer != null) ? viewer.getView() : jEdit.getActiveView();

        try {
            p4.exec(v).waitFor();
        } catch (Exception e) {
            SwingUtilities.invokeLater(new ErrorReporter(e));
            return;
        }

        if (!p4.isSuccess()) {
            SwingUtilities.invokeLater(new ErrorReporter(p4));
            return;
        }

        PVActions.swingInvoke(
            new Runnable() {
                public void run() {
                    postProcess(p4);
                }
            }
        );

    }

    /**
     *  Implementations that need to do post-processing of the data
     *  returned by p4 can override this method. By default this does
     *  nothing. This method is not called if the p4 call fails for
     *  any reason, so it can't be used to process errors.
     */
    protected void postProcess(Perforce p4) {

    }

    /** Shows the output in a dialog. */
    protected void showOutputDialog(Perforce p4, String title) {
        final JTextArea message = new JTextArea(24, 80);
        message.setText(p4.getOutput());
        message.setEditable(false);
        JScrollPane pane = new JScrollPane(message);

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    try {
                        Rectangle r = message.modelToView(0);
                        message.scrollRectToVisible(r);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        );
        JOptionPane.showMessageDialog(jEdit.getActiveView().getContentPane(),
            pane, title, JOptionPane.PLAIN_MESSAGE);
    }

    /** Shows the output of the process in a shell. */
    protected void showInShell(Perforce p4) {
        P4Shell.writeToShell(p4.getOutput());
    }

    /** Runnable used to choose change lists. */
    protected class CListChooser implements Runnable {

        public String   change;
        public boolean  cancelled;

        private ActionEvent ae;
        private boolean showDefault;
        private boolean autoInvoke;

        public CListChooser() {
            this(true, null);
            this.autoInvoke = false;
        }

        public CListChooser(boolean showDefault, ActionEvent ae) {
            this.showDefault = showDefault;
            this.autoInvoke = true;
            this.ae = ae;
        }

        public void setAutoInvoke(boolean flag) {
            this.autoInvoke = flag;
        }

        public void run() {
            change = null;
            View v = (viewer != null) ? viewer.getView() : jEdit.getActiveView();
            ChangeListDialog dlg = new ChangeListDialog(v, showDefault);
            PVActions.swingInvoke(dlg);
            if (autoInvoke && !dlg.isCancelled())
                invokePerforce(dlg.getChange(), ae);
            change = dlg.getChange();
            cancelled = dlg.isCancelled();
        }

    }

    /** Runnable implementation that reports p4 errors in the AWT thread. */
    protected class ErrorReporter implements Runnable {

        private Perforce p4;
        private Exception error;

        public ErrorReporter(Perforce p4) {
            this.p4 = p4;
        }

        public ErrorReporter(Exception error) {
            this.error = error;
        }

        public void run() {
            View v = (viewer != null) ? viewer.getView() : jEdit.getActiveView();
            if (p4 != null) {
                p4.showError(v);
            } else {
                Log.log(Log.ERROR, this, error);
                v.getStatus().setMessageAndClear(
                                jEdit.getProperty("p4plugin.action.error",
                                    new String[] { error.getMessage() }));
            }
        }

    }

    protected class WRWaiter implements Runnable {

        private ActionEvent ae;
        private CListChooser chooser;
        private WorkRequest req;

        public WRWaiter(WorkRequest wr, CListChooser chooser, ActionEvent ae) {
            this.ae = ae;
            this.chooser = chooser;
            this.req = wr;
        }

        public void run() {
            while (true) {
                try {
                    req.waitFor();
                    break;
                } catch (InterruptedException ie) {
                    // annoying.
                }
            }
            if (!chooser.cancelled) {
                invokePerforce(chooser.change, ae);
            }
        }

    }

}

