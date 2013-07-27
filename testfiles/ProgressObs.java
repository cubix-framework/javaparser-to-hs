/*
 * $Revision: 14018 $
 * $Date: 2008-11-08 04:32:14 -0500 (Sat, 08 Nov 2008) $
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

package cswilly.jeditPlugins.spell.hunspellbridge;

import javax.swing.JButton;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Frame;
import java.awt.Dialog;

import javax.swing.SwingUtilities;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.ProgressObserver;

public class ProgressObs implements ProgressObserver{

		//{{{ ProgressObs constructor
		public ProgressObs(ActionListener action){
			this.stopAction = action;
			init();
		}

		public ProgressObs(Thread thread)
		{
			this.thread = thread;
			init();
		}
		///}}}
		
		
		//{{{ asDialog() and asComponent()
		public JDialog asDialog(Frame parent,String title)
		{
			dialog = new JDialog(parent,title);
			initDialog(dialog);
			return dialog;
		}

		public JDialog asDialog(Dialog parent,String title)
		{
			dialog = new JDialog(parent,title);
			initDialog(dialog);
			return dialog;
		}


		public JComponent asComponent(String title){
			content.add(BorderLayout.NORTH,new JLabel(title));
			return content;
		}
		
		private void initDialog(final JDialog dialog){
			dialog.setContentPane(content);

			dialog.addWindowListener(new WindowHandler());
	
			dialog.pack();
			new Thread(){
				public void run(){
					dialog.setVisible(true);
			}}.start();
		}

		private void init(){
			content = new JPanel(new BorderLayout(12,12));
			content.setBorder(new EmptyBorder(12,12,12,12));
	
			progress = new JProgressBar();
			progress.setStringPainted(true);
			progress.setString("<WAITING>");
	
	
			progress.setMaximum(1);
			content.add(BorderLayout.NORTH,progress);
	
			
			if(thread!=null && stopAction==null){
				stopAction = new ActionListener(){
				public void actionPerformed(ActionEvent evt){
						ProgressObs.this.thread.stop();
						if(dialog!=null)dialog.setVisible(false);
				}};
			}
			
			stop = new JButton(jEdit.getProperty("options.spellcheck.hunspell.stop","Stop"));
			stop.addActionListener(stopAction);
			JPanel panel = new JPanel(new FlowLayout(
				FlowLayout.CENTER,0,0));
			panel.add(stop);
			content.add(BorderLayout.CENTER,panel);
			} //}}}
	
	
		//{{{ setValue() method
		/**
		 * Update the progress value.
		 *
		 * @param value the new value
		 */
		public void setValue(final long value)
		{
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						progress.setValue(valueSoFar + (int) value);
					}
				});
		} //}}}
	
		//{{{ setMaximum() method
		/**
		 * This method is unused with the plugin manager.
		 *
		 * @param value the new max value (it will be ignored)
		 * @since jEdit 4.3pre3
		 */
		public void setMaximum(final long value) 
		{
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						progress.setMaximum((int)value);
					}
				});
		} //}}}
	
		//{{{ setStatus() method
		 public void setStatus(final String status) 
		 {
			SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						 progress.setString(status);
					}
				});
		} //}}}
	
	
		//{{{ Private members
	
		//{{{ Instance variables
		private Thread thread;
	
		private JProgressBar progress;
		private JButton stop;
		private ActionListener stopAction;
		// progress value as of start of current task
		private int valueSoFar;
		private JPanel content;
		private JDialog dialog;
		//}}}
	
	
		//{{{ WindowHandler class
		class WindowHandler extends WindowAdapter
		{
			public void windowClosing(WindowEvent evt)
			{
				thread.stop();
				evt.getWindow().dispose();
			}
		} //}}}
	
		//}}}
	}

