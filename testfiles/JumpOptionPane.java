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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.gui.FontSelector;
import org.gjt.sp.util.Log;

public class JumpOptionPane extends AbstractOptionPane implements ActionListener
{

    //{{{ fields
    private JTextField pathName;
    private JCheckBox enableJump;
    private JCheckBox parseOnSave;
    private JCheckBox sortFoldList;
    private JButton BrowseButt;
    private FontSelector font; //}}}

    //{{{ Constructor
    public JumpOptionPane()
    {
        super("JumpPlugin");
    } //}}}

    //{{{ actionPerformed
    public void actionPerformed(ActionEvent e)
    {
        //FileSelector
        Log.log(Log.DEBUG,this,"Action command - "+e.getActionCommand());
        if (e.getActionCommand().equals("chooser"))
        {
            _save();
            String[] path = GUIUtilities.showVFSFileDialog(jEdit.getActiveView(), new String(), VFSBrowser.OPEN_DIALOG, false);
            if (path.length >0 )
            {
                pathName.setText(path[0]);
                _save();
            }
        }

        if (e.getActionCommand().equals("Test"))
        {
            _save();
            if (JumpPlugin.isListenerAdded == false) JumpPlugin.init();
            String mess = new String();
            if (JumpPlugin.getListener().CtagsTest()==true) mess = "JumpPlugin.ctags.test.complete";
            else mess = "JumpPlugin.ctags.test.fail";
            JumpPlugin.getListener().errorMsg(mess);
        }
    } //}}}

    //{{{ void _init()
    public void _init()
    {
        setBorder(new EmptyBorder(15, 15, 15, 15));

        Dimension big = new Dimension(0,25);
        Dimension medium = new Dimension(0,15);

        enableJump = new JCheckBox(jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "enable.label"));
        enableJump.setSelected(jEdit.getBooleanProperty("jump.enable", false));
        addComponent(enableJump);
        addComponent(new Box.Filler(big,big,big));

        JPanel ctags_panel = new JPanel(new BorderLayout(8,8));

        JPanel inner_panel = new JPanel(new BorderLayout(8,8));
        ctags_panel.setOpaque(false);

        parseOnSave = new JCheckBox(jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "parse_on_save.label"));
        parseOnSave.setSelected(jEdit.getBooleanProperty("jump.parse_on_save", false));
        inner_panel.add(parseOnSave,BorderLayout.NORTH);

        JLabel lab = new JLabel(jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "ctags.def.path.label"));
        inner_panel.add(lab,BorderLayout.CENTER);

        JPanel path_panel = new JPanel(new BorderLayout(8,8));

        pathName = new JTextField(jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "ctags.def.path.label"), 45);
        pathName.setText(jEdit.getProperty("jump.ctags.path", jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "ctags.def.path")));

        BrowseButt = new JButton("...");
        BrowseButt.setActionCommand("chooser");
        BrowseButt.addActionListener(this);

        path_panel.add(pathName, BorderLayout.CENTER);
        path_panel.add(BrowseButt, BorderLayout.EAST);
        inner_panel.add(path_panel,BorderLayout.SOUTH);

        ctags_panel.add(inner_panel,BorderLayout.NORTH);

//****************** Fontselector *******************************   
        JPanel font_panel = new JPanel(new BorderLayout(8,8));
        JLabel lab1 = new JLabel("List font:");
        font = new FontSelector(getListFont());

        font_panel.add(lab1,BorderLayout.WEST);
        font_panel.add(font,BorderLayout.CENTER);

        ctags_panel.add(font_panel,BorderLayout.CENTER);
//***************************************************************  
        JPanel test_panel = new JPanel(new BorderLayout(8,8));

        JLabel lab2 = new JLabel("Test ctags:");

        JButton butt = new JButton("Test");
        butt.setActionCommand("Test");
        butt.addActionListener(this);
        test_panel.add(lab2,BorderLayout.WEST);
        test_panel.add(butt, BorderLayout.CENTER);
        ctags_panel.add(test_panel, BorderLayout.SOUTH);

        addComponent(ctags_panel);

        addComponent(new Box.Filler(medium, medium, medium));
        sortFoldList = new JCheckBox(jEdit.getProperty(JumpPlugin.OPTION_PREFIX + "sort_foldlist.label"));
        sortFoldList.setSelected(jEdit.getBooleanProperty("jump.sort_foldlist", true));
        addComponent(sortFoldList);
    } //}}}

    //{{{ void _save()
    public void _save()
    {
        jEdit.setProperty("jump.ctags.path", pathName.getText().trim());
        jEdit.setBooleanProperty("jump.enable", enableJump.isSelected());
        jEdit.setFontProperty("jump.list.font", font.getFont());
        jEdit.setBooleanProperty("jump.parse_on_save", parseOnSave.isSelected());
        jEdit.setBooleanProperty("jump.sort_foldlist", sortFoldList.isSelected());
    } //}}}

    //{{{ Font getListFont()
    private Font getListFont()
    {
        return (jEdit.getFontProperty("jump.list.font", new Font("Monospaced", Font.PLAIN, 11)));
    } //}}}
}
