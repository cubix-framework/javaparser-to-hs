/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package gruntspud;

import gruntspud.connection.ConnectionException;
import gruntspud.connection.ConnectionPlugin;
import gruntspud.connection.ConnectionProfile;
import gruntspud.event.GruntspudCVSListener;
import gruntspud.file.GruntspudFileHandler;
import gruntspud.file.GruntspudGZIPFileHandler;
import gruntspud.ui.ConnectionProfileChooserPane;
import gruntspud.ui.MultilineLabel;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.KeywordSubstitutionOptions;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.util.DefaultIgnoreFileFilter;
import org.netbeans.lib.cvsclient.util.IgnoreFileFilter;

/**
 * Responsible for making a connection appropriate for the CVSROOT in the current directory and running a single command for the
 * current. Also handles the messages that come from the cvs client library. A handler should be created and the <code>doCommand()</code>
 * method should be called to run the command.
 * 
 * @author magicthize @created 26 May 2002
 */
public class CVSCommandHandler {
  //  Private statics
  private static CVSCommandHandler instance;

  //  Private instance variables
  private EventListenerList listenerList;

  private CommandGroup currentCommandGroup;

  /**
   * Constructor for CVSCommandHandler object
   */
  private CVSCommandHandler() {
    listenerList = new EventListenerList();
  }

  public EventListener[] getGruntspudCVSListeners() {
    return listenerList.getListeners(GruntspudCVSListener.class);
  }

  public void addGruntspudCVSListener(GruntspudCVSListener l) {
    listenerList.add(GruntspudCVSListener.class, l);
  }

