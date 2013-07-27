
package beauty;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.util.Log;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.swing.*;

import beauty.beautifiers.*;

/**
 * Beauty uses either beautifiers defined in services.xml or by the user having
 * created a properties file describing a custom beautifier.  The custom properties
 * files are one per mode, with the name of each file being the mode name with a
 * .properties extension.  These properties files are stored in the plugin home
 * directory.
 */
public class BeautyPlugin extends EditPlugin {

    // map of mode name <-> custom beautifier properties file
    private static HashMap<String, File> modeFiles;

    public void start() {
        copyBundledProperties();
        loadProperties();
        registerServices();
    }

    // copies any custom beautifier properties files from the plugin jar to
    // the plugin home directory, but does not overwrite files of the same
    // name that already exist in the plugin home directory.
    private void copyBundledProperties() {
        // this property has a comma separated list of the just the names of the properties
        // files.  The files are located in the jar file at beauty/beautifiers/custom.
        String propsFiles = jEdit.getProperty("plugin.beauty.beautifiers.custom");
        if (propsFiles == null || propsFiles.length() == 0) {
            return ;
        }
        String[] filenames = propsFiles.split(",");
        File homeDir = jEdit.getPlugin("beauty.BeautyPlugin").getPluginHome();
        homeDir.mkdirs();
        for (String filename : filenames) {
            filename = filename.trim();
            File outfile = new File(homeDir, filename);
            if (outfile.exists()) {
                continue;
            }
            String resource = "beauty/beautifiers/custom/" + filename;
            copyToFile(getClass().getClassLoader().getResourceAsStream(resource), outfile);
        }
    }

    public static void registerServices() {
        // read the custom mode beautifiers file and dynamically add services
        // for any defined beautifiers
        PluginJAR jar = jEdit.getPlugin("beauty.BeautyPlugin").getPluginJAR();
        for (String modeName : modeFiles.keySet()) {
            ServiceManager.registerService("beauty.beautifiers.Beautifier", modeName + ".custom", "new beauty.beautifiers.DefaultBeautifier(\"" + modeName + "\")", jar);
        }
    }

