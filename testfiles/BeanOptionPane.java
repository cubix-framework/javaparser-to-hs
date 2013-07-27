/*
* BeanOptionPane.java - jEdit option panel for a Java bean
* Copyright (C) 2001 Dirk Moebius
*
* :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package org.gjt.sp.jedit.options;


import java.lang.reflect.Method;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.gui.JMouseComboBox;
import org.gjt.sp.jedit.gui.VariableGridLayout;
import org.gjt.sp.util.Log;


/**
 * A jEdit <code>OptionPane</code> for setting the properties of a Java bean.
 *
 * The option pane shows a tabular list of property editors for each
 * bean property:
 *
 * <ul>
 *   <li>A <code>JComboBox</code> with the tag values for a tagged property
 *       (This includes properties of type <code>boolean</code>, because
 *       boolean fields have the tags "True" and "False" by default).
 *   <li>A <code>JButton</code> for a property that has a custom editor.
 *       Clicking on the button invokes the custom editor.
 *   <li>A <code>JTextField</code> for all other properties.
 * </ul>
 *
 * The following Java bean features are currently not supported:
 *
 * <ul>
 *   <li>non-editable properties (will not be shown)
 *   <li>bound/constrained properties (other values may be entered)
 *   <li>properties with attribute names
 * <ul>
 *
 * @author Dirk Moebius (<a href="mailto:dmoebius@gmx.net">dmoebius@gmx.net</a>)
 * @see java.beans.BeanInfo
 * @see java.beans.PropertyDescriptor
 */
public class BeanOptionPane extends AbstractOptionPane {

	/**
	 * Creates a new BeanOptionPane using the current class loader.
	 *
	 * @param  optionsKey     prefix for bean properties when loaded and
	 *                        saved from/to the jEdit properties.
	 * @param  beanClassName  the class name of the bean.
	 */
	public BeanOptionPane( String optionsKey, String beanClassName ) {
		this( optionsKey, beanClassName, null );
	}


	/**
	 * Creates a new BeanOptionPane using a custom class loader.
	 *
	 * @param  optionsKey       prefix for bean properties when loaded and
	 *                          saved from/to the jEdit properties.
	 * @param  beanClassName    the class name of the bean.
	 * @param  beanClassLoader  the class loader used to construct a bean
	 *                          instance for getting its properties.
	 */
	public BeanOptionPane( String optionsKey, String beanClassName, ClassLoader beanClassLoader ) {
		super( optionsKey );
		this.beanClassName = beanClassName;
		this.beanClassLoader = beanClassLoader;
	}


