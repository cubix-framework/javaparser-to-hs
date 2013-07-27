package javamacros;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;
import javax.swing.JOptionPane;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JavaMacroHandler extends Macros.Handler {

    private final List<File> roots = new ArrayList<File>(4);
    private JavaMacrosClassLoader classLoader;

    public JavaMacroHandler() {
        super("java");
    }

    @Override
    public Macros.Macro createMacro(String macroName, String path) {
        // remove '.class'
        macroName = macroName.substring(0, macroName.length() - 6);
        path = path.substring(0, path.length() - 6);
        // retrieve macros root path; assuming it is the first directory named 'macros' in macro file name
        final int p = path.indexOf(File.separatorChar + "macros" + File.separatorChar);
        if (p == -1) {
            throw new IllegalStateException("Cannot determine macros root path for file: " + path);
        }
        final File root = new File(path.substring(0, p));
        if (!roots.contains(root)) {
            roots.add(root);
        }
        // retrieve class name
        final String className = path.substring(p + 1).replace(File.separatorChar, '.');
        return new Macros.Macro(this, macroName, Macros.Macro.macroNameToLabel(macroName), className);
    }

    @Override
    public void runMacro(View view, Macros.Macro macro) {
        try {
            final Class<?> clazz = findMacroClass(macro.getPath());
            if (!MacroClass.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Macro class must implement " + MacroClass.class.getCanonicalName());
            }
            MacroClass instance = (MacroClass)clazz.newInstance();
            instance.run(view.getBuffer(), view, macro, view.getTextArea());
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (MacroErrorMessageException e) {
            Log.log(Log.ERROR, this, e.getMessage());
            Macros.error(view, e.getMessage());
        } catch (Throwable e) {
            Log.log(Log.ERROR, this, "Cannot run macro " + macro.getName(), e);
            JOptionPane.showMessageDialog(view,
                    e.getClass().getName() + ": " + e.getMessage(),
                    jEdit.getProperty("macro-error.title"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Class<?> findMacroClass(String className) {
        if (classLoader != null && classLoader.isClassLoaded(className)) {
            final JavaMacrosClassLoader.ClassDetails classDetails = classLoader.getClassDetails(className);
            if (classDetails.lastModified == classDetails.file.lastModified()) {
                return classDetails.clazz;
            }
            // at least one class was changed, re-create class loader and reload class
            classLoader = null;
        }
        if (classLoader == null) {
            classLoader = new JavaMacrosClassLoader(this.getClass().getClassLoader(), roots.toArray(new File[roots.size()]));
        }
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

}