/*
*  Copyright (C) 2003 Don Brown (mrdon@techie.com)
*  Copyright (C) 2000, 2001 Greg Merrill (greghmerrill@yahoo.com)
*  This file is part of Log Viewer, a plugin for jEdit (http://www.jedit.org).
*  It is heavily  based off Follow (http://follow.sf.net).
*  Log Viewer is free software; you can redistribute it and/or modify
*  it under the terms of version 2 of the GNU General Public
*  License as published by the Free Software Foundation.
*  Log Viewer is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*  You should have received a copy of the GNU General Public License
*  along with Log Viewer; if not, write to the Free Software
*  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package logviewer;
import java.awt.Insets;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.regex.*;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import ghm.follow.*;

/**
 * A component which allows one to view a text file to which information is
 * being asynchronously appended. <p>
 *
 * danson: modified to allow the view component to be either a JTextArea or a
 * JTable.
 *
 * @author    <a href="mailto:mrdon@techie.com">Don Brown</a>
 * @author    <a href="mailto:greghmerrill@yahoo.com">Greg Merrill</a>
 * @version   $Revision: 1156 $
 */
public class FileFollowingPane extends JScrollPane {

    /** FileFollower used to print to this component */
    protected FileFollower fileFollower_;

    /** Component into which the followed file's contents are printed */
    protected JComponent viewComponent_;

    /** OutputDestination used w/FileFollower */
    protected OutputDestinationComponent destination_;

    /**
     * Should the 'caret' be repositioned to the bottom of the component when
     * new text is added? This is somewhat of a misnomer since the component may
     * not be a text area and may not actually have a caret.
     */
    private boolean autoPositionCaret = true;

    // Control to lock the horizontal scrollbar
    private JCheckBox lock_box;
    private int locked_value = 0;

    /**
     * Constructor
     *
     * @param file               text file which is to be followed
     * @param bufferSize         size of the character buffer inside the
     *      FileFollower used to follow the supplied file
     * @param latency            latency of the FileFollower used to follow the
     *      supplied file
     * @param autoPositionCaret  Whether to autoposition the caret
     */
    public FileFollowingPane( File file, int bufferSize, int latency, boolean autoPositionCaret ) {
        // check if there is a defined type for this file
        List logTypes = LogViewerPlugin.getLogTypes();
        String filename = file.getName();
        LogType myType = null;
        for ( Iterator it = logTypes.iterator(); it.hasNext(); ) {
            LogType logType = ( LogType ) it.next();
            String fileNameGlob = logType.getFileNameGlob();
            String firstLineGlob = logType.getFirstLineGlob();
            //Log.log("fileNameGlob = " + fileNameGlob + ", filename = " + filename + ", matches? " + StringUtils.matches(filename, fileNameGlob));
            if ( fileNameGlob != null && StringUtils.matches( filename, fileNameGlob ) ) {
                // got a possible
                myType = logType;
                if ( firstLineGlob != null ) {
                    String first_line = getFirstLine( file );
                    if ( StringUtils.matches( first_line, firstLineGlob ) ) {
                        // got a winner
                        break;
                    }
                }
            }
            else if ( firstLineGlob != null ) {
                String first_line = getFirstLine( file );
                if ( StringUtils.matches( first_line, firstLineGlob ) ) {
                    // got a possible
                    myType = logType;
                }
            }
        }

        if ( myType == null ) {
            // no type defined for this file, so default to use the text area viewer.
            viewComponent_ = new JTextArea();
            ( ( JTextArea ) viewComponent_ ).setEditable( false );
            ( ( JTextArea ) viewComponent_ ).setWrapStyleWord( true );
            destination_ = new JTextAreaDestination( ( ( JTextArea ) viewComponent_ ), autoPositionCaret );
        }
        else {
            // found a defined type. If no columns are defined, use the text area viewer.
            if ( myType.getColumnCount() == 0 ) {
                viewComponent_ = new JTextArea();
                ( ( JTextArea ) viewComponent_ ).setEditable( false );
                ( ( JTextArea ) viewComponent_ ).setWrapStyleWord( true );
                destination_ = new JTextAreaDestination( ( ( JTextArea ) viewComponent_ ), autoPositionCaret, myType );
            }
            else {
                // found a defined type, so use a table viewer.
                viewComponent_ = new JTable();
                int initialSize = (int)Math.max(file.length() / 80, 500);
                destination_ = new JTableDestination( ( ( JTable ) viewComponent_ ), myType, initialSize );
            }

        }
        fileFollower_ = new FileFollower(
                    file,
                    bufferSize,
                    latency,
                    new OutputDestination[] {destination_}
                );
        fileFollower_.setLogType(myType);
        Log.log("myType = " + myType);

        setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
        setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
        /*
        String image_src = "images/lock.png";
        URL url = getClass().getClassLoader().getResource( image_src );
        ImageIcon icon = null;
        if ( url != null )
            icon = new ImageIcon( url );
        lock_box = new JCheckBox();
        if (icon != null)
            lock_box.setSelectedIcon(icon);
        */
        lock_box = new JCheckBox();
        lock_box.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
        lock_box.setSelected( false );
        lock_box.setToolTipText( "Lock the horizontal scroll bar." );
        setCorner( JScrollPane.LOWER_RIGHT_CORNER, lock_box );
        getHorizontalScrollBar().setModel(
            new javax.swing.DefaultBoundedRangeModel() {
                public int getValue() {
                    return lock_box.isSelected() ? locked_value : super.getValue();
                }

                public void setValue( int value ) {
                    if ( lock_box.isSelected() )
                        return ;
                    locked_value = value;
                    super.setValue( value );
                }
            }
        );

        this.getViewport().setView( viewComponent_ );
        this.getViewport().setScrollMode( JViewport.BLIT_SCROLL_MODE );   // fastest scroll mode

    }

