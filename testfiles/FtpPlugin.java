/*
 * FtpPlugin.java - Main class of FTP plugin
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2000, 2003 Slava Pestov
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

package ftp;

//{{{ Imports
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.io.VFSManager;

public class FtpPlugin extends EditPlugin
{
	//{{{ start() method
	public void start()
	{
		ConnectionManager.loadPasswords();
	} //}}}
	
	//{{{ stop() method
	public void stop()
	{
		DirectoryCache.clearAllCachedDirectories();
		ConnectionManager.savePasswords();
	} //}}}

	//{{{ showOpenFTPDialog() method
	public static void showOpenFTPDialog(View view, boolean secure)
	{
		String path = ((FtpVFS)VFSManager.getVFSForProtocol(FtpVFS.getProtocol(secure)))
			.showBrowseDialog(new Object[1],view);
		if(path == null)
			return;
		String[] files = GUIUtilities.showVFSFileDialog(view, path, VFSBrowser.OPEN_DIALOG, true);
		if (files == null)
			return;

		Buffer buffer = null;
		for (int i = 0; i < files.length; i++) {
			Buffer _buffer = jEdit.openFile((View)null, files[i]);
			if (_buffer != null)
				buffer = _buffer;
		}
		if (buffer != null)
			view.setBuffer(buffer);
	} //}}}

	//{{{ showSaveFTPDialog() method
	public static void showSaveFTPDialog(View view, boolean secure)
	{

		String path = ( (FtpVFS)VFSManager.getVFSForProtocol(FtpVFS.getProtocol(secure)) )
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

	//{{{ Private members

	//}}}
}
