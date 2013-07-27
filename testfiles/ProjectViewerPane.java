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
package projectviewer;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.lang.*;


/**
This is the option pane that jEdit displays for Plugin Options.
*/
public class ProjectViewerPane
  extends AbstractOptionPane
{

  private JPanel options;
  private ProjectViewer viewer;
  
  /**
   * Create a new <code>ProjectViewerPane</code>.
   */
	public ProjectViewerPane( ProjectViewer aViewer ) {
		super(ProjectPlugin.NAME);
    viewer = aViewer;
    options = new JPanel();
    setLayout( new BorderLayout() );
    options.setLayout( new BorderLayout() );
    add(options, BorderLayout.CENTER);

    options.add( getOptionPanel( viewer.getCurrentProject() ) );
	}

  /**
   * Save project configuration.
   */
	public void save() {
	}

  /**
  Returns a JPanel with information about this Projects options...  if project
  is null it assumes you mean all projects...
  
  @author <A HREF="mailto:burton@relativity.yi.org">Kevin A. Burton</A>
  @version $Id: ProjectViewerPane.java 5718 2001-05-21 12:37:22Z cyu $
  */
  private JPanel getOptionPanel(Project project) {
    JPanel options = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    
    JPanel misc = new JPanel();
    misc.setBorder( BorderFactory.createTitledBorder("Misc:"));

    gbc.insets = new Insets(2,2,2,2);
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    if (project != null) {
      misc.add(new JLabel("Project Root:  "));
      
      JTextField rootfield = new JTextField(project.getRoot().getPath());
      misc.add(rootfield);
        
    } else {
      misc.add(new JLabel("Number of Projects: "
        + ProjectManager.getInstance().getProjectCount()) );
    }
    options.add(misc, gbc);

    if (project != null) {
      JPanel compiler = new JPanel(new GridBagLayout());
      compiler.setBorder( BorderFactory.createTitledBorder("Build Options:") );
      GridBagConstraints compilerGbc = new GridBagConstraints();

      JCheckBox compile = new JCheckBox("Build \"" + project.getName() + "\"");
        
      compilerGbc.gridy = 0;
      compilerGbc.insets = new Insets(2,2,2,2);

      compiler.add(compile, compilerGbc);

      gbc.gridy = 1;
      options.add(compiler, gbc);
    }    

    return options;
  }

  public static void main(String args[]) {
    ProjectViewerPane pvp = new ProjectViewerPane(null);

    JFrame frame = new JFrame();
    frame.getContentPane().add(pvp);
    frame.setSize(new Dimension(350, 600) );
    frame.setVisible(true);
    frame.toFront();        
  }

}
