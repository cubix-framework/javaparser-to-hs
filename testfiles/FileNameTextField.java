/*
 * Gruntspud Copyright (C) 2002 Brett Smith. Written by: Brett Smith <t_magicthize@users.sourceforge.net> This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU Library General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your option) any later version. This program is distributed in
 * the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Library General Public License for more details. You should have received a copy of the GNU
 * Library General Public License along with this program; if not, write to the Free Software Foundation, Inc., 675 Mass Ave,
 * Cambridge, MA 02139, USA.
 */

package gruntspud.ui;

import gruntspud.Constants;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * An extension of <code>XTextField</code> that provides filename completion
 * 
 * @author Brett Smith
 */
public class FileNameTextField extends XTextField {

    private JWindow fileWindow;
    private JList fileList;
    private DefaultListModel fileModel;
    private boolean adjusting, focused, autoComplete, includeFiles;
    private File cwd;
    private Insets insets;
    private JScrollPane scroller;
    private JButton browse;

    /**
	 * Constructs a new FileNameTextField. A default model is created, the initial string is null, and the number of columns is set
	 * to 0.
	 */
    public FileNameTextField() {
        this(null, null, 0);
    }

    /**
	 * Constructs a new FileNameTextField initialized with the specified text. A default model is created and the number of columns
	 * is 0.
	 * 
	 * @param text the text to be displayed, or null
	 */
    public FileNameTextField(String text) {
        this(null, text, 0);
    }

    /**
	 * Constructs a new empty FileNameTextField with the specified number of columns. A default model is created and the initial
	 * string is set to null.
	 * 
	 * @param columns the number of columns to use to calculate the preferred width. If columns is set to zero, the preferred width
	 *            will be whatever naturally results from the component implementation.
	 */
    public FileNameTextField(int columns) {
        this(null, null, columns);
    }

    /**
	 * Constructs a new FileNameTextField initialized with the specified text and columns. A default model is created.
	 * 
	 * @param text the text to be displayed, or null
	 * @param columns the number of columns to use to calculate the preferred width. If columns is set to zero, the preferred width
	 *            will be whatever naturally results from the component implementation.
	 */
    public FileNameTextField(String text, int columns) {
        this(null, text, columns);
    }

    /**
	 * Constructs a new FileNameTextField that uses the given text storage model and the given number of columns. This is the constructor
	 * through which the other constructors feed. If the document is null, a default model is created.
	 * 
	 * @param doc the text storage to use. If this is null, a default will be provided by calling the createDefaultModel method.
	 * @param text the initial string to display, or null
	 * @param columns the number of columns to use to calculate the preferred width >= 0. If columns is set to zero, the preferred
	 *            width will be whatever naturally results from the component implementation.
	 * @exception IllegalArgumentException if columns < 0
	 */
    public FileNameTextField(Document doc, String text, int columns) {
        this(doc, text, columns, true);
    }

    /**
	 * Constructs a new FileNameTextField that uses the given text storage model and the given number of columns. This is the constructor
	 * through which the other constructors feed. If the document is null, a default model is created.
	 * 
	 * @param doc the text storage to use. If this is null, a default will be provided by calling the createDefaultModel method.
	 * @param text the initial string to display, or null
	 * @param columns the number of columns to use to calculate the preferred width >= 0. If columns is set to zero, the preferred
	 *            width will be whatever naturally results from the component implementation.
	 * @param autoComplete automatically complete filenames
	 * @exception IllegalArgumentException if columns < 0
	 */
    public FileNameTextField(Document doc, String text, int columns, boolean autoComplete) {
        this(doc, text, columns, autoComplete, false);
    }

    /**
	 * Constructs a new JTextField that uses the given text storage model and the given number of columns. This is the constructor
	 * through which the other constructors feed. If the document is null, a default model is created.
	 * 
	 * @param doc the text storage to use. If this is null, a default will be provided by calling the createDefaultModel method.
	 * @param text the initial string to display, or null
	 * @param columns the number of columns to use to calculate the preferred width >= 0. If columns is set to zero, the preferred
	 *            width will be whatever naturally results from the component implementation.
	 * @param autoComplete automatically complete filenames
	 * @param includeFiles include files in the completion
	 * @exception IllegalArgumentException if columns < 0
	 */
    public FileNameTextField(Document doc, String text, int columns, boolean autoComplete, boolean includeFiles) {
        this(doc, text, columns, autoComplete, includeFiles, null, false);
    }

