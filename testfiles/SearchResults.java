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

import gatchan.jedit.lucene.Index.ActivityListener;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Collections;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import marker.FileMarker;
import marker.MarkerSetsPlugin;
import marker.tree.FileTreeBuilder;
import marker.tree.SourceLinkTree;
import marker.tree.SourceLinkTree.SourceLinkParentNode;
import marker.tree.SourceLinkTree.SubtreePopupMenuProvider;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.EditBus.EBHandler;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.*;

@SuppressWarnings("serial")
/** Dockable for searching */
public class SearchResults extends JPanel implements DefaultFocusComponent
{
	public static final String CURRENT_BUFFER = "__current buffer__";
	public static final String ALL_BUFFERS = "__all buffers__";

	private static final String LUCENE_SEARCH_INDEX = "lucene.search.index";
	private static final String DOCKABLE_NAME = "lucene-search";
	private static final String MESSAGE_IDLE = "";
	private static final String MESSAGE_INDEXING = "Indexing";
	private final HistoryTextField searchField;
	private final JTextField type;
	private final JPanel mainPanel;
	private final JList list;
	private final JCheckBox lineResults;
	private final JSpinner maxResults;
	private final JCheckBox filterComments;
	private final JCheckBox filterLiterals;
	private final JComboBox indexes;
	private final MyModel model;
	private final SourceLinkTree tree;
	private final IndexComboBoxModel indexModel;
	private final JLabel indexStatus;
	private final RolloverButton clear;
	private final RolloverButton multi;
	private boolean multiStatus;
	private boolean horizontalLayout;
	private boolean firstTimeLayout = true;
	// private JTextPane preview;
	private final JLabel textLabel;
	private final JLabel fileTypeLabel;
	private final JLabel maxResultsLabel;
	private boolean shortLabels;
	private final ActionListener indexActionListener;
	private final JCheckBox extendedOptions;
	private JPanel searchOptions;
	private final View view;

