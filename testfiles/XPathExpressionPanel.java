/*
 * XPathExpressionPanel.java - Holds XPath expression text field
 *
 * Copyright (C) 2003 Robert McKinnon
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

package xslt;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.XPathAPI;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Panel housing the "XPath Expression" label & text area.
 *
 * @author Robert McKinnon
 */
public class XPathExpressionPanel extends JPanel implements KeyListener,
		DocumentListener, DefaultFocusComponent {

	private static final String LAST_EXPRESSION = "xpath.last-expression";
	private static final String EXPRESSIONS = "xpath.expression";

	private JTextArea textArea = new JTextAreaWithoutTab();
	private int historyLength = Integer.parseInt(jEdit.getProperty("xpath.expression.history-length"));
	private int historyIndex;

    private Window popup;
    private JList popupList;
    private View view;

	public XPathExpressionPanel(View viewParm) {
		super(new BorderLayout());
		view = viewParm;
		String label = jEdit.getProperty("xpath.expression.label");
		String text = jEdit.getProperty(LAST_EXPRESSION);

		textArea.getDocument().addDocumentListener(this);
		textArea.setName("xpath.expression");
		textArea.addKeyListener(this);
		textArea.setText((text == null) ? "" : text);

		add(new JLabel(label), BorderLayout.NORTH);
		add(new JScrollPane(textArea));

		historyIndex = getExpressionList().size() - 1;

		textArea.addCaretListener(new CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
            	textAreaCaretUpdate(evt);
            }
        });
        
		popupList = new JList();
        popupList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                String selection=(String)popupList.getModel().getElementAt(popupList.locationToIndex(e.getPoint()));
                textArea.append(selection);
                popup.setVisible(false);
                textArea.requestFocus();
            }
        });
        
        popupList.addKeyListener(new java.awt.event.KeyAdapter() {
        	public void keyTyped(java.awt.event.KeyEvent evt) {
        		if (popup == null)
        			return;
	            if (evt.getKeyChar() == '@' || evt.getKeyChar() == '/') {
	            	popup.setVisible(false);
	            	textArea.append(Character.toString(evt.getKeyChar()));        		
	        	}
        	}
            public void keyPressed(java.awt.event.KeyEvent evt) {
        		if (popup == null)
        			return;
                if (evt.getKeyCode() == KeyEvent.VK_ESCAPE ||
                		(evt.getKeyCode() == KeyEvent.VK_BACK_SPACE
                		|| evt.getKeyCode() == KeyEvent.VK_RIGHT
						|| evt.getKeyCode() == KeyEvent.VK_LEFT)) {
                    popup.setVisible(false);
                    textArea.requestFocus();
                } else if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_TAB) {
                    int curSel = popupList.getSelectedIndex();
                    String selection=(String)popupList.getModel().getElementAt(curSel);
                    textArea.append(selection);
                    popup.setVisible(false);
                    textArea.requestFocus();
                }
            }
        });

	}

	private void textAreaCaretUpdate(CaretEvent evt) {
        XPathTool xpathTool = (XPathTool)view.getDockableWindowManager().getDockable("xpath-tool");
        if (xpathTool==null || !xpathTool.isAutoCompleteEnabled())
        	return;
        int dot = evt.getDot();
        String txt = textArea.getText().trim();
        if (dot < txt.length())
        	return;
        Rectangle caretCoords = new Rectangle(0,0,0,0);
        try {
            caretCoords = textArea.modelToView(dot);
        } catch (BadLocationException ble) {
            return;
        }
        if (caretCoords == null)
        	return;
        Point caretPos = new Point(caretCoords.x, caretCoords.y);
        SwingUtilities.convertPointToScreen(caretPos, textArea);
		
        txt = txt.replaceAll("\n", "");
        String wildCardExpr = null;
        short elementSearchType = 0;
        // TODO: display completions for string constants
        // TODO: correctly handle nested [
        if (txt.endsWith("/")) {
        	wildCardExpr = txt.concat("*");
        	elementSearchType = Node.ELEMENT_NODE;
        } else if (txt.endsWith("::")) {
        	for (int ndx = txt.length() - 1; ndx >= 0; ndx--) {
        		if (txt.charAt(ndx) == '/')
        			break;
        		if (txt.charAt(ndx) == '[') {
        			txt = txt.substring(0, ndx).concat("/").concat(txt.substring(ndx + 1));
        			break;
        		}
        	}
        	wildCardExpr = txt.concat("*");
        	elementSearchType = Node.ELEMENT_NODE;
        } else if (txt.endsWith("@")) {
        	for (int ndx = txt.length() - 1; ndx >= 0; ndx--) {
        		if (txt.charAt(ndx) == '/')
        			break;
        		if (txt.charAt(ndx) == '[') {
        			txt = txt.substring(0, ndx).concat("/@");
        			break;
        		}
        	}
        	wildCardExpr = txt.concat("*");
        	elementSearchType = Node.ATTRIBUTE_NODE;	
        } else if (txt.endsWith("[")) {
        	int bracketPos = txt.lastIndexOf("[");
        	wildCardExpr = txt.substring(0, bracketPos).concat("/*");
        	elementSearchType = Node.ELEMENT_NODE;
        }
        
        if (wildCardExpr != null) {
			Document xdoc = null;
			try {
				xdoc = xpathTool.getCurrentDocument();
			} catch (Exception e) {
				XSLTPlugin.processException(e, jEdit.getProperty("xpath.result.message.buffer-unparseable"), 
						XPathExpressionPanel.this);
			}
			popupList.setListData(new Object[]{""});
			NodeList nodes = null;
			try {
				nodes = XPathAPI.selectNodeList(xdoc, wildCardExpr);
			} catch (TransformerException e1) {
				return;
			}
			SortedSet names = new TreeSet();
			if (nodes.getLength() == 0)
				return;
			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);
				if (node.getNodeType() != elementSearchType)
					continue;
				names.add(node.getNodeName());
			}
			Object namelist[] = new Object[names.size()];
			namelist = names.toArray();
			popupList.setListData(namelist);     
			popupList.setSelectedIndex(0);
	        
	        //Container cc = xpathTool.getParent();
			Container cc = (Container)xpathTool;
			JFrame jf = (JFrame)view;
			while ((cc = cc.getParent()) != null) {
				try {
					jf = (JFrame)cc;
				} catch (Exception ex) {
				}
			}
	        popup = new Window(jf);
	        popup.add(new JScrollPane(popupList));
	        popup.pack();

	        popup.setLocation(caretPos.x + 5, caretPos.y);
            popup.setVisible(true);
        }
	}

	/**
	 * Stores the current expression at the end of the expression history, and then returns the expression.
	 */
	public String getExpression() {
		return textArea.getText();
	}


	/**
	 * Adds supplied expression to the expression history.
	 *
	 * @param expression expression to be stored in the history.
	 */
	public void addToHistory(String expression) {
		List expressionList = getExpressionList();

		for (int i = 0; i < expressionList.size(); i++) {
			String oldExpression = (String) expressionList.get(i);

			if (oldExpression.equals(expression)) {
				expressionList.remove(i);
			}
		}

		if (expressionList.size() == historyLength) {
			expressionList.remove(0);
		}

		expressionList.add(expression);
		PropertyUtil.setEnumeratedProperty(EXPRESSIONS, expressionList);
		historyIndex = expressionList.size() - 1;
	}


	public void changedUpdate(DocumentEvent e) {
		storeExpression();
	}


	public void insertUpdate(DocumentEvent e) {
		storeExpression();
	}


	public void removeUpdate(DocumentEvent e) {
		storeExpression();
	}


	private void storeExpression() {
		String text = textArea.getText();
		jEdit.setProperty(LAST_EXPRESSION, (text == null) ? "" : text);
	}


	public void keyTyped(KeyEvent e) {
	}


	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_PAGE_UP:
				displayPreviousExpression();
				break;
			case KeyEvent.VK_PAGE_DOWN:
				displayNextExpression();
				break;
		}
	}


	public void keyReleased(KeyEvent e) {
	}

	private void displayPreviousExpression() {
		List expressionList = getExpressionList();

		if (historyIndex > 0) {
			historyIndex--;
			String expression = (String) expressionList.get(historyIndex);
			textArea.setText(expression);
		}
	}


	private void displayNextExpression() {
		List expressionList = getExpressionList();
		int lastIndex = expressionList.size() - 1;

		if (historyIndex < lastIndex && historyIndex > -1) {
			historyIndex++;
			String expression = (String) expressionList.get(historyIndex);
			textArea.setText(expression);
		}
	}


	private List getExpressionList() {
		return PropertyUtil.getEnumeratedProperty(EXPRESSIONS);
	}

	public void focusOnDefaultComponent() {
		textArea.selectAll();
		textArea.requestFocus();
	}

	/**
	 * JTextArea that let's tab key change focus to the next component.
	 */
	public class JTextAreaWithoutTab extends JTextArea {
		public boolean isManagingFocus() {
			return false;
		}
	}

}
