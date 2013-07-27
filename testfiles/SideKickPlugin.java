/*
 * SideKickPlugin.java
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2003 Slava Pestov
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

package sidekick;

//{{{ Imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.EditPaneUpdate;
import org.gjt.sp.jedit.msg.PluginUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.ViewUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.SwingWorker;
//}}}

/**
 * SideKick plugin core class
 * Manages a mapping of View to SideKick instances, creating/destroying
 * SideKick objects whenever Views are created/destroyed.  
 * 
 * @version $Id: SideKickPlugin.java 20353 2011-11-16 08:04:45Z kpouer $
 */
public class SideKickPlugin extends EditPlugin
{
	private static final String MARKER_SETS_PLUGIN = "marker.MarkerSetsPlugin";
	private static final String SHOW_TOOL_BAR = "sidekick.showToolBar";

	/** The name of the dockable */
	public static final String NAME = "sidekick-tree";
	
	//{{{ Some constants
	public static final String PARSER_MODE_PROPERTY = "sidekick.parser-mode";
	public static final String PARSER_PROPERTY = "sidekick.parser";
	public static final String PARSED_DATA_PROPERTY = "sidekick.parsed-data";
	public static final String PARSE_COUNT = "sidekick.parse-count";
	//}}}
	public static final String NONE="None";
	public static final String DEFAULT = "default parser";

	//{{{ Private members
	private static final String MACRO_PATH = "/macros";
	private static Map<View, SideKick> sidekicks;
	private static Map<String, SideKickParser> parsers;
	private static Executor executor;
	private static Set<Buffer> parsedBufferSet;
	private static Map<View, SideKickToolBar> toolBars;
	private static boolean toolBarsEnabled;
	private static Map<View, SwingWorker<SideKickParsedData, Object>> workers;
	private static EditPlugin markerSetsPlugin;

	//{{{ start() method
	public void start()
	{
		BeanShell.getNameSpace().addCommandPath(MACRO_PATH, getClass());
		markerSetsPlugin = jEdit.getPlugin(MARKER_SETS_PLUGIN, false);
		sidekicks = new HashMap<View, SideKick>();
		parsers = new HashMap<String, SideKickParser>();
		workers = new HashMap<View, SwingWorker<SideKickParsedData, Object>>();
		parsedBufferSet = new HashSet<Buffer>();
		toolBars = new HashMap<View, SideKickToolBar>();
		toolBarsEnabled = jEdit.getBooleanProperty(SHOW_TOOL_BAR);
		View view = jEdit.getFirstView();
		while(view != null)
		{
			initView(view);
			EditPane[] panes = view.getEditPanes();
			for(int i = 0; i < panes.length; i++)
				initTextArea(panes[i].getTextArea());
			view = view.getNext();
		}
		jEdit.addActionSet(SideKickMenuProvider.getParserSwitchers());
		SideKickActions.propertiesChanged();
		EditBus.addToBus(this);
	} //}}}

	//{{{ stop() method
	public void stop()
	{
		EditBus.removeFromBus(this);
		jEdit.removeActionSet(SideKickMenuProvider.getParserSwitchers());
		View view = jEdit.getFirstView();
		while(view != null)
		{
			uninitView(view);
			SideKickParsedData.setParsedData(view,null);

			EditPane[] panes = view.getEditPanes();
			for(int i = 0; i < panes.length; i++)
				uninitTextArea(panes[i].getTextArea());
			view = view.getNext();
		}
		
		Buffer buffer = jEdit.getFirstBuffer();
		while(buffer != null)
		{
			buffer.setProperty(PARSED_DATA_PROPERTY,null);
			buffer = buffer.getNext();
		}
		sidekicks = null;
		parsers = null;
		parsedBufferSet = null;
		toolBars = null;
		workers = null;
	} //}}}

	//{{{ handleViewUpdate() method
	@EBHandler
	public void handleViewUpdate(ViewUpdate vu)
	{
		View view = vu.getView();

		if(vu.getWhat() == ViewUpdate.CREATED)
			initView(view);
		else if(vu.getWhat() == ViewUpdate.CLOSED)
			uninitView(view);
	} //}}}

