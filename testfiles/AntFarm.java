/*
 *  AntFarm.java - Plugin for running Ant builds from jEdit.
 *  Copyright (C) 2001 Brian Knowles
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package antfarm;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.RolloverButton;
import org.gjt.sp.jedit.msg.BufferUpdate;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.jedit.msg.VFSUpdate;
import org.gjt.sp.util.Log;

import console.Console;
import console.ConsolePlugin;
import console.Shell;

public class AntFarm extends JPanel implements EBComponent
{

    public final static String NAME = AntFarmPlugin.NAME;

    JButton addAntFile;

    JButton removeAntFile;

    JButton runTarget;

    JButton options;

    private Hashtable _antProjects = new Hashtable();

    private Panel _toolBar;

    private JScrollPane _projects;

    private AntTree _antTree;

    private View _view;

    private JPopupMenu _optionsPopup;

    public AntFarm(View view)
    {
        super(new BorderLayout());
        _view = view;
        initComponents();
    }

    static Icon loadIcon(String propertyName)
    {
        String iconProperty = propertyName + "icon.";
        String iconNameProperty = iconProperty + "name";
        String iconName = jEdit.getProperty(iconNameProperty);

        if (iconName == null)
            return null;

        String iconTypeProperty = iconProperty + "type";
        String iconType = jEdit.getProperty(iconTypeProperty);

        if (iconType == null)
            iconType = "custom";

        Icon returnValue = null;

        if (iconType.equalsIgnoreCase("stock"))
        {
            returnValue = GUIUtilities.loadIcon(iconName);
        }
        else
        {
            URL rsc = AntFarm.class.getResource(iconName);
            returnValue = new ImageIcon(rsc);
        }

        return returnValue;
    }

    public String getName()
    {
        return AntFarm.NAME;
    }

    public Shell getAntFarmShell()
    {
        return AntFarmPlugin.ANT_SHELL;
    }

    public Component getComponent()
    {
        return this;
    }

    public AntTree getAntTree()
    {
        return _antTree;
    }

    public Vector getAntBuildFiles()
    {
        String prop = jEdit.getProperty("antviewer.buildfiles");
        if (prop == null)
            prop = "";
        StringTokenizer tok = new StringTokenizer(prop, ",");

        Vector v = new Vector();
        while (tok.hasMoreElements())
            v.addElement(tok.nextToken());

        return v;
    }

    public void addNotify()
    {
        super.addNotify();
        EditBus.addToBus(this);
    }

    public void removeNotify()
    {
        super.removeNotify();
        EditBus.removeFromBus(this);
    }

    public void handleMessage(EBMessage msg)
    {

        if (msg instanceof BufferUpdate)
        {
            BufferUpdate updateMessage = (BufferUpdate) msg;
            if (updateMessage.getWhat() == BufferUpdate.LOADED)
            {
                Buffer buffer = updateMessage.getBuffer();
                if (buffer.getName().equals("build.xml"))
                    addAntBuildFile(buffer.getPath());
            }
        }

        if (msg instanceof VFSUpdate)
        {
            VFSUpdate updateMessage = (VFSUpdate) msg;
            reloadAntBuildFile(updateMessage.getPath());
        }

        if (msg instanceof PropertiesChanged)
        {
            _optionsPopup = new OptionsPopup();
            _antTree.reload();
        }
    }

    public void addAntBuildFile(String path)
    {
        if (isAntFileKnown(path))
            return;

        String currentProp = jEdit.getProperty("antviewer.buildfiles");
        if ((currentProp != null) && (currentProp.length() > 0))
            currentProp += "," + path;
        else
            currentProp = path;

        jEdit.setProperty("antviewer.buildfiles", currentProp);
        jEdit.saveSettings();

        _antTree.reload();
    }

    public void reloadAntBuildFile(String path)
    {
        if (!isAntFileKnown(path))
            return;
        AntFarmPlugin.getErrorSource().clear();
        try
        {
            _antProjects.put(path, AntFarmPlugin.parseBuildFile(path));
        }
        catch (Exception e)
        {
            // always force broken files to be re-parsed
            _antProjects.remove(path);
        }
        _antTree.reload();
        loadBuildFileInShell(path, true);
    }

    public void removeAntBuildFile(String path)
    {
        if (_antTree == null)
            return;
        Vector buildFiles = getAntBuildFiles();
        Vector remainingBuildFiles = new Vector();

        String file = null;
        for (int i = 0; i < buildFiles.size(); i++)
        {
            file = (String) buildFiles.elementAt(i);
            if (!file.equals(path))
                remainingBuildFiles.addElement(file);
        }

        saveAntFilesProperty(remainingBuildFiles);
        _antTree.removeBuildFileNode();
    }

    public void promptForAntBuildFile()
    {
        String[] buildFile = GUIUtilities.showVFSFileDialog(_view, null,
            VFSBrowser.OPEN_DIALOG, false);
        if (null == buildFile)
            return;

        // assume we only got one back since we only allowed single
        // selection.
        addAntBuildFile(buildFile[0]);
    }

    Project getProject(String buildFilePath) throws Exception
    {
        Project project = (Project) _antProjects.get(buildFilePath);
        if (project != null)
            return project;
        project = AntFarmPlugin.parseBuildFile(buildFilePath);
        _antProjects.put(buildFilePath, project);
        return project;
    }

    void removeAntBuildFile()
    {
        if (_antTree == null)
            return;
        removeAntBuildFile(_antTree.getSelectedBuildFile());
    }

    void loadBuildFileInShell(String filePath)
    {
        loadBuildFileInShell(filePath, false);
    }

    void loadBuildFileInShell(String filePath, boolean forceLoad)
    {
        Vector buildFiles = getAntBuildFiles();
        for (int i = 0; i < buildFiles.size(); i++)
        {
            String fileName = (String) buildFiles.elementAt(i);
            if (fileName.equals(filePath) && forceLoad)
            {
                Console console = AntFarmPlugin.getConsole(_view, false);
                console.run(AntFarmPlugin.ANT_SHELL, "=" + (i + 1));
                break;
            }
            else if (fileName.equals(filePath)
                && !filePath
                    .equals(AntFarmPlugin.ANT_SHELL.getCurrentProjectPath()))
            {
                Console console = AntFarmPlugin.getConsole(_view, false);
                console.run(AntFarmPlugin.ANT_SHELL, "=" + (i + 1));
                break;
            }
        }
    }

    void displayProjectDir(File directory)
    {
        String window = "vfs.browser";
        _view.getDockableWindowManager().addDockableWindow(window);
        VFSBrowser browser = (VFSBrowser) _view.getDockableWindowManager().getDockable(
            window);
        browser.setDirectory(directory.getAbsolutePath());
    }

    private boolean isAntFileKnown(String path)
    {
        Vector buildFiles = getAntBuildFiles();
        for (int i = 0; i < buildFiles.size(); i++)
        {
            if (path.equals((String) buildFiles.elementAt(i)))
                return true;
        }
        return false;
    }

    private void saveAntFilesProperty(Vector v)
    {
        String prop = "";
        for (int i = 0; i < v.size(); i++)
            prop += (String) v.elementAt(i) + ",";
        jEdit.setProperty("antviewer.buildfiles", prop);
        jEdit.saveSettings();
    }

    private void initComponents()
    {
        _projects = null;

        setLayout(new BorderLayout());

        Box topBox = new Box(BoxLayout.Y_AXIS);
        topBox.add(createToolBar());
        add(BorderLayout.NORTH, topBox);

        JScrollPane _projects = new JScrollPane();
        _projects.getViewport().add(_antTree = new AntTree(this, _view));

        add(BorderLayout.CENTER, _projects);

        _optionsPopup = new OptionsPopup();

        setVisible(true);

    }

    private JToolBar createToolBar()
    {
        JToolBar _toolBar = new JToolBar();
        _toolBar.setFloatable(false);
        _toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);

        _toolBar.add(addAntFile = createToolButton("add"));
        _toolBar.add(removeAntFile = createToolButton("remove"));

        _toolBar.addSeparator();
        _toolBar.add(runTarget = createToolButton("run"));

        _toolBar.addSeparator();
        _toolBar.add(options = createToolButton("options"));

        // jiwils 2003/02/23
        // options.setText("Options");

        options.addMouseListener(new MouseHandler());

        // default to enabled to false
        removeAntFile.setEnabled(false);
        runTarget.setEnabled(false);

        _toolBar.addSeparator();

        _toolBar.add(Box.createGlue());

        return _toolBar;
    }

    private JButton createToolButton(String name)
    {
        String buttonPrefix = AntFarmPlugin.NAME + "." + name + ".";
        String iconLabel = jEdit.getProperty(buttonPrefix + "text");
        Icon icon = loadIcon(buttonPrefix);
        String toolTip = jEdit.getProperty(buttonPrefix + "label");

        JButton button = new RolloverButton();

        if (icon != null)
            button.setIcon(icon);

        if (iconLabel != null)
            button.setText(iconLabel);

        if (toolTip != null)
            button.setToolTipText(toolTip);

        button.setHorizontalTextPosition(SwingConstants.LEADING);

        button.setRequestFocusEnabled(false);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.addActionListener(new ActionHandler());

        return button;
    }

    class ActionHandler implements ActionListener
    {
        public void actionPerformed(ActionEvent evt)
        {
            Object source = evt.getSource();
            if (source == addAntFile)
            {
                promptForAntBuildFile();
            }
            if (source == removeAntFile)
            {
                removeAntBuildFile();
            }
            if (source == runTarget)
            {
                _antTree.executeCurrentTarget();
            }
        }
    }

    class MouseHandler extends MouseAdapter
    {
        public void mousePressed(MouseEvent evt)
        {
            if (!_optionsPopup.isVisible())
            {

                GUIUtilities.showPopupMenu(_optionsPopup, options, 0, options
                    .getHeight(), false);
            }
            else
            {
                _optionsPopup.setVisible(false);
            }
        }
    }

    class OptionsPopup extends JPopupMenu
    {

        public OptionsPopup()
        {
            JCheckBoxMenuItem singleClickOpens = new JCheckBoxMenuItem( jEdit.getProperty( AntFarmPlugin.OPTION_PREFIX + "open-on-singleclick-label" ) );
            singleClickOpens.setSelected(jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX + "open-on-singleclick"));
            singleClickOpens.setActionCommand("single-click-opens");
            singleClickOpens.addActionListener(new ActionHandler());
            this.add(singleClickOpens);


            JCheckBoxMenuItem propertiesPrompt = new JCheckBoxMenuItem(jEdit
                .getProperty(AntFarmPlugin.NAME + ".commands.properties.label"));
            propertiesPrompt.setActionCommand("properties-prompt");
            propertiesPrompt.setSelected(!jEdit
                .getBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                    + "suppress-properties"));
            propertiesPrompt.addActionListener(new ActionHandler());
            this.add(propertiesPrompt);


            JCheckBoxMenuItem supressSubTargets = new JCheckBoxMenuItem(jEdit
                .getProperty(AntFarmPlugin.NAME
                    + ".commands.supress-sub-targets.label"));
            supressSubTargets.setActionCommand("supress-sub-targets");
            supressSubTargets.setSelected(jEdit
                .getBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                    + "supress-sub-targets"));
            supressSubTargets.addActionListener(new ActionHandler());
            this.add(supressSubTargets);

            JCheckBoxMenuItem saveAll = new JCheckBoxMenuItem(
                jEdit.getProperty(AntFarmPlugin.NAME
                    + ".commands.save-on-execute.label"));
            saveAll.setActionCommand("save-on-execute");
            saveAll.setSelected(jEdit.getBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                + "save-on-execute"));
            saveAll.addActionListener(new ActionHandler());
            this.add(saveAll);

            JMenu logLevelMenu = new JMenu("Logging level");

            for (int i = 0; i < LogLevelEnum.getAll().length; i++)
            {
                LogLevelEnum level = LogLevelEnum.getAll()[i];
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(level
                    .toString());
                item.setActionCommand(new Integer(level.getValue()).toString());
                item.addActionListener(new LogLevelActionHandler());
                item.setSelected(level.getValue() == jEdit.getIntegerProperty(
                    AntFarmPlugin.OPTION_PREFIX + "logging-level",
                    LogLevelEnum.INFO.getValue()));

                logLevelMenu.add(item);
            }

            this.add(logLevelMenu);
        }

        class LogLevelActionHandler implements ActionListener
        {
            public void actionPerformed(ActionEvent evt)
            {
                jEdit.setProperty(AntFarmPlugin.OPTION_PREFIX + "logging-level",
                    evt.getActionCommand());
                EditBus.send(new PropertiesChanged(null));

            }
        }

        class ActionHandler implements ActionListener
        {
            public void actionPerformed(ActionEvent evt)
            {
                String actionCommand = evt.getActionCommand();
                if (actionCommand.equals("single-click-opens")) {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt.getSource();
                    jEdit.setBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                        + "open-on-singleclick", item.isSelected());
                }
                if (actionCommand.equals("properties-prompt"))
                {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt
                        .getSource();
                    jEdit.setBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                        + "suppress-properties", !item.isSelected());
                }
                if (actionCommand.equals("supress-sub-targets"))
                {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt
                        .getSource();
                    jEdit.setBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                        + "supress-sub-targets", item.isSelected());
                }
                if (actionCommand.equals("save-on-execute"))
                {
                    JCheckBoxMenuItem item = (JCheckBoxMenuItem) evt
                        .getSource();
                    jEdit.setBooleanProperty(AntFarmPlugin.OPTION_PREFIX
                        + "save-on-execute", item.isSelected());
                }

                EditBus.send(new PropertiesChanged(null));
            }
        }
    }
}
