/*
 * Copyright (c) 2008 Eric Berry <elberry@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.townsfolkdesigns.swixml.jedit;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.StringReader;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBPlugin;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;
import org.swixml.SwingEngine;

/**
 *
 * @author eberry
 */
public class SwiXmlPlugin extends EBPlugin {
   
   public static final Dimension DEFAULT_CONTAINER_SIZE = new Dimension(640, 480);

   public SwiXmlPlugin() {
   }

   public static void renderBuffer(View view) {
      TextArea textArea = view.getTextArea();
      String bufferText = textArea.getText();
      StringReader reader = new StringReader(bufferText);
      SwingEngine swingEngine = new JEditSwingEngine();
      Container container = null;
      try {
         container = swingEngine.render(reader);
      } catch (Exception e) {
         Log.log(Log.ERROR, SwiXmlPlugin.class, "Error rendering - buffer: " + view.getBuffer().getName(), e);
      }
      showContainer(container);
   }

   public static void renderFile(View view) {
      Buffer buffer = view.getBuffer();
      VFSFileChooserDialog fileChooserDialog = new VFSFileChooserDialog(view, buffer.getDirectory(), JFileChooser.OPEN_DIALOG, false, true);
      String[] selectedFiles = fileChooserDialog.getSelectedFiles();
      if (selectedFiles != null && selectedFiles.length == 1) {
         File selectedFile = new File(selectedFiles[0]);
         SwingEngine swingEngine = new JEditSwingEngine();
         Container container = null;
         try {
            container = swingEngine.render(selectedFile);
         } catch (Exception e) {
            Log.log(Log.ERROR, SwiXmlPlugin.class, "Error rendering - file: " + selectedFile.getPath(), e);
         }
         showContainer(container);
      }
   }

   public static void showContainer(Container container) {
      // if the container isn't an instance of JFrame, put it in one.
      if ((container instanceof JFrame) == false) {
         Dimension containerSize = container.getSize();
         if(containerSize == null) {
            containerSize = DEFAULT_CONTAINER_SIZE;
         } 
         if (containerSize.width == 0) {
            containerSize.width = DEFAULT_CONTAINER_SIZE.width;
         } 
         if (containerSize.height == 0) {
            containerSize.height = DEFAULT_CONTAINER_SIZE.height;
         }
         Log.log(Log.DEBUG, SwiXmlPlugin.class, "Container Size - w: " + containerSize.width + " | h: " + containerSize.height);
         JFrame frame = new JFrame("Test Frame");
         frame.setSize(containerSize);
         frame.setContentPane(container);
         frame.pack();
         int diffW = frame.getWidth() - container.getWidth();
         int diffH = frame.getHeight() - container.getHeight();
         Log.log(Log.DEBUG, SwiXmlPlugin.class, "Window Size Differences - w: " + diffW + " | h: " + diffH);
         containerSize.width += diffW;
         containerSize.height += diffH;
         frame.setSize(containerSize);
         container = frame;
         Toolkit toolkit = Toolkit.getDefaultToolkit();
         Dimension screenSize = toolkit.getScreenSize();
         int frameX = (screenSize.width - containerSize.width) / 2;
         int frameY = (screenSize.height - containerSize.height) / 2;
         frame.setLocation(frameX, frameY);
      }
      container.setVisible(true);
   }
}
