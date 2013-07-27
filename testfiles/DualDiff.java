/*
* DualDiff.java
* Copyright (c) 2000, 2001, 2002 Andre Kaplan
* Copyright (c) 2006 Denis Koryavov
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

package jdiff;

import java.awt.Color;
import java.awt.Font;

import java.io.*;
import java.nio.*;

import java.util.*;
import javax.swing.*;

import jdiff.component.DiffLocalOverview;
import jdiff.component.DiffGlobalPhysicalOverview;
import jdiff.component.DiffOverview;
import jdiff.component.DiffLineOverview;
import jdiff.component.DiffTextAreaModel;
import jdiff.text.FileLine;
import jdiff.util.Diff;
import jdiff.util.JDiffDiff;
import jdiff.util.DualDiffUtil;
import jdiff.util.ScrollHandler;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.jedit.textarea.TextAreaPainter;

/**
 * This is the controller.  There are 2 DiffOverviews and optionally 1 DiffLineOverview
 * as the views.  The models are a DiffTextAreaModel for the DiffOverviews and a
 * DiffLineModel for the DiffLineOverview.
 */
public class DualDiff implements EBComponent {
    // diff options
    private boolean ignoreCase;
    private boolean trimWhitespace;
    private boolean ignoreAmountOfWhitespace;
    private boolean ignoreLineSeparators;
    private boolean ignoreAllWhitespace;

    // the actual diffs
    private Diff.Change edits;

    // gui objects this dual diff is acting on
    private View view;
    private EditPane editPane0;
    private EditPane editPane1;
    private JEditTextArea textArea0;
    private JEditTextArea textArea1;
    private DiffOverview diffOverview0;
    private DiffOverview diffOverview1;
    private DiffLineOverview diffLineOverview;
    private ScrollHandler scrollHandler;

    protected DualDiff(View view) {

        this(view, DualDiffUtil.ignoreCaseDefault, DualDiffUtil.trimWhitespaceDefault, DualDiffUtil.ignoreAmountOfWhitespaceDefault, DualDiffUtil.ignoreLineSeparatorsDefault, DualDiffUtil.ignoreAllWhitespaceDefault);
    }

    protected DualDiff(View view, boolean ignoreCase, boolean trimWhitespace, boolean ignoreAmountOfWhiteSpace, boolean ignoreLineSeparators, boolean ignoreAllWhiteSpace) {

        // diff options
        this.ignoreCase = ignoreCase;
        this.trimWhitespace = trimWhitespace;
        this.ignoreAmountOfWhitespace = ignoreAmountOfWhiteSpace;
        this.ignoreLineSeparators = ignoreLineSeparators;
        this.ignoreAllWhitespace = ignoreAllWhiteSpace;

        // gui objects
        this.view = view;
        EditPane[] editPanes = this.view.getEditPanes();
        this.editPane0 = editPanes[0];
        this.editPane1 = editPanes[1];
        this.textArea0 = this.editPane0.getTextArea();
        this.textArea1 = this.editPane1.getTextArea();
        scrollHandler = new ScrollHandler(this);

        // initialize
        refresh();
    }

    /**
     * @return the left EditPane
     */
    public EditPane getEditPane0() {
        return editPane0;
    }

    /**
     * @return the right EditPane
     */
    public EditPane getEditPane1() {
        return editPane1;
    }

    /**
     * @return the left text area
     */
    public TextArea getTextArea0() {
        return textArea0;
    }

    /**
     * @return the right text area
     */
    public TextArea getTextArea1() {
        return textArea1;
    }

    /**
     * @return the DiffOverview for the left text area
     */
    public DiffOverview getDiffOverview0() {
        return diffOverview0;
    }

    /**
     * @return the DiffOverview for the right text area
     */
    public DiffOverview getDiffOverview1() {
        return diffOverview1;
    }

