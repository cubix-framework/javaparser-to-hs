/*:folding=indent:
 * NavigationPanel.java - LaTeX Navigator
 * Copyright (C) 2002 Anthony Roy
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
package uk.co.antroy.latextools; 

import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.REMatch;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

public class NavigationPanel
  extends DefaultToolPanel {

  //~ Instance/static variables ...............................................

  private int lowlev = 0;
  private ArrayList navItems = new ArrayList();
  private JTree navTree;
  private JComboBox options;
  private JScrollPane scp = new JScrollPane();

  //~ Constructors ............................................................

  /**
   * Creates a new NavigationPanel object.
   * 
   * @param view the current view
   * @param buff the active buffer
   */
  public NavigationPanel(View view, Buffer buff) {
    super(view, buff, "Nav");
    _init();
  }

  //~ Methods .................................................................

  /**
   * ¤
   * 
   * @param view ¤
   * @param buff ¤
   */
  public static void createNavigationDialog(View view, Buffer buff) {

    final NavigationPanel n = new NavigationPanel(view, buff);
    EnhancedDialog ed = new EnhancedDialog(view, "LaTeX Navigator", false) {
      public void cancel() {
        this.hide();
      }

      public void ok() {
        this.hide();
      }
    };

    ed.setContentPane(n);
    ed.pack();
    ed.show();
  }

  /**
   * ¤
   * 
   * @param view ¤
   * @param buff ¤
   * @return ¤
   */
  public static JPanel createNavigationPanel(View view, Buffer buff) {

    return new NavigationPanel(view, buff);
  }

  /**
   * ¤
   * 
   * @return ¤
   */
  public JComboBox getCombo() {

    return options;
  }

  /**
   * ¤
   * 
   * @return ¤
   */
  public JScrollPane getScrollPane() {

    return scp;
  }

  /**
   * ¤
   */
  public void _init() {
    ArrayList navList = new ArrayList(NavigationList.getNavigationData());
    options = new JComboBox(navList.toArray());
    options.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refresh();
      }
    });
    refresh();
  }

  /**
   * ¤
   */
  public void refresh() {

    if (bufferChanged) {
      removeAll();
      bufferChanged = false;
    }

    if (!isTeXFile(buffer)) {
      displayNotTeX(BorderLayout.CENTER);
    } else {
      loadNavigationItems();
      buildTree();
      scp.setViewportView(navTree);
      setLayout(new BorderLayout());
      setPreferredSize(new Dimension(400, 100));
      add(scp, BorderLayout.CENTER);
      add(options, BorderLayout.NORTH);
      add(createButtonPanel(RELOAD_AND_REFRESH), BorderLayout.SOUTH);
    }

    repaint();
  }

  /**
   * ¤
   */
  public void reload() {
    ArrayList navList = new ArrayList(NavigationList.getNavigationData());
    options.removeAllItems();

    Iterator it = navList.iterator();

    while (it.hasNext()) {
      options.addItem(it.next());
    }

    refresh();
  }

  private void buildTree() {

    File f = new File(buffer.getPath());
    String fileName = f.getName();
    DefaultMutableTreeNode top = new DefaultMutableTreeNode(new TagPair(
                                                                  fileName, 0, 
                                                                  0));
    navTree = new JTree(top);
    navTree.getSelectionModel().setSelectionMode(
          TreeSelectionModel.SINGLE_TREE_SELECTION);
    navTree.putClientProperty("JTree.lineStyle", "Angled");
    navTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) navTree.getLastSelectedPathComponent();

        if (node == null)

          return;

        LaTeXAsset asset = (LaTeXAsset) node.getUserObject();
        visitLabel(asset.start.getOffset());
      }
    });

    Iterator it = navItems.iterator();
    DefaultMutableTreeNode lastNode = top;

    while (it.hasNext()) {

      TagPair t = (TagPair) it.next();
      DefaultMutableTreeNode n = new DefaultMutableTreeNode(t);
      DefaultMutableTreeNode correctNode = findCorrectNode(n, lastNode);
      lastNode = n;
      correctNode.add(n);
    }

    for (int i = 0; i < navTree.getRowCount(); i++) {
      navTree.expandRow(i);
    }
  }

  private void createButtonPanel() {
  }

  private DefaultMutableTreeNode findCorrectNode(DefaultMutableTreeNode thisNode, 
                                                 DefaultMutableTreeNode lastNode) {

    LaTeXAsset thisTp = (LaTeXAsset) thisNode.getUserObject();
    int thisLevel = thisTp.getLevel();
    LaTeXAsset lastTp = (LaTeXAsset) lastNode.getUserObject();
    int lastLevel = lastTp.getLevel();

    if (thisLevel > lastLevel) {

      return lastNode;
    } else if (thisLevel == lastLevel) {

      return (DefaultMutableTreeNode) lastNode.getParent();
    } else {

      return findCorrectNode(thisNode, 
                             (DefaultMutableTreeNode) lastNode.getParent());
    }
  }


  private void loadNavigationItems() {
    navItems.clear();
		
		NavigationList nlist = (NavigationList) options.getSelectedItem();
		File main = new File(LaTeXMacros.getMainFile());
		
		if (main.exists()) {
			searchInput(main, nlist);
		}else {
			searchBuffer(buffer, nlist);
		}
		
    Collections.sort(navItems);
  }

	private void searchInput(File input, NavigationList nlist){
		Buffer[] buffs = jEdit.getBuffers();

		// Call appropriate search method.
		for (int i=0; i<buffs.length; i++){
			
			File bfile = new File(buffs[i].getPath());			
			if (bfile.equals(input)){
				searchBuffer(buffs[i], nlist);
				return;
			}
		}
				searchFile(input, nlist);
	}
	
	private void searchFile(File input, NavigationList nlist){
		// Search a file for items in the NavigationList.
		try{
			
			BufferedReader in = new BufferedReader(new FileReader(input));
      String nextLine = in.readLine().trim();
			int index = 0;

      while (nextLine != null) {
				lowlev = nlist.getLowestLevel();
				searchLine(nextLine, index, nlist);
				index++;
        nextLine = in.readLine().trim();
			}
		}
		catch(Exception e){
			Log.log(Log.ERROR, this, "Reading file: " + input.toString() + "caused error:" + e);
		}
	}
	
	private void searchBuffer(Buffer input, NavigationList nlist){
		// Search a buffer for items in the NavigationList.
		  
			int index = 0;

			while (index < input.getLineCount() - 1) {
				lowlev = nlist.getLowestLevel();
				String line = input.getLineText(index);
				searchLine(line, index, nlist);
				index++;
			}

	}

	private void searchLine(String line, int lineNumber, NavigationList nl){
		// Search a string for items in the NavigationList.
		    
				Iterator it = nl.iterator();

	      while (it.hasNext()) {

        TagPair srch = (TagPair) it.next();
        RE search = null;

        try {
          search = new RE(srch.getTag());
        } catch (REException e) {

          search = null;
        }

        if (search == null) {

          int refStart = line.indexOf(srch.getTag());

          if (refStart >= 0) {

            LaTeXAsset asset = LaTeXAsset.createAsset(line,
                                                      buffer.createPosition(buffer.getLineStartOffset(lineNumber)),
                                                      buffer.createPosition(buffer.getLineEndOffset(lineNumber)),
                                                      srch.getLevel(),
                                                      srch.getIcon());
            navItems.add(asset);

            break;
          }
        } else {

          REMatch match = search.getMatch(line);
          String result = "";

          if (match != null) {

            int i;

            if ((i = search.getNumSubs()) > 0) {

              StringBuffer sb = new StringBuffer();

              for (int j = 1; j <= i; j++) {
                sb.append(match.toString(j));
              }

              result = sb.toString();
            } else {
              result = line;
            }

            LaTeXAsset asset = LaTeXAsset.createAsset(result,
                                                      buffer.createPosition(buffer.getLineStartOffset(lineNumber)),
                                                      buffer.createPosition(buffer.getLineEndOffset(lineNumber)),
                                                      srch.getLevel(),
                                                      srch.getIcon());
            navItems.add(asset);

            break;
          }
        }
      }
	
	}
	
	
  private void visitLabel(int offset) {
    view.getTextArea().setFirstLine(buffer.getLineOfOffset(offset));
  }

}
