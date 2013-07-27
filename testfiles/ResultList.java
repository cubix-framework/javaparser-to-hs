package org.columba.mail.gui.tagging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.columba.api.exception.ServiceNotFoundException;
import org.columba.core.facade.ServiceFacadeRegistry;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.gui.base.EmptyIcon;
import org.columba.core.search.api.ISearchResult;
import org.columba.mail.facade.IDialogFacade;
import org.columba.mail.resourceloader.MailImageLoader;
import org.columba.mail.search.MailSearchResult;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

/**
 * 
 * Copied from org.columba.mail.gui.search.ResultList
 * 
 * @author hubms
 *
 */
@SuppressWarnings("serial")
public class ResultList extends JXList {
	
	private DefaultListModel listModel;
	
	public ResultList() {
	
		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new MyListCellRenderer());
	
		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { new RolloverHighlighter(new Color(248, 248,
						248), Color.white) }));
		setRolloverEnabled(true);
	
		/* addMouseListener(new DoubleClickListener() {
	
			@Override
			public void doubleClick(MouseEvent event) {
				ISearchResult result = (ISearchResult) getSelectedValue();
	
				try {
					IDialogFacade facade = (IDialogFacade) ServiceRegistry
							.getInstance().getService(IDialogFacade.class);
					facade.openMessage(result.getLocation());
				} catch (ServiceNotFoundException e) {
					e.printStackTrace();
				}
	
			}
		});
		*/
	}

	class MyListCellRenderer extends JPanel implements ListCellRenderer {

		private JPanel centerPanel;

		private JPanel topPanel;

		private Border lineBorder = new LineBorder(new Color(230,
				230, 230));

		private JLabel statusLabel = new JLabel();

		private JLabel fromLabel = new JLabel();

		private JLabel dateLabel = new JLabel();

		private JLabel subjectLabel = new JLabel();

		private JLabel flagLabel = new JLabel();

		private ImageIcon flagIcon = MailImageLoader.getSmallIcon("flag.png");

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			topPanel = new JPanel();
			topPanel.setLayout(new BorderLayout());
			topPanel.add(fromLabel, BorderLayout.CENTER);
			topPanel.add(dateLabel, BorderLayout.EAST);

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(topPanel, BorderLayout.NORTH);
			centerPanel.add(subjectLabel, BorderLayout.CENTER);

			add(statusLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);
			add(flagLabel, BorderLayout.EAST);

			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));

			statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			flagLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

			topPanel.setOpaque(false);
			centerPanel.setOpaque(false);
			setOpaque(true);

		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				// setBackground(list.getSelectionBackground());
				// setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			MailSearchResult result = (MailSearchResult) value;

			statusLabel.setIcon(result.getStatusIcon());
			subjectLabel.setText(result.getTitle());
			fromLabel.setText(result.getFrom().getShortAddress());
			dateLabel.setText(result.getStringDate());

			if (result.isFlagged())
				flagLabel.setIcon(flagIcon);
			else
				flagLabel.setIcon(new EmptyIcon());

			return this;
		}

	}


}
