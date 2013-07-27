// jedit mode line :folding=explicit:collapseFolds=1:
package superabbrevs;

import java.util.*;
import java.io.*;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.bsh.*;
import org.gjt.sp.jedit.bsh.ParseException;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.textarea.TextArea;
import org.gjt.sp.util.Log;
import superabbrevs.gui.AddAbbrevDialog;
import superabbrevs.template.*;
import superabbrevs.zencoding.html.*;
import superabbrevs.zencoding.ZenParser;
import superabbrevs.zencoding.html.TokenMgrError;

import javax.annotation.Nonnull;
import javax.swing.JOptionPane;


/**************************************************************
 *            This class needs serious refactoring            *
 **************************************************************/
public class SuperAbbrevs {

	//{{{ load and save abbrevs

	// caching of the abbreviations
	private static Hashtable modeAbbrevs = new Hashtable();

	/**
	 * Adds an abbreviation to the global abbreviation list.
	 * @param abbrev The abbreviation
	 * @param expansion The expansion
	 */
	public static void addGlobalAbbrev(String abbrev, String expansion){
		addModeAbbrev("global",abbrev,expansion);
	}

	/**
	 * Adds a mode-specific abbrev.
	 * @param mode The edit mode
	 * @param abbrev The abbrev
	 * @param expansion The expansion
	 */
	public static void addModeAbbrev(String mode, String abbrev, String expansion){
		Hashtable abbrevs = (Hashtable)modeAbbrevs.get(mode);

		if (abbrevs==null){
			// try to read abbrevs from file
			abbrevs = SuperAbbrevsIO.readModeFile(mode);

			if (abbrevs == null){
				// if the abbrevs is not defined, define them
				abbrevs = new Hashtable();
			}
			modeAbbrevs.put(mode,abbrevs);
		}

		abbrevs.put(abbrev,expansion);

		SuperAbbrevsIO.writeModeFile(mode,abbrevs);
	}

	public static void saveAbbrevs(String mode, Hashtable abbrevs){
		modeAbbrevs.put(mode,abbrevs);
		SuperAbbrevsIO.writeModeFile(mode,abbrevs);
	}

	public static Hashtable loadAbbrevs(String mode){
		Hashtable abbrevs = (Hashtable)modeAbbrevs.get(mode);
		if(abbrevs == null){
			abbrevs = SuperAbbrevsIO.readModeFile(mode);
			// cache abbrevs
			if (abbrevs != null) modeAbbrevs.put(mode,abbrevs);
		}
		return abbrevs;
	}

	private static String getTemplateString(String mode, String abbrev) {

		// get the template of the abbreviation in the current mode
		Hashtable abbrevs = loadAbbrevs(mode);

		if (abbrevs == null || !abbrevs.containsKey(abbrev)){
			// if the template doesn't exists try the global mode
			abbrevs = loadAbbrevs("global");
		}

		if (abbrevs != null){
			return (String)abbrevs.get(abbrev);
		} else {
			return null;
		}
	}

	//}}}

	//{{{ load and save abbrevs

	// caching of the variables
	private static Hashtable variables = null;

	public static void saveVariables(Hashtable variables){
		SuperAbbrevs.variables = variables;
		SuperAbbrevsIO.writeModeFile("global.variables",variables);
	}

	public static Hashtable loadVariables(){
		if(variables == null){
			variables = SuperAbbrevsIO.readModeFile("global.variables");
		}
		return variables;
	}

	//}}}

