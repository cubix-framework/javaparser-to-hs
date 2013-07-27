package com.lipstikLF.delegate;

import com.lipstikLF.util.LipstikBorderFactory;
import sun.awt.shell.ShellFolder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFileChooserUI;
import javax.swing.table.*;
import javax.swing.text.Position;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;


public class LipstikFileChooserUI extends BasicFileChooserUI implements MouseListener
{
    /**	caches icons used for the file choooser */
    private static final Hashtable iconCache = new Hashtable();

    private JPanel listViewPanel;
    private JPanel detailsViewPanel;
    private JPanel currentViewPanel;
    private JPanel centerPanel;
    private JPanel buttonPanel;
    private JPanel bottomPanel;

    private JButton approveButton;
    private JButton cancelButton;
    private JComboBox directoryComboBox;
    private JToggleButton listViewButton;
    private JToggleButton detailsViewButton;

    private JList  list;

    private JLabel lookInLabel;
    private JTable detailsTable;
    private JPopupMenu contextMenu;

    private JTextField fileNameTextField;
    private ListSelectionModel listSelectionModel;
    private DirectoryComboBoxModel directoryComboBoxModel;
    private FilterComboBoxModel filterComboBoxModel;

    private Action directoryComboBoxAction = new DirectoryComboBoxAction();
    private boolean useShellFolder;

    private static final Insets shrinkwrap= new Insets(0, 0, 0, 0);

    // Preferred and Minimum sizes for the dialog box
    private static final int PREF_WIDTH  = 483;
    private static final int PREF_HEIGHT = 326;
    private static Dimension PREF_SIZE = new Dimension(PREF_WIDTH, PREF_HEIGHT);

    private static final int MIN_WIDTH  = 500;
    private static final int MIN_HEIGHT = 326;
    private static Dimension MIN_SIZE = new Dimension(MIN_WIDTH, MIN_HEIGHT);

    private static int LIST_PREF_WIDTH  = 405;
    private static int LIST_PREF_HEIGHT = 135;
    private static Dimension LIST_PREF_SIZE = new Dimension(LIST_PREF_WIDTH, LIST_PREF_HEIGHT);

	private static final int COLUMN_FILENAME= 0;
	private static final int COLUMN_FILESIZE= 1;
	private static final int COLUMN_FILETYPE= 2;
	private static final int COLUMN_FILEDATE= 3;
	private static final int COLUMN_FILEATTR= 4;
	private static final int COLUMN_COLCOUNT= 5;

    private int[] COLUMN_WIDTHS= { 150, 75, 130, 130, 40 };

    private int lookInLabelMnemonic = 0;
    private String lookInLabelText  = null;
    private String saveInLabelText  = null;

    private int fileNameLabelMnemonic           = 0;
    private int filesOfTypeLabelMnemonic        = 0;
    private String fileNameLabelText            = null;
    private String filesOfTypeLabelText         = null;
    private String upFolderToolTipText          = null;
    private String upFolderAccessibleName       = null;
    private String homeFolderToolTipText        = null;
    private String homeFolderAccessibleName     = null;
    private String newFolderToolTipText         = null;
    private String newFolderAccessibleName      = null;
    private String listViewButtonToolTipText    = null;
    private String listViewButtonAccessibleName = null;

    private String detailsViewButtonToolTipText    = null;
    private String detailsViewButtonAccessibleName = null;

	private String fileNameHeaderText = null;
	private String fileSizeHeaderText = null;
	private String fileTypeHeaderText = null;
	private String fileDateHeaderText = null;
	private String fileAttrHeaderText = null;

	private Icon upFolderIconDisabled  = UIManager.getIcon("FileChooser.upFolderIconDisabled");
	private Icon newFolderIconDisabled = UIManager.getIcon("FileChooser.newFolderIconDisabled");

	class ViewButtonListener implements ActionListener
    {
        JFileChooser fc;
        ViewButtonListener(JFileChooser fc)
        {
            this.fc = fc;
        }

        public void actionPerformed(ActionEvent e)
        {
            JToggleButton b = (JToggleButton) e.getSource();
            JPanel oldViewPanel = currentViewPanel;

            if (b == detailsViewButton)
            {
                if (detailsViewPanel == null)
                {
                    detailsViewPanel = createDetailsView(fc);
                    detailsViewPanel.setPreferredSize(LIST_PREF_SIZE);
                }
                currentViewPanel = detailsViewPanel;
                listViewButton.getModel().setRollover(false);
            }
            else
            {
                detailsViewButton.getModel().setRollover(false);
                currentViewPanel= listViewPanel;
            }

            if (currentViewPanel != oldViewPanel)
            {
                centerPanel.remove(oldViewPanel);
                centerPanel.add(currentViewPanel, BorderLayout.CENTER);
                centerPanel.revalidate();
                centerPanel.repaint();
            }
        }
    }

    /**
     * Create the UI delegate for the given component
     *
     * @param c The component for which to create the ui delegate
     * @return The created ui delegate
     */
    public static ComponentUI createUI(JComponent c)
    {
        return new LipstikFileChooserUI((JFileChooser) c);
    }


    /**	Creates an instance for the specified JFileChooser */
    public LipstikFileChooserUI(JFileChooser filechooser)
    {
        super(filechooser);
    }

    /**	Uninstalls the UI delegate for the specified JFileChooser */
    public void uninstallComponents(JFileChooser fc)
    {
        fc.removeAll();
        bottomPanel = null;
        buttonPanel = null;
    }


