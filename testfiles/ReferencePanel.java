/*:folding=indent:
 * ReferencePanel.java - Reference Panel
 * Copyright (C) 2002 Anthony Roy
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
package uk.co.antroy.latextools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.jEdit;

import uk.co.antroy.latextools.macros.ProjectMacros;
import uk.co.antroy.latextools.macros.TextMacros;
import uk.co.antroy.latextools.parsers.LabelParser;
import uk.co.antroy.latextools.parsers.LaTeXAsset;


public class ReferencePanel
    extends AbstractToolPanel {

    //~ Instance/static variables .............................................

    public static final String REFERENCE_EXP = "\\\\label\\{(.*?)\\}";
    private ActionListener insert;
    private ArrayList refEntries = new ArrayList();
    private JList refList;
    private boolean suppress = false;

    //~ Constructors ..........................................................

    /**
     * Creates a new ReferencePanel object.
     * 
     * @param view the current view
     * @param buff the active buffer
     */
    public ReferencePanel(final View view, Buffer buff) {
        super(view, buff, "Ref");
        refList = new JList();
        refList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    insert();
                } else if (e.getClickCount() == 1) {

                    if (!suppress) {
                        refreshCurrentCursorPosn();
                    }

                    if ((e.getModifiers() & e.ALT_MASK) == e.ALT_MASK) {
                        suppress = true;
                    } else {
                        suppress = false;
                    }

                    LaTeXAsset asset = (LaTeXAsset)refList.getSelectedValue();
                    TextMacros.visitAsset(view, asset);
                }
            }

            public void mouseExited(MouseEvent e) {
                suppress = false;
            }
        });
        refresh();
    }

    //~ Methods ...............................................................

    /**
     * �
     * 
     * @param view �
     * @param buff �
     */
    public static void createReferenceDialog(View view, Buffer buff) {

        final ReferencePanel n = new ReferencePanel(view, buff);
        EnhancedDialog ed = new EnhancedDialog(view, "Insert Cross Reference", 
                                               false) {
            public void cancel() {
                this.hide();
            }

            public void ok() {
                n.insert();
                this.hide();
            }
        };

        ed.setContentPane(n);
        ed.pack();
        ed.show();
    }

    public void refresh() {

        if (suppress) {

            return;
        }

        if (bufferChanged) {
            removeAll();
            bufferChanged = false;
        }

        if (!ProjectMacros.isTeXFile(buffer)) {
            displayNotTeX(BorderLayout.CENTER);
        } else {

            LabelParser parser = new LabelParser(view, buffer);
            Object[] be = parser.getLabelArray();
            refList.setListData(be);

            JScrollPane scp = new JScrollPane(refList, 
                                              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                              JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(400, 100));
            add(scp, BorderLayout.CENTER);
        }

        repaint();
    }

    public void reload() {
    }

    private void insert() {

        LaTeXAsset refTagPair = (LaTeXAsset)refList.getSelectedValue();
        String ref = refTagPair.getName();

        if (ref != null) {

            if (jEdit.getBooleanProperty("reference.inserttags")) {
                ref = "\\ref{" + ref + "}";
            }

            view.setBuffer(currentBuffer);
            currentBuffer.insert(currentCursorPosn, ref);
            view.getTextArea().setCaretPosition(
                        currentCursorPosn + ref.length());
        }
    }
}