	public SearchResults(View v)
	{
		super(new BorderLayout());
		view = v;
		KeyListener kl = v.getDockableWindowManager().closeListener(DOCKABLE_NAME);

		lineResults = new JCheckBox(getLabel("lucene.line-based"));
		lineResults.setToolTipText(jEdit.getProperty("lucene.results.line-based.tooltip"));
		lineResults.setSelected(true);
		fileTypeLabel = new JLabel(getLabel("lucene.file-type"));
		type = new JTextField(6);
		type.addKeyListener(kl);
		type.setToolTipText(jEdit.getProperty("lucene.file-type.tooltip"));
		maxResultsLabel = new JLabel(getLabel("lucene.max-results"));
		maxResults = new JSpinner(new SpinnerNumberModel(100, 1, 10000, 1));
		filterComments = new JCheckBox(getLabel("lucene.filter-comments"));
		filterLiterals = new JCheckBox(getLabel("lucene.filter-literals"));
		//JPanel maxPanel = new JPanel(new BorderLayout());
		//maxPanel.add(BorderLayout.WEST, new JLabel("Max results:"));
		//maxPanel.add(BorderLayout.EAST, maxResults);
		maxResults.setToolTipText(jEdit.getProperty("lucene.max.results.tooltip"));
		indexStatus = new JLabel();
		String[] items = LucenePlugin.instance.getIndexes();
		indexModel = new IndexComboBoxModel(items);
		indexes = new JComboBox(indexModel);
		setIndexesToolTipText(null);
		extendedOptions = new JCheckBox();
		extendedOptions.setToolTipText(jEdit.getProperty("lucene.extendedoptions.tooltip"));
		extendedOptions.setSelected(jEdit.getBooleanProperty("lucene.extendedoptions"));
		extendedOptions.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchOptions.setVisible(extendedOptions.isSelected());
				jEdit.setBooleanProperty("lucene.extendedoptions", extendedOptions.isSelected());
			}
		});
		indexActionListener = new ActionListener()
		{
			private Index prevIndex;
			private ActivityListener listener = new ActivityListener()
			{
				@Override
				public void indexingEnded(Index index)
				{
					indexStatus.setText(MESSAGE_IDLE);
					indexStatus.setToolTipText(MESSAGE_IDLE);
				}

				@Override
				public void indexingStarted(Index index)
				{
					indexStatus.setText(MESSAGE_INDEXING);
					indexStatus.setToolTipText(jEdit.getProperty("task.progress.tooltip",
						"See Utilities - Troubleshooting - Task Monitor for progress"));
				}
			};

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Index index = getSelectedIndex();
				if (index == null)
					return;
				if (prevIndex != null)
					prevIndex.removeActivityListener(listener);
				prevIndex = index;
				indexStatus.setText(index.isChanging() ? MESSAGE_INDEXING : MESSAGE_IDLE);
				index.addActivityListener(listener);
				jEdit.setProperty(LUCENE_SEARCH_INDEX, index.getName());
				setIndexesToolTipText((String) indexes.getSelectedItem());
			}
		};
		indexes.addActionListener(indexActionListener);
		// panel.add(new JLabel("Search for:"), BorderLayout.WEST);
		textLabel = new JLabel(getLabel("lucene.search-string"));
		searchField = new HistoryTextField("lucene.search-history");
		searchField.addKeyListener(kl);
		if (OptionPane.getSearchStringLength() != 0)
			searchField.setColumns(OptionPane.getSearchStringLength());
		searchField.setToolTipText(jEdit.getProperty("lucene.search-string.tooltip"));

		final MyActionListener actionListener = new MyActionListener();
		searchField.addActionListener(actionListener);
		type.addActionListener(actionListener);
		clear = new RolloverButton(GUIUtilities.loadIcon(
			jEdit.getProperty("hypersearch-results.clear.icon")));
		clear.setToolTipText(jEdit.getProperty("hypersearch-results.clear.label"));
		clear.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.setFiles(Collections.emptyList());
				tree.clear();
			}
		});
		multi = new RolloverButton();
		multi.setToolTipText(jEdit.getProperty("hypersearch-results.multi.label"));
		multi.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				toggleMultiStatus();
			}
		});
		tree = new SourceLinkTree(v);
		multiStatus = jEdit.getBooleanProperty("lucene.multipleresults", true);
		updateMultiStatus();

		setLayoutByGeometry();

		model = new MyModel();
		list = new JList(model);

		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				Object obj = list.getSelectedValue();
				if (obj instanceof String)
				{
					String path = (String) list.getSelectedValue();
					jEdit.openFile(view, path);
				}
				else if (obj instanceof FileMarker)
				{
					((FileMarker) obj).jump(view);
				}
			}
		});
		//HTMLDocument htmlDocument = new HTMLDocument();
		//preview = new JTextPane(htmlDocument);
		//preview.setContentType("text/html");
		mainPanel = new JPanel(new CardLayout());
		mainPanel.add(new JScrollPane(list), "list");
		mainPanel.add(new JScrollPane(tree), "tree");
		tree.setBuilder(new FileTreeBuilder());
		//add(mainPanel, new JScrollPane(tree));
		add(mainPanel, BorderLayout.CENTER);