	public void _init() {
		JSplitPane split = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, getBeanPropertiesPane(), getHelpPane() );
		this.setLayout( new BorderLayout( 5, 5 ) );
		this.add( split );
	}


	public void _save() {
		for ( Enumeration e = properties.keys(); e.hasMoreElements(); ) {
			Component editor = ( Component ) e.nextElement();
			PropertyInfo info = ( PropertyInfo ) properties.get( editor );
			String value = null;

			// Three types of editors are used in this BeanOptionPane:
			// - JButton for bean properties with a custom editor
			//   (the button invokes the custom editor),
			// - JComboBox for tagged bean properties,
			// - JTextField for untagged bean properties.

			if ( editor instanceof JComboBox )
				value = ( String ) ( ( JComboBox ) editor ).getSelectedItem();
			else if ( editor instanceof JTextField )
				value = ( ( JTextField ) editor ).getText();
			else if ( editor instanceof JButton )
				value = info.editor.getAsText();
			else
				continue; // ignore - it's a label

			String name = info.descriptor.getName();
			jEdit.setProperty( super.getName() + "." + name, value );
			Log.log( Log.DEBUG, this, "saved property: " + name + "=" + value );
		}
	}


	private JScrollPane getHelpPane() {
		helpArea = new JTextArea();
		helpArea.setEditable( false );
		helpArea.setLineWrap( true );
		helpArea.setWrapStyleWord( true );
		helpArea.setFont( HELP_FONT );
		helpArea.setBackground( HELP_BACKGROUND );
		helpArea.setForeground( HELP_FOREGROUND );
		helpArea.setText( "" );

		JScrollPane scrHelpArea = new JScrollPane( helpArea );
		scrHelpArea.setPreferredSize( new Dimension( 300, 50 ) );

		return scrHelpArea;
	}


	private JScrollPane getBeanPropertiesPane() {
		JPanel content = new JPanel( new VariableGridLayout( VariableGridLayout.FIXED_NUM_COLUMNS, 2, 10, 3 ) );
		JScrollPane scrContent = new JScrollPane( content );
		scrContent.setPreferredSize( new Dimension( 300, 300 ) );

		try {
			// Try to instantiate a beanClass object:
			Object beanObject;
			try {
				beanObject = Beans.instantiate( beanClassLoader, beanClassName );
			}
			catch ( NoSuchMethodError nsmerr ) {
				throw new Exception( "Could not instantiate bean class " + beanClassName + " because default constructor not found!" );
			}
			catch ( Exception ex ) {
				throw new Exception( "Could not instantiate bean class " + beanClassName + ": " + ex );
			}

			// Try to get BeanInfo (it should be there!):
			BeanInfo beanInfo = Introspector.getBeanInfo( beanObject.getClass() );
			if ( beanInfo == null )    // cannot work without a BeanInfo:
				throw new Exception( "BeanInfo not found for class " + beanClassName + "! Maybe this is not a bean?!?" );

			// Try to get customizer class, if available:
			BeanDescriptor beanDescriptor = beanInfo.getBeanDescriptor();
			if ( beanDescriptor != null ) {
				Class customizerClass = beanDescriptor.getCustomizerClass();
				if ( customizerClass != null ) {
					Customizer customizer = ( Customizer ) customizerClass.newInstance();
					if ( !( customizer instanceof Component ) )
						throw new Exception( "can't use bean customizer because it is no java.awt.Component" );
					// Add bean customizer as the only component:
					content.add( ( Component ) customizer );
					return scrContent;
				}
			}

			// Get all property descriptions:
			PropertyDescriptor[] beanProperties = beanInfo.getPropertyDescriptors();
			if ( beanProperties == null ) {
				Log.log( Log.WARNING, this, "no bean properties found for bean " + beanClassName );
				return scrContent;
			}

			java.util.Arrays.sort( beanProperties, new FeatureDescriptorComparator() );

			// Get all property editors:
			BeanHelper beanHelper = new BeanHelper( super.getName(), beanClassName, beanClassLoader );
			Hashtable beanPropertyEditors = beanHelper.getPropertyEditors( beanProperties );

			// Traverse all properties:
			for ( int i = 0; i < beanProperties.length; i++ ) {
				PropertyDescriptor pd = beanProperties[ i ];
				String name = pd.getName();
				String displayName = pd.getDisplayName();

				PropertyEditor propertyEditor = ( PropertyEditor ) beanPropertyEditors.get( name );
				if ( propertyEditor == null )
					continue;

				// Try to get value of property from jEdit properties:
				String valueText = jEdit.getProperty( super.getName() + "." + name );
				if ( valueText != null ) {
					propertyEditor.setAsText( valueText );
				}
				else {
					// Get the value from the bean object:
					Method method = pd.getReadMethod();
					if ( method != null ) {
						try {
							Object value = method.invoke( beanObject, null );
							propertyEditor.setValue( value );
						}
						catch ( Exception ex ) {
							Log.log( Log.ERROR, this, "property " + name + ": could not get value, got exception:" );
							Log.log( Log.ERROR, this, ex );
							continue;
						}
					}
					else {
						Log.log( Log.WARNING, this, "property " + name + ": no read method to get property value" );
					}
				}

				// Add an editor for the property. The editor is either a JTextField,
				// a JComboBox or a JButton (for calling a custom editor):
				Component editor;
				if ( propertyEditor.getCustomEditor() != null ) {
					// The property editor has a custom editor:
					// need to wrap it in a dialog that's invoked by a button:
					JButton button = new JButton( "Customize..." );
					button.setActionCommand( name );
					button.addActionListener( propertyConfigureAction );
					editor = button;
				}
				else if ( propertyEditor.isPaintable() ) {
					Log.log( Log.ERROR, this, "property " + name + ": paintable editors are not supported" );
					continue;
				}
				else {
					// Get the property value as text:
					String propertyText = propertyEditor.getAsText();
					// If the property editor support tags, add a JComboBox of all tags:
					String[] tags = propertyEditor.getTags();
					if ( tags != null ) {
						JComboBox comboBox = new JMouseComboBox( tags );
						comboBox.setEditable( false );
						comboBox.setSelectedItem( propertyText );
						editor = comboBox;
					}
					else {
						JTextField textField = new JTextField( propertyText );
						editor = textField;
					}
				}

				// Add the editor for this property:
				JLabel label = new JLabel( displayName + ":", SwingConstants.RIGHT );
				label.addMouseListener( mouseHandler );
				editor.addMouseListener( mouseHandler );
				content.add( label );
				content.add( editor );

				// Remember the property for save() and the mouse handler
				// and property configure action:
				PropertyInfo info = new PropertyInfo( pd, propertyEditor );
				properties.put( label, info );
				properties.put( editor, info );
			} // for all properties
		}
		catch ( Exception ex ) {
			Log.log( Log.ERROR, this, ex );
		}

		content.add( Box.createGlue() );

		return scrContent;
	}


	private MouseHandler mouseHandler = new MouseHandler();
	private PropertyConfigureAction propertyConfigureAction = new PropertyConfigureAction();
	private String beanClassName = null;
	private ClassLoader beanClassLoader = null;

	// The following members cannot be private because inner classes like
	// MouseHandler cannot access private fields if compiled on JDK 1.1.x
	// (this is a bug on old JDK's).

	/*private*/
	JTextArea helpArea;
	/*private*/
	Hashtable properties =  new Hashtable();

	private static final Color HELP_BACKGROUND = UIManager.getColor( "Label.background" );
	private static final Color HELP_FOREGROUND = UIManager.getColor( "Label.foreground" );
	private static final Font HELP_FONT = new Font( UIManager.getFont( "Label.font" ).getName(), UIManager.getFont( "Label.font" ).getStyle(), 11 );


	/**
	 * Displays short descritions for properties in the help area,
	 * if the mouse is over them.
	 */
	private class MouseHandler extends MouseAdapter {
		public void mouseEntered( MouseEvent evt ) {
			PropertyInfo info = ( PropertyInfo ) properties.get( evt.getSource() );
			helpArea.setText( info.descriptor.getShortDescription() );
			helpArea.setCaretPosition( 0 );
		}
	}


	/**
	 * Action to perform if a property with a "Customize..." button is pressed.
	 */
	private class PropertyConfigureAction implements ActionListener {
		public void actionPerformed( ActionEvent evt ) {
			PropertyInfo info = ( PropertyInfo ) properties.get( evt.getSource() );
			new CustomPropertyEditorDialog( info );
		}
	}


	/**
	 * Modal dialog for properties with a custom property editor.
	 */
	private class CustomPropertyEditorDialog extends JDialog implements ActionListener {

		public CustomPropertyEditorDialog( final PropertyInfo info ) {
			super( ( Frame ) null, "Customize " + info.descriptor.getDisplayName(), true );
			this.info = info;

			origValue = info.editor.getValue();
			Component editor = info.editor.getCustomEditor();

			ok = new JButton( jEdit.getProperty( "common.ok" ) );
			ok.addActionListener( this );

			cancel = new JButton( jEdit.getProperty( "common.cancel" ) );
			cancel.addActionListener( this );

			JPanel buttons = new JPanel( new FlowLayout( FlowLayout.CENTER, 10, 10 ) );
			buttons.add( ok );
			buttons.add( cancel );

			this.getContentPane().setLayout( new BorderLayout( 10, 10 ) );
			this.getContentPane().add( editor, BorderLayout.CENTER );
			this.getContentPane().add( buttons, BorderLayout.SOUTH );

			this.addWindowListener( new WindowAdapter() {
				        public void windowClosing( WindowEvent e ) {
					        // Restore original value:
					        info.editor.setValue( origValue );
				        }
			        }
			                      );

			this.pack();
			this.setLocationRelativeTo( BeanOptionPane.this );
			this.setVisible( true );
		}

		public void actionPerformed( ActionEvent evt ) {
			if ( evt.getSource() == ok ) {
				this.dispose();
			}
			else if ( evt.getSource() == cancel ) {
				// Restore original value:
				info.editor.setValue( origValue );
				this.dispose();
			}
		}

		private JButton ok;
		private JButton cancel;
		private PropertyInfo info;
		private Object origValue;
	}


	/**
	 * Entry for Hashtable 'properties'.
	 */
	private static class PropertyInfo {
		public PropertyDescriptor descriptor;
		public PropertyEditor editor;

		public PropertyInfo( PropertyDescriptor d, PropertyEditor e ) {
			this.descriptor = d;
			this.editor = e;
		}
	}


	/**
	 * A comparator that sorts by the display name of
	 * <code>java.beans.FeatureDescriptor</code> objects.
	 * @see java.beans.FeatureDescriptor#getDisplayName()
	 */
	private static class FeatureDescriptorComparator implements java.util.Comparator {
		/**
		 * @exception ClassCastException if obj1 and obj2 are not instances of
		 *            <code>java.beans.FeatureDescriptor</code>.
		 * @see java.beans.FeatureDescriptor
		 */
		public int compare( Object obj1, Object obj2 ) {
			return ( ( FeatureDescriptor ) obj1 ).getDisplayName().compareTo( ( ( FeatureDescriptor ) obj2 ).getDisplayName() );
		}
	}

}

