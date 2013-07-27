/*
 * SchemaMappingManager.java - select a schema for validation
 * :tabSize=4:indentSize=4:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009, Eric Le Lay
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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.Color;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import common.gui.OkCancelButtons;
import ise.java.awt.KappaLayout;

import xml.parser.SchemaMapping;
import static xml.PathUtilities.*;
//}}}

public final class SchemaMappingManager
{
	public static final String SCHEMA_MAPPING_PROP = "xml.schema-mapping";
	public static final String BUFFER_ENABLE_SCHEMA_MAPPING_PROP = "xml.enable-schema-mapping";
	public static final String ENABLE_SCHEMA_MAPPING_PROP = "buffer."+BUFFER_ENABLE_SCHEMA_MAPPING_PROP;
	public static final String BUFFER_SCHEMA_PROP = "xml.validation.schema";
	public static final String BUFFER_AUTO_SCHEMA_PROP = "xml.validation.schema-auto";
	
	private static final String BUILT_IN_SCHEMA = "xml/dtds/schemas.xml";
	
	// {{{ singleton constructor
	private SchemaMappingManager(){}
	// }}}
	
	//{{{ promptSchemaForBuffer() method
	/**
	 * - let the user choose a schema file from the VFSBrowser
	 */
	public static void promptSchemaForBuffer(View view, Buffer buffer)
	{
		String oldSchema = buffer.getStringProperty(BUFFER_SCHEMA_PROP);

		String specificSchema = null;
		SchemaMapping lMapping = null;
		SchemaMapping gMapping = null;
		
		boolean schemasEnabled = isSchemaMappingEnabled(buffer);
		
		// local schema-mapping
		if(schemasEnabled){
			specificSchema = MiscUtilities.constructPath(
				MiscUtilities.getParentOfPath(
					buffer.getPath()),SchemaMapping.SCHEMAS_FILE);
		
			lMapping = getLocalSchemaMapping(buffer, new LoggingErrorHandler(jEdit.getProperty("xml.local-schema")));
			gMapping = getGlobalSchemaMapping(new LoggingErrorHandler(jEdit.getProperty("xml.global-schema")));
		
			// always prefer the explicit schema
			String oldSchemaAuto = buffer.getStringProperty(BUFFER_AUTO_SCHEMA_PROP);
			if(oldSchema == null)oldSchema = oldSchemaAuto;
		}
		
		
		ChooseSchemaDialog choose = new ChooseSchemaDialog(view);
		
        if(choose.choose(buffer.getPath(),urlToPath(oldSchema), true))
        {
			String bufferURL = pathToURL(buffer.getPath());
			URI schemaURL = choose.schemaURL;

        	if(schemasEnabled)
        	{
				// no schemas.xml in the buffer's directory : will create one
				if(lMapping == null)
				{
					try
					{
						lMapping = new SchemaMapping(new URI(pathToURL(specificSchema)));
						if(gMapping != null)lMapping.ensureIncluded(gMapping);
					}
					catch(URISyntaxException ue)
					{
						Log.log(Log.ERROR,SchemaMappingManager.class,ue);
					}
				}
				
				
				SchemaMapping.URIResourceRule newRule = new SchemaMapping.URIResourceRule(null,bufferURL,schemaURL.toString(), false);
				
				lMapping.updateMapping(newRule);
				
				try
				{
					lMapping.toDocument(lMapping.getBaseURI().toString());
				}
				catch(IOException ioe)
				{
					// if saving fails, try to output it in a buffer
					JOptionPane.showMessageDialog(
						view,
						jEdit.getProperty("xml-error-to-document.message",new Object[]{ioe.getClass(),ioe.getMessage()}),
						jEdit.getProperty("xml-error-to-document.title"),
						JOptionPane.ERROR_MESSAGE
						);
					Buffer b = jEdit.newFile(view,lMapping.getBaseURI().toString());
					b.insert(0,lMapping.toString());
					view.getEditPane().setBuffer(buffer);
				}
			}
			else
			{
				buffer.setProperty(BUFFER_SCHEMA_PROP,schemaURL);
			}
			
			// finally, use the new schema mapping
			view.getInputHandler().setRepeatCount(1);
			view.getInputHandler().invokeAction("sidekick-parse");
		}
	}
	//}}}

	// {{{ ChooseSchemaDialog class
	static class ChooseSchemaDialog extends EnhancedDialog
	{
		// {{{ private instance variables
		private JCheckBox relative_cb;
		private JTextField path;
		private JTextField buffer_path;
		private JTextField relative_path;
		private boolean valid;
		// }}}
		
		/** URI of the chosen schema */
		URI schemaURL = null;
		/** is schemaURL relative to the buffer */
		boolean relative = false;
		
		/** did the user click OK */
		boolean confirmed = false;
		
		
		ChooseSchemaDialog(final View view)
		{
			super(view, jEdit.getProperty("xml.choose-schema.title"),true);
			JPanel panel = new JPanel( new KappaLayout() );
			panel.setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );
			
			
			ActionListener relativize = new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					relativize();
				}
			};
			
			// reminder : buffer path
			JLabel buffer_label = new JLabel( jEdit.getProperty("xml.choose-schema.buffer-path") );
			buffer_path = new JTextField(40);
			buffer_path.setEditable(false);
			buffer_path.setName("buffer_path");
			
			// choose a path
			JLabel choose_label = new JLabel( jEdit.getProperty("xml.choose-schema.message") );
			path = new HistoryTextField("xml.choose-schema",true);
			path.setName("path");
			path.setText("");
			path.setColumns(40);
			path.addActionListener(relativize);
			
			JButton browse = new JButton( jEdit.getProperty("xml.choose-schema.browse") );
			browse.setMnemonic(KeyEvent.VK_B);
			browse.setName("browse");
			browse.addActionListener( new ActionListener()
				{
					public void actionPerformed( ActionEvent ae )
					{
						String[] paths = GUIUtilities.showVFSFileDialog( view, path.getText(), VFSBrowser.OPEN_DIALOG, false );
						if ( paths != null && paths.length > 0 ) {
							path.setText( paths[0] );
							relativize();
						}
					}
				});
			
			// set a relative path
			relative_cb = new JCheckBox(jEdit.getProperty("xml.choose-schema.relative"));
			relative_cb.addActionListener(relativize);
			relative_cb.setName("relative");
			
			relative_path = new JTextField(40);
			relative_path.setEditable(false);
			relative_path.setName("relative_path");
			
			//LTTTTTTT
			//S
			//LLLLLLLL
			//TTTTTTTB
			//S
			//S
			//S
			//CCCCCC
			//TTTTTTT
			//S
			// OK CANCEL
			
			JPanel okCancel = new OkCancelButtons(this);
			
			// add the components to the option panel
			panel.add( "0, 0, 1, 1, W, w, 3", buffer_label );
			panel.add( "1, 0, 6, 1, W, w, 3", buffer_path );
			panel.add( "0, 1, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );
			panel.add( "0, 2, 8, 1, W, w, 3", choose_label );
			panel.add( "0, 3, 7, 1, W, w, 3", path );
			panel.add( "7, 3, 1, 1, W, w, 3", browse );
			panel.add( "0, 4, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 33, true ) );
			panel.add( "0, 5, 6, 1, 0, w, 0", relative_cb );
			panel.add( "0, 6, 5, 1, W, w, 3", relative_path );
			panel.add( "0, 7, 1, 1, 0,  , 0", KappaLayout.createVerticalStrut( 11, true ) );
			panel.add( "0, 8, 8, 1, 0, w, 0", okCancel );
			
			setContentPane(panel);
			pack();
		}
		
		/**
		 * for unit tests, mainly
		 */
		void init(String buffer, String oldSchema, boolean relativeEnabled)
		{
			buffer_path.setText(buffer);
			if(oldSchema == null)
			{
				path.setText(MiscUtilities.getParentOfPath(buffer));
			}
			else
			{
				path.setText(oldSchema);
			}
			relative_cb.setSelected(false);
			relative_cb.setEnabled(relativeEnabled);
			relativize();
		}
		
		/**
		 * shows the dialog and returns true if the user confirmed
		 * @param	buffer		path to the xml document needing a schema
		 * @param	oldSchema	path to the schema currently assigned or null
		 * @param	relativeEnabled	is a relative path enabled
		 */
		public boolean choose(String buffer, String oldSchema, boolean relativeEnabled)
		{
			confirmed = false;
			init(buffer,oldSchema, relativeEnabled);
			setVisible(true);
			return confirmed;
		}
		
		/**
		 * refuse to close the dialog if there is an error
		 */
		public void ok()
		{
			relativize();
			if(valid)
			{
				confirmed = true;
				setVisible(false);
			}
		}
		
		public void cancel() 
		{
			confirmed = false;
			setVisible(false);
		}
		
		/**
		* transform the contents of path into a relative URL, if relative_cb is checked.
		* TODO: handle a relative URL entered by the user
		* TODO: relativize jar:file:...!etc URLs
		*/
		private void relativize()
		{
			relative = relative_cb.isSelected();
        	try
        	{
        		schemaURL = new URI(pathToURL(path.getText()));
				if(relative)
				{
					String bufferURL = pathToURL(MiscUtilities.getParentOfPath(buffer_path.getText()));
					schemaURL = new URI(bufferURL).relativize(schemaURL);
				}
        		path.setForeground(Color.BLACK);
        		valid = true;
        		relative_path.setText(schemaURL.toString());
        	}
        	catch(URISyntaxException ue)
        	{
        		Log.log(Log.ERROR,SchemaMappingManager.class,ue);
        		path.setForeground(Color.RED);
        		valid = false;
        		relative_path.setText(ue.getMessage());
        	}
		}
	}
	// }}}
	
	//{{{ promptTypeIdForBuffer() method
	/**
	 * - gather all type Ids
	 * - let the user choose one
	 * - update the SchemaMapping
	 */
	public static void promptTypeIdForBuffer(View view, Buffer buffer)
	{
		// local schema-mapping
		String specificSchema = MiscUtilities.constructPath(
			MiscUtilities.getParentOfPath(
				buffer.getPath()),SchemaMapping.SCHEMAS_FILE);
		
		URI specificSchemaURI = null;
		
		try
		{
			specificSchemaURI = new URI(pathToURL(specificSchema));
		}
		catch(URISyntaxException mfue)
		{
			// TODO: react to the error 
			Log.log(Log.ERROR,SchemaMappingManager.class,mfue);
		}
		
		SchemaMapping lMapping = getLocalSchemaMapping(buffer, new LoggingErrorHandler(jEdit.getProperty("xml.local-schema")));
		SchemaMapping gMapping = getGlobalSchemaMapping(new LoggingErrorHandler(jEdit.getProperty("xml.global-schema")));
		SchemaMapping bMapping = getBuiltInSchemaMapping(new LoggingErrorHandler(jEdit.getProperty("xml.builtin-schema")));
		
		Map<String,SchemaMapping> allTypeIds = new HashMap<String,SchemaMapping>();
		
		String oldTypeId = null;
		
		for(SchemaMapping m: Arrays.asList(lMapping, gMapping, bMapping))
		{
			if(m != null)
			{
				for(SchemaMapping.TypeIdMapping tid : m.getTypeIds())
				{
					allTypeIds.put(tid.getId(),m);
				}
				
				if(oldTypeId == null)
				{
					oldTypeId = m.getTypeIdForDocument(buffer.getPath());
				}
			}
		}
		
		String[] tids = allTypeIds.keySet().toArray(new String[]{});
		if(tids.length == 0)
		{
			view.getToolkit().beep();
			view.getStatus().setMessage("xml.no-type-id.message");
			return;
		}
		
		String tid = (String)JOptionPane.showInputDialog(
			view,
			jEdit.getProperty("xml.choose-type-id.message"),
			jEdit.getProperty("xml.choose-type-id.title"),
            JOptionPane.OK_CANCEL_OPTION,
            null,
            tids,
            oldTypeId == null ? tids[0] : oldTypeId
            );
        
        if(tid!=null)
        {
        	SchemaMapping tidMapping = allTypeIds.get(tid);
			
        	if(isSchemaMappingEnabled(buffer))
        	{
				// no schemas.xml in the buffer's directory : will create one
				if(lMapping == null)
				{
					lMapping = new SchemaMapping(specificSchemaURI);
					if(gMapping != null)lMapping.ensureIncluded(gMapping);
				}
				
				if(lMapping != tidMapping)
				{
					lMapping.ensureIncluded(tidMapping);
				}
				
				String bufferURL = pathToURL(buffer.getPath());
				SchemaMapping.URIResourceRule newRule = new SchemaMapping.URIResourceRule(null,bufferURL,tid, true);
				
				lMapping.updateMapping(newRule);
				
				try
				{
					lMapping.toDocument(lMapping.getBaseURI().toString());
				}
				catch(IOException ioe)
				{
					// if saving fails, try to output it in a buffer
					JOptionPane.showMessageDialog(
						view,
						jEdit.getProperty("xml-error-to-document.message",new Object[]{ioe.getClass(),ioe.getMessage()}),
						jEdit.getProperty("xml-error-to-document.title"),
						JOptionPane.ERROR_MESSAGE
						);
					Buffer b = jEdit.newFile(view,lMapping.getBaseURI().toString());
					b.insert(0,lMapping.toString());
					view.getEditPane().setBuffer(buffer);
				}
				
			}
			else
			{
				SchemaMapping.Result res = tidMapping.resolveTypeId(tid);
				if(res == null)
				{
				}
				else
				{
					String schemaURL = res.baseURI.resolve(res.target).toString();
					buffer.setStringProperty(BUFFER_SCHEMA_PROP,schemaURL);
				}
			}
				
			// finally, use the new schema mapping
			view.getInputHandler().setRepeatCount(1);
			view.getInputHandler().invokeAction("sidekick-parse");
        }
	}
	//}}}
	
	//{{{ getGlobalSchemaMapping() method
	/**
	 * @param errorHandler	notified of errors loading the global schemas.xml
	 * @return	schema mapping rules in JEDIT_SETTINGS/plugins/xml.XMLPLugin/schemas.xml
	 */
	public static SchemaMapping getGlobalSchemaMapping(ErrorHandler errorHandler)
	{
		String schemaURL = jEdit.getProperty(SCHEMA_MAPPING_PROP);
		SchemaMapping mapping = null;
		// if schemaURL is null, it means that there is no settings directory
		if(schemaURL != null)
		{
			Log.log(Log.DEBUG,SchemaMappingManager.class,"global mapping="+schemaURL);
			try {
				mapping = SchemaMapping.fromDocument(schemaURL,errorHandler);
			} catch (IOException e) {
				mapping = null;
				Log.log(Log.ERROR,SchemaMappingManager.class,
						"Error loading global schema mapping: '"+schemaURL+"'",e);
			} catch (SAXException e) {
				mapping = null;
			}
		}
		return mapping;
	}
	//}}}
	
	//{{{ getBuiltInSchemaMapping() method
	/**
	 * @param errorHandler	notified of errors/warning loading builtin schema mapping (not likely)
	 * @return	mappings shipped with this plugin : jeditresource:XML.jar!/xml/dtds/schemas.xml
	 */
	public static SchemaMapping getBuiltInSchemaMapping(ErrorHandler errorHandler)
	{
		java.net.URL schemaURL = SchemaMappingManager.class.getClassLoader().getResource(BUILT_IN_SCHEMA);
		
		if(schemaURL == null)
		{
			throw new AssertionError("built-in schemas.xml cant be found !");
		}
		else
		{
			try {
				return SchemaMapping.fromDocument(schemaURL.toString(),errorHandler);
			} catch (IOException e) {
				Log.log(Log.ERROR,SchemaMappingManager.class,
						"Error loading local schema mapping : '"+schemaURL+"'",e);
				return null;
			} catch (SAXException e) {
				return null;
			}
		}
	}
	//}}}
	
	//{{{ getLocalSchemaMapping() method
	/**
	* @return schemas.xml next to buffer or null if it doesn't exist
	*/
	public static SchemaMapping getLocalSchemaMapping(Buffer buffer, ErrorHandler errorHandler)
	{
		SchemaMapping mapping;
		
		// local schema-mapping
		String specificSchema = MiscUtilities.constructPath(
			MiscUtilities.getParentOfPath(
				buffer.getPath()),SchemaMapping.SCHEMAS_FILE);
		
		// VFS-compatible
		try
		{
			URI uriSpecificSchema = new URI(PathUtilities.pathToURL(specificSchema));
			if(SchemaMapping.resourceExists(uriSpecificSchema))
			{
					String schemaURL = uriSpecificSchema.toString();
					mapping = SchemaMapping.fromDocument(schemaURL, errorHandler);
			}
			else
			{
				Log.log(Log.DEBUG, SchemaMappingManager.class,
					"no schemas.xml in "+specificSchema);
				mapping = null;
			}
		}
		catch(URISyntaxException ue)
		{
			// may happen if specificSchema is relative, but it should not be the case with buffer.getPath()
			Log.log(Log.ERROR,SchemaMappingManager.class,
				"Error converting path for fromDocument : '"+specificSchema+"'",ue);
			mapping = null;
		} catch (IOException e) {
			mapping = null;
			Log.log(Log.ERROR,SchemaMappingManager.class,
					"Error loading local schema mapping : '"+specificSchema+"'",e);
		} catch (SAXException e) {
			// logging has already been taken care of
			mapping = null;
		}
		return mapping;
	}
	//}}}
	
	//{{{ getSchemaMappingForBuffer() method
	/**
	* @return local SchemaMapping or global SchemaMapping or built-in SchemaMapping
	*/
	public static SchemaMapping getSchemaMappingForBuffer(Buffer buffer, ErrorHandler errorHandler)
	{
		SchemaMapping mapping;
		
		// local schema-mapping ; be sure not to continue on validation error by encapsulating the errorHandler in a LoggingErrorHandler
		mapping  = getLocalSchemaMapping(buffer, new LoggingErrorHandler(jEdit.getProperty(jEdit.getProperty("xml.local-schema")), errorHandler));
		
		if(mapping == null)
		{
			mapping = getGlobalSchemaMapping(new LoggingErrorHandler(jEdit.getProperty(jEdit.getProperty("xml.global-schema")), errorHandler));
		}
		
		if(mapping == null)
		{
			Log.log(Log.DEBUG, SchemaMappingManager.class,
				"no settings => using built-in schema mapping file");
			mapping = getBuiltInSchemaMapping(new LoggingErrorHandler(jEdit.getProperty(jEdit.getProperty("xml.builtin-schema")), errorHandler));
		}
		return mapping;
	}
	//}}}
	
	//{{{ initGlobalSchemaMapping() method
	/**
	 * Finds (and creates if needed) the global schema mapping (schemas.xml)
	 * file in the settings directory, for reading or overwriting it.
	 */
	public static void initGlobalSchemaMapping(View view){
		jEdit.unsetProperty(SCHEMA_MAPPING_PROP);
		
		File home = org.gjt.sp.jedit.EditPlugin.getPluginHome(xml.XmlPlugin.class);
		if(home == null)
		{
			// -nosettings
			return;
		}
		else if(!home.exists())
		{
			Log.log(Log.DEBUG,SchemaMappingManager.class, "creating settings directory");
			try
			{
				boolean created = home.mkdirs();
				if(!created)
				{
					GUIUtilities.error( view, "unable to create settings directory: "+home,null);
					return;
				}
			}
			catch(SecurityException se)
			{
				GUIUtilities.error( view, "unable to create settings directory (security exception): "+home,null);
				return;
			}
		}
		File schemas = new File(home,SchemaMapping.SCHEMAS_FILE);
		// create an empty mapping file in settings directory
		// it points to the global schemaMapping, to get all
		// the builtin rules
		if(!schemas.exists()){
			SchemaMapping builtinMapping = getBuiltInSchemaMapping(new LoggingErrorHandler(jEdit.getProperty("xml.builtin-schema")));
			SchemaMapping tmp = new SchemaMapping();
			tmp.ensureIncluded(builtinMapping);
			try{
				tmp.toDocument(schemas.toURI().toURL().toString());
			}catch(IOException ioe){
				Log.log(Log.ERROR,SchemaMappingManager.class,"Unable to save default RelaxNG mappings",ioe);
				return;
			}
		}
		try
		{
			jEdit.setProperty(SCHEMA_MAPPING_PROP,schemas.toURI().toURL().toString());
		}
		catch(java.net.MalformedURLException mfe)
		{
			Log.log(Log.ERROR,SchemaMappingManager.class,mfe);
		}
	}
	//}}}

	//{{{ isSchemaMappingEnabled() method
	public static boolean isSchemaMappingEnabled(Buffer b){
		return b.getBooleanProperty(BUFFER_ENABLE_SCHEMA_MAPPING_PROP);
	}
	//}}}
	
	//{{{ LoggingErrorHandler class
	/** logs errors and warnings to Activity Log and rethrow exception on error and fatalError to prevent
	 *  invalid documents to result in invalid SchemaMappings.
	 */
	public static class LoggingErrorHandler implements ErrorHandler{
		private String resourceName;
		private ErrorHandler errorHandler;

		public LoggingErrorHandler(String resourceName){
		}
		
		public LoggingErrorHandler(String property, ErrorHandler errorHandler) {
			this.resourceName = resourceName;
			this.errorHandler = errorHandler;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			Log.log(Log.WARNING, SchemaMappingManager.class, jEdit.getProperty("xml.SchemaMappingManager.error",new Object[]{resourceName,exception.getSystemId(),exception.getLineNumber(),exception.getLocalizedMessage()}));
			if(errorHandler != null)errorHandler.error(exception);
			throw exception;
		}
		
		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			Log.log(Log.WARNING, SchemaMappingManager.class, jEdit.getProperty("xml.SchemaMappingManager.fatal-error",new Object[]{resourceName,exception.getSystemId(),exception.getLineNumber(),exception.getLocalizedMessage()}));
			if(errorHandler != null)errorHandler.fatalError(exception);
			throw exception;
		}
		
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			Log.log(Log.NOTICE, SchemaMappingManager.class, jEdit.getProperty("xml.SchemaMappingManager.warning",new Object[]{resourceName,exception.getSystemId(),exception.getLineNumber(),exception.getLocalizedMessage()}));
			if(errorHandler != null)errorHandler.warning(exception);
		}
	}
}