    /**
     * <strong>Deprecated</strong> Used to return the text area to which the
     * followed file's contents are being printed. Since LogViewer has been
     * updated to possibly use a JTable, it would be inappropriate to return a
     * JTextArea. Use <code>getComponent</code> to retrieve the actual component
     * used to display the log.
     *
     * @return       Used to return the text area containing followed file's
     *      contents, now returns null as the component may not be a text area.
     * @deprecated   use getComponent
     */
    public JTextArea getTextArea() {
        return null;
    }

    /**
     * Gets the component attribute of the FileFollowingPane object
     *
     * @return   The component value
     */
    public JComponent getComponent() {
        return viewComponent_;
    }

    /**
     * Gets the outputDestination attribute of the FileFollowingPane object
     *
     * @return   The outputDestination value
     */
    public OutputDestinationComponent getOutputDestination() {
        return destination_;
    }

    /**
     * Sets whether the display component should word wrap the output. In the
     * case of a JTextArea, this is a simple word wrap for long lines, in the
     * case of a JTable, individual cells are wrapped.
     *
     * @param value  The new wordWrap value
     */
    public void setWordWrap( boolean value ) {
        destination_.setWordWrap( value );
    }

    /**
     * Gets the current word wrap setting.
     *
     * @return   The current word wrap setting.
     */
    public boolean getWordWrap() {
        return destination_.getWordWrap();
    }

    /** Toggle word wrap. */
    public void toggleWordWrap() {
        destination_.toggleWordWrap();
    }

    /**
     * Returns whether caret is automatically repositioned to the end of the
     * text area when text is appended to the followed file
     *
     * @return   whether caret is automatically repositioned on append
     */
    public boolean autoPositionCaret() {
        return destination_.autoPositionCaret();
    }

    /**
     * Sets whether caret is automatically repositioned to the end of the text
     * area when text is appended to the followed file
     *
     * @param value  whether caret is automatically repositioned on append
     */
    public void setAutoPositionCaret( boolean value ) {
        destination_.setAutoPositionCaret( value );
    }

    /** Toggles the auto position caret setting. */
    public void toggleAutoPositionCaret() {
        destination_.toggleAutoPositionCaret();
    }

    /**
     * Returns the FileFollower which is being used to print information in this
     * component.
     *
     * @return   FileFollower used by this component
     */
    public FileFollower getFileFollower() {
        return fileFollower_;
    }

    /**
     * Convenience method; equivalent to calling
     * getFileFollower().getFollowedFile()
     *
     * @return   The followedFile value
     */
    public File getFollowedFile() {
        return fileFollower_.getFollowedFile();
    }

    /** Convenience method; equivalent to calling getFileFollower().start() */
    public void startFollowing() {
        fileFollower_.start();
    }

    /** Convenience method; equivalent to calling getFileFollower().stop() */
    public void stopFollowing() {
        fileFollower_.stop();
    }

    /** Description of the Method */
    public void refresh() {
        fileFollower_.refresh();
    }

    /**
     * Convenience method; equivalent to calling getFileFollower().stopAndWait()
     *
     * @exception InterruptedException  If something goes wrong
     */
    public void stopFollowingAndWait()
    throws InterruptedException {
        fileFollower_.stopAndWait();
    }

    /**
     * Clears the contents of this FileFollowingPane synchronously.
     *
     * @exception IOException  If something goes wrong
     */
    public void clear()
    throws IOException {
        if ( fileFollower_.getFollowedFile().length() == 0L ) {
            return ;
        }
        synchronized ( fileFollower_ ) {
            try {
                fileFollower_.stopAndWait();
            }
            catch ( InterruptedException interruptedException ) {
                // Handle this better later
                interruptedException.printStackTrace( System.err );
            }

            // This has the effect of clearing the contents of the followed file
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(
                        fileFollower_.getFollowedFile()
                    ) );
            bos.close();

            fileFollower_.start();
        }
    }

    /**
     * @param file the file to read
     * @return      the first non-blank line of a file, that is, the first line
     *      of a file that contains something other than whitespace. Returns
     *      null on any error.
     */
    public String getFirstLine( File file ) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new FileReader( file ) );
            String line = reader.readLine();
            while ( line != null ) {
                if ( line.trim().length() > 0 )
                    return line;
                line = reader.readLine();
            }
            return null;
        }
        catch ( Exception e ) {
            return null;
        }
        finally {
            try {
                if ( reader != null )
                    reader.close();
            }
        catch ( Exception ignored ) {}
        }
    }

}

