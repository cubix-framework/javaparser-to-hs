/*
 * Created on Apr 21, 2004
 *
 */
package xquery.options;

import java.awt.GridBagConstraints;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.JARClassLoader;
import org.gjt.sp.jedit.jEdit;

import xquery.AdapterOptionsPanel;

/**
 * @author Wim Le Page
 * @author Pieter Wellens
 *
 */
public class AdapterOptionPane extends AbstractOptionPane {
	
	private AdapterOptionsPanel adapterOptionsPanel = null;
	public boolean exception = false;
	
	public AdapterOptionPane(){
		super("xquery.adapter");
	}
	
	public void _init()
	{
		try {
			exception = false;
			
			//dynamic loading of the OptionsPanel of the selected adapter
			JARClassLoader loader = new JARClassLoader();
			Class adapterOptionsPanelClass = null;
			Constructor adapterOptionsPanelConstructor = null;
			
			String adapterString = jEdit.getProperty("xquery.adapter.selection");
		
			//adapterOptionsPanelClass = loader.loadClass(jEdit.getProperty("xquery.adapter.selection") + "OptionsPanel");
			adapterOptionsPanelClass = loader.loadClass(adapterString.toLowerCase() + "." + adapterString + "OptionsPanel");
			adapterOptionsPanelConstructor = adapterOptionsPanelClass.getConstructor(new Class[] {});
			adapterOptionsPanel = (AdapterOptionsPanel)adapterOptionsPanelConstructor.newInstance(new Object[] {});
			adapterOptionsPanel.init();
			removeAll(); //needed to do a clean refresh
			addComponent(adapterOptionsPanel, GridBagConstraints.HORIZONTAL); //this adds the complete OptionsPanel from the adapter
		// We have to catch because _init in AbstractOptionPane does not throw
		// That is also the reason that we create an OptionsPanel in the catchclauses with information because jEdit expects one
		} catch (ClassNotFoundException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"No OptionsPanel found for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (SecurityException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"SecurityException while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (NoSuchMethodException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"No init method found while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (IllegalArgumentException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"IllegalArgumentException while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (InstantiationException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"InstantiationExceptioin while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (IllegalAccessException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"IllegalAccessException while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		} catch (InvocationTargetException e) {
			adapterOptionsPanel = new AdapterOptionsPanel();
			removeAll();
			JOptionPane.showMessageDialog(null,"InvocationException while creating OptionsPanel for " + jEdit.getProperty("xquery.adapter.selection"),"Error Creating OptionsPanel", JOptionPane.ERROR_MESSAGE );
			exception = true;
		}
	}
	
	public void _save()
	{
		adapterOptionsPanel.save();
		initialized = false; // because of dynamic loading of this pane
	}
}
