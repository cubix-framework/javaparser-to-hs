/*
 * FoldWindow.java 
 * Copyright (c) Sun Aug 27 MSD 2006 Denis Koryavov
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


package foldviewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.SyntaxUtilities;
import org.gjt.sp.util.Log;


public class FoldWindow extends JWindow {
        private TextArea textArea;

        //{{{ constructor.
        public FoldWindow(JEditTextArea jEditTextArea) {
                super(jEditTextArea.getView());
                try {
                        Map props = new HashMap();
                        props.put("wrap", "none");
                        props.put(Buffer.CARET, 0);
                        props.put(Buffer.ENCODING_AUTODETECT, true);


                        // {{{ setContentPane
                        setContentPane(new JPanel(new BorderLayout()) {
                                        public boolean getFocusTraversalKeysEnabled() {
                                                return false;
                                        }
                        }); 
                        //}}}
                        
                        getRootPane().setBorder(new LineBorder(Color.BLACK));
                        
                        textArea = new JEditEmbeddedTextArea();
	                textArea.getBuffer().setMode(jEditTextArea.getBuffer().getMode());
                        updatePainter();
                        //{{{ listeners.
                        addWindowFocusListener(new WindowAdapter() {
                                        public void windowLostFocus(WindowEvent e) {
                                                dispose();
                                        }
                        }); 
                        
                        jEditTextArea.getPainter()
                        .addMouseMotionListener(new MouseMotionAdapter() {
                                        public void mouseMoved(MouseEvent e) {
                                                dispose();
                                        }
                        });
                        //}}}
                        
                        textArea.add(ScrollLayout.BOTTOM, new JLabel());
                        textArea.add(ScrollLayout.RIGHT, new JLabel());
                        textArea.setBorder(null);
                        add(textArea);
                } catch (Exception e) {
	                Log.log(Log.ERROR, this, e, e);
                }
        } 
        //}}}
        
        //{{{ showText method.
        public void showText(int x, int y, String text, int width, int height) {
	        JEditBuffer buffer = textArea.getBuffer();
	        buffer.beginCompoundEdit();
                textArea.setText(text);
		textArea.setCaretPosition(0);
                buffer.endCompoundEdit();
                
                setSize(width, height);
                setLocation(x, y);
                enshureSize(x, y);
                setVisible(true);
        } 
        //}}}
        
        //{{{ updatePainter method.
        private void updatePainter() {
                TextAreaPainter painter = textArea.getPainter();
                painter.setFont(jEdit.getFontProperty("view.font"));
		painter.setStructureHighlightEnabled(jEdit.getBooleanProperty("view.structureHighlight"));
		painter.setStructureHighlightColor(jEdit.getColorProperty("view.structureHighlightColor"));
		painter.setBackground(jEdit.getColorProperty("options.foldviewer.bgcolor"));
		painter.setForeground(jEdit.getColorProperty("view.fgColor"));
		painter.setBlockCaretEnabled(jEdit.getBooleanProperty("view.blockCaret"));
		painter.setLineHighlightEnabled(jEdit.getBooleanProperty("view.lineHighlight"));
		painter.setLineHighlightColor(jEdit.getColorProperty("view.lineHighlightColor"));
		painter.setAntiAlias(new AntiAlias(jEdit.getProperty("view.antiAlias")));
		painter.setFractionalFontMetricsEnabled(jEdit.getBooleanProperty("view.fracFontMetrics"));
                
		String defaultFont = jEdit.getProperty("view.font");
		int defaultFontSize = jEdit.getIntegerProperty("view.fontsize",12);
		painter.setStyles(SyntaxUtilities.loadStyles(defaultFont,defaultFontSize));
                painter.setLineHighlightEnabled(false);
        } 
        //}}}
        
        //{{{ enshureSize method.
        private void enshureSize(int x, int y) {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                
                int width = (getWidth() + x) < screenSize.width 
                ? getWidth() : screenSize.width -(x + 2);
                
                int height = (getHeight() + y) < screenSize.height 
                ? getHeight() : screenSize.height - (y + 2);
                
                setSize(width, height);
        } 
        //}}}
        
        // :collapseFolds=1:tabSize=8:indentSize=8:folding=explicit:
}
