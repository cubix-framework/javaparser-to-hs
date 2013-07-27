package android.actions;

import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import ise.java.awt.KappaLayout;

// start Android AVD
// requires CommonControls plugin
// assumes "android" and "emulator" are both in your path
// can start multiple AVDs at once
public class LaunchAVD implements Command {

    private View view;

    public void execute( View view ) {
        this.view = view;
        Runner runner = new Runner();
        runner.execute();
    }

    class Runner extends SwingWorker<Vector<String>, Object> {
        @Override
        public Vector<String> doInBackground() {
            Vector<String> avds = new Vector<String>();
            try {
                Process p = Runtime.getRuntime().exec( "android list avd" );
                BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
                while ( true ) {
                    String line = in.readLine();
                    if ( line == null ) {
                        break;

                    }
                    line = line.trim();
                    if ( line.startsWith( "Name: " ) ) {
                        String name = line.substring( "Name: ".length() );
                        if ( name != null && name.trim().length() > 0 ) {
                            avds.add( name );
                        }
                    }
                }
            } catch ( Exception ex ) {
                ex.printStackTrace();
                Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.Can_not_get_list_of_AVDs>", "Can not get list of AVDs:" + "\n" + ex.getMessage() ) );
            }
            return avds;
        }

        @Override
        public void done() {
            try {
                Vector<String> avds = get();
                if ( avds.size() == 0 ) {
                    Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.No_AVDs_found_to_launch.", "No AVDs found to launch." ) );
                    return;
                }

                // create the dialog to choose the AVD to start
                String title = "Launch AVD";
                final JDialog dialog = new JDialog( view, title, false );
                JPanel content = new JPanel( new KappaLayout() );
                content.setBorder( BorderFactory.createEmptyBorder(12, 12, 12, 12 ) );
                dialog.setContentPane( content );

                // create the components
                final JList avdList = new JList( avds );
                avdList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

                // ok/cancel panel
                KappaLayout kl = new KappaLayout();
                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout( kl );
                JButton ok = new JButton( "OK" );
                JButton cancel = new JButton( "Cancel" );
                buttonPanel.add( "0,1,,,,w, 3", ok );
                buttonPanel.add( "1,1,,,,w, 3", cancel );
                kl.makeColumnsSameWidth(0, 1 );
                dialog.getRootPane().setDefaultButton( ok );

                // add the components to the dialog
                content.add( "0, 0, 1, 1, W, w, 3", new JScrollPane( avdList ) );
                content.add( "0, 1", KappaLayout.createVerticalStrut(11 ) );
                content.add( "0, 2, 1, 1, E,, 3", buttonPanel );

                // add listeners
                ok.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        Object[] selected = avdList.getSelectedValues();
                        if ( selected == null || selected.length == 0 ) {
                            Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), jEdit.getProperty( "android.No_AVDs_selected_to_launch.", "No AVDs selected to launch." ) );
                            return;
                        }
                        dialog.dispose();
                        for ( int i = 0; i < selected.length; i++ ) {
                            String avd = ( String ) selected[i];
                            startEmulator( avd );
                        }
                    }
                }
                );
                cancel.addActionListener( new ActionListener() {
                    public void actionPerformed( ActionEvent event ) {
                        dialog.dispose();
                    }
                }
                );

                // show the dialog
                dialog.pack();
                dialog.setLocationRelativeTo( view );
                dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
                dialog.setVisible( true );

            } catch ( Exception e ) {
                GUIUtilities.error( view, "android.listAVDs.error", new String[] {e.getMessage()} );
            }
        }
    }

    void startEmulator( String avd ) {
        if ( avd == null || avd.length() == 0 ) {
            return;
        }
        try {
            Runtime.getRuntime().exec( "emulator -avd " + avd );
        } catch ( Exception e ) {
            e.printStackTrace();
            Util.showError( view, jEdit.getProperty( "android.Error", "Error" ), e.getMessage() );

        }
    }

}

