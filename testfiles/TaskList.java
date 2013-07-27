/*
* TaskList.java - TaskList plugin
* Copyright (C) 2001,2002 Oliver Rutherfurd
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
* $Id: TaskList.java 20709 2012-01-03 16:41:24Z daleanson $
*/

package tasklist;

//{{{ imports
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.msg.*;
import common.gui.CloseableTabbedPane;
//}}}

/**
 * A dockable component contaning a scrollable tree; the tree contains
 * data on task items found by parsing one or more buffers.
 *
 * @author Oliver Rutherfurd
 */
public class TaskList extends JPanel implements EBComponent {

    private View view = null;
    private CloseableTabbedPane tabs = null;
    private boolean showOpenFiles = true;
    private boolean showProjectFiles = true;
    private Set<TaskType> taskTypes = null;
    private JMenu filterMenu = null;

    //{{{ constructor
    /**
     * Constructor
     *
     * @param view The view in which the TaskList component will appear
     */
    public TaskList( View view ) {
        this.view = view;
        init();
        EditBus.addToBus( this );
        TaskListPlugin.registerTaskList( this );
        send( new ParseBufferMessage( view, null, ParseBufferMessage.DO_PARSE_ALL ) );
    } //}}}

    private void init() {
        // this method can be called on property change, which can change the
        // layout of this panel, so remove all child components re-install them.
        removeAll();

        setLayout( new BorderLayout() );

        showOpenFiles = jEdit.getBooleanProperty( "tasklist.show-open-files", true );
        showProjectFiles = jEdit.getBooleanProperty( "tasklist.show-project-files", true );
        boolean projectViewerAvailable = PVHelper.isProjectViewerAvailable();

        tabs = new CloseableTabbedPane();
        Icon close_icon = GUIUtilities.loadIcon( "closebox.gif" );
        Icon hover_icon = GUIUtilities.loadIcon( "closebox.gif" );
        Icon pressed_icon = GUIUtilities.loadIcon( "closebox.gif" );
        tabs.setCloseIcons( close_icon, hover_icon, pressed_icon );

        // add a mouse listener to be able to close tabs
        tabs.addMouseListener( tabMouseListener );

        addTab( jEdit.getProperty( "tasklist.current-file", "Current File" ), new CurrentBufferTaskList( view ) );
        if ( showOpenFiles ) {
            addTab( jEdit.getProperty( "tasklist.open-files", "Open Files" ), new OpenBuffersTaskList( view ) );
        }
        if ( showProjectFiles && projectViewerAvailable ) {
            addTab( jEdit.getProperty( "tasklist.project-files", "Project Files" ), new ProjectTaskList( view ) );
        }
        add( BorderLayout.CENTER, tabs );
        tabs.setSelectedIndex( 0 );

        // bottom panel holds refresh button and task filter
        JPanel bottomPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        JToolBar buttonPanel = new JToolBar();
        bottomPanel.add( buttonPanel );

        // create the refresh button
        buttonPanel.setFloatable( false );
        JButton refreshButton = new JButton( GUIUtilities.loadIcon( "22x22/actions/view-refresh.png" ) );
        refreshButton.setToolTipText( jEdit.getProperty( "tasklist.toolbar.refresh", "Refresh" ) );
        refreshButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    TaskListPlugin.send( new ParseBufferMessage( view, null, ParseBufferMessage.DO_PARSE_ALL ) );
                }
            }
        );
        buttonPanel.add( refreshButton );

        // create the task filter
        filterMenu = new JMenu( jEdit.getProperty( "tasklist.toolbar.filter", "Filter" ) );
        taskTypes = new HashSet<TaskType>();
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
            TaskType taskType = new TaskType( name, pattern, sample, ignoreCase, iconPath );
            taskTypes.add( taskType );
            JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem( name, taskType.getIcon(), true );
            menuItem.setActionCommand( name );
            menuItem.addActionListener( filterActionListener );
            filterMenu.add( menuItem );
            i++;
        }
        JMenuItem toggleAll = new JMenuItem( jEdit.getProperty( "tasklist.toggleAll", "Toggle All" ) );
        toggleAll.addActionListener( toggleAllActionListener );
        filterMenu.add( toggleAll );

        JMenuBar menuBar = new JMenuBar();
        menuBar.add( filterMenu );
        bottomPanel.add( menuBar );

        add( BorderLayout.SOUTH, bottomPanel );
    }

    ActionListener toggleAllActionListener = new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    for ( int i = 0; i < filterMenu.getItemCount(); i++ ) {
                        Component c = filterMenu.getMenuComponent( i );
                        if ( c instanceof JCheckBoxMenuItem ) {
                            JCheckBoxMenuItem mi = ( JCheckBoxMenuItem ) c;
                            mi.setSelected( !mi.isSelected() );
                            String command = mi.getActionCommand();
                            for ( TaskType type : taskTypes ) {
                                if ( command.equals( type.getName() ) ) {
                                    type.setActive( mi.isSelected() );
                                }
                            }
                        }
                    }
                    TaskListPlugin.send( new ParseBufferMessage( view, null, ParseBufferMessage.APPLY_FILTER ) );
                }
            };

    ActionListener filterActionListener = new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    JCheckBoxMenuItem mi = ( JCheckBoxMenuItem ) ae.getSource();
                    if ( mi == null ) {
                        return ;
                    }
                    String command = mi.getActionCommand();
                    for ( TaskType type : taskTypes ) {
                        if ( command.equals( type.getName() ) ) {
                            type.setActive( mi.isSelected() );
                        }
                    }
                    TaskListPlugin.send( new ParseBufferMessage( view, null, ParseBufferMessage.APPLY_FILTER ) );
                }
            };

    public void addTab( String name, AbstractTreeTaskList tasklist ) {
        tabs.add( name, tasklist );
        tabs.setSelectedIndex( tabs.getTabCount() - 1 );
    }

    //{{{ getName() method
    /**
     * Property accessor required by jEdit Plugin API
     * @return The plugin's name property
     */
    public String getName() {
        return "tasklist";
    } //}}}

    //{{{ getComponent() method
    /**
     * Property accessor required by jEdit Plugin API
     * @return A reference to the TaskList object
     */
    public Component getComponent() {
        return this;
    } //}}}

    public View getView() {
        return view;
    }

    public Set<TaskType> getTaskTypes() {
        return taskTypes;
    }

    public Set<TaskType> getActiveTaskTypes() {
        Set<TaskType> activeTypes = new HashSet<TaskType>();
        for ( TaskType type : taskTypes ) {
            if ( type.isActive() ) {
                activeTypes.add( type );
            }
        }
        return activeTypes;
    }

    // pass messages on to task trees
    public void send( ParseBufferMessage msg ) {
        for ( int i = 0; i < tabs.getTabCount(); i++ ) {
            AbstractTreeTaskList treeList = ( AbstractTreeTaskList ) tabs.getComponentAt( i );
            treeList.handleMessage( msg );
        }
    }

    // add or remove tabs based on property settings
    public void handleMessage( EBMessage msg ) {
        if ( msg instanceof PropertiesChanged ) {
            boolean sof = jEdit.getBooleanProperty( "tasklist.show-open-files", true );
            boolean spf = jEdit.getBooleanProperty( "tasklist.show-project-files", true );
            if ( sof != showOpenFiles || spf != showProjectFiles ) {
                init();
            }
        }
        else if ( msg instanceof ParseBufferMessage ) {
            Object what = ((ParseBufferMessage)msg).getWhat();
            if (ParseBufferMessage.REINIT.equals(what)) {
                init();   
            }
        }
    }

    MouseListener tabMouseListener =
        new MouseAdapter() {
            public void mousePressed( MouseEvent me ) {
                if ( me.isPopupTrigger() ) {
                    handleIsPopup( me );
                }
            }

            public void mouseReleased( MouseEvent me ) {
                if ( me.isPopupTrigger() ) {
                    handleIsPopup( me );
                }
            }

            private void handleIsPopup( MouseEvent me ) {
                final int x = me.getX();
                final int y = me.getY();
                int index = tabs.indexAtLocation( x, y );
                if ( index < 1 || ( index < 2 && ( showOpenFiles || showProjectFiles ) ) || ( index < 3 && showOpenFiles && showProjectFiles ) ) {
                    // index 0 is the current buffer, don't close it ever,
                    // less than 0 is an invalid tab
                    return ;
                }
                final Component c = tabs.getComponentAt( index );
                final JPopupMenu pm = new JPopupMenu();
                JMenuItem close_mi = new JMenuItem( jEdit.getProperty( "tasklist.close", "Close" ) );
                pm.add( close_mi );
                close_mi.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            tabs.remove( c );
                        }
                    }
                );
                JMenuItem close_all_mi = new JMenuItem( jEdit.getProperty( "tasklist.close-all", "Close All" ) );
                pm.add( close_all_mi );
                close_all_mi.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent ae ) {
                            int start_index = 1;
                            if ( showOpenFiles ) {
                                ++start_index;
                            }
                            if ( showProjectFiles ) {
                                ++start_index;
                            }
                            for ( int i = start_index; i < tabs.getTabCount(); ) {
                                Component comp = tabs.getComponentAt( i );
                                tabs.remove( comp );
                                comp = null;
                            }
                        }
                    }
                );
                GUIUtilities.showPopupMenu( pm, tabs, x, y );
            }
        };
}