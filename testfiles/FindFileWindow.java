/*
* OpenIt jEdit Plugin (FindFileWindow.java)
*
* Copyright (C) 2003 Matt Etheridge (matt@etheridge.org)
* Copyright (C) 2006 Denis Koryavov
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
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

package org.etheridge.openit.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import org.etheridge.openit.*;
import org.etheridge.openit.sourcepath.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.util.*;

/**
* Popup dialog that allows users to search for files.
*/
public class FindFileWindow extends JDialog {
        // source file list GUI components
        private JWindow mSourceFileListWindow;
        private JList mSourceFileList;
        private SourceFileListModel mSourceFileListModel;
        private JScrollPane mScrollPane;
        private ArrayList mFileSelectionListeners;

        // source file name and filter GUI components
        private JTextField mSourceFileNameField = new JTextField();
        private FilterPanel mFilterPanel;
        private SourceFileFilter mSourceFileFilter;
        private boolean useRegexp = false;


        // Loading label
        private JLabel mLoadingLabel;

        //{{{ showWindow method.
        public void showWindow() {
                mSourceFileNameField.requestFocus();
                updateList(mSourceFileNameField.getText());
                setVisible(true);
        } //}}}

        //{{{ constructor.
        public FindFileWindow() {
                // super();
                super(jEdit.getActiveView(), false);
                init();
        } //}}}

        // {{{ Import Selection Listener Methods
        public void addFileSelectionListener(FileSelectionListener listener) {
                if (mFileSelectionListeners == null) {
                        mFileSelectionListeners = new ArrayList();
                }
                mFileSelectionListeners.add(listener);
        }

        public void removeFileSelectionListener(FileSelectionListener listener) {
                if (mFileSelectionListeners != null) {
                        mFileSelectionListeners.remove(listener);
                }
        }
        // }}}

        //{{{ clearSourceFiles method.
        /**
        * Clear any source files currently in the list.
        */
        public void clearSourceFiles() {
                // if we are loading, ignore this request (as we do not want to remove
                        // the loading label!)
                if (SourcePathManager.staticGetQuickAccessSourcePath() != null) {
                        mSourceFileNameField.setText("");
                }
        } //}}}

        //{{{ selectFile method.
        public void selectFile(String fileName) {
                if (fileName != null) {
                        mSourceFileNameField.setText(fileName);
                }
        } //}}}

        //{{{ setSourceFiles method.
        private void setSourceFiles(ArrayList sourceFiles) {
                mSourceFileListModel.refreshModel(sourceFiles);
                mSourceFileList.setSelectedIndex(0);

                if (sourceFiles.isEmpty()) {
                        mSourceFileListWindow.setVisible(false);
			if(!OperatingSystem.isMacOS()) // There is no focus issue on mac
			{
				mSourceFileNameField.requestFocus();
			}
                        mSourceFileNameField.setForeground(Color.RED);
                } else {
                        mSourceFileNameField.setForeground(jEdit.getColorProperty("view.fgColor", Color.BLACK));
                        mSourceFileList.setVisibleRowCount(Math.min(sourceFiles.size(), 8));
                        mSourceFileListWindow.pack();
                        showFileListWindow();
                }

                // mSourceFileList.invalidate();
                // mScrollPane.revalidate();
                // mScrollPane.repaint();
        }
        //}}}

        //{{{ showFileListWindow method.
        private void showFileListWindow() {
                Rectangle findFileWindowBounds = getBounds();
                int x = (int) findFileWindowBounds.getX();
                int y = (int) (findFileWindowBounds.getY() + findFileWindowBounds.getHeight());
                mSourceFileListWindow.setLocation(new Point(x, y));
                mSourceFileListWindow.setVisible(true);
        }
        //}}}

        // {{{ init method.
        /**
         * Initialise the GUI components
         */
        private void init() {
                getContentPane().setLayout(new BorderLayout());
                setUndecorated(true);
		mSourceFileNameField.setBackground(jEdit.getColorProperty("view.bgColor", Color.white));

                mSourceFileListModel = new SourceFileListModel();
                mSourceFileList = new JList(mSourceFileListModel);
                mSourceFileList.setBorder(BorderFactory.createEtchedBorder());

                addEscapeKeyStroke(mSourceFileList);
                KeyStroke enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
                mSourceFileList.getInputMap().put(enterKeyStroke, enterKeyStroke);
                mSourceFileList.getActionMap().put(enterKeyStroke, new AbstractAction() {
                                public void actionPerformed(ActionEvent ae) {
                                        selectionMade();
                                }
                });

                mSourceFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                mSourceFileList.addMouseListener(new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {
                                        if (e.getClickCount() >= 2) {
                                                selectionMade();
                                        }
                                }
                });

