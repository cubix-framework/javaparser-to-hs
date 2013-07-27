/*
Copyright (C) 2007  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package perl.variables;

import perl.context.StackTrace;
import perl.context.StackTrace.FrameChangeListener;
import perl.core.GdbState;
import perl.core.GdbView;
import perl.core.Parser.GdbResult;
import perl.core.Parser.ResultHandler;
import perl.variables.GdbVar.ChangeListener;
import perl.variables.GdbVar.UpdateListener;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

@SuppressWarnings("serial")
public class LocalVariables extends GdbView implements ChangeListener,
	FrameChangeListener {
	
	private JTree tree;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	
	public LocalVariables() {
		setLayout(new BorderLayout());
		GdbVar.addChangeListener(this);
		StackTrace.addFrameChangeListener(this);
		
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		JButton modify = new JButton("Modify");
		modify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreePath tp = tree.getSelectionPath();
				if (tp == null)
					return;
				Object [] path = tp.getPath();
				GdbVar v = (GdbVar)(path[1]);
				if (v != null)
					v.contextRequested();
			}
		});
		tb.add(modify);
		add(tb, BorderLayout.NORTH);
		
		tree = new JTree();
		root = new DefaultMutableTreeNode("Locals");
		model = new DefaultTreeModel(root);
		tree.setModel(model);
		tree.setRootVisible(false);
		JScrollPane locals = new JScrollPane(tree);
		add(locals);
		tree.addMouseListener(new VarTreeMouseListener());
		if (GdbState.isStopped())
			update();
	}

	public void update() {
		frameChanged(0);
	}
	public void frameChanged(int frame) {
		for (int i = 0; i < root.getChildCount(); i++)
			((GdbVar)root.getChildAt(i)).done();
		root.removeAllChildren();
		getCommandManager().add("-stack-list-arguments 0 " + frame + " " + frame,
				new StackArgumentsResultHandler());
		getCommandManager().add("-stack-list-locals 0", new LocalsResultHandler());
	}
	public void sessionEnded() {
		root.removeAllChildren();
		model.reload(root);
	}

	private class StackArgumentsResultHandler implements ResultHandler {
		@SuppressWarnings("unchecked")
		public void handle(String msg, GdbResult res) {
			if (! msg.equals("done"))
				return;
			Object frameArgs = res.getValue("stack-args/0/frame/args");
			if (frameArgs == null)
				return;
			if (frameArgs instanceof Vector) {
				Vector<Object> args = (Vector<Object>)frameArgs;
				for (int i = 0; i < args.size(); i++) {
					Hashtable<String, Object> hash =
						(Hashtable<String, Object>)args.get(i);
					String name = hash.get("name").toString();
					GdbVar v = new GdbVar(name);
					v.setChangeListener(new UpdateListener() {
						public void updated(GdbVar v) {
							model.reload(v);
						}
					});
					root.add(v);
				}
			}
		}
	}
	
	private class LocalsResultHandler implements ResultHandler {
		@SuppressWarnings("unchecked")
		public void handle(String msg, GdbResult res) {
			if (! msg.equals("done"))
				return;
			Object locals = res.getValue("locals");
			if (locals == null)
				return;
			if (locals instanceof Vector) {
				Vector<Object> localsVec = (Vector<Object>)locals;
				for (int i = 0; i < localsVec.size(); i++) {
					Object local = localsVec.get(i);
					if (local instanceof Hashtable) {
						Hashtable<String, Object> localHash =
							(Hashtable<String, Object>)local;
						String name = localHash.get("name").toString();
						GdbVar v = new GdbVar(name);
						v.setChangeListener(new UpdateListener() {
							public void updated(GdbVar v) {
								model.reload(v);
							}
						});
						root.add(v);
					}
				}
			}
			updateTree();
		}
	}

	public DefaultMutableTreeNode createTreeNode(String name, String value) {
		return new DefaultMutableTreeNode(name + " = " + value);
	}

	public void updateTree() {
		model.reload(root);
	}

	public void changed(GdbVar v) {
		for (int i = 0; i < root.getChildCount(); i++)
			((GdbVar)root.getChildAt(i)).update();
	}

}
