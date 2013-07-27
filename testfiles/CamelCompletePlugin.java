package com.illengineer.jcc.jedit;

import com.illengineer.jcc.*;

import java.util.*;
import java.io.*;

import org.gjt.sp.jedit.textarea.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.StatusBar;
import org.gjt.sp.jedit.gui.BeanShellErrorDialog;

import org.gjt.sp.jedit.bsh.Interpreter;

import javax.swing.JOptionPane;

public class CamelCompletePlugin extends EditPlugin {

	// {{{ Member Variables
	public static final String NAME = "camelcomplete";
	public static final String OPTION_PREFIX = "options.camelcomplete.";

	public static final String DEFAULT_ENGINE_NAME = "default";
	
	private static boolean debug = false;
	private static PrintWriter debugWriter;
	private static OptionPanel cachedOptionPanel;
	
	/*  This Map will contain all the options and configuration set in the OptionPane
	    Keys/Vals:
	      engines -> Map<String (enginename), (providers)>
	      	(providers) -> List<OptionPanel.OptionGroup>
	      engine-opts -> Map<String (enginename), OptionPanel.EngineOpts>
	      groups -> Map<String (groupname), List<OptionPanel.OptionGroup>>
	      cache -> Boolean
	      update -> Boolean
	      popup-rows -> Integer
	      remove-dups -> Boolean
	      loading-dlg -> Boolean
	*/
	private static HashMap<String,Object> optionsMap;
	
	private static HashMap<String,List<OptionPanel.OptionGroup>> enginesOptionsMap;
	private static HashMap<String, OptionPanel.EngineOpts> eoMap;
	private static HashMap<String,EngineGroup> engineMap;
	private static ArrayList<CompletionEngine> engines;
	
	
	private static File homeDir;
	// }}}
	
	// {{{ Lifecycle methods
	
	public void start() {
	    if (debug) {
		try {
		    debugWriter = new PrintWriter(new FileWriter(new File("/Users/jpavel/Prog/JCodeComplete/debug.log")));
		} catch (IOException ex) {}
	    }
	    
	    /* For debugging! */
	    // try {
	    
	    optionsMap = null;
	    InputStream i;

	    i = getResourceAsStream(CamelCompletePlugin.class, "options");
	    if (i == null)
		i = CamelCompletePlugin.class.getResourceAsStream("/default.options");
	    if (i != null) {
		boolean failed = false;
		try {
		    ObjectInputStream ois = new ObjectInputStream(i);
		    optionsMap = (HashMap<String,Object>)ois.readObject();
		    ois.close();
		} catch (IOException ex) {
		    failed = true;
		} catch (ClassNotFoundException ex) {
		    failed = true;
		}
		if (failed)
		    optionsMap = null;
	    }
	    if (optionsMap == null || !optionsMap.containsKey("engines")) {
		optionsMap = new HashMap<String,Object>();
		enginesOptionsMap = new HashMap<String,List<OptionPanel.OptionGroup>>();
		enginesOptionsMap.put(DEFAULT_ENGINE_NAME, new ArrayList<OptionPanel.OptionGroup>());
		optionsMap.put("engines", enginesOptionsMap);
	    } else {
		enginesOptionsMap = (HashMap<String,List<OptionPanel.OptionGroup>>)optionsMap.get("engines");
	    }
	    
	    engineMap = new HashMap<String,EngineGroup>();
	    engines = new ArrayList<CompletionEngine>();

	    if (!optionsMap.containsKey("cache"))
		optionsMap.put("cache", Boolean.TRUE);
	    if (!optionsMap.containsKey("update"))
		optionsMap.put("update", Boolean.FALSE);
	    if (!optionsMap.containsKey("simple-mode"))
		optionsMap.put("simple-mode", Boolean.TRUE);
	    if (!optionsMap.containsKey("simple-search-all"))
		optionsMap.put("simple-search-all", Boolean.FALSE);
	    if (!optionsMap.containsKey("simple-token-C-style"))
		optionsMap.put("simple-token-C-style", Boolean.TRUE);
	    if (!optionsMap.containsKey("popup-rows"))
		optionsMap.put("popup-rows", new Integer(12));
	    if (!optionsMap.containsKey("remove-dups"))
		optionsMap.put("remove-dups", Boolean.FALSE);
	    if (!optionsMap.containsKey("loading-dlg"))
		optionsMap.put("loading-dlg", Boolean.TRUE);
	    if (!optionsMap.containsKey("engine-opts")) {
		eoMap = new HashMap<String, OptionPanel.EngineOpts>();
		// We'll sync our maps.
		for (String engineName : enginesOptionsMap.keySet())
		    eoMap.put(engineName, new OptionPanel.EngineOpts());
		optionsMap.put("engine-opts", eoMap);
	    } else
		eoMap = (HashMap<String, OptionPanel.EngineOpts>)optionsMap.get("engine-opts");
		

	    MessageDialog mdlg = null;
	    if (((Boolean)optionsMap.get("loading-dlg")).equals(Boolean.TRUE)) {
		View v = jEdit.getActiveView();
		if (v != null) {
		    mdlg = new MessageDialog(jEdit.getActiveView());
		    mdlg.showDlg("CamelComplete", "Loading CamelComplete data...");
		}
	    }
	    
	    for (String engineName : enginesOptionsMap.keySet()) {
		EngineGroup eg = new EngineGroup();
		eg.engine = new CompletionEngine();
		eg.modified = false;
		

		if (((Boolean)optionsMap.get("cache")).booleanValue() && eoMap.get(engineName).enabled) {
		    i = getResourceAsStream(CamelCompletePlugin.class, "cache/engine-"+engineName);
		    if (i != null) {
			try {
			    eg.engine.deserializeData(i);
			    i.close();
			} catch (IOException ex) {
			    eg.engine = new CompletionEngine();
			}
		    }
		}
		engineMap.put(engineName, eg);
		engines.add(eg.engine);
	    }
	    if (mdlg != null) {
		mdlg.closeDlg();
		mdlg = null;
	    }
	    
	    homeDir = getPluginHome();
	    
	    if (((Boolean)optionsMap.get("update")).booleanValue())
		processConfiguration();
	    
	    /* For debugging! */
	    // } catch (Exception ex) {
		// debugPrintStacktrace(ex);
	    // }
	}
	
