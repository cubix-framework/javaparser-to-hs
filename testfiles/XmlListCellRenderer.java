/*
 * XmlListCellRenderer.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2003 Slava Pestov
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
import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import xml.completion.*;
import xml.completion.ElementDecl.AttributeDecl;
//}}}

/** displays an icon corresponding to the kind of item and a preview for entities. */
public class XmlListCellRenderer extends JPanel implements ListCellRenderer
{
	public static final XmlListCellRenderer INSTANCE = new XmlListCellRenderer();

	//{{{ Icons
	public static final ImageIcon COMMENT_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/Comment.png"));
	public static final ImageIcon CDATA_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/CDATA.png"));
	public static final ImageIcon ELEMENT_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/Element.png"));
	public static final ImageIcon EMPTY_ELEMENT_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/EmptyElement.png"));
	public static final ImageIcon INTERNAL_ENTITY_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/InternalEntity.png"));
	public static final ImageIcon EXTERNAL_ENTITY_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/ExternalEntity.png"));
	public static final ImageIcon ID_ICON = new ImageIcon(
		XmlListCellRenderer.class.getResource("/xml/ID.png"));
	//}}}

	private DefaultListCellRenderer left;
	private DefaultListCellRenderer right;
	
	public XmlListCellRenderer()
	{
		left = new DefaultListCellRenderer();
		right = new DefaultListCellRenderer();
		setLayout(new BorderLayout());
		right.setHorizontalAlignment(SwingConstants.TRAILING);
		add(left,BorderLayout.LINE_START);
		add(right,BorderLayout.LINE_END);
	}
	
	//{{{ getListCellRendererComponent() method
	public Component getListCellRendererComponent(
		JList list,
		Object obj,
		int index,
		boolean isSelected,
		boolean cellHasFocus)
	{
		left.getListCellRendererComponent(list,null,index,
			isSelected,cellHasFocus);
		right.getListCellRendererComponent(list,null,index,
			isSelected,cellHasFocus);
		setBackground(left.getBackground());
		setBorder(left.getBorder());
		left.setBorder(null);
		right.setBorder(null);
		// objects are stored in a WithLabel,
		// to have a preview of the qualified element name
		// without modifying the ElementDecl (resp. AttributeDecl)
		WithLabel<Object> wqn = (WithLabel<Object>)obj;
		Object value = wqn.element;
		if(value instanceof Comment)
		{
			left.setIcon(COMMENT_ICON);
			left.setText("!--");
		}
		else if(value instanceof CDATA)
		{
			left.setIcon(CDATA_ICON);
			left.setText("![CDATA[");
		}
		else if(value instanceof ClosingTag)
		{
			left.setIcon(ELEMENT_ICON);
			left.setText("/" + ((ClosingTag)value).name);
		}
		else if(value instanceof ElementDecl)
		{
			ElementDecl element = (ElementDecl)value;
			left.setIcon(element.empty ? EMPTY_ELEMENT_ICON : ELEMENT_ICON);
			left.setText(wqn.label);
		}
		/* Add a case for AttribDecl */
		else if(value instanceof AttributeDecl)
		{
			left.setText(wqn.label);
		}
		else if(value instanceof EntityDecl)
		{
			EntityDecl entity = (EntityDecl)value;
			left.setIcon(entity.type == EntityDecl.INTERNAL
				? INTERNAL_ENTITY_ICON
				: EXTERNAL_ENTITY_ICON);
			String entityValue = entity.value;
			if(entityValue==null)
			{
				entityValue = entity.publicId;
				if(entityValue == null)
					entityValue = entity.systemId;
			}
			if(entityValue == null)
			{
				right.setText("");
			}
			else
			{
				if(entityValue.length() > 60)
				{
					entityValue = entityValue.substring(0,30)+"..."+entityValue.substring(entityValue.length()-20);
				}
				right.setText("    "+entityValue);
			}
			left.setText(entity.name);
		}
		else if(value instanceof IDDecl)
		{
			left.setIcon(ID_ICON);
			// it's toString() already does this cos I'm too
			// lazy to write a custom renderer for the edit tag
			// dialog box.
			left.setText(value.toString());
		}
		else /* What is it? Dunno. Try toString(). */ 
		{
			left.setText(value.toString());
			left.setIcon(null);
		}
		return this;
	} //}}}

	/** @return	the name of the tag, comment, etc.*/
	public String getMainText()
	{
		return left.getText();
	}
	
	/** @return	the preview of the entity or empty string */
	public String getValueText()
	{
		return right.getText();
	}

	// {{{ Overridden for performance reasons
	/** Overridden for performance reasons. */
	public void repaint() {}
	
	/** Overridden for performance reasons. */
	public void repaint(long tm, int x, int y, int width, int height) {}
	
	/** Overridden for performance reasons. */
	public void repaint(Rectangle r) {}
	
	/** Overridden for performance reasons. */
	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, byte oldValue, byte newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, char oldValue, char newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, short oldValue, short newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, int oldValue, int newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, long oldValue, long newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, float oldValue, float newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, double oldValue, double newValue) {}
	
	/** Overridden for performance reasons. */
	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
	// }}}
	
	public static class ClosingTag
	{
		public String name;

		public ClosingTag(String name)
		{
			this.name = name;
		}
	}

	public static class Comment {}

	public static class CDATA {}

	/**
	 * store a completion with its label for preview
	 */
	public static class WithLabel<Q extends Object>
	{
		public String label;
		public Q element;
		
		/** label will be null */
		public WithLabel(Q element)
		{
			this(null, element);
		}

		public WithLabel(String qname, Q element)
		{
			this.label = qname;
			this.element = element;
		}

	}
}
