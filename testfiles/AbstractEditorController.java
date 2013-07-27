//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.composer;

import java.awt.Font;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;
import javax.swing.JTextPane;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;

/**
 * This class serves as a common super class for the Editor Controllers used in
 * Composer: TextEditorController and HtmlEditorController. As such, it defines
 * the common interface needed by mainly the ComposerController.
 * <p>
 * It extends Observable to allow all actions to enable/disable themselves on
 * text selection changes.
 *
 * @author Karl Peder Olesen (karlpeder), 2003-09-06
 */
public abstract class AbstractEditorController extends Observable implements Observer {

	/** Reference to the controller */
	private ComposerController controller;

	private JTextPane view;

	  //	name of font
    private String name;

    // size of font
    private String size;

    // currently used font
    private Font font;

    // font configuration
    private XmlElement textFontElement;
    private XmlElement fonts;

    // overwrite look and feel font settings
    private boolean overwrite;

	/**
	 * Default constructor. Stores a reference to the controller
	 *
	 * @param ctrl
	 *            Controller controlling this object
	 */
	public AbstractEditorController(ComposerController ctrl) {
		controller = ctrl;

		XmlElement options = Config.getInstance().get("options").getElement("/options");
        XmlElement guiElement = options.getElement("gui");
        fonts = guiElement.getElement("fonts");

        if (fonts == null) {
            fonts = guiElement.addSubElement("fonts");
        }

        overwrite = Boolean.valueOf(fonts.getAttribute("overwrite", "true"))
                           .booleanValue();

        // register for configuration changes
        fonts.addObserver(this);

        textFontElement = fonts.getElement("text");

        if (textFontElement == null) {
            textFontElement = fonts.addSubElement("text");
        }

        if (overwrite) {
            name = "Default";
            size = "12";

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        } else {
            name = textFontElement.getAttribute("name", "Default");
            size = textFontElement.getAttribute("size", "12");

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        }
	}

	public void setView(JTextPane view) {
		this.view = view;
	}

	public JTextPane getView() {
		return view;
	}

	/**
	 * Returns the controller
	 */
	public ComposerController getController() {
		return controller;
	}

	// ********** Methods necessary to hide view from clients ********

	/**
	 * Sets the text of the editor view
	 *
	 * @param text
	 *            New text, which replaces the current view text
	 */
	public abstract void setViewText(String text);

	/** **************** FocusOwner implementation **************************** */

	// the following lines add cut/copy/paste/undo/redo/selectall
	// actions support using the Columba action objects.
	//
	// This means that we only have a single instance of these
	// specific actions, which is shared by all menuitems and
	// toolbar buttons.
	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isCutActionEnabled()
	 */
	public boolean isCutActionEnabled() {
		if (view.getSelectedText() == null) {
			return false;
		}

		if (view.getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isCopyActionEnabled()
	 */
	public boolean isCopyActionEnabled() {
		if (view.getSelectedText() == null) {
			return false;
		}

		if (view.getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isPasteActionEnabled()
	 */
	public boolean isPasteActionEnabled() {
		return true;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isDeleteActionEnabled()
	 */
	public boolean isDeleteActionEnabled() {
		if (view.getSelectedText() == null) {
			return false;
		}

		if (view.getSelectedText().length() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isSelectAllActionEnabled()
	 */
	public boolean isSelectAllActionEnabled() {
		return true;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isRedoActionEnabled()
	 */
	public boolean isRedoActionEnabled() {
		// TODO (@author karlpeder): Implementation of undo/redo missing
		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#isUndoActionEnabled()
	 */
	public boolean isUndoActionEnabled() {
		// TODO (@author karlpeder): Implementation of undo/redo missing
		return false;
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#cut()
	 */
	public void cut() {
		view.cut();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#copy()
	 */
	public void copy() {
		view.copy();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#paste()
	 */
	public void paste() {
		view.paste();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#delete()
	 */
	public void delete() {
		view.replaceSelection("");
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#redo()
	 */
	public void redo() {
		// TODO (@author karlpeder): Implementation of undo/redo missing
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#undo()
	 */
	public void undo() {
		// TODO (@author karlpeder): Implementation of undo/redo missing
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#selectAll()
	 */
	public void selectAll() {
		view.selectAll();
	}

	/**
	 * @see org.columba.core.gui.focus.FocusOwner#getComponent()
	 */
	public JComponent getComponent() {
		return view;
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#getViewUIComponent()
	 */
	public JTextPane getViewUIComponent() {
		return getView();
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#setViewEnabled(boolean)
	 */
	public void setViewEnabled(boolean enabled) {
		getView().setEnabled(enabled);
	}

	public void setCaretPosition(int position) {
		getView().setCaretPosition(position);
	}

	public void moveCaretPosition(int position) {
		getView().moveCaretPosition(position);
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#getViewFont()
	 */
	public Font getViewFont() {
		return getView().getFont();
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#setViewFont(java.awt.Font)
	 */
	public void setViewFont(Font f) {
		getView().setFont(f);
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#getViewText()
	 */
	public String getViewText() {
		return getView().getText();
	}

	/**
	 * @see org.columba.mail.gui.composer.AbstractEditorController#updateComponents(boolean)
	 */
	public void updateComponents(boolean b) {
		if (b) {
			if (this.getController().getModel().getBodyText() != null) {
				this.setViewText(this.getController().getModel().getBodyText());
			}
		} else {
			if (getView().getText() != null) {
				this.getController().getModel()
						.setBodyText(getView().getText());
			}
		}
	}

	/**
     * Gets fired when configuration changes occur.
     *
     * @see org.columba.core.gui.config.GeneralOptionsDialog
     *
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable arg0, Object arg1) {
        // fonts
        overwrite = Boolean.valueOf(fonts.getAttribute("overwrite", "true"))
                           .booleanValue();

        if (overwrite == false) {
            // use default font settings
            name = "Default";
            size = "12";

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));
        } else {
            // overwrite look and feel font settings
            name = textFontElement.getAttribute("name", "Default");
            size = textFontElement.getAttribute("size", "12");

            font = new Font(name, Font.PLAIN, Integer.parseInt(size));

            setViewFont(font);
        }
    }
}