    /**
	 * Constructs a new JTextField that uses the given text storage model and the given number of columns. This is the constructor
	 * through which the other constructors feed. If the document is null, a default model is created.
	 * 
	 * @param doc the text storage to use. If this is null, a default will be provided by calling the createDefaultModel method.
	 * @param text the initial string to display, or null
	 * @param columns the number of columns to use to calculate the preferred width >= 0. If columns is set to zero, the preferred
	 *            width will be whatever naturally results from the component implementation.
	 * @param autoComplete automatically complete filenames
	 * @param includeFiles include files in the completion and file chooser (if selected)
	 * @param chooser include a file chooser (<code>nu
     * @param useSave use save in chooser instead of open
     * @exception IllegalArgumentException if columns < 0
	 */
    public FileNameTextField(
        Document doc,
        String text,
        int columns,
        boolean autoComplete,
        boolean includeFiles,
        final JFileChooser chooser,
        final boolean useSave) {

        super(doc, text, columns);

        if (chooser != null) {
            setLayout(new BorderLayout());
            browse = new JButton("..") {
            	public Dimension getPreferredSize() {
            		return new Dimension(16, super.getPreferredSize().height);
            	}            	            	
            	public Dimension getMaximumSize() {
            		return getPreferredSize();
            	}	
            	public Dimension getMinimumSize() {
            		return getPreferredSize();
            	}
            };
            browse.setMargin(new Insets(0, 0, 0, 0));
//            browse.setBorder(BorderFactory.createRaisedBevelBorder());
            browse.setFocusPainted(false);
            setFocusable(browse, false);
            browse.setDefaultCapable(false);
            browse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    chooser.setCurrentDirectory(new File(getText()));
                    int opt = 0;
                    if (useSave)
                        opt = chooser.showSaveDialog(browse);
                    else
                        opt = chooser.showOpenDialog(browse);
                    if (opt == JFileChooser.APPROVE_OPTION) {
                        FileNameTextField.this.grabFocus();
                        setFile(chooser.getSelectedFile());
                        checkVisible();
                    }
                }
            });
            add(browse, BorderLayout.EAST);
        }

        this.autoComplete = autoComplete;
        this.includeFiles = includeFiles;

        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent evt) {
                focused = true;
                //				  checkVisible();
            }

            public void focusLost(FocusEvent evt) {
                focused = false;
                hide();
            }

        });

        //		what if the document changes? - no need to be bothered at the moment
        this.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                checkVisible();
            }

            public void changedUpdate(DocumentEvent evt) {
                update();
            }

            public void removeUpdate(DocumentEvent evt) {
                update();
            }

            public void insertUpdate(DocumentEvent evt) {
                update();
            }

        });
        ActionMap actionMap = FileNameTextField.this.getActionMap();
        actionMap.put("completionDown", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                if (fileList != null && isAutoComplete() && focused) {
                    int sel = fileList.getSelectedIndex();
                    if ((sel + 1) < fileModel.getSize()) {
                        sel++;
                        fileList.setSelectedIndex(sel);
                        fileList.ensureIndexIsVisible(sel);
                    }
                }
            }
        });
        actionMap.put("completionUp", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                if (fileList != null && isAutoComplete() && focused) {
                    int sel = fileList.getSelectedIndex();
                    if ((sel - 1) > -1) {
                        sel--;
                        fileList.setSelectedIndex(sel);
                        fileList.ensureIndexIsVisible(sel);
                    }
                }
            }
        });
        actionMap.put("completionEnter", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                if (fileList != null) {
                    int sel = fileList.getSelectedIndex();
                    if (sel > -1) {
                        setText(fileList.getSelectedValue().toString());
                    }
                }
                hide();
            }
        });
        actionMap.put("completionEscape", new AbstractAction() {
            public void actionPerformed(ActionEvent evt) {
                hide();
            }
        });

    }

    protected int getColumnWidth() {
        return super.getColumnWidth() + (browse == null ? 0 : 12);
    }
    
    public void setEnabled(boolean enabled) {
    	super.setEnabled(enabled);
    	if(browse != null) {
    		browse.setEnabled(enabled);
    	}
    }

    public void setFile(File file) {
        setCaretPosition(0);
        setText(file.getAbsolutePath());
        //		  grabFocus();
        setSelectionStart(getDocument().getLength());
        setSelectionEnd(getDocument().getLength());
        setCaretPosition(getDocument().getLength());
    }

    public void hide() {
        if (fileWindow != null && fileWindow.isVisible()) {
            fileWindow.setVisible(false);
            getInputMap(FileNameTextField.WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0));
            getInputMap(FileNameTextField.WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0));
            getInputMap(FileNameTextField.WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
            getInputMap(FileNameTextField.WHEN_FOCUSED).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));

        }
    }

    public void setAutoComplete(boolean autoComplete) {
        this.autoComplete = autoComplete;
        if (!autoComplete) {
            hide();
        }
    }

    public boolean isAutoComplete() {
        return autoComplete;
    }

    public boolean isAdjusting() {
        return adjusting;
    }

    public synchronized void setText(String text) {
        adjusting = true;
        try {
        	super.setText(text);
        }
        catch(Throwable t) {
        	Constants.UI_LOG.error("Failed to set the filename. ", t);
        }
        adjusting = false;
    }

    public void checkVisible() {
        // Dont popup is autocomplete is off, the component is not visible on
        // screen, the value is adjusting or the component is not focused
        if (!autoComplete || !isShowing() || adjusting || !focused) {
            return;
        }

        if (fileWindow == null) {
            Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
            fileWindow = new JWindow(w);
            setFocusable(fileWindow, false);
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(UIManager.getBorder("ToolTip.border"));
            insets = p.getBorder() == null ? null : p.getBorder().getBorderInsets(p);
            fileModel = new DefaultListModel();
            fileList = new JList(fileModel);
            fileList.setBackground(UIManager.getColor("ToolTip.background"));
            fileList.setForeground(UIManager.getColor("ToolTip.foreground"));
            fileList.setSelectionBackground(fileList.getForeground());
            fileList.setSelectionForeground(fileList.getBackground());
            setFocusable(fileList, false);
            fileList.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (fileList.getSelectedIndex() != -1) {
                        setFile((File) fileList.getSelectedValue());
                        hide();
                    }
                }
            });
            scroller = new JScrollPane(fileList);
            setFocusable(scroller, false);
            setFocusable(scroller.getVerticalScrollBar(), false);
            setFocusable(scroller.getHorizontalScrollBar(), false);
            p.add(scroller);
            setFocusable(p, false);
            fileWindow.getContentPane().add(p);
        }

        // Now update the file list
        String text = getText();
        String base = text;
        int idx = text.lastIndexOf(File.separator);
        if (idx != -1) {
            base = text.substring(idx + 1);
        }
        base = base.toLowerCase();
        File f = new File(text);
        if (!f.exists()) {
            f = f.getParentFile();

            //
        }
        if (f != null && f.exists() && (cwd == null || (f.isDirectory() && !cwd.equals(f)))) {
            fileModel.removeAllElements();
            java.util.List lst = new java.util.ArrayList();
            if (f.isDirectory() || includeFiles) {
                lst.add(f);
            }
            File[] l = f.listFiles();
            for (int i = 0; i < l.length; i++) {
                if (l[i].isDirectory() || includeFiles) {
                    lst.add(l[i]);
                }
            }
            Collections.sort(lst);
            for (Iterator i = lst.iterator(); i.hasNext();) {
                fileModel.addElement(i.next());
            }
            cwd = f;
        }

        // Select the file that has the closes name
        int sel = -1;
        for (int i = 0; i < fileModel.getSize() && sel == -1; i++) {
            File z = (File) fileModel.getElementAt(i);
            if (z.getName().toLowerCase().startsWith(base)) {
                sel = i;
            }
        }

        fileList.setSelectedIndex(sel);
        if (sel != -1) {
            fileList.ensureIndexIsVisible(sel);

            // Make the window visible
        }
        if (fileModel.getSize() > 0 && !fileWindow.isVisible()) {
            Rectangle virtualBounds = new Rectangle();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();
            for (int j = 0; j < gs.length; j++) {
                GraphicsDevice gd = gs[j];
                GraphicsConfiguration[] gc = gd.getConfigurations();
                for (int i = 0; i < gc.length; i++) {
                    virtualBounds = virtualBounds.union(gc[i].getBounds());
                }
            }
            FontMetrics m = fileList.getFontMetrics(fileList.getFont());
            int width = getSize().width;
            for (int i = 0; i < fileModel.size(); i++) {
                int w = m.stringWidth(fileModel.elementAt(i).toString());
                if (w > width) {
                    width = w;
                }
            }
            width += 24;
            int height = 120;
            int x = getLocationOnScreen().x;
            int y = getLocationOnScreen().y + getSize().height;
            if (virtualBounds != null && (y + fileWindow.getSize().height) >= (virtualBounds.y + virtualBounds.height - 32)) {
                y = getLocationOnScreen().y - fileWindow.getSize().height;
            }
            if (virtualBounds != null && (x + width) >= (virtualBounds.x + virtualBounds.width - 32)) {
                x = virtualBounds.x + virtualBounds.width - width;
                if (x < virtualBounds.x) {
                    x = virtualBounds.x;
                    width = virtualBounds.width;
                }
            }
            fileWindow.setSize(width, height);
            fileWindow.setLocation(x, y);

            getInputMap(FileNameTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "completionDown");
            getInputMap(FileNameTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "completionUp");
            getInputMap(FileNameTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "completionEnter");
            getInputMap(FileNameTextField.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "completionEscape");

            fileWindow.setVisible(true);
        }
    }

    public static void setFocusable(Component component, boolean focusable) {
        try {
            Method m = component.getClass().getMethod("setFocusable", new Class[] { boolean.class });
            m.invoke(component, new Object[] { new Boolean(focusable)});
        } catch (Throwable t) {
        }
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame f = new JFrame("Test filename completion");
        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir")));
        chooser.setDialogTitle("Test chooser");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        FileNameTextField t = new FileNameTextField(null, "", 20, true, false, chooser, false);
        f.getContentPane().setLayout(new FlowLayout());
        f.getContentPane().add(new JLabel("Filename: "));
        f.getContentPane().add(t);
        f.pack();
        UIUtil.positionComponent(SwingConstants.CENTER, f);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                System.exit(0);
            }
        });
        f.setVisible(true);
    }
}