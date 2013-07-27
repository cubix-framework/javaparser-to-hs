/*
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2009, 2011 Matthieu Casanova
 * Copyright (C) 2009, 2011 Shlomy Reinstein
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package gatchan.jedit.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.io.VFS;
import org.gjt.sp.jedit.io.VFSFile;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.msg.PluginUpdate;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.IOUtilities;
import org.gjt.sp.util.Log;

import gatchan.jedit.lucene.Index.FileProvider;

import org.gjt.sp.util.ProgressObserver;
import org.gjt.sp.util.Task;
import org.gjt.sp.util.ThreadUtilities;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matthieu Casanova
 */
public class LucenePlugin extends EditPlugin
{
	public static final String LUCENE_DEFAULT_ANALYZER = "lucene.default-analyzer";
	static CentralIndex CENTRAL;
	private static final String CENTRAL_INDEX_NAME = "__CENTRAL__";
	private static final String INDEXES_FILE_NAME = "indexes.cfg";
	private static final String SEARCH_DOCKABLE_NAME = "lucene-search";
	private final Map<String, Index> indexMap = new HashMap<String, Index>();
	private ProjectWatcher projectWatcher;
	
	public static LucenePlugin instance;

	@Override
	public void start()
	{
		instance = this;
		File home = getPluginHome();
		CENTRAL = new CentralIndex(new File(home, CENTRAL_INDEX_NAME));
		EditBus.addToBus(CENTRAL);
		loadIndexes();
		EditPlugin p = jEdit.getPlugin("projectviewer.ProjectPlugin", false);
		if (p != null && !(p instanceof Deferred))
			projectWatcher = new ProjectWatcher();
		EditBus.addToBus(this);
	}

	@EBHandler
	public void handlePluginUpdate(PluginUpdate pluginUpdate)
	{
		if (pluginUpdate.getWhat() == PluginUpdate.ACTIVATED)
		{
			if ("projectviewer.ProjectPlugin".equals(pluginUpdate.getPluginJAR().getPlugin().getClassName()))
			{
				if (projectWatcher == null)
				{
					projectWatcher = new ProjectWatcher();
				}
			}
		}
		else if (pluginUpdate.getWhat() == PluginUpdate.DEACTIVATED)
		{
			if ("projectviewer.ProjectPlugin".equals(pluginUpdate.getPluginJAR().getPlugin().getClassName()))
			{
				if (projectWatcher != null)
				{
					projectWatcher.stop();
					projectWatcher = null;
				}
			}
		}
	}

