/*
* $Source$
* Copyright (C) 2003 Robert Fletcher
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
*/
package sidekick.java.options;

// imports
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.browser.*;
import sidekick.java.util.CopyUtils;

/**
 * danson: borrowed directly from JavaCore by Robert Fletcher.
 *
 * This is an updated version of the <code>common.gui.PathBuilder</code>
 * component from the Common Controls plugin. It has a number of advantages over
 * the Common Controls version.
 * <ul>
 * <li>Doesn't hard code the caption above the table.
 * <li>Fixes some of the geometry problems that the Common Controls version has
 * and can more easily be used in an option pane.
 * <li>Can be enabled/disabled with the state of all contained components being
 * updated accordingly.
 * <li>The look and feel is more in line with current jEdit core components.
 * </ul><p>
 * In order to use with a jEdit option pane it is recommended that your option
 * pane class should override the <code>addComponent</code> method with
 * something like:
 * <pre>public void addComponent(PathBuilder comp)
 * {
 *     GridBagConstraints cons = new GridBagConstraints();
 *     cons.gridy = y++; // y is a protected member of AbstractOptionPane
 *     cons.gridheight = 1;
 *     cons.gridwidth = GridBagConstraints.REMAINDER;
 *     cons.fill = GridBagConstraints.BOTH;
 *     cons.anchor = GridBagConstraints.WEST;
 *     cons.weightx = 1.0f;
 *     cons.weighty = 1.0f; // the vital difference from the super method
 *     cons.insets = new Insets(1,0,1,0);
 *     gridBag.setConstraints(comp, cons);
 *     add(comp);
 * }</pre>
 * Just using the default <code>AbstractOptionPane.addComponent()</code> method
 * can result in odd things happening when the option pane is resized.
 *
 * @author    <a href="mailto:rfletch6@yahoo.co.uk">Robert Fletcher</a>
 * @version   $Revision: 18090 $ $Date: 2010-06-15 22:31:31 -0400 (Tue, 15 Jun 2010) $
 */
public class PathBuilder extends JPanel {
    
    /**
     * Constant to identify "lib" type in .classpath files.    
     */
    public static final String LIB = "lib";
    
    /**
     * Constant to identify "src" type in .classpath files.    
     */
    public static final String SRC = "src";
    
    // instance fields
    private JLabel caption;
    private DefaultListModel listModel;
    private JList list;
    private JButton add;
    private JButton remove;
    private JButton moveUp;
    private JButton moveDown;

    private String startDirectory;
    private int fileSelectionMode;
    private boolean multiSelectionEnabled;
    private FileFilter fileFilter;
    private String fileDialogTitle;

    private boolean enabled;
    
    private String dotClassPathType = null;


    // +PathBuilder() : <init>
    /**
     * Constructs a <code>PathBuilder</code> object. Equivalent to calling
     * <code>new PathBuilder(null, true)</code>.
     */
    public PathBuilder() {
        this( null, true );
    }

    // +PathBuilder(String) : <init>
    /**
     * Constructs a <code>PathBuilder</code> object. Equivalent to calling
     * <code>new PathBuilder(captionText, true)</code>.
     *
     * @param captionText  text to appear above the path list, may be <code>null</code>
     */
    public PathBuilder( String captionText ) {
        this( captionText, true );
    }

