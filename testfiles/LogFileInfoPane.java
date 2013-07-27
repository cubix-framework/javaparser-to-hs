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

package gruntspud.ui.report;

import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.ui.UIUtil;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.command.log.LogInformation;

import allensoft.javacvs.client.ui.swing.LogDetailsGraphPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class LogFileInfoPane
    extends FileInfoPane {
    //  Private instance variables
    private JLabel file;

    //  Private instance variables
    private JLabel headRevision;

    //  Private instance variables
    private JLabel locks;

    //  Private instance variables
    private JLabel keywordSubstitution;

    //  Private instance variables
    private JLabel accessList;

    //  Private instance variables
    private JLabel totalRevisions;

    //  Private instance variables
    private JLabel selectedRevisions;

    //  Private instance variables
    private JLabel branch;

    //  Private instance variables
    private JLabel revisionDate;

    //  Private instance variables
    private JLabel revisionBranches;

    //  Private instance variables
    private JLabel revisionLines;

    //  Private instance variables
    private JLabel revisionState;

    //  Private instance variables
    private JLabel revisionAuthor;

    //  Private instance variables
    private JLabel directory;
    private JTextArea description;
    private JTextArea revisionMessage;
    private LogDetailsGraphPanel graphPanel;
    private JList allSymNames;
    private JList revision;
    private JTabbedPane tabs;
    private JPanel revisionPanel;

    /**
     * Constructor
     */
    public LogFileInfoPane(GruntspudContext context) {
        super(context);
        setLayout(new BorderLayout());

        //  Details pane
        JPanel details = new JPanel(new GridBagLayout());
        Font valFont = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.BOTH;

        UIUtil.jGridBagAdd(details, new JLabel("File: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        file = new JLabel() {
            public Dimension getPreferredSize() {
                return new Dimension(260,
                                     super.getPreferredSize().height);
            }
        };
        file.setFont(valFont);
        UIUtil.jGridBagAdd(details, file, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Directory: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        directory = new JLabel();
        directory.setFont(valFont);
        UIUtil.jGridBagAdd(details, directory, gbc,
                           GridBagConstraints.REMAINDER);
        gbc.weightx = 0.0;

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Revision: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        headRevision = new JLabel();
        headRevision.setFont(valFont);
        UIUtil.jGridBagAdd(details, headRevision, gbc,
                           GridBagConstraints.REMAINDER);
        gbc.weightx = 0.0;

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Branch: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        branch = new JLabel();
        branch.setFont(valFont);
        UIUtil.jGridBagAdd(details, branch, gbc, GridBagConstraints.REMAINDER);
        gbc.weightx = 0.0;

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Access list: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        accessList = new JLabel();
        accessList.setFont(valFont);
        UIUtil.jGridBagAdd(details, accessList, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Locks: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        locks = new JLabel();
        locks.setFont(valFont);
        UIUtil.jGridBagAdd(details, locks, gbc, GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Keyword substitution: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        keywordSubstitution = new JLabel();
        keywordSubstitution.setFont(valFont);
        UIUtil.jGridBagAdd(details, keywordSubstitution, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Total revisions: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        totalRevisions = new JLabel();
        totalRevisions.setFont(valFont);
        UIUtil.jGridBagAdd(details, totalRevisions, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Selected revisions: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        selectedRevisions = new JLabel();
        selectedRevisions.setFont(valFont);
        UIUtil.jGridBagAdd(details, selectedRevisions, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Description: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        description = new JTextArea() {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 128);
            }
        };
        description.setFont(valFont);
        description.setEditable(false);

        JScrollPane desScroller = new JScrollPane(description);
        UIUtil.jGridBagAdd(details, desScroller, gbc,
                           GridBagConstraints.REMAINDER);

        gbc.weightx = 0.0;
        UIUtil.jGridBagAdd(details, new JLabel("Symbolic names: "), gbc,
                           GridBagConstraints.RELATIVE);
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        allSymNames = new JList(new DefaultListModel());
        allSymNames.setCellRenderer(new SymNameListCellRenderer());
        allSymNames.setFont(valFont);
        allSymNames.setVisibleRowCount(5);

        JScrollPane scroller = new JScrollPane(allSymNames) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 128);
            }
        };

        UIUtil.jGridBagAdd(details, scroller, gbc, GridBagConstraints.REMAINDER);

        //  Revisions
        revisionPanel = new JPanel(new BorderLayout());

        JPanel n = new JPanel(new BorderLayout());
        n.add(new JScrollPane(revision = new JList(new DefaultListModel())), BorderLayout.CENTER);
        revision.setVisibleRowCount(4);
        revision.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        revision.setCellRenderer(new RevisionCellRenderer());
        revision.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                showSelectedRevision();
            }
        });

        JSeparator sep = new JSeparator();
        sep.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        n.add(sep, BorderLayout.SOUTH);
        revisionPanel.add(n, BorderLayout.NORTH);

        JPanel rd = new JPanel(new GridBagLayout());
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(3, 3, 3, 3);
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        gbc2.fill = GridBagConstraints.BOTH;

        //        LogInformation.Revision rev = (LogInformation.Revision)rd;
        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(rd, new JLabel("Author: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionAuthor = new JLabel();
        UIUtil.jGridBagAdd(rd, revisionAuthor, gbc2,
                           GridBagConstraints.REMAINDER);

        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(rd, new JLabel("Date: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionDate = new JLabel();
        UIUtil.jGridBagAdd(rd, revisionDate, gbc2, GridBagConstraints.REMAINDER);

        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(rd, new JLabel("Branches: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionBranches = new JLabel();
        UIUtil.jGridBagAdd(rd, revisionBranches, gbc2,
                           GridBagConstraints.REMAINDER);

        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(rd, new JLabel("Lines: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionLines = new JLabel();
        UIUtil.jGridBagAdd(rd, revisionLines, gbc2,
                           GridBagConstraints.REMAINDER);

        gbc2.weightx = 0.0;
        UIUtil.jGridBagAdd(rd, new JLabel("State: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionState = new JLabel();
        UIUtil.jGridBagAdd(rd, revisionState, gbc2,
                           GridBagConstraints.REMAINDER);

        gbc2.weightx = 0.0;
        gbc2.weighty = 1.0;
        UIUtil.jGridBagAdd(rd, new JLabel("Message: "), gbc2,
                           GridBagConstraints.RELATIVE);
        gbc2.weightx = 1.0;
        revisionMessage = new JTextArea();
        revisionMessage.setLineWrap(true);
        revisionMessage.setWrapStyleWord(true);
        revisionMessage.setEditable(false);

        JScrollPane revTextScroller = new JScrollPane(revisionMessage);
        UIUtil.jGridBagAdd(rd, revTextScroller, gbc2,
                           GridBagConstraints.REMAINDER);

        revisionPanel.add(rd, BorderLayout.CENTER);

        //
        graphPanel = new LogDetailsGraphPanel();

        //
        tabs = new JTabbedPane();
        tabs.addTab("Details", details);
        tabs.addTab("Graph", graphPanel);

        //
        add(tabs, BorderLayout.CENTER);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Object getInfoValueForInfoContainer(FileInfoContainer container) {
        LogInformation info = (LogInformation) container;

        return info.getHeadRevision() + "(of " + info.getTotalRevisions() +
            ")";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public TableCellRenderer getInfoRenderer() {
        return null;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Class getInfoClass() {
        return String.class;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_LOG);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public Icon getActionSmallIcon() {
        return UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_LOG);
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String getActionText() {
        return "Log";
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public void setFileInfo(FileInfoContainer container) {
        //  File
        //  HEAD Revision
        //  Description
        //  Keyword substitution
        //  Locks
        //  Access list
        LogInformation info = (LogInformation) container;
        file.setText(info.getFile().getName());
        file.setToolTipText("<html><b><i>Local</i></b>: " +
                            info.getFile().getAbsolutePath() +
                            "<br><b><i>Remote:</i></b> " +
                            info.getRepositoryFilename() + "</html>");
        directory.setText(info.getFile().getParentFile().getName());
        directory.setToolTipText(info.getFile().getParentFile().getAbsolutePath());
        headRevision.setText(info.getHeadRevision());
        locks.setText(info.getLocks());
        keywordSubstitution.setText(info.getKeywordSubstitution());
        accessList.setText(info.getAccessList());
        description.setText(info.getDescription());
        totalRevisions.setText(info.getTotalRevisions());
        selectedRevisions.setText(info.getSelectedRevisions());
        branch.setText(info.getBranch());

        java.util.List l = info.getAllSymbolicNames();
        ( (DefaultListModel) allSymNames.getModel()).clear();

        for (int i = 0; i < l.size(); i++) {
            ( (DefaultListModel) allSymNames.getModel()).addElement(l.get(i));
        }

        java.util.List l2 = info.getRevisionList();
        ( (DefaultListModel) revision.getModel()).removeAllElements();

        if ( (l2 != null) && (l2.size() > 0)) {
            if (tabs.getComponentCount() == 2) {
                tabs.addTab("Revisions", revisionPanel);

            }
            for (int i = 0; i < l2.size(); i++) {
                ( (DefaultListModel) revision.getModel()).addElement(l2.get(
                    i));
            }

            showSelectedRevision();
        }
        else {
            if (tabs.getComponentCount() == 3) {
                tabs.removeTabAt(2);
            }
        }
        
        int maxDots = 0;
        for(Iterator i = info.getRevisionList().iterator(); i.hasNext(); ) {
            maxDots = Math.max(maxDots, countDots(((LogInformation.Revision)i.next()).getNumber()) + 1);
        }
        List sortedTagList = new ArrayList(info.getRevisionList());  
        Collections.sort(sortedTagList, new TagComparator(maxDots));
        graphPanel.setLogInformation(info, sortedTagList);
    }
    
    int countDots(String number) {
        int dots = 0;
        for(int i = 0; i < number.length(); i++) {
            dots += number.charAt(i) == '.' ? 1 : 0;
        }
        return dots;
    }

    private void showSelectedRevision() {
        LogInformation.Revision rev = (LogInformation.Revision) revision.
            getSelectedValue();

        if (rev == null) {
            revisionBranches.setText("");
            revisionDate.setText("");
            revisionLines.setText("");
            revisionMessage.setText("");
            revisionState.setText("");
            revisionAuthor.setText("");
        }
        else {
            revisionBranches.setText(rev.getBranches());
            revisionDate.setText(rev.getDateString());
            revisionLines.setText(rev.getLines());
            revisionState.setText(rev.getState());
            revisionMessage.setText(rev.getMessage());
            revisionAuthor.setText(rev.getAuthor());
        }
    }

    //  Supporting classes
    class SymNameListCellRenderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);

            LogInformation.SymName n = (LogInformation.SymName) value;
            StringBuffer buf = new StringBuffer();
            buf.append(n.getName());
            buf.append(" [");
            buf.append(n.getRevision());
            buf.append("]");
            setText(buf.toString());

            return this;
        }
    }

    class RevisionCellRenderer
        extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                                               cellHasFocus);

            LogInformation.Revision n = (LogInformation.Revision) value;

            if (n != null) {
                StringBuffer buf = new StringBuffer();
                buf.append(n.getNumber());
                buf.append(" [");
                buf.append(n.getDateString());
                if(n.getBranches() != null) {
                    buf.append("/");
                    buf.append(n.getBranches());
                }
                buf.append("]");
                setText(buf.toString());
            }
            else {
                setText("No revision!");

            }
            return this;
        }
    }
    
    class TagComparator implements Comparator {
      
        String zeroes = "00000";
        
        int maxDots;
        
        TagComparator(int maxDots) {
            this.maxDots = maxDots;
        }
        
        String padDots(String rev) {
            StringBuffer buf = new StringBuffer();
            StringTokenizer t = new StringTokenizer(rev, ".");
            String e;
            int l = zeroes.length();
            for(int i = 0 ; i < maxDots; i++) {
              if(i > 0) {
                buf.append('.');
              }
              if(t.hasMoreTokens()) {
                e = t.nextToken();
                buf.append(zeroes.substring(0, l - e.length()));
                buf.append(e);
              }
              else {
                buf.append(zeroes);
              }
            }
            return buf.toString();
        }

        public int compare(Object o1, Object o2) {
            String rev1 = padDots(((LogInformation.Revision)o1).getNumber());
            String rev2 = padDots(((LogInformation.Revision)o2).getNumber());
            return rev1.compareTo(rev2);
        }
        
    }
}