    /**	Installs the components for the specified JFileChooser */
    public void installComponents(JFileChooser fc)
    {
        FileSystemView fsv = fc.getFileSystemView();

        fc.setBorder(new EmptyBorder(8, 8, 0, 7));
        fc.setLayout(new BorderLayout(0, 8));

        // ********************************* //
        // **** Construct the top panel **** //
        // ********************************* //

        // Directory manipulation buttons
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(new BoxLayout(topButtonPanel, BoxLayout.LINE_AXIS));
        topPanel.add(topButtonPanel, BorderLayout.AFTER_LINE_ENDS);

        // Add the top panel to the fileChooser
        fc.add(topPanel, BorderLayout.NORTH);

        // ComboBox Label
        lookInLabel = new JLabel(lookInLabelText);
        lookInLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        topPanel.add(lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);

        // CurrentDir ComboBox
        directoryComboBox = new JComboBox();
        directoryComboBox.putClientProperty("JComboBox.lightweightKeyboardNavigation","Lightweight");
        lookInLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(new DirectoryComboBoxRenderer());
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.TOP_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);
        topPanel.add(directoryComboBox, BorderLayout.CENTER);

        // Up Button
        JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setRolloverEnabled(true);
        upFolderButton.setFocusable(false);
        upFolderButton.setBorder(LipstikBorderFactory.getButtonFileChooserBorder());
        upFolderButton.setDisabledIcon(upFolderIconDisabled);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        upFolderButton.setMargin(shrinkwrap);

        topButtonPanel.add(upFolderButton);

        File homeDir = fsv.getHomeDirectory();
        String toolTipText= homeFolderToolTipText;
        if (fsv.isRoot(homeDir))
            toolTipText = getFileView(fc).getName(homeDir); // Probably "Desktop".

        // Home Button
        JButton b = new JButton(homeFolderIcon);
        b.setToolTipText(toolTipText);
        b.setRolloverEnabled(true);
        b.setFocusable(false);
        b.setBorder(LipstikBorderFactory.getButtonFileChooserBorder());
        b.getAccessibleContext().setAccessibleName(homeFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);
        b.addActionListener(getGoHomeAction());

        topButtonPanel.add(b);

        // New Directory Button
        b = new JButton(getNewFolderAction());
        b.setText(null);
        b.setIcon(newFolderIcon);
        b.setDisabledIcon(newFolderIconDisabled);
        b.setToolTipText(newFolderToolTipText);
        b.setRolloverEnabled(true);
        b.setFocusable(false);
        b.setBorder(LipstikBorderFactory.getButtonFileChooserBorder());
        b.getAccessibleContext().setAccessibleName(newFolderAccessibleName);
        b.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        b.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        b.setMargin(shrinkwrap);

        topButtonPanel.add(b);
        topButtonPanel.add(Box.createRigidArea(new Dimension(6, 1)));

        ButtonGroup viewButtonGroup = new ButtonGroup();
        ViewButtonListener viewButtonListener = new ViewButtonListener(fc);

        // Toggle buttons
        // --------------

        listViewButton= new JToggleButton(listViewIcon);
        listViewButton.setRolloverEnabled(true);
        listViewButton.setFocusable(false);
        listViewButton.setBorder(LipstikBorderFactory.getButtonFileChooserBorder());
        listViewButton.setToolTipText(listViewButtonToolTipText);
        listViewButton.getAccessibleContext().setAccessibleName(listViewButtonAccessibleName);
        listViewButton.setSelected(true);
        listViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        listViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        listViewButton.setMargin(shrinkwrap);
        listViewButton.addActionListener(viewButtonListener);

        viewButtonGroup.add(listViewButton);
        topButtonPanel.add(listViewButton);

        detailsViewButton= new JToggleButton(detailsViewIcon);
        detailsViewButton.setRolloverEnabled(true);
        detailsViewButton.setFocusable(false);
        detailsViewButton.setBorder(LipstikBorderFactory.getButtonFileChooserBorder());
        detailsViewButton.setToolTipText(detailsViewButtonToolTipText);
        detailsViewButton.getAccessibleContext().setAccessibleName(detailsViewButtonAccessibleName);
        detailsViewButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        detailsViewButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        detailsViewButton.setMargin(shrinkwrap);
        detailsViewButton.addActionListener(viewButtonListener);

        viewButtonGroup.add(detailsViewButton);
        topButtonPanel.add(detailsViewButton);


        // Use ShellFolder class to populate combobox only if
        // FileSystemView.getRoots() returns one folder and that is
        // the same as the first item in the ShellFolder combobox list.
        {
            useShellFolder = false;
            File[] roots =  fsv.getRoots();
            if (roots != null && roots.length == 1)
            {
                File[] cbFolders = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                if (cbFolders != null && cbFolders.length > 0 && roots[0] == cbFolders[0])
                {
                    useShellFolder = true;
                }
            }
        }

        // ************************************** //
        // ******* Add the directory pane ******* //
        // ************************************** //

        centerPanel = new JPanel(new BorderLayout());
        listViewPanel = createList(fc);
        listSelectionModel = list.getSelectionModel();
        listViewPanel.setPreferredSize(LIST_PREF_SIZE);
        centerPanel.add(listViewPanel, BorderLayout.CENTER);
        currentViewPanel = listViewPanel;
        centerPanel.add(getAccessoryPanel(), BorderLayout.AFTER_LINE_ENDS);
        JComponent accessory = fc.getAccessory();
        if (accessory != null)
            getAccessoryPanel().add(accessory);

        fc.add(centerPanel, BorderLayout.CENTER);

        // ********************************** //
        // **** Construct the bottom panel ** //
        // ********************************** //

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(1,1,1,1);

        JLabel fileNameLabel = new JLabel(fileNameLabelText);
        fileNameLabel.setDisplayedMnemonic(fileNameLabelMnemonic);
        c.weightx = 0.0;
        bottomPanel.add(fileNameLabel,c);