	//{{{ handleEditPaneUpdate() method
	@EBHandler
	public void handleEditPaneUpdate(EditPaneUpdate epu)
	{
		EditPane editPane = epu.getEditPane();

		if(epu.getWhat() == EditPaneUpdate.CREATED)
			initTextArea(editPane.getTextArea());
		else if(epu.getWhat() == EditPaneUpdate.DESTROYED)
			uninitTextArea(editPane.getTextArea());
	} //}}}

	//{{{ handleBufferUpdate() method
	@EBHandler
	public void handleBufferUpdate(BufferUpdate bu)
	{
		if(bu.getWhat() == BufferUpdate.CLOSED)
			finishParsingBuffer(bu.getBuffer());
	} //}}}

	//{{{ handlePropertiesChanged() method
	@EBHandler
	public void handlePropertiesChanged(PropertiesChanged msg)
	{
		SideKickActions.propertiesChanged();
		boolean showToolBar = jEdit.getBooleanProperty(SHOW_TOOL_BAR);
		if (showToolBar != toolBarsEnabled)
		{
			toolBarsEnabled = showToolBar;
			for (View v: jEdit.getViews())
			{
				if (toolBarsEnabled)
					attachToolBar(v);
				else
					detachToolBar(v);
			}
		}
	} //}}}

    // {{{ handlePluginUpdate() method
    @EBHandler
    public void handlePluginUpdate(PluginUpdate msg)
    {
    	EditPlugin plugin = msg.getPluginJAR().getPlugin();
    	if (plugin == null)
    		return;
    	if (plugin.getClassName().equals(MARKER_SETS_PLUGIN))
    	{
            if (msg.getWhat() == PluginUpdate.ACTIVATED)
            	markerSetsPlugin = plugin;
            else if (msg.getWhat() == PluginUpdate.DEACTIVATED)
            	markerSetsPlugin = null;
    	}
    } //}}}

    // {{{ getMarkerSetsPlugin() method
    public static EditPlugin getMarkerSetsPlugin()
    {
    	return markerSetsPlugin;
    } //}}}

	/**
	 * Returns the parser for the given mode.
	 *
	 * @param m the mode (it must not be null)
	 * @return the parser associated to this mode (or null if there is no parser)
	 */
	public static SideKickParser getParserForMode(Mode m) {
		String modeStr = m.getName();
		String propName = "mode." + modeStr + '.' + SideKickPlugin.PARSER_PROPERTY;
		String parserName = jEdit.getProperty(propName);

		if (parserName == null)
			return null;
		
		SideKickParser parser = (SideKickParser) ServiceManager.getService(
			SideKickParser.SERVICE, parserName);
		return parser;
	}
	

	//{{{ getParser() method
	/**
	 * @param name - the name of the parser, as defined in services.xml
	 */
	public static SideKickParser getParser(String name)
	{
		SideKickParser parser = (SideKickParser)ServiceManager
			.getService(SideKickParser.SERVICE,name);
		if(parser != null)
			return parser;
		else
			return parsers.get(name);
	} //}}}

	//{{{ getParserForView() method
	public static SideKickParser getParserForView(View view)
	{
		SideKick sidekick = sidekicks.get(view);
		if(sidekick == null)
			return null;
		else
			return sidekick.getParser();
	} //}}}

	/**
	 * 
	 * @param buffer
	 * @param parserName the new parser we want to use
	 * @since Sidekick 0.6
	 */
	public static void setParserForBuffer(Buffer buffer, String parserName) 
	{
		if (parserName.equals(NONE) ) {
			buffer.setStringProperty(PARSER_PROPERTY, parserName);
			return;
		}
		if (parserName.equals(DEFAULT)) {
			buffer.unsetProperty(PARSER_PROPERTY);
			return;
		}
		SideKickParser newParser = getParser(parserName);
		if (newParser != null) {
			buffer.setStringProperty(PARSER_PROPERTY, parserName);
		}
		else throw new RuntimeException("Unknown parser: " + parserName);
	}
	
	//{{{ getParserForBuffer() method
	public static SideKickParser getParserForBuffer(Buffer buffer)
	{
		String parserName = buffer.getStringProperty(PARSER_PROPERTY);
		Mode mode = buffer.getMode();
		String modeName = (mode != null) ? mode.getName() : "";
		buffer.setStringProperty(PARSER_MODE_PROPERTY, modeName);
		if(parserName == null || parserName.equals(DEFAULT) || parserName.length() == 0) {
			if (mode != null) 
				return getParserForMode(mode);
			else return null;
		}
		if (parserName.equals(NONE)) {
			return null;
		}
		return getParser(parserName);
	} //}}}

