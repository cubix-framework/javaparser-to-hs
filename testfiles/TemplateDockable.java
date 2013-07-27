/*
 * TemplateDockable.java
 * :tabSize=3:indentSize=3:noTabs=true:
 *
 * Copyright (c) 2002 Calvin Yu
 * Copyright (c) 2008 Steve Jakob
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
package templates;

import java.awt.BorderLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.TreePath;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;

/**
 * A dockable template tree..
 */
public class TemplateDockable extends JPanel
   implements MouseListener, ActionListener, EBComponent
{

   public final static String RELOAD = "reload";
   public final static String EDIT = "edit";
   public final static String SET_ACCELERATOR = "setAccelerator";

   private View view;
   private TemplateTree templates;

   /**
    * Create a new <code>TemplateDockable</code>.
    */
   public TemplateDockable(View aView)
   {
      super(new BorderLayout());
      view = aView;
      add(new JScrollPane(templates = new TemplateTree()));
      // Add ENTER key binding for TemplateTree component
      templates.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            "templates-tree-select-node");
      templates.getActionMap().put("templates-tree-select-node",
            new NodeSelectAction());

      templates.addMouseListener(this);
   }
   
   public boolean requestDefaultFocus() {
      templates.requestFocus();
      return true;
   }

	/**
	 * Register this object as a receiver for EditBus messages.
	 */

	public void addNotify() {
      super.addNotify();
		EditBus.addToBus(this);
	}

	/**
	 * Remove this object as a receiver for EditBus messages.
	 */

	public void removeNotify() {
      super.removeNotify();
		EditBus.removeFromBus(this);
	}
	
	/**
	 * Handle messages received by the jEdit EditBus.
	 * At this time, TemplatesDockable objects will respond only to 
	 * TemplatesChanged messages.
	 * @param msg An EBMessage object sent by the jEdit EditBus, to which 
	 * the TemplatesMenu object may wish to respond.
	 */
	public void handleMessage(EBMessage msg) {
		if (msg instanceof TemplatesChanged) {
		Log.log(Log.DEBUG,this,"... TemplateDockable.handleMessage()");
			templates.reload();
      }
	}

   /**
    * Process the selected template.
    */
   public void processSelectedTemplate()
   {
      TemplatesPlugin plugin = (TemplatesPlugin) jEdit.getPlugin("templates.TemplatesPlugin");
      plugin.processTemplate(templates.getSelectedTemplate(), view, view.getTextArea());
      view.getEditPane().getTextArea().requestFocus();
   }


   //{{{ MouseListener Method
   /**
    * Handle a mouse entered.
    */
   public void mouseEntered(MouseEvent evt)
   {
   }

   /**
    * Handle a mouse exited.
    */
   public void mouseExited(MouseEvent evt)
   {
   }

   /**
    * Handle a mouse press.
    */
   public void mousePressed(MouseEvent evt)
   {
      if (GUIUtilities.isPopupTrigger(evt)) {
         templates.setSelectionPath(templates.getPathForLocation(evt.getX(), evt.getY()));
         JPopupMenu popup = new JPopupMenu();

         JMenuItem edit = new JMenuItem(jEdit.getProperty("Templates.edit-template.label"));
         edit.setActionCommand(EDIT);
         edit.addActionListener(this);
         popup.add(edit);

         JMenuItem setAccelerator = new JMenuItem(jEdit.getProperty("Templates.set-accelerator.label"));
         setAccelerator.setActionCommand(SET_ACCELERATOR);
         setAccelerator.addActionListener(this);
         popup.add(setAccelerator);

         popup.addSeparator();

         JMenuItem reload = new JMenuItem(jEdit.getProperty("Templates.refresh-templates.label"));
         reload.setActionCommand(RELOAD);
         reload.addActionListener(this);
         popup.add(reload);
         popup.show(templates, evt.getX(), evt.getY());
      }
   }

   /**
    * Handle a mouse release.
    */
   public void mouseReleased(MouseEvent evt)
   {
   }

   /**
    * Handle a mouse click.
    */
   public void mouseClicked(MouseEvent evt)
   {
      if ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0 && evt.getClickCount() == 1) {
         TreePath path = templates.getPathForLocation(evt.getX(), evt.getY());
         if (templates.isLastPathComponentATemplate(path) && path.equals(templates.getSelectionPath())) {
            processSelectedTemplate();
         }
      }
   }
   //}}}

   //{{{ ActionListener Method
   /**
    * Handle a action.
    */
   public void actionPerformed(ActionEvent evt)
   {
      if (RELOAD.equals(evt.getActionCommand())) {
         TemplatesPlugin.refreshTemplates();
         templates.reload();
      } else if (EDIT.equals(evt.getActionCommand())) {
         jEdit.openFile(view, MiscUtilities.concatPath(TemplatesPlugin.getTemplateDir(),
                                                       templates.getSelectedTemplate()));
      } else if (SET_ACCELERATOR.equals(evt.getActionCommand())) {
         String mode =
            GUIUtilities.input(view, "plugin.TemplatesPlugin.set-accelerator.input-mode",
                               jEdit.getModes(), view.getEditPane().getBuffer().getMode().getName());
         if (mode == null) {
            return;
         }
         String accelerator =
            GUIUtilities.input(view, "plugin.TemplatesPlugin.set-accelerator.input-accelerator", null);
         if (accelerator == null) {
            return;
         }
         if (AcceleratorManager.getInstance().findTemplatePath(mode,
                                                               accelerator) != null)
         {
            int result =
               GUIUtilities.confirm(view, "plugin.TemplatesPlugin.set-accelerator.confirm-overwrite",
                                    new String[] {"accelerator"}, JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);
               if (result != JOptionPane.YES_OPTION) {
                  return;
               }
         }
         AcceleratorManager.getInstance().addAccelerator(mode, accelerator,
                                                         templates.getSelectedTemplate());
      }
   }
   //}}}

   public class NodeSelectAction extends AbstractAction
   {
      public void actionPerformed(ActionEvent e)
      {
         TreePath path = templates.getSelectionPath();
         if (templates.isLastPathComponentATemplate(path)) 
         {
            processSelectedTemplate();
         } else
         {
            templates.expandPath(path);
         }
      }
   }

}

