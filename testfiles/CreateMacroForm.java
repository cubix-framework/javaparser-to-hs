/*
 *  Copyright (c) 2008 TownsfolkDesigns.com
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package scripting.forms;

import scripting.ScriptEngineDelegate;
import scripting.ScriptEnginePlugin;

import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.jEdit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.script.ScriptEngineManager;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.gjt.sp.jedit.JARClassLoader;


/**
 *
 * @author elberry
 */
public class CreateMacroForm extends JPanel {

   private static final Dimension INPUT_FIELD_SIZE = new Dimension(300, 30);
   private static final long serialVersionUID = -507053874116374428L;

   private int dialogValue;
   private JLabel directionsLabel;
   private JTextField directoryField;
   private JLabel directoryLabel;
   private GridBagConstraints gbc;
   private JComboBox languageField;
   private JLabel languageLabel;
   private GridBagLayout layout;
   private JTextField nameField;
   private JLabel nameLabel;
   private boolean showModeOn;

   public CreateMacroForm() {
      initComponents();
      setShowModeOn(true);
   }

   public static void main(String[] args) {
		ScriptEngineManager manager = new ScriptEngineManager();
      CreateMacroForm form = new CreateMacroForm();
      form.show(null);
      form = new CreateMacroForm();
      form.setShowModeOn(false);
      form.show(null);
   }

   public int getDialogValue() {
      return dialogValue;
   }

   public String getDirectoryName() {
      return directoryField.getText();
   }

   public String getMacroName() {
      return nameField.getText();
   }

   public Mode getMode() {
      return (Mode) languageField.getSelectedItem();
   }

   public boolean isShowModeOn() {
      return showModeOn;
   }

   public void setDialogValue(int dialogValue) {
      this.dialogValue = dialogValue;
   }

   public void setDirectoryName(String directoryName) {
      directoryField.setText(directoryName);
   }

   public void setMacroName(String name) {
      nameField.setText(name);
   }

   public void setMode(Mode mode) {
      languageField.setSelectedItem(mode);
   }

   public void setShowModeOn(boolean showModeOn) {
      this.showModeOn = showModeOn;
   }

   public void show(Component parent) {
      initLayout();

      String dialogTitle = getMessage("scriptengine.plugin.macro.create.label", "Create Macro");
      dialogValue = JOptionPane.showConfirmDialog(parent, this, dialogTitle, JOptionPane.OK_CANCEL_OPTION, -1);
   }

   private String getMessage(String key, String defaultMessage, String... parameters) {
      String message = null;

      try {
         message = jEdit.getProperty(key);
      } catch (NullPointerException npe) {
         // jedit isn't set up. Forget it, use default message.
      }

      if (message == null) {
         message = defaultMessage;
      }

      return String.format(message, (Object[]) parameters);
   }

   private void initComponents() {
      nameLabel = new JLabel(getMessage("scriptengine.plugin.macro.create.form.name.label", "Name:"));
      directoryLabel = new JLabel(getMessage("scriptengine.plugin.macro.create.form.directory.label", "Directory:"));
      languageLabel = new JLabel(getMessage("scriptengine.plugin.macro.create.form.language.label", "Language:"));
      directionsLabel = new JLabel(getMessage("scriptengine.plugin.macro.create.form.directions",
            "<html><body>Spaces in the macro name will be replaced with underscores<br>and dirctory names are subject to the restrictions of your<br>Operating System.<br><br>" +
            "Macro files can be found under:<br>%1$s/macros/[directory]/[macro name]</body></html>",
            jEdit.getSettingsDirectory()));
      nameField = new JTextField();
      nameField.setSize(INPUT_FIELD_SIZE);
      nameField.setPreferredSize(INPUT_FIELD_SIZE);
      directoryField = new JTextField();
      directoryField.setSize(INPUT_FIELD_SIZE);
      directoryField.setPreferredSize(INPUT_FIELD_SIZE);
      languageField = new JComboBox(new ScriptEngineDelegate().getRegisteredModes().toArray());
      languageField.setSize(INPUT_FIELD_SIZE);
      languageField.setPreferredSize(INPUT_FIELD_SIZE);
   }

   private void initLayout() {
      layout = new GridBagLayout();
      gbc = new GridBagConstraints();
      setLayout(layout);

      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 0.0f;
      gbc.weighty = 0.0f;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.gridwidth = 2;
      layout.setConstraints(directionsLabel, gbc);
      add(directionsLabel);
      gbc.gridwidth = 1;
      gbc.gridy++;
      layout.setConstraints(nameLabel, gbc);
      add(nameLabel);
      gbc.gridy++;
      layout.setConstraints(directoryLabel, gbc);
      add(directoryLabel);

      if (isShowModeOn()) {
         gbc.gridy++;
         layout.setConstraints(languageLabel, gbc);
         add(languageLabel);
      }

      gbc.gridx = 1;
      gbc.gridy = 1;
      gbc.weightx = 1.0f;
      layout.setConstraints(nameField, gbc);
      add(nameField);
      gbc.gridy++;
      layout.setConstraints(directoryField, gbc);
      add(directoryField);

      if (isShowModeOn()) {
         gbc.gridy++;
         layout.setConstraints(languageField, gbc);
         add(languageField);
      }
   }
}
