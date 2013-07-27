/*
 * TextAutocompletePlugin.java
 * $id$
 * author Jakub (Kuba) Holy, 2005
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
package net.jakubholy.jedit.autocomplete;

import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;

/**
 * OptionPane for the TextAutocomplete plugin.
 * <h4>Adding new option</h4>
 * <ol>
 * 	<li>{@link #_init()} - create the form control here, set tool-tip and label
 * 	<li>{@link #_save()} - save the form control's value into jEdit's preferences
 * 	<li>{@link #redisplayValues()} - load the stored value from the {@link PreferencesManager} here
 * </ol>
 *
 * TODO Refactor: simplify adding new options (UI, save, load value) using reflection and
 * perhaps a custom annotation like @Option(label=XX,tooltip=YY,defaultValue=true).
 *
 * @author Jakub Holý
 *
 */
@SuppressWarnings("serial")
public class TextAutocompletePane extends AbstractOptionPane
{
	/*
	 * # Is a character a part of a word or is it a word separator?
		#  options.isWordElement.code
		# Only offer completions when >= minPrefixLength characters has been typed:
		options.minPrefixLength=3
		# Keys used to accept a selected completion:
		# options.isAcceptKey=KeyEvent.VK_TAB KeyEvent.VK_ENTER
		# Keys used to dispose (hide) the popup window:
		# options.isDisposeKey=KeyEvent.VK_ESCAPE
		# Keys used to move up in the popup list of completions
		# options.isSelectionUpKey=KeyEvent.VK_UP
		# Keys used to move down in the popup list of completions
		# options.isSelectionDownKey=KeyEvent.VK_DOWN
		# Shall the word be rememberd i.e. added to the completion word list?
		# options.isWordToRemember.code
	 */
	private JTextField 	isWordCode;
	private JTextField 	minPrefixLength;
	private JTextField 	acceptKeys;
	private JTextField 	disposeKeys;
	private JTextField 	selectionUpKeys;
	private JTextField 	selectionDownKeys;
	private JTextField 	isWordToRememberCode;
	private JTextField 	minWordToRememberLength;
	private JTextField 	maxCountOfWords;
	private JCheckBox  	isStartForBuffers;
	private JCheckBox  	isSelectionByNumberEnabled;
	private JComboBox 	selectionByNumberModifierMask;
	private JButton 	resetButton;
	private JTextField	filenameFilter;
	private JRadioButton isInclusionFilter;
	private JRadioButton isExclusionFilter;
	private JCheckBox  	isLoadModeKeywords;
	private JCheckBox  	isLoadMainModeOnly;

	public TextAutocompletePane() {
		super("TextAutocomplete");
	}

