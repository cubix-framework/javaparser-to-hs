/*
 * $Revision: 13763 $
 * $Date: 2008-09-23 06:02:41 -0400 (Tue, 23 Sep 2008) $
 * $Author: kerik-sf $
 *
 * Copyright (C) 2008 Eric Le Lay
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.util.*;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.util.Log;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


//for asDialog()
import org.gjt.sp.jedit.gui.EnhancedDialog;
import java.beans.*;
import common.gui.OkCancelButtons;


import cswilly.spell.SpellException;
import cswilly.spell.EngineManager;
import cswilly.spell.Dictionary;

import static cswilly.jeditPlugins.spell.SpellCheckPlugin.*;


/**
 * encapsulates dictionnary selection.
 * pilot via refresh Action
**/
public class DictionaryPicker{
	static final String LISTING_DICTS = "spell-check.listing-dicts.combo-item";
	static final String REFRESH_BUTTON_START= "options.SpellCheck.refresh-langs";
	static final String REFRESH_BUTTON_STOP= "options.SpellCheck.stop-refresh-langs";
	static final String NO_DICTIONARY="options.SpellCheck.no-dictionary";

	private static final String INITIAL_LANG_PROP = "dict-picker-initial-lang";
	public static final String LANG_PROP = "dict-picker-lang";
	public static final String ERROR_PROP = "dict-picker-error";
	public static final String CONFIRMED_PROP = "dict-picker-confirmed";

	private MutableComboBoxModel modelDicts;
	private Future<Vector<Dictionary>> futureDicts;
	private EngineManager futureSource;
	private Action refreshAction;
	
	private PropertyStore propertyStore;
	
	public DictionaryPicker(EngineManager futureSource,String initial){
		modelDicts= new DefaultComboBoxModel();
		this.futureSource = futureSource;
		refreshAction = null;
		propertyStore = new PropertyStore(this);
		propertyStore.put(INITIAL_LANG_PROP,initial);
	}
	/**
	 * thread-safe. Not effective until a refresh is triggered.
	 * @param	newEngine	new EngineManager to use
	 * @throws	NullPointerException	on null newEngine
	 */
	public void setEngineManager(EngineManager newEngine){
		if(newEngine == null)throw new IllegalArgumentException("EngineManager can't be null");
		futureSource=newEngine;
	}
	
