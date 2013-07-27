package com.patelsoft.fastopen;

import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.gjt.sp.jedit.gui.HistoryTextField;

@SuppressWarnings("serial")
public class FastOpenTextField extends HistoryTextField
{
	/**
	 * Creates a new history text field.
	 * @param name The history model name
	 * @param instantPopups If true, selecting a value from the history
	 * popup will immediately fire an ActionEvent. If false, the user
	 * will have to press 'Enter' first
	 * @param enterAddsToHistory If true, pressing the Enter key will
	 * automatically add the currently entered text to the history.
	 *
	 */
	public FastOpenTextField(String name, boolean instantPopups, boolean enterAddsToHistory)
	{
		super(name,instantPopups, enterAddsToHistory);
		setSelectAllOnFocus();

	}

	protected void processKeyEvent(KeyEvent evt)
	{
		if(isEnabled())
		{
			if(evt.getID() == KeyEvent.KEY_PRESSED)
			{
				if(evt.getKeyCode() == KeyEvent.VK_UP && evt.isControlDown())
				{
					super.processKeyEvent(evt);
					return; //Can't call evt.consume() & continue the flow because HistoryTextField does not check for isConsumed() & we will see funny results of double action execution.
				}
				else if(evt.getKeyCode() == KeyEvent.VK_DOWN && evt.isControlDown())
				{
					super.processKeyEvent(evt);
					return;
				}
				else if(evt.getKeyCode() == KeyEvent.VK_TAB && evt.isControlDown())
				{
					super.processKeyEvent(evt);
					return;
				}
				else if(evt.getKeyCode() == KeyEvent.VK_UP && evt.getModifiers() == 0)
				{
					//Log.log(Log.DEBUG,this.getClass(),"Inside !consumed " +" Key pressed? "+((evt.getID() == KeyEvent.KEY_PRESSED)));
					processKeyBinding(KeyStroke.getKeyStroke("UP"),evt,JComponent.WHEN_IN_FOCUSED_WINDOW,(evt.getID() == KeyEvent.KEY_PRESSED));
					evt.consume();
					return;
				}
				else if(evt.getKeyCode() == KeyEvent.VK_DOWN && evt.getModifiers() == 0)
				{
					//Log.log(Log.DEBUG,this.getClass(),"Inside !consumed " +" Key pressed? "+((evt.getID() == KeyEvent.KEY_PRESSED)));
					evt.consume();
					processKeyBinding(KeyStroke.getKeyStroke("DOWN"),evt,JComponent.WHEN_IN_FOCUSED_WINDOW,(evt.getID() == KeyEvent.KEY_PRESSED));
					return;
				}
			}
			super.processKeyEvent(evt);
		}
	}//End of processKeyEvent
}