    /**
     * This is called only from dockables.xml.
     * @return the line diff dockable
     */
    public DiffLineOverview getDiffLineOverview() {
        return diffLineOverview;
    }

    /**
     * @return the View that this DualDiff is acting on.
     */
    public View getView() {
        return view;
    }

    /**
     * @return the diffs found between the left and right text areas.
     */
    public Diff.Change getEdits() {
        return edits;
    }

    /**
     * Handle messages from the EditBus.
     */
    public void handleMessage(EBMessage message) {
        if (message instanceof BufferUpdate) {
            BufferUpdate bu = (BufferUpdate) message;
            Buffer b0 = (Buffer) textArea0.getBuffer();
            Buffer b1 = (Buffer) textArea1.getBuffer();
            if (!b0.equals(bu.getBuffer()) || !b1.equals(bu.getBuffer())) {
                // not my buffers
                return ;
            }
            if (bu.getWhat() == BufferUpdate.LOADED || bu.getWhat() == BufferUpdate.SAVED || bu.getWhat() == BufferUpdate.DIRTY_CHANGED) {
                refresh();
            }
        } else if (message instanceof EditPaneUpdate) {
            EditPaneUpdate epu = (EditPaneUpdate) message;
            EditPane editPane = epu.getEditPane();
            if (!view.equals(editPane.getView())) {
                // not my view
                return ;
            }
            if (epu.getWhat() == EditPaneUpdate.CREATED || epu.getWhat() == EditPaneUpdate.DESTROYED) {
                remove(true);
            } else if (epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED) {
                refresh();
            }
        } else if (message instanceof DiffMessage) {
            DiffMessage dm = (DiffMessage) message;
            if (!view.equals(dm.getView())) {
                // not my view
                return ;
            }
            if (DiffMessage.OFF.equals(dm.getWhat())) {
                remove(false);
            }
        } else if (message instanceof PropertiesChanged) {
            // maybe update properties
            boolean changed = false;
            boolean b = jEdit.getBooleanProperty("jdiff.ignore-case", false);
            if (b != ignoreCase) {
                setIgnoreCase(b);
                changed = true;
            }
            b = jEdit.getBooleanProperty("jdiff.trim-whitespace", false);
            if (b != trimWhitespace) {
                setTrimWhitespace(b);
                changed = true;
            }
            b = jEdit.getBooleanProperty("jdiff.ignore-amount-whitespace", false);
            if (b != ignoreAmountOfWhitespace) {
                setIgnoreAmountOfWhitespace(jEdit.getBooleanProperty("jdiff.ignore-amount-whitespace", false));
                changed = true;
            }
            b = jEdit.getBooleanProperty("jdiff.ignore-line-separators", true);
            if (b != ignoreLineSeparators) {
                setIgnoreLineSeparators(b);
                changed = true;
            }
            b = jEdit.getBooleanProperty("jdiff.ignore-all-whitespace", false);
            if (b != ignoreAllWhitespace) {
                setIgnoreAllWhitespace(jEdit.getBooleanProperty("jdiff.ignore-all-whitespace", false));
                changed = true;
            }
            if (changed) {
                refresh();
            }
        }
    }

    public boolean getIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public void toggleIgnoreCase() {
        ignoreCase = !ignoreCase;
    }

    public boolean getTrimWhitespace() {
        return trimWhitespace;
    }

    public void setTrimWhitespace(boolean trimWhitespace) {
        this.trimWhitespace = trimWhitespace;
    }

    public void toggleTrimWhitespace() {
        trimWhitespace = !trimWhitespace;
    }

    public boolean getIgnoreAmountOfWhitespace() {
        return ignoreAmountOfWhitespace;
    }

    public void setIgnoreAmountOfWhitespace(boolean ignoreAmountOfWhitespace) {
        this.ignoreAmountOfWhitespace = ignoreAmountOfWhitespace;
    }

    public void toggleIgnoreAmountOfWhitespace() {
        ignoreAmountOfWhitespace = !ignoreAmountOfWhitespace;
    }

