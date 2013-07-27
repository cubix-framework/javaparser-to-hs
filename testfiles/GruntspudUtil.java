/*
 *  Gruntspud
 *
 *  Copyright (C) 2002 Brett Smith.
 *
 *  Written by: Brett Smith <t_magicthize@users.sourceforge.net>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package gruntspud;

import gruntspud.ui.MultilineLabel;

import java.awt.Component;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.swing.JOptionPane;

import org.netbeans.lib.cvsclient.connection.AuthenticationException;

/**
 *  Description of the Class
 *
 *@author     magicthize
 *@created    26 May 2002
 */
public class GruntspudUtil {
    private final static NumberFormat FILE_SIZE_FORMAT = new DecimalFormat();

    public final static File PREFERENCES_DIRECTORY = new File(
        System.getProperty("user.home") + File.separator + ".gruntspud");


    static {
        FILE_SIZE_FORMAT.setMinimumFractionDigits(1);
        FILE_SIZE_FORMAT.setMaximumFractionDigits(1);
    }

    private static char[] XML_SPECIAL_CHARACTERS = { '&', '<', '>', '\'' };
    private static String[] XML_REPLACEMENT_CHACTERS = { "&", "<", ">", "&apos;" };

    public static String encodeXML(String text) {
        StringBuffer buffer = new StringBuffer();
        boolean replaced;
        for( int i = 0; i < text.length(); i++ ) {
            char ch = text.charAt(i);
            replaced = false;
            for( int k = 0; k < XML_SPECIAL_CHARACTERS.length && !replaced; k++ ) {
                if( ch == XML_SPECIAL_CHARACTERS[k] ) {
                     buffer.append(XML_REPLACEMENT_CHACTERS[k]);
                     replaced = true;
                }
            }
            if(!replaced)
                buffer.append(ch);
        }
        return buffer.toString();
    }

    /**
     * Show an error dialog given an exception
     *
     * @param component parent component
     * @param title
     * @param execption exception
     */
    public static void showErrorMessage(Component parent, String title,
                                        Throwable exception) {
        showErrorMessage(parent, null, title, exception);
    }

    /**
     * Show an error dialog given an exception
     *
     * @param component parent component
     * @param mesg option message
     * @param title title
     * @param execption exception
     */
    public static void showErrorMessage(Component parent,
                                        String mesg, String title,
                                        Throwable exception) {
        boolean details = false;
        while (true) {
            String[] opts = new String[] {
                details ? "Hide Details" : "Details", "Ok"};
            StringBuffer buf = new StringBuffer();
            if (mesg != null) {
                buf.append(mesg);
            }
            appendException(exception, 0, buf, details);
            MultilineLabel message = new MultilineLabel(buf.toString());
            int opt = JOptionPane.showOptionDialog(
                parent, message, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.ERROR_MESSAGE, null, opts, opts[1]);
            if (opt == 0) {
                details = !details;
            }
            else {
                break;
            }
        }
    }

    private static void appendException(Throwable exception, int level,
                                        StringBuffer buf, boolean details) {
        if (exception.getMessage() != null &&
            exception.getMessage().length() > 0) {
            if(details && level > 0)
                buf.append("\n \nCaused by ...\n");
            buf.append(exception.getMessage());
        }
        if (details) {
            if (exception.getMessage() == null ||
                exception.getMessage().length() == 0) {
                buf.append("\n \nCaused by ...");
            }
            else {
                buf.append("\n \n");
            }
            StringWriter sw = new StringWriter();
            exception.printStackTrace(new PrintWriter(sw));
            buf.append(sw.toString());
        }
        if (exception instanceof GruntspudException &&
            ( (GruntspudException) exception).getRootCause() != null) {
            appendException( ( (GruntspudException) exception).getRootCause(),
                            level + 1, buf, details);
        }
        else if (exception instanceof AuthenticationException &&
                 ( (AuthenticationException) exception).getUnderlyingThrowable() != null) {
            buf.append(" \nCaused by ...\n \n");
            appendException(
                ( (AuthenticationException) exception).getUnderlyingThrowable(),
                level + 1, buf, details);
        }
        else {
            try {
                java.lang.reflect.Method method =
                    exception.getClass().getMethod("getCause", new Class[] {});
                Throwable cause = (Throwable) method.invoke(exception, null);
                if (cause != null) {
                    appendException(cause, level + 1, buf, details);
                }
            }
            catch (Exception e) {
                Constants.SYSTEM_LOG.warn(
                    "Cause of exception could not be determined");
            }
        }
    }

    /**
     * Copy the input from one stream to the output of another until EOF. It
     * is up to the invoker to close the streams.
     *
     * @param in input stream
     * @param out output stream
     * @param buf buffer size (-1 means don't buffer)
     */
    public static void copyStreams(InputStream in, OutputStream out, int buf) throws
        IOException {
        copyStreams(in, out, buf, -1);
    }