                mSourceFileList.setCellRenderer(new SourceFileListCellRenderer());

                if (mSourceFileFilter == null) {
                        mSourceFileFilter = new SourceFileFilter();
                }

                // add teh source file list in a new window
                mScrollPane = new JScrollPane(mSourceFileList);
                mSourceFileListWindow = new JWindow(this);
                mSourceFileListWindow.getContentPane().setLayout(new BorderLayout());
                mSourceFileListWindow.getContentPane().add(mScrollPane);
                mSourceFileListWindow.pack();




                if (SourcePathManager.staticGetQuickAccessSourcePath() == null) {
                        createLoaderThread();
                }

                mSourceFileList.addKeyListener(new KeyAdapterProxy(mSourceFileNameField){
                                protected boolean shouldForwardEvent(KeyEvent e)
                                {
                                        return !isKeyHandledBySourceFileList(e.getKeyCode());
                                }
                });

                //{{{ mSourceFileList.addKeyListener
                mSourceFileList.addKeyListener(new KeyAdapter() {
                                private boolean mDownAtEndPressed = false;
                                private boolean mUpPressedAtBeginning = false;

                                public void keyPressed(KeyEvent e)
                                {
                                        handleEvent(e);
                                }

                                public void keyReleased(KeyEvent e)
                                {
                                        handleEvent(e);
                                }

                                private void handleEvent(KeyEvent e)
                                {
                                        if (downPressedAtEnd(e)) {
                                                if (mDownAtEndPressed) {
                                                        mSourceFileList.setSelectedIndex(0);
                                                        mSourceFileList.ensureIndexIsVisible(0);
                                                        mDownAtEndPressed = false;
                                                        e.consume();
                                                } else {
                                                        mDownAtEndPressed = true;
                                                }
                                                mUpPressedAtBeginning = false;
                                        } else if (upPressedAtBeginning(e)) {
                                                if (mUpPressedAtBeginning) {
                                                        mSourceFileList.setSelectedIndex(mSourceFileList.getModel().getSize()-1);
                                                        mSourceFileList.ensureIndexIsVisible(mSourceFileList.getModel().getSize()-1);
                                                        mUpPressedAtBeginning = false;
                                                        e.consume();
                                                } else {
                                                        mUpPressedAtBeginning = true;
                                                }
                                                mDownAtEndPressed = false;
                                        } else {
                                                // neither up or down was pressed at beginnig or end
                                                mUpPressedAtBeginning = false;
                                                mDownAtEndPressed = false;
                                        }
                                }

                                private boolean upPressedAtBeginning(KeyEvent e)
                                {
                                        return (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_PAGE_UP)
                                        && (mSourceFileList.getSelectedIndex() == 0);
                                }

                                private boolean downPressedAtEnd(KeyEvent e)
                                {
                                        return (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
                                        && (mSourceFileList.getSelectedIndex() == mSourceFileList.getModel().getSize()-1);
                                }
                }); //}}}

                // certain key events (up,down,enter) should be dispatched onto the
                // class list for movement and selecting
                getContentPane().add(createClassNamePanel(), BorderLayout.NORTH);
                mSourceFileNameField.addKeyListener(new KeyAdapterProxy(mSourceFileList) {
                                protected boolean shouldForwardEvent(KeyEvent e) {
                                        return isKeyHandledBySourceFileList(e.getKeyCode());
                                }
                });

                pack();
        }
        // }}}

