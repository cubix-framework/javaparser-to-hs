package browser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;

import marker.FileMarker;
import marker.MarkerSetsPlugin;
import marker.tree.SourceLinkTree;
import marker.tree.SourceLinkTree.SourceLinkParentNode;
import marker.tree.SourceLinkTree.SubtreePopupMenuProvider;
import options.GlobalOptionPane;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.util.Log;

@SuppressWarnings("serial")
public class GlobalResultsView extends JPanel implements
	DefaultFocusComponent, GlobalDockableInterface
{
	private View view;
	private String param;
	private JTextField symbolTF;
	private JLabel statusLbl;
	private JCheckBox multiCB;
	private SourceLinkTree tree;
	
	public GlobalResultsView(final View view, String param)
	{
		super(new BorderLayout());
		this.view = view;
		this.param = param;
		JPanel symbolPanel = new JPanel(new BorderLayout());
		add(symbolPanel, BorderLayout.NORTH);
		symbolPanel.add(new JLabel("Search for:"), BorderLayout.WEST);
		symbolTF = new JTextField(40);
		symbolTF.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					show(view, symbolTF.getText());
			}
		});
		symbolPanel.add(symbolTF, BorderLayout.CENTER);
		JPanel toolbar = new JPanel(new BorderLayout(5, 0));
		symbolPanel.add(toolbar, BorderLayout.EAST);
		statusLbl = new JLabel("");
		toolbar.add(statusLbl, BorderLayout.EAST);
		tree = new SourceLinkTree(view);
		add(new JScrollPane(tree), BorderLayout.CENTER);
		multiCB = new JCheckBox("Multiple", true);
		toolbar.add(multiCB, BorderLayout.CENTER);
		multiCB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tree.allowMultipleResults(multiCB.isSelected());
			}
		});
	}
	
	protected String getParam()	{
		return param;
	}
	
	private String getBufferDirectory() {
		File file = new File(view.getBuffer().getPath());
		return file.getParent();
	}

	private String makeBold(String s) {
		return "<html><body><b>" + s + "</body></html>";
	}

	private class SearchNode implements SubtreePopupMenuProvider
	{
		String search;
		SourceLinkParentNode parent;
		
		public SearchNode(String search) {
			this.search = search;
			parent = null;
		}
		public void setTreeNode(SourceLinkParentNode parent) {
			this.parent = parent;
		}
		public String toString() {
			if (parent == null)
				return makeBold(search);
			int [] counts = parent.getFileAndMarkerCounts(); 
			return makeBold(search + " (" + counts[1] +
				" occurrences in " + counts[0] + " files)");
		}
		public void addPopupMenuItemsFor(JPopupMenu popup, final SourceLinkParentNode parent,
			final DefaultMutableTreeNode node)
		{
			if (popup == null)
				return;
			popup.add(new AbstractAction("Toggle marker(s)") {
				public void actionPerformed(ActionEvent e) {
					Vector<FileMarker> markers = parent.getFileMarkers(node);
					for (FileMarker marker: markers)
						MarkerSetsPlugin.toggleMarker(marker);
				}
			});
		}
	}
	
	private class RecordQuery implements Runnable {
		String identifier;
		String workingDirectory;
		public RecordQuery(String identifier, String workingDirectory)
		{
			this.identifier = identifier;
			this.workingDirectory = workingDirectory;
		}
		public void run() {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusLbl.setText("Working...");
				}
			});
			long start = System.currentTimeMillis();
			final Vector<GlobalRecord> refs =
				GlobalLauncher.instance().runRecordQuery(getParam() +
					" " + identifier, workingDirectory);
			SearchNode rootNode = new SearchNode(identifier);
			SourceLinkParentNode parent =
				tree.addSourceLinkParent(rootNode);
			rootNode.setTreeNode(parent);
			final int index = parent.getRoot().getIndex(parent);
			for (int i = 0; i < refs.size(); i++)
			{
				GlobalRecord rec = refs.get(i);
				String file = rec.getFile().replace("/", File.separator);
				int line = rec.getLine();
				parent.addSourceLink(new FileMarker(file, line - 1, rec.getText()));
			}
			if (refs.size() == 1 && GlobalOptionPane.isJumpImmediately())
				new GlobalReference(refs.get(0)).jump(view);
			long end = System.currentTimeMillis();
			Log.log(Log.DEBUG, this.getClass(), "GlobalResultsView(" + getParam() +
				", " + identifier + "' took " + (end - start) * .001 + " seconds.");
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					statusLbl.setText(refs.size() + " results");
					for (int i = index; i < tree.getRowCount(); i++)
						tree.expandRow(i);
				}
			});
		}
	}
	public void show(View view, String identifier) {
		symbolTF.setText(identifier);
		RecordQuery query = new RecordQuery(identifier,	getBufferDirectory());
		GlobalPlugin.runInBackground(query);
	}
	
	public void focusOnDefaultComponent() {
		tree.requestFocus();
	}
}
