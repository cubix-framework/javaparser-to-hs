/*
 * Copyright (c) 2009, Romain Francois <francoisromain@free.fr>
 *
 * This file is part of the InfoNodePlugin plugin for jedit
 *
 * The InfoNodePlugin plugin for jedit is free software: 
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * The InfoNodePlugin plugin for jedit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the InfoNodePlugin plugin for jedit. If not, see <http://www.gnu.org/licenses/>.
 */
package com.addictedtor.infonode;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.infonode.docking.RootWindow;
import net.infonode.docking.View;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.View.ViewConfig;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowFactory;
import org.gjt.sp.jedit.gui.DockableWindowManager;

@SuppressWarnings("serial")
public class InfoNodeDockingWindowManager extends DockableWindowManager {

	private Map<String,JEditDockableView> dockables ;  
	
	public InfoNodeDockingLayout _layout;
	
	private RootWindow root;
	
	@SuppressWarnings("unused")
	private org.gjt.sp.jedit.View view ; 
	
	private InfoNodeDockingArea TOP ; 
	private InfoNodeDockingArea BOTTOM ; 
	private InfoNodeDockingArea LEFT ; 
	private InfoNodeDockingArea RIGHT ; 
	private Map<String,InfoNodeDockingArea> areaMap; 
	
	private JEditViewView VIEW ; 
	
