//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.frapuccino.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import org.frapuccino.DemoComponent;
import org.frapuccino.swing.MultipleTransferHandler;

/**
 * @author redsolo
 */
public final class MultipleTransferHandlerDemo extends JSplitPane implements DemoComponent {

    /**
     * Utility constructor
     */
    public MultipleTransferHandlerDemo() {
        super(JSplitPane.VERTICAL_SPLIT);

        setResizeWeight(0.5);

        JTextArea textArea = new JTextArea(
                "This JTextArea supports only drag and drop of texts into it (default implementation). You cannot drag and drop files into it.");
        textArea.setDragEnabled(true);
        textArea.setLineWrap(true);
        setTopComponent(new JScrollPane(textArea));

        textArea = new JTextArea(
                "This JTextArea supports both drag and drop of texts and file lists into it.");
        MultipleTransferHandler handler = new MultipleTransferHandler();
        handler.addTransferHandler(textArea.getTransferHandler());
        handler.addTransferHandler(new DummyFileTransferHandler());
        textArea.setTransferHandler(handler);
        textArea.setDragEnabled(true);
        textArea.setLineWrap(true);
        setBottomComponent(new JScrollPane(textArea));
    }


    /** {@inheritDoc} */
    public JComponent getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public String getDescription() {
        return "The multiple transfer handler is composed of one or several TransferHandlers."
            + "When setting a TransferHandler on a component, the new transfer handler"
            + " is the only one used in a Drag and Drop action. Most of the times, this is"
            + " fine but sometimes you want to preserve the behaviour of the component's"
            + " default transfer handler. You only want to implement one specific data flavor"
            + " to the component, not redoing those feature already implemented."
            + " This class lets a component to have several transfer handlers instead of one."
            + "<p>For example, the JTextArea has a transfer handler that supports draging and dropping"
            + " strings into it by default. Say that you want to add support of DnD of a file into the"
            + " text. Instead of mimicing behaviour of DnD of strings, you will only implement a"
            + " transfer handler that supports the file list data flavor. Add these two transfer handler"
            + " to this class and set this as the component transferhandler and the problem is solved."
            + "<p>Note that this implementation will only extend a Components import ability, it will not"
            + " extend the Components export ability. One of the Transfer Handlers must be set as the"
            + " default to use when exporting data from the Component.";
    }

    /** {@inheritDoc} */
    public String getDemoName() {
        return "Multiple transfer handler";
    }

    /**
     * Creates and returns an example of this component.
     *
     * @return a Component demostrating this component.
     */
    public static JComponent createDemoComponent() {
        JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        pane.setResizeWeight(0.5);

        JTextArea textArea = new JTextArea(
                "This JTextArea supports only drag and drop of texts into it (default implementation). You cannot drag and drop files into it.");
        textArea.setDragEnabled(true);
        textArea.setLineWrap(true);
        pane.setTopComponent(new JScrollPane(textArea));

        textArea = new JTextArea(
                "This JTextArea supports both drag and drop of texts and file lists into it.");
        MultipleTransferHandler handler = new MultipleTransferHandler();
        handler.addTransferHandler(textArea.getTransferHandler());
        handler.addTransferHandler(new DummyFileTransferHandler());
        textArea.setTransferHandler(handler);
        textArea.setDragEnabled(true);
        textArea.setLineWrap(true);
        pane.setBottomComponent(new JScrollPane(textArea));

        return pane;
    }

    /**
     * Dummy transfer handler that can only import file lists.
     */
    private static class DummyFileTransferHandler extends TransferHandler {

        /** {@inheritDoc} */
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            boolean canImport = false;
            for (int i = 0; i < transferFlavors.length; i++) {
                canImport |= transferFlavors[i].equals(DataFlavor.javaFileListFlavor);
            }
            return canImport;
        }

        /** {@inheritDoc} */
        public boolean importData(JComponent comp, Transferable t) {
            boolean wasImported = false;
            if ((t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) && (comp instanceof JTextArea)) {
                JTextArea text = (JTextArea) comp;
                try {
                    List files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                    String str = "";
                    for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                        File file = (File) iterator.next();
                        str += file.getName() + ", ";
                    }
                    text.replaceSelection(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return wasImported;
        }
    }
}
