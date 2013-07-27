/**
 * LockManager.java - manage DAV resource locks
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 * @author James Glaubiger
 * @version $$
*/
package dav;

//{{{ imports
import javax.swing.*;
import java.io.IOException;
import java.awt.Component;

import org.gjt.sp.jedit.io.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
import org.apache.util.HttpURL;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpException;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.*;
import org.apache.webdav.lib.Lock;
import org.apache.webdav.lib.properties.LockDiscoveryProperty;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;

import java.util.Vector;
//}}}

public class LockManager extends Thread
{
   	//{{{ private members
	private String filePath;
	private String lockOwner;
	private String lockPassword;
	private WebdavResource resource;
	
	//Duration of lock, in seconds
	private static int LOCK_TIME = 720;
	
	//Wait until two minutes before lock is expired to attempt to refresh lock
	private static int RELOCK_WAIT = LOCK_TIME - 120;
	
	//List of locked files
	private static Vector lockedFiles = new Vector();
	//}}}
	
	//{{{ LockManager () constructor
	LockManager( String path, String owner, String password ){
		filePath = path;
		lockOwner = owner;
		lockPassword = password;
	} //}}}
	
	//{{{ run () method
	/** Set up WebdavResource associated with filePath
	 * Loop
	 *  Check for active locks on file
	 *   - if lock exists, and we own it, remove it
	 *   - else print notification message
	 *  Lock file, then counts down until lock time expires.
	 *  Unless lock has been explicitly removed by the user, re-lock the file
	 */
	public void run(){
		//Add to list of locked files, if file is not already in list
		//This check is done in case user opens a file multiple times in one session
		if (lockedFiles.indexOf(filePath) == -1)
			lockedFiles.addElement(filePath);

		//No need to run thread if file has already been locked this session
		else
			return;
		
		//this.resource = ConnectionManager.getResource(filePath);

		while ( true ){
			try{
				resource = ConnectionManager.getResource(filePath);
				//				JOptionPane.showMessageDialog(null, "lock activity on  " + filePath,
				//		      "WARNING", JOptionPane.WARNING_MESSAGE);
				
				LockDiscoveryProperty lockDiscoveryProperty = 
					resource.lockDiscoveryPropertyFindMethod(filePath);

				if ( lockDiscoveryProperty != null ){
					Lock[] locks = lockDiscoveryProperty.getActiveLocks();

					//Check for active locks
					if ( locks != null && locks.length > 0 ){
						//If lock is not owned by us, let user know
						if ( locks[0].getOwner().compareTo(lockOwner) != 0 ){
								JOptionPane.showMessageDialog(null, "Existing lock on " + filePath
									+ "\nis owned by " + locks[0].getOwner()
									+ " and will remain for " + locks[0].getTimeout() + " seconds.",
									"WARNING", JOptionPane.WARNING_MESSAGE);
								lockedFiles.remove(filePath);
								break;
						}
					}
					//Log.log(Log.DEBUG, LockManager.class, "Attempting to (re)lock " + filePath);
					//Log.log(Log.DEBUG, LockManager.class, "Lock owner = " + lockOwner);
					
					//Lock the file
					if (!resource.lockMethod(lockOwner, LOCK_TIME)){
						JOptionPane.showMessageDialog(null, "Lock failed on " + filePath,
								"ERROR", JOptionPane.ERROR_MESSAGE);
						Log.log(Log.ERROR, LockManager.class, "LOCK on " +
								filePath + ": "+
								resource.getStatusMessage());
						lockedFiles.remove(filePath);
						break;
					}
					//resource.close();
					
					//Sleep for the lock time amount (in microseconds it seems)
					//Update, give us 2 minutes to refresh the lock
					sleep((RELOCK_WAIT) * 1000);
			
					//If user has explicitly removed the lock, its name will not appear in lockedFiles,
					// so there is no need to refresh the lock
					if (lockedFiles.indexOf(filePath) == -1)
					    break;
				} else {
					JOptionPane.showMessageDialog(null, "File [" + filePath + "] not found on WebDAV repository.",
								"ERROR", JOptionPane.ERROR_MESSAGE);
					break;
				}
			} catch (HttpException he) {
			    //				JOptionPane.showMessageDialog(null, "Error: " + he.getMessage());
			    //	Log.log(Log.ERROR, LockManager.class, "Error: httpexception: " + resource.getStatusMessage());
			} catch (Exception e) {
			} catch (Throwable e) {
			    //				Log.log(Log.DEBUG, LockManager.class, "Error: " + e.getMessage());
			}
		}
	} //}}}
	
	//{{{ unlockFile() method
	/** Calls the unlock method on the given file path name
	 * Prints appropriate error messages if:
	 *  - Lock could not be removed
	 *  - File is not locked
	 *  - File is not found
	 */
	public static boolean unlockFile(String path){
		/* ConnectionManager.ConnectionInfo info =  
		    ConnectionManager.getConnectionInfo((Component)null, new DavAddress( path ) );
		*/
		WebdavResource resource = ConnectionManager.getResource(path);
			
		try
		{
			// Check for locks
			LockDiscoveryProperty lockDiscoveryProperty = 
				resource.lockDiscoveryPropertyFindMethod(path);
			
			if ( lockDiscoveryProperty != null ){
				Lock[] locks = lockDiscoveryProperty.getActiveLocks();
				
				//Remove any existing locks
				if ( locks != null && locks.length > 0 ){
					if (!resource.unlockMethod()){
						JOptionPane.showMessageDialog(null, "Lock on " + path + " could not be removed\n" +
							"See activity log for details",
							"ERROR", 
							JOptionPane.ERROR_MESSAGE);
						Log.log(Log.DEBUG, LockManager.class, "Lock failed: " + resource.getStatusMessage());
						return false;
					}
					Log.log(Log.DEBUG, LockManager.class, "Lock removed on " + path);
				} else {
					Log.log(Log.DEBUG, LockManager.class, "File [" + path + "] is not currently locked on WebDAV Repository.");
					return true;
				}
			} else {
				JOptionPane.showMessageDialog(null, "File [" + path + "] not found on WebDAV Repository.");
				return true;
			}
		} catch (HttpException we) {
		    //JOptionPane.showMessageDialog(null, "UNLOCK warning:\n" + we.getMessage());
			//			Log.log(Log.ERROR, LockManager.class, "Error: httpexception: " + resource.getStatusMessage());
                } catch (IOException e) {
		} catch (Throwable t) {
		    }
		return true;
	} //}}}
	
	//{{{ unlockAllFiles() method
	/** Unlocks all files that have been opened with our plugin
	 */
	public static void unlockAllFiles(){
		for( int i = 0; i < lockedFiles.size(); i++ ){
			unlockFile( (String)lockedFiles.elementAt(i) );
		}
	} //}}}
}
