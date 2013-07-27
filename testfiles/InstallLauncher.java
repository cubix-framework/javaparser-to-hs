/*
 * InstallLauncher - Launches the installer to installed the updated version
 *
 * Copyright (C) 2009 Shlomy Reinstein
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

package updater;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class InstallLauncher
{
	// Control commands provided via standard input
	public static final String LAUNCH_INSTALLER_NOW = "Launch Installer Now";
	public static final String ASK_FOR_CONFIRMATION = "Ask For Confirmation";
	public static final String END_INSTALLER_PARAMS = "End Of sInstaller Parameters";
	public static final String END_EXECUTION = "Exit Now";
	public static final String EXECUTION_ABORTED = "Execution Aborted";
	public static final String SILENT_SHUTDOWN = "Shutdown Silently";
	public static final String PROGRESS_INDICATOR = "*** Progress: ";

	// Control commands sent via standard output
	public static final String ABORT = "Abort";
	public static final String REJECTED = "Rejected";
	public static final String CONFIRMED = "Confirmed";
	public static final String AUTO_CONFIRM = "Auto Confirm";

	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

	private static JFrame window;
	private static JTextArea text;
	private static JPanel buttonPanel;
	private static JButton ok;
	private static JButton cancel;
	private static JButton alwaysYes;
	private static int pos;
	private static Properties props;
	private static FileWriter logWriter;
	private static boolean awaitingConfirmation; 
	private static OutputStreamWriter out;
	private static ActionListener buttonActionListener;

	public static void main(String [] args)
	{
		String logFile = (args.length > 0) ? args[0] : null;
		if (logFile != null)
			startLogging(logFile);
		props = new Properties();
		try
		{
			InputStream in = InstallLauncher.class.getResourceAsStream(
				"/Updater.props");
			props.load(in);
			in.close();
		}
		catch(IOException io)
		{
			log("Error reading 'Updater.props': " + io.getMessage());
			System.err.println("Error reading 'Updater.props':");
			io.printStackTrace();
		}
		window = new JFrame(props.getProperty("updater.msg.updateDialogTitle"));
		window.setLayout(new BorderLayout());
		text = new JTextArea(8, 80);
		text.setEditable(false);
		window.add(new JScrollPane(text), BorderLayout.CENTER);
		buttonPanel = new JPanel();
		ok = new JButton(props.getProperty("updater.msg.updateDialogCloseButton"));
		buttonPanel.add(ok);
		cancel = new JButton(props.getProperty("updater.msg.updateDialogCancelButton"));
		buttonPanel.add(cancel);
		// This button is only added when a confirmation is requested
		alwaysYes = new JButton(props.getProperty("updater.msg.autoConfirmUpdate"));

		window.add(buttonPanel, BorderLayout.SOUTH);
		buttonActionListener = new UpdateActionListener();
		ok.addActionListener(buttonActionListener);
		ok.setEnabled(false);
		cancel.addActionListener(buttonActionListener);
		alwaysYes.addActionListener(buttonActionListener);
		window.pack();
		window.setLocationRelativeTo(null);
		window.setVisible(true);

		// Create a writer for stdout (for cancellation and confirmation)
		out = new OutputStreamWriter(System.out);

		// Show messages from jEdit until told to launch installer
		Vector<String> params = processInputStream();
		if (params == null)
		{
			endLogging();
			return;
		}
		// Must have at least installer file. In case of interactive install,
		// no more parameters are specified.
		if (params.size() == 0)
		{
			checkNullInput(null);	// ... abort with "unknown problem" message
			return;
		}
		cancel.setEnabled(false);
		appendText(props.getProperty("updater.msg.waitForInstall"));
		runInstaller(params);
		text.append(props.getProperty("updater.msg.installerDone"));
		ok.setEnabled(true);
		OutputStreamWriter bout = new OutputStreamWriter(System.out);
		try
		{
			bout.close();
		}
		catch (IOException e1)
		{
		}
		endLogging();
	}

	private static Vector<String> processInputStream()
	{
		Vector<String> params = new Vector<String>();
		String line;
		InputLineReader in = new InputLineReader(System.in);
		try
		{
			while ((line = in.readLine()) != null)
			{
				if (line.equals(SILENT_SHUTDOWN) ||
					line.equals(EXECUTION_ABORTED))
				{
					window.setVisible(false);
					window.dispose();
					return null;
				}
				if (line.equals(END_EXECUTION))
				{
					ok.setEnabled(true);
					cancel.setEnabled(false);
					return null;
				}
				if (line.equals(ASK_FOR_CONFIRMATION))
				{
					requestConfirmation();
					continue;
				}
				if (line.startsWith(PROGRESS_INDICATOR))
					appendProgress(line.substring(PROGRESS_INDICATOR.length()));
				else if (line.equals(LAUNCH_INSTALLER_NOW))
					break;
				else
					appendText(line);
			}
			if (checkNullInput(line))
				return null;
			// Now get the installer parameters (installer file, install type,
			// install dir).
			while ((line = in.readLine()) != null)
			{
				if (line.equals(END_INSTALLER_PARAMS))
					break;
				params.add(line);
			}
			in.close();
		}
		catch (IOException e1)
		{
		}
		return params;
	}

	private static void requestConfirmation()
	{
		ok.setText(props.getProperty("updater.msg.confirmUpdate"));
		cancel.setText(props.getProperty("updater.msg.rejectUpdate"));
		buttonPanel.add(alwaysYes);
		buttonPanel.revalidate();
		buttonPanel.repaint();
		ok.setEnabled(true);
		cancel.setEnabled(true);
		awaitingConfirmation = true;
	}

	private static void endConfirmation(boolean confirmed, String reply)
	{
		ok.setText(props.getProperty("updater.msg.updateDialogCloseButton"));
		cancel.setText(props.getProperty("updater.msg.updateDialogCancelButton"));
		buttonPanel.remove(alwaysYes);
		buttonPanel.revalidate();
		buttonPanel.repaint();
		writeOutput(reply);
		if (confirmed)
			ok.setEnabled(false);
		awaitingConfirmation = false;
	}

	private static void runInstaller(Vector<String> params)
	{
		String [] installerArgs = new String[2 + params.size()];
		installerArgs[0] = "java";
		installerArgs[1] = "-jar";
		for (int i = 0; i < params.size(); i++)
			installerArgs[i + 2] = params.get(i);
		StringBuilder sb = new StringBuilder();
		for (String s: installerArgs)
		{
			if (sb.length() > 0)
				sb.append(" ");
			if (s.matches(".*\\S\\s+\\S.*"))
				sb.append("\"" + s + "\"");
			else
				sb.append(s);
		}
		log("Running: " + sb.toString() + "\n");
		try {
			Process p = Runtime.getRuntime().exec(installerArgs);
			StreamConsumer osc = new StreamConsumer(p.getInputStream());
			osc.start();
			StreamConsumer esc = new StreamConsumer(p.getErrorStream());
			esc.start();
			p.waitFor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void startLogging(String logFile)
	{
		try
		{
			logWriter = new FileWriter(logFile, true);
			logWriter.write(">>> Started: " + now() + "\n");
			logWriter.flush();
		}
		catch (IOException e2)
		{
			logWriter = null;
		}
	}

	private static void endLogging()
	{
		if (logWriter == null)
			return;
		try
		{
			logWriter.write("<<< Ended: " + now() + "\n");
			logWriter.flush();
			logWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void log(String s)
	{
		if (logWriter == null)
			return;
		try
		{
			logWriter.write("[" + now() + "] " + s);
			logWriter.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static String now()
	{
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());
	}

	private static boolean checkNullInput(String line)
	{
		if (line != null)
			return false;
		appendText("\n" + props.getProperty("updater.msg.updateErrorAbort"));
		ok.setEnabled(true);
		return true;
	}

	private static void appendProgress(final String s)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				text.replaceRange(s, pos, text.getText().length());
			}
		});
	}

	private static void appendText(final String s)
	{
		final String sOut = s.endsWith("\n") ? s : s + "\n";
		log(sOut);
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				text.append(sOut);
				text.setCaretPosition(text.getText().length());
				pos = text.getCaretPosition();
			}
		});
	}

	private static boolean writeOutput(String s)
	{
		try
		{
			out.write(s);
			if (! s.endsWith("\n"))
				out.write("\n");
			out.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static final class UpdateActionListener implements ActionListener
	{
		private void okAction()
		{
			if (awaitingConfirmation)
				endConfirmation(true, CONFIRMED);
			else
				window.dispose();
		}

		// Only for confirmations
		private void alwaysYesAction()
		{
			endConfirmation(true, AUTO_CONFIRM);
		}

		private void cancelAction()
		{
			if (awaitingConfirmation)
				endConfirmation(false, REJECTED);
			else
			{
				writeOutput(ABORT);
				appendText("\n" + props.getProperty("updater.msg.abortingUpdate"));
			}
		}

		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == ok)
				okAction();
			else if (e.getSource() == alwaysYes)
				alwaysYesAction();
			else
				cancelAction();
		}
	}

	private static class StreamConsumer extends Thread
	{
		private InputStream is;
		public StreamConsumer(InputStream is) {
			this.is = is;
		}
		public void run() {
			try {
				BufferedReader br = new BufferedReader(
					new InputStreamReader(is));
				String line;
				while ((line = br.readLine()) != null)
					appendText(line + "\n");
			} catch (IOException ioe) {
				ioe.printStackTrace();  
			}
		}
	}
}