	public InfoNodeDockingWindowManager(org.gjt.sp.jedit.View view,
			DockableWindowFactory instance, ViewConfig config) {

		super(view, instance, config);
		
		this.view = view; 
		dockables = new HashMap<String,JEditDockableView>() ;
		
		ViewMap viewMap = new ViewMap();
		root = DockingUtil.createRootWindow(viewMap, true);

		// Remove the border around the root window
		root.getRootWindowProperties().getWindowAreaProperties()
			.setInsets(new Insets(0, 0, 0, 0))
			.setBorder(null);

		// Hide window bars around if they contain no window
		root.getRootWindowProperties().getWindowBarProperties().setMinimumWidth(0);
		
		// Create the wrapper view for the text area, remove its toolbar to prevent
		// the user from closing it and set it as main window
		VIEW = new JEditViewView( view, new JLabel( "" ) ) ;
		VIEW.getViewProperties().setAlwaysShowTitle(false);
		VIEW.getViewProperties().getViewTitleBarProperties().setVisible(false);
		root.setWindow(VIEW);
		
		setLayout(new BorderLayout());
		TOP     = new InfoNodeDockingArea( Direction.UP   , this ); 
		BOTTOM  = new InfoNodeDockingArea( Direction.DOWN , this );
		LEFT    = new InfoNodeDockingArea( Direction.LEFT , this );
		RIGHT   = new InfoNodeDockingArea( Direction.RIGHT, this ); 
		areaMap = new HashMap<String,InfoNodeDockingArea>() ;
		
		TOP.init(); 
		LEFT.init(); 
		RIGHT.init();
		BOTTOM.init();
		areaMap.put( "right" , RIGHT) ;
		areaMap.put( "left" , LEFT) ;
		areaMap.put( "top" , TOP) ;
		areaMap.put( "bottom" , BOTTOM) ;
		
		if( config == null){
			_layout = new InfoNodeDockingLayout();
		} else {
			DockingLayout l = config.docking ;
			if( l instanceof InfoNodeDockingLayout ){
				_layout = (InfoNodeDockingLayout)l ; 
			} else {
				// don't know what to do now, maybe generate a default 
				// perspective file from the dock-position properties
				_layout = null; 
			}
		}
		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				add( root , BorderLayout.CENTER ) ;
			}
		}) ;
		
	}

	public JEditViewView getViewView(){
		return VIEW; 
	}
	
	
	@Override
	protected void dockingPositionChanged(String name, String oldPosition,
			String newPosition) {
	}

	/** 
	 * Does nothing at the moment
	 */
	@Override
	public void closeCurrentArea() {}

	@Override
	public JComponent floatDockableWindow(String name) {
		View v = createView(name);
		return (JComponent) v.getComponent();
	}

	@Override
	public DockingLayout getDockingLayout(ViewConfig config) {
		return _layout;
	}

	@Override
	public void hideDockableWindow(String name) {
		if (hasDockable(name)) {
			dockables.get(name).minimize();
		}
	}

	@Override
	public boolean isDockableWindowDocked(String name) {
		if( hasDockable( name) ){
			// add some logic here
			return true ;
		}
		return false;
	}

	@Override
	public boolean isDockableWindowVisible(String name) {
		return hasDockable(name)
				&& dockables.get(name).isVisible();
	}

	@Override
	public void applyDockingLayout(DockingLayout docking) {
		
		/* try to apply the layout as expressed by this plugin */ 
		if( docking instanceof InfoNodeDockingLayout ){
			_layout = (InfoNodeDockingLayout)docking; 
			SwingUtilities.invokeLater( new ApplyRunnable(this, _layout ) ) ; 
		} else{
			super.applyDockingLayout( docking ) ;
		}
	}

	@Override
	public void showDockableWindow(String name) {
		JEditDockableView iview = createView(name);
		focusView(iview);
	}

	@Override
	protected void focusDockable(String name) {
		if (hasDockable(name)) {
			focusView( dockables.get(name) );
		}
	}

	protected void focusView(JEditDockableView iview) {
		iview.makeVisible();
		iview.restoreFocus();
		Component window = iview.getComponent();
		if (window instanceof DefaultFocusComponent)
			((DefaultFocusComponent) window).focusOnDefaultComponent();
		else
			window.requestFocus();
	}

	private JEditDockableView createView(String name) {
		boolean alreadyHasView = hasDockable(name);
		JEditDockableView iview ; 
		if (!alreadyHasView) {
			
			JComponent window = makeDockable(name) ;
			iview = new JEditDockableView( window, name, getDockableTitle(name) ) ; 
			LEFT.getBar().addTab(iview);
			
			String position = jEdit.getProperty( name + "dock-position" ) ;
			if( areaMap.containsKey(position) ){
				areaMap.get(position).getBar().addTab( iview ) ;
			}
			
			
		} else {
			iview = dockables.get(name); 
			if (!iview.isVisible()) {
				iview.restore();
			}
		}
		return iview;
	}

	@Override
	public void setMainPanel(JPanel panel) {
		VIEW.setComponent(panel) ;
		if( _layout != null){
			SwingUtilities.invokeLater( new ApplyRunnable( this, _layout) ); 
		}
	}
	
	private static class ApplyRunnable implements Runnable {
		private InfoNodeDockingWindowManager wm;
		private InfoNodeDockingLayout  layout; 
		public ApplyRunnable( InfoNodeDockingWindowManager wm, InfoNodeDockingLayout layout ){
			this.wm = wm;  
			this.layout = layout; 
		}
		public void run(){
			layout.apply( wm ) ;
		}
	}
	

	@Override
	public DockingArea getBottomDockingArea() {
		return BOTTOM  ;
	}

	@Override
	public DockingArea getLeftDockingArea() {
		return LEFT ;
	}

	@Override
	public DockingArea getRightDockingArea() {
		return RIGHT ;
	}

	@Override
	public DockingArea getTopDockingArea() {
		return TOP  ;
	}

	/**
	 * Dummy. Does not do anything
	 */
	@Override
	protected void propertiesChanged() {
		super.propertiesChanged();
	}

	/**
	 * Not yet implemented
	 */
	@Override
	public void dockableTitleChanged(String dockable, String newTitle) {}

	/**
	 * Dummy. This implementation does not understand this concept
	 */
	@Override
	protected void applyAlternateLayout(boolean alternateLayout) {}

	@Override
	public void disposeDockableWindow(String name) {
		if( hasDockable(name)){
			JEditDockableView dockable = dockables.get(name) ;
			// dockables.remove( dockable ) ;
			dockable.close() ;
		}
	}

	/**
	 * Do we already have a dockable with this title 
	 * 
	 * @param name name of the dockable window 
	 * @return true if we already have this dockable
	 */
	private boolean hasDockable(String name){
		return dockables.containsKey(name) ;
	}
	
	/**
	 * Returns the root window of this manager
	 * 
	 * @return the root window in the view managed by this
	 */
	public RootWindow getRootWindow(){
		return root ; 
	}
	
	public void put( String name, JEditDockableView w){
		dockables.put( name, w) ;
	}

	public JComponent makeDockable(String name) {
		JComponent comp; 
		comp = getDockable(name); 
		if( comp == null){
			comp = createDockable(name) ;
		}
		if( comp == null ){
			comp = new JLabel( "could not create dockable " + name ) ;
		}
		return comp; 
	}
	
}
