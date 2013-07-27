/*
* TaskListTaskTypesOptionPane.java - TaskList plugin
* Copyright (C) 2001 Oliver Rutherfurd
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
*
* $Id: TaskListTaskTypesOptionPane.java 20709 2012-01-03 16:41:24Z daleanson $
*/

package tasklist.options;

//{{{ imports
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;
import java.util.regex.*;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import tasklist.*;
//}}}

public class TaskListTaskTypesOptionPane extends AbstractOptionPane {
    //{{{ constructor
    public TaskListTaskTypesOptionPane() {
        super( "tasklist.tasktypes" );
    } //}}}

    //{{{ _init() method
    protected void _init() {
        setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

        addComponent( new JLabel( jEdit.getProperty(
                    "options.tasklist.tasktypes.patterns" ) ) );

        JPanel types = new JPanel( new BorderLayout() );
        typesListModel = createListModel();
        typesList = new JList( typesListModel );
        types.add( BorderLayout.CENTER, new JScrollPane( typesList ) );
        typesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        typesList.setCellRenderer( new TaskTypeCellRenderer() );
        typesList.addListSelectionListener( new ListHandler() );
        typesList.addMouseListener( new MouseHandler() );

        JPanel buttons = new JPanel();
        buttons.add( editBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.edit", "Edit" ) ) );
        editBtn.addActionListener( new ActionHandler() );
        buttons.add( addBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.add", "Add" ) ) );
        addBtn.addActionListener( new ActionHandler() );
        buttons.add( removeBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.remove", "Remove" ) ) );
        removeBtn.addActionListener( new ActionHandler() );
        buttons.add( upBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.up", "Up" ) ) );
        upBtn.addActionListener( new ActionHandler() );
        buttons.add( downBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.down", "Down" ) ) );
        downBtn.addActionListener( new ActionHandler() );
        buttons.add( resetBtn = new JButton( jEdit.getProperty(
                    "options.tasklist.tasktypes.reset", "Reset" ) ) );
        resetBtn.addActionListener( new ActionHandler() );
        types.add( BorderLayout.SOUTH, buttons );

        updateButtons();

        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++;
        cons.gridheight = cons.REMAINDER;
        cons.gridwidth = cons.REMAINDER;
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx = cons.weighty = 1.0f;

        gridBag.setConstraints( types, cons );
        add( types );

        iconList = new DefaultComboBoxModel();
        StringTokenizer st = new StringTokenizer(
                    jEdit.getProperty( "tasklist.icons" ) );
        while ( st.hasMoreElements() ) {
            String icon = st.nextToken();
            iconList.addElement( new IconListEntry(
                        TaskType.loadIcon( icon ), icon ) );
        }
    } //}}}

    //{{{ _save() method
    public void _save() {
        int i = 0;
        while ( i < typesListModel.getSize() ) {
            ( ( TaskType ) typesListModel.getElementAt( i ) ).save( i );
            i++;
        }
        TaskListPlugin.pruneTaskListProperties( i );
        TaskListPlugin.reloadTaskTypes();
        View[] views = jEdit.getViews();
        for (View view : views) {
            EditBus.send(new ParseBufferMessage(view, view.getBuffer(), ParseBufferMessage.REINIT));
            EditBus.send(new ParseBufferMessage(view, view.getBuffer(), ParseBufferMessage.DO_PARSE_ALL));
        }
    } //}}}

    //{{{ createListModel() method
    private DefaultListModel createListModel() {
        DefaultListModel listModel = new DefaultListModel();

        int i = 0;
        String pattern;
        while ( ( pattern = jEdit.getProperty( "tasklist.tasktype." + i
                + ".pattern" ) ) != null ) {
            String name = jEdit.getProperty(
                        "tasklist.tasktype." + i + ".name" );
            String iconPath = jEdit.getProperty(
                        "tasklist.tasktype." + i + ".iconpath" );
            String sample = jEdit.getProperty(
                        "tasklist.tasktype." + i + ".sample" );
            boolean ignoreCase = jEdit.getBooleanProperty(
                        "tasklist.tasktype." + i + ".ignorecase" );

            listModel.addElement( new TaskType(
                        name, pattern, sample, ignoreCase, iconPath ) );

            i++;
        }
        return listModel;
    } //}}}

    //{{{ updateButtons() method
    private void updateButtons() {
        int index = typesList.getSelectedIndex();

        editBtn.setEnabled( index != -1 );
        removeBtn.setEnabled( index != -1 && typesListModel.getSize() != 0 );
        upBtn.setEnabled( index > 0 );
        downBtn.setEnabled( index != -1 && index != typesListModel.getSize() - 1 );
    } //}}}

    //{{{ ActionHandler class
    class ActionHandler implements ActionListener {
        public void actionPerformed( ActionEvent evt ) {
            Object source = evt.getSource();

            if ( source == editBtn ) {
                TaskType taskType = ( TaskType ) typesList.getSelectedValue();
                new TaskTypeDialog( TaskListTaskTypesOptionPane.this, taskType,
                        TaskListTaskTypesOptionPane.this.iconList );
                typesList.repaint();
            }
            else if ( source == addBtn ) {
                TaskType taskType = new TaskType();
                if ( new TaskTypeDialog( TaskListTaskTypesOptionPane.this, taskType, TaskListTaskTypesOptionPane.this.iconList ).isOK() ) {
                    int index = typesList.getSelectedIndex();
                    if ( index == -1 ) {
                        index = typesListModel.getSize();
                    }
                    else {
                        index++;
                    }

                    typesListModel.insertElementAt( taskType, index );
                    typesList.setSelectedIndex( index );
                }
            }
            else if ( source == removeBtn ) {
                typesListModel.removeElementAt( typesList.getSelectedIndex() );
                updateButtons();
            }
            else if ( source == upBtn ) {
                int index = typesList.getSelectedIndex();
                Object selected = typesList.getSelectedValue();
                typesListModel.removeElementAt( index );
                typesListModel.insertElementAt( selected, index - 1 );
                typesList.setSelectedIndex( index - 1 );
            }
            else if ( source == downBtn ) {
                int index = typesList.getSelectedIndex();
                Object selected = typesList.getSelectedValue();
                typesListModel.removeElementAt( index );
                typesListModel.insertElementAt( selected, index + 1 );
                typesList.setSelectedIndex( index + 1 );
            }
            else if ( source == resetBtn ) {
                TaskListPlugin.resetPatterns( jEdit.getActiveView() );
                typesListModel = createListModel();
                typesList.setModel( typesListModel );
            }
        }
    } //}}}

    //{{{ ListHandler class
    class ListHandler implements ListSelectionListener {
        public void valueChanged( ListSelectionEvent evt ) {
            updateButtons();
        }
    } //}}}

    //{{{ MouseHandler class
    class MouseHandler extends MouseAdapter {
        public void mouseClicked( MouseEvent evt ) {
            if ( evt.getClickCount() == 2 ) {
                TaskType taskType = ( TaskType ) typesList.getSelectedValue();
                new TaskTypeDialog( TaskListTaskTypesOptionPane.this, taskType, TaskListTaskTypesOptionPane.this.iconList );
                typesList.repaint();
            }
        }
    } //}}}

    //{{{ TaskTypeCellRenderer class
    class TaskTypeCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus ) {
            TaskType taskType = ( TaskType ) value;

            super.getListCellRendererComponent( list, taskType,
                    index, isSelected, cellHasFocus );

            setIcon( taskType.getIcon() );

            return this;
        }
    } //}}}

    //{{{ private members
    private JList typesList;
    private DefaultListModel typesListModel;

    private DefaultComboBoxModel iconList;

    private JButton addBtn;
    private JButton editBtn;
    private JButton removeBtn;
    private JButton upBtn;
    private JButton downBtn;
    private JButton resetBtn;
    //}}}

}

//{{{ TaskTypeDialog class
class TaskTypeDialog extends EnhancedDialog {
    //{{{ constructor
    public TaskTypeDialog( Component comp, TaskType taskType,
            ComboBoxModel iconListModel ) {
        super( JOptionPane.getFrameForComponent( comp ),
               jEdit.getProperty( "options.tasklist.tasktype.title" ), true );

        this.taskType = taskType;

        ActionHandler actionHandler = new ActionHandler();

        JPanel panel = new JPanel(
                    new VariableGridLayout(
                        VariableGridLayout.FIXED_NUM_COLUMNS, 2, 3, 3 ) );
        panel.setBorder( new EmptyBorder( 12, 12, 6, 12 ) );
        JLabel label;

        // name
        label = new JLabel( jEdit.getProperty(
                    "options.tasklist.tasktype.name" ) );
        label.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        panel.add( label );
        panel.add( name = new JTextField( taskType.getName() ) );
        name.setEditable(false);

        // sample
        label = new JLabel( jEdit.getProperty(
                    "options.tasklist.tasktype.sample" ) );
        label.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        panel.add( label );
        panel.add( sample = new JTextField( taskType.getSample() ) );

        // pattern
        label = new JLabel( jEdit.getProperty(
                    "options.tasklist.tasktype.pattern" ) );
        label.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        panel.add( label );
        panel.add( pattern = new JTextField( taskType.getPattern() ) );

        // ignore case
        label = new JLabel( jEdit.getProperty(
                    "options.tasklist.tasktype.ignorecase" ) );
        label.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        panel.add( label );
        panel.add( ignoreCase = new JCheckBox( "", taskType.getIgnoreCase() ) );

        // icon
        ButtonGroup grp = new ButtonGroup();
        useBuiltin = new JRadioButton( jEdit.getProperty(
                    "options.tasklist.tasktype.usebuiltin" ) );
        useBuiltin.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        useBuiltin.addActionListener( actionHandler );
        useBuiltin.setSelected( true );
        grp.add( useBuiltin );
        panel.add( useBuiltin );
        panel.add( builtinIcons = new JComboBox( iconListModel ) );
        builtinIcons.setRenderer( new IconCellRenderer() );

        useCustom = new JRadioButton( jEdit.getProperty(
                    "options.tasklist.tasktype.usecustom" ) );
        useCustom.setBorder( new EmptyBorder( 0, 0, 0, 12 ) );
        useCustom.addActionListener( actionHandler );
        grp.add( useCustom );
        panel.add( useCustom );
        panel.add( customIcon = new JButton(
                    jEdit.getProperty( "options.tasklist.tasktype.noicon" ) ) );
        customIcon.addActionListener( actionHandler );

        //Log.log(Log.DEBUG, TaskListTaskTypesOptionPane.class,
        // "iconPath=" + taskType.getIconPath());

        if ( taskType.getIconPath().startsWith( "file:" ) ) {
            iconListModel.setSelectedItem( iconListModel.getElementAt( 0 ) );
            customIcon.setIcon( taskType.getIcon() );
            customIcon.setText( MiscUtilities.getFileName( taskType.getIconPath() ) );
            useCustom.setSelected( true );
        }
        else {
            customIcon.setIcon( null );

            Object obj = null;
            for ( int i = 0; i < iconListModel.getSize(); i++ ) {
                obj = iconListModel.getElementAt( i );
                if ( obj instanceof IconListEntry ) {
                    if ( ( ( IconListEntry ) obj ).icon.equals( taskType.getIcon() ) ) {
                        iconListModel.setSelectedItem( obj );
                        break;
                    }
                }
            }
        }

        getContentPane().add( BorderLayout.CENTER, panel );

        JPanel southPanel = new JPanel();
        southPanel.setLayout( new BoxLayout( southPanel, BoxLayout.X_AXIS ) );
        southPanel.setBorder( new EmptyBorder( 6, 0, 12, 0 ) );
        southPanel.add( Box.createGlue() );
        okBtn = new JButton( jEdit.getProperty( "common.ok" ) );
        okBtn.addActionListener( actionHandler );
        getRootPane().setDefaultButton( okBtn );
        southPanel.add( okBtn );
        southPanel.add( Box.createHorizontalStrut( 6 ) );
        cancelBtn = new JButton( jEdit.getProperty( "common.cancel" ) );
        cancelBtn.addActionListener( actionHandler );
        southPanel.add( cancelBtn );
        southPanel.add( Box.createGlue() );

        getContentPane().add( BorderLayout.SOUTH, southPanel );

        updateEnabled();

        pack();
        setLocationRelativeTo( JOptionPane.getFrameForComponent( comp ) );
        setVisible( true );
    } //}}}

    //{{{ ok() method
    public void ok() {
        String _name, _pattern, _iconName, _sample;
        IconListEntry iconListEntry = null;

        if ( useBuiltin.isSelected() ) {
            iconListEntry = ( IconListEntry ) builtinIcons.getSelectedItem();
            _iconName = iconListEntry.name;
        }
        else {
            _iconName = iconPath;
        }

        _name = name.getText();
        _pattern = pattern.getText();
        _sample = sample.getText();


        Pattern re = null;

        // test if the regular expression is valid
        try {
            re = Pattern.compile( _pattern, ignoreCase.isSelected() ? Pattern.CASE_INSENSITIVE : 0 );
        }
        catch ( PatternSyntaxException rex ) {
            Object[] args = new Object[] {rex.getMessage(), };
            GUIUtilities.error( JOptionPane.getFrameForComponent( this ),
                    "task.regex-error", args );
            return ;
        }

        // Test if the regular expression matches the sample text
        Matcher match = re.matcher( _sample );
        if ( !match.matches() ) {
            GUIUtilities.error( JOptionPane.getFrameForComponent( this ),
                    "task.sample-doesnt-match", null );
            return ;
        }
        if ( match.groupCount() != 2) {
            GUIUtilities.error( JOptionPane.getFrameForComponent( this ),
                    "task.regex-error2", null );
            return ;
        }
        
        _name = match.group(1);
        name.setText(_name);

        // make sure fields are all field out
        if ( _name.length() == 0
                || _pattern.length() == 0
                || _sample.length() == 0 ) {
            GUIUtilities.error( JOptionPane.getFrameForComponent( this ),
                    "task.not-filled-out", null );
            return ;
        }


        taskType.setName( _name );
        taskType.setPattern( _pattern );
        taskType.setSample( _sample );
        taskType.setIgnoreCase( ignoreCase.isSelected() );
        taskType.setIconPath( _iconName );

        isOK = true;
        dispose();
    } //}}}

    //{{{ cancel() method
    public void cancel() {
        dispose();
    } //}}}

    //{{{ isOK() method
    public boolean isOK() {
        return isOK;
    } //}}}

    //{{{ private members
    private String iconPath; // for custom icon
    private TaskType taskType;
    private JTextField name;
    private JTextField pattern;
    private JTextField sample;
    private JCheckBox ignoreCase;
    private JButton okBtn, cancelBtn;
    private JButton customIcon;
    private JRadioButton useBuiltin, useCustom;
    private JComboBox builtinIcons;
    private boolean isOK;
    //}}}

    //{{{ updateEnabled() method
    private void updateEnabled() {
        builtinIcons.setEnabled( useBuiltin.isSelected() );
        customIcon.setEnabled( useCustom.isSelected() );
    } //}}}

    //{{{ ActionHandler class
    class ActionHandler implements ActionListener {
        public void actionPerformed( ActionEvent evt ) {
            Object source = evt.getSource();

            if ( source instanceof JRadioButton ) {
                updateEnabled();
            }
            else if ( source == okBtn ) {
                ok();
            }
            else if ( source == cancelBtn ) {
                cancel();
            }
            else if ( source == useBuiltin || source == useCustom ) {
                updateEnabled();
            }
            else if ( source == customIcon ) {
                String directory;
                if ( iconPath == null || iconPath.equals( "" ) ) {
                    directory = null;
                }
                else {
                    directory = MiscUtilities.getParentOfPath( iconPath );
                }
                String paths[] = GUIUtilities.showVFSFileDialog( null, directory,
                        VFSBrowser.OPEN_DIALOG, false );
                if ( paths == null ) {
                    return ;
                }

                iconPath = "file:" + paths[ 0 ];
                //Log.log(Log.DEBUG, TaskTypeDialog.class,
                // "custom icon path: " + iconPath);//##

                try {
                    customIcon.setIcon( new ImageIcon( new URL( iconPath ) ) );
                }
                catch ( MalformedURLException mf ) {
                    Log.log( Log.ERROR, this, mf );
                }
                customIcon.setText( MiscUtilities.getFileName( iconPath ) );
            }
        }
    } //}}}

} //}}}

//{{{ IconCellRenderer class
class IconCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent( JList list,
            Object value, int index, boolean isSelected,
            boolean cellHasFocus ) {
        super.getListCellRendererComponent( list, value, index,
                isSelected, cellHasFocus );

        IconListEntry icon = ( IconListEntry ) value;
        setText( icon.name );
        setIcon( icon.icon );

        return this;
    }
} //}}}

//{{{ IconListEntry class
class IconListEntry {
    IconListEntry( Icon icon, String name ) {
        this.icon = ( ImageIcon ) icon;
        this.name = name;
    }

    Icon icon;
    String name;
} //}}}
