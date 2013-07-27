/*
 * jEdit - Programmer's Text Editor
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright © 2011 Matthieu Casanova
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.kpouer.jedit.fxbrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.web.WebView;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.util.Log;

/**
 * @author Matthieu Casanova
 */
public class FXBrowserPanel extends JPanel
{

	private final HistoryTextField addressTextfield;
	WebView webView;

	public FXBrowserPanel()
	{
		super(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		addressTextfield = new HistoryTextField("fxbrowser.address");
		addressTextfield.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				browse(addressTextfield.getText());
			}
		});
		topPanel.add(new JLabel("Go to:"), BorderLayout.WEST);
		topPanel.add(addressTextfield);
		add(topPanel, BorderLayout.NORTH);
		final JFXPanel fxPanel = new JFXPanel();
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				webView = new WebView();
				final VBox layout = VBoxBuilder.create().spacing(10).children(webView).build();
//				layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");

				// display the scene.
				final Scene scene = new Scene(layout);
				fxPanel.setScene(scene);
			}
		});
		add(fxPanel);
	}
	
	private void browse(final String url)
	{
		Log.log(Log.MESSAGE, this, "Browse "+url);
		Platform.runLater(new Runnable()
		{
			@Override
			public void run()
			{
				String _url = url;
				if (!_url.startsWith("http://"))
					_url = "http://" + _url;
				webView.getEngine().load(_url);
			}
		});
	}

	public static void main(String[] args)
	{
		JFrame f = new JFrame();
		f.setContentPane(new FXBrowserPanel());
		f.pack();
		f.setVisible(true);
	}
}