        //{{{ createClassNamePanel method.
        /**
        * Creates a text field for users to enter class names in
        */
        private JPanel createClassNamePanel() {
                JPanel classNamePanel = new JPanel(new GridLayout(2,1));
                classNamePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.lightGray),
                        BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.lightGray, Color.darkGray)));

                JPanel labelAndFilterPanel = new JPanel(new BorderLayout());

                JLabel instructionLabel = new JLabel(jEdit.getProperty("openit.FindFileWindow.Instruction.label"));
                instructionLabel.setFont(new Font("dialog", Font.BOLD, 12));
                instructionLabel.setForeground(Color.black);
                instructionLabel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

                mSourceFileNameField.setColumns(35);

                labelAndFilterPanel.add(instructionLabel, BorderLayout.WEST);
                labelAndFilterPanel.add(mFilterPanel = new FilterPanel(), BorderLayout.EAST);

                classNamePanel.add(labelAndFilterPanel);
                classNamePanel.add(mSourceFileNameField);

                // listen for changes to the document (the text) in the text field, and
                // reload the class list accordingly.
                //{{{ DocumentListener methods.
                mSourceFileNameField.getDocument().addDocumentListener(new DocumentListener() {
                                public void changedUpdate(DocumentEvent e) {
                                        updateSourceFileList(e);
                                }

                                public void insertUpdate(DocumentEvent e) {
                                        updateSourceFileList(e);
                                }

                                public void removeUpdate(DocumentEvent e) {
                                        updateSourceFileList(e);
                                }

                                private void updateSourceFileList(DocumentEvent e) {
                                        String documentText = null;
                                        try {
                                                documentText = e.getDocument().getText(0, e.getDocument().getLength());
                                        } catch (BadLocationException ble) {
                                                Log.log(Log.MESSAGE, FindFileWindow.class,
                                                        "[OpenIt Plugin]: BadLocationException caught!");
                                                return;
                                        }

                                        updateList(documentText);
					// There is no focus issue on mac
					if(!OperatingSystem.isMacOS())
					{
						// fix for focus problems under windows (and perhaps other os)
						SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									mSourceFileNameField.requestFocus();
								}
						});
					}
                                }
                });
                // }}}

                // escape listener (to close the window when escape is pressed).
                addEscapeKeyStroke(mSourceFileNameField);
                return classNamePanel;
        }
        //}}}

        // {{{ updateList method.
        private void updateList(String documentText) {
                if (documentText.length() == 0) {
                        setSourceFiles(new ArrayList()); // empty list
                        return;
                }

                char startingChar = documentText.charAt(0);
                QuickAccessSourcePath quickAccessSourcePath =
                SourcePathManager.staticGetQuickAccessSourcePath();

                // if the QuickAccessSourcePath instance is null (it can be null
                        // if the initial creation thread has not finished yet), then just
                // return as we cannot do anything yet.
                if (quickAccessSourcePath == null) {
                        return;
                }

                ArrayList sourceFilesStartingWithLetter = null;

                if (useRegexp) {
                        try {
                                mSourceFileFilter.setRegularExpressionString(mSourceFileNameField.getText());
                        } catch (Exception e) {
                                mSourceFileFilter.clearRegularExpression();

                                Log.log(Log.MESSAGE, FindFileWindow.class,
                                        "[OpenIt Plugin]: Invalid regular expression: " + mSourceFileNameField.getText());
                        }

                        sourceFilesStartingWithLetter =
                        new ArrayList(quickAccessSourcePath.getAllFiles());
                        mSourceFileFilter.filter(sourceFilesStartingWithLetter);
                } else {
                        sourceFilesStartingWithLetter =
                        new ArrayList(quickAccessSourcePath.getSourceFilesStartingWith(startingChar));


                        // iterate through list and remove those source files that do not
                        // start with the text in the source file name field
                        for (Iterator i = sourceFilesStartingWithLetter.iterator(); i.hasNext();) {
                                SourcePathFile currentSourcePathFile = (SourcePathFile) i.next();
                                String currentFileName = currentSourcePathFile.getFullName();

                                // if the match is *not* case sensitive then make the file name and
                                // document text (to compare with) lowercase
                                if (!jEdit.getBooleanProperty(OpenItProperties.POP_UP_CASE_SENSITIVE_FILE_MATCHING, false)) {
                                        currentFileName = currentFileName.toLowerCase();
                                        documentText = documentText.toLowerCase();
                                }

                                if (!currentFileName.startsWith(documentText)) {
                                        i.remove();
                                }
                        }
                }
                setSourceFiles(sourceFilesStartingWithLetter);
        }
        // }}}

        //{{{ isKeyHandledBySourceFileList method.
        /**
        * @return whether or not the specified keycode is one that should be
        * handled by the class list
        */
        private boolean isKeyHandledBySourceFileList(int keyCode) {
                return (keyCode == KeyEvent.VK_UP ||
                        keyCode == KeyEvent.VK_DOWN ||
                        keyCode == KeyEvent.VK_ENTER ||
                        keyCode == KeyEvent.VK_PAGE_UP ||
                        keyCode == KeyEvent.VK_PAGE_DOWN);
        }
        //}}}

        //{{{ notifyFileSelectionListeners method.
        /**
        * Notify any import selection listeners that an import statement was
        * selected.
        */
        private void notifyFileSelectionListeners(SourcePathFile sourceFile) {
                if (mFileSelectionListeners != null) {
                        for (Iterator i = mFileSelectionListeners.iterator(); i.hasNext();) {
                                FileSelectionListener listener = (FileSelectionListener) i.next();
                                listener.fileSelected(sourceFile);
                        }
                }
        }
        //}}}

        //{{{ selectionMade method.
        private void selectionMade() {
                if (!mSourceFileListModel.isEmpty()) {
                        notifyFileSelectionListeners((SourcePathFile)mSourceFileList.getSelectedValue());
                        closeWindow();
                }
        } //}}}

        //{{{ closeWindow method.
        private void closeWindow() {
                mSourceFileListWindow.setVisible(false);
                setVisible(false);
        } //}}}

        //{{{ createLoaderThread method.
        private void createLoaderThread() {
                // set the loading text on the file name field
                mSourceFileNameField.setText(jEdit.getProperty("openit.FindFileWindow.InitialLoadingMessage.label"));

                // create and start the thread
                InitialLoadingThread loaderThread = new InitialLoadingThread();
                loaderThread.start();
        } //}}}

        //{{{ escape listener (to close the window when escape is pressed).
        private void addEscapeKeyStroke(JComponent component) {
                KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                component.getInputMap().put(ks, ks);
                component.getActionMap().put(ks, new AbstractAction() {
                                public void actionPerformed(ActionEvent ae) {
                                        closeWindow();
                                }
                });
        } //}}}

        // {{{ FilterPanel class
        /**
         * Small panel for entering a regular expression string
         */
        public class FilterPanel extends JPanel {
                private JCheckBox mFilterCheckBox;

                //{{{ constructor.
                public FilterPanel() {
                        setLayout(new BorderLayout(6,6));
                        mFilterCheckBox = new JCheckBox(jEdit.getProperty("openit.FilterWindow.FilterCheckbox.label"));
                        mFilterCheckBox.setMnemonic('F');
                        mFilterCheckBox.addItemListener(new ItemListener() {
                                        public void itemStateChanged(ItemEvent e) {
                                                useRegexp = (e.getStateChange() == ItemEvent.SELECTED);
                                                jEdit.setBooleanProperty(OpenItProperties.POP_UP_FILTER_APPLIED,
                                                        (e.getStateChange() == ItemEvent.SELECTED));
                                                updateList(mSourceFileNameField.getText());
						if(!OperatingSystem.isMacOS())
						{
							SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										mSourceFileNameField.requestFocus();
									}
							});
						}
                                        }
                        });

                        mFilterCheckBox.setSelected(
                                jEdit.getBooleanProperty(OpenItProperties.POP_UP_FILTER_APPLIED, false));
                        addEscapeKeyStroke(mFilterCheckBox);
                        add(mFilterCheckBox, BorderLayout.WEST);
                }
                //}}}
        }
        //}}}

        //{{{ InitialLoadingThread class
        /**
        * Keeps checking to see whether the QuickAccessSourcePath is available, and
        * when it is, it updates clears the text on the file name field.
        * NOTE: could use a SwingWorker here
        */
        public class InitialLoadingThread extends Thread
        {
                public void run()
                {
                        // periodicially check to see whether the QuickAccessSourcePath is
                        // available yet.
                        while (SourcePathManager.staticGetQuickAccessSourcePath() == null) {
                                try {
                                        Thread.sleep(300);
                                } catch (Exception e) {
                                }
                        }

                        // clear the text in the text field to notify user that the file list
                        // has loaded.
                        SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                                mSourceFileNameField.setText("");
                                        }
                        });
                }
        }
        //}}}

        // :collapseFolds=1:tabSize=8:indentSize=8:folding=explicit:
}
