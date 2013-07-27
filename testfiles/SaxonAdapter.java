/*
 * Created on Dec 15, 2003
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */



package saxonadapter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.Loader;
import net.sf.saxon.StandardErrorListener;
import net.sf.saxon.event.Builder;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.QueryProcessor;
import net.sf.saxon.query.QueryResult;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trace.SimpleTraceListener;
import net.sf.saxon.type.Type;
import net.sf.saxon.xpath.XPathException;

import org.xml.sax.InputSource;

import xquery.Adapter;
import xquery.AdapterException;
import xquery.ContextualAdapterException;
import xquery.NonContextualAdapterException;
import xquery.XQueryGUI;

/**
 * @author Wim Le Page
 * @author Pieter Wellens
 * @version May 19, 2004
 */
public class SaxonAdapter implements Adapter {

	private Configuration config;
	private StaticQueryContext sQueryContex;
	private DynamicQueryContext dQueryContext;
	private Properties adapterProps;
	private Properties outputProps;
	private QueryProcessor queryProcessor;
	private StandardErrorListener listner = new StandardErrorListener();
	private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	private PrintStream errorStream = new PrintStream(byteStream);
	private String performance = "";

	
	/**
	 * Constructor for the Saxon adapter
	 */
	public SaxonAdapter() {
		adapterProps = new Properties();
		config = new Configuration();
		setOptions();
		config.setHostLanguage(Configuration.XQUERY);
		
		listner.setErrorOutput(errorStream);
		config.setErrorListener(listner);
		
		sQueryContex = new StaticQueryContext(config);
		queryProcessor = new QueryProcessor(sQueryContex);

		dQueryContext = new DynamicQueryContext();
		dQueryContext.setErrorListener(listner);
		outputProps = new Properties();
		if (XQueryGUI.getBooleanProperty("saxon.omitdecl")){
			outputProps.setProperty("omit-xml-declaration", "yes");
		} else {
			outputProps.setProperty("omit-xml-declaration", "no");
		}
		
	}
	

	/* (non-Javadoc)
	 * @see xquery.Adapter#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties prop) throws AdapterException {
		adapterProps = prop;		
	}

	
	/* (non-Javadoc)
	 * @see xquery.Adapter#loadContextFromFile(java.lang.String)
	 */
	public void loadContextFromFile(String contextPath) throws AdapterException {
		
		File sourceFile = null;
		Source sourceInput = null;
		try {
			if (contextPath.startsWith("http:") || contextPath.startsWith("file:")) {
                sourceInput = dQueryContext.getURIResolver().resolve(contextPath, null);
            } else {
                sourceFile = new File(contextPath);
                if (!sourceFile.exists() || !sourceFile.isFile()) {
                	throw new AdapterException(
        					"The path '"
        						+ contextPath
        						+ "' does not point to a file");
        					}
            }
            InputSource eis = new InputSource(sourceFile.toURI().toString());
            sourceInput = new SAXSource(eis);
           
			DocumentInfo docInfo = queryProcessor.buildDocument(sourceInput);
			dQueryContext.setContextNode(docInfo);
		} catch (XPathException e) {
			//e.printStackTrace();
			throw new AdapterException(
				"Failed to load XML file:\n" + e.getMessage(), e);
		} catch (TransformerException e) {
			//e.printStackTrace();
			throw new AdapterException(
				"Failed to load XML file:\n" + e.getMessage(), e);
		}
	}
		
	/* (non-Javadoc)
	 * @see xquery.Adapter#loadContextFromString(java.lang.String)
	 */
	public void loadContextFromString(String context) throws AdapterException {
		StringReader contextReader = new StringReader(context);
		InputSource eis = new InputSource(contextReader);
		Source  sourceInput = new SAXSource(eis);
       
		try {
			DocumentInfo docInfo = queryProcessor.buildDocument(sourceInput);
			dQueryContext.setContextNode(docInfo);
		} catch (XPathException e) {
			//e.printStackTrace();
			throw new AdapterException(
					"Failed to load XML file:\n" + e.getMessage(), e);
		}
		
	}
	
	
	/* (non-Javadoc)
	 * @see xquery.Adapter#setBaseUri(java.lang.String)
	 */
	public void setBaseUri(String uri) throws AdapterException {
			queryProcessor.getStaticContext().setBaseURI(uri);
	}


