package clearcase;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

import console.Console;
import console.Output;
import console.Shell;

/**
 */
public class ClearCaseShell extends Shell implements ClearCaseConstants
{
    public static String VARIABLE_CURRENT_BUFFER = "$current";

    public static String [] VARIABLES = {VARIABLE_CURRENT_BUFFER};

    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    
    InteractiveProcess clearTool;
    
    public ClearCaseShell()
    {
        super("ClearTool");
    }

    public void printInfoMessage(Output output)
    {
        output.print(null, "ClearTool shell. Type help for a list of ClearTool commands. \n\nVariables\n\t$current = Path to current buffer\n");
    }

    public InteractiveProcess getClearTool(Console console) throws IOException, InterruptedException
    {
        if(clearTool == null)
        {
            init(console);
        }

        return clearTool;
    }
    
    public void init(Console console) throws IOException, InterruptedException
    {
        if(clearTool == null)
        {
            OutputStream input = new ConsoleOutputStream( console.getInfoColor(), console );
            OutputStream error = new ConsoleOutputStream( console.getErrorColor(), console );
            
            String path = jEdit.getProperty("clearcase.path");
            Log.log(Log.DEBUG, this, "clearcase.path = " + path);
            
            // Tidy path
            if(path != null && path.length() > 0)
            {
            	    String cmd = COMMAND_CLEARTOOL;
            	    if (OperatingSystem.isWindows())
			cmd = cmd + ".exe";
				
		    File file = new File(path, cmd);
		    path = file.getCanonicalPath();
		    // Check if ClearTool can be found.
		    if(!file.exists()) 
		    	Log.log(Log.ERROR, this, "Cleartool not found: " + path);		    
            }
            else
            {
		// Tell user to set ClearTool path or verify that it is in the PATH.
                if(jEdit.getProperty("clearcase.firstTime") == null)
                {
                    jEdit.setProperty("clearcase.firstTime", "true");
                    JOptionPane.showMessageDialog(
                        null, 
                        "ClearTool path is not set. " + 
                        "\n\nGoto 'Global Options' and set the folder where " + 
                        "ClearTool is located." + 
                        "\n\nOr verify that ClearTool is in the system path.",
                        "ClearCase executed for the first time", 
                        JOptionPane.ERROR_MESSAGE
                        );
                }
                path = "cleartool";
            }
            clearTool = new InteractiveProcess(path, input, error);
            
            // Start process.
            clearTool.start();
        }
    }
    
    /**
    *   Commands from the shell are just sent to ClearTool.
    *   But before that some special variables are replaced.
    *   @see
    */
    public void execute(Console console, String input, Output output, Output error, String command)
    {
		try
		{
            // Print out the executed command.
            output.print(console.getInfoColor(), ">" + command);
            
            String cmd = command.trim();
    
            // Start process if it is null or not started.
            //if(clearTool == null || !clearTool.isStarted())
            if(clearTool == null)
            {
                init(console);
            }
            
            cmd = replaceVariables(cmd, console);
            
            Log.log(Log.DEBUG, this, "Executing ClearTool command: " + cmd);
            
            // Exec command
            clearTool.write(cmd + LINE_SEPARATOR);
		}
		catch(IOException ex)
		{
			console.print(console.getErrorColor(), ex.getMessage());
			ex.printStackTrace();
            // Process error try again next time
            clearTool = null;
		}
		catch(InterruptedException ex)
		{
			console.print(console.getErrorColor(), ex.getMessage());
			ex.printStackTrace();
            // Process error try again next time
            clearTool = null;
		}
        finally
        {
            console.commandDone();
        }
    }

    /**
    * 
    */
    public void stop(Console console)
    {
        try
        {
            if(clearTool != null)
            {
                clearTool.stop();
            }
            
            clearTool = null;
            
            init(console);
		}
		catch(IOException ex)
		{
			console.print(console.getErrorColor(), ex.getMessage());
			ex.printStackTrace();
		}
		catch(InterruptedException ex)
		{
			console.print(console.getErrorColor(), ex.getMessage());
			ex.printStackTrace();
		}
    }

    public boolean waitFor(Console console)
    {
        return true;
    }
    
    // ========================================
    // Utility methods
    // ========================================
    
    public static String replaceVariables(String text, Console console)
    {
        // This will be the new text where variables have been replaced.
        StringBuffer buffer = new StringBuffer(text.length() + 1024);
        buffer.append(text);
        
        String currentFile = console.getView().getBuffer().getPath();
        
        // Replace all variables.
        for(int i = 0; i < VARIABLES.length; i++)
        {
            String variable = VARIABLES[i];
            int textIndex = text.indexOf(variable);
            // Text found
            if(textIndex != -1)
            {
                // Replace with path to current buffer
                if(variable.equals(VARIABLE_CURRENT_BUFFER))
                {
                    buffer.replace(textIndex, textIndex + variable.length(), currentFile);
                }
                else
                {
                    Log.log(Log.ERROR, console, "Unknown variable " + variable);
                }
            }
        }
        return buffer.toString();
    }

}
