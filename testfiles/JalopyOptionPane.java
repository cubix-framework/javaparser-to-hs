/*
 * Copyright (c) 2001-2002, Marco Hunsicker. All rights reserved.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 */
package de.hunsicker.jalopy.plugin.jedit.option;

import org.gjt.sp.jedit.AbstractOptionPane;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.SwingUtilities;

import de.hunsicker.jalopy.storage.Convention;
import de.hunsicker.jalopy.swing.AbstractSettingsPage;
import de.hunsicker.jalopy.swing.ValidationException;
import de.hunsicker.swing.ErrorDialog;
import de.hunsicker.swing.util.SwingHelper;


/**
 * Serves as wrapper for a Jalopy settings page in order to embedd it into the jEdit Global Options
 * dialog.
 *
 * @author <a href="http://jalopy.sf.net/contact.html">Marco Hunsicker</a>
 * @version $Revision: 1.4 $
 */
public final class JalopyOptionPane
    extends AbstractOptionPane
{
    //~ Instance variables ---------------------------------------------------------------

    /** The actual settings page to embedd. */
    private AbstractSettingsPage _page;

    /** The classname of the settings page to embedd. */
    private String _panelClass;

    //~ Constructors ---------------------------------------------------------------------

    /**
     * Creates a new JalopyOptionPane object.
     *
     * @param labelname the name of the option pane.
     * @param classname the name of the settings page class.
     */
    public JalopyOptionPane(String labelName, String className)
    {
        super(labelName);

        _panelClass = className;

        setPreferredSize(new Dimension(450, 450));
        setMaximumSize(new Dimension(500, 500));
    }

    //~ Methods --------------------------------------------------------------------------

    /**
     * Performs initialization, sets up the option's pane contents.
     */
    protected void _init()
    {
        try
        {
            Class clazz = Class.forName(_panelClass);
            _page = (AbstractSettingsPage)clazz.newInstance();
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();

            return;
        }

        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        GridBagConstraints c = new GridBagConstraints();
        SwingHelper.setConstraints(
            c, 0, 0, GridBagConstraints.REMAINDER, 1, 1.0, 1.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, c.insets, 0, 0);
        layout.setConstraints(_page, c);
        add(_page);

        this.initialized = true;
    }


    /**
     * Saves the options.
     */
    protected void _save()
    {
        try
        {
            _page.validateSettings();
            _page.updateSettings();
        }
        catch (ValidationException ex)
        {
            return;
        }

        // save the code convention
        try
        {
            /**
             * @todo if the user presses OK/Apply in jEdit's Global Options dialog, this
             *       results in calling this method as often as settings panel were
             *       added to the option dialog which is wasteful Implement the flush()
             *       method so that the convention will only be saved if indeed
             *       something changed
             */
             Convention.getInstance().flush();
        }
        catch (Throwable ex)
        {
            Window owner = SwingUtilities.windowForComponent(this);
            ErrorDialog dialog = ErrorDialog.create(owner, ex);
            dialog.setVisible(true);
            dialog.dispose();
        }
    }
}
