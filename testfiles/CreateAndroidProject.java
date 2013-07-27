package android.actions;

import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.browser.VFSBrowser;

import ise.java.awt.KappaLayout;

import projectviewer.ProjectManager;
import projectviewer.ProjectViewer;
import projectviewer.config.ProjectOptions;
import projectviewer.importer.RootImporter;
import projectviewer.vpt.*;

// Create Android project
// Assumes "android" is in your path.
// Must have CommonControls plugin installed for KappaLayout.
// Must have ProjectViewer plugin installed.
public class CreateAndroidProject implements Command {

    private View view;

    public void execute( View view ) {
        this.view = view;
        Runner runner = new Runner();
        runner.execute();
    }

    class Runner extends SwingWorker<Vector, Object> {
        @Override
        public Vector doInBackground() {
            try {
                // load the available target names
                Process p = Runtime.getRuntime().exec( "android list targets" );
                BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
                Vector<String> targetList = new Vector<String>();                // NOPMD
                try {
                    while ( true ) {
                        String line = in.readLine();
                        if ( line == null ) {
                            break;
                        }
                        if ( line.startsWith( "id:" ) ) {
                            String id = line.substring(4, line.indexOf( ' ', 4 ) );
                            String name = line.substring( line.indexOf( ' ', 4 ) + " or ".length() );
                            name = name.replaceAll( "\"", "" );
                            targetList.add( id + " " + name );
                        }
                    }
                } catch ( Exception e ) {
                    e.printStackTrace();
                }
                return targetList;
            } catch ( Exception e ) {
                Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), e.getMessage() );
                return null;
            }
        }

        @Override
        public void done() {
            Vector targetList = null;
            try {
                targetList = get();
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            if ( targetList != null ) {
                // create the dialog
                String title = "Create Android Project";
                final JDialog dialog = new JDialog( view, title, false );
                JPanel content = new JPanel( new KappaLayout() );
                content.setBorder( BorderFactory.createEmptyBorder(12, 12, 12, 12 ) );
                dialog.setContentPane( content );

                // create the components
                final JTextField projectNameField = new JTextField();
                final JComboBox targetField = new JComboBox( targetList );
                final JTextField pathField = new JTextField();
                final JButton chooseButton = new JButton( "Browse" );
                final JTextField activityNameField = new JTextField();
                final JTextField packageNameField = new JTextField();

                // ok/cancel panel
                KappaLayout kl = new KappaLayout();
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout( kl );
                final JButton ok = new JButton( "OK" );
                final JButton cancel = new JButton( "Cancel" );
                buttonPanel.add( "0,1,,,,w, 3", ok );
                buttonPanel.add( "1,1,,,,w, 3", cancel );
                kl.makeColumnsSameWidth(0, 1 );
                dialog.getRootPane().setDefaultButton( ok );

                // add the components to the dialog
                content.add( "0, 0, 1, 1, W, w, 3", new JLabel( "Project name" ) );
                content.add( "1, 0, 4, 1, W, w, 3", projectNameField );

                content.add( "0, 1, 1, 1, W, w, 3", new JLabel( "Build target:" ) );
                content.add( "1, 1, 4, 1, W, w, 3", targetField );

                content.add( "0, 2, 1, 1, W, w, 3", new JLabel( "Project path" ) );
                content.add( "1, 2, 3, 1, W, w, 3", pathField );
                content.add( "4, 2, 1, 1, W, w, 3", chooseButton );

                content.add( "0, 3, 1, 1, W, w, 3", new JLabel( "Activity name" ) );
                content.add( "1, 3, 4, 1, W, w, 3", activityNameField );

                content.add( "0, 4, 1, 1, W, w, 3", new JLabel( "Package name" ) );
                content.add( "1, 4, 4, 1, W, w, 3", packageNameField );

                content.add( "0, 5", KappaLayout.createVerticalStrut(11 ) );

                content.add( "0, 6, 5, 1, E,, 3", buttonPanel );

                // button action listener
                ActionListener listener = new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        if ( e.getSource().equals( ok ) ) {
                            String selectedItem = ( String ) targetField.getSelectedItem();
                            String target = selectedItem.substring(0, selectedItem.indexOf( ' ' ) );
                            String projectName = projectNameField.getText();
                            if ( projectName != null && projectName.trim().length() > 0 ) {
                                if ( projectName.indexOf( ' ' ) > -1 ) {
                                    Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Project_name_may_not_have_spaces.", "Project name may not have spaces." ) );
                                    projectName = projectName.replaceAll( " ", "" );
                                    return;
                                }
                            }
                            String path = pathField.getText();
                            if ( path == null || path.trim().length() == 0 ) {
                                Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Please_enter_a_path_for_the_project_files.", "Please enter a path for the project files." ) );
                                return;
                            }
                            String activityName = activityNameField.getText();
                            if ( activityName == null || activityName.trim().length() == 0 ) {
                                Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Please_enter_a_name_for_the_default_activity_class.", "Please enter a name for the default activity class." ) );
                                return;
                            }
                            if ( activityName != null && activityName.trim().length() > 0 ) {
                                if ( activityName.indexOf( ' ' ) > -1 ) {
                                    Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Activity_name_may_not_have_spaces.", "Activity name may not have spaces." ) );
                                    activityName = activityName.replaceAll( " ", "" );
                                    return;
                                }
                            }
                            String packageName = packageNameField.getText();
                            if ( packageName == null || packageName.trim().length() == 0 ) {
                                Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Please_enter_a_package_name.", "Please enter a package name." ) );
                                return;
                            }
                            try {
                                boolean success = createAndroidProject( target, projectName, path, activityName, packageName );
                                if (success) {
                                    dialog.dispose();
                                }
                            } catch ( IOException ioe ) {
                                ioe.printStackTrace();
                                GUIUtilities.error( view, "android.listAVDs.error", new String[] {ioe.getMessage()} );
                                return;
                            }
                            createPVProject( projectName, path );
                        } else if ( e.getSource().equals( cancel ) ) {
                            dialog.dispose();
                        } else if ( e.getSource().equals( chooseButton ) ) {
                            String[] paths = GUIUtilities.showVFSFileDialog( view, System.getProperty( "user.home" ), VFSBrowser.CHOOSE_DIRECTORY_DIALOG, false );
                            if ( paths != null && paths.length == 1 ) {
                                pathField.setText( paths[0] );
                            }
                        }
                    }
                };

                // add listeners
                ok.addActionListener( listener );
                cancel.addActionListener( listener );
                chooseButton.addActionListener( listener );

                // show the dialog
                dialog.pack();
                dialog.setLocationRelativeTo( view );
                dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
                dialog.setVisible( true );
            }
        }
    }

    boolean createAndroidProject( String targetId, String projectName, String path, String activityName, String packageName ) throws IOException {
        if ( targetId == null ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Target_ID_cannot_be_null.", "Target ID cannot be null."));
            return false;
        }
        if ( projectName == null ) {
            projectName = activityName;
        }
        if ( path == null ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Path_cannot_be_null.", "Path cannot be null."));
            return false;
        }
        if ( activityName == null ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Activity_name_cannot_be_null.", "Activity name cannot be null."));
            return false;
        }
        if ( packageName == null ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Package_name_cannot_be_null.", "Package name cannot be null."));
            return false;
        }
        String command = "android create project --target " + targetId + " --name " + projectName + " --path " + path + " --activity " + activityName + " --package " + packageName;
        Util.runInSystemShell( view, command );
        return true;
    }

    void createPVProject( String projectName, String projectPath ) {
        if ( !Util.isProjectViewerAvailable() ) {
            return;
        }
        int make_project = JOptionPane.showConfirmDialog( view, "Would you like to create a ProjectViewer project for " + projectName + "?", "Create ProjectViewer Project?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
        if ( make_project != JOptionPane.YES_OPTION ) {
            return;
        }

        final VPTProject project = new VPTProject( projectName );
        project.setRootPath( projectPath );

        // show the 'create project' dialog
        ProjectOptions.run( project, true, null );

        // get the group as set in the 'create project' dialog
        VPTGroup group = ( VPTGroup ) project.getParent();
        if ( group == null ) {
            group = VPTRoot.getInstance();
        }

        // actually add the project to ProjectManager and set it as the active project
        ProjectManager.getInstance().addProject( project, group );
        ProjectViewer.setActiveNode( jEdit.getActiveView(), project );

        // import the checked out files into the project. This next line is a suggestion
        // from Marcelo that will automatically choose the 'Use CVS or SVN Entries' for
        // importing the files.
        projectviewer.importer.ImportUtils.saveFilter( project.getProperties(), new projectviewer.importer.CVSEntriesFilter(), "projectviewer.import" );
        RootImporter ipi = new RootImporter( project, null, ProjectViewer.getViewer( jEdit.getActiveView() ), jEdit.getActiveView() );
        ipi.doImport();

        // now show ProjectViewer
        view.getDockableWindowManager().showDockableWindow( "projectviewer" );
    }

}

