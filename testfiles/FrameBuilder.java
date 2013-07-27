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
package org.frapuccino.common;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

/**
 * Utility class to create JFrame objects.
 * @author fdietz
 * @author redsolo
 */
public final class FrameBuilder {

    /**
     * Utility constructor.
     */
    private FrameBuilder() {
    }

    /**
     * Creates a JFreame with the component.
     * @param c the component to put in the JFrame.
     * @return a JFrame.
     */
    public static JFrame createFrame(JComponent c) {
        return createFrame(c, true);
    }

    /**
     * Creates a JFreame with the component.
     * @param c the component to put in the JFrame.
     * @param useScrollpane if the component should be put into a scroll pane or not.
     * @return a JFrame.
     */
    public static JFrame createFrame(JComponent c, boolean useScrollpane) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout());

        if (useScrollpane) {
            frame.getContentPane().add(new JScrollPane(c), BorderLayout.CENTER);
        } else {
            frame.getContentPane().add(c, BorderLayout.CENTER);
        }

        frame.pack();

        return frame;
    }
}
