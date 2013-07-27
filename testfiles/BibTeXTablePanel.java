/*:folding=indent:
* BibTeXTablePanel.java - BibTeX Dialog
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

import tableutils.TableSorter;
import uk.co.antroy.latextools.macros.ProjectMacros;
import uk.co.antroy.latextools.parsers.BibEntry;
import uk.co.antroy.latextools.parsers.BibTeXParser;
import uk.co.antroy.latextools.parsers.BibTeXTableModel;
import uk.co.antroy.latextools.parsers.IRowTableModel;

/**
 * BibTeX Navigator's dockable panel 
 * with a sortable table of 
 * defined BibTeX references.
 *
 * @see uk.co.antroy.latextools.parsers.IRowTableModel
 * 		Content of the table of references
 * @see tableutils.TableSorter
 * 		Wraps and sorts the BibTeXTableModel
 * @see uk.co.antroy.latextools.parsers.BibTeXParser
 * 		Parses the document to extract BibTeX entries
 */
public class BibTeXTablePanel
    extends AbstractToolPanel {

    //~ Instance/static variables .............................................

    private JTable table;
    private IRowTableModel<BibEntry> model;
    //private ActionListener insert;
    private boolean enableInsert = true;

    //~ Constructors ..........................................................

    /**
     * Creates a new BibTeXTablePanel object.
     * 
     * @param view the current view
     * @param buff the active buffer
     */
    public BibTeXTablePanel(View view, Buffer buff) {
        super(view, buff, "Bib");
        buildPanel();
    }

    //~ Methods ...............................................................

    public void refresh() {
        removeAll();

        if (!ProjectMacros.isTeXFile(buffer) && 
            !ProjectMacros.isBibFile(buffer)) {
            displayNotTeX(BorderLayout.CENTER);
        } else {
            enableInsert = !ProjectMacros.isBibFile(buffer);
            buildPanel();
        }

        super.refresh();
    }

    public void reload() {
    }

    private void _buildPanel() {

        JLabel parsingLabel = new JLabel("<html><font color='#dd0000'>Parsing...");
        add(parsingLabel);

        BibTeXParser parser = new BibTeXParser(view, buffer);
        BibTeXTableModel bibTeXodel = new BibTeXTableModel(parser.getBibEntries());
        TableSorter<BibEntry> sortableModel = new TableSorter<BibEntry>( bibTeXodel );
        this.model = sortableModel;
        
        table = new JTable(sortableModel);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                if (e.getClickCount() == 2) {
                    insert();
                }
            }
        });

        String key = "Enter";
        table.getActionMap().put(key, 
                                 new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                insert();
            }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), 
                                key);
        sortableModel.addMouseListenerToHeaderInTable(table);

        JScrollPane scp = new JScrollPane(table, 
                                          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                          JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        remove(parsingLabel);
        add(scp, BorderLayout.CENTER);
        sendUpdateEvent("latextools-bibliography-table-dock");
    }

    private void buildPanel() {

        Thread parseThread = new Thread(new Runnable() {
            public void run() {
                _buildPanel();
            }
        });
        parseThread.start();
    }

    private void insert() {

        if (!enableInsert) {

            return;
        }

        int[] sels = table.getSelectedRows();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sels.length; i++) {

            BibEntry bi = model.getRowEntry( sels[i] );
            sb.append(bi.getRef());
            sb.append((i < sels.length - 1) ? "," : "");
        }

        if (jEdit.getBooleanProperty("bibtex.inserttags")) {
            sb.insert(0, "\\cite{");
            sb.append("}");
        }

        int posn = view.getTextArea().getCaretPosition();
        buffer.insert(posn, sb.toString());
    }
}