	public JComboBox asComboBox(){
		final JComboBox _aspellMainLanguageList = new JComboBox( modelDicts );
		_aspellMainLanguageList.setName("languages");
		_aspellMainLanguageList.setEditable( true );
		_aspellMainLanguageList.setPrototypeDisplayValue("Franais Classique (France)");
		_aspellMainLanguageList.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					Object itm = _aspellMainLanguageList.getSelectedItem();
					if(itm==null)return;
					System.out.println("item is :"+itm.getClass()+" itm="+itm);
					if(itm instanceof Dictionary){
						Dictionary dict = (Dictionary)itm;
						if(dict==null)return;
						propertyStore.put(LANG_PROP,dict.getName());
						Log.log(Log.DEBUG,DictionaryPicker.this,"changed : "+dict);
					}
				}
		});
		return _aspellMainLanguageList;
	}
	
	public JDialog asDialog(Frame view){
	 String title = jEdit.getProperty("spell-check-pick-language.title","");
		
		EnhancedDialog dialog = new EnhancedDialog(view,title,true){
			public void ok(){
				DictionaryPicker.this.getPropertyStore().put(CONFIRMED_PROP,"true");
				setVisible(false);
			}
			public void cancel(){
				DictionaryPicker.this.cancel();
				setVisible(false);
			}
		};
		final JTextArea errorReport = new JTextArea(4,40);
		errorReport.setName("error-report");
		errorReport.setEditable(false);
		errorReport.setForeground(Color.RED);
		JScrollPane sp = new JScrollPane(errorReport);
		dialog.getContentPane().add(BorderLayout.NORTH,asComboBox());
		dialog.getContentPane().add(BorderLayout.CENTER,sp);
		dialog.getContentPane().add(BorderLayout.SOUTH,new OkCancelButtons(dialog));
		
		getPropertyStore().addPropertyChangeListener(new PropertyChangeListener(){
				public void propertyChange(final PropertyChangeEvent pe){
					if(ERROR_PROP.equals(pe.getPropertyName()) && pe.getNewValue()!=null)
					SwingUtilities.invokeLater(new Runnable(){
							public void run(){errorReport.setText(pe.getNewValue().toString());}
					});
				}
		});
		
		dialog.pack();
		return dialog;
	}
	
	public PropertyStore getPropertyStore(){
		return propertyStore;
	}
	
	private class RefreshAction extends AbstractAction{

		RefreshAction(){
			setEnabled(false);
		}
		
		public void actionPerformed( ActionEvent evt )
		{
			if(futureDicts == null)
			{
			 	refreshList();
			}else{
				cancel();
			}
		}
	}
	
	public Action getRefreshAction(){
		if(refreshAction==null) refreshAction = new RefreshAction();
		return refreshAction;
	}
	
	public void cancel(){
		if(futureDicts!=null)
		{
			getRefreshAction().setEnabled(false);//disable while threads cleanup
			futureDicts.cancel(true);
		}
	}
	
	/**
	* executes a FutureListDicts in a new Thread.
	* finally, update the combo model to contain all found dictionnaries
	* the futureDicts variable serves both to cancel in response to user action on Stop! button
	* and to flag that some work is done (while it's not null).
	* The aspell executable used is the one currently configured, even if not applied yet.
	**/
	private void refreshList(){
		new Thread(){
			public void run(){
				refreshAction.putValue(Action.NAME,jEdit.getProperty(REFRESH_BUTTON_STOP));
				String listing = jEdit.getProperty( LISTING_DICTS, "" );
				propertyStore.put(ERROR_PROP,null);
				modelDicts.addElement(listing);
				modelDicts.setSelectedItem(listing);
				final Vector<Dictionary> dicts = new Vector<Dictionary>();
				

				getRefreshAction().setEnabled(true);
				
				Future<Vector<Dictionary>> ft = futureSource.getAlternateLangDictionaries();
				futureDicts = ft;
				try
				{
					dicts.addAll(ft.get(10,TimeUnit.SECONDS));
				}
				catch(InterruptedException ie){
					Log.log(Log.ERROR,DictionaryPicker.this,ie);
					ft.cancel(true);
					propertyStore.put(ERROR_PROP,"Interrupted while listing dictionaries");
				}
				catch(CancellationException ce){
					Log.log(Log.DEBUG,DictionaryPicker.this,"Cancel Action worked");
					propertyStore.put(ERROR_PROP,jEdit.getProperty("list-dict-cancelled.message"));
				}
				catch(ExecutionException ee){
					Log.log(Log.ERROR,DictionaryPicker.this,ee);
					ft.cancel(true);
					propertyStore.put(ERROR_PROP,jEdit.getProperty("list-dict-error.message",new Object[]{ee.getCause().getMessage()}));

				}
				catch(TimeoutException te){
					Log.log(Log.ERROR,DictionaryPicker.this,te);
					ft.cancel(true);
					propertyStore.put(ERROR_PROP,jEdit.getProperty("list-dict-timeout-error.message"));
				}
				
				SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
				 			if(!dicts.isEmpty())
								Log.log(Log.DEBUG,DictionaryPicker.this,"finished refreshList ("+dicts.size()+" dictionaries found)");
							else
								Log.log(Log.ERROR,DictionaryPicker.this,"no dictionary found)");
							while(modelDicts.getSize()!=0)modelDicts.removeElementAt(0);//remove listing...
							for(int i=0;i<dicts.size();i++)modelDicts.addElement(dicts.get(i));
							if(dicts.isEmpty()){
								String nothing = jEdit.getProperty( NO_DICTIONARY, "" );
								modelDicts.addElement(nothing);
								modelDicts.setSelectedItem(nothing);
							}else{
								String dict = propertyStore.get(INITIAL_LANG_PROP);
								Log.log(Log.DEBUG,DictionaryPicker.this,"dict was "+dict);
								for(Dictionary d:dicts){
									if(d.getName().equals(dict))modelDicts.setSelectedItem(d);
								}
							}
							
							getRefreshAction().putValue(Action.NAME,jEdit.getProperty(REFRESH_BUTTON_START));
							getRefreshAction().setEnabled(true);
							//we are done!
							futureDicts = null;
					 	}
					});
		}}.start();
	}
}
