/*
 */
package gruntspud;

import gruntspud.event.GruntspudCVSAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.event.BinaryMessageEvent;
import org.netbeans.lib.cvsclient.event.FileToRemoveEvent;
import org.netbeans.lib.cvsclient.event.MessageEvent;

/**
 * @author magicthize
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class FileGruntspudCVSListener extends GruntspudCVSAdapter {
    private FileOutputStream out;
    private File file;
    private GruntspudContext context;
    private PrintWriter writer;
    private StringBuffer taggedLine = new StringBuffer();

    public FileGruntspudCVSListener(GruntspudContext context) {
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.event.GruntspudCVSListener#getShortName()
     */
    public String getShortName() {
        return "File";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.lib.cvsclient.event.CVSListener#messageSent(org.netbeans.lib.cvsclient.event.MessageEvent)
     */
    public void messageSent(MessageEvent evt) {
        String line = evt.getMessage();
        String mesg = evt.isTagged() ? evt.parseTaggedMessage(taggedLine, evt.getMessage()) : line;
        if (writer != null && mesg != null && mesg.length() > 0) {
            if (!evt.isError()) {
                writer.println(mesg);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.event.GruntspudCVSListener#isOptionalListener()
     */
    public boolean isOptionalListener() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.event.GruntspudCVSListener#commandUnitFinished()
     */
    public void commandUnitFinished() {
        if (writer != null) {
            writer.flush();
        }
        GruntspudUtil.closeStream(out);
        out = null;
        file = null;
        writer = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.event.GruntspudCVSListener#commandUnitStarted(org.netbeans.lib.cvsclient.command.Command)
     */
    public void commandUnitStarted(Command command) {
        File f = new File(context.getHost()
                        .getProperty(Constants.LAST_SAVE_TO_FILE,
                                     System.getProperty("user.home") + File.separator + "cvs-console-output.log"));
        JFileChooser chooser = new JFileChooser(f.getParent());
        chooser.setSelectedFile(f);
        chooser.setDialogTitle("Choose file to save output to");
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
            if (file.exists()
                            && JOptionPane.showConfirmDialog(null, "File exists, are you sure?", "Confirm overwrite",
                                                             JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
                return;
            }
            context.getHost().setProperty(Constants.LAST_SAVE_TO_FILE, file.getAbsolutePath());
            try {
                out = new FileOutputStream(file);
                writer = new PrintWriter(out);
                taggedLine.setLength(0);
            } catch (IOException ioe) {
                Constants.IO_LOG.error(ioe);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gruntspud.event.GruntspudCVSListener#isSelectedByDefault()
     */
    public boolean isSelectedByDefault() {
        return false;
    }
}