	/* Build the GUI */
	protected void _init() {

		addComponent(new JLabel("<html>" +
				"<h2>TextAutocomplete Global Options</h2>" +
				"Leave the mouse pointer above any option to display detailed description including the [default value]." +
				"</html>"));

		addSeparator();

		isStartForBuffers = new JCheckBox();
		isStartForBuffers.setToolTipText("Start the autocompletion automatically for every new buffer [false]");
		addComponent("Start autom. for new buffers", isStartForBuffers);
		isInclusionFilter = new JRadioButton("Include filenames matching glob patterns");
		isExclusionFilter = new JRadioButton("Exclude filenames matching glob patterns");
		ButtonGroup group = new ButtonGroup();
		group.add(isInclusionFilter);
		group.add(isExclusionFilter);
		JPanel filterPane = new JPanel(new GridLayout(0, 1));
		TitledBorder filterBorder = new TitledBorder("Filename filters");
		filterPane.setBorder(filterBorder);
		filterPane.add(isInclusionFilter);
		filterPane.add(isExclusionFilter);
		filenameFilter = new JTextField();
		filterPane.add(filenameFilter);
		addComponent(filterPane, GridBagConstraints.HORIZONTAL);

		//----------------------------------------------------------------
		addSeparator(TextAutocompletePlugin.PROPS_PREFIX + "options.words-and-completions.label"); // -------------------------------------------------

		maxCountOfWords = new JTextField();
		maxCountOfWords.setToolTipText("Remember at maximum this number of completions (words) per buffer [1000]");
		addComponent("Remember at max. words      ", maxCountOfWords);

		minPrefixLength = new JTextField();
		minPrefixLength.setToolTipText("Only offer completions when this number of characters have been typed. [2]");
		addComponent("Minimal prefix length       ", minPrefixLength);

		minWordToRememberLength = new JTextField();
		minWordToRememberLength.setToolTipText("Don't remeber words of shorter length. [5]");
		addComponent("Remember words long at least", minWordToRememberLength);

		isWordToRememberCode = new JTextField();
		isWordToRememberCode.setToolTipText("Should I remember the typed word? Use the variable 'word' (String). [true]");
		addComponent("Is word to remember? [code] ", isWordToRememberCode);

		isWordCode = new JTextField();
		isWordCode.setToolTipText("<html>Does the insertion mark the end of the word being typed? " +
				"<br>Use the variables 'insertion' (char), 'prefix' (String), 'noWordSeparators' (String). [only letters]</html>");
		/*JPanel panel = new JPanel();
		panel.add( new JLabel("Is word element? [beanshell code]") );
		panel.add( isWordElementCode );*/
		addComponent("Belongs to word? [code]     ", isWordCode);

		// EDIT MODE KEYWORDS
		{
			isLoadModeKeywords = new JCheckBox();
			isLoadModeKeywords.setToolTipText("Add keywords from the buffer's edit mode to the completions list upon start [false]");
			addComponent("Complete keywords defined by syntax highlighting rules", isLoadModeKeywords);

			isLoadMainModeOnly = new JCheckBox();
			isLoadMainModeOnly.setToolTipText("Only add keywords from the main edit mode (saves memory) [false]");
			addComponent(" Consider only keywords from the main edit mode", isLoadMainModeOnly);

			isLoadModeKeywords.addChangeListener(new ChangeListener(){
				public void stateChanged(ChangeEvent e)
				{
					isLoadMainModeOnly.setEnabled(
							isLoadModeKeywords.isSelected());
				}
			});
		}

		//----------------------------------------------------------------
		addSeparator(TextAutocompletePlugin.PROPS_PREFIX + "options.control-keys.label");

		//JEditorPane pane = new  JEditorPane();
		JTextArea pane = new JTextArea();
		pane.setLineWrap(true);
		pane.setWrapStyleWord(true);
		// pane.setFont(new java.awt.Font("Dialog", 0, 10));
		pane.setFont( this.getFont() );
		//pane.setEditorKit( pane.getEditorKitForContentType("text/html") );	// switch to html
		pane.setEditable(false);
		pane.setBackground(this.getBackground());
		/*pane.setText("<html><p>Keys to control the completion pop-up window:<br>" +
				"A list of names of constants of the class java.awt.event.KeyEvent separated by a space or comma. " +
				"Only special, non-displayable keys such as arrows, Esc and Enter work.</p>" +
				"Example: <code>VK_SPACE VK_ENTER</code></html>");*/
		pane.setText("Keys to control the completion pop-up window:\n" +
				"A list of names of constants of the class java.awt.event.KeyEvent separated by spaces or commas. " +
				"Only special, non-displayable keys such as arrows, Esc and Enter work.\n" +
				"Example: VK_SPACE VK_ENTER");
		addComponent(pane, GridBagConstraints.BOTH);

		acceptKeys = new JTextField();
		acceptKeys.setToolTipText("Key to insert selected completion. (Only non-displayable keys such as Enter.) [VK_ENTER VK_TAB]");
		addComponent("Accept with key             ", acceptKeys);

		disposeKeys = new JTextField();
		disposeKeys.setToolTipText("Key to hide the completions popup. (Only non-displayable keys such as Escape.) [VK_ESCAPE]");
		addComponent("Dispose with key            ", disposeKeys);

		selectionUpKeys = new JTextField();
		selectionUpKeys.setToolTipText("Key to select the completion above. (Only non-displayable keys such as Up arrow.) [VK_UP]");
		addComponent("Up in completions key       ", selectionUpKeys);

		selectionDownKeys = new JTextField();
		selectionDownKeys.setToolTipText("Key to select the completion below. (Only non-displayable keys such as Down arrow.) [VK_DOWN]");
		addComponent("Down in completions key ", selectionDownKeys);

		isSelectionByNumberEnabled = new JCheckBox();
		isSelectionByNumberEnabled.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e)
			{
				selectionByNumberModifierMask.setEnabled(
						isSelectionByNumberEnabled.isSelected());
			}

		});
		isSelectionByNumberEnabled.setToolTipText("Is it possible to select an offered completion by typing its number? [yes]");
		addComponent("Select compl. by number ", isSelectionByNumberEnabled);

		selectionByNumberModifierMask = new JComboBox();
		selectionByNumberModifierMask.setModel(new DefaultComboBoxModel(
				new String[] { "None", "Alt (left)", "AltGr (right)", "Ctrl" }));
		selectionByNumberModifierMask.setToolTipText("Additional key that must be pressed together with the number N to select the Nth completion. Some may not work! [none]");
		addComponent("Select by number modifier   ", selectionByNumberModifierMask);

		//----------------------------------------------------------------
		addSeparator();

		URL defWordList = PreferencesManager.getPreferencesManager().getDefaultWordListForBuffer("", false);
		addComponent(new JLabel("<html><p>Default word list for buffer (not yet configurable)</p>" +
				"<ul><li>General word list: " + defWordList +
				"</li><li>Buffer extension-specific word list: Same as above + the extension in lowercase<br>" +
				"Ex: <code>" + defWordList + ".php</code>" +
				"</li></html>"));

		//----------------------------------------------------------------
		addSeparator();

		resetButton = new javax.swing.JButton();
		resetButton.setText("Reset options");
		resetButton.addActionListener( new java.awt.event.ActionListener()
			{
            	public void actionPerformed(java.awt.event.ActionEvent evt)
            	{ resetToDefault(); }
        });
		addComponent(resetButton);

		// Display the values
		redisplayValues();
	}

	/* Save & aspply the changes to the properties. */
	protected void _save() {

		// Set properties
		{
			String propertyValue = null;

			setJEditProperty("isStartForBuffers", isStartForBuffers.isSelected());

			setJEditProperty("isInclusionFilter", isInclusionFilter.isSelected());
			setJEditProperty("filenameFilter", filenameFilter.getText());

			propertyValue = maxCountOfWords.getText();
			if( isInteger("maxCountOfWords", propertyValue) )
			{ setJEditProperty("maxCountOfWords", propertyValue); }

			propertyValue = isWordCode.getText();
			if(isValueSet(propertyValue))
			{ propertyValue = PreferencesManager.sanitizeCode(propertyValue); }
			setJEditProperty("isWord-code", propertyValue);

			propertyValue = minPrefixLength.getText();
			if( isInteger("minPrefixLength", propertyValue) )
			{ setJEditProperty("minPrefixLength", propertyValue); }

			propertyValue = acceptKeys.getText();
			setJEditProperty("acceptKey", propertyValue);

			propertyValue = disposeKeys.getText();
			setJEditProperty("disposeKey", propertyValue);

			propertyValue = selectionUpKeys.getText();
			setJEditProperty("selectionUpKey", propertyValue);

			propertyValue = selectionDownKeys.getText();
			setJEditProperty("selectionDownKey", propertyValue);

			propertyValue = isWordToRememberCode.getText();
			if(isValueSet(propertyValue))
			{ propertyValue = PreferencesManager.sanitizeCode(propertyValue); }
			setJEditProperty("isWordToRemember-code", propertyValue);

			propertyValue = minWordToRememberLength.getText();
			if(isInteger("minWordToRememberLength", propertyValue))
			{ setJEditProperty("minWordToRememberLength", propertyValue); }

			setJEditProperty("isLoadModeKeywords", isLoadModeKeywords.isSelected());
			setJEditProperty("isLoadMainModeOnly", isLoadMainModeOnly.isSelected());

			setJEditProperty("isSelectionByNumberEnabled", isSelectionByNumberEnabled.isSelected());

			// selectionByNumberModifierMask
			propertyValue = minPrefixLength.getText();
			int modifier = 0;
			int selectedInd = selectionByNumberModifierMask.getSelectedIndex(); // selectionByNumberModifierMask
			switch (selectedInd) {
				case 0:
					modifier = 0;
					break;
				case 1:
					modifier = InputEvent.ALT_MASK;
					break;
				case 2:
					modifier = InputEvent.ALT_GRAPH_MASK;
					break;
				case 3:
					modifier = InputEvent.CTRL_MASK;
					break;
			}
			setJEditProperty("selectionByNumberModifierMask", String.valueOf(modifier));
		}

		// #######################################################################
		// Notify the PreferencesManager that options have changed
		PreferencesManager.getPreferencesManager().optionsChanged();
	}

	/**
	 * Return true if the propertyValue is convertable to an integer.
	 * @param propertyName Property name to display in an error dialog
	 * @param propertyValue the value that should be an integer
	 */
	private boolean isInteger(final String propertyName, final String propertyValue) {
		if(isValueSet(propertyValue))
		{
			try
			{ Integer.parseInt(propertyValue.trim()); }
			catch(NumberFormatException nf)
			{
				GUIUtilities.error(null, TextAutocompletePlugin.PROPS_PREFIX + "errorMessage",
					new Object[]{ propertyName + " must be an integer but is '"+propertyValue+"'" });
				return false;
			}
		}
		return true;
	}

	private void redisplayValues()
	{
		isStartForBuffers.setSelected(
				PreferencesManager.getPreferencesManager().isStartForBuffers() );

		isInclusionFilter.setSelected(
				PreferencesManager.getPreferencesManager().isInclusionFilter() );
		isExclusionFilter.setSelected(
				PreferencesManager.getPreferencesManager().isExclusionFilter() );
		filenameFilter.setText(
				PreferencesManager.getPreferencesManager().getFilenameFilter() );

		maxCountOfWords.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "maxCountOfWords") );

		isWordCode.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "isWord-code") );

		minPrefixLength.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "minPrefixLength")
				);
		acceptKeys.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "acceptKey")
				);
		disposeKeys.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "disposeKey")
				);
		selectionUpKeys.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "selectionUpKey")
				);
		selectionDownKeys.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "selectionDownKey")
				);
		isWordToRememberCode.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "isWordToRemember-code")
				);
		minWordToRememberLength.setText(
				getJEditProperty(TextAutocompletePlugin.PROPS_PREFIX + "minWordToRememberLength")
				);

		isLoadModeKeywords.setSelected(
				PreferencesManager.getPreferencesManager().isLoadModeKeywords());
		isLoadMainModeOnly.setSelected(
				PreferencesManager.getPreferencesManager().isLoadMainModeOnly());
		isLoadMainModeOnly.setEnabled(
				isLoadModeKeywords.isSelected());

		isSelectionByNumberEnabled.setSelected(
				PreferencesManager.getPreferencesManager().isSelectionByNumberEnabled());
