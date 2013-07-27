/***************************************************************
*  Copyright notice
*
*  (c) 2005,2006 Neil Bertram (neil@tasmanstudios.co.nz)
*  All rights reserved
*
*  This plugin is part of the Typo3 project. The Typo3 project is
*  free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*
*  The GNU General Public License can be found at
*  http://www.gnu.org/copyleft/gpl.html.
*  A copy is found in the textfile GPL.txt
*
*
*  This plugin is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  This copyright notice MUST APPEAR in all copies of the source!
***************************************************************/
/**
 * $Id: RemoteCallWorker.java 10415 2007-08-22 12:02:29Z n31l $
 * 
 * This handles the communication with the TYPO3 server.
 * This is accomplished through XML-RPC calls (using Apache's
 * XML-RPC library) to the jeditvfs plugin on the TYPO3 side.
 *
 * @author      Neil Bertram <neil@tasmanstudios.co.nz>
 */
package typoscript;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.gjt.sp.util.Log;

/**
 * This class handles the gruntwork of communicating with the TYPO3 plugin over XML-RPC
 * 
 */
public class RemoteCallWorker {
	
	private static final int CLIENT_PROTOCOL_VERSION = 2;
	
	public static final short JEDITVFS_ERROR_AUTHFAIL = 0;
	public static final short JEDITVFS_ERROR_UNKNOWNMETHOD = 1;
	public static final short JEDITVFS_ERROR_NOSUCHTEMPLATE = 2;
	private T3Site site;
	private XmlRpcClient xmlrpc;
	private boolean protocolVersionTested = false;
	
	public RemoteCallWorker(T3Site parent) {
		site = parent;
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(site.getUrlFull());
		config.setEncoding("UTF-8");
		config.setBasicUserName(site.getUsername());
		config.setBasicPassword(site.getPassword());
		
		xmlrpc = new XmlRpcClient();
		xmlrpc.setConfig(config);
	}
	
	public HashMap getPageTree() throws XmlRpcException, IOException {
		HashMap result = null;
		result = (HashMap)remoteCall("vfs.getList", null);
		return result;
	}
	
	public String getSiteTitle() throws IOException, XmlRpcException {
		String result = null;
		result = (String)remoteCall("vfs.getSiteTitle", null);
		return result;
	}
	
	public String getConstants(Integer templateuid) throws IOException, XmlRpcException {
		String result = null;
		ArrayList params = new ArrayList(1);
		params.add(templateuid);
		result = (String)remoteCall("vfs.getConstants", params);
		return result;
	}
	
	public String getSetup(Integer templateuid) throws IOException, XmlRpcException {
		String result = null;
		ArrayList params = new ArrayList(1);
		params.add(templateuid);
		result = (String)remoteCall("vfs.getSetup", params);
		return result;
	}
	
	public void putConstants(Integer templateuid, String constants) throws IOException, XmlRpcException {
		ArrayList params = new ArrayList(3);
		params.add(templateuid);
		params.add(constants);
		Boolean clearCache = new Boolean(site.clearCacheOnSave());
		params.add(clearCache);
		remoteCall("vfs.putConstants", params);
	}
	
	public void putSetup(Integer templateuid, String setup) throws IOException, XmlRpcException {
		ArrayList params = new ArrayList(3);
		params.add(templateuid);
		params.add(setup);
		Boolean clearCache = new Boolean(site.clearCacheOnSave());
		params.add(clearCache);
		remoteCall("vfs.putSetup", params);
	}
	
	private Object remoteCall(String methodName, ArrayList params) throws IOException, XmlRpcException {
		if (!protocolVersionTested) {
			protocolVersionTested = true;
			
			// Need to check the protocols are in sync
			Integer remoteVersion = null;
			try {
				remoteVersion = (Integer)this.remoteCall("vfs.getProtocolVersion", null);
			} catch (XmlRpcException e) {
				if (e.code == JEDITVFS_ERROR_UNKNOWNMETHOD) {
					// Probably a server that doesn't support this call, eg. version 1
					remoteVersion = new Integer(1);
				} else {
					throw e;
				}
			}
			if (remoteVersion.intValue() > CLIENT_PROTOCOL_VERSION) {
				JOptionPane.showMessageDialog(null, "This plugin is outdated, please check for updates in Plugin Manager", "TypoScript: Protocol version mismatch (can't proceed)", JOptionPane.ERROR_MESSAGE);
				return null;
			} else if (remoteVersion.intValue() < CLIENT_PROTOCOL_VERSION) {
				JOptionPane.showMessageDialog(null, "The version of jeditvfs on your server is out-of-date, please update it using the extension manager", "TypoScript: Protocol version mismatch (can't proceed)", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			
			// If we get here, all is well
			Log.log(Log.DEBUG, this, "Procotol version check passed");
		}
		
		if (params == null) params = new ArrayList(0);
		
		// Append the protocol version to the request to prove we know about this (version 1 clients didn't do this, so will get
		// and XmlRpcException from a modern server for not providing it)
		params.add("PROTOCOL" + CLIENT_PROTOCOL_VERSION);
		
		Log.log(Log.DEBUG, this, "Sending remote call " + methodName  + " params " + params);
		
		Object result = xmlrpc.execute(methodName, params);
		if (result == null) throw new XmlRpcException(-1, "No result - Server plugin missing?");
		if (result instanceof XmlRpcException) throw (XmlRpcException)result; // bug in xmlrpc?
		Log.log(Log.DEBUG, this, "Result: " + result);
		return result;
	}
}
