/*
 * open_file.java - display a file selection box and open a file in InfoViewer
 * Copyright (C) 1999-2001 Dirk Moebius
 *
 * :tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
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

package infoviewer.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.io.FileVFS;
import org.gjt.sp.jedit.io.VFSManager;


public class open_file extends InfoViewerAction
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 8244129956268231628L;


	public open_file()
    {
        super("infoviewer.open_file");
    }


    public void actionPerformed(ActionEvent evt)
    {
        if (lastfile == null)
        {
            lastfile = jEdit.getProperty("infoviewer.lastfile");
            if (lastfile == null)
                lastfile = jEdit.getJEditHome();
        }

        Frame frame = getFrame(evt);
        View view = jEdit.getFirstView();
        if(frame != null && frame instanceof View)
            view = (View) frame;

        String files[] = GUIUtilities.showVFSFileDialog(
            view, lastfile, JFileChooser.OPEN_DIALOG, false);

        if (files == null || files.length == 0)
            return; // no file chosen

        if (VFSManager.getVFSForPath(files[0]) instanceof FileVFS)
            lastfile = "file:" + files[0];
        else
            lastfile = files[0];

        jEdit.setProperty("infoviewer.lastfile", lastfile);
        getViewer(evt).gotoURL(lastfile, true, -1);
    }


    private static String lastfile = null;

}

