/*
* $Revision: 13697 $
* $Date: 2008-09-15 08:54:08 -0400 (Mon, 15 Sep 2008) $
* $Author: kerik-sf $
*
* Copyright (C) 2008 Eric Le Lay
*
* ProgressObs is freely adapted from
* org/gjt/sp/jedit/pluginmgr/PluginManagerProgress.java
* Copyright (C) 2000, 2001 Slava Pestov
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

package cswilly.jeditPlugins.spell;

import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.Description;
import org.junit.runner.Result;

import javax.swing.tree.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.Component;

import java.util.*;

import org.gjt.sp.util.Log;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;

import cswilly.jeditPlugins.spell.hunspellbridge.ProgressObs;

/**
 *  A nice frame providing a tree view of all the tests in a class,
 *  with buttons to run/stop tests and live status update.
 *  To use it, invoke :
 * 	new cswilly.jeditPlugins.spell.JUnitTree(cswilly.jeditPlugins.spell.SpellCheckPluginTest.class)
 *  as a beanshell expression or add it to startup
 */
public class JUnitTree extends JFrame{

	private JUnitTreeModel model;
	/** live tests, to stop them
	 * @see RunNotifier.pleaseStop()
	 */
	private RunNotifier runNotifier;
	
	//actions for the buttons
	private RunOneAction runAction;
	private Action stopAction,clearAction;
	
	/** the original request, to get it's runner
	 */
	private Request request;
	
	/**
	 * Construct and display a new instance for given class.
	 * One can (probably) have multiple instances, but FEST-Swing doesn't like
	 * multiple tests at once
	 * TODO: does keeping request and descriptions arround prevent plugin reloading ?
	 * @param	c	class to test
	 */
	public JUnitTree(Class c){
		super("Runner for class "+c.getName());
		
		request = Request.aClass(c);
		Runner runner = request.getRunner();
		Description description = runner.getDescription();
		
		model = new JUnitTreeModel(description);
		init();
	}
	
	private void init(){

		//{{{ Buttons
		Box buttonsBox = Box.createHorizontalBox();
		
		runAction = new RunOneAction();
		JButton bout = new JButton(runAction);
		bout.setName("run-one");
		buttonsBox.add(bout);
		
		stopAction = new StopAction();
		bout = new JButton(stopAction);
		bout.setName("stop");
		buttonsBox.add(bout);
		
		clearAction = new ClearAction();
		bout = new JButton(clearAction);
		bout.setName("clear");
		buttonsBox.add(bout);
		
		getContentPane().add(BorderLayout.NORTH,buttonsBox);
		//}}}
		
		//{{{ Tree
		JTree tree = new JTree(model);
		tree.setCellRenderer(new JUnitCellRenderer());
		tree.getSelectionModel().addTreeSelectionListener(runAction);
		
		getContentPane().add(BorderLayout.CENTER,new JScrollPane(tree));
		
		//}}}
		
		//{{{ Status
		JTextArea area = new JTextArea("",4,50);
		UpdateDescriptionPane upd = new UpdateDescriptionPane(area);
		tree.getSelectionModel().addTreeSelectionListener(upd);
		getContentPane().add(BorderLayout.SOUTH,new JScrollPane(area));
		//}}}
		
		pack();
		new Thread(){
			public void run(){
				setVisible(true);
		}}.start();
	}
	
	
	static enum TestStatus {NONE,STARTED,SUCCESS,FAILURE};
	
	static class JUnitTreeNode implements TreeNode{
		private Description description;
		private Failure failure;
		private TestStatus status;
		private List<JUnitTreeNode> children;
		private JUnitTreeNode parent;
		
		public JUnitTreeNode(JUnitTreeNode parent,Description d){
			this.parent = parent;
			description = d;
			
			if(d.isSuite()){
				List<Description> dchildren = d.getChildren();
				children = new ArrayList<JUnitTreeNode>(dchildren.size());
				for(Description cd : dchildren){
					children.add(new JUnitTreeNode(this,cd));
				}
			}else{
				children = Collections.emptyList();
			}
			status = TestStatus.NONE;
		}
		