	public void stop() {
	    // Persist CompletionEngine data and optionsMap
	    OutputStream out;
	    
	    if (((Boolean)optionsMap.get("cache")).booleanValue()) {
		for (String engineName : engineMap.keySet()) {
		    if (!eoMap.get(engineName).enabled) continue;
		    EngineGroup eg = engineMap.get(engineName);
		    if (eg.modified) {
			out = getResourceAsOutputStream(CamelCompletePlugin.class, "cache/engine-"+engineName);
			if (out != null) {
			    try {
				eg.engine.serializeData(out);
				out.close();
			    } catch (IOException ex) {}
			}
		    }
		}
	    }
	    
	    out = getResourceAsOutputStream(CamelCompletePlugin.class, "options");
	    if (out != null) {
		try {
		    ObjectOutputStream oos = new ObjectOutputStream(out);
		    oos.writeObject(optionsMap);
		    oos.close();
		} catch (IOException ex) {
		}
	    }
	    
	    if (debug)
		debugWriter.close();

	    optionsMap = null;
	    enginesOptionsMap = null;
	    eoMap = null;
	    engineMap = null;
	    engines = null;
	    
	    if (cachedOptionPanel != null) {
		cachedOptionPanel.forgetStaticThings();
		cachedOptionPanel = null;
	    }
	    
	    // And just for the heck of it.
	    debugWriter = null;
	    homeDir = null;
	}
	
	static void rememberOptionPanel(OptionPanel panel) {
	    cachedOptionPanel = panel;
	}
	
	// }}}
	
	// {{{ processConfiguration() & helpers
	
	/**
	    This routine reads all the configuration options in the optionsMap and creates the
	    appropriate IdentifierProviders and Tokenizers, and sends them to the CompletionEngine.
	*/
	public static void processConfiguration() {
	    for (String engineName : enginesOptionsMap.keySet())
		processConfiguration(engineName);
	    
	}  
	