    /**
     * Copy the input from one stream to the output of another for a number of
     * bytes (-1 means until EOF)
     *
     * @param in input stream
     * @param out output stream
     * @param buf buffer size (-1 means don't buffer)
     * @param bytes bytes to copy
     */
    public static void copyStreams(InputStream in, OutputStream out, int buf,
                                   long bytes) throws IOException {
        InputStream bin = (buf == -1) ? in : new BufferedInputStream(in, buf);
        OutputStream bout = (buf == -1) ? out : new BufferedOutputStream(out,
            buf);
        byte[] b = null;
        int r = 0;

        while (true && ( (bytes == -1) || (r >= bytes))) {
            int a = bin.available();

            if (a == -1) {
                break;
            }
            else if (a == 0) {
                a = 1;

            }
            if ( (bytes != -1) && ( (r + a) > bytes)) {
                a -= ( (r + a) - bytes);

            }
            b = new byte[a];
            a = bin.read(b);

            if (a == -1) {
                break;
            }

            r += a;
            bout.write(b, 0, a);
        }

        bout.flush();
    }

    /**
     *  Gets the selectedGlobalCVSRoot attribute of the GruntspudUtil class
     *
     *@param  host  Description of the Parameter
     *@return       The selectedGlobalCVSRoot value
     */
    public static CVSRoot getSelectedGlobalCVSRoot(GruntspudContext context) {
        StringTokenizer s = new StringTokenizer(context.getHost().getProperty(
            Constants.OPTIONS_CONNECTION_ROOT_LIST,
            "|"), "");

        while (s.hasMoreTokens()) {
            boolean sel = false;
            String t = s.nextToken();

            if (t.startsWith("&")) {
                t = t.substring(1);
                sel = true;
            }

            if (sel) {
                try {
                    return new CVSRoot(t);
                }
                catch (IllegalArgumentException iae) {
                }
            }
        }

        return null;
    }

    public static File getPreferenceFile(String name, boolean create) {
        File d = new File(PREFERENCES_DIRECTORY, name);
        if(!PREFERENCES_DIRECTORY.exists() && !PREFERENCES_DIRECTORY.mkdirs()) {
            Constants.SYSTEM_LOG.error("Could not create preferences directory.");
            return null;
        }
        else {
            if(!d.exists() && create) {
                FileOutputStream out = null;
                Constants.SYSTEM_LOG.info("Creating preferences file " + d.getAbsolutePath());
                try {
                    out = new FileOutputStream(d);
                }
                catch(IOException ioe) {
                    Constants.SYSTEM_LOG.error("Failed to create preferences file "  + d.getAbsolutePath(), ioe);
                    return null;
                }
                finally {
                    GruntspudUtil.closeStream(out);
                }
            }
            return d;
        }
    }

    /**
     * Create a fake stack trace
     */
    public static void stackTrace() {
        try {
            throw new Exception("STACK TRACE");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Close an input stream and don't worry about any exceptions, but return
     * true or false instead. If <code>null</code> is supplied as they stream
     * then it is just ignored
     *
     * @param stream stream to close
     * @return closed ok
     */
    public static boolean closeStream(InputStream in) {
        try {
            if (in != null) {
                in.close();

            }
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Close an output stream and don't worry about any exceptions, but return
     * true or false instead. If <code>null</code> is supplied as they stream
     * then it is just ignored
     *
     * @param stream stream to close
     * @return closed ok
     */
    public static boolean closeStream(OutputStream out) {
        try {
            if (out != null) {
                out.close();

            }
            return true;
        }
        catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Format a date to GMT
     */
    public static String formatDate(Date date, GruntspudContext context) {
        DateFormat format = new SimpleDateFormat(StringListModel.
            getSelectedItemForStringListPropertyString(
            context.getHost().getProperty(Constants.OPTIONS_DISPLAY_DATE_FORMAT,
                                          "&" + Constants.DEFAULT_DATE_FORMAT)));
        format.setTimeZone(TimeZone.getTimeZone("GMT+0000"));

        return format.format(date);
    }

    /**
     * Delete a directory recursively or delete a single file
     *  
     * @param file to delete
     * @return true if sucessfully deleted
     */
    public static boolean delTree(File file)
    {
        if(file.exists()) {
            if(file.isDirectory()) {
                File[] files = file.listFiles();
                for(int i = 0;files != null && i < files.length;i++) {
                    if(!delTree(files[i])) {
                        return false;
                    }
                }
            }
            if(!file.delete()) {
                return false;
            }
            else {
                if(file.exists()) {
                    return false;
                }
            }
            return true;
        }
        else {
            return false;
        }
    }

    /**
     *
     */
    public static String formatFileSize(long size) {
        double bytes = (double) size;

        if (bytes < 1024) {
            return String.valueOf( (int) bytes) + " bytes";
        }
        else if (bytes < 1048576) {
            return FILE_SIZE_FORMAT.format(bytes / 1024) + " K";
        }
        else {

            return FILE_SIZE_FORMAT.format(bytes / 1024 / 1024) + " MB";
        }
    }
}
