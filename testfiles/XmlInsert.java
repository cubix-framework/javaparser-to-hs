/*
 * XmlInsert.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Slava Pestov
 *
 * The XML plugin is licensed under the GNU General Public License, with
 * the following exception:
 *
 * "Permission is granted to link this code with software released under
 * the Apache license version 1.1, for example used by the Xerces XML
 * parser package."
 */

package xml;

//{{{ Imports
import javax.swing.event.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.Log;
import sidekick.*;
import xml.XmlListCellRenderer.WithLabel;
import xml.completion.*;
import xml.parser.TagParser;
//}}}

public class XmlInsert extends JPanel implements EBComponent
{
	//{{{ XmlInsert constructor
	public XmlInsert(View view, boolean sideBySide)
	{
		this.view = view;

		setLayout(new GridLayout(sideBySide ? 1 : 3,
			sideBySide ? 3 : 1,3,3));

		JPanel elementPanel = new JPanel(new BorderLayout());
		elementPanel.add(BorderLayout.NORTH,
			new JLabel(jEdit.getProperty("xml-insert.elements")));

		elementList = new JList();
		elementList.setName("elements");
		elementList.setCellRenderer(new XmlListCellRenderer());
		elementList.addMouseListener(new MouseHandler());
		elementList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		elementPanel.add(BorderLayout.CENTER,
			new JScrollPane(elementList));

		add(elementPanel);

		JPanel entityPanel = new JPanel(new BorderLayout());
		entityPanel.add(BorderLayout.NORTH,
			new JLabel(jEdit.getProperty("xml-insert.entities")));

		entityList = new JList();
		entityList.setName("entities");
		entityList.setCellRenderer(new XmlListCellRenderer());
		entityList.addMouseListener(new MouseHandler());
		entityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		entityPanel.add(BorderLayout.CENTER,
			new JScrollPane(entityList));

		add(entityPanel);

		JPanel idPanel = new JPanel(new BorderLayout());
		idPanel.add(BorderLayout.NORTH,
			new JLabel(jEdit.getProperty("xml-insert.ids")));

		idList = new JList();
		idList.setName("ids");
		idList.setCellRenderer(new XmlListCellRenderer());
		idList.addMouseListener(new MouseHandler());
		idList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		idPanel.add(BorderLayout.CENTER,new JScrollPane(idList));

		add(idPanel);

		caretHandler = new CaretHandler();

		update();

		updateTimer = new Timer(0,new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				updateTagList();
			}
		});
	} //}}}

	//{{{ addNotify() method
	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);

		EditPane[] editPanes = view.getEditPanes();
		for(int i = 0; i < editPanes.length; i++)
		{
			JEditTextArea textArea = editPanes[i].getTextArea();
			textArea.addCaretListener(caretHandler);
		}
	} //}}}

	//{{{ removeNotify() method
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);

		EditPane[] editPanes = view.getEditPanes();
		for(int i = 0; i < editPanes.length; i++)
		{
			JEditTextArea textArea = editPanes[i].getTextArea();
			textArea.removeCaretListener(caretHandler);
		}

		updateTimer.stop();
	} //}}}

	//{{{ handleMessage() method
	public void handleMessage(EBMessage msg)
	{
		//{{{ EditPaneUpdate
		if(msg instanceof EditPaneUpdate)
		{
			EditPaneUpdate emsg = (EditPaneUpdate)msg;
			EditPane editPane = emsg.getEditPane();
			if(emsg.getWhat() == EditPaneUpdate.BUFFER_CHANGED
				&& editPane == view.getEditPane())
			{
				update();
			}
			else if(emsg.getWhat() == EditPaneUpdate.CREATED
				&& editPane.getView() == view)
			{
				JEditTextArea textArea = editPane.getTextArea();
				textArea.addCaretListener(caretHandler);
			}
			else if(emsg.getWhat() == EditPaneUpdate.DESTROYED
				&& editPane.getView() == view)
			{
				JEditTextArea textArea = editPane.getTextArea();
				textArea.removeCaretListener(caretHandler);
			}
			else if(emsg.getWhat() == EditPaneUpdate.BUFFER_CHANGED)
				update();
		} //}}}
		//{{{ BufferUpdate
		else if(msg instanceof BufferUpdate)
		{
			BufferUpdate bmsg = (BufferUpdate)msg;
			if((bmsg.getWhat() == BufferUpdate.PROPERTIES_CHANGED
				|| bmsg.getWhat() == BufferUpdate.LOADED)
				&& bmsg.getBuffer() == view.getBuffer())
			{
				update();
			}
		} //}}}
		//{{{ SideKickUpdate
		else if(msg instanceof SideKickUpdate)
		{
			if(((SideKickUpdate)msg).getView() == view)
				update();
		} //}}}
	} //}}}
	
	
	//{{{ openAndGo method
	public static void openAndGo(View view, TextArea textArea, String uri, int gotoLine, int col)
	{
		Buffer buffer = jEdit.openFile(view,uri);
		if(buffer == null)
			return;
		// waiting for complete loading of buffer seems
		// to prevent the NullPointerException
		VFSManager.waitForRequests();
		int line = Math.min(buffer.getLineCount() - 1,gotoLine);
		int column = Math.min(buffer.getLineLength(line),col);
		int offset = buffer.getLineStartOffset(line) + column;
		// TODO: follow best practice from Navigator plugin
		try{
			textArea.setCaretPosition(offset);
		}catch(NullPointerException npe){
			Log.log(Log.ERROR,XmlInsert.class,"FIXME : setCaretPosition("+offset+")");
			Log.log(Log.ERROR,XmlInsert.class,npe);
		}
	}//}}}

	//{{{ Private members

	//{{{ Instance variables
	private View view;
	private CaretHandler caretHandler;
	private List elements;
	private JList elementList;
	private JList entityList;
	private JList idList;
	private boolean html;

	private int delay;
	private Timer updateTimer;
	//}}}

	//{{{ update() method
	private void update()
	{
		XmlParsedData data = XmlParsedData.getParsedData(view, false);
		if(data == null)
		{
			setDeclaredEntities(null);
			setDeclaredIDs(null);
			html = false;
		}
		else
		{
			setDeclaredEntities(data.entities);
			setDeclaredIDs(data.getSortedIds());
			html = data.html;
		}

		updateTagList();
	} //}}}

	//{{{ showNotParsedMessage() method
	private void showNotParsedMessage()
	{
		setDeclaredElements(null);
		setDeclaredEntities(null);
		setDeclaredIDs(null);
	} //}}}

	//{{{ setDeclaredElements() method
	private void setDeclaredElements(List elements)
	{
		this.elements = elements;

		if(elements == null)
		{
			DefaultListModel model = new DefaultListModel();
			model.addElement(new WithLabel<String>("",jEdit.getProperty("xml-insert.not-parsed")));
			elementList.setModel(model);
		}
		else
		{
			ArrayListModel model = new ArrayListModel(elements);
			elementList.setModel(model);

			if(model.getSize() != 0)
			{
				Rectangle cellBounds = elementList
					.getCellBounds(0,0);
				if(cellBounds != null)
				{
					elementList.setFixedCellHeight(cellBounds.height);
				}
			}
		}
	} //}}}

	//{{{ setDeclaredEntities() method
	private void setDeclaredEntities(List<EntityDecl> entities)
	{
		if(entities == null)
		{
			DefaultListModel model = new DefaultListModel();
			model.addElement(new WithLabel<String>(jEdit.getProperty("xml-insert.not-parsed")));
			entityList.setModel(model);
		}
		else
		{
			List<WithLabel<EntityDecl>> nentities = new ArrayList<WithLabel<EntityDecl>>(entities.size());
			for(EntityDecl e:entities){
				nentities.add(new WithLabel<EntityDecl>(e));
			}

			ArrayListModel model = new ArrayListModel(nentities);
			entityList.setModel(model);

			if(model.getSize() != 0)
			{
				Rectangle cellBounds = entityList
					.getCellBounds(0,0);
				if(cellBounds != null)
				{
					entityList.setFixedCellHeight(cellBounds.height);
				}
			}
		}

	} //}}}

	//{{{ setDeclaredIDs() method
	private void setDeclaredIDs(List<IDDecl> ids)
	{
		if(ids == null)
		{
			DefaultListModel model = new DefaultListModel();
			model.addElement(new WithLabel<String>(jEdit.getProperty("xml-insert.not-parsed")));
			idList.setModel(model);
		}
		else
		{
			
			List<WithLabel<IDDecl>> nids = new ArrayList<WithLabel<IDDecl>>(ids.size());
			for(IDDecl id:ids){
				nids.add(new WithLabel<IDDecl>(id));
			}
			
			ArrayListModel model = new ArrayListModel(nids);
			idList.setModel(model);

			if(model.getSize() != 0)
			{
				Rectangle cellBounds = idList.getCellBounds(0,0);
				if(cellBounds != null)
				{
					idList.setFixedCellHeight(cellBounds.height);
				}
			}
		}

	} //}}}

	//{{{ updateTagList() method
	private void updateTagList()
	{
		Buffer buffer = view.getBuffer();

		XmlParsedData data = XmlParsedData.getParsedData(view,false);

		if(data != null)
		{

			Selection[] selection = view.getTextArea().getSelection();
			List<ElementDecl> l;
			if(selection.length > 0) {
				// in multiple selection mode, elements that are not allowed for the other selections
				// are still proposed. Not a big deal, apparently
				l = data.getAllowedElements(
						buffer,
						selection[0].getStart(),
						selection[0].getEnd());
			}
			else
			{
				l = (data.getAllowedElements(
					buffer,view.getTextArea().getCaretPosition()));
			}
			
			List<WithLabel<ElementDecl>> nl = new ArrayList<WithLabel<ElementDecl>>(l.size());
			Map<String, String> namespaces = data.getNamespaceBindings(view.getTextArea().getCaretPosition());
			Map<String, String> localNamespacesToInsert = new HashMap<String, String>();
			for(ElementDecl elementDecl: l){
				String elementName;
				String elementNamespace = elementDecl.completionInfo.namespace;
				// elementDecl.name is the local name, now we must find a qualified name
				if(elementNamespace == null || "".equals(elementNamespace))
				{
					elementName = elementDecl.name;
				}
				else
				{
					String pre = namespaces.get(elementNamespace);
					if(pre == null)
					{
						pre = localNamespacesToInsert.get(elementNamespace);
					}
					if(pre == null)
					{
						// handle elements in undeclared namespace and no prefix.
						// Generate a new prefix.
						// Store it locally, so that the declaration is not inserted when this completion is not chosen.
						// If it's chosen, a prefix (maybe different) will be generated
						pre = EditTagDialog.generatePrefix(namespaces, localNamespacesToInsert);
						localNamespacesToInsert.put(elementNamespace,pre);
						elementName = pre + ":" + elementDecl.name;
					}
					else
					{
						if("".equals(pre)){
							elementName = elementDecl.name;
						}else{
							elementName = pre + ":" + elementDecl.name;
						}
					}
				}
				
				nl.add(new WithLabel<ElementDecl>(elementName, elementDecl));
			}
			setDeclaredElements(nl);
		}
		else
		{
			setDeclaredElements(null);
		}
	} //}}}

	//{{{ updateTagListWithDelay() method
	private void updateTagListWithDelay()
	{
		if(updateTimer.isRunning())
			updateTimer.stop();

		updateTimer.setInitialDelay(200);
		updateTimer.setRepeats(false);
		updateTimer.start();
	} //}}}

	//}}}
	
	

	//{{{ ArrayListModel class
	static class ArrayListModel implements ListModel
	{
		List list;

		ArrayListModel(List list)
		{
			this.list = list;
		}

		public int getSize()
		{
			return list.size();
		}
		public Object getElementAt(int index)
		{
			return list.get(index);
		}

		public void addListDataListener(ListDataListener l) {}
		public void removeListDataListener(ListDataListener l) {}
	} //}}}

	//{{{ CaretHandler class
	class CaretHandler implements CaretListener
	{
		public void caretUpdate(CaretEvent evt)
		{
			if(evt.getSource() == view.getTextArea())
				updateTagListWithDelay();
		}
	} //}}}

	//{{{ MouseHandler class
	class MouseHandler extends MouseAdapter
	{
		public void mouseClicked(MouseEvent evt)
		{
			Selection insideTag = null;
			//{{{ Handle clicks in element list
			if(evt.getSource() == elementList)
			{
				
				int index = elementList.locationToIndex(
					evt.getPoint());
				if(index == -1)
					return;
				EditPane editPane = view.getEditPane();
				JEditTextArea textArea = editPane.getTextArea();
				Buffer buffer = editPane.getBuffer();

				XmlParsedData data = XmlParsedData.getParsedData(view,true);
				if(data==null)return;

				int pos = textArea.getCaretPosition();
				/* Check if we are inside a tag, and if so, wipe it out before
				   inserting the one we just created */
				CharSequence text = buffer.getSegment(0, buffer.getLength());
				TagParser.Tag current = TagParser.getTagAtOffset(text,pos);
				if (current!=null && current.start < pos && current.end >= pos) {
					insideTag = new Selection.Range(current.start, current.end+1);
				}
				
				idList.setSelectedIndex(index);
				WithLabel<Object> wqn = (WithLabel<Object>)elementList.getModel().getElementAt(index);
				Object obj = wqn.element;
				if(!(obj instanceof ElementDecl))
					return;

				ElementDecl element = (ElementDecl)obj;

				Map<String,String> namespaces = data.getNamespaceBindings(pos);
				Map<String,String> namespacesToInsert = new HashMap<String,String>();
				
				// on left click, show the Edit Tag dialog, on right click, don't show the Edit Tag dialog
				// all the work is done in XmlActions.showEditTagDialog
				XmlActions.showEditTagDialog(view,wqn.label, element,insideTag, namespaces, namespacesToInsert,!GUIUtilities.isPopupTrigger(evt));
			} //}}}
			//{{{ Handle clicks in entity list
			else if(evt.getSource() == entityList)
			{
				int index = entityList.locationToIndex(
					evt.getPoint());
				if(index == -1)
					return;

				WithLabel<Object> wl = (WithLabel<Object>) entityList.getModel().getElementAt(index);
				Object obj = wl.element;
				if(!(obj instanceof EntityDecl))
					return;

				EntityDecl entity = (EntityDecl)obj;

				JEditTextArea textArea = view.getTextArea();

				textArea.setSelectedText("&" + entity.name + ";");
				textArea.requestFocus();
			} //}}}
			//{{{ Handle clicks in ID list
			else if(evt.getSource() == idList)
			{
				int index = idList.locationToIndex(
					evt.getPoint());
				if(index == -1)
					return;

				idList.setSelectedIndex(index);
				WithLabel<Object> wl = (WithLabel<Object>) idList.getModel().getElementAt(index);
				Object obj = wl.element;
				if(!(obj instanceof IDDecl))
					return;

				IDDecl id = (IDDecl)obj;

				JEditTextArea textArea = view.getTextArea();

				if(GUIUtilities.isPopupTrigger(evt))
				{
					openAndGo(view, textArea, id.uri, id.line, id.column);
				}
				else
				{
					textArea.setSelectedText(id.id);
				}

				textArea.requestFocus();
			} //}}}
		}
	} //}}}
}
