package com.addictedtor.infonode.perspective.exporter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.infonode.docking.DockingWindow;

/**
 * Exports a DockingWindow tree to an xml file
 */
public abstract class DefaultExporter implements Exporter {

	/**
	 * window we are representing as xml
	 */
	protected DockingWindow window;

	/**
	 * current level of nesting, used to write the correct number of tab
	 * character in the xml tree
	 */
	protected static int level = 0;

	/**
	 * a new line character
	 */
	protected static String NEWLINE = "\n";

	/**
	 * a tabulation character
	 */
	protected static String TAB = "\t";

	/**
	 * Attributes to write within the opening tag
	 */
	private HashMap<String, String> attributes;

	/**
	 * Constructor for the abstract exporter, no attributes
	 * 
	 * @param window
	 *            the DockingWindow to export to an xml file
	 */
	public DefaultExporter(DockingWindow window) {
		this(window, new HashMap<String, String>());
	}

	/**
	 * Constructor for the abstract exporter, no attributes
	 * 
	 * @param window
	 *            the DockingWindow to export to an xml file
	 * @param attributes
	 *            Attributes to write to the opening xml tag
	 */
	public DefaultExporter(DockingWindow window,
			HashMap<String, String> attributes) {
		this.window = window;
		this.attributes = attributes;
	}

	/**
	 * Writes the opening tag to represent the window
	 * 
	 * @param out
	 *            the stream to write the tag to
	 */
	public void open(Writer out) {
		try {
			write(out, "<");
			write(out, getTag());
			if (attributes.size() > 0) {
				for (Map.Entry<String, String> entry : attributes.entrySet()) {
					write(out, " ");
					write(out, entry.getKey());
					write(out, "=\"");
					write(out, entry.getValue());
					write(out, "\"");
				}
			}
			if (!hasChilds()) {
				write(out, " /");
			}
			write(out, ">");
			write(out, NEWLINE);
		} catch (IOException ioe) {
		}
	}

	/**
	 * Writes the closing tag to represent the window
	 * 
	 * @param out
	 *            the stream to write the tag to
	 */
	public void close(Writer out) {
		if (hasChilds()) {
			try {
				write(out, "</");
				write(out, getTag());
				write(out, ">");
				write(out, NEWLINE);
			} catch (IOException ioe) {
			}
		}

	}

	/**
	 * Writes the childs of the window to the stream
	 * 
	 * @param out
	 *            the stream to write the childs window to
	 */
	public void content(Writer out) {
		for (int i = 0; i < window.getChildWindowCount(); i++) {
			ExporterProvider.getExporter(window.getChildWindow(i)).save(out);
		}
	}

	/**
	 * Writes the xml representation of the window layout
	 * 
	 * @param out
	 *            the stream to write the xml to
	 */
	@Override
	public void save(Writer out) {

		writeTabs(out);
		open(out);
		if (hasChilds()) {
			level++;
			content(out);
			level--;
			writeTabs(out);
		}
		close(out);
	}

	/**
	 * Writes as many tabulations as the current level of nesting
	 * 
	 * @param out
	 *            the stream to write to
	 */
	protected void writeTabs(Writer out) {
		try {
			for (int i = 0; i < level; i++) {
				write(out, TAB);
			}
		} catch (IOException ioe) {
		}
	}

	/**
	 * Indicates whether the window has child windows
	 * 
	 * @return does the window have child windows
	 */
	protected boolean hasChilds() {
		return window.getChildWindowCount() != 0;
	}

	/**
	 * Returns the name of the class of the window
	 * 
	 * @return base name of the window class, ie "RootWindow" for instances of
	 *         the RootWindow class
	 */
	protected String getTag() {
		return window.getClass().getSimpleName();
	}

	/**
	 * Adds an attribute to the attributes map
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param value
	 *            the value of the attribute
	 */
	public void setAttribute(String name, String value) {
		attributes.put(name, value);
	}

	protected void write(Writer out, String txt) throws IOException {
		if (txt != null) {
			out.write(txt);
		}
	}

}
