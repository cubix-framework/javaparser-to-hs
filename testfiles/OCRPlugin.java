/*
Copyright (C) 2009  Shlomy Reinstein

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ocr;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;


public class OCRPlugin extends EditPlugin
{
	private static final String SRC_IMAGE_FORMAT = "png";
	private static final String DST_IMAGE_FORMAT = "ppm";

	static public OCRPlugin instance()
	{
		return (OCRPlugin) jEdit.getPlugin("ocr.OCRPlugin");
	}

	public void readPicture(View view)
	{
		JFileChooser dlg = new JFileChooser();
		FileFilter filter = new FileNameExtensionFilter("Picture file", "pbm");
		dlg.setFileFilter(filter);
		if (dlg.showOpenDialog(view) != JFileChooser.APPROVE_OPTION)
			return;
		importPictureFile(view, dlg.getSelectedFile());
	}

	private Buffer importPictureFile(View v, File f)
	{
		String text = readPictureFile(f);
		Buffer b = getBufferForImportedText(v);
		if (b == null)
			return null;
		b.insert(0, text);
		return b;
	}

	public void readScreenRect(final View view)
	{
		final JFrame frame = new JFrame(jEdit.getProperty("titles.ocr.preCaptureDialog"));
		frame.setLayout(new BorderLayout());
		frame.add(BorderLayout.CENTER, new JLabel(jEdit.getProperty(
			"messages.ocr.preCaptureDialog")));
		JButton close = new JButton("Close");
		frame.add(BorderLayout.SOUTH, close);
		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});
		frame.setAlwaysOnTop(true);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						captureScreen(view);
					}
				});
			}
		});
	}

	private void captureScreen(final View view) {
		final Rectangle rect = new Rectangle(
			Toolkit.getDefaultToolkit().getScreenSize());
		Robot robot;
		try {
			robot = new Robot();
			final BufferedImage image = robot.createScreenCapture(rect);
			image.flush();
			final JFrame f = new JFrame("Capture - select rectangle to import");
			f.setBounds(rect);
			final JPanel imagePanel = new RectangleSelectionPanel(image,
					rect.getSize(), new RectangleSelectionPanel.SelectionListener() {
					public void rectSelected(Point [] p) {
						final BufferedImage subImage = image.getSubimage(
							p[0].x, p[0].y, p[1].x - p[0].x, p[1].y - p[0].y);
						File imageFile = writeImage(subImage);
						if (imageFile != null)
						{
							importPictureFile(view, imageFile);
							deleteTempFile(imageFile);
						}
						f.dispose();
						view.setVisible(true);
					}
				});
			f.add(imagePanel);
			f.setVisible(true);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	private void deleteTempFile(File f)
	{
		f.delete();
	}

	File writeImage(BufferedImage image)
	{
		File srcImage = null, dstImage = null;
		try {
			srcImage = File.createTempFile("ocr", "." + SRC_IMAGE_FORMAT);
			if (srcImage == null)
				return null;
			ImageIO.write(image, SRC_IMAGE_FORMAT, srcImage);
			dstImage = convertImage(srcImage);
			deleteTempFile(srcImage);
		} catch (IOException e) {
			return null;
		}
		return dstImage;
	}

	private File convertImage(File srcImage)
	{
		File dstImage = null;
		try {
			dstImage = File.createTempFile("ocr", "." + DST_IMAGE_FORMAT);
			String exe = jEdit.getProperty(OptionPane.OPTIONS_OCR_CONVERTER_PATH);
			String [] cmd = { exe, srcImage.getAbsolutePath(), dstImage.getAbsolutePath() };
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return dstImage;
	}

	private Buffer getBufferForImportedText(View view)
	{
		return jEdit.newFile(view);
	}

	private String readPictureFile(File f)
	{
		String path = f.getAbsolutePath();
		String exe = jEdit.getProperty(OptionPane.OPTIONS_OCR_GOCR_PATH);
		String [] cmd = { exe, path };
		try {
			Process p = Runtime.getRuntime().exec(cmd);
			InputStream is = p.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = reader.readLine()) != null)
				sb.append(line + "\n");
			p.waitFor();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
