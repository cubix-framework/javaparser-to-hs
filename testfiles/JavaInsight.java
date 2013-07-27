/*
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

package net.sourceforge.jedit.javainsight;
 
//GUI stuff.
import javax.swing.*;
import java.awt.*;
import javax.swing.tree.*;

//classpath browser functionality
import net.sourceforge.jedit.buildtools.java.packagebrowser.*;
import net.sourceforge.jedit.buildtools.java.classpathmanager.*;
import net.sourceforge.jedit.buildtools.StaticLogger;
import net.sourceforge.jedit.buildtools.*;

//qsort functionality
import org.gjt.sp.jedit.MiscUtilities;

//events
import javax.swing.event.*;
import java.awt.event.*;

//decompile support
import java.io.*;
import jode.*;

//jedit open file support.
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View;

import net.sourceforge.jedit.pluginholder.*;

public class JavaInsight extends HoldablePlugin implements TreeSelectionListener, MouseListener {
    
    public final static String PRODUCT      = "Java Insight";
    public final static String VERSION      = "0.0.3";    

    public final static String PACKAGES     = "Packages";
    public final static String CLASSPATH    = "Classpath";


    private DefaultMutableTreeNode  root            = new DefaultMutableTreeNode("All Packages");
    private JTree                   tree            = new JTree( root );
    private JLabel                  status          = new JLabel(" ");
    private DefaultMutableTreeNode  currentNode     = null;
    private View                    view            = null;
    private JTabbedPane             tabs            = new JTabbedPane();
    


    public void init(Config config) {

        this.view = config.getView();
        
        this.setSize(new Dimension(300, 600) );
        //this.tree.setRootVisible(false);
        
        //populate the root node...
        JavaPackage[] packages = PackageBrowser.getPackages();
        
        MiscUtilities.quicksort(packages, new JavaPackageComparator() );
        
        
        for (int i = 0; i < packages.length; ++i) {
            DefaultMutableTreeNode packageNode = new DefaultMutableTreeNode( packages[i].getName() );

            JavaClass[] classes = packages[i].getClasses();
            MiscUtilities.quicksort(classes, new JavaClassComparator() );         
         
            for (int j = 0; j < classes.length; ++j) {

                packageNode.add( new DefaultMutableTreeNode( classes[j] ) );

            }

            root.add( packageNode );
        }



        
        tabs.addTab(PACKAGES, new JScrollPane( tree ));
        tabs.addTab(CLASSPATH, new JScrollPane( new ClasspathManager() ));

        this.add( tabs, BorderLayout.CENTER);
        this.add(status, BorderLayout.SOUTH);

        this.tree.addTreeSelectionListener(this);
        this.tree.addMouseListener(this);

        //expand the root node so that all its children are instantly visible
        
        this.tree.expandPath( new TreePath( root.getPath() ) );
        
        this.setVisible(true);

        
    }
    
    public void setStatus(String status) {
        this.status.setText(status);        
    }

    public DefaultMutableTreeNode getCurrentNode() {
        return this.currentNode;
    }

    /**
     * Method that returns classpath needed by the Jode Decompiler Package.
     * The Jode package expects a comma delimited classpath.
    **/   

    public static String getJodeClassPath() {
        String classArray[] = JavaUtils.getClasspath();
        String classpath = "";
        for(int i=0; i<classArray.length; ++i)
           classpath += classArray[i]+",";
        if(!classpath.equals(""))
          classpath = classpath.substring(0, classpath.length()-1);
        else
          classpath = ".";
        return classpath;
    }


    /**
    Given a classname, decompile it and store it on the filesystem.
    

    @param className the name of the class is to be decompiled.
    @param force     forces this class to be decompiled even if it was on the 
    filesystem from before
    @return the name of the filename that was decompiled.
    */
    public static String decompileClass(String className, boolean force) {
        

       
        String output = getBaseDirectory() + 
                        System.getProperty("file.separator") + 
                        getJavaFile(className);


                        
                        
        System.out.println(getJodeClassPath());
        //if it already exists... assume that it was decompiled successfully before.
        if ( new File(output).exists() ) {
            return output;
        }
                        
        String[] params =   { 
                            className,
                            "--pretty",
                            "-c",
                            getJodeClassPath()
                            };

                            
                            
                            
                            
        //make sure all its directories exist.
        new File( output.substring(0, output.lastIndexOf(System.getProperty("file.separator")) ) ).mkdirs();
                            

        PrintStream original = System.out;
        try {                    
            System.setOut( new PrintStream( new FileOutputStream( output ) ) );
            jode.decompiler.Main.main(params);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            System.setOut(original);
        }
        
        
        return output;
    }

    /**
     Given a java class name (ie org.apache.jetspeed.Test) return a filename
     (ie org/apache/jetspeed/Test.java)
    
     @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
     @version $Id: JavaInsight.java 9273 2000-05-07 02:37:45Z  $
     */
    private static String getJavaFile(String classname) {
        String filename = MiscUtils.globalStringReplace( classname, ".", System.getProperty("file.separator") );
        return filename + ".java";
    }

    /**
    Return the base directory for JavaInsight.  On UNIX this would be 
    /tmp/Java Insight
    */
    public static String getBaseDirectory() {
        return MiscUtils.getTempDir() + System.getProperty("file.separator") + MiscUtils.globalStringReplace( PRODUCT, " ", "" );
    }
    



    //MouseListener interface
    public void mouseClicked(MouseEvent evt) {

        if(evt.getClickCount() == 2) {

            if (this.getCurrentNode() == null) {
                return;
            }

            if ( this.getCurrentNode().getUserObject() instanceof JavaClass ) {
                String object = ((JavaClass)this.getCurrentNode().getUserObject()).getName();

                StaticLogger.log("Decompiling: " + object);

                this.setCursor( new Cursor(Cursor.WAIT_CURSOR) );

                String result = decompileClass(object, false);

                this.setCursor( new Cursor(Cursor.DEFAULT_CURSOR) );                

                jEdit.openFile( view, null, result, false, false );
                
            }

        }

    }
    
    public void mousePressed(MouseEvent evt)  { }
    public void mouseReleased(MouseEvent evt) { }
    public void mouseEntered(MouseEvent evt)  { }
    public void mouseExited(MouseEvent evt)   { }



    //TreeSelectionListener interface
    /**
    @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>    
    */
    public void valueChanged(TreeSelectionEvent e) {
        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)this.tree.getLastSelectedPathComponent();
        if (node == null)
            return;
        
        
        this.currentNode = node;
        
        if (node.getUserObject() instanceof JavaClass) {
            //also output the source of this class
            JavaClass classnode = ((JavaClass)node.getUserObject());
            
            System.out.println("The source CLASSPATH entry of \"" + classnode.getName() + "\" is \"" + classnode.getSource() + "\"");
            
            this.setStatus( classnode.getName() );
        }
    }
    

}
