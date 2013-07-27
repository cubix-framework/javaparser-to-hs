package infoviewer.lucene;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.jedit.help.HelpViewerInterface;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.Log;
/**
 * SearchPanel for the Lucene IndexBuilder.
 * 
 * @author ezust
 *
 */
public class SearchPanel extends JPanel
{
	
	//{{{ Private members
	private HelpViewerInterface helpViewer;
	private HistoryTextField searchField;
	
	ListModel resultsModel;
	JList resultsView;
		
	//{{{ HelpSearchPanel constructor
	public SearchPanel(HelpViewerInterface helpViewer)
	{
		super(new BorderLayout(6,6));

		this.helpViewer = helpViewer;

		Box box = new Box(BoxLayout.X_AXIS);
		box.add(new JLabel(jEdit.getProperty("helpviewer.search.caption")));
		box.add(Box.createHorizontalStrut(6));
		box.add(searchField = new HistoryTextField("helpviewer.search"));
		searchField.addActionListener(new ActionHandler());

		add(BorderLayout.NORTH,box);
		
		resultsView = new JList();
		resultsView.addMouseListener(new MouseHandler());
		resultsView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsView.setCellRenderer(new ResultRenderer());
		add(BorderLayout.CENTER,new JScrollPane(resultsView));
	} //}}}


	//{{{ ResultIcon class
	static class ResultIcon implements Icon
	{
		private static RenderingHints renderingHints;

		static
		{
			HashMap<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();

			hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			renderingHints = new RenderingHints(hints);
		}

		private float score;
		private int rank;

		ResultIcon(float score)
		{
			this.score= score;
			rank = (int)(score * 5.0);
		}

		public int getIconWidth()
		{
			return 40;
		}

		public int getIconHeight()
		{
			return 9;
		}

		public void paintIcon(Component c, Graphics g, int x, int y)
		{
			Graphics2D g2d = (Graphics2D)g.create();
			g2d.setRenderingHints(renderingHints);
			g2d.setColor(UIManager.getColor("Label.foreground"));
			

			for(int i = 0; i < 4; i++)
			{
				if(rank > i)
					g2d.setColor(UIManager.getColor("Label.foreground"));
				else
					g2d.setColor(UIManager.getColor("Label.disabledForeground"));
				g2d.fillOval(x+i*10,y,9,9);	
			} 
		}
	} //}}}

	//{{{ ResultRenderer class
	class ResultRenderer extends DefaultListCellRenderer
	{
		public Component getListCellRendererComponent(
			JList list,
			Object value,
			int cellIndex,
			boolean isSelected,
			boolean cellHasFocus)
		{
			super.getListCellRendererComponent(list,null,cellIndex,
				isSelected,cellHasFocus);

			if(value instanceof String)
			{
				setIcon(null);
				setText((String)value);
			}
			else try
			{
				Hit hit = (Hit)value;
				hit.getScore();
				String title = hit.get("title");
				setIcon(new ResultIcon(hit.getScore()));
				setText(title);
			}
			catch (IOException ioe) {
				Log.log(Log.ERROR, this, "can't get document", ioe);
			}
			

			return this;
		}
	} //}}}


	//{{{ ActionHandler class
	class ActionHandler implements ActionListener
	{
		String field = "content";
		public void actionPerformed(ActionEvent evt)
		{
			

			resultsView.setListData(new String[] { jEdit.getProperty(
				"helpviewer.searching") });

			final String queryString = searchField.getText();
			

			VFSManager.runInWorkThread(new Runnable()
			{
				public void run()
				{
					try {
						IndexBuilder builder = IndexBuilder.instance();
						Hits hits = builder.search(queryString);
						resultsModel = IndexBuilder.hitsToModel(hits);
						resultsView.setModel(resultsModel);
						if (resultsModel.getSize() < 1) {
							resultsView.setListData(new String[] {
								jEdit.getProperty("helpviewer.no-results") });
								getToolkit().beep();
						}
					}
					catch (Exception e) {
						Log.log(Log.ERROR, this, "can't search", e);
					}
					
				}
					
			});


		}
	} //}}}

	//{{{ MouseHandler class
	public class MouseHandler extends MouseAdapter
	{
		public void mouseReleased(MouseEvent evt)
		{
			int row = resultsView.locationToIndex(evt.getPoint());
			if(row != -1)
			{
				Hit result =(Hit) resultsModel.getElementAt(row);
				try {
					Document d = result.getDocument();
					String url = d.get("url");
					helpViewer.gotoURL(url,true, 0);
				}
				catch (IOException ioe) {
					Log.log(Log.ERROR, this, "can't open document", ioe);
				}
			}
		}
	} //}}}

}
