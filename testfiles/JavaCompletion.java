/*
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
package sidekick.java;

//{{{ Imports
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import sidekick.java.node.*;
import sidekick.SideKickPlugin;
import sidekick.SideKickCompletion;
import sidekick.SideKickParsedData;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.*;
//}}}

public class JavaCompletion extends SideKickCompletion {

	// PARTIAL means replace all of the text
	public static final int PARTIAL = 2;

	// DOT type means to replace everything after the last dot in the text
	public static final int DOT = 3;

	// CONSTRUCTOR means insert everything after the first parenthese
	public static final int CONSTRUCTOR = 4;

	// PACKAGE means package-insertion or import
	public static final int PACKAGE = 5;

	private int insertionType = PARTIAL;

	//{{{ JavaCompletion(View, String, List)
	public JavaCompletion(View view, String text, List choices) {
		super(view, text, choices);
		determineInsertionType();
	} //}}}

	//{{{ JavaCompletion(View, String, int, List)
	public JavaCompletion(View view, String text, int type, List choices) {
		super(view, text, choices);
		this.insertionType = type;
	} //}}}

	//{{{ getChoices() : List
	public List getChoices() {
		return super.items;
	} //}}}

	//{{{ determineInsertionType() : void
	private void determineInsertionType() {
		if (text.endsWith("."))
			insertionType = DOT;
		else
			insertionType = PARTIAL;
	} //}}}

	//{{{ setInsertionType(int) : void
	public void setInsertionType(int type) {
		insertionType = type;
	} //}}}

	//{{{ getRenderer() : ListCellRenderer
	public ListCellRenderer getRenderer() {
		return new JavaCompletionRenderer();
	} //}}}

	//{{{ insert(int) : void
	public void insert( int index ) {
		String to_replace = text;
		String to_insert = String.valueOf(get(index));

		// Trim the return type
		if (to_insert.lastIndexOf(':') != -1) {
			to_insert = to_insert.substring(0, to_insert.lastIndexOf(':')).trim();
		}

		if (insertionType == DOT) {
			int dot_index = text.lastIndexOf('.');
			if (dot_index > 0)
				to_replace = text.substring(dot_index + 1);
		}
		else if (insertionType == CONSTRUCTOR) {
			to_replace = "(";
			to_insert = to_insert.substring(to_insert.indexOf('('));
		}
		else if (insertionType == PARTIAL) {
			// Do nothing, as the whole word will get replaced
		}
		else if (insertionType == PACKAGE) {
			if (jEdit.getBooleanProperty("sidekick.java.importPackage")) {
				// Insert the import statement and return
				insertImport(textArea, to_insert);
				return;
			} else {
				// Do nothing, as the whole word will get replaced
			}
		}

		int caret = textArea.getCaretPosition();
		Selection s = textArea.getSelectionAtOffset( caret );
		int start = ( s == null ? caret : s.getStart() );
		JEditBuffer buffer = textArea.getBuffer();

		try {
			buffer.beginCompoundEdit();
			buffer.remove( start - to_replace.length(), to_replace.length() );

			boolean useSuperAbbrevs = (jEdit.getPlugin("SuperAbbrevsPlugin") != null);
			if (useSuperAbbrevs)
				to_insert = to_insert.replace("$", "\\$");

			// If the text to insert has parameters and superabbrevs is installed, use it
			// Otherwise, insert up to the first parenthese (if any)
			int popen, pclose;
			if ((popen = to_insert.indexOf('(')) != -1 && (pclose = to_insert.indexOf(')')) != -1 && useSuperAbbrevs) {
				String params = to_insert.substring(popen + 1, pclose);
				to_insert = to_insert.substring(0, popen);
				StringTokenizer tokenizer = new StringTokenizer(params, ",");
				int token = 1;
				StringBuilder new_params = new StringBuilder();
				while (tokenizer.hasMoreTokens()) {
					new_params.append("${").append(token).append(":").append(tokenizer.nextToken()).append("}");
					if (tokenizer.hasMoreTokens())
						new_params.append(", ");
					token++;
				}
				to_insert += "(" + new_params.toString() + ")";
				superabbrevs.SuperAbbrevs.expandAbbrev(textArea.getView(), to_insert, null);
			}
			else {
				if (popen != -1) {
					to_insert = to_insert.substring(0, popen);
				}
				buffer.insert( start - to_replace.length(), to_insert );
			}
		}
		finally {
			buffer.endCompoundEdit();
		}
	} //}}}

	//{{{ insertImport(JEditTextArea, String) : void
	/**
	 * Insert an import directly into the top of the buffer
	 * @param textArea the text area to insert into
	 * @param cls the class to import
	 */
	public void insertImport(JEditTextArea textArea, String cls) {
		if (cls.indexOf(".") == -1) {
			// No need to import a class in the default package
			return;
		}
		JEditBuffer buffer = textArea.getBuffer();
		// Create the new import statement
		String newImport = "import "+cls+";\n";
		// Locate the start of the class
		int startOfClass = -1;
		SideKickParsedData data = SideKickParsedData.getParsedData(textArea.getView());
		for (int i = 0; i<data.root.getChildCount(); i++) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) data.root.getChildAt(i);
			Object ob = node.getUserObject();
			if (ob instanceof ClassNode) {
				startOfClass = ((ClassNode) ob).getStart().getOffset();
				break;
			}
		}
		// If no class is defined, return
		if (startOfClass == -1) return;
		// Locate the first import statement
		int classLine = buffer.getLineOfOffset(startOfClass);
		int startOfImports = -1;
		for (int i = 0; i<classLine; i++) {
			if (buffer.getLineText(i).startsWith("import ")) {
				startOfImports = i;
				break;
			}
		}
		if (startOfImports != -1) {
			// If an import statement was found
			boolean inserted = false;
			int endOfImports = startOfImports;
			// The root package is used to group imports
			String newRootPackage = newImport.substring(7,
					newImport.indexOf("."));
			String rootPackage = null;
			// Loop through the import statements, searching for the correct
			// spot to put it alphabetically
			for (int i = startOfImports; i<classLine; i++) {
				try {
					String importLine = buffer.getLineText(i);
					if (!importLine.startsWith("import ")) continue;
					if (newImport.equals(importLine+"\n")) {
						// The import already exists, return
						return;
					}
					endOfImports = i;
					rootPackage = importLine.substring(7,
							importLine.indexOf("."));
					if (importLine.compareTo(newImport) > 0) {
						buffer.insert(textArea.getLineStartOffset(i), newImport+
								((rootPackage.equals(newRootPackage)) ? "" : "\n"));
						inserted = true;
						break;
					}
				} catch (Exception e) {}
			}
			if (!inserted) {
				// If it's not inserted it yet, put it at the end
				buffer.insert(textArea.getLineStartOffset(endOfImports+1),
						((rootPackage.equals(newRootPackage)) ? "" : "\n") + newImport);
			}
		}
		else {
			// No import statements exist yet, so just insert it above the class
			buffer.insert(startOfClass, "\n"+newImport+"\n");
		}
		// Quietly re-parse after import in order to update imports and class starting location
		SideKickParsedData skpd = (new JavaParser()).parse((Buffer) buffer, null);
		SideKickParsedData.setParsedData( view , skpd );
	} //}}}

	//{{{ JavaCompletionRenderer
	class JavaCompletionRenderer extends DefaultListCellRenderer {

		//{{{ getListCellRendererComponent() : Component
		public Component getListCellRendererComponent(
				JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {

			JavaCompletionFinder.JavaCompletionCandidate candid =
				(JavaCompletionFinder.JavaCompletionCandidate) value;
			JLabel cmp = new JLabel(candid.toString()+"   ", candid.getIcon(), JLabel.LEFT);
			Font font = jEdit.getFontProperty("view.font");
			cmp.setFont(new Font(font.getName(),
						(candid.isProtected()) ? Font.ITALIC : Font.PLAIN,
						font.getSize()));
			cmp.setOpaque(true);
			if (isSelected) {
				cmp.setForeground(list.getSelectionForeground());
				cmp.setBackground(list.getSelectionBackground());
			}
			return cmp;
		} //}}}
	} //}}}

}
