/*
 *  JavaImportClassForm.java - Plugin for add java imports to the top of a java file.
 *  Copyright (C) 2002 Matthew Flower (MattFlower@yahoo.com)
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package jimporter.importer;

import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gjt.sp.jedit.jEdit;
import javax.swing.JOptionPane;
import java.awt.event.KeyEvent;
import jimporter.searchmethod.SearchMethod;
import javax.swing.KeyStroke;
import javax.swing.AbstractAction;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventPostProcessor;
import javax.swing.JComponent;
import jimporter.classpath.Classpath;

/**
 * This class provides an interface to allow a user to search for a class. The
 * user types an unqualified class name in the top text box and a list of fully
 * qualified class names appear in the JList in the middle panel.
 *
 * @author Matthew Flower
 */
public class JavaImportClassForm extends JDialog {
    private static final String NO_MATCHING_CLASSES_MESSAGE = "No Matching Classes Found";
    
    private DefaultListModel listModel;
    private JPanel buttonPanel;
    private JPanel foundClassesPanel;
    private JButton searchButton;
    private JButton helpButton;
    private JPanel searchPanel;
    private JButton cancelButton;
    private JButton importButton;
    private JTextField classToFind;
    private JList classesList;
    private boolean importCancelled = false;

    /**
     * Creates new form JavaImportClassForm.
     *
     * @param parent Parent frame to the dialog we are going to display.
     * @param className A name of a class that we should fill in the class name
     * textbox by default.
     */
    public JavaImportClassForm(java.awt.Frame parent, String className) {
        super(parent, true);
        initComponents();
        setupEscapeKey();
        if (className != null) {
            classToFind.setText(className);
        }
    }
    
    /**
     * Setup the escape key to cause the windows to close.
     */
    private void setupEscapeKey() {
        //Get the keystroke corresponding to the escape key
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, true);
        
