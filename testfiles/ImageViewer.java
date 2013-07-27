/*:folding=indent:
* ImageViewer.java - Display Image
* Copyright (C) 2003 Anthony Roy
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

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.awt.BorderLayout;

import java.io.File;

import java.net.MalformedURLException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import uk.co.antroy.latextools.macros.ProjectMacros;


public class ImageViewer {

    //~ Instance/static variables .............................................

    private JComponent imagePanel;
    private static final ImageViewer imageViewer = new ImageViewer();

    //~ Constructors ..........................................................

    private ImageViewer() {
    }

    //~ Methods ...............................................................

    public static ImageViewer getInstance() {

        return imageViewer;
    }

    public JComponent showImage() {

        return imagePanel;
    }

    public static void showInInfoPane(View view, Buffer buffer) {

        ImageViewer viewer = ImageViewer.getInstance();
        viewer.setImageFromBuffer(view, buffer);

        JComponent p = viewer.showImage();
        LaTeXDockable.getInstance().setInfoPanel(p, "Image Under Caret:");
    }

    public void setImageFromBuffer(View view, Buffer buffer) {

        File file = findFileName(view, buffer);

        if (file == null) {
            imagePanel = new JLabel("<html><font color='#dd0000'><b>There are no images under the caret!");

            return;
        }

        //JLabel imageLabel = new JLabel(file.getPath());
        ImageIcon image = null;

        if (file.exists()) {

            try {
                image = new ImageIcon(file.toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            image = new ImageIcon();
        }

        JLabel label = new JLabel(image);
        JPanel inner = new JPanel();
        inner.add(label, BorderLayout.CENTER);
        imagePanel = new JScrollPane(inner, 
                                     JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                     JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    private File findFileName(View view, Buffer buffer) {

        JEditTextArea textArea = view.getTextArea();
        int line = textArea.getCaretLine();
        String currentLine = buffer.getLineText(line);
        RE pattern = null;

        try {
            pattern = new RE("\\\\includegraphic(?:x|s).*?\\{(.+?)\\}");
        } catch (REException e) {
            e.printStackTrace();
        }

        REMatch match = pattern.getMatch(currentLine);
        String filename;

        if (match != null) {
            filename = match.toString(1);
        } else {

            return null;
        }

        File imageFile = new File(filename);

        if (!imageFile.exists()) {

            String tex = ProjectMacros.getMainTeXPath(buffer);
            File texFile = new File(tex);
            imageFile = new File(texFile.getParent(), filename);
        }

        if (!imageFile.exists()) {

            return null;
        }

        return imageFile;
    }
}
