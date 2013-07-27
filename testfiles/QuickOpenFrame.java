/*
 * @(#)QuickOpenFrame.java   2011.01.15 at 11:07:34 PST
 *
 * Copyright 2011 Brian Schlining
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package hohonuuli.jedit.quickopen;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.util.Log;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


/**
 * @author Brian Schlining
 * @since 2011-01-03
 */
public class QuickOpenFrame extends JFrame {

    private static final String downKey = "selectNextRow";
    private static final String upKey = "selectPreviousRow";
    private static KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
    private static KeyStroke kpUp = KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0);
    private static KeyStroke kpDown = KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0);
    private static KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
    private static KeyStroke cmdW = KeyStroke.getKeyStroke(KeyEvent.VK_W,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
    private JLabel label = new JLabel("");    // Displays full path at bottom of window
    private final QuickOpenFrameController controller;
    private volatile FileList fileList;
    private JList list;
    private JScrollPane scrollPane;
    private JTextField textField;
    private final View view;

    /**
     * Constructs ...
     *
     * @param view
     *
     * @throws HeadlessException
     */
    public QuickOpenFrame(View view) throws HeadlessException {
        Log.log(Log.MESSAGE, this, "Creating a new QuickOpenFrame");
        this.view = view;
        controller = new QuickOpenFrameController(this);
        initialize();
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                Log.log(Log.DEBUG, this, "Got focus");
                updateFileList();
            }
        });
    }

    /**
     * @return
     */
    public QuickOpenFrameController getController() {
        return controller;
    }

    /**
     * @return
     */
    public FileList getFileList() {
        return fileList;
    }

    /**
     * List of files to be quickopened. The JList is backed by a
     * DefaultListModel of Files.
     * @return
     */
    private JList getList() {
        if (list == null) {
            list = new JList();
            list.setModel(new DefaultListModel());
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new FilenameRenderer());
            list.setVisibleRowCount(17);

            // Set label at bottom with full path of file
            list.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        Object[] fileObjects = list.getSelectedValues();
                        File[] files = new File[fileObjects.length];

                        for (int i = 0; i < fileObjects.length; i++) {
                            files[i] = (File) fileObjects[i];
                        }

                        if (files.length == 1) {
                            try {
                                String path = files[0].getCanonicalPath();

                                label.setText(path);
                                label.setToolTipText(path);
                            }
                            catch (IOException e1) {
                                label.setText("");
                            }
                        }
                        else {
                            label.setText("");
                        }
                    }
                }

            });

            // When enter is pressed open the selected file
            list.addKeyListener(new OpenKeyListener());

            // Open a file on a mouse click
            list.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {

                    // Look for a double click
                    if (e.getClickCount() == 2) {
                        Object[] selectedValues = list.getSelectedValues();

                        if (selectedValues.length == 1) {
                            controller.openFile((File) selectedValues[0]);
                        }
                    }
                }

            });

        }

        return list;
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane(getList(), JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        return scrollPane;
    }

    private JTextField getTextField() {
        if (textField == null) {
            textField = new JTextField();
            textField.setColumns(35);
            textField.addKeyListener(new OpenKeyListener());
            // The next 2 lines add a prompt to the text field on Macs
            textField.putClientProperty("JTextField.variant", "search");
            textField.putClientProperty("JTextField.Search.Prompt", "Search");
            textField.getDocument().addDocumentListener(new SearchDocumentListener());

            // --- Configure arrow keys
            InputMap inputMap = textField.getInputMap(JComponent.WHEN_FOCUSED);
            ActionMap actionMap = textField.getActionMap();

            // Remove some existing key bindings, they get in our way
            inputMap.put(cmdW, "none");    // This would eat the close window keys stroke

            // Actions to bind up and down arrows to
            Action upAction = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    JList theJList = getList();
                    int idx = theJList.getSelectedIndex();
                    if (idx >= 0) {
                        if (idx == 0 && theJList.getModel().getSize() > 0) {
                            idx = theJList.getModel().getSize() - 1;
                        }
                        else {
                            idx = idx - 1;
                        }
                        theJList.setSelectedIndex(idx);
                        theJList.ensureIndexIsVisible(idx);
                        theJList.repaint();
                    }
                }
            };
            Action downAction = new AbstractAction() {

                public void actionPerformed(ActionEvent e) {
                    JList theJList = getList();
                    int idx = theJList.getSelectedIndex();
                    if (idx >= 0) {
                        if (idx < theJList.getModel().getSize() - 1) {
                            idx = idx + 1;
                        }
                        else {
                            idx = 0; // Wrap around
                        }
                        theJList.setSelectedIndex(idx);
                        theJList.ensureIndexIsVisible(idx);
                        theJList.repaint();
                    }
                }
            };

            inputMap.put(up, upKey);
            inputMap.put(kpUp, upKey);
            inputMap.put(down, downKey);
            inputMap.put(kpDown, downKey);
            actionMap.put(downKey, downAction);
            actionMap.put(upKey, upAction);
        }

        return textField;
    }

    /**
     * @return
     */
    public View getView() {
        return view;
    }

    private void initialize() {

        setLayout(new BorderLayout());
        add(getTextField(), BorderLayout.NORTH);
        add(getScrollPane(), BorderLayout.CENTER);
        add(label, BorderLayout.SOUTH);

        // --- Map C+W to hide the window
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = getRootPane().getActionMap();
        String hideKey = "hideTheWindow";
        Action hideAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                QuickOpenFrame.this.setVisible(false);
            }
        };

        inputMap.put(cmdW, hideKey);
        actionMap.put(hideKey, hideAction);

        // --- Window should disappear if it loses focus.
        addWindowFocusListener(new WindowFocusListener() {

            public void windowGainedFocus(WindowEvent e) {

                // Select all text and request focus
                JTextField tf = getTextField();
                String text = tf.getText();
                tf.setSelectionStart(0);
                tf.setSelectionEnd(text.length());
                tf.requestFocus();

            }

            public void windowLostFocus(WindowEvent e) {
                getTextField().setText("");
                setVisible(false);
            }
        });

    }

    private void setEnableUI(boolean enabled) {
        getTextField().setEnabled(enabled);
        getList().setEnabled(enabled);
        getTextField().requestFocus();
    }

    /**
     */
    public void updateFileList() {
        setEnableUI(false);

        final File directory = controller.getDirectory();

        if (directory != null) {

            setTitle(directory.getAbsolutePath());

            SwingWorker swingWorker = new SwingWorker<FileList, Object>() {

                @Override
                protected FileList doInBackground() throws Exception {
                    return new FileList(directory);
                }

                @Override
                protected void done() {
                    try {
                        fileList = get();
                        ((DefaultListModel) getList().getModel()).clear();
                        updateFiles(fileList.getFiles());
                    }
                    catch (Exception e) {
                        Log.log(Log.ERROR, "Failed to create FileList", e);
                    }

                    setEnableUI(true);
                }
            };

            swingWorker.execute();
        }
        else {
            GUIUtilities.message(view, "quickopen.message.noDirectory", null);
        }

    }

    /**
     * This method redraws the JList. It's why the UI momentarily hangs when loading
     * large lists. I don't know how to work around this as it needs to be called on the
     * swing EDT.
     * @param files
     */
    private void updateFiles(java.util.List<File> files) {

        DefaultListModel model = (DefaultListModel) getList().getModel();

        model.clear();

        for (File file : files) {
            model.addElement(file);
        }

        if (files.size() > 0) {
            getList().setSelectedIndex(0);
        }
    }

    private class FilenameRenderer extends DefaultListCellRenderer {

        /**
         *
         * @param list
         * @param value
         * @param index
         * @param isSelected
         * @param cellHasFocus
         * @return
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            File file = (File) value;

            setText(file.getName());
            setToolTipText(file.getAbsolutePath());

            return this;
        }
    }


    private class OpenKeyListener extends KeyAdapter {

        /**
         *
         * @param e
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                Object[] selectedValues = getList().getSelectedValues();

                if (selectedValues.length == 1) {
                    controller.openFile((File) selectedValues[0]);
                }
            }
        }
    }


    /**
     * Performs searches as a user types in a the text field. Searches are performed in a
     * background thread
     */
    private class SearchDocumentListener implements DocumentListener {

        private volatile Thread currentThread;
        private volatile SwingWorker swingWorker;

        /**
         *
         * @param e
         */
        public void changedUpdate(DocumentEvent e) {
            search();
        }

        /**
         *
         * @param e
         */
        public void insertUpdate(DocumentEvent e) {
            search();
        }

        /**
         *
         * @param e
         */
        public void removeUpdate(DocumentEvent e) {
            search();
        }

        private void search() {
            
            if (swingWorker != null && !swingWorker.isDone()) {
                swingWorker.cancel(true); 
            }
            
            
            swingWorker = new SwingWorker<List<File>, Object>() {
                @Override
                protected List<File> doInBackground() throws Exception {
                    return  fileList.search(textField.getText());
                }

                @Override
                protected void done() {
                    try {
                        List<File> files = get();
                        updateFiles(get());
                    } catch (Exception e) {
                        Log.log(Log.MESSAGE, "Search did not execute", e);
                    }
                }
            };
            swingWorker.execute();
            
        }
    }
}