		fileNameTextField = new JTextField();
		fileNameTextField.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				if (!getFileChooser().isMultiSelectionEnabled())
					listSelectionModel.clearSelection();
			}
		});
		fileNameLabel.setLabelFor(fileNameTextField);
		if (fc.isMultiSelectionEnabled())
			setFileName(fileNameString(fc.getSelectedFiles()));
		else
			setFileName(fileNameString(fc.getSelectedFile()));
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.1;
        bottomPanel.add(fileNameTextField,c);

        JLabel filesOfTypeLabel = new JLabel(filesOfTypeLabelText);
        filesOfTypeLabel.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 0.0;
        bottomPanel.add(filesOfTypeLabel,c);

        filterComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterComboBoxModel);

        JComboBox filterComboBox = new JComboBox(filterComboBoxModel);
        filterComboBox.setRenderer(createFilterComboBoxRenderer());
        filesOfTypeLabel.setLabelFor(filterComboBox);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.1;
        bottomPanel.add(filterComboBox,c);

        // bottom buttons
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 10));

        approveButton = new JButton(getApproveButtonText(fc));
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        buttonPanel.add(approveButton);

        buttonPanel.add(Box.createRigidArea(new Dimension(10, 1)));

        cancelButton = new JButton(cancelButtonText);
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        buttonPanel.add(cancelButton);

        c.gridwidth = GridBagConstraints.REMAINDER;
        bottomPanel.add(buttonPanel,c);

        fc.add(bottomPanel, BorderLayout.SOUTH);
        createContextMenu();
    }

    protected void installStrings(JFileChooser fc)
    {
        super.installStrings(fc);
        Locale l = fc.getLocale();

        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", l);
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", l);

        fileNameLabelMnemonic=
            UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText=
            UIManager.getString("FileChooser.fileNameLabelText", l);

        filesOfTypeLabelMnemonic =
            UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText =
            UIManager.getString("FileChooser.filesOfTypeLabelText", l);

        upFolderToolTipText =
            UIManager.getString("FileChooser.upFolderToolTipText", l);
        upFolderAccessibleName =
            UIManager.getString("FileChooser.upFolderAccessibleName", l);

        homeFolderToolTipText =
            UIManager.getString("FileChooser.homeFolderToolTipText", l);
        homeFolderAccessibleName =
            UIManager.getString("FileChooser.homeFolderAccessibleName", l);

        newFolderToolTipText =
            UIManager.getString("FileChooser.newFolderToolTipText", l);
        newFolderAccessibleName =
            UIManager.getString("FileChooser.newFolderAccessibleName", l);

        listViewButtonToolTipText =
            UIManager.getString("FileChooser.listViewButtonToolTipText", l);
        listViewButtonAccessibleName =
            UIManager.getString("FileChooser.listViewButtonAccessibleName", l);

        detailsViewButtonToolTipText =
            UIManager.getString("FileChooser.detailsViewButtonToolTipText", l);
        detailsViewButtonAccessibleName =
            UIManager.getString("FileChooser.detailsViewButtonAccessibleName", l);
        fileNameHeaderText =
            UIManager.getString("FileChooser.fileNameHeaderText", l);
        fileSizeHeaderText =
            UIManager.getString("FileChooser.fileSizeHeaderText", l);
        fileTypeHeaderText =
			UIManager.getString("FileChooser.fileTypeHeaderText", l);
        fileDateHeaderText =
            UIManager.getString("FileChooser.fileDateHeaderText", l);
        fileAttrHeaderText =
			UIManager.getString("FileChooser.fileAttrHeaderText", l);
    }

    protected void createContextMenu()
    {
    	contextMenu = new JPopupMenu();
    	contextMenu.add(new JMenuItem(listViewButtonToolTipText));
    	contextMenu.add(new JMenuItem(detailsViewButtonToolTipText));
    }

    protected JPanel createList(JFileChooser fc)
    {
        JPanel p = new JPanel(new BorderLayout());
        final JFileChooser fileChooser = fc;
        list = new JList()
        {
            public int getNextMatch(String prefix, int startIndex, Position.Bias bias)
            {
                ListModel model = getModel();
                int max = model.getSize();
                if (prefix == null || startIndex < 0 || startIndex >= max)
                    throw new IllegalArgumentException();

                // start search from the next element before/after the selected element
                boolean backwards = (bias == Position.Bias.Backward);
                for (int i=startIndex; backwards ? i>=0 : i<max; i+=(backwards ? -1 : 1))
                {
                    String filename = fileChooser.getName((File) model.getElementAt(i));
                    if (filename.regionMatches(true, 0, prefix, 0, prefix.length()))
                        return i;
                }
                return -1;
            }
        };
        list.setCellRenderer(new FileRenderer());
        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        list.setVisibleRowCount(-1);
        list.setFixedCellHeight(17);

        if (fc.isMultiSelectionEnabled())
            list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        else
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        list.setModel(getModel());
        list.addListSelectionListener(createListSelectionListener(fc));
        list.addMouseListener(createDoubleClickListener(fc, list));
        getModel().addListDataListener(new ListDataListener()
        {
            public void contentsChanged(ListDataEvent e)
            {
                // Update the selection after JList has been updated
                new DelayedSelectionUpdater();
            }
            public void intervalAdded(ListDataEvent e)
            {
                new DelayedSelectionUpdater();
            }
            public void intervalRemoved(ListDataEvent e)
            {
            }
        });

        JScrollPane scrollpane = new JScrollPane(list);
        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }

    class DetailsTableModel extends AbstractTableModel implements ListDataListener
    {
        String[] columnNames =
        {
				fileNameHeaderText,
				fileSizeHeaderText,
				fileTypeHeaderText,
				fileDateHeaderText,
				fileAttrHeaderText
		};

        JFileChooser chooser;
        ListModel listModel;

        DetailsTableModel(JFileChooser fc)
        {
            this.chooser = fc;
            listModel = getModel();
            listModel.addListDataListener(this);
        }

        public int getRowCount()
        {
            return listModel.getSize();
        }

        public int getColumnCount()
        {
            return COLUMN_COLCOUNT;
        }

        public String getColumnName(int column)
        {
            return columnNames[column];
        }

        public Class getColumnClass(int column)
        {
            switch (column)
            {
                case COLUMN_FILENAME :
                    return File.class;
                case COLUMN_FILEDATE :
                    return Date.class;
                default :
                    return super.getColumnClass(column);
            }
        }

        public Object getValueAt(int row, int col)
        {
            // Note: It is very important to avoid getting info on drives, as
            // this will trigger "No disk in A:" and similar dialogs.
            //
            // Use (f.exists() && !chooser.getFileSystemView().isFileSystemRoot(f)) to
            // determine if it is safe to call methods directly on f.

            File f = (File) listModel.getElementAt(row);
            switch (col)
            {
                case COLUMN_FILENAME :
                    return f;

                case COLUMN_FILESIZE :
                    if (!f.exists() || f.isDirectory())
                        return null;

                    long len = f.length() >> 10;
                    if (len < 1024L)
                        return ((len == 0L) ? 1L : len) + " KB";
                    else
                    {
                        len >>= 10;
                        if (len < 1024L)
                            return len + " MB";
                        else
                        {
                            len >>= 10;
                            return len + " GB";
                        }
                    }
				case COLUMN_FILETYPE :
					if (!f.exists())
					{
						return null;
					}
					return chooser.getFileSystemView().getSystemTypeDescription(f);

				case COLUMN_FILEATTR :
					if (!f.exists() || chooser.getFileSystemView().isFileSystemRoot(f))
						return null;

					String attributes= "";

					if (!f.canWrite())
						attributes += "R";
					if (f.isHidden())
						attributes += "H";
					return attributes;

                case COLUMN_FILEDATE :
                    if (!f.exists() || chooser.getFileSystemView().isFileSystemRoot(f))
                        return null;

                    long time = f.lastModified();
                    return (time == 0L) ? null : new Date(time);
            }
            return null;
        }
        public void contentsChanged(ListDataEvent e)
        {
            fireTableDataChanged();
        }
        public void intervalAdded(ListDataEvent e)
        {
            fireTableDataChanged();
        }
        public void intervalRemoved(ListDataEvent e)
        {
            fireTableDataChanged();
        }
    }

    class DetailsTableCellRenderer extends DefaultTableCellRenderer
    {
        JFileChooser chooser;
        DateFormat df;

        DetailsTableCellRenderer(JFileChooser chooser)
        {
            this.chooser = chooser;
            df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, chooser.getLocale());
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
        {

            if (column == COLUMN_FILENAME)
            {
                setHorizontalAlignment(SwingConstants.LEADING);
            }
            else
            if (column == COLUMN_FILESIZE || column == COLUMN_FILEATTR)
            {
                setHorizontalAlignment(SwingConstants.TRAILING);
            }
            else
            {
                setHorizontalAlignment(SwingConstants.LEADING);
            }
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        public void setValue(Object value)
        {
            setIcon(null);
            if (value instanceof File)
            {
                File file = (File) value;
                setText(chooser.getName(file));
                setIcon(LipstikFileChooserUI.getIcon(file, fileIcon, getFileChooser().getFileSystemView()));
            }
            else
            if (value != null && value instanceof Date)
                setText(df.format((Date) value));
            else
                super.setValue(value);
        }
    }

    protected JPanel createDetailsView(final JFileChooser fc)
    {
        DetailsTableModel detailsTableModel= new DetailsTableModel(fc);
        JPanel p = new JPanel(new BorderLayout());

        detailsTable = new JTable(detailsTableModel)
        {
            // Handle Escape key events here
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    fc.dispatchEvent(e);
                    return true;
                }
                return super.processKeyBinding(ks, e, condition, pressed);
            }
        };

        detailsTable.setComponentOrientation(fc.getComponentOrientation());
        detailsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        detailsTable.setShowGrid(false);
        detailsTable.setRowHeight(17);
        detailsTable.setSelectionModel(listSelectionModel);
        detailsTable.setIntercellSpacing(new Dimension(1,1));

        TableColumnModel columnModel = detailsTable.getColumnModel();
        TableColumn[] columns = new TableColumn[COLUMN_COLCOUNT];

        for (int i= 0; i < COLUMN_COLCOUNT; i++)
        {
            columns[i]= columnModel.getColumn(i);
            columns[i].setPreferredWidth(COLUMN_WIDTHS[i]);
        }

        if (!System.getProperty("os.name").startsWith("Windows"))
		{
			columnModel.removeColumn(columns[COLUMN_FILETYPE]);
			columnModel.removeColumn(columns[COLUMN_FILEATTR]);
		}

        TableCellRenderer cellRenderer= new DetailsTableCellRenderer(fc);
        detailsTable.setDefaultRenderer(File.class, cellRenderer);
        detailsTable.setDefaultRenderer(Date.class, cellRenderer);
        detailsTable.setDefaultRenderer(Object.class, cellRenderer);
        detailsTable.setSelectionModel(listSelectionModel);
        detailsTable.addMouseListener(new MouseListener()
                {
                    public void mouseClicked(MouseEvent e)
                    {
                        int index = detailsTable.rowAtPoint(e.getPoint());
                        if (index >= 0 && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                        {
                            File f = (File)list.getModel().getElementAt(index);
                            try
                            {
                            	// Strip trailing ".."
                                f = f.getCanonicalFile();
                            }
                            catch (IOException ex)
                            {
                            	// That's ok, we'll use f as is
                            }
                            if (fc.isTraversable(f))
                            {
                                detailsTable.clearSelection();
                                // Traverse shortcuts on Windows
                                if (File.separatorChar == '\\' && f.getPath().endsWith(".lnk"))
                                {
                                    try
                                    {
                                        File linkedTo = ShellFolder.getShellFolder(f).getLinkLocation();
                                        if (linkedTo != null && fc.isTraversable(linkedTo))
                                            f = linkedTo;
                                        else
                                            return;
                                    }
                                    catch (FileNotFoundException ex) { return; }
                                }
                                fc.setCurrentDirectory(f);
                            }
                            else getFileChooser().approveSelection();
                        }
                    }
                    public void mousePressed(MouseEvent e)
                    {
                    }
                    public void mouseReleased(MouseEvent e)
                    {
                    }
                    public void mouseEntered(MouseEvent e)
                    {
                    }
                    public void mouseExited(MouseEvent e)
                    {
                    }
                });

        JScrollPane scrollpane= new JScrollPane(detailsTable);
        scrollpane.setComponentOrientation(fc.getComponentOrientation());
        LookAndFeel.installColors(scrollpane.getViewport(), "Table.background", "Table.foreground");

        // Adjust width of first column so the table fills the viewport when
        // first displayed (temporary listener).
        scrollpane.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                JScrollPane sp = (JScrollPane) e.getComponent();
                fixNameColumnWidth(sp.getViewport().getSize().width);
                sp.removeComponentListener(this);
            }
        });

        p.add(scrollpane, BorderLayout.CENTER);
        return p;
    }

    private void fixNameColumnWidth(int viewWidth)
    {
        TableColumn nameCol = detailsTable.getColumnModel().getColumn(COLUMN_FILENAME);
        int tableWidth = detailsTable.getPreferredSize().width;

        if (tableWidth < viewWidth)
            nameCol.setPreferredWidth(nameCol.getPreferredWidth() + viewWidth - tableWidth);
    }

    private class DelayedSelectionUpdater implements Runnable
    {
        DelayedSelectionUpdater()
        {
            SwingUtilities.invokeLater(this);
        }

        public void run()
        {
            setFileSelected();
        }
    }

    /**
     * Creates a selection listener for the list of files and directories.
     *
     * @param fc a <code>JFileChooser</code>
     * @return a <code>ListSelectionListener</code>
     */
    public ListSelectionListener createListSelectionListener(JFileChooser fc)
    {
        return new SelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    JFileChooser chooser = getFileChooser();
                    FileSystemView fsv = chooser.getFileSystemView();
                    JList list = (JList) e.getSource();

                    if (chooser.isMultiSelectionEnabled())
                    {
                        File[] files = null;
                        Object[] objects = list.getSelectedValues();

                        int j=0,n=objects.length;

                        if (n == 1
                            && ((File) objects[0]).isDirectory()
                            && chooser.isTraversable(((File) objects[0]))
                            && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY
                                || !fsv.isFileSystem(((File) objects[0]))))
                        {
                            setDirectorySelected(true);
                            setDirectory(((File) objects[0]));
                        }
                        else
                        {
                            files = new File[n];
                            for (int i=0; i<n; i++)
                            {
                                File f = (File) objects[i];
                                if ((chooser.isFileSelectionEnabled() && f.isFile())
                                    || (chooser.isDirectorySelectionEnabled()
                                        && fsv.isFileSystem(f)
                                        && f.isDirectory()))
                                {
                                    files[j++] = f;
                                }
                            }
                            if (j == 0)
                                files = null;
                            else
                            if (j < n)
                            {
                                File[] tmpFiles = new File[j];
                                System.arraycopy(files, 0, tmpFiles, 0, j);
                                files = tmpFiles;
                            }
                            setDirectorySelected(false);
                        }
                        chooser.setSelectedFiles(files);
                    }
                    else
                    {
                        File file = (File) list.getSelectedValue();
                        if (file != null
                            && file.isDirectory()
                            && chooser.isTraversable(file)
                            && (chooser.getFileSelectionMode() == JFileChooser.FILES_ONLY
                                || !fsv.isFileSystem(file)))
                        {
                            setDirectorySelected(true);
                            setDirectory(file);
                            chooser.setSelectedFile(null);
                        }
                        else
                        {
                            setDirectorySelected(false);
                            if (file != null)
                                chooser.setSelectedFile(file);
                        }
                    }
                }
            }
        };
    }


    protected class FileRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {

            super.getListCellRendererComponent(
                list,
                value,
                index,
                isSelected,
                cellHasFocus);

            File file = (File) value;
            setText(getFileChooser().getName(file));
            setIcon(LipstikFileChooserUI.getIcon(file, fileIcon, getFileChooser().getFileSystemView()));
            return this;
        }
    }


    /**
     * Uninstall the UI delegate for the given component
     *
     * @param c The component for which to install the ui delegate.
     */
    public void uninstallUI(JComponent c)
    {
        // Remove listeners
        c.removePropertyChangeListener(filterComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        fileNameTextField.removeActionListener(getApproveSelectionAction());
        super.uninstallUI(c);
    }

    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    public Dimension getPreferredSize(JComponent c)
    {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null)
            return new Dimension(d.width < prefWidth ? prefWidth : d.width,
                                 d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        else
            return new Dimension(prefWidth, PREF_SIZE.height);
    }

    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    public Dimension getMinimumSize(JComponent c)
    {
        return MIN_SIZE;
    }

    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    public Dimension getMaximumSize(JComponent c)
    {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    void setFileSelected()
    {
        if (getFileChooser().isMultiSelectionEnabled() && !isDirectorySelected())
        {
            File[] files = getFileChooser().getSelectedFiles();
            // Should be selected
            Object[] selectedObjects = list.getSelectedValues();
            // Are actually selected

            // Remove files that shouldn't be selected
            for (int j=0,i; j < selectedObjects.length; j++)
            {
                for (i=0; i < files.length; i++)
                    if (files[i].equals(selectedObjects[j]))
                        break;

                if (i == files.length)
                    if ((i=getModel().indexOf(selectedObjects[j])) >= 0)
                        listSelectionModel.removeSelectionInterval(i, i);
            }
            // Add files that should be selected
            for (int i=0,j; i < files.length; i++)
            {
                for (j=0; j < selectedObjects.length; j++)
                    if (files[i].equals(selectedObjects[j]))
                        break;

                if (j == selectedObjects.length)
                    if ((j=getModel().indexOf(files[i])) >= 0)
                        listSelectionModel.addSelectionInterval(j, j);
            }
        }
        else
        {
            JFileChooser chooser = getFileChooser();
            File f;
            if (isDirectorySelected())
                f = getDirectory();
            else
                f = chooser.getSelectedFile();

            int i;
            if (f != null && (i=getModel().indexOf(f)) >= 0)
            {
                listSelectionModel.setSelectionInterval(i, i);
                ensureIndexIsVisible(i);
            }
            else
                listSelectionModel.clearSelection();
        }
    }


    /**	Returns the icon for the specified file.
     *
     * 	@param	f			The file for which to return the icon
     * 	@param	fileIcon	The default file icon
     * 	@param	fsv			The file system view from which we get
     * 								the system-specific file icons
     *
     * 	@return				The icon for the specified file, or the
     * 						default icon, if the file system view did not
     * 						provide a suitable icon.
     */
    public static Icon getIcon(File f, Icon fileIcon, FileSystemView fsv)
    {
        Icon icon = (Icon)iconCache.get(f);
        if (icon != null)
            return icon;

        icon = fileIcon;
        if (f != null)
        {
            if(f.isDirectory() && !fsv.isDrive(f) && !fsv.isFileSystemRoot(f) && !fsv.isComputerNode(f)
                    && !fsv.isFloppyDrive(f) && !fsv.isRoot(f))
                icon = UIManager.getIcon("FileChooser.folderIcon");
            else
                icon = fsv.getSystemIcon(f);
        }
        iconCache.put(f, icon);
        return icon;
    }


    private String fileNameString(File file)
    {
        if (file == null)
            return null;
        else
        {
            JFileChooser fc = getFileChooser();
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled())
                return file.getPath();
            else
                return file.getName();
        }
    }

    private String fileNameString(File[] files)
    {
        if (files == null)
            return "";

        StringBuffer buf = new StringBuffer();
        for (int i=0,n=files.length; i<n; i++)
        {
            if (i > 0)
                buf.append(" ");
            if (n > 1)
                buf.append("\"");

            buf.append(fileNameString(files[i]));

            if (n > 1)
                buf.append("\"");
        }
        return buf.toString();
    }

    /* The following methods are used by the PropertyChange Listener */

    private void doSelectedFileChanged(PropertyChangeEvent e)
    {
        File f= (File) e.getNewValue();
        JFileChooser fc= getFileChooser();
        if (f != null
            && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled())))
        {
            setFileName(fileNameString(f));
        }
        setFileSelected();
    }

    private void doSelectedFilesChanged(PropertyChangeEvent e)
    {
        File[] files= (File[]) e.getNewValue();
        JFileChooser fc= getFileChooser();
        if (files != null
            && files.length > 0
            && (files.length > 1
                || fc.isDirectorySelectionEnabled()
                || !files[0].isDirectory()))
        {
            setFileName(fileNameString(files));
        }
        setFileSelected();
    }

    private void doDirectoryChanged()
    {
        JFileChooser fc= getFileChooser();
        FileSystemView fsv= fc.getFileSystemView();
        clearIconCache();
        listSelectionModel.clearSelection();
        ensureIndexIsVisible(0);
        File currentDirectory= fc.getCurrentDirectory();
        if (currentDirectory != null)
        {
            directoryComboBoxModel.addItem(currentDirectory);
            // Currently can not create folder in the Desktop folder on Windows
            // (ShellFolder limitation)
            getNewFolderAction().setEnabled(
                fsv.isFileSystem(currentDirectory) && currentDirectory.canWrite());
            getChangeToParentDirectoryAction().setEnabled(
                !fsv.isRoot(currentDirectory));

            if (fc.isDirectorySelectionEnabled()
                && !fc.isFileSelectionEnabled()
                && fsv.isFileSystem(currentDirectory))
            {
                setFileName(currentDirectory.getPath());
            }
        }
    }

    private void doFilterChanged()
    {
        clearIconCache();
        listSelectionModel.clearSelection();
    }

    private void doFileSelectionModeChanged()
    {
        clearIconCache();
        listSelectionModel.clearSelection();

        JFileChooser fc= getFileChooser();
        File currentDirectory= fc.getCurrentDirectory();
        if (currentDirectory != null
            && fc.isDirectorySelectionEnabled()
            && !fc.isFileSelectionEnabled()
            && fc.getFileSystemView().isFileSystem(currentDirectory))
        {

            setFileName(currentDirectory.getPath());
        }
        else
        {
            setFileName(null);
        }
    }

    private void doMultiSelectionChanged()
    {
        if (getFileChooser().isMultiSelectionEnabled())
        {
            listSelectionModel.setSelectionMode(
                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
        else
        {
            listSelectionModel.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
            listSelectionModel.clearSelection();
            getFileChooser().setSelectedFiles(null);
        }
    }

    private void doAccessoryChanged(PropertyChangeEvent e)
    {
        if (getAccessoryPanel() != null)
        {
            if (e.getOldValue() != null)
            {
                getAccessoryPanel().remove((JComponent) e.getOldValue());
            }
            JComponent accessory= (JComponent) e.getNewValue();
            if (accessory != null)
            {
                getAccessoryPanel().add(accessory, BorderLayout.CENTER);
            }
        }
    }

    private void doApproveButtonTextChanged()
    {
        JFileChooser chooser= getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
    }

    private void doDialogTypeChanged()
    {
        JFileChooser chooser= getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG)
        {
            lookInLabel.setText(saveInLabelText);
        }
        else
        {
            lookInLabel.setText(lookInLabelText);
        }
    }


    /*
      * Listen for filechooser property changes, such as
      * the selected file changing, or the type of the dialog changing.
      */
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc)
    {
        return new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                String s= e.getPropertyName();
                if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY))
                {
                    doSelectedFileChanged(e);
                }
                else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY))
                {
                    doSelectedFilesChanged(e);
                }
                else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY))
                {
                    doDirectoryChanged();
                }
                else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
                {
                    doFilterChanged();
                }
                else if (
                    s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY))
                {
                    doFileSelectionModeChanged();
                }
                else if (
                    s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY))
                {
                    doMultiSelectionChanged();
                }
                else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY))
                {
                    doAccessoryChanged(e);
                }
                else if (
                    s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY)
                        || s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY))
                {
                    doApproveButtonTextChanged();
                }
                else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY))
                {
                    doDialogTypeChanged();
                }
                else if (
                    s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY))
                {
                    //doApproveButtonMnemonicChanged();
                }
                else if (
                    s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY))
                {
                	buttonPanel.setVisible(getFileChooser().getControlButtonsAreShown());
                }
                else if (s.equals("componentOrientation"))
                {
                    ComponentOrientation o = (ComponentOrientation) e.getNewValue();
                    JFileChooser cc = (JFileChooser) e.getSource();
                    if (o != e.getOldValue())
                    {
                        cc.applyComponentOrientation(o);
                    }
                    if (detailsTable != null)
                    {
                        detailsTable.setComponentOrientation(o);
                        detailsTable.getParent().getParent().setComponentOrientation(o);
                    }
                }
                else if (s.equals("ancestor"))
                {
                    if (e.getOldValue() == null && e.getNewValue() != null)
                    {
                        // Ancestor was added, set initial focus
                        fileNameTextField.selectAll();
                        fileNameTextField.requestFocus();
                    }
                }
            }
        };
    }

    private void ensureIndexIsVisible(int i)
    {
        if (i >= 0)
        {
            list.ensureIndexIsVisible(i);
            if (detailsTable != null)
            {
                detailsTable.scrollRectToVisible(
                    detailsTable.getCellRect(i, COLUMN_FILENAME, true));
            }
        }
    }


    /**	Ensures that the specified file in the specified JFileChooser
     * 	is visible in the file list.
     */
    public void ensureFileIsVisible(JFileChooser fc, File f)
    {
        ensureIndexIsVisible(getModel().indexOf(f));
    }


    /**	Updates the display for the current directory, by rescanning it
     * 	and updating the files contained in the directory.
     */
    public void rescanCurrentDirectory(JFileChooser fc)
    {
        getModel().validateFileCache();
    }


    /**	Returns the file name of the selected file, or null if
     * 	the user has not entered or selected a file.
     */
    public String getFileName()
    {
        if (fileNameTextField != null)
        {
            return fileNameTextField.getText();
        }
        else
        {
            return null;
        }
    }


    /**	Sets the file name in the file name text field */
    public void setFileName(String filename)
    {
        if (fileNameTextField != null)
        {
            fileNameTextField.setText(filename);
        }
    }

    /**
     * Property to remember whether a directory is currently selected in the UI.
     * This is normally called by the UI on a selection event.
     *
     * @param directorySelected if a directory is currently selected.
     * @since 1.4
     */
    protected void setDirectorySelected(boolean directorySelected)
    {
        super.setDirectorySelected(directorySelected);
        JFileChooser chooser= getFileChooser();
        if (directorySelected)
        {
            approveButton.setText(directoryOpenButtonText);
            approveButton.setToolTipText(directoryOpenButtonToolTipText);
        }
        else
        {
            approveButton.setText(getApproveButtonText(chooser));
            approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        }
    }

    //
    // Renderer for DirectoryComboBox
    //
    class DirectoryComboBoxRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value == null)
            {
                setText("");
                return this;
            }

            File directory = (File) value;
            setText(getFileChooser().getName(directory));
            Icon icon = LipstikFileChooserUI.getIcon(directory, fileIcon, getFileChooser().getFileSystemView());
            setIcon(IndentIcon.getIndentInstance(icon, directoryComboBoxModel.getDepth(index)));
            return this;
        }
    }

    static IndentIcon indentIcon = null;
    static class IndentIcon implements Icon
    {
        Icon icon = null;
        int depth = 0;
        public static IndentIcon getIndentInstance(Icon icon, int depth)
        {
            if (indentIcon == null)
                indentIcon = new IndentIcon();

            indentIcon.icon = icon;
            indentIcon.depth = depth;
            return indentIcon;
        }
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            if (c.getComponentOrientation().isLeftToRight())
                icon.paintIcon(c, g, x + depth * 10, y-2);
            else
                icon.paintIcon(c, g, x, y-2);
        }
        public int getIconWidth()   { return icon.getIconWidth() + depth * 10; }
        public int getIconHeight()  { return icon.getIconHeight()-4;            }

    }

    //
    // DataModel for DirectoryComboxbox
    //
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc)
    {
        return new DirectoryComboBoxModel();
    }

    /**
     * Data model for a type-face selection combo-box.
     */
    class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel
    {
        Vector directories     = new Vector(5);
        JFileChooser chooser   = getFileChooser();
        File selectedDirectory = null;
        FileSystemView fsv     = chooser.getFileSystemView();
        int depthLo = 0;
        int depthHi = 0;

        public DirectoryComboBoxModel()
        {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if (dir != null)
                addItem(dir);
        }

        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(File directory)
        {
            if (directory == null)
                return;

            directories.clear();

            File[] baseFolders= useShellFolder ? (File[]) ShellFolder.get("fileChooserComboBoxFolders") : fsv.getRoots();

            directories.addAll(Arrays.asList(baseFolders));

            // Get the canonical (full) path. This has the side
            // benefit of removing extraneous chars from the path,
            // for example /foo/bar/ becomes /foo/bar
            File canonical;
            try
            {
                canonical = directory.getCanonicalFile();
            }
            catch (IOException e)
            {
                // Maybe drive is not ready. Can't abort here.
                canonical = directory;
            }

            // create File instances of each directory leading up to the top
            try
            {
                File sf = ShellFolder.getShellFolder(canonical);
                File f  = sf;
                Vector path = new Vector(10);
                do
                {
                    path.addElement(f);
                }
                while ((f = f.getParentFile()) != null);

                int pathCount = path.size();
                // Insert chain at appropriate place in vector
                for (int i=0; i < pathCount; i++)
                {
                    f= (File) path.get(i);
                    if (directories.contains(f))
                    {
                    	depthLo = directories.indexOf(f);
                    	depthHi = depthLo;
                        for (int j=i-1; j >= 0; j--,depthHi++)
                            directories.insertElementAt(path.get(j), depthLo + i - j);
                        break;
                    }
                }
                setSelectedItem(sf);
            }
            catch (FileNotFoundException ex)
            {
                System.err.println(ex);
            }
        }

        public int getDepth(int i)
        {
            return (i>=depthLo && i <= depthHi) ? i-depthLo : 0;
        }

        public void setSelectedItem(Object selectedDirectory)
        {
            this.selectedDirectory = (File) selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }

        public Object getSelectedItem()
        {
            return selectedDirectory;
        }

        public int getSize()
        {
            return directories.size();
        }

        public Object getElementAt(int index)
        {
            return directories.elementAt(index);
        }
    }

    //
    // Renderer for Types ComboBox
    //
    protected FilterComboBoxRenderer createFilterComboBoxRenderer()
    {
        return new FilterComboBoxRenderer();
    }

    /**
     * Render different type sizes and styles.
     */
    public static class FilterComboBoxRenderer extends DefaultListCellRenderer
    {
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus)
        {

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null && value instanceof FileFilter)
                setText(((FileFilter) value).getDescription());
            return this;
        }
    }

    //
    // DataModel for Types Comboxbox
    //
    protected FilterComboBoxModel createFilterComboBoxModel()
    {
        return new FilterComboBoxModel();
    }

    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener
    {
        protected FileFilter[] filters;
        protected FilterComboBoxModel()
        {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }

        public void propertyChange(PropertyChangeEvent e)
        {
            String prop = e.getPropertyName();
            if (prop.equals(JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY))
            {
                filters= (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            }
            else if (prop.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY))
            {
                fireContentsChanged(this, -1, -1);
            }
        }

        public void setSelectedItem(Object filter)
        {
            if (filter != null)
            {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }

        public Object getSelectedItem()
        {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter= getFileChooser().getFileFilter();

            boolean found = false;
            if (currentFilter != null)
            {
                for (int i=0; i < filters.length; i++)
                    if (filters[i] == currentFilter)
                    {
                        found = true;
                        break;
                    }

                if (!found)
                    getFileChooser().addChoosableFileFilter(currentFilter);
            }
            return getFileChooser().getFileFilter();
        }

        public int getSize()
        {
            return filters != null ? filters.length : 0;
        }

        public Object getElementAt(int index)
        {
            if (index > getSize() - 1)
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();

            return (filters != null) ? filters[index] : null;
        }
    }


    /**	Informs this instance that the selection has changed. */
    public void valueChanged(ListSelectionEvent e)
    {
        JFileChooser fc = getFileChooser();
        File f = fc.getSelectedFile();
        if (!e.getValueIsAdjusting() && f != null && !getFileChooser().isTraversable(f))
            setFileName(fileNameString(f));

    }

    /**
     * Acts when DirectoryComboBox has changed the selected item.
     */
    class DirectoryComboBoxAction extends AbstractAction
    {
        protected DirectoryComboBoxAction()
        {
            super("DirectoryComboBoxAction");
        }

        public void actionPerformed(ActionEvent e)
        {
            File f = (File) directoryComboBox.getSelectedItem();
            getFileChooser().setCurrentDirectory(f);
        }
    }

    protected JButton getApproveButton(JFileChooser fc)
    {
        return approveButton;
    }

    public void mouseClicked(MouseEvent e)
    {
        if (list != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
        {
            int index = list.locationToIndex(e.getPoint());
            if (index >= 0)
            {
                File f = (File)list.getModel().getElementAt(index);
                try
                {
                    // Strip trailing ".."
                    f = f.getCanonicalFile();
                }
                catch (IOException ex)
                { // That's ok, we'll use f as is

                }
                if(getFileChooser().isTraversable(f))
                {
                    list.clearSelection();
                    changeDirectory(f);
                }
                else
                    getFileChooser().approveSelection();
            }
        }

    }


    public void mousePressed(MouseEvent e)
    {
    }


    public void mouseReleased(MouseEvent e)
    {
    }


    public void mouseEntered(MouseEvent e)
    {
    }


    public void mouseExited(MouseEvent e)
    {
    }

    private void changeDirectory(File dir) {
        JFileChooser fc = getFileChooser();

        // Traverse shortcuts on Windows
        if (dir != null && File.separatorChar == '\\' && dir.getPath().endsWith(".lnk"))
        {
            try {
	            File linkedTo = ShellFolder.getShellFolder(dir).getLinkLocation();
	            if (linkedTo != null && fc.isTraversable(linkedTo))
	                dir = linkedTo;
	            else
	                return;

            } catch (FileNotFoundException ex) { return; }
        }
        fc.setCurrentDirectory(dir);
    }
}