//		add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(list), preview), BorderLayout.CENTER);
		if (indexes.getItemCount() > 0)
		{
			String lastIndex = jEdit.getProperty(LUCENE_SEARCH_INDEX);
			int index = 0;
			for (int i = 1; i < indexes.getItemCount(); i++)
			{
				if (indexes.getItemAt(i).equals(lastIndex))
				{
					index = i;
					break;
				}
			}
			indexes.setSelectedIndex(index);
			setIndexesToolTipText((String) indexes.getSelectedItem());
		}

		maxResults.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				if (searchField.getText().length() != 0)
					actionListener.actionPerformed(null);
			}
		});
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent e)
			{
				setLayoutByGeometry();
			}
		});

		indexes.addMouseListener(new MouseAdapter()
		{
			private JPopupMenu menu;

			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (GUIUtilities.isRightButton(e.getModifiers()))
				{
					menu = new JPopupMenu();
					JMenuItem refresh = new JMenuItem("refresh");
					refresh.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							String selectedIndex = (String) indexes.getSelectedItem();
							Task task = new ReindexTask(selectedIndex, new Runnable()
							{
								@Override
								public void run()
								{
									indexes.setEnabled(true);
								}
							});
							indexes.setEnabled(false);
							ThreadUtilities.runInBackground(task);
						}
					});
					menu.add(refresh);
					GUIUtilities.showPopupMenu(menu, indexes, e.getX(), e.getY());
				}
			}
		});
		propertiesChanged(true);
	}

	private void setIndexesToolTipText(String indexName)
	{
		Index index = null;
		if (indexName != null)
			index = LucenePlugin.instance.getIndex(indexName);
		String tooltip;
		if (index != null)
		{
			tooltip = jEdit.getProperty("lucene.index-combo-selected.tooltip",
				new Object [] {indexName, AnalyzerFactory.getAnalyzerName(index.getAnalyzer())});
		}
		else
		{
			tooltip = jEdit.getProperty("lucene.index-combo.tooltip");
		}
		indexes.setToolTipText(tooltip);
	}

	private void setLayoutByGeometry()
	{
		boolean horizontal = (getWidth() > getHeight());
		if ((! firstTimeLayout) && (horizontal == horizontalLayout))
			return;
		firstTimeLayout = false;
		horizontalLayout = horizontal;
		Component c = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.NORTH);
		if (c != null)
			remove(c);
		if (horizontal)
		{
			JPanel searchPanel = new JPanel(new BorderLayout());

			searchPanel.add(textLabel, BorderLayout.WEST);
			JPanel searchFieldContainer = new JPanel();
			searchFieldContainer.setLayout(new BoxLayout(searchFieldContainer,
				BoxLayout.PAGE_AXIS));
			searchFieldContainer.add(new JPanel());
			searchFieldContainer.add(searchField);
			searchFieldContainer.add(new JPanel());
			searchPanel.add(searchFieldContainer, BorderLayout.CENTER);
			JPanel p = new JPanel();
			searchPanel.add(BorderLayout.EAST, p);
			p.add(indexes);
			p.add(clear);
			p.add(multi);
			p.add(indexStatus);

			p.add(extendedOptions);
			searchOptions = new JPanel(new FlowLayout(FlowLayout.LEADING));
			searchOptions.add(lineResults);
			searchOptions.add(fileTypeLabel);
			searchOptions.add(type);
			searchOptions.add(lineResults);
			searchOptions.add(maxResultsLabel);
			searchOptions.add(maxResults);
			searchOptions.add(filterComments);
			searchOptions.add(filterLiterals);
			searchOptions.setVisible(extendedOptions.isSelected());
			searchPanel.add(searchOptions, BorderLayout.SOUTH);
			add(searchPanel, BorderLayout.NORTH);
		}
		else
		{
			JPanel typePanel = new JPanel();
			typePanel.add(fileTypeLabel, BorderLayout.WEST);
			typePanel.add(type, BorderLayout.CENTER);

			JPanel maxResultsPanel = new JPanel();
			maxResultsPanel.add(maxResultsLabel, BorderLayout.WEST);
			maxResultsPanel.add(maxResults, BorderLayout.CENTER);

			JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			filterPanel.add(filterComments);
			filterPanel.add(filterLiterals);

			searchOptions = new JPanel();
			searchOptions.setLayout(new BoxLayout(searchOptions, BoxLayout.Y_AXIS));

			searchOptions.add(filterPanel);
			searchOptions.add(typePanel);
			searchOptions.add(lineResults);
			searchOptions.add(maxResultsPanel);
			searchOptions.setVisible(extendedOptions.isSelected());

			JPanel searchPanel = new JPanel();
			searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
			add(searchPanel, BorderLayout.NORTH);

			JPanel p = new JPanel(new BorderLayout());
			p.add(textLabel, BorderLayout.WEST);
			p.add(searchField, BorderLayout.CENTER);

			searchPanel.add(p);

			searchPanel.add(indexes);
			p = new JPanel(new FlowLayout(FlowLayout.LEADING));

			p.add(clear);
			p.add(multi);
			p.add(indexStatus);
			p.add(extendedOptions);
			searchPanel.add(p);
			searchPanel.add(searchOptions);
		}
		revalidate();
	}

	private static String getLabel(String prefix)
	{
		return jEdit.getProperty(prefix + ".label." +
			(OptionPane.getUseShortLabels() ? "short" : "long"));
	}

	private void updateMultiStatus()
	{
		if (multiStatus)
			multi.setIcon(GUIUtilities.loadIcon(jEdit.getProperty("hypersearch-results.multi.multiple.icon")));
		else
			multi.setIcon(GUIUtilities.loadIcon(jEdit.getProperty("hypersearch-results.multi.single.icon")));
		tree.allowMultipleResults(multiStatus);
	}

	private void toggleMultiStatus()
	{
		multiStatus = (! multiStatus);
		jEdit.setBooleanProperty("lucene.multipleresults", multiStatus); 
		updateMultiStatus();
	}

	private Index getSelectedIndex()
	{
		String indexName = (String) indexes.getSelectedItem();
		if (indexName == null)
			return null;
		if (indexName == CURRENT_BUFFER)
		{
			Index index = new TemporaryIndex(CURRENT_BUFFER);
			jEdit.getActiveView().getBuffer().getPath();
			index.addFile(jEdit.getActiveView().getBuffer().getPath());
			index.commit();
			return index;
		}
		if (indexName == ALL_BUFFERS)
		{
			Index index = new TemporaryIndex(ALL_BUFFERS);
			Buffer[] buffers = jEdit.getBuffers();
			for (Buffer buffer : buffers)
				index.addFile(buffer.getPath());
			index.commit();
			return index;
		}
		return LucenePlugin.instance.getIndex(indexName);
	}

	public void setType(String fileType)
	{
		type.setText(fileType);
	}

	public void setText(String text)
	{
		searchField.setText(text);
	}

	public void search(String text, String fileType)
	{
		Index index = getSelectedIndex();
		if (index == null)
			return;
		int max = (Integer) maxResults.getValue();
		searchField.setText(text);
		type.setText(fileType);
		text = text.trim();
		if (text.isEmpty())
			return;
		ThreadUtilities.runInBackground(new SearchQuery(index, text, fileType, max,
			lineResults.isSelected(), new TokenFilter(filterComments.isSelected(),
			filterLiterals.isSelected())));
	}

	public void goToNextResult()
	{
		if (tree != null)
			tree.goToNextLink();
	}

	public void goToPreviousResult()
	{
		if (tree != null)
			tree.goToPreviousLink();
	}

	public void setCurrentIndex(String name)
	{
		indexModel.setSelectedItem(name);
		indexActionListener.actionPerformed(null);
	}

	//{{{ addNotify() method
	@Override
	public void addNotify()
	{
		super.addNotify();
		EditBus.addToBus(this);
		// It is not sufficient to register to the edit bus. If new indexes
		// were created while this dockable was hidden (e.g. with MyDoggy),
		// it did not receive the edit bus messages and needs to refresh the
		// index list.
		indexModel.setIndexes(LucenePlugin.instance.getIndexes());
	} //}}}

	//{{{ removeNotify() method
	@Override
	public void removeNotify()
	{
		super.removeNotify();
		EditBus.removeFromBus(this);
	} //}}}

	@EBHandler
	public void handleMessage(LuceneIndexUpdate msg)
	{
		Vector<String> items = new Vector<String>();
		for (int i = 0; i < indexModel.getSize(); i++)
		{
			String indexName = (String) indexModel.getElementAt(i);
			if (indexName != CURRENT_BUFFER && indexName != ALL_BUFFERS)
				items.add(indexName);
		}
		String indexName = (String) msg.getSource();
		if (msg.getWhat() == LuceneIndexUpdate.What.CREATED)
			items.add(indexName);
		else
			items.remove(indexName);
		String [] names = new String[items.size()];
		items.toArray(names);
		indexModel.setIndexes(names);
		if (msg.getWhat() == LuceneIndexUpdate.What.CREATED)
			indexes.setSelectedItem(indexName);
	}

	@EBHandler
	public void handlePropertiesChanged(PropertiesChanged msg)
	{
		propertiesChanged(false);
	}

	private void propertiesChanged(boolean firstTime)
	{
		boolean updateLayout = false;
		int columns = OptionPane.getSearchStringLength();
		if ((columns != 0) && (firstTime || (columns != searchField.getColumns())))
		{
			updateLayout = true;
			searchField.setColumns(columns);
		}
		boolean newShortLabels = OptionPane.getUseShortLabels();
		if (firstTime || (shortLabels != newShortLabels))
		{
			updateLayout = true;
			shortLabels = newShortLabels;
			lineResults.setText(getLabel("lucene.line-based"));
			fileTypeLabel.setText(getLabel("lucene.file-type"));
			maxResultsLabel.setText(getLabel("lucene.max-results"));
			textLabel.setText(getLabel("lucene.search-string"));
			filterComments.setText(getLabel("lucene.filter-comments"));
			filterLiterals.setText(getLabel("lucene.filter-literals"));
		}
		if (updateLayout)
			revalidate();
	}

	private class SearchQuery extends Task
	{
		private final Index index;
		private final String text;
		private final String fileType;
		private final int max;
		private final boolean lineResult;
		private final TokenFilter tokenFilter;

		private SearchQuery(Index index, String text, String fileType, int max, boolean lineResult,
			TokenFilter tokenFilter)
		{
			this.index = index;
			this.text = text;
			this.fileType = fileType;
			this.max = max;
			this.lineResult = lineResult;
			this.tokenFilter = tokenFilter;
		}

		@Override
		public void _run()
		{
			Log.log(Log.NOTICE, this, "Search for " + text + " in file type: " + fileType);
			ResultProcessor processor;
			final List<Object> files = new ArrayList<Object>();
			if (lineResult)
				processor = new MarkerListQueryProcessor(index, files, max, tokenFilter);
			else
				processor = new FileListQueryProcessor(files, max);
			index.search(text, fileType, max, processor);
			ThreadUtilities.runInDispatchThread(new Runnable()
			{
				@Override
				public void run()
				{
					if (lineResult)
					{
						SearchRootNode rootNode = new SearchRootNode(text);
						SourceLinkParentNode parent = tree.addSourceLinkParent(rootNode);
						FileMarker prev = null;
						int count = 0;
						for (Object o: files)
						{
							FileMarker marker = (FileMarker) o;
							if (marker.equals(prev))
							{
								Vector<FileMarker.Selection> selections = marker.getSelections();
								for (FileMarker.Selection selection: selections)
									prev.addSelection(selection);
							}
							else
							{
								parent.addSourceLink(marker);
								prev = marker;
								count++;
							}
						}
						rootNode.addChildCount(count);
						TreeModel model = tree.getModel();
						if (model instanceof DefaultTreeModel)
							((DefaultTreeModel) model).nodeChanged(parent);
						tree.setExpandedOnly(parent);
						((CardLayout) mainPanel.getLayout()).show(mainPanel, "tree");
					}
					else
					{
						model.setFiles(files);
						((CardLayout) mainPanel.getLayout()).show(mainPanel, "list");
					}
				}
			});
		}
	}

	private static class MyModel extends AbstractListModel
	{
		private List<Object> files = new ArrayList<Object>();

		public void setFiles(List<Object> files)
		{
			this.files = files;
			fireContentsChanged(this, 0, files.size());
		}

		@Override
		public int getSize()
		{
			return files.size();
		}

		@Override
		public Object getElementAt(int index)
		{
			return files.get(index);
		}
	}

	private static class IndexComboBoxModel extends AbstractListModel implements ComboBoxModel
	{
		private String[] indexes;

		private String selectedItem;
		private int selectedIndex;

		private IndexComboBoxModel(String[] indexes)
		{
			setIndexes(indexes);
		}

		@Override
		public int getSize()
		{
			return indexes.length;
		}

		@Override
		public Object getElementAt(int index)
		{
			return indexes[index];
		}

		public void setIndexes(String[] indexes)
		{
			this.indexes = new String[indexes.length + 2];
			System.arraycopy(indexes, 0, this.indexes, 2, indexes.length);
			this.indexes[0] = CURRENT_BUFFER;
			this.indexes[1] = ALL_BUFFERS;
			if (indexes.length == 0)
			{
				selectedItem = null;
			}
			else
			{
				boolean selectedStillExists = false;
				for (String index : this.indexes)
				{
					if (StandardUtilities.objectsEqual(selectedItem, index))
					{
						selectedStillExists = true;
						break;
					}
				}
				if (!selectedStillExists)
					selectedItem = this.indexes[0];
			}
			fireContentsChanged(this, 0, this.indexes.length);
		}

		@Override
		public void setSelectedItem(Object selectedItem)
		{
			int i;
			for (i = 0; i < indexes.length; i++)
				if (selectedItem.equals(indexes[i]))
					break;
			if (i == indexes.length)	// Can't select given item
				return;
			int prev = selectedIndex;
			selectedIndex = i;
			this.selectedItem = (String) selectedItem;
			fireContentsChanged(this, prev, selectedIndex);
		}

		@Override
		public Object getSelectedItem()
		{
			return selectedItem;
		}
	}

	private static class SearchRootNode implements SubtreePopupMenuProvider
	{
		private String text;

		SearchRootNode(String searchText)
		{
			text = searchText;
		}

		public String toString()
		{
			return text;
		}

		@Override
		public void addPopupMenuItemsFor(JPopupMenu popup,
			final SourceLinkParentNode parent, final DefaultMutableTreeNode node)
		{
			if (popup == null)
				return;
			popup.add(new AbstractAction("Toggle marker(s)")
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Vector<FileMarker> markers = parent.getFileMarkers(node);
					for (FileMarker marker: markers)
						MarkerSetsPlugin.toggleMarker(marker);
				}
			});
		}

		public void addChildCount(int count)
		{
			text = text + " (" + count + ')';
		}
	}

	private class MyActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			String search = searchField.getText().trim();
			String fileType = type.getText().trim();
			search(search, fileType);
		}
	}

	@Override
	public void focusOnDefaultComponent()
	{
		searchField.requestFocusInWindow();
	}
}