  public static CVSCommandHandler getInstance() {
    if (instance == null) {
      instance = new CVSCommandHandler();
    }
    return instance;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param parent DOCUMENT ME!
   * @param context DOCUMENT ME!
   * @param cwd DOCUMENT ME!
   * @param cmd DOCUMENT ME!
   * @param sel DOCUMENT ME!
   * @param types DOCUMENT ME!
   * @param askForConnectionProfile DOCUMENT ME!
   * @param ignoreFileFilter DOCUMENT ME!
   * @param profile DOCUMENT ME!
   * @param gruntspudActionCallBack DOCUMENT ME!
   * @param modal DOCUMENT ME!
   * @param fileToPipeTo DOCUMENT ME!
   */
  public void runCommandGroup(Component parent, GruntspudContext context,
      File cwd, Command[] cmd, CVSFileNode[] sel, CVSSubstType[] types,
      boolean askForConnectionProfile, IgnoreFileFilter ignoreFileFilter,
      ConnectionProfile profile, GruntspudCVSListener listener,
      GruntspudCVSListener[] enabledOptionalListeners) {
    Constants.CVS_LOG.debug("Supplied connection profile " + profile);
    for (int i = 0; i < (enabledOptionalListeners == null ? 0
        : enabledOptionalListeners.length); i++) {
      Constants.CVS_LOG.debug(" Enabled listeners="
          + enabledOptionalListeners[i]);
    }
    currentCommandGroup = new CommandGroup(context, parent, cwd, cmd, sel,
        types, askForConnectionProfile, ignoreFileFilter, profile, listener,
        enabledOptionalListeners);
    currentCommandGroup.start();
  }

  public void reset() {
    currentCommandGroup = null;
  }

  public boolean isCommandRunning() {
    return currentCommandGroup != null
        && currentCommandGroup.isCommandRunning();
  }

  public void stop() {
    if (currentCommandGroup != null) {
      currentCommandGroup.stopCommand();
    }
  }

  /**
   * Lookup the password for the specified CVSROOT.
   * 
   * @param root CVSRoot to find password for
   * @return The password
   * @exception IOException on error reading the password file
   */
  private static String lookupPassword(CVSRoot root) throws IOException {
    String s = root.toString();
    File file = new File(System.getProperty("cvs.passfile", System
        .getProperty("user.home")
        + "/.cvspass"));
    BufferedReader bufferedreader = null;
    String s1 = null;
    try {
      bufferedreader = new BufferedReader(new FileReader(file));
      String s2;
      while ((s2 = bufferedreader.readLine()) != null) {
        if (s2.startsWith(s)) {
          s1 = s2.substring(s.length() + 1);
          break;
        }
      }
    } finally {
      if (bufferedreader != null) {
        try {
          bufferedreader.close();
        } catch (IOException ioexception1) {
          System.err.println("Warning: could not close password file.");
        }
      }
    }
    return s1;
  }

  class CommandGroup extends Thread {
    Component parent;

    File cwd;

    Command[] cmd;

    CVSFileNode[] sel;

    CVSSubstType[] types;

    boolean askForConnectionProfile;

    IgnoreFileFilter ignoreFileFilter;

    ConnectionProfile profile;

    java.util.List defaultIgnoreList;

    Client client;

    Connection connection;

    GruntspudContext context;

    boolean commandRunning;

    boolean abortedOk;

    GruntspudCVSListener listener;

    GruntspudCVSListener[] enabledOptionalListeners;

    CommandGroup(GruntspudContext context, Component parent, File cwd,
        Command[] cmd, CVSFileNode[] sel, CVSSubstType[] types,
        boolean askForConnectionProfile, IgnoreFileFilter ignoreFileFilter,
        ConnectionProfile profile, GruntspudCVSListener listener,
        GruntspudCVSListener[] enabledOptionalListeners) {
      super("CommandGroup");
      this.enabledOptionalListeners = enabledOptionalListeners;
      this.context = context;
      this.listener = listener;
      this.parent = parent;
      this.cwd = cwd;
      this.cmd = cmd;
      this.sel = sel;
      this.types = types;
      this.askForConnectionProfile = askForConnectionProfile;
      this.ignoreFileFilter = ignoreFileFilter;
      this.profile = profile;
    }

    public boolean isCommandRunning() {
      return commandRunning;
    }

    public void stopCommand() throws IllegalStateException {
      if (client != null) {
        abortedOk = false;
        Thread t = new Thread() {
          public void run() {
            client.abort();
            abortedOk = true;
          }
        };
        t.start();
        try {
          t.join(5000);
          if (!abortedOk) {
            t.interrupt();
          }
        } catch (InterruptedException ie) {
        }
      }
      finish();
    }

    private void finish() {
      if (connection != null && connection.isOpen()) {
        try {
          connection.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
      commandRunning = false;
      if (listener != null) listener.commandGroupFinished();
      EventListener[] l = listenerList.getListeners(GruntspudCVSListener.class);
      for (int x = l.length - 1; x >= 0; x--) {
        if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
          ((GruntspudCVSListener) l[x]).commandGroupFinished();
        }
      }
      if (client != null) {
        if (listener != null) {
          client.getEventManager().removeCVSListener(listener);
          ;
        }
        for (int j = 0; j < l.length; j++) {
          if (listenerEnabled((GruntspudCVSListener) (l[j]))) {
            client.getEventManager().removeCVSListener(
                (GruntspudCVSListener) l[j]);
          }
        }
      }
      parent = null;
      cwd = null;
      cmd = null;
      sel = null;
      types = null;
      profile = null;
      defaultIgnoreList = null;
      client = null;
      connection = null;
      context = null;
      listener = null;
    }

    public void run() {
      if (isCommandRunning()) { throw new IllegalStateException(
          "A Command is currently running."); }
      //  Get th default ignore list
      File defaultIgnoreListFile = null;
      if (ignoreFileFilter == null) {
        defaultIgnoreList = CVSUtil.getDefaultIgnoreFileList(context);
      } else {
        defaultIgnoreList = null;
      }
      EventListener[] l = listenerList.getListeners(GruntspudCVSListener.class);
      commandRunning = true;
      if (listener != null) listener.initListener(parent);
      for (int x = l.length - 1; x >= 0; x--) {
        if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
          ((GruntspudCVSListener) (l[x])).initListener(parent);
        }
      }
      if (listener != null) listener.commandGroupStarted(cmd);
      for (int x = l.length - 1; x >= 0; x--) {
        if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
          ((GruntspudCVSListener) (l[x])).commandGroupStarted(cmd);
        }
      }
      try {
        for (int i = 0; i < cmd.length; i++) {
          if (listener != null) listener.commandUnitStarted(cmd[i]);
          for (int x = l.length - 1; x >= 0; x--) {
            if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
              ((GruntspudCVSListener) l[x]).commandUnitStarted(cmd[i]);
            }
          }
          doNodes(cmd[i]);
          if (listener != null) listener.commandUnitFinished();
          for (int x = l.length - 1; x >= 0; x--) {
            if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
              ((GruntspudCVSListener) l[x]).commandUnitFinished();
            }
          }
        }
      } catch (Throwable t) {
        commandRunning = false;
        t.printStackTrace();
        Constants.CVS_LOG.error(t);
        if (listener != null) {
            Constants.CVS_LOG.debug("Sending exception to main listener " + listener.getClass().getName());
            listener.commandException(t);
        }
        for (int x = l.length - 1; x >= 0; x--) {
          if (listenerEnabled((GruntspudCVSListener) (l[x]))) {
              Constants.CVS_LOG.debug("Sending exception to " + l[x].getClass().getName());
            ((GruntspudCVSListener) l[x]).commandException(t);
          }
        }
      } finally {
        finish();
      }
    }

    /**
     * Description of the Method
     * 
     * @param cwd Description of the Parameter
     * @param cmd Description of the Parameter
     * @param sel Description of the Parameter
     * @param types Description of the Parameter
     * @exception CommandException Description of the Exception
     * @exception CommandAbortedException Description of the Exception
     * @exception AuthenticationException Description of the Exception
     * @exception IOException Description of the Exception
     */
    private void doNodes(Command cmd) throws CommandException,
        CommandAbortedException, AuthenticationException, IOException,
        ConnectionException {
      GlobalOptions options = null;
      File cwd = this.cwd;
      if (sel == null) {
        ConnectionProfile p = profile;
        {
          CVSRoot r = null;
          if (cwd != null) {
            r = CVSUtil.getCVSRoot(cwd, context);
            if ((r != null) && (p == null)) {
              p = context.getConnectionProfileModel().getProfileForCVSRoot(r);
            }
          }
          if ((p == null) || askForConnectionProfile) {
            p = chooseConnectionProfile(r, p, getNeedProfileText(
                askForConnectionProfile, r));
            if (p == null) { throw new ConnectionException(
                "No connection profile selected."); }
          }
        }
        options = CVSUtil.getGlobalOptions(context, p);
        doDir(cmd, cwd, null, options, null, p);
      } else {
        Vector v = new Vector();
        CVSSubstType type = null;
        ConnectionProfile p = profile;
        for (int i = 0; i < sel.length; i++) {
          File d = null;
          File c = null;
          if (sel[i].isLeaf()) {
            d = ((CVSFileNode) sel[i].getParent()).getFile();
          } else {
            d = sel[i].getFile().getParentFile();
          }
          if ((cwd == null) || !cwd.equals(d)
              || ((types != null) && (type != types[i]))) {
            String cvsRoot = String.valueOf(sel[i].getCVSRoot());
            if (p == null) {
              p = context.getConnectionProfileModel().getProfileForCVSRoot(
                  cvsRoot);
            }
            if ((p == null) || askForConnectionProfile) {
              p = chooseConnectionProfile(sel[i].getCVSRoot(), profile,
                  getNeedProfileText(askForConnectionProfile, sel[i]
                      .getCVSRoot()));
              if (p == null) { throw new ConnectionException(
                  "No connection profile selected."); }
            }
            options = CVSUtil.getGlobalOptions(context, p);
            options.setCVSRoot(p.getCVSRoot().toString());
            if (cwd != null) {
              doDir(cmd, cwd, v, options, type, p);
              v.removeAllElements();
            }
            cwd = d;
            if (types != null) {
              type = types[i];
            }
          }
          v.addElement(sel[i]);
        }
        if (v.size() > 0) {
          doDir(cmd, cwd, v, options, type, p);
        }
      }
    }

    private void doDir(Command cmd, File cwd, Vector v,
        GlobalOptions globalOptions, CVSSubstType type,
        ConnectionProfile profile) throws IOException, AuthenticationException,
        CommandAbortedException, CommandException, ConnectionException {
      if (profile == null) { throw new IOException(
          "There is no connection profile setup for the " + "CVSROOT "
              + globalOptions.getCVSRoot() + ". You " + "should set one up now"); }
      /**
       * @todo not sure why this is necessary, if it isn't done then only the first command will get any results
       */
      globalOptions.setCVSRoot(profile.getCVSRoot().toString());
      //        if(cmd instanceof LogCommand || cmd instanceof StatusCommand)
      //        {
      cmd = (Command) cmd.clone();
      //        }
      if (ignoreFileFilter == null) {
        File f = new File(cwd, ".cvsignore");
        if (f.isFile() && f.canRead()) {
          java.util.List l = DefaultIgnoreFileFilter.parseCvsIgnoreFile(f);
          if (defaultIgnoreList == null) {
            ignoreFileFilter = new DefaultIgnoreFileFilter(l);
          } else {
            ignoreFileFilter = new DefaultIgnoreFileFilter(defaultIgnoreList);
            for (Iterator i = l.iterator(); i.hasNext();) {
              ((DefaultIgnoreFileFilter) ignoreFileFilter)
                  .addPattern((String) i.next());
            }
          }
        } else {
          if (defaultIgnoreList != null) {
            ignoreFileFilter = new DefaultIgnoreFileFilter(defaultIgnoreList);
          }
        }
      }
      //  Set the files if appropriate
      if (v != null) {
        File[] files = new File[v.size()];
        for (int i = 0; i < v.size(); i++) {
          CVSFileNode node = (CVSFileNode) v.elementAt(i);
          files[i] = node.getFile();
          if (files[i] == null) {
            files[i] = new File(((CVSFileNode) node.getParent()).getFile(),
                node.getName());
          }
        }
        try {
          if (v != null) {
            try {
              Method m = cmd.getClass().getMethod("setFiles",
                  new Class[]{File[].class});
              if (m != null) {
                m.invoke(cmd, new Object[]{files});
              }
            } catch (NoSuchMethodException msme) {
              Constants.CVS_LOG.debug("No setFiles method for command, ignoring.");
            }
          }
          if (type != null) {
            try {
              Method m = cmd.getClass().getMethod("setKeywordSubst",
                  new Class[]{KeywordSubstitutionOptions.class});
              if (m != null) {
                KeywordSubstitutionOptions opts = type
                    .getKeywordSubstitutionOptions();
                m.invoke(cmd, new Object[]{opts});
              }
            } catch (NoSuchMethodException msme) {
              Constants.CVS_LOG.debug("No setKeywordSubst method for command, ignoring.");
            }
          }
        } catch (Exception e) {
          IOException e2 = new IOException(
              "Failed to invoke method on command. " + e.getMessage());
          throw e2;
        }
      }
      doCommand(cmd, globalOptions, cwd, profile, ignoreFileFilter);
    }

    private void doCommand(Command command, GlobalOptions options, File cwd,
        ConnectionProfile profile, IgnoreFileFilter ignoreFileFilter)
        throws IOException, AuthenticationException, CommandAbortedException,
        CommandException, IllegalArgumentException, ConnectionException {
      boolean updates = false;
      EventListener[] l = listenerList.getListeners(GruntspudCVSListener.class);
      //  Connect and run the command
      client = null;
      connection = null;
      int i = -1;
      try {
        context.getHost().writeToConsole(
            context.getTextStyleModel().getStyle(
                Constants.OPTIONS_STYLE_GRUNTSPUD),
            "Command " + command.getCVSCommand() + " ["
                + (profile.getCVSRoot().toString()) + "]");
        if (command.getOptString().equals("login")) {
          if (!profile.getCVSRoot().getConnectionType().equals("pserver")) { throw new IllegalArgumentException(
              "login only applies to pserver connection"); }
        } else {
          ConnectionPlugin plugin = CVSUtil.getConnectionPluginForRoot(context,
              profile.getCVSRoot());
          if (plugin == null) { throw new ConnectionException(
              "No connection plugin that handles "
                  + profile.getCVSRoot().toString() + " could be found."); }
          connection = plugin.createConnection(context.getViewManager()
              .getProgressDialog().getMainComponent(), context, profile);
          context.getHost().writeToConsole(
              context.getTextStyleModel().getStyle(
                  Constants.OPTIONS_STYLE_GRUNTSPUD), "Opening connection");
          connection.open();
          context.getHost().writeToConsole(
              context.getTextStyleModel().getStyle(
                  Constants.OPTIONS_STYLE_GRUNTSPUD), "Connection opened");
          GruntspudWriteTextFilePreprocessor writeProcessor = new GruntspudWriteTextFilePreprocessor(
              profile, context);
          GruntspudTransmitTextFilePreprocessor transmitProcessor = new GruntspudTransmitTextFilePreprocessor(
              profile);
          GruntspudRcsDiffFilePreprocessor diffProcessor = new GruntspudRcsDiffFilePreprocessor(
              profile);
          client = new Client(connection, new StandardAdminHandler());
          client.setUncompressedFileHandler(new GruntspudFileHandler(context,
              transmitProcessor, writeProcessor, diffProcessor));
          client.setGzipFileHandler(new GruntspudGZIPFileHandler(context,
              transmitProcessor, writeProcessor, diffProcessor));
          if (cwd != null) {
            context.getHost().writeToConsole(
                context.getTextStyleModel().getStyle(
                    Constants.OPTIONS_STYLE_GRUNTSPUD),
                "Local path is  " + cwd.getAbsolutePath().toString());
            client.setLocalPath(cwd.getAbsolutePath());
          }
          for (int j = 0; j < l.length; j++) {
            if (listenerEnabled((GruntspudCVSListener) l[j])) {
              client.getEventManager().addCVSListener(
                  (GruntspudCVSListener) l[j]);
            }
          }
          if (listener != null) {
            client.getEventManager().addCVSListener(listener);
          }
          client.getEventManager().setFireEnhancedEventSet(true);
          if (ignoreFileFilter != null) {
            Constants.CVS_LOG.info("Ignore file filter set");
            client.setIgnoreFileFilter(ignoreFileFilter);
          } else {
            Constants.CVS_LOG.info("No ignore file filter set");
          }
          client.executeCommand(command, options);
        }
      } catch (IllegalArgumentException iae) {
        iae.printStackTrace();
        throw iae;
      } finally {
        if (connection != null) {
          try {
            context.getHost().writeToConsole(
                context.getTextStyleModel().getStyle(
                    Constants.OPTIONS_STYLE_GRUNTSPUD), "Closing connection");
            connection.close();
            connection = null;
          } catch (IOException ioe) {
            context.getHost().showException(ioe, "Failed to close connection");
          }
        }
        if (client != null) {
          if (listener != null) {
            client.getEventManager().removeCVSListener(listener);
            ;
          }
          for (int j = 0; j < l.length; j++) {
            if (listenerEnabled((GruntspudCVSListener) (l[j]))) {
              client.getEventManager().removeCVSListener(
                  (GruntspudCVSListener) l[j]);
            }
          }
          client = null;
        }
      }
    }

    private boolean listenerEnabled(GruntspudCVSListener l) {
      for (int i = 0; enabledOptionalListeners != null
          && i < enabledOptionalListeners.length; i++) {
        if (enabledOptionalListeners[i] == l) { return true; }
      }
      return !l.isOptionalListener()
          || (l.isOptionalListener() && l.isSelectedByDefault());
    }

    private String getNeedProfileText(boolean askForProfile, CVSRoot r) {
      if (askForProfile) {
        return "The command you are running requires that you\n"
            + "specify a connection profile to use. Please\n"
            + "choose a profile from the list below.";
      } else {
        return (r == null) ? ("A connection profile could not be automatically\n"
            + "determined as there is no CVSROOT for the current\n" + "action. Please choose a profile from the list below.\n")
            : ("A connection profile could not be found for the\n"
                + "CVSROOT of ...\n \n" + r.toString() + "\n \n" + "Please choose a profile from the list below.");
      }
    }

    private ConnectionProfile chooseConnectionProfile(CVSRoot r,
        ConnectionProfile selectedProfile, String text) {
      MultilineLabel l = new MultilineLabel(text);
      l.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 0));
      final ConnectionProfileChooserPane conx = new ConnectionProfileChooserPane(
          context);
      conx.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
      if (selectedProfile != null) {
        conx.setSelectedName(selectedProfile.getName());
      }
      JPanel n = new JPanel(new BorderLayout());
      n.add(l, BorderLayout.CENTER);
      n.add(conx, BorderLayout.SOUTH);
      JPanel p = new JPanel(new BorderLayout());
      p.add(
          new JLabel(UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_CONNECT)),
          BorderLayout.WEST);
      p.add(n, BorderLayout.CENTER);
      //  Show the dialog
      OptionDialog.Option ok = new OptionDialog.Option("Ok", "Ok", 'o');
      OptionDialog.Option cancel = new OptionDialog.Option("Cancel", "Cancel",
          'c');
      OptionDialog.Option opt = OptionDialog.showOptionDialog(
          "useConnectionProfile", context, context.getViewManager()
              .getProgressDialog().getMainComponent(),
          new OptionDialog.Option[]{ok, cancel}, p, "Connection profile", ok,
          null, true, true);
      if (opt != ok) { return null; }
      return conx.getSelectedProfile();
    }
  }
}