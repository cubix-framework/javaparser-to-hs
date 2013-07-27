/*
 * XmlComplete.java - Complete tag popup
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2001, 2002 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml.completion;

//{{{ Imports
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import org.gjt.sp.jedit.gui.KeyEventWorkaround;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.*;
import xml.*;
import xml.parser.*;
//}}}

public class XmlComplete extends JWindow
{
	//{{{ XmlComplete constructor
	public XmlComplete(View view, String text, ArrayList completions, Point location)
	{
		super(view);

		this.view = view;
		this.textArea = view.getTextArea();
		this.text = text;
		this.completions = completions;

		list = new JList();
		list.setCellRenderer(new XmlListCellRenderer());

		list.setVisibleRowCount(Math.min(8,
			Math.max(1,completions.size())));

		list.addMouseListener(new MouseHandler());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		setUpListModel();

		// stupid scrollbar policy is an attempt to work around
		// bugs people have been seeing with IBM's JDK -- 7 Sep 2000
		JScrollPane scroller = new JScrollPane(list,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		getContentPane().add(scroller, BorderLayout.CENTER);

		GUIUtilities.requestFocus(this,list);

		pack();
		setLocation(location);
		show();

		KeyHandler keyHandler = new KeyHandler();
		addKeyListener(keyHandler);
		getRootPane().addKeyListener(keyHandler);
		list.addKeyListener(keyHandler);
		view.setKeyEventInterceptor(keyHandler);
	} //}}}

	//{{{ dispose() method
	public void dispose()
	{
		view.setKeyEventInterceptor(null);
		super.dispose();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				view.getTextArea().requestFocus();
			}
		});
	} //}}}

	//{{{ Private members

	//{{{ Instance variables
	private View view;
	private JEditTextArea textArea;
	private String text;
	private ArrayList completions;
	private JList list;
	//}}}

	//{{{ setUpListModel() method
	private void setUpListModel()
	{
		DefaultListModel model = new DefaultListModel();

		for(int i = 0; i < completions.size(); i++)
		{
			Object obj = completions.get(i);
			if(obj instanceof ElementDecl)
			{
				ElementDecl element = (ElementDecl)obj;
				if(element.name.startsWith(text)
					|| (element.html
					&& element.name.toLowerCase()
					.startsWith(text.toLowerCase())))
				{
					model.addElement(element);
				}
			}
			else if(obj instanceof EntityDecl)
			{
				EntityDecl entity = (EntityDecl)obj;
				if(entity.name.startsWith(text))
					model.addElement(entity);
			}
			else if(obj instanceof IDDecl)
			{
				IDDecl id = (IDDecl)obj;
				if(id.id.startsWith(text))
					model.addElement(id);
			}
		}

		if(model.getSize() == 0)
		{
			model.addElement(new XmlListCellRenderer.EmptyListPlaceholder());
			pack();
		}

		list.setModel(model);
		list.setSelectedIndex(0);
	} //}}}

	//{{{ insertSelected() method
	private void insertSelected(char ch)
	{
		Object obj = list.getSelectedValue();
		if(obj instanceof XmlListCellRenderer.EmptyListPlaceholder)
		{
			if(ch == '>')
				XmlActions.insertClosingTagKeyTyped(view);
			else if(ch == '/')
				XmlActions.completeClosingTag(view);
			else
				textArea.userInput(ch);

			/* do nothing; dispose() is called below. */
		}
		else if(obj instanceof ElementDecl)
		{
			ElementDecl element = (ElementDecl)obj;

			StringBuffer buf = new StringBuffer();
			buf.append(element.name.substring(text.length()));

			for(int i = 0; i < element.attributes.size(); i++)
			{
				ElementDecl.AttributeDecl attr
					= (ElementDecl.AttributeDecl)
					element.attributes.get(i);

				if(attr.required)
				{
					buf.append(' ');
					buf.append(attr.name);
					buf.append("=\"");
					if(attr.value != null)
						buf.append(attr.value);
					buf.append('"');
				}
			}

			if(ch == '\n' || ch == '>')
			{
				int caret = textArea.getCaretPosition();

				if(element.empty)
				{
					if(!element.html)
						buf.append("/>");
					else
						buf.append(">");

					caret += buf.length();
				}
				else
				{
					buf.append(">");

					caret += buf.length();

					if(jEdit.getBooleanProperty(
						"xml.close-complete-open"))
					{
						buf.append("</");
						buf.append(element.name);
						buf.append(">");
					}
				}

				textArea.setSelectedText(buf.toString());
				textArea.setCaretPosition(caret);

				if(ch == '\n')
				{
					// hide the popup first, since the edit tag
					// dialog is modal
					dispose();
					XmlActions.showEditTagDialog(view);
					return;
				}
			}
			else
			{
				buf.append(ch);
				textArea.setSelectedText(buf.toString());
			}
		}
		else if(obj instanceof EntityDecl)
		{
			EntityDecl entity = (EntityDecl)obj;

			textArea.setSelectedText(entity.name.substring(
				text.length()) + ";");
		}
		else if(obj instanceof IDDecl)
		{
			IDDecl id = (IDDecl)obj;

			if(ch == '\t')
			{
				textArea.setCaretPosition(id.declaringLocation
					.getOffset());
			}
			else
				textArea.setSelectedText(id.id);
		}

		dispose();
	} //}}}

	//}}}

	//{{{ KeyHandler class
	class KeyHandler extends KeyAdapter
	{
		//{{{ keyPressed() method
		public void keyPressed(KeyEvent evt)
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if(evt == null)
				return;

			switch(evt.getKeyCode())
			{
			case KeyEvent.VK_ENTER:
				insertSelected('\n');
				evt.consume();
				break;
			case KeyEvent.VK_TAB:
				insertSelected('\t');
				evt.consume();
				break;
			case KeyEvent.VK_ESCAPE:
				dispose();
				evt.consume();
				break;
			case KeyEvent.VK_UP:
				int selected = list.getSelectedIndex();

				if(selected == 0)
					selected = list.getModel().getSize() - 1;
				else if(getFocusOwner() == list)
					return;
				else
					selected = selected - 1;

				list.setSelectedIndex(selected);
				list.ensureIndexIsVisible(selected);

				evt.consume();
				break;
			case KeyEvent.VK_DOWN:
				/* int */ selected = list.getSelectedIndex();

				if(selected == list.getModel().getSize() - 1)
					selected = 0;
				else if(getFocusOwner() == list)
					return;
				else
					selected = selected + 1;

				list.setSelectedIndex(selected);
				list.ensureIndexIsVisible(selected);

				evt.consume();
				break;
			case KeyEvent.VK_SPACE:
				break;
			case KeyEvent.VK_BACK_SPACE:
				if(text.length() == 0)
				{
					textArea.backspace();
					dispose();
				}
				else
				{
					text = text.substring(0,text.length() - 1);
					textArea.backspace();
					setUpListModel();
				}
				break;
			default:
				if(evt.isActionKey())
				{
					dispose();
					view.processKeyEvent(evt);
				}
				break;
			}
		} //}}}

		//{{{ keyTyped() method
		public void keyTyped(KeyEvent evt)
		{
			evt = KeyEventWorkaround.processKeyEvent(evt);
			if(evt == null)
				return;
			else
			{
				char ch = evt.getKeyChar();
				if(ch == '\b')
					return;

				if(ch == ';' || ch == '>'
					|| ch == ' ' || ch == '\t')
				{
					insertSelected(ch);
				}
				else if(ch == '/')
				{
					// in an XML file, a closing tag
					// must always close the most
					// recently opened tag.
					XmlActions.completeClosingTag(view);
					dispose();
				}
				else
				{
					textArea.userInput(ch);
					text = text + ch;
					setUpListModel();
				}

				evt.consume();
			}
		} //}}}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			insertSelected('\n');
		}
	} //}}}
}