	public static void processConfiguration(String engineName) {
	    OptionPanel.EngineOpts eo = eoMap.get(engineName);
	    if (eo != null && !eo.enabled)
		return;
		
	    EngineGroup eg;
	    if (engineMap.containsKey(engineName))
		eg = engineMap.get(engineName);
	    else {
		eg = new EngineGroup();
		eg.engine = new CompletionEngine();
		eg.modified = false;
		engineMap.put(engineName, eg);
		engines.add(eg.engine);
	    }

	    List<OptionPanel.OptionGroup> groups = 
			(List<OptionPanel.OptionGroup>)enginesOptionsMap.get(engineName);
	    if (groups != null) {
		IdentifierProvider provider = null;
		ArrayList<Tokenizer> tokenizers;
		eg.engine.clearTokens();
		for (OptionPanel.OptionGroup og : groups) {
		    tokenizers = new ArrayList<Tokenizer>(og.tokenizers.size());
		    
		    if (og.provider.equals("ctags"))
			provider = new CTagsFileProvider(new File(og.fileName));
		    else if (og.provider.equals("jar"))
			provider = new JarFileProvider(new File(og.fileName));
		    else if (og.provider.equals("text"))
			provider = new TextFileProvider(new File(og.fileName));
		    else if (og.provider.equals("code")) {
			boolean failed = false;
			Interpreter bsh = new Interpreter();
			try {
			    bsh.eval("import com.illengineer.jcc.*;");
			    bsh.eval("import com.illengineer.jcc.jedit.*;");
			    bsh.eval("IdentifierProvider __ip;");
			    bsh.eval("__ip = (IdentifierProvider)" + og.extra);
			    provider = (IdentifierProvider)bsh.get("__ip");
			} catch (org.gjt.sp.jedit.bsh.EvalError ex) {
			    failed = true;
			    View v = jEdit.getActiveView();
			    if (v != null)
				new BeanShellErrorDialog(v, ex);
			    debugPrintStacktrace(ex);
			} catch (Exception ex) {
			    failed = true;
			    View v = jEdit.getActiveView();
			    if (v != null)
				new BeanShellErrorDialog(v, ex);
			    debugPrintStacktrace(ex);
			}
			if (failed)
			    provider = new NullProvider();
		    } else if (og.provider.equals("buffer")) {
			provider = new BufferWordsProvider(og.extra, 
			    (og.config != null ? ((Boolean)og.config).booleanValue() : true));
		    }
		    for (String [] t : og.tokenizers) {
			if (t[0].equals("camelcase"))
			    tokenizers.add(new CamelCaseTokenizer());
			else if (t[0].equals("regex")) 
			    tokenizers.add(new RegexpTokenizer(t[1], t[2].equals("y")));
		    }
		    provider.process();
		    eg.engine.loadIdentifiers(provider, tokenizers, og.minparts, og.maxparts,
					      og.ignoreCase, og.filterRegex);
		    provider.forget();
		}
	    }
	    eg.modified = true;
	}
	
	public static void deleteEngine(String engineName) {
	    if (enginesOptionsMap.size() <= 1)
		return;
		
	    enginesOptionsMap.remove(engineName);
	    eoMap.remove(engineName);
	    EngineGroup eg = engineMap.remove(engineName);
	    if (eg != null)
		engines.remove(eg.engine);
	}
	
	// }}}
	
	// {{{ Option routines
	public static void setOption(String key, Object val) {
	    optionsMap.put(key, val);
	}
	
	public static Object getOption(String key) {
	    return optionsMap.get(key);
	}
	// }}}
	
	// {{{ completion methods
	public static void complete(View view, JEditTextArea textArea, int completionType) {
	    // completionTypes: 1 = CamelCase, 2 = Normal, 3 = Total
	    CompleteWord.completeWord(view, completionType, null);
	}
	
	public static void complete(View view, JEditTextArea textArea, int completionType,
				    List<String> engineNames) {
	    // completionTypes: 1 = CamelCase, 2 = Normal, 3 = Total
	    CompleteWord.completeWord(view, completionType, engineNames);
	}

