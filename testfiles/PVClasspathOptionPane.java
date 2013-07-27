package sidekick.java.options;

// imports
import common.gui.FileTextField;
import common.gui.pathbuilder.PathBuilder;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.browser.VFSBrowser;

import projectviewer.vpt.VPTProject;

import sidekick.java.PVHelper;
import sidekick.java.util.Locator;


/**
 * Option pane for setting the classpath via ProjectViewer.  Borrowed a lot of
 * this code from the Java Core plugin by Robert Fletcher.
 */
public class PVClasspathOptionPane extends AbstractOptionPane {
    // instance fields
    private PathBuilder classpathBuilder;
    private PathBuilder sourcepathBuilder;
    private JCheckBox useJavaClassPath;
    private FileTextField buildPath;
    private View view;
    private VPTProject proj;
    public static String PREFIX = "sidekick.java.pv.";


    public PVClasspathOptionPane(VPTProject proj) {
        super( "javasidekick.pv.options" );
        this.proj = proj;
    }

    /** Initialises the option pane. */
    protected void _init() {
        setBorder( BorderFactory.createEmptyBorder( 6, 6, 6, 6 ) );

        view = jEdit.getActiveView();

        String name = PVHelper.getProjectName( view );

		addComponent(new JLabel(jEdit.getProperty(PREFIX + "desc")));
        
        // Include java.class.path in classpath
        // If prop is null or not equal to "true", set it to false
        String prop = proj.getProperty( "java.useJavaClasspath" );
        if (prop == null) prop = jEdit.getProperty( PREFIX + "useJavaClasspath" );
        useJavaClassPath = new JCheckBox(
                    jEdit.getProperty( PREFIX + "useJavaClasspath.label" ),
                    prop.equals("true")
                );
        addComponent( useJavaClassPath );

        // Classpath components
        classpathBuilder = new PathBuilder(
                    jEdit.getProperty( PREFIX + "optionalClasspath.label" )
                );
		classpathBuilder.setMultiSelectionEnabled(true);
        classpathBuilder.setFileFilter( new ClasspathFilter() );
        prop = proj.getProperty( "java.optionalClasspath" );
        classpathBuilder.setPath(
            ((prop == null) ? "" : prop)
        );
        classpathBuilder.setStartDirectory( proj.getRootPath()+File.separator );
        classpathBuilder.setEnabled( true );
		classpathBuilder.setPreferredSize( new Dimension(400, 200) );
        //classpathBuilder.setDotClassPathType(PathBuilder.LIB);
        addComponent( classpathBuilder );
        addComponent( Box.createVerticalStrut(11));

        // Sourcepath components
        sourcepathBuilder = new PathBuilder(
                    jEdit.getProperty( PREFIX + "optionalSourcepath.label" )
                );
		sourcepathBuilder.setMultiSelectionEnabled(true);
        sourcepathBuilder.setFileFilter( new SourceFileFilter() );
        prop = proj.getProperty( "java.optionalSourcepath" );
        sourcepathBuilder.setPath(
            ((prop == null) ? "" : prop)
        );
        sourcepathBuilder.setStartDirectory( proj.getRootPath()+File.separator );
        sourcepathBuilder.setEnabled( true );
		sourcepathBuilder.setPreferredSize( new Dimension(400, 200) );
        //sourcepathBuilder.setDotClassPathType(PathBuilder.SRC);
        addComponent( sourcepathBuilder );
        addComponent( Box.createVerticalStrut(11));

        // build path components
		/*
        JLabel buildPathLabel = new JLabel( jEdit.getProperty( PREFIX + "buildOutputPath.label" ) );
        buildPath = new JTextField( 30 );
        prop = proj.getProperty( "java.optionalBuildpath" );
        buildPath.setText( ((prop == null) ? "" : prop) );
        JButton browse_btn = new JButton( "Browse" );
        browse_btn.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent ae ) {
                        View view = GUIUtilities.getView( PVClasspathOptionPane.this );
                        String[] dirs = GUIUtilities.showVFSFileDialog( view, PVHelper.getProjectRoot( view ), VFSBrowser.CHOOSE_DIRECTORY_DIALOG, false );
                        if ( dirs != null && dirs.length > 0 ) {
                            buildPath.setText( dirs[ 0 ] );
                        }
                    }
                }
                                    );
        JPanel buildPathPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        buildPathPanel.add( buildPathLabel );
        buildPathPanel.add( buildPath );
        buildPathPanel.add( browse_btn );
		*/
		buildPath = new FileTextField();
		buildPath.getTextField().setColumns(30);
		prop = proj.getProperty("java.optionalBuildpath");
		buildPath.getTextField().setText( ((prop == null) ? "" : prop) );
		buildPath.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        addComponent( jEdit.getProperty(PREFIX + "buildOutputPath.label"), buildPath );
    }