		public Enumeration children(){
			return Collections.enumeration(children);
		}
		
		public boolean getAllowsChildren(){
			return !children.isEmpty();
		}
		
		public TreeNode getChildAt(int index){
			if(index<0||index>=children.size())return null;
			return children.get(index);
		}
		
		public int getChildCount(){
			return children.size();
		}
		
		public int getIndex(TreeNode node){
			return children.indexOf(node);
		}
		
		public TreeNode getParent(){
			return parent;
		}
		
		public boolean isLeaf(){
			return children.isEmpty();
		}
		
		
		public TestStatus getStatus(){
			return status;
		}
		
		void setStatus(TestStatus ts){
			status = ts;
		}
		
		void clear(){
			setStatus(TestStatus.NONE);
			failure = null;
			for(JUnitTreeNode child:children){
				child.clear();
			}
		}
	}
	
	
	static class JUnitTreeModel implements TreeModel{
		private JUnitTreeNode rootNode;
		private List<TreeModelListener> listeners;
		private Map<Description,TreePath> mapper;
		
		JUnitTreeModel(Description d){
			rootNode = new JUnitTreeNode(null,d);
			listeners = new ArrayList<TreeModelListener>();

			//mapper
			mapper = new HashMap<Description,TreePath>();
			TreePath rootPath = new TreePath(rootNode);
			mapper.put(d,rootPath);
			for(int i=0;i<rootNode.getChildCount();i++){
				initMapper(rootPath,(JUnitTreeNode)rootNode.getChildAt(i));
			}
		}
		
		//{{{ from TreeModel
 		public Object getChild(Object parent, int index){
			if(parent==null)return null;
			if(index<0)return null;
			if(!(parent instanceof JUnitTreeNode))return null;
			
			JUnitTreeNode p = (JUnitTreeNode)parent;
			return p.getChildAt(index);
		}
		
 		public int getChildCount(Object parent){
			if(parent==null)return 0;
			if(!(parent instanceof JUnitTreeNode))return 0;
			
			JUnitTreeNode p = (JUnitTreeNode)parent;
			return p.getChildCount();
		}
		
 		public int getIndexOfChild(Object parent, Object child){
			if(parent==null)return -1;
			if(child==null)return -1;
			if(!(parent instanceof JUnitTreeNode))return -1;
			
			JUnitTreeNode p = (JUnitTreeNode)parent;
			return p.getIndex((JUnitTreeNode)child);
		}
		
 		public Object getRoot(){
			return rootNode;
		}
 		public boolean isLeaf(Object node){
			if(node==null)return true;
			if(!(node instanceof JUnitTreeNode))return true;
			
			JUnitTreeNode p = (JUnitTreeNode)node;
			return p.isLeaf();
			
		}
 		public void addTreeModelListener(TreeModelListener l){
			listeners.add(l);
		}
 		public void removeTreeModelListener(TreeModelListener l){
			listeners.remove(l);
		}
		
 		public void valueForPathChanged(TreePath path, Object newValue){
			throw new UnsupportedOperationException("should not change");
		}
		
		//}}}

		private void clearTests(){
			rootNode.clear();
			fireTreeChanged(rootNode);
		}

		private void fireTreeChanged(JUnitTreeNode tn){
			TreePath parentPath = mapper.get(tn.description);
			fireNodesChanged(new TreeModelEvent(this,parentPath));
			if(!tn.isLeaf()){
				Object[]children = tn.children.toArray(new Object[0]);
				int[]indices = new int[children.length];
				for(int i=0;i<indices.length;i++){
					indices[i]=i;
				}
				fireNodesChanged(new TreeModelEvent(this,parentPath,indices,children));
			}
		}
		
		private void fireNodeChanged(TreePath tp){
			fireNodesChanged(new TreeModelEvent(this,tp));
		}
		
		private void fireNodesChanged(TreeModelEvent tme){
			for(TreeModelListener tm:listeners){
				tm.treeNodesChanged(tme);
			}
		}
		
		private TreePath getPathForDescription(Description d){
			return mapper.get(d);
		}
		