    public void toggleIgnoreLineSeparators() {
        ignoreLineSeparators = !ignoreLineSeparators;
    }

    public boolean getIgnoreLineSeparators() {
        return ignoreLineSeparators;
    }

    public void setIgnoreLineSeparators(boolean ignoreLineSeparators) {
        this.ignoreLineSeparators = ignoreLineSeparators;
    }

    public boolean getIgnoreAllWhitespace() {
        return ignoreAllWhitespace;
    }

    public void setIgnoreAllWhitespace(boolean ignoreAllWhitespace) {
        this.ignoreAllWhitespace = ignoreAllWhitespace;
    }

    public void toggleIgnoreAllWhitespace() {
        ignoreAllWhitespace = !ignoreAllWhitespace;
    }

    // reinstalls this DualDiff for the same View
    protected void refresh() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // if the view isn't split, don't refresh
                JSplitPane sp = view.getSplitPane();
                if (sp != null) {
                    // remove
                    EditBus.removeFromBus(DualDiff.this);
                    removeHandlers();
                    removeHighlighters();
                    removeOverviews();

                    // install
                    installOverviews();
                    installHighlighters();
                    installHandlers();

                    // reset overviews
                    diffLineOverview.clear();
                    DiffTextAreaModel taModel = new DiffTextAreaModel(DualDiff.this);
                    diffOverview0.setModel(taModel);
                    diffOverview0.synchroScrollRight();
                    diffOverview1.setModel(taModel);
                    diffOverview1.repaint();

                    EditBus.addToBus(DualDiff.this);

                    // possibly show the dockable
                    DockableWindowManager dwm = view.getDockableWindowManager();
                    if (!dwm.isDockableWindowVisible(DualDiffManager.JDIFF_LINES) && jEdit.getBooleanProperty("jdiff.auto-show-dockable")) {
                        if (dwm.getDockableWindow(DualDiffManager.JDIFF_LINES) == null) {
                            dwm.addDockableWindow(DualDiffManager.JDIFF_LINES);
                        }
                        dwm.showDockableWindow(DualDiffManager.JDIFF_LINES);
                    }

                    // make sure View divider is in the middle
                    sp.setDividerLocation(0.5);
                    view.invalidate();
                    view.validate();
                }
            }
        }
       );
    }

    /**
     * Removes this DualDiff from our View
     * @param propagate If true, tell DualDiffManager to do a remove also.
     */
    private void remove(boolean propagate) {
        EditBus.removeFromBus(this);
        removeOverviews();
        removeHighlighters();
        removeHandlers();

        // turn off the dockable if it is visible
        view.getDockableWindowManager().hideDockableWindow(DualDiffManager.JDIFF_LINES);

        diffLineOverview.setModel(null);
        if (propagate) {
            DualDiffManager.removeFrom(view);
        }
    }

    private void installOverviews() {
        Buffer buf0 = editPane0.getBuffer();
        Buffer buf1 = editPane1.getBuffer();

        if (!buf0.isLoaded() || !buf1.isLoaded()) {
            edits = null;
            diffOverview0 = new DiffLocalOverview(this);
            diffOverview1 = new DiffGlobalPhysicalOverview(this);
        } else {
            FileLine[] fileLines0 = DualDiffUtil.getFileLines(this, buf0);
            FileLine[] fileLines1 = DualDiffUtil.getFileLines(this, buf1);

            Diff d = new JDiffDiff(fileLines0, fileLines1);
            edits = d.diff_2();
            diffOverview0 = new DiffLocalOverview(this);
            diffOverview1 = new DiffGlobalPhysicalOverview(this);
            diffLineOverview = new DiffLineOverview(this, view);
        }
        textArea0.addLeftOfScrollBar(diffOverview0);
        textArea1.addLeftOfScrollBar(diffOverview1);
    }

    // remove overviews and merge controls
    private void removeOverviews() {
        if (textArea0 != null && diffOverview0 != null) {
            textArea0.removeLeftOfScrollBar(diffOverview0);
        }
        if (textArea1 != null && diffOverview1 != null) {
            textArea1.removeLeftOfScrollBar(diffOverview1);
        }
    }

    private void installHighlighters() {
        DiffHighlight diffHighlight0 = (DiffHighlight) DiffHighlight.getHighlightFor(editPane0);
        if (diffHighlight0 == null) {
            diffHighlight0 = (DiffHighlight) DiffHighlight.addHighlightTo(editPane0, edits, DiffHighlight.LEFT);
            textArea0.getPainter().addExtension(TextAreaPainter.BELOW_SELECTION_LAYER, diffHighlight0);
        } else {
            diffHighlight0.setEdits(edits);
            diffHighlight0.setPosition(DiffHighlight.LEFT);
        }
        diffHighlight0.setEnabled(true);
        diffHighlight0.updateTextArea();

        DiffHighlight diffHighlight1 = (DiffHighlight) DiffHighlight.getHighlightFor(editPane1);
        if (diffHighlight1 == null) {
            diffHighlight1 = (DiffHighlight) DiffHighlight.addHighlightTo(editPane1, edits, DiffHighlight.RIGHT);
            textArea1.getPainter().addExtension(TextAreaPainter.BELOW_SELECTION_LAYER, diffHighlight1);
        } else {
            diffHighlight1.setEdits(edits);
            diffHighlight1.setPosition(DiffHighlight.RIGHT);
        }
        diffHighlight1.setEnabled(true);
        diffHighlight1.updateTextArea();
    }

    private void removeHighlighters() {
        DiffHighlight diffHighlight0 = (DiffHighlight) DiffHighlight.getHighlightFor(editPane0);
        if (diffHighlight0 != null) {
            diffHighlight0.setEnabled(false);
            diffHighlight0.updateTextArea();
            DiffHighlight.removeHighlightFrom(editPane0);
        }

        DiffHighlight diffHighlight1 = (DiffHighlight) DiffHighlight.getHighlightFor(editPane1);
        if (diffHighlight1 != null) {
            diffHighlight1.setEnabled(false);
            diffHighlight1.updateTextArea();
            DiffHighlight.removeHighlightFrom(editPane1);
        }
    }

    private void installHandlers() {
        textArea0.addScrollListener(scrollHandler);
        textArea0.addFocusListener(scrollHandler);
        textArea0.getDisplayManager().expandAllFolds();

        textArea1.addScrollListener(scrollHandler);
        textArea1.addFocusListener(scrollHandler);
        textArea1.getDisplayManager().expandAllFolds();
    }

    private void removeHandlers() {
        textArea0.removeScrollListener(scrollHandler);
        textArea0.removeFocusListener(scrollHandler);

        textArea1.removeScrollListener(scrollHandler);
        textArea1.removeFocusListener(scrollHandler);
    }

    /**
     * Provided so other components can use the font of the
     * text area in their own display.
     * @return the font of the left text area.
     */
    public Font getFont() {
        return textArea0.getPainter().getFont();
    }

    /**
     * Provided so other components can use the background color of the
     * text area to paint themselves with the appropriate color.
     * @return the background color of the left text area.
     */
    public Color getBackground() {
        return textArea0.getPainter().getBackground();
    }

    protected void nextDiff0() {
        Diff.Change hunk = edits;
        int caretLine = textArea0.getCaretLine();
        for (; hunk != null; hunk = hunk.next) {
            if (hunk.first0 > caretLine + ((hunk.lines0 == 0) ? 1 : 0)) {
                int line = hunk.first0;
                if (line >= textArea0.getLineCount()) {
                    line = textArea0.getLineCount() - 1;   
                }

                // move the caret to the start of the first line of the diff
                int caret_position = textArea0.getLineStartOffset(line);
                textArea0.setCaretPosition(caret_position, false);

                // scroll so line is visible
                int visibleLines = textArea0.getVisibleLines();
                int leftLineCount = textArea0.getLineCount();
                int distance = 1;
                if (line > leftLineCount - visibleLines) {
                    textArea0.setFirstLine(leftLineCount - visibleLines);
                    distance = line - (leftLineCount - visibleLines);
                } else {
                    textArea0.setFirstLine(line - 1);
                }

                // move caret in other text area to start of diff hunk
                // and scroll to it
                caret_position = textArea1.getLineStartOffset(hunk.first1 >= textArea1.getLineCount() ? textArea1.getLineCount() - 1 : hunk.first1);
                textArea1.setCaretPosition(caret_position, false);
                textArea1.setFirstLine(caret_position - distance);

                // maybe move the caret to the first actual diff character
                if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
                    DualDiffUtil.centerOnDiff(textArea0, textArea1);

                    // maybe select the first diff word
                    if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                        textArea0.selectWord();
                        textArea1.selectWord();
                    }
                }

                if (textArea0.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
                    textArea0.getToolkit().beep();
                }
                return ;
            }
        }

        if (jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea1.getToolkit().beep();
        }
    }

    protected void nextDiff1() {
        Diff.Change hunk = edits;
        int caretLine = textArea1.getCaretLine();
        for (; hunk != null; hunk = hunk.next) {
            if (hunk.first1 > caretLine + ((hunk.lines1 == 0) ? 1 : 0)) {
                int line = hunk.first1;
                if (line > textArea1.getLineCount()) {
                    line = textArea1.getLineCount() - 1;   
                }

                // move the caret to the start of the first line of the diff
                int caret_position = textArea1.getLineStartOffset(line);
                textArea1.setCaretPosition(caret_position, false);

                // scroll so line is visible
                int visibleLines = textArea1.getVisibleLines();
                int rightLineCount = textArea1.getLineCount();
                int distance = 1;
                if (line > rightLineCount - visibleLines) {
                    textArea1.setFirstLine(rightLineCount - visibleLines);
                    distance = line - (rightLineCount - visibleLines);
                } else {
                    textArea1.setFirstLine(line - 1);
                }

                // move caret in other text area to start of diff hunk
                // and scroll to it
                caret_position = textArea0.getLineStartOffset(hunk.first0 >= textArea0.getLineCount() ? textArea0.getLineCount() - 1 : hunk.first0);
                textArea0.setCaretPosition(caret_position, false);
                textArea0.setFirstLine(caret_position - distance);

                // maybe move the caret to the first actual diff character
                if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
                    DualDiffUtil.centerOnDiff(textArea0, textArea1);

                    // maybe select the first diff word
                    if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                        textArea0.selectWord();
                        textArea1.selectWord();
                    }
                }

                if (textArea1.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
                    textArea1.getToolkit().beep();
                }
                return ;
            }
        }

        if (jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea1.getToolkit().beep();
        }
    }

    protected void prevDiff0() {
        Diff.Change hunk = edits;
        int caretLine = textArea0.getCaretLine();
        for (; hunk != null; hunk = hunk.next) {
            if (hunk.first0 < caretLine) {
                // hunk starts before the caret line.  If caret line is in hunk,
                // go to start of current hunk.  If caret line is after end of
                // current hunk, but before the next hunk, go to start of current
                // hunk.
                // caret is in current hunk ||
                // caret is after last hunk ||
                // caret is before next hunk
                if (hunk.first0 + hunk.lines0 > caretLine || hunk.next == null || hunk.next.first0 >= caretLine) {  // NOPMD
                    int line = hunk.first0;                    // first line of diff hunk

                    // move caret to start of diff hunk
                    int caret_position = textArea0.getLineStartOffset(line);
                    textArea0.setCaretPosition(caret_position, false);

                    // scroll so line is visible
                    int visibleLines = textArea0.getVisibleLines();
                    int leftLineCount = textArea0.getLineCount();
                    int distance = 1;
                    if (line > leftLineCount - visibleLines) {
                        textArea0.setFirstLine(leftLineCount - visibleLines);
                        distance = line - (leftLineCount - visibleLines);
                    } else {
                        textArea0.setFirstLine(line - 1);
                    }

                    // move caret in other text area to start of diff hunk
                    // and scroll to it
                    caret_position = textArea1.getLineStartOffset(hunk.first1);
                    textArea1.setCaretPosition(caret_position, false);
                    textArea1.setFirstLine(hunk.first1 - distance);

                    // maybe move the caret to the first actual diff character
                    if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
                        DualDiffUtil.centerOnDiff(textArea0, textArea1);

                        // maybe select the first diff word
                        if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                            textArea0.selectWord();
                            textArea1.selectWord();
                        }
                    }

                    if (textArea0.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
                        textArea0.getToolkit().beep();
                    }
                    return ;
                }
            }
        }

        if (jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea0.getToolkit().beep();
        }
    }

    protected void prevDiff1() {
        Diff.Change hunk = edits;
        int caretLine = textArea1.getCaretLine();
        for (; hunk != null; hunk = hunk.next) {
            if (hunk.first1 < caretLine) {
                // hunk starts before the caret line.  If caret line is in hunk,
                // go to start of current hunk.  If caret line is after end of
                // current hunk, but before current hunk, go to start of current
                // hunk.
                // caret is in current hunk ||
                // caret is after last hunk ||
                // caret is before next hunk
                if (hunk.first1 + hunk.lines1 > caretLine || hunk.next == null || hunk.next.first1 >= caretLine) {  // NOPMD
                    int line = hunk.first1;                    // first line of hunk

                    // move caret to start of diff hunk
                    int caret_position = textArea1.getLineStartOffset(line);
                    textArea1.setCaretPosition(caret_position, false);

                    // scroll so line is visible
                    int visibleLines = textArea1.getVisibleLines();
                    int rightLineCount = textArea1.getLineCount();
                    int distance = 1;
                    if (line > rightLineCount - visibleLines) {
                        textArea1.setFirstLine(rightLineCount - visibleLines);
                        distance = line - (rightLineCount - visibleLines);
                    } else {
                        textArea1.setFirstLine(line - 1);
                    }

                    // move caret in other text area to start of diff hunk
                    // and scroll to it
                    caret_position = textArea0.getLineStartOffset(hunk.first0);
                    textArea0.setCaretPosition(caret_position, false);
                    textArea0.setFirstLine(hunk.first0 - distance);

                    // maybe move the caret to the first actual diff character
                    if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
                        DualDiffUtil.centerOnDiff(textArea0, textArea1);

                        // maybe select the first diff word
                        if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                            textArea0.selectWord();
                            textArea1.selectWord();
                        }
                    }

                    if (textArea1.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
                        textArea1.getToolkit().beep();
                    }
                    return ;
                }
            }
        }

        if (jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea1.getToolkit().beep();
        }
    }
    
    protected void firstDiff0() {
        Diff.Change hunk = edits;
        int line = hunk.first0;
        
        // move caret to start of diff hunk
        int caret_position = textArea0.getLineStartOffset(line);
        textArea0.setCaretPosition(caret_position, false);
        
        // scroll so line is visible
        int visibleLines = textArea0.getVisibleLines();
        int leftLineCount = textArea0.getLineCount();
        int distance = 1;
        if (line > leftLineCount - visibleLines) {
            textArea0.setFirstLine(leftLineCount - visibleLines);
            distance = line - (leftLineCount - visibleLines);
        } else {
            textArea0.setFirstLine(line - 1);
        }

        // move caret in other text area to start of diff hunk
        // and scroll to it
        caret_position = textArea1.getLineStartOffset(hunk.first1);
        textArea1.setCaretPosition(caret_position, false);
        textArea1.setFirstLine(hunk.first1 - distance);

        // maybe move the caret to the first actual diff character
        if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
            DualDiffUtil.centerOnDiff(textArea0, textArea1);

            // maybe select the first diff word
            if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                textArea0.selectWord();
                textArea1.selectWord();
            }
        }

        if (textArea0.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea0.getToolkit().beep();
        }
    }
    
    protected void firstDiff1() {
        Diff.Change hunk = edits;
        int line = hunk.first1;
        
        // move caret to start of diff hunk
        int caret_position = textArea1.getLineStartOffset(line);
        textArea1.setCaretPosition(caret_position, false);
        
        // scroll so line is visible
        int visibleLines = textArea1.getVisibleLines();
        int leftLineCount = textArea1.getLineCount();
        int distance = 1;
        if (line > leftLineCount - visibleLines) {
            textArea1.setFirstLine(leftLineCount - visibleLines);
            distance = line - (leftLineCount - visibleLines);
        } else {
            textArea1.setFirstLine(line - 1);
        }

        // move caret in other text area to start of diff hunk
        // and scroll to it
        caret_position = textArea0.getLineStartOffset(hunk.first0);
        textArea0.setCaretPosition(caret_position, false);
        textArea0.setFirstLine(hunk.first0 - distance);

        // maybe move the caret to the first actual diff character
        if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
            DualDiffUtil.centerOnDiff(textArea0, textArea1);

            // maybe select the first diff word
            if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                textArea0.selectWord();
                textArea1.selectWord();
            }
        }

        if (textArea1.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea1.getToolkit().beep();
        }
    }
    
    protected void lastDiff0() {
        Diff.Change hunk = edits;
        while(hunk.next != null) {
            hunk = hunk.next;   
        }
        
        int line = hunk.first0;
        
        // move caret to start of diff hunk
        int caret_position = textArea0.getLineStartOffset(line);
        textArea0.setCaretPosition(caret_position, false);
        
        // scroll so line is visible
        int visibleLines = textArea0.getVisibleLines();
        int leftLineCount = textArea0.getLineCount();
        int distance = 1;
        if (line > leftLineCount - visibleLines) {
            textArea0.setFirstLine(leftLineCount - visibleLines);
            distance = line - (leftLineCount - visibleLines);
        } else {
            textArea0.setFirstLine(line - 1);
        }

        // move caret in other text area to start of diff hunk
        // and scroll to it
        caret_position = textArea1.getLineStartOffset(hunk.first1);
        textArea1.setCaretPosition(caret_position, false);
        textArea1.setFirstLine(hunk.first1 - distance);

        // maybe move the caret to the first actual diff character
        if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
            DualDiffUtil.centerOnDiff(textArea0, textArea1);

            // maybe select the first diff word
            if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                textArea0.selectWord();
                textArea1.selectWord();
            }
        }

        if (textArea0.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea0.getToolkit().beep();
        }
    }
    
    protected void lastDiff1() {
        Diff.Change hunk = edits;
        while(hunk.next != null) {
            hunk = hunk.next;   
        }
        
        int line = hunk.first1;
        
        // move caret to start of diff hunk
        int caret_position = textArea1.getLineStartOffset(line);
        textArea1.setCaretPosition(caret_position, false);
        
        // scroll so line is visible
        int visibleLines = textArea1.getVisibleLines();
        int leftLineCount = textArea1.getLineCount();
        int distance = 1;
        if (line > leftLineCount - visibleLines) {
            textArea1.setFirstLine(leftLineCount - visibleLines);
            distance = line - (leftLineCount - visibleLines);
        } else {
            textArea1.setFirstLine(line - 1);
        }

        // move caret in other text area to start of diff hunk
        // and scroll to it
        caret_position = textArea0.getLineStartOffset(hunk.first0);
        textArea0.setCaretPosition(caret_position, false);
        textArea0.setFirstLine(hunk.first0 - distance);

        // maybe move the caret to the first actual diff character
        if (jEdit.getBooleanProperty(DualDiffManager.HORIZ_SCROLL)) {
            DualDiffUtil.centerOnDiff(textArea0, textArea1);

            // maybe select the first diff word
            if (jEdit.getBooleanProperty(DualDiffManager.SELECT_WORD)) {
                textArea0.selectWord();
                textArea1.selectWord();
            }
        }

        if (textArea1.getFirstLine() != line && jEdit.getBooleanProperty(DualDiffManager.BEEP_ON_ERROR)) {
            textArea1.getToolkit().beep();
        }
    }
    
    /**
     * Using the given EditPane as a basis, moves the hunk corresponding to the
     * caret line of the given EditPane from the left text area to the right
     * text area.
     */
    protected void moveRight(EditPane editPane) {
        if (editPane == null) {
            return ;
        }
        if (editPane.equals(editPane0)) {
            diffOverview0.moveRight(editPane.getTextArea().getCaretLine());
        } else {
            // want to move right but have right EditPane.  Need to find
            // corresponding hunk from left EditPane and use first line of hunk.
            Diff.Change hunk = edits;
            int caretLine = editPane.getTextArea().getCaretLine();
            for (; hunk != null; hunk = hunk.next) {
                if (hunk.first1 <= caretLine && caretLine <= hunk.last1) {
                    diffOverview0.moveRight(hunk.first0);
                    return ;
                }
            }
        }
    }

    /**
     * Using the given EditPane as a basis, moves the hunk corresponding to the
     * caret line of the given EditPane from the right text area to the left
     * text area.
     */
    protected void moveLeft(EditPane editPane) {
        if (editPane == null) {
            return ;
        }
        if (editPane.equals(editPane1)) {
            diffOverview0.moveLeft(editPane.getTextArea().getCaretLine());
        } else {
            // want to move left but have left EditPane.  Need to find
            // corresponding hunk from right EditPane and use first line of hunk.
            Diff.Change hunk = edits;
            int caretLine = editPane.getTextArea().getCaretLine();
            for (; hunk != null; hunk = hunk.next) {
                if (hunk.first0 <= caretLine && caretLine <= hunk.last0) {
                    diffOverview0.moveLeft(hunk.first1);
                    return ;
                }
            }
        }
    }

    /**
     * Move all non-conflicting diff hunks from the left text are to the right text area.
     */
    protected void moveMultipleRight(EditPane editPane) {
        if (editPane == null) {
            return ;
        }
        // want to move left but have left EditPane.  Need to find
        // corresponding hunk from right EditPane and use first line of hunk.
        // Start with the last hunk and work backwards to the first, this ensures
        // line numbers for inserts of previous hunks remain valid.
        Diff.Change hunk = edits;
        for (; hunk.next != null; hunk = hunk.next) ;        // go to last hunk
        for (; hunk != null; hunk = hunk.prev) {
            if (hunk.lines1 == 0) {
                diffOverview0.moveRight(hunk.first0);
            }
        }
    }

    /**
     * Move all non-conflicting diff hunks from the right text area to the left text area.
     */
    protected void moveMultipleLeft(EditPane editPane) {
        if (editPane == null) {
            return ;
        }
        // want to move left but have left EditPane.  Need to find
        // corresponding hunk from right EditPane and use first line of hunk.
        // Start with the last hunk and work backwards to the first, this ensures
        // line numbers for inserts of previous hunks remain valid.
        Diff.Change hunk = edits;
        for (; hunk.next != null; hunk = hunk.next) ;        // go to last hunk
        for (; hunk != null; hunk = hunk.prev) {
            if (hunk.lines0 == 0) {
                diffOverview0.moveLeft(hunk.first1);
            }
        }
    }
}