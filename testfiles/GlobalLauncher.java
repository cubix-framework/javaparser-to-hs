/*
Copyright (C) 2007  Shlomy Reinstein

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

package browser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JOptionPane;

import options.GlobalOptionPane;

import org.gjt.sp.jedit.jEdit;

public class GlobalLauncher {

	static GlobalLauncher instance = new GlobalLauncher();
	
	static public GlobalLauncher instance() {
		return instance;
	}
	
	interface OutputParser {
		// Return true to continue reading output, false to abort.
		boolean parse(String line);
	}
	
	private class OutputCollector implements OutputParser {
		Vector<String> lines;
		public OutputCollector(Vector<String> lines) {
			this.lines = lines;
		}
		public boolean parse(String line) {
			lines.add(line);
			return true;
		}
	}
	
	public boolean run(String options, String workingDirectory)
	{
		return run(options, workingDirectory, null);
	}
	public boolean run(String options, String workingDirectory, boolean wait)
	{
		return run(options, workingDirectory, null, wait);
	}
	public boolean run(String options, String workingDirectory, OutputParser parser)
	{
		return run(options, workingDirectory, parser, false);
	}
	public boolean run(String options, String workingDirectory, OutputParser parser,
		boolean wait)
	{
        String globalPath = jEdit.getProperty(GlobalOptionPane.GLOBAL_PATH_OPTION);
		try {
			String command = globalPath + " " + options;
			File dir = new File(workingDirectory);
			Process p = Runtime.getRuntime().exec(command, null, dir);
			// To ensure the process doesn't hang due to output/error
			// stream buffering (i.e. process blocks because the buffer
			// is full, and there's no consumer), consume these streams
			// in any case.
			new StreamConsumer(p.getErrorStream()).start();
			if (parser != null) {
		        BufferedReader stdInput = new BufferedReader(
		        	new InputStreamReader(p.getInputStream()));
		        // read the output from the command
		        String s;
		        while ((s = stdInput.readLine()) != null) {
		        	if (! parser.parse(s))
		        		break;
		        }
		        stdInput.close();
			} else {
				new StreamConsumer(p.getInputStream()).start();
			}
			if (wait) {
				try {
					p.waitFor();
				} catch (InterruptedException e) {
					return false;
				}
			}
		} catch (Exception e) {
			 JOptionPane.showMessageDialog(null, 
				jEdit.getProperty("messages.GlobalPlugin.cannot_run_global"),
				"Error", JOptionPane.ERROR_MESSAGE);
			 return false;
		}
		return true;
	}

	public Vector<String> runForOutput(String options, String workingDirectory) {
		Vector<String> output = new Vector<String>();
		run(options, workingDirectory, new OutputCollector(output));
		return output;
	}

	public String getDatabasePath(String workingDirectory) {
		Vector<String> output = runForOutput("-p", workingDirectory);
		if (output == null || output.size() == 0)
			return null;
		return output.get(0);
	}
	
	public boolean isFileInDatabase(String file, String workingDirectory) {
		file = file.replace('\\', '/');
		int i = file.lastIndexOf('/');
		String filename = (i > 0) ? file.substring(i + 1) : file;
		Vector<String> output = runForOutput("-P -a " + filename, workingDirectory);
		for (String line: output)
			if (line.equals(file))
				return true;
		return false;
	}
	
	public Vector<GlobalRecord> runRecordQuery(String options, String workingDirectory)
	{
        final Vector<GlobalRecord> records = new Vector<GlobalRecord>();
		OutputParser parser = new OutputParser() {
			public boolean parse(String line) {
				records.add(new GlobalRecord(line));
				return true;
			}
		};
		run(options, workingDirectory, parser);
		return records;
	}
	
	// A dummy class to consume the output and error streams of the
	// GNU Global process, to prevent it from hanging due to OS
	// buffering.
	private class StreamConsumer extends Thread {
		private InputStream is;
		public StreamConsumer(InputStream is) {
			this.is = is;
		}
		public void run() {
	        BufferedReader stdInput = new BufferedReader(
	        	new InputStreamReader(is));
	        try {
				while (stdInput.readLine() != null)
					;
				stdInput.close();
			} catch (IOException e) {
			}
		}
	}
}
