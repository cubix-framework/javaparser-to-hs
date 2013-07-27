package android.actions;

import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;
import ise.java.awt.KappaLayout;

// Create Android project
// Assumes "android" is in your path.
// Must have CommonControls plugin installed for KappaLayout.
public class CreateAVD implements Command {

    private View view;

    // targets start with a number, sort on the number
    TreeMap<String, String> targetSkins = new TreeMap<String, String>( new Comparator<String>() {
        public int compare( String a, String b ) {
            String inta = a.substring(0, a.indexOf( ' ' ) );
            String intb = b.substring(0, b.indexOf( ' ' ) );
            return ( new Integer( inta ).compareTo( new Integer( intb ) ) );
        }
    } );

    public void execute( View view ) {
        this.view = view;
        Runner runner = new Runner();
        runner.execute();
    }

    class Runner extends SwingWorker<Vector<String>, Object> {
        @Override
        public Vector<String> doInBackground() {
            // load the available targets and associated skins
            loadTargetsAndSkins();
            Vector<String> targets = new Vector<String>();
            for ( String target : targetSkins.keySet() ) {
                targets.add( target );
            }
            return targets;
        }

        @Override
        public void done() {
            Vector<String> targets = null;
            try {
                targets = get();
            } catch ( Exception e ) {
                e.printStackTrace();
                Util.showError(view, jEdit.getProperty("android.Error", "Error"), e.getMessage());
                return;
            }
            if ( targets == null ) {
                Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.No_targets_found.", "No targets found."));
                return;
            }
            // create the dialog
            String title = "Create Android Virtual Device (AVD)";
            final JDialog dialog = new JDialog( view, title, false );
            JPanel content = new JPanel( new KappaLayout() );
            content.setBorder( BorderFactory.createEmptyBorder(12, 12, 12, 12 ) );
            dialog.setContentPane( content );

            // create the components
            final JTextField nameField = new JTextField();
            final JComboBox targetField = new JComboBox( targets );
            final JTextField cardSizeField = new JTextField();
            final JRadioButton kButton = new JRadioButton( "K" );
            final JRadioButton mButton = new JRadioButton( "M" );
            final JComboBox skinField = new JComboBox();

            // km stuff
            JPanel kmPanel = new JPanel();
            kmPanel.add( kButton );
            kmPanel.add( mButton );
            ButtonGroup bg = new ButtonGroup();
            bg.add( kButton );
            bg.add( mButton );
            mButton.setSelected( true );

            // ok/cancel panel
            KappaLayout kl = new KappaLayout();
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout( kl );
            JButton ok = new JButton( "OK" );
            JButton cancel = new JButton( "Cancel" );
            buttonPanel.add( "0,1,,,,w, 3", ok );
            buttonPanel.add( "1,1,,,,w, 3", cancel );
            kl.makeColumnsSameWidth(0, 1 );

            // add the components to the dialog
            content.add( "0, 0, 1, 1, W, w, 3", new JLabel( "AVD Name" ) );
            content.add( "1, 0, 4, 1, W, w, 3", nameField );

            content.add( "0, 1, 1, 1, W, w, 3", new JLabel( "Target:" ) );
            content.add( "1, 1, 4, 1, W, w, 3", targetField );

            content.add( "0, 2, 1, 1, W, w, 3", new JLabel( "SD Card Size:" ) );
            content.add( "1, 2, 3, 1, W, w, 3", cardSizeField );
            content.add( "4, 2, 1, 1, W, w, 3", kmPanel );

            content.add( "0, 3, 1, 1, W, w, 3", new JLabel( "Skin:" ) );
            content.add( "1, 3, 4, 1, W, w, 3", skinField );

            content.add( "0, 5", KappaLayout.createVerticalStrut(11 ) );

            content.add( "0, 6, 5, 1, E,, 3", buttonPanel );

            // add listeners
            ok.addActionListener ( new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    String selectedItem = ( String ) targetField.getSelectedItem();
                    String target = selectedItem.substring(0, selectedItem.indexOf( ' ' ) );
                    String name = nameField.getText();
                    if ( name != null && name.trim().length() > 0 ) {
                        if ( name.indexOf( ' ' ) > -1 ) {
                            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Name_may_not_have_spaces.", "Name may not have spaces."));
                            name = name.replaceAll( " ", "" );
                            return;
                        }
                    }
                    String cardSize = cardSizeField.getText();
                    if ( cardSize == null || cardSize.trim().length() == 0 ) {
                        cardSize = "0";
                    }
                    cardSize += mButton.isSelected() ? "M" : "K";

                    String skin = ( String ) skinField.getSelectedItem();
                    if ( skin.indexOf( "(default)" ) > -1 ) {
                        skin = skin.substring(0, skin.indexOf( "(default)" ) ).trim();
                    }
                    boolean success = createAndroidAVD( name, target, cardSize, skin );
                    if (success) {
                        dialog.dispose();
                    }
                    
                    
                }
            }
            );
            cancel.addActionListener ( new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    dialog.dispose();
                }
            }
            );
            targetField.addActionListener ( new ActionListener() {
                public void actionPerformed( ActionEvent ae ) {
                    String target = (String)targetField.getSelectedItem();
                    loadSkinCombo( skinField, target );
                }
            }
            );

            // show the dialog
            dialog.pack();
            String target = (String)targetField.getSelectedItem();
            loadSkinCombo( skinField, target );
            dialog.setLocationRelativeTo( view );
            dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
            dialog.setVisible( true );
        }
    }

    boolean createAndroidAVD( String name, String targetId, String cardSize, String skin ) {
        if ( name == null || name.trim().length() == 0 ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Name_cannot_be_empty.", "Name cannot be empty."));
            return false;
        }
        if ( targetId == null || targetId.trim().length() == 0 ) {
            Util.showError(view, jEdit.getProperty("android.Error", "Error"), jEdit.getProperty("android.Target_ID_cannot_be_empty.", "Target ID cannot be empty."));
            return false;
        }
        if ( cardSize == null || cardSize.trim().length() == 0 ) {
            cardSize = "0M";
        }
        if ( skin == null || skin.trim().length() == 0 ) {
            skin = getDefaultSkin( targetId );
            skin = skin.substring(0, skin.indexOf( "(default)" ) ).trim();
        }
        String command = "android create avd -n " + name + " -t " + targetId + " -c " + cardSize + " -s " + skin;
        Util.runInSystemShell(view, command);
        return true;
    }

    String getDefaultSkin( String target ) {
        String skinNames = targetSkins.get( target );
        String[] skins = skinNames.split( ", " );
        for ( String skin : skins ) {
            if ( skin.indexOf( "(default)" ) > -1 ) {
                return skin;
            }
        }
        return null;
    }

    void loadTargetsAndSkins() {
        // load the available target names
        try {
            Process p = Runtime.getRuntime().exec( "android list targets" );
            BufferedReader in = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
            String name = null;
            while ( true ) {
                String line = in.readLine();
                if ( line == null ) {
                    break;
                }
                if ( line.startsWith( "id:" ) ) {
                    String id = line.substring(4, line.indexOf( ' ', 4 ) );
                    name = line.substring( line.indexOf( ' ', 4 ) + " or ".length() );
                    name = name.replaceAll( "\"", "" );
                    name = id + ' ' + name;
                }
                if ( line.trim().startsWith( "Skins: " ) ) {
                    String skins = line.trim().substring( "Skins: ".length() );
                    targetSkins.put( name, skins );
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    Vector<String> getSkins( String target ) {
        String skinNames = targetSkins.get( target );
        String[] skins = skinNames.split( ", " );
        Vector<String> v = new Vector<String>();
        for ( String skin : skins ) {
            v.add( skin );
        }
        return v;
    }

    void loadSkinCombo( JComboBox skinField, String target ) {
        Vector<String> skins = getSkins( target );
        skinField.setModel( new DefaultComboBoxModel( skins ) );
        skinField.setSelectedItem( getDefaultSkin( target ) );
    }

}

