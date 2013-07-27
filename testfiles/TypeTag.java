/*
 *  Jump plugin for jEdit
 *  Copyright (c) 2003 Pavlikus
 *
 *  :tabSize=4:indentSize=4:
 *  :folding=explicit:collapseFolds=1:
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

    //{{{ imports
package jump;
    import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.EnhancedDialog;
import org.gjt.sp.jedit.gui.HistoryTextField;

public class TypeTag extends EnhancedDialog
{

    //{{{ fields
    private HistoryTextField history;
    private TypeTagActionListener listener; //}}}

    //{{{ public TypeTag
    public TypeTag()
    {
        super (jEdit.getActiveView(), "Jump to tag", true);
        listener = new TypeTagActionListener();
    } //}}}

    //{{{ void prepareGUI()
    private void prepareGUI()
    {
        JPanel content = new JPanel(new BorderLayout(7,7));
		content.setBorder(new EmptyBorder(0,12,12,12));
		setContentPane(content);

        JLabel title = new JLabel("Type a tag to jump to");

        String ModelName ="jump.tag_history.project."+JumpPlugin.getActiveProjectBuffer().name;
        history = new HistoryTextField(ModelName, true, false);
        history.setColumns(35);

        JPanel buttons = new JPanel(new BorderLayout(7,7));
        JButton FindButt = new JButton("Find");
        FindButt.addActionListener(listener);
        FindButt.setActionCommand("find");

        buttons.add(FindButt, BorderLayout.EAST);
        buttons.add(history, BorderLayout.CENTER);

        content.add(title, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.CENTER);
    } //}}}

    //{{{ void _show()
    public void _show()
    {
        prepareGUI();

        Dimension screen = getToolkit().getScreenSize();
        Dimension size = new Dimension(getWidth(), getHeight());
        setLocation((screen.width - size.width) / 2,
                (screen.height - size.height) / 2);
        GUIUtilities.loadGeometry(this,"jump.type_tag");
        pack();
        setVisible(true);
        GUIUtilities.requestFocus(this,history);
    } //}}}

    //{{{ void ok()
    public void ok()
    {
        GUIUtilities.saveGeometry(this,"jump.type_tag");
        history.addCurrentToHistory();
        String search = history.getText().trim();
        dispose();
        JumpPlugin.pja.getTagBySelection(search);
    } //}}}

    //{{{ void cancel()
    public void cancel()
    {
        GUIUtilities.saveGeometry(this,"jump.type_tag");
        dispose();
    } //}}}

    //{{{ class HistoryActionListener
    private class TypeTagActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            if (e.getActionCommand().equals("find"))
            {
                ok();
            }
        }
    }
    //}}}

}