    // +PathBuilder(String, boolean) : <init>
    /**
     * Constructs a <code>PathBuilder</code> object.
     *
     * @param captionText  text to appear above the path list, may be <code>null</code>
     * @param moveButtons  if <code>true</code> buttons allowing list elements
     *                     to be moved up &amp; down will be included
     */
    public PathBuilder( String captionText, boolean moveButtons ) {
        super( new BorderLayout() );

        // defaults
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES;
        multiSelectionEnabled = true;
        VFSFileChooserDialog dialog;
        fileFilter = null;
        fileDialogTitle = jEdit.getProperty( "vfs.browser.title" );

        if ( captionText != null ) {
            caption = new JLabel( captionText );
            add( BorderLayout.NORTH, caption );
        }

        listModel = new DefaultListModel();
        list = new JList( listModel );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        list.addListSelectionListener(
            new ListSelectionListener() {
                // +valueChanged(ListSelectionEvent) : void
                public void valueChanged( ListSelectionEvent event ) {
                    updateButtons();
                }
            }
        );
        add( BorderLayout.CENTER, new JScrollPane( list ) );

        JPanel buttons = new JPanel();
        buttons.setBorder( new EmptyBorder( 3, 0, 0, 0 ) );
        buttons.setLayout( new BoxLayout( buttons, BoxLayout.X_AXIS ) );

        add = new RolloverButton( GUIUtilities.loadIcon( "Plus.png" ) );
        add.setToolTipText( jEdit.getProperty( "common.add" ) );
        add.addActionListener(
            new ActionListener() {
                // +actionPerformed(ActionEvent) : void
                public void actionPerformed( ActionEvent event ) {
                    JFileChooser chooser;
                    if ( startDirectory == null ) {
                        chooser = new JFileChooser();
                    }
                    else {
                        chooser = new JFileChooser( startDirectory );
                    }

                    chooser.setFileSelectionMode( fileSelectionMode );
                    chooser.setMultiSelectionEnabled( multiSelectionEnabled );
                    if ( fileFilter != null ) {
                        chooser.addChoosableFileFilter( fileFilter );
                    }

                    chooser.setDialogTitle( fileDialogTitle );
                    chooser.setFileHidingEnabled( false );
                    chooser.setApproveButtonText( jEdit.getProperty( "common.ok" ) );
                    int returnVal = chooser.showDialog(
                                PathBuilder.this.getRootPane(),
                                null
                            );
                    
                    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
                        try {
                            if ( multiSelectionEnabled ) {
                                File[] files = chooser.getSelectedFiles();
                                for ( File file : files ) {
                                    if ( dotClassPathType != null && ".classpath".equals( file.getName() ) ) {
                                        // this is a special case
                                        String path = getDotClassPathPaths( file );
                                        setPath( path );
                                    }
                                    else {
                                        listModel.addElement( file.getCanonicalPath() );
                                    }
                                }
                            }
                            else {
                                listModel.addElement(
                                    chooser.getSelectedFile().getCanonicalPath()
                                );
                            }
                        }
                        catch ( IOException e ) {
                            Log.log( Log.WARNING, this.getClass(), e );
                        }
                    }
                }
            }
        );
        buttons.add( add );
        buttons.add( Box.createHorizontalStrut( 6 ) );

        remove = new RolloverButton( GUIUtilities.loadIcon( "Minus.png" ) );
        remove.setToolTipText( jEdit.getProperty( "common.remove" ) );
        remove.addActionListener(
            new ActionListener() {
                // +actionPerformed(ActionEvent) : void
                public void actionPerformed( ActionEvent event ) {
                    int[] rows = list.getSelectedIndices();
                    for ( int i = rows.length - 1; i >= 0; i-- ) {
                        listModel.remove( rows[ i ] );
                    }
                }
            }
        );
        buttons.add( remove );

        if ( moveButtons ) {
            buttons.add( Box.createHorizontalStrut( 6 ) );

            moveUp = new RolloverButton( GUIUtilities.loadIcon( "ArrowU.png" ) );
            moveUp.setToolTipText( jEdit.getProperty( "common.moveUp" ) );
            moveUp.addActionListener(
                new ActionListener() {
                    // +actionPerformed(ActionEvent) : void
                    public void actionPerformed( ActionEvent event ) {
                        int index = list.getSelectedIndex();
                        Object selected = list.getSelectedValue();
                        listModel.removeElementAt( index );
                        listModel.insertElementAt( selected, index - 1 );
                        list.setSelectedIndex( index - 1 );
                        list.ensureIndexIsVisible( index - 1 );
                    }
                }
            );
            buttons.add( moveUp );
            buttons.add( Box.createHorizontalStrut( 6 ) );

            moveDown = new RolloverButton( GUIUtilities.loadIcon( "ArrowD.png" ) );
            moveDown.setToolTipText( jEdit.getProperty( "common.moveDown" ) );
            moveDown.addActionListener(
                new ActionListener() {
                    // +actionPerformed(ActionEvent) : void
                    public void actionPerformed( ActionEvent event ) {
                        int index = list.getSelectedIndex();
                        Object selected = list.getSelectedValue();
                        listModel.removeElementAt( index );
                        listModel.insertElementAt( selected, index + 1 );
                        list.setSelectedIndex( index + 1 );
                        list.ensureIndexIsVisible( index + 1 );
                    }
                }
            );
            buttons.add( moveDown );
        }

        buttons.add( Box.createGlue() );

