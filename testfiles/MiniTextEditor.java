/*
 * Gruntspud
 * 
 * Copyright (C) 2002 Brett Smith.
 * 
 * Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud.editor;

import gruntspud.ColorUtil;
import gruntspud.Constants;
import gruntspud.GruntspudContext;
import gruntspud.GruntspudUtil;
import gruntspud.ResourceUtil;
import gruntspud.StringUtil;
import gruntspud.actions.AbstractCopyAction;
import gruntspud.actions.AbstractCutAction;
import gruntspud.actions.AbstractDeleteAction;
import gruntspud.actions.AbstractGruntspudAction;
import gruntspud.actions.AbstractNextAction;
import gruntspud.actions.AbstractOpenAction;
import gruntspud.actions.AbstractPasteAction;
import gruntspud.actions.AbstractPreviousAction;
import gruntspud.actions.AbstractRedoAction;
import gruntspud.actions.AbstractSaveAction;
import gruntspud.actions.AbstractSaveAsAction;
import gruntspud.actions.AbstractSearchAction;
import gruntspud.actions.AbstractUndoAction;
import gruntspud.actions.GruntspudAction;
import gruntspud.style.TextStyle;
import gruntspud.ui.OptionDialog;
import gruntspud.ui.TellMeAgainPane;
import gruntspud.ui.ToolBarSeparator;
import gruntspud.ui.UIUtil;
import gruntspud.ui.OptionDialog.Option;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabStop;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;

/**
 * Simple text editor editor that also doubles up as a conflict resolver.
 * 
 * @author magicthize
 */
