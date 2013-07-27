/**
 * DavPlugin.java - Main class of the DAV plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 * @author James Glaubiger
 * @author Martin Raspe
 * @version $$
*/
package dav;

//{{{ imports
import java.awt.Component;
import java.util.Hashtable;
import java.util.Vector;
import java.io.File;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import javax.swing.*;

import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

import org.apache.util.HttpURL;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.PropertyName;
import org.apache.webdav.lib.methods.*;
import org.apache.webdav.lib.Lock;
import org.apache.webdav.lib.properties.LockDiscoveryProperty;
//}}}

public class DavPlugin extends EBPlugin
{
	//{{{ start() method
	/**Run at jEdit startup
	 * Creates the local working directory
	 */
	public void start()
	{
		// (replaced by services.xml)
		// VFSManager.registerVFS(DavVFS.PROTOCOL,new DavVFS());
		new File(DavVFS.WORKING_DIRECTORY).mkdir();
	} //}}}
	
	//{{{ stop() method
	 /** Run at jEdit exit
	 * Clears all cached directories
	 * Unlocks all files opened with our plugin
	 */
	public void stop()
	{
		DirectoryCache.clearAllCachedDirectories();
	    LockManager.unlockAllFiles();
	} //}}}
	
	//{{{ createMenuItems() method
	public void createMenuItems(Vector menuItems)
	{
		menuItems.addElement(GUIUtilities.loadMenu("dav"));
	} //}}}
	
	//{{{ handleMessage() method
	/** reacts to "buffer closed" message
	*/
	public void handleMessage(EBMessage msg) {
        if (!(msg instanceof BufferUpdate))
            return;
        final BufferUpdate umsg = (BufferUpdate) msg;
        if (umsg.getWhat() != umsg.CLOSED)
            return;
		final String path = umsg.getBuffer().getPath();
		if (!path.startsWith(DavVFS.PROTOCOL))
		    return;
		VFS vfs = VFSManager.getVFSForProtocol(DavVFS.PROTOCOL);
		((DavVFS)vfs)._close(path);
	} //}}}
	
	//{{{ addNotify() method
	/** register plugin with the edit bus
	*/
	public void addNotify() {
		EditBus.addToBus(this);
	} //}}}
	
	//{{{ removeNotify() method
	/** unregister plugin with the edit bus
	*/
	public void removeNotify() {
		EditBus.removeFromBus(this);
	} //}}}

	//{{{ showOpenDAVDialog() method
	/** choose a file
	*/
	public static void showOpenDAVDialog(View view)
 	{
		String path = ((DavVFS)VFSManager.getVFSForProtocol(DavVFS.PROTOCOL))
			.showBrowseDialog(new Object[1],view);

		if(path != null)
		{
			String[] files = GUIUtilities.showVFSFileDialog(
				view,path,VFSBrowser.OPEN_DIALOG,true);
			if(files == null)
				return;

			Buffer buffer = null;
			for(int i = 0; i < files.length; i++)
			{
				Buffer _buffer = jEdit.openFile((View)null,files[i]);
				if(_buffer != null)
					buffer = _buffer;
			}
			if(buffer != null)
				view.setBuffer(buffer);
		}
	} //}}}

	//{{{ showSaveDAVDialog() method
	/** save a file
	*/
	public static void showSaveDAVDialog(View view)
	{
		String path = ((DavVFS)VFSManager.getVFSForProtocol(DavVFS.PROTOCOL))
			.showBrowseDialog(new Object[1],view);
		if(path != null)
		{
			String[] files = GUIUtilities.showVFSFileDialog(
				view,path,VFSBrowser.SAVE_DIALOG,false);
			if(files == null)
				return;

			view.getBuffer().save(view,files[0],true);
		}
	} //}}}