	public static void simpleComplete(View view, JEditTextArea textArea) {
	    if (isEngineEnabled("View Buffers") && isEngineEnabled("Buffers")) {
		processConfiguration("View Buffers");
		ArrayList l = new ArrayList(1);
		l.add("View Buffers");
		if (((Boolean)CamelCompletePlugin.getOption("simple-search-all")).booleanValue()) {
		    processConfiguration("Buffers");
		    l.add("Buffers");
		}
		CamelCompletePlugin.complete(view, textArea, 1, l);
	    } else {
		JOptionPane.showMessageDialog(jEdit.getFirstView(),
		    "CamelComplete is not configured for simple usage.\n" +
		    "Go to Plugin Options to set simple mode.",
		    "CamelComplete", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
	public static List<String> getCompletions(String word, List<String> engineNames) {
	    TreeSet<String> t = new TreeSet<String>();
	    _addCompletions(word, t, engineNames);
	    t.remove(word);  // just in case
	    return new ArrayList<String>(t);
	}
	
	public static List<String> getNormalCompletions(String word, List<String> engineNames) {
	    TreeSet<String> t = new TreeSet<String>();
	    _addNormalCompletions(word, t, engineNames);
	    t.remove(word);  // just in case
	    return new ArrayList<String>(t);
	}
	
	public static List<String> getTotalCompletions(String word, List<String> engineNames) {
	    TreeSet<String> t = new TreeSet<String>();
	    _addCompletions(word, t, engineNames);
	    _addNormalCompletions(word, t, engineNames);
	    t.remove(word);
	    return new ArrayList<String>(t);
	}
	
	private static void _addCompletions(String word, TreeSet<String> t, List<String> engineNames) {
	    Iterable<String> is = (engineNames != null ? engineNames : eoMap.keySet());
	    for (String engineName : is) {
		OptionPanel.EngineOpts eo = eoMap.get(engineName);
		if (eo.enabled) {
		    EngineGroup eg = engineMap.get(engineName);
		    if (eg != null) {
			CompletionEngine engine = eg.engine;
			List<String> c = engine.complete(word, false);
			if (c != null)
			    t.addAll(c);
		    }
		}
	    }
	}
	
	private static void _addNormalCompletions(String word, TreeSet<String> t, List<String> engineNames) {
	    Iterable<String> is = (engineNames != null ? engineNames : eoMap.keySet());
	    for (String engineName : is) {
		OptionPanel.EngineOpts eo = eoMap.get(engineName);
		if (eo.enabled && eo.normalCompletionMode != 0) {
		    EngineGroup eg = engineMap.get(engineName);
		    if (eg == null) continue;
		    CompletionEngine engine = eg.engine;
		    List<String> ids = engine.getIdentifiers();
		    for (String id : ids) {
			if (eo.normalCompletionMode == 1) { // starts with
			    if (id.startsWith(word))
				t.add(id);
			} else if (eo.normalCompletionMode == 2) {  // contains
			    if (id.contains(word))
				t.add(id);
			}
		    }
		}
	    }
	}
	
	// }}}
	
	// {{{ Debugging methods
	public static void debugPrint(String s) {
	    if (debug) {
		debugWriter.println(s);
		debugWriter.flush();
	    }
	}

	public static void debugPrintStacktrace(Exception ex) {
	    if (debug) {
		ex.printStackTrace(debugWriter);
		debugWriter.flush();
	    }
	}

	// }}}
	
	// {{{ Inner Classes
	private static class EngineGroup
	{
	    CompletionEngine engine;
	    boolean modified;
	}
	// }}}

	// {{{ Misc public methods
	public static Set<String> getEngineNames() {
	    return enginesOptionsMap.keySet();
	}
	
	public static boolean isEngineEnabled(String engineName) {
	    return (eoMap.containsKey(engineName) && eoMap.get(engineName).enabled);
		
	}
	
	public static void clearCacheDir() {
	    if (homeDir != null) {
		File cacheDir = new File(homeDir, "cache");
		if (cacheDir.exists()) {
		    File [] caches = cacheDir.listFiles();
		    for (File f : caches)
			f.delete();
		}
	    }
	}
	// }}}
}

//:folding=explicit:collapseFolds=1: