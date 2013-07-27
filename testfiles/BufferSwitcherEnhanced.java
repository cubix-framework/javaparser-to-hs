package treebufferswitcher;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.bufferset.BufferSetListener;
import org.gjt.sp.jedit.bufferset.BufferSet;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.util.Log;

import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;

import treebufferswitcher.model.BufferSwitcherModel;
import treebufferswitcher.model.BufferItem;
import treebufferswitcher.model.PathItem;

import java.awt.event.*;
import java.awt.*;

// BUG: when holding "delete", some of keypressed are occasionally passed to textarea causing buffer modification
public class BufferSwitcherEnhanced extends JComboBox implements EBComponent, BufferSetListener {

    private final TreeBufferSwitcherPlugin plugin;
    private final EditPane editPane;
    private BufferSwitcherModel model;
    private BufferSet bufferSet;
    private int adjustingDirection = 1;
    private boolean updating = false;

    public BufferSwitcherEnhanced(TreeBufferSwitcherPlugin plugin, EditPane editPane) {
        this.plugin = plugin;
        this.editPane = editPane;
        Component textArea = editPane.getTextArea();
        Container container = textArea.getParent();
        container.add(this, BorderLayout.NORTH);
        setRenderer(new CellRendererImpl());
        addActionListener(new ActionListenerImpl());
        addFocusListener(new FocusListenerImpl());
        addPopupMenuListener(new PopupMenuListenerImpl());
        EditBus.addToBus(this);
        bufferSet = editPane.getBufferSet();
        bufferSet.addBufferSetListener(this);
        propertiesChanged();
        dataChanged();
        container.validate();
    }

    public void propertiesChanged() {
        setMaximumRowCount(plugin.getVisibleRowCount());
        dataChanged();
    }

    public void dataChanged() {
        // if the buffer count becomes 0, then it is guaranteed to
        // become 1 very soon, so don't do anything in that case.
        if (editPane.getBufferSet().size() == 0) {
            return;
        }
        model = plugin.modelBuilder.createModel(editPane.getBufferSet().getAllBuffers());
        setModel(model);
        setSelectedItem(model.itemByBuffer(editPane.getBuffer()));
    }

    public void dispose() {
        bufferSet.removeBufferSetListener(this);
        editPane.getBufferSet().removeBufferSetListener(this);
        EditBus.removeFromBus(this);
        Container container = getParent();
        container.remove(this);
        container.validate();
    }