		private void initMapper(TreePath tp,JUnitTreeNode node){
			TreePath newPath = tp.pathByAddingChild(node);
			mapper.put(node.description,newPath);
			for(int i=0;i<node.getChildCount();i++){
				initMapper(newPath,(JUnitTreeNode)node.getChildAt(i));
			}
		}
	}
	
	class MyRunListener extends RunListener{
		private TreePath currentPath;
		private JUnitTreeNode currentNode;
		
		
		//{{{ from RunListener
		public void testFailure(Failure failure) {
			Log.log(Log.ERROR,JUnitTree.class,"Test failed : "+failure.getDescription());
			Log.log(Log.ERROR,JUnitTree.class,failure.getException());
			if(currentPath==null)return;
			currentNode.setStatus(TestStatus.FAILURE);
			currentNode.failure = failure;
			model.fireNodeChanged(currentPath);
		}
		
		public void testFinished(Description description){
			if(currentPath==null)return;
			//Log.log(Log.ERROR,JUnitTree.class,"Finished : "+description);
			if(currentNode.getStatus()!=TestStatus.FAILURE)currentNode.setStatus(TestStatus.SUCCESS);
			model.fireNodeChanged(currentPath);
		}
		
		public void testIgnored(Description description){
			TreePath tp = model.getPathForDescription(description);
			if(tp==null)return;
			JUnitTreeNode jtn = (JUnitTreeNode)tp.getLastPathComponent();
			jtn.setStatus(TestStatus.NONE);
			model.fireNodeChanged(tp);
		}
		
		public void testRunFinished(Result result){
			JUnitTreeNode jtn = (JUnitTreeNode)model.getRoot();
			if(result.wasSuccessful()&&TestStatus.FAILURE!=jtn.getStatus())
				jtn.setStatus(TestStatus.SUCCESS);
			else{
				jtn.setStatus(TestStatus.FAILURE);
			}
			model.fireNodeChanged(new TreePath(jtn));
		}
		
		public void testRunStarted(Description description){
			currentPath = new TreePath(model.getRoot());
			currentNode = (JUnitTreeNode)currentPath.getLastPathComponent();
			currentNode.setStatus(TestStatus.STARTED);
			model.fireNodeChanged(currentPath);
		}
		
		public void testStarted(Description description){
			currentPath = model.getPathForDescription(description);
			if(currentPath==null){
				Log.log(Log.ERROR,JUnitTree.class,"path is null for "+description);
			}
			currentNode = (JUnitTreeNode)currentPath.getLastPathComponent();
			currentNode.setStatus(TestStatus.STARTED);
			model.fireNodeChanged(currentPath);
		}
		
		//}}}
		
		
	}		
	
	void stopTests(){
		if(runNotifier!=null){
			runNotifier.pleaseStop();
			runNotifier=null;
			stopAction.setEnabled(false);
		}
	}
	
	void runTests(TreePath path){
		JUnitTreeNode tn = (JUnitTreeNode)path.getLastPathComponent();
		Request newRequest;
		if(path.getPathCount()==1)newRequest=request;
		else newRequest = request.filterWith(tn.description);
		final Runner runner = newRequest.getRunner();
		stopTests();
		runAction.setEnabled(false);
		stopAction.setEnabled(true);
		clearAction.setEnabled(false);
		
		Thread testThread = new Thread("JUnitTree runner thread"){
			private RunNotifier myRunNotifier;
			
			public void run(){
				Log.log(Log.NOTICE,JUnitTree.class,"running test : "+runner.getDescription()+" ("+runner.getDescription().testCount()+") tests");
				myRunNotifier=new RunNotifier();
				runNotifier=myRunNotifier;
				Result result= new Result();
				RunListener resultListener= result.createListener();
				RunListener myListener = new MyRunListener();
				runNotifier.addFirstListener(resultListener);
				runNotifier.addListener(myListener);
				try {
					myRunNotifier.fireTestRunStarted(runner.getDescription());
					runner.run(runNotifier);
					myRunNotifier.fireTestRunFinished(result);
				}catch(StoppedByUserException sbue){
					Log.log(Log.NOTICE,JUnitTree.class,"Tests Interrupted by user");
					try{
						myListener.testFailure(new Failure(runner.getDescription(),sbue));
						myRunNotifier.fireTestRunFinished(result);
					}catch(Exception e){}
				}catch(Throwable t){
					Log.log(Log.ERROR,JUnitTree.class,"Exception : "+t);
					
				}finally {
					Log.log(Log.NOTICE,JUnitTree.class,"Done with test!");
					myRunNotifier.removeListener(resultListener);
					myRunNotifier.removeListener(myListener);
					runAction.setEnabled(true);
					stopAction.setEnabled(false);
					clearAction.setEnabled(true);
					runNotifier = null;
				}
				
			}
		};
		testThread.start();
	}
	