	/* (non-Javadoc)
	 * @see xquery.Adapter#evaluateFromString(java.lang.String)
	 */
	public String evaluateFromString(String queryString) throws AdapterException{
		String result = "";
		try {
			long startTime = (new Date()).getTime();
			
			XQueryExpression xqueryExpression =
				queryProcessor.compileQuery(queryString);
			
            if (XQueryGUI.getBooleanProperty("saxon.timing")) {
                long endTime = (new Date()).getTime();
                performance += "Compilation time: " + (endTime-startTime) + " milliseconds\n";
                startTime = endTime;
            }

			result = serialize(xqueryExpression);
			
		} catch (XPathException xe) {
			throwAdapterException();
		} catch (TransformerException e) {
			throw new NonContextualAdapterException(
					"Saxon could not evaluate XQuery:\n" + e.toString());
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see xquery.Adapter#evaluateFromFile(java.lang.String)
	 */
	public String evaluateFromFile(String queryPath){
		String result = "";
		try {
			FileReader queryReader = new FileReader(queryPath);
			long startTime = (new Date()).getTime();
			
			XQueryExpression xqueryExpression = queryProcessor.compileQuery(queryReader);
			
			if (XQueryGUI.getBooleanProperty("saxon.timing")) {
	            long endTime = (new Date()).getTime();
	            performance += "Compilation time: " + (endTime-startTime) + " milliseconds\n";
	            startTime = endTime;
	        }
			
			result = serialize(xqueryExpression);
			
		} catch (XPathException xe) {
			throwAdapterException();
		} catch (TransformerException e) {
			throw new NonContextualAdapterException(
					"Saxon could not evaluate XQuery:\n" + e.toString());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * @param xqueryExpression to serialize
	 * @return sertialized result
	 * @throws TransformerException
	 * @throws XPathException
	 */
	private String serialize(XQueryExpression xqueryExpression) throws TransformerException, XPathException {
		StringWriter stringwriter = new StringWriter();
		PrintWriter printwriter = new PrintWriter(stringwriter, true);
		
		String result;
        if (XQueryGUI.getBooleanProperty("saxon.explain")) {
        	sQueryContex.explainGlobalFunctions();
        	xqueryExpression.explain(sQueryContex.getNamePool());
            // TODO: provide "explain" output for variables
        }
		SequenceIterator sequenceIterator =
			xqueryExpression.iterator(dQueryContext);
		
		/* code adopted from the Saxon 7.9 command line XQuery by Michael Kay */
		while (true) {
		    Item item = sequenceIterator.next();
		    if (item == null) break;
		    if (item instanceof NodeInfo) {
		        switch (((NodeInfo)item).getNodeKind()) {
		        case Type.DOCUMENT:
		        case Type.ELEMENT:
		            // TODO: this is OK for constructed elements, but
		            // if the query retrieves an element from a source doc,
		            // we are outputting the whole document.
		            QueryResult.serialize((NodeInfo)item,
		                    new StreamResult(printwriter),
		                    outputProps);
		            printwriter.println("");
		            break;
		        case Type.ATTRIBUTE:
		            printwriter.println(((NodeInfo)item).getLocalPart() +
		                           "=\"" +
		                           item.getStringValue() +
		                           "\"");
		            break;
		        case Type.COMMENT:
		            printwriter.println("<!--" + item.getStringValue() + "-->");
		            break;
		        case Type.PROCESSING_INSTRUCTION:
		            printwriter.println("<?" +
		                           ((NodeInfo)item).getLocalPart() +
		                           " " +
		                           item.getStringValue() +
		                           "?>");
		            break;
		        default:
		            printwriter.println(item.getStringValue());
		        }
		    } else {
		        printwriter.println(item.getStringValue());
		    }
		}

		printwriter.close();
		result = stringwriter.getBuffer().toString();
		return result;
	}
	
	/**
	 * examines saxon output and throws approriate adapter exception
	 */
	private void throwAdapterException() {
		String test = byteStream.toString();
		try {
			
			Pattern pattern =
				Pattern.compile("on line [0-9]*");
			Matcher matcher = pattern.matcher(test);
			matcher.find();
			String characters = matcher.group();

			pattern = Pattern.compile("[0-9]+");
			matcher = pattern.matcher(characters);
			matcher.find();
			String lineNumber = matcher.group();
			int lineNumberInt = Integer.parseInt(lineNumber)-1;

			throw new ContextualAdapterException("Saxon could not evaluate XQuery:\n"  
				+ test, lineNumberInt, 0, 0 );

		} catch (IllegalStateException e) {
			throw new NonContextualAdapterException(
					"Saxon could not evaluate XQuery:\n" + test);	
		}
	}
	/**
	 * setting the options for of the saxon engine
	 *
	 */
	private void setOptions(){
		String saxonTree = XQueryGUI.getProperty("saxon.tree");
		if (saxonTree != null && saxonTree.equals("Standard Tree")) config.setTreeModel(Builder.STANDARD_TREE);
		else config.setTreeModel(Builder.TINY_TREE);
		
		config.setLineNumbering(XQueryGUI.getBooleanProperty("saxon.linenumber"));
		config.setAllowExternalFunctions(!XQueryGUI.getBooleanProperty("saxon.nojava"));
		
		//uriResolverClass
		if(XQueryGUI.getBooleanProperty("saxon.useuriclass")){
            String r = XQueryGUI.getProperty("saxon.uriclass");
			try {
				config.setURIResolver(makeURIResolver(r));
				dQueryContext.setURIResolver(makeURIResolver(r));
			} catch (TransformerException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Adapter Options warning", JOptionPane.WARNING_MESSAGE);
				//e.printStackTrace();
			}
		}
		
		config.setStripsAllWhiteSpace(XQueryGUI.getBooleanProperty("saxon.stripws"));
		
		/* version and timing */
		Loader.setTracing(XQueryGUI.getBooleanProperty("saxon.timing"));
		config.setTiming(XQueryGUI.getBooleanProperty("saxon.timing"));
		
		if (XQueryGUI.getBooleanProperty("saxon.tracequery")) config.setTraceListener(new SimpleTraceListener());
		config.setTraceExternalFunctions(XQueryGUI.getBooleanProperty("saxon.tracejava"));
		
		//useUrls is not an option
		
		//wrap is not an option
	}
	
	/* (non-Javadoc)
	 * @see xquery.Adapter#setPerformanceEnabled(boolean)
	 */
	public void setPerformanceEnabled(boolean enabled) throws AdapterException {
		//this is not neede by the Saxon adapter
	}

	/* (non-Javadoc)
	 * @see xquery.Adapter#getPerformance()
	 */
	public String getPerformance() throws AdapterException {
		return performance;
	}
	
    /**
     * Create an instance of a URIResolver with a specified class name
     *
     * @exception TransformerException if the requested class does not
     *     implement the javax.xml.transform.URIResolver interface
     * @param className The fully-qualified name of the URIResolver class
     * @return The newly created URIResolver
     */
    public static URIResolver makeURIResolver (String className)
    throws TransformerException
    {
        Object obj = Loader.getInstance(className);
        if (obj instanceof URIResolver) {
            return (URIResolver)obj;
        }
        throw new TransformerException("Class " + className + " is not a URIResolver");
    }

}
