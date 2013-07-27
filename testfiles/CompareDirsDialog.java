/*
 * CompareDirsDialog.java
 * Copyright (c) 2000 Andre Kaplan
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


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import java.io.IOException;
import java.io.File;

import javax.swing.*;

import cz.autel.dmi.HIGConstraints;
import cz.autel.dmi.HIGLayout;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.util.Log;


public class CompareDirsDialog
    extends JDialog 
{
    public CompareDirsDialog(jdiff.gui.JDiffPanel destPanel) {
        super(getFrame((Component)destPanel), "Compare Files or Directories");

        Container contentPane = this.getContentPane();

        contentPane.setLayout(new BorderLayout());

        contentPane.add(BorderLayout.NORTH, new CompareDirsPanel(
            destPanel,
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    CompareDirsDialog.this.dispose();
                }
            }
        ));

        this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                CompareDirsDialog.this.dispose();
            }
        });

        this.pack();
        GUIUtilities.loadGeometry(this, "jdiff-compare-dirs-window");
        this.show();
    }

    public void dispose()
    {
        GUIUtilities.saveGeometry(this, "jdiff-compare-dirs-window");
        super.dispose();
    }

    private static Frame getFrame(Component comp) {
        while (comp != null) {
            Log.log(Log.DEBUG, CompareDirsDialog.class, comp.getClass());
            if (comp instanceof Frame) {
                return (Frame)comp;
            } else if (comp instanceof JPopupMenu) {
                comp = ((JPopupMenu)comp).getInvoker();
            } else {
                comp = comp.getParent();
            }
        }
        return null;
    }
}

class CompareDirsPanel
    extends JPanel
{
    public CompareDirsPanel(jdiff.gui.JDiffPanel destPanel, ActionListener cancelAction)
    {
        this.destPanel = destPanel;

        JPanel top = new JPanel();
        {
            int w[] = {12, 200, 8, 0, 12};
            int h[] = {4, 0, 6, 0, 4};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();

            l.setColumnWeight(2,1);

            top.setLayout(l);

            this.dir1    = new HistoryTextField("jdiff.compare-dirs.history.1", true);
            this.choose1 = new JButton("Choose...");
            this.choose1.addActionListener(new ChooseListener());
            c.setVCorrection(0, -4);
            top.add(this.dir1, c.rc(2, 2));
            top.add(this.choose1, c.rc(2, 4));

            this.dir2    = new HistoryTextField("jdiff.compare-dirs.history.2", true);
            this.choose2 = new JButton("Choose...");
            this.choose2.addActionListener(new ChooseListener());
            c.setVCorrection(0, -4);
            top.add(this.dir2, c.rc(4, 2));
            top.add(this.choose2, c.rc(4, 4));

            try {
                this.dir1.setText(this.dir1.getModel().getItem(0));
            } catch (Exception e) {}

            try {
                this.dir2.setText(this.dir2.getModel().getItem(0));
            } catch (Exception e) {}
        }

        JPanel middle = new JPanel();
        {
            int w[] = {12, 48, 4, 48, 8, 0, 12};
            int h[] = {4, 0, 4};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();

            l.setColumnWeight(4, 1);

            middle.setLayout(l);
            middle.add(new JLabel("Filter:"), c.rcwh(2, 2, 1, 1, "rtb"));
            this.glob = new HistoryTextField("jdiff.filter.history", true);
            this.subDirs = new JCheckBox(
                jEdit.getProperty("jdiff.include-sub-dirs.label")
            );

            c.setVCorrection(0, -4);
            middle.add(this.glob, c.rc(2, 4));

            c.setVCorrection(-1, -7);
            middle.add(this.subDirs, c.rc(2, 6));

            try {
                this.glob.setText(this.glob.getModel().getItem(0));
            } catch (Exception e) {
                this.glob.setText("*");
            }

            this.subDirs.setSelected(jEdit.getBooleanProperty("jdiff.include-sub-dirs", false));
        }

        JPanel bottom = new JPanel();
        {
            int w[] = {12, -4, 8, -2, 12};
            int h[] = {4, 0, 6};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();

            bottom.setLayout(l);

            this.okBtn = new JButton("OK");
            this.okBtn.addActionListener(new SubmitListener());
            this.okBtn.addActionListener(cancelAction);
            c.setVCorrection(0, -4);
            bottom.add(this.okBtn, c.rc(2, 2));

            this.cancelBtn = new JButton("Cancel");
            this.cancelBtn.addActionListener(cancelAction);
            c.setVCorrection(0, -4);
            bottom.add(this.cancelBtn, c.rc(2, 4));
        }

        {
            int w[] = {0};
            int h[] = {0, 4, 0, 4, 0};
            HIGLayout l = new HIGLayout(w, h);
            HIGConstraints c = new HIGConstraints();

            this.setLayout(l);

            this.add(top,    c.rcwh(1, 1, 1, 1, "r"));
            this.add(middle, c.rcwh(3, 1, 1, 1, "r"));
            this.add(bottom, c.rcwh(5, 1, 1, 1, "r"));
        }
    }

    private jdiff.gui.JDiffPanel destPanel;

    private HistoryTextField dir1;
    private JButton choose1;
    private HistoryTextField dir2;
    private JButton choose2;
    private HistoryTextField glob;
    private JCheckBox subDirs;
    private JButton okBtn;
    private JButton cancelBtn;

    private class ChooseListener
        implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            if (evt.getSource() == CompareDirsPanel.this.choose1) {
                chooser.setCurrentDirectory(
                    new File(CompareDirsPanel.this.dir1.getText())
                );
            } else if (evt.getSource() == CompareDirsPanel.this.choose2) {
                chooser.setCurrentDirectory(
                    new File(CompareDirsPanel.this.dir2.getText())
                );
            }
            int retVal = chooser.showDialog(CompareDirsPanel.this, "Choose...");
            if(retVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    try {
                        String fileName = file.getCanonicalPath();
                        if (evt.getSource() == CompareDirsPanel.this.choose1) {
                            CompareDirsPanel.this.dir1.setText(fileName);
                        } else if (evt.getSource() == CompareDirsPanel.this.choose2) {
                            CompareDirsPanel.this.dir2.setText(fileName);
                        }
                    } 
                    catch(IOException e) {
                        // shouldn't happen
                    }
                }
            }
        }
    }

    private class SubmitListener
        implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            if (CompareDirsPanel.this.dir1.getText().equals("") || 
                CompareDirsPanel.this.dir2.getText().equals("")
            ) {
                return;
            }

            CompareDirsPanel.this.dir1.addCurrentToHistory();
            CompareDirsPanel.this.dir2.addCurrentToHistory();

            String filter = "*";
            if (!CompareDirsPanel.this.glob.getText().equals("")) {
                filter = CompareDirsPanel.this.glob.getText();
                CompareDirsPanel.this.glob.addCurrentToHistory();
            }

            jEdit.setBooleanProperty("jdiff.include-sub-dirs",
                CompareDirsPanel.this.subDirs.isSelected()
            );

            jdiff.text.DiffDocument doc = DiffUtilities.diffFilesOrDirs(
                CompareDirsPanel.this.dir1.getText(),
                CompareDirsPanel.this.dir2.getText(),
                filter, 
                CompareDirsPanel.this.subDirs.isSelected()
            );

            if (doc != null) {
                CompareDirsPanel.this.destPanel
                    .addDiffPane(new jdiff.gui.DualScrollPane(doc));
            }
        }
    }

}

