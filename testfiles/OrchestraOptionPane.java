package com.addictedtor.orchestra ;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.af.commons.threading.ProgressDialog;
import org.af.commons.widgets.WidgetFactory;
import org.af.jhlir.tools.DirectoryGuesser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;

/**
 * Options panel
 *
 * @author Bernd Bischl <bernd_bischl@gmx.net>
 * @author Romain Francois <francoisromain@free.fr>
 */

@SuppressWarnings("serial")
public class OrchestraOptionPane extends AbstractOptionPane implements ActionListener {
	protected static final Log logger = LogFactory.getLog(OrchestraOptionPane.class);

	public static final String OPTION_PREFIX = "options.orchestra.";
	private JTextField tfRHome = new JTextField();
	private JButton bSelectRHome = new JButton();
	private JCheckBox chbShortcutEnabler = new JCheckBox("Create shortcut?");
	private JTextField tfShortcut = new JTextField();
	private JButton bSelectShortcut = new JButton();
	// has the user clicked some checkbox at least once
	private boolean buttonToggled = false;
	// should orchestra r pack be installed regardless ?
	private JCheckBox chbForceRPackInstall = new JCheckBox("Force R package installation?");

	public OrchestraOptionPane() {
		super("orchestra-optionpane");
	}

	public void _init() {
		
		String text = "This option pane configures R specific settings for using jedit as an R IDE.\n" +
				"Please take time to review the options and click OK to create the orchestra startup script"  ;
		addComponent( getTextComponent( text ), GridBagConstraints.BOTH );
		
		addSeparator(OPTION_PREFIX + "rhome" + ".title");
		text = "We have made our best guess at where R is installed\n"+
		"but if R is installed somewhere else, or you want to use another R, "+
		"please update the information" ; 
		addComponent( getTextComponent( text ), GridBagConstraints.BOTH );
		addPathPanel("rhome", tfRHome, bSelectRHome);
		
		addSeparator(OPTION_PREFIX + "shortcut" + ".title");
		text = "Click the checkbox if you want to install a desktop shortcut (recommended)" ;
		addComponent( getTextComponent( text ), GridBagConstraints.BOTH );
		addComponent("", chbShortcutEnabler);
		addPathPanel("shortcut", tfShortcut, bSelectShortcut);
		
		addSeparator(OPTION_PREFIX + "advanced.title");
		chbForceRPackInstall.setSelected(true);
		text = "Should we force installation of the R package 'orchestra' into " +
				"an R library specific to jedit or try " +
				"to use another installed version of the 'orchestra' R package" ;
		addComponent( getTextComponent( text ), GridBagConstraints.BOTH );
		addComponent("", chbForceRPackInstall);
		WidgetFactory.registerEnabler(chbShortcutEnabler, tfShortcut);
		WidgetFactory.registerEnabler(chbShortcutEnabler, bSelectShortcut);

		guessDirs();
	}

	private void addPathPanel(String name, JTextField tf, JButton b) {
		tf.setText(jEdit.getProperty(OPTION_PREFIX + name + ".path"));
		b.setText(jEdit.getProperty(OPTION_PREFIX + "choose-dir"));
		b.setActionCommand("choose-" + name);
		JPanel pathPanel = new JPanel(new BorderLayout(0, 0));
		pathPanel.add(tf, BorderLayout.CENTER);
		pathPanel.add(b, BorderLayout.EAST);
		b.addActionListener(this);
		addComponent("", pathPanel);
	}

	private void guessDirs() {
		if (tfRHome.getText().trim().equals("")) {
			String rHome = DirectoryGuesser.guessRHome();
			logger.info("Guessing R_HOME : " + rHome);
			tfRHome.setText(rHome);
		}
		if (tfShortcut.getText().trim().equals("")) {
			String desktop = DirectoryGuesser.guessDesktop();
			logger.info("Guessing shortcut dir : " + desktop);
			tfShortcut.setText(desktop);
		}
	}

	private void selectDir(JTextField tf) {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int res = fc.showOpenDialog(this);
		if (res == JFileChooser.APPROVE_OPTION) {
			String dir = fc.getSelectedFile().getAbsolutePath();
			tf.setText(dir);
		}
	}

	public void actionPerformed(ActionEvent e) {
		String ac = e.getActionCommand();
		if (ac.startsWith("choose-shortcut")) {
			selectDir(tfShortcut);
		}
		if (ac.startsWith("choose-rhome")) {
			selectDir(tfRHome);
		}
		if (e.getSource() == chbShortcutEnabler || e.getSource() == chbShortcutEnabler) {
			buttonToggled = true;
		}
	}



	public void _save() {

		logger.info("Saving options.");

		String rhome = tfRHome.getText();
		@SuppressWarnings("unused")
		String javahome = System.getProperty("java.home");
		String shortcut = tfShortcut.getText();


		File rhomeFile = new File(rhome);

		String err = null;
		if (!rhomeFile.exists() || !rhomeFile.isDirectory()) {
			err = "R home directory does not exist or is not a proper directory!";
		}

		if (!chbShortcutEnabler.isSelected())
			shortcut = null;
		else {
			File shortcutFile = new File(shortcut);
			if (!shortcutFile.exists() || !shortcutFile.isDirectory()) {
				err = "Directory for shortcut does not exist or is not a proper directory!";
			}
		}

		if (err != null) {
			JOptionPane.showMessageDialog(this, err);
		} else {
			// only install when user has changed something
			if (propertiesChanged()) {
				Installer i = new Installer(
						jEdit.getJEditHome(),
						OrchestraPlugin.getPluginHomePath(),
						rhome,
						shortcut,
						chbForceRPackInstall.isSelected()
				);
				ProgressDialog<Void, String> pd = ProgressDialogInstaller.make(this, i);
				pd.setSize(600,500);
				pd.setVisible(true);
			}
		}
		logger.info("Saving options done.");

	}

	private boolean propertiesChanged() {
		logger.info("Current Properties:");
		logger.info("rhome:" + jEdit.getProperty(OPTION_PREFIX + "rhome.path"));
		logger.info("shortcut:" + jEdit.getProperty(OPTION_PREFIX + "shortcut.path"));
		logger.info("toggled:" + buttonToggled);
		return
		jEdit.getProperty(OPTION_PREFIX + "rhome.path") == null ||
		!jEdit.getProperty(OPTION_PREFIX + "rhome.path").equals(tfRHome.getText()) ||
		jEdit.getProperty(OPTION_PREFIX + "shortcut.path") == null ||
		!jEdit.getProperty(OPTION_PREFIX + "shortcut.path").equals(tfShortcut.getText()) ||
		buttonToggled
		;

	}

	private JComponent getTextComponent( String text ){
		JTextArea ta = new JTextArea( "\n" + text + "\n" ) ;
		ta.setEditable( false) ;
		ta.setLineWrap(true) ;
		ta.setBackground( getBackground() ) ;
		ta.setBorder( BorderFactory.createEmptyBorder() ) ;
		JScrollPane scroller = new JScrollPane( ta ) ;
		scroller.setBorder( BorderFactory.createEmptyBorder() ) ;
		return scroller ; 
	}
	
}