	//{{{ key handlers
	/**
	 * Method tab(View view, JEditTextArea textArea, Buffer buffer)
	 * The method that desides what action should be taken for the tab key
	 */
	public static void tab(View view, JEditTextArea textArea, Buffer buffer) {

		if (!textArea.isEditable()){
			// beep if the textarea is not editable
			textArea.getToolkit().beep();

		} else if(Handler.enabled(buffer)){
			// If we already is in template mode, jump to the next field
			nextAbbrev(textArea,buffer);
		} else if(0 < textArea.getSelectionCount()){
			// If there is a selection in the buffer use the default behavior
			// for the tab key
			textArea.insertTabAndIndent();
		} else {
			String mode = getMode(textArea, buffer);
			String abbrev = getAbbrev(textArea, buffer);
			// if the abbreviation is empty we use the default behavior for the
			// tab key
			String template = null;
			if (abbrev.trim().isEmpty())
			{
				abbrev = getZenCodingAbbrevs(textArea, buffer, mode);
				if (!abbrev.isEmpty())
				{
					template = getZenCodingTemplate(abbrev, mode);
					if (template == null)
					{
						textArea.insertTabAndIndent();
						return;
					}
				}
			}
			else
			{
				template = getTemplateString(mode, abbrev);
			}


			if (template == null)
			{
				abbrev = getZenCodingAbbrevs(textArea, buffer, mode);
				if (!abbrev.isEmpty())
					template = getZenCodingTemplate(abbrev, mode);
			}

			if(template!=null){
				// Support for soft tabs
				if(buffer.getBooleanProperty("noTabs")) {
					// we will replace tab with spaces
					int tabSize = buffer.getTabSize();
					template = template.replaceAll("\t", spaces(tabSize));
				}

				try {
					buffer.beginCompoundEdit();
					// remove the abbrevation from the buffer
					removeAbbrev(textArea, buffer, abbrev);

					Hashtable modeVariables = loadVariables();

					// Expand the abbreviation
					expandAbbrev(view, template, modeVariables);
				}
				finally {
					buffer.endCompoundEdit();
				}
			} else {
				// If there no template exist use the default behavior
				// for the tab key
				textArea.insertTabAndIndent();
			}
		}
	}
	
	private static String getZenCodingTemplate(String abbrev, String mode)
	{
		String template = null;
		ZenParser zenParser = new HTMLZenParser(new StringReader(abbrev));
		try
		{
			template = zenParser.parse(new MLSerializer(mode, jEdit.getProperties()));
		}
		catch (TokenMgrError e)
		{
		}
		catch (Exception e)
		{
			Log.log(Log.WARNING, SuperAbbrevs.class, e);
		}
		return template;
	}

	@Nonnull
	private static String getZenCodingAbbrevs(TextArea textArea, Buffer buffer, CharSequence mode)
	{
		if (jEdit.getBooleanProperty("options.superabbrevs.zencoding") &&
		    jEdit.getProperty("options.superabbrevs.zencoding.modes").contains(mode))
		{
			String abbrev = buffer.getLineText(textArea.getCaretLine()).trim();
			abbrev = stripTags(abbrev);
			return abbrev;
		}
		return "";
	}

	private static String stripTags(String abbrev)
	{
		int closedAt = -1;
		for (int i = abbrev.length() - 1;i>=0;i--) {
			char c = abbrev.charAt(i);
			if (c == '>')
				closedAt = i;
			if (closedAt != -1)
			{
				if (c == '<')
				{
					abbrev = abbrev.substring(closedAt + 1);
					break;
				}
			}

		}
		return abbrev;
	}

