/*
 * HexToolsPlugin
 * Copyright (C) 2010 Jürgen Weber
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

package de.jwi.jedit.hextools;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;

/**
 * @author Juergen Weber (WJ3369) created 05.12.2010
 * 
 */
public class HexToolsPlugin extends EditPlugin
{
	private static final String HEX_TOOLS_PLUGIN_SAVE_AS_FOLDER = "HexToolsPlugin.SaveAsFolder";

	public static void parseHex(View view, JEditTextArea textArea)
	{
		Selection[] selections = textArea.getSelection();

		if (selections.length == 0)
		{
			textArea.getToolkit().beep();
			return;
		}

		byte[] b = null;
		
		try
		{
			b = parseHexBytes(textArea, selections);
		}
		catch (NumberFormatException e1)
		{
			textArea.getToolkit().beep();
			return;
		}
		
		if (b.length == 0)
		{
			textArea.getToolkit().beep();
			return;
		}

		String encoding = textArea.getBuffer().getStringProperty(JEditBuffer.ENCODING);
		
		HexDialog hexDialog = new HexDialog(view, selections.length, b.length, encoding);

		if (hexDialog.isOKClosed)
		{
			String s = hexDialog.radioChoice;
			

			if (HexDialog.CLIP.equals(s))
			{
				String hs = null;
				try
				{
					hs = new String(b, hexDialog.encoding);
					if (hexDialog.nonPrintingAsDot)
					{
						hs = hs.replaceAll("[^\\p{Print}]", ".");
					}
				}
				catch (UnsupportedEncodingException e)
				{
					throw new RuntimeException(e);
				}
				Registers.getRegister('$').setValue(hs);
			}
			else if (HexDialog.SAVE.equals(s))
			{
				save(view, b);
			}
		}
	}

	private static byte[] parseHexBytes(JEditTextArea textArea,
			Selection[] selections) throws NumberFormatException
	{
		String s = textArea.getSelectedText(" ");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		s = s.replaceAll("\\s", "");
		System.out.println("\"" + s + "\"");
		int n = s.length();
		for (int i = 0; i < n - 1;)
		{
			String s1 = s.substring(i, i + 2);
			byte b = (byte) Integer.parseInt(s1, 16);
			bos.write(b);
			i += 2;
		}

		return bos.toByteArray();
	}

	private static void save(View view, byte[] b)
	{
		String dir = jEdit.getProperty(HEX_TOOLS_PLUGIN_SAVE_AS_FOLDER);
		dir = dir == null ? "" : dir;
		
		VFSFileChooserDialog vfsFileChooserDialog = new VFSFileChooserDialog(
				view, dir, VFSBrowser.SAVE_DIALOG, false, true);

		String[] selectedFiles = vfsFileChooserDialog.getSelectedFiles();

		if (selectedFiles != null && selectedFiles.length == 1)
		{
			dir = selectedFiles[0];
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(dir);
				fos.write(b);
				fos.close();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			jEdit.setProperty(HEX_TOOLS_PLUGIN_SAVE_AS_FOLDER,dir);
		}
	}

}