    /**
     * Load the properties files containing the definitions any custom beautifiers.
     * These files are stored in the plugin home directory.  They are named with
     * the mode name followed by .properties and are stored in the modeFiles map.
     */
    private static void loadProperties() {
        try {
            File homeDir = jEdit.getPlugin("beauty.BeautyPlugin").getPluginHome();
            File[] files = homeDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".properties");
                }
            }
           );
            modeFiles = new HashMap<String, File>();
            for (File file : files) {
                String filename = file.getName();
                String modeName = filename.substring(0, filename.lastIndexOf(".properties"));
                modeFiles.put(modeName, file);
            }
        } catch (Exception ignored) {            // NOPMD
        }
    }

    /**
     * Get the properties describing a custom beautifier.
     * @param modeName The name of the mode to look for a custom beautifier.
     * @return A properties.  This may be empty, but won't be null.
     */
    public static Properties getCustomModeProperties(String modeName) {
        loadProperties();
        File modeFile = modeFiles.get(modeName);

        Properties props = getModeIndentProperties(modeName);

        if (modeFile == null) {
            // no custom beautifier properties file found for this mode,
            // nothing more to do.
            StringWriter sw = new StringWriter();
            props.list(new PrintWriter(sw));
            return props;
        }

        // read the custom beautifier properties file into a Properties
        try {
            InputStream reader = new BufferedInputStream(new FileInputStream(modeFile));
            Properties p = new Properties();
            p.load(reader);
            reader.close();
            props.putAll(p);
            StringWriter sw = new StringWriter();
            props.list(new PrintWriter(sw));
            return props;
        } catch (Exception e) {
            e.printStackTrace();
            return props;
        }
    }

    private static Properties getModeIndentProperties(String modeName) {
        Mode mode = jEdit.getMode(modeName);
        ModeProvider.instance.loadMode(mode);
        mode.loadIfNecessary();
        mode.init();
        mode.getIndentRules();        // initializes the indent rules if they haven't been already
        Properties p = new Properties();

        String indentOpenBrackets = (String) mode.getProperty("indentOpenBrackets");
        String indentCloseBrackets = (String) mode.getProperty("indentCloseBrackets");
        String unalignedOpenBrackets = (String) mode.getProperty("unalignedOpenBrackets");
        String unalignedCloseBrackets = (String) mode.getProperty("unalignedCloseBrackets");
        String indentNextLine = (String) mode.getProperty("indentNextLine");
        String unindentThisLine = (String) mode.getProperty("unindentThisLine");
        String electricKeys = (String) mode.getProperty("electricKeys");
        String lineUpClosingBracket = mode.getBooleanProperty("lineUpClosingBracket") ? "true" : "false";
        String doubleBracketIndent = mode.getBooleanProperty("doubleBracketIndent") ? "true" : "false";

        p.setProperty("indentOpenBrackets", indentOpenBrackets == null ? "" : indentOpenBrackets);
        p.setProperty("indentCloseBrackets", indentCloseBrackets == null ? "" : indentCloseBrackets);
        p.setProperty("unalignedOpenBrackets", unalignedOpenBrackets == null ? "" : unalignedOpenBrackets);
        p.setProperty("unalignedCloseBrackets", unalignedCloseBrackets == null ? "" : unalignedCloseBrackets);
        p.setProperty("indentNextLine", indentNextLine == null ? "" : indentNextLine);
        p.setProperty("unindentThisLine", unindentThisLine == null ? "" : unindentThisLine);
        p.setProperty("electricKeys", electricKeys == null ? "" : electricKeys);
        p.setProperty("lineUpClosingBracket", lineUpClosingBracket);
        p.setProperty("doubleBracketIndent", doubleBracketIndent);
        return p;
    }

    /**
     * Saves given properties with the mode name.
     * @param modeName The name of the mode.  This will be used as the start of the
     * file name, with .properties appended.
     * @param modeProperties The properties to save.
     */
    @SuppressWarnings("deprecation")
    public static void saveProperties(String modeName, Properties modeProperties) {
        if (modeName == null || modeProperties == null) {
            return ;
        }
        try {
            File homeDir = jEdit.getPlugin("beauty.BeautyPlugin").getPluginHome();
            if (!homeDir.exists()) {
                homeDir.mkdir();
            }
            File customFile = new File(homeDir, modeName + ".properties");
            OutputStream writer = new BufferedOutputStream(new FileOutputStream(customFile));
            modeProperties.save(writer, "Properties for " + modeName + " custom beautifier.");
            writer.flush();
            writer.close();
        } catch (Exception ignored) {            // NOPMD
        }
        loadProperties();
    }

    /**
     * For backward compatibility -- Beautify the current buffer using Beauty.
     *
     * @param buffer  The buffer to be beautified.
     * @param view  The view; may be null, if there is no current view.
     * @param showErrorDialogs not used, it's now a user setting.
     */
    public static void beautify(Buffer buffer, View view, boolean showErrorDialogs) {
        beautify(buffer, view);
    }
    
    /**
     * Beautify the current buffer using Beauty.
     *
     * @param buffer  The buffer to be beautified.
     * @param view  The view; may be null, if there is no current view.
     */
    public static void beautify(Buffer buffer, View view) {
        boolean showErrorDialogs = jEdit.getBooleanProperty("beauty.general.showErrorDialogs", true);

        if (buffer.isReadOnly()) {
            Log.log(Log.NOTICE, BeautyPlugin.class, jEdit.getProperty("beauty.error.isReadOnly.message"));
            if (showErrorDialogs) {
                GUIUtilities.error(view, "beauty.error.isReadOnly", null);
            }
            return ;
        }

        // load beautifier
        String mode = buffer.getStringProperty("beauty.beautifier");
        if (mode == null) {
            mode = buffer.getMode().getName();
        }
        Beautifier beautifier = (Beautifier) ServiceManager.getService(Beautifier.SERVICE_NAME, mode);
        if (beautifier == null) {
            if (jEdit.getBooleanProperty("beauty.useBuiltInIndenter", false)) {
                indentLines(view);
                return ;
            } else {
                if (showErrorDialogs) {
                    JOptionPane.showMessageDialog(view, "Error: can't beautify this buffer because I don't know how to handle this mode.", "Beauty Error", JOptionPane.ERROR_MESSAGE);
                    return ;
                } else {
                    Log.log(Log.NOTICE, BeautyPlugin.class, "buffer " + buffer.getName() + " not beautified, because mode is not supported.");
                    return ;
                }
            }
        }

        // run the format routine synchronously on the AWT thread
        if (SwingUtilities.isEventDispatchThread()) {
            new BeautyThread(buffer, view, showErrorDialogs, beautifier).run();
        } else {
            try {
                SwingUtilities.invokeAndWait(new BeautyThread(buffer, view, showErrorDialogs, beautifier));
            } catch (Exception e) {
                if (showErrorDialogs) {
                    JOptionPane.showMessageDialog(view, e.getMessage(), "Beauty Error", JOptionPane.ERROR_MESSAGE);
                    return ;
                } else {
                    Log.log(Log.NOTICE, BeautyPlugin.class, "buffer " + buffer.getName() + " not beautified.\n" + e.getMessage());
                    return ;
                }
            }
        }
    }

    public static void indentLines(View view) {
        JEditTextArea ta = view.getEditPane().getTextArea();
        int cp = ta.getCaretPosition();
        Buffer buffer = view.getBuffer();
        buffer.indentLines(0, buffer.getLineCount() - 1);
        restoreCaretPosition(view.getEditPane(), cp);
    }

    static void restoreCaretPosition(EditPane editPane, int caretPosition) {
        final EditPane ep = editPane;
        final int offset = Math.min(caretPosition, editPane.getTextArea().getBufferLength());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ep.getTextArea().setCaretPosition(offset, true);
                ep.getTextArea().scrollToCaret(true);
            }
        }
       );
    }

    public static void toggleSplitAttributes(View view) {
        boolean split = jEdit.getBooleanProperty("xmlindenter.splitAttributes", false);
        jEdit.setBooleanProperty("xmlindenter.splitAttributes", ! split);
        beautify(view.getBuffer(), view);
    }

    /**
     * Copies a stream to a file. If destination file exists, it will be
     * overwritten. The input stream may be closed when this method returns.
     *
     * @param from           stream to copy from, will be closed after copy
     * @param to             file to write
     * @exception Exception  most likely an IOException
     */
    public static void copyToFile(InputStream from, File to) {
        try {
            FileOutputStream out = new FileOutputStream(to);
            byte[] buffer = new byte[1024];
            int bytes_read;
            while (true) {
                bytes_read = from.read(buffer);
                if (bytes_read == -1) {
                    break;
                }
                out.write(buffer, 0, bytes_read);
            }
            out.flush();
            out.close();
            from.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}