// jedit mode line :folding=explicit:collapseFolds=1: 
package superabbrevs.gui;

import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;
import org.gjt.sp.jedit.*;

/*
 * I modified Slava Pestov code
 * @author Sune Simonsen
 */ 
public class AbbrevEditor extends JPanel
{
	//{{{ AbbrevEditor constructor
	public AbbrevEditor()
	{
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = cons.WEST;
		cons.fill = cons.BOTH;
		cons.weightx = 0.0f;
		cons.gridx = 1;
		cons.gridy = 1;

		JLabel label = new JLabel(jEdit.getProperty("abbrev-editor.abbrev"),
			SwingConstants.RIGHT);
		label.setBorder(new EmptyBorder(0,0,0,12));
		layout.setConstraints(label,cons);
		add(label);
		cons.gridx++;
		cons.weightx = 1.0f;
		abbrev = new JTextField();
		layout.setConstraints(abbrev,cons);
		add(abbrev);

		cons.gridx = 1;
		cons.weightx = 0.0f;
		cons.gridwidth = 2;

		cons.gridy++;
		label = new JLabel(jEdit.getProperty("SuperAbbrevs.abbrev-editor.template"));
		label.setBorder(new EmptyBorder(6,0,3,0));
		layout.setConstraints(label,cons);
		add(label);

		cons.gridy++;
		cons.weighty = 1.0f;
		template = new JTextArea(4,40);
		JScrollPane scroller = new JScrollPane(template);
		layout.setConstraints(scroller,cons);
		add(scroller);
		
		setPreferredSize(new Dimension(566, 300));
	} //}}}

	//{{{ getAbbrev() method
	public String getAbbrev()
	{
		return abbrev.getText();
	} //}}}

	//{{{ setAbbrev() method
	public void setAbbrev(String abbrev)
	{
		this.abbrev.setText(abbrev);
	} //}}}

	//{{{ getExpansion() method
	public String getExpansion()
	{
		return template.getText();
	} //}}}

	//{{{ setExpansion() method
	public void setExpansion(String expansion)
	{
		if(expansion == null) {
			template.setText(null);
		} else {
			template.setText(expansion);
		}
	} //}}}

	//{{{ getAbbrevField() method
	public JTextField getAbbrevField()
	{
		return abbrev;
	} //}}}

	//{{{ getBeforeCaretTextArea() method
	public JTextArea getTemplateTextArea()
	{
		return template;
	} //}}}

	//{{{ Private members
	private JTextField abbrev;
	private JTextArea template;
	//}}}
}