    @Override
    public void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED) {
            // if shortcut to buffer is pressed, switch to buffer
            if (plugin.isUseShortcuts() && isPopupVisible() && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                int keyIndex = plugin.getShortcutsUppercased().indexOf(e.getKeyChar());
                if (keyIndex == -1) {
                    keyIndex = plugin.getShortcutsLowercased().indexOf(e.getKeyChar());
                }
                BufferSwitcherModel model = (BufferSwitcherModel)getModel();
                if (keyIndex != -1 && keyIndex < model.buffersCount) {
                    BufferItem item = model.itemByKeyIndex(keyIndex);
                    if (item != null) {
                        setSelectedItem(item);
                        editPane.setBuffer(item.buffer);
                        hidePopup();
                        e.consume();
                        return;
                    }
                }
            }
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_PAGE_UP:
                    adjustingDirection = -1;
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_PAGE_DOWN:
                    adjustingDirection = 1;
                    break;
                case KeyEvent.VK_DELETE:
                    if (getSelectedItem() instanceof BufferItem) {
                        BufferItem bufferItem = (BufferItem)getSelectedItem();
                        Log.log(Log.DEBUG, this, "Closing buffer: " + bufferItem.buffer.getPath());
                        e.consume();
                        if (jEdit.closeBuffer(editPane.getView(), bufferItem.buffer)) {
                            //final boolean oldEnabled = editPane.getTextArea().isEnabled();
                            //editPane.getTextArea().setEnabled(false);
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    requestFocusInWindow();
                                    showPopup();
                                    //editPane.getTextArea().setEnabled(oldEnabled);
                                }
                            });
                        }
                    }
                    return;
                case KeyEvent.VK_ESCAPE:
                    hidePopup();
                    e.consume();
                    return;
            }
        }
        super.processKeyEvent(e);
    }

    @Override
    public void setSelectedItem(Object anObject) {
        if (anObject instanceof PathItem) {
            adjustSelectedIndex(model.getIndexOf(anObject), adjustingDirection);
        } else {
            super.setSelectedItem(anObject);
        }
    }

    private void adjustSelectedIndex(int from, int step) {
        for (;;) {
            if (from < 0) {
                adjustSelectedIndex(0, 1);
                return;
            }
            if (from >= model.getSize()) {
                adjustSelectedIndex(model.getSize() - 1, -1);
                return;
            }
            if (model.getElementAt(from) instanceof BufferItem) {
                break;
            }
            from += step;
        }
        setSelectedItem(getItemAt(from));
    }

    public void handleMessage(EBMessage message) {
        if (message instanceof EditPaneUpdate) {
            EditPaneUpdate editPaneUpdate = (EditPaneUpdate)message;
            if (editPaneUpdate.getEditPane() != editPane) {
                return;
            }
            if (editPaneUpdate.getWhat() == EditPaneUpdate.BUFFER_CHANGED) {
                setSelectedItem(model.itemByBuffer(editPane.getBuffer()));
            } else if (editPaneUpdate.getWhat() == EditPaneUpdate.BUFFERSET_CHANGED) {
                bufferSet.removeBufferSetListener(this);
                bufferSet = editPane.getBufferSet();
                bufferSet.addBufferSetListener(this);
                dataChanged();
            }
        } else if (message instanceof BufferUpdate) {
            if (((BufferUpdate)message).getWhat() == BufferUpdate.DIRTY_CHANGED) {
                BufferUpdate bufferUpdate = (BufferUpdate)message;
                BufferItem bufferItem = model.itemByBuffer(bufferUpdate.getBuffer());
                plugin.modelBuilder.updateBuffer(bufferItem);
                model.fireItemChanged(bufferItem);
            }
        }
    }

    public void bufferAdded(Buffer buffer, int index) {
        dataChanged();
    }

    public void bufferRemoved(Buffer buffer, int index) {
        dataChanged();
    }

    public void bufferMoved(Buffer buffer, int oldIndex, int newIndex) {
        dataChanged();
    }

    public void bufferSetSorted() {
        dataChanged();
    }

    private class CellRendererImpl extends DefaultListCellRenderer {

        private int index;
        private int level;

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            this.index = index;
            if (value instanceof BufferItem) {
                BufferItem bufferItem = (BufferItem)value;
                if (plugin.isUseShortcuts() && index != -1 && bufferItem.keyIndex < plugin.getShortcuts().length()) {
                    char key = plugin.getShortcuts().charAt(bufferItem.keyIndex);
                    value = "<html><font color='red'>" + key + "</font> " + bufferItem.title;
                } else {
                    value = bufferItem.title;
                }
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(bufferItem.buffer.getIcon());
                setToolTipText(bufferItem.buffer.getPath());
                level = bufferItem.level;
            } else if (value instanceof PathItem) {
                PathItem pathItem = (PathItem)value;
                super.getListCellRendererComponent(list, pathItem.title, index, false, cellHasFocus);
                level = pathItem.level;
            } else {
                super.getListCellRendererComponent(list, "Bad value: " + value, index, isSelected, cellHasFocus);
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            boolean offset = index != -1 && isPopupVisible() && level != 0;
            if (offset) {
                g.translate(level * plugin.getTreeLevelOffset(), 0);
            }
            super.paintComponent(g);
            if (offset) {
                g.translate(- level * plugin.getTreeLevelOffset(), 0);
            }
        }

    }

    private class FocusListenerImpl extends FocusAdapter {

        @Override
        public void focusLost(FocusEvent e) {
            editPane.getTextArea().requestFocus();
        }

    }

    private class PopupMenuListenerImpl implements PopupMenuListener {

        public void popupMenuCanceled(PopupMenuEvent e) {
            editPane.requestFocus();
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            editPane.requestFocus();
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

    }

    private class ActionListenerImpl implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (!updating) {
                Log.log(Log.DEBUG, this, "actionPerformed: " + e.getActionCommand());
                Object item = getSelectedItem();
                if (item instanceof BufferItem) {
                    BufferItem bufferItem = (BufferItem)item;
                    if (editPane.getBuffer() != bufferItem.buffer) {
                        updating = true;
                        editPane.setBuffer(bufferItem.buffer, false);
                        updating = false;
                    }
                }
            }
        }

    }

}