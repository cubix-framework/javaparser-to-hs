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

package gruntspud.standalone;

import gruntspud.CVSFileNode;
import gruntspud.GruntspudContext;
import gruntspud.editor.MiniTextEditor;
import gruntspud.ui.view.AbstractCVSView;
import gruntspud.ui.view.ViewEvent;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class DockedEditorView
    extends AbstractCVSView {
    private JPanel pane;
    private GruntspudContext context;
    private MiniTextEditor editor;
    private File file;
    private Icon icon;

    /**
     * Creates a new DockedEditorView object.
     *
     * @param context DOCUMENT ME!
     * @param icon DOCUMENT ME!
     * @param conflicts DOCUMENT ME!
     */
    public DockedEditorView(GruntspudContext context, Icon icon,
                            boolean conflicts) {
        super("<New Document>", null, "<New Document>");

        //
        this.context = context;
        this.file = file;
        this.icon = icon;

        //
        editor = new MiniTextEditor(context, true, true, false,
                                    JDK13GruntspudHost.PROP_EDITOR_WORD_WRAP, false,
                                    conflicts ?
                                    MiniTextEditor.CONFLICT_RESOLVER
                                    : MiniTextEditor.LINE_NUMBERED_EDITOR);

        //
        pane = new JPanel(new BorderLayout());
        pane.add(editor, BorderLayout.CENTER);
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public void updateNode(CVSFileNode node) {
    }


    /**
     * DOCUMENT ME!
     *
     * @param file DOCUMENT ME!
     */
    public void openFile(File file, String encoding) {
        editor.openFile(file, encoding);
        setViewName(editor.getFile().getName());
        setViewToolTipText(editor.getFile().getAbsolutePath());
        editor.grabFocus();
        fireViewEvent(new ViewEvent(this, ViewEvent.VIEW_CHANGED, true));
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public File getFile() {
        return editor.getFile();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean preserveViewOnInit() {
        return true;
    }

    /**
     * DOCUMENT ME!
     */
    public void stop() {
        editor.cleanUp();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Icon getViewIcon() {
        return icon;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean closing() {
        if (editor.isChanged()) {
            int opt = JOptionPane.showConfirmDialog(pane,
                "You have unsaved changed. Save now?", "Unsaved changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (opt != JOptionPane.CANCEL_OPTION) {
                if (opt == JOptionPane.YES_OPTION) {
                    if (editor.getFile() == null) {
                        editor.saveAs();
                    }
                    else {
                        editor.save(editor.getFile(), false);
                    }
                }
            }
            else {

                return false;
            }
        }

        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean canClose() {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSFileNode getCWDNode() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param node DOCUMENT ME!
     */
    public void reload(CVSFileNode node) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public JComponent getViewComponent() {
        return pane;
    }

    /**
     * DOCUMENT ME!
     *
     * @param rootNode DOCUMENT ME!
     */
    public void setRootNode(CVSFileNode rootNode) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param cwdNode DOCUMENT ME!
     */
    public void setCWDNode(CVSFileNode cwdNode) {
    }

    /**
     * DOCUMENT ME!
     *
     * @param sel DOCUMENT ME!
     */
    public void setSelectedNodes(CVSFileNode[] sel) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public CVSFileNode[] getSelectedNodes() {
        return null;
    }

	/**
	 * DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 */
	public void refilterAndResort() {

	}
}