        //Set up the escape key to trigger the "Close action" action.
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "CloseAction");
        
        //Create the "close action" action.
        getRootPane().getActionMap().put("CloseAction", new AbstractAction() {
           public void actionPerformed(ActionEvent ae) {
               importCancelled = true;
               closeDialog(null);
          }
        });
    }

    /**
     * The main method allows the class to be instantiated from the command
     * line.  It is intended primarily for testing purposes.
     *
     * @param args a <code>String[]</code> value containing the command line 
     * arguments.
     */
    public static void main(String args[]) {
        new JavaImportClassForm(new javax.swing.JFrame(), null).show();
    }
    
    public void show() {
        //Since we are showing the dialog box, make sure that if the user closes
        //the dialog box abnormally that we consider the transaction cancelled.
        importCancelled = true;

        super.show();
    }

    /**
     * This method returns the class that the user has selected to import, or
     * the null value if the user choose to import none.
     *
     * @return a <code>String</code> value containing the fully qualified class
     * name of the class the user has chosen to import
     */
    public String getImportedClass() {
        if ((classesList.getSelectedIndex() > -1) && 
            (!classesList.getModel().getElementAt(classesList.getSelectedIndex()).equals(NO_MATCHING_CLASSES_MESSAGE)) &&
            (!importCancelled)) {
            return (String) classesList.getModel().getElementAt(classesList.getSelectedIndex());
        } else {
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        setTitle("Please Select a Class To Import");
        addWindowListener(
            new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    closeDialog(evt);
                }
            });

        //Set up the search panel and add it to the form.
        initSearchPanel();
        getContentPane().add(searchPanel, BorderLayout.NORTH);

        //Set up the "Found Classes" panel and add it to the form.
        initFoundClassesPanel();
        getContentPane().add(foundClassesPanel, BorderLayout.CENTER);

        //Set up the button panel and add it to the form.
        initButtonPanel();
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
    }
    
    /**
     * This method creates the components in the top search panel and
     * initializes them.
     */
    private void initSearchPanel() {
        //Create the search panel
        searchPanel = new JPanel();
        searchPanel.setLayout(new java.awt.BorderLayout());
        searchPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 10, 1, 10)));

        //Create the "Search" button and create a listener to respond to clicks
        searchButton = new JButton("Search");
        searchButton.setMnemonic(KeyEvent.VK_S);
        searchButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    generateImportModel(classToFind.getText());
                }
            });

        //Create the textbox where the user will enter the name of a class to
        //search for.
        classToFind = new JTextField();
        classToFind.addKeyListener(
            new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == evt.VK_ENTER) {
                        generateImportModel(classToFind.getText());
                    } else if (evt.getKeyCode() == evt.VK_DOWN) {
                        if ((classesList.getSelectedIndex() == -1) && (classesList.getModel().getSize() > 0)) {
                            classesList.setSelectedIndex(0);
                        } else if (classesList.getSelectedIndex() < classesList.getModel().getSize() - 1) {
                            classesList.setSelectedIndex(classesList.getSelectedIndex() + 1);
                        }
                    } else if (evt.getKeyCode() == evt.VK_UP) {
                        if (classesList.getSelectedIndex() > 0) {
                            classesList.setSelectedIndex(classesList.getSelectedIndex() - 1);
                        }
                    }
                }
            });

        //Add our components to the search panel
        searchPanel.add(classToFind, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(new JLabel("Class Name"), BorderLayout.NORTH);
    }

    /**
     * Create the panel which contains the JList which will display classes that
     * match our search parameters.
     */
    private void initFoundClassesPanel() {
        //Create our panel which will display the classes matching our search
        //criteria
        foundClassesPanel = new JPanel();
        foundClassesPanel.setLayout(new BorderLayout());
        foundClassesPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 10, 5, 10)));

        //Create the list box which will hold the matching classes
        classesList = new JList();
        classesList.setBorder(new javax.swing.border.EtchedBorder());

        //Set up the list to respond to key presses
        classesList.addKeyListener(
            new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if (evt.getKeyCode() == evt.VK_ENTER) {
                        importCancelled = false;
                        closeDialog(null);
                    } else if (evt.getKeyCode() == evt.VK_ESCAPE) {
                        importCancelled = true;
                        closeDialog(null);
                    }
                }
            });

        //Set the preferred size of the class list -- otherwise it won't show up
        classesList.setPreferredSize(new Dimension(400, 250));

        //Create the storage model for the listbox.  We need to do this so we will
        //have easy access to add or remove things from it when we match searches.
        listModel = new DefaultListModel();
        classesList.setModel(listModel);

        //Add our new components to the panel
        foundClassesPanel.add(classesList, BorderLayout.CENTER);
        foundClassesPanel.add(new JLabel("Matching Classes"), BorderLayout.NORTH);
    }

    /**
     * Create the panel that will contain the button bar that appears at the
     * bottom of the dialog box.
     */
    private void initButtonPanel() {
        buttonPanel = new JPanel();

        // Set up the import button
        importButton = new JButton("Import");
        importButton.setMnemonic('I');
        importButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    importCancelled = false;
                    hide();
                }
            });
        buttonPanel.add(importButton);

        // Set up the cancel button
        cancelButton = new JButton("Cancel");
        cancelButton.setMnemonic(KeyEvent.VK_C);
        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    hide();
                }
            });
        buttonPanel.add(cancelButton);

        // Set up the help button
        helpButton = new JButton("Help");
        helpButton.setMnemonic(KeyEvent.VK_H);
        buttonPanel.add(helpButton);
    }

    /**
     * Closes the dialog box by setting it's visibility to false.  This allows 
     * those who use the dialog box to grab the fully qualified class name.
     *
     * @param evt a <code>WindowEvent</code> object that is entirely unused.
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
    }

    /**
     * Generate list of Imports which correspond to the classname.
     *
     * @param className a <code>String</code> value containing the name of the 
     * class that we are going to search for.
     */
    public void generateImportModel(String className) {
        //Figure out which class searching method we are using and use that method.
        SearchMethod finder = SearchMethod.getCurrent();
        
        //Tell the finder where to look for the class.
        finder.setClassPath(Classpath.getCurrent().getClasspath());
        
        //Return all fully-qualified class names that match our "short" classname.
        List fqClassName = finder.findFullyQualifiedClassName(className);

        //Remove any classes that might be in there from before.
        listModel.removeAllElements();

        if (fqClassName.size() == 0) {
            listModel.addElement(NO_MATCHING_CLASSES_MESSAGE);
        } else {
            //Iterate through all the matching classes, adding each to the classes list
            Iterator it = fqClassName.iterator();
            while (it.hasNext()) {
                listModel.addElement(it.next());
            }
            
            //Select the first item in the list
            classesList.setSelectedIndex(0);
            classesList.requestFocus();
        }
    }
    
    /** 
     * Get the number of classes that matched the search string.
     *
     * @return an <code>int</code> specifying the number of classes that matched
     * the search string.
     */
    public int getMatchCount() {
        int toReturn = 0;
        
        if ((listModel.size() > 0) && 
            (!listModel.getElementAt(0).equals(NO_MATCHING_CLASSES_MESSAGE))) {
            toReturn = listModel.size();
        }
        
        return toReturn;
    }
}
