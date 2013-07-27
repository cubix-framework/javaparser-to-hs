/**
 * Utils.java
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2006 Jakub Roztocil
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

package sidekick.css;

//{{{ Imports
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.browser.VFSBrowser;
import java.io.*; 
//}}}


public class Utils {
	
	//{{{ insertRelativePathToBuffer() method
	public static void insertRelativePathToBuffer() {
		Buffer buffer = jEdit.getActiveView().getBuffer();
		buffer.insert(jEdit.getActiveView().getTextArea().getCaretPosition(),
						chooseFileAndGetRelativePath());
	} //}}}
	
	//{{{ chooseFileAndGetRelativePath() method
	public static String chooseFileAndGetRelativePath() {
		String relPath = "";
		Buffer buffer = (Buffer)jEdit.getActiveView().getBuffer();
		String from = MiscUtilities.getParentOfPath(buffer.getPath());
		String[] paths = GUIUtilities.showVFSFileDialog(jEdit.getActiveView(),
															from,
															VFSBrowser.OPEN_DIALOG,
															false);
		if (paths != null) {
			String to = paths[0];
			if (buffer.isUntitled()) {
				return paths[0];
			}
			from = from.substring(0, from.length() - 1);
			relPath = getRelativePath(from, to);
		}
		
		return relPath;
	} //}}}
	
	//{{{ getRelativePath() method
	public static String getRelativePath(String fromDir, String toFile) {
		/* Based on part of macro Browse_and_link.bsh, 
		thanks to authors Pavel Stetina and Jean-Francois Magni. */
		String separator;
		
		// File separator for use in regexp
		if (File.separator.equals("\\")) {
			separator = "\\\\";
		} else {
			separator = File.separator;
		}
		String[] dirsBuffer = fromDir.split(separator);
		String[] dirsFile = MiscUtilities.getParentOfPath(toFile).split(separator);
		int dirLevels = Math.min(dirsBuffer.length, dirsFile.length);
		int sameDirs = 0;
		int i;
		for (i=0; i < dirLevels; i++) {
			if (dirsBuffer[i].equals(dirsFile[i])) {
				sameDirs++;
			}
		}
		// From now href contains new relative URL
		String href = "";
		// Backward in directories tree
		for (i=sameDirs; i < dirsBuffer.length; i++) {
			href += "../";
		}
		// Forward in directories tree
		for (i=sameDirs; i < dirsFile.length; i++) {
			href += dirsFile[i] + "/";
		}
		href += MiscUtilities.getFileName(toFile);
		return href;
	} //}}}

}