	//{{{ parse() method
	/**
	 * Immediately begins parsing the current buffer in a background thread.
	 * @param view The view
	 * @param showParsingMessage Clear the tree and show a status message
	 * there?
	 */
	public static void parse(View view, boolean showParsingMessage)
	{
		SideKick sidekick = sidekicks.get(view);
		if (sidekick == null) return;
		// Had to remove this 
		sidekick.setParser(view.getBuffer());
		sidekick.parse(showParsingMessage);
	} //}}}

	public static void execute(Runnable runnable)
	{
		if (executor == null)
		{
			executor = Executors.newSingleThreadExecutor();
		}
		executor.execute(runnable);
	}
	
	public static void execute(View view, SwingWorker<SideKickParsedData, Object> worker) 
	{
		// QUESTION: there should be only one worker per view. Is it possible
		// there could be more than one?
		// ANSWER: No. 
		workers.put(view, worker);
		worker.execute();
	}

	public static void cleanup(View view)
	{
		workers.remove(view);
	}

	//{{{ isParsingBuffer()
	public static boolean isParsingBuffer(Buffer buffer)
	{
		return parsedBufferSet.contains(buffer);
	} //}}}

	//{{{ Package-private members

	//{{{ startParsingBuffer()
	static void startParsingBuffer(Buffer buffer)
	{
		parsedBufferSet.add(buffer);
	} //}}}

	//{{{ finishParsingBuffer()
	static void finishParsingBuffer(Buffer buffer)
	{
		parsedBufferSet.remove(buffer);
	} //}}}

	//}}}

	//{{{ attachToolBar() method
	private static void attachToolBar(View view)
	{
		SideKickToolBar toolBar = new SideKickToolBar(view);
		view.addToolBar(toolBar);
		toolBars.put(view, toolBar);
	} //}}}

	//{{{ detachToolBar() method
	private static void detachToolBar(View view)
	{
		SideKickToolBar toolBar = toolBars.remove(view);
		if (toolBar != null)
		{
			view.removeToolBar(toolBar);
			toolBar.dispose();
		}
	} //}}}
	
	//{{{ initView() method
	private static void initView(View view)
	{
		SideKick sideKick = new SideKick(view);
		sidekicks.put(view, sideKick);
		sideKick.parse(true);
		if (toolBarsEnabled)
			attachToolBar(view);
	} //}}}

	// {{{ getSideKick() method
	static SideKick getSideKick(View v) {
		return sidekicks.get(v);
	}
	// }}}
	
	//{{{ uninitView() method
	private static void uninitView(View view)
	{
		SideKick sidekick = sidekicks.get(view);
		sidekick.dispose();
		sidekicks.remove(view);
		stop(view);
		workers.remove(view);
		detachToolBar(view);
	} //}}}

	
	
	//{{{ initTextArea() method
	private static void initTextArea(JEditTextArea textArea)
	{
		SideKickBindings b = new SideKickBindings();
		textArea.putClientProperty(SideKickBindings.class,b);
		textArea.addKeyListener(b);
	} //}}}

	//{{{ uninitTextArea() method
	private static void uninitTextArea(JEditTextArea textArea)
	{
		SideKickBindings b = (SideKickBindings)
			textArea.getClientProperty(
			SideKickBindings.class);
		textArea.putClientProperty(SideKickBindings.class,null);
		textArea.removeKeyListener(b);
		SideKickTree.CaretHandler caretHandler = (SideKickTree.CaretHandler) textArea.getClientProperty(SideKickTree.CaretHandler.class);
		if (caretHandler != null)
		{
			textArea.putClientProperty(SideKickTree.CaretHandler.class, null);
			textArea.removeCaretListener(caretHandler);
		}
	} //}}}

	public static void stop(View view) {
		SwingWorker<SideKickParsedData, Object> worker = workers.get(view);
		if (worker != null && !worker.isCancelled() && !worker.isDone()) {
			worker.cancel(true);	
		}
	}

	//}}}
}
