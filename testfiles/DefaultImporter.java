package com.addictedtor.infonode.perspective.importer;

import java.util.HashMap;
import java.util.Vector;

import net.infonode.docking.DockingWindow;
import net.infonode.util.Direction;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Default abstract importer
 */
public abstract class DefaultImporter implements Importer {

	/**
	 * the node to import
	 */
	protected Node node;

	/**
	 * the attributes of the nodes
	 */
	protected NamedNodeMap attributes;

	/**
	 * the snapshot of the root window
	 */
	protected Snapshot snapshot;

	/**
	 * Constructor for the Default Importer
	 * 
	 * @param node
	 *            the node to parse
	 * @param snapshot
	 *            the snapshot of the current perspective
	 */
	public DefaultImporter(Node node, Snapshot snapshot) {
		this.node = node;
		this.snapshot = snapshot;
		this.attributes = node.getAttributes();
	}

	/**
	 * parse the node based on abstract methods createWindow that creates the
	 * current window and processChilds that appends child windows to the
	 * current window
	 * 
	 * @param node
	 *            the XML node to parse
	 * @return the DockingWindow that represent the node
	 */
	public DockingWindow parse(Node node) {
		DockingWindow window = createWindow();
		if (hasChilds()) {
			processChilds(window, getChildWindows());
		}
		finish(window);
		return window;
	}

	/**
	 * Returns a vector of the Docking below the current node
	 * 
	 * @return a vector of Docking window, each element of the vector is the
	 *         DockingWindow of a child node of the current node
	 */
	public Vector<DockingWindow> getChildWindows() {
		Vector<DockingWindow> childs = new Vector<DockingWindow>();
		NodeList nl = node.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				childs.add(ImporterProvider.getImporter(n, snapshot).parse(n));
			}
		}
		return childs;
	}

	/**
	 * Indicates if the current node has child element nodes
	 * 
	 * @return whether the current node has child nodes
	 */
	public boolean hasChilds() {
		NodeList nl = node.getChildNodes();
		if (nl.getLength() == 0)
			return false;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the (possibly empty) docking window for the current node
	 * 
	 * @return the docking window for the current node
	 */
	public abstract DockingWindow createWindow();

	/**
	 * Append the child windows to the window of the current node
	 */
	public abstract void processChilds(DockingWindow window,
			Vector<DockingWindow> childs);

	/**
	 * Performs the final steps on the created docking window after the child
	 * windows have been added
	 */
	public void finish(DockingWindow window) {
	}

	/**
	 * Returns the value of the name attribute
	 * 
	 * @param name
	 *            name of the attribute to extract
	 * @return the value of the attribute
	 */
	protected String getAttribute(String name) {
		String value = null;
		try {
			value = attributes.getNamedItem(name).getNodeValue();
		} catch (Exception e) {
		}
		return value;
	}

	/**
	 * returns the Direction associated with the direction string
	 * 
	 * @param direction
	 *            the string value of a direction, ie Up, Down, Left or Right
	 * @return the Direction value associated with this Direction
	 */
	protected Direction getDirection(String direction) {
		if (directionMap.containsKey(direction)) {
			return directionMap.get(direction);
		}
		return null;
	}

	/**
	 * mapping between string direction and the Direction class
	 */
	private static HashMap<String, Direction> directionMap = new HashMap<String, Direction>();
	static {
		directionMap.put("Up", Direction.UP);
		directionMap.put("Down", Direction.DOWN);
		directionMap.put("Left", Direction.LEFT);
		directionMap.put("Right", Direction.RIGHT);
	}

}