	private void loadIndexes()
	{
		File f = new File(getPluginHome(), INDEXES_FILE_NAME);
		if (! f.exists())
			return;
		BufferedReader reader = null;
		try
		{
			Pattern pattern = Pattern.compile(",");
			reader = new BufferedReader(new FileReader(f));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String name = line;
				line = reader.readLine();
				if (line == null)
					break;
				String [] parts = pattern.split(line);
				/* analyzerIndex is for backward-compatibility. In the past, there
				 * were two types of index - file index and line index. The type
				 * and the analyzer were both specified on the same line in the
				 * meta-index file, separated by comma. Now, only the analyzer is
				 * specified.
				 */
				int analyzerIndex = (parts.length == 2) ? 1 : 0;
				String analyzer = parts[analyzerIndex];
				createIndex(name, analyzer);
			}
		}
		catch (Exception e)
		{
			Log.log(Log.ERROR, this, e);
		}
		finally
		{
			IOUtilities.closeQuietly(reader);
		}
	}
	private void saveIndexes()
	{
		File home = getPluginHome();
		if (home == null || ((!home.exists()) && (!home.mkdirs())))
			return;
		
		File f = new File(home, INDEXES_FILE_NAME);
		PrintWriter writer = null;
		try
		{
			writer = new PrintWriter(new FileWriter(f));
			for (Map.Entry<String, Index> stringIndexEntry : indexMap.entrySet())
			{
				writer.println(stringIndexEntry.getKey());
				Index index = stringIndexEntry.getValue();
				String analyzer = AnalyzerFactory.getAnalyzerName(index.getAnalyzer());
				writer.println(analyzer);
			}
		}
		catch (Exception e)
		{
			Log.log(Log.ERROR,  this, e);
		}
		finally
		{
			IOUtilities.closeQuietly(writer);
		}
	}

	@Override
	public void stop()
	{
		EditBus.removeFromBus(this);
		if (projectWatcher != null)
		{
			projectWatcher.stop();
			projectWatcher = null;
		}
		EditBus.removeFromBus(CENTRAL);
		CENTRAL.close();
		CENTRAL = null;
		instance = null;
		closeAllIndexes();
		indexMap.clear();
		AnalyzerFactory.dispose();
	}

	/**
	 * Close all opened indexes.
	 * This release memory
	 */
	public void closeAllIndexes()
	{
		Log.log(Log.DEBUG, this, "closeAllIndexes");
		Collection<Index> indexCollection = indexMap.values();
		for (Index index : indexCollection)
		{
			index.close();
		}
	}

	/**
	 * Return an index, or null.
	 *
	 * @param name the name of the index
	 * @return the index or null if there is no settings directory.
	 */
	public Index getIndex(String name)
	{
		if (getIndexFile(name) == null)
			return null;
		return indexMap.get(name);
	}

	public Index getIndexForProject(String name)
	{
		Index index = getIndex(name);
		if (index == null)
		{
			NewIndexDialog dlg = new NewIndexDialog(jEdit.getActiveView(), name);
			dlg.setVisible(true);
			if (dlg.accepted())
			{
				index = createIndex(dlg.getIndexName(), dlg.getIndexAnalyzer());
				jEdit.setProperty(LUCENE_DEFAULT_ANALYZER, dlg.getIndexAnalyzer());
			}
		}
		return index;
	}

	public Index createIndex(String name, String analyzerName)
	{
		Index index = getIndex(name);
		if (index != null)
			return index;

		File path = getIndexFile(name);
		index = new IndexImpl(name, path);
		Analyzer analyzer = AnalyzerFactory.getAnalyzer(analyzerName);
		if (analyzer != null)
			index.setAnalyzer(analyzer);
		indexMap.put(name, index);
		CentralIndex.createIndex(index);
		saveIndexes();
		if (!path.exists())
			path.mkdirs();
		return index;
	}

	/**
	 * Delete an index.
	 *
	 * @param name the name of the index.
	 */
	public void removeIndex(String name)
	{
		Index index = indexMap.remove(name);
		saveIndexes();
		if (index != null)
			index.close();
		File indexFile = getIndexFile(name);
		if (indexFile == null)
			return;

		VFS vfs = VFSManager.getVFSForPath(name);
		Object session = vfs.createVFSSession(indexFile.getAbsolutePath(), jEdit.getActiveView());
		try
		{
			vfs._delete(session, indexFile.getAbsolutePath(), jEdit.getActiveView());
		}
		catch (IOException e)
		{
			Log.log(Log.ERROR, this, e);
		}
		finally
		{
			try
			{
				vfs._endVFSSession(session, jEdit.getActiveView());
			}
			catch (IOException e)
			{
				Log.log(Log.ERROR, this, e);
			}
		}
		CENTRAL.removeIndex(name);		
	}

	public String chooseIndex()
	{
		String[] names = getIndexes();
		String name = (String) JOptionPane.showInputDialog(jEdit.getActiveView(), "Choose an index", "Choose an index",
		                                                   JOptionPane.QUESTION_MESSAGE, null, names, null);
		return name;
	}

	/*
	 * Open the new index dialog.
	 * Returns the name of the new index, or null if cancelled.
	 * @param suggestedName the suggested name
	 */
	public String createNewIndex(String suggestedName)
	{
		NewIndexDialog dlg = new NewIndexDialog(jEdit.getActiveView(), suggestedName);
		dlg.setVisible(true);
		if (!dlg.accepted())
			return null;
		Index index = createIndex(dlg.getIndexName(), dlg.getIndexAnalyzer());
		if (index == null)
			return null;
		// Update the properties
		return index.getName();
	}

	/**
	 * Add some files to the given index.
	 *
	 * @param indexName the index name
	 * @param files     the file array to add
	 * @param sharedSession whether the VFS session can be shared by all files
	 */
	public void addToIndex(String indexName, FileProvider files,
		boolean sharedSession, ProgressObserver progressObserver)
	{
		Index index = getIndex(indexName);
		if (index == null)
		{
			Log.log(Log.ERROR, this, "Unable to get index " + indexName);
			return;
		}
		if (sharedSession)
			index.addFiles(files, progressObserver);
		else
		{
			for (VFSFile file = files.next(); file != null; file = files.next())
			{
				index.addFile(file.getPath());
			}
		}
		index.commit();
	}

	/**
	 * Add some files to the given index.
	 *
	 * @param indexName the index name
	 * @param files     the file array to add
	 * @param sharedSession whether the VFS session can be shared by all files
	 */
	public void addToIndex(final String indexName, final VFSFile[] files,
		final boolean sharedSession)
	{

		Task task = new Task()
		{
			@Override
			public void _run()
			{
				addToIndex(indexName, new FileArrayProvider(files), sharedSession, this);
			}
		};
		ThreadUtilities.runInBackground(task);
	}

	/**
	 * Returns the index list.
	 * @return the index list
	 */
	public String[] getIndexes()
	{
		File home = getPluginHome();
		File indexFolder = new File(home, "indexes");
		File[] indexes = indexFolder.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isDirectory();
			}
		});

		if (indexes == null || indexes.length == 0)
			return new String[0];
		List<String> names = new ArrayList<String>(indexes.length);
		for (File index : indexes)
		{
			names.add(index.getName());
		}
		return names.toArray(new String[names.size()]);
	}

	private File getIndexFile(String name)
	{
		File home = getPluginHome();
		if (home == null)
			return null;
		File indexFolder = new File(home, "indexes");
		return new File(indexFolder, name);
	}

	private static SearchResults getSearchDockable(View view, boolean show)
	{
		DockableWindowManager dwm = view.getDockableWindowManager();
		if (show)
			dwm.showDockableWindow(SEARCH_DOCKABLE_NAME);
		SearchResults dockable = (SearchResults)
			dwm.getDockable(SEARCH_DOCKABLE_NAME);
		return dockable;
	}

	public void goToNextResult(View view)
	{
		SearchResults dockable = getSearchDockable(view, true);
		if (dockable == null)	// Should not happen
			return;
		dockable.goToNextResult();
	}

	public void goToPrevResult(View view)
	{
		SearchResults dockable = getSearchDockable(view, true);
		if (dockable == null)	// Should not happen
			return;
		dockable.goToPreviousResult();
	}

	/**
	 * Searches for the word at the caret location.
	 */
	public static void searchWordAtCaret(View view)
	{
		DockableWindowManager dwm = view.getDockableWindowManager();
		dwm.showDockableWindow(SEARCH_DOCKABLE_NAME);
		SearchResults dockable = (SearchResults)
			dwm.getDockable(SEARCH_DOCKABLE_NAME);
		if (dockable == null)	// Should not happen
			return;
		String word = getWordAtCaret(view);
		if (word == null)
			return;
		dockable.search(word, "");
	}

	public static String getWordAtCaret(View view)
	{
		JEditTextArea ta = view.getTextArea();
		String selected = ta.getSelectedText();
		if ((selected != null) && (! "".equals(selected)))
			return selected;
		int line = ta.getCaretLine();
		String text = ta.getLineText(line);
		Pattern pat = Pattern.compile("\\w+");
		Matcher m = pat.matcher(text);
		int end = -1;
		int start = -1;
		int offset = ta.getCaretPosition() - ta.getLineStartOffset(line);
		while (end <= offset)
		{
			if (! m.find())
				return null;
			end = m.end();
			start = m.start();
			selected = m.group();
		}
		if ((start > offset) || (selected.length() == 0))
			return null;
		return selected;
	}

	private static LucenePlugin getPluginInstance()
	{
		return (LucenePlugin) jEdit.getPlugin("gatchan.jedit.lucene.LucenePlugin");
	}

	// Plugin-API

	public static boolean search(String indexName, String text, int max,
		List<Object> files, TokenFilter tokenFilter)
	{
		LucenePlugin instance = getPluginInstance();
		if (instance == null)
			return false;
		Index index = instance.getIndex(indexName);
		if (index == null)
			return false;
		ResultProcessor processor = new MarkerListQueryProcessor(index, files, max, tokenFilter);
		index.search(text, "", max, processor);
		return true;
	}

	public void setCurrentIndex(View view, String name)
	{
		SearchResults dockable = getSearchDockable(view, false);
		if (dockable != null)
			dockable.setCurrentIndex(name);
	}
}
