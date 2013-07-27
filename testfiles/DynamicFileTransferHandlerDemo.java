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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

import org.frapuccino.DemoComponent;
import org.frapuccino.swing.DynamicFileFactory;
import org.frapuccino.swing.DynamicFileTransferHandler;


/**
 * @author redsolo
 */
public final class DynamicFileTransferHandlerDemo extends JPanel implements DemoComponent, DynamicFileFactory {

    private JTextArea textArea;

    private JRadioButton textFileButton;
    private JRadioButton imageFileButton;

    private ButtonGroup radioButtonGroup;

    /**
     * Creates the demo for the dynamic file transfer handler.
     */
    public DynamicFileTransferHandlerDemo() {
        textArea = new JTextArea("A sample text, that can be dragged from this component"
                + " onto the native file storage. The file that is created on the native storage"
                + " can either be a text file or an image file. DnD me!");
        textArea.setDragEnabled(true);
        textArea.setTransferHandler(new DemoTransferHandler(this));
        textArea.setLineWrap(true);

        radioButtonGroup = new ButtonGroup();
        textFileButton = new JRadioButton("Create text file");
        imageFileButton = new JRadioButton("Create image file");
        radioButtonGroup.add(textFileButton);
        radioButtonGroup.add(imageFileButton);
        radioButtonGroup.setSelected(textFileButton.getModel(), true);

        JPanel radioPanel = new JPanel();
        radioPanel.add(textFileButton);
        radioPanel.add(imageFileButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);
        add(radioPanel, BorderLayout.SOUTH);

        // Preload the ImageIO, a little cheating to speed up the file creation.
        ImageIO.scanForPlugins();
    }

    /** {@inheritDoc} */
    public JComponent getComponent() {
        return this;
    }

    /** {@inheritDoc} */
    public String getDescription() {
        return ("Demonstrates the dynamic file transfer handler object. This handler generates files"
                + " dynamically when there is a DnD action. This demo can generate two different"
                + " files from the text area. Drag the text to the desktop and the file will"
                + " be generated dynamically. Change the radio buttons to switch between a normal"
                + " text file (.txt) and an image file (.jpg).");
    }

    /** {@inheritDoc} */
    public String getDemoName() {
        return "Dynamic file DnD";
    }

    /**
     * Transfer handler that enables dragging out of the text area.
     * @author redsolo
     */
    private class DemoTransferHandler extends DynamicFileTransferHandler {

        /**
         * @param factory the factory to create the files.
         */
        public DemoTransferHandler(DynamicFileFactory factory) {
            super(factory, DynamicFileTransferHandler.LATE_GENERATION);
        }

        /** {@inheritDoc} */
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return false;
        }

        /** {@inheritDoc} */
        public int getSourceActions(JComponent c) {
            return TransferHandler.COPY;
        }
    }

    /** {@inheritDoc} */
    public File[] createFiles(JComponent component) throws IOException {
        File[] files = new File[1];

        if (textFileButton.getModel() == radioButtonGroup.getSelection()) {
            files[0] = new File("demo-file.txt");

            FileWriter writer = new FileWriter(files[0]);
            writer.write(textArea.getSelectedText());
            writer.close();

        } else {
            files[0] = new File("demo-file.jpg");
            BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setFont(new Font("Arial", Font.PLAIN, 15));
            String text = textArea.getSelectedText();
            int textWidth = graphics.getFontMetrics().stringWidth(text);
            int imageWidth = image.getWidth();
            int lines = (textWidth / imageWidth) + 1;
            int charsInLine = text.length() / lines;

            for (int i = 0; i <= lines; i++) {
                int end;
                if (charsInLine * i + charsInLine > text.length()) {
                    end = text.length();
                } else {
                    end = charsInLine * i + charsInLine;
                }
                //int end = (charsInLine * i + charsInLine > text.length() ? text.length() : charsInLine * i + charsInLine);
                graphics.drawString(text.substring(charsInLine * i, end), 0, graphics.getFontMetrics().getHeight() * i + 20);
            }
            graphics.dispose();

            ImageIO.write(image, "jpeg", files[0]);
        }
        files[0].deleteOnExit();

        return files;
    }
}
