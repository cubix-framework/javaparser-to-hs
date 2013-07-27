package superabbrevs.gui.scrollablepopupmenu;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractListModel;

import org.gjt.sp.jedit.GUIUtilities;

import superabbrevs.utilities.Log;
import superabbrevs.utilities.Log.Level;

public class ScrollablePopupMenu<T> extends javax.swing.JWindow {
    
    private ScrollAbleMenuModel model = new ScrollAbleMenuModel();
    private ArrayList<ScrollablePopupMenuListner<T>> listeners = 
            new ArrayList<ScrollablePopupMenuListner<T>>();
        
    public ScrollablePopupMenu(Window owner, Point location, Collection<T> elements) {
        super(owner);
        
        if (elements != null) {
            for (T elem : elements) {
                model.add(elem);
            }
        } 
                        
        initComponents();
        
        GUIUtilities.requestFocus(this,menuJList);
        setLocationByPlatform(false);
        
        setLocation(location);
        menuJList.setFocusTraversalKeysEnabled(false);
    }
    
    public void addActionListener(ScrollablePopupMenuListner<T> listener) {
        listeners.add(listener);
    }
    
    public void removeActionListener(ScrollablePopupMenuListner<T> listener) {
        listeners.remove(listener);
    }
    
    public void fireActionPerfermed(ScrollablePopupMenuEvent<T> event) {
        for (ScrollablePopupMenuListner<T> listener : listeners) {
            listener.selectedMenuItem(event);
        }
    }
    
    private void initComponents() {
        menuJScrollPane = new javax.swing.JScrollPane();
        menuJList = new javax.swing.JList();

        getAccessibleContext().setAccessibleParent(null);
        menuJScrollPane.setInheritsPopupMenu(true);
        menuJScrollPane.setMinimumSize(null);
        menuJScrollPane.setPreferredSize(null);
        menuJList.setModel(model);
        menuJList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        menuJList.setSelectedIndex(0);
        menuJList.setValueIsAdjusting(true);
        menuJList.setVisibleRowCount(Math.min(model.getSize(),10));
        menuJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                menuJListMouseClicked(evt);
            }
        });
        menuJList.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                menuJListFocusLost(evt);
            }
        });
        menuJList.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                menuJListKeyPressed(evt);
            }
        });

        menuJScrollPane.setViewportView(menuJList);

        getContentPane().add(menuJScrollPane, java.awt.BorderLayout.CENTER);

        pack();
    }

    private void menuJListKeyPressed(java.awt.event.KeyEvent evt) {
        switch(evt.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                closeWindow();
                evt.consume();
                break;
            case KeyEvent.VK_ENTER: invoke(); break;
            case KeyEvent.VK_TAB: invoke(); break;
            case KeyEvent.VK_1: invoke(1); break;
            case KeyEvent.VK_2: invoke(2); break;
            case KeyEvent.VK_3: invoke(3); break;
            case KeyEvent.VK_4: invoke(4); break;
            case KeyEvent.VK_5: invoke(5); break;
            case KeyEvent.VK_6: invoke(6); break;
            case KeyEvent.VK_7: invoke(7); break;
            case KeyEvent.VK_8: invoke(8); break;
            case KeyEvent.VK_9: invoke(9); break;
            case KeyEvent.VK_0: invoke(0); break;
        }
    }

    private void menuJListFocusLost(java.awt.event.FocusEvent evt) {
        closeWindow();
    }
    
    private void closeWindow() {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                dispose();
            }
        });
    }
    
    private void menuJListMouseClicked(java.awt.event.MouseEvent evt) {
        invoke();
    }

    private void invoke() {
        int i = menuJList.getSelectedIndex();
        invoke(i);
    }
    
    private void invoke(int index) {
        if (index < model.getSize()) {
            T o = model.get(index);
            ScrollablePopupMenuEvent<T> event = new ScrollablePopupMenuEvent<T>(this,o);
            fireActionPerfermed(event);
            closeWindow();
        }
    }
    
    private class ScrollAbleMenuModel extends AbstractListModel {
        private ArrayList<T> elements = new ArrayList<T>();
        
        public void add(T element) {
            int index = elements.size();
            elements.add(element);
            this.fireIntervalAdded(this,index,index);
        }
        
        public int getSize() {
            return elements.size();
        }
        
        public T get(int index) {
            return elements.get(index);
        }

        public Object getElementAt(int index) {
            String label = "";
            if (0 <= index && index <= 10) {
                label = index+": ";
            }
            label += elements.get(index).toString();
            return label;
        }
    }
    
    private javax.swing.JList menuJList;
    private javax.swing.JScrollPane menuJScrollPane;
}