	class RunOneAction extends AbstractAction implements TreeSelectionListener{
		private TreePath currentPath;
		
		RunOneAction(){
			super("Run");
			putValue(Action.SHORT_DESCRIPTION,"Run selected test case");
			setEnabled(false);
		}
		
		public void valueChanged(TreeSelectionEvent tse){
			TreePath selectedPath = tse.getPath();
			if(selectedPath==null){
				if(isEnabled())setEnabled(false);
				currentPath=null;
				return;
			}else{
				currentPath = selectedPath;
				if(!isEnabled())setEnabled(true);
			}
		}
		
		public void actionPerformed(ActionEvent ae){
			if(currentPath!=null)runTests(currentPath);
			else setEnabled(false);
		}
	}
	
	class StopAction extends AbstractAction{
		StopAction(){
			super("Stop!");
			putValue(Action.SHORT_DESCRIPTION,"Abort the tests");
			setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent ae){
			stopTests();
			setEnabled(false);
		}
	}
	
	class ClearAction extends AbstractAction{
		ClearAction(){
			super("Clear");
			putValue(Action.SHORT_DESCRIPTION,"Clear the tests");
			setEnabled(true);
		}
		
		public void actionPerformed(ActionEvent ae){
			runAction.setEnabled(false);
			model.clearTests();
			runAction.setEnabled(true);
		}
	}

	class JUnitCellRenderer extends DefaultTreeCellRenderer{
		private final Map<TestStatus,Icon> icons;
		
		JUnitCellRenderer(){
			icons = new EnumMap<TestStatus,Icon>(TestStatus.class);
			icons.put(TestStatus.NONE,GUIUtilities.loadIcon("16x16/status/document-empty.png"));
			icons.put(TestStatus.STARTED,GUIUtilities.loadIcon("16x16/actions/view-refresh.png"));
			icons.put(TestStatus.FAILURE,GUIUtilities.loadIcon("16x16/status/dialog-warning.png"));
			icons.put(TestStatus.SUCCESS,GUIUtilities.loadIcon("16x16/status/dialog-information.png"));
		}
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus){
			JUnitTreeNode node = (JUnitTreeNode)value;
			String label = node.description.getDisplayName();
			super.getTreeCellRendererComponent(tree,label,
					sel,expanded,leaf,row,hasFocus);
			setIcon(icons.get(node.getStatus()));
			return this;
		}
	}
	
	static class UpdateDescriptionPane implements TreeSelectionListener{
		private JTextArea area;

		UpdateDescriptionPane(JTextArea area){
			this.area = area;
		}

		public void valueChanged(TreeSelectionEvent tse){
			TreePath selectedPath = tse.getPath();
			if(selectedPath==null){
				area.setText("");
			}else{
					
				JUnitTreeNode node = (JUnitTreeNode)selectedPath.getLastPathComponent();
				
				String t = node.description.getDisplayName();
				t+="\n";
				switch(node.getStatus()){
					case FAILURE:
						{
							t+="Test FAILED !!\n";
							if(node.failure!=null){
								t+=node.failure.getMessage();
								t+="\n";
								t+=node.failure.getTrace();
							}
							break;
						}
					case SUCCESS:
						{
							t+="Test passed !\n";
							break;
						}
				}

				area.setText(t);
			}
		}
	}
}


