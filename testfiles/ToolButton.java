/*
 *
 * Copyright (c)2004 Electronic Data Processing PLC. All Rights Reserved.
 *
 *		PROPRIETARY AND COPYRIGHT NOTICE.
 *
 * This Quantum VS software product contains information which is
 * proprietary to and considered a trade secret of Electronic Data
 * Processing PLC.
 *
 * It is expressly agreed that it shall not be reproduced in whole
 * or part, disclosed, divulged or otherwise made available to any
 * third party directly or indirectly, without written authorisation
 * from Electronic Data Processing PLC.
 * 
 * $Id: ToolButton.java,v 1.3 2004/09/10 11:37:59 t_magicthize Exp $
 */
package gruntspud.ui;

import gruntspud.actions.DefaultGruntspudAction;
import gruntspud.actions.GruntspudAction;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.KeyStroke;


public class ToolButton extends JButton {
    
    public final static int USE_SMALL_ICON = 0;
    public final static int USE_MEDIUM_ICON = 1;
    public final static int USE_LARGE_ICON = 2;
    
    private final static int HONOR_SHOW_TEXT_VALUE = 0;

    //	
    private boolean disableAccelerators;
    
    public ToolButton(Icon icon) {
        super();
        init(null, USE_SMALL_ICON, false, icon);
    }

    public ToolButton(Action a,boolean showText, boolean smallIcon, boolean disableAccelerators) {
        this(a, smallIcon ? USE_SMALL_ICON : USE_MEDIUM_ICON, disableAccelerators, showText);
    }


    public ToolButton(Action a, int iconType, boolean disableAccelerators, boolean showText) {
        super(a);
        this.disableAccelerators = disableAccelerators;
        init(a, iconType, showText, null);
    }
    
    private void init(Action a, int iconType, boolean showText, Icon icon) {
        addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                if(isEnabled()) {
	                setBorderPainted(true);
	                setContentAreaFilled(true);
                }
            }

            public void mouseExited(MouseEvent e) {
                setBorderPainted(false);
                setContentAreaFilled(false);
            }
        });
        setBorderPainted(false);
        setContentAreaFilled(false);

        /*
         * If there is an accelerator, use it instead of the keystroke that
         * is generated from the mnemonic. This causes a problem for some
         * actions, like "All tools" ending up with a second accelerator of
         * ALT+NUMPAD-1, which prevents composing in jEdit.
         */
        if (disableAccelerators) {
            setMnemonic(0);
        } else {
            if (a != null && a.getValue(Action.ACCELERATOR_KEY) != null) {
                setMnemonic(0);
                registerKeyboardAction(a, (KeyStroke) a.getValue(Action.ACCELERATOR_KEY), JButton.WHEN_IN_FOCUSED_WINDOW);
            }
        }
        
        if(a == null || icon != null) {
            setIcon(icon);
        }
        else {
	        switch(iconType) {
	        	case USE_LARGE_ICON:
	                setIcon((Icon) a.getValue(DefaultGruntspudAction.LARGE_ICON));
	                break;
	        	case USE_MEDIUM_ICON:
	                setIcon((Icon) a.getValue(DefaultGruntspudAction.ICON));
	                break;
	            default:
	                setIcon((Icon) a.getValue(DefaultGruntspudAction.SMALL_ICON));
	            break;
	        }
        }
        if (!showText) {
            setText(null);
        }
    }

    public Insets getMargin() {
        return UIUtil.EMPTY_INSETS;
    }

    public boolean isRequestFocusEnabled() {
        return false;
    }

    public boolean isFocusTraversable() {
        return false;
    }

}