        updateButtons();
        add( BorderLayout.SOUTH, buttons );
    }
    
    /**
     * Abstracts the "lib" or "src" lines from a .classpath file.    
     * @param dotClassPathFile The .classpath file.    
     */
    private String getDotClassPathPaths( File dotClassPathFile ) {
        if (dotClassPathType == null) {
            return "";   
        }
        
        // the .classpath file is an xml file, but it's pretty simple, so I'm parsing
        // it by hand rather than invoking an xml parser. I'm looking for lines like
        // <classpathentry kind="lib" path="lib/ant-contrib-1.0b3.jar"/>, so the line
        // must contain "classpathentry" and "kind="lib"".  If it does, then grab
        // the "path" value. Paths are assumed to be relative to the directory 
        // containing the .classpath file. The existence of each path is checked,
        // non-existent paths will not be included.
        try {
            // paths are assumed to be relative to this directory
            File dotClassPathDir = dotClassPathFile.getParentFile();
            
            // load the .classpath file into a string array of lines
            BufferedReader reader = new BufferedReader( new FileReader( dotClassPathFile ) );
            StringWriter writer = new StringWriter();
            CopyUtils.copy( reader, writer );
            String[] lines = writer.toString().split( "\\n" );

            // parse the string into paths
            StringBuffer sb = new StringBuffer();
            String pathRegex = "path=[\"](.*?)[\"]";
            Pattern pattern = Pattern.compile( pathRegex );
            for ( String line : lines ) {
                if ( line.indexOf( "classpathentry" ) < 0 || line.indexOf( "kind=\"" + dotClassPathType + "\"" ) < 0 ) {
                    continue;
                }
                Matcher matcher = pattern.matcher( line );
                if ( matcher.find() ) {
                    String path = matcher.group( 1 );
                    File file = new File( dotClassPathDir, path );
                    if (file.exists()) {
                        sb.append( file.getAbsolutePath() ).append( File.pathSeparator );
                    }
                }
            }
            return sb.toString();
        }
        catch ( Exception e ) {
            return "";
        }
    }

    // +getPath() : String
    public String getPath() {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < listModel.getSize(); i++ ) {
            if ( i != 0 ) {
                buf.append( File.pathSeparator );
            }
            buf.append( ( String ) listModel.get( i ) );
        }
        return buf.toString();
    }

    // +getPathArray() : String[]
    public String[] getPathArray() {
        String[] pathArray = new String[ listModel.getSize() ];
        for ( int i = 0; i < listModel.getSize(); i++ ) {
            pathArray[ i ] = ( String ) listModel.get( i );
        }
        return pathArray;
    }

    // +setEnabled(boolean) : void
    public void setEnabled( boolean enabled ) {
        this.enabled = enabled;
        super.setEnabled( enabled );
        if ( caption != null ) {
            caption.setEnabled( enabled );
        }
        list.clearSelection();
        list.setEnabled( enabled );
        updateButtons();
    }

    public void setFileSelectionMode( int fileSelectionMode ) {
        this.fileSelectionMode = fileSelectionMode;
    }

    public void setFileDialogTitle( String fileDialogTitle ) {
        this.fileDialogTitle = fileDialogTitle;
    }

    public void setFileFilter( FileFilter fileFilter ) {
        this.fileFilter = fileFilter;
    }

    public void setMultiSelectionEnabled( boolean multiSelectionEnabled ) {
        this.multiSelectionEnabled = multiSelectionEnabled;
    }

    /**
     * "setPath" is a bad name, this actually adds the given path.
     * @param path A single path or multiple paths separated by the system path separator.
     */
    public void setPath( String path ) {
        StringTokenizer strtok = new StringTokenizer( path, File.pathSeparator );
        while ( strtok.hasMoreTokens() ) {
            listModel.addElement( strtok.nextToken() );
        }
    }

    /**
     * Removes the given path from the list.
     * @param path A single path or multiple paths separated by the system path separator.
     */
    public void removePath( String path ) {
        StringTokenizer strtok = new StringTokenizer( path, File.pathSeparator );
        while ( strtok.hasMoreTokens() ) {
            listModel.removeElement( strtok.nextToken() );
        }
    }

    public void setPathArray( String[] pathArray ) {
        for ( int i = 0; i < pathArray.length; i++ ) {
            listModel.addElement( pathArray[ i ] );
        }
    }

    public void setSelectionMode( int selectionMode ) {
        list.setSelectionMode( selectionMode );
    }

    public void setStartDirectory( String startDirectory ) {
        this.startDirectory = startDirectory;
    }
    
    /**
     * If set, attempts to find the "lib" or "src" lines in a .classpath file.    
     * @param type One of "lib" or "src".    
     */
    public void setDotClassPathType(String type) {
        if (LIB.equals(type) || SRC.equals(type)) {
            dotClassPathType = type;        
        }
    }

    private void updateButtons() {
        int index = list.getSelectedIndex();
        add.setEnabled( enabled );
        remove.setEnabled( enabled && index >= 0 && listModel.getSize() > 0 );
        if ( moveUp != null ) {
            moveUp.setEnabled( enabled && index > 0 );
            moveDown.setEnabled(
                enabled && index >= 0 && index < listModel.getSize() - 1
            );
        }
    }
}