    // #_save() : void
    /** Saves properties from the option pane. */
    protected void _save() {
        String name = PVHelper.getProjectName( view );
        /*
        jEdit.setBooleanProperty(
            PREFIX + name + ".useJavaClasspath",
            useJavaClassPath.isSelected()
        );
		*/
        proj.setProperty(
            "java.useJavaClasspath",
            (useJavaClassPath.isSelected()) ? "true" : "false"
        );
		/*
        jEdit.setProperty(
            PREFIX + name + ".optionalClasspath",
            classpathBuilder.getPath()
        );
        */
        proj.setProperty(
            "java.optionalClasspath",
            classpathBuilder.getPath()
        );
        /*
        jEdit.setProperty(
            PREFIX + name + ".optionalSourcepath",
            sourcepathBuilder.getPath()
        );
        */
        proj.setProperty(
            "java.optionalSourcepath",
            sourcepathBuilder.getPath()
        );
        /*
        jEdit.setProperty(
            PREFIX + name + ".optionalBuildpath",
            buildPath.getText()
        );
        */
        proj.setProperty(
            "java.optionalBuildpath",
            buildPath.getTextField().getText()
        );

        Locator.getInstance().refreshProject( proj );
    }

	/*
    public void addComponent( JComponent comp ) {
        GridBagConstraints cons = new GridBagConstraints();
        cons.gridy = y++; // y is a protected member of AbstractOptionPane
        cons.gridheight = 1;
        cons.gridwidth = GridBagConstraints.REMAINDER;
        cons.fill = GridBagConstraints.BOTH;
        cons.anchor = GridBagConstraints.WEST;
        cons.weightx = 1.0f;
        cons.weighty = 1.0f;
        cons.insets = new Insets( 1, 0, 1, 0 );
        gridBag.setConstraints( comp, cons );
        add( comp );
    }
	*/

    private static class ClasspathFilter extends FileFilter {
        public boolean accept( File file ) {
            if ( file.isDirectory() ) {
                return true;
            }

            String filename = file.getName();
            if ( ".classpath".equals( filename ) ) {
                return true;
            }
            int idx = filename.lastIndexOf( '.' );
            if ( idx >= 0 ) {
                String ext = filename.substring( idx );
                if ( ext.equalsIgnoreCase( ".jar" ) || ext.equalsIgnoreCase( ".zip" ) ) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return "Classpath elements (directories, *.jar, *.zip, .classpath)";
        }
    }
    // -class _SourceFileFilter_
    private static class SourceFileFilter extends FileFilter {
        // +accept(File) : boolean
        public boolean accept( File file ) {
            if ( file.isDirectory() ) {
                return true;
            }

            String filename = file.getName();
            if ( ".classpath".equals( filename ) ) {
                return true;
            }
            int idx = filename.lastIndexOf( '.' );
            if ( idx >= 0 ) {
                String ext = filename.substring( idx );
                if ( ext.equalsIgnoreCase( ".zip" ) ) {
                    return true;
                }
            }
            return false;
        }

        // +getDescription() : String
        public String getDescription() {
            return "Sourcepath elements (directories, *.zip, .classpath)";
        }
    }
}
