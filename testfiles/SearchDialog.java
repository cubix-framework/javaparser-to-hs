package superabbrevs.gui.searchdialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.gui.EnhancedDialog;

public class SearchDialog extends EnhancedDialog {
    
	public SearchDialog(Frame parent, String title, boolean modal, SearchDialogModel model) {
        super(parent, title, modal);
        this.model = model;
        initComponents();
    }

    private void fireSearchAcceptedEvent(Object o) {
        for (SearchAcceptedListener listener : searchAcceptedListeners) {
            listener.accepted(o);
        }
    }
    
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search for an abbreviation");
        setName("searchDialog");
        BorderLayout layout = new BorderLayout();
		setLayout(layout);
		
        add(createSearchPanel(), BorderLayout.NORTH);        
        add(createScrollPane(), BorderLayout.CENTER);

        pack();
    }

	private JScrollPane createScrollPane() {
		abbreviationsJScrollPane = new javax.swing.JScrollPane();
        abbreviationsJScrollPane.setFocusable(false);
        abbreviationsJScrollPane.setName("abbreviationsJScrollPane");
        abbreviationsJScrollPane.setViewportView(createAbbrevsTable());
        return abbreviationsJScrollPane;
	}

	private JTable createAbbrevsTable() {
		abbreviationsJTable = new JTable();
        abbreviationsJTable.setName("abbreviationsJTable");
        abbreviationsJTable.setShowHorizontalLines(false);
        abbreviationsJTable.setShowVerticalLines(false);
        abbreviationsJTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                abbreviationsJTableFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                abbreviationsJTableFocusLost(evt);
            }
        });
        abbreviationsJTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                abbreviationsJTableKeyPressed(evt);
            }
        });
        abbreviationsJTable.setModel(model);
        abbreviationsJTable.getSelectionModel().setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        abbreviationsJTable.setIntercellSpacing(new Dimension(3,3));
        return abbreviationsJTable;
	}

	private JPanel createSearchPanel() {
		JPanel searchPanel = new JPanel(new BorderLayout(7,7));
		searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        searchPanel.add(CreateSearchLabel(), BorderLayout.WEST);
        searchPanel.add(createSearchField(), BorderLayout.CENTER);
		return searchPanel;
	}

	private JTextField createSearchField() {
		searchField = new JTextField();
		searchField.setName("searchJTextField");
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                searchJTextFieldFocusGained(evt);
            }
        });
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchJTextFieldKeyReleased(evt);
            }
        });
        return searchField;
	}

	private JLabel CreateSearchLabel() {
	    JLabel searchLabel = new JLabel();
		searchLabel.setDisplayedMnemonic('S');
        searchLabel.setLabelFor(searchField);
        searchLabel.setText("Search for:");
        searchLabel.setName("searchLabel");
        return searchLabel;
	}

    private void searchJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {
        model.searchTextChanged(searchField.getText());
        
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            dispose();
        }
    }

    private void searchJTextFieldFocusGained(java.awt.event.FocusEvent evt) {
        searchField.setSelectionStart(0);
        searchField.setSelectionEnd(searchField.getText().length());
    }

    private void abbreviationsJTableFocusLost(java.awt.event.FocusEvent evt) {
        abbreviationsJTable.getSelectionModel().clearSelection();
    }

    private void abbreviationsJTableFocusGained(java.awt.event.FocusEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run(){
                if (abbreviationsJTable.getSelectionModel().isSelectionEmpty()) {
                    abbreviationsJTable.changeSelection(0, 0, false, false);
                }
            }
        });
    }

    private void abbreviationsJTableKeyPressed(java.awt.event.KeyEvent evt) {
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                dispose();
                break;
            case KeyEvent.VK_TAB:
                searchField.grabFocus();
                break;
        }
    }
    
    public void addSearchAcceptedListener(SearchAcceptedListener listener) {
        searchAcceptedListeners.add(listener);
    }
    
    public void removeSearchAcceptedListener(SearchAcceptedListener listener) {
        searchAcceptedListeners.remove(listener);
    }
    
    @Override
    public void ok() {
        if (!abbreviationsJTable.getSelectionModel().isSelectionEmpty()) {
            int s = abbreviationsJTable.getSelectionModel().getMinSelectionIndex();
            Object o = model.getRowObject(s); 
            fireSearchAcceptedEvent(o);
        } else if (0 < abbreviationsJTable.getRowCount()) {
            fireSearchAcceptedEvent(model.getRowObject(0));
        }
    }

    @Override
    public void cancel() {
        dispose();
    }
    
    private javax.swing.JScrollPane abbreviationsJScrollPane;
    private javax.swing.JTable abbreviationsJTable;
    private JTextField searchField;
    private SearchDialogModel model;
    
    private ArrayList<SearchAcceptedListener> searchAcceptedListeners = 
            new ArrayList<SearchAcceptedListener>();
}
