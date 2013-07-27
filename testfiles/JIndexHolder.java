/*
 * jEdit editor settings:
 * :mode=java:tabSize=4:indentSize=4:noTabs=true:maxLineLen=0:
 *
 * JIndexHolder.java - maintains a singleton instance of a JIndex
 * Copyright (C) 2001 Dirk Moebius
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

package jindex;

import com.microstar.xml.XmlException;
import java.awt.Dialog;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.util.Log;
import org.gjt.sp.util.WorkRequest;


public class JIndexHolder {

    public static final int STATUS_OK = 1;
    public static final int STATUS_NOT_EXISTS = 2;
    public static final int STATUS_LOAD_ERROR = 3;
    public static final int STATUS_INV_FILE = 4;
    public static final int STATUS_NO_SETTINGSDIR = 5;
    public static final int STATUS_NOT_LOADED = 6;
    public static final int STATUS_LOADING = 7;
    public static final int STATUS_IS_CREATING = 8;
    public static final int STATUS_CREATE_ERROR = 9;



    /** Get the singleton instance. */
    public static final JIndexHolder getInstance() {
        return instance;
    }


    /**
     * Get the index.
     * Returns null. if the index is not loaded yet.
     * Use the JIndexListener interface to be notified
     * when the index has been loaded completely.
     */
    public final JIndex getIndex() {
        if (status == STATUS_NOT_EXISTS) {
            // There is no index file: show informational message and
            // open JIndex creation dialog afterwards
            GUIUtilities.message(null, "jindex.createinfo", null);
            new ConfigureDialog(jEdit.getFirstView());
        } else if (status == STATUS_NOT_LOADED) {
            // The index has not been loaded yet.
            // Start a new load thread.
            VFSManager.runInWorkThread(new JIndexLoader());
        }
        return index;
    }


    /** Return the current status */
    public final int getStatus() {
        return status;
    }


    public final String getIndexFilename() {
        return indexfilename;
    }


    public final boolean indexExists() {
        return status == STATUS_OK || status == STATUS_NOT_LOADED;
    }


    public void addJIndexListener(JIndexListener listener) {
        if (!listeners.contains(listener))
            listeners.addElement(listener);
    }


    public void removeJIndexListener(JIndexListener listener) {
        if (listeners.contains(listener))
            listeners.removeElement(listener);
    }


    public void createIndex(LibEntry[] libs) {
        if (status == STATUS_IS_CREATING) {
            GUIUtilities.message(null, "jindex.createIsRunning", null);
            return;
        }
        setStatus(STATUS_IS_CREATING);
        if (!jEdit.getBooleanProperty("options.backgroundinfo.dontShowAgain", false)) {
            JCheckBox dontShowAgain = new JCheckBox(jEdit.getProperty("jindex.backgroundinfo.dontShowAgain"), false);
            String title = jEdit.getProperty("jindex.backgroundinfo.title");
            String msg = jEdit.getProperty("jindex.backgroundinfo.message");
            JOptionPane.showMessageDialog(null, new Object[] { msg, dontShowAgain }, title, JOptionPane.INFORMATION_MESSAGE);
            jEdit.setBooleanProperty("options.backgroundinfo.dontShowAgain", dontShowAgain.isSelected());
        }
        VFSManager.runInWorkThread(new JIndexCreator(libs));
    }


    private JIndexHolder() { }


    /**
     * Sets a new index and fires a JIndex change event.
     * @param newIndex  the new JIndex.
     */
    private void setIndex(JIndex newIndex) {
        synchronized(instance) {
            JIndex oldIndex = index;
            index = newIndex;
            if (index != oldIndex) {
                // fire a JIndex changed event:
                JIndexChangeEvent evt = new JIndexChangeEvent(index, status);
                for (Enumeration e = listeners.elements(); e.hasMoreElements(); )
                    ((JIndexListener)e.nextElement()).indexChanged(evt);
            }
        }
    }


    /**
     * Set a new status and fire a JIndex statusChange.
     * @param newStatus  the new status.
     */
    private void setStatus(int newStatus) {
        synchronized(instance) {
            int oldStatus = status;
            status = newStatus;
            if (status != oldStatus) {
                // fire a JIndex status changed event:
                JIndexChangeEvent evt = new JIndexChangeEvent(index, status);
                for (Enumeration e = listeners.elements(); e.hasMoreElements(); )
                    ((JIndexListener)e.nextElement()).indexChanged(evt);
            }
        }
    }


    /** The index. */
    private static JIndex index = null;

    /** JIndexChangeEvent listeners. */
    private static Vector listeners = new Vector();

    /** The full qualified filename of the serialized JIndex object */
    private static String indexfilename;

    /** The current status. */
    private static int status;

    /** A flag indicating whether JIndex is currently creating a new index. */
    private static boolean isCreatingIndex = false;

    /** The filename of the serialized JIndex object. */
    private static final String INDEXFILENAME = "jindex.ser";

    /** The singleton instance of this class. */
    private static final JIndexHolder instance = new JIndexHolder();


    static {
        String homeDir = jEdit.getSettingsDirectory();
        if (homeDir == null) {
            status = STATUS_NO_SETTINGSDIR;
            GUIUtilities.error(null, "jindex.error.nohome", null);
        } else {
            String dirname = homeDir + File.separator + "jindex";
            File indexdir = new File(dirname);
            indexfilename = dirname + File.separator + INDEXFILENAME;
            if (!indexdir.exists()) {
                indexdir.mkdirs();
                status = STATUS_NOT_EXISTS;
            } else {
                File f = new File(indexfilename);
                if (f.exists() && f.isFile() && f.canRead())
                    status = STATUS_NOT_LOADED;
                else
                    status = STATUS_NOT_EXISTS;
            }
        }
    }


    class JIndexCreator extends WorkRequest {

        private LibEntry[] libs;


        public JIndexCreator(LibEntry[] libs) {
            this.libs = libs;
        }


        public void run() {
            JIndex newIndex = new JIndex(libs);
            System.gc();

            JIndexCreator.this.setStatus("Writing index out to " + getIndexFilename() + " ...");
            FileWriter fw;
            BufferedWriter bw;

            // create output stream:
            try {
                fw = new FileWriter(getIndexFilename());
                bw = new BufferedWriter(fw);
            }
            catch (IOException e) {
                GUIUtilities.error(null, "jindex.error.write", new Object[] { getIndexFilename(), e } );
                return;
            }

            // write index out:
            try {
                newIndex.writeXML(bw);
                bw.flush();
                bw.close();
                setIndex(newIndex);
                JIndexHolder.this.setStatus(STATUS_OK);
            }
            catch (OutOfMemoryError e) {
                System.gc();
                JIndexHolder.this.setStatus(STATUS_CREATE_ERROR);
                Log.log(Log.ERROR, this, e.toString());
                String mxOption = "-Xmx<size>m";
                if (MiscUtilities.compareVersions("1.2", System.getProperty("java.version")) > 0)
                    mxOption = "-mx<size>m"; // old JDK 1.1.x
                GUIUtilities.error(null, "jindex.error.outofmemory", new Object[] { "creating", mxOption });
            }
            catch (IOException e) {
                JIndexHolder.this.setStatus(STATUS_CREATE_ERROR);
                GUIUtilities.error(null, "jindex.error.write", new Object[] { getIndexFilename(), e } );
            }
            finally {
                newIndex = null;
                libs = null;
                System.gc();
            }
        }

    } // inner class JIndexCreator


    /**
     * This class is responsible for loading a JIndex index asynchronously.
     */
    class JIndexLoader extends WorkRequest {

        class CountReader extends BufferedReader {
            public CountReader(Reader r) { super(r); }
            public int read(char[] cbuf, int off, int len) throws IOException {
                int ret = super.read(cbuf, off, len);
                pos += len;
                ++readNr;
                if (readNr % 30 == 0)
                    setProgressValue((int)pos);
                return ret;
            }
            public int read(char[] cbuf) throws IOException {
                int ret = super.read(cbuf);
                pos += cbuf.length;
                ++readNr;
                if (readNr % 30 == 0)
                    setProgressValue((int)pos);
                return ret;
            }
            public int read() throws IOException {
                int ret = super.read();
                ++pos;
                ++readNr;
                if (readNr % 30 == 0)
                    setProgressValue((int)pos);
                return ret;
            }
            private long readNr = 0;
            private long pos = 0;
        }

        public void run() {
            if (getStatus() != STATUS_NOT_LOADED)
                return;

            // inform listeners to display an info text.
            JIndexHolder.this.setStatus(STATUS_LOADING);
            JIndexLoader.this.setStatus("Loading index...");

            // load the JIndex
            try {
                File f = new File(getIndexFilename());
                setProgressMaximum((int)f.length());
                FileReader fr = new FileReader(f);
                CountReader cr = new CountReader(fr);
                JIndex newIndex = new JIndex();
                newIndex.readXML(cr);
                cr.close();
                setIndex(newIndex);
                JIndexHolder.this.setStatus(STATUS_OK);
            }
            catch (OutOfMemoryError e) {
                System.gc();
                JIndexHolder.this.setStatus(STATUS_LOAD_ERROR);
                Log.log(Log.ERROR, this, e.toString());
                String mxOption = "-Xmx<size>m";
                if (MiscUtilities.compareVersions("1.2", System.getProperty("java.version")) > 0)
                    mxOption = "-mx<size>m"; // old JDK 1.1.x
                GUIUtilities.error(null, "jindex.error.outofmemory", new Object[] { "loading", mxOption });
            }
            catch (FileNotFoundException fnf) {
                JIndexHolder.this.setStatus(STATUS_NOT_EXISTS);
            }
            catch(XmlException xe) {
                JIndexHolder.this.setStatus(STATUS_INV_FILE);
                Log.log(Log.ERROR, this, xe.toString());
                String msg = xe.getMessage();
                if (msg.startsWith("wrong version")) {
                    GUIUtilities.error(null, "jindex.error.wrongversion",
                        new Object[] { getIndexFilename(), xe.getMessage() }
                    );
                    new ConfigureDialog(jEdit.getFirstView());
                } else {
                    GUIUtilities.error(null, "jindex.error.invalidfile",
                        new Object[] {
                            getIndexFilename(),
                            xe.getMessage(),
                            new Integer(xe.getLine()),
                            new Integer(xe.getColumn())
                        }
                    );
                }
            }
            catch (Exception e) {
                JIndexHolder.this.setStatus(STATUS_LOAD_ERROR);
                Log.log(Log.ERROR, this, e);
                GUIUtilities.error(null, "jindex.error.load", new Object[] { getIndexFilename(), e });
            }
        }

    } // inner class JIndexLoader

}