//		if (! isSelectionByNumberEnabled.isSelected())
//		{ selectionByNumberModifierMask.setEnabled(false); }

		// selectionByNumberModifierMask
		int selectedInd;
		int mask = PreferencesManager.getPreferencesManager().getSelectionByNumberModifier();
		switch (mask) {
			case InputEvent.ALT_MASK:
				selectedInd = 1;
				break;
			case InputEvent.ALT_GRAPH_MASK:
				selectedInd = 2;
				break;
			case InputEvent.CTRL_MASK:
				selectedInd = 3;
				break;
			default:
				selectedInd = 0;
		}
		selectionByNumberModifierMask.setSelectedIndex(selectedInd);
	}

	/** Reset all options to the default values. */
	public void resetToDefault()
	{
		String[] properties = new String[] {
				"isStartForBuffers",
				"maxCountOfWords",
				"isWord-code",
				"minPrefixLength",
				"acceptKey",
				"disposeKey",
				"selectionUpKey",
				"selectionDownKey",
				"isWordToRemember-code",
				"minWordToRememberLength",
				"isSelectionByNumberEnabled",
				"selectionByNumberModifierMask",
				"isInclusionFilter",
				"filenameFilter",
				"isLoadModeKeywords",
				"isLoadMainModeOnly"
		};

		for (int i = 0; i < properties.length; i++) {
			jEdit.unsetProperty(TextAutocompletePlugin.PROPS_PREFIX + properties[i]);
		}

		// Display the new properties
		redisplayValues();
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////

	/** What to display as a value for a property that is not set. */
	private static String UNSET_PROP = "<default - see documentation>";

	/** Return jEdit property or, if it isn't set, a default string. */
	private String getJEditProperty(String property)
	{ return jEdit.getProperty(property, UNSET_PROP); }

	//////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Set jEdit property if the value has been set.
	 * @param property The property name without the common prefix
	 * @value The value to set
	 */
	private boolean setJEditProperty(String property, String value)
	{
		if( isValueSet(value) )
		{
			jEdit.setProperty(TextAutocompletePlugin.PROPS_PREFIX + property, value);
			return true;
		}
		else
		{ return false; }
	}

	/** Set jEdit property if the value has been set.
	 * @param property The property name without the common prefix
	 * @value The value to set
	 */
	private boolean setJEditProperty(String property, boolean value)
	{
		jEdit.setBooleanProperty(TextAutocompletePlugin.PROPS_PREFIX + property, value);
		return value;
	}

	//////////////////////////////////////////////////////////////////////////////////////

	/** True if the display value != the "property unset" value. */
	final private boolean isValueSet(String value)
	{ return (value != null) && (value.trim().length() > 0) && (! value.equals(UNSET_PROP)); }

	/*
	public static void main(String[] args) {
		java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {

            	TextAutocompletePane pane = new TextAutocompletePane();
            	pane._init();
            	pane.setSize(600, 600);

            	javax.swing.JFrame frame = new javax.swing.JFrame("PAutocomplPaneTest");
            	javax.swing.JFrame.setDefaultLookAndFeelDecorated(true);
                frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
                frame.getContentPane().add( pane );
            	frame.add( pane );
            	frame.pack();
        		frame.setVisible(true);
            }
		});
		System.out.println("Done!");
	}
	*/

}
