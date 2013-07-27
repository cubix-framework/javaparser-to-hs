/*
 *  YesNoPrompt.java
 *  Copyright (c) 2006 Steve Jakob
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	 See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package velocity.directives;

import java.io.Writer;
import javax.swing.JOptionPane;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.runtime.parser.node.Node;

import org.gjt.sp.jedit.textarea.JEditTextArea;
import velocity.VelocityConstants;
/**
 * A directive to prompt the user to select one of two values. <p>
 *
 * When using the directive, the following are the arguments accepted:
 * <ul>
 *   <li> variable name (required)</li>
 *   <li> prompt string (required)</li>
 *   <li> the text to display for the "Yes" button (optional, default = "Yes")
 *   </li>
 *   <li> the text to display for the "No" button (optional, default = "No")
 *   </li>
 * </ul>
 * </p>
 *
 * @author   Steve Jakob
 */
public class YesNoPrompt extends SimpleDirective
	implements VelocityConstants
{

	/**
	 * Return name of this directive.
	 */
	public String getName()
	{
		return "yes_no";
	}

	/**
	 * Return type of this directive.
	 */
	public int getType()
	{
		return LINE;
	}

	/**
	 * Prompt the user for a value.
	 */
	public boolean render(InternalContextAdapter context,
			Writer writer, Node node)
			throws MethodInvocationException
	{
		// Retrieve prompt
		Object prompt = getRequiredValue(node, 0, "label", context);
		if (prompt == null)
		{
			return false;
		}
		// Retrieve variable name
		String key = getRequiredVariable(node, 1, "key");
		// Retrieve optional "yes" text
		Object yesString = getOptionalValue(node, 2, context);
		if (yesString == null)
		{
			yesString = "Yes";
		}
		// Retrieve optional "no" text
		Object noString = getOptionalValue(node, 3, context);
		if (noString == null)
		{
			noString = "No";
		}

		// Prompt the user
		JEditTextArea textArea = (JEditTextArea) context.get(TEXT_AREA);
		Object[] options = {yesString, noString};
		int n = JOptionPane.showOptionDialog(textArea, prompt,
				"Select desired option",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				null);

		// Set the variable's value in the context
		if (n == JOptionPane.YES_OPTION)
			context.getInternalUserContext().put(key, new Boolean(true));
		else
			context.getInternalUserContext().put(key, new Boolean(false));

		return true;
	}

}