	/**
	 * @return a string containing the number of spaces specified by the given
	 * size.
	 */
    private static String spaces(int size) {
        StringBuilder buf = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            buf.append(' ');
        }
        return buf.toString();
    }

	/**
	 * Method shiftTab(View view, JEditTextArea textArea, Buffer buffer)
	 * desides what action to take for the shift tab key
	 */
	public static void shiftTab(View view, JEditTextArea textArea, Buffer buffer) {

		if (Handler.enabled(buffer)){
			// If we already is in template mode, jump to the previous field
			SuperAbbrevs.prevAbbrev(textArea,buffer);
		} else if(0 < textArea.getSelectionCount()){
			// If there is a selection in the buffer use the default behavior
			// for the shift tab key
			textArea.shiftIndentLeft();
		} else {
			String abbrev = getAbbrev(textArea, buffer);
			if (abbrev.trim().isEmpty())
			{
				textArea.shiftIndentLeft();
			}
			else
			{
				Hashtable abbrevs = loadAbbrevs(getMode(textArea, buffer));

				String expansion = "";
				if (abbrevs != null && abbrevs.get(abbrev) != null)
				{
					expansion = (String) abbrevs.get(abbrev);
				}

				AddAbbrevDialog dialog = new AddAbbrevDialog(view, abbrev, expansion);
			}
		}
	}

	//}}}

	//{{{ expand abbreviations

	public static void expandAbbrev(View view) {
		Buffer buffer = view.getBuffer();
		JEditTextArea textArea = view.getTextArea();

		if (!textArea.isEditable()){
			// beep if the textarea is not editable
			textArea.getToolkit().beep();
		} else if(Handler.enabled(buffer)){
			// If we already is in template mode, beep
			textArea.getToolkit().beep();
		} else {
			// get the abbrevation before the caret
			String abbrev = getAbbrev(textArea, buffer);

			// if the abbreviation is empty, beep
			if (abbrev.trim().isEmpty()){
				textArea.getToolkit().beep();
				return;
			}

			String mode = getMode(textArea, buffer);

			String template = getTemplateString(mode, abbrev);

			if(template!=null){
				// remove the abbrevation from the buffer
				removeAbbrev(textArea, buffer, abbrev);

				Hashtable modeVariables = loadVariables();

				// Expand the abbreviation
				expandAbbrev(view, template, modeVariables);
			} else {
				// If there no template exist, beep
				textArea.getToolkit().beep();
			}
		}
	}

	/**
	 * Expands the abbrev at the caret position in the specified
	 * view.
	 * @param view The view
	 */
	public static void expandAbbrev(View view, String template,
		Hashtable variables){

		Buffer buffer = view.getBuffer();
		JEditTextArea textArea = view.getTextArea();

		// the offset of the caret in the full text
		int caretPos = textArea.getCaretPosition();

		// indent the template as the current line
		String indent = getIndent(textArea, buffer);

		try{
			Interpreter interpreter = new Interpreter();

			String selection = "";
			if(textArea.getSelectionCount() == 1){
				selection = textArea.getSelectedText();
				//escape all \
				selection = selection.replaceAll("\\\\","\\\\\\\\");
				textArea.setSelectedText("");
				// the offset of the caret in the full text
				caretPos = textArea.getCaretPosition();
			}

			interpreter.set("filename", buffer.getName());
			interpreter.set("selection", selection);

			// put the user defined variables into the interpreter
			putVariables(interpreter,variables);

			Template t = TemplateFactory.createTemplate(template, interpreter, indent);
			t.setOffset(caretPos);

			// insert the template in the buffer
			textArea.setSelectedText(t.toString(), false);

			// select the current field in the template
			selectField(textArea, t.getCurrentField());

			Handler h = new Handler(t,textArea,buffer);
			Handler.putHandler(buffer,h);

			TemplateCaretListener.putCaretListener(textArea, new TemplateCaretListener());
		} catch ( TargetError e ) {
			// The script threw an exception
			System.out.println("TargetError");
			System.out.println(e.getMessage());
		} catch ( ParseException e ) {
			// Parsing error
			System.out.println("ParseException");
			System.out.println(e.getMessage());
		} catch ( EvalError e ) {
			// General Error evaluating script
			System.out.println("EvalError");
			System.out.println(e.getErrorLineNumber());
			System.out.println(e.getMessage());
		} catch ( IOException e){
			// Input output error
			System.out.println("IOException");
			System.out.println(e.getMessage());
		}
	}

	//}}}

	//{{{ show abbreviations dialog

	/**
	 * Method showAbbrevDialog(View view, JEditTextArea textArea, Buffer buffer)
	 * show a dialog to type in the abbrev
	 */
	public static void showAbbrevDialog(View view, JEditTextArea textArea, Buffer buffer) {
		// TODO replace with a live search

		if(!textArea.isEditable() ||
		 	1 < textArea.getSelectionCount() ||
		 	Handler.enabled(buffer)){

		 	textArea.getToolkit().beep();
			return;
		}

		String abbrev = JOptionPane.showInputDialog(view, "Type in an abbreviation", "Abbreviation Input", JOptionPane.INFORMATION_MESSAGE);
		if(abbrev != null && !abbrev.trim().equals("")){

			String mode = getMode(textArea, buffer);

			String template = getTemplateString(mode, abbrev);
			if(template!=null){
				Hashtable variables = loadVariables();
				expandAbbrev(view,template,variables);
			}
		}
	}

	//}}}

	//{{{ abbreviation navigation

	public static void nextAbbrev(JEditTextArea textArea, Buffer buffer){
		Handler h = Handler.getHandler(buffer);
		Template t = h.getTemplate();

		if (t != null){
			TemplateCaretListener listener =
				TemplateCaretListener.removeCaretListener(textArea);
			t.nextField();
			SelectableField f = t.getCurrentField();
			if (f!=null){
				int start = f.getOffset();
				int end = start + f.getLength();
				textArea.setCaretPosition(end);
				textArea.addToSelection(new Selection.Range(start,end));
			}
			TemplateCaretListener.putCaretListener(textArea,listener);
		}

	}

	public static void prevAbbrev(JEditTextArea textArea, Buffer buffer){
		Handler h = Handler.getHandler(buffer);
		Template t = h.getTemplate();

		if (t != null){
			TemplateCaretListener listener =
				TemplateCaretListener.removeCaretListener(textArea);
			t.prevField();
			SelectableField f = t.getCurrentField();
			if (f!=null){
				int start = f.getOffset();
				int end = start + f.getLength();
				textArea.setCaretPosition(end);
				textArea.addToSelection(new Selection.Range(start,end));
			}
			TemplateCaretListener.putCaretListener(textArea,listener);
		}
	}

	//}}}

	//{{{ Text area helper methods

	public static String getMode(JEditTextArea textArea, Buffer buffer){
		// the offset of the caret in the full text
		int caretPos = textArea.getCaretPosition();

		// a string indication the mode of the current buffer
		return buffer.getRuleSetAtOffset(caretPos).getModeName();
	}

	/**
	 * Method selectField(JEditTextArea textArea, SelectableField field)
	 * Select the field in the buffer
	 */
	public static void selectField(JEditTextArea textArea, SelectableField field) {
		int  start = field.getOffset();
		int end = start + field.getLength();
		textArea.setCaretPosition(end);
		textArea.addToSelection(new Selection.Range(start,end));
	}

	/**
	 * Get the abbreviation before the caret
	 */
	@Nonnull
	private static String getAbbrev(JEditTextArea textArea, Buffer buffer){
		// in the following i will refere to the line where the caret resides
		// as the current line.

		// the line number of the current line
		int line = textArea.getCaretLine();
		// the start position of the current line in the full text
		int lineStart = buffer.getLineStartOffset(line);
		// the offset of the caret in the full text
		int caretPos = textArea.getCaretPosition();
		// the offset of the caret in the current line
		int caretPosition = caretPos - lineStart;

		// the text on the current line
		String lineText = buffer.getLineText(line);

		int i=caretPosition-1;
		while(0<=i && Character.isJavaIdentifierPart(lineText.charAt(i))){
			i--;
		}
		return lineText.substring(i+1,caretPosition);
	}

	private static String getIndent(JEditTextArea textArea, Buffer buffer){
		// the line number of the current line
		int lineNumber = textArea.getCaretLine();

		// the text on the current line
		String line = buffer.getLineText(lineNumber);

		int i=0;
		String output = "";
		while(i<line.length() && (line.charAt(i)==' ' || line.charAt(i)=='\t')){
			output = output + line.substring(i,i+1);
			i++;
		}
		return output;
	}

	/**
	 * Method removeAbbrev(Buffer buffer, String abbrev)
	 * Removes the abbreviation from the buffer
	 */
	public static void removeAbbrev(JEditTextArea textArea, Buffer buffer,
									String abbrev) {
		// the offset of the caret in the full text
		int caretPos = textArea.getCaretPosition();

		int templateStart = caretPos - abbrev.length();
		// remove the abbreviation
		buffer.remove(templateStart,abbrev.length());
	}

	//}}}
	//{{{ Defaults

	public static void makeDefaults(){
		SuperAbbrevsIO.createAbbrevsDir();
		SuperAbbrevsIO.removeOldMacros();
		SuperAbbrevsIO.writeDefaultAbbrevs();
		SuperAbbrevsIO.writeDefaultVariables();
		SuperAbbrevsIO.writeDefaultAbbrevFunctions();
		SuperAbbrevsIO.writeDefaultTemplateGenerationFunctions();
	}

	//}}}

	//{{{ put user defined variables into interpreter

	private static void putVariables(Interpreter interpreter,Hashtable variables)
		throws EvalError {
		if(variables != null){
			Iterator iter = variables.entrySet().iterator();
			while(iter.hasNext()){
				Map.Entry entry = (Map.Entry)iter.next();
				String name = (String)entry.getKey();
				String value = (String)entry.getValue();

				interpreter.set(name,value);
			}
		}
	}

	//}}}
}
