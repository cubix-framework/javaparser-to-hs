/*
 * JCompilerOptionPaneCompiler.java - plugin options pane for JCompiler - compiler options
 * (c) 1999, 2000 Kevin A. Burton and Aziz Sharif
 *
 * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:
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


package jcompiler.options;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.HistoryTextField;
import org.gjt.sp.util.Log;


/**
 * This is the option pane that jEdit displays for JCompiler's
 * compiler plugin options.
 */
public class JCompilerOptionPaneCompiler
		 extends AbstractOptionPane
		 implements ActionListener
{

	public JCompilerOptionPaneCompiler()
	{
		super("jcompiler.compiler");
	}


	public void _init()
	{
		JLabel label = new JLabel(jEdit.getProperty("options.jcompiler.compiler"));
		useModernCompiler = new JRadioButton(jEdit.getProperty("options.jcompiler.compiler.modern"));
		useOldCompiler = new JRadioButton(jEdit.getProperty("options.jcompiler.compiler.old"));
		useExternalCompiler = new JRadioButton(jEdit.getProperty("options.jcompiler.compiler.external"));

		ButtonGroup group1 = new ButtonGroup();
		group1.add(useModernCompiler);
		group1.add(useOldCompiler);
		group1.add(useExternalCompiler);

		boolean modernCompile = jEdit.getBooleanProperty("jcompiler.modernCompiler", true);
		boolean externalCompile = jEdit.getBooleanProperty("jcompiler.compileexternal", false);
		if (externalCompile)
			useExternalCompiler.setSelected(true);
		else if (modernCompile)
			useModernCompiler.setSelected(true);
		else
			useOldCompiler.setSelected(true);

		addComponent(label);
		addComponent(useModernCompiler);
		addComponent(useOldCompiler);
		addComponent(useExternalCompiler);

		externalCompilerTextField = new HistoryTextField("jcompiler.externalcompiler");
		String externalCompilerTextFieldValue = jEdit.getProperty("jcompiler.externalcompiler");
		externalCompilerTextField.setText(externalCompilerTextFieldValue == null ? "" : externalCompilerTextFieldValue);
		externalCompilerTextField.setEnabled(externalCompile);
		JPanel externalCompilerPanel = new JPanel(new BorderLayout());
		externalCompilerPanel.add(externalCompilerTextField, BorderLayout.CENTER);
		pickExternalCompilerButton = new JButton(pickIcon);
		pickExternalCompilerButton.setMargin(new Insets(0,0,0,0));
		pickExternalCompilerButton.setToolTipText(jEdit.getProperty("options.jcompiler.pick.tooltip"));
		pickExternalCompilerButton.setEnabled(externalCompile);
		pickExternalCompilerButton.addActionListener(this);
		externalCompilerPanel.add(pickExternalCompilerButton, BorderLayout.EAST);
		addComponent(jEdit.getProperty("options.jcompiler.compilerpath"), externalCompilerPanel);

		ActionListener compListener = new java.awt.event.ActionListener()
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				if (useExternalCompiler.isSelected())
				{
					externalCompilerTextField.setEnabled(true);
					pickExternalCompilerButton.setEnabled(true);
				}
				else
				{
					externalCompilerTextField.setEnabled(false);
					pickExternalCompilerButton.setEnabled(false);
				}
			}
		};
		useExternalCompiler.addActionListener(compListener);
		useOldCompiler.addActionListener(compListener);
		useModernCompiler.addActionListener(compListener);

		addComponent(Box.createVerticalStrut(12));

		// "Generate debug info (-g)"
		genDebug = new JCheckBox(jEdit.getProperty("options.jcompiler.genDebug"));
		genDebug.setSelected(jEdit.getBooleanProperty("jcompiler.genDebug", true));
		addComponent(genDebug);

		// "Generate optimized code (-O)"
		genOptimized = new JCheckBox(jEdit.getProperty("options.jcompiler.genOptimized"));
		genOptimized.setSelected(jEdit.getBooleanProperty("jcompiler.genOptimized", false));
		addComponent(genOptimized);

		// "Warn about use of deprecated API (-deprecation)"
		showDeprecation = new JCheckBox(jEdit.getProperty("options.jcompiler.showdeprecated"));
		showDeprecation.setSelected(jEdit.getBooleanProperty("jcompiler.showdeprecated", true));
		addComponent(showDeprecation);

		// "Other options:"
		otherOptions = new HistoryTextField("jcompiler.otheroptions");
		String options = jEdit.getProperty("jcompiler.otheroptions");
		otherOptions.setText(options == null ? "" : options);
		addComponent(jEdit.getProperty("options.jcompiler.otheroptions"), otherOptions);

		addComponent(Box.createVerticalStrut(12));
		addSeparator("options.jcompiler.pathsSeperator");
		addComponent(Box.createVerticalStrut(12));

		//Project Viewer / JavaSideKick path option
		//if this is selected, then we take the classpath, sourcepath & basepath from
		//these plugins instead of the above options when possible
		if ((PluginJAR.findPlugin("sidekick.java.JavaSideKickPlugin") != null) &&
			(PluginJAR.findPlugin("projectviewer.ProjectPlugin") != null))
		{
			useJskPaths = new JCheckBox(jEdit.getProperty("options.jcompiler.useJskPaths"));
			useJskPaths.setSelected(jEdit.getBooleanProperty("jcompiler.useJskPaths", true));
			addComponent(useJskPaths);
		}

		addComponent(Box.createVerticalStrut(12));

		// "Set the base directory of your current project here (or leave it empty)"
		addComponent(new JLabel(jEdit.getProperty("options.jcompiler.basepath.description1")));

		// "$basepath"
		basePath = new HistoryTextField("jcompiler.basepath");
		String basePathValue = jEdit.getProperty("jcompiler.basepath");
		basePath.setText(basePathValue == null ? "" : basePathValue);
		pickBasePathButton = new JButton(pickIcon);
		pickBasePathButton.setMargin(new Insets(0,0,0,0));
		pickBasePathButton.setToolTipText(jEdit.getProperty("options.jcompiler.pick.tooltip"));
		pickBasePathButton.addActionListener(this);
		JPanel basePathPanel = new JPanel(new BorderLayout());
		basePathPanel.add(basePath, BorderLayout.CENTER);
		basePathPanel.add(pickBasePathButton, BorderLayout.EAST);
		addComponent(jEdit.getProperty("options.jcompiler.basepath"), basePathPanel);

		// "You may use the $basepath variable in the following paths:"
		addComponent(new JLabel(jEdit.getProperty("options.jcompiler.basepath.description2")));
		addComponent(Box.createVerticalStrut(12));

		// Output directory text field (+ select button)
		String output = null;
		if (jEdit.getBooleanProperty("jcompiler.specifyoutputdirectory"))
			output = jEdit.getProperty("jcompiler.outputdirectory");

		outputDirectory = new HistoryTextField("jcompiler.outputdirectory");
		outputDirectory.setText(output == null ? "" : output);
		pickDirectory = new JButton(pickIcon);
		pickDirectory.setMargin(new Insets(0,0,0,0));
		pickDirectory.setToolTipText(jEdit.getProperty("options.jcompiler.pick.tooltip"));
		pickDirectory.addActionListener(this);
		JPanel outputPanel = new JPanel(new BorderLayout());
		outputPanel.add(outputDirectory, BorderLayout.CENTER);
		outputPanel.add(pickDirectory, BorderLayout.EAST);
		addComponent(jEdit.getProperty("options.jcompiler.outputDirectory"), outputPanel);

		addComponent(Box.createVerticalStrut(12));

		// "Required library path"
		libPath = new HistoryTextField("jcompiler.libpath");
		String libPathValue = jEdit.getProperty("jcompiler.libpath");
		libPath.setText(libPathValue == null ? "" : libPathValue);
		libPath.setPreferredSize(new Dimension(270, libPath.getPreferredSize().height));
		addComponent(jEdit.getProperty("options.jcompiler.libpath"), libPath);

		// "Class path" (+ select system cp button)
		classPath = new HistoryTextField("jcompiler.classpath");
		String classPathValue = jEdit.getProperty("jcompiler.classpath");
		classPath.setText(classPathValue == null ? "" : classPathValue);
		classPath.setPreferredSize(new Dimension(270, classPath.getPreferredSize().height));
		pickCP = new JButton(pickCPIcon);
		pickCP.setMargin(new Insets(0,0,0,0));
		pickCP.setToolTipText(jEdit.getProperty("options.jcompiler.pickCP.tooltip"));
		pickCP.addActionListener(this);
		JPanel cpPanel = new JPanel(new BorderLayout());
		cpPanel.add(classPath, BorderLayout.CENTER);
		cpPanel.add(pickCP, BorderLayout.EAST);
		addComponent(jEdit.getProperty("options.jcompiler.classpath"), cpPanel);

		// "Add package of current sourcefile to CLASSPATH"
		addPkg2CP = new JCheckBox(jEdit.getProperty("options.jcompiler.addpkg2cp"));
		addPkg2CP.setSelected(jEdit.getBooleanProperty("jcompiler.addpkg2cp", true));
		addComponent("", addPkg2CP);

		addComponent(Box.createVerticalStrut(12));

		// "Source path"
		srcPath = new HistoryTextField("jcompiler.sourcepath");
		String srcPathValue = jEdit.getProperty("jcompiler.sourcepath");
		srcPath.setText(srcPathValue == null ? "" : srcPathValue);
		srcPath.setPreferredSize(new Dimension(270, srcPath.getPreferredSize().height));

		addComponent(jEdit.getProperty("options.jcompiler.sourcepath"), srcPath);

	}


	public void _save()
	{
		jEdit.setBooleanProperty("jcompiler.modernCompiler", useModernCompiler.isSelected());
		jEdit.setBooleanProperty("jcompiler.compileexternal", useExternalCompiler.isSelected());
		jEdit.setBooleanProperty("jcompiler.genDebug", genDebug.isSelected());
		jEdit.setBooleanProperty("jcompiler.genOptimized", genOptimized.isSelected());
		jEdit.setBooleanProperty("jcompiler.showdeprecated", showDeprecation.isSelected());
		jEdit.setBooleanProperty("jcompiler.addpkg2cp", addPkg2CP.isSelected());

		jEdit.setProperty("jcompiler.basepath", basePath.getText().trim());
		jEdit.setProperty("jcompiler.libpath", libPath.getText().trim());
		jEdit.setProperty("jcompiler.classpath", classPath.getText().trim());
		jEdit.setProperty("jcompiler.otheroptions", otherOptions.getText().trim());
		jEdit.setProperty("jcompiler.externalcompiler",externalCompilerTextField.getText().trim());
		jEdit.setProperty("jcompiler.sourcepath", srcPath.getText().trim());

		jEdit.setBooleanProperty("jcompiler.useJskPaths", useJskPaths.isSelected());

		String outputDir = outputDirectory.getText().trim();
		jEdit.setBooleanProperty("jcompiler.specifyoutputdirectory", outputDir.length() > 0);
		jEdit.setProperty("jcompiler.outputdirectory", outputDir);

		basePath.addCurrentToHistory();
		libPath.addCurrentToHistory();
		classPath.addCurrentToHistory();
		otherOptions.addCurrentToHistory();
		outputDirectory.addCurrentToHistory();
		externalCompilerTextField.addCurrentToHistory();
		srcPath.addCurrentToHistory();
	}


	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == pickDirectory)
		{
			File file = chooseDirectory();
			setDirectoryText(file, outputDirectory);
		}
		else if (e.getSource() == pickBasePathButton)
		{
			File file = chooseDirectory();
			setDirectoryText(file, basePath);
		}
		else if (e.getSource() == pickCP)
		{
			classPath.setText(System.getProperty("java.class.path"));
		}
		else if (e.getSource() == pickExternalCompilerButton)
		{
			if (useExternalCompiler.isSelected())
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle(jEdit.getProperty("options.jcompiler.pick.tooltip"));
				int retVal = chooser.showOpenDialog(this);
				File file;

				if (retVal == JFileChooser.APPROVE_OPTION)
					setDirectoryText(chooser.getSelectedFile(),externalCompilerTextField);
			}
		}
	}


	/**
	 * Display a file chooser dialog for directories only.
	 *
	 * @return the File if one was selected, or null if user clicked cancel.
	 */
	private File chooseDirectory()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle(jEdit.getProperty("options.jcompiler.pick.tooltip"));
		int retVal = chooser.showOpenDialog(this);

		if (retVal == JFileChooser.APPROVE_OPTION)
			return chooser.getSelectedFile();
		else
			return null;
	}


	private void setDirectoryText(File file, JTextField textField)
	{
		if (file != null)
		{
			try
			{
				String dirName = file.getCanonicalPath();
				textField.setText(dirName);
			}
			catch(IOException e)
			{
				Log.log(Log.ERROR, this, "Something went wrong getting the canonical path for directory " + file);
				Log.log(Log.ERROR, this, e);
			}
		}
	}


	private JRadioButton useOldCompiler;
	private JRadioButton useModernCompiler;
	private JRadioButton useExternalCompiler;
	private JCheckBox genDebug;
	private JCheckBox genOptimized;
	private JCheckBox showDeprecation;
	private JCheckBox addPkg2CP;
	private JCheckBox useJskPaths;
	private JLabel cpLabel;
	private JButton pickDirectory;
	private JButton pickCP;
	private JButton pickBasePathButton;
	private JButton pickExternalCompilerButton;
	private HistoryTextField basePath;
	private HistoryTextField srcPath;
	private HistoryTextField libPath;
	private HistoryTextField classPath;
	private HistoryTextField outputDirectory;
	private HistoryTextField otherOptions;
	private HistoryTextField externalCompilerTextField;

	private static Icon pickIcon = null;
	private static Icon pickCPIcon = null;


	static
	{
		URL url = JCompilerOptionPaneCompiler.class.getResource("DirOpen.gif");
		if (url != null)
			pickIcon = new ImageIcon(url);
		else
			Log.log(Log.ERROR, JCompilerOptionPaneCompiler.class, "Error fetching image DirOpen.gif");

		url = JCompilerOptionPaneCompiler.class.getResource("JavaCup.gif");
		if (url != null)
			pickCPIcon = new ImageIcon(url);
		else
			Log.log(Log.ERROR, JCompilerOptionPaneCompiler.class, "Error fetching image JavaCup.gif");
	}

}