public class MiniTextEditor extends JPanel implements DocumentListener, ClipboardOwner, OptionDialog.Callback, CaretListener,
                ActionListener {

    //  Public statics
    public final static int PLAIN_EDITOR = 0;

    public final static int LINE_NUMBERED_EDITOR = 1;

    public final static int CONFLICT_RESOLVER = 2;

    //
    static ResourceBundle res = ResourceBundle.getBundle("gruntspud.editor.ResourceBundle");

    //  Private instance variables
    private EditorTextPane text;

    private File file;

    private Action openAction;

    private Action saveAction;

    private Action nextConflictAction;

    private Action previousConflictAction;

    private Action deleteAction;

    private Action cutAction;

    private Action copyAction;

    private Action pasteAction;

    private Action saveAsAction;

    private Action searchAction;

    private Action resolveAction;

    private Action undoAction;

    private Action redoAction;

    private JPopupMenu popup;

    private GruntspudContext context;

    private String applicationName;

    private JCheckBox wordWrap;

    private boolean changed;

    private SearchPane search;

    private SearchCriteria criteria;

    private OptionDialog.Option next;

    private OptionDialog.Option close;

    private OptionDialog dialog;

    private Searcher searcher;

    private String wordWrapPropertyName;

    private int type;

    private Vector conflicts;

    private int conflictIdx;

    private boolean inBlock2;

    private boolean adjusting;

    private SimpleAttributeSet block1, block2, middle;

    private Timer parseTimer;

    private ConflictNavigator conflictNavigator;

    private JScrollPane textScroller;

    private int previousConflictIdx, nextConflictIdx;

    private String currentEncoding;

    private UndoManager undoManager;

    /**
     * <p>
     * Construct a new mini text editor. The editor type may be one of
     * </p>
     * 
     * <ul>
     * <li>MiniTextEditor.PLAIN_EDITOR</li>
     * <li>MiniTextEditor.LINE_NUMBERED_EDITOR</li>
     * <li>MiniTextEditor.CONFLICT_RESOLVER</li>
     * </ul>
     * 
     * @param context
     *            context
     * @param showSaveAs
     *            show the save as icon
     * @param showOpen
     *            show the open icon
     * @param wordWrapPropertyName
     *            the property name to store word wrap property in
     * @param type
     *            editor type
     */
    public MiniTextEditor(final GruntspudContext context, boolean showSave, boolean showSaveAs, boolean showOpen,
                    String wordWrapPropertyName, boolean wordWrapDefault, int type) {
        super(new BorderLayout());
        setBorder(null);

        //  Initialise
        this.context = context;
        this.wordWrapPropertyName = wordWrapPropertyName;
        this.type = type;
        conflicts = new Vector();

        //  Create the text area
        text = new EditorTextPane();
        text.addCaretListener(this);
        undoManager = new UndoManager();
        text.getDocument().addUndoableEditListener(undoManager);

        parseTimer = new Timer(500, this);
        parseTimer.setRepeats(false);
        parseTimer.setCoalesce(false);

        // Styles

        block1 = TextStyle
                        .toSwingStyle(context, Constants.OPTIONS_STYLE_CONFLICT_LOCAL, text.getForeground(), text.getBackground());
        block2 = TextStyle.toSwingStyle(context, Constants.OPTIONS_STYLE_CONFLICT_REMOTE, text.getForeground(), text
                        .getBackground());
        middle = TextStyle.toSwingStyle(context, Constants.OPTIONS_STYLE_CONFLICT_SEPARATOR, text.getForeground(), text
                        .getBackground());

        textScroller = new JScrollPane() {

            public void processEvent(AWTEvent evt) {
                /**
                 * We can't add a MouseWheelListener because it was not
                 * available in 1.3, so direct processing of events is necessary
                 */
                if (evt instanceof MouseEvent && evt.getID() == 507) {
                    try {
                        Method m = evt.getClass().getMethod("getWheelRotation", new Class[] {});
                        Rectangle r = textScroller.getViewport().getViewRect();
                        int y = (int) (Math.min(Math.max(0, r.y
                                        + (3 * text.getFontMetrics(text.getFont()).getHeight() * ((Integer) m.invoke(evt,
                                                        new Object[] {})).intValue())), text.getSize().getHeight() - r.height));
                        textScroller.getViewport().setViewPosition(new Point(r.x, y));

                    } catch (Throwable t) {
                        //	In theory, this should never happen
                    }
                } else {
                    super.processEvent(evt);
                }
            }

        };
        text.getDocument().addDocumentListener(this);
        text.setFont(StringUtil.stringToFont(context.getHost().getProperty(Constants.OPTIONS_EDITOR_FONT, "monospaced,0,12")));
        text.addCaretListener(new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                setAvailableActions();
            }
        });
        text.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showPopup(evt.getX(), evt.getY());
                }
            }
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showPopup(evt.getX(), evt.getY());
                }
            }
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    showPopup(evt.getX(), evt.getY());
                }
            }
        });
        
        //	Set the keyboard shortcuts
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();      

        //  Create the toolbar
        JToolBar toolBar = new JToolBar(res.getString("miniTextEditor.annotationTools.title"));
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.setBorder(null);
        toolBar.setFloatable(false);
        boolean showSelectiveText = context.getHost().getBooleanProperty(Constants.TOOL_BAR_SHOW_SELECTIVE_TEXT, true);

        if (type == CONFLICT_RESOLVER) {
            toolBar.add(UIUtil.createButton(resolveAction = new ResolveAction(), showSelectiveText, false));
            toolBar.add(UIUtil.createButton(previousConflictAction = new PreviousConflictAction(), showSelectiveText, false));
            toolBar.add(UIUtil.createButton(nextConflictAction = new NextConflictAction(), showSelectiveText, false));
            toolBar.add(new ToolBarSeparator());
            conflictNavigator = new ConflictNavigator(text, textScroller, conflicts, context);
            addToMap(inputMap, actionMap, resolveAction);
            addToMap(inputMap, actionMap, previousConflictAction);
            addToMap(inputMap, actionMap, nextConflictAction);
        }

        if (showOpen) {
            toolBar.add(UIUtil.createButton(openAction = new OpenFileAction(), showSelectiveText, false));
            addToMap(inputMap, actionMap, openAction);

        }
        if (showSave) {
            toolBar.add(UIUtil.createButton(saveAction = new SaveFileAction(), showSelectiveText, false));
            addToMap(inputMap, actionMap, saveAction);
        }
        if (showSaveAs) {
            toolBar.add(UIUtil.createButton(saveAsAction = new SaveAsFileAction(), showSelectiveText, false));
            addToMap(inputMap, actionMap, saveAsAction);

        }
        if (showOpen || showSave || showSaveAs) {
            toolBar.add(new ToolBarSeparator());
        }
        toolBar.add(UIUtil.createButton(searchAction = new SearchAction(), showSelectiveText, false));
        toolBar.add(new ToolBarSeparator());
        toolBar.add(UIUtil.createButton(cutAction = new CutAction(), showSelectiveText, false));
        toolBar.add(UIUtil.createButton(copyAction = new CopyAction(), showSelectiveText, false));
        toolBar.add(UIUtil.createButton(pasteAction = new PasteAction(), showSelectiveText, false));
        toolBar.add(UIUtil.createButton(deleteAction = new DeleteAction(), showSelectiveText, false));

        toolBar.add(new ToolBarSeparator());
        toolBar.add(UIUtil.createButton(undoAction = new UndoAction(), showSelectiveText, false));
        toolBar.add(UIUtil.createButton(redoAction = new RedoAction(), showSelectiveText, false));

        addToMap(inputMap, actionMap, searchAction);
        addToMap(inputMap, actionMap, cutAction);
        addToMap(inputMap, actionMap, copyAction);
        addToMap(inputMap, actionMap, pasteAction);
        addToMap(inputMap, actionMap, deleteAction);
        addToMap(inputMap, actionMap, undoAction);
        addToMap(inputMap, actionMap, redoAction);

        if (wordWrapPropertyName != null) {
            toolBar.add(new ToolBarSeparator());
            toolBar.add(wordWrap = new JCheckBox(res.getString("miniTextEditor.wordWrap.text")));
            wordWrap.setOpaque(false);
            wordWrap.setMnemonic(ResourceUtil.getResourceMnemonic(res, "miniTextEditor.wordWrap.mnemonic"));
        }

        //
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.NORTH);
        topPanel.add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);

        //
        final WrapPanel wrapPanel = new WrapPanel(text, (wordWrapPropertyName == null) ? true : context.getHost()
                        .getBooleanProperty(wordWrapPropertyName, wordWrapDefault));

        if (wordWrapPropertyName != null) {
            wordWrap.setSelected(wrapPanel.isWrap());

            //  Line numbers
            //  Scroller
        }
        textScroller.setViewportView(wrapPanel);
        textScroller.setBorder(null);

        if ((type == CONFLICT_RESOLVER) || (type == LINE_NUMBERED_EDITOR)) {
            LineNumber lineNumber = new LineNumber(text);
            lineNumber.setPreferredSize(99999);
            textScroller.setRowHeaderView(lineNumber);
        }

        //  Build this
        add(topPanel, BorderLayout.NORTH);
        add(textScroller, BorderLayout.CENTER);
        if (conflictNavigator != null) {
            JPanel p = new JPanel(new GridLayout(1, 1));
            p.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            p.add(conflictNavigator);
            add(p, BorderLayout.EAST);
        }

        //
        if (wordWrapPropertyName != null) {
            wordWrap.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    context.getHost().setBooleanProperty(Constants.MINI_TEXT_EDITOR_WORD_WRAP, wordWrap.isSelected());
                    wrapPanel.setWrap(!wrapPanel.isWrap());

                }
            });

            //  Set the intially available actions
        }  

        
        setAvailableActions();
    }
    
    private void addToMap(InputMap inputMap, ActionMap actionMap, Action action) {
        inputMap.put((KeyStroke)action.getValue(Action.ACCELERATOR_KEY), action.getValue(Action.NAME));
        actionMap.put(action.getValue(Action.NAME), action);        
    }

    public void undo() {
        undoManager.undo();
    }

    public void redo() {
        undoManager.redo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
     */
    public void caretUpdate(CaretEvent e) {
        if (type == CONFLICT_RESOLVER) {
            setAvailableActions();
        }
    }

    /**
     * Return the editor type. This will be one of ..
     * 
     * <ul>
     * <li>MiniTextEditor.PLAIN_EDITOR</li>
     * <li>MiniTextEditor.LINE_NUMBERED_EDITOR</li>
     * <li>MiniTextEditor.CONFLICT_RESOLVER</li>
     * </ul>
     */
    public int getType() {
        return type;
    }

    /**
     * Clean up the editor
     */
    public void cleanUp() {
        if (dialog != null) {
            context.getHost().saveGeometry(dialog, Constants.OPTION_DIALOG_GEOMETRY_PREFIX + "search");
            dialog.dispose();
            context.getHost().setBooleanProperty(Constants.EDITOR_SEARCH_DIALOG_CASE_SENSITIVE, criteria.isCaseSensitive());
            context.getHost().setBooleanProperty(Constants.EDITOR_SEARCH_DIALOG_WHOLE_WORDS, criteria.isWholeWords());
            search.cleanUp();
        }

        if (wordWrapPropertyName != null) {
            context.getHost().setBooleanProperty(wordWrapPropertyName, wordWrap.isSelected());
        }
    }

    /**
     * Return the file that is being edited (or <code>null</code> if no file
     * is being editied.
     * 
     * @return file being edited
     */
    public File getFile() {
        return file;
    }

    /**
     * Can the search dialog close
     * 
     * @param dialog
     *            the dialog
     * @param option
     *            the option that was select
     * @return can close the dialog
     */
    public boolean canClose(OptionDialog dialog, OptionDialog.Option option) {
        if (option == next) {
            return nextSearch();
        } else {

            return true;
        }
    }

    private boolean nextSearch() {
        Match match = null;

        while (true) {
            if ((searcher == null) || search.isCriteriaChanged()) {
                search.applyCriteria();
                searcher = new Searcher(text.getDocument());
                searcher.startSearch(criteria);
                match = searcher.nextMatch();

                if (match == null) {
                    Toolkit.getDefaultToolkit().beep();
                    searcher = null;

                    break;
                }
            } else {
                match = searcher.nextMatch();

            }
            if (match != null) {
                text.setCaretPosition(match.getStart());

                Caret caret = text.getCaret();
                caret.setVisible(true);
                caret.setSelectionVisible(true);
                text.setSelectionStart(match.getStart());
                text.setSelectionEnd(match.getStart() + match.getLength());
                text.scrollRectToVisible(text.getVisibleRect());

                return false;
            } else {
                searcher = null;
            }
        }

        return false;

    }

    /**
     * Resolve the currently selected conflict
     */
    public void resolveSelected() {
        Conflict c = (Conflict) conflicts.elementAt(conflictIdx);
        Document doc = text.getDocument();
        adjusting = true;

        try {
            if (inBlock2) {
                int se = Utilities.getRowStart(text, (c.getStart2() + c.getLength2()) - 1);
                int le = Utilities.getRowEnd(text, se) - se;
                doc.remove(se, le + 1);
                doc.remove(c.getStart1(), c.getStart2() - c.getStart1());
            } else {
                int se = Utilities.getRowStart(text, c.getStart2() - 1);
                int le = Utilities.getRowEnd(text, c.getStart2() + c.getLength2()) - se;
                doc.remove(se, le + 1);
                le = Utilities.getRowEnd(text, c.getStart1()) - c.getStart1();
                doc.remove(c.getStart1(), le + 1);
            }
        } catch (BadLocationException ble) {
        }

        changed = true;
        adjusting = false;
        highlightConflicts();
    }

    /**
     * Show the search dialog
     */
    public void search() {
        if (dialog == null) {
            criteria = new SearchCriteria(text.getSelectedText(), context.getHost().getBooleanProperty(
                            Constants.EDITOR_SEARCH_DIALOG_CASE_SENSITIVE, false), context.getHost().getBooleanProperty(
                            Constants.EDITOR_SEARCH_DIALOG_WHOLE_WORDS, false));
            next = new OptionDialog.Option(res.getString("miniTextEditor.search.option.next.text"), res
                            .getString("miniTextEditor.search.option.next.toolTipText"), ResourceUtil.getResourceMnemonic(res,
                            "miniTextEditor.search.option.next.mnemonic"));
            close = new OptionDialog.Option(res.getString("miniTextEditor.search.option.close.text"), res
                            .getString("miniTextEditor.search.option.close.toolTipText"), ResourceUtil.getResourceMnemonic(res,
                            "miniTextEditor.search.option.close.mnemonic"));

            search = new SearchPane(criteria, context);
            search.getSearchTermComponent().addActionListener(this);

            Window w = (Window) SwingUtilities.getAncestorOfClass(Window.class, this);
            OptionDialog.Option[] options = { next, close};
            OptionDialog.Option defaultOption = next;
            String title = res.getString("miniTextEditor.search.title");

            if (w instanceof JFrame) {
                dialog = new OptionDialog((JFrame) w, options, search, title, defaultOption, this, context, false, UIUtil
                                .getCachedIcon(Constants.ICON_TOOL_LARGE_SEARCH), "search");
            } else if (w instanceof JDialog) {
                dialog = new OptionDialog((JDialog) w, options, search, title, defaultOption, this, context, false, UIUtil
                                .getCachedIcon(Constants.ICON_TOOL_LARGE_SEARCH), "search");
            } else {
                dialog = new OptionDialog((JFrame) null, options, search, title, defaultOption, this, context, false, UIUtil
                                .getCachedIcon(Constants.ICON_TOOL_LARGE_SEARCH), "search");

            }
            context.getHost().loadGeometry(dialog, Constants.OPTION_DIALOG_GEOMETRY_PREFIX + "search");
            dialog.pack();
            dialog.setSize(360, dialog.getSize().height);
            dialog.getRootPane().setDefaultButton(dialog.getDefaultButton());
            dialog.setResizable(false);
        }

        dialog.setVisible(!dialog.isVisible());
    }

    public void grabFocus() {
        text.grabFocus();
    }

    /**
     * Show the save as dialog
     */
    public void saveAs() {
        JFileChooser chooser = new JFileChooser((file == null) ? System.getProperty("user.home") : file.getParent());

        if (file != null) {
            chooser.setSelectedFile(file);

        }
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(res.getString("miniTextEditor.saveAs.title"));
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            public String getDescription() {
                return res.getString("miniTextEditor.saveAs.filter.textFile.description");
            }
        });

        if (chooser.showSaveDialog(MiniTextEditor.this) == JFileChooser.APPROVE_OPTION) {
            //  Save the current chooser directory
            File f = chooser.getSelectedFile();
            context.getHost().setProperty(Constants.LAST_EDITOR_SAVE_LOCATION, f.getParentFile().getAbsolutePath());
            save(f, true, currentEncoding);
        }
    }

    /**
     * Save the current text as a specified file, optionally checking for its
     * existance. If this option is
     * 
     * @param f
     *            the file to save the text as
     * @param checkExists
     *            check whether the file exists.
     */
    public void save(File f, boolean checkExists, String encoding) {

        if (!checkForUnresolvedConflicts()) {
            //  Check if the file exists
            if (f.exists() && checkExists) {
                if (JOptionPane.showConfirmDialog(MiniTextEditor.this, res
                                .getString("miniTextEditor.save.warning.fileAreadyExists.text"), res
                                .getString("miniTextEditor.save.warning.fileAreadyExists.title"), JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) { return; }
            }

            //  Create the file and write to corrections to it
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(f);
                Writer writer = encoding == null ? new OutputStreamWriter(out) : new OutputStreamWriter(out, encoding);
                PrintWriter pw = new PrintWriter(writer);
                pw.print(text.getText());
                pw.flush();
                changed = false;
                context.getViewManager().externalFileUpdate(f);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MiniTextEditor.this, e.getMessage());
            } finally {
                GruntspudUtil.closeStream(out);
            }
        }
    }

    /**
     * Save to a file using the current encoding
     * 
     * @param file
     * @param saveAs
     *            force save as
     */
    public void save(File file, boolean saveAs) {
        save(file, saveAs, currentEncoding);

    }

    /**
     * Open a file. A file chooser is presented
     */
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        String dir = context.getHost().getProperty(Constants.LAST_EDITOR_OPEN_LOCATION);

        if (dir != null) {
            chooser.setCurrentDirectory(new File(dir));

        }
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle(res.getString("miniTextEditor.load.title"));
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {

            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            public String getDescription() {
                return res.getString("miniTextEditor.load.filter.textFile.description");
            }
        });

        if (chooser.showOpenDialog(MiniTextEditor.this) == JFileChooser.APPROVE_OPTION) {
            openFile(chooser.getSelectedFile(), null);
        }
    }

    /**
     * Open a specified file and place it in the editor.
     * 
     * @param f
     *            file to open
     */
    public void openFile(File f, String encoding) {
        //  Create the file and write to corrections to it
        FileInputStream in = null;
        currentEncoding = encoding;

        try {
            in = new FileInputStream(f);
            undoManager.discardAllEdits();
            Reader reader = encoding == null ? new InputStreamReader(in) : new InputStreamReader(in, encoding);
            char[] buf = new char[256];
            int r = 0;
            text.getDocument().remove(0, text.getDocument().getLength());
            while (true) {
                r = reader.read(buf);
                if (r == -1) {
                    break;
                }
                text.getDocument().insertString(text.getDocument().getLength(), new String(buf, 0, r), null);
            }
            text.setCaretPosition(0);
            changed = false;
            file = f;
            setEditable(f.canWrite());

            if (getType() == CONFLICT_RESOLVER) {
                highlightConflicts();

            }
            setAvailableActions();
        } catch (Exception e) {
            GruntspudUtil.showErrorMessage(MiniTextEditor.this, res.getString("miniTextEditor.opemFile.error.title"), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    GruntspudUtil.showErrorMessage(MiniTextEditor.this, "Error", ioe);
                }
            }
        }
    }

    /**
     * Get whether or not the text has changed since the last save.
     * 
     * @return text has changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Set whether or not the text is editable
     * 
     * @param editable
     *            editable
     */
    public void setEditable(boolean editable) {
        text.setEditable(editable);
        setAvailableActions();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private synchronized void textChanged(DocumentEvent evt) {
        if (!adjusting) {
            if (!parseTimer.isRunning()) {
                parseTimer.start();
            }
            changed = true;
            setAvailableActions();
        }
    }

    /**
     * Show the popup
     * 
     * @param x
     *            x position
     * @param y
     *            y position
     */
    private void showPopup(int x, int y) {
        //  Lazily create the popup
        if (popup == null) {
            popup = new JPopupMenu("Tools");
            popup.add(UIUtil.createMenuItem(cutAction));
            popup.add(UIUtil.createMenuItem(copyAction));
            popup.add(UIUtil.createMenuItem(pasteAction));
            popup.add(UIUtil.createMenuItem(deleteAction));
        }

        //
        popup.show(text, x, y);
    }

    /**
     * Lost ownership of the clipboard
     * 
     * @param clipboard
     * @param contents
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        setAvailableActions();
    }

    /**
     * Set what actions are available depending on the current state
     */
    private void setAvailableActions() {
        boolean editable = text.isEditable();
        boolean sel = editable && (text.getCaret().getDot() != text.getCaret().getMark());

        //  Undo
        boolean can = undoManager.canUndo() && editable;
        if (undoAction.isEnabled() != can) {
            undoAction.setEnabled(can);
        }
        can = undoManager.canRedo() && editable;
        if (redoAction.isEnabled() != can) {
            redoAction.setEnabled(can);

            //  Save action
        }

        //  Cut action
        if (cutAction.isEnabled() != sel) {
            cutAction.setEnabled(sel);

            //  Dlete action
        }
        if (deleteAction.isEnabled() != sel) {
            deleteAction.setEnabled(sel);

            //  Save action
        }
        if (saveAction != null && saveAction.isEnabled() != editable) {
            saveAction.setEnabled(editable);

            //  Paste action
        }
        if (pasteAction.isEnabled() != editable) {
            pasteAction.setEnabled(editable);

            //  Copy action
        }
        boolean enableCopy = text.getCaret().getDot() != text.getCaret().getMark();

        if (copyAction.isEnabled() != enableCopy) {
            copyAction.setEnabled(enableCopy);

            //  Resolve conflict
        }
        if (type == CONFLICT_RESOLVER) {
            int conflictIdx = -1;
            int pos = text.getCaretPosition();
            inBlock2 = false;
            previousConflictIdx = -1;
            nextConflictIdx = -1;

            for (int i = 0; (i < conflicts.size()); i++) {
                Conflict c = (Conflict) conflicts.elementAt(i);

                if (pos > (c.getStart2() + c.getLength1()) && previousConflictIdx == -1) {
                    previousConflictIdx = i;
                }

                if (pos < c.getStart1() && nextConflictIdx == -1) {
                    nextConflictIdx = i;
                }

                if ((pos >= c.getStart1()) && (pos <= (c.getStart1() + c.getLength1()))) {
                    conflictIdx = i;
                } else if ((pos >= c.getStart2()) && (pos <= (c.getStart2() + c.getLength2())) && (conflictIdx == -1)) {
                    conflictIdx = i;
                    inBlock2 = true;
                }
            }

            if (resolveAction.isEnabled() != (conflictIdx != -1)) {
                resolveAction.setEnabled(conflictIdx != -1);
            }

            if (previousConflictAction.isEnabled() != (previousConflictIdx != -1)) {
                previousConflictAction.setEnabled(previousConflictIdx != -1);
            }

            if (nextConflictAction.isEnabled() != (nextConflictIdx != -1)) {
                nextConflictAction.setEnabled(nextConflictIdx != -1);
            }
        }
    }

    /**
     * Highlight all of the conflicts in the current text
     */
    private void highlightConflicts() {
        try {
            adjusting = true;

            /**
             * @todo this could be done more efficiently. But this is quick and
             *       easy at the moment
             */
            int idx = 0;
            Match match = null;
            StyledDocument document = text.getStyledDocument();
            conflicts.removeAllElements();
            boolean oldChanged = isChanged();

            //  Creater the three searchers
            SearchCriteria startOfConflict = new SearchCriteria("<<<<<<< ", true, false);
            SearchCriteria middleOfConflict = new SearchCriteria("=======", true, false);
            SearchCriteria endOfConflict = new SearchCriteria(">>>>>>> ", true, false);
            Searcher searcher = new Searcher(document);
            searcher.startSearch(startOfConflict);

            //
            try {
                SimpleAttributeSet defaultStyle = new SimpleAttributeSet();
                StyleConstants.setForeground(defaultStyle, text.getForeground());
                StyleConstants.setBackground(defaultStyle, text.getBackground());
                document.setCharacterAttributes(0, document.getLength(), defaultStyle, true);

                while (true) {
                    //  Look for the start of conflict marker
                    Match startMatch = searcher.nextMatch();

                    if (startMatch == null) {
                        break;
                    }

                    if (startMatch.getStart() != Utilities.getRowStart(text, startMatch.getStart())) {
                        break;
                    }

                    //  Now look for the middle marker
                    searcher.startSearch(middleOfConflict, (startMatch.getStart() + startMatch.getLength()) - 1);

                    Match middleMatch = searcher.nextMatch();

                    if (middleMatch == null) {
                        break;
                    }

                    if ((middleMatch.getStart() != Utilities.getRowStart(text, middleMatch.getStart()))
                                    || ((middleMatch.getStart() + middleMatch.getLength()) != Utilities.getRowEnd(text, middleMatch
                                                    .getStart()))) {
                        break;
                    }

                    //  Now look for the end marker
                    searcher.startSearch(endOfConflict, (middleMatch.getStart() + middleMatch.getLength()) - 1);

                    Match endMatch = searcher.nextMatch();

                    if (endMatch == null) {
                        break;
                    }

                    if (endMatch.getStart() != Utilities.getRowStart(text, endMatch.getStart())) {
                        break;
                    }

                    //  We have a conlifct
                    int m = middleMatch.getStart() + middleMatch.getLength() + 1;
                    int re = Utilities.getRowEnd(text, endMatch.getStart());
                    Conflict conflict = new Conflict(startMatch.getStart(), middleMatch.getStart() - startMatch.getStart(), m, re
                                    - m);
                    conflicts.addElement(conflict);

                    text.getStyledDocument().setCharacterAttributes(conflict.getStart1(), conflict.getLength1(), block1, true);
                    text.getStyledDocument().setCharacterAttributes(conflict.getStart2(), conflict.getLength2(), block2, true);
                    text.getStyledDocument().setCharacterAttributes(conflict.getStart1() + conflict.getLength1(),
                                    conflict.getStart2() - (conflict.getStart1() + conflict.getLength1()), middle, true);

                    //  Next start of conflict
                    searcher.startSearch(startOfConflict, endMatch.getStart() + endMatch.getLength());
                }
                conflictNavigator.repaint();
            } catch (Exception e) {
                Constants.UI_LOG.error("Search failed", e);
            }

            changed = oldChanged;

            setAvailableActions();
        } finally {
            adjusting = false;
        }
    }

    /**
     * Return the text currently being edited
     * 
     * @return text
     */
    public String getText() {
        return text.getText();
    }

    /**
     * Set the text currently being edited
     * 
     * @param text
     *            text to edit
     */
    public void setText(String text) {
        this.text.setText(text);
    }

    /**
     * Return the text component
     * 
     * @return text component
     */
    public JTextComponent getTextComponent() {
        return text;
    }

    /**
     * When saving, this method should be called to check whether there are any
     * unresolved conflicts. If there are, a dialog will be displayed asking the
     * user to confirm. If the user cancels <code>true</code> will be returned
     * and the caller should abort its save operation. The user can also elect
     * to not receive this warning any more.
     * 
     * @return <code>true</code> if there are unresolved conflicts that the
     *         user didnt accept
     */
    public boolean checkForUnresolvedConflicts() {
        if (getType() == CONFLICT_RESOLVER) {
            if (conflicts.size() > 0) {
                OptionDialog.Option yes = new OptionDialog.Option(res
                                .getString("miniTextEditor.save.warning.unresolvedConflicts.option.yes.text"), res
                                .getString("miniTextEditor.save.warning.unresolvedConflicts.option.yes.toolTipText"), ResourceUtil
                                .getResourceMnemonic(res, "miniTextEditor.save.warning.unresolvedConflicts.option.yes.mnemonic"));
                OptionDialog.Option no = new OptionDialog.Option(res
                                .getString("miniTextEditor.save.warning.unresolvedConflicts.option.no.text"), res
                                .getString("miniTextEditor.save.warning.unresolvedConflicts.option.no.toolTipText"), ResourceUtil
                                .getResourceMnemonic(res, "miniTextEditor.save.warning.unresolvedConflicts.option.no.mnemonic"));
                return TellMeAgainPane.showTellMeAgainDialog(context, this, res
                                .getString("miniTextEditor.save.warning.unresolvedConflicts.tellMeAgain.text"),
                                "gruntspud.editor.warnAboutUnResolvedConflicts", res
                                                .getString("miniTextEditor.save.warning.unresolvedConflicts.text"),
                                new OptionDialog.Option[] { yes, no}, res
                                                .getString("miniTextEditor.save.warning.unresolvedConflicts.title"), UIUtil
                                                .getCachedIcon(Constants.ICON_TOOL_LARGE_RESOLVE)) == no;
            }
        }
        return false;
    }

    //  Supporting classes
    class CutAction extends AbstractCutAction {

        /**
         * Cut the currently selected text and place it in the clipboard
         */
        public void cut() {
            try {
                //  Get the system clipboard
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();

                //  Set the clipboard contents
                c.setContents(new StringSelection(text.getText().substring(text.getSelectionStart(), text.getSelectionEnd())),
                                MiniTextEditor.this);

                //  Remove the selection from the text area
                text.getDocument().remove(text.getSelectionStart(), text.getSelectionEnd() - text.getSelectionStart());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class DeleteAction extends AbstractDeleteAction {

        /**
         * Delete the currently selected text, <u>not </u> placing it in the
         * clipboard
         */
        public void delete() {
            try {
                text.getDocument().remove(text.getSelectionStart(), text.getSelectionEnd() - text.getSelectionStart());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class CopyAction extends AbstractCopyAction {

        /**
         * Copy the the selected text into the clipboard
         */
        public void copy() {
            try {
                //  Get the system clipboard
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();

                //  Set the clipboard contents
                c.setContents(new StringSelection(text.getText().substring(text.getSelectionStart(), text.getSelectionEnd())),
                                MiniTextEditor.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class PasteAction extends AbstractPasteAction {

        /**
         * Paste the current clipboard contents into the text at the cursor
         * position or over the selection
         */
        public void paste() {
            try {
                //  Get the system clipboard and the contents as a string
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                String s = c.getContents(this).getTransferData(DataFlavor.stringFlavor).toString();

                //  Set the clipboard contents. If there is no selection, paste
                // the
                //  contents at 'dot', otherwise replace the selection
                if ((text.getSelectionStart() != 0) || (text.getSelectionStart() != text.getCaret().getDot())) {
                    text.getDocument().remove(text.getSelectionStart(), text.getSelectionEnd() - text.getSelectionStart());

                }
                text.getDocument().insertString(text.getCaret().getDot(), s, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class UndoAction extends AbstractUndoAction {

        /**
         *  
         */
        public void undo() {
            MiniTextEditor.this.undo();
        }
    }

    class RedoAction extends AbstractRedoAction {

        /**
         *  
         */
        public void redo() {
            MiniTextEditor.this.redo();
        }
    }

    class OpenFileAction extends AbstractOpenAction {

        public OpenFileAction() {
            super();
            putValue(GruntspudAction.SHOW_NAME, Boolean.valueOf(ResourceUtil.getResourceBoolean(res,
                            "miniTextEditor.openFileAction.showName")));
        }

        /**
         * Open a file
         */
        public void open() {
            openFile();
        }
    }

    class SaveFileAction extends AbstractSaveAction {

        public SaveFileAction() {
            super();
            putValue(GruntspudAction.SHOW_NAME, Boolean.valueOf(ResourceUtil.getResourceBoolean(res,
                            "miniTextEditor.saveFileAction.showName")));
        }

        /**
         * Save the file, if the current edit has no file associated with it
         * show the save as dialog
         */
        public void save() {
            if (file == null) {
                saveAs();
            } else {
                MiniTextEditor.this.save(file, false, currentEncoding);
            }
        }
    }

    class SaveAsFileAction extends AbstractSaveAsAction {

        /**
         * Save file, asking what to save it as in a chooser first
         */
        public void saveAs() {
            MiniTextEditor.this.saveAs();
        }
    }

    class SearchAction extends AbstractSearchAction {

        /**
         * Show the search dialog
         * 
         * @param evt
         *            evt causing the action
         */
        public void actionPerformed(ActionEvent evt) {
            MiniTextEditor.this.search();
        }
    }

    class NextConflictAction extends AbstractNextAction {

        public void actionPerformed(ActionEvent evt) {
            Conflict c = (Conflict) conflicts.get(nextConflictIdx);
            text.setCaretPosition(c.getStart1() + c.getLength1());
            text.scrollRectToVisible(text.getVisibleRect());
        }
    }

    class PreviousConflictAction extends AbstractPreviousAction {

        public void actionPerformed(ActionEvent evt) {
            Conflict c = (Conflict) conflicts.get(previousConflictIdx);
            text.setCaretPosition(c.getStart1() + c.getLength1());
            text.scrollRectToVisible(text.getVisibleRect());
        }
    }

    /**
     * Wrapper component to give a <code>JTextPane</code> word wrap
     * capabilities
     */
    class WrapPanel extends JPanel {

        private boolean wrap = true;

        /**
         * Creates a new WrapPanel object.
         * 
         * @param textPane
         *            the text component to wrap
         * @param wrap
         *            word wrap
         */
        public WrapPanel(JTextPane textPane, boolean wrap) {
            super(new BorderLayout());
            this.wrap = wrap;
            add(textPane, BorderLayout.CENTER);
        }

        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();

            if (wrap) {
                d.width = 1;

            }
            return d;
        }

        /**
         * Set the word wrap is on or off
         * 
         * @param wrap
         *            word wrap
         */
        public void setWrap(boolean wrap) {
            this.wrap = wrap;

            Container con = getParent();

            if (con == null) { return; }

            con.invalidate();
            con.validate();
            con.doLayout();
            con.repaint();
        }

        /**
         * Is word wrap enabled
         * 
         * @return word wrap enabled
         */
        public boolean isWrap() {
            return wrap;
        }
    }

    class EditorTextPane extends JTextPane {

        private Insets insets;

        /**
         * Construct a new editor
         * 
         * @param
         */
        EditorTextPane() {
            setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            insets = getBorder().getBorderInsets(this);

            if (insets == null) {
                insets = new Insets(0, 0, 0, 0);

            }
            Color c = ColorUtil.getColor(Constants.OPTIONS_EDITOR_BACKGROUND, UIManager.getColor("TextPane.background"), context);
            setBackground(c);
            c = ColorUtil.getColor(Constants.OPTIONS_EDITOR_FOREGROUND, UIManager.getColor("TextPane.foreground"), context);
            setForeground(c);
            setCaretColor(c);
            setOpaque(false);
            setTabs(context.getHost().getIntegerProperty(Constants.OPTIONS_EDITOR_TAB_SIZE, 4));
        }

        /**
         * Paint the text pane
         */
        public void paintComponent(Graphics g) {

            g.setColor(getBackground());
            g.fillRect(0, 0, getSize().width, getSize().height);

            try {
                for (Enumeration e = conflicts.elements(); e.hasMoreElements();) {
                    Conflict c = (Conflict) e.nextElement();

                    //	Current version
                    Rectangle s1 = modelToView(c.getStart1());
                    Rectangle e1 = modelToView((c.getStart1() + c.getLength1()) - 1);
                    g.setColor(StyleConstants.getBackground(block1));
                    g.fillRect(insets.left, s1.y, getSize().width - insets.left - insets.right, (e1.y + e1.height) - s1.y);

                    //	Separator
                    Rectangle s2 = modelToView(c.getStart1() + c.getLength1());
                    Rectangle e2 = modelToView(c.getStart2() - 1);
                    g.setColor(StyleConstants.getBackground(middle));
                    g.fillRect(insets.left, s2.y, getSize().width - insets.left - insets.right, (e2.y + e2.height) - s2.y);

                    //	Conflicting version
                    Rectangle s3 = modelToView(c.getStart2());
                    Rectangle e3 = modelToView(c.getStart2() + c.getLength2());
                    g.setColor(StyleConstants.getBackground(block2));
                    g.fillRect(insets.left, s3.y, getSize().width - insets.left - insets.right, (e3.y + e3.height) - s3.y);
                }
            } catch (BadLocationException ble) {
                Constants.UI_LOG.error("Painting of conflicts failed", ble);
            }

            super.paintComponent(g);

            try {
                g.setColor(getForeground());

                for (Enumeration e = conflicts.elements(); e.hasMoreElements();) {
                    Conflict c = (Conflict) e.nextElement();
                    Rectangle s1 = modelToView(c.getStart1());
                    Rectangle e1 = modelToView((c.getStart1() + c.getLength1()) - 1);
                    g.drawRect(insets.left, s1.y, getSize().width - insets.left - insets.right, (e1.y + e1.height) - s1.y);

                    Rectangle s2 = modelToView(c.getStart1() + c.getLength1());
                    Rectangle e2 = modelToView(c.getStart2() - 1);
                    g.drawRect(insets.left, s2.y, getSize().width - insets.left - insets.right, (e2.y + e2.height) - s2.y);

                    Rectangle s3 = modelToView(c.getStart2());
                    Rectangle e3 = modelToView(c.getStart2() + c.getLength2());
                    g.drawRect(insets.left, s3.y, getSize().width - insets.left - insets.right, (e3.y + e3.height) - s3.y);
                }
            } catch (BadLocationException ble) {
                Constants.UI_LOG.error("Painting of conflicts failed", ble);
            }
        }

        public void setTabs(int charactersPerTab) {
            FontMetrics fm = getFontMetrics(getFont());
            int charWidth = fm.charWidth('w');
            int tabWidth = charWidth * charactersPerTab;

            TabStop[] tabs = new TabStop[10];

            for (int j = 0; j < tabs.length; j++) {
                int tab = j + 1;
                tabs[j] = new TabStop(tab * tabWidth);
            }
        }
    }

    /**
     * Resolve conflict action
     */
    class ResolveAction extends AbstractGruntspudAction {

        ResolveAction() {
            super(res, "miniTextEditor.resolveAction");
            putValue(AbstractGruntspudAction.SMALL_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_SMALL_RESOLVE));
            putValue(AbstractGruntspudAction.ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_RESOLVE));
            putValue(AbstractGruntspudAction.LARGE_ICON, UIUtil.getCachedIcon(Constants.ICON_TOOL_LARGE_RESOLVE));
        }

        public void actionPerformed(ActionEvent evt) {
            resolveSelected();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if(search != null && e.getSource() == search.getSearchTermComponent()) {
            nextSearch();
        }
        else {
	        if (getType() == CONFLICT_RESOLVER) {
	            highlightConflicts();
	        }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.ui.OptionDialog.Callback#close(gruntspud.ui.OptionDialog,
     *      gruntspud.ui.OptionDialog.Option)
     */
    public void close(OptionDialog dialog, Option option) {
        // TODO Auto-generated method stub

    }
}