	//{{{ clear_directory_cache() method
	/** deletes the working directory with the local copy of the remote collection
	*/
	public static void clear_directory_cache(View view){
		File dir = new File(DavVFS.WORKING_DIRECTORY);

		if(!dav.DavVFS.deleteDir(dir)){
			JOptionPane.showMessageDialog(view, "Warning: Error Deleting Files in " + DavVFS.WORKING_DIRECTORY);
		}else{
			JOptionPane.showMessageDialog(view, "All Files Deleted From " + DavVFS.WORKING_DIRECTORY);
		}
		new File( DavVFS.WORKING_DIRECTORY).mkdir();
	} //}}}
	
	//{{{ editProps() method
	public static void editProps(View view){
		Buffer buffer = view.getBuffer();
		String stringUrl = buffer.getPath();
		PropertyName comment = new PropertyName("http://dav.cse.ucsc.edu/props/jedit", "Comment");
		PropertyName author = new PropertyName("http://dav.cse.ucsc.edu/props/jedit", "Author");
		String commentStringValue = new String();
		String authorStringValue = new String();
		boolean locked = false;
		
		ConnectionManager.ConnectionInfo info = 
			ConnectionManager.getConnectionInfo( view, new DavAddress(stringUrl) );
		
		WebdavResource webdavResource = ConnectionManager.getResource(stringUrl);
		
		Vector properties = new Vector();
		try {
			//Get the property values one at a time.  Makes things easier
			properties.addElement( comment );
			Enumeration commentValue =
				webdavResource.propfindMethod(stringUrl, properties);
			
			if ( commentValue.hasMoreElements() ){
				commentStringValue = (String)commentValue.nextElement();
			} else {
				Log.log(Log.DEBUG, DavPlugin.class, "Attempt to get comments on " + stringUrl + " failed.");
			}
			
			properties.removeAllElements();
			properties.addElement( author );
			Enumeration authorValue = 
				webdavResource.propfindMethod(stringUrl, properties);

			if ( authorValue.hasMoreElements() ){
				authorStringValue = (String)authorValue.nextElement();
			} else {
				Log.log(Log.DEBUG, DavPlugin.class, "Attempt to get comments on " + stringUrl + " failed.");
			}
		} catch ( HttpException he ) {
			JOptionPane.showMessageDialog(view, "HttpException: PropFind Method: " + he.getMessage());
		} catch ( IOException ioe ) {
		} catch ( Throwable e) {
		}
		
		//Set up dialog window to get new Author and Comments
		PropsDialog dialog = new PropsDialog(view, authorStringValue, null, commentStringValue, null);

		if ( dialog.isOK() ){
			authorStringValue = dialog.getAuthor();
			commentStringValue = dialog.getComment();
			try{
				//Patch the new properties to the resource
				if ( !webdavResource.proppatchMethod(stringUrl, comment, commentStringValue ) ){
						JOptionPane.showMessageDialog(view, "Error: proppatchMethod for comment failed: " +
							webdavResource.getStatusMessage() );
				}
				if ( !webdavResource.proppatchMethod(stringUrl, author, authorStringValue ) ){
						JOptionPane.showMessageDialog(view, "Error: proppatchMethod for author failed: " +
							webdavResource.getStatusMessage() );
				}
				//webdavResource.close();
			} catch ( HttpException he ) {
				JOptionPane.showMessageDialog(view, "HttpException: PropPatch Method:" + he.getMessage());
			} catch ( IOException ioe ) {
			}
		}
	} //}}}
	
	//{{{ unlockThisFile() method
	/** explicitly unlock the WebDav file in the current buffer 
	*/
	public static void unlockThisFile( View view ){
		Buffer buffer = view.getBuffer();
		String stringUrl = buffer.getPath();
		
		if ( !ConnectionManager.existsResource( stringUrl ) ){
			JOptionPane.showMessageDialog(view, "File " + stringUrl + " not associated with WebDAV");
			return;
		}
		
		LockManager.unlockFile( stringUrl );
	} //}}}
}
