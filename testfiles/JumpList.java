/*
 *  Jump plugin for jEdit
 *  Copyright (c) 2003 Pavlikus
 *
 *  :tabSize=4:indentSize=4:
 *  :folding=explicit:collapseFolds=1:
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

    //{{{ imports
package jump;
    import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;

public class JumpList extends JWindow implements CaretListener
{

    //{{{ fields
    public View parent;
    public JEditTextArea textArea;
    public JList itemsList;
    public int width = 25;
    public StringBuffer keyBuff = new StringBuffer();
    private Object[] elements;
    private boolean incremental; //}}}

    //{{{ constructor
    public JumpList(View parent, Object[] list, ListModel model,
                boolean incr_search, String title, int list_width, Point location)
        {
            super(parent);
            this.parent = parent;
            this.textArea = parent.getTextArea();
            this.incremental = incr_search;
            this.elements = list;
            this.width = list_width;

            JPanel pane = new JPanel();
            pane.setLayout(new BorderLayout());

            Font font = jEdit.getFontProperty("jump.list.font", new Font("Monospaced", Font.PLAIN, 11));

            JLabel label = new JLabel(title);
            label.setFont(font);
            pane.add(label, BorderLayout.NORTH);

            itemsList = new JList(model);
            itemsList.setFont(font);
            itemsList.setVisibleRowCount(list.length < 5 ? list.length : 5);
            itemsList.setSelectedIndex(0);
            itemsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            itemsList.addMouseListener(new MouseHandler());
            itemsList.addListSelectionListener(new SelectionListener());

            FontMetrics fm = getFontMetrics(font);
            itemsList.setPreferredSize(
                    new Dimension(this.width * fm.charWidth('m'),
                    (int) itemsList.getPreferredSize().height));

            JScrollPane scroll = new JScrollPane();
            scroll.getViewport().setView(itemsList);

            scroll.setBorder(null);
            pane.add(scroll, BorderLayout.SOUTH);
            pane.setBorder(LineBorder.createBlackLineBorder());
            this.getContentPane().add(pane);

            this.setBackground(Color.lightGray);

            GUIUtilities.requestFocus(this, itemsList);
            pack();

            setLocation(location);
            itemsList.setSelectedIndex(0);
            setVisible(true);

            KeyHandler handler = new KeyHandler();
            itemsList.addKeyListener(handler);
            parent.setKeyEventInterceptor(handler);
            textArea.addCaretListener(this);
        }
    //}}}

    //{{{ void dispose()
    public void dispose()
    {
        // Clear status bar messages if need
        jEdit.getActiveView().getStatus().setMessage(null);   
        
        parent.setKeyEventInterceptor(null);
        textArea.removeCaretListener(this);
        super.dispose();
        SwingUtilities.invokeLater(new Runnable()
                                   {
                                       public void run()
                                       {
                                           textArea.requestFocus();
                                       }
                                   }
                                  );
    } //}}}

    //{{{ empty methods (they override by descendants)
    public void processAction(Object o){}
    public void processInsertAction(Object o){}
    public void processActionInNewView(Object o){}
    public void updateStatusBar(Object itemlist){}
    //}}}

    //{{{ CaretListener inteface
    public void caretUpdate(CaretEvent evt)
    {
        dispose();
    }
    //}}}

    //{{{ class SelectionListener
    class SelectionListener implements ListSelectionListener
    {
        public void valueChanged(ListSelectionEvent e) 
        {
            updateStatusBar((JList)e.getSource());
        }
    } //}}}

    //{{{ class KeyHandler
    class KeyHandler extends KeyAdapter
    {
        int selected;
        int CHAR_DELTA = 1000;
        long m_time = 0;
        String m_key = "";

        //{{{ void keyPressed
        public void keyPressed(KeyEvent evt)
        {
            View view = jEdit.getActiveView();
            switch (evt.getKeyCode())
            {
            case KeyEvent.VK_ENTER:
                if (evt.isShiftDown())
                {
                    JumpList.this.processActionInNewView((Object) itemsList);    
                }
                else
                {
                    JumpList.this.processAction((Object) itemsList);
                }
                dispose();
                evt.consume();
                break;

            case KeyEvent.VK_INSERT:
                JumpList.this.processInsertAction((Object) itemsList);
                dispose();
                evt.consume();
                break;

            case KeyEvent.VK_SPACE:
                if (evt.isShiftDown())
                {
                    JumpList.this.processActionInNewView((Object) itemsList);    
                }
                else
                {
                    JumpList.this.processAction((Object) itemsList);
                }
                dispose();
                evt.consume();
                break;

            case KeyEvent.VK_ESCAPE:
                dispose();
                evt.consume();
                break;

            case KeyEvent.VK_HOME:
                itemsList.setSelectedIndex(0);
                itemsList.ensureIndexIsVisible(0);
                evt.consume();
                break;

            case KeyEvent.VK_END:
                itemsList.setSelectedIndex(
                        itemsList.getModel().getSize() - 1);
                itemsList.ensureIndexIsVisible(
                        itemsList.getModel().getSize() - 1);
                evt.consume();
                break;

            case KeyEvent.VK_PAGE_UP:
                int selected = itemsList.getSelectedIndex();
                selected -= 5;
                if (selected < 0)
                    selected = itemsList.getModel().getSize() - 1;
                itemsList.setSelectedIndex(selected);
                itemsList.ensureIndexIsVisible(selected);
                evt.consume();
                break;

            case KeyEvent.VK_PAGE_DOWN:
                selected = itemsList.getSelectedIndex();
                selected += 5;
                if (selected >= itemsList.getModel().getSize())
                    selected = 0;
                itemsList.setSelectedIndex(selected);
                itemsList.ensureIndexIsVisible(selected);
                evt.consume();
                break;

            case KeyEvent.VK_UP:
                selected = itemsList.getSelectedIndex();
                if (selected == 0)
                {
                    selected = itemsList.getModel().getSize() - 1;
                }
                else
                {
                    selected--;
                }

                itemsList.setSelectedIndex(selected);
                itemsList.ensureIndexIsVisible(selected);
                evt.consume();
                break;

            case KeyEvent.VK_DOWN:
                selected = itemsList.getSelectedIndex();
                if (selected == itemsList.getModel().getSize() - 1)
                {
                    selected = 0;
                }
                else
                {
                    selected++;
                }

                itemsList.setSelectedIndex(selected);
                itemsList.ensureIndexIsVisible(selected);
                evt.consume();
                break;
            }
        } //}}}

        //{{{ void keyTyped
        public void keyTyped(KeyEvent evt)
        {
            char ch = evt.getKeyChar();
            if (evt.getKeyCode() == KeyEvent.VK_INSERT)
            {
                JumpList.this.processInsertAction((Object) itemsList);
                dispose();
                evt.consume();
                return;
            }

            if (!Character.isLetterOrDigit(ch))
                return;

            if (m_time + CHAR_DELTA < System.currentTimeMillis())
                m_key = "";

            m_time = System.currentTimeMillis();
            m_key += Character.toLowerCase(ch);

            int len = itemsList.getModel().getSize();

            for (int i = 0; i < len; i++)
            {
                String item = new String(
                        itemsList.getModel().getElementAt(i).toString().toLowerCase());
                if (item.startsWith(m_key))
                {
                    itemsList.setSelectedIndex(i);
                    itemsList.ensureIndexIsVisible(i);
                    break;
                }
            }
            itemsList.ensureIndexIsVisible(itemsList.getSelectedIndex());  
        } //}}}

    } //}}}

    //{{{ class MouseHandler
    class MouseHandler extends MouseAdapter
    {
        //{{{ MouseHandler.mouseClicked
        public void mouseClicked(MouseEvent me)
        {
            if (me.isShiftDown() == true)
            {
                JumpList.this.processActionInNewView((Object) itemsList);
            }
            else if (me.isControlDown() == true)
            {
                JumpList.this.processInsertAction((Object) itemsList);
            }
            else
            {
                JumpList.this.processAction((Object) itemsList);
            }
            dispose();
            return;
        } //}}}

    } //}}}
}
