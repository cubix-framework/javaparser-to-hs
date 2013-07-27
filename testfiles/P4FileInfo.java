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
package p4plugin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Component;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.gjt.sp.jedit.jEdit;

import projectviewer.gui.NodePropertyProvider;
import projectviewer.vpt.VPTNode;
import projectviewer.vpt.VPTProject;

import p4plugin.action.P4FileInfoAction;
import p4plugin.config.P4Config;

/**
 *  Shows Perforce-related information about the selected file node.
 *
 *  @author     Marcelo Vanzin
 *  @version    $Id: P4FileInfo.java 17587 2010-04-04 00:08:07Z vanza $
 *  @since      P4P 0.3.0
 */
public class P4FileInfo implements NodePropertyProvider
{

	public boolean isNodeSupported(VPTNode node)
	{
	    if (node.isFile()) {
	        VPTProject p = VPTNode.findProjectFor(node);
	        P4Config cfg = P4Config.getProjectConfig(p);
	        return cfg != null;
	    }
	    return false;
	}


	public String getTitle()
	{
		return "Perforce";
	}


	public Component getComponent(VPTNode node)
	{
		return new P4FileInfoPane(node);
	}


    private static class P4FileInfoPane
        extends JPanel
        implements ActionListener, Perforce.Visitor
    {
        private final VPTNode node;
        private Map<String, String> info;

        private JButton filelog;
        private JButton diff;


        public P4FileInfoPane(VPTNode node)
        {
            this.node = node;
            this.info = new HashMap<String, String>();
            try {
                loadInfo();
            } catch (Exception e) {
                setLayout(new BorderLayout());
                add(BorderLayout.NORTH, new JLabel("Error executing p4:"));
                add(BorderLayout.CENTER, new JLabel( e.getMessage()));
            }
        }


        private void loadInfo()
            throws IOException, InterruptedException
        {
            Perforce p4 = new Perforce("fstat", node.getNodePath());
            p4.setVisitor(this);
            p4.exec(jEdit.getActiveView()).waitFor();

            if (!p4.isSuccess()) {
                throw new IOException(p4.getError());
            }

            setLayout(new BorderLayout());

            /* The top pane contains the depot file path. */
            add(BorderLayout.NORTH,
                new JLabel("Depot Path: " + info.get("depotFile")));

            JPanel data = new JPanel(new GridLayout(1, 2));
            data.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

            List<String> labels;
            List<String> values;

            /* The left pane contains depot file state information. */
            labels = new ArrayList<String>();
            labels.add("Depot Info");
            labels.add("Revision");
            labels.add("Action");
            labels.add("Change");
            labels.add("Mod time");
            labels.add("Type");

            values = new ArrayList<String>();
            values.add("");
            values.add(info.get("headRev"));
            values.add(info.get("headAction"));
            values.add(info.get("headChange"));
            values.add(info.get("headModTime"));
            values.add(info.get("headType"));

            JPanel depot = createPanel(labels, values);
            depot.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            data.add(depot);

            /*
             * The right pane contains local file state information,
             * if any.
             */
            labels = new ArrayList<String>();
            labels.add("Local Info");
            labels.add("Revision");

            values = new ArrayList<String>();
            values.add("");
            values.add(info.get("haveRev"));


            if (info.get("action") != null) {
                labels.add("Action");
                labels.add("Change");
                labels.add("Type");

                values.add(info.get("action"));
                values.add(info.get("change"));
                values.add(info.get("type"));
            }

            JPanel local = createPanel(labels, values);
            local.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            data.add(local);

            add(BorderLayout.CENTER, data);

            /* The bottom pane contains filelog and diff buttons. */
            JPanel buttons = new JPanel(new FlowLayout());

            filelog = new JButton("File Log");
            filelog.addActionListener(this);
            buttons.add(filelog);

            if (info.get("action") != null) {
                diff = new JButton("Diff");
                diff.addActionListener(this);
                buttons.add(diff);
            }

            add(BorderLayout.SOUTH, buttons);
        }


        private JPanel createPanel(List<String> labels,
                                   List<String> values)
        {
            assert labels.size() == values.size();

            int i = 0;
            JLabel jlabels[] = new JLabel[labels.size()];
            JLabel jvals[] =  new JLabel[values.size()];

            for (String s : labels) {
                jlabels[i++] = new JLabel(s);
            }

            i = 0;
            for (String s : values) {
                jvals[i++] = new JLabel(s);
            }

            JPanel pane = new JPanel();
            GroupLayout layout = new GroupLayout(pane);
            pane.setLayout(layout);

            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            /* The horizontal group. */
            GroupLayout.SequentialGroup hGroup = layout.createSequentialGroup();

            GroupLayout.ParallelGroup pGroup = layout.createParallelGroup();
            for (i = 0; i < jlabels.length; i++) {
                pGroup.addComponent(jlabels[i]);
            }
            hGroup.addGroup(pGroup);

            pGroup = layout.createParallelGroup();
            for (i = 0; i < jvals.length; i++) {
                pGroup.addComponent(jvals[i]);
            }
            hGroup.addGroup(pGroup);

            layout.setHorizontalGroup(hGroup);

            /* The vertical group. */
            GroupLayout.SequentialGroup vGroup = layout.createSequentialGroup();

            for (i = 0; i < jlabels.length; i++) {
                pGroup = layout.createParallelGroup(GroupLayout.Alignment.BASELINE);
                pGroup.addComponent(jlabels[i]);
                pGroup.addComponent(jvals[i]);
                vGroup.addGroup(pGroup);
            }

            layout.setVerticalGroup(vGroup);

            return pane;
        }


        public void actionPerformed(ActionEvent ae)
        {
            if (ae.getSource() == filelog) {
                P4FileInfoAction a = new P4FileInfoAction(node, "filelog");
                a.actionPerformed(null);
            } else if (ae.getSource() == diff) {
                P4FileInfoAction a = new P4FileInfoAction(node, "diff");
                a.actionPerformed(null);
            }
        }


        public boolean process(String line)
        {
            if (line.startsWith("... ")) {
                int idx = line.indexOf(" ", 4);
                if (idx > 4) {
                    info.put(line.substring(4, idx),
                             line.substring(idx + 1));
                }
            }
            return true;
        }

    }